 package com.googlecode.htmlcompressor.compressor;
 
 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.yahoo.platform.yui.compressor.CssCompressor;
 import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
 
 /**
  * Class that compresses given HTML source by removing comments, extra spaces and 
  * line breaks while preserving content within &lt;pre>, &lt;textarea>, &lt;script> 
  * and &lt;style> tags. Can optionally compress content inside &lt;script> 
  * or &lt;style> tags using 
  * <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a> 
  * library.
  * 
  * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
  */
 public class HtmlCompressor implements Compressor {
     
     private boolean enabled = true;
     
     //default settings
     private boolean removeComments = true;
     private boolean removeJspComments = true;
     private boolean removeMultiSpaces = true;
     private boolean skipCommentsWithStrutsForm = false;
     
     //optional settings
     private boolean removeIntertagSpaces = false;
     private boolean removeQuotes = false;
     private boolean compressJavaScript = false;
     private boolean compressCss = false;
     private boolean debugMode = false;
     
     //YUICompressor settings
     private boolean yuiJsNoMunge = false;
     private boolean yuiJsPreserveAllSemiColons = false;
     private boolean yuiJsDisableOptimizations = false;
     private int yuiJsLineBreak = -1;
     private int yuiCssLineBreak = -1;
     
     //temp replacements for preserved blocks 
     private static final String tempPreBlock = "%%%COMPRESS~PRE~#%%%";
     private static final String tempTextAreaBlock = "%%%COMPRESS~TEXTAREA~#%%%";
     private static final String tempScriptBlock = "%%%COMPRESS~SCRIPT~#%%%";
     private static final String tempStyleBlock = "%%%COMPRESS~STYLE~#%%%";
     private static final String tempJSPBlock = "%%%COMPRESS~JSP~#%%%";
     private static final String tempJSPAssignBlock = "%%%COMPRESS~JSPASSIGN~#%%%";
     
     //compiled regex patterns
     // The commentStrutsFormHack pattern purposely excludes any comment with <html:form> in it due to a work around
     // for a struts 1.0 bug that we use. 
     private static final Pattern commentMarkersInScript = Pattern.compile("(<!--)|(\\/\\/-->)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern commentStrutsFormHackPattern = Pattern.compile("<!--[^\\[](?!.*?html:form.*?).*?-->", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern commentPattern = Pattern.compile("<!--[^\\[].*?-->", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern jspCommentPattern = Pattern.compile("<%--.+?--%>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern intertagPattern = Pattern.compile(">\\s+?<", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern multispacePattern = Pattern.compile("\\s{2,}", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern prePattern = Pattern.compile("<pre[^>]*?>.*?</pre>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern taPattern = Pattern.compile("<textarea[^>]*?>.*?</textarea>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern tagquotePattern = Pattern.compile("\\s*=\\s*([\"'])([a-z0-9-_]+?)\\1(?=[^<]*?>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern scriptPattern = Pattern.compile("<script[^>]*?>.*?</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern stylePattern = Pattern.compile("<style[^>]*?>.*?</style>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern scriptPatternNonEmpty = Pattern.compile("<script[^>]*?>(.+?)</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern stylePatternNonEmpty = Pattern.compile("<style[^>]*?>(.+?)</style>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     
     // JSP and js block patterns used to strip leading and trailing space, as well as empty lines.
     private static final Pattern jspAssignPattern = Pattern.compile("<%=.*?%>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern jspPattern = Pattern.compile("<%[^-=@].*?%>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern jsLeadingSpacePattern = Pattern.compile("^[ \\t]+", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
     private static final Pattern jsTrailingSpacePattern = Pattern.compile("[ \\t]+$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
     private static final Pattern jsEmptyLinePattern = Pattern.compile("^$\\n", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
     
     private static final Pattern tempPrePattern = Pattern.compile("%%%COMPRESS~PRE~(\\d+?)%%%", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern tempTextAreaPattern = Pattern.compile("%%%COMPRESS~TEXTAREA~(\\d+?)%%%", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern tempScriptPattern = Pattern.compile("%%%COMPRESS~SCRIPT~(\\d+?)%%%", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern tempStylePattern = Pattern.compile("%%%COMPRESS~STYLE~(\\d+?)%%%", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern tempJSPPattern = Pattern.compile("%%%COMPRESS~JSP~(\\d+?)%%%", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
     private static final Pattern tempJSPAssignPattern = Pattern.compile("%%%COMPRESS~JSPASSIGN~(\\d+?)%%%", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
 
     
     /**
      * The main method that compresses given HTML source and returns compressed result.
      * 
      * @param html HTML content to compress
      * @return compressed content.
      * @throws Exception
      */
     public String compress(String html) throws Exception {
         if(!enabled || html == null || html.length() == 0) {
             return html;
         }
         
         //preserved block containers
         List<String> preBlocks = new ArrayList<String>();
         List<String> taBlocks = new ArrayList<String>();
         List<String> scriptBlocks = new ArrayList<String>();
         List<String> styleBlocks = new ArrayList<String>();
         List<String> jspBlocks = new ArrayList<String>();
         List<String> jspAssignBlocks = new ArrayList<String>();
         //preserve blocks
         html = preserveBlocks(html, preBlocks, taBlocks, scriptBlocks, styleBlocks, jspBlocks, jspAssignBlocks);
         
         //process pure html
         html = processHtml(html);
 
         //process preserved blocks
         processScriptBlocks(scriptBlocks);
         processStyleBlocks(styleBlocks);
         
         //put blocks back
         html = returnBlocks(html, preBlocks, taBlocks, scriptBlocks, styleBlocks, jspBlocks, jspAssignBlocks);
         
         return html.trim();
     }
 
     
     private String preserveBlocks(String html, List<String> preBlocks, List<String> taBlocks, List<String> scriptBlocks, List<String> styleBlocks, List<String>jspBlocks, List<String>jspAssignBlocks) {
         Matcher matcher = null;
         int index;
         StringBuffer sb = null;
 
         //preserve JSP variable reference tags
         matcher = jspAssignPattern.matcher(html);
         index = 0;
         sb = new StringBuffer();
         while(matcher.find()) {
             jspAssignBlocks.add(matcher.group(0));
             matcher.appendReplacement(sb, tempJSPAssignBlock.replaceFirst("#", Integer.toString(index++)));
         }
         
         matcher.appendTail(sb);
         html = sb.toString();
         
         //preserve JSP tags
         matcher = jspPattern.matcher(html);
         index = 0;
         sb = new StringBuffer();
         while(matcher.find()) {
             jspBlocks.add(matcher.group(0));
             matcher.appendReplacement(sb, tempJSPBlock.replaceFirst("#", Integer.toString(index++)));
         }
         
         matcher.appendTail(sb);
         html = sb.toString();
         
         //preserve PRE tags
         matcher = prePattern.matcher(html);
         
         index = 0;
         sb = new StringBuffer();
         while(matcher.find()) {
             preBlocks.add(matcher.group(0));
             matcher.appendReplacement(sb, tempPreBlock.replaceFirst("#", Integer.toString(index++)));
         }
         matcher.appendTail(sb);
         html = sb.toString();
         
         //preserve SCRIPT tags
         matcher = scriptPattern.matcher(html);
         index = 0;
         sb = new StringBuffer();
         while(matcher.find()) {
             scriptBlocks.add(matcher.group(0));
             matcher.appendReplacement(sb, tempScriptBlock.replaceFirst("#", Integer.toString(index++)));
         }
         
         matcher.appendTail(sb);
         html = sb.toString();
 
         //preserve STYLE tags
         matcher = stylePattern.matcher(html);
         index = 0;
         sb = new StringBuffer();
         while(matcher.find()) {
             styleBlocks.add(matcher.group(0));
             matcher.appendReplacement(sb, tempStyleBlock.replaceFirst("#", Integer.toString(index++)));
         }
         matcher.appendTail(sb);
         html = sb.toString();
         
         //preserve TEXTAREA tags
         matcher = taPattern.matcher(html);
         index = 0;
         sb = new StringBuffer();
         while(matcher.find()) {
             taBlocks.add(matcher.group(0));
             matcher.appendReplacement(sb, tempTextAreaBlock.replaceFirst("#", Integer.toString(index++)));
         }
         matcher.appendTail(sb);
         html = sb.toString();
 
         return html;
     }
     
     private String returnBlocks(String html, List<String> preBlocks, List<String> taBlocks, List<String> scriptBlocks, List<String> styleBlocks, List<String> jspBlocks, List<String> jspAssignBlocks) {
         //put TEXTAREA blocks back
         Matcher matcher = tempTextAreaPattern.matcher(html);
         StringBuffer sb = new StringBuffer();
         while(matcher.find()) {
             matcher.appendReplacement(sb, Matcher.quoteReplacement(taBlocks.get(Integer.parseInt(matcher.group(1)))));
         }
         matcher.appendTail(sb);
         html = sb.toString();
         
         //put STYLE blocks back
         matcher = tempStylePattern.matcher(html);
         sb = new StringBuffer();
         while(matcher.find()) {
             matcher.appendReplacement(sb, Matcher.quoteReplacement(styleBlocks.get(Integer.parseInt(matcher.group(1)))));
         }
         matcher.appendTail(sb);
         html = sb.toString();
         
         //put SCRIPT blocks back
         matcher = tempScriptPattern.matcher(html);
         sb = new StringBuffer();
         while(matcher.find()) {
             matcher.appendReplacement(sb, Matcher.quoteReplacement(scriptBlocks.get(Integer.parseInt(matcher.group(1)))));
         }
         matcher.appendTail(sb);
         html = sb.toString();
 
         //put PRE blocks back
         matcher = tempPrePattern.matcher(html);
         sb = new StringBuffer();
         while(matcher.find()) {
             matcher.appendReplacement(sb, Matcher.quoteReplacement(preBlocks.get(Integer.parseInt(matcher.group(1)))));
         }
         matcher.appendTail(sb);
         html = sb.toString();
         
         //put JSP blocks back
         matcher = tempJSPPattern.matcher(html);
         sb = new StringBuffer();
         while(matcher.find()) {
             matcher.appendReplacement(sb, Matcher.quoteReplacement(jspBlocks.get(Integer.parseInt(matcher.group(1)))));
         }
         matcher.appendTail(sb);
         html = sb.toString();
         
         //put variable reference blocks back
         matcher = tempJSPAssignPattern.matcher(html);
         sb = new StringBuffer();
         while(matcher.find()) {
             matcher.appendReplacement(sb, Matcher.quoteReplacement(jspAssignBlocks.get(Integer.parseInt(matcher.group(1)))));
         }
         matcher.appendTail(sb);
         html = sb.toString();
         return html;
     }
     
     private String processHtml(String html) throws Exception {
         // remove comments and JSP comments, if specified.
         if(this.removeComments) {
             if (skipCommentsWithStrutsForm) {
                 html = commentStrutsFormHackPattern.matcher(html).replaceAll("");
             } else {
                 html = commentPattern.matcher(html).replaceAll("");
             }
         }
         
         if (this.removeJspComments) {
             html = jspCommentPattern.matcher(html).replaceAll("");
         }
         
         //remove inter-tag spaces
         if(removeIntertagSpaces) {
             html = intertagPattern.matcher(html).replaceAll("><");
         }
         
         //remove multi whitespace characters
         if(removeMultiSpaces) {
             html = multispacePattern.matcher(html).replaceAll(" ");
         }
         
         //remove quotes from tag attributes
         if(removeQuotes) {
             html = tagquotePattern.matcher(html).replaceAll("=$2");
         }
         
         return html;
     }
     
     private void processScriptBlocks(List<String> scriptBlocks) throws Exception {
 
         for(int i = 0; i < scriptBlocks.size(); i++) {
             String scriptBlock = scriptBlocks.get(i);
             
             // Remove any JSP comments that might be in the javascript for security reasons
             // (developer only comments, etc)
             scriptBlock = jspCommentPattern.matcher(scriptBlock).replaceAll("");
 
             if (!compressJavaScript) {
                 scriptBlock = jsLeadingSpacePattern.matcher(scriptBlock).replaceAll("");
                 scriptBlock = jsTrailingSpacePattern.matcher(scriptBlock).replaceAll("");
                 scriptBlock = jsEmptyLinePattern.matcher(scriptBlock).replaceAll("");
             } else {
                 scriptBlock = compressJavaScript(scriptBlock);
             }
             
             scriptBlocks.set(i, scriptBlock);
         }
     }
     
     private void processStyleBlocks(List<String> styleBlocks) throws Exception {
         if(compressCss) {
             for(int i = 0; i < styleBlocks.size(); i++) {
                 styleBlocks.set(i, compressCssStyles(styleBlocks.get(i)));
             }
         }
     }
     
     private String compressJavaScript(String source) throws Exception {
         StringWriter result = new StringWriter();
         String originalSource = new String(source);
 
         source = commentMarkersInScript.matcher(source).replaceAll("");                
         //check if block is not empty
         Matcher scriptMatcher = scriptPatternNonEmpty.matcher(source);
  
         if(scriptMatcher.find()) {
             
             //call YUICompressor
             try {
                 if (debugMode) {
                     System.out.println("\n---- COMPRESSING ---- \n" + source + "\n------\n");
                 }
                 
                 JavaScriptCompressor compressor = new JavaScriptCompressor(new StringReader(scriptMatcher.group(1)), null);
                 compressor.compress(result, yuiJsLineBreak, !yuiJsNoMunge, false, yuiJsPreserveAllSemiColons, yuiJsDisableOptimizations);
             } catch (Exception e) {
                 if (debugMode) {
                     System.out.println(e.getMessage() + "\nIn Javascript block:\n" + scriptMatcher.group(1));
                     System.out.println("Returning " + source);
                     throw new Exception(e.getMessage() + "\nIn Javascript block:\n" + scriptMatcher.group(1));
                 }
           
                 return(originalSource);
             }
 
             if (debugMode) {              
                 System.out.println(source.substring(0, scriptMatcher.start(1)) + result.toString() + source.substring(scriptMatcher.end(1)));  
             }      
             return (new StringBuilder(source.substring(0, scriptMatcher.start(1))).append(result.toString()).append(source.substring(scriptMatcher.end(1)))).toString();
         
         } else {
             return source;
         }
     }
     
     private String compressCssStyles(String source) throws Exception {
         
         //check if block is not empty
         Matcher styleMatcher = stylePatternNonEmpty.matcher(source);
         if(styleMatcher.find()) {
             
             //call YUICompressor
             StringWriter result = new StringWriter();
             CssCompressor compressor = new CssCompressor(new StringReader(styleMatcher.group(1)));
             compressor.compress(result, yuiCssLineBreak);
             
             return (new StringBuilder(source.substring(0, styleMatcher.start(1))).append(result.toString()).append(source.substring(styleMatcher.end(1)))).toString();
         
         } else {
             return source;
         }
     }
     
     /**
      * Returns <code>true</code> if JavaScript compression is enabled.
      * 
      * @return current state of JavaScript compression.
      */
     public boolean isCompressJavaScript() {
         return compressJavaScript;
     }
 
     /**
      * Enables JavaScript compression within &lt;script> tags using 
      * <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a> 
      * if set to <code>true</code>. Default is <code>false</code> for performance reasons.
      *  
      * <p><b>Note:</b> Compressing JavaScript is not recommended if pages are 
      * compressed dynamically on-the-fly because of performance impact. 
      * You should consider putting JavaScript into a separate file and
      * compressing it using standalone YUICompressor for example.</p>
      * 
      * @param compressJavaScript set <code>true</code> to enable JavaScript compression. 
      * Default is <code>false</code>
      * 
      * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
      * 
      */
     public void setCompressJavaScript(boolean compressJavaScript) {
         this.compressJavaScript = compressJavaScript;
     }
 
     /**
      * Returns <code>true</code> if CSS compression is enabled.
      * 
      * @return current state of CSS compression.
      */
     public boolean isCompressCss() {
         return compressCss;
     }
 
     /**
      * Enables CSS compression within &lt;style> tags using 
      * <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a> 
      * if set to <code>true</code>. Default is <code>false</code> for performance reasons.
      *  
      * <p><b>Note:</b> Compressing CSS is not recommended if pages are 
      * compressed dynamically on-the-fly because of performance impact. 
      * You should consider putting CSS into a separate file and
      * compressing it using standalone YUICompressor for example.</p>
      * 
      * @param compressCss set <code>true</code> to enable CSS compression. 
      * Default is <code>false</code>
      * 
      * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
      * 
      */
     public void setCompressCss(boolean compressCss) {
         this.compressCss = compressCss;
     }
 
     /**
      * Returns <code>true</code> if Yahoo YUI Compressor
      * will only minify javascript without obfuscating local symbols. 
      * This corresponds to <code>--nomunge</code> command line option.  
      *   
      * @return <code>nomunge</code> parameter value used for JavaScript compression.
      * 
      * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
      */
     public boolean isYuiJsNoMunge() {
         return yuiJsNoMunge;
     }
 
     /**
      * Tells Yahoo YUI Compressor to only minify javascript without obfuscating 
      * local symbols. This corresponds to <code>--nomunge</code> command line option. 
      * This option has effect only if JavaScript compression is enabled. 
      * Default is <code>false</code>.
      * 
      * @param yuiJsNoMunge set <code>true<code> to enable <code>nomunge</code> mode
      * 
      * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
      */
     public void setYuiJsNoMunge(boolean yuiJsNoMunge) {
         this.yuiJsNoMunge = yuiJsNoMunge;
     }
 
     /**
      * Returns <code>true</code> if Yahoo YUI Compressor
      * will preserve unnecessary semicolons during JavaScript compression. 
      * This corresponds to <code>--preserve-semi</code> command line option.
      *   
      * @return <code>preserve-semi</code> parameter value used for JavaScript compression.
      * 
      * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
      */
     public boolean isYuiJsPreserveAllSemiColons() {
         return yuiJsPreserveAllSemiColons;
     }
 
     /**
      * Tells Yahoo YUI Compressor to preserve unnecessary semicolons 
      * during JavaScript compression. This corresponds to 
      * <code>--preserve-semi</code> command line option. 
      * This option has effect only if JavaScript compression is enabled.
      * Default is <code>false</code>.
      * 
      * @param yuiJsPreserveAllSemiColons set <code>true<code> to enable <code>preserve-semi</code> mode
      * 
      * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
      */
     public void setYuiJsPreserveAllSemiColons(boolean yuiJsPreserveAllSemiColons) {
         this.yuiJsPreserveAllSemiColons = yuiJsPreserveAllSemiColons;
     }
 
     /**
      * Returns <code>true</code> if Yahoo YUI Compressor
      * will disable all the built-in micro optimizations during JavaScript compression. 
      * This corresponds to <code>--disable-optimizations</code> command line option.
      *   
      * @return <code>disable-optimizations</code> parameter value used for JavaScript compression.
      * 
      * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
      */
     public boolean isYuiJsDisableOptimizations() {
         return yuiJsDisableOptimizations;
     }
     
     /**
      * Tells Yahoo YUI Compressor to disable all the built-in micro optimizations
      * during JavaScript compression. This corresponds to 
      * <code>--disable-optimizations</code> command line option. 
      * This option has effect only if JavaScript compression is enabled.
      * Default is <code>false</code>.
      * 
      * @param yuiJsDisableOptimizations set <code>true<code> to enable 
      * <code>disable-optimizations</code> mode
      * 
      * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
      */
     public void setYuiJsDisableOptimizations(boolean yuiJsDisableOptimizations) {
         this.yuiJsDisableOptimizations = yuiJsDisableOptimizations;
     }
     
     /**
      * Returns number of symbols per line Yahoo YUI Compressor
      * will use during JavaScript compression. 
      * This corresponds to <code>--line-break</code> command line option.
      *   
      * @return <code>line-break</code> parameter value used for JavaScript compression.
      * 
      * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
      */
     public int getYuiJsLineBreak() {
         return yuiJsLineBreak;
     }
 
     /**
      * Tells Yahoo YUI Compressor to break lines after the specified number of symbols 
      * during JavaScript compression. This corresponds to 
      * <code>--line-break</code> command line option. 
      * This option has effect only if JavaScript compression is enabled.
      * Default is <code>-1</code> to disable line breaks.
      * 
      * @param yuiJsLineBreak set number of symbols per line
      * 
      * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
      */
     public void setYuiJsLineBreak(int yuiJsLineBreak) {
         this.yuiJsLineBreak = yuiJsLineBreak;
     }
     
     /**
      * Returns number of symbols per line Yahoo YUI Compressor
      * will use during CSS compression. 
      * This corresponds to <code>--line-break</code> command line option.
      *   
      * @return <code>line-break</code> parameter value used for CSS compression.
      * 
      * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
      */
     public int getYuiCssLineBreak() {
         return yuiCssLineBreak;
     }
     
     /**
      * Tells Yahoo YUI Compressor to break lines after the specified number of symbols 
      * during CSS compression. This corresponds to 
      * <code>--line-break</code> command line option. 
      * This option has effect only if CSS compression is enabled.
      * Default is <code>-1</code> to disable line breaks.
      * 
      * @param yuiCssLineBreak set number of symbols per line
      * 
      * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
      */
     public void setYuiCssLineBreak(int yuiCssLineBreak) {
         this.yuiCssLineBreak = yuiCssLineBreak;
     }
 
     /**
      * Returns <code>true</code> if all unnecessary quotes will be removed 
      * from tag attributes. 
      *   
      */
     public boolean isRemoveQuotes() {
         return removeQuotes;
     }
 
     /**
      * If set to <code>true</code> all unnecessary quotes will be removed  
      * from tag attributes. Default is <code>false</code>.
      * 
      * <p><b>Note:</b> Even though quotes are removed only when it is safe to do so, 
      * it still might break strict HTML validation. Turn this option on only if 
      * a page validation is not very important or to squeeze the most out of the compression.
      * This option has no performance impact. 
      * 
      * @param removeQuotes set <code>true</code> to remove unnecessary quotes from tag attributes
      */
     public void setRemoveQuotes(boolean removeQuotes) {
         this.removeQuotes = removeQuotes;
     }
 
     /**
      * Returns <code>true</code> if compression is enabled.  
      * 
      * @return <code>true</code> if compression is enabled.
      */
     public boolean isEnabled() {
         return enabled;
     }
 
     /**
      * If set to <code>false</code> all compression will be bypassed. Might be useful for testing purposes. 
      * Default is <code>true</code>.
      * 
      * @param enabled set <code>false</code> to bypass all compression
      */
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     /**
      * Returns <code>true</code> if all HTML comments will be removed.
      * 
      * @return <code>true</code> if all HTML comments will be removed
      */
     public boolean isRemoveComments() {
         return removeComments;
     }
 
     /**
      * If set to <code>true</code> all HTML comments will be removed.   
      * Default is <code>true</code>.
      * 
      * @param removeComments set <code>true</code> to remove all HTML comments
      */
     public void setRemoveComments(boolean removeComments) {
         this.removeComments = removeComments;
     }
 
     /**
      * Returns <code>true</code> if all HTML comments will be removed.
      * 
      * @return <code>true</code> if all HTML comments will be removed
      */
     public boolean isRemoveJspComments() {
         return removeJspComments;
     }
 
     /**
      * If set to <code>true</code> all HTML comments will be removed.   
      * Default is <code>true</code>.
      * 
      * @param removeComments set <code>true</code> to remove all HTML comments
      */
     public void setRemoveJspComments(boolean removeComments) {
         this.removeJspComments = removeComments;
     }
 
     /**
      * Returns <code>true</code> if all multiple whitespace characters will be replaced with single spaces.
      * 
      * @return <code>true</code> if all multiple whitespace characters will be replaced with single spaces.
      */
     public boolean isRemoveMultiSpaces() {
         return removeMultiSpaces;
     }
 
     /**
      * If set to <code>true</code> all multiple whitespace characters will be replaced with single spaces.
      * Default is <code>true</code>.
      * 
      * @param removeMultiSpaces set <code>true</code> to replace all multiple whitespace characters 
      * will single spaces.
      */
     public void setRemoveMultiSpaces(boolean removeMultiSpaces) {
         this.removeMultiSpaces = removeMultiSpaces;
     }
 
     /**
      * Returns <code>true</code> if all inter-tag whitespace characters will be removed.
      * 
      * @return <code>true</code> if all inter-tag whitespace characters will be removed.
      */
     public boolean isRemoveIntertagSpaces() {
         return removeIntertagSpaces;
     }
 
     /**
      * If set to <code>true</code> all inter-tag whitespace characters will be removed.
      * Default is <code>false</code>.
      * 
      * <p><b>Note:</b> It is fairly safe to turn this option on unless you 
      * rely on spaces for page formatting. Even if you do, you can always preserve 
      * required spaces with <code>&amp;nbsp;</code>. This option has no performance impact.    
      * 
      * @param removeIntertagSpaces set <code>true</code> to remove all inter-tag whitespace characters
      */
     public void setRemoveIntertagSpaces(boolean removeIntertagSpaces) {
         this.removeIntertagSpaces = removeIntertagSpaces;
     }
 
     /**
      * If set to <code>true</code> comments with the <html:form> opening and closing tags will be skipped
      * during comment removal.  This is a workaround for a Struts 1.0 bug, in which there were issues
      * with this tag that caused other form fields to work improperly.   The workaround was to put the
      * <html:tag> opening and closing tags in comments which caused the proper structures to (for some reason)
      * still be initialized properly, but did not use the tag, and  then to use standard for tags around your
      * struts controls.   Kind of odd, but thats how it works.
      *
      * @return Nothing
      */    
     public void setSkipStrutsFormComments(boolean leaveComments) {
        skipCommentsWithStrutsForm = leaveComments;
     }
     
     public void setDebugMode(boolean debugMode) {
         this.debugMode = debugMode;
     }
     
 }
