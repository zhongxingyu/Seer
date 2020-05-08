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
 
 package com.xerox.amazonws.sqs2;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
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
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 
 import com.xerox.amazonws.common.AWSException;
 import com.xerox.amazonws.common.AWSQueryConnection;
 import com.xerox.amazonws.typica.sqs2.jaxb.Attribute;
 import com.xerox.amazonws.typica.sqs2.jaxb.DeleteMessageResponse;
 import com.xerox.amazonws.typica.sqs2.jaxb.DeleteQueueResponse;
 import com.xerox.amazonws.typica.sqs2.jaxb.GetQueueAttributesResponse;
 import com.xerox.amazonws.typica.sqs2.jaxb.ReceiveMessageResponse;
 import com.xerox.amazonws.typica.sqs2.jaxb.SendMessageResponse;
 import com.xerox.amazonws.typica.sqs2.jaxb.SetQueueAttributesResponse;
 
 /**
  * This class provides an interface with the Amazon SQS message queue. It provides methods
  * for sending / receiving messages and deleting queues and messsages on queues.
  *
  * @author D. Kavanagh
  * @author developer@dotech.com
  */
 public class MessageQueue extends AWSQueryConnection {
     public static final int MAX_MESSAGES = 600;
     public static final int MAX_MESSAGE_BODIES_SIZE = 4096;
 
 	protected String queueId;
 	private boolean enableEncoding = true;
 
     protected MessageQueue(String queueUrl, String awsAccessId,
 							String awsSecretKey, boolean isSecure,
 							String server) throws SQSException {
         super(awsAccessId, awsSecretKey, isSecure, server, isSecure ? 443 : 80);
 		if (queueUrl.startsWith("http")) {
 			queueId = queueUrl.substring(queueUrl.indexOf("//")+2);
 		}
 		else {
 			queueId = queueUrl;	// this is the case where the queue is created from a
 								// fully qualified queue name, not a full queue URL
 		}
 		queueId = queueId.substring(queueId.indexOf("/")+1);
 		QueueService.setVersionHeader(this);
     }
 
 	/**
 	 * This method provides the URL for the message queue represented by this object.
 	 *
 	 * @return generated queue service url
 	 */
 	public URL getUrl() {
 		try {
 			return new URL(super.getUrl().toString());
 		} catch (MalformedURLException ex) {
 			return null;
 		}
 	}
 
 	/**
 	 * This method returns the state of the base64 encoding flag. By default, all messages
 	 * are encoded on send and decoded on receive.
 	 *
 	 * @return state of encoding flag
 	 */
 	public boolean isEncoding() {
 		return enableEncoding;
 	}
 
 	/**
 	 * This method sets the state of the encoding flag. Use this to override the default and
 	 * turn off automatic base64 encoding.
 	 *
 	 * @param enable the new state of the encoding flag
 	 */
 	public void setEncoding(boolean enable) {
 		enableEncoding = enable;
 	}
 
 	/**
 	 * Sends a message to a specified queue. The message must be between 1 and 256K bytes long.
 	 *
 	 * @param msg the message to be sent
 	 */
     public String sendMessage(String msg) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		String encodedMsg = enableEncoding?new String(Base64.encodeBase64(msg.getBytes())):msg;
 		params.put("MessageBody", encodedMsg);
 		PostMethod method = new PostMethod();
 		try {
 			SendMessageResponse response =
 					makeRequestInt(method, "SendMessage", params, SendMessageResponse.class);
 			return response.getSendMessageResult().getMessageId();
 		} finally {
 			method.releaseConnection();
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
 	 * @param numMessages the maximum number of messages to return
 	 * @param visibilityTimeout the duration (in seconds) the retrieved message is hidden from
 	 *                          subsequent calls to retrieve.
 	 * @return an array of message objects
 	 */
     protected Message[] receiveMessages(BigInteger numMessages, BigInteger visibilityTimeout) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		if (numMessages != null) {
 			params.put("MaxNumberOfMessages", numMessages.toString());
 		}
 		if (visibilityTimeout != null) {
 			params.put("VisibilityTimeout", visibilityTimeout.toString());
 		}
 		GetMethod method = new GetMethod();
 		try {
 			ReceiveMessageResponse response =
 					makeRequestInt(method, "ReceiveMessage", params, ReceiveMessageResponse.class);
 			if (response.getReceiveMessageResult().getMessages() == null) {
 				return new Message[0];
 			}
 			else {
 				ArrayList<Message> msgs = new ArrayList();
 				for (com.xerox.amazonws.typica.sqs2.jaxb.Message msg : response.getReceiveMessageResult().getMessages()) {
 					String decodedMsg = enableEncoding?
 								new String(Base64.decodeBase64(msg.getBody().getBytes())):
 											msg.getBody();
 					msgs.add(new Message(msg.getMessageId(), msg.getReceiptHandle(), decodedMsg, msg.getMD5OfBody()));
 				}
 				return msgs.toArray(new Message [msgs.size()]);
 			}
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Deletes the message identified by message object on the queue this object represents.
 	 *
 	 * @param msg the message to be deleted
 	 */
     public void deleteMessage(Message msg) throws SQSException {
 		deleteMessage(msg.getReceiptHandle());
 	}
 
 	/**
 	 * Deletes the message identified by receiptHandle on the queue this object represents.
 	 *
 	 * @param receiptHandle the handle of the message to be deleted
 	 */
     public void deleteMessage(String receiptHandle) throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ReceiptHandle", receiptHandle);
 		GetMethod method = new GetMethod();
 		try {
 			//DeleteMessageResponse response =
 			makeRequestInt(method, "DeleteMessage", params, DeleteMessageResponse.class);
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Deletes the message queue represented by this object. Will delete non-empty queue.
 	 */
     public void deleteQueue() throws SQSException {
 		Map<String, String> params = new HashMap<String, String>();
 		GetMethod method = new GetMethod();
 		try {
 			//DeleteQueueResponse response =
 			makeRequestInt(method, "DeleteQueue", params, DeleteQueueResponse.class);
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Gets the visibility timeout for the queue. Uses {@link #getQueueAttributes(QueueAttribute)}.
 	 */
     public int getVisibilityTimeout() throws SQSException {
 		return Integer.parseInt(getQueueAttributes(QueueAttribute.VISIBILITY_TIMEOUT)
 										.values().iterator().next());
 	}
 
 	/**
 	 * Gets the visibility timeout for the queue. Uses {@link #getQueueAttributes(QueueAttribute)}.
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
 		params.put("AttributeName", qAttr.queryAttribute());
 		GetMethod method = new GetMethod();
 		try {
 			GetQueueAttributesResponse response =
 					makeRequestInt(method, "GetQueueAttributes", params, GetQueueAttributesResponse.class);
 			Map<String,String> ret = new HashMap<String,String>();
 			List<Attribute> attrs = response.getGetQueueAttributesResult().getAttributes();
 			for (Attribute attr : attrs) {
 				ret.put(attr.getName(), attr.getValue());
 			}
 			return ret;
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Sets the visibility timeout of the queue. Uses {@link #setQueueAttribute(String, String)}.
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
 		params.put("Attribute.Name", attribute);
 		params.put("Attribute.Value", value);
 		GetMethod method = new GetMethod();
 		try {
 			//SetQueueAttributesResponse response =
 			makeRequestInt(method, "SetQueueAttributes", params, SetQueueAttributesResponse.class);
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Overriding this because the queue name is baked into the URL and QUERY
 	 * assembles the URL within the baseclass.
 	 */
 	protected URL makeURL(String resource) throws MalformedURLException {
 		return super.makeURL(queueId+resource);
 	}
 
 	protected <T> T makeRequestInt(HttpMethodBase method, String action, Map<String, String> params, Class<T> respType)
 		throws SQSException {
 		try {
 			return makeRequest(method, action, params, respType);
 		} catch (AWSException ex) {
 			throw new SQSException(ex);
 		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
 		} catch (HttpException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new SQSException(ex.getMessage(), ex);
 		}
 	}
 
 	public static List<MessageQueue> createList(String [] queueUrls, String awsAccessId,
 								String awsSecretKey, boolean isSecure, String server, HttpClient hc)
 			throws SQSException {
 		ArrayList<MessageQueue> ret = new ArrayList<MessageQueue>();
 		for (int i=0; i<queueUrls.length; i++) {
 			MessageQueue mq = new MessageQueue(queueUrls[i], awsAccessId, awsSecretKey, isSecure, server);
 			mq.setHttpClient(hc);
 			ret.add(mq);
 		}
 		return ret;
 	}
 }
