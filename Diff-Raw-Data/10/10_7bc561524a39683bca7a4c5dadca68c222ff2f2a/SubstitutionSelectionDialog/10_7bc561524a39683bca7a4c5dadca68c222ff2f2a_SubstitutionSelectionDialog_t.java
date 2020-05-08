 package edu.emory.prove_it;
 import java.awt.Dialog;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Vector;
 
import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 
 import edu.emory.prove_it.expression.Expression;
 import edu.emory.prove_it.expression.OperatorExpression;
 import edu.emory.prove_it.expression.Operators;
 import edu.emory.prove_it.expression.Statement;
import edu.emory.prove_it.util.LatexHandler;
 import acm.gui.TableLayout;
 
 @SuppressWarnings("serial")
 public class SubstitutionSelectionDialog extends JDialog {
 	
 	private final Statement substitutingIn;
 	private final JLabel previewLabel;
 	private final SubstitutionCellRenderer renderer = new SubstitutionCellRenderer();
 	private final JComboBox<Statement> selectionBox;
 	private final JCheckBox checkBox;
 	private boolean choiceMade = true;
 	
 	public SubstitutionSelectionDialog(MainWindow owner, Statement substitutingIn) {
 		super();
 		setSize(400, 300);
 		setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
 		this.substitutingIn = substitutingIn;
		previewLabel = new JLabel(new ImageIcon(LatexHandler.latexToImage(substitutingIn.toLatex())));
 		
 		Object[] statementsFromMainWindow = owner.getStatements();
 		Vector<Statement> statements = new Vector<Statement>();
 		for (int i = 0; i<statementsFromMainWindow.length; i++) {
 			Expression ex = ((Statement) statementsFromMainWindow[i]).getExpression();
 			if (ex instanceof OperatorExpression
 					&& ! substitutingIn.equals(ex)
 					&& ((OperatorExpression) ex).getOp().equals(Operators.named("=")))
 				statements.add((Statement) statementsFromMainWindow[i]);
 		}		
 		
 		selectionBox = new JComboBox<Statement>(statements);
 		selectionBox.setRenderer(renderer);
 		selectionBox.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent event) {
 				updatePreview();
 			}
 		});
 		
 		checkBox = new JCheckBox("Reverse");
 		checkBox.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent event) {
 				renderer.reversed = !renderer.reversed;
 				selectionBox.update(selectionBox.getGraphics());
 				updatePreview();
 			}
 		});
 		
 		JButton okButton = new JButton("OK");
 		okButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				choiceMade = true;
 				setVisible(false);
 			}
 		});
 		JButton cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				choiceMade = false;
 				setVisible(false);
 			}
 		});
 				
 		setLayout(new TableLayout(6, 2, 10, 10));
 		add(new JLabel(substitutingIn.toLatex()), "gridwidth=2");
 		add(new JLabel("becomes"), "gridwidth=2");
 		add(previewLabel, "gridwidth=2");
 		add(new JLabel());
 		add(checkBox);
 		add(selectionBox, "gridwidth=2");
 		add(okButton);
 		add(cancelButton);
 		
 	}
 	
 	public Statement getChoice() {
 		if (choiceMade) {
 			return (Statement) selectionBox.getSelectedItem();
 		}
 		
 		return null;
 	}
 	public Expression getFrom() {
 		if (choiceMade) {
 			OperatorExpression ex = (OperatorExpression) getChoice().getExpression();
 			return renderer.reversed ? ex.getArg(1) : ex.getArg(0);
 		}
 		
 		return null;
 	}
 	public Expression getTo() {
 		if (choiceMade) {
 			OperatorExpression ex = (OperatorExpression) getChoice().getExpression();
 			return renderer.reversed ? ex.getArg(0) : ex.getArg(1);
 		}
 		
 		return null;
 	}
 	
 	private void updatePreview() {
		String latex = substitutingIn.substitute(getFrom(), getTo()).toLatex();
		previewLabel.setIcon(new ImageIcon(LatexHandler.latexToImage(latex)));
 	}
 
 }
