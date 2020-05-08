 package eu.janinko.aiforlife.World;
 
 import eu.janinko.aiforlife.Organism.AttackingOrganism;
 import eu.janinko.aiforlife.Organism.Organism;
 import eu.janinko.aiforlife.Organism.GPOrganism.GPOrganism;
 
 public class OrganismWorldObject implements WorldObject {
	Organism organism;
 	
 	public OrganismWorldObject(Organism o){
		this.organism = o;
 	}
 	
 
 	@Override
 	public Organism getOrganism() {
		return this.organism;
 	}
 
 	@Override
 	public boolean isOrganism() {
 		return true;
 	}
 
 
 	@Override
 	public void attacked(Organism o, int attack) {
		if(organism instanceof AttackingOrganism){
			((AttackingOrganism) organism).damage(attack, o);
 		}else{
			organism.damage(attack);
 		}
 		
 	}
 
 }
