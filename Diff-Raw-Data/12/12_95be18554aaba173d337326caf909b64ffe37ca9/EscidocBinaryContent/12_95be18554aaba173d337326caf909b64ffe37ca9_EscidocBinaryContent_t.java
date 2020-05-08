 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package org.esidoc.core.utils.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 
 /**
  * Class encapsulating binary content.
  */
 public class EscidocBinaryContent {
 
     private Stream stream;
 
     private String fileName;
 
     private String mimeType;
 
     private String redirectUrl;
 
     /**
      * @return the redirectUrl
      */
     public String getRedirectUrl() {
         return this.redirectUrl;
     }
 
     /**
      * @param redirectUrl
      *            the redirectUrl to set
      */
     public void setRedirectUrl(final String redirectUrl) {
         this.redirectUrl = redirectUrl;
     }
 
     /**
      * @return the content
      */
     public InputStream getContent() throws IOException {
         if (this.stream == null) {
             return null;
         }
         return this.stream.getInputStream();
     }
 
     /**
      * @param content
      *            the content to set
      */
     public void setContent(final InputStream content) throws IOException {
         this.stream = new Stream();
        IOUtils.copyAndCloseInput(content, this.stream);
     }
 
     /**
      * @return the fileName
      */
     public String getFileName() {
         return this.fileName;
     }
 
     /**
      * @param fileName
      *            the fileName to set
      */
     public void setFileName(final String fileName) {
         this.fileName = fileName;
     }
 
     /**
      * @return the mimeType
      */
     public String getMimeType() {
         return this.mimeType;
     }
 
     /**
      * @param mimeType
      *            the mimeType to set
      */
     public void setMimeType(final String mimeType) {
         this.mimeType = mimeType != null ? mimeType.trim() : null;
     }
 
     /**
      * @return the size of the stream
      */
     public long getContentLength() {
         return this.stream.size();
     }
 
 }
