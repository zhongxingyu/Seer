 package org.hackystat.projectbrowser.page.projectportfolio.detailspanel;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.hackystat.projectbrowser.page.projectportfolio.detailspanel.chart.MiniBarChart;
 import org.hackystat.projectbrowser.page.projectportfolio.detailspanel.chart.StreamClassifier;
 import org.hackystat.projectbrowser.page.projectportfolio.detailspanel.chart.PortfolioCategory;
 import org.hackystat.projectbrowser.page.projectportfolio.jaxb.Measures.Measure;
 
 /**
  * Configuration for project portfolio measures.
  * 
  * @author Shaoxuan Zhang
  * 
  */
 public class PortfolioMeasureConfiguration implements Serializable {
   /** Support serialization. */
   private static final long serialVersionUID = -6799287509742157998L;
   
   /** The parameter separator. */
   private static final String PARAMETER_SEPARATOR = ",";
 
   /** The name of the measure. */
   private String measureName;
   /** The alias of this measure, which will be used as display name. */
   private String alias;
   /** If this measure is enabled. */
   private boolean enabled = true;
   /** The method to merge multiple streams. */
   private String merge;
 
   /** The data model this measure belongs to. */
   private final ProjectPortfolioDataModel dataModel;
 
   /** This meausre's parameter list. */
   //private List<String> parameters = new ArrayList<String>();
   /** The parameters for telemetry analysis. */
   private final List<IModel> parameters = new ArrayList<IModel>();
   
   /** The stream classifier.*/
   private StreamClassifier streamClassifier;
 
   /**
    * Create an instance.
    * 
    * @param name The name of the measure.
    * @param alias The alias of this measure, which will be used as display name.
    * @param merge The method to merge multiple streams. Can be sum, avg, min or max.
    * @param enabled If this measure is enabled.
    * @param streamClassifier the {@link StreamClassifier} to use, null means no coloring.
    * @param dataModel The data model this measure belongs to.
    */
   public PortfolioMeasureConfiguration(String name, String alias, String merge, boolean enabled,
       StreamClassifier streamClassifier, ProjectPortfolioDataModel dataModel) {
     this.measureName = name;
     this.alias = alias;
     this.merge = merge;
     this.enabled = enabled;
     this.streamClassifier = streamClassifier;
     this.dataModel = dataModel;
   }
   
   /**
    * Load configuration from a PortfolioMeasure object.
    * @param enabled If this measure is enabled.
    * @param streamClassifier The stream classifier.
    * @param telemetryParameters The parameters for telemetry analysis.
    */
   public void setMeasureConfiguration(boolean enabled, StreamClassifier streamClassifier,
       String telemetryParameters) {
     this.setEnabled(enabled);
     this.streamClassifier = streamClassifier;
     this.parameters.clear();
     if (telemetryParameters != null) {
       for (String parameter : telemetryParameters.split(PARAMETER_SEPARATOR)) {
         this.parameters.add(new Model(parameter));
       }
     }
   }
 
   /**
    * @return the measureName
    */
   public String getName() {
     return measureName;
   }
 
   /**
    * @param enabled the enable to set
    */
   public void setEnabled(boolean enabled) {
     this.enabled = enabled;
   }
 
   /**
    * @return the enable
    */
   public boolean isEnabled() {
     return enabled;
   }
 
   /**
    * Return the color associated with the given category.
    * @param category the {@link PortfolioCategory}.
    * @return Color in String.
    */
   public String getColor(PortfolioCategory category) {
     switch (category) {
       case GOOD: return getDataModel().getGoodColor();
       case AVERAGE: return getDataModel().getAverageColor();
       case POOR: return getDataModel().getPoorColor();
       case NA: return getDataModel().getNAColor();
       default: return getDataModel().getFontColor();
     }
   }
   
   /**
    * Return the color of the given MiniBarChart.
    * @param chart the MiniBarChart
    * @return the color in String
    */
   public String getChartColor(MiniBarChart chart) {
     if (hasClassifier()) {
       return getColor(this.streamClassifier.getStreamCategory(chart));
     }
     else {
       return getDataModel().getFontColor();
     }
   }
   
   /**
    * Return the color according to the value.
    * The color method is defined in this measure configuration.
    * @param value the value.
    * @return the color in String
    */
   public String getValueColor(double value) {
     if (hasClassifier()) {
       return getColor(this.streamClassifier.getValueCategory(value));
     }
     else {
       return getDataModel().getFontColor();
     }
   }
   
   /**
    * Return the color for average value or unstable trend.
    * @return the color
    */
   public String getAverageColor() {
     return dataModel.getAverageColor();
   }
 
   /**
    * Return the parameters in a single String.
    * 
    * @return parameters separated by ',' in a single String.
    */
   public String getParamtersString() {
     if (this.parameters.isEmpty()) {
       return "";
     }
     StringBuffer param = new StringBuffer();
     for (int i = 0; i < this.parameters.size(); ++i) {
       IModel model = this.parameters.get(i);
       if (model != null) {
         param.append(model.getObject());
         if (i < this.parameters.size() - 1) {
           param.append(PARAMETER_SEPARATOR);
         }
       }
     }
     return param.toString();
   }
 
   /**
    * @return the parameters
    */
   public List<IModel> getParameters() {
     return this.parameters;
   }
 
   /**
    * @return the dataModel
    */
   public ProjectPortfolioDataModel getDataModel() {
     return dataModel;
   }
 
   /**
    * Set the stream classifier according to the given classifier name.
    * If the colorMethod is null or is equals to the name of {@link streamClassifier}, 
    * nothing will happen.
    * Otherwise, it will create a new stream classifier with information saved in UriCache.
    * @param classifierName the String represents the classifier.
    */
   public void setStreamClassifier(String classifierName) {
     if (classifierName == null || (this.hasClassifier() && 
         classifierName.equalsIgnoreCase(this.streamClassifier.getName()))) {
       return;
     }
     Measure measure = dataModel.getSavedMeasure(this);
    if (measure == null) {
      measure = new Measure();
    }
     measure.setClassifierMethod(classifierName);
     this.setStreamClassifier(ProjectPortfolioDataModel.getClassifier(measure));
   }
   
   /**
    * @param streamClassifier the streamTrendClassifier to set
    */
   public void setStreamClassifier(StreamClassifier streamClassifier) {
     this.streamClassifier = streamClassifier;
   }
 
   /**
    * @return the streamTrendClassifier
    */
   public StreamClassifier getStreamClassifier() {
     return streamClassifier;
   }
 
   /**
    * @param alias the alias to set
    */
   public void setAlias(String alias) {
     this.alias = alias;
   }
 
   /**
    * @return the alias
    */
   public String getAlias() {
     return alias;
   }
 
   /**
    * Check if the {@link alias} field is available.
    * @return true if {@link alias} field is available.
    */
   public boolean hasAlias() {
     return alias != null && alias.length() > 0;
   }
   
   /**
    * Return the display name of this measure.
    * If alias is available, alias will be return. Otherwise, measure name will be return.
    * @return the display name.
    */
   public String getDisplayName() {
     if (this.alias != null && this.alias.length() > 0) {
       return this.getAlias();
     }
     return this.getName();
   }
 
   /**
    * @param merge the merge to set
    */
   public void setMerge(String merge) {
     this.merge = merge;
   }
 
   /**
    * @return the merge
    */
   public String getMerge() {
     return merge;
   }
   
   /**
    * Check if the {@link merge} field is available.
    * @return true if {@link merge} field is available.
    */
   public boolean hasMerge() {
     return merge != null && merge.length() > 0;
   }
 
   /**
    * Return the configuration panel of the stream classifer.
    * @param id the wicket id.
    * @return the panel.
    */
   public Panel getConfigurationPanel(String id) {
     if (hasClassifier()) {
       return this.streamClassifier.getConfigurationPanel(id);
     }
     Panel panel = new Panel(id);
     panel.setVisible(false);
     return panel;
   }
 
   /**
    * @return the name of the {@link streamClassifier}.
    */
   public String getClassiferName() {
     if (!hasClassifier()) {
       return "None";
     }
     return this.streamClassifier.getName();
   }
 
   /**
    * Save classifier's setting into the given {@link Measure} instance.
    * @param measure the given {@link Measure} instance
    */
   public void saveClassifierSetting(Measure measure) {
     if (hasClassifier()) {
       this.streamClassifier.saveSetting(measure);
     }
   }
 
   /**
    * Check if this instance has the stream classifier.
    * @return true if it has.
    */
   public boolean hasClassifier() {
     return this.streamClassifier != null;
   }
 
 }
