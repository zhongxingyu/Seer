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
 import acitiveobjects.Manager;
 import acitiveobjects.Observer;
 import acitiveobjects.Worker;
 
 /**
  * @author lxmonk
  * 
  */
 public class Init {
 
 	/**
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
 		logger.addHandler(fh);
 
 		// Set the log level specifying which message levels will be logged by
 		// this logger
 		logger.setLevel(Level.ALL); // TODO: update this.
 		/* start the logger */
 		logger.info("logger started.");
 		/* create the Boards */
 		ManagerBoard managerBoard = new ManagerBoardImpl();
 		logger.info("ManagerBoardImpl created");
 		WorkingBoard workingBoard = new WorkingBoardImpl();
 		Warehouse warehouse = new WarehouseImpl();
 		List<Project> completedProjects = new Vector<Project>();
 		/* create temporary data structures */
 		// Map<String, ManagerSpecialization> managerSpecializations = new
 		// HashMap<
 		// String, ManagerSpecialization>();
 		Vector<String> managerSpecializations = new Vector<String>();
 		Set<WorkerSpecialty> workerSpecialties = new HashSet<WorkerSpecialty>();
 		Set<Resource> resources = new HashSet<Resource>();
 		List<ProjectImpl> projects = new Vector<ProjectImpl>();
 		Map<Project, Project> executingProjects = new ConcurrentHashMap<Project, Project>();
 		Map<String, ProjectBox> initMap = new HashMap<String, ProjectBox>();
 		List<Manager> managers = new Vector<Manager>();
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
 
 		// config.parse(args[?])
 		// Init.parse(configTxt, logger); // ???
 
 		/* prepare to create the ProjectBoxes */
 		logger.info("creating Project Boxes");
 		int numOfManagerSpecializations = Integer.parseInt(configTxt
 				.getProperty("numOfManagerSpecialities")); // Redundant??
 		int numOfWorkerSpecialties = Integer.parseInt(configTxt
 				.getProperty("numOfWorkerSpeciality")); // Redundant??
		String[] specializations = configTxt.getProperty("ManagerSpecialties")
 				.split(",");
		String[] specialties = configTxt.getProperty("workerSpecialties")
 				.split(",");
 
 		/* create ManagerSpecializations and ProjectBoxes */
 		for (String specialization : specializations) {
 			ManagerSpecialization managerSpecialization = new ManagerSpecialization(
 					specialization);
			managerSpecializations.add(managerSpecialization.getSpecialization());
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
 		}
 
 		/* create managers */
 		int numOfMangers = Integer.parseInt(configTxt
 				.getProperty("numOfManagers"));
 		for (int i = 0; i < numOfMangers; i++) {
 			String si = String.valueOf(i);
 			Manager manager = new Manager(configTxt.getProperty("manager" + si
 					+ "Name"), new ManagerSpecialization(configTxt
 					.getProperty("manager" + si + "Specialty")), managerBoard,
 					workingBoard, completedProjects, executingProjects, logger);
 			managers.add(manager);
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
 					"worker" + si + "specialties").split(",")), logger);
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
 				Task task = new TaskImpl(new ManagerSpecialization(projectsTxt
 						.getProperty("p" + si + ts + "ManagerSpecialty")),
 						new WorkerSpecialty(projectsTxt.getProperty("p" + si
 								+ ts + "WorkerSpecialty")), Integer
 								.parseInt(projectsTxt.getProperty("p" + si + ts
 										+ "Time")), Init.arr2res(projectsTxt
 								.getProperty("p" + si + ts + "Tools")
 								.split(",")));
 				taskList.add(task);
 			}
 			ProjectImpl project = new ProjectImpl(pIname, taskList);
 			projects.add(project);
 		}
 
 		/* populate the boards */
 		for (Resource resource : resources) {
 			warehouse.addResource(resource);
 		}
 		managerBoard.createProjectsMap(initMap);
 		for (ProjectImpl project : projects) {
 			ManagerSpecialization ms = project.getNextManagerSpecializtion();
 			if (!managerSpecializations.contains(ms.getSpecialization())) {
				System.out.println(ms.getSpecialization()
 						+ " is not a known specialization");
 			} else {
			System.out.println(managerBoard.getProjectBox(project
 					.getNextManagerSpecializtion()));
 			managerBoard.getProjectBox(project.getNextManagerSpecializtion())
 					.addProject(project);
 			}
 		}
 		/* create the observer */
 		Observer observer = new Observer();
 
 		ExecutorService mangersExecutorService = Executors
 				.newCachedThreadPool();
 		ExecutorService workersExecutorService = Executors
 				.newCachedThreadPool();
 
 		observer.start();
 
 		for (Worker worker : workers)
 			workersExecutorService.execute(worker);
 
 		for (Manager manager : managers)
 			mangersExecutorService.execute(manager);
 
 	}
 
 	private static List<Resource> arr2res(String[] arr) {
 		Vector<Resource> vec = new Vector<Resource>();
 		for (String s : arr) {
 			vec.add(new Resource(s));
 		}
 		return vec;
 	}
 
 	private static Vector<WorkerSpecialty> arr2spec(String[] arr) {
 		Vector<WorkerSpecialty> vector = new Vector<WorkerSpecialty>();
 		for (String specialty : arr) {
 			vector.add(new WorkerSpecialty(specialty));
 		}
 		return vector;
 	}
 
 	private static void parse(Properties properties, Logger logger) {
 		// TODO Auto-generated method stub
 
 	}
 }
