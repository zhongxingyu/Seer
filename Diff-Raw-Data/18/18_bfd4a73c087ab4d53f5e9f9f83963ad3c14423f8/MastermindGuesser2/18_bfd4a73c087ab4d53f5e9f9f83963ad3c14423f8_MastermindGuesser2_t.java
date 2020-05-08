 package mapthatset.g6;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import mapthatset.sim.Guesser;
 import mapthatset.sim.GuesserAction;
 
 public class MastermindGuesser2 extends Guesser {
 	Map<Integer, List<Integer>> possibilities;
 	Map<List<Integer>, List<Integer>> rules;
 
 	int mapLength;
 	boolean debuggingMode=false;
 
 
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
 			Collection 	<ArrayList<Integer>> leftTodoNoDups=new LinkedHashSet<ArrayList<Integer>>(leftTodo);
 			leftTodo.clear();
 			if(!leftTodoNoDups.isEmpty())
 				leftTodo.addAll(leftTodoNoDups);
 			if(leftTodo.isEmpty() && !leftToExplore.isEmpty()){
 				leftTodo.add(new ArrayList<Integer>(leftToExplore));
 				//leftToExplore.clear();
 			}
 			if(!leftTodo.isEmpty()){
 				currentGuess = (leftTodo.remove(0));
 				while( (!isNewRule(currentGuess) && !leftTodo.isEmpty() )|| rules.containsKey(currentGuess)) {
 					if (leftTodo.isEmpty())
 						leftTodo.add(new ArrayList<Integer>(leftToExplore));
 					currentGuess = (leftTodo.remove(0));
 				}
 			}
 			//else
 			//System.exit(1);
 		} else {
 			todo = "g";
 			currentGuess = (ArrayList<Integer>) makeGuess();
 		}
 
 		printIfDebugging("");
 
 		return new GuesserAction(todo, currentGuess);
 	}
 
 	public static void main(String args[]){
 		MastermindGuesser2 mmg2 = new MastermindGuesser2();
 		mmg2.testSolutionReached();
 		mmg2.testGetRandomIndex();
 		mmg2.testLimitPossibilities1();
 		mmg2.testLimitPossibilities2();
 
 		//printIfDebugging("Helper methods work.");
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
 
 
 	public ArrayList<ArrayList<Integer>> makeBalancedGuess(List<Integer> toChooseFrom){
 		// currently taking n^0.5 , where n is total size of the toChooseFrom
 		/*
 		 * It is toChooseFrom - elements whose value is already known
 		 */
 		ArrayList<Integer> condensedChoices =new ArrayList<Integer>();
 
 		ArrayList<ArrayList<Integer>> toReturn = new ArrayList<ArrayList<Integer>>();
 		for(Integer i :toChooseFrom)
 			if(possibilities.get(i).size() > 1 ) {
 				condensedChoices.add(i);
 			}
 		int n = condensedChoices.size();
		if(n<=2) {
			toReturn.add(condensedChoices);
			return toReturn;
		}
 		int rootN =(int)Math.ceil(Math.sqrt(n));
 		int lowerIndex=0;
 		int upperIndex=0;
		while(upperIndex<=n) {
 			upperIndex+=rootN;
 			ArrayList<Integer> toAdd=new ArrayList<Integer>();
 			toAdd.clear();
			if(lowerIndex>=n) {
				upperIndex+=rootN;
				continue;
			}
 			
 			if(upperIndex>=n)
				toAdd.addAll(condensedChoices.subList(lowerIndex,n));
 			else
 				toAdd.addAll(condensedChoices.subList(lowerIndex, upperIndex));
 			
 			if(!rules.containsKey(toAdd))
 				toReturn.add(toAdd);
 			else {
             	ArrayList<ArrayList<Integer>> subGuess=new ArrayList<ArrayList<Integer>>(makeBalancedGuess(toAdd));
             	if(!subGuess.isEmpty())
 			          	toReturn.addAll(subGuess);
 			}
 			lowerIndex+=rootN;
 		}
 		
 		return toReturn;
 	}
 
 	public ArrayList<ArrayList<Integer>> makeSmallGuess(List<Integer> toChooseFrom){
 		int j = 0;
 		int n = toChooseFrom.size();
 		int k = 0;
 		ArrayList<ArrayList<Integer>> toReturn = new ArrayList<ArrayList<Integer>>();
 		while(k<n){
 			while(j < n && possibilities.get(toChooseFrom.get(j)).size() == 1) j++;
 			k = j+1;
 			while(k < n && (possibilities.get(toChooseFrom.get(k)).size()==1 || !isNewRule(toChooseFrom, j, k) )  ) k++;
 			if(k<n) toReturn.add(getArr(toChooseFrom,j,k));
 			else if(j < n){
 				ArrayList<Integer> t = new ArrayList<Integer>();
 				t.add(j);
 				toReturn.add(t);
 			}
 			j=k+1;
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
 		//rules.put((List<Integer>) currentGuess.clone(), (List<Integer>) alResult.clone());
 		//	printIfDebugging(possibilities);
 		printIfDebugging("\t###########\n\t"+currentGuess+" ->> "+alResult+"\n\t###########");
 		updateRules(currentGuess,alResult);
 		limitPossibilities(rules, possibilities);
 		printCurrentRules("After");
 		printIfDebugging("\n\t\t\t\t\t\tPossibilities"+possibilities);
 
 		if(currentGuess.size() == alResult.size()){
 			ArrayList<ArrayList<Integer>> perms = solvePermutation(currentGuess);
 			if(leftToExplore.containsAll(currentGuess)){
 				leftTodo.addAll(perms);
 				leftToExplore.removeAll(currentGuess);
 			}
 		}
 		else{
 			leftTodo.addAll(makeBalancedGuess(leftToExplore));
			// should this be commented ? leftToExplore.removeAll(currentGuess);
 		}
 	}
 
 	public void  updatePossibilities(ArrayList<Integer> domain, ArrayList<Integer> range) {
 		//printIfDebugging("\n updatePosin="+domain+"  range="+range);
 		for(Integer choice : domain){
 			possibilities.get(choice).retainAll(range);
 		}
 	}
 
 	/*
 	 *  To print the current state of rules table
 	 *  (supporting method)
 	 */
 	public void printCurrentRules(String message) {
 		printIfDebugging("\n\tRules Table  "+message);
 		for (Map.Entry<List<Integer>,List<Integer>> singleRule : rules.entrySet()) {
 			List<Integer> domainList = singleRule.getKey();
 			List<Integer> rangeList = singleRule.getValue();
 			if(domainList.size()>=1)
 				printIfDebugging("\t"+domainList+"  --> "+rangeList);
 		}
 		printIfDebugging("\tRules END  \n");
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
 				//printIfDebugging(perms);
 				for(ArrayList<Integer> perm : perms)
 					mapping.put((List<Integer>) perm.clone(), (List<Integer>) perm.clone());
 						limitPossibilities(mapping, poss);
 						printIfDebugging(mapping.keySet().size() + " for " + tmp.size() + " elements");
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
 	/**
 	 *   updateRules updates rules recursively
 	 *         By creating a new rule considering 
 	 *         intersection and UMI (union minus intersection )with previous rules
 	 *          
 	 *  
 	 *  @param newRuleDomain
 	 *                   query of the rule
 	 *  @param  newRuleRange                 
 	 *                    result of the query
 	 */
 	public void updateRules (ArrayList<Integer> newRuleDomain,ArrayList<Integer> newRuleRange) {
 		Collections.sort(newRuleDomain);
 		Collections.sort(newRuleRange);
 		if(rules.size()==0) {
 			if(newRuleDomain.size()>0 && newRuleRange.size()>0) {
 				rules.put(newRuleDomain, newRuleRange);
 				if(newRuleDomain.size()==newRuleRange.size() && newRuleRange.size() ==1)
 					possibilities.put(newRuleDomain.get(0), newRuleRange);
 				else
 					updatePossibilities(newRuleDomain, newRuleRange);
 				//updateQueryHistory(newRuleDomain);
 			}
 		} else {
 			if(! ( rules.containsKey(newRuleDomain) && rules.get(newRuleDomain).equals(newRuleRange) ) ) {
 				// if the new rule is not in in the rule set - add it 
 				if(newRuleDomain.size()>0 && newRuleRange.size()>0) {
 					rules.put(newRuleDomain, newRuleRange);
 					if(newRuleRange.size() ==1){
 						for(Integer singleDomain :newRuleDomain ) {
 							possibilities.put(singleDomain, newRuleRange);
 							ArrayList<Integer> dummy = new ArrayList<Integer>();
 							dummy.add(singleDomain);
 							if(!rules.containsKey(dummy))
 								rules.put(dummy,newRuleRange);
 						}
 					}
 					else
 						updatePossibilities(newRuleDomain, newRuleRange);
 					//updateQueryHistory(newRuleDomain);
 				}
 			}
 			Map<ArrayList<Integer>, ArrayList<Integer>> rulesToBeAdded=new HashMap<ArrayList<Integer>,ArrayList<Integer>>();
 			for (Map.Entry<List<Integer>,List<Integer>> singleRule : rules.entrySet()) {
 				ArrayList<Integer> ruleDomain = (ArrayList<Integer>)singleRule.getKey();
 				ArrayList<Integer> ruleRange = (ArrayList<Integer>)singleRule.getValue();
 
 				/* domainIntersection and rangeIntersection are the intersections of the new rule
 				 * with  already present rule
 				 * example -> ([1,3,6]->[2,3]) intersection ([1,4,9]->[3,4])
 				 *          ==>  [1]->[3]
 				 * */
 				ArrayList<Integer> domainIntersection =new ArrayList<Integer>();
 				domainIntersection.addAll(newRuleDomain);  domainIntersection.retainAll(ruleDomain);
 				ArrayList<Integer> rangeIntersection =new ArrayList<Integer>();
 				rangeIntersection.addAll(newRuleRange);  rangeIntersection.retainAll(ruleRange);
 				if (domainIntersection.size()>0 && rangeIntersection.size()>0) {
 					if(! ( 
 							(rules.containsKey(domainIntersection) && rules.get(domainIntersection).equals(rangeIntersection))
 							||
 							(rulesToBeAdded.containsKey(domainIntersection) && rulesToBeAdded.get(domainIntersection).equals(rangeIntersection))
 							) ) {
 						// if the result of intersection is a new rule -add it in the rule list
 						if(domainIntersection.size()>=rangeIntersection.size())
 							rulesToBeAdded.put(domainIntersection, rangeIntersection);
 						else
 							printIfDebugging("ERROR -");
 						printIfDebugging("domainIntersection="+domainIntersection+" rangeIntersection="+rangeIntersection+
 								"\n\tnewRuleDomain="+newRuleDomain+" newRuleRange="+newRuleRange+
 								"\n\truleDomain="+ruleDomain+"  ruleRange="+ruleRange);
 					}
 				}
 
 				/* domainUnion and rangeUnion are the union of the new rule
 				 * with  already present rule
 				 * example -> ([1,3,6]->[2,3]) union ([1,4,9]->[3,4])
 				 *          ==>  [1,3,4,6,9]->[2,3,4]
 				 * */
 				Set<Integer> unionSet = new HashSet<Integer>();
 				ArrayList<Integer> domainUnion =new ArrayList<Integer>();
 				unionSet.addAll(newRuleDomain);unionSet.addAll(ruleDomain);
 				domainUnion.addAll(unionSet); unionSet.clear();
 				ArrayList<Integer> rangeUnion =new ArrayList<Integer>();
 				unionSet.addAll(newRuleRange);unionSet.addAll(ruleRange);
 				rangeUnion.addAll(unionSet); 
 				if (domainUnion.size()>0 && rangeUnion.size()>0) {
 					if(! ( 
 							(rules.containsKey(domainUnion) && rules.get(domainUnion).equals(rangeUnion))
 							||
 							(rulesToBeAdded.containsKey(domainUnion) && rulesToBeAdded.get(domainUnion).equals(rangeUnion))
 							) ) {
 						// if the result of union is a new rule -add it in the rule list
 						if(domainUnion.size()>=rangeUnion.size())
 							rulesToBeAdded.put(domainUnion, rangeUnion);
 						else
 							System.out.print("ERROR --");
 						printIfDebugging("domainUnion="+domainUnion+" rangeUnion="+rangeUnion+
 								"\n\tnewRuleDomain="+newRuleDomain+" newRuleRange="+newRuleRange+
 								"\n\truleDomain="+ruleDomain+"  ruleRange="+ruleRange);
 					}
 				}
 
 
 				/* this section is for R1MI -  rule 1 ( the newRule) minus intersection 
 				 * if  the two rules( rule and intersections) have same difference in their respective domain and range
 				 * then this applies 
 				 * example -> ([3,5,7,8,9]->[1,2,3,4]) R1MI  ([5,6,7,8]->[1,2])
 				 *           (Rule1  [3,5,7,8,9]->[1,2,3,4]) - (Intersection - [5,7,8] ->[1,2]) 
 				 *         ==>[3,9]->[3,4] 
 				 */ 				
 				if (domainIntersection.size()>=rangeIntersection.size() && newRuleDomain.size()-newRuleRange.size() == domainIntersection.size()-rangeIntersection.size()
 						&& domainIntersection.size()>0
 						) {
 
 					ArrayList<Integer> domainR1MI =new ArrayList<Integer>();
 					domainR1MI.addAll(newRuleDomain); domainR1MI.removeAll(domainIntersection);
 					ArrayList<Integer> rangeR1MI =new ArrayList<Integer>();
 					rangeR1MI.addAll(newRuleRange);rangeR1MI.removeAll(rangeIntersection);
 					if (domainR1MI.size()>0 && rangeR1MI.size()>0) {
 						if(! ( 
 								( rules.containsKey(domainR1MI) && rules.get(domainR1MI).equals(rangeR1MI)  ) 
 								||
 								( rulesToBeAdded.containsKey(domainR1MI) && rulesToBeAdded.get(domainR1MI).equals(rangeR1MI)  )
 								)) {
 							if(domainR1MI.size()>=rangeR1MI.size())
 								rulesToBeAdded.put(domainR1MI, rangeR1MI);
 							else
 								System.out.print("ERROR");
 							printIfDebugging("domainR1MI="+domainR1MI+" rangeR1MI="+rangeR1MI+
 									"\n\tnewRuleDomain="+newRuleDomain+" newRuleRange="+newRuleRange+
 									"\n\truleDomain="+ruleDomain+"  ruleRange="+ruleRange);
 						}
 					}
 				}
 
 				/* this section is for R2MI -  rule 2 ( the rule) minus intersection 
 				 * if  the two rules( rule and intersections) have same difference in their respective domain and range
 				 * then this applies 
 				 * example ->    ([5,6,7,8]->[1,2]) R2MI ([3,5,7,8,9]->[1,2,3,4])
 				 *           (Rule2  [3,5,7,8,9]->[1,2,3,4]) - (Intersection - [5,7,8] ->[1,2]) 
 				 *         ==>[3,9]->[3,4] 
 				 */ 				
 				if (domainIntersection.size()>=rangeIntersection.size() && ruleDomain.size()-ruleRange.size() == domainIntersection.size()-rangeIntersection.size()
 						&& domainIntersection.size()>0
 						) {
 
 					ArrayList<Integer> domainR2MI =new ArrayList<Integer>();
 					domainR2MI.addAll(newRuleDomain); domainR2MI.removeAll(domainIntersection);
 					ArrayList<Integer> rangeR2MI =new ArrayList<Integer>();
 					rangeR2MI.addAll(newRuleRange);rangeR2MI.removeAll(rangeIntersection);
 					if (domainR2MI.size()>0 && rangeR2MI.size()>0) {
 						if(! ( 
 								( rules.containsKey(domainR2MI) && rules.get(domainR2MI).equals(rangeR2MI)  ) 
 								||
 								( rulesToBeAdded.containsKey(domainR2MI) && rulesToBeAdded.get(domainR2MI).equals(rangeR2MI)  )
 								)) {
 							if(domainR2MI.size()>=rangeR2MI.size())
 								rulesToBeAdded.put(domainR2MI, rangeR2MI);
 							else
 								System.out.print("ERROR ");
 							printIfDebugging("domainR2MI="+domainR2MI+" rangeR2MI="+rangeR2MI+
 									"\n\tnewRuleDomain="+newRuleDomain+" newRuleRange="+newRuleRange+
 									"\n\truleDomain="+ruleDomain+"  ruleRange="+ruleRange);
 						}
 					}
 				}
 
 
 				/* this section is for UMI - union minus intersection 
 				 * if  the two rules( their union  and intersections) have same difference in their respective domain and range
 				 * then this applies 
 				 * example -> ([3,5,7,8]->[1,2,4]) UMI  ([5,6,7,8]->[1,2,3])
 				 *           Union - [3,5,6,7,8]->[1,2,3,4]   Intersection - [5,7,8] ->[1,2] 
 				 *         ==>[3,6]->[3,4] ( which will ultimately solve for 3 and 6 etc..)
 				 */ 				
 				if (domainIntersection.size()>=rangeIntersection.size() && domainUnion.size()-rangeUnion.size() == domainIntersection.size()-rangeIntersection.size()
 						&& domainIntersection.size()>0
 						) {
 					ArrayList<Integer> domainUMI =new ArrayList<Integer>();
 					domainUMI.addAll(domainUnion); domainUMI.removeAll(domainIntersection);
 					ArrayList<Integer> rangeUMI =new ArrayList<Integer>();
 					rangeUMI.addAll(rangeUnion);rangeUMI.removeAll(rangeIntersection);
 					if (domainUMI.size()>0 && rangeUMI.size()>0) {
 						if(! ( 
 								( rules.containsKey(domainUMI) && rules.get(domainUMI).equals(rangeUMI)  ) 
 								||
 								( rulesToBeAdded.containsKey(domainUMI) && rulesToBeAdded.get(domainUMI).equals(rangeUMI)  )
 								)) {
 							if(domainUMI.size()>=rangeUMI.size())
 								rulesToBeAdded.put(domainUMI, rangeUMI);
 							else
 								System.out.print("ERROR");
 							printIfDebugging("domainUMI="+domainUMI+" rangeUMI="+rangeUMI+
 									"\n\tnewRuleDomain="+newRuleDomain+" newRuleRange="+newRuleRange+
 									"\n\truleDomain="+ruleDomain+"  ruleRange="+ruleRange);
 						}
 					}
 				}
 			}//end of rules loop
 			if (rulesToBeAdded.size()>=1){
 				// add all the new rules
 				for (Map.Entry<ArrayList<Integer>,ArrayList<Integer>> singleRule : rulesToBeAdded.entrySet()) {
 					ArrayList<Integer> ruleDomain = singleRule.getKey();
 					ArrayList<Integer> ruleRange = singleRule.getValue();
 					if( !(rules.containsKey(ruleDomain) && rules.get(ruleDomain).equals(ruleRange) ) ) {
 						Collections.sort(ruleDomain);
 						Collections.sort(ruleRange);
 						rules.put(ruleDomain,ruleRange);
 						if(ruleDomain.size()==ruleRange.size() && ruleRange.size() ==1)
 							possibilities.put(ruleDomain.get(0), ruleRange);
 						else
 							updatePossibilities(ruleDomain, ruleRange);
 						//	updateQueryHistory((ArrayList<Integer>)ruleDomain);
 					}
 				}
 			}
 		}
 	}
 
 	public  void printIfDebugging(String stringToPrint) {
 		if(debuggingMode) {
 			System.out.println(stringToPrint);
 		}
 	}
 
 }
