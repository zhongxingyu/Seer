 package com.k_int.euinside.client.module.dataTransformation;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.http.entity.ContentType;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.k_int.euinside.client.HttpResult;
 import com.k_int.euinside.client.UnZipRecords;
 import com.k_int.euinside.client.ZipRecords;
 import com.k_int.euinside.client.http.ClientHTTP;
 import com.k_int.euinside.client.json.ClientJSON;
 import com.k_int.euinside.client.module.Action;
 import com.k_int.euinside.client.module.Attribute;
 import com.k_int.euinside.client.module.BaseModule;
 import com.k_int.euinside.client.module.CommandLineArguments;
 import com.k_int.euinside.client.module.Module;
 
 public class Transform extends BaseModule {
 	
 	/**
 	 * Builds the path required for the data mapping module
 	 * 
 	 * @param provider A code for the provider of the data
 	 * @param batch An identifier that identifies this submission, if the records are to be retrieved later
 	 * @param action The action to be performed 
 	 * @return The path that is required for the data mapping module
 	 */
 	static private String buildPath(String provider, String batch, Action action, ArrayList<BasicNameValuePair> attributes) {
 		return(buildPath(Module.DATA_TRANSFORMATION, PATH_SEPARATOR + provider + PATH_SEPARATOR + batch + PATH_SEPARATOR + action.getName(), attributes));
 	}
 
 	/**
 	 * Builds the parameters used by the transform action
 	 * 
 	 * @param sourceFormat The format of the supplied file(s)
 	 * @param targetFormat The format that the file(s) are to converted to
 	 * 
 	 * @return An array of attributes that can be used by the underlying clients
 	 */
 	static private ArrayList<BasicNameValuePair> buildFormatParameters(Format sourceFormat, Format targetFormat) {
 		ArrayList<BasicNameValuePair> attributes = new ArrayList<BasicNameValuePair>();
 		attributes.add(new BasicNameValuePair(Attribute.SOURCE_FORMAT.getName(), ((sourceFormat == null) ? Format.LIDO.toString() : sourceFormat.toString())));
 		attributes.add(new BasicNameValuePair(Attribute.TARGET_FORMAT.getName(), ((targetFormat == null) ? Format.EDM.toString() : targetFormat.toString())));
 
 		return(attributes);
 	}
 	
 	/**
 	 * Builds the parameters used by the status and fetch actions
 	 * 
 	 * @param request The request object returned by the transform call
 	 * 
 	 * @return An array of attributes that can be used by the underlying clients
 	 */
 	static private ArrayList<BasicNameValuePair> buildRequestParameters(RequestResponse request) {
 		ArrayList<BasicNameValuePair> attributes = new ArrayList<BasicNameValuePair>();
 		if (request != null) {
 			attributes.add(new BasicNameValuePair(Attribute.REQUEST_ID.getName(), request.getRequestId()));
 		}
 		return(attributes);
 	}
 	
 	/**
 	 * Sends the supplied xml record to the DataMapping module, for zip files use sendFiles
 	 *
 	 * @param provider A code for the provider of the data
 	 * @param batch An identifier that identifies this submission, for when the records are retrieved later
 	 * @param sourceFormat The format the source data is in 
 	 * @param targetFormat The format that the data is to be converted to 
 	 * @param transformRecords The records that are to be zipped and sent
 	 * 
 	 * @return The trnasformRecords that were passed to us, accept they will now be updated with the converted record 
 	 */
 	static public List<TransformRecord> transformWait(String provider, String batch, Format sourceFormat, Format targetFormat, List<TransformRecord> transformRecords) {
 		
 		// Build the zip file
 		ZipRecords zipRecords = new ZipRecords();
 		HashMap<String, TransformRecord> recordMap = new HashMap<String, TransformRecord>();  
 		for (TransformRecord transformRecord : transformRecords) {
 			recordMap.put("Transformed_" + transformRecord.add(zipRecords), transformRecord);
 		}
 		
 		// Send the records away to be converted
 		HttpResult httpResult = ClientHTTP.sendBytes(buildPath(provider, batch, Action.DATA_TRANSFORMATION_TRANSFORM, buildFormatParameters(sourceFormat, targetFormat)), zipRecords.getZip(), ContentType.APPLICATION_JSON);
 		if (httpResult.getHttpStatusCode() == HttpServletResponse.SC_OK) {
 			RequestResponse request = ClientJSON.readJSONString(httpResult.getContent(), RequestResponse.class);
 			
 			// Wait until the records have been converted
 			byte [] zipResponse = wait(provider, batch, request);
 			if (zipResponse != null) {
 				UnZipRecords unZipRecords = new UnZipRecords(zipResponse);
 				StringBuffer name = new StringBuffer();
 				byte [] convertedRecord = null;
 				while ((convertedRecord = unZipRecords.getNextEntry(name)) != null) {
 					// Look up our hash map for the record
 					TransformRecord record = recordMap.get(name.toString());
 					if (record != null) {
 						record.setXmlConvertedRecord(convertedRecord);
 					}
 				}
 			}
 		}
 		return(transformRecords);
 	}
 
 	/**
 	 * Sends the supplied xml record to the DataMapping module, for zip files use sendFiles
 	 *
 	 * @param provider A code for the provider of the data
 	 * @param batch An identifier that identifies this submission, for when the records are retrieved later
 	 * @param sourceFormat The format the source data is in 
 	 * @param targetFormat The format that the data is to be converted to 
 	 * @param xmlRecord The record that is to be sent
 	 * 
 	 * @return The result returned from the server, we do not attempt to transform it as we are expecting either an xml file
 	 */
 	static public RequestResponse transform(String provider, String batch, Format sourceFormat, Format targetFormat, byte[] xmlRecord) {
 		RequestResponse request = null;
 		ArrayList<byte[]> recordArray = new ArrayList<byte[]>();
 		recordArray.add(xmlRecord);
 		HttpResult httpResult = ClientHTTP.sendBytes(buildPath(provider, batch, Action.DATA_TRANSFORMATION_TRANSFORM, buildFormatParameters(sourceFormat, targetFormat)), recordArray, null, ContentType.APPLICATION_JSON);
 		if (httpResult.getHttpStatusCode() == HttpServletResponse.SC_OK) {
 			request = ClientJSON.readJSONString(httpResult.getContent(), RequestResponse.class);
 		}
 		return(request);
 	}
 
 	/**
 	 * Sends the supplied xml or zip file to the DataMapping module and waits until it is processed
 	 *  
 	 * @param provider A code for the provider of the data
 	 * @param batch An identifier that identifies this submission, for when the records are retrieved later
 	 * @param sourceFormat The format the source data is in 
 	 * @param targetFormat The format that the data is to be converted to 
 	 * @param xmlRecord The record that is to be sent
 	 * 
 	 * @return The result returned form the server
 	 */
 	static public byte [] transformWait(String provider, String batch, Format sourceFormat, Format targetFormat, byte[] xmlRecord) {
 		RequestResponse request = transform(provider, batch, sourceFormat, targetFormat, xmlRecord);
 		return(wait(provider, batch, request));
 	}
 	
 	/**
 	 * Sends the supplied xml or xip file to the DataMapping module
 	 *  
 	 * @param provider A code for the provider of the data
 	 * @param batch An identifier that identifies this submission, for when the records are retrieved later
 	 * @param sourceFormat The format the source data is in 
 	 * @param targetFormat The format that the data is to be converted to 
 	 * @param filename The file that contains the record that is to be sent to the validation module
 	 * 
 	 * @return The result returned form the server (it is not structured so we do not attempt to interpret it)
 	 */
 	static public RequestResponse transform(String provider, String batch, Format sourceFormat, Format targetFormat, String filename) {
 		RequestResponse request = null;
 		ArrayList<String> filenameArray = new ArrayList<String>();
 		filenameArray.add(filename);
 		HttpResult httpResult = ClientHTTP.sendFiles(buildPath(provider, batch, Action.DATA_TRANSFORMATION_TRANSFORM, buildFormatParameters(sourceFormat, targetFormat)), filenameArray, ContentType.APPLICATION_JSON);
 		if (httpResult.getHttpStatusCode() == HttpServletResponse.SC_OK) {
 			request = ClientJSON.readJSONString(httpResult.getContent(), RequestResponse.class);
 		}
 		return(request);
 	}
 	
 	/**
 	 * Sends the supplied xml or zip file to the DataMapping module and waits until it is processed
 	 *  
 	 * @param provider A code for the provider of the data
 	 * @param batch An identifier that identifies this submission, for when the records are retrieved later
 	 * @param sourceFormat The format the source data is in 
 	 * @param targetFormat The format that the data is to be converted to 
 	 * @param filename The file that contains the record that is to be sent to the validation module
 	 * 
 	 * @return The result returned form the server
 	 */
 	static public byte [] transformWait(String provider, String batch, Format sourceFormat, Format targetFormat, String filename) {
 		RequestResponse request = transform(provider, batch, sourceFormat, targetFormat, filename);
 		return(wait(provider, batch, request));
 	}
 	
 	/**
 	 * Waits for the record to be transformed
 	 *  
 	 * @param provider A code for the provider of the data
 	 * @param batch An identifier that identifies this submission, for when the records are retrieved later
	 * @param sourceFormat The format the source data is in 
	 * @param targetFormat The format that the data is to be converted to 
	 * @param filename ... The file that contains the record that is to be sent to the validation module
 	 * 
 	 * @return The result returned form the server
 	 */
 	static public byte [] wait(String provider, String batch, RequestResponse request) {
 		byte [] result = null;
 		if (request != null) {
 			// It has accepted our submission, let us wait until it has been processed
 			TransformationStatus transformationStatus = TransformationStatus.NOT_READY;
 			while (transformationStatus == TransformationStatus.NOT_READY) {
 				// We will have to make sure this does not become an endless loop somehow or other
 				transformationStatus = status(provider, batch, request);
 				
 				// Total guess but, we will assume the httpcode of OK means that it is ok to be retrieved
 				if (transformationStatus == TransformationStatus.READY) {
 					result = fetch(provider, batch, request);
 				} else {
 					// It is not ready yet, so wait 10 seconds, before we try again
 					try {
 						Thread.sleep(10000);
 					} catch (InterruptedException e) {
 						// will ignore for time being
 					}
 				}
 			}
 		}
 		return(result);
 	}
 	
 	/**
 	 * Fetch the results for the supplied batch transmission
 	 *  
 	 * @param provider A code for the provider of the data
 	 * @param batch An identifier that identifies the submission we are interested in
 	 * 
 	 * @return The result returned from the server
 	 */
 	static public byte [] fetch(String provider, String batch, RequestResponse request) {
 		byte [] result = null;
 		HttpResult httpResult = ClientHTTP.send(buildPath(provider, batch, Action.DATA_TRANSFORMATION_FETCH, buildRequestParameters(request)), ContentType.APPLICATION_OCTET_STREAM);
 		if (httpResult.getHttpStatusCode() == HttpServletResponse.SC_OK) {
 			result = httpResult.getContentBytes();
 		}
 		return(result);
 	}
 	
 	/**
 	 * Requests the status of a previously submitted batch
 	 *  
 	 * @param provider A code for the provider of the data
 	 * @param batch An identifier that identifies the submission we are interested in
 	 * 
 	 * @return The result returned from the server
 	 */
 	static public TransformationStatus status(String provider, String batch, RequestResponse request) {
 		TransformationStatus transformationStatus = TransformationStatus.NOT_READY;
 		HttpResult httpResult = ClientHTTP.send(buildPath(provider, batch, Action.DATA_TRANSFORMATION_STATUS, buildRequestParameters(request)), ContentType.APPLICATION_JSON);
 		if (httpResult.getHttpStatusCode() == HttpServletResponse.SC_OK) {
 			StatusResponse status = ClientJSON.readJSONString(httpResult.getContent(), StatusResponse.class);
 			transformationStatus = status.getTransformationStatus();
 		}
 		return(transformationStatus);
 	}
 
 	/**
 	 * Exercises all the methods with the supplied parameters
 	 * 
 	 * @param args The parameters passed in on the command line, valid parameters are:<br/><br/>
 	 *  <table class="arguments">
 	 * 	    <col width="15%"/>
 	 *      <col width="85%"/>
 	 *      <tr><td>-batch</td><td>The batch name to use for the transformation</td></tr>
 	 *      <tr><td>-coreBaseURL</td><td>The base URL of the core module</td></tr>
 	 * 		<tr><td>-filename</td><td>The name of a file that contains a record with no validation errors</td></tr>
 	 * 		<tr><td>-provider</td><td>The provider this transformation is on behalf off</td></tr>
 	 * 		<tr><td>-outputFile</td><td>The filename to send the transformed file to, if not specified it will be sent to stdout, if you are expecting a zip file in response it would make sense to specify this file</td></tr>
 	 * 		<tr><td>-sourceFormat</td><td>The format of the file</td></tr>
 	 * 		<tr><td>-targetFormat</td><td>The format that you want the file being converted to</td></tr>
      *  </table>
 	 */
 	public static void main(String [] args)	{
 		CommandLineArguments arguments = parseCommandLineArguments(args);
 		
 		String provider = arguments.getProvider();
 		String batch = arguments.getBatch();
 		if (batch.isEmpty()) {
 			batch = "batchXX";
 		}
 		
 		if (provider.isEmpty()) {
 			System.out.println("No provider supplied");
 		} else {
 			// have we been supplied with any files
 			if (arguments.getFilenames().isEmpty()) {
 				System.out.println("No files supplied");
 			} else {
 				// Test sending the first file
 				byte [] content = transformWait(provider, batch, arguments.getSourceFormat(), arguments.getTargetFormat(), arguments.getFilenames().get(0));
 		
 				if (content == null) {
 					System.out.println("Error transforming file");
 				} else {
 					System.out.println("Result from transform");
 					if (arguments.getOutputFile() != null) {
 						System.out.println("output written to file: " + arguments.getOutputFile());
 						try {
 							FileOutputStream outputStream = new FileOutputStream(new File(arguments.getOutputFile()));
 							IOUtils.write(content, outputStream);
 							outputStream.close();
 						} catch (Exception e) {
 							System.out.println("Exception writing to file: " + e.toString());
 						}
 					} else {
 						System.out.println(new String(content));
 					}
 				}
 
 				// Test sending the files zipped, but passing through as a byte array
 				List<TransformRecord> transformRecords = new ArrayList<TransformRecord>();
 				Long counter = new Long(1);
 				for (String filename : arguments.getFilenames()) {
 					try {
 						if (filename.endsWith(".xml")) {
 							TransformRecord transformRecord = new TransformRecord();
 							transformRecord.setIdentifier(counter);
 							transformRecord.setXmlRecordToConvert(FileUtils.readFileToByteArray(new File(filename)));
 							transformRecords.add(transformRecord);
 							counter++;
 						} else {
 							System.out.println("Ignore file: " + filename + " as it dosn't end with .xml");
 						}
 					} catch (IOException e) {
 						System.out.println("Error reading file: " + filename);
 					}
 				}
 				transformWait(provider, batch, Format.LIDO, Format.EDM, transformRecords);
 				for (TransformRecord transformRecord : transformRecords) {
 					System.out.println("Identifier: " + transformRecord.getIdentifier().toString());
 					byte [] converted = transformRecord.getXmlConvertedRecord();
 					System.out.println("Converted record: " + ((converted == null) ? "Not Converted" : new String(converted, Charset.forName("UTF-8"))));
 				}
 			}
 		}
 	}
 }
