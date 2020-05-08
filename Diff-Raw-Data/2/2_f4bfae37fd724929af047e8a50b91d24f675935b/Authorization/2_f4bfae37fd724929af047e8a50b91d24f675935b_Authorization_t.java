 package org.fiteagle.interactors.sfa.common;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public class Authorization {
 	
 	Boolean authorized=false;
 	private Boolean credentialTypesAndVersionValid=false;
 	private String authorizationFailMessage="";
 	private AMCode returnCode;
 	
 	
 	public static final String GENI_VERSION = "3";
 	public static final String GENI_TYPE = "geni_sfa";
 	GeniCredentialType credentialType;
 	
 	public Authorization() {
 		this.credentialType = new GeniCredentialType();
 		credentialType.setGeni_type(GENI_TYPE);
 		credentialType.setGeni_version(GENI_VERSION);
 	}
 
 	public GeniCredentialType getCredentialType() {
 		return credentialType;
 	}
 
 	public void setCredentialType(GeniCredentialType credentialType) {
 		this.credentialType = credentialType;
 	}
 	
 	public void checkCredentialsList(ListCredentials listCredentials) {
 		
 		ArrayList<Credentials> credentialList = listCredentials.getCredentialsList();
		for (Iterator<Credentials> iterator = credentialList.iterator(); iterator.hasNext();) {
 			Credentials credentials = (Credentials) iterator.next();
 			this.checkCredentialTypeAndVersion(credentials);
 			if (!this.areCredentialTypeAndVersionValid()) {
 				return;
 			}
 			//TODO: check credentials in Credentials.geni_value!!
 		}
 		
 	}
 	
 	
 
 	public boolean areCredentialTypeAndVersionValid() {
 		return getCredentialTypesAndVersionValid();
 	}
 
 	private void checkCredentialTypeAndVersion(Credentials credentials) {
 		if (credentials.getGeni_type().compareToIgnoreCase(GENI_TYPE)==0&&credentials.getGeni_version().compareToIgnoreCase(GENI_VERSION)==0){
 			this.setCredentialTypesAndVersionValid(true);
 			return;
 		}
 		
 		if(credentials.getGeni_type().compareToIgnoreCase(GENI_TYPE)!=0)
 			this.setAuthorizationFailMessage("The GENI_TYPE: "+credentials.getGeni_type()+" is not supported, please use another one (e. g. "+this.GENI_TYPE+"). ");
 		
 		if(credentials.getGeni_value().compareToIgnoreCase(GENI_VERSION)!=0)
 			this.setAuthorizationFailMessage(this.getAuthorizationFailMessage()+"The GENI_VERSION: "+credentials.getGeni_version()+" is not supported, please use another one (e. g. "+this.GENI_VERSION+")");
 		
 		this.setReturnCode(new AMCode());
 		this.getReturnCode().setGeni_code(GENI_CodeEnum.UNSUPPORTED);
 		
 		this.setCredentialTypesAndVersionValid(false);
 	}
 
 	public String getAuthorizationFailMessage() {
 		return authorizationFailMessage;
 	}
 
 	public void setAuthorizationFailMessage(String authorizationFailMessage) {
 		this.authorizationFailMessage = authorizationFailMessage;
 	}
 
 	public Boolean getCredentialTypesAndVersionValid() {
 		return credentialTypesAndVersionValid;
 	}
 
 	private void setCredentialTypesAndVersionValid(
 			Boolean credentialTypesAndVersionValid) {
 		this.credentialTypesAndVersionValid = credentialTypesAndVersionValid;
 	}
 
 	public AMCode getReturnCode() {
 		return returnCode;
 	}
 
 	private void setReturnCode(AMCode returnCode) {
 		this.returnCode = returnCode;
 	}
 
 }
