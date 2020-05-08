 package com.cgu.service;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import PolicyInquiryV1.RetrieveInsurancePoliciesReq;
//CHECKSTYLE:OFF
 public class PolicyInquiryServiceImplTest
 {
     @Test
     public void testPolicyInquiry()
     {
         PolicyInquiry inquiry = new PolicyInquiryServiceImpl();
        RetrieveInsurancePoliciesReq request = Mockito
                .mock(RetrieveInsurancePoliciesReq.class);
         inquiry.retrieveInsurancePolicies(request);
     }
 }
