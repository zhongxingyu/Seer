 package com.zarcode.data.resources.v1;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.UriInfo;
 
 import com.google.gdata.client.photos.PicasawebService;
 import com.google.gdata.data.photos.AlbumEntry;
 import com.zarcode.common.ApplicationProps;
 import com.zarcode.common.Util;
 import com.zarcode.data.dao.BucketDao;
 import com.zarcode.data.exception.AllBucketsAreFullException;
 import com.zarcode.data.gdata.PicasaClient;
 import com.zarcode.data.model.BucketDO;
 import com.zarcode.data.resources.ResourceBase;
 import com.zarcode.platform.model.AppPropDO;
 
 @Path("/v1/photos")
 public class Photo extends ResourceBase {
 	
 	private Logger logger = Logger.getLogger(Photo.class.getName());
 	
 	@Context 
 	UriInfo uriInfo = null;
     
 	@Context 
     Request request = null;
 	
 	String container = null;
 	
 	private static int NO_ACTIVE_BUCKETS = 10;
 	
 	
 	/**
 	 * This method creates object instance of a remote bucket in Picasa.
 	 * 
 	 * @param album
 	 * @return
 	 */
 	private BucketDO createBucket(AlbumEntry album) {
 		BucketDO bucket = new BucketDO();
 		bucket.setAlbumId(album.getId());
 		bucket.setBytesUsed(album.getBytesUsed());
 		bucket.setFullFlag(false);
		bucket.setAlbumName(album.getName());
 		bucket.setRemainingPhotos(album.getPhotosLeft());
 		return bucket;
 	}
 
 	@GET 
 	@Path("/activeBucket")
 	@Produces("application/json")
 	public BucketDO getActiveBucket() {
 		BucketDO target = null;
 		int i = 0;
 		AlbumEntry album = null;
 		BucketDO b = null;
 	
 		/*
 		 * get local buckets
 		 */
 		logger.info("Getting active buckets ... ");
 		BucketDao dao = new BucketDao();
 		List<BucketDO> buckets = dao.getAllActiveBuckets();
 		logger.info("No of buckets found: " + (buckets == null ? 0 : buckets.size()));
 		
 		if (buckets != null && buckets.size() > 0) {
 			logger.info("Found a local store of buckets -- " + buckets.size());
 			for (i=0; i<buckets.size(); i++) {
 				b = buckets.get(i);
 				if (b.getRemainingPhotos() > 10) {
 					target = b;
 					break;
 				}
 				else {
 					dao.markFull(b);
 				}
 			}
 			if (target == null) {
 				logger.warning("Unable to available bucket -- all of our buckets are full.");
 				throw new AllBucketsAreFullException();
 			}
 		}
 		/*
 		 * if local store is empty, go to picasa and create a local store
 		 */
 		else {
 			logger.info("Trying to go Picasa to create a local store of buckets ...");
 			PicasawebService service = new PicasawebService("DockedMobile");
 			
 			try {
 				logger.info(">>> Invoking Picasa service  ... ");
 				PicasaClient client = new PicasaClient(service);
 				List<AlbumEntry> albums = client.getAlbums();
 				logger.info(">>> Got response from Picasa service  ");
 				
 				int count = 0;
 				if (albums != null && albums.size() > 0) {
 					for (i=0; i<albums.size(); i++) {
 						album = albums.get(i);
 						if (album.getPhotosLeft() > 100) {
 							b = createBucket(album);
 							if (target == null) {
 								target = b;
 							}
 							dao.addBucket(b);
 							count++;
 							if (count == NO_ACTIVE_BUCKETS) {
 								break;
 							}
 						}
 					}
 				}
 			}
 			catch (Exception e) {
 				logger.severe("[EXCEPTION] -- Unable to complete communicatation with Picasa services --> " + e.getMessage() + "\n" + Util.getStackTrace(e));
 			}
 			
 		}
 		/*
 		 * okay, we have a local store, return first best album
 		 */
 		
 		return target;
 		
 	} // getActiveBucket
 	
 	
 }
