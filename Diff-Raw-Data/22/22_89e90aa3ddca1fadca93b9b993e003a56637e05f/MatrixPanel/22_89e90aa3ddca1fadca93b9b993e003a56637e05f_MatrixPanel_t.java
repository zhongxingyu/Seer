 package edu.cudenver.bios.powercalculator.client.panels;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DeckPanel;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.NamedNodeMap;
 import com.google.gwt.xml.client.Node;
 import com.google.gwt.xml.client.NodeList;
 
 import edu.cudenver.bios.powercalculator.client.PowerCalculatorConstants;
 import edu.cudenver.bios.powercalculator.client.PowerCalculatorGUI;
 import edu.cudenver.bios.powercalculator.client.listener.AlphaListener;
 import edu.cudenver.bios.powercalculator.client.listener.InputWizardStepListener;
 import edu.cudenver.bios.powercalculator.client.listener.MatrixResizeListener;
 import edu.cudenver.bios.powercalculator.client.listener.MetaDataListener;
 
 public class MatrixPanel extends Composite
 implements AlphaListener
 {
     private static final String STYLE = "matrixPanel";
 	// these default names derived from linear model theory.
 	// ensures that default matrix dimensions conform properly
 	private static final int DEFAULT_N = 3;
 	private static final int DEFAULT_Q = 3;
 	private static final int DEFAULT_P = 2;
 	private static final int DEFAULT_A = 2;
 	private static final int DEFAULT_B = 1;
 
 	// indices for the sigma panels for fixed vs. random predictors
 	private static final int FIXED_INDEX = 0;
 	private static final int COVARIATE_INDEX = 1;
 
 	// matrix headers
 	protected SubpanelHeader essenceHeader = new SubpanelHeader(PowerCalculatorGUI.constants.matrixDesignEssence(), 
 	        PowerCalculatorGUI.constants.matrixDesignEssenceDetails());
 	protected SubpanelHeader withinContrastHeader = new SubpanelHeader(PowerCalculatorGUI.constants.matrixWithinSubjectContrast(),
 	        PowerCalculatorGUI.constants.matrixWithinSubjectContrastDetails());
 	protected SubpanelHeader betweenContrastHeader = new SubpanelHeader(PowerCalculatorGUI.constants.matrixBetweenSubjectContrast(), 
             PowerCalculatorGUI.constants.matrixBetweenSubjectContrastDetails());
 	protected SubpanelHeader betaHeader = new SubpanelHeader(PowerCalculatorGUI.constants.matrixBeta(), 
             PowerCalculatorGUI.constants.matrixBetaDetails());
 	protected SubpanelHeader thetaNullHeader = new SubpanelHeader(PowerCalculatorGUI.constants.matrixThetaNull(), 
             PowerCalculatorGUI.constants.matrixThetaNullDetails());
 	protected SubpanelHeader sigmaErrorHeader = new SubpanelHeader(PowerCalculatorGUI.constants.matrixSigmaError(), 
             PowerCalculatorGUI.constants.matrixSigmaErrorDetails());
 	protected SubpanelHeader sigmaCovariateHeader = new SubpanelHeader(PowerCalculatorGUI.constants.matrixSigmaG(), 
             PowerCalculatorGUI.constants.matrixSigmaGDetails());
 	protected SubpanelHeader sigmaOutcomesHeader = new SubpanelHeader(PowerCalculatorGUI.constants.matrixSigmaY(), 
             PowerCalculatorGUI.constants.matrixSigmaYDetails());
 	protected SubpanelHeader sigmaCovariateOutcomeHeader = new SubpanelHeader(PowerCalculatorGUI.constants.matrixSigmaYG(), 
             PowerCalculatorGUI.constants.matrixSigmaYGDetails());
 	
 	// matrix inputs
 	protected EssenceMatrix essence = new EssenceMatrix("design", DEFAULT_N, DEFAULT_Q);
 	protected ResizableMatrix withinContrast = new ResizableMatrix("withinSubjectContrast", DEFAULT_P, DEFAULT_B);
 	protected ResizableMatrix betweenContrast = new ResizableMatrix("betweenSubjectContrast", DEFAULT_A, DEFAULT_Q);
 	protected ResizableMatrix beta = new ResizableMatrix("beta", DEFAULT_Q, DEFAULT_P);
 	protected ResizableMatrix thetaNull = new ResizableMatrix("theta", DEFAULT_A, DEFAULT_B);
 	// variance/covariance matrix for fixed predictors
 	protected ResizableMatrix sigmaError = new ResizableMatrix("sigmaError", DEFAULT_P, DEFAULT_P);
 	/* the following are needed for a baseline covariate */
 	// variance of the baseline covariate
 	protected ResizableMatrix sigmaCovariate = new ResizableMatrix("sigmaGaussianRandom", 1, 1);
 	// variance/covariance of the outcomes
 	protected ResizableMatrix sigmaOutcomes = new ResizableMatrix("sigmaOutcome", DEFAULT_P, DEFAULT_P);
 	// correlation of covariate and outcomes
 	protected ResizableMatrix sigmaCovariateOutcome = new ResizableMatrix("sigmaOutcomeGaussianRandom", DEFAULT_P, DEFAULT_P);
 
 	protected DeckPanel sigmaDeck = new DeckPanel();
 	protected FormPanel form = new FormPanel("_blank");
 	protected Hidden matrixXML = new Hidden("data");
 
 	protected AlphaPanel alphaPanel = new AlphaPanel();
 	protected int covariateColumn = -1;
 	
     // build the advanced options panel
     AdvancedLinearModelOptionsPanel advOpts = new AdvancedLinearModelOptionsPanel();
 
     // tell wizard this step is complete
     InputWizardStepListener wizard;
     int stepIndex;
     
 	public MatrixPanel(InputWizardStepListener w, int idx)
 	{
 	    this.wizard = w;
 	    this.stepIndex = idx;
 	    
 		VerticalPanel panel = new VerticalPanel();
 
 		panel.add(alphaPanel);
		alphaPanel.addAlphaListener(this);
 		panel.add(advOpts);
 		// add each header / matrix to the panel
 		panel.add(essenceHeader);
 		panel.add(essence);
 		panel.add(betaHeader);
 		panel.add(beta);
 		panel.add(sigmaDeck);
 		panel.add(thetaNullHeader);
 		panel.add(thetaNull);
 		panel.add(betweenContrastHeader);
 		panel.add(betweenContrast);
 		panel.add(withinContrastHeader);
 		panel.add(withinContrast);
 		// deck panel since we have different variance/covariance matrices
 		// for all fixed predictors vs. a baseline covariate 
 	    VerticalPanel covariateSigma = new VerticalPanel();
 	    covariateSigma.add(sigmaCovariateOutcomeHeader);
 	    covariateSigma.add(sigmaCovariateOutcome);
 	    covariateSigma.add(sigmaCovariateHeader);
 	    covariateSigma.add(sigmaCovariate);
 	    covariateSigma.add(sigmaOutcomesHeader);
 	    covariateSigma.add(sigmaOutcomes);
 	    VerticalPanel fixedOnlySigma = new VerticalPanel();
 	    fixedOnlySigma.add(sigmaErrorHeader);
 	    fixedOnlySigma.add(sigmaError);
 	    sigmaDeck.add(fixedOnlySigma);
 	    sigmaDeck.add(covariateSigma);
 	    sigmaDeck.showWidget(FIXED_INDEX);
 		panel.add(sigmaDeck);
 		
 		// set matrix size restrictions
         sigmaError.setIsSquare(true, true);
         sigmaCovariate.setIsSquare(true, true);
         sigmaOutcomes.setIsSquare(true, true);
 		
 		// add listeners to ensure matrix conformance when each 
 		// matrix changes
 		essence.addMatrixResizeListener(new MatrixResizeListener() {
 			public void onRows(int rows) 
 			{
 			    if (betweenContrast.getRowDimension() >= rows)
 			    {
 			        betweenContrast.setRowDimension(rows-1);
 			        thetaNull.setRowDimension(rows-1);
 			    }
 			}
 			public void onColumns(int cols)
 			{
 				beta.setRowDimension(cols);
 				betweenContrast.setColumnDimension(cols);
 			}
 		});
 		essence.addMetaDataListener(new MetaDataListener() {
 			public void onCovariate(boolean hasCovariate)
 			{
 				if (hasCovariate)
 					sigmaDeck.showWidget(COVARIATE_INDEX);
 				else
 					sigmaDeck.showWidget(FIXED_INDEX);
 				// update the B, C matrices with covariate entries
 				beta.setCovariateRow(hasCovariate);
 				betweenContrast.setColumnDimension(beta.getRowDimension());
 			}
 			
 			public void onMinimumSampleSize(int minN) {}
 		});
 		beta.addMatrixResizeListener(new MatrixResizeListener() {
 		    public void onRows(int rows)
 		    {
 		        essence.setColumnDimension(rows);
 		        betweenContrast.setColumnDimension(rows);
 		    }
 		    public void onColumns(int cols)
 		    {
 		        withinContrast.setRowDimension(cols);
 		        sigmaError.setRowDimension(cols);
 		        sigmaOutcomes.setRowDimension(cols);
 		        sigmaCovariateOutcome.setRowDimension(cols);
 		    }
 		});
 		betweenContrast.addMatrixResizeListener(new MatrixResizeListener() {
 			public void onRows(int rows)
 			{
 			    if (rows >= essence.getRowDimension())
 			    {
 			        essence.setRowDimension(rows+1);
 			    }
 				thetaNull.setRowDimension(rows);
 			}
 			public void onColumns(int cols)
 			{
 				beta.setRowDimension(cols);
 				essence.setColumnDimension(cols);
 			}
 		});
 		withinContrast.addMatrixResizeListener(new MatrixResizeListener() {
 			public void onRows(int rows)
 			{
 				beta.setColumnDimension(rows);
                 sigmaError.setRowDimension(rows);
                 sigmaOutcomes.setRowDimension(rows);
                 sigmaCovariateOutcome.setRowDimension(rows);
 			}
 			public void onColumns(int cols)
 			{
                 thetaNull.setColumnDimension(cols);
 			}
 		});
         thetaNull.addMatrixResizeListener(new MatrixResizeListener() {
             public void onRows(int rows)
             {
                 betweenContrast.setRowDimension(rows);
                 if (rows >= essence.getRowDimension())
                 {
                     essence.setRowDimension(rows+1);
                 }
             }
             public void onColumns(int cols)
             {
                 withinContrast.setColumnDimension(cols);
             }
         });
 		// make sure sigma and within subject contrast (U) conform
 		sigmaError.addMatrixResizeListener(new MatrixResizeListener() {
 			public void onRows(int rows)
 			{
 				withinContrast.setColumnDimension(rows);
                 sigmaOutcomes.setRowDimension(rows);
                 sigmaCovariateOutcome.setRowDimension(rows);
                 beta.setColumnDimension(rows);
 			}
 			public void onColumns(int cols) 
 			{
 			    withinContrast.setColumnDimension(cols);
 			    sigmaOutcomes.setRowDimension(cols);
 			    sigmaCovariateOutcome.setRowDimension(cols);
 			    beta.setColumnDimension(cols);
 			}
 		});
 		sigmaCovariateOutcome.addMatrixResizeListener(new MatrixResizeListener() {
             public void onRows(int rows)
             {
                 withinContrast.setColumnDimension(rows);
                 sigmaOutcomes.setRowDimension(rows);
                 sigmaError.setRowDimension(rows);
                 beta.setColumnDimension(rows);
             }
             public void onColumns(int cols) 
             {
                 withinContrast.setColumnDimension(cols);
                 sigmaOutcomes.setRowDimension(cols);
                 sigmaError.setRowDimension(cols);
                 beta.setColumnDimension(cols);
             }
         });
 		sigmaOutcomes.addMatrixResizeListener(new MatrixResizeListener() {
             public void onRows(int rows)
             {
                 withinContrast.setColumnDimension(rows);
                 sigmaError.setRowDimension(rows);
                 sigmaCovariateOutcome.setRowDimension(rows);
                 beta.setColumnDimension(rows);
             }
             public void onColumns(int cols) 
             {
                 withinContrast.setColumnDimension(cols);
                 sigmaError.setRowDimension(cols);
                 sigmaCovariateOutcome.setRowDimension(cols);
                 beta.setColumnDimension(cols);
             }
         });
 		
 		
 		// set style
 		panel.setStyleName(STYLE);
 		
 		// initialize the widget
 		initWidget(panel);
 	}
 	
 	public String getAlpha()
 	{
 	    return alphaPanel.getAlpha();
 	}
 	
 	public boolean validate()
 	{
 		return true;
 	}
 	
 	public String getStudyAttributes()
 	{
         StringBuffer buffer = new StringBuffer();
         buffer.append("alpha='" + alphaPanel.getAlpha() + "' ");
         buffer.append(advOpts.getStudyAttributes());
         return buffer.toString();
 	}
 	
 	public String getStudyXML(int totalN)
 	{
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(essence.toXML(totalN));
 		buffer.append(beta.toXML());
 		buffer.append(thetaNull.toXML());
 		buffer.append(withinContrast.toXML());
 		buffer.append(betweenContrast.toXML());
 		if (covariateColumn == -1)
 		{
 		    buffer.append(sigmaError.toXML());
 		}
 		else
 		{
 		    buffer.append(sigmaCovariate.toXML());
             buffer.append(sigmaOutcomes.toXML());
             buffer.append(sigmaCovariateOutcome.toXML());
 		}
 		return buffer.toString();
 	}
 	
 	public void addEssenceMatrixResizeListener(MatrixResizeListener listener)
 	{
 	    essence.addMatrixResizeListener(listener);
 	}
 	
 	public void addEssenceMatrixMetaDataListener(MetaDataListener listener)
 	{
 		essence.addMetaDataListener(listener);
 	}
 	
 	public void loadFromXMLDocument(Document doc)
 	{
     	Node studyNode = doc.getElementsByTagName("study").item(0);
     	if (studyNode != null)
     	{
     		Node alpha = studyNode.getAttributes().getNamedItem("alpha");
     		if (alpha != null) 	
     		{
     		    alphaPanel.setAlpha(alpha.getNodeValue());
     		}
 
     		// parse the essence matrix
     		Node essenceNode = doc.getElementsByTagName("essencematrix").item(0);
     		if (essenceNode != null) essence.loadFromDomNode(essenceNode);
     		
     		// parse the remaining matrices
     		NodeList matrixNodes = doc.getElementsByTagName("matrix");
     		for(int i = 0; i < matrixNodes.getLength(); i++)
     		{
     			Node matrixNode = matrixNodes.item(i);
     			NamedNodeMap attrs = matrixNode.getAttributes();
     			Node nameNode = attrs.getNamedItem("name");
     			if (nameNode != null)
     			{
     				String name = nameNode.getNodeValue();
     				if (name.equals("beta"))
     					beta.loadFromDomNode(matrixNode);
     				else if (name.equals("withinSubjectContrast"))
     					withinContrast.loadFromDomNode(matrixNode);
    				else if (name.equals("betweenSubjectContrast"))
    					betweenContrast.loadFromDomNode(matrixNode);
     				else if (name.equals("theta"))
     					thetaNull.loadFromDomNode(matrixNode);
     				else if (name.equals("sigmaError"))
     					sigmaError.loadFromDomNode(matrixNode);
     				else if (name.equals("sigmaGaussianRandom"))
     					sigmaCovariate.loadFromDomNode(matrixNode);
     				else if (name.equals("sigmaOutcome"))
     					sigmaOutcomes.loadFromDomNode(matrixNode);
     				else if (name.equals("sigmaOutcomeGaussianRandom"))
     					sigmaCovariateOutcome.loadFromDomNode(matrixNode);
     			}
     		}
     	}
 	}
     
     public void reset()
     {
         // clear the alpha level
         alphaPanel.reset();
         // clear the matrices
         essence.reset(DEFAULT_N, DEFAULT_Q);
         withinContrast.reset(DEFAULT_P, DEFAULT_B);
         betweenContrast.reset(DEFAULT_A, DEFAULT_Q);
         beta.reset(DEFAULT_Q, DEFAULT_P);
         thetaNull.reset(DEFAULT_A, DEFAULT_B);
         sigmaError.reset(DEFAULT_P, DEFAULT_P);
         sigmaCovariate.reset(1, 1);
         sigmaOutcomes.reset(DEFAULT_P, DEFAULT_P);
         sigmaCovariateOutcome.reset(DEFAULT_P, DEFAULT_P);
     }
     
     public void onAlpha(double alpha)
     {
         wizard.onStepComplete(stepIndex);
     }
     
     public void onAlphaInvalid()
     {
         wizard.onStepInProgress(stepIndex);
 
     }
 }
