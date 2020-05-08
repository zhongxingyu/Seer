 package at.ac.tuwien.complang.sbc11.jms;
 
 import javax.jms.Destination;
 
 abstract class JMSCloud 
 {	
 	public JMSCloud()
 	{
 
 	}
 	
 	abstract Object read(Destination dest);
 	abstract void write(Object obj);
 	abstract Object delete(Object obj); // bei einer queue?
 	
 }
