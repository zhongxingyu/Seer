 package mapthatset.g3;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Random;
 import java.util.Set;
 
 import mapthatset.sim.Guesser;
 import mapthatset.sim.GuesserAction;
 
 public class TeamMegamindGuesser extends Guesser {
 
 	boolean verbose = false;
 	// group size
 	int Group_Size = 2;
 	// name of the guesser
 	String strID = "MegamindGuesser";
 	// length of the mapping
 	int MappingLength;
 	// answer[i] is the mapping for i, default value 0 indicates "unknown"
 	ArrayList<Integer> answers;
 	// queue of queries
 	Queue<ArrayList<Integer>> query_queue;
 	// the current query
 	ArrayList<Integer> current_query;
 	// M: all the mapping we have so far
 	Map<HashSet<Integer>, HashSet<Integer>> memory;
 	// M': the subset with approximate maximal expected answers
 	Map<HashSet<Integer>, HashSet<Integer>> m_subset;
 	// Unique element set of M'
 	Set<Integer> uniq_set;
 	// shuffled list
 	ArrayList<Integer> shuffled_list;
 	// indicator of initial phase
 	int processed;
 
 	Random rand = new Random();
 
 	enum Phase {
 		PreInitial, Initial, SloppyInference, StrictInference, PermutationInference, Guess
 	}
 
 	enum MappingType {
 		RandomMapping, BinaryMapping, PermutationMapping
 	}
 
 	Phase current_phase;
 	MappingType mapping_type;
 
 	@Override
 	public void startNewMapping(int intMappingLength) {
 		this.MappingLength = intMappingLength;
 		if (MappingLength <= 100)
 			this.Group_Size = (int) (0.05 * MappingLength + 2.92);
 
 		// initialize answer to be default value 0
 		this.answers = new ArrayList<Integer>(this.MappingLength + 1);
 		// subscript starts from 1, ignoring 0
 		for (int i = 0; i != this.MappingLength + 1; ++i)
 			this.answers.add(0);
 
 		// get a random permutation of 1..n as shuffledList
		this.shuffled_list = new ArrayList<Integer>(
 				intMappingLength);
 		for (int i = 0; i != this.MappingLength; ++i)
			shuffled_list.add(i + 1);
		java.util.Collections.shuffle(shuffled_list);
 
 		// initialize the memory
 		memory = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 
 		current_phase = Phase.PreInitial;
 		mapping_type = MappingType.RandomMapping;
 
 		this.uniq_set = new HashSet<Integer>();
 		this.m_subset = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 
 		// since have processed 0 elements in the shuffled list
 		this.processed = 0;
 	}
 
 	@Override
 	public GuesserAction nextAction() {
 		// if we know the answer
 		if ((current_phase == Phase.SloppyInference || current_phase == Phase.StrictInference)
 				&& memory.isEmpty()) {
 			// remove the first element
 			List<Integer> guess = answers.subList(1, answers.size());
 			answers = new ArrayList<Integer>(guess);
 			current_phase = Phase.Guess;
 			return new GuesserAction("g", answers);
 		}
 
 		HashSet<Integer> query = new HashSet<Integer>();
 
 		// the first move
 		if (current_phase == Phase.PreInitial) {
 			for (int i = 0; i != MappingLength; ++i)
 				query.add(i + 1);
 		} else if (current_phase == Phase.Initial) {
 			for (int i = 0; i != Group_Size; ++i) {
 				int next_key = shuffled_list.get(processed++);
 				query.add(next_key);
 				if (processed == this.MappingLength)
 					break;
 			}
 		} else if (current_phase == Phase.SloppyInference
 				|| current_phase == Phase.StrictInference) {
 			mappingReduction();
 			double exp_answer = agglomerateConstruction(current_phase == Phase.StrictInference);
 			if (verbose)
 				System.out.println("----Expected Answer:" + exp_answer);
 			// select one unknown key from each K(m) for m in M'
 			if (verbose)
 				System.out.println("----Randomly select unknown keys");
 			for (HashSet<Integer> keys : m_subset.keySet()) {
 				while (true) {
 					int index = rand.nextInt(keys.size());
 					int k = (Integer) keys.toArray()[index];
 					if (answers.get(k) == 0) {
 						query.add(k);
 						break;
 					}
 				}
 			}
 		} else if (current_phase == Phase.PermutationInference) {
 			// how to query for permutation?
 		}
 
 		if (verbose)
 			System.out.println("----Post Query:" + query);
 
 		current_query = new ArrayList<Integer>(query);
 		return new GuesserAction("q", current_query);
 	}
 
 	@Override
 	public void setResult(ArrayList<Integer> alResult) {
 
 		int answers_obtained = 0;
 		HashSet<Integer> keys;
 		HashSet<Integer> values;
 
 		switch (current_phase) {
 		case Guess:
 			return; // ignore whatever feedback we get from the guess
 		case PreInitial:
 			if (alResult.size() == MappingLength) {
 				mapping_type = MappingType.PermutationMapping;
 				current_phase = Phase.PermutationInference;
 			} else if (alResult.size() == 2) {
 				mapping_type = MappingType.BinaryMapping;
 				this.Group_Size = 2;
 				current_phase = Phase.Initial;
 			} else {
 				mapping_type = MappingType.RandomMapping;
 				current_phase = Phase.Initial;
 			}
 			break;
 		case Initial:
 			// basic inference
 			keys = new HashSet<Integer>(current_query);
 			values = new HashSet<Integer>(alResult);
 			answers_obtained = basicInference(keys, values);
 			if (answers_obtained == 0)
 				// gather all mappings
 				memory.put(new HashSet<Integer>(current_query),
 						new HashSet<Integer>(alResult));
 			if (processed == this.MappingLength) {
 				if (mapping_type == MappingType.BinaryMapping)
 					current_phase = Phase.StrictInference;
 				else if (mapping_type == MappingType.RandomMapping)
 					current_phase = Phase.SloppyInference;
 				else
 					System.out.println("Unexpected Case");
 			}
 			break;
 		case SloppyInference:
 		case StrictInference:
 			HashSet<Integer> query = new HashSet<Integer>(current_query);
 			HashSet<Integer> result = new HashSet<Integer>(alResult);
 			memory.put(query, result);
 			answers_obtained = uniqueInference(query, result);
 			answers_obtained += basicInference(query, result);
 
 			// random mapping allow the switch between two phases
 			if (mapping_type == MappingType.RandomMapping) {
 				if (answers_obtained == 0) {
 					if (current_phase != Phase.StrictInference)
 						if (verbose)
 							System.out
 									.println("----Switch to Strict Inference");
 					current_phase = Phase.StrictInference;
 				} else {
 					if (current_phase != Phase.SloppyInference)
 						if (verbose)
 							System.out
 									.println("----Switch to Sloppy Inference");
 					current_phase = Phase.SloppyInference;
 				}
 			}
 			while (answers_obtained != 0) {
 				answers_obtained = lastElementInference();
 			}
 			break;
 		case PermutationInference:
 			// how to interpret the result for permutation queries?
 			break;
 		}
 
 		mappingReduction();
 		if (verbose) {
 			System.out.println("----Memory:" + memory);
 			System.out.println("----Answer:" + getAnswer());
 		}
 	}
 
 	void cleanEmptyMapping() {
 		Map<HashSet<Integer>, HashSet<Integer>> new_memory = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 		for (Entry<HashSet<Integer>, HashSet<Integer>> e : memory.entrySet()) {
 			if (e.getKey().size() != 0)
 				new_memory.put(e.getKey(), e.getValue());
 		}
 
 		memory = new_memory;
 	}
 
 	int lastElementInference() {
 		int answers_obtained = 0;
 		for (Entry<HashSet<Integer>, HashSet<Integer>> mapping : memory
 				.entrySet()) {
 			HashSet<Integer> keys = mapping.getKey();
 			HashSet<Integer> values = mapping.getValue();
 
 			int unknown_keys = 0;
 			for (Integer k : keys) {
 				if (answers.get(k) == 0)
 					unknown_keys++;
 			}
 			if (unknown_keys == 1) {
 				if (verbose)
 					System.out
 							.println("----Because it's the last key I don't know in"
 									+ mapping + "\n----And:");
 				int unknown_key = 0;
 				HashSet<Integer> values_from_known_keys = new HashSet<Integer>();
 				for (Integer k : keys) {
 					int value = answers.get(k);
 					if (value != 0) {
 						values_from_known_keys.add(value);
 						if (verbose)
 							System.out.println("----I've already known:" + k
 									+ "->" + value);
 					} else
 						unknown_key = k;
 				}
 				HashSet<Integer> mapping_values = new HashSet<Integer>(values);
 				for (Integer known_value : values_from_known_keys) {
 					if (mapping_values.contains(known_value))
 						mapping_values.remove(known_value);
 				}
 
 				// one left
 				if (mapping_values.size() == 1) {
 					int value_left = (Integer) mapping_values.toArray()[0];
 					answers.set(unknown_key, value_left);
 					answers_obtained++;
 					if (verbose)
 						System.out.println("----Therefore I infer:"
 								+ unknown_key + "->" + value_left);
 				}
 			}
 		}
 		mappingReduction();
 		return answers_obtained;
 	}
 
 	void mappingReduction() {
 		for (Entry<HashSet<Integer>, HashSet<Integer>> mapping : memory
 				.entrySet()) {
 			HashSet<Integer> keys = mapping.getKey();
 			// HashSet<Integer> values = mapping.getValue();
 
 			int unknown_keys = 0;
 			for (Integer k : keys) {
 				if (answers.get(k) == 0)
 					unknown_keys++;
 			}
 
 			if (unknown_keys == 0) {
 				keys.clear();
 			}
 		}
 
 		cleanEmptyMapping();
 	}
 
 	int uniqueInference(HashSet<Integer> query, HashSet<Integer> result) {
 		int answers_obtained = 0;
 		for (Integer v : result) {
 			if (uniq_set.contains(v)) {
 				for (Entry<HashSet<Integer>, HashSet<Integer>> mapping : m_subset
 						.entrySet()) {
 					if (mapping.getValue().contains(v)) {
 						for (Integer k : mapping.getKey()) {
 							if (query.contains(k)) {
 								answers.set(k, v);
 								answers_obtained++;
 								if (verbose) {
 									System.out.println("----Because " + v
 											+ " must come from:" + mapping);
 									System.out.println("----and " + k
 											+ " is the key I chose");
 									System.out.println("----Therefore I infer:"
 											+ k + "->" + v);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		mappingReduction();
 		return answers_obtained;
 	}
 
 	@Override
 	public String getID() {
 		return this.strID;
 	}
 
 	public String getAnswer() {
 		return this.answers.subList(1, answers.size()).toString();
 	}
 
 	private int basicInference(HashSet<Integer> keys, HashSet<Integer> values) {
 		int answers_obtained = 0;
 		if (keys.size() == 0)
 			return answers_obtained;
 		if (values.size() == 1) {
 			int v = (Integer) values.toArray()[0];
 			for (Integer k : keys)
 				answers.set(k, v);
 			answers_obtained = keys.size();
 			if (verbose)
 				System.out.println("----I know:" + keys + "->" + v
 						+ " since they all map to the same value");
 		}
 		keys.clear();
 		mappingReduction();
 		return answers_obtained;
 	}
 
 	private double agglomerateConstruction(boolean strict) {
 
 		Map<Integer, Integer> frequency = new HashMap<Integer, Integer>(
 				MappingLength);
 		for (int i = 0; i != MappingLength; ++i)
 			frequency.put(i + 1, 0);
 
 		// calculate the frequency for every value v in all V(mi)
 		for (HashSet<Integer> vMi : memory.values()) {
 			for (Integer v : vMi) {
 				int f = frequency.get(v);
 				frequency.put(v, f + 1);
 			}
 		}
 
 		PrioritizedMappingComparator cmp = new PrioritizedMappingComparator();
 		PriorityQueue<PrioritizedMapping> queue = new PriorityQueue<PrioritizedMapping>(
 				memory.size(), cmp);
 		for (Entry<HashSet<Integer>, HashSet<Integer>> mapping : memory
 				.entrySet()) {
 			// HashSet<Integer> keys = mapping.getKey();
 			HashSet<Integer> values = mapping.getValue();
 
 			// calculate mapping frequency
 			int f = 0;
 			for (Integer v : values)
 				f += frequency.get(v);
 
 			// calculate priority factor
 			double priority_factor = 1.0 / f / values.size();
 
 			PrioritizedMapping pm = new PrioritizedMapping();
 			pm.mapping = mapping;
 			pm.priority_factor = priority_factor;
 			queue.add(pm);
 		}
 
 		Map<HashSet<Integer>, HashSet<Integer>> tmp_m_subset = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 		HashSet<Integer> tmp_uniq_set = new HashSet<Integer>();
 		double exp_answer = 0;
 		this.uniq_set = new HashSet<Integer>();
 		this.m_subset = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 
 		if (strict == false) {
 			while (!queue.isEmpty()) {
 				Entry<HashSet<Integer>, HashSet<Integer>> mapping = queue
 						.remove().mapping;
 				HashSet<Integer> keys = mapping.getKey();
 				HashSet<Integer> values = mapping.getValue();
 				tmp_m_subset.put(keys, values);
 				tmp_uniq_set = findUniqueSet(tmp_m_subset);
 				double new_exp_answer = expectedAnswer(tmp_m_subset,
 						tmp_uniq_set);
 				if (new_exp_answer < exp_answer)
 					// if (tmp_uniq_set.size() <= this.uniq_set.size())
 					break;
 				this.uniq_set = new HashSet<Integer>(tmp_uniq_set);
 				this.m_subset = new HashMap<HashSet<Integer>, HashSet<Integer>>(
 						tmp_m_subset);
 				exp_answer = new_exp_answer;
 			}
 		} else {
 			while (!queue.isEmpty()) {
 				Entry<HashSet<Integer>, HashSet<Integer>> mapping = queue
 						.remove().mapping;
 				HashSet<Integer> keys = mapping.getKey();
 				HashSet<Integer> values = mapping.getValue();
 				HashSet<Integer> tmp = new HashSet<Integer>(uniq_set);
 				tmp.retainAll(values);
 				if (!tmp.isEmpty())
 					break;
 				uniq_set.addAll(values);
 				m_subset.put(keys, values);
 				exp_answer++;
 			}
 		}
 		if (verbose) {
 			System.out.println("----M':" + this.m_subset);
 			System.out.println("----U(M'):" + this.uniq_set);
 		}
 		return exp_answer;
 	}
 
 	double expectedAnswer(Map<HashSet<Integer>, HashSet<Integer>> tmp_m_subset,
 			HashSet<Integer> tmp_uniq_set) {
 		double exp_answer = 0;
 
 		for (HashSet<Integer> vm : tmp_m_subset.values()) {
 			HashSet<Integer> local_vm = new HashSet<Integer>(vm);
 			int v_size = local_vm.size();
 			local_vm.retainAll(tmp_uniq_set);
 			exp_answer += (double) local_vm.size() / v_size;
 		}
 
 		return exp_answer;
 	}
 
 	HashSet<Integer> findUniqueSet(Map<HashSet<Integer>, HashSet<Integer>> M) {
 		HashMap<Integer, Integer> local_frequency = new HashMap<Integer, Integer>();
 		for (int i = 0; i != this.MappingLength; ++i)
 			local_frequency.put(i + 1, 0);
 		for (HashSet<Integer> vm : M.values()) {
 			for (Integer v : vm) {
 				int f = local_frequency.get(v);
 				local_frequency.put(v, f + 1);
 			}
 		}
 		HashSet<Integer> local_uniq_set = new HashSet<Integer>();
 		for (Entry<Integer, Integer> e : local_frequency.entrySet()) {
 			if (e.getValue() == 1)
 				local_uniq_set.add(e.getKey());
 		}
 		return local_uniq_set;
 	}
 
 	class PrioritizedMapping {
 		Entry<HashSet<Integer>, HashSet<Integer>> mapping;
 		double priority_factor;
 	}
 
 	class PrioritizedMappingComparator implements
 			Comparator<PrioritizedMapping> {
 
 		@Override
 		public int compare(PrioritizedMapping a, PrioritizedMapping b) {
 			if (a.priority_factor > b.priority_factor)
 				return -1;
 			if (a.priority_factor < b.priority_factor)
 				return 1;
 			return 0;
 		}
 	}
 
 }
