 // Copyright (c) 2002 Sean Kelly
 // All rights reserved.
 // 
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are
 // met:
 // 
 // 1. Redistributions of source code must retain the above copyright
 //    notice, this list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright
 //    notice, this list of conditions and the following disclaimer in the
 //    documentation and/or other materials provided with the
 //    distribution.
 // 
 // THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 // IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 // PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 // BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 // CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 // SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 // BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 // OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 // IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 // $Id$
 
 package net.sourceforge.xmlresume;
 
 import java.io.OutputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileOutputStream;
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.MalformedURLException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.sax.SAXSource;
 import org.xml.sax.InputSource;
 import java.util.Properties;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.StringTokenizer;
 import java.util.Iterator;
 
 /**
  * Test the XML Resume Library transformations.
  *
  * @author Kelly
  */
 class ResumeTest {
 	/**
 	 * Read the properties file.
 	 *
 	 * @return Properties initialized from <code>ResumeTest.properties</code>.
 	 * @throws IOException if an error occurs.
 	 */
 	private static Properties readProperties() throws IOException {
 		Properties props = new Properties();
 		BufferedInputStream in = new BufferedInputStream(ResumeTest.class.getResourceAsStream("ResumeTest.properties"));
 		props.load(in);
 		in.close();
 		return props;
 	}
 
 	/**
 	 * Generate all possible combinations of parameter settings defined in the properites.
 	 *
 	 * @param props Test properties.
 	 * @return A {@link List} of {@link List}s of {@link Setting}s.
 	 */
 	private static List generateParameterSettings(Properties props) {
 		// Create a set of all parameters defined in the properties file.
 		List parameters = new ArrayList();
 		for (StringTokenizer t = new StringTokenizer(props.getProperty("parameters"), ","); t.hasMoreTokens();) {
 			String paramKey = t.nextToken().trim();
 			String name = props.getProperty(paramKey + ".name");
 			Parameter p = new Parameter(name);
 			for (StringTokenizer u = new StringTokenizer(props.getProperty(paramKey + ".values"), ",");
 			        u.hasMoreTokens();)
 				p.addValue(u.nextToken().trim());
 			parameters.add(p);
 		}
 
 		// Create a set of all combinations of parameter settings.
 		return generateSettings(parameters);
 	}
 
 	/**
 	 * Get the XML input files defined in the test properties.
 	 *
 	 * @param Test properties.
 	 * @return A {@link List} of {@link URL}s to XML resumes.
 	 */
 	private static List getInputURLs(Properties props) {
   		List inputURLs = new ArrayList();
   		for (StringTokenizer t = new StringTokenizer(props.getProperty("testdata"), ","); t.hasMoreTokens();)
   			inputURLs.add(ResumeTest.class.getResource("testdata/" + t.nextToken().trim() + ".xml"));
 		return inputURLs;
 	}
 
 	/**
 	 * Generate a formatted resume output file name.
 	 *
 	 * @param url XML input file.
 	 * @param stylesheetKey Stylesheet identifier.
 	 * @param settings A {@link List} of {@link List}s of {@link Setting}s. 
 	 * @param extension Filename extension for the formatted output file, including the dot.
 	 * @param dir In what directory to create the formatted output file.
 	 * @return A resume output file name.
 	 */
 	private static File generateOutputFile(URL url, String stylesheetKey, List settings, String extension, File dir) {
 		String inputFile = url.getFile().substring(0, url.getFile().lastIndexOf(".xml"))
 			.substring(url.getFile().lastIndexOf('/'));
 		StringBuffer settingsLabel = new StringBuffer();
 		for (Iterator i = settings.iterator(); i.hasNext();) {
 			Setting s = (Setting) i.next();
 			settingsLabel.append(s).append('_');
 		}
 		return new File(dir, inputFile + "_" + stylesheetKey + "_"
 			+ settingsLabel.substring(0, settingsLabel.length() - 1) + extension);
 	}
 
 	/**
 	 * Get a resource name for a target (formatted, standard reference copy) resume.
 	 *
 	 * @param url Corresponding XML resume input URL.
 	 * @param stylesheetKey Stylesheet identifier.
 	 * @param settings A {@link List} of {@link List}s of {@link Setting}s. 
 	 * @param extension Filename extension for the target file, including the dot.
 	 * @return A resource key for the target resume.
 	 */
 	private static String getTargetResource(URL url, String stylesheetKey, List settings, String extension) {
 		String inputFile = url.getFile().substring(0, url.getFile().lastIndexOf(".xml"))
 			.substring(url.getFile().lastIndexOf('/'));
 		StringBuffer settingsLabel = new StringBuffer();
 		for (Iterator i = settings.iterator(); i.hasNext();) {
 			Setting s = (Setting) i.next();
 			settingsLabel.append(s).append('_');
 		}
 		return "targetData" +  inputFile + "_" + stylesheetKey + "_"
 			+ settingsLabel.substring(0, settingsLabel.length() - 1) + extension;
 	}
 
 	/**
 	 * Generate all test data.  This method creates all of the target formatted
 	 * resumes that become the new reference standard editions for CVS.  You need to
 	 * do this when you make changes to the XML Resume Library that affects
 	 * formatting, such as adding new sections, or new formatting options.
 	 *
 	 * <p>After doing so and verifying the results by hand, you can overwrite the CVS
 	 * copies with the copies you just generated.  They then become the new standard
 	 * reference resumes for nightly testing and for modifications that don't affect
 	 * formatting, like refactoring.
 	 *
 	 * @param outputDir Where to write the test data.
 	 * @throws Throwable if an error occurs.
 	 */
 	private static void generateTestData(File outputDir) throws Throwable {
 		Properties props = readProperties();
   		TransformerFactory transformerFactory = TransformerFactory.newInstance();
 		for (Iterator i = getInputURLs(props).iterator(); i.hasNext();) {
 			URL inputURL = (URL) i.next();
 			for (StringTokenizer t = new StringTokenizer(props.getProperty("stylesheets"), ","); t.hasMoreTokens();) {
 				String stylesheetKey = t.nextToken().trim();
				URL stylesheetURL = ResumeTest.class.getResource("/xsl/output/" + props.getProperty(stylesheetKey
 					+ ".file"));
 				if (stylesheetURL == null)
 					throw new IllegalStateException("Stylesheet " + stylesheetKey + " missing");
 				Transformer transformer
 					= transformerFactory.newTransformer(new StreamSource(stylesheetURL.toString()));
 				String ext = props.getProperty(stylesheetKey + ".ext");
 				for (Iterator j = generateParameterSettings(props).iterator(); j.hasNext();) {
 					transformer.clearParameters();
 					List settings = (List) j.next();
 					for (Iterator k = settings.iterator(); k.hasNext();) {
 						Setting setting = (Setting) k.next();
 						setting.applyTo(transformer);
 					}
 					File outputFile = generateOutputFile(inputURL, stylesheetKey, settings, ext, outputDir);
 					System.err.println("Generating " + outputFile);
   					OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
   					transformer.transform(new StreamSource(inputURL.toString()), new StreamResult(out));
   					out.close();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Run the XML resume library transformation tests.
 	 *
 	 * @throws Throwable if an error occurs.
 	 */
 	private static void test() throws Throwable {
 		Properties props = readProperties();
 		TransformerFactory transformerFactory = TransformerFactory.newInstance();
 		for (Iterator i = getInputURLs(props).iterator(); i.hasNext();) {
 			URL inputURL = (URL) i.next();
 			for (StringTokenizer t = new StringTokenizer(props.getProperty("stylesheets"), ","); t.hasMoreTokens();) {
 				String stylesheetKey = t.nextToken().trim();
 				URL stylesheetURL = ResumeTest.class.getResource("/xsl/" + props.getProperty(stylesheetKey
 					+ ".file"));
 				if (stylesheetURL == null)
 					throw new IllegalStateException("Stylesheet " + stylesheetKey + " missing");
 				Transformer transformer
 					= transformerFactory.newTransformer(new StreamSource(stylesheetURL.toString()));
 				String ext = props.getProperty(stylesheetKey + ".ext");
 				eachSetting:
 				for (Iterator j = generateParameterSettings(props).iterator(); j.hasNext();) {
 					transformer.clearParameters();
 					List settings = (List) j.next();
 					for (Iterator k = settings.iterator(); k.hasNext();) {
 						Setting setting = (Setting) k.next();
 						setting.applyTo(transformer);
 					}
 					ByteArrayOutputStream out = new ByteArrayOutputStream();
   					transformer.transform(new StreamSource(inputURL.toString()), new StreamResult(out));
   					out.close();
 					String targetKey = getTargetResource(inputURL, stylesheetKey, settings, ext);
 					InputStream in = new BufferedInputStream(ResumeTest.class.getResourceAsStream(targetKey));
 					byte[] output = out.toByteArray();
 					int index = 0;
 					int c;
 					while ((c = in.read()) != -1) {
 						if (index == output.length) {
 							in.close();
 							fail("output too short (only " + output.length + " bytes)", inputURL,
 								stylesheetURL, settings, targetKey);
 							continue eachSetting;
 						}
 						if (((byte) c) != output[index]) {
 							in.close();
 							fail("byte " + index + " not equal; expected " + ((byte)c) + " but got "
 								+ output[index], inputURL, stylesheetURL, settings, targetKey);
 							continue eachSetting;
 						}
 						++index;
 					}
 					if (index < output.length) 
 						fail("output too long (" + output.length + " bytes)", inputURL, stylesheetURL,
 							settings, targetKey);
 					in.close();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Log a failure of an XML resume library transformation.
 	 *
 	 * This method is called when the result of the XSL file results in a
 	 * transformation that no longer matches the target.
 	 *
 	 * @param msg Message describing what went wrong.
 	 * @param input XML input that's being transformed.
 	 * @param stylesheet XSL stylesheet doing the transformation.
 	 * @param settings A {@link List} of {@link List}s of {@link Setting}s. 
 	 * @param target Name of the target that contains the correct data.
 	 */
 	private static void fail(String msg, URL input, URL stylesheet, List settings, String target) {
 		System.err.println("TEST FAIL: " + msg);
 		System.err.println("  Transforming: " + input);
 		System.err.println("    Stylesheet: " + stylesheet);
 		System.err.println("    Parameters: " + settings.toString().substring(1, settings.toString().length() - 1));
 		System.err.println("        Target: " + target);
 	}
 
 	/**
 	 * Generate settings for the given parameters.
 	 *
 	 * @param parameters A {@link List} of {@link Parameter}s.
 	 * @return A {@link List} of {@link List}s of {@link Setting}s of all possible combinations of <var>parameters</var>.
 	 */
 	private static List generateSettings(List parameters) {
 		if (parameters.isEmpty()) return Collections.EMPTY_LIST;
 		Parameter parameter = (Parameter) parameters.remove(0);
 		List otherSettings = generateSettings(parameters);
 		List newSettings = new ArrayList();
 		for (Iterator i = parameter.getValues().iterator(); i.hasNext();) {
 			String value = (String) i.next();
 			Setting setting = new Setting(parameter, value);
 			if (otherSettings.isEmpty()) {
 				List settings = new ArrayList();
 				settings.add(setting);
 				newSettings.add(settings);
 			} else for (Iterator j = otherSettings.iterator(); j.hasNext();) {
 				List settings = new ArrayList((List) j.next());
 				settings.add(setting);
 				newSettings.add(settings);
 			}
 		}
 		return newSettings;
 	}
 
 	/**
 	 * Run the resume test to either generate the target test data or to run the regression tests.
 	 *
 	 * @param argv Command-line arguments.
 	 * @throws Throwable if an error occurs.
 	 */
 	public static void main(String[] argv) throws Throwable {
 		if (argv.length < 1 || argv.length > 2) {
 			System.err.println("Usage: -test");
 			System.err.println("   or: -generate <dir>");
 			System.exit(1);
 		}
 		if ("-test".equals(argv[0])) {
 			test();
 			System.out.println("All tests pass.");
 		} else if ("-generate".equals(argv[0])) {
 			if (argv.length != 2) {
 				System.err.println("Output directory required with -generate");
 				System.exit(1);
 			}
 			File dir = new File(argv[1]);
 			dir.mkdir();
 			generateTestData(dir);
 		} else {
 			System.err.println("Specify either -test or -generate");
 			System.exit(1);
 		}
 		System.exit(0);
 	}
 }
