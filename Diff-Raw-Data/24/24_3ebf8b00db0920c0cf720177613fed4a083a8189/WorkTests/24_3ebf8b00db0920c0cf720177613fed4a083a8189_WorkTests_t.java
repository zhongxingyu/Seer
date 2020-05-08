 /**
  * 
  */
 package openscriptures.text;
 
 import java.util.Date;
 
 import openscriptures.utils.Language;
 import openscriptures.utils.License;
 import junit.framework.TestCase;
 
 /**
  * @author Neal Audenaert
  */
 public class WorkTests extends TestCase {
     
     String EXAMPLE_TEXT = 
         "Ἰούδας Ἰησοῦ Χριστοῦ δοῦλος, ἀδελφὸς δὲ Ἰακώβου, τοῖς ἐν θεῷ πατρὶ " +
         "ἠγαπημένοις καὶ Ἰησοῦ Χριστῷ τετηρημένοις κλητοῖς·";
     
    @SuppressWarnings("unused")
     private static final String TOKENIZATION_PATTERN =
         "\\p{L}+|" +                                // Unicode letters
         "\\p{P}+|" +                                // Unicode punctuation
         "\\p{javaWhitespace}+|" +                   // Java whitespace (becase I can't figure out Unicode whitespace)
         "[^\\p{L}\\p{P}\\p{javaWhitespace}]+";      // everything else (typically ignored).
     
     public void testCreateWork() {
         Work work = new Work(new WorkId("Bible.grc.SBLGNT.2010"));
         assertNotNull(work);
         assertEquals(work.getWorkId(), "Bible.grc.SBLGNT.2010");
         assertEquals(work.getWorkUUID(), work);
     }
 
     public void testAssignValues() {
         Work work = new Work(new WorkId("Bible.grc.SBLGNT.2010"));
 
         String title = "The Greek New Testament: SBL Edition";
         work.setTitle(title);
         assertEquals("Incorrect title", title, work.getTitle());
 
         work.setType("Bible");
         assertEquals("Incorrect type", "Bible", work.getType());
 
         String pub = "Logos Bible Software and the Society of Biblical Literature";
         work.setPublisher(pub);
         assertEquals("Incorrect publihser", pub, work.getPublisher());
 
         work.setCreator("Michael W. Holmes");
         assertEquals("Incorrect creator", "Michael W. Holmes", work.getCreator());
 
         work.setLanguage(Language.lookup("grc"));
         assertEquals("Invalid language", Language.lookup("grc"), work.getLanguage());
 
         work.setAbbreviation("SBLGNT");
         assertEquals("Incorrect abbreviation", "SBLGNT", work.getAbbreviation());
 
         String copy = "Copyright 2010 Logos Bible Software and the Society of " +
                       "Biblical Literature. See SBLGNT.com for license details.";
         work.setCopyright(copy);
         assertEquals("Incorrect copyright", copy, work.getCopyright());
 
        work.setPublicationDate("2010-10-28");
        assertEquals("Unexpected publication date", 
                "2010-10-28", 
                work.getPublicationDate());
 
         work.setRefSystem("Bible.NRSVA");       // TODO need to perform lookups
         assertEquals("Incorrect ref system", "Bible.NRSVA", work.getRefSystem());
 
         work.setScope("Matt-Rev");
         assertEquals("Incorrect ref system", "Bible.NRSVA", work.getRefSystem());
 
         Date importDate = new Date();
         work.setImportDate(importDate);
         assertEquals("Incorrect import date", importDate, work.getImportDate());
 
         String desc = "this is a version of the Greek New Testament with a very permissive license";
         work.setDescription(desc);
         assertEquals("Incorrect description", desc, work.getDescription());
         
         work.setLicense(License.BY_NC_ND);
         assertEquals("Incorrect license", License.BY_NC_ND, work.getLicense());
         
 //        currently un-tested        
 //        work.setSourceURL(value);
     }
     
     
     public void testAddTokens() {
 //        List<Token> tokens = new ArrayList<Token>();
 //        Work work = new Work(new WorkId("Bible.grc.SBLGNT.2010"));
 //        Matcher mat = Pattern.compile(TOKENIZATION_PATTERN).matcher(EXAMPLE_TEXT);
 //        
 //        while (mat.find()) {
 //            tokens.add(work.addToken(mat.group()));
 //        }
 //        
 //        StringBuilder sb = new StringBuilder();
 //        for (Token t : tokens) {
 //            sb.append(t.getText());
 //        }
 //        
 //        assertEquals("Text and token list do not match", EXAMPLE_TEXT, sb.toString());
     }
     
     public void testTokenizse() {
 //        Work work = new Work(new WorkId("Bible.grc.SBLGNT.2010"));
 //        List<Token> tokens = work.tokenize(EXAMPLE_TEXT);
 //        
 //        StringBuilder sb = new StringBuilder();
 //        for (Token t : tokens) {
 //            sb.append(t.getText());
 //        }
 //        
 //        assertEquals("Text and token list do not match", EXAMPLE_TEXT, sb.toString());
     }
 }
