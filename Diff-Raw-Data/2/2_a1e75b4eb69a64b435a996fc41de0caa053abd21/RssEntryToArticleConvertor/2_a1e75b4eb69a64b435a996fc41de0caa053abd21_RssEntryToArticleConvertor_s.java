 package nz.gen.wellington.guardian.contentapiproxy.datasources.rss;
 
 import java.util.Map;
 
 import nz.gen.wellington.guardian.contentapiproxy.datasources.ArticleHtmlCleaner;
 import nz.gen.wellington.guardian.contentapiproxy.model.Article;
 import nz.gen.wellington.guardian.contentapiproxy.model.MediaElement;
 import nz.gen.wellington.guardian.contentapiproxy.model.Section;
 import nz.gen.wellington.guardian.contentapiproxy.model.Tag;
 
 import org.apache.log4j.Logger;
 import org.htmlparser.Node;
 import org.htmlparser.NodeFilter;
 import org.htmlparser.Parser;
 import org.htmlparser.filters.HasAttributeFilter;
 import org.htmlparser.util.NodeIterator;
 import org.htmlparser.util.NodeList;
 import org.htmlparser.util.ParserException;
 import org.joda.time.DateTime;
 
 import com.sun.syndication.feed.module.DCModule;
 import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
 import com.sun.syndication.feed.module.mediarss.MediaModule;
 import com.sun.syndication.feed.module.mediarss.types.MediaContent;
 import com.sun.syndication.feed.module.mediarss.types.Metadata;
 import com.sun.syndication.feed.module.mediarss.types.UrlReference;
 import com.sun.syndication.feed.synd.SyndEntry;
 
 public class RssEntryToArticleConvertor {
 
 	Logger log = Logger.getLogger(RssEntryToArticleConvertor.class);
 	
 	public Article entryToArticle(SyndEntry item, Map<String, Section> sections) {
 		
 		DCModule dcModule = (DCModule) item.getModule("http://purl.org/dc/elements/1.1/");
 		if (dcModule == null || dcModule.getType() == null || !dcModule.getType().equals("Article")) {
 			return null;
 		}
 		
 		Article article = new Article();
 		article.setId(dcModule.getIdentifier());
 		article.setTitle(ArticleHtmlCleaner.stripHtml(item.getTitle()));
 		article.setPubDate(new DateTime(item.getPublishedDate()));
 		article.setByline(ArticleHtmlCleaner.stripHtml(item.getAuthor()));
 		
 		setSectionFromDCSubject(dcModule, article, sections);
 		
 		final String description = item.getDescription().getValue();
 		processBody(description, article, sections);
 		
 		processMediaElements(item, article);
 		
 		if (article.getPubDate() != null && article.getSection() != null) {
 			return article;
 		}
 		return null;
 	}
 
 
 	private void processMediaElements(SyndEntry item, Article article) {
 		MediaEntryModuleImpl mediaModule = (MediaEntryModuleImpl) item.getModule(MediaModule.URI);
         if (mediaModule != null) {
         
 	         log.debug("Found media module");        
 	         MediaContent[] mediaContents = mediaModule.getMediaContents();
 	         if (mediaContents.length > 0) {
 	        	 MediaContent mediaContent = mediaContents[0];
 	        	 
 	        	 if (mediaContent.getType().startsWith("image")) {
 	        		 UrlReference reference = (UrlReference) mediaContent.getReference();
 	        		 if (mediaContent.getWidth() == 140 && mediaContent.getHeight() == 84) {
 	        			 article.setThumbnailUrl(reference.getUrl().toExternalForm());
 	        		 }
 	        	 }
 	        	 
 	        	 for (int i = 0; i < mediaContents.length; i++) {
 	        		 mediaContent = mediaContents[i];
	        		 if (mediaContent.getType().startsWith("image")) {
 	        			 UrlReference reference = (UrlReference) mediaContent.getReference();
 		        		 Metadata metadata = mediaContent.getMetadata();
 	        			 if (mediaContent.getWidth() == 460) {
 	        				 MediaElement picture = new MediaElement("picture", reference.getUrl().toExternalForm(), metadata.getDescription());
 	        				 article.addMediaElement(picture);
 	        			 }	
 	        		 }
 	        	 }
 	         }
         }
 
 	}
 
 
 	private void processBody(final String description, Article article, Map<String, Section> sections) {
 		Parser parser = new Parser();
 		 try {
 			extractStandfirstAndBodyText(description, article, parser);		
 			extractTagsFromRelatedDiv(description, article, parser, sections);
 			
 		} catch (ParserException e) {
 			log.error(e.getMessage());
 		}
 	}
 
 
 	private void extractStandfirstAndBodyText(final String description,
 			Article article, Parser parser) throws ParserException {
 		parser.setInputHTML(description);
 		NodeFilter standfirstFilter = new HasAttributeFilter("class", "standfirst");
 		NodeList list = parser.extractAllNodesThatMatch(standfirstFilter);
 		if (list.size() > 0) {
 			final String standfirst = list.elementAt(0).toHtml();
 			article.setStandfirst(ArticleHtmlCleaner.stripHtml(standfirst));
 			description.replace(standfirst, "");
 		}
 		
 		StringBuilder body = new StringBuilder();
 		parser.setInputHTML(description);
 		NodeIterator elements = parser.elements();
 		while (elements.hasMoreNodes()) {
 			Node node = elements.nextNode();
 			if (node instanceof org.htmlparser.Tag) {
 				org.htmlparser.Tag tag = (org.htmlparser.Tag) node;
 				final String tagClass = tag.getAttribute("class");
 				if (tagClass == null || !(
 						tagClass.equals("standfirst") || 
 						tagClass.equals("track") ||
 						tagClass.equals("related") || 
 						tagClass.equals("terms") || 
 						tagClass.equals("author"))) {
 					body.append(tag.toHtml());
 				}
 			}
 		}
 		
 		body.append("<p>&copy; Guardian News & Media Limited " + new DateTime().toString("yyyy") + "</p>");
 		article.setDescription(ArticleHtmlCleaner.stripHtml(body.toString()));
 	}
 
 
 	private void extractTagsFromRelatedDiv(final String description, Article article, Parser parser, Map<String, Section> sections) throws ParserException {
 		parser.setInputHTML(description);
 		NodeFilter relatedFilter = new HasAttributeFilter("class", "related");
 		NodeList list = parser.extractAllNodesThatMatch(relatedFilter);
 		if (list.size() > 0) {
 			org.htmlparser.Tag related = (org.htmlparser.Tag) list.elementAt(0);
 			
 			Node tagList = related.getFirstChild();
 			NodeList tags = tagList.getChildren();
 			for (int i = 0; i < tags.size(); i++) {
 				org.htmlparser.Tag tag = (org.htmlparser.Tag) tags.elementAt(i);
 				
 				org.htmlparser.Tag href = (org.htmlparser.Tag) tag.getFirstChild();
 				String id = href.getAttribute("href");
 				id = id.replace("http://www.guardian.co.uk/", "");
 
 				final String sectionId = id.split("/")[0];
 				if (sectionId.equals("profile")) {
 					article.addTag(new Tag(href.toPlainTextString(), id, null, "contributor"));
 
 				} else {
 					Section section = sections.get(sectionId);
 					article.addTag(new Tag(href.toPlainTextString(), id, section, "keyword"));
 				}
 			}
 		}
 		
 		parser.setInputHTML(description);
 		NodeFilter authorsFilter = new HasAttributeFilter("class", "author");
 		list = parser.extractAllNodesThatMatch(authorsFilter);
 		if (list.size() > 0) {
 			
 			for (int i = 0; i < list.size(); i++) {
 				org.htmlparser.Tag tag = (org.htmlparser.Tag) list.elementAt(i);
 				
 				org.htmlparser.Tag href = (org.htmlparser.Tag) tag.getFirstChild();
 				String id = href.getAttribute("href");
 				id = id.replace("http://www.guardian.co.uk/", "");
 
 				String sectionId = id.split("/")[0];
 				if (sectionId.equals("profile")) {
 					article.addTag(new Tag(href.toPlainTextString(), id, null, "contributor"));
 
 				} else {
 					Section section = sections.get(sectionId);
 					article.addTag(new Tag(href.toPlainTextString(), id, section, "keyword"));
 				}
 			}
 		}
 		
 		
 	}
 
 	
 	private void setSectionFromDCSubject(DCModule dcModule, Article article, Map<String, Section> sections) {
 		String sectionName = dcModule.getSubject().getValue();		
 		article.setSection(getSectionByName(sections, sectionName));
 	}
 
 
 	private Section getSectionByName(Map<String, Section> sections, String sectionName) {
 		for (String sectionId : sections.keySet()) {
 			Section section = sections.get(sectionId);
 			if (section.getName().equals(sectionName)) {
 				return section;
 			}	
 		}
 		return null;
 	}
 	
 }
