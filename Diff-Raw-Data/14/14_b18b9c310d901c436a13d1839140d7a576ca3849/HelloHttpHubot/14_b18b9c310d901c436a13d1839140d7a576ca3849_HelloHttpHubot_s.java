 /*
  * Copyright (c) Novedia Group 2012.
  *
  *    This file is part of Hubiquitus
  *
  *    Permission is hereby granted, free of charge, to any person obtaining a copy
  *    of this software and associated documentation files (the "Software"), to deal
  *    in the Software without restriction, including without limitation the rights
  *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  *    of the Software, and to permit persons to whom the Software is furnished to do so,
  *    subject to the following conditions:
  *
  *    The above copyright notice and this permission notice shall be included in all copies
  *    or substantial portions of the Software.
  *
  *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  *    INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  *    PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
  *    FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  *    ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  *    You should have received a copy of the MIT License along with Hubiquitus.
  *    If not, see <http://opensource.org/licenses/mit-license.php>.
  */
 
 package org.hubiquitus.HelloHttpBot;
 
 import org.hubiquitus.hapi.exceptions.MissingAttrException;
 import org.hubiquitus.hapi.hStructures.HMessage;
 import org.hubiquitus.hubotsdk.Hubot;
 import org.hubiquitus.hubotsdk.adapters.HHttpAdapter.HHttpData;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 public class HelloHttpHubot extends Hubot{
 
 	final Logger logger = LoggerFactory.getLogger(HelloHttpHubot.class);
 	public static void main(String[] args) throws Exception{
		HelloHttpHubot hubot = new HelloHttpHubot();
 		hubot.start();
 	}
	
 	@Override
 	public void inProcessMessage(HMessage incomingMessage) {
 		logger.info("received message from " + incomingMessage.getAuthor() + " : ");
 		logger.info("---------------------------------------------------------");
 		logger.info(incomingMessage.toString());
 		logger.info("---------------------------------------------------------\n");
 		
 		if (incomingMessage != null && incomingMessage.getType().equals("hHttpData")) {
 			HHttpData httpData = null;
 			try {
 				httpData = new HHttpData(incomingMessage.getPayloadAsJSONObject());
 			} catch (Exception e) {
 				logger.info("message: ",e);
 			}
 			logger.info("the httpdata : ");
 			logger.info("---------------------------------------------------------");
 			logger.info(httpData.toString());
 			logger.info("---------------------------------------------------------");
 		
 			/*****test out box*****/
 			// try to use out box to send the message to http://localhost:8082
 			try {
				incomingMessage.setActor("httpOutbox@domain.com");
 				if (httpData.getServerPort() != 8082){
 					httpData.setServerPort(8082);
 
 					incomingMessage.setPayload(httpData);
 					logger.info("send message to http://" + httpData.getServerName() + ":" + httpData.getServerPort() + httpData.getQueryPath());
 					send(incomingMessage);
 				}
 			} catch (MissingAttrException e) {
 				logger.info("",e);
 			}
 			/**************************/
 		}
 	}
 }
