 package org.motechproject.ananya.referencedata.web.controller;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ananya.referencedata.contactCenter.request.FrontLineWorkerWebRequest;
 import org.motechproject.ananya.referencedata.contactCenter.service.FrontLineWorkerService;
 import org.motechproject.ananya.referencedata.flw.domain.VerificationStatus;
 import org.motechproject.ananya.referencedata.flw.response.BaseResponse;
 import org.motechproject.ananya.referencedata.web.utils.TestUtils;
 import org.springframework.http.MediaType;
 import org.springframework.test.web.server.MvcResult;
 import org.springframework.test.web.server.ResultMatcher;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ananya.referencedata.web.utils.MVCTestUtils.mockMvc;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
 
 public class FrontLineWorkerControllerTest {
 
     @Mock
     private FrontLineWorkerService frontLineWorkerService;
 
     private FrontLineWorkerController frontLineWorkerController;
 
     @Before
     public void setUp() {
         initMocks(this);
         frontLineWorkerController = new FrontLineWorkerController(frontLineWorkerService);
     }
 
     @Test
     public void shouldUpdateTheStatusForAValidFLWRequestXml() throws Exception {
         FrontLineWorkerWebRequest frontLineWorkerWebRequest = new FrontLineWorkerWebRequest("flwId", VerificationStatus.INVALID.name(), "Invalid User");
 
         postFLWRequestXml(frontLineWorkerWebRequest, BaseResponse.success(), status().isOk());
 
         ArgumentCaptor<FrontLineWorkerWebRequest> captor = ArgumentCaptor.forClass(FrontLineWorkerWebRequest.class);
         verify(frontLineWorkerService).updateVerifiedFlw(captor.capture());
         FrontLineWorkerWebRequest deserializedFrontLineWorkerWebRequest = captor.getValue();
         assertEquals(frontLineWorkerWebRequest, deserializedFrontLineWorkerWebRequest);
     }
 
     @Test
     public void shouldReturnValidationErrorForAnInvalidFLWRequestXml() throws Exception {
         FrontLineWorkerWebRequest frontLineWorkerWebRequest = new FrontLineWorkerWebRequest("flwId", null, "Invalid User");
 
        postFLWRequestXml(frontLineWorkerWebRequest, BaseResponse.failure("Verification-Status field has invalid/blank value"), status().isBadRequest());
         verify(frontLineWorkerService, never()).updateVerifiedFlw(any(FrontLineWorkerWebRequest.class));
     }
 
     @Test
     public void shouldUpdateTheStatusForAValidFLWRequestJson() throws Exception {
         FrontLineWorkerWebRequest frontLineWorkerWebRequest = new FrontLineWorkerWebRequest("flwId", VerificationStatus.INVALID.name(), "Invalid User");
 
         postFlwRequestJson(frontLineWorkerWebRequest, BaseResponse.success(), status().isOk());
 
         ArgumentCaptor<FrontLineWorkerWebRequest> captor = ArgumentCaptor.forClass(FrontLineWorkerWebRequest.class);
         verify(frontLineWorkerService).updateVerifiedFlw(captor.capture());
         FrontLineWorkerWebRequest deserializedFrontLineWorkerWebRequest = captor.getValue();
         assertEquals(frontLineWorkerWebRequest, deserializedFrontLineWorkerWebRequest);
     }
 
     @Test
     public void shouldReturnValidationErrorForAnInvalidFLWRequestJson() throws Exception {
         String flwId = "flwId";
         String reason = "Invalid User";
         FrontLineWorkerWebRequest frontLineWorkerWebRequest = new FrontLineWorkerWebRequest(flwId, null, reason);
 
        postFlwRequestJson(frontLineWorkerWebRequest, BaseResponse.failure("Verification-Status field has invalid/blank value"), status().isBadRequest());
         verify(frontLineWorkerService, never()).updateVerifiedFlw(any(FrontLineWorkerWebRequest.class));
     }
 
     private void postFLWRequestXml(FrontLineWorkerWebRequest frontLineWorkerWebRequest, BaseResponse expectedResponse, ResultMatcher statusMatcher) throws Exception {
         MvcResult result = mockMvc(frontLineWorkerController)
                 .perform(post("/flw").body(TestUtils.toXml(FrontLineWorkerWebRequest.class, frontLineWorkerWebRequest).getBytes())
                         .contentType(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML))
                 .andExpect(statusMatcher)
                 .andExpect(content().type("application/xml"))
                 .andReturn();
 
         String responseString = result.getResponse().getContentAsString();
         BaseResponse actualResponse = TestUtils.fromXml(BaseResponse.class, responseString);
         Assert.assertEquals(expectedResponse, actualResponse);
     }
 
     private void postFlwRequestJson(FrontLineWorkerWebRequest frontLineWorkerWebRequest, BaseResponse expectedResponse, ResultMatcher statusMatcher) throws Exception {
         MvcResult result = mockMvc(frontLineWorkerController)
                 .perform(post("/flw").body(TestUtils.toJson(frontLineWorkerWebRequest).getBytes())
                         .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                 .andExpect(statusMatcher)
                 .andExpect(content().type("application/json"))
                 .andReturn();
 
         String responseString = result.getResponse().getContentAsString();
         BaseResponse actualResponse = TestUtils.fromJson(BaseResponse.class, responseString);
         Assert.assertEquals(expectedResponse, actualResponse);
     }
 }
