 package pls.vrp;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import pls.PlsSolution;
 
 public class VrpPlsSolution implements PlsSolution {
 	private int maxIter;
 	private int maxEscalation;
 	private int relaxationRandomness;
 	private int maxDiscrepancies;
 	
 	private int curIter;
 	private int curEscalation;
 	
 	private int traceId;
 	
 	private VrpSolution sol;
 	
 	public VrpPlsSolution() {
 	}
 	
 	public VrpPlsSolution(VrpSolution sol, int maxIter, int maxEscalation, int relaxationRandomness, int maxDiscrepancies, 
 			int traceId) {
 		this.sol = sol;
 		this.maxIter = maxIter;
 		this.maxEscalation = maxEscalation;
 		this.maxDiscrepancies = maxDiscrepancies;
 		this.traceId = traceId;
 	}
 	
 	public int getCurEscalation() {
 		return curEscalation;
 	}
 	
 	public void setCurEscalation(int curEscalation) {
 		this.curEscalation = curEscalation;
 	}
 	
 	public int getMaxEscalation() {
 		return maxEscalation;
 	}
 	
 	public int getMaxDiscrepancies() {
 		return maxDiscrepancies;
 	}
 	
 	public int getCurIteration() {
 		return curIter;
 	}
 	
 	public void setCurIteration(int curIter) {
 		this.curIter = curIter;
 	}
 	
 	public int getMaxIterations() {
 		return maxIter;
 	}
 	
 	public int getRelaxationRandomness() {
 		return relaxationRandomness;
 	}
 	
 	public VrpSolution getSolution() {
 		return sol;
 	}
 	
 	public void setSolution(VrpSolution sol) {
 		this.sol = sol;
 	}
 	
 	public int getTraceId() {
 		return traceId;
 	}
 	
 	@Override
 	public void writeToStream(DataOutputStream dos) throws IOException {
		dos.writeDouble(sol.getToursCost());
 		dos.writeInt(traceId);
 		dos.writeInt(sol.getNumVehicles());
 		for (List<Integer> route : sol.getRoutes()) {
 			dos.writeInt(route.size());
 			for (int custId : route) {
 				dos.writeInt(custId);
 			}
 		}
 		
 		writeProblemToStream(sol.getProblem(), dos);
 		
 		dos.writeInt(maxIter);
 		dos.writeInt(curIter);
 		dos.writeInt(maxEscalation);
 		dos.writeInt(curEscalation);
 		dos.writeInt(relaxationRandomness);
 		dos.writeInt(maxDiscrepancies);
 	}
 	
 	@Override
 	public VrpPlsSolution buildFromStream(DataInputStream dis) throws IOException {
 		double toursCost = dis.readDouble();
 		traceId = dis.readInt();
 		int numVehicles = dis.readInt();
 		List<List<Integer>> routes = new ArrayList<List<Integer>>(numVehicles);
 		for (int i = 0; i < numVehicles; i++) {
 			int numCusts = dis.readInt();
 			List<Integer> route = new ArrayList<Integer>(numCusts);
 			routes.add(route);
 			for (int j = 0; j < numCusts; j++) {
 				route.add(dis.readInt());
 			}
 		}
 		VrpProblem problem = buildProblemFromStream(dis);
 		sol = new VrpSolution(routes, problem, toursCost);
 		maxIter = dis.readInt();
 		curIter = dis.readInt();
 		maxEscalation = dis.readInt();
 		curEscalation = dis.readInt();
 		relaxationRandomness = dis.readInt();
 		maxDiscrepancies = dis.readInt();
 		return this;
 	}
 
 	@Override
 	public int serializedSize() {
 		return 4 //cost
 			+ 4 //numVehicles
 			+ sol.getNumVehicles() * 4 //route sizes
 			+ sol.getProblem().getNumCities() * 4 //cust ids
 			+ sol.getProblem().getNumCities() * 4 * 6 //attrs
 			+ 4 * 4 //other problem attrs
 			+ 4 * 6; //curIter, maxIter, etc.
 	}
 
 	@Override
 	public int getCost() {
 		return (int)sol.getToursCost();
 	}
 	
 	private VrpProblem buildProblemFromStream(DataInputStream dis) throws IOException {
 		int numCities = dis.readInt();
 		int depotX = dis.readInt();
 		int depotY = dis.readInt();
 		int vehicleCapacity = dis.readInt();
 		int[] serviceTimes = new int[numCities];
 		int[] demands = new int[numCities];
 		int[] windowStartTimes = new int[numCities];
 		int[] windowEndTimes = new int[numCities];
 		int[] xCoors = new int[numCities];
 		int[] yCoors = new int[numCities];
 		for (int i = 0; i < numCities; i++) {
 			demands[i] = dis.readInt();
 			serviceTimes[i] = dis.readInt();
 			windowStartTimes[i] = dis.readInt();
 			windowEndTimes[i] = dis.readInt();
 			xCoors[i] = dis.readInt();
 			yCoors[i] = dis.readInt();
 		}
 		
 		return new VrpProblem(demands, xCoors, yCoors, serviceTimes, windowStartTimes, windowEndTimes,
 				depotX, depotY, vehicleCapacity);
 	}
 	
 	public void writeProblemToStream(VrpProblem problem, DataOutputStream dos) throws IOException {
 		dos.writeInt(problem.getDemands().length);
 		dos.writeInt(problem.getDepotX());
 		dos.writeInt(problem.getDepotY());
 		dos.writeInt(problem.getVehicleCapacity());
 		for (int i = 0; i < problem.getNumCities(); i++) {
 			dos.writeInt(problem.getDemands()[i]);
 			dos.writeInt(problem.getServiceTimes()[i]);
 			dos.writeInt(problem.getWindowStartTimes()[i]);
 			dos.writeInt(problem.getWindowEndTimes()[i]);
 			dos.writeInt(problem.getXCoors()[i]);
 			dos.writeInt(problem.getYCoors()[i]);
 		}
 	}
 
 }
