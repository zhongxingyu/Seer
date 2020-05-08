 package ch.opentrainingcenter.core.helper;
 
 import static org.junit.Assert.assertEquals;
 
 import java.awt.Color;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import ch.opentrainingcenter.core.helper.ZoneHelper.Zone;
 import ch.opentrainingcenter.transfer.IAthlete;
 
 public class ZoneHelperTest {

     private IPreferenceStore store;
     private ZoneHelper zoneHelper;
 
     @Before
     public void before() {
         store = Mockito.mock(IPreferenceStore.class);
         zoneHelper = new ZoneHelper(store);
     }
 
     @Test
     public void testZoneHelper() {
         Mockito.when(store.getString(Zone.RECOM.getName() + "_color")).thenReturn("255,254,23");
         final Color zonenFarbe = zoneHelper.getZonenFarbe(Zone.RECOM);
         final Color referenz = new Color(255, 254, 23);
         assertEqualsColor(zonenFarbe, referenz);
     }
 
     @Test
     public void testGetZonenFarbe() {
         Mockito.when(store.getInt(Zone.GA1.getName())).thenReturn(Integer.valueOf(42));
         final IAthlete athlete = Mockito.mock(IAthlete.class);
         Mockito.when(athlete.getMaxHeartRate()).thenReturn(Integer.valueOf(180));
         assertEquals(75.6, zoneHelper.getZonenWert(athlete, Zone.GA1), 0.00001);
     }
 
     @Test
     public void testGetZonenFarbeNotFound() {
         Mockito.when(store.getInt(Zone.GA1.getName())).thenReturn(Integer.valueOf(-42));
         final IAthlete athlete = Mockito.mock(IAthlete.class);
         Mockito.when(athlete.getMaxHeartRate()).thenReturn(Integer.valueOf(180));
         assertEquals(0, zoneHelper.getZonenWert(athlete, Zone.GA1), 0.00001);
     }
 
     private void assertEqualsColor(final Color zonenFarbe, final Color referenz) {
         assertEquals(referenz.getRed(), zonenFarbe.getRed());
         assertEquals(referenz.getGreen(), zonenFarbe.getGreen());
         assertEquals(referenz.getBlue(), zonenFarbe.getBlue());
     }
 }
