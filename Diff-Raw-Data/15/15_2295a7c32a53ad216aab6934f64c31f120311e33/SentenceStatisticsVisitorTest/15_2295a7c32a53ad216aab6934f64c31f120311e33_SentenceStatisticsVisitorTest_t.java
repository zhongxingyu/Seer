 package com.weieditor.mds;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import com.weieditor.mds.model.Article;
 import com.weieditor.mds.model.Document;
 import com.weieditor.mds.model.MultiDocument;
 import com.weieditor.mds.model.Paragraph;
 import com.weieditor.mds.model.Sentence;
 import com.weieditor.mds.visitor.SentenceStatisticsVisitor;
 import com.weieditor.mds.visitor.WordStatisticsVisitor;
 
 public class SentenceStatisticsVisitorTest {
 
 	@Test
 	public void should_calculate_sentence_weight() {
 		WordStatisticsVisitor wordVisitor = new WordStatisticsVisitor();
 		SentenceStatisticsVisitor sentenceVisitor = new SentenceStatisticsVisitor(
 				wordVisitor);
 		ArticleBuilder articleBuilder = new ArticleBuilder();
 		Article article = articleBuilder.withContent(
 				"李克强:新型城镇化要真正惠及农民\n要让新型城镇化路子走好走顺").build();
 		Document doc = new DocumentParserImpl(new IctclasSegmenter())
 				.parse(article);
 		MultiDocument multiDoc = new MultiDocument();
 		multiDoc.add(doc);
 
 		multiDoc.accept(wordVisitor);
 		multiDoc.accept(sentenceVisitor);
 
		Sentence sentence1 = new Sentence(new Paragraph(doc, 1), 1);
		double weight1 = sentenceVisitor.getSentenceWeight(sentence1);
		assertTrue(weight1 > 0.0);

		Sentence sentence2 = new Sentence(new Paragraph(doc, 2), 1);
		double weight2 = sentenceVisitor.getSentenceWeight(sentence2);
		assertTrue(weight2 > 0.0);
 	}
 
 }
