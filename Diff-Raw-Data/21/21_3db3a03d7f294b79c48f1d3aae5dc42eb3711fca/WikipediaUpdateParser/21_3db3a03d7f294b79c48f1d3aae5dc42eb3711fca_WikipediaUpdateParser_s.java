 package org.werelate.wikipedia;
 
 import org.apache.log4j.Logger;
 import org.werelate.parser.WikiPageParser;
 import org.werelate.utils.MultiMap;
 import org.werelate.utils.Util;
 import org.werelate.editor.PageEditor;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.io.IOException;
 
 import nu.xom.ParsingException;
 
 public class WikipediaUpdateParser implements WikiPageParser {
    private static final String COMMENT = "<!-- This text is copied from wikipedia. Any changes made will be " +
            "overwritten during the next update. -->";
 
    private static Logger logger = Logger.getLogger("org.werelate.wikipedia");
 
    private MultiMap<String, String> wp2t;
    private MultiMap<String, String> t2wr;
    private Map<String, String> wp2wr; // moreinfo wikipedia template references
    private Map<String, String> alt2wp;
    private Set<String> sourceTargets;
    private Set<String> foundWpTitles;
    private boolean updateAllTemplates;
    // Whether we're allowed to post to the web server
    private boolean writeEnabled = false;
    private int updateCount;
    private PageEditor editor;
    private String startTitle;
 
    public WikipediaUpdateParser(MultiMap<String, String> wp2t,
                                 MultiMap<String, String> t2wr,
                                 Map<String, String> wp2wr,
                                 Map<String, String> alt2wp,
                                 Set<String> sourceTargets,
                                 String host, String agentPassword,
                                 boolean updateAllTemplates) {
       this.wp2t = wp2t;
       this.t2wr = t2wr;
       this.wp2wr = wp2wr;
       this.alt2wp = alt2wp;
       this.sourceTargets = sourceTargets;
       this.editor = new PageEditor(host, agentPassword);
       this.updateAllTemplates = updateAllTemplates;
       this.updateCount = 0;
       this.foundWpTitles = new HashSet<String>();
       this.startTitle = null;
    }
 
    public void setWriteEnabled() {
       writeEnabled = true;
    }
 
    public int getUpdateCount() {
       return updateCount;
    }
 
    public void setStartTitle(String startTitle) {
       logger.info("Setting start title="+startTitle);
       this.startTitle = startTitle;
    }
 
    public Set<String> getFoundWpTitles() {
       return foundWpTitles;
    }
 
    // Used for when we have a link such as this in the wikipedia text:
    // [[User:NathanPowell (the second)|]]
    // This will be displayed in the text as: NathanPowell
    // Because the display logic removes the namespace and the
    // parentheses.
    private static final Pattern pWikipediaHrefRemove = Pattern.compile(
            "(Media|Special|Talk|User|User_talk|Wikipedia|Wikipedia_talk|:?Image|Image_talk|MediaWiki|MediaWiki_talk|Template|Template_talk|Help|Help_talk|Category|Category_talk|Portal|Portal_talk):|\\s*\\([^\\)(]*\\)"
    );
 
    // This function is very similar to one I found
    // in wikiRuleRunner but it has been adjusted for
    // use in this update parser.
    // Basically it just takes the wptext and
    // changes the links to point to a WRPage
    // if it can, or changes it to a Wikipedia:
    // link otherwise.
    private String updateLinks(String text) {
       if (text == null) return null;
       else {
          StringBuffer buf = new StringBuffer();
          Matcher m = WikiPage.LINK_TEXT_PATTERN.matcher(text);
          while (m.find()) {
             String linkTitle = m.group(1);
             String stdTitle = WikiPage.standardizeTitle(linkTitle);
             String newTitle = null;
             String linkText = m.group(2);
             // get the redirect target
             while (alt2wp.containsKey(stdTitle)) {
                stdTitle = alt2wp.get(stdTitle);
             }
             // do we have a template for the wp page?
             if (wp2t.containsKey(stdTitle)) {
                String template = wp2t.get(stdTitle).iterator().next();
                if (t2wr.containsKey(template)) {
                   newTitle = t2wr.get(template).iterator().next();
                }
                else {
 //                  logger.warn("Template not referenced in any WR page: "+template);
                }
             }
             // do we have a moreinfo template for the wp page?
             if (newTitle == null && wp2wr.containsKey(stdTitle)) {
                newTitle = wp2wr.get(stdTitle);
             }
             // fall back to linking to the wikipedia article
             if (newTitle == null) {
                newTitle = "Wikipedia:" + stdTitle;
             }
             if (linkText.length() == 0) {
                if (m.group(0).contains("|")) {
                   linkText = getSpecialLinkText(linkTitle);
                } else {
                   linkText = linkTitle;
                }
             }
             if (newTitle.startsWith("Category:") || newTitle.startsWith("category:") ||
                 newTitle.startsWith("Image:") || newTitle.startsWith("image:")) newTitle = ":"+newTitle;
             m.appendReplacement(buf, "[[" + Util.protectDollarSlash(newTitle) + "|" +
                     Util.protectDollarSlash(linkText) + "]]");
          }
          m.appendTail(buf);
          return buf.toString();
       }
    }
 
    private String getSpecialLinkText(String pageTitle) {
       String linkText;
       StringBuffer removeBuf = new StringBuffer();
       Matcher mRemove = pWikipediaHrefRemove.matcher(pageTitle);
       while (mRemove.find()) {
          mRemove.appendReplacement(removeBuf, "");
       }
       mRemove.appendTail(removeBuf);
       linkText = removeBuf.toString();
       return linkText;
    }
 
 
    /**
     * This method updates all WeRelate articles that happen to have
     * copy-wikipedia templates that point to the wikipedia article
     * specified by "title"
     *
     * @param title Title of the Wikipedia article from which we can get
     *              updated content
     * @param text  Text of the Wikipedia article. We update the WeRelate
     *              articles with this text.
     * @throws java.io.IOException
     * @throws nu.xom.ParsingException
     */
    public void parse(String title, String text, int pageId, int latestRevId, String username, String timestamp, String revComment) throws IOException, ParsingException {
       title = title.trim();
 
       if (sourceTargets.contains(title) || (updateAllTemplates && wp2t.containsKey(title))) {
          foundWpTitles.add(title);
          if (startTitle == null || startTitle.equals(title)) {
             if (startTitle != null) {
                System.out.println("Found "+startTitle);
                startTitle = null;
             }
             Set<String> s = wp2t.get(title);
             for (String wrTitle : s) {
                if (writeEnabled) {
                   editor.doGet("Template:"+wrTitle, true);
                   String wrText = editor.readVariable(PageEditor.TEXTBOX1_PATTERN, true).trim();
                   Matcher mCopyWikipedia = WP2WRTitleParser.pCopyWikipedia.matcher(wrText);
                   String copySection = null;
                   if (mCopyWikipedia.find()) {
                      copySection = mCopyWikipedia.group(3);
                   }
                   else if (wrText.length() > 0) {
                      logger.warn("Updating a template page without a copy-wikipedia template: Template:"+wrTitle);
                   }
                   String wpText;
                   WikiPage wp = new WikiPage(title, WikiPage.cleanText(new StringBuffer(text)));
                   String copyWikipediaTag = "{{copy-wikipedia|" + title;
                   if (copySection == null) {
                      wpText = wp.getOpeningSection();
                   } else {
                      wpText = wp.getSection(copySection);
                      copyWikipediaTag += '#' + copySection;
                   }
                   if (wpText == null) {
                      wpText = "";
                   }
                   copyWikipediaTag += "}}";
                   wpText = copyWikipediaTag + '\n' + COMMENT + '\n' + updateLinks(wpText).trim();
 
                   if (!wpText.trim().equals(wrText.trim())) {
                      editor.setPostVariable("wpTextbox1", wpText);
                      editor.setPostVariable("wpSummary", "Updated from Wikipedia");
                      editor.doPost();
                   }
                  Util.sleep(2500);
                   updateCount++;
                   logger.info("update: "+wrTitle+"  ->  "+title);
                   if (updateCount % 1000 == 0) System.out.print(".");
                }
                else {
                   updateCount++;
                   logger.info("update: "+wrTitle+"  ->  "+title);
                }
             }
          }
       }
    }
 }
