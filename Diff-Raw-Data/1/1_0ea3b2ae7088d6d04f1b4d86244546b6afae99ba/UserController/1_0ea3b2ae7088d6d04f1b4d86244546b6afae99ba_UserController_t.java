 package com.checkers.server.controllers;
 
 import com.checkers.server.Consts;
 import com.checkers.server.beans.ExceptionMessage;
 import com.checkers.server.beans.Game;
 import com.checkers.server.beans.User;
 import com.checkers.server.exceptions.LogicException;
 import com.checkers.server.services.GameService;
 import com.checkers.server.services.UserService;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.converter.HttpMessageNotReadableException;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.MethodArgumentNotValidException;
 import org.springframework.web.bind.annotation.*;
 
 import javax.validation.Valid;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.List;
 
 /**
  *
  *
  * @author Pavel Kuchin
  */
 
 @Controller
 @RequestMapping(value="/users")
 public class UserController {
 
     static Logger log = Logger.getLogger(UserController.class.getName());
 
     @Autowired
     UserService userService;
 
     @Autowired
     GameService gameService;
 
     /**
      * <h3>/users</h3>
      *
      * <b>Method:</b> GET
      * <b>Description:</b> return all users as JSON objects list
      * <b>Allowed roles:</b> ROLE_USER, ROLE_ADMIN
      */
     @RequestMapping(value = "", method = RequestMethod.GET, headers = {"Accept=application/json"})
     public @ResponseBody
     List<User> getUsers(){
         log.info("All users returned");
             return userService.getUsers();
     }
 
     /**
      * <h3>/users</h3>
      *
      * <b>Method:</b> POST
      * <b>Description:</b> creates a new user
      * <b>Allowed roles:</b> ROLE_ADMIN
      */
     @RequestMapping(value = "", method = RequestMethod.POST, headers = {"Accept=application/json"})
     @ResponseStatus(HttpStatus.CREATED)
     public @ResponseBody
     User newUser(@Valid @RequestBody User user) throws LogicException {
         log.info("User: \"" + user.getLogin() + "\" created");
         userService.newUser(user);
         return user;
     }
 
     /**
      * <3>/users/{uuid}</3>
      *
      * <b>Method:</b> GET
      * <b>Description:</b> returns user with specific uuid (Long)
      * <b>Allowed roles:</b> ROLE_USER, ROLE_ADMIN
      */
     @RequestMapping(value = "/{uuid}", method = RequestMethod.GET, headers = {"Accept=application/json"})
     public @ResponseBody
     User getUser(@PathVariable String uuid) throws LogicException {
         log.info("Returned user with uuid: " + uuid);
 
         Long uuidLong = null;
 
         if(!uuid.equals(Consts.ME)){
             try{
                 uuidLong = Long.parseLong(uuid);
             } catch(NumberFormatException nfe){
                 uuidLong = userService.getUserByLogin(uuid).getUuid();
             }
         }
 
             return userService.getUser(uuidLong);
     }
 
     /**
      * <h3>/users/{uuid}/games</h3>
      *
      * <b>Method:</b> GET
      * <b>Description:</b> returns all user games
      * <b>Allowed roles:</b> ROLE_USER, ROLE_ADMIN
      */
     @RequestMapping(value = "/{uuid}/games", method = RequestMethod.GET, headers = {"Accept=application/json"})
     public @ResponseBody
     List<Game> getUserGames(@PathVariable String uuid) throws LogicException {
         log.info("All games for user " + uuid + " returned");
 
             Long uuidLong = null;
 
             if(!uuid.equals(Consts.ME)){
                 try{
                     uuidLong = Long.parseLong(uuid);
                 } catch(NumberFormatException nfe){
                     uuidLong = userService.getUserByLogin(uuid).getUuid();
                 }
             }
 
                 return gameService.getUserGames(uuidLong);
     }
 
     /**
      *   <h3>/users?action=registration</h3>
      *
      *   <b>Method:</b> POST
      *   <b>Description:</b> new user registrations
      *   <b>Allowed roles:</b> ANYONE
      */
     @RequestMapping(value = "/registration/", method = RequestMethod.POST, headers = {"Accept=application/json"})
     @ResponseStatus(HttpStatus.CREATED)
     public @ResponseBody
     User regUser(@Valid @RequestBody User user) throws LogicException {
         log.info("User " + user.getLogin() + " registration has been started");
 
         userService.regUser(user);
 
         return user;
     }
 
     /**
     *    <3>/users/{uuid}</3>
     *
     *    <b>Method: DELETE</b>
     *    <b>Description: deletes user with specific uuid (Long). The 'me' constant is not appropriate here.</b>
     *    <b>Allowed roles: ROLE_ADMIN</b>
     */
     @RequestMapping(value = "/{uuid}", method = RequestMethod.DELETE, headers = {"Accept=application/json"})
     @ResponseStatus(HttpStatus.OK)
     public @ResponseBody
     User delUser(@PathVariable String uuid) throws LogicException {
         log.info("User delete process has been started");
 
         Long uuidLong;
 
         try{
             uuidLong = Long.parseLong(uuid);
         } catch(NumberFormatException nfe){
             uuidLong = userService.getUserByLogin(uuid).getUuid();
         }
 
         userService.delUser(uuidLong);
 
         return null;
     }
 
     /**
     *    <h3>/users/{uuid}</h3>
     *
     *    <b>Method:</b> PUT
     *    <b>Description:</b> modifies user with specific uuid (Long)
     *    <b>Allowed roles:</b> ROLE_USER(owner only), ROLE_ADMIN
     */
     @RequestMapping(value = "/{uuid}", method = RequestMethod.PUT, headers = {"Accept=application/json"})
    @ResponseStatus(HttpStatus.OK)
     public @ResponseBody
     User modUser(@PathVariable String uuid, @Valid @RequestBody User user) throws LogicException {
         log.info("User modification has been started");
 
         Long uuidLong = null;
 
         if(!uuid.equals(Consts.ME)){
             try{
                 uuidLong = Long.parseLong(uuid);
             } catch(NumberFormatException nfe){
                 uuidLong = userService.getUserByLogin(uuid).getUuid();
             }
         }
 
             return userService.modUser(uuidLong, user);
     }
 
     /**
      *
      * EXCEPTION HANDLERS
      *
      */
 
     @ExceptionHandler(LogicException.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     public @ResponseBody
     ExceptionMessage handleLogicException(LogicException e){
         log.warn(e + " : " + e.getMessage());
 
         return e.getExceptionMessage();
     }
 
     @ExceptionHandler(HttpMessageNotReadableException.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     public @ResponseBody
     ExceptionMessage handleHttpMessageNotReadableException(HttpMessageNotReadableException e){
         log.warn(e + " : " + e.getMessage());
 
         ExceptionMessage em = new ExceptionMessage();
 
         em.setCode(105L);
         em.setMessage(e.getMessage());
         em.setDetailsURL("https://github.com/pavelkuchin/checkers/wiki/Errors#code-105");
 
         return em;
     }
 
 
     @ExceptionHandler(MethodArgumentNotValidException.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     public @ResponseBody
     ExceptionMessage ArgumentNotValidException(MethodArgumentNotValidException e){
         log.warn(e + " : " + e.getMessage());
 
         ExceptionMessage em = new ExceptionMessage();
 
         em.setCode(106L);
         List<ObjectError> errors = e.getBindingResult().getAllErrors();
         StringBuilder strErrors = new StringBuilder();
         for(ObjectError oe : errors){
             strErrors.append(oe.getDefaultMessage());
             strErrors.append("\n");
         }
 
         em.setMessage(strErrors.toString());
         em.setDetailsURL("https://github.com/pavelkuchin/checkers/wiki/Errors#code-106");
 
         return em;
     }
 
     @ExceptionHandler(Exception.class)
     @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
     public @ResponseBody
     ExceptionMessage internalException(Exception e){
         StringWriter sw = new StringWriter();
         e.printStackTrace(new PrintWriter(sw));
         log.error("Message: " + e.getMessage());
         log.error("StackTrace: " + sw.toString());
 
         ExceptionMessage em = new ExceptionMessage();
 
         em.setCode(4L);
         em.setMessage(e.getMessage());
         em.setDetailsURL("https://github.com/pavelkuchin/checkers/wiki/Errors#code-4");
 
         return em;
     }
 }
