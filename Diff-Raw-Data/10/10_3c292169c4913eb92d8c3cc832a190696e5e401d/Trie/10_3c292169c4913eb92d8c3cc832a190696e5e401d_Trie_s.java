 import java.util.ArrayList;
 
 public class Trie {
 	
 //	===========================================================================
 //							START OF NODE CLASS
 	private class Node {
 		
 		private char letter;
 		//marks the end of one word
 		private boolean wordEnd;
 		private Node child_list[];
 		int nr;
 		
 		public Node() {
 			letter = ' ';
 			//any node can have maximum 26 descendes (26 letters in the dictionary)
			child_list = new Node[26];
 			wordEnd = false;
 			nr = 0;
 		}
 		
 		public int getNrOfChild() {
 			return nr;
 		}
 		
 		public char getValue() {
 			return this.letter;
 		}
 		
 		//add a child at the end of the child_list
 		public Node addChild(char letter, boolean end) {
 			int index = getNrOfChild();
 			Node new_node = new Node();
 			new_node.letter = letter;
 			new_node.wordEnd = end;
 			child_list[index] = new_node;
 			nr ++;
 			return child_list[getNrOfChild() - 1];
 		}
 		
 		private Node addNode(char letter, boolean end) {
 			if (this.getNrOfChild() == 0) {
 				return addChild(letter, end);
 			}
 			int index;
 			if (this.getNrOfChild() == 1) {
 				//if the new letter is grater than the only child
 				//we have to add the new letter to the end of the child list
 				if (child_list[0].getValue() < letter) {
 					return addChild(letter, end);
 				} else {
 					//the index to be inserted is 0
 					index = 0;
 				}
 			}
 			//index to insert before
 			index = binarySearchForIndex(letter, 0, getNrOfChild() - 1);
 			//shift right with 1 the child_list to add the new node
 			for (int i = getNrOfChild(); i > index; i --) {
 				child_list[i] = child_list[i - 1];
 			}
 			Node new_node = new Node();
 			new_node.letter = letter;
 			new_node.wordEnd = end;
 			child_list[index] = new_node; 
 			nr ++;
 			return child_list[index];
 		}
 		
 		//binary search for the right position where the new letter has to be
 		//inserted
 		private int binarySearchForIndex(char letter, int start, int end) {
 			if (start == end) {
 				return start;
 			}
 			int mid = start + (end - start) / 2;
 			char mid_letter = child_list[mid].getValue();
 			if (mid_letter < letter && child_list[mid + 1].getValue() > letter) {
 				return mid;
 			} else 	if (mid_letter < letter) {
 				return binarySearchForIndex(letter, start, mid);
 			} else {
 				return binarySearchForIndex(letter, mid + 1, end);
 			}
 		}
 		
 		public Node[] getChildList() {
 			return this.child_list;
 		}
 		
 		//binary search for the node with the given value
 		private Node searchValue(char value) {
 			//set the left side, right side and middle for the binary search
 			int l = 0;
 			int r = getNrOfChild() - 1;
 			//if there is more then one element
 			//binary search for the 
 			if (r >= 0) {
 				while(l <= r) {
 					if (l == r) {
 						if (child_list[l].getValue() == value) {
 							return child_list[l];
 						} else {
 							return null;
 						}
 					}
 					int m = l + (r - l)/2;
 					if (child_list[m].getValue() == value) {
 						return child_list[m];
 					} else if (child_list[m].getValue() < value) {
						r = m - 1;
					} else {
 						l = m + 1;
 					}
 					m = l + (r - l)/2;
 				}
 			}
 			return null;
 		}
 	}
 //								END OF NODE CLASS
 //	===========================================================================
 	private Node root;
 	private boolean ordered;
 	
 	public Trie(boolean ordered) {
 		root = new Node();
 		this.ordered = ordered;
 	}
 
 	public void addWord(String word) {
 		/*Search for every letter in the word starting from root
 		 * if the letter is not found, add letter to root child list
 		 * the root is now the new added letter
 		 * if the letter is found, set the new root and move on to the next letter
 		 * of the word
 		 * */
 		Node parent = root;
 		Node child;
 		for (int i = 0; i < word.length(); i++) {
 			child = parent.searchValue(word.charAt(i));
 			if (child == null) {
 				//if it's the last letter of the word end will be set true
 				boolean end = (i == word.length() - 1)? true : false;
 				if (ordered == true)
 					parent = parent.addChild(word.charAt(i), end);
 				else
 					parent = parent.addNode(word.charAt(i), end);
 			} else {
 				//moving to the next level in the trie
 				parent = child;
 			}
 		}		
 	}
 	
 	public Node getRoot() {
 		return root;
 	}
 	
 	public Node getLastNodeOfPrefix(String prefix) {
 		Node elem = root;
 		for (int i = 0; i < prefix.length(); i++) {
 			elem = elem.searchValue(prefix.charAt(i));
 		}
 		return elem;
 	}
 	
 	public void showTrie(int level, Node root) {
 		Node[] list = new Node[26];
 		list = root.getChildList();
 		root = root.getChildList()[0];
 		list = list[0].getChildList();
 		for (int i = 0; i < root.getNrOfChild(); i++) {
 			
 //			showTrie(level + 1, list[i]);
 			System.out.println(list[i].getValue() + " " + level + " " + list[i].wordEnd);
 		}
 	}
 	
 	public void getWords(Node _root, String prefix, ArrayList<String> words) {
 		//if the current node represent the end of one root add the prefix to
 		//the word list
 		prefix += _root.letter;
 		if(_root.wordEnd == true) {
 			words.add(prefix);
 		}
 		//if the current node has no more children, it's the
 		//of the poor prefix
 		if(_root.nr == 0) {
 			return;
 		}
 		//recursively find all the other words with the new prefix
 		Node[] childList = _root.getChildList();
 		for (int i = 0; i < _root.nr; i++) {
 			getWords(childList[i], prefix, words);
 		}
 	}
 	
 	public void trial() {
 		ArrayList<String> words = new ArrayList<>();
 		String prefix = "an";
 		Node _root = getLastNodeOfPrefix("ana");
 		getWords(_root, prefix, words);
 		for (int i = 0; i < words.size(); i++) {
 			System.out.println(words.get(i));
 		}
 	}
 	
 	public ArrayList<String> getAllWords(String prefix) {
 		ArrayList<String> words = new ArrayList<>();
 		Node _root = getLastNodeOfPrefix(prefix);
 		//delete the last letter of the prefix because it will be added twice
 		//in each word
 		prefix = prefix.substring(0, prefix.length() - 1);
 		getWords(_root, prefix, words);
 		return words;
 	}
 }
