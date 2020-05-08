 package worlds;
 
 import worlds.intf.WorldGridView;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.awt.*;
 
 import utils.Utils;
 
 /**
  * Created by IntelliJ IDEA.
  * User: adenysenko
  * Date: Jan 13, 2009
  * Time: 4:21:51 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ComparableSensors1 implements WorldGridView, Serializable {
   Color[] cells = new Color[2];
   int availableResults=0;
  int countEq=0;
 
   public ComparableSensors1(){
     genCells();
   }
 
   void genCells(){
     Color[] cc = {
         Color.BLACK, Color.YELLOW, Color.ORANGE,
         Color.RED, Color.GREEN, Color.WHITE,
         Color.GRAY, Color.BLUE, Color.CYAN,
 
     };
     for( int i=0; i<cells.length; i++ ){
       cells[i] = cc[(int)(Math.random()*cc.length)];
     }
    if( cells[0]==cells[1] ){
      System.out.println("world eq count " + (++countEq) );
    }
   }
 
   public int availableResults() {
     return availableResults;
   }
 
   public boolean commandWrong(String cmd) {
     return false;
   }
 
   public List<String> commands() {
     return Arrays.asList("F0","F1");
   }
 
   public Map<String, Object> view() {
     Map<String, Object> ret = new HashMap<String, Object>();
     ret.put("a", Utils.color2name(cells[0]));
     ret.put("b", Utils.color2name(cells[1]));
     ret.put("q", Boolean.toString(cells[0]==cells[1]));
     return ret;
   }
 
   public int command(String cmd) {
     int res=0;
     if( cmd.equals("F0") && cells[0]!=cells[1] ){
       res=1;
     }
     if( cmd.equals("F1") && cells[0]==cells[1] ){
       res=1;
     }
     availableResults++;
     genCells();
     return res;
   }
 
   public int getWidth() {
     return 2;
   }
 
   public int getHeight() {
     return 1;
   }
 
   public Color getColorDisplay(int x, int y) {
     return cells[x];
   }
 
   public String getChar(int x, int y) {
     return null;
   }
 
   public Map<String, Point> sensorLocations() {
     return null;
   }
 }
