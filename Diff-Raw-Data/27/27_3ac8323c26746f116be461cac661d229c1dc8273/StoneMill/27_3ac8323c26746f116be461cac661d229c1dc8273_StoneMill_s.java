 /*
 
 StoneMill.java
 
 */
 
 public class StoneMill extends Entity
 {
     public StoneMill()
     {
         super();
         type = Entity.Type.STONEMILL;
         description = "Stone Mill";
         
         attributes.clear();
         attributes.add(Entity.Attribute.FETCHABLE);
     }
 }
