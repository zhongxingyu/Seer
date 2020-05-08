 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.equation.ui;
 
 import com.jme.renderer.ColorRGBA;
 import com.jmex.bui.BButton;
 import com.jmex.bui.BContainer;
 import com.jmex.bui.BImage;
 import com.jmex.bui.BLabel;
 import com.jmex.bui.BScrollPane;
 import com.jmex.bui.background.TintedBackground;
 import com.jmex.bui.border.LineBorder;
 import com.jmex.bui.event.ActionEvent;
 import com.jmex.bui.event.ActionListener;
 import com.jmex.bui.event.MouseAdapter;
 import com.jmex.bui.event.MouseEvent;
 import com.jmex.bui.icon.ImageIcon;
 import com.jmex.bui.layout.BorderLayout;
 import com.jmex.bui.layout.GroupLayout;
 import edu.gatech.statics.exercise.DiagramType;
 import edu.gatech.statics.math.Quantified;
 import edu.gatech.statics.math.Quantity;
 import edu.gatech.statics.modes.equation.EquationDiagram;
 import edu.gatech.statics.modes.equation.EquationMode;
 import edu.gatech.statics.modes.equation.actions.LockEquation;
 import edu.gatech.statics.modes.equation.worksheet.EquationMath;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.Load;
 import edu.gatech.statics.objects.SimulationObject;
 import edu.gatech.statics.objects.bodies.TwoForceMember;
 import edu.gatech.statics.ui.InterfaceRoot;
 import edu.gatech.statics.ui.applicationbar.ApplicationModePanel;
 import edu.gatech.statics.ui.applicationbar.ApplicationTab;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class EquationModePanel extends ApplicationModePanel<EquationDiagram> {
 
     public static final String panelName = "equation";
     private Map<EquationMath, EquationUIData> uiMap = new HashMap<EquationMath, EquationModePanel.EquationUIData>();
     private BContainer equationBarContainer;
     private BContainer equationButtonContainer;
     private BContainer solutionContainer;
     private BScrollPane equationScrollPane;
     private EquationBar activeEquation;
     //private BButton momentSelectButton;
     private static final ColorRGBA regularBackgroundColor = ColorRGBA.black;
     private static final ColorRGBA regularBorderColor = ColorRGBA.black;
     private static final ColorRGBA activeBackgroundColor = ColorRGBA.darkGray;
     private static final ColorRGBA activeBorderColor = ColorRGBA.white;
 
     public void onClick(Load load) {
         if (activeEquation == null || load == null) {
             return;
         }
         // add the term if the equation is not locked
         if (!activeEquation.isLocked()) {
             activeEquation.performAddTerm(load.getAnchoredVector());
         }
 
         // highlight the load in the equations
         for (EquationUIData data : uiMap.values()) {
             data.equationBar.highlightVector(load == null ? null : load.getAnchoredVector());
         }
     }
 
     public void onHover(Load load) {
         for (EquationUIData data : uiMap.values()) {
             data.equationBar.highlightVector(load == null ? null : load.getAnchoredVector());
         }
     }
 
     /**
      * Called when the diagram state changes. This causes the EquationBars to refresh
      */
     @Override
     public void stateChanged() {
         super.stateChanged();
         for (EquationUIData data : uiMap.values()) {
             data.equationBar.stateChanged();
         }
     }
 
     public EquationBar getActiveEquation() {
         return activeEquation;
     }
 
     private void setActiveEquation(EquationBar bar) {
         if (activeEquation != null) {
             activeEquation.setBackground(new TintedBackground(regularBackgroundColor));
             activeEquation.setBorder(new LineBorder(regularBorderColor));
         }
         this.activeEquation = bar;
         activeEquation.setBackground(new TintedBackground(activeBackgroundColor));
         activeEquation.setBorder(new LineBorder(activeBorderColor));
     }
 
     public EquationModePanel() {
         super();
 
         BContainer fullEquationContainer = new BContainer(new BorderLayout());
         add(fullEquationContainer, BorderLayout.CENTER);
 
         GroupLayout equationLayout = GroupLayout.makeVert(GroupLayout.CENTER);
         equationLayout.setOffAxisJustification(GroupLayout.LEFT);
         equationBarContainer = new BContainer(equationLayout);
 
         equationScrollPane = new BScrollPane(equationBarContainer, false, true);
         equationScrollPane.setShowScrollbarAlways(false);
         fullEquationContainer.add(equationScrollPane, BorderLayout.CENTER);
 
 
         equationButtonContainer = new BContainer(GroupLayout.makeVert(GroupLayout.CENTER));
         fullEquationContainer.add(equationButtonContainer, BorderLayout.WEST);
 
         // the solution container is to the right of the mode panel, and 
         // will contain the solution to the equations.
         GroupLayout solutionLayout = GroupLayout.makeVert(GroupLayout.CENTER);
         solutionLayout.setOffAxisJustification(GroupLayout.LEFT);
         solutionContainer = new BContainer(solutionLayout);
 
         add(solutionContainer, BorderLayout.EAST);
         solutionContainer.setPreferredSize(200, -1);
     }
 
     void refreshRows() {
         equationScrollPane.layout();
     }
 
     private void addEquationRow(EquationMath math) {
 
         final EquationUIData data = new EquationUIData();
         data.equationBar = new EquationBar(math, this);
 
         data.checkButton = new BButton("check", new ActionListener() {
 
             public void actionPerformed(ActionEvent event) {
                 check(data.equationBar);
             }
         }, "check");
         data.checkButton.setStyleClass("smallcircle_button");
 
         data.equationBar.addListener(new MouseAdapter() {
 
             @Override
             public void mousePressed(MouseEvent event) {
                 setActiveEquation(data.equationBar);
             }
         });
 
         equationBarContainer.add(data.equationBar);
         equationButtonContainer.add(data.checkButton);
 
         uiMap.put(math, data);
 
         if (math.isLocked()) {
             data.equationBar.setLocked();
             setCheckIcon(data.equationBar);
         }
 
         if(getDiagram().getBodySubset().getBodies().size() == 1) {
             for (Body b : getDiagram().getBodySubset().getBodies()) {
                 if (b instanceof TwoForceMember) {
 //                    getDiagram().equationSolved();
                     
                     LockEquation lockEquationAction = new LockEquation(math.getName(), true);
                     getDiagram().performAction(lockEquationAction);
                     
                     data.equationBar.setLocked();
                     setCheckIcon(data.equationBar);
                 }
             }
         }
         
         if (uiMap.size() == 1) {
             setActiveEquation(data.equationBar);
         }
     }
 
     private void setCheckIcon(EquationBar bar) {
 
         EquationUIData data = uiMap.get(bar.getMath());
         data.checkButton.setEnabled(false);
 
         ImageIcon icon = null;
         try {
             icon = new ImageIcon(new BImage(getClass().getClassLoader().getResource("rsrc/checkmark.png")));
         } catch (IOException e) {
         }
 
         data.checkButton.setIcon(icon);
         data.checkButton.setText("");
     }
 
     private void check(EquationBar bar) {
         boolean success = bar.getMath().check();
         if (success) {
             getDiagram().equationSolved();
 
             bar.setLocked();
 
             setCheckIcon(bar);
             //InterfaceRoot.getInstance().setAdvice("yay, it worked!");
 
             //boolean allSolved = true;
             int numberSolved = 0;
             for (EquationMath math : uiMap.keySet()) {
                 if (math.isLocked()) {
                     //allSolved = false;
                     numberSolved++;
                 }
             }
 
             if (numberSolved >= getNumberUnknowns()) {
                 //if (allSolved) {
                 performSolve();
             }
         }
     }
 
     /**
      * find the number of unknowns for use in determining whether the user
      * has solved enough equations
      * @return
      */
     private int getNumberUnknowns() {
         EquationDiagram diagram = (EquationDiagram) getDiagram();
         List<String> symbols = new ArrayList<String>();
         for (SimulationObject obj : diagram.allObjects()) {
             Quantified q = null;
 
             if (obj instanceof Load) {
                 q = ((Load) obj).getVector();
             } else if (obj instanceof Quantified) {
                 q = (Quantified) obj;
             }
 
             if (q != null && q.isSymbol() && !q.isKnown()) {
                 String symbol = q.getSymbolName();
                 if (!symbols.contains(symbol)) {
                     symbols.add(symbol);
                 }
             }
         }
         return symbols.size();
     }
 
     private void performSolve() {
         EquationDiagram diagram = (EquationDiagram) getDiagram();
         boolean firstTime = !diagram.getCurrentState().isLocked();
         //!diagram.getWorksheet().isSolved();
         Logger.getLogger("Statics").info("Attempting to solve the equations...");
         Map<Quantity, Float> solution = diagram.getWorksheet().solve();
         if (solution != null) {
 
             //this might be broken now because it can get in on non first time
             //should the equation set had been initially unsolvable
             if (firstTime) {
                 // this is our first time solving the system.
                 diagram.performSolve(solution);
             }
 
             solutionContainer.removeAll();
             for (Map.Entry<Quantity, Float> entry : solution.entrySet()) {
                 Quantity q = entry.getKey();
                 q = new Quantity(q);
                 q.setDiagramValue(BigDecimal.valueOf(entry.getValue()));
 
                 BLabel entryLabel = new BLabel(
                         "@=b(@=#ff0000(" + q.getSymbolName() + ")" +
                         " = " + q.getDiagramValue() + //entry.getValue() + 
                         " " + q.getUnit().getSuffix() + ")");
                 solutionContainer.add(entryLabel);
             }
 
         } else {
             boolean allSolved = true;
             for (EquationMath math : uiMap.keySet()) {
                 if (!math.isLocked()) {
                     allSolved = false;
                 }
             }
             if (allSolved) {
                 InterfaceRoot.getInstance().setAdvice("Your system is not solvable!");
             }
         }
     }
 
     private class EquationUIData {
 
         public EquationBar equationBar;
         public BButton checkButton;
     }
 
     @Override
     protected ApplicationTab createTab() {
         return new ApplicationTab("Solve");
     }
 
     private void clear() {
         for (EquationUIData eqdata : uiMap.values()) {
             eqdata.equationBar.clear();
         }
 
         equationBarContainer.removeAll();
         equationButtonContainer.removeAll();
         solutionContainer.removeAll();
         uiMap.clear();
     }
 
     @Override
     public void activate() {
         super.activate();
 
         if (!uiMap.isEmpty()) {
             clear();
         }
 
         EquationDiagram diagram = (EquationDiagram) getDiagram();
        
        if (diagram.getBodySubset().getSpecialName() != null) {
            getTitleLabel().setText("My Diagram: " + diagram.getBodySubset().getSpecialName());
        } else {
            getTitleLabel().setText("My Diagram: " + diagram.getBodySubset());
        }
 
         for (String mathName : diagram.getWorksheet().getEquationNames()) {
             EquationMath math = diagram.getWorksheet().getMath(mathName);
             addEquationRow(math);
         }
 
         stateChanged();
         performSolve();
 
         refreshRows();
         invalidate();
     }
 
     @Override
     public DiagramType getDiagramType() {
         return EquationMode.instance.getDiagramType();
     }
 }
