 package edu.cudenver.bios.powersvc.resource;
 
 import java.util.ArrayList;
 
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.RealMatrix;
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import edu.cudenver.bios.matrix.ColumnMetaData;
 import edu.cudenver.bios.matrix.EssenceMatrix;
 import edu.cudenver.bios.matrix.RowMetaData;
 import edu.cudenver.bios.matrix.ColumnMetaData.PredictorType;
 import edu.cudenver.bios.powersamplesize.parameters.LinearModelPowerSampleSizeParameters;
 import edu.cudenver.bios.powersamplesize.parameters.PowerSampleSizeParameters;
 import edu.cudenver.bios.powersamplesize.parameters.SimplePowerSampleSizeParameters;
 import edu.cudenver.bios.powersamplesize.parameters.LinearModelPowerSampleSizeParameters.PowerMethod;
 import edu.cudenver.bios.powersamplesize.parameters.LinearModelPowerSampleSizeParameters.TestStatistic;
 import edu.cudenver.bios.powersvc.application.PowerConstants;
 import edu.cudenver.bios.powersvc.application.PowerLogger;
 
 /**
  * Parsing of model parameters from DOM tree.
  * 
  * @author kreidles
  *
  */
 public class ParameterResourceHelper
 {
     
     public static PowerSampleSizeParameters powerSampleSizeParametersFromDomNode(String modelName, Node node)
     throws ResourceException
     {
         if (PowerConstants.TEST_ONE_SAMPLE_STUDENT_T.equals(modelName))
         {
             return ParameterResourceHelper.simpleParamsFromDomNode(node);
         } 
         else if (PowerConstants.TEST_GLMM.equals(modelName))
         {
             return ParameterResourceHelper.linearModelParamsFromDomNode(node);
         }
         else
         {
             PowerLogger.getInstance().warn("Invalid model name found while parsing parameters: " + modelName);
             return null;
         }
     }
     
     /**
      * Parse simple inputs for power/sample size calculations from a DOM tree
      * 
      * @param node
      * @return SimplePowerSampleSizeParameters object
      * @throws ResourceException
      */
     public static SimplePowerSampleSizeParameters simpleParamsFromDomNode(Node node) throws ResourceException
     {
         SimplePowerSampleSizeParameters powerParams = new SimplePowerSampleSizeParameters();
         
         // make sure the root node is a power parameters
         if (!PowerConstants.TAG_PARAMS.equals(node.getNodeName()))
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid root node '" + node.getNodeName() + "' when parsing parameter object");
 
         // get the parameter attributes
         NamedNodeMap attrs = node.getAttributes();
         if (attrs != null)
         {
             /* parse required parameters: mu0, muA, sigma, alpha, sample size */
             Node mu0 = attrs.getNamedItem(PowerConstants.ATTR_MU0);
             if (mu0 != null) powerParams.setMu0(Double.parseDouble(mu0.getNodeValue()));
 
             Node muA = attrs.getNamedItem(PowerConstants.ATTR_MUA);
             if (muA != null) powerParams.setMuA(Double.parseDouble(muA.getNodeValue()));
             
             Node sigma = attrs.getNamedItem(PowerConstants.ATTR_SIGMA_ERROR);
             if (sigma != null) powerParams.setSigma(Double.parseDouble(sigma.getNodeValue()));
             
             Node alpha = attrs.getNamedItem(PowerConstants.ATTR_ALPHA);
             if (alpha != null) powerParams.setAlpha(Double.parseDouble(alpha.getNodeValue()));
             
             Node sampleSize = attrs.getNamedItem(PowerConstants.ATTR_SAMPLESIZE);
             if (sampleSize != null) powerParams.setSampleSize(Integer.parseInt(sampleSize.getNodeValue()));
 
             Node power = attrs.getNamedItem(PowerConstants.ATTR_POWER);
             if (power != null) powerParams.setPower(Double.parseDouble(power.getNodeValue()));
             
             // parse optional parameters: oneTailed, simulate, simulationSize
             Node oneTailed = attrs.getNamedItem(PowerConstants.ATTR_ONE_TAILED);
             if (oneTailed != null) powerParams.setOneTailed(Boolean.parseBoolean(oneTailed.getNodeValue()));
         }
 
         return powerParams;
     }
     
     /**
      * Get linear model parameters from a DOM tree
      * 
      * @param node
      * @return
      * @throws ResourceException
      */
     public static LinearModelPowerSampleSizeParameters linearModelParamsFromDomNode(Node node) throws ResourceException
     {
         LinearModelPowerSampleSizeParameters params = new LinearModelPowerSampleSizeParameters();
 
         // make sure the root node is a power parameters
         if (!node.getNodeName().equals(PowerConstants.TAG_PARAMS))
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid root node '" + node.getNodeName() + "' when parsing parameter object");
 
         // get the parameter attributes
         NamedNodeMap attrs = node.getAttributes();
         if (attrs != null)
         {
             /* parse required attributes: alpha */
             Node alpha = attrs.getNamedItem(PowerConstants.ATTR_ALPHA);
             if (alpha != null) params.setAlpha(Double.parseDouble(alpha.getNodeValue()));
             
             // TODO: extract this from design or essence matrix.  It's silly to specify
             Node sampleSize = attrs.getNamedItem(PowerConstants.ATTR_SAMPLESIZE);
             if (sampleSize != null) params.setSampleSize(Integer.parseInt(sampleSize.getNodeValue()));
             
             // parse desired power
             Node power = attrs.getNamedItem(PowerConstants.ATTR_POWER);
             if (power != null) params.setPower(Double.parseDouble(power.getNodeValue()));
             
             /* parse the power method */
             Node powerMethod = attrs.getNamedItem(PowerConstants.ATTR_POWER_METHOD);
             if (powerMethod != null)
             {
                 String powerMethodName = powerMethod.getNodeValue();
                 if (powerMethodName != null)
                 {
                     if (PowerConstants.POWER_METHOD_QUANTILE.equals(powerMethodName))
                         params.setPowerMethod(PowerMethod.QUANTILE_POWER);
                     else if (PowerConstants.POWER_METHOD_UNCONDITIONAL.equals(powerMethodName))
                         params.setPowerMethod(PowerMethod.UNCONDITIONAL_POWER);
                     else
                         params.setPowerMethod(PowerMethod.CONDITIONAL_POWER);
                 }
             }
             
             /* parse the quantile (if using quantile power) */
             Node quantile = attrs.getNamedItem(PowerConstants.ATTR_QUANTILE);
             if (quantile != null) params.setQuantile(Double.parseDouble(quantile.getNodeValue()));
             
             /* parse the test statistic */
             Node stat = attrs.getNamedItem(PowerConstants.ATTR_STATISTIC);
             if (stat != null) 
             {
                 String statName = stat.getNodeValue();
                 if (statName != null)
                 {
                     if (PowerConstants.STATISTIC_UNIREP.equals(statName))
                         params.setTestStatistic(TestStatistic.UNIREP);
                     else if (PowerConstants.STATISTIC_HOTELLING_LAWLEY_TRACE.equals(statName))
                         params.setTestStatistic(TestStatistic.HOTELLING_LAWLEY_TRACE);
                     else if (PowerConstants.STATISTIC_PILLAU_BARTLETT_TRACE.equals(statName))
                        params.setTestStatistic(TestStatistic.PILLAI_BARTLETT_TRACE);
                     else if (PowerConstants.STATISTIC_WILKS_LAMBDA.equals(statName))
                         params.setTestStatistic(TestStatistic.WILKS_LAMBDA);
                     else
                     {
                         PowerLogger.getInstance().warn("Invalid statistic name '" + statName + "', defaulting to Hotelling-Lawley Trace");
                     }
                 }
             }
         }
 
         // parse the matrix inputs: beta, design, theta, sigma, C-contrast, U-contrast
         NodeList children = node.getChildNodes();
         if (children != null && children.getLength() > 0)
         {
             for (int i = 0; i < children.getLength(); i++)
             {
                 Node child = children.item(i);
                 if (PowerConstants.TAG_ESSENCE_MATRIX.equals(child.getNodeName()))
                 {
                     EssenceMatrix essence = essenceMatrixFromDomNode(child);
                     params.setDesignEssence(essence);
                 }
                 else if (PowerConstants.TAG_MATRIX.equals(child.getNodeName()))
                 {
                     // get the name of this matrix
                     String matrixName = null;
                     NamedNodeMap matrixAttrs = child.getAttributes();
                     Node name = matrixAttrs.getNamedItem(PowerConstants.ATTR_NAME);
                     if (name != null) matrixName = name.getNodeValue();
                     
                     // if we have a valid name, parse and save the matrix to the linear model parameters
                     if (matrixName != null && !matrixName.isEmpty())
                     {
                         RealMatrix matrix = matrixFromDomNode(child);
 
                         if (PowerConstants.MATRIX_TYPE_BETA.equals(matrixName))
                             params.setBeta(matrix);
                         else if (PowerConstants.MATRIX_TYPE_DESIGN.equals(matrixName))
                             params.setDesign(matrix);
                         else if (PowerConstants.MATRIX_TYPE_THETA.equals(matrixName))
                             params.setTheta(matrix);
                         else if (PowerConstants.MATRIX_TYPE_WITHIN_CONTRAST.equals(matrixName))
                             params.setWithinSubjectContrast(matrix);
                         else if (PowerConstants.MATRIX_TYPE_BETWEEN_CONTRAST.equals(matrixName))
                             params.setBetweenSubjectContrast(matrix);
                         else if (PowerConstants.MATRIX_TYPE_SIGMA_ERROR.equals(matrixName))
                             params.setSigmaError(matrix);
                         else if (PowerConstants.MATRIX_TYPE_SIGMA_GAUSSIAN.equals(matrixName))
                             params.setSigmaGaussianRandom(matrix);
                         else if (PowerConstants.MATRIX_TYPE_SIGMA_OUTCOME.equals(matrixName))
                             params.setSigmaOutcome(matrix);
                         else if (PowerConstants.MATRIX_TYPE_SIGMA_OUTCOME_GAUSSIAN.equals(matrixName))
                             params.setSigmaOutcomeGaussianRandom(matrix);
                         else
                             PowerLogger.getInstance().warn("Ignoring Invalid matrix: " + matrixName);                    
                     }
                     else
                     {
                         PowerLogger.getInstance().warn("Ignoring unnamed matrix");
                     }
 
                 }
                 else 
                 {
                     PowerLogger.getInstance().warn("Ignoring unknown tag while parsing parameters: " + child.getNodeName());
                 }
             }
         }
         return params;
     }  
     
     /**
      * Parse an essence matrix from a DOM tree
      * 
      * @param node root node of the DOM tree
      * @return
      * @throws IllegalArgumentException
      */
     public static EssenceMatrix essenceMatrixFromDomNode(Node node)
     throws ResourceException
     {
         EssenceMatrix essence = null;
         RealMatrix matrix = null;
         RowMetaData[] rmd = null;
         ColumnMetaData[] cmd = null;
         Node seed = null;
         
         // parse the random seed value if specified
         NamedNodeMap attrs = node.getAttributes();
         if (attrs != null) seed = attrs.getNamedItem(PowerConstants.ATTR_RANDOM_SEED);
         
         // parse the matrix data, row meta data, and column meta data
         NodeList children = node.getChildNodes();
         if (children != null && children.getLength() > 0)
         {
             for (int i = 0; i < children.getLength(); i++)
             {
                 Node child = children.item(i);
                 if (PowerConstants.TAG_MATRIX.equals(child.getNodeName()))
                 {
                     matrix = ParameterResourceHelper.matrixFromDomNode(child);
                 }
                 else if (PowerConstants.TAG_ROW_META_DATA.equals(child.getNodeName()))
                 {
                     rmd = ParameterResourceHelper.rowMetaDataFromDomNode(child);
                 }
                 else if (PowerConstants.TAG_COLUMN_META_DATA.equals(child.getNodeName()))
                 {
                     cmd = ParameterResourceHelper.columnMetaDataFromDomNode(child);
                 }
                 else
                 {
                     PowerLogger.getInstance().warn("Ignoring unknown essence matrix child tag: " + child.getNodeName());
                 }
             }
         }
         
         // now that we're done parsing, build the essence matrix object
         if (matrix != null)
         {
             essence = new EssenceMatrix(matrix);
             if (seed != null) essence.setRandomSeed(Integer.parseInt(seed.getNodeValue()));
             if (cmd != null) essence.setColumnMetaData(cmd);
             if (rmd != null) essence.setRowMetaData(rmd);
         }        
         return essence;
     }
     
     /**
      * Parse a row meta data object from a DOM node
      * 
      * @param node
      * @return array of RowMetaData objects
      */
     public static RowMetaData[] rowMetaDataFromDomNode(Node node)
     {
         ArrayList<RowMetaData> metaDataList = new ArrayList<RowMetaData>();
         
         NodeList children = node.getChildNodes();
         if (children != null && children.getLength() > 0)
         {
             for (int i = 0; i < children.getLength(); i++)
             {
                 RowMetaData rmd = new RowMetaData();
                 Node child = children.item(i);
                 if (PowerConstants.TAG_ROW.equals(child.getNodeName()))
                 {
                     NamedNodeMap attrs = child.getAttributes();
                     Node reps = attrs.getNamedItem(PowerConstants.ATTR_REPETITIONS);
                     if (reps != null) rmd.setRepetitions(Integer.parseInt(reps.getNodeValue()));
                     
                     Node ratio = attrs.getNamedItem(PowerConstants.ATTR_RATIO);
                     if (ratio != null) rmd.setRatio(Integer.parseInt(ratio.getNodeValue()));
                 }
                 else
                 {
                     PowerLogger.getInstance().warn("Ignoring unknown tag while parsing row meta data: " + child.getNodeName());
                 }
                 metaDataList.add(rmd);
             }
         }
         
         return (RowMetaData[]) metaDataList.toArray(new RowMetaData[metaDataList.size()]);
     }
 
     /**
      * Parse an array of column meta data from a DOM tree
      * 
      * @param node 
      * @return list of column meta data
      */
     public static ColumnMetaData[] columnMetaDataFromDomNode(Node node)
     {
         ArrayList<ColumnMetaData> metaDataList = new ArrayList<ColumnMetaData>();
         
         NodeList children = node.getChildNodes();
         if (children != null && children.getLength() > 0)
         {
             for (int i = 0; i < children.getLength(); i++)
             {
                 ColumnMetaData cmd = new ColumnMetaData();
                 Node child = children.item(i);
                 if (PowerConstants.TAG_COLUMN.equals(child.getNodeName()))
                 {
                     NamedNodeMap attrs = child.getAttributes();
                     Node predictorType = attrs.getNamedItem(PowerConstants.ATTR_TYPE);
                     if (predictorType != null)
                     {
                         // default is fixed, so only set if we see a random predictor specified
                         if (PowerConstants.COLUMN_TYPE_RANDOM.equals(predictorType.getNodeValue()))
                         {
                             cmd.setPredictorType(PredictorType.RANDOM);
                         }
                     }
                     
                     Node mean = attrs.getNamedItem(PowerConstants.ATTR_MEAN);
                     if (mean != null) cmd.setMean(Double.parseDouble(mean.getNodeValue()));
                     
                     Node variance = attrs.getNamedItem(PowerConstants.ATTR_VARIANCE);
                     if (variance != null) cmd.setVariance(Double.parseDouble(variance.getNodeValue()));                   
                     
                     metaDataList.add(cmd);
                 }
                 else
                 {
                     PowerLogger.getInstance().warn("Ignoring unknown tag while parsing row meta data: " + child.getNodeName());
                 }
             }
         }
         
         return (ColumnMetaData[]) metaDataList.toArray(new ColumnMetaData[metaDataList.size()]);
     }
     
     /**
      * Parse a matrix from XML DOM.  The matrix should be specified as follows:
      * <p>
      * &lt;matrix type=(beta|theta|sigma|design|withinSubjectContrast|betweenSubjectContrast) &gt;
      * <br>&lt;row&gt;&lt;col&gt;number&lt;col/&gt;...&lt;/row&gt;
      * <br>...
      * <br>&lt;/matrix&gt;
      * 
      * @param node
      * @return
      * @throws ResourceException
      */
     public static RealMatrix matrixFromDomNode(Node node) throws ResourceException
     {        
         // make sure the root node is a matrix
         if (!node.getNodeName().equals(PowerConstants.TAG_MATRIX))
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid root node '" + node.getNodeName() + "' when parsing matrix object");
         
         // parse the rows / columns from the attribute list
         NamedNodeMap attrs = node.getAttributes();
         Node numRowsStr = attrs.getNamedItem(PowerConstants.ATTR_ROWS);
         int numRows = 0;
         if (numRowsStr != null) numRows = Integer.parseInt(numRowsStr.getNodeValue());
 
         Node numColsStr = attrs.getNamedItem(PowerConstants.ATTR_COLUMNS);
         int numCols = 0;
         if (numColsStr != null) numCols = Integer.parseInt(numColsStr.getNodeValue());
         
         // make sure we got a reasonable value for rows/columns
         if (numRows <= 0 || numCols <=0)
             throw new IllegalArgumentException("Invalid matrix rows/columns specified - must be positive integer");
             
         // create a placeholder matrix for storing the rows/columns
         Array2DRowRealMatrix matrix = new Array2DRowRealMatrix(numRows, numCols);
         
         // parse the children: should contain multiple row objects with col objects as children
         NodeList rows = node.getChildNodes();
         if (rows != null && rows.getLength() > 0)
         {
             for (int rowIndex = 0; rowIndex < rows.getLength() && rowIndex < numRows; rowIndex++)
             {
                 Node row = rows.item(rowIndex);
                 if (!PowerConstants.TAG_ROW.equals(row.getNodeName()))
                     throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid row node '" + row.getNodeName() + "' when parsing matrix object");
 
                 // get all of the columns for the current row and insert into a matrix
                 NodeList columns = row.getChildNodes();
                 if (columns != null && columns.getLength() > 0)
                 {
                     for(int colIndex = 0; colIndex < columns.getLength() && colIndex < numCols; colIndex++)
                     {
                         Node colEntry = columns.item(colIndex);
                         String valStr = colEntry.getFirstChild().getNodeValue();
                         if (colEntry.hasChildNodes() && valStr != null && !valStr.isEmpty())
                         {
                             double val = Double.parseDouble(valStr);
                             matrix.setEntry(rowIndex, colIndex, val);
                         }
                         else
                         {
                             throw new IllegalArgumentException("Missing data in matrix [row=" + rowIndex + " col=" + colIndex + "]");
                         }
                     }
                 }
             }
             
         }
         return matrix;
     }
     
 }
