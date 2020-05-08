 package example.design.pattern.singleton;
 
 /**
  * 
  * @author gauchy
  *
  */
 public class Singular 
 {
 	//volatile means all write action on the "instance" will be visible to other threads too.where as 
 	//for non volatile variables changes will be visible only once thread is out of sync block
 	//hence if variable is non-volatile and value is set to instance variable by a thread in sync block and it crashes , 
 	//other threads will still assume it as null rather than concrete obj.
 	private volatile Singular instance;
 	private Singular()
 	{
 		
 	}
 	
 	public Singular getInstance()
 	{
 		if(instance == null)
 		{
 			//if synchronized block is not kept then two threads can 
 			//at the same moment enter into outer if and create two different instance
			synchronized (instance) 
 			{
 				//Second null check is required because if two threads have entered
 				//outer if and one thread goes and aquires the lock and creates the object
 				//in that case the other thread shouldn't create another object thus 
 				//having a null check
 				if(instance == null)
 					instance = new Singular();
 			}
 		}
 		return instance;
 	}
 }
