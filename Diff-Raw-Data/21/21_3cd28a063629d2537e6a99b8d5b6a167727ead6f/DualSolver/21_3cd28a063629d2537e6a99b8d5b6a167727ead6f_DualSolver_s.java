 package solvers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.plaf.ListUI;
 
 import org.apache.commons.collections.CollectionUtils;
 
 import parser.DimacsParser;
 import representation.Clause;
 import representation.Quantum;
 import representation.QuantumTable;
 import representation.SearchState;
 
 public class DualSolver implements Solver {
 
 	private List<Clause> cnfClauses;
 	
 	public boolean isSatisfiable(List<Clause> clauses){
 		List<SearchState> finalStates = calculateFinalStates(clauses, true);
 		return !finalStates.isEmpty();
 	}
 	
 	@Override
 	public List<Clause> toMinimalDualClauses(List<Clause> clauses) {
 		List<Clause> minimalDualClauses = new ArrayList<Clause>();
 
 		List<SearchState> finalStates = calculateFinalStates(clauses, false);
 		for(SearchState state : finalStates){
 			List<Integer> literals = new ArrayList<Integer>();
 			Set<Quantum> quantums = state.getQuantums();
 			for(Quantum quantum: quantums){
 				literals.add(quantum.getLiteral());
 			}
 			
 			minimalDualClauses.add(new Clause(literals));
 		}
 		
 		return minimalDualClauses;
 		
 	}
 	
 	public List<SearchState> calculateFinalStates(List<Clause> clauses, boolean returnFirst) {
 		
 		this.cnfClauses = clauses;
 
 		long loops = 0;
 		long begin = System.currentTimeMillis();
 		long loopsFirst = 0;
 		long timeFirst = 0;
 		
 		QuantumTable quantumTable = buildQuantumTable();
 		
 		List<SearchState> openedStates = calculateInitialOpenedStates(quantumTable);
 		List<SearchState> closedStates = new ArrayList<SearchState>();
 		List<SearchState> finalStates = new ArrayList<SearchState>();
 		
 		while(!openedStates.isEmpty()){
 			loops++;
 			SearchState currentState = getStateWithSmallestGap(openedStates);
 
 			if(isFinalState(currentState)){
 				
 //				System.out.println("\n****** final ******\n");
 				
 				if(loopsFirst == 0){
 					loopsFirst = loops;
 					timeFirst = System.currentTimeMillis();
 				}
 				
 				openedStates.remove(currentState);
 				finalStates.add(currentState);
 				if(returnFirst){
 					return finalStates;
 				}
 				continue;
 			}
 			
 			openedStates.remove(currentState);
 			closedStates.add(currentState);
 			
 			List<SearchState> neighbors = calculateNeighbors(currentState, quantumTable);
 			
 			for(SearchState state: neighbors){
 				openedStates.add(state);
 			}
 			
 		}
 		
 		long end = System.currentTimeMillis();
 		System.out.print((timeFirst-begin));
 		System.out.print(" | "+(loopsFirst));
 		System.out.print(" | "+(end-begin));
 		System.out.println(" | "+(loops));
 		
 		return finalStates;
 	}
 	
 	
 	
 	
 	private List<SearchState> calculateNeighbors(SearchState currentState, QuantumTable quantumTable) {
 		
 		// Sucessors function implemented from pg 25
 		
 		List<SearchState> sucessors = new ArrayList<SearchState>();
 		
 		//step 1
 		List<Quantum> possibleExtensions = determinePossibleExtensions(currentState, quantumTable);
 		
 		//step 2
 		sortQuantumsAccordingTOHeuristic(possibleExtensions, currentState);
 		
 		String tabs = "";
 		for(int i=1; i<currentState.getQuantums().size(); i++){
 			tabs += "\t";
 		}
 		
 //		System.out.print(tabs+"Selected: ");
 //		for(Quantum quantum: currentState.getQuantums()){
 //			System.out.print(quantum.getLiteral() + ", ");
 //		}
 //		System.out.println(tabs+"");
 //		
 //		System.out.print(tabs+"Gap: ");
 //		for(Clause clause: currentState.getGap()){
 //			for(int i=0; i<this.cnfClauses.size();i++){
 //				if(clause.equals(this.cnfClauses.get(i))){
 //					System.out.print(i+", ");
 //				}
 //			}
 //		}
 //		System.out.println(tabs+"");
 //		
 //		System.out.print(tabs+"Possible extensions: ");
 //		for(Quantum quantum: possibleExtensions){
 //			System.out.print(quantum.getLiteral() + ", ");
 //		}
 //		System.out.println(tabs+"");
 //		
 //		System.out.print(tabs+"Forbidden quanta: ");
 //		for(Quantum quantum: currentState.getForbiddenQuantums()){
 //			System.out.print(quantum.getLiteral() + ", ");
 //		}
 //		System.out.println(tabs+"");
 		
 		//step 3
 		List<Clause> gapConditions = gapConditions(currentState, quantumTable);
 		for(Clause clause: gapConditions){
 			
 			if(!intersects(clause, possibleExtensions)){
 //				System.out.println(tabs+"------");
 				return new ArrayList<SearchState>();
 			}
 		}
 		
 		List<Quantum> usedQuantums = new ArrayList<Quantum>();
 		List<Quantum> refused = new ArrayList<Quantum>();
 		//step 4
 		for(Quantum quantum: possibleExtensions){
 			
 			SearchState possibleNextState = new SearchState(currentState);
 			possibleNextState.addQuantum(quantum);
			possibleNextState.addForbiddenQuantum(quantumTable.getQuantum(quantum.getLiteral() * -1));
 			removeFromGapClausesOfQuantum(possibleNextState, quantum);
 			
 			List<Clause> possibleNextStateGapConditions = gapConditions(possibleNextState, quantumTable);
 			
 			if(isExclusiveCoordinateCompatible(currentState, quantum) 
 //					&& isNewRestrictionsContradictory(possibleNextState, quantumTable) 
 //					&& isNewRestrictionsCompatibleWithForbiddenList(possibleNextState, quantumTable, currentState)
 					&& isGapConditionsOk(possibleNextStateGapConditions, currentState)){
 				
 				for(Quantum forbiddenQuantum:usedQuantums){
 					possibleNextState.addForbiddenQuantum(forbiddenQuantum);
 				}			
 				
 				usedQuantums.add(quantum);
 				
 				sucessors.add(possibleNextState);
 				
 			}else{
 				refused.add(quantum);
 			}
 			
 		}
 		
 		for(SearchState sucessor: sucessors){
 			for(Quantum quantum: refused){
 				sucessor.addForbiddenQuantum(quantum);
 			}
 		}
 		
 //		System.out.print(tabs+"used: ");
 //		for(Quantum quantum: usedQuantums){
 //			System.out.print(quantum.getLiteral() + ", ");
 //		}
 //		System.out.println(tabs+"");
 //		
 //		System.out.print(tabs+"refused: ");
 //		for(Quantum quantum: refused){
 //			System.out.print(quantum.getLiteral() + ", ");
 //		}
 //		System.out.println(tabs+"");
 		
 		List<SearchState> sucessorsWithFuture = new ArrayList<SearchState>();
 		for(SearchState sucessor: sucessors){
 			List<Clause> gapConditionsSucessor = gapConditions(sucessor, quantumTable);
 			if(haveFuture(gapConditionsSucessor, sucessor)){
 				sucessorsWithFuture.add(sucessor);
 			} else {
 //				System.out.print(tabs+"future less: ");
 //
 //				for (Quantum quantumFerrado : sucessor.getQuantums()) {
 //					System.out.print(quantumFerrado.getLiteral() + ", ");
 //				}
 //				System.out.println(tabs+"");
 			}
 		}
 		
 		
 //		System.out.println(tabs+"-------");
 		return sucessorsWithFuture;
 	}
 
 	private boolean haveFuture(List<Clause> nextStateGapConditions, SearchState state){
 		
 		for(Clause clause: nextStateGapConditions){
 			if(areAllLiteralsInQuantums(clause.getLiterals(), state.getForbiddenQuantums())) return false;
 		}
 		
 		return true;
 	}
 	
 	
 	private boolean isGapConditionsOk(List<Clause> nextStateGapConditions , SearchState currentState){
 		
 		Set<Quantum> forbiddenQuantums = currentState.getForbiddenQuantums();
 		
 		for(Clause clause: nextStateGapConditions){
 			
 			if(clause.isEmpty()) return false;
 			
 			if(clause.isUnit()){
 				for(Clause otherClause: nextStateGapConditions){
 					if(otherClause.isUnit() && otherClause.getLiterals().get(0).equals(clause.getLiterals().get(0) * -1)){
 						return false;
 					}
 				}
 			}
 			
 			if(areAllLiteralsInQuantums(clause.getLiterals(), forbiddenQuantums)){
 				return false;
 			}
 			
 		}
 		
 		return true;
 		
 	}
 	
 	private void removeFromGapClausesOfQuantum(SearchState possibleNextState, Quantum quantum) {
 		
 		List<Clause> gap = possibleNextState.getGap();
 		
 		for(Integer coordinate: quantum.getCoordinates()){
 			gap.remove(this.cnfClauses.get(coordinate));
 		}
 		
 		possibleNextState.setGap(gap);
 		
 		
 	}
 
 	private boolean isNewRestrictionsCompatibleWithForbiddenList(SearchState possibleNextState, QuantumTable quantumTable, SearchState currentState) {
 		
 		Set<Quantum> forbiddenQuantums = currentState.getForbiddenQuantums();
 		List<Clause> gapConditions = gapConditions(possibleNextState, quantumTable);
 		for(Clause clause: gapConditions){
 			if(areAllLiteralsInQuantums(clause.getLiterals(), forbiddenQuantums)){
 				return false;
 			}
 		}
 		
 		return true;
 		
 	}
 
 	private boolean areAllLiteralsInQuantums(List<Integer> literals, Set<Quantum> forbiddenQuantums) {
 		
 		for(Integer literal:literals){
 			if(!containsLiteral(forbiddenQuantums, literal)){
 				return false;
 			}
 		}
 		
 		return true;
 	}
 
	private boolean containsLiteral(Set<Quantum> forbiddenQuantums,	Integer literal) {
		
		for(Quantum quantum: forbiddenQuantums){
			if(quantum.getLiteral().equals(literal)) return true;
 		}
 		return false;
		
 	}
 
 	private boolean isNewRestrictionsContradictory(SearchState possibleNextState, QuantumTable quantumTable) {
 		List<Clause> gapConditions = gapConditions(possibleNextState, quantumTable);
 		for(Clause clause: gapConditions){
 			if(clause.isEmpty()) return false;
 		}
 		
 		return true;
 	}
 
 	private boolean isExclusiveCoordinateCompatible(SearchState currentState, Quantum quantumBeingAdded) {
 		
 		Set<Integer> quantumBeingAddedCoordinates = quantumBeingAdded.getCoordinates();
 		
 		for(Quantum quantum: currentState.getQuantums()){
 			
 			Set<Integer> coordinates = getExclusiveCoordinatesFor(currentState, quantum);
 			if(quantumBeingAddedCoordinates.containsAll(coordinates)){
 				return false;
 			}
 			
 		}
 		
 		return true;
 	}
 
 
 	private Set<Integer> getExclusiveCoordinatesFor(SearchState currentState, Quantum quantum) {
 		
 		Quantum copy = new Quantum(quantum);
 		Set<Integer> coordinates =  copy.getCoordinates();
 		
 		for(Quantum currentStateQuantum: currentState.getQuantums()){
 			if(currentStateQuantum.equals(quantum)){
 				continue;
 			}
 			coordinates.removeAll(currentStateQuantum.getCoordinates());
 		}
 		
 		return coordinates;
 	}
 
 	private List<Clause> gapConditions(SearchState state,QuantumTable quantumTable){
 		
 		List<Clause> gapConditions = new ArrayList<Clause>();
 		
 		Set<Quantum> mirrorQuantums = calculateMirror(state.getQuantums(), quantumTable); 
 		List<Clause> gap = state.getGap();
 		
 		for(Clause clause: gap){
 			if(!intersects(clause, mirrorQuantums)) continue;
 			
 			Clause clone = new Clause(clause);
 			removeLiteralsOfQuantumsFromClause(mirrorQuantums, clone);
 			gapConditions.add(clone);
 			
 		}
 		
 		return gapConditions;
 	}
 	
 	private void removeLiteralsOfQuantumsFromClause(Set<Quantum> mirrorQuantums, Clause clause) {
 		for(Quantum quantum: mirrorQuantums){
 			clause.removeLiteral(quantum.getLiteral());
 		}
 	}
 
 	private boolean intersects(Clause clause, Collection<Quantum> quantums) {
 		
 		for(Integer literal: clause.getLiterals()){
 			for(Quantum quantum: quantums){
 				if (quantum.getLiteral().equals(literal)) return true;
 			}
 		}
 		
 		return false;
 	}
 
 	private Set<Quantum> calculateMirror(Set<Quantum> quantums, QuantumTable quantumTable) {
 		
 		Set<Quantum> mirror = new HashSet<Quantum>();
 		for(Quantum quantum: quantums){
 			Integer mirrorLiteral = quantum.getLiteral() * -1;
 			Quantum mirrorQuantum = quantumTable.getQuantum(mirrorLiteral);
 			if(mirrorQuantum != null){
 				mirror.add(mirrorQuantum);
 			}
 		}
 		
 		return mirror;
 		
 	}
 
 	private List<Quantum> determinePossibleExtensions(SearchState currentState, QuantumTable quantumTable) {
 		
 		Set<Integer> literalsInTheClausesOfGap = getLiteralsFromGapOf(currentState);
 
 		List<Quantum> possibleExtensions = new ArrayList<Quantum>();
 		for(Integer literal: literalsInTheClausesOfGap){
 			possibleExtensions.add(quantumTable.getQuantum(literal));
 		}
 		
 		removeForbiddenQuantums(possibleExtensions, currentState);
 		
 		return possibleExtensions;
 	}
 
 	private void removeForbiddenQuantums(List<Quantum> possibleExtensions, SearchState currentState) {
 		Set<Quantum> forbiddenQuantums = currentState.getForbiddenQuantums();
 		for(Quantum forbiddenQuantum: forbiddenQuantums){
 			possibleExtensions.remove(forbiddenQuantum);
 		}
 	}
 
 	private Set<Integer> getLiteralsFromGapOf(SearchState currentState) {
 		List<Clause> gap = currentState.getGap();
 
 		Set<Integer> literalsInTheClausesOfGap = new HashSet<Integer>();
 		for(Clause clause: gap){
 			List<Integer> literals = clause.getLiterals();
 			literalsInTheClausesOfGap.addAll(literals);
 		}
 		
 		return literalsInTheClausesOfGap;
 	}
 
 	private boolean isFinalState(SearchState state) {
 		return state.getGap().isEmpty();
 	}
 
 	private SearchState getStateWithSmallestGap(List<SearchState> openedStates) {
 		
 		int betterStateIndex = 0;
 		List<Clause> minorGap = openedStates.get(0).getGap();
 		
 		for(int i=1; i < openedStates.size(); i++){
 			
 			SearchState state = openedStates.get(i);
 			List<Clause> gap = state.getGap();
 			
 			if(gap.size() < minorGap.size()){
 				minorGap = gap;
 				betterStateIndex = i;
 			}
 			
 		}
 		
 		return openedStates.get(betterStateIndex);
 		
 	}
 
 	private List<Clause> calculateGap(SearchState searchState) {
 
 		Set<Integer> coveredClauses = new HashSet<Integer>();
 		
 		for(Quantum quantum: searchState.getQuantums()){
 			for(Integer coordinate: quantum.getCoordinates()){
 				coveredClauses.add(coordinate);
 			}
 		}
 		
 		List<Clause> gap = new ArrayList<Clause>();
 		for(int i=0; i< cnfClauses.size(); i++){
 			if(!coveredClauses.contains(i)){
 				gap.add(cnfClauses.get(i));
 			}
 		}
 		
 		return gap;
 	
 	}
 
 	private List<SearchState> calculateInitialOpenedStates(QuantumTable quantumTable) {
 	
 		Clause clause = getBestCnfClauseToStart(quantumTable);
 		
 		List<Quantum> quantumsOfClause = new ArrayList<Quantum>();
 		List<Integer> literals = clause.getLiterals();
 		for(Integer literal: literals){
 			quantumsOfClause.add(quantumTable.getQuantum(literal));
 		}
 		
 //		quantumsOfClause = sortQuantumsAccordingTOHeuristic(quantumsOfClause, );
 		
 		List<SearchState> initialOpenedStates = new ArrayList<SearchState>();
 		
 		for(int i=0; i<quantumsOfClause.size(); i++){
 			SearchState state = new SearchState();
 			state.addQuantum(quantumsOfClause.get(i));
 			state.addForbiddenQuantum(quantumTable.getQuantum(quantumsOfClause.get(i).getLiteral() * -1));
 			
 			//TODO Im sure it can be done in a better way
 			for(int j=i-1; j>=0; j--){
 				state.addForbiddenQuantum(quantumsOfClause.get(j));
 			}
 			
 			List<Clause> calculatedGap = calculateGap(state);
 			state.setGap(calculatedGap);
 			
 			initialOpenedStates.add(state);
 		}
 		
 		return initialOpenedStates;
 		
 	}
 
 	private List<Quantum> sortQuantumsAccordingTOHeuristic(List<Quantum> quantumsOfClause, SearchState currentState) {
 		
 		List<Clause> gap = currentState.getGap();
 		List<Integer> gapCoordinates = getCoordinates(gap);
 		
 		for(int i=0; i<quantumsOfClause.size(); i++){
 			for(int j=i+1; j<quantumsOfClause.size(); j++){
 				
 				Quantum quantumI = quantumsOfClause.get(i);
 				Quantum quantumJ = quantumsOfClause.get(j);
 				
 				Set<Integer> coordinatesInterGapI = new HashSet<Integer>(quantumI.getCoordinates());
 				coordinatesInterGapI.retainAll(gapCoordinates);
 				
 				Set<Integer> coordinatesInterGapJ = new HashSet<Integer>(quantumJ.getCoordinates());
 				coordinatesInterGapJ.retainAll(gapCoordinates);
 				
 				Set<Integer> coordinatesInterGapIJ = new HashSet<Integer>(coordinatesInterGapI);
 				coordinatesInterGapIJ.retainAll(coordinatesInterGapJ);
 				
 				Collection coordinatesIMinusIJ = CollectionUtils.subtract(coordinatesInterGapI, coordinatesInterGapIJ);
 				Collection coordinatesJMinusIJ = CollectionUtils.subtract(coordinatesInterGapJ, coordinatesInterGapIJ);
 				
 				if(!(coordinatesIMinusIJ.size() > coordinatesJMinusIJ.size())){
 					
 					quantumsOfClause.set(i, quantumJ);
 					quantumsOfClause.set(j, quantumI);
 					
 				}
 				
 			}
 		}
 		
 		return quantumsOfClause;
 	}
 
 	private List<Integer> getCoordinates(List<Clause> clauses) {
 		
 		List<Integer> coordinates = new ArrayList<Integer>();
 		
 		for(int i=0; i<this.cnfClauses.size(); i++){
 			if(clauses.contains(cnfClauses.get(i))){
 				coordinates.add(new Integer(i));
 			}
 		}
 		
 		return coordinates;
 		
 	}
 
 	private Clause getBestCnfClauseToStart(QuantumTable quantumTable) {
 
 		//Trying heuristic to determine the first clause to be used, i.e. the initial state (pg 25)
 		
 		Map<Integer, Set<Integer>> clausesCoverage = new HashMap<Integer, Set<Integer>>();
 		
 		for(int i=0; i< cnfClauses.size(); i++){
 			Set<Integer> coverage = new HashSet<Integer>();
 
 			Clause clause = cnfClauses.get(i);
 			List<Integer> literals = clause.getLiterals();
 			for(Integer literal: literals){
 				Quantum quantum = quantumTable.getQuantum(literal);
 				
 				for(Integer coordinate: quantum.getCoordinates()){
 					coverage.add(coordinate);
 				}
 			}
 			clausesCoverage.put(i, coverage);
 		}
 		
 		int maxCoverage = clausesCoverage.get(0).size();
 		int bestClauseIndex = 0;
 		for(int i=0; i< cnfClauses.size(); i++){
 			
 			if(clausesCoverage.get(i).size() > maxCoverage){
 				maxCoverage = clausesCoverage.get(i).size();
 				bestClauseIndex = i;
 			}
 			
 		}
 		
 		return cnfClauses.get(bestClauseIndex);
 	}
 
 	private QuantumTable buildQuantumTable(){
 		
 		QuantumTable table = new QuantumTable();
 		
 		for(int i=0;i<cnfClauses.size();i++){
 			
 			Clause clause = cnfClauses.get(i);
 			
 			List<Integer> literals = clause.getLiterals();
 			for(Integer literal:literals){
 
 				table.addCoordinate(literal, i);
 				
 			}
 			
 		}
 		
 		return table;
 		
 	}
 	
 	// -------------------------------------------------------------
 	// END OF AUXILIAR METHOD ON DEBUGGING
 	// -------------------------------------------------------------
 	
 	public static void main(String[] args) throws IOException {
 	
 		DimacsParser parser = new DimacsParser();
 		
		List<Clause> clauses = parser.parse("/home/murillo/Dropbox/tcc/satlib/uf50-218/uf50-0112.cnf");
 //		List<Clause> expectedAnswer = parser.parse("examples/t3.dnf");
 		
 		DualSolver solver =  new DualSolver();
 //		System.out.println(solver.isSatisfiable(clauses));
 		List<Clause> minimalDualClauses = solver.toMinimalDualClauses(clauses);
 		System.out.println("Size: "+minimalDualClauses.size());
 		System.out.println(minimalDualClauses);
 //		compararSolucaoResposta(expectedAnswer, minimalDualClauses);		
 		
 	}
 
 	private static void compararSolucaoResposta(List<Clause> expectedAnser, List<Clause> minimalDualClauses) {
 		
 		for(Clause clause: minimalDualClauses){
 			
 			if(isIn(clause, expectedAnser)){
 				System.out.println("Y + " + clause);
 			}else{
 				System.out.println("N + " + clause);
 			}
 				
 			
 		}
 		
 	}
 
 	private static boolean isIn(Clause clause, List<Clause> expectedAnser) {
 		
 		for(Clause expected: expectedAnser){
 			
 			List<Integer> expecLiterals = expected.getLiterals();
 			List<Integer> literals = clause.getLiterals();
 			
 			if(expecLiterals.containsAll(literals) && expecLiterals.size() == literals.size()){
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
