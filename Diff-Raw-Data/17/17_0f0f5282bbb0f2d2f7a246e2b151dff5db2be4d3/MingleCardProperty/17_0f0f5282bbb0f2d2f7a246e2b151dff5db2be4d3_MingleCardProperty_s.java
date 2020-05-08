 package mingleplugin;
 
 // Mingle api doc says:
 // property: Resource; name and a current value for each card property defined for current
 // card's card type are listed; Data type will depend on the property while property name 
 // is always String. The property also includes attributes about the property type_description
 // and whether or not it is hidden.
 
 import java.util.Arrays;
 
 class MingleCardProperty extends MingleObject{
   
   private String mingleObjectType = "MingleCardProperty";
 
   private String name;
   private Object value;
  //private String type_description;
   private boolean hidden;
 
   public String getName()
   {
     return name;
   }
 
   public void setName( String name )
   {
     this.name = name;
   }
 
   public Object getValue() {
     return value;
   }
 
   public void setValue( Object obj ) {
     // checks for valid data types. incomplete!
     // TODO: complete checks
     if (obj instanceof String || obj instanceof String || 
         obj instanceof Integer || obj instanceof Object[]
         ) {
       this.value = obj;
     }
   }
 
   public String getTypeDescription()
   {
     return type_description;
   }
 
   public void setTypeDescription( String description )
   {
     this.type_description = description;
   }
 
  public boolean getHidden()
   {
     return hidden;
   }
 
   public void setHidden( boolean hidden )
   {
     this.hidden = hidden;
   }
 
   // The attributes hidden and type_description are optional
   MingleCardProperty(String name, Object value, String type_description, boolean hidden) {
     this.name = name;
     this.value = value;
     this.type_description = type_description;
     this.hidden = hidden;
   }
 
   MingleCardProperty(String name, Object value, boolean hidden) {
     this.name = name;
     this.value = value;
     this.hidden = hidden;
   }
 
   MingleCardProperty(String name, Object value) {
     this.name = name;
     this.value = value;
    this.hidden = null;
   }
 
 }
