 package org.cmpa.bean;
 
 import java.util.Locale;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import org.easymock.EasyMock;
 import static org.junit.Assert.assertEquals;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
import org.cmpa.web.ResourceBundleMap;

 @RunWith(PowerMockRunner.class)
 @PrepareForTest(
 {
 	FacesContext.class
 })
 public class PropertyBeanTest
 {
 	
 	private FacesContext faces;
 	private UIViewRoot root;
 	
 	@Before
 	public void setUp() throws Exception
 	{
 		PowerMock.mockStatic(FacesContext.class);
 
 		
 		faces = PowerMock.createMock(FacesContext.class);
 		EasyMock.expect(FacesContext.getCurrentInstance()).andReturn(faces);
 		root = EasyMock.createMock(UIViewRoot.class);
 		EasyMock.expect(faces.getViewRoot()).andReturn(root).anyTimes();
 		EasyMock.expect(root.getLocale()).andReturn(Locale.ENGLISH).anyTimes();
 	}
 	
 	@Test(expected = NullPointerException.class)
 	public void testSaveNonExistentResourceBundle()
 	{
 		PowerMock.replayAll(faces, root);
 		PropertyBean propertyBean = new PropertyBean();
 		propertyBean.setBundle("nonexistent.bundle");
 		propertyBean.setContent("content");
 		propertyBean.setProperty("property");
 		propertyBean.save();
 	}
 	
 	@Test
 	public void testSaveNonExistentProperty()
 	{
 		PowerMock.replayAll(faces, root);
 		PropertyBean propertyBean = new PropertyBean();
 		propertyBean.setBundle("pages.index.propertyBean");
 		propertyBean.setContent("content");
 		propertyBean.setProperty("nonexistent.bundle");
 		propertyBean.save();
 
 		//TODO do we expect it to save new properties?
 		ResourceBundleMap rbm = new ResourceBundleMap("pages.index.propertyBean", Locale.ENGLISH);
 		String result = (String) rbm.get("nonexistent.bundle");
 		assertEquals("content", result);
 	}
 	
 	@Test
 	public void testSavingUpdatedContent()
 	{
 		PowerMock.replayAll(faces, root);
 		PropertyBean propertyBean = new PropertyBean();
 		propertyBean.setBundle("pages.index.propertyBean");
 		propertyBean.setContent("updated content");
 		propertyBean.setProperty("section1.txt");
 		propertyBean.save();
 		
 		ResourceBundleMap rbm = new ResourceBundleMap("pages.index.propertyBean", Locale.ENGLISH);
 		String result = (String) rbm.get("section1.txt");
 		assertEquals("updated content", result);
 	}
 	
 	@Test
 	public void testSavingFrenchPropertiesFile()
 	{
 		EasyMock.reset(root);
 		EasyMock.expect(root.getLocale()).andReturn(Locale.FRENCH).anyTimes();
 		
 		PowerMock.replayAll(faces, root);
 		ResourceBundleMap rbm = new ResourceBundleMap("pages.index.propertyBean", Locale.FRENCH);
 		
 		PropertyBean propertyBean = new PropertyBean();
 		propertyBean.setBundle("pages.index.propertyBean");
 		propertyBean.setContent((String) rbm.get("section1.txt"));
 		propertyBean.setProperty("section1.txt");
 		propertyBean.save();
 		
 		rbm = new ResourceBundleMap("pages.index.propertyBean", Locale.FRENCH);
 		String result = (String) rbm.get("section1.txt");
 		assertEquals("test text french", result);
 	}
 	
 	@Test
 	public void testFrenchCharacters()
 	{
 		EasyMock.reset(root);
 		EasyMock.expect(root.getLocale()).andReturn(Locale.FRENCH).anyTimes();
 		PowerMock.replayAll(faces, root);
 		PropertyBean propertyBean = new PropertyBean();
 		propertyBean.setBundle("pages.index.propertyBean");
 		propertyBean.setContent("éâäàåçêëèïîìæÄÅÉÆôöòùÿÖÜ¢");
 		propertyBean.setProperty("section1.txt");
 		propertyBean.save();
 		
 		ResourceBundleMap rbm = new ResourceBundleMap("pages.index.propertyBean", Locale.FRENCH);
 		String result = (String) rbm.get("section1.txt");
 		assertEquals("éâäàåçêëèïîìæÄÅÉÆôöòùÿÖÜ¢", result);
 	}
 }
