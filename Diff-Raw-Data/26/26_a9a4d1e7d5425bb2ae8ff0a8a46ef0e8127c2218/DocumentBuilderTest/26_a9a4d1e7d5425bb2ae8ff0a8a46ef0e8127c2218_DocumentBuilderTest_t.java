 package com.weieditor.mds;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import com.weieditor.mds.model.Article;
 import com.weieditor.mds.model.Document;
 import com.weieditor.mds.model.Paragraph;
 import com.weieditor.mds.model.Sentence;
 
 public class DocumentBuilderTest {
 
 	@Test
 	public void should_parse_article() {
 		DocumentBuilder builder = new DocumentBuilder();
 		ArticleBuilder articleBuilder = new ArticleBuilder();
 		Article article = articleBuilder
				.withTitle("国资委官员称从4月起文件批复就不再请示蒋洁敏")
 				.withContent(
						"9月1日上午11点，新华网发布消息称，从中央纪委获悉，国资委主任、党委副书记蒋洁敏涉嫌严重违纪，目前正接受组织调查。当晚，国资委将其网站上所有涉及蒋的信息全部撤除。")
 				.build();
 
 		Document doc = builder.parse(article);
 
 		List<Paragraph> paragraphs = doc.getParagraphs();
 		assertThat(paragraphs.size(), equalTo(1));
 		List<Sentence> sentences = paragraphs.get(0).getSentences();
 		assertThat(
 				sentences.get(0).getContent(),
				equalTo("9月1日上午11点，新华网发布消息称，从中央纪委获悉，国资委主任、党委副书记蒋洁敏涉嫌严重违纪，目前正接受组织调查。"));
 		assertThat(sentences.get(1).getContent(),
				equalTo("当晚，国资委将其网站上所有涉及蒋的信息全部撤除。"));
 	}
 
 }
