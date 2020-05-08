 package pt.up.fe.pt.lpoo.bombermen;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import pt.up.fe.pt.lpoo.utils.Ref;
 
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 
 public class MapLoader
 {
     private int _lastId;
     HashMap<Integer, Entity> _entities;
 
     public MapLoader(HashMap<Integer, Entity> entities, int lastId)
     {
         _entities = entities;
         _lastId = lastId;
     }
 
     public boolean TryLoad(int number, Ref<Integer> width, Ref<Integer> height, Ref<Integer> lastId)
     {
         BufferedReader br = null;
 
         try
         {
             br = new BufferedReader(new FileReader("maps/map" + number + ".txt"));
         }
         catch (FileNotFoundException e)
         {
             return false;
         }
 
         ArrayList<String> lines = new ArrayList<String>();
 
         String currLine;
         try
         {
             while ((currLine = br.readLine()) != null)
                 lines.add(currLine);
         }
         catch (IOException e)
         {
             return false;
         }
 
         try
         {
             br.close();
         }
         catch (IOException e)
         {
             // really?
         }
 
         for (int y = 0; y < lines.size(); ++y)
         {
             char[] chars = lines.get(y).toCharArray();
             for (int x = 0; x < chars.length; ++x)
             {
                 switch (chars[x])
                 {
                     case ' ': // empty space, can be destroyable wall
                         if (MathUtils.random() < Constants.WALL_CHANCE) AddWall(1, x, lines.size() - 1 - y);
                         break;
                     case 'X': // undestroyable wall
                         AddWall(-1, x, lines.size() - 1 - y);
                         break;
                     case '#': // space reserved for players
                         ReservePlayerSpace(x, lines.size() - 1 - y);
                         break;
                 }
             }
         }
 
        lastId.Set(_lastId);
         return true;
     }
 
     private void ReservePlayerSpace(int tileX, int tileY)
     {
         // @TODO: Implement
     }
 
     private void AddWall(int hp, int tileX, int tileY)
     {
         _entities.put(_lastId, new Wall(_lastId, hp, new Vector2(tileX * Wall.Size.x, tileY * Wall.Size.y)));
         _lastId++;
     }
 }
