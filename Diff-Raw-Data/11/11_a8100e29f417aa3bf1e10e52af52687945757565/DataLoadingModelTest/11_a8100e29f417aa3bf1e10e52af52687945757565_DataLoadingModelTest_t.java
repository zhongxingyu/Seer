 package net.todd.bible.scripturelookup.client;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 
 import java.util.Arrays;
 import java.util.UUID;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 
 public class DataLoadingModelTest {
 	private IDataLoadingServiceCaller dataLoadingServiceCaller;
 	private IDataLoadingModel dataLoadingModel;
 	private IListener loadingSuccessfullListener;
 	private IListener loadingFailedListener;
 	private IFileProvider fileProvider;
 	private String fileName1;
 	private String fileName2;
 	private String fileName3;
 	private String fileName4;
 
 	@Before
 	public void setUpMocks() {
 		dataLoadingServiceCaller = mock(IDataLoadingServiceCaller.class);
 		fileProvider = mock(IFileProvider.class);
 		
 		fileName1 = UUID.randomUUID().toString();
 		fileName2 = UUID.randomUUID().toString();
 		fileName3 = UUID.randomUUID().toString();
 		fileName4 = UUID.randomUUID().toString();
 		doReturn(Arrays.asList(fileName1, fileName2, fileName3, fileName4)).when(fileProvider)
 				.filesToLoad();
 		
 		dataLoadingModel = new DataLoadingModel(dataLoadingServiceCaller, fileProvider);
 		
 		ArgumentCaptor<IListener> loadingSuccessListenerCapture = ArgumentCaptor
 				.forClass(IListener.class);
 		verify(dataLoadingServiceCaller)
 				.addSuccessListener(loadingSuccessListenerCapture.capture());
 		loadingSuccessfullListener = loadingSuccessListenerCapture.getValue();
 
 		ArgumentCaptor<IListener> loadingFailedListenerCapture = ArgumentCaptor
 				.forClass(IListener.class);
 		verify(dataLoadingServiceCaller).addFailureListener(loadingFailedListenerCapture.capture());
 		loadingFailedListener = loadingFailedListenerCapture.getValue();
 	}
 
 	@Test
 	public void errorMessageIsMadeAvailableWhenDataLoadingFails() {
 		String errorMessage = UUID.randomUUID().toString();
 		doReturn(new Exception(errorMessage)).when(dataLoadingServiceCaller).getException();
 
 		loadingFailedListener.handleEvent();
 
 		assertEquals(errorMessage, dataLoadingModel.getErrorMessage());
 	}
 
 	@Test
 	public void notifyDataLoadingFailedListenersWhenDataLoadingFails() {
 		IListener listener = mock(IListener.class);
 		dataLoadingModel.addFailureListener(listener);
 
 		loadingFailedListener.handleEvent();
 
 		verify(listener).handleEvent();
 	}
 	
 	@Test
 	public void callServiceWithFirstFileWhenReloadData() {
 		dataLoadingModel.reloadData();
 		
 		verify(dataLoadingServiceCaller).callService(fileName1);
 	}
 
 	@Test
 	public void loadEachDataFileWhenThePreviousReturnsSuccessFully() {
 		loadingSuccessfullListener.handleEvent();
 		verify(dataLoadingServiceCaller).callService(fileName2);
 		
 		loadingSuccessfullListener.handleEvent();
 		verify(dataLoadingServiceCaller).callService(fileName3);
 	}
 
 	@Test
 	public void progressListenersAreNotifiedAfterEveryDataFileIsLoaded() {
 		IListener progressListener = mock(IListener.class);
 
 		dataLoadingModel.addProgressListener(progressListener);
 		
 		loadingSuccessfullListener.handleEvent();
 		loadingSuccessfullListener.handleEvent();
 		loadingSuccessfullListener.handleEvent();
 		loadingSuccessfullListener.handleEvent();
 
 		verify(progressListener, times(4)).handleEvent();
 	}
 	
 	@Test
 	public void successListenersAreNotifiedAfterAllTheDataFilesHaveBeenLoaded() {
 		IListener successListener = mock(IListener.class);
 
 		dataLoadingModel.addDataReloadedListener(successListener);
 
 		loadingSuccessfullListener.handleEvent();
 		verify(successListener, times(0)).handleEvent();
 		
 		loadingSuccessfullListener.handleEvent();
 		verify(successListener, times(0)).handleEvent();
 		
 		loadingSuccessfullListener.handleEvent();
 		verify(successListener, times(0)).handleEvent();
 
 		loadingSuccessfullListener.handleEvent();
 		verify(successListener, times(1)).handleEvent();
 	}
 	
 	@Test
 	public void progressPercentageIsAvailableAfterEachDataFileHasBeenLoaded() {
 		loadingSuccessfullListener.handleEvent();
		assertEquals(0.25, dataLoadingModel.getPercentComplete(), 0);
 
 		loadingSuccessfullListener.handleEvent();
		assertEquals(0.5, dataLoadingModel.getPercentComplete(), 0);
 		
 		loadingSuccessfullListener.handleEvent();
		assertEquals(0.75, dataLoadingModel.getPercentComplete(), 0);
 
 		loadingSuccessfullListener.handleEvent();
		assertEquals(1, dataLoadingModel.getPercentComplete(), 0);
 	}
 }
