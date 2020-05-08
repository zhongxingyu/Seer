 package au.org.intersect.samifier.parser;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.util.List;
 
 import org.junit.Test;
 
 import au.org.intersect.samifier.domain.PeptideSearchResult;
 import au.org.intersect.samifier.domain.ProteinToOLNMap;
 
 public class PeptideSearchResultsParserImplUnitTest
 {
     @Test
     public void testParsingMascotPeptideSearchResultsDatFormat()
     {
         File mascotFile = new File("test/resources/test_mascot_search_results.txt");
 
         File mapFile = new File("test/resources/test_accession.txt");
         try {
             ProteinToOLNParser proteinToOLNParser = new ProteinToOLNParserImpl();
             ProteinToOLNMap proteinToOLNMap = proteinToOLNParser.parseMappingFile(mapFile);
             PeptideSearchResultsParser peptideSearchResultsParser = new PeptideSearchResultsParserImpl(proteinToOLNMap);
 
             List<PeptideSearchResult> list = peptideSearchResultsParser.parseResults(mascotFile);
             assertEquals("Parser should find seven ", 7, list.size());

             assertTrue(list.contains(new PeptideSearchResult("q21_p1", "EFGILK", "KPYK1_YEAST", 469, 474 , new BigDecimal("25.95"))));
             assertTrue(list.contains(new PeptideSearchResult("q131_p1", "SVIDNAR", "KPYK1_YEAST", 62, 68 , new BigDecimal("40.45"))));
             assertTrue(list.contains(new PeptideSearchResult("q217_p1", "INFGIEK", "KPYK1_YEAST", 460, 466 , new BigDecimal("37.51"))));
             assertTrue(list.contains(new PeptideSearchResult("q376_p2", "TGIAIGLNK", "RL36B_YEAST", 5, 13 , new BigDecimal("35.86"))));
             assertTrue(list.contains(new PeptideSearchResult("q887_p1", "KRNEEEDAK", "RL31A_YEAST", 78, 86 , new BigDecimal("40.09"))));
             assertTrue(list.contains(new PeptideSearchResult("q1009_p2", "EYTINLHKR", "RL31A_YEAST", 11, 19 , new BigDecimal("52.75"))));
             assertTrue(list.contains(new PeptideSearchResult("q2365_p1", "NEEEDAKNPLFSYVEPVLVASAK", "RL31A_YEAST", 80, 102 , new BigDecimal("20.69"))));
         }
         catch(Exception e)
         {
             fail("Unexpected exception: " + e.getMessage());
             e.printStackTrace();
         }
     }
 
     @Test
     public void testParsingMascotPeptideSearchResultsMzidentMLFormat()
     {
         File mascotFile = new File("test/resources/test_mascot_search_results.mzid");
 
         File mapFile = new File("test/resources/test_accession.txt");
         try {
             ProteinToOLNParser proteinToOLNParser = new ProteinToOLNParserImpl();
             ProteinToOLNMap proteinToOLNMap = proteinToOLNParser.parseMappingFile(mapFile);
             PeptideSearchResultsParser peptideSearchResultsParser = new PeptideSearchResultsParserImpl(proteinToOLNMap);
 
             List<PeptideSearchResult> list = peptideSearchResultsParser.parseResults(mascotFile);
             assertEquals("Parser should find sixteen ", 16, list.size());
            System.out.println();
             assertTrue(list.contains(new PeptideSearchResult("PE_11_1_KPYK1_YEAST_0_469_474", "EFGILK", "KPYK1_YEAST", 469, 474 , new BigDecimal("25.95"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_11_2_KPYK1_YEAST_0_62_68", "SVIDNAR", "KPYK1_YEAST", 62, 68 , new BigDecimal("40.45"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_11_3_KPYK1_YEAST_0_460_466", "INFGIEK", "KPYK1_YEAST", 460, 466 , new BigDecimal("37.51"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_11_4_RL36B_YEAST_0_5_13", "TGIAIGLNK", "RL36B_YEAST", 5, 13 , new BigDecimal("35.86"))));  
             assertTrue(list.contains(new PeptideSearchResult("PE_14_1_RL31A_YEAST_0_78_86", "KRNEEEDAK", "RL31A_YEAST", 78, 86 , new BigDecimal("40.09"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_14_2_RL31B_YEAST_0_78_86", "KRNEEEDAK", "RL31B_YEAST", 78, 86 , new BigDecimal("40.09"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_14_3_RL31A_YEAST_0_11_19", "EYTINLHKR", "RL31A_YEAST", 11, 19 , new BigDecimal("52.75"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_14_4_RL31B_YEAST_0_11_19", "EYTINLHKR", "RL31B_YEAST", 11, 19 , new BigDecimal("52.75"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_14_5_RL31_ASHGO_0_11_19", "EYTINLHKR", "RL31_ASHGO", 11, 19 , new BigDecimal("52.75"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_14_6_RL31_CYAPA_0_18_26", "EYTINLHKR", "RL31_CYAPA", 18, 26 , new BigDecimal("52.75"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_14_7_RL31_DICDI_0_10_18", "EYTINLHKR", "RL31_DICDI", 10, 18 , new BigDecimal("52.75"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_14_8_RL31_NICGU_0_16_24", "EYTINLHKR", "RL31_NICGU", 16, 24 , new BigDecimal("52.75"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_14_9_RL31_PANGI_0_16_24", "EYTINLHKR", "RL31_PANGI", 16, 24 , new BigDecimal("52.75"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_14_10_RL31_PERFR_0_17_25", "EYTINLHKR", "RL31_PERFR", 17, 25 , new BigDecimal("52.75"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_14_11_RL31A_YEAST_0_80_102", "NEEEDAKNPLFSYVEPVLVASAK", "RL31A_YEAST", 80, 102 , new BigDecimal("20.69"))));
             assertTrue(list.contains(new PeptideSearchResult("PE_14_12_RL31B_YEAST_0_80_102", "NEEEDAKNPLFSYVEPVLVASAK", "RL31B_YEAST", 80, 102 , new BigDecimal("20.69"))));
         }
         catch(Exception e)
         {
             fail("Unexpected exception: " + e.getMessage());
             e.getCause().printStackTrace();
         }
     }
 }
