 package gui.controller;
 
 import gui.BreakpointView;
 import gui.VariableView;
 import interpreter.ArrayValue;
 import interpreter.BooleanValue;
 import interpreter.GlobalBreakpoint;
 import interpreter.IntegerValue;
 import interpreter.Value;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import misc.ExecutionHandler;
 
 import org.antlr.runtime.RecognitionException;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 import ast.Expression;
 import ast.Identifier;
 
 /**
  * This class is responsible for controlling the views @see{VariableView},
  * @see{BreakpointView} and uses the model @see{ExecutionHandler}.
  */
 public class TableViewController implements SelectionListener {
 	/**
 	 * view for displaying variables
 	 */
 	private VariableView varView;
 	/**
 	 * view for displaying global breakpoints
 	 */
 	private BreakpointView breakpointView;
 	/**
 	 * model for variables and global breakpoints
 	 */
 	private ExecutionHandler executionHandler;
 	
 	/**
 	 * Construct a table view controller with the specified views and model.
 	 * @param breakpointView specified view
 	 * @param varView specified view
 	 * @param executionHandler specified model
 	 */
 	public TableViewController(BreakpointView breakpointView, VariableView varView, ExecutionHandler executionHandler) {	
 		this.breakpointView = breakpointView;	
 		this.varView = varView;	
 		this.executionHandler = executionHandler;
 		
 		this.breakpointView.getGlobalBreakpoint().addSelectionListener(this);
 		this.breakpointView.getAddButton().addSelectionListener(this);
 	}
 	
 
 	/**
 	 * Update the variable view.
 	 */
 	public void updateVarView() {        		
         if (this.executionHandler == null || this.executionHandler.getProgramExecution() == null) {
         	return;
         }
         HashMap<Identifier, Value> vars = this.executionHandler.getProgramExecution().getVariables();
       
         //insert Tree items       
         this.varView.getVarTree().removeAll();       
 		Iterator<Map.Entry<Identifier, Value>> it = vars.entrySet().iterator();		
 		while (it.hasNext()) {
 			Map.Entry<Identifier, Value> entry = it.next();
 			String type = entry.getValue().getType().toString();
 			String id = entry.getKey().toString();
 			Value tmp = entry.getValue();
 			this.checkValue(this.varView.getVarTree(), type, id, tmp);
 		}     
 	}
 	
 	/**
 	 * Add variable items to the first level of the tree.
 	 * @param parent variable tree
 	 * @param type type of the variable
 	 * @param id name of the variable
 	 * @param tmp value of the variable
 	 */
 	private void checkValue(Tree parent, String type, String id, Value tmp) {
 		if (tmp instanceof IntegerValue || tmp instanceof BooleanValue) {
 			this.varView.addTreeItem(parent, type, id, tmp.toString());
 		}
 		else if (tmp instanceof ArrayValue) {
 			TreeItem item = this.varView.addTreeItem(parent, type, id, "-");
 			for (int i = 0; i < ((ArrayValue) tmp).getValues().length; i++) {
 				String newId = id + "[" + i + "]";
 				this.checkValue(item, ((ArrayValue) tmp).getValues()[i].getType().toString(), 
 						newId, ((ArrayValue) tmp).getValues()[i]);
 			}
 		}
 	}
 	
 	/**
 	 * Add variable items to the second or deeper levels of the tree.
 	 * @param parent variable tree item
 	 * @param type type of the variable
 	 * @param id name of the variable
 	 * @param tmp value of the variable
 	 */
 	private void checkValue(TreeItem parent, String type, String id, Value tmp) {
 		if (tmp instanceof IntegerValue || tmp instanceof BooleanValue) {
 			this.varView.addTreeItem(parent, type, id, tmp.toString());
 		}
 		else if (tmp instanceof ArrayValue) {
             TreeItem item = this.varView.addTreeItem(parent, type, id, "-");
 			for (int i = 0; i < ((ArrayValue) tmp).getValues().length; i++) {
 				String newId = id + "[" + i + "]";
 				this.checkValue(item, ((ArrayValue) tmp).getValues()[i].getType().toString(), 
 						newId, ((ArrayValue) tmp).getValues()[i]);
 			}
 		}
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		if(e.getSource() == this.breakpointView.getGlobalBreakpoint() && e.detail == SWT.CHECK) {
 			Table table = this.breakpointView.getGlobalBreakpoint();		
 			int i;
 			for (i = 0; i < table.getItemCount(); i++) {
 				if (e.item == table.getItem(i)) {
 					break;
 				}
 			}	
 			if (this.executionHandler.getProgramExecution() != null) {
 				table.getItem(i).setChecked(!table.getItem(i).getChecked());
 				return;
 			} 			
 			this.executionHandler.getGlobalBreakpoints().get(i).setActive(
 					!this.executionHandler.getGlobalBreakpoints().get(i).isActive());
 		} else if(e.getSource() == this.breakpointView.getAddButton()) {
 			String expression = this.breakpointView.getAddTextField().getText();
 			if (expression == null || expression.length() == 0) {
 				return;
 			}		
 			int i;
 			Expression exp = null;
 			try {
 				exp = this.executionHandler.getParserInterface().parseExpression(expression);
 			} catch (RecognitionException ignored) {
			} catch (NullPointerException ignored) {
			}
 			if (exp == null) return;
 			for (i = 0; i < this.executionHandler.getGlobalBreakpoints().size(); i++) {
 				if (this.executionHandler.getGlobalBreakpoints().get(i).getExpression().toString().equals(exp.toString())) {
 					break;
 				}
 			}	
 			if (i >= this.executionHandler.getGlobalBreakpoints().size()) {
 				if (exp != null) {
 					GlobalBreakpoint gBreakpoint = new GlobalBreakpoint(exp);
 					this.executionHandler.getGlobalBreakpoints().add(gBreakpoint);
 				}
 			} else {
 				this.executionHandler.getGlobalBreakpoints().remove(i);
 			}
 			this.breakpointView.drawGlobalBreakpointItem(this.executionHandler.getGlobalBreakpoints());
 		} else if (e.getSource() == this.breakpointView.getGlobalBreakpoint()) {
 			Table table = this.breakpointView.getGlobalBreakpoint();		
 			int i;
 			for (i = 0; i < table.getItemCount(); i++) {
 				if (e.item == table.getItem(i)) {
 					this.breakpointView.getAddTextField().setText(table.getItem(i).getText(1));
 				}
 			}	
 		}
 	} 
 	
 	/**
 	 * Deactivate the variable view to show execution termination.
 	 */
 	public void deactivateVarView() {
 		this.varView.getVarTree().setBackground(new Color(this.varView.getDisplay(), 231, 231, 231));
 		this.varView.getVarTree().setForeground(new Color(this.varView.getDisplay(), 151, 151, 151));
 	}
 	
 	/**
 	 * Reactivate the variable view.
 	 */
 	public void activateVarView() {
 		this.varView.getVarTree().setBackground(null);
 		this.varView.getVarTree().setForeground(null);
 	}
 	
 	/**
 	 * Deactivate the breakpoint view to avoid invalid changes during program execution.
 	 */
 	public void deactivateBreakpointView() {
 		if (this.breakpointView.getActive()) {
 			this.breakpointView.getAddButton().removeSelectionListener(this);
 			this.breakpointView.setActive(false);
 		}
 	}
 	
 	/**
 	 * Reactivate the breakpoint view.
 	 */
 	public void activateBreakpointView() {
 		if (!this.breakpointView.getActive()) {
 			this.breakpointView.getAddButton().addSelectionListener(this);
 			this.breakpointView.setActive(true);
 		}
 	}
 	
 	/**
 	 * Return the variable view of this controller.
 	 * @return variable view
 	 */
 	public VariableView getVarView() {
 		return this.varView;
 	}
 	
 	/**
 	 * Return the breakpoint view of the controller.
 	 * @return breakpoint view
 	 */
 	public BreakpointView getBreakpointView() {
 		return this.breakpointView;
 	}
 	
 	@Override
 	public void widgetDefaultSelected(SelectionEvent arg0) {	
 	}
 }
