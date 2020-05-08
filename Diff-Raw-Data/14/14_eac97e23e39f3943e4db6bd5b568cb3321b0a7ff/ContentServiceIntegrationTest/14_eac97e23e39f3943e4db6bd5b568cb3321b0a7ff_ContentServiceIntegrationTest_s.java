 package com.madalla;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.jcr.RepositoryException;
 import javax.swing.tree.TreeModel;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.markup.html.tree.LinkTree;
 import org.joda.time.DateTime;
 import org.springmodules.jcr.JcrTemplate;
 
 import com.madalla.bo.blog.BlogEntryData;
 import com.madalla.bo.blog.IBlogData;
 import com.madalla.bo.blog.IBlogEntryData;
 import com.madalla.bo.image.AlbumData;
 import com.madalla.bo.image.IAlbumData;
 import com.madalla.bo.image.IImageData;
 import com.madalla.bo.image.ImageData;
 import com.madalla.bo.page.ContentData;
 import com.madalla.bo.page.PageData;
 import com.madalla.bo.security.UserData;
 import com.madalla.cms.bo.impl.ocm.image.Image;
 import com.madalla.cms.bo.impl.ocm.page.Content;
 import com.madalla.service.BackupFile;
 import com.madalla.service.IDataService;
 import com.madalla.service.IRepositoryAdminService;
 
 public class ContentServiceIntegrationTest extends  AbstractSpringWicketTester{
 
 	Log log = LogFactory.getLog(this.getClass());
 	private IDataService contentService;
     private IRepositoryAdminService repositoryAdminService;
     private JcrTemplate jcrTemplate;
 	private final static String SITE = "test";
 	
 	protected List<String> getTestConfigLocations() {
 		List<String> configLocations = new ArrayList<String>();
 		configLocations.add("classpath:com/madalla/cms/service/ocm/applicationContext-cms.xml");
         configLocations.add("classpath:com/madalla/cms/jcr/applicationContext-jcr.xml");
         
 		return configLocations;
 	}
 	
 //fails cause ocm: namespace is not set anymore
 //not needed - all migrations done already
 //	public void testDataMigration(){
 //		RepositoryDataMigration.transformData(jcrTemplate, SITE);
 //	}
     public void testContentBackup(){
         repositoryAdminService.backupContentRoot();
         repositoryAdminService.backupContentSite();
 		List <BackupFile> files = repositoryAdminService.getBackupFileList();
 		for (int i = 0; i < files.size(); i++) {
 			if ("test-backup.xml".equals(files.get(i).getName())){
 				repositoryAdminService.restoreContentSite(files.get(i).getFile());
 			}
 		}
 
 		if (repositoryAdminService.isRollbackApplicationAvailable()){
 			repositoryAdminService.rollbackApplicationRestore();
 		}
 		if (repositoryAdminService.isRollbackSiteAvailable()){
 			repositoryAdminService.rollbackSiteRestore();
 		}
 
     }
 
     public void testContentExplorer(){
     	TreeModel treeModel = repositoryAdminService.getSiteContent();
     	assertNotNull(treeModel);
     	
     	LinkTree tree = new LinkTree("test", treeModel);
     	
     	appTester.startComponent(tree);
     	//tree.render();
     	//int size = tree.size();
     	//tree.g
     	//tree.render();
     	
     }
     
     private final static String CONTENT_ID = "testContent";
     private final static String CONTENT_PARENT = "testParentNode";
 
     public void testImageGetSet() throws FileNotFoundException{
     	InputStream stream = this.getClass().getResourceAsStream("test1.jpg");
     	assertNotNull(stream);
     	
     	final String name = "image1";
     	IAlbumData originalAlbum = contentService.getOriginalsAlbum();
     	String imagePath = contentService.createImage(originalAlbum, name, stream);
     	
     	AlbumData album;
     	{
     		album = contentService.getAlbum("testAlbum");
     		IImageData image = contentService.getImage(imagePath);
     		contentService.addImageToAlbum(album, image.getName());
     	}
     	{
     		IImageData testImage = contentService.getImage(imagePath);
     	    assertNotNull(testImage);
     	}
     	{
     	    List<ImageData> list = contentService.getAlbumImages(album);
     	    assertNotNull(list);
     	    assertTrue(list.size() > 0);
     	    Image image = (Image)list.get(0);
     	    assertNotNull(image);
     	    assertNotNull(image.getImageFull());
     	    assertNotNull(image.getImageThumb());
     	}
     	{
     		List<ImageData> list = contentService.getAlbumOriginalImages();
     	    assertNotNull(list);
     	    assertTrue(list.size() > 0);
     	}
     	TreeModel treeModel =  contentService.getAlbumImagesAsTree(album);
     	assertNotNull(treeModel);
     	assertTrue(treeModel.getChildCount(treeModel.getRoot()) >= 1);
     }
         
     private final static String BLOG = "testBlog";
     private final static String BLOGCATEGORY = "testCategory";
     private final static String BLOGDESCRIPTION = "Blog Description Test";
     private final static String BLOGKEYWORDS = "test, keyword";
     private final static String BLOGTEXT = "test text";
     private final static String BLOGTITLE = "test title";
     private final static DateTime BLOGDATE = new DateTime();
     
     public void testBlogGetSet(){
     	IBlogData blog = contentService.getBlog(BLOG);
     	assertNotNull(blog);
     	BlogEntryData blogEntry = contentService.getNewBlogEntry(blog, BLOGTITLE, BLOGDATE);
     	assertNotNull(blogEntry);
     	blogEntry.setDescription(BLOGDESCRIPTION);
     	String path = blogEntry.getId();
     	contentService.saveBlogEntry(blogEntry);
     	
     	IBlogEntryData testEntry = contentService.getBlogEntry(path);
     	assertNotNull(testEntry);
     	
     	contentService.deleteNode(path);
     }
     
     public void testUserCreate(){
         String username = "test1User";
         String password = "password";
         UserData user = contentService.getNewUser(username, password);
         
         UserData test = contentService.getUser(username);
         assertNotNull(test);
         
         UserData nouser = contentService.getNewUser("nouser","pwd");
         assertNull(nouser);
         
         contentService.deleteNode(test.getId());
     }
     
     public void testUserGetAll(){
     	Collection<UserData> list = contentService.getUsers();
     	for(UserData user : list){
     		System.out.println(user);
     	}
     }
     
     public void setDataService(IDataService contentService) {
 		this.contentService = contentService;
 	}
 
     public void setRepositoryAdminService(IRepositoryAdminService contentAdminService) {
         this.repositoryAdminService = contentAdminService;
     }
 
 	public void setJcrTemplate(JcrTemplate jcrTemplate) {
 		this.jcrTemplate = jcrTemplate;
 	}
 
 	public JcrTemplate getJcrTemplate() {
 		return jcrTemplate;
 	}
 
 
 }
