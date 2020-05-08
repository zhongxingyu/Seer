 package com.smallcultfollowing.lathos;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.text.AttributedCharacterIterator;
 import java.text.MessageFormat;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 public abstract class Lathos
 {
     private static ThreadLocal<Context> currentContext = new ThreadLocal<Context>();
 
     /**
      * Starts and returns a "batteries included" Lathos HTTP server on port
      * {@code port}, using the default web server (currently Jetty). If port is
      * 0, returns a {@code DevNullServer}.
      */
     public static LathosServer serverOnPort(int port) throws Exception
     {
         LathosServer server = JettyServer.start(port);
         setupServer(server);
         return server;
     }
 
     /**
      * Performs the standard setup for a server, adding default object renders,
      * a link cache, and other goodies.
      */
     public static void setupServer(LathosServer server)
     {
         server.addRootPage(new IndexPage());
         server.addRootPage(new StaticPage());
 
         server.setLinkCache(new DefaultLinkCache(10000));
 
         server.addRenderer(new ReflectiveRenderer());
         server.addRenderer(new ConstantRenderer());
         server.addRenderer(new ThrowableRenderer());
         server.addRenderer(new CollectionRenderer());
         server.addRenderer(new PageRenderer());
     }
 
     public static Context context()
     {
         Context ctx = currentContext.get();
         if (ctx == null)
             return DevNullContext.instance;
         return ctx;
     }
 
     /**
      * Sets the current context that will be returned by context() for this
      * thread, returning the current value.
      * 
      * This method is intended to be used in a lexically-scoped fashion, like
      * so:
      * 
      * <pre>
      *  final Context oldContext = Lathos.setContext(foo);
      *  try {
      *     ...
      *  } finally {
      *     Lathos.setContext(oldContext);
      *  }
      * </pre>
      * 
      * @param newCtx
      *            the new context to use for this thread
      * @returns the current context for this thread
      */
     public static Context setContext(Context newCtx)
     {
         Context oldCtx = currentContext.get();
         currentContext.set(newCtx);
         return oldCtx;
     }
 
     /**
      * Creates a new context associated with {@code server} whose stack contains
      * the index page for {@code server} (if any).
      * 
      * @param server
      *            the server to associate with the context. If null, the
      *            function returns {@link DevNullContext#instance}.
      */
     public static Context newContextWithIndex(LathosServer server)
     {
         if (server == null)
             return DevNullContext.instance;
 
         Context ctx = server.context();
 
         Object indexPage = server.getIndexPage();
         if (indexPage != null && indexPage instanceof ExtensiblePage) {
             ctx.push((ExtensiblePage) indexPage);
         }
 
         return ctx;
     }
 
     /**
      * Creates a new context associated with {@code server} whose stack contains
      * the page {@code page}.
      * 
      * @param server
      *            the server to associate with the context. If null, the
      *            function returns {@link DevNullContext#instance}.
      * 
      * @param page
      *            the page to push. If null, no page is pushed.
      */
     public static Context newContextWithPage(LathosServer server, ExtensiblePage page)
     {
         if (server == null)
             return DevNullContext.instance;
 
         Context ctx = server.context();
 
         if (page != null)
             ctx.push(page);
 
         return ctx;
     }
 
     public static Line log(Object... objs)
     {
         return context().log(objs);
     }
 
     /**
      * Emits a table containing all fields of {@code page} and their values.
      * Fields annotated with {@link Ignore} will not be printed. This is the
      * method which is used to render an object as a page by default, unless the
      * object implements the interface {@link Page}.
      */
     public static void reflectiveRenderDetails(Object page, Output out, Link link) throws IOException
     {
         out.table();
 
         out.tr();
         out.th().text("Field")._th();
         out.th().text("Value")._th();
         out._tr();
 
         Class<?> cls = page.getClass();
         while (cls != Object.class) {
             for (Field fld : cls.getDeclaredFields()) {
                 if (fld.getAnnotation(Ignore.class) != null)
                     continue;
 
                 fld.setAccessible(true);
                 out.tr();
 
                 Link fldLink = new RelativeLink(link, fld.getName());
 
                 out.td();
                 out.text(fld.getName());
                 out._td();
 
                 out.td();
                 Object value;
                 try {
                     value = fld.get(page);
                 } catch (Exception e) {
                     value = e;
                 }
                 out.obj(fldLink, value);
                 out._td();
 
                 out._tr();
             }
 
             for (Method mthd : cls.getDeclaredMethods()) {
                 AllowReflectiveDeref allow = mthd.getAnnotation(AllowReflectiveDeref.class);
                 if (allow == null || !allow.showInDetails())
                     continue;
 
                 mthd.setAccessible(true);
                 out.tr();
                 Link mthdLink = new RelativeLink(link, mthd.getName());
 
                 out.td();
                 out.text(mthd.getName());
                 out._td();
 
                 out.td();
                 Object value;
                 try {
                     value = mthd.invoke(page);
                 } catch (Exception e) {
                     value = e;
                 }
                 out.obj(mthdLink, value);
                 out._td();
 
                 out._tr();
             }
 
             cls = cls.getSuperclass();
         }
 
         out._table();
 
     }
 
     private static boolean indicesEqual(Integer oldIndex, Integer newIndex)
     {
         if (oldIndex == newIndex)
             return true;
 
         if (oldIndex == null || newIndex == null)
             return false;
 
         return newIndex.equals(oldIndex);
     }
 
     public static void renderI18nSummary(
             String messageName,
             Object[] arguments,
             Output out,
             Link mainLink,
             Link argumentsLink) throws IOException
     {
         ResourceBundle bundle = out.server.getResourceBundle();
         fallback: if (bundle != null) {
             String messageFmt;
             try {
                 messageFmt = bundle.getString(messageName);
             } catch (MissingResourceException e) {
                 break fallback;
             }
 
             MessageFormat fmt = new MessageFormat(messageFmt);
             AttributedCharacterIterator iter = fmt.formatToCharacterIterator(arguments);
 
             Integer prevArgument = null;
             Link currentLink = null;
 
            for(char c = iter.current(); c != AttributedCharacterIterator.DONE; c = iter.next()) {
                 Integer argument = (Integer) iter.getAttribute(MessageFormat.Field.ARGUMENT);
 
                 if (!indicesEqual(prevArgument, argument)) {
                     if (currentLink != null)
                         out._a(currentLink);
 
                     if (argument != null) {
                         currentLink = new IndexLink(argumentsLink, argument);
                         out.a(currentLink);
                     } else {
                         currentLink = null;
                     }
 
                     prevArgument = argument;
                 }
 
                 // if some area is not otherwise linked, link to "mainLink"
                 if (currentLink == null) {
                     currentLink = mainLink;
                     out.a(currentLink);
                 }
 
                 out.text(Character.toString(c));
             }
 
             if (currentLink != null) {
                 out._a(currentLink);
             }
 
             return;
         }
 
         out.a(mainLink);
         out.text(messageName);
         out._a(mainLink);
         out.text("(");
         for (int i = 0; i < arguments.length; i++) {
             if (i > 0)
                 out.text(", ");
             out.obj(argumentsLink, i, arguments[i]);
         }
         out.text(")");
     }
 
     /**
      * Reflectively dereferences a link from this object by looking for a field
      * or method with that name.
      * 
      * The behavior of this method can be somewhat controlled via annotation.
      * Fields are enabled by default but can be annotated with {@link Ignore} to
      * make them invisible and not followable. Methods are <b>disabled</b> by
      * default but can be annotated with {@link AllowReflectiveDeref} to make
      * them followable.
      * 
      * @throws InvalidDeref
      *             if there is no field {@code link}
      */
     public static Object reflectiveDerefPage(Object parentPage, String link) throws InvalidDeref
     {
         // Search for a field with the name link:
         Class<?> cls = parentPage.getClass();
         while (cls != Object.class) {
             Field fld;
             try {
                 fld = cls.getDeclaredField(link);
                 if (fld.getAnnotation(Ignore.class) == null) {
                     fld.setAccessible(true);
                     try {
                         return fld.get(parentPage);
                     } catch (Exception e) {
                         return e;
                     }
                 }
             } catch (SecurityException e1) {
             } catch (NoSuchFieldException e1) {
             }
             cls = cls.getSuperclass();
         }
 
         // Search for a method with the name link:
         // (This is particularly important for Scala)
         cls = parentPage.getClass();
         while (cls != Object.class) {
             try {
                 Method mthd = cls.getDeclaredMethod(link);
                 if (mthd.getAnnotation(AllowReflectiveDeref.class) != null) {
                     mthd.setAccessible(true);
                     try {
                         return mthd.invoke(parentPage);
                     } catch (Exception e) {
                         return e;
                     }
                 }
             } catch (SecurityException e1) {
             } catch (NoSuchMethodException e) {
             }
             cls = cls.getSuperclass();
         }
 
         throw InvalidDeref.instance;
     }
 
     public static void headerRow(Output out, Object... columns) throws IOException
     {
         out.tr();
         for (Object column : columns) {
             out.th();
             out.obj(column);
             out._th();
         }
         out._tr();
     }
 
     public static void row(Output out, Object... columns) throws IOException
     {
         out.tr();
         for (Object column : columns) {
             out.td();
             out.obj(column);
             out._td();
         }
         out._tr();
     }
 
     /**
      * Reflectively renders an object as a link by by using its
      * {@link Object#toString()} value. This is the method which is used to
      * render an object by default, unless the object implements the interface
      * {@link Page}.
      */
     public static void reflectiveRenderSummary(Object obj, Output out, Link link) throws IOException
     {
         if(obj != null) {
             out.a(link);
             out.text(obj.toString());
             out._a(link);
         } else {
             out.text("null");
         }
     }
 
     /**
      * Pushes and returns a sub-page with the content {@code objs} on the
      * current context.
      */
     public static ExtensiblePage indent(Object... objs)
     {
         Context ctx = Lathos.context();
         ExtensiblePage page = ctx.newPage(null, objs);
         ctx.embed(page);
         ctx.push(page);
         return page;
     }
 
     public static Object invalidDerefIfNull(Object object) throws InvalidDeref
     {
         if (object == null)
             throw InvalidDeref.instance;
         return object;
     }
 
     public static void reflectiveRenderTitle(Object obj, Output out, Link link) throws IOException
     {
         if (obj == null) {
             out.text("Null");
         } else {
             Class<?> cls = obj.getClass();
             out.text("Instance of ");
             out.text(cls.getName());
             out.text(" with identity hash ");
             out.text(String.format("%x", System.identityHashCode(obj)));
         }
     }
 
 }
