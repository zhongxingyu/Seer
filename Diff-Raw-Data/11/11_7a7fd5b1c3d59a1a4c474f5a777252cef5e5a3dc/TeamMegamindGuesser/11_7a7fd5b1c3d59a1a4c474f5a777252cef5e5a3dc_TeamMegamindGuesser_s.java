 package mapthatset.g3;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
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
 
 	boolean verbose = true;
 	// group size
 	int Group_Size = 10;
 	// threshold
	int threshold = 5;
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
 	// Unique keys
 	Set<Integer> uniq_keys;
 	// shuffled list
 	ArrayList<Integer> shuffled_list;
 	// indicator of initial phase
 	int processed;
 
 	Random rand = new Random();
 
 	enum Phase {
 		PreInitial, Initial, SloppyInference, StrictInference, Guess, PermutationInference
 	}
 
 	enum MappingType {
 		RandomMapping, BinaryMapping, PermutationMapping
 	}
 
 	Phase current_phase;
 	MappingType mapping_type;
 
 	@Override
 	public void startNewMapping(int intMappingLength) {
 		this.MappingLength = intMappingLength;
 
 		// initialize answer to be default value 0
 		this.answers = new ArrayList<Integer>(this.MappingLength + 1);
 		// subscript starts from 1, ignoring 0
 		for (int i = 0; i != this.MappingLength + 1; ++i)
 			this.answers.add(0);
 
 		// get a random permutation of 1..n as shuffledList
 		this.shuffled_list = new ArrayList<Integer>(intMappingLength);
 		for (int i = 0; i != this.MappingLength; ++i)
 			shuffled_list.add(i + 1);
 		java.util.Collections.shuffle(shuffled_list);
 
 		// initialize the memory
 		memory = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 
 		if (MappingLength > threshold)
 			current_phase = Phase.PreInitial;
 		else
 			current_phase = Phase.Initial;
 		mapping_type = MappingType.RandomMapping;
 
 		this.uniq_set = new HashSet<Integer>();
 		this.m_subset = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 		this.uniq_keys = new HashSet<Integer>();
 
 		// since have processed 0 elements in the shuffled list
 		this.processed = 0;
 	}
 
 	@Override
 	public GuesserAction nextAction() {
 		if (verbose)
 			System.out.println("----Phase:" + current_phase);
 		// if we know the answer
 		boolean solved = true;
 		for (int i = 0; i != MappingLength; ++i) {
 			if (answers.get(i + 1) == 0) {
 				solved = false;
 				break;
 			}
 		}
 		if (solved) {
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
 			do {
 				for (int i = 0; i != Group_Size; ++i) {
 					int next_key = shuffled_list.get(processed++);
 					query.add(next_key);
 					if (processed == this.MappingLength) {
 						if (!memory.containsKey(query) && notSolvedYet(query)) {
 							break;
 						} else {
 							query.clear();
 							if (mapping_type == MappingType.BinaryMapping)
 								current_phase = Phase.StrictInference;
 							else if (mapping_type == MappingType.RandomMapping)
 								current_phase = Phase.SloppyInference;
 							else if (mapping_type == MappingType.PermutationMapping)
 								current_phase = Phase.PermutationInference;
 							else
 								System.out.println("Unexpected Case");
 
 							break;
 						}
 					}
 				}
 			} while (memory.containsKey(query));
 		}
 		if (current_phase == Phase.PermutationInference) {
 			int max_keyset_size = 0;
 
 			Map<HashSet<Integer>, HashSet<Integer>> max_mappings = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 
 			for (Entry<HashSet<Integer>, HashSet<Integer>> m : memory
 					.entrySet()) {
 				if (m.getKey().size() > max_keyset_size) {
 					max_keyset_size = m.getKey().size();
 				}
 			}
 
 			for (Entry<HashSet<Integer>, HashSet<Integer>> m : memory
 					.entrySet()) {
 				if (m.getKey().size() >= max_keyset_size - 1)
 					max_mappings.put(m.getKey(), m.getValue());
 			}
 
 			for (Entry<HashSet<Integer>, HashSet<Integer>> m : max_mappings
 					.entrySet()) {
 				HashSet<Integer> max_keyset = m.getKey();
 				int half_size = (int) Math.ceil(max_keyset.size() / 2.0);
 				if (half_size <= 1) {
 					current_phase = Phase.StrictInference;
 					break;
 				} else {
 					Iterator<Integer> iter = max_keyset.iterator();
 					for (int i = 0; i != half_size; ++i) {
 						query.add(iter.next());
 					}
 				}
 			}
 		}
 
 		if (current_phase == Phase.SloppyInference) {
 			mappingCleaning();
 			double exp_answer = agglomerateConstruction(false);
 			if (verbose)
 				System.out.println("----Expected Answer:" + exp_answer);
 			// select one unknown key from each K(m) for m in M'
 			if (verbose) {
 				System.out.println("----Randomly select unknown keys");
 				System.out.println("----Unique keys:" + uniq_keys);
 			}
 			for (HashSet<Integer> keys : m_subset.keySet()) {
 				int count = 0;
 				while (true) {
 					if (++count > MappingLength)
 						break;
 
 					int index = rand.nextInt(keys.size());
 					int k = (Integer) keys.toArray()[index];
 					if (answers.get(k) == 0 && uniq_keys.contains(k)) {
 						query.add(k);
 						break;
 					}
 				}
 			}
 			if (query.isEmpty()) {
 				current_phase = Phase.StrictInference;
 				if (verbose)
 					System.out
 							.println("----Cannot find such query. Switch to Strict Inference.");
 			}
 		}
 
 		if (current_phase == Phase.StrictInference) {
 			mappingCleaning();
 			double exp_answer = agglomerateConstruction(true);
 			if (verbose)
 				System.out.println("----Expected Answer:" + exp_answer);
 			// select one unknown key from each K(m) for m in M'
 			if (verbose) {
 				System.out.println("----Randomly select unknown keys");
 				System.out.println("----Unique keys:" + uniq_keys);
 			}
 			for (HashSet<Integer> keys : m_subset.keySet()) {
 				while (true) {
 					int index = rand.nextInt(keys.size());
 					int k = (Integer) keys.toArray()[index];
 					if (answers.get(k) == 0 && uniq_keys.contains(k)) {
 						query.add(k);
 						break;
 					}
 				}
 			}
 		}
 
 		if (verbose)
 			System.out.println("----Post Query:" + query);
 
 		current_query = new ArrayList<Integer>(query);
 		return new GuesserAction("q", current_query);
 	}
 
 	boolean notSolvedYet(HashSet<Integer> query) {
 		boolean solved = true;
 		for (Integer q : query) {
 			if (answers.get(q) == 0) {
 				solved = false;
 				break;
 			}
 		}
 		return !solved;
 	}
 
 	@Override
 	public void setResult(ArrayList<Integer> alResult) {
 
 		int answers_obtained = 0;
 		HashSet<Integer> query = new HashSet<Integer>(current_query);
 		HashSet<Integer> result = new HashSet<Integer>(alResult);
 
 		switch (current_phase) {
 		case Guess:
 			return; // ignore whatever feedback we get from the guess
 		case PreInitial:
 			memory.put(query, result);
 			if (alResult.size() == 2) {
 				mapping_type = MappingType.BinaryMapping;
 				this.Group_Size = 2;
 				current_phase = Phase.Initial;
 			} else if (alResult.size() == MappingLength) {
 				mapping_type = MappingType.PermutationMapping;
 				this.Group_Size = (int) Math.ceil(MappingLength / 2.0);
 				current_phase = Phase.PermutationInference;
 			} else {
 				mapping_type = MappingType.RandomMapping;
				if (MappingLength <= 5)
					this.Group_Size = 2;
				if (MappingLength <= 10)
					this.Group_Size = 3;
 				if (MappingLength <= 100)
 					this.Group_Size = (int) (0.05 * MappingLength + 2.92);
 
 				current_phase = Phase.Initial;
 			}
 			break;
 		case Initial:
 			// basic inference
 			memory.put(query, result);
 			answers_obtained = basicInference();
 			if (answers_obtained == 0)
 				// gather all mappings
 				memory.put(new HashSet<Integer>(current_query),
 						new HashSet<Integer>(alResult));
 			if (processed == this.MappingLength) {
 				if (mapping_type == MappingType.BinaryMapping
 						|| MappingLength < threshold)
 					current_phase = Phase.StrictInference;
 				else if (mapping_type == MappingType.RandomMapping)
 					current_phase = Phase.SloppyInference;
 				else if (mapping_type == MappingType.PermutationMapping)
 					current_phase = Phase.PermutationInference;
 				else
 					System.out.println("Unexpected Case");
 			}
 			break;
 		case SloppyInference:
 		case StrictInference:
 			memory.put(query, result);
 			answers_obtained = uniqueInference(query, result);
 			int mapping_reduced = this.mappingReduction();
 			if (verbose)
 				System.out.println("----Mapping Reduced:" + mapping_reduced);
 			answers_obtained += basicInference();
 			if (verbose)
 				System.out
 						.println("----Answers I get from Strict/Sloppy Unique Inference:"
 								+ answers_obtained);
 			// random mapping allow the switch between two phases
 			if (mapping_type == MappingType.RandomMapping) {
 				if (answers_obtained == 0) {
 					if (current_phase != Phase.StrictInference)
 						if (verbose)
 							System.out
 									.println("----Switch to Strict Inference");
 					current_phase = Phase.StrictInference;
 				} else {
 					if (mapping_type == MappingType.RandomMapping
 							&& MappingLength > threshold)
 						current_phase = Phase.SloppyInference;
 				}
 			}
 			break;
 		case PermutationInference:
 			Map<HashSet<Integer>, HashSet<Integer>> toBeAdded = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 
 			for (Entry<HashSet<Integer>, HashSet<Integer>> m : memory
 					.entrySet()) {
 				HashSet<Integer> keys = new HashSet<Integer>(m.getKey());
 				HashSet<Integer> values = new HashSet<Integer>(m.getValue());
 				keys.retainAll(query);
 				values.retainAll(result);
 
 				toBeAdded.put(keys, values);
 			}
 
 			for (Entry<HashSet<Integer>, HashSet<Integer>> m : toBeAdded
 					.entrySet()) {
 				memory.put(m.getKey(), m.getValue());
 			}
 		}
 
 		mappingCleaning();
 		mappingInference();
 		basicInference();
 		mappingCleaning();
 		if (verbose) {
 			System.out.println("----Memory:" + memory);
 			System.out.println("----Answer:" + getAnswer());
 		}
 	}
 
 	int mappingInference() {
 		int mapping_inferenced = 0;
 		int max_keyset_size = 0;
 		Map<HashSet<Integer>, HashSet<Integer>> full_mappings = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 		Map<HashSet<Integer>, HashSet<Integer>> toBeAdded = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 		HashSet<HashSet<Integer>> toBeDeleted = new HashSet<HashSet<Integer>>();
 
 		for (Entry<HashSet<Integer>, HashSet<Integer>> m : memory.entrySet()) {
 			if (m.getKey().size() == m.getValue().size())
 				full_mappings.put(m.getKey(), m.getValue());
 		}
 
 		for (Entry<HashSet<Integer>, HashSet<Integer>> m1 : memory.entrySet()) {
 			for (Entry<HashSet<Integer>, HashSet<Integer>> m2 : memory
 					.entrySet()) {
 				if (m1.getKey().size() <= m2.getKey().size())
 					continue;
 				HashSet<Integer> keys_larger = new HashSet<Integer>(m1.getKey());
 				HashSet<Integer> values_larger = new HashSet<Integer>(
 						m1.getValue());
 				HashSet<Integer> keys_smaller = new HashSet<Integer>(
 						m2.getKey());
 				HashSet<Integer> values_smaller = new HashSet<Integer>(
 						m2.getValue());
 				if (keys_larger.containsAll(keys_smaller)) {
 					for (Integer v : values_smaller)
 						values_larger.remove(v);
 					for (Integer k : keys_smaller)
 						keys_larger.remove(k);
 					if (keys_larger.size() == values_larger.size()) {
 						toBeAdded.put(keys_larger, values_larger);
 						toBeDeleted.add(m1.getKey());
 						mapping_inferenced++;
 						if (verbose)
 							System.out.println("----Mapping Inference from "
 									+ m1.getKey() + "->" + m1.getValue()
 									+ " and " + m2.getKey() + "->"
 									+ m2.getValue() + " to " + keys_larger
 									+ "->" + values_larger);
 					}
 				}
 
 			}
 		}
 
 		for (Entry<HashSet<Integer>, HashSet<Integer>> mapping : toBeAdded
 				.entrySet())
 			memory.put(mapping.getKey(), mapping.getValue());
 
 		for (HashSet<Integer> keys : toBeDeleted)
 			memory.remove(keys);
 				
 		return mapping_inferenced;
 	}
 
 	void cleanEmptyMapping() {
 		Map<HashSet<Integer>, HashSet<Integer>> new_memory = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 		for (Entry<HashSet<Integer>, HashSet<Integer>> e : memory.entrySet()) {
 			if (e.getKey().size() != 0)
 				new_memory.put(e.getKey(), e.getValue());
 		}
 
 		memory = new_memory;
 	}
 
 	int mappingReduction() {
 		int mapping_reduced = 0;
 		for (Entry<HashSet<Integer>, HashSet<Integer>> mapping : memory
 				.entrySet()) {
 			HashSet<Integer> known_keys = new HashSet<Integer>();
 			HashSet<Integer> keys = mapping.getKey();
 			HashSet<Integer> old_keys = new HashSet<Integer>(keys);
 			HashSet<Integer> values = mapping.getValue();
 			HashSet<Integer> old_values = new HashSet<Integer>(values);
 			if (keys.size() == values.size()) {
 				for (Integer k : keys) {
 					int value = answers.get(k);
 					if (value != 0) {
 						known_keys.add(k);
 						values.remove(value);
 					}
 				}
 				for (Integer k : known_keys)
 					keys.remove(k);
 
 				if (known_keys.size() != 0) {
 					mapping_reduced++;
 					if (verbose)
 						System.out.println("----Mapping Reduced from:"
 								+ old_keys + "->" + old_values + " to:" + keys
 								+ "->" + values);
 				}
 			}
 		}
 		return mapping_reduced;
 	}
 
 	void mappingCleaning() {
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
 		return answers_obtained;
 	}
 
 	@Override
 	public String getID() {
 		return this.strID;
 	}
 
 	public String getAnswer() {
 		return this.answers.subList(1, answers.size()).toString();
 	}
 
 	private int basicInference() {
 		int answers_obtained = 0;
 		for (Entry<HashSet<Integer>, HashSet<Integer>> mapping : memory
 				.entrySet()) {
 			HashSet<Integer> keys = mapping.getKey();
 			HashSet<Integer> values = mapping.getValue();
 			if (keys.size() == 0)
 				continue;
 			if (values.size() == 1) {
 				int v = (Integer) values.toArray()[0];
 				for (Integer k : keys)
 					answers.set(k, v);
 				answers_obtained = keys.size();
 				if (verbose)
 					System.out.println("----I know:" + keys + "->" + v
 							+ " by basic inference");
 				keys.clear();
 			}
 		}
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
 		HashSet<Integer> tmp_uniq_keys = new HashSet<Integer>();
 
 		double exp_answer = 0;
 		this.uniq_set = new HashSet<Integer>();
 		this.m_subset = new HashMap<HashSet<Integer>, HashSet<Integer>>();
 		this.uniq_keys = new HashSet<Integer>();
 
 		if (strict == false) {
 			while (!queue.isEmpty()) {
 				Entry<HashSet<Integer>, HashSet<Integer>> mapping = queue
 						.remove().mapping;
 				HashSet<Integer> keys = mapping.getKey();
 				HashSet<Integer> values = mapping.getValue();
 				tmp_m_subset.put(keys, values);
 				tmp_uniq_keys = findUniqueKeys(tmp_m_subset);
 				tmp_uniq_set = findUniqueValues(tmp_m_subset);
 
 				double new_exp_answer = expectedAnswer(tmp_m_subset,
 						tmp_uniq_set);
 				if (new_exp_answer < exp_answer)
 					// if (tmp_uniq_set.size() <= this.uniq_set.size())
 					break;
 				this.uniq_set = new HashSet<Integer>(tmp_uniq_set);
 				this.uniq_keys = new HashSet<Integer>(tmp_uniq_keys);
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
 				uniq_keys.addAll(keys);
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
 
 	HashSet<Integer> findUniqueKeys(Map<HashSet<Integer>, HashSet<Integer>> M) {
 		HashMap<Integer, Integer> local_frequency = new HashMap<Integer, Integer>();
 		for (int i = 0; i != this.MappingLength; ++i)
 			local_frequency.put(i + 1, 0);
 		for (HashSet<Integer> km : M.keySet()) {
 			for (Integer k : km) {
 				int f = local_frequency.get(k);
 				local_frequency.put(k, f + 1);
 			}
 		}
 		HashSet<Integer> local_uniq_set = new HashSet<Integer>();
 		for (Entry<Integer, Integer> e : local_frequency.entrySet()) {
 			if (e.getValue() == 1)
 				local_uniq_set.add(e.getKey());
 		}
 		return local_uniq_set;
 	}
 
 	HashSet<Integer> findUniqueValues(Map<HashSet<Integer>, HashSet<Integer>> M) {
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
