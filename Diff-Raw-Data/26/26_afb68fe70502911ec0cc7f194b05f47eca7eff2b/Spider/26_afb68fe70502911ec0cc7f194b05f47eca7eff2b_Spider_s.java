 package org.eweb4j.spiderman.spider;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.eweb4j.spiderman.fetcher.FetchResult;
 import org.eweb4j.spiderman.fetcher.Page;
 import org.eweb4j.spiderman.plugin.BeginPoint;
 import org.eweb4j.spiderman.plugin.DigPoint;
 import org.eweb4j.spiderman.plugin.DoneException;
 import org.eweb4j.spiderman.plugin.DupRemovalPoint;
 import org.eweb4j.spiderman.plugin.EndPoint;
 import org.eweb4j.spiderman.plugin.FetchPoint;
 import org.eweb4j.spiderman.plugin.ParsePoint;
 import org.eweb4j.spiderman.plugin.PojoPoint;
 import org.eweb4j.spiderman.plugin.TargetPoint;
 import org.eweb4j.spiderman.plugin.TaskPushPoint;
 import org.eweb4j.spiderman.plugin.TaskSortPoint;
 import org.eweb4j.spiderman.task.Task;
 import org.eweb4j.spiderman.xml.Target;
 import org.eweb4j.util.CommonUtil;
 
 
 /**
  * 网络蜘蛛
  * @author weiwei
  *
  */
 public class Spider implements Runnable{
 
 	public Task task;
 	public SpiderListener listener;
 	
 	public void init(Task task, SpiderListener listener) {
 		this.task = task;
 		this.listener = listener;
 	}
 	
 	public void run() {
 		try {
 			//扩展点：begin 蜘蛛开始
 			Collection<BeginPoint> beginPoints = task.site.beginPointImpls;
 			if (beginPoints != null && !beginPoints.isEmpty()){
 				for (BeginPoint point : beginPoints){
 					task = point.confirmTask(task);
 				}
 			}
 			
 			if (task == null) return ;
 			
 			//扩展点：fetch 获取HTTP内容
 			FetchResult result = null;
 			Collection<FetchPoint> fetchPoints = task.site.fetchPointImpls;
 			if (fetchPoints != null && !fetchPoints.isEmpty()){
 				for (FetchPoint point : fetchPoints){
 //					point.context(task);
 					result = point.fetch(task, result);
 				}
 			}
 			
 			if (result == null || result.getPage() == null || result.getPage().getContent() == null || result.getPage().getContent().trim().length() == 0) {
 				listener.onInfo(Thread.currentThread(), task, " spider stop cause the fetch result->+"+result+" of task["+task+"] is null");
 				return ;
 			}
 			
 			listener.onFetch(Thread.currentThread(), task, result);
 			
 			//扩展点：dig new url 发觉新URL
 			Collection<String> newUrls = null;
 			Collection<DigPoint> digPoints = task.site.digPointImpls;
 			if (digPoints != null && !digPoints.isEmpty()){
 				for (DigPoint point : digPoints){
 //					point.context(result, task);
 					newUrls = point.digNewUrls(result, task, newUrls);
 				}
 			}
 			
 			if (newUrls != null && !newUrls.isEmpty())
 				this.listener.onNewUrls(Thread.currentThread(), task, newUrls);
 			else
 				newUrls = new ArrayList<String>();
 			
 			//扩展点：dup_removal URL去重,然后变成Task
 			Collection<Task> validTasks = null;
 			Collection<DupRemovalPoint> dupRemovalPoints = task.site.dupRemovalPointImpls;
 			if (dupRemovalPoints != null && !dupRemovalPoints.isEmpty()){
 				for (DupRemovalPoint point : dupRemovalPoints){
 //					point.context(task, newUrls);
 					validTasks = point.removeDuplicateTask(task, newUrls, validTasks);
 				}
 			}
 			
 			if (newUrls != null && !newUrls.isEmpty())
 				this.listener.onDupRemoval(Thread.currentThread(), task, validTasks);
 			
 			if (validTasks == null)
 				validTasks = new ArrayList<Task>();
 			
 			//扩展点：task_sort 给任务排序
 			Collection<TaskSortPoint> taskSortPoints = task.site.taskSortPointImpls;
 			if (taskSortPoints != null && !taskSortPoints.isEmpty()){
 				for (TaskSortPoint point : taskSortPoints){
 					validTasks = point.sortTasks(validTasks);
 				}
 			}
 			
 			if (validTasks == null)
 				validTasks = new ArrayList<Task>();
 			
 			//扩展点：task_push 将任务放入队列
 			validTasks = pushTask(validTasks);
 			
 			if (validTasks != null && !validTasks.isEmpty())
 				this.listener.onNewTasks(Thread.currentThread(), task, validTasks);
 			
 			Page page = result.getPage();
 			if (page == null) {
 				listener.onInfo(Thread.currentThread(), task, " spider stop cause the fetch result.page is null");
 				return ;
 			}
 			//扩展点：target 确认当前的Task.url符不符合目标期望
 			Target target = null;
 			Collection<TargetPoint> targetPoints = task.site.targetPointImpls;
 			if (targetPoints != null && !targetPoints.isEmpty()){
 				for (TargetPoint point : targetPoints){
 //					point.context(task);
 					target = point.confirmTarget(task, target);
 				}
 			}
 			
 			if (target == null) {
 //				listener.onInfo(Thread.currentThread(), task, " spider stop cause the task is not the target");
 				return ;
 			}
 			
 			this.listener.onTargetPage(Thread.currentThread(), task, page);
 			
 			//扩展点：parse 把已确认好的目标页面解析成为Map对象
 			List<Map<String, Object>> models = null;
 			Collection<ParsePoint> parsePoints = task.site.parsePointImpls;
 			if (parsePoints != null && !parsePoints.isEmpty()){
 				for (ParsePoint point : parsePoints){
 //					point.context(task, target, page);
 					models = point.parse(task, target, page, models);
 				}
 			}
 			
 			if (models == null) 
 				return ;
 			
 			for (Map<String,Object> model : models)
 				model.put("task_url", task.url);
 			
 			// 统计任务完成数+1
 			this.task.site.counter.plus();
 			listener.onParse(Thread.currentThread(), task, models);
 			
 			//扩展点：pojo 将Map数据映射为POJO
 			String modelCls = target.getModel().getClazz();
 			Class<?> cls = null;
 			if (modelCls != null)
 				cls = Class.forName(modelCls);
 			
 			List<Object> pojos = null;
 			Collection<PojoPoint> pojoPoints = task.site.pojoPointImpls;
 			if (pojoPoints != null && !pojoPoints.isEmpty()){
 				for (PojoPoint point : pojoPoints){
					point.context(task, cls, models);
					pojos = point.mapping(pojos);
 				}
 			}
 			if (pojos != null)
 				listener.onPojo(Thread.currentThread(), task, pojos);
 			//扩展点：end 蜘蛛完成工作，该收尾了
 			Collection<EndPoint> endPoints = task.site.endPointImpls;
 			if (endPoints != null && !endPoints.isEmpty()){
 				for (EndPoint point : endPoints){
 //					point.context(task, models);
 					models = point.complete(task, models);
 				}
 			}
 			
 		} catch (DoneException e){
 			this.listener.onInfo(Thread.currentThread(), task, "Spiderman has shutdown already...");
 		} catch(Exception e){
 			if (this.listener != null)
 				this.listener.onError(Thread.currentThread(), task, CommonUtil.getExceptionString(e), e);
 		}
 	}
 
 	public Collection<Task> pushTask(Collection<Task> validTasks) throws Exception {
 		Collection<TaskPushPoint> taskPushPoints = task.site.taskPushPointImpls;
 		if (taskPushPoints != null && !taskPushPoints.isEmpty()){
 			for (TaskPushPoint point : taskPushPoints){
 				validTasks = point.pushTask(validTasks);
 			}
 		}
 		return validTasks;
 	}
 
 }
