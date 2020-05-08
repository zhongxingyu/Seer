 /*
  * Power Service for the GLIMMPSE Software System.  Processes
  * incoming HTTP requests for power, sample size, and detectable
  * difference
  * 
  * Copyright (C) 2010 Regents of the University of Colorado.  
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package edu.cudenver.bios.matrixsvc.resource;
 
 import edu.cudenver.bios.matrixsvc.application.MatrixConstants;
 import edu.cudenver.bios.matrixsvc.application.MatrixServiceParameters;
 
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.RealMatrix;
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.util.ArrayList;
 
 /**
  * This is a utility class to parse an XML DOM Node containing the request XML
  * or "Entity Body" in RESTful terminology.
  * @author Jonathan Cohen
  *
  */
 public class MatrixParamParser {
 
 	/**
 	 * @param Node node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getAdditionParamsFromDomNode(Node node)
 	throws ResourceException
 	{
 		return processParametersFromMatrixList(node);
 	}
 	
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getSubtractionParamsFromDomNode(Node node)
 	throws ResourceException
 	{
 		return processParametersFromMatrixList(node);
 	}
 	
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getScalarMultiplicationParamsFromDomNode(Node node)
 	throws ResourceException
 	{
 		return processParametersFromParameterList(node);
 	}
 
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getElementWiseMultiplicationParamsFromDomNode(Node node)
 	throws ResourceException
 	{
 		return processParametersFromMatrixList(node);
 	}
 	
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getMatrixMultiplicationParamsFromDomNode(Node node)
 	throws ResourceException
 	{
		return processParametersFromMatrixList(node);
 	}
 	
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getHorizontalDirectProductParamsFromDomNode(Node node)
 	throws ResourceException
 	{
 		return processParametersFromMatrixList(node);
 	}
 
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getKroneckerProductParamsFromDomNode(Node node)
 	throws ResourceException
 	{
 		return processParametersFromMatrixList(node);
 	}
 	
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getMatrixInversionParamsFromDomNode(Node node)
 	throws ResourceException, IllegalArgumentException
 	{
 		MatrixServiceParameters params = new MatrixServiceParameters();
 		ArrayList<RealMatrix> matrixList = new ArrayList<RealMatrix>();
 		
         matrixList.add( extractMatrixFromDomNode(node) );
         params.setMatrixListFromRequest(matrixList);
         
         return params;
 	}
 	
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getMatrixRankParamsFromDomNode(Node node)
 	throws ResourceException
 	{
 		MatrixServiceParameters params = new MatrixServiceParameters();
 		ArrayList<RealMatrix> matrixList = new ArrayList<RealMatrix>();
 		
         matrixList.add( extractMatrixFromDomNode(node) );
         params.setMatrixListFromRequest(matrixList);
         
         return params;
 	}
 	
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getMatrixTraceParamsFromDomNode(Node node)
 	throws ResourceException
 	{
 		MatrixServiceParameters params = new MatrixServiceParameters();
 		ArrayList<RealMatrix> matrixList = new ArrayList<RealMatrix>();
 		
         matrixList.add( extractMatrixFromDomNode(node) );
         params.setMatrixListFromRequest(matrixList);
         
         return params;
 	}
 	
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getPositiveDefiniteParamsFromDomNode(Node node)
 	throws ResourceException
 	{
 		MatrixServiceParameters params = new MatrixServiceParameters();
 		ArrayList<RealMatrix> matrixList = new ArrayList<RealMatrix>();
 		
         matrixList.add( extractMatrixFromDomNode(node) );
         params.setMatrixListFromRequest(matrixList);
         
         return params;
 	}
 	
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getDecompCholeskyParamsFromDomNode(Node node)
 	throws ResourceException
 	{
 		MatrixServiceParameters params = new MatrixServiceParameters();
 		ArrayList<RealMatrix> matrixList = new ArrayList<RealMatrix>();
 		
         matrixList.add( extractMatrixFromDomNode(node) );
         params.setMatrixListFromRequest(matrixList);
         
         return params;
 	}
 	
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getMatrixVecParamsFromDomNode(Node node)
 	throws ResourceException
 	{
 		MatrixServiceParameters params = new MatrixServiceParameters();
 		ArrayList<RealMatrix> matrixList = new ArrayList<RealMatrix>();
 		
         matrixList.add( extractMatrixFromDomNode(node) );
         params.setMatrixListFromRequest(matrixList);
         
         return params;
 	}
 	
 	/**
 	 * 
 	 * @param node Node
 	 * @return MatrixServiceParameters
 	 * @throws ResourceException
 	 */
 	public static MatrixServiceParameters getMatrixVechParamsFromDomNode(Node node)
 	throws ResourceException
 	{
 		MatrixServiceParameters params = new MatrixServiceParameters();
 		ArrayList<RealMatrix> matrixList = new ArrayList<RealMatrix>();
 		
         matrixList.add( extractMatrixFromDomNode(node) );
         params.setMatrixListFromRequest(matrixList);
         
         return params;
 	}
 	
 	/**
 	 * This is a convenience method to throw a ResourceException if the
 	 * request XML's root node is incorrect.
 	 * @param nodeName is the name that was found in the XML for the root node.
 	 * @param expectedNodeName is the expected root node name for this service.
 	 */
 	private static void notifyClientBadRequest(String nodeName, String expectedNodeName)
 	throws ResourceException
 	{
 		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, 
         		"Invalid node '" + nodeName + 
         		"' when parsing parameter object.  " +
         		"It must be " + expectedNodeName + " for this service.");
 	}
 	
 	/**
      * Parse a matrix from XML DOM.  The matrix should be specified as follows:
      * <p>
      * &lt;matrix name='' rows='' columns='' &gt;
      * <br>&lt;r&gt;&lt;c&gt;number&lt;c/&gt;...&lt;/r&gt;
      * <br>...
      * <br>&lt;/matrix&gt;
      * 
      * @param node
      * @return matrix object
      * @throws ResourceException
      */
     private static RealMatrix extractMatrixFromDomNode(Node node) 
     throws ResourceException,IllegalArgumentException
     {   
     	//make sure we have a matrix
     	if (!MatrixConstants.TAG_MATRIX.equals(node.getNodeName().trim()))
         {
             notifyClientBadRequest(node.getNodeName().trim(), MatrixConstants.TAG_MATRIX);
         }
     	
         // parse the rows / columns from the attribute list
         NamedNodeMap attrs = node.getAttributes();
         Node numRowsStr = attrs.getNamedItem(MatrixConstants.ATTR_ROWS);
         int numRows = 0;
        
        int numCols;
 		try 
 		{
 			if (numRowsStr != null){
 				numRows = Integer.parseInt(numRowsStr.getNodeValue());
 			}
 			else{
 				throw new IllegalArgumentException("Cannot find attribute 'rows' in this matrix.");
 			}
 			Node numColsStr = attrs.getNamedItem(MatrixConstants.ATTR_COLUMNS);
			numCols = 0;
 			if (numColsStr != null){
 				numCols = Integer.parseInt(numColsStr.getNodeValue());
 			}
 			else{
 				throw new IllegalArgumentException("Cannot find attribute 'columns' in this matrix.");
 			}
 		} 
 		catch (NumberFormatException e) 
 		{
 			e.printStackTrace();
 			throw new IllegalArgumentException("'rows' or 'columns' attributes couldn't be converted " +
 					"from a String to a number.");
 		} 
         
         // make sure we got a reasonable value for rows/columns
         if (numRows <= 0 || numCols <=0)
         {
             throw new IllegalArgumentException("Invalid matrix rows/columns " +
             		"specified - must be positive integer");
         }
         
         // create a place holder matrix for storing the rows/columns
         Array2DRowRealMatrix matrix = new Array2DRowRealMatrix(numRows, numCols);
         
         // parse the children: should contain multiple row objects with 
         // column objects as children
         NodeList rows = node.getChildNodes();
         if (rows != null && rows.getLength() > 0)
         {
             for (int rowIndex = 0; rowIndex < rows.getLength() && 
             	rowIndex < numRows; rowIndex++)
             {
                 Node row = rows.item(rowIndex);
                 if (!MatrixConstants.TAG_ROW.equals(row.getNodeName())){
                     notifyClientBadRequest(row.getNodeName(), MatrixConstants.TAG_ROW);
                 }
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
                             double val;
 							try {
 								val = Double.parseDouble(valStr);
 							} catch (NumberFormatException e) {
 								throw new IllegalArgumentException("Caught exception parsing the value of element (" 
 										+ rowIndex + "," + colIndex + ").  This position contained the following: "
 										+ valStr);
 							}
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
     
     /**
      * 
      * @param node
      * @return double representing the scalar multiplier for the 
      * scalar multiplication service.
      * @throws IllegalArgumentException
      */
     private static double extractScalarFromDomNode(Node node)
     throws IllegalArgumentException,ResourceException
     {
     	if( !MatrixConstants.ATTR_VALUE.equals( node.getNodeName().trim() ) )
     	{
     		notifyClientBadRequest(node.getNodeName().trim(), MatrixConstants.ATTR_VALUE);
     	}
     	String valStr = node.getNodeValue();
     	Double val = null;
     	if( valStr != null && !valStr.isEmpty() )
     	{
     		try {
 				val = Double.parseDouble(valStr);
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 				throw new IllegalArgumentException("Scalar multiplier value doesn't " +
 						"parse to a double. Its String value is "+valStr);
 			}
     	}
     	else
     	{
     		throw new IllegalArgumentException("Scalar multiplier is null or empty.");
     	}
     	return val.doubleValue();
     }
     
     /**
      * Since this method expects to operate on a list of matrices, the 
      * list must contain at least one matrix.  If the list is empty, it will
      * throw an exception.
      * @param node containing 
 	 * <p>
 	 * &lt;matrixList&gt;
      * <br>&lt;matrix name="name" rows="number" columns="number" &gt;
      * <br>&lt;r&gt;&lt;c&gt;number&lt;/c&gt;&lt;c&gt;number&lt;/c&gt;...&lt;/r&gt;
      * <br>...
      * <br>&lt;/matrix&gt;
      * <br>&lt;matrix name="name" rows="number" columns="number" &gt;
      * <br>&lt;r&gt;&lt;c&gt;number&lt;/c&gt;&lt;c&gt;number&lt;/c&gt;...&lt;/r&gt;
      * <br>...
      * <br>&lt;/matrix&gt;
 	 * <br>&lt;/matrixList&gt;
      * @return MatrixServiceParameters
      * @throws ResourceException
      */
     private static MatrixServiceParameters processParametersFromMatrixList(Node node)
     throws ResourceException
     {
     	// make sure the root node is a matrixList
         if ( !MatrixConstants.TAG_MATRIX_LIST.equals(node.getNodeName().trim() ) )
         {
             notifyClientBadRequest(node.getNodeName().trim(), MatrixConstants.TAG_MATRIX_LIST);
         }
         
         //initialize our return object and the list of matrices it will contain
     	MatrixServiceParameters params = new MatrixServiceParameters();
     	ArrayList<RealMatrix> matrixList = new ArrayList<RealMatrix>();
     	
     	//iterate over the child nodes (matrices)
     	NodeList nodeList = node.getChildNodes();
     	if( nodeList.getLength() == 0) {
     		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
     		"The matrixList did not contain any matrices.  " +
     		"It must contain at least one.");
     	}
     	for(int i = 0; i < nodeList.getLength(); i++)
     	{
     		//extract the matrix from the DOM, and add to our list
     		matrixList.add( extractMatrixFromDomNode(nodeList.item(i)) );
     	}
     	params.setMatrixListFromRequest(matrixList);
         return params;	
     }
     
     /**
      * 
      * @param node containing 
 	 * <p>
 	 * &lt;parameterList&gt;
      * <br>&lt;matrix name="name" rows="number" columns="number" &gt;
      * <br>&lt;r&gt;&lt;c&gt;number&lt;/c&gt;&lt;c&gt;number&lt;/c&gt;...&lt;/r&gt;
      * <br>...
      * <br>&lt;/matrix&gt;
      * <br>&lt;scalarMultiplier&gt;
      * <br>number
      * <br>&lt;/scalarMultiplier&gt;
      * <br>&lt;/parameterList&gt;
      * @return MatrixServiceParameters
      * @throws ResourceException
      */
     private static MatrixServiceParameters processParametersFromParameterList(Node node)
     throws ResourceException
     {
     	// make sure the root node is parameterList
         if (!MatrixConstants.TAG_PARAMETER_LIST.equals(node.getNodeName().trim()))
         {
             notifyClientBadRequest(node.getNodeName().trim(), MatrixConstants.TAG_PARAMETER_LIST);
         }
         boolean matrixFound = false;
         boolean scalarFound = false;
         
         //initialize our list of matrices, and the return object.
         MatrixServiceParameters params = new MatrixServiceParameters();
         ArrayList<RealMatrix> matrixList = new ArrayList<RealMatrix>();
         Node scalarValue  = null;
         
         //iterate over the child nodes to find the matrix
     	NodeList nodeList = node.getChildNodes();
     	if(nodeList.getLength() < 2)
     	{
     		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
     				"The parameterList doesn't contain proper number of elements - 2." +
     				" It only contains "+ nodeList.getLength() + ".");
     	}
     	for(int i = 0; i < nodeList.getLength(); i++)
     	{
     		//get our matrix
         	Node tmpNode = nodeList.item(i);
         	if( MatrixConstants.TAG_MATRIX.equals(tmpNode.getNodeName().trim()))
         	{
         		//parse the matrix, and add to our matrix list.
             	matrixList.add( extractMatrixFromDomNode(tmpNode) );
             	matrixFound = true;
             }
         	else if( MatrixConstants.TAG_SCALAR_MULTIPLIER.equals(tmpNode.getNodeName().trim()))
         	{
         		//get our scalar
         		NamedNodeMap attrs = tmpNode.getAttributes();
                 scalarValue  = attrs.getNamedItem("value");
                 scalarFound = true;
         	}
     	}
         
     	if(!matrixFound){
     		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
 			"The parameterList doesn't contain a <matrix> element.");
     	}
     	if(!scalarFound){
     		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
 			"The parameterList doesn't contain a <scalarMultiplier> element.");
     	}
     	//set matrix on the parameter object.
     	params.setMatrixListFromRequest(matrixList);
         
         //extract scalar and set on parameter object
         params.setScalarMultiplier( extractScalarFromDomNode(scalarValue));
         return params;	
     }
 }
