 /*
  * Copyright 2000-2003 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
  *
  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
  *
  * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
  *
  * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
  *
  * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
  *
  * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
  *
  * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
  */
 package gov.nih.nci.ncicb.cadsr.loader.parser;
 import com.infomata.data.CSVFormat;
 import com.infomata.data.DataFile;
 import com.infomata.data.DataRow;
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.domain.Property;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.ReviewTracker;
 import gov.nih.nci.ncicb.cadsr.loader.util.LookupUtil;
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * A writer for CSV files 
  *
  * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
  */
 public class CSVWriter implements ElementWriter
 {
   private DataFile write;
   private File report;
   private ReviewTracker reviewTracker = ReviewTracker.getInstance();
  
   public void setOutput(String url) 
   {
     report = new File(url);
   }
   
   public void write(ElementsLists elements) 
   {
     write = DataFile.createWriter("8859_2", true);
     write.setDataFormat(new CSVFormat());
     DataRow row = null;
     if((report!=null) && (report.exists()))
 		{
 		    report.delete();
 		}
     List ocs =elements.getElements(DomainObjectFactory.newObjectClass().getClass());
 
     try
       {
         write.open(report);
         row = write.next();
         row.add("UMLClass");
         row.add("UMLEntity");
         row.add("UMLDescription");
         row.add("ConceptName");
         row.add("ConceptPreferredName");
         row.add("Classification");
         row.add("ConceptCode");
         row.add("ConceptDefinition");
         row.add("ConceptDefinitionSource");
         row.add("ModifiedDate");
         row.add("HumanVerified");
         
         for(ObjectClass oc : (List<ObjectClass>) ocs) {
 
           String className = oc.getLongName();
           className = className.substring(className.lastIndexOf(".") + 1);
 
           String [] conceptCodes = oc.getPreferredName().split(":");
           for(int i = conceptCodes.length-1; i > -1; i--) {
             row = write.next();
             
             row.add(className);
             row.add(className);
             row.add(oc.getPreferredDefinition());
             Concept con = LookupUtil.lookupConcept(conceptCodes[i]);
             if(con != null) {
               row.add(con.getLongName());
               row.add(con.getLongName());
               if(i == conceptCodes.length-1)
                 row.add("ObjectClass");
               else
                 row.add("ObjectClassQualifier" + ((conceptCodes.length-1) - i));
               row.add(con.getPreferredName());
               row.add(con.getPreferredDefinition());
               row.add(con.getDefinitionSource());
               row.addEmpty();
               row.add(reviewTracker.get(oc.getLongName())?"1":"0");
             }
           }
         
           DataElementConcept o = DomainObjectFactory.newDataElementConcept();
           List<DataElementConcept> decs = (List<DataElementConcept>) elements.getElements(o.getClass());
 
           for(DataElementConcept dec : decs) {
             if(dec.getObjectClass().getLongName()
               .equals(oc.getLongName())) {
 
             String [] propConCodes = dec.getProperty().getPreferredName().split(":");
             for(int i = propConCodes.length-1; i > -1; i--)
             {
               row = write.next();
               row.add(className);
               row.add(dec.getProperty().getLongName());
 
               findDef:
               for(Definition def : (Collection<Definition>)dec.getDefinitions()) {
                 if(def.getType().equals(Definition.TYPE_UML_DEC)) {
                  row.add(def.getDefinition());
                   break findDef;
                 }
               }              
               
               Concept con = LookupUtil.lookupConcept(propConCodes[i]);
               row.add(con.getLongName());
               row.add(con.getLongName());
               if(i == propConCodes.length-1)
                 row.add("Property");
               else
                 row.add("PropertyQualifier" + ((propConCodes.length-1) - i));
         
               row.add(con.getPreferredName());
               row.add(con.getPreferredDefinition());
               row.add(con.getDefinitionSource());
               row.addEmpty();
               row.add(reviewTracker.get(oc.getLongName() + "." + dec.getProperty().getLongName())?"1":"0");            
             }
            }
           }          
           
         }
       }
       catch (IOException e)
       {
         e.printStackTrace();
       }
       finally 
       {
         write.close();
       }
     }
   
 }
 
