 /*
  * Copyright (c) 2012-2013, Dienst Landelijk Gebied - Ministerie van Economische Zaken
  * 
  * Gepubliceerd onder de BSD 2-clause licentie, 
  * zie https://github.com/MinELenI/CBSviewer/blob/master/LICENSE.md voor de volledige licentie.
  */
 package nl.mineleni.cbsviewer.servlet.wms;
 
 import static nl.mineleni.cbsviewer.servlet.AbstractBaseServlet.USER_ID;
 import static nl.mineleni.cbsviewer.servlet.AbstractBaseServlet.USER_PASSWORD;
 import static nl.mineleni.cbsviewer.util.StringConstants.REQ_PARAM_BGMAP;
 import static nl.mineleni.cbsviewer.util.StringConstants.REQ_PARAM_FGMAP_ALPHA;
 import static nl.mineleni.cbsviewer.util.StringConstants.REQ_PARAM_MAPID;
 import static nl.mineleni.cbsviewer.util.StringConstants.REQ_PARAM_STRAAL;
 import static nl.mineleni.cbsviewer.util.StringConstants.REQ_PARAM_XCOORD;
 import static nl.mineleni.cbsviewer.util.StringConstants.REQ_PARAM_YCOORD;
 import static org.junit.Assume.assumeNoException;
 
 import java.io.IOException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nl.mineleni.cbsviewer.util.StringConstants;
 
 import org.jmock.Expectations;
 import org.jmock.integration.junit4.JUnitRuleMockery;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test case for {@link nl.mineleni.cbsviewer.servlet.wms.WMSClientServlet}. dit
  * is een online/ integratie test omdat er met live sevices wordt
  * gecommuniceerd.
  * 
  * @author mprins
  */
 public class WMSClientServletIntegrationTest {
 	/** servlet die we testen. */
 	private WMSClientServlet servlet;
 	/** junit mockery. */
 	private final JUnitRuleMockery mockery = new JUnitRuleMockery();
 	/** mocked servlet request. */
 	private HttpServletRequest request;
 	/** ge-mockte servlet config gebruikt in de test. */
 	private ServletConfig servletConfig;
 	/** ge-mockte servlet context gebruikt in de test. */
 	private ServletContext servletContext;
 	/** ge-mockte servlet response gebruikt in de test. */
 	private HttpServletResponse response;
 
 	/**
 	 * Set up before each test.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@SuppressWarnings("serial")
 	@Before
 	public void setUp() throws Exception {
 		this.request = this.mockery.mock(HttpServletRequest.class);
 		this.servletConfig = this.mockery.mock(ServletConfig.class);
 		this.servletContext = this.mockery.mock(ServletContext.class);
 		this.response = this.mockery.mock(HttpServletResponse.class);
 
 		this.servlet = new WMSClientServlet() {
 			/** return de mocked servletContext. */
 			@Override
 			public ServletContext getServletContext() {
 				return WMSClientServletIntegrationTest.this.servletContext;
 			}
 
 			/** return de mocked servletConfig. */
 			@Override
 			public ServletConfig getServletConfig() {
 				return WMSClientServletIntegrationTest.this.servletConfig;
 			}
 		};
 
 		this.mockery.checking(new Expectations() {
 			{
 				this.atLeast(1)
 						.of(WMSClientServletIntegrationTest.this.servletContext)
 						.getRealPath(StringConstants.MAP_CACHE_DIR.code);
 				this.will(returnValue("C:/workspace/CBSviewer/target/"
 						+ StringConstants.MAP_CACHE_DIR.code));
 				this.oneOf(WMSClientServletIntegrationTest.this.servletConfig)
 						.getInitParameter(USER_ID);
 				this.will(returnValue("userID"));
 				this.oneOf(WMSClientServletIntegrationTest.this.servletConfig)
 						.getInitParameter(USER_PASSWORD);
 				this.will(returnValue("passID"));
 
 				this.oneOf(WMSClientServletIntegrationTest.this.servletConfig)
 						.getInitParameter("bgCapabilitiesURL");
 				this.will(returnValue("http://osm.wheregroup.com/cgi-bin/osm_basic.xml?REQUEST=GetCapabilities&SERVICE=WMS&amp;VERSION=1.1.1"));
 				this.oneOf(WMSClientServletIntegrationTest.this.servletConfig)
 						.getInitParameter("bgWMSlayers");
 				this.will(returnValue("Grenzen"));
 				this.oneOf(WMSClientServletIntegrationTest.this.servletConfig)
 						.getInitParameter("lufoCapabilitiesURL");
 				this.will(returnValue("http://gisdemo2.agro.nl/arcgis/services/Luchtfoto2010/MapServer/WMSServer?REQUEST=GetCapabilities&SERVICE=WMS"));
 				this.oneOf(WMSClientServletIntegrationTest.this.servletConfig)
 						.getInitParameter("lufoWMSlayers");
 				this.will(returnValue(""));
				this.oneOf(WMSClientServletIntegrationTest.this.servletConfig)
						.getInitParameter("featureInfoType");
				this.will(returnValue(""));
 			}
 		});
 		// init servlet
 		this.servlet.init(this.servletConfig);
 	}
 
 	/**
 	 * Tear down after each test.
 	 * 
 	 * @throws Exception
 	 *             the exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 		this.servlet.destroy();
 	}
 
 	/**
 	 * Test methode voor
 	 * {@link nl.mineleni.cbsviewer.servlet.wms.WMSClientServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
 	 * .
 	 * 
 	 * @throws ServletException
 	 *             the servlet exception
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	@Test
 	// @Ignore("needs more work")
 	public void testServiceHttpServletRequestHttpServletResponse() {
 
 		this.mockery.checking(new Expectations() {
 			{
 				this.allowing(WMSClientServletIntegrationTest.this.request)
 						.getAttributeNames();
 				this.allowing(WMSClientServletIntegrationTest.this.request)
 						.getParameterMap();
 
 				this.allowing(WMSClientServletIntegrationTest.this.request)
 						.getParameter(REQ_PARAM_XCOORD.code);
 				this.will(returnValue("143542"));
 				this.allowing(WMSClientServletIntegrationTest.this.request)
 						.getParameter(REQ_PARAM_YCOORD.code);
 				this.will(returnValue("453977"));
 				this.allowing(WMSClientServletIntegrationTest.this.request)
 						.getParameter(REQ_PARAM_STRAAL.code);
 				this.will(returnValue("3000"));
 
 				this.allowing(WMSClientServletIntegrationTest.this.request)
 						.getParameter(REQ_PARAM_BGMAP.code);
 				this.will(returnValue("topografie"));
 
 				this.allowing(WMSClientServletIntegrationTest.this.request)
 						.getParameter(REQ_PARAM_FGMAP_ALPHA.code);
 				this.will(returnValue(".7"));
 
 				this.allowing(WMSClientServletIntegrationTest.this.request)
 						.getParameter(REQ_PARAM_MAPID.code);
 				this.will(returnValue("wijkenbuurten2011_thema_gemeenten2011_aantal_inwoners"));
 
 				this.allowing(WMSClientServletIntegrationTest.this.request)
 						.setAttribute(this.with(any(String.class)),
 								this.with(any(Object.class)));
 			}
 		});
 
 		try {
 			this.servlet.service(this.request, this.response);
 		} catch (final ServletException | IOException e) {
 			assumeNoException(e);
 		}
 		// cant't do this
 		// assertNotNull(this.request.getAttribute(REQ_PARAM_MAPID.code));
 		// assertNotNull(this.request.getAttribute(REQ_PARAM_LEGENDAS.code));
 		// assertNotNull(this.request.getAttribute(REQ_PARAM_FEATUREINFO.code));
 		// assertNotNull(this.request.getAttribute(REQ_PARAM_DOWNLOADLINK.code));
 	}
 
 }
