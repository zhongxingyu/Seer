 // Software License Agreement (BSD License)
 //
 // Copyright (c) 2011, Willow Garage, Inc.
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions
 // are met:
 //
 //  * Redistributions of source code must retain the above copyright
 //    notice, this list of conditions and the following disclaimer.
 //  * Redistributions in binary form must reproduce the above
 //    copyright notice, this list of conditions and the following
 //    disclaimer in the documentation and/or other materials provided
 //    with the distribution.
 //  * Neither the name of Willow Garage, Inc. nor the names of its
 //    contributors may be used to endorse or promote products derived
 //    from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 // CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 // LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 // ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 // POSSIBILITY OF SUCH DAMAGE.
 
 package org.ros.rosserial;
 import org.ros.rosserial.*;
 
 
 import java.io.*;
 import java.security.KeyException;
 import java.util.*;
 
 
 import org.ros.exception.RosInitException;
 import org.ros.message.*;
 import org.ros.*;
 
 import org.ros.message.Message;
 
 import org.ros.message.rosserial_msgs.*;
 import org.ros.message.std_msgs.Time;
 
 
 public class RosSerial implements Runnable{
 	//define some modes for the serial communication state machine	
 	
 	
 	private Boolean  running  = false;
 	static Boolean debug = false;
 	
 	Node node;
 	
 	OutputStream ostream;
 	//InputStream istream;
 	BufferedInputStream istream;
 	
 
 	public class ROSTopic {
 		public String name;
 		public String type;
 		public ROSTopic(String name, String type){
 			this.name= name;
 			this.type=type;
 		}
 	}
 	
 	
 	public interface onNegotionListener{
 		public void onNegotiation(ROSTopic topic);
 	}
 	onNegotionListener onSubscriptionCB= null;
 	onNegotionListener onPublicationCB = null;
 
 	//Set Callback function for new subscription
 	void setOnSubscriptionCB(onNegotionListener cb){
 		onSubscriptionCB=cb;
 	}
 	
 	//Set Callback for new publication
 	void setOnPublicationCB(onNegotionListener cb){
 		onPublicationCB = cb;
 	}
 	
 	
 	
 	
 	
 	Hashtable<Integer, Publisher> publishers = new Hashtable();
 	Hashtable<Integer, Subscriber> subscribers= new Hashtable();
 
 	Hashtable<String, Integer  > id_lookup= new Hashtable();
 	Hashtable<Integer, ROSTopic  > topic_lookup = new Hashtable();
 	Hashtable<Integer, Class > msg_classes = new Hashtable();
 
 	static final int TOPIC_PUBLISHERS = 0;
 	static final int TOPIC_SUBSCRIBERS = 1;
 	static final int TOPIC_TIME = 10;
 	
 	enum PARSE_STATE {FLAGA, FLAGB, ID, LENGTH, DATA};
 	PARSE_STATE parse_state;
 	int chk=0;
 	int topic_id;
 
 	
 	public ROSTopic[] getSubscriptions()
 	{
 		ROSTopic[] topics =  new ROSTopic[subscribers.size()];
 		
 		Enumeration<Integer> e = subscribers.keys();
 		
 		for (int i =0; e.hasMoreElements(); i ++){
 			Integer id =  e.nextElement();
 			topics[i]  = topic_lookup.get(id);
 		}
 		return topics;
 	}
 	
 	public ROSTopic[] getPublications()
 	{
 		ROSTopic[] topics =  new ROSTopic[publishers.size()];
 		
 		Enumeration<Integer> e = publishers.keys();
 		
 		for (int i =0; e.hasMoreElements(); i ++){
 			Integer id =  e.nextElement();
 			topics[i]  = topic_lookup.get(id);
 		}
 		return topics;
 	}
 	
 	
 	public RosSerial(Node nh, InputStream input, OutputStream output){
 		node = nh;
 		ostream = output;
 		istream = new BufferedInputStream(input,100);
 		parse_state = PARSE_STATE.FLAGA;
 		requestTopics();
 
 		}
 	
 	private  Class loadMsgClass(String msg) throws ClassNotFoundException{
 		String[] msgParts = msg.split("/");
 		Class aClass = RosSerial.class.getClassLoader().loadClass("org.ros.message."+msgParts[0]+"."+msgParts[1]);
 		System.out.println("Loading Msg Class : " + aClass.getName());
 		return aClass;
 	}
 	
 	private void requestTopics(){
 		byte[] request= {(byte) 0x0FF, (byte) 0x0FF, (byte) 0, (byte) 0,(byte) 0, (byte) 0, (byte)0xff };
 		byte[] flushing = new byte[50];
 		for(int i =0; i<50; i++) flushing[i]=0;
 		try{
 			ostream.write(flushing);
 			Thread.sleep(200);
 			ostream.write(request);
 			ostream.flush();
 			Thread.sleep(500);
 
 
 		}
 		catch(Exception e){
 		System.out.println("Failed sending topic request : " +e.toString());
 		e.printStackTrace();
 		}
 	}
 	
 	private boolean addTopic(String topic, String topic_type, int id, boolean Publisher){
 		if ( topic.equals(topic_lookup.get(id)) ) return true;
 		System.out.println("Adding topic " + topic + " of type " + topic_type +" : " + id);
 
 		try{
 			Class msg_class = loadMsgClass(topic_type);
 			msg_classes.put(id, msg_class);
 			
 			id_lookup.put(topic, id);
 			
 			ROSTopic t = new ROSTopic(topic, topic_type);
 			topic_lookup.put(id, t);
 			
 			if (Publisher){
 				Publisher pub = node.createPublisher(topic, topic_type);
 				publishers.put(id,pub);
 			}
 			else{
 				Subscriber sub = node.createSubscriber(topic, topic_type, new MessageListenerRosSerial(this, id));
 				subscribers.put(id,sub);
 			}	
 			return true;
 		}
 		catch (Exception e){
 			e.printStackTrace();
 			}
 		return false;
 	}
 
 
 	
 	void resetParseStateMachine(){
 		parse_state  = PARSE_STATE.FLAGA;
 		chk=0;
 	}
 	
 	private int parseData(byte[] buff ) throws InstantiationException, IllegalAccessException{
 		switch(parse_state){
 			case FLAGA:
 				if (buff[0] == (byte) 0xff  ) {
 					parse_state = PARSE_STATE.FLAGB;
 				}
 				return 1;
 			case FLAGB:
 				if (buff[0] == (byte) 0xff  ) {
 					parse_state = PARSE_STATE.ID;
 					return 2;
 				}
 				else{
 					resetParseStateMachine();
 					return 1;
 				}
 			case ID:
 				//Read topic id and add it to the checksum
 				//little endian
				chk += ( 0xff & buff[0]); chk+= (int)( 0xff & buff[1]);
 				topic_id = (int)(buff[1] <<  8) | (int)(buff[0]);
 				parse_state = PARSE_STATE.LENGTH;
 				return 2;
 			case LENGTH:
				chk += ( 0xff & buff[0]); chk+= (int)( 0xff & buff[1]);
 				int l_data = (int)(buff[1] <<  8) | (int)(buff[0]);
 				parse_state = PARSE_STATE.DATA;
 				return l_data +1; //plus one for checksum
 			case DATA:
				StringBuilder sb = new StringBuilder(buff.length*6);

				for(int i =0; i <buff.length; i++) sb.append( Integer.toString((int) buff[i] & 0xff) + " ");
				//System.out.println("The data : " + sb.toString());
				for(int i =0; i< buff.length; i++) chk += ( 0xff & buff[i]); 

 				if (chk%256 == 255){ //valid checksum
 					resetParseStateMachine();
 					switch(topic_id){
 						case TOPIC_PUBLISHERS:
 							{
 							TopicInfo m =  new TopicInfo();
 							m.deserialize(buff);
 							addTopic(m.topic_name, m.message_type, m.topic_id, true);
 							break;
 							}
 						case TOPIC_SUBSCRIBERS:
 							{
 							TopicInfo m =  new TopicInfo();
 							m.deserialize(buff);
 							addTopic(m.topic_name, m.message_type, m.topic_id, false);
 							break;
 							}
 						case TOPIC_TIME:
 							org.ros.message.Time t = node.getCurrentTime();
 							org.ros.message.std_msgs.Time t_msg = new org.ros.message.std_msgs.Time();
 							t_msg.data = t;
 							send(TOPIC_TIME,t_msg);
 							break;
 						default:
       						Message msg = (Message) msg_classes.get(topic_id).newInstance();
 							msg.deserialize(buff);
 							publishers.get(topic_id).publish(msg);
 							break;
 					}
 				}
 				else{
					System.out.println("Checksum failed! chk = "+Integer.toString(chk%256));

 					resetParseStateMachine();
 				}
 		}
 		
 		return 1;
 	}
 	
 	public void run(){
 
 	    running = true;
 
 		requestTopics();
 	    System.out.println("Topics requested");
 
 	    byte buff[] = new byte[200];
 	    int len_requested=1;
 	    int len_data=0;
 	    
 		while (running){
 			try{
 		    	int b = istream.read(); 
 		    	if (b!= -1){
 		    		buff[len_data] = (byte) (0xff & b) ;
 		    		++len_data;
 		    	}
 				if (len_data >= len_requested){
 					len_requested = parseData(buff);
 					len_data = 0;
 					buff = new byte[len_requested];
 				}
 				Thread.sleep(1);
 			}
 			catch(Exception e ){
 				e.printStackTrace();
 			}
 			
 		}
 	}
 	
 	public void shutdown(){
 		synchronized (running) {
 			running = false;
 		}
 	}
 	
 	public void send(int id, org.ros.message.Message t){
 		
 		int l = t.serializationLength();
 		
 		byte[] packet_header = new byte[2+2+2]; //sync_flags + topic_id + data_len 
 		packet_header[0] = (byte) 0xff;
 		packet_header[1] = (byte) 0xff;
 		packet_header[2] = (byte) id;
 		packet_header[3] = (byte) (id >>8);
 		packet_header[4] = (byte) l;
 		packet_header[5] = (byte) (l >> 8);
 		
 		byte[] data = t.serialize(0);
 		
 		int chk = 0;
 		for (int i=2; i<6; i++) chk+= packet_header[i];
 		for(int i=0;  i<l; i++) chk+= data[i];
 		chk = 255-chk%256;
 		try{
 			ostream.write(packet_header);
 			ostream.write(data);
 			ostream.write(chk);
 		}catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 }
