 package gov.nih.nci.caxchange.caxchangerequest;
 
 import gov.nih.nci.caxchange.messaging.ResponseMessage;
 
 import javax.xml.ws.AsyncHandler;
 import javax.xml.ws.Response;
 
 
 public class SampleAsyncHandler implements AsyncHandler<ResponseMessage> {
 	
	private ResponseMessage reply;
 
 	public void handleResponse(Response<ResponseMessage> response) {
 		try{
 			reply = response.get();
 		} catch (Exception e){
 			e.printStackTrace();
 		}		
 	}
 	
 	public gov.nih.nci.caxchange.messaging.Response getResponse(){
 		return reply.getResponse();
 	}
 
 }
