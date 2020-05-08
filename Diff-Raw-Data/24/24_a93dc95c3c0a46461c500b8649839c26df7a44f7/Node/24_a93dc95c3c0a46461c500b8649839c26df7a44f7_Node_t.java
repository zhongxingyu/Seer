 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package isad.w6.practicum.o1;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author jappie
  */
 public class Node<T> extends isad.w4.tree.binary.Node {
 	private int _x, _y;
 	public Node(T data){
 		this(data, null);
 	}
 	public Node(T data, Node<T> lft){
 		this(data, lft, null);
 	}
 	public Node(T data, Node<T> lft, Node<T> rght){
 		super(data, lft, rght);
 	}
 	/**
 	 * @return the _x
 	 */
 	public int getX() {
 		return _x;
 	}
 
 	/**
 	 * @param x the _x to set
 	 */
 	public void setX(int x) {
 		this._x = x;
 	}
	public void calcY(int y){
		setY(y);
 		if(hasLft()){
			((Node<T>) getLft()).calcY(y+1);
 		}
 		if(hasRght()){
			((Node<T>) getRght()).calcY(y+1);
 		}
 		
 	}
 	public int calcX( int offset){
 		int result = setChildX(getLft(), offset)  + 1;
 		int rght = setChildX(getRght(), result);
 		setX(result);
 		result += (rght-result);
 		return result;
 				
 	}
 	private int setChildX(isad.w4.tree.binary.Node<T> target, int offset){
 		if(target != null){
 			return ((Node<T>)target).calcX(offset);
 		}
 		return offset;
 	}
 	public String printPreWithCoordinates(){
 		return toString() + "(" + getX() +"," + getY() +")"
 				+ (hasLft() ?  ((Node<T>)getLft()).printPreWithCoordinates()  : "") 
 				+ (hasRght() ? ((Node<T>)getRght()).printPreWithCoordinates() : "");
 
 	}
 	/**
 	 * @return the _y
 	 */
 	public int getY() {
 		return _y;
 	}
 
 	/**
 	 * @param y the _y to set
 	 */
 	public void setY(int y) {
 		this._y = y;
 	}
 	
 	public List<Node<T>> getNodes(){
 		ArrayList<Node<T>> list = new ArrayList<Node<T>>();
 		if(hasLft()){
 			list.add((Node<T>) this.getLft());
 			list.addAll(((Node<T>) this.getLft()).getNodes());
 		}
 		if(hasRght()){
 			list.add((Node<T>) this.getRght());
 			list.addAll(((Node<T>) this.getRght()).getNodes());
 		}
 		return list;
 		
 	}
 }
