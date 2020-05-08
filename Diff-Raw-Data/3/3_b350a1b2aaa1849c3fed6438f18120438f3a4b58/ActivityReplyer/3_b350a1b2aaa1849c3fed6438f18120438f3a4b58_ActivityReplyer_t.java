 package com.cj.lion.replyer;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import lombok.Setter;
 
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.util.UriComponentsBuilder;
 
 import com.cj.config.AppProperties;
 import com.cj.domain.received.ImageReceivedMessage;
 import com.cj.domain.received.ReceivedMessage;
 import com.cj.domain.received.TextReceivedMessage;
 import com.cj.domain.sent.Article;
 import com.cj.domain.sent.NewsSentContent;
 import com.cj.domain.sent.SentContent;
 import com.cj.replyer.Replyer;
 
 @Setter
 public class ActivityReplyer implements Replyer {
 
	@Autowired
 	private AppProperties appProperties;
 
 	private final Date endDate;
 	private static final String FLAG = "新生新体验";
 	private final NewsSentContent newsSentContentStart;
 
 	private static final String TITLE = "请提交联系方式来抽奖";
 	private static final String DESCRIPTION = "我们将选出最美牙刷";
 	private static final String DESCRIPTION_START = "发送牙刷靓照参与有奖活动";
 	private static final String PIC_URL_START = "/resources/site/student/student.jpg";
 	private static final String PIC_URL = "/resources/site/student/studentInfo.jpg";
 	private static final String URL_START="/resources/site/brand4.html";
 	private static final String URL = "/user/studentInfo";
 
 	public ActivityReplyer() {
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(2013, 9, 7);
 		endDate = calendar.getTime();
 		newsSentContentStart = new NewsSentContent();
 		List<Article> articles = new ArrayList<Article>();
 		newsSentContentStart.setArticles(articles);
 		Article article = new Article();
 		articles.add(article);
 		article.setTitle(FLAG);
 		article.setDescription(DESCRIPTION_START);
 		article.setPicUrl(appProperties.getSiteBase() + PIC_URL_START);
 		article.setUrl(appProperties.getSiteBase()+URL_START);
 	}
 
 	@Override
 	public SentContent reply(ReceivedMessage receivedMessage) {
 		Date now = new Date();
 		if (now.after(endDate)) {
 			return null;
 		}
 		if (receivedMessage instanceof TextReceivedMessage) {
 			String incomingContent = ((TextReceivedMessage) receivedMessage)
 					.getContent();
 			if (FLAG.equals(incomingContent)) {
 				return newsSentContentStart;
 			}
 		}
 		if (receivedMessage instanceof ImageReceivedMessage) {
 			NewsSentContent newsSentContent = new NewsSentContent();
 			List<Article> articles = new ArrayList<Article>();
 			newsSentContent.setArticles(articles);
 			Article article = new Article();
 			articles.add(article);
 			article.setTitle(TITLE);
 			article.setDescription(DESCRIPTION);
 			article.setPicUrl(appProperties.getSiteBase() + PIC_URL);
 
 			String url = UriComponentsBuilder
 					.fromHttpUrl(appProperties.getSiteBase() + URL)
 					.queryParam("other", receivedMessage.getOther()).build()
 					.toUriString();
 			article.setUrl(url);
 
 			return newsSentContent;
 		}
 		return null;
 	}
 }
