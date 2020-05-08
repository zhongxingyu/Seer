 package com.nordija.statistic.mnm.agent;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.nordija.activemq.admin.ActiveMQBrokerAdmin;
 import com.nordija.statistic.mnm.rest.jaxb.DSRequest;
 import com.nordija.statistic.mnm.rest.jaxb.DSResponse;
 
 @Service("amqAdminResource")
 @Path("/amq/admin")
 @Produces( { MediaType.APPLICATION_XML})
 @Consumes( { MediaType.TEXT_XML })
 public class AmqAdminResource {
 	private static final Logger logger = LoggerFactory.getLogger(AmqAdminResource.class);
 	
 	@Autowired
 	private ActiveMQBrokerAdmin amqBrokerAdmin;
		
 	@POST
 	@Path("/brokerInfo")
 	public DSResponse brokerInfo(DSRequest request){
 		logger.info("amq/admin/brokerInfo called with {} operation.", request.getOperationType().name());
 
 		Map<String, Object> data = (Map)request.getData();
 		if(data == null || data.get("brokerName") == null){
 			String err = "No brokerName specified in the request";
 			logger.error(err);
 			throw new WebApplicationException(new IllegalArgumentException(err), 500);			
 		}
 		
 		logger.info("Returning Broker Info. of "+data.get("brokerName"));
 		try{
 			DSResponse response = new DSResponse();
 			response.addRecord(amqBrokerAdmin.getBrokerInfo());
 			response.setStatus(DSResponse.STATUS_SUCCESS);
 			return response;
 		}catch(Exception e){
 			logger.error("Failed to retrieve broker information for {}.", data.get("brokerName"));
 			throw new WebApplicationException(e, 500);
 		}
 	}
 
 	@POST
 	@Path("/queue")
 	public DSResponse queue(DSRequest request){
 		logger.info("admin/amq/queue called with {} operation.", request.getOperationType().name());
 
 		Map<String, Object> qData = (Map)request.getData();
 		if(qData == null || qData.get("brokerName") == null){
 			String err = "No brokerName specified in the request";
 			logger.error(err);
 			throw new WebApplicationException(new IllegalArgumentException(err), 500);			
 		}
 
 		DSResponse response = new DSResponse();
 		switch(request.getOperationType()){
 		case FETCH:
 			try {
 				List<Map<String, Object>> result = amqBrokerAdmin.getQueueInfo();
 				response.setData(result);
 				response.setTotalRows(result.size());
 			} catch (Exception e) {
 				logger.error("Error in retreiving list of queues.", e);
 				throw new WebApplicationException(e, 500);
 			}
 			break;
 		case ADD:
 			String qName = (String)qData.get("name");
 			try{
 				amqBrokerAdmin.createQueue(qName);
 				Map<String, Object> qi = amqBrokerAdmin.getQueueInfo(qName);
 				response.addRecord(qi);
 				response.setTotalRows(1);
 			}catch(Exception e){
 				logger.error("Failed to add queue {} in broker {}", qName, qData.get("brokerName"));
 				throw new WebApplicationException(e, 500);
 			}
 			break;
 		case REMOVE:
 			qName = (String)qData.get("name");
 			try{
 				amqBrokerAdmin.deleteQueue(qName);
 			}catch(Exception e){
 				logger.error("Failed to remove queue {} from broker {}", qName, qData.get("brokerName"));
 				throw new WebApplicationException(e, 500);
 			}
 			break;
 		case UPDATE:
 			String cmd = (String)qData.get("command");
 			qName = (String)qData.get("name");
 			if(cmd.equals("purge")){
 				try{
 					amqBrokerAdmin.purgeQueue(qName);
 				}catch(Exception e){
 					logger.error("Failed to purge queue {} from broker {}", qName, qData.get("brokerName"));
 					throw new WebApplicationException(e, 500);
 				}
 			}else if(cmd.equals("move")){
 				moveMessages(qName, (String)qData.get("toQueue"), null, (String)qData.get("selector"), 
 						(qData.get("maxMsgs") == null ? -1 : Integer.parseInt((String)qData.get("maxMsgs"))));
 			}
 			break;
 		}
 		response.setStatus(DSResponse.STATUS_SUCCESS);
 		return response;
 	}	
 	
 	@POST
 	@Path("/message")
 	public DSResponse message(DSRequest request){
 		logger.info("admin/amq/message called with {} operation.", request.getOperationType().name());
 		Map<String, Object> msg = request.getData();
 		if(msg == null || msg.get("queue") == null || msg.get("brokerName") == null){
 			String err = "No queue or broker was specified in the request. Both must be present in a message request.";
 			logger.error(err);
 			throw new WebApplicationException(new IllegalArgumentException(err), 500);			
 		}
 
 		DSResponse response = new DSResponse();
 		switch(request.getOperationType()){
 		case FETCH:
 			try {
 				if(msg.get("messageID") != null){
 					response.addRecord(amqBrokerAdmin.getMessage((String)msg.get("queue"), (String)msg.get("messageID")));
 				}else if(msg.get("selector") != null){
					logger.debug("FETCH: startRow = {}, endRow = {}, selector = {}.", new Object[]{request.getStartRow(), request.getEndRow(), msg.get("selector")});
 					response.setData(amqBrokerAdmin.getMessages((String)msg.get("queue"), (String)msg.get("selector"), request.getStartRow(), request.getEndRow()));
 				}else{
					logger.debug("FETCH: startRow = {}, endRow = {}.", request.getStartRow(), request.getEndRow());
 					response.setData(amqBrokerAdmin.getMessages((String)msg.get("queue"), null, request.getStartRow(), request.getEndRow()));
 				}
 				response.setStartRow(request.getStartRow());
				response.setEndRow(request.getStartRow()+(response.getData() == null ? 0 : response.getData().size()));
				if(request.getEndRow() > response.getEndRow()){					
					response.setTotalRows(response.getEndRow());
				}else{
					response.setTotalRows(getMaxPageSize((String)msg.get("queue")));
				}
				logger.info("FETCH response: startRow = {}, endRow = {}, totalRows = {}.", 
						new Object[]{response.getStartRow(), response.getEndRow(), response.getTotalRows()});
 			} catch (Exception e) {
 				logger.error("Error in fetching messages.", e);
 				throw new WebApplicationException(e, 500);
 			}
 			break;
 		case ADD:
 			String err = "Add operation is not supported for message.";
 			logger.error(err);
 			throw new WebApplicationException(new IllegalAccessError(err), 500);
 		case REMOVE:
 			String msgId = (String)msg.get("messageID");
 			try{
 				String[] msgIds;
 				if(msgId.contains(",")){
 					msgIds = msgId.split(",");
 				}else{
 					msgIds = new String[]{msgId};
 				}
 				amqBrokerAdmin.deleteMessage((String)msg.get("queue"), Arrays.asList(msgIds));
 			}catch(Exception e){
 				logger.error("Failed to remove messages {} from queue {}", msgId, msg.get("queue"));
 				throw new WebApplicationException(e, 500);
 			}
 			break;
 		case UPDATE:
 			// move and copy operations are sent as update
 			String cmd = (String)msg.get("command");
 			if(cmd == null){
 				String err2 = "Update operation is not supported for message.";
 				logger.error(err2);
 				throw new WebApplicationException(new IllegalAccessError(err2), 500);
 			}
 			msgId = (String)msg.get("messageID");
 			if(cmd.equals("move")){
 				moveMessages((String)msg.get("queue"), (String)msg.get("toQueue"), msgId, (String)msg.get("selector"), (msg.get("maxMsgs") == null ? -1 : Integer.parseInt((String)msg.get("maxMsgs"))));
 			}else if(cmd.equals("copy")){
 				try {
 					amqBrokerAdmin.copyMessage((String)msg.get("queue"), (String)msg.get("toQueue"), Arrays.asList(msgId.split(",")));				
 				} catch (Exception e) {
 					logger.error("Failed to copy messages {} to queue {}", msgId, msg.get("toQueue"));
 					throw new WebApplicationException(e, 500);
 				}
 			}
 		}
 		response.setStatus(DSResponse.STATUS_SUCCESS);
 		return response;
 	}	
 	
 	private int moveMessages(String fromQ, String toQ, String msgIds, String selector, int maxMsgs){
 		try{
 			if(msgIds != null){
 				amqBrokerAdmin.moveMessage(fromQ, toQ, Arrays.asList(msgIds.split(",")));
 			}else if(selector != null){
 				int result = amqBrokerAdmin.moveMessage(fromQ, toQ, selector, maxMsgs);
 				logger.info("Moved {} messages from {} to {}", new Object[]{result, fromQ, toQ});
 				return result;
 			}
 		} catch (Exception e) {
 			logger.error("Failed to move messages by selector '{}' from {} to {}", new Object[]{selector, fromQ, toQ});
 			throw new WebApplicationException(e, 500);
 		}
 		logger.warn("No message was moved from {} to {}, probably because neither 'msgId' nor 'selector' was specified in the request.");
 		return 0;
 	}	
	
	private int maxPageSize;
	private int getMaxPageSize(String queue){
		if(maxPageSize == 0){
			try {
				maxPageSize = amqBrokerAdmin.getMaxPageSize(queue);
			} catch (Exception e) {
				maxPageSize = 400;
			}
		}
		return maxPageSize;
	}
 }
