 /*
  * Copyright luntsys (c) 2004-2005,
  * Date: 2004-5-20
  * Time: 13:18:04
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
 import com.luntsys.luntbuild.builders.Builder;
 import com.luntsys.luntbuild.facades.lb12.BuildFacade;
 import com.luntsys.luntbuild.facades.lb12.BuilderFacade;
 import com.luntsys.luntbuild.facades.lb12.VcsFacade;
 import com.luntsys.luntbuild.reports.Report;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.utility.LuntbuildLogger;
 import com.luntsys.luntbuild.utility.OgnlHelper;
 import com.luntsys.luntbuild.vcs.Vcs;
 import com.luntsys.luntbuild.web.Home;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * A build from a <code>Schedule</code>. Represents an execution result of a particular schedule.
  * 
  * <p>This is a hibernate mapping class.</p>
  *
  * @author robin shine
  * @see Schedule
  */
 public class Build {
     private long id;
 
     /**
      * Status of current build
      */
     private int status;
 
     /**
      * The date when this build is started
      */
     private Date startDate;
 
     /**
      * The date when this build is finished
      */
     private Date endDate;
 
     /**
      * Version number of this build
      */
     private String version;
 
     /**
      * Label strategy of this build
      */
     private int labelStrategy;
 
     /**
      * Post-build strategy of this build
      */
     private int postbuildStrategy;
 
     /**
      * Does this build have corresponding label in the vcs repository for
      * head revisions configured for this build's vcs setting?
      */
     private boolean haveLabelOnHead = false;
 
     /**
      * Whether or not this is a clean build
      */
     private int buildType;
 
     /**
      * Is this build a rebuild?
      */
     private boolean rebuild;
 
     private Schedule schedule;
 
     /**
      * Version control systems used to construct this build
      */
     private List vcsList;
 
     /**
      * Builders used to construct this build
      */
     private List builderList;
 
     /**
      * Post-builders used to construct this build
      */
     private List postbuilderList;
 
     private static transient Map loggersById = new HashMap();
 
     private String user = null;
     
 	/**
 	 * Checks if this build has a corresponding label in the VCS repository.
 	 * 
 	 * @return <code>true</code> if this build has a corresponding label in the VCS repository
 	 */
     public boolean isHaveLabelOnHead() {
         return haveLabelOnHead;
     }
 
 	/**
 	 * Sets <code>true</code> or <code>false</code> if this build has a corresponding label in the VCS repository.
 	 * 
 	 * @param haveLabelOnHead set <code>true</code> if this build has a label in the VCS repository
 	 */
     public void setHaveLabelOnHead(boolean haveLabelOnHead) {
         this.haveLabelOnHead = haveLabelOnHead;
     }
 
 	/**
 	 * Gets the identifer of this build.
 	 * 
 	 * @return the identifer of this build
 	 */
     public long getId() {
         return id;
     }
 
     /**
      * Checks if this is a rebuilt build.
      * 
      * @return <code>true</code> if this is a rebuilt build
      */
     public boolean isRebuild() {
         return rebuild;
     }
 
     /**
      * Sets whether this is a rebuilt build.
      * 
      * @param rebuild set <code>true</code> if this is a rebuilt build
      */
     public void setRebuild(boolean rebuild) {
         this.rebuild = rebuild;
     }
 
 	/**
 	 * Sets the identifier of this build, will be called by hibernate.
 	 *
 	 * @param id the identifier of this build
 	 */
     public void setId(long id) {
         this.id = id;
     }
 
     /**
      * Gets the version of this build.
      * 
      * @return the version of this build
      */
     public String getVersion() {
         return version;
     }
 
     /**
      * Gets the version of this build with all spaces escaped.
      * 
      * @return the version of this build
      */
     public String getVersionNoSpace() {
         return version.replaceAll("\\s", "%20");
     }
 
     /**
      * Set version of this build
      * @param version
      */
     public void setVersion(String version) {
         this.version = version;
     }
 
     /**
      * Gets the status of this build.
      * 
      * @return the status of this build
      * @see com.luntsys.luntbuild.facades.Constants#BUILD_STATUS_FAILED
      * @see com.luntsys.luntbuild.facades.Constants#BUILD_STATUS_RUNNING
      * @see com.luntsys.luntbuild.facades.Constants#BUILD_STATUS_SUCCESS
      */
     public int getStatus() {
         return status;
     }
 
     /**
      * Sets the status of this build.
      * 
      * @param status the status of this build
      * @see com.luntsys.luntbuild.facades.Constants#BUILD_STATUS_FAILED
      * @see com.luntsys.luntbuild.facades.Constants#BUILD_STATUS_RUNNING
      * @see com.luntsys.luntbuild.facades.Constants#BUILD_STATUS_SUCCESS
      */
     public void setStatus(int status) {
         this.status = status;
     }
 
     /**
      * Gets the starting date of this build.
      * 
      * @return the starting date of this build
      */
     public Date getStartDate() {
         return startDate;
     }
 
     /**
      * Sets the start date of this build.
      * 
      * @param startDate the start date of this build
      */
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     /**
      * Gets the ending date of this build.
      * 
      * @return the ending date of this build
      */
     public Date getEndDate() {
         return endDate;
     }
 
     /**
      * Sets the ending date of this build.
      * 
      * @param endDate the ending date of this build
      */
     public void setEndDate(Date endDate) {
         this.endDate = endDate;
     }
 
 	/**
 	 * Indicates whether some other object is "equal to" this one.
 	 * 
 	 * @param obj the reference object with which to compare
 	 * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise
 	 */
     public boolean equals(Object obj) {
         if (obj != null && obj instanceof Build) {
             if (getId() == ((Build) obj).getId())
                 return true;
         }
         return false;
     }
 
 	/**
 	 * Returns a hash code value for the object.
 	 * 
 	 * @return a hash code value for this object
 	 * @see #equals(Object)
 	 */
     public int hashCode() {
         return (int) getId();
     }
 
     /**
      * Validates the properties of this build.
      */
     public void validate() {
         // current does nothing
     }
 
     /**
      * Gets the URL of this build.
      * 
      * @return the URL of this build
      */
     public String getUrl() {
         return Luntbuild.getServletUrl() + "?service=external/Home&sp=l" +
                 Home.SERVICE_PARAMETER_BUILD + "&sp=l" + getId();
     }
 
 	/**
 	 * Ensures that the HTML and plain text versions of the build log are up to date, exist, and can be read.
 	 * 
 	 * @return <code>true</code> if the HTML build log exists and can be read
 	 */
     private boolean ensureBuildLog() {
         String publishDir = getPublishDir();
         String buildXmlPath = publishDir + File.separator + BuildGenerator.BUILD_XML_LOG;
         String buildPath = publishDir + File.separator + BuildGenerator.BUILD_HTML_LOG;
         String buildTextPath = publishDir + File.separator + BuildGenerator.BUILD_LOG;
 
         LuntbuildLogger buildLogger = getLogger();
         if (buildLogger != null)
             buildLogger.logHtml(buildXmlPath, Luntbuild.installDir + "/log.xsl", buildPath, buildTextPath);
 
         File f = new File(buildPath);
         return f.exists() && f.canRead();
     }
 
     /**
      * Gets the build log URL of this build.
      * 
      * @return the build log URL of this build
      */
     public String getBuildLogUrl() {
         if (ensureBuildLog())
             return getPublishUrl() + "/" + BuildGenerator.BUILD_HTML_LOG;
         else
             return null;
     }
 
     /**
      * Gets the system log URL.
      * 
      * @return the system log URL
      */
     public String getSystemLogUrl() {
         String servletUrl = Luntbuild.getServletUrl();
         if (!servletUrl.endsWith("app.do"))
             throw new RuntimeException("Invalid servlet url: " + servletUrl);
         return servletUrl.substring(0, servletUrl.length() - 6) + "logs/" +
             Luntbuild.log4jFileName;
     }
 
 	/**
 	 * Ensures that the revision log is up to date, exist, and can be read.
 	 * 
 	 * @return <code>true</code> if the revision log exists and can be read
 	 */
     private boolean ensureRevisionLog() {
         String publishDir = getPublishDir();
         String revisionLogFile = publishDir + "/" + BuildGenerator.REVISION_HTML_LOG;
         File f = new File(revisionLogFile);
         return f.exists() && f.canRead();
     }
 
     /**
      * Gets the revision log URL of this build.
      *
      * @return the revision log URL of this build
      */
     public String getRevisionLogUrl() {
         if (ensureRevisionLog())
             return getPublishUrl() + "/" + BuildGenerator.REVISION_HTML_LOG;
         else
             return null;
     }
 
     /**
      * Gets the facade of this build.
      * 
      * @return the facade of this build
      */
     public BuildFacade getFacade() {
         BuildFacade facade = new BuildFacade();
         facade.setBuildType(getBuildType());
         facade.setEndDate(getEndDate());
         facade.setHaveLabelOnHead(isHaveLabelOnHead());
         facade.setId(getId());
         facade.setLabelStrategy(getLabelStrategy());
         facade.setPostbuildStrategy(getPostbuildStrategy());
         facade.setRebuild(isRebuild());
         facade.setScheduleId(getSchedule().getId());
         facade.setStartDate(getStartDate());
         facade.setStatus(getStatus());
         facade.setVersion(getVersion());
         Iterator it = getVcsList().iterator();
         while (it.hasNext()) {
             Vcs vcs = (Vcs) it.next();
             facade.getVcsList().add(vcs.getFacade());
         }
         it = getBuilderList().iterator();
         while (it.hasNext()) {
             Builder builder = (Builder) it.next();
             facade.getBuilderList().add(builder.getFacade());
         }
         it = getPostbuilderList().iterator();
         while (it.hasNext()) {
             Builder builder = (Builder) it.next();
             facade.getPostbuilderList().add(builder.getFacade());
         }
         facade.setUrl(getUrl());
         facade.setBuildLogUrl(getBuildLogUrl());
         facade.setRevisionLogUrl(getRevisionLogUrl());
         facade.setSystemLogUrl(getSystemLogUrl());
         return facade;
     }
 
     /**
      * Sets the facade of this build.
      * 
      * @param facade the facade the build facade
      * @throws RuntimeException if unable to instantiate a builder or VCS adaptor
      */
     public void setFacade(BuildFacade facade) {
         setBuilderList(facade.getBuilderList());
         setBuildType(facade.getBuildType());
         setEndDate(facade.getEndDate());
         setHaveLabelOnHead(facade.isHaveLabelOnHead());
         setLabelStrategy(facade.getLabelStrategy());
         setPostbuilderList(facade.getPostbuilderList());
         setPostbuildStrategy(facade.getPostbuildStrategy());
         setRebuild(facade.isRebuild());
         setStartDate(facade.getStartDate());
         setStatus(facade.getStatus());
         setVersion(facade.getVersion());
         try {
             getVcsList().clear();
             Iterator it = facade.getVcsList().iterator();
             while (it.hasNext()) {
                 VcsFacade vcsFacade = (VcsFacade) it.next();
                 Vcs vcs = (Vcs) Class.forName(vcsFacade.getVcsClassName()).newInstance();
                 vcs.setFacade(vcsFacade);
                 getVcsList().add(vcs);
             }
             getBuilderList().clear();
             it = facade.getBuilderList().iterator();
             while (it.hasNext()) {
                 BuilderFacade builderFacade = (BuilderFacade) it.next();
                 Builder builder = (Builder) Class.forName(builderFacade.getBuilderClassName()).newInstance();
                 builder.setFacade(builderFacade);
                 getBuilderList().add(builder);
             }
             getPostbuilderList().clear();
             it = facade.getPostbuilderList().iterator();
             while (it.hasNext()) {
                 BuilderFacade builderFacade = (BuilderFacade) it.next();
                 Builder builder = (Builder) Class.forName(builderFacade.getBuilderClassName()).newInstance();
                 builder.setFacade(builderFacade);
                 getPostbuilderList().add(builder);
             }
         } catch (InstantiationException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Gets the publish directory for current build. Publish directory is used to hold
      * output of this build, including build log and build artifacts, etc.
      * 
      * @return the publish directory for current build
      */
     public String getPublishDir() {
         String publishDir = getSchedule().getPublishDir() + File.separator + getVersion();
         try {
             publishDir = new File(publishDir).getCanonicalPath();
             return publishDir.replaceAll("\\\\", "\\\\\\\\"); // in order to keep back slash for ognl expression evaluation
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Gets the publish URL for current build. Publish URL is used to access
      * output of this build, including build log and build artifacts, etc.
      * 
      * @return the publish URL for current build
      */
     public String getPublishUrl() {
         String servletUrl = Luntbuild.getServletUrl();
         if (!servletUrl.endsWith("app.do"))
             throw new RuntimeException("Invalid servlet url: " + servletUrl);
         return servletUrl.substring(0, servletUrl.length() - 6) + "publish/" +
             getSchedule().getProject().getName() + "/" + getSchedule().getName() + "/" +
             getVersionNoSpace();
     }
 
     /**
      * Gets the artifacts directory where hold artifacts for this build.
      * 
      * @return the artifacts directory where hold artifacts for this build
      */
     public String getArtifactsDir() {
         try {
             String artifactsDir =  new File(getPublishDir() + File.separator +
                     Builder.ARTIFACTS_DIR).getCanonicalPath();
             return artifactsDir.replaceAll("\\\\", "\\\\\\\\"); // in order to keep back slash for ognl expression evaluation
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Gets the directory where to hold Junit html report stuff.
      * 
      * @deprecated Replaced with <code>getReportDir("JUnit")</code>.
      * 
      * @return the directory where to hold Junit html report stuff
      */
     public String getJunitHtmlReportDir() {
         return getReportDir("JUnit");
     }
 
     /**
      * Gets the description of the specified report.
      * 
      * @param report_name the name of the report
      * @return the report description
      * @throws RuntimeException if no report with that name was found
      */
     public String getReportDescription(String report_name) {
         Report report = (Report) Luntbuild.reports.get(report_name);
         if (report == null)
             throw new RuntimeException("Report named \"" + report_name + "\" not found.");
 
         return report.getReportDescription();
     }
 
     /**
      * Gets the directory of the specified report.
      * 
      * @param report_name the name of the report
      * @return the report directory
      * @throws RuntimeException if no report with that name was found
      */
     public String getReportDir(String report_name) {
         try {
             Report report = (Report) Luntbuild.reports.get(report_name);
             if (report == null)
                 throw new RuntimeException("Report named \"" + report_name + "\" not found.");
 
             String reportDir =  new File(getPublishDir() + File.separator +
                     report.getReportDir()).getCanonicalPath();
             return reportDir.replaceAll("\\\\", "\\\\\\\\"); // in order to keep back slash for ognl expression evaluation
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Gets the URL of the specified report.
      * 
      * @param report_name the name of the report
      * @return the report URL
      * @throws RuntimeException if no report with that name was found
      */
     public String getReportUrl(String report_name) {
         Report report = (Report) Luntbuild.reports.get(report_name);
         if (report == null)
             throw new RuntimeException("Report named \"" + report_name + "\" not found.");
 
        String report_url = report.getReportUrl(getReportDir(report_name));
         if (report_url != null)
             return getPublishUrl() + "/" + report_url;
         else
             return null;
     }
 
     /**
      * Gets the icon of the specified report.
      * 
      * @param report_name the name of the report
      * @return the icon
      * @throws RuntimeException if no report with that name was found
      */
     public String getReportIcon(String report_name) {
         Report report = (Report) Luntbuild.reports.get(report_name);
         if (report == null)
             throw new RuntimeException("Report named \"" + report_name + "\" not found.");
 
         return report.getIcon();
     }
 
     /**
      * Gets the label strategy for this build.
      * 
      * @return the label strategy for this build
      * @see com.luntsys.luntbuild.facades.Constants#LABEL_IF_SUCCESS
      * @see com.luntsys.luntbuild.facades.Constants#LABEL_ALWAYS
      * @see com.luntsys.luntbuild.facades.Constants#LABEL_NONE
      */
     public int getLabelStrategy() {
         return labelStrategy;
     }
 
     /**
      * Sets the label strategy for this build.
      * 
      * @param labelStrategy the labelStrategy the label strategy for this build
      * @see com.luntsys.luntbuild.facades.Constants#LABEL_IF_SUCCESS
      * @see com.luntsys.luntbuild.facades.Constants#LABEL_ALWAYS
      * @see com.luntsys.luntbuild.facades.Constants#LABEL_NONE
      */
     public void setLabelStrategy(int labelStrategy) {
         this.labelStrategy = labelStrategy;
     }
 
     /**
      * Gets the post-build strategy for this build.
      * 
      * @return the post-build strategy for this build
      * @see com.luntsys.luntbuild.facades.Constants#POSTBUILD_NONE
      * @see com.luntsys.luntbuild.facades.Constants#POSTBUILD_IF_SUCCESS
      * @see com.luntsys.luntbuild.facades.Constants#POSTBUILD_IF_FAILED
      * @see com.luntsys.luntbuild.facades.Constants#POSTBUILD_ALWAYS
      */
     public int getPostbuildStrategy() {
         return postbuildStrategy;
     }
 
     /**
      * Sets the post-build strategy for this build.
      * 
      * @param postbuildStrategy the postbuildStrategy the post-build strategy for this build
      * @see com.luntsys.luntbuild.facades.Constants#POSTBUILD_NONE
      * @see com.luntsys.luntbuild.facades.Constants#POSTBUILD_IF_SUCCESS
      * @see com.luntsys.luntbuild.facades.Constants#POSTBUILD_IF_FAILED
      * @see com.luntsys.luntbuild.facades.Constants#POSTBUILD_ALWAYS
      */
     public void setPostbuildStrategy(int postbuildStrategy) {
         this.postbuildStrategy = postbuildStrategy;
     }
 
     /**
      * Gets the build type of this build.
      * 
      * @return the build type of this build
      * @see com.luntsys.luntbuild.facades.Constants#BUILD_TYPE_CLEAN
      * @see com.luntsys.luntbuild.facades.Constants#BUILD_TYPE_INCREMENT
      */
     public int getBuildType() {
         return buildType;
     }
 
     /**
      * Sets the build type of this build.
      * 
      * @param buildType the build type the build type of this build
      * @see com.luntsys.luntbuild.facades.Constants#BUILD_TYPE_CLEAN
      * @see com.luntsys.luntbuild.facades.Constants#BUILD_TYPE_INCREMENT
      */
     public void setBuildType(int buildType) {
         this.buildType = buildType;
     }
 
     /**
      * Gets the schedule of this build.
      * 
      * @return the schedule of this build
      */
     public Schedule getSchedule() {
         return schedule;
     }
 
     /**
      * Sets the schedule of this build.
      * 
      * @param schedule the schedule of this build
      */
     public void setSchedule(Schedule schedule) {
         this.schedule = schedule;
     }
 
     /**
      * Gets the VCS list of this build.
      * 
      * @return the VCS list of this build
 	 * @see com.luntsys.luntbuild.vcs.Vcs
      */
     public List getVcsList() {
         if (vcsList == null)
             vcsList = new ArrayList();
         return vcsList;
     }
 
     /**
      * Sets the VCS list of this build.
      * 
      * @param vcsList the list of VCS adaptors
 	 * @see com.luntsys.luntbuild.vcs.Vcs
      */
     public void setVcsList(List vcsList) {
         this.vcsList = vcsList;
     }
 
     /**
      * Gets the change list for this build from the first Perforce adaptor.
      * 
      * @return the changelist for this build
      * @see com.luntsys.luntbuild.vcs.PerforceAdaptor#getChangelist()
      */
     public String getChangelist() {
     	String changelist = "0";
     	Iterator vcss = getVcsList().iterator();
     	while (vcss.hasNext()) {
     		Vcs vcs = (Vcs) vcss.next();
     		if (vcs.getClass().getName().equals("com.luntsys.luntbuild.vcs.PerforceAdaptor")) {
     			com.luntsys.luntbuild.vcs.PerforceAdaptor p4 = (com.luntsys.luntbuild.vcs.PerforceAdaptor) vcs;
     			changelist = p4.getChangelist();
     			break;
     		}
     	}
 
     	return changelist;
     }
 
     /**
      * Checks if this build is a clean build.
      * 
      * @return <code>true</code> if this is a clean build
      */
     public boolean isCleanBuild() {
         if (buildType == com.luntsys.luntbuild.facades.Constants.BUILD_TYPE_CLEAN)
             return true;
         else
             return false;
     }
 
 	/**
 	 * Gets a string representation of this object.
 	 * 
 	 * @return a string representation of this object
 	 */
     public String toString() {
         return getSchedule().getProject().getName() + "/" + getSchedule().getName() + "/" + getVersion();
     }
 
     /**
      * Gets the builder list of this build.
      * 
      * @return the builder list of this build
 	 * @see com.luntsys.luntbuild.builders.Builder
      */
     public List getBuilderList() {
         if (builderList == null)
             builderList = new ArrayList();
         return builderList;
     }
 
     /**
      * Sets the builder list of this build.
      * 
      * @param builderList the list of builders
 	 * @see com.luntsys.luntbuild.builders.Builder
      */
     public void setBuilderList(List builderList) {
         this.builderList = builderList;
     }
 
     /**
      * Gets the post-builder list of this build.
      * 
      * @return the post-builder list of this build
 	 * @see com.luntsys.luntbuild.builders.Builder
      */
     public List getPostbuilderList() {
         if (postbuilderList == null)
             postbuilderList = new ArrayList();
         return postbuilderList;
     }
 
     /**
      * Sets the post-builder list of this build.
      * 
      * @param postbuilderList the list of post-builders
 	 * @see com.luntsys.luntbuild.builders.Builder
      */
     public void setPostbuilderList(List postbuilderList) {
         this.postbuilderList = postbuilderList;
     }
 
     /**
      * Gets the system object. Mainly used for ognl evaluation.
      * 
      * @return the system object
      */
     public OgnlHelper getSystem() {
         return new OgnlHelper();
     }
 
     /**
      * Gets the logger for this build.
      * 
      * @return the logger for this build
      */
     public LuntbuildLogger getLogger() {
         LuntbuildLogger logger = (LuntbuildLogger)loggersById.get(new Long(this.id));
         return logger;
     }
 
     /**
      * Sets the logger for this build.
      * 
      * @param logger the logger to set
      */
     public void setLogger(LuntbuildLogger logger) {
         loggersById.put(new Long(this.id), logger);
     }
 
     /**
      * Removes the logger for this build.
      * 
      * @see LuntbuildLogger
      */
     public void removeLogger() {
     	LuntbuildLogger logger = (LuntbuildLogger)loggersById.remove(new Long(this.id));
     	if (logger != null) logger.close();
     }
 
 	/**
 	 * @return build user
 	 */
 	public String getUser() {
 		return user;
 	}
 
 	/**
 	 * @param buildUser build user
 	 */
 	public void setUser(String buildUser) {
 		this.user = buildUser;
 	}
 
     /**
      * Get the most recent previous build compared to this build.
      * 
      * @return the most recent previous build
      */
     public Build getPrevBuild() {
         return Luntbuild.getDao().loadPreviousBuild(this);
     }
 }
