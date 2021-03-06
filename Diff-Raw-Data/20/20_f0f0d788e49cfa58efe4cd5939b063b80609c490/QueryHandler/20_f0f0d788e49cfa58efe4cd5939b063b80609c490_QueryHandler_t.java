 /*
  * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
  * Copyright (c) 2010, 2011 On-Site.com.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 only, as
  * published by the Free Software Foundation.  Oracle designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Oracle in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 2 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 2 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
  * or visit www.oracle.com if you need additional information or have any
  * questions.
  */
 
 
 /*
  * The Original Code is HAT. The Initial Developer of the
  * Original Code is Bill Foote, with contributions from others
  * at JavaSoft/Sun.
  */
 
 package com.sun.tools.hat.internal.server;
 
 import java.io.PrintWriter;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableListMultimap;
 import com.google.common.collect.ImmutableMultimap;
 import com.google.common.collect.Multimap;
 import com.sun.tools.hat.internal.model.*;
 import com.sun.tools.hat.internal.util.Misc;
 
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.Formatter;
 import java.util.Map;
 import java.io.UnsupportedEncodingException;
 
 /**
  *
  * @author      Bill Foote
  */
 
 
 abstract class QueryHandler implements Runnable {
     protected enum GetIdString implements Function<JavaClass, String> {
         INSTANCE;
 
         @Override
         public String apply(JavaClass clazz) {
             return clazz.getIdString();
         }
     }
 
     protected static class ClassResolver implements Function<String, JavaClass> {
         private final Snapshot snapshot;
         private final boolean allowNull;
 
         public ClassResolver(Snapshot snapshot, boolean allowNull) {
             this.snapshot = snapshot;
             this.allowNull = allowNull;
         }
 
         @Override
         public JavaClass apply(String name) {
             if (name == null && allowNull) {
                 return null;
             }
             JavaClass result = snapshot.findClass(name);
             Preconditions.checkNotNull(result, "class not found: %s", name);
             return result;
         }
     }
 
     protected String path;
     protected String urlStart;
     protected String query;
     protected PrintWriter out;
     protected Snapshot snapshot;
     protected ImmutableListMultimap<String, String> params;
 
     void setPath(String s) {
         path = s;
     }
 
     void setUrlStart(String s) {
         urlStart = s;
     }
 
     void setQuery(String s) {
         query = s;
     }
 
     void setOutput(PrintWriter o) {
         this.out = o;
     }
 
     void setSnapshot(Snapshot ss) {
         this.snapshot = ss;
     }
 
     void setParams(ImmutableListMultimap<String, String> params) {
         this.params = params;
     }
 
     protected static String encodeForURL(String s) {
         try {
             s = URLEncoder.encode(s, "UTF-8");
         } catch (UnsupportedEncodingException ex) {
             // Should never happen
             throw new AssertionError(ex);
         }
         return s;
     }
 
     protected void startHtml(String title) {
         out.print("<html><title>");
         print(title);
         out.println("</title>");
         out.println("<body bgcolor=\"#ffffff\"><center><h1>");
         print(title);
         out.println("</h1></center>");
     }
 
     protected void endHtml() {
         out.println("</body></html>");
     }
 
     protected void error(String msg) {
         out.println(msg);
     }
 
     protected void printAnchorStart() {
         out.print("<a href=\"");
         out.print(urlStart);
     }
 
     protected void printThingAnchorTag(long id) {
         printAnchorStart();
         out.print("object/");
         printHex(id);
         out.print("\">");
     }
 
     protected void printObject(JavaObject obj) {
         printThing(obj);
     }
 
     protected void printThing(JavaThing thing) {
         if (thing == null) {
             out.print("null");
             return;
         }
         if (thing instanceof JavaHeapObject) {
             JavaHeapObject ho = (JavaHeapObject) thing;
             long id = ho.getId();
             if (id != -1L) {
                 if (ho.isNew())
                 out.println("<strong>");
                 printThingAnchorTag(id);
             }
             print(thing.toString());
             if (id != -1) {
                 if (ho.isNew())
                     out.println("[new]</strong>");
                 out.print(" (" + ho.getSize() + " bytes)");
                 out.println("</a>");
             }
         } else {
             print(thing.toString());
         }
     }
 
     protected void printRoot(Root root) {
         StackTrace st = root.getStackTrace();
         boolean traceAvailable = (st != null) && (st.getFrames().length != 0);
         if (traceAvailable) {
             printAnchorStart();
             out.print("rootStack/");
             printHex(root.getIndex());
             out.print("\">");
         }
         print(root.getDescription());
         if (traceAvailable) {
             out.print("</a>");
         }
     }
 
     protected void printClass(JavaClass clazz) {
         if (clazz == null) {
             out.println("null");
             return;
         }
         printAnchorStart();
         out.print("class/");
         print(encodeForURL(clazz));
         out.print("\">");
         print(clazz.toString());
         out.println("</a>");
     }
 
     protected static String encodeForURL(JavaClass clazz) {
         if (clazz.getId() == -1) {
             return encodeForURL(clazz.getName());
         } else {
             return clazz.getIdString();
         }
     }
 
     protected void printField(JavaField field) {
         print(field.getName() + " (" + field.getSignature() + ")");
     }
 
     protected void printStatic(JavaStatic member) {
         JavaField f = member.getField();
         printField(f);
         out.print(" : ");
         if (f.hasId()) {
             JavaThing t = member.getValue();
             printThing(t);
         } else {
             print(member.getValue().toString());
         }
     }
 
     protected void printStackTrace(StackTrace trace) {
         StackFrame[] frames = trace.getFrames();
         for (StackFrame f : frames) {
             String clazz = f.getClassName();
             out.print("<font color=purple>");
             print(clazz);
             out.print("</font>");
             print("." + f.getMethodName() + "(" + f.getMethodSignature() + ")");
             out.print(" <bold>:</bold> ");
             print(f.getSourceFileName() + " line " + f.getLineNumber());
             out.println("<br>");
         }
     }
 
     protected void printHex(long addr) {
         if (snapshot.getIdentifierSize() == 4) {
             out.print(Misc.toHex((int)addr));
         } else {
             out.print(Misc.toHex(addr));
         }
     }
 
     protected long parseHex(String value) {
         return Misc.parseHex(value);
     }
 
     protected void print(String str) {
         out.print(Misc.encodeHtml(str));
     }
 
     /**
      * Returns a link to <code>/<var>path</var>/<var>pathInfo</var></code>
      * with the given label and parameters.
      *
      * @param path the static portion of the link target (should only
      *             contain trusted text)
      * @param pathInfo the non-static portion of the link target (will be
      *                 URL-encoded)
      * @param label the link text to use
      * @param params any {@code GET} parameters to append to the link target
      * @return an HTML {@code <a>} tag formatted as described
      */
     protected static String formatLink(String path, String pathInfo,
             String label, Multimap<String, String> params) {
         StringBuilder sb = new StringBuilder();
         Formatter fmt = new Formatter(sb);
        fmt.format("<a href='/%s/%s?", path,
                pathInfo == null ? "" : encodeForURL(pathInfo));
         if (params != null) {
             for (Map.Entry<String, String> entry : params.entries()) {
                 fmt.format("%s=%s&", encodeForURL(entry.getKey()),
                         encodeForURL(entry.getValue()));
             }
         }
         sb.setLength(sb.length() - 1);
         fmt.format("'>%s</a>", Misc.encodeHtml(label));
         return sb.toString();
     }
 
     /**
      * Returns a link to <code>/<var>path</var>/<var>pathInfo</var></code>
      * that can be used to construct a referrer chain. See also the related
      * {@link #printBreadcrumbs} function.
      *
      * <p>For queries that support referrer chains, there is a primary
      * class that the query works on, followed by a referrer chain that
      * further filters instances. The primary class is specified as
      * {@code clazz}.
      *
      * <p>The referrer chain is always written with parameter name
      * {@code referrer}, so the query handler should use that name to get
      * the referrer chain. Additionally, if the primary class is omitted,
      * then the referrer chain is irrelevant and will not be printed.
      *
      * @param path the static portion of the link target (should only
      *             contain trusted text)
      * @param pathInfo the non-static portion of the link target (will be
      *                 URL-encoded); ignored if {@code name} is omitted
      * @param label the link text to use
      * @param name the parameter name for referring to the primary class;
      *             if omitted, place the class reference in {@code pathInfo}
      * @param clazz the primary class in use
      * @param referrers the referrer chain in use
      * @param tail an optional element to append to the referrer chain
      * @return an HTML {@code <a>} tag formatted as described
      */
     protected static String formatLink(String path, String pathInfo,
             String label, String name, JavaClass clazz,
             Collection<JavaClass> referrers, JavaClass tail) {
         ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();
         if (clazz != null) {
             if (name != null) {
                 builder.put(name, clazz.getIdString());
             } else {
                 pathInfo = clazz.getIdString();
             }
             if (referrers != null) {
                 builder.putAll("referrer", Collections2.transform(referrers,
                         GetIdString.INSTANCE));
             }
             if (tail != null) {
                 builder.put("referrer", tail.getIdString());
             }
         }
         return formatLink(path, pathInfo, label, builder.build());
     }
 
     /**
      * Prints out breadcrumbs for accessing previous elements in the
      * referrer chain.
      *
      * <p>For queries that support referrer chains, there is a primary
      * class that the query works on, followed by a referrer chain that
      * further filters instances. The primary class is specified as
      * {@code clazz}.
      *
      * <p>The referrer chain is always written with parameter name
      * {@code referrer}, so the query handler should use that name to get
      * the referrer chain. Additionally, if the primary class is omitted,
      * then the referrer chain is irrelevant and will not be printed.
      *
      * @param path the static portion of the link target (see {@link #formatLink})
      * @param pathInfo the non-static portion of the link target
      *                 (see {@link #formatLink}); ignored if {@code name}
      *                 is omitted
      * @param name the parameter name for referring to the primary class;
      *             if omitted, place the class reference in {@code pathInfo}
      * @param clazz the primary class in use
      * @param referrers the referrer chain in use
      * @param params any further parameters to be prepended
      */
     protected void printBreadcrumbs(String path, String pathInfo,
             String name, JavaClass clazz, Iterable<JavaClass> referrers,
             Multimap<String, String> params) {
         ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
         if (params != null) {
             builder.putAll(params);
         }
         if (clazz != null) {
             out.print("<p align='center'>");
             if (name != null) {
                 builder.put(name, clazz.getIdString());
             } else {
                 pathInfo = clazz.getIdString();
             }
             out.print(formatLink(path, pathInfo, clazz.getName(), builder.build()));
             for (JavaClass referrer : referrers) {
                 out.print(" &rarr; ");
                 builder.put("referrer", referrer.getIdString());
                 out.print(formatLink(path, pathInfo, referrer.getName(), builder.build()));
             }
             out.println("</p>");
         }
     }
 }
