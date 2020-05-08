 package gau.gui;
 
 import gau.db.Repo;
 import gau.models.Game;
 import gau.models.RealTeam;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 import javax.swing.SwingWorker;
 
 public class GameWithPlugs extends JPanel {
 
    private static final long serialVersionUID = 1L;
    private JTextPane jTextPane = null;
    private JButton jButton = null;
    private GameRun game = null;
    private boolean test = false;
 
    /**
     * This is the default constructor.
     */
    public GameWithPlugs() {
       super();
       initialize();
    }
 
    /**
     * This is the default constructor.
     * @param testp
     */
    public GameWithPlugs(final boolean testp) {
       super();
       test = testp;
       initialize();
    }
 
    /**
     * This method initializes this.
     */
    private void initialize() {
       GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
       gridBagConstraints4.gridx = 1;
       gridBagConstraints4.gridy = 0;
       GridBagConstraints gridBagConstraints = new GridBagConstraints();
       gridBagConstraints.fill = GridBagConstraints.BOTH;
       gridBagConstraints.gridy = 0;
       gridBagConstraints.weightx = 1.0;
       gridBagConstraints.weighty = 1.0;
       gridBagConstraints.gridx = 0;
       this.setSize(519, 342);
       this.setBackground(new Color(206, 218, 251));
       this.setLayout(new GridBagLayout());
       this.add(getJTextPane(), gridBagConstraints);
       this.add(getJButton(), gridBagConstraints4);
    }
 
    /**
     * This method initializes jTextPane.
     *
     * @return javax.swing.JTextPane
     */
    private JTextPane getJTextPane() {
       if (jTextPane == null) {
          jTextPane = new JTextPane();
          jTextPane.setEditable(false);
          jTextPane.setBackground(new Color(206, 218, 251));
       }
       return jTextPane;
    }
 
    /**
     * This method initializes jButton.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
       if (jButton == null) {
          jButton = new JButton();
          jButton.setText("Iniciar");
          jButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(final java.awt.event.ActionEvent e) {
                if (game == null || game.isDone()) {
                   game = new GameRun();
                   game.execute();
                } else {
                   game.cancel(true);
                   game = null;
                }
             }
          });
       }
       return jButton;
    }
 
    class GameRun extends SwingWorker<Void, String> {
 
       public GameRun() {
          cleanPanel();
          toggleButton();
       }
 
       @Override
        public Void doInBackground() {
           try {
              ArrayList<RealTeam> teams = Repo.getTeams();
             Game prova = new Game(teams);
              if (prova.start(test)) {
                 while (!isCancelled() && prova.isActive()) {
                    String update = prova.resume();
                    publish(update);
                 }
                 if (isCancelled()) {
                    publish(prova.finish());
                 }
              }
           } catch (Exception e) {
              publish(e.toString());
           } catch (UnsatisfiedLinkError e) {
              publish("Driver do EasyLab nao encontrado!");
           }
           publish("The End.\n");
           return null;
        }
 
        @Override
        public void done() {
           toggleButton();
        }
 
        private void toggleButton() {
           String current = jButton.getText();
          if (current.equals("Iniciar")) {
             jButton.setText("Finalizar");
          } else {
             jButton.setText("Iniciar");
          }
        }
 
        private void cleanPanel() {
           jTextPane.setText("");
        }
 
        @Override
        protected void process(final List<String> updates) {
            for (String update : updates) {
               jTextPane.setText(jTextPane.getText() + update);
            }
        }
    }
 
 }  //  @jve:decl-index=0:visual-constraint="10,10"
