 //    Data SQL is a light JDBC wrapper.
 //    Copyright (C) 2014 Adrián Romero Corchado.
 //
 //    This file is part of Data SQL
 //
 //     Licensed under the Apache License, Version 2.0 (the "License");
 //     you may not use this file except in compliance with the License.
 //     You may obtain a copy of the License at
 //     
 //         http://www.apache.org/licenses/LICENSE-2.0
 //     
 //     Unless required by applicable law or agreed to in writing, software
 //     distributed under the License is distributed on an "AS IS" BASIS,
 //     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //     See the License for the specific language governing permissions and
 //     limitations under the License.
 
 package com.adr.datasql;
 
 import com.adr.datasql.orm.Data;
 import com.adr.datasql.orm.DataJson;
 import com.adr.datasql.orm.Definition;
 import com.adr.datasql.orm.Field;
 import com.adr.datasql.orm.FieldKey;
 import com.adr.datasql.orm.GetData;
 import com.adr.datasql.orm.InsertData;
 import com.adr.datasql.orm.UpsertData;
 import java.sql.SQLException;
 import javax.json.Json;
 import javax.json.JsonObject;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  * @author adrian
  */
 public class ORMTest {
 
 
     @Test
     public void InsertPojo() throws SQLException {
 
         try (Session session = new Session(DataTestSuite.getDataSource().getConnection())) {        
             
             ProcExec<SamplePojo> insertSamplePojo = new InsertData<SamplePojo>(SamplePojo.DATA);
             
             SamplePojo pojo = new SamplePojo();
             pojo.setId("pojoid");
             pojo.setCode("pojocode");
             pojo.setName("pojoname");
             
             session.exec(insertSamplePojo, pojo);   
             
             
            Object[] result = session.find(new QueryArray("select id, code, name from com_adr_datasql_SamplePojo where id = ?"), "pojoid");
             System.out.println("--> " + result[0] + ", " + result[1] + ", " + result[2]);              
         }
     }
     
     @Test
     public void insertJson() throws SQLException {
         
         try (Session session = new Session(DataTestSuite.getDataSource().getConnection())) {
  
             // Metadata Definition
             Data<JsonObject> DATAJSON = new DataJson(new Definition(
                "com_adr_datasql_SamplePojo",
                 new FieldKey("id", Kind.STRING),
                 new Field("code", Kind.STRING),
                 new Field("name", Kind.STRING)));   
             
             // Object definition          
 //            JsonReader jsonReader = Json.createReader(new StringReader("{}"));
 //            JsonObject value = jsonReader.readObject();
 //            jsonReader.close();     
             
             JsonObject value = Json.createObjectBuilder()
                 .add("id", "John")
                 .add("code", "Smith")
                 .add("name", "pepeto")
                 .build();            
             
             ProcExec<JsonObject> insertJson = new UpsertData<JsonObject>(DATAJSON);
             
             session.exec(insertJson, value);
         }
     }
     
     // @Test
     public void insertMap() throws SQLException {
     }   
     
     @Test
     public void findPojo() throws SQLException {
         
 
         try (Session session = new Session(DataTestSuite.getDataSource().getConnection())) {        
             
             ProcFind<SamplePojo, Object[]> findSamplePojo = new GetData<SamplePojo>(SamplePojo.DATA);
 
             SamplePojo pojo = session.find(findSamplePojo, "pojoid");   
             
             System.out.println(pojo.toString());              
         }        
         
     }  
     
 //    // @Test
 //    public void listPojo() throws SQLException {
 //        
 //
 //        try (Session session = new Session(DataTestSuite.getDataSource().getConnection())) {        
 //            
 //            ProcList<SamplePojo, Object[]> listSamplePojo = new ListData<SamplePojo>(SamplePojo.DATA);
 //                    
 //            List<SamplePojo> pojo = session.list(listSamplePojo);   
 //            
 //            //System.out.println(pojo.toString());              
 //        }        
 //        
 //    }  
 //    
 //    // @Test
 //    public void filterPojo() throws SQLException {
 //        
 //        try (Session session = new Session(DataTestSuite.getDataSource().getConnection())) {        
 //            
 //            ProcList<SamplePojo, Object[]> listSamplePojo = new ListData<SamplePojo>(SamplePojo.DATA);
 //            
 //            Map<String, Object> parameters = new HashMap<String, Object>();
 //            parameters.put("name", "two name")
 //                    
 //            List<SamplePojo> pojo = session.list(listSamplePojo, parameters);   
 //            
 //            //System.out.println(pojo.toString());              
 //        }       
 //        
 //    }  
     
     @BeforeClass
     public static void setUpClass() throws SQLException {   
    
         try (Session session = new Session(DataTestSuite.getDataSource().getConnection())) {;  
        
            session.exec(new QueryArray("create table com_adr_datasql_SamplePojo(id varchar(32), code varchar(128), name varchar(1024))"));     
         }
     }
     
     @AfterClass
     public static void tearDownClass() {
     }
     
     @Before
     public void setUp() {  
     }
     
     @After
     public void tearDown() {
     }     
 }
