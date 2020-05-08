 package essentials.objects;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import essentials.enums.LetterEnum;
 
 
 /**
  * Brick class
  */
 @XmlAccessorType(XmlAccessType.FIELD)
 @XmlRootElement(name = "brick")
 public class Brick implements Comparable<Brick> {
 
 	@XmlElement(name = "letter")
 	protected LetterEnum letter;
 	
 	@XmlElement(name = "weight")
     protected int weight = 0;
     
     /**
      * Constructor
      * @param aLetter
      * @param aWeight
      * @param aQuantity
      */
 	public Brick(LetterEnum aLetter, int aWeight){
     	letter = aLetter;
     	weight = aWeight;
     }
     
     /**
      * Constructor for empty obejct
      */
     public Brick(){
    	letter = LetterEnum.NULL;
    	weight = 0;
     }
 
     /**
      * Gets the value of the letter property.    
      */
     @XmlTransient
     public LetterEnum getLetter() {
         return letter;
     }
 
     /**
      * Sets the value of the letter property.   
      */
     public void setLetter(LetterEnum value) {
         this.letter = value;
     }
 
     /**
      * Gets the value of the weight property.   
      */
     @XmlTransient
     public int getWeight() {
         return weight;
     }
 
     /**
      * Sets the value of the weight property.
      * Set to = 0 to set brick as joker
      * Set to < 0 to set brick that's already on the map
      */
     public void setWeight(int value) {
         this.weight = value;
     }
 
     
     /**
      * Clones brick
      */
     public Brick clone(){    	
     	return new Brick( getLetter(), getWeight() );
     }
     
     
     public String toString(){
     	return (letter.toString() + ":" + weight);
     }
 
     
     /**
      * @param aBrick Brick to compare to this one
      * @return True if both, weight and letter are the same, otherwise false.
      */
     public boolean equals(Brick aBrick){
     	if( (letter == aBrick.getLetter() && weight == aBrick.getWeight())
     			|| (aBrick.getWeight() == 0 && (letter == aBrick.getLetter() || letter == LetterEnum.JOKER) )){
     		return true;
     	}
     	return false;
     }
 
 	@Override
 	public int compareTo(Brick aBrick) {
 		if( letter.ordinal() > aBrick.getLetter().ordinal() ){
 			return 1;
 		}
 		else if( letter.ordinal() < aBrick.getLetter().ordinal() ){
 			return -1;
 		}
 		else{
 			
 			return (aBrick.getWeight() - weight);
 			
 		}
 	}
 
 }
