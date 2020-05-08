 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nl.jappieklooster.JME3.ProceduralPlacement.ShouldPlace;
 
 import com.jme3.math.Vector3f;
 import nl.jappieklooster.JME3.ProceduralPlacement.TerrainDataAcces;
 
 /**
  *
  * @author jappie
  */
 public class SeededShould extends TerrainDataAcces implements IShouldPlace{
     private String _seed;
     private static final float PER = 1500;
    private int _placeCycle = 0;
     public SeededShould(String seed){
         _seed = seed;
     }
     public boolean shouldPlace(Vector3f where) {
        if(_placeCycle < PER){
            _placeCycle++;
             return false;
         }
        _placeCycle = 0;
        boolean result = determinPlacement(where);
        return result;
     }
     
     private boolean determinPlacement(Vector3f where){
        return getCell().hashCode()*where.hashCode() < (_seed.hashCode() * where.x + where.y * where.z );
     }
 }
