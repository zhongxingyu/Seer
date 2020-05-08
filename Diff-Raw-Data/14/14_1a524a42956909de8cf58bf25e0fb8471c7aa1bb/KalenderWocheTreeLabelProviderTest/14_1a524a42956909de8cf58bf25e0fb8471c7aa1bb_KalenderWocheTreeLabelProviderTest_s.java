 package ch.opentrainingcenter.client.views.navigation.tree;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Calendar;
 import java.util.Locale;
 
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.opentrainingcenter.model.ModelFactory;
 import ch.opentrainingcenter.model.navigation.INavigationItem;
 import ch.opentrainingcenter.model.navigation.INavigationParent;
 import ch.opentrainingcenter.transfer.CommonTransferFactory;
 import ch.opentrainingcenter.transfer.IHealth;
 
 public class KalenderWocheTreeLabelProviderTest {
 
     private KalenderWocheTreeLabelProvider provider;
 
     @Before
     public void before() {
         provider = new KalenderWocheTreeLabelProvider();
     }
 
     @Test
     public void testGetTextNoElement() {
         final INavigationParent element = ModelFactory.createNavigationParent();
         final String result = provider.getText(element);
         assertEquals("KW", result);
     }
 
     @Test
     public void testGetTextElement() {
         final INavigationParent element = ModelFactory.createNavigationParent();
         final Calendar cal = Calendar.getInstance(Locale.GERMAN);
         cal.set(2013, 0, 01);
         final IHealth health = CommonTransferFactory.createHealth(null, null, null, cal.getTime());
         final INavigationItem item = ModelFactory.createConcreteHealth(health, null);
         element.add(item);
         final String result = provider.getText(element);
         assertEquals("KW1", result);
     }
 
     @Test
     public void testGetTextElementInteger() {
         final String jahr = "2013";
         final Integer element = Integer.valueOf(jahr);
 
         final String result = provider.getText(element);
         assertEquals("Das Jahr wird dargestellt", jahr, result);
     }
 
     @Test
     public void testGetTextElementINavigationItem() {
         final Calendar cal = Calendar.getInstance(Locale.GERMAN);
         cal.set(2013, 11, 25);
         final IHealth health = CommonTransferFactory.createHealth(null, null, null, cal.getTime());
         final INavigationItem item = ModelFactory.createConcreteHealth(health, null);
 
         final String result = provider.getText(item);
         assertEquals("Das Jahr wird dargestellt", "25.12.2013", result);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testGetTextElementException() {
         provider.getText(Boolean.TRUE);
     }
 
     @Test
     public void testGetImageObjectINavigationParent() {
         final INavigationParent element = ModelFactory.createNavigationParent();
         final Image result = provider.getImage(element);
         assertEquals(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER), result);
     }
 
     @Test
     public void testGetImageObjectInteger() {
         final Image result = provider.getImage(Integer.valueOf(42));
         assertEquals(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER), result);
     }
 
     @Test
     public void testGetImageObjectOther() {
         final Image result = provider.getImage(Boolean.FALSE);
         assertEquals(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE), result);
     }
 }
