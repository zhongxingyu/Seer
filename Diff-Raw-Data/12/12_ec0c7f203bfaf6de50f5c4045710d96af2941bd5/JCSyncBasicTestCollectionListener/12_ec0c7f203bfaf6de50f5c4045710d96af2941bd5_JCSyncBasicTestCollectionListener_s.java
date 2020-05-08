 package pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic;
 
 import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncAbstractCollection;
 import pl.edu.pjwstk.mteam.jcsync.collections.JCSyncCollectionStateListener;
 import pl.edu.pjwstk.mteam.jcsync.lang.reflect.JCSyncMethod;
 import pl.edu.pjwstk.mteam.p2pm.tests.core.events.EventManager;
 import pl.edu.pjwstk.mteam.p2pm.tests.tests.tests.jcsyncbasic.JCsyncBasicTest;
 
 /**
  *
 * @author pb
  */
 public class JCSyncBasicTestCollectionListener implements JCSyncCollectionStateListener {
 
     @Override
    public void onRemoteStateUpdated(JCSyncAbstractCollection collection, JCSyncMethod details) {
         EventManager.getInstance().addEventToQueue(JCsyncBasicTest.EVENT_JCSYNC_ON_REMOTE_UPDATE, details);
     }
 
     @Override
    public void onLocalStateUpdated(JCSyncAbstractCollection collection, JCSyncMethod details) {
         EventManager.getInstance().addEventToQueue(JCsyncBasicTest.EVENT_JCSYNC_ON_LOCAL_UPDATE, details);
     }
     
}
