 package com.gentics.cr.analytics.language.test;
 
 import java.io.File;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.gentics.DefaultTestConfiguration;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.configuration.EnvironmentConfiguration;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.lucene.indexer.transformer.ContentTransformer;
 import com.gentics.cr.lucene.indexer.transformer.lang.LanguageIdentifyer;
 
 public class LanguageIdentifyerTest {
 
 	CRResolvableBean bean;
 	GenericConfiguration config;
 	ContentTransformer t;
 
 	@Before
 	public void init() throws Exception {
		EnvironmentConfiguration.setConfigPath(new File(DefaultTestConfiguration.class.getResource("conf/gentics").getFile()).getAbsolutePath());
 		EnvironmentConfiguration.loadEnvironmentProperties();
 		bean = new CRResolvableBean();
 
 		config = new GenericConfiguration();
 		config.set("attribute", "content");
 		config.set("langattribute", "lang");
 		t = new LanguageIdentifyer(config);
 	}
 
 	@Test
 	public void testTransformerDE() throws Exception {
 
 		bean.set("content", "Dies ist ein Test um den LanguageDetector auf" + " eine Sprache zu bringen");
 		t.processBean(bean);
 		String s = (String) bean.get("lang");
 		Assert.assertEquals("The found Language (" + s + ") should be (de).", "de", s);
 	}
 
 	@Test
 	public void testTransformerEN() throws Exception {
 		bean.set("content", "This is some text to set the LanguageDetector" + "to a language.");
 		t.processBean(bean);
 		String s = (String) bean.get("lang");
 
 		Assert.assertEquals("The found Language (" + s + ") should be (en).", "en", s);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 
 	}
 }
