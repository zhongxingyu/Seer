 package com.atlassian.confluence.extra.snippet;
 
 import com.atlassian.renderer.RenderContext;
 import com.atlassian.renderer.v2.RenderMode;
 import com.atlassian.renderer.v2.SubRenderer;
 import com.atlassian.renderer.v2.macro.BaseMacro;
 import com.atlassian.renderer.v2.macro.MacroException;
 import com.atlassian.confluence.renderer.PageContext;
 import com.atlassian.confluence.setup.BootstrapManager;
 import com.opensymphony.util.TextUtils;
 
 import java.io.StringWriter;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Map;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * <p/>
  * Macro that gets a snippet of the content from a URL. The content will be trimmed down to the content
  * found between some special tags. This is very handy for documentation that includes code. The code
  * can be in CVS (whatever build system is used will make sure it builds properly) and the URL can
  * be a ViewCVS URL. (In case of ViewCVS the URL should point to the download URL of a file). The content
  * is cached for one hour in order to improve performance. The snippet is also unindented to remove excess space.
  * Example of content of the URL:
  * </p>
  * <p/>
  * <pre>
  * this
  * // START SNIPPET: something
  * macro
  * // START SNIPPET: somethingElse
  * is
  * // END SNIPPET: somethingElse
  * very
  * // END SNIPPET: something
  * cool
  * </pre>
  * </p>
  * <p/>
  * Example usage:
  * <pre>
  * {snippet:lang=java|title=Title Value|id=something|linenumbers=true|url=http://some/url/to/viewcvs}
  * </pre>
  * </p>
  * <p/>
  * This will give the following content
  * <pre>
  * {code:java}
  * 1. macro
  * 2. is
  * 3. very
  * {code}
  * </pre>
  * </p>
  * <p/>
  * Parameters:
  * <ul>
  * <li>url - (required) the location of the content</li>
  * <li>id - (optional) the id of the snippet in the URL content</li>
  * <li>lang - (optional) if present, will wrap the content in {code:lang}</li>
  * <li>title - (optional) if present, will wrap the content in {code:title=Title Value}. If present and lang is also present, will wrap code in {code:lang|title=Title Value}</li>
  * <li>linenumbers (optional) if present and true, will add line numbers</li>
  * <li>javadoc (optional) if present and true, will process snippet as if within Javadocs</li>
  * </ul>
  * </p>
  *
  * @author Jon Tirs&eacute;n
  * @author Aslak Helles&oslash;y
  * @author Carlos Villela
  * @version $Revision: 280 $
  */
 public class SnippetMacro extends BaseMacro
 {
 
     private SubRenderer subRenderer;
     SnippetManager snippetManager;
     SnippetErrorManager snippetErrorManager;
     private BootstrapManager bootstrapManager;
 
     public static int NOT_SET = 0;
 
     public String execute(Map parameters, String string, RenderContext renderContext) throws MacroException
     {
         PageContext pageContext = ((PageContext)renderContext);
         String contentTitle = pageContext.getEntity().getTitle();
 
         String id = (String) parameters.get("id");
         if (!TextUtils.stringSet(id)) 
             id = "all";
         String snippetUrl = (String) parameters.get("url");
         if (!TextUtils.stringSet(snippetUrl))
         {
             throw new IllegalArgumentException("'url' is a required parameter");
         }
 
         ResolvedUrl resolvedUrl = snippetManager.resolveUrl(snippetUrl);
         if (resolvedUrl == null)
             throw new IllegalArgumentException("Invalid url: must begin with a configured prefix.");
 
         String linesParam = (String) parameters.get("linenumbers");
 
         // parameters for start:end range
         final String startLineParam = (String) parameters.get("start");
         final String endLineParam = (String) parameters.get("end");
 
         final int startLine = startLineParam == null ? NOT_SET : Integer.parseInt(startLineParam);
         final int endLine = endLineParam == null ? NOT_SET : Integer.parseInt(endLineParam);
 
         URL url = null;
         try {
             url = new URL(resolvedUrl.getResolvedUrl());
         } catch (IOException e)
         {
             throw new RuntimeException(e.getMessage());
         }
 
         String lang = (String) parameters.get("lang");
         String title = (String) parameters.get("title");
         
         boolean codeBlock = false;        
         if(lang != null || title != null) {
             codeBlock = true;
         }
 
         try {
             boolean withLineNumbers = "true".equals(linesParam);
 
             Snippet snippet = snippetManager.getSnippet(url, resolvedUrl.getCredentials(), id);
 
             List<String> lines = snippet.getLines();
             // if there are no actual lines, this snippet is invalid!
             if (lines == null || lines.isEmpty()) {
                 snippetErrorManager.add(new SnippetError(pageContext.getSpaceKey(), contentTitle , url.toString(), id, "An invalid snippet was found"));
             }
 
             StringWriter writer = new StringWriter();
 
             // check if each line starts with a whitespace and a "*" (JavaDocs)
             boolean javaDoc = true;
             if (!"true".equals(parameters.get("javadoc"))) {
                 // ok, we weren't told specifically, let's try to figure it out
                 for (String line : lines)
                 {
                     if (!Snippet.stripEOL(line).trim().startsWith("* "))
                     {
                         javaDoc = false;
                         break;
                     }
                 }
             }
 
             if (codeBlock)
             {
                 if(title == null) {
                     writer.write("{code:lang=" + lang + "}\n");
                 } else if(lang == null) {
                     writer.write("{code:title=" + title + "}\n");
                 } else {
                     writer.write("{code:lang=" + lang + "|title=" + title + "}\n");
                 }
             }
             snippet.writeContent(writer, withLineNumbers, javaDoc, startLine, endLine);
             if (codeBlock)
             {
                 writer.write("{code}\n");
             }
 
 
             String msg;
             if (codeBlock) {
                 String content = writer.getBuffer().toString();
                 if (javaDoc) {
                     // this is in a javadoc, so we can assume that XML content is escaped, we need to fix that
                     content = decodeJavadoc(content);
                 }
 
                 msg = subRenderer.render(content, new RenderContext());
             } else {
                 msg = subRenderer.render(writer.getBuffer().toString(), new RenderContext(),
                         RenderMode.suppress(RenderMode.F_MACROS | RenderMode.F_HTMLESCAPE | RenderMode.F_LINEBREAKS | RenderMode.F_LINKS));
 
                 if (javaDoc) {
                     // {@link Writer} -> Writer
                     // {@link Foo#bar()} -> Foo.bar()
                     // {@link #foo()} -> foo()
                     msg = snippetManager.cleanupJavadoc(msg);
                 }
             }
 
             if (snippetManager.shouldShowToggle()) {
                 String encoded = URLEncoder.encode(snippetUrl, "UTF-8");
                 StringBuffer sb = new StringBuffer();
                 sb.append("<table bgcolor='#ffffe0' cellpadding='0' cellspacing='0' width='100%' border='0'>")
                       .append("<tr>")
                           .append("<td>")
                               .append("<div style='font-size:8px; text-align:center;'>")
                                 .append("<center>Content pulled from external source. Click <a href='")
                                 .append(bootstrapManager.getBaseUrl())
                                 .append("/plugins/snippet/clearCache.action?id=" + id + "&amp;url=" + encoded + "'>here</a> to refresh.</center>")
                               .append("</div>")
                           .append("</td>")
                       .append("</tr>")
                       .append("<tr>")
                           .append("<td>")
                               .append(msg)
                           .append("</td>")
                       .append("</tr>")
                   .append("</table>");
                 return sb.toString();
             } else {
                 return msg;
             }
         } catch (Exception e) {
             if (!(e instanceof CachedIOException)){
               StringBuffer sb = new StringBuffer();
               sb.append(e.getMessage());
               StackTraceElement[] frames = e.getStackTrace();
               if (frames != null && frames.length > 0)
                   sb.append(" at ").append(frames[0].getClassName()).append(":").append(frames[0].getLineNumber());
   
               snippetErrorManager.add(new SnippetError(pageContext.getSpaceKey(), contentTitle , url.toString(), id, sb.toString()));
             }
            return "<div style='color:red; margin: 10px 0;'><strong>Code Snippet error:</strong> " + e.getMessage() + ".</div>";
         }
     }
 
     String decodeJavadoc(String content)
     {
         return content
                 .replaceAll("&lt;", "<")
                 .replaceAll("&gt;", ">")
                 .replaceAll("&quot;", "\"")
                 .replaceAll("&#64;", "@");
     }
 
     public RenderMode getBodyRenderMode()
     {
         return RenderMode.ALL;
     }
 
     public boolean hasBody()
     {
         return false;
     }
 
     public boolean isInline()
     {
         return false;
     }
 
     public void setSubRenderer(SubRenderer subRenderer)
     {
         this.subRenderer = subRenderer;
     }
 
     public void setSnippetManager(SnippetManager snippetManager)
     {
         this.snippetManager = snippetManager;
     }
 
     public void setSnippetErrorManager(SnippetErrorManager snippetErrorManager)
     {
         this.snippetErrorManager = snippetErrorManager;
     }
 
     public void setBootstrapManager(BootstrapManager bootstrapManager)
     {
         this.bootstrapManager = bootstrapManager;
     }
 }
