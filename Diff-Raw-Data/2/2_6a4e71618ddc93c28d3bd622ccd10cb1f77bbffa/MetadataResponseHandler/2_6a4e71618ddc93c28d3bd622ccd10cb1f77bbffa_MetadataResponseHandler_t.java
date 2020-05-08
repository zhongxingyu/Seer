 /**
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.ushahidi.swiftriver.core.dropqueue;
 
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.amqp.core.Message;
 import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
 import org.springframework.util.ErrorHandler;
 
 import com.rabbitmq.client.Channel;
 import com.ushahidi.swiftriver.core.dropqueue.model.RawDrop;
 
 /**
  * Handler for incoming drops from metadata extractors.
  * 
  * Update the in memory drops map with the response from the metadata
  * extractors.
  * 
  * Puts drops that have completed metadata extraction onto a publish queue for
  * posting to the SwiftRiver REST API.
  * 
  */
 public class MetadataResponseHandler implements ChannelAwareMessageListener,
 		ErrorHandler {
 
 	final Logger logger = LoggerFactory
			.getLogger(MetadataResponseHandler.class);
 
 	private ObjectMapper objectMapper;
 
 	private Map<String, RawDrop> dropsMap;
 
 	private BlockingQueue<RawDrop> publishQueue;
 	
 	private BlockingQueue<RawDrop> rulesQueue;
 	
 	public ObjectMapper getObjectMapper() {
 		return objectMapper;
 	}
 
 	public void setObjectMapper(ObjectMapper objectMapper) {
 		this.objectMapper = objectMapper;
 	}
 
 	public Map<String, RawDrop> getDropsMap() {
 		return dropsMap;
 	}
 
 	public void setDropsMap(Map<String, RawDrop> dropsMap) {
 		this.dropsMap = dropsMap;
 	}
 
 	public BlockingQueue<RawDrop> getPublishQueue() {
 		return publishQueue;
 	}
 
 	public void setPublishQueue(BlockingQueue<RawDrop> publishQueue) {
 		this.publishQueue = publishQueue;
 	}
 	
 	public BlockingQueue<RawDrop> getRulesQueue() {
 		return rulesQueue;
 	}
 
 	public void setRulesQueue(BlockingQueue<RawDrop> rulesQueue) {
 		this.rulesQueue = rulesQueue;
 	}
 
 	/**
 	 * Receive drop that has completed metadata extraction.
 	 * 
 	 * Updates the locally cached drop with received metadata. 
 	 * Drops that have completed both media and sematic extraction
 	 * get added to the publishQueue for posting to the API.
 	 * 
 	 * @param message
 	 * @param channel
 	 * @throws Exception
 	 */
 	public void onMessage(Message message, Channel channel) throws Exception {
 		String correlationId = new String(message.getMessageProperties()
 				.getCorrelationId());
 		RawDrop updatedDrop = objectMapper.readValue(
 				new String(message.getBody()), RawDrop.class);
 		logger.debug(String
 				.format("Metadata Response received from '%s' with correlation_id '%s'",
 						updatedDrop.getSource(), correlationId));
 
 		synchronized (dropsMap) {
 			RawDrop cachedDrop = dropsMap.get(correlationId);
 
 			if (updatedDrop.getSource().equals("mediaextractor")) {
 				cachedDrop.setMediaComplete(true);
 				cachedDrop.setMedia(updatedDrop.getMedia());
 				cachedDrop.setLinks(updatedDrop.getLinks());
 			} else if (updatedDrop.getSource().equals("semantics")) {
 				cachedDrop.setSemanticsComplete(true);
 				cachedDrop.setTags(updatedDrop.getTags());
 				cachedDrop.setPlaces(updatedDrop.getPlaces());
 			} else if (updatedDrop.getSource().equals("rules")) {
 				cachedDrop.setRulesComplete(true);
 			}
 
 			// When semantics and metadata extraction are complete,
 			// submit for rules processing
 			if (cachedDrop.isSemanticsComplete() && cachedDrop.isMediaComplete()) {
 				logger.debug(String.format("Sending drop with correlation id '%s' for rules processing",
 						correlationId));
 				rulesQueue.put(cachedDrop);
 			}
 
 			if (cachedDrop.isSemanticsComplete()
 					&& cachedDrop.isMediaComplete() && cachedDrop.isRulesComplete()) {
 				logger.debug(String
 						.format("Drop with correlation id '%s' has completed metadata extraction",
 								correlationId));
 				publishQueue.put(cachedDrop);
 				dropsMap.remove(correlationId);
 			}
 		}
 	}
 
 	public void handleError(Throwable t) {
 		logger.error("Metadata response error", t);
 	}
 
 }
