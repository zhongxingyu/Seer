 package roborally.model;
 
 import be.kuleuven.cs.som.annotate.Basic;
 import roborally.property.Energy;
 import roborally.property.Weight;
 
 /**
  * Deze klasse houdt een repair kit bij dat op een bord kan staan. Dit kan een positie, een gewicht, energie en een positie kan hebben. Daarnaast kan dit object ook door een robot gedragen worden.
  * 
  * @author 	Bavo Goosens (1e bachelor informatica, r0297884), Samuel Debruyn (1e bachelor informatica, r0305472)
  * 
  * @version 1.0
  */
 public class RepairKit extends Item{
 	
 	public final static Energy MAX_ENERGY = new Energy(Double.MAX_VALUE);
 	
 	public final static Energy HIT_ENERGY = new Energy(500);
 	
 	public RepairKit(Energy energy, Weight weight){
 		super(weight);
		setEnergy(energy);
 	}
 
 	/**
	 * Deze methode wijzigt de energie van het item.
	 * 
	 * @param	energy
	 * 			De nieuwe energie van het item.
	 * 
	 * @post	De energie van het item is gelijk aan de gegeven energie.
	 * 			|new.getEnergy() == energy
	 */
	private void setEnergy(Energy energy) {
		this.energy = energy;
	}
	
	/**
 	 * Geeft de energie van het item.
 	 * 
 	 * @return	Energie van het item.
 	 * 			|energy
 	 */
 	@Basic
 	public Energy getEnergy() {
 		return energy;
 	}
 	
 	/**
 	 * Energie in het item.
 	 */
	private Energy energy;
 	
 	public boolean isValidRepairKitEnergy(Energy energy){
 		return (energy.getEnergy() <= MAX_ENERGY.getEnergy());
 	}
 
 	@Override
 	protected void damage(){
		Energy newEnergy = new Energy(getEnergy().getEnergy() + HIT_ENERGY.getEnergy());
 		if (newEnergy.getEnergy() > MAX_ENERGY.getEnergy())
			setEnergy(MAX_ENERGY);
		setEnergy(newEnergy);
 	}
 	
 }
