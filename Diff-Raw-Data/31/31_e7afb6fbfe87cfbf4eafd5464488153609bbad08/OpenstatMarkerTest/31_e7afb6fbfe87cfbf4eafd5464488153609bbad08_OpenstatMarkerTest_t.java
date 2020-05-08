 package com.openstat.test;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 
 import org.junit.Test;
 
 import com.openstat.OpenstatMarker;
 
 public class OpenstatMarkerTest {
     private static final String CURR_DIR = System.getProperty("user.dir");
    private static final String TEST_DIR = CURR_DIR + File.separator + ".."
        + File.separator + "test";
    private static final String TEST_FILE = TEST_DIR + File.separator + "testcase.txt";
 
     private static final int COL_URL = 0;
     private static final int COL_RAWMARKER = 1;
     private static final int COL_SERVICE = 2;
     private static final int COL_CAMPAIGN = 3;
     private static final int COL_AD = 4;
     private static final int COL_SOURCE = 5;
 
     @Test public void testParseRawMarker() throws IOException {
         BufferedReader r = utf8Reader(TEST_FILE);
         String l;
         int n = 0;
         while ((l = r.readLine()) != null) {
             String[] col = l.split("\t", -1);
 
             OpenstatMarker m = OpenstatMarker.parseRawMarker(col[COL_RAWMARKER]);
 
             assertEquals("Mismatch at service in line #" + n, col[COL_SERVICE], m.getService());
             assertEquals("Mismatch at campaign in line #" + n, col[COL_CAMPAIGN], m.getCampaign());
             assertEquals("Mismatch at ad in line #" + n, col[COL_AD], m.getAd());
             assertEquals("Mismatch at source in line #" + n, col[COL_SOURCE], m.getSource());
 
             assertEquals(col[COL_RAWMARKER], m.toString());
 
             n++;
         }
     }
 
     @Test public void testParseURL() throws IOException {
         BufferedReader r = utf8Reader(TEST_FILE);
         String l;
         int n = 0;
         while ((l = r.readLine()) != null) {
             String[] col = l.split("\t", -1);
 
             OpenstatMarker m = OpenstatMarker.parseURL(col[COL_URL]);
 
             assertNotNull("No marker found in line #" + n, m);
 
             assertEquals("Mismatch at service in line #" + n, col[COL_SERVICE], m.getService());
             assertEquals("Mismatch at campaign in line #" + n, col[COL_CAMPAIGN], m.getCampaign());
             assertEquals("Mismatch at ad in line #" + n, col[COL_AD], m.getAd());
             assertEquals("Mismatch at source in line #" + n, col[COL_SOURCE], m.getSource());
 
             assertEquals(col[COL_RAWMARKER], m.toString());
 
             n++;
         }
     }
 
    @Test public void testBadMarkers() throws IOException {
        BufferedReader r = utf8Reader(TEST_DIR + File.separator + "bad-markers.txt");
        String l;
        int n = 0;
        while ((l = r.readLine()) != null) {
            OpenstatMarker m = OpenstatMarker.parseURL(l);
            assertNull("Marker found in line #" + n, m);
            n++;
        }
    }

     private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
     private static BufferedReader utf8Reader(String filePath) throws FileNotFoundException {
         return new BufferedReader(
                 new InputStreamReader(
                         new FileInputStream(filePath),
                         CHARSET_UTF8
                 )
         );
     }
 }
