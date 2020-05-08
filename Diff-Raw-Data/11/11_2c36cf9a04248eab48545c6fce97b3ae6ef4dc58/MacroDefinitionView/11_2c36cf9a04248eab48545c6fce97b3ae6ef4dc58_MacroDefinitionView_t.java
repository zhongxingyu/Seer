 package lambda.gui.macroview;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
import java.awt.FontMetrics;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 import lambda.ast.Lambda;
 
 @SuppressWarnings("serial")
 public class MacroDefinitionView extends JPanel
 {
 	private Set<String> items = new HashSet<String>();
 	private DefaultTableModel tableModel;
 	private JTable macroTable;
 
 	public MacroDefinitionView()
 	{
 		setLayout(new BorderLayout());
 
 		tableModel = new DefaultTableModel()
 		{
 			public boolean isCellEditable(int row, int column)
 			{
 				return false;
 			}
 		};
 		tableModel.addColumn("Name");
 		tableModel.addColumn("Definition");
 		macroTable = new JTable(tableModel);
 		macroTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		macroTable.setFillsViewportHeight(true);
 		macroTable.setDragEnabled(false);
 		macroTable.setRowSelectionAllowed(true);
 		macroTable.getTableHeader().setReorderingAllowed(false);
 		macroTable.setRowSorter(new TableRowSorter<TableModel>(tableModel));
 		add(new JScrollPane(macroTable), BorderLayout.CENTER);
 	}
 
 	public void setFont(Font font)
 	{
 		super.setFont(font);
 		if (macroTable != null)
 		{
 			macroTable.setFont(font);
			FontMetrics fm = getFontMetrics(font);
			macroTable.setRowHeight(fm.getHeight());
 		}
 	}
 
 	public void addMacro(String name, Lambda def)
 	{
 		if (items.contains(name))
 		{
 			for (int row = 0; row < tableModel.getRowCount(); row++)
 			{
 				if (name.equals(tableModel.getValueAt(row, 0)))
 				{
 					tableModel.removeRow(row);
 					break;
 				}
 			}
 		}
 		else
 		{
 			items.add(name);
 		}
 		tableModel.addRow(new Object[] { name, def.toString() });
 	}
 
 	public void clearList()
 	{
 		items.clear();
 		tableModel.setRowCount(0);
 	}
 }
