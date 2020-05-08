 /*
  * AlJazeeraArticleParserTest.java
  * Copyright (C) 2011 Ty Overby
  * All rights reserved.
  */
 
 package com.subitarius.instance.server.parse;
 
 import org.junit.Test;
 
 import com.google.inject.Inject;
 
 public final class AlJazeeraArticleParserTest extends ArticleParserTestBase {
 	@Inject
 	private AlJazeeraArticleParser parser;
 
 	@Override
 	protected ArticleParser getParser() {
 		return parser;
 	}
 
 	@Test
 	public void testParse() throws ArticleParseException {
 		testVector(0);
 	}
 }
