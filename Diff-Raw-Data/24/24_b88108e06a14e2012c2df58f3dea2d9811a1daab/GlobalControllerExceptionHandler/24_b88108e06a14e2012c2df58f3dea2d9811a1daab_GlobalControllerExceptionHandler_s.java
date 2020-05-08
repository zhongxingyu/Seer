 package net.luversof.web.config.core;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import lombok.extern.slf4j.Slf4j;
 
 import org.springframework.web.bind.annotation.ControllerAdvice;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.servlet.ModelAndView;
 
 @Slf4j
 @ControllerAdvice
 public class GlobalControllerExceptionHandler {
 	
 	@ExceptionHandler
 	public ModelAndView handleException(Exception exception) {
 		Map<String, Object> map = new HashMap<>();
 		map.put("exception", exception);
		log.debug("@ExceptionHandler called");
 		return new ModelAndView("/error", map);
 	}
 }
