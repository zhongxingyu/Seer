 /*
  * Copyright (c) 2007, Mark E. Madsen, Alex Bentley, and Carl P. Lipo. All Rights Reserved.
  *
  * This code is offered for use under the terms of the Creative Commons-GNU General Public License
  * http://creativecommons.org/licenses/GPL/2.0/
  *
  * Our intent in licensing this software under the CC-GPL is to provide freedom for researchers, students,
  * and other interested parties to replicate our research results, pursue their own research, etc.  You are, however,
  * free to use the code contained in this package for whatever purposes you wish, provided you adhere to the
  * open license terms specified in LICENSE and GPL.txt
  *
  * See the files LICENSE and GPL.txt in the top-level directory of this source archive for the license
  * details and grant.
  */
 
 package org.mmadsen.sim.transmissionlab.models;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.commons.cli.*;
 import org.mmadsen.sim.transmissionlab.analysis.TraitFrequencyAnalyzer;
 import org.mmadsen.sim.transmissionlab.analysis.top40DataFileRecorder;
 import org.mmadsen.sim.transmissionlab.analysis.OverallStatisticsRecorder;
 import org.mmadsen.sim.transmissionlab.analysis.AbstractDataCollector;
 import org.mmadsen.sim.transmissionlab.interfaces.IAgentPopulation;
 import org.mmadsen.sim.transmissionlab.interfaces.IDataCollector;
 import org.mmadsen.sim.transmissionlab.interfaces.IPopulationFactory;
 import org.mmadsen.sim.transmissionlab.interfaces.ISharedDataManager;
 import org.mmadsen.sim.transmissionlab.population.SingleTraitPopulationFactory;
 import org.mmadsen.sim.transmissionlab.rules.MoranProcessRandomSamplingTransmission;
 import org.mmadsen.sim.transmissionlab.rules.NonOverlappingRandomSamplingTransmission;
 // debug only
 //import org.mmadsen.sim.transmissionlab.rules.NullRule;
 import org.mmadsen.sim.transmissionlab.rules.RandomAgentInfiniteAllelesMutation;
 import org.mmadsen.sim.transmissionlab.util.PopulationRuleset;
 import org.mmadsen.sim.transmissionlab.util.SharedRepository;
 import org.mmadsen.sim.transmissionlab.util.DataCollectorScheduleType;
 
 import uchicago.src.reflector.ListPropertyDescriptor;
 import uchicago.src.sim.engine.ActionGroup;
 import uchicago.src.sim.engine.Schedule;
 import uchicago.src.sim.engine.ScheduleBase;
 import uchicago.src.sim.engine.SimInit;
 import uchicago.src.sim.engine.SimModelImpl;
 import uchicago.src.sim.util.Random;
 
 public class TransmissionLabModel extends SimModelImpl implements ISharedDataManager {
 
     public static void main(String[] args) {

        System.out.println("(debug) first arg: " + args[0]);

         // parse command line options to see if this is a batch run
         Options cliOptions = new Options();
         cliOptions.addOption("b", false, "enable batch mode");
         CommandLineParser parser = new PosixParser();
         CommandLine cmd = null;
         try {
             cmd = parser.parse( cliOptions, args );
         }
         catch( ParseException ex ) {
             System.out.println("ERROR: Command line exception: " + ex.toString());
             System.exit(1);
         }
 
         SimInit init = new SimInit();
         TransmissionLabModel model = new TransmissionLabModel();
 
         model.isBatchExecution = cmd.hasOption("b");
         System.out.println("INFO: batch mode setting: " + model.isBatchExecution);
 
         init.loadModel(model, null, model.isBatchExecution);
     }
 
     private ActionGroup simulationActionGroups = null;
     private ActionGroup analysisActionGroup = null;
     private ActionGroup finalActionGroup = null;
     private ActionGroup modelActionGroup = null;
     private ActionGroup setupActionGroup = null;
     private List<IDataCollector> dataCollectorList = null;
     private Map<String, IDataCollector> dataCollectorMap = null;
     private Boolean dataCollectorsPresent = false;
     private String dataDumpDirectory = "/tmp";
     private double dataFileSnapshotPercentage = 0.10;
     private Boolean enableFileSnapshot = false;
     private Boolean enableNewTopN = true;
     private Boolean enableOverallStats = true;
     private int ewensThetaMultipler = 2;
     private String initialTraitStructure = null;
     private String populationProcessType = null;
     private int moranProcessNumPairs = 1;
     private Log log = null;
     private int maxVariants = 4000;
     private double mu = 0.01;
     private int numAgents = 500;
     private int numTicks = 500;
     private PopulationRuleset popRuleSet = null;
     private IAgentPopulation population = null;
     private Schedule schedule;
     private SharedRepository sharedDataRepository = null;
     private int topNListSize = 40;
 
     public Boolean getBatchExecution() {
         return isBatchExecution;
     }
 
     public void setBatchExecution(Boolean batchExecution) {
         isBatchExecution = batchExecution;
     }
 
     private Boolean isBatchExecution = false;
 
     @SuppressWarnings("unchecked")
     // vector usage is type-unsafe but follows repast examples
     public TransmissionLabModel() {
         super();
         URL log4jresource = this.getClass().getResource("log4j.properties");
         PropertyConfigurator.configure(log4jresource);
         this.log = LogFactory.getLog(TransmissionLabModel.class);
         this.addDynamicParameters();
     }
 
     @SuppressWarnings("unchecked")
     private void addDynamicParameters() {
         this.log.debug("Adding dynamic parameters");
         Vector<String> popPropertyVec = new Vector<String>();
         popPropertyVec.add("SequentialTrait");
         popPropertyVec.add("GaussianTrait");
         ListPropertyDescriptor pd = new ListPropertyDescriptor("InitialTraitStructure", popPropertyVec);
 
         Vector<String> transmissionRulePropVec = new Vector<String>();
         transmissionRulePropVec.add("WrightFisherProcess");
         transmissionRulePropVec.add("MoranProcess");
         ListPropertyDescriptor pd2 = new ListPropertyDescriptor("PopulationProcessType", transmissionRulePropVec);
         this.descriptors.put("InitialTraitStructure", pd);
         this.descriptors.put("PopulationProcessType", pd2);
     }
 
     private void addDataCollector(IDataCollector collector) {
         this.dataCollectorList.add(collector);
         this.dataCollectorMap.put(collector.getDataCollectorName(), collector);
     }
 
     public void begin() {
         this.buildModel();
 
         // check to see if we've enabled or disabled any data collectors
         // would be nice if we could genericize this by having a standard parameter for each IDataCollector
         // but I haven't solved the problem of initializing the getInitParams() before we've actually run
         // anything in the model yet...
         if (!this.getEnableNewTopN()) {
             this.log.debug("removing TraitFrequencyAnalyzer from active data collectors - not selected");
             IDataCollector d = this.dataCollectorMap.get("TraitFrequencyAnalyzer");
             this.removeDataCollector(d);
         }
 
         if (!this.getEnableFileSnapshot()) {
             this.log.debug("removing top40DataFileRecorder from active data collectors - not selected");
             IDataCollector d = this.dataCollectorMap.get("top40DataFileRecorder");
             this.removeDataCollector(d);
         }
 
         if (!this.getEnableOverallStats()) {
             this.log.debug("removing OverallStatisticsRecorder from active data collectors - not selected");
             IDataCollector d = this.dataCollectorMap.get("OverallStatisticsRecorder");
             this.removeDataCollector(d);
         }
 
         // now that the user has entered/modified parameters in the GUI,
         // let all of the IDataCollectors initialize themselves.  Then we
         // let the IDataCollector tell us how to schedule itself.
         for (IDataCollector collector : this.dataCollectorList) {
             collector.initialize();
             DataCollectorScheduleType schedGroupType = collector.getSchedGroupType();
             if (schedGroupType == DataCollectorScheduleType.EACH_TICK) {
                 this.analysisActionGroup.addAction(collector.getDataCollectorSchedule());
             } else if (schedGroupType == DataCollectorScheduleType.END) {
                 this.finalActionGroup.addAction(collector.getDataCollectorSchedule());
             } else if (schedGroupType == DataCollectorScheduleType.BEGIN) {
                 this.setupActionGroup.addAction(collector.getDataCollectorSchedule());
             } else {
                 this.log.error("ERROR: unknown schedule group type: " + schedGroupType.toString());
             }
 
         }
 
         // debug
         log.debug("dataCollectorMap: " + this.dataCollectorMap.toString());
 
         // now let's setup the rules that'll govern the population transformations at each step
         // we do it here rather than in setup() because we want access to GUI parameter selections
         this.buildPopulationRules();
 
         // now that the data collector list has been filtered by GUI params
         // and built into an ActionGroup, we can set up the schedule
         this.buildSchedule();
     }
 
 
     private void buildModel() {
         IPopulationFactory factory = new SingleTraitPopulationFactory();
         this.population = factory.generatePopulation(this, log);
         this.maxVariants = this.population.getCurrentMaximumVariant();
     }
 
 
     private void buildSchedule() {
         // set up the model actions - this essentially runs the simulation
         this.modelActionGroup.createActionFor(this, "executePopulationRules");
 
         // we're stacking up two action groups, running them in sequence.
         // first the model action group, then any analysis.  so *all* analyzer
         // modules always run *after* any modeling rule modules in any given
         // model step.  Perhaps this isn't flexible enough for all situations,
         // but it's a good place to start.
         this.simulationActionGroups.addAction(this.modelActionGroup);
         // analysis action group is already set up in begin(), which calls this method
         this.simulationActionGroups.addAction(this.analysisActionGroup);
 
         // set up the actual schedule.  It's pretty simple since all the complexity
         // is pushed down to the rules themselves.
         // we do use tick #1 as the "setup" tick - where any BasicActions that should run
         // prior to the simulation really starting go -- not necessary in the current
         // model but technically, we could set up all sorts of stuff there.
         // we then start the real simulation on tick #2, running the combined
         // modelActionGroup and analysisActionGroup actions in sequence at each tick.
         // we then run any "final" actions at the end of the simulation.
         // we then *define* the end of the simulation to be the last configured tick,
         // unless the user hits stop first.  
 
         schedule.scheduleActionAt(1, this.setupActionGroup);
         schedule.scheduleActionBeginning(2, this.simulationActionGroups);
         schedule.scheduleActionAtEnd(this.finalActionGroup);
         schedule.scheduleActionAt(numTicks, this, "stop", ScheduleBase.LAST);
     }
 
     // pretty much, this is all we have to do to run the stack of population rules...
     // and then we create a BasicAction for it in the modelActionGroup which
     // schedules it to run.  that is done in buildSchedule() in the current model class
     @SuppressWarnings({"UnusedDeclaration"})
     public void executePopulationRules() {
         this.log.debug("executing population rules at tick: " + this.getTickCount());
         this.population = (IAgentPopulation) this.popRuleSet.transform(this.population);
     }
 
     @SuppressWarnings({"WeakerAccess"})
     public Boolean getEnableOverallStats() {
         return enableOverallStats;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setEnableOverallStats(Boolean enableOverallStats) {
         this.enableOverallStats = enableOverallStats;
     }
 
     public String getDataDumpDirectory() {
         return this.dataDumpDirectory;
     }
 
     public double getDataFileSnapshotPercentage() {
         return this.dataFileSnapshotPercentage;
     }
 
 
     @SuppressWarnings({"WeakerAccess"})
     public Boolean getEnableFileSnapshot() {
         return enableFileSnapshot;
     }
 
     @SuppressWarnings({"WeakerAccess"})
     public Boolean getEnableNewTopN() {
         return this.enableNewTopN;
     }
 
     public int getEwensThetaMultipler() {
         return this.ewensThetaMultipler;
     }
 
     public String getInitialTraitStructure() {
         return initialTraitStructure;
     }
 
     @SuppressWarnings({"UnnecessaryLocalVariable"})
     public String[] getInitParam() {
         String[] params = {"NumAgents", "Mu", "NumTicks",
                 "EwensThetaMultipler",
                 "PopulationProcessType",
                 "MoranProcessNumPairs",
                 "DataDumpDirectory",
                 "EnableNewTopN", "EnableFileSnapshot", "EnableOverallStats",
                 "TopNListSize", "DataFileSnapshotPercentage", "InitialTraitStructure"};
 
         return params;
     }
 
     public Log getLog() {
         return log;
     }
 
     public int getMaxVariants() {
         return maxVariants;
     }
 
     public double getMu() {
         return mu;
     }
 
 
     public String getName() {
         return "TransmissionLab";
     }
 
 
     public int getNumAgents() {
         return numAgents;
     }
 
 
     public int getNumTicks() {
         return numTicks;
     }
 
 
     public IAgentPopulation getPopulation() {
         return this.population;
     }
 
 
     public Schedule getSchedule() {
         return schedule;
     }
 
 
     public int getTopNListSize() {
         return topNListSize;
     }
 
 
     @SuppressWarnings({"SuspiciousMethodCalls"})
     private void removeDataCollector(IDataCollector collector) {
         this.dataCollectorList.remove(collector);
         this.dataCollectorMap.remove(collector);
     }
 
     public void removeSharedObject(String key) {
         this.sharedDataRepository.removeEntry(key);
     }
 
     public Collection<Object> retrieveAllAsCollection() {
         return this.sharedDataRepository.getAllEntries();
     }
 
     public Object retrieveSharedObject(String key) {
         return this.sharedDataRepository.getEntry(key);
     }
 
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setDataDumpDirectory(String dir) {
         this.dataDumpDirectory = dir;
         this.log.debug("Directory for file output: " + this.dataDumpDirectory);
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setDataFileSnapshotPercentage(double d) {
         this.dataFileSnapshotPercentage = d;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setEnableFileSnapshot(Boolean enableFileSnapshot) {
         this.enableFileSnapshot = enableFileSnapshot;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setEnableNewTopN(Boolean e) {
         this.enableNewTopN = e;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setEwensThetaMultipler(int ewensThetaMultipler) {
         this.ewensThetaMultipler = ewensThetaMultipler;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setInitialTraitStructure(String initialTraitStructure) {
         this.initialTraitStructure = initialTraitStructure;
     }
 
 
     public void setMaxVariants(int newMaxVariant) {
         this.maxVariants = newMaxVariant;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setMu(double mew) {
         mu = mew;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setNumAgents(int n) {
         numAgents = n;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setnumTicks(int timest) {
         numTicks = timest;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setPopulation(IAgentPopulation population) {
         this.population = population;
     }
 
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setTopNListSize(int topNListSize) {
         this.topNListSize = topNListSize;
     }
 
     public void setup() {
         this.log.info("Entering setup() for a new simulation run");
 
         /*
            * CLEANUP SECTION - Ensure that if this is just the reset button in the GUI,
            * we're ready for another run, not leaking memory, and not working with stale
            * data.
            */
         this.schedule = null;
 
         // if this is the first time through (i.e., just started the simulation),
         // we don't have data collectors yet, but if this is called by hitting
         // the rest for a second or later run, we need to clean out the graphs
         // and other objects held by data collectors.
         if (this.dataCollectorsPresent) {
             for (IDataCollector collector : this.dataCollectorList) {
                 collector.completion();
             }
         }
 
         this.dataCollectorList = null;
         this.dataCollectorMap = null;
         this.dataCollectorsPresent = false;
         this.sharedDataRepository = null;
         this.popRuleSet = null;
 
         /*
            * INSTANTIATION AND SETUP SECTION - now that we're clean and safe, construct
            * things that need to be present for the "configuration" portion of the run --
            * i.e., after starting the simulation and displaying the parameter panel, but before
            * hitting "start"
            */
 
         Random.createUniform();
 
        // HACK:  SimModelImpl doesn't retain any info on whether this model was constructed as a
        // batch-mode simulation or not, so if we were started via the main() method in the model
        // class, great.  If we were started via SimInit.main(), we need a way to tell whether
        // we're in batch mode and this is the "cleanest" way that doesn't involve me hacking the
        // repast source.
        if (this.getBatchExecution() == false && this.getController().isBatch()) {
            log.info("Probably started from SimInit.main as batch, will disable GUI elements");
            this.setBatchExecution(true);
        }

         // Upon setup(), we may need to reinitialize dynamic parameters in the combo box
         this.addDynamicParameters();
 
         this.dataCollectorList = new ArrayList<IDataCollector>();
         this.dataCollectorMap = new HashMap<String, IDataCollector>();
         this.sharedDataRepository = new SharedRepository();
         this.schedule = new Schedule();
         this.modelActionGroup = new ActionGroup(ActionGroup.SEQUENTIAL);
         this.analysisActionGroup = new ActionGroup(ActionGroup.SEQUENTIAL);
         this.simulationActionGroups = new ActionGroup(ActionGroup.SEQUENTIAL);
         this.setupActionGroup = new ActionGroup(ActionGroup.SEQUENTIAL);
         // at the final tick, when executing things at the finish, we don't have an inherent notion
         // of "ordering" for stuff that happens after the model run.
         this.finalActionGroup = new ActionGroup(ActionGroup.RANDOM);
 
         // TODO: somehow this needs to be configurable, so we can have looser coupling here.
         IDataCollector f = new TraitFrequencyAnalyzer(this);
         this.addDataCollector(f);
 
         IDataCollector t = new top40DataFileRecorder(this);
         this.addDataCollector(t);
 
         IDataCollector o = new OverallStatisticsRecorder(this);
         this.addDataCollector(o);
 
         for (IDataCollector collector : this.dataCollectorList) {
             collector.build(this);
         }
 
         this.dataCollectorsPresent = true;
 
 
         this.log.debug("completed model setup() successfullly");
     }
 
     private void buildPopulationRules() {
         this.log.debug("Setting up population transformation rules");
         this.popRuleSet = new PopulationRuleset();
 
         // DEBUG: testing only - remove
         //this.popRuleSet.addRule(new NullRule(log, this));
 
         if ( this.getPopulationProcessType() == null ) {
             log.info("No PopulationProcess selection made - defaulting to WrightFisherProcess");
             this.setPopulationProcessType("WrightFisherProcess");
         }
 
         if (this.getPopulationProcessType().equals("WrightFisherProcess")) {
             this.popRuleSet.addRule(new NonOverlappingRandomSamplingTransmission(log, this));
         } else if (this.getPopulationProcessType().equals("MoranProcess")) {
             MoranProcessRandomSamplingTransmission mpRule = new MoranProcessRandomSamplingTransmission(log, this);
             mpRule.setReproductivePairsPerTick(this.getMoranProcessNumPairs());
             this.popRuleSet.addRule(mpRule);
         } else {
             this.log.error("Unknown PopulationProcessType: " + this.getPopulationProcessType());
         }
 
         this.log.debug("created Transmission rule: " + this.getPopulationProcessType());
 
         this.popRuleSet.addRule(new RandomAgentInfiniteAllelesMutation(log, this));
         this.log.debug("created Mutation rule");
     }
 
     public void storeSharedObject(String key, Object value) {
         this.sharedDataRepository.putEntry(key, value);
     }
 
     @SuppressWarnings({"WeakerAccess"})
     public String getPopulationProcessType() {
         return this.populationProcessType;
     }
 
     @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
     public void setPopulationProcessType(String populationProcessType) {
         this.populationProcessType = populationProcessType;
     }
 
     @SuppressWarnings({"WeakerAccess"})
     public int getMoranProcessNumPairs() {
         return this.moranProcessNumPairs;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void setMoranProcessNumPairs(int moranProcessNumPairs) {
         this.moranProcessNumPairs = moranProcessNumPairs;
     }
 
 }
