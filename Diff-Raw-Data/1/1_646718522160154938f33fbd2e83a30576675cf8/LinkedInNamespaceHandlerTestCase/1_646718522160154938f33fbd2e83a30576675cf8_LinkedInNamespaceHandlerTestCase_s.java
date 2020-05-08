 /**
  * Mule LinkedIn Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.linkedin.config;
 
 import com.google.code.linkedinapi.client.LinkedInApiClient;
 import com.google.code.linkedinapi.client.Parameter;
 import com.google.code.linkedinapi.client.enumeration.ConnectionModificationType;
 import com.google.code.linkedinapi.client.enumeration.FacetField;
 import com.google.code.linkedinapi.client.enumeration.NetworkUpdateType;
 import com.google.code.linkedinapi.client.enumeration.ProfileField;
 import com.google.code.linkedinapi.client.enumeration.ProfileType;
 import com.google.code.linkedinapi.client.enumeration.SearchParameter;
 import com.google.code.linkedinapi.client.enumeration.SearchSortOrder;
 import com.google.code.linkedinapi.schema.FacetType;
 import com.google.code.linkedinapi.schema.VisibilityType;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Captor;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.mule.api.MuleContext;
 import org.mule.construct.Flow;
 import org.mule.module.linkedin.LinkedInClientFactory;
 import org.mule.tck.FunctionalTestCase;
 import org.mule.transport.http.HttpConnector;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.fest.assertions.MapAssert.entry;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.verify;
 
 public class LinkedInNamespaceHandlerTestCase extends FunctionalTestCase {
 
     private static final String ID = "some-id";
     private static final String NETWORK_UPDATE_ID = "some-network-update-id";
     private static final int START = 10;
     private static final int COUNT = 20;
     private static final String SUBJECT = "some-subject";
     private static final String MESSAGE = "some-message";
     private static final boolean SHOW_HIDDEN_MEMBERS = true;
     private static final Date START_DATE = new Date(2011 - 1900, 8 - 1, 10);
     private static final Date END_DATE = new Date(2011 - 1900, 8 - 1, 10);
     private static final Date MODIFICATION_DATE = new Date(2011 - 1900, 8 - 1, 20);
     private static final String HTTP_PORT = "9099";
     private Set<ProfileField> profileFields;
     private Set<NetworkUpdateType> networkUpdateTypes;
     private Set<FacetField> facetFields;
     private List<Parameter<FacetType, String>> facets;
     private Map<SearchParameter, String> searchParameters;
     @Mock
     private LinkedInApiClient mockClient;
     @Captor
     private ArgumentCaptor<List<Parameter<FacetType, String>>> facetsCaptor;
     @Captor
     private ArgumentCaptor<Set<ProfileField>> profileFieldsCaptor;
     @Captor
     private ArgumentCaptor<Set<NetworkUpdateType>> networkUpdateTypesCaptor;
     @Captor
     private ArgumentCaptor<Map<SearchParameter, String>> searchParametersCaptor;
     @Captor
     private ArgumentCaptor<Set<FacetField>> facetFieldsCaptor;
 
     @Override
     protected String getConfigResources() {
         return "linkedin-namespace-config.xml";
     }
 
     @Override
     protected MuleContext createMuleContext() throws Exception {
         System.setProperty("http.port", HTTP_PORT);
 
         MuleContext muleContext = super.createMuleContext();
         muleContext.getRegistry().registerObject("connector.http.mule.default", new HttpConnector(muleContext));
         return muleContext;
     }
 
     @Override
     protected void doSetUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         LinkedInClientFactory.setDefaultClient(mockClient);
 
         LinkedInConnectorOAuth1Adapter moduleObject = muleContext.getRegistry().lookupObject(LinkedInConnectorOAuth1Adapter.class);
         moduleObject.setAccessToken("");
         moduleObject.setAccessTokenSecret("");
         moduleObject.setOauthVerifier("");
 
         profileFields = new LinkedHashSet<ProfileField>(2);
         profileFields.add(ProfileField.LAST_NAME);
         profileFields.add(ProfileField.HONORS);
 
         networkUpdateTypes = new LinkedHashSet<NetworkUpdateType>(2);
         networkUpdateTypes.add(NetworkUpdateType.PROFILE_UPDATE);
         networkUpdateTypes.add(NetworkUpdateType.RECOMMENDATION_UPDATE);
 
         searchParameters = new LinkedHashMap<SearchParameter, String>(2);
         searchParameters.put(SearchParameter.CURRENT_COMPANY, "MuleSoft");
         searchParameters.put(SearchParameter.TITLE, "Engineer");
 
         facets = new ArrayList<Parameter<FacetType, String>>(2);
         facets.add(new Parameter<FacetType, String>(FacetType.INDUSTRY, "Software"));
         facets.add(new Parameter<FacetType, String>(FacetType.PAST_COMPANY, "MuleSource"));
 
         facetFields = new LinkedHashSet<FacetField>(2);
         facetFields.add(FacetField.BUCKET_NAME);
         facetFields.add(FacetField.BUCKET_CODE);
     }
 
     public void testGetProfileForCurrentUser() throws Exception {
         runFlow("GetProfileForCurrentUser");
         verify(mockClient).getProfileForCurrentUser(profileFieldsCaptor.capture());
         assertThat(profileFieldsCaptor.getValue()).containsOnly(profileFields.toArray());
     }
 
     public void testGetProfileById() throws Exception {
         runFlow("GetProfileById");
         verify(mockClient).getProfileById(eq(ID), profileFieldsCaptor.capture());
         assertThat(profileFieldsCaptor.getValue()).containsOnly(profileFields.toArray());
     }
 
     public void testGetProfileByUrl() throws Exception {
         runFlow("GetProfileByUrl");
         verify(mockClient).getProfileByUrl("some-url", ProfileType.STANDARD);
     }
 
     public void testGetNetworkUpdates() throws Exception {
         runFlow("GetNetworkUpdates");
         verify(mockClient).getNetworkUpdates(networkUpdateTypesCaptor.capture(), eq(START), eq(COUNT), eq(START_DATE), eq(END_DATE), eq(SHOW_HIDDEN_MEMBERS));
         assertThat(networkUpdateTypesCaptor.getValue()).containsOnly(networkUpdateTypes.toArray());
     }
 
     public void testGetUserUpdates() throws Exception {
         runFlow("GetUserUpdates");
         verify(mockClient).getUserUpdates(networkUpdateTypesCaptor.capture(), eq(START), eq(COUNT), eq(START_DATE), eq(END_DATE));
         assertThat(networkUpdateTypesCaptor.getValue()).containsOnly(networkUpdateTypes.toArray());
     }
 
     public void testGetUserUpdatesById() throws Exception {
         runFlow("GetUserUpdatesById");
         verify(mockClient).getUserUpdates(eq(ID), networkUpdateTypesCaptor.capture(), eq(START), eq(COUNT), eq(START_DATE), eq(END_DATE));
         assertThat(networkUpdateTypesCaptor.getValue()).containsOnly(networkUpdateTypes.toArray());
     }
 
     public void testGetNetworkUpdateComments() throws Exception {
         runFlow("GetNetworkUpdateComments");
         verify(mockClient).getNetworkUpdateComments(NETWORK_UPDATE_ID);
     }
 
     public void testGetNetworkUpdateLikes() throws Exception {
         runFlow("GetNetworkUpdateLikes");
         verify(mockClient).getNetworkUpdateLikes(NETWORK_UPDATE_ID);
     }
 
     public void testGetConnectionsForCurrentUser() throws Exception {
         runFlow("GetConnectionsForCurrentUser");
         verify(mockClient).getConnectionsForCurrentUser(profileFieldsCaptor.capture(), eq(START), eq(COUNT), eq(MODIFICATION_DATE), eq(ConnectionModificationType.NEW));
         assertThat(profileFieldsCaptor.getValue()).containsOnly(profileFields.toArray());
     }
 
     public void testGetConnectionsById() throws Exception {
         runFlow("GetConnectionsById");
         verify(mockClient).getConnectionsById(eq(ID), profileFieldsCaptor.capture(), eq(START), eq(COUNT), eq(MODIFICATION_DATE), eq(ConnectionModificationType.NEW));
         assertThat(profileFieldsCaptor.getValue()).containsOnly(profileFields.toArray());
     }
 
     public void testGetConnectionsByUrl() throws Exception {
         runFlow("GetConnectionsByUrl");
         verify(mockClient).getConnectionsByUrl(eq("some-url"), profileFieldsCaptor.capture(), eq(START), eq(COUNT), eq(MODIFICATION_DATE), eq(ConnectionModificationType.NEW));
         assertThat(profileFieldsCaptor.getValue()).containsOnly(profileFields.toArray());
     }
 
     public void testSearchPeople() throws Exception {
         runFlow("SearchPeople");
         verify(mockClient).searchPeople(searchParametersCaptor.capture(), profileFieldsCaptor.capture(), eq(START), eq(COUNT), eq(SearchSortOrder.RECOMMENDERS));
         verifySearchParameters();
         assertThat(profileFieldsCaptor.getValue()).containsOnly(profileFields.toArray());
     }
 
     public void testSearchPeopleWithFacets() throws Exception {
         runFlow("SearchPeopleWithFacets");
         verify(mockClient).searchPeople(searchParametersCaptor.capture(), profileFieldsCaptor.capture(), eq(START), eq(COUNT), eq(SearchSortOrder.RECOMMENDERS), facetsCaptor.capture());
         verifySearchParameters();
         assertThat(profileFieldsCaptor.getValue()).containsOnly(profileFields.toArray());
         assertThat(facetsCaptor.getValue()).containsOnly(facets.toArray());
     }
 
     public void testSearchPeopleWithFacetFields() throws Exception {
         runFlow("SearchPeopleWithFacetFields");
         verify(mockClient).searchPeople(searchParametersCaptor.capture(), profileFieldsCaptor.capture(), facetFieldsCaptor.capture(), eq(START), eq(COUNT), eq(SearchSortOrder.RECOMMENDERS), facetsCaptor.capture());
         verifySearchParameters();
         assertThat(facetFieldsCaptor.getValue()).containsOnly(facetFields.toArray());
         assertThat(profileFieldsCaptor.getValue()).containsOnly(profileFields.toArray());
         assertThat(facetsCaptor.getValue()).containsOnly(facets.toArray());
     }
 
     public void testPostNetworkUpdate() throws Exception {
         runFlow("PostNetworkUpdate");
         verify(mockClient).postNetworkUpdate("some-update-text");
     }
 
     public void testPostComment() throws Exception {
         runFlow("PostComment");
         verify(mockClient).postComment(NETWORK_UPDATE_ID, "some-comment-text");
     }
 
     public void testLikePost() throws Exception {
         runFlow("LikePost");
         verify(mockClient).likePost(NETWORK_UPDATE_ID);
     }
 
     public void testUnlikePost() throws Exception {
         runFlow("UnlikePost");
         verify(mockClient).unlikePost(NETWORK_UPDATE_ID);
     }
 
     public void testUpdateCurrentStatus() throws Exception {
         runFlow("UpdateCurrentStatus");
         verify(mockClient).updateCurrentStatus("new-status", true);
     }
 
     public void testDeleteCurrentStatus() throws Exception {
         runFlow("DeleteCurrentStatus");
         verify(mockClient).deleteCurrentStatus();
     }
 
     public void testSendMessage() throws Exception {
         runFlow("SendMessage");
         verify(mockClient).sendMessage(Arrays.asList("recipientId1", "recipientId2"), SUBJECT, MESSAGE);
     }
 
     public void testSendInviteByEmail() throws Exception {
         runFlow("SendInviteByEmail");
         verify(mockClient).sendInviteByEmail("some-email", "some-name", "some-last-name", SUBJECT, MESSAGE);
     }
 
     public void testPostShare() throws Exception {
         runFlow("PostShare");
         verify(mockClient).postShare("some-comment", "some-title", "some-url", "some-image-url", VisibilityType.ALL_MEMBERS, true);
     }
 
     public void testReShare() throws Exception {
         runFlow("ReShare");
         verify(mockClient).reShare("some-share-id", "some-comment", VisibilityType.ALL_MEMBERS);
     }
 
     private void runFlow(String flowName) throws Exception {
         Flow flowConstruct = (Flow) muleContext.getRegistry().lookupFlowConstruct(flowName);
         flowConstruct.process(getTestEvent(""));
     }
 
     private void verifySearchParameters() {
         assertEquals(searchParameters.size(), searchParametersCaptor.getValue().size());
         for (Map.Entry<SearchParameter, String> entry : searchParameters.entrySet()) {
             assertThat(searchParametersCaptor.getValue()).includes(entry(entry.getKey(), entry.getValue()));
         }
     }
 }
