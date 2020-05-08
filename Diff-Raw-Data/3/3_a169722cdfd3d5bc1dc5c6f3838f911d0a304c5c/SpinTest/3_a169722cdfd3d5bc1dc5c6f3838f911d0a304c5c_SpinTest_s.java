 package siver.cox.actions;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import repast.simphony.space.continuous.ContinuousSpace;
 import repast.simphony.space.continuous.NdPoint;
 import siver.cox.actions.Spin;
 import siver.river.River;
 import siver.river.lane.LaneTest;
 
 public class SpinTest extends ActionTest {
 	private River r;
 	@Override
 	protected String className() {
 		return Spin.class.getName();
 	}
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 	
 	private void mockSpinSetup() {
 		r = LaneTest.setupRiver();
 		expect(mockBoat.getRiver()).andStubReturn(r);
 		expect(mockBoat.getLocation()).andReturn(new NdPoint(10,30)).times(8);
 		expect(mockBoat.getAngle()).andReturn(0.0).once();
 		expect(mockLocation.headingUpstream()).andReturn(true).times(2);
		expect(mockLocation.getEdge()).andReturn(r.upstream_lane().edgeNearest(new NdPoint(10,30))).once();
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testExecute() {
 		runExecute(1);
 	}
 	
 	@Test
 	public void testFinalExecute() {		
 		runExecute(59);
 		
 		reset(mockCox);
 		reset(mockBoat);
 		reset(mockLocation);
 		
 		mockBoat.deadStop();
 		expectLastCall().once();
 		
 		mockBoat.moveTo(new NdPoint(10,10));
 		expectLastCall().once();
 		
 		mockLocation.toggleUpstream();
 		expectLastCall().once();
 		
 		mockLocation.updateEdge(r.downstream_lane().getNextEdge(r.downstream_lane().getStartNode(), false));
 		expectLastCall().once();
 		
 		
 		mockCox.clearAction();
 		expectLastCall().once();
 		
 		executeWithMocks();
 	}
 
 	
 	private void runExecute(int runs) {
 		mockSpinSetup();
 		mockBoat.deadStop();
 		expectLastCall().times(runs);
 		
 		ContinuousSpace<Object> mockSpace = createMock(ContinuousSpace.class);
 		
 		expect(mockBoat.getSpace()).andStubReturn(mockSpace);
 		
 		
 		for(int i = 1; i<= runs; i++) {
 			mockBoat.setAngle(eq(i*Math.PI/60.0, 1E-5));
 			expectLastCall().once();
 			
 			NdPoint boatLoc = new NdPoint(10,30-(i*20.0/60.0));
 			expect(mockBoat.getLocation()).andReturn(boatLoc).once();
 			
 			expect(mockSpace.getDisplacement(boatLoc, new NdPoint(10,10))).andReturn(new double[]{0,-20+(i*20.0/60.0)}).once();
 		}
 		
 		mockBoat.move(eq(20.0/60.0, 1E-5), eq(-Math.PI/2.0, 1E-5));
 		expectLastCall().times(runs);
 		
 		replay(mockSpace);
 		replay(mockLocation);
 		replay(mockBoat);
 		replay(mockCox);
 		for(int i = 1; i<= runs; i++) {
 			
 			action.execute();
 		}
 		verify(mockCox);
 		verify(mockBoat);
 		verify(mockLocation);
 		verify(mockSpace);
 	}
 
 	@Test
 	public void testExecuteNoDestinationEdge() {
 		//nowhere to spin to
 		r = LaneTest.setupRiver();
 		expect(mockBoat.getRiver()).andStubReturn(r);
 		expect(mockBoat.getLocation()).andReturn(new NdPoint(5,30)).times(1);
 		expect(mockLocation.headingUpstream()).andReturn(false).times(2);
		expect(mockLocation.getEdge()).andReturn(r.upstream_lane().edgeNearest(new NdPoint(5,30))).once();
 		
 		mockCox.clearAction();
 		expectLastCall().once();
 		
 		replay(mockLocation);
 		replay(mockBoat);
 		replay(mockCox);
 		action.execute();
 		verify(mockCox);
 		verify(mockBoat);
 		verify(mockLocation);
 	}
 	
 }
