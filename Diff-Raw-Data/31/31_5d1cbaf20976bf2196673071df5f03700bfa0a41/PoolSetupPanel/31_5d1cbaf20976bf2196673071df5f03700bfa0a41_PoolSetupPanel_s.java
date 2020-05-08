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
 import java.util.Collection;
 
 import final_project.control.*;
 import final_project.model.FencerPool;
 import final_project.model.Pool;
 import final_project.model.store.IDataStore;
 
 public class PoolSetupPanel extends JPanel implements Constants {
 
 	/**
 	 * Create the panel.
 	 */
 	public PoolSetupPanel(TournamentController t, IDataStore store) {
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 
 		JScrollPane scrollPane = new JScrollPane();
 		add(scrollPane);
 
 		JPanel panel_1 = new JPanel();
 		scrollPane.setViewportView(panel_1);
 		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
 
		//TODO Replace with for-loop of generated PoolRefLists from pool information
 		PoolRefList lastPool = new PoolRefList(t, new FencerPool(), store);
		if(t.getPools(EVENT_ID) != null) {
 			for(Pool p: t.getPools(EVENT_ID)) {
				panel_1.add(lastPool);
 				lastPool = new PoolRefList(t, p, store);
 			}
 		}
 
 		GridBagLayout gridBagLayout = (GridBagLayout) lastPool.getLayout();
 		gridBagLayout.rowWeights = new double[]{0.0, 1.0};
 		gridBagLayout.rowHeights = new int[]{0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 1.0};
 		gridBagLayout.columnWidths = new int[]{111, 0, 0, 0, 0};
 
 		JPanel panel = new JPanel();
 		panel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
 		add(panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{346, 141, 143, 0};
 		gbl_panel.rowHeights = new int[]{27, 0};
 		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 
 		JButton btnReassignReferees = new JButton("Re-assign Referees");
 		GridBagConstraints gbc_btnReassignReferees = new GridBagConstraints();
 		gbc_btnReassignReferees.anchor = GridBagConstraints.NORTHWEST;
 		gbc_btnReassignReferees.insets = new Insets(0, 0, 0, 5);
 		gbc_btnReassignReferees.gridx = 1;
 		gbc_btnReassignReferees.gridy = 0;
 		panel.add(btnReassignReferees, gbc_btnReassignReferees);
 
 		JButton btnAcceptSeeding = new JButton("Accept Assignments");
 		GridBagConstraints gbc_btnAcceptSeeding = new GridBagConstraints();
 		gbc_btnAcceptSeeding.anchor = GridBagConstraints.NORTHWEST;
 		gbc_btnAcceptSeeding.gridx = 2;
 		gbc_btnAcceptSeeding.gridy = 0;
 		panel.add(btnAcceptSeeding, gbc_btnAcceptSeeding);
 
 	}
 }
