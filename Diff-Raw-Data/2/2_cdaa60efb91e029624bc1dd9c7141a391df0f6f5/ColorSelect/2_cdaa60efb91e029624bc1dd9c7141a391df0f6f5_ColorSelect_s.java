 package nextapp.echo2.extras.app;
 
 import nextapp.echo2.app.Color;
 import nextapp.echo2.app.Component;
 
 /**
  * A visual hue/saturation/value-based color selection component to select 
  * RGB colors. 
  */
 public class ColorSelect extends Component {
 
     public static final String COLOR_CHANGED_PROPERTY = "color";
     
     private Color color;
     
     /**
      * Creates a new <code>ColorSelect</code> with an initially selected color
      * of <code>Color.WHITE</code>.
      */
     public ColorSelect() {
        this(Color.WHITE);
     }
     
     /**
      * Creates a new <code>ColorSelect</code> with the specified color 
      * initially selected.
      * 
      * @param color the initially selected color
      */
     public ColorSelect(Color color) {
         super();
         setColor(color);
     }
     
     /**
      * Retrieves the selected color.
      * 
      * @return the selected color
      */
     public Color getColor() {
         return color;
     }
     
     /**
      * @see nextapp.echo2.app.Component#processInput(java.lang.String, java.lang.Object)
      */
     public void processInput(String inputName, Object inputValue) {
         if (COLOR_CHANGED_PROPERTY.equals(inputName)) {
             setColor((Color) inputValue);
         }
     }
 
     /**
      * Sets the selected color.
      * 
      * @param newValue the new color
      */
     public void setColor(Color newValue) {
         if (newValue == null) {
             newValue = Color.WHITE;
         }
         Color oldValue = color;
         color = newValue;
         firePropertyChange(COLOR_CHANGED_PROPERTY, oldValue, newValue);
     }
 }
