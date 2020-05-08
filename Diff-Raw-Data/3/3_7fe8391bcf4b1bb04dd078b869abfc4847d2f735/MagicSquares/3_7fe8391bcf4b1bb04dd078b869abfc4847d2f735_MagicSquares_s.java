 import java.util.Collections;
 import java.util.Comparator;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.ForkJoinPool;
 import java.util.concurrent.RecursiveTask;
 
 public class MagicSquares {
 	
 	int order;
 	int max;
 	int magic_constant;
 	long start_time;
 	boolean print_squares = true;
 	int count = 0;
 	Comparator<int[]> int_arr_comparator;
 
 	public MagicSquares(int order) {
 		this.order = order;
 		this.max = this.order*this.order;
 		this.magic_constant = (this.order*this.order*this.order + this.order) / 2;
 		this.int_arr_comparator = new Comparator<int[]>() {
 			public int compare(int[] arr1, int[] arr2) {
 				for (int i = 0; i < arr1.length; i++) {
 					if (arr1[i] != arr2[i]) {
 						return (arr1[i] > arr2[i] ? 1 : -1);
 					}
 				}
 				return 0;
 			}
 		};
 	}
 	
 	public static void main(String[] args) {
 		if (args.length > 0) {
 			
 			int order = Integer.parseInt(args[0]);
 			MagicSquares obj = new MagicSquares(order);
 			
 			System.out.println("Finding all magic matricies of order " + order);
 			
 			obj.init_magic_tree();
 			
 			long end_time = System.currentTimeMillis();
 	        long runtime = end_time - obj.start_time;
 	        double runtime_seconds = (double) runtime / (double) 1000;
 	        
 	        System.out.println(String.format("%f", runtime_seconds)+" seconds");
 			
         } else {
             System.out.println("Usage: java MagicSquares <order>");
         }
 	}
 	
 	public final class SquareMatrix {
 		final int[] data;
 		final int[] equivalent_data;
 		
 		public SquareMatrix(int[] data) {
 			this.data = data;
 			this.equivalent_data = this.get_equivalence_class().get(0);
 		}
 		
 		public SquareMatrix(int[][] data_2d) {
 			this.data = new int[max];
 			for (int m = 0; m < order; m++) 
 				for (int n = 0; n < order; n++)
 					this.data[order*m+n] = data_2d[m][n];
 			this.equivalent_data = this.get_equivalence_class().get(0);
 		}
 		
 		public boolean is_magic_lazy() {
 			int right_diagonal_sum = 0;
 			for (int i = 0; i < order; i++)
 				right_diagonal_sum += this.data[order*i+order-i-1];
 			if (right_diagonal_sum != magic_constant)
 				return false;
 			
 			return true;
 		}
 		
 		public boolean is_magic() {
 			
 			for (int m = 0; m < order; m++) {
 				int row_sum = 0;
 				for (int n = 0; n < order; n++)
 					row_sum += this.data[order * m +n];
 				if (row_sum != magic_constant)
 					return false;
 			}
 			
 			for (int n = 0; n < order; n++) {
 				int col_sum = 0;
 				for (int m = 0; m < order; m++)
 					col_sum += this.data[order * m + n];
 				if (col_sum != magic_constant)
 					return false;
 			}
 			
 			int left_diagonal_sum = 0;
 			for (int i = 0; i < order; i++)
 				left_diagonal_sum += this.data[order*i+i];
 			if (left_diagonal_sum != magic_constant)
 				return false;
 			
 			int right_diagonal_sum = 0;
 			for (int i = 0; i < order; i++)
 				right_diagonal_sum += this.data[order*i+order-i-1];
 			if (right_diagonal_sum != magic_constant)
 				return false;
 			
 			return true;
 		}
 		
 		public String toString() {
 			// old_data[m][n] = new_data[order*m + n]
 			String max_term = max + "";
 			int max_term_size = max_term.length();
 			
 			String result = "";
 			
 			String border = "+";
 			String border_between = "|";
 			for (int i = 0; i < order; i++) {
 				border += "--" + MagicSquares.str_repeat("-", max_term_size);
 				border_between += "--" + MagicSquares.str_repeat("-", max_term_size);
 				if (i != order - 1 ) {
 					border += "-";
 					border_between += "+";
 				}
 			}
 			border += "+\n";
 			border_between += "|\n";
 			
 			result += border;
 			
 			for (int m = 0; m < order; m++) {
 				result += "|";
 				for (int n = 0; n < order; n++) {
 					int padding_right = max_term_size - (data[order*m+n] + "").length();
 					result += " " + data[order*m+n] + MagicSquares.str_repeat(" ", padding_right)+ " |";
 				}
 				result += "\n";
 				if (m != order - 1)
 					result += border_between;
 			}
 			result += border;
 			return result;
 		}
 		
 		public void rotate_right(int[] data2) {
 			int[] new_data = new int[max];
 			for (int i = 0; i < data2.length; i++) {
 				int m = i / order;
 				int n = i % order;
 				new_data[order*n+order-m-1] = data2[i];
 			}
 			for (int i = 0; i < max; i++) 
 				data2[i] = new_data[i];
 		}
 		
 		public void transpose(int[] data2) {
 			int[] new_data = new int[max];
 			for (int i = 0; i < data2.length; i++) {
 				int m = i / order;
 				int n = i % order;
 				new_data[order*n+m] = data2[i];
 			}
 			for (int i = 0; i < max; i++) 
 				data2[i] = new_data[i];
 		}
 		
 		public ArrayList<int[]> get_equivalence_class() {
 			ArrayList<int[]> r = new ArrayList<int[]>();
 			
 			int[] data_copy = this.data.clone();
 			for (int i = 0; i < 4; i++) {
 				r.add(data_copy.clone());
 				rotate_right(data_copy);
 			}
 			transpose(data_copy);
 			for (int i = 0; i < 4; i++) {
 				r.add(data_copy.clone());
 				rotate_right(data_copy);
 			}
 			
 			Collections.sort(r, int_arr_comparator);
 			return r;
 		}
 		
 		@Override
 		public boolean equals(Object other) {
 			return this.hashCode() == ((SquareMatrix) other).hashCode() && 
 				Arrays.equals(this.equivalent_data, ((SquareMatrix) other).equivalent_data);
 		}
 		
 		@Override
 		public int hashCode() {
 			return Arrays.hashCode(this.equivalent_data);
 		}
 
 	}
 	
 	private static String str_repeat(String str, int repeat) {
 		String result = "";
 		for (int i = 0; i < repeat; i++)
 			result += str;
 		return result;
 	}
 	
 	public class MagicTree {
 		MagicTreeNode root = new MagicTreeNode();
 		SumPermutationsList sum_permutations_list;
 		
 		public MagicTree(SumPermutationsList sum_permutations_list) {
 			this.sum_permutations_list = sum_permutations_list;
 			
 			ArrayList<int[]> permutations_list_data = sum_permutations_list.get_all_data();
 			SortedSet<int[]> root_permutations_list = new TreeSet<int[]>(int_arr_comparator);
 			
 			for (int i = 0; i < permutations_list_data.size(); i++) {
 				if (!root_permutations_list.contains(arr_reverse(permutations_list_data.get(i)))) {
 					root_permutations_list.add(permutations_list_data.get(i));
 				}
 			}
 			for (int[] p: root_permutations_list)
 				root.add_child(p);
 		}
 		
 		public int[] arr_reverse(int[] arr) {
 			int[] r = new int[arr.length];
 			for (int i = 0; i < arr.length; i++) {
 				r[arr.length-1-i] = arr[i];
 			}
 			return r;
 		}
 		
 		public class NodeBuilderTask extends RecursiveTask<Set<SquareMatrix>> {
 	
 			private static final long serialVersionUID = 7218910311926378380L;
 			
 			private List<MagicTreeNode> nodes;
 			public Set<SquareMatrix> result = new HashSet<SquareMatrix>();
 			
 			public NodeBuilderTask(List<MagicTreeNode> nodes) {
 				this.nodes = nodes;
 			}
 			
 			@Override
 			public Set<SquareMatrix> compute() {
 				if (nodes.size() <= 500) {
 					for (MagicTreeNode node: nodes) {
 						node.get_children();
 						result.addAll(node.build());
 					}
 				} else {
 					
 					int half = nodes.size() / 2;
 					
 					List<MagicTreeNode> upper_half = nodes.subList(0, half);
 					List<MagicTreeNode> lower_half = nodes.subList(half, nodes.size());
 					
 					NodeBuilderTask worker1 = new NodeBuilderTask(upper_half);
 					NodeBuilderTask worker2 = new NodeBuilderTask(lower_half);
 					
 					worker1.fork();
					result.addAll(worker1.join());
 					result.addAll(worker2.compute());
 				}
 				return result;
 			}
 		}
 		
 		public void build_tree() {
 			int processors = Runtime.getRuntime().availableProcessors();
 			
 			NodeBuilderTask task = new NodeBuilderTask(root.children);
 			ForkJoinPool pool = new ForkJoinPool(processors);
 			pool.invoke(task);
 			
 			Set<SquareMatrix> magic_squares = task.result;
 			System.out.println("Computed Result: " + magic_squares.size());
 		}
 		
 		public class MagicTreeNode {
 			public int[] data;
 			public ArrayList<MagicTreeNode> children = new ArrayList<MagicTreeNode>();
 			public int type; // 0 for root, 1 for main diagonal, 2 for row, 3 for column.
 			public int index;
 			public MagicTreeNode parent;
 			
 			public MagicTreeNode() {};
 			
 			public MagicTreeNode(int[] data, int type, int index, MagicTreeNode parent) {
 				this.data = data;
 				this.type = type;
 				this.index = index;
 				this.parent = parent;
 			}
 			
 			public MagicTreeNode add_child(int[] data) {
 				int child_type = 0;
 				int child_index = 0;
 				switch (this.type) {
 					case 0: 
 						child_type = 1;
 						child_index = -1;
 					break;
 					case 1: 
 						child_type = 2;
 						child_index = 0;
 					break;
 					case 2: 
 						child_type = 3;
 						child_index = this.index;
 					break;
 					case 3: 
 						child_type = 2;
 						child_index = this.index+1;
 					break;
 					default: 
 						child_type = -42;
 						child_index = -42;
 				}
 				MagicTreeNode child = new MagicTreeNode(data, child_type, child_index, this);
 				this.children.add(child);
 				return child;
 			}
 			
 			public int[] get_main_diagonal() {
 				MagicTreeNode current_node = this;
 				while (current_node.data != null) {
 					if (current_node.type == -1)
 						return current_node.data;
 					current_node = current_node.parent;
 				}
 				return null;
 			}
 			
 			public int[] get_row(int m) {
 				int[] r = new int[this.index+2];
 				MagicTreeNode current_node = this;
 				while (current_node.data != null) {
 					if (current_node.type == 3) 
 						r[current_node.index] = current_node.data[m];
 					else if (current_node.type == 1)
 						r[m] = current_node.data[m];
 					current_node = current_node.parent;
 				}
 				return r;
 			}
 			
 			public int[] get_column(int n) {
 				int[] r = new int[this.index+1];
 				MagicTreeNode current_node = this;
 				while (current_node.data != null) {
 					if (current_node.type == 2)
 						r[current_node.index] = current_node.data[n];
 					current_node = current_node.parent;
 				}
 				return r;
 			}
 		
 			public Set<Integer> get_elements() {
 				Set<Integer> r = new HashSet<Integer>();
 				MagicTreeNode current_node = this;
 				while (current_node.data != null) {
 					for (int e: current_node.data) {
 						r.add(e);
 					}
 					current_node = current_node.parent;
 				}
 				return r;
 			}
 			
 			public SquareMatrix to_matrix() {
 				int[][] matrix_data = new int[order][order];
 				MagicTreeNode current_node = this;
 				while (current_node.data != null) {
 					if (current_node.type == 2) {
 						matrix_data[current_node.index] = current_node.data;
 					}
 					current_node = current_node.parent;
 				}
 				matrix_data[order-1] = this.get_row(order-1);
 				return new SquareMatrix(matrix_data);
 			}
 			
 			public void get_children() {
 				Set<Integer> forbidden_elements = this.get_elements();
 				
 				int[] child_begin = new int[] {};
 				if (this.type == 1) {
 					child_begin = new int[] {this.data[0]};
 				} else if (this.type == 2) {
 					child_begin = this.get_column(this.index);
 				} else {
 					child_begin = this.get_row(this.index+1);
 				}
 				
 				for (int i: child_begin) {
 					forbidden_elements.remove(i);
 				}
 				ArrayList<int[]> child_possibilities = sum_permutations_list.query(child_begin, forbidden_elements);
 				for (int i = 0; i < child_possibilities.size(); i++) {
 					this.add_child(child_possibilities.get(i));
 				}			
 			}
 			
 			public Set<SquareMatrix> build() {
 				Set<SquareMatrix> r = new HashSet<SquareMatrix>();
 				if (order == 1 || (this.type == 3 && this.index == order-2)) {
 					// this is a potentially magic square
 					SquareMatrix matrix = this.to_matrix();
 					if (matrix.is_magic_lazy()) {
 						handle_magic_matrix(matrix);
 						r.add(matrix);
 					}
 				} else {
 					
 					while (this.children.size() > 0) {
 						MagicTreeNode child = this.children.get(0);
 						child.get_children();
 						r.addAll(child.build());
 						this.children.remove(child);
 					}
 					
 				}
 				return r;
 			}
 			
 		}
 	}
 	public void init_magic_tree() {
 		this.start_time = System.currentTimeMillis();
 		SumPermutationsList sum_permutations_list = this.new SumPermutationsList();
 		
 		MagicTree magic_tree = this.new MagicTree(sum_permutations_list);
 		
 		magic_tree.build_tree();
 	}
 	
 	public void handle_magic_matrix(SquareMatrix matrix) {
 		this.count++;
 		if (this.print_squares) {
 			long time = System.currentTimeMillis();
 			String str = new String("["+(time-start_time)+"]: Magic Square #" + count+"\n");
 			str += matrix.toString();
 			System.out.println(str);
 		}
 	}
 
 	public class SumPermutationsList {
 		public ArrayList<int[]> data;
 		
 		public SumPermutationsList() {
 			this.data = get_sum_combinations();
 		}
 		
 		public ArrayList<int[]> get_all_data() {
 			return this.data;
 		}
 		
 		public boolean arr_begins_with(int[] arr, int[] init) {
 			for (int i = 0; i < init.length; i++) {
 				if (init[i] != arr[i])
 					return false;
 			}
 			return true;
 		}
 		
 		public boolean arr_disjoint(int[] arr, Set<Integer> exclusion_set) {
 			for (int i = 0; i < arr.length; i++) {
 				if (exclusion_set.contains(arr[i]))
 					return false;
 			}
 			return true;
 		}
 		
 		public ArrayList<int[]> query(int[] init) {
 			return query(init, new HashSet<Integer>());
 		}
 		
 		public ArrayList<int[]> query(int[] init, Set<Integer> exclusion_set) {
 			ArrayList<int[]> r = new ArrayList<int[]>();
 			
 			int found_index = Collections.binarySearch(this.data, init, new Comparator<int[]>() {
 				public int compare(int[] arr, int[] init) {
 					for (int i = 0; i < init.length; i++) {
 						if (arr[i] != init[i]) {
 							return (arr[i] > init[i] ? 1 : -1);
 						}
 					}
 					return 0;
 				}
 			});
 			
 			int i = found_index;
 			
 			if (i >= 0) {
 				while (i >= 0) {
 					if (arr_begins_with(this.data.get(i), init)) {
 						if (arr_disjoint(this.data.get(i), exclusion_set))
 							r.add(this.data.get(i));
 					} else {
 						break;
 					}
 					i--;
 				}
 				
 				i = found_index + 1;
 				while (i < this.data.size()) {
 					if (arr_begins_with(this.data.get(i), init)) {
 						if (arr_disjoint(this.data.get(i), exclusion_set))
 							r.add(this.data.get(i));
 					} else {
 						break;
 					}
 					i++;
 				}
 			}
 			
 			return r;
 		}
 	}
 	
 	public ArrayList<int[]> get_sum_combinations() {
 		
 		ArrayList<Integer> elements = new ArrayList<Integer>();
 		for (int i = 0; i < max; i++) {
 			elements.add(i+1);
 		}
 		
 		ArrayList<int[]>r = get_sum_combinations_recursive(elements, order, magic_constant);
 		
 		return r;
 	}
 	
 	public ArrayList<int[]> get_sum_combinations_recursive(ArrayList<Integer> elements, int length, int sum) {
 		ArrayList<int[]>r = new ArrayList<int[]>();
 		
 		if (length == 1) {
 			for (int i = 0; i < elements.size(); i++) {
 				if (elements.get(i).equals(sum)) {
 					int[] base_answer = new int[1];
 					base_answer[0] = elements.get(i);
 					r.add(base_answer);
 					return r;
 				} 
 			}
 			return null;
 		}
 		
 		for (int i = 0; i < elements.size(); i++) {
 			int e = elements.get(i);
 			ArrayList<Integer> sub_sequence = new ArrayList<Integer>();
 			for (Integer j: elements) {
 				if (j != e)
 					sub_sequence.add(j);
 			}
 			
  			ArrayList<int[]> sub_combinations = get_sum_combinations_recursive(sub_sequence, length-1, sum-e);
  			if (sub_combinations != null) {
 				for (int j = 0; j < sub_combinations.size(); j++) {
 					int[] r2 = new int[length];
 					r2[0] = e;
 					int[] arr = sub_combinations.get(j);
 					for (int k = 0; k < arr.length; k++) {
 						r2[k+1] = arr[k];
 					}
 					r.add(r2);
 				}
  			}
 		}
 		return r;
 	}
 
 }
