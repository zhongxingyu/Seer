 
 
 
 import java.math.BigInteger;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Trida reprezentujici jednu volbu v .ini souboru
  * @author Vladimir Fiklik, Michal Demin
  *
  */
 public class IniOption {
 	/**
 	 * Nazev volby
 	 */
 	private String name;
 	
 	/**
 	 * Urcuje, zda je volba seznamem (true), nebo ma pouze jedinou hodnotu (false)
 	 */
 	private boolean isList;
 	/**
 	 * Typ hodnot teto volby
 	 */
 	private OptionType type;
 	/**
 	 * Urcuje, zda je volba povinna
 	 */
 	private boolean mandatory;
 	/**
 	 * Seznam defaultnich hodnot (pokud je volba jednohodnotova, 
 	 * bere se pouze prvni prvek)
 	 */
 	private List<Element> defaultValueList;
 	/**
 	 * Seznam hodnot (pokud je volba jednohodnotova, 
 	 * bere se pouze prvni prvek)
 	 */
 	private List<Element> valueList;
 	/**
 	 * Nazev vyctoveho typu, k nemuz hodnota patri.
 	 * Pokud nepatri k zadnemu, null
 	 */
 	private String enumName;
 	/**
 	 * Oddelovac jednotlivych prvku seznamu hodnot
 	 */
 	private char delimiter;
 	
 	/**
 	 * Metoda pro inicializaci private promennych
 	 * @param name nazev volby
 	 * @param type typ volby
 	 * @param isList zda je volba seznamem hodnot
 	 */
 	private void init(String name, OptionType type, boolean isList )
 	{
 		this.name = name;
 		this.defaultValueList = new LinkedList<Element>();
 		this.mandatory = false;
 		this.type = type;
 		this.isList = isList;
 	}
 	
 	/**
 	 * 
 	 * @param name Nazev volby
 	 * @param type typ volby
 	 * @param isList zda je volba seznamem hodnot
 	 */
 	public IniOption(String name, OptionType type, boolean isList) {
 		init(name, type, isList);
 	}
 	
 	/**
 	 * 
 	 * @param name Nazev volby
 	 * @param type typ volby
 	 * @param isList zda je volba seznamem hodnot
 	 * @param mandatory zda je volba povinna
 	 */
 	public IniOption(String name, OptionType type, boolean isList, boolean mandatory) {
 		init(name, type, isList);
 		this.mandatory = mandatory;
 	}
 
 	/**
 	 * Test na povinnost volby
 	 * @return true, pokud je volba povinna, jinak false
 	 */
 	public boolean isMandatory() {
 		return mandatory;
 	}
 
 	/**
 	 * Nastaveni povinnosti volby
 	 * @param mandatory povinnost volby
 	 */
 	public void setMandatory(boolean mandatory) {
 		this.mandatory = mandatory;
 	}
 
 	/**
 	 * Nastaveni defaultni hodnoty
 	 * @param defaultValue defaultni hodnota
 	 */
 	public void setDefaultElement(Element defaultValue) {
 		this.defaultValueList.clear();
 		this.defaultValueList.add(defaultValue);
 	}
 
 	/**
 	 * Zjisteni defaultni hodnoty
 	 * @return defaultni hodnota
 	 */
 	public Element getDefaultElement() {
 		if (!this.isList)
 			return defaultValueList.get(0);
 		
 		return null;
 	}
 	
 	/**
 	 * Nastaveni defaultni hodnoty pro list-volbu
 	 * @param defaultValues defaultni seznam hodnot
 	 */
 	public void setDefaultElementList(List<Element> defaultValues) {
 		this.defaultValueList = defaultValues;
 	}
 	
 	/**
 	 * Zjisteni defaultni hodnoty pro list-volbu
 	 * @return defaultni seznam hodnot
 	 */
 	public List<Element> getDefaultElementList() {
 		if(this.isList)
 			return this.defaultValueList;
 		
 		return null;
 	}
 
 	/**
 	 * Zjisteni hodnoty volby
 	 * @return hodnota
 	 */
 	public Element getElement() {
 		if (!this.isList)
 			return valueList.get(0);
 
 		return null;
 	}
 
 	/**
 	 * Nastaveni hodnoty volby
 	 * @param value hodnota
 	 */
 	public void setElement(Element value) {
		if (this.valueList == null) {
			this.valueList = new LinkedList<Element>();
		}
 		this.valueList.clear();
 		this.valueList.add(value);
 	}
 	
 	/**
 	 * Zjisteni seznamu hodnot list-volby
 	 * @return seznam hodnot, null v pripade jednohodnotove volby
 	 */
 	public List<Element> getElementList()
 	{
 		if(this.isList)
 			return this.valueList;
 		
 		return null;
 	}
 	
 	/**
 	 * Nastaveni seznamu hodnot list-volby
 	 * @param values seznam hodnot
 	 */
 	public void setElementList(List<Element> values)
 	{
 		this.valueList = values;
 	}
 
 	/**
 	 * Nastveni nazvu vyctoveho typu, ke kteremu patri hodnoty volby
 	 * @param enumName nazev vyctoveho typu
 	 */
 	public void setEnumName(String enumName) {
 		this.enumName = enumName;
 	}
 
 	/**
 	 * Zjisteni nazvu vyctoveho typu, k nemuz patri hodnoty volby
 	 * @return nazev vyctoveho typu
 	 */
 	public String getEnumName() {
 		return enumName;
 	}
 
 	/**
 	 * Nastaveni oddelovace pro list-volbu
 	 * @param delimiter oddelovac. Pripustne hodnoty ',' a ':' 
 	 */
 	public void setDelimiter(char delimiter) {
 		this.delimiter = delimiter;
 	}
 
 	public char getDelimiter() {
 		return delimiter;
 	}
 	
 	public boolean getValueBool() throws BadTypeException {
 		if(this.type != OptionType.BOOLEAN)
 			throw new BadTypeException("Requested option is not of type Boolean");
 		
 		return Boolean.parseBoolean(this.getValue());
 	}
 	
 	public BigInteger getValueSigned() throws BadTypeException {
 		if(this.type != OptionType.SIGNED)
 			throw new BadTypeException("Requested option is not of type Signed");
 		
 		return new BigInteger(this.getValue());
 	}
 	
 	public BigInteger getValueUnsigned() throws BadTypeException {		
 		if(this.type != OptionType.UNSIGNED)
 			throw new BadTypeException("Requested option is not of type Unsigned");
 		
 		return new BigInteger(this.getValue());
 	}
 	
 	public float getValueFloat() throws BadTypeException {
 		if(this.type != OptionType.FLOAT)
 			throw new BadTypeException("Requested option is not of type Float");
 		
 		return Float.parseFloat(this.getValue());
 	}
 	
 	public String getValueEnum() throws BadTypeException {
 		if(this.type != OptionType.ENUM)
 			throw new BadTypeException("Requested option is not of type Enum");
 		
 		return this.getValue();
 	}
 	
 	public String getValueString() throws BadTypeException {
 		if(this.type != OptionType.STRING)
 			throw new BadTypeException("Requested option is not of type String");
 		
 		return this.getValue();
 	}
 	
 	public String getValueUntyped() throws BadTypeException {
 		return getValue();
 	}
 	
 	public List<Boolean> getValueListBool() throws BadTypeException {
 		
 		if(this.type != OptionType.BOOLEAN)
 			throw new BadTypeException("Requested option is not of type Boolean");
 		
 		List<Element> elementValues = this.getValueList();
 		List<Boolean> resultList = new LinkedList<Boolean>();
 		
 		for(Element element : elementValues)
 		{
 			resultList.add(Boolean.parseBoolean(element.getValue()));
 		}
 		
 		return resultList;
 		
 	}
 
 	public List<String> getValueListString() throws BadTypeException {
 		
 		if(this.type != OptionType.STRING)
 			throw new BadTypeException("Requested option is not of type String");
 		
 		List<Element> elementValues = this.getValueList();
 		List<String> resultList = new LinkedList<String>();
 		
 		for(Element element : elementValues)
 		{
 			resultList.add(element.getValue());
 		}
 		
 		return resultList;
 		
 	}
 
 	public List<Float> getValueListFloat() throws BadTypeException {
 		
 		if(this.type != OptionType.FLOAT)
 			throw new BadTypeException("Requested option is not of type Float");
 		
 		List<Element> elementValues = this.getValueList();
 		List<Float> resultList = new LinkedList<Float>();
 		
 		for(Element element : elementValues)
 		{
 			resultList.add(Float.parseFloat(element.getValue()));
 		}
 		
 		return resultList;
 		
 	}
 
 	public List<BigInteger> getValueListSigned() throws BadTypeException {
 		
 		if(this.type != OptionType.SIGNED)
 			throw new BadTypeException("Requested option is not of type Signed");
 		
 		List<Element> elementValues = this.getValueList();
 		List<BigInteger> resultList = new LinkedList<BigInteger>();
 		
 		for(Element element : elementValues)
 		{
 			resultList.add( new BigInteger(element.getValue()));
 		}
 		
 		return resultList;
 		
 	}
 	
 	public List<BigInteger> getValueListUnsigned() throws BadTypeException {
 		
 		if(this.type != OptionType.UNSIGNED)
 			throw new BadTypeException("Requested option is not of type Unsigned");
 		
 		List<Element> elementValues = this.getValueList();
 		List<BigInteger> resultList = new LinkedList<BigInteger>();
 		
 		for(Element element : elementValues)
 		{
 			resultList.add( new BigInteger(element.getValue()));
 		}
 		
 		return resultList;
 		
 	}
 	
 	public List<String> getValueListEnum() throws BadTypeException {
 		
 		if(this.type != OptionType.ENUM)
 			throw new BadTypeException("Requested option is not of type Enum");
 		
 		List<Element> elementValues = this.getValueList();
 		List<String> resultList = new LinkedList<String>();
 		
 		for(Element element : elementValues)
 		{
 			resultList.add(element.getValue());
 		}
 		
 		return resultList;
 		
 	}
 	
 	public void setValue(String value) {
 		this.valueList.clear();
 		this.valueList.add(new Element(value));
 	}
 	
 	public void setValue( BigInteger value) {
 		this.setValue(value.toString());
 	}
 	
 	public void setValue(float value) {
 		this.setValue(Float.toString(value));
 	}
 	
 	public void setValue(boolean value)	{
 		this.setValue(Boolean.toString(value));
 	}
 	
 	public void accept(IniVisitor visitor){
 		visitor.visit(this);		
 	}
 
 	public String getName() {	
 		return name;
 	}
 	
 	public OptionType getType() {
 		return this.type;
 	}
 	
 	public String getValue() throws BadTypeException
 	{
 		if(this.isList())
 			throw new BadTypeException("List option accessed as single-value option");
 		
 		List<Element> listVal = valueList;
 		
 		if(listVal== null)
 			listVal = defaultValueList;
 		
 		if(listVal == null)
 			return null;
 		
 		return listVal.get(0).getValue();
 		
 	}
 
 	public List<Element> getValueList() throws BadTypeException {
 		
 		if( ! this.isList())
 			throw new BadTypeException("Single-value option accessed as list option");
 		
 		List<Element> listVal = valueList;
 		
 		if(listVal== null)
 			listVal = defaultValueList;
 		
 		return listVal;
 		
 	}
 	
 	public boolean isList()
 	{
 		return this.isList;
 	}
 }
