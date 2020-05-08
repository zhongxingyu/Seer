 public class House extends Property {
     private static final String HOUSE_TYPE_CODE = "2";
 
     public House() {
         super(HOUSE_TYPE_CODE);
     }
 
     public Site upgrade(){
         Property skyscraper = new Skyscraper();
         skyscraper.setIndex(index);
         skyscraper.setMap(map);
         skyscraper.setPlayer(player);
         skyscraper.setPrice(price);
 
        map.setSite(index, skyscraper);
         return skyscraper;
     }
 }
