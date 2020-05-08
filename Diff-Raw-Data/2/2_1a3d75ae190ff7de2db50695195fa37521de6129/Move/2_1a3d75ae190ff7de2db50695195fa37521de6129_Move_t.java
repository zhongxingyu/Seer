 package model;
 import java.util.*;
 public class Move {
 	private Map<Position, Position> own_positions;
 	private Map<Position, Position> other_positions;
 	private int direction;
 	private boolean removed;
 	private boolean parallel;
 	private Position position_removed;
 
 	public Move(){
 		own_positions= new HashMap<Position, Position>();
 		other_positions= new HashMap<Position, Position>();
 		removed=false;
 		parallel=false;
 	}
 	public Move(int d){
 		direction = d;
 		own_positions= new HashMap<Position, Position>();
 		other_positions= new HashMap<Position, Position>();
 		removed=false;
 		parallel=false;
 	}
 
 	public Move(Set<Position> l, int d){
 //		System.out.println(l + " " + d);
 		direction=d;
 		own_positions= new HashMap<Position, Position>();
 		other_positions= new HashMap<Position, Position>();
 		for (Position p:  l){
 			
 			own_positions.put(p, p.get_neighbour(d));
 		}
 		parallel=false;
 		removed=false;
 	}
 
 	public void add_position(Position p){
 		own_positions.put(p, p.get_neighbour(direction));
 	}
 
 	public void remove_position(Position p){
 		if (own_positions.containsKey(p)) { own_positions.remove(p);}
 	}
 
 	public void add_other_position(Position p){
 //		other_positions.put(p, null);
 //		System.out.println(p);
 //		System.out.println(p.get_neighbour(direction));
 		other_positions.put(p, p.get_neighbour(direction));
 	}
 
 	public void remove_other_position(Position p){
 		if (other_positions.containsKey(p)) { other_positions.remove(p);}
 	}
 
     public int get_direction(){
         return direction;
     }
 
 	public void set_direction(int d){
 		direction=d;
 	}
 
 	public boolean is_pushed(){
		return !other_positions.isEmpty();
 	}
 
 	public void set_removed(boolean r){
 		removed=r;
 	}
 
 	public boolean is_removed(){
 		return removed;
 	}
 
 	public void set_parallel(boolean l){
 		parallel=l;
 	}
 
 	public boolean is_parallel(){
 		return parallel;
 	}
 
 	public Map<Position, Position> get_own_positions(){
 		return own_positions;
 	}
 	public Map<Position, Position> get_other_positions(){
 		return other_positions;
 	}
 	
 	public void set_position_removed(Position pos){
 		position_removed=pos;
 	}
 	
 	public Position get_position_removed(){
 		return position_removed;
 	}
 	
 	
 	public Set<Position> get_affected_positions(){
 		Set<Position> setpos = new HashSet<Position>();
 //		System.out.println("OWN POSITIONS : " + own_positions);
 		setpos.addAll(own_positions.values());
 		setpos.addAll(own_positions.keySet());
 //		System.out.println("OTHER POSITIONS : " + other_positions);
 		setpos.addAll(other_positions.keySet());
 		setpos.addAll(other_positions.values());
 		if (setpos.contains(null)) { setpos.remove(null);}
 		return setpos;
 	}
 	
 	public Move get_inverse(){
 		int d = (this.direction+3)%6; if (d==0){d=6;}
 		Set<Position> set_pos = new HashSet<Position>();
 		set_pos.addAll(this.get_own_positions().values());
 		Move m = new Move(set_pos,d);
 		Set<Position> set_pos2 = new HashSet<Position>();
 		set_pos2.addAll(this.get_other_positions().values());
 		for (Position pos : set_pos2){
 			if (pos!=null){ 
 				m.add_other_position(pos);
 				}
 			
 		}
 		if (this.is_removed()) { 
 			m.set_removed(true); 
 			m.set_position_removed(this.get_position_removed()); 
 		}
 		
 		return m;
 	}
 	
 	
 	public static void main(String[] args){
 
 		Position p = new Position(3,2);
 //		System.out.println(p.get_neighbour(4).get_neighbour(4));
 		Set<Position> l = new TreeSet<Position>();
 		l.add(p);
 		l.add(p.get_neighbour(3));
 		l.add(p.get_neighbour(3).get_neighbour(3));
 		Move m = new Move(l,6);
 		System.out.println(m.own_positions);
 		System.out.println(m.get_inverse().own_positions);
                 m.add_other_position(new Position(1,1));
 //		m.own_positions.put(new Position(1,2), null);
 //		m.add_other_position(p.get_neighbour(6));
 //		System.out.println(p.get_neighbour(6));
 //		m.add_position(p);
 //		m.add_position(p.get_neighbour(3));
 //		m.add_position(p.get_neighbour(3).get_neighbour(3));
 //		System.out.println(m.own_positions);
 //		System.out.println(m.other_positions);
 		System.out.println(4%6);
 		System.out.println(5%6);
 		System.out.println(6%6);
 		System.out.println(7%6);
 		System.out.println(8%6);
 		System.out.println(9%6);
 //		System.out.println(10%6);
 	}
 
 
 }
