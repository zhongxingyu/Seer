 package org.jamwiki.parser.bliki;
 
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.Topic;
 import org.jamwiki.parser.ParserDocument;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.NamespaceHandler;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
import info.bliki.wiki.filter.AbstractWikiModel;
import info.bliki.wiki.filter.Encoder;
import info.bliki.wiki.filter.ImageFormat;
 
 /**
  * Standard model implementation
  *
  */
 public class JAMWikiModel extends AbstractWikiModel {
	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiModel.class.getName());
 
 	private static final int DEFAULT_THUMBNAIL_SIZE = 180;
 
 	protected String fExternalImageBaseURL;
 
 	protected String fExternalWikiBaseURL;
 
 	protected ParserInput fParserInput;
 
 	protected ParserDocument fDocument;
 
 	public JAMWikiModel(ParserInput parserInput, ParserDocument document, String imageBaseURL, String linkBaseURL) {
 		super();
 		fParserInput = parserInput;
 		fDocument = document;
 		fExternalImageBaseURL = imageBaseURL;
 		fExternalWikiBaseURL = linkBaseURL;
 	}
 
 	public void parseInternalImageLink(StringBuffer writer, String imageNamespace, String name) {
 		if (fExternalImageBaseURL != null) {
 			ImageFormat imageFormat = ImageFormat.getImageFormat(name, imageNamespace);
 
 			String imageName = imageFormat.getFilename();
 			imageName = imageName.replaceAll("_", " ");
 			int maxDimension = imageFormat.getSize();
 			String type = imageFormat.getType();
 			boolean frame = type == null ? false : type.equals("frame");
 			boolean thumb = type == null ? false : type.equals("thumb");
 			if (thumb && maxDimension <= 0) {
 				maxDimension = DEFAULT_THUMBNAIL_SIZE;
 			}
 			try {
 				writer
 						.append(LinkUtil.buildImageLinkHtml(fParserInput.getContext(), fParserInput.getVirtualWiki(), getImageNamespace() + NamespaceHandler.NAMESPACE_SEPARATOR
 								+ imageName, frame, thumb, imageFormat.getLocation(), imageFormat.getCaption(), maxDimension, false, null, false));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void appendInternalLink(StringBuffer writer, String link, String hashSection, String linkText) {
 		String hrefLink = fExternalWikiBaseURL;
 		String encodedtopic = Encoder.encodeTitleUrl(link);
 		hrefLink = StringUtils.replace(hrefLink, "${title}", encodedtopic);
 		super.appendInternalLink(writer, hrefLink, hashSection, linkText);
 	}
 
 	public void addCategory(String categoryName, String sortKey) {
 		fDocument.addCategory(getCategoryNamespace()+NamespaceHandler.NAMESPACE_SEPARATOR+categoryName, sortKey);
 	}
 
 	public void addLink(String topic) {
 		fDocument.addLink(topic);
 	}
 
 	public void addTemplate(String template) {
 		fDocument.addTemplate(template);
 	}
 
 	public String getRawWikiContent(String namespace, String topicName) {
 		String result = super.getRawWikiContent(namespace, topicName);
 		if (result != null) {
 			return result;
 		}
 		try {
 			topicName = topicName.replaceAll("_", " ");
 			Topic topic = WikiBase.getDataHandler().lookupTopic(fParserInput.getVirtualWiki(), namespace + ':' + topicName, false, null);
 			if (topic==null) {
 				return null;
 			}
 			return topic.getTopicContent();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	public String buildEditLinkUrl(int section) {
 		if (fParserInput.getAllowSectionEdit()) {
 
 			// FIXME - template inclusion causes section edits to break, so disable
 			// for now
 			// String inclusion =
 			// (String)fParserInput.getTempParams().get(TemplateTag.TEMPLATE_INCLUSION);
 			// boolean disallowInclusion = (inclusion != null &&
 			// inclusion.equals("true"));
 			// if (disallowInclusion) return "";
 			String output = "<div style=\"font-size:90%;float:right;margin-left:5px;\">[";
 			String url = "";
 			try {
 				url = LinkUtil.buildEditLinkUrl(fParserInput.getContext(), fParserInput.getVirtualWiki(), fParserInput.getTopicName(),
 						null, section);
 			} catch (Exception e) {
 				logger.severe("Failure while building link for topic " + fParserInput.getVirtualWiki() + " / "
 						+ fParserInput.getTopicName(), e);
 			}
 			output += "<a href=\"" + url + "\">";
 			output += Utilities.formatMessage("common.sectionedit", fParserInput.getLocale());
 			output += "</a>]</div>";
 			return output;
 		}
 		return "";
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
 
 }
