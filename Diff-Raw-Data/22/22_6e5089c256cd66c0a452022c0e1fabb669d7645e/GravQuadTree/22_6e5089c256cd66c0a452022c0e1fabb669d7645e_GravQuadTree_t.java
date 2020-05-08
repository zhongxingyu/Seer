 package se.exuvo.planets.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.exuvo.planets.components.Acceleration;
 import se.exuvo.planets.components.Mass;
 import se.exuvo.planets.components.Position;
 
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.utils.FastMath;
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * Tree data-structure used to speed up the GravitationSystem.
  * A QuadTree represents an square containing entities.
  * Each QuadTree keeps track of the total mass and the center of mass
  * of all its entities.
  * 
  * Each QuadTree object is in one of 3 states: has an entity, has 4 subtrees or has neither.
  */
 public class GravQuadTree {
 
 	// TODO should this class have direct access to entity-mappers (statically)?
 	// or pass as needed?
 
 	// We could store these in a com.badlogic.gdx.math.Rectangle, but it would store
 	// 'side' twofold: width and height. Since we don't need that many methods from that class it
 	// was just as easy to implement what was needed directly instead.
 	Vector2 pos; // bottom left
 	float side; // length of sides in Cube
 
 	float mass; // total
 	Vector2 massVector; // the sum of each (planet*planet.mass). Divide with this.mass to get center.
 
 	GravQuadTree bl, br, tl, tr; // {bottom,top}{left,right}
 	Entity entity; // TODO necessary to store entity? if we only update using updateMassCenter it would be unncessary.
 	
 	int size; // no of entities. (not nodes)
 
 	
 	
 	
 	/**
 	 * Creates a new QuadTree spanning the given cube.
 	 * 
 	 * @param pos
 	 *            bottomLeft corner of cube
 	 * @param side
 	 *            length of each side in cube
 	 */
 	public GravQuadTree(Vector2 pos, float side) {
 		this.pos = pos;
 		this.side = side;
 		massVector = pos.cpy();
 	}
 
 	
 	
 	
 	
 	
 	public boolean add(Entity entity, ComponentMapper<Mass> mm, ComponentMapper<Position> pm) {
 		if (!contains(pm.get(entity).vec)) {// TODO shouldn't be necessary
 			return false;
 		}
 		
 		if (isEmpty()) {
 			this.entity = entity;
 			this.mass = mm.get(entity).mass;
 			this.massVector = pm.get(entity).vec.cpy().mul(mass);
 			++size;
 			return true;
 		} else {
 			if (bl == null) {
 				addSubtrees();
 				quadrantOf(this.entity, mm, pm).add(this.entity, mm, pm);
 				this.entity = null;
 			}
 			quadrantOf(entity, mm, pm).add(entity, mm, pm);
 			float m = mm.get(entity).mass;
 			Vector2 p = pm.get(entity).vec;
 			this.massVector.add(p.cpy().mul(m));
 			this.mass += m;
 			++size;
 			return true;
 		}
 	}
 	
 	private void addSubtrees() {
 		float subside = side / 2;
 
 		bl = new GravQuadTree(pos.cpy(), subside);
 
 		br = new GravQuadTree(pos.cpy(), subside);
 		br.pos.x += subside;
 
 		tl = new GravQuadTree(pos.cpy(), subside);
 		tl.pos.y += subside;
 
 		tr = new GravQuadTree(pos.cpy(), subside);
 		tr.pos.x += subside;
 		tr.pos.y += subside;
 	}
 	
 	
 	
 	
 	public boolean remove(Entity entity, ComponentMapper<Mass> mm, ComponentMapper<Position> pm) {
 		if (hasEntity()) {
 			if (this.entity == entity) {
 				this.entity = null;
 				mass = 0f;
 				massVector.set(0f,0f);
 				--size;
 				return true;
 			}
 		} else if (hasChildren()) {
 			GravQuadTree sub = quadrantOf(entity, mm, pm);
 			float oldMass = sub.mass;
 			Vector2 oldVector = sub.massVector.cpy();
 			
 			if (sub.remove(entity, mm, pm)) {
 				--size;
 				mass -= oldMass - sub.mass; 
 				massVector.sub(oldVector).add(sub.massVector);
 			}
 		}
 		return false;
 	}
 	
 	/** Doesn't update mass and massVector. */
 	public boolean dirtyRemove(Entity entity, ComponentMapper<Mass> mm, ComponentMapper<Position> pm) {
 		if (hasEntity()) {
 			if (this.entity == entity) {
 				this.entity = null;
 				--size;
 				return true;
 			}
 		} else if (hasChildren()) {
 			GravQuadTree sub = quadrantOf(entity, mm, pm);
 			if (sub.remove(entity, mm, pm)) {
 				--size;
 			}
 		}
 		return false;
 
 	}
 	
 	
 	
 	
 	public void updateAcceleration(Entity e, float theta, float G, ComponentMapper<Mass> mm, ComponentMapper<Position> pm, ComponentMapper<Acceleration> am) {
 		if (isEmpty()) { // TODO also check if M is to small?
 			return;
 		}
 		float M = this.mass;
 		Vector2 p = pm.get(e).vec;
 		Vector2 diff;
 		
 //		if (contains(p)) { // TODO this is only necessary if theta>=1
 //			float m = mm.get(e).mass;
 //			diff = this.massVector.cpy().sub(p.cpy().mul(m)).div(M-m).sub(p);
 //		} else 
 			diff = this.massVector.cpy().div(M).sub(p);
 		
 		float d2 = diff.len2();
 		
 		// s/d < theta
 		if (this.entity != null || side*side < theta*theta*d2) { // TODO weight with mass.
 			if (this.entity == e) { // TODO temporary hack
 				return;
 			}
 			// F = G*m*M/d^2
 			// F = m*a
 			// a = G*M/d^2
 			float a = G*M/d2;
 			float k = (float) (a * FastMath.inverseSqrt(d2)); // normalize diff
 			if (!Float.isNaN(k)) {
 				Vector2 accVec = am.get(e).vec;
 				accVec.add(diff.mul(k));
 			}
 		} else {
 			bl.updateAcceleration(e, theta, G, mm, pm, am);
 			br.updateAcceleration(e, theta, G, mm, pm, am);
 			tl.updateAcceleration(e, theta, G, mm, pm, am);
 			tr.updateAcceleration(e, theta, G, mm, pm, am);
 		}
 	}
 	
 	
 	
 	
 	public void update(ComponentMapper<Mass> mm, ComponentMapper<Position> pm) {
 		List<Entity> moved = new ArrayList<Entity>();
 		update(moved, mm, pm);
 		for (Entity e : moved) {
 			add(e, mm, pm);
 		}
 	}
 	
 	private void update(List<Entity> moved, ComponentMapper<Mass> mm, ComponentMapper<Position> pm) {
 		if (hasEntity()) {
			if (!entity.isActive()) {
				Vector2 p = pm.get(entity).vec;
				if (!contains(p)) {
					moved.add(entity);
					--size;
					this.mass = 0f;
					this.massVector.set(0f,0f);
					entity = null;
				} else {
					mass = mm.get(entity).mass;
					massVector.set(p).mul(mass);
				}
 			}
 		} else if (hasChildren()){
 			bl.update(moved, mm, pm);
 			br.update(moved, mm, pm);
 			tl.update(moved, mm, pm);
 			tr.update(moved, mm, pm);
 			
 			size = bl.size + br.size + tl.size + tr.size;
 			
 			mass = bl.mass + br.mass + tl.mass + tr.mass;
 				
 			massVector.set(0f,0f);
 			massVector.add(bl.massVector).add(br.massVector);
 			massVector.add(tl.massVector).add(tr.massVector);
 		}
 	}
 		
 	
 	
 	
 	public boolean contains(Vector2 pos) {
 		return pos.x >= this.pos.x && pos.x < this.pos.x + side
 				&& pos.y >= this.pos.y && pos.y < this.pos.y + side;
 	}
 	
 	public int size() {
 		return size;
 	}
 	
 	public boolean isEmpty() {
 		return !hasChildren() && !hasEntity();
 	}
 	public boolean hasChildren() {
 		return bl != null;
 	}
 	public boolean hasEntity() {
 		return entity != null;
 	}
 	
 	
 	
 	
 	private GravQuadTree quadrantOf(Entity e, ComponentMapper<Mass> mm, ComponentMapper<Position> pm) {
 		float subside = side/2;
 		Vector2 p = pm.get(e).vec;
 		if (p.x < pos.x + subside) {
 			if (p.y < pos.y + subside) {
 				return bl;
 			} else {
 				return tl;
 			}
 		} else {
 			if (p.y < pos.y + subside) {
 				return br;
 			} else {
 				return tr;
 			}
 		}
 	}
 
 }
