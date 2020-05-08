 package com.heartyoh.service;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Entity;
 import com.heartyoh.util.SessionUtils;
 
 @Controller
 public class IncidentVideoService extends EntityService {
 	private static final Logger logger = LoggerFactory.getLogger(IncidentVideoService.class);
 
 	@Override
 	protected String getEntityName() {
 		return "Incident";
 	}
 
 	@Override
 	protected String getIdValue(Map<String, Object> map) {
 //		return (String)map.get("id");
 		return map.get("terminal_id") + "@" + map.get("datetime");
 	}
 
 	@Override
 	protected void onCreate(Entity entity, Map<String, Object> map, DatastoreService datastore) {
 		entity.setProperty("terminal_id", map.get("terminal_id"));
//		entity.setProperty("datetime", map.get("datetime"));
 		entity.setProperty("datetime", SessionUtils.stringToDateTime((String)map.get("datetime")));
 
 		super.onCreate(entity, map, datastore);
 	}
 
 	@Override
 	protected void preMultipart(Map<String, Object> map, MultipartHttpServletRequest request) throws IOException {
 		super.preMultipart(map, request);
 
 //		MultipartFile video_clip = (MultipartFile)map.get("video_clip");
 		
 //		String filename = video_clip.getOriginalFilename();
 //		String[] params = filename.split("@");
 //		if(params.length < 2)
 //			throw new IOException("Invalid Filename(" + filename + ")");
 //		
 //		String terminal_id = params[0];
 //		Date datetime = SessionUtils.stringToDateTime(params[1], "yyyyMMddHHmmss", null);
 //
 //		map.put("id", terminal_id + "@" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(datetime));
 //		
 //		map.put("terminal_id", terminal_id);
 //		map.put("datetime", datetime);
 	}
 
 	@Override
 	protected void postMultipart(Entity entity, Map<String, Object> map, MultipartHttpServletRequest request)
 			throws IOException {
 		entity.setProperty("video_clip", saveFile((MultipartFile)map.get("video_clip")));
 
 		super.postMultipart(entity, map, request);
 	}
 
 	@Override
 	protected void onSave(Entity entity, Map<String, Object> map, DatastoreService datastore) {
 		super.onSave(entity, map, datastore);
 	}
 
 	@RequestMapping(value = "/incident/upload_video", method = RequestMethod.POST)
 	public @ResponseBody
 	String imports(MultipartHttpServletRequest request, HttpServletResponse response) throws IOException {
 		return super.save(request, response);
 	}
 
 }
