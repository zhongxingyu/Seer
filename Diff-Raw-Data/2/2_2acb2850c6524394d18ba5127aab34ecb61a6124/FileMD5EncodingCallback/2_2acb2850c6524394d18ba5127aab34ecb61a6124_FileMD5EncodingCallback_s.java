 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.tutorials.ace.faces.listeners;
 
import org.icefaces.component.fileentry.*;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import org.apache.commons.codec.binary.Hex;
 import org.icefaces.tutorials.util.TutorialMessageUtils;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nils
  * Date: 11-04-05
  * Time: 12:38 PM
  */
 @RequestScoped
 @ManagedBean
 public class FileMD5EncodingCallback implements FileEntryCallback {
     private MessageDigest digest;
     private boolean md5NotFound = false;
 
     // Set up a instance of a MD5 block-encoder
     public void begin(FileEntryResults.FileInfo fileInfo) {
         try { digest = MessageDigest.getInstance("MD5"); }
         catch (NoSuchAlgorithmException e) {
             md5NotFound = true;
         }
     }
     // Hash a block of bytes
     public void write(byte[] bytes, int offset, int length) {
        if (!md5NotFound) digest.update(bytes, offset, length);
     }
     // Hash a single byte
     public void write(int i) {
         if (!md5NotFound) digest.update((byte) i);
     }
     // When FileEntryCallback ends for a file:
     public void end(FileEntryResults.FileInfo fileEntryInfo) {
         // If the file upload was completed properly
         if (md5NotFound) fileEntryInfo.updateStatus(new EncodingNotFoundUploadStatus(), true, true);
 
         if (fileEntryInfo.getStatus().isSuccess()) {
             fileEntryInfo.updateStatus(new EncodingSuccessStatus(), false);
         }
     }
 
     // Assistance method to convert digested bytes to hex string
     public String getHash() { return String.valueOf(Hex.encodeHex(digest.digest())); }
 
     private class EncodingNotFoundUploadStatus implements FileEntryStatus {
         public boolean isSuccess() {
             return false;
         }
 
         public FacesMessage getFacesMessage(FacesContext facesContext, UIComponent uiComponent, FileEntryResults.FileInfo fileInfo) {
             return new FacesMessage(FacesMessage.SEVERITY_ERROR, TutorialMessageUtils.getMessage("content.callback.encode.fail.message"), TutorialMessageUtils.getMessage("content.callback.encode.fail.detail"));
         }
     }
 
     private class EncodingSuccessStatus implements FileEntryStatus {
         public boolean isSuccess() {
             return true;
         }
 
         public FacesMessage getFacesMessage(FacesContext facesContext, UIComponent uiComponent, FileEntryResults.FileInfo fileInfo) {
             return new FacesMessage(FacesMessage.SEVERITY_INFO, TutorialMessageUtils.getMessage("content.callback.result.message"), getHash());
         }
     }
 }
