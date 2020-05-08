 package counter;
 
 import java.io.IOException;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import counter.countstatistics.*;
 import counter.saveresults.*;
 
 public class CodeCounter implements ContentHandler {
 
 	private String fileName;
 	private elementName eleName;
 	public CountStatistics countStat;
 	public String language;
 	public Boolean saveFunction;//save the name of function to a list or not
 	public Boolean saveOperator;//save operator of assignment of a list or not
 	public Boolean savefilestat;//save file statistics or save only the total statistics
 
 
 	public CodeCounter(String f, String l, Boolean a, 
 			Boolean o, Boolean s){
 		fileName = f;
 		language = l;
 		saveFunction = a;
 		saveOperator = o;
 		savefilestat = s;
 		
 	}
 
 	public void startDocument() throws SAXException {
 		QualifiedName.createCollection();
 		if(language.equals("c")){
 			//			System.out.println("c");
 			countStat = new CountStatistics_c(saveOperator);
 		}
 		else if(language.equals("c++")){
 			//			System.out.println("c++");
 			countStat = new CountStatistics_cpp(saveOperator);
 		}
 		else{
 			//			System.out.println("java");
 			countStat = new CountStatistics_java(saveOperator);
 		}
 	}
 
 	// We should count either the start-tag of the element or the end-tag,
 	// but not both. Empty elements will still be reported by each of these
 	// methods.
 	public void startElement(String namespaceURI, String localName,
 			String qualifiedName, Attributes atts) throws SAXException {
 
 //		System.out.println("start***"+qualifiedName);
 		if(qualifiedName.equals("cpp:define")){
 			qualifiedName = "cppdefine";
 		}
 		eleName = QualifiedName.getElementName(qualifiedName);
 		switch (eleName) {
 
 		case unitEle:
 			//we only deal with unit that has filename as its attribute
 			if(atts.getValue("filename")!=null){
 				countStat.startUnit(atts.getValue("filename"));
 			}
 			break;
 		case structEle:
 			countStat.startStruct();
 			break;
 		case blockEle:
 			countStat.startBlock();
 			break;           
 		case function_declEle:
 			countStat.startFunctiondecl();
 			break;
 		case decl_stmtEle:
 			countStat.startDeclstmt();
 			break;
 		case declEle:
 			countStat.startDecl();
 			break;
 		case functionEle:
 			countStat.startFunction();
 			break;
 		case callEle:
 			countStat.startCall();
 			break;
 		case expr_stmtEle:
 			countStat.startExprstmt();
 			break;
 		case exprEle:
 			countStat.startExpr();
 			break;
 		case continueEle:
 			countStat.startContinue();
 			break;
 		case breakEle:
 			countStat.startBreak();
 			break;
 		case returnEle:
 			countStat.startReturn();
 			break;
 		case forEle:
 			countStat.startFor();
 			break;
 		case ifEle:
 			countStat.startIf();
 			break;
 		case elseEle:
 			countStat.startElse();
 			break;
 		case whileEle:
 			countStat.startWhile();
 			break;
 		case doEle:
 			countStat.startDo();
 			break;
 		case switchEle:
 			countStat.startSwitch();
 			break;
 		case caseEle:
 			countStat.startCase();
 			break;
 		case parameter_listEle:
 			countStat.startParamList();
 			break;
 		case paramEle:
 			countStat.startParam();
 			break;
 		case argument_listEle:
 			countStat.startArgumentList();
 			break;
 		case argumentEle:
 			countStat.startArgument();
 			break;
 		case commentEle:
 			countStat.startComment();
 			break;
 		case gotoEle:
 			countStat.startGoto();
 			break;
 		case labelEle:
 			countStat.startLabel();
 			break;
 		case typeEle:
 			countStat.startType();
 			break;
 		case nameEle:
 			countStat.startName();
 			break;
 		case classEle:
 			countStat.startClass();
 			break;
 		case constructorEle:
 			countStat.startConstructor();
 			break;
 		case tryEle:
 			countStat.startTry();
 			break;
 		case catchEle:
 			countStat.startCatch();
 			break;
 		case throwEle:
 			countStat.startThrow();
 			break;
 		case constructor_declEle:
 			countStat.startConstructordecl();
 			break;
 		case destructor_declEle:
 			countStat.startDestructordecl();
 			break;
 		case destructorEle:
 			countStat.startDestructor();
 			break;
 		case unionEle:
 			countStat.startUnion();
 			break;
 		case cppdefineEle:
 			countStat.startCppdefine();
 			break;
 		default:
 			break;
 		}
 	}
 
 	public void endElement(String namespaceURI, String localName,
 			String qualifiedName) throws SAXException {
 //		System.out.println("end***"+qualifiedName);
 		eleName = QualifiedName.getElementName(qualifiedName);
 		switch (eleName) {
 
 		case unitEle:
 			countStat.endUnit();
 			break;
 		case commentEle:
 			countStat.endComment();
 			break;
 		case declEle:
 			countStat.endDecl();
 			break;
 		case exprEle:
 			countStat.endExpr();
 			break;
 		case typeEle:
 			countStat.endType();
 			break;
 		case nameEle:
 			countStat.endName();
 			break;
 		case argument_listEle:
 			countStat.endArguList();
 			break;
 		case callEle:
 			countStat.endCall();
 			break;
 		case forEle:
 			countStat.endFor();
 			break;
 		case paramEle:
 			countStat.endParam();
 			break;
 		case decl_stmtEle:
 			countStat.endDeclStmt();
 			break;
 		default:
 			break;
 		}
 	}
 
 	public void characters(char[] text, int start, int length)
 	throws SAXException {
 //				System.out.println(new String(text, start, length));
 		countStat.characterHandle(text, start, length);
 	}
 
 
 
 
 	public void ignorableWhitespace(char[] text, int start, int length)
 	throws SAXException {
 	}
 
 	public void processingInstruction(String target, String data)
 	throws SAXException {
 
 	}
 
 	// Now that the document is done, we can print out the final results
 	public void endDocument() throws SAXException {
		SaveResults sr = null;
		if(language.equals("C")){
 			sr = new SaveResults_c();
 		}
		else if(language.equals("C++")){
 			sr = new SaveResults_cpp();
 		}
		else if(language.equals("Java")){
 			sr = new SaveResults_java();
 		}
		else{
			System.err.println("Can not parse projects in this language");
			System.exit(0);
		}
 		sr.writeResult(fileName, countStat.getFileList(),
 				countStat.getFunctionList(), 
 				countStat.getFunctionCallList(),countStat.getClassList(),
 				countStat.getCallerList(),countStat.getNumLocalCall(), 
 				saveFunction, saveOperator, savefilestat);
 
 
 	}
 
 	// Do-nothing methods we have to implement only to fulfill
 	// the interface requirements:
 	public void setDocumentLocator(Locator locator) {
 	}
 
 	public void startPrefixMapping(String prefix, String uri)
 	throws SAXException {
 	}
 
 	public void endPrefixMapping(String prefix) throws SAXException {
 	}
 
 	public void skippedEntity(String name) throws SAXException {
 	}
 
 	public static void main(String[] args) {
 
 		XMLReader parser;
 		/**
 		 * Command line parsing
 		 */
 		CmdLineParser cmdparser = new CmdLineParser();
 		CmdLineParser.Option file_opt = cmdparser.addStringOption('f', "filename");
 		CmdLineParser.Option language_opt = cmdparser.addStringOption('l', "language");
 		CmdLineParser.Option call_opt = cmdparser.addBooleanOption('a', "call");
 		CmdLineParser.Option operator_opt = cmdparser.addBooleanOption('o', "operator");
 		CmdLineParser.Option filestat_opt = cmdparser.addBooleanOption('s', "filestat");
 		try {
 			cmdparser.parse(args);
 		} catch (CmdLineParser.OptionException e) {
 			System.err.println(e.getMessage());
 			//            printUsage();
 			System.exit(2);
 		}
 
 		String fileName = (String) cmdparser.getOptionValue(file_opt, null);
 		String language = (String) cmdparser.getOptionValue(language_opt, null);
 		Boolean savecall = (Boolean) cmdparser.getOptionValue(call_opt, false);
 		Boolean saveoperator = (Boolean) cmdparser.getOptionValue(operator_opt, false);
 		Boolean savefilestat = (Boolean) cmdparser.getOptionValue(filestat_opt, false);
 		/*
 		 * start parsing the xml file
 		 */
 		try {
 			parser = XMLReaderFactory.createXMLReader();
 		} catch (SAXException e) {
 			// fall back on Xerces parser by name
 			try {
 				parser = XMLReaderFactory
 				.createXMLReader("org.apache.xerces.parsers.SAXParser");
 			} catch (SAXException ee) {
 				System.err.println("Couldn't locate a SAX parser");
 				return;
 			}
 		}
 
 		if (args.length == 0) {
 			System.out.println("Usage: java DocumentStatistics URL1 URL2...");
 		}
 		CodeCounter codeCounter = 
 			new CodeCounter(fileName, language, savecall, 
 					saveoperator, savefilestat);
 		// Install the Content Handler
 		parser.setContentHandler(codeCounter);
 
 		// start parsing...
 		//		for (int i = 0; i < args.length; i++) {
 
 		// command line should offer URIs or file names
 		try {
 			// parse the file offered from the command line
 			parser.parse(fileName);
 		} catch (SAXParseException e) { // well-formedness error
 			System.out.println(fileName + " is not well formed.");
 			System.out.println(e.getMessage() + " at line "
 					+ e.getLineNumber() + ", column "
 					+ e.getColumnNumber());
 		} catch (SAXException e) { // some other kind of error
 			System.out.println(e.getMessage());
 		} catch (IOException e) {
 			System.out.println("Could not report on "
 					+ fileName + " because of the IOException " + e);
 		}
 		//		}
 	}
 }
