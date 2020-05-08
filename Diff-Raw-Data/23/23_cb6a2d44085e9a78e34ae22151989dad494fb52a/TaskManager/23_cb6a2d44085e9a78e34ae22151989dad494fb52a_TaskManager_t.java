 /**
  * 
  */
 package com.k99k.khunter;
 
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 
 import com.k99k.tools.JSONTool;
 
 
 /**
  * Task管理器，负责载入和刷新Task，以及添加新的Task等.
  * 创建Task的例子：
 <pre>
 //log为Action的name
 ActionMsg msg = new ActionMsg("log");
 //task的类型必须要有
 msg.addData(TASK_TYPE, TASK_TYPE_SCHEDULE_POOL);
 //不同类型的task需要有不同的参数
 msg.addData(TASK_DELAY, 5000);
 //静态方法创建新任务,至此任务创建完成,TaskManager将按类型完成任务的执行
 TaskManager.makeNewTask("newTaskTest", msg);
 //取消未执行的任务或定时/循环任务
 TaskManager.cancelTask("newTaskTest2");
 </pre>
  * @author keel
  *
  */
 public final class TaskManager {
 
 	private TaskManager() {
 	}
 	
 	static final Logger log = Logger.getLogger(TaskManager.class);
 	
 	/**
 	 * 用于在json中定位
 	 * @return 返回"tasks"
 	 */
 	public static final String getName(){
 		return "tasks";
 	}
 
 	private static boolean isInitOK = false;
 	
 	private static String iniFilePath;
 	
 	private static String classFilePath;
 	
 	
 	
 	private static int ratePoolSize = 5;
 	private static int scheduledPoolSize = 10;
 	
 	private static int taskMapInitSize = 50000;
 	
 	/**
 	 * 非即时任务的引用集合,以任务名为key,ScheduledFuture为value实现对任务的调度
 	 */
 	private static ConcurrentHashMap<String,ScheduledFuture<?>> taskMap = new ConcurrentHashMap<String, ScheduledFuture<?>>(taskMapInitSize);
 	
 	/**
 	 * 定时任务执行的线程池
 	 */
 	private final static ScheduledThreadPoolExecutor scheduledPool =  new ScheduledThreadPoolExecutor(ratePoolSize,new RejectedTaskHandler());
 	
 	/**
 	 * 定时循环执行任务的线程池
 	 */
 	private final static ScheduledThreadPoolExecutor ratePool =  new ScheduledThreadPoolExecutor(scheduledPoolSize,new RejectedTaskHandler());
 	
 	/**
 	 * 单线程立即执行任务的线程池
 	 */
 	private final static ThreadPoolExecutor singleExePool = new ThreadPoolExecutor(1, 1,
             0L, TimeUnit.SECONDS,
             new LinkedBlockingQueue<Runnable>());
 	
 		
 
 	//exePool的相关参数如下
 	private static int corePoolSize = 10;
 	private static int maximumPoolSize = 200;
 	private static long keepAliveTime = 3000L;
 	private static int queueSize = 100;
 	private static ArrayBlockingQueue<Runnable> arrBlockQueue = new ArrayBlockingQueue<Runnable>(queueSize);
 	
 	/**
 	 * 立即执行任务的多线程线程池
 	 */
 	private static ThreadPoolExecutor exePool = new ThreadPoolExecutor(
 			corePoolSize,
 			maximumPoolSize,
 			keepAliveTime,TimeUnit.MILLISECONDS,
 			arrBlockQueue,
 			new RejectedTaskHandler()
 	);
 	
 	
 	public static final int TASK_TYPE_EXE_POOL = 1;
 	public static final int TASK_TYPE_EXE_SINGLE = 2;
 	public static final int TASK_TYPE_SCHEDULE_POOL = 3;
 	public static final int TASK_TYPE_SCHEDULE_RATE = 4;
 	
 	public static final String TASK_TYPE = "[taskType]";
 	public static final String TASK_DELAY = "[taskDelay]";
 	public static final String TASK_INIT_DELAY = "[taskInitDelay]";
 	public static final String TASK_CANCEL = "[taskCanCancel]";
 	
 	
 //	/**
 //	 * 清理taskMap,移除已经完成的过期task --由Task运行结束时自动调用清理
 //	 * TO DO 可通过配置Action来实现定期或达到一定大小时自动清理taskMap
 //	 */
 //	public static final void clearTaskMap(){
 //		for (Iterator<String> it = taskMap.keySet().iterator(); it.hasNext();) {
 //			String taskKey = it.next();
 //			ScheduledFuture<?> sf = taskMap.get(taskKey);
 //			if (sf.isDone()) {
 //				taskMap.remove(taskKey);
 //			}
 //		}
 //	}
 	
 	/**
 	 * 从taskMap中移除task
 	 * 
 	 */
 	public static final void removeFromTaskMap(String taskKey){
 		taskMap.remove(taskKey);
 	}
 	
 	/**
 	 * 退出前关闭所有任务
 	 */
 	public static final void exit(){
 		exePool.shutdownNow();
 		singleExePool.shutdownNow();
 		ratePool.shutdownNow();
 		scheduledPool.shutdownNow();
 		isInitOK = false;
 		taskMap.clear();
 		log.info("TaskManager exited");
 	}
 	
 	/**
 	 * 添加一个立即执行的任务到立即处理的多线程线程池
 	 * @param task
 	 */
 	private static void addExeTask(Task task){
 		exePool.execute(task);
 	}
 	
 	/**
 	 * 添加一个定时执行的任务
 	 * @param task Task
 	 * @param delay 延迟
 	 * @param unit 时间单位
 	 */
 	private static void addScheduledTask(Task task,long delay,TimeUnit unit){
 		if (delay <= 0) {
 			log.warn("ScheduledTask with no delay! Excuting now. task:"+task);
 		}
 		ScheduledFuture<?> sf = scheduledPool.schedule(task, delay, unit);
 		if (task.isCanCanceled()) {
 			taskMap.put(task.getName(), sf);
 		}
 	}
 	
 
 	/**
 	 * 添加一个循环执行的任务,如果没有delay参数则直接返回
 	 * @param task Task
 	 * @param initDelay 初始延迟
 	 * @param delay 循环延迟
 	 * @param unit 时间单位
 	 */
 	private static void addRateTask(Task task,long initDelay,long delay,TimeUnit unit){
 		if (delay <= 0) {
 			log.error("RateTask with no delay! Task canceled!! task:"+task);
 			return;
 		}
 		ScheduledFuture<?> sf = ratePool.scheduleAtFixedRate(task, initDelay, delay, unit);
 		if (task.isCanCanceled()) {
 			taskMap.put(task.getName(), sf);
 		}
 	}
 	
 	/**
 	 * 由任务名来取消任务
 	 * @param taskName String 
 	 * @return 是否取消成功
 	 */
 	public static boolean cancelTask(String taskName){
 		ScheduledFuture<?> sf = taskMap.remove(taskName);
 		return sf.cancel(false);
 	}
 	
 	/**
 	 * 添加一个立即处理的任务到单线程池
 	 * @param task Task
 	 */
 	private static void addSingleTask(Task task){
 		singleExePool.execute(task);
 	}
 	
 
 	/**
 	 * 创建一个新的任务,ActionMsg必须包括Task相关的参数
 	 * @param taskName String 任务名,取消任务时需要以此来定位任务
 	 * @param msg ActionMsg 用于执行任务的Action
 	 * @return 当taskName有重名或msg参数有误时返回false
 	 */
 	public static boolean makeNewTask(String taskName,ActionMsg msg){
 		if (msg == null || taskMap.containsKey(taskName)) {
 			return false;
 		}
 		Object o = msg.getData(TASK_TYPE);
 		int type = (o != null && o.toString().matches("[1234]")) ? Integer.parseInt(o.toString()):0;
 		Object oCancel = msg.getData(TASK_CANCEL);
 		boolean canCancel = (oCancel != null && oCancel instanceof Boolean)?(Boolean)oCancel:true;
 		switch (type) {
 		case TASK_TYPE_EXE_POOL:
 			addExeTask(new Task(taskName,msg,canCancel));
 			break;
 		case TASK_TYPE_EXE_SINGLE:
 			addSingleTask(new Task(taskName,msg,canCancel));
 			break;
 		case TASK_TYPE_SCHEDULE_POOL:
 			Object o1 = msg.getData(TASK_DELAY);
 			//如果没有delay字段则立即表示执行,delay为0
 			long delay = (o1 != null && o1.toString().matches("\\d+")) ? Long.parseLong(o1.toString()):0;
 			addScheduledTask(new Task(taskName,msg,canCancel), delay, TimeUnit.MILLISECONDS);
 			break;
 		case TASK_TYPE_SCHEDULE_RATE:
 			Object o2 = msg.getData(TASK_INIT_DELAY);
 			//如果没有initDelay字段则立即表示执行,initDelay为0
 			long initDelay = (o2 != null && o2.toString().matches("\\d+")) ? Long.parseLong(o2.toString()):0;
 			Object o3 = msg.getData(TASK_DELAY);
 			//如果没有delay字段则立即表示执行,delay为0
 			long delay2 = (o3 != null && o3.toString().matches("\\d+")) ? Long.parseLong(o3.toString()):0;
 			addRateTask(new Task(taskName,msg,canCancel), initDelay, delay2, TimeUnit.MILLISECONDS);
 			break;
 		default:
 			log.error("Task type error:"+type);
 			return false;
 		}
		//log.info("TASK added:"+taskName);
 		return true;
 	}
 	
 	/**
 	 * 初始化TaskManager
 	 * @param iniFile 配置文件路径
 	 * @param classPath class文件所在的路径
 	 * @return 是否初始化成功
 	 */
 	@SuppressWarnings("unchecked")
 	public static boolean init(String iniFile,String classPath){
 		if (!isInitOK) {
 			//读取配置文件刷新注入的Task数据
 			try {
 				String ini = KIoc.readTxtInUTF8(iniFile);
 				Map<String,?> root = (Map<String,?>) JSONTool.readJsonString(ini);
 				//先定位到json的tasks属性
 				Map<String, ?> m = (Map<String, ?>) root.get(TaskManager.getName());
 				Object o = m.get("taskMapInitSize");
 				if (o != null && o.toString().matches("\\d+")) {
 					int val = Integer.parseInt(o.toString());
 					if (val != taskMapInitSize) {
 						taskMapInitSize = val;
 						ConcurrentHashMap<String, ScheduledFuture<?>> tm = new ConcurrentHashMap<String, ScheduledFuture<?>>(taskMapInitSize);
 						synchronized (taskMap) {
 							tm.putAll(taskMap);
 							taskMap = tm;
 						}
 					}
 				}
 				o = m.get("ratePoolSize");
 				if (o != null && o.toString().matches("\\d+")) {
 					int val = Integer.parseInt(o.toString());
 					if (val != ratePoolSize) {
 						ratePoolSize = val;
 						ratePool.setCorePoolSize(ratePoolSize);
 					}
 					
 				}
 				o = m.get("scheduledPoolSize");
 				if (o != null && o.toString().matches("\\d+")) {
 					int val = Integer.parseInt(o.toString());
 					if (val != scheduledPoolSize) {
 						scheduledPoolSize = val;
 						scheduledPool.setCorePoolSize(scheduledPoolSize);
 					}
 					
 				}
 				o =  m.get("exePool");
 				if (o != null && o instanceof Map) {
 					Map<String,?> ep = (Map<String, ?>)o;
 					o = ep.get("corePoolSize");
 					if (o != null && o.toString().matches("\\d+")) {
 						int val = Integer.parseInt(o.toString());
 						if (val != corePoolSize) {
 							corePoolSize = val;
 							exePool.setCorePoolSize(corePoolSize);
 						}
 						
 					}
 					o = ep.get("maximumPoolSize");
 					if (o != null && o.toString().matches("\\d+")) {
 						int val = Integer.parseInt(o.toString());
 						if (val != maximumPoolSize) {
 							maximumPoolSize = val;
 							exePool.setMaximumPoolSize(maximumPoolSize);
 						}
 					}
 					o = ep.get("keepAliveTime");
 					if (o != null && o.toString().matches("\\d+")) {
 						long val = Long.parseLong(o.toString());
 						if (val != keepAliveTime) {
 							keepAliveTime = val;
 							exePool.setKeepAliveTime(keepAliveTime, TimeUnit.MILLISECONDS);
 						}
 						
 					}
 					o = ep.get("queueSize");
 					if (o != null && o.toString().matches("\\d+")) {
 						int val = Integer.parseInt(o.toString());
 						if (val != queueSize) {
 							queueSize = val;
 							ArrayBlockingQueue<Runnable> aq = new ArrayBlockingQueue<Runnable>(queueSize);
 							arrBlockQueue = aq;
 							exePool.shutdown();
 							exePool = new ThreadPoolExecutor(
 									corePoolSize,
 									maximumPoolSize,
 									keepAliveTime,TimeUnit.MILLISECONDS,
 									arrBlockQueue,
 									new RejectedTaskHandler()
 							);
 						}
 					}
 				}
 			} catch (Exception e) {
 				log.error("TaskManager init Error!", e);
 				isInitOK = false;
 				return false;
 			}
 			isInitOK = true;
 			iniFilePath = iniFile;
 			classFilePath = classPath;
 			log.info("TaskManager init OK!");
 		}
 		return true;
 	}
 	
 	/**
 	 * 重新初始化
 	 * @param iniFile 配置文件路径
 	 * @param classPath class文件所在的路径
 	 * @return 是否初始化成功
 	 */
 	public static boolean reInit(String iniFile,String classPath){
 		exit();
 		return init(iniFile,classPath);
 	}
 	
 	public static final boolean isInitOK(){
 		return isInitOK;
 	}
 	
 	
 	//TODO 告警机制：可采用在另一服务器用robot监控游戏各个环节，与服务端分离
 
 	
 	/**
 	 * 刷新(重载)一个Task
 	 * @param act actionName
 	 */
 	@SuppressWarnings("unchecked")
 	public static final boolean reLoadTask(String act){
 		try {
 			String ini = KIoc.readTxtInUTF8(iniFilePath);
 			Map<String,?> root = (Map<String,?>) JSONTool.readJsonString(ini);
 			//先定位到json的actions属性
 			Map<String, ?> tasksMap = (Map<String, ?>) root.get(TaskManager.getName());
 			Map<String, ?> m = (Map<String, ?>) tasksMap.get(act);
 			if (!m.containsKey("_class")) {
 				log.error("Task init Error! miss key prop:_class");
 				return false;
 			}
 				
 			String _class = (String) m.get("_class");
 			Object o = KIoc.loadClassInstance("file:/"+classFilePath, _class, new Object[]{act});
 			if (o == null) {
 				log.error("loadClassInstance error! _class:"+_class+" _name:"+act);
 				return false;
 			}
 			Task action = (Task)o;
 			HTManager.fetchProps(action, m);
 			
 		} catch (Exception e) {
 			log.error("TaskManager reLoadTask Error:"+act, e);
 			return false;
 		}
 		log.info("Task reLoaded: "+act);
 		return true;
 	}
 	
 	public static void main(String[] args) {
 		String webRoot = "f:/works/workspace_keel/KHunter/WebContent/WEB-INF/";
 		String jsonFilePath = webRoot+"kconfig.json";
 		String classPath = webRoot+"classes/";
 		ActionManager.init(jsonFilePath, classPath);
 		TaskManager.init(jsonFilePath, classPath);
 		
 		ActionMsg msg = new ActionMsg("testLog");
 		msg.addData(TASK_TYPE, TASK_TYPE_SCHEDULE_POOL);
 		msg.addData(TASK_DELAY, 5000);
 		TaskManager.makeNewTask("newTaskTest", msg);
 		ActionMsg msg2 = new ActionMsg("testLog");
 		msg2.addData(TASK_INIT_DELAY, 2000);
 		msg2.addData(TASK_DELAY, 2000);
 		msg2.addData(TASK_TYPE, TASK_TYPE_SCHEDULE_RATE);
 		try {
  			Thread.sleep(2000);
  		} catch (InterruptedException e) {
  		}
 		
  		TaskManager.makeNewTask("newTaskTest2", msg2);
 		try {
  			Thread.sleep(15000);
  		} catch (InterruptedException e) {
  		}
  		System.out.println("cancel newTaskTest2:"+TaskManager.cancelTask("newTaskTest2"));
 	}
 	
 }
