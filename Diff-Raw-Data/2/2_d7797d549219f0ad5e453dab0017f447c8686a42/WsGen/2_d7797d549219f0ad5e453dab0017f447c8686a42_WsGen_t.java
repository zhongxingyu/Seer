 package org.codehaus.xfire.gen;
 
 import org.apache.tools.ant.BuildException;
 
 /**
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a> 
  * Helper class  which allows to run wsgen from command line.
  * 
  */
 public class WsGen {
 
 	
     private static void usage() {
 		System.out
 				.print("Usage: wsgen -wsdl wsdl.file -o outputDirectory [-p package] [-b binding] " +
                        "[-r profile] [-e externalBinding] [-u baseURI] [-overwrite true/false] " +
                         "[-x true/false] [-ss true/false] [-w true/false] \n");
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
 		String externalBindings = null;
 		String baseURI = null;
         boolean overwrite = false;
         boolean explicit = false;
         boolean serverStubs = true;
         boolean forceBare = false;
         
        if(args.length>0 && "-h".equals(args[0])){
            printHelpMessage();
            return;
        }
         
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
 			if("-e".equals(param)){
 				externalBindings = value;
 			}
 			if("-u".equals(param)){
 				baseURI = value;
 			}
 			if("-r".equals(param)){
 				profile = value;
 			}
             if ("-x".equals(param)) {
                 explicit = Boolean.parseBoolean(value);
             }
             if("-overwrite".equals(param)){
                 overwrite=Boolean.parseBoolean(value);
             }
             if("-ss".equals(param)){
                 serverStubs = Boolean.parseBoolean(value);
             }
             if("-w".equals(param)){
                 forceBare = Boolean.parseBoolean(value);
             }
             
 		}
 
 		if (wsdl == null) {
 			missingParam("wsdl");
 			usage();
 			return;
 		}
 		if (outputDirectory == null) {
 			outputDirectory = ".";
 			System.out.print("Output directory not specified. Using current.\n");
 		}
               
         
 		System.out.print("Running WsGen...\n");
 		System.out.print("wsdl    : " + wsdl + "\n");
 		System.out.print("package : " + _package + "\n");
 		System.out.print("output  : " + outputDirectory + "\n");
 		System.out.print("binding : " + (binding==null?"":binding) + "\n");
 		System.out.print("externalBindings : " + (externalBindings == null?"" : externalBindings) + "\n" );
 		System.out.print("baseURI : " + (baseURI == null?"" : baseURI)+ "\n");
 		System.out.print("profile : " + (profile == null?"" : profile)+ "\n");
         System.out.print("explictAnnotation : " + explicit+ "\n");
         System.out.print("overwrite : " + overwrite+ "\n");
         System.out.print("serverStub : " + serverStubs  + "\n");
         System.out.print("forceBare : " + forceBare+ "\n");
         
 
 		Wsdl11Generator generator = new Wsdl11Generator();
 		generator.setDestinationPackage(_package);
 		generator.setOutputDirectory(outputDirectory);
 		generator.setWsdl(wsdl);
 		generator.setExplicitAnnotation(explicit);
         generator.setOverwrite(overwrite);
         generator.setGenerateServerStubs(serverStubs);
         generator.setForceBare(forceBare);
         
 		if (binding != null)
 			generator.setBinding(binding);
 		if (profile != null)
 			generator.setProfile(profile);
 		
 		if( baseURI!= null )
 			generator.setBaseURI(baseURI);
 			
 		if(externalBindings != null )
 			generator.setExternalBindings(externalBindings);
 			
 
 		try {
 			generator.generate();
 			System.out.print("Done.\n");
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new BuildException(e);
 		}
 
 	}
 
 
     private static void printHelpMessage()
     {
         StringBuffer buffer = new StringBuffer();
         buffer.append("wsgen options:");
         buffer.append("\n");
         buffer.append("-wsdl : location of wsdl file, can be URL or file path ");
         buffer.append("\n");
         buffer.append("-o : output directory");
         buffer.append("\n");
         buffer.append("-p : package to use for generated files");
         buffer.append("\n");
         buffer.append("-b : binding to use - jaxb or xmlbeans");
         buffer.append("\n");
         buffer.append("-r : profile");
         buffer.append("\n");
         buffer.append("-e : external binding");
         buffer.append("\n");
         buffer.append("-u : base uri");
         buffer.append("\n");
         buffer.append("-overwrite : determine if existing classes should be overwriten ( true/false )");
         buffer.append("\n");
         buffer.append("-x : explicit");
         buffer.append("\n");
         buffer.append("-ss : generate server stubs");
         buffer.append("\n");
         buffer.append("-w : force bare instead of wrapped invocation style");
         buffer.append("\n");
         buffer.append("-h : print this help message");
         buffer.append("\n");
         
         System.out.print(buffer.toString());
         
     }
 
 }
