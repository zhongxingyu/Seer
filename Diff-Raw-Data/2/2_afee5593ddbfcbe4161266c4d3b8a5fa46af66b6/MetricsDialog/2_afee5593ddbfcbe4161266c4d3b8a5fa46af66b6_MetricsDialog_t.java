 package gr.uoi.cs.daintiness.hecate.gui.swing;
 
 import gr.uoi.cs.daintiness.hecate.diff.Metrics;
 
 import java.awt.Toolkit;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 
 /**
  * A dialog with miscellaneous metics
  * @author giskou
  *
  */
 @SuppressWarnings("serial")
 public class MetricsDialog extends JDialog {
 
 	/**
 	 * Paramerized Constructor
 	 * @param d a Delta object that has run {@link minus} at least once
 	 */
 	public MetricsDialog(Metrics d) {
 		setTitle("Metrics");
 		setIconImage(Toolkit.getDefaultToolkit().getImage(MetricsDialog.class.getResource("/gr/uoi/cs/daintiness/hecate/art/icon.png")));
 		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
 		
 		
 		// *** VERSIONS ***
 		JPanel versionContainer = new JPanel();
 		versionContainer.setBorder(new EmptyBorder(8, 8, 8, 8));
 		getContentPane().add(versionContainer);
 		versionContainer.setLayout(new BoxLayout(versionContainer, BoxLayout.X_AXIS));
 		
 		JPanel oldVersionPane = new JPanel();
 		String oldVersionStr = d.getVersionNames()[0] + "  ";
 		JPanel oldVersionTablesLine = new JPanel();
 		JLabel lblOldTables = new JLabel(Integer.toString(d.getOldSizes()[0]));
 		JPanel oldVersionAttributesLine = new JPanel();
 		JLabel lblOldAttributes = new JLabel(Integer.toString(d.getOldSizes()[1]));
 		
 		oldVersionPane.setBorder(new TitledBorder(null, oldVersionStr, TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		versionContainer.add(oldVersionPane);
 		oldVersionPane.setLayout(new BoxLayout(oldVersionPane, BoxLayout.Y_AXIS));
 		oldVersionPane.add(oldVersionTablesLine);
 		oldVersionTablesLine.setLayout(new BoxLayout(oldVersionTablesLine, BoxLayout.X_AXIS));
 		oldVersionTablesLine.add(new JLabel("Tables"));
 		oldVersionTablesLine.add(Box.createHorizontalGlue());
 		oldVersionTablesLine.add(lblOldTables);
 		oldVersionPane.add(oldVersionAttributesLine);
 		oldVersionAttributesLine.setLayout(new BoxLayout(oldVersionAttributesLine, BoxLayout.X_AXIS));
 		oldVersionAttributesLine.add(new JLabel("Attributes"));
 		oldVersionAttributesLine.add(Box.createHorizontalGlue());
 		oldVersionAttributesLine.add(lblOldAttributes);
 		
 		JPanel newVersionPane = new JPanel();
		String newVersionStr = d.getVersionNames()[1] + "  ";
 		JPanel newVersionTablesLine = new JPanel();
 		JLabel lblNewTables = new JLabel(Integer.toString(d.getNewSizes()[0]));
 		JPanel newVersionAttributesLine = new JPanel();
 		JLabel lblNewAttributes= new JLabel(Integer.toString(d.getNewSizes()[1]));
 		
 		newVersionPane.setBorder(new TitledBorder(null, newVersionStr, TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		versionContainer.add(newVersionPane);
 		newVersionPane.setLayout(new BoxLayout(newVersionPane, BoxLayout.Y_AXIS));
 		newVersionPane.add(newVersionTablesLine);
 		newVersionTablesLine.setLayout(new BoxLayout(newVersionTablesLine, BoxLayout.X_AXIS));
 		newVersionTablesLine.add(new JLabel("Tables"));
 		newVersionTablesLine.add(Box.createHorizontalGlue());
 		newVersionTablesLine.add(lblNewTables);
 		newVersionPane.add(newVersionAttributesLine);
 		newVersionAttributesLine.setLayout(new BoxLayout(newVersionAttributesLine, BoxLayout.X_AXIS));
 		newVersionAttributesLine.add(new JLabel("Attributes"));
 		newVersionAttributesLine.add(Box.createHorizontalGlue());
 		newVersionAttributesLine.add(lblNewAttributes);
 		
 		
 		// *** TRANSITIONS ***
 		JPanel transitionContainer = new JPanel();
 		transitionContainer.setBorder(new EmptyBorder(8, 8, 8, 8));
 		getContentPane().add(transitionContainer);
 		transitionContainer.setLayout(new BoxLayout(transitionContainer, BoxLayout.X_AXIS));
 		
 		JPanel deletionsPane = new JPanel();
 		JPanel deletionTablesLine = new JPanel();
 		JLabel lblDeletionTables = new JLabel(Integer.toString(d.getTableMetrics()[1]));
 		JPanel deletionAttributesLine = new JPanel();
 		JLabel lblDeletionAttributes = new JLabel(Integer.toString(d.getAttributeMetrics()[1]));
 		
 		deletionsPane.setBorder(new TitledBorder(null, "Deletions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		transitionContainer.add(deletionsPane);
 		deletionsPane.setLayout(new BoxLayout(deletionsPane, BoxLayout.Y_AXIS));
 		deletionsPane.add(deletionTablesLine);
 		deletionTablesLine.setLayout(new BoxLayout(deletionTablesLine, BoxLayout.X_AXIS));
 		deletionTablesLine.add(new JLabel("Tables"));
 		deletionTablesLine.add(Box.createHorizontalGlue());
 		deletionTablesLine.add(lblDeletionTables);
 		deletionsPane.add(deletionAttributesLine);
 		deletionAttributesLine.setLayout(new BoxLayout(deletionAttributesLine, BoxLayout.X_AXIS));
 		deletionAttributesLine.add(new JLabel("Attributes"));
 		deletionAttributesLine.add(Box.createHorizontalGlue());
 		deletionAttributesLine.add(lblDeletionAttributes);
 		
 		JPanel insertionsPane = new JPanel();
 		JPanel insertionTableLine = new JPanel();
 		JLabel lblInsertionTables = new JLabel(Integer.toString(d.getTableMetrics()[0]));
 		JPanel insertionAttributeLine = new JPanel();
 		JLabel lblInsertionAttributes = new JLabel(Integer.toString(d.getAttributeMetrics()[0]));
 		
 		insertionsPane.setBorder(new TitledBorder(null, "Insertions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		transitionContainer.add(insertionsPane);
 		insertionsPane.setLayout(new BoxLayout(insertionsPane, BoxLayout.Y_AXIS));
 		insertionsPane.add(insertionTableLine);
 		insertionTableLine.setLayout(new BoxLayout(insertionTableLine, BoxLayout.X_AXIS));
 		insertionTableLine.add(new JLabel("Tables"));
 		insertionTableLine.add(Box.createHorizontalGlue());
 		insertionTableLine.add(lblInsertionTables);
 		insertionsPane.add(insertionAttributeLine);
 		insertionAttributeLine.setLayout(new BoxLayout(insertionAttributeLine, BoxLayout.X_AXIS));
 		insertionAttributeLine.add(new JLabel("Attributes"));
 		insertionAttributeLine.add(Box.createHorizontalGlue());
 		insertionAttributeLine.add(lblInsertionAttributes);
 		
 		
 //		JPanel exportContainer = new JPanel();
 //		JButton btnExport = new JButton("Export");
 //		getContentPane().add(Box.createVerticalGlue());
 //		exportContainer.setBorder(new EmptyBorder(8, 8, 8, 8));
 //		getContentPane().add(exportContainer);
 //		exportContainer.setLayout(new BoxLayout(exportContainer, BoxLayout.X_AXIS));
 //		exportContainer.add(btnExport);
 		
 		pack();
 	}
 }
