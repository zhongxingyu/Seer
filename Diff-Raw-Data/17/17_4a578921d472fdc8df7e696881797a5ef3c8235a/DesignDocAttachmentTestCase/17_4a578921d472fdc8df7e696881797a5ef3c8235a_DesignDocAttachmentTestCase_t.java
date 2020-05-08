 package org.jcouchdb.db;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 import java.io.UnsupportedEncodingException;
 
 import org.jcouchdb.document.DesignDocument;
import org.jcouchdb.exception.NotFoundException;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DesignDocAttachmentTestCase
 {
     private static Logger log = LoggerFactory.getLogger(DesignDocAttachmentTestCase.class);
     
     @Test
     public void test() throws UnsupportedEncodingException
     {
         Database db = LocalDatabaseTestCase.createDatabaseForTest();
         
        DesignDocument doc = null;
        try
        {
            doc = db.getDesignDocument("listDoc");
        }
        catch(NotFoundException e)
        {
            
        }
        
        if (doc == null)
        {
            doc = new DesignDocument("listDoc");
        }
         
         db.createAttachment(doc.getId(), doc.getRevision(), "test.txt", "text/plain", "TestTest".getBytes("UTF-8"));
         
         String data = new String(db.getAttachment("_design/listDoc", "test.txt"), "UTF-8");
         assertThat(data, is("TestTest"));
         
         log.info("Data = {}", data);
     }
 
 }
