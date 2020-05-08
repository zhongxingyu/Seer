 /*
  * Copyright 2006 - 2011 
  *     Julien Baudry	<julien.baudry@graphstream-project.org>
  *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
  *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
  *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
  * 
  * This file is part of GraphStream <http://graphstream-project.org>.
  * 
  * GraphStream is a library whose purpose is to handle static or dynamic
  * graph, create them from scratch, file or any source and display them.
  * 
  * This program is free software distributed under the terms of two licenses, the
  * CeCILL-C license that fits European law, and the GNU Lesser General Public
  * License. You can  use, modify and/ or redistribute the software under the terms
  * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
  * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
  * the Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
  */
 package org.graphstream.boids;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import org.graphstream.boids.Boid.BoidParticle;
 import org.miv.pherd.Particle;
 import org.miv.pherd.geom.Point3;
 import org.miv.pherd.geom.Vector3;
 import org.miv.pherd.ntree.Cell;
 
 /**
  * Models the forces applied to a boid.
  * 
  * @author Antoine Dutot
  * 
  */
 public abstract class Forces {
 	public Point3 barycenter;
 	public Vector3 direction;
 	public Vector3 attraction;
 	public Vector3 repulsion;
 	public int countAtt;
 	public int countRep;
 
 	public Forces() {
 		barycenter = new Point3();
 		direction = new Vector3();
 		attraction = new Vector3();
 		repulsion = new Vector3();
 		countAtt = 0;
 		countRep = 0;
 	}
 
 	/**
 	 * Compute the forces applied to a boid under the form of a barycenter that
 	 * the boids tries to reach (attraction), an overall direction for all the
 	 * surrounding boids, an overall direction of all the surrounding boids.
 	 * 
 	 * @param source
 	 *            The boid the forces are computed on.
 	 * @param startCell
 	 *            The start cell (usually the root cell of the n-tree).
 	 */
 	public abstract void compute(Boid source, Cell startCell);
 
 	/**
 	 * Store each force vector as an attribute in the boid. This allows all
 	 * listeners to retrieve the force vectors. This is particularly useful for
 	 * the viewer for example. Each force is stored with the attribute name
 	 * "force&lt;number&gt;" where &lt;number&gt; is a number that starts at
 	 * zero.
 	 * 
 	 * @param boid
 	 *            The boid to modify.
 	 */
 	protected void store(BoidParticle boid) {
 		boid.setAttribute("force0", direction);
 		boid.setAttribute("force1", attraction);
 		boid.setAttribute("force2", repulsion);
 	}
 
 	public void addRepulsion(Vector3 rep) {
 		repulsion.add(rep);
 		countRep++;
 	}
 
 	public void addDirection(Vector3 dir) {
 		direction.add(dir);
 		countAtt++;
 	}
 
 	public void addAttraction(Vector3 att) {
 		attraction.add(att);
 		countAtt++;
 	}
 
 	public void moveBarycenter(Point3 p) {
 		barycenter.move(p);
 	}
	
	public abstract boolean isVisible(BoidParticle boid, Point3 other); 
 
 	/**
 	 * A definition of the forces.
 	 * 
 	 * @author Antoine Dutot
 	 */
 	public static class BasicForces extends Forces {
 		@Override
 		public void compute(Boid source, Cell startCell) {
 			barycenter.set(0, 0, 0);
 			direction.fill(0);
 			attraction.fill(0);
 			repulsion.fill(0);
 			countAtt = 0;
 			countRep = 0;
 
 			exploreTree(source, startCell);
 			
 			if (countAtt > 0) {
 				barycenter.scale(1f / countAtt, 1f / countAtt, 1f / countAtt);
 				direction.scalarDiv(countAtt);
 				attraction.set(barycenter.x - source.getPosition().x,
 						barycenter.y - source.getPosition().y, barycenter.z
 								- source.getPosition().z);
 			}
 
 			if (countRep > 0) {
 				repulsion.scalarDiv(countRep);
 			}
 		}
 
 		/**
 		 * Recursively explore the n-tree to search for intersection cells, and
 		 * the visible boids.
 		 * 
 		 * @param source
 		 *            The boid the forces are computed on.
 		 * @param cell
 		 *            The cell to explore recursively.
 		 */
 		protected void exploreTree(Boid source, Cell cell) {
 			if (intersection(source, cell)) {
 				if (cell.isLeaf()) {
 					forcesFromCell(source.getParticle(), cell);
 				} else {
 					int n = cell.getSpace().getDivisions();
 
 					for (int i = 0; i < n; ++i)
 						exploreTree(source, cell.getSub(i));
 				}
 			}
 		}
 
 		/**
 		 * A leaf cell has been found that is in intersection with the boid
 		 * area, computes the forces from this cell.
 		 * 
 		 * @param source
 		 *            The boid the forces are computed on.
 		 * @param cell
 		 *            The cell.
 		 */
 		protected void forcesFromCell(BoidParticle source, Cell cell) {
 			// BoidCellData data = (BoidCellData) cell.getData();
 			Iterator<? extends Particle> particles = cell.getParticles();
 			Vector3 rep = new Vector3();
 			LinkedList<BoidParticle> contacts = null;
 
 			while (particles.hasNext()) {
 				Particle particle = particles.next();
 
 				if (particle instanceof BoidParticle) {
 					if (contacts == null)
 						contacts = new LinkedList<BoidParticle>();
 
 					if (source != particle
 							&& isVisible(source, particle.getPosition())) {
 						contacts.add((BoidParticle) particle);
 						actionWithNeighboor(source, (BoidParticle) particle,
 								rep);
 					}
 				}
 			}
 
 			source.getBoid().checkNeighborhood(
 					contacts == null ? null : contacts
 							.toArray(new BoidParticle[contacts.size()]));
 
 			// barycenter.move( data.getCenter() );
 			// direction.add( data.getDirection() );
 		}
 
 		/**
 		 * A rectangular intersection function, is the boid view area
 		 * intersecting the given cell?. This provides a quick lookup function
 		 * to test if a cell must be explored or not. Later, a better test
 		 * according to a spherical view zone will be done.
 		 * 
 		 * @param cell
 		 *            The cell to test for intersection with the boid
 		 *            rectangular view area.
 		 * @return True if there is an intersection.
 		 */
 		protected boolean intersection(Boid source, Cell cell) {
 			double x1 = cell.getSpace().getLoAnchor().x;
 			double y1 = cell.getSpace().getLoAnchor().y;
 			double z1 = cell.getSpace().getLoAnchor().z;
 			double x2 = cell.getSpace().getHiAnchor().x;
 			double y2 = cell.getSpace().getHiAnchor().y;
 			double z2 = cell.getSpace().getHiAnchor().z;
 
 			double X1 = source.getPosition().x - source.getSpecies().viewZone;
 			double Y1 = source.getPosition().y - source.getSpecies().viewZone;
 			double Z1 = source.getPosition().z - source.getSpecies().viewZone;
 			double X2 = source.getPosition().x + source.getSpecies().viewZone;
 			double Y2 = source.getPosition().y + source.getSpecies().viewZone;
 			double Z2 = source.getPosition().z + source.getSpecies().viewZone;
 
 			// Only when the area is before or after the cell there cannot
 			// exist an intersection (case a and b). Else there must be an
 			// intersection (cases c, d, e and f).
 			//
 			// |-a-| +---------+ |-b-|
 			// | |
 			// |-c-| |-d-|
 			// | |
 			// | |-e-| |
 			// | |
 			// |-+----f----+-|
 			// | |
 			// +---------+
 
 			if (X2 < x1 || X1 > x2)
 				return false;
 
 			if (Y2 < y1 || Y1 > y2)
 				return false;
 
 			if (Z2 < z1 || Z1 > z2)
 				return false;
 
 			return true;
 		}
 
 		/**
 		 * True if the given position is visible by the boid.
 		 * 
 		 * @param point
 		 *            The point to consider.
 		 * @return A true if point is visible.
 		 */
 /*		protected boolean isVisible(BoidParticle source, Point3 point) {
 			double d = point.distance(source.getPosition());
 			return (d <= source.getBoid().getSpecies().viewZone);
 		}
 */
		@Override
		public boolean isVisible(BoidParticle source, Point3 point) {
 			// Check both the distance and angle of view according to the direction
 			// of the source.
 			
 			BoidSpecies species = source.getBoid().getSpecies();
 
 			Point3 pos = source.getPosition();
 			double d   = pos.distance(point);
 			
 			// At good distance.
 			if(d <= species.viewZone) {
 				// If there is an angle of view.
 				if(species.angleOfView > -1) {
 					Vector3 dir   = new Vector3(source.dir);
 					Vector3 light = new Vector3(point.x - pos.x, point.y - pos.y, point.z - pos.z);//(pos.x - point.x, pos.y - point.y, pos.z - point.z);
 					
 					dir.normalize();
 					light.normalize();
 					
 					double  angle = dir.dotProduct(light);
 				
 					// In the field of view.
 					if(angle > species.angleOfView)
 						return true;
 				} else {
 					return true;
 				}
 			}
 			
 			// Not in view.
 			return false;
 		}
 
 		protected void actionWithNeighboor(BoidParticle p1, BoidParticle p2,
 				Vector3 rep) {
 			Point3 pp = p2.getPosition();
 			BoidSpecies p1Species = p1.getBoid().getSpecies();
 			BoidSpecies p2Species = p2.getBoid().getSpecies();
 			double v = p1.getBoid().getSpecies().viewZone;
 
 			rep.set(p1.getPosition().x - pp.x, p1.getPosition().y - pp.y,
 					p1.getPosition().z - pp.z);
 
 			double len = rep.length();
 			
 			if (len != 0) {
 				if (p1Species != p2Species)
 					rep.scalarMult(1 / (len * len) * p2Species.getFearFactor());
 				else
 					rep.scalarMult(1 / (len * len));
 			}
 			
 			double a = Math.log(Math.min(len, v)) / Math.log(v);
 			rep.scalarMult(a);
 			
 			repulsion.add(rep);
 			countRep++;
 
 			if (p1Species == p2Species) {
 				barycenter.move(pp);
 				direction.add(p2.dir);
 				countAtt++;
 			}
 		}
 	}
 }
