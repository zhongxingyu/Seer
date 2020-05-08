 package project_385;
 
 public class ADTStack<T> {
 
 	private Node<T> top;
 	
 	public ADTStack() {
 		top=null;
 	}
 
 	public boolean isEmpty() {	//returns true if the list is empty, false if otherwise
 		return (top==null);
 	}
 	
 	public void push(T newItem){
 		top=new Node<T>(newItem, top);
 	}
 	
 	public T pop(){
 		Node<T>tmp=top;
 		if(tmp!=null){
 			top=top.getNext();
 			return(tmp.getItem());
 		}else{
 			return(null);
 		}	
 	}
 	
 	public T peek(){
 		if(isEmpty()){
 			return null;
 		}
 		return top.getItem();
 	}
 	
 	public void clear(){
 		top=null;
 	}
 	
 	public String toString() {
 		StringBuilder buffer = new StringBuilder(']');
                buffer.append('[');
                 for (Node<T> curNode = top; curNode != null; curNode = curNode.getNext()) {
                	buffer.append(curNode.getItem());
                        if (curNode.getNext() != null)
                 		buffer.append(',');
                 }
        	buffer.append(']');
                return buffer.toString();
 	}
 }
