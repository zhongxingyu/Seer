 package br.com.zeng.dao;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Session;
 import org.joda.time.DateTime;
 
 import br.com.caelum.vraptor.ioc.Component;
 import br.com.zeng.chart.QuantityOfTasksPerMonth;
 import br.com.zeng.chart.UserTasksPerMonth;
 import br.com.zeng.model.Project;
 import br.com.zeng.model.Task;
 import br.com.zeng.model.User;
 
 @Component
 public class TaskDao {
 	public static final int MANY_TASKS = 3;
 	private final Session session;
 	private GenericDao<Task> dao;
 
 	public TaskDao(Session session) {
 		this.session = session;
 		dao = new GenericDao<Task>(session, Task.class);
 	}
 
 	public void insert(Task task) {
 		dao.insert(task);
 	}
 
 	public Task getWithId(Long id) {
 		return dao.getById(id);
 	}
 	
 	public void update(Task task) {
 		session.update(task);
 	}
 
 	public void archive(Task task) {
 		task.setArchived(true);
 		update(task);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Task> getTasksWithContentInAProject(String content, Long projectId){
		List<Task> tasks = session.createQuery("from Task t where (t.name like :content or t.description like :content) and t.taskList.category.project.id like :project")
 							.setString("content", "%"+content+"%")
 							.setLong("project", projectId)
 							.list();
 		return tasks;
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean manyTasksWithSameExpirationDate(Project project) {
 		List<Long> list = session.createQuery("select count(*) from Task t where t.expirationDate != null and t.taskList.category.project.url like :project and t.state != 2 group by year(t.expirationDate),month(t.expirationDate)")
 								.setString("project", project.getUrl())
 								.list();
 		for (Long numberOfTasks : list) {
 			if(numberOfTasks>=MANY_TASKS) return true;
 		}
 		return false;
 	}
 	
 	public boolean manyTasksInAProjectWith(DateTime expirationDate, Project project) {
 		Long numberOfTasks = (Long) session.createQuery("select count(*) from Task t where t.expirationDate = :expirationDate and t.taskList.category.project.url like :project and t.state != 2")
 				.setString("project", project.getUrl())
 				.setParameter("expirationDate", expirationDate)
 				.uniqueResult();
 		return numberOfTasks>=MANY_TASKS;
 	}
 
 	public List<UserTasksPerMonth> getQuantityOfTasksGroupedByDateAndUser(Project project){
 		List<User> contributors = project.getContributors();
 		ArrayList<UserTasksPerMonth> userTasksPerMonthList = new ArrayList<UserTasksPerMonth>();
 		for (User user : contributors) {
 			Map<DateTime, Long> quantityOfTasksOfUserByMonth = getQuantityOfTasksOfUserByMonth(user, project);
 			UserTasksPerMonth userTasksPerMonth = new UserTasksPerMonth(user, quantityOfTasksOfUserByMonth);
 			if(userTasksPerMonth.getQuantityOfTasks()!=0){
 				userTasksPerMonthList.add(userTasksPerMonth);
 			}
 		}
 		return userTasksPerMonthList;
 	}
 
 	@SuppressWarnings("unchecked")
 	private Map<DateTime, Long> getQuantityOfTasksOfUserByMonth(User user, Project project){
 		List<QuantityOfTasksPerMonth> list = session.createQuery("select new "+QuantityOfTasksPerMonth.class.getName()+
 				"	(year(t.dateOfCompletion), month(t.dateOfCompletion), count(*))" +
 				"	from Task t where t.dateOfCompletion != null " +
 				"	and t.state=2 " +
 				"	and :user in elements(t.contributors) " +
 				"	and t.taskList.category.project = :project " +
 				"	group by year(t.dateOfCompletion), month(t.dateOfCompletion)")
 					.setParameter("user", user)
 					.setParameter("project", project)
 					.list();
 		HashMap<DateTime, Long> tasksPerMonthMap = new HashMap<DateTime, Long>();
 		for (QuantityOfTasksPerMonth tasksPerMonth : list) {
 			tasksPerMonthMap.put(tasksPerMonth.getMonth(), tasksPerMonth.getQuantityOfTasks());
 		}
 		return tasksPerMonthMap;
 	}
 	
 	public void finalize(Task task) {
 		task.finalize();
 		update(task);
 	}
 
 	public void start(Task task) {
 		task.start();
 		update(task);
 	}
 
 	public void stop(Task task) {
 		task.stop();
 		update(task);
 	}
 
 }
