 /**
  * 
  */
 package org.qburst.search.controller;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.qburst.openidlogin.LoginFilter;
 import org.qburst.search.indexer.IndexEngine;
 import org.qburst.search.model.Authentication;
 import org.qburst.search.model.Search;
 import org.qburst.search.model.UploadStatus;
 import org.qburst.search.services.ISolrUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 /**
  * @author Cyril
  * 
  */
 @Controller
 public class SearchController {
 	private ISolrUtils solrService;
 	private IndexEngine iEngine;
 	@Autowired
 	public SearchController(@Qualifier(value = "SolrUtilsService") ISolrUtils su,
 			@Qualifier(value = "pdfIndexerBean") IndexEngine ie) {
 		solrService = su;
 		iEngine = ie;
 	}
 
 	@RequestMapping(value = "/search", method = RequestMethod.GET)
 	public @ResponseBody
 	String doSearch(@RequestParam(required = true, value = "query") String q) {
 		String jsonData = "";
 		try {
 			QueryResponse response = solrService.queryBooks(q);
 			SolrDocumentList results = response.getResults();
 			Map<String, Map<String, List<String>>> highlights = response
 					.getHighlighting();
 			ArrayList<Search> mySearch = new ArrayList<Search>();
 			for (String key : highlights.keySet()) {
 				List<String> data = highlights.get(key).get("content");
 				if (data != null) {
 					Search s = new Search();
 					s.setHighlights(data);
 					s.setId(key);
 					SolrDocument sd = getResult(results, key);	
 					s.setAuthor(sd.containsKey("author") ? (sd.get("author").toString().trim().isEmpty() ? "Unknown": sd.get("author").toString().trim()): "Unknown");					
 					s.setUrl(sd.containsKey("url") ? sd.get("url").toString() : "");
 					s.setTitle(sd.containsKey("title") ? stringify(sd.get("title")) : "");
 					s.setUserDetails(solrService.getUserInfo(s.getUrl()));
 					mySearch.add(s);
 				}
 			}
 			ObjectMapper mapper = new ObjectMapper();
 			jsonData = mapper.writeValueAsString(mySearch);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		String a = "aaData";
 		return "{" + '"' + a + '"' + ":" + jsonData + "}";
 	}
	@RequestMapping(value = "/findFiles", method = RequestMethod.GET)
 	public @ResponseBody String doFindMyFiles(HttpServletRequest request,HttpServletResponse response) {
 		String jsonData = "";
 		try {
 			Authentication user = ((Authentication)request.getSession().getAttribute(LoginFilter.OPENID_INDENTITY));
 			SolrDocumentList books = solrService.queryUserBooks(user);
 			ArrayList<Search> mySearch = new ArrayList<Search>();
 			for(SolrDocument book :  books){
 				Search s = new Search();
 				s.setId(book.get("id").toString());
 				s.setAuthor(book.containsKey("author") ? (book.get("author").toString().trim().isEmpty() ? "Unknown": book.get("author").toString().trim()): "Unknown");					
 				s.setUrl(book.containsKey("url") ? book.get("url").toString() : "");
 				s.setTitle(book.containsKey("title") ? stringify(book.get("title")) : "");
 				s.setUserDetails(solrService.getUserInfo(s.getUrl()));
 				mySearch.add(s);
 			}
 			ObjectMapper mapper = new ObjectMapper();
 			jsonData = mapper.writeValueAsString(mySearch);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		String a = "aaData";
 		return "{" + '"' + a + '"' + ":" + jsonData + "}";
 	}
 	private SolrDocument getResult(SolrDocumentList sdl, String id){
 		for (SolrDocument sd : sdl){
 			if (sd.get("id").equals(id)){
 				return sd;
 			}
 		}
 		return null;
 	}
 	private String stringify(Object ary) {
 		String ret = "";
 		if (ary != null && ary instanceof ArrayList) {
 			ArrayList objects = (ArrayList) ary;
 			ret = StringUtils.arrayToDelimitedString(objects.toArray(), ", ");
 		}
 		return ret;
 	}
 	@RequestMapping(value = "/export", method = RequestMethod.GET)
 	public void doDownload(HttpServletRequest request,HttpServletResponse response,@RequestParam(required = true, value = "filePath") String filePath)
 			throws IOException {
 		int BUFFER_SIZE = 4096;
 		ServletContext context = request.getServletContext();
 		File downloadFile = new File(filePath);
 		if(downloadFile.exists()){
 			String appPath = context.getRealPath("");
 			String fullPath = appPath + filePath;
 			FileInputStream inputStream = new FileInputStream(downloadFile);
 			String mimeType = context.getMimeType(fullPath);
 			if (mimeType == null) {
 				mimeType = "application/pdf";
 			}
 			response.setContentType(mimeType);
 			response.setContentLength((int) downloadFile.length());
 			String headerKey = "Content-Disposition";
 			String headerValue = String.format("attachment; filename=\"%s\"",downloadFile.getName());
 			response.setHeader(headerKey, headerValue);
 			OutputStream outStream = response.getOutputStream();
 			byte[] buffer = new byte[BUFFER_SIZE];
 			int bytesRead = -1;
 			while ((bytesRead = inputStream.read(buffer)) != -1) {
 				outStream.write(buffer, 0, bytesRead);
 			}
 			inputStream.close();
 			outStream.close();
 		}
 	}
 	
 	@RequestMapping(value="/upload", method=RequestMethod.POST)
 	public @ResponseBody String upload(@RequestParam("files[]") ArrayList<MultipartFile> files, HttpServletRequest request) {
 		ArrayList<UploadStatus> usList = new ArrayList<UploadStatus>();
 		String jsonData = "";
 		Authentication user = ((Authentication)request.getSession().getAttribute(LoginFilter.OPENID_INDENTITY));
 		boolean bIndexing = false;
 		for (MultipartFile mf : files){
 			String fn = mf.getOriginalFilename();
 			fn = solrService.getBookFolder() + "/" + mf.getOriginalFilename();
 			File file = new File(fn);
 			UploadStatus us = new UploadStatus();
 			us.setFileName(mf.getOriginalFilename());
 			if (!file.exists()){
 				try{
 					FileOutputStream fos = new FileOutputStream(file);
 					fos.write(mf.getBytes());
 					fos.close();
 					us.setStatus("SUCCESS");
 					solrService.writeBookMeta(user, fn);
 					bIndexing = true;
 				} catch (Exception e){
 					e.printStackTrace();
 					us.setStatus("FAILURE");
 					us.setReason(e.getMessage());
 				}
 			} else {
 				us.setStatus("FAILURE");
 				us.setReason("File Already Present");
 			}
 			usList.add(us);
 		}
 		ObjectMapper mapper = new ObjectMapper();
 		try {
 			jsonData = mapper.writeValueAsString(usList);
 			if (bIndexing){
 				Runnable r = new Runnable() {
 					@Override
 					public void run() {
 						try{
 							iEngine.start();
 						} catch(Exception e){
 							e.printStackTrace();
 						}
 					}
 				};
 				new Thread(r).start();
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		return jsonData;
 	}
 }
