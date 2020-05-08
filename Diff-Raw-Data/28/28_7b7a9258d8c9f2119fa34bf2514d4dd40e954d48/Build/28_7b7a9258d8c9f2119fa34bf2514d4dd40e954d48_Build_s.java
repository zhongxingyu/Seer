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
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.utility.LuntbuildLogger;
 import com.luntsys.luntbuild.utility.OgnlHelper;
 import com.luntsys.luntbuild.vcs.Vcs;
 import com.luntsys.luntbuild.web.Home;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * This class mapps to a hibernate entity. It represents a
  * execution result of a particular schedule
  *
  * @author robin shine
  */
 public class Build {
     private long id;
 
     /**
      * Status of current build,
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
 
     public boolean isHaveLabelOnHead() {
         return haveLabelOnHead;
     }
 
     public void setHaveLabelOnHead(boolean haveLabelOnHead) {
         this.haveLabelOnHead = haveLabelOnHead;
     }
 
     /**
      * Get identifier for this build
      * @return identifier for this build
      */
     public long getId() {
         return id;
     }
 
     /**
      * Is this a rebuilt build
      * @return Is this a rebuilt build
      */
     public boolean isRebuild() {
         return rebuild;
     }
 
     /**
      * Set whether this is a rebuilt build
      * @param rebuild
      */
     public void setRebuild(boolean rebuild) {
         this.rebuild = rebuild;
     }
 
     /**
      * Set identifier of this build
      * @param id
      */
     public void setId(long id) {
         this.id = id;
     }
 
     /**
      * Get version of this build
      * @return version of this build
      */
     public String getVersion() {
         return version;
     }
 
     /**
      * Get version of this build
      * @return version of this build
      */
     public String getVersionNoSpace() {
        return version.replaceAll("\\s", "_");
     }
 
     /**
      * Set version of this build
      * @param version
      */
     public void setVersion(String version) {
         this.version = version;
     }
 
     /**
      * Get status of this build.
      * @return one value of status of this build
      * {@link com.luntsys.luntbuild.facades.Constants#BUILD_STATUS_FAILED},
      * {@link com.luntsys.luntbuild.facades.Constants#BUILD_STATUS_RUNNING},
      * {@link com.luntsys.luntbuild.facades.Constants#BUILD_STATUS_SUCCESS}
      */
     public int getStatus() {
         return status;
     }
 
     /**
      * Set status of this build
      * @param status
      */
     public void setStatus(int status) {
         this.status = status;
     }
 
     /**
      * Get starting date of this build
      * @return starting date of this build
      */
     public Date getStartDate() {
         return startDate;
     }
 
     /**
      * Set start date of this build
      * @param startDate
      */
     public void setStartDate(Date startDate) {
         this.startDate = startDate;
     }
 
     /**
      * Get ending date of this build
      * @return ending date of this build
      */
     public Date getEndDate() {
         return endDate;
     }
 
     /**
      * Set ending date of this build
      * @param endDate
      */
     public void setEndDate(Date endDate) {
         this.endDate = endDate;
     }
 
     public boolean equals(Object obj) {
         if (obj != null && obj instanceof Build) {
             if (getId() == ((Build) obj).getId())
                 return true;
         }
         return false;
     }
 
     public int hashCode() {
         return (int) getId();
     }
 
     public void validate() {
         // current does nothing
     }
 
     /**
      * Return url of this build
      *
      * @return url of this build
      */
     public String getUrl() {
         return Luntbuild.getServletUrl() + "?service=external/Home&sp=l" +
                 Home.SERVICE_PARAMETER_BUILD + "&sp=l" + getId();
     }
 
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
      * Return the build log url of this build
      *
      * @return the build log url of this build
      */
     public String getBuildLogUrl() {
         String servletUrl = Luntbuild.getServletUrl();
         if (!servletUrl.endsWith("app.do"))
             throw new RuntimeException("Invalid servlet url: " + servletUrl);
         if (ensureBuildLog())
             return servletUrl.substring(0, servletUrl.length() - 6) + "publish/" +
                 getSchedule().getProject().getName() +
                 "/" + getSchedule().getName() + "/" + getVersionNoSpace() + "/" + BuildGenerator.BUILD_HTML_LOG;
         else
             return null;
     }
 
     /**
      * @return system log url
      */
     public String getSystemLogUrl() {
         String servletUrl = Luntbuild.getServletUrl();
         if (!servletUrl.endsWith("app.do"))
             throw new RuntimeException("Invalid servlet url: " + servletUrl);
         return servletUrl.substring(0, servletUrl.length() - 6) + "logs/" +
             Luntbuild.log4jFileName;
     }
 
     private boolean ensureRevisionLog() {
         String publishDir = getPublishDir();
         String revisionLogFile = publishDir + "/" + BuildGenerator.REVISION_HTML_LOG;
         File f = new File(revisionLogFile);
         return f.exists() && f.canRead();
     }
      /**
      * Return revision log url of this build
      *
      * @return revision log url of this build
      */
     public String getRevisionLogUrl() {
         String servletUrl = Luntbuild.getServletUrl();
         if (!servletUrl.endsWith("app.do"))
             throw new RuntimeException("Invalid servlet url: " + servletUrl);
         if (ensureRevisionLog())
             return servletUrl.substring(0, servletUrl.length() - 6) + "publish/" +
                 getSchedule().getProject().getName() + "/" + getSchedule().getName() + "/" +
                 getVersionNoSpace() + "/" + BuildGenerator.REVISION_HTML_LOG;
         else
             return null;
     }
 
     /**
      * Get facade of this build
      * @return facade of this build
      */
     public BuildFacade getFacade() {
         com.luntsys.luntbuild.facades.lb12.BuildFacade facade = new BuildFacade();
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
      * Set facade of this build
      * @param facade
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
                 com.luntsys.luntbuild.facades.lb12.VcsFacade vcsFacade = (com.luntsys.luntbuild.facades.lb12.VcsFacade) it.next();
                 Vcs vcs = (Vcs) Class.forName(vcsFacade.getVcsClassName()).newInstance();
                 vcs.setFacade(vcsFacade);
                 getVcsList().add(vcs);
             }
             getBuilderList().clear();
             it = facade.getBuilderList().iterator();
             while (it.hasNext()) {
                 BuilderFacade builderFacade = (com.luntsys.luntbuild.facades.lb12.BuilderFacade) it.next();
                 Builder builder = (Builder) Class.forName(builderFacade.getBuilderClassName()).newInstance();
                 builder.setFacade(builderFacade);
                 getBuilderList().add(builder);
             }
             getPostbuilderList().clear();
             it = facade.getPostbuilderList().iterator();
             while (it.hasNext()) {
                 BuilderFacade builderFacade = (com.luntsys.luntbuild.facades.lb12.BuilderFacade) it.next();
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
      * Get the publish directory for current build. Publish directory is used to hold
      * output of this build, including build log and build artifacts, etc.
      *
      * @return the publish directory for current build
      */
     public String getPublishDir() {
        String publishDir = getSchedule().getPublishDir() + File.separator + getVersionNoSpace();
         try {
             publishDir = new File(publishDir).getCanonicalPath();
             return publishDir.replaceAll("\\\\", "\\\\\\\\"); // in order to keep back slash for ognl expression evaluation
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Get artifacts directory where hold artifacts for this build
      * @return artifacts directory where hold artifacts for this build
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
      * Get the directory where to hold Junit html report stuff
      * @return the directory where to hold Junit html report stuff
      */
     public String getJunitHtmlReportDir() {
         try {
             String artifactsDir =  new File(getPublishDir() + File.separator +
                     Builder.JUNIT_HTML_REPORT_DIR).getCanonicalPath();
             return artifactsDir.replaceAll("\\\\", "\\\\\\\\"); // in order to keep back slash for ognl expression evaluation
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Get label strategy for this build
      * @return label strategy for this build
      */
     public int getLabelStrategy() {
         return labelStrategy;
     }
 
     /**
      * Set label strategy for this build
      * @param labelStrategy
      */
     public void setLabelStrategy(int labelStrategy) {
         this.labelStrategy = labelStrategy;
     }
 
     /**
      * Get post-build strategy for this build
      * @return post-build strategy for this build
      */
     public int getPostbuildStrategy() {
         return postbuildStrategy;
     }
 
     /**
      * Set post-build strategy for this build
      * @param postbuildStrategy
      */
     public void setPostbuildStrategy(int postbuildStrategy) {
         this.postbuildStrategy = postbuildStrategy;
     }
 
     /**
      * Get build type of this build
      * @return build type of this build
      */
     public int getBuildType() {
         return buildType;
     }
 
     /**
      * Set build type of this build
      * @param buildType
      */
     public void setBuildType(int buildType) {
         this.buildType = buildType;
     }
 
     /**
      * Get {@link Schedule} of this build
      * @return schedule of this build
      */
     public Schedule getSchedule() {
         return schedule;
     }
 
     /**
      * Set {@link Schedule} of this build
      * @param schedule
      */
     public void setSchedule(Schedule schedule) {
         this.schedule = schedule;
     }
 
     /**
      * Get VCS list of this build
      * @return VCS list of this build
      */
     public List getVcsList() {
         if (vcsList == null)
             vcsList = new ArrayList();
         return vcsList;
     }
 
     /**
      * Set VCS list of this build
      * @param vcsList
      */
     public void setVcsList(List vcsList) {
         this.vcsList = vcsList;
     }
 
     /**
      * Is this build a clean build?
      * @return Is this build a clean build?
      */
     public boolean isCleanBuild() {
         if (buildType == com.luntsys.luntbuild.facades.Constants.BUILD_TYPE_CLEAN)
             return true;
         else
             return false;
     }
 
     public String toString() {
        return getSchedule().getProject().getName() + "/" + getSchedule().getName() + "/" + getVersionNoSpace();
     }
 
     /**
      * Get builder list of this build
      * @return builder list of this build
      */
     public List getBuilderList() {
         if (builderList == null)
             builderList = new ArrayList();
         return builderList;
     }
 
     /**
      * Set builder list of this build
      * @param builderList
      */
     public void setBuilderList(List builderList) {
         this.builderList = builderList;
     }
 
     /**
      * Get post-builder list of this build
      * @return post-builder list of this build
      */
     public List getPostbuilderList() {
         if (postbuilderList == null)
             postbuilderList = new ArrayList();
         return postbuilderList;
     }
 
     /**
      * Set post-builder list of this build
      * @param postbuilderList
      */
     public void setPostbuilderList(List postbuilderList) {
         this.postbuilderList = postbuilderList;
     }
 
     /**
      * Get system object. Mainly used for ognl evaluation
      * @return system object
      */
     public OgnlHelper getSystem() {
         return new OgnlHelper();
     }
 
     /**
      * @return Returns the logger.
      */
     public LuntbuildLogger getLogger() {
         LuntbuildLogger logger = (LuntbuildLogger)loggersById.get(new Long(this.id));
         return logger;
     }
 
     /**
      * @param logger The logger to set.
      */
     public void setLogger(LuntbuildLogger logger) {
         loggersById.put(new Long(this.id), logger);
     }
 
     /**
      * Remove logger
      */
     public void removeLogger() {
         loggersById.remove(new Long(this.id));
     }
 }
