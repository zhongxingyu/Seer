 /*
    Copyright 2012 Xebia Nederland B.V.
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.xebia.os.maven.couchdocsplugin;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.SecureRandom;
 
 import org.codehaus.plexus.util.IOUtil;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 import org.junit.runner.RunWith;
 
 import com.google.common.base.Optional;
 import com.xebia.os.maven.couchdocsplugin.CouchDatabaseException;
 import com.xebia.os.maven.couchdocsplugin.CouchFunctionsImpl;
 import com.xebia.os.maven.couchdocsplugin.LocalDocument;
 import com.xebia.os.maven.couchdocsplugin.RemoteDocument;
 import com.xebia.os.maven.couchdocsplugin.junit.ConditionalTestRunner;
 import com.xebia.os.maven.couchdocsplugin.junit.EnvironmentCondition;
 import com.xebia.os.maven.couchdocsplugin.junit.EnvironmentCondition.Kind;
 
 /**
  * Integration test for the {@link CouchFunctionsImpl}.
  *
  * <p>This test requires manual intervention.</p>
  *
  * <ul>
  * <li>Make sure CouchDB is running, update the BASE_URL below as needed.</li>
  * </ul>
  *
  * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
  */
 @RunWith(ConditionalTestRunner.class)
 @EnvironmentCondition(name = "COUCHDB_INTEGRATION_TESTS", kind = Kind.ENVIRONMENT_VARIABLE)
 public class CouchFunctionsImplTest {
 
     private static final String BASE_URL = "http://admin:admin@localhost:5984";
 
     @Rule public TemporaryFolder tempDir = new TemporaryFolder();
 
     @Test
     public void playScenario() throws IOException {
         final String databaseName = "test-database-" + (new SecureRandom()).nextLong();
         final CouchFunctionsImpl impl = new CouchFunctionsImpl(BASE_URL);
 
         // 0. isExistentDatabase() -> false
         assertFalse("The database randomly named \"" + databaseName + "\" should not exist at the start of the test.",
                 impl.isExistentDatabase(databaseName));
 
         // 1. createDatabase().
         impl.createDatabase(databaseName);
 
         // 2. isExistentDatabase() -> true
         assertTrue("The database \"" + databaseName + "\" should have been created and isExistentDatabase() should find it.",
                 impl.isExistentDatabase(databaseName));
 
         // 3. download() -> not found
         Optional<RemoteDocument> remoteDoc = impl.download(databaseName, "_design/Demo");
         assertFalse("The document \"_design/Demo\" should not exist in the database.", remoteDoc.isPresent());
 
         // 4. upload()
         File file = newTempFile("/design_doc.js");
         LocalDocument localDoc = new LocalDocument(file);
         localDoc.load();
         assertEquals("The local document should have the id \"_design/Demo\".", "_design/Demo", localDoc.getId());
         impl.upload(databaseName, localDoc);
 
         // 5. download() -> found
         remoteDoc = impl.download(databaseName, "_design/Demo");
         assertTrue("The document \"_design/Demo\" should now exist in the database, it was just uploaded.", remoteDoc.isPresent());
 
         // 6. delete()
         impl.delete(databaseName, remoteDoc.get());
 
         // 7. download() -> not found
         remoteDoc = impl.download(databaseName, "_design/Demo");
         assertFalse("The document \"_design/Demo\" should no longer exist in the database.", remoteDoc.isPresent());
 
         // 8. deleteDatabase()
         impl.deleteDatabase(databaseName);
 
         // 9. isExistentDatabase() -> false
         assertFalse("The database randomly named \"" + databaseName + "\" should no longer exist.",
                 impl.isExistentDatabase(databaseName));
     }
 
    /**
     * Proves that {@linkplain https://github.com/xebia/couch-docs-maven-plugin/issues/7} is absent.
     */
    @Test
    public void createDatabaseWithWeirdCharactersInTheName() throws IOException {
        final String databaseName = "test/da+ta(ba)$_$e-" + (new SecureRandom()).nextLong();
        final CouchFunctionsImpl impl = new CouchFunctionsImpl(BASE_URL);

        // 0. isExistentDatabase() -> false
        assertFalse("The database randomly named \"" + databaseName + "\" should not exist at the start of the test.",
        impl.isExistentDatabase(databaseName));

        // 1. createDatabase().
        impl.createDatabase(databaseName);

        // 2. isExistentDatabase() -> true
        assertTrue("The database \"" + databaseName + "\" should have been created and isExistentDatabase() should find it.",
        impl.isExistentDatabase(databaseName));

        // 3. clean up
        impl.deleteDatabase(databaseName);
    }

     @Test
     public void shouldWrapServerErrors() throws IOException {
         try {
             final CouchFunctionsImpl impl = new CouchFunctionsImpl(BASE_URL);
             impl.createDatabase("Database-Names-Cannot-Have-Capital-Letters");
             fail("An exception should have been thrown.");
         } catch (CouchDatabaseException e) {
             assertEquals(400, e.getResponseCode());
         }
     }
 
     private File newTempFile(final String source) throws IOException, FileNotFoundException {
         File result = tempDir.newFile();
         final InputStream dummyData = UpdateCouchDocsTest.class.getResourceAsStream(source);
         FileOutputStream fos = null;
         try {
             fos = new FileOutputStream(result);
             IOUtil.copy(dummyData, fos);
         } finally {
             IOUtil.close(dummyData);
             IOUtil.close(fos);
         }
         return result;
     }
 }
