 package nz.org.nesi.goldwrap.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jws.WebService;
 import javax.ws.rs.Path;
 
 import nz.org.nesi.goldwrap.Config;
 import nz.org.nesi.goldwrap.api.GoldWrapService;
 import nz.org.nesi.goldwrap.domain.Allocation;
 import nz.org.nesi.goldwrap.domain.ExternalCommand;
 import nz.org.nesi.goldwrap.domain.Machine;
 import nz.org.nesi.goldwrap.domain.Project;
 import nz.org.nesi.goldwrap.domain.User;
 import nz.org.nesi.goldwrap.errors.MachineFault;
 import nz.org.nesi.goldwrap.errors.ProjectFault;
 import nz.org.nesi.goldwrap.errors.ServiceException;
 import nz.org.nesi.goldwrap.errors.UserFault;
 import nz.org.nesi.goldwrap.util.GoldHelper;
 import nz.org.nesi.goldwrap.utils.BeanHelpers;
 import nz.org.nesi.goldwrap.utils.JSONHelpers;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.io.Files;
 
 @WebService(endpointInterface = "nz.org.nesi.goldwrap.api.GoldWrapService", name = "GoldWrapService")
 @Path("/goldwrap")
 public class GoldWrapServiceImpl implements GoldWrapService {
 
 	public static Logger myLogger = LoggerFactory
 			.getLogger(GoldWrapServiceImpl.class);
 
 	private static ExternalCommand executeGoldCommand(String command) {
 		ExternalCommand gc = new ExternalCommand(command);
 		gc.execute();
 		gc.verify();
 		return gc;
 	}
 
 	private static ExternalCommand executeGoldCommand(List<String> command) {
 		ExternalCommand gc = new ExternalCommand(command);
 		gc.execute();
 		gc.verify();
 		return gc;
 	}
 
 	private static volatile boolean initialized = false;
 
 	public GoldWrapServiceImpl() {
 		initialize();
 	}
 
 	public synchronized void initialize() {
 
 		if (!initialized) {
 
 			try {
 
 				File configDir = Config.getConfigDir();
 
 				myLogger.debug("Running init commands...");
 				File initFile = new File(configDir, "init.config");
 
 				if (initFile.exists()) {
 					List<String> lines = null;
 					try {
 						lines = Files.readLines(initFile, Charsets.UTF_8);
 					} catch (IOException e1) {
 						throw new RuntimeException("Can't read file: "
 								+ initFile.toString());
 					}
 
 					for (String line : lines) {
 						line = line.trim();
 						if (StringUtils.isEmpty(line) || line.startsWith("#")) {
 							continue;
 						}
 						myLogger.debug("Executing: " + line);
 						ExternalCommand ec = executeGoldCommand(line);
 
 						myLogger.debug("StdOut:\n\n{}\n\n", Joiner.on("\n")
 								.join(ec.getStdOut()));
 						myLogger.debug("StdErr:\n\n{}\n\n", Joiner.on("\n")
 								.join(ec.getStdErr()));
 
 					}
 
 				}
 				myLogger.debug("Trying to initialize static values...");
 
 				File machinesFile = new File(configDir, "machines.json");
 
 				if (machinesFile.exists()) {
 					try {
 						List<Machine> machines = JSONHelpers.readJSONfile(
 								machinesFile, Machine.class);
 
 						for (Machine m : machines) {
 
 							Machine mInGold = null;
 							try {
 								mInGold = getMachine(m.getName());
 								myLogger.debug("Machine " + m.getName()
 										+ " in Gold, modifying it...");
 								modifyMachine(m.getName(), m);
 							} catch (MachineFault mf) {
 								myLogger.debug("Machine " + m.getName()
 										+ " not in Gold, creating it...");
 								createMachine(m);
 							}
 
 						}
 
 					} catch (Exception e) {
 						throw new RuntimeException("Can't parse json file: "
 								+ machinesFile.toString(), e);
 					}
 				}
 			} finally {
 				initialized = true;
 			}
 		}
 
 	}
 
 	public Project addUserToProject(String projName, String userId) {
 		return GoldHelper.addUserToProject(projName, userId);
 	}
 
 	private void checkProjectname(String projectname) {
 		if (StringUtils.isBlank(projectname)) {
 			throw new ServiceException("Can't execute operation.",
 					"Projectname blank or not specified.");
 		}
 	}
 
 	private void checkUsername(String username) {
 		if (StringUtils.isBlank(username)) {
 			throw new ServiceException("Can't execute operation.",
 					"Username blank or not specified.");
 		}
 	}
 
 	public void checkMachineName(String machineName) {
 		if (StringUtils.isBlank(machineName)) {
 			throw new ServiceException("Can't execute operation.",
 					"Machine name blank or not specified.");
 		}
 	}
 
 	public void createMachine(Machine mach) {
 
 		String machName = mach.getName();
 		mach.validate(true);
 
 		if (GoldHelper.machineExists(machName)) {
 			throw new MachineFault(mach, "Can't create machine " + machName,
 					"Machine name '" + machName + "' already exists in Gold.");
 		}
 
 		StringBuffer command = new StringBuffer("gmkmachine ");
 
 		String desc = mach.getDescription();
 		if (StringUtils.isNotBlank(desc)) {
 			command.append("-d '" + desc + "' ");
 		}
 
 		String arch = mach.getArch();
 		if (StringUtils.isNotBlank(arch)) {
 			command.append("--arch '" + arch + "' ");
 		}
 
 		String opsys = mach.getOpsys();
 		if (StringUtils.isNotBlank(opsys)) {
 			command.append("--opsys '" + opsys + "' ");
 		}
 
 		command.append(machName);
 
 		ExternalCommand ec = executeGoldCommand(command.toString());
 
 		if (!GoldHelper.machineExists(machName)) {
 			throw new MachineFault(mach, "Can't create machine.",
 					"Unknow reason");
 		}
 
 	}
 
 	public void createProject(Project proj) {
 
 		String projName = proj.getProjectId();
 
 		proj.validate(true);
 
 		if (GoldHelper.projectExists(projName)) {
 			throw new ProjectFault(proj, "Can't create project " + projName,
 					"Project name '" + projName + "' already exists in Gold.");
 		}
 
 		String principal = proj.getPrincipal();
 		if (StringUtils.isNotBlank(principal)) {
 			try {
 				User princ = getUser(principal);
 			} catch (Exception e) {
 				throw new ProjectFault(proj,
 						"Can't create project " + projName, "Principal '"
 								+ principal + "' does not exist in Gold.");
 			}
 		}
 
 		List<User> users = proj.getUsers();
 		if (users != null) {
 			users = Lists.newArrayList(users);
 		} else {
 			users = Lists.newArrayList();
 		}
 
 		for (User user : users) {
 			String userId = user.getUserId();
 			if (StringUtils.isBlank(userId)) {
 				throw new ProjectFault(proj,
 						"Can't create project " + projName,
 						"Userid not specified.");
 			}
 			// if (!GoldHelper.isRegistered(userId)) {
 			// throw new ProjectFault(proj,
 			// "Can't create project " + projName, "User '" + userId
 			// + "' does not exist in Gold yet.");
 			// }
 		}
 
 		List<String> command = Lists.newArrayList("gmkproject");
 
 		proj.setUsers(new ArrayList<User>());
 		proj.setAllocations(new ArrayList<Allocation>());
 
 		String desc = JSONHelpers.convertToJSONString(proj);
 		// desc = "{\"projectId\":\"" + projName + "\"}";
 		command.add("-d");
 		command.add(desc);
 		// command.add("'" + desc + "'");
 
 		// String users = Joiner.on(",").join(proj.getUsers());
 		//
 		// if (StringUtils.isNotBlank(users)) {
 		// command.append("-u '" + users + "' ");
 		// }
 
 		command.add("--createAccount=False");
 
 		if (proj.isFunded()) {
 			command.add("-X");
 			command.add("Funded=True");
 		} else {
 			command.add("-X");
 			command.add("Funded=False");
 		}
 
 		command.add(projName);
 
 		ExternalCommand ec = executeGoldCommand(command);
 
 		if (!GoldHelper.projectExists(projName)) {
 			throw new ProjectFault(proj, "Can't create project.",
 					"Unknow reason");
 		}
 
 		myLogger.debug("Creating account...");
 		String command2 = "gmkaccount ";
 
 		command2 = command2 + "-p " + projName + " ";
 		command2 = command2 + "-n " + "acc_" + projName;
 		ExternalCommand ec2 = executeGoldCommand(command2);
 
 		int exitCode = ec2.getExitCode();
 		if (exitCode != 0) {
 			try {
 				myLogger.debug("Trying to delete project {}...", projName);
 				deleteProject(projName);
 			} catch (Exception e) {
 				myLogger.debug("Deleting project failed: {}",
 						e.getLocalizedMessage());
 			}
 			throw new ProjectFault(proj, "Could not create project.",
 					"Could not create associated account for some reason.");
 		}
 		myLogger.debug("Parsing output to find out account number.");
 		try {
 			String stdout = ec2.getStdOut().get(0);
 			Iterable<String> tokens = Splitter.on(' ').split(stdout);
 			Integer accNr = Integer.parseInt(Iterables.getLast(tokens));
 			Project tempProj = new Project(projName);
 			tempProj.setAccountId(accNr);
 			// remove ANY user
 			myLogger.debug("Removeing ANY user from account {}", accNr);
 			String removeAnyCommand = "gchaccount --delUsers ANY " + accNr;
 			ExternalCommand removeCommand = executeGoldCommand(removeAnyCommand);
 			modifyProject(projName, tempProj);
 		} catch (Exception e) {
 			try {
 				myLogger.debug("Trying to delete project {}...", projName);
 				deleteProject(projName);
 			} catch (Exception e2) {
 				myLogger.debug("Deleting project failed: {}",
 						e2.getLocalizedMessage());
 			}
 			throw new ProjectFault(proj, "Could not create project.",
 					"Could not parse account nr for project.");
 		}
 
 		myLogger.debug("Account created. Now adding users...");
 
 		createOrModifyUsers(users);
 
 		addUsersToProject(projName, users);
 
 	}
 
 	private void createOrModifyUsers(List<User> users) {
 		for (User user : users) {
 			if (GoldHelper.isRegistered(user.getUserId())) {
 				myLogger.debug("Potentially modifying user " + user.getUserId());
 				modifyUser(user.getUserId(), user);
 			} else {
 				myLogger.debug("Creating user: " + user.getUserId());
 				createUser(user);
 			}
 		}
 	}
 
 	public void createUser(User user) {
 
 		user.validate(false);
 
 		String username = user.getUserId();
 		String phone = user.getPhone();
 		String email = user.getEmail();
 
 		String middlename = user.getMiddleName();
 		String fullname = user.getFirstName();
 		if (StringUtils.isNotBlank(middlename)) {
 			fullname = fullname + " " + middlename;
 		}
 		fullname = fullname + " " + user.getLastName();
 
 		String institution = user.getInstitution();
 
 		if (GoldHelper.isRegistered(username)) {
 			throw new UserFault("Can't create user.", "User " + username
 					+ " already in Gold database.", 409);
 		}
 
 		String desc = JSONHelpers.convertToJSONString(user);
 
 		String command = "gmkuser ";
 		if (StringUtils.isNotBlank(fullname)) {
 			command = command + "-n \"" + fullname + "\" ";
 		}
 		if (StringUtils.isNotBlank(email)) {
 			command = command + "-E " + email + " ";
 		}
 		if (StringUtils.isNotBlank(phone)) {
 			command = command + "-F " + phone + " ";
 		}
 
 		command = command + " -d '" + desc + "' " + username;
 
 		ExternalCommand ec = executeGoldCommand(command);
 
 		if (!GoldHelper.isRegistered(username)) {
 			throw new UserFault(user, "Can't create user.", "Unknown reason");
 		}
 
 	}
 
 	public void deleteProject(String projName) {
 
 		checkProjectname(projName);
 
 		if (!GoldHelper.projectExists(projName)) {
 			throw new ProjectFault("Can't delete project " + projName + ".",
 					"Project " + projName + " not in Gold database.", 404);
 		}
 
 		String command = "grmproject " + projName;
 		ExternalCommand ec = executeGoldCommand(command);
 
 		if (GoldHelper.projectExists(projName)) {
 			throw new ProjectFault(
 					"Could not delete project " + projName + ".",
 					"Unknown reason.", 500);
 		}
 	}
 
 	public void deleteUser(String username) {
 
 		if (StringUtils.isBlank(username)) {
 			throw new ServiceException("Can't delete user.",
 					"Username blank or not specified.");
 		}
 
 		if (!GoldHelper.isRegistered(username)) {
 			throw new UserFault("Can't delete user.", "User " + username
 					+ " not in Gold database.", 404);
 		}
 
 		String command = "grmuser " + username;
 		ExternalCommand ec = executeGoldCommand(command);
 
 		if (GoldHelper.isRegistered(username)) {
 			throw new UserFault("Could not delete user.", "Unknown reason.",
 					500);
 		}
 
 	}
 
 	public Machine getMachine(String machineName) {
 		checkMachineName(machineName);
 
 		return GoldHelper.getMachine(machineName);
 
 	}
 
 	public List<Machine> getMachines() {
 		return GoldHelper.getAllMachines();
 	}
 
 	public Project getProject(String projName) {
 
 		checkProjectname(projName);
 
 		return GoldHelper.getProject(projName);
 
 	}
 
 	public List<Project> getProjects() {
 		return GoldHelper.getAllProjects();
 	}
 
 	public List<Project> getProjectsForUser(String username) {
 		return GoldHelper.getProjectsForUser(username);
 	}
 
 	public User getUser(String username) {
 
 		checkUsername(username);
 
 		User u = GoldHelper.getUser(username);
 		return u;
 
 	}
 
 	public List<User> getUsers() {
 		return GoldHelper.getAllUsers();
 	}
 
 	public List<User> getUsersForProject(String projName) {
 		return GoldHelper.getUsersForProject(projName);
 	}
 
 	public boolean isRegistered(String user) {
 		return GoldHelper.isRegistered(user);
 	}
 
 	public Project modifyProject(String projName, Project project) {
 
 		checkProjectname(projName);
 
 		if (StringUtils.isNotBlank(project.getProjectId())
 				&& !projName.equals(project.getProjectId())) {
 			throw new ProjectFault(project, "Can't modify project.",
 					"Project name can't be changed.");
 		}
 
 		if (!GoldHelper.projectExists(projName)) {
 			throw new ProjectFault("Can't modify project.", "Project "
 					+ projName + " not in Gold database.", 404);
 		}
 
 		String principal = project.getPrincipal();
 		if (StringUtils.isNotBlank(principal)) {
 			try {
 				User princ = getUser(principal);
 			} catch (Exception e) {
 				throw new ProjectFault(project, "Can't create project "
 						+ projName, "Principal '" + principal
 						+ "' does not exist in Gold.");
 			}
 		}
 
 		List<User> users = project.getUsers();
 		if (users != null) {
 			users = Lists.newArrayList(users);
 		} else {
 			users = Lists.newArrayList();
 		}
 
 		for (User user : users) {
 			String userId = user.getUserId();
 			if (StringUtils.isBlank(userId)) {
 				throw new ProjectFault(project, "Can't modify project "
 						+ projName, "Userid not specified.");
 			}
 
 		}
 
 		project.validate(false);
 
 		Project goldProject = getProject(projName);
 		try {
 			BeanHelpers.merge(goldProject, project);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new ProjectFault(project, "Can't modify project " + projName,
 					"Can't merge new properties: " + e.getLocalizedMessage());
 		}
 
 		// we don't want to store userdata in the description
 		goldProject.setUsers(new ArrayList<User>());
 
 		List<String> command = Lists.newArrayList("gchproject");
 		String desc = JSONHelpers.convertToJSONString(goldProject);
 		command.add("-d");
 		command.add(desc);
 
 		command.add(projName);
 
		ExternalCommand ec = executeGoldCommand(command.toString());
 
 		// ensuring users are present
 		createOrModifyUsers(users);
 
 		addUsersToProject(projName, users);
 
 		Project p = getProject(projName);
 		return p;
 
 	}
 
 	public void addUsersToProject(String projectName, List<User> users) {
 
 		for (User user : users) {
 
 			addUserToProject(projectName, user.getUserId());
 
 		}
 
 	}
 
 	public Machine modifyMachine(String machName, Machine machine) {
 
 		checkMachineName(machName);
 
 		machine.validate(true);
 
 		Machine mach = null;
 		try {
 			mach = getMachine(machName);
 		} catch (Exception e) {
 			myLogger.debug("Can't load machine {}", machName, e);
 		}
 
 		if (mach == null) {
 			throw new MachineFault("Can't modify machine " + machName + ".",
 					"Machine " + machName + " not in Gold database", 404);
 		}
 
 		String newArch = machine.getArch();
 		String newOs = machine.getOpsys();
 		String newDesc = machine.getDescription();
 
 		StringBuffer command = new StringBuffer("gchmachine ");
 		if (StringUtils.isNotBlank(newDesc)) {
 			command.append("-d '" + newDesc + "' ");
 		}
 
 		if (StringUtils.isNotBlank(newArch)) {
 			command.append("--arch '" + newArch + "' ");
 		}
 
 		if (StringUtils.isNotBlank(newOs)) {
 			command.append("--opsys '" + newOs + "' ");
 		}
 
 		command.append(machName);
 
 		ExternalCommand ec = executeGoldCommand(command.toString());
 
 		return getMachine(machName);
 
 	}
 
 	public void modifyUser(String username, User user) {
 
 		if (StringUtils.isBlank(username)) {
 			throw new UserFault(user, "Can't modify user.",
 					"Username field can't be blank.");
 		}
 
 		if (StringUtils.isNotBlank(user.getUserId())
 				&& !username.equals(user.getUserId())) {
 			throw new UserFault(user, "Can't modify user.",
 					"Username can't be changed.");
 		}
 
 		if (!GoldHelper.isRegistered(username)) {
 			throw new UserFault("Can't modify user.", "User " + username
 					+ " not in Gold database.", 404);
 		}
 
 		User goldUser = getUser(username);
 		try {
 			BeanHelpers.merge(goldUser, user);
 		} catch (Exception e) {
 			throw new UserFault(goldUser, "Can't merge new user into old one.",
 					e.getLocalizedMessage());
 		}
 
 		goldUser.validate(false);
 
 		String middlename = goldUser.getMiddleName();
 		String fullname = goldUser.getFirstName();
 		if (StringUtils.isNotBlank(middlename)) {
 			fullname = fullname + " " + middlename;
 		}
 		fullname = fullname + " " + goldUser.getLastName();
 		String phone = goldUser.getPhone();
 		String institution = goldUser.getInstitution();
 		String email = goldUser.getEmail();
 
 		String desc = JSONHelpers.convertToJSONString(goldUser);
 
 		String command = "gchuser ";
 		if (StringUtils.isNotBlank(fullname)) {
 			command = command + "-n \"" + fullname + "\" ";
 		}
 		if (StringUtils.isNotBlank(email)) {
 			command = command + "-E " + email + " ";
 		}
 		if (StringUtils.isNotBlank(phone)) {
 			command = command + "-F " + phone + " ";
 		}
 
 		command = command + " -d '" + desc + "' " + username;
 
 		ExternalCommand ec = executeGoldCommand(command);
 
 		if (!GoldHelper.isRegistered(username)) {
 			throw new UserFault(goldUser, "Can't create user.",
 					"Unknown reason");
 		}
 
 	}
 }
