 package org.ebag.runtime.handler;
 
import org.apache.log4j.Logger;
 import org.apache.mina.common.IoSession;
 
 public abstract class BasicHandler<T> {
 	IoSession session;
 	T message;
	Logger log=Logger.getLogger(this.getClass());
 	@SuppressWarnings("unchecked")
 	public BasicHandler(IoSession session,Object message){
 		this.session=session;
 		this.message=(T)message;
 	}
 	
 	public abstract void handle();
 }
