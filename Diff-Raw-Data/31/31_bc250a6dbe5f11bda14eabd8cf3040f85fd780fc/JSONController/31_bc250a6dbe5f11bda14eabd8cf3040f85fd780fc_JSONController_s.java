 package edu.helsinki.sulka.controllers;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import edu.helsinki.sulka.services.APIQueryException;
 
 /**
  * Base class for Controllers that only return JSON.
  */
 @Controller
 public abstract class JSONController {
 	@Autowired
 	private Logger logger;
 	
 	/**
 	 * The error response JSON
 	 */
 	protected static class ErrorResponse {
 	    public ErrorResponse(String msg) { error = msg; }
 		public String error;
 	    public boolean success = false;
 	}
 
 	/**
 	 * Multiple objects response JSON using array
 	 */
 	protected static class ArrayResponse<T> {
 		public ArrayResponse(T[] objs) { this.objects = objs; }
 		public T[] objects = null;
 		public String error = null;
 		public boolean success = true;
 	}
 	
 	/**
 	 * Multiple objects response JSON using java.util.List
 	 */
 	protected static class ListResponse<T> {
 		public ListResponse(List<T> objs) { this.objects = objs; }
 		public List<T> objects = null;
 		public String error = null;
 		public boolean success = true;
 	}
 	
 	/**
 	 * Single object response JSON
 	 */
 	protected static class ObjectResponse<T> {
 		public ObjectResponse(T obj) { this.object = obj; }
 		public T object = null;
 		public String error = null;
 		public boolean success = true;
 	}
 	
 	/**
 	 * Handles API exceptions gracefully.
 	 */
 	@ExceptionHandler(APIQueryException.class)
 	@ResponseBody
 	@ResponseStatus(HttpStatus.BAD_REQUEST)
 	public ErrorResponse handleAPIException(Exception e) {
 		return new ErrorResponse(e.getMessage());
 	}
	
	/**
	 * Other exceptions are printed and HTTP 500 is returned.
	 */
	@ExceptionHandler(Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleException(Exception e) {
		e.printStackTrace();
		return new ErrorResponse(e.getMessage());
	}
 }
