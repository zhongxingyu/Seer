 package ch.bli.mez.view.management;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 public class MissionTitlePanel extends JPanel{
   
   private static final long serialVersionUID = 1L;
   private JLabel missionName;
   private JLabel comment;
   private JPanel spacer;
 
   public MissionTitlePanel(){
     build();
   }
   
   private void build() {
     setLayout(new FlowLayout(FlowLayout.CENTER, 13, 0));
     
     missionName = new JLabel("Name");
     missionName.setHorizontalAlignment(SwingConstants.LEFT);
     this.add(missionName);
     missionName.setPreferredSize(new Dimension(114, 25));
 
     comment = new JLabel("Kommentar");
     comment.setHorizontalAlignment(SwingConstants.LEFT);
     this.add(comment);
    comment.setPreferredSize(new Dimension(171, 25));
 
     spacer = new JPanel();
     FlowLayout flowLayout = (FlowLayout) spacer.getLayout();
     flowLayout.setHgap(145);
     flowLayout.setVgap(0);
     add(spacer);
   }
 
 }
