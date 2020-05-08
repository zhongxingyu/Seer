 package ch.bli.mez.view.management;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 public class PositionTitlePanel extends JPanel {
 
   private static final long serialVersionUID = 1L;
   private JLabel positionName;
   private JLabel comment;
   private JLabel positionCode;
   private JPanel spacer;
   private JLabel category;
 
   public PositionTitlePanel() {
     build();
   }
 
   private void build() {
     setLayout(new FlowLayout(FlowLayout.CENTER, 13, 0));
     
     positionCode = new JLabel("Position");
     positionCode.setHorizontalAlignment(SwingConstants.LEFT);
     this.add(positionCode);
     positionCode.setPreferredSize(new Dimension(80, 25));
     
     positionName = new JLabel("Name");
     positionName.setHorizontalAlignment(SwingConstants.LEFT);
     this.add(positionName);
     positionName.setPreferredSize(new Dimension(114, 25));
     
     category = new JLabel("Auftrag");
     category.setHorizontalAlignment(SwingConstants.LEFT);
     this.add(category);
     category.setPreferredSize(new Dimension(114, 25));
 
     comment = new JLabel("Kommentar");
     comment.setHorizontalAlignment(SwingConstants.LEFT);
     this.add(comment);
    comment.setPreferredSize(new Dimension(171, 25));
 
     spacer = new JPanel();
     FlowLayout flowLayout = (FlowLayout) spacer.getLayout();
     flowLayout.setHgap(98);
     flowLayout.setVgap(0);
     add(spacer);
   }
 }
