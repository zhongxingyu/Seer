 package ru.ifmo.neerc.connectors;
 
 import java.net.MalformedURLException;
import com.caucho.hessian.client.HessianProxyFactory;
 
 import pcms2.services.client.AuthorizationFailedException;
 import pcms2.services.client.LoginData;
 import pcms2.services.client.LoginDataService;
 import pcms2.services.site.Clock;
 import ru.ifmo.neerc.framework.Callback;
 import ru.ifmo.neerc.framework.Watch;
 
 
 
 
 public class Pcms2 {
 	private final int updateInterval;
 	private final String url, login, password;
 
 	private Watch<Long> time, length, startTime;
 	private Watch<Integer> status;
 
 	private class Worker implements Runnable {
 
 		@Override
 		public void run() {
 			HessianProxyFactory factory = new HessianProxyFactory();
 			while (true) {
 				try {
 					LoginDataService ldsvc = (LoginDataService) factory.create(
 							LoginDataService.class, url);
 					LoginData ld = ldsvc.getLoginData(login, password);
					System.out.println("Sync " + ld);
 					if (ld != null) {
 						Clock clock = ld.getClock();
 	
 						time.set(clock.getTime());
 						length.set(clock.getLength());
 						startTime.set(clock.getStartTime());
 						status.set(clock.getStatus());
 					}
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (AuthorizationFailedException e) {
 					e.printStackTrace();
 				}
 				
 				try {
 					Thread.sleep(updateInterval);
 				} catch (InterruptedException e) {
 					return;
 				}
 			}
 		}
 
 	}
 
 	public Pcms2(String host) {
 		this(host, "guest", "guest");
 	}
 	
 	public Pcms2(String host, String login, String password) {
 		this(host, login, password, 1000);
 	}
 	
 	public Pcms2(String host, String login, String password, int updateInterval) {
 		this.url = host;
 		this.updateInterval = updateInterval;
 		this.login = login;
 		this.password = password;
 		new Thread(new Worker()).start();
 		time = new Watch<Long>();
 		startTime = new Watch<Long>();
 		length = new Watch<Long>();
 		status = new Watch<Integer>();
 	}
 	
 	public void hookTimeChange(Callback<Long> c) {
 		time.addListener(c);
 	}
 	
 	public void hookStartTimeChange(Callback<Long> c) {
 		startTime.addListener(c);
 	}
 	
 	public void hookLengthChange(Callback<Long> c) {
 		length.addListener(c);
 	}
 	
 	public void hookStatusChange(Callback<Integer> c) {
 		status.addListener(c);
 	}
 
 }
