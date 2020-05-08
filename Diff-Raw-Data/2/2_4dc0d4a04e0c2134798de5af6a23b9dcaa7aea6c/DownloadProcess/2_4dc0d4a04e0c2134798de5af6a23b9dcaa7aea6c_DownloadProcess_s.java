 package net.madz.download.engine;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import net.madz.core.lifecycle.IState;
 import net.madz.download.LogUtils;
 import net.madz.download.service.exception.ErrorException;
 import net.madz.download.service.metadata.DownloadTask;
 import net.madz.download.service.metadata.MetaManager;
 import net.madz.download.service.metadata.Segment;
 import net.madz.download.service.requests.CreateTaskRequest;
 import net.madz.download.service.requests.PauseTaskRequest;
 
 public class DownloadProcess implements IDownloadProcess {
 
     private static final long serialVersionUID = -2404735057674043661L;
     private final DownloadTask task;
     private volatile long receiveBytes;
    private transient File metadataFile;
     private transient File dataFile;
     private transient final ExecutorService receiveUpdateExecutor = Executors.newSingleThreadExecutor();
     private boolean pauseFlag = false;
     private transient List<DownloadSegmentWorker> workers = new LinkedList<DownloadSegmentWorker>();
     private IDownloadProcess proxy;
     private ExecutorService localThreadPool;
 
     public DownloadProcess(CreateTaskRequest request) {
         super();
         this.task = MetaManager.createDownloadTask(request);
         metadataFile = new File("./meta/new/" + request.getFilename() + "_log");
         MetaManager.serializeForNewState(request, metadataFile);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public StateEnum getState() {
         int stateIndex = task.getState();
         StateEnum[] states = StateEnum.values();
         return states[stateIndex];
     }
 
     void setState(StateEnum state) {
         this.task.setState((byte) state.ordinal());
     }
 
     @Override
     public void prepare() {
         MetaManager.serializeForPreparedState(task, metadataFile);
         MetaManager.computeSegmentsInformation(task);
         MetaManager.serializeSegmentsInformation(task, metadataFile);
         metadataFile = MetaManager.move(metadataFile, new File("./meta/prepared"));
         File folder = task.getFolder();
         dataFile = new File(folder, task.getFileName());
         try {
             dataFile.createNewFile();
         } catch (IOException ignored) {
             LogUtils.error(DownloadProcess.class, ignored);
         }
     }
 
     @Override
     public void start() {
         metadataFile = MetaManager.move(metadataFile, new File("./meta/started"));
         try {
             MetaManager.updateTaskState(metadataFile, StateEnum.Started);
         } catch (FileNotFoundException ignored) {
             LogUtils.debug(DownloadProcess.class, ignored.getMessage());
         } catch (IOException ignored) {
             LogUtils.debug(DownloadProcess.class, ignored.getMessage());
         }
         List<Segment> segments = task.getSegments();
         localThreadPool = Executors.newFixedThreadPool(10);
         for ( final Segment segment : segments ) {
             if ( !pauseFlag ) {
                 DownloadSegmentWorker worker = new DownloadSegmentWorker(proxy, task, segment, dataFile, metadataFile);
                 workers.add(worker);
                 localThreadPool.execute(worker);
             }
         }
     }
 
     @Override
     public void receive(final long bytes) {
         receiveUpdateExecutor.submit(new Runnable() {
 
             @Override
             public void run() {
                 DownloadProcess.this.receiveBytes += bytes;
                 synchronized (DownloadProcess.this) {
                     if ( receiveBytes == task.getTotalLength() ) {
                         DownloadProcess.this.notify();
                     }
                 }
             }
         });
     }
 
     @Override
     public void inactivate() {
         if ( (byte) StateEnum.Started.ordinal() == task.getState() ) {
             task.setState((byte) StateEnum.InactiveStarted.ordinal());
             MetaManager.move(metadataFile, new File("./meta/inactiveStarted"));
         } else if ( (byte) StateEnum.Prepared.ordinal() == task.getState() ) {
             task.setState((byte) StateEnum.InactivePrepared.ordinal());
             MetaManager.move(metadataFile, new File("./meta/inactivePrepared"));
         }
         resetProcessWhenNotResumable();
     }
 
     private void resetProcessWhenNotResumable() {
         if ( !task.isResumable() ) {
             try {
                 MetaManager.updateSegmentDownloadProgress(metadataFile, 0, 0);
             } catch (FileNotFoundException ignored) {
                 LogUtils.error(DownloadProcess.class, ignored);
             } catch (IOException ignored) {
                 LogUtils.error(DownloadProcess.class, ignored);
             }
         }
     }
 
     @Override
     public void activate() {
         MetaManager.move(metadataFile, new File("./meta/prepared"));
     }
 
     @Override
     public void pause() {
         this.pauseFlag = true;
         if ( workers.size() > 0 ) {
             for ( DownloadSegmentWorker worker : workers ) {
                 worker.setPauseFlag(true);
             }
         }
         resetProcessWhenNotResumable();
         metadataFile = MetaManager.move(metadataFile, new File("./meta/paused"));
         this.receiveUpdateExecutor.shutdown();
         this.localThreadPool.shutdown();
     }
 
     @Override
     public void finish() {
         System.out.println("============Enter finish===========");
         try {
             MetaManager.updateTaskState(metadataFile, StateEnum.Finished);
         } catch (FileNotFoundException ignored) {
             LogUtils.error(DownloadProcess.class, ignored);
         } catch (IOException ignored) {
             LogUtils.error(DownloadProcess.class, ignored);
         }
         metadataFile = MetaManager.move(metadataFile, new File("./meta/finished"));
         this.receiveUpdateExecutor.shutdown();
         this.localThreadPool.shutdown();
     }
 
     @Override
     public void err() {
         task.setState((byte) StateEnum.Failed.ordinal());
         metadataFile = MetaManager.move(metadataFile, new File("./meta/failed"));
         this.receiveUpdateExecutor.shutdown();
         this.localThreadPool.shutdown();
     }
 
     @Override
     public void remove(boolean both) {
         receiveUpdateExecutor.shutdown();
         dataFile.delete();
         metadataFile.delete();
     }
 
     @Override
     public void restart() {
         // TODO Auto-generated method stub
     }
 
     @Override
     public void resume() {
     }
 
     public synchronized long getReceiveBytes() {
         return receiveBytes;
     }
 
     public synchronized DownloadTask getTask() {
         return task;
     }
 
     public void setProxy(IDownloadProcess proxy) {
         this.proxy = proxy;
     }
 
     @Override
     public String getUrl() {
         return this.task.getUrl().toString();
     }
 
     public synchronized IDownloadProcess getProxy() {
         return proxy;
     }
 }
