 package mapthatset.g6;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import mapthatset.sim.Guesser;
 import mapthatset.sim.GuesserAction;
 
 public class MastermindGuesser2 extends Guesser {
 	Map<Integer, List<Integer>> possibilities;
 	Map<List<Integer>, List<Integer>> rules;
 	
 	int mapLength;
 	
 	ArrayList<Integer> currentGuess;
 	ArrayList<Integer> randIndex;
 	ArrayList<ArrayList<Integer>> leftTodo;
 	ArrayList<Integer> leftToExplore;
 	String strID = "Mastermind";
 
 	/**
 	 * Initializes a new mapping.
 	 */
 	@Override
 	public void startNewMapping(int mapLength) {
 		this.mapLength = mapLength;
 		this.randIndex = getRandomIndex(mapLength);
 		
 		leftToExplore = new ArrayList<Integer>();
 		for(int i = 1 ; i <= mapLength; i++)
 			leftToExplore.add(i);
 	
 		possibilities = new HashMap<Integer, List<Integer>>();
 		for (int i = 1; i <= mapLength; i++)
 			possibilities.put(i, (ArrayList<Integer>) leftToExplore.clone());
 		
 		rules = new HashMap<List<Integer>, List<Integer>>();
 		
 		leftTodo = new ArrayList<ArrayList<Integer>>();
 		
 		leftTodo.add((ArrayList<Integer>) leftToExplore.clone());
 		
 		currentGuess = null;
 	}
 
 	/**
 	 * Gives us our next action.
 	 */
 	@Override
 	public GuesserAction nextAction() {
 		String todo = "q";
 		if (!solutionReached()) {
 			if(!leftTodo.isEmpty()){
 				currentGuess = (leftTodo.remove(0));
 				while(!isNewRule(currentGuess) && !leftTodo.isEmpty())
 					currentGuess = (leftTodo.remove(0));
 			}
 			else
 				System.exit(1);
 		}
 		else {
 			todo = "g";
 			currentGuess = (ArrayList<Integer>) makeGuess();
 		}
 		
 		System.out.println();
 
 		return new GuesserAction(todo, currentGuess);
 	}
 	
 	public static void main(String args[]){
 		MastermindGuesser2 mmg2 = new MastermindGuesser2();
 		mmg2.testSolutionReached();
 		mmg2.testGetRandomIndex();
 		mmg2.testLimitPossibilities1();
 		mmg2.testLimitPossibilities2();
 
 		//System.out.println("Helper methods work.");
 	}
 
 	public ArrayList<ArrayList<Integer>> trySplitting(List<Integer> toChooseFrom) {
 		ArrayList<Integer> t1 = new ArrayList<Integer>();
 		ArrayList<Integer> t2 = new ArrayList<Integer>();
 		for(int i = 0; i < toChooseFrom.size(); i++)
 			if(possibilities.get(toChooseFrom.get(i)).size()!=1)
 				if(i < toChooseFrom.size()/2)
 					t1.add(toChooseFrom.get(i));
 				else
 					t2.add(toChooseFrom.get(i));
 		ArrayList<ArrayList<Integer>> toReturn = new ArrayList<ArrayList<Integer>>();
 		toReturn.add(t1);
 		toReturn.add(t2);
 		return toReturn;
 	}
 	
 	public boolean isNewRule(ArrayList<Integer> obj){
 		boolean b = false;
 		for(List<Integer> rule : rules.keySet())
 			b |= obj.containsAll(rule);
 		return !b;
 	}
 	
 	public boolean isNewRule(List<Integer> chooseFrom, int i, int j){
 		return isNewRule(getArr(chooseFrom, i, j));
 	}
 	
 	public ArrayList<Integer> getArr(List<Integer> chooseFrom, int i, int j){
 		ArrayList<Integer> t = new ArrayList<Integer>();
 		t.add(chooseFrom.get(i));
 		t.add(chooseFrom.get(j));
 		return t;
 	}
 	
 	
 	public ArrayList<ArrayList<Integer>> makeSmallGuess(List<Integer> toChooseFrom){
 		int j = 0;
 		int n = toChooseFrom.size();
 		int k = 0;
 		ArrayList<ArrayList<Integer>> toReturn = new ArrayList<ArrayList<Integer>>();
 		while(k<n){
 			while(j < n && possibilities.get(toChooseFrom.get(j)).size() == 1) j++;
 			k = j+1;
			while(k < n && (possibilities.get(toChooseFrom.get(j)).size()==1 || !isNewRule(toChooseFrom, j, k))) k++;
 			if(k<n) toReturn.add(getArr(toChooseFrom,j,k));
 			else if(j < n){
 				ArrayList<Integer> t = new ArrayList<Integer>();
 				t.add(j);
 				toReturn.add(t);
 			}
			j = k+1;
 		}
 		return toReturn;
 	}
 	
 
 	public ArrayList<Integer> makeGuess() {
 		ArrayList<Integer> toReturn = new ArrayList<Integer>();
 		for(Integer i : possibilities.keySet())
 			toReturn.add(0);
 		for(Integer i : possibilities.keySet())
 			toReturn.set(i-1,possibilities.get(i).get(0));
 		return toReturn;
 	}
 	
 	public boolean solutionReached(Map<Integer, List<Integer>> possibilities){
 		boolean toReturn = true;
 		for(Integer i : possibilities.keySet())
 			toReturn &= possibilities.get(i).size()==1;
 		return toReturn;
 	}
 	
 	public boolean solutionReached(){
 		return solutionReached(possibilities);
 	}
 	
 	public void testSolutionReached(){
 		int n = 10;
 		Map<Integer, List<Integer>> tmp = new HashMap<Integer, List<Integer>>();
 		for(int i = 1; i<= n; i++)
 		{
 			tmp.put(i, new ArrayList<Integer>());
 			tmp.get(i).add(i);
 		}
 		assert solutionReached(tmp);
 	}
 
 
 	public String getID() {
 		return strID;
 	}
 
 	public static ArrayList<Integer> getRandomIndex(int n) {
 		ArrayList<Integer> start = new ArrayList<Integer>();
 		ArrayList<Integer> end = new ArrayList<Integer>();
 		Random r = new Random();
 		int i = 1;
 		while(start.size() < n)
 			start.add(i++);
 		while(start.size() > 0)
 			end.add(start.remove(r.nextInt(start.size())));
 		return end;
 	}
 	
 	public void testGetRandomIndex(){
 		int n = 10;
 		ArrayList<Integer> r = getRandomIndex(n);
 		assert r.size() == n;
 		for(int i = 1; i <= n; i++)
 			assert r.contains(i);
 	}
 
 	@Override
 	public void setResult(ArrayList<Integer> alResult) {
 		rules.put((List<Integer>) currentGuess.clone(), (List<Integer>) alResult.clone());
 	//	System.out.println(possibilities);
 
 		limitPossibilities(rules, possibilities);
 		
		if(currentGuess.size() == alResult.size()){
 			ArrayList<ArrayList<Integer>> perms = solvePermutation(currentGuess);
 			if(leftToExplore.containsAll(currentGuess)){
 				leftTodo.addAll(perms);
 				leftToExplore.removeAll(currentGuess);
 			}
 		}
		else{
 			leftTodo.addAll(makeSmallGuess(leftToExplore));
		}
 	}
 	
 	public void limitPossibilities(Map<List<Integer>, List<Integer>> mapping, Map<Integer, List<Integer>> poss){
 		for(List<Integer> r1 : mapping.keySet())
 			for(List<Integer> r2 : mapping.keySet())
 				if(!r1.equals(r2)){
 					List<Integer> g1 = new ArrayList<Integer>();
 					List<Integer> g2 = new ArrayList<Integer>();
 					g1.addAll(r1);
 					g1.retainAll(r2);
 					g2.addAll(mapping.get(r1));
 					g2.retainAll(mapping.get(r2));
 
 					if(g1.size()!= 0 && r1.size() - g1.size() == mapping.get(r1).size() - g2.size())
 						for(Integer choice : r1){
 							poss.get(choice).retainAll(mapping.get(r1));
 							if(g1.contains(choice))
 								poss.get(choice).retainAll(g2);
 							else
 								poss.get(choice).removeAll(g2);
 						}
 				}
 	}
 	
 	public void testLimitPossibilities1(){
 		HashMap<List<Integer>, List<Integer>> mapping = new HashMap<List<Integer>, List<Integer>>();
 		HashMap<Integer, List<Integer>> poss  = new HashMap<Integer, List<Integer>>();
 		ArrayList<Integer> tmp = new ArrayList<Integer>();
 		for(int i = 1 ; i <= 5; i++){
 			tmp.add(i);
 			if( i < 5 ){
 				ArrayList<Integer> t1 = new ArrayList<Integer>();
 				ArrayList<Integer> t2 = new ArrayList<Integer>();
 				t1.add(i);
 				t1.add(i+1);
 				t2.addAll(t1);
 				mapping.put(t1,t2);
 			}
 		}
 		for(Integer i : tmp)
 			poss.put(i, (List<Integer>) tmp.clone());
 		limitPossibilities(mapping, poss);
 		
 		for(Integer i : poss.keySet()){
 			assert(poss.get(i).size()==1);
 			assert(i == poss.get(i).get(0));
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void testLimitPossibilities2(){
 		HashMap<List<Integer>, List<Integer>> mapping = new HashMap<List<Integer>, List<Integer>>();
 		HashMap<Integer, List<Integer>> poss  = new HashMap<Integer, List<Integer>>();
 		ArrayList<Integer> tmp = new ArrayList<Integer>();
 		for(int i = 1; i<=200; i++)
 			tmp.add(i);
 		for(Integer i : tmp)
 			poss.put(i,  (List<Integer>) tmp.clone());
 		mapping.put((List<Integer>)tmp.clone(), (List<Integer>)tmp.clone());
 		ArrayList<ArrayList<Integer>> perms = solvePermutation(tmp);
 		//System.out.println(perms);
 		for(ArrayList<Integer> perm : perms)
 			mapping.put((List<Integer>) perm.clone(), (List<Integer>) perm.clone());
 		limitPossibilities(mapping, poss);
 		System.out.println(mapping.keySet().size() + " for " + tmp.size() + " elements");
 		for(Integer i : poss.keySet()){
 			assert(poss.get(i).size()==1);
 			assert(i == poss.get(i).get(0));
 		}
 	}
 
 	/**
 	 * Given a random permutation, returns the (log n) queries that will solve it.
 	 * @param list The permutation
 	 * @return The queries which will solve the permutation.
 	 */
 	public ArrayList<ArrayList<Integer>> solvePermutation(ArrayList<Integer> list) {
 		ArrayList<ArrayList<Integer>> toReturn = new ArrayList<ArrayList<Integer>>();
 		
 		int n = list.size();
 		for(int k = 1; k <= n; k*=2){
 			ArrayList<Integer> partitions = new ArrayList<Integer>();
 			int j = 0;
 			while(j < n){
 				for(int i = 0; i < k && j < n; i++)
 					partitions.add(list.get(j++));
 				j+=k;
 			}
 			if(partitions.size() != n)
 				toReturn.add((ArrayList<Integer>) partitions.clone());
 		}
 		return toReturn;
 	}
 }
