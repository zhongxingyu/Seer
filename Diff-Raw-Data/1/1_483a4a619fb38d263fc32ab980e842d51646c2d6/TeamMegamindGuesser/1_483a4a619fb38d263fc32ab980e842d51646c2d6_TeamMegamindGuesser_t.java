 package mapthatset.g3;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Set;
 
 import mapthatset.sim.Guesser;
 import mapthatset.sim.GuesserAction;
 
 public class TeamMegamindGuesser extends Guesser {
 
 	// name of the guesser
 	String strID = "MegamindGuesser";
 	// length of the mapping
 	int MappingLength;
 	// answer[i] is the mapping for i, default value 0 indicates "unknown"
 	ArrayList<Integer> answer;
 	// the number of unknown mapping
 	int unknown;
 	// queue of queries
 	Queue<ArrayList<Integer>> query_queue;
 	// the current query
 	ArrayList<Integer> current_query;
 	// record the query-result pairs
 	Map<HashSet<Integer>, HashSet<Integer>> memory;
 	//
 	Map<HashSet<Integer>, HashSet<Integer>> disjoint_set;
 
 	enum QueryType {
 		Pairs, Disjoint, One, Two
 	}
 
 	// indicator of current query type
 	QueryType current_query_type;
 
 	@Override
 	public void startNewMapping(int intMappingLength) {
 		this.MappingLength = intMappingLength;
 		this.unknown = intMappingLength;
 
 		// initialize answer to be default value 0
 		this.answer = new ArrayList<Integer>(this.MappingLength + 1);
 		// subscript starts from 1, ignoring 0
 		for (int i = 0; i != this.MappingLength + 1; ++i)
 			this.answer.add(0);
 
 		// get a random permutation of 1..n as shuffledList
 		ArrayList<Integer> shuffledList = new ArrayList<Integer>(
 				intMappingLength);
 		for (int i = 0; i != this.MappingLength; ++i)
 			shuffledList.add(i + 1);
 		java.util.Collections.shuffle(shuffledList);
 
 		// initialize the memory
 		memory = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 
 		// initialize the query queue
 		this.query_queue = new LinkedList<ArrayList<Integer>>();
 
 		// generate all pair queries into query queue
 		Iterator<Integer> iter = shuffledList.iterator();
 		while (iter.hasNext()) {
 			ArrayList<Integer> query = new ArrayList<Integer>();
 			query.add(iter.next());
 			if (iter.hasNext())
 				query.add(iter.next());
 			query_queue.add(query);
 		}
 
 		current_query_type = QueryType.Pairs;
 	}
 
 	@Override
 	public GuesserAction nextAction() {
 		// if we know the answer
 		if (unknown == 0) {
 			// remove the first element
 			List<Integer> guess = answer.subList(1, answer.size());
 			answer = new ArrayList<Integer>(guess);
 			return new GuesserAction("g", answer);
 		}
 		if (query_queue.isEmpty())
 			try {
 				this.generateQueries();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		current_query = query_queue.remove();
 		return new GuesserAction("q", current_query);
 	}
 
 	@Override
 	public void setResult(ArrayList<Integer> alResult) {
 
 		switch (current_query_type) {
 		case Pairs:
 			// special case: same result for current query
 			if (alResult.size() == 1) {
 				int value = alResult.get(0);
 				for (Integer i : current_query)
 					answer.set(i, value);
 				unknown -= current_query.size();
				return;
 			}
 			// memorize the result
 			memory.put(new HashSet<Integer>(current_query),
 					new HashSet<Integer>(alResult));
 			break;
 		case Disjoint:
 			resolveDisjoint(alResult);
 			this.updateMemory();
 			System.out.println("After disjoint query, memory:\n"
 					+ this.getMemory());
 			System.out.println("Answer:" + this.getAnswer());
 			current_query_type = QueryType.Pairs;
 			break;
 		}
 	}
 
 	@Override
 	public String getID() {
 		return this.strID;
 	}
 
 	public String getAnswer() {
 		return this.answer.subList(1, answer.size()).toString();
 	}
 
 	public String getQueryQueue() {
 		String s = new String();
 		for (ArrayList<Integer> q : query_queue)
 			s += q.toString() + ' ';
 		return s;
 	}
 
 	public String getMemory() {
 		String s = new String();
 		for (Entry<HashSet<Integer>, HashSet<Integer>> e : memory.entrySet())
 			s += e.getKey().toString() + "->" + e.getValue().toString() + "\n";
 		return s;
 	}
 
 	private void generateQueries() throws Exception {
 		System.out.println("\nMemory:");
 		System.out.print(this.getMemory());
 		System.out.println("\nAnswer:");
 		System.out.println(this.getAnswer());
 
 		switch (current_query_type) {
 		case Pairs: // all pairs
 			disjoint_set = findMaximalDisjointSet();
 			// future improvement: if disjoint_set.size == 1?
 			ArrayList<Integer> query = selectQueryFromDisjointSet(disjoint_set);
 			query_queue.add(query);
 			current_query_type = QueryType.Disjoint;
 
 			System.out.println("Query from disjoint set:");
 			System.out.println(query);
 			break;
 
 		case Disjoint:
 			break;
 
 		case One:
 			break;
 
 		case Two:
 			break;
 
 		default:
 			throw new Exception("Invalid Phase!");
 		}
 	}
 
 	private void updateMemory() {
 		HashSet<HashSet<Integer>> toBeDeleted = new HashSet<HashSet<Integer>>();
 		HashSet<HashMap<Integer, Integer>> new_answers = new HashSet<HashMap<Integer, Integer>>();
 		for (Entry<HashSet<Integer>, HashSet<Integer>> e : memory.entrySet()) {
 			// both query and result must be pairs
 			HashSet<Integer> query = e.getKey();
 			HashSet<Integer> result = e.getValue();
 			for (Integer q : query) {
 				// if we know the answer for q
 				if (answer.get(q) != 0) {
 					HashMap<Integer, Integer> new_answer = resolvePairs(query,
 							result, q);
 					new_answers.add(new_answer);
 					toBeDeleted.add(query);
 				}
 			}
 		}
 
 		// delete resolved pairs
 		for (HashSet<Integer> obsolete : toBeDeleted)
 			memory.remove(obsolete);
 
 		// store new answers
 		for (HashMap<Integer, Integer> as : new_answers) {
 			for (Entry<Integer, Integer> a : as.entrySet()) {
 				answer.set(a.getKey(), a.getValue());
 			}
 		}
 		unknown -= new_answers.size();
 	}
 
 	private HashMap<Integer, Integer> resolvePairs(HashSet<Integer> query,
 			HashSet<Integer> result, int q) {
 		// copy the query and result
 		HashSet<Integer> tmp_query = new HashSet<Integer>(query);
 		HashSet<Integer> tmp_result = new HashSet<Integer>(result);
 
 		// get the other query
 		tmp_query.remove(q);
 		int other = (Integer) tmp_query.toArray()[0];
 
 		// get the other value
 		int value = answer.get(q);
 		tmp_result.remove(value);
 		int other_value = (Integer) tmp_result.toArray()[0];
 
 		System.out.println("I know:" + q + "->" + value);
 		System.out.println("I infer:" + other + "->" + other_value);
 
 		HashMap<Integer, Integer> new_answer = new HashMap<Integer, Integer>();
 		new_answer.put(other, other_value);
 		return new_answer;
 	}
 
 	private Map<HashSet<Integer>, HashSet<Integer>> findMaximalDisjointSet() {
 		// not optimal, using agglomerate cover
 		HashSet<Integer> cover = new HashSet<Integer>();
 		Map<HashSet<Integer>, HashSet<Integer>> disjoint_set = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 		for (Entry<HashSet<Integer>, HashSet<Integer>> e : memory.entrySet()) {
 			HashSet<Integer> tmp_cover = new HashSet<Integer>(cover);
 			HashSet<Integer> result = e.getValue();
 			// intersection
 			tmp_cover.retainAll(result);
 			// if it is disjoint
 			if (tmp_cover.size() == 0) {
 				cover.addAll(result);
 				disjoint_set.put(e.getKey(), e.getValue());
 			}
 		}
 
 		System.out.println("Disjoint Set:");
 		System.out.println(disjoint_set.toString());
 		return disjoint_set;
 	}
 
 	private ArrayList<Integer> selectQueryFromDisjointSet(
 			Map<HashSet<Integer>, HashSet<Integer>> disjoint_set) {
 		ArrayList<Integer> query = new ArrayList<Integer>();
 		for (HashSet<Integer> q : disjoint_set.keySet()) {
 			int anyone = (Integer) q.toArray()[0];
 			query.add(anyone);
 		}
 		return query;
 	}
 
 	private void resolveDisjoint(ArrayList<Integer> result) {
 		System.out.println("Resolve disjoint query:");
 		System.out.println("From result:" + result);
 		for (Integer value : result) {
 			for (Entry<HashSet<Integer>, HashSet<Integer>> e : disjoint_set
 					.entrySet()) {
 				if (e.getValue().contains(value)) {
 					HashSet<Integer> query = new HashSet<Integer>(e.getKey());
 					query.retainAll(current_query);
 					int q = (Integer) query.toArray()[0];
 					answer.set(q, value);
 					unknown -= 1;
 					System.out.println("I infer:" + q + "->" + value);
 					break;
 				}
 			}
 		}
 	}
 }
