 package circlesvssquares;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.jbox2d.common.Vec2;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import pbox2d.PBox2D;
 
 public class LevelEditor {
     public static void saveLevel(ArrayList<Box2DObjectNode> objectList) {
         JSONArray level = new JSONArray();
         // Save objects
         for (int i = objectList.size()-1; i >=0; i--) {
             Box2DObjectNode n = objectList.get(i);
             if (n.getClass() != Bullet.class) {
                 JSONObject object = new JSONObject();
                 Vec2 pos = n.getPhysicsPosition();
                 object.put("x", new Float(pos.x));
                 object.put("y", new Float(pos.y));
                 object.put("class", n.getClass().toString());
 
                 if (n.getClass() == Ground.class) {
                     Ground g = (Ground) n;
                     object.put("w", new Float(g.w));
                     object.put("h", new Float(g.h));
                 }
                 else if (n.getClass() == Enemy.class) {
                     Enemy e = (Enemy) n;
                     object.put("w", new Float(e.w));
                     object.put("h", new Float(e.h));
                 }
 
                 level.add(object);
             }
         }
 
         try {
            FileWriter file = new FileWriter("/levels/test.json");
             file.write(level.toJSONString());
             file.flush();
             file.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void loadLevel(String slevel, PBox2D box2d) {
         if (slevel == null) {
            slevel = "levels/test.json";
         }
         JSONParser parser = new JSONParser();
 
         try {
             Object list = parser.parse(new FileReader(slevel));
             JSONArray level = (JSONArray) list;
             for (Object obj : level.toArray()) {
                 JSONObject node =  (JSONObject) obj;
 
                 String sClass = (String) node.get("class");
                 float x = ((Double) node.get("x")).floatValue(),
                         y = ((Double) node.get("y")).floatValue();
                 if (sClass.equals(Ground.class.toString())) {
                     float w = ((Double) node.get("w")).floatValue(),
                             h = ((Double) node.get("h")).floatValue();
 
                     Ground g = new Ground(new Vec2(x, y), w, h, box2d);
                 }
                 else if (sClass.equals(Enemy.class.toString())) {
                     float w = ((Double) node.get("w")).floatValue(),
                             h = ((Double) node.get("h")).floatValue();
 
                     Enemy e = new Enemy(new Vec2(x, y), w, h, box2d);
                 }
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (ParseException e) {
             e.printStackTrace();
         }
     }
 }
