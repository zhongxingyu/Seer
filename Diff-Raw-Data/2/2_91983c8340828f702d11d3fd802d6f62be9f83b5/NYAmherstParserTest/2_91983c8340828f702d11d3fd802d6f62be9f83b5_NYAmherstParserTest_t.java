 package net.anei.cadpage.parsers;
 
 import org.junit.Test;
 
 
 public class NYAmherstParserTest extends BaseParserTest {
   
   public NYAmherstParserTest() {
     setParser(new NYAmherstParser(), "AMHERST", "NY");
   }
   
   @Test
   public void testParser() {
     doTest("T1",
     		"CLA 10990 Keller Rd. EMS 80M slurred speech",
     		"CALL:EMS 80M slurred speech",
     		"ADDR:10990 Keller Rd"
     		);
     doTest("T2",
     		"CLA 9970 Greiner Rd. EMS Finger laceration",
     		"CALL:EMS Finger laceration",
     		"ADDR:9970 Greiner Rd"
     		);
     doTest("T3",
     	    "CLA 5727 Marthas Vineyard EMS 55 y/o fell",
     	    "CALL:EMS 55 y/o fell",
     	    "ADDR:5727 Marthas Vineyard"
     	    );
     doTest("T4",
     	    "CLA 5945 Vinecroft Dr. EMA apt. 401",
     	    "CALL:EMA apt. 401",
     	    "ADDR:5945 Vinecroft Dr"
     	    );	
     doTest("T5",
     	    "CLA Salt rd. & County rd. MVA. 1/2 mile north of county.",
     	    "CALL:MVA. 1/2 mile north of county.",
    	    "ADDR:Salt rd & County rd"
     	    );	
     
     
   }
 }
