 package org.mdissjava.mdisscore.controller.bll;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.mdissjava.mdisscore.controller.bll.impl.AlbumManagerImpl;
 import org.mdissjava.mdisscore.controller.bll.impl.PhotoManagerImpl;
 import org.mdissjava.mdisscore.model.dao.factory.MorphiaDatastoreFactory;
 import org.mdissjava.mdisscore.model.pojo.Album;
 import org.mdissjava.mdisscore.model.pojo.Photo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.morphia.Datastore;
 
 public class PhotoManagerTest {
 
 	
 	
 	private PhotoManager photoManager;
 	private AlbumManager albumManager;
 	private final String NAME = "715_15_4_7357";
 	private final Logger logger = LoggerFactory.getLogger(this.getClass());
 	private final Album album = new Album();
 	private final Album albumMaster = new Album();
	private String photoId;
	private String albumId;
 	
 	@Before
 	public void init()
 	{
 		Datastore ds = MorphiaDatastoreFactory.getDatastore("test");
 		this.albumManager = new AlbumManagerImpl(ds);
 		this.photoManager = new PhotoManagerImpl(ds);
 	}
 	
 	@After
 	public void destroy() throws IOException {
		albumManager.deleteAlbum(albumId, album.getUserNick());
		photoManager.deletePhoto(photoId);
 		albumManager.forceDeleteAlbum(albumMaster);
 		this.photoManager = null;
 	}
 	
 	@Test
 	public void creationTest() throws IllegalStateException, IOException {
 		
 		this.logger.info("[TEST] testUpdate TagDaoImpl");
 		Datastore db = MorphiaDatastoreFactory.getDatastore("test");
 			
 		PhotoManagerImpl pManager = new PhotoManagerImpl(db);
 				
 		String tags = "animals, cats, dogs, police, dogs" ;
 		
 		String albumTitle = "Jaiak 2012";
 		String userNickname = "slok";
				
 		albumMaster.setTitle("Master");
 		albumMaster.setUserNick(userNickname);
 		
 		albumManager.insertAlbum(albumMaster);		
 
 		album.setTitle(albumTitle);
 		album.setUserNick(userNickname);
 		albumManager.insertAlbum(album);
 		
		List<Album> albums = albumManager.findAlbum(album);
		
		System.out.println("AlbumSize: "+albums.size());
		albumId = albums.get(0).getAlbumId();
		System.out.println("AlbumID: "+albumId);

		
 		this.photoManager.insertPhoto(NAME, userNickname, 
				"yeaaaaaaaaaaaah", albumId, true, false, 
 				"CC", tags);
 		 
		Photo photoNew = new Photo();
		photoNew.setDataId(NAME);
 
		List<Photo> photos = photoManager.findPhoto(photoNew);
		Photo photo = photos.get(0);
		photoId = photo.getPhotoId();
 		
 		if(photo!=null)
 		{
 			assertTrue(!(photo.getTags().isEmpty()));
 		}
 		
 	}
 
 	
 }
