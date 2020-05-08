 package com.example.rest.controller;
 
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.ControllerAdvice;
 import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.client.HttpStatusCodeException;
 
 import com.example.rest.exception.IllegalResourceContentException;
 import com.example.rest.exception.ResourceNotFoundException;
import com.example.rest.model.Error;
 
 @ControllerAdvice
 public class ControllerExceptionHandler {
 
	@ExceptionHandler(IllegalResourceContentException.class)
	@ResponseBody
	public ResponseEntity<Error> handleBadRequestException(IllegalResourceContentException ex) {
		return Error.buildResponse(HttpStatus.BAD_REQUEST, ex);
 	}
 	
	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseBody
	public ResponseEntity<Error> handleBadRequestException(ResourceNotFoundException ex) {
		return Error.buildResponse(HttpStatus.NOT_FOUND, ex);
 	}
 	
 	@ExceptionHandler(HttpStatusCodeException.class)
	protected ResponseEntity<Error> entityNotFound(HttpStatusCodeException exception){
		return Error.buildResponse(exception.getStatusCode(), exception);
 	}
 }
