 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 public class IniSectionImpl implements IniSection {
 
 	private List<IniOption> optionList;
 	private String name;
 	private IniParser parser;
 	
 	/** 
 	 * Komentar vztahujici se k sekci.
 	 * Jedna se o komentar uvedeny na stejnem radku jako sekce.
 	 */
 	private String inlineComment;
 	
 	/**
 	 * Seznam komentaru predchazejicich sekci.
 	 * Kazda radka je ulozena v samostatnem sekci.
 	 */
 	private List<String> priorComments;
 
 	public IniSectionImpl(String name, IniParser parser) {
 		this.parser = parser;
 		optionList = new LinkedList<IniOption>();
 		this.name = name;
 	}
 
 	@Override
 	public IniOption defineOptBoolean(String option) {
 		IniOption opt = new IniOption(option, OptionType.BOOLEAN, false, true);
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptBoolean(String option, boolean defaultValue) {
 		IniOption opt = new IniOption(option, OptionType.BOOLEAN, false);
 		Element defVal = new Element();
 
 		defVal.setValue(Boolean.toString(defaultValue));
 		try {
 			opt.setDefaultElement(defVal);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptEnum(String option, String enumName) {
 		IniOption opt = new IniOption(option, OptionType.ENUM, false, true);
 		opt.setEnumName(enumName);
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptEnum(String option, String enumName,
 			String defaultValue) throws BadValueException, IniAccessException {
 		IniOption opt = new IniOption(option, OptionType.ENUM, false);
 		
 		if (! parser.isValidForEnum(enumName, defaultValue))
 			throw new BadValueException("Invalid value");
 		
 		Element defVal = new Element();
 		defVal.setValue(defaultValue);
 		
 		opt.setDefaultElement(defVal);
 		opt.setEnumName(enumName);
 		
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptFloat(String option) {
 		IniOption opt = new IniOption(option, OptionType.FLOAT, false, true);
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptFloat(String option, float defaultValue) {
 		IniOption opt = new IniOption(option, OptionType.FLOAT, false);
 		Element defVal = new Element();
 
 		defVal.setValue(Float.toString(defaultValue));
 		try {
 			opt.setDefaultElement(defVal);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptSigned(String option) {
 		IniOption opt = new IniOption(option, OptionType.SIGNED, false, true);
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptSigned(String option, BigInteger defaultValue) {
 		IniOption opt = new IniOption(option, OptionType.SIGNED, false);
 		Element defVal = new Element();
 
 		defVal.setValue(defaultValue.toString());
 		try {
 			opt.setDefaultElement(defVal);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptString(String option) {
 		IniOption opt = new IniOption(option, OptionType.STRING, false, true);
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptString(String option, String defaultValue) {
 		IniOption opt = new IniOption(option, OptionType.STRING, false);
 		Element defVal = new Element();
 
 		defVal.setValue(defaultValue);
 		try {
 			opt.setDefaultElement(defVal);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptUnsigned(String option) {
 		IniOption opt = new IniOption(option, OptionType.UNSIGNED, false, true);
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptUnsigned(String option, BigInteger defaultValue) {
 		IniOption opt = new IniOption(option, OptionType.UNSIGNED, false);
 		Element defVal = new Element();
 
 		defVal.setValue(defaultValue.toString());
 		try {
 			opt.setDefaultElement(defVal);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	/*
 	 * @Override public IniOption defineOptList(String option, String code, char
 	 * delim) { // TODO Auto-generated method stub return null; }
 	 * 
 	 * @Override public IniOption defineOptList(String option, String code, char
 	 * delim, List<String> defaultValues) { // TODO Auto-generated method stub
 	 * return null; }
 	 */
 	@Override
 	public IniOption defineOptListBoolean(String option, char delimiter) {
 		IniOption opt = new IniOption(option, OptionType.BOOLEAN, true, true);
 		try {
 			opt.setDelimiter(delimiter);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListBoolean(String option, char delimiter, 
 		List<Boolean> defaultValue) {
 		
 		IniOption opt = new IniOption(option, OptionType.BOOLEAN, true);
 		LinkedList<Element> defaultValues = new LinkedList<Element>();
 		try {
 			opt.setDelimiter(delimiter);
 		
 		for (Boolean boolVal : defaultValue) {
 			Element element = new Element();
 			element.setValue(boolVal.toString());
 			defaultValues.add(element);
 		}
 
 		opt.setDefaultElementList(defaultValues);
 		optionList.add(opt);
 		
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListEnum(String option, String enumName, 
 		char delimiter) {
 		
 		IniOption opt = new IniOption(option, OptionType.ENUM, true, true);
 		opt.setEnumName(enumName);
 		try {
 			opt.setDelimiter(delimiter);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListEnum(String option, String enumName,
 		char delimiter, List<String> defaultValue) throws BadValueException, IniAccessException{
 		
 		IniOption opt = new IniOption(option, OptionType.ENUM, true);
 		LinkedList<Element> defaultValues = new LinkedList<Element>();
 		opt.setDelimiter(delimiter);
 
 		for (String strVal : defaultValue) {
 			Element element = new Element();
 			
 			if(! parser.isValidForEnum(enumName, strVal))
 				throw new BadValueException("Invalid Value");
 			
 			element.setValue(strVal);
 			defaultValues.add(element);
 		}
 
 		opt.setEnumName(enumName);
 		opt.setDefaultElementList(defaultValues);
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListFloat(String option, char delimiter) {
 		IniOption opt = new IniOption(option, OptionType.FLOAT, true, true);
 		try {
 			opt.setDelimiter(delimiter);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListFloat(String option, char delimiter, 
 		List<Float> defaultValue) {
 		
 		IniOption opt = new IniOption(option, OptionType.FLOAT, true);
 		LinkedList<Element> defaultValues = new LinkedList<Element>();
 		try {
 		opt.setDelimiter(delimiter);
 
 		for (Float floatVal : defaultValue) {
 			Element element = new Element();
 			element.setValue(Float.toString(floatVal));
 			defaultValues.add(element);
 		}
 
 		opt.setDefaultElementList(defaultValues);
 		optionList.add(opt);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListSigned(String option, char delimiter) {
 		IniOption opt = new IniOption(option, OptionType.SIGNED, true, true);
 		try {
 			opt.setDelimiter(delimiter);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListSigned(String option,
 		char delimiter,	List<BigInteger> defaultValue) {
 		
 		IniOption opt = new IniOption(option, OptionType.SIGNED, true);
 		LinkedList<Element> defaultValues = new LinkedList<Element>();
 		try {
 			opt.setDelimiter(delimiter);
 
 			for (BigInteger signedVal : defaultValue) {
 				Element element = new Element();
 				element.setValue(signedVal.toString());
 				defaultValues.add(element);
 			}
 	
 			opt.setDefaultElementList(defaultValues);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListString(String option, char delimiter) {
 		IniOption opt = new IniOption(option, OptionType.STRING, true, true);
 		try {
 			opt.setDelimiter(delimiter);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListString(String option,
 		char delimiter, List<String> defaultValue) {
 		
 		IniOption opt = new IniOption(option, OptionType.STRING, true);
 		LinkedList<Element> defaultValues = new LinkedList<Element>();
 		try {
 			opt.setDelimiter(delimiter);
 	
 			for (String strVal : defaultValue) {
 				Element element = new Element();
 				element.setValue(strVal);
 				defaultValues.add(element);
 			}
 	
 			opt.setDefaultElementList(defaultValues);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListUnsigned(String option, char delimiter) {
 		IniOption opt = new IniOption(option, OptionType.UNSIGNED, true, true);
 		try {
 			opt.setDelimiter(delimiter);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public IniOption defineOptListUnsigned(String option,
 		char delimiter,	List<BigInteger> defaultValue) {
 		
 		IniOption opt = new IniOption(option, OptionType.UNSIGNED, true);
 		LinkedList<Element> defaultValues = new LinkedList<Element>();
 		try {
 			opt.setDelimiter(delimiter);
 	
 			for (BigInteger unsignedVal : defaultValue) {
 				Element element = new Element();
 				element.setValue(unsignedVal.toString());
 				defaultValues.add(element);
 			}
 	
 			opt.setDefaultElementList(defaultValues);
 		} 
 		catch(IniException e) {
 			System.err.println(e.toString());
 		}
 		optionList.add(opt);
 
 		return opt;
 	}
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 
 	@Override
 	public IniOption getOption(String option) {
		
 		// Nalezeni volby
 		int index = optionList.indexOf(option);
 		
 		// Pokud takova volba neni, vratit null
 		if(index == -1)
 			return null;
 		
 		// Vratit volbu
 		IniOption opt = optionList.get(index);
		
 		return opt;
 	}
 
 	@Override
 	public void accept(IniVisitor visitor) {
 		visitor.visit(this);
 		
 		for(IniOption opt : optionList)
 		{
 			opt.accept(visitor);
 		}
 		
 	}
 
 	/**
 	 * Nastaveni radkoveho komentare k sekci
 	 * @param comment radkovy komentar prislusejici sekci 
 	 */
 	@Override
 	public void setInlineComment(String inlineComment) {
 		this.inlineComment = inlineComment;
 		
 	}
 
 	/**
 	 * Zjisteni radkoveho komentare k sekci
 	 * @return radkovy komentar prislusejici sekci 
 	 */
 	@Override
 	public String getInlineComment() {
 		return this.inlineComment;
 	}
 
 	/**
 	 * Nastaveni seznamu komentaru predchazejicich sekci.
 	 * Kazda radka je ulozena v jednom stringu.
 	 * @param priorComments seznam komentaru predchazejicich sekci
 	 */
 	@Override
 	public void setPriorComments(List<String> priorComments) {
 		this.priorComments = priorComments;
 		
 	}
 
 	/**
 	 * Zjisteni seznamu komentaru predchazejicich sekci.
 	 * Kazda radka je ulozena v jednom stringu.
 	 * @return seznam komentaru predchazejicich sekci
 	 */
 	@Override
 	public List<String> getPriorComments() {
 		return this.priorComments;
 	}
 
 }
