 /* This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.bionicmessage.funambol.citadel.store;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * A test class for the EmailObjectStore
  * @author matt
  */
 public class EmailObjectStoreTest {
 
     Properties props = null;
 
     public EmailObjectStoreTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() throws Exception {
         props = new Properties();
         /* Set up your config here */
         File configFile = new File("/Users/matt/.emailobjectstore/eosprops");
         FileInputStream confStream = new FileInputStream(configFile);
         props.load(confStream);
     }
 
     @After
     public void tearDown() {
     }
 
     @Test
     public void syncHeaderTest() throws Exception {
         props.setProperty(CtdlFnblConstants.PURGE_DB_OPTION, "");
         EmailObjectStore eos = new EmailObjectStore(props);
         eos.startSync();
         List<String> msgsInMail = eos.listMessagesInRoom("Mail");
         Iterator<String> it = msgsInMail.iterator();
         while (it.hasNext()) {
             String key = it.next();
             CitadelMailObject cmo = eos.getMessageByPointer(key);
             System.out.format("Message %s, From: %s\n", key, cmo.getFrom()).flush();
         }
         eos.close();
     }
 
     @Test
     public void syncAddBodies() throws Exception {
         EmailObjectStore eos = new EmailObjectStore(props);
         eos.startSync();
         List<String> msgsInMail = eos.listMessagesInRoom("Mail");
         Iterator<String> it = msgsInMail.iterator();
         while (it.hasNext()) {
             String key = it.next();
             CitadelMailObject cmo = eos.getFilledMessageByPointer(key);
             System.out.format("Message %s, From: %s\n", key, cmo.getFrom()).flush();
             System.out.format("Message body size: %d\n", cmo.getData().length()).flush();
         }
         eos.close();
     }
     @Test
     public void stillHaveBodies() throws Exception {
         EmailObjectStore eos = new EmailObjectStore(props);
         eos.startSync();
         List<String> msgsInMail = eos.listMessagesInRoom("Mail");
         Iterator<String> it = msgsInMail.iterator();
         while (it.hasNext()) {
             String key = it.next();
             CitadelMailObject cmo = eos.getFilledMessageByPointer(key);
             assertTrue(cmo.hasData());
         }
         eos.close();
     }
     @Test
     public void syncAddParts() throws Exception {
         EmailObjectStore eos = new EmailObjectStore(props);
         eos.startSync();
         List<String> msgsInMail = eos.listMessagesInRoom("Mail");
         Iterator<String> it = msgsInMail.iterator();
         while (it.hasNext()) {
             String key = it.next();
             CitadelMailObject cmo = eos.getFilledMessageByPointer(key);
             eos.fillPartsForMessage(cmo);
             System.out.println("Object "+key+ " has "+cmo.getAttachedParts().size() + " attached parts");
         }
         eos.close();
     }
 }
