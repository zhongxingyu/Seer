 /*
  * Copyright (C) 2007-2009  Medsphere Systems Corporation
  * All rights reserved.
  *
  * This source code contains the intellectual property
  * of its copyright holder(s), and is made available
  * under a license. If you do not know the terms of
  * the license, please stop and do not read further.
  *
  * Please read LICENSES for detailed information about
  * the license this source code file is available under.
  * Questions should be directed to legal@medsphere.com
  *
  */
 package com.medsphere.fmdomain;
 
 import java.lang.reflect.AnnotatedElement;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Map;
 import java.util.Set;
 
 import com.medsphere.fileman.FMAnnotateFieldInfo;
 import com.medsphere.fileman.FMField;
 import com.medsphere.fileman.FMFile;
 import com.medsphere.fileman.FMRecord;
 import com.medsphere.fileman.FMResultSet;
 
 public class FMOutpatientEncounter extends FMRecord {
 
     /*-------------------------------------------------------------
      * begin static initialization
      *-------------------------------------------------------------*/
     private static Set<FMField> domainFields;
     private static FMFile fileInfo;
     private static Map<String, AnnotatedElement> domainJavaFields;
     private static Map<String, String> domainNumbers;
 
     static {
         domainJavaFields = getDomainJavaFields(FMOutpatientEncounter.class);
         domainFields = getFieldsInDomain(domainJavaFields);
         domainNumbers = getNumericMapping(FMHospitalLocation.class);
         fileInfo = new FMFile("OUTPATIENT ENCOUNTER") {
 
             @Override
             public Collection<FMField> getFields() {
                 return domainFields;
             }
         };
         fileInfo.setPack(true);
     }
 
     public static FMFile getFileInfoForClass() {
         return fileInfo;
     }
 
     @Override
     protected Set<FMField> getDomainFields() {
         return domainFields;
     }
 
     @Override
     protected Map<String, AnnotatedElement> getDomainJavaFields() {
         return domainJavaFields;
     }
 
     @Override
     protected Map<String, String> getNumericMapping() {
         return domainNumbers;
     }
 
     /*-------------------------------------------------------------
      * end static initialization
      *-------------------------------------------------------------*/
 
     @FMAnnotateFieldInfo( name="DATE", number=".01", fieldType=FMField.FIELDTYPE.DATE)
     protected Date date;
     @FMAnnotateFieldInfo( name="PATIENT", number=".02", fieldType=FMField.FIELDTYPE.POINTER_TO_FILE)
     protected Integer patient;
     @FMAnnotateFieldInfo( name="LOCATION", number=".04", fieldType=FMField.FIELDTYPE.POINTER_TO_FILE)
     protected Integer location;
     @FMAnnotateFieldInfo( name="DATE TIME CREATED", number="104", fieldType=FMField.FIELDTYPE.DATE)
     protected Date dateTimeCreated;

     public FMOutpatientEncounter() {
         super( fileInfo.getFileName() );
     }
 
     public FMOutpatientEncounter(FMResultSet results) {
         super( results );
     }
 
     public Date getDate() {
         return date;
     }
 
     public Integer getPatient() {
         return patient;
     }
 
     public Integer getLocation() {
         return location;
     }
 
     public Date getDateTimeCreated() {
         return dateTimeCreated;
     }
 }
