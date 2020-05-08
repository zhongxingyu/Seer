 //: "The contents of this file are subject to the Mozilla Public License
 //: Version 1.1 (the "License"); you may not use this file except in
 //: compliance with the License. You may obtain a copy of the License at
 //: http://www.mozilla.org/MPL/
 //:
 //: Software distributed under the License is distributed on an "AS IS"
 //: basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 //: License for the specific language governing rights and limitations
 //: under the License.
 //:
 //: The Original Code is Guanxi (http://www.guanxi.uhi.ac.uk).
 //:
 //: The Initial Developer of the Original Code is Alistair Young alistair@codebrane.com
 //: All Rights Reserved.
 //:
 
 package org.guanxi.common.security;
 
 import javax.security.cert.X509Certificate;
 import javax.security.cert.CertificateException;
 import java.io.*;
 
 /**
  * <font size=5><b></b></font>
  *
  * @author Alistair Young alistair@smo.uhi.ac.uk
  */
 public class Cert {
   public static X509Certificate getCertificate(String certFilePath) {
     try {
       return getCertificate(new FileInputStream(new File(certFilePath)));
     }
     catch(FileNotFoundException fnfe) {
       return null;
     }
   }
 
   public static X509Certificate getCertificate(File certFile) {
     try {
       return getCertificate(new FileInputStream(certFile));
     }
     catch(FileNotFoundException fnfe) {
       return null;
     }
   }
 
  /**
   * Generates an X509 certificate from an input stream. Note that the
   * stream will be closed when this method returns
   *
   * @param in The stream to read from
   * @return X509 certificate generated from the stream
   */
   public static X509Certificate getCertificate(InputStream in) {
     try {
      return X509Certificate.getInstance(in);
     }
     catch(CertificateException se) {
       return null;
     }
     finally {
      try {
        in.close();
      }
      catch(IOException ioe) {
        // Closing the stream failed. Shouldn't stop us returning the cert though
      }
     }
   }
 }
