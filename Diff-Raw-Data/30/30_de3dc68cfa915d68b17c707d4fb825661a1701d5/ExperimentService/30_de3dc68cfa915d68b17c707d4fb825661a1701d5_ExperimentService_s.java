 /*******************************************************************************
  * Copyright (c) 2012 Emanuele Tamponi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele Tamponi - initial API and implementation
  ******************************************************************************/
 package game.main;
 
 import game.Settings;
 import game.core.Experiment;
 import game.core.LongTask.LongTaskUpdate;
 import game.utils.Msg;
 
 import java.util.Observable;
 import java.util.Observer;
 import java.util.concurrent.Executor;
 
import javafx.application.Platform;
 import javafx.beans.property.IntegerProperty;
 import javafx.beans.property.SimpleIntegerProperty;
 import javafx.beans.property.SimpleStringProperty;
 import javafx.beans.property.StringProperty;
 import javafx.concurrent.Service;
 import javafx.concurrent.Task;
 import javafx.event.Event;
 import javafx.event.EventType;
 
 @SuppressWarnings("deprecation")
 public class ExperimentService extends Service<Experiment> {
 	
 	public static final EventType<Event> FINISHED = new EventType<>();
 	
 	private static class SimpleThreadExecutor implements Executor {
 		
 		private Thread thread;
 
 		@Override
 		public void execute(Runnable command) {
			if (thread == null || !thread.isAlive()) {
				thread = new Thread(command);
				thread.start();
			} else {
				new Exception().printStackTrace();
				Platform.exit();
			}
 		}
 		
 		public Thread getThread() {
 			return thread;			
 		}
 		
 	}
 	
 	private SimpleThreadExecutor executor = new SimpleThreadExecutor();
 	
 	private IntegerProperty counter = new SimpleIntegerProperty(0);
 	private StringProperty currentExperiment = new SimpleStringProperty("");
 	
 	private MainController controller;
 	
 	private boolean paused = false;
 	private boolean stopped = false;
 	private boolean finished = true;
 	
 	public ExperimentService(MainController controller) {
 		super();
 		setExecutor(executor);
 		this.controller = controller;
 	}
 	
 	public void pause() {
 		if (!paused) {
 			executor.getThread().suspend();
 			paused = true;
 		}
 	}
 	
 	public void resume() {
 		if (paused) {
 			executor.getThread().resume();
 			paused = false;
 		}
 	}
 	
 	public void stop() {
 		stopped = true;
 		executor.getThread().stop();
 		try {
 			executor.getThread().join();
 		} catch (InterruptedException e) {}
 	}
 	
 	public boolean isPaused() {
 		return paused;
 	}
 	
 	public boolean isStopped() {
 		return stopped;
 	}
 	
	public void start() { 
 		counter.set(0);
 		currentExperiment.set(controller.experimentList.get(0).toString());
 		paused = false;
 		stopped = false;
 		finished = false;
 		
		super.start();
 	}
 	
 	public IntegerProperty counterProperty() {
 		return counter;
 	}
 	
 	public StringProperty currentExperimentProperty() {
 		return currentExperiment;
 	}
 	
 	public boolean hasFinished() {
 		return finished;
 	}
 
 	@Override
 	protected Task<Experiment> createTask() {
 		return new Task<Experiment>() {
 			@Override
 			protected Experiment call() throws Exception {
 				System.gc();
 				System.out.println("Total memory: " + Runtime.getRuntime().totalMemory());
 				System.out.println(" Free memory: " + Runtime.getRuntime().freeMemory());
 				
 				final Experiment e = (Experiment)controller.experimentList.get(counter.get());
 				Msg.setLogPrefix(e.name);
 				Observer o = new Observer() {
 					@Override
 					public void update(Observable obs, Object m) {
 						if (m instanceof LongTaskUpdate) {
 							Msg.info("%6.2f%%: %s", e.getCurrentPercent()*100, e.getCurrentMessage());
 							updateMessage(e.getCurrentMessage());
 							updateProgress((long)(e.getCurrentPercent()*100), 100);
 //							try {
 //								Thread.sleep(100);
 //							} catch (InterruptedException e) {}
 						}
 					}
 				};
 				e.addObserver(o);
 				Experiment ret = e.startExperiment(Settings.RESULTSDIR);
 				e.deleteObserver(o);
 				return ret;
 			}
 		};
 	}
 
 	@Override
 	protected void succeeded() {
 		super.succeeded();
 		Experiment completed = getValue();
 		if (controller.addToResultList())
 			controller.getResultListController().addCompletedExperiment(completed);
 		counter.set(counter.get()+1);
 		if (counter.get() < controller.experimentList.size()) {
 			reset();
 			currentExperiment.set(controller.experimentList.get(counter.get()).toString());
 			start();
 		} else {
 			finished = true;
 			fireEvent(new Event(FINISHED));
 		}
 	}
 	
 }
