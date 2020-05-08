 package org.stwerff.mslraws;
 
 import java.io.IOException;
 //import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.chap.memo.memoNodes.MemoNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 //import com.google.appengine.api.blobstore.BlobKey;
 //import com.google.appengine.api.blobstore.BlobstoreService;
 //import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 //import com.google.appengine.api.files.AppEngineFile;
 //import com.google.appengine.api.files.FileService;
 //import com.google.appengine.api.files.FileServiceFactory;
 //import com.google.appengine.api.files.FileWriteChannel;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 
 public class LandingPageServlet extends HttpServlet {
 	private static final long serialVersionUID = 8110001398162695563L;
 	static final ObjectMapper om = new ObjectMapper();
 	static MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();;
 
 	
 	public String generateJSON(int sol, String camera, boolean countsOnly, boolean flat, boolean repair){
 		MemoNode baseNode = MemoNode.getRootNode().getChildByStringValue("msl-raw-images");
 		
 		ArrayList<MemoNode> imgList = null;
 		MemoNode allImagesNode = baseNode.getChildByStringValue("allImages");
 		if (repair){
 			if (allImagesNode == null){
 				allImagesNode = baseNode.addChild(new MemoNode("allImages"));
 			}
 			imgList = allImagesNode.getChildren();
 		}
 
 		
 		ArrayList<MemoNode> cameras = baseNode.getChildren();
 		Iterator<MemoNode> iter = cameras.iterator();
 		ArrayNode result = om.createArrayNode();
 		while (iter.hasNext()){
 			MemoNode cam = iter.next();
 			if (cam.getStringValue().equals("sols")) continue;
 			if (!camera.equals("all") && !cam.getStringValue().equals(camera)) continue;
 			
 			ArrayList<MemoNode> sols;
 			if (sol > 0){
 				sols = cam.getChildrenByStringValue("sol"+sol,1);
 			} else {
 				sols = cam.getChildren();
 			}
 			if (sols.size()>0){
 				ObjectNode cameraNode = om.createObjectNode();
 				ObjectNode solNodes = om.createObjectNode();
 				if (!flat){
 					result.add(cameraNode);					
 					cameraNode.put("camera", cam.getStringValue());
 					cameraNode.put("sols", solNodes);
 				}
 				Iterator<MemoNode> soliter = sols.iterator();
 				while (soliter.hasNext()){
 					MemoNode node_sol = soliter.next();	
 					if (node_sol.getChildByStringValue("images") == null){
 						node_sol.addChild(new MemoNode("images"));
 					}
 					int nofc = 0;
 					if (node_sol.getChildByStringValue("images").getChildren() != null){
 						nofc = node_sol.getChildByStringValue("images").getChildren().size();
 					}
 					if (nofc > 0){
 						ObjectNode solNode = om.createObjectNode();
 						if (!flat){
 							solNodes.put(node_sol.getStringValue().replace("sol", ""),solNode);
 							solNode.put("count", nofc);
 						}
 						if (!countsOnly){
 							ArrayList<MemoNode> images = node_sol.getChildByStringValue("images").getChildren();
 							Iterator<MemoNode> image_iter = images.iterator();
 							ArrayNode imagesNode = om.createArrayNode();
 							if (!flat){
 								solNode.put("images", imagesNode);
 							}
 							while(image_iter.hasNext()){
 								MemoNode image = image_iter.next();
 								String url = image.getStringValue();
 								if (url.equals(""))continue;
 								if (repair){
 									if (imgList != null && !imgList.contains(image)){
 										allImagesNode.addChild(image);
 									}
 								}
 								ObjectNode imageNode = om.createObjectNode();
 								imageNode.put("name", url.substring(url.lastIndexOf('/')+1));
 								imageNode.put("url", url);
 								imageNode.put("thumbnailUrl", image.getPropertyValue("thumbnail"));
 								imageNode.put("type", image.getPropertyValue("type"));
 								imageNode.put("unixTimeStamp", image.getPropertyValue("timestamp"));
 								imageNode.put("date", image.getPropertyValue("stringDate"));
 								imageNode.put("fileTimeStamp", image.getPropertyValue("fileTimeStamp"));
 								imageNode.put("lastModified", image.getPropertyValue("lastModified"));
 								if (flat){
 									imageNode.put("camera", cam.getStringValue());
 									imageNode.put("sol", node_sol.getStringValue().replace("sol", ""));
 									result.add(imageNode);
 								} else {
 									imagesNode.add(imageNode);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		if (repair){
 			//remove duplicates
 			ArrayList<MemoNode> allImages = allImagesNode.getChildren();
 			HashSet<String> set = new HashSet<String>(allImages.size());
 			for (MemoNode image : allImages){
 				if (set.contains(image.getStringValue())){
 					image.delete();
 				} else {
 					set.add(image.getStringValue());
 				}
 			}
 		}
 		return result.toString();
 	}
 	
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		int sol=-1;
 		String ssol = req.getParameter("sol");
 		if (ssol != null) {
 			try {
 			   sol = Integer.parseInt(ssol);
 			} catch (Exception e){
 				System.out.println("Non numeric sol input!");
 			}
 		}
 		String camera = req.getParameter("cam");
 		if (camera == null){
 			camera="all";
 		}
 		boolean countsOnly = (req.getParameter("counts") != null);
 		boolean flat = (req.getParameter("flat") != null);
 		boolean repair = (req.getParameter("repair")!=null);
 		
 		resp.setContentType("application/json");
 		String result = generateJSON(sol,camera,countsOnly,flat,repair);
 		resp.getWriter().write(result);
 		if (repair) MemoNode.flushDB();
 	}
 }
