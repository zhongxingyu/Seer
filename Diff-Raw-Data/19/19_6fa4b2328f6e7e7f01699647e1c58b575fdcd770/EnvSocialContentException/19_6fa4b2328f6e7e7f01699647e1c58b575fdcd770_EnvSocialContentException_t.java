 package com.envsocial.android.api.exceptions;
 
 import com.envsocial.android.api.EnvSocialResource;
 
 public class EnvSocialContentException extends EnvSocialException {
 	protected String mContent;
 	protected EnvSocialResource mResource;
 	
 	public EnvSocialContentException(String jsonContent, EnvSocialResource resource, Throwable cause) {
 		super(cause);
 		
 		mContent = jsonContent;
 		mResource = resource;
 	}
 	
 	public String getContent() {
 		return mContent;
 	}
 
 	@Override
 	public String getMessage() {
		String info = "Error parsing content for resource :: " + mResource.getName();
 		return info;
 	}
 
 }
