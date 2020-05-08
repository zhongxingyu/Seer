 package ch.opentrainingcenter.client.commands;
 
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

 import org.eclipse.jface.preference.IPreferenceStore;
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.opentrainingcenter.client.views.ApplicationContext;
 import ch.opentrainingcenter.core.PreferenceConstants;
 import ch.opentrainingcenter.core.db.DBSTATE;
 
 @SuppressWarnings("nls")
 public class OtcAbstractHandlerTest {
 
     private OtcAbstractHandler handler;
     private IPreferenceStore store;
 
     @Before
     public void before() {
         store = mock(IPreferenceStore.class);
        handler = new AddRoute(store);
     }
 
     @Test
     public void testAthleteNotFound() {
         when(store.getString(PreferenceConstants.ATHLETE_ID)).thenReturn(null);
         assertFalse(handler.isEnabled());
     }
 
     @Test
     public void testAthleteFound() {
         ApplicationContext.getApplicationContext().setDbState(DBSTATE.OK);
         when(store.getString(PreferenceConstants.ATHLETE_ID)).thenReturn("42");
         assertTrue(handler.isEnabled());
     }
 
     @Test
     public void testAthleteFoundButDatabaseNotOk() {
         ApplicationContext.getApplicationContext().setDbState(DBSTATE.PROBLEM);
         when(store.getString(PreferenceConstants.ATHLETE_ID)).thenReturn("42");
         assertFalse("Da DB nicht ok ist", handler.isEnabled());
     }
 }
