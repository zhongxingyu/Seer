 package aic.monitor;
 
import aic.monitor.util.PropertyManager;
 
 public class BaseMonitor {
 	private int sleepTime = 5000;
 	private AbstractMonitorStrategy strategy;
 
 	public BaseMonitor(AbstractMonitorStrategy strategy) {
 		super();
 		this.strategy = strategy;
 		this.sleepTime = Integer.valueOf(PropertyManager.getInstance()
 				.getProperty("monitor_sleep_time"));
 	}
 
 	public BaseMonitor(AbstractMonitorStrategy strategy, int sleepTime) {
 		super();
 		this.sleepTime = sleepTime;
 		this.strategy = strategy;
 	}
 
 	public int getSleepTime() {
 		return sleepTime;
 	}
 
 	public void setSleepTime(int sleepTime) {
 		this.sleepTime = sleepTime;
 	}
 
 	public void startMonitoring() {
 		while (true) {
 			strategy.monitor();
 			try {
 				Thread.sleep(sleepTime);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	public static void main(String[] args) {
 		BaseMonitor monitor = new BaseMonitor(new SimpleStrategy());
 		monitor.startMonitoring();
 	}
 
 }
