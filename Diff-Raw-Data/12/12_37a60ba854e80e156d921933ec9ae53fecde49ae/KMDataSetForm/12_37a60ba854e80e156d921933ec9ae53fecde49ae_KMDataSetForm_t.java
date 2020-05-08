 package gov.nih.nci.nautilus.ui.struts.form;
 
 import gov.nih.nci.nautilus.constants.NautilusConstants;
 import gov.nih.nci.nautilus.query.GeneExpressionQuery;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionForm;
 import org.jfree.data.XYSeriesCollection;
 
 import de.laures.cewolf.DatasetProduceException;
 import de.laures.cewolf.DatasetProducer;
 
 public class KMDataSetForm extends ActionForm implements DatasetProducer,
 		Serializable {
 
    private String pValue;
 	private GeneExpressionQuery geneQuery;
 
 	private String method;
 
 	private String geneOrCytoband;
 
 	private String plotType;
 
 	private int upFold = 2;
 
 	private int downFold = 2;
 
 	private XYSeriesCollection censorDataset;
 
 	private XYSeriesCollection lineDataset;
 
 	private String selectedReporter;
 
 	private List reporters = new ArrayList();;
 
 	private String chartTitle = "";//NautilusConstants.GENE_EXP_KMPLOT;
 
 	private String changeType = "";
 
 	private String upOrAmplified = "";
 
 	private String downOrDeleted = "";
 
 	private ArrayList folds = new ArrayList();
 
 	private static Logger logger = Logger.getLogger(KMDataSetForm.class);
 
 	public KMDataSetForm() {
 
 	}
 
 	public KMDataSetForm(String _plotType) {
 		if(_plotType != null &&
 				( _plotType.equals(NautilusConstants.GENE_EXP_KMPLOT) ||
 				_plotType.equals(NautilusConstants.COPY_NUMBER_KMPLOT))){
 		this.plotType = _plotType;
 		}
 	}
 
 	/**
 	 * This method is called by the ceWolf chart tag to create the data for the
 	 * plot.
 	 */
 
 	public Object produceDataset(Map params) throws DatasetProduceException {
 		HashMap hashMap = (HashMap) params;
 		if (((Boolean) (hashMap.get("censusPlot"))).booleanValue()) {
 			return censorDataset;
 		} else {
 			return lineDataset;
 		}
 	}
 
 	/***************************************************************************
 	 *  
 	 */
 	public boolean hasExpired(Map params, Date since) {
 		return (System.currentTimeMillis() - since.getTime()) > 5000;
 	}
 
 	public String getProducerId() {
 		return "KMDataSetForm";
 	}
 
 	/**
 	 * @return Returns the geneOrCytoband.
 	 */
 	public String getGeneOrCytoband() {
 		return geneOrCytoband;
 	}
 
 	/**
 	 * @param geneOrCytoband
 	 *            The geneOrCytoband to set.
 	 */
 	public void setGeneOrCytoband(String geneSymbol) {
 		this.geneOrCytoband = geneSymbol;
 	}
 
 	/**
 	 * @return Returns the geneFolds.
 	 */
 	public int getUpFold() {
 		return upFold;
 	}
 
 	/**
 	 * @param geneFolds
 	 *            The geneFolds to set.
 	 */
 	public void setUpFold(int upFolds) {
 		this.upFold = upFolds;
 	}
 
 	/**
 	 * @return Returns the method.
 	 */
 	public String getMethod() {
 		return method;
 	}
 
 	/**
 	 * @param method
 	 *            The method to set.
 	 */
 	public void setMethod(String method) {
 		this.method = method;
 	}
 
 	/**
 	 * @return Returns the folds.
 	 */
 	public ArrayList getFolds() {
 		if (folds.isEmpty()) {
 			for (int i = 2; i < 11; i++) {
 				folds.add(new Integer(i));
 			}
 		}
 		return folds;
 	}
 
 	/**
 	 * @return Returns the downFold.
 	 */
 	public int getDownFold() {
 		return downFold;
 	}
 
 	/**
 	 * @param downFold
 	 *            The downFold to set.
 	 */
 	public void setDownFold(int downFold) {
 		this.downFold = downFold;
 	}
 
 	/**
 	 * @return Returns the chartTitle.
 	 */
 	public String getChartTitle() {
 		if (getPlotType().equals(NautilusConstants.GENE_EXP_KMPLOT)) {
 			chartTitle = "Kaplan-Meier Survival Plot for Samples with Differential "
 					+ geneOrCytoband + " Gene Expression";
 		}
 		if (getPlotType().equals(NautilusConstants.COPY_NUMBER_KMPLOT)) {
 			chartTitle = "Kaplan-Meier Survival Plot for Samples with Copy Number Analysis for "
 					+ geneOrCytoband;
 		}
 		return chartTitle;
 	}
 
 	/**
 	 * @param censorDataset
 	 *            The censorDataset to set.
 	 */
 	public void setCensorDataset(XYSeriesCollection censorDataset) {
 		this.censorDataset = censorDataset;
 	}
 
 	/**
 	 * @param lineDataset
 	 *            The lineDataset to set.
 	 */
 	public void setLineDataset(XYSeriesCollection lineDataset) {
 		this.lineDataset = lineDataset;
 	}
 
 	/**
 	 * @return Returns the reporters.
 	 */
 	public List getReporters() {
 		return reporters;
 	}
 
 	/**
 	 * @param reporters
 	 *            The reporters to set.
 	 */
 	public void setReporters(List reporters) {
 		this.reporters = reporters;
 	}
 
 	/**
 	 * @return Returns the selectedReporter.
 	 */
 	public String getSelectedReporter() {
 		return selectedReporter;
 	}
 
 	/**
 	 * @param selectedReporter
 	 *            The selectedReporter to set.
 	 */
 	public void setSelectedReporter(String selectedReporter) {
 		this.selectedReporter = selectedReporter;
 	}
 
 	/**
 	 * @return Returns the plotType.
 	 */
 	public String getPlotType() {
 		return plotType;
 	}
 
 	/**
 	 * @param plotType
 	 *            The plotType to set.
 	 */
 	public void setPlotType(String plotType) {
 		this.plotType = plotType;
 	}
 
 	/**
 	 * @return Returns the changeType.
 	 */
 	public String getChangeType() {
 		if (getPlotType().equals(NautilusConstants.GENE_EXP_KMPLOT)) {
 			changeType = "Folds";
 		}
 		if (getPlotType().equals(NautilusConstants.COPY_NUMBER_KMPLOT)) {
 			changeType = "Copies";
 		}
 		return changeType;
 	}
 
 	/**
 	 * @return Returns the downOrDeleted.
 	 */
 	public String getDownOrDeleted() {
 		if (getPlotType().equals(NautilusConstants.GENE_EXP_KMPLOT)) {
 			downOrDeleted = "Down-Regulated";
 		}
 		if (getPlotType().equals(NautilusConstants.COPY_NUMBER_KMPLOT)) {
 			downOrDeleted = "Deleted";
 		}
 		return downOrDeleted;
 	}
 
 	/**
 	 * @return Returns the upOrAmplified.
 	 */
 	public String getUpOrAmplified() {
 		if (getPlotType().equals(NautilusConstants.GENE_EXP_KMPLOT)) {
 			upOrAmplified = "Up-Regulated";
 		}
 		if (getPlotType().equals(NautilusConstants.COPY_NUMBER_KMPLOT)) {
 			upOrAmplified = "Amplified";
 		}
 		return upOrAmplified;
 	}

    public String getPValue() {
        return pValue;
    }
    

    public void setPValue(String value) {
        pValue = "";
    }
    
 }
