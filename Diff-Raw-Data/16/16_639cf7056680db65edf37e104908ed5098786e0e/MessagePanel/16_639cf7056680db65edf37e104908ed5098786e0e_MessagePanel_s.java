 package net.rptools.maptool.client.ui.commandpanel;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Rectangle;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import ca.odell.renderpack.HTMLTableCellRenderer;
 
 public class MessagePanel extends JPanel {
 
 	private JTable messageTable;
 	private JScrollPane scrollPane;
 	
 	public MessagePanel() {
 		setLayout(new BorderLayout());
 		
 		messageTable = createMessageTable();
 		
 		scrollPane = new JScrollPane(messageTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setBorder(null);
 		scrollPane.getViewport().setBackground(Color.white);
 		
 		add(BorderLayout.CENTER, scrollPane);
 	}
 
 	public String getMessagesText() {
		return "FIXME";
 	}
 	
 	public void clearMessages() {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				messageTable.setModel(new DefaultTableModel());
 			}
 		});
 	}
 	
 	public void addMessage(final String message) {
 		
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				synchronized(messageTable) {
 					DefaultTableModel model = (DefaultTableModel) messageTable.getModel();
 					if (model.getRowCount() > 0) {
 						model.removeRow(model.getRowCount()-1);
 					}
 					model.addRow(new Object[]{message});
 					
 					// This makes the bottom row scroll into view, otherwise it cuts off the last part of the last line
					model.addRow(new Object[]{"testing"});
 				}
 			}
 		});
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
 			}
 		});
 	}
 
 	private JTable createMessageTable() {
 		JTable table = new JTable();
 		table.setModel(new DefaultTableModel(new Object[][]{}, new Object[]{""}));
 		table.setTableHeader(null);
 		table.getColumnModel().getColumn(0).setCellRenderer(new MessageCellRenderer());
 		table.setShowGrid(false);
 		table.setBackground(Color.white);
 		
 		return table;
 	}
 	
 	private static class MessageCellRenderer extends HTMLTableCellRenderer {
 		public MessageCellRenderer(){
 			super(true);
 			styleSheet.addRule("body { font-family: sans-serif; font-size: 11pt}");
 	    }
 		
 		@Override
 		public void writeObject(StringBuffer buff, JTable table, Object value, boolean isSelected, boolean isFocused, int row, int col) {
 
 			buff.append("<html><body>");
 			buff.append(value);
 			buff.append("</body></html>");
 		}
 	}
 }
