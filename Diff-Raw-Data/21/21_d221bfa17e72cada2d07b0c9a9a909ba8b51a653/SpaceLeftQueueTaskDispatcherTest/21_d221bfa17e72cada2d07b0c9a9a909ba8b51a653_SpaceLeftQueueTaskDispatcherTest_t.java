 package hudson.plugins.space_left;
 
 import hudson.model.*;
 import hudson.model.labels.LabelAtom;
 import hudson.model.queue.CauseOfBlockage;
 import hudson.slaves.DumbSlave;
 import hudson.slaves.SlaveComputer;
import jenkins.model.Jenkins;
 import org.junit.Test;
 import org.jvnet.hudson.test.HudsonTestCase;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.Future;
 
 /**
  * Created by IntelliJ IDEA.
  * User: jet
  * Date: 3/6/12
  * Time: 10:42 AM
  * To change this template use File | Settings | File Templates.
  */
 public class SpaceLeftQueueTaskDispatcherTest extends HudsonTestCase {
     @Test
     public void testCanTake() throws Exception {
 
         // init slave
         LabelAtom label = new LabelAtom("label");
         DumbSlave slave = this.createSlave(label);
         SlaveComputer c = slave.getComputer();
         c.connect(false).get(); // wait until it's connected
         if(c.isOffline()) {
             fail("Slave failed to go online: "+c.getLog());
         }
 
         // get free space from empty slave
         SpaceLeftQueueTaskDispatcher spaceLeftQueueTaskDispatcher = new SpaceLeftQueueTaskDispatcher();
 
         // add project to slave
         FreeStyleProject project = this.createFreeStyleProject();
         project.setAssignedLabel(label);
 
         Queue.BuildableItem item = new Queue.BuildableItem(new Queue.WaitingItem(Calendar.getInstance(), project, new ArrayList<Action>()));
 
         CauseOfBlockage causeOfBlockage = spaceLeftQueueTaskDispatcher.canTake(slave, item);
 
         assertNull(causeOfBlockage);
 
        causeOfBlockage = spaceLeftQueueTaskDispatcher.canTake(Jenkins.getInstance(), item);

        assertNull(causeOfBlockage);

         // set required space property on project, that needs lots of space
         SpaceLeftProperty spaceLeftProperty = new SpaceLeftProperty();
         spaceLeftProperty.setRequiredSpace(Long.MAX_VALUE / 4L);
         
         project.addProperty(spaceLeftProperty);
 
         causeOfBlockage = spaceLeftQueueTaskDispatcher.canTake(slave, item);
         
         assertNotNull(causeOfBlockage);
 
         // reset required space property to 1 MB
         spaceLeftProperty.setRequiredSpace(1000000L);
 
         causeOfBlockage = spaceLeftQueueTaskDispatcher.canTake(slave, item);
 
         assertNull(causeOfBlockage);
 
         // take required space of other projects on slave into count
         FreeStyleProject otherProject = this.createFreeStyleProject();
         otherProject.setAssignedLabel(label);
         SpaceLeftProperty otherSpaceLeftProperty = new SpaceLeftProperty();
         otherSpaceLeftProperty.setRequiredSpace(2000000L);
         otherProject.addProperty(otherSpaceLeftProperty);
         FreeStyleBuild b = otherProject.scheduleBuild2(0).get();
 
         assertTrue(b.getWorkspace().exists());
 
         StringParameterValue value = new StringParameterValue("REQUIRED_SPACE", "3000000");
         List<ParameterValue> params = new ArrayList<ParameterValue>();
         params.add(value);
         item.addAction(new ParametersAction(params));
 
         causeOfBlockage = spaceLeftQueueTaskDispatcher.canTake(slave, item);
 
         assertNull(causeOfBlockage);
     }
 
 }
