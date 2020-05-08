 package ch.opentrainingcenter.client.views.navigation.tree;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import org.junit.Before;
 import org.junit.Test;
import org.mockito.Matchers;
 
 import ch.opentrainingcenter.model.ModelFactory;
 import ch.opentrainingcenter.model.navigation.IKalenderWocheNavigationModel;
 import ch.opentrainingcenter.model.navigation.INavigationItem;
 import ch.opentrainingcenter.model.navigation.INavigationParent;
 import ch.opentrainingcenter.transfer.CommonTransferFactory;
 import ch.opentrainingcenter.transfer.IHealth;
 
 public class KalenderWocheTreeContentProviderTest {
     private KalenderWocheTreeContentProvider provider;
 
     @Before
     public void before() {
         provider = new KalenderWocheTreeContentProvider();
     }
 
     @Test
     public void testInputChanged() {
         final IKalenderWocheNavigationModel newInput = ModelFactory.createKwModel();
         provider.inputChanged(null, null, newInput);
 
         assertEquals(newInput, provider.getModel());
     }
 
     @Test
     public void testGetChildrenObject() {
         assertNull(provider.getChildren(new Object()));
     }
 
     @Test
     public void testGetChildrenINavigationParent() {
         final INavigationParent parent = ModelFactory.createNavigationParent();
         final IHealth health = CommonTransferFactory.createHealth(null, null, null, new Date());
         final INavigationItem item1 = ModelFactory.createConcreteHealth(health, null);
         parent.add(item1);
         final Object[] result = provider.getChildren(parent);
         assertEquals("Ein Child muss vorhanden sein", 1, result.length);
     }
 
     @Test
     public void testGetChildrenInteger() {
         final IKalenderWocheNavigationModel model = ModelFactory.createKwModel();
         final Calendar cal = Calendar.getInstance(Locale.GERMAN);
         cal.set(2013, 0, 01);
         final IHealth health = CommonTransferFactory.createHealth(null, null, null, cal.getTime());
         final INavigationItem item = ModelFactory.createConcreteHealth(health, null);
         model.addItem(item);
         provider.inputChanged(null, null, model);
 
         final Object[] result = provider.getChildren(Integer.valueOf(2013));
         assertEquals("Ein Child muss vorhanden sein", 1, result.length);
     }
 
     @Test
     public void testGetParent() {
        assertNull("Keine Parents", provider.getParent(Matchers.any()));
     }
 
     @Test
     public void testHasChildrenINavigationParent() {
         final INavigationParent element = ModelFactory.createNavigationParent();
         assertTrue("INavigationParent hat Kinder", provider.hasChildren(element));
     }
 
     @Test
     public void testHasChildrenINavigationInteger() {
         assertTrue("Integer hat Kinder", provider.hasChildren(Integer.valueOf(42)));
     }
 
     @Test
     public void testHasChildrenINavigationOther() {
         assertFalse("Anderes hat keine Kinder", provider.hasChildren(new Object()));
     }
 
     @Test
     public void testGetElements() {
         final IKalenderWocheNavigationModel model = ModelFactory.createKwModel();
         final Calendar cal = Calendar.getInstance(Locale.GERMAN);
         cal.set(2013, 0, 01);
         final IHealth health = CommonTransferFactory.createHealth(null, null, null, cal.getTime());
         final INavigationItem item = ModelFactory.createConcreteHealth(health, null);
         model.addItem(item);
         provider.inputChanged(null, null, model);
         provider.dispose();
         final Object[] result = provider.getElements(new Object());
         assertEquals("Ein Child muss vorhanden sein", 1, result.length);
     }
 }
