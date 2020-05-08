 package org.codehaus.xfire.gen;
 
 import org.apache.tools.ant.BuildException;
 
 /**
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a> Helper class
  *         which allows to run wsgen from command line.
  * 
  * These classes are required to run wsgen with jaxb binding:
  *  wstx-asl-2.9.jar
  *  commons-logging-1.0.4.jar 
  *  xfire-jsr181-api-1.0-M1.jar 
  *  jdom-1.0.jar
  *  XmlSchema-1.0.jar 
  *  jaxb-xjc-2.0-ea3.jar 
  *  activation-1.0.2.jar
  *  wsdl4j-1.5.2.jar
  *  stax-api-1.0.jar 
  *  jaxb-api-2.0-ea3.jar 
  *  xfire-all-1.1-SNAPSHOT.jar
  *  sun-jaxws-api-2.0-ea3.jar
  *  xsdlib-20050913.jar
  *  jaxb-impl-2.0-ea3.jar
  *  ant-1.6.5.jar
  * 
  */
 public class WsGen {
 
 	private static void usage() {
 		System.out
 				.print("Usage: wsgen -wsdl wsdl.file -o outputDirectory -p package [-b binding] \n");
 	}
 
 	private static void missingParam(String param) {
 		System.out.print("Missing param : " + param + "\n");
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String _package = null;
 		String outputDirectory = null;
 		String wsdl = null;
 		String binding = null;
 		String profile = null;
 		if (args.length < 3) {
 			usage();
 			return;
 		}
 		for (int i = 0; i < args.length; i += 2) {
 			String param = args[i];
 			String value = args[i + 1];
 			param = param.toLowerCase().trim();
 			value = value.trim();
 			if ("-wsdl".equals(param)) {
 				wsdl = value;
 			}
 			if ("-o".equals(param)) {
 				outputDirectory = value;
 			}
 			if ("-p".equals(param)) {
 				_package = value;
 			}
 			if ("-b".equals(param)) {
 				binding = value;
 			}
 		}
 
 		if (_package == null) {
 			missingParam("package");
 			usage();
 			return;
 		}
 
 		if (wsdl == null) {
 			missingParam("wsdl");
 			usage();
 			return;
 		}
 		if (outputDirectory == null) {
 			outputDirectory = ".";
 			System.out
 					.print("Output directory not specified. Using current.\n");
 		}
 		System.out.print("Running WsGen...\n");
 		System.out.print("wsdl    : " + wsdl + "\n");
 		System.out.print("package : " + _package + "\n");
 		System.out.print("output  : " + outputDirectory + "\n");
		System.out.print("binding : " + binding + "\n");
 
 		Wsdl11Generator generator = new Wsdl11Generator();
 		generator.setDestinationPackage(_package);
 		generator.setOutputDirectory(outputDirectory);
 		generator.setWsdl(wsdl);
 
 		if (binding != null)
 			generator.setBinding(binding);
 		if (profile != null)
 			generator.setProfile(profile);
 
 		try {
 			generator.generate();
 			System.out.print("Done.\n");
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new BuildException(e);
 		}
 
 	}
 
 }
