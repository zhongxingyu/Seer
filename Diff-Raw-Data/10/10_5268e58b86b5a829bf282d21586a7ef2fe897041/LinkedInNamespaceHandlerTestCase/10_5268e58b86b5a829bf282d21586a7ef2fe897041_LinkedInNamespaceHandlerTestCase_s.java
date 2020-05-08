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
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.mule.api.MuleContext;
import org.mule.construct.SimpleFlowConstruct;
 import org.mule.module.linkedin.LinkedInClientFactory;
 import org.mule.tck.FunctionalTestCase;
 import org.mule.transport.http.HttpConnector;
 
import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
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
     private Set<ProfileField> profileFields;
     private Set<NetworkUpdateType> networkUpdateTypes;
     private Set<FacetField> facetFields;
     private List<Parameter<FacetType, String>> facets;
     private Map<SearchParameter, String> searchParameters;
     @Mock
     private LinkedInApiClient mockClient;
 
     @Override
     protected String getConfigResources() {
         return "linkedin-namespace-config.xml";
     }
 
     @Override
     protected MuleContext createMuleContext() throws Exception {
         MuleContext muleContext = super.createMuleContext();
         muleContext.getRegistry().registerObject("connector.http.mule.default", new HttpConnector(muleContext));
         return muleContext;
     }
 
     @Override
     protected void doSetUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         LinkedInClientFactory.setDefaultClient(mockClient);
 
        LinkedInConnectorOAuthAdapter moduleObject = muleContext.getRegistry().lookupObject(LinkedInConnectorOAuthAdapter.class);
         moduleObject.setAccessToken("");
         moduleObject.setAccessTokenSecret("");
         moduleObject.setOauthVerifier("");
 
         profileFields = new HashSet<ProfileField>(2);
         profileFields.add(ProfileField.LAST_NAME);
         profileFields.add(ProfileField.HONORS);
 
         networkUpdateTypes = new HashSet<NetworkUpdateType>(2);
         networkUpdateTypes.add(NetworkUpdateType.PROFILE_UPDATE);
         networkUpdateTypes.add(NetworkUpdateType.RECOMMENDATION_UPDATE);
 
         searchParameters = new HashMap<SearchParameter, String>(2);
         searchParameters.put(SearchParameter.CURRENT_COMPANY, "MuleSoft");
         searchParameters.put(SearchParameter.TITLE, "Engineer");
 
         facets = new ArrayList<Parameter<FacetType, String>>(2);
         facets.add(new Parameter<FacetType, String>(FacetType.INDUSTRY, "Software"));
         facets.add(new Parameter<FacetType, String>(FacetType.PAST_COMPANY, "MuleSource"));
 
         facetFields = new HashSet<FacetField>(2);
         facetFields.add(FacetField.BUCKET_NAME);
         facetFields.add(FacetField.BUCKET_CODE);
     }
 
     public void testGetProfileForCurrentUser() throws Exception {
         runFlow("GetProfileForCurrentUser");
         verify(mockClient).getProfileForCurrentUser(profileFields);
     }
 
     public void testGetProfileById() throws Exception {
         runFlow("GetProfileById");
         verify(mockClient).getProfileById(ID, profileFields);
     }
 
     public void testGetProfileByUrl() throws Exception {
         runFlow("GetProfileByUrl");
         verify(mockClient).getProfileByUrl("some-url", ProfileType.STANDARD);
     }
 
     public void testGetNetworkUpdates() throws Exception {
         runFlow("GetNetworkUpdates");
         verify(mockClient).getNetworkUpdates(networkUpdateTypes, START, COUNT, START_DATE, END_DATE, SHOW_HIDDEN_MEMBERS);
     }
 
     public void testGetUserUpdates() throws Exception {
         runFlow("GetUserUpdates");
         verify(mockClient).getUserUpdates(networkUpdateTypes, START, COUNT, START_DATE, END_DATE);
     }
 
     public void testGetUserUpdatesById() throws Exception {
         runFlow("GetUserUpdatesById");
         verify(mockClient).getUserUpdates(ID, networkUpdateTypes, START, COUNT, START_DATE, END_DATE);
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
         verify(mockClient).getConnectionsForCurrentUser(profileFields, START, COUNT, MODIFICATION_DATE, ConnectionModificationType.NEW);
     }
 
     public void testGetConnectionsById() throws Exception {
         runFlow("GetConnectionsById");
         verify(mockClient).getConnectionsById(ID, profileFields, START, COUNT, MODIFICATION_DATE, ConnectionModificationType.NEW);
     }
 
     public void testGetConnectionsByUrl() throws Exception {
         runFlow("GetConnectionsByUrl");
         verify(mockClient).getConnectionsByUrl("some-url", profileFields, START, COUNT, MODIFICATION_DATE, ConnectionModificationType.NEW);
     }
 
     public void testSearchPeople() throws Exception {
         runFlow("SearchPeople");
         verify(mockClient).searchPeople(searchParameters, profileFields, START, COUNT, SearchSortOrder.RECOMMENDERS);
     }
 
     public void testSearchPeopleWithFacets() throws Exception {
         runFlow("SearchPeopleWithFacets");
         verify(mockClient).searchPeople(searchParameters, profileFields, START, COUNT, SearchSortOrder.RECOMMENDERS, facets);
     }
 
     public void testSearchPeopleWithFacetFields() throws Exception {
         runFlow("SearchPeopleWithFacetFields");
         verify(mockClient).searchPeople(searchParameters, profileFields, facetFields, START, COUNT, SearchSortOrder.RECOMMENDERS, facets);
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
        SimpleFlowConstruct flowConstruct = (SimpleFlowConstruct) muleContext.getRegistry().lookupFlowConstruct(flowName);
         flowConstruct.process(getTestEvent(""));
     }
 }
