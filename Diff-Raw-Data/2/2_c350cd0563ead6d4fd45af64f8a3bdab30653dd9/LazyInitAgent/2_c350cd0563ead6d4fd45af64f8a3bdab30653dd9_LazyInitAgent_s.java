 package awesome.persistence.agent;
 
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.instrument.IllegalClassFormatException;
 import java.security.ProtectionDomain;
 
import com.sun.xml.internal.ws.org.objectweb.asm.ClassReader;
 
 
 
 public class LazyInitAgent implements ClassFileTransformer {
 
 	@Override
 	public byte[] transform(ClassLoader loader, String className,
 			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
 			byte[] bytes) throws IllegalClassFormatException {
 		try {
 			ClassReader creader = new ClassReader(bytes);
 			//Method
 			//creader.accept(visitor, 0);
 			
 		} finally {
 			
 		}
 		
 		
 		return null;
 	}
 
 }
