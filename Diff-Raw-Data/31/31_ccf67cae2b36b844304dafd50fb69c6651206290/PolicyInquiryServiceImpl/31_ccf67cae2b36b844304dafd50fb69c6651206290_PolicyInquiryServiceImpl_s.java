 package com.cgu.service;
 
 import PolicyInquiryV1.RetrieveInsurancePoliciesReq;
 import PolicyInquiryV1.RetrieveInsurancePoliciesResp;
 import PolicyInquiryV1.RetrievePolicyDetailsReq;
 import PolicyInquiryV1.RetrievePolicyDetailsResp;
 import dataservice.policyinquirytopmscache.v1.PolicyInquiryToPMSCacheV1;
import dataservice.policyinquirytopmscache.v1.PolicyInquiryToPMSCacheV1SDO;

 
 /**
  * Implementation of PolicyInquiry.
  * 
  * @author s28109
  * 
  */
 public class PolicyInquiryServiceImpl implements PolicyInquiry
 {
    /**  {@inheritDoc}
      */
     @Override
     public RetrievePolicyDetailsResp retrievePolicyDetails(
             RetrievePolicyDetailsReq request)
     {
                
         // Route to DXSI
         PolicyInquiryToPMSCacheV1 service = new PolicyInquiryToPMSCacheV1();
        
        
         return service.retrievePolicyDetails(request);
     }
 
     /**
      * {@inheritDoc}
      * 
      */
     @Override
     public RetrieveInsurancePoliciesResp retrieveInsurancePolicies(
             RetrieveInsurancePoliciesReq request)
     {
         PolicyInquiryToPMSCacheV1 service = new PolicyInquiryToPMSCacheV1();
        RetrieveInsurancePoliciesResp resp = service.retrieveInsurancePolicies(request);
         resp.getXml();
         service.getLastRuntimeReport().getXml();
         return resp;
       
     }
 }
