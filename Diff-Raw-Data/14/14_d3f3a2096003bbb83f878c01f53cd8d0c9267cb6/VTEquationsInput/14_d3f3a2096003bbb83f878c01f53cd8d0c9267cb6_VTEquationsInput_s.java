 package applets.Termumformungen$in$der$Technik_02_Kondensatoren;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.LineBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 public class VTEquationsInput extends VisualThing {
 	
 	abstract class EquationPanel extends JPanel {
 		private static final long serialVersionUID = 1L;
 		MathTextField textField = new MathTextField();
 		JLabel infoLabel = new JLabel();
 		JButton removeButton = new JButton();
 		EquationSystem.Equation eq = new EquationSystem.Equation();
 		boolean correct = false;
 		boolean correctInput = false;
 				
 		EquationPanel() {
 			super();
 			this.setLayout(null);
 			textField.getDocument().addDocumentListener(new DocumentListener() {
 				public void removeUpdate(DocumentEvent e) { updateEq(); }
 				public void insertUpdate(DocumentEvent e) { updateEq(); }
 				public void changedUpdate(DocumentEvent e) { updateEq(); }
 			});
 			removeButton.setText("-");
 			removeButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) { onRemoveClick(); }
 			});
 			this.add(textField);
 			this.add(infoLabel);
 			this.add(removeButton);
 			doLayout();
 		}
 
 		@Override public void doLayout() {
 			int height = 30;
 			int removeButtonWidth = 30;
 			textField.setBounds(0, 0, this.getWidth() - removeButtonWidth - 5, height);
 			removeButton.setBounds(this.getWidth() - removeButtonWidth, 0, removeButtonWidth, removeButtonWidth);
 			if(infoLabel.getText().isEmpty()) {
 				infoLabel.setVisible(false);
 			}
 			else {
 				infoLabel.setVisible(true);
 				//int infoLabelWidth = infoLabel.getFontMetrics(infoLabel.getFont()).stringWidth(infoLabel.getText());
 				int infoLabelHeight = infoLabel.getFontMetrics(infoLabel.getFont()).getHeight();
 				infoLabel.setBounds(0, textField.getHeight() + 1, this.getWidth(), infoLabelHeight);
 				height = infoLabel.getY() + infoLabelHeight;
 			}
 			this.setPreferredSize(new Dimension(this.getWidth(), height));
 		}
 		
 		void setInputError(String s) {
 			//System.out.println(s + ", expression: " + textField.getOperatorTree());
 			correct = false;
 			infoLabel.setForeground(Color.red.brighter());
 			infoLabel.setText(s);
 			textField.setBackground(Color.white);
 			revalidate();
 		}
 		
 		void setInputWrong(String s) {
 			if(!s.isEmpty()) System.out.println(s + ", equation: " + eq.normalize());
 			correct = false;
 			infoLabel.setForeground(Color.red);
 			infoLabel.setText(s);
 			textField.setBackground(Color.red.brighter());
 			revalidate();			
 		}
 
 		void setInputRight(String s) {
 			correct = true;
 			infoLabel.setForeground(Color.blue);
 			infoLabel.setText(s);
 			textField.setBackground(Color.green.brighter());
 			revalidate();			
 		}
 
 		void resetInput() { updateEq(); }
 		
 		void updateEq() {
 			correctInput = false;
 			try {
 				eq = new EquationSystem.Equation(textField.getOperatorTree(), eqSys.variableSymbols);
 				correctInput = true;
 			} catch (EquationSystem.Equation.ParseError e) {
 				setInputError("Eingabe: " + e.german);
 				return;
 			}
 			try {
 				onEquationUpdate();
 			}
 			catch(Throwable e) {
 				e.printStackTrace();
 			}
 		}
 		
 		abstract void onRemoveClick();
 		abstract void onEquationUpdate();
 	}
 	
 	abstract class EquationsPanel extends JPanel {
 		private static final long serialVersionUID = 1L;
 		List<EquationPanel> equations = new LinkedList<EquationPanel>();
 		JButton addNewEquationButton = new JButton();
 		
 		public EquationsPanel() { this(0); }
 		public EquationsPanel(int startSize) {
 			super();
 			this.setLayout(null);
 			this.setBorder(new LineBorder(Color.black, 1));
 			addNewEquationButton.setText("+");
 			addNewEquationButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					EquationPanel p = addEquation();
 					p.textField.requestFocus();
 				}
 			});
 			this.add(addNewEquationButton);
 			for(int i = 0; i < startSize; ++i)
 				addEquation();
 		}
 				
 		EquationPanel addEquation() {
 			final EquationPanel eqp = new EquationPanel() {
 				private static final long serialVersionUID = 1L;
 				@Override void onEquationUpdate() { EquationsPanel.this.onEquationUpdate(this); }
 				@Override void onRemoveClick() { removeEquation(this); }
 			};
 			eqp.textField.addKeyListener(new KeyListener() {
 				public void keyTyped(KeyEvent e) {}
 				public void keyReleased(KeyEvent e) {}
 				public void keyPressed(KeyEvent e) {
 					switch(e.getKeyCode()) {
 					case KeyEvent.VK_ENTER:
 						addNewEquationButton.doClick();
 						break;
 					case KeyEvent.VK_BACK_SPACE:
 						if(eqp.textField.getText().isEmpty())
 							removeEquation(eqp);
 						break;
 					}
 				}
 			});
 			equations.add(eqp);
 			this.add(eqp, equations.size());
 			revalidate();
 			return eqp;
 		}
 		
 		void removeEquation(EquationPanel eqp) {
 			EquationPanel last = null;
 			for(Iterator<EquationPanel> i = equations.iterator(); i.hasNext();) {
 				EquationPanel p = i.next();
 				if(p == eqp) {
 					i.remove();
 					this.remove(p);
 					if(last != null) last.textField.requestFocus();
 					revalidate();
 					return;
 				}
 				last = p;
 			}
 			throw new AssertionError("equation panel not found");
 		}
 		
 		abstract boolean recheck(EquationPanel eqp);
 		
 		Iterator<EquationPanel> getNextAfter(EquationPanel eqp) {			
 			for(Iterator<EquationPanel> eqpIter = equations.iterator(); eqpIter.hasNext();)
 				if(eqpIter.next() == eqp) return eqpIter;
 			throw new AssertionError("equation panel not found");				
 		}
 		boolean recheckAllFrom(EquationPanel eqp) {
 			Iterator<EquationPanel> eqpIter = getNextAfter(eqp);
 			if(!recheck(eqp)) { resetRest(eqpIter); return false; }
 			return recheckAll(eqpIter);
 		}
 		boolean recheckAll(Iterator<EquationPanel> eqpIter) { while(eqpIter.hasNext()) if(!recheck(eqpIter.next())) { resetRest(eqpIter); return false; } return true; }
 		boolean recheckAll() { return recheckAll(equations.iterator()); }
 		void resetRest(Iterator<EquationPanel> eqpIter) { while(eqpIter.hasNext()) eqpIter.next().resetInput(); }
 		void resetAll() { resetRest(equations.iterator()); }
 
 		boolean haveEarlierSameEquation(EquationPanel eqp) {
 			for(EquationPanel p : equations) {
 				if(p == eqp) break;
 				if(p.eq.equals(eqp.eq)) return true;
 			}
 			return false;
 		}
 
 		void onEquationUpdate(EquationPanel eqp) { recheckAllFrom(eqp); }
 		
 		@Override public void doLayout() {
 			int height = 0;
 			int y = 5;
 			for(EquationPanel eqp : equations) {
 				eqp.setBounds(1, y, this.getWidth() - 2, eqp.getPreferredSize().height);
 				eqp.doLayout();
 				eqp.setBounds(1, y, this.getWidth() - 2, eqp.getPreferredSize().height);
 				y += eqp.getHeight() + 5;
 			}
 			addNewEquationButton.setBounds(1, y, 30, 30);
 			height = y + addNewEquationButton.getHeight();
 			this.setPreferredSize(new Dimension(this.getWidth(), height));
 		}
 				
 		void clear() {
 			for(Iterator<EquationPanel> i = equations.iterator(); i.hasNext();) {
 				EquationPanel eqp = i.next();
 				i.remove();
 				this.remove(eqp);
 			}			
 			revalidate();
 		}
 	}
 	
 	class MainPanel extends EquationsPanel {
 		private static final long serialVersionUID = 1L;
 
 		public MainPanel() { clear(); }		
 		void clear() {
 			super.clear();
 			addEquation();
 		}
 		
 		EquationSystem eqSysForNewEquation(final EquationPanel eqp) {
 			/*			
 			Iterable<EquationPanel> restEquPanels = Utils.cuttedFromRight(this.equations,
 					new Utils.Predicate<EquationPanel>() {
 						public boolean apply(EquationPanel obj) {
 							return eqp == obj;
 						}
 					});
 			Iterable<EquationSystem.Equation> restEquations = Utils.map(
 					restEquPanels,
 					new Utils.Function<EquationPanel,EquationSystem.Equation>() {
 						public EquationSystem.Equation eval(EquationPanel obj) { return obj.eq; }
 					}); */
 			return new EquationSystem(
 					eqSys.equations,
 					//Utils.concatCollectionView(eqSys.equations, restEquations),
 					eqSys.variableSymbols
 					);
 		}
 		
 		@Override boolean recheck(EquationPanel eqp) {
 			if(!eqp.correctInput) return false;
 			//System.out.print("base eq ");
 			//eqp.baseEqSystem.dump();
 			EquationSystem eqSys = eqSysForNewEquation(eqp);
 			if(eqSys.canConcludeTo(eqp.eq)) {
 				if(wantedResult.isWanted(eqp.eq))
 					eqp.setInputRight("fertig :)");
 				else if(eqSys.contains(eqp.eq))
 					eqp.setInputWrong("doppelt");
 				else
 					eqp.setInputRight("");
 			}
 			else {
 				eqp.setInputWrong("kann nicht hergeleitet werden");
 				//System.out.println("eq: " + eqp.eq);
 				//System.out.println("sys: ");
 				//eqSys.dump();
 				return false;
 			}
 			return true;
 		}
 	}
 	
 	MainPanel mainPanel = null;
 	String name;
 	int stepX;
 	int stepY;
 	int width;
 	private EquationSystem eqSys = null;
 	
 	class WantedResult {
 		String wantedExprStr = "";
 		Utils.OperatorTree wantedExpr = null;
 		Set<String> allowedVars = null;
 		
 		WantedResult() {}
 		WantedResult(String wantedExprStr, Iterable<String> allowedVars) {
 			try {
 				this.wantedExprStr = wantedExprStr;
 				this.wantedExpr = Utils.OperatorTree.parse(wantedExprStr).simplify();
 				this.allowedVars = new TreeSet<String>(Utils.collFromIter(allowedVars));
 			}
 			catch(Throwable e) {
 				e.printStackTrace();
 			}
 		}
 		
 		boolean isWantedExpr(Utils.OperatorTree expr) {
 			expr = expr.simplify();
 			if(wantedExpr.equals(expr)) return true;
 			//System.out.println("not wanted. " + expr + " != " + wantedExpr);
 			return false;
 		}
 		
 		boolean hasOnlyAllowedVars(Utils.OperatorTree expr) {
			for(String varName : expr.leafsAsString()) {
 				if(!allowedVars.contains(varName)) {
 					//System.out.println("not allowed var: " + varName + " in " + expr);
 					return false;					
 				}
 			}
 			return true;
 		}
 		
 		boolean isWanted(EquationSystem.Equation eq) {
 			if(isWantedExpr(eq.left) && hasOnlyAllowedVars(eq.right)) return true;
 			if(isWantedExpr(eq.right) && hasOnlyAllowedVars(eq.left)) return true;
 			return false;
 		}
 		
 		@Override public String toString() {
 			return "Geben Sie " + wantedExprStr + " in Abhängigkeit von " + Utils.concat(allowedVars, ", ") + " an.";
 		}
 	}
 	WantedResult wantedResult = new WantedResult();
 	
 	void setEquationSystem(EquationSystem eqSys, String wantedExpr, Iterable<String> allowedVars) {
 		this.eqSys = eqSys;
 		this.eqSys.dump();
 		this.wantedResult = new WantedResult(wantedExpr, allowedVars);
 		//System.out.print("linear independent "); this.eqSys_linearIndependent.dump();
 	}
 	
 	VTEquationsInput(String name, int stepX, int stepY, int width) {
 		this.name = name;
 		this.stepX = stepX;
 		this.stepY = stepY;
 		this.width = width;
 	}
 	
 	@Override
 	public Component getComponent() {
 		if(mainPanel == null) {
 			mainPanel = new MainPanel();
 			mainPanel.setName(name);
 		}
 		return mainPanel;
 	}
 
 	@Override public int getWidth() { return width; }
 	@Override public int getHeight() { getComponent(); return mainPanel.getPreferredSize().height; }
 	@Override public int getStepX() { return stepX; }
 	@Override public int getStepY() { return stepY; }
 	@Override public void setStepX(int v) { stepX = v; }
 	@Override public void setStepY(int v) { stepY = v; }
 
 	public void clear() {
 		if(mainPanel != null)
 			mainPanel.clear();
 	}
 
 }
