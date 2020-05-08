 package jDistsim.application.designer.common;
 
 import jDistsim.utils.pattern.mvc.IComponentFactory;
 
 import javax.swing.*;
 import javax.swing.table.TableModel;
 import java.awt.*;
 import java.awt.event.ActionListener;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 27.10.12
  * Time: 15:44
  */
 public class ComponentFactory implements IComponentFactory {
 
     @Override
     public JFrame frame(String title, JComponent contentPane, JMenuBar menuBar) {
         JFrame frame = new JFrame();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setLayout(new BorderLayout());
         if (title != null) frame.setTitle(title);
         if (menuBar != null) frame.setJMenuBar(menuBar);
         if (contentPane != null) frame.getContentPane().add(contentPane, BorderLayout.CENTER);
        frame.setPreferredSize(new Dimension(1000, 600));
         return frame;
     }
 
     @Override
     public JFrame showFrame(JFrame frame) {
         frame.pack();
         centerFrame(frame);
         frame.setVisible(true);
         return frame;
     }
 
     @Override
     public JFrame centerFrame(JFrame frame) {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension frameSize = frame.getSize();
         if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
         if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
         frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
         return frame;
     }
 
     @Override
     public JPanel panel(LayoutManager layoutManager, String title) {
         return null;
     }
 
     @Override
     public JLabel label(String text, Font font) {
         return null;
     }
 
     @Override
     public JTextField textField(int columnSize, boolean editable) {
         return null;
     }
 
     @Override
     public JButton button(String label, ActionListener actionListener) {
         return null;
     }
 
     @Override
     public JPanel buttons(JButton... buttons) {
         return null;
     }
 
     @Override
     public JTable table(TableModel tableModel) {
         return null;
     }
 }
