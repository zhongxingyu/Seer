 public class Cabin extends Property{
     private static final String CABIN_TYPE_CODE = "1";
 
     public Cabin() {
         super(CABIN_TYPE_CODE);
     }
 
     public Site upgrade(){
         Property house = new House();
         house.setIndex(index);
         house.setMap(map);
         house.setPlayer(player);
         house.setPrice(price);
 
        // TODO: map.setSite(index, house);
         return house;
     }
 }
