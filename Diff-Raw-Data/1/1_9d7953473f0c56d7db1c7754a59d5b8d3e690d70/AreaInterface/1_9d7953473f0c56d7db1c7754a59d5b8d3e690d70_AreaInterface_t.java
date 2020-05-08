 import java.util.List;
 
 public interface AreaInterface {
     public String getDescription();
     public List<Item> getAllItems();
     public Area getDirection(String direction);
     public Character getCharacter(String name);
     public String look(String direction);
 }
