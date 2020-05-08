 package applets.Termumformungen$in$der$Technik_01_URI;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
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
 
 public class VTEquationInput extends VisualThing {
 	
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
 		SimpleLabel descriptionLabel = new SimpleLabel();
 		List<EquationPanel> equations = new LinkedList<EquationPanel>();
 		JButton addNewEquationButton = new JButton();
 		
 		public EquationsPanel() { this(0); }
 		public EquationsPanel(int startSize) {
 			super();
 			this.setLayout(null);
 			this.setBorder(new LineBorder(Color.black, 1));
 			addNewEquationButton.setText("+");
 			addNewEquationButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) { addEquation().requestFocus(); }
 			});
 			this.add(descriptionLabel);
 			this.add(addNewEquationButton);
 			for(int i = 0; i < startSize; ++i)
 				addEquation();
 		}
 				
 		EquationPanel addEquation() {
 			EquationPanel eqp = new EquationPanel() {
 				private static final long serialVersionUID = 1L;
 				@Override void onEquationUpdate() { EquationsPanel.this.onEquationUpdate(this); }
 				@Override void onRemoveClick() { removeEquation(this); }
 			};
 			equations.add(eqp);
 			this.add(eqp, equations.size());
 			revalidate();
 			return eqp;
 		}
 		
 		void removeEquation(EquationPanel eqp) {
 			for(Iterator<EquationPanel> i = equations.iterator(); i.hasNext();) {
 				if(i.next() == eqp) {
 					i.remove();
 					this.remove(eqp);
 					revalidate();
 					return;
 				}
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
 			descriptionLabel.fixedWidth = this.getWidth() - 2 - 10;
 			int y = descriptionLabel.getPreferredSize().height + 5;
 			descriptionLabel.setBounds(1 + 5, 2, descriptionLabel.fixedWidth, y);
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
 	
 	class MainPanel extends JPanel {
 		private static final long serialVersionUID = 1L;
 
 		class BasicEquationsPanel extends EquationsPanel {
 			private static final long serialVersionUID = 1L;
 			{
 				super.descriptionLabel.text =
 					"Geben Sie Gleichungen, die Sie benötigen " +
 					"und aus der Schaltung ablesen können, " +
 					"hier ein.";
 			}
 			
 			public BasicEquationsPanel(int i) { super(i); }
 
 			@Override void onEquationUpdate(EquationPanel eqp) {
 				if(!recheckAllFrom(eqp))
 					followingEquationsPanel.resetAll();
 				else
 					followingEquationsPanel.recheckAll();
 			}
 			
 			@Override boolean recheck(EquationPanel eqp) {
 				if(!eqp.correctInput) return false;
 				if(eqSys.contains(eqp.eq.normalize())) {
 					if(!haveEarlierSameEquation(eqp))
 						eqp.setInputRight("");
 					else
 						eqp.setInputWrong("doppelt");
 					return true;
 				} else {
 					eqp.setInputWrong("kann nicht aus der Schaltung abgelesen werden");
 					return false;
 				}
 			}
 		}
 		
 		class FollowingEquationsPanel extends EquationsPanel {
 			private static final long serialVersionUID = 1L;
 			{
 				super.descriptionLabel.text =
 					"Leiten Sie Schritt für Schritt eine neue Gleichung her, die " +
 					"sich logisch aus den bisherigen Gleichungen folgern lässt.\n" +
 					"Am Ende soll der gefragte Ausdruck hergeleitet sein.";
 			}
 			
 			EquationSystem eqSysForNewEquation(final EquationPanel eqp) {
 				Iterable<EquationPanel> basicEquPanels = basicEquationsPanel.equations;
 				Iterable<EquationPanel> restEquPanels = Utils.cuttedFromRight(this.equations,
 						new Utils.Predicate<EquationPanel>() {
 							public boolean apply(EquationPanel obj) {
 								return eqp == obj;
 							}
 						});
 				Iterable<EquationPanel> allEquPanels = Utils.concatCollectionView(basicEquPanels, restEquPanels);
 				Iterable<EquationSystem.Equation> allEquations = Utils.map(
 						allEquPanels,
 						new Utils.Function<EquationPanel,EquationSystem.Equation>() {
 							public EquationSystem.Equation eval(EquationPanel obj) { return obj.eq; }
 						});
 				return new EquationSystem(
 						Utils.collFromIter(allEquations),
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
 					return false;
 				}
 				return true;
 			}
 		}
 
 		BasicEquationsPanel basicEquationsPanel = new BasicEquationsPanel(1);
 		FollowingEquationsPanel followingEquationsPanel = new FollowingEquationsPanel();
 		
 		@Override public void doLayout() {
 			basicEquationsPanel.setSize(getWidth(), 0);
 			basicEquationsPanel.doLayout();
 			followingEquationsPanel.setSize(getWidth(), 0);
 			followingEquationsPanel.doLayout();
 			basicEquationsPanel.setBounds(0, 0, getWidth(), basicEquationsPanel.getPreferredSize().height);
 			followingEquationsPanel.setBounds(0, basicEquationsPanel.getHeight(), width, followingEquationsPanel.getPreferredSize().height);
 			setPreferredSize(new Dimension(getWidth(), followingEquationsPanel.getY() + followingEquationsPanel.getHeight()));			
 		}
 		
 		MainPanel() {
 			super();
 			this.setLayout(null);
 			this.add(basicEquationsPanel);
 			this.add(followingEquationsPanel);
 			doLayout();
 		}
 
 		void clear() {
 			basicEquationsPanel.clear();
 			basicEquationsPanel.addEquation();
 			followingEquationsPanel.clear();
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
		EquationSystem.Equation.FracSum wantedExpr = new EquationSystem.Equation.FracSum();
		Set<EquationSystem.VariableSymbol> allowedVars = new TreeSet<EquationSystem.VariableSymbol>();
 		
 		WantedResult() {}
 		WantedResult(String wantedExprStr, Iterable<String> allowedVars) {
 			try {
 				this.wantedExprStr = wantedExprStr;
				this.wantedExpr = new EquationSystem.Equation.FracSum(Utils.OperatorTree.parse(wantedExprStr), eqSys.variableSymbols);
				this.wantedExpr = this.wantedExpr.normalize();
				for(String var : allowedVars)
					this.allowedVars.add( eqSys.variableSymbols.get(var) );
 			}
 			catch(Throwable e) {
 				e.printStackTrace();
 			}
 		}
 		
		boolean isWantedExpr(EquationSystem.Equation.FracSum expr) {
			expr = expr.normalize();
 			if(wantedExpr.equals(expr)) return true;
			if(wantedExpr.equals(expr.minusOne())) return true;
			System.out.println("not wanted. " + expr + " != " + wantedExpr);
 			return false;
 		}
 		
		boolean hasOnlyAllowedVars(EquationSystem.Equation.FracSum expr) {
			for(EquationSystem.VariableSymbol var : expr.vars()) {
				if(!allowedVars.contains(var)) {
					System.out.println("not allowed var: " + var + " in " + expr);
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
 	
 	VTEquationInput(String name, int stepX, int stepY, int width) {
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
