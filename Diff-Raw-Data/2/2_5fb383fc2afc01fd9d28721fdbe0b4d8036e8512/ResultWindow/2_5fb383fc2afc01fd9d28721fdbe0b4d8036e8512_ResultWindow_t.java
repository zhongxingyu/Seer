 package ua.kharkov.kture.ot.view.swing.additional.result;
 
 import com.google.common.collect.Sets;
 import com.google.inject.Inject;
 import ua.kharkov.kture.ot.common.localization.MessageBundle;
 import ua.kharkov.kture.ot.common.math.Probability;
 import ua.kharkov.kture.ot.shared.OptimizerCriterionKeeper;
 import ua.kharkov.kture.ot.shared.simpleobjects.ComponentDTO;
 import ua.kharkov.kture.ot.shared.simpleobjects.VariantDTO;
 import ua.kharkov.kture.ot.view.declaration.additionalwindows.optimization.MinimizationOptimizationCriteria;
 import ua.kharkov.kture.ot.view.declaration.additionalwindows.optimization.OptimizerController;
 import ua.kharkov.kture.ot.view.swing.additional.AbstractAdditionalWindow;
 import ua.kharkov.kture.ot.view.swing.additional.componetedit.ProbabilityRenderer;
 
 import javax.inject.Named;
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.awt.*;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 public class ResultWindow extends AbstractAdditionalWindow {
 
     @Inject
     @Named("optimization")
     MessageBundle bundle;
     @Inject
     private OptimizerController optimizerController;
     @Inject
     @Named("resultWindow")
     MessageBundle tartgetBundle;
     @Inject
     @Named("coordinate")
     Map<String, MinimizationOptimizationCriteria> coordinate;
     @Inject
     OptimizerCriterionKeeper сriterionKeeper;
     @Inject
     Collection<ComponentDTO> components;
 
     private String risk = "unknown";
     private String cost = "unknown";
     private String avgCost = "unknown";
     private String optCriteriaValue = "unknown";
 
     private JTable top = new JTable();
     private JTable table = new JTable();
 
     public ResultWindow() {
         top.setEnabled(false);
         table.setEnabled(false);
 
         setContentPane(new JPanel(new BorderLayout()));
 
         getContentPane().setLayout(new BorderLayout());
         JPanel topPanel = new JPanel();
         topPanel.setSize(400, 200);
         top.setSize(400, 200);
         topPanel.add(top);
 
         getContentPane().add(topPanel, BorderLayout.PAGE_START);
 
         JPanel tablePanel = new JPanel(new BorderLayout());
         tablePanel.setBackground(Color.red);
         tablePanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
         tablePanel.add(table);
         tablePanel.setSize(400, 200);
         table.setSize(400, 200);
         table.setDefaultRenderer(Probability.class, new ProbabilityRenderer());
         getContentPane().add(tablePanel, BorderLayout.PAGE_END);
     }
 
     @Override
     public boolean isValidToDisplay() {
         return isComponentsValid();
     }
 
     @Override
     protected void go() {
         OptimizerController.SystemDescriber bestSystem = optimizerController.bestBy(сriterionKeeper.get());
         setCost(Double.toString(optimizerController.valueByCriteria(bestSystem, coordinate.get(bundle.getMessage("cost"))).doubleValue()));
         setRisk(
                 Double.toString(
                         ((Probability)
                                 optimizerController.valueByCriteria(bestSystem, coordinate.get(bundle.getMessage("crashProbability")))
                         ).inScientificForm()
                 ));
         //setAvgCost(optimizerController.valueByCriteria(bestSystem, coordinate.get(bundle.getMessage("averageCost"))).toString());
         setOptCriteriaValue(optimizerController.valueByCriteria(bestSystem, coordinate.get(bundle.getMessage("defaultCriterion"))).toString());
         setComponents(Sets.newHashSet(components));
 
         update();
         pack();
         setVisible(true);
     }
 
     public void update() {
         DefaultTableModel topModel = getNewTopTableModel();
         topModel.addRow(new String[]{tartgetBundle.getMessage("label.probability"), risk});
        topModel.addRow(new String[]{tartgetBundle.getMessage("label.systemCost"), cost});
         top.setModel(topModel);
         top.getColumnModel().getColumn(0).setMinWidth(200);
 
         DefaultTableModel tableModel = getNewTableModel();
         for (ComponentDTO dto : components) {
             tableModel.addRow(new Object[]{
                     dto.getTitle(),
                     dto.getBaseVariant().getCrashProbability(),
                     dto.getBaseVariant().getCost().getDollars() + ", y.e."
             });
         }
         table.setModel(tableModel);
     }
 
     private static DefaultTableModel getNewTopTableModel() {
         return new DefaultTableModel(new String[]{"field", "value"}, 0);
     }
 
     private DefaultTableModel getNewTableModel() {
         DefaultTableModel tableModel = new DefaultTableModel(new String[]{
                 tartgetBundle.getMessage("label.component"),
                 tartgetBundle.getMessage("label.probability"),
                 tartgetBundle.getMessage("label.cost")
         }, 0) {
             @Override
             public Class<?> getColumnClass(int columnIndex) {
                 //TODO: extremly bad solution
                 switch (columnIndex) {
                     case 0:
                         return String.class;
                     case 1:
                         return Probability.class;
                     case 2:
                         return String.class;
                     default:
                         throw new IllegalStateException();
                 }
             }
         };
         return tableModel;
     }
 
 
     private boolean isComponentsValid() {
         if (components.isEmpty()) {
             return false;
         }
         for (ComponentDTO c : components) {
             if (c.getVariants().isEmpty()) {
                 return false;
             }
             for (VariantDTO each : c.getVariants()) {
                 if (each.getCrashProbability() == null || each.getCost() == null) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     private void showErrorMessage() {
         JOptionPane.showMessageDialog(
                 this,
                 bundle.getMessage("error.componentsNotValid"),
                 bundle.getMessage("error.componentsNotValidTitle"),
                 JOptionPane.WARNING_MESSAGE);
     }
 
     public String getRisk() {
         return risk;
     }
 
     public void setRisk(String risk) {
 
         this.risk = risk;
     }
 
     public String getCost() {
         return cost;
     }
 
     public void setCost(String cost) {
         this.cost = cost + ", y.e.";
     }
 
     public String getAvgCost() {
         return avgCost;
     }
 
     public void setAvgCost(String avgCost) {
         this.avgCost = avgCost + " $";
     }
 
     public String getOptCriteriaValue() {
         return optCriteriaValue;
     }
 
     public void setOptCriteriaValue(String optCriteriaValue) {
         this.optCriteriaValue = optCriteriaValue;
     }
 
     public void setComponents(Set<ComponentDTO> components) {
         this.components = components;
     }
 }
