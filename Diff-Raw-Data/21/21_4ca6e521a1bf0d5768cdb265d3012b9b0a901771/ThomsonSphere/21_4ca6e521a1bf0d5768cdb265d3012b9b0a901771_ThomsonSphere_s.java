 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.util.Arrays;
 import no.geosoft.cc.geometry.Geometry;
 
 /**
  *
  * @author martins
  */
 public class ThomsonSphere {
 
 	public ThomsonPoint[] points;
 	/**
 	 * Point point_energy
 	 */
 	public double[][] point_energy;
 	public double[][] point_distances;
 	/**
 	 * Energy sum from a point to all other points
 	 */
 	public double[] point_energy_sums;
 
 	/**
 	 * initialize points
 	 * @param points 
 	 */
 	public ThomsonSphere(int point_count) {
 
 		this.points = new ThomsonPoint[point_count];
 
 		this.point_energy = new double[point_count][point_count];
 
 		this.point_distances = new double[point_count][point_count];
 
 		this.point_energy_sums = new double[point_count];
 
 		// create points
 		for (int i = 0; i < point_count; i++) {
 
 			this.points[i] = new ThomsonPoint();
 			this.points[i].put_in_random_position();
 
 		}
 
 		// calculate energy between points for the first time
 		calculate_point_energy();
 	}
 
 	/**
 	 * recalculate energy between points
 	 */
 	public void calculate_point_energy() {
 
 		// clear point energy array
 		Arrays.fill(this.point_energy_sums, 0);
 
 		for (int i = 0; i < this.points.length; i++) {
 			for (int j = i + 1; j < this.points.length; j++) {
 
 				update_point_distance(i, j);
 				update_point_energy(i, j);
 
 				// add energy to each point energy sum
 				this.point_energy_sums[i] += this.point_energy[i][j];
 				this.point_energy_sums[j] += this.point_energy[i][j];
 			}
 		}
 	}
 
 	/**
 	 * Update distance between two points by their keys in point array
 	 * @param id1
 	 * @param id2 
 	 */
 	public void update_point_distance(int id1, int id2) {
 
 		double distance = this.points[id1].get_distance(this.points[id2]);
 
 		this.point_distances[id1][id2] = distance;
 		this.point_distances[id2][id1] = distance;
 
 	}
 
 	/**
 	 * Update point energy.
 	 * Before updating energy you must update distance
 	 * @param id1
 	 * @param id2 
 	 */
 	public void update_point_energy(int id1, int id2) {
 
 		double energy = 1 / this.point_distances[id1][id2];
 
 		this.point_energy[id1][id2] = energy;
 		this.point_energy[id2][id1] = energy;
 
 	}
 
 	/**
 	 * Calculate spheres energy
 	 * @return double
 	 */
 	public double get_energy() {
 
 		double energy = 0;
 
 		for (int i = 0; i < this.points.length; i++) {
 
 			for (int j = i + 1; j < this.points.length; j++) {
				energy += this.points[i].get_energy(this.points[j]);
 			}
 		}
 
 		return energy;
 
 	}
 
 	/**
 	 * Returns point with maximum energy
 	 * @return int
 	 */
 	public int get_maximum_energy_point() {
 
 		// first point is with maximum energy
 		int maximum_energy_point = 0;
 
 		for (int i = 0; i < this.points.length; i++) {
 			if (this.point_energy_sums[i] > this.point_energy_sums[maximum_energy_point]) {
 
 				maximum_energy_point = i;
 
 			}
 		}
 
 		return maximum_energy_point;
 	}
 
 	/**
 	 * Find closest point to a point
 	 * @param point_id
 	 * @return 
 	 */
 	public int find_closest_point(int point_id) {
 
 		int closest_point = -1;
 
 		for (int i = 0; i < this.points.length; i++) {
 
 			if (point_id != i && (closest_point == -1
 					|| this.point_distances[point_id][i] < this.point_distances[point_id][closest_point])) {
 
 				closest_point = i;
 			}
 
 		}
 
 		return closest_point;
 	}
 
 	/**
 	 * Arrange to their correct places
 	 */
 	public void arrange_points() {
 
 		// Find point with maximum energy
 		int maximum_energy_point = this.get_maximum_energy_point();
 
 		// Find closest, second closest neighbour
 		int closest_point = find_closest_point(maximum_energy_point);
 
 		// Move p0 away. in direction max_e_p->p0
 		move_point(closest_point, maximum_energy_point);
 
 	}
 
 	/**
 	 * Move point away from other point
 	 * @param point_move
 	 * @param point_away_from 
 	 */
 	public void move_point(int point_move, int point_away_from) {
 
 		// resulting vector for first point
 		double[] v1 = new double[3];
 
 		// vector p1->p2
 		v1 = Geometry.createVector(this.points[point_away_from].point, this.points[point_move].point);
 
 		double distance_koef = 1.3;
 
 		// strech vector
 		v1[0] *= distance_koef;
 		v1[1] *= distance_koef;
 		v1[2] *= distance_koef;
 
 		// add resulting vector to p1
 		this.points[point_move].point[0] = this.points[point_away_from].point[0] + v1[0];
 		this.points[point_move].point[1] = this.points[point_away_from].point[1] + v1[1];
 		this.points[point_move].point[2] = this.points[point_away_from].point[2] + v1[2];
 
 		ThomsonPoint.normalizeVector(this.points[point_move].point);
 
 		// After point is moved we must update distances 
 		// and enrgies from this point to other points
 
 		for (int i = 0; i < this.points.length; i++) {
 			if (i != point_move) {
 
 				// update distance
 				this.update_point_distance(i, point_move);
 
 				// remove previously added energy from energy sums
 				this.point_energy_sums[i] -= this.point_energy[i][point_move];
 				this.point_energy_sums[point_move] -= this.point_energy[i][point_move];
 
 				// update energy
 				this.update_point_energy(i, point_move);
 
 				// add back new energy
 				this.point_energy_sums[i] += this.point_energy[i][point_move];
 				this.point_energy_sums[point_move] += this.point_energy[i][point_move];
 			}
 		}
 	}
 }
