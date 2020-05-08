 package taskdispatcher.cluster;
 
 import taskdispatcher.AbstractTaskDispatcher;
 import taskdispatcher.AbstractTaskRunner;
 import taskdispatcher.Job;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 /**
  * An implementation of the AbstractTaskRunner for use by the ClusterDispatcher.
  * These runners should match one to one with each ClusterStub running on 
  * remote machines. As such each runner represents several processing elements.
  * 
  * If communication between the Runner and the Stub goes down then this Runner
  * will cease execution, but may be started again by resetting the socket and starting
  * in another thread if the remote machine tries to reconnect.
  * 
  * The Thread inside this runner will do all the listening, the thread used by the
  * dispatcher does the sending.
  * 
  * @param <J> The type of Job to be sent and received.
  * @author gg32
  */
 public class ClusterTaskRunner<J extends Job> extends AbstractTaskRunner<J> {
 
 	/**
 	 * A monitor panel that lives in a GUI that will need to be updated with the
 	 * progress of this Runner.
 	 */
 	protected MachinePanel monitor;
 
 	private Socket socket;
 	private ObjectInputStream in;
 	private ObjectOutputStream out;
 	private Class<?> clazz;
 
 	/**
 	 * Create a new ClusterTask runner to run jobs for the given TaskDispatcher.
 	 * @param atd The Dispatcher that this runner works for.
 	 */
     public ClusterTaskRunner(AbstractTaskDispatcher<?,?> atd) {
         super(atd);
     }
 
     /**
      * Setup this Runner to listen and send on the given socket.
      * @param s The socket for this runner to use.
      */
     public void setUP(Socket s) {
         try {
             socket = s;
 			in = new ObjectInputStream(s.getInputStream());
 			//Read in one object to make sure the connections are up okay.
 			in.readObject();
 			out = new ObjectOutputStream(s.getOutputStream());
         } catch (IOException | ClassNotFoundException e) {
             e.printStackTrace();
         }
     }
     
     /**
      * Returns the hostname of the machine that this runner is conencted to.
      * @return The hostname of the machine that this runner is conencted to.
      */
     public String getHostName(){
         if(socket!=null){
             return socket.getInetAddress().getHostName();
         }
         else{
             return null;
         }
     }
 
     /**
      * Tell the remote machine that we are done. The remote machine should then
      * then report a clean shutdown, then this Runner will end.
      */
     @Override
     public void shutdown() {
 		try{
 			out.writeObject(ClusterCommunicationTypes.FINISHED);
 			out.flush();
 		}
 		catch(IOException e){
 			e.printStackTrace();
 		}
     }
 
     /**
      * Add a new task to this Runner. This task is then sent to the remote machine,
      * regardless of how busy it is.
      * @param job The job to be added.
      */
     @Override
     public void addTask(J job) {
 		clazz=job.getClass();
         if(monitor!=null)monitor.assignedJob();
 		try{
 			jobs.put(job.getID(), job);
 			out.writeObject(ClusterCommunicationTypes.NEW_JOB);
 			out.writeObject(job);
 	        out.flush();
 		}
 		catch(IOException e){
 			e.printStackTrace();
 		}
     }
 
     /**
      * Read in a finished job and then report back to the dispatcher that it has
      * been completed. This job is then removed from the jobs collection and
      * added to the finished jobs collection.
      * @throws IOException 
      */
     private void finishedJob() throws IOException,ClassNotFoundException {
 		Object o = in.readObject();
 		J job=null;
 		if(clazz.isInstance(o))
 			job = (J)o;
 		else
 			throw new ClassCastException("Wrong class found");
         
         String jobID = job.getID();
         jobs.remove(jobID);
         finishedJobs.put(jobID, job);
         dispatcher.jobCompleted(jobID);
     }
 
     /**
      * Listen for messages from the ClusterStub until the ClusterStub finishes.
      * Other methods are used based upon the message type.
      */
     @Override
     public void run() {
         setAlive(true);
         if(monitor!=null)monitor.setAlive(true);
         boolean error = false;
         try {
             Object line;
             outer:
             while (!error) {
                 try {
                     while ((line = in.readObject()) != null) {
 						ClusterCommunicationTypes mType;
 						if(line instanceof ClusterCommunicationTypes)
 							mType = (ClusterCommunicationTypes)line;
 						else
 							throw new ClassCastException("Expected a ClusterCommunicationType");
                         switch (mType) {
                             case START_UP: {
 								line = in.readObject();
 								if(line instanceof Integer)
 									maxJobs = (Integer)line;
                                 break;
                             }
                             case FINISHED_JOB: {
                                 if(monitor!=null)monitor.finishJob();
                                 finishedJob();
                                 break;
                             }
                             case KEEP_ALIVE: {
                                 break;
                             }
                             case FINISHED: {
                                 break outer;
                             }
                             case JOB_FAILED: {
 								String jid = null;
 								line = in .readObject();
 								if(line instanceof String)
 									jid = (String) line;
 								String problem = null;
 								line = in.readObject();
 								if(line instanceof String)
 									problem = (String)line;
                                 jobFailed(jid,problem);
                                 break;
                             }
                         }
                     }
                     error = true;
 
                 } catch (InterruptedIOException e) {
                     e.printStackTrace();
                     error = true;
                 } finally {
                     out.writeObject(ClusterCommunicationTypes.KEEP_ALIVE);
                     out.flush();
                 }
             }
             out.close();
             in.close();
             socket.close();
 
         } catch (IOException e) {
             if(monitor!=null)monitor.setAlive(false);
         }
 		catch(ClassCastException e){
 			e.printStackTrace();
 		}
 		catch(ClassNotFoundException e){
 			e.printStackTrace();
 		}
         finally{
             setAlive(false);
             if(monitor!=null)monitor.setAlive(false);
         }
     }
 }
