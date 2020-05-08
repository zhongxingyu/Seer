 package cj.demo;
 
 import org.springframework.http.ResponseEntity;
 import org.springframework.http.HttpStatus;
 
 public class RestResponse {
 
     public static <T> ResponseEntity<T> ok() {
         return new ResponseEntity<T>(HttpStatus.OK);
     }
 
     public static <T> ResponseEntity<T> ok(T entity) {
         return new ResponseEntity<T>(entity, HttpStatus.OK);
     }
 
     public static <T> ResponseEntity<T> created() {
         return new ResponseEntity<T>(HttpStatus.CREATED);
     }
 
     public static <T> ResponseEntity<T> created(T entity) {
         return new ResponseEntity<T>(entity, HttpStatus.CREATED);
     }
 
    public static  ResponseEntity noContent() {
         return new ResponseEntity(HttpStatus.NO_CONTENT);
     }
 
    public static ResponseEntity notFound() {
         return new ResponseEntity(HttpStatus.NOT_FOUND);
     }
 
 }
