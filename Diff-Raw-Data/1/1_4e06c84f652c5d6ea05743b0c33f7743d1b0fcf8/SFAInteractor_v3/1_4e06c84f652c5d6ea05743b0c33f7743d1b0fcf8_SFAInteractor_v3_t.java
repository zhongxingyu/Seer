 package org.fiteagle.interactors.sfa;
 
 import java.io.IOException;
 import java.security.cert.CertificateParsingException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.fiteagle.core.aaa.CertificateAuthority;
 import org.fiteagle.interactors.sfa.allocate.AllocateOptions;
 import org.fiteagle.interactors.sfa.allocate.AllocateRequestProcessor;
 import org.fiteagle.interactors.sfa.allocate.AllocateResult;
 import org.fiteagle.interactors.sfa.common.AMCode;
 import org.fiteagle.interactors.sfa.common.ListCredentials;
 import org.fiteagle.interactors.sfa.common.SFARequestProcessorFactory;
 import org.fiteagle.interactors.sfa.common.SFAv3MethodsEnum;
 import org.fiteagle.interactors.sfa.delete.DeleteOptions;
 import org.fiteagle.interactors.sfa.delete.DeleteRequestProcessor;
 import org.fiteagle.interactors.sfa.delete.DeleteResult;
 import org.fiteagle.interactors.sfa.describe.DescribeOptions;
 import org.fiteagle.interactors.sfa.describe.DescribeRequestProcessor;
 import org.fiteagle.interactors.sfa.describe.DescribeResult;
 import org.fiteagle.interactors.sfa.getSelfCredential.GetSelfCredentialRequestProcessor;
 import org.fiteagle.interactors.sfa.getversion.GetVersionRequestProcessor;
 import org.fiteagle.interactors.sfa.getversion.GetVersionResult;
 import org.fiteagle.interactors.sfa.listresources.ListResourceOptions;
 import org.fiteagle.interactors.sfa.listresources.ListResourceRequestProcessor;
 import org.fiteagle.interactors.sfa.listresources.ListResourcesResult;
 import org.fiteagle.interactors.sfa.provision.ProvisionOptions;
 import org.fiteagle.interactors.sfa.provision.ProvisionRequestProcessor;
 import org.fiteagle.interactors.sfa.provision.ProvisionResult;
 import org.fiteagle.interactors.sfa.rspec.RSpecContents;
 import org.fiteagle.interactors.sfa.status.StatusOptions;
 import org.fiteagle.interactors.sfa.status.StatusRequestProcessor;
 import org.fiteagle.interactors.sfa.status.StatusResult;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SFAInteractor_v3 implements ISFA {
 
 	
 	private final SFARequestProcessorFactory requestProcessorFactor = SFARequestProcessorFactory.getInstance();
 	private final Logger log = LoggerFactory.getLogger(this.getClass());
 	
 	private X509Certificate certificate;
 	
 	@Override
 	public GetVersionResult getVersion() throws IOException {
 		
 		GetVersionRequestProcessor getVersionRequestProcessor = requestProcessorFactor.createRequestProcessor(SFAv3MethodsEnum.GET_VERSION);
 		
 		final GetVersionResult getVersionResult = getVersionRequestProcessor.processRequest();		
 		return getVersionResult;
 	}
 
 	@Override
 	public ListResourcesResult listResources(ListCredentials credentials,
 			ListResourceOptions listResourceOptions) throws IOException {
 		
 	
 		ListResourceRequestProcessor listResourceRequestProcessor = requestProcessorFactor.createRequestProcessor(SFAv3MethodsEnum.LIST_RESOURCES);
 		ListResourcesResult result = listResourceRequestProcessor.processRequest(credentials, listResourceOptions);
 		
 		AMCode returnCode = result.getCode();
 		//Do something
 		
 		return result;
 		
 	}
 	
 	@Override
 	public DescribeResult describe(ArrayList<String> urns, ListCredentials credentials,
 			DescribeOptions describeOptions) throws IOException {
 		
 		SFARequestProcessorFactory sfaRequestProcFactory = new SFARequestProcessorFactory();
 		DescribeRequestProcessor describeRequestProcessor = sfaRequestProcFactory.createRequestProcessor(SFAv3MethodsEnum.DESCRIBE);
 		DescribeResult result = describeRequestProcessor.processRequest(urns, credentials, describeOptions);
 		
 		return result;
 		
 	}
 
   @Override
   public String resolve(Object o1, Object o2) {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public String getSelfCredential(String certificate, String xrn, String type) {
 	  SFARequestProcessorFactory sfaRequestProcFactory = new SFARequestProcessorFactory();
 	  GetSelfCredentialRequestProcessor getSelfCredentialRequestProcessor = sfaRequestProcFactory.createRequestProcessor(SFAv3MethodsEnum.GET_SELF_CREDENTIAL);
 	  String result = getSelfCredentialRequestProcessor.processRequest(certificate, xrn, type);
 	  log.info(result);
 	  return result;
   }
 
   @Override
   public String getCredential(String credential, String xrn, String type) {
     log.info("GetCredential");
     log.info(credential);
     log.info("target: " + xrn);
     log.info("type: "+ type);
     return credential;
 //    return "";
   }
   
   @Override
   public String getCredential() {
     if(this.certificate!=null){
       Collection<List<String>> alternativeNames;
       Collection<List<?>> alternativeNamesCollection;
       try {
         alternativeNamesCollection = certificate.getSubjectAlternativeNames();
 //        alterna
       } catch (CertificateParsingException e) {
         e.printStackTrace();
         throw new RuntimeException();//TODO: specify this.
       }
       
       Iterator<?> iter =  alternativeNamesCollection.iterator();
       String urn = "";
       while(iter.hasNext()){
         List<?> altName = (List<?>) iter.next();
         if (altName.get(0).equals(Integer.valueOf(6))) {
           urn = (String) altName.get(1);
         }
       }
       
       try {
         return getSelfCredential(CertificateAuthority.getInstance().getCertificateBodyEncoded(certificate), urn, "user");
       } catch (Exception e) {
         e.printStackTrace();
         throw new RuntimeException();//TODO: specify this.
       }
     }
     return null;
   }
   
   @Override
   public AllocateResult allocate(String urn, ListCredentials credentials, RSpecContents requestRspec, AllocateOptions allocateOptions) throws IOException {
     
     SFARequestProcessorFactory sfaRequestProcFactory = new SFARequestProcessorFactory();
     AllocateRequestProcessor allocateRequestProcessor = sfaRequestProcFactory.createRequestProcessor(SFAv3MethodsEnum.ALLOCATE);
     AllocateResult result = allocateRequestProcessor.processRequest(urn, credentials, requestRspec, allocateOptions);
     return result;
     
   }
   
   @Override
   public ProvisionResult provision(ArrayList<String> urns, ListCredentials credentials, ProvisionOptions provisionOptions) throws IOException {
     
     SFARequestProcessorFactory sfaRequestProcFactory = new SFARequestProcessorFactory();
     ProvisionRequestProcessor provisionRequestProcessor = sfaRequestProcFactory.createRequestProcessor(SFAv3MethodsEnum.PROVISION);
     ProvisionResult result = provisionRequestProcessor.processRequest(urns, credentials, provisionOptions);
     return result;
     
   }
   
   @Override
   public StatusResult status(ArrayList<String> urns, ListCredentials credentials,
       StatusOptions statusOptions) throws IOException {
     
     SFARequestProcessorFactory sfaRequestProcFactory = new SFARequestProcessorFactory();
     StatusRequestProcessor statusRequestProcessor = sfaRequestProcFactory.createRequestProcessor(SFAv3MethodsEnum.STATUS);
     StatusResult result = statusRequestProcessor.processRequest(urns, credentials, statusOptions);
     
     return result;
     
   }
 
   @Override
   public DeleteResult delete(ArrayList<String> urns, ListCredentials credentials,
       DeleteOptions deleteOptions) throws IOException {
     
     SFARequestProcessorFactory sfaRequestProcFactory = new SFARequestProcessorFactory();
     DeleteRequestProcessor deleteRequestProcessor = sfaRequestProcFactory.createRequestProcessor(SFAv3MethodsEnum.DELETE);
     DeleteResult result = deleteRequestProcessor.processRequest(urns, credentials, deleteOptions);
     
     return result;
     
   }
 
   public X509Certificate getCertificate() {
     return certificate;
   }
 
   public void setCertificate(X509Certificate certificate) {
     this.certificate = certificate;
   }
 
 
 
 
 }
