 package org.kalibro.core.processing;
 
 import java.io.File;
 import java.util.SortedSet;
 
 import org.kalibro.*;
 import org.kalibro.core.concurrent.Producer;
 import org.kalibro.core.concurrent.TaskListener;
 import org.kalibro.core.concurrent.TaskReport;
 import org.kalibro.core.concurrent.VoidTask;
 import org.kalibro.core.persistence.DatabaseDaoFactory;
 import org.kalibro.core.persistence.ProcessingObserverDatabaseDao;
 import org.kalibro.dao.DaoFactory;
 
 /**
  * Performs a {@link Processing} for a {@link Repository} according to its {@link Configuration}.
  * 
  * @author Carlos Morais
  */
 public class ProcessTask extends VoidTask implements TaskListener<Void>, Observable {
 
 	File codeDirectory;
 	Repository repository;
 	Processing processing;
 	DatabaseDaoFactory daoFactory;
 	Producer<NativeModuleResult> resultProducer;
 	SortedSet<ProcessingObserver> observers;

 	public ProcessTask(Repository repository) {
 		this.repository = repository;
		setObservers();
 	}

 	public void setObservers() {
		ProcessingObserverDatabaseDao processingObserverDatabaseDao =
 			(ProcessingObserverDatabaseDao) DaoFactory.getProcessingObserverDao();
 		this.observers = processingObserverDatabaseDao.
 			observersOf(repository.getId());
 	}
 
 	@Override
 	protected void perform() {
 		daoFactory = new DatabaseDaoFactory();
 		processing = daoFactory.createProcessingDao().createProcessingFor(repository);
 		resultProducer = new Producer<NativeModuleResult>();
 		new LoadingTask().prepare(this).execute();
 		new CollectingTask().prepare(this).executeInBackground();
 		ProcessSubtask analyzingTask = new AnalyzingTask().prepare(this);
 		analyzingTask.execute();
 	}
 
 	@Override
 	public synchronized void taskFinished(TaskReport<Void> report) {
 		processing.setStateTime(getTaskState(report), report.getExecutionTime());
 		if (processing.getState().isTemporary())
 			updateState(report);
		if (! processing.getState().isTemporary())
 			notifyObservers();

 		daoFactory.createProcessingDao().save(processing, repository.getId());
 	}
 
 	@Override
 	public void notifyObservers() {
 		for (ProcessingObserver observer : observers) {
 			observer.update(repository, processing.getState());
 		}
 	}
 
 	private void updateState(TaskReport<Void> report) {
 		if (report.isTaskDone())
 			processing.setState(getTaskState(report).nextState());
 		else
 			processing.setError(report.getError());
 	}

 	private ProcessState getTaskState(TaskReport<Void> report) {
 		String taskClassName = report.getTask().getClass().getSimpleName();
 		return ProcessState.valueOf(taskClassName.replace("Task", "").toUpperCase());
 	}
 }
