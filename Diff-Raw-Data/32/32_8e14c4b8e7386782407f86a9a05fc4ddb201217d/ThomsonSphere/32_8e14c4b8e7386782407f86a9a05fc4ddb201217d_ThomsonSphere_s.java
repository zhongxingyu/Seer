 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author martins
  */
 public class ThomsonSphere {
 
 	public ThomsonPoint[] points;
 
 	/**
 	 * initialize points
 	 * @param points 
 	 */
 	public ThomsonSphere(int point_count) {
 
 		this.points = new ThomsonPoint[point_count];

 		// create points
 		for (int i = 0; i < point_count; i++) {
 			
			ThomsonPoint point = this.points[i];
			point.put_in_random_position(this);
 			
 		}
 		
 	}
 	
 	/**
 	 * Update point energy
 	 */
 	private void update_point_energy() {
 		
 		for(ThomsonPoint p : this.points) {
 			p.calculate_energy(this.points);
 		}
 		
 	}
 	
 	/**
 	 * Returns point with maximum energy
 	 * @return ThomsonPoint
 	 */
 	public ThomsonPoint get_maximum_energy_point() {
 
 		ThomsonPoint maximum_energy_point = points[0];
 
 		for (ThomsonPoint p : this.points) {
 			if (p.energy > maximum_energy_point.energy) {
 				maximum_energy_point = p;
 			}
 		}
 
 		return maximum_energy_point;
 	}
 	
 	/**
 	 * Arrange to their correct places
 	 */
 	public void arrange_points() {
 		
 		// update point energy
 		this.update_point_energy();
 		
 		// Find point with maximum energy
 		ThomsonPoint maximum_energy_point = this.get_maximum_energy_point();
 		
 		// Find closest, second closest neighbour
 		double energy_between, maximum_energy_between = 0;
 		ThomsonPoint maximum_energy_neighbour, maximum_energy_neighbour2;
 		
 		for(ThomsonPoint p : this.points) {
 			if(p!=maximum_energy_point) {
 				
 				energy_between = maximum_energy_point.get_energy(p);
 				if(energy_between > maximum_energy_between) {
 
 					maximum_energy_neighbour2 = maximum_energy_neighbour;
 					maximum_energy_neighbour = p;
 					maximum_energy_between = energy_between;
 				}
 			}
 		}
 		
 	}
 }
