 package net.vidageek.autoweb.taglib.core;
 
 import static org.mockito.Matchers.contains;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import net.vidageek.autoweb.taglib.support.ResourceVersionIdHandler;
 import net.vidageek.autoweb.taglib.support.TagEnvironment;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 final public class JsTagCoreTest {
 
 	@Mock
 	public TagEnvironment env;
 
 	@Before
 	public void setup() {
 		MockitoAnnotations.initMocks(this);
 	}
 
 	@Test
 	public void testThatAddsContextPath() {
 		when(env.contextPath()).thenReturn("/context");
 
 		new JsTagCore("").applyTo(env);
 
 		verify(env).write(contains("/context"));
 	}
 
 	@Test
 	public void testThatAddsResourceVersionToInvalidateCache() {
 
 		new JsTagCore("").applyTo(env);
 
 		verify(env).write(contains("autowebResourceVersion=" + ResourceVersionIdHandler.getId()));
 	}
 
 	@Test
 	public void testThatCreatesCompleteJsTagWithSimpleUrl() {
 		when(env.contextPath()).thenReturn("/");
 
 		new JsTagCore("/js/test.js").applyTo(env);
 
 		verify(env).write(	"<script type=\"text/javascript\" src=\"/js/test.js?autowebResourceVersion="
									+ ResourceVersionIdHandler.getId() + "\" ></script>");
 	}
 
 	@Test
 	public void testThatCreatesCompleteCssTagWithComplexUrl() {
 		when(env.contextPath()).thenReturn("/");
 
 		new JsTagCore("/js/test.js?a=b").applyTo(env);
 
 		verify(env).write(	"<script type=\"text/javascript\" src=\"/js/test.js?a=b&autowebResourceVersion="
									+ ResourceVersionIdHandler.getId() + "\" ></script>");
 	}
 }
