 // Copyright (c) 2011 Martin Ueding <dev@martin-ueding.de>
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
 
 /**
  * In order to make the user interface faster, the writing of unneeded images is
  * put into a second thread. That way, the UI does not freeze during the little
  * IO time.
  */
 public class WriteoutThread extends Thread {
 	private LinkedBlockingQueue<ImageSwapTask> tasks;
 
 	/**
 	 * Creates and starts the thread.
 	 */
 	public WriteoutThread() {
 		tasks = new LinkedBlockingQueue<ImageSwapTask>();
 		this.start();
 	}
 
 	/**
 	 * Schedules a new task.
 	 * @param t task to schedule
 	 */
 	public void schedule(ImageSwapTask t) {
 		try {
 			tasks.put(t);
 		}
 		catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void run() {
 		ImageSwapTask task;
 		while (!stopAfterLastItem || !tasks.isEmpty()) {
 			try {
 				if (stopAfterLastItem) {
 					task = tasks.poll();
 				}
 				else {
 					System.out.println("waiting for task in queue");
 					task = tasks.take();
 				}
 
 				if (task.getImg() != null) {
 					System.out.println("writing out " + task.getOutfile().getCanonicalPath());
 					javax.imageio.ImageIO.write(task.getImg(), "png", new FileOutputStream(task.getOutfile()));
 				}
 			}
 			catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 		}
 		System.out.println("thread dies");
 	}
 
 	private boolean stopAfterLastItem = false;
 
 	/**
 	 * Stops the thread's daemon mode and lets it die when the queue is empty.
 	 */
 	public void stopAfterLast() {
 		stopAfterLastItem = true;
 		schedule(new ImageSwapTask(null, null));
 	}
 }
