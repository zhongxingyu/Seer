 import javax.swing.*;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 
 public class MetropolisGUI extends JFrame {
 	private static final long serialVersionUID = 1L;
 	
 	private static final int TEXT_SIZE = 9;
 	private JTextField metropolisBox;
 	private JTextField continentBox;
 	private JTextField populationBox;
 	
 	private MetropolisTableModel model;
 	
 	private JButton addButton;
 	private JButton searchButton;
 	private JComboBox<String> populationDrop;
 	private JComboBox<String> matchDrop;
 		
 	
 	public MetropolisGUI() {
 		super("Metropolis Viewer");
 		this.setMinimumSize(new Dimension(650, 510));
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JPanel topRow = getTopRow();
 		
 		JPanel bottomRow = new JPanel();
 		model = new MetropolisTableModel();
 		JTable table = new JTable(model);
 		bottomRow.add(new JScrollPane(table), BorderLayout.LINE_START);
 		JPanel rightSide = getRightSide();
 		bottomRow.add(rightSide, BorderLayout.LINE_END);
 
 		this.add(topRow, BorderLayout.PAGE_START);
 		this.add(bottomRow, BorderLayout.PAGE_END);
 		
 		pack();
 		setVisible(true);
 		
 		addListeners();
 	}
 	
 	private JPanel getTopRow() {
 		JPanel topRow = new JPanel();
 		JLabel metropolisLabel = new JLabel("Metropolis: ");
 		metropolisBox = new JTextField(TEXT_SIZE);
 		JLabel continentLabel = new JLabel("Continent: ");
 		continentBox = new JTextField(TEXT_SIZE);
 		JLabel populationLabel = new JLabel("Population: ");
 		populationBox = new JTextField(TEXT_SIZE);
 		topRow.add(metropolisLabel);
 		topRow.add(metropolisBox);
 		topRow.add(continentLabel);
 		topRow.add(continentBox);
 		topRow.add(populationLabel);
 		topRow.add(populationBox);
 		return topRow;
 	}
 	
 	private final String[] POPULATION_OPTIONS = new String[]{
 			"Equal", "Population Larger Than:", "Population Smaller Than:" };
 	private final String[] MATCH_OPTIONS = new String[]{
 			"Exact Match", "Partial Match" };
 	
 	private JPanel getRightSide() {
 		JPanel rightSide = new JPanel();
 		rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
 
 		addButton = new JButton("Add");
 		searchButton = new JButton("Search");
 		populationDrop = new JComboBox<String>(POPULATION_OPTIONS);
 		matchDrop = new JComboBox<String>(MATCH_OPTIONS);
 		
 		addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
 		addButton.setAlignmentY(Component.TOP_ALIGNMENT);
 		searchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
 		populationDrop.setAlignmentX(Component.LEFT_ALIGNMENT);
 		matchDrop.setAlignmentX(Component.LEFT_ALIGNMENT);
 		
 		rightSide.add(addButton);
 		rightSide.add(Box.createVerticalStrut(5));
 		rightSide.add(searchButton);
 		rightSide.add(Box.createVerticalStrut(25));
 		rightSide.add(populationDrop);
 		rightSide.add(Box.createVerticalStrut(5));
 		rightSide.add(matchDrop);
 		return rightSide;
 	}
 	
 	private MetropolisTableModel.PopulationSearchOptions getPopulationDropValue() {
 		String s = (String) populationDrop.getSelectedItem();
 		if(s.equals(POPULATION_OPTIONS[MetropolisTableModel.PopulationSearchOptions.EQUAL.index()])) { 
 			return MetropolisTableModel.PopulationSearchOptions.EQUAL;
 		} else if(s.equals(POPULATION_OPTIONS[MetropolisTableModel.PopulationSearchOptions.LARGER.index()])) {
 			return MetropolisTableModel.PopulationSearchOptions.LARGER;
 		} else return MetropolisTableModel.PopulationSearchOptions.SMALLER;
 	}
 	
 	private MetropolisTableModel.MatchSearchOptions getMatchDropValue() {
 		String s = (String) matchDrop.getSelectedItem();
 		if(s.equals(MATCH_OPTIONS[MetropolisTableModel.MatchSearchOptions.EXACT.index()])) {
 			return MetropolisTableModel.MatchSearchOptions.EXACT;
 		} else return MetropolisTableModel.MatchSearchOptions.PARTIAL;
 	}
 	
 	
 	
 	public static void main(String args[]) {
 		@SuppressWarnings("unused")
 		MetropolisGUI gui = new MetropolisGUI();
 	}
 	
 	
 	private void addListeners() {
 		addButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				model.add(metropolisBox.getText(),
 					   	  continentBox.getText(),
 					   	  populationBox.getText());
 			}
 		});
 		searchButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				model.search(metropolisBox.getText(),
 							 continentBox.getText(),
 							 populationBox.getText(),
 							 getPopulationDropValue(),
 							 getMatchDropValue());
 			}
 		});
 	}
 	
 }
 	
 	
 	
 	
