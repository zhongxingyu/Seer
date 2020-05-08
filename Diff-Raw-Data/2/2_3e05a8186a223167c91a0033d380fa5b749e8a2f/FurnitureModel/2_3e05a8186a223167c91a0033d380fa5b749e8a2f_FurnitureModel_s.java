 package interiores.business.models;
 
 import interiores.core.Utils;
 import interiores.core.business.BusinessException;
 import interiores.data.adapters.ColorAdapter;
 import interiores.utils.CoolColor;
 import interiores.utils.Dimension;
 import java.awt.Color;
 import java.awt.Point;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 /**
  * Represents a furniture model of a specific type. A model has its own name, size, price, color, and material
  * @author larribas
  */
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 public class FurnitureModel {
     @XmlAttribute
     private String type;       // Furniture's type of this model
         
     @XmlAttribute
     private String name;        // Comercial name of the furniture model
     
     @XmlElement
     private Dimension size;     // Size of the furniture model
     
     @XmlElement
     private SpaceAround passiveSpace; // Passive space requirements for the furniture model
     
     @XmlAttribute
     private float price;          // Market price of the furniture model
     
     @XmlAttribute
     @XmlJavaTypeAdapter(ColorAdapter.class)
     private CoolColor color;        // Color of the furniture model
     
     @XmlAttribute
     private String material;    // Material the furniture model is made in
     
     /**
      * Default constructor.
      */
     public FurnitureModel() {
         
     }
     
     public FurnitureModel(String name, Dimension size, float price, String color, String material)
             throws BusinessException
     {
         this(name, size, price, color, material, new SpaceAround(0, 0, 0, 0));
     }
     
     public FurnitureModel(String name, Dimension size, float price, String color, String material,
             SpaceAround passiveSpace)
             throws BusinessException
     {
         this.name = name;
         this.size = size;
         this.price = price;
         this.color = CoolColor.getEnum(color);
         this.material = material;
         this.passiveSpace = passiveSpace;
     }
     
     /**
      * Gets the type of the furniture
      * @return The furniture type of the model
      */
     public String getType() {
         return type;
     }
     
     /**
      * Sets the type of the furniture
      * @param type The type of this piece of furniture
      */
     public void setType(String type) {
         this.type = type;
     }
     
     /**
      * Gets the name of the furniture model
      * @return String object representing the name of the model
      */
     public String getName() {
         return name;
     }
     
     /**
      * Gets the size of the furniture model
      * @return Dimension object representing the size of the model
      */
     public Dimension getSize() {
         return size;
     }
     
     public OrientedRectangle getActiveArea(Point position, Orientation orientation) {
         OrientedRectangle activeArea = new OrientedRectangle(position, getSize(), Orientation.S);
         activeArea.setOrientation(orientation);
         
         return activeArea;
     }
     
     public boolean hasPassiveSpace() {
        return passiveSpace.isEmpty();
     }
     
     public SpaceAround getPassiveSpace() {
         return passiveSpace;
     }
     
     public void setPassiveSpace(SpaceAround passiveSpace) {
         this.passiveSpace = passiveSpace;
     }
     
     /**
      * Gets the color of the furniture model
      * @return Color object representing the color of the model
      */
     public Color getColor() {
         return color.getColor();
     }
 
     /**
      * Gets the material of the furniture model
      * @return String representing the material the model is made from
      */
     public String getMaterial() {
         return material;
     }
 
     /**
      * Gets the price of the furniture model
      * @return float object representing the market price of the model 
      */
     public float getPrice() {
         return price;
     }
     
     @Override
     public String toString() {
         return Utils.padRight(name, 20) + "Size[" + size + "], Price[" + price + "], Color[" + color
                 + "], Material[" + material + "], Passive space[" + passiveSpace + "]";
     }
 }
