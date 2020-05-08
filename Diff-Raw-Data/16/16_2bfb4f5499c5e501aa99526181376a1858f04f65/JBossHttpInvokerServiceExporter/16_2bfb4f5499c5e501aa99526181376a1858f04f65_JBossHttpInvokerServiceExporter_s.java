 /**
  * 
  */
 package uk.org.vacuumtube.http;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 
 import org.jboss.serial.io.JBossObjectInputStream;
 import org.jboss.serial.io.JBossObjectOutputStream;
 
 /**
  * @author clivem
  *
  */
 public class JBossHttpInvokerServiceExporter extends CustomHttpInvokerServiceExporter {
 
 	/* (non-Javadoc)
 	 * @see org.springframework.remoting.rmi.RemoteInvocationSerializingExporter#createObjectInputStream(java.io.InputStream)
 	 */
 	@Override
 	protected ObjectInputStream createObjectInputStream(InputStream is)
 			throws IOException {
 		return new JBossObjectInputStream(new BufferedInputStream(is));
 	}
 
 	/* (non-Javadoc)
 	 * @see org.springframework.remoting.rmi.RemoteInvocationSerializingExporter#createObjectOutputStream(java.io.OutputStream)
 	 */
 	@Override
 	protected ObjectOutputStream createObjectOutputStream(OutputStream os)
 			throws IOException {
 		return new JBossObjectOutputStream(new BufferedOutputStream(os));
 	}
 }
