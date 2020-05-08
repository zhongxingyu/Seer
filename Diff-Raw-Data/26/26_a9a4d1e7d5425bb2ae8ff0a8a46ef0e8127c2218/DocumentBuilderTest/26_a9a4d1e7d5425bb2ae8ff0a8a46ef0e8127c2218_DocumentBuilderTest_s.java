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
				.withTitle("ίԱƴ4ļͲʾ")
 				.withContent(
						"9111㣬»ϢƣίϤίΡίǽΥͣĿǰ֯顣ίվ漰Ϣȫ")
 				.build();
 
 		Document doc = builder.parse(article);
 
 		List<Paragraph> paragraphs = doc.getParagraphs();
 		assertThat(paragraphs.size(), equalTo(1));
 		List<Sentence> sentences = paragraphs.get(0).getSentences();
 		assertThat(
 				sentences.get(0).getContent(),
				equalTo("9111㣬»ϢƣίϤίΡίǽΥͣĿǰ֯顣"));
 		assertThat(sentences.get(1).getContent(),
				equalTo("ίվ漰Ϣȫ"));
 	}
 
 }
