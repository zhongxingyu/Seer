 package tuplespace;
 
 import java.sql.Timestamp;
 import java.util.Date;
 
 import oopl.DistributedOOPL;
 import net.jini.core.entry.Entry;
 
 public class Coin implements TimeEntry {
 	
 	public Integer id;
 	public Cell cell;
 	public String agent;
 	public Date time;
 	public Integer clock;
 	
 	public Coin() {
 
 	}
 public Coin(Cell cell, String agent, Integer clock) {
 		
 		this.cell = cell;
 		this.agent = agent;
 		this.clock = clock;
 		this.time = new Date();
 	}
 	public Coin(Integer id, Cell cell, String agent, int clock) {
 
 		this.id = id;
 		this.cell = cell;
 		this.agent = agent;
 		this.clock = clock;
 		this.time = new Date();
 
 	}
 
 	public Coin(Integer clock) {
 		this.clock = clock;
 	}
 
 	public Coin(String agent) {
 		this.agent = agent;
 	}
 	
     public Coin(String[] params) {
		
 	}
     
 	public int[] toArray(DistributedOOPL oopl) {
 		int[] r = new int[18];
 		JL.addPredicate(r,0,oopl.prolog.strStorage.getInt("coin"),3, oopl); // cargo/2
 
 		
 		JL.addPredicate(r, 3, oopl.prolog.strStorage.getInt("cell"), 2, oopl);
 		JL.addNumber(r, 6, this.cell.x, oopl);
 		JL.addNumber(r, 9, this.cell.y, oopl);
 			
 		JL.addNumber(r,12,this.clock, oopl);
 		JL.addPredicate(r,15, JL.makeStringKnown(this.agent,oopl),0, oopl);
 		//addPredicate(r,3,makeStringKnown(t.agent==null?"null":t.agent),0); // the name
 		//for (int i = 0;  i<r.length; i++){
 		//	System.out.println("to array: " + oopl.prolog.strStorage.getString(r[i]));
 			
 		//}
 		
 		//addNumber(r, c,t.i);
 		return r;
 	}
     
 	@Override
 	public String toString() {
 		return "Coin [id=" + id + ", cell=" + cell + ", agent=" + agent
 				+ ", time=" + time + ", clock=" + clock + "]";
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public Cell getCell() {
 		return cell;
 	}
 
 	public void setCell(Cell cell) {
 		this.cell = cell;
 	}
 
 	public Date getTime() {
 		return time;
 	}
 
 	public void setTime(Date time) {
 		this.time = time;
 	}
 
 	public Integer getClock() {
 		return clock;
 	}
 
 	public void setClock(Integer clock) {
 		this.clock = clock;
 	}
 }
