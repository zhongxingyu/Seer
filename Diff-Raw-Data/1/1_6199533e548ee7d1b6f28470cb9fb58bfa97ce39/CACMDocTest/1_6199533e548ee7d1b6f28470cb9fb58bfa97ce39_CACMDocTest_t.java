 package com.bdt.kiradb;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.bdt.kiradb.mykdbapp.TextDocument;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 
 public class CACMDocTest {
     Core db;
 
     // Gets run before each method annotated with @Test
     @Before
     public void setup() throws KiraCorruptIndexException, IOException {
         db = new Core("KiraDBIndex");
         System.out.println("Creating Index...");
         db.createIndex();
     }
 
     // Gets run after any method annotated with @Test
     @After
     public void teardown() throws IOException {
         db.deleteIndex();
     }
 
 
     @Test
     public void testCACMAllDocs() throws IOException, InterruptedException, KiraException, ClassNotFoundException {
     	File dir = new File("testdocs");
     	if (!dir.isDirectory()) {
     		System.out.println("testdocs is not a directory, skipping test");
     		return;
     	}
 
     	String[] exts = {"txt"};
     	Collection<File> files = FileUtils.listFiles(dir, exts, false);
     	System.out.println("Indexing  " + files.size() + " documents...");
 
     	int nDocs = 0;
     	for (File f : files) {
         	//System.out.println("Found document: " + f.getName());
         	String baseName = f.getName();
         	final int lastPeriodPos = baseName.lastIndexOf('.');
             if (lastPeriodPos > 0) {
                 // Remove the last period and everything after it
             	baseName = baseName.substring(0, lastPeriodPos);
             }
         	//System.out.println("docId: " + baseName);
 
         	String fData = FileUtils.readFileToString(f);
         	int n = fData.length();
         	int i = 0;
         	for ( ; i < n; i++) {
         		char c = fData.charAt(i);
         		if (!Character.isWhitespace(c))
         			break;
         	}
         	// Beginning of title
         	int beg = i++;
         	while (i < n) {
         		char c = fData.charAt(i);
         		if (c < ' ')
         			break;
         		i++;
         	}
         	String title = fData.substring(beg, i).trim();
         	//System.out.println("title: " + title);
 
         	TextDocument doc = new TextDocument();
         	doc.setDocId(baseName);
         	doc.setTitle(title);
         	doc.setBody(fData);
 
         	db.storeObject(doc);
         	nDocs++;
 
     	}
         System.out.println("Indexed docs: " + nDocs);
 
 
     	List<Object> qResults = db.executeQuery(new TextDocument(), TextDocument.BODY, "system", Integer.MAX_VALUE, 0, null, true);
 
         assertNotNull("The CACM query result should not be null", qResults);
         System.out.println("Matched docs: " + qResults.size());
 
         assertTrue("The query should have matched exactly 719 documents", qResults.size() == 719);
 
 
     }
 
 
 }
