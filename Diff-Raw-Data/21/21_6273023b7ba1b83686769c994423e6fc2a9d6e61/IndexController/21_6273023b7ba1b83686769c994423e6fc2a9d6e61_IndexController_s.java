 package com.guotingchao.controlle.front;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import redis.clients.jedis.Jedis;
 
 import com.guotingchao.MyPlugin.RedisKit;
 import com.guotingchao.interceptor.ForntInterceptor;
 import com.guotingchao.interceptor.ForntUnameInterceptor;
 import com.guotingchao.model.BaseModel;
 import com.guotingchao.model.impl.Branch;
 import com.guotingchao.model.impl.T_user_task;
 import com.guotingchao.model.impl.Task;
 import com.guotingchao.model.impl.User;
 import com.guotingchao.tools.Compare;
 import com.guotingchao.tools.Utils;
 import com.guotingchao.validator.front.AddBranchValidate;
 import com.guotingchao.validator.front.AddTaskValidate;
 import com.guotingchao.validator.front.LoginValidate;
 import com.guotingchao.validator.front.UpdateTaskValidate;
 import com.jfinal.aop.Before;
 import com.jfinal.core.Controller;
 import com.jfinal.log.Log4jLogger;
 import com.jfinal.log.Logger;
 import com.jfinal.plugin.activerecord.Model;
 
 /**
  * 前端Index控制器
  * @author os-yugq
  */
 @Before(ForntUnameInterceptor.class)
 public class IndexController extends Controller{
 	
 	Logger log= Log4jLogger.getLogger(IndexController.class);
 	Jedis jedis = RedisKit.getJedis();
 	
 	/**
 	 * 添加分支
 	 */
 	@Before(ForntInterceptor.class)	
 	public void addBranch(){
 		if(getUser("id")!=null){
 			Long uid =Long.parseLong(getUser("id"));
 			String task_key = getSession().getId()+uid;
 			Task task= new Task();
 			task.set("taskName",getPara("taskName"));
 			task.set("taskInfo",getPara("taskInfo"));
 			task.set("rank",getPara("taskRank"));
 			task.set("play_Time",getPara("taskPlayTime"));
 			log.info(task.toJson());
 			jedis.set(task_key+"task", task.toJson());
 			setSessionAttr("task", task_key+"task");
 			
 			jedis.set(task_key+"userNames", getPara("userNames"));
 			setSessionAttr("taskUserNames", task_key+"userNames");
 		}
 		render("addBranch.jsp");
 		
 	}
 	/**
 	 * 添加分支后挑战到任务页面
 	 */
 	@Before(AddBranchValidate.class)
 	public void backTask(){
 		if(getPara("branch.branchName")!=null){
 			if(getUser("id")!=null){
 				String branchStr = "";
 				//判断是否第一次添加分支模块
 				String branch_key = getSessionAttr("branch_key");
 				if(branch_key == null){
 					//第一次创建key值
 					branch_key= Utils.getCurTime()+getUser("id")+"branch_key";
 				}else{
 					//已经添加过  获取添加过的内容
 					if(jedis.exists(branch_key)){
 						branchStr = jedis.get(branch_key);
 						if(!"".equals(branchStr)){
 							branchStr = branchStr.substring(0, branchStr.length()-1);
 						}
 					}
 				}
 				
 				//获取参数并封装json保存到redis
 				Branch branch = new Branch();
 				branch.set("branchName",getPara("branch.branchName"));
 				branch.set("branchInfo",getPara("branch.branchInfo"));
 				branch.set("rank",getPara("branch.rank"));
 				branch.set("play_Time",getPara("branch.play_Time"));
 				branch.set("uid",getPara("branch.uid"));
 				if("".equals(branchStr)){
 					branchStr = "["+branch.toJson()+"]";
 				}else{
 					branchStr += ","+branch.toJson()+"]";
 				}
 				//保存到redis 并保存的session
 				jedis.set(branch_key, branchStr);
 				setSessionAttr("branch_key",branch_key);
 
 				/********单独的保存分支模块的名称********/
 				String branchName = "";
 				String branchName_key = getSessionAttr("branchName_key");
 				if(branchName_key == null){
 					//第一次创建key值
 					branchName_key= Utils.getCurTime()+getUser("id")+"branchName_key";
 				}else{
 					//已经添加过  获取添加过的内容
 					if(jedis.exists(branchName_key)){
 						branchName = jedis.get(branchName_key);
 					}
 				}
 				if("".equals(branchName)){
 					branchName = getPara("branch.branchName");
 				}else{
 					branchName +="," + getPara("branch.branchName");
 				}
 				
 				setAttr("branchName", branchName);
 				jedis.set(branchName_key, branchName);
 				setSessionAttr("branchName_key",branchName_key);
 			}
 		}
 		//获取已经写入任务的信息  
 		String task_key  = getSessionAttr("task");
 		if(task_key!=null){
 			if(jedis.exists(task_key)){
 				try {
 					JSONObject json=  new JSONObject(jedis.get(task_key));
					setAttr("taskName", json.get("taskname"));
					setAttr("taskInfo", json.get("taskinfo"));
					setAttr("play_Time", json.get("play_time"));
 					setAttr("rank", json.get("rank"));
 					
 				}catch(JSONException e){
 					log.error(e.getMessage());
 				}
 			}
 		}
 		//选定的人员
 		String taskUserNames = getSessionAttr("taskUserNames");
 		if(taskUserNames!=null){
 			if(jedis.exists(taskUserNames)){
 				setAttr("taskUserNames", jedis.get(taskUserNames));
 			}
 		}
 		render("addTask.jsp");
 	}
 	/**
 	 * 添加新任务
 	 */
 	@Before(ForntInterceptor.class)
 	public void addTask(){
 		
 		//全部可选人员
 		setSessionAttr("userListSession",User.userDao.findUserList());
 		render("addTask.jsp");
 	}
 	
 	@Before(AddTaskValidate.class)
 	public void doAddTask(){
 		try {
 			//保存新任务
 			Task task =new Task();
 			task.set("taskMaker", getPara("task.taskMaker"));
 			task.set("taskInfo", getPara("task.taskInfo"));
 			task.set("rank", getParaToInt("task.rank"));
 			task.set("taskName", getPara("task.taskName"));
 			task.set("play_Time", Utils.getDate(getPara("task.play_Time"))); 
 			
 			if (task.save()) {
 				//中间表添加内容  批量保存处理
 				List<Model> user_task_list = new ArrayList<Model>();
 				String[] uid = getPara("user.id").split(",");
 				String tid = task.get("id").toString();
 				for(int i=0;i<uid.length;i++){
 					T_user_task temp_user_task = new T_user_task();
 					temp_user_task.set("tid",tid);
 					temp_user_task.set("uid",uid[i]);
 					
 					user_task_list.add(temp_user_task);
 				}
 				//保存中间表
 				log.info("batchSave返回内容="+T_user_task.taskUserDao.batchSave(user_task_list));
 				
 				/*************添加模块*****************/
 				//获取session的branch_key
 				String branch_key = getSessionAttr("branch_key");
 				if(branch_key != null && !"".equals(branch_key)){
 					if(jedis.exists(branch_key)){						
 						String branchStr = jedis.get(branch_key);
 						
 						
 						if(branchStr != null && !"".equals(branchStr)){
 							JSONArray array = new JSONArray(branchStr);
 							//批量保存处理
 							List<Model> branch_list = new ArrayList<Model>();
 							for(int i = 0; i < array.length(); i++){
 								JSONObject json =  array.getJSONObject(i);
 								Branch branch = new Branch();
 								branch.set("tid",tid);
								branch.set("branchName", json.get("branchname"));
								branch.set("branchInfo", json.get("branchinfo"));
								branch.set("play_Time", json.get("play_time"));
 								branch.set("rank", json.get("rank"));
 								branch.set("uid", json.get("uid"));
 								
 								branch_list.add(branch);
 							}
 							if(Branch.branchDao.batchSave(branch_list)){
 								setAttr("add_success_msg", "添加成功");
 							}else{
 								setAttr("add_success_msg", "失败模块未添加");
 							}
 							
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			log.error("添加任务" + getPara("task.taskName")+ "失败,原因：" + e.getMessage());
 			setAttr("add_success_msg", "添加失败");
 		}
 			render("addTask.jsp");
 	}
 	/**
 	 *  主页
 	 */
 	public void index(){
 		if(getUser("uname")!=null){
 			String uname = getUser("uname");
 			List<Task> taskListRelative = Task.taskDao.findTaskByUser(uname);
 			if(taskListRelative.size()>0){
 				setAttr("taskListRelative", taskListRelative);
 			}
 		}
 		//初始化任务
 		setAttr("taskListInit", Task.taskDao.findTaskListByType(0));
 		setAttr("taskListOn", Task.taskDao.findTaskListByType(1));
 		setAttr("taskListFinish", Task.taskDao.findTaskListByType(2));
 		setAttr("taskListBlocked", Task.taskDao.findTaskListByType(-1));
 		render("index.jsp");
 	}
 	/**
 	 * 获取消息任务内容
 	 */
 	public void msgInterceptor(){
 		HttpServletResponse response = getResponse();
 		JSONObject json = null;
 		response.setContentType("charset=utf-8");
 		
 		if(getUser("id")!=null){
 			Long uid = Long.parseLong(getUser("id"));
 			//获取有信息的任务
 			List<BaseModel<T_user_task>> list = T_user_task.taskUserDao.findMsgTaskByUid(uid);
 
 			if(list!=null){
 				int count = list.size();
 				//有信息的任务列表
 				List<Task> taskList = new ArrayList<Task>(count);
 				for(int i = 0;i < count;i++){
 					taskList.add(Task.taskDao.findById(list.get(i).getInt("tid")));
 				}
 				if(taskList.size()>0){
 					int last = taskList.size()-1;
 					
 					try {
 						json = new JSONObject(taskList.get(last).toJson());
 						json.put("MsgCount", count);
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		if(json==null){
 			renderNull();
 		}else{
 			renderJson(json.toString());
 		}
 	}
 	/**
 	 * 查看未读任务，并标识为已读
 	 */			
 	public void doCheckedMsgTask(){
 		if(getUser("id")!=null){
 			Long uid = Long.parseLong(getUser("id"));
 			int tid = getParaToInt("tid");
 			T_user_task.taskUserDao.checkedMsgTask(tid, uid);
 		}
 		
 		renderNull();
 	}
 	/**
 	 * 显示与自己相关的未查看任务
 	 */
 	public void showMsgTask(){
 		if(getUser("id")!=null){
 			Long uid = Long.parseLong(getUser("id"));
 			//获取有信息的任务
 			List<BaseModel<T_user_task>> list = T_user_task.taskUserDao.findMsgTaskByUid(uid);
 			if(list!=null){
 				int count = list.size();
 				//有信息的任务列表
 				List<Task> taskList = new ArrayList<Task>(count);
 				for(int i = 0;i < count;i++){
 					taskList.add(Task.taskDao.findById(list.get(i).getInt("tid")));
 				}
 			    setAttr("MsgTaskList", taskList);
 			}
 		}
 		render("showMsgTask.jsp");
 	}
 	/**
 	 * 把消息任务全部更新为已查看任务
 	 */
 	public void doUpdateMsgTask(){
  		try{
 			if(T_user_task.taskUserDao.updateMsgTask()){
 				removeSessionAttr("MsgTaskList");
 				removeSessionAttr("MsgTask");
 				removeSessionAttr("MsgCount");
 			}else{
 				log.info("更新消息任务失败！");
 			}
 		}catch(Exception e){
 			log.info("更新消息任务失败！"+e.getMessage());
 		}
 		renderNull();
 	}
 	/**
 	 * 更新任务
 	 */
 	public void updateTask(){
 		Long tid = getParaToLong();
 		setSessionAttr("tid", tid);
 		setSessionAttr("userListSession",User.userDao.findUserList());
 		setAttr("userList",User.userDao.getUserByTaskId(tid));
 		setAttr("task", Task.taskDao.findTaskById(tid));
 		render("updateTask.jsp");
 	}
 	@Before(UpdateTaskValidate.class)
 	public void doUpdateTask(){
 		try {
 			//更新任务
 			Long tid= getSessionAttr("tid");
 			Task task =Task.taskDao.findById(tid);
 			task.set("taskMaker", getPara("task.taskMaker"));
 			task.set("taskInfo", getPara("task.taskInfo"));
 			task.set("percent", getPara("task.percent"));
 			task.set("taskType", getPara("task.taskType"));
 			task.set("rank", getParaToInt("task.rank"));
 			task.set("taskName", getPara("task.taskName"));
 			SimpleDateFormat sdf = new  SimpleDateFormat( "yyyy-MM-dd" ); 
 			Date play_time=sdf.parse(getPara("task.play_Time"));
 			task.set("play_Time", play_time); 
 			
 			if (task.update()) {
 				String[] uid_new = getPara("user.id").split(",");
 				String[] uid_old = T_user_task.taskUserDao.getUidArray(tid);
 				//两个数组内容不同就进行  更新中间表
 				if(!Compare.ComArray(uid_new, uid_old)){
 					int newCount = uid_new.length;
 					int oldCount = uid_old.length;
 					//在uid_new中不包含uid_old里的uid  就删除此项
 					for(int i = 0;i < oldCount; i++){
 						if(!Compare.Content(uid_new, uid_old[i])){
 							if(T_user_task.taskUserDao.deleteByTidAndUid(tid, Long.parseLong(uid_old[i]))){
 								log.info("删除中间表中的里面的内容。。。 成功");
 							}
 						}
 					}
 					//在uid_old中不包含uid_new里的uid  就添加此项
 					for(int i = 0;i < newCount; i++){
 						if(!Compare.Content(uid_old, uid_new[i])){
 							T_user_task user_task = new T_user_task();
 							user_task.set("tid", tid);
 							user_task.set("uid",uid_new[i]);
 							if(user_task.save()){
 								log.info("添加中间表中的里面的内容。。。成功");
 							};
 						}
 					}
 					
 				}
 				
 				setAttr("update_success_msg", "添加成功");
 			}
 		} catch (Exception e) {
 			log.error("更新任务" + getPara("task.taskName")+ "失败,原因：" + e.getMessage());
 			setAttr("update_success_msg", "添加失败");
 		}
 		render("updateTask.jsp");
 	}
 	
 	
 	/**
 	 * 登录
 	 */
 	
 	public void login(){
 		
 		render("login.jsp");
 	}
 	/**
 	 * 登录动作处理
 	 */
 	@Before(LoginValidate.class)
 	public void doLogin() {
 		
 	}
 	/**
 	 * 退出登录
 	 */
 	public void loginOut(){
 		String user_info = getCookie("user_info");
 		if(user_info==null){
 			user_info = getSessionAttr("user_info");
 			if(user_info!=null){
 				if(jedis.exists(user_info)){
 					jedis.del(user_info);
 				}
 			}
 		}
 		
 		removeSessionAttr("user_info");
 		removeCookie("user_info");
 		removeSessionAttr("actionKey");
 		redirect("/");
 	}
 	/**
 	 * 获取user的 某个字段
 	 * @param key
 	 * @return
 	 */
 	public String getUser(String key){
 		String value = null;
 		String user_info = getCookie("user_info");
 		if(user_info==null){
 			user_info = getSessionAttr("user_info");
 		}
 		if(user_info!=null){
 			if(jedis.exists(user_info)){
 				try {
 					JSONObject json = new JSONObject(jedis.get(user_info));
 					value = json.getString(key);
 				}catch(JSONException e){
 					log.error(e.getMessage());
 				}
 			}
 		}
 		return value;
 	}
 
 }
