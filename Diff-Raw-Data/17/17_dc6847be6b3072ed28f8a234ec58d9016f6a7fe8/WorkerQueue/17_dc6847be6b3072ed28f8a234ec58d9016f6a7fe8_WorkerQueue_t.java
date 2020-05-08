 package main;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.antlr.runtime.tree.CommonTree;
 
 import utils.DirtyLittleHelper;
 import workers.Worker;
 import database.Database;
 import database.Record;
 import database.Relation;
 import database.Scope;
 
 // TODO: replace all LinkedLists by "LinkedHashLists"
 
 public class WorkerQueue {
 	private boolean workerInitializationFinished;
 	private HashMap<Scope, LinkedList<Worker>> queues = new HashMap<Scope, LinkedList<Worker>>();
 	private HashMap<Scope, Relation> relations = new HashMap<Scope, Relation>();
 	private HashMap<Scope, Integer> currentIDs = new HashMap<Scope, Integer>();
 	private Database database;
 	
 	public WorkerQueue(Database database) {
 		this.database = database;
 		
 		for (Scope scope : Scope.getInstances()) {
 			queues.put(scope, new LinkedList<Worker>());
 			relations.put(scope, new Relation(database, scope));
 			currentIDs.put(scope, new Integer(0));
 		}
 	}
 	
 	public void addWorker(Worker worker) {
 		if (workerInitializationFinished) {
 			System.out.println("should not add workers anymore");
 			return;
 		}
 		
 		Scope scope = worker.getScope();
 		LinkedList<Worker> queue = queues.get(scope);
 		queue.add(worker);
 		relations.get(worker.getScope()).addAttribute(worker.getAttributeName());
 	}
 	
 	public void doWork(TreePackage projectPackage) {
 		workerInitializationFinished = true;
 		
 		process(projectPackage);
 		
 		Collection<TreePackage> treePackagesOfProject = 
 			(new TreePackageGenerator()).generateTreePackagesForProject(projectPackage.getFile());
 		
 		for (TreePackage treePackage : treePackagesOfProject){
 			traverse(treePackage);
 		}
 	}
 
 	private void traverse(TreePackage treePackage) {
 		List<CommonTree> children = DirtyLittleHelper.castList(CommonTree.class, treePackage.getTree().getChildren());
 		
 		for (CommonTree child : children) {
 			if (child.getText().equals("class")) {
 				process(new TreePackage(child, treePackage, Scope.CLASS));
 			}
 			
 			if (child.getText().matches(".*METHOD_DECL")) {
 				process(new TreePackage(child, treePackage, Scope.METHOD));
 
 				continue;
 			}
 
 			traverse(new TreePackage(child, treePackage, null));
 		}
 	}
 	
 	private void process(TreePackage treePackage) {
 		Scope scope = treePackage.getScope();
 		Record record = relations.get(scope).newRecord();
 		record.setID(newID(scope));
 		record.setParentID(currentIDs.get(scope.getParent()));
 		
 		// TODO: remove if when project has own treePackage
 		if (treePackage != null) {
 			record.setName(treePackage.getName());
 			
 			for (Worker worker : queues.get(scope)) {
 				record.setValueForAttribute(worker.doWork(treePackage), worker.getAttributeName());
 			}
 		}
 			
 		relations.get(scope).add(record);
 	}
 
 	private int newID(Scope scope) {
 		int currentID = currentIDs.get(scope);
 		++currentID;
 		currentIDs.put(scope, currentID);
 		return currentID;
 	}
 
 	public void createTables() {
 		for (Relation relation : relations.values()) {
 			relation.createTable();
 		}
 	}
 
 	public void createViews() {
 		createMethodView();
 		createClassView();
 	}
 
 	private void createMethodView() {
 		String attributes = "";
 
 		LinkedList<Worker> workers = queues.get(Scope.METHOD);
 		for (int i = 0; i < workers.size(); ++i) {
 			attributes += workers.get(i).getAttributeName();
 			if (i < workers.size() - 1) {
 				attributes += ", ";
 			}
 		}
 		
 		String query = "CREATE VIEW method_view AS SELECT " + attributes + " FROM method";
 		
 		try {
 			database.update(query);
 		} catch (Exception exception) {
 			// view existed already
 		}
 		
 		database.query("SELECT * FROM method_view");
 	}
 	
 	private void createClassView() {
 		String attributes = "";
 
 		LinkedList<Worker> workers = queues.get(Scope.CLASS);
 		for (int i = 0; i < workers.size(); ++i) {
 			attributes += workers.get(i).getAttributeName();
 			if (i < workers.size() - 1) {
 				attributes += ", ";
 			}
 		}
 		
 		String query = "CREATE VIEW class_view AS SELECT " + attributes + " FROM class";
 		
 		try {
 			database.update(query);
 		} catch (Exception exception) {
 			// view existed already
 		}
 	}
 }
