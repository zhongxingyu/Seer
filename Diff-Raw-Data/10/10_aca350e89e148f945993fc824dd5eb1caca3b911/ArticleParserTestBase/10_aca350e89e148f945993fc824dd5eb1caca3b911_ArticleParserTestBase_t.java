 /*
  * ArticleParserTestBase.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.subitarius.instance.server.parse;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
 
 import java.beans.XMLDecoder;
 import java.io.InputStream;
 import java.util.List;
 
 import org.junit.runner.RunWith;
 
 import com.mycila.testing.junit.MycilaJunitRunner;
 import com.mycila.testing.plugin.guice.GuiceContext;
 import com.subitarius.domain.Article;
 import com.subitarius.domain.ArticleUrl;
 import com.subitarius.util.http.HttpModule;
 import com.subitarius.util.logging.TestLoggingModule;
 
 @RunWith(MycilaJunitRunner.class)
 @GuiceContext({ HttpModule.class, TestLoggingModule.class })
 public abstract class ArticleParserTestBase {
 	private final List<TestVector> testVectors;
 
 	@SuppressWarnings("unchecked")
 	protected ArticleParserTestBase() {
 		InputStream stream = getClass().getResourceAsStream(
 				getClass().getSimpleName() + ".xml");
 		XMLDecoder decoder = new XMLDecoder(stream);
 		testVectors = (List<TestVector>) decoder.readObject();
 	}
 
 	protected abstract ArticleParser getParser();
 
 	protected void testVector(int index) throws ArticleParseException {
 		TestVector vector = testVectors.get(index);
 		Article expected = vector.article;
 		Article actual = getParser().parse(new ArticleUrl(vector.url));
 
 		// if expected is null, we have to return out now to avoid NPE
 		if (expected == null) {
 			assertNull(actual);
 			return;
 		} else {
 			assertNotNull(actual);
 		}
 
 		// check each part individually so that we can see failures easily
 		assertEquals("title", expected.getTitle(), actual.getTitle());
 		assertEquals("byline", expected.getByline(), actual.getByline());
		// assertEquals("date", expected.getDate(), actual.getDate());
 
 		// check each paragraph individually as well
 		List<String> expectedText = expected.getParagraphs();
 		List<String> actualText = actual.getParagraphs();
 		assertEquals("paragraph count", expectedText.size(), actualText.size());
 		for (int i = 0; i < expectedText.size(); i++) {
 			assertEquals("paragraph " + i, expectedText.get(i),
 					actualText.get(i));
 		}
 		assertEquals("paragraphs", expectedText, actualText);
 
 		// just in case
		// assertEquals("Entire Object", expected, actual);
 	}
 }
