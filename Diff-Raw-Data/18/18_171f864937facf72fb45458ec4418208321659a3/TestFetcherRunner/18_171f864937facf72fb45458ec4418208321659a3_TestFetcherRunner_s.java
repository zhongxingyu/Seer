 package net.kokkeli.ripservice;
 
 import static org.mockito.Mockito.mock;
 import net.kokkeli.data.FetchRequest;
 import net.kokkeli.data.FetchStatus;
 import net.kokkeli.data.ILogger;
 import net.kokkeli.data.LogSeverity;
 import net.kokkeli.data.db.DatabaseException;
 import net.kokkeli.data.db.IFetchRequestDatabase;
 import net.kokkeli.data.db.NotFoundInDatabase;
 import net.kokkeli.data.services.ServiceException;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.mockito.Mockito.*;
 
 public class TestFetcherRunner {
     private IFetchRequestDatabase mockDatabase;
     private IFetcher mockFetcher;
     private ILogger mockLogger;
     
     private String mockType = "mocktype";
     
     private FetcherRunner fetcherRunner;
     
     @Before
     public void setup() throws NotFoundInDatabase, DatabaseException, ServiceException{
         mockDatabase = mock(IFetchRequestDatabase.class);
         mockLogger = mock(ILogger.class);
         mockFetcher = mock(IFetcher.class);
         
         when(mockFetcher.getHandledType()).thenReturn(mockType);
         
         fetcherRunner = new FetcherRunner(mockLogger, mockFetcher, mockDatabase);
     }
     
     @Test
    public void testRunCallsCorrectServices() throws DatabaseException{
         FetchRequest request = new FetchRequest();
         request.setId(666);
         
         when(mockDatabase.oldestUnhandledFetchRequestOrNull(mockType)).thenReturn(request);
         
        fetcherRunner.run();
         
         verify(mockDatabase).updateRequest(request.getId(), FetchStatus.HANDLING);
         verify(mockFetcher).fetch(request);
         verify(mockDatabase).updateRequest(request.getId(), FetchStatus.HANDLED);
     }
     
     @Test
    public void testRunHandlesDatabaseException() throws DatabaseException{
         when(mockDatabase.oldestUnhandledFetchRequestOrNull(mockType)).thenThrow(new DatabaseException("Kaboom!"));
         
        fetcherRunner.run();
         verify(mockLogger).log("Something went wrong with the database while processing fetch items. Kaboom!", LogSeverity.ERROR);
     }
 }
