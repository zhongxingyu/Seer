 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.util.ArrayList;
 import java.util.List;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import wad.spring.domain.Reference;
 import wad.spring.domain.ReferenceType;
 import wad.spring.service.BibtexService;
 import wad.spring.service.BibtexServiceImpl;
 import wad.spring.tools.Parsers;
 
 /**
  *
  * @author tonykovanen
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = BibtexServiceConfig.class)
 public class BibtexServiceImplTest {
     ReferenceRepositoryTestImpl referenceRepository;
     
     BibtexService bibtexService;
 
     public BibtexServiceImplTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
         referenceRepository = new ReferenceRepositoryTestImpl();
         bibtexService = new BibtexServiceImpl(referenceRepository);
     }
 
     @After
     public void tearDown() {
     }
    
 
     @Test
     public void generateBibtexGeneratesCorrectlyWithOneReference() {
         referenceRepository.save(generateParsittava());
         assertEquals("\n\n" + generateValmis().toString(), bibtexService.generateBibtex());
     }
     
     @Test
     public void generateBibtexGeneratesCorrectlyTwoReferencesThatAreSameWIthIncrementedReferenceCite() {
        Parsers.nollaaLaskuri();
         Reference first = generateParsittava();
         Reference second = generateParsittava();
         Reference changedReady = generateValmis();
         changedReady.setReferenceCite("V061");
         referenceRepository.save(first);
         referenceRepository.save(second);
         assertEquals("\n\n" + generateValmis().toString() + "\n\n" + changedReady.toString(), bibtexService.generateBibtex());
     }
 
     private Reference generateParsittava() {
         Reference reference = new Reference();
         reference.setType(ReferenceType.INPROCEEDINGS);
         reference.setAuthor("Vihavainen");
         reference.setBooktitle("Nörttien päivä");
         reference.setAddress("USA");
         reference.setPublisher("ACM");
         reference.setTitle("kirja");
         reference.setJournal("journal");
         reference.setPublishingYear("2006");
         reference.setPages("3-7");
 
         return reference;
     }
 
     private Reference generateValmis() {
         Reference reference = new Reference();
         reference.setType(ReferenceType.INPROCEEDINGS);
         reference.setAuthor("Vihavainen");
         reference.setBooktitle("N\\\"{o}rttien p\\\"{a}iv\\\"{a}");
         reference.setAddress("USA");
         reference.setPublisher("ACM");
         reference.setTitle("kirja");
         reference.setJournal("journal");
         reference.setPages("3--7");
         reference.setPublishingYear("2006");
         reference.setReferenceCite("V06");
         return reference;
     }
 }
