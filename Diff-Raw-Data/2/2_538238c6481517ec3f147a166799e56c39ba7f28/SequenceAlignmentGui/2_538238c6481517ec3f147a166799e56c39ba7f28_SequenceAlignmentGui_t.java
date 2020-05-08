 package gui;
 
 import javax.swing.*;
 import javax.swing.event.ListDataListener;
 import javax.swing.table.JTableHeader;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.*;
 
 public class SequenceAlignmentGui
 {
 	private JFrame frame;
 	
 	public SequenceAlignmentGui()
 	{
 		frame = new JFrame("Sequence Alignment");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridBagLayout());
 		
 		GridBagConstraints gbc = new GridBagConstraints();
 		Insets two = new Insets(2, 2, 2, 2);
 		gbc.insets = two;
 		gbc.fill = GridBagConstraints.BOTH;
 		panel.add(getSequencesPanel(), gbc);
 		
 		gbc.gridx = 1;
 		panel.add(getScoringPanel(), gbc);
 		
 		gbc = new GridBagConstraints();
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.gridwidth = 2;
 		gbc.gridy = 1;
 		gbc.insets = two;
 		panel.add(getTable(), gbc);
 		
 		gbc = new GridBagConstraints();
 		frame.add(panel);
 		frame.pack();
 		frame.setVisible(true);
 	}
 	
 	private JComponent getSequencesPanel()
 	{
 		JPanel panel = new JPanel();
 		panel.setBorder(BorderFactory.createTitledBorder("Sequences"));
 		return panel;
 	}
 	
 	private JComponent getScoringPanel()
 	{
 		JPanel panel = new JPanel();
 		panel.setBorder(BorderFactory.createTitledBorder("Scoring"));
 		return panel;
 	}
 	
 	private JComponent getTable()
 	{
 		JTable table = new JTable();
 		return table;
 	}
 	
 	private static class StringListModel extends AbstractListModel
 	{
 		private String string;
 		
 		public StringListModel(String string_)
 		{
 			string = string_;
 		}
 		
 		public void setString(String string_)
 		{
 			string = string_;
 		}
 		
 		@Override
 		public Object getElementAt(int index)
 		{
			return string.substring(index, index + 1);
 		}
 		
 		@Override
 		public int getSize()
 		{
 			return string.length();
 		}
 	}
 	
 	private static class RowHeaderRenderer extends JLabel implements ListCellRenderer
 	{
 		public RowHeaderRenderer(JTable table)
 		{
 			JTableHeader header = table.getTableHeader();
 			setOpaque(true);
 			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
 			setHorizontalAlignment(CENTER);
 			setForeground(header.getForeground());
 			setBackground(header.getBackground());
 			setFont(header.getFont());
 		}
 		
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
 			boolean cellHasFocus)
 		{
 			setText((value == null) ? "" : value.toString());
 			return this;
 		}
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		new SequenceAlignmentGui();
 	}
 }
