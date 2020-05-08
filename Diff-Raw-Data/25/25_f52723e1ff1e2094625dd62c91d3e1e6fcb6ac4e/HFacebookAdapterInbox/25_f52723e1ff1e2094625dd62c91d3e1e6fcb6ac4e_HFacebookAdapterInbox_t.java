 /**
  Copyright (c) Novedia Group 2012.
  This file is part of Hubiquitus
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  of the Software, and to permit persons to whom the Software is furnished to do so,
  subject to the following conditions:
 
  The above copyright notice and this permission notice shall be included in all copies
  or substantial portions of the Software.
 
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
  FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 
  You should have received a copy of the MIT License along with Hubiquitus.
  If not, see <http://opensource.org/licenses/mit-license.php>. 
  
 */
 package org.hubiquitus.hubotsdk.adapters;
 
 import org.hubiquitus.hapi.hStructures.HMessage;
 import org.hubiquitus.hfacebook.publics.FBStatus;
 import org.hubiquitus.hfacebook.publics.GetLikeFacebook;
import org.hubiquitus.hfacebook.publics.HFacebookListener;
 import org.hubiquitus.hubotsdk.AdapterInbox;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
public class HFacebookAdapterInbox extends AdapterInbox implements HFacebookListener{
 
 
 	final Logger log = LoggerFactory.getLogger(HFacebookAdapterInbox.class);
 
 	private String proxyHost;
 	private int    proxyPort;	
 
 	private String pageName;
 	private long roundTime;
 
 	private GetLikeFacebook like;
 
 	@Override
 	public void setProperties(JSONObject properties) {
 
 		if(properties != null){
 			try{
 				if (properties.has("proxyHost")) {
 					this.proxyHost = properties.getString("proxyHost");
 				}				
 				if (properties.has("proxyPort")) {
 					this.proxyPort = properties.getInt("proxyPort");
 				}
 				if (properties.has("pageName")) {
 					this.pageName = properties.getString("pageName");
 				}
 				if (properties.has("roundTime")) {
 					this.roundTime = properties.getLong("roundTime");
 				}
 			}catch(JSONException e){
 				log.error("ERROR ON CONFIG FILE 'JSON NOT VALID', ERROR TYPE : ",e);
 			}
 		}
 		log.info("Properties Initialized : " + this);
 	}
 
 	@Override
 	public void start() {
 		log.info("Starting request...");
 		like = new GetLikeFacebook(proxyHost, proxyPort, pageName, roundTime);
 		like.addListener(this);
 		like.start();
 		log.info("Started request.");
 
 	}
 
 	@Override
 	public void stop() {
 		log.info("Stopping request...");
 		if (like != null)
 			like.stop();
 		log.info("Stopped request.");
 	}
 	
 	
 	@Override
 	public String toString() {
 		return "HFacebookInBox [proxyHost =" + proxyHost 
 				+ ", proxyPort ="      + proxyPort 
 				+ ", pageName ="  + pageName 
 				+ ", roundTime =" + roundTime  + "]";
 	}
 
 	@Override
 	public void onStatus(FBStatus item) {
 		HMessage msg = new HMessage();
 		msg.setType("FBStatus");
 		msg.setPayload(item);
 		put(msg);		
 	}
 }
