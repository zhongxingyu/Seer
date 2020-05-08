 /**
  * 
  */
 package com.github.podd.impl.file.test;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Rule;
 import org.junit.rules.TemporaryFolder;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.impl.LiteralImpl;
 import org.openrdf.model.impl.StatementImpl;
 import org.openrdf.model.vocabulary.RDF;
 
 import com.github.podd.api.file.PoddFileRepository;
 import com.github.podd.api.file.SSHFileReference;
 import com.github.podd.api.file.test.AbstractPoddFileRepositoryTest;
 import com.github.podd.api.test.TestConstants;
 import com.github.podd.impl.file.SSHFileReferenceImpl;
 import com.github.podd.impl.file.SSHFileRepositoryImpl;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * @author kutila
  * 
  */
 public class SSHFileRepositoryImplTest extends AbstractPoddFileRepositoryTest<SSHFileReference>
 {
     
     private static final String TEST_SSH_HOST = "localhost";
     private static final String TEST_SSH_SERVICE_PORT = "9856";
     private static final String TEST_SSH_FINGERPRINT = "ce:a7:c1:cf:17:3f:96:49:6a:53:1a:05:0b:ba:90:db";
     private static final String TEST_SSH_USERNAME = "salt";
     private static final String TEST_SSH_SECRET = "salt";
     
     @Rule
     public final TemporaryFolder tempDirectory = new TemporaryFolder();
 
     /** SSH File Repository server for tests */
     protected SSHService sshd;
     
     @Override
     protected Collection<URI> getExpectedTypes() throws Exception
     {
         final Collection<URI> types = new ArrayList<URI>();
         types.add(PoddRdfConstants.PODD_FILE_REPOSITORY);
         types.add(PoddRdfConstants.PODD_SSH_FILE_REPOSITORY);
         return types;
     }
     
     /*
      * Create a {@link Model} containing configuration details for an SSH File Repository.
      * 
      * (non-Javadoc)
      * 
      * @see com.github.podd.api.file.test.AbstractPoddFileRepositoryTest#getNewPoddFileRepository()
      */
     @Override
     protected PoddFileRepository<SSHFileReference> getNewPoddFileRepository() throws Exception
     {
         final Model model = new LinkedHashModel();
         model.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, PoddRdfConstants.PODD_BASE_ALIAS,
                 new LiteralImpl(SSHFileRepositoryImplTest.TEST_ALIAS)));
         model.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_FILE_REPOSITORY));
         model.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
         
         // ssh specific attributes
         model.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, new LiteralImpl(SSHFileRepositoryImpl.PROTOCOL_SSH)));
         model.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, new LiteralImpl(SSHFileRepositoryImplTest.TEST_SSH_HOST)));
         model.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, new LiteralImpl(
                         SSHFileRepositoryImplTest.TEST_SSH_SERVICE_PORT)));
         model.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, new LiteralImpl(
                         SSHFileRepositoryImplTest.TEST_SSH_FINGERPRINT)));
         model.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, new LiteralImpl(
                         SSHFileRepositoryImplTest.TEST_SSH_USERNAME)));
         model.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET,
                 new LiteralImpl(SSHFileRepositoryImplTest.TEST_SSH_SECRET)));
         
         return this.getNewPoddFileRepository(model);
     }
     
     @Override
     protected PoddFileRepository<SSHFileReference> getNewPoddFileRepository(final Model model) throws Exception
     {
         final PoddFileRepository result = new SSHFileRepositoryImpl(model);
         return result;
     }
     
     @Override
     protected Collection<Model> getIncompleteModels()
     {
         final Collection<Model> incompleteModels = new ArrayList<Model>();
         
         // - no "protocol"
         final Model model1 = new LinkedHashModel();
         model1.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, PoddRdfConstants.PODD_BASE_ALIAS,
                 new LiteralImpl(SSHFileRepositoryImplTest.TEST_ALIAS)));
         model1.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_FILE_REPOSITORY));
         model1.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
         
         model1.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, new LiteralImpl(SSHFileRepositoryImplTest.TEST_SSH_HOST)));
         model1.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, new LiteralImpl(
                         SSHFileRepositoryImplTest.TEST_SSH_SERVICE_PORT)));
         model1.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, new LiteralImpl(
                         SSHFileRepositoryImplTest.TEST_SSH_FINGERPRINT)));
         model1.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, new LiteralImpl(
                         SSHFileRepositoryImplTest.TEST_SSH_USERNAME)));
         model1.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET,
                 new LiteralImpl(SSHFileRepositoryImplTest.TEST_SSH_SECRET)));
         
         incompleteModels.add(model1);
         
         // - no "host"
         final Model model2 = new LinkedHashModel();
         model2.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, PoddRdfConstants.PODD_BASE_ALIAS,
                 new LiteralImpl(SSHFileRepositoryImplTest.TEST_ALIAS)));
         model2.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_FILE_REPOSITORY));
         model2.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
         
         model2.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, new LiteralImpl(SSHFileRepositoryImpl.PROTOCOL_SSH)));
         model2.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, new LiteralImpl(
                         SSHFileRepositoryImplTest.TEST_SSH_SERVICE_PORT)));
         model2.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, new LiteralImpl(
                         SSHFileRepositoryImplTest.TEST_SSH_FINGERPRINT)));
         model2.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, new LiteralImpl(
                         SSHFileRepositoryImplTest.TEST_SSH_USERNAME)));
         model2.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET,
                 new LiteralImpl(SSHFileRepositoryImplTest.TEST_SSH_SECRET)));
         
         incompleteModels.add(model2);
         
         // - no "fingerprint"
         final Model model3 = new LinkedHashModel();
         model3.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, PoddRdfConstants.PODD_BASE_ALIAS,
                 new LiteralImpl(SSHFileRepositoryImplTest.TEST_ALIAS)));
         model3.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_FILE_REPOSITORY));
         model3.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
         
         model3.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, new LiteralImpl(SSHFileRepositoryImpl.PROTOCOL_SSH)));
         model3.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, new LiteralImpl(SSHFileRepositoryImplTest.TEST_SSH_HOST)));
         model3.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, new LiteralImpl(
                         SSHFileRepositoryImplTest.TEST_SSH_SERVICE_PORT)));
         model3.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, new LiteralImpl(
                         SSHFileRepositoryImplTest.TEST_SSH_USERNAME)));
         model3.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET,
                 new LiteralImpl(SSHFileRepositoryImplTest.TEST_SSH_SECRET)));
         
         incompleteModels.add(model3);
         
         // - no protocol, host, port, fingerprint, username, secret
         final Model model4 = new LinkedHashModel();
         model4.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, PoddRdfConstants.PODD_BASE_ALIAS,
                 new LiteralImpl(SSHFileRepositoryImplTest.TEST_ALIAS)));
         model4.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_FILE_REPOSITORY));
         model4.add(new StatementImpl(SSHFileRepositoryImplTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
         
         incompleteModels.add(model4);
         return incompleteModels;
     }
     
     @Override
     protected SSHFileReference getNewFileReference(String alias, String fileIdentifier)
     {
         // prepare: create the FileReference to be validated
         final SSHFileReference fileReference = new SSHFileReferenceImpl();
         fileReference.setRepositoryAlias(alias);
         
         // prepare: get the name and path of File to be validated
         final String testFile = this.getClass().getResource(TestConstants.TEST_FILE).getFile();
         String fileName = testFile;
         String path = this.getClass().getResource(TestConstants.TEST_FILE).getPath();
         final int lastSlashPosition = testFile.lastIndexOf(File.separatorChar);
         if(lastSlashPosition != -1)
         {
             fileName = testFile.substring(lastSlashPosition + 1);
             path = testFile.substring(0, lastSlashPosition);
         }
         
         if (fileIdentifier != null)
         {
             fileReference.setFilename(fileIdentifier);
         }
         else
         {
             fileReference.setFilename(fileName);
         }
         fileReference.setPath(path);
         
         return fileReference;
     }
     
     protected void startRepositorySource() throws Exception
     {
         sshd = new SSHService();
         final File tempDirForHostKey = this.tempDirectory.newFolder();
         sshd.startTestSSHServer(Integer.parseInt(SSHFileRepositoryImplTest.TEST_SSH_SERVICE_PORT),
                 tempDirForHostKey);
     }
 
     protected void stopRepositorySource() throws Exception
     {
         if (sshd != null)
         {
             sshd.stopTestSSHServer();
         }
     }
 
 }
