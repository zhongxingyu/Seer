 package vooga.towerdefense.view.gamescreens;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import vooga.towerdefense.controller.Controller;
 
 
 /**
  * Next Wave Screen holds the button to start the next wave.
  * 
  * @author Angelica Schwartz
  */
 public class NextWaveScreen extends JPanel {
 
     /**
      * default serialized id.
      */
     private static final long serialVersionUID = 1L;
    // TODO: get this name from resources
     /**
      * string to display on the button.
      */
     public static final String WAVE_SCREEN_BUTTON_KEYWORD = "NextWaveButtonName";
     /**
      * constant for the wave timer. 
      */
     private static final String NEXT_WAVE_TEXT = "Time Until Next Wave: ";
     /**
      * controller associated with this screen.
      */
     private Controller myController;
     /**
      * jLabel to display the time until the next wave.
      */
     private JLabel myNextWaveTimerDisplay;
     /**
      * button to start the next wave.
      */
     private JButton nextWaveButton;
     /**
      * mouse listener for the button.
      */
     private MouseAdapter myMouseAdapter;
 
     /**
      * Constructor.
      * 
      * @param size
      * @param controller
      */
     public NextWaveScreen (Dimension size, Controller controller) {
         setPreferredSize(size);
         setLayout(new BorderLayout());
         myController = controller;
         nextWaveButton =
                 new JButton(myController.getStringFromResources(WAVE_SCREEN_BUTTON_KEYWORD));
         add(nextWaveButton, BorderLayout.NORTH);
         myNextWaveTimerDisplay = new JLabel(NEXT_WAVE_TEXT);
         add(myNextWaveTimerDisplay, BorderLayout.CENTER);
         makeMouseAdapter();
         nextWaveButton.addMouseListener(myMouseAdapter);
         setVisible(true);
     }
     
     /**
      * updates the timer display with the time left.
      * @param timeLeft
      */
     public void updateTimerDisplay(String timeLeft) {
        myNextWaveTimerDisplay = new JLabel(NEXT_WAVE_TEXT + timeLeft);
        add(myNextWaveTimerDisplay, BorderLayout.CENTER);
     }
 
     /**
      * helper method to create the mouse adapter.
      */
     private void makeMouseAdapter () {
         myMouseAdapter = new MouseAdapter() {
             @Override
             public void mouseClicked (MouseEvent e) {
                 if (e.getSource().equals(nextWaveButton)) {
                     myController.startNextLevel();
                 }
             }
         };
     }
 }
