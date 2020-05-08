 package eu.janinko.aiforlife.World;
 
 import eu.janinko.aiforlife.Organism.AttackingOrganism;
 import eu.janinko.aiforlife.Organism.Organism;
 import eu.janinko.aiforlife.Organism.GPOrganism.GPOrganism;
 
 public class OrganismWorldObject implements WorldObject {
	Organism o;
 	
 	public OrganismWorldObject(Organism o){
		this.o = o;
 	}
 	
 
 	@Override
 	public Organism getOrganism() {
		return this.o;
 	}
 
 	@Override
 	public boolean isOrganism() {
 		return true;
 	}
 
 
 	@Override
 	public void attacked(Organism o, int attack) {
		if(o instanceof AttackingOrganism){
			((AttackingOrganism) o).damage(attack, o);
 		}else{
			o.damage(attack);
 		}
 		
 	}
 
 }
