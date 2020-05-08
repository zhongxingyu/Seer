 package ase.modules;
 
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import ase.interfaces.InterruptibleModule;
 
 public class Timer extends Thread {
 
 	private int ms;
 	InterruptibleModule module;
 	private int pin;
 	private ArrayBlockingQueue<Boolean> interruptSignal = new ArrayBlockingQueue<Boolean>(1);
 	
 	public Timer(InterruptibleModule module, int id){
 		this.module = module;
 		this.pin = id;
 		start();
 	}
 	
 	public void reset()
 	{
 		interruptSignal.offer(true);
 	}
 	
 	public void initiate(int ms)
 	{
 		interruptSignal.poll();
 		this.ms = ms;
 		resume();
 	}
 	
 	@Override
 	public void run()
 	{
 		Object signal = null;
 		while(true)
 		{
 			suspend();
 			try {
 				signal = interruptSignal.poll(ms, TimeUnit.MILLISECONDS);
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 
 			if(signal == null) this.module.interruptModule(pin);
 		}
 	}
 }
