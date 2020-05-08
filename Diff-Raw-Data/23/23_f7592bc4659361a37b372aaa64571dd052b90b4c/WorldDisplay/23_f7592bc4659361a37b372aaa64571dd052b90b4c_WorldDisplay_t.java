 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package student.gui;
 
 import java.awt.BorderLayout;
 import javax.swing.*;
 import student.world.World;
 
 public class WorldDisplay extends JPanel{
     public GridPanel gp;
     public JPanel sp;
     public JTextArea state;
     public World WORLD;
 
 
     public WorldDisplay(World world) {
         WORLD = world;
 //        xSTART = 100;
 //        ySTART = 100;
         gp = generateGridpanel();
         sp = new JPanel();
         state = new JTextArea();
         sp.add(state);
         setLayout(new BorderLayout());
        add(gp, BorderLayout.CENTER);
         //add(sp, BorderLayout.WEST);
         gp.setVisible(true);
     }
     public final GridPanel generateGridpanel(){
         GridPanel grid = new GridPanel(WORLD);
         grid.setSize(10000, 10000);
         return grid;
     }
     public void updateState(){
         //state.setText(world.currentLocation.getState());
     }
     /**
      * unit = hexlength/4
      * height = (numhexes*2-1)hexlength
      * width = numhexes*hexlength   //numhexes*hexlength-[(numhexes-1)*2]*hexlength/4
      */
     /**
      * draw first hex at 
      * x = numhexes*hexlength - (numhexes-1)
      * y = 0
      */
     /**
      * draw hex at
      * y = startY + (
      */
     /**
      * Finds the point 
      * @param row
      * @param col
      * @param startpoint
      * @param hexsize
      * @return 
      */
     public int[] point(int row, int col, int[] startpoint, int hexsize)
     {
         return new int[]{startpoint[0]+(hexsize*3/4)*row, startpoint[1]+(hexsize/2)*col}; //[x, y]
     }
 }
