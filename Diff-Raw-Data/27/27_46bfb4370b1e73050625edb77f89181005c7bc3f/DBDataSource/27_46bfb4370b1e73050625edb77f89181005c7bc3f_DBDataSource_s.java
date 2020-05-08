 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
  * Java(TM), available at http://sourceforge.net/projects/dcm4che.
  *
  * The Initial Developer of the Original Code is
  * ACG.
  * Portions created by the Initial Developer are Copyright (C) 2003-2005
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  * Lin Yang <YangLin@cn-arg.com>
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.dcm4chex.archive.dcm;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.imageio.stream.ImageInputStream;
 
 import org.dcm4che.data.Dataset;
 import org.dcm4chex.archive.dcm.qrscp.QrSCPDBImpl;
 import org.dcm4chex.archive.util.FileDataSource;
 
 /**
  * @author YangLin@cn-arg.com
  * @version 1.0
  * @since 01.22.2009
  * 
  * This class overrides two methods of DataSource in order to read data 
  * from database. 
  */
 public class DBDataSource extends FileDataSource{
     
     private QrSCPDBImpl qri = null;
     
     /**
      * Constructor of DBDataSource.
      */
     public DBDataSource(File file, Dataset mergeAttrs, byte[] buffer) {
         super(file, mergeAttrs, buffer);
     }
     
     /**
      * Acquire the inputStream of @param file.
      */
     protected InputStream createInputStream(File file) throws IOException {
         InputStream is = null;       
         try {
             is = qri.getInputStream(file);
         } catch (Exception e) {
         	throw new IOException(e.toString());
         }  
         return is;
     }
     
     /**
      * Acquire the imageInputStream of @param file.
      */
     protected ImageInputStream createImageInputStream(File file) throws IOException {
         ImageInputStream iis = null;
         try {
             iis = qri.getImageInputStream(file);
         } catch (Exception e) {
             throw new IOException(e.toString());
         }
         return iis;
     }
     
     /**
     * Set qri field.
      */
     public void setQrSCPDBImpl(QrSCPDBImpl qri) {
         this.qri = qri;
     }
 
 }
