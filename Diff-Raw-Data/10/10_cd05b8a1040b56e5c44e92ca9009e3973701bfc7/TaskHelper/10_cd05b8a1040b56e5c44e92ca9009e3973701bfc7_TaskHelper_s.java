 package com.kain.tom.dioe;
 
 import java.util.List;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import com.kain.tom.dioe.database.HibernateUtil;
 
 public class TaskHelper {
 
 	public static Task createTask(String taskName) {
 		Session dbSession = null;
 		try {
 			Task newTask = new Task(taskName);
 			dbSession = HibernateUtil.getSessionFactory().openSession();
 			Transaction transaction = dbSession.beginTransaction();
 			dbSession.save(newTask);
 			transaction.commit();
 			// dbSession.flush();
 			return newTask;
 		} catch (HibernateException he) {
 			he.printStackTrace();
 		} finally {
 			if (dbSession != null) {
 				dbSession.close();
 			}
 		}
 		return null;
 	}
 
 	public static List<Task> getAllTasks() {
 		Session dbSession = null;
 		try {
 			dbSession = HibernateUtil.getSessionFactory().openSession();
 			Query query = dbSession.createQuery("from Task");
 			List<Task> allTasks = query.list();
 			return allTasks;
 		} catch (HibernateException he) {
 			he.printStackTrace();
 		} finally {
 			if (dbSession != null) {
 				dbSession.close();
 			}
 		}
 		return null;
 	}
 
 	public static Task findTaskByName(String taskName) {
 		Session dbSession = HibernateUtil.getSessionFactory().openSession();
 		Query query = dbSession
 				.createQuery("from Task WHERE taskName = :taskName");
 		query.setParameter("taskName", taskName);
 		if (dbSession != null) {
 			dbSession.close();
 		}
 		return (Task) query.uniqueResult();
 	}
 
 	public static Task findTaskByID(int taskID) {
 		Session dbSession = null;
 
 		try {
 			dbSession = HibernateUtil.getSessionFactory().openSession();
 			Query query = dbSession
 					.createQuery("from Task WHERE taskId = :taskID");
 			query.setParameter("taskID", taskID);
 			return (Task) query.uniqueResult();
 		} catch (HibernateException he) {
 			he.printStackTrace();
 		} finally {
 			if (dbSession != null) {
 				dbSession.close();
 			}
 		}
 		return null;
 	}
 
 	public static void markTasksDone(int taskID) {
 		Task taskMarkingDone = findTaskByID(taskID);
 		if (taskMarkingDone != null) {
 			Session dbSession = null;
 			try {
 				taskMarkingDone.setIsDone(true);
 				dbSession = HibernateUtil.getSessionFactory().openSession();
 				Transaction transaction = dbSession.beginTransaction();
 				dbSession.update(taskMarkingDone);
 				transaction.commit();
 			}
 			// dbSession.flush();
 			catch (HibernateException he) {
 				he.printStackTrace();
 			} finally {
 				if (dbSession != null) {
 					dbSession.close();
 				}
 			}
 		}
 	}
 
 	public static void updateTask(Task taskToUpdate) {
 		if (taskToUpdate != null) {
 			Session dbSession = null;
 			try {
 				dbSession = HibernateUtil.getSessionFactory().openSession();
 				Transaction transaction = dbSession.beginTransaction();
				dbSession.update(taskToUpdate);
 				transaction.commit();
 			}
 			catch (HibernateException he) {
 				he.printStackTrace();
 			} finally {
 				if (dbSession != null) {
 					dbSession.close();
 				}
 			}
 		}
 	}
 	
 }
