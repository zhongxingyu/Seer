 package final_project.view;
 
 import javax.swing.JPanel;
 import java.awt.FlowLayout;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.JScrollPane;
 import java.awt.CardLayout;
 import javax.swing.JLabel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.JList;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import javax.swing.AbstractListModel;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import java.awt.Component;
 import java.util.*;
 import java.awt.event.*;
 
 import final_project.control.*;
 import final_project.model.FencerPool;
 import final_project.model.Pool;
 
 public class PoolSetupPanel extends JPanel implements Constants {
     TournamentController tournament;
     private JButton btnAcceptSeeding;
     private JButton btnReassignReferees;
     private JPanel panel_1;
     
 	/**
 	 * Create the panel.
 	 */
 	public PoolSetupPanel(TournamentController t) {
         tournament = t;
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
 		JScrollPane scrollPane = new JScrollPane();
 		add(scrollPane);
 
         panel_1 = new JPanel();
 		scrollPane.setViewportView(panel_1);
 		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
 
         resetRefs();
         
 		JPanel panel = new JPanel();
 		panel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
 		add(panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{346, 141, 143, 0};
 		gbl_panel.rowHeights = new int[]{27, 0};
 		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 
 		btnReassignReferees = new JButton("Re-assign Referees");
 		GridBagConstraints gbc_btnReassignReferees = new GridBagConstraints();
 		gbc_btnReassignReferees.anchor = GridBagConstraints.NORTHWEST;
 		gbc_btnReassignReferees.insets = new Insets(0, 0, 0, 5);
 		gbc_btnReassignReferees.gridx = 1;
 		gbc_btnReassignReferees.gridy = 0;
 		panel.add(btnReassignReferees, gbc_btnReassignReferees);
 
         btnReassignReferees.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     Collection<Integer> changes = new LinkedList<Integer>();
                    Component[] components = getComponents();
                     for (int i = 0; i < components.length; i++) {
                         PoolRefList c = (PoolRefList) components[i];
                         if (c.needsNewRef())
                             changes.addAll(c.getRefs());
                     }
                     tournament.changeRefs(0,changes);
                     resetRefs();
                 }
             });
 
 		btnAcceptSeeding = new JButton("Accept Assignments");
 		GridBagConstraints gbc_btnAcceptSeeding = new GridBagConstraints();
 		gbc_btnAcceptSeeding.anchor = GridBagConstraints.NORTHWEST;
 		gbc_btnAcceptSeeding.gridx = 2;
 		gbc_btnAcceptSeeding.gridy = 0;
 		panel.add(btnAcceptSeeding, gbc_btnAcceptSeeding);
 
         btnAcceptSeeding.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                 	tournament.notifyPools(EVENT_ID);
                     tournament.getMainWindow().loadRightPanel(new PoolRoundObserverPanel(tournament));
                 }
             });
 
 	}
 	public JButton getAcceptSeeding() {
 		return btnAcceptSeeding;
 	}
     
 	public JButton getReassignReferees() {
 		return btnReassignReferees;
 	}
     
     public void resetRefs() {
         panel_1.removeAll();
 		// Generating PoolRefLists from pool information
 		PoolRefList lastPool = new PoolRefList(tournament, new FencerPool());
         for(Pool p: tournament.getPools(EVENT_ID)) {
             lastPool = new PoolRefList(tournament, p);
             panel_1.add(lastPool);
         }
         
 		GridBagLayout gridBagLayout = (GridBagLayout) lastPool.getLayout();
 		gridBagLayout.rowWeights = new double[]{0.0, 1.0};
 		gridBagLayout.rowHeights = new int[]{0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 1.0};
 		gridBagLayout.columnWidths = new int[]{111, 0, 0, 0, 0};
 
         panel_1.validate();
     }
 }
