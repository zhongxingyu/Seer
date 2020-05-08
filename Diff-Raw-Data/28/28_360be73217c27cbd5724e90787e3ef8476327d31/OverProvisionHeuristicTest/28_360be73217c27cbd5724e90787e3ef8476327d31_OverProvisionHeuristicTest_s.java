 package planning.heuristic;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import commons.cloud.MachineType;
 import commons.cloud.Request;
 import commons.config.Configuration;
 import commons.config.PropertiesTesting;
 import commons.io.GEISTWorkloadParser;
 import commons.io.TimeBasedWorkloadParser;
 import commons.sim.util.SaaSAppProperties;
 import commons.sim.util.SimulatorProperties;
 
 @RunWith(PowerMockRunner.class)
 public class OverProvisionHeuristicTest {
 	
 	@Test
 	@PrepareForTest({OverProvisionHeuristic.class, Configuration.class})
 	public void testFindPlanWithWorkloadWithEmptyWorkload() throws Exception{
 		long sla = 8000l;
 		
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(3);
 		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.VALID_WORKLOAD_2});
 		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(sla);
 		EasyMock.expect(config.getString(SimulatorProperties.PLANNING_TYPE)).andReturn("SMALL");
 	
 		TimeBasedWorkloadParser parser = PowerMock.createStrictMockAndExpectNew(TimeBasedWorkloadParser.class, EasyMock.isA(GEISTWorkloadParser.class), EasyMock.anyLong());
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(new ArrayList<Request>());
 		EasyMock.expect(parser.hasNext()).andReturn(false);
 		PowerMock.replay(TimeBasedWorkloadParser.class);
 		
 		PowerMock.replayAll(config, parser);
 		
 		OverProvisionHeuristic heuristic = new OverProvisionHeuristic();
 		heuristic.findPlan(null, null, null);
 		
 		Map<MachineType, Integer> plan = heuristic.getPlan(null);
 		assertNotNull(plan);
 		assertEquals(1, plan.size());
 		int value = plan.get(MachineType.SMALL);
 		assertEquals(0, value);
 		
 		PowerMock.verifyAll();
 	}
 	
 	@Test
 	@PrepareForTest({OverProvisionHeuristic.class, Configuration.class})
 	public void testFindPlanWithWorkloadWithUniqueUsersAndDifferentNumberOfRequests() throws Exception{
 		long sla = 8000l;
 		
 		Request request = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request.getUserID()).andReturn("1");
 		Request request2 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request2.getUserID()).andReturn("2");
 		Request request3 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request3.getUserID()).andReturn("3");
 		Request request4 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request4.getUserID()).andReturn("4");
 		Request request5 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request5.getUserID()).andReturn("5");
 		Request request6 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request6.getUserID()).andReturn("6");
 		Request request7 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request7.getUserID()).andReturn("7");
 		Request request8 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request8.getUserID()).andReturn("8");
 		Request request9 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request9.getUserID()).andReturn("9");
 		
 		List<Request> firstIntervalRequests = new ArrayList<Request>();
 		firstIntervalRequests.add(request);
 		firstIntervalRequests.add(request2);
 		firstIntervalRequests.add(request3);
 		firstIntervalRequests.add(request4);
 		
 		List<Request> secondIntervalRequests = new ArrayList<Request>();
 		secondIntervalRequests.add(request5);
 		secondIntervalRequests.add(request6);
 		secondIntervalRequests.add(request7);
 		secondIntervalRequests.add(request8);
 		secondIntervalRequests.add(request9);
 		
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(3);
 		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.VALID_WORKLOAD_2});
 		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(sla);
 		EasyMock.expect(config.getString(SimulatorProperties.PLANNING_TYPE)).andReturn("SMALL");
 		
 		TimeBasedWorkloadParser parser = PowerMock.createStrictMockAndExpectNew(TimeBasedWorkloadParser.class, EasyMock.isA(GEISTWorkloadParser.class), EasyMock.anyLong());
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(firstIntervalRequests);
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(secondIntervalRequests);
 		EasyMock.expect(parser.hasNext()).andReturn(false);
 		PowerMock.replay(TimeBasedWorkloadParser.class);
 		
 		PowerMock.replayAll(parser, config, request, request2, request3, request4, request5, request6, request7, request8, request9);
 		
 		OverProvisionHeuristic heuristic = new OverProvisionHeuristic();
 		heuristic.findPlan(null, null, null);
 		
 		Map<MachineType, Integer> plan = heuristic.getPlan(null);
 		assertNotNull(plan);
 		assertEquals(1, plan.size());
 		int value = plan.get(MachineType.SMALL);
		assertEquals(5, value);
 		
 		PowerMock.verifyAll();
 	}
 	
 	@Test
 	@PrepareForTest({OverProvisionHeuristic.class, Configuration.class})
 	public void testFindPlanWithWorkloadWithRepeatedUsersAndDifferentNumberOfRequests() throws Exception{
 		long sla = 8000l;
 		
 		Request request = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request.getUserID()).andReturn("1");
 		Request request2 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request2.getUserID()).andReturn("2");
 		Request request3 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request3.getUserID()).andReturn("3");
 		Request request4 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request4.getUserID()).andReturn("4");
 		Request request5 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request5.getUserID()).andReturn("5");
 		Request request6 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request6.getUserID()).andReturn("5");
 		Request request7 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request7.getUserID()).andReturn("7");
 		Request request8 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request8.getUserID()).andReturn("8");
 		Request request9 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request9.getUserID()).andReturn("9");
 		
 		List<Request> firstIntervalRequests = new ArrayList<Request>();
 		firstIntervalRequests.add(request);
 		firstIntervalRequests.add(request2);
 		firstIntervalRequests.add(request3);
 		firstIntervalRequests.add(request4);
 		
 		List<Request> secondIntervalRequests = new ArrayList<Request>();
 		secondIntervalRequests.add(request5);
 		secondIntervalRequests.add(request6);
 		secondIntervalRequests.add(request7);
 		secondIntervalRequests.add(request8);
 		secondIntervalRequests.add(request9);
 		
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(3);
 		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.VALID_WORKLOAD_2});
 		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(sla);
 		EasyMock.expect(config.getString(SimulatorProperties.PLANNING_TYPE)).andReturn("SMALL");
 		
 		TimeBasedWorkloadParser parser = PowerMock.createStrictMockAndExpectNew(TimeBasedWorkloadParser.class, EasyMock.isA(GEISTWorkloadParser.class), EasyMock.anyLong());
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(firstIntervalRequests);
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(secondIntervalRequests);
 		EasyMock.expect(parser.hasNext()).andReturn(false);
 		PowerMock.replay(TimeBasedWorkloadParser.class);
 		
 		PowerMock.replayAll(parser, config, request, request2, request3, request4, request5, request6, request7, request8, request9);
 		
 		OverProvisionHeuristic heuristic = new OverProvisionHeuristic();
 		heuristic.findPlan(null, null, null);
 		
 		Map<MachineType, Integer> plan = heuristic.getPlan(null);
 		assertNotNull(plan);
 		assertEquals(1, plan.size());
 		int value = plan.get(MachineType.SMALL);
		assertEquals(4, value);
 		
 		PowerMock.verifyAll();
 	}
 	
 	@Test
 	@PrepareForTest({OverProvisionHeuristic.class, Configuration.class})
 	public void testFindPlanWithWorkloadWithRepeatedUsersAndDifferentNumberOfRequests2() throws Exception{
 		long sla = 8000l;
 		
 		Request request = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request.getUserID()).andReturn("1");
 		Request request2 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request2.getUserID()).andReturn("1");
 		Request request3 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request3.getUserID()).andReturn("1");
 		Request request4 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request4.getUserID()).andReturn("1");
 		Request request5 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request5.getUserID()).andReturn("5");
 		Request request6 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request6.getUserID()).andReturn("5");
 		Request request7 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request7.getUserID()).andReturn("5");
 		Request request8 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request8.getUserID()).andReturn("9");
 		Request request9 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request9.getUserID()).andReturn("9");
 		
 		List<Request> firstIntervalRequests = new ArrayList<Request>();
 		firstIntervalRequests.add(request);
 		firstIntervalRequests.add(request2);
 		firstIntervalRequests.add(request3);
 		firstIntervalRequests.add(request4);
 		
 		List<Request> secondIntervalRequests = new ArrayList<Request>();
 		secondIntervalRequests.add(request5);
 		secondIntervalRequests.add(request6);
 		secondIntervalRequests.add(request7);
 		secondIntervalRequests.add(request8);
 		secondIntervalRequests.add(request9);
 		
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(3);
 		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.VALID_WORKLOAD_2});
 		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(sla);
 		EasyMock.expect(config.getString(SimulatorProperties.PLANNING_TYPE)).andReturn("SMALL");
 		
 		TimeBasedWorkloadParser parser = PowerMock.createStrictMockAndExpectNew(TimeBasedWorkloadParser.class, EasyMock.isA(GEISTWorkloadParser.class), EasyMock.anyLong());
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(firstIntervalRequests);
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(secondIntervalRequests);
 		EasyMock.expect(parser.hasNext()).andReturn(false);
 		PowerMock.replay(TimeBasedWorkloadParser.class);
 		
 		PowerMock.replayAll(parser, config, request, request2, request3, request4, request5, request6, request7, request8, request9);
 		
 		OverProvisionHeuristic heuristic = new OverProvisionHeuristic();
 		heuristic.findPlan(null, null, null);
 		
 		Map<MachineType, Integer> plan = heuristic.getPlan(null);
 		assertNotNull(plan);
 		assertEquals(1, plan.size());
 		int value = plan.get(MachineType.SMALL);
		assertEquals(2, value);
 		
 		PowerMock.verifyAll();
 	}
 
 	@Test
 	@PrepareForTest({OverProvisionHeuristic.class, Configuration.class})
 	public void testFindPlanWithWorkloadWithUniqueUsersAndSameNumberOfRequests() throws Exception{
 		long sla = 8000l;
 		
 		Request request = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request.getUserID()).andReturn("1");
 		Request request2 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request2.getUserID()).andReturn("2");
 		Request request3 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request3.getUserID()).andReturn("3");
 		Request request4 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request4.getUserID()).andReturn("4");
 		Request request5 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request5.getUserID()).andReturn("5");
 		Request request6 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request6.getUserID()).andReturn("6");
 		Request request7 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request7.getUserID()).andReturn("7");
 		Request request8 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request8.getUserID()).andReturn("8");
 		
 		List<Request> firstIntervalRequests = new ArrayList<Request>();
 		firstIntervalRequests.add(request);
 		firstIntervalRequests.add(request2);
 		firstIntervalRequests.add(request3);
 		firstIntervalRequests.add(request4);
 		
 		List<Request> secondIntervalRequests = new ArrayList<Request>();
 		secondIntervalRequests.add(request5);
 		secondIntervalRequests.add(request6);
 		secondIntervalRequests.add(request7);
 		secondIntervalRequests.add(request8);
 		
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(3);
 		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.VALID_WORKLOAD_2});
 		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(sla);
 		EasyMock.expect(config.getString(SimulatorProperties.PLANNING_TYPE)).andReturn("SMALL");
 		
 		TimeBasedWorkloadParser parser = PowerMock.createStrictMockAndExpectNew(TimeBasedWorkloadParser.class, EasyMock.isA(GEISTWorkloadParser.class), EasyMock.anyLong());
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(firstIntervalRequests);
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(secondIntervalRequests);
 		EasyMock.expect(parser.hasNext()).andReturn(false);
 		PowerMock.replay(TimeBasedWorkloadParser.class);
 		
 		PowerMock.replayAll(parser, config, request, request2, request3, request4, request5, request6, request7, request8);
 		
 		OverProvisionHeuristic heuristic = new OverProvisionHeuristic();
 		heuristic.findPlan(null, null, null);
 		
 		Map<MachineType, Integer> plan = heuristic.getPlan(null);
 		assertNotNull(plan);
 		assertEquals(1, plan.size());
 		int value = plan.get(MachineType.SMALL);
		assertEquals(4, value);
 		
 		PowerMock.verifyAll();
 	}
 	
 	@Test
 	@PrepareForTest({OverProvisionHeuristic.class, Configuration.class})
 	public void testFindPlanWithWorkloadWithRepeatedUsersAndSameNumberOfRequests() throws Exception{
 		long sla = 8000l;
 		
 		Request request = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request.getUserID()).andReturn("1");
 		Request request2 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request2.getUserID()).andReturn("2");
 		Request request3 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request3.getUserID()).andReturn("3");
 		Request request4 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request4.getUserID()).andReturn("4");
 		Request request5 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request5.getUserID()).andReturn("5");
 		Request request6 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request6.getUserID()).andReturn("6");
 		Request request7 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request7.getUserID()).andReturn("6");
 		Request request8 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request8.getUserID()).andReturn("8");
 		
 		List<Request> firstIntervalRequests = new ArrayList<Request>();
 		firstIntervalRequests.add(request);
 		firstIntervalRequests.add(request2);
 		firstIntervalRequests.add(request3);
 		firstIntervalRequests.add(request4);
 		
 		List<Request> secondIntervalRequests = new ArrayList<Request>();
 		secondIntervalRequests.add(request5);
 		secondIntervalRequests.add(request6);
 		secondIntervalRequests.add(request7);
 		secondIntervalRequests.add(request8);
 		
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(3);
 		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.VALID_WORKLOAD_2});
 		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(sla);
 		EasyMock.expect(config.getString(SimulatorProperties.PLANNING_TYPE)).andReturn("SMALL");
 		
 		TimeBasedWorkloadParser parser = PowerMock.createStrictMockAndExpectNew(TimeBasedWorkloadParser.class, EasyMock.isA(GEISTWorkloadParser.class), EasyMock.anyLong());
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(firstIntervalRequests);
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(secondIntervalRequests);
 		EasyMock.expect(parser.hasNext()).andReturn(false);
 		PowerMock.replay(TimeBasedWorkloadParser.class);
 		
 		PowerMock.replayAll(parser, config, request, request2, request3, request4, request5, request6, request7, request8);
 		
 		OverProvisionHeuristic heuristic = new OverProvisionHeuristic();
 		heuristic.findPlan(null, null, null);
 		
 		Map<MachineType, Integer> plan = heuristic.getPlan(null);
 		assertNotNull(plan);
 		assertEquals(1, plan.size());
 		int value = plan.get(MachineType.SMALL);
		assertEquals(4, value);
 		
 		PowerMock.verifyAll();
 	}
 	
 	@Test
 	@PrepareForTest({OverProvisionHeuristic.class, Configuration.class})
 	public void testFindPlanWithWorkloadWithRepeatedUsersAndSametNumberOfRequests2() throws Exception{
 		long sla = 8000l;
 		
 		Request request = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request.getUserID()).andReturn("1");
 		Request request2 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request2.getUserID()).andReturn("2");
 		Request request3 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request3.getUserID()).andReturn("2");
 		Request request4 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request4.getUserID()).andReturn("2");
 		Request request5 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request5.getUserID()).andReturn("5");
 		Request request6 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request6.getUserID()).andReturn("5");
 		Request request7 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request7.getUserID()).andReturn("7");
 		Request request8 = EasyMock.createStrictMock(Request.class);
 		EasyMock.expect(request8.getUserID()).andReturn("8");
 		
 		List<Request> firstIntervalRequests = new ArrayList<Request>();
 		firstIntervalRequests.add(request);
 		firstIntervalRequests.add(request2);
 		firstIntervalRequests.add(request3);
 		firstIntervalRequests.add(request4);
 		
 		List<Request> secondIntervalRequests = new ArrayList<Request>();
 		secondIntervalRequests.add(request5);
 		secondIntervalRequests.add(request6);
 		secondIntervalRequests.add(request7);
 		secondIntervalRequests.add(request8);
 		
 		Configuration config = EasyMock.createStrictMock(Configuration.class);
 		PowerMock.mockStatic(Configuration.class);
 		EasyMock.expect(Configuration.getInstance()).andReturn(config).times(3);
 		EasyMock.expect(config.getWorkloads()).andReturn(new String[]{PropertiesTesting.VALID_WORKLOAD_2});
 		EasyMock.expect(config.getLong(SaaSAppProperties.APPLICATION_SLA_MAX_RESPONSE_TIME)).andReturn(sla);
 		EasyMock.expect(config.getString(SimulatorProperties.PLANNING_TYPE)).andReturn("SMALL");
 		
 		TimeBasedWorkloadParser parser = PowerMock.createStrictMockAndExpectNew(TimeBasedWorkloadParser.class, EasyMock.isA(GEISTWorkloadParser.class), EasyMock.anyLong());
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(firstIntervalRequests);
 		EasyMock.expect(parser.hasNext()).andReturn(true);
 		EasyMock.expect(parser.next()).andReturn(secondIntervalRequests);
 		EasyMock.expect(parser.hasNext()).andReturn(false);
 		PowerMock.replay(TimeBasedWorkloadParser.class);
 		
 		PowerMock.replayAll(parser, config, request, request2, request3, request4, request5, request6, request7, request8);
 		
 		OverProvisionHeuristic heuristic = new OverProvisionHeuristic();
 		heuristic.findPlan(null, null, null);
 		
 		Map<MachineType, Integer> plan = heuristic.getPlan(null);
 		assertNotNull(plan);
 		assertEquals(1, plan.size());
 		int value = plan.get(MachineType.SMALL);
		assertEquals(3, value);
 		
 		PowerMock.verifyAll();
 	}
 }
