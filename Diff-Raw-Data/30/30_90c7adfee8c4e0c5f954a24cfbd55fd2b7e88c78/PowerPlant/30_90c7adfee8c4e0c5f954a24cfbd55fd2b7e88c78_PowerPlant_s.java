 package polep.domain.technology;
 
 import org.neo4j.graphdb.Direction;
 import org.springframework.data.neo4j.annotation.Indexed;
 import org.springframework.data.neo4j.annotation.NodeEntity;
 import org.springframework.data.neo4j.annotation.RelatedTo;
 
 @NodeEntity
 public class PowerPlant {
 
         // @Indexed
 	private String name;
     private String nameTechnology;
     private double capacity;
     private double efficiency; 
     private double marginalCost;
     
     @RelatedTo(type = "USES_FUEL", elementClass = Fuel.class, direction = Direction.OUTGOING)
     public Fuel fuel;
     
    
     public Fuel getFuel() {
 		return fuel;
 	}
 
 	public void setFuel(Fuel fuel) {
 		this.fuel = fuel;
 	}
 
 	public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
 	/**
 	 * @return the nameTechnology
 	 */
 	public String getNameTechnology() {
 		return nameTechnology;
 	}
 
 	/**
 	 * @param nameTechnology the nameTechnology to set
 	 */
 	public void setNameTechnology(String nameTechnology) {
 		this.nameTechnology = nameTechnology;
 	}
 
 	public double getCapacity() {
 		return capacity;
 	}
 
 	public void setCapacity(double capacity) {
 		this.capacity = capacity;
 	}
 
 	public double getEfficiency() {
 		return efficiency;
 	}
 
 	public void setEfficiency(double efficiency) {
 		this.efficiency = efficiency;
 	}
 
 	public double getMarginalCost() {
 		return marginalCost;
 	}
 	
 	public void setMarginalCost(double marginalCost){
 		this.marginalCost = marginalCost;
 	} 
 	
 	public double calculateMarginalCost() {
 		return getFuel().getPrice()/getEfficiency();
 	}
 
 
 
 }
