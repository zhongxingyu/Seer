 //
 // typica - A client library for Amazon Web Services
 // Copyright (C) 2007 Xerox Corporation
 // 
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 
 package com.xerox.amazonws.sqs;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBException;
 
 import com.xerox.amazonws.common.JAXBuddy;
 import com.xerox.amazonws.typica.jaxb.AddGrantResponse;
 import com.xerox.amazonws.typica.jaxb.AttributedValue;
 import com.xerox.amazonws.typica.jaxb.ChangeMessageVisibilityResponse;
 import com.xerox.amazonws.typica.jaxb.DeleteMessageResponse;
 import com.xerox.amazonws.typica.jaxb.DeleteQueueResponse;
 import com.xerox.amazonws.typica.jaxb.GetQueueAttributesResponse;
 import com.xerox.amazonws.typica.jaxb.ListGrantsResponse;
 import com.xerox.amazonws.typica.jaxb.PeekMessageResponse;
 import com.xerox.amazonws.typica.jaxb.ReceiveMessageResponse;
 import com.xerox.amazonws.typica.jaxb.RemoveGrantResponse;
 import com.xerox.amazonws.typica.jaxb.SendMessageResponse;
 import com.xerox.amazonws.typica.jaxb.SetQueueAttributesResponse;
 
 /**
  * This class provides an interface with the Amazon SQS message queue. It provides methods
  * for sending / receiving messages and deleting queues and messsages on queues.
  *
  * @author D. Kavanagh
  * @author developer@dotech.com
  */
 public class MessageQueue extends QueueService {
     public static final int MAX_MESSAGES = 600;
     public static final int MAX_MESSAGE_BODIES_SIZE = 4096;
 
 	protected String queueId;
 
     protected MessageQueue(String queueUrl, String awsAccessKeyId,
 							String awsSecretAccessKey, boolean isSecure,
 							String server) throws SQSException {
         super(awsAccessKeyId, awsSecretAccessKey, isSecure, server);
 		queueId = queueUrl.substring(queueUrl.indexOf("//")+2);
 		queueId = queueId.substring(queueId.indexOf("/")+1);
     }
 
 	/**
 	 * This method provides the URL for the message queue represented by this object.
 	 *
 	 * @return generated queue service url
 	 */
 	public URL getUrl() {
 		try {
			return new URL(super.getUrl().toString()+queueId);
 		} catch (MalformedURLException ex) {
 			return null;
 		}
 	}
 
 	/**
 	 * Sends a message to a specified queue. The message must be between 1 and 256K bytes long.
 	 *
 	 * @param msg the message to be sent (should be base64 encoded)
 	 * @return the message id for the message just sent
 	 */
     public String sendMessage(String msg) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		try {
 			URLConnection conn = makeRequest("POST", "SendMessage", params);
 			conn.setRequestProperty("content-type", "text/plain");
 			conn.setDoOutput(true);
 			OutputStream oStr = conn.getOutputStream();
 			oStr.write(new String(msg).getBytes());
 			oStr.flush();
 			InputStream iStr = conn.getInputStream();
 			SendMessageResponse response = JAXBuddy.deserializeXMLStream(SendMessageResponse.class, iStr);
 			return response.getMessageId();
 		} catch (JAXBException ex) {
 			throw new SQSException("Problem parsing returned message.", ex);
 		} catch (MalformedURLException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Attempts to receive a message from the queue. The queue default visibility timeout
 	 * is used.
 	 *
 	 * @return the message object
 	 */
     public Message receiveMessage() throws SQSException {
         Message amessage[] = receiveMessages(BigInteger.valueOf(1L), ((BigInteger) (null)));
         if(amessage.length > 0)
             return amessage[0];
         else
             return null;
 	}
 
 	/**
 	 * Attempts to receive a message from the queue.
 	 *
 	 * @param visibilityTimeout the duration (in seconds) the retrieved message is hidden from
 	 *                          subsequent calls to retrieve.
 	 * @return the message object
 	 */
     public Message receiveMessage(int visibilityTimeout) throws SQSException {
         Message amessage[] = receiveMessages(BigInteger.valueOf(1L), BigInteger.valueOf(visibilityTimeout));
         if(amessage.length > 0)
             return amessage[0];
         else
             return null;
 	}
 
 	/**
 	 * Attempts to retrieve a number of messages from the queue. If less than that are availble,
 	 * the max returned is the number of messages in the queue, but not necessarily all messages
 	 * in the queue will be returned. The queue default visibility timeout is used.
 	 *
 	 * @param numMessages the maximum number of messages to return
 	 * @return an array of message objects
 	 */
     public Message[] receiveMessages(int numMessages) throws SQSException {
         return receiveMessages(BigInteger.valueOf(numMessages), ((BigInteger) (null)));
 	}
 
 	/**
 	 * Attempts to retrieve a number of messages from the queue. If less than that are availble,
 	 * the max returned is the number of messages in the queue, but not necessarily all messages
 	 * in the queue will be returned.
 	 *
 	 * @param numMessages the maximum number of messages to return
 	 * @param visibilityTimeout the duration (in seconds) the retrieved message is hidden from
 	 *                          subsequent calls to retrieve.
 	 * @return an array of message objects
 	 */
     public Message[] receiveMessages(int numMessages, int visibilityTimeout) throws SQSException {
         return receiveMessages(BigInteger.valueOf(numMessages), BigInteger.valueOf(visibilityTimeout));
 	}
 
 	/**
 	 * Internal implementation of receiveMessages.
 	 *
 	 * @param
 	 * @return
 	 */
     protected Message[] receiveMessages(BigInteger numMessages, BigInteger visibilityTimeout) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		if (numMessages != null) {
 			params.put("NumberOfMessages", numMessages.toString());
 		}
 		if (visibilityTimeout != null) {
 			params.put("VisibilityTimeout", visibilityTimeout.toString());
 		}
 		try {
 			HttpURLConnection conn = makeRequest("GET", "ReceiveMessage", params);
 			if (conn.getResponseCode() < 400) {
 				InputStream iStr = conn.getInputStream();
 				ReceiveMessageResponse response = JAXBuddy.deserializeXMLStream(ReceiveMessageResponse.class, iStr);
 				if (response.getMessages() == null) {
 					return new Message[0];
 				}
 				else {
 					ArrayList<Message> msgs = new ArrayList();
 					for (com.xerox.amazonws.typica.jaxb.Message msg : response.getMessages()) {
 						msgs.add(new Message(msg.getMessageId(), msg.getMessageBody()));
 					}
 					return msgs.toArray(new Message [msgs.size()]);
 				}
 			}
 			else {
 				return new Message[0];
 			}
 		} catch (JAXBException ex) {
 			throw new SQSException("Problem parsing returned message.", ex);
 		} catch (MalformedURLException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Returns a specified message. This does not affect and is not affected by the visibility
 	 * timeout of either the queue or the message.
 	 *
 	 * @param
 	 * @return
 	 */
     public Message peekMessage(String msgId) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("MessageId", msgId);
 		try {
 			InputStream iStr = makeRequest("GET", "PeekMessage", params).getInputStream();
 			PeekMessageResponse response = JAXBuddy.deserializeXMLStream(PeekMessageResponse.class, iStr);
 			com.xerox.amazonws.typica.jaxb.Message msg = response.getMessage();
 			if (msg == null) {
 				return null;
 			}
 			else {
 				return new Message(msg.getMessageId(), msg.getMessageBody());
 			}
 		} catch (JAXBException ex) {
 			throw new SQSException("Problem parsing returned message.", ex);
 		} catch (MalformedURLException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Deletes the message identified by message object on the queue this object represents.
 	 *
 	 * @param msg the message to be deleted
 	 */
     public void deleteMessage(Message msg) throws SQSException {
 		deleteMessage(msg.getMessageId());
 	}
 
 	/**
 	 * Deletes the message identified by msgid on the queue this object represents.
 	 *
 	 * @param msgId the id of the message to be deleted
 	 */
     public void deleteMessage(String msgId) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("MessageId", msgId);
 		try {
 			HttpURLConnection conn = makeRequest("GET", "DeleteMessage", params);
 			if (conn.getResponseCode() < 400) {
 				InputStream iStr = conn.getInputStream();
 				DeleteMessageResponse response = JAXBuddy.deserializeXMLStream(DeleteMessageResponse.class, iStr);
 			}
 			else {
 				throw new SQSException("Error deleting message id="+msgId);
 			}
 		} catch (JAXBException ex) {
 			throw new SQSException("Problem parsing returned message.", ex);
 		} catch (MalformedURLException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Deletes the message queue represented by this object. Will fail if queue isn't empty.
 	 */
     public void deleteQueue() throws SQSException {
 		deleteQueue(false);
 	}
 
 	/**
 	 * Deletes the message queue represented by this object.
 	 *
 	 * @param force when true, non-empty queues will be deleted
 	 */
     public void deleteQueue(boolean force) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		if (force) {
 			params.put("ForceDeletion", "true");
 		}
         int respCode;
 		try {
 			HttpURLConnection conn = makeRequest("GET", "DeleteQueue", params);
 			if ((respCode = conn.getResponseCode()) < 400) {
 				InputStream iStr = conn.getInputStream();
 				DeleteQueueResponse response = JAXBuddy.deserializeXMLStream(DeleteQueueResponse.class, iStr);
 			}
 			else {
 				throw new SQSException("Error deleting queue, response code = "+respCode);
 			}
 		} catch (JAXBException ex) {
 			throw new SQSException("Problem parsing returned message.", ex);
 		} catch (MalformedURLException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Sets the message visibility timeout. 
 	 *
 	 * @param msg the message
 	 * @param timeout the duration (in seconds) the retrieved message is hidden from
 	 *                          subsequent calls to retrieve.
 	 */
     public void setVisibilityTimeout(Message msg, int timeout) throws SQSException {
 		setVisibilityTimeout(msg.getMessageId(), timeout);
 	}
 
 	/**
 	 * Sets the message visibility timeout. 
 	 *
 	 * @param msgId the id of the message
 	 * @param timeout the duration (in seconds) the retrieved message is hidden from
 	 *                          subsequent calls to retrieve.
 	 */
     public void setVisibilityTimeout(String msgId, int timeout) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("MessageId", ""+msgId);
 		params.put("VisibilityTimeout", ""+timeout);
 		try {
 			HttpURLConnection conn = makeRequest("GET", "ChangeMessageVisibility", params);
 			if (conn.getResponseCode() < 400) {
 				InputStream iStr = conn.getInputStream();
 				ChangeMessageVisibilityResponse response = JAXBuddy.deserializeXMLStream(ChangeMessageVisibilityResponse.class, iStr);
 				if (response.getResponseStatus().getStatusCode().equals("Success")) {
 					return;
 				}
 				else {
 					throw new SQSException("Error setting timeout. Response msg = "+response.getResponseStatus().getMessage());
 				}
 			}
 			else {
 				throw new SQSException("Error setting timeout. Response code = "+conn.getResponseCode());
 			}
 		} catch (JAXBException ex) {
 			throw new SQSException("Problem setting the visibility timeout.", ex);
 		} catch (MalformedURLException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Sets the messages' visibility timeout. 
 	 *
 	 * @param msgIds the ids of the messages
 	 * @param timeout the duration (in seconds) the retrieved message is hidden from
 	 *                          subsequent calls to retrieve.
 	 */
     public void setVisibilityTimeout(String[] msgIds, int timeout) throws SQSException {
 		for (String id : msgIds) {
 			setVisibilityTimeout(id, timeout);
 		}
 	}
 
 	/**
 	 * Gets the visibility timeout for the queue. Uses {@link getQueueAttribute()}.
 	 */
     public int getVisibilityTimeout() throws SQSException {
 		return Integer.parseInt(getQueueAttributes(QueueAttribute.VISIBILITY_TIMEOUT)
 										.values().iterator().next());
 	}
 
 	/**
 	 * Gets the visibility timeout for the queue. Uses {@link getQueueAttribute()}.
 	 */
     public int getApproximateNumberOfMessages() throws SQSException {
 		return Integer.parseInt(getQueueAttributes(QueueAttribute.APPROXIMATE_NUMBER_OF_MESSAGES)
 										.values().iterator().next());
 	}
 
 	/**
 	 * Gets queue attributes. This is provided to expose the underlying functionality.
 	 * Currently supported attributes are ApproximateNumberOfMessages and VisibilityTimeout.
 	 *
 	 * @return a map of attributes and their values
 	 */
 	public Map<String,String> getQueueAttributes(QueueAttribute qAttr) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("Attribute", qAttr.queryAttribute());
 		try {
 			HttpURLConnection conn = makeRequest("GET", "GetQueueAttributes", params);
 			if (conn.getResponseCode() < 400) {
 				InputStream iStr = conn.getInputStream();
 				GetQueueAttributesResponse response = JAXBuddy.deserializeXMLStream(GetQueueAttributesResponse.class, iStr);
 				if (response.getResponseStatus().getStatusCode().equals("Success")) {
 					Map<String,String> ret = new HashMap<String,String>();
 					List<AttributedValue> attrs = response.getAttributedValues();
 					for (AttributedValue attr : attrs) {
 						ret.put(attr.getAttribute(), attr.getValue());
 					}
 					return ret;
 				}
 				else {
 					throw new SQSException("Error getting timeout. Response msg = "+response.getResponseStatus().getMessage());
 				}
 			}
 			else {
 				throw new SQSException("Error getting timeout. Response code = "+conn.getResponseCode());
 			}
 		} catch (JAXBException ex) {
 			throw new SQSException("Problem getting the visilibity timeout.", ex);
 		} catch (MalformedURLException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Sets the visibility timeout of the queue. Uses {@link setQueueAttribute(String, String)}.
 	 *
 	 * @param timeout the duration (in seconds) the retrieved message is hidden from
 	 *                          subsequent calls to retrieve.
 	 */
     public void setVisibilityTimeout(int timeout) throws SQSException {
 		setQueueAttribute("VisibilityTimeout", ""+timeout);
 	}
 
 	/**
 	 * Sets a queue attribute. This is provided to expose the underlying functionality, although
 	 * the only attribute at this time is visibility timeout.
 	 *
 	 * @param attribute name of the attribute being set
 	 * @param value the value being set for this attribute
 	 */
     public void setQueueAttribute(String attribute, String value) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("Attribute", attribute);
 		params.put("Value", value);
 		try {
 			HttpURLConnection conn = makeRequest("GET", "SetQueueAttributes", params);
 			if (conn.getResponseCode() < 400) {
 				InputStream iStr = conn.getInputStream();
 				SetQueueAttributesResponse response = JAXBuddy.deserializeXMLStream(SetQueueAttributesResponse.class, iStr);
 				if (response.getResponseStatus().getStatusCode().equals("Success")) {
 					return;
 				}
 				else {
 					throw new SQSException("Error setting timeout. Response msg = "+response.getResponseStatus().getMessage());
 				}
 			}
 			else {
 				throw new SQSException("Error setting timeout. Response code = "+conn.getResponseCode());
 			}
 		} catch (JAXBException ex) {
 			throw new SQSException("Problem setting the visibility timeout.", ex);
 		} catch (MalformedURLException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Adds a grant for a specific user.
 	 *
 	 * @param eMailAddress the amazon address of the user
 	 * @param permission the permission to add (ReceiveMessage | SendMessage | FullControl)
 	 */
     public void addGrantByEmailAddress(String eMailAddress, String permission) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		if (permission != null && !permission.trim().equals("")) {
 			params.put("Permission", permission);
 		}
 		params.put("Grantee.EmailAddress", eMailAddress);
 		addGrant(params);
 	}
 
 	/**
 	 * Adds a grant for a specific user.
 	 *
 	 * @param id the amazon user id of the user
 	 * @param displayName not sure if this can even be used
 	 * @param permission the permission to add (ReceiveMessage | SendMessage | FullControl)
 	 */
     public void addGrantByCustomerId(String id, String displayName, String permission) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		if (permission != null && !permission.trim().equals("")) {
 			params.put("Permission", permission);
 		}
 		params.put("Grantee.ID", id);
 		addGrant(params);
 	}
 
 	private void addGrant(Map<String, String> params) throws SQSException {
 		try {
 			HttpURLConnection conn = makeRequest("GET", "AddGrant", params);
 			if (conn.getResponseCode() < 400) {
 				InputStream iStr = conn.getInputStream();
 				AddGrantResponse response = JAXBuddy.deserializeXMLStream(AddGrantResponse.class, iStr);
 				if (response.getResponseStatus().getStatusCode().equals("Success")) {
 					return;
 				}
 				else {
 					throw new SQSException("Error adding grant. Response msg = "+response.getResponseStatus().getMessage());
 				}
 			}
 			else {
 				throw new SQSException("Error adding grant. Response code = "+conn.getResponseCode());
 			}
 		} catch (JAXBException ex) {
 			throw new SQSException("Problem parsing returned message.", ex);
 		} catch (MalformedURLException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Removes a grant for a specific user.
 	 *
 	 * @param eMailAddress the amazon address of the user
 	 * @param permission the permission to add (ReceiveMessage | SendMessage | FullControl)
 	 */
     public void removeGrantByEmailAddress(String eMailAddress, String permission) throws Exception {
 		Map<String, String> params = new HashMap<String, String>();
 		if (permission != null && !permission.trim().equals("")) {
 			params.put("Permission", permission);
 		}
 		params.put("Grantee.EmailAddress", eMailAddress);
 		removeGrant(params);
 	}
 
 	/**
 	 * Removes a grant for a specific user.
 	 *
 	 * @param id the amazon user id of the user
 	 * @param displayName not sure if this can even be used
 	 * @param permission the permission to add (ReceiveMessage | SendMessage | FullControl)
 	 */
     public void removeGrantByCustomerId(String id, String permission) throws Exception {
 		Map<String, String> params = new HashMap<String, String>();
 		if (permission != null && !permission.trim().equals("")) {
 			params.put("Permission", permission);
 		}
 		params.put("Grantee.ID", id);
 		removeGrant(params);
 	}
 
 	private void removeGrant(Map<String, String> params) throws SQSException {
 		try {
 			HttpURLConnection conn = makeRequest("GET", "RemoveGrant", params);
 			if (conn.getResponseCode() < 400) {
 				InputStream iStr = conn.getInputStream();
 				RemoveGrantResponse response = JAXBuddy.deserializeXMLStream(RemoveGrantResponse.class, iStr);
 				if (response.getResponseStatus().getStatusCode().equals("Success")) {
 					return;
 				}
 				else {
 					throw new SQSException("Error removing grant. Response msg = "+response.getResponseStatus().getMessage());
 				}
 			}
 			else {
 				throw new SQSException("Error adding grant. Response code = "+conn.getResponseCode());
 			}
 		} catch (JAXBException ex) {
 			throw new SQSException("Problem parsing returned message.", ex);
 		} catch (MalformedURLException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Retrieves a list of grants for this queue. The results can be filtered by specifying
 	 * a grantee or a particular permission.
 	 *
 	 * @param grantee the optional user or group
 	 * @param permission the optional permission
 	 * @return a list of objects representing the grants
 	 */
     public Grant[] listGrants(Grantee grantee, String permission) throws Exception {
 		Map<String, String> params = new HashMap<String, String>();
 		if (permission != null && !permission.trim().equals("")) {
 			params.put("Permission", permission);
 		}
 		if (grantee instanceof CanonicalUser) {
 			params.put("Grantee.ID", ((CanonicalUser)grantee).getID());
 		}
 		try {
 			InputStream iStr =
 				makeRequest("GET", "ListGrants", params).getInputStream();
 			ListGrantsResponse response =
 					JAXBuddy.deserializeXMLStream(ListGrantsResponse.class, iStr);
 			if (response.getResponseStatus().getStatusCode().equals("Success")) {
 				Grant [] grants = new Grant[response.getGrantLists().size()];
 				int i=0;
 				for (com.xerox.amazonws.typica.jaxb.Grant g : response.getGrantLists()) {
 					Grantee g2 = null;
 					if (g.getGrantee() instanceof com.xerox.amazonws.typica.jaxb.Group) {
 						com.xerox.amazonws.typica.jaxb.Group grp =
 								(com.xerox.amazonws.typica.jaxb.Group)g.getGrantee();
 						g2 = new Group(new URI(grp.getURI()));
 					}
 					else if (g.getGrantee() instanceof com.xerox.amazonws.typica.jaxb.CanonicalUser) {
 						com.xerox.amazonws.typica.jaxb.CanonicalUser u =
 								(com.xerox.amazonws.typica.jaxb.CanonicalUser)g.getGrantee();
 						g2 = new CanonicalUser(u.getID(), u.getDisplayName());
 					}
 					grants[i] = new Grant(g2, g.getPermission());
 					i++;
 				}
 				return grants;
 			}
 			else {
 				throw new SQSException("Error getting grants. Response msg = "+response.getResponseStatus().getMessage());
 			}
 		} catch (JAXBException ex) {
 			throw new SQSException("Problem parsing returned message.", ex);
 		} catch (MalformedURLException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		}
 	}
 
 	/**
 	 * Overriding this because the queue name is baked into the URL and QUERY
 	 * assembles the URL within the baseclass.
 	 */
     protected URL makeURL(String resource) throws MalformedURLException {
 		return super.makeURL(queueId+resource);
 	}
 
 	public static List<MessageQueue> createList(String [] queueUrls, String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server) throws SQSException {
 		ArrayList<MessageQueue> ret = new ArrayList<MessageQueue>();
 		for (int i=0; i<queueUrls.length; i++) {
 			ret.add(new MessageQueue(queueUrls[i], awsAccessKeyId, awsSecretAccessKey, isSecure, server));
 		}
 		return ret;
 	}
 }
