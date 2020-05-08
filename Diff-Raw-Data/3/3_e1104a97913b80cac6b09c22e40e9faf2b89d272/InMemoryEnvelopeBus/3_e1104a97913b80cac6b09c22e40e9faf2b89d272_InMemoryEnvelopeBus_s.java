 package cmf.bus.support;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import cmf.bus.Envelope;
 import cmf.bus.IEnvelopeBus;
 import cmf.bus.IRegistration;
 
 public class InMemoryEnvelopeBus implements IEnvelopeBus {
 
     protected List<IRegistration> registrationList;
 
     public InMemoryEnvelopeBus() {
         registrationList = new LinkedList<IRegistration>();
     }
 
     protected void dispatch(
     		final Envelope envelope, 
     		final List<IRegistration> registrationList) {
         new Thread() {
 
             @Override
             public void run() {
                 for (IRegistration registration : registrationList) {
                     try {
                         registration.handle(envelope);
                     } catch (Exception e) {
                         try {
 							registration.handleFailed(envelope, e);
 						} catch (Exception failedToFail) {
 							failedToFail.printStackTrace();
 						}
                     }
                 }
             }
         }.run();
     }
 
     @Override
     public synchronized void register(IRegistration registration) {
         registrationList.add(registration);
     }
 
     @Override
     public synchronized void send(Envelope envelope) {
         List<IRegistration> registrations = new LinkedList<IRegistration>();
         for (IRegistration registration : registrationList) {
         		
         		if (null != registration.getFilterPredicate() 
        			&& registration.getFilterPredicate().filter(envelope)){
                    registrations.add(registration);
         		} else {
         			registrations.add(registration);
         		}
         }
         dispatch(envelope, registrations);
     }
 
     @Override
     public synchronized void unregister(IRegistration registration) {
         registrationList.remove(registration);
     }
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 }
