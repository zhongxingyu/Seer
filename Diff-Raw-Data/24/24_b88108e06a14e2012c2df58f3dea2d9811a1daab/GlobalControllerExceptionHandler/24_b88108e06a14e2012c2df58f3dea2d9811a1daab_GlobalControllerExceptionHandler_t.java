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
		// MappingJacksonJsonView의 경우  (entry.getValue() instanceof BindingResult) 조건 체크를 하여 exception은 리턴하지 않으므로 별도의 결과값을 주어야 함
		// Exception Message 규칙을 정할 필요가 있음
		map.put("message", exception.getLocalizedMessage());
		log.debug("GlobalControllerExceptionHandler called {}", exception);
 		return new ModelAndView("/error", map);
 	}
 }
