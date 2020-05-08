 package at.ac.tuwien.complang.sbc11.jms;
 
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
 
 abstract class JMSCloud 
 {	
 	public JMSCloud()
 	{
 
 	}
 	
 	abstract Object read(Destination dest);
 	abstract void write(Object obj);
 	abstract Object delete(Object obj); // bei einer queue?
 	
 }
