 package hudson.plugins.pipelinesinktrigger;
 
 import hudson.Extension;
 import hudson.model.BuildableItem;
 import hudson.model.Item;
 import hudson.model.Result;
 import hudson.model.TopLevelItem;
 import hudson.model.AbstractProject;
 import hudson.model.Cause;
 import hudson.model.Hudson;
 import hudson.model.Project;
 import hudson.model.Run;
 import hudson.model.listeners.ItemListener;
 import hudson.triggers.Trigger;
 import hudson.triggers.TriggerDescriptor;
 import hudson.triggers.TimerTrigger;
 import hudson.util.FormValidation;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 
 import org.antlr.runtime.RecognitionException;
 import org.apache.commons.lang3.StringUtils;
 import org.jgrapht.DirectedGraph;
 import org.jgrapht.EdgeFactory;
 import org.jgrapht.alg.CycleDetector;
 import org.jgrapht.graph.DefaultDirectedGraph;
 import org.jgrapht.traverse.DepthFirstIterator;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 
 public class BuildGraphPipelineSinkTrigger extends Trigger<BuildableItem> {
 
     private static final Logger LOGGER = Logger.getLogger(BuildGraphPipelineSinkTrigger.class.getName());
 
     private String rootProjectName;
     private String sinkProjectName;
     private String excludedProjectNames;
     private final boolean ignoreNonSuccessfulUpstreamDependencyBuilds;
 
     @DataBoundConstructor
     public BuildGraphPipelineSinkTrigger(String spec, String rootProjectName, String sinkProjectName, String excludedProjectNames,
             boolean ignoreNonSuccessfulUpstreamDependencyBuilds) throws RecognitionException {
         super(spec);
         this.rootProjectName = rootProjectName;
         this.sinkProjectName = sinkProjectName;
         this.excludedProjectNames = excludedProjectNames;
         this.ignoreNonSuccessfulUpstreamDependencyBuilds = ignoreNonSuccessfulUpstreamDependencyBuilds;
     }
 
     public String getRootProjectName() {
         return rootProjectName;
     }
 
     public String getSinkProjectName() {
         return sinkProjectName;
     }
 
     public String getExcludedProjectNames() {
         return excludedProjectNames;
     }
 
     public boolean isIgnoreNonSuccessfulUpstreamDependencyBuilds() {
         return ignoreNonSuccessfulUpstreamDependencyBuilds;
     }
 
     @Override
     public void run() {
         if (!Hudson.getInstance().isQuietingDown()) {
             LOGGER.log(Level.INFO, String.format("Deciding if a build of '%s' should be triggered...", sinkProjectName));
             try {
                 final TopLevelItem rootProjectItem = Hudson.getInstance().getItem(rootProjectName);
                 if (rootProjectItem == null) {
                     throw new Exception(Messages.BuildGraphPipelineSinkTrigger_RootProjectDoesNotExist(rootProjectName));
                 }
                 final AbstractProject<?,?> rootProject = (AbstractProject<?,?>) rootProjectItem;
                 if (rootProject.isDisabled()) {
                     throw new Exception(Messages.BuildGraphPipelineSinkTrigger_RootProjectDisabled(rootProjectName));
                 }
 
                 final TopLevelItem sinkProjectItem = Hudson.getInstance().getItem(sinkProjectName);
                 if (sinkProjectItem == null) {
                     throw new Exception(Messages.BuildGraphPipelineSinkTrigger_SinkProjectDoesNotExist(sinkProjectName));
                 }
                 final AbstractProject<?,?> sinkProject = (AbstractProject<?,?>) sinkProjectItem;
                 if (sinkProject.isDisabled()) {
                     throw new Exception(Messages.BuildGraphPipelineSinkTrigger_SinkProjectDisabled(sinkProjectName));
                 }
 
                 final Set<String> exclusions = new HashSet<String>();
                 for (String excludedPojectName : StringUtils.split(excludedProjectNames, ',')) {
                     final TopLevelItem excludedProjectItem = Hudson.getInstance().getItem(excludedPojectName.trim());
                     if (excludedProjectItem == null) {
                         throw new Exception(Messages.BuildGraphPipelineSinkTrigger_ExcludedProjectDoesNotExist(excludedPojectName.trim()));
                     }
                     exclusions.add(excludedProjectItem.getName());
                 }
 
                 final DirectedGraph<AbstractProject<?,?>, String> graph = constructDirectedGraph(rootProject, exclusions);
                 //TODO should we check if the graph has cycle(s) - if so then fail...
                 final CycleDetector<AbstractProject<?,?>, String> cycleDetector = new CycleDetector<AbstractProject<?,?>, String>(graph);
                 if (cycleDetector.detectCycles()) {
                     throw new Exception(String.format("A build of '%s' will not be scheduled: build pipeline graph contains cycle(s)", sinkProjectName));
                 }
                 triggerBuildOfSinkIfNecessary(graph, rootProject, sinkProject);
             }
             catch (Exception e) {
                 LOGGER.log(Level.SEVERE, "Encountered an error during trigger execution.", e);
             }
         }
     }
 
     @SuppressWarnings("rawtypes")
     private DirectedGraph<AbstractProject<?,?>, String> constructDirectedGraph(AbstractProject<?,?> root, Set<String> exclusions) {
         final DirectedGraph<AbstractProject<?,?>, String> graph = new DefaultDirectedGraph<AbstractProject<?,?>, String>(
                 new EdgeFactory<AbstractProject<?,?>, String>() {
                     public String createEdge(AbstractProject<?,?> source, AbstractProject<?,?> target) {
                         return String.format("'%s' -----> '%s'", source.getName(), target.getName());
                     }
                 });
 
         final StringBuilder prettyPrinter = new StringBuilder();
         final Stack<AbstractProject<?,?>> stack = new Stack<AbstractProject<?,?>>();
         graph.addVertex(root);
         stack.push(root);
         while (!stack.isEmpty()) {
             final AbstractProject<?,?> p = stack.pop();
             prettyPrinter.append(p.getName());
             prettyPrinter.append(": {");
             int index = 0;
             final List<AbstractProject> children = p.getDownstreamProjects();
             for (AbstractProject<?,?> child : children) {
                 if (!child.isDisabled() && !exclusions.contains(child.getName())) {
                     graph.addVertex(child);
                     graph.addEdge(p, child);
                     stack.push(child);
                     if (index > 0) {
                         prettyPrinter.append(", ");
                     }
                     prettyPrinter.append(child.getName());
					index++;
                 }
             }
             prettyPrinter.append("}");
             prettyPrinter.append(String.format("%n"));
         }
         LOGGER.log(Level.INFO, String.format("The build pipeline graph rooted at '%s':%n%s", root.getName(), prettyPrinter.toString()));
         return graph;
     }
 
     private void triggerBuildOfSinkIfNecessary(DirectedGraph<AbstractProject<?,?>, String> graph, AbstractProject<?,?> root, AbstractProject<?,?> sink) {
         final List<String> listOfNonSuccessfulUpstreamProjectBuilds = new ArrayList<String>();
         long maxCompletionTimestamp = Long.MIN_VALUE;
         final DepthFirstIterator<AbstractProject<?,?>, String> itr = new DepthFirstIterator<AbstractProject<?,?>, String>(graph, root);
         while (itr.hasNext()) {
             final AbstractProject<?,?> project = itr.next();
             if (project.isBuilding() || project.isInQueue()) {
                 LOGGER.log(Level.INFO, Messages.BuildGraphPipelineSinkTrigger_PipelineActive(sinkProjectName));
                 return;
             }
 
             final Run<?,?> lastBuild = project.getLastBuild();
             if (lastBuild != null) {
                 if (lastBuild.getResult().isBetterOrEqualTo(Result.UNSTABLE)) {
                     final long completedAt = lastBuild.getTimeInMillis() + lastBuild.getDuration();
                     maxCompletionTimestamp = Math.max(maxCompletionTimestamp, completedAt);
                 }
                 else {
                     listOfNonSuccessfulUpstreamProjectBuilds.add(project.getName());
                 }
             }
         }
 
         if (!listOfNonSuccessfulUpstreamProjectBuilds.isEmpty()) {
             final StringBuilder sb = new StringBuilder();
             for (int i = 0; i < listOfNonSuccessfulUpstreamProjectBuilds.size(); i++) {
                 sb.append(listOfNonSuccessfulUpstreamProjectBuilds.get(i));
                 if (i < listOfNonSuccessfulUpstreamProjectBuilds.size() - 1) {
                     sb.append(", ");
                 }
             }
             if (!ignoreNonSuccessfulUpstreamDependencyBuilds) {
                 LOGGER.log(Level.INFO, Messages.BuildGraphPipelineSinkTrigger_DetectedNonSuccessfulUpstreamDependencyBuilds(sinkProjectName, sb.toString()));
                 return;
             }
             LOGGER.log(Level.INFO, Messages.BuildGraphPipelineSinkTrigger_IgnoringNonSuccessfulUpstreamDependencyBuilds(sb.toString()));
         }
 
         boolean isStale = false;
         final Run<?,?> lastSuccessfulBuildOfSink = sink.getLastSuccessfulBuild();
         if (lastSuccessfulBuildOfSink != null) {
             final long completedAt = lastSuccessfulBuildOfSink.getTimeInMillis() + lastSuccessfulBuildOfSink.getDuration();
             if (completedAt >= maxCompletionTimestamp) {
                 LOGGER.log(Level.INFO, Messages.BuildGraphPipelineSinkTrigger_NoUpstreamDependencyBuildChanges(sinkProjectName));
                 return;
             }
             isStale = true;
         }
 
         LOGGER.log(Level.INFO, isStale ? Messages.BuildGraphPipelineSinkTrigger_DetectedUpstreamDependencyBuildChanges(sinkProjectName) :
             Messages.BuildGraphPipelineSinkTrigger_BuildSink(sinkProjectName));
         final boolean isBuildScheduled = sink.scheduleBuild(new BuildGraphPipelineSinkTriggerCause());
         LOGGER.log(Level.INFO, isBuildScheduled ? hudson.tasks.Messages.BuildTrigger_Triggering(sinkProjectName) :
             hudson.tasks.Messages.BuildTrigger_InQueue(sinkProjectName));
     }
 
     @Extension
     public static final class BuildGraphPipelineSinkTriggerDescriptor extends TriggerDescriptor {
 
         private final TimerTrigger.DescriptorImpl timerTriggerDescriptorDelegate = new TimerTrigger.DescriptorImpl();
 
         public FormValidation doCheckRootProjectName(@QueryParameter String rootProjectName) throws IOException, ServletException {
             return validateProjectParemeter(rootProjectName);
         }
 
         public FormValidation doCheckSinkProjectName(@QueryParameter String sinkProjectName) throws IOException, ServletException {
             return validateProjectParemeter(sinkProjectName);
         }
 
         public FormValidation doCheckExcludedProjectNames(@QueryParameter String excludedProjectNames) throws IOException, ServletException {
             if (excludedProjectNames.trim().length() > 0) {
                 for (String excludedPojectName : StringUtils.split(excludedProjectNames, ',')) {
                     final FormValidation val = validateProjectParemeter(excludedPojectName.trim());
                     if (FormValidation.Kind.ERROR.equals(val.getKind())) {
                         return val;
                     }
                 }
             }
             return FormValidation.ok();
         }
 
         public FormValidation doCheckSpec(@QueryParameter String spec) throws IOException, ServletException {
             return timerTriggerDescriptorDelegate.doCheckSpec(spec);
         }
 
         private FormValidation validateProjectParemeter(String projectName) throws IOException, ServletException {
             if (projectName.trim().length() == 0) {
                 return FormValidation.error(Messages.BuildGraphPipelineSinkTrigger_NoProjectSpecified());
             }
             final Item item = Hudson.getInstance().getItem(projectName);
             if (item == null) {
                 return FormValidation.error(Messages.BuildGraphPipelineSinkTrigger_NoSuchProject(projectName));
             }
             if (!AbstractProject.class.isAssignableFrom(item.getClass())) {
                 return FormValidation.error(hudson.tasks.Messages.BuildTrigger_NotBuildable(projectName));
             }
             return FormValidation.ok();
         }
 
         @Override
         public String getDisplayName() {
             return Messages.BuildGraphPipelineSinkTrigger_DisplayName();
         }
 
         @Override
         public boolean isApplicable(Item item) {
             return item instanceof TopLevelItem;
         }
 
     }
 
     private static final class BuildGraphPipelineSinkTriggerCause extends Cause {
 
         public BuildGraphPipelineSinkTriggerCause() {
             super();
         }
 
         @Override
         public String getShortDescription() {
             return Messages.BuildGraphPipelineSinkTrigger_CauseShortDescription();
         }
 
     }
 
     /**
      * Called from {@link BuildGraphPipelineSinkTrigger.DefaultItemListener} when a job is renamed.
      *
      * @return {@code true} if this {@link BuildGraphPipelineSinkTrigger} is changed and needs to be saved, otherwise {@code false}.
      */
     public boolean onJobRenamed(String oldName, String newName) {
         final boolean excludedProjectNamesChanged = handleRenameForExcludedProjectNames(oldName, newName);
         final boolean rootProjectNameChanged = handleRenameForRootProjectName(oldName, newName);
         final boolean sinkProjectNameChanged = handleRenameForSinkProjectName(oldName, newName);
         return (excludedProjectNamesChanged || rootProjectNameChanged || sinkProjectNameChanged);
     }
 
     private boolean handleRenameForExcludedProjectNames(String oldName, String newName) {
         boolean changed = false;
         if (StringUtils.stripToEmpty(excludedProjectNames).length() > 0) {
             final StringBuilder sb = new StringBuilder();
             final String[] exclusions = StringUtils.split(excludedProjectNames, ',');
             for (int i = 0; i < exclusions.length; i++) {
                 if (exclusions[i].trim().equals(oldName)) {
                     sb.append(newName);
                     changed = true;
                 }
                 else {
                     sb.append(exclusions[i].trim());
                 }
                 if (i < exclusions.length - 1) {
                     sb.append(',');
                 }
             }
             excludedProjectNames = sb.toString();
         }
         return changed;
     }
 
     private boolean handleRenameForRootProjectName(String oldName, String newName) {
         if (rootProjectName.equals(oldName)) {
             rootProjectName = newName;
             return true;
         }
         return false;
     }
 
     private boolean handleRenameForSinkProjectName(String oldName, String newName) {
         if (sinkProjectName.equals(oldName)) {
             sinkProjectName = newName;
             return true;
         }
         return false;
     }
 
     @Extension
     public static final class DefaultItemListener extends ItemListener {
 
         @Override
         public void onRenamed(Item item, String oldName, String newName) {
             for (Project<?, ?> p : Hudson.getInstance().getProjects()) {
                 final BuildGraphPipelineSinkTrigger trigger = p.getTrigger(BuildGraphPipelineSinkTrigger.class);
                 if (trigger != null) {
                     if (trigger.onJobRenamed(oldName, newName)) {
                         try {
                             p.save();
                         } catch (IOException e) {
                             LOGGER.log(Level.WARNING, "Failed to persist project setting during rename from " + oldName + " to " + newName, e);
                         }
                     }
                 }
             }
         }
     }
 
 }
