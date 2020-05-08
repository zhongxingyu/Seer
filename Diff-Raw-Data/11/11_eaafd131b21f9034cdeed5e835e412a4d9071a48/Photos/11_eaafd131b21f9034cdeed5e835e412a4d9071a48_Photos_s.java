 package org.mdissjava.api;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 
 import org.jboss.resteasy.spi.HttpRequest;
 import org.mdissjava.api.helpers.ApiHelper;
 import org.mdissjava.commonutils.properties.PropertiesFacade;
 import org.mdissjava.mdisscore.model.dao.AlbumDao;
 import org.mdissjava.mdisscore.model.dao.PhotoDao;
 import org.mdissjava.mdisscore.model.dao.TagDao;
 import org.mdissjava.mdisscore.model.dao.factory.MorphiaDatastoreFactory;
 import org.mdissjava.mdisscore.model.dao.impl.AlbumDaoImpl;
 import org.mdissjava.mdisscore.model.dao.impl.PhotoDaoImpl;
 import org.mdissjava.mdisscore.model.dao.impl.TagDaoImpl;
 import org.mdissjava.mdisscore.model.pojo.Album;
 import org.mdissjava.mdisscore.model.pojo.Photo;
 import org.mdissjava.mdisscore.model.pojo.Tag;
 
 import com.google.code.morphia.Datastore;
 
 @Path("/photos")
 public class Photos {
 
 	//constants
 	private final String GLOBAL_PROPS_KEY = "globals";
 	private final String MORPHIA_DATABASE_KEY = "morphia.db";
 	private Datastore datastore = null;
 	private String userName = null;
 	
 	public Photos(@Context HttpRequest request) throws IllegalArgumentException, IOException {
 		this.userName = ApiHelper.getUserFromHttpRequest(request);
 		PropertiesFacade propertiesFacade = new PropertiesFacade();
 		String database = propertiesFacade.getProperties(GLOBAL_PROPS_KEY).getProperty(MORPHIA_DATABASE_KEY);
 		this.datastore = MorphiaDatastoreFactory.getDatastore(database);
 	}
 	
 	@GET
 	@Path("/{photoId}")
 	@Produces("application/json")
 	public Response getPhoto(@PathParam("photoId") String photoId){
 		
 		Photo photo = new Photo();
 		photo.setPhotoId(photoId);
 		
 		PhotoDao photoDao = new PhotoDaoImpl(this.datastore);
 		List<Photo> photos = photoDao.findPhoto(photo);
 		
 		if (photos.size() == 1)
 			return Response.status(200).entity(photos.get(0)).build();
 		else
 			return Response.status(400).entity("Error retrieving photo").build();
 		
 	}
 	
 	@PUT
 	@Path("/{photoId}")
 	@Consumes("application/json")
 	public Response updatePhoto(@PathParam("photoId") String photoId, Photo photo){
 	
 		PhotoDao photoDao = new PhotoDaoImpl(this.datastore);
 		//Search if photo exists
 		Photo p = new Photo();
 		p.setPhotoId(photoId);
 		
 		List<Photo> photos = photoDao.findPhoto(p);
 		
 		if (photos.size() == 1)
 		{
 			photo.setId(photos.get(0).getId());
 			photo.setPhotoId(photos.get(0).getPhotoId());
 			
 			photoDao.updatePhoto(photo);
 			
 			return Response.status(200).entity(photoDao.findPhoto(p).get(0)).build();
 		}
 		else
 			return Response.status(400).entity("Error updating photo info. Photo not found.").build();
 	}
 	
 	@POST
 	@Consumes("application/json")
 	public Response createPhoto(Photo photo){
 		
 		/* photo has set: 
 		*	-DataID [Required]
 		*	-PhotoID [Required]
 		*	-Title [Required]
 		*	-License
 		*	-PublicPhoto
 		*	-Plus18
 		*	-Tags (List<String>)
 		*	-Metadata
 		*
 		*	Besides, it is stored in the "Master" album by default.
 		*
 		*/
 		
 		//the other are not necessary, only title, dataID and photoID
 		if (photo.getDataId().isEmpty() || photo.getPhotoId().isEmpty() || photo.getTitle().isEmpty())
 		{
 			return Response.status(400).entity("Error creating photo. Some arguments is/are null, can't continue with the action").build();
 		}
 		
 		PhotoDao photoDao = new PhotoDaoImpl(this.datastore);
 		
 		//search if photo already exists
 		List<Photo> photos = photoDao.findPhoto(photo);
 		
 		if (photos.size() > 0)
 		{
 			return Response.status(400).entity("Error creating photo. Photo already exists in DB.").build();
 		}
 		
 		//set date to now!
 		photo.setUploadDate(new Date());
 		photo.setRandom(Math.random());
 		//create a public token (with some blocks of the uuid)
 		String publicToken = UUID.randomUUID().toString();
 		String[] splittedToken = publicToken.split("-");
 		publicToken = splittedToken[0] + splittedToken[2] + splittedToken[1];
 		
 		photo.setPublicToken(publicToken);
 		
 		//By default every photo uploaded this way will be set in the default "Master" album
 		Album a = new Album();
 		a.setUserNick(this.userName);
 		a.setTitle("Master");
 		
 		AlbumDao albumDao = new AlbumDaoImpl(this.datastore);
 		List<Album> albums = albumDao.findAlbum(a);
 		if(albums.size() == 1)
 		{		
 			Album album = new Album();
 			album = albums.get(0);
 			
 			//add photo to album
 			album.getPhotos().add(photo);
 			
 			//update the album
 			albumDao.updateAlbum(album);
 			
 			photo.setAlbum(album);
 			
 			try
 			{
 				List<String> tags4Search = photo.getTags();
 				
 				TagDao tagDao = new TagDaoImpl(this.datastore);
 				
 				Iterator<String> iterator = tags4Search.listIterator();
 				while(iterator.hasNext())
 				{
 					Tag newTag = new Tag();
 					newTag.setDescription(iterator.next());
 					try
 					{
 					List<Tag> tagList = tagDao.findTag(newTag);
 					if(tagList.isEmpty())
 					{
 						List<Photo> photoList = new ArrayList<Photo>();
 						photoList.add(photo);
 						newTag.setPhotos(photoList);
 						tagDao.insertTag(newTag);
 					}
 					else if(!tagList.isEmpty())
 					{
 						List<Photo> photoList = tagDao.findTag(newTag).get(0).getPhotos();
 						photoList.add(photo);
 						newTag.setPhotos(photoList);
 						tagDao.updateTag(newTag);
 					}
 					
 					}
 					catch(Exception e)
 					{
 						e.printStackTrace();
 					}
 					
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
			photoDao.insertPhoto(photo);
 			return Response.status(200).entity("Photo successfully created").build();		
 		}
 		
 		else
 			return Response.status(400).entity("Error while creating photo.").build();		
 	}
 	
 	@DELETE
 	@Path("/{photoId}")
 	public Response deletePhoto(@PathParam("photoId") String photoId){
 		
 		Photo photo = new Photo();
 		photo.setPhotoId(photoId);
 		
 		PhotoDao photoDao = new PhotoDaoImpl(this.datastore);
 		TagDao tagDao = new TagDaoImpl(this.datastore);
 		
 		List<Photo> photoList = photoDao.findPhoto(photo);
 		
 		if(photoList.size() == 1)
 		{
 			photo = photoList.get(0);
 			List<String> stringTags = photo.getTags();
 			if(stringTags != null)
 			{
 				Iterator<String> iterator = stringTags.listIterator();
 				while(iterator.hasNext())
 				{
 					Tag newTag = new Tag();
 					newTag.setDescription(iterator.next());
 					List<Tag> tagList = tagDao.findTag(newTag);
 					if(!(tagList.isEmpty()))
 					{
 						newTag = tagList.get(0);
 						List<Photo> photos = newTag.getPhotos();
 						
 						if((!(photos.isEmpty()))&&(photos.contains(photo)))
 						{
 							photos.remove(photo);
 							newTag.setPhotos(photos);
 							tagDao.updateTag(newTag);
 							List<Photo> updatedPhotoList = newTag.getPhotos();
 							if(updatedPhotoList.isEmpty())
 							{
 								tagDao.deleteTag(newTag);
 							}
 						}
 					}
 					
 				}
 			}
 			
 			//Delete photo from the album
 			Album a = photo.getAlbum();
 			
 			if (a != null)
 			{	
 				a.getPhotos().remove(photo);
 				AlbumDao albumDao = new AlbumDaoImpl(this.datastore);
 				albumDao.updateAlbum(a);				
 			}
 			
 			//Delete the photo
 			photoDao.deletePhoto(photo);
 			return Response.status(200).entity("Photo successfully deleted").build();		
 		}
 		else
 			return Response.status(400).entity("Error photo doesn't exist").build();		
 	}
 	
 	@GET
 	@Path("/{photoId}/album")
 	@Produces("application/json")
 	public Response getPhotoAlbum(@PathParam("photoId") String photoId){
 		
 		Photo photo = new Photo();
 		photo.setPhotoId(photoId);
 		
 		PhotoDao photoDao = new PhotoDaoImpl(this.datastore);
 		List<Photo> photos = photoDao.findPhoto(photo);
 		
 		if (photos.size() == 1)
 		{
 			Album album = new Album();
 			Album a = new Album();
 			AlbumDao albumDao = new AlbumDaoImpl(this.datastore);
 			
 			String albumId = photos.get(0).getAlbum().getAlbumId();
 			
 			a.setAlbumId(albumId);
 			album = albumDao.findAlbum(a).get(0);
 			
 			return Response.status(200).entity(album).build();
 		}
 		else
 			return Response.status(400).entity("Error retrieving photo").build();
 		
 	}
 
 }
