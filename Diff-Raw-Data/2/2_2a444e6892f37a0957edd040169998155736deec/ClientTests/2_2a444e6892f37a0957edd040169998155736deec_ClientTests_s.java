 package org.cometdocs.tests;
 
 import static org.junit.Assert.*;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.cometdocs.*;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class ClientTests {
 
     private static String userEMail = null;
     private static String userPassword = null;
     private static String userAPIKey = null;
     private static String testFolderName = null;
     private static FolderInfo testFolderInfo = null;
 
     private static final Charset charset = Charset.forName("UTF-8");
     
     private static Client client = null;
     private static AuthenticationToken authToken = null;
 
     /**
      * Read the testing credentials from "credentials.txt" file.
      */
     private static void readCredentials() throws Exception
     {
         if (!Files.exists(Paths.get("credentials.txt")))
         {
             throw new Exception("The file 'credentials.txt' has not been found!\n" +
                                 "You must provide some credentials to use the CometDocs API.\n" +
                                 "Copy or rename the 'credentials.sample.txt' file and fill in your email, CometDocs password, CometDocs API key and testing folder name.");
         }
 
         String credentials = new String(Files.readAllBytes(Paths.get("credentials.txt")));
 
         String lineEnding = credentials.contains("\r\n") ? "\r\n" : "\n";
 
         String[] tokens = credentials.split(lineEnding);
 
         if (tokens.length != 4)
         {
             throw new Exception("You must provide exactly, and in the same order, your: email, password, API key and the tests folder!");
         }
 
         userEMail = tokens[0];
         userPassword = tokens[1];
         userAPIKey = tokens[2];
         testFolderName = tokens[3];
     }
     
     @BeforeClass
     public static void testFixtureSetUp() throws Exception
     {
     	readCredentials();
     	
         client = new Client();
         
         canAuthenticate();
 
         Folder root = client.getFolder(authToken);
         
         for (FolderInfo f : root.getFolders())
         {
         	if (f.getName().equals(testFolderName))
         	{
         		testFolderInfo = f;
         		break;
         	}
         }
 
         if (testFolderInfo == null)
         {
             throw new Exception(String.format("Unable to find tests folder '%s'!\nPlease create it first.", testFolderName));
         }
     }
     
     private void assertClean() throws Exception
     {
         Folder testFolder = client.getFolder(authToken, testFolderInfo);
 
         for (FileInfo file : testFolder.getFiles())
         {
         	client.deleteFile(authToken, file);
         }
         
         for (FolderInfo folder : testFolder.getFolders())
         {
         	client.deleteFolder(authToken, folder);
         }
 
         testFolder = client.getFolder(authToken, testFolderInfo);
 
         assertEquals(0, testFolder.getFiles().length);
         assertEquals(0, testFolder.getFolders().length);
     }
     
 	public static void canAuthenticate() throws Exception
 	{
 		authToken = client.authenticate(userEMail, userPassword, userAPIKey);
 		
 		assertFalse(authToken.getValue().trim().isEmpty());
 	}
 	
 	@Test
 	public void canConvertAFile() throws Exception
 	{
         assertClean();
 
         File file = new File("a.txt");
         file.setContent("ABC".getBytes(charset));
 
         FileInfo fileInfo = client.uploadFile(authToken, file, testFolderInfo);
 
         Folder testFolder = client.getFolder(authToken, testFolderInfo);
 
         assertEquals(1, testFolder.getFiles().length);
         assertEquals(fileInfo.getId(), testFolder.getFiles()[0].getId());
         assertEquals(fileInfo.getName(), testFolder.getFiles()[0].getName());
         assertEquals(fileInfo.getExtension(), testFolder.getFiles()[0].getExtension());
 
         ConversionType convType = new ConversionType("", "PDF");
 
         FileInfo pdfInfo = client.convertFile(authToken, fileInfo, convType);
 
         // assertTrue(pdfInfo.getExtension().equalsIgnoreCase("pdf"));
 
         testFolder = client.getFolder(authToken, testFolderInfo);
 
         assertEquals(1, testFolder.getFiles().length);
 
         assertEquals(fileInfo.getId(), testFolder.getFiles()[0].getId());
         assertEquals(fileInfo.getName(), testFolder.getFiles()[0].getName());
         assertEquals(fileInfo.getExtension(), testFolder.getFiles()[0].getExtension());
 
         Conversion[] conversions = client.getConversions(authToken, fileInfo);
 
         assertEquals(1, conversions.length);
 
         assertEquals(pdfInfo.getId(), conversions[0].getId());
         assertEquals(fileInfo.getId(), conversions[0].getParentId());
         assertEquals(convType, conversions[0].getType());
         // assertEquals(pdfInfo.getExtension(), conversions[0].getExtension());
 	}
 	
     @Test
     public void canCreateFileObject()
     {
         File file1 = new File("File1");
 
         assertEquals("File1", file1.getName());
         assertEquals(null, file1.getExtension());
 
         File file2 = new File("File1.txt");
 
         assertEquals("File1", file2.getName());
         assertEquals("txt", file2.getExtension());
 
         File file3 = new File("File2.txt.xml");
 
         assertEquals("File2.txt", file3.getName());
         assertEquals("xml", file3.getExtension());
     }
     
     @Test
     public void canCreateAFolder() throws Exception
     {
     	assertClean();
 
     	Folder testFolder = client.getFolder(authToken, testFolderInfo);
     	
     	String name = "__testFolder";
     	
     	FolderInfo newFolder = client.createFolder(authToken, testFolder, name);
     	
     	assertTrue(newFolder.getName().equals(name));
     	
     	testFolder = client.getFolder(authToken, testFolderInfo);
     	
     	Folder match = null;
     	for (Folder f : testFolder.getFolders())
     	{
     		if (f.getID() == newFolder.getID())
     		{
     			match = f;
     			break;
     		}
     	}
     	
     	assertNotNull(match);
     	
     	assertEquals(match.getName(), newFolder.getName());
     }
 	
     @Test
     public void canDeleteAFile() throws Exception
     {
         assertClean();
 
         File file = new File("Test.txt");
         file.setContent(new byte[0]);
 
         FileInfo info = client.uploadFile(authToken, file, testFolderInfo);
 
         Folder testFolder = client.getFolder(authToken, testFolderInfo);
 
         assertEquals(1, testFolder.getFiles().length);
         assertEquals(file.getName(), testFolder.getFiles()[0].getName());
         assertEquals(file.getExtension(), testFolder.getFiles()[0].getExtension());
 
         client.deleteFile(authToken, info);
 
         testFolder = client.getFolder(authToken, testFolderInfo);
 
         assertEquals(0, testFolder.getFiles().length);
     }
 	
     @Test
     public void canGetAFolder() throws Exception
     {
         assertClean();
 
         File file1 = new File("a.txt");
         File file2 = new File("b.txt");
         File file3 = new File("c.txt");
 
         client.uploadFile(authToken, file1, testFolderInfo);
         client.uploadFile(authToken, file2, testFolderInfo);
         client.uploadFile(authToken, file3, testFolderInfo);
 
         Folder testFolder = client.getFolder(authToken, testFolderInfo);
 
         assertNotNull(testFolder.getFolders());
 
         assertNotNull(testFolder.getFiles());
         assertEquals(3, testFolder.getFiles().length);
 
         assertEquals(file1.getName(), testFolder.getFiles()[0].getName());
         assertEquals(file1.getExtension(), testFolder.getFiles()[0].getExtension());
 
         assertEquals(file2.getName(), testFolder.getFiles()[1].getName());
         assertEquals(file2.getExtension(), testFolder.getFiles()[1].getExtension());
 
         assertEquals(file3.getName(), testFolder.getFiles()[2].getName());
         assertEquals(file3.getExtension(), testFolder.getFiles()[2].getExtension());
     }
     
     @Test
     public void canGetCategories() throws Exception
     {
         Category[] categories = client.getCategories();
 
         assertTrue(categories.length > 10);
         
         boolean hasArt = false;
         boolean hasBusiness = false;
         boolean hasUnicorns = false;
         for (Category cat : categories)
         {
         	hasArt |= cat.getName().equals("Art");
         	hasBusiness |= cat.getName().equals("Business");
         	hasUnicorns |= cat.getName().equals("Unicorns");
         }
         
         assertTrue(hasArt);
         assertTrue(hasBusiness);
         assertFalse(hasUnicorns);
     }
 
     @Test
    public void CanGetConversionTypes() throws Exception
     {
         ConversionType[] types = client.getConversionTypes();
 
         assertTrue(types.length > 10);
         
         boolean hasPDF2DOC = false;
         boolean hasPDF2XLS = false;
         boolean hasPDF2XYZ = false;
         for (ConversionType type : types)
         {
         	hasPDF2DOC |= type.toString().equals("PDF2DOC");
         	hasPDF2XLS |= type.toString().equals("PDF2XLS");
         	hasPDF2XYZ |= type.toString().equals("PDF2XYZ");
         }
         
         assertTrue(hasPDF2DOC);
         assertTrue(hasPDF2XLS);
         assertFalse(hasPDF2XYZ);
     }
 
     @Test
     public void canGetMethods() throws Exception
     {
         String[] methods = client.getMethods();
 
         Set<String> methodsName = new HashSet<String>();
         for (String method : methods)
         {
         	methodsName.add(method.split("\\(")[0]);
         }
         
         Set<String> clientMethods = new HashSet<String>();
 		for (Method method : Client.class.getDeclaredMethods())
 		{
 			if (!Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()))
 			{
 				clientMethods.add(method.getName());
 			}
 		}
         
 		assertTrue(methodsName.size() >= 18);
 		assertTrue(clientMethods.equals(methodsName));
         
         return;
     }
 
     @Test
     public void canGetPublicFiles() throws Exception
     {
         FileInfo[] files = client.getPublicFiles(authToken);
 
         assertTrue(files.length > 100);
         
         boolean hasPDF = false;
         boolean hasJPG = false;
         boolean hasXYZ = false;
         for (FileInfo file : files)
         {
         	hasPDF |= file.getExtension().equals("pdf");
         	hasJPG |= file.getExtension().equals("jpg");
         	hasXYZ |= file.getExtension().equals("xyz");
         }
         
         assertTrue(hasPDF);
         assertTrue(hasJPG);
         assertFalse(hasXYZ);
     }
 
     @Test
     public void canGetSharedFiles() throws Exception
     {
         FileInfo[] files = client.getSharedFiles(authToken);
 
         assertEquals(0, files.length);
     }
 
     @Test
     public void canInvalidateToken() throws Exception
     {
     	assertClean();
     	
     	client.getFolder(authToken);
     	
     	client.invalidateToken(authToken);
     	
     	boolean hasThrownInvalidTokenException = false;
     	try
     	{
     		client.getFolder(authToken);
     	}
     	catch(InvalidTokenException e)
     	{
     		hasThrownInvalidTokenException = true;
     	}
     	finally
     	{
     		// Ensure we're authenticated again for next tests
     		canAuthenticate();
     	}
     	
     	assertTrue(hasThrownInvalidTokenException);
     }
     
     @Test
     public void canSendAFile() throws Exception
     {
         assertClean();
 
         File file = new File("a.txt");
         file.setContent("ABC".getBytes(charset));
 
         FileInfo fileInfo = client.uploadFile(authToken, file, testFolderInfo);
 
         client.sendFile(authToken, fileInfo, new String[] { userEMail }, "some.user@company.com", "Hello!");
 
         // Assert you've received the email :)
     }
     
     @Test
     public void canUploadAndDownloadAFile() throws Exception
     {
         assertClean();
 
         String content = "The peanut, or groundnut (Arachis hypogaea), is a species in the legume or \"bean\" family (Fabaceae). The peanut was probably first domesticated and cultivated in the valleys of Paraguay.[1] It is an annual herbaceous plant growing 30 to 50 cm (1.0 to 1.6 ft) tall. The leaves are opposite, pinnate with four leaflets (two opposite pairs; no terminal leaflet), each leaflet is 1 to 7 cm (⅜ to 2¾ in) long and 1 to 3 cm (⅜ to 1 inch) broad.\n" +
         				 "The flowers are a typical peaflower in shape, 2 to 4 cm (0.8 to 1.6 in) (¾ to 1½ in) across, yellow with reddish veining. Hypogaea means \"under the earth\"; after pollination, the flower stalk elongates causing it to bend until the ovary touches the ground. Continued stalk growth then pushes the ovary underground where the mature fruit develops into a legume pod, the peanut – a classical example of geocarpy. Pods are 3 to 7 cm (1.2 to 2.8 in) long, containing 1 to 4 seeds.[2]\n" +
         				 "Peanuts are known by many other local names such as earthnuts, ground nuts, goober peas, monkey nuts, pygmy nuts and pig nuts.[3] Despite its name and appearance, the peanut is not a nut, but rather a legume.";
         byte[] contentBytes = content.getBytes(charset);
         File inputFile = new File("Peanuts.txt");
         inputFile.setContent(contentBytes);
 
         FileInfo info = client.uploadFile(authToken, inputFile, testFolderInfo);
         assertEquals(contentBytes.length, info.getSize());
 
         File outputFile = client.downloadFile(authToken, info);
 
         String outputContent = new String(outputFile.getContent(), charset);
 
         assertEquals(content, outputContent);
     }
     
     @Test
     public void canUploadAFileFromUrl() throws Exception
     {
         assertClean();
 
         String fileUrl = "http://www.imf.org/external/np/res/commod/Table4.pdf";
 
         client.uploadFileFromUrl(authToken, fileUrl, testFolderInfo);
 
         Folder root = client.getFolder(authToken, testFolderInfo);
 
         FileInfo info = root.getFiles()[0];
 
         assertEquals(1, root.getFiles().length);
         assertEquals("Table4", info.getName());
         assertEquals("pdf", info.getExtension());
 
         File outputFile = client.downloadFile(authToken, info);
 
         HttpClient httpClient = new DefaultHttpClient();
         HttpGet get = new HttpGet(fileUrl);
         HttpResponse httpResponse = httpClient.execute(get);
 
         byte[] fileContent = EntityUtils.toByteArray(httpResponse.getEntity());
          
         assertTrue(Arrays.equals(fileContent, outputFile.getContent()));
     }
 }
