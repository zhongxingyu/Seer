 package org.sakaiproject.search.solr.indexing;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.sakaiproject.search.indexing.DefaultTask;
 import org.sakaiproject.search.indexing.Task;
 import org.sakaiproject.search.indexing.TaskHandler;
 import org.sakaiproject.search.indexing.TaskMatcher;
 import org.sakaiproject.search.indexing.exception.TaskHandlingException;
 import org.sakaiproject.search.queueing.IndexQueueing;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import static org.hamcrest.CoreMatchers.sameInstance;
 import static org.mockito.Mockito.*;
 
 /**
  * Checks if the splitting system works as expected.
  *
  * @author Colin Hebert
  */
 public class SolrSplitterProcessesTest {
     @Rule
     public ExpectedException thrown = ExpectedException.none();
     @Mock
     private TaskHandler mockTaskHandler;
     @Mock
     private IndexQueueing mockIndexQueueing;
     @Mock
     private SolrTools mockSolrTools;
     private Queue<String> indexableSites = new LinkedList<String>();
     private SolrSplitterProcesses solrSplitterProcesses;
 
     @Before
     public void setUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         solrSplitterProcesses = new SolrSplitterProcesses();
         solrSplitterProcesses.setActualTaskHandler(mockTaskHandler);
         solrSplitterProcesses.setIndexQueueing(mockIndexQueueing);
         solrSplitterProcesses.setSolrTools(mockSolrTools);
 
         indexableSites.offer("test1");
         indexableSites.offer("test2");
         indexableSites.offer("test3");
     }
 
     /**
      * Attempts to execute a simple task.
      * <p>
      * Checks that the task is relayed to a {@link TaskHandler} that can process it.
      * </p>
      */
     @Test
     public void testSimpleTask() {
         Task task = mock(Task.class);
         solrSplitterProcesses.executeTask(task);
 
         verify(mockTaskHandler).executeTask(task);
     }
 
     /**
      * Attempts to throw an exception during the execution of a relayed task.
      * <p>
      * Checks that the exception is caught and is wrapped in a {@link TaskHandlingException}.
      * </p>
      */
     @Test
     public void testRuntimeExceptionDuringSimpleTask() {
         Task task = mock(Task.class);
         RuntimeException toBeThrown = mock(RuntimeException.class);
         doThrow(toBeThrown).when(mockTaskHandler).executeTask(any(Task.class));
 
         thrown.expect(TaskHandlingException.class);
         thrown.expectCause(sameInstance(toBeThrown));
 
         solrSplitterProcesses.executeTask(task);
     }
 
     /**
      * Attempts to throw a {@link TaskHandlingException} during the execution of a relayed task.
      * <p>
      * Check that the exception is automatically rethrown without being wrapped.
      * </p>
      */
     @Test
     public void testTaskHandlingExceptionDuringSimpleTask() {
         Task task = mock(Task.class);
         TaskHandlingException toBeThrown = mock(TaskHandlingException.class);
         doThrow(toBeThrown).when(mockTaskHandler).executeTask(any(Task.class));
 
         thrown.expect(TaskHandlingException.class);
         thrown.expect(sameInstance(toBeThrown));
 
         solrSplitterProcesses.executeTask(task);
     }
 
     /**
      * Attempts to execute an "IndexAll" task.
      * <p>
      * Checks that the task is split in multiple subtasks.<br />
      * Checks that a "RemoveAll" task has been created.<br />
      * Checks that an "IndexSite" task has been created for each site available.
      * </p>
      */
     @Test
     public void testIndexAllTask() {
         Task task = mock(Task.class);
         when(task.getType()).thenReturn(DefaultTask.Type.INDEX_ALL.getTypeName());
         when(mockSolrTools.getIndexableSites()).thenReturn(indexableSites);
         int indexableSitesSize = indexableSites.size();
        int numberOfTasks = indexableSitesSize + 2;
         solrSplitterProcesses.executeTask(task);
 
         verify(mockIndexQueueing, times(numberOfTasks)).addTaskToQueue(any(Task.class));
         verify(mockIndexQueueing).addTaskToQueue(
                 argThat(new TaskMatcher(SolrTask.Type.REMOVE_ALL_DOCUMENTS.getTypeName())));
         verify(mockIndexQueueing, times(indexableSitesSize)).addTaskToQueue(
                 argThat(new TaskMatcher(DefaultTask.Type.INDEX_SITE.getTypeName())));
     }
 
     /**
      * Attempts to execute a "RefreshAll" task.
      * <p>
      * Checks that the task is split in multiple subtasks.<br />
      * Checks that a "RemoveAll" task has been created.<br />
     * Checks that an "OptimiseIndex" task has been created.<br />
      * Checks that an "RefreshSite" task has been created for each site available.
      * </p>
      */
     @Test
     public void testRefreshAllTask() {
         Task task = mock(Task.class);
         when(task.getType()).thenReturn(DefaultTask.Type.REFRESH_ALL.getTypeName());
         when(mockSolrTools.getIndexableSites()).thenReturn(indexableSites);
         int indexableSitesSize = indexableSites.size();
        int numberOfTasks = indexableSitesSize + 2;
         solrSplitterProcesses.executeTask(task);
 
         verify(mockIndexQueueing, times(numberOfTasks)).addTaskToQueue(any(Task.class));
         verify(mockIndexQueueing).addTaskToQueue(
                 argThat(new TaskMatcher(SolrTask.Type.REMOVE_ALL_DOCUMENTS.getTypeName())));
        verify(mockIndexQueueing).addTaskToQueue(
                argThat(new TaskMatcher(SolrTask.Type.OPTIMISE_INDEX.getTypeName())));
         verify(mockIndexQueueing, times(indexableSitesSize)).addTaskToQueue(
                 argThat(new TaskMatcher(DefaultTask.Type.REFRESH_SITE.getTypeName())));
     }
 }
