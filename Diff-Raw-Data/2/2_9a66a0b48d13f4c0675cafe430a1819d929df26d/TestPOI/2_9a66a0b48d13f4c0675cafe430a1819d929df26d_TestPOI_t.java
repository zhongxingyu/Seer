 /*
  *  Copyright 2010 Acer.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package ua.dp.primat;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import ua.dp.primat.curriculum.data.IndividualControl;
 import ua.dp.primat.curriculum.data.StudentGroup;
 import ua.dp.primat.curriculum.data.WorkloadEntry;
 import ua.dp.primat.curriculum.planparser.CurriculumParser;
 import ua.dp.primat.curriculum.planparser.CurriculumXLSRow;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Acer
  */
 public class TestPOI {
 
     public TestPOI() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     
     @Test
     public void testIt() {
         int semesters = 8;
         StudentGroup pz081 = new StudentGroup("", new Long(1), new Long(2008));
         CurriculumParser cParser = new CurriculumParser(pz081, 0, 8, 83, semesters,
                 "src/test/resources/PZ_B.07_08_140307_lev4.xls");
         List<CurriculumXLSRow> listParsed = cParser.Parse();
         for (int i=0;i<listParsed.size();i++) {
             System.out.println(listParsed.get(i).getDisciplineName());
             for (int j=1;j<=semesters;j++)
                 System.out.print(">"+listParsed.get(i).getFinalControlTypeInSemester(j).toString());
             System.out.print("\n");
         }
 
         //check result
        assertEquals(true,listParsed.size() > 50);
     }
 
 }
