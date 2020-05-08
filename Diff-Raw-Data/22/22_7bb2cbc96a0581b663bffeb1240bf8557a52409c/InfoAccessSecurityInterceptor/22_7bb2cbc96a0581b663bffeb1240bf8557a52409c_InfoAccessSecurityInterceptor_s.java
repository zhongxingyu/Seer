 package org.mdissjava.api.interceptors;
 
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.Provider;
 
 import org.jboss.resteasy.annotations.interception.ServerInterceptor;
 import org.jboss.resteasy.core.ResourceMethod;
 import org.jboss.resteasy.core.ServerResponse;
 import org.jboss.resteasy.spi.Failure;
 import org.jboss.resteasy.spi.HttpRequest;
 import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
 import org.mdissjava.api.helpers.ApiHelper;
 import org.mdissjava.mdisscore.model.dao.AlbumDao;
 import org.mdissjava.mdisscore.model.dao.PhotoDao;
 import org.mdissjava.mdisscore.model.dao.UserDao;
 import org.mdissjava.mdisscore.model.dao.factory.MorphiaDatastoreFactory;
 import org.mdissjava.mdisscore.model.dao.impl.AlbumDaoImpl;
 import org.mdissjava.mdisscore.model.dao.impl.PhotoDaoImpl;
 import org.mdissjava.mdisscore.model.dao.impl.UserDaoImpl;
 import org.mdissjava.mdisscore.model.pojo.Album;
 import org.mdissjava.mdisscore.model.pojo.Photo;
 import org.mdissjava.mdisscore.model.pojo.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.morphia.Datastore;
 
 
 @Provider
 @ServerInterceptor
 public class InfoAccessSecurityInterceptor implements PreProcessInterceptor {
 
 	private final Logger logger = LoggerFactory.getLogger(this.getClass());
 	private final String DATABASE =  "mdissphoto";
 	
 	@Override
 	public ServerResponse preProcess(HttpRequest request, ResourceMethod resourceMethod)
 			throws Failure, WebApplicationException {
 		
 		Datastore datastore = MorphiaDatastoreFactory.getDatastore(DATABASE);
 		
 		//get neccesary data
 		Map<String, List<String>> headers = request.getHttpHeaders().getRequestHeaders();
 			
 		this.logger.info("Executing Info Access Security Interceptor");
 	
 		String method = request.getHttpMethod();
 		String user = headers.get(ApiHelper.HEADER_KEY_USER).get(0);
 		URI uri = request.getUri().getAbsolutePath();
 		
 		if (method.equals("GET"))
 		{
 			String path = uri.getPath().toString();
 			String[] uriData = path.substring(1, path.length()-1).split("/");
 			
 			Map<String,String> hm = new HashMap<String,String>();
 			
 			//1st set of keyword/value
 			String keyword1 = uriData[3];
 			String value1 = uriData[4];
 			hm.put(keyword1, value1);
 			
 			Iterator it = hm.entrySet().iterator();
 
 			while (it.hasNext()) 
 			{
 				Map.Entry e = (Map.Entry)it.next();
 				
 				//TODO: Reflection instead of a simple "if else" to do this? 
 				
 				if (e.getKey().toString().toLowerCase().indexOf("user") != -1)
 				{
 					User requestedUser = new User();		
 					UserDao userDao = new UserDaoImpl();
 					
 					requestedUser = userDao.getUserByNick(e.getValue().toString());
 					boolean isFollowing = userDao.followsUser(user, requestedUser);
 					
 					if (requestedUser != null)
 					{
 						if(!requestedUser.getNick().equals(user))
 						{
							if ((!isFollowing) && (requestedUser.getConfiguration().isPrivate()))
 									{
 								this.logger.error("FORBIDDEN ACCESS EVENT: User {} tried to access restricted area.", user);
 								return (ServerResponse) Response.status(400).entity("Access error:  User " + user + " tried to access restricted area.").build();
 									}	
 						}
 					}
 				}
 				else if (e.getKey().toString().toLowerCase().indexOf("album") != -1)
 				{
 					Album album = new Album();
 					album.setAlbumId(e.getValue().toString());
 					
 					AlbumDao albumDao = new AlbumDaoImpl(datastore);
 					List<Album> albums = albumDao.findAlbum(album);
 					
 					if(albums.size() == 1)
 					{
 						Album requestedAlbum = new Album();
 						requestedAlbum = albums.get(0);
 						
 						UserDao userDao = new UserDaoImpl();
 						
 						User requestedAlbumOwner = new User();		
 						requestedAlbumOwner = userDao.getUserByNick(requestedAlbum.getUserNick());
 					
 						if(!requestedAlbumOwner.getNick().equals(user))
 						{
 							boolean isFollowing = userDao.followsUser(user, requestedAlbumOwner);
 							
							if ((!isFollowing) && (requestedAlbumOwner.getConfiguration().isPrivate()))
 							{
 								this.logger.error("FORBIDDEN ACCESS EVENT: User {} tried to access restricted area.", user);
 								return (ServerResponse) Response.status(400).entity("Access error:  User " + user + " tried to access restricted area.").build();
 							}	
 						}
 					}
 				}
 				else if (e.getKey().toString().toLowerCase().indexOf("photo") != -1)
 				{
 					Photo photo = new Photo();
 					photo.setPhotoId(e.getValue().toString());
 					
 					PhotoDao photoDao = new PhotoDaoImpl(datastore);
 					List<Photo> photos = photoDao.findPhoto(photo);
 					
 					if(photos.size() == 1) 
 					{
 						Photo requestedPhoto = new Photo();
 						requestedPhoto = photos.get(0);
 						
 						UserDao userDao = new UserDaoImpl();
 						
 						User requestedPhotoOwner = new User();		
 						requestedPhotoOwner = userDao.getUserByNick(requestedPhoto.getAlbum().getUserNick());
 						if(!requestedPhotoOwner.getNick().equals(user))
 						{
 							boolean isFollowing = userDao.followsUser(user, requestedPhotoOwner);
 							
							if ((!isFollowing) && (requestedPhotoOwner.getConfiguration().isPrivate()))
 							{
 								this.logger.error("FORBIDDEN ACCESS EVENT: User {} tried to access restricted area.", user);
 								return (ServerResponse) Response.status(400).entity("Access error:  User " + user + " tried to access restricted area.").build();
 							}
 						}
 					}				
 				}
 				
 				return null;
 			}
 		}
 		
 		return null;
 	}
 }
