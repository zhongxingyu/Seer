 package net.madz.download.engine;
 
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.madz.core.common.Dumper;
 import net.madz.core.lifecycle.impl.TransitionInvocationHandler;
 import net.madz.core.lifecycle.meta.StateMachineMetaData;
 import net.madz.core.lifecycle.meta.impl.StateMachineMetaDataBuilderImpl;
 import net.madz.core.verification.VerificationFailureSet;
 import net.madz.download.LogUtils;
 import net.madz.download.engine.IDownloadProcess.StateEnum;
 import net.madz.download.engine.IDownloadProcess.TransitionEnum;
 import net.madz.download.service.requests.CreateTaskRequest;
 import net.madz.download.service.services.CreateTaskService;
 
 import org.junit.Test;
 
 public class StateMachineTest {
 
     @Test
     public void should_in_right_state_after_transition() {
         final Dumper dumper = new Dumper(System.out);
         final StateMachineMetaData<IDownloadProcess, StateEnum, TransitionEnum> machineMetaData = testBuildStateMachineMetaData(dumper);
         final DownloadProcess process = createSampleProcess();
         testTransition(dumper, process, machineMetaData);
     }
    @Test
    public void pause_and_restart_task() {
        final Dumper dumper = new Dumper(System.out);
        final StateMachineMetaData<IDownloadProcess, StateEnum, TransitionEnum> machineMetaData = testBuildStateMachineMetaData(dumper);
        final DownloadProcess process = createSampleProcess();
        testTransition_with_pause_restart(dumper, process, machineMetaData);
    }
     private StateMachineMetaData<IDownloadProcess, StateEnum, TransitionEnum> testBuildStateMachineMetaData(Dumper dumper) {
         dumper.println("");
         dumper.println("Dumping State Machine Meta Data");
         dumper.println("");
         final StateMachineMetaDataBuilderImpl builder = new StateMachineMetaDataBuilderImpl(null, "StateMachine");
         @SuppressWarnings("unchecked")
         final StateMachineMetaData<IDownloadProcess, StateEnum, TransitionEnum> machineMetaData = (StateMachineMetaData<IDownloadProcess, StateEnum, TransitionEnum>) builder
                 .build(null, IDownloadProcess.class);
         machineMetaData.dump(dumper);
         VerificationFailureSet verificationSet = new VerificationFailureSet();
         machineMetaData.verifyMetaData(verificationSet);
         verificationSet.dump(dumper);
         return machineMetaData;
     }
 
     private DownloadProcess createSampleProcess() {
         CreateTaskRequest r = new CreateTaskRequest();
         r.setUrl("https://github-central.s3.amazonaws.com/mac/GitHub%20for%20Mac%2053.zip");
         r.setFilename("git.zip");
         r.setFolder("/Users/tracy/Downloads/demo");
         final DownloadProcess process = new DownloadProcess(r);
         final List<IDownloadProcess> list = new ArrayList<IDownloadProcess>();
         list.add(process);
         return process;
     }
 
     private void testTransition(Dumper dumper, final DownloadProcess process,
             final StateMachineMetaData<IDownloadProcess, StateEnum, TransitionEnum> machineMetaData) {
         dumper.println("");
         dumper.println("Test Transition");
         dumper.println("");
         @SuppressWarnings({ "rawtypes", "unchecked" })
         IDownloadProcess iProcess = (IDownloadProcess) Proxy.newProxyInstance(StateMachineTest.class.getClassLoader(), new Class[] { IDownloadProcess.class },
                 new TransitionInvocationHandler(process));
         process.setProxy(iProcess);
         dumper.print("From = ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
         iProcess.prepare();
         dumper.print("To   = ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
         dumper.print("From = ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
         iProcess.start();
         dumper.print("To   = ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
         dumper.print("From 1111= ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
         synchronized (process) {
             try {
                if ( process.getReceiveBytes() != process.getTask().getTotalLength() ) {
                     process.wait();
                 }
                 iProcess.finish();
                 
             } catch (InterruptedException ignored) {
                 LogUtils.error(CreateTaskService.class, ignored);
             }
         }
         dumper.print("To 2222  = ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
         dumper.print("From  3333 = ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
         iProcess.remove(true);
         dumper.print("To  444 = ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
     }
     private void testTransition_with_pause_restart(Dumper dumper, DownloadProcess process, StateMachineMetaData<IDownloadProcess, StateEnum, TransitionEnum> machineMetaData) {
         dumper.println("");
         dumper.println("Test Transition");
         dumper.println("");
         @SuppressWarnings({ "rawtypes", "unchecked" })
         IDownloadProcess iProcess = (IDownloadProcess) Proxy.newProxyInstance(StateMachineTest.class.getClassLoader(), new Class[] { IDownloadProcess.class },
                 new TransitionInvocationHandler(process));
         process.setProxy(iProcess);
         dumper.print("From = ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
         iProcess.prepare();
         dumper.print("To   = ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
         dumper.print("From = ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
         iProcess.start();
         dumper.print("To   = ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
         try {
             Thread.sleep(2000);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         
         iProcess.pause();
         dumper.print("To   = ");
         machineMetaData.getStateMetaData((StateEnum) iProcess.getState()).dump(dumper);
         
     }
 }
