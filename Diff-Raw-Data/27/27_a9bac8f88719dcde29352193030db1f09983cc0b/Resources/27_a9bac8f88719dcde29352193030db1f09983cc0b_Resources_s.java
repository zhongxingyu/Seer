 package org.iskconsv.client.resources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.ExternalTextResource;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.resources.client.TextResource;
 
 public interface Resources extends ClientBundle
 {
 	public static final Resources INSTANCE = GWT.create(Resources.class);
 	Style style();
 
 	@Source("logo.png")
 	ImageResource logo();
 	
 	@Source("peacock.jpg")
 	ImageResource peacock();
 	
 	@Source("errorMessage.txt")
 	TextResource errorMessage();
 	
 	@Source("contact.html")
 	ExternalTextResource contact();
 
 	@Source("iskcon.html")
 	ExternalTextResource iskcon();
 
 	@Source("founder.html")
 	ExternalTextResource founder();
 	
 	@Source("newTemple.html")
 	ExternalTextResource newTemple();
 	
 	@Source("sankirtanDonation.html")
 	ExternalTextResource sankirtanDonation();
 	
 	@Source("templeOpsDonation.html")
 	ExternalTextResource templeOpsDonation();
 	
 	@Source("motelSankirtan.html")
 	ExternalTextResource motelSankirtan();
 	
 	@Source("donateElectronicInstructions.html")
 	ExternalTextResource donateElectronicInstructions();
 	
 	@Source("donateInPersonInstructions.html")
 	ExternalTextResource donateInPersonInstructions();
 }
