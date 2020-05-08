 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.io.BufferedReader;
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.*;
 import java.util.LinkedList;
 
 /**
  * Konkretni implementace interface IniParser
  * @author Vladimir Fiklik, Michal Demin
  *
  */
 public class IniParserImpl implements IniParser {
 	
 	/**
 	 * Seznam sekci
 	 */
 	private List<IniSection> sectionList;
 	
 	/**
 	 * Mapa vyctovych typu <Nazev typu, Mnozina pripustnych hodnot>
 	 */
 	private Map<String, Set<String> > enumMap;
 	
 	/**
 	 * Seznam komentaru na konci ini souboru
 	 * Kazda radka je ulozena v samostatnem stringu
 	 */
 	private List<String> closingComments;
 	
 	/**
 	 * Znakova konstanta reprezentujici zpetne lomitko
 	 */
 	public static final char CHAR_BACKSLASH = '\\';
 	
 	/**
 	 * Znakova konstanta reprezentujici mezeru
 	 */
 	public static final char CHAR_SPACE = ' ';
 	
 	/**
 	 * Znakova konstanta reprezentujici oddelovac komentaru
 	 */
 	public static final char DELIM_COMENT = ';';
 	
 	/**
 	 * Znakova konstanta reprezentujici prirazovaci znak
 	 */
 	public static final char DELIM_OPTION = '=';
 
 	private IniSection parsedSection = null;
 	private ParserAttitude parserAttitude = ParserAttitude.UNDEF;
 	private LinkedList<String> parsedCommentsList = null;
 	private String parsedInlineComment = null;
 
 	public static final String PATTERN_ID = "[a-zA-Z\\.\\:\\$][a-zA-Z0-9\\_\\~\\-\\.\\:\\$\\ ]*";
 	public static final String PATTERN_ID_STRICT = "^[a-zA-Z\\.\\:\\$][a-zA-Z0-9\\_\\~\\-\\.\\:\\$\\ ]*$";
 	public static final String PATTERN_REFER = "\\$\\{" + PATTERN_ID + "\\#" + PATTERN_ID + "\\}";
 	public static final String PATTERN_SECTION = "\\[[^\\]]*\\]"; 
 	public static final String PATTERN_SECTION_STRICT = "\\[[a-zA-Z\\.\\:\\$][a-zA-Z0-9\\_\\~\\-\\.\\:\\$\\ ]*\\]";
 	
 	/**
 	 * Konstruktor, defaultni rezim STRICT
 	 */
 	public IniParserImpl()
 	{
 		sectionList = new LinkedList<IniSection>();
 		enumMap = new HashMap<String, Set<String>> ();
 
 		parserAttitude = ParserAttitude.STRICT;
 	}
 
 	@Override
 	public void setAttitude(ParserAttitude attitude) {
 		this.parserAttitude = attitude;
 	}
 
 	@Override
 	public IniSection addSection(String sectionName) {
 		IniSection section = new IniSectionImpl(sectionName, this);
 		sectionList.add(section);
 
 		// TODO check na unikatnost sekce
 		
 		return section;
 	}
 
 	@Override
 	public void createEnumType(String enumName, String[] values) {
 		Set<String> enumValues = new HashSet<String>();
 
 		for(String val : values)
 			enumValues.add(val);
 
 		enumMap.put(enumName, enumValues);
 
 	}
 
 	@Override
 	public boolean getBoolean(String sectionName, String option) throws BadTypeException, IniAccessException {
 		IniSection section = findSection(sectionName);
 		IniOption opt = section.getOption(option);
 
 		return opt.getValueBool();
 
 	}
 
 	@Override
 	public String getEnum(String sectionName, String option) throws BadTypeException, IniAccessException {
 		IniSection section = findSection(sectionName);
 		IniOption opt = section.getOption(option);
 
 		return opt.getValueEnum();
 	}
 
 	@Override
 	public float getFloat(String sectionName, String option) throws BadTypeException, IniAccessException {
 		IniSection section = findSection(sectionName);
 		IniOption opt = section.getOption(option);
 
 		return opt.getValueFloat();
 	}
 
 	@Override
 	public IniSection getSection(String sectionName) {
 		return getSection(sectionName, false);
 	}
 
 	/**
 	 * Nalezne sekci podle jejiho nazvu. V auto-create rezimu pokud nenalezne
 	 * zadanou sekci, automaticky ji vytvori
 	 * @param sectionName nazev sekce
 	 * @param autoCreate rezim automatickeho vytvareni
 	 * @return hledana sekce, nebo null pokud neni zapnut auto-create rezim
 	 */
 	protected IniSection getSection(String sectionName, boolean autoCreate) {
 		IniSection section = findSection(sectionName);
 
 		if(autoCreate && (section == null))
 			section = addSection(sectionName);
 
 		return section;
 	}
 
 	@Override
 	public BigInteger getSigned(String sectionName, String option) throws BadTypeException, IniAccessException {
 		IniSection section = findSection(sectionName);
 		IniOption opt = section.getOption(option);
 
 		return opt.getValueSigned();
 	}
 
 	@Override
 	public String getString(String sectionName, String option) throws BadTypeException, IniAccessException {
 		IniSection section = findSection(sectionName);
 		IniOption opt = section.getOption(option);
 
 		return opt.getValueString();
 	}
 
 	@Override
 	public BigInteger getUnsigned(String sectionName, String option) throws BadTypeException, IniAccessException {
 		IniSection section = findSection(sectionName);
 		IniOption opt = section.getOption(option);
 
 		return opt.getValueUnsigned();
 	}
 
 	@Override
 	public String getUntyped(String sectionName, String option) throws BadTypeException, IniAccessException {
 		IniSection section = findSection(sectionName);
 		IniOption opt = section.getOption(option);
 
 		return opt.getValueUntyped();
 	}
 
 
 	@Override
 	public boolean isValidForEnum(String enumName, String value){
 
 		Set<String> enumValues = enumMap.get(enumName);
 
 		if(enumValues == null)
 			return false;
 
 		return enumValues.contains(value);
 
 	}
 
 	@Override
 	public void setBoolean(String sectionName, String option, boolean value) throws BadTypeException, IniAccessException {
 		IniOption opt = this.getOption(sectionName, option);
 
 		opt.setValue(value);
 	}
 
 	@Override
 	public IniOption defineOptString(String sectionName, String option) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptString(option);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptString(String sectionName, String option,
 			String defaultValue) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptString(option, defaultValue);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptBoolean(String sectionName, String option) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptBoolean(option);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptBoolean(String sectionName, String option,
 			boolean defaultValue) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptBoolean(option, defaultValue);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptFloat(String sectionName, String option) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptFloat(option);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptFloat(String sectionName, String option,
 			float defaultValue) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptFloat(option, defaultValue);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptSigned(String sectionName, String option) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptSigned(option);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptSigned(String sectionName, String option,
 			BigInteger defaultValue) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptSigned(option, defaultValue);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptUnsigned(String sectionName, String option) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptUnsigned(option);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptUnsigned(String sectionName, String option,
 			BigInteger defaultValue) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptUnsigned(option, defaultValue);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptEnum(String sectionName, String option,
 			String enumName) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptEnum(option, enumName);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptEnum(String sectionName, String option,
 			String enumName, String defaultValue) throws BadValueException {
 		IniSection section = getSection(sectionName, true);
 		
 		IniOption opt = null;
 		
 		try {
 			opt = section.defineOptEnum(option, enumName, defaultValue);
 		} catch (IniAccessException e) {
 			System.err.println(e.toString());
 		}
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListString(String sectionName, String option,
 			char delimiter) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptListString(option, delimiter);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListString(String sectionName, String option,
 			char delimiter, List<String> defaultValue) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptListString(option, delimiter, defaultValue);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListBoolean(String sectionName, String option,
 			char delimiter) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptListBoolean(option, delimiter);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListBoolean(String sectionName, String option,
 			char delimiter, List<Boolean> defaultValue) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptListBoolean(option, delimiter, defaultValue);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListFloat(String sectionName, String option,
 			char delimiter) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptListFloat(option, delimiter);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListFloat(String sectionName, String option,
 			char delimiter, List<Float> defaultValue) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptListFloat(option, delimiter, defaultValue);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListSigned(String sectionName, String option,
 			char delimiter) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptListSigned(option, delimiter);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListSigned(String sectionName, String option,
 			char delimiter, List<BigInteger> defaultValue) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptListSigned(option, delimiter, defaultValue);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListUnsigned(String sectionName, String option,
 			char delimiter) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptListUnsigned(option, delimiter);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListUnsigned(String sectionName, String option,
 			char delimiter, List<BigInteger> defaultValue) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptListUnsigned(option, delimiter, defaultValue);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListEnum(String sectionName, String option,
 			String enumName, char delimiter) {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptListEnum(option, enumName, delimiter);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListEnum(String sectionName, String option,
 			String enumName, char delimiter, List<String> defaultValue)
 			throws BadValueException {
 		IniSection section = getSection(sectionName, true);
 		IniOption opt = section.defineOptListEnum(option, enumName, delimiter, defaultValue);
 
 		return opt;
 	}
 
 	@Override
 	public void setEnum(String sectionName, String option, String enumName, String value) throws BadTypeException, IniAccessException, BadValueException {
 		IniOption opt = this.getOption(sectionName, option);
 
 		opt.setValue(value);
 		opt.setEnumName(enumName);
 	}
 
 	@Override
 	public void setFloat(String sectionName, String option, float value) throws BadTypeException, IniAccessException {
 		IniOption opt = this.getOption(sectionName, option);
 		opt.setValue(value);
 	}
 
 	@Override
 	public void setSigned(String sectionName, String option, BigInteger value) throws BadTypeException, IniAccessException {
 		IniOption opt = this.getOption(sectionName, option);
 		
 		try {
 		opt.setValue(value);
 		} catch (BadValueException e) {
 			System.err.println(e.toString());
 		}
 	}
 
 	@Override
 	public void setString(String sectionName, String option, String value) throws BadTypeException, IniAccessException {
 		IniOption opt = this.getOption(sectionName, option);
 		
 		try {
 			opt.setValue(value);
 		} catch (BadValueException e) {
 			System.err.println(e.toString());
 		}
 	}
 
 	@Override
 	public void setUnsigned(String sectionName, String option, BigInteger value) throws BadTypeException, IniAccessException, BadValueException {
 		IniOption opt = this.getOption(sectionName, option);
 		opt.setValue(value);
 	}
 
 	/**
 	 * Nalezme volbu podle zadaneho nazvu sekce a volby
 	 * @param sectionName nazev sekce
 	 * @param option nazev volby
 	 * @return hledana volba, nebo null pokud neexistuje
 	 */
 	protected IniOption getOption(String sectionName, String option)
 	{
 		return getSection(sectionName).getOption(option);
 	}
 
 	@Override
 	public void accept(IniVisitor visitor) {
 		for( IniSection section : sectionList)
 		{
 			section.accept(visitor);
 		}
 		
 		visitor.visit(this);
 	}
 
 	@Override
 	public void readFile(String fileName) throws IOException {
 		File file = new File(fileName);
 		FileInputStream fs = new FileInputStream(file);
 
 		this.readStream(fs);
 		fs.close();
 	}
 
 	@Override
 	public void readStream(InputStream inStream) throws IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
 		StringBuilder sb = new StringBuilder();
 		String line = null;
 
 		while ((line = br.readLine()) != null) {
 			sb.append(line + "\n");
 		}
 
 		this.readString(sb.toString());
 	}
 
 	@Override
 	public void readString(String inString) {
 		BufferedReader reader = new BufferedReader(new StringReader(inString));
         
 		boolean fail = true;
 		try {
 			String line;
 			int linenum = 0;
 			while ((line = reader.readLine()) != null) {
 				linenum ++;
 
 				if (line.length() == 0) {
 					continue;
 				}
 
 				fail = parseLine(line);
 				if (fail == false) {
 					if (this.parserAttitude == ParserAttitude.STRICT) {
 						System.out.println("Fatal error on line " + linenum);
 						break;
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			parseFinish(fail);
 		}
 
 	}
 
 	@Override
 	public void writeFile(String fileName) throws IOException {
 		File file = new File(fileName);
 		FileOutputStream fs = new FileOutputStream(file);
 
 		this.writeStream(fs);
 		fs.close();
 	}
 
 	@Override
 	public void writeStream(OutputStream outStream) throws IOException {
 		String data = this.writeString();
 		outStream.write(data.getBytes());
 	}
 
 	@Override
 	public String writeString() {
 		StringVisitor sv = new StringVisitor();
 		this.accept(sv);
 
 		return sv.getString();
 	}
 
 	private void parseFinish(boolean fail) {
 			this.setClosingComments(parsedCommentsList);
 
 
 		parsedSection = null;
 		parsedCommentsList = null;
 		parsedInlineComment = "";
 	}
 
 	private void parseAddComment(String comment) {
 		if ((comment == null) || (comment.length() == 0)) {
 			return;
 		}
 
 		if (parsedCommentsList == null) {
 			parsedCommentsList = new LinkedList<String>();
 		}
 
 		System.out.println("Adding to list '" + comment + "'");
 		parsedCommentsList.add(comment);
 	}
 
 	private boolean parseLine(String input) {
 		String strComment = new String("");
 
 		int idxComent = input.indexOf(';');
 		if (idxComent != -1) {
 			strComment = input.substring(idxComent + 1).trim();
 System.out.println("komentar je '" + strComment + "'");
 
 			if (idxComent == 0) {
 				// only comment on line
 				parseAddComment(strComment);
 				return true;
 			}
 
 			input = input.substring(0, idxComent - 1);
 		}
 
 		input = trim_special(input);
 
 		if (input.length() == 0) {
 			if (strComment.length() != 0) {
 				// only comment
 System.out.println("test'" + parsedInlineComment + "'");
 				parseAddComment(strComment);
 				return true;
 			}
 System.out.println("zly test'" + parsedInlineComment + "'");
 			return false;
 		}
 
 		parsedInlineComment = strComment;
 
 		Pattern patSection = Pattern.compile(IniParserImpl.PATTERN_SECTION);
 		Matcher matSection = patSection.matcher(input);
 
 		boolean fail = false;
 		if (matSection.find()) {
 			String in_match = matSection.toMatchResult().group();
 			fail = parseSection(in_match);
 		} else if (input.indexOf(DELIM_OPTION) != -1) {
 			fail = parseOption(input);
 		}
 
 System.out.println("test'" + parsedInlineComment + "'");
 		parseAddComment(parsedInlineComment);
 		return fail;
 	}
 
 	private boolean parseSection(String input) {
 		Pattern patId = Pattern.compile(IniParserImpl.PATTERN_ID);
 		Matcher m = patId.matcher(input);
 
 		if (!m.find()) {
 			System.err.println("Section identifier expected");
 			return false;
 		}
 
 		String sectionID = m.toMatchResult().group();
 
 System.out.println(" Sekcia '" + sectionID + "'"); 
 
		boolean autocreate = false;
		if (parserAttitude != ParserAttitude.STRICT) {
			autocreate = true;
		}
		
		parsedSection = this.getSection(sectionID, autocreate);
 
 		if (parsedSection == null) {
 			System.err.println("no such section defined - '" + sectionID + "'");
 			return false;
 		}
 
 		parsedSection.setPriorComments(parsedCommentsList);
 		parsedSection.setInlineComment(parsedInlineComment);
 
 		parsedCommentsList = null;
 		parsedInlineComment = "";
 
 		return true;
 	}
 
 	private boolean parseOption(String input) {
 		String[] parts = input.split(Character.toString(DELIM_OPTION));
 
 		if (parts.length != 2) {
 			System.err.println(" '" + DELIM_OPTION + "' missing, or more than once on line");
 			return false;
 		}
 
 		Pattern patId = Pattern.compile(IniParserImpl.PATTERN_ID);
 		Matcher m_id = patId.matcher(parts[0]);
 
 		if (!m_id.find()) {
 			System.err.println("Identifier expected");
 			return false;
 		}
 
 		String optionID = m_id.toMatchResult().group();
 		String optionValue = parts[1];
 
 		if (parsedSection == null) {
 			System.err.println("Not in section !! ");
 			return false;
 		}
 
 System.out.print("Option '" + optionID + "' = ");
 		IniOption parsedOption = parsedSection.getOption(optionID);
 
 		if (parsedOption == null) {
			if (parserAttitude == ParserAttitude.STRICT) {
				System.err.println("no such option '" + optionID + "' defined in section '" + parsedSection.getName() + "'");
				return false;
			} else {
				parsedOption = parsedSection.defineOptString(optionID);	
			}
 		}
 
 		parsedOption.setPriorComments(parsedCommentsList);
 if (parsedCommentsList != null) System.out.println("Adding comments '" + parsedCommentsList.toString());
 		parsedOption.setInlineComment(parsedInlineComment);
 		parsedCommentsList = null;
 		parsedInlineComment = "";
 
 		if (parsedOption.isList() == false) {
 			try {
 				Element elem = new Element(optionValue);
 System.out.println(" '" + optionValue + "' ");
 				parsedOption.setElement(elem);
 			}
 			catch(IniException e) {
 				System.err.println(e.toString());
 				return false;
 			}
 		} else {
 			try {
 				String delim = Character.toString(parsedOption.getDelimiter());
 
 				String[] values = optionValue.split(delim);
 
 				LinkedList<Element> listElem = new LinkedList<Element>();
 				for (String value : values) {
 					Element elem = new Element(value);
 System.out.println(" '" + value + "' ");
 					listElem.add(elem);
 				}
 
 				parsedOption.setElementList(listElem);
 			} 
 			catch(IniException e) {
 				System.err.println(e.toString());
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private String trim_special(String input) {
 		boolean isBS = false;
 
 		String output = new String();
 
 		for (char c : input.toCharArray()) {
 			if (c == CHAR_BACKSLASH) {
 				isBS = true;
 				continue;
 			}
 
 			if (!isBS) {
 				if (c == CHAR_SPACE) {
 					continue;
 				}
 			}
 
 			isBS = false;
 
 			output += c;
 		}
 		return output;
 	}
 
 	/**
 	 * Nastaveni seznamu komentaru na konci ini souboru.
 	 * Kazda radka je ulozena v samostatnem stringu.
 	 * @param closingComments seznam komentaru na konci ini souboru
 	 */
 	public void setClosingComments(List<String> closingComments) {
 		this.closingComments = closingComments;
 	}
 
 	/**
 	 * Zjisteni seznamu komentaru na konci ini souboru.
 	 * Kazda radka je ulozena v samostatnem stringu.
 	 * @return seznam komentaru na konci ini souboru
 	 */
 	public List<String> getClosingComments() {
 		return closingComments;
 	}
 	
 	/**
 	 * Najde sekci podle zadaneho jmena sekce
 	 * @param sectionName jmeno hledane sekce
 	 * @return hledana sekce, nebo null pokud takova sekce neexistuje
 	 */
 	private IniSection findSection(String sectionName)
 	{
 		
 		for(IniSection section : sectionList)
 		{
 			if(section.getName().equals(sectionName))
 				return section;
 		}
 
 		return null;
 	}
 }
