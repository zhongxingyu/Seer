 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mvc.views;
 
 import java.util.ArrayList;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import org.netbeans.lib.awtextra.AbsoluteLayout;
 
 /**
  *
  * @author David
  */
 public class LeftTopView {
     private ArrayList<JLabel> jLabels;
     private JLabel jLabelPark;
     private JPanel jPanelLeftTop;
     
     private int NUMLINES = 4;  //TODO pass in the constructor?
     private final int LEFTLABELSPAN = 30;
     private final int TOPLABELSPAN = 90;
     private final int SPANBETWEENLABELS = 20;
     private final int EXTRASPANFORPARKLABEL = 10;
     
     public LeftTopView(JPanel jPanelLeft){
         jPanelLeftTop = new JPanel();
         jLabelPark = new JLabel();
         
         jPanelLeftTop.setLayout(new AbsoluteLayout());
         
         jLabels = new ArrayList<>();
         for(int i=0; i<NUMLINES; i++){
             JLabel aux = new JLabel();
             aux.setText("Line"+(i+1));
             jLabels.add(aux);
             jPanelLeftTop.add(jLabels.get(i), new org.netbeans.lib.awtextra.AbsoluteConstraints(LEFTLABELSPAN, TOPLABELSPAN+i*SPANBETWEENLABELS, -1, -1));
         }
 
         jLabelPark.setText("Park");
         jPanelLeftTop.add(jLabelPark, new org.netbeans.lib.awtextra.AbsoluteConstraints(LEFTLABELSPAN, TOPLABELSPAN+NUMLINES*SPANBETWEENLABELS+EXTRASPANFORPARKLABEL, -1, -1));
 
         jPanelLeft.add(jPanelLeftTop);
         
     }
     
     public void addProductionLine(){
         JLabel aux = new JLabel();
         aux.setText("Line"+NUMLINES+1);
         jLabels.add(aux);
         jPanelLeftTop.add(jLabels.get(NUMLINES), new org.netbeans.lib.awtextra.AbsoluteConstraints(LEFTLABELSPAN, TOPLABELSPAN+NUMLINES*SPANBETWEENLABELS, -1, -1));
         NUMLINES++;
         //move the "Park" label downwards
         jPanelLeftTop.remove(jLabelPark);
         jPanelLeftTop.add(jLabelPark, new org.netbeans.lib.awtextra.AbsoluteConstraints(LEFTLABELSPAN, TOPLABELSPAN+NUMLINES*SPANBETWEENLABELS+EXTRASPANFORPARKLABEL, -1, -1));
     }
    /* 
     public void removeProductionLine(){
         if(NUMLINES>0){
             //call a method to move all the activities from the last production line to the "Park" section!
             jPanelLeftTop.remove(jLabels.get(NUMLINES-1));
             jLabels.remove(NUMLINES-1);
             NUMLINES--;
             //move the "Park" label upwards
             jPanelLeftTop.remove(jLabelPark);
             jPanelLeftTop.add(jLabelPark, new org.netbeans.lib.awtextra.AbsoluteConstraints(LEFTLABELSPAN, TOPLABELSPAN+NUMLINES*SPANBETWEENLABELS+EXTRASPANFORPARKLABEL, -1, -1));
         }
     }
     */
 }
