 /**
  * 
  */
 package com.github.podd.impl.file.test;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.UUID;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.rules.TemporaryFolder;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.impl.StatementImpl;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.RDF;
 
 import com.github.podd.api.file.PoddFileRepository;
 import com.github.podd.api.file.SSHFileReference;
 import com.github.podd.api.file.test.AbstractPoddFileRepositoryTest;
 import com.github.podd.impl.file.SSHFileRepositoryImpl;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * @author kutila
  * 
  */
 public class SSHFileRepositoryImplTest extends AbstractPoddFileRepositoryTest<SSHFileReference>
 {
     
     @Rule
     public final TemporaryFolder tempDirectory = new TemporaryFolder();
     
     /** SSH File Repository server for tests */
     protected SSHService sshd;
     
     private Path sshDir = null;
     
     @Before
     @Override
     public void setUp() throws Exception
     {
         super.setUp();
         sshDir = tempDirectory.newFolder("podd-filerepository-manager-impl-test").toPath();
     }
     
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
         model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                         AbstractPoddFileRepositoryTest.TEST_ALIAS)));
         model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_FILE_REPOSITORY));
         model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
         
         // ssh specific attributes
         model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, ValueFactoryImpl.getInstance().createLiteral(
                         SSHFileRepositoryImpl.PROTOCOL_SSH)));
         model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_HOST)));
         model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_SERVICE_PORT)));
         model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_FINGERPRINT)));
         model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_USERNAME)));
         model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_SECRET)));
         
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
         model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                         AbstractPoddFileRepositoryTest.TEST_ALIAS)));
         model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_FILE_REPOSITORY));
         model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
         
         model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_HOST)));
         model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_SERVICE_PORT)));
         model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_FINGERPRINT)));
         model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_USERNAME)));
         model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_SECRET)));
         
         incompleteModels.add(model1);
         
         // - no "host"
         final Model model2 = new LinkedHashModel();
         model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                         AbstractPoddFileRepositoryTest.TEST_ALIAS)));
         model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_FILE_REPOSITORY));
         model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
         
         model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, ValueFactoryImpl.getInstance().createLiteral(
                         SSHFileRepositoryImpl.PROTOCOL_SSH)));
         model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_SERVICE_PORT)));
         model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_FINGERPRINT)));
         model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_USERNAME)));
         model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_SECRET)));
         
         incompleteModels.add(model2);
         
         // - no "fingerprint"
         final Model model3 = new LinkedHashModel();
         model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                         AbstractPoddFileRepositoryTest.TEST_ALIAS)));
         model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_FILE_REPOSITORY));
         model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
         
         model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, ValueFactoryImpl.getInstance().createLiteral(
                         SSHFileRepositoryImpl.PROTOCOL_SSH)));
         model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_HOST)));
         model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_SERVICE_PORT)));
         model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_USERNAME)));
         model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, ValueFactoryImpl.getInstance().createLiteral(
                         SSHService.TEST_SSH_SECRET)));
         
         incompleteModels.add(model3);
         
         // - no protocol, host, port, fingerprint, username, secret
         final Model model4 = new LinkedHashModel();
         model4.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                 PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                         AbstractPoddFileRepositoryTest.TEST_ALIAS)));
         model4.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_FILE_REPOSITORY));
         model4.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                 PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
         
         incompleteModels.add(model4);
         return incompleteModels;
     }
     
     @Override
     protected SSHFileReference getNewFileReference(final String alias, final String fileIdentifier)
     {
         try
         {
             return SSHService.getNewFileReference(alias, fileIdentifier,
                    tempDirectory.newFolder("sshfilerepositoryimpltest-resources-" + UUID.randomUUID().toString())
                            .toPath());
         }
         catch(IOException e)
         {
             throw new RuntimeException(e);
         }
     }
     
     @Override
     protected void startRepositorySource() throws Exception
     {
         this.sshd = new SSHService();
         this.sshd.startTestSSHServer(Integer.parseInt(SSHService.TEST_SSH_SERVICE_PORT), sshDir);
     }
     
     @Override
     protected void stopRepositorySource() throws Exception
     {
         if(this.sshd != null)
         {
             this.sshd.stopTestSSHServer(sshDir);
         }
     }
     
 }
