 package com.itjenny.domain;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import com.itjenny.common.CommonTest;
 import com.itjenny.support.Const;
 
 public class HtmlArticleTest extends CommonTest {
 	@Test
 	public void testSection() {
 		// Given
 		String content = "# title0\nthis is first\nthis is second\n# title1\nthis is first\nthis is second.";
 
 		// When
		HtmlArticle htmlArticle = new HtmlArticle("", content);
 
 		// Then
 		assertThat(htmlArticle.getChapters().size(), is(1));
 		List<Section> sections = htmlArticle.getChapters().get(0).getSections();
 		for (int i = 0; i < htmlArticle.getChapters().size(); i++) {
 			assertThat(sections.get(i).getSubtitle(), is("title" + i));
 			assertThat(sections.get(i).getContent(), containsString("this is first"));
 			assertThat(sections.get(i).getCss(), is(Const.CSS[i % Const.CSS.length]));
 		}
 	}
 	
 	@Test
 	public void testQuiz() {
 		// Given
 		String content = "# QUIZ\nquestion1\n>answer1\n";
 
 		// When
		HtmlArticle htmlArticle = new HtmlArticle("", content);
 
 		// Then
 		assertThat(htmlArticle.getChapters().size(), is(1));
 		List<Chapter> chapters = htmlArticle.getChapters();
 		for (int i = 0; i < htmlArticle.getChapters().size(); i++) {
 			assertThat(chapters.get(i).getQuiz().getSubtitle(), is(Const.QUIZ));
 			assertThat(chapters.get(i).getQuiz().getContent(), containsString("question1"));
 			assertThat(chapters.get(i).getQuiz().getCss(), is(Const.CSS[i % Const.CSS.length]));
 			assertThat(chapters.get(i).getQuiz().getAnswer(), is("answer1"));
 		}
 	}
 
 	@Test
 	public void testChapter() {
 		// Given
 		String content = "# title0\nthis is first\nthis is second\n# title1\nthis is first\nthis is second.\n# QUIZ\nquestion1\n>answer1\n";
 
 		// When
		HtmlArticle htmlArticle = new HtmlArticle("", content);
 
 		// Then
 		assertThat(htmlArticle.getChapters().size(), is(1));
 	}
 
 	@Test
 	public void testChapter2() {
 		// Given
 		String content = "# title0\nthis is first\nthis is second\n# title1\nthis is first\nthis is second.\n# QUIZ\nquestion1\n>answer1\n";
 		String content2 = content + content;
 
 		// When
		HtmlArticle htmlArticle = new HtmlArticle("", content2);
 
 		// Then
 		assertThat(htmlArticle.getChapters().size(), is(2));
 	}
 }
