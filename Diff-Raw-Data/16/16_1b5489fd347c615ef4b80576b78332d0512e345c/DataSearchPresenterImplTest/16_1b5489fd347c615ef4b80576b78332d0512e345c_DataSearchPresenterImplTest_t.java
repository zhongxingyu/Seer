 package org.iplantc.core.uidiskresource.client.search.presenter.impl;
 
 import com.google.common.collect.Lists;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwtmockito.GxtMockitoTestRunner;
 
 import com.sencha.gxt.data.shared.TreeStore;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import org.iplantc.core.uicommons.client.info.IplantAnnouncer;
 import org.iplantc.core.uicommons.client.models.diskresources.Folder;
 import org.iplantc.core.uicommons.client.models.search.DiskResourceQueryTemplate;
 import org.iplantc.core.uicommons.client.models.search.SearchAutoBeanFactory;
 import org.iplantc.core.uicommons.client.services.SearchServiceFacade;
 import org.iplantc.core.uidiskresource.client.events.FolderSelectedEvent;
 import org.iplantc.core.uidiskresource.client.events.FolderSelectedEvent.FolderSelectedEventHandler;
 import org.iplantc.core.uidiskresource.client.events.FolderSelectedEvent.HasFolderSelectedEventHandlers;
 import org.iplantc.core.uidiskresource.client.search.events.DeleteSavedSearchEvent.HasDeleteSavedSearchEventHandlers;
 import org.iplantc.core.uidiskresource.client.search.events.SaveDiskResourceQueryEvent;
 import org.iplantc.core.uidiskresource.client.search.events.SaveDiskResourceQueryEvent.SaveDiskResourceQueryEventHandler;
 import org.iplantc.core.uidiskresource.client.search.events.SubmitDiskResourceQueryEvent;
 import org.iplantc.core.uidiskresource.client.search.events.SubmitDiskResourceQueryEvent.SubmitDiskResourceQueryEventHandler;
 import org.iplantc.core.uidiskresource.client.search.views.DiskResourceSearchField;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Captor;
 import org.mockito.Mock;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * FIXME Update
  * 
  * @author jstroot
  * 
  */
 @RunWith(GxtMockitoTestRunner.class)
 public class DataSearchPresenterImplTest {
 
     @Mock DiskResourceSearchField viewMock;
     @Mock TreeStore<Folder> treeStoreMock;
     @Mock SearchServiceFacade searchService;
     @Mock IplantAnnouncer announcer;
     @Mock SearchAutoBeanFactory factoryMock;
 
     @Captor ArgumentCaptor<List<DiskResourceQueryTemplate>> drqtListCaptor;
     @Captor ArgumentCaptor<AsyncCallback<List<DiskResourceQueryTemplate>>> stringAsyncCbCaptor;
     @Captor ArgumentCaptor<AsyncCallback<List<DiskResourceQueryTemplate>>> drqtListAsyncCaptor;
     @Captor ArgumentCaptor<AsyncCallback<Boolean>> booleanAsyncCaptor;
 
     private DataSearchPresenterImpl dsPresenter;
 
     @Before public void setUp() {
         dsPresenter = new DataSearchPresenterImpl(searchService, announcer, factoryMock);
         dsPresenter.searchField = viewMock;
         dsPresenter.treeStore = treeStoreMock;
     }
 
     /**
      * Verifies that nothing will occur if a query template's name is null
      * 
      * @see org.iplantc.core.uidiskresource.client.search.presenter.DataSearchPresenter#doSaveDiskResourceQueryTemplate(org.iplantc.core.uidiskresource.client.search.events.SaveDiskResourceQueryEvent)
      */
     @Test public void testDoSaveDiskResourceQueryTemplate_Case1() {
         DataSearchPresenterImpl spy = spy(dsPresenter);
         SaveDiskResourceQueryEvent mockEvent = mock(SaveDiskResourceQueryEvent.class);
         DiskResourceQueryTemplate mockTemplate = mock(DiskResourceQueryTemplate.class);
         // This mock will return null for a call to getName() by default
         when(mockEvent.getQueryTemplate()).thenReturn(mockTemplate);
 
         spy.doSaveDiskResourceQueryTemplate(mockEvent);
         // Perform verify for record keeping
         verify(spy).doSaveDiskResourceQueryTemplate(eq(mockEvent));
 
         verifyZeroInteractions(searchService);
 
         verifyNoMoreInteractions(spy);
     }
 
     /**
      * Verifies that nothing will occur if a query template's name is an empty string.
      * 
      * @see org.iplantc.core.uidiskresource.client.search.presenter.DataSearchPresenter#doSaveDiskResourceQueryTemplate(org.iplantc.core.uidiskresource.client.search.events.SaveDiskResourceQueryEvent)
      */
     @Test public void testDoSaveDiskResourceQueryTemplate_Case2() {
         DataSearchPresenterImpl spy = spy(dsPresenter);
         SaveDiskResourceQueryEvent mockEvent = mock(SaveDiskResourceQueryEvent.class);
         DiskResourceQueryTemplate mockTemplate = mock(DiskResourceQueryTemplate.class);
         when(mockEvent.getQueryTemplate()).thenReturn(mockTemplate);
 
         when(mockTemplate.getName()).thenReturn("");
         when(mockEvent.getQueryTemplate()).thenReturn(mockTemplate);
 
         spy.doSaveDiskResourceQueryTemplate(mockEvent);
         // Perform verify for record keeping
         verify(spy).doSaveDiskResourceQueryTemplate(eq(mockEvent));
 
         verifyZeroInteractions(searchService);
 
         verifyNoMoreInteractions(spy);
     }
 
     /**
      * Verifies that an existing query template will be replaced when a request to save a template of the
      * same name is received.
     * FIXME Another test needs to be fixed due to autobean issues
      * 
      * @see org.iplantc.core.uidiskresource.client.search.presenter.DataSearchPresenter#doSaveDiskResourceQueryTemplate(org.iplantc.core.uidiskresource.client.search.events.SaveDiskResourceQueryEvent)
      */
    @Ignore
     @Test public void testDoSaveDiskResourceQueryTemplate_Case3() {
         DataSearchPresenterImpl spy = spy(dsPresenter);
         SaveDiskResourceQueryEvent mockEvent = mock(SaveDiskResourceQueryEvent.class);
 
         /* ================ Save first template =================== */
         DiskResourceQueryTemplate mockTemplate = mock(DiskResourceQueryTemplate.class);
         when(mockTemplate.getName()).thenReturn("firstMock");
         when(mockEvent.getQueryTemplate()).thenReturn(mockTemplate);
 
         // Call method under test
         spy.doSaveDiskResourceQueryTemplate(mockEvent);
 
         /* Verify that the service was called to save the template, and only 1 template was saved */
         verify(searchService).saveQueryTemplates(drqtListCaptor.capture(), booleanAsyncCaptor.capture());
         assertEquals("list passed to search service is expected size", 1, drqtListCaptor.getValue().size());
         assertTrue("list passed to search service contains the template to be saved", drqtListCaptor.getValue().contains(mockTemplate));
         // Mock expected behavior from service success
         booleanAsyncCaptor.getValue().onSuccess(true);
 
 
         /* ================ Save second template =================== */
         DiskResourceQueryTemplate mockTemplate_2 = mock(DiskResourceQueryTemplate.class);
         when(mockTemplate_2.getName()).thenReturn("secondMock");
         when(mockEvent.getQueryTemplate()).thenReturn(mockTemplate_2);
 
         // Call method under test
         spy.doSaveDiskResourceQueryTemplate(mockEvent);
 
 
         /* Verify that the service was called to save the template, and only 2 templates were saved */
         verify(searchService, times(2)).saveQueryTemplates(drqtListCaptor.capture(), booleanAsyncCaptor.capture());
         assertEquals("list passed to search service is expected size", 2, drqtListCaptor.getValue().size());
         assertTrue("list passed to search service contains previously saved template", drqtListCaptor.getValue().contains(mockTemplate));
         assertTrue("list passed to search service contains the template to be saved", drqtListCaptor.getValue().contains(mockTemplate_2));
         // Mock expected behavior from service success
         booleanAsyncCaptor.getValue().onSuccess(true);
 
 
         /* ================ Save third template =================== */
         DiskResourceQueryTemplate mockTemplate_3 = mock(DiskResourceQueryTemplate.class);
         when(mockTemplate_3.getName()).thenReturn("firstMock");
         when(mockEvent.getQueryTemplate()).thenReturn(mockTemplate_3);
         spy.doSaveDiskResourceQueryTemplate(mockEvent);
 
 
         /* Verify that the service was called to save the template, and only 2 templates were saved but one of them was
          * replaced.
          */
         verify(searchService, times(3)).saveQueryTemplates(drqtListCaptor.capture(), booleanAsyncCaptor.capture());
         assertEquals("list passed to search service is expected size", 2, drqtListCaptor.getValue().size());
         assertTrue("list passed to search service contains previously saved template", drqtListCaptor.getValue().contains(mockTemplate_2));
         assertTrue("list passed to search service contains previously saved template", drqtListCaptor.getValue().contains(mockTemplate_3));
         assertFalse("list passed to search service contains the template to be saved", drqtListCaptor.getValue().contains(mockTemplate));
     }
 
     /**
      * Verifies that a search of a given query will be requested after it is successfully persisted.
     * 
     * FIXME Another test needs to be fixed due to autobean issues
     * 
      * @see org.iplantc.core.uidiskresource.client.search.presenter.DataSearchPresenter#doSaveDiskResourceQueryTemplate(org.iplantc.core.uidiskresource.client.search.events.SaveDiskResourceQueryEvent)
      */
    @Ignore
     @Test public void testDoSaveDiskResourceQueryTemplateOnSuccess_Case1() {
         DataSearchPresenterImpl spy = spy(dsPresenter);
         SaveDiskResourceQueryEvent mockEvent = mock(SaveDiskResourceQueryEvent.class);
         DiskResourceQueryTemplate mockTemplate = mock(DiskResourceQueryTemplate.class);
         when(mockTemplate.getName()).thenReturn("mock1");
         when(mockEvent.getQueryTemplate()).thenReturn(mockTemplate);
 
         spy.doSaveDiskResourceQueryTemplate(mockEvent);
         verify(searchService).saveQueryTemplates(drqtListCaptor.capture(), booleanAsyncCaptor.capture());
 
         assertEquals("Verify that the query has not been added to the presenter's list", 0, spy.getQueryTemplates().size());
 
         // Force service success
         booleanAsyncCaptor.getValue().onSuccess(true);
 
         ArgumentCaptor<SubmitDiskResourceQueryEvent> submitEventCaptor = ArgumentCaptor.forClass(SubmitDiskResourceQueryEvent.class);
         verify(spy).doSubmitDiskResourceQuery(submitEventCaptor.capture());
         assertEquals("Verify that a search is requested after a successful persist. ", mockTemplate, submitEventCaptor.getValue().getQueryTemplate());
 
         verify(searchService).createFrozenList(drqtListCaptor.capture());
         assertEquals("Verify that list passed to createFrozenList is expected size", 1, drqtListCaptor.getValue().size());
         assertEquals("Verify that list passed to createFrozenList contains intended template", mockTemplate, drqtListCaptor.getValue().get(0));
 
         verify(spy).setCleanCopyQueryTemplates(anyListOf(DiskResourceQueryTemplate.class));
 
         assertEquals("Verify that the query has been added to the presenter's list after successful persist", 1, spy.queryTemplates.size());
         verifyNoMoreInteractions(searchService);
     }
 
     /**
      * Verifies that a query will be removed from the treestore when a name change is detected.
     * FIXME Another test needs to be fixed due to autobean issues
      */
    @Ignore
     @Test public void testDoSaveDiskResourceQueryTemplateOnSuccess_Case2() {
         final String originalName = "originalMockName";
         DataSearchPresenterImpl spy = spy(dsPresenter);
         SaveDiskResourceQueryEvent mockEvent = mock(SaveDiskResourceQueryEvent.class);
         DiskResourceQueryTemplate mockTemplate = mock(DiskResourceQueryTemplate.class);
         DiskResourceQueryTemplate cleanMockTemplate = mock(DiskResourceQueryTemplate.class);
 
         when(mockTemplate.getName()).thenReturn("mock1");
         when(cleanMockTemplate.getName()).thenReturn(originalName);
         when(mockEvent.getQueryTemplate()).thenReturn(mockTemplate);
         when(mockEvent.getOriginalName()).thenReturn(originalName);
 
         spy.cleanCopyQueryTemplates = Lists.newArrayList(cleanMockTemplate);
         spy.doSaveDiskResourceQueryTemplate(mockEvent);
         verify(searchService).saveQueryTemplates(drqtListCaptor.capture(), booleanAsyncCaptor.capture());
         
         // Force service success
         booleanAsyncCaptor.getValue().onSuccess(true);
 
         ArgumentCaptor<Folder> folderCaptor = ArgumentCaptor.forClass(Folder.class);
         verify(treeStoreMock).remove(folderCaptor.capture());
         assertEquals("Verify that the intended template was passed to remove method", cleanMockTemplate, folderCaptor.getValue());
     }
     
     /**
      * Verifies that a search will not be requested after a failure to persist a query.
      *
      * @see org.iplantc.core.uidiskresource.client.search.presenter.DataSearchPresenter#doSaveDiskResourceQueryTemplate(org.iplantc.core.uidiskresource.client.search.events.SaveDiskResourceQueryEvent)
      */
     @Test public void testDoSaveDiskResourceQueryTemplate_Case5() {
         DataSearchPresenterImpl spy = spy(dsPresenter);
         SaveDiskResourceQueryEvent mockEvent = mock(SaveDiskResourceQueryEvent.class);
         DiskResourceQueryTemplate mockTemplate = mock(DiskResourceQueryTemplate.class);
         when(mockTemplate.getName()).thenReturn("mock1");
         when(mockEvent.getQueryTemplate()).thenReturn(mockTemplate);
 
         spy.doSaveDiskResourceQueryTemplate(mockEvent);
         // Perform verify for record keeping
         verify(spy).doSaveDiskResourceQueryTemplate(eq(mockEvent));
         verify(searchService).saveQueryTemplates(drqtListCaptor.capture(), booleanAsyncCaptor.capture());
 
         assertEquals("Verify that the query has not been added to the presenter's list", 0, spy.getQueryTemplates().size());
 
         // Force service failure
         booleanAsyncCaptor.getValue().onFailure(null);
 
         /* Verify that a search is not requested after failure to persist */
         verify(spy, never()).doSubmitDiskResourceQuery(any(SubmitDiskResourceQueryEvent.class));
         verifyNoMoreInteractions(searchService);
 
         assertEquals("Verify that the query has not been added to the presenter's list after failed persist", 0, spy.getQueryTemplates().size());
     }
 
     /**
      * Verifies that the item to be submitted will be set as the active query, the given template will be
      * fired in a {@link FolderSelectedEvent}, and the
      * {@link DataSearchPresenterImpl#updateDataNavigationWindow(List, TreeStore)} method will be called.
      * 
      * @see org.iplantc.core.uidiskresource.client.search.presenter.DataSearchPresenter#doSubmitDiskResourceQuery(SubmitDiskResourceQueryEvent)
      */
     @Test public void testDoSubmitDiskResourceQuery_Case1() {
         DataSearchPresenterImpl spy = spy(dsPresenter);
         DiskResourceQueryTemplate mockedTemplate = mock(DiskResourceQueryTemplate.class);
         when(mockedTemplate.getName()).thenReturn("mockedTemplateId");
         SubmitDiskResourceQueryEvent mockEvent = mock(SubmitDiskResourceQueryEvent.class);
         when(mockEvent.getQueryTemplate()).thenReturn(mockedTemplate);
 
         spy.setCleanCopyQueryTemplates(Collections.<DiskResourceQueryTemplate> emptyList());
 
         // Call method under test
         spy.doSubmitDiskResourceQuery(mockEvent);
 
         /* Verify that the updateDataNavigationWindow method has been called */
         verify(spy).updateDataNavigationWindow(anyListOf(DiskResourceQueryTemplate.class), eq(treeStoreMock));
 
         assertEquals("Verify that the active query has been set", mockedTemplate, spy.getActiveQuery());
 
         ArgumentCaptor<FolderSelectedEvent> fseCaptor = ArgumentCaptor.forClass(FolderSelectedEvent.class);
         verify(spy).fireEvent(fseCaptor.capture());
         assertEquals("Verify that a folder selected event has been fired with the mocked template", mockedTemplate, fseCaptor.getValue().getSelectedFolder());
     }
 
     /**
      * Verifies that the list passed to the updateNavigationWindow method is correct when the given query
      * template has no name.
      * 
      * @see org.iplantc.core.uidiskresource.client.search.presenter.DataSearchPresenter#doSubmitDiskResourceQuery(SubmitDiskResourceQueryEvent)
      */
     @Test public void testDoSubmitDiskResourceQuery_Case2() {
         DataSearchPresenterImpl spy = spy(dsPresenter);
         DiskResourceQueryTemplate eventMockTemplate = mock(DiskResourceQueryTemplate.class);
         // Return empty string to indicate that the template is new
         when(eventMockTemplate.getName()).thenReturn("");
         SubmitDiskResourceQueryEvent mockEvent = mock(SubmitDiskResourceQueryEvent.class);
         when(mockEvent.getQueryTemplate()).thenReturn(eventMockTemplate);
 
         // Add an existing mock to the classes template list
         DiskResourceQueryTemplate existingMock = mock(DiskResourceQueryTemplate.class);
         spy.getQueryTemplates().add(existingMock);
 
         // Set up Folder root tree store items
         List<Folder> toReturn = createTreeStoreRootFolderList();
         when(treeStoreMock.getRootItems()).thenReturn(toReturn);
 
         // Call method under test
         spy.doSubmitDiskResourceQuery(mockEvent);
 
         /* Verify that the given list only contains the existing template */
         verify(spy).updateDataNavigationWindow(drqtListCaptor.capture(), eq(treeStoreMock));
         assertEquals("verify that the given list only has one item", 1, drqtListCaptor.getValue().size());
         assertTrue("verify that the initial query template was not removed from the given list", drqtListCaptor.getValue().contains(existingMock));
         assertFalse("verify that the query template from the event is not in the given list", drqtListCaptor.getValue().contains(eventMockTemplate));
 
         verify(eventMockTemplate, never()).setDirty(eq(true));
     }
 
     /**
      * Verifies the list passed to the updateNavigationWindow method when the given query template is not
      * new, and has been changed.
      * 
      * The list should be equivalent to what was returned by getQueryTemplates() prior to the method
      * call, but with the changed item in place of the non-changed item.
      * 
      * @see org.iplantc.core.uidiskresource.client.search.presenter.DataSearchPresenter#doSubmitDiskResourceQuery(SubmitDiskResourceQueryEvent)
      */
     @Test public void testDoSubmitDiskResourceQuery_Case3() {
         // Create presenter with overridden method to control test execution.
         dsPresenter = new DataSearchPresenterImpl(searchService, announcer, factoryMock) {
             @Override
             boolean areTemplatesEqual(DiskResourceQueryTemplate lhs, DiskResourceQueryTemplate rhs) {
                 if (lhs.getName().equals("qtMockId1") && rhs.getName().equals("qtMockId1")) {
                     return false;
                 }
                 return true;
             }
         };
         dsPresenter.searchField = viewMock;
         dsPresenter.treeStore = treeStoreMock;
         DataSearchPresenterImpl spy = spy(dsPresenter);
         DiskResourceQueryTemplate eventMockTemplate = mock(DiskResourceQueryTemplate.class);
         // Return empty string to indicate that the template is new
         when(eventMockTemplate.getName()).thenReturn("qtMockId1");
         SubmitDiskResourceQueryEvent mockEvent = mock(SubmitDiskResourceQueryEvent.class);
         when(mockEvent.getQueryTemplate()).thenReturn(eventMockTemplate);
 
         // Add an existing mock to the classes template list
         DiskResourceQueryTemplate existingMock1 = mock(DiskResourceQueryTemplate.class);
         DiskResourceQueryTemplate existingMock2 = mock(DiskResourceQueryTemplate.class);
         when(existingMock1.getName()).thenReturn("qtMockId1");
         when(existingMock2.getName()).thenReturn("qtMockId2");
         spy.getQueryTemplates().clear();
         spy.getQueryTemplates().addAll(Lists.<DiskResourceQueryTemplate> newArrayList(existingMock1, existingMock2));
         int initialSize = spy.getQueryTemplates().size();
 
         spy.setCleanCopyQueryTemplates(Lists.<DiskResourceQueryTemplate> newArrayList(existingMock1, existingMock2));
 
         // Call method under test
         spy.doSubmitDiskResourceQuery(mockEvent);
 
         /* Verify that the setDirty flag of the changed template has been set */
         verify(eventMockTemplate).setDirty(eq(true));
 
         /* Verify that the given list only contains the existing and event mock templates */
         verify(spy).updateDataNavigationWindow(drqtListCaptor.capture(), eq(treeStoreMock));
         assertEquals("verify that the size of the list has not changed", initialSize, drqtListCaptor.getValue().size());
         assertTrue("verify that the list contains the updated query template", drqtListCaptor.getValue().contains(eventMockTemplate));
         assertTrue("verify that the list contains the unmodified starting original template", drqtListCaptor.getValue().contains(existingMock2));
 
     }
 
     /**
      * Verifies that a template will be added to the tree store if it is not already there.
      * 
      * @see DataSearchPresenterImpl#updateDataNavigationWindow(List, TreeStore)
      */
     @Test public void testUpdateDataNavigationWindow_Case1() {
         DiskResourceQueryTemplate mock1 = mock(DiskResourceQueryTemplate.class);
         DiskResourceQueryTemplate mock2 = mock(DiskResourceQueryTemplate.class);
         DiskResourceQueryTemplate mock3 = mock(DiskResourceQueryTemplate.class);
         when(mock1.getId()).thenReturn("qtMock1Id");
         when(mock2.getId()).thenReturn("qtMock2Id");
         when(mock3.getId()).thenReturn("qtMock3Id");
     
         // Set up Folder root tree store items
         List<Folder> toReturn = createTreeStoreRootFolderList();
         // Add 2 mocks to tree store root items
         toReturn.add(mock1);
         toReturn.add(mock2);
         when(treeStoreMock.getRootItems()).thenReturn(toReturn);
         when(treeStoreMock.findModelWithKey("qtMock1Id")).thenReturn(mock1);
         when(treeStoreMock.findModelWithKey("qtMock2Id")).thenReturn(mock2);
 
         // Create list to pass which contains all 3 query template mocks
         List<DiskResourceQueryTemplate> queryTemplates = Lists.newArrayList(mock1, mock2, mock3);
         // Call method under test
         dsPresenter.updateDataNavigationWindow(queryTemplates, treeStoreMock);
 
         /* Verify that nothing is removed from the store */
         verify(treeStoreMock, never()).remove(any(Folder.class));
 
         /* Verify that no item is updated in the store */
         verify(treeStoreMock, never()).update(any(Folder.class));
 
         /* Verify that the mock not previously contained in the tree store is added */
         verify(treeStoreMock).add(eq(mock3));
     }
 
     /**
      * Verifies that an item which is dirty and already in the tree store will be updated.
      * 
      * @see DataSearchPresenterImpl#updateDataNavigationWindow(List, TreeStore)
      */
     @Test public void testUpdateDataNavigationWindow_Case2() {
         DiskResourceQueryTemplate mock1 = mock(DiskResourceQueryTemplate.class);
         DiskResourceQueryTemplate mock2 = mock(DiskResourceQueryTemplate.class);
         when(mock1.getId()).thenReturn("qtMock1Id");
         when(mock1.isDirty()).thenReturn(true);
         when(mock2.getId()).thenReturn("qtMock2Id");
 
         // Set up Folder root tree store items
         List<Folder> toReturn = createTreeStoreRootFolderList();
         // Add 2 mocks to tree store root items
         toReturn.add(mock1);
         toReturn.add(mock2);
         when(treeStoreMock.getRootItems()).thenReturn(toReturn);
         when(treeStoreMock.findModelWithKey("qtMock1Id")).thenReturn(mock1);
         when(treeStoreMock.findModelWithKey("qtMock2Id")).thenReturn(mock2);
 
         // Create list to pass which contains all 3 query template mocks
         List<DiskResourceQueryTemplate> queryTemplates = Lists.newArrayList(mock1, mock2);
         // Call method under test
         dsPresenter.updateDataNavigationWindow(queryTemplates, treeStoreMock);
 
         /* Verify that nothing is removed from the store */
         verify(treeStoreMock, never()).remove(any(Folder.class));
 
         /* Verify that the tree store is updated with the dirty query template */
         verify(treeStoreMock).update(eq(mock1));
 
         /* Verify that nothing is added to the store */
         verify(treeStoreMock, never()).add(any(Folder.class));
     }
 
     /**
      * Verify #searchInit functionality when the call to retrieve saved templates is successful and returns queries.
      *
      * @see org.iplantc.core.uidiskresource.client.search.presenter.DataSearchPresenter#searchInit(org.iplantc.core.uidiskresource.client.views.DiskResourceView)
      */
     @Test public void testSearchInit_Case1() {
         final HasFolderSelectedEventHandlers hasFolderSelectHandlersMock = mock(HasFolderSelectedEventHandlers.class);
         final HasDeleteSavedSearchEventHandlers hasDeleteSavedSearchEventHandlers = mock(HasDeleteSavedSearchEventHandlers.class);
         final FolderSelectedEventHandler folderSelectedEventHandlerMock = mock(FolderSelectedEventHandler.class);
         DataSearchPresenterImpl spy = spy(dsPresenter);
         spy.searchInit(hasFolderSelectHandlersMock, hasDeleteSavedSearchEventHandlers, folderSelectedEventHandlerMock, treeStoreMock, viewMock);
 
         /* Verify that presenter adds itself as handler to hasHandlersMock */
         verify(hasFolderSelectHandlersMock).addFolderSelectedEventHandler(eq(spy));
         verify(hasDeleteSavedSearchEventHandlers).addDeleteSavedSearchEventHandler(eq(spy));
 
         verify(spy).addFolderSelectedEventHandler(eq(folderSelectedEventHandlerMock));
 
         assertEquals("Verify that view is saved", viewMock, dsPresenter.searchField);
         assertEquals("Verify that the treeStore is saved", treeStoreMock, dsPresenter.treeStore);
 
         ArgumentCaptor<SaveDiskResourceQueryEvent.SaveDiskResourceQueryEventHandler> saveEventHandlerCaptor
                 = ArgumentCaptor.forClass(SaveDiskResourceQueryEvent.SaveDiskResourceQueryEventHandler.class);
         ArgumentCaptor<SubmitDiskResourceQueryEvent.SubmitDiskResourceQueryEventHandler> submitEventHandlerCaptor
                 = ArgumentCaptor.forClass(SubmitDiskResourceQueryEvent.SubmitDiskResourceQueryEventHandler.class);
         verify(viewMock).addSaveDiskResourceQueryEventHandler(saveEventHandlerCaptor.capture());
         verify(viewMock).addSubmitDiskResourceQueryEventHandler(submitEventHandlerCaptor.capture());
         assertEquals("Verify that the presenter registers itself to SaveDiskResourceQueryEvents", saveEventHandlerCaptor.getValue(), spy);
         assertEquals("Verify that the presenter registers itself to SubmitDiskResourceQueryEvents", submitEventHandlerCaptor.getValue(), spy);
 
         verifyNoMoreInteractions(viewMock, hasFolderSelectHandlersMock, folderSelectedEventHandlerMock, hasDeleteSavedSearchEventHandlers);
         verifyZeroInteractions(searchService, treeStoreMock);
     }
 
     @Test public void testOnFolderSelected_Case1() {
         final HasFolderSelectedEventHandlers hasFolderSelectHandlersMock = mock(HasFolderSelectedEventHandlers.class);
         final HasDeleteSavedSearchEventHandlers hasDeleteSavedSearchEventHandlersMock = mock(HasDeleteSavedSearchEventHandlers.class);
         final FolderSelectedEventHandler folderSelectedEventHandlerMock = mock(FolderSelectedEventHandler.class);
         
         dsPresenter.searchInit(hasFolderSelectHandlersMock, hasDeleteSavedSearchEventHandlersMock, folderSelectedEventHandlerMock, treeStoreMock, viewMock);
         verify(hasFolderSelectHandlersMock).addFolderSelectedEventHandler(eq(dsPresenter));
         verify(hasDeleteSavedSearchEventHandlersMock).addDeleteSavedSearchEventHandler(dsPresenter);
 
         verify(viewMock).addSaveDiskResourceQueryEventHandler(any(SaveDiskResourceQueryEventHandler.class));
         verify(viewMock).addSubmitDiskResourceQueryEventHandler(any(SubmitDiskResourceQueryEventHandler.class));
 
         // Test update of searchResults mock
         FolderSelectedEvent fse = mock(FolderSelectedEvent.class);
         when(fse.getSelectedFolder()).thenReturn(mock(Folder.class));
         dsPresenter.onFolderSelected(fse);
         
         verify(viewMock).clearSearch();
 
         verifyNoMoreInteractions(viewMock, hasFolderSelectHandlersMock, folderSelectedEventHandlerMock, hasDeleteSavedSearchEventHandlersMock);
         verifyZeroInteractions(searchService, treeStoreMock);
     }
     
     @Test public void testOnFolderSelected_Case2() {
         final HasFolderSelectedEventHandlers hasFolderSelectHandlersMock = mock(HasFolderSelectedEventHandlers.class);
         final HasDeleteSavedSearchEventHandlers hasDeleteSavedSearchEventHandlersMock = mock(HasDeleteSavedSearchEventHandlers.class);
         final FolderSelectedEventHandler folderSelectedEventHandlerMock = mock(FolderSelectedEventHandler.class);
         dsPresenter.searchInit(hasFolderSelectHandlersMock, hasDeleteSavedSearchEventHandlersMock, folderSelectedEventHandlerMock, treeStoreMock, viewMock);
         verify(hasFolderSelectHandlersMock).addFolderSelectedEventHandler(eq(dsPresenter));
 
         verify(viewMock).addSaveDiskResourceQueryEventHandler(any(SaveDiskResourceQueryEventHandler.class));
         verify(viewMock).addSubmitDiskResourceQueryEventHandler(any(SubmitDiskResourceQueryEventHandler.class));
 
         FolderSelectedEvent fse = mock(FolderSelectedEvent.class);
         final DiskResourceQueryTemplate selectedFolderMock = mock(DiskResourceQueryTemplate.class);
         when(fse.getSelectedFolder()).thenReturn(selectedFolderMock);
         dsPresenter.onFolderSelected(fse);
         
         verify(viewMock).edit(eq(selectedFolderMock));
 
         verifyNoMoreInteractions(viewMock, hasFolderSelectHandlersMock, folderSelectedEventHandlerMock);
         verifyZeroInteractions(searchService, treeStoreMock);
 
     }
     
     @Test public void testLoadSavedQueries_Case1() {
         DataSearchPresenterImpl spy = spy(dsPresenter);
 
         assertTrue("Verify that store query templates are empty prior to calling method under test", spy.getQueryTemplates().isEmpty());
 
         final ArrayList<DiskResourceQueryTemplate> newArrayList = Lists.newArrayList(mock(DiskResourceQueryTemplate.class), mock(DiskResourceQueryTemplate.class));
         // Call method under test
         spy.loadSavedQueries(newArrayList);
         // Verify for record keeping
         verify(spy).loadSavedQueries(eq(newArrayList));
 
         verify(spy).setCleanCopyQueryTemplates(anyListOf(DiskResourceQueryTemplate.class));
         verify(searchService).createFrozenList(eq(newArrayList));
         // Verify for record keeping
         verify(spy).getQueryTemplates();
         verify(spy).updateDataNavigationWindow(drqtListCaptor.capture(), eq(treeStoreMock));
         verify(treeStoreMock, times(newArrayList.size())).findModelWithKey(anyString());
         verify(treeStoreMock).add(eq(newArrayList.get(0)));
         verify(treeStoreMock).add(eq(newArrayList.get(1)));
         assertEquals("Verify that list passed to updateDataNavigationWindow is same size as list passed to loadSavedQueries", newArrayList.size(), drqtListCaptor.getValue().size());
         assertTrue("Verify that all items from each list match", drqtListCaptor.getValue().contains(newArrayList.get(0)));
         assertTrue("Verify that all items from each list match", drqtListCaptor.getValue().contains(newArrayList.get(1)));
 
         verifyNoMoreInteractions(searchService, treeStoreMock, spy);
     }
 
 
     List<Folder> createTreeStoreRootFolderList(){
         // Set up Folder root tree store items
         Folder root1 = mock(Folder.class);
         Folder root2 = mock(Folder.class);
         Folder root3 = mock(Folder.class);
         Folder root4 = mock(Folder.class);
         when(root1.getId()).thenReturn("root1Id");
         when(root2.getId()).thenReturn("root2Id");
         when(root3.getId()).thenReturn("root3Id");
         when(root4.getId()).thenReturn("root4Id");
         List<Folder> toReturn = Lists.newArrayList(root1, root2, root3, root4);
         return toReturn;
     }
 
 }
