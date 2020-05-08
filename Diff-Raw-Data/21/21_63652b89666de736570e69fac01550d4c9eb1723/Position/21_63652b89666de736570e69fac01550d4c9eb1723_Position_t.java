 package tuplespace;
 
 import java.util.Date;
 import net.jini.core.entry.Entry;
 
 public class Position implements TimeEntry {
 	
 	
 	public Integer id = null;
 	public String agent = null;
 	//public Long longitude;
 	//public Long latitude;
 	public Cell cell = null;
 	public Date time = null;
 	public Integer clock;
 	
 	public Position() {
 
 	}
 	public Position(Integer id, Cell cell) {
 		this.id = id;
 		this.cell = cell;
 	}
 	public Position(String agent, Cell cell) {
 		this.agent = agent;
 		this.cell = cell;
		//this.time = new Date();
 	}
 	public Position(Integer id, String agent, Cell cell, int clock) {
 		this.id = id;
 		this.agent = agent;
 		this.cell = cell;
 		this.clock = clock;
 		this.time = new Date();
 	}
 	public Position(String agent, Cell cell, int clock) {
 		this.agent = agent;
 		this.cell = cell;
 		this.clock = clock;
 		this.time = new Date();
 	}
 
 	public Position(String agent) {
 		this.agent = agent;
 	}
 
 	public Position(int clock) {
 		this.clock = clock;
 	}
 
 	@Override
 	public String toString() {
 		return "Position [agent=" + agent + ", id=" + id + ", cell=" + cell
 				+ ", time=" + time + ", clock=" + clock + "]";
 	}
 
 	public String getAgent() {
 		return agent;
 	}
 
 	public void setAgent(String agent) {
 		this.agent = agent;
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
 
