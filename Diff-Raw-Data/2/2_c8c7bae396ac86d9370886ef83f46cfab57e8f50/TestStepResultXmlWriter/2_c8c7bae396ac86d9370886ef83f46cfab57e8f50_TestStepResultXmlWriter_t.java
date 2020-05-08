 /**
  * 
  */
 package org.testtoolinterfaces.testresultinterface;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testsuite.Parameter;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.testsuite.ParameterHash;
 import org.testtoolinterfaces.testsuite.ParameterImpl;
 import org.testtoolinterfaces.testsuite.ParameterVariable;
 import org.testtoolinterfaces.utils.Trace;
 
 /**
  * @author arjan.kranenburg
  *
  */
 public class TestStepResultXmlWriter
 {
 //	private String myIndent;
 	private int myIndentLevel = 0;
 	private TestStepResultXmlWriter mySubTestStepResultXmlWriter;
 
 	/**
 	 * 
 	 */
 	public TestStepResultXmlWriter()
 	{
 		this( 4 );
 	}
 
 //	/**
 //	 * @param anIndent
 //	 */
 //	public TestStepResultXmlWriter( String anIndent )
 //	{
 //		Trace.println(Trace.CONSTRUCTOR);
 //		myIndent = anIndent;
 //	}
 
 	/**
 	 * @param anIndent
 	 */
 	public TestStepResultXmlWriter( int anIndentLevel )
 	{
 		Trace.println(Trace.CONSTRUCTOR);
 		myIndentLevel = anIndentLevel;
 	}
 
 	/**
 	 * @param aResult	the Test Step Result
 	 * @param aStream	the stream to write the test step result in xml-format to
 	 * 
 	 * @throws IOException 
 	 */
 	public void printXml( TestStepResult aResult,
 	                      OutputStreamWriter aStream,
 	                      File aLogDir) throws IOException
 	{
 		Trace.println(Trace.UTIL);
 		String indent = repeat( ' ', myIndentLevel );
 		aStream.write( indent + "<teststep" );
 		aStream.write(" sequence='" + aResult.getSequenceNr() + "'");
 		aStream.write(">\n");
 
 		String description = aResult.getDescription();
     	aStream.write( indent + "  <description>");
     	aStream.write(description);
     	aStream.write("</description>\n");
 	    
     	String command = aResult.getCommand();
     	if ( ! command.isEmpty() ) { aStream.write( indent + "  <command>" + command + "</command>\n"); }
 
     	String script = aResult.getScript();
     	if ( ! script.isEmpty() ) { aStream.write( indent + "  <script>" + script + "</script>\n"); }
     	aStream.write( indent + "  <displayName>" + aResult.getDisplayName() + "</displayName>\n");
 
     	printSubTestStep( aResult, aStream, aLogDir );
     	
     	aStream.write( indent + "  <result>" + aResult.getResult().toString() + "</result>\n");
 
     	ParameterArrayList parameters = aResult.getParameters();
     	ArrayList<Parameter> params = parameters.sort();
     	for(int i=0; i<params.size(); i++)
     	{
     		Parameter param = params.get(i);
         	aStream.write( indent + "  <parameter id='" + param.getName() + "' " );
     		if (ParameterImpl.class.isInstance(param))
     		{
     			aStream.write( "type='value' sequence='" + param.getIndex() + "'>"
             	               + ((ParameterImpl) param).getValue().toString() );
    			
     		}
     		else if (ParameterVariable.class.isInstance(param))
     		{
             	aStream.write( "type='variable' sequence='" + param.getIndex() + "'>"
             	               + ((ParameterVariable) param).getVariableName() );
     		}
     		else if (ParameterHash.class.isInstance(param))
     		{
     			// TODO print the sub-parameters
             	aStream.write( "type='hash' sequence='" + param.getIndex() + "'>"
             	               + ((ParameterHash) param).size() + " sub-parameters" );
     		}
     		else
     		{
             	aStream.write( "type='unknown' sequence='" + param.getIndex() + "'>" );
     		}
         	aStream.write("</parameter>\n" );
     	}
 
     	String comment = aResult.getComment();
     	if ( ! comment.isEmpty() ) { aStream.write( indent + "  <comment>" + comment + "</comment>\n"); }
 
    	XmlWriterUtils.printXmlLogFiles(aResult.getLogs(), aStream, aLogDir.getAbsolutePath(), indent + "  ");
 		aStream.write( indent + "</teststep>\n");
 	}
 	
 	private void printSubTestStep( TestStepResult aResult,
 	    	                       OutputStreamWriter aStream,
 	    	                       File aLogDir ) throws IOException
 	{
 		Trace.println(Trace.UTIL);
 
 		ArrayList<TestStepResult> subStepResults = aResult.getSubSteps();
 		if ( subStepResults.size() > 0 )
 		{
 			String indent = repeat( ' ', myIndentLevel + 2 );
 	    	aStream.write( indent + "<substeps>\n");
 	    	if ( mySubTestStepResultXmlWriter == null )
 	    	{
 	    		mySubTestStepResultXmlWriter = new TestStepResultXmlWriter( myIndentLevel + 2 );
 	    	}
 
 	    	Iterator<TestStepResult> subStepResultsItr = subStepResults.iterator();
 	    	while (subStepResultsItr.hasNext())
 	    	{
 				TestStepResult tsResult = subStepResultsItr.next();
 				mySubTestStepResultXmlWriter.printXml(tsResult, aStream, aLogDir);
 	    	}
 			
 	    	aStream.write( indent + "</substeps>\n");
 		}	
 	}
 
 	private static String repeat(char c,int i)
 	{
 		String str = "";
 		for(int j = 0; j < i; j++)
 		{
 			str = str+c;
 		}
 		return str;
 	}
 }
