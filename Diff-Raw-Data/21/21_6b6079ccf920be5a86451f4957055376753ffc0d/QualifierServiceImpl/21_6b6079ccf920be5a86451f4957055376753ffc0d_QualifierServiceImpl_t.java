 /*
  * Copyright 2012 EMBL-EBI
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.ebi.ricordo.rdfstore.service;
 
 import uk.ac.ebi.ricordo.rdfstore.bean.Qualifier;
 
 import java.io.*;
 import java.util.ArrayList;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Sarala Wimalaratne
  * Date: 27/07/12
  * Time: 15:57
  */
 public class QualifierServiceImpl implements QualifierService {
 
     private String qualifierFile;
     private String qualifierURL;
     ArrayList<Qualifier> queryList = new ArrayList<Qualifier>();
 
     public QualifierServiceImpl(String qualifierFile, String qualifierURL) {
         this.qualifierFile = qualifierFile;
         this.qualifierURL = qualifierURL;
     }
 
     public ArrayList<Qualifier> getQualifierList() {
         if (queryList.isEmpty()){
             try {
                 InputStream inputStream = QualifierServiceImpl.class.getResourceAsStream(qualifierFile);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                 String strLine;
                 while ((strLine = reader.readLine()) != null)   {
                     queryList.add(new Qualifier(qualifierURL,strLine));
                 }
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return queryList;
     }
 
    public String getQualifierURL() {
        return qualifierURL;
    }
 }
