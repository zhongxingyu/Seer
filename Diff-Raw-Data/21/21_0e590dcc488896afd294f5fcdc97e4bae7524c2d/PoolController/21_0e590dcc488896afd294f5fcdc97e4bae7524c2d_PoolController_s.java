 package com.porvak.bracket.controller;
 
 import com.porvak.bracket.domain.Pool;
 import com.porvak.bracket.repository.PoolRepository;
 import com.porvak.bracket.service.PoolService;
 import com.porvak.bracket.socialize.account.Account;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.security.Principal;
 import java.util.Map;
 
 import static com.google.common.base.Preconditions.*;
 import static org.springframework.http.HttpStatus.*;
 import static org.springframework.web.bind.annotation.RequestMethod.*;
 
 @Controller
 public class PoolController extends AbstractBracketController {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(PoolController.class);
 
     @Inject
     private PoolService poolService;
 
     @Inject
     private PoolRepository poolRepository;
 
     /**
      * Create or update a users pick for a specific game in a given pool.
      *
      * A user may only have one set of picks per pool, so if a users pick already exists then
      * the application will overwrite the existing pick for the given game.
      *
      * @param poolId
      * @param userPickMap Map that contains 'regionId', 'gameId', and 'teamId'
      * @param currentUser
      */
     @ResponseStatus(CREATED)
     @RequestMapping(value = "/api/pool/{poolId}/user/pick", method = POST)
     public void saveUserPick(@PathVariable("poolId") String poolId, @RequestBody Map<String, Object> userPickMap, Principal currentUser){
         //TODO: validate that the given user has rights to save this pick
         Account account = getUserAccount(currentUser);
         poolService.addUserPick(account.getId(), poolId, userPickMap);
 
         LOGGER.debug("Added userpick for userId: [{}] poolId: [{}]with:\n{}", new Object[]{account.getId(), poolId, userPickMap});
     }
 
     @ResponseStatus(CREATED)
     @RequestMapping(value = "/api/pool/{poolId}/user/tiebreaker", method = POST)
     public void saveUserTieBreaker(@PathVariable("poolId") String poolId, @RequestBody Map<String, Object> tieBreakerScore, Principal currentUser){
         Account account = getUserAccount(currentUser);
         poolService.addTieBreaker(account.getId(), poolId, Integer.parseInt(checkNotNull(tieBreakerScore.get("tieBreaker"), "Tie Breaker can not be null.").toString()));
     }
    
     @ResponseStatus(OK)
     @RequestMapping(value = "/api/pool/{poolId}/region/{regionId}/game/{gameId}/userpick/{position}", method = DELETE)
     public void deleteUserPick(@PathVariable("poolId") String poolId, @PathVariable("regionId") int regionId, @PathVariable("gameId") int gameId, @PathVariable("position") int position, Principal currentUser){
         Account account = getUserAccount(currentUser);
         poolService.removeUserPick(account.getId(), poolId, regionId, gameId, position);
     }
 
     /**
      * Retrieve pool information for given pool
      *
      * @param poolId
      * @return
      */
     @ResponseBody
     @RequestMapping(value = "/api/pool/{poolId}", method = GET)
     public Pool getPool(@PathVariable("poolId") String poolId){
         return poolRepository.findOne(poolId);
     }
 
     @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
     @ExceptionHandler({NullPointerException.class, IOException.class, JsonGenerationException.class, JsonMappingException.class})
     public void handleException(HttpServletResponse response, Exception x) {
         LOGGER.warn("Unexpected Exception in Pool Controller", x);
     }
 }
