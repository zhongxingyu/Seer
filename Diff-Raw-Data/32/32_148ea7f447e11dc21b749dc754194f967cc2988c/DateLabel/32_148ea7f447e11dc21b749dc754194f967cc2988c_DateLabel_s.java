 package at.ac.tuwien.sepm.ui.calender;
 
 import at.ac.tuwien.sepm.entity.Date;
 import at.ac.tuwien.sepm.entity.DateEntity;
 import at.ac.tuwien.sepm.entity.LvaDate;
 import at.ac.tuwien.sepm.service.TimeFrame;
 import at.ac.tuwien.sepm.ui.UIHelper;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import net.miginfocom.swing.MigLayout;
 import org.joda.time.DateTime;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * @author Markus MUTH
  */
 public class DateLabel extends JLabel{
     private static final int MAX_TEXT_LENGTH=20;
     private Date date;
 
     public DateLabel(Date date) {
         this.date=date;
         if(date.getName()==null || date.getName().equals("") || consistsOf(' ', date.getName())){
             this.setText("...");
         } else {
             this.setText(cutText(date.getName()));
         }
         this.setFont(new Font("Arial", Font.PLAIN, 10));
         this.addMouseListener(new PrivateMouseListener());
     }
 
     public DateTime getStart() {
         return date.getTime().from();
     }
 
     public DateTime getStop() {
         return date.getTime().to();
     }
 
     public TimeFrame getTime() {
         return date.getTime();
     }
 
     private boolean consistsOf(char c, String s) {
         for(int i=0; i<s.length(); i++) {
             if(c != s.charAt(i)) {
                 return false;
             }
         }
         return true;
     }
 
     private String cutText(String text) {
         if(text.length() > MAX_TEXT_LENGTH) {
             text = text.substring(0,17) + "...";
         }
         return text;
     }
 
     private Date getDate() {
         return date;
     }
 
     class PrivateMouseListener extends MouseAdapter {
         public void mouseClicked(MouseEvent evt) {
             if (evt.getClickCount() == 2) {
                 UpdateDateFrameFactory.createFrame(getDate());
             }
         }
 
         @Override
         public void mousePressed(MouseEvent e) {}
 
         @Override
         public void mouseReleased(MouseEvent e) {
             if(e.getButton()==3) {
                 PopUpMenu menu = new PopUpMenu();
                 menu.show(e.getComponent(), e.getX(), e.getY());
             }
         }
 
         @Override
         public void mouseEntered(MouseEvent e) {}
 
         @Override
         public void mouseExited(MouseEvent e) {}
     }
 
     private class PopUpMenu extends JPopupMenu {
         private JMenuItem edit;
         private JMenuItem attendance;
         private JMenuItem showRoom;
         private JMenuItem share;
 
         public PopUpMenu(){
             edit = new JMenuItem("Bearbeiten");
             attendance = new JMenuItem("Anwesenheit");
             showRoom = new JMenuItem("Wegbeschreibung");
             share = new JMenuItem("Share");
             add(edit);
             add(attendance);
             add(showRoom);
             add(share);
             addActionListeners();
         }
 
         private void addActionListeners() {
             edit.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseReleased(MouseEvent e) {
                       if(date instanceof LvaDate) {
                           PanelTube.backgroundPanel.viewLvaDate((LvaDate)date);
                       } else if(date instanceof DateEntity) {
                           PanelTube.backgroundPanel.viewDate((DateEntity)date);
                       }
                 }
             });
             attendance.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseReleased(MouseEvent e) {
                     // TODO implement this
                 }
             });
             showRoom.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseReleased(MouseEvent e) {
                     if(date instanceof LvaDate) {
                        PanelTube.backgroundPanel.openRoomBrowser(((LvaDate) date).getRoom());
                     }
                 }
             });
             share.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseReleased(MouseEvent e) {
                     // TODO implement this
                 }
             });
 
         }
     }
 
     private static class UpdateDateFrameFactory extends JFrame {
         private static void createFrame(Date d) {
             if(d instanceof DateEntity) {
                 new UpdateDateFrame((DateEntity)d);
             } else if (d instanceof LvaDate) {
                 new UpdateLvaDateFrame((LvaDate)d);
             }
         }
 
         private static class UpdateDateFrame extends JFrame {
             // TODO implement this class correctly
             private DateEntity date;
             private JLabel l0=new JLabel();
             private JLabel l1=new JLabel();
 
             public UpdateDateFrame(DateEntity d) {
                 this.setLayout(new MigLayout());
                 this.date = d;
                 this.setLocation(500,500);
                 l0.setText("DateLabel.UpdateDateFrame ... ");
                 l1.setText("Name of date: " + date.getName());
                 this.setMinimumSize(new Dimension((int)(l0.getSize().getWidth() + l1.getSize().getWidth()), (int)(l0.getSize().getHeight() + l1.getSize().getHeight())));
                 this.add(l0, "wrap");
                 this.add(l1);
                 this.pack();
                 this.revalidate();
                 this.repaint();
                 this.setVisible(true);
             }
         }
 
         private static class UpdateLvaDateFrame extends JFrame {
             // TODO implement this class correctly
             private LvaDate date;
             private JLabel l0=new JLabel();
             private JLabel l1=new JLabel();
 
             public UpdateLvaDateFrame(LvaDate d) {
                 this.setLayout(new MigLayout());
                 this.date = d;
                 this.setLocation(500,500);
                 l0.setText("DateLabel.UpdateLvaDateFrame ... ");
                 l1.setText("Name of date: " + date.getName());
                 this.setMinimumSize(new Dimension((int)(l0.getSize().getWidth() + l1.getSize().getWidth()), (int)(l0.getSize().getHeight() + l1.getSize().getHeight())));
                 this.add(l0, "wrap");
                 this.add(l1);
                 this.pack();
                 this.revalidate();
                 this.repaint();
                 this.setVisible(true);
             }
         }
     }
 }
