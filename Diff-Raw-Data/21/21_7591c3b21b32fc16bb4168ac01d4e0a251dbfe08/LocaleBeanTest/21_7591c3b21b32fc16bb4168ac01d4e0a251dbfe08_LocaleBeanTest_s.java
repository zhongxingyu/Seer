 package org.cmpa.bean;
 
import com.bean.common.LocaleBean;
 import java.util.Locale;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import org.easymock.EasyMock;
 import org.jboss.test.faces.mock.MockFacesEnvironment;
 import org.junit.After;
 import static org.junit.Assert.assertEquals;
 import org.junit.Before;
 import org.junit.Test;
 
 public class LocaleBeanTest
 {
 
 	private static final String ENGLISH = "en";
 	private static final String FRENCH = "fr";
 	private MockFacesEnvironment environment;
 	private FacesContext facesContext;
 	private UIViewRoot viewRoot;
 
 	@Before
 	public void setUp() throws Exception
 	{
 		//create mock environment
 		environment = MockFacesEnvironment.createEnvironment();
 		facesContext = environment.getFacesContext();
 		viewRoot = environment.createMock(UIViewRoot.class);
 		EasyMock.expect(facesContext.getViewRoot()).andReturn(viewRoot).anyTimes();
 	}
 
 	@After
 	public void tearDown() throws Exception
 	{
 		//do verification for mocked objects
 		environment.verify();
 
 		//do clean up
 		environment.release();
 		environment = null;
 
 	}
 
 	@Test
 	public void testBrowsersDefaultLanguageIsFrench()
 	{
 		//record interaction with mock objects
 		EasyMock.expect(viewRoot.getLocale()).andReturn(new Locale(FRENCH)).anyTimes();
 		//start replay
 		environment.replay();
 		LocaleBean localeBean = new LocaleBean();
 		//have to force the call to the @PostConstruct because jsf-mock does not do it for us
 		localeBean.initialize();
 		assertEquals(FRENCH, localeBean.getLanguage());
 	}
 
 	@Test
 	public void testSwapLanguagesStartingWithEnglish()
 	{
 		//record interaction with mock objects
 		EasyMock.expect(viewRoot.getLocale()).andReturn(new Locale(ENGLISH)).anyTimes();
 		viewRoot.setLocale(new Locale(FRENCH));
 		//start replay
 		environment.replay();
 		LocaleBean localeBean = new LocaleBean();
 		//have to force the call to the @PostConstruct because jsf-mock does not do it for us
 		localeBean.initialize();
 		localeBean.swapLocale();
 		assertEquals(FRENCH, localeBean.getLanguage());
 	}
 
 	@Test
 	public void testSwapLanguagesStartingWithFrench()
 	{
 		//record interaction with mock objects
 		EasyMock.expect(viewRoot.getLocale()).andReturn(new Locale(FRENCH)).anyTimes();
 		viewRoot.setLocale(new Locale(ENGLISH));
 		//start replay
 		environment.replay();
 		LocaleBean localeBean = new LocaleBean();
 		//have to force the call to the @PostConstruct because jsf-mock does not do it for us
 		localeBean.initialize();
 		localeBean.swapLocale();
 		assertEquals(ENGLISH, localeBean.getLanguage());
 	}
 }
