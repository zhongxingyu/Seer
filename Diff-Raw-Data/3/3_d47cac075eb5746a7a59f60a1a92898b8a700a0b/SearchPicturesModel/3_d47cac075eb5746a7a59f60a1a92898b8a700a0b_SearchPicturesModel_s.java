 package ar.kennedy.is2011.models;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import ar.kennedy.is2011.db.dao.AbstractDao;
 import ar.kennedy.is2011.db.entities.AlbumEy;
 import ar.kennedy.is2011.db.entities.PictureEy;
 import ar.kennedy.is2011.db.exception.EntityNotFoundException;
 
 /**
  * @author mlabarinas
  */
 public class SearchPicturesModel extends AbstractModel {
 
 	private AbstractDao<PictureEy> pictureDao;
 	private AbstractDao<AlbumEy> albumDao;
 	
 	private static final String PICTURE_BY_USER_QUERY = "SELECT e FROM PictureEy e WHERE e.username = :1";
 	private static final String LAST_PICTURE_UPLOAD_BY_USER_QUERY = "SELECT e FROM PictureEy e WHERE e.username = :1 ORDER BY e.dateCreated DESC";
 	private static final String ALBUMS_TO_BE_DISPAYED_BY_VISIBILITY_QUERY = "SELECT a FROM AlbumEy a WHERE a.visibility = :1";
 	private static final String ALBUMS_TO_BE_DISPAYED_BY_OWNER_QUERY = "SELECT a FROM AlbumEy a WHERE a.owner = :1";
 	private static final String PICTURES_TO_BE_DISPAYED_BY_USER_QUERY = "SELECT e FROM PictureEy e WHERE e.albumId IN (:1)";
 	private static final String PICTURE_BY_ALBUM_QUERY = "SELECT e FROM PictureEy e WHERE e.albumId = :1";
 	
 private static final String PICTURE_BY_TAGS = "SELECT e FROM PictureEy e WHERE e.tags IN (:1)";
 	
 	public SearchPicturesModel() {
 		super();
 		
 		this.pictureDao = new AbstractDao<PictureEy>();
 		this.albumDao = new AbstractDao<AlbumEy>();
 	}
 	
 	public List<PictureEy> getPicturesByUsername(String username) {
 		List<PictureEy> pictures = null;
 		
 		try {
 			pictures = pictureDao.createCollectionQuery(PICTURE_BY_USER_QUERY, new Vector<Object>(Arrays.asList(new String[] {username})));
 		
 			return pictures;
 			
 		} catch(EntityNotFoundException e) {
 			return new ArrayList<PictureEy>();
 		}
 	}
 	
 	public PictureEy getLastPictureUploadByUser(String username) {
 		List<PictureEy> pictures = null;
 		
 		try {
 			pictures = pictureDao.createCollectionQuery(LAST_PICTURE_UPLOAD_BY_USER_QUERY, new Vector<Object>(Arrays.asList(new String[] {username})));
 		
 			return pictures.size() > 0 ? pictures.get(0) : null;
 			
 		} catch(EntityNotFoundException e) {
 			return null;
 		}
 	}
 	
 	public List<PictureEy> getPicturesByTags(Vector<Object> tags) {
 		List<PictureEy> pictures = null;
 
 		try {
 			pictures = pictureDao.createCollectionQuery(PICTURE_BY_TAGS, tags);
 
 		} catch (EntityNotFoundException e) {
 			return new ArrayList<PictureEy>();
 		}
 
 		return pictures;
 	}
 	
 	public Set<AlbumEy> getAlbumsToBeDisplayedByUser(String username) {
 		List<AlbumEy> albumsByVisibility = null;
 		List<AlbumEy> albumsByOwner = null;
 		Set<AlbumEy> albums = new HashSet<AlbumEy>();
 		
 		try {
 			albumsByVisibility = albumDao.createCollectionQuery(ALBUMS_TO_BE_DISPAYED_BY_VISIBILITY_QUERY, new Vector<Object>(Arrays.asList(new String[] {"public"})));
 			albumsByOwner = albumDao.createCollectionQuery(ALBUMS_TO_BE_DISPAYED_BY_OWNER_QUERY, new Vector<Object>(Arrays.asList(new String[] {username})));
 			
 			albums.addAll(albumsByVisibility);
 			albums.addAll(albumsByOwner);
 			
 			return albums;
 			
 		} catch(EntityNotFoundException e) {
 			return new HashSet<AlbumEy>();
 		}
 	}
 	
 	public List<PictureEy> getPicturesToBeDisplayedByUser(String username) {
 		Set<AlbumEy> albums = null;
 		List<PictureEy> pictures = null;
 		
 		try {
 			albums = getAlbumsToBeDisplayedByUser(username);
 			
 			if(albums.size() > 0) {
 				pictures = pictureDao.createCollectionQuery(PICTURES_TO_BE_DISPAYED_BY_USER_QUERY, new Vector<Object>(Arrays.asList(new String[] {getAlbumsSplit(albums)})));
 			}
 		
 			return pictures;
 			
 		} catch(EntityNotFoundException e) {
 			return new ArrayList<PictureEy>();
 		}
 	}
 	
 	public List<PictureEy> getPictureByAlbum(String albumId) {
 		List<PictureEy> pictures = null;
 
 		try {
 			pictures = pictureDao.createCollectionQuery(PICTURE_BY_ALBUM_QUERY, new Vector<Object>(Arrays.asList(new String[] {albumId})));
 
 		} catch (EntityNotFoundException e) {
 			return new ArrayList<PictureEy>();
 		}
 
 		return pictures;
 	}
 	
 	private String getAlbumsSplit(Set<AlbumEy> albums) {
 		StringBuilder splitAlbums = new StringBuilder();
 		
 		for(AlbumEy album : albums) {
 			splitAlbums.append(album.getAlbumId()).append(",");
 		}
 		
 		if(albums.size() > 0) {
 			return splitAlbums.toString().substring(0, splitAlbums.toString().length() - 1);
 		}
 		
 		return splitAlbums.toString();
 	}
 	
 }
