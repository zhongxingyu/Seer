 package com.heartyoh.service;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
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
 public class IncidentService extends EntityService {
 	private static final Logger logger = LoggerFactory.getLogger(IncidentService.class);
 
 	@Override
 	protected String getEntityName() {
 		return "Incident";
 	}
 
 	@Override
 	protected boolean useFilter() {
 		return true;
 	}
 
 	@Override
 	protected String getIdValue(Map<String, Object> map) {
 		return map.get("terminal_id") + "@" + map.get("datetime");
 	}
 
 	@Override
 	protected void onCreate(Entity entity, Map<String, Object> map, DatastoreService datastore) {
 		entity.setProperty("terminal_id", map.get("terminal_id"));
		entity.setProperty("datetime", SessionUtils.stringToDateTime((String)map.get("datetime")));
 
 		super.onCreate(entity, map, datastore);
 	}
 
 	@Override
 	protected void postMultipart(Entity entity, Map<String, Object> map, MultipartHttpServletRequest request) throws IOException {
 		entity.setProperty("video_clip", saveFile((MultipartFile) map.get("video_file")));
 
 		super.postMultipart(entity, map, request);
 	}
 
 	@Override
 	protected void onSave(Entity entity, Map<String, Object> map, DatastoreService datastore) {
 
 		if (map.get("vehicle_id") != null)
 			entity.setProperty("vehicle_id", stringProperty(map, "vehicle_id"));
 		if (map.get("driver_id") != null)
 			entity.setProperty("driver_id", stringProperty(map, "driver_id"));
 		if (map.get("lattitude") != null)
 			entity.setProperty("lattitude", doubleProperty(map, "lattitude"));
 		if (map.get("longitude") != null)
 			entity.setProperty("longitude", doubleProperty(map, "longitude"));
 		if (map.get("velocity") != null)
 			entity.setProperty("velocity", doubleProperty(map, "velocity"));
 		if (map.get("impulse_abs") != null)
 			entity.setProperty("impulse_abs", doubleProperty(map, "impulse_abs"));
 		if (map.get("impulse_x") != null)
 			entity.setProperty("impulse_x", doubleProperty(map, "impulse_x"));
 		if (map.get("impulse_y") != null)
 			entity.setProperty("impulse_y", doubleProperty(map, "impulse_y"));
 		if (map.get("impulse_z") != null)
 			entity.setProperty("impulse_z", doubleProperty(map, "impulse_z"));
 		if (map.get("impulse_threshold") != null)
 			entity.setProperty("impulse_threshold", doubleProperty(map, "impulse_threshold"));
 		if (map.get("engine_temp") != null)
 			entity.setProperty("engine_temp", doubleProperty(map, "engine_temp"));
 		if (map.get("engine_temp_threshold") != null)
 			entity.setProperty("engine_temp_threshold", doubleProperty(map, "engine_temp_threshold"));
 		if (map.get("obd_connected") != null)
 			entity.setProperty("obd_connected", booleanProperty(map, "obd_connected"));
 		if (map.get("confirm") != null)
 			entity.setProperty("confirm", booleanProperty(map, "confirm"));
 
 		super.onSave(entity, map, datastore);
 	}
 
 	@RequestMapping(value = "/incident/import", method = RequestMethod.POST)
 	public @ResponseBody
 	String imports(MultipartHttpServletRequest request, HttpServletResponse response) throws IOException {
 		return super.imports(request, response);
 	}
 
 	@RequestMapping(value = "/incident/save", method = RequestMethod.POST)
 	public @ResponseBody
 	String save(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		return super.save(request, response);
 	}
 
 	@RequestMapping(value = "/incident/delete", method = RequestMethod.POST)
 	public @ResponseBody
 	String delete(HttpServletRequest request, HttpServletResponse response) {
 		return super.delete(request, response);
 	}
 
 	@RequestMapping(value = "/incident", method = RequestMethod.GET)
 	public @ResponseBody
 	List<Map<String, Object>> retrieve(HttpServletRequest request, HttpServletResponse response) {
 		return super.retrieve(request, response);
 	}
 
 }
