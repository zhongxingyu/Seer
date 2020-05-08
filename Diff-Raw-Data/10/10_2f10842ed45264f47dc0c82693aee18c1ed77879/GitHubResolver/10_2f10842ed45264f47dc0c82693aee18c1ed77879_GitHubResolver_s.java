 package org.qrone.r7.resolver;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.ho.yaml.Yaml;
 import org.qrone.r7.fetcher.URLFetcher;
 import org.qrone.r7.handler.URIHandler;
 import org.qrone.util.QrONEUtils;
 
 public class GitHubResolver implements URIResolver, URIHandler {
 	private URLFetcher fetcher;
 	private URIResolver cacheresolver;
 	private String user;
 	private String repo;
 	private String treesha;
 	
 	private Map<String,String> blobs;
 	private Set<String> updatedSet = new HashSet<String>();
 	
 	public GitHubResolver(URLFetcher fetcher, URIResolver cacheresolver, String user, String repo, String treesha){
 		this.fetcher = fetcher;
 		this.cacheresolver = cacheresolver;
 		this.user = QrONEUtils.escape(user);
 		this.treesha = QrONEUtils.escape(treesha);
 	}
 	
 	public void clear(){
 		blobs = null;
 	}
 	
 	private Map<String,String> getFiles(){
 		if(blobs == null){
 			Map map = (Map)Yaml.load(fetcher.fetch("http://github.com/api/v2/yaml/blob/all/" 
 					+ user + "/" + repo + "/" + treesha));
 			blobs = (Map<String,String>)map.get("blobs");
 		}
 		return blobs;
 	}
 	
 	@Override
 	public boolean exist(String path) {
 		getFiles();
 		return blobs.containsKey(path);
 	}
 
 	@Override
 	public boolean updated(URI uri) {
 		return updatedSet.contains(uri.toString());
 	}
 
 	@Override
 	public InputStream getInputStream(URI uri) throws IOException {
 		updatedSet.remove(uri.toString());
 		String sha = getFiles().get(uri.toString());
 		return fetcher.fetch("http://github.com/api/v2/yaml/blob/show/" 
 				+ user + "/" + repo + "/" + blobs.get(sha));
 	}
 
 	@Override
 	public OutputStream getOutputStream(URI uri) throws IOException {
		return null;
 	}
 
 	@Override
 	public boolean handle(HttpServletRequest request,
 			HttpServletResponse response, String path, String pathArg) {
 		if(path.equals("/qrone-server/github-post-receive")){
 			try {
 				
 				Map<String,String> oldblobs = blobs;
 				blobs = null;
 				getFiles();
 				
 				for (Entry<String,String> e : oldblobs.entrySet()) {
 					String sha = blobs.get(e.getKey());
 					if(sha == null || !sha.equals(e.getValue())){
 						try {
 							cacheresolver.remove(new URI(e.getKey()));
 						} catch (URISyntaxException e1) {}
 					}
 				}
 				
 				/*
 				
 				Map body = (Map)JSON.decode(request.getInputStream());
 				List<Map> commits = (List)body.get("commits");
 				
 				for (Map map : commits) {
 					String id = (String)map.get("id");
 					InputStream in = fetcher.fetch("http://github.com/api/v2/json/commits/show/" + user + "/" + repo + "/" + id);
 					Map commit = (Map)((Map)JSON.decode(in)).get("commit");
 					
 					List<String> added = (List)commit.get("added");
 					for (String file : added) {
 						clearCache(file);
 					}
 					
 					List<String> removed = (List)commit.get("removed");
 					for (String file : removed) {
 						clearCache(file);
 					}
 					
 					List<Map> modified = (List)commit.get("modified");
 					for (Map m : modified) {
 						String file = (String)m.get("filename");
 						clearCache(file);
 					}
 				}
 				*/
 				
 				return true;
 			} catch (ClassCastException e) {
 			}
 		}
 		return false;
 	}
 	
 	/*
 	private void clearCache(String path){
 		updatedSet.add(path);
 		blobs.remove(path);
 		try {
 			cacheresolver.remove(new URI(path));
 		} catch (URISyntaxException e) {
 		}
 	}
 	*/
 	
 	@Override
 	public boolean remove(URI uri) {
 		return false;
 	}
 }
