 /*
  * Copyright luntsys (c) 2001-2004,
  * Date: 2004-4-27
  * Time: 6:24:27
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: 1.
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 package com.luntsys.luntbuild.db;
 
 import com.luntsys.luntbuild.BuildGenerator;
 import com.luntsys.luntbuild.facades.BuildParams;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.facades.lb12.ScheduleFacade;
 import com.luntsys.luntbuild.builders.Builder;
 import com.luntsys.luntbuild.dependency.DependentNode;
 import com.luntsys.luntbuild.security.SecurityHelper;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.utility.OgnlHelper;
 import com.luntsys.luntbuild.utility.Revisions;
 import com.luntsys.luntbuild.utility.ValidationException;
 import com.luntsys.luntbuild.web.ProjectPage;
 import ognl.Ognl;
 import ognl.OgnlException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.quartz.CronTrigger;
 import org.quartz.SimpleTrigger;
 import org.quartz.Trigger;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.*;
 
 /**
  * Schedule utilize a quartz(http://www.opensymphony.com/quartz/) trigger to
  * implement its functionality for schedules. By utilizing this trigger, it can be
  * able to achieve periodical schedules or cron-like schedules. This is a hibernate
  * mapping class
  *
  * @author robin shine
  */
 public class Schedule implements DependentNode {
     private static Log logger = LogFactory.getLog(Schedule.class);
 
     private static Map currentThreads = Collections.synchronizedMap(new HashMap());
 
     private long id;
 
     private String name;
 
     private String description;
 
     private String nextVersion;
 
     private List associatedBuilderNames;
 
     private List associatedPostbuilderNames;
 
     /**
      * Whether or not this schedule is marked as selected.
      */
     private transient boolean selected;
 
     /**
      * Trigger of this schedule. Null value indicates current schedule can only be
      * triggered manually
      */
     private Trigger trigger;
 
     private String buildNecessaryCondition = "vcsModified or dependencyNewer";
 
     /**
      * Project this schedule belongs to
      */
     private Project project;
 
     private int buildType = Constants.BUILD_TYPE_CLEAN;
     private int labelStrategy = Constants.LABEL_IF_SUCCESS;
     private int notifyStrategy = Constants.NOTIFY_WHEN_STATUS_CHANGED;
     private int postbuildStrategy = Constants.POSTBUILD_NONE;
 
     private int triggerDependencyStrategy = Constants.TRIGGER_SCHEDULES_THIS_DEPENDS_ON;
 
     private int buildCleanupStrategy = Constants.BUILD_CLEANUP_NONE;
     private String buildCleanupStrategyData;
 
     /**
      * Status of this schedule
      */
     private int status = Constants.SCHEDULE_STATUS_CREATED;
 
     /**
      * Date when this schedule get current status
      */
     private Date statusDate = new Date();
 
     private Set builds;
 
     /**
      * Schedule ids this schedule depends on
      */
     private List dependentScheduleIds;
 
     private String workingPath;
 
     /**
      * Used for migration purpose
      */
     private List dependentSchedules;
 
     /**
      * Create schedule
      */
     public Schedule() {}
 
     /** Create schedule
      * @param schedule used for creation
      */
     public Schedule(Schedule schedule) {
         setName(getName());
         setProject(schedule.getProject());
         setDescription(schedule.getDescription());
         setNextVersion(schedule.getNextVersion());
         if (schedule.trigger != null)
             setTrigger((Trigger)schedule.getTrigger().clone());
         setBuildNecessaryCondition(schedule.getBuildNecessaryCondition());
         if (schedule.getAssociatedBuilderNames() != null)
             setAssociatedBuilderNames((ArrayList)((ArrayList)schedule.getAssociatedBuilderNames()).clone());
         if (schedule.getAssociatedPostbuilderNames() != null)
         setAssociatedPostbuilderNames((ArrayList)((ArrayList)schedule.getAssociatedPostbuilderNames()).clone());
         setBuildType(schedule.getBuildType());
         setLabelStrategy(schedule.getLabelStrategy());
         setNotifyStrategy(schedule.getNotifyStrategy());
         setPostbuildStrategy(schedule.getPostbuildStrategy());
         setTriggerDependencyStrategy(schedule.getTriggerDependencyStrategy());
         setBuildCleanupStrategy(schedule.getBuildCleanupStrategy());
         setBuildCleanupStrategyData(schedule.getBuildCleanupStrategyData());
         if (schedule.getDependentScheduleIds() != null)
             setDependentScheduleIds((ArrayList)((ArrayList)schedule.getDependentScheduleIds()).clone());
         if (schedule.getDependentSchedules() != null)
             setDependentSchedules((ArrayList)((ArrayList)schedule.getDependentSchedules()).clone());
         setStatus(schedule.getStatus());
         if (schedule.getStatusDate() != null)
             setStatusDate((Date)schedule.getStatusDate().clone());
         setWorkingPath(schedule.getWorkingPath());
     }
 
     /**
      * set a unique identify for this schedule, called automatically by hibernate
      *
      * @param id
      */
     public void setId(long id) {
         this.id = id;
     }
 
     /**
      * Get identifer of this schedule
      * @return id
      */
     public long getId() {
         return id;
     }
 
     /**
      * set a name for this schedule
      *
      * @param name
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Get name of this schedule
      * @return name of this schedule
      */
     public String getName() {
         return name;
     }
 
     /**
      * Set description of this schedule
      * @param description
      */
     public void setDescription(String description) {
         this.description = description;
     }
 
     /**
      * Get description of this schedule
      * @return description of this schedule
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * set trigger of this schedule
      *
      * @param trigger
      * @see org.quartz.Trigger
      */
     public void setTrigger(Trigger trigger) {
         this.trigger = trigger;
     }
 
     /**
      * Get trigger of this schedule
      * @return trigger of this schedule
      */
     public Trigger getTrigger() {
         return trigger;
     }
 
     /**
      * Get project of this schedule
      * @return project of this schedule
      */
     public Project getProject() {
         return project;
     }
 
     /**
      * Get project of this schedule
      * @param name name
      * @return project of this schedule
      */
     public Project getProject(String name) {
         List projects = Luntbuild.getDao().loadProjects();
         for (Iterator iter = projects.iterator(); iter.hasNext();) {
             Project p = (Project) iter.next();
             if (p.getName().equals(name)) return p;
         }
         return null;
     }
 
     /**
      * Set project of this schedule
      * @param project
      */
     public void setProject(Project project) {
         this.project = project;
     }
 
     /**
      * Get build type of this schedule.
      * @return one value of {@link com.luntsys.luntbuild.facades.Constants#BUILD_TYPE_CLEAN},
      * and {@link com.luntsys.luntbuild.facades.Constants#BUILD_TYPE_INCREMENT}
      */
     public int getBuildType() {
         return buildType;
     }
 
     /**
      * Set build type of this schedule
      * @param buildType refer to return value of {@link #getBuildType()}
      */
     public void setBuildType(int buildType) {
         this.buildType = buildType;
     }
 
     public boolean equals(Object obj) {
         if (obj != null && obj instanceof Schedule) {
             if (getId() == ((Schedule) obj).getId())
                 return true;
         }
         return false;
     }
 
     public int hashCode() {
         return (int) getId();
     }
 
     /**
      * Validates this schedule at build time, this is different from the validate() method
      * in the way that it enforces a project build time validation
      */
     public void validateAtBuildTime() {
         getProject().validateAtBuildTime();
         validate();
     }
 
     /**
      * Validates properties of this schedule
      *
      * @throws ValidationException
      */
     public void validate() {
         try {
             Luntbuild.validatePathElement(getName());
         } catch (ValidationException e) {
             throw new ValidationException("Invalid name: " + e.getMessage());
         }
         setName(getName().trim());
 
         if (getWorkingPath() != null) {
             setWorkingPath(getWorkingPath().trim());
         }
 
         validateBuildVersion(getNextVersion());
         setNextVersion(getNextVersion().trim());
 
         if (getTrigger() != null) {
             try {
                 Trigger cloneTrigger = (Trigger) getTrigger().clone();
                 if (cloneTrigger instanceof SimpleTrigger) {
                     SimpleTrigger simpleTrigger = (SimpleTrigger) cloneTrigger;
                     simpleTrigger.setStartTime(new Date(System.currentTimeMillis() + simpleTrigger.getRepeatInterval()));
                 } else
                     cloneTrigger.setStartTime(new Date(System.currentTimeMillis()));
                 Luntbuild.getSchedService().validateTrigger(cloneTrigger);
             } catch (Exception e) {
                 if (getTrigger() instanceof SimpleTrigger)
                     throw new ValidationException("Invalid repeat interval: " + e.getMessage());
                 else
                     throw new ValidationException("Invalid cron expression: " + e.getMessage());
             }
         }
         validateBuildNecessaryCondition(getBuildNecessaryCondition());
         setBuildNecessaryCondition(getBuildNecessaryCondition().trim());
         validateBuildType(getBuildType());
         validateLabelStrategy(getLabelStrategy());
         validateNotifyStrategy(getNotifyStrategy());
         validatePostbuildStrategy(getPostbuildStrategy());
         validateBuildCleanupStrategy(getBuildCleanupStrategy(), getBuildCleanupStrategyData());
 
         if (getAssociatedBuilderNames().size() == 0)
             throw new ValidationException("No builders defined for schedule: " + getProject().getName() + "/" + getName());
         if (getAssociatedPostbuilderNames().size() == 0 && getPostbuildStrategy() != Constants.POSTBUILD_NONE)
             throw new ValidationException("No post-builders defined for schedule: " + getProject().getName() + "/" + getName());
     }
 
     private static void validateBuildCleanupStrategy(int buildCleanupStrategy, String buildCleanupStrategyData) {
         if (buildCleanupStrategy != Constants.BUILD_CLEANUP_NONE &&
                 buildCleanupStrategy != Constants.BUILD_KEEP_BY_COUNT &&
                 buildCleanupStrategy != Constants.BUILD_KEEP_BY_DAYS)
             throw new ValidationException("Invalid build cleanup strategy!");
         if (buildCleanupStrategy != com.luntsys.luntbuild.facades.Constants.BUILD_CLEANUP_NONE) {
             try {
                 int intValue = new Integer(buildCleanupStrategyData).intValue();
                 if (intValue <= 0)
                     throw new ValidationException("Invalid data setting for build cleanup strategy!");
             } catch (NumberFormatException e) {
                 throw new ValidationException("Invalid data setting for build cleanup strategy!");
             }
         }
     }
 
     /**
      * Get label strategy of this schedule
      * @return one value of {@link com.luntsys.luntbuild.facades.Constants#LABEL_ALWAYS},
      * {@link com.luntsys.luntbuild.facades.Constants#LABEL_NONE}, and
      * {@link com.luntsys.luntbuild.facades.Constants#LABEL_IF_SUCCESS}
      */
     public int getLabelStrategy() {
         return labelStrategy;
     }
 
     /**
      * Set label strategy of this schedule
      * @param labelStrategy refer to return value of {@link #getLabelStrategy()}
      */
     public void setLabelStrategy(int labelStrategy) {
         this.labelStrategy = labelStrategy;
     }
 
     /**
      * Get notify strategy of this schedule
      * @return one value of {@link com.luntsys.luntbuild.facades.Constants#NOTIFY_ALWAYS},
      * {@link com.luntsys.luntbuild.facades.Constants#NOTIFY_NONE},
      * {@link com.luntsys.luntbuild.facades.Constants#NOTIFY_IF_FAILED},
      * {@link com.luntsys.luntbuild.facades.Constants#NOTIFY_IF_SUCCESS}, and
      * {@link com.luntsys.luntbuild.facades.Constants#NOTIFY_WHEN_STATUS_CHANGED}
      */
     public int getNotifyStrategy() {
         return notifyStrategy;
     }
 
     /**
      * Set notify strategy of this schedule
      * @param notifyStrategy refer to return value of {@link #getNotifyStrategy()}
      */
     public void setNotifyStrategy(int notifyStrategy) {
         this.notifyStrategy = notifyStrategy;
     }
 
     /**
      * Get post-build strategy of this schedule
      * @return one of the value of {@link com.luntsys.luntbuild.facades.Constants#POSTBUILD_ALWAYS},
      * {@link com.luntsys.luntbuild.facades.Constants#POSTBUILD_NONE},
      * {@link com.luntsys.luntbuild.facades.Constants#POSTBUILD_IF_FAILED},
      * {@link com.luntsys.luntbuild.facades.Constants#POSTBUILD_IF_SUCCESS}
      */
     public int getPostbuildStrategy() {
         return postbuildStrategy;
     }
 
     /**
      * Set post-build strategy of this schedule
      * @param postbuildStrategy refer to return value of {@link #getPostbuildStrategy()}
      */
     public void setPostbuildStrategy(int postbuildStrategy) {
         this.postbuildStrategy = postbuildStrategy;
     }
 
     /**
      * Get the work directory of the current schedule 'workdir'/'project'/'schedule'
      *
      * @return work directory of the current schedule 'workdir'/'project'/'schedule'
      */
     public String getWorkDirRaw() {
         String workingDir;
         String globalWorkingDir =
             (String) Luntbuild.getProperties().get(com.luntsys.luntbuild.facades.Constants.WORKING_DIR);
         if (Luntbuild.isEmpty(globalWorkingDir))
             globalWorkingDir = new File(Luntbuild.installDir + File.separator + "work").getAbsolutePath();
         if (Luntbuild.isEmpty(getWorkingPath()))
             workingDir = globalWorkingDir + File.separator + getProject().getName();
         else {
             if (new File(getWorkingPath()).isAbsolute())
                 workingDir = getWorkingPath();
             else
                 workingDir = globalWorkingDir + File.separator + getWorkingPath();
         }
         try {
             workingDir = new File(workingDir).getCanonicalPath();
             return workingDir;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Get the work directory of the current schedule 'workdir'/'project'/'schedule'
      * <strong>
      * Note this method should be used in Ognl expressions only because of beckslash handling!
      * </strong>
      *
      * @return work directory of the current schedule 'workdir'/'project'/'schedule'
      */
     public String getWorkingDir() {
         String workingDir;
         String globalWorkingDir =
             (String) Luntbuild.getProperties().get(com.luntsys.luntbuild.facades.Constants.WORKING_DIR);
         if (Luntbuild.isEmpty(globalWorkingDir))
             globalWorkingDir = new File(Luntbuild.installDir + File.separator + "work").getAbsolutePath();
         if (Luntbuild.isEmpty(getWorkingPath()))
             workingDir = globalWorkingDir + File.separator + getProject().getName();
         else {
             if (new File(getWorkingPath()).isAbsolute())
                 workingDir = getWorkingPath();
             else
                 workingDir = globalWorkingDir + File.separator + getWorkingPath();
         }
         try {
             workingDir = new File(workingDir).getCanonicalPath();
             return workingDir.replaceAll("\\\\", "\\\\\\\\"); // in order to keep back slash for ognl expression evaluation
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Resolves the absolute path for specified file path, if this file path already denotes a
      * absolute file path, it will just return this path. Otherwise it will prefix the file path with
      * this schedule's working directory and return that.
      *
      * @param filePath
      * @return absolute path
      */
     public String resolveAbsolutePath(String filePath) {
         if (Luntbuild.isEmpty(filePath))
             return getWorkDirRaw();
         File file = new File(filePath);
         if (file.isAbsolute())
             return filePath;
         else
             return getWorkDirRaw() + File.separator + filePath;
     }
 
     public com.luntsys.luntbuild.facades.lb12.ScheduleFacade getFacade() {
         com.luntsys.luntbuild.facades.lb12.ScheduleFacade facade = new ScheduleFacade();
         facade.setId(getId());
         facade.setName(getName());
         facade.setDescription(getDescription());
         facade.setNextVersion(getNextVersion());
 
         if (getTrigger() == null)
             facade.setTriggerType(Constants.TRIGGER_TYPE_MANUAL);
         else if (getTrigger().getClass() == SimpleTrigger.class) {
             facade.setTriggerType(Constants.TRIGGER_TYPE_SIMPLE);
             SimpleTrigger simpleTrigger = (SimpleTrigger) getTrigger();
             facade.setRepeatInterval(simpleTrigger.getRepeatInterval() / 60000);
         } else {
             facade.setTriggerType(Constants.TRIGGER_TYPE_CRON);
             CronTrigger cronTrigger = (CronTrigger) getTrigger();
             facade.setCronExpression(cronTrigger.getCronExpression());
         }
         facade.setBuildNecessaryCondition(getBuildNecessaryCondition());
         facade.setAssociatedBuilderNames(getAssociatedBuilderNames());
         facade.setAssociatedPostbuilderNames(getAssociatedPostbuilderNames());
         facade.setPostbuildStrategy(getPostbuildStrategy());
         facade.setBuildType(getBuildType());
         facade.setLabelStrategy(getLabelStrategy());
         facade.setNotifyStrategy(getNotifyStrategy());
         facade.setTriggerDependencyStrategy(getTriggerDependencyStrategy());
         facade.setBuildCleanupStrategy(getBuildCleanupStrategy());
         facade.setBuildCleanupStrategyData(getBuildCleanupStrategyData());
         facade.setStatus(getStatus());
         facade.setStatusDate(getStatusDate());
         facade.setUrl(getUrl());
         facade.setProjectId(getProject().getId());
         facade.setDependentScheduleIds(getDependentScheduleIds());
         facade.setWorkingPath(getWorkingPath());
         return facade;
     }
 
     public void setFacade(ScheduleFacade facade) {
         setDescription(facade.getDescription());
         setNextVersion(facade.getNextVersion());
         if (facade.getTriggerType() == Constants.TRIGGER_TYPE_MANUAL)
             setTrigger(null);
         else if (facade.getTriggerType() == Constants.TRIGGER_TYPE_SIMPLE) {
             SimpleTrigger simpleTrigger = new SimpleTrigger();
             simpleTrigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
             simpleTrigger.setRepeatInterval(facade.getRepeatInterval() * 60000);
             setTrigger(simpleTrigger);
         } else {
             CronTrigger cronTrigger = new CronTrigger();
             try {
                 cronTrigger.setCronExpression(facade.getCronExpression());
             } catch (ParseException e) {
                 throw new RuntimeException("Invalid cron expression: " + facade.getCronExpression());
             }
             setTrigger(cronTrigger);
         }
 
         setBuildNecessaryCondition(facade.getBuildNecessaryCondition());
         setAssociatedBuilderNames(facade.getAssociatedBuilderNames());
         setAssociatedPostbuilderNames(facade.getAssociatedPostbuilderNames());
         setBuildType(facade.getBuildType());
         setLabelStrategy(facade.getLabelStrategy());
         setNotifyStrategy(facade.getNotifyStrategy());
         setPostbuildStrategy(facade.getPostbuildStrategy());
         setTriggerDependencyStrategy(facade.getTriggerDependencyStrategy());
         setBuildCleanupStrategy(facade.getBuildCleanupStrategy());
         setBuildCleanupStrategyData(facade.getBuildCleanupStrategyData());
         setDependentScheduleIds(facade.getDependentScheduleIds());
         setStatus(facade.getStatus());
         setStatusDate(facade.getStatusDate());
         setWorkingPath(facade.getWorkingPath());
     }
 
     /**
      * Get status of this schedule
      * @return one value of {@link com.luntsys.luntbuild.facades.Constants#SCHEDULE_STATUS_FAILED},
      * {@link com.luntsys.luntbuild.facades.Constants#SCHEDULE_STATUS_RUNNING}, and
      * {@link com.luntsys.luntbuild.facades.Constants#SCHEDULE_STATUS_SUCCESS}
      */
     public int getStatus() {
         return status;
     }
 
     /**
      * Set status of this schedule
      * @param status refer to return value of {@link #getStatus()}
      */
     public void setStatus(int status) {
         this.status = status;
     }
 
     /**
      * Get the date when this schedule get its current status
      * @return date when current status occurs
      */
     public Date getStatusDate() {
         return statusDate;
     }
 
     /**
      * Set date when this schedule get its current status
      * @param statusDate
      */
     public void setStatusDate(Date statusDate) {
         this.statusDate = statusDate;
     }
 
     /**
      * Get list of builds belonging to this schedule
      * @return list of builds belonging to this schedule
      */
     public Set getBuilds() {
         if (builds == null)
             builds = new HashSet();
         return builds;
     }
 
     /**
      * Set list of builds belonging to this schedule
      * @param builds
      */
     public void setBuilds(Set builds) {
         this.builds = builds;
     }
 
     /**
      * Validates post-build strategy
      *
      * @param postbuildStrategy
      * @throws ValidationException
      */
     public static void validatePostbuildStrategy(int postbuildStrategy) {
         if (postbuildStrategy != Constants.POSTBUILD_ALWAYS && postbuildStrategy != Constants.POSTBUILD_IF_FAILED &&
                 postbuildStrategy != Constants.POSTBUILD_IF_SUCCESS && postbuildStrategy != com.luntsys.luntbuild.facades.Constants.POSTBUILD_NONE)
             throw new ValidationException("Invalid post-build strategy");
     }
 
     /**
      * Validate build type
      *
      * @param buildType
      * @throws ValidationException
      */
     public static void validateBuildType(int buildType) {
         if (buildType != com.luntsys.luntbuild.facades.Constants.BUILD_TYPE_CLEAN && buildType != Constants.BUILD_TYPE_INCREMENT)
             throw new ValidationException("Invalid build type");
     }
 
     /**
      * Validate dependent triggering strategy
      *
      * @param triggerDependencyStrategy
      */
     public static void validateTriggerDependencyStrategy(int triggerDependencyStrategy) {
         if (triggerDependencyStrategy != Constants.TRIGGER_ALL_DEPENDENT_SCHEDULES &&
                 triggerDependencyStrategy != com.luntsys.luntbuild.facades.Constants.TRIGGER_NONE_DEPENDENT_SCHEDULES &&
                 triggerDependencyStrategy != Constants.TRIGGER_SCHEDULES_DEPENDS_ON_THIS &&
                 triggerDependencyStrategy != Constants.TRIGGER_SCHEDULES_THIS_DEPENDS_ON)
             throw new ValidationException("Invalid dependent triggering strategy");
     }
 
     public static void validateBuildNecessaryCondition(String buildNecessaryCondition) {
         if (Luntbuild.isEmpty(buildNecessaryCondition))
             throw new ValidationException("Invalid build necessary condition: should not be empty!");
         if (buildNecessaryCondition.indexOf(Luntbuild.TRIGGER_NAME_SEPERATOR) != -1)
             throw new ValidationException("Invalid build necessary condition: should not contain sequence \"" +
                     Luntbuild.TRIGGER_NAME_SEPERATOR + "\"");
         try {
             Ognl.parseExpression(buildNecessaryCondition);
         } catch (OgnlException e) {
             throw new ValidationException("Invalid build necessary condition: " + buildNecessaryCondition +
                     ", reason: " + e.getMessage());
         }
     }
 
     /**
      * Validate the label strategy
      *
      * @param labelStrategy
      * @throws ValidationException
      */
     public static void validateLabelStrategy(int labelStrategy) {
         if (labelStrategy != Constants.LABEL_ALWAYS && labelStrategy != Constants.LABEL_IF_SUCCESS &&
                 labelStrategy != Constants.LABEL_NONE)
             throw new ValidationException("Invalid label strategy");
     }
 
     /**
      * Validate the notify strategy
      *
      * @param notifyStrategy
      * @throws ValidationException
      */
     public static void validateNotifyStrategy(int notifyStrategy) {
         if (notifyStrategy != Constants.NOTIFY_WHEN_STATUS_CHANGED && notifyStrategy != Constants.NOTIFY_ALWAYS &&
                 notifyStrategy != Constants.NOTIFY_IF_FAILED && notifyStrategy != Constants.NOTIFY_IF_SUCCESS &&
                 notifyStrategy != Constants.NOTIFY_NONE)
             throw new ValidationException("Invalid notify strategy");
     }
 
     /**
      * Get url to this schedule
      * @return url to this schedule
      */
     public String getUrl() {
         return Luntbuild.getServletUrl() + "?service=external/ProjectPage&sp=l" +
                 getProject().getId() + "&sp=l" + ProjectPage.SERVICE_PARAMETER_SCHEDULES;
     }
 
     /**
      * Get publishing directory of this schedule, builds generated in this schedule will be put under this
      * directory
      * @return publishing directory of this schedule
      */
     public String getPublishDir() {
         String publishDir = getProject().getPublishDir() + File.separator + getName();
         try {
             publishDir = new File(publishDir).getCanonicalPath();
             return publishDir.replaceAll("\\\\", "\\\\\\\\"); // in order to keep back slash for ognl expression evaluation
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public boolean isSelected() {
         return selected;
     }
 
     public void setSelected(boolean selected) {
         this.selected = selected;
     }
 
     /**
      * Get list of schedule ids this schedule depends on
      * @return list of schedule ids this schedule depends on
      */
     public List getDependentScheduleIds() {
         if (dependentScheduleIds == null) {
             dependentScheduleIds = new ArrayList();
         }
         return dependentScheduleIds;
     }
 
     /**
      * Set list of schedule ids this schedule depends on
      * @param dependentScheduleIds
      */
     public void setDependentScheduleIds(List dependentScheduleIds) {
         this.dependentScheduleIds = dependentScheduleIds;
     }
 
     public Set getDependsOn(Object userData) {
         List schedules = (List) userData;
         Set dependsOn = new HashSet();
         Iterator itId = getDependentScheduleIds().iterator();
         while (itId.hasNext()) {
             Schedule schedule = new Schedule();
             schedule.setId(((Long) itId.next()).longValue());
             int index = schedules.indexOf(schedule);
             if (index != -1)
                 dependsOn.add(schedules.get(index));
         }
         return dependsOn;
     }
 
     public void visit(Object userData) {
         logger.debug("Visit schedule \"" + getProject().getName() + "/" + getName() + "\"");
         try {
             SecurityHelper.runAsSiteAdmin();
             BuildParams buildParams;
             if (userData == null)
                 buildParams = getBuildParams();
             else
                 buildParams = (com.luntsys.luntbuild.facades.BuildParams) userData;
 
             SimpleTrigger trigger = new SimpleTrigger();
             trigger.setGroup(BuildGenerator.DEPENDENT_GROUP);
             trigger.setName(constructTriggerName(buildParams));
             trigger.setRepeatCount(0);
             trigger.setRepeatInterval(0);
             trigger.setStartTime(new Date(System.currentTimeMillis()));
             Luntbuild.getSchedService().scheduleBuild(this, trigger);
             while (true) {
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e) {
                     // ignore
                 }
                 if (!Luntbuild.getSchedService().isTriggerAvailable(trigger))
                     break;
             }
         } catch (Throwable throwable) {
             logger.error("Visit of schedule \"" + getProject().getName() + "/" + getName() + "\" failed: " + throwable.getMessage());
         }
     }
 
     /**
      * Get build necessary condition for this schedule
      * @return build necessary condition
      */
     public String getBuildNecessaryCondition() {
         return buildNecessaryCondition;
     }
 
     /**
      * Sete build necessary condition of this schedule
      * @param buildNecessaryCondition
      */
     public void setBuildNecessaryCondition(String buildNecessaryCondition) {
         this.buildNecessaryCondition = buildNecessaryCondition;
     }
 
     /**
      * Determines if content of vcs has been modified for project of schedule since last build of this schedule.
      * Need the following thread local variables been set in {@link com.luntsys.luntbuild.utility.OgnlHelper}
      * <i> antProject
      * <i> workingSchedule
      * <i> revisions
      *
      * @return if content of vcs has been modified
      */
     public boolean isVcsModified() {
         Schedule workingSchedule = OgnlHelper.getWorkingSchedule();
         Revisions revisions = new Revisions();
         Build build = Luntbuild.getDao().loadLastBuild(this);
         if (build == null) {
             if (OgnlHelper.getRevisions() == null && workingSchedule.getProject() == getProject()) {
                 revisions.getChangeLogs().add("========== Change log ignored: base build does not exist ==========");
                 OgnlHelper.setRevisions(revisions);
             }
             return true;
         }
         return getProject().isVcsModifiedSince(build.getStartDate());
     }
 
     /**
      * Whether or not there are new builds generated in schedules which current schedule depends on
      * @return Whether or not there are new builds generated
      */
     public boolean isDependencyNewer() {
         Build lastBuild = Luntbuild.getDao().loadLastBuild(this);
         if (lastBuild == null)
             return true;
         Set dependents = getDependsOn(Luntbuild.getDao().loadSchedules());
         boolean isDependentNewer = false;
         Iterator it = dependents.iterator();
         while (it.hasNext()) {
             Schedule dependentSchedule = (Schedule) it.next();
             Build lastBuildOfDependentSchedule = Luntbuild.getDao().loadLastBuild(dependentSchedule);
             if (lastBuildOfDependentSchedule != null && lastBuildOfDependentSchedule.getId() > lastBuild.getId()) {
                 isDependentNewer = true;
                 break;
             }
         }
         return isDependentNewer;
     }
 
     public boolean isDependencySuccessful() {
         return getNotSatisfiedDependency() == null;
     }
 
     public Schedule getNotSatisfiedDependency() {
         Set dependents = getDependsOn(Luntbuild.getDao().loadSchedules());
         Iterator it = dependents.iterator();
         while (it.hasNext()) {
             Schedule dependentSchedule = (Schedule) it.next();
             Build lastBuildOfDependentSchedule = Luntbuild.getDao().loadLastBuild(dependentSchedule);
             if (lastBuildOfDependentSchedule == null || lastBuildOfDependentSchedule.getStatus() != Constants.BUILD_STATUS_SUCCESS)
                 return dependentSchedule;
         }
         return null;
     }
 
     /** Returns true, always run build.
      * @return true, always run build.
      */
     public boolean isAlways() {
         return true;
     }
 
     /**
      * Returns false, never run build.
      * @return false, never run build.
      */
     public boolean isNever() {
         return false;
     }
 
     /**
      * Return true if last build is failed
      * @return true if last build is failed
      */
     public boolean isAlwaysIfFailed() {
         Build lastBuild = getLastBuild();
         if (lastBuild == null || lastBuild.getStatus() == Constants.BUILD_STATUS_FAILED)
             return true;
         else
             return isVcsModified() || isDependencyNewer();
     }
 
     /**
      * Get system object. Mainly used for ognl evaluation
      * @return system object
      */
     public OgnlHelper getSystem() {
         return new OgnlHelper();
     }
 
     /**
      * Get dependent triggering strategy, should be one value of
      * @return one value of
      * {@link com.luntsys.luntbuild.facades.Constants#TRIGGER_ALL_DEPENDENT_SCHEDULES},
      * {@link com.luntsys.luntbuild.facades.Constants#TRIGGER_NONE_DEPENDENT_SCHEDULES},
      * {@link com.luntsys.luntbuild.facades.Constants#TRIGGER_SCHEDULES_DEPENDS_ON_THIS},
      * {@link com.luntsys.luntbuild.facades.Constants#TRIGGER_SCHEDULES_THIS_DEPENDS_ON}
 
      */
     public int getTriggerDependencyStrategy() {
         return triggerDependencyStrategy;
     }
 
     /**
      * Set dependent triggering strategy
      * @param triggerDependencyStrategy see reteurn value of {@link Schedule#getTriggerDependencyStrategy()}
      */
     public void setTriggerDependencyStrategy(int triggerDependencyStrategy) {
         this.triggerDependencyStrategy = triggerDependencyStrategy;
     }
 
     public static String constructTriggerName(com.luntsys.luntbuild.facades.BuildParams buildParams) {
         String triggerName = buildParams.getBuildNecessaryCondition() +
                 Luntbuild.TRIGGER_NAME_SEPERATOR + buildParams.getBuildType() +
                 Luntbuild.TRIGGER_NAME_SEPERATOR + buildParams.getBuildVersion() +
                 Luntbuild.TRIGGER_NAME_SEPERATOR + buildParams.getLabelStrategy() +
                 Luntbuild.TRIGGER_NAME_SEPERATOR + buildParams.getNotifyStrategy() +
                 Luntbuild.TRIGGER_NAME_SEPERATOR + buildParams.getPostbuildStrategy() +
                 Luntbuild.TRIGGER_NAME_SEPERATOR + buildParams.getTriggerDependencyStrategy() +
                 Luntbuild.TRIGGER_NAME_SEPERATOR + buildParams.getScheduleId() +
                 Luntbuild.TRIGGER_NAME_SEPERATOR + System.currentTimeMillis() + Thread.currentThread().hashCode();
         return triggerName;
     }
 
     public static com.luntsys.luntbuild.facades.BuildParams parseTriggerName(String triggerName) {
         BuildParams buildParams = new com.luntsys.luntbuild.facades.BuildParams();
         String fields[] = triggerName.split("\\" + Luntbuild.TRIGGER_NAME_SEPERATOR);
         buildParams.setBuildNecessaryCondition(fields[0]);
         buildParams.setBuildType(new Integer(fields[1]).intValue());
         buildParams.setBuildVersion(fields[2]);
         buildParams.setLabelStrategy(new Integer(fields[3]).intValue());
         buildParams.setNotifyStrategy(new Integer(fields[4]).intValue());
         buildParams.setPostbuildStrategy(new Integer(fields[5]).intValue());
         buildParams.setTriggerDependencyStrategy(new Integer(fields[6]).intValue());
         buildParams.setScheduleId(new Integer(fields[7]).intValue());
         return buildParams;
     }
 
     public com.luntsys.luntbuild.facades.BuildParams getBuildParams() {
         com.luntsys.luntbuild.facades.BuildParams buildParams = new BuildParams();
         buildParams.setBuildNecessaryCondition(getBuildNecessaryCondition());
         buildParams.setBuildType(getBuildType());
         buildParams.setBuildVersion("");
         buildParams.setLabelStrategy(getLabelStrategy());
         buildParams.setNotifyStrategy(getNotifyStrategy());
         buildParams.setPostbuildStrategy(getPostbuildStrategy());
         buildParams.setTriggerDependencyStrategy(getTriggerDependencyStrategy());
         buildParams.setScheduleId(getId());
         return buildParams;
     }
 
     public String toString() {
         return getProject().getName() + "/" + getName();
     }
 
     /**
      * Get latest build of this schedule, maybe null if there are not any builds in
      * this schedule
      * @return latest build of this schedule
      */
     public Build getLastBuild() {
         return Luntbuild.getDao().loadLastBuild(this);
     }
 
     /**
      * Get last success build for this schedule. Maybe null if there are not any successful
      * builds in this schedule
      * @return last success build for this schedule
      */
     public Build getLastSuccessBuild() {
         return Luntbuild.getDao().loadLastSuccessBuild(this);
     }
 
     /**
      * Get running build of this schedule. Maybe null if there is no running builds in
      * this schedule when this method is called
      * @return running build of this schedule
      */
     public Build getRunningBuild() {
         return Luntbuild.getDao().loadRunningBuild(this);
     }
 
     /**
      * Get build with specified version
      * @param version
      * @return build with specified version
      */
     public Build getBuild(String version) {
         return Luntbuild.getDao().loadBuild(getProject().getName(), getName(), version);
     }
 
     /**
      * Empty method, only available to conform with Ognl indexed property requirement
      * @param version
      * @param build
      */
     public void setBuild(String version, Build build) {
         // empty method, just used to conform with ognl index property
     }
 
     public void setCurrentThread(Thread currentThread) {
         currentThreads.put(new Long(getId()), currentThread);
     }
 
     public Thread getCurrentThread() {
         return (Thread) currentThreads.get(new Long(getId()));
     }
 
     /**
      * Get build cleanup strategy
      * @return one value of {@link com.luntsys.luntbuild.facades.Constants#BUILD_CLEANUP_NONE},
      * {@link com.luntsys.luntbuild.facades.Constants#BUILD_KEEP_BY_COUNT},
      * {@link com.luntsys.luntbuild.facades.Constants#BUILD_KEEP_BY_DAYS}
      */
     public int getBuildCleanupStrategy() {
         return buildCleanupStrategy;
     }
 
     /**
      * Set build cleanup strategy
      * @param buildCleanupStrategy see return value of {@link Schedule#getBuildCleanupStrategy()}
      */
     public void setBuildCleanupStrategy(int buildCleanupStrategy) {
         this.buildCleanupStrategy = buildCleanupStrategy;
     }
 
     /**
      * Get build cleanup data
      * @return build cleanup data
      */
     public String getBuildCleanupStrategyData() {
         return buildCleanupStrategyData;
     }
 
     /**
      * Set build cleanup data
      * @param buildCleanupStrategyData
      */
     public void setBuildCleanupStrategyData(String buildCleanupStrategyData) {
         this.buildCleanupStrategyData = buildCleanupStrategyData;
     }
 
     /**
      * Set the build version number for the next build. This property is updated automatically
      * by the building process, And can also be adjusted manually from user interface
      *
      * @param nextVersion
      */
     public void setNextVersion(String nextVersion) {
         this.nextVersion = nextVersion;
     }
 
     /**
      * Get next build version of this schedule
      * @return next build version of this schedule
      */
     public String getNextVersion() {
         return nextVersion;
     }
 
     public void validateBuildVersion(String buildVersion) {
         if (Luntbuild.isEmpty(buildVersion))
             throw new ValidationException("version string can not be empty!");
         try {
             OgnlHelper.setTestMode(true);
             OgnlHelper.setAntProject(Luntbuild.createAntProject());
             String versionValue = Luntbuild.evaluateExpression(this, buildVersion);
             if (versionValue.matches(".*[/\\\\:*?\"<>|$,;@#].*"))
                 throw new ValidationException("version string should not contain characters: /\\:*?\"<>|$,;@#");
             if (!versionValue.matches(".*[\\d].*"))
                 throw new ValidationException("version string should contain digit");
            if (!versionValue.matches(".*[\\s].*"))
                throw new ValidationException("version string should contain whitespace");
         } catch (Throwable e) {
             throw new ValidationException("Error parsing version string: " + buildVersion +
                     ", reason: "+ Luntbuild.getExceptionMessage(e));
         }
     }
 
     /**
      * Get list of associated builder names
      * @return list of associated builder names
      */
     public List getAssociatedBuilderNames() {
         if (associatedBuilderNames == null)
             associatedBuilderNames = new ArrayList();
         Iterator it = associatedBuilderNames.iterator();
         while (it.hasNext()) {
             String name = (String) it.next();
             if (getProject().getBuilderByName(name) == null)
                 it.remove();
         }
         return associatedBuilderNames;
     }
 
     /**
      * Get list associated builders
      * @return list associated builders
      */
     public List getAssociatedBuilders() {
         List associatedBuilders = new ArrayList();
         Iterator it = associatedBuilderNames.iterator();
         while (it.hasNext()) {
             String name = (String) it.next();
             Builder builder = getProject().getBuilderByName(name);
             if (builder != null)
                 associatedBuilders.add(builder);
         }
         return associatedBuilders;
     }
 
     /**
      * Set list of associated builder names for this schedule
      * @param associatedBuilderNames
      */
     public void setAssociatedBuilderNames(List associatedBuilderNames) {
         this.associatedBuilderNames = associatedBuilderNames;
     }
 
     /**
      * Get list of associated post-builder names
      * @return list of associated post-builder names
      */
     public List getAssociatedPostbuilderNames() {
         if (associatedPostbuilderNames == null)
             associatedPostbuilderNames = new ArrayList();
         Iterator it = associatedPostbuilderNames.iterator();
         while (it.hasNext()) {
             String name = (String) it.next();
             if (getProject().getBuilderByName(name) == null)
                 it.remove();
         }
         return associatedPostbuilderNames;
     }
 
     /**
      * Get list of associated post-builders
      * @return list of associated post-builders
      */
     public List getAssociatedPostbuilders() {
         List associatedPostbuilders = new ArrayList();
         Iterator it = associatedPostbuilderNames.iterator();
         while (it.hasNext()) {
             String name = (String) it.next();
             Builder builder = getProject().getBuilderByName(name);
             if (builder != null)
                 associatedPostbuilders.add(builder);
         }
         return associatedPostbuilders;
     }
 
     /**
      * Set list of associated post-builder names for this schedule
      * @param associatedPostbuilderNames
      */
     public void setAssociatedPostbuilderNames(List associatedPostbuilderNames) {
         this.associatedPostbuilderNames = associatedPostbuilderNames;
     }
 
     /**
      * Get list of schedules
      * @return list of schedules
      */
     public List getDependentSchedules() {
         if (dependentSchedules == null)
             dependentSchedules = new ArrayList();
         return dependentSchedules;
     }
 
     /**
      * Set list of dependent schedules
      * @param dependentSchedules
      */
     public void setDependentSchedules(List dependentSchedules) {
         this.dependentSchedules = dependentSchedules;
     }
 
     /**
      * Get quartz job name for this schedule
      * @return quartz job name for this schedule
      */
     public String getJobName() {
         String workingDir = getWorkDirRaw();
         try {
             workingDir = new File(workingDir).getCanonicalPath();
             return workingDir.replaceAll("[:\\s/\\\\]", "-");
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Get working path setting of this schedule. NOTE: Do not call this method to  get
      * working directory of this schedule. Instead, you should call {@link Schedule#getWorkDirRaw()}
      * @return working path setting of this schedule
      */
     public String getWorkingPath() {
         return workingPath;
     }
 
     /**
      * Set working path of this schedule.
      * @param workingPath
      */
     public void setWorkingPath(String workingPath) {
         this.workingPath = workingPath;
     }
 }
