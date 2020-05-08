 __INIT_IMPORTS__
 import org.springframework.http.HttpStatus;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 __END_IMPORTS__
 
 __INIT_METHODS__
    @RequestMapping(method = RequestMethod.POST)
     @ResponseStatus(HttpStatus.CREATED)
     @ResponseBody
     public void createRest(@RequestBody __ENTITY__ __ENTITY_LOWER__) {
         __ENTITY_LOWER__Repository.save(__ENTITY_LOWER__);
     }
 __END_METHODS__
