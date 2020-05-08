 package prototype;
 
 import java.awt.Dimension;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.vecmath.*;
 import jp.sourceforge.acerola3d.a3.*;
 import com.github.hiuprocon.pve.ui.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 public class Simulator2GUI extends JFrame implements ActionListener, ChangeListener {
     private static final long serialVersionUID = 1L;
     Simulator2 simulator;
     A3Canvas canvas;
     A3SubCanvas sc1;
     A3SubCanvas sc2;
     JCheckBox oneCarCB;
     JButton resetWorldB;
     JSlider waitTimeS;
     JLabel timeL;
     JButton defaultViewB;
     JButton topViewB;
     JButton frontViewB;
     JButton sideViewB;
     JCheckBox parallelCB;
     JCheckBox polygonizeCB;
     JButton snapshotB;
     JTextArea textArea;
     Vector3d lookAt = new Vector3d(0.0, 0.0, 6.0);
     Vector3d camera = new Vector3d(0.0, 3.0, -6.0);
     Vector3d up = new Vector3d(0.0, 1.0, 0.0);
 
     public Simulator2GUI(Simulator2 simulator) {
         super("Simulator2");
         this.simulator = simulator;
         HBox box1 = new HBox();
         VBox box3 = new VBox();
         textArea = new JTextArea();
         textArea.setEditable(false);
         JScrollPane sp = new JScrollPane(textArea);
         sp.setPreferredSize(new Dimension(800,150));
         canvas = (A3Canvas) simulator.w.getMainCanvas();
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         defaultView();
         canvas.setSize(800, 550);
         canvas.setCanvasWidthInPWorld(2.0);
 
         VBox box2 = new VBox();
 
         VBox controlBox = new VBox();
         box2.myAdd(controlBox, 1);
         oneCarCB = new JCheckBox("only one car");
         oneCarCB.addActionListener(this);
         controlBox.myAdd(oneCarCB,1);
         resetWorldB = new JButton("Rese World");
         resetWorldB.addActionListener(this);
         controlBox.myAdd(resetWorldB, 1);
         HBox sliderBox = new HBox();
         sliderBox.myAdd(new JLabel("wait time"),0);
         waitTimeS = new JSlider(0,33,33);
         waitTimeS.addChangeListener(this);
         sliderBox.myAdd(waitTimeS,1);
         controlBox.myAdd(sliderBox,1);
         timeL = new JLabel("");
         updateTime(0);
         controlBox.myAdd(timeL,1);
         defaultViewB = new JButton("Default View");
         defaultViewB.addActionListener(this);
         controlBox.myAdd(defaultViewB, 1);
         topViewB = new JButton("Top View");
         topViewB.addActionListener(this);
         controlBox.myAdd(topViewB, 1);
         frontViewB = new JButton("Front View");
         frontViewB.addActionListener(this);
         controlBox.myAdd(frontViewB, 1);
         sideViewB = new JButton("Side View");
         sideViewB.addActionListener(this);
         controlBox.myAdd(sideViewB, 1);
         parallelCB = new JCheckBox("parallel");
         parallelCB.addActionListener(this);
         controlBox.myAdd(parallelCB, 1);
         polygonizeCB = new JCheckBox("polygonize");
         polygonizeCB.addActionListener(this);
         controlBox.myAdd(polygonizeCB, 1);
         snapshotB = new JButton("snapshot");
         snapshotB.addActionListener(this);
         controlBox.myAdd(snapshotB, 1);
 
         sc1 = A3SubCanvas.createA3SubCanvas(200, 150);
         canvas.addA3SubCanvas(sc1);
         box2.myAdd(sc1,1);
 
         sc2 = A3SubCanvas.createA3SubCanvas(200, 150);
         canvas.addA3SubCanvas(sc2);
         box2.myAdd(sc2, 1);
 
         box3.myAdd(sp,1);
         box3.myAdd(canvas,1);
         box1.myAdd(box2, 1);
         box1.myAdd(box3, 1);
         add(box1);
         //pack();
         setBounds(500,0,1000,700);
         setVisible(true);
 
        canvas.setBackground(new A3Background(0.1f, 0.3f, 0.5f));

         //for Java1.7.0_40ea bug of Mac
         if (System.getProperty("os.name").equals("Mac OS X")) {
             try{Thread.sleep(1000);}catch(Exception e){;}
             setBounds(500,0,1010,710);
             setBounds(500,0,1000,700);
         }
     }
 
     void defaultView() {
         double y = 50;
         double z = 150;
         canvas.setCameraLocImmediately(0, y, z);
         canvas.setCameraLookAtPointImmediately(0, 0, 0);
         canvas.setNavigationMode(A3CanvasInterface.NaviMode.SIMPLE,
                 Math.sqrt(y * y + z * z));
     }
 
     void topView() {
         canvas.setCameraLocImmediately(0,100,0);
         canvas.setCameraRevImmediately(-90, 0, 0);
         canvas.setNavigationMode(A3CanvasInterface.NaviMode.SIMPLE,100.0);
     }
 
     void frontView() {
         canvas.setCameraLocImmediately(0,0,100);
         canvas.setCameraRevImmediately(0, 0, 0);
         canvas.setNavigationMode(A3CanvasInterface.NaviMode.SIMPLE,100.0);
     }
 
     void sideView() {
         canvas.setCameraLocImmediately(100,0,0);
         canvas.setCameraRevImmediately(0, 90, 0);
         canvas.setNavigationMode(A3CanvasInterface.NaviMode.SIMPLE,100.0);
     }
 
     public void setCar1(CarInterface c) {
         sc1.setAvatar(c.getMainA3());
         sc1.setNavigationMode(A3CanvasInterface.NaviMode.CHASE, lookAt, camera,
                 up, 10.0);
     }
 
     public void setCar2(CarInterface c) {
         sc2.setAvatar(c.getMainA3());
         sc2.setNavigationMode(A3CanvasInterface.NaviMode.CHASE, lookAt, camera,
                 up, 10.0);
     }
 
     public void appendText(String s) {
         textArea.append(s+"\n");
     }
 
     public void updateTime(double t) {
         timeL.setText(String.format("time: %9.2f",t));
     }
     @Override
     public void actionPerformed(ActionEvent ae) {
         Object source = ae.getSource();
         if (source==oneCarCB) {
             if (oneCarCB.isSelected()==true)
                 simulator.deactivateOneCar();
             else
                 simulator.activateTwoCars();
         } else if (source==resetWorldB) {
             simulator.initWorld();
             oneCarCB.setSelected(false);
             waitTimeS.setValue(30);
             updateTime(0);
             textArea.setText("");
         } else if (source==defaultViewB) {
             defaultView();
         } else if (source==topViewB) {
             topView();
         } else if (source==frontViewB) {
             frontView();
         } else if (source==sideViewB) {
             sideView();
         } else if (source==parallelCB) {
             if (parallelCB.isSelected()==true) 
                 canvas.setProjectionMode(ProjectionMode.PARALLEL);
             else
                 canvas.setProjectionMode(ProjectionMode.PERSPECTIVE);
         } else if (source==polygonizeCB) {
             if (polygonizeCB.isSelected()==true) {
                 simulator.w.polygonize();
             } else
                 simulator.w.unpolygonize();
         } else if (source==snapshotB) {
             BufferedImage bi = canvas.snapshot();
             JFileChooser fc = new JFileChooser();
             int selected = fc.showSaveDialog(canvas);
             if (selected == JFileChooser.APPROVE_OPTION){
                 File file = fc.getSelectedFile();
                 try {
                     ImageIO.write(bi,"png",file);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     @Override
     public void stateChanged(ChangeEvent ce) {
         simulator.setWaitTime(waitTimeS.getValue());
     }
 }
