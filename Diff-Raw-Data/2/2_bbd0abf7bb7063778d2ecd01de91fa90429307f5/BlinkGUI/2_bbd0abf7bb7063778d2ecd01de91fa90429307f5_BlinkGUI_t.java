 /**
  * TODO: Rewrite code in a more modular way.
  * 
  */
 
 
 import javax.swing.SwingUtilities;
 import java.awt.BorderLayout;
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 import java.awt.Dimension;
 import javax.swing.JInternalFrame;
 import javax.swing.JSplitPane;
 import java.awt.GridBagLayout;
 import javax.swing.JButton;
 import java.awt.GridBagConstraints;
 import javax.swing.BorderFactory;
 import javax.swing.border.BevelBorder;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingConstants;
 import javax.swing.JTextPane;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 public class BlinkGUI extends JFrame {
 
     // Saves a reference to the connector
     public BlinkConnector connector;
 	
     private static final long serialVersionUID = 1L;
     private JPanel jPanel = null;
     private JPanel jPanel1 = null;
     private JCheckBox moteChoice1 = null;
     private JLabel jLabel = null;
     private JCheckBox moteChoice2 = null;
     private JCheckBox moteChoice3 = null;
     private JCheckBox moteChoice4 = null;
     private JCheckBox moteChoice5 = null;
     private JLabel jLabel1 = null;
     private JPanel jPanel2 = null;
     private JButton led0Button1 = null;
     private JButton led0Button2 = null;
     private JButton led1Button1 = null;
     private JButton led1Button2 = null;
     private JButton led2Button1 = null;
     private JButton led2Button2 = null;
     private JPanel jPanel3 = null;
     private JPanel jPanel4 = null;
     private JLabel jLabel2 = null;
     private JCheckBox led0Choice = null;
     private JCheckBox led1Choice = null;
     private JCheckBox led2Choice = null;
     private JButton setCustomButton = null;
     private JPanel jPanel5 = null;
     private JPanel jPanel6 = null;
     private JTextArea DebugArea = null;
     private JLabel jLabel3 = null;
     /**
      * This method initializes jPanel	
      * 	
      * @return javax.swing.JPanel	
      */
     private JPanel getJPanel() {
         if (jPanel == null) {
             jLabel1 = new JLabel();
             jLabel1.setText("Choose an action to execute:");
             jLabel1.setPreferredSize(new Dimension(179, 25));
             jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
             jPanel = new JPanel();
             jPanel.setLayout(new BorderLayout());
             jPanel.add(getJPanel1(), BorderLayout.NORTH);
             jPanel.add(getJPanel2(), BorderLayout.CENTER);
             jPanel.add(getJPanel6(), BorderLayout.SOUTH);
         }
         return jPanel;
     }
 
     /**
      * This method initializes jPanel1	
      * 	
      * @return javax.swing.JPanel	
      */
     private JPanel getJPanel1() {
         if (jPanel1 == null) {
             jLabel = new JLabel();
             jLabel.setText("Choose Motes:");
             jLabel.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
             jLabel.setPreferredSize(new Dimension(92, 25));
             jLabel.setHorizontalAlignment(SwingConstants.CENTER);
             jPanel1 = new JPanel();
             jPanel1.setLayout(new BorderLayout());
             jPanel1.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
             jPanel1.setName("");
             jPanel1.add(jLabel, BorderLayout.NORTH);
             jPanel1.add(getJPanel5(), BorderLayout.CENTER);
         }
         return jPanel1;
     }
 
     /**
      * This method initializes moteChoice1	
      * 	
      * @return javax.swing.JCheckBox	
      */
     private JCheckBox getMoteChoice1() {
         if (moteChoice1 == null) {
             moteChoice1 = new JCheckBox();
             moteChoice1.setText("Mote0");
         }
         return moteChoice1;
     }
 
     /**
      * This method initializes moteChoice2	
      * 	
      * @return javax.swing.JCheckBox	
      */
     private JCheckBox getMoteChoice2() {
         if (moteChoice2 == null) {
             moteChoice2 = new JCheckBox();
             moteChoice2.setText("Mote1");
         }
         return moteChoice2;
     }
 
     /**
      * This method initializes moteChoice3	
      * 	
      * @return javax.swing.JCheckBox	
      */
     private JCheckBox getMoteChoice3() {
         if (moteChoice3 == null) {
             moteChoice3 = new JCheckBox();
             moteChoice3.setText("Mote2");
         }
         return moteChoice3;
     }
 
     /**
      * This method initializes moteChoice4	
      * 	
      * @return javax.swing.JCheckBox	
      */
     private JCheckBox getMoteChoice4() {
         if (moteChoice4 == null) {
             moteChoice4 = new JCheckBox();
             moteChoice4.setText("Mote3");
         }
         return moteChoice4;
     }
 
     /**
      * This method initializes moteChoice5	
      * 	
      * @return javax.swing.JCheckBox	
      */
     private JCheckBox getMoteChoice5() {
         if (moteChoice5 == null) {
             moteChoice5 = new JCheckBox();
             moteChoice5.setText("Mote4");
         }
         return moteChoice5;
     }
 
     /**
      * This method initializes jPanel2	
      * 	
      * @return javax.swing.JPanel	
      */
     private JPanel getJPanel2() {
         if (jPanel2 == null) {
             jPanel2 = new JPanel();
             jPanel2.setLayout(new BorderLayout());
             jPanel2.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
             jPanel2.add(jLabel1, BorderLayout.NORTH);
             jPanel2.add(getJPanel3(), BorderLayout.WEST);
             jPanel2.add(getJPanel4(), BorderLayout.EAST);
         }
         return jPanel2;
     }
 
     /**
      * This method initializes led0Button1	
      * 	
      * @return javax.swing.JButton	
      */
     private JButton getLed0Button1() {
         if (led0Button1 == null) {
             led0Button1 = new JButton();
             led0Button1.setText("LED 0 On");
 	    led0Button1.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent e) {
 			short mask = 0x09;
 			sendSelected(mask);
 		    }
 		});
         }
         return led0Button1;
     }
 
     /**
      * This method initializes led0Button2	
      * 	
      * @return javax.swing.JButton	
      */
     private JButton getLed0Button2() {
         if (led0Button2 == null) {
             led0Button2 = new JButton();
             led0Button2.setText("LED 0 Off");
 	    led0Button2.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent e) {
 			short mask = 0x08;
 			sendSelected(mask);
 		    }
 		});
         }
         return led0Button2;
     }
 
     /**
      * This method initializes led1Button1	
      * 	
      * @return javax.swing.JButton	
      */
     private JButton getLed1Button1() {
         if (led1Button1 == null) {
             led1Button1 = new JButton();
             led1Button1.setText("LED 1 On");
 	    led1Button1.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent e) {
 			short mask = 0x12;
 			sendSelected(mask);
 		    }
 		});
         }
         return led1Button1;
     }
 
     /**
      * This method initializes led1Button2	
      * 	
      * @return javax.swing.JButton	
      */
     private JButton getLed1Button2() {
         if (led1Button2 == null) {
             led1Button2 = new JButton();
             led1Button2.setText("LED 1 Off");
 	    led1Button2.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent e) {
 			short mask = 0x10;
 			sendSelected(mask);
 		    }
 		});
         }
         return led1Button2;
     }
 
     /**
      * This method initializes led2Button1	
      * 	
      * @return javax.swing.JButton	
      */
     private JButton getLed2Button1() {
         if (led2Button1 == null) {
             led2Button1 = new JButton();
             led2Button1.setText("LED 2 On");
 	    led2Button1.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent e) {
 			short mask = 0x24;
 			sendSelected(mask);
 		    }
 		});
         }
         return led2Button1;
     }
 
     /**
      * This method initializes led2Button2	
      * 	
      * @return javax.swing.JButton	
      */
     private JButton getLed2Button2() {
         if (led2Button2 == null) {
             led2Button2 = new JButton();
             led2Button2.setText("LED 2 Off");
 	    led2Button2.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent e) {
			short mask = 0x20;
 			sendSelected(mask);
 		    }
 		});
         }
         return led2Button2;
     }
 
     /**
      * This method initializes jPanel3	
      * 	
      * @return javax.swing.JPanel	
      */
     private JPanel getJPanel3() {
         if (jPanel3 == null) {
             GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
             gridBagConstraints11.gridx = 1;
             gridBagConstraints11.gridy = 0;
             GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
             gridBagConstraints10.gridx = 2;
             gridBagConstraints10.gridy = 1;
             GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
             gridBagConstraints9.gridx = 2;
             gridBagConstraints9.gridy = 2;
             GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
             gridBagConstraints8.gridx = 1;
             gridBagConstraints8.gridy = 2;
             GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
             gridBagConstraints7.gridx = 1;
             gridBagConstraints7.gridy = 1;
             GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
             gridBagConstraints6.gridx = 2;
             gridBagConstraints6.gridy = 0;
             jPanel3 = new JPanel();
             jPanel3.setLayout(new GridBagLayout());
             jPanel3.setName("LEDButtonPanel");
             jPanel3.add(getLed0Button2(), gridBagConstraints6);
             jPanel3.add(getLed0Button1(), gridBagConstraints11);
             jPanel3.add(getLed2Button1(), gridBagConstraints8);
             jPanel3.add(getLed1Button1(), gridBagConstraints7);
             jPanel3.add(getLed2Button2(), gridBagConstraints9);
             jPanel3.add(getLed1Button2(), gridBagConstraints10);
         }
         return jPanel3;
     }
 
     /**
      * This method initializes jPanel4	
      * 	
      * @return javax.swing.JPanel	
      */
     private JPanel getJPanel4() {
         if (jPanel4 == null) {
             jLabel2 = new JLabel();
             jLabel2.setText("Set custom LEDs:");
             jPanel4 = new JPanel();
             jPanel4.setLayout(new BorderLayout());
             jPanel4.add(jLabel2, BorderLayout.NORTH);
             jPanel4.add(getLed0Choice(), BorderLayout.WEST);
             jPanel4.add(getLed1Choice(), BorderLayout.CENTER);
             jPanel4.add(getLed2Choice(), BorderLayout.EAST);
             jPanel4.add(getSetCustomButton(), BorderLayout.SOUTH);
         }
         return jPanel4;
     }
 
     /**
      * This method initializes led0Choice	
      * 	
      * @return javax.swing.JCheckBox	
      */
     private JCheckBox getLed0Choice() {
         if (led0Choice == null) {
             led0Choice = new JCheckBox();
             led0Choice.setText("LED0");
             led0Choice.setActionCommand("LED0");
         }
         return led0Choice;
     }
 
     /**
      * This method initializes led1Choice	
      * 	
      * @return javax.swing.JCheckBox	
      */
     private JCheckBox getLed1Choice() {
         if (led1Choice == null) {
             led1Choice = new JCheckBox();
             led1Choice.setText("LED1");
         }
         return led1Choice;
     }
 
     /**
      * This method initializes led2Choice	
      * 	
      * @return javax.swing.JCheckBox	
      */
     private JCheckBox getLed2Choice() {
         if (led2Choice == null) {
             led2Choice = new JCheckBox();
             led2Choice.setText("LED2");
             led2Choice.setMnemonic(KeyEvent.VK_UNDEFINED);
         }
         return led2Choice;
     }
 
     /**
      * This method initializes setCustomButton	
      * 	
      * @return javax.swing.JButton	
      */
     private JButton getSetCustomButton() {
         if (setCustomButton == null) {
             setCustomButton = new JButton();
             setCustomButton.setText("Set");
             setCustomButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					short mask = 0x38;
 					
 					if(getLed0Choice().isSelected()){
 						mask += 0x01;
 					}
 					if(getLed1Choice().isSelected()){
 						mask += 0x02;
 					}
 					if(getLed2Choice().isSelected()){
 						mask += 0x04;
 					}
 					
 											
 				}
 			});
         }
         return setCustomButton;
     }
 
     /**
      * This method initializes jPanel5	
      * 	
      * @return javax.swing.JPanel	
      */
     private JPanel getJPanel5() {
         if (jPanel5 == null) {
             GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
             gridBagConstraints4.gridx = 4;
             gridBagConstraints4.gridy = 0;
             GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
             gridBagConstraints3.gridx = 3;
             gridBagConstraints3.gridy = 0;
             GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
             gridBagConstraints2.gridx = 2;
             gridBagConstraints2.gridy = 0;
             GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
             gridBagConstraints1.gridx = 1;
             gridBagConstraints1.gridy = 0;
             GridBagConstraints gridBagConstraints = new GridBagConstraints();
             gridBagConstraints.gridx = 0;
             gridBagConstraints.gridy = 0;
             jPanel5 = new JPanel();
             jPanel5.setLayout(new GridBagLayout());
             jPanel5.add(getMoteChoice1(), gridBagConstraints);
             jPanel5.add(getMoteChoice2(), gridBagConstraints1);
             jPanel5.add(getMoteChoice3(), gridBagConstraints2);
             jPanel5.add(getMoteChoice4(), gridBagConstraints3);
             jPanel5.add(getMoteChoice5(), gridBagConstraints4);
         }
         return jPanel5;
     }
 
     /**
      * This method initializes jPanel6	
      * 	
      * @return javax.swing.JPanel	
      */
     private JPanel getJPanel6() {
         if (jPanel6 == null) {
             jLabel3 = new JLabel();
             jLabel3.setText("Debug output:");
             jLabel3.setHorizontalAlignment(SwingConstants.CENTER);
             jLabel3.setPreferredSize(new Dimension(90, 25));
             jPanel6 = new JPanel();
             jPanel6.setLayout(new BorderLayout());
             jPanel6.setPreferredSize(new Dimension(30, 200));
             jPanel6.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
             jPanel6.add(jLabel3, BorderLayout.NORTH);
             jPanel6.add(getDebugArea(), BorderLayout.CENTER);
         }
         return jPanel6;
     }
 
     /**
      * This method initializes DebugArea	
      * 	
      * @return javax.swing.JTextPane	
      */
     private JTextArea getDebugArea() {
         if (DebugArea == null) {
             DebugArea = new JTextArea();
             DebugArea.setText("");
             DebugArea.setColumns(0);
             DebugArea.setEditable(false);
             DebugArea.setPreferredSize(new Dimension(30, 30));
         }
         return DebugArea;
     }
 
     /**
      * @param args
      */
     /*public static void main(String[] args) {
     // TODO Auto-generated method stub
     SwingUtilities.invokeLater(new Runnable() {
     public void run() {
     BlinkGUI thisClass = new BlinkGUI();
     thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     thisClass.setVisible(true);
     }
     });
     }*/
 
     /**
      * This is the default constructor
      */
     public BlinkGUI(BlinkConnector connector) {
         super();
         this.connector = connector;
         initialize();
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setVisible(true);
     }
 
     /**
      * This method initializes this
      * 
      * @return void
      */
     private void initialize() {
         this.setSize(458, 389);
         this.setContentPane(getJPanel());
         this.setTitle("Mote Control Interface");
         // Minimizes the size of the window
         // this.pack();
         // Prevent resizing
         this.setResizable(false);
     }
     
     public void sendSelected(short mask){
 	JCheckBox[] cbAr = {getMoteChoice1(), getMoteChoice2(), getMoteChoice3(), getMoteChoice4(), getMoteChoice5()};
 					
 	for (int i = 0; i < cbAr.length; i++) {
 	    if(cbAr[i].isSelected()){
 		connector.sendLedMask((short)i, mask);
 	    }
 	}
     }
 
     /**
      * Prints messages to the debug window.
      * @param message
      */
     public void print(String message){
     	this.DebugArea.append(message + "\n");
     	this.DebugArea.setText(DebugArea.getText());
     }
 
 }  //  @jve:decl-index=0:visual-constraint="10,10"
