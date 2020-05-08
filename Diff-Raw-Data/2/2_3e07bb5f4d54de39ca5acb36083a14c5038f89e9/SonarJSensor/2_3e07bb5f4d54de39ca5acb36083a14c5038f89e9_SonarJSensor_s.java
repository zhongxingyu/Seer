 /*
  * Sonar-SonarJ-Plugin
  * Open source plugin for Sonar
  * Copyright (C) 2009 hello2morrow GmbH
  * mailto: info AT hello2morrow DOT com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.hello2morrow.sonarplugin;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.Sensor;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.batch.maven.DependsUponMavenPlugin;
 import org.sonar.api.batch.maven.MavenPluginHandler;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.profiles.RulesProfile;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.rules.ActiveRule;
 import org.sonar.api.rules.Rule;
 import org.sonar.api.rules.RulePriority;
 import org.sonar.api.rules.RulesManager;
 import org.sonar.api.rules.Violation;
 
 import com.hello2morrow.sonarplugin.xsd.ReportContext;
 import com.hello2morrow.sonarplugin.xsd.XsdArchitectureViolation;
 import com.hello2morrow.sonarplugin.xsd.XsdAttribute;
 import com.hello2morrow.sonarplugin.xsd.XsdAttributeCategory;
 import com.hello2morrow.sonarplugin.xsd.XsdAttributeRoot;
 import com.hello2morrow.sonarplugin.xsd.XsdCycleGroup;
 import com.hello2morrow.sonarplugin.xsd.XsdCycleGroups;
 import com.hello2morrow.sonarplugin.xsd.XsdCyclePath;
 import com.hello2morrow.sonarplugin.xsd.XsdDependencyProblem;
 import com.hello2morrow.sonarplugin.xsd.XsdElementProblem;
 import com.hello2morrow.sonarplugin.xsd.XsdPosition;
 import com.hello2morrow.sonarplugin.xsd.XsdProblemCategory;
 import com.hello2morrow.sonarplugin.xsd.XsdProjects;
 import com.hello2morrow.sonarplugin.xsd.XsdTask;
 import com.hello2morrow.sonarplugin.xsd.XsdTasks;
 import com.hello2morrow.sonarplugin.xsd.XsdTypeRelation;
 import com.hello2morrow.sonarplugin.xsd.XsdViolations;
 import com.hello2morrow.sonarplugin.xsd.XsdWarning;
 import com.hello2morrow.sonarplugin.xsd.XsdWarnings;
 import com.hello2morrow.sonarplugin.xsd.XsdWarningsByAttribute;
 import com.hello2morrow.sonarplugin.xsd.XsdWarningsByAttributeGroup;
 
 public final class SonarJSensor implements Sensor, DependsUponMavenPlugin
 {
     public static final String LICENSE_FILE_NAME = "sonarj.license";
     public static final String DEVELOPER_COST_PER_HOUR = "sonarj.hourly_rate";
     
     private static final Logger LOG = LoggerFactory.getLogger(SonarJSensor.class);
 
     private static final String ACD = "Average component dependency (ACD)";
     private static final String NCCD = "Normalized cumulative component dependency (NCCD)";
     private static final String INTERNAL_PACKAGES = "Number of internal packages";
     private static final String INSTRUCTIONS = "Number of instructions";    
     private static final String UNASSIGNED_TYPES = "Number of unassigned types";
     private static final String VIOLATING_DEPENDENCIES = "Number of violating type dependencies";
     private static final String VIOLATING_TYPES = "Number of violating types";
     private static final String CYCLIC_LAYER_GROUPS = "Number of cyclic layer groups";
     private static final String CYCLIC_LAYERS = "Number of cyclic layers";
     private static final String CYCLIC_SUBSYSTEMS = "Number of cyclic subsystems";
     private static final String CYCLIC_VERTICAL_SLICE_GROUPS = "Number of cyclic vertical slice groups";
     private static final String CYCLIC_VERTICAL_SLICES = "Number of cyclic vertical slices";
     private static final String TYPE_DEPENDENCIES = "Number of type dependencies (all)";
     private static final String JAVA_FILES = "Number of Java source files (non-excluded)";
     private static final String IGNORED_VIOLATIONS = "Number of ignored violations";
     private static final String IGNORED_WARNINGS = "Number of ignored warnings";
     private static final String TASKS = "Number of tasks";
     private static final String THRESHOLD_WARNINGS = "Number of warnings (thresholds)";
     private static final String WORKSPACE_WARNINGS = "Number of warnings (workspace)";
     private static final String EROSION_REFS = "Structural erosion - reference level";
     private static final String EROSION_TYPES = "Structural erosion - type level";
     private static final String INTERNAL_TYPES = "Number of internal types (all)";
     
     private final SonarJPluginHandler pluginHandler;
     private Map<String, Number> projectMetrics;
     private SensorContext sensorContext;
     private RulesManager rulesManager;
     private RulesProfile rulesProfile;
     private double developerCostRate = 70.0;
     private Project project;
     private int cycleGroupId = 0;
    
     protected static ReportContext readSonarjReport(String fileName)
     {
         ReportContext result = null;
         InputStream input = null;
         
         try
         {
             JAXBContext context = JAXBContext.newInstance("com.hello2morrow.sonarplugin.xsd");
             Unmarshaller u = context.createUnmarshaller();
             
             input = new FileInputStream(fileName);
             result = (ReportContext) u.unmarshal(input);
         }
         catch (JAXBException e)
         {
             LOG.error("JAXB Problem in "+fileName, e);
         }
         catch (FileNotFoundException e)
         {
             LOG.error("Cannot open "+fileName, e);
         }
         finally
         {
             if (input != null)
             {
                 try
                 {
                     input.close();
                 }
                 catch (IOException e)
                 {
                     LOG.error("Cannot close "+fileName, e);
                 }
             }
         }
         return result;
     }
     
     public SonarJSensor(Configuration config, RulesManager rulesManager, RulesProfile rulesProfile)
     {
         String licenseFileName = config.getString(LICENSE_FILE_NAME);
         
         developerCostRate = config.getDouble(DEVELOPER_COST_PER_HOUR, 70.0);
         pluginHandler = new SonarJPluginHandler(licenseFileName);
         this.rulesManager = rulesManager;
         this.rulesProfile = rulesProfile;
         if (rulesManager == null)
         {
             LOG.warn("No RulesManager provided to sensor");
         }
         if (rulesProfile == null)
         {
             LOG.warn("No RulesProfile given to sensor");
         }
     }
     
     public MavenPluginHandler getMavenPluginHandler(Project proj)
     {
         return pluginHandler;
     }
 
     public boolean shouldExecuteOnProject(Project project)
     {
         return true; 
     }
 
     private Map<String, Number> readAttributes(XsdAttributeRoot root)
     {
         Map<String, Number> result = new HashMap<String, Number>();
 
         for (XsdAttributeCategory cat : root.getAttributeCategory())
         {
             for (XsdAttribute attr : cat.getAttribute())
             {
                 String attrName = attr.getName();
                 String value = attr.getValue();
                 
                 try
                 {
 	                if (value.indexOf('.') >= 0)
 	                {
 	                    result.put(attrName, Double.valueOf(value));
 	                }
 	                else if (value.indexOf(':') == -1)
 	                {
 	                    result.put(attrName, Integer.valueOf(value));
 	                }
                 }
                 catch (NumberFormatException e)
                 {
                 	// Ignore this value
                 }
             }
         }
         return result;
     }
             
     private double getProjectMetric(String key)
     {
         Number num = projectMetrics.get(key);
         
         if (num == null)
         {
             LOG.error("Cannot find metric <"+key+"> in generated report");
             return 0.0;
         }
         return num.doubleValue();
     }
     
     private Measure saveMeasure(String key, Metric metric, int precision)
     {
         double value = getProjectMetric(key);
 	
         return saveMeasure(metric, value, precision);
     }
         
     private Measure saveMeasure(Metric metric, double value, int precision)
     {
     	Measure m = new Measure(metric, value, precision);
     	
     	sensorContext.saveMeasure(m);  
     	return m;
     }
     
     private String getAttribute(List<XsdAttribute> map, String name)
     {
     	String value = null;
     	
     	for (XsdAttribute attr : map)
     	{
     		if (attr.getName().equals(name))
     		{
     			value = attr.getValue();
     			break;
     		}
     	}
     	return value;
     }
     
     @SuppressWarnings("unchecked")
     private void analyseCycleGroups(ReportContext report, Number internalPackages, String projectName)
     {
         XsdCycleGroups cycleGroups = report.getCycleGroups();
         double cyclicity = 0;
         double biggestCycleGroupSize = 0;
         double cyclicPackages = 0;
         
         for (XsdCycleGroup group : cycleGroups.getCycleGroup())
         {
             if (group.getNamedElementGroup().equals("Package") && group.getParent().equals(projectName))
             {
                 int groupSize = group.getCyclePath().size();
                 
                 cycleGroupId++;
                 cyclicPackages += groupSize;
                 cyclicity += groupSize*groupSize;
                 if (groupSize > biggestCycleGroupSize)
                 {
                     biggestCycleGroupSize = groupSize;
                 }
                 for (XsdCyclePath cycleMember : group.getCyclePath())
                 {
                     String packageName = cycleMember.getParent();
                     Resource thePackage = sensorContext.getResource(packageName);
                     
                     if (thePackage != null)
                     {
                         sensorContext.saveMeasure(thePackage, SonarJMetrics.CYCLE_GROUP_SIZE, Double.valueOf(groupSize));
                         sensorContext.saveMeasure(thePackage, SonarJMetrics.CYCLE_GROUP_ID, Double.valueOf(cycleGroupId));
                     }
                     else
                     {
                         LOG.error("Cannot find package resource "+packageName);
                     }
                 }
             }
         }
         saveMeasure(SonarJMetrics.BIGGEST_CYCLE_GROUP, biggestCycleGroupSize, 0);
         saveMeasure(SonarJMetrics.CYCLICITY, cyclicity, 0);
         saveMeasure(SonarJMetrics.CYCLIC_PACKAGES, cyclicPackages, 0);
         
         double relativeCyclicity = 100.0 * Math.sqrt(cyclicity) / internalPackages.doubleValue(); 
         
         saveMeasure(SonarJMetrics.RELATIVE_CYCLICITY, relativeCyclicity, 1);
         saveMeasure(SonarJMetrics.INTERNAL_PACKAGES, internalPackages.doubleValue(), 0);        
     }
     
     @SuppressWarnings("unchecked")
     private void saveViolation(Rule rule, ActiveRule activeRule, RulePriority priority, String fqName, int line, String msg)
     {
         Resource javaFile = sensorContext.getResource(fqName);
         
         if (javaFile == null)
         {
             LOG.error("Cannot obtain resource "+fqName);
         }
         else
         {
             Violation v = new Violation(rule, javaFile);
                 
             v.setMessage(msg);
             v.setLineId(line);
             v.setPriority(priority);
             sensorContext.saveViolation(v);
         }
     }
     
     private int handleArchitectureViolations(XsdViolations violations, String projectName)
     {
         Rule rule = rulesManager.getPluginRule(SonarJPluginBase.PLUGIN_KEY, SonarJPluginBase.ARCH_RULE_KEY);
         ActiveRule activeRule = rulesProfile.getActiveRule(SonarJPluginBase.PLUGIN_KEY, SonarJPluginBase.ARCH_RULE_KEY);
         int count = 0;
         
         for (XsdArchitectureViolation violation : violations.getArchitectureViolations())
         {
             String prjName = getAttribute(violation.getArchitectureViolation().getAttribute(), "From scope");
             
             if (prjName.equals(projectName))
             {
                 String toName = getAttribute(violation.getArchitectureViolation().getAttribute(), "To");
                 String toElemType = getAttribute(violation.getArchitectureViolation().getAttribute(), "To element type").toLowerCase();
                 String target = toElemType+' '+toName;
                 
                 for (XsdTypeRelation rel : violation.getTypeRelation())
                 {
                     String toType = getAttribute(rel.getAttribute(), "To");
                     String msg = "Type "+toType+" from "+target+" must not be used from here";
                     
                     for (XsdPosition pos : rel.getPosition())
                     {
                         if (rule != null && activeRule != null)
                         {
                             String relFileName = pos.getFile();
                             String fqName = relFileName.substring(0, relFileName.length()-5).replace('/', '.');
                             saveViolation(rule, activeRule, activeRule.getPriority(), fqName, Integer.valueOf(pos.getLine()), msg);
                         }
                         count++;
                     }
                 }
             }
         }
         if (rule == null)
         {
             LOG.error("SonarJ architecture rule not found");
         }
         else if (activeRule == null)
         {
             LOG.warn("SonarJ architecture rule deactivated");
         }  
         return count;
     }
 
     private void handleThresholdViolations(XsdWarnings warnings, String projectName)
     {
         Rule rule = rulesManager.getPluginRule(SonarJPluginBase.PLUGIN_KEY, SonarJPluginBase.THRESHOLD_RULE_KEY);
         ActiveRule activeRule = rulesProfile.getActiveRule(SonarJPluginBase.PLUGIN_KEY, SonarJPluginBase.THRESHOLD_RULE_KEY);
 
         if (rule != null && activeRule != null)
         {
             for (XsdWarningsByAttributeGroup warningGroup : warnings.getWarningsByAttributeGroup())
             {
                 if (warningGroup.getAttributeGroup().equals("Threshold"))
                 {
                     for (XsdWarningsByAttribute warningByAttribute : warningGroup.getWarningsByAttribute())
                     {
                         String attrName = warningByAttribute.getAttributeName();
                         
                         for (XsdWarning warning : warningByAttribute.getWarning())
                         {
                             String msg = attrName + "="+getAttribute(warning.getAttribute(), "Attribute value");
                             String prj = getAttribute(warning.getAttribute(), "Project");
                             
                             if (prj.equals(projectName))
                             {
                                 for (XsdPosition pos : warning.getPosition())
                                 {
                                     String relFileName = pos.getFile();
                                     String fqName = relFileName.substring(0, relFileName.length()-5).replace('/', '.');
                                     
                                     saveViolation(rule, activeRule, activeRule.getPriority(), fqName, Integer.valueOf(pos.getLine()), msg);
                                 }
                             }
                         }
                     }
                 }
             }
         }
         else if (rule == null)
         {
             LOG.error("SonarJ threshold rule not found");
         }
         else if (activeRule == null)
         {
             LOG.info("SonarJ threshold rule deactivated");
         }  
     }
     
     private String handleDescription(String descr)
     {
         if (descr.startsWith("Fix warning"))
         {
             // TODO: handle ascending metrics correctly (99% are descending)
             return "Reduce"+descr.substring(descr.indexOf(':')+1).toLowerCase();
         }
         if (descr.startsWith("Cut type"))
         {
             String toType = descr.substring(descr.indexOf("to "));
             
             return "Cut dependency "+toType;
         }
         if (descr.startsWith("Move type"))
         {
             String toPackage = descr.substring(descr.indexOf("to package"));
             
             return "Move "+toPackage;
         }
         if (descr.startsWith("Delete type"))
         {
             String toDelete = descr.substring(descr.indexOf("delete ")+7);
             return "Delete "+toDelete;
         }
         LOG.warn("Unprocessed description: "+descr);
         return descr;
     }
     
     private int handleTasks(XsdTasks tasks, String projectName)
     {
         Map<String, RulePriority> priorityMap = new HashMap<String, RulePriority>();
         
         Rule rule = rulesManager.getPluginRule(SonarJPluginBase.PLUGIN_KEY, SonarJPluginBase.TASK_RULE_KEY);
         int count = 0;
         
         if (rule == null)
         {
             LOG.error("SonarJ task rule not found");
             return 0;
         }
 
         ActiveRule activeRule = rulesProfile.getActiveRule(SonarJPluginBase.PLUGIN_KEY, SonarJPluginBase.TASK_RULE_KEY);
         
         if (activeRule == null)
         {
             LOG.info("SonarJ task rule not activated");
         }
 
         priorityMap.put("Low", RulePriority.INFO);
         priorityMap.put("Medium", RulePriority.MINOR);
         priorityMap.put("High" , RulePriority.MAJOR);
         
         for (XsdTask task : tasks.getTask())
         {
             String prjName = getAttribute(task.getAttribute(), "Project");
 
             if (prjName.equals(projectName))
             {
                 String priority = getAttribute(task.getAttribute(), "Priority");            
                 String description = getAttribute(task.getAttribute(), "Description");
                 String assignedTo = getAttribute(task.getAttribute(), "Assigned to");
     
                 
                 description = handleDescription(description); // This should not be needed, but the current description suck
                 if (assignedTo != null)
                 {
                     assignedTo = '['+StringUtils.trim(assignedTo)+']';
                     if (assignedTo.length() > 2)
                     {
                         description += ' '+assignedTo;
                     }
                 }
                 for (XsdPosition pos : task.getPosition())
                 {
                     String relFileName = pos.getFile();
                     String fqName = relFileName.substring(0, relFileName.length()-5).replace('/', '.');
                     int line = Integer.valueOf(pos.getLine());
                     
                     if (activeRule != null)
                     {
                         saveViolation(rule, activeRule, priorityMap.get(priority), fqName, line, description);
                     }
                     count++;
                 }
             }
         }
         return count;
     }
     
     private void addArchitectureMeasures(ReportContext report, String projectName)
     {
     	int cyclicArtifacts = 0;
     	
     	for (String key : new String[] { CYCLIC_LAYERS, CYCLIC_LAYER_GROUPS, CYCLIC_VERTICAL_SLICES, CYCLIC_VERTICAL_SLICE_GROUPS, CYCLIC_SUBSYSTEMS })
     	{
     		cyclicArtifacts += projectMetrics.get(key).intValue();
     	}
         double types = saveMeasure(INTERNAL_TYPES, SonarJMetrics.INTERNAL_TYPES, 0).getValue();
     	saveMeasure(SonarJMetrics.CYCLIC_ARTIFACTS, cyclicArtifacts, 0);
     	Measure unassignedTypes = saveMeasure(UNASSIGNED_TYPES, SonarJMetrics.UNASSIGNED_TYPES, 0);
     	Measure violatingTypes = saveMeasure(VIOLATING_TYPES, SonarJMetrics.VIOLATING_TYPES, 0);
     	saveMeasure(VIOLATING_DEPENDENCIES, SonarJMetrics.VIOLATING_DEPENDENCIES, 0);
     	saveMeasure(TASKS, SonarJMetrics.TASKS, 0);
     	saveMeasure(THRESHOLD_WARNINGS, SonarJMetrics.THRESHOLD_WARNINGS, 0);
     	saveMeasure(WORKSPACE_WARNINGS, SonarJMetrics.WORKSPACE_WARNINGS, 0);
     	saveMeasure(IGNORED_VIOLATIONS, SonarJMetrics.IGNORED_VIOLATONS, 0);
     	saveMeasure(IGNORED_WARNINGS, SonarJMetrics.IGNORED_WARNINGS, 0);
     	
     	assert types >= 1.0 : "Project must not be empty !";
 
     	saveMeasure(SonarJMetrics.VIOLATING_TYPES_PERCENT, 100.0 * violatingTypes.getValue()/types, 1);
     	saveMeasure(SonarJMetrics.UNASSIGNED_TYPES_PERCENT, 100.0 * unassignedTypes.getValue()/types, 1);
 
     	int consistencyWarnings = 0;
     	
     	for (XsdProblemCategory problem : report.getConsistencyProblems().getCategories())
     	{
     	    for (XsdElementProblem p : problem.getElementProblems())
     	    {
     	        if (p.getScope().equals(projectName))
     	        {
     	            consistencyWarnings++;
     	        }
     	    }
     	    for (XsdDependencyProblem p : problem.getDependencyProblems())
     	    {
     	        if (p.getFromScope().equals(projectName))
     	        {
     	            consistencyWarnings++;
     	        }
     	    }
     	}
     	
     	saveMeasure(SonarJMetrics.CONSISTENCY_WARNINGS, consistencyWarnings, 0);
     	
     	XsdViolations violations = report.getViolations();
     	
     	double violating_refs = 0;
     	double task_refs = 0;
     	
        	if (rulesManager != null && rulesProfile != null)
     	{    
     	    violating_refs = handleArchitectureViolations(violations, projectName);
     	    handleThresholdViolations(report.getWarnings(), projectName);
     	    task_refs = handleTasks(report.getTasks(), projectName);
     	}        
         saveMeasure(SonarJMetrics.ARCHITECTURE_VIOLATIONS, violating_refs, 0);
         saveMeasure(SonarJMetrics.TASK_REFS, task_refs, 0);
     }
     
     private void analyse(XsdAttributeRoot xsdProject, ReportContext report)
     {
         String projectName = xsdProject.getName();
         projectMetrics = readAttributes(xsdProject);
         
         Number internalPackages = projectMetrics.get(INTERNAL_PACKAGES);
         double acd = projectMetrics.get(ACD).doubleValue();
         double nccd = projectMetrics.get(NCCD).doubleValue();
 
         saveMeasure(SonarJMetrics.ACD, acd, 1);
         saveMeasure(SonarJMetrics.NCCD, nccd, 1);
         saveMeasure(INSTRUCTIONS, SonarJMetrics.INSTRUCTIONS, 0);
         saveMeasure(JAVA_FILES, SonarJMetrics.JAVA_FILES, 0);
         saveMeasure(TYPE_DEPENDENCIES, SonarJMetrics.TYPE_DEPENDENCIES, 0);
         double refs = saveMeasure(EROSION_REFS, SonarJMetrics.EROSION_REFS, 0).getValue();
         double deps = saveMeasure(EROSION_TYPES, SonarJMetrics.EROSION_TYPES, 0).getValue();
         double effortInHours = deps + refs/10;
         double effortInDays = effortInHours/8.0;
         double cost = effortInHours * developerCostRate;
         saveMeasure(SonarJMetrics.EROSION_DAYS, effortInDays, 1);
         saveMeasure(SonarJMetrics.EROSION_COST, cost, 0);
         analyseCycleGroups(report, internalPackages, projectName);        
        if (pluginHandler.isUsingArchitectureDescription())
         {
             addArchitectureMeasures(report, projectName);
         }
         AlertDecorator.setAlertLevels(new SensorProjectContext(sensorContext));
     }
     
     protected void analyse(SensorContext sensorContext, ReportContext report)
     {
     	this.sensorContext = sensorContext;
         Map<String,Number> generalMetrics = readAttributes(report.getAttributes());
         
         Number internalPackages = generalMetrics.get(INTERNAL_PACKAGES);
         
         if (internalPackages.intValue() == 0)
         {
         	LOG.warn("No classes found in project "+project.getName());
         	return;
         }
         
         XsdProjects projects = report.getProjects();  
         List<XsdAttributeRoot> projectList = projects.getProject();
         
         if (projectList.size() > 1)
         {
             LOG.error("The SonarJ plugin currently cannot analyze the architecture scope of SonarJ projects witj multiple sub-projects. This will be implemented after Sonar 1.12 is available.");
         }   
         else
         {
             analyse(projectList.get(0), report);
         }
     }
     
     public void analyse(Project project, SensorContext sensorContext)
     {
         ReportContext report = readSonarjReport(pluginHandler.getReportFileName());
         
         if (report != null)
         {
             this.project = project;
         	analyse(sensorContext, report);        	
         }
     }
 }
