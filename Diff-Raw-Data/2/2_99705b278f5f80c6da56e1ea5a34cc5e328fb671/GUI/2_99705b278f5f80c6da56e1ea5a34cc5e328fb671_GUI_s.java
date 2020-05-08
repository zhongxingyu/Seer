 package datas;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.LinkedHashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.awt.event.*;
 import javax.imageio.ImageIO;
 import java.io.*;
 
 public class GUI extends JFrame {
 
     private Controller control;
     private int move = 1;
     private JPanel panel;
     private JPanel frame;
     private LinkedHashMap<Integer, JButton> images;
     private Image cross;
     private Image empty;
     private Image circle;
     private JButton restart;
     private CrossListener listener = new CrossListener();
     private JLabel info;
 
     public void registerController(Controller inControl) {
         this.control = inControl;
     }
 
     public GUI() {
         setUp();
     }
 
     public void setUp() {
         setTitle("Ristinolla");
         panel = new JPanel(new GridLayout(10, 10));
         try {
             empty = ImageIO.read(new File("Blank.gif"));
             cross = ImageIO.read(new File("Cross.gif"));
             circle = ImageIO.read(new File("Circle.gif"));
         } catch (IOException e) {
             e.printStackTrace();
         }
         images = new LinkedHashMap<Integer, JButton>();
         for (int i = 0; i < 100; i++) {
             JButton b = new JButton(new ImageIcon(empty));
             b.setContentAreaFilled(false);
             b.addActionListener(listener);
             b.setSize(20, 20);
             images.put(i, b);
             panel.add(b);
         }
        info = new JLabel("Nollan Vuoro");
         frame = new JPanel(new BorderLayout());
         frame.add(panel, BorderLayout.NORTH);
         frame.add(info, BorderLayout.SOUTH);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setContentPane(frame);
         setSize(500, 500);
         setVisible(true);
     }
 
     public void makeCross(JButton button) {
         if (move % 2 == 0) {
             int newCircle = control.getAiMove();
             images.put(newCircle,new JButton(new ImageIcon(circle)));
             control.makeMove(newCircle,2);
             info = new JLabel("Ristin vuoro");
             refresh();
             move++;
         } else {
             Set<Entry<Integer, JButton>> es = images.entrySet();
             Iterator<Entry<Integer, JButton>> ei = es.iterator();
             Map.Entry entry;
             while (ei.hasNext()) {
                 entry = ei.next();
                 if (entry.getValue().equals(button)) {
                     int newCross = (Integer) entry.getKey();
                     images.put(newCross, new JButton(new ImageIcon(cross)));
                     control.makeMove(newCross,1);
                     info = new JLabel("Nollan vuoro");
                     refresh();
                     move++;
                 }
             }
         }
     }
 
     public void refresh() {
         panel = new JPanel(new GridLayout(10, 10));
         Set<Entry<Integer, JButton>> es = images.entrySet();
         Iterator<Entry<Integer, JButton>> ei = es.iterator();
         while (ei.hasNext()) {
             panel.add(ei.next().getValue());
         }
         frame = new JPanel(new BorderLayout());
         frame.add(panel, BorderLayout.NORTH);
         frame.add(info, BorderLayout.SOUTH);
         setContentPane(frame);
         setVisible(true);
     }
 
     private class CrossListener implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             makeCross((JButton) e.getSource());
         }
     }
 }
