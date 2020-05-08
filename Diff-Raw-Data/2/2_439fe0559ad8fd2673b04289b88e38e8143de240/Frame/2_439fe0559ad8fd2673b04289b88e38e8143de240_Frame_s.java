 package visualization;
 
 public class Frame {
 	public Flower[] flowers;
	public int time;
 	public int id;
 	
 	public Frame (int id, int time, Flower[] flowers){
 		this.flowers = flowers;
 		this.time = time;
 		this.id = id;
 	}
 }
