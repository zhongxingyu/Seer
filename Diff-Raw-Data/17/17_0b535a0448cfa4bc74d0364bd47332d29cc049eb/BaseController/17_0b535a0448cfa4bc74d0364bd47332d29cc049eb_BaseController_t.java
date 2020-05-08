 package com.onb.otp.controller;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.http.HttpStatus;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.onb.otp.domain.ErrorMessage;
 import com.onb.otp.exception.InvalidRequestParameterException;
 import com.onb.otp.exception.OneTimePasswordListNotFreeException;
 import com.onb.otp.exception.UserDoesNotExistException;
 
 public abstract class BaseController {
 	@ExceptionHandler(UserDoesNotExistException.class)
 	public @ResponseBody ErrorMessage handle204Success(Throwable ex, HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.NO_CONTENT.value());
 		return new ErrorMessage("204", ex.getMessage());
 	}
 	
 	@ExceptionHandler(OneTimePasswordListNotFreeException.class)
 	public @ResponseBody ErrorMessage handle403Exception(Throwable ex, HttpServletResponse response) throws IOException {
 		response.setStatus(HttpStatus.FORBIDDEN.value());
 		return new ErrorMessage("403", ex.getMessage());
 	}
 	
 	@ExceptionHandler(InvalidRequestParameterException.class)
 	public @ResponseBody ErrorMessage handle404Exception(Throwable ex, HttpServletResponse response) throws IOException {
 		response.setStatus(HttpStatus.NOT_FOUND.value());
 		return new ErrorMessage("404", ex.getMessage());
 	}
 
 }
