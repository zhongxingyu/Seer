 package com.alexshabanov.springrestapi.rest.client;
 
 import com.alexshabanov.springrestapi.domain.User;
 import com.alexshabanov.springrestapi.rest.common.IdHolder;
 import com.alexshabanov.springrestapi.rest.common.RestConstants;
 import com.alexshabanov.springrestapi.service.UserService;
 import org.springframework.web.client.RestOperations;
 
 import java.util.List;
 
 /**
  * Communicates with the exposed UserService.
  */
 public final class UserRestClientService extends AbstractRestClientService implements UserService {
 
     public UserRestClientService(String baseUrl, RestOperations restOperations) {
         super(baseUrl, restOperations);
     }
 
     @Override
     public int register(String name) {
        return getRestOperations().postForObject(getMethodUri(RestConstants.REGISTER_USER_URI), name, IdHolder.class)
                 .getId();
     }
 
     @Override
     public User findById(int id) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public List<User> findUsers() {
         throw new UnsupportedOperationException();
     }
 }
