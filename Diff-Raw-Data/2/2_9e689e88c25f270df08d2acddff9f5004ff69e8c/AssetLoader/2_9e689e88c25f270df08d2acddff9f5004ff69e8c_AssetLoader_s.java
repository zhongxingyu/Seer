 package net.rptools.maptool.model;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.zip.GZIPInputStream;
 
 import net.rptools.lib.FileUtil;
 import net.rptools.lib.MD5Key;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 
 public class AssetLoader {
 	
 	public enum RepoState {
 		ACTIVE,
 		BAD_URL,
 		INDX_BAD_FORMAT,
 		UNAVAILABLE
 	}
 
 	private static final File REPO_CACHE_DIR = AppUtil.getAppHome("repoindx");
 	
 	private ExecutorService retrievalThreadPool = Executors.newFixedThreadPool(3);
 	private Set<MD5Key> requestedIdSet = new HashSet<MD5Key>();
 	private Map<String, Map<String, String>> repositoryMap = new HashMap<String, Map<String, String>>();
 	private Map<String, RepoState> repositoryStateMap = new HashMap<String, RepoState>();
 	
 	public synchronized void addRepository(String repository) {
 		
 		// Assume active, unless we find otherwise during setup
 		repositoryStateMap.put(repository, RepoState.ACTIVE);
 		
 		repositoryMap.put(repository, getIndexMap(repository));
 	}
 
 	protected Map<String, String> getIndexMap(String repository) {
 		Map<String, String> indexMap = new HashMap<String, String>();
 		try {
 			byte[] index = null;
 			if (!hasCurrentIndexFile(repository)) {
 				URL url = new URL(repository);
 				index = FileUtil.getBytes(url);
 				storeIndexFile(repository, index);
 			} else {
 				index = FileUtil.loadFile(getRepoIndexFile(repository));
 			}
 			
 			indexMap = parseIndex(decode(index));
 		} catch (MalformedURLException e) {
 			repositoryStateMap.put(repository, RepoState.BAD_URL);
 		} catch (IOException e) {
 			repositoryStateMap.put(repository, RepoState.UNAVAILABLE);
 		}
 
 		return indexMap;
 	}
 	
 	protected List<String> decode(byte[] indexData) throws IOException {
 		
 		BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(indexData))));
 
 		List<String> list = new ArrayList<String>();
 		
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			list.add(line);
 		}
 		
 		return list;
 	}
 
 	protected Map<String, String> parseIndex(List<String> index) {
 		
 		Map<String, String> idxMap = new HashMap<String, String>();
 		
 		for (String line : index) {
 			
 			String id = line.substring(0, 32);
 			String ref = line.substring(33).trim();
 			
 			idxMap.put(id, ref);
 		}
 		
 		return idxMap;
 	}
 	
 	protected void storeIndexFile(String repository, byte[] data) throws IOException {
 		
 		File file = getRepoIndexFile(repository);
 		FileUtil.writeBytes(file, data);
 	}
 	
 	protected boolean hasCurrentIndexFile(String repository) {
 		// TODO: make this check timestamps, or update once a day or something like that
 		return getRepoIndexFile(repository).exists();
 	}
 	
 	protected File getRepoIndexFile(String repository) {
 		return new File(REPO_CACHE_DIR.getAbsolutePath() + "/" +  new MD5Key(repository.getBytes()));
 	}
 	
 	public synchronized void requestAsset(MD5Key id) {
 		retrievalThreadPool.submit(new ImageRetrievalRequest(id, createRequestQueue(id)));
 		requestedIdSet.add(id);
 	}
 	
 	public synchronized void completeRequest(MD5Key id) {
 		requestedIdSet.remove(id);
 	}
 	
 	protected List<String> createRequestQueue(MD5Key id) {
 		
 		List<String> requestList = new LinkedList<String>();
 		for (java.util.Map.Entry<String, Map<String, String>> entry : repositoryMap.entrySet()) {
 			
 			String repo = entry.getKey();
 			if (repositoryStateMap.get(repo) == RepoState.ACTIVE && entry.getValue().containsKey(id.toString())) {
 				
 				requestList.add(repo);
 			}
 		}
 		
 		return requestList;
 	}
 
 	private class ImageRetrievalRequest implements Runnable {
 		MD5Key id;
 		List<String> repositoryQueue;
 
 		public ImageRetrievalRequest(MD5Key id, List<String> repositoryQueue) {
 			this.id = id;
 			this.repositoryQueue = repositoryQueue;
 		}
 		
 		public void run() {
 			
 			while (repositoryQueue.size() > 0) {
 				
 				String repo = repositoryQueue.remove(0);
 				Map<String, String> repoMap = repositoryMap.get(repo);
 				if (repoMap == null) {
 					// Must have been removed while we were asleep
 					continue;
 				}
 				
 				String ref = repoMap.get(id.toString());
 				if (ref == null) {
 					// Must have updated while we were asleep
 					continue;
 				}
 				
 				// Create the reference, need to work relative to the repo indx file
 				int split = repo.lastIndexOf('/');
 				
 				// Get the content
 				try {
 					String path = repo.substring(0, split+1) + ref;
 					path = path.replaceAll(" ", "%20");
 					byte[] data = FileUtil.getBytes(new URL(path));
 					
 					// Verify the content
 					MD5Key sum = new MD5Key(data);
 					if (!sum.equals(id)) {
 						// Bad file
 						System.err.println("Downloaded invalid file from: " + repo);
 						
 						// Try a different repo
 						continue;
 					}
 					
 					// Done
 					split = ref.lastIndexOf('/');
 					if (split >= 0) {
 						ref = ref.substring(split+1);
 					}
					FileUtil.getNameWithoutExtension(ref);
 					AssetManager.putAsset(new Asset(ref, data));
 
 					completeRequest(id);
 				} catch (IOException ioe) {
 					// Well, try a different repo
 					ioe.printStackTrace();
 					continue;
 				} catch (Throwable t) {
 					t.printStackTrace();
 				}
 			}
 
 			// Last resort, ask the MT server
 			// We can drop off the end of this runnable because it'll background load the 
 			// image from the server
 	        MapTool.serverCommand().getAsset(id);
 		}
 	}
 	
 	public static void main(String[] args) throws Exception {
 		
 		AssetLoader al = new AssetLoader();
 		
 		al.addRepository("http://rptools.net/image-indexes/gallery.rpax.gz");
 		al.requestAsset(new MD5Key("45b26ca00bccee6156c436bbd4c865df"));
 		
 	}
 }
