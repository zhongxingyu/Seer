 package com.felipelo.etlgui.schema.txt;
 
 import br.com.saxes.suite.converter.ValueType;
 import br.com.saxes.suite.model.DateTreeNode;
 import br.com.saxes.suite.model.NumericTreeNode;
 import br.com.saxes.suite.model.TextTreeNode;
 import com.felipelo.etlgui.schema.PropertyTableModel;
 import com.felipelo.etlgui.schema.model.DateMutable;
 import com.felipelo.etlgui.schema.model.NumericMutable;
 import com.felipelo.etlgui.schema.model.TextMutable;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 
 /**
  *
  * @author felipe.lorenz
  */
 public class TextNodePropTableModel extends PropertyTableModel {
 	
 	public TextNodePropTableModel( DefaultTreeModel treeModel ) {
 		super(treeModel);
 	}
 	
 	@Override
 	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 		 if (aValue == null)
 			return;
 		 
 		 TextTreeNode _textTreeNode = (TextTreeNode) treeNode;
 		 
 		 switch(rowIndex) {
 			case 2:
 				if( _textTreeNode.getValueType() != aValue) {
					DefaultMutableTreeNode _parent = (DefaultMutableTreeNode) mutableTreeNode.getParent();
 					int _index = treeModel.getIndexOfChild( _parent, mutableTreeNode );
 					DefaultMutableTreeNode _oldMutable = mutableTreeNode;
					 
 					switch((ValueType)aValue) {
 						case DATE:
 							DateTreeNode _dateTN = new DateTreeNode();
 							_dateTN.setId( _textTreeNode.getId() );
 							_dateTN.setName( _textTreeNode.getName() );
 							_dateTN.setDescription( _textTreeNode.getDescription() );
 							_dateTN.setParentTreeNode( _textTreeNode.getParentTreeNode() );
 							
 							mutableTreeNode = new DateMutable( _dateTN, new DateNodePropTableModel(treeModel) );
 							this.treeNode = _dateTN;
 							
 							break;
 						case NUMERIC:
 							NumericTreeNode _numTN = new NumericTreeNode();
 							_numTN.setId( _textTreeNode.getId() );
 							_numTN.setName( _textTreeNode.getName() );
 							_numTN.setDescription( _textTreeNode.getDescription() );
 							_numTN.setParentTreeNode( _textTreeNode.getParentTreeNode() );
 							
 							mutableTreeNode = new NumericMutable( _numTN, new NumericNodePropTableModel(treeModel) );
 							this.treeNode = _numTN;
 							
 							break;
 					}
 					
 					treeModel.insertNodeInto( mutableTreeNode, _parent, _index );
 					treeModel.removeNodeFromParent( _oldMutable );
 				}
 				break;
 				
 		 }
 		 super.setValueAt(aValue, rowIndex, columnIndex);
 	}
 	
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		Object value;
 		
 		value = super.getValueAt(rowIndex, columnIndex);
 		if( value != null )
 			return value;
 		
 		if( columnIndex == 0 ) {
 			switch(rowIndex) {
 				case 2:
 					return "Value Type";
 			}
 		}
 		
 		if( treeNode == null ) {
 			return value;
 		}
 		
 		TextTreeNode _textTN = (TextTreeNode)treeNode;
 		
 		switch(rowIndex) {
 			case 2:
 				value = _textTN.getValueType();
 				break;
 		}
 		
 		return value;
 	}
 	
 }
