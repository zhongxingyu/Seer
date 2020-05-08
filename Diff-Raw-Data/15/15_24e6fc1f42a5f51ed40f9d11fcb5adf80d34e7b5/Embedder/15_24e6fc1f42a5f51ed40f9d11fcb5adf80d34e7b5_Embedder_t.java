 package de.cosmocode.palava.components.mail;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.velocity.app.VelocityEngine;
 
 public class Embedder {
 	
 	private static final Logger log = Logger.getLogger(Embedder.class);
 
 	private final Map <String, String> embeddings = new HashMap<String, String>(); // mapping  path => cid
 	private final List<String> filenames = new ArrayList<String>();
 	private int cidCount = 0;
 	
 	private final File resourcePath;
 	
 	public Embedder(File resourcePath) {
 		this.resourcePath = resourcePath;
 	}
 	
 	public Embedder(VelocityEngine engine) {
 		this.resourcePath = new File(engine.getProperty("file.resource.loader.path").toString());
 	}
 	
 	public String image (String path)  {
 		String cid = embeddings.get(path);
 
 		if (cid == null) {
 			setEmbedding(path, cid = generateCID());
 			log.debug("creating inline image; path=" + path + " cid=" + cid);
 		} else {
 			log.debug("reusing inline image; path=" +  path + " cid=" + cid);
 
 		}
 		return cid;
 	}
 	
 	public String name(String path) {
 		String fileName = null;
 		String cid = null;
 		String[] parts = null;
 		cid = embeddings.get(path);
 		
 		if (cid == null) {
 			log.debug("Image " + path + " not embedded.");
 			return path;
 		}
 		
 		parts = path.split("/"); // to get the
 		fileName = parts[parts.length - 1]; // filename
 		return fileName;
 		
 	}
 
 	private String generateCID() {
 		cidCount++;
 		String cid = System.currentTimeMillis()+ "" + cidCount;
 		return cid;
 	}
 	
 	public void setEmbedding(String path, String cid){
 		String[] parts = path.split("/"); // to get the
 		String fileName = parts[parts.length - 1]; // filename
 		
 		if (embeddings.get(path) == null && filenames.contains(fileName)) {
 			throw new IllegalArgumentException(fileName + " was embedded twice from different paths");
 		}
 		
 		filenames.add(fileName);
 		embeddings.put(path, cid);
 	}
 	
 	public Map<String, String> getEmbeddings() {
 		return embeddings;
 	}
 	
 	public boolean hasEmbeddings() {
 		return !embeddings.isEmpty();
 	}
 	
 	public File getResourcePath() {
 		return resourcePath;
 	}
 	
 }
