 package com.apple;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.apple.common.RandomName;
 import com.apple.dao.UserDao;
 import com.apple.entity.AppleUser;
 import com.apple.service.AppleService;
 
 public class IphoneMain {
 	private static Log log = LogFactory.getLog(IphoneMain.class);
 	// 从配置文件中取出最大线程，初始化线程池
 	private static ExecutorService exec = Executors
 			.newFixedThreadPool(Config.THREAD_POOL_NUM);
 
 	private static ApplicationContext appContext = 
 		new ClassPathXmlApplicationContext("spring.xml");
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String... args) throws Exception {
 		
 		Logs.getLogger().info("开始");
 		
 
 		// LOOP START
 		while (true) {
 			// 取得密码正确的注册用户
 			List<User> users = new ArrayList<User>();
 
 			try {
 				Logs.getLogger().info("开始取得用户列表");
 				users = getUsers();
 
 			} catch (Exception e) {
 				Logs.getLogger().error("取得用户列表错误", e);
 				return;
 			}
 			// 开始时间
 			long startTime = System.currentTimeMillis();
 
 			int userSize = users.size();
 			Logs.getLogger().info("用户数:" + userSize);
 
 			// 监视单个线程
 			final CountDownLatch doneSignal = new CountDownLatch(userSize);
 
 			final String type = args[0];
 			for (final User user : users) {
 				Runnable run = new Runnable() {
 					public void run() {
 						try {
 							// 1 iphone3 8
 							// 2 iphone4 16
 							// 3 iphone4 32
 							user.buy(Integer.parseInt(type));
 							
 						} catch (Exception e) {
 							Logs.getLogger().error(e,e);
 							Logs.getLogger().error("出错用户信息：" + user.getAppleId());
 						} finally {
 							doneSignal.countDown();
 						}
 					}
 				};
 				// 线程池里添加线程
 				exec.execute(run);
 			}
 
 			doneSignal.await(); // wait for all to finish
 			
 			// 保存
 			appContext.getBean(AppleService.class).save(users);
 			
 			// 结束时间
 			long endTime = System.currentTimeMillis();
 			// 用时
 			long usedTime = (endTime - startTime);
 			Logs.getLogger().info("用时： " + usedTime + " MS");
 
 			// 结束时间停止进程
 			if (isEndTime()) {
 				exec.shutdown(); // must shutdown
 				return;
 			}
 		}
 		// LOOP END
 
 	}
 
 	/**
 	 * 是否到结束时间
 	 * 
 	 * @return boolean
 	 * @throws ParseException
 	 */
 	private static boolean isEndTime() throws ParseException {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 		Date nowDate = Calendar.getInstance().getTime();
 		String dateStr = dateFormat.format(nowDate);
 
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
 		Date endTime = df.parse(dateStr + " " + Config.END_TIME);
 		Logs.getLogger().info("结束时间" + endTime);
 		return nowDate.after(endTime);
 	}
 
 	private static List<User> getUsers() {
 		List<User> startUsers = new ArrayList<User>();
 		int[] intArr = {2,4};
 		for(int i: intArr){
 			User user = new User();
 			user.setAppleId("hujiag"+ i +"@eyou.com");
 			user.setPassword("pengyan");
 			user.setEmail("hujiag"+ i +"@eyou.com");
			user.setPhoneNumber("13482379211" + i);
 			user.setStreet("宜山路1398号特力集团" + i);
 			String name = RandomName.getName();
 			user.setLastName(name.substring(0, 1));
 			user.setFirstName(name.substring(1));
 			startUsers.add(user);
 		}
 		
 //		UserDao userDao = (UserDao)appContext.getBean("userDao");
 //		List<AppleUser> appleUsers = userDao.getList();
 //		log.info("user size:" + appleUsers.size());
 //		for(AppleUser appleUser : appleUsers) {
 //			int i =1;
 //			User user = new User();
 //			user.setAppleId(appleUser.getAppleid());
 //			user.setPassword(appleUser.getPassword());
 //			user.setEmail(appleUser.getAppleid());
 //			user.setPhoneNumber("134823792" + i);
 //			user.setStreet("苍梧路468弄6号60" + i);
 //			startUsers.add(user);
 //			
 //			log.info("appleid:" + appleUser.getAppleid()+ " password:" + user.getPassword());
 //			i++;
 //		}
 		return startUsers;
 	}
 }
