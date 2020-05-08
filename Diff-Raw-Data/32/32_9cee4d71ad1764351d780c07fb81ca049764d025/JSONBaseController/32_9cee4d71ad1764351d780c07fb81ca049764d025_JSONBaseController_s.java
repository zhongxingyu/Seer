 package wad.tukki.controllers;
 
 import org.springframework.http.HttpStatus;
 import org.springframework.http.converter.HttpMessageNotReadableException;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.MethodArgumentNotValidException;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import wad.tukki.models.JSONMessage;
 import wad.tukki.models.JSONMessageCode;
 import wad.tukki.models.JSONMessageMap;
 
 public abstract class JSONBaseController {
         
     @ExceptionHandler(MethodArgumentNotValidException.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     @ResponseBody
     public JSONMessageMap handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
         
         JSONMessageMap errors = new JSONMessageMap("errors");
         
         // Validation errors
         for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
             
             JSONMessage error = new JSONMessage(JSONMessageCode.VALIDATION_ERROR,
                                                 fieldError.getDefaultMessage());
             errors.put(fieldError.getField(), error);
         }
         
         return errors;
     }
 
     @ExceptionHandler(HttpMessageNotReadableException.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     @ResponseBody
     public JSONMessage handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return new JSONMessage(JSONMessageCode.PARSING_ERROR, "Could not read JSON.");
     }
     
     @ExceptionHandler(Exception.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     @ResponseBody
     public JSONMessage handleException(Exception exception) {
         
         System.out.println("handleException");
         exception.printStackTrace();
        return new JSONMessage(JSONMessageCode.INTERNAL_ERROR, exception.getMessage());
     }
 }
