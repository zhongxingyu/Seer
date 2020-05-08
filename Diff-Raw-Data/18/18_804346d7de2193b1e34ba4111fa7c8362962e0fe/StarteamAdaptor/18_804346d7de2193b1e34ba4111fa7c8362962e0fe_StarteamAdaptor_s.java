 /*
  * Copyright luntsys (c) 2004-2005,
  * Date: 2004-5-9
  * Time: 10:44:16
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
 
 import com.luntsys.luntbuild.db.Build;
 import com.luntsys.luntbuild.db.Schedule;
 import com.luntsys.luntbuild.facades.lb12.ModuleFacade;
 import com.luntsys.luntbuild.facades.lb12.StarteamAdaptorFacade;
 import com.luntsys.luntbuild.facades.lb12.StarteamModuleFacade;
 import com.luntsys.luntbuild.facades.lb12.VcsFacade;
 import com.luntsys.luntbuild.utility.*;
 import com.luntsys.luntbuild.ant.starteam.StarTeamCheckout;
 import com.luntsys.luntbuild.ant.starteam.StarTeamLabel;
 import com.starbase.starteam.*;
 import com.starbase.util.OLEDate;
 import org.apache.tapestry.form.IPropertySelectionModel;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 
 import java.util.*;
 
 /**
  * Borland StarTeam VCS adaptor implementation.
  * 
  * <p>This adaptor is NOT safe for remote hosts.</p>
  *
  * @author robin shine
  */
 public class StarteamAdaptor extends Vcs {
 	/**
 	 * Keep tracks of version of this class, used when do serialization-deserialization
 	 */
 	static final long serialVersionUID = 1;
 
 	private String projectLocation;
 	private String user;
 	private String password;
 	private String convertEOL;
 
 	private transient OLEDate checkoutDate;
 
     /**
      * @inheritDoc
      */
 	public String getDisplayName() {
 		return "StarTeam";
 	}
 
     /**
      * @inheritDoc
      */
 	public String getIconName() {
 		return "starteam.jpg";
 	}
 
 	/**
      * @inheritDoc
 	 */
 	public void checkoutActually(Build build, Project antProject) {
 		// record current time (minus 10 seconds to tolerate time difference between starteam server and build server
 		checkoutDate = new OLEDate(System.currentTimeMillis() - 10000);
 		String workingDir = build.getSchedule().getWorkDirRaw();
 		// retrieve modules
 		Iterator it = getModules().iterator();
 		while (it.hasNext()) {
 			StarteamModule module = (StarteamModule) Luntbuild.cloneModule(this, (Vcs.Module) it.next());
 			if (build.isRebuild() && Luntbuild.isEmpty(module.getLabel()))
 				module.setLabel(Luntbuild.getLabelByVersion(build.getVersion()));
 			if (build.isRebuild() || build.isCleanBuild())
 				retrieveModule(workingDir, module, true, antProject);
 			else
 				retrieveModule(workingDir, module, false, antProject);
 		}
 	}
 
 	/**
      * @inheritDoc
 	 */
 	public void label(Build build, Project antProject) {
 		Iterator it = getModules().iterator();
 		while (it.hasNext()) {
 			StarteamModule module = (StarteamModule) it.next();
 			if (Luntbuild.isEmpty(module.getLabel()))
 				labelModule(module, Luntbuild.getLabelByVersion(build.getVersion()), antProject);
 		}
 	}
 
     /**
      * @inheritDoc
      */
 	public List getVcsSpecificProperties() {
 		List properties = new ArrayList();
 		properties.add(new DisplayProperty() {
 			public String getDisplayName() {
 				return "Project location";
 			}
 
 			public String getDescription() {
 				return "Location of a StarTeam project is defined as: " +
 						"<servername>:<portnum>/<projectname>, where <servername> " +
 						"is the host where the StarTeam server runs, <portnum> is " +
 						"the port number the StarTeam server uses, default value " +
 						"is 49201. <projectname> is a StarTeam project under this StarTeam server.";
 			}
 
 			public String getValue() {
 				return getProjectLocation();
 			}
 
 			public String getActualValue() {
 				return getActualProjectLocation();
 			}
 
 			public void setValue(String value) {
 				setProjectLocation(value);
 			}
 		});
 		properties.add(new DisplayProperty() {
 			public String getDisplayName() {
 				return "User";
 			}
 
 			public String getDescription() {
 				return "User name to login to the StarTeam server.";
 			}
 
 			public String getValue() {
 				return getUser();
 			}
 
 			public void setValue(String value) {
 				setUser(value);
 			}
 		});
 		properties.add(new DisplayProperty() {
 			public String getDisplayName() {
 				return "Password";
 			}
 
 			public String getDescription() {
 				return "Password to login to the StarTeam server.";
 			}
 
 			public boolean isRequired() {
 				return false;
 			}
 
 			public boolean isSecret() {
 				return true;
 			}
 
 			public String getValue() {
 				return getPassword();
 			}
 
 			public void setValue(String value) {
 				setPassword(value);
 			}
 		});
 		DisplayProperty p = new DisplayProperty() {
 			public String getDisplayName() {
 				return "Convert EOL?";
 			}
 
 			public String getDescription() {
 				return "The following values are possible:\n" +
 						"yes: all ASCII files will have their end-of-line characters adjusted " +
 						"to the EOL type of the local machine on checkout\n" +
 						"no:  the files will be checked out with whatever EOL " +
 						"characters are used on the server";
 			}
 
 			public boolean isRequired() {
 				return false;
 			}
 
             public boolean isSelect() {
                 return true;
             }
 
 			public String getValue() {
 				return getConvertEOL();
 			}
 
 			public void setValue(String value) {
 				setConvertEOL(value);
 			}
 		};
         // Create selection model
         IPropertySelectionModel model = new StarteamLineEndSelectionModel();
         // Set selection model
         p.setSelectionModel(model);
         // Set model initial value
         p.setValue(model.getValue(0));
         // Add property to properties list
         properties.add(p);
 
         return properties;
 	}
 
 	/**
 	 * Selection model used for user interface of <code>StarteamAdaptor</code>.
 	 */
     static class StarteamLineEndSelectionModel implements IPropertySelectionModel {
         String[] values = {"yes", "no"};
 
 		/**
 		 * Gets the number of options.
 		 * 
 		 * @return the number of options
 		 */
         public int getOptionCount() {
             return this.values.length;
         }
 
 		/**
 		 * Gets an option.
 		 * 
 		 * @param index the index of the opiton
 		 * @return the option
 		 */
         public Object getOption(int index) {
             return this.values[index];
         }
 
 		/**
 		 * Gets the display label of an option.
 		 * 
 		 * @param index the index of the opiton
 		 * @return the label
 		 */
         public String getLabel(int index) {
             return this.values[index];
         }
 
 		/**
 		 * Gets the value of an option.
 		 * 
 		 * @param index the index of the opiton
 		 * @return the value
 		 */
         public String getValue(int index) {
             return this.values[index];
         }
 
 		/**
 		 * Gets the option that corresponds to a value.
 		 * 
 		 * @param value the value
 		 * @return the option
 		 */
         public Object translateValue(String value) {
             return value;
         }
     }
 
 	/**
 	 * Checks out the contents from a module.
 	 * 
 	 * @param workingDir the working directory
 	 * @param module the module
 	 * @param isClean set <code>true</code> if this is a clean build
 	 * @param antProject the ant project used for logging
 	 */
 	private void retrieveModule(String workingDir, StarteamModule module, boolean isClean, Project antProject) {
 		if (isClean)
 			antProject.log("Retrieve source path \"" + module.getActualSrcPath() + "\" of StarTeam view \"" + module.getActualStarteamView() + "\"");
 		else
 			antProject.log("Update source path \"" + module.getActualSrcPath() + "\" of StarTeam view \"" + module.getActualStarteamView() + "\"");
 		String destDir;
 		if (Luntbuild.isEmpty(module.getDestPath()))
 			destDir = Luntbuild.concatPath(workingDir, module.getActualSrcPath());
 		else
 			destDir = Luntbuild.concatPath(workingDir, module.getActualDestPath());
 
 		// for a clean retrieve, we first delete the module directory, we need to do this
 		// because we do not want retrieve of this module be disturbed by pre-retrieved
 		// codes of other modules.
 		if (isClean)
 			Luntbuild.deleteDir(destDir);
 		Luntbuild.createDir(destDir);
 
 		// call ant starteam task to acquire module
 		StarTeamCheckout starteamTask = new StarTeamCheckout();
 		starteamTask.setProject(antProject);
 		starteamTask.setUserName(getUser());
 		if (Luntbuild.isEmpty(getPassword()))
 			starteamTask.setPassword("");
 		else
 			starteamTask.setPassword(getPassword());
 
 		if (Luntbuild.isEmpty(module.getStarteamView()))
 			starteamTask.setURL(getActualProjectLocation() + "/" + getStarteamProject());
 		else
 			starteamTask.setURL(getActualProjectLocation() + "/" + module.getActualStarteamView());
 
 	    if (!Luntbuild.isEmpty(module.getStarteamPromotionState()))
 	      starteamTask.setPromotionState(module.getActualStarteamPromotionState());
 	    else if (!Luntbuild.isEmpty(module.getLabel()))
 			starteamTask.setLabel(module.getActualLabel());
 		else
 			starteamTask.setAsOfDate(checkoutDate);
 		starteamTask.setRootStarteamFolder(Luntbuild.concatPath("/", module.getActualSrcPath()));
 		starteamTask.setRootLocalFolder(destDir);
 		if (Luntbuild.isEmpty(getConvertEOL()) || getConvertEOL().equalsIgnoreCase("yes"))
 			starteamTask.setConvertEOL(true);
 		else
 			starteamTask.setConvertEOL(false);
 		starteamTask.setDeleteUncontrolled(false);
 		starteamTask.setTaskType("StarTeamCheckout");
 		starteamTask.setTaskName("StarTeamCheckout");
 		starteamTask.execute();
 	}
 
 	/**
 	 * Labels the contents of a module.
 	 * 
 	 * @param module the module
 	 * @param label the label to use
 	 * @param antProject the ant project used for logging
 	 */
 	private void labelModule(StarteamModule module, String label, Project antProject) {
 		antProject.log("Label source path \"" + module.getActualSrcPath() + "\" of StarTeam view \"" + module.getActualStarteamView() + "\"");
 
 		// call ant starteam task to label module
 		StarTeamLabel starteamTask = new StarTeamLabel();
 		starteamTask.setProject(antProject);
 		starteamTask.setUserName(getUser());
 		if (Luntbuild.isEmpty(getPassword()))
 			starteamTask.setPassword("");
 		else
 			starteamTask.setPassword(getPassword());
 
 		if (Luntbuild.isEmpty(module.getStarteamView()))
 			starteamTask.setURL(getActualProjectLocation() + "/" + getStarteamProject());
 		else
 			starteamTask.setURL(getActualProjectLocation() + "/" + module.getActualStarteamView());
 
 		starteamTask.setLastBuild(checkoutDate);
 		starteamTask.setLabel(label);
 		starteamTask.setDescription("luntbuild labels");
 		starteamTask.setTaskType("StarTeamLabel");
 		starteamTask.setTaskName("StarTeamLabel");
 		starteamTask.execute();
 	}
 
 	/**
      * @inheritDoc
 	 * @see StarteamModule
 	 */
 	public Module createNewModule() {
 		return new StarteamModule();
 	}
 
 	/**
      * @inheritDoc
 	 * @see StarteamModule
 	 */
     public Module createNewModule(Module module) {
         return new StarteamModule((StarteamModule)module);
     }
 
 	/**
      * @inheritDoc
 	 */
 	public Revisions getRevisionsSince(Date sinceDate, Schedule workingSchedule, Project antProject) {
 		Revisions revisions = new Revisions();
         revisions.addLog(this.getClass().getName(), toString());
         revisions.getChangeLogs().add("*************************************************************");
         revisions.getChangeLogs().add(toString());
         revisions.getChangeLogs().add("");
 
 		Iterator it = getModules().iterator();
 		while (it.hasNext()) {
 			StarteamModule module = (StarteamModule) it.next();
 			if (Luntbuild.isEmpty(module.getLabel())) {
 				Server server = null;
 				try {
 					String viewName;
 					if (Luntbuild.isEmpty(module.getStarteamView()))
 						viewName = getStarteamProject();
 					else
 						viewName = module.getActualStarteamView();
 					String viewUrl = getActualProjectLocation() + "/" + viewName;
 
 					// get the specified floating view
 					View view;
 					if (!Luntbuild.isEmpty(getPassword()))
 						view = StarTeamFinder.openView(getUser() + ":" + getPassword() + "@" + viewUrl);
 					else
 						view = StarTeamFinder.openView(getUser() + "@" + viewUrl);
 
 					if (view == null)
 						throw new BuildException("StarTeam view \"" + viewName + "\" not found!");
 
 					server = view.getServer();
 
 					// shift back 10 seconds to tolerate time difference between starteam server and build server
 					OLEDate currentTime = new OLEDate(System.currentTimeMillis() - 10000);
 					OLEDate buildTime = new OLEDate(sinceDate);
 
 					// get the time based configuration at now and at last build's start time
 					View currentConfiguration = new View(view, ViewConfiguration.createFromTime(currentTime));
 					View buildConfiguration = new View(view, ViewConfiguration.createFromTime(buildTime));
 
 					// use map to hold all files for current configuration and last build's configuration respectively
 					Map currentFiles = new HashMap();
 					Map buildFiles = new HashMap();
 
 					Folder currentRoot = StarTeamFinder.findFolder(currentConfiguration.getRootFolder(),
 							Luntbuild.concatPath("/", module.getActualSrcPath()));
 					if (currentRoot == null)
 						throw new BuildException("Source path \"" + module.getActualSrcPath() + "\" " +
 								"does not exist in the current configuration of StarTeam view \"" + viewName + "\"");
 					Folder buildRoot = StarTeamFinder.findFolder(buildConfiguration.getRootFolder(),
 							Luntbuild.concatPath("/", module.getActualSrcPath()));
 					if (buildRoot == null)
 						throw new BuildException("Source path \"" + module.getActualSrcPath() + "\" " +
 								"does not exist in the last build's configuration of StarTeam view \"" + viewName + "\"");
 
 					// fill file maps with files recursively
 					getFolderFiles(currentRoot, currentFiles);
 					getFolderFiles(buildRoot, buildFiles);
 
 					// iterate through fetched files of both configurations to identify
 					// change logs
 					Iterator itFile = currentFiles.keySet().iterator();
 					while (itFile.hasNext()) {
 						Integer itemId = (Integer) itFile.next();
 						File currentFile = (File) currentFiles.get(itemId);
 						if (buildFiles.containsKey(itemId)) {
 							File buildFile = (File) buildFiles.get(itemId);
							if (currentFile.getContentVersion() != buildFile.getContentVersion())
								addChangeLogs(currentFile, "File modified", revisions);
							else if (!currentFile.getParentFolderHierarchy().equals(buildFile.getParentFolderHierarchy()))
								addChangeLogs(currentFile, "File moved", revisions);
 							buildFiles.remove(itemId);
 						} else {
 							addChangeLogs(currentFile, "File added", revisions);
 						}
 					}
 					// find files already been deleted
 					itFile = buildFiles.values().iterator();
 					while (itFile.hasNext()) {
 						File file = (File) itFile.next();
 						addChangeLogs((File) file.getFromHistoryByDate(currentTime), "File deleted", revisions);
 					}
 
 					// release resources
 					currentConfiguration.getRootFolder().discardItems(server.getTypeNames().FILE, -1);
 					buildConfiguration.getRootFolder().discardItems(server.getTypeNames().FILE, -1);
 				} finally {
 					if (server != null)
 						server.disconnect();
 				}
 			}
 		}
		if (revisions.getChangeLogs().size() != 0)
			revisions.setFileModified(true);
 		return revisions;
 	}
 
 	private void getFolderFiles(Folder folder, Map files) {
 		Item[] items = folder.getItems("File");
 		for (int i = 0; i < items.length; i++) {
 			File file = (File) items[i];
 			files.put(new Integer(file.getItemID()), file);
 		}
 
 		Folder[] subFolders = folder.getSubFolders();
 		for (int i = 0; i < subFolders.length; i++) {
 			getFolderFiles(subFolders[i], files);
 		}
 	}
 
 	private void addChangeLogs(File file, String action, Revisions revisions) {
 		User user = file.getServer().getUser(file.getModifiedBy());
 		String userName = "<unknown user>";
 		if (user != null) {
 			revisions.getChangeLogins().add(user.getName());
 			userName = user.getName();
 		}
 		revisions.addEntryToLastLog("", userName, file.getModifiedTime().createDate(), file.getComment());
 		revisions.addPathToLastEntry(file.getParentFolderHierarchy() + file.getName(), action, "");
 		revisions.getChangeLogs().add(action + ": " + userName + " | " + file.getModifiedTime().createDate().toString() +
 				" | " + file.getParentFolderHierarchy() + file.getName());
 		revisions.getChangeLogs().add("Comment: " + file.getComment());
 		revisions.getChangeLogs().add("");
 	}
 
 	/**
 	 * Gets the project location.
 	 * <p>Location of a StarTeam project is defined as: <servername>:<portnum>/<projectname>,
 	 * where <servername> is the host where the StarTeam server runs, <portnum> is the port
 	 * number the StarTeam server uses, default value is 49201. <projectname> is a StarTeam
 	 * project under this StarTeam server.</p>
 	 * 
 	 * @return the project location
 	 */
 	public String getProjectLocation() {
 		return projectLocation;
 	}
 
 	/**
 	 * Gets the project location. This method will parse OGNL variables.
 	 * <p>Location of a StarTeam project is defined as: <servername>:<portnum>/<projectname>,
 	 * where <servername> is the host where the StarTeam server runs, <portnum> is the port
 	 * number the StarTeam server uses, default value is 49201. <projectname> is a StarTeam
 	 * project under this StarTeam server.</p>
 	 * 
 	 * @return the project location
 	 */
 	public String getActualProjectLocation() {
 		return OgnlHelper.evaluateScheduleValue(getProjectLocation());
 	}
 
 	/**
 	 * Sets the project location.
 	 * 
 	 * @param projectLocation the project location
 	 */
 	public void setProjectLocation(String projectLocation) {
 		this.projectLocation = projectLocation;
 	}
 
 	/**
 	 * Gets the login user.
 	 * 
 	 * @return the login user
 	 */
 	public String getUser() {
 		return user;
 	}
 
 	/**
 	 * Sets the login user.
 	 * 
 	 * @param user the login user
 	 */
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	/**
 	 * Gets the login password.
 	 * 
 	 * @return the login password
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * Sets the login password.
 	 * 
 	 * @param password the login password
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	/**
 	 * Gets the convert EOL property ("yes"/"no").
 	 * 
 	 * @return the convert EOL property
 	 */
 	public String getConvertEOL() {
 		return convertEOL;
 	}
 
 	/**
 	 * Sets the convert EOL property ("yes"/"no").
 	 * 
 	 * @param convertEOL the convert EOL property
 	 */
 	public void setConvertEOL(String convertEOL) {
 		this.convertEOL = convertEOL;
 	}
 
 	private String getStarteamProject() {
 		String[] fields = getActualProjectLocation().split("/");
 		if (fields.length != 2 || fields[1].trim().equals(""))
 			throw new ValidationException("Invalid value for project location: " + getActualProjectLocation());
 		return fields[1].trim();
 	}
 
     /**
      * Validates the properties of this VCS.
      *
      * @throws ValidationException if a property has an invalid value
      */
 	public void validateProperties() {
 		super.validateProperties();
 		String[] fields = getActualProjectLocation().split("/");
 		if (fields.length != 2 || fields[1].trim().equals(""))
 			throw new ValidationException("Invalid value for project location: " + getActualProjectLocation());
 		if (!Luntbuild.isEmpty(getConvertEOL())) {
 			if (!getConvertEOL().trim().equalsIgnoreCase("yes") &&
 					!getConvertEOL().trim().equalsIgnoreCase("no"))
 				throw new ValidationException("Invalid value for convert EOL: should be " +
 						"yes or no");
 			setConvertEOL(getConvertEOL().trim());
 		}
 	}
 
 	/**
 	 * A StarTeam module definition.
 	 *
 	 * @author robin shine
 	 */
 	public class StarteamModule extends Module {
 		/**
 		 * Keep tracks of version of this class, used when do serialization-deserialization
 		 */
 		static final long serialVersionUID = 1;
 
 		private String starteamView;
 		private String starteamPromotionState;
 		private String srcPath;
 		private String label;
 		private String destPath;
 
 		/**
 		 * Constructor, creates a blank StarTeam module.
 		 */
 		public StarteamModule() {}
 
 		/**
 		 * Copy constructor, creates a StarTeam module from another StarTeam module.
 		 * 
 		 * @param module the module to create from
 		 */
 		public StarteamModule(StarteamModule module) {
 			this.starteamView = module.starteamView;
 			this.srcPath = module.srcPath;
 			this.label = module.label;
 			this.destPath = module.destPath;
 		}
 
 		/**
 		 * @inheritDoc
 		 */
 		public List getProperties() {
 			List properties = new ArrayList();
 			properties.add(new DisplayProperty() {
 				public String getDisplayName() {
 					return "StarTeam view";
 				}
 
 				public String getDescription() {
 					return "Specify a StarTeam view. This property is optional. If it is left empty, the " +
 					"root view of the current StarTeam project will be used.";
 				}
 
 				public boolean isRequired() {
 					return false;
 				}
 
 				public String getValue() {
 					return getStarteamView();
 				}
 
 				public String getActualValue() {
 					return getActualStarteamView();
 				}
 
 				public void setValue(String value) {
 					setStarteamView(value);
 				}
 			});
 			properties.add(new DisplayProperty() {
 				public String getDisplayName() {
 					return "Source path";
 				}
 
 				public String getDescription() {
 					return "Specify a path relative to the root of the above StarTeam view. Enter \"/\" to " +
 					"specify the root.";
 				}
 
 				public String getValue() {
 					return getSrcPath();
 				}
 
 				public String getActualValue() {
 					return getActualSrcPath();
 				}
 
 				public void setValue(String value) {
 					setSrcPath(value);
 				}
 			});
 			properties.add(new DisplayProperty() {
 				public String getDisplayName() {
 					return "Label";
 				}
 
 				public String getDescription() {
 					return "Specify the label for the above StarTeam view. This property " +
 					"is optional. When left empty, the latest version of specified view is" +
 					" assumed.  NOTE: Only one of Promotion State or Label may be set.  " +
 					"Checkout will FAIL if both values are set!";
 				}
 
 				public boolean isRequired() {
 					return false;
 				}
 
 				public String getValue() {
 					return getLabel();
 				}
 
 				public String getActualValue() {
 					return getActualLabel();
 				}
 
 				public void setValue(String value) {
 					setLabel(value);
 				}
 			});
 			properties.add(new DisplayProperty() {
 				public String getDisplayName() {
 					return "Promotion State";
 				}
 
 				public String getDescription() {
 					return "Specify the promotion state for the above StarTeam view. This property " +
 					"is optional. When left empty, the latest version of specified view is" +
 					" assumed.  NOTE: Only one of Promotion State or Label may be set.  " +
 					"Checkout will FAIL if both values are set!";
 				}
 
 				public boolean isRequired() {
 					return false;
 				}
 
 				public String getValue() {
 					return getStarteamPromotionState();
 				}
 
 				public String getActualValue() {
 					return getActualStarteamPromotionState();
 				}
 
 				public void setValue(String value) {
 					setStarteamPromotionState(value);
 				}
 			});
 			properties.add(new DisplayProperty() {
 				public String getDisplayName() {
 					return "Destination path";
 				}
 
 				public String getDescription() {
 					return "Specify the destination directory relative to the project work directory, where " +
 					"the contents under the above source path should be retrieved to. This property " +
 					"is optional. When left empty, retrieved code will be put into directory specified in " +
 					"source path, relative to the project work directory.";
 				}
 
 				public boolean isRequired() {
 					return false;
 				}
 
 				public String getValue() {
 					return getDestPath();
 				}
 
 				public String getActualValue() {
 					return getActualDestPath();
 				}
 
 				public void setValue(String value) {
 					setDestPath(value);
 				}
 			});
 			return properties;
 		}
 
 		/**
 		 * Gets the StarTeam view.
 		 * 
 		 * @return the StarTeam view
 		 */
 		public String getStarteamView() {
 			return starteamView;
 		}
 
 		/**
 		 * Gets the StarTeam view. This method will parse OGNL variables.
 		 * 
 		 * @return the StarTeam view
 		 */
 		private String getActualStarteamView() {
 			return OgnlHelper.evaluateScheduleValue(getStarteamView());
 		}
 
 		/**
 		 * Sets the StarTeam view.
 		 * 
 		 * @param starteamView the StarTeam view
 		 */
 		public void setStarteamView(String starteamView) {
 			this.starteamView = starteamView;
 		}
 
 		/**
 		 * Gets the StarTeam promotion state.
 		 * 
 		 * @return the StarTeam promotion state
 		 */
 		public String getStarteamPromotionState() {
 			return starteamPromotionState;
 		}
 
 		/**
 		 * Gets the StarTeam promotion state. This method will parse OGNL variables.
 		 * 
 		 * @return the StarTeam promotion state
 		 */
 		private String getActualStarteamPromotionState() {
 			return OgnlHelper.evaluateScheduleValue(getStarteamPromotionState());
 		}
 
 		/**
 		 * Sets the StarTeam promotion state.
 		 * 
 		 * @param starteamPromotionState the StarTeam promotion state
 		 */
 		public void setStarteamPromotionState(String starteamPromotionState) {
 			this.starteamPromotionState = starteamPromotionState;
 		}
 
 		/**
 		 * Gets the source path. This path is relative to the StarTeam view.
 		 * 
 		 * @return the source path
 		 */
 		public String getSrcPath() {
 			return srcPath;
 		}
 
 		/**
 		 * Gets the source path. This path is relative to the StarTeam view.
 		 * This method will parse OGNL variables.
 		 * 
 		 * @return the source path
 		 */
 		private String getActualSrcPath() {
 			return OgnlHelper.evaluateScheduleValue(getSrcPath());
 		}
 
 		/**
 		 * Sets the source path.
 		 * 
 		 * @param srcPath the source path
 		 */
 		public void setSrcPath(String srcPath) {
 			this.srcPath = srcPath;
 		}
 
 		/**
 		 * Gets the label to use.
 		 * 
 		 * @return the label
 		 */
 		public String getLabel() {
 			return label;
 		}
 
 		/**
 		 * Gets the label to use. This method will parse OGNL variables.
 		 * 
 		 * @return the label
 		 */
 		private String getActualLabel() {
 			return OgnlHelper.evaluateScheduleValue(getLabel());
 		}
 
 		/**
 		 * Sets the label to use.
 		 * 
 		 * @param label the label
 		 */
 		public void setLabel(String label) {
 			this.label = label;
 		}
 
 		/**
 		 * Gets the destination path to check out to.
 		 * 
 		 * @return the destination path
 		 */
 		public String getDestPath() {
 			return destPath;
 		}
 
 		/**
 		 * Gets the destination path to check out to. This method will parse OGNL variables.
 		 * 
 		 * @return the destination path
 		 */
 		private String getActualDestPath() {
 			return OgnlHelper.evaluateScheduleValue(getDestPath());
 		}
 
 		/**
 		 * Sets the destination path to check out to.
 		 * 
 		 * @param destPath the destination path
 		 */
 		public void setDestPath(String destPath) {
 			this.destPath = destPath;
 		}
 
 	    /**
 	     * @inheritDoc
 	     * @see StarteamModuleFacade
 	     */
 		public ModuleFacade getFacade() {
 			StarteamModuleFacade facade = new StarteamModuleFacade();
 			facade.setStarteamView(getStarteamView());
 			facade.setDestPath(getDestPath());
 			facade.setLabel(getLabel());
 			facade.setStarteamPromotionState(getStarteamPromotionState());
 			facade.setSrcPath(getSrcPath());
 			return facade;
 		}
 
 	    /**
 	     * @inheritDoc
 	     * @throws RuntimeException if the facade is not an <code>StarteamModuleFacade</code>
 	     * @see StarteamModuleFacade
 	     */
 		public void setFacade(ModuleFacade facade) {
 			if (facade instanceof StarteamModuleFacade) {
 				StarteamModuleFacade starteamModuleFacade = (StarteamModuleFacade) facade;
 				setStarteamView(starteamModuleFacade.getStarteamView());
 				setLabel(starteamModuleFacade.getLabel());
 				setStarteamPromotionState(starteamModuleFacade.getStarteamPromotionState());
 				setSrcPath(starteamModuleFacade.getSrcPath());
 				setDestPath(starteamModuleFacade.getDestPath());
 			} else
 				throw new RuntimeException("Invalid facade class: " + facade.getClass().getName());
 		}
 	}
 
     /**
      * @inheritDoc
      * @see StarteamAdaptorFacade
      */
 	public void saveToFacade(VcsFacade facade) {
     	// TODO throw RuntimeException if the facade is not the right class
 		StarteamAdaptorFacade starteamFacade = (StarteamAdaptorFacade) facade;
 		starteamFacade.setConvertEOL(getConvertEOL());
 		starteamFacade.setPassword(getPassword());
 		starteamFacade.setProjectLocation(getProjectLocation());
 		starteamFacade.setUser(getUser());
 	}
 
     /**
      * @inheritDoc
      * @throws RuntimeException if the facade is not an <code>StarteamAdaptorFacade</code>
      * @see StarteamAdaptorFacade
      */
 	public void loadFromFacade(VcsFacade facade) {
 		if (!(facade instanceof StarteamAdaptorFacade))
 			throw new RuntimeException("Invalid facade class: " + facade.getClass().getName());
 		StarteamAdaptorFacade starteamFacade = (StarteamAdaptorFacade) facade;
 		setConvertEOL(starteamFacade.getConvertEOL());
 		setPassword(starteamFacade.getPassword());
 		setProjectLocation(starteamFacade.getProjectLocation());
 		setUser(starteamFacade.getUser());
 	}
 
     /**
      * @inheritDoc
      * @see StarteamAdaptorFacade
      */
 	public VcsFacade constructFacade() {
 		return new StarteamAdaptorFacade();
 	}
 }
