 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package cz.fjfi.pvs.kosapi.parser;
 
 import java.util.List;
 import junit.framework.TestCase;
 
 /**
  *
  * @author Snake66
  */
 public class AtomTagParserTest extends TestCase {
     private String atomFeed = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><atom:feed xmlns=\"http://kosapi.feld.cvut.cz/schema/3\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:osearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xml:base=\"https://kosapi.fit.cvut.cz/api/3b/\" xml:lang=\"en\"><atom:id>https://kosapi.fit.cvut.cz/api/3b/courses</atom:id><atom:updated>2012-12-02T14:32:19.343</atom:updated><atom:link rel=\"next\" href=\"courses?offset=10&amp;limit=10\"/><atom:entry><atom:title>Foreign Language - Spanish 5</atom:title><atom:id>urn:cvut:kos:course:23368305</atom:id><atom:updated>2004-11-28T14:13:42.0</atom:updated><atom:author><atom:name>f6carsky</atom:name></atom:author><atom:link rel=\"self\" href=\"courses/15JS5/\"/><atom:content atom:type=\"xml\" xsi:type=\"course\"><approvalDate>1998-07-13</approvalDate><code>15JS5</code><completion>CREDIT</completion><credits>2</credits><department xlink:href=\"divisions/16115/\">Department of Languages and Humanities</department><name xml:lang=\"cs\">Jazyk - ĹˇpanÄ›lĹˇtina 5</name><name xml:lang=\"en\">Foreign Language - Spanish 5</name><range>0+2</range><state>APPROVED</state></atom:content></atom:entry><atom:entry><atom:title>Foreign Language - Spanish 8</atom:title><atom:id>urn:cvut:kos:course:23368605</atom:id><atom:updated>2004-11-28T14:14:28.0</atom:updated><atom:author><atom:name>f6carsky</atom:name></atom:author><atom:link rel=\"self\" href=\"courses/15JS8/\"/><atom:content atom:type=\"xml\" xsi:type=\"course\"><approvalDate>1998-07-13</approvalDate><code>15JS8</code><completion>CREDIT_EXAM</completion><credits>2</credits><department xlink:href=\"divisions/16115/\">Department of Languages and Humanities</department><name xml:lang=\"cs\">Jazyk - ĹˇpanÄ›lĹˇtina 8</name><name xml:lang=\"en\">Foreign Language - Spanish 8</name><range>0+2</range><state>APPROVED</state></atom:content></atom:entry><atom:entry><atom:title>Foreign Language - Swedish 5</atom:title><atom:id>urn:cvut:kos:course:23368705</atom:id><atom:updated>2004-11-28T14:16:03.0</atom:updated><atom:author><atom:name>f6carsky</atom:name></atom:author><atom:link rel=\"self\" href=\"courses/15JV5/\"/><atom:content atom:type=\"xml\" xsi:type=\"course\"><approvalDate>1998-07-13</approvalDate><code>15JV5</code><completion>CREDIT</completion><credits>2</credits><department xlink:href=\"divisions/16115/\">Department of Languages and Humanities</department><name xml:lang=\"cs\">Jazyk - ĹˇvĂ©dĹˇtina 5</name><name xml:lang=\"en\">Foreign Language - Swedish 5</name><range>0+2</range><state>APPROVED</state></atom:content></atom:entry><atom:entry><atom:title>Foreign Language - Swedish 6</atom:title><atom:id>urn:cvut:kos:course:23368805</atom:id><atom:updated>2004-11-28T14:16:17.0</atom:updated><atom:author><atom:name>f6carsky</atom:name></atom:author><atom:link rel=\"self\" href=\"courses/15JV6/\"/><atom:content atom:type=\"xml\" xsi:type=\"course\"><approvalDate>1998-07-13</approvalDate><code>15JV6</code><completion>CREDIT</completion><credits>2</credits><department xlink:href=\"divisions/16115/\">Department of Languages and Humanities</department><name xml:lang=\"cs\">Jazyk - ĹˇvĂ©dĹˇtina 6</name><name xml:lang=\"en\">Foreign Language - Swedish 6</name><range>0+2</range><state>APPROVED</state></atom:content></atom:entry><atom:entry><atom:title>Foreign Language - Swedish 7</atom:title><atom:id>urn:cvut:kos:course:23368905</atom:id><atom:updated>2004-11-28T14:16:32.0</atom:updated><atom:author><atom:name>f6carsky</atom:name></atom:author><atom:link rel=\"self\" href=\"courses/15JV7/\"/><atom:content atom:type=\"xml\" xsi:type=\"course\"><approvalDate>1998-07-13</approvalDate><code>15JV7</code><completion>CREDIT</completion><credits>2</credits><department xlink:href=\"divisions/16115/\">Department of Languages and Humanities</department><name xml:lang=\"cs\">Jazyk - ĹˇvĂ©dĹˇtina 7</name><name xml:lang=\"en\">Foreign Language - Swedish 7</name><range>0+2</range><state>APPROVED</state></atom:content></atom:entry><atom:entry><atom:title>Foreign Language - Swedish 8</atom:title><atom:id>urn:cvut:kos:course:23369005</atom:id><atom:updated>2004-11-28T14:16:48.0</atom:updated><atom:author><atom:name>f6carsky</atom:name></atom:author><atom:link rel=\"self\" href=\"courses/15JV8/\"/><atom:content atom:type=\"xml\" xsi:type=\"course\"><approvalDate>1998-07-13</approvalDate><code>15JV8</code><completion>CREDIT</completion><credits>2</credits><department xlink:href=\"divisions/16115/\">Department of Languages and Humanities</department><name xml:lang=\"cs\">Jazyk - ĹˇvĂ©dĹˇtina 8</name><name xml:lang=\"en\">Foreign Language - Swedish 8</name><range>0+2</range><state>APPROVED</state></atom:content></atom:entry><atom:entry><atom:id>urn:cvut:kos:course:23369205</atom:id><atom:updated>2001-04-23T15:16:30.0</atom:updated><atom:author><atom:name>f6spotak</atom:name></atom:author><atom:link rel=\"self\" href=\"courses/15JX2/\"/><atom:content atom:type=\"xml\" xsi:type=\"course\"><approvalDate>1998-07-13</approvalDate><code>15JX2</code><completion>CREDIT</completion><credits>3</credits><department xlink:href=\"divisions/16115/\">Department of Languages and Humanities</department><name xml:lang=\"cs\">Jazyk - doplnĂ­ 2</name><range>0+4</range><state>APPROVED</state></atom:content></atom:entry><atom:entry><atom:id>urn:cvut:kos:course:23369305</atom:id><atom:updated>2001-04-23T15:16:30.0</atom:updated><atom:author><atom:name>f6spotak</atom:name></atom:author><atom:link rel=\"self\" href=\"courses/15JX3/\"/><atom:content atom:type=\"xml\" xsi:type=\"course\"><approvalDate>1998-07-13</approvalDate><code>15JX3</code><completion>CREDIT</completion><credits>3</credits><department xlink:href=\"divisions/16115/\">Department of Languages and Humanities</department><name xml:lang=\"cs\">Jazyk - doplnĂ­ 3</name><range>0+4</range><state>APPROVED</state></atom:content></atom:entry><atom:entry><atom:id>urn:cvut:kos:course:23369405</atom:id><atom:updated>2001-04-23T15:16:30.0</atom:updated><atom:author><atom:name>f6spotak</atom:name></atom:author><atom:link rel=\"self\" href=\"courses/15JX4/\"/><atom:content atom:type=\"xml\" xsi:type=\"course\"><approvalDate>1998-07-13</approvalDate><code>15JX4</code><completion>CREDIT_EXAM</completion><credits>3</credits><department xlink:href=\"divisions/16115/\">Department of Languages and Humanities</department><name xml:lang=\"cs\">Jazyk - doplnĂ­ 4</name><range>0+4</range><state>APPROVED</state></atom:content></atom:entry><atom:entry><atom:id>urn:cvut:kos:course:23369505</atom:id><atom:updated>2001-04-08T18:55:20.0</atom:updated><atom:author><atom:name>f6</atom:name></atom:author><atom:link rel=\"self\" href=\"courses/15JX5/\"/><atom:content atom:type=\"xml\" xsi:type=\"course\"><approvalDate>1998-07-13</approvalDate><code>15JX5</code><completion>CREDIT</completion><credits>2</credits><department xlink:href=\"divisions/16115/\">Department of Languages and Humanities</department><name xml:lang=\"cs\">JAZYK - doplnĂ­ 5</name><range>0+2</range><state>APPROVED</state></atom:content></atom:entry><osearch:itemsPerPage>10</osearch:itemsPerPage><osearch:startIndex>0</osearch:startIndex></atom:feed>";
     private AtomParser atomParser = new AtomParser(atomFeed);
     private String[][] entryTagNames = {{"approvalDate", "code", "completion", "credits", "department", "name", "name", "range", "state"},
                                         {"approvalDate", "code", "completion", "credits", "department", "name", "name", "range", "state"},
                                         {"approvalDate", "code", "completion", "credits", "department", "name", "name", "range", "state"},
                                         {"approvalDate", "code", "completion", "credits", "department", "name", "name", "range", "state"},
                                         {"approvalDate", "code", "completion", "credits", "department", "name", "name", "range", "state"},
                                         {"approvalDate", "code", "completion", "credits", "department", "name", "name", "range", "state"},
                                         {"approvalDate", "code", "completion", "credits", "department", "name", "range", "state", ""},
                                         {"approvalDate", "code", "completion", "credits", "department", "name", "range", "state", ""},
                                         {"approvalDate", "code", "completion", "credits", "department", "name", "range", "state", ""},
                                         {"approvalDate", "code", "completion", "credits", "department", "name", "range", "state", ""}};
     private String[][] entryTagContents = {{"1998-07-13", "15JS5", "CREDIT", "2", "Department of Languages and Humanities", "Jazyk - ĹˇpanÄ›lĹˇtina 5", "Foreign Language - Spanish 5", "0+2", "APPROVED"},
                                             {"1998-07-13", "15JS8", "CREDIT_EXAM", "2", "Department of Languages and Humanities", "Jazyk - ĹˇpanÄ›lĹˇtina 8", "Foreign Language - Spanish 8", "0+2", "APPROVED"},
                                             {"1998-07-13", "15JV5", "CREDIT", "2", "Department of Languages and Humanities", "Jazyk - ĹˇvĂ©dĹˇtina 5", "Foreign Language - Swedish 5", "0+2", "APPROVED"},
                                             {"1998-07-13", "15JV6", "CREDIT", "2", "Department of Languages and Humanities", "Jazyk - ĹˇvĂ©dĹˇtina 6", "Foreign Language - Swedish 6", "0+2", "APPROVED"},
                                             {"1998-07-13", "15JV7", "CREDIT", "2", "Department of Languages and Humanities", "Jazyk - ĹˇvĂ©dĹˇtina 7", "Foreign Language - Swedish 7", "0+2", "APPROVED"},
                                             {"1998-07-13", "15JV8", "CREDIT", "2", "Department of Languages and Humanities", "Jazyk - ĹˇvĂ©dĹˇtina 8", "Foreign Language - Swedish 8", "0+2", "APPROVED"},
                                             {"1998-07-13", "15JX2", "CREDIT", "3", "Department of Languages and Humanities", "Jazyk - doplnĂ­ 2", "0+4", "APPROVED",""},
                                             {"1998-07-13", "15JX3", "CREDIT", "3", "Department of Languages and Humanities", "Jazyk - doplnĂ­ 3", "0+4", "APPROVED",""},
                                             {"1998-07-13", "15JX4", "CREDIT_EXAM", "3", "Department of Languages and Humanities", "Jazyk - doplnĂ­ 4", "0+4", "APPROVED",""},
                                             {"1998-07-13", "15JX5", "CREDIT", "2", "Department of Languages and Humanities", "JAZYK - doplnĂ­ 5", "0+2", "APPROVED",""}};
     private int[][] entryNumberOfTagAttributes = {{0,0,0,0,1,1,1,0,0},
                                                 {0,0,0,0,1,1,1,0,0},
                                                 {0,0,0,0,1,1,1,0,0},
                                                 {0,0,0,0,1,1,1,0,0},
                                                 {0,0,0,0,1,1,1,0,0},
                                                 {0,0,0,0,1,1,1,0,0},
                                                 {0,0,0,0,1,1,0,0,0},
                                                 {0,0,0,0,1,1,0,0,0},
                                                 {0,0,0,0,1,1,0,0,0},
                                                 {0,0,0,0,1,1,0,0,0}};
     private String[][][] entryTagAttributeNames = {{{""}, {""}, {""}, {""}, {"href"}, {"lang"}, {"lang"}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"href"}, {"lang"}, {"lang"}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"href"}, {"lang"}, {"lang"}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"href"}, {"lang"}, {"lang"}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"href"}, {"lang"}, {"lang"}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"href"}, {"lang"}, {"lang"}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"href"}, {"lang"}, {""}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"href"}, {"lang"}, {""}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"href"}, {"lang"}, {""}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"href"}, {"lang"}, {""}, {""}, {""}}};
     private String[][][] entryTagAttributeValues = {{{""}, {""}, {""}, {""}, {"divisions/16115/"}, {"cs"}, {"en"}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"divisions/16115/"}, {"cs"}, {"en"}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"divisions/16115/"}, {"cs"}, {"en"}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"divisions/16115/"}, {"cs"}, {"en"}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"divisions/16115/"}, {"cs"}, {"en"}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"divisions/16115/"}, {"cs"}, {"en"}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"divisions/16115/"}, {"cs"}, {""}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"divisions/16115/"}, {"cs"}, {""}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"divisions/16115/"}, {"cs"}, {""}, {""}, {""}},
                                                 {{""}, {""}, {""}, {""}, {"divisions/16115/"}, {"cs"}, {""}, {""}, {""}}};
 
     public AtomTagParserTest(String testName) {
         super(testName);
     }
 
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * Test of getTagName method, of class AtomTagParser.
      */
     public void testGetTagName() {
         int n = atomParser.getNumberOfEntries();
         for (int i = 0; i < n; i++) {
             AtomContentParser entry = atomParser.getEntry(i);
             int nn = entry.getNumberOfTags();
             for (int j = 0; j < nn; j++) {
                 assertEquals(entryTagNames[i][j],entry.getTag(j).getTagName());
             }
         }
     }
 
     /**
      * Test of getTagContent method, of class AtomTagParser.
      */
     public void testGetTagContent() {
         int n = atomParser.getNumberOfEntries();
         for (int i = 0; i < n; i++) {
             AtomContentParser entry = atomParser.getEntry(i);
             int nn = entry.getNumberOfTags();
             for (int j = 0; j < nn; j++) {
                 assertEquals(entryTagContents[i][j],entry.getTag(j).getTagContent());
             }
         }
     }
 
     /**
      * Test of getNumberOfAttributes method, of class AtomTagParser.
      */
     public void testGetNumberOfAttributes() {
         int n = atomParser.getNumberOfEntries();
         for (int i = 0; i < n; i++) {
             AtomContentParser entry = atomParser.getEntry(i);
             int nn = entry.getNumberOfTags();
             for (int j = 0; j < nn; j++) {
                 AtomTagParser tag = entry.getTag(j);
                 assertEquals(entryNumberOfTagAttributes[i][j],tag.getNumberOfAttributes());
             }
         }
     }
 
     /**
      * Test of getAttributeName method, of class AtomTagParser.
      */
     public void testGetAttributeName() {
         int n = atomParser.getNumberOfEntries();
         for (int i = 0; i < n; i++) {
             AtomContentParser entry = atomParser.getEntry(i);
             int nn = entry.getNumberOfTags();
             for (int j = 0; j < nn; j++) {
                 AtomTagParser tag = entry.getTag(j);
                 int nnn = tag.getNumberOfAttributes();
                 for (int k = 0; k < nnn; k++) {
                     assertEquals(entryTagAttributeNames[i][j][k],tag.getAttributeName(k));
                 }
             }
         }
     }
 
     /**
      * Test of getAllAttributeNames method, of class AtomTagParser.
      */
     public void testGetAllAttributeNames() {
         int n = atomParser.getNumberOfEntries();
         for (int i = 0; i < n; i++) {
             AtomContentParser entry = atomParser.getEntry(i);
             int nn = entry.getNumberOfTags();
             for (int j = 0; j < nn; j++) {
                 AtomTagParser tag = entry.getTag(j);
                List listOfAllTagAttributeNames = tag.getAllAttributeNames();
                 int nnn = 0;
                 if (listOfAllTagAttributeNames != null) {
                     nnn = listOfAllTagAttributeNames.size();
                 }
                 for (int k = 0; k < nnn; k++) {
                     assertEquals(entryTagAttributeNames[i][j][k],listOfAllTagAttributeNames.get(k));
                 }
             }
         }
     }
 
     /**
      * Test of getAttributeValue method, of class AtomTagParser.
      */
     public void testGetAttributeValue() {
         int n = atomParser.getNumberOfEntries();
         for (int i = 0; i < n; i++) {
             AtomContentParser entry = atomParser.getEntry(i);
             int nn = entry.getNumberOfTags();
             for (int j = 0; j < nn; j++) {
                 AtomTagParser tag = entry.getTag(j);
                 int nnn = tag.getNumberOfAttributes();
                 for (int k = 0; k < nnn; k++) {
                     assertEquals(entryTagAttributeValues[i][j][k],tag.getAttributeValue(k));
                 }
             }
         }
     }
 
     /**
      * Test of getAllAttributeValues method, of class AtomTagParser.
      */
     public void testGetAllAttributeValues() {
         int n = atomParser.getNumberOfEntries();
         for (int i = 0; i < n; i++) {
             AtomContentParser entry = atomParser.getEntry(i);
             int nn = entry.getNumberOfTags();
             for (int j = 0; j < nn; j++) {
                 AtomTagParser tag = entry.getTag(j);
                List listOfAllTagAttributeValues = tag.getAllAttributeValues();
                 int nnn = 0;
                 if (listOfAllTagAttributeValues != null) {
                     nnn = listOfAllTagAttributeValues.size();
                 }
                 for (int k = 0; k < nnn; k++) {
                     assertEquals(entryTagAttributeValues[i][j][k],listOfAllTagAttributeValues.get(k));
                 }
             }
         }
     }
 
 }
