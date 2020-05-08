 package edu.gatech.cs7450.xnat.gui;
 
 import java.awt.Color;
 import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.LineBorder;
 
 import edu.gatech.cs7450.xnat.SearchWhere.SearchMethod;
 
 /**
  * A panel for manipulating an xNAT search group.
  */
 public class SearchWherePanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	
 	private JButton btnAddCriteria;
 	private JButton btnAddGroup;
 	private JComboBox searchMethod;
 	private JButton btnDelete;
 	
 	public static void main(String[] args) {
 		SearchWherePanel view = new SearchWherePanel();
 		JFrame frame = new JFrame("blah");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().add(view);
 		frame.pack();
 		frame.setVisible(true);
 	}
 	
 	/**
 	 * Create the panel.
 	 */
 	public SearchWherePanel() {
 		setBorder(new LineBorder(new Color(0, 0, 0)));
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{129, 71, 0, 0, 0, 0};
 		gridBagLayout.rowHeights = new int[]{37, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		JLabel lblNewLabel = new JLabel("Search Method:");
 		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
 		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
 		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
 		gbc_lblNewLabel.gridx = 0;
 		gbc_lblNewLabel.gridy = 0;
 		add(lblNewLabel, gbc_lblNewLabel);
 		
 		searchMethod = new JComboBox();
 		GridBagConstraints gbc_searchMethod = new GridBagConstraints();
 		gbc_searchMethod.anchor = GridBagConstraints.WEST;
 		gbc_searchMethod.insets = new Insets(0, 0, 0, 5);
 		gbc_searchMethod.gridx = 1;
 		gbc_searchMethod.gridy = 0;
 		add(searchMethod, gbc_searchMethod);
 		searchMethod.setModel(new DefaultComboBoxModel(SearchMethod.values()));
 		
 		btnAddCriteria = new JButton("Add Criteria");
 		GridBagConstraints gbc_btnAddCriteria = new GridBagConstraints();
 		gbc_btnAddCriteria.insets = new Insets(0, 0, 0, 5);
 		gbc_btnAddCriteria.gridx = 2;
 		gbc_btnAddCriteria.gridy = 0;
 		add(btnAddCriteria, gbc_btnAddCriteria);
 		
 		btnAddGroup = new JButton("Add Group");
 		GridBagConstraints gbc_btnAddGroup = new GridBagConstraints();
 		gbc_btnAddGroup.insets = new Insets(0, 0, 0, 5);
 		gbc_btnAddGroup.gridx = 3;
 		gbc_btnAddGroup.gridy = 0;
 		add(btnAddGroup, gbc_btnAddGroup);
 		
 		btnDelete = new JButton("x");
 		btnDelete.setEnabled(false);
 		btnDelete.setVisible(false);
 		btnDelete.setPreferredSize(new Dimension(25, 25));
 		btnDelete.setMinimumSize(new Dimension(25, 25));
 		btnDelete.setSize(new Dimension(25, 25));
 		btnDelete.setMargin(new Insets(0, 0, 0, 0));
 		btnDelete.setAlignmentY(Component.TOP_ALIGNMENT);
 		btnDelete.setFont(new Font("Dialog", Font.BOLD, 18));
 		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
 		gbc_btnDelete.anchor = GridBagConstraints.EAST;
 		gbc_btnDelete.gridx = 4;
 		gbc_btnDelete.gridy = 0;
 		add(btnDelete, gbc_btnDelete);
 		
 		addButtonListeners();
 	}
 	
 	public JButton getDeleteButton() { return btnDelete; }
 	
 	private void addButtonListeners() {
 		btnAddCriteria.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addSingleCriteria();
 			}
 		});
 		btnAddGroup.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addSearchWhere();
 			}
 		});
 	}
 	
 
 	protected void addSingleCriteria() {
 		final SingleCriteriaPanel pnlCriteria = new SingleCriteriaPanel();
 		addDeletableComponent(pnlCriteria, pnlCriteria.getDeleteButton());
 	}
 	
 	protected void addSearchWhere() {
 		final SearchWherePanel pnlSearch = new SearchWherePanel();
 		addDeletableComponent(pnlSearch, pnlSearch.getDeleteButton());
 	}
 	
	protected void addDeletableComponent(final JComponent component, final JButton delButton) {
		// reduce weight of previous last row
		GridBagLayout layout = (GridBagLayout)getLayout();
		Component[] components = getComponents();
		Component lastComponent = components[components.length - 1];
		GridBagConstraints lastConstraints = layout.getConstraints(lastComponent);
		lastConstraints.weighty = 0.0;
		layout.setConstraints(lastComponent, lastConstraints);
		
 		GridBagConstraints constraints = new GridBagConstraints();
 		constraints.anchor = GridBagConstraints.NORTHWEST;
 		constraints.gridwidth = 5;
 		constraints.gridx = 0;
 		constraints.gridy = GridBagConstraints.RELATIVE;
 		constraints.fill = GridBagConstraints.HORIZONTAL;
 		constraints.weighty = 1.0;
		component.setAlignmentY(Component.TOP_ALIGNMENT);
 		add(component, constraints);
 
 		delButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				remove(component);
 				delButton.removeActionListener(this);
 				revalidate();
 				repaint();
 			}
 		});
 		delButton.setEnabled(true);
 		delButton.setVisible(true);
 		
 		revalidate();
 		repaint();
 	}
 
 }
