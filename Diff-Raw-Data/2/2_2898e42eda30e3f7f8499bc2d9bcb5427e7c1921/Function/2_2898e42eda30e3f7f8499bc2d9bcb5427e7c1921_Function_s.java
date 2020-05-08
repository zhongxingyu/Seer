 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.spec;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import org.xins.common.text.ParseException;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementParser;
 
 /**
  * Specification of the function.
  *
  * @version $Revision$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class Function {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new instance of Function
     */
    public Function(String functionName, Class reference) throws InvalidSpecificationException {
       _reference = reference;
       _functionName = functionName;
       try {
          InputStream in = reference.getResourceAsStream("/specs/" + functionName + ".fnc");
          if (in == null) {
             throw new IllegalArgumentException("No function named \"" + functionName +"\" found in the specifications.");
          }
          InputStreamReader reader = new InputStreamReader(in);
          parseFunction(reader);
          reader.close();
          in.close();
       } catch (IOException ioe) {
          throw new InvalidSpecificationException(ioe.getMessage());
       }
    }
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
    
    /**
     * The class used as reference.
     */
    private final Class _reference;
    
    /**
     * Name of the function.
     */
    private final String _functionName;
    
    /**
     * Description of the function.
     */
    private String _description;
    
    /**
     * The input parameters of the function.
     */
    private Parameter[] _inputParameters;
 
    /**
     * The input param combos of the function.
     */
    private ParamCombo[] _inputParamCombos;
 
    /**
     * The input data section elements of the function.
     */
    private DataSectionElement[] _inputDataSectionElements;
 
    /**
     * The defined error code that the function can return.
     */
    private ErrorCode[] _errorCodes;
 
    /**
     * The output parameters of the function.
     */
    private Parameter[] _outputParameters;
 
    /**
     * The output param combos of the function.
     */
    private ParamCombo[] _outputParamCombos;
 
    /**
     * The output data section elements of the function.
     */
    private DataSectionElement[] _outputDataSectionElements;
 
    
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the name of the function.
     *
     * @return
     *    The name of the function, never <code>null</code>.
     */
    public String getName() {
 
       return _functionName;
    }
 
    /**
     * Gets the description of the function.
     *
     * @return
     *    The description of the function, never <code>null</code>.
     */
    public String getDescription() {
       
       return _description;
    }
 
    /**
     * Gets the input parameter specifications defined in the function.
     *
     * @return
     *    The input parameters, never <code>null</code>.
     */
    public Parameter[] getInputParameters() {
       
       return _inputParameters;
    }
 
    /**
     * Gets the output parameter specifications defined in the function.
     *
     * @return
     *    The output parameters, never <code>null</code>.
     */
    public Parameter[] getOutputParameters() {
       
       return _outputParameters;
    }
 
    /**
     * Gets the error code specifications defined in the function.
     * The standard error code are not included.
     *
     * @return
     *    The error code specifications, never <code>null</code>.
     */
    public ErrorCode[] getErrorCodes() {
       
       return _errorCodes;
    }
 
    /**
     * Gets the specification of the elements of the input data section.
     *
     * @return
     *   The input data section elements, never <code>null</code>.
     */
    public DataSectionElement[] getInputDataSectionElements() {
       
       return _inputDataSectionElements;
    }
 
    /**
     * Gets the specification of the elements of the output data section.
     *
     * @return
     *   The output data section elements, never <code>null</code>.
     */
    public DataSectionElement[] getOutputDataSectionElements() {
       
       return _outputDataSectionElements;
    }
 
    /**
     * Gets the input param combo specifications.
     *
     * @return
     *    The specification of the input param combos, never <code>null</code>.
     */
    public ParamCombo[] getInputParamCombos() {
       
       return _inputParamCombos;
    }
 
    /**
     * Gets the output param combo specifications.
     *
     * @return
     *    The specification of the output param combos, never <code>null</code>.
     */
    public ParamCombo[] getOutputParamCombos() {
       
       return _outputParamCombos;
    }
    
    /**
     * Parses the function file.
     */
    private void parseFunction(Reader reader) throws IOException, InvalidSpecificationException {
       ElementParser parser = new ElementParser();
       Element function = null;
       try {
          function = parser.parse(reader);
       } catch (ParseException pe) {
          throw new InvalidSpecificationException(pe.getMessage());
       }
       Element descriptionElement = (Element) function.getChildElements("description").get(0);
       _description = descriptionElement.getText();
       List input = function.getChildElements("input");
       if (input.size() == 0) {
          _inputParameters = new Parameter[0];
          _inputParamCombos = new ParamCombo[0];
          _inputDataSectionElements = new DataSectionElement[0];
       } else {
          
          // Input parameters
          Element inputElement = (Element) input.get(0);
          _inputParameters = parseParameters(_reference, inputElement);
 
          // Param combos
          _inputParamCombos = parseParamCombos(_reference, inputElement, _inputParameters);
          
          // Data section
          List dataSections = inputElement.getChildElements("data");
          if (dataSections.size() == 0) {
             _inputDataSectionElements = new DataSectionElement[0];
          } else {
             Element dataSection = (Element) dataSections.get(0);
             // TODO String contains = dataSection.getAttribute("contains");
             _inputDataSectionElements = parseDataSectionElements(_reference, dataSection, dataSection);
          }
       }
       
       List output = function.getChildElements("output");
       if (output.size() == 0) {
          _errorCodes = new ErrorCode[0];
          _outputParameters = new Parameter[0];
          _outputParamCombos = new ParamCombo[0];
          _outputDataSectionElements = new DataSectionElement[0];
       } else {
          
          Element outputElement = (Element) output.get(0);
          
          // Error codes
          List errorCodesList = outputElement.getChildElements("resultcode-ref");
          _errorCodes = new ErrorCode[errorCodesList.size()];
          Iterator itErrorCodes = errorCodesList.iterator();
          int i = 0;
          while (itErrorCodes.hasNext()) {
             Element nextErrorCode = (Element) itErrorCodes.next();
             String errorCodeName = nextErrorCode.getAttribute("name");
             _errorCodes[i++] = new ErrorCode(errorCodeName, _reference);
          }
          
          // Output parameters
          _outputParameters = parseParameters(_reference, outputElement);
 
          // Param combos
          _outputParamCombos = parseParamCombos(_reference, outputElement, _outputParameters);
          
          // Data section
          List dataSections = outputElement.getChildElements("data");
          if (dataSections.size() == 0) {
             _outputDataSectionElements = new DataSectionElement[0];
          } else {
             Element dataSection = (Element) dataSections.get(0);
             // TODO String contains = dataSection.getAttribute("contains");
             _outputDataSectionElements = parseDataSectionElements(_reference, dataSection, dataSection);
          }
       }
    }
    
    static DataSectionElement[] parseDataSectionElements(Class reference, Element topElement, Element dataSection) {
       List dataSectionContains = topElement.getChildElements("contains");
       if (!dataSectionContains.isEmpty()) {
          Element containsElement = (Element) dataSectionContains.get(0);
          List contained = containsElement.getChildElements("contained");
          DataSectionElement[] dataSectionElements = new DataSectionElement[contained.size()]; 
          Iterator itContained = contained.iterator();
          int i = 0;
          while (itContained.hasNext()) {
             Element containedElement = (Element) itContained.next();
             String name = containedElement.getAttribute("element");
             DataSectionElement dataSectionElement = getDataSectionElement(reference, name, dataSection);
             dataSectionElements[i++] = dataSectionElement;
          }
          return dataSectionElements;
       } else {
          return new DataSectionElement[0];
       }
    }
    
    static DataSectionElement getDataSectionElement(Class reference, String name, Element dataSection) {
       Iterator itElements = dataSection.getChildElements("element").iterator();
       while (itElements.hasNext()) {
          Element nextElement = (Element) itElements.next();
          String nextName = nextElement.getAttribute("name");
          if (nextName.equals(name)) {
             
             String description = ((Element) nextElement.getChildElements("description").get(0)).getText();
             
             DataSectionElement[] subElements = parseDataSectionElements(reference, nextElement, dataSection);
             
             boolean isPcdataEnable = false;
             List dataSectionContains = nextElement.getChildElements("contains");
             if (!dataSectionContains.isEmpty()) {
                Element containsElement = (Element) dataSectionContains.get(0);
                List pcdata = containsElement.getChildElements("pcdata");
                if (!pcdata.isEmpty()) {
                   isPcdataEnable = true;
                }
             }
             
            List attributesList = nextElement.getChildElements("attributes");
             int attributesCount = attributesList.size();
             Parameter[] attributes = new Parameter[attributesCount];
             for (int i = 0; i < attributesCount; i++) {
                attributes[i] = parseParameter(reference, (Element) attributesList.get(i));
             }
             
             DataSectionElement result = new DataSectionElement(nextName, description, isPcdataEnable, subElements, attributes);
             return result;
          }
       }
       return null;
    }
    
    static Parameter parseParameter(Class reference, Element paramElement) {
       String parameterName = paramElement.getAttribute("name");
       String parameterTypeName = paramElement.getAttribute("type");
       boolean requiredParameter = "true".equals(paramElement.getAttribute("required"));
       String parameterDescription = ((Element) paramElement.getChildElements("description").get(0)).getText();
       Parameter parameter = new Parameter(reference ,parameterName, parameterTypeName, requiredParameter, parameterDescription);
       return parameter;
    }
    
    static Parameter[] parseParameters(Class reference, Element topElement) {
       List parametersList = topElement.getChildElements("param");
       Parameter[] parameters = new Parameter[parametersList.size()];
       Iterator itParameters = parametersList.iterator();
       int i = 0;
       while (itParameters.hasNext()) {
          Element nextParameter = (Element) itParameters.next();
          parameters[i++] = parseParameter(reference, nextParameter);
       }
       return parameters;
    }
    
    static ParamCombo[] parseParamCombos(Class reference, Element topElement, Parameter[] parameters) {
       
       // The parameter table is needed for the param combo.
       Map parameterTable = new HashMap();
       for (int j = 0; j < parameters.length; j++) {
          parameterTable.put(parameters[j].getName(), parameters[j]);
       }
 
       List paramCombosList = topElement.getChildElements("param-combo");
       ParamCombo[] paramCombos = new ParamCombo[paramCombosList.size()];
       Iterator itParamCombos = paramCombosList.iterator();
       int i = 0;
       while (itParamCombos.hasNext()) {
          Element nextParamCombo = (Element) itParamCombos.next();
          String type = nextParamCombo.getAttribute("type");
          List paramDefs = nextParamCombo.getChildElements("param-def");
          Iterator itParamDefs = paramDefs.iterator();
          Parameter[] paramComboParameters = new Parameter[paramDefs.size()];
          int j = 0;
          while (itParamDefs.hasNext()) {
             Element paramDef = (Element) itParamDefs.next();
             String parameterName = paramDef.getAttribute("name");
             Parameter parameter = (Parameter) parameterTable.get(parameterName);
             paramComboParameters[j++] = parameter;
          }
          ParamCombo paramCombo = new ParamCombo(type, paramComboParameters);
          paramCombos[i++] = paramCombo;
       }
       return paramCombos;
    }
 }
