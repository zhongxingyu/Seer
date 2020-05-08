 public class Conn {
 	public Entity e1;
 	public Entity e2;
 	
 	public double strength;
 
 	public Conn(Entity en1, Entity en2) {
 		e1 = en1;
 		e2 = en2;
 
 		strength = connStrength();
 	}
 	
 	public double dist() {
 		return Math.sqrt(Math.pow(e2.x - e1.x, 2) + Math.pow(e2.y - e1.y, 2));
 	}
 	
 	public void changeSpeed(double force) {
 		double dx = ((e2.x - e1.x) / dist() * force);
 		double dy = ((e2.y - e1.y) / dist() * force);
 
 		e1.speedX += dx;
 		e1.speedY += dy;
 		e2.speedX -= dx;
 		e2.speedY -= dy;
 	}
 	
 	public void moveFor(double force) {
 		double dx = ((e2.x - e1.x) / dist() * force);
 		double dy = ((e2.y - e1.y) / dist() * force);
 
 		e1.x += dx;
 		e1.y += dy;
 		e2.x -= dx;
 		e2.y -= dy;
 	}
 	
 	public void move() {
 		e1.move();
 		e2.move();	
 	}
 	
 	private double connStrength() {
 
 		double strength = 0;
 		for (int i = 0; i < AttrMeta.size; i++) {
 			double d = Math.abs(e1.attr[i] - e2.attr[i]);
 			double s = 0;
 			if (AttrMeta.type[i].equals("d") && d == 0) {
 				s = 1;
 			} else if (AttrMeta.type[i].equals("c")) {
				double n = (e1.attr[i] + e2.attr[i]) - 2 * AttrMeta.getMin(i);
				n = n == 0 ? 1 : n;
				
				s = 1 - (d / n);
 			}
 			double k1 = AttrMeta.valueScore(i, e1.attr[i]) * AttrMeta.valueScore(i, e2.attr[i]);
 			//double k2 = !e1.clasHidden && !e2.clasHidden && e1.clas != e2.clas ? 0.5 : 1;
 			
 			strength += s * AttrMeta.scores[i] * k1;
 		}
 		return strength;
 	}
 /*
 	private double connStrength() {	//TODO NEW
 
 		double strength = 0;
 		for (int i = 0; i < AttrMeta.size; i++) {
 			double d = Math.abs(e1.attr[i] - e2.attr[i]);
 			double s = 0;
 			if (AttrMeta.type[i].equals("d") && d == 0) {
 				s = 0.5;
 			} else if (AttrMeta.type[i].equals("c")) {
 				double n = (e1.attr[i] + e2.attr[i]) - 2 * AttrMeta.getMin(i);
 				n = n == 0 ? 1 : n;
 				
 				s = Math.pow(1 - (d / n), 2);
 				//System.out.println(0.0 / 0.0);
 			}
 			
 			strength += s * AttrMeta.scores[i];
 		}
 		return strength;
 	}
 	*/
 }
