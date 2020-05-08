 package fedora.client.batch;
 
 import java.io.FileOutputStream;
 
 import java.io.PrintStream;
 import java.io.File;
 //import java.util.Hashtable;
 import java.util.Properties;
 import java.util.Enumeration;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerConfigurationException;
 //import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactory; // RunXSLT works with this
 //import javax.xml.transform.sax.SAXTransformerFactory; // was with error
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.stream.StreamResult;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.FileInputStream;
 import java.io.OutputStream;
 
 import java.io.Reader;
 import java.io.BufferedReader;
 
 import java.util.Vector;
 
 
 class BatchXforms {
 	/** Constants used for JAXP 1.2 */
     private static final String JAXP_SCHEMA_LANGUAGE =
         "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
     private static final String W3C_XML_SCHEMA =
         "http://www.w3.org/2001/XMLSchema";
     private static final String JAXP_SCHEMA_SOURCE =
         "http://java.sun.com/xml/jaxp/properties/schemaSource";
 
 
       private String additionsPath = null;
       private String objectsPath = null;
       private String xformPath = null;
       private String modelPath = null;      
       private Transformer transformer = null;
 		
       BatchXforms(Properties optValues) throws Exception {
		xformPath = System.getProperty("fedora.home") + "client/lib/merge.xsl";
System.out.println("xformPath => " + xformPath);
 		additionsPath = optValues.getProperty(BatchTool.ADDITIONSPATH);
 		objectsPath = optValues.getProperty(BatchTool.OBJECTSPATH);
 		modelPath = optValues.getProperty(BatchTool.CMODEL);		
 			
 		if (! BatchTool.argOK(additionsPath)) {
 			System.err.println("additionsPath required");			
 			throw new Exception();
 		}	
 		if (! BatchTool.argOK(objectsPath)) {
 			System.err.println("objectsPath required");			
 			throw new Exception();
 		}
 		if (! BatchTool.argOK(xformPath)) {
 			System.err.println("xformPath required");			
 			throw new Exception();
 		}
 
       }
 	
 	private static final String[] padding = {"", "0", "00", "000"};
 	private static final String leftPadded (int i, int n) throws Exception {
 		if ((n > 3) || (n < 0) || (i < 0) || (i > 999)) {			
 			throw new Exception();			
 		}
 	        int m = (i > 99) ? 3 : (i > 9) ? 2 : 1;
 		int p = n - m;
 		return padding[p] + Integer.toString(i);
 	}
 
 	
 	private boolean good2go = false;
 	
 	final void prep() throws Exception {		
 		good2go = true;
 	}
 	
 	private Vector keys = null;
 		
 	/* package */ Vector getKeys() {
 		return keys;
 	}	
 	
 	final void process() throws TransformerConfigurationException, Exception {
     		//System.err.println("before TransformerFactory.newInstance()"); //<<==		
     		//System.err.println("xformPath=[" + xformPath + "]"); //<<==		
 		//SAXTransformerFactory tfactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();		
 		TransformerFactory tfactory = TransformerFactory.newInstance(); //try this from RunXSLT		
     		//System.err.println("after TransformerFactory.newInstance(); tf is null?=" + (tfactory == null)); //<<==
 		keys = new Vector();		
 		if (good2go) {
 			int count = 0;
 			File file4catch = null;
 			int files4catch = 0;
 			int badFileCount = 0;
 			int succeededBuildCount = 0;
 			int failedBuildCount = 0;			
 			try {
 				File[] files = null; {
 // System.err.println("additions path " + additionsPath);
 					File additionsDirectory = new File(additionsPath);
 					files = additionsDirectory.listFiles();
 				}			
 				//int badFileCount = 0;
 				files4catch = files.length;
 				for (int i = 0; i < files.length; i++) {
 //System.err.println("another it");
 					file4catch = files[i]; 
 					if (! files[i].isFile()) {
 						badFileCount++;
 						System.err.println("additions directory contains unexpected directory or file: " + files[i].getName());
 					} else {
     		//System.err.println("before tfactory.newTransformer()"); //<<==
 
 		File f = new File(xformPath);
 		//System.err.println("File " + xformPath + " exists=[" + (f.exists()) + "]");	
 		
 		StreamSource ss = new StreamSource(f);
 		//System.err.println("ss null=[" + (ss == null) + "]");
 /*
 		Reader r = ss.getReader();
 		System.err.println("r null=[" + (r == null) + "]");
 		BufferedReader br = new BufferedReader(r);
 		System.err.println("br null=[" + (br == null) + "]");
 		String st = br.readLine();
 		System.err.println("st null=[" + (st == null) + "]");		
 		System.err.println("first line[" +  st + "]");
     		System.err.println("after dummy SS"); //<<==	
     		System.err.println("support?=[" + tfactory.getFeature(StreamSource.FEATURE) + "]"); //<<==
 		*/
 		
 	
 		transformer = tfactory.newTransformer(ss); //xformPath
 		
 		//System.err.println("after tfactory.newTransformer(); is transformer null? " + (transformer == null));	
 	
 						GregorianCalendar calendar = new GregorianCalendar();
 						String year = Integer.toString(calendar.get(Calendar.YEAR)); 
 						String month = leftPadded(1+ calendar.get(Calendar.MONTH),2); 
 						String dayOfMonth = leftPadded(calendar.get(Calendar.DAY_OF_MONTH),2);
 						String hourOfDay = leftPadded(calendar.get(Calendar.HOUR_OF_DAY),2);
 						String minute = leftPadded(calendar.get(Calendar.MINUTE),2);
 						String second = leftPadded(calendar.get(Calendar.SECOND),2);
 						transformer.setParameter("date",
 						year + "-" + month + "-" + dayOfMonth + 
 						"T" + hourOfDay + ":" + minute + ":" + second);
 						//"2002-05-20T06:32:00"
 						//System.err.println("about to xform " + count++);
 //System.err.println(">>>>>>>>>>>>" + files[i].getPath());
 //System.err.println("objectsPath [" + objectsPath + "]");
 /*
 for (int bb=0; bb<objectsPath.length(); bb++) {
 	char c = objectsPath.charAt(bb);
 	int n = Character.getNumericValue(c);
 	boolean t = Character.isISOControl(c);
 	System.err.print("["+c+"("+n+")"+t+"]");
 }
 */
 //System.err.println("File.separator " + File.separator);
 //System.err.println("files[i].getName() " + files[i].getName());
 		//System.err.println("before calling xform");	
 
 						String temp = "file:///" + files[i].getPath(); //(files[i].getPath()).replaceFirst("C:", "file:///C:");
 				//System.err.println("path is [" + temp); //files[i].getPath());							
 						transformer.setParameter("subfilepath",temp); //files[i].getPath());
 						
 //System.out.println("fis");
 						FileInputStream fis = new FileInputStream(modelPath);
 //System.out.println("fos");
 						FileOutputStream fos = new FileOutputStream(objectsPath + File.separator + files[i].getName());
 //System.out.println("fos");						
 						try {
 							//transform(new FileInputStream(files[i]), new FileOutputStream (objectsPath + File.separator + files[i].getName()));
 							transform(fis, fos);
 //System.out.println("Fedora METS XML created at " + files[i].getName());
 							succeededBuildCount++;	
 //System.out.println("before keys.add()");
 							keys.add(files[i].getName());
 //System.out.println("after keys.add()");
 						} catch (Exception e) {
 							//for now, follow processing as-is and throw out rest of batch
 							throw e;
 						} finally { // so that files are available to outside editor during batchtool client use
 //System.out.println("before fis.close");
 							fis.close();
 //System.out.println("before fos.close");
 							fos.close();
 //System.out.println("after fos.close()");
 						}
 					}
 				}
 			} catch (Exception e) {
 				System.err.println("Fedora METS XML failed for " + file4catch.getName());
 				System.err.println("exception: " + e.getMessage() + " , class is " + e.getClass());
 				failedBuildCount++;
 			} finally {
 			}
 			System.err.println("\n" + "Batch Build Summary");		
 			System.err.println("\n" + (succeededBuildCount + failedBuildCount + badFileCount) + " files processed in this batch");
 			System.err.println("\t" + succeededBuildCount + " Fedora METS XML documents successfully created");
 			System.err.println("\t" + failedBuildCount + " Fedora METS XML documents failed");
 			System.err.println("\t" + badFileCount + " unexpected files in directory");
 			System.err.println("\t" + (files4catch - (succeededBuildCount + failedBuildCount + badFileCount)) + " files ignored after error");				
 		}
 	}	
 	
 	public static final void main(String[] args) {
 		try {
 			Properties miscProperties = new Properties();		
 			miscProperties.load(new FileInputStream("c:\\batchdemo\\batchtool.properties"));			
 			BatchXforms batchXforms = new BatchXforms(miscProperties);
 			batchXforms.prep();
 			batchXforms.process();
 		} catch (Exception e) {
 		}
 	}
 
     public void transform(InputStream sourceStream, OutputStream destStream) throws TransformerException, TransformerConfigurationException {
     	//System.err.println("before transformer.transform()");
 	transformer.transform(new StreamSource(sourceStream), new StreamResult(destStream));
     	//System.err.println("after transformer.transform()");
     }
 	
 }
 
 
