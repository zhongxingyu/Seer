 package com.grooveshark.util.maxmind;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.fail;
 
 import org.apache.log4j.Logger;
 
 public class ParserTest 
 {
 
     public static final Logger log = Logger.getLogger(ParserTest.class);
 
     public static final String cityFilename = "GeoLiteCity_latest/GeoLiteCity-Location.csv";
     public static final String blocksFilename = "GeoLiteCity_latest/GeoLiteCity-Blocks.locid";
     public static final String regionFilename = "GeoLiteCity_latest/region.csv";
 
     private Parser parser = null;
 
    @Before 
     public void setup()
     {
         try {
            this.parser = new Parser(cityFilename, regionFilename);
         } catch (Exception e) {
             e.printStackTrace();
             fail("Failed to setup maxmind parser. Excpetion:\n"  + e.getMessage());
         }
     }
 
    @Test
     public void testParser()
     {
         long start = System.currentTimeMillis();
         System.out.println("Parsing region data..");
         try {
             this.parser.parseRegionData();
         } catch (Exception e) {
             e.printStackTrace();
             fail("Failed to parse region data. Excpetion:\n"  + e.getMessage());
         }
         System.out.println("Parsing city data..");
         try {
             this.parser.parseCityData();
         } catch (Exception e) {
             e.printStackTrace();
             fail("Failed to parse city data. Excpetion:\n"  + e.getMessage());
         }
         float elapsed = (System.currentTimeMillis() - start)/(float) 1000;
         System.out.println("Done ["+elapsed+" secs].");
     }
 
 }
