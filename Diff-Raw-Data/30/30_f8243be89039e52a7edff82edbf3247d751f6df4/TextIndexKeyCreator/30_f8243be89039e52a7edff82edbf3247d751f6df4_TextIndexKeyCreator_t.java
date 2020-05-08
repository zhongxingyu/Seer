 import com.sleepycat.db.DatabaseEntry;
 import com.sleepycat.db.DatabaseException;
 import com.sleepycat.db.SecondaryDatabase;
 import com.sleepycat.db.SecondaryMultiKeyCreator;
 
 import java.util.Iterator;
 import java.util.HashSet;
 import java.util.Set;
 import java.io.IOException;
 
 public class TextIndexKeyCreator implements SecondaryMultiKeyCreator {
     private XMLFileBinding theBinding;
 
     public TextIndexKeyCreator (XMLFileBinding theBinding1) {
         theBinding = theBinding1;
     }
 
     public void createSecondaryKeys(SecondaryDatabase secdDb,
             DatabaseEntry keyEntry,
             DatabaseEntry dataEntry,
             Set results) throws DatabaseException {
         DatabaseEntry key = null;
         String word;
         XMLFile xml = (XMLFile) theBinding.entryToObject(dataEntry);
         Set<String> textKeys = FileData.uniqTerms(xml.getContent());
         Iterator it = textKeys.iterator();
         while(it.hasNext()) {
             word = (String) it.next();
             key = new DatabaseEntry(word.getBytes());
             results.add(key);
         }
     }
 }
 
