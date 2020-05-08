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
 
 import org.graphstream.graph.Graph;
 import org.graphstream.graph.implementations.AbstractGraph;
 import org.graphstream.graph.implementations.AdjacencyListNode;
 import org.miv.pherd.Particle;
 import org.miv.pherd.geom.Point3;
 import org.miv.pherd.geom.Vector3;
 
 public class Boid extends AdjacencyListNode {
 
 	protected BoidParticle particle;
 	protected BoidSpecies species;
 	protected Forces forces;
 
 	public Boid(Graph graph, String id) {
 		super((AbstractGraph)graph, id);
 		particle = new BoidParticle((Context) graph);
 		species = ((Context) graph).getDefaultSpecies();
 		forces = getDefaultForces();
 	}
 
 	public void setPosition(double x, double y, double z) {
 		particle.setPosition(x, y, z);
 	}
 
 	public Point3 getPosition() {
 		return particle.getPosition();
 	}
 
 	public BoidSpecies getSpecies() {
 		return species;
 	}
 
 	public void setSpecies(BoidSpecies species) {
 		this.species = species;
 	}
 
 	public BoidParticle getParticle() {
 		return particle;
 	}
 
 	public Forces getDefaultForces() {
 		return new Forces.BasicForces();
 	}
 
 	@Override
 	protected void attributeChanged(String sourceId, long timeId,
 			String attribute, AttributeChangeEvent event, Object oldValue,
 			Object newValue) {
 		if (attribute.equals("species")) {
 			Context ctx = (Context) getGraph();
 			BoidSpecies species = ctx.getOrCreateSpecies(newValue.toString());
 			this.species = species;
 		}
 
 		super.attributeChanged(sourceId, timeId, attribute, event, oldValue,
 				newValue);
 	}
 
 	protected void checkNeighborhood(BoidParticle... particles) {
 		if (particles != null) {
 			Iterator<Boid> it = getNeighborNodeIterator();
 			LinkedList<Boid> toRemove = null;
 
 			while (it.hasNext()) {
 				boolean found = false;
 				Boid b = it.next();
 
 				for (BoidParticle p : particles)
 					if (p.getId().equals(b.getParticle().getId())) {
 						found = true;
 						break;
 					}
 
				if (!found && !forces.isVisible(b.particle, this.getPosition())) {
 					if (toRemove == null)
 						toRemove = new LinkedList<Boid>();
 
 					toRemove.add(b);
 				}
 			}
 
 			if (toRemove != null) {
 				for (Boid b : toRemove)
 					getGraph().removeEdge(getEdgeId(this, b));
 
 				toRemove.clear();
 				toRemove = null;
 			}
 
 			for (BoidParticle p : particles) {
 				if (getEdgeBetween(p.getBoid().getId()) == null)
 					getGraph().addEdge(getEdgeId(this, p.getBoid()), getId(),
 							p.getBoid().getId());
 			}
 		}
 	}
 
 	public static final String getEdgeId(Boid b1, Boid b2) {
 		if (b1.hashCode() > b2.hashCode()) {
 			Boid t = b1;
 			b1 = b2;
 			b2 = t;
 		}
 
 		return String.format("%s--%s", b1.getId(), b2.getId());
 	}
 
 	class BoidParticle extends Particle {
 		protected Vector3 dir;
 		protected Context ctx;
 
 		protected int contacts = 0;
 		protected int mySpeciesContacts = 0;
 
 		protected float energy = 0;
 
 		public BoidParticle(Context ctx) {
 			super(Boid.this.getId(), ctx.random.nextDouble() * (ctx.area * 2)
 					- ctx.area, ctx.random.nextDouble() * (ctx.area * 2)
 					- ctx.area, 0);
 
 			this.dir = new Vector3(ctx.random.nextDouble(),
 					ctx.random.nextDouble(), 0);
 			this.ctx = ctx;
 		}
 
 		@Override
 		public void move(int time) {
 			contacts = 0;
 			mySpeciesContacts = 0;
 
 			forces.compute(Boid.this, cell.getTree().getRootCell());
 			
 			forces.direction.scalarMult(species.directionFactor);
 			forces.attraction.scalarMult(species.attractionFactor);
 			forces.repulsion.scalarMult(species.repulsionFactor);
 			dir.scalarMult(species.inertia);
 
 			dir.add(forces.direction);
 			dir.add(forces.attraction);
 			dir.add(forces.repulsion);
 
 			if (ctx.normalizeMode) {
 				double len = dir.normalize();
 				if (len <= species.minSpeed)
 					len = species.minSpeed;
 				else if (len >= species.maxSpeed)
 					len = species.maxSpeed;
 
 				dir.scalarMult(species.speedFactor * len);
 			} else {
 				dir.scalarMult(species.speedFactor);
 			}
 			
 			if (ctx.storeForcesAttributes)
 				forces.store(this);
 
 			checkWalls();
 			nextPos.move(dir);
 
 			Boid.this.setAttribute("x", pos.x);
 			Boid.this.setAttribute("y", pos.y);
 			Boid.this.setAttribute("z", pos.z);
 
 			moved = true;
 		}
 
 		@Override
 		public void inserted() {
 
 		}
 
 		@Override
 		public void removed() {
 
 		}
 
 		public Boid getBoid() {
 			return Boid.this;
 		}
 
 		public void setPosition(double x, double y, double z) {
 			initPos(x, y, z);
 		}
 
 		/**
 		 * Check the boid does not go out of the space walls.
 		 */
 		protected void checkWalls() {
 			// /float area = ctx.area;
 			float aarea = 0.000001f;
 
 			if (nextPos.x + dir.data[0] <= ctx.getSpace().getLoAnchor().x
 					+ aarea) {
 				nextPos.x = ctx.getSpace().getLoAnchor().x + aarea;
 				dir.data[0] = -dir.data[0];
 			} else if (nextPos.x + dir.data[0] >= ctx.getSpace().getHiAnchor().x
 					- aarea) {
 				nextPos.x = ctx.getSpace().getHiAnchor().x - aarea;
 				dir.data[0] = -dir.data[0];
 			}
 			if (nextPos.y + dir.data[1] <= ctx.getSpace().getLoAnchor().y
 					+ aarea) {
 				nextPos.y = ctx.getSpace().getLoAnchor().y + aarea;
 				dir.data[1] = -dir.data[1];
 			} else if (nextPos.y + dir.data[1] >= ctx.getSpace().getHiAnchor().y
 					- aarea) {
 				nextPos.y = ctx.getSpace().getHiAnchor().y - aarea;
 				dir.data[1] = -dir.data[1];
 			}
 			if (nextPos.z + dir.data[2] <= ctx.getSpace().getLoAnchor().z
 					+ aarea) {
 				nextPos.z = ctx.getSpace().getLoAnchor().z + aarea;
 				dir.data[2] = -dir.data[2];
 			} else if (nextPos.z + dir.data[2] >= ctx.getSpace().getHiAnchor().z
 					- aarea) {
 				nextPos.z = ctx.getSpace().getHiAnchor().z - aarea;
 				dir.data[2] = -dir.data[2];
 			}
 		}
 	}
 }
