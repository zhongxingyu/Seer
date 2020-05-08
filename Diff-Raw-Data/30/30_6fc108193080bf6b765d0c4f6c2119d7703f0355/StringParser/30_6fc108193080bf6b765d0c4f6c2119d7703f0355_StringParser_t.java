 /**
  * 
  */
 package com.i18n.parser;
 
 import japa.parser.JavaParser;
 import japa.parser.ParseException;
 import japa.parser.ast.CompilationUnit;
 import japa.parser.ast.body.MethodDeclaration;
 import japa.parser.ast.body.Parameter;
 import japa.parser.ast.expr.StringLiteralExpr;
 import japa.parser.ast.visitor.VoidVisitorAdapter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
 
 import org.xml.sax.SAXException;
 
 import com.i18n.file.FileHelper;
 import com.i18n.file.output.IOutputHelper;
 import com.i18n.file.output.XmlHelper;
 import com.i18n.keymaker.IKeyMakerHelper;
 import com.i18n.keymaker.pinyinHelper;
 import com.i18n.parser.util.ILog;
 import com.i18n.parser.util.Log;
 
 /**
  * @author simsun
  *
  */
 public class StringParser {
 	
 	private HashMap<String, String> strMap = new HashMap<String, String>();
 	private Range ct_flag = new Range();
 	private FileHelper file_help = new FileHelper();	
 	private ILog log;
 	
 	private final boolean debug;
 	private final String in_folder;
 	private final String out_folder;
 	private final String xml_file;
 	private final StringFilterType filter_type;
 	private final IOutputHelper output_client;
 	private final IKeyMakerHelper key_maker;
 	
 	
 	private Pattern pat;
 	
 	private StringParser(String in_folder, String out_folder, String xml_file, StringFilterType type, IOutputHelper out_helper,
 			IKeyMakerHelper km, boolean debug, ILog log) {
 		this.debug = debug;
 		this.in_folder = in_folder;
 		this.out_folder = out_folder;
 		this.xml_file = xml_file;
 		this.filter_type = type;
 		this.output_client = out_helper;
 		this.key_maker = km;
 		this.log = log;
 		
 		pat = Pattern.compile(type.getRegex());
 	}
 	
 	public void run() throws ParseException, IOException, ParserConfigurationException, TransformerException, SAXException {
 		List<File> fileList = file_help.getFileList(in_folder, "java");
 		for (File file : fileList) {
 			parserFile(file);
 		}
 	}
 	
 	class Range{
 		Range() {
 			this.has_ct = false;
 		}
 		void setRange(boolean has_ct, int startline, int endline, String ct) {
 			this.has_ct = has_ct;
 			this.startline = startline;
 			this.endline = endline;
 			this.ct_name = ct;
 		}
 		
 		boolean getHas_ct() {
 			return this.has_ct;
 		}
 		
 		String getCt_name() {
 			return this.ct_name;
 		}
 		
 		boolean bIn_scope(int line) {
 			if (line >= startline && line <= endline) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 		private boolean has_ct;
 		private int startline;
 		private int endline;
 		private String ct_name;
 	}
 	
 	private class StringVisitor extends VoidVisitorAdapter {
 		private static final String prefix = "getContext().getString(R.string.";
 		private static final String suffix = ")";
 
 		@Override
 		public void visit(StringLiteralExpr n, Object arg) {
 			String str = n.getValue();
 			String key = null;
 
 			if (isContainsFilter(str)) {
 				try {
 					key = key_maker.convert(str);
 					strMap.put(key, str);
 				} catch (BadHanyuPinyinOutputFormatCombination e) {
 					e.printStackTrace();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 				if (!ct_flag.getHas_ct()
 						|| !ct_flag.bIn_scope(n.getBeginLine())) {
 					n.setValue(new StringBuilder().append(prefix).append(key)
 							.append(suffix).toString());
 				} else {
 					n.setValue(new StringBuilder().append(ct_flag.getCt_name()).append(".getString(R.string.")
 												  .append(key).append(suffix).toString());
 				}
 			} else {
 				n.setValue('"' + str + '"');
 			}
 			super.visit(n, arg);
 		}
 
 		private boolean isContainsFilter(String str) {
 			Matcher match = pat.matcher(str);
 			if (match.find()) {
 				return true;
 			} 
 			return false;
 		}
 
 		@Override
 		public void visit(MethodDeclaration n, Object arg) {
 			ct_flag.setRange(false, 0, 0, "");
 			if (n.getParameters() != null) {
 				for (Parameter pa : n.getParameters()) {
 					if (pa.getType().toString().equals("Context")) {
 						ct_flag.setRange(true, n.getBeginLine(),
 								n.getEndLine(), pa.getId().getName());
 					}
 				}
 			}
 			super.visit(n, arg);
 		}
 	}
 	
 	private void parserFile(File file) throws ParseException, IOException, ParserConfigurationException, TransformerException, SAXException {
 		log.d("Start parse" + file.getPath());
 		
 		FileInputStream in = new FileInputStream(file.getAbsoluteFile());
 		CompilationUnit cu ;
 		try {
 			cu = JavaParser.parse(in);
 		} finally {
 			in.close();
 		}
 		log.d("Start visit" + file.getPath());
 		// find all hard code string, and put them to strMap
 		new StringVisitor().visit(cu, null);
 		
 		//write to xml file		
 		log.d("write xml" + file.getPath());
 		try {
 			output_client.write(strMap);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		//write to java file		
 		log.d("write java" + file.getPath());
 		File out = new File(new StringBuilder().append(out_folder).append(file.getPath()).toString());
 		if (!out.getParentFile().exists()) {
 			out.getParentFile().mkdirs();
 		}
 		if (!out.exists()) {
 			out.createNewFile();
 		}
 		
 		FileOutputStream fw = new FileOutputStream(out);
 		try {
 			fw.write(cu.toString().getBytes());
 		} finally {
 			fw.close();
 		}
 		log.d("End parse" + file.getPath());
 	}
 	
 	public static class Builder {
 		private boolean debug;
 		private String in_folder;
 		private String out_folder;
 		private String xml_file;
 		private StringFilterType filter_type;
 		private IOutputHelper output_client;
 		private IKeyMakerHelper key_maker;
 		private static final String IN_DEFAULT_FOLDER = "test/";
 		private static final String OUT_DEFAULT_FOLDER = "out/";
 		private ILog mlog; 
 		
 		public Builder setDebug(boolean debug) {
 			this.debug = debug;
 			return this;
 		}
 		
 		public Builder setInputFolder(String path) {
 			this.in_folder = path;
 			return this;
 					
 		}
 		
 		public Builder setOutputFolder(String path) {
 			this.out_folder = path;
 			return this;
 		}
 		
 		public Builder setFilterType(StringFilterType type) {
 			this.filter_type = type;
 			return this;
 		}
 		
 		public Builder setOutputClient(IOutputHelper out) {
 			this.output_client = out;
 			return this;
 		}
 		
 		public Builder setKeyMakerHelper(IKeyMakerHelper mk) {
 			this.key_maker = mk;
 			return this;
 		}
		public StringParser build() throws ParserConfigurationException, TransformerException, IOException {
 			ensureSaneDefaults();
 			return new StringParser(in_folder, out_folder, xml_file, filter_type, output_client, key_maker, debug, mlog);
 		}
 		
		private void ensureSaneDefaults() throws ParserConfigurationException, TransformerException, IOException {
 			if (xml_file == null) {
 				xml_file = "./string.xml";
 			}
 			if (in_folder == null) {
 				in_folder = IN_DEFAULT_FOLDER;
 			}
 			if (out_folder == null) {
 				out_folder = OUT_DEFAULT_FOLDER;
 			}
 
 			if (filter_type == null) {
 				filter_type = StringFilterType.ALL;
 			}
 			if (output_client == null) {
 				output_client =  new XmlHelper.Builder()
 									.setDebug(false)
 									.setFilePath(out_folder + xml_file).build();
 			}
 			if (key_maker == null) {
 				key_maker = new pinyinHelper();
 			}
 			if (mlog == null) {
 				mlog = new Log(debug);
 			}
 		}
 	}
 }
