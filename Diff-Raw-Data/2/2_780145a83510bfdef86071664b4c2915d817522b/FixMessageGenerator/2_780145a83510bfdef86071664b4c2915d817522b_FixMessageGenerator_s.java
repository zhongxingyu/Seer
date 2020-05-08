 package org.tomac.tools.messagegen;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.SortedSet;
 
 import org.dom4j.Document;
 import org.dom4j.io.SAXReader;
 import org.tomac.tools.messagegen.FixMessageDom.DomBase;
 import org.tomac.tools.messagegen.FixMessageDom.DomFixComponent;
 import org.tomac.tools.messagegen.FixMessageDom.DomFixComponentRef;
 import org.tomac.tools.messagegen.FixMessageDom.DomFixField;
 import org.tomac.tools.messagegen.FixMessageDom.DomFixField.DomFixValue;
 import org.tomac.tools.messagegen.FixMessageDom.DomFixMessage;
 
 
 public class FixMessageGenerator {
 	
 	// static include strings, allowing for cahnging the import libraries.
 	private static String strInByteBuffer = "import java.nio.ByteBuffer;";
 	private static String strOutByteBuffer = "";
 	private static String strUtils = "import org.tomac.utils.Utils;";
 	private static String strFixUtils = "import org.tomac.protocol.fix.FixUtils;";
 	private static String strOtherUtils = "";
 	private static String strFixException = "import org.tomac.protocol.fix.FixSessionException;";
 	private static String strFixGarbledException = "import org.tomac.protocol.fix.FixGarbledException;";
 	private static String strConstants = "import org.tomac.protocol.fix.FixConstants;";
 	private static String strBaseUtils = "";
 	
 	private static String strReadableByteBuffer = "ByteBuffer";
 	private static String strWritableByteBuffer = "ByteBuffer";
 	private static String strUtil = "Utils";
 	private static String strOtherUtil = "Utils";
 
 	
 	private static String capFirst(final String s) {
 		return s.substring(0, 1).toUpperCase() + s.substring(1);
 	}
 
 	static FixMessageDom fixLoadAndValidate(final File specFile) throws Exception {
 		final FixMessageDom fixDom = new FixMessageDom();
 
 		final SAXReader reader = new SAXReader();
 		final Document doc = reader.read(specFile);
 
 		fixDom.buildFrom(doc.getRootElement());
 
 		return fixDom;
 	}
 
 	private static int getMsgTypeTagAsInt(final byte[] b, final int length) {
 		int val = 0;
 
 		val |= b[0];
 
 		if (length > 1)
 			for (int i = 1; i < length; i++) {
 				val <<= 8;
 				val |= b[i];
 			}
 
 		return val;
 	}
 
 	public static void main(final String[] args) {
 		
 		if (args.length < 1) {
 			System.out.println("Usage: FixMessageGenerator [specFile] [outputDirectory]");
 			System.out.println("specFile:\tthe quickFix xml file.\noutputDirectory:\tthe java src root.\n");
 			return;
 		} 
 
 		final File specFile = new File(args[0]);
 
 		if (!specFile.exists()) {
 			System.out.println("Spec file " + args[0] + " cannot be found!");
 
 			return;
 		}
 
 		File outputDir = new File(System.getProperty("user.dir"));
 
 		if (args.length > 1) {
 			outputDir = new File(args[1]);
 
 			if (!outputDir.exists())
				outputDir.mkdir();
 		}
 		
 		FixMessageDom fixDom = null;
 
 		try {
 			fixDom = fixLoadAndValidate(specFile);
 		} catch (final Exception e) {
 
 			System.out.println("Failure: " + e);
 
 			e.printStackTrace();
 
 			return;
 		}
 		
 		/*if (prettyformat) {
 			new FixMessageGenerator().prettyformat(fixDom);
 			return;
 		}*/
 		// print missing fields.
 		if (args.length > 2) {
 			final File refFile = new File(args[2]);
 			try {
 				FixMessageDom refFixDom = fixLoadAndValidate(refFile);
 				Iterator<String> it = fixDom.missingFields.iterator();
 				while (it.hasNext()) {
 					String name = it.next();
 					DomFixField field = refFixDom.domFixNamedFields.get(name);
 					if (field!=null) {
 						if (field.domFixValues.size()==0) {
 							System.out.println("<field name=\""+field.name+"\" number=\""+field.number+"\" type=\""+field.type+"\"/>");
 						} else {
 							System.out.println("<field name=\""+field.name+"\" number=\""+field.number+"\" type=\""+field.type+"\">");
 							Iterator<DomFixValue> iv = field.domFixValues.iterator();
 							while (iv.hasNext()) {
 								DomFixValue v = iv.next();
 								System.out.println("\t<value enum=\"" + v.fixEnum + "\" description=\"" + v.description + "\"/>");
 							}
 							System.out.println("</field>");
 						}
 					}
 				}
 			} catch (final Exception e) {
 				e.printStackTrace();
 				return;
 			}
 			
 		}		
 
 
 		
 		try {
 			new FixMessageGenerator().generate(fixDom, outputDir);
 		} catch (final Exception e) {
 			System.out.println("Failure: " + e);
 
 			e.printStackTrace();
 
 			return;
 		}
 
 		System.out.println("Done.");
 	}
 
 	static String uncapFirst(final String s) {
 		return s.substring(0, 1).toLowerCase() + s.substring(1);
 	}
 
 	private void clearComponent(DomFixComponentRef c, BufferedWriter out) throws Exception {
 		out.write("\t\t" + uncapFirst(c.name) + ".clear();\n");
 	}
 	
 	private void clearField(DomFixField f, BufferedWriter out) throws Exception {
 		
 		switch (FixMessageDom.toInt(f.type)) {
 
 		case FixMessageDom.STRING:
 		case FixMessageDom.MULTIPLECHARVALUE:
 		case FixMessageDom.MULTIPLESTRINGVALUE:
 		case FixMessageDom.COUNTRY:
 		case FixMessageDom.CURRENCY:
 		case FixMessageDom.EXCHANGE:
 		case FixMessageDom.MONTHYEAR:
 		case FixMessageDom.UTCTIMESTAMP:
 		case FixMessageDom.UTCTIMEONLY:
 		case FixMessageDom.UTCDATEONLY:
 		case FixMessageDom.LOCALMKTDATE:
 		case FixMessageDom.TZTIMEONLY:
 		case FixMessageDom.TZTIMESTAMP:
 		case FixMessageDom.DATA:
 		case FixMessageDom.XMLDATA:
 		case FixMessageDom.LANGUAGE:
 			out.write("\t\t" + strUtil + ".clear( " + uncapFirst(f.name) + " );\n");
 			break;
 
 		case FixMessageDom.CHAR:
 			out.write("\t\t" + uncapFirst(f.name) + " = Byte.MAX_VALUE;		\n");
 			break;
 
 		case FixMessageDom.INT:
 		case FixMessageDom.LENGTH:
 		case FixMessageDom.TAGNUM:
 		case FixMessageDom.SEQNUM:
 		case FixMessageDom.NUMINGROUP:
 		case FixMessageDom.DAYOFMOUNTH:
 			out.write("\t\t" + uncapFirst(f.name) + " = Long.MAX_VALUE;		\n");
 			break;
 
 		case FixMessageDom.FLOAT:
 		case FixMessageDom.PRICE:
 		case FixMessageDom.QTY:
 		case FixMessageDom.PRICEOFFSET:
 		case FixMessageDom.AMT:
 		case FixMessageDom.PERCENTAGE:
 			out.write("\t\t" + uncapFirst(f.name) + " = Long.MAX_VALUE;		\n");
 			break;
 
 		case FixMessageDom.BOOLEAN:
 			out.write("\t\t" + uncapFirst(f.name) + " = false;		\n");
 			break;
 
 		default:
 			throw new RuntimeException("No idea how to parse this field: " + f.name);
 		}
 	}
 
 	private void allocateComponent(final DomFixComponentRef c, final BufferedWriter out) throws IOException {
 		out.write("\t\t" + uncapFirst(c.name) + " = new Fix" + capFirst(c.name) + "();\n");
 	}
 	
 	private void allocateField(final DomFixField f, final BufferedWriter out) throws IOException {
 
 		switch (FixMessageDom.toInt(f.type)) {
 
 			case FixMessageDom.STRING:
 			case FixMessageDom.MULTIPLECHARVALUE:
 			case FixMessageDom.MULTIPLESTRINGVALUE:
 			case FixMessageDom.COUNTRY:
 			case FixMessageDom.CURRENCY:
 			case FixMessageDom.EXCHANGE:
 			case FixMessageDom.MONTHYEAR:
 			case FixMessageDom.UTCTIMESTAMP:
 			case FixMessageDom.UTCTIMEONLY:
 			case FixMessageDom.UTCDATEONLY:
 			case FixMessageDom.LOCALMKTDATE:
 			case FixMessageDom.TZTIMEONLY:
 			case FixMessageDom.TZTIMESTAMP:
 			case FixMessageDom.DATA:
 			case FixMessageDom.XMLDATA:
 			case FixMessageDom.LANGUAGE:
 				out.write("\t\t" + uncapFirst(f.name) + " = new byte[" + getJavaLength(f) + "];\n");
 				break;
 		}
 	}	
 
 	private void declareComponent(final DomFixComponentRef c, final BufferedWriter out) throws IOException {
 		out.write("\tpublic Fix" + capFirst(c.name) + " " + uncapFirst(c.name) + ";\n");
 	}
 	
 	private void declareField(final DomFixField f, final BufferedWriter out) throws IOException {
 
 		switch (FixMessageDom.toInt(f.type)) {
 
 			case FixMessageDom.STRING:
 			case FixMessageDom.MULTIPLECHARVALUE:
 			case FixMessageDom.MULTIPLESTRINGVALUE:
 			case FixMessageDom.COUNTRY:
 			case FixMessageDom.CURRENCY:
 			case FixMessageDom.EXCHANGE:
 			case FixMessageDom.MONTHYEAR:
 			case FixMessageDom.UTCTIMESTAMP:
 			case FixMessageDom.UTCTIMEONLY:
 			case FixMessageDom.UTCDATEONLY:
 			case FixMessageDom.LOCALMKTDATE:
 			case FixMessageDom.TZTIMEONLY:
 			case FixMessageDom.TZTIMESTAMP:
 			case FixMessageDom.DATA:
 			case FixMessageDom.XMLDATA:
 			case FixMessageDom.LANGUAGE:
 				out.write("\tpublic byte[] " + uncapFirst(f.name) + ";\n");
 				break;
 
 			case FixMessageDom.CHAR:
 				out.write("\tpublic byte " + uncapFirst(f.name) + " = (byte)' ';\n");
 				break;
 
 			case FixMessageDom.INT:
 			case FixMessageDom.LENGTH:
 			case FixMessageDom.TAGNUM:
 			case FixMessageDom.SEQNUM:
 			case FixMessageDom.NUMINGROUP:
 			case FixMessageDom.DAYOFMOUNTH:
 				out.write("\tpublic long " + uncapFirst(f.name) + " = 0;\n");
 				break;
 
 			case FixMessageDom.FLOAT:
 			case FixMessageDom.PRICE:
 			case FixMessageDom.QTY:
 			case FixMessageDom.PRICEOFFSET:
 			case FixMessageDom.AMT:
 			case FixMessageDom.PERCENTAGE:
 				out.write("\tpublic long " + uncapFirst(f.name) + " = 0;\n");
 				break;
 
 			case FixMessageDom.BOOLEAN:
 				out.write("\tpublic boolean " + uncapFirst(f.name) + " = false;\n");
 				break;
 
 			default:
 				throw new RuntimeException("No idea how to parse this field: " + f.name);
 		}
 	}
 
 	private void decodeFieldValue(final DomFixMessage m, final DomFixField f, final BufferedWriter out) throws IOException {
 
 		String msgType = m == null ? "null" : "MsgTypes."+ m.name.toUpperCase();
 		
  		out.write("\t\t\t\t" + uncapFirst(f.name) + " = ");
 
 		switch (FixMessageDom.toInt(f.type)) {
 
 			case FixMessageDom.STRING:
 			case FixMessageDom.MULTIPLECHARVALUE:
 			case FixMessageDom.MULTIPLESTRINGVALUE:
 			case FixMessageDom.COUNTRY:
 			case FixMessageDom.CURRENCY:
 			case FixMessageDom.EXCHANGE:
 			case FixMessageDom.MONTHYEAR:
 			case FixMessageDom.UTCTIMESTAMP:
 			case FixMessageDom.UTCTIMEONLY:
 			case FixMessageDom.UTCDATEONLY:
 			case FixMessageDom.LOCALMKTDATE:
 			case FixMessageDom.TZTIMEONLY:
 			case FixMessageDom.TZTIMESTAMP:
 			case FixMessageDom.DATA:
 			case FixMessageDom.XMLDATA:
 			case FixMessageDom.LANGUAGE:
 				out.write("FixUtils.getTagStringValue(" + msgType + " ,id ,value, " + uncapFirst(f.name) + ")");
 				break;
 
 			case FixMessageDom.INT:
 			case FixMessageDom.LENGTH:
 			case FixMessageDom.TAGNUM:
 			case FixMessageDom.SEQNUM:
 			case FixMessageDom.NUMINGROUP:
 			case FixMessageDom.DAYOFMOUNTH:
 				out.write("FixUtils.getTagIntValue(" + msgType + " ,id ,value )");
 				break;
 			case FixMessageDom.FLOAT:
 			case FixMessageDom.PRICE:
 			case FixMessageDom.QTY:
 			case FixMessageDom.PRICEOFFSET:
 			case FixMessageDom.AMT:
 			case FixMessageDom.PERCENTAGE:
 				out.write("FixUtils.getTagFloatValue(" + msgType + " ,id ,value)");
 				break;
 			case FixMessageDom.CHAR:
 				out.write("FixUtils.getTagCharValue(" + msgType + " ,id ,value )");
 				break;
 			case FixMessageDom.BOOLEAN:
 				out.write("FixUtils.getTagBooleanValue(" + msgType + " ,id ,value )");
 				break;
 			default:
 				throw new RuntimeException("No idea how to parse this field: " + f.name);
 		}
 		
  		out.write(";\n");
 
 	}	
 
 	private void encodeComponent(final DomFixComponentRef c, final BufferedWriter out) throws IOException {
 		String chk = "";
 		
 		if (printIsSetCheck(c) != null) chk = "if (" + printIsSetCheck(c) + ") ";
 		// TODO what is wrong with header only gropus
 		if (c.name.equals("HopGrp")) chk = "if ( FixUtils.isSet(hopGrp.noHops) )";
 		out.write("\t\t" + chk + uncapFirst(c.name) + ".encode( out );\n");
 	}	
 	
 	private void encodeTagField(final DomFixField f, final BufferedWriter out) throws IOException {
 		String chk = "";
 		
 		switch (FixMessageDom.toInt(f.type)) {
 
 			case FixMessageDom.STRING:
 			case FixMessageDom.MULTIPLECHARVALUE:
 			case FixMessageDom.MULTIPLESTRINGVALUE:
 			case FixMessageDom.COUNTRY:
 			case FixMessageDom.CURRENCY:
 			case FixMessageDom.EXCHANGE:
 			case FixMessageDom.DATA:
 			case FixMessageDom.XMLDATA:
 			case FixMessageDom.LANGUAGE:
 				if (printIsSetCheck(f) != null) chk = "if (" + printIsSetCheck(f) + ") ";
 				out.write("\t\t" + chk + "FixUtils.putFixTag( out, FixTags." + f.name.toUpperCase() + "_INT, " + uncapFirst(f.name) + ", 0, Utils.lastIndexTrim(" + uncapFirst(f.name) + ", (byte)0) );\n");
 				break;
 
 			case FixMessageDom.CHAR:
 				if (printIsSetCheck(f) != null) chk = "if (" + printIsSetCheck(f) + ") ";
 				out.write("\t\t" + chk + "FixUtils.putFixTag( out, FixTags." + f.name.toUpperCase() + "_INT, " + uncapFirst(f.name) + " );\n");
 				break;
 
 			case FixMessageDom.MONTHYEAR:
 			case FixMessageDom.UTCTIMESTAMP: // TODO currently this is the only one that works, and the only one used.
 			case FixMessageDom.UTCTIMEONLY:
 			case FixMessageDom.UTCDATEONLY: 
 			case FixMessageDom.LOCALMKTDATE: 
 			case FixMessageDom.TZTIMEONLY:
 			case FixMessageDom.TZTIMESTAMP:
 				if (printIsSetCheck(f) != null) chk = "if (" + printIsSetCheck(f) + ") ";
 				out.write("\t\t" + chk + "FixUtils.putFixTag( out, FixTags." + f.name.toUpperCase() + "_INT, " + uncapFirst(f.name) + ");\n");
 				break;
 
 			case FixMessageDom.INT:
 			case FixMessageDom.LENGTH:
 			case FixMessageDom.TAGNUM:
 			case FixMessageDom.SEQNUM:
 			case FixMessageDom.NUMINGROUP:
 			case FixMessageDom.DAYOFMOUNTH:
 			case FixMessageDom.AMT:
 				if (printIsSetCheck(f) != null) chk = "if (" + printIsSetCheck(f) + ") ";
 				out.write("\t\t" + chk + "FixUtils.putFixTag( out, FixTags." + f.name.toUpperCase() + "_INT, " + uncapFirst(f.name) + ");\n");
 				break;
 
 			case FixMessageDom.FLOAT:
 			case FixMessageDom.PRICE:
 			case FixMessageDom.PRICEOFFSET:
 			case FixMessageDom.PERCENTAGE:
 			case FixMessageDom.QTY:
 				if (printIsSetCheck(f) != null) chk = "if (" + printIsSetCheck(f) + ") ";
 				out.write("\t\t" + chk + "FixUtils.putFixFloatTag( out, FixTags." + f.name.toUpperCase() + "_INT, " + uncapFirst(f.name) + ");\n");
 				break;
 
 			case FixMessageDom.BOOLEAN:
 				if (printIsSetCheck(f) != null) chk = "if (" + printIsSetCheck(f) + ") ";
 				out.write("\t\t" + chk + "FixUtils.putFixTag( out, FixTags." + f.name.toUpperCase() + "_INT, " + uncapFirst(f.name) + "?(byte)'Y':(byte)'N' );\n");
 				break;
 
 			default:
 				throw new RuntimeException("No idea how to parse this field: " + f.name);
 		}
 		
 	}
 	
 	private void printComponent(final DomFixComponentRef c, final BufferedWriter out) throws IOException {
 		String chk = "";
 		
 		if (printIsSetCheck(c) != null) chk = "if (" + printIsSetCheck(c) + ")";
 		out.write("\t\t\t" + chk + " s += " + uncapFirst(c.name) + ".toString().trim();\n");
 	}	
 	
 	private void printTagField(final DomFixField f, final BufferedWriter out) throws IOException {
 		String chk = "";
 		
 		switch (FixMessageDom.toInt(f.type)) {
 
 		case FixMessageDom.STRING:
 		case FixMessageDom.MULTIPLECHARVALUE:
 		case FixMessageDom.MULTIPLESTRINGVALUE:
 		case FixMessageDom.COUNTRY:
 		case FixMessageDom.CURRENCY:
 		case FixMessageDom.EXCHANGE:
 		case FixMessageDom.MONTHYEAR:
 		case FixMessageDom.UTCTIMESTAMP:
 		case FixMessageDom.UTCTIMEONLY:
 		case FixMessageDom.UTCDATEONLY:
 		case FixMessageDom.LOCALMKTDATE:
 		case FixMessageDom.TZTIMEONLY:
 		case FixMessageDom.TZTIMESTAMP:
 		case FixMessageDom.DATA:
 		case FixMessageDom.XMLDATA:
 		case FixMessageDom.LANGUAGE:
 			if (printIsSetCheck(f) != null) chk = "if (" + printIsSetCheck(f) + ")";
 			out.write("\t\t\t" + chk + " s += \"" + f.name + "(" + f.number + ")=\" + (new String(" + uncapFirst(f.name) + ")).trim() + sep;\n");
 			break;
 			
 		case FixMessageDom.CHAR:
 			if (printIsSetCheck(f) != null) chk = "if (" + printIsSetCheck(f) + ")";
 			out.write("\t\t\t" + chk + " s += \"" + f.name + "(" + f.number + ")=\" + String.valueOf(" + uncapFirst(f.name) + ").trim() + sep;\n");
 			break;
 
 		case FixMessageDom.INT:
 		case FixMessageDom.LENGTH:
 		case FixMessageDom.TAGNUM:
 		case FixMessageDom.SEQNUM:
 		case FixMessageDom.NUMINGROUP:
 		case FixMessageDom.DAYOFMOUNTH:
 		case FixMessageDom.FLOAT:
 		case FixMessageDom.PRICE:
 		case FixMessageDom.QTY:
 		case FixMessageDom.PRICEOFFSET:
 		case FixMessageDom.AMT:
 		case FixMessageDom.PERCENTAGE:
 			if (printIsSetCheck(f) != null) chk = "if (" + printIsSetCheck(f) + ")";
 			out.write("\t\t\t" + chk + " s += \"" + f.name + "(" + f.number + ")=\" + String.valueOf(" + uncapFirst(f.name) + ").trim() + sep;\n");
 			break;
 
 		case FixMessageDom.BOOLEAN:
 			if (printIsSetCheck(f) != null) chk = "if (" + printIsSetCheck(f) + ")";
 			out.write("\t\t\t" + chk + " s += \"" + f.name + "(" + f.number + ")=\" + String.valueOf(" + uncapFirst(f.name) + ").trim() + sep;\n");
 			break;
 
 		default:
 			throw new RuntimeException("No idea how to parse this field: " + f.name);
 		}
 	}		
 	
 	private String printIsSetCheck(DomFixComponentRef c) {
 		return printIsSetCheck(true,c);
 	}
 	
 	private String printIsSetCheck(boolean isSet, DomFixComponentRef c) {
 		
 		if (!isSet && c.reqd.equals("Y")) {
 			return null;
 		}
 		
 		// TODO ugh! something is wrong in the header component..
 		if (c.name.equalsIgnoreCase("hopGrp")) return "FixUtils.isSet(hopGrp.noHops)";
 
 		if (c.isRepeating()) { 
 			return "FixUtils.isSet(" + uncapFirst(c.name) + "." + uncapFirst(c.noInGroupTag()) + ")";
 		} else {
 			if (c.getKeyTagHierarchy() == null) return "()";
 			return "FixUtils.isSet(" + uncapFirst(c.name) + "." + uncapFirst(c.getKeyTagHierarchy()) + ")";
 		}
 	}
 	
 	private String printIsSetCheck(DomFixField f) {
 		
 		if (f.reqd.equals("Y")) return null;
 
 		return "FixUtils.isSet(" + uncapFirst(f.name) + ")";
 	}
 	
 	private void genConstants(final FixMessageDom dom, final BufferedWriter out) throws Exception {
 		String unknown = "U0";
 		
 		// write package line
 		out.write("package " + dom.packageName + ";\n\n");
 
 		writeGeneratedFileHeader(out);
 
 		out.write(strUtils + "\n\n");
 
 		out.write("public interface " + dom.type + "MessageInfo\n");
 		out.write("{\n\n");
 		final String servicepack = Integer.valueOf(dom.major) > 4 ? "SP" + dom.servicepack : "";
 		if (Integer.valueOf(dom.major) < 5) {
 			out.write("\tpublic static final byte[] BEGINSTRING_VALUE = \"" + dom.type.toUpperCase() + "." + dom.major + "." + dom.minor + servicepack + "\".getBytes();\n");
 			out.write("\tpublic static final byte[] BEGINSTRING_VALUE_WITH_TAG = \"8=" + dom.type.toUpperCase() + "." + dom.major + "." + dom.minor + servicepack + "\u0001\".getBytes();\n");
 		} else {
 			out.write("\tpublic static final byte[] BEGINSTRING_VALUE = \"FIXT.1.1\".getBytes();\n");
 			out.write("\tpublic static final byte[] BEGINSTRING_VALUE_WITH_TAG = \"8=FIXT.1.1\u0001\".getBytes();\n");
 		}
 		out.write("\tpublic static final byte[] FLAVOUR = \"" + dom.flavour + "\".getBytes();\n");
 		out.write("\tpublic static final byte SOH = ( byte )0x01;\n");
 		out.write("\tpublic static final byte EQUALS = ( byte )'=';\n");
 		out.write("\tpublic static final byte DECIMAL = ( byte )'.';\n\n");
 
 		out.write("\tpublic static class MsgTypes\n");
 		out.write("\t{\n");
 
 		out.write("\t\tpublic static final byte[] UNKNOWN = \""+ unknown + "\".getBytes();\n");
 		for (final DomFixMessage m : dom.domFixMessages) {
 			final String name = m.name.toUpperCase();
 			out.write("\t\tpublic static final byte[] " + name + " = \"" + m.msgtype + "\".getBytes();\n");
 		}
 		out.write("\n");
 
 		out.write("\t\tpublic static final int UNKNOWN_INT = " + getMsgTypeTagAsInt(unknown.getBytes(), unknown.getBytes().length) + ";\n");
 		for (final DomFixMessage m : dom.domFixMessages) {
 			final String msgType = m.msgtype + (m.msgsubtype.length() > 0 ? m.msgsubtype : "");
 			final int tmp = getMsgTypeTagAsInt(msgType.getBytes(), msgType.getBytes().length);
 			out.write("\t\tpublic static final int "+ m.name.toUpperCase() + "_INT = " + tmp + ";\n");
 		}
 		
 		out.write("\t}\n\n");
 
 		for (final DomFixField f : dom.domFixFields)
 			if (f.domFixValues.size() > 0) {
 				out.write("\tpublic static class " + f.name + " {\n");
 
 				for (final DomFixValue v : f.domFixValues)
 					writeEnum(out, f, v);
 
 				out.write("\t\tpublic static boolean isValid(" + getJavaType(f) + " val) {\n");
 				for (final DomFixValue v : f.domFixValues) {
 					out.write("\t\t\tif (" + getEqualExpression(v.description, f.type, "val"));
 					out.write(") return true;\n");
 				}
 				out.write("\t\t\treturn false;\n");
 				out.write("\t\t}\n");
 				
 				out.write("\t}\n\n");
 			}
 
 		out.write("}\n");
 
 		// done. close out the file
 		out.close();
 	}
 
 	private void generate(final FixMessageDom dom, final File outputDir) throws Exception {
 
 		final File packageDir = new File(outputDir, dom.packageName.replace('.', File.separatorChar));
 		final File componentPackageDir = new File(outputDir, (dom.packageName + ".component").replace('.', File.separatorChar));
 
 		// ensure the directory exists
 		if (!packageDir.exists())
 			packageDir.mkdir();
 		else
 			// delete all the old files
 			for (final File f : packageDir.listFiles(new FilenameFilter() {
 				public boolean accept(final File dir, final String name) {
 					return name.endsWith(".java");
 				}
 			}))
 				;//f.delete();
 
 		// ensure the directory exists
 		if (!componentPackageDir.exists())
 			componentPackageDir.mkdir();
 		else
 			// delete all the old files
 			for (final File f : componentPackageDir.listFiles(new FilenameFilter() {
 				public boolean accept(final File dir, final String name) {
 					return name.endsWith(".java");
 				}
 			}))
 				f.delete();
 		
 		// generate the tags
 		File f = new File(packageDir, dom.type + "Tags.java");
 
 		genTags(dom, new BufferedWriter((new FileWriter(f))));
 
 		// generate the constants
 		f = new File(packageDir, dom.type + "MessageInfo.java");
 
 		genConstants(dom, new BufferedWriter(new FileWriter(f)));
 
 		// generate the generated base message
 		f = new File(packageDir, dom.type + "GeneratedBaseMessage.java");
 
 		genBaseMessage(dom, new BufferedWriter(new FileWriter(f)));
 
 		// generate the fix message
 		f = new File(packageDir, dom.type + "Message.java");
 
 		genFixMessage(dom, new BufferedWriter(new FileWriter(f), 8 * 1024));
 
 		// generate the fix component
 		f = new File(componentPackageDir, dom.type + "Component.java");
 
 		genFixComponent(dom, new BufferedWriter(new FileWriter(f)));
 
 		// generate the messages
 		for (final DomFixMessage m : dom.domFixMessages) {
 
 			f = new File(packageDir, dom.type + m.name + ".java");
 
 			genMessage(m, dom, new BufferedWriter(new FileWriter(f)));
 
 		}
 
 		// generate the components
 		for (final DomFixComponent c : dom.domFixComponents) {
 
 			f = new File(componentPackageDir, dom.type + c.name + ".java");
 
 			if (c.isRepeating)
 				genRepeatingComponent(c, dom, new BufferedWriter(new FileWriter(f)));
 			else
 				genComponent(c, dom, new BufferedWriter(new FileWriter(f)));
 
 		}
 		
 		// generate a listener interface
 		f = new File(packageDir, dom.type + "MessageListener.java");
 
 		genListenerInterface(dom, new BufferedWriter(new FileWriter(f)));
 
 		// generate a listener interface implementation
 		f = new File(packageDir, dom.type + "MessageListenerImpl.java");
 
 		genListenerInterfaceImpl(dom, new BufferedWriter(new FileWriter(f)));
 		
 		// generate a parser
 		f = new File(packageDir, dom.type + "MessageParser.java");
 
 		genParser(dom, new BufferedWriter(new FileWriter(f)));
 
 		// generate session validator callback
 		f = new File(packageDir, dom.type + "SessionValidator.java");
 
 		genSessionValidator(dom, new BufferedWriter(new FileWriter(f)));
 		
 	
 	}
 
 
 	private void genFixComponent(FixMessageDom dom, final BufferedWriter out) throws Exception 
 	{
 		out.write("package " + dom.packageName + ".component;\n\n");
 
 		out.write("import java.nio.ByteBuffer;\n\n");
 
 		out.write(strFixException + "\n");
 		out.write(strFixGarbledException + "\n");
 
 		out.write("public interface FixComponent {\n\n");
 
 		out.write("	public void clear();\n\n");
 
 		//out.write("	public void getAll(" + strWritableByteBuffer + " buf) throws FixSessionException, FixGarbledException;\n\n");
 
 		out.write("	public void encode(" + strWritableByteBuffer + " out);\n\n");
 		
 		out.write("\tpublic boolean isSet();\n");
 
 		out.write("}\n");
 		
 		out.close();
 	}
 	
 	private void genFixMessage(FixMessageDom dom, final BufferedWriter out) throws Exception 
 	{
 		
 		// write package line
 		out.write("package " + dom.packageName + ";\n\n");
 
 		writeGeneratedFileHeader(out);
 
 		// import ByteBuffer
 		out.write(strInByteBuffer + "\n");
 		out.write(strOutByteBuffer + "\n");
 		out.write(strFixUtils + "\n");
 		out.write(strFixException + "\n");
 		out.write(strFixGarbledException + "\n");
 		out.write(strUtils + "\n\n");
 		out.write(strOtherUtils + "\n");
 		for (DomFixComponentRef c : dom.domFixHeader.components)
 			out.write("import " + dom.packageName + ".component.Fix" + capFirst(c.name) + ";\n");
 		out.write("\n");
 		
 		// write out the open to the interface
 		out.write("public abstract class FixMessage extends FixGeneratedBaseMessage\n{\n\n");
 		
 		out.write("\tpublic static boolean IGNORE_CHECKSUM = false;\n");
 		out.write("\tpublic static boolean STDOUT_EQ_DIFFERANCE = false;\n");
 		out.write("\tpublic static boolean IGNORE_EQ_TIME = false;\n\n");
 
 		out.write("\tprivate int msgTypeEnd;\n");
 		out.write("\tint msgEnd = 0;\n\n");
 		
 		for (DomFixField f : dom.domFixTrailer.fields )
 			declareField(f, out);
 		for (DomBase b : dom.domFixHeader.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField)b;
 				declareField(f, out);
 			}
 			if (b instanceof DomFixComponentRef) {
 				DomFixComponentRef c = (DomFixComponentRef)b;
 				declareComponent(c, out);
 			}
 		}
 		
 		out.write("\t\n");
 		out.write("\tprivate static byte[] tmpMsgType = new byte[4];\n");
 		out.write("\tprivate static byte[] tmpBeginString = new byte[BEGINSTRING_VALUE.length];\n\n");
 
 		out.write("\t/**\n");
 		out.write("\t * crackMsgType performs a garbled check on the fix message. \n");
 		out.write("\t * @param data\n");
 		out.write("\t * @return msgType as an int.\n");
 		out.write("\t * @throws FixSessionException\n");
 		out.write("\t */\n");
 		out.write("\tpublic static int crackMsgType( " + strReadableByteBuffer + " buf ) throws FixSessionException, FixGarbledException {\n");
 		out.write("\t\tint startPos;\n");
 		out.write("\t\tint checkSum;\n");
 		out.write("\t\tint msgTypeInt = MsgTypes.UNKNOWN_INT;\n\n");
 		
 		genGetMsgType(dom, out, true);
 		
 		out.write("\t\treturn msgTypeInt;\n");
 		out.write("\t}\n");
     
 		out.write("\tpublic FixMessage() {\n");
 		out.write("\t\tsuper();\n\n");
 		for (DomFixField f : dom.domFixTrailer.fields )
 			allocateField(f, out);
 		for (DomBase b : dom.domFixHeader.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField)b;
 				if (f.name.equals("MsgType")) continue;
 				allocateField(f, out);
 			}
 			if (b instanceof DomFixComponentRef) {
 				DomFixComponentRef c = (DomFixComponentRef)b;
 				allocateComponent(c, out);
 			}
 		}
 
 		out.write("\n\n");
 		out.write("	}\n\n");
 		
 		out.write("\t@Override\n");
 		out.write("\tpublic void clear()\n");
 		out.write("\t{\n\n");
 
 		out.write("\t\t// clear out all the fields that aren't msgType\n\n");
 		for (DomFixField f : dom.domFixTrailer.fields )
 			clearField(f, out);
 		for (DomFixField f : dom.domFixHeader.fields ) {
 			if (f.name.equals("MsgType")) continue;
 			clearField(f, out);
 		}
 		for (DomFixComponentRef c : dom.domFixHeader.components ) {
 			clearComponent(c, out);
 		}
 
 		out.write("\t}\n\n");
 
 		out.write("\t/**\n");
 		out.write("\t * getAll performs stateless session level message validations. Throws a FixSessionException if this fails \n");
 		out.write("\t */\n");
 		out.write("\t@Override\n");
 		out.write("\tpublic void getAll() throws FixSessionException, FixGarbledException\n");
 		out.write("\t{\n\n");
 		
  		genGetMsgType(dom, out, false);
 
  		out.write("\t\t// assumption message is full otherwise decode would return null\n");
  		out.write("\t\t// so negative id means that we are at the end of the message\n");
  		out.write("\t\tint id;\n");
  		out.write("\t\tbuf.position(msgTypeEnd);\n");
  		out.write("\t\tint lastTagPosition = msgTypeEnd;\n");
  		out.write("\t\twhile ( ( id = FixUtils.getTagId( buf ) ) >= 0 )\n");
  		out.write("\t\t{\n");
  		out.write("\t\t\t" + strReadableByteBuffer + " value;\n\n");
 			
  		out.write("\t\t\tvalue = buf;\n\n");
 
  		out.write("\t\t\tswitch( id ) {\n\n");
  		
 		for (DomFixField f : dom.domFixHeader.fields ) {
 			if (f.name.contains("BeginString") || f.name.contains("BodyLength") || f.name.equals("MsgType") ) continue;
 	 		out.write("\t\t\tcase FixTags." + f.name.toUpperCase() + "_INT:\n");
 	 		decodeFieldValue(null, f, out);
 			if (f.domFixValues.size() > 0 && FixMessageDom.toInt(f.type) != FixMessageDom.BOOLEAN ) {
 	 			out.write("\t\t\t\tif (!" + capFirst(f.name) + ".isValid("+ uncapFirst(f.name) + ") ) " +
 	 					"throw new FixSessionException(SessionRejectReason.VALUE_IS_INCORRECT_OUT_OF_RANGE_FOR_THIS_TAG, (\"Invalid enumerated value(\" + " + uncapFirst(f.name) + " + \") for tag\").getBytes(), id, FixUtils.getMsgType(msgTypeInt) );\n");
 			}
 	 		out.write("\t\t\t\tbreak;\n\n");
 		} 		
  		
  		out.write("\t\t\tdefault:\n");
  		// tag not in header, we are done\n");
  		out.write("\t\t\t\tbuf.position( lastTagPosition );\n\n");
 
  		out.write("\t\t\t\treturn;\n\n");
 
  		out.write("\t\t\t}\n\n");
 
  		out.write("\t\t\tlastTagPosition = buf.position();\n\n");
 
 		out.write("\t\t}\n");
 		out.write("\t}\n\n");
 
 		out.write("\t/**\n");
 		out.write("\t * @return the length written\n");
 		out.write("\t *\n");
 		out.write("\t */\n");
 		out.write("\t@Override\n");
 		out.write("\tpublic abstract void encode( " + strWritableByteBuffer + " out );\n\n");
     
 		out.write("\t@Override\n");
 		out.write("\tpublic abstract void printBuffer( " + strWritableByteBuffer + " out );\n\n");
 
 		// equals
 		out.write("\t@Override\n");
 		out.write("\tpublic boolean equals(Object o) {\n");
 		out.write("\t\tboolean ret = true;\n\n");
 		out.write("\t\tif (! ( o instanceof FixMessage)) { print(\"class\", o.getClass().getSimpleName(), \"not instance of FixMessage\"); return false; }\n\n");
 		out.write("\t\tFixMessage msg = (FixMessage) o;\n\n");
 		printEquals(out, dom.domFixHeader.fieldsAndComponents);
 
 		out.write("\n");
 		out.write("\t}\n\n");
 
 		out.write("\t@Override\n");
 		out.write("\tpublic abstract String toString();\n");
 
 		print_print(out);
 
 		// write out the close to the class
 		out.write("}\n");
 
 		// done. close out the file
 		out.close();
 	}	
 	
 	private void print_print(BufferedWriter out) throws IOException {
 		out.write("\tprotected void print(final String type, final Object msg, final Object compare) {\n");
 		out.write("\t\tif (FixMessage.STDOUT_EQ_DIFFERANCE) { \n");
 		out.write("\t\t\tString msgString = msg instanceof byte[] ? new String((byte[])msg).trim() : String.valueOf(msg).trim();\n");
 		out.write("\t\t\tString compareString = compare instanceof byte[] ? new String((byte[])compare).trim() : String.valueOf(compare).trim();\n");
 		out.write("\t\t\tbyte[] pad1 = new byte[24 - type.length() > 0 ? 24 - type.length() : 1];\n");
 		out.write("\t\t\tUtils.fill(pad1, (byte)' ');\n");
 		out.write("\t\t\tbyte[] pad2 = new byte[32 - msgString.length() > 0 ? 32 - msgString.length() : 1];\n");
 		out.write("\t\t\tUtils.fill(pad2, (byte)' ');\n");
 		out.write("\t\t\tSystem.out.println(type + new String(pad1) + msgString + new String(pad2) + compareString);\n");
 		out.write("\t\t}\n");
 		out.write("\t}\n\n");
 	}
 
 	private void genGetMsgType(FixMessageDom dom, BufferedWriter out, boolean isCrackMsgType) throws Exception 
 	{
 		if (isCrackMsgType) 
 			out.write("\t\ttry {\n");
 		out.write("\t\tstartPos = buf.position();\n");
 
 		out.write("\t\tif(buf.remaining() < (FixMessageInfo.BEGINSTRING_VALUE_WITH_TAG.length + 1 /* SOH */ + 5 /* 9=00SOH */) )\n");
 		out.write("\t\t\tthrow new FixGarbledException(buf, \"Message too short to contain mandatory header tags\");\n\n");
 
 		out.write("\t\tint begin = buf.position();\n\n");
 
 		out.write("\t\tint tagId = FixUtils.getTagId(buf);\n");
 		out.write("\t\tif(tagId != FixTags.BEGINSTRING_INT)\n");
 		out.write("\t\t\tthrow new FixGarbledException(buf, \"First tag in FIX message is not BEGINSTRING (8)\");\n\n");
 
 		out.write("\t\tFixUtils.getTagStringValue(null, FixTags.BEGINSTRING_INT, buf, tmpBeginString);\n");
 		if (!isCrackMsgType) {
 			out.write("\t\tif(!Utils.equals(FixMessageInfo.BEGINSTRING_VALUE, tmpBeginString))\n");
 			out.write("\t\t	throw new FixSessionException(SessionRejectReason.VALUE_IS_INCORRECT_OUT_OF_RANGE_FOR_THIS_TAG, (\"BeginString not equal to: \" + new String(FixMessageInfo.BEGINSTRING_VALUE)).getBytes(), FixTags.BEGINSTRING_INT, new byte[0]);");
 		}
 		
 		out.write("\t\t//now look to get bodyLength field\n");
 		out.write("\t\ttagId = FixUtils.getTagId(buf);\n");
 		out.write("\t\tif(tagId != FixTags.BODYLENGTH_INT)\n");
 		out.write("\t\t\tthrow new FixGarbledException(buf, \"Second tag in FIX message is not BODYLENGTH (9)\");\n\n");
 
 		out.write("\t\tint bodyLength = FixUtils.getTagIntValue(null, FixTags.BODYLENGTH_INT, buf);\n");
 		out.write("\t\tif(bodyLength < 0)\n\n");
 		out.write("\t\t	throw new FixGarbledException(buf, \"Invalid BODYLENGTH (9) value: \" + bodyLength);\n\n");
 		
 		out.write("\t\tint checkSumBegin = buf.position() + bodyLength; \n");
 
 		out.write("\t\tif(checkSumBegin > buf.limit()) \n\n");
 		if (isCrackMsgType) 
 			out.write("\t\t\treturn -1; // signal that buffer is to short.\n");
 		else
 			out.write("\t\t\tthrow new FixGarbledException(buf, \"Message too short to contain mandatory checksum\");\n\n");
 
 		out.write("\t\t//FIRST, validate that we got a msgType field\n");
 		out.write("\t\ttagId = FixUtils.getTagId(buf);\n");
 		out.write("\t\tif(tagId != FixTags.MSGTYPE_INT)\n");
 		out.write("\t\t	throw new FixGarbledException(buf, \"Third tag in FIX message is not MSGTYPE (35)\");\n\n");
 
 		out.write("\t\tFixUtils.getTagStringValue(null, FixTags.MSGTYPE_INT, buf, tmpMsgType);\n\n");
 		if (!isCrackMsgType)
 			out.write("\t\tmsgTypeEnd = buf.position();\n\n");
 
 		out.write("\t\t//we should verify that the final tag IS checksum here if we want to\n");
 		out.write("\t\tbuf.position(checkSumBegin);\n");
 		out.write("\t\ttagId = FixUtils.getTagId(buf);\n");
 		out.write("\t\tif(tagId != FixTags.CHECKSUM_INT)\n");
 		out.write("\t\t	throw new FixGarbledException(buf, \"Final tag in FIX message is not CHECKSUM (10)\");\n\n");
 
 		out.write("\t\tcheckSum = FixUtils.getTagIntValue(tmpMsgType, FixTags.CHECKSUM_INT, buf);\n");
 		if (isCrackMsgType) {
 			out.write("\t\tint calculatedCheckSum = FixUtils.computeChecksum(buf, begin, checkSumBegin);\n");
 			out.write("\t\tif(checkSum != calculatedCheckSum && !IGNORE_CHECKSUM)\n");
 			out.write("\t\t	throw new FixGarbledException(buf, String.format(\"Checksum mismatch; calculated: %s is not equal message checksum: %s\", calculatedCheckSum, checkSum));\n\n");
 		}
 		out.write("\t\t// finish-up\n");
 		//if (isCrackMsgType) 
 		//	out.write("\t\tbuf.flip();\n\n");
 		
 		out.write("\t\tbuf.position(startPos);\n\n");
 
 		out.write("\t\tmsgTypeInt = FixUtils.getMsgTypeTagAsInt(tmpMsgType, Utils.lastIndexTrim(tmpMsgType, (byte)0));\n\n");
 		
 		if (!isCrackMsgType) {
 			out.write("\t\tif (! MsgType.isValid(tmpMsgType))\n");
 			out.write("\t\t\tthrow new FixSessionException(SessionRejectReason.INVALID_MSGTYPE, \"MsgType not in specificaton for tag\".getBytes(), FixTags.MSGTYPE_INT, FixUtils.getMsgType(msgTypeInt) );");
 		}
 		
 		if (isCrackMsgType) {
 			out.write("\t\t} catch (FixSessionException e) {\n");
 			out.write("\t\t\tthrow new FixGarbledException(buf, e.getMessage());\n");
 			out.write("\t\t} catch (NumberFormatException e) {\n");
 			out.write("\t\t\tthrow new FixGarbledException(buf, e.getMessage());\n");
 			out.write("\t\t}\n\n");
 		}
 	}
 
 	private void genBaseMessage(FixMessageDom dom, final BufferedWriter out) throws Exception 
 	{
 		
 		// write package line
 		out.write("package " + dom.packageName + ";\n\n");
 
 		writeGeneratedFileHeader(out);
 
 		// import ByteBuffer
 		out.write(strInByteBuffer + "\n");
 		out.write(strOutByteBuffer + "\n");
 		out.write(strFixException + "\n");
 		out.write(strFixGarbledException + "\n\n");
 	
 		// write out the open to the interface
 		out.write("public abstract class FixGeneratedBaseMessage implements FixMessageInfo\n{\n\n");
 
 	    out.write("\tpublic " + strReadableByteBuffer + " buf;\n\n");
 
 	    out.write("\tpublic int startPos = Integer.MAX_VALUE;\n\n");
 
 	    out.write("\tpublic int msgTypeInt;\n\n");
 
 	    out.write("\tpublic int msgLen;\n\n");
 
 	    out.write("\tpublic final StringBuffer status = new StringBuffer();\n\n");
 
 	    out.write("\tpublic int size()\n");
 	    out.write("\t{\n\n");
 
 	    out.write("\t\treturn msgLen;\n");
 	    out.write("\t}\n\n");
 
 	    out.write("\tpublic abstract void encode( " + strWritableByteBuffer + " msg );\n\n");
 
 	    out.write("\tpublic abstract void getAll() throws FixSessionException, FixGarbledException;\n\n");
 
 	    out.write("\tpublic abstract void clear();\n\n");
 
 	    out.write("\tpublic void setBuffer( " + strReadableByteBuffer + " buf )\n");
 	    out.write("\t{\n\n");
 
 	    out.write("\t\tthis.buf = buf;\n\n");
 
 	    out.write("\t\tstartPos = buf.position();\n");
 	    out.write("\t}\n\n");
 
 	    out.write("\tpublic void setLen( int len )\n");
 	    out.write("\t{\n\n");
 
 	    out.write("\t\tmsgLen = len;\n");
 	    out.write("\t}\n\n");
 
 	    out.write("\tpublic abstract void printBuffer( " + strWritableByteBuffer + " out );\n\n");
 
 		// write out the close to the class
 		out.write("}\n");
 
 		// done. close out the file
 		out.close();
 		
 	}
 
 	private void genListenerInterface(final FixMessageDom dom, final BufferedWriter out) throws Exception {
 
 		// write package line
 		out.write("package " + dom.packageName + ";\n\n");
 
 		writeGeneratedFileHeader(out);
 
 		out.write(strInByteBuffer + "\n");
 
 		// write out the open to the interface
 		out.write("public interface " + dom.type + "MessageListener\n{\n\n");
 
 		// write out a handler for unknown message types 	
 		out.write("\tpublic void onUnknownMessageType( FixMessage msg );\n\n");
 
 		// write out each callback
 		for (final DomFixMessage m : dom.domFixMessages)
 			out.write("\tvoid on" + dom.type + m.name + "( " + dom.type + m.name + " msg );\n\n");
 
 		// write out the close to the interface
 		out.write("}\n");
 
 		// done. close out the file
 		out.close();
 	}
 
 	private void genListenerInterfaceImpl(final FixMessageDom dom, final BufferedWriter out) throws Exception {
 
 		// write package line
 		out.write("package " + dom.packageName + ";\n\n");
 
 		writeGeneratedFileHeader(out);
 
 		out.write(strInByteBuffer + "\n");
 
 		// write out the open to the interface
 		out.write("public class " + dom.type + "MessageListenerImpl implements " + dom.type + "MessageListener\n{\n\n");
 
 		out.write("\t\t@Override\n");
 		out.write("\t\tpublic void onUnknownMessageType( FixMessage msg ) {}\n\n");
 						
 		// write out each callback
 		for (final DomFixMessage m : dom.domFixMessages) {
 			out.write("\t\t@Override\n");
 			out.write("\t\tpublic void on" + dom.type + m.name + "( " + dom.type + m.name + " msg ) {}\n\n");
 		}
 		
 		// write out the close to the class
 		out.write("}\n");
 
 		// done. close out the file
 		out.close();
 	}
 
 	private void genMessage(final DomFixMessage m, final FixMessageDom dom, final BufferedWriter out) throws Exception {
 
 		String name = dom.type + m.name;
 		
 		// write package line
 		out.write("package " + dom.packageName + ";\n\n");
 
 		writeGeneratedFileHeader(out);
 
 		// import ByteBuffer
 		out.write(strInByteBuffer + "\n");
 		out.write(strOutByteBuffer + "\n");
 		out.write(strFixUtils + "\n");
 		out.write(strFixException + "\n");
 		out.write(strFixGarbledException + "\n");
 		out.write(strUtils + "\n");
 		out.write(strConstants + "\n");
 		out.write(strBaseUtils + "\n");
 		out.write(strOtherUtils + "\n");
 		for (DomFixComponentRef c : dom.domFixHeader.components)
 			out.write("import " + dom.packageName + ".component.Fix" + capFirst(c.name) + ";\n");
 		for (DomFixComponentRef c : m.components)
 			out.write("import " + dom.packageName + ".component.Fix" + capFirst(c.name) + ";\n");
 		out.write("\n");
 		
 		// write out the open to the interface
 		out.write("public class " + name + " extends FixMessage\n{\n\n");
 		
 		for (DomBase b : m.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField)b;
 				if (f.name.equals("MsgType")) continue;
 				declareField(f, out);
 			}
 			if (b instanceof DomFixComponentRef) {
 				DomFixComponentRef c = (DomFixComponentRef)b;
 				declareComponent(c, out);
 			}
 		}
 
 		out.write("\n");
 
 		out.write("\tpublic " + name + "() {\n");
 		out.write("\t\tsuper();\n\n");
 		
 		for (DomBase b : m.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField)b;
 				allocateField(f, out);
 			}
 			if (b instanceof DomFixComponentRef) {
 				DomFixComponentRef c = (DomFixComponentRef)b;
 				allocateComponent(c, out);
 			}
 		}
 
 		out.write("\t\tthis.clear();\n\n");
 
 		out.write("\t\tmsgTypeInt = MsgTypes." + m.name.toUpperCase() + "_INT;\n\n");
 
 		out.write("	}\n\n");
 		
 		out.write("\t@Override\n");
 		out.write("\tpublic void clear()\n");
 		out.write("\t{\n\n");
 
 		out.write("\t\t// clear out all the fields that aren't msgType\n\n");
 		for (DomFixField f : m.fields ) {
 			if (f.name.equals("MsgType")) continue;
 			clearField(f, out);
 		}
 		for (DomFixComponentRef c : m.components ) {
 			clearComponent(c, out);
 		}
 
 		out.write("\t}\n\n");
 		
 		// getAll()
 		out.write("\t@Override\n");
 		out.write("\tpublic void getAll() throws FixSessionException, FixGarbledException\n");
 		out.write("\t{\n\n");
 
  		out.write("\t\tint startTagPosition = buf.position();\n\n");
 
 		out.write("\t\tsuper.getAll();\n\n");
 		
  		out.write("\t\t// assumption message is full otherwise decode would return null\n");
  		out.write("\t\t// so negative id means that we are at the end of the message\n");
  		out.write("\t\tint id;\n");
  		out.write("\t\tint lastTagPosition = buf.position();\n");
  		out.write("\t\twhile ( ( id = FixUtils.getTagId( buf ) ) >= 0 )\n");
  		out.write("\t\t{\n");
  		out.write("\t\t\t" + strReadableByteBuffer + " value;\n\n");
 			
  		out.write("\t\t\tvalue = buf;\n\n");
 
  		out.write("\t\t\tswitch( id ) {\n\n");
  		
 		for (DomBase b : m.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField) b;
 				if (f.name.contains("BeginString") || f.name.contains("BodyLength") || f.name.equals("MsgType") ) continue;
 				out.write("\t\t\tcase FixTags." + f.name.toUpperCase() + "_INT:\n");
 				decodeFieldValue(m, f, out);
 				if (f.domFixValues.size() > 0) {
 					out.write("\t\t\t\tif (!" + capFirst(f.name) + ".isValid("+ uncapFirst(f.name) + ") ) " + 
 							"throw new FixSessionException(SessionRejectReason.VALUE_IS_INCORRECT_OUT_OF_RANGE_FOR_THIS_TAG, (\"Invalid enumerated value(\" + " + uncapFirst(f.name) + " + \") for tag\").getBytes(), id, FixUtils.getMsgType(msgTypeInt) );\n");
 	 			}
 	 			out.write("\t\t\t\tbreak;\n\n");
 			}
 			if (b instanceof DomFixComponentRef) {
 				DomFixComponentRef c = (DomFixComponentRef) b;
 				if (c.isRepeating()) {
 					out.write("\t\t\tcase FixTags." + c.noInGroupTag().toUpperCase() + "_INT:\n");
 					out.write("\t\t\t\t" + uncapFirst(c.name) + "." + uncapFirst(c.noInGroupTag()) + " = FixUtils.getTagIntValue( MsgTypes." + m.name.toUpperCase() + " ,FixTags." + c.noInGroupTag().toUpperCase() + "_INT ,value );\n");
 					out.write("\t\t\t\t" + uncapFirst(c.name) + ".getAll(" + uncapFirst(c.name) + "." + uncapFirst(c.noInGroupTag()) + ", value );\n");
 				} else {
 					out.write("\t\t\tcase FixTags." + c.getKeyTag().toUpperCase() + "_INT:\n");
 					out.write("\t\t\t\t" + uncapFirst(c.name) + ".getAll(FixTags." + c.getKeyTag().toUpperCase() + "_INT, value );\n");
 				}
 	 			out.write("\t\t\t\tbreak;\n\n");
 			}
 		} 		
 
 		out.write("\t\t\t// for a message always get the checksum\n");
 		out.write("\t\t\tcase FixTags.CHECKSUM_INT:\n");
 		out.write("\t\t\t\tcheckSum = FixUtils.getTagIntValue( MsgTypes." + m.name.toUpperCase() + " ,FixTags.CHECKSUM_INT, value );\n\n");
 		
 		out.write("\t\t\t\tid = checkRequiredTags();\n");
 		out.write("\t\t\t\tif (id > 0) throw new FixSessionException(SessionRejectReason.REQUIRED_TAG_MISSING, \"Required tag missing\".getBytes(), id, FixUtils.getMsgType(msgTypeInt) );\n\n");
 		
 		out.write("\t\t\t\treturn;\n\n");
 
  		out.write("\t\t\tdefault:\n");
 		out.write("\t\t\t\tthrow new FixSessionException(SessionRejectReason.UNDEFINED_TAG, \"Unknown tag\".getBytes(), id, FixUtils.getMsgType(msgTypeInt) );\n\n");
  		// for components -> out.write("\t\t\t\tbuf.position( lastTagPosition );\n\n");
  		//out.write("\t\t\t\treturn;\n\n");
 
  		out.write("\t\t\t}\n\n");
 
  		out.write("\t\t\tlastTagPosition = buf.position();\n\n");
 
 		out.write("\t\t}\n\n");
 
  		out.write("\t\tbuf.position(startTagPosition);\n\n");
  		
 		out.write("\t}\n\n");
 		
 		out.write("\tprivate int checkRequiredTags() {\n");
 		out.write("\t\tint tag = -1;\n\n");
 		
 		for (DomFixField f : dom.domFixHeader.fields ) {
 			if (f.reqd.equals("N") || f.name.contains("BeginString") || f.name.contains("BodyLength") || f.name.equals("MsgType") ) continue;
 			out.write("\t\tif (! FixUtils.isSet(" + uncapFirst(f.name) + ") ) return FixTags." + f.name.toUpperCase() + "_INT;\n");
 		}
 
 		for (DomFixComponentRef c : dom.domFixHeader.components ) {
 			if (c.reqd.equals("N") ) continue;
 			if (c.isRepeating())
 				out.write("\t\tif (! " + uncapFirst(c.name) + ".isSet() ) return FixTags." + c.noInGroupTag().toUpperCase() + "_INT;\n");
 			else
 				out.write("\t\tif (! " + uncapFirst(c.name) + ".isSet() ) return FixTags." + c.getKeyTag().toUpperCase() + "_INT;\n");
 		}
 		
 		for (DomFixField f : m.fields ) {
 			if (f.reqd.equals("N") ) continue;
 			out.write("\t\tif (! FixUtils.isSet(" + uncapFirst(f.name) + ") ) return FixTags." + f.name.toUpperCase() + "_INT;\n");
 		}
 
 		for (DomFixComponentRef c : m.components ) {
 			if (c.reqd.equals("N") ) continue;
 			if (c.isRepeating())
 				out.write("\t\tif (! " + uncapFirst(c.name) + ".isSet() ) return FixTags." + c.noInGroupTag().toUpperCase() + "_INT;\n");
 			else
 				out.write("\t\tif (! " + uncapFirst(c.name) + ".isSet() ) return FixTags." + c.getKeyTag().toUpperCase() + "_INT;\n");
 		}
 		
 		for (DomFixField f : dom.domFixTrailer.fields ) {
 			if (f.reqd.equals("N") ) continue;
 			out.write("\t\tif (! FixUtils.isSet(" + uncapFirst(f.name) + ") ) return FixTags." + f.name.toUpperCase() + "_INT;\n");
 		}
 		
 		out.write("\t\treturn tag;\n\n");
 		out.write("\t}\n");	
 		
 		// encode
 		out.write("\t@Override\n");
 		out.write("\tpublic void encode( " + strWritableByteBuffer + " out )\n");
 		out.write("\t{\n");
 		out.write("\t\t// Encode message. Set msgSeqNum and sendingTime and optional resend flags, before encoding. \n\n");
 		
 		out.write("\t\tint msgStart = out.position();\n\n");
 		
 		out.write("\t\tout.put( BEGINSTRING_VALUE_WITH_TAG );\n\n");
 		
 		out.write("\t\tint msgLengthValueStart = out.position() + 2 /* 9= */;\n\n");
 		
 
 		out.write("\t\t// placeholder\n");
 		out.write("\t\tFixUtils.putFixTag(out, FixTags.BODYLENGTH_INT, FixConstants.MAX_MESSAGE_SIZE );\n\n");
 		
 		out.write("\t\tint msgTypeStart = out.position();\n\n");
 
 		out.write("\t\tFixUtils.putFixTag( out, FixTags.MSGTYPE_INT, MsgTypes." + m.name.toUpperCase() + ");\n\n");
 
 		out.write("\t\t// encode all fields including the header\n\n");
 		for (DomBase b : dom.domFixHeader.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField) b;
 				if (f.name.contains("BeginString") || f.name.contains("BodyLength") || f.name.equals("MsgType") ) continue;
 				encodeTagField(f, out);
 			} 
 			if (b instanceof DomFixComponentRef) {
 				encodeComponent((DomFixComponentRef)b, out);
 			}
 		}
 		out.write("\n");
 		for (DomBase b : m.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField) b;
 				encodeTagField(f, out);
 			} 
 			if (b instanceof DomFixComponentRef) {
 				encodeComponent((DomFixComponentRef)b, out);
 			}
 		}
 		
 		out.write("\t\t// the checksum at the end\n\n");
 	    
 		out.write("\t\tint checkSumStart = out.position();\n");
 		out.write("\t\tout.position( msgLengthValueStart );\n");
 		out.write("\t\tbodyLength = checkSumStart - msgTypeStart;\n");
 		out.write("\t\t" + strOtherUtil + ".longToNumeric( out, bodyLength, " + strUtil + ".digits(FixConstants.MAX_MESSAGE_SIZE) );\n\n");
 	    
 		out.write("\t\tcheckSum = FixUtils.computeChecksum( out, msgStart, checkSumStart );\n");
 		out.write("\t\tout.position( checkSumStart );\n");
 		out.write("\t\tFixUtils.putFixTag( out, FixTags.CHECKSUM_INT, checkSum );\n\n");
 	    
 		out.write("\t\tout.flip();\n\n");
 		
 	    out.write("\t}\n");
 		
 		
 		out.write("\t@Override		\n");
 		out.write("\tpublic void printBuffer(" + strWritableByteBuffer + " out) {\n\n");
 		out.write("\t\tout.put(buf);\n\n");
 		out.write("\t\tout.flip();\n\n");
 		out.write("\t}\n\n");
 
 		// toString
 		out.write("\t/**\n");
 		out.write("\t * If you use toString for any other purpose than administrative printout.\n");
 		out.write("\t * You will end up in nifelheim!\n");
 		out.write("\t**/\n");
 		out.write("\t@Override\n");
 		out.write("\tpublic String toString() {\n");
 		out.write("\t\tchar sep = '\\n';\n");
 		out.write("\t\tif (Boolean.getBoolean(\"fix.useOneLiner\")) sep = SOH;\n\n");
 		
 		out.write("\t\tString s = \"" + m.name + "\" + sep;\n");
 		out.write("\t\ts += \"BeginString(8)=\" + new String(BEGINSTRING_VALUE) + sep;\n");
 		out.write("\t\ts += \"BodyLength(9)=\" + bodyLength + sep;\n");
 		out.write("\t\ts += \"MsgType(35)=\" + new String(MsgTypes." + m.name.toUpperCase() + ") + sep;\n\n");
 
 		out.write("\t\ttry {\n");
 		out.write("\t\t\t// print all fields including the header\n\n");
 		for (DomBase b : dom.domFixHeader.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField) b;
 				if (f.name.contains("BeginString") || f.name.contains("BodyLength") || f.name.equals("MsgType") ) continue;
 				printTagField(f, out);
 			}
 			if (b instanceof DomFixComponentRef) {
 				printComponent((DomFixComponentRef)b, out);
 			}
 		}
 		out.write("\n");
 		for (DomBase b : m.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField) b;
 				printTagField(f, out);
 			}
 			if (b instanceof DomFixComponentRef) {
 				printComponent((DomFixComponentRef)b, out);
 			}
 		}
 		out.write("\n");
 		out.write("\t\t\ts += \"checkSum(10)=\" + String.valueOf(checkSum) + sep;\n\n");
 		out.write("\t\t} catch(Exception e) {  };\n\n");
 		
 		out.write("\t\treturn s;\n");
 		out.write("\t}\n\n");
 
 		// equals
 		name = capFirst(dom.type) + capFirst(m.name);
 		out.write("\t@Override\n");
 		out.write("\tpublic boolean equals(Object o) {\n");
 		out.write("\t\tboolean ret = true;\n\n");
 		out.write("\t\tif (! ( o instanceof " + name + ")) { print(\"class\", o.getClass().getSimpleName(), \"not instance of " + name + "\"); return false; }\n\n");
 		out.write("\t\t\t" + name + " msg = (" + name + ") o;\n\n");
 		out.write("\t\tif ( ! super.equals(msg) ) ret = false;\n\n");
 		printEquals(out, m.fieldsAndComponents);
 		out.write("\t}\n");
 
 		// write out the close to the class
 		out.write("}\n");
 
 		// done. close out the file
 		out.close();
 	}
 	
 	private void genRepeatingComponent(final DomFixComponent m, final FixMessageDom dom, final BufferedWriter out) throws Exception {
 		String name = m.name;
 		
 		// write package line
 		out.write("package " + dom.packageName + ".component;\n\n");
 
 		writeGeneratedFileHeader(out);
 
 		// import ByteBuffer
 		out.write(strInByteBuffer + "\n");
 		out.write(strOutByteBuffer + "\n");
 		out.write(strFixUtils + "\n");
 		out.write(strFixException + "\n");
 		out.write(strFixGarbledException + "\n");
 		out.write(strUtils + "\n");
 		out.write(strConstants + "\n");
 		out.write(strBaseUtils + "\n");
 		out.write(strOtherUtils + "\n");
 		out.write("import org.tomac.protocol.fix.messaging.FixMessage;\n");
 		out.write("import " + dom.packageName + ".FixMessageInfo.SessionRejectReason;\n");
 		out.write("import " + dom.packageName + ".FixMessageInfo;\n");
 		out.write("import " + dom.packageName + ".FixTags;\n");
 		for (DomFixComponentRef c : m.components)
 			out.write("import " + dom.packageName + ".component.Fix" + capFirst(c.name) + ";\n");
 		out.write("\n");
 		
 		// write out the open to the interface
 		out.write("public class Fix" + name + "\n{\n\n");
 		
 		out.write("\tpublic int " + uncapFirst(m.noInGroupTag) + ";\n");
 		out.write("\tpublic " + m.name + "[] group;\n\n");
 
 		out.write("\tpublic void getAll(int " + uncapFirst(m.noInGroupTag) + ", ByteBuffer buf) throws FixSessionException, FixGarbledException {\n");
 		out.write("\t\tthis." + uncapFirst(m.noInGroupTag) + " = " + uncapFirst(m.noInGroupTag) + ";\n\n");
 			
 		out.write("\t\tif (" + uncapFirst(m.noInGroupTag) + " < 1) throw new FixSessionException(SessionRejectReason.INCORRECT_NUMINGROUP_COUNT_FOR_REPEATING_GROUP, (\"Incorrect num in group count \" + " + uncapFirst(m.noInGroupTag) + " ).getBytes(), FixTags." + m.noInGroupTag.toUpperCase() + "_INT, new byte[0]);\n");
 		out.write("\t\t// this will leak memory if we grow the group\n");
 		out.write("\t\tif (group == null || group.length < " + uncapFirst(m.noInGroupTag) + ") {\n");
 		out.write("\t\t\tgroup = new " + capFirst(m.name) + "[" + uncapFirst(m.noInGroupTag) + "];\n\n");
 		out.write("\t\t\tfor ( int i = 0; i < " + uncapFirst(m.noInGroupTag) + "; i++ ) group[i] = new " + capFirst(m.name) + "();\n");
 		out.write("\t}\n\n");
 
 		out.write("\t	for ( int i = 0; i < " + uncapFirst(m.noInGroupTag) + "; i++ ) \n");
 		out.write("\t		group[i].getAllGroup(buf);\n");
 
 		out.write("\t}\n\n");
 		
 		out.write("\tpublic void clear() {\n");
 		out.write("\t	for (int i = 0; i<" + uncapFirst(m.noInGroupTag) + "; i++)\n");
 		out.write("\t		group[i].clear();\n");
 		out.write("\t}\n");
 
 		out.write("\tpublic void encode(ByteBuffer out) {\n");
 		out.write("\t	for (int i = 0; i<" + uncapFirst(m.noInGroupTag) + "; i++)\n");
 		out.write("\t		group[i].encode(out);\n");
 		out.write("\t}\n");
 
 		out.write("\tpublic boolean isSet() {\n");
 		out.write("\t	for (int i = 0; i<" + uncapFirst(m.noInGroupTag) + "; i++)\n");
 		out.write("\t		if (group[i].isSet()) return true;\n");
 		out.write("\t	return false;\n");
 		out.write("\t}\n\n");
 
 		out.write("\t@Override\n");
 		out.write("\tpublic boolean equals(Object o) {\n");
 		out.write("\t\tif (! ( o instanceof Fix" + name + ")) return false;\n\n");
 
 		out.write("\t\tFix" + name + " msg = (Fix" + name + ") o;\n\n");
 
 		out.write("\t\tfor (int i = 0; i<" + uncapFirst(m.noInGroupTag) + "; i++)\n");
 		out.write("\t\t\tif (!group[i].equals(msg.group[i])) return false;\n");
 		out.write("\t\treturn true;\n");
 		out.write("\t}\n\n");
 
 		print_print(out);
 
 		out.write("\t@Override\n");
 		out.write("\tpublic String toString() {\n");
 		out.write("\t	String s = \"\";\n");
 		out.write("\t	for (int i = 0; i<" + uncapFirst(m.noInGroupTag) + "; i++)\n");
 		out.write("\t		s += group[i].toString();\n");
 		out.write("\t\treturn s;\n");
 		out.write("\t}\n\n");
 		
 		genComponent(m, dom, out);
 		
 		out.write("}\n");
 
 		out.close();
 
 	}
 	
 	private void genComponent(final DomFixComponent m, final FixMessageDom dom, final BufferedWriter out) throws Exception {
 
 		String name;
 		if (m.isRepeating)
 			name = m.name;
 		else
 			name = dom.type + m.name;
 		
 		if (!m.isRepeating) {
 			// write package line
 			out.write("package " + dom.packageName + ".component;\n\n");
 
 			writeGeneratedFileHeader(out);
 
 			// import ByteBuffer
 			out.write(strInByteBuffer + "\n");
 			out.write(strOutByteBuffer + "\n");
 			out.write(strFixUtils + "\n");
 			out.write(strFixException + "\n");
 			out.write(strFixGarbledException + "\n");
 			out.write(strUtils + "\n");
 			out.write(strConstants + "\n");
 			out.write(strBaseUtils + "\n");
 			out.write(strOtherUtils + "\n");
 			out.write("import " + dom.packageName + ".FixTags;\n");
 			out.write("import " + dom.packageName + ".FixMessageInfo.*;\n");
 			out.write("import " + dom.packageName + ".FixMessage;\n");
 			for (DomFixComponentRef c : m.components)
 				out.write("import " + dom.packageName + ".component.Fix" + capFirst(c.name) + ";\n");
 			out.write("\n");
 		}
 		
 		// write out the open to the interface
 		out.write("public class " + name + " implements FixComponent\n{\n\n");
 		
 		for (DomBase b : m.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField)b;
 				if (f.name.equals("MsgType")) continue;
 				declareField(f, out);
 			}
 			if (b instanceof DomFixComponentRef) {
 				DomFixComponentRef c = (DomFixComponentRef)b;
 				declareComponent(c, out);
 			}
 		}
 
 		out.write("\n");
 
 		out.write("\tpublic " + name + "() {\n");
 		out.write("\t\tsuper();\n\n");
 		
 		for (DomBase b : m.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField)b;
 				allocateField(f, out);
 			}
 			if (b instanceof DomFixComponentRef) {
 				DomFixComponentRef c = (DomFixComponentRef)b;
 				allocateComponent(c, out);
 			}
 		}
 
 		out.write("\t\tthis.clear();\n\n");
 
 		out.write("	}\n\n");
 		
 		out.write("\t@Override\n");
 		out.write("\tpublic void clear()\n");
 		out.write("\t{\n\n");
 
 		out.write("\t\t// clear out all the fields that aren't msgType\n\n");
 		for (DomFixField f : m.fields ) {
 			if (f.name.equals("MsgType")) continue;
 			clearField(f, out);
 		}
 		for (DomFixComponentRef c : m.components ) {
 			clearComponent(c, out);
 		}
 
 		out.write("\t}\n\n");
 		
 		// getAll()
 		if (m.isRepeating) 
 			out.write("\tpublic void getAllGroup(" + strReadableByteBuffer + " buf) throws FixSessionException, FixGarbledException\n");
 		else
 			out.write("\tpublic void getAll(int id, " + strReadableByteBuffer + " buf) throws FixSessionException, FixGarbledException\n");
 		out.write("\t{\n\n");
 
  		out.write("\t\tint startTagPosition = buf.position();\n\n");
 
  		if (m.isRepeating) out.write("\t\tint id = FixUtils.getTagId( buf );\n");
  		out.write("\t\tint lastTagPosition = buf.position();\n");
 
  		if (m.isRepeating) {
  			genGetTagsSwitchForRepeatingComponent(dom, m, out);
  		} else {
  			genGetTagsSwitchForComponent(m, out);
  		}
  		
 		out.write("\t}\n\n");
 		// end getAll
 		
 		out.write("\tprivate int checkRequiredTags() {\n");
 		out.write("\t\tint tag = -1;\n\n");
 		
 		for (DomFixField f : m.fields ) {
 			if (f.reqd.equals("N") ) continue;
 			out.write("\t\tif (! FixUtils.isSet(" + uncapFirst(f.name) + ") ) return FixTags." + f.name.toUpperCase() + "_INT;\n");
 		}
 
 		for (DomFixComponentRef c : m.components ) {
 			if (c.reqd.equals("N") ) continue;
 			if (c.isRepeating())
 				out.write("\t\tif (! " + uncapFirst(c.name) + ".isSet() ) return FixTags." + c.noInGroupTag().toUpperCase() + "_INT;\n");
 			else
 				out.write("\t\tif (! " + uncapFirst(c.name) + ".isSet() ) return FixTags." + c.getKeyTag().toUpperCase() + "_INT;\n");
 		}
 		
 		out.write("\t\treturn tag;\n\n");
 		out.write("\t}\n");	
 		
 		// isSet
 		out.write("\t@Override\n");
 		out.write("\tpublic boolean isSet()\n");
 		out.write("\t{\n");
 		for (DomBase b : m.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField) b;
 				if (printIsSetCheck(f) != null) 
 					out.write("\t\tif (" + printIsSetCheck(f) + ") return true;\n");
 			} 
 			if (b instanceof DomFixComponentRef) {
 				DomFixComponentRef c = (DomFixComponentRef)b;
 				out.write("\t\tif (" + printIsSetCheck(true, c) + ") return true;\n");
 			}
 		}
 	    out.write("\t\treturn false;\n");
 	    out.write("\t}\n");
 		
 		// encode
 		out.write("\t@Override\n");
 		out.write("\tpublic void encode( " + strWritableByteBuffer + " out )\n");
 		out.write("\t{\n");
 		for (DomBase b : m.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField) b;
 				encodeTagField(f, out);
 			} 
 			if (b instanceof DomFixComponentRef) {
 				encodeComponent((DomFixComponentRef)b, out);
 			}
 		}
 	    out.write("\t}\n");
 		
 		print_print(out);
 
 		// toString
 		out.write("\t/**\n");
 		out.write("\t * If you use toString for any other purpose than administrative printout.\n");
 		out.write("\t * You will end up in nifelheim!\n");
 		out.write("\t**/\n");
 		out.write("\t@Override\n");
 		out.write("\tpublic String toString() {\n");
 		out.write("\t\tchar sep = '\\n';\n");
 		out.write("\t\tif (Boolean.getBoolean(\"fix.useOneLiner\")) sep = ( byte )0x01;\n\n");
 		
 		out.write("\t\tString s = \"\";\n");
 		out.write("\n");
 		for (DomBase b : m.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField) b;
 				printTagField(f, out);
 			}
 			if (b instanceof DomFixComponentRef) {
 				printComponent((DomFixComponentRef)b, out);
 			}
 		}
 		out.write("\t\treturn s;\n\n");
 		
 		out.write("\t}\n\n");
 
 		// equals
 		if (m.isRepeating)
 			name = capFirst(m.name);
 		else
 			name = capFirst(dom.type) + capFirst(m.name);
 		out.write("\t@Override\n");
 		out.write("\tpublic boolean equals(Object o) {\n");
 		out.write("\t\tboolean ret = true;\n\n");
 		out.write("\t\tif (! ( o instanceof " + name + ")) { print(\"class\", o.getClass().getSimpleName(), \"not instance of " + name + "\"); return false; }\n\n");
 		out.write("\t\t\t" + name + " msg = (" + name + ") o;\n\n");
 		//out.write("\t\tif ( ! super.equals(msg) ) return false;\n\n");
 		printEquals(out, m.fieldsAndComponents);
 		out.write("\t}\n");
 
 		// write out the close to the class
 		out.write("}\n");
 
 		// done. close out the file
 		if (!m.isRepeating)
 			out.close();
 	}
 	
 	private void genGetTagsSwitchForRepeatingComponent(final FixMessageDom dom, DomFixComponent m, BufferedWriter out) throws IOException {
  		out.write("\t\t\t" + strReadableByteBuffer + " value;\n\n");
 		
  		out.write("\t\t\tvalue = buf;\n\n");
 
  		for (DomBase b : m.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField) b;
 				if (f.name.contains("BeginString") || f.name.contains("BodyLength") || f.name.contains("MsgType") ) continue;
 				out.write("\t\t\tif(id == FixTags." + f.name.toUpperCase() + "_INT) {\n");
 				decodeFieldValue(null, f, out);
 				if (f.domFixValues.size() > 0) {
 					out.write("\t\t\t\tif (!FixMessageInfo." + capFirst(f.name) + ".isValid("+ uncapFirst(f.name) + ") ) " + 
 		 					"throw new FixSessionException(SessionRejectReason.VALUE_IS_INCORRECT_OUT_OF_RANGE_FOR_THIS_TAG, (\"Invalid enumerated value(\" + " + uncapFirst(f.name) + " + \") for tag\").getBytes(), id, new byte[0] );\n");
 	 			}
 		 		out.write("\t\t\t\tlastTagPosition = buf.position();\n\n");
 				out.write("\t\t\t\tid = FixUtils.getTagId( buf );\n");
 	 			out.write("\t\t\t}\n\n");
 			}
 			if (b instanceof DomFixComponentRef) {
 				DomFixComponentRef c = (DomFixComponentRef) b;
 				if (c.isRepeating()) {
 					out.write("\t\t\tif(id == FixTags." + c.noInGroupTag().toUpperCase() + "_INT) {\n");
 					out.write("\t\t\t\tint " + uncapFirst(c.noInGroupTag()) + ";\n"); 
 					decodeFieldValue(null, dom.domFixNamedFields.get(c.noInGroupTag()), out);
 					out.write("\t\t\t\t" + uncapFirst(c.name) + ".getAll(" + uncapFirst(c.noInGroupTag()) + ", buf);\n");
 					out.write("\t\t\t\tlastTagPosition = buf.position();\n\n");
 					out.write("\t\t\t\tid = FixUtils.getTagId( buf );\n");
 					out.write("\t\t\t}\n\n");
 				} else {
 					out.write("\t\t\tif(id == FixTags." + c.getKeyTag().toUpperCase() + "_INT) {\n");
 					out.write("\t\t\t\t" + uncapFirst(c.name) + ".getAll(FixTags." + c.getKeyTag().toUpperCase() + "_INT, buf);\n");
 					out.write("\t\t\t\tlastTagPosition = buf.position();\n\n");
 					out.write("\t\t\t\tid = FixUtils.getTagId( buf );\n");
 					out.write("\t\t\t}\n\n");
 				}
 			}
 		} 		
 		// unknown, unread
 		out.write("\t\t\tid = checkRequiredTags();\n");
 		out.write("\t\t\t\tif (id > 0) throw new FixSessionException(SessionRejectReason.REQUIRED_TAG_MISSING, \"Required tag missing\".getBytes(), id, new byte[0] );\n\n");
 		
 		out.write("\t\t\tbuf.position( lastTagPosition );\n");
  		out.write("\t\t\treturn;\n\n");
 	}
 
 	private void genGetTagsSwitchForComponent(DomFixComponent m, final BufferedWriter out) throws IOException {
  		out.write("\t\tdo {\n");
  		out.write("\t\t\t" + strReadableByteBuffer + " value;\n\n");
 			
  		out.write("\t\t\tvalue = buf;\n\n");
 
  		out.write("\t\t\tswitch( id ) {\n\n");
  		
 		for (DomBase b : m.fieldsAndComponents ) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField) b;
 				if (f.name.contains("BeginString") || f.name.contains("BodyLength") || f.name.contains("MsgType") ) continue;
 				out.write("\t\t\tcase FixTags." + f.name.toUpperCase() + "_INT:\n");
 				decodeFieldValue(null, f, out);
 				if (f.domFixValues.size() > 0) {
 					out.write("\t\t\t\tif (!" + capFirst(f.name) + ".isValid("+ uncapFirst(f.name) + ") ) " + 
 		 					"throw new FixSessionException(SessionRejectReason.VALUE_IS_INCORRECT_OUT_OF_RANGE_FOR_THIS_TAG, (\"Invalid enumerated value(\" + " + uncapFirst(f.name) + " + \") for tag\").getBytes(), id, new byte[0] );\n");
 	 			}
 	 			out.write("\t\t\t\tbreak;\n\n");
 			}
 			if (b instanceof DomFixComponentRef) {
 				DomFixComponentRef c = (DomFixComponentRef) b;
 				if (c.isRepeating()) {
 					out.write("\t\t\tcase FixTags." + c.noInGroupTag().toUpperCase() + "_INT:\n");
 					out.write("\t\t\t\t" + uncapFirst(c.name) + "." + uncapFirst(c.noInGroupTag()) + " = FixUtils.getTagIntValue(null, FixTags." + c.noInGroupTag().toUpperCase() + "_INT, value );\n");
 					out.write("\t\t\t\t" + uncapFirst(c.name) + ".getAll(" + uncapFirst(c.name) + "." + uncapFirst(c.noInGroupTag()) + ", value );\n");
 				} else {
 					out.write("\t\t\tcase FixTags." + c.getKeyTag().toUpperCase() + "_INT:\n");
 					out.write("\t\t\t\t" + uncapFirst(c.name) + ".getAll( FixTags." + c.getKeyTag().toUpperCase() + "_INT, value );\n");
 				}
 	 			out.write("\t\t\t\tbreak;\n\n");
 			}
 		} 		
 
 		out.write("\t\t\t// we will always endup with unknown tag, unread and return to upper layer in hierarchy\n");
  		out.write("\t\t\tdefault:\n");
 		out.write("\t\t\t\tid = checkRequiredTags();\n");
 		out.write("\t\t\t\tif (id > 0) throw new FixSessionException(SessionRejectReason.REQUIRED_TAG_MISSING, \"Required tag missing\".getBytes(), id, new byte[0] );\n\n");
 		
 		out.write("\t\t\t\tbuf.position( lastTagPosition );\n");
  		out.write("\t\t\t\treturn;\n\n");
 
  		out.write("\t\t\t}\n\n");
  		
  		out.write("\t\t\tlastTagPosition = buf.position();\n\n");
 
  		out.write("\t\t} while ( ( id = FixUtils.getTagId( buf ) ) >= 0 );\n\n");
  		
  		out.write("\t\tbuf.position(startTagPosition);\n\n");
  		
 	}
 
 	private void genParser(final FixMessageDom dom, final BufferedWriter out) throws Exception {
 
 		// write package line
 		out.write("package " + dom.packageName + ";\n\n");
 
 		writeGeneratedFileHeader(out);
 
 		out.write(strInByteBuffer + "\n");
 		out.write(strFixException + "\n");
 		out.write(strFixGarbledException + "\n");
 		out.write(strFixUtils + "\n");
 
 		// write out the open to the parser class
 		out.write("public class " + dom.type + "MessageParser implements " + dom.type + "MessageInfo\n{\n\n");
 		out.write("\tpublic FixSessionValidator validator = new FixSessionValidator() {};\n\n");
 
 		for (final DomFixMessage m : dom.domFixMessages) {
 			out.write("\t" + capFirst(dom.type.toLowerCase()) + capFirst(m.name) + " " + dom.type.toLowerCase() + capFirst(m.name) + 
 					" = new " + dom.type + capFirst(m.name) + "();\n");
 		}
 		out.write("\tFixMessage fixMessage = new FixMessage() {\n");
 		out.write("\t	@Override\n");
 		out.write("\t	public void encode(ByteBuffer out) {}\n");
 		out.write("\t	@Override\n");
 		out.write("\t	public void printBuffer(ByteBuffer out) {}\n");
 		out.write("\t	@Override\n");
 		out.write("\t	public String toString() { return null; }\n");
 		out.write("\t	};\n");
 		out.write("\n");
 		
 		out.write("\t/**");
 		out.write("\t * Returns parsed FixMessage. Buffer is flipped containing the message. If the buffer is to small to contains a fix Message null is returned and the buffer is untouched.\n");
 		out.write("\t * If an exception is thrown, the buffer position is set to its limit, indicating that it should be discarded.\n");
 		out.write("\t * @param buf\n");
 		out.write("\t * @param l\n");
 		out.write("\t * @return\n");
 		out.write("\t * @throws FixSessionException\n");
 		out.write("\t * @throws FixGarbledException\n");
 		out.write("\t */\n");
 		out.write("\tpublic FixMessage parse( " + strReadableByteBuffer + " buf, FixMessageListener l) throws FixSessionException, FixGarbledException {\n\n");
 
 		out.write("\t\tint msgTypeInt = FixMessage.crackMsgType(buf);\n\n");
 
 		out.write("\t\tif (msgTypeInt < 0 ) return null;\n\n");
 
 		out.write("\t\tswitch (msgTypeInt) {\n\n");
 
 		for (final DomFixMessage m : dom.domFixMessages) {
 
 			final String name = dom.type.toLowerCase() + m.name;
 			out.write("\t\t\tcase MsgTypes." + m.name.toUpperCase() + "_INT:\n");
 			out.write("\t\t\t\t" + name + ".setBuffer( buf );\n");
 			out.write("\t\t\t\t" + name + ".getAll();\n");
 			out.write("\t\t\t\tif (!validator.validate(" + name + ")) return "+ name + ";\n");
 			out.write("\t\t\t\tl.on" + capFirst(dom.type.toLowerCase()) + m.name + "(" + name + ");\n");
 			out.write("\t\t\t\treturn "+ name + ";\n");
 		}
 		out.write("\n");
 
 		out.write("\t\t\tdefault:\n");
 		out.write("\t\t\t\tfixMessage.setBuffer(buf);\n");
 		out.write("\t\t\t\tfixMessage.getAll();\n");
 		out.write("\t\t\t\tFixUtils.findEndOfMessage(buf);\n");
 		out.write("\t\t\t\tif (!validator.validate(fixMessage)) return fixMessage;\n");
 		out.write("\t\t\t\tl.onUnknownMessageType( fixMessage );\n");
 		out.write("\t\t\t\treturn fixMessage;\n\n");
 		out.write("\t\t}\n\n");
 		out.write("\t}\n\n");
 
 		// write out the close to the parser class
 		out.write("}\n");
 
 		// done. close out the file
 		out.close();
 	}
 	
 	private void genSessionValidator(FixMessageDom dom, BufferedWriter out) throws IOException 
 	{
 		out.write("package " + dom.packageName + ";\n\n");
 
 		out.write(strFixException + "\n\n");
 
 		out.write("public abstract class FixSessionValidator {\n\n");
 
 		out.write("\tpublic boolean validate(FixMessage msg)  throws FixSessionException {return true;}\n\n");
 
 		out.write("}\n");		
 
 		out.close();
 	}
 
 	private void genTags(final FixMessageDom dom, final BufferedWriter out) throws IOException {
 		// write package line
 		out.write("package " + dom.packageName + ";\n\n");
 
 		writeGeneratedFileHeader(out);
 
 		out.write("public class FixTags {\n\n");
 
 		// write out each callback
 		for (final DomFixField f : dom.domFixFields) {
 
 			out.write("\tpublic static final int " + f.name.toUpperCase() + "_INT = " + f.number + ";\n");
 			out.write("\tpublic static final byte[]  " + f.name.toUpperCase() + " = \"" + f.number + "\".getBytes();\n");
 			if (f.length > 0) {
 				out.write("\tpublic static final int " + f.name.toUpperCase() + "_LENGTH = " + f.length + ";\n");
 			}
 			/* TODO no default length
 			else
 				try {
 					final String l = getJavaLength(f);
 					out.write("\t\tpublic static final int " + f.name.toUpperCase() + "_LENGTH = " + l + ";\n");
 				} catch (final Exception e) {
 					// ignore
 				}
 			*/
 			out.write("\n");
 		}
 
 		// write out the close to the interface
 		out.write("}\n");
 
 		// done. close out the file
 		out.close();
 	}
 
 	private String getEqualExpression(final String name, final String type, String prefix) {
 		switch (FixMessageDom.toInt(type)) {
 
 			case FixMessageDom.STRING:
 			case FixMessageDom.MULTIPLECHARVALUE:
 			case FixMessageDom.MULTIPLESTRINGVALUE:
 			case FixMessageDom.COUNTRY:
 			case FixMessageDom.CURRENCY:
 			case FixMessageDom.EXCHANGE:
 			case FixMessageDom.MONTHYEAR:
 			case FixMessageDom.DATA:
 			case FixMessageDom.XMLDATA:
 			case FixMessageDom.LANGUAGE:
 				return "" + strUtil + ".equals( " + name + ", " + prefix + ")";
 
 			case FixMessageDom.CHAR:
 			case FixMessageDom.INT:
 			case FixMessageDom.LENGTH:
 			case FixMessageDom.TAGNUM:
 			case FixMessageDom.SEQNUM:
 			case FixMessageDom.NUMINGROUP:
 			case FixMessageDom.DAYOFMOUNTH:
 			case FixMessageDom.BOOLEAN:
 			case FixMessageDom.FLOAT:
 			case FixMessageDom.PRICE:
 			case FixMessageDom.QTY:
 			case FixMessageDom.PRICEOFFSET:
 			case FixMessageDom.AMT:
 			case FixMessageDom.PERCENTAGE:
 				return "( " + name + "==" + prefix + ")";
 
 			case FixMessageDom.UTCTIMESTAMP: // timestamps are ignored
 			case FixMessageDom.UTCTIMEONLY:
 			case FixMessageDom.UTCDATEONLY:
 			case FixMessageDom.LOCALMKTDATE:
 			case FixMessageDom.TZTIMEONLY:
 			case FixMessageDom.TZTIMESTAMP:
 				return "true";
 			default:
 				throw new RuntimeException("No idea how to parse this field: " + name);
 		}
 	}
 
 	String getJavaLength(final DomFixField f) {
 
 		switch (FixMessageDom.toInt(f.type)) {
 
 			// TODO more checks
 			case FixMessageDom.STRING:
 			case FixMessageDom.MULTIPLECHARVALUE:
 			case FixMessageDom.MULTIPLESTRINGVALUE:
 			case FixMessageDom.COUNTRY:
 			case FixMessageDom.EXCHANGE:
 			case FixMessageDom.MONTHYEAR:
 			case FixMessageDom.UTCTIMEONLY:
 			case FixMessageDom.UTCDATEONLY:
 			case FixMessageDom.LOCALMKTDATE:
 			case FixMessageDom.TZTIMEONLY:
 			case FixMessageDom.TZTIMESTAMP:
 			case FixMessageDom.DATA:
 			case FixMessageDom.XMLDATA:
 			case FixMessageDom.LANGUAGE: {
 				if (f.length > 0)
 					return String.valueOf(f.length);
 				else if (f.name.toLowerCase().contains("text"))
 					return "FixUtils.FIX_MAX_STRING_TEXT_LENGTH";
 				else
 					return "FixUtils.FIX_MAX_STRING_LENGTH";
 			}
 
 			case FixMessageDom.CURRENCY:
 				return "FixUtils.CURRENCY_LENGTH";
 
 			case FixMessageDom.UTCTIMESTAMP:
 				return "FixUtils.UTCTIMESTAMP_LENGTH";
 
 			default:
 				throw new RuntimeException("No idea how to parse this field: " + f.name);
 		}
 	}
 
 	String getJavaType(final DomFixField f) {
 
 		switch (FixMessageDom.toInt(f.type)) {
 
 			case FixMessageDom.STRING:
 			case FixMessageDom.MULTIPLECHARVALUE:
 			case FixMessageDom.MULTIPLESTRINGVALUE:
 			case FixMessageDom.COUNTRY:
 			case FixMessageDom.CURRENCY:
 			case FixMessageDom.EXCHANGE:
 			case FixMessageDom.MONTHYEAR:
 			case FixMessageDom.UTCTIMESTAMP:
 			case FixMessageDom.UTCTIMEONLY:
 			case FixMessageDom.UTCDATEONLY:
 			case FixMessageDom.LOCALMKTDATE:
 			case FixMessageDom.TZTIMEONLY:
 			case FixMessageDom.TZTIMESTAMP:
 			case FixMessageDom.DATA:
 			case FixMessageDom.XMLDATA:
 			case FixMessageDom.LANGUAGE:
 				return "byte[]";
 
 			case FixMessageDom.CHAR:
 				return "byte";
 
 			case FixMessageDom.INT:
 			case FixMessageDom.LENGTH:
 			case FixMessageDom.TAGNUM:
 			case FixMessageDom.SEQNUM:
 			case FixMessageDom.NUMINGROUP:
 			case FixMessageDom.DAYOFMOUNTH:
 				return "long";
 
 			case FixMessageDom.FLOAT:
 			case FixMessageDom.PRICE:
 			case FixMessageDom.QTY:
 			case FixMessageDom.PRICEOFFSET:
 			case FixMessageDom.AMT:
 			case FixMessageDom.PERCENTAGE:
 				return "long";
 
 			case FixMessageDom.BOOLEAN:
 				return "boolean";
 
 			default:
 				throw new RuntimeException("No idea how to parse this field: " + f.name);
 		}
 	}
 
 	String getJavaTypeNull(final DomFixField f) {
 
 		switch (FixMessageDom.toInt(f.type)) {
 
 			case FixMessageDom.STRING:
 			case FixMessageDom.MULTIPLECHARVALUE:
 			case FixMessageDom.MULTIPLESTRINGVALUE:
 			case FixMessageDom.COUNTRY:
 			case FixMessageDom.CURRENCY:
 			case FixMessageDom.EXCHANGE:
 			case FixMessageDom.MONTHYEAR:
 			case FixMessageDom.UTCTIMESTAMP:
 			case FixMessageDom.UTCTIMEONLY:
 			case FixMessageDom.UTCDATEONLY:
 			case FixMessageDom.LOCALMKTDATE:
 			case FixMessageDom.TZTIMEONLY:
 			case FixMessageDom.TZTIMESTAMP:
 			case FixMessageDom.DATA:
 			case FixMessageDom.XMLDATA:
 			case FixMessageDom.LANGUAGE:
 				return "null";
 
 			case FixMessageDom.CHAR:
 				return "(byte)'0'";
 
 			case FixMessageDom.INT:
 			case FixMessageDom.LENGTH:
 			case FixMessageDom.TAGNUM:
 			case FixMessageDom.SEQNUM:
 			case FixMessageDom.NUMINGROUP:
 			case FixMessageDom.DAYOFMOUNTH:
 				return "0";
 
 			case FixMessageDom.FLOAT:
 			case FixMessageDom.PRICE:
 			case FixMessageDom.QTY:
 			case FixMessageDom.PRICEOFFSET:
 			case FixMessageDom.AMT:
 			case FixMessageDom.PERCENTAGE:
 				return "0";
 
 			case FixMessageDom.BOOLEAN:
 				return "false";
 
 			default:
 				throw new RuntimeException("No idea how to parse this field: " + f.name);
 		}
 	}
 
 	/**
 	 * @param type
 	 * @return
 	 */
 	private boolean isPartOfEqualCopmarison(final String type) {
 		switch (FixMessageDom.toInt(type)) {
 			case FixMessageDom.UTCTIMESTAMP: // timestamps are ignored
 			case FixMessageDom.UTCTIMEONLY:
 			case FixMessageDom.UTCDATEONLY:
 			case FixMessageDom.LOCALMKTDATE:
 			case FixMessageDom.TZTIMEONLY:
 			case FixMessageDom.TZTIMESTAMP:
 				return false;
 			default:
 				return true;
 		}
 	}
 
 	private void printEquals(final BufferedWriter out, final SortedSet<DomBase> fieldsAndComponents) throws IOException {
 		for (final DomBase b : fieldsAndComponents) {
 			if (b instanceof DomFixField) {
 				DomFixField f = (DomFixField) b;
 				if (f.name.equalsIgnoreCase("BodyLength") || f.name.equalsIgnoreCase("CheckSum") || f.name.equalsIgnoreCase("MsgType") || f.name.equalsIgnoreCase("SendingTime") || f.name.equalsIgnoreCase("OrigSendingTime")) continue;
 				if (isPartOfEqualCopmarison(f.type)) {
 					out.write("\t\tif (!" + getEqualExpression(uncapFirst(f.name), f.type, "msg." + uncapFirst(f.name)));
 					out.write(") {\n");
 					out.write("\t\t\tret=false;\n");
 					out.write("\t\t\tprint(\"" + capFirst(f.name) + "\", " + uncapFirst(f.name) + ", msg."+ uncapFirst(f.name) +");\n");
 					out.write("\t\t}\n");
 				}
 			}
 			if (b instanceof DomFixComponentRef) {
 				out.write("\t\tif (!" + uncapFirst(((DomFixComponentRef)b).name) + ".equals(msg." + uncapFirst(((DomFixComponentRef)b).name) + ")");
 				out.write(") ret = false;\n\n");
 			}
 		}
 		out.write("		return ret;\n");
 	}
 	
 	private void writeEnum(final BufferedWriter out, final DomFixField f, final DomFixValue v) throws IOException {
 
 		switch (FixMessageDom.toInt(f.type)) {
 
 			case FixMessageDom.STRING:
 			case FixMessageDom.MULTIPLECHARVALUE:
 			case FixMessageDom.MULTIPLESTRINGVALUE:
 			case FixMessageDom.COUNTRY:
 			case FixMessageDom.CURRENCY:
 			case FixMessageDom.EXCHANGE:
 			case FixMessageDom.MONTHYEAR:
 			case FixMessageDom.UTCTIMESTAMP:
 			case FixMessageDom.UTCTIMEONLY:
 			case FixMessageDom.UTCDATEONLY:
 			case FixMessageDom.LOCALMKTDATE:
 			case FixMessageDom.TZTIMEONLY:
 			case FixMessageDom.TZTIMESTAMP:
 			case FixMessageDom.DATA:
 			case FixMessageDom.XMLDATA:
 			case FixMessageDom.LANGUAGE:
 				out.write("\t\tpublic static final byte[] " + v.description + " = \"" + v.fixEnum + "\".getBytes();\n");
 				break;
 
 			case FixMessageDom.CHAR:
 				out.write("\t\tpublic static final byte " + v.description + " = \'" + v.fixEnum + "\';\n");
 				break;
 
 			case FixMessageDom.INT:
 			case FixMessageDom.LENGTH:
 			case FixMessageDom.TAGNUM:
 			case FixMessageDom.SEQNUM:
 			case FixMessageDom.NUMINGROUP:
 			case FixMessageDom.DAYOFMOUNTH:
 			case FixMessageDom.FLOAT:
 			case FixMessageDom.PRICE:
 			case FixMessageDom.QTY:
 			case FixMessageDom.PRICEOFFSET:
 			case FixMessageDom.AMT:
 			case FixMessageDom.PERCENTAGE:
 				out.write("\t\tpublic static final long " + v.description + " = " + v.fixEnum + ";\n");
 				break;
 
 			case FixMessageDom.BOOLEAN:
 				out.write("\t\tpublic static final boolean " + v.description + " = " + (v.fixEnum.equals("Y") ? "true" : "false") + ";\n");
 				break;
 
 			default:
 				throw new RuntimeException("No idea how to parse this field: " + f.name);
 		}
 
 	}
 
 	private void writeGeneratedFileHeader(final BufferedWriter out) throws IOException {
 
 		out.write("// DO NOT EDIT!!!\n");
 		out.write("// This file is generated by FixMessageGenerator.\n");
 		out.write("// If you need additional functionality, put it in a helper class\n");
 		out.write("// that does not live in this folder!!!  Any java file in this folder \n");
 		out.write("// will be deleted upon the next run of the FixMessageGenerator!\n\n");
 	}
 
 	public void prettyformat(FixMessageDom fixDom) {
 		
 			// TODO Auto-generated method stub
 			
 	}
 	
 }
