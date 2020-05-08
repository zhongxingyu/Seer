 package com.fingy.kickstarter.scrape;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.fingy.kickstarter.Comment;
 import com.fingy.scrape.jsoup.AbstractJsoupScraper;
 import com.fingy.scrape.util.JsoupParserUtil;
 
 public class CommentsJsoupScraper extends AbstractJsoupScraper<Collection<Comment>> {
 
 	public CommentsJsoupScraper(String scrapeUrl) {
 		super(scrapeUrl);
 	}
 
 	@Override
 	protected Collection<Comment> scrapePage(Document page) {
 		List<Comment> comments = new ArrayList<>();
 		comments.addAll(getCommentsFromPage(page));
 
 		int currentPage = 1;
 		String cursor = getCursorFromPage(page);
 		while (true) {
 			try {
				Document nextPage = getPage(getScrapeUrl() + "/?cursor=" + cursor);
 				Collection<Comment> nextPageComments = getCommentsFromPage(nextPage);
 
 				if (nextPageComments.isEmpty())
 					break;
 				else {
					cursor = getCursorFromPage(page);
 					comments.addAll(nextPageComments);
 				}
 
 				System.out.println("Scraping page " + currentPage++);
 			} catch (IOException ignored) {
 			}
 		}
 		return new LinkedHashSet<>(comments);
 	}
 
	private String getCursorFromPage(Document page) {
		return extractCommentFromElement(page.select(".comments li").last()).getId();
	}

 	private Collection<Comment> getCommentsFromPage(Document page) {
 		List<Comment> comments = new ArrayList<>();
 		Elements foundComments = page.select(".comments .comment");
 
 		for (Element commentElement : foundComments) {
 			Comment comment = extractCommentFromElement(commentElement);
 			comments.add(comment);
 		}
 
 		return comments;
 	}
 
 	private Comment extractCommentFromElement(Element commentElement) {
 		String commentId = scrapeCommentIdFromElement(commentElement);
 		String commenterName = scrapeCommenterNameFromElement(commentElement);
 		String commenterUrl = scrapeCommenterUrlFromElement(commentElement);
 		String commentTimestamp = scrapeCommentTimestampFromElement(commentElement);
 		String commentText = scrapeCommentTextElement(commentElement);
 		return new Comment(commentId, commenterName, commenterUrl, commentTimestamp, commentText);
 	}
 
 	private String scrapeCommentIdFromElement(Element commentElement) {
 		return commentElement.id().replace("comment-", "");
 	}
 
 	private String scrapeCommenterNameFromElement(Element commentElement) {
 		return JsoupParserUtil.getTagTextFromCssQuery(commentElement, "div.comment-inner div.main h3 a.author");
 	}
 
 	private String scrapeCommenterUrlFromElement(Element commentElement) {
 		String prefix = "http://www.kickstarter.com";
		Element authorTag = JsoupParserUtil
				.getTagFromCssQuery(commentElement, "div.comment-inner div.main h3 a.author");
 		return prefix + authorTag.attr("href");
 	}
 
 	private String scrapeCommentTimestampFromElement(Element commentElement) {
		Element dateTag = JsoupParserUtil.getTagFromCssQuery(
			commentElement,
			"div.comment-inner div.main h3 span.date data");
 		return dateTag.val();
 	}
 
 	private String scrapeCommentTextElement(Element commentElement) {
 		return JsoupParserUtil.getTagTextFromCssQuery(commentElement, "div.comment-inner div.main p");
 	}
 
 	public static void main(String[] args) throws IOException {
 		String scrapeUrl = "http://www.kickstarter.com/projects/559914737/the-veronica-mars-movie-project/comments";
 		Collection<Comment> comments = new CommentsJsoupScraper(scrapeUrl).call();
 		FileUtils.writeLines(new File("comments.txt"), comments);
 		System.out.println(comments);
 	}
 
 }
