 package ch.opentrainingcenter.db;
 
import static org.junit.Assert.assertEquals;

 import org.junit.Test;
 import org.mockito.Mockito;
 
 import ch.opentrainingcenter.core.db.DatabaseHelper;
import ch.opentrainingcenter.core.db.DatabaseHelper.DBSTATE;
 import ch.opentrainingcenter.core.db.IDatabaseAccess;
 import ch.opentrainingcenter.transfer.IAthlete;
 
 @SuppressWarnings("nls")
 public class DatabaseHelperTest {
     @Test
     public void testDbExistingOk() {
         final IDatabaseAccess databaseAccess = Mockito.mock(IDatabaseAccess.class);
         final IAthlete athlete = Mockito.mock(IAthlete.class);
         Mockito.when(databaseAccess.getAthlete(1)).thenReturn(athlete);
         assertEquals("Datenbank existiert, wenn athlete gefunden wird", true, DatabaseHelper.isDatabaseExisting(databaseAccess));
     }
 
     @Test
     public void testDbExistingNOk() {
         final IDatabaseAccess databaseAccess = Mockito.mock(IDatabaseAccess.class);
         final RuntimeException exception = Mockito.mock(RuntimeException.class);
         final Throwable throwable = Mockito.mock(Throwable.class);
         Mockito.when(exception.getCause()).thenReturn(throwable);
         Mockito.when(throwable.getMessage()).thenReturn("Andere Meldung");
         Mockito.when(databaseAccess.getAthlete(1)).thenThrow(exception);
         assertEquals("Datenbank existiert", true, DatabaseHelper.isDatabaseExisting(databaseAccess));
     }
 
     @Test
     public void testDbExistingMEssageNull() {
         final IDatabaseAccess databaseAccess = Mockito.mock(IDatabaseAccess.class);
         final RuntimeException exception = Mockito.mock(RuntimeException.class);
         final Throwable throwable = Mockito.mock(Throwable.class);
         Mockito.when(exception.getCause()).thenReturn(throwable);
         Mockito.when(throwable.getMessage()).thenReturn(null);
         Mockito.when(databaseAccess.getAthlete(1)).thenThrow(exception);
         assertEquals("Datenbank existiert", true, DatabaseHelper.isDatabaseExisting(databaseAccess));
     }
 
     @Test
     public void testDbExistingNOTExisting() {
         final IDatabaseAccess databaseAccess = Mockito.mock(IDatabaseAccess.class);
         final RuntimeException exception = Mockito.mock(RuntimeException.class);
         final Throwable throwable = Mockito.mock(Throwable.class);
         Mockito.when(exception.getCause()).thenReturn(throwable);
         Mockito.when(throwable.getMessage()).thenReturn("Table \"ATHLETE\" not found; SQL statement:");
         Mockito.when(databaseAccess.getAthlete(1)).thenThrow(exception);
         assertEquals("Datenbank existiert nicht", false, DatabaseHelper.isDatabaseExisting(databaseAccess));
     }
 
     @Test
     public void testDbLockedOk() {
         final IDatabaseAccess databaseAccess = Mockito.mock(IDatabaseAccess.class);
         final IAthlete athlete = Mockito.mock(IAthlete.class);
         Mockito.when(databaseAccess.getAthlete(1)).thenReturn(athlete);
        assertEquals("Datenbank nicht gelockt, wenn athlete gefunden wird", DBSTATE.OK, DatabaseHelper.getDatabaseState(databaseAccess));
     }
 
     @Test
     public void testDbLockedNOk() {
         final IDatabaseAccess databaseAccess = Mockito.mock(IDatabaseAccess.class);
         final RuntimeException exception = Mockito.mock(RuntimeException.class);
         final Throwable throwable = Mockito.mock(Throwable.class);
         Mockito.when(exception.getCause()).thenReturn(throwable);
         Mockito.when(throwable.getMessage()).thenReturn("Andere Meldung");
         Mockito.when(databaseAccess.getAthlete(1)).thenThrow(exception);
        assertEquals("Datenbank nicht gelockt", DBSTATE.PROBLEM, DatabaseHelper.getDatabaseState(databaseAccess));
     }
 
     @Test
     public void testDbLockedMEssageNull() {
         final IDatabaseAccess databaseAccess = Mockito.mock(IDatabaseAccess.class);
         final RuntimeException exception = Mockito.mock(RuntimeException.class);
         final Throwable throwable = Mockito.mock(Throwable.class);
         Mockito.when(exception.getCause()).thenReturn(throwable);
         Mockito.when(throwable.getMessage()).thenReturn(null);
         Mockito.when(databaseAccess.getAthlete(1)).thenThrow(exception);
        assertEquals("Datenbank nicht gelockt", DBSTATE.PROBLEM, DatabaseHelper.getDatabaseState(databaseAccess));
     }
 
     @Test
     public void testDbLockedNOTExisting() {
         final IDatabaseAccess databaseAccess = Mockito.mock(IDatabaseAccess.class);
         final RuntimeException exception = Mockito.mock(RuntimeException.class);
         final Throwable throwable = Mockito.mock(Throwable.class);
         Mockito.when(exception.getCause()).thenReturn(throwable);
         Mockito.when(throwable.getMessage()).thenReturn("Locked by another process");
         Mockito.when(databaseAccess.getAthlete(1)).thenThrow(exception);
        assertEquals("Datenbank gelockt", DBSTATE.LOCKED, DatabaseHelper.getDatabaseState(databaseAccess));
     }
 }
