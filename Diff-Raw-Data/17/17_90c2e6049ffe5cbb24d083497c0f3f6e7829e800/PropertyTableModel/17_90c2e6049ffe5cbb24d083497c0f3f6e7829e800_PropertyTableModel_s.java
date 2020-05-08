 package com.felipelo.etlgui;
 
 import br.com.saxes.suite.model.TreeNode;
 import br.com.saxes.suite.model.txt.TXTTreeSchema;
import javax.swing.JTree;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 
 /**
  *
  * @author felipe
  */
 public class PropertyTableModel extends AbstractTableModel {
 	
 	private final static int NAME = 0;
 	private final static int DESCRIPTION = 1;
 	
 	private DefaultMutableTreeNode mutableTreeNode;
 	private TreeNode treeNode;
 	
 	private String columns[] = {"Property", "Value"};
 	
 	private DefaultTreeModel treeModel;
 	
 	public PropertyTableModel( DefaultTreeModel treeModel ) {
 		super();
 		this.treeModel = treeModel;
 	}
 	
 	public void setTreeNode( DefaultMutableTreeNode mutableTreeNode ) {
 		this.mutableTreeNode = mutableTreeNode;
 		
 		TreeNode _treeNode = (TreeNode) mutableTreeNode.getUserObject();
 		this.treeNode = _treeNode;
 		
 		fireTableRowsInserted(0, 2);
 	}
 	
 	@Override
 	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 		switch(rowIndex) {
 			case NAME:
 				treeNode.setName( (String)aValue );
 				break;
 			case DESCRIPTION:
 				treeNode.setDescription( (String)aValue );
 		}
 		fireTableRowsInserted(rowIndex, rowIndex);
 		treeModel.reload( mutableTreeNode );
     }
 
 	public int getRowCount() {
 		int _rowCount = 0;
 		
 		if( treeNode == null ) {
 			_rowCount = 0;
 		} else if( treeNode instanceof TXTTreeSchema ) {
 			_rowCount = 4;
 		} else {
 			_rowCount = 2;
 		}
 		
 		return _rowCount;
 	}
 
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		String value = "";
 		
 		if( columnIndex == 0 ) {
 			switch(rowIndex) {
 				case NAME:
 					return "Name";
 				case DESCRIPTION:
 					return "Description";
 				case 2:
 					if( treeNode instanceof TXTTreeSchema )
 						return "File Reference";
 				case 3:
 					if( treeNode instanceof TXTTreeSchema )
 						return "Field Qualifier";
 			}
 		}
 		
 		if( treeNode == null ) {
 			return value;
 		}
 		
 		switch(rowIndex) {
 			case NAME:
 				value = treeNode.getName();
 				break;
 			case DESCRIPTION:
 				value = treeNode.getDescription();
 				break;
 			case 2:
 				if( treeNode instanceof TXTTreeSchema )
 					value = ((TXTTreeSchema)treeNode).getFileRef().getFilePath();
 				break;
 			case 3:
 				if( treeNode instanceof TXTTreeSchema )
 					value = ((TXTTreeSchema)treeNode).getFieldQualifier();
 		}
 		
 		return value;
 	}
 
 	public int getColumnCount() {
 		return columns.length;
 	}
 
 	@Override
 	public String getColumnName(int column) {
 		return columns[column];
 	}
 
 	@Override
 	public boolean isCellEditable(int rowIndex, int columnIndex) {
 		return (columnIndex > 0);
 	}
 	
 }
