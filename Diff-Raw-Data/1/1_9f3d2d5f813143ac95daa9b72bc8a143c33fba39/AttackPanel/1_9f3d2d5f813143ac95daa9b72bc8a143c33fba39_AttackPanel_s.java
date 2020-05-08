 package risk.view.client;
 
 import javax.swing.JPanel;
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import java.awt.BorderLayout;
 import javax.swing.SwingConstants;
 import java.awt.GridLayout;
 import javax.swing.JButton;
 
 import risk.game.Attack;
 import risk.game.Controller;
 import risk.game.CountryPair;
 
 import java.awt.FlowLayout;
 import java.awt.Component;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.RowSpec;
 import com.jgoodies.forms.factories.FormFactory;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.Font;
 import javax.swing.AbstractAction;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.swing.Action;
 
 public class AttackPanel extends JPanel {
     private int fromCurrentArmies, toCurrentArmies; 
     private AttackDialog parent;
     private JLabel thrownAttacker, thrownDefender, lblFromAfterArmies, lblToAfterArmies;
     private JButton aThreeDice, aTwoDice, aOneDice, btnCancelAttack, dTwoDice, dOneDice;
     private Controller controller;
     private int viewerType;
     private CountryPair cp;
     private final Action a3 = new SwingAction();
     private final Action a2 = new SwingAction_1();
     private final Action a1 = new SwingAction_2();
     private final Action aCancel = new SwingAction_3();
     private final Action d2 = new SwingAction_4();
     private final Action d1 = new SwingAction_5();
     /**
      * Create the panel.
      * viewerType==0: all buttons disabled
      * viewerType==1: defender buttons disabled (attacker mode)
      * viewerType==2: attacker buttons disabled (defender mode)
      */
     public AttackPanel(AttackDialog ad, Attack a, int viewerType, Controller controller) {
         this.controller=controller;
         this.viewerType=viewerType;
         cp=a.getCountryPair();
         parent=ad;
         setLayout(new BorderLayout(0, 0));
         
         JLabel lblAttack = new JLabel("Attack");
         lblAttack.setFont(new Font("Tahoma", Font.BOLD, 12));
         lblAttack.setHorizontalAlignment(SwingConstants.CENTER);
         add(lblAttack, BorderLayout.NORTH);
         
         JPanel panel = new JPanel();
         add(panel, BorderLayout.CENTER);
         panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
         
         JPanel panel_1 = new JPanel();
         panel.add(panel_1);
         GridBagLayout gbl_panel_1 = new GridBagLayout();
         gbl_panel_1.columnWidths = new int[]{102, 0};
         gbl_panel_1.rowHeights = new int[]{0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
         gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
         gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
         panel_1.setLayout(gbl_panel_1);
         
         JLabel lblAttacker = new JLabel(cp.From.getOwner().getName());
         lblAttacker.setForeground(cp.From.getOwner().getColor());
         GridBagConstraints gbc_lblAttacker = new GridBagConstraints();
         gbc_lblAttacker.insets = new Insets(0, 0, 5, 0);
         gbc_lblAttacker.gridx = 0;
         gbc_lblAttacker.gridy = 0;
         panel_1.add(lblAttacker, gbc_lblAttacker);
         
         JLabel lblFrom = new JLabel(cp.From.getName());
         GridBagConstraints gbc_lblFrom = new GridBagConstraints();
         gbc_lblFrom.insets = new Insets(0, 0, 5, 0);
         gbc_lblFrom.gridx = 0;
         gbc_lblFrom.gridy = 1;
         panel_1.add(lblFrom, gbc_lblFrom);
         
         JPanel panel_2 = new JPanel();
         GridBagConstraints gbc_panel_2 = new GridBagConstraints();
         gbc_panel_2.insets = new Insets(0, 0, 5, 0);
         gbc_panel_2.gridx = 0;
         gbc_panel_2.gridy = 2;
         panel_1.add(panel_2, gbc_panel_2);
         panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
         
         JLabel label_2 = new JLabel("Current armies:");
         panel_2.add(label_2);
         
         JLabel lblFromCurrentArmies = new JLabel(cp.From.getTroops()+"");
         panel_2.add(lblFromCurrentArmies);
         
         JButton aThreeDice = new JButton("Attack with 3 dice");
         aThreeDice.setAction(a3);
         GridBagConstraints gbc_AThreeDice = new GridBagConstraints();
         gbc_AThreeDice.insets = new Insets(0, 0, 5, 0);
         gbc_AThreeDice.gridx = 0;
         gbc_AThreeDice.gridy = 3;
         panel_1.add(aThreeDice, gbc_AThreeDice);
         
         aTwoDice = new JButton("Attack with 2 dice");
         aTwoDice.setAction(a2);
         GridBagConstraints gbc_ATwoDice = new GridBagConstraints();
         gbc_ATwoDice.insets = new Insets(0, 0, 5, 0);
         gbc_ATwoDice.gridx = 0;
         gbc_ATwoDice.gridy = 4;
         panel_1.add(aTwoDice, gbc_ATwoDice);
         
         aOneDice = new JButton("Attack with 1 dice");
         aOneDice.setAction(a1);
         GridBagConstraints gbc_AOneDice = new GridBagConstraints();
         gbc_AOneDice.insets = new Insets(0, 0, 5, 0);
         gbc_AOneDice.gridx = 0;
         gbc_AOneDice.gridy = 5;
         panel_1.add(aOneDice, gbc_AOneDice);
         
         btnCancelAttack = new JButton("Cancel attack");
         btnCancelAttack.setAction(aCancel);
         GridBagConstraints gbc_btnCancelAttack = new GridBagConstraints();
         gbc_btnCancelAttack.insets = new Insets(0, 0, 5, 0);
         gbc_btnCancelAttack.gridx = 0;
         gbc_btnCancelAttack.gridy = 6;
         panel_1.add(btnCancelAttack, gbc_btnCancelAttack);
         
         JPanel panel_6 = new JPanel();
         GridBagConstraints gbc_panel_6 = new GridBagConstraints();
         gbc_panel_6.insets = new Insets(0, 0, 5, 0);
         gbc_panel_6.fill = GridBagConstraints.BOTH;
         gbc_panel_6.gridx = 0;
         gbc_panel_6.gridy = 7;
         panel_1.add(panel_6, gbc_panel_6);
         
         JPanel panel_7 = new JPanel();
         GridBagConstraints gbc_panel_7 = new GridBagConstraints();
         gbc_panel_7.insets = new Insets(2, 0, 5, 0);
         gbc_panel_7.fill = GridBagConstraints.BOTH;
         gbc_panel_7.gridx = 0;
         gbc_panel_7.gridy = 8;
         panel_1.add(panel_7, gbc_panel_7);
         panel_7.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
         
         JLabel label_4 = new JLabel("Thrown:");
         panel_7.add(label_4);
         
         thrownAttacker = new JLabel("");
         panel_7.add(thrownAttacker);
         
         JPanel panel_8 = new JPanel();
         GridBagConstraints gbc_panel_8 = new GridBagConstraints();
         gbc_panel_8.insets = new Insets(0, 0, 5, 0);
         gbc_panel_8.fill = GridBagConstraints.BOTH;
         gbc_panel_8.gridx = 0;
         gbc_panel_8.gridy = 9;
         panel_1.add(panel_8, gbc_panel_8);
         panel_8.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
         
         JLabel label_6 = new JLabel("Armies after throwing:");
         panel_8.add(label_6);
         
         lblFromAfterArmies = new JLabel("?");
         panel_8.add(lblFromAfterArmies);
         
         JPanel panel_3 = new JPanel();
         panel.add(panel_3);
         GridBagLayout gbl_panel_3 = new GridBagLayout();
         gbl_panel_3.columnWidths = new int[]{102, 0};
         gbl_panel_3.rowHeights = new int[]{0, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0};
         gbl_panel_3.columnWeights = new double[]{1.0, Double.MIN_VALUE};
         gbl_panel_3.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
         panel_3.setLayout(gbl_panel_3);
         
         JLabel lblDefender = new JLabel(cp.To.getOwner().getName());
         lblDefender.setForeground(cp.To.getOwner().getColor());
         GridBagConstraints gbc_lblDefender = new GridBagConstraints();
         gbc_lblDefender.insets = new Insets(0, 0, 5, 0);
         gbc_lblDefender.gridx = 0;
         gbc_lblDefender.gridy = 0;
         panel_3.add(lblDefender, gbc_lblDefender);
         
         JLabel lblTo = new JLabel(cp.To.getName());
         GridBagConstraints gbc_lblTo = new GridBagConstraints();
         gbc_lblTo.insets = new Insets(0, 0, 5, 0);
         gbc_lblTo.gridx = 0;
         gbc_lblTo.gridy = 1;
         panel_3.add(lblTo, gbc_lblTo);
         
         JPanel panel_5 = new JPanel();
         GridBagConstraints gbc_panel_5 = new GridBagConstraints();
         gbc_panel_5.insets = new Insets(0, 0, 5, 0);
         gbc_panel_5.gridx = 0;
         gbc_panel_5.gridy = 2;
         panel_3.add(panel_5, gbc_panel_5);
         panel_5.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
         
         JLabel label = new JLabel("Current armies:");
         panel_5.add(label);
         
         JLabel lblToCurrentArmies = new JLabel(cp.To.getTroops()+"");
         panel_5.add(lblToCurrentArmies);
         
         dTwoDice = new JButton("Defend with 2 dice");
         dTwoDice.setAction(d2);
         GridBagConstraints gbc_DTwoDice = new GridBagConstraints();
         gbc_DTwoDice.insets = new Insets(0, 0, 5, 0);
         gbc_DTwoDice.gridx = 0;
         gbc_DTwoDice.gridy = 3;
         panel_3.add(dTwoDice, gbc_DTwoDice);
         
         dOneDice = new JButton("Defend with 1 dice");
         dOneDice.setAction(d1);
         GridBagConstraints gbc_DOneDice = new GridBagConstraints();
         gbc_DOneDice.insets = new Insets(0, 0, 5, 0);
         gbc_DOneDice.gridx = 0;
         gbc_DOneDice.gridy = 4;
         panel_3.add(dOneDice, gbc_DOneDice);
         
         JPanel panel_4 = new JPanel();
         GridBagConstraints gbc_panel_4 = new GridBagConstraints();
         gbc_panel_4.insets = new Insets(0, 0, 5, 0);
         gbc_panel_4.fill = GridBagConstraints.BOTH;
         gbc_panel_4.gridx = 0;
         gbc_panel_4.gridy = 5;
         panel_3.add(panel_4, gbc_panel_4);
         
         JPanel panel_9 = new JPanel();
         GridBagConstraints gbc_panel_9 = new GridBagConstraints();
         gbc_panel_9.anchor = GridBagConstraints.SOUTH;
         gbc_panel_9.insets = new Insets(33, 0, 5, 0);
         gbc_panel_9.gridx = 0;
         gbc_panel_9.gridy = 7;
         panel_3.add(panel_9, gbc_panel_9);
         panel_9.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
         
         JLabel label_8 = new JLabel("Thrown:");
         panel_9.add(label_8);
         
         thrownDefender = new JLabel("");
         panel_9.add(thrownDefender);
         
         JPanel panel_10 = new JPanel();
         GridBagConstraints gbc_panel_10 = new GridBagConstraints();
         gbc_panel_10.anchor = GridBagConstraints.SOUTH;
         gbc_panel_10.insets = new Insets(0, 0, 5, 0);
         gbc_panel_10.fill = GridBagConstraints.HORIZONTAL;
         gbc_panel_10.gridx = 0;
         gbc_panel_10.gridy = 8;
         panel_3.add(panel_10, gbc_panel_10);
         panel_10.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
         
         JLabel label_10 = new JLabel("Armies after throwing:");
         panel_10.add(label_10);
         
         lblToAfterArmies = new JLabel("?");
         panel_10.add(lblToAfterArmies);
        setButtonsStatus();
 
         setButtonsStatus();
     }
     
     public void refresh(Attack attack){
         setButtonsStatus();
         Collection<Integer> temp=attack.getaDiceResults();
         String tempS="";
         for(Integer i: temp){
             tempS+=i+";";
         }
         if(tempS.endsWith(";")) tempS.substring(0, tempS.length()-1);
         thrownAttacker.setText(tempS); 
         temp=attack.getdDiceResults();
         for(Integer i: temp){
             tempS+=i+";";
         }
         if(tempS.endsWith(";")) tempS.substring(0, tempS.length()-1);
         thrownDefender.setText(tempS);
         int deltaA=0, deltaD=0;
         attack.calcLosses(deltaA, deltaD);
         //lblToAfterArmies=
         
     }
     private void disableAllButtons(){
         dOneDice.setEnabled(false);
         dTwoDice.setEnabled(false);
         aOneDice.setEnabled(false);
         aTwoDice.setEnabled(false);
         aThreeDice.setEnabled(false);
         btnCancelAttack.setEnabled(false);
     }
     private void setButtonsStatus(){
         if(viewerType==0){
             dOneDice.setEnabled(false);
             dTwoDice.setEnabled(false);
             aOneDice.setEnabled(false);
             aTwoDice.setEnabled(false);
             aThreeDice.setEnabled(false);
             btnCancelAttack.setEnabled(false);
         }
         if (viewerType==1){
             if(cp.From.getTroops()<4) aThreeDice.setEnabled(false);
             if(cp.From.getTroops()<3) aTwoDice.setEnabled(false);
             if(cp.From.getTroops()<2) aOneDice.setEnabled(false);
             dOneDice.setEnabled(false);
             dTwoDice.setEnabled(false);
         }
         if(viewerType==2){
             if(cp.From.getTroops()<2) dTwoDice.setEnabled(false);
             if(cp.From.getTroops()<1) dOneDice.setEnabled(false);
             aOneDice.setEnabled(false);
             aTwoDice.setEnabled(false);
             aThreeDice.setEnabled(false);
             btnCancelAttack.setEnabled(false);
         }
         
     }
     private class SwingAction extends AbstractAction {
         public SwingAction() {
             putValue(NAME, "Attack with 3 dice");
             putValue(SHORT_DESCRIPTION, "Attack with 3 dice");
         }
         public void actionPerformed(ActionEvent e) {
             disableAllButtons();
             controller.onAttack_AttackerChose(3);
         }
     }
     private class SwingAction_1 extends AbstractAction {
         public SwingAction_1() {
             putValue(NAME, "Attack with 2 dice");
             putValue(SHORT_DESCRIPTION, "Attack with 2 dice");
         }
         public void actionPerformed(ActionEvent e) {
             disableAllButtons();
             controller.onAttack_AttackerChose(2);
         }
     }
     private class SwingAction_2 extends AbstractAction {
         public SwingAction_2() {
             putValue(NAME, "Attack with 1 dice");
             putValue(SHORT_DESCRIPTION, "Attack with 1 dice");
         }
         public void actionPerformed(ActionEvent e) {
             disableAllButtons();
             controller.onAttack_AttackerChose(1);
         }
     }
     private class SwingAction_3 extends AbstractAction {
         public SwingAction_3() {
             putValue(NAME, "Cancel attack");
             putValue(SHORT_DESCRIPTION, "Cancel attack");
         }
         public void actionPerformed(ActionEvent e) {
             disableAllButtons();
             controller.onAttackRetreat();
         }
     }
     private class SwingAction_4 extends AbstractAction {
         public SwingAction_4() {
             putValue(NAME, "Defend with 2 dice");
             putValue(SHORT_DESCRIPTION, "Defend with 1 dice");
         }
         public void actionPerformed(ActionEvent e) {
             disableAllButtons();
             controller.onAttack_DefenderChose(2);
         }
     }
     private class SwingAction_5 extends AbstractAction {
         public SwingAction_5() {
             putValue(NAME, "Defend with 1 dice");
             putValue(SHORT_DESCRIPTION, "Defend with 1 dice");
         }
         public void actionPerformed(ActionEvent e) {
             disableAllButtons();
             controller.onAttack_DefenderChose(1);
         }
     }
 }
