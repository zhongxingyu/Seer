 package org.jamwiki.parser.bliki;
 
 import info.bliki.wiki.filter.TemplateParser;
 import info.bliki.wiki.filter.WikipediaParser;
 import info.bliki.wiki.model.AbstractWikiModel;
 import info.bliki.wiki.model.Configuration;
 import info.bliki.wiki.model.ImageFormat;
 import info.bliki.wiki.tags.util.TagStack;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.Set;
 
 import org.htmlcleaner.ContentToken;
 import org.htmlcleaner.TagNode;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.Topic;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.jflex.JFlexParser;
 import org.jamwiki.parser.jflex.WikiHeadingTag;
 import org.jamwiki.parser.jflex.WikiSignatureTag;
 import org.jamwiki.utils.InterWikiHandler;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.NamespaceHandler;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiUtil;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * An IWikiModel model implementation for JAMWiki
  * 
  */
 public class JAMWikiModel extends AbstractWikiModel {
	// see: JFlexParser.MODE_MINIMAL
	protected static final int MODE_MINIMAL = 3;
	
 	private static final WikiLogger logger = WikiLogger.getLogger(WikiHeadingTag.class.getName());
 
 	protected String fBaseURL;
 
 	protected ParserInput fParserInput;
 
 	protected ParserOutput fDocument;
 
 	static {
 
 		TagNode.addAllowedAttribute("style");
 	}
 
 	public JAMWikiModel(ParserInput parserInput, ParserOutput document, String baseURL) {
 		super(Configuration.DEFAULT_CONFIGURATION);
 
 		fParserInput = parserInput;
 		fDocument = document;
 		fBaseURL = baseURL;
 	}
 
 	public void parseInternalImageLink(String imageNamespace, String name) {
 		// see JAMHTMLConverter#imageNodeToText() for the real HTML conversion
 		// routine!!!
 		ImageFormat imageFormat = ImageFormat.getImageFormat(name, imageNamespace);
 
 		int pxSize = imageFormat.getSize();
 		String caption = imageFormat.getCaption();
 		TagNode divTagNode = new TagNode("div");
 		divTagNode.addAttribute("id", "image", false);
 		// divTagNode.addAttribute("href", hrefImageLink, false);
 		// divTagNode.addAttribute("src", srcImageLink, false);
 		divTagNode.addObjectAttribute("wikiobject", imageFormat);
 		if (pxSize != -1) {
 			divTagNode.addAttribute("style", "width:" + pxSize + "px", false);
 		}
 		pushNode(divTagNode);
 
 		if (caption != null && caption.length() > 0) {
 
 			TagNode captionTagNode = new TagNode("div");
 			String clazzValue = "caption";
 			String type = imageFormat.getType();
 			if (type != null) {
 				clazzValue = type + clazzValue;
 			}
 			captionTagNode.addAttribute("class", clazzValue, false);
 
 			TagStack localStack = WikipediaParser.parseRecursive(caption, this, true, true);
 			captionTagNode.addChildren(localStack.getNodeList());
 			String altAttribute = captionTagNode.getBodyString();
 			imageFormat.setAlt(altAttribute);
 			pushNode(captionTagNode);
 			// WikipediaParser.parseRecursive(caption, this);
 			popNode();
 		}
 
 		popNode(); // div
 
 	}
 
 	public void appendSignature(Appendable writer, int numberOfTildes) throws IOException {
 		WikiSignatureTag parserTag;
 		switch (numberOfTildes) {
 		case 3:
 			parserTag = new WikiSignatureTag();
			writer.append(parserTag.parse(fParserInput, fDocument, MODE_MINIMAL, "~~~"));
 			break;
 		case 4:
 			parserTag = new WikiSignatureTag();
			writer.append(parserTag.parse(fParserInput, fDocument, MODE_MINIMAL, "~~~~"));
 			break;
 		case 5:
 			parserTag = new WikiSignatureTag();
			writer.append(parserTag.parse(fParserInput, fDocument, MODE_MINIMAL, "~~~~~"));
 			break;
 		}
 	}
 
 	public void appendInternalLink(String topic, String hashSection, String linkText, String cssStyle) {
 		String encodedtopic = WikiUtil.encodeForFilename(topic);
 		String topicURL = fBaseURL + encodedtopic;
 		String style = null;
 		try {
 			if (InterWikiHandler.isInterWiki(fParserInput.getVirtualWiki())) {
 				style = "interwiki";
 			} else if (!LinkUtil.isExistingArticle(fParserInput.getVirtualWiki(), topic)) {
 				style = "edit";
 				topicURL = fBaseURL + "Special:Edit?topic=" + encodedtopic;
 			}
 		} catch (Exception e) {
 
 		}
 
 		super.appendInternalLink(topicURL, hashSection, linkText, style);
 	}
 
 	public void addCategory(String categoryName, String sortKey) {
 		fDocument.addCategory(getCategoryNamespace() + NamespaceHandler.NAMESPACE_SEPARATOR + categoryName, sortKey);
 	}
 
 	public void addLink(String topic) {
 		fDocument.addLink(topic);
 	}
 
 	public void addTemplate(String template) {
 		fDocument.addTemplate(template);
 	}
 
 	public String getRawWikiContent(String namespace, String topicName, Map map) {
 		String result = super.getRawWikiContent(namespace, topicName, map);
 		if (result != null) {
 			return result;
 		}
 		try {
 			topicName = topicName.replaceAll("_", " ");
 			Topic topic = WikiBase.getDataHandler().lookupTopic(fParserInput.getVirtualWiki(), namespace + ':' + topicName, false, null);
 			if (topic == null) {
 				return null;
 			}
 			return topic.getTopicContent();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	public void buildEditLinkUrl(int section) {
 		if (fParserInput.getAllowSectionEdit()) {
 			TagNode divTagNode = new TagNode("div");
 			divTagNode.addAttribute("style", "font-size:90%;float:right;margin-left:5px;", false);
 			divTagNode.addChild(new ContentToken("["));
 			append(divTagNode);
 
 			String url = "";
 			try {
 				url = LinkUtil.buildEditLinkUrl(fParserInput.getContext(), fParserInput.getVirtualWiki(), fParserInput.getTopicName(),
 						null, section);
 			} catch (Exception e) {
 				logger.severe("Failure while building link for topic " + fParserInput.getVirtualWiki() + " / "
 						+ fParserInput.getTopicName(), e);
 			}
 			TagNode aTagNode = new TagNode("a");
 			aTagNode.addAttribute("href", url, false);
 			aTagNode.addChild(new ContentToken(Utilities.formatMessage("common.sectionedit", fParserInput.getLocale())));
 			divTagNode.addChild(aTagNode);
 			divTagNode.addChild(new ContentToken("]"));
 		}
 	}
 
 	public boolean parseBBCodes() {
 		return false;
 	}
 
 	public boolean replaceColon() {
 		return false;
 	}
 
 	public String getCategoryNamespace() {
 		return NamespaceHandler.NAMESPACE_CATEGORY;
 	}
 
 	public String getImageNamespace() {
 		return NamespaceHandler.NAMESPACE_IMAGE;
 	}
 
 	public String getTemplateNamespace() {
 		return NamespaceHandler.NAMESPACE_TEMPLATE;
 	}
 
 	public Set getLinks() {
 		return null;
 	}
 
 	public void appendInterWikiLink(String namespace, String title, String linkText) {
 		// no interwiki link parsing
 		return;
 	}
 
 	public boolean isTemplateTopic() {
 		String topicName = fParserInput.getTopicName();
 		int index = topicName.indexOf(':');
 		if (index > 0) {
 			String namespace = topicName.substring(0, index);
 			if (isTemplateNamespace(namespace)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean isMathtranRenderer() {
 		return true;
 	}
 
 	public String parseTemplates(String rawWikiText, boolean parseOnlySignature) {
 		if (rawWikiText == null) {
 			return "";
 		}
 		if (!parseOnlySignature) {
 			initialize();
 		}
 		StringBuilder buf = new StringBuilder(rawWikiText.length() + rawWikiText.length() / 10);
 		try {
 			TemplateParser.parse(rawWikiText, this, buf, parseOnlySignature);
 		} catch (Exception ioe) {
 			ioe.printStackTrace();
 			buf.append("<span class=\"error\">TemplateParser exception: " + ioe.getClass().getSimpleName() + "</span>");
 		}
 		return buf.toString();
 	}
 
 }
