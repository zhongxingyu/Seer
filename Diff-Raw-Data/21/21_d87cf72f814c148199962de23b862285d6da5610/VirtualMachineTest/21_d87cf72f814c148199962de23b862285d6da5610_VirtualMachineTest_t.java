 package de.unisiegen.informatik.bs.alvis.vm.test;
 
 import junit.framework.Assert;
 
 import org.testng.annotations.Test;
 
 import resources.FirstAlgo;
 import resources.SecondAlgo;
 import resources.ThreadAlgo;
 
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PseudoCodeInteger;
 import de.unisiegen.informatik.bs.alvis.vm.BPListener;
 import de.unisiegen.informatik.bs.alvis.vm.VirtualMachine;
 
 /**
  * 
  * @author Dominik Dingel
  * 
  */
 
 public class VirtualMachineTest {
 	@Test
 	public void checkDoneBeforeInit() {
 		VirtualMachine vm = VirtualMachine.getInstance();
 		Assert.assertEquals(vm.isAlive(), false);
 	}
 
 	@Test
 	public void loadAlgoClassTrue() {
 		VirtualMachine vm = VirtualMachine.getInstance();
 		Assert.assertEquals(true, vm.setAlgoClassToRun(FirstAlgo.class));
 	}
 
 	@Test
 	public void loadAlgoClassFalse() {
 		VirtualMachine vm = VirtualMachine.getInstance();
 		Assert.assertEquals(vm.setAlgoClassToRun(null), false);
 	}
 
 	@Test
 	public void loadAlgoAndStartConstructor() {
 		VirtualMachine vm = VirtualMachine.getInstance();
 		vm.setAlgoClassToRun(FirstAlgo.class);
 		vm.startAutoRun();
 	}
 
 	@Test
 	public void getStartTypes() {
 		VirtualMachine vm = VirtualMachine.getInstance();
 		vm.setAlgoClassToRun(SecondAlgo.class);
 		Assert.assertEquals(
 				((PseudoCodeInteger) vm.getStartParameters().get(0))
 						.getTypeName(), "Integer");
 	}
 
 	@Test
 	public void runThreadWithBPBackwardsOnTermination() {
 		VirtualMachine vm = VirtualMachine.getInstance();
 		Object lock = new Object();
 		vm.setAlgoClassToRun(ThreadAlgo.class);
 		vm.addBPListener(new BPListener() {
 			@Override
 			public void onBreakPoint(int BreakPointNumber) {
 			}
 		});
 		vm.startDefaultRun();
 		boolean toRun = true;
 		while (toRun) {
 			synchronized (lock) {
 				try {
 					lock.wait(10);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				vm.stepForward();
 				if(!vm.isAlive()) {
 					toRun = false;
 				}
 			}
 		}
 		vm.waitForBreakPoint();
 		Assert.assertEquals(4, ((PseudoCodeInteger) vm.getRunningRef().get(0)).getLiteralValue());
 	
 	}
 	
 	@Test
 	public void runThreadWithBPBackwards() {
 		VirtualMachine vm = VirtualMachine.getInstance();
 		Object lock = new Object();
		vm.setAlgoClassToRun(vm.getAlgo("test.src.resources.ThreadAlgo"));
 		vm.addBPListener(new BPListener() {
 			@Override
 			public void onBreakPoint(int BreakPointNumber) {
 			}
 		});
 		vm.startDefaultRun();
 		boolean toRun = true;
 		while (toRun) {
 			synchronized (lock) {
 				try {
 					lock.wait(10);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				vm.stepForward();
 				if(!vm.isAlive()) {
 					toRun = false;
 				}
 			}
 		}
 		vm.stepBackward();
 		vm.waitForBreakPoint();
 		Assert.assertEquals(2, ((PseudoCodeInteger) vm.getRunningRef().get(0)).getLiteralValue());
 	}
 	
 	@Test
 	public void runThreadWithBP() {
 		VirtualMachine vm = VirtualMachine.getInstance();
 		Object lock = new Object();
 		vm.setAlgoClassToRun(ThreadAlgo.class);
 		vm.addBPListener(new BPListener() {
 			@Override
 			public void onBreakPoint(int BreakPointNumber) {
 			}
 		});
 		vm.startDefaultRun();
 		while (true) {
 			synchronized (lock) {
 				try {
 					lock.wait(10);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				vm.stepForward();
 				if(!vm.isAlive()) {
 					Assert.assertEquals(4, ((PseudoCodeInteger) vm.getRunningRef().get(0)).getLiteralValue());
 					return;
 				}
 			}
 		}
 	}
 }
