 /**
  * 
  */
 package org.testtoolinterfaces.testresultinterface;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOError;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.nio.channels.FileChannel;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import org.testtoolinterfaces.testresult.TestResult;
 import org.testtoolinterfaces.utils.Trace;
 
 /**
  * @author Arjan
  *
  */
 public class XmlWriterUtils
 {
 	/**
 	 * @param aStream
 	 * @param anXslRef
 	 * @throws IOException
 	 */
 	public static void printXmlDeclaration (OutputStreamWriter aStream, String aXslRef) throws IOException
 	{
 	    Trace.println(Trace.UTIL);
 		aStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
 		aStream.write("<?xml-stylesheet type=\"text/xsl\" href=\"" + aXslRef + "\"?>\n\n");
 	}
 
 	/**
 	 * Checks if there are log-files and then prints them in XML format 
 	 * 
 	 * @param aTestResult   Test Result(s)
 	 * @param aStream   	OutputStreamWriter of the stream to print the xml to
 	 * 
 	 * @throws IOException
 	 */
 	public static void printXmlLogFiles(Hashtable<String, String> aLogs, OutputStreamWriter aStream, String aBaseLogDir, String anIndent) throws IOException
 	{
 	    Trace.println( Trace.UTIL, "printXmlLogFiles( " + aLogs.size() + "logs, aStream, " 
 	                  									+ aBaseLogDir + ", "
 	                  									+ anIndent + ")", true );
 
       	if (!aLogs.isEmpty())
       	{
 		    for (Enumeration<String> keys = aLogs.keys(); keys.hasMoreElements();)
 		    {
 		    	String key = keys.nextElement();
 		    	String logFile = (new File(aLogs.get(key))).getCanonicalPath();
		    	logFile = makeFileRelative(logFile, new File(aBaseLogDir).getCanonicalPath());
 		    	aStream.write(anIndent + "  <logfile");
 		    	aStream.write(" type='" + key + "'");
 		    	aStream.write(">" + logFile);
 		    	aStream.write("</logfile>\n");
 		    }
   
       	}
 	}
 
 	/**
 	 * @param aLogFile
 	 * @param aBaseLogDir
 	 * @return
 	 */
 	public static String makeFileRelative(String aLogFile, String aBaseLogDir)
 	{
 		if ( aLogFile.startsWith(aBaseLogDir) )
 		{
 			aLogFile = aLogFile.substring(aBaseLogDir.length());
 			if ( aLogFile.startsWith(File.separator) )
 			{
 				aLogFile = aLogFile.substring(File.separator.length());
 			}
 		}
 		aLogFile = aLogFile.replace('\\', '/');
 		return aLogFile;
 	}
 	
 	/**
 	 * Checks if there is a comment and then prints it in XML format
 	 * 
 	 * @param aStream		the OutputStreamWriter of the stream to write the xml to
 	 * @param aTestResult	the Test Result(s)
 	 * 
 	 * @throws IOException
 	 */
 	public static void printXmlComment(TestResult aTestResult, OutputStreamWriter aStream, String anIndent) throws IOException
 	{
 	    Trace.println(Trace.UTIL);
 		String comment = aTestResult.getComment();
 	    if ( !comment.isEmpty() )
 	    {
 	    	aStream.write(anIndent + "<comment>");
 	    	aStream.write(comment);
 	    	aStream.write("</comment>\n");
 	    }
 	}
 
 	public static void printXmlRequirement(OutputStreamWriter aStream, String aRequirementId, String anIndent) throws IOException
 	{
 	    Trace.println(Trace.UTIL);
     	aStream.write(anIndent + "<requirementid>");
     	aStream.write(aRequirementId);
     	aStream.write("</requirementid>\n");
 	}
 	
 	public static void copyXsl(File aSourceDir, File aTargetLogDir)
 	{
 	    Trace.println(Trace.UTIL, "copyXsl( " + aTargetLogDir.getName() + " )", true);
 		if ( aSourceDir == null )
 		{
 			return;
 		}
 		
         File[] files = aSourceDir.listFiles();
         for (int i=0; i<files.length; i++)
         {
         	File srcFile = files[i];
         	if (!srcFile.isDirectory())
         	{
             	File tgtFile = new File(aTargetLogDir + File.separator + srcFile.getName());
 
             	FileChannel inChannel = null;
             	FileChannel outChannel = null;
             	try
             	{
             		inChannel = new FileInputStream(srcFile).getChannel();
             		outChannel = new FileOutputStream(tgtFile).getChannel();
                 	inChannel.transferTo(0, inChannel.size(), outChannel);
                 } 
                 catch (IOException e)
                 {
                 	throw new IOError( e );
                 }
                 finally
                 {
 	                if (inChannel != null) try
 					{
 						inChannel.close();
 					}
 					catch (IOException exc)
 					{
 	                	throw new IOError( exc );
 					}
 	                if (outChannel != null) try
 					{
 						outChannel.close();
 					}
 					catch (IOException exc)
 					{
 	                	throw new IOError( exc );
 					}
 	            }
         	}
         }
 	}
 }
