 /*
  * Copyright luntsys (c) 2004-2005,
  * Date: 2004-7-23
  * Time: 10:06
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
 package com.luntsys.luntbuild.vcs;
 
 import com.luntsys.luntbuild.ant.Commandline;
 import com.luntsys.luntbuild.facades.lb12.VcsFacade;
 import com.luntsys.luntbuild.db.Build;
 import com.luntsys.luntbuild.db.Schedule;
 import com.luntsys.luntbuild.utility.*;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.taskdefs.Delete;
 import org.apache.tools.ant.types.FileSet;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * The clearcase base adaptor
  *
  * @author robin shine
  */
 public class BaseClearcaseAdaptor extends AbstractClearcaseAdaptor {
 	/**
 	 * Keep tracks of version of this class, used when do serialization-deserialization
 	 */
 	static final long serialVersionUID = 2;
 
 	public String getDisplayName() {
 		return "Base Clearcase";
 	}
 
 	protected List getClearcaseAdaptorProperties() {
         return Collections.EMPTY_LIST;
     }
 
     protected void validateClearcaseAdaptorProperties() {
         // EMPTY        
     }
     
     protected boolean isSnapshot() {
         return true;
     }
     
     protected String getClearcaseWorkDirRaw(final Schedule schedule) {
         return schedule.getWorkDirRaw();
     }
 
     protected void postCreateCcView(Schedule schedule, Project antProject) {
         // EMPTY
     }
 
     public void checkoutActually(Build build, Project antProject) {
 		String workingDir = getClearcaseWorkDirRaw(build.getSchedule());
 		List loadElements = getLoadElements();
 		if (loadElements.size() == 0)
 			throw new BuildException("ERROR: No elements configured for load in the view config spec!");
 		if (build.isCleanBuild() || build.isRebuild()) {
 			Luntbuild.deleteDir(workingDir); // delete view path before create snapshot view
			createCcView(build.getSchedule(), antProject);
 			antProject.log("Retrieving source code from Clearcase...", Project.MSG_INFO);
 		} else
 			antProject.log("Updating source code from Clearcase...", Project.MSG_INFO);
 
 		// when set the config spec, clearcase will automatically update the project working directory
 		// with latest codes
 		if (!build.isRebuild() || !containLatestVersion())
 			setCcViewCfgSpec(build.getSchedule(), viewCfgSpec, Project.MSG_VERBOSE, antProject);
 		else {
 			String rebuildCfgSpec = "element * CHECKEDOUT\n";
 			rebuildCfgSpec += "element * " + Luntbuild.getLabelByVersion(build.getVersion()) + "\n";
 			Iterator itLoadElement = loadElements.iterator();
 			while (itLoadElement.hasNext()) {
 				String loadElement = (String) itLoadElement.next();
 				rebuildCfgSpec += "load " + loadElement + "\n";
 			}
 			setCcViewCfgSpec(build.getSchedule(), rebuildCfgSpec, Project.MSG_VERBOSE, antProject);
 		}
 	}
 
 	public void label(Build build, Project antProject) {
 		if (containLatestVersion()) {
 			List loadElements = getLoadElements();
 			antProject.log("Labeling current retrieved code...", Project.MSG_INFO);
 			String workingDir = getClearcaseWorkDirRaw(build.getSchedule());
 			Iterator itLoadElement = loadElements.iterator();
 			while (itLoadElement.hasNext()) {
 				String loadElement = (String) itLoadElement.next();
 				createCcLabelType(workingDir, loadElement, Luntbuild.getLabelByVersion(build.getVersion()), antProject);
 			}
 			itLoadElement = loadElements.iterator();
 			while (itLoadElement.hasNext()) {
 				String loadElement = (String) itLoadElement.next();
 				createCcLabel(workingDir, loadElement, Luntbuild.getLabelByVersion(build.getVersion()), antProject);
 			}
 		}
 	}
 
 	protected void postSetCs(Project antProject, String workingDir) {
         antProject.log("Delete Clearcase update logs...", Project.MSG_INFO);
         Delete deleteTask = new Delete();
         deleteTask.setProject(antProject);
         FileSet fileSet = new FileSet();
         fileSet.setDir(new File(workingDir));
         fileSet.setIncludes("*.updt");
         deleteTask.addFileset(fileSet);
         deleteTask.setTaskType("delete");
         deleteTask.setTaskName("delete");
         deleteTask.execute();
     }
 
 	/**
 	 * Create the clearcase label type
 	 *
 	 * @param workingDir
 	 * @param loadElement
 	 * @param ccLabelType
 	 * @param antProject
 	 */
 	private void createCcLabelType(String workingDir, String loadElement, String ccLabelType, Project antProject) {
 		String vobPath = Luntbuild.concatPath(workingDir, loadElement);
 
 		Commandline cmdLine = buildCleartoolExecutable();
 		cmdLine.createArgument().setLine("mklbtype -c \"build_label\" " + ccLabelType);
 		try {
 			new MyExecTask("mklbtype", antProject, vobPath, cmdLine, null, null, Project.MSG_INFO).execute();
 		} catch (BuildException e) {
 			// then re-try with -replace option
 			cmdLine.clearArgs();
 			cmdLine.createArgument().setLine("mklbtype -replace -c \"build_label\" " + ccLabelType);
 			new MyExecTask("mklbtype", antProject, vobPath, cmdLine, null, null, Project.MSG_INFO).execute();
 		}
 	}
 
 	/**
 	 * Create the clearcase label
 	 *
 	 * @param workingDir
 	 * @param loadElement
 	 * @param ccLabelType
 	 * @param antProject
 	 */
 	private void createCcLabel(String workingDir, String loadElement, String ccLabelType, Project antProject) {
 		String vobPath = Luntbuild.concatPath(workingDir, loadElement);
 
 		Commandline cmdLine = buildCleartoolExecutable();
 		cmdLine.createArgument().setLine("mklabel -recurse -c \"build_label\" " + ccLabelType);
 		cmdLine.createArgument().setValue(vobPath);
 		try {
 			new MyExecTask("mklabel", antProject, cmdLine, Project.MSG_VERBOSE).execute();
 		} catch (BuildException e) {
 			// then re-try with -replace option
 			cmdLine.clearArgs();
 			cmdLine.createArgument().setLine("mklabel -replace -recurse -c \"build_label\" " + ccLabelType);
 			cmdLine.createArgument().setValue(vobPath);
 			new MyExecTask("mklabel", antProject, cmdLine, Project.MSG_VERBOSE).execute();
 		}
 	}
 
 	/**
 	 * Get the elements configured for load in the view config spec
 	 *
 	 * @return
 	 */
 	private List getLoadElements() {
 		BufferedReader reader = new BufferedReader(new StringReader(viewCfgSpec.replace(';', '\n')));
 		List loadElements = new ArrayList();
 		try {
 			String line;
 			Pattern pattern = Pattern.compile("^\\s*load(.*)", Pattern.CASE_INSENSITIVE);
 			while ((line = reader.readLine()) != null) {
 				Matcher matcher = pattern.matcher(line);
 				if (matcher.find())
 					loadElements.add(matcher.group(1).trim());
 			}
 		} catch (IOException e) {
 		}
 		return loadElements;
 	}
 
 	protected void prepForHistory(Schedule workingSchedule, Project antProject, String workingDir) {
         try {
 			setCcViewCfgSpec(workingSchedule, viewCfgSpec, -1, antProject);
 		} catch (BuildException e) {
 			antProject.log("Failed to update the work directory with current config spec, " +
 					"try to re-create work directory...", Project.MSG_INFO);
 			cleanupCheckout(workingSchedule, antProject);
 			Luntbuild.deleteDir(workingDir);
 			createCcView(workingSchedule, antProject);
 			setCcViewCfgSpec(workingSchedule, viewCfgSpec, -1, antProject);
 		}
     }
 
 	public VcsFacade constructFacade() {
 		return new com.luntsys.luntbuild.facades.lb12.BaseClearcaseAdaptorFacade();
 	}
 
     protected void saveAdditionalStuffToFacade(VcsFacade facade) {
         // EMPTY        
     }
     
     protected void loadAdditionalStuffFromFacade(VcsFacade facade) {
         // EMPTY        
     }
     
 	public void cleanupCheckout(Schedule workingSchedule, Project antProject) {
 		String workingDir = getClearcaseWorkDirRaw(workingSchedule);
 		if (ccViewExists(workingSchedule, antProject)) {
 			deleteCcView(workingDir, antProject);
 			Luntbuild.createDir(workingDir);
 		} else
 			super.cleanupCheckout(workingSchedule, antProject);
 	}
 }
