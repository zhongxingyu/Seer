 package fr.openwide.nuxeo.utils;
 
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.runtime.test.runner.Deploy;
 
 import fr.openwide.nuxeo.DefaultHierarchy;
 import fr.openwide.nuxeo.test.AbstractNuxeoTest;
 import fr.openwide.nuxeo.types.TypeFile;
 import fr.openwide.nuxeo.types.TypeWorkspaceRoot;
 import fr.openwide.nuxeo.utils.document.DocumentUtils;
 import fr.openwide.nuxeo.utils.document.PathFormatter;
 
 @Deploy({
     "org.nuxeo.ecm.platform.types.core"
 })
 public class DocumentUtilsTest extends AbstractNuxeoTest  {
 
     @Test
     public void testDiff() throws ClientException {
         DocumentModel testFile = documentManager.createDocumentModel("/", "test", TypeFile.TYPE);
         testFile.setPropertyValue(TypeFile.XPATH_TITLE, "Hello");
         testFile = documentManager.createDocument(testFile);
         
         DocumentModel testFile2 = documentManager.getDocument(testFile.getRef());
         testFile2.setPropertyValue(TypeFile.XPATH_TITLE, "World");
         testFile2.setPropertyValue(TypeFile.XPATH_DESCRIPTION, "!");
         
         List<String> differingProperties = DocumentUtils.getDifferingProperties(testFile, testFile2);
         
         Assert.assertTrue(differingProperties.contains(TypeFile.XPATH_TITLE));
         Assert.assertTrue(differingProperties.contains(TypeFile.XPATH_DESCRIPTION));
     }
     
     @Test
     public void testPathFormatter() throws ClientException {
         PathFormatter pathFormatter = new PathFormatter(documentManager);
         
         // General usage
         DocumentModel fileModel = documentManager
                 .createDocumentModel(DefaultHierarchy.WORKSPACES_PATH_AS_STRING, "myfile", TypeFile.TYPE);
         fileModel.setPropertyValue(TypeFile.XPATH_TITLE, "Foo");
         fileModel = documentManager.createDocument(fileModel);
         Assert.assertEquals("Default domain > Workspaces > Foo", pathFormatter.getFormattedPath(fileModel));
         pathFormatter.setSeparator("/");
         Assert.assertEquals("Default domain/Workspaces/Foo", pathFormatter.getFormattedPath(fileModel));
         pathFormatter.setRootType(TypeWorkspaceRoot.TYPE);
         Assert.assertEquals("Workspaces/Foo", pathFormatter.getFormattedPath(fileModel));
         pathFormatter.setShowLeaf(false);
         Assert.assertEquals("Workspaces", pathFormatter.getFormattedPath(fileModel));
         
         // Special cases
         pathFormatter = new PathFormatter(documentManager);
         DocumentModel rootDocument = documentManager.getRootDocument();
        String rootName = rootDocument.getName();
         Assert.assertEquals(rootName, pathFormatter.getFormattedPath(documentManager.getRootDocument()));
         pathFormatter.setShowLeaf(false);
         Assert.assertEquals(rootName, pathFormatter.getFormattedPath(documentManager.getRootDocument()));
     }
 }
