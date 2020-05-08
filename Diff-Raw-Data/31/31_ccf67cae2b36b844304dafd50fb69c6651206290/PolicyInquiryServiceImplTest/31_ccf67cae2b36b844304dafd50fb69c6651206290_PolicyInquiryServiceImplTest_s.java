 package com.cgu.service;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 
import PolicyInquiryV1.PolicySearchCriteria;
import PolicyInquiryV1.Reference;
 import PolicyInquiryV1.RetrieveInsurancePoliciesReq;

 public class PolicyInquiryServiceImplTest
 {
     @Test
     public void testPolicyInquiry()
     {
         PolicyInquiry inquiry = new PolicyInquiryServiceImpl();
        RetrieveInsurancePoliciesReq request = Mockito.mock(RetrieveInsurancePoliciesReq.class);
         inquiry.retrieveInsurancePolicies(request);
     }
 }
