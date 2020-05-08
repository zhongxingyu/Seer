 package gui;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashSet;
 import java.util.Vector;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.UIManager;
 import javax.swing.table.TableCellRenderer;
 
 import code.NgramViewer;
 import code.StringUtils;
 
 class AddEditExpansionButtonRenderer extends JButton implements TableCellRenderer {
 
 	  /**
 	 * 
 	 */
 	private static final long serialVersionUID = -6192443981155186455L;
 
 	public AddEditExpansionButtonRenderer() {
 	    setOpaque(true);
 	  }
 
 	  public Component getTableCellRendererComponent(JTable table, Object value,
 	      boolean isSelected, boolean hasFocus, int row, int column) {
 	    if (isSelected) {
 	    	System.out.println(row + " " +column);
 	      setForeground(table.getSelectionForeground());
 	      setBackground(table.getSelectionBackground());
 	    } else {
 	      setForeground(table.getForeground());
 	      setBackground(UIManager.getColor("Button.background"));
 	    }
 	    setText((value == null) ? "" : value.toString());
 	    return this;
 	  }
 	}
 
 	/**
 	 * @version 1.0 11/09/98
 	 */
 
 	class AddEditExpansionButtonEditor extends DefaultCellEditor {
 	  /**
 		 * 
 		 */
 		private static final long serialVersionUID = 5697922254476725878L;
 
 	protected JButton button;
 
 	  private String label;
 	  private String data;
 	  private int parentDesc;
 	  private String tableName;
 
 	  private boolean isPushed;
 
 	  public AddEditExpansionButtonEditor(JCheckBox checkBox) {
 	    super(checkBox);
 	
 	    button = new JButton();
 	    button.setOpaque(true);
 	    button.addActionListener(new ActionListener() {
 	      public void actionPerformed(ActionEvent e) {
 	        fireEditingStopped();
 	      }
 	    });
 	  }
 
 	  public Component getTableCellEditorComponent(JTable table, Object value,
 	      boolean isSelected, int row, int column) {
 	    if (isSelected) {
 	      button.setForeground(table.getSelectionForeground());
 	      button.setBackground(table.getSelectionBackground());
 	    } else {
 	      button.setForeground(table.getForeground());
 	      button.setBackground(table.getBackground());
 	    }
 	    label = (value == null) ? "" : value.toString();
 	    data = table.getValueAt(row, 1).toString();
 	    parentDesc = Integer.parseInt(table.getValueAt(row, 7).toString());
 	    button.setText(label);
 	    isPushed = true;
 	    tableName = table.getName();
 	    return button;
 	  }
 
 	  public Object getCellEditorValue() {
 	    if (isPushed) {
 	      // 
 	      //
 	    	 String formatedData = NgramViewer.mergeNgrams(StringUtils.convertStringToList(data));
 	    	 int selection = JOptionPane.showConfirmDialog(
                      null
              , "       :" + formatedData + "?"
              , "  "
              , JOptionPane.OK_CANCEL_OPTION
              , JOptionPane.INFORMATION_MESSAGE);
 	    	 
 	    	 if (selection == JOptionPane.OK_OPTION)
 	    		 if (tableName.equals(EditJudgePanel.ANCIENTPANEL)){
 	    			 HashSet<String> ancientSet = new HashSet<String>();
 	    			 for(Object element:EditJudgePanel.ancientExpDm.getDataVector())
 	    				 ancientSet.add((String) ((Vector<?>) element).get(0));
 	    			 if (ancientSet.contains(data))
 	    				 JOptionPane.showMessageDialog(null, "  ", "  ", 0);
 	    			 else	 
	    				 EditJudgePanel.ancientExpDm.addRow(new Object[]{data,parentDesc});
 	    		 }
 	    		 else {
 	    			 HashSet<String> modernSet = new HashSet<String>();
 	    			 for(Object element:EditJudgePanel.modernExpDm.getDataVector())
 	    				 modernSet.add((String) ((Vector<?>) element).get(0));
 	    			 if (modernSet.contains(data))
 	    				 JOptionPane.showMessageDialog(null, "  ", "  ", 0);
 	    			 else {
	    				 EditJudgePanel.modernExpDm.addRow(new Object[]{data,parentDesc});
 	    			 }
 	    			 	
 	    			 }
 	      
 	    }
 	    isPushed = false;
 	    return new String(label);
 	  }
 
 	  public boolean stopCellEditing() {
 	    isPushed = false;
 	    return super.stopCellEditing();
 	  }
 
 	  protected void fireEditingStopped() {
 	    super.fireEditingStopped();
 	  }
 	}
