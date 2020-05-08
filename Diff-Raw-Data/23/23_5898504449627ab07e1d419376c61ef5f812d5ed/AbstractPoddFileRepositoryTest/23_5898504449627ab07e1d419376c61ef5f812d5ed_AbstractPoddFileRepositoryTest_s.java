 /**
  * 
  */
 package com.github.podd.api.file.test;
 
 import java.util.Collection;
 import java.util.Set;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.ValueFactoryImpl;
 
 import com.github.podd.api.file.FileReference;
 import com.github.podd.api.file.PoddFileRepository;
 import com.github.podd.exception.FileReferenceNotSupportedException;
 import com.github.podd.exception.IncompleteFileRepositoryException;
 
 /**
  * 
  * @author kutila
  */
 public abstract class AbstractPoddFileRepositoryTest<T extends FileReference>
 {
     protected static final String TEST_ALIAS = "test_alias";
     protected static final URI TEST_ALIAS_URI = ValueFactoryImpl.getInstance().createURI(
             "http://purl.org/podd/test_alias");
     
     protected PoddFileRepository<T> testFileRepository;
     
     /**
      * @return A Collection of URIs representing the expected types of the test FileRepository
      *         instance
      */
     protected abstract Collection<URI> getExpectedTypes() throws Exception;
     
     /**
      * @return A new {@link PoddFileRepository} instance for use by the test
      */
     protected abstract PoddFileRepository<T> getNewPoddFileRepository() throws Exception;
     
     /**
      * @return A new {@link PoddFileRepository} instance for use by the test
      */
     protected abstract PoddFileRepository<T> getNewPoddFileRepository(final Model model) throws Exception;
     
     /**
      * @param alias
      *            The alias to be assigned to the created FileReference.
      * @param fileIdentifier
      *            If this parameter is not null, its value will be set as the file Identifier
      * @return A new FileReference instance for use by the test
      */
     protected abstract T getNewFileReference(String alias, String fileIdentifier);
     
     /**
      * @return A Collection of Models that do not contain sufficient information to create a
      *         FileRepository object
      */
     protected abstract Collection<Model> getIncompleteModels();
     
     /**
      * Start a File Repository source for test
      */
     protected abstract void startRepositorySource() throws Exception;
     
     /**
      * Stop the test File Repository source
      */
     protected abstract void stopRepositorySource() throws Exception;
     
     @Before
     public void setUp() throws Exception
     {
         this.testFileRepository = this.getNewPoddFileRepository();
     }
     
     @After
     public void tearDown() throws Exception
     {
         this.testFileRepository = null;
     }
     
     @Test
     public void testCanHandle() throws Exception
     {
         final String thisRepositorysAlias = this.testFileRepository.getAlias();
         final T fileReference = this.getNewFileReference(thisRepositorysAlias, null);
         
         Assert.assertTrue("Repository should be able to handle this file reference",
                 this.testFileRepository.canHandle(fileReference));
     }
     
     @Test
     public void testCanHandleWithDifferentAliases() throws Exception
     {
         final T fileReference = this.getNewFileReference("no_such_alias", null);
         
         Assert.assertFalse("Repository should not be able to handle this file reference",
                 this.testFileRepository.canHandle(fileReference));
     }
     
     @Test
     public void testCanHandleWithNullReference() throws Exception
     {
         Assert.assertFalse("Repository should not be able to handle NULL file reference",
                 this.testFileRepository.canHandle(null));
     }
     
     @Test
     public void testCreateFileRepositoryWithIncompleteModel() throws Exception
     {
         final Collection<Model> incompleteModels = this.getIncompleteModels();
         
         for(final Model nextModel : incompleteModels)
         {
             try
             {
                 this.getNewPoddFileRepository(nextModel);
                 Assert.fail("Should have thrown an IncompleteFileRepositoryException");
             }
             catch(final IncompleteFileRepositoryException e)
             {
                 Assert.assertNotNull(e.getModel());
                 Assert.assertEquals("SSH repository configuration incomplete", e.getMessage());
             }
         }
     }
     
     @Test
     public void testGetAlias() throws Exception
     {
         final String alias = this.testFileRepository.getAlias();
         Assert.assertNotNull("NULL alias", alias);
     }
     
     @Test
     public void testGetTypes() throws Exception
     {
         final Set<URI> types = this.testFileRepository.getTypes();
         Assert.assertNotNull("NULL types", types);
         Assert.assertEquals("Expected 2 TYPEs", 2, types.size());
         final Collection<URI> expectedTypes = this.getExpectedTypes();
         for(final URI uri : expectedTypes)
         {
             Assert.assertTrue("Expected TYPE missing", types.contains(uri));
         }
     }
     
     /**
      * This test starts up an internal file repository source and therefore can be slow.
     * 
     * TODO: fix test failure when run through maven 
      */
    @Ignore
     @Test
     public void testValidateSuccess() throws Exception
     {
         try
         {
             final T fileReference = this.getNewFileReference(AbstractPoddFileRepositoryTest.TEST_ALIAS, null);
             this.startRepositorySource();
             Assert.assertTrue("File Reference should have been valid", this.testFileRepository.validate(fileReference));
         }
         finally
         {
             this.stopRepositorySource();
         }
     }
     
     /**
      * The internal file repository source is not started since this test fails before reaching a
      * stage where the remote repository is accessed.
      */
     @Test
     public void testValidateWithAliasMismatch() throws Exception
     {
         final T fileReference = this.getNewFileReference("no_such_alias", null);
         try
         {
             this.testFileRepository.validate(fileReference);
             Assert.fail("Should have thrown a FileReferenceNotSupportedException");
         }
         catch(final FileReferenceNotSupportedException e)
         {
             Assert.assertEquals("cannot handle file reference for validation", e.getMessage());
         }
     }
     
     /**
      * This test starts up an internal file repository source and therefore can be slow.
      */
     @Test
     public void testValidateWithNonExistentFile() throws Exception
     {
         try
         {
             final T fileReference =
                     this.getNewFileReference(AbstractPoddFileRepositoryTest.TEST_ALIAS, "no_such_file.zip");
             this.startRepositorySource();
             Assert.assertFalse("File Reference should be invalid", this.testFileRepository.validate(fileReference));
         }
         finally
         {
             this.stopRepositorySource();
         }
     }
     
 }
