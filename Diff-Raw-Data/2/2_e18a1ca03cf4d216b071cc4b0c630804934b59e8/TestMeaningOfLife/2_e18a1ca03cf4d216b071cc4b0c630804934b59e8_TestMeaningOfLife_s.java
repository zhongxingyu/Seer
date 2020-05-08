 package ourCode;
 
 import org.junit.Before;
 import org.junit.Test;
 import ourCode.ServiceUnavailableException;
 import thirdparty.ZenService;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 public class TestMeaningOfLife {
     private ZenService mockZenService;
     private MeaningOfLife meaningOfLife;
     @Before
     public void setUp() {
         mockZenService = mock(ZenService.class);
         meaningOfLife = new MeaningOfLife(mockZenService);
     }
 
     @Test(expected = ServiceUnavailableException.class)
     public void meaningOfLifeShouldThrowServiceUnavailableExceptionWhenZenServiceIsUnavailable() throws ServiceUnavailableException {
         when(mockZenService.getUltimateAnswer()).thenThrow(ServiceUnavailableException.class);
         meaningOfLife.reveal();
     }
 
     @Test
    public void meaningOfLifeShouldShouldRevealAnswerWhenZenServiceIsAvailable() throws ServiceUnavailableException {
         when(mockZenService.getUltimateAnswer()).thenReturn(10);
         assertThat(meaningOfLife.reveal(), is(10));
     }
 
     @Test(expected = ServiceUnavailableException.class)
     public void meaningOfLifeShouldThrowServiceUnavailableExceptionWhenZenServiceReturnsMaxInteger() throws ServiceUnavailableException {
         when(mockZenService.getUltimateAnswer()).thenReturn(Integer.MAX_VALUE);
         meaningOfLife.reveal();
     }
 
     @Test
     public void meaningOfLifeShouldCallZenService() throws ServiceUnavailableException {
         meaningOfLife.reveal();
         verify(mockZenService).getUltimateAnswer();
     }
 
 }
