 package com.codiform.as400shell.command;
 
 import java.beans.PropertyVetoException;
 import java.io.IOException;
 
 import javanet.staxutils.IndentingXMLStreamWriter;
 
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 
 import com.codiform.as400shell.model.FileType;
 import com.codiform.as400shell.model.Library;
 import com.codiform.as400shell.model.LibraryFile;
 import com.codiform.as400shell.model.LibraryFileMember;
 import com.codiform.as400shell.shell.ShellContext;
 import com.ibm.as400.access.AS400Array;
 import com.ibm.as400.access.AS400DataType;
 import com.ibm.as400.access.AS400Exception;
 import com.ibm.as400.access.AS400SecurityException;
 import com.ibm.as400.access.FieldDescription;
 import com.ibm.as400.access.IFSFile;
 import com.ibm.as400.access.RecordFormat;
 
 public class ShowSchemaCommand extends ParsedArgumentCommand {
 
 	public ShowSchemaCommand() {
 		super( getParser() );
 	}
 
 	private static OptionParser getParser() {
 		OptionParser parser = new OptionParser();
 		parser.accepts(
 				"logical",
 				"Detail level for logical files, where 'full' includes record formats, 'summary' shows members and 'none' ignores logical files altogether." ).withOptionalArg().ofType(
 				String.class ).describedAs( "detailLevel" ).defaultsTo(
 				"summary" );
 		parser.accepts(
 				"physical",
 				"Detail level for physical files, where 'full' includes record formats, 'summary' shows members and 'none' ignores logical files altogether." ).withOptionalArg().ofType(
 				String.class ).describedAs( "detailLevel" ).defaultsTo(
 				"full" );
 		parser.accepts( "save", "Save the local file." ).withRequiredArg().describedAs(
 				"filename" );
 		return parser;
 	}
 
 	@Override
 	public void displayHelp(ShellContext context) {
 		try {
 			context.out().println(
 					"SYNTAX: showSchema [options] <Library Name>\nDisplays the schema for files in the specified library.\n" );
 			parser.printHelpOn( context.out() );
 		} catch( IOException exception ) {
 			exception.printStackTrace( context.err() );
 		}
 	}
 
 	@Override
 	public void execute(ShellContext context, OptionSet options) {
 		if( options.nonOptionArguments().size() != 1 ) {
 			context.err().println( "No library name specified." );
 			displayHelp( context );
 			return;
 		}
 
 		String detailLevel = (String) options.valueOf( "logical" );
		if( isOneOf( detailLevel, "full", "summary", "none" ) ) {
 			context.err().println(
 					"Unknown detail level for logical files: "
 							+ detailLevel );
 			displayHelp( context );
 			return;
 		}
 
 		detailLevel = (String) options.valueOf( "physical" );
		if( isOneOf( detailLevel, "full", "summary", "none" ) ) {
 			context.err().println(
 					"Unknown detail level for physical files: "
 							+ detailLevel );
 			displayHelp( context );
 			return;
 		}
 
 		try {
 			show( context, options );
 		} catch( Exception exception ) {
 			exception.printStackTrace( context.err() );
 		}
 	}
 
	private boolean isOneOf(String toMatch, String... matches) {
		for( String item : matches ) {
			if( toMatch.equals( matches ) ) {
				return true;
			}
		}
		return false;
	}

 	public void show(ShellContext context, OptionSet options)
 			throws IOException,
 			AS400SecurityException, XMLStreamException,
 			FactoryConfigurationError, AS400Exception, InterruptedException,
 			PropertyVetoException {
 		String libraryName = options.nonOptionArguments().get( 0 );
 		IFSFile file = new IFSFile( context.getServer(), "/QSYS.LIB/"
 				+ libraryName + ".LIB" );
 		if( file.exists() ) {
 			show( context, options, libraryName, file );
 		} else {
 			context.err().printf( "Library %s cannot be found at: %s\n",
 					libraryName, file.getAbsolutePath() );
 		}
 	}
 
 	private void show(ShellContext context, OptionSet options,
 			String libraryName, IFSFile file)
 			throws IOException,
 			AS400SecurityException, XMLStreamException,
 			FactoryConfigurationError, AS400Exception, InterruptedException,
 			PropertyVetoException {
 		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(
 				context.out() );
 		try {
 			showSchema( context, options, new Library( libraryName, file ),
 					new IndentingXMLStreamWriter( writer ) );
 		} finally {
 			writer.close();
 		}
 	}
 
 	private void showSchema(ShellContext content, OptionSet options,
 			Library library, IndentingXMLStreamWriter writer)
 			throws XMLStreamException, AS400Exception, IOException,
 			AS400SecurityException, InterruptedException, PropertyVetoException {
 
 		writer.writeStartDocument();
 		writer.writeStartElement( "library" );
 		writer.writeAttribute( "name", library.getName() );
 
 		for( LibraryFile file : library.getFiles() ) {
 			logLibraryFile( options, writer, file );
 		}
 
 		writer.writeEndElement();
 		writer.writeEndDocument();
 	}
 
 	private void logLibraryFile(OptionSet options,
 			IndentingXMLStreamWriter writer,
 			LibraryFile file) throws XMLStreamException, IOException,
 			AS400SecurityException,
 			AS400Exception, InterruptedException, PropertyVetoException {
 		
 		if( file.getType() == FileType.LOGICAL && options.valueOf("logical").equals( "none" ) )
 			return;
 		if( file.getType() == FileType.PHYSICAL && options.valueOf("physical").equals( "none" ) )
 			return;
 		
 		writer.writeStartElement( "file" );
 		writer.writeAttribute( "name", file.getName() );
 		writer.writeAttribute( "type", file.getType().toString() );
 		writer.writeAttribute( "members",
 				String.valueOf( file.getMembers().size() ) );
 
 		for( LibraryFileMember member : file.getMembers() ) {
 			logLibraryFileMember( writer, member,
 					showDetails( file.getType(), options ) );
 		}
 
 		writer.writeEndElement();
 	}
 
 	private boolean showDetails(FileType type, OptionSet options) {
 		if( type == FileType.PHYSICAL ) {
 			return options.valueOf( "physical" ).equals( "full" );
 		}
 		if( type == FileType.LOGICAL ) {
 			return options.valueOf( "logical" ).equals( "full" );
 		}
 		return false;
 	}
 
 	private void logLibraryFileMember(IndentingXMLStreamWriter writer,
 			LibraryFileMember member, boolean detailed)
 			throws XMLStreamException,
 			IOException, AS400SecurityException, AS400Exception,
 			InterruptedException, PropertyVetoException {
 		if( detailed )
 			writer.writeStartElement( "member" );
 		else
 			writer.writeEmptyElement( "member" );
 
 		writer.writeAttribute( "name", member.getName() );
 		RecordFormat[] formats = member.getRecordFormats();
 
 		if( formats.length > 0 ) {
 			logFormats( writer, formats, detailed );
 		}
 
 		if( detailed ) {
 			writer.writeEndElement();
 		}
 	}
 
 	private void logFormats(IndentingXMLStreamWriter writer,
 			RecordFormat[] formats, boolean detailed) throws XMLStreamException {
 		if( detailed ) {
 			for( RecordFormat item : formats ) {
 				logDetailedFormat( writer, item );
 			}
 		} else {
 			logFormatNames( writer, formats );
 		}
 	}
 
 	private void logFormatNames(IndentingXMLStreamWriter writer,
 			RecordFormat[] formats) throws XMLStreamException {
 		if( formats.length == 1 ) {
 			writer.writeAttribute( "format", formats[0].getName() );
 		} else {
 			StringBuilder builder = new StringBuilder();
 			for( RecordFormat item : formats ) {
 				if( builder.length() > 0 )
 					builder.append( ',' );
 				builder.append( item.getName() );
 			}
 			writer.writeAttribute( "format", builder.toString() );
 		}
 	}
 
 	private void logDetailedFormat(IndentingXMLStreamWriter writer,
 			RecordFormat format) throws XMLStreamException {
 		writer.writeStartElement( "format" );
 		writer.writeAttribute( "name", format.getName() );
 		FieldDescription[] fields = format.getFieldDescriptions();
 		writer.writeAttribute( "fields", String.valueOf( fields.length ) );
 
 		for( FieldDescription item : fields ) {
 			writer.writeEmptyElement( "field" );
 			writer.writeAttribute( "name", item.getFieldName() );
 			writer.writeAttribute( "type", getType( item.getDataType() ) );
 		}
 
 		writer.writeEndElement();
 	}
 
 	private String getType(AS400DataType item) {
 		switch( item.getInstanceType() ) {
 		case AS400DataType.TYPE_ARRAY:
 			AS400Array array = (AS400Array) item;
 			return String.format( "%s[%d]", getType( array.getType() ),
 					array.getNumberOfElements() );
 		case AS400DataType.TYPE_BIN2:
 			return "Short(BIN2)";
 		case AS400DataType.TYPE_BIN4:
 			return "Integer(BIN4)";
 		case AS400DataType.TYPE_BIN8:
 			return "Long(BIN8)";
 		case AS400DataType.TYPE_BYTE_ARRAY:
 			return "Byte[]";
 		case AS400DataType.TYPE_FLOAT4:
 			return "Float(FLOAT4)";
 		case AS400DataType.TYPE_FLOAT8:
 			return "Double(FLOAT8)";
 		case AS400DataType.TYPE_PACKED:
 			return "BigDecimal(PACKED)";
 		case AS400DataType.TYPE_STRUCTURE:
 			return "STRUCTURE";
 		case AS400DataType.TYPE_TEXT:
 			return "String(TEXT)";
 		case AS400DataType.TYPE_UBIN2:
 			return "Integer(UBIN2)";
 		case AS400DataType.TYPE_UBIN4:
 			return "Long(UBIN4)";
 		case AS400DataType.TYPE_ZONED:
 			return "BigDecimal(ZONED)";
 		default:
 			return item.toString();
 		}
 	}
 
 }
