 package listlang.objects;
 
 import java.util.LinkedList;
 
 public class List {
 	
 	private LinkedList<Integer> mList = new LinkedList<Integer>();
 	
 	public List() {
 	}
 	
 	public void print() {		
 		String str = "";
 		if(!mList.isEmpty()) {
 			str = mList.get(0).toString();
 			for(int i = 1; i < mList.size(); i++) {
 				str += ", " + mList.get(i).toString();
 			}
 		}
 		System.out.print("[" + str + "]");
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List(LinkedList<Integer> linkedList) {
 		mList = (LinkedList<Integer>) linkedList.clone();
 	}
 	
 	public List clone() {
 		return new List(this.getLinkedList());
 	}
 	
 	public LinkedList<Integer> getLinkedList() {
 		return mList;
 	}
 	
 	public void addFirst(int n) {
 		mList.addFirst(new Integer(n));
 	}
 	
 	public void addLast(int n) {
 		mList.addLast(new Integer(n));
 	}
 	
 	public void removeFirst() {
 		mList.remove(0);
 	}
 	
 	public void removeLast() {
 		mList.remove(mList.size() - 1);
 	}
 	
 	public void delete(int i) {
 		mList.remove(i);
 	}
 	
 	public boolean boolean_value() {
 		return !mList.isEmpty();
 	}
 	
 	public int to_int() {
 		return boolean_value() ? 1 : 0;
 	}
 	
 	// expressions
 	
 	public int get(int i) {
 		return mList.get(i).intValue();
 	}
 	
 	public List slice(int begin, int end) {
 		LinkedList<Integer> slicingLinkedList = this.getLinkedList();
 		for(int i = slicingLinkedList.size() - end; i > 0; i--) {
 			slicingLinkedList.remove(end);
 		}
 		for(int i = 0; i < begin; i++) {
 			slicingLinkedList.remove(0);
 		}
 		return new List(slicingLinkedList);
 	}
 	
 	public int len() {
 		return mList.size();
 	}
 
 	public int equal(List second) {
 		if(second.getLinkedList().equals(mList)) {
 			return 1;
 		} else {
 			return 0;
 		}
 	}
 	
 	public List concat(List second) {
 		List resList = this.clone();
 		for(int i = 0; i < second.len(); i++) {
 			resList.addLast(second.get(i));
 		}
 		return resList;
 	}
 	
 	public List multiply(int times) {
 		List result = this;
 		for(int i = 1; i < times; i++) {
 			result = result.concat(this);
 		}
 		return result;
 	}
 	
 	public List removeEvery(int n) {
 		List result = clone();
 		int i = 0;
 		while(i < result.len()) {
 			if(result.get(i) == n) {
 				result.delete(i);
 			} else {
 				i++;
 			}
 		}
 		return result;
 	}
 
 	public List pre_incr() {
 		addFirst(0);
 		return this;
 	}
 
 	public List pre_decr() {
 		delete(0);
 		return this;
 	}
 
 	public List post_incr() {
 		addLast(0);
 		return this;
 	}
 
 	public List post_decr() {
 		delete(len() - 1);
 		return this;
 	}
 }
