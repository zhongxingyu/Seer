 package org.kevoree.monitoring.comp.monitor;
 
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.framework.MessagePort;
 import org.kevoree.library.defaultNodeTypes.context.KevoreeDeployManager;
 import org.kevoree.microsandbox.api.communication.ComposeMonitoringReport;
 import org.kevoree.microsandbox.api.communication.MonitoringReporterFactory;
 import org.kevoree.microsandbox.api.contract.PlatformDescription;
 import org.kevoree.microsandbox.api.event.MicrosandboxEvent;
 import org.kevoree.monitoring.communication.MicrosandboxEventListener;
 import org.kevoree.monitoring.communication.MicrosandboxReporter;
 import org.kevoree.monitoring.ranking.ComponentRankerFunctionFactory;
 import org.kevoree.monitoring.ranking.ComponentsInfoStorage;
 import org.kevoree.monitoring.ranking.ModelRankingAlgorithm;
 import org.kevoree.monitoring.sla.GlobalThreshold;
 import org.kevoree.monitoring.strategies.AbstractMonitoringTask;
 import org.kevoree.monitoring.strategies.MonitoringTask;
 import org.kevoree.monitoring.strategies.MonitoringTaskAllComponents;
 import org.kevoree.monitoring.strategies.monitoring.FineGrainedStrategyFactory;
 
 /**
  * Created with IntelliJ IDEA.
  * User: inti
  * Date: 6/11/13
  * Time: 2:57 PM
  *
  */
 @Requires( {
  @RequiredPort(name = "output" , type = PortType.MESSAGE, optional = true),
  @RequiredPort(name = "reasoner", type = PortType.MESSAGE,
          className = MicrosandboxEvent.class, optional = true)
 })
 @DictionaryType( {
         @DictionaryAttribute(name = "memory_threshold", defaultValue = "60"),
         @DictionaryAttribute(name = "cpu_threshold", defaultValue = "70"),
         @DictionaryAttribute(name = "net_in_threshold", defaultValue = "80"),
         @DictionaryAttribute(name = "net_out_threshold", defaultValue = "80"),
         @DictionaryAttribute(name = "io_in_threshold", defaultValue = "80"),
         @DictionaryAttribute(name = "io_out_threshold", defaultValue = "80"),
 
         // indicates that we want adaptive monitoring
         @DictionaryAttribute(name = "adaptiveMonitoring", defaultValue = "true"),
 
         // indicates the kind of fine-grained monitoring
         @DictionaryAttribute(name = "fineGrainedStrategy", defaultValue = "all-components"), // the other is single-monitoring
 
         // indicate the function used to rank components
         @DictionaryAttribute(name ="componentRankFunction", defaultValue = "amount_of_time_alive")
 }
 )
 @ComponentType
 public class MonitoringComponent extends AbstractComponentType implements MicrosandboxEventListener {
     AbstractMonitoringTask monitoringTask;
 
     private ModelRankingAlgorithm modelRanker;
 
     @Start
     public void startComponent() {
         double cpu = Double.valueOf(getDictionary().get("cpu_threshold").toString());
         double memory = Double.valueOf(getDictionary().get("memory_threshold").toString());
         double net_received = Double.valueOf(getDictionary().get("net_in_threshold").toString());
         double net_sent = Double.valueOf(getDictionary().get("net_out_threshold").toString());
         double io_read = Long.valueOf(getDictionary().get("io_in_threshold").toString());
         double io_write = Long.valueOf(getDictionary().get("io_out_threshold").toString());
 
         if (getDictionary().get("componentRankFunction") != null && getDictionary().get("componentRankFunction").equals("model_history")) {
            modelRanker = new ModelRankingAlgorithm(getModelService(), getBootStrapperService(), ComponentsInfoStorage.instance);
             ComponentRankerFunctionFactory.instance$.setModelRanker(modelRanker);
             getModelService().registerModelListener(modelRanker);
         }
 
         PlatformDescription description = null;
         for (String key : KevoreeDeployManager.instance$.getInternalMap().keySet())
             if (key.contains("_platformDescription")) {
                 description = (PlatformDescription) KevoreeDeployManager.instance$.getInternalMap().get(key);
                 break;
             }
         if (description == null) {
             System.out.println("panic: Why the platform description isn't here?");
             System.exit(0);
         }
 
         boolean adaptiveMonitoring = Boolean.valueOf(getDictionary().get("adaptiveMonitoring").toString());
         String componentRankFunction = getDictionary().get("componentRankFunction").toString();
 
         if (MonitoringReporterFactory.reporter() instanceof ComposeMonitoringReport) {
             ((ComposeMonitoringReport)MonitoringReporterFactory.reporter()).addReporter(
                     new MicrosandboxReporter(this));
         }
 
         if (adaptiveMonitoring) {
             FineGrainedStrategyFactory.instance$.init(getDictionary().get("fineGrainedStrategy").toString());
 
             GlobalThreshold globalThreshold = new GlobalThreshold(cpu,memory,
                                                                     net_received, net_sent,
                                                                     io_read, io_write,description);
             monitoringTask = new MonitoringTask(getNodeName(),
                     globalThreshold,
                     componentRankFunction,
                     getModelService(),
                     getBootStrapperService());
 
             getModelService().registerModelListener(monitoringTask);
         }
         else {
             monitoringTask = new MonitoringTaskAllComponents(getNodeName(),
                                 componentRankFunction,
                                 getModelService(),
                                 getBootStrapperService());
             getModelService().registerModelListener(monitoringTask);
         }
         new Thread(monitoringTask).start();
     }
 
     @Stop
     public void stopComponent() {
         monitoringTask.stop();
         getModelService().unregisterModelListener(modelRanker);
     }
 
     @Update
     public void updateComponent() {
         stopComponent();
         startComponent();
     }
 
     @Override
     public void notifyEvent(MicrosandboxEvent monitoringEvent) {
         if (isPortBinded("reasoner")) {
             MessagePort port = getPortByName("reasoner", MessagePort.class);
             port.process(monitoringEvent);
         }
     }
 }
