 package cz.cvut.indepmod.uc.frames.toolChooser;
 
 import com.jgoodies.forms.builder.PanelBuilder;
 import com.jgoodies.forms.factories.Borders;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 import cz.cvut.promod.services.ModelerSession;
 import cz.cvut.promod.services.componentFactoryService.ComponentFactoryService;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * UseCase plugin - SI2/3 school project
  * User: Alena Varkockova
  * User: Viktor Bohuslav Bohdal
  *
  * The View component of the ToolChooser dockable frame.
  */
 public class ToolChooserView extends JPanel {
 
     protected final JToggleButton addControlFlowLineButton = ModelerSession.getComponentFactoryService().createToggleButton(
             ToolChooserModel.CONTROL_FLOW_LINE_TOOL_RES, null
     );
 
     protected final JToggleButton controlButton = ModelerSession.getComponentFactoryService().createToggleButton(
             ToolChooserModel.CONTROL_TOOL_RES, null
     );
 
     protected final JToggleButton addIncludeFlowLineButton = ModelerSession.getComponentFactoryService().createToggleButton(
             ToolChooserModel.INCLUDE_FLOW_LINE_TOOL_RES, null
     );
 
     protected final JToggleButton addActorButton = ModelerSession.getComponentFactoryService().createToggleButton(
             ToolChooserModel.ACTOR_TOOL_RES, null
     );
 
     protected final JToggleButton addUseCaseButton = ModelerSession.getComponentFactoryService().createToggleButton(
             ToolChooserModel.USE_CASE_TOOL_RES, null
     );
 
     protected final JToggleButton addSystemBorderButton = ModelerSession.getComponentFactoryService().createToggleButton(
             ToolChooserModel.SYSTEM_BORDER_TOOL_RES, null
     );
 
     protected final JToggleButton addScenarioButton = ModelerSession.getComponentFactoryService().createToggleButton(
             ToolChooserModel.SCENARIO_TOOL_RES, null
     );
 
     protected final JToggleButton addSelectMssButton = ModelerSession.getComponentFactoryService().createToggleButton(
             ToolChooserModel.SELECT_MSS_TOOL_RES, null
     );
 
     protected final JToggleButton addStepButton = ModelerSession.getComponentFactoryService().createToggleButton(
             ToolChooserModel.STEP_TOOL_RES, null
     );
 
     protected final JToggleButton addIncludeUcButton = ModelerSession.getComponentFactoryService().createToggleButton(
             ToolChooserModel.INCLUDE_UC_TOOL_RES, null
     );
 
     public ToolChooserView(){
         //outlookTabbedPane = new ToolChooserOutlookTabs();
 
         initLayout();       
     }
 
     private PanelBuilder builderUC;
     private PanelBuilder builderDetail;
 
     private void initLayout() {
         setBorder(Borders.createEmptyBorder(ComponentFactoryService.DEFAULT_FORM_BORDER));
 
         final FormLayout layout = new FormLayout(
                 /* columns */ "pref:grow",
                 /* rows */ "pref, 3dlu, pref, 10dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, 5dlu, pref"
         );
 
         builderUC = new PanelBuilder(layout);
         builderDetail = new PanelBuilder(layout);
         final CellConstraints cellConstraints = new CellConstraints();
 
         builderUC.add(controlButton, cellConstraints.xy(1,1));
         builderUC.add(addActorButton, cellConstraints.xy(1,5));
         builderUC.add(addUseCaseButton, cellConstraints.xy(1,7));
         builderUC.add(addSystemBorderButton, cellConstraints.xy(1,9));
         builderDetail.add(addScenarioButton, cellConstraints.xy(1,1));
         builderDetail.add(addSelectMssButton, cellConstraints.xy(1,3));
         builderDetail.add(addStepButton, cellConstraints.xy(1,5));
         builderDetail.add(addIncludeUcButton, cellConstraints.xy(1,7));
         builderUC.add(new JLabel("   "), cellConstraints.xy(1,12));
         builderUC.add(addControlFlowLineButton, cellConstraints.xy(1,13));
         builderUC.add(addIncludeFlowLineButton, cellConstraints.xy(1,15));
         
         setLayout(new BorderLayout());
         add(builderUC.getPanel(), BorderLayout.NORTH);
     }
 
     public void ChangePanel(String panelName)
     {
         removeAll();
         setLayout(new BorderLayout());
         if(panelName.compareTo("UC") == 0)
         {
             add(builderUC.getPanel(), BorderLayout.NORTH);
         }
         else
         {
             add(builderDetail.getPanel(), BorderLayout.NORTH);
         }
         //this.requestFocus();
        updateUI();
        //repaint();
     }
 
     public JToggleButton getAddControlFlowLineButton() {
         return addControlFlowLineButton;
     }
 
     public JToggleButton getAddIncludeFlowLineButton() {
         return addIncludeFlowLineButton;
     }
 
     public JToggleButton getAddUseCaseButton() {
         return addUseCaseButton;
     }
 
     public JToggleButton getAddActorButton() {
         return addActorButton;
     }
 
     public JToggleButton getAddScenarioButton() {
         return addScenarioButton;
     }
 
     public JToggleButton getAddSelectMssButton() {
         return addSelectMssButton;
     }
 
     public JToggleButton getAddStepButton() {
         return addStepButton;
     }
 
     public JToggleButton getAddIncludeUcButton() {
         return addIncludeUcButton;
     }
 
     public JToggleButton getAddSystemBorderButton() {
         return addSystemBorderButton;
     }
 }
