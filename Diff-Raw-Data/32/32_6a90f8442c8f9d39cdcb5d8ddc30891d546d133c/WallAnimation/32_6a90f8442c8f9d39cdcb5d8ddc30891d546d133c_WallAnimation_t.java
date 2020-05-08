 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package level;
 
 import Utilities.Image2D;
 import Utilities.ImageCollection;
 import Utilities.Rect;
 import Utilities.Vector2;
 import java.awt.Color;
 import java.io.File;
 import java.util.ArrayList;
 import raycaster.Main;
 
 /**
  *
  * @author kdsweenx
  */
 public class WallAnimation {
     ArrayList<ArrayList<Image2D>> sprites;
     int index;
     int framesPerSecond;
     int lastCycleNum;
     int count;
     
    int numberOfRays;
    String folder;
    
     public WallAnimation(String folder, int fps, int numberOfRays){
         sprites=new ArrayList<>();
        this.numberOfRays=numberOfRays;
        this.folder=new String(folder);
         framesPerSecond=fps;
         lastCycleNum=0;
         index=0;
         count=0;
     }
     
     public void Draw(ImageCollection batch, Vector2 pos, float scaleX, float scaleY, float angle, float percent, Color c, Rect drawnArea, int depth, int i){
         batch.Draw(sprites.get(i).get(index), pos, angle, c, percent, scaleX, scaleY, drawnArea, depth);
     }
     
     public void update(){
         int cyc=Main.inst.cycleNum;
         if(cyc > lastCycleNum || cyc==0){
             count+=1;
             if(count >= 60/framesPerSecond){
                 index+=1;
                 index%=sprites.get(0).size();
                 count=0;
             }
             lastCycleNum=cyc;
         }
     }
     
     
     private class load implements Runnable{
         ArrayList<ArrayList<Image2D>> sprites;
         String file;
 
         public load(ArrayList<ArrayList<Image2D>> sprites, String file) {
             this.sprites = sprites;
             this.file = file;
         }
         
         @Override
         public void run() {
             ArrayList<Image2D> sprite = new ArrayList<>();
             File f = new File(file);
             for (int i = 1; i <= f.list().length; i++) {
 //            System.out.println(folder+"/"+i+".png");
                 sprite.add(new Image2D(file + "/" + i + ".png"));
             }
             sprites.add(sprite);
         }
     }
     
    public void make(){
        for(int i=0; i<numberOfRays; i++){
            new Thread(new load(sprites,folder)).start();
        }
    }
    
 }
