 /*
  * Copyright luntsys (c) 2001-2004,
  * Date: 2005-4-4
  * Time: 10:00:40
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
 
 package com.luntsys.luntbuild.migration;
 
 import com.luntsys.luntbuild.builders.AntBuilder;
 import com.luntsys.luntbuild.dao.Dao;
 import com.luntsys.luntbuild.db.*;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.thoughtworks.xstream.XStream;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xmlpull.mxp1.MXParser;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * Manager for importing and migrating data into Luntbuild.
  *
  * @author robin shine
  */
 public class MigrationManager {
 	private static Log logger = LogFactory.getLog(MigrationManager.class);
 
 	/**
 	 * Imports Luntbuild data from the specified XML file into a version 1.2 compatable facade collection object.
 	 * 
 	 * @param xmlDataFile the XML file to import
 	 * @return the facade collection object
 	 * @throws RuntimeException if an error occurs while accessing the data file
 	 * @throws MigrationException if the data file is invalid or unsupported
 	 */
 	public static com.luntsys.luntbuild.facades.lb12.DataCollection importAsDataCollection12(File xmlDataFile) {
 		FileReader fileReader = null;
 		try {
 			logger.info("Detect data version...");
 			fileReader = new FileReader(xmlDataFile);
 			XmlPullParser xpp = new MXParser();
 			xpp.setInput(fileReader);
 			xpp.nextTag();
 			if (!xpp.getName().equals("DataCollection"))
 				throw new MigrationException("Invalid data file: Root element should be <DataCollection>");
 			xpp.nextTag();
 			if (!xpp.getName().equals("version"))
 				throw new MigrationException("Invalid data file: First element of <DataCollection> should be <version>");
 			String version = xpp.nextText().trim();
 			XStream xstream = new XStream();
 			fileReader.close();
 			fileReader = null;
 			fileReader = new FileReader(xmlDataFile);
 			logger.info("Data version of importing file is: " + version);
 			if (version.equals("1.2")) {
 				xstream.alias("DataCollection", com.luntsys.luntbuild.facades.lb12.DataCollection.class);
 				return (com.luntsys.luntbuild.facades.lb12.DataCollection) xstream.fromXML(fileReader);
 			} else if (version.equals("1.1.1")) {
 				logger.info("Setup class aliases to read 1.1.1 data...");
 				setupAliasesFor111(xstream);
 				logger.info("Reading 1.1.1 data...");
 				com.luntsys.luntbuild.facades.lb111.DataCollection data111 =
 						(com.luntsys.luntbuild.facades.lb111.DataCollection) xstream.fromXML(fileReader);
 				logger.info("Migrating 1.1.1 data to 1.2 data...");
 				return migrateFrom111To12(data111);
 			}
 			throw new MigrationException("This version of data file is not supported!");
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		} catch (XmlPullParserException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (fileReader != null)
 				try {
 					fileReader.close();
 				} catch (IOException e) {
 					// ignore
 				}
 		}
 	}
 	/**
 	 * Imports Luntbuild project data from the specified XML file into a version 1.2 compatable project facade collection object.
 	 * 
 	 * @param xmlDataFile the XML file to import
 	 * @return the project facade collection object
 	 * @throws RuntimeException if an error occurs while accessing the data file
 	 * @throws MigrationException if the data file is invalid or unsupported
 	 */
 	public static com.luntsys.luntbuild.facades.lb12.ProjectCollection importAsProjectCollection12(File xmlDataFile) {
 		FileReader fileReader = null;
 		try {
 			logger.info("Detect data version...");
 			fileReader = new FileReader(xmlDataFile);
 			XmlPullParser xpp = new MXParser();
 			xpp.setInput(fileReader);
 			xpp.nextTag();
 			if (!xpp.getName().equals("ProjectCollection"))
 				throw new MigrationException("Invalid data file: Root element should be <ProjectCollection>");
 			xpp.nextTag();
 			if (!xpp.getName().equals("version"))
 				throw new MigrationException("Invalid data file: First element of <ProjectCollection> should be <version>");
 			String version = xpp.nextText().trim();
 			XStream xstream = new XStream();
 			fileReader.close();
 			fileReader = null;
 			fileReader = new FileReader(xmlDataFile);
 			logger.info("Data version of importing file is: " + version);
 			if (version.equals("1.2")) {
 				xstream.alias("ProjectCollection", com.luntsys.luntbuild.facades.lb12.ProjectCollection.class);
 				return (com.luntsys.luntbuild.facades.lb12.ProjectCollection) xstream.fromXML(fileReader);
 			}
 			throw new MigrationException("This version of data file is not supported!");
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		} catch (XmlPullParserException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (fileReader != null)
 				try {
 					fileReader.close();
 				} catch (IOException e) {
 					// ignore
 				}
 		}
 	}
 
 	/**
 	 * Migrates a version 1.1.1 compatable facade collection object to a version 1.2 compatable facade collection object.
 	 * 
 	 * @param data111 the version 1.1.1 facade collection object
 	 * @return the version 1.2 facade collection object
 	 */
 	private static com.luntsys.luntbuild.facades.lb12.DataCollection migrateFrom111To12(com.luntsys.luntbuild.facades.lb111.DataCollection data111) {
 		com.luntsys.luntbuild.facades.lb12.DataCollection data12 = new com.luntsys.luntbuild.facades.lb12.DataCollection();
 		// migrate properties
 		String publishDir = null;
 		logger.info("Migrating system settings...");
 		Iterator it = data111.getProperties().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb111.PropertyFacade propertyFacade111 =
 					(com.luntsys.luntbuild.facades.lb111.PropertyFacade) it.next();
 			if (propertyFacade111.getName().equals("antCommand") || propertyFacade111.getName().equals("envFile") ||
 					propertyFacade111.getName().equals("principalEmail") || propertyFacade111.getName().equals("smtpHost") ||
 					propertyFacade111.getName().equals("smtpPassword") || propertyFacade111.getName().equals("smtpUser")) {
 				continue;
 			} else if (propertyFacade111.getName().equals("publishDir")) {
 				publishDir = propertyFacade111.getValue();
 			}
 			com.luntsys.luntbuild.facades.lb12.PropertyFacade propertyFacade12 = new com.luntsys.luntbuild.facades.lb12.PropertyFacade();
 			if (propertyFacade111.getName().equals("com.luntsys.luntbuild.notifiers.EmailNotifier#Email address of sender"))
 				propertyFacade12.setName("com.luntsys.luntbuild.notifiers.EmailNotifier#Email address of the sender");
 			else if (propertyFacade111.getName().equals("com.luntsys.luntbuild.notifiers.MsnNotifier#luntbuild msn account"))
 				propertyFacade12.setName("com.luntsys.luntbuild.notifiers.MsnNotifier#Luntbuild MSN account");
 			else if (propertyFacade111.getName().equals("com.luntsys.luntbuild.notifiers.MsnNotifier#luntbuild msn password"))
 				propertyFacade12.setName("com.luntsys.luntbuild.notifiers.MsnNotifier#Luntbuild MSN password");
 			else
 				propertyFacade12.setName(propertyFacade111.getName());
 			propertyFacade12.setValue(propertyFacade111.getValue());
 			data12.getProperties().add(propertyFacade12);
 		}
 
 		// migrate users
 		logger.info("Migrating users...");
 		it = data111.getUsers().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb111.UserFacade userFacade111 =
 					(com.luntsys.luntbuild.facades.lb111.UserFacade) it.next();
 			com.luntsys.luntbuild.facades.lb12.UserFacade userFacade12 = new com.luntsys.luntbuild.facades.lb12.UserFacade();
 			logger.info("Migrating user: " + userFacade111.getName());
 			userFacade12.setCanCreateProject(false);
 			userFacade12.setFullname(userFacade111.getName());
 			userFacade12.setPassword(userFacade111.getName());
 			userFacade12.setId(userFacade111.getId());
 			if (userFacade111.getName().equals("<users who check in codes recently>"))
                 userFacade12.setName(User.CHECKIN_USER_NAME_RECENT);
 			else
 				userFacade12.setName(userFacade111.getName());
 			Iterator itContact = userFacade111.getContacts().keySet().iterator();
 			while (itContact.hasNext()) {
 				Object key = itContact.next();
 				logger.info("Migrating contact: " + (String)key);
 				Object value = userFacade111.getContacts().get(key);
 				if (key.equals("com.luntsys.luntbuild.notifiers.EmailNotifier#email"))
 					userFacade12.getContacts().put("com.luntsys.luntbuild.notifiers.EmailNotifier#Email", value);
 				else if (key.equals("com.luntsys.luntbuild.notifiers.MsnNotifier#msn account"))
 					userFacade12.getContacts().put("com.luntsys.luntbuild.notifiers.MsnNotifier#MSN account", value);
 				else
 					userFacade12.getContacts().put(key, value);
 			}
 			data12.getUsers().add(userFacade12);
 		}
 
 		// migrate projects
 		logger.info("Migrating projects...");
 		it = data111.getViews().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb111.ViewFacade viewFacade111 = (com.luntsys.luntbuild.facades.lb111.ViewFacade) it.next();
 			com.luntsys.luntbuild.facades.lb111.ProjectFacade projectFacade111 =
 					data111.getProject(viewFacade111.getProjectId());
 			com.luntsys.luntbuild.facades.lb12.ProjectFacade projectFacade12 =
 					new com.luntsys.luntbuild.facades.lb12.ProjectFacade();
 			logger.info("Migrating view: " + projectFacade111.getName() + "/" + viewFacade111.getName());
 			// migrate general information
 			projectFacade12.setId(viewFacade111.getId());
 			projectFacade12.setName(createProjectNameFor12(data12, projectFacade111.getName(), viewFacade111.getName()));
 			logger.info("New project name: " + projectFacade12.getName());
 			projectFacade12.setDescription(projectFacade111.getDescription() + "\n" + viewFacade111.getDescription());
 			projectFacade12.setLogLevel(Constants.LOG_LEVEL_NORMAL);
 
 			// migrate vcs information
 			if (projectFacade111.getVcs() instanceof com.luntsys.luntbuild.facades.lb111.CompositeVcsAdaptorFacade) {
 				Iterator itViewComposition = data111.getViewCompositionConfig().iterator();
 				while (itViewComposition.hasNext()) {
 					com.luntsys.luntbuild.facades.lb111.ViewCompositionConfigFacade viewCompositionConfigFacade111
 							= (com.luntsys.luntbuild.facades.lb111.ViewCompositionConfigFacade) itViewComposition.next();
 					if (viewCompositionConfigFacade111.getCompositeViewId() == viewFacade111.getId()) {
 						com.luntsys.luntbuild.facades.lb111.ViewFacade basicViewFacade111 =
 								data111.getView(viewCompositionConfigFacade111.getBasicViewId());
 						com.luntsys.luntbuild.facades.lb111.ProjectFacade basicProjectFacade111 =
 								data111.getProject(basicViewFacade111.getProjectId());
 						logger.info("Migrating VCS information from basic view: " + basicProjectFacade111.getName()
 								+ "/" + basicViewFacade111.getName());
 						projectFacade12.getVcsList().add(migrateVcsFrom111To12(basicProjectFacade111.getVcs(),
 								basicViewFacade111.getVcs()));
 					}
 				}
 			} else {
 				logger.info("Migrating VCS information...");
 				projectFacade12.getVcsList().add(migrateVcsFrom111To12(projectFacade111.getVcs(),
 						viewFacade111.getVcs()));
 			}
 
 			// migrate builder information
 			logger.info("Migrating builder information...");
 			AntBuilder antBuilder = new AntBuilder();
 			antBuilder.setName("builder");
 			logger.info("Creating builder with name: " + antBuilder.getName());
 			antBuilder.setBuildScriptPath(viewFacade111.getBuildFilePath());
 			antBuilder.setTargets(viewFacade111.getBuildTarget());
 			projectFacade12.getBuilderList().add(antBuilder.getFacade());
 
 			if (!Luntbuild.isEmpty(viewFacade111.getPostbuildFilePath())) {
 				antBuilder.setName("post-builder");
 				logger.info("Creating builder with name: " + antBuilder.getName());
 				antBuilder.setBuildScriptPath(viewFacade111.getPostbuildFilePath());
 				antBuilder.setTargets(viewFacade111.getBuildTarget());
 				projectFacade12.getBuilderList().add(antBuilder.getFacade());
 			}
 			data12.getProjects().add(projectFacade12);
 		}
 
 		// migrate schedules
 		logger.info("Migrating schedules...");
 		it = data111.getBuildSchedules().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb111.BuildScheduleFacade buildScheduleFacade111 =
 					(com.luntsys.luntbuild.facades.lb111.BuildScheduleFacade) it.next();
 			com.luntsys.luntbuild.facades.lb111.ScheduleFacade scheduleFacade111 =
 					data111.getSchedule(buildScheduleFacade111.getScheduleId());
 			com.luntsys.luntbuild.facades.lb111.ViewFacade viewFacade111 =
 					data111.getView(buildScheduleFacade111.getViewId());
 			com.luntsys.luntbuild.facades.lb12.ProjectFacade projectFacade12 =
 					data12.getProject(buildScheduleFacade111.getViewId());
 			com.luntsys.luntbuild.facades.lb12.ScheduleFacade scheduleFacade12 =
 					new com.luntsys.luntbuild.facades.lb12.ScheduleFacade();
 			logger.info("Migrating build schedule: " + projectFacade12.getName() + "/" +
 					scheduleFacade111.getName());
 			scheduleFacade12.setId(buildScheduleFacade111.getId());
 			scheduleFacade12.setName(createScheduleNameFor12(data12, projectFacade12.getId(),
 					scheduleFacade111.getName()));
 			logger.info("Creating new schedule: " + projectFacade12.getName() + "/" +
 					scheduleFacade12.getName());
 			scheduleFacade12.setScheduleDisabled(false);
 			scheduleFacade12.setProjectId(projectFacade12.getId());
 			scheduleFacade12.setStatus(buildScheduleFacade111.getStatus());
 			scheduleFacade12.setStatusDate(buildScheduleFacade111.getStatusDate());
 			scheduleFacade12.setTriggerDependencyStrategy(Constants.TRIGGER_SCHEDULES_THIS_DEPENDS_ON);
 			scheduleFacade12.setCronExpression(scheduleFacade111.getCronExpression());
 			scheduleFacade12.setRepeatInterval(scheduleFacade111.getRepeatInterval());
 			scheduleFacade12.setTriggerType(scheduleFacade111.getTriggerType());
 			scheduleFacade12.setDescription(scheduleFacade111.getDescription());
 
 			scheduleFacade12.getAssociatedBuilderNames().add("builder");
 			if (projectFacade12.getBuilderList().size() == 2)
 				scheduleFacade12.getAssociatedPostbuilderNames().add("post-builder");
 
 			scheduleFacade12.setBuildCleanupStrategy(Constants.BUILD_CLEANUP_NONE);
 			scheduleFacade12.setBuildCleanupStrategyData(null);
 
 			if (buildScheduleFacade111.isCleanBuild())
 				scheduleFacade12.setBuildType(Constants.BUILD_TYPE_CLEAN);
 			else
 				scheduleFacade12.setBuildType(Constants.BUILD_TYPE_INCREMENT);
 
 			if (buildScheduleFacade111.getLabelStrategy() == null)
 				scheduleFacade12.setLabelStrategy(Constants.LABEL_IF_SUCCESS);
 			else if (buildScheduleFacade111.getLabelStrategy().equals("do not label"))
 				scheduleFacade12.setLabelStrategy(Constants.LABEL_NONE);
 			else if (buildScheduleFacade111.getLabelStrategy().equals("label always"))
 				scheduleFacade12.setLabelStrategy(Constants.LABEL_ALWAYS);
 			else
 				scheduleFacade12.setLabelStrategy(Constants.LABEL_IF_SUCCESS);
 
 			scheduleFacade12.setNextVersion(viewFacade111.getNextVersion());
 			if (scheduleFacade12.getNextVersion().startsWith("\"") && scheduleFacade12.getNextVersion().endsWith("\"")) {
 				scheduleFacade12.setNextVersion(scheduleFacade12.getNextVersion().substring(1, scheduleFacade12.getNextVersion().length() - 1));
 			}
 
 			if (buildScheduleFacade111.getNotifyStrategy() == null)
 				scheduleFacade12.setNotifyStrategy(Constants.NOTIFY_WHEN_STATUS_CHANGED);
 			else if (buildScheduleFacade111.getNotifyStrategy().equals("notify when build success"))
 				scheduleFacade12.setNotifyStrategy(Constants.NOTIFY_IF_SUCCESS);
 			else if (buildScheduleFacade111.getNotifyStrategy().equals("notify when build failed"))
 				scheduleFacade12.setNotifyStrategy(Constants.NOTIFY_IF_FAILED);
 			else if (buildScheduleFacade111.getNotifyStrategy().equals("do not notify"))
 				scheduleFacade12.setNotifyStrategy(Constants.NOTIFY_NONE);
 			else if (buildScheduleFacade111.getNotifyStrategy().equals("notify always"))
 				scheduleFacade12.setNotifyStrategy(Constants.NOTIFY_ALWAYS);
 			else
 				scheduleFacade12.setNotifyStrategy(Constants.NOTIFY_WHEN_STATUS_CHANGED);
 
 			if (buildScheduleFacade111.getPostbuildStrategy() == null)
 				scheduleFacade12.setPostbuildStrategy(Constants.POSTBUILD_NONE);
 			else if (buildScheduleFacade111.getPostbuildStrategy().equals("post-build when success"))
 				scheduleFacade12.setPostbuildStrategy(Constants.POSTBUILD_IF_SUCCESS);
 			else if (buildScheduleFacade111.getPostbuildStrategy().equals("post-build when failed"))
 				scheduleFacade12.setPostbuildStrategy(Constants.POSTBUILD_IF_FAILED);
 			else if (buildScheduleFacade111.getPostbuildStrategy().equals("post-build always"))
 				scheduleFacade12.setPostbuildStrategy(Constants.POSTBUILD_NONE);
 
 			if (buildScheduleFacade111.getBuildStrategy() == null) {
 				scheduleFacade12.setBuildNecessaryCondition("vcsModified or dependencyNewer");
 			} else if (buildScheduleFacade111.getBuildStrategy().equals("build always if failed"))
 				scheduleFacade12.setBuildNecessaryCondition("lastBuild==null or (lastBuild.status==1?(vcsModified or dependencyNewer):true)");
 			else if (buildScheduleFacade111.getBuildStrategy().equals("build always"))
 				scheduleFacade12.setBuildNecessaryCondition("true");
 			else if (buildScheduleFacade111.getBuildStrategy().equals("do not build"))
 				scheduleFacade12.setBuildNecessaryCondition("false");
 			else {
 				scheduleFacade12.setBuildNecessaryCondition("vcsModified or dependencyNewer");
 			}
 
 			data12.getSchedules().add(scheduleFacade12);
 		}
 
 		// migrate builds
 		logger.info("Migrating builds...");
 		it = data111.getBuilds().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb111.BuildFacade buildFacade111 = (com.luntsys.luntbuild.facades.lb111.BuildFacade) it.next();
 			com.luntsys.luntbuild.facades.lb12.BuildFacade buildFacade12 = new com.luntsys.luntbuild.facades.lb12.BuildFacade();
 			com.luntsys.luntbuild.facades.lb12.ScheduleFacade scheduleFacade12 = data12.getSchedule(buildFacade111.getBuildScheduleId());
 			com.luntsys.luntbuild.facades.lb12.ProjectFacade projectFacade12 = data12.getProject(scheduleFacade12.getProjectId());
 			logger.info("Migrating build: " + projectFacade12.getName() + "/" + scheduleFacade12.getName() + "/" +
 					buildFacade111.getVersion());
 			buildFacade12.getBuilderList().add(projectFacade12.getBuilderList().get(0));
 			if (projectFacade12.getBuilderList().size() == 2)
 				buildFacade12.getPostbuilderList().add(projectFacade12.getBuilderList().get(1));
 
 			buildFacade12.setBuildType(scheduleFacade12.getBuildType());
 			buildFacade12.setEndDate(buildFacade111.getEndDate());
 			buildFacade12.setHaveLabelOnHead(buildFacade111.isHaveLabelOnHead());
 			buildFacade12.setId(buildFacade111.getId());
 			buildFacade12.setLabelStrategy(scheduleFacade12.getLabelStrategy());
 			buildFacade12.setPostbuildStrategy(scheduleFacade12.getPostbuildStrategy());
 			buildFacade12.setRebuild(buildFacade111.isRebuild());
 			buildFacade12.setScheduleId(scheduleFacade12.getId());
 			buildFacade12.setStartDate(buildFacade111.getStartDate());
 			buildFacade12.setStatus(buildFacade111.getStatus());
 			buildFacade12.setVcsList(projectFacade12.getVcsList());
 			buildFacade12.setVersion(correctBuildVersionFor12(buildFacade111.getVersion()));
 			logger.info("Correct build version as: " + buildFacade12.getVersion());
 
 			data12.getBuilds().add(buildFacade12);
 		}
 
 		// create necessary roles
 		logger.info("Creating necessary roles...");
 		com.luntsys.luntbuild.facades.lb12.RoleFacade roleFacade = new com.luntsys.luntbuild.facades.lb12.RoleFacade();
 		roleFacade.setId(1);
 		roleFacade.setName(Role.ROLE_AUTHENTICATED);
 		data12.getRoles().add(roleFacade);
 		roleFacade = new com.luntsys.luntbuild.facades.lb12.RoleFacade();
 		roleFacade.setId(2);
 		roleFacade.setName(Role.ROLE_SITE_ADMIN);
 		data12.getRoles().add(roleFacade);
 		roleFacade = new com.luntsys.luntbuild.facades.lb12.RoleFacade();
 		roleFacade.setId(3);
 		roleFacade.setName(Role.LUNTBUILD_PRJ_ADMIN);
 		data12.getRoles().add(roleFacade);
 		roleFacade = new com.luntsys.luntbuild.facades.lb12.RoleFacade();
 		roleFacade.setId(4);
 		roleFacade.setName(Role.LUNTBUILD_PRJ_BUILDER);
 		data12.getRoles().add(roleFacade);
 		roleFacade = new com.luntsys.luntbuild.facades.lb12.RoleFacade();
 		roleFacade.setId(5);
 		roleFacade.setName(Role.LUNTBUILD_PRJ_VIEWER);
 		data12.getRoles().add(roleFacade);
 
 		// migrate vcs login mappings
 		logger.info("Migrating VCS login mapping...");
 		it = data111.getVcsLoginConfig().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb111.VcsLoginFacade vcsLoginFacade111 =
 					(com.luntsys.luntbuild.facades.lb111.VcsLoginFacade) it.next();
 			Iterator itView = data111.getViews().iterator();
 			while (itView.hasNext()) {
 				com.luntsys.luntbuild.facades.lb111.ViewFacade viewFacade111 =
 						(com.luntsys.luntbuild.facades.lb111.ViewFacade) itView.next();
 				if (viewFacade111.getProjectId() != vcsLoginFacade111.getProjectId())
 					continue;
 				com.luntsys.luntbuild.facades.lb12.VcsLoginFacade vcsLoginFacade12 =
 						new com.luntsys.luntbuild.facades.lb12.VcsLoginFacade();
 				vcsLoginFacade12.setId(0);
 				vcsLoginFacade12.setLogin(vcsLoginFacade111.getLogin());
 				vcsLoginFacade12.setUserId(vcsLoginFacade111.getUserId());
 				vcsLoginFacade12.setProjectId(viewFacade111.getId());
 				data12.getVcsLoginMapping().add(vcsLoginFacade12);
 			}
 		}
 
 		// migrate notification config
 		logger.info("Migrating notification mapping...");
 		it = data111.getNotificationConfig().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb111.NotificationConfigFacade notificationConfigFacade111 =
 					(com.luntsys.luntbuild.facades.lb111.NotificationConfigFacade) it.next();
 			com.luntsys.luntbuild.facades.lb12.NotifyMappingFacade notifyMappingFacade12 =
 					new com.luntsys.luntbuild.facades.lb12.NotifyMappingFacade();
 			notifyMappingFacade12.setId(0);
 			notifyMappingFacade12.setUserId(notificationConfigFacade111.getUserId());
 			notifyMappingFacade12.setProjectId(notificationConfigFacade111.getViewId());
 			data12.getNotifyMapping().add(notifyMappingFacade12);
 		}
 
 		// make all users be able to view all projects
 		logger.info("Setup roles mapping so that all users can view all projects initially...");
 		it = data12.getProjects().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.ProjectFacade projectFacade12 =
 					(com.luntsys.luntbuild.facades.lb12.ProjectFacade) it.next();
 			Iterator itUser = data12.getUsers().iterator();
 			while (itUser.hasNext()) {
 				com.luntsys.luntbuild.facades.lb12.UserFacade userFacade12 =
 						(com.luntsys.luntbuild.facades.lb12.UserFacade) itUser.next();
                 if (userFacade12.getName().equals(User.CHECKIN_USER_NAME_RECENT))
 					continue;
 				com.luntsys.luntbuild.facades.lb12.RolesMappingFacade rolesMappingFacade12 =
 						new com.luntsys.luntbuild.facades.lb12.RolesMappingFacade();
 				rolesMappingFacade12.setId(0);
 				rolesMappingFacade12.setProjectId(projectFacade12.getId());
 				rolesMappingFacade12.setUserId(userFacade12.getId());
 				rolesMappingFacade12.setRoleId(5);
 				data12.getRolesMapping().add(rolesMappingFacade12);
 			}
 		}
 
 		// migrate build artifacts
 		if (Luntbuild.isEmpty(publishDir))
 			publishDir = Luntbuild.installDir + File.separator + "publish";
 		logger.info("Migrating build artifacts published in: " + publishDir);
 		it = data12.getBuilds().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.BuildFacade buildFacade12 =
 					(com.luntsys.luntbuild.facades.lb12.BuildFacade) it.next();
 			com.luntsys.luntbuild.facades.lb12.ScheduleFacade scheduleFacade12 =
 					data12.getSchedule(buildFacade12.getScheduleId());
 			com.luntsys.luntbuild.facades.lb12.ProjectFacade projectFacade12 =
 					data12.getProject(scheduleFacade12.getProjectId());
 			logger.info("Migrating build artifacts for build: " + projectFacade12.getName() + "/" +
 					scheduleFacade12.getName() + "/" + buildFacade12.getVersion());
 			String buildPublishDir12 = publishDir + File.separator + projectFacade12.getName() +
 					File.separator + scheduleFacade12.getName() + File.separator +
 					buildFacade12.getVersion();
 			String buildPublishDir111 = publishDir + File.separator + buildFacade12.getId();
 			if (new File(buildPublishDir111).exists()) {
 				logger.info("Original directory: " + buildPublishDir111);
 				logger.info("New directory: " + buildPublishDir12);
 				Luntbuild.renameDir(buildPublishDir111, buildPublishDir12);
 			}
 		}
 		return data12;
 	}
 
 	/**
 	 * Migrates version 1.1.1 compatable VCS adaptors to version 1.2 compatable VCS adaptors.
 	 * <p>This process involves combining the VCS adaptor from the project and view level.</p>
 	 * 
 	 * @param vcs111AtProjectLevel the version 1.1.1 VCS adaptor from the project level
 	 * @param vcs111AtViewLevel the version 1.1.1 VCS adaptor from the view level
 	 * @return the version 1.2 VCS adaptor
 	 * @throws RuntimeException if the VCS adaptor is not supported
 	 */
 	private static com.luntsys.luntbuild.facades.lb12.VcsFacade migrateVcsFrom111To12(com.luntsys.luntbuild.facades.lb111.VcsFacade vcs111AtProjectLevel,
 																					  com.luntsys.luntbuild.facades.lb111.VcsFacade vcs111AtViewLevel) {
 		if (vcs111AtProjectLevel instanceof com.luntsys.luntbuild.facades.lb111.BaseClearcaseAdaptorFacade) {
 			com.luntsys.luntbuild.facades.lb111.BaseClearcaseAdaptorFacade projectVcs =
 					(com.luntsys.luntbuild.facades.lb111.BaseClearcaseAdaptorFacade) vcs111AtProjectLevel;
 			com.luntsys.luntbuild.facades.lb111.BaseClearcaseAdaptorFacade viewVcs =
 					(com.luntsys.luntbuild.facades.lb111.BaseClearcaseAdaptorFacade) vcs111AtViewLevel;
 			com.luntsys.luntbuild.facades.lb12.BaseClearcaseAdaptorFacade vcs12 =
 					new com.luntsys.luntbuild.facades.lb12.BaseClearcaseAdaptorFacade();
 			vcs12.setViewStgLoc(projectVcs.getViewStgLoc());
 			vcs12.setMkviewExtraOpts(viewVcs.getMkviewExtraOpts());
 			vcs12.setModificationDetectionConfig(viewVcs.getModificationDetectionConfig());
 			vcs12.setViewCfgSpec(viewVcs.getViewCfgSpec());
 			vcs12.setVws(viewVcs.getVws());
 			return vcs12;
 		} else if (vcs111AtProjectLevel instanceof com.luntsys.luntbuild.facades.lb111.CvsAdaptorFacade) {
 			com.luntsys.luntbuild.facades.lb111.CvsAdaptorFacade projectVcs =
 					(com.luntsys.luntbuild.facades.lb111.CvsAdaptorFacade) vcs111AtProjectLevel;
 			com.luntsys.luntbuild.facades.lb111.CvsAdaptorFacade viewVcs =
 					(com.luntsys.luntbuild.facades.lb111.CvsAdaptorFacade) vcs111AtViewLevel;
 			com.luntsys.luntbuild.facades.lb12.CvsAdaptorFacade vcs12 =
 					new com.luntsys.luntbuild.facades.lb12.CvsAdaptorFacade();
 			vcs12.setCvsRoot(projectVcs.getCvsRoot());
 			vcs12.setCvsPassword(projectVcs.getCvsPassword());
 			vcs12.setCygwinCvs(projectVcs.getCygwinCvs());
 			vcs12.setDisableHistoryCmd(projectVcs.getDisableHistoryCmd());
 			vcs12.setDisableSuppressOption(projectVcs.getDisableSuppressOption());
 			vcs12.setModules(viewVcs.getModules());
 			return vcs12;
 		} else if (vcs111AtProjectLevel instanceof com.luntsys.luntbuild.facades.lb111.FileSystemAdaptorFacade) {
 			com.luntsys.luntbuild.facades.lb111.FileSystemAdaptorFacade projectVcs =
 					(com.luntsys.luntbuild.facades.lb111.FileSystemAdaptorFacade) vcs111AtProjectLevel;
 			com.luntsys.luntbuild.facades.lb12.FileSystemAdaptorFacade vcs12 =
 					new com.luntsys.luntbuild.facades.lb12.FileSystemAdaptorFacade();
 			vcs12.setSourceDir(projectVcs.getSourceDir());
 			return vcs12;
 		} else if (vcs111AtProjectLevel instanceof com.luntsys.luntbuild.facades.lb111.PerforceAdaptorFacade) {
 			com.luntsys.luntbuild.facades.lb111.PerforceAdaptorFacade projectVcs =
 					(com.luntsys.luntbuild.facades.lb111.PerforceAdaptorFacade) vcs111AtProjectLevel;
 			com.luntsys.luntbuild.facades.lb111.PerforceAdaptorFacade viewVcs =
 					(com.luntsys.luntbuild.facades.lb111.PerforceAdaptorFacade) vcs111AtViewLevel;
 			com.luntsys.luntbuild.facades.lb12.PerforceAdaptorFacade vcs12 =
 					new com.luntsys.luntbuild.facades.lb12.PerforceAdaptorFacade();
 			vcs12.setPassword(projectVcs.getPassword());
 			vcs12.setPort(projectVcs.getPort());
 			vcs12.setUser(projectVcs.getPort());
 			vcs12.setModules(viewVcs.getModules());
 			return vcs12;
 		} else if (vcs111AtProjectLevel instanceof com.luntsys.luntbuild.facades.lb111.StarteamAdaptorFacade) {
 			com.luntsys.luntbuild.facades.lb111.StarteamAdaptorFacade projectVcs =
 					(com.luntsys.luntbuild.facades.lb111.StarteamAdaptorFacade) vcs111AtProjectLevel;
 			com.luntsys.luntbuild.facades.lb111.StarteamAdaptorFacade viewVcs =
 					(com.luntsys.luntbuild.facades.lb111.StarteamAdaptorFacade) vcs111AtViewLevel;
 			com.luntsys.luntbuild.facades.lb12.StarteamAdaptorFacade vcs12 =
 					new com.luntsys.luntbuild.facades.lb12.StarteamAdaptorFacade();
 			vcs12.setConvertEOL(projectVcs.getConvertEOL());
 			vcs12.setPassword(projectVcs.getPassword());
 			vcs12.setProjectLocation(projectVcs.getProjectLocation());
 			vcs12.setUser(projectVcs.getUser());
 			vcs12.setModules(viewVcs.getModules());
 			return vcs12;
 		} else if (vcs111AtProjectLevel instanceof com.luntsys.luntbuild.facades.lb111.SvnAdaptorFacade) {
 			com.luntsys.luntbuild.facades.lb111.SvnAdaptorFacade projectVcs =
 					(com.luntsys.luntbuild.facades.lb111.SvnAdaptorFacade) vcs111AtProjectLevel;
 			com.luntsys.luntbuild.facades.lb111.SvnAdaptorFacade viewVcs =
 					(com.luntsys.luntbuild.facades.lb111.SvnAdaptorFacade) vcs111AtViewLevel;
 			com.luntsys.luntbuild.facades.lb12.SvnAdaptorFacade vcs12 =
 					new com.luntsys.luntbuild.facades.lb12.SvnAdaptorFacade();
 			vcs12.setBranches(projectVcs.getBranches());
 			vcs12.setPassword(projectVcs.getPassword());
 			vcs12.setTags(projectVcs.getTags());
 			vcs12.setTrunk(projectVcs.getTrunk());
 			vcs12.setUrlBase(projectVcs.getUrlBase());
 			vcs12.setUser(projectVcs.getUser());
 			vcs12.setModules(viewVcs.getModules());
 			return vcs12;
 		} else if (vcs111AtProjectLevel instanceof com.luntsys.luntbuild.facades.lb111.UCMClearcaseAdaptorFacade) {
 			com.luntsys.luntbuild.facades.lb111.UCMClearcaseAdaptorFacade projectVcs =
 					(com.luntsys.luntbuild.facades.lb111.UCMClearcaseAdaptorFacade) vcs111AtProjectLevel;
 			com.luntsys.luntbuild.facades.lb111.UCMClearcaseAdaptorFacade viewVcs =
 					(com.luntsys.luntbuild.facades.lb111.UCMClearcaseAdaptorFacade) vcs111AtViewLevel;
 			com.luntsys.luntbuild.facades.lb12.UCMClearcaseAdaptorFacade vcs12 =
 					new com.luntsys.luntbuild.facades.lb12.UCMClearcaseAdaptorFacade();
 			vcs12.setProjectVob(projectVcs.getProjectVob());
 			vcs12.setViewStgLoc(projectVcs.getViewStgLoc());
 			vcs12.setMkviewExtraOpts(viewVcs.getMkviewExtraOpts());
 			vcs12.setModificationDetectionConfig(viewVcs.getModificationDetectionConfig());
 			vcs12.setStream(viewVcs.getStream());
 			vcs12.setVws(viewVcs.getVws());
 			vcs12.setWhatToBuild(viewVcs.getWhatToBuild());
 			return vcs12;
 		} else if (vcs111AtProjectLevel instanceof com.luntsys.luntbuild.facades.lb111.VssAdaptorFacade) {
 			com.luntsys.luntbuild.facades.lb111.VssAdaptorFacade projectVcs =
 					(com.luntsys.luntbuild.facades.lb111.VssAdaptorFacade) vcs111AtProjectLevel;
 			com.luntsys.luntbuild.facades.lb111.VssAdaptorFacade viewVcs =
 					(com.luntsys.luntbuild.facades.lb111.VssAdaptorFacade) vcs111AtViewLevel;
 			com.luntsys.luntbuild.facades.lb12.VssAdaptorFacade vcs12 =
 					new com.luntsys.luntbuild.facades.lb12.VssAdaptorFacade();
 			vcs12.setDateTimeFormat(projectVcs.getDateTimeFormat());
 			vcs12.setSsDir(projectVcs.getSsDir());
 			vcs12.setVssPassword(projectVcs.getVssPassword());
 			vcs12.setVssPath(projectVcs.getVssPath());
 			vcs12.setVssUser(projectVcs.getVssUser());
 			vcs12.setModules(viewVcs.getModules());
 			return vcs12;
 		}
 		throw new RuntimeException("Un-expected VCS class: " + vcs111AtProjectLevel.getClass().getName());
 	}
 
 	/**
 	 * Removes illegal characters from build version for version 1.2 compatable Luntbuild.
 	 * 
 	 * @param buildVersion the build version string
 	 * @return the cleaned build version string
 	 */
 	private static String correctBuildVersionFor12(String buildVersion) {
 		return buildVersion.replaceAll("[/\\\\:*?\"<>|]", "-");
 	}
 
 	/**
 	 * Creates a version 1.2 compatable project name from a version 1.1.1 compatable project.
 	 * <p>This process involves combining the project and view name.  A version 1.2 compatable facade
 	 * collection object is needed to make sure duplicate project names are not generated.</p>
 	 * 
 	 * @param data12 a version 1.2 compatable facade collection object
 	 * @param projectNameOf111 the version 1.1.1 project name
 	 * @param viewNameOf111 the version 1.1.1 view name
 	 * @return the new project name
 	 */
 	private static String createProjectNameFor12(com.luntsys.luntbuild.facades.lb12.DataCollection data12,
 												 String projectNameOf111, String viewNameOf111) {
 		String temp = projectNameOf111 + "-" + viewNameOf111;
 		if (Luntbuild.isEmpty(temp))
 			temp = "noname";
 		temp = temp.replaceAll("[/\\\\:*?\"<>|]", "-");
 		int repeatCount = 2;
 		String projectName = temp;
 		while (data12.getProject(projectName) != null) {
 			projectName = temp + (repeatCount++);
 		}
 		return projectName;
 	}
 
 	/**
 	 * Creates a version 1.2 compatable schedule name from a version 1.1.1 compatable schedule.
 	 * <p>A version 1.2 compatable facade collection object is needed to make sure duplicate schedule names are not generated.</p>
 	 * 
 	 * @param data12 a version 1.2 compatable facade collection object
 	 * @param projectIdOf12 the version 1.2 project name
 	 * @param scheduleNameOf111 the version 1.1.1 schedule name
 	 * @return the new schedule name
 	 */
 	private static String createScheduleNameFor12(com.luntsys.luntbuild.facades.lb12.DataCollection data12,
 												  long projectIdOf12, String scheduleNameOf111) {
 		String temp = scheduleNameOf111;
 		if (Luntbuild.isEmpty(temp))
 			temp = "noname";
 		temp = temp.replaceAll("[/\\\\:*?\"<>|]", "-");
 		int repeatCount = 2;
 		String scheduleName = temp;
 		while (data12.getSchedule(projectIdOf12, scheduleNameOf111) != null) {
 			scheduleName = temp + (repeatCount++);
 		}
 		return scheduleName;
 	}
 
 	/**
 	 * Sets up mappings from XML elements to version 1.1.1 compatable facade classes.
 	 * 
 	 * @param xstream the XML parser
 	 */
 	private static void setupAliasesFor111(XStream xstream) {
 		xstream.alias("com.luntsys.luntbuild.remoting.BaseClearcaseAdaptorFacade", com.luntsys.luntbuild.facades.lb111.BaseClearcaseAdaptorFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.BuildFacade", com.luntsys.luntbuild.facades.lb111.BuildFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.BuildScheduleFacade", com.luntsys.luntbuild.facades.lb111.BuildScheduleFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.CommonModuleFacade", com.luntsys.luntbuild.facades.lb111.CommonModuleFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.CompositeVcsAdaptorFacade", com.luntsys.luntbuild.facades.lb111.CompositeVcsAdaptorFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.CvsAdaptorFacade", com.luntsys.luntbuild.facades.lb111.CvsAdaptorFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.CvsModuleFacade", com.luntsys.luntbuild.facades.lb12.CvsModuleFacade.class);
 		xstream.alias("DataCollection", com.luntsys.luntbuild.facades.lb111.DataCollection.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.VssModuleFacade", com.luntsys.luntbuild.facades.lb12.VssModuleFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.VssAdaptorFacade", com.luntsys.luntbuild.facades.lb111.VssAdaptorFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.ViewFacade", com.luntsys.luntbuild.facades.lb111.ViewFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.ViewCompositionConfigFacade", com.luntsys.luntbuild.facades.lb111.ViewCompositionConfigFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.VcsLoginFacade", com.luntsys.luntbuild.facades.lb111.VcsLoginFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.VcsFacade", com.luntsys.luntbuild.facades.lb111.VcsFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.UserFacade", com.luntsys.luntbuild.facades.lb111.UserFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.UCMClearcaseAdaptorFacade", com.luntsys.luntbuild.facades.lb111.UCMClearcaseAdaptorFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.SvnModuleFacade", com.luntsys.luntbuild.facades.lb12.SvnModuleFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.SvnAdaptorFacade", com.luntsys.luntbuild.facades.lb111.SvnAdaptorFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.StarteamModuleFacade", com.luntsys.luntbuild.facades.lb12.StarteamModuleFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.StarteamAdaptorFacade", com.luntsys.luntbuild.facades.lb111.StarteamAdaptorFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.ScheduleFacade", com.luntsys.luntbuild.facades.lb111.ScheduleFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.PropertyFacade", com.luntsys.luntbuild.facades.lb111.PropertyFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.ProjectFacade", com.luntsys.luntbuild.facades.lb111.ProjectFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.PerforceModuleFacade", com.luntsys.luntbuild.facades.lb12.PerforceModuleFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.PerforceAdaptorFacade", com.luntsys.luntbuild.facades.lb111.PerforceAdaptorFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.NotificationConfigFacade", com.luntsys.luntbuild.facades.lb111.NotificationConfigFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.ModuleFacade", com.luntsys.luntbuild.facades.lb111.ModuleFacade.class);
 		xstream.alias("com.luntsys.luntbuild.remoting.FileSystemAdaptorFacade", com.luntsys.luntbuild.facades.lb111.FileSystemAdaptorFacade.class);
 	}
 
 	/**
 	 * Converts a version 1.2 compatable facade collection object into a version 1.2 compatable data collection object.
 	 * 
 	 * @param data12 the facade collection object
 	 * @return the data collection object
 	 */
 	public static com.luntsys.luntbuild.db.DataCollection deFacade(com.luntsys.luntbuild.facades.lb12.DataCollection data12) {
 		logger.info("Defacading 1.2 data collection...");
 		com.luntsys.luntbuild.db.DataCollection data = new com.luntsys.luntbuild.db.DataCollection();
 		logger.info("Defacading system settings...");
 		Iterator it = data12.getProperties().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.PropertyFacade propertyFacade =
 					(com.luntsys.luntbuild.facades.lb12.PropertyFacade) it.next();
 			Property property = new Property();
 			property.setName(propertyFacade.getName());
 			property.setFacade(propertyFacade);
 			data.getProperties().add(property);
 		}
 
 		it = data12.getProjects().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.ProjectFacade projectFacade =
 					(com.luntsys.luntbuild.facades.lb12.ProjectFacade) it.next();
 			logger.info("Defacading project: " + projectFacade.getName());
 			Project project = new Project();
 			project.setId(projectFacade.getId());
 			project.setName(projectFacade.getName());
 			project.setFacade(projectFacade);
 			data.getProjects().add(project);
 		}
 
 		it = data12.getSchedules().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.ScheduleFacade scheduleFacade =
 					(com.luntsys.luntbuild.facades.lb12.ScheduleFacade) it.next();
 			logger.info("Defacading schedule: " + data12.getProject(scheduleFacade.getProjectId()).getName() +
 					"/" + scheduleFacade.getName());
 			Schedule schedule = new Schedule();
 			schedule.setId(scheduleFacade.getId());
 			schedule.setName(scheduleFacade.getName());
 			Project project = new Project();
 			project.setId(scheduleFacade.getProjectId());
 			int projectId = data.getProjects().indexOf(project);
 			if (projectId == -1) {
 				logger.error("Unable to locate project with id: " + scheduleFacade.getProjectId());
 				continue;
 			}
 			schedule.setProject((Project) data.getProjects().get(projectId));
 			schedule.setFacade(scheduleFacade);
 			data.getSchedules().add(schedule);
 		}
 		it = data.getSchedules().iterator();
 		while (it.hasNext()) {
 			Schedule schedule = (Schedule) it.next();
 			logger.info("Remapping dependent schedules for schedule: " + schedule.getProject().getName() +
 					"/" + schedule.getName());
 			Iterator itDependent = schedule.getDependentScheduleIds().iterator();
 			while (itDependent.hasNext()) {
 				long dependentScheduleId = ((Long) itDependent.next()).longValue();
 				Schedule dependentSchedule = new Schedule();
 				dependentSchedule.setId(dependentScheduleId);
 				int index = data.getSchedules().indexOf(dependentSchedule);
 				if (index != -1) {
 					schedule.getDependentSchedules().add(data.getSchedules().get(index));
 				} else {
 					logger.error("Unable to get dependent schedule with id: " + dependentScheduleId);
 					continue;
 				}
 			}
 		}
 
 		it = data12.getBuilds().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.BuildFacade buildFacade = (com.luntsys.luntbuild.facades.lb12.BuildFacade) it.next();
 			logger.info("Defacading build: " + data.getSchedule(buildFacade.getScheduleId()).getProject().getName() +
 					"/" + data.getSchedule(buildFacade.getScheduleId()) + "/" + buildFacade.getVersion());
 			Build build = new Build();
 			build.setId(buildFacade.getId());
 			Schedule schedule = new Schedule();
 			schedule.setId(buildFacade.getScheduleId());
 			int index = data.getSchedules().indexOf(schedule);
 			if (index == -1) {
 				logger.error("Unable to get schedule with id: " + buildFacade.getScheduleId());
 				continue;
 			}
 			build.setSchedule((Schedule) data.getSchedules().get(index));
 			build.setFacade(buildFacade);
 			data.getBuilds().add(build);
 		}
 
 		it = data12.getUsers().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.UserFacade userFacade =
 					(com.luntsys.luntbuild.facades.lb12.UserFacade) it.next();
 			logger.info("Defacading user: " + userFacade.getName());
 			User user = new User();
 			user.setId(userFacade.getId());
 			user.setName(userFacade.getName());
 			user.setFacade(userFacade);
 			data.getUsers().add(user);
 		}
 
 		it = data12.getRoles().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.RoleFacade roleFacade =
 					(com.luntsys.luntbuild.facades.lb12.RoleFacade) it.next();
 			logger.info("Defacading role: " + roleFacade.getName());
 			Role role = new Role();
 			role.setId(roleFacade.getId());
 			role.setName(roleFacade.getName());
 			role.setFacade(roleFacade);
 			data.getRoles().add(role);
 		}
 
 		logger.info("Defacading vcs logins...");
 		it = data12.getVcsLoginMapping().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.VcsLoginFacade vcsLoginFacade =
 					(com.luntsys.luntbuild.facades.lb12.VcsLoginFacade) it.next();
 			VcsLogin vcsLogin = new VcsLogin();
 			vcsLogin.setLogin(vcsLoginFacade.getLogin());
 			Project project = new Project();
 			project.setId(vcsLoginFacade.getProjectId());
 			User user = new User();
 			user.setId(vcsLoginFacade.getUserId());
 			int index = data.getProjects().indexOf(project);
 			if (index == -1) {
 				logger.error("Unable to find project with id: " + vcsLoginFacade.getProjectId());
 				continue;
 			}
 			vcsLogin.setProject((Project) data.getProjects().get(index));
 			index = data.getUsers().indexOf(user);
 			if (index == -1) {
 				logger.error("Unable to find user with id: " + vcsLoginFacade.getUserId());
 				continue;
 			}
 			vcsLogin.setUser((User) data.getUsers().get(index));
 			data.getVcsLoginMapping().add(vcsLogin);
 		}
 
 		logger.info("Defacading roles mappings...");
 		it = data12.getRolesMapping().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.RolesMappingFacade rolesMappingFacade =
 					(com.luntsys.luntbuild.facades.lb12.RolesMappingFacade) it.next();
 			RolesMapping rolesMapping = new RolesMapping();
 			Project project = new Project();
 			project.setId(rolesMappingFacade.getProjectId());
 			int projectId = data.getProjects().indexOf(project);
 			if (projectId == -1) {
 				logger.error("Unable to locate project with id: " + rolesMappingFacade.getProjectId());
 				continue;
 			}
 			project = (Project) data.getProjects().get(projectId);
 			User user = new User();
 			user.setId(rolesMappingFacade.getUserId());
 			Role role = new Role();
 			role.setId(rolesMappingFacade.getRoleId());
 			rolesMapping.setProject(project);
 			int index = data.getUsers().indexOf(user);
 			if (index == -1) {
 				logger.error("Unable to get user with id: " + rolesMappingFacade.getUserId());
 				continue;
 			} else {
 				rolesMapping.setUser((User) data.getUsers().get(index));
 			}
 			index = data.getRoles().indexOf(role);
 			if (index == -1) {
 				logger.error("Unable to get role with id: " + rolesMappingFacade.getRoleId());
 				continue;
 			} else {
 				rolesMapping.setRole((Role) data.getRoles().get(index));
 			}
 			project.getRolesMappings().add(rolesMapping);
 		}
 
 		logger.info("Defacading notify mappings...");
 		it = data12.getNotifyMapping().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.NotifyMappingFacade notifyMappingFacade =
 					(com.luntsys.luntbuild.facades.lb12.NotifyMappingFacade) it.next();
 			NotifyMapping notifyMapping = new NotifyMapping();
 			Project project = new Project();
 			project.setId(notifyMappingFacade.getProjectId());
 			int projectId = data.getProjects().indexOf(project);
 			if (projectId == -1) {
 				logger.error("Unable to get project with id: " + notifyMappingFacade.getProjectId());
 				continue;
 			}
 			project = (Project) data.getProjects().get(projectId);
 			User user = new User();
 			user.setId(notifyMappingFacade.getUserId());
 			int userId = data.getUsers().indexOf(user);
 			if (userId == -1) {
 				logger.error("Unable to get user with id: " + notifyMappingFacade.getUserId());
 				continue;
 			} else {
 				user = (User) data.getUsers().get(userId);
 			}
 			notifyMapping.setProject(project);
 			notifyMapping.setUser(user);
 			project.getNotifyMappings().add(notifyMapping);
 		}
 
 		it = data.getProjects().iterator();
 		while (it.hasNext()) {
 			Project project = (Project) it.next();
 			project.setId(0);
 		}
 		it = data.getSchedules().iterator();
 		while (it.hasNext()) {
 			Schedule schedule = (Schedule) it.next();
 			schedule.setId(0);
 		}
 		it = data.getBuilds().iterator();
 		while (it.hasNext()) {
 			Build build = (Build) it.next();
 			build.setId(0);
 		}
 		it = data.getUsers().iterator();
 		while (it.hasNext()) {
 			User user = (User) it.next();
 			user.setId(0);
 		}
 		it = data.getRoles().iterator();
 		while (it.hasNext()) {
 			Role role = (Role) it.next();
 			role.setId(0);
 		}
 		return data;
 	}
 
 	/**
 	 * Converts a version 1.2 compatable project facade collection object into a version 1.2 compatable project collection object.
 	 * 
 	 * @param project12 the project facade collection object
 	 * @return the project collection object
 	 */
 	public static com.luntsys.luntbuild.db.ProjectCollection deFacade(com.luntsys.luntbuild.facades.lb12.ProjectCollection project12) {
 		logger.info("Defacading 1.2 project collection...");
 		Dao dao = Luntbuild.getDao();
 		com.luntsys.luntbuild.db.ProjectCollection data = new com.luntsys.luntbuild.db.ProjectCollection();
 
 		// Defacade project
 		com.luntsys.luntbuild.facades.lb12.ProjectFacade projectFacade = project12.getProject();
 		logger.info("Defacading project: " + projectFacade.getName());
 		Project project = new Project();
 		project.setId(0);
 		project.setName(projectFacade.getName());
 		project.setFacade(projectFacade);
 		// Check for existing project
 		while (dao.isProjectNameUsed(project)) {
 			logger.warn("Project name \"" + project.getName() + "\" already in use, changing name to \""
 					+ project.getName() + "_copy\".");
 			project.setName(project.getName() + "_copy");
 		}
 		data.setProject(project);
 
 		Iterator it = project12.getSchedules().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.ScheduleFacade scheduleFacade =
 					(com.luntsys.luntbuild.facades.lb12.ScheduleFacade) it.next();
 			logger.info("Defacading schedule: " + projectFacade.getName() + "/" + scheduleFacade.getName());
 			scheduleFacade.setDependentScheduleIds(new ArrayList());
 			Schedule schedule = new Schedule();
 			schedule.setId(scheduleFacade.getId());
 			schedule.setName(scheduleFacade.getName());
 			schedule.setScheduleDisabled(scheduleFacade.isScheduleDisabled());
 			schedule.setProject(project);
 			schedule.setFacade(scheduleFacade);
 			data.getSchedules().add(schedule);
 
			Iterator buildit = project12.getBuilds().iterator();
			while (buildit.hasNext()) {
				com.luntsys.luntbuild.facades.lb12.BuildFacade buildFacade = (com.luntsys.luntbuild.facades.lb12.BuildFacade) buildit.next();
 				logger.info("Defacading build: " + data.getSchedule(buildFacade.getScheduleId()).getProject().getName() +
 						"/" + data.getSchedule(buildFacade.getScheduleId()) + "/" + buildFacade.getVersion());
 				Build build = new Build();
 				build.setId(buildFacade.getId());
 				build.setSchedule(schedule);
 				build.setFacade(buildFacade);
 				data.getBuilds().add(build);
 			}
 		}
 
 		logger.info("Defacading vcs logins...");
 		it = project12.getVcsLoginMapping().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.VcsLoginFacade vcsLoginFacade =
 				(com.luntsys.luntbuild.facades.lb12.VcsLoginFacade) it.next();
 			com.luntsys.luntbuild.facades.lb12.UserFacade userFacade =
 				project12.getUser(vcsLoginFacade.getUserId());
 			User user = data.getUser(userFacade.getName());
 			if (user == null) {
 				if (dao.isUserExist(userFacade.getName())) {
 					user = dao.loadUser(userFacade.getName());
 				} else {
 					logger.info("Creating missing user \"" + userFacade.getName() + "\".");
 					user = new User();
 					user.setId(userFacade.getId());
 					user.setName(userFacade.getName());
 					user.setFacade(userFacade);
 					data.getUsers().add(user);
 				}
 			}
 			VcsLogin vcsLogin = new VcsLogin();
 			vcsLogin.setProject(project);
 			vcsLogin.setUser(user);
 			vcsLogin.setLogin(vcsLoginFacade.getLogin());
 			data.getVcsLoginMapping().add(vcsLogin);
 		}
 
 		logger.info("Defacading roles mappings...");
 		it = project12.getRolesMapping().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.RolesMappingFacade rolesMappingFacade =
 				(com.luntsys.luntbuild.facades.lb12.RolesMappingFacade) it.next();
 			com.luntsys.luntbuild.facades.lb12.UserFacade userFacade =
 				project12.getUser(rolesMappingFacade.getUserId());
 			User user = data.getUser(userFacade.getName());
 			if (user == null) {
 				if (dao.isUserExist(userFacade.getName())) {
 					user = dao.loadUser(userFacade.getName());
 				} else {
 					logger.info("Creating missing user \"" + userFacade.getName() + "\".");
 					user = new User();
 					user.setId(userFacade.getId());
 					user.setName(userFacade.getName());
 					user.setFacade(userFacade);
 					data.getUsers().add(user);
 				}
 			}
 			Role role = dao.loadRole(project12.getRole(rolesMappingFacade.getRoleId()).getName());
 			RolesMapping rolesMapping = new RolesMapping();
 			rolesMapping.setProject(project);
 			rolesMapping.setUser(user);
 			rolesMapping.setRole(role);
 			project.getRolesMappings().add(rolesMapping);
 		}
 
 		logger.info("Defacading notify mappings...");
 		it = project12.getNotifyMapping().iterator();
 		while (it.hasNext()) {
 			com.luntsys.luntbuild.facades.lb12.NotifyMappingFacade notifyMappingFacade =
 					(com.luntsys.luntbuild.facades.lb12.NotifyMappingFacade) it.next();
 			com.luntsys.luntbuild.facades.lb12.UserFacade userFacade =
 				project12.getUser(notifyMappingFacade.getUserId());
 			User user = data.getUser(userFacade.getName());
 			if (user == null) {
 				if (dao.isUserExist(userFacade.getName())) {
 					user = dao.loadUser(userFacade.getName());
 				} else {
 					logger.info("Creating missing user \"" + userFacade.getName() + "\".");
 					user = new User();
 					user.setId(userFacade.getId());
 					user.setName(userFacade.getName());
 					user.setFacade(userFacade);
 					data.getUsers().add(user);
 				}
 			}
 			NotifyMapping notifyMapping = new NotifyMapping();
 			notifyMapping.setProject(project);
 			notifyMapping.setUser(user);
 			project.getNotifyMappings().add(notifyMapping);
 		}
 
 		project = data.getProject();
 		project.setId(0);
 		it = data.getSchedules().iterator();
 		while (it.hasNext()) {
 			Schedule schedule = (Schedule) it.next();
 			schedule.setId(0);
 		}
 		it = data.getBuilds().iterator();
 		while (it.hasNext()) {
 			Build build = (Build) it.next();
 			build.setId(0);
 		}
 		it = data.getUsers().iterator();
 		while (it.hasNext()) {
 			User user = (User) it.next();
 			user.setId(0);
 		}
 
 		return data;
 	}
 }
