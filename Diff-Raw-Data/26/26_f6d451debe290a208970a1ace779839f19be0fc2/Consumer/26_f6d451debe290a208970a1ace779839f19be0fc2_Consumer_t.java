 import java.util.ArrayList;
 import java.util.concurrent.BlockingQueue;
 
 import com.google.gdata.data.photos.PhotoEntry;
 import com.google.gdata.util.ServiceException;
 
 
 public class Consumer implements Runnable {
 	private final BlockingQueue<PhotoEntry> queue;
 	// private final HashMap<Integer, Object> photos;
 	private final ArrayList<String> photoMetaList;
 
 	Consumer(BlockingQueue<PhotoEntry> q, ArrayList<String> photoMetaList){ 
 		queue = q; 
 		this.photoMetaList = photoMetaList;
 	}
 
 
 	public void run() {
 		try {
 			while (true) 
 			{ consume(queue.take()); }
 		}
 		catch (InterruptedException ex) {}
 	}
 
 
 	void consume(PhotoEntry photoInfo)  {
 
 		synchronized(photoMetaList){
 			StringBuilder data = new StringBuilder();
 			data.append("<newEntry>");
			//data.append("\n");
 			data.append("<GPhotoID>" + photoInfo.getGphotoId() +"</GPhotoID>");
 			if(photoInfo.getAlbumId() != null){
				//data.append("\n");
 				data.append("<AlbumID>" + photoInfo.getAlbumId() + "</AlbumID>");
 			}
 			if(photoInfo.getTitle() != null){
				data.append("<Title>" + photoInfo.getTitle().getPlainText() + "</Title>");
 		   } 
 			try {
 				if(photoInfo.getTimestamp() != null){
 				//	data.append("\n");
 					data.append("<Timestamp>" + photoInfo.getTimestamp().toString() + "</Timestamp>");
 				}
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			if(photoInfo.getHtmlLink() != null){
				//data.append("\n");
 				data.append("<Link>" + photoInfo.getHtmlLink().getHref() + "</Link>");
 			}
 			
			//data.append("\n");
 			data.append("</newEntry>");
 			
 			photoMetaList.add(data.toString());
 
 		}
 	}
 }
