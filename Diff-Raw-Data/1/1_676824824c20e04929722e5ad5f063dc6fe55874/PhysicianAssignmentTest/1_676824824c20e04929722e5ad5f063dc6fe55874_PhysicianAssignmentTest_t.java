 package puf.m2.hms.model;
 
 import static org.easymock.EasyMock.expectLastCall;
 import static org.easymock.EasyMock.replay;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 
 public class PhysicianAssignmentTest {
 
 	PhysicianAssignment mock;
 
 	@Before
 	public void setUp() {
 		mock = EasyMock.createMock(PhysicianAssignment.class);
 	}
 
 	@Test
 	public void testSave() throws Exception {
 		mock.save();
 		expectLastCall();
 		replay(mock);
 	}
 
 }
