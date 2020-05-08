 package com.fauxwerd.util;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.jsoup.nodes.Attribute;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Entities;
 import org.jsoup.parser.Tag;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fauxwerd.util.parse.Link;
 import com.fauxwerd.util.parse.ScoredElement;
 
 /*
  * Number of html parsing utility methods based on readability js
  */
 
 public class ParseUtil {
 	
 	private static Logger log = LoggerFactory.getLogger(ParseUtil.class);
 			
 	private static int FLAG_STRIP_UNLIKELYS = 1;
 	private static int FLAG_WEIGHT_CLASSES = 2;
 	private static int FLAG_CLEAN_CONDITIONALLY = 4;
 	
 	// Start with all flags set. 
 	private static int[] FLAGS = {FLAG_STRIP_UNLIKELYS, FLAG_WEIGHT_CLASSES, FLAG_CLEAN_CONDITIONALLY };
 	
 	// Not sure if this is a good idea
 	private static int currentPageNum = 1;
 	
 	/**
      * All of the regular expressions in use within readability.
      * Defined up here so we don't instantiate them repeatedly in loops.
      **/
 	@SuppressWarnings("serial")
 	public static final Map<String, Pattern> REGEX =
 		new HashMap<String, Pattern>()
 		{
 			{
 				put("unlikelyCandidates", Pattern.compile("combx|comment|community|disqus|extra|foot|header|menu|remark|rss|shoutbox|sidebar|sponsor|ad-break|agegate|pagination|pager|popup|tweet|twitter", Pattern.CASE_INSENSITIVE)); //case-insensitive
 				put("okMaybeItsACandidate", Pattern.compile("and|article|body|column|main|shadow", Pattern.CASE_INSENSITIVE)); //case-insensitive
 				put("positive", Pattern.compile("article|body|content|entry|hentry|main|page|pagination|post|text|blog|story", Pattern.CASE_INSENSITIVE)); //case-insensitive
 				put("negative", Pattern.compile("combx|comment|com-|contact|foot|footer|footnote|masthead|media|meta|outbrain|promo|related|scroll|shoutbox|sidebar|sponsor|shopping|tags|tool|widget", Pattern.CASE_INSENSITIVE)); //case-insensitive
 				put("extraneous", Pattern.compile("print|archive|comment|discuss|e[\\-]?mail|share|reply|all|login|sign|single", Pattern.CASE_INSENSITIVE)); //case-insensitive
 				put("divToPElements", Pattern.compile("<(a|blockquote|dl|div|img|ol|p|pre|table|ul)", Pattern.CASE_INSENSITIVE)); //case-insensitive
 				put("replaceBrs", Pattern.compile("(<br[^>]*>[ \n\r\t]*){2,}", Pattern.CASE_INSENSITIVE)); //global replace & case insensitive
 				put("replaceFonts", Pattern.compile("<(\\/?)font[^>]*>", Pattern.CASE_INSENSITIVE)); //global replace & case insensitive
 				put("trim", Pattern.compile("^\\s+|\\s+$")); //global replace
 				put("normalize", Pattern.compile("\\s{2,}")); //global replace
 				put("killBreaks", Pattern.compile("(<br\\s*\\/?>(\\s|&nbsp;?)*){1,}")); //global replace
 				put("videos", Pattern.compile("http:\\/\\/(www\\.)?(youtube|vimeo)\\.com", Pattern.CASE_INSENSITIVE)); //case-insensitive
 				put("skipFootnoteLink", Pattern.compile("^\\s*(\\[?[a-z0-9]{1,2}\\]?|^|edit|citation needed)\\s*$", Pattern.CASE_INSENSITIVE)); //case-insensitive
 				put("nextLink", Pattern.compile("(next|weiter|continue|>([^\\|]|$)|»([^\\|]|$))", Pattern.CASE_INSENSITIVE)); //case-insensitive
 				put("prevLink", Pattern.compile("(prev|earl|old|new|<|«)", Pattern.CASE_INSENSITIVE)); //case-insensitive
 			}
 		};	
 	
     /**
      * Initialize a node with the readability object. Also checks the
      * className/id for special names to add to its score.
      *
      * @param Element
      * @return void
     **/
     public static void initializeNode(ScoredElement scoredElement) {
     	scoredElement.setScore(0);
     	
     	if (scoredElement.tagName().equalsIgnoreCase("DIV")) scoredElement.incrementScore(5);
     	if (scoredElement.tagName().equalsIgnoreCase("BLOCKQUOTE")) scoredElement.incrementScore(3);
     	if (scoredElement.tagName().equalsIgnoreCase("FORM")) scoredElement.incrementScore(-3);
     	if (scoredElement.tagName().equalsIgnoreCase("TH")) scoredElement.incrementScore(-5);
        
         scoredElement.incrementScore(getClassWeight(scoredElement));        
     }	
     
     /**
      * Get an elements class/id weight. Uses regular expressions to tell if this 
      * element looks good or bad.
      *
      * @param Element
      * @return number (Integer)
     **/
     public static float getClassWeight(Element e) {
         if(!flagIsActive(FLAG_WEIGHT_CLASSES)) {
             return 0;
         }
 
         float weight = 0;
 
         /* Look for a special classname */
         if (e.className() != null && !"".equals(e.className())) {
         	if (REGEX.get("negative").matcher(e.className()).find()) {
         		weight -= 25;
         	}
         	if (REGEX.get("positive").matcher(e.className()).find()) {
         		weight += 25;
         	}
         }
 
         /* Look for a special ID */
         if (e.id() != null && !"".equals(e.id())) {
         	if (REGEX.get("negative").matcher(e.id()).find()) {
         		weight -= 25;
         	}
         	if (REGEX.get("positive").matcher(e.id()).find()) {
         		weight += 25;
         	}
         }
         
         return weight;
     }    
 	
     /**
      * Prepare the HTML document for readability to scrape it.
      * This includes things like stripping javascript, CSS, and handling terrible markup.
      * 
      * @return void
      **/
     public static void prepDocument(Document document) {
         /**
          * In some cases a body element can't be found (if the HTML is totally hosed for example)
          * so we create a new body node and append it to the document.
          */    	    	
         if(document.body() == null) {
             Element body = document.createElement("body");
             Element head = document.head();
             head.after(body.html());                        
         }
 
         document.body().attr("id", "fauxwerdBody");
 
 //TODO need to understand how to grab this frame info        
 //        List<Element> frames = document.getElementsByTag("frame");
 //        if(frames.size() > 0) {
 //            Element bestFrame = null;
 //            int bestFrameSize = 0;    /* The frame to try to run readability upon. Must be on same domain. */
 //            int biggestFrameSize = 0; /* Used for the error message. Can be on any domain. */
 //
 //            for(int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
 // TODO revisit offsetWidth & offsetHeight            	
 //                int frameSize = frames.get(frameIndex).offsetWidth + frames[frameIndex].offsetHeight;
 //                boolean canAccessFrame = false;
 //                try {                	
 //                    Element frameBody = frames.get(frameIndex).contentWindow.document.body;
 //                    canAccessFrame = true;
 //                }
 //                catch(eFrames) {
 //                    dbg(eFrames);
 //                }
 //
 //                if(frameSize > biggestFrameSize) {
 //                    biggestFrameSize         = frameSize;
 //                    readability.biggestFrame = frames[frameIndex];
 //                }
 //                
 //                if(canAccessFrame && frameSize > bestFrameSize) {
 //                    readability.frameHack = true;
 //    
 //                    bestFrame = frames[frameIndex];
 //                    bestFrameSize = frameSize;
 //                }
 //            }
 //
 //            if(bestFrame) {
 //            	Element body = new Element(Tag.valueOf("body"), "");
 //                newBody.innerHTML = bestFrame.contentWindow.document.body.innerHTML;
 //                newBody.style.overflow = 'scroll';
 //                document.body = newBody;
 //                
 //                var frameset = document.getElementsByTagName('frameset')[0];
 //                if(frameset) {
 //                    frameset.parentNode.removeChild(frameset); }
 //            }
 //        }
 
         /* Remove all stylesheets */
         for (Element styleSheet : document.getElementsByTag("style")) {
         	if (styleSheet.attr("href") != null  && styleSheet.attr("href").lastIndexOf("readability") == -1) {
         		styleSheet.remove();
         	}
         	styleSheet.text("");
         }
 
         /* Turn all double br's into p's */
         /* Note, this is pretty costly as far as processing goes. Maybe optimize later. */                
         String fixedHtml = REGEX.get("replaceBrs").matcher(document.body().html()).replaceAll("</p><p>");
         fixedHtml = REGEX.get("replaceFonts").matcher(fixedHtml).replaceAll("<$1span>");
         document.body().html(fixedHtml);
                 
     }
     
     /**
      * Get the article title as an H1.
      *
      * @return void
      **/
     public static Element getArticleTitle(Document document) {
         String curTitle = "";
         String origTitle = "";
 
         curTitle = origTitle = document.title();
         
         //TODO can probably remove this, don't think it can ever occur
         if(!(curTitle instanceof String)) { /* If they had an element with id "title" in their HTML */
         	curTitle = origTitle = document.getElementsByTag("title").get(0).text();             
         }
         
         if(curTitle.matches(" [\\|\\-] ")) {
             curTitle = origTitle.replaceAll("(.*)[\\|\\-] .*","$1"); //global replace case insensitive
             
             if(curTitle.split(" ").length < 3) {
                 curTitle = origTitle.replaceAll("[^\\|\\-]*[\\|\\-](.*)","$1"); //global replace case insensitive
             }
         }
         else if(curTitle.indexOf(": ") != -1) {
             curTitle = origTitle.replaceAll(".*:(.*)", "$1"); //global replace case insensitive
 
             if(curTitle.split(" ").length < 3) {
                 curTitle = origTitle.replaceAll("[^:]*[:](.*)","$1"); //global replace case insensitive
             }
         }
         else if(curTitle.length() > 150 || curTitle.length() < 15) {
             Elements hOnes = document.getElementsByTag("h1");
             if(hOnes.size() == 1) {
                 curTitle = hOnes.get(0).text();
             }
         }
         
         curTitle = REGEX.get("trim").matcher(curTitle).replaceAll("");
 
         if(curTitle.split(" ").length <= 4) {
             curTitle = origTitle;
         }
         
         Element articleTitle = new Element(Tag.valueOf("H1"),"");
         articleTitle.html(curTitle);
         
         return articleTitle;
     }    
 
     /***
      * grabArticle - Using a variety of metrics (content score, classname, element types), find the content that is
      *               most likely to be the stuff a user wants to read. Then return it wrapped up in a div.
      *
      * @param page a document to run upon. Needs to be a full document, complete with body.
      * @return Element
     **/
     public static Element grabArticle(Document page) {		    	
         boolean stripUnlikelyCandidates = flagIsActive(FLAG_STRIP_UNLIKELYS);
 //TOOD revisit how this can be implemented
 //        boolean isPaging = (page != null) ? true: false;
         boolean isPaging = false;
         
 //in original js version, initial call to grabArticle does not include parameter, but we will always pass one so this line is unnecessary
 //        page = page != null ? page : document.body;
          
         String pageCacheHtml = page.html();
 
         Elements allElements = page.getAllElements();
 
         /**
          * First, node prepping. Trash nodes that look cruddy (like ones with the class name "comment", etc), and turn divs
          * into P tags where they have been used inappropriately (as in, where they contain no other block level elements.)
          *
          * Note: Assignment from index for performance. See http://www.peachpit.com/articles/article.aspx?p=31567&seqNum=5
          * TODO: Shouldn't this be a reverse traversal?
         **/
         Elements nodesToScore = new Elements();
         
         for(Element node : allElements) {
         	
             /* Remove unlikely candidates */
             if (stripUnlikelyCandidates) {
                 String unlikelyMatchString = node.className() + node.id();
                 	
                 if ((
                 		REGEX.get("unlikelyCandidates").matcher(unlikelyMatchString).find() &&
                 		REGEX.get("okMaybeItsACandidate").matcher(unlikelyMatchString).find() &&
                 		!node.tagName().equalsIgnoreCase("BODY")
                     )
                 ) {
                 	node.remove(); 
                 	continue;
                 }               
             }
             
             if (node.tagName().equalsIgnoreCase("P") || node.tagName().equalsIgnoreCase("TD") || node.tagName().equalsIgnoreCase("PRE")) {
             	nodesToScore.add(node);
             }
 
             /* Turn all divs that don't have children block level elements into p's */
             if (node.tagName().equalsIgnoreCase("DIV")) {
             	
             	if(!REGEX.get("divToPElements").matcher(node.html()).find()) {
                     Element newNode = new Element(Tag.valueOf("p"), "");
                     newNode.html(node.html());                                                            
 //                    if (log.isDebugEnabled()) log.debug(String.format("replacing %s with %s", node, newNode));
                     node.replaceWith(newNode);
                     nodesToScore.add(newNode);
                 }
 //                else
 //                {
 //                    /* EXPERIMENTAL */
 //                	for(int i = 0; i < node.childNodes().size(); i++) {
 //                    for(int i = 0, il = node.childNodes.length; i < il; i+=1) {
 //                        Node childNode = node.childNode(i);
 //
 //                        if(childNode. === 3) { // Node.TEXT_NODE
 //                            var p = document.createElement('p');
 //                            p.innerHTML = childNode.nodeValue;
 //                            p.style.display = 'inline';
 //                            p.className = 'readability-styled';
 //                            childNode.parentNode.replaceChild(p, childNode);
 //                        }
 //                    }
 //                }
             } 
         }
                 
         /**
          * Loop through all paragraphs, and assign a score to them based on how content-y they look.
          * Then add their score to their parent node.
          *
          * A score is determined by things like number of commas, class names, etc. Maybe eventually link density.
         **/
         List<ScoredElement> candidates = new ArrayList<ScoredElement>();
         for (Element nodeToScore : nodesToScore) {
         	//TODO revisit if this is a good idea, may need to change how we create/initialize new ScoredElements
             ScoredElement parentNode = nodeToScore.parent() != null ? new ScoredElement(nodeToScore.parent(),-1) : null;
             ScoredElement grandParentNode = (parentNode != null && parentNode.parent() != null) ? new ScoredElement(parentNode.parent(),-1) : null;
             String innerText = nodeToScore.text();
             
             if(parentNode == null || parentNode.tagName() == null) {
                 continue;
             }
             
             /* If this paragraph is less than 25 characters, don't even count it. */
             if(innerText.length() < 25) {
                 continue; 
             }            
             
             /* Initialize readability data for the parent. */
             if (parentNode.getScore() < 0) {
             	//TODO confirm if we need to use the returned Element instead                        	
             	initializeNode(parentNode);            	
             	candidates.add(parentNode);
             }
 
             /* Initialize readability data for the grandparent. */
             if (grandParentNode != null && grandParentNode.getScore() < 0) {
             	initializeNode(grandParentNode);
             	candidates.add(grandParentNode);
             }
 
             int contentScore = 0;
 
             /* Add a point for the paragraph itself as a base. */
             contentScore+=1;
 
             /* Add points for any commas within this paragraph */
             contentScore += innerText.split(",").length;
             
             /* For every 100 characters in this paragraph, add another point. Up to 3 points. */
             contentScore += Math.min(Math.floor(innerText.length() / 100), 3);
             
             /* Add the score to the parent. The grandparent gets half. */
             parentNode.incrementScore(contentScore);
 
             if(grandParentNode != null) {
                 grandParentNode.incrementScore(contentScore/2);             
             }
         }
 
 //        if (log.isDebugEnabled()) log.debug(String.format("candidates.size() = %s", candidates.size()));
         
         /**
          * After we've calculated scores, loop through all of the possible candidate nodes we found
          * and find the one with the highest score.
         **/
         ScoredElement topCandidate = null;
         for(ScoredElement candidate : candidates) {
             /**
              * Scale the final candidates score based on link density. Good content should have a
              * relatively small link density (5% or less) and be mostly unaffected by this operation.
             **/        	
             candidate.setScore(candidate.getScore() * (1 - getLinkDensity(candidate)));
 
 //            if (log.isDebugEnabled()) log.debug(String.format("\n----------\nCandidate with score %s: %s (%s:%s)\n----------", 
 //            		candidate.getScore(), candidate.outerHtml() , candidate.className(), candidate.id()));
 
             if(topCandidate == null || candidate.getScore() > topCandidate.getScore()) {
                 topCandidate = candidate; 
             }
         }
 
 //        if (log.isDebugEnabled()) log.debug(String.format("topCandidate = %s", topCandidate));
         
         /**
          * If we still have no top candidate, just use the body as a last resort.
          * We also have to copy the body node so it is something we can modify.
          **/
         if (topCandidate == null || topCandidate.tagName().equalsIgnoreCase("BODY")) {
             topCandidate = new ScoredElement(Tag.valueOf("DIV"), "");
             topCandidate.html(page.html());
             page.html("");
             page.appendChild(topCandidate);
             initializeNode(topCandidate);
         }
 
         /**
          * Now that we have the top candidate, look through its siblings for content that might also be related.
          * Things like preambles, content split by ads that we removed, etc.
         **/
 //TODO check it
 //        ScoredElement articleContent = new ScoredElement(Tag.valueOf("DIV"), "");
         
 //        if (log.isDebugEnabled()) log.debug(String.format("topCandidate = %s", topCandidate));
         
         ScoredElement articleContent = new ScoredElement(page.createElement("DIV"), -1);
         if (isPaging) {
             articleContent.attr("id", "readability-content");
         }
         float siblingScoreThreshold = (float)Math.max(10, topCandidate.getScore() * 0.2);
 
         Elements siblingNodes = (topCandidate != null && topCandidate.parent() != null) ? topCandidate.siblingElements() : new Elements();
                 
         for(int s=0; s < siblingNodes.size(); s++) {
         	ScoredElement siblingNode = new ScoredElement(siblingNodes.get(s), -1);
         	initializeNode(siblingNode);
         	        	
         	boolean append = false;
 
 //            if (log.isDebugEnabled()) log.debug(String.format("Looking at sibling node: %s (%s:%s) with score %s", siblingNode, siblingNode.className(), siblingNode.id(), siblingNode.getScore()));
 
             if(siblingNode.html().equals(topCandidate.html())) {
                 append = true;
             }
 
             float contentBonus = 0;
             /* Give a bonus if sibling nodes and top candidates have the example same classname */
             if(siblingNode.className().equals(topCandidate.className()) && !topCandidate.className().equals("")) {
                 contentBonus += topCandidate.getScore() * 0.2;
             }
             //TODO check if the score we have for sibling node at this point is sufficient (only using intialize)
             if((siblingNode.getScore() + contentBonus) >= siblingScoreThreshold) {
                 append = true;
             }
             
             if(siblingNode.tagName().equalsIgnoreCase("P")) {
                 float linkDensity = getLinkDensity(siblingNode);
                 String nodeContent = siblingNode.text();
                 int nodeLength  = nodeContent.length();
                 
                 Pattern pattern = Pattern.compile("\\.( |$)");
                                
                 if(nodeLength > 80 && linkDensity < 0.25) {
                     append = true;
                 }
                 else if(nodeLength < 80 && linkDensity == 0 && pattern.matcher(nodeContent).find()) {
                     append = true;
                 }
             }
 
             if(append) {
 //                if(log.isDebugEnabled()) log.debug(String.format("Appending node: %s", siblingNode));
 
                 ScoredElement nodeToAppend = null;
                 if(!siblingNode.tagName().equalsIgnoreCase("DIV") && !siblingNode.tagName().equalsIgnoreCase("P")) {
                     /* We have a node that isn't a common block level element, like a form or td tag. Turn it into a div so it doesn't get filtered out later by accident. */                    
 //                    if (log.isDebugEnabled()) log.debug(String.format("Altering siblingNode of %s  to div.", siblingNode.tagName()));
                     nodeToAppend = new ScoredElement(Tag.valueOf("DIV"), "");
                     nodeToAppend.attr("id", siblingNode.id());
                     nodeToAppend.html(siblingNode.html());
                 } else {
                     nodeToAppend = new ScoredElement(siblingNode, 0);
                 }
                 
                 /* To ensure a node does not interfere with readability styles, remove its classnames */
                 nodeToAppend.classNames(new HashSet<String>());
 
                 articleContent.appendChild(nodeToAppend);
             }
         }
         
 //        if (log.isDebugEnabled()) log.debug(String.format("articleContent = %s", articleContent));        
                 
         /**
          * So we have all of the content that we need. Now we clean it up for presentation.
         **/
         prepArticle(articleContent);
         
 //        if (log.isDebugEnabled()) log.debug(String.format("articleContent after prep = %s", articleContent));        
 //TODO revisit why below causes NPE in Jsoup classes        
 //        if (currentPageNum == 1) {
 //            articleContent.wrap("<div id='readability-page-1' class='page'></div>");
 //        }
 
         /**
          * Now that we've gone through the full algorithm, check to see if we got any meaningful content.
          * If we didn't, we may need to re-run grabArticle with different flags set. This gives us a higher
          * likelihood of finding the content, and the sieve approach gives us a higher likelihood of
          * finding the -right- content.
         **/
         if(articleContent.text().length() < 250) {
         	
         	if (log.isDebugEnabled()) log.debug(String.format("\n------------------ Article too short (%s chars), reparsing ----------------------", articleContent.text().length()));
         	
         	//reset page back - I think
         	page.html(pageCacheHtml);
 
             if (flagIsActive(FLAG_STRIP_UNLIKELYS)) {
                 removeFlag(FLAG_STRIP_UNLIKELYS);
                 return grabArticle(page);
             }
             else if (flagIsActive(FLAG_WEIGHT_CLASSES)) {
                 removeFlag(FLAG_WEIGHT_CLASSES);
                 return grabArticle(page);
             }
             else if (flagIsActive(FLAG_CLEAN_CONDITIONALLY)) {
                 removeFlag(FLAG_CLEAN_CONDITIONALLY);
                 return grabArticle(page);
             } else {
             	//TODO make this flag resetting less ugly            	
             	//reset flags for next article
             	FLAGS[0] = FLAG_STRIP_UNLIKELYS;
             	FLAGS[1] = FLAG_WEIGHT_CLASSES;
             	FLAGS[2] = FLAG_CLEAN_CONDITIONALLY;
                 return null;
             }
         }
 
     	//reset flags for next article
     	FLAGS[0] = FLAG_STRIP_UNLIKELYS;
     	FLAGS[1] = FLAG_WEIGHT_CLASSES;
     	FLAGS[2] = FLAG_CLEAN_CONDITIONALLY;
                 
         return articleContent;
     }
     
     /**
      * Get the density of links as a percentage of the content
      * This is the amount of text that is inside a link divided by the total text in the node.
      * 
      * @param Element
      * @return number (float)
     **/
     public static float getLinkDensity(Element e) {
         Elements links = e.getElementsByTag("a");
         int textLength = e.text().length();
         int linkLength = 0;
         for(Element link : links) {
             linkLength += link.text().length();
         }       
 
         return textLength != 0 ? linkLength / textLength : 0;
     }    
     
     /**
      * Prepare the article node for display. Clean out any inline styles,
      * iframes, forms, strip extraneous <p> tags, etc.
      *
      * @param Element
      * @return void
      **/
     public static void prepArticle(Element articleContent) {    	
         cleanStyles(articleContent);
         
         killBreaks(articleContent);
         
         /* Clean out junk from the article content */
         cleanConditionally(articleContent, "form");
         clean(articleContent, "object");
         clean(articleContent, "h1");
                 
         /**
          * If there is only one h2, they are probably using it
          * as a header and not a subheader, so remove it since we already have a header.
         ***/
         if(articleContent.getElementsByTag("h2").size() == 1) {
             clean(articleContent, "h2");
         }
         clean(articleContent, "iframe");
 
         cleanHeaders(articleContent);
                 
         /* Do these last as the previous stuff may have removed junk that will affect these */
         cleanConditionally(articleContent, "table");
         cleanConditionally(articleContent, "ul");                
         cleanConditionally(articleContent, "div");
                
         /* Remove extra paragraphs */
         Elements articleParagraphs = articleContent.getElementsByTag("p");
         for(Element articleParagraph : articleParagraphs) {
             int imgCount    = articleParagraph.getElementsByTag("img").size();
             int embedCount  = articleParagraph.getElementsByTag("embed").size();
             int objectCount = articleParagraph.getElementsByTag("object").size();
             
             if(imgCount == 0 && embedCount == 0 && objectCount == 0 && articleParagraph.text().equals("")) {
             	articleParagraph.remove();
             }
         }
         
         Pattern replacePattern = Pattern.compile("<br[^>]*>\\s*<p", Pattern.CASE_INSENSITIVE);
         articleContent.html(replacePattern.matcher(articleContent.html()).replaceAll("<p"));      
     }    
     
     /**
      * Remove the style attribute on every e and under.
      * TODO: Test if getElementsByTagName(*) is faster.
      *
      * @param Element
      * @return void
     **/
     public static void cleanStyles(Element e) {
         Element cur = (e.children() != null && !e.children().isEmpty()) ? e.child(0) : null;
 
         // Remove any root styles, if we're able.        
         if (!e.className().equals("readability-styled")) {
         	e.removeAttr("style");
         }
 
         // Go until there are no more child nodes
         while ( cur != null ) {
         	// Remove style attribute(s) :
         	if(!cur.className().equals("readability-styled")) {
         		cur.removeAttr("style");                   
         	}
         	cleanStyles( cur );
             cur = cur.nextElementSibling();
         }           
     }    
     
     /**
      * Remove extraneous break tags from a node.
      *
      * @param Element
      * @return void
      **/
     public static void killBreaks(Element e) {
     	e.html(REGEX.get("killBreaks").matcher(e.html()).replaceAll("<br />"));       
     }    
     
     /**
      * Clean an element of all tags of type "tag" if they look fishy.
      * "Fishy" is an algorithm based on content length, classnames, link density, number of images & embeds, etc.
      *
      * @return void
      **/
     public static void cleanConditionally(Element e, String tagString) {
 
         if(!flagIsActive(FLAG_CLEAN_CONDITIONALLY)) {
             return;
         }
 
         Elements tagsList = e.getElementsByTag(tagString);
         int curTagsLength = tagsList.size();
         
         /**
          * Gather counts for other typical elements embedded within.
          * Traverse backwards so we can remove nodes at the same time without effecting the traversal.
          *
          * TODO: Consider taking into account original contentScore here.
         **/
         
 //        if (log.isDebugEnabled()) log.debug(String.format("[%s] tagsList.size() = %s", tagString, tagsList.size()));
 //        if (log.isDebugEnabled()) log.debug(String.format("[%s] tagsList.outerHtml() = %s", tagString, tagsList.outerHtml()));
         
 
         for (int i=curTagsLength-1; i >= 0; i--) {
 //        	if (log.isDebugEnabled()) log.debug(String.format("\n[%s] ------------------------ %s --------------------------", tagString, i));
 
         	ScoredElement scoredTag = new ScoredElement(tagsList.get(i), -1);
         	initializeNode(scoredTag);
         	        	
             float weight = getClassWeight(scoredTag);
             float contentScore = scoredTag.getScore();
             
 //        	if (log.isDebugEnabled()) log.debug(String.format("contentScore = %s, classWeight = %s", contentScore, weight));                        
 //            if (log.isDebugEnabled()) log.debug(String.format("\n----------\nCleaning Conditionally with score %s: %s (%s:%s)\n----------", 
 //            		scoredTag.getScore(), scoredTag.outerHtml() , scoredTag.className(), scoredTag.id()));            
 
             if(weight+contentScore < 0) {
             	//TODO look into this further, tagsList.empty() was added b/c remove() didn't seem to work
             	if (tagsList.get(i).parent() != null) {
 //	            	if (log.isDebugEnabled()) log.debug(String.format("removing tag: %s", tagsList.get(i).outerHtml()));
 	            	tagsList.get(i).remove();
 	            	tagsList.empty();
             	} else {
             		//hack
 //            		if (log.isDebugEnabled()) log.debug(String.format("clearing tag: %s", tagsList.get(i).outerHtml()));
             		tagsList.get(i).empty();
             	}
             } else if (getCharCount(scoredTag,",") < 10) {
                 /**
                  * If there are not very many commas, and the number of
                  * non-paragraph elements is more than paragraphs or other ominous signs, remove the element.
                 **/
                 int p      = scoredTag.getElementsByTag("p").size();
                 int img    = scoredTag.getElementsByTag("img").size();
                 int li     = scoredTag.getElementsByTag("li").size()-100;
                 int input  = scoredTag.getElementsByTag("input").size();
 
                 int embedCount = 0;
                 Elements embeds = scoredTag.getElementsByTag("embed");
                 for(Element embed : embeds) {                	
                 	if (!REGEX.get("videos").matcher(embed.attr("src")).find()) {
                       embedCount+=1; 
                     }
                 }
 
                 float linkDensity = getLinkDensity(scoredTag);
                 int contentLength = scoredTag.text().length();
                 boolean toRemove = false;
 
                 if ( img > p ) {
                     toRemove = true;
                 } else if(li > p && !tagString.equalsIgnoreCase("ul") && !tagString.equalsIgnoreCase("ol")) {
                     toRemove = true;
                 } else if( input > Math.floor(p/3) ) {
                     toRemove = true; 
                 } else if(contentLength < 25 && (img == 0 || img > 2) ) {
                     toRemove = true;
                 } else if(weight < 25 && linkDensity > 0.2) {
                     toRemove = true;
                 } else if(weight >= 25 && linkDensity > 0.5) {
                     toRemove = true;
                 } else if((embedCount == 1 && contentLength < 75) || embedCount > 1) {
                     toRemove = true;
                 }
 
                 if(toRemove) {
                 	//TODO figure out if this null check is ok or if there is another way around this
                 	if (tagsList.get(i).parent() != null) {
                 		tagsList.get(i).remove();
 //                		if (log.isDebugEnabled()) log.debug(String.format("removing tag: %s", tagsList.get(i).outerHtml()));
                 	}   
                 	else {
                 		//hack
 //                		if (log.isDebugEnabled()) log.debug(String.format("clearing tag: %s", tagsList.get(i).outerHtml()));
                 		tagsList.get(i).empty();
                 	}
                 }
             }
             
 //            if (log.isDebugEnabled()) log.debug(String.format("------- [%s] > [%s] tagsList.outerHtml() = %s", i, tagString, tagsList.outerHtml()));            
         }
         
 //        if (log.isDebugEnabled()) log.debug(String.format("after > [%s] tagsList.size() = %s", tagString, tagsList.size()));
 //        if (log.isDebugEnabled()) log.debug(String.format("after > [%s] tagsList.outerHtml() = %s", tagString, tagsList.outerHtml()));
         
     }    
     
     /**
      * Clean a node of all elements of type "tag".
      * (Unless it's a youtube/vimeo video. People love movies.)
      *
      * @param Element
      * @param string tag to clean
      * @return void
      **/
     public static void clean(Element e, String tag) {
         Elements targetList = e.getElementsByTag(tag);
         boolean isEmbed = (tag.equalsIgnoreCase("object") || tag.equalsIgnoreCase("embed"));
         
         for (Element target : targetList) {
             /* Allow youtube and vimeo videos through as people usually want to see those. */
             if(isEmbed) {
                 String attributeValues = "";
                 for (Attribute attr : target.attributes()) {
                 	attributeValues += attr.getValue() + "|";
                 }                
                 
                 /* First, check the elements attributes to see if any of them contain youtube or vimeo */
                 if (REGEX.get("videos").matcher(attributeValues).find()) {
                 	continue;
                 }
 
                 /* Then check the elements inside this element for the same. */
                 if (REGEX.get("videos").matcher(target.html()).find()) {
                 	continue;
                 }                
                 
             }
 
             target.remove();
         }
     }    
     
     /**
      * Clean out spurious headers from an Element. Checks things like classnames and link density.
      *
      * @param Element
      * @return void
     **/
     public static void cleanHeaders(Element e) {
         for (int headerIndex = 1; headerIndex < 3; headerIndex+=1) {
             Elements headers = e.getElementsByTag("h" + headerIndex);
             for (int i=headers.size()-1; i >=0; i-=1) {
                 if (getClassWeight(headers.get(i)) < 0 || getLinkDensity(headers.get(i)) > 0.33) {
                     headers.get(i).remove();
                 }
             }
         }
     }    
     
     /**
      * Get the number of times a string s appears in the node e.
      *
      * @param Element
      * @param string - what to split on. Default is ","
      * @return number (integer)
     **/
     public static int getCharCount(Element e, String s) {
         return e.text().split(s).length-1;
     }    
     
     public static boolean flagIsActive(int flag) {
         	    	
     	for (int i : FLAGS) {
     		if (i == flag) return true;
     	}
     	
     	return false;
     }   
 
     public static void removeFlag(int flag) {
     	for (int i =0; i < FLAGS.length; i++) {
     		if (FLAGS[i] == flag) FLAGS[i] = 0;
     	}
     }        
     
     /**
      * Look for any paging links that may occur within the document.
      * 
      * @param body
      * @return object (array)
     **/
     public static String findNextPageLink(Element body, String url) {
     	
     	URL theUrl = null;
     	
     	try {
     		theUrl = new URL(url);
     	} catch (MalformedURLException e) {
     		if (log.isErrorEnabled()) log.error(null, e);
     	}    	
     	
     	List<Link> possiblePages = new ArrayList<Link>();
     	List<Element> allLinks = body.getElementsByTag("a");
     	String articleBaseUrl = findBaseUrl(url);
     	
 //    	var possiblePages = {},
 //      allLinks = elem.getElementsByTagName('a'),
 //      articleBaseUrl = readability.findBaseUrl();
     	
     	
         /**
          * Loop through all links, looking for hints that they may be next-page links.
          * Things like having "page" in their textContent, className or id, or being a child
          * of a node with a page-y className or id.
          *
          * Also possible: levenshtein distance? longest common subsequence?
          *
          * After we do that, assign each page a score, and 
         **/
         for(int i = 0; i < allLinks.size(); i++) {
             Element link = allLinks.get(i);
             //TODO vet this regex
             Link linkHref = new Link(allLinks.get(i).attr("href").replace("/#.*$", "").replace("/\\/$/", ""));
             
 //            href.replace(/#.*$/, '').replace(/\/$/, '');
 
             /* If we've already seen this page, ignore it */
             //TODO revisit last part of this conditional statement
             if(linkHref.getHref().equals("") || linkHref.getHref().equals("articleBaseUrl") || linkHref.getHref().equals("url") /*|| linkHref in readability.parsedPages*/) {
                 continue;
             }
             
             /* If it's on a different domain, skip it. */
             //TODO vet this regex
             if(theUrl.getHost() != linkHref.getHref().split("/\\/+/g")[1]) {
                 continue;
             }
             
             String linkText = link.text();
 
             /* If the linkText looks like it's not the next page, skip it. */
             //TODO reinstate part 1 of this conditional
             if(/*linkText.match(readability.regexps.extraneous) ||*/ linkText.length() > 25) {
                 continue;
             }
 
             /* If the leftovers of the URL after removing the base URL don't contain any digits, it's certainly not a next page link. */
             String linkHrefLeftover = linkHref.getHref().replace(articleBaseUrl, "");
             //TODO vet this regex
             if(!linkHrefLeftover.matches("/\\d/")) {
                 continue;
             }
             
 //TODO verify this is right            
             
             if(!(possiblePages.contains(linkHref))) {            	
             	//                possiblePages[linkHref] = {"score": 0, "linkText": linkText, "href": linkHref};             
             	possiblePages.add(new Link(linkHref.getHref(), linkText, 0));
             } else {
                 possiblePages.set(possiblePages.indexOf(linkHref), new Link(linkHref.getHref(), linkHref.getText() + " | " + linkText, linkHref.getScore()));
             }
 
 
 //TODO revist, does possiblePages need to be a map?
             Link linkObj = possiblePages.get(possiblePages.indexOf(linkHref));
 
             /**
              * If the articleBaseUrl isn't part of this URL, penalize this link. It could still be the link, but the odds are lower.
              * Example: http://www.actionscript.org/resources/articles/745/1/JavaScript-and-VBScript-Injection-in-ActionScript-3/Page1.html
             **/
             if(linkHref.getHref().indexOf(articleBaseUrl) != 0) {
                 linkObj.setScore(linkObj.getScore() - 25);
             }
 
             String linkData = linkText + " " + link.className() + ' ' + link.id();
 //TODO need to revisit this once global regex set up            
 //            if(linkData.match(readability.regexps.nextLink)) {
 //                linkObj.score += 50;
 //            }
             if(linkData.matches("/pag(e|ing|inat)/i")) {
                 linkObj.setScore(linkObj.getScore() + 25);
             }
             if(linkData.matches("/(first|last)/i")) { // -65 is enough to negate any bonuses gotten from a > or » in the text, 
                 /* If we already matched on "next", last is probably fine. If we didn't, then it's bad. Penalize. */
 //TODO need to revist once global regex set up
 //                if(!linkObj.getText().matches(readability.regexps.nextLink)) {
 //                    linkObj.setScore(linkObj.getScore() - 65);
 //                }
             }
 //            if(linkData.match(readability.regexps.negative) || linkData.match(readability.regexps.extraneous)) {
 //                linkObj.score -= 50;
 //            }
 //            if(linkData.match(readability.regexps.prevLink)) {
 //                linkObj.score -= 200;
 //            }
 
             /* If a parentNode contains page or paging or paginat */
             Element parentNode = link.parent();
             boolean positiveNodeMatch = false;
             boolean negativeNodeMatch = false;
 
 //TODO need to dig into this further            
 //            while(parentNode) {
 //                var parentNodeClassAndId = parentNode.className + ' ' + parentNode.id;
 //                if(!positiveNodeMatch && parentNodeClassAndId && parentNodeClassAndId.match(/pag(e|ing|inat)/i)) {
 //                    positiveNodeMatch = true;
 //                    linkObj.score += 25;
 //                }
 //                if(!negativeNodeMatch && parentNodeClassAndId && parentNodeClassAndId.match(readability.regexps.negative)) {
 //                    /* If this is just something like "footer", give it a negative. If it's something like "body-and-footer", leave it be. */
 //                    if(!parentNodeClassAndId.match(readability.regexps.positive)) {
 //                        linkObj.score -= 25;
 //                        negativeNodeMatch = true;                       
 //                    }
 //                }
 //                
 //                parentNode = parentNode.parentNode;
 //            }
 
             /**
              * If the URL looks like it has paging in it, add to the score.
              * Things like /page/2/, /pagenum/2, ?p=3, ?page=11, ?pagination=34
             **/
             if (linkHref.getHref().matches("/p(a|g|ag)?(e|ing|ination)?(=|\\/)[0-9]{1,2}/i") || linkHref.getHref().matches("/(page|paging)/i")) {
                 linkObj.setScore(linkObj.getScore() + 25);
             }
 
             /* If the URL contains negative values, give a slight decrease. */
 //TODO revisit once global regex is set up
 //            if (linkHref.match(readability.regexps.extraneous)) {
 //                linkObj.score -= 15;
 //            }
 
             /**
              * Minor punishment to anything that doesn't match our current URL.
              * NOTE: I'm finding this to cause more harm than good where something is exactly 50 points.
              *       Dan, can you show me a counterexample where this is necessary?
              * if (linkHref.indexOf(window.location.href) !== 0) {
              *    linkObj.score -= 1;
              * }
             **/
 
             /**
              * If the link text can be parsed as a number, give it a minor bonus, with a slight
              * bias towards lower numbered pages. This is so that pages that might not have 'next'
              * in their text can still get scored, and sorted properly by score.
             **/
 //TODO revisit
 //            int linkTextAsNumber = parseInt(linkText, 10);
 //            if(linkTextAsNumber) {
 //                // Punish 1 since we're either already there, or it's probably before what we want anyways.
 //                if (linkTextAsNumber === 1) {
 //                    linkObj.score -= 10;
 //                }
 //                else {
 //                    // Todo: Describe this better
 //                    linkObj.score += Math.max(0, 10 - linkTextAsNumber);
 //                }
 //            }
         }
 
         /**
          * Loop thrugh all of our possible pages from above and find our top candidate for the next page URL.
          * Require at least a score of 50, which is a relatively high confidence that this page is the next link.
         **/
         Link topPage = null;
         for(Link page : possiblePages) {
 //TODO revisit
 //            if(possiblePages.hasOwnProperty(page)) {
 //                if(possiblePages[page].score >= 50 && (!topPage || topPage.score < possiblePages[page].score)) {
 //                    topPage = possiblePages[page];
 //                }
 //            }
         }
 
 //        if(topPage) {
 //            var nextHref = topPage.href.replace(/\/$/,'');
 //
 //            dbg('NEXT PAGE IS ' + nextHref);
 //            readability.parsedPages[nextHref] = true;
 //            return nextHref;            
 //        }
 //        else {
 //            return null;
 //        }
         
         return null;
     }	
     
     
     public static String findBaseUrl(String url) {
     	//TODO parse full url out to protocol, host and path    	
     	URL theUrl = null;
     	
     	try {
     		theUrl = new URL(url);
     	} catch (MalformedURLException e) {
     		if (log.isErrorEnabled()) log.error(null, e);
     	}
     	
     	String noUrlParams = theUrl.getPath().split("\\?")[0];
     	List<String> urlSlashes = (Arrays.asList(noUrlParams.split("/")));
     	Collections.reverse(urlSlashes);
     	List<String> cleanedSegments = new ArrayList<String>();
     	String possibleType = "";
     	
 /*
     	var noUrlParams     = window.location.pathname.split("?")[0],
             urlSlashes      = noUrlParams.split("/").reverse(),
             cleanedSegments = [],
             possibleType    = "";
 */
     	    	
         for (int i = 0; i < urlSlashes.size(); i++) {
             String segment = urlSlashes.get(i);
 
             if (log.isDebugEnabled()) log.debug(String.format("url segment = %s", segment));
             
             // Split off and save anything that looks like a file type.
             if (segment.indexOf(".") != -1) {
                possibleType = segment.split("\\.")[1];
 
                 /* If the type isn't alpha-only, it's probably not actually a file extension. */
                 //TODO vet this regex
                 if(!possibleType.matches("/[^a-zA-Z]/")) {
                     segment = segment.split(".")[0];                    
                 }
             }
             
             /**
              * EW-CMS specific segment replacement. Ugly.
              * Example: http://www.ew.com/ew/article/0,,20313460_20369436,00.html
             **/
             if(segment.indexOf(",00") != -1) {
                 segment = segment.replace(",00", "");
             }
 
             // If our first or second segment has anything looking like a page number, remove it.
             //TODO vet this regex
             if (segment.matches("/((_|-)?p[a-z]*|(_|-))[0-9]{1,2}$/i") && ((i == 1) || (i == 0))) {
                 segment = segment.replace("/((_|-)?p[a-z]*|(_|-))[0-9]{1,2}$/i", "");
             }
 
 
             boolean del = false;
 
             /* If this is purely a number, and it's the first or second segment, it's probably a page number. Remove it. */
             //TODO vet this regex
             if (i < 2 && segment.matches("/^\\d{1,2}$/")) {
                 del = true;
             }
             
             /* If this is the first segment and it's just "index", remove it. */
             if(i == 0 && segment.toLowerCase() == "index") {
                 del = true;
             }
 
             /* If our first or second segment is smaller than 3 characters, and the first segment was purely alphas, remove it. */
             if(i < 2 && segment.length() < 3 && !urlSlashes.get(0).matches("/[a-z]/i")) {
                 del = true;
             }
 
             /* If it's not marked for deletion, push it to cleanedSegments. */
             if (!del) {
                 cleanedSegments.add(segment);
             }
         }
 
         // This is our final, cleaned, base article URL.
 //        return window.location.protocol + "//" + window.location.host + cleanedSegments.reverse().join("/");
         
         Collections.reverse(cleanedSegments);
         String cleanedSegmentsString = "";
         for (String seg : cleanedSegments)
         	cleanedSegmentsString = cleanedSegmentsString + seg + "/";
         
         return String.format("%s//%s%s", theUrl.getProtocol(), theUrl.getHost(), cleanedSegmentsString);
     }    
 
 }
