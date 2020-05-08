 package org.eclipse.rmf.tests.reqif10.common;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.eclipse.rmf.reqif10.ReqIF10Factory;
 import org.eclipse.rmf.reqif10.XhtmlContent;
 import org.eclipse.rmf.reqif10.common.util.ReqIF10XHTMLContentAdapter;
 import org.eclipse.rmf.reqif10.common.util.ReqIF10XHTMLContentAdapterFactory;
 import org.eclipse.rmf.reqif10.xhtml.XhtmlDivType;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ReqIF10XHTMLContentAdapterTests {
 	ReqIF10XHTMLContentAdapterFactory factory = new ReqIF10XHTMLContentAdapterFactory();
 
 	@Before
 	public void setUp() {
 		// EPackage.Registry.INSTANCE.put(XhtmlPackage.eNS_URI, XhtmlPackage.eINSTANCE);
 	}
 
 	@Test
 	public void testSetXhtmlContentAdapterFactory() {
 		XhtmlContent xhtmlContent = getEmptyXhtmlContent();
 		Object adapterObject = factory.adapt(xhtmlContent, ReqIF10XHTMLContentAdapter.class);
 		assertNotNull(adapterObject);
 		System.out.println(adapterObject);
 		assertTrue(adapterObject instanceof ReqIF10XHTMLContentAdapter);
 
 		Object adapterObject2 = factory.adapt(xhtmlContent, ReqIF10XHTMLContentAdapter.class);
 		assertSame(adapterObject, adapterObject2);
 	}
 
 	@Test
 	public void testXhtmlStringNull() {
 		XhtmlContent xhtmlContent = getEmptyXhtmlContent();
 		ReqIF10XHTMLContentAdapter adapter = (ReqIF10XHTMLContentAdapter) factory.adapt(xhtmlContent, ReqIF10XHTMLContentAdapter.class);
 		try {
 			adapter.setXhtmlString(null);
 		} catch (IOException ioe) {
 			assertTrue(false);
 		}
 		assertNull(adapter.getXhtmlString());
 		assertFalse(xhtmlContent.isSetXhtml());
 		assertNull(xhtmlContent.getXhtml());
 	}
 
 	@Test
 	public void testXhtmlStringEmpty() {
 		XhtmlContent xhtmlContent = getEmptyXhtmlContent();
 		ReqIF10XHTMLContentAdapter adapter = (ReqIF10XHTMLContentAdapter) factory.adapt(xhtmlContent, ReqIF10XHTMLContentAdapter.class);
 		try {
 			adapter.setXhtmlString("");
 			assertTrue(false);
 		} catch (IOException ioe) {
 
 		}
 		assertEquals(null, adapter.getXhtmlString());
 		assertFalse(xhtmlContent.isSetXhtml());
 		assertEquals(1, adapter.getErrors().size());
 	}
 
 	@Test
 	public void testXhtmlStringDiv() {
 		XhtmlContent xhtmlContent = getEmptyXhtmlContent();
 		ReqIF10XHTMLContentAdapter adapter = (ReqIF10XHTMLContentAdapter) factory.adapt(xhtmlContent, ReqIF10XHTMLContentAdapter.class);
 		try {
 			adapter.setXhtmlString("<div xmlns=\"http://www.w3.org/1999/xhtml\">hello</div>"); //$NON-NLS-1$
 		} catch (IOException ioe) {
 			assertTrue(false);
 		}
 		assertEquals("<div xmlns=\"http://www.w3.org/1999/xhtml\">hello</div>\n", adapter.getXhtmlString());
 		assertTrue(xhtmlContent.isSetXhtml());
 		assertEquals(0, adapter.getErrors().size());
 		assertTrue(xhtmlContent.getXhtml() instanceof XhtmlDivType);
 	}
 
 	@Test
 	public void testXhtmlStringDivNoNamespace() {
 		XhtmlContent xhtmlContent = getEmptyXhtmlContent();
 		ReqIF10XHTMLContentAdapter adapter = (ReqIF10XHTMLContentAdapter) factory.adapt(xhtmlContent, ReqIF10XHTMLContentAdapter.class);
 		try {
 			adapter.setXhtmlString("<div>hello</div>");
 		} catch (IOException ioe) {
 			assertTrue(false);
 		}
 		assertEquals("<div xmlns=\"http://www.w3.org/1999/xhtml\">hello</div>\n", adapter.getXhtmlString());
 		assertTrue(xhtmlContent.isSetXhtml());
 		assertEquals(0, adapter.getErrors().size());
 		assertTrue(xhtmlContent.getXhtml() instanceof XhtmlDivType);
 	}
 
 	XhtmlContent getEmptyXhtmlContent() {
 		return ReqIF10Factory.eINSTANCE.createXhtmlContent();
 	}
 
 }
