 package de.schelklingen2008.canasta.client.view;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.logging.Logger;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import de.schelklingen2008.canasta.client.Constants;
 import de.schelklingen2008.canasta.client.controller.Controller;
 import de.schelklingen2008.util.LoggerFactory;
 
 public class BoardPanel extends JPanel
 {
 
     private static final Logger sLogger = LoggerFactory.create();
 
     public BoardPanel(final Controller controller)
     {
 
         setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
         setBackground(new Color(Constants.COLOR_OUTLAY));
 
         JButton outlayButton = new JButton("Outlay");
 
         JPanel bp = new JPanel();
         bp.setLayout(new BoxLayout(bp, BoxLayout.PAGE_AXIS));
         bp.setBackground(new Color(Constants.COLOR_OUTLAY));
         bp.setBorder(BorderFactory.createEmptyBorder(0, 350, 0, 0));
 
         final BoardView view = new BoardView(controller);
         ActionListener outlayListener = new ActionListener()
         {
 
             public void actionPerformed(ActionEvent e)
             {
                 sLogger.info("pressed NewOutlay");
 
                controller.makeOutlay(view.getSelectedCardNumbers(), view.isDiscardSelected());
             }
         };
 
         outlayButton.addActionListener(outlayListener);
 
         bp.add(outlayButton);
 
         add(view);
         add(bp);
 
         //        
 
     }
 }
