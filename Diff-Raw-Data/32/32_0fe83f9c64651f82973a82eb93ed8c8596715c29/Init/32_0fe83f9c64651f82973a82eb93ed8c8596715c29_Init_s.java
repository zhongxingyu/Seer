 /**
  * 
  */
 package main;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Vector;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import passiveobjects.Helpers;
 import passiveobjects.ManagerBoard;
 import passiveobjects.ManagerBoardImpl;
 import passiveobjects.ManagerSpecialization;
 import passiveobjects.Project;
 import passiveobjects.ProjectBox;
 import passiveobjects.ProjectBoxImpl;
 import passiveobjects.ProjectImpl;
 import passiveobjects.Resource;
 import passiveobjects.Task;
 import passiveobjects.TaskImpl;
 import passiveobjects.Warehouse;
 import passiveobjects.WarehouseImpl;
 import passiveobjects.WorkerSpecialty;
 import passiveobjects.WorkingBoard;
 import passiveobjects.WorkingBoardImpl;
 import repl.Repl;
 import acitiveobjects.Manager;
 import acitiveobjects.Worker;
 
 /**
  * @author lxmonk
  * 
  */
 public class Init {
 
 	/**
 	 * String arrays are used only where creating them is easier than populating
 	 * a Vector<String>, in single-threaded mode only, they are never altered
 	 * after creation.
 	 * 
 	 * @param args
 	 *            the invocation arguments.
 	 */
 	public static void main(String[] args) {
 
 		/* create the logger */
 		Logger logger = Logger.getLogger("SplAssignment1.Logger");
 		Handler fh = null;
 		try {
 			fh = new FileHandler(args[2]);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// logger output is written to a file in fh handler
 		fh.setFormatter(new OurFormatter());
 		logger.addHandler(fh);
 
 		// Set the log level specifying which message levels will be logged by
 		// this logger
 		logger.setLevel(Level.INFO); // FIXME: update this.
 		/* start the logger */
 		logger.fine("logger started.");

 		/* create the Boards */
 		ManagerBoard managerBoard = new ManagerBoardImpl();
 		logger.fine("ManagerBoardImpl created");
 		WorkingBoard workingBoard = new WorkingBoardImpl();
 		Warehouse warehouse = new WarehouseImpl();
 		List<Project> completedProjects = new Vector<Project>();
 		Map<Project, Project> executingProjects = new ConcurrentHashMap<Project, Project>();
 
 		/* create temporary data structures */
 		Set<String> managerSpecializations = new HashSet<String>();
 		Set<WorkerSpecialty> workerSpecialties = new HashSet<WorkerSpecialty>();
 		Set<Resource> resources = new HashSet<Resource>();
 		Map<String, ProjectImpl> projects = new HashMap<String, ProjectImpl>();
 		Map<String, ProjectBox> initMap = new HashMap<String, ProjectBox>();
 		Map<String, Manager> managers = new HashMap<String, Manager>();
 		List<Worker> workers = new Vector<Worker>();
 
 		/* read configuration files */
 		Properties configTxt = new Properties();
 		Properties projectsTxt = new Properties();
 		try {
 			// loading file content into data structure prop
 			configTxt.load(new FileInputStream(args[0]));
 			projectsTxt.load(new FileInputStream(args[1]));
 		} catch (IOException e) {
 			logger.severe("properties file unreadable. exiting.");
 			System.exit(-1);
 		}
 
 		/* prepare to create the ProjectBoxes */
 		logger.fine("creating Project Boxes");
 		// int numOfManagerSpecializations = Integer.parseInt(configTxt
 		// .getProperty("numOfManagerSpecialities")); // Redundant??
 		// int numOfWorkerSpecialties = Integer.parseInt(configTxt
 		// .getProperty("numOfWorkerSpeciality")); // Redundant??
 		String[] specializations = Init.parseStrArr(configTxt,
 				"ManagerSpecialties");
 		String[] specialties = Init.parseStrArr(configTxt, "workerSpecialties");
 
 		/* create ManagerSpecializations and ProjectBoxes */
 		for (String specialization : specializations) {
 			ManagerSpecialization managerSpecialization = new ManagerSpecialization(
 					specialization);
 			managerSpecializations.add(specialization);
 			ProjectBox projectBox = new ProjectBoxImpl(managerSpecialization);
 			initMap.put(managerSpecialization.getSpecialization(), projectBox);
 		}
 		/* create the resources */
 		int numOfResources = Integer.parseInt(configTxt
 				.getProperty("numOfResources"));
 		for (int i = 0; i < numOfResources; i++) {
 			String si = String.valueOf(i);
 			Resource resource = new Resource(configTxt.getProperty("resource"
 					+ si + "Name"), Integer.parseInt(configTxt
 					.getProperty("resource" + si + "Amount")));
 			resources.add(resource);
 			logger.fine("Resource " + resource.getName() + " added. amout: "
 					+ resource.getAmount());
 		}
 
 		/* create managers */
 		int numOfMangers = Integer.parseInt(configTxt
 				.getProperty("numOfManagers"));
 		for (int i = 0; i < numOfMangers; i++) {
 			String si = String.valueOf(i);
 			Manager manager = new Manager(configTxt.getProperty("manager" + si
 					+ "Name"), new ManagerSpecialization(configTxt
 					.getProperty("manager" + si + "Specialty")), managerBoard,
 					completedProjects, executingProjects);
 			manager.setLogger(logger);
 			manager.setWorkingBoard(workingBoard);
 			logger.info(manager.getName() + " started working at "
 					+ Helpers.staticTimeNow());
 			managers.put(manager.getName(), manager);
 		}
 
 		/* create workers */
 		for (String specialty : specialties) {
 			WorkerSpecialty workerSpecialty = new WorkerSpecialty(specialty);
 			workerSpecialties.add(workerSpecialty);
 		}
 		int numOfWorkers = Integer.parseInt(configTxt
 				.getProperty("numOfWorkers"));
 		for (int i = 0; i < numOfWorkers; i++) {
 			String si = String.valueOf(i);
 			Worker worker = new Worker(configTxt.getProperty("worker" + si
 					+ "Name"), Integer.parseInt(configTxt.getProperty("worker"
 					+ si + "WorkHours")), Init.arr2spec(configTxt.getProperty(
 					"worker" + si + "specialties").replaceAll(" ", "").split(
 					",")), workingBoard, warehouse);
 			worker.setLogger(logger);
 			logger.info(worker.getName() + " started working at "
 					+ Helpers.staticTimeNow());
 			workers.add(worker);
 		}
 
 		/* create the projects */
 		int numOfProjects = Integer.parseInt(projectsTxt
 				.getProperty("numOfProjects"));
 		for (int i = 0; i < numOfProjects; i++) {
 			String si = String.valueOf(i);
 			List<Task> taskList = new Vector<Task>();
 			String pIname = projectsTxt.getProperty("project" + si + "Name");
 			int pNumOfTasks = Integer.parseInt(projectsTxt.getProperty("p" + si
 					+ "NumOfTasks"));
 			for (int j = 0; j < pNumOfTasks; j++) {
 				String ts = "Task" + String.valueOf(j);
 				Task task = new TaskImpl(String.valueOf(j),
 						new ManagerSpecialization(projectsTxt.getProperty("p"
 								+ si + ts + "ManagerSpecialty")),
 						new WorkerSpecialty(projectsTxt.getProperty("p" + si
 								+ ts + "WorkerSpecialty")), Integer
 								.parseInt(projectsTxt.getProperty("p" + si + ts
 										+ "Time")), Init.arr2res(projectsTxt
 								.getProperty("p" + si + ts + "Tools")
 								.replaceAll(" ", "").split(",")));
 				taskList.add(task);
 			}
 			ProjectImpl project = new ProjectImpl(pIname, taskList);
 			projects.put(project.getName(), project);
 		}
 
 		/* populate the boards */
 		for (Resource resource : resources) {
 			warehouse.addResource(resource);
 		}
 		managerBoard.createProjectsMap(initMap);
 		for (ProjectImpl project : projects.values()) {
 			ManagerSpecialization ms = project.getNextManagerSpecializtion();
 			if (!managerSpecializations.contains(ms.getSpecialization())) {
 				logger.severe(ms.getSpecialization()
 						+ " is not a known specialization");
 			} else {
 				managerBoard.getProjectBox(
 						project.getNextManagerSpecializtion()).addProject(
 						project);
 				logger.fine("project "
 						+ managerBoard.getProjectBox(
 								project.getNextManagerSpecializtion())
 								.toString() + "created.");
 			}
 		}
 		/* create the observer */
 		Repl observer = new Repl(executingProjects, managerBoard,
 				completedProjects, projects, warehouse);
 		observer.setLogger(logger);
 		observer.setManagers(managers);
 		observer.setWorkers(workers);
 
 		ExecutorService mangersExecutorService = Executors
 				.newCachedThreadPool();
 		ExecutorService workersExecutorService = Executors
 				.newCachedThreadPool();
 
 		observer.setManagersExecutorService(mangersExecutorService);
 		observer.setWorkersExecutorService(workersExecutorService);
 
 		for (Manager manager : managers.values()) {
 			mangersExecutorService.execute(manager);
 		}
 
 		for (Worker worker : workers) {
 			workersExecutorService.execute(worker);
 		}
 
 		observer.start();
 
 	}
 
 	private static String[] parseStrArr(Properties configTxt, String string) {
 		return configTxt.getProperty("ManagerSpecialties").replaceAll(" ", "")
 				.split(",");
 	}
 
 	private static List<Resource> arr2res(String[] arr) {
 		Vector<Resource> vec = new Vector<Resource>();
 		for (String s : arr) {
 			vec.add(new Resource(s));
 		}
 		return vec;
 	}
 
 	private static List<WorkerSpecialty> arr2spec(String[] arr) {
 		Vector<WorkerSpecialty> vector = new Vector<WorkerSpecialty>();
 		for (String specialty : arr) {
 			vector.add(new WorkerSpecialty(specialty));
 		}
 		return vector;
 	}
 
 }
