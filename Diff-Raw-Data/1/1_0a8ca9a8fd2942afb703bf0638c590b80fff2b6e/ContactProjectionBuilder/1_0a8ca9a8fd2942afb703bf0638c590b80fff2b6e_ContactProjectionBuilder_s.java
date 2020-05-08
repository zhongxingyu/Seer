 package com.utopia.lijiang.contacts;
 
 import java.util.ArrayList;
 
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.CommonDataKinds.Photo;
 
 public class ContactProjectionBuilder {
 	
 	static final String[] PHONES_PROJECTION = 
 			new String[] {Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,Phone.CONTACT_ID };	
 	static final int PHONES_DISPLAY_NAME_INDEX = 0;  
     static final int PHONES_NUMBER_INDEX = 1;   
     static final int PHONES_PHOTO_ID_INDEX = 2;   
     static final int PHONES_CONTACT_ID_INDEX = 3;  
 	
     
 	private Boolean[] includeField = new Boolean[4];
 	
 	public void inlcudeDisplayName(Boolean value){
 		 includeField[PHONES_DISPLAY_NAME_INDEX] = value;
 	}
 		
 	public void inlcudeNumber(Boolean value){
 		includeField[PHONES_NUMBER_INDEX] = value;
 	}
 	
 	public void inlcudePhotoId(Boolean value){
 		includeField[PHONES_PHOTO_ID_INDEX] = value;
 	}
 
 	public void inlcudeContactId(Boolean value){
 		includeField[PHONES_CONTACT_ID_INDEX] = value;
 	}
 
 	public Boolean isIncludeDisplayName(){
 		return includeField[PHONES_DISPLAY_NAME_INDEX];
 	}
 	
 	public Boolean isIncludeNumber(){
 		return includeField[PHONES_NUMBER_INDEX];
 	}
 	
 	public Boolean isIncludePhotoId(){
 		return includeField[PHONES_PHOTO_ID_INDEX];
 	}
 	
 	public Boolean isIncludeContanctId(){
 		return includeField[PHONES_CONTACT_ID_INDEX];
 	}
 	
 	
 	public String[] getProjection() {
 		ArrayList<String> projection = new ArrayList<String>();
 		
 		int index=0;
 		while(index< includeField.length){
 			if(includeField[index]){
 				projection.add(PHONES_PROJECTION[index]);
 			}
 		}
 			
 		return (String[]) projection.toArray();
 	}
 	
 	
 	
 }
