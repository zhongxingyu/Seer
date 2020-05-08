 package net.sf.testium.configuration;
 
 import net.sf.testium.executor.general.SpecifiedParameter;
 
 import org.testtoolinterfaces.testsuite.TestSuiteException;
 import org.testtoolinterfaces.utils.GenericTagAndBooleanXmlHandler;
 import org.testtoolinterfaces.utils.GenericTagAndStringXmlHandler;
 import org.testtoolinterfaces.utils.Trace;
 import org.testtoolinterfaces.utils.XmlHandler;
 import org.xml.sax.Attributes;
 import org.xml.sax.XMLReader;
 
 /**
  * XmlHandler to read the parameter Specification from an XML file
  *  <parameterspec name="..." type="...">
  *    <optional>...</optional>
  *    <valueAllowed>...</valueAllowed>
  *    <variableAllowed>...</variableAllowed>
  *    <emptyAllowed>...</emptyAllowed>
  *    <default>...</default>
  *  </parameterspec>
  * 
  * @author Arjan Kranenburg 
  * @see http://www.testtoolinterfaces.org
  * 
  */
 
 public class ParameterSpecificationXmlHandler extends XmlHandler {
 
 	public static final String START_ELEMENT = "parameterspec";
 	private static final String ATTRIBUTE_NAME = "name";
 	private static final String ATTRIBUTE_TYPE = "type";
 
 	private static final String OPTIONAL_ELEMENT = "optional";
 	private static final String VALUE_ELEMENT = "valueAllowed";
 	private static final String VARIABLE_ELEMENT = "variableAllowed";
 	private static final String EMPTY_ELEMENT = "emptyAllowed";
 	private static final String DEFAULT_ELEMENT = "default";
 	
 	private GenericTagAndBooleanXmlHandler myOptionalXmlHandler;
 	private GenericTagAndBooleanXmlHandler myValueXmlHandler;
 	private GenericTagAndBooleanXmlHandler myVariableXmlHandler;
 	private GenericTagAndBooleanXmlHandler myEmptyXmlHandler;
 	private GenericTagAndStringXmlHandler myDefaultXmlHandler;
 
 	private String myName;
 	private String myType;
 	
 	private boolean myOptional;
 	private boolean myValueAllowed;
 	private boolean myVariableAllowed;
 	private boolean myEmptyAllowed;
 	private String myDefault;
 
     
 	/**
 	 * Creates the XML Handler
 	 * 
 	 * @param anXmlReader		The XML Reader
 	 */
 	public ParameterSpecificationXmlHandler( XMLReader anXmlReader )
 	{
 		super(anXmlReader, START_ELEMENT);
 		Trace.println(Trace.CONSTRUCTOR);
 
 		myOptionalXmlHandler = new GenericTagAndBooleanXmlHandler(anXmlReader, OPTIONAL_ELEMENT);
 		this.addElementHandler(myOptionalXmlHandler);
 
 		myValueXmlHandler = new GenericTagAndBooleanXmlHandler(anXmlReader, VALUE_ELEMENT);
 		this.addElementHandler(myValueXmlHandler);
 		myVariableXmlHandler = new GenericTagAndBooleanXmlHandler(anXmlReader, VARIABLE_ELEMENT);
 		this.addElementHandler(myVariableXmlHandler);
 		
 		myEmptyXmlHandler = new GenericTagAndBooleanXmlHandler(anXmlReader, EMPTY_ELEMENT);
 		this.addElementHandler(myEmptyXmlHandler);
 		myDefaultXmlHandler = new GenericTagAndStringXmlHandler(anXmlReader, DEFAULT_ELEMENT);
 		this.addElementHandler(myDefaultXmlHandler);
 
 		reset();
 	}
 
 	@Override
 	public void processElementAttributes(String aQualifiedName, Attributes att)
     {
 		Trace.print(Trace.SUITE, "processElementAttributes( "
 	            + aQualifiedName, true );
      	if (aQualifiedName.equalsIgnoreCase(START_ELEMENT))
     	{
 		    for (int i = 0; i < att.getLength(); i++)
 		    {
 	    		Trace.append( Trace.SUITE, ", " + att.getQName(i) + "=" + att.getValue(i) );
 		    	if (att.getQName(i).equalsIgnoreCase(ATTRIBUTE_NAME))
 		    	{
 		        	myName = att.getValue(i);
 		    	}
 		    	else if (att.getQName(i).equalsIgnoreCase(ATTRIBUTE_TYPE))
 		    	{
 		    		myType = att.getValue(i);
 		    	}
 		    	else
 		    	{
 					throw new Error( "The attribute '" + att.getQName(i) 
 					                 + "' is not supported for configuration of the Selenium Plugin, element " + START_ELEMENT );
 		    	}
 		    }
     	}
 		Trace.append( Trace.SUITE, " )\n");
     }
 
 	@Override
 	public void handleStartElement(String aQualifiedName)
 	{
 		// nop
 	}
 
 	@Override
 	public void handleCharacters(String aValue)
 	{
 		// nop
 	}
 
 	@Override
 	public void handleEndElement(String aQualifiedName)
 	{
 		// nop
 	}
 
 	@Override
 	public void handleGoToChildElement(String aQualifiedName)
 	{
 		// nop
 	}
 
 	@Override
 	public void handleReturnFromChildElement(String aQualifiedName, XmlHandler aChildXmlHandler)
 	{
 		Trace.println(Trace.SUITE);
 		if (aQualifiedName.equalsIgnoreCase( OPTIONAL_ELEMENT ))
     	{
 			myOptional = myOptionalXmlHandler.getBoolean();
 			myOptionalXmlHandler.reset();
     	}
 		else if (aQualifiedName.equalsIgnoreCase( VALUE_ELEMENT ))
     	{
 			myValueAllowed = myValueXmlHandler.getBoolean();
 			myValueXmlHandler.reset();
     	}
     	else if (aQualifiedName.equalsIgnoreCase( VARIABLE_ELEMENT ))
     	{
     		myVariableAllowed = myVariableXmlHandler.getBoolean();
     		myVariableXmlHandler.reset();
     	}
     	else if (aQualifiedName.equalsIgnoreCase( EMPTY_ELEMENT ))
     	{
     		myEmptyAllowed = myEmptyXmlHandler.getBoolean();
     		myEmptyXmlHandler.reset();
     	}
     	else if (aQualifiedName.equalsIgnoreCase( DEFAULT_ELEMENT ))
     	{
     		myDefault = myDefaultXmlHandler.getValue();
     		myDefaultXmlHandler.reset();
     	}
     	else
     	{ // Programming fault
 			throw new Error( "Child XML Handler returned, but not recognized. The handler was probably defined " +
 			                 "in the Constructor but not handled in handleReturnFromChildElement()");
     	}
 	}
 
 	/**
 	 * @return the parameter
 	 * @throws TestSuiteException when parameterId is not set, when there is no value
 	 * 							 or when the parameter cannot be created for the current interface.
 	 */
 	public SpecifiedParameter getParameterSpec() throws TestSuiteException
 	{
 		Trace.println(Trace.GETTER, "getParameter()", true);
 		if ( myName.isEmpty() )
 		{
 			throw new TestSuiteException("Unknown Parameter Name");
 		}
 		if ( myType.isEmpty() )
 		{
 			throw new TestSuiteException("Unknown Parameter Type for parameter \"" + myName + "\"" );
 		}
 		Class<?> type;
 		try
 		{
 			type = Class.forName("java.lang." + myType);
		} catch (ClassNotFoundException e)
 		{
			throw new TestSuiteException("No class \"" + myType + "\" known for parameter \"" + myName + "\"" );
 		}
 
 		SpecifiedParameter paramSpec = new SpecifiedParameter(myName, type, myOptional, myValueAllowed, myVariableAllowed, myEmptyAllowed);
 		if ( ! myDefault.isEmpty() )
 		{
 			paramSpec.setDefaultValue(myDefault);
 		}
 		
 		return paramSpec;
 	}
 
 	@Override
 	public void reset()
 	{
 		Trace.println(Trace.SUITE);
 		
 		myName = "";
 		myType = "";
 		
 		myOptional = false;
 		myValueAllowed = true;
 		myVariableAllowed = false;
 		myEmptyAllowed = true;
 		myDefault = "";
 	}
 }
