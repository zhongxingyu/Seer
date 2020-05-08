 package org.iplantc.core.uicommons.client.services.impl;
 
 import com.google.common.collect.Lists;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwtmockito.GxtMockitoTestRunner;
 import com.google.web.bindery.autobean.shared.AutoBean;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import org.iplantc.core.uicommons.client.DEServiceFacade;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uicommons.client.models.diskresources.DiskResourceAutoBeanFactory;
 import org.iplantc.core.uicommons.client.models.search.DiskResourceQueryTemplate;
 import org.iplantc.core.uicommons.client.models.search.DiskResourceQueryTemplateList;
 import org.iplantc.core.uicommons.client.models.search.SearchAutoBeanFactory;
 import org.iplantc.core.uicommons.client.services.AsyncCallbackConverter;
 import org.iplantc.core.uicommons.client.services.Endpoints;
 import org.iplantc.core.uicommons.client.services.ReservedBuckets;
 import org.iplantc.core.uicommons.client.services.SearchServiceFacade;
 import org.iplantc.de.shared.services.BaseServiceCallWrapper.Type;
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Captor;
 import org.mockito.Mock;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @RunWith(GxtMockitoTestRunner.class)
 public class SearchServiceFacadeImplTest {
     
     @Mock DEServiceFacade deServiceFacadeMock;
     @Mock SearchAutoBeanFactory searchAbFactoryMock;
     @Mock
     DiskResourceAutoBeanFactory drFactoryMock;
     @Mock Endpoints endpointMock;
     @Mock ReservedBuckets bucketsMock;
     @Mock UserInfo userInfoMock;
     @Mock AutoBean<DiskResourceQueryTemplateList> qtlistAbMock;
     
     @Mock AsyncCallback<List<DiskResourceQueryTemplate>> asyncQtListMock;
     @Mock AsyncCallback<Boolean> asyncBooleanMock;
 
     @Captor ArgumentCaptor<AsyncCallback<List<DiskResourceQueryTemplate>>> asyncQtCaptor;
     @Captor ArgumentCaptor<AsyncCallbackConverter<String, List<DiskResourceQueryTemplate>>> asyncStringCaptor;
     @Captor ArgumentCaptor<List<DiskResourceQueryTemplate>> qtListCaptor;
 
     private SearchServiceFacade searchService;
 
     @Before public void setUp() {
         searchService = new SearchServiceFacadeImpl(deServiceFacadeMock, searchAbFactoryMock, drFactoryMock, endpointMock, bucketsMock, userInfoMock);
     }
 
     /**
      * Verifies the correct address formation and HTTP type submitted to the DE Service.
      * 
      * @see SearchServiceFacade#getSavedQueryTemplates(AsyncCallback)
      */
     @Test public void testGetSavedQueryTemplates_Case1() {
         
         final String bucketAddy = "buckets";
         final String userName = "genericUser";
         final String queryTemplateBucket = "savedQueryTemplatesBucket";
         when(endpointMock.buckets()).thenReturn(bucketAddy);
         when(userInfoMock.getUsername()).thenReturn(userName);
         when(bucketsMock.queryTemplates()).thenReturn(queryTemplateBucket);
         searchService.getSavedQueryTemplates(asyncQtListMock);
 
         /* Verify proper construction of service call wrapper */
         ArgumentCaptor<ServiceCallWrapper> wrapperCaptor = ArgumentCaptor.forClass(ServiceCallWrapper.class);
         verify(deServiceFacadeMock).getServiceData(wrapperCaptor.capture(), asyncStringCaptor.capture());
         // verify(endpointMock).buckets();
         verify(userInfoMock).getUsername();
         verify(bucketsMock).queryTemplates();
 
         final String expectedAddress = bucketAddy + "/" + userName + "/" + queryTemplateBucket;
        /* Verify expected address construction */
        assertEquals(expectedAddress, wrapperCaptor.getValue().getAddress());
         /* Verify that it is a GET */
         assertEquals(Type.GET, wrapperCaptor.getValue().getType());
 
         /* TODO Verify that an empty collection will be returned if service provides empty string. */
 
     }
 
     /**
      * Verifies that the returned list of {@link DiskResourceQueryTemplate}s have all
      * <code>isDirty()</code> flags set to false.
      * 
      * This test currently breaks because of autobean codex
      * FIXME JDS ignoring this test until AutoBeanCodex can be appropriately mocked.
      * 
      * @see SearchServiceFacade#getSavedQueryTemplates(AsyncCallback)
      */
     @Ignore
     @Test public void testGetSavedQueryTemplates_Case2() {
 
         searchService.getSavedQueryTemplates(asyncQtListMock);
         verify(deServiceFacadeMock).getServiceData(any(ServiceCallWrapper.class), asyncStringCaptor.capture());
         DiskResourceQueryTemplate qtMock1 = mock(DiskResourceQueryTemplate.class);
         DiskResourceQueryTemplate qtMock2 = mock(DiskResourceQueryTemplate.class);
         DiskResourceQueryTemplate qtMock3 = mock(DiskResourceQueryTemplate.class);
         when(qtMock1.isDirty()).thenReturn(true);
         when(qtMock1.isDirty()).thenReturn(false);
         when(qtMock1.isDirty()).thenReturn(true);
         final DiskResourceQueryTemplateList qtListMock = mock(DiskResourceQueryTemplateList.class);
         when(qtListMock.getQueryTemplateList()).thenReturn(Lists.newArrayList(qtMock1, qtMock2, qtMock3));
         when(qtlistAbMock.as()).thenReturn(qtListMock);
         when(searchAbFactoryMock.create(DiskResourceQueryTemplateList.class)).thenReturn(qtlistAbMock);
 
         // Trigger pre-canned response
         final String endpointResponse = "";
         asyncStringCaptor.getValue().onSuccess(endpointResponse);
 
         verify(asyncQtListMock).onSuccess(qtListCaptor.capture());
         for (DiskResourceQueryTemplate qt : qtListCaptor.getValue()) {
             /* Verify that all isDirty() methods of return templates return false. */
             verify(qt).setDirty(eq(false));
         }
     }
 
     /**
      * Verifies the expected address construction and HTTP type submitted to the DE Service
      * 
      * @see SearchServiceFacade#saveQueryTemplates(List, AsyncCallback)
      */
     @Test public void testSaveQueryTemplates_Case1() {
         final String bucketAddy = "testSaveBuckets";
         final String userName = "testSaveUsername";
         final String queryTemplatesBucket = "testSaveQuertyTemplatesBucket";
         when(endpointMock.buckets()).thenReturn(bucketAddy);
         when(userInfoMock.getUsername()).thenReturn(userName);
         when(bucketsMock.queryTemplates()).thenReturn(queryTemplatesBucket);
         
         final ArrayList<DiskResourceQueryTemplate> newArrayList = Lists.newArrayList();
         searchService.saveQueryTemplates(newArrayList, asyncBooleanMock);
 
         /* Verify proper construction of service call wrapper */
         ArgumentCaptor<ServiceCallWrapper> wrapperCaptor = ArgumentCaptor.forClass(ServiceCallWrapper.class);
         verify(deServiceFacadeMock).getServiceData(wrapperCaptor.capture(), asyncStringCaptor.capture());
         // verify(endpointMock).buckets();
         verify(userInfoMock).getUsername();
         verify(bucketsMock).queryTemplates();
 
         final String expectedAddress = bucketAddy + "/" + userName + "/" + queryTemplatesBucket;
        /* Verify expected address construction */
        assertEquals(expectedAddress, wrapperCaptor.getValue().getAddress());
         /* Verify that it is a POST */
         assertEquals(Type.POST, wrapperCaptor.getValue().getType());
     }
 
     /**
      * Verifies proper body construction.
      * 
      * 
      * Ignored until a means of dealing with Autobeans is discovered
      * FIXME JDS ignoring this test until AutoBeanCodex can be appropriately mocked.
      * 
      * @see SearchServiceFacade#saveQueryTemplates(List, AsyncCallback)
      */
     @Ignore
     @Test public void testSaveQueryTemplates_Case2() {
 
         DiskResourceQueryTemplate mock1 = mock(DiskResourceQueryTemplate.class);
         DiskResourceQueryTemplate mock2 = mock(DiskResourceQueryTemplate.class);
         when(mock1.getName()).thenReturn("a name");
         final ArrayList<DiskResourceQueryTemplate> newArrayList = Lists.newArrayList(mock1, mock2);
         searchService.saveQueryTemplates(newArrayList, asyncBooleanMock);
 
         /* Verify proper construction of service call wrapper */
         ArgumentCaptor<ServiceCallWrapper> wrapperCaptor = ArgumentCaptor.forClass(ServiceCallWrapper.class);
         verify(deServiceFacadeMock).getServiceData(wrapperCaptor.capture(), asyncStringCaptor.capture());
         String expectedBody = "";
         assertEquals(expectedBody, wrapperCaptor.getValue().getBody());
     }
 
     /**
      * Verifies the expected address constructions and HTTP type submitted to the DE Service
      * 
      * @see DiskResourceQueryTemplate, FilterPagingLoadConfigBean, AsyncCallback)
      */
     @Test
     public void testSubmitSearchFromQueryTemplate_Case1() {
         // TODO create test
     }
 
     @Test public void testCreateFrozenList() {
         // TODO Verify necessity of this method
     }
 
 }
