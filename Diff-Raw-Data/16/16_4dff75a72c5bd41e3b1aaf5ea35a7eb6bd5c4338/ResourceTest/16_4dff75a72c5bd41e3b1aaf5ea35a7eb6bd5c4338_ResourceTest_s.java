 package at.ac.tuwien.lsdc.generator.test;
 
 import static org.junit.Assert.*;
 
 import java.util.LinkedList;
 
 import org.junit.Test;
 
 import at.ac.tuwien.lsdc.resources.App;
 import at.ac.tuwien.lsdc.resources.PhysicalMachine;
 import at.ac.tuwien.lsdc.resources.Resource;
 import at.ac.tuwien.lsdc.resources.VirtualMachine;
 
 public class ResourceTest {
 
 	@Test
 	//sum up:
 	//l1: 1, 2, 3, 4, 5
 	//l2:       1, 4, 5
 	//l3:
 	//out:1, 2, 4, 8, 10
 	public void testAggregate() {
 		System.out.println ("testAggregate:");
 		LinkedList<Integer> l1 = new LinkedList<Integer>();
 		l1.addLast(1);
 		l1.addLast(2);
 		l1.addLast(3);
 		l1.addLast(4);
 		l1.addLast(5);
 		
 		LinkedList<Integer> l2 = new LinkedList<Integer>();
 		l2.addLast(1);
 		l2.addLast(4);
 		l2.addLast(5);
 		
 		LinkedList<Integer> l3 = new LinkedList<Integer>();
 		LinkedList<LinkedList<Integer>> in =new  LinkedList<LinkedList<Integer>>();
 		in.add(l1);
 		in.add(l2);
 		in.add(l3);
 		
 		LinkedList<Integer> out = Resource.aggregateValues(in);
 		System.out.println(out.size());
 		String s="";
 		for (Integer i1 : out) {
 			s+=i1 + ";";
 		}
 		System.out.println(s);
 		assertTrue(out.size()==5);
 		assertTrue(out.get(0)==1);
 		assertTrue(out.get(1)==2);
 		assertTrue(out.get(2)==4);
 		assertTrue(out.get(3)==8);
 		assertTrue(out.get(4)==10);
 	}
 	
 	@Test
 	//get last 3 entries
 	//l1: 1, 2, 3, 4, 5
 	//out:3,4,5
 	public void testGetLastEntriesUtil() {
 		System.out.println ("testGetLastEntriesUtil:");
 		LinkedList<Integer> l1 = new LinkedList<Integer>();
 		l1.addLast(1);
 		l1.addLast(2);
 		l1.addLast(3);
 		l1.addLast(4);
 		l1.addLast(5);
 		
 		LinkedList<Integer> out = Resource.getLastEntriesUtil(l1, 3);
 		
 		String s="";
 		for (Integer i1: l1) {
 			s+=i1 + ";";
 		}
 		System.out.println(s);
 		
 		s="";
 		for (Integer i1: out) {
 			s+=i1 + ";";
 		}
 		System.out.println (s);
 		assertTrue(out.size()==3);
 		assertTrue(out.get(0)==3);
 		assertTrue(out.get(1)==4);
 		assertTrue(out.get(2)==5);
 		
 		out = Resource.getLastEntriesUtil(l1, 0);
 		assertTrue(out.size()==0);
 		
 		out = Resource.getLastEntriesUtil(l1, 10);
 		assertTrue(out.size()==5);
 		
 		out = Resource.getLastEntriesUtil(l1, 3,3);
 		assertTrue(out.size()==3);
 		assertTrue(out.get(0)==2);
 		assertTrue(out.get(1)==3);
 		assertTrue(out.get(2)==4);
 		
 		s="";
 		for (Integer i1: out) {
 			s+=i1 + ";";
 		}
 		System.out.println (s);
 		
 		
 	}
 	
 	@Test
 	// pm1: 
 	// vm1: 
 	//	app1: cpu= 11, mem = 23, storage = 9
 	//	app2: cpu= 22, mem = 14, storage = 11
 	// => VM: cpu= 33, mem = 37, storage = 20
 	//
 	// vm2:
 	//  app3: cpu= 3, mem= 4, storage = 5
 	// => VM: cpu= 3, mem= 4, storage = 5
 	public void testResourceUsages() {
 		System.out.println ("Resource Usage");
 		PhysicalMachine pm1 = new PhysicalMachine();
 		pm1.setRunning(true);
 		
 		VirtualMachine vm1 = new VirtualMachine(40, 40, 40, 0);
 		//App1
 		LinkedList<Integer> cpu = new LinkedList<Integer>();
 		cpu.add(11);
 		cpu.add(13);
 		cpu.add(10);
 		LinkedList<Integer> mem = new LinkedList<Integer>();
 		mem.add(23);
 		mem.add(20);
 		mem.add(18);
 		LinkedList<Integer> storage = new LinkedList<Integer>();
 		storage.add(9);
 		storage.add(12);
 		storage.add(15);
		App app1 = new App(15, 25, 10, cpu, mem, storage);
 		
 		LinkedList<Integer> cpu2 = new LinkedList<Integer>();
 		cpu2.add(22);
 		cpu2.add(15);
 		LinkedList<Integer> mem2 = new LinkedList<Integer>();
 		mem2.add(14);
 		mem2.add(24);
 		LinkedList<Integer> storage2 = new LinkedList<Integer>();
 		storage2.add(11);
 		storage2.add(17);
		App app2 = new App(25, 25, 15, cpu2, mem2, storage2);
 		
 		vm1.getApps().add(app1);
 		vm1.getApps().add(app2);
 		app1.setVm(vm1);
 		app2.setVm(vm1);
 		
 		VirtualMachine vm2 = new VirtualMachine(40, 40, 40, 0);
 		//App1
 		LinkedList<Integer> cpu3 = new LinkedList<Integer>();
 		cpu3.add(3);
 		LinkedList<Integer> mem3 = new LinkedList<Integer>();
 		mem3.add(4);
 		LinkedList<Integer> storage3 = new LinkedList<Integer>();
 		storage3.add(5);
		App app3 = new App(10, 10, 10, cpu3, mem3, storage3);
 		
 		vm2.getApps().add(app3);
 		app3.setVm(vm2);
 		
 
 		pm1.getVms().add(vm1);
 		pm1.getVms().add(vm2);
 		
 		System.out.println("vorTick:");
 		printStatus(pm1);
 		pm1.nextTick();
 		
 		System.out.println("nachTick1:");
 		printStatus(pm1);
 		
 		LinkedList<Integer>cpuHist = pm1.getCpuUsageHistory(3);
 		for (Integer i1: cpuHist) {
 			System.out.print (i1 + ", ");
 		}
 		
 		
 		
 		assertTrue(pm1.getCurrentCpuUsage()==36);
 		assertTrue(pm1.getCurrentMemoryUsage()==41);
 		assertTrue(pm1.getCurrentStorageUsage()==25);
 		
 		System.out.println("nachTick2:");
 		pm1.nextTick();
 		printStatus(pm1);
 		cpuHist = pm1.getCpuUsageHistory(3);
 		for (Integer i1: cpuHist) {
 			System.out.print (i1 + ", ");
 		}
 	}
 	
 	public static void printStatus(PhysicalMachine pm1) {
 		System.out.println ("PM " + pm1.getResourceId() + ": "+ pm1.getRunningTicks() + ", suspended="+ pm1.getSuspendedTicks());
 		System.out.println ("PM " + pm1.getResourceId() + ": " + pm1.getCurrentCpuUsage() + " no vms: " + pm1.getVms().size());
 		for (VirtualMachine vm : pm1.getVms()) {
 			System.out.println ("VM " + vm.getResourceId() + ": " + vm.getRunningTicks() + ", " + vm.getSuspendedTicks());
 			for (App a: vm.getApps()){
 				System.out.println("App " + a.getResourceId() +  ": "+ a.getRunningTicks() + "/" + a.getTicks()+ ", " + a.getSuspendedTicks());
 			}
 		}
 	}
 	
 }
 	
 
 
