 package core.repository;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 import core.interfaces.Solver;
 import core.owl.objects.SolvingMethod;
 import core.utils.ConfigStorage;
 
 public class SolverRepository {
 
	private static final String SOLVER_REPO_CONFIG_PATH = "config/repositary/solver";
 	private final ConfigStorage storage = new ConfigStorage(SOLVER_REPO_CONFIG_PATH);
 	private final XStream xstream = new XStream(new DomDriver());
 	private final Map<String, Solver> solvers = new HashMap<String, Solver>();
 
 	public Solver getSolver(SolvingMethod solvingMethod) {
 		String solverClassName = solvingMethod.getSolverClassName();
 		return getSolver(solverClassName);
 	}
 
 	public Solver getSolver(String solverClassName) {
 		if (solvers.containsKey(solverClassName)) {
 			return solvers.get(solverClassName);
 		} else {
 			Solver newSolver;
 			newSolver = loadSolverFromStorage(solverClassName);
 			if (newSolver == null) {
 				newSolver = constructSolverByClassName(solverClassName);
 			}
 			solvers.put(solverClassName, newSolver);
 			return newSolver;
 		}
 	}
 
 	public MethodSignature getMethod(SolvingMethod solvingMethod) {
 		return this.getSolver(solvingMethod).getMethodBySolvingMethod(solvingMethod);
 	}
 
 	public Collection<Solver> getSolvers() {
 		return solvers.values();
 	}
 
 	public void addSolver(Solver solver) {
 		this.solvers.put(extractSolverName(solver), solver);
 	}
 
 	public void removeSolver(Solver solver) {
 		this.solvers.remove(extractSolverName(solver));
 	}
 
 	public void saveToStorage() {
 		for (Solver solver : solvers.values()) {
 			saveSolverToStorage(solver);
 		}
 	}
 
 	public void updateFromStorage() {
 		for (Solver solver : solvers.values()) {
 			Solver updatedSolver = loadSolverFromStorage(solver);
 			solver = (updatedSolver == null) ? solver : updatedSolver;
 		}
 	}
 
 	public List<String> getSolverListFromStorage() {
 		return storage.getStorageList();
 	}
 
 	private String extractSolverName(Solver solver) {
 		return solver.getClass().getName();
 	}
 
 	private Solver constructSolverByClassName(String solverClassName) {
 		try {
 			return (Solver) Class.forName(solverClassName).newInstance();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new AssertionError("Failed to resolve Solver by SolverClassName \"" + solverClassName + "\"");
 		}
 	}
 
 	private Solver loadSolverFromStorage(Solver solver) {
 		return loadSolverFromStorage(extractSolverName(solver));
 	}
 
 	private Solver loadSolverFromStorage(String solverClassName) {
 		try {
 			String xmlSerializtion = storage.readConfig(solverClassName);
 			return (Solver) xstream.fromXML(xmlSerializtion);
 		} catch (Exception e) {
 			System.err.println("SolverRepository: No config file found for solver " + solverClassName + " in storage");
 			return null;
 		}
 	}
 
 	private void saveSolverToStorage(Solver solver) {
 		String xmlSerialization = xstream.toXML(solver);
 		storage.writeConfig(extractSolverName(solver), xmlSerialization);
 	}
 }
