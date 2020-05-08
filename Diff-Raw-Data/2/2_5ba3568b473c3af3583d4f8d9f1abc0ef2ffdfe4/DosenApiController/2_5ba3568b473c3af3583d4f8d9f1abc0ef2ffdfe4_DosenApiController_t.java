 package id.ac.pcr.springhibernate.api.controller;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import id.ac.pcr.springhibernate.model.Dosen;
 import id.ac.pcr.springhibernate.repository.DosenRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataIntegrityViolationException;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.FieldError;
 import org.springframework.web.HttpMediaTypeNotSupportedException;
 import org.springframework.web.bind.MethodArgumentNotValidException;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.util.UriTemplate;
 
 import javax.persistence.NoResultException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 import java.io.IOException;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Deny Prasetyo
  * Date: 4/5/13
  * Time: 9:01 AM
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 @RequestMapping("/api/dosen")
 public class DosenApiController {
 
     @Autowired
     private DosenRepository dosenRepository;
 
    @RequestMapping(value = "/",method = RequestMethod.GET)
     @ResponseBody
     public Page<Dosen> getAll(ModelMap modelMap, Pageable pageable) {
         return dosenRepository.findAll(pageable);
     }
 
 
     @RequestMapping(value = "/", method = RequestMethod.POST)
     @ResponseStatus(HttpStatus.CREATED)
     public void create(@Valid @RequestBody Dosen o, HttpServletRequest request, HttpServletResponse response) {
         dosenRepository.save(o);
 
         String requestUrl = request.getRequestURL().toString();
         URI uri = new UriTemplate("{requestUrl}{id}").expand(requestUrl, o.getId());
         response.setHeader("Location", uri.toASCIIString());
     }
 
 
     @RequestMapping("/{id}")
     @ResponseBody
     public Dosen findById(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
         Dosen o = dosenRepository.findOne(id);
         if (o == null) {
             throw new IllegalStateException();
         }
         return o;
     }
 
     @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
     @ResponseStatus(HttpStatus.OK)
     public void update(@PathVariable Long id, @Valid @RequestBody Dosen a, HttpServletRequest request, HttpServletResponse response) {
         Dosen o = dosenRepository.findOne(id);
 
         if (o == null) {
             throw new IllegalStateException();
         }
 
         o.setNiy(a.getNiy());
         o.setNama(a.getNama());
 
         String requestUrl = request.getRequestURL().toString();
         URI uri = new UriTemplate("{requestUrl}{id}").expand(requestUrl, a.getId());
         response.setHeader("Location", uri.toASCIIString());
     }
 
     @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
     @ResponseStatus(HttpStatus.OK)
     public void delete(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
         Dosen a = dosenRepository.findOne(id);
         if (a == null) {
             throw new IllegalStateException();
         }
 
         dosenRepository.delete(a);
 
         String requestUrl = request.getRequestURL().toString();
         URI uri = new UriTemplate("{requestUrl}").expand(requestUrl);
         response.setHeader("Location", uri.toASCIIString());
     }
 
     /*Exception Handlers*/
     @ResponseStatus(HttpStatus.NOT_FOUND)
     @ExceptionHandler({NoResultException.class})
     public void handle(NoResultException ex, HttpServletResponse response) {
         ex.printStackTrace();
         response.setHeader("Exception Message", ex.getMessage());
     }
 
     @ResponseStatus(HttpStatus.NOT_FOUND)
     @ExceptionHandler({EmptyResultDataAccessException.class})
     public void handle(EmptyResultDataAccessException ex, HttpServletResponse response) {
         ex.printStackTrace();
         response.setHeader("Exception Message", ex.getMessage());
     }
 
     @ResponseStatus(HttpStatus.NOT_FOUND)
     @ExceptionHandler({IllegalStateException.class})
     public void handle(IllegalStateException ex, HttpServletResponse response) {
         ex.printStackTrace();
         response.setHeader("Exception Message", ex.getMessage());
     }
 
     @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
     @ExceptionHandler({MethodArgumentNotValidException.class})
     public void handle(MethodArgumentNotValidException ex, HttpServletResponse response) throws IOException {
 
         ex.printStackTrace();
 
         Map<String, String> errorMap = new HashMap<String, String>();
         for (FieldError error : ex.getBindingResult().getFieldErrors()) {
             errorMap.put(error.getField(), error.getDefaultMessage());
         }
 
 
         response.setHeader("Exception Message", new ObjectMapper().writeValueAsString(errorMap));
     }
 
     @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
     @ExceptionHandler({DataIntegrityViolationException.class})
     public void handle(DataIntegrityViolationException ex, HttpServletResponse response) throws IOException {
         ex.printStackTrace();
         response.setHeader("Exception Message", ex.getMessage());
     }
 
     @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
     @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
     public void handle(HttpMediaTypeNotSupportedException ex, HttpServletResponse response) throws IOException {
         ex.printStackTrace();
         response.setHeader("Exception Message", ex.getMessage());
     }
 
 }
