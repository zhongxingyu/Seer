 package org.estudy.webservice;
 
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Response;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pham
  * Date: 11/4/13
  * Time: 11:58 PM
  * To change this template use File | Settings | File Templates.
  */
 public interface WebService {
	
    public Response checkPermission(@PathParam("username") String username, @PathParam("testid") String calendarId, @PathParam("type") String type) throws Exception;
 
    public Response getQuestions() throws Exception;
 
    public Response getAnswer(@PathParam("questionid") String questionid )throws Exception;
 }
