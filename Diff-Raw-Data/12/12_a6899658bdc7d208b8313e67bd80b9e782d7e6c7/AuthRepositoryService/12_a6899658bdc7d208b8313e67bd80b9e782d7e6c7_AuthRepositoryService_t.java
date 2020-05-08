 package com.madalla.webapp;
 
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.List;
 import java.util.Locale;
 
 import javax.swing.tree.TreeModel;
 
 import org.joda.time.DateTime;
 
 import com.madalla.bo.SiteData;
 import com.madalla.bo.blog.BlogData;
 import com.madalla.bo.blog.BlogEntryData;
 import com.madalla.bo.blog.IBlogData;
 import com.madalla.bo.image.AlbumData;
 import com.madalla.bo.image.IAlbumData;
 import com.madalla.bo.image.IImageData;
 import com.madalla.bo.image.ImageData;
 import com.madalla.bo.page.ContentData;
 import com.madalla.bo.page.PageData;
 import com.madalla.bo.page.ResourceData;
 import com.madalla.bo.security.IUser;
 import com.madalla.bo.security.UserData;
 import com.madalla.bo.security.UserSiteData;
 import com.madalla.service.IRepositoryService;
 import com.madalla.webapp.security.IAuthenticator;
 
 //TODO Solution for authorizing calls to RepositoryService
 public class AuthRepositoryService implements IRepositoryService, Serializable {
 	private IRepositoryService service;
 	private IUser user;
 	
	public AuthRepositoryService(){

	}
 	public AuthRepositoryService(IRepositoryService service){
 		this.service = service;
 	}
 
 	public IUser getUser() {
 		return user;
 	}
 
 	public void setUser(IUser user) {
 		this.user = user;
 	}
 
 	public void addImageToAlbum(IAlbumData album, String name) {
 		service.addImageToAlbum(album, name);
 	}
 
 	public String createImage(IAlbumData album, String name, InputStream inputStream) {
 		return service.createImage(album, name, inputStream);
 	}
 
 	public void deleteNode(String id) {
 		service.deleteNode(id);
 	}
 
 	public AlbumData getAlbum(String name) {
 		return null;
 	}
 
 	public List<ImageData> getAlbumImages(AlbumData album) {
 		return null;
 	}
 
 	public TreeModel getAlbumImagesAsTree(AlbumData album) {
 		return null;
 	}
 
 	public List<ImageData> getAlbumOriginalImages() {
 		return null;
 	}
 
 	public BlogData getBlog(String blogName) {
 		return null;
 	}
 
 	public List<BlogEntryData> getBlogEntries(BlogData blog) {
 		return null;
 	}
 
 	public BlogEntryData getBlogEntry(String id) {
 		return null;
 	}
 
 	public ContentData getContent(PageData parent, String name, Locale locale) {
 		return null;
 	}
 
 	public ContentData getContent(PageData parent, String name) {
 		return null;
 	}
 
 	public ContentData getContent(String id) {
 		return null;
 	}
 
 	public ResourceData getContentResource(PageData page, String name) {
 		return null;
 	}
 
 	public String getContentText(PageData page, String id) {
 		return null;
 	}
 
 	public String getContentText(PageData page, String id, Locale locale) {
 		return null;
 	}
 
 	public IImageData getImage(String id) {
 		return null;
 	}
 
 	public String getLocaleId(String id, Locale locale) {
 		return null;
 	}
 
 	public BlogEntryData getNewBlogEntry(IBlogData blog, String title, DateTime date) {
 		return null;
 	}
 
 	public UserData getNewUser(String username, String password) {
 		return null;
 	}
 
 	public AlbumData getOriginalsAlbum() {
 		return null;
 	}
 
 	public PageData getPage(String name) {
 		return null;
 	}
 
 	public InputStream getResourceStream(String path, String property) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public SiteData getSite(String name) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<SiteData> getSiteEntries() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public UserData getUser(String username) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IAuthenticator getUserAuthenticator() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<UserSiteData> getUserSiteEntries(UserData user) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public List<UserData> getUsers() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public boolean isBlogNode(String id) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean isContentNode(String id) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean isContentPasteNode(String id) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean isDeletableNode(String id) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean isImageNode(String id) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void pasteContent(String id, ContentData content) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void saveBlogEntry(BlogEntryData blogEntry) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void saveContent(ContentData content) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void saveContentResource(ResourceData data) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void saveSite(SiteData data) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void saveUser(UserData user) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void saveUserSiteEntries(UserData user, List<SiteData> sites) {
 		// TODO Auto-generated method stub
 		
 	}
 }
