 package ch.guitarpracticebuddy;
 
 import ch.guitarpracticebuddy.domain.PracticeBuddyBean;
 import ch.guitarpracticebuddy.ui.GuitarBuddyUi;
 import ch.guitarpracticebuddy.util.KeyEventDispatcherUtil;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 public class Main {
 
     public static void main(String[] args) {
 
         System.setProperty("objectdb.home", "/Users/mjunker/test");
         System.setProperty("guitarbuddy.home", "/Users/mjunker/test");
 
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("$objectdb/db/guitarpracticebuddy.odb");
         final EntityManager em = emf.createEntityManager();
 
         em.getTransaction().begin();
         GuitarBuddyUi guitarBuddyUi = createMainModel(em);
         openUi(em, guitarBuddyUi);
 
     }
 
     private static GuitarBuddyUi createMainModel(EntityManager em) {
         GuitarBuddyUi guitarBuddyUi = new GuitarBuddyUi();
         PracticeBuddyBean practiceBuddy = new PracticeBuddyBean(em);
         guitarBuddyUi.getPlanningForm().setData(practiceBuddy);
         guitarBuddyUi.getPracticeForm().setData(practiceBuddy);
         return guitarBuddyUi;
     }
 
     private static void openUi(final EntityManager em, GuitarBuddyUi guitarBuddyUi) {
        final JFrame frame = new JFrame("GuitarBuddy");
         frame.setContentPane(guitarBuddyUi.getMainPanel());
         frame.setPreferredSize(new Dimension(1440, (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()));
 
         frame.pack();
         frame.setVisible(true);
 
         frame.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                int answer = JOptionPane.showConfirmDialog(frame, "Do you want to save your changes?");
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);


                if (answer == JOptionPane.YES_OPTION) {
                    em.getTransaction().commit();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


                } else if (answer == JOptionPane.NO_OPTION) {
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                }
             }
         });
 
         KeyEventDispatcherUtil.addKeyListener(new KeyEventDispatcherUtil.KeyEventListener() {
             @Override
             public void onKeyPressed(KeyEvent e) {
                 if (e.isControlDown() && e.getKeyChar() == 's') {
                     em.getTransaction().commit();
                     em.getTransaction().begin();
                 }
             }
         });
 
     }
 }
 
