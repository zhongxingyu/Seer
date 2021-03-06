 package org.nutz.ztask.impl.mongo;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.nutz.lang.Each;
 import org.nutz.lang.Lang;
 import org.nutz.lang.Strings;
 import org.nutz.lang.util.NutMap;
 import org.nutz.lang.util.ObjFilter;
 import org.nutz.mongo.MongoConnector;
 import org.nutz.mongo.util.MCur;
 import org.nutz.mongo.util.MKeys;
 import org.nutz.mongo.util.Moo;
 import org.nutz.ztask.Err;
 import org.nutz.ztask.ZTasks;
 import org.nutz.ztask.api.GInfo;
 import org.nutz.ztask.api.TaskQuery;
 import org.nutz.ztask.api.TaskStack;
 import org.nutz.ztask.api.Task;
 import org.nutz.ztask.api.TaskService;
 import org.nutz.ztask.api.TaskStatus;
 
 public class MongoTaskService extends AbstractMongoService implements TaskService {
 
 	public MongoTaskService(MongoConnector conn, String dbname) {
 		super(conn, dbname);
 	}
 
 	@Override
 	public Task getTask(String taskId) {
 		if (Strings.isBlank(taskId))
 			return null;
 		return dao.findById(Task.class, taskId);
 	}
 
 	@Override
 	public Task getTopTask(String taskId) {
 		Task t = getTask(taskId);
 		if (null == t || t.isTop())
 			return t;
 		return getTopTask(t.getParentId());
 	}
 
 	@Override
 	public Task addComment(String taskId, String comment) {
 		Task t = checkTask(taskId);
 		dao.updateById(Task.class, t.get_id(), Moo.NEW().push("comments", comment));
 		return t;
 	}
 
 	@Override
 	public Task checkTopTask(String taskId) {
 		Task t = checkTask(taskId);
 		if (t.isTop())
 			return t;
 		return checkTopTask(t.getParentId());
 	}
 
 	@Override
 	public List<Task> queryTasks(TaskQuery tq) {
 		Moo q = Moo.NEW();
 		MCur mcur = MCur.NEW();
 
 		// 设置查询和排序条件
 		_setupQuery(tq == null ? TaskQuery.NEW() : tq, q, mcur);
 
 		// 返回结果
 		return dao.find(Task.class, q, mcur);
 	}
 
 	@Override
 	public void each(Each<Task> callback, TaskQuery tq) {
 		// 设置查询条件
 		Moo q = null == tq ? null : Moo.NEW();
 		MCur mcur = null == tq ? null : MCur.NEW();
 		if (null != tq) {
 			_setupQuery(tq, q, mcur);
 		}
 		// 开始迭代
 		dao.each(callback, Task.class, q, mcur);
 	}
 
 	@Override
 	public List<Task> getTasksInStack(String stackName) {
 		return getTasksInStack(checkStack(stackName), null);
 	}
 
 	@Override
 	public List<Task> getTasksInStack(TaskStack stack, TaskStatus st) {
 		Moo q = Moo.NEW().append("stack", stack.getName());
 		if (null != st)
 			q.append("status", st);
 		return dao.find(Task.class, q, MCur.NEW().desc("_id"));
 	}
 
 	@Override
 	public List<Task> getTasks(Task p, TaskStatus st) {
 		Moo q = Moo.NEW();
 		// 限定状态
 		if (null != st)
 			q.append("status", st);
 		// 设定父任务，如果 p 为 null，则获取顶层任务
 		q.append("parentId", null == p ? null : p.get_id());
 		// 查询
 		return dao.find(Task.class, q, MCur.NEW().desc("_id"));
 	}
 
 	@Override
 	public List<Task> getTopNewTasks() {
 		return dao.find(Task.class,
 						Moo.OR(	Moo.NEW("parentId", null).append("status", TaskStatus.NEW),
 								Moo.NEW("parentId", null).gt("number." + Task.I_NEW, 0)),
 						MCur.NEW().desc("_id"));
 	}
 
 	@Override
 	public List<TaskStack> getAllStacks() {
 		return dao.find(TaskStack.class, null, null);
 	}
 
 	@Override
 	public void eachStack(Each<TaskStack> callback) {
 		dao.each(callback, TaskStack.class, null, null);
 	}
 
 	@Override
 	public List<Task> getChildTasks(String taskId) {
 		return dao.find(Task.class, Moo.NEW().append("parentId", taskId), MCur.NEW().asc("text"));
 	}
 
 	@Override
 	public Task loadTaskChildren(Task task) {
 		if (null == task)
 			return null;
 		task.setChildren(getChildTasks(task.get_id()));
 		return task;
 	}
 
 	@Override
 	public List<Task> getLeafTasks(Task task) {
 		return getLeafTasksBy(task, null);
 	}
 
 	@Override
 	public List<Task> getLeafTasks(Task task, final TaskStatus st) {
 		return getLeafTasksBy(task, null == st ? null : new ObjFilter<Task>() {
 			public boolean accept(Task o) {
 				return o.getStatus() == st;
 			}
 		});
 	}
 
 	@Override
 	public List<Task> getLeafTasksBy(Task task, ObjFilter<Task> filter) {
 		List<Task> list = new LinkedList<Task>();
 		for (Task t : getChildTasks(task.get_id())) {
 			// 是叶子
 			if (t.getNumberAll() == 0) {
 				if (null == filter || filter.accept(t))
 					list.add(t);
 			}
 			// 是节点，递归
 			else {
 				list.addAll(getLeafTasksBy(t, filter));
 			}
 		}
 		return list;
 	}
 
 	@Override
 	public Task createTask(Task task) {
 		// 检查标题
 		if (Strings.isBlank(task.getText())) {
 			throw Err.T.BLANK_TASK();
 		}
 		// 保存 parent
 		Task p = getTask(task.getParentId());
 		task.setParentId(null);
 		// 设置创建时间
 		task.setStatus(TaskStatus.NEW);
 		task.setCreateTime(ZTasks.now());
 		task.setLastModified(task.getCreateTime());
		if (!task.isInStack())
			task.setStack(null == p ? null : p.getStack());
 		// 执行创建
 		dao.save(task);
 		// 之后判断一下是否需要 setParent
 		if (null != p) {
 			this._set_task_parent(p, task);
 		}
 		// 返回
 		return task;
 	}
 
 	@Override
 	public Task removeTask(String taskId, boolean recur) {
 		Task t = checkTask(taskId);
 		_remove_task(t, recur);
 		return t;
 	}
 
 	private void _remove_task(Task t, boolean recur) {
 		Task top = checkTopTask(t.get_id());
 		// 处理递归
 		if (recur) {
 			List<Task> subs = this.getChildTasks(t.get_id());
 			if (null != subs)
 				for (Task sub : subs) {
 					_remove_task(sub, true);
 				}
 		}
 		// 不递归，向上一级
 		else {
 			dao.update(	Task.class,
 						Moo.NEW().append("parentId", t.get_id()),
 						Moo.NEW().set("parentId", t.getParentId()));
 		}
 		// 删除本尊并同步一下 Top
 		dao.removeById(Task.class, t.get_id());
 		syncDescendants(top);
 		// 重新计算一下堆栈的数量
 		_recountStackTaskNumber(t.getStack());
 	}
 
 	@Override
 	public Task setTaskOwner(String taskId, String ownerName) {
 		Task t = checkTask(taskId);
 		dao.updateById(Task.class, taskId, Moo.NEW().set("owner", ownerName));
 		t.setOwner(ownerName);
 		return t;
 	}
 
 	@Override
 	public Task setTaskText(String taskId, String newTitle) {
 		Task t = checkTask(taskId);
 		dao.updateById(Task.class, taskId, Moo.NEW().set("text", newTitle));
 		t.setText(newTitle);
 		return t;
 	}
 
 	@Override
 	public Task setTaskLabels(String taskId, String[] labels) {
 		Task t = checkTask(taskId);
 		dao.updateById(Task.class, taskId, Moo.NEW().set("labels", labels));
 		t.setLabels(labels);
 		return t;
 	}
 
 	@Override
 	public List<Task> setTasksParent(String parentId, String... taskIds) {
 		return _set_task_parent(getTask(parentId), checkTasks(taskIds));
 	}
 
 	private List<Task> _set_task_parent(Task p, Task... ts) {
 		String pid = null == p ? null : p.get_id();
 		List<Task> list = new ArrayList<Task>(ts.length);
 		List<String> ids = new ArrayList<String>(ts.length);
 		Map<String, Task> oldps = new HashMap<String, Task>();
 		// 获得需要修改的 Task 列表，以及归纳旧的 parents
 		for (Task t : ts) {
 			if ((null == pid && null != t.getParentId()) || !Lang.equals(pid, t.getParentId())) {
 				list.add(t);
 				ids.add(t.get_id());
 				if (null != t.getParentId())
 					oldps.put(t.getParentId(), null);
 				// 危险，抛错
 				if (Lang.equals(t.get_id(), pid))
 					throw Err.T.SELF_PARENT(pid);
 			}
 		}
 		// 没必要修改的情况
 		if (list.isEmpty())
 			return list;
 
 		// 执行更新
 		for (Task t : ts)
 			t.setParentId(pid);
 		dao.update(Task.class, Moo.NEW().inArray("_id", ids), Moo.NEW().set("parentId", pid));
 
 		// 归纳需要同步的节点
 		Map<String, Task> tops = new HashMap<String, Task>();
 		Task top = null;
 		// 记录新的 Task Parent
 		if (null != p) {
 			top = p.isTop() ? p : this.checkTopTask(p.getParentId());
 			tops.put(top.get_id(), top);
 		}
 		// 归纳旧的 ...
 		for (String id : oldps.keySet()) {
 			top = this.checkTopTask(id);
 			tops.put(top.get_id(), top);
 		}
 		// 重新统计相关的父Task
 		for (Task topTask : tops.values())
 			syncDescendants(topTask);
 
 		// 返回更新了的 Task
 		return list;
 	}
 
 	/**
 	 * 叶子节点优先的递归，根据这个顺序，统计 owner
 	 */
 	@Override
 	public Task syncDescendants(Task task) {
 		// 记录原始的 owner
 		String oldOwner = task.getOwner();
 		/*
 		 * 仅仅查询几个字段
 		 */
 		List<Task> children = dao.find(	Task.class,
 										Moo.NEW().append("parentId", task.get_id()),
 										MKeys.OFF(	"text",
 													"createTime",
 													"lastModified",
 													"pushAt",
 													"startAt",
 													"hungupAt",
 													"popAt",
 													"creater",
 													"labels",
 													"comments"),
 										null);
 		// 准备统计
 		int all = 0, done = 0, ing = 0, nnew = 0, hungup = 0;
 
 		// 不为空，递归
 		if (!children.isEmpty()) {
 			// 循环一下
 			for (Task t : children) {
 				// 递归同步一下自身
 				syncDescendants(t);
 				// 叶子
 				if (t.isLeaf()) {
 					all++;
 					switch (t.getStatus()) {
 					case NEW:
 						nnew++;
 						break;
 					case DONE:
 						done++;
 						break;
 					case ING:
 						ing++;
 						break;
 					case HUNGUP:
 						hungup++;
 						break;
 					default:
 						throw Lang.impossible();
 					}
 				}
 				/*
 				 * 非叶子，加入计数，并且根据叶子节点，改变自身的 owner
 				 */
 				else {
 					all += t.getNumberAll();
 					done += t.getNumberDone();
 					ing += t.getNumberProcessing();
 					nnew += t.getNumberNew();
 					hungup += t.getNumberHungup();
 				}
 			}
 			// 递归自己的 children 完成，那么循环一遍，为每个 children 的 owner 打分
 			// 分值，为每个子任务的 numberAll + 1
 			// 没有 owner 的子任务，不参与评分
 			NutMap weis = new NutMap();
 			for (Task t : children) {
 				int wei = weis.getInt(t.getOwner(), 0);
 				if (!Strings.isBlank(t.getOwner()))
 					weis.put(t.getOwner(), wei + t.getNumberAll() + 1);
 			}
 			// 寻找分值最大的两个 owner
 			if (!weis.isEmpty()) {
 				String ow = null, bow = null;
 				int ow_c = 0, bow_c = 0;
 				for (String u : weis.keySet()) {
 					int wei = weis.getInt(u);
 					if (null == ow) {
 						ow = u;
 						ow_c = wei;
 						bow = u;
 						bow_c = ow_c;
 						continue;
 					}
 					// 可否作为正选
 					if (wei > ow_c) {
 						bow_c = ow_c;
 						bow = ow;
 						ow = u;
 						ow_c = wei;
 					}
 					// 可否作为备选
 					else if (wei > bow_c) {
 						bow = u;
 						bow_c = ow_c;
 					}
 				}
 				// 正选备选一回事儿
 				if (Lang.equals(ow, bow)) {
 					task.setOwner(ow);
 				}
 				// 正选备选权重相同
 				else if (ow_c == bow_c) {
 					// 那么看看谁是 creator
 					if (Lang.equals(task.getCreater(), ow)) {
 						task.setOwner(ow);
 					} else if (Lang.equals(task.getCreater(), bow)) {
 						task.setOwner(bow);
 					}
 					// 如果都不是
 					else {
 						task.setOwner(ow);
 					}
 				}
 				// 则用正选
 				else {
 					task.setOwner(ow);
 				}
 			}
 		}
 		// 更新自身
 		if (task.getNumberAll() != all
 			|| task.getNumberDone() != done
 			|| task.getNumberProcessing() != ing
 			|| task.getNumberNew() != nnew
 			|| task.getNumberHungup() != hungup
 			|| !Lang.equals(oldOwner, task.getOwner())) {
 			// 更新 ...
 			task.setNumber(new int[]{all, done, ing, nnew, hungup});
 			// 设置字段值
 			Moo o = Moo.SET("number", task.getNumber());
 			// 如果不是叶子节点，根据子节点判断状态
 			if (!task.isLeaf()) {
 				// DONE
 				if (task.isChildrenDone()) {
 					task.setStatus(TaskStatus.DONE);
 				}
 				// ING
 				else if (task.isChildrenProcessing()) {
 					task.setStatus(TaskStatus.ING);
 				}
 				// NEW
 				else if (task.isChildrenNew()) {
 					task.setStatus(TaskStatus.NEW);
 				}
 				// 不可能 !!!
 				else {
 					throw Lang.impossible();
 				}
 				// 保证它不在任何堆栈里
 				task.setStack(ZTasks.NULL_STACK);
 			}
 			// 如果是叶子节点，但是不在堆栈里，变成 NEW
 			else if (TaskStatus.DONE != task.getStatus() && !task.isInStack()) {
 				task.setStatus(TaskStatus.NEW);
 			}
 			// 设置到更新对象中
 			o.set("status", task.getStatus());
 			o.set("stack", task.getStack());
 			o.set("owner", task.getOwner());
 			// 执行更新
 			dao.updateById(Task.class, task.get_id(), o);
 		}
 		// 返回
 		return task;
 	}
 
 	@Override
 	public Task pushToStack(String taskId, String stackName) {
 		return pushToStack(checkTask(taskId), checkStack(stackName));
 	}
 
 	@Override
 	public Task pushToStack(Task task, TaskStack stack) {
 		// 已经在栈里，就没必要再执行了
 		if (stack.getName().equals(task.getStack()))
 			return task;
 
 		// 如果不是叶子任务，那么，插入其所有的叶子节点
 		if (!task.isLeaf()) {
 			List<Task> leafs = this.getLeafTasks(task);
 			for (Task leaf : leafs)
 				pushToStack(leaf, stack);
 			return task;
 		}
 
 		// 如果这个任务之前在别的栈里，先弹栈
 		if (task.isInStack())
 			_pop_from_stack(task, false, false);
 
 		// 更新 Java 对象 ...
 		task.setStack(stack.getName());
 		task.setStatus(TaskStatus.ING);
 		task.setPushAt(ZTasks.now());
 		task.setStartAt(task.getPushAt());
 		task.setPopAt(null);
 		task.setHungupAt(null);
 		task.setOwner(stack.getOwner());
 
 		// 压入 ...
 		Moo o = Moo.NEW();
 		o.set("status", task.getStatus());
 		o.set("stack", task.getStack());
 		o.set("pushAt", task.getPushAt());
 		o.set("startAt", task.getStartAt());
 		o.set("popAt", task.getPopAt());
 		o.set("hungupAt", task.getHungupAt());
 		o.set("owner", stack.getOwner());
 		dao.updateById(Task.class, task.get_id(), o);
 
 		// 重新计算一下堆栈的数量
 		_recountStackTaskNumber(stack.getName());
 
 		// 重新计算一下自己所有的 parents 的 number
 		syncDescendants(checkTopTask(task.get_id()));
 
 		// 最后返回
 		return this.getTask(task.get_id());
 	}
 
 	@Override
 	public Task popFromStack(String taskId, boolean done) {
 		return popFromStack(getTask(taskId), done);
 	}
 
 	@Override
 	public Task popFromStack(Task task, boolean done) {
 		if (null != task) {
 			_pop_from_stack(task, done, true);
 		}
 		return null == task ? null : this.getTask(task.get_id());
 	}
 
 	@Override
 	public Task hungupTask(String taskId) {
 		Task t = checkTask(taskId);
 		if (TaskStatus.HUNGUP != t.getStatus()) {
 			t.setStatus(TaskStatus.HUNGUP);
 			Moo o = Moo.SET("status", t.getStatus());
 			o.set("startAt", null);
 			o.set("hungupAt", ZTasks.now());
 			dao.updateById(Task.class, t.get_id(), o);
 		}
 		return t;
 	}
 
 	@Override
 	public Task restartTask(String taskId) {
 		Task t = checkTask(taskId);
 		if (TaskStatus.HUNGUP == t.getStatus()) {
 			t.setStatus(TaskStatus.ING);
 			Moo o = Moo.SET("status", t.getStatus());
 			o.set("startAt", ZTasks.now());
 			o.set("hungupAt", null);
 			dao.updateById(Task.class, t.get_id(), o);
 		}
 		return t;
 	}
 
 	private void _pop_from_stack(Task t, boolean done, boolean syncTop) {
 		// 记录自己所在的堆栈
 		String myStack = t.getStack();
 		// 更新 Java 对象 ...
 		t.setStack(ZTasks.NULL_STACK);
 		t.setStatus(done ? TaskStatus.DONE : TaskStatus.NEW);
 		t.setPushAt(null);
 		t.setStartAt(done ? t.getStartAt() : null);
 		t.setPopAt(ZTasks.now());
 		t.setHungupAt(null);
 		if (!done)
 			t.setOwner(t.getCreater());
 
 		// 压入 ...
 		Moo o = Moo.NEW();
 		o.set("status", t.getStatus());
 		o.set("stack", t.getStack());
 		o.set("pushAt", t.getPushAt());
 		if (null == t.getStartAt())
 			o.set("startAt", t.getStartAt());
 		o.set("popAt", t.getPopAt());
 		o.set("hungupAt", t.getHungupAt());
 		o.set("owner", t.getOwner());
 		dao.updateById(Task.class, t.get_id(), o);
 
 		// 重新计算堆栈 Task
 		_recountStackTaskNumber(myStack);
 
 		// 重新计算一下自己所有的 parents 的 number
 		if (syncTop)
 			syncDescendants(checkTopTask(t.get_id()));
 	}
 
 	private void _recountStackTaskNumber(String stackName) {
 		if (!ZTasks.isBlankStack(stackName)) {
 			int num = (int) dao.count(Task.class, Moo.NEW().append("stack", stackName));
 			dao.update(	TaskStack.class,
 						Moo.NEW().append("name", stackName),
 						Moo.NEW().set("count", num));
 		}
 	}
 
 	@Override
 	public List<TaskStack> getTopStacks() {
 		return getChildStacks(null);
 	}
 
 	@Override
 	public List<TaskStack> getStacksByOwner(String ownerName) {
 		return dao.find(TaskStack.class,
 						Moo.NEW("owner", ownerName),
 						MCur.ASC("parentName").asc("name"));
 	}
 
 	@Override
 	public List<TaskStack> getChildStacks(String stackName) {
 		return dao.find(TaskStack.class, Moo.NEW("parentName", stackName), MCur.ASC("name"));
 	}
 
 	@Override
 	public TaskStack getStack(String stackName) {
 		if (Strings.isBlank(stackName))
 			return null;
 		return dao.findOne(TaskStack.class, Moo.NEW().append("name", stackName));
 	}
 
 	@Override
 	public TaskStack saveStack(TaskStack stack) {
 		stack.setName(Strings.trim(stack.getName()));
 		return dao.save(stack);
 	}
 
 	@Override
 	public TaskStack createStackIfNoExistis(String stackName, String ownerName) {
 		TaskStack s = getStack(stackName);
 		if (null == s) {
 			s = new TaskStack();
 			s.setName(stackName);
 			s.setOwner(ownerName);
 			dao.save(s);
 		} else {
 			dao.updateById(TaskStack.class, s.get_id(), Moo.NEW().set("owner", ownerName));
 			s.setOwner(ownerName);
 		}
 		return s;
 	}
 
 	@Override
 	public TaskStack setStackParent(String stackName, String parentName) {
 		checkStack(parentName);
 		TaskStack s = checkStack(stackName);
 		if (!parentName.equals(s.getParentName())) {
 			s.setParentName(parentName);
 			dao.updateById(TaskStack.class, s.get_id(), Moo.NEW().set("parentName", parentName));
 		}
 		return s;
 	}
 
 	@Override
 	public TaskStack removeStack(String stackName) {
 		Moo q = Moo.NEW().append("name", stackName);
 		// 获取任务堆栈
 		TaskStack ts = dao.findOne(TaskStack.class, q);
 
 		// 修改所有相关任务状态为 NEW, stack 为 ZTasks.NULL_STACK
 		dao.update(	Task.class,
 					Moo.NEW().append("stack", stackName).append("status", TaskStatus.ING),
 					Moo.NEW().set("stack", ZTasks.NULL_STACK).set("status", TaskStatus.NEW));
 
 		// 移除任务堆栈
 		dao.remove(TaskStack.class, q);
 
 		// 返回任务堆栈
 		return ts;
 	}
 
 	@Override
 	public Task checkTask(String taskId) {
 		Task t = getTask(taskId);
 		if (null == t)
 			throw Err.T.NO_EXISTS(taskId);
 		return t;
 	}
 
 	@Override
 	public Task[] checkTasks(String... taskIds) {
 		Task[] list = new Task[taskIds.length];
 		int index = 0;
 		for (String taskId : taskIds)
 			list[index++] = checkTask(taskId);
 		return list;
 	}
 
 	@Override
 	public TaskStack checkStack(String stackName) {
 		TaskStack ts = getStack(stackName);
 		if (null == ts)
 			throw Err.S.NO_EXISTS(stackName);
 		return ts;
 	}
 
 	private void _setupQuery(TaskQuery tq, Moo q, MCur mcur) {
 		// 如果是 ID
 		if (null != tq.qID()) {
 			q.append("_id", tq.qID());
 			return;
 		}
 
 		// 处理时间范围
 		if (null != tq.qTimeScope()) {
 			Date[] ds = tq.qTimeScope();
 			q.d_gte(tq.getSortBy(), ZTasks.D(ds[0]));
 			q.d_lte(tq.getSortBy(), ZTasks.D(ds[1]));
 
 		}
 
 		// 处理 owners
 		if (null != tq.qOwners()) {
 			q.inArray("owner", tq.qOwners());
 		}
 
 		// 处理 creater
 		if (null != tq.qCreaters()) {
 			q.inArray("creater", tq.qCreaters());
 		}
 
 		// 处理标签
 		if (null != tq.qLabels()) {
 			q.all("labels", tq.qLabels());
 		}
 
 		// 处理关键字，这个放在后面，以便提高查询效率
 		if (null != tq.qText()) {
 			if (tq.qText().startsWith("^"))
 				q.startsWith("text", tq.qText().substring(1));
 			else
 				q.contains("text", tq.qText());
 		}
 
 		// 处理正则表达式
 		if (null != tq.qRegex()) {
 			q.match("text", tq.qRegex());
 		}
 
 		// 处理游标排序
 		if (tq.isDESC()) {
 			mcur.desc(tq.getSortBy());
 		} else {
 			mcur.asc(tq.getSortBy());
 		}
 
 		if (tq.limit() > 0)
 			mcur.limit(tq.limit());
 
 		if (tq.skip() > 0)
 			mcur.skip(tq.skip());
 	}
 
 	@Override
 	public GInfo getGlobalInfo() {
 		return dao.findOne(GInfo.class, null);
 	}
 
 	@Override
 	public GInfo setGlobalInfo(GInfo info) {
 		info.setLastModified(ZTasks.now());
 		dao.save(info);
 		return info;
 	}
 
 }
