 package com.cargosmart.b2b.edi.output;
 
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.InputStreamReader;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.cargosmart.b2b.edi.common.Document;
 import com.cargosmart.b2b.edi.input.EdifactBuilder;
 
 public class EdifactOutputterTest {
 
     public static final String SIMPLE_EDIFACT_W_UNA = 
         "UNA:+.? '" + 
         "UNB+UNOA:1+005435656:1+006415160:1+060515:1434+00000000000778'" + 
         "UNH+00000000000117+INVOIC:D:97B:UN'" + 
        "BGM+380+3?+4?:2?'4??5?:9+9'" + 
         "DTM+3:20060515:102'" +
         "UNT+4+00000000000117'" + 
         "UNZ+1+00000000000778'";
 
     public static final String SIMPLE_EDI_WO_UNA_UNG = 
         "UNB+UNOA:1+005435656:1+006415160:1+060515:1434+00000000000778'" + 
         "UNH+00000000000117+INVOIC:D:97B:UN'" + 
         "BGM+380+?????+?+?:?:?'?'+9'" + 
         "DTM+3:20060515:102'" +
         "UNT+4+00000000000117'" +
         "UNZ+1+00000000000778'";
     
     public static final String SIMPLE_EDIFACT_WO_UNA =
         "UNB+UNOA:1+005435656:1+006415160:1+060515:1434+00000000000778'" + 
         "UNG+INVOIC+CARGOSMART:01+EXAMPLE:ZZ+110414:2138+268877+UN+D:97B'" +
         "UNH+00000000000117+INVOIC:D:97B:UN'" + 
        "BGM+380+?????+?+?:?:?'?'+9'" + 
         "DTM+3:20060515:102'" +
         "UNT+4+00000000000117'" +
         "UNE+1+268877'" +
         "UNZ+1+00000000000778'";
     
     private Document doc;
     private Document doc_w_una;
     private Document doc_wo_una;
     
     @Before
     public void setUp() throws Exception {
         EdifactBuilder builder = new EdifactBuilder();
         doc = builder.buildDocument(new InputStreamReader(EdifactOutputterTest.class.getResourceAsStream("/invoice_d97b.txt")));
         doc_w_una = builder.buildDocument(SIMPLE_EDIFACT_W_UNA);
         doc_wo_una = builder.buildDocument(SIMPLE_EDIFACT_WO_UNA);
     }
 
     @After
     public void tearDown() throws Exception {
     }
 
     @Test
     public void testOutput() {
         EdifactOutputter outputter = new EdifactOutputter();
         String output = outputter.outputString(doc_w_una);
         assertEquals(SIMPLE_EDIFACT_W_UNA, output);
         
         output = outputter.outputString(doc_wo_una);
         assertEquals(SIMPLE_EDIFACT_WO_UNA, output);
     }
     
     @Test
     public void testAddGroupEnvelope() {
     	
     }
 }
