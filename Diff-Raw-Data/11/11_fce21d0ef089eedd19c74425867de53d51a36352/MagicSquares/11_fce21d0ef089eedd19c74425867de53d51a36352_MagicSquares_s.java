 import java.util.Collections;
 import java.util.Comparator;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 public class MagicSquares {
 	
 	int order;
 	int max;
 	int magic_constant;
 	long start_time;
 	boolean print_squares = true;
 	boolean eliminate_dupes = true;
 	ArrayList<MagicSquares.SquareMatrix> magic_squares = new ArrayList<MagicSquares.SquareMatrix>();
 	
 	public MagicSquares(int order) {
 		this.order = order;
 		this.max = this.order*this.order;
 		this.magic_constant = (this.order*this.order*this.order + this.order) / 2;
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
 	        
 	        System.out.println("Found "+obj.magic_squares.size()+" magic squares in "+String.format("%f", runtime_seconds)+" seconds");
 			
         } else {
             System.out.println("Usage: java MagicSquares <order>");
         }
 	}
 	
 	public class SquareMatrix {
 		int[] data;
 		int[][] equivalence_class;
 		
 		public SquareMatrix(int[] data) {
 			this.data = data;
 		}
 		
 		public SquareMatrix(int[][] data_2d) {
 			this.data = new int[max];
 			for (int m = 0; m < order; m++) 
 				for (int n = 0; n < order; n++)
 					this.data[order*m+n] = data_2d[m][n];
 		}
 		
 		public boolean is_magic_lazy() {
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
 		
 		public void rotate_right() {
 			int[][] new_data = new int[order][order];
 			for (int i = 0; i < this.data.length; i++) {
 				int m = i / order;
 				int n = i % order;
 				new_data[n][order-m-1] = this.data[i];
 			}
 			for (int m = 0; m < order; m++) 
 				for (int n = 0; n < order; n++)
 					this.data[order*m + n] = new_data[m][n];
 		}
 		
 		public void transpose() {
 			int[][] new_data = new int[order][order];
 			for (int i = 0; i < this.data.length; i++) {
 				int m = i / order;
 				int n = i % order;
 				new_data[n][m] = this.data[i];
 			}
 			for (int m = 0; m < order; m++) 
 				for (int n = 0; n < order; n++)
 					this.data[order*m + n] = new_data[m][n];
 		}
 		
 		public int[][] get_equivalence_class() {
 			MagicSquares.SquareMatrix matrix = new MagicSquares.SquareMatrix(this.data);
 			int[][] r = new int[8][max];
 			for (int i = 0; i < 8; i = i + 2) {
 				
 				for (int m = 0; m < order; m++) 
 					for (int n = 0; n < order; n++)
 						r[i][order*m + n] = matrix.data[order*m+n];
 				
 				matrix.transpose();
 				
 				for (int m = 0; m < order; m++) 
 					for (int n = 0; n < order; n++)
 						r[i+1][order*m + n] = matrix.data[order*m+n];
 				
 				matrix.transpose();
 				matrix.rotate_right();
 				
 			}
 			return r;
 		}
 		
 		public boolean equals(MagicSquares.SquareMatrix comp_matrix) {
 			if (this.equivalence_class == null)
 				this.equivalence_class = this.get_equivalence_class();
 			for (int i = 0; i < equivalence_class.length; i++)
 				if (Arrays.equals(comp_matrix.data, equivalence_class[i]))
 					return true;
 			return false;
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
 		ArrayList<Thread> threads = new ArrayList<Thread>();
 		
 		public MagicTree(SumPermutationsList sum_permutations_list) {
 			this.sum_permutations_list = sum_permutations_list;
 			
 			ArrayList<int[]> permutations_list_data = sum_permutations_list.get_all_data();
 			ArrayList<int[]> root_permutations_list = new ArrayList<int[]>();
 			
 			for (int i = 0; i < permutations_list_data.size(); i++) {
 				boolean add_it = true;
 				for (int j = 0; j < root_permutations_list.size(); j++) {
 					if (Arrays.equals(permutations_list_data.get(i), arr_reverse(root_permutations_list.get(j)))) {
 						add_it = false;
 						break;
 					}
 				}
 				if (add_it)
 					root_permutations_list.add(permutations_list_data.get(i));
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
 		
 		public class NodeBuilderThread extends Thread {
 			ArrayList<MagicTreeNode> nodes;
 			public NodeBuilderThread(ArrayList<MagicTreeNode> nodes) {
 				this.nodes = nodes;
 			}
 			public void run() {
 				for(MagicTreeNode node: this.nodes) {
 					node.build();
 					root.children.remove(node);
 				}
 			}
 		}
 		
 		public void build_tree() {
 			//Iterator<MagicTreeNode> child_iterator = this.root.children.iterator();
 			/*while (child_iterator.hasNext()) {
 				MagicTreeNode node = (MagicTreeNode) child_iterator.next();
 				threads.add(new NodeBuilderThread(node));
 			}*/
 			int num_threads = 8;
 			int num_nodes = this.root.children.size();
 			int nodes_per_thread =  num_nodes / num_threads;
 			int i = 0;
 			for (int j = 0; j < num_threads; j++) {
 	        	int a = i;
 	        	int b = Math.min(i + nodes_per_thread, num_nodes-1);
 	        	ArrayList<MagicTreeNode> sub_list = new ArrayList<MagicTreeNode>();
 	        	for (int k = a; k <= b; k++) {
 	        		sub_list.add(this.root.children.get(k));
 	        	}
 	        	Thread t = new NodeBuilderThread(sub_list);
 	        	threads.add(t);
	        	t.start();
 	        	if (b >= num_nodes-1)
 	        		break;
 	        	i += nodes_per_thread + 1;
 	        }
 			
			/*for (Thread t: this.threads) {
 				t.start();
			}*/
 			try {
 				for (Thread t: this.threads) {
 					t.join();
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		public class MagicTreeNode {
 			public int[] data;
 			public ArrayList<MagicTreeNode> children = new ArrayList<MagicTreeNode>();
 			public boolean is_row;
 			public int index;
 			public MagicTreeNode parent;
 			
 			public MagicTreeNode() {};
 			
 			public MagicTreeNode(int[] data, boolean is_row, int index, MagicTreeNode parent) {
 				this.data = data;
 				this.is_row = is_row;
 				this.index = index;
 				this.parent = parent;
 			}
 			
 			public MagicTreeNode add_child(int[] data) {
 				boolean child_is_row = this.data == null ? true : !this.is_row;
 				int child_index = this.data == null ? 0 : (this.is_row ? this.index : this.index+1);
 				MagicTreeNode child = new MagicTreeNode(data, child_is_row, child_index, this);
 				this.children.add(child);
 				return child;
 			}
 			
 			public int[] get_row(int m) {
 				int[] r = new int[this.index+1];
 				MagicTreeNode current_node = this;
 				while (current_node.data != null) {
 					if (current_node.is_row != true) {
 						r[current_node.index] = current_node.data[m];
 					}
 					current_node = current_node.parent;
 				}
 				return r;
 			}
 			
 			public int[] get_column(int n) {
 				int[] r = new int[this.index+1];
 				MagicTreeNode current_node = this;
 				while (current_node.data != null) {
 					if (current_node.is_row == true)
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
 					if (current_node.is_row) {
 						matrix_data[current_node.index] = current_node.data;
 					}
 					current_node = current_node.parent;
 				}
 				return new SquareMatrix(matrix_data);
 			}
 			
 			public void build() {
 				Set<Integer> forbidden_elements = this.get_elements();
 				
 				int[] child_begin = new int[] {};
 				if (this.is_row) {
 					if (this.index == order-1) {
 						// this is a potentially magic square
 						SquareMatrix matrix = this.to_matrix();
 						if (matrix.is_magic_lazy())
 							handle_magic_matrix(matrix);
 						
 						return;
 					} else {
 						child_begin = this.get_column(this.index);
 					}
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
 				Iterator<MagicTreeNode> child_iter = this.children.iterator();
 				while (child_iter.hasNext()) {
 					((MagicTreeNode) child_iter.next()).build();
 					child_iter.remove();
 				}
 				
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
 		boolean valid = false;
 		if (eliminate_dupes) {
 			boolean is_unique = true;
 			for (int r = 0; r < magic_squares.size(); r++) {
 				if (matrix.equals(magic_squares.get(r)))
 					is_unique = false;
 			}
 			if (is_unique)
 				valid = true;
 		} else {
 			valid = true;
 		}
 		
 		if (valid) {
 			magic_squares.add(matrix);
 			if (print_squares) {
 				long time = System.currentTimeMillis();
 				String str = new String("["+(time-start_time)+"]: Magic Square #" + magic_squares.size()+"\n");
 				str += matrix.toString();
 				System.out.println(str);
 			}
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
