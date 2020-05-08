 package gov.nih.nci.protexpress.ui.actions.experiment.viewedit;
 
 import org.apache.commons.lang.StringUtils;
 
 import gov.nih.nci.protexpress.ProtExpressRegistry;
 import gov.nih.nci.protexpress.domain.experiment.ExperimentRun;
 import gov.nih.nci.protexpress.domain.protocol.InputOutputObject;
 import gov.nih.nci.protexpress.domain.protocol.ProtocolApplication;
 
 /**
  *  Action to handle reload the experiment nav tree content
  */
 public class ExperimentTreeAction extends ExperimentDetailsAction {
 
     private String treeMode;
 
     private Long experimentRunId;
     private ExperimentRun experimentRun;
     private Long protAppId;
     private ProtocolApplication protApp; 
     private Long inputId;
     private InputOutputObject input;
     private Long outputId;
     private InputOutputObject output;
 
     /** 
      * {@inheritDoc}
      */
     @Override
     public void prepare() throws Exception {
         super.prepare();
         if (getExperiment() != null) {
             getExperiment().getName();
         }
         if (getExperimentRunId() != null) {
             setExperimentRun(ProtExpressRegistry.getExperimentService().getExperimentRunById(getExperimentRunId()));
             if (StringUtils.isBlank(getSelectedNodeId())) {
                 setSelectedNodeId(getExperimentRun().getId().toString());
             }
         }
         if (getProtAppId() != null) {
             setProtApp(ProtExpressRegistry.getExperimentService().getProtocolApplicationById(getProtAppId()));
             if (StringUtils.isBlank(getSelectedNodeId())) {
                 setSelectedNodeId(getProtApp().getId().toString());
             }
         }
         if (getInputId() != null) {
             setInput(ProtExpressRegistry.getExperimentService().getInputOutputObjectById(getInputId()));
             if (StringUtils.isBlank(getSelectedNodeId())) {
                 setSelectedNodeId(getInput().getId().toString());
             }
         }
         if (getOutputId() != null) {
             setOutput(ProtExpressRegistry.getExperimentService().getInputOutputObjectById(getOutputId()));
             if (StringUtils.isBlank(getSelectedNodeId())) {
                 setSelectedNodeId(getOutput().getId().toString());
             }
         }
        
        if (getProtApp() != null) {
            if (getExperimentRun() == null) {
                setExperimentRun(getProtApp().getExperimentRun());
            }
            if (getExperiment() == null) {
                setExperiment(getExperimentRun().getExperiment());
            }
         }
     }
     
     /**
      * @return refresh the entire tree
      */
     public String refresh() {
         return "refresh";
     }
     
     /**
      * @return refresh the experiment node
      */
     public String refreshExperiment() {
         return "refreshExperiment";
     }
     
     /**
      * @return refresh the experiment.experimentrun node
      */
     public String refreshExperimentRun() {
         return "refreshExperimentRun";
     }
     
     /**
      * @return refresh the experiment.experimentrun.protocolApplication node
      */
     public String refreshProtocolApplication() {
         return "refreshProtocolApplication";
     }
     
     /**
      * @return refresh the experiment.experimentrun.protocolApplication.input
      */
     public String refreshInput() {
         return "refreshInput";
     }
     
     /**
      * @return refresh the experiment.experimentrun.protocolApplication.output
      */
     public String refreshOutput() {
         return "refreshOutput";
     }
     
     /**
      * @return VIEW/EDIT
      */
     public String getTreeMode() {
         return treeMode;
     }
     
     /**
      * @param treeMode VIEW/EDIT
      */
     public void setTreeMode(String treeMode) {
         this.treeMode = treeMode;
     }
 
     /**
      * @return id
      */
     public Long getExperimentRunId() {
         return experimentRunId;
     }
 
     /**
      * @param experimentRunId id
      */
     public void setExperimentRunId(Long experimentRunId) {
         this.experimentRunId = experimentRunId;
     }
 
     /**
      * @return id
      */
     public Long getProtAppId() {
         return protAppId;
     }
 
     /**
      * @param protAppId id
      */
     public void setProtAppId(Long protAppId) {
         this.protAppId = protAppId;
     }
 
     /**
      * @return id
      */
     public Long getInputId() {
         return inputId;
     }
 
     /**
      * @param inputId id
      */
     public void setInputId(Long inputId) {
         this.inputId = inputId;
     }
 
     /**
      * @return id
      */
     public Long getOutputId() {
         return outputId;
     }
 
     /**
      * @param outputId id
      */
     public void setOutputId(Long outputId) {
         this.outputId = outputId;
     }
 
     /**
      * @return the selected experimentRun 
      */
     public ExperimentRun getExperimentRun() {
         return experimentRun;
     }
 
     /**
      * @param experimentRun selected 
      */
     public void setExperimentRun(ExperimentRun experimentRun) {
         this.experimentRun = experimentRun;
     }
     /**
      * @return selected PA
      */
     public ProtocolApplication getProtApp() {
         return protApp;
     }
 
     /**
      * @param protApp selected PA
      */
     public void setProtApp(ProtocolApplication protApp) {
         this.protApp = protApp;
     }
 
     /**
      * @return selected input
      */
     public InputOutputObject getInput() {
         return input;
     }
 
     /**
      * @param input selected
      */
     public void setInput(InputOutputObject input) {
         this.input = input;
     }
 
     /**
      * @return output selected
      */
     public InputOutputObject getOutput() {
         return output;
     }
 
     /**
      * @param output selected
      */
     public void setOutput(InputOutputObject output) {
         this.output = output;
     }
 
 }
