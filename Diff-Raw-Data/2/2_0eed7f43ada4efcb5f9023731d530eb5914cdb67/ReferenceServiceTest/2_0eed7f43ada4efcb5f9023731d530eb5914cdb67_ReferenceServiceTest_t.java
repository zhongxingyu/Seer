 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Bibtex.service;
 
 import Bibtex.domain.Reference;
 import Bibtex.repository.ReferenceRepository;
 import java.util.HashMap;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Noemj
  */
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/spring-context.xml"})
 public class ReferenceServiceTest {
 
     @Autowired
     ReferenceService referenceService;
     @Autowired
     ReferenceRepository referenceRepository;
 
     HashMap<String, String> kentat;
     Reference ref1;
     
        
     @Before
     public void setUp() throws Exception {
         kentat = new HashMap<String,String>();
         kentat.put("Author", "Author = esa");    
         ref1 = new Reference(); 
         ref1.setId(1L);
         ref1.setType("book");
         ref1.setKey("ABCD");      
         ref1.setFields(kentat);
         referenceService.add(ref1);
     }
 
     @After
     public void tearDown() {
     }
     
     @Test
     public void AddReferenceTest(){     
         for (Reference reference : referenceRepository.findAll()){
             assertEquals(reference.getType(), ref1.getType());
             assertEquals(reference.getKey(), ref1.getKey());
         }
     }
 }
