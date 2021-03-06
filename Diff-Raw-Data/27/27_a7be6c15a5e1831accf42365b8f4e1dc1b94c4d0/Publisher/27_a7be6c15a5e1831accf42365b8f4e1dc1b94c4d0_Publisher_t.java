 package org.hl7.fhir.tools.publisher;
 
 /*
  Copyright (c) 2011-2012, HL7, Inc
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without modification, 
  are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, this 
  list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, 
  this list of conditions and the following disclaimer in the documentation 
  and/or other materials provided with the distribution.
  * Neither the name of HL7 nor the names of its contributors may be used to 
  endorse or promote products derived from this software without specific 
  prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  POSSIBILITY OF SUCH DAMAGE.
 
  */
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.xml.XMLConstants;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
 import org.hl7.fhir.definitions.Config;
 import org.hl7.fhir.definitions.generators.specification.DictHTMLGenerator;
 import org.hl7.fhir.definitions.generators.specification.DictXMLGenerator;
 import org.hl7.fhir.definitions.generators.specification.ProfileGenerator;
 import org.hl7.fhir.definitions.generators.specification.SchematronGenerator;
 import org.hl7.fhir.definitions.generators.specification.TerminologyNotesGenerator;
 import org.hl7.fhir.definitions.generators.specification.XmlSpecGenerator;
 import org.hl7.fhir.definitions.generators.xsd.SchemaGenerator;
 import org.hl7.fhir.definitions.model.Definitions;
 import org.hl7.fhir.definitions.model.Example;
 import org.hl7.fhir.definitions.model.ProfileDefn;
 import org.hl7.fhir.definitions.model.RegisteredProfile;
 import org.hl7.fhir.definitions.model.ResourceDefn;
 import org.hl7.fhir.definitions.parsers.SourceParser;
 import org.hl7.fhir.definitions.validation.ModelValidator;
 import org.hl7.fhir.definitions.validation.ProfileValidator;
 import org.hl7.fhir.definitions.validation.UMLValidator;
 import org.hl7.fhir.instance.formats.AtomComposer;
 import org.hl7.fhir.instance.formats.XmlComposer;
 import org.hl7.fhir.instance.formats.XmlParser;
 import org.hl7.fhir.instance.formats.XmlParserBase;
 import org.hl7.fhir.instance.formats.XmlParserBase.ResourceOrFeed;
 import org.hl7.fhir.instance.model.AtomEntry;
 import org.hl7.fhir.instance.model.AtomFeed;
 import org.hl7.fhir.instance.model.Profile;
 import org.hl7.fhir.tools.publisher.implementations.CSharpGenerator;
 import org.hl7.fhir.tools.publisher.implementations.DelphiGenerator;
 import org.hl7.fhir.tools.publisher.implementations.ECoreOclGenerator;
 import org.hl7.fhir.tools.publisher.implementations.JavaGenerator;
 import org.hl7.fhir.utilities.CSVProcessor;
 import org.hl7.fhir.utilities.IniFile;
 import org.hl7.fhir.utilities.Logger;
 import org.hl7.fhir.utilities.SchemaInputSource;
 import org.hl7.fhir.utilities.TextFile;
 import org.hl7.fhir.utilities.Utilities;
 import org.hl7.fhir.utilities.ZipGenerator;
 import org.hl7.fhir.utilities.xhtml.NodeType;
 import org.hl7.fhir.utilities.xhtml.XhtmlComposer;
 import org.hl7.fhir.utilities.xhtml.XhtmlDocument;
 import org.hl7.fhir.utilities.xhtml.XhtmlNode;
 import org.hl7.fhir.utilities.xhtml.XhtmlParser;
 import org.hl7.fhir.utilities.xml.XhtmlGenerator;
 import org.hl7.fhir.utilities.xml.XmlGenerator;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.ls.LSInput;
 import org.w3c.dom.ls.LSResourceResolver;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
  * This is the entry point for the publication method for FHIR The general order
  * of publishing is Check that everything we expect to find is found Load the
  * page.getDefinitions() Produce the specification 1. reference implementations
  * 2. schemas 4. final specification Validate the XML
  * 
  * @author Grahame
  * 
  */
 public class Publisher {
 
 	private SourceParser prsr;
 	private ChmMaker chm;
 	private PageProcessor page = new PageProcessor();
 	private BookMaker book;
 
 	private boolean isInternalRun;
 	private boolean isGenerate;
 	private AtomFeed profileFeed;
 
 	public static void main(String[] args) throws Exception {
 		//
 		Publisher pub = new Publisher();
 		pub.isInternalRun = !(args.length > 1 && hasParam(args, "-web"));
     pub.isGenerate = !(args.length > 1 && hasParam(args, "-nogen"));
 		pub.execute(args[0]);
 	}
 
 	private static boolean hasParam(String[] args, String param) {
     for (String a : args) 
       if (a.equals(param))
         return true;
     return false;
   }
 
   public void execute(String folder) throws Exception {
 
 		log("Publish FHIR in folder "
 				+ folder
 				+ (isInternalRun ? " [internal development mode - including the sandbox]"
 						: ""));
 
 		registerReferencePlatforms();
 
 		if (initialize(folder)) {
       if (isGenerate) {
         Utilities.clearDirectory(page.getFolders().dstDir);
         Utilities.createDirectory(page.getFolders().dstDir + "html");
         Utilities.createDirectory(page.getFolders().dstDir + "examples");
       }
       prsr.parse(isInternalRun, page.getGenDate(), page.getVersion());
       
 			if (validate()) 
 			{
 			  if (isGenerate) {
 			    String eCorePath =  page.getFolders().dstDir + "ECoreDefinitions.xml";
 			    generateECore(prsr.getECoreParseResults(), eCorePath);
 			    produceSpecification(eCorePath);
 			  }
 				validateXml();
 				System.out.println("Finished publishing FHIR");
 			} else
 				System.out.println("Didn't publish FHIR due to errors");
 		}
 	}
 
 
   private void generateECore(org.hl7.fhir.definitions.ecore.fhir.Definitions eCoreDefinitions, String filename) throws IOException {
 		 Resource resource = new XMLResourceImpl();
 		 Map<String, String> options = new HashMap<String, String>();
 		 options.put(XMLResource.OPTION_ENCODING, "UTF-8");
 		 options.put(XMLResource.OPTION_XML_VERSION, "1.0");
 		
 		 resource.getContents().add(eCoreDefinitions);
 		 resource.save(new FileOutputStream(filename), options);
 	}
 
 	private void registerReferencePlatforms() {
 		page.getReferenceImplementations().add(new DelphiGenerator());
 		page.getReferenceImplementations().add(new JavaGenerator());
 		page.getReferenceImplementations().add(new CSharpGenerator());
 		page.getReferenceImplementations().add(new ECoreOclGenerator());
 	}
 
 	private boolean initialize(String folder) throws Exception {
 		page.setDefinitions(new Definitions());
 		page.setFolders(new FolderManager(folder));
 
 		System.out.println("Checking Source");
 
 		List<String> errors = new ArrayList<String>();
 
 		Utilities.checkFolder(page.getFolders().rootDir, errors);
 		if (Utilities.checkFile("required", page.getFolders().rootDir,
 				"publish.ini", errors)) {
 			Utilities.checkFile("required", page.getFolders().srcDir,
 					"navigation.xml", errors);
 			page.setIni(new IniFile(page.getFolders().rootDir + "publish.ini"));
 			page.setVersion(page.getIni().getStringProperty("FHIR", "version"));
 
 			prsr = new SourceParser((Logger) page, folder,
 					page.getDefinitions());
 			prsr.checkConditions(errors);
 
 			Utilities.checkFolder(page.getFolders().xsdDir, errors);
 			for (PlatformGenerator gen : page.getReferenceImplementations())
 				Utilities.checkFolder(page.getFolders().implDir(gen.getName()),
 						errors);
 			Utilities.checkFolder(page.getFolders().umlDir, errors);
 			Utilities.checkFile("required", page.getFolders().srcDir, "fhir-all.xsd", errors);
 			Utilities.checkFile("required", page.getFolders().srcDir, "header.htm", errors);
 			Utilities.checkFile("required", page.getFolders().srcDir, "footer.htm", errors);
 			Utilities.checkFile("required", page.getFolders().srcDir, "template.htm", errors);
 			Utilities.checkFile("required", page.getFolders().srcDir, "template-book.htm", errors);
 			Utilities.checkFile("required", page.getFolders().srcDir, "template-print.htm", errors);
 			Utilities.checkFolder(page.getFolders().dstDir, errors);
 
 			for (String n : page.getIni().getPropertyNames("support"))
 				Utilities.checkFile("support", page.getFolders().srcDir, n, errors);
 			for (String n : page.getIni().getPropertyNames("images"))
 				Utilities.checkFile("image", page.getFolders().imgDir, n, errors);
 			for (String n : page.getIni().getPropertyNames("schema"))
 				Utilities.checkFile("schema", page.getFolders().srcDir, n, errors);
 			for (String n : page.getIni().getPropertyNames("pages"))
 				Utilities.checkFile("page", page.getFolders().srcDir, n, errors);
 
 		}
 		if (errors.size() > 0)
 			System.out.println("Unable to publish FHIR specification:");
 		for (String e : errors) {
 			System.out.println(e);
 		}
 		return errors.size() == 0;
 	}
 
 	private boolean validate() throws Exception {
 		log("Validating");
 		ModelValidator val = new ModelValidator(page.getDefinitions());
 
 		List<String> errors = new ArrayList<String>();
 		for (String n : page.getDefinitions().getResources().keySet())
 			errors.addAll(val.check(n, page.getDefinitions().getResources()
 					.get(n)));
 
 		for (String n : page.getDefinitions().getResources().keySet()) {
 			String filename = page.getFolders().srcDir + n + File.separatorChar
 					+ n + "-uml.xml";
 			if (new File(filename).exists()
 					|| !page.getDefinitions().getResources().get(n).isSandbox()) {
 				 List<String> dummyErrors = new ArrayList<String>();
 				new UMLValidator(page.getDefinitions().getResources().get(n)
 						.getRoot(), filename, dummyErrors).validate();
 			}
 		}
 		for (String e : errors)
 			System.out.println(e);
 		return errors.size() == 0;
 	}
 
 	private void produceSpecification(String eCorePath) throws Exception {
 		page.setNavigation(new Navigation());
 		page.getNavigation().parse(page.getFolders().srcDir + "navigation.xml",	isInternalRun);
 		chm = new ChmMaker(page.getNavigation(), page.getFolders(),	page.getDefinitions(), page);
 		book = new BookMaker(page, chm);
 
 		XMIResource resource = new XMIResourceImpl();
 		resource.load( new FileInputStream(eCorePath), null );
 		org.hl7.fhir.definitions.ecore.fhir.Definitions eCoreDefs = (org.hl7.fhir.definitions.ecore.fhir.Definitions)resource.getContents().get(0);	
 		
 		for (PlatformGenerator gen : page.getReferenceImplementations()) 
 		{
 			log("Produce " + gen.getName() + " Reference Implementation");
 		
 			String destDir = page.getFolders().dstDir;
 			String implDir = page.getFolders().implDir(gen.getName());
 			
 			if( !gen.isECoreGenerator() )
 				gen.generate(page.getDefinitions(), destDir, implDir, page.getVersion(), page.getGenDate().getTime(), page);
 			else
 				gen.generate(eCoreDefs, destDir, implDir, page);
 		}
     for (PlatformGenerator gen : page.getReferenceImplementations()) 
     {
       if (gen.doesCompile()) {
         log("Compile " + gen.getName() + " Reference Implementation");
         if (!gen.compile(new ArrayList<String>())) 
           log("Compile " + gen.getName() + " failed");
       }
     }
     
 		log("Produce Schemas");
 		new SchemaGenerator().generate(page.getDefinitions(), page.getIni(), page.getFolders().tmpResDir, page.getFolders().xsdDir, 
 		    page.getFolders().dstDir, page.getFolders().srcDir, page.getVersion(), Config.DATE_FORMAT().format(page.getGenDate().getTime()));
     for (ResourceDefn r : page.getDefinitions().getResources().values()) {
       String n = r.getName().toLowerCase();
       SchematronGenerator sch = new SchematronGenerator(new FileOutputStream(page.getFolders().dstDir + n + ".sch"), page);
       sch.generate(r.getRoot(), page.getDefinitions());
     }
 		new SchematronGenerator(new FileOutputStream(page.getFolders().dstDir+"fhir-atom.sch"), page).generate(page.getDefinitions());
 		produceSchemaZip();
 		log("Produce Specification");
 		produceSpec();
 
 		log("Produce fhir.chm");
 		chm.produce();
 		if (!isInternalRun) {
 			log("Produce HL7 copy");
 			new WebMaker(page.getFolders(), page.getVersion()).produceHL7Copy();
 			log("Produce Archive copy");
 			produceArchive();
 		}
 	}
 
 	private void produceArchive() throws Exception {
 		String target = page.getFolders().rootDir + "archive" + File.separator
 				+ "v" + page.getVersion() + ".zip";
 		File tf = new File(target);
 		if (tf.exists())
 			tf.delete();
 
 		ZipGenerator zip = new ZipGenerator(target);
 
 		int c = 0;
 		String[] files = new File(page.getFolders().dstDir).list();
 		for (String f : files) {
 			File fn = new File(page.getFolders().dstDir + f);
 			if (!fn.isDirectory()) {
 				if (f.endsWith(".htm")) {
 					String src = TextFile.fileToString(fn.getAbsolutePath());
 					String srcn = src
 							.replace(
 									"Warning: FHIR is a draft specification that is still undergoing development prior to balloting as a full HL7 standard",
 									"This is an old version of FHIR retained for archive purposes. Do not use for anything else");
 					if (!srcn.equals(src))
 						c++;
 					srcn = srcn.replace("<body>",
 							"<body><div class=\"watermark\"/>").replace(
 							"<body class=\"book\">",
 							"<body class=\"book\"><div class=\"watermark\"/>");
 					zip.addFileSource(f, srcn);
 					// Utilities.stringToFile(srcn, target+File.separator+f);
 				} else if (f.endsWith(".css")) {
 					String src = TextFile.fileToString(fn.getAbsolutePath());
 					src = src.replace("#fff", "lightcyan");
 					zip.addFileSource(f, src);
 					// Utilities.stringToFile(srcn, target+File.separator+f);
 				} else
 					zip.addFileName(f, fn.getAbsolutePath());
 			} else {
 				// used to put stuff in sub-directories. clean them out if they
 				// still exist
 				Utilities.clearDirectory(fn.getAbsolutePath());
 				fn.delete();
 			}
 		}
 		if (c < 3)
 			throw new Exception("header note replacement in archive failed");
 		zip.close();
 	}
 
 	private void produceSpec() throws Exception {
 		for (String n : page.getIni().getPropertyNames("support"))
 			Utilities.copyFile(new File(page.getFolders().srcDir + n),
 					new File(page.getFolders().dstDir + n));
 		for (String n : page.getIni().getPropertyNames("images"))
 			Utilities.copyFile(new File(page.getFolders().imgDir + n),
 					new File(page.getFolders().dstDir + n));
 
 		profileFeed = new AtomFeed();
 		profileFeed.setId("http://hl7.org/fhir/profile/resources");
		profileFeed.setTitle("Resources as Profiles");
		profileFeed.setLink("http://hl7.org/implement/standards/fhir/profiles-resources.xml");
 		for (ResourceDefn n : page.getDefinitions().getResources().values())
 			produceResource(n);
 		new AtomComposer().compose(new FileOutputStream(page.getFolders().dstDir + "profiles-resources.xml"), profileFeed, true, false);
 		Utilities.copyFile(new File(page.getFolders().dstDir + "profiles-resources.xml"), new File(page.getFolders().dstDir +"examples"+File.separator+ "profiles-resources.xml"));
 		cloneToXhtml("profiles-resources", "Base Resources defined as profiles (implementation assistance, for derivation and product development)");		
 		for (String n : page.getIni().getPropertyNames("pages"))
 			producePage(n);
 
 		for (String n : page.getDefinitions().getProfiles().keySet())
 			produceProfile(n, page.getDefinitions().getProfiles().get(n));
 
 		produceCombinedDictionary();
 		Utilities.copyFile(new File(page.getFolders().umlDir + "fhir.eap"),
 				new File(page.getFolders().dstDir + "fhir.eap"));
 		// todo - collect and zip the xmi files
 		// Utilities.copyFile(new File(page.getFolders().umlDir + "fhir.xmi"),
 		// new File(page.getFolders().dstDir + "fhir.xmi"));
 
 		ZipGenerator zip = new ZipGenerator(page.getFolders().dstDir + "examples.zip");
     zip.addFiles(page.getFolders().dstDir+"examples"+File.separator, "", null);
     zip.close();
 
 		
 		produceZip();
 		book.produce();
 
 	}
 
 	private void produceZip() throws Exception {
 		File f = new File(page.getFolders().dstDir + "fhir-spec.zip");
 		if (f.exists())
 			f.delete();
 		ZipGenerator zip = new ZipGenerator(page.getFolders().tmpResDir
 				+ "fhir-spec.zip");
 		zip.addFiles(page.getFolders().dstDir, "", null);
 		zip.close();
 		Utilities.copyFile(new File(page.getFolders().tmpResDir
 				+ "fhir-spec.zip"), f);
 	}
 
 	private void produceSchemaZip() throws Exception {
 		File f = new File(page.getFolders().dstDir + "fhir-all-xsd.zip");
 		if (f.exists())
 			f.delete();
 		ZipGenerator zip = new ZipGenerator(page.getFolders().tmpResDir	+ "fhir-all-xsd.zip");
     zip.addFiles(page.getFolders().dstDir, "", ".xsd");
     zip.addFiles(page.getFolders().dstDir, "", ".sch");
     zip.addFiles(page.getFolders().rootDir+"tools\\schematron\\", "", ".xsl");
 		zip.close();
 		Utilities.copyFile(new File(page.getFolders().tmpResDir	+ "fhir-all-xsd.zip"), f);
 	}
 
 	private void produceResource(ResourceDefn resource) throws Exception {
 		File tmp = File.createTempFile("tmp", ".tmp");
 		tmp.deleteOnExit();
 		String n = resource.getName().toLowerCase();
 
 		XmlSpecGenerator gen = new XmlSpecGenerator(new FileOutputStream(tmp), n+"-definitions.htm", null);
 		gen.generate(resource.getRoot());
 		String xml = TextFile.fileToString(tmp.getAbsolutePath());
 
 		TerminologyNotesGenerator tgen = new TerminologyNotesGenerator(
 				new FileOutputStream(tmp));
 		tgen.generate(resource.getRoot(), page.getDefinitions().getBindings());
 		String tx = TextFile.fileToString(tmp.getAbsolutePath());
 
 		DictHTMLGenerator dgen = new DictHTMLGenerator(
 				new FileOutputStream(tmp));
 		dgen.generate(resource.getRoot());
 		String dict = TextFile.fileToString(tmp.getAbsolutePath());
 
 		DictXMLGenerator dxgen = new DictXMLGenerator(new FileOutputStream(page.getFolders().dstDir + n + ".dict.xml"));
 		dxgen.generate(resource.getRoot(), "HL7");
 
     generateProfile(resource, n, xml);
 		for (RegisteredProfile p : resource.getProfiles()) 
 		  produceProfile(p.getFilename(), p.getProfile());
 		
 		for (Example e : resource.getExamples()) {
 			processExample(e);
 		}
 
 		String src = TextFile.fileToString(page.getFolders().srcDir+ "template.htm");
 		TextFile.stringToFile(page.processResourceIncludes(n, resource, xml, tx, dict, src),	page.getFolders().dstDir + n + ".htm");
 		src = TextFile.fileToString(page.getFolders().srcDir + "template-examples.htm");
 		TextFile.stringToFile(page.processResourceIncludes(n, resource, xml, tx, dict, src),	page.getFolders().dstDir + n + "-examples.htm");
 		src = TextFile.fileToString(page.getFolders().srcDir+ "template-definitions.htm");
 		TextFile.stringToFile(page.processResourceIncludes(n, resource, xml, tx, dict, src),	page.getFolders().dstDir + n + "-definitions.htm");
 		src = TextFile.fileToString(page.getFolders().srcDir+ "template-explanations.htm");
 		TextFile.stringToFile(page.processResourceIncludes(n, resource, xml, tx, dict, src),  page.getFolders().dstDir + n + "-explanations.htm");
 		src = TextFile.fileToString(page.getFolders().srcDir+ "template-profiles.htm"); 
 		TextFile.stringToFile(page.processResourceIncludes(n, resource, xml, tx, dict, src),	page.getFolders().dstDir + n + "-profiles.htm");
 
 		src = TextFile.fileToString(page.getFolders().srcDir + "template-print.htm").replace("<body>", "<body class=\"book\">");
 		TextFile.stringToFile(page.processResourceIncludes(n, resource, xml, tx, dict, src), page.getFolders().dstDir + "print-" + n + ".htm");
 
 		File umlf = new File(page.getFolders().imgDir + n + ".png");
 		Utilities.copyFile(umlf, new File(page.getFolders().dstDir + n + ".png"));
 		src = TextFile.fileToString(page.getFolders().srcDir + "template-book.htm").replace("<body>", "<body style=\"margin: 10px\">");
 		src = page.processResourceIncludes(n, resource, xml, tx, dict, src);
 		cachePage(n + ".htm", src);
     src = TextFile.fileToString(page.getFolders().srcDir + "template-book-ex.htm").replace("<body>", "<body style=\"margin: 10px\">");
     src = page.processResourceIncludes(n, resource, xml, tx, dict, src);
     cachePage(n + "Ex.htm", src);
     src = TextFile.fileToString(page.getFolders().srcDir + "template-book-defn.htm").replace("<body>", "<body style=\"margin: 10px\">");
     src = page.processResourceIncludes(n, resource, xml, tx, dict, src);
     cachePage(n + "Defn.htm", src);
 
 		// xml to json
 		// todo - fix this up
 		// JsonGenerator jsongen = new JsonGenerator();
 		// jsongen.generate(new File(page.getFolders().dstDir+n+".xml"), new
 		// File(page.getFolders().dstDir+n+".json"));
 
 		tmp.delete();
 
 	}
 
 	private void cloneToXhtml(String n, String description) throws Exception {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     factory.setNamespaceAware(true);
     DocumentBuilder builder = factory.newDocumentBuilder();
 
     Document xdoc = builder.parse(new FileInputStream(new File(page.getFolders().dstDir + n + ".xml")));
     XhtmlGenerator xhtml = new XhtmlGenerator();
     xhtml.generate(xdoc,
         new File(page.getFolders().dstDir + n + ".xml.htm"), n
             .toUpperCase().substring(0, 1) + n.substring(1),
         description);
 	}
 	
 	private void processExample(Example e) throws Exception {
     if ("tool".equals(e.getType())) 
       return;
     
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     factory.setNamespaceAware(true);
     DocumentBuilder builder = factory.newDocumentBuilder();
     String n = e.getFileTitle();
 
     if (!e.getPath().exists())
       throw new Exception("unable to find example file");
 
     if ("csv".equals(e.getType())) {
       CSVProcessor csv = new CSVProcessor();
       csv.setSource(new FileInputStream(e.getPath()));
       csv.setData(new FileInputStream(Utilities.changeFileExt(e.getPath()
           .getAbsolutePath(), ".csv")));
       csv.setOutput(new FileOutputStream(page.getFolders().dstDir + n
           + ".xml"));
       csv.process();
     } else {
       // strip the xsi: stuff. seems to need double processing in order to
       // delete namespace crap
       Document xdoc = builder.parse(new FileInputStream(e.getPath()));
       XmlGenerator xmlgen = new XmlGenerator();
       if (xdoc.getDocumentElement().getLocalName().equals("feed"))
         xmlgen.generate(xdoc.getDocumentElement(),
             new File(page.getFolders().dstDir + n + ".xml"),
             "http://www.w3.org/2005/Atom", xdoc
             .getDocumentElement().getLocalName());
       else
         xmlgen.generate(xdoc.getDocumentElement(),
             new File(page.getFolders().dstDir + n + ".xml"),
             "http://hl7.org/fhir", xdoc.getDocumentElement()
             .getLocalName());
     }
 
 		// reload it now, xml to xhtml of xml
 		builder = factory.newDocumentBuilder();
 		Document xdoc = builder.parse(new FileInputStream(new File(page.getFolders().dstDir + n + ".xml")));
 		XhtmlGenerator xhtml = new XhtmlGenerator();
 		xhtml.generate(xdoc,
 				new File(page.getFolders().dstDir + n + ".xml.htm"), n
 						.toUpperCase().substring(0, 1) + n.substring(1),
 				e.getDescription());
 		if (e.isInBook()) {
 		  XhtmlDocument d = new XhtmlParser().parse(new FileInputStream(page
 		      .getFolders().dstDir + n + ".xml.htm"));
 		  XhtmlNode pre = d.getElement("html").getElement("body")
 		      .getElement("div");
 		  e.setXhtm(new XhtmlComposer().compose(pre));
 		}
 		Utilities.copyFile(new File(page.getFolders().dstDir + n + ".xml"), new File(page.getFolders().dstDir + "examples" + File.separator + n + ".xml"));
 	}
 
 	private void generateProfile(ResourceDefn root, String n, String xmlSpec)
 			throws Exception, FileNotFoundException {
 		ProfileDefn p = new ProfileDefn();
 		p.putMetadata("id", root.getName().toLowerCase());
 		p.putMetadata("name", n);
 		p.putMetadata("author.name", "todo (committee)");
 		p.putMetadata("author.ref", "todo");
 		p.putMetadata("description", "Basic Profile. "+root.getRoot().getDefinition());
 		p.putMetadata("status", "testing");
 		p.putMetadata("date", new SimpleDateFormat("yyyy-MM-dd", new Locale(
 				"en", "US")).format(new Date()));
 		p.getResources().add(root);
 		ProfileGenerator pgen = new ProfileGenerator();
 		Profile rp = pgen.generate(p, new FileOutputStream(page.getFolders().dstDir + n + ".profile.xml"), xmlSpec);
     Utilities.copyFile(new File(page.getFolders().dstDir + n + ".profile.xml"), new File(page.getFolders().dstDir +"examples"+File.separator+ n + ".profile.xml"));
 		addToResourceFeed(rp);
 		saveAsPureHtml(rp, new FileOutputStream(page.getFolders().dstDir
 				+ "html" + File.separator + n + ".htm"));
 	}
 
 	private void saveAsPureHtml(Profile resource, FileOutputStream stream)
 			throws Exception {
 		XhtmlDocument html = new XhtmlDocument();
 		html.setNodeType(NodeType.Document);
 		html.addComment("Generated by automatically by FHIR Tooling");
 		XhtmlNode doc = html.addTag("html");
 		XhtmlNode head = doc.addTag("head");
 		XhtmlNode work = head.addTag("title");
 		work.addText("test title");
 		work = head.addTag("link");
 		work.setAttribute("rel", "Stylesheet");
 		work.setAttribute("href", "/css/fhir.css");
 		work.setAttribute("type", "text/css");
 		work.setAttribute("media", "screen");
 		work = doc.addTag("body");
 		if ((resource.getText() != null)
 				&& (resource.getText().getDiv() != null)) {
 			work.getAttributes().putAll(
 					resource.getText().getDiv().getAttributes());
 			work.getChildNodes().addAll(
 					resource.getText().getDiv().getChildNodes());
 		}
 		XhtmlComposer xml = new XhtmlComposer();
 		xml.setPretty(false);
 		xml.compose(stream, html);
 	}
 
 	private void addToResourceFeed(Profile profile) {
 		AtomEntry e = new AtomEntry();
 		e.setId("http://hl7.org/fhir/profile/"+profile.getId());
 		e.setLink("http://hl7.org/implement/standards/fhir/" + profile.getId() + ".profile.xml");
 		e.setTitle("Resource \"" + profile.getId()
 				+ "\" as a profile (to help derivation)");
 		e.setUpdated(page.getGenDate());
 		e.setPublished(page.getGenDate());
 		e.setAuthorName("HL7, Inc");
 		e.setAuthorUri("http://hl7.org");
 		e.setCategory("Profile");
 		e.setResource(profile);
 		e.setSummary(profile.getText().getDiv());
 		profileFeed.getEntryList().add(e);
 	}
 
 	private void produceProfile(String filename, ProfileDefn profile) throws Exception {
 		File tmp = File.createTempFile("tmp", ".tmp");
 		tmp.deleteOnExit();
 
 		// you have to validate a profile, because it has to merged with it's
 		// base resource to fill out all the missing bits
 		validateProfile(profile);
 
 		XmlSpecGenerator gen = new XmlSpecGenerator(new FileOutputStream(tmp), null, "http://hl7.org/fhir/");
 		gen.generate(profile, page.getDefinitions());
 		String xml = TextFile.fileToString(tmp.getAbsolutePath());
 
 		ProfileGenerator pgen = new ProfileGenerator();
 		pgen.generate(profile, new FileOutputStream(page.getFolders().dstDir+ filename + ".profile.xml"), xml);
     Utilities.copyFile(new File(page.getFolders().dstDir + filename + ".profile.xml"), new File(page.getFolders().dstDir +"examples"+File.separator+ filename + ".profile.xml"));
 		
 		TerminologyNotesGenerator tgen = new TerminologyNotesGenerator(new FileOutputStream(tmp));
 		tgen.generate(profile);
 		String tx = TextFile.fileToString(tmp.getAbsolutePath());
 		//
 		// DictHTMLGenerator dgen = new DictHTMLGenerator(new
 		// FileOutputStream(tmp));
 		// dgen.generate(root);
 		// String dict = Utilities.fileToString(tmp.getAbsolutePath());
 		//
 		// DictXMLGenerator dxgen = new DictXMLGenerator(new
 		// FileOutputStream(page.getFolders().dstDir+n+".dict.xml"));
 		// dxgen.generate(root, "HL7");
 		//
 		// File xmlf = new
 		// File(page.getFolders().srcDir+n+File.separatorChar+"example.xml");
 		// File umlf = new File(page.getFolders().imgDir+n+".png");
 		//
 		String src = TextFile.fileToString(page.getFolders().srcDir	+ "template-profile.htm");
 		src = page.processProfileIncludes(filename, profile, xml, tx, src);
 		TextFile.stringToFile(src, page.getFolders().dstDir + filename + ".htm");
 		//
 		// src = Utilities.fileToString(page.getFolders().srcDir +
 		// "template-print.htm").replace("<body>",
 		// "<body style=\"margin: 20px\">");
 		// src = processResourceIncludes(n, root, xml, tx, dict, src);
 		// Utilities.stringToFile(src, page.getFolders().dstDir +
 		// "print-"+n+".htm");
 		// Utilities.copyFile(umlf, new
 		// File(page.getFolders().dstDir+n+".png"));
 		// src = Utilities.fileToString(page.getFolders().srcDir +
 		// "template-book.htm").replace("<body>",
 		// "<body style=\"margin: 10px\">");
 		// src = processResourceIncludes(n, root, xml, tx, dict, src);
 		// cachePage(n+".htm", src);
 		//
 		 // xml to xhtml of xml
 		 // first pass is to strip the xsi: stuff. seems to need double
 		 // processing in order to delete namespace crap
 		 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		 factory.setNamespaceAware(true);
 		 DocumentBuilder builder = factory.newDocumentBuilder();
 		 Document xdoc = builder.parse(new FileInputStream(page.getFolders().dstDir+ filename + ".profile.xml"));
 		 XmlGenerator xmlgen = new XmlGenerator();
 		 xmlgen.generate(xdoc.getDocumentElement(), tmp, "http://hl7.org/fhir", xdoc.getDocumentElement().getLocalName());
 		
 		 // reload it now
 		 builder = factory.newDocumentBuilder();
 		 xdoc = builder.parse(new FileInputStream(tmp));
 		 XhtmlGenerator xhtml = new XhtmlGenerator();
 		 xhtml.generate(xdoc, new File(page.getFolders().dstDir+ filename + ".profile.xml.htm"), "Profile", profile.metadata("name"));
 		// // xml to json
 		// JsonGenerator jsongen = new JsonGenerator();
 		// jsongen.generate(new File(page.getFolders().dstDir+n+".xml"), new
 		// File(page.getFolders().dstDir+n+".json"));
 		//
 		tmp.delete();
 
 	}
 
 	private void validateProfile(ProfileDefn profile)
 			throws FileNotFoundException, Exception {
 		for (ResourceDefn c : profile.getResources()) {
 			Profile resource = loadResourceProfile(c.getName());
 			ProfileValidator v = new ProfileValidator();
 			v.setProfile(c);
 			v.setResource(resource);
 			List<String> errors = v.evaluate();
 			if (errors.size() > 0)
 				throw new Exception("Error validating "
 						+ profile.metadata("name") + ": " + errors.toString());
 		}
 	}
 
 	// private void produceFutureResource(String n) throws Exception {
 	// ElementDefn e = new ElementDefn();
 	// e.setName(page.getIni().getStringProperty("future-resources", n));
 	// }
 
 	private Profile loadResourceProfile(String name)
 			throws FileNotFoundException, Exception {
 		XmlParser xml = new XmlParser();
 		return (Profile) xml.parse(new FileInputStream(page.getFolders().dstDir
 				+ name + ".profile.xml"));
 	}
 
 	private void producePage(String file) throws Exception {
 		String src = TextFile.fileToString(page.getFolders().srcDir + file);
 		src = page.processPageIncludes(file, src);
 		TextFile.stringToFile(src, page.getFolders().dstDir + file);
 		src = TextFile.fileToString(page.getFolders().srcDir + file).replace(
 				"<body>", "<body class=\"book\">");
 		src = page.processPageIncludesForPrinting(file, src);
 		TextFile.stringToFile(src, page.getFolders().dstDir + "print-" + file);
 
 		src = TextFile.fileToString(page.getFolders().srcDir + file).replace(
 				"<body>", "<body style=\"margin: 10px\">");
 		src = page.processPageIncludesForBook(file, src);
 		cachePage(file, src);
 	}
 
 	private void cachePage(String filename, String source) throws Exception {
 		try {
 			// log("parse "+filename);
 			book.getPages().put(filename, new XhtmlParser().parse(source));
 		} catch (Exception e) {
 			throw new Exception("error parsing page " + filename + ": "
 					+ e.getMessage()+"in source\r\n"+source);
 		}
 	}
 
 	public class MyErrorHandler implements ErrorHandler {
 
 		private boolean trackErrors;
 		private List<String> errors = new ArrayList<String>();
 
 		public MyErrorHandler(boolean trackErrors) {
 			this.trackErrors = trackErrors;
 		}
 
 		public void error(SAXParseException arg0) throws SAXException {
 			if (trackErrors) {
 				System.out.println("error: " + arg0.toString());
 				errors.add(arg0.toString());
 			}
 
 		}
 
 		public void fatalError(SAXParseException arg0) throws SAXException {
 			System.out.println("fatal error: " + arg0.toString());
 
 		}
 
 		public void warning(SAXParseException arg0) throws SAXException {
 			// System.out.println("warning: " + arg0.toString());
 
 		}
 
 		public List<String> getErrors() {
 			return errors;
 		}
 
 	}
 
 	public class MyResourceResolver implements LSResourceResolver {
 
 		private String dir;
 
 		public MyResourceResolver(String dir) {
 			this.dir = dir;
 		}
 
 		public LSInput resolveResource(final String type,
 				final String namespaceURI, final String publicId,
 				String systemId, final String baseURI) {
 			// System.out.println(type+", "+namespaceURI+", "+publicId+", "+systemId+", "+baseURI);
 			if (!new File(dir  + systemId).exists())
 				return null;
 			try {
 				return new SchemaInputSource(new FileInputStream(new File(dir	+ systemId)), publicId, systemId, namespaceURI);
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}
 		}
 	}
 
 	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
 	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
 	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
 
 	private void validateXml() throws Exception {
 		log("Validating XML");
 		log(".. Loading schemas");
 		StreamSource[] sources = new StreamSource[2];
 		sources[0] = new StreamSource(new FileInputStream(page.getFolders().dstDir + "fhir-all.xsd"));
 		sources[1] = new StreamSource(new FileInputStream(page.getFolders().dstDir + "fhir-atom.xsd"));
 		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 		schemaFactory.setErrorHandler(new MyErrorHandler(false));
 		schemaFactory.setResourceResolver(new MyResourceResolver(page.getFolders().dstDir));
 		Schema schema = schemaFactory.newSchema(sources);
 		log(".... done");
 
 		for (ResourceDefn r : page.getDefinitions().getResources().values()) {
 			for (Example e : r.getExamples()) {
 				String n = e.getFileTitle();
 				validateXmlFile(schema, n);
 			}
 		}
 		validateXmlFile(schema, "profiles-resources");
 
     log("Reference Platform Validation.");
 		
     for (ResourceDefn r : page.getDefinitions().getResources().values()) {
       for (Example e : r.getExamples()) {
         String n = e.getFileTitle();
         validateRoundTrip(schema, n);
       }
     }
     validateRoundTrip(schema, "profiles-resources");
 	}
 
 	private void validateXmlFile(Schema schema, String n) throws Exception {
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setNamespaceAware(true);
 		factory.setValidating(false);
 		factory.setSchema(schema);
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		MyErrorHandler err = new MyErrorHandler(true);
 		builder.setErrorHandler(err);
 		Document doc = builder.parse(new FileInputStream(new File(page.getFolders().dstDir + n + ".xml")));
 		if (err.getErrors().size() > 0)
 			throw new Exception("Resource Example " + n	+ " failed schema validation");
     File tmpTransform = File.createTempFile("tmp", ".xslt");
     tmpTransform.deleteOnExit();
     File tmpOutput = File.createTempFile("tmp", ".xml");
     tmpOutput.deleteOnExit();
     String sch = doc.getDocumentElement().getNodeName().toLowerCase();
     if (sch.equals("feed"))
       sch = "fhir-atom";
     
     Utilities.transform(page.getFolders().rootDir+"tools\\schematron\\", page.getFolders().dstDir+sch+".sch", page.getFolders().rootDir+"tools\\schematron\\iso_svrl_for_xslt1.xsl", tmpTransform.getAbsolutePath());
     Utilities.transform(page.getFolders().rootDir+"tools\\schematron\\", page.getFolders().dstDir + n + ".xml", tmpTransform.getAbsolutePath(), tmpOutput.getAbsolutePath());
 
     factory = DocumentBuilderFactory.newInstance();
     factory.setNamespaceAware(true);
     builder = factory.newDocumentBuilder();
     doc = builder.parse(new FileInputStream(tmpOutput));
     NodeList nl = doc.getDocumentElement().getElementsByTagNameNS("http://purl.oclc.org/dsdl/svrl", "failed-assert");
     if (nl.getLength() > 0) {
       page.log("Schematron Validation Failed for "+n+".xml:");
       for (int i = 0; i < nl.getLength(); i++) {
         Element e = (Element) nl.item(i);
         page.log("  @"+e.getAttribute("location")+": "+e.getTextContent());        
       }
     }
 	}
 
 	private void validateRoundTrip(Schema schema, String n) throws Exception {
     for (PlatformGenerator gen : page.getReferenceImplementations()) 
     {
       if (gen.doesTest()) {
         gen.loadAndSave(page.getFolders().dstDir + n + ".xml", page.getFolders().tmpResDir+"tmp.xml");
         compareXml(n, gen.getName(), page.getFolders().dstDir + n + ".xml", page.getFolders().tmpResDir+"tmp.xml");
       }
     }
 	}
 
 	 private void compareXml(String t, String n, String fn1, String fn2) throws Exception {
 	   DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	   dbf.setNamespaceAware(true);
 	   dbf.setCoalescing(true);
 	   dbf.setIgnoringElementContentWhitespace(true);
 	   dbf.setIgnoringComments(true);
 	   DocumentBuilder db = dbf.newDocumentBuilder();
 
 	   Document doc1 = db.parse(new File(fn1));
 	   doc1.normalizeDocument();
 	   stripWhitespaceAndComments(doc1);
 
 	   Document doc2 = db.parse(new File(fn2));
 	   doc2.normalizeDocument();
 	   stripWhitespaceAndComments(doc2);
 
      XmlGenerator xmlgen = new XmlGenerator();
      File tmp1 = File.createTempFile("xml", ".xml");
      tmp1.deleteOnExit();
      xmlgen.generate(doc1.getDocumentElement(), tmp1, doc1.getDocumentElement().getNamespaceURI(), doc1.getDocumentElement().getLocalName());
      File tmp2 = File.createTempFile("xml", ".xml");
      tmp2.deleteOnExit();
      xmlgen.generate(doc2.getDocumentElement(), tmp2, doc2.getDocumentElement().getNamespaceURI(), doc2.getDocumentElement().getLocalName());
 	   
 	   if (!TextFile.fileToString(tmp1.getAbsolutePath()).equals(TextFile.fileToString(tmp2.getAbsolutePath()))) {
 	     page.log("file "+t+" did not round trip perfectly in XML in platform "+n);
 	     if (new File("c:\\program files (x86)\\WinMerge\\WinMergeU.exe").exists()) {
 
 	       List<String> command = new ArrayList<String>();
 	       command.add("\"c:\\program files (x86)\\WinMerge\\WinMergeU.exe\" \""+tmp1.getAbsolutePath()+"\" \""+tmp2.getAbsolutePath()+"\"");
 
 	       ProcessBuilder builder = new ProcessBuilder(command);
 	       builder.directory(new File(page.getFolders().rootDir));
 	       final Process process = builder.start();
 	       process.waitFor();
 	     }
 
 	   }
   }
 
 
 	private void stripWhitespaceAndComments(Node node) {
 	  if (node.getNodeType() == Node.ELEMENT_NODE) {
 	    Element e = (Element) node;
 	    Map<String, String> attrs = new HashMap<String, String>();
 	    for (int i = e.getAttributes().getLength() - 1; i >= 0; i--) { 
 	      attrs.put(e.getAttributes().item(i).getNodeName(), e.getAttributes().item(i).getNodeValue());
   	    e.removeAttribute(e.getAttributes().item(i).getNodeName());
 	    }
 	    for (String n : attrs.keySet()) {
 	      e.setAttribute(n, attrs.get(n));
 	    }
 	  }
     for (int i = node.getChildNodes().getLength()-1; i >= 0; i--) {
       Node c = node.getChildNodes().item(i);
       if (c.getNodeType() == Node.TEXT_NODE && c.getTextContent().trim().length() == 0)
         node.removeChild(c);
       else if (c.getNodeType() == Node.TEXT_NODE)
         c.setTextContent(c.getTextContent().trim());
       else if (c.getNodeType() == Node.COMMENT_NODE)
         node.removeChild(c);
       else if (c.getNodeType() == Node.ELEMENT_NODE)
         stripWhitespaceAndComments(c);
     }
     if (node.getNodeType() == Node.ELEMENT_NODE) {
       node.appendChild(node.getOwnerDocument().createTextNode("\r\n"));
     }
     
   }
 
   private void produceCombinedDictionary() throws FileNotFoundException,
 			UnsupportedEncodingException, Exception, IOException {
 		FileOutputStream fos = new FileOutputStream(page.getFolders().dstDir
 				+ "fhir.dict.xml");
 		DictXMLGenerator dxgen = new DictXMLGenerator(fos);
 		dxgen.setConceptDomains(page.getDefinitions().getBindings());
 		dxgen.generate(page.getDefinitions().getResources().values(), "HL7");
 		fos.close();
 	}
 
 	public void log(String content) {
 		page.log(content);
 	}
 
 }
