 package com.playlife.presentation.controllers;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.fasterxml.jackson.core.JsonGenerationException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.playlife.presentation.converters.JSONConverter;
 import com.playlife.settings.ErrorSetting;
 import com.playlife.utility.LocaleService;
 import com.playlife.utility.Request;
 import com.playlife.utility.exceptions.LogicException;
 import com.playlife.utility.exceptions.PersistenceException;
 import com.playlife.utility.exceptions.PresentationException;
 import com.playlife.utility.exceptions.ValidationException;
 
 @Controller
 @RequestMapping (value = "/error")
 public class ErrorController {
 	@Autowired
 	MessageSource messageSource;
 
 	ObjectMapper mapper = JSONConverter.mapper;
 
 	private static final Logger log = Logger.getLogger("Error");
 
 	@RequestMapping (value = "/*")
 	protected String registerRequest(String s_displayMessage, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
 		LocaleService.resolve(request, response);
 		model.put("persistence", Request.getArrayObject(request, "s_persistence", PersistenceException.class));
 		model.put("logic", Request.getArrayObject(request, "s_logic", LogicException.class));
 		model.put("presentation", Request.getArrayObject(request, "s_presentation", PresentationException.class));
 		model.put("validation", Request.getArrayObject(request, "s_validation", ValidationException.class));
 		model.put("exception", Request.getArrayObject(request, "s_exception", Exception.class));
 		model.put("displayMessage", s_displayMessage);
 
 		Map<String, Object> map_return = new HashMap<String, Object>();
 		map_return.put("persistence", Request.getArrayObject(request, "s_persistence", PersistenceException.class));
 		map_return.put("logic", Request.getArrayObject(request, "s_logic", LogicException.class));
 		map_return.put("presentation", Request.getArrayObject(request, "s_presentation", PresentationException.class));
 		map_return.put("validation", Request.getArrayObject(request, "s_validation", ValidationException.class));
 		map_return.put("exception", Request.getArrayObject(request, "s_exception", Exception.class));
 		map_return.put("displayMessage", s_displayMessage.replace("\"", "==/=="));
 
 		model.put("s_log", mapper.writeValueAsString(map_return));
 		return "error/show";
 	}
 
 	@RequestMapping (value = "/log.json")
 	@ResponseBody
 	protected String logRequest(String s_log, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException {
 		LocaleService.resolve(request, response);
 
 		Writer errorWriter = null;
 		try {
 			@SuppressWarnings ("unchecked")
 			Map<String, Object> m_error = mapper.readValue(s_log, Map.class);
			m_error.put("ip", request.getRemoteAddr());
 			m_error.put("time", ErrorSetting.ERROR_LOG_TIME_FORMATTER.format(Calendar.getInstance().getTime()));
 
 			String s_logFileName = m_error.get("time") + "_" + m_error.get("ip");
 			@SuppressWarnings ("deprecation")
 			String s_logFilePath = request.getRealPath("") + ErrorSetting.ERROR_LOG_PATH + s_logFileName + ErrorSetting.ERROR_LOG_EXTENSION;
 			errorWriter = new BufferedWriter(new FileWriter(new File(s_logFilePath)));
 			errorWriter.write(mapper.writeValueAsString(m_error));
		}
		catch (Exception ex) {
 			throw new PresentationException(-9999, ex);
		}
		finally {
 			try {
 				if (errorWriter != null) {
 					errorWriter.close();
 				}
 			}
 			catch (Exception ex) {
 				throw new PresentationException(-9999, ex);
 			}
 		}
 
 		log.info(s_log);
 		Map<String, Object> map_return = new HashMap<String, Object>();
 		map_return.put("status", "ok");
 		return mapper.writeValueAsString(map_return);
 	}
 
 	@ExceptionHandler (Exception.class)
 	@ResponseBody
 	public String handlerException(HttpServletRequest request, Exception ex) throws JsonGenerationException, JsonMappingException, IOException {
 		Map<String, Object> map_return = new HashMap<String, Object>();
 		map_return.put("error", JSONConverter.constructError(ex, messageSource, LocaleService.getLocale(request)));
 		map_return.put("status", "error");
 		return mapper.writeValueAsString(map_return);
 	}
 }
