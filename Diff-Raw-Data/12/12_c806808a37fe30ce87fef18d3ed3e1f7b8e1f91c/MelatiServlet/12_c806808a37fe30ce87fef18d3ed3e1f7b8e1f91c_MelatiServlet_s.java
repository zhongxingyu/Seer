 package org.melati;
 
 import java.util.*;
 import java.io.*;
 import org.melati.util.*;
 import org.melati.poem.*;
 import org.webmacro.*;
 import org.webmacro.util.*;
 import org.webmacro.engine.*;
 import org.webmacro.servlet.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public abstract class MelatiServlet extends MelatiWMServlet {
 
   /**
   * <A NAME=hackedVariable>You must use a hacked version of
   * <TT>org.webmacro.engine.Variable</TT> with Melati.</A> Sorry this has to go
   * into <TT>org.webmacro.engine</TT>: lobby Justin to stop making everything
   * final or package-private or static!  It will probably not break your
   * existing WebMacro code if you put it in your <TT>CLASSPATH</TT> since its
   * semantics are essentially the same as the traditional ones until configured
   * otherwise.  You can get the hacked version by anonymous CVS from melati.org
   * (see the <A
  * HREF=http://paneris.org/cgi-bin/cvsweb.cgi/~checkout~/org/melati/qa/Installation.html>Installation
   * guide</A>).
   */
 
   public static final Object check =
       org.webmacro.engine.Variable.youNeedToBeUsingAVersionOfVariableHackedForMelati;
 
   static final String
       OVERLAY_PARAMETERS = "org.melati.MelatiServlet.overlayParameters",
       USER = "org.melati.MelatiServlet.user";
 
   /**
    * Melati's main entry point.  Override this to do WebMacro-like things and
    * return a WebMacro template---like <TT>WMServlet.handle</TT>.
    *
    * <UL>
    * <LI>
    *
    * <A NAME=pathinfoscan>By default, the path info of the URL by which the
    * servlet was called up is examined to determine the `logical name' of the
    * Melati POEM database to which the servlet should connect, and possibly a
    * table within that database, an object within that table, and a `method' to
    * apply to that object.</A>  The URL is expected to take one of the following
    * forms:
    *
    * <BLOCKQUOTE><TT>
    *     http://<I>h</I>/<I>s</I>/<I>db</I>/
    * <BR>http://<I>h</I>/<I>s</I>/<I>db</I>/<I>meth</I>
    * <BR>http://<I>h</I>/<I>s</I>/<I>db</I>/<I>tbl</I>/<I>meth</I> 
    * <BR>http://<I>h</I>/<I>s</I>/<I>db</I>/<I>tbl</I>/<I>troid</I>/<I>meth</I>
    * </TT></BLOCKQUOTE>
    *
    * and the following components are broken out of the path info and passed to
    * your application code in the <TT>melati</TT> parameter (which is also
    * copied automatically into <TT>context</TT> so that it is easily available
    * in templates):
    *
    * <TABLE>
    *   <TR>
    *     <TD><TT><I>h</I></TT></TD>
    *     <TD>host name, such as <TT>www.melati.org</TT></TD>
    *   </TR>
    *   <TR>
    *     <TD><TT><I>s</I></TT></TD>
    *     <TD>
    *       servlet-determining part, such as
    *       <TT>melati/org.melati.admin.Admin</TT>
    *     </TD>
    *   </TR>
    *   <TR>
    *     <TD><TT><I>db</I></TT></TD>
    *     <TD>
    *       The first element of the path info is taken to be the `logical name'
    *       of the Melati POEM database to which the servlet should connect.  It
    *       is mapped onto JDBC connection details via the config file
    *       <TT>org.melati.LogicalDatabase.properties</TT>, of which there is an
    *       example in the source tree.  This is automatically made available in
    *       templates as <TT>$melati.Database</TT>.
    *     </TD>
    *   <TR>
    *     <TD><TT><I>tbl</I></TT></TD>
    *     <TD>
    *       The DBMS name of a table with which the servlet is concerned:
    *       perhaps it is meant to list its contents.  This is automatically
    *       made available in templates as <TT>$melati.Table</TT>.
    *     </TD>
    *   </TR>
    *   <TR>
    *     <TD><TT><I>troid</I></TT></TD>
    *     <TD>
    *       The POEM `troid' (table row identifier, or row-unique integer) of a
    *       row within <TT><I>tbl</I></TT> with which the servlet is concerned:
    *       perhaps it is meant to display it.  This is automatically made
    *       available in templates as <TT>$melati.Object</TT>.
    *     </TD>
    *   </TR>
    *   <TR>
    *     <TD><TT><I>meth</I></TT></TD>
    *     <TD>
    *       A freeform string telling your servlet what it is meant to do.  This
    *       is automatically made available in templates as
    *       <TT>$melati.Method</TT>.
    *     </TD>
    *   </TR>
    * </TABLE>
    *
    * You can change the way these things are determined by overriding
    * <TT>melatiContext</TT>.
    *
    * <LI>
    *
    * Any POEM database operations you perform will be done with the access
    * rights of the POEM <TT>User</TT> associated with the servlet session.  If
    * there is no established servlet session, the current user will be set to
    * the default `guest' user.  If this method terminates with an
    * <TT>AccessPoemException</TT>, indicating that you have attempted something
    * which you aren't entitled to do, the <TT>loginTemplate</TT> method will be
    * invoked instead; once the user has logged in, the original request will be
    * retried.
    *
    * <LI>
    *
    * No changes made to the database by other concurrently executing threads
    * will be visible to you (in the sense that once you have seen a particular
    * version of a record, you will always subsequently see the same one), and
    * your own changes will not be made permanent until this method completes
    * successfully or you perform an explicit <TT>PoemThread.commit()</TT>.  If
    * it terminates with an exception or you issue a
    * <TT>PoemThread.rollback()</TT>, your changes will be lost.
    *
    * </UL>
    *
    * @param context	a WebMacro `context' object, representing the
    *                    template expansion namespace and carrying the servlet
    *                    request, session <I>etc.</I>
    * @param melati	a source of information about the Melati database
    *                    context (database, table, object) and utility objects
    *                    like error handlers
    *
    * @see org.melati.poem.Database#guestUser
    * @see #loginTemplate
    * @see org.melati.poem.PoemThread#commit
    * @see org.melati.poem.PoemThread#rollback
    * @see org.webmacro.servlet.WMServlet#handle
    * @see #melatiContext
    */
 
   protected Template handle(WebContext context, Melati melati)
       throws PoemException, WebMacroException {
     return null;
   }
 
   /**
    * Provided for drop-in compatibility with servlets derived from
    * <TT>WMServlet</TT>.  You probably mean to use <TT>handle(WebContext,
    * Melati)</TT>.
    *
    * @see #handle(org.webmacro.servlet.WebContext, org.melati.Melati)
    */
 
   protected Template handle(WebContext context)
       throws PoemException, WebMacroException {
     return handle(context, (Melati)context.get("melati"));
   }
 
   /**
    * The class name of the class implementing the login servlet.  Unless
    * overridden, this is <TT>org.melati.Login</TT>.
    *
    * @see org.melati.Login
    */
 
   protected String loginPageServletClassName() {
     return "org.melati.Login";
   }
 
   /**
    * The URL of the login servlet.  Unless overridden, this is computed by
    * substituting <TT>loginPageServletClassName()</TT> into the URL of the
    * request being serviced.
    *
    * @param request	the request currently being serviced
    *
    * @see #loginPageServletClassName
    */
 
   protected String loginPageURL(HttpServletRequest request) {
     StringBuffer url = new StringBuffer();
     url.append(request.getScheme());
     url.append("://");
     url.append(request.getServerName());
     if (request.getScheme().equals("http") && request.getServerPort() != 80 ||
         request.getScheme().equals("https") && request.getServerPort() != 443) {
       url.append(':');
       url.append(request.getServerPort());
     }
 
     String servlet = request.getServletPath();
     if (servlet != null)
       url.append(servlet.substring(0, servlet.lastIndexOf('/') + 1));
 
     url.append(loginPageServletClassName());
     url.append('/');
     // FIXME cut the front off the pathinfo to retrieve the DB name
     String pathInfo = request.getPathInfo();
     url.append(pathInfo.substring(1, pathInfo.indexOf('/', 1) + 1));
 
     return url.toString();
   }
 
   /**
    * Handle an exception that occurs during the execution of
    * <TT>melatiHandle</TT> or during the expansion of the template it returns.
    * The base version returns the standard WebMacro error template as defined
    * in your <TT>WebMacro.properties</TT>, except if the problem was an access
    * failure (<TT>AccessPoemException</TT>), in which case the client is
    * redirected to the login page.
    *
    * @param context     the <TT>WebContext</TT> of the original template
    *
    * @param exception   what went wrong: for problems expanding template
    *                    variables when variable error propagation is enabled,
    *                    you will see a <TT>VariableException</TT> (available in
    *                    the <A HREF=#hackedVariable>Melati-hacked version of
    *                    WebMacro</A> against which you must compile Melati)
    *                    whose <TT>subException</TT> is what you are interested
    *                    in
    *
    * @return a template to expand, or <TT>null</TT> if you have already
    *         sent something (like a redirect) back to the client
    *
    * @see #melatiHandle
    * @see org.melati.poem.AccessPoemException
    * @see #loginPageURL
    * @see org.webmacro.util.VariableException
    */
 
   protected Template handleException(WebContext context, Exception exception)
       throws Exception {
 
     Exception underlying =
         exception instanceof VariableException ?
           ((VariableException)exception).subException : exception;
 
     if (underlying == null || !(underlying instanceof AccessPoemException))
       super.handleException(context, exception);
     else {
 
       AccessPoemException accessException = (AccessPoemException)underlying;
 
       HttpServletRequest request = context.getRequest();
       HttpServletResponse response = context.getResponse();
 
       HttpSession session = request.getSession(true);
 
       session.putValue(Login.TRIGGERING_REQUEST_PARAMETERS,
                        new HttpServletRequestParameters(request));

      if (accessException != null)
        session.putValue(Login.TRIGGERING_EXCEPTION, accessException);
      else
        session.removeValue(Login.TRIGGERING_EXCEPTION);
 
       try {
         response.sendRedirect(loginPageURL(request));
       }
       catch (IOException e) {
         throw new HandlerException(e.toString());
       }
     }
 
     return null;
   }
 
   private void superDoRequest(WebContext context)
       throws ServletException, IOException {
     super.doRequest(context);
   }
 
   private static class TrappedException extends RuntimeException {
     public TrappedException(String message) {
       super(message);
     }
   }
 
   /**
    * Implements the path info scanning process described <A
    * HREF=#pathinfoscan>above</A>.  Override this to do it differently,
    * <I>e.g.</I> using named fields rather than path info.  Note that you must
    * whatever you do return a legal <TT>MelatiContext</TT>, and its
    * <TT>logicalDatabase</TT> field must be filled in.
    *
    * @see MelatiContext#logicalDatabase
    */
 
   protected MelatiContext melatiContext(WebContext context)
       throws MelatiException {
     try {
       String[] parts = StringUtils.split(context.getRequest().getPathInfo(),
 					 '/');
 
       if (parts.length < 2)
 	// FIXME make this nicer since users will see it if they play around
 	// with URLs
 	throw new HandlerException(
             "The servlet expects to see pathinfo in the form " +
 	    "/db/, /db/method, /db/table/method or /db/table/troid/method");
 
       MelatiContext it = new MelatiContext();
 
       it.method = parts[parts.length - 1];
 
       switch (parts.length - 1) {
         case 4:
           it.troid =
               parts[3].equals("new") ? // see Add.wm
                   new Integer(-1) : new Integer(parts[3]);
         case 3:
           it.table = parts[2];
         default:
           it.logicalDatabase = parts[1];  // provoke exception if 0-length
       }
 
       return it;
     }
     catch (Exception e) {
       throw new PathInfoException(null);
     }
   }
 
   /**
    * Overrides a basic WMServlet entry point to allow Melati to set up its
    * environment before WebMacro takes over.  We have to take control very
    * early, since the POEM database session must be wrapped around the whole
    * WebMacro logic: the session must be active while the template is expanded.
    * NB the application programmer's entry point to Melati is
    * <TT>melatiHandle</TT>, above.
    *
    * @see #melatiHandle
    */
 
   protected void doRequest(WebContext contextIn)
       throws ServletException, IOException {
 
     final HttpSession session = contextIn.getSession();
 
     // First off, is the user continuing after a login?  If so, we want to
     // recover any POSTed fields from the request that triggered it.
 
     WebContext newContext = null;
 
     synchronized (session) {
       HttpServletRequestParameters oldParams =
           (HttpServletRequestParameters)session.getValue(OVERLAY_PARAMETERS);
       if (oldParams != null) {
         session.removeValue(OVERLAY_PARAMETERS);
         try {
           newContext = contextIn.clone(
               new ReconstructedHttpServletRequest(oldParams,
                                                   contextIn.getRequest()),
               contextIn.getResponse());
         }
         catch (ReconstructedHttpServletRequestMismatchException e) {
         }
       }
     }
 
     final WebContext context = newContext == null ? contextIn : newContext;
 
     try {
       final MelatiContext melatiContext = melatiContext(context);
 
       // Set up a POEM session and call the application code
 
       // dearie me, what a lot of hoops to jump through
       // at the end of the day Java is terribly poorly suited to this kind of
       // lambda idiom :(
 
       final MelatiServlet _this = this;
 
       final Database database;
       try {
 	database = LogicalDatabase.named(melatiContext.logicalDatabase);
       }
       catch (DatabaseInitException e) {
 	e.printStackTrace();
 	throw new ServletException(e.toString());
       }
 
       database.logSQL = true;
 
       database.inSession(
           AccessToken.root,
           new PoemTask() {
             public void run() {
               try {
                 context.put("melati", new Melati(context, database,
 						 melatiContext));
 		context.put(Variable.EXCEPTION_HANDLER,
 			    PropagateVariableExceptionHandler.it);
 		User user = (User)session.getValue(USER);
 		PoemThread.setAccessToken(
 		    user == null ? database.guestAccessToken() : user);
                 _this.superDoRequest(context);
               }
               catch (Exception e) {
                 // FIXME oops we have to do this in-session!  This is because
                 // some PoemExceptions (might?) generate their messages on the
                 // fly from Persistents that can't be interrogated outside a
                 // database session.  Indeed the toString() can actually
                 // generate a further exception.  Not very satisfactory.
 
                 e.printStackTrace();
                 throw new TrappedException(e.toString());
               }
             }
           });
     }
     catch (TrappedException e) {
       throw new ServletException(e.getMessage());
     }
     catch (MelatiException e) {
       throw new ServletException(e.getMessage());
     }
   }
 }
