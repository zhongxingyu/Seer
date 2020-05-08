 package roborally.model;
 
 import be.kuleuven.cs.som.annotate.Basic;
 import be.kuleuven.cs.som.annotate.Model;
 
 import roborally.property.Energy;
 import roborally.property.Weight;
 
 /**
  * Deze klasse houdt een object bij dat op een bord kan staan en een positie en een gewicht kan hebben. Daarnaast kan dit object ook door een robot gedragen worden.
  * 
  * @author 	Bavo Goosens (1e bachelor informatica, r0297884), Samuel Debruyn (1e bachelor informatica, r0305472)
  * 
  * @version 1.0
  */
 public abstract class Item extends Entity{
 
 	/**
 	 * Deze constructor maakt een nieuw item aan.
 	 * 
 	 * @param	weight
 	 * 			Het gewicht dat het nieuwe item moet krijgen.
 	 * 
 	 * @post	Het nieuwe item heeft het gegeven gewicht.
	 * 			|new.getWeight().equals(weight)
 	 */
 	@Model
 	protected Item (Weight weight){
 		this.weight = weight;
 	}
 
 	/**
 	 * Geeft het gewicht terug van het item.
 	 * 
 	 * @return	Het gewicht van het item.
 	 * 			|weight
 	 */
 	@Basic
 	public Weight getWeight() {
 		return weight;
 	}
 	
 	/**
 	 * Gewicht van het item.
 	 */
 	private final Weight weight;
 	
 	/*
 	 * Deze methode zet het object om naar een String.
 	 * 
 	 * @return	Een textuele representatie van dit object waarbij duidelijk wordt wat de eigenschappen van dit object zijn.
 	 * 			|super.toString() + ", gewicht: " + getWeight().toString()
 	 */
 	@Override
 	public String toString() {
 		return super.toString() + ", gewicht: " + getWeight().toString();
 	}
 	
 }
