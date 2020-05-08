 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame;
 
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Quad;
 
 /**
  *
  * @author armageddon
  */
 public class AreaConnectionNode extends Node{
     
     static final float CONNECTION_PADDING = AreaNode.AREA_PADDING + 1.0f/16;
     
     private AreaNode src, to;
     
     private Vector2f pos, size;
 
 
     int toMoveZombies, toMoveHumans, toMoveSoldiers;
     
     AreaConnectionNode(AreaNode source, AreaNode target){
         
         src = source;
         to = target;
         
         pos = new Vector2f();
         size = new Vector2f();
         
         
        if(to.getPosition().x + to.getSize().x <= src.getPosition().x){
            pos.setY(Math.max(src.getPosition().y, to.getPosition().y) + CONNECTION_PADDING);
            size.setY(Math.min(src.getPosition().y + src.getSize().y, to.getPosition().y + to.getSize().y) - pos.y - CONNECTION_PADDING);
             
             pos.x = 0f;
             size.x = 1.0f/16;
         }
        else if(src.getPosition().x + src.getSize().x <= to.getPosition().x){
            pos.setY(Math.max(src.getPosition().y, to.getPosition().y) + CONNECTION_PADDING);
            size.setY(Math.min(src.getPosition().y + src.getSize().y, to.getPosition().y + to.getSize().y) - pos.y - CONNECTION_PADDING);
             
             size.x = 1.0f/16;
             pos.x = 1f - size.x;
         }
         
         
         //Basic Rectangle
         Quad q = new Quad(size.x, size.y);
         Geometry geom = new Geometry("Quad", q);
         Material mat = new Material(Main.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
         mat.setColor("Color", ColorRGBA.Green);
         geom.setMaterial(mat);
         geom.setLocalTranslation(pos.x, pos.y, 0f);
         attachChild(geom);
     }
     
     public AreaNode getSrc() {
         return src;
     }
 
     public AreaNode getTo() {
         return to;
     }
     
     public void executeMovements(){
         if(src.getNumZombies() >= toMoveZombies){
             src.setNumZombies(src.getNumZombies()-toMoveZombies);
             to.setNumZombies(to.getNumZombies()+toMoveZombies);
         }
         if(src.getNumHumans() >= toMoveHumans){
             src.setNumHumans(src.getNumHumans()-toMoveHumans);
             to.setNumHumans(to.getNumHumans()+toMoveHumans);
         }
         if(src.getNumSoldiers() >= toMoveSoldiers){
             src.setNumSoldiers(src.getNumSoldiers()-toMoveSoldiers);
             to.setNumSoldiers(to.getNumSoldiers()+toMoveSoldiers);
         }
         
         toMoveHumans = toMoveSoldiers = toMoveZombies = 0;
     }
     
     public void moveZombies(int num){
         toMoveZombies += num;
     }
     
     public void moveHumans(int num){
         toMoveHumans += num;
     }
     
     public void moveSoldiers(int num){
         toMoveSoldiers += num;
     }
     
 }
