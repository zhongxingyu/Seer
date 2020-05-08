 /*
  * The MIT License
  * 
  * Copyright (c) 2004-2009, Andrew Bayer
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package hudson.plugins.hgca;
 
 import hudson.MarkupText;
 import hudson.MarkupText.SubText;
 import hudson.model.AbstractBuild;
 import hudson.scm.ChangeLogAnnotator;
 import hudson.scm.ChangeLogSet.Entry;
 
 import java.util.regex.Pattern;
 import java.util.HashMap;
 import java.util.ArrayList;
 /**
  * Given a set of pattern->URL pairs, replaces "pattern" in changelog text with 
  * "&lt;a href='url'&gt;pattern&lt;/a&gt;", replacing $1, $2, etc in "url" with matched 
  * groups in "pattern". Does this for each pair.
  *
  * @author Andrew Bayer
  */
 public class HGCALinkAnnotator extends ChangeLogAnnotator {
 
     @Override
    public void annotate(AbstractBuild<?,?> build, Entry change, MarkupText text) {
         HGCAProjectProperty hpp = build.getProject().getProperty(HGCAProjectProperty.class);
         
         if(hpp==null) 
             return; // not configured
 
         HashMap<String,String> annoPats = hpp.getAnnotations();
         if (annoPats.size() == 0) 
             return;
 
         annotate(annoPats, text);
         return;
     }
 
     void annotate(HashMap<String,String> annoPats, MarkupText text) {
         ArrayList<LinkMarkup> patternMarkups = createLinkMarkups(annoPats);
         
         for (LinkMarkup markup : patternMarkups) {
             markup.process(text);
         }
     }
 
     private ArrayList<LinkMarkup> createLinkMarkups(HashMap<String,String> annoPats) {
         ArrayList<LinkMarkup> lm = new ArrayList<LinkMarkup>();
 
         for (java.util.Map.Entry<String,String> entry : annoPats.entrySet()) {
             lm.add(new LinkMarkup(entry.getKey(), entry.getValue()));
         }
         
         return lm;
     }
 
     private static final class LinkMarkup {
         private final Pattern pattern;
         private final String href;
         
         LinkMarkup(String pattern, String href) {
             pattern = NUM_PATTERN.matcher(pattern).replaceAll("(\\\\d+)"); // \\\\d becomes \\d when in the expanded text.
             pattern = ANYWORD_PATTERN.matcher(pattern).replaceAll("((?:\\\\w|[._-])+)");
             this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
             this.href = href;
         }
 
         void process(MarkupText text) {
             // Currently doing surroundWith - debating switching to pure replace?
             for(SubText st : text.findTokens(pattern)) {
                 st.surroundWith(
                                 "<a href='"+href+"'>",
                                 "</a>");
             }
         }
 
         private static final Pattern NUM_PATTERN = Pattern.compile("NUM");
         private static final Pattern ANYWORD_PATTERN = Pattern.compile("ANYWORD");
     }
 
 }
