 package org.jboss.qa;
 
 import javax.naming.InitialContext;
 import javax.rmi.PortableRemoteObject;
 
 
 import org.jboss.qa.iiop.ejb2.Test2;
 import org.jboss.qa.iiop.ejb2.Test2Home;
 
 public class IIOPEJB2Testing {
 	// private static final String BEAN_NAME= "ejbTest2";
 	private static final String BEAN_NAME= "ejbTest2";
 	
 	public static void main(String[] args) throws Throwable {
 		String beanName = (args.length > 1) ? args[1] : BEAN_NAME; 
 		
		Util.presetOrb();
 		
 		InitialContext context = Util.getContext();
 		
         final Object iiopObj = context.lookup(beanName);
         final Test2Home beanHome = (Test2Home) PortableRemoteObject.narrow(iiopObj, Test2Home.class);
         final Test2 result = beanHome.create();
         
         Util.startCorbaTx();
         try {
 	        result.testAccess();
         } catch(Throwable e) {
         	e.printStackTrace();
         	System.out.println("Exception got on call - rollback");
         	Util.rollbackCorbaTx();
         	return;
         }
         
         System.out.println("Well done - commit");
         Util.commitCorbaTx();
 	}
 }
 
