 package MainPackage {
 	
 	inst RequestBlockingProxy;
 	inst RequestCountingProxy;
 	inst RealSubject;
 	
 	class RequestCounter 		adds implements OutputSubject { }
 	class RequestBlocker  		adds implements OutputSubject { }
 	class OutputImplementation 	adds implements OutputSubject { } 
 	
 	/**
 	 * Implements the driver for the RequestCounter design pattern example.<p> 
 	 *
 	 * Intent: <i>Provide a surrogate or placeholder for another object to control
 	 * access to it.</i><p>
 	 *
 	 * Participating objects are <code>OutputImplementation</code> and 
 	 * <code>RequestCounter</code> as <i>RealSubject</i> and <i>Proxy</i>, 
 	 * respectively.
 	 *  
 	 * Both implement the <code>OutputSubject</code> interface, which represents
 	 * the <i>Subject</i> interface.
 	 *
 	 * Experimental setup:
 	 * <code>Main</code> issues three different kinds of requests to
 	 * the <i>RealSubject</i> (<code>OutputImplementation</code>) twice. 
 	 * <UL>
 	 * 	<LI> SAFE requests are not affected
 	 *  <LI> REGULAR request are counted
 	 *  <LI> UNSAFE requests are blocked entirely.
 	 * </UL>
 	 *
 	 * <p><i>This is the Java version.</i><p> 
 	 *
 	 * <i>Proxy</i>s needs to implement all methods of 
 	 * <code>OutputSubject</code>, even those it is not interested in. 
 	 * They need to be aware of their role in the pattern.
 	 *
 	 * @author  Jan Hannemann
 	 * @author  Gregor Kiczales
 	 * @version 1.1, 02/17/04
 	 */
 	public class Main {   
 
 	    /**
 	     * Implements the driver for the proxy design pattern. <p>
 	     */
 
 		public static void main (String[] args) { 
			OutputSubject real          = new OutputImplementation();
 			OutputSubject countingProxy = new RequestCounter(); // contains the blocking proxy as well
 
 			System.out.println("\n===> Issuing SAFE request...");		
 			countingProxy.safeRequest   ("Safe Reqeust");
 			System.out.println("\n===> Issuing REGULAR request...");		
 			countingProxy.regularRequest("Normal Request");
 			System.out.println("\n===> Issuing UNSAFE request...");		
 			countingProxy.unsafeRequest ("Unsafe Request");
 
 			System.out.println("\n===> Issuing SAFE request...");		
 			countingProxy.safeRequest   ("Safe Reqeust");
 			System.out.println("\n===> Issuing REGULAR request...");		
 			countingProxy.regularRequest("Normal Request");
 			System.out.println("\n===> Issuing UNSAFE request...");		
 			countingProxy.unsafeRequest ("Unsafe Request");
 		}
 	}
 	
 	
 		
 	/**
 	 * Defines the <i>Subject</i> interface that is implemented by both 
 	 * <code>RequestCounter</code> and <code>OutputImplementation</code>.
 	 *
 	 * @author  Jan Hannemann
 	 * @author  Gregor Kiczales
 	 * @version 1.1, 02/17/04
 	 */  
 	public interface OutputSubject { 
 
 	    /**
 	     * A type of <i>request(..)</i>.
 	     *
 	     * @param s the string to print
 	     */
 
 		public void safeRequest(String s);
 
 	    /**
 	     * A type of <i>request(..)</i>.
 	     *
 	     * @param s the string to print
 	     */
 
 		public void regularRequest(String s);
 
 		/**
 	     * A type of <i>request(..)</i>.
 		 *
 		 * @param s the string to print
 		 */
 
 		public void unsafeRequest(String s);
 
 	}
 	
 }
