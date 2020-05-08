 package fr.esiea.esieaddress.controllers.exception;
 
 import fr.esiea.esieaddress.model.exception.RestException;
 import fr.esiea.esieaddress.service.exception.security.NotConnectedException;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.ControllerAdvice;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
 
 import java.util.logging.Logger;
 
 /**
  * Copyright (c) 2013 ESIEA M. Labusquiere D. Déïs
  * <p/>
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * <p/>
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * <p/>
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 @ControllerAdvice
 public class ExceptionHandlerCtrl extends ResponseEntityExceptionHandler {
 
 	private final static Logger LOGGER = Logger.getLogger("ExceptionHandlerCtrl");
 
 	@ExceptionHandler(value = {RestException.class})
 	protected ResponseEntity<Object> handleException(RestException ex, WebRequest request) {
 		LOGGER.info("An exception was catch by spring mvc :" + ex.getClass() + " : " + ex.getMessage());
 		Object model = ex.getModel();
        //lazy code to enforce json
        if(model instanceof String)
            return new ResponseEntity<Object>("{error:\"+model+\"}", HttpStatus.valueOf(ex.getStatus()));
 		return new ResponseEntity<Object>(model, HttpStatus.valueOf(ex.getStatus()));
 	}
 
     @ExceptionHandler(value = {NotConnectedException.class})
     protected ResponseEntity<Object> handleException(NotConnectedException ex, WebRequest request) {
         // avoid a useless log each time that the user ping (open the web-app)
         Object model = ex.getModel();
         return new ResponseEntity<Object>(model, HttpStatus.valueOf(ex.getStatus()));
     }
 }
