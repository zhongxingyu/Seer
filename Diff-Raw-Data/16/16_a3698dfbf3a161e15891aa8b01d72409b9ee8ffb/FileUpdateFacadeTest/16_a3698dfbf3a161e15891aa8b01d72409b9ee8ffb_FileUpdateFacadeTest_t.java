 package com.frugs.filesync.local.system;
 
 import com.frugs.filesync.domain.Diff;
 import com.frugs.filesync.local.FileUpdateFacade;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import java.io.IOException;
 
 import static com.frugs.filesync.domain.Diff.emptyDiff;
 import static com.frugs.filesync.domain.DiffBuilder.aDiff;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 
 @RunWith(MockitoJUnitRunner.class)
 public class FileUpdateFacadeTest {
     @Mock private SystemCommandFacade mockSystemCommandFacade;
 
     private FileUpdateFacade fileUpdateFacade;
 
     @Before
     public void setUp() throws Exception {
         fileUpdateFacade = new FileUpdateFacade(mockSystemCommandFacade);
     }
 
     @Test
     public void getCurrentState_delegates_to_commandFacade() throws IOException {
         Diff diff = aDiff().build();
         when(mockSystemCommandFacade.gitDiffHead()).thenReturn(diff);
 
         Diff result = fileUpdateFacade.getCurrentState();
         assertTrue(result == diff);
     }
 
     @Test
     public void applyDiff_delegates_to_commandExecutor() throws IOException {
         Diff diff = aDiff().build();
 
         fileUpdateFacade.applyDiff(diff);
         verify(mockSystemCommandFacade).gitApply(diff);
     }
 
     @Test
     public void applyDiff_does_nothing_given_empty_diff() throws IOException {
         fileUpdateFacade.applyDiff(emptyDiff);
         verifyNoMoreInteractions(mockSystemCommandFacade);
     }
 
     @Test
     public void interDiff_delegates_to_commandExecutor() throws IOException {
         Diff firstDiff = aDiff().withContent("firstDiff").build();
         Diff secondDiff = aDiff().withContent("secondDiff").build();
         Diff interDiff = aDiff().withContent("interDiff").build();
         when(mockSystemCommandFacade.interDiff((Diff) any(), (Diff) any())).thenReturn(interDiff);
 
         Diff result = fileUpdateFacade.interDiff(firstDiff, secondDiff);
 
         assertThat(result, is(interDiff));
         verify(mockSystemCommandFacade).interDiff(firstDiff, secondDiff);
     }
 
     @Test
     public void interDiff_returns_secondDiff_given_firstDiff_isEmpty() throws IOException {
         Diff secondDiff = aDiff().withContent("secondDiff").build();
 
         Diff result = fileUpdateFacade.interDiff(emptyDiff, secondDiff);
         assertThat(result, is(secondDiff));
         verifyNoMoreInteractions(mockSystemCommandFacade);
     }
 }
