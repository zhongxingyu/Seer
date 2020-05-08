 import java.io.*;
 import java.math.BigInteger;
 import java.util.List;
 
 /**
  * Interface parseru
  * @author Vladimir Fiklik, Michal Demin
  *
  */
 public interface IniParser {
 	
 	/**
 	 * Nastaveni rezimu prace s ini souborem 
 	 * STRICT - obsah souboru musi presne odpovidat definici
 	 * RELAXED - u znamych optionu probiha kontrola, nezname se povazuji za string
 	 * @param attitude rezim prace 
 	 */
 	public void setAttitude(ParserAttitude attitude);
 
 	/**
 	 * Cteni konfigurace ze souboru
 	 * @param fileName nazev souboru
 	 * @throws IOException V pripade chyby cteni
 	 * @throws ParserException V pripade chyby parsovania
 	 */
 	public void readFile(String fileName) throws IOException, ParserException;
 	
 	/**
 	 * Zapis konfigurace do souboru
 	 * @param fileName nazev souboru
 	 * @throws IOException V pripade chyby zapisu
 	 * @throws ParserException V pripade chyby parsovania
 	 */
 	public void writeFile(String fileName) throws IOException, ParserException;
 	
 	/**
 	 * Cteni konfigurace ze streamu
 	 * @param inStream vstupni stream
 	 * @throws IOException V pripade chyby cteni
 	 * @throws ParserException V pripade chyby parsovania
 	 */
 	public void readStream(InputStream inStream) throws IOException, ParserException;
 	
 	/**
 	 * Zapis konfigurace do streamu
 	 * @param outStream vystupni stream
 	 * @throws IOException V pripade chyby zapisu
 	 * @throws ParserException V pripade chyby parsovania
 	 */
	public void writeStream(OutputStream outStream) throws IOException, ParserExcepion;
 	
 	/**
 	 * Cteni konfigurace ze stringu
 	 * @param inString string s konfiguraci
 	 * @throws ParserException V pripade chyby parsovania
 	 */
 	public void readString(String inString) throws ParserException;
 	
 	/**
 	 * Zapis konfigurace do stringu
 	 * @return string se zapsanou konfiguraci
 	 * @throws ParserException V pripade chyby parsovania
 	 */
 	public String writeString() throws ParserException;
 
 	
 	/**
 	 * Zjisteni hodnoty z volby typu string
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @return hodnota volby
 	 * @throws BadTypeException V pripade pouziti na nespravny typ volby
 	 * @throws IniAccessException V pripade pouziti na list-volbu
 	 */
 	public String getString(String sectionName, String option) throws BadTypeException, IniAccessException;
 	
 	/**
 	 * Zjisteni hodnoty z volby typu float
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @return hodnota volby
 	 * @throws BadTypeException V pripade pouziti na nespravny typ volby
 	 * @throws IniAccessException V pripade pouziti na list-volbu
 	 */
 	public float getFloat(String sectionName, String option) throws BadTypeException, IniAccessException;
 	
 	/**
 	 * Zjisteni hodnoty z volby typu boolean
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @return hodnota volby
 	 * @throws BadTypeException V pripade pouziti na nespravny typ volby
 	 * @throws IniAccessException V pripade pouziti na list-volbu
 	 */
 	public boolean getBoolean(String sectionName, String option) throws BadTypeException, IniAccessException;
 	
 	/**
 	 * Zjisteni hodnoty z volby typu signed
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @return hodnota volby
 	 * @throws BadTypeException V pripade pouziti na nespravny typ volby
 	 * @throws IniAccessException V pripade pouziti na list-volbu
 	 */
 	public BigInteger getSigned(String sectionName, String option) throws BadTypeException, IniAccessException;
 	
 	/**
 	 * Zjisteni hodnoty z volby typu unsigned
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @return hodnota volby
 	 * @throws BadTypeException V pripade pouziti na nespravny typ volby
 	 * @throws IniAccessException V pripade pouziti na list-volbu
 	 */
 	public BigInteger getUnsigned(String sectionName, String option) throws BadTypeException, IniAccessException;
 	
 	/**
 	 * Zjisteni hodnoty z volby typu enum
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @return hodnota volby
 	 * @throws BadTypeException V pripade pouziti na nespravny typ volby
 	 * @throws IniAccessException V pripade pouziti na list-volbu
 	 */
 	public String getEnum(String sectionName, String option) throws BadTypeException, IniAccessException;
 	
 	/**
 	 * Zjisteni hodnoty z volby bez kontroly typu
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @return hodnota volby
 	 */
 	public String getUntyped(String sectionName, String option);
 
 	/**
 	 * Nalezeni volby podle nazvu sekce a nazvu volby
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option nazev hledane volby
 	 * @return hledana volba, nebo null pokud neexistuje
 	 */
 	public IniOption getOption(String sectionName, String option);
 	
 	/**
 	 * Nastaveni hodnoty volbe typu string
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @param value hodnota volby
 	 * @throws BadTypeException V pripade pouziti na nespravny typ volby
 	 * @throws IniAccessException V pripade pouziti na list-volbu
 	 */
 	public void setString(String sectionName, String option, String value) throws BadTypeException, IniAccessException;
 	
 	/**
 	 * Nastaveni hodnoty volbe typu float
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @param value hodnota volby
 	 * @throws BadTypeException V pripade pouziti na nespravny typ volby
 	 * @throws IniAccessException V pripade pouziti na list-volbu
 	 */
 	public void setFloat(String sectionName, String option, float value) throws BadTypeException, IniAccessException;
 	
 	/**
 	 * Nastaveni hodnoty volbe typu boolean
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @param value hodnota volby
 	 * @throws BadTypeException V pripade pouziti na nespravny typ volby
 	 * @throws IniAccessException V pripade pouziti na list-volbu
 	 */
 	public void setBoolean(String sectionName, String option, boolean value) throws BadTypeException, IniAccessException;
 	
 	/**
 	 * Nastaveni hodnoty volbe typu signed
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @param value hodnota volby
 	 * @throws BadTypeException V pripade pouziti na nespravny typ volby
 	 * @throws IniAccessException V pripade pouziti na list-volbu
 	 */
 	public void setSigned(String sectionName, String option, BigInteger value) throws BadTypeException, IniAccessException;
 	
 	/**
 	 * Nastaveni hodnoty volbe typu unsigned
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @param value hodnota volby
 	 * @throws BadTypeException V pripade pouziti na nespravny typ volby
 	 * @throws IniAccessException V pripade pouziti na list-volbu
 	 * @throws BadValueException V pripade dosazovani zaporne hodnoty
 	 */
 	public void setUnsigned(String sectionName, String option, BigInteger value) throws BadTypeException, IniAccessException, BadValueException;
 	
 	/**
 	 * Nastaveni hodnoty volbe typu enum
 	 * @param sectionName nazev sekce do ktere volba patri
 	 * @param option Nazev volby
 	 * @param enumName Nazev vyctoveho typu, do nehoz patri hodnoty volby
 	 * @param value hodnota volby
 	 * @throws BadTypeException V pripade pouziti na nespravny typ volby
 	 * @throws IniAccessException V pripade pouziti na list-volbu
 	 * @throws BadValueException V pripade dosazovani neplatne pro dany vyctovy typ
 	 */
 	public void setEnum(String sectionName, String option, String enumName, String value) throws BadTypeException, IniAccessException, BadValueException;
 
 	/**
 	 * Definice existence volby typu string
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptString(String sectionName, String option);
 	
 	/**
 	 * Definice existence volby typu string a nastaveni jeji defaultni hodnoty
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param defaultValue defaultni hodnota
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptString(String sectionName, String option, String defaultValue);
 	
 	/**
 	 * Definice existence volby typu boolean
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptBoolean(String sectionName, String option);
 	
 	/**
 	 * Definice existence volby typu boolean a nastaveni jeji defaultni hodnoty
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param defaultValue defaultni hodnota
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptBoolean(String sectionName, String option, boolean defaultValue);
 	
 	/**
 	 * Definice existence volby typu float
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptFloat(String sectionName, String option);
 	
 	/**
 	 * Definice existence volby typu float a nastaveni jeji defaultni hodnoty
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param defaultValue defaultni hodnota
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptFloat(String sectionName, String option, float defaultValue);
 	
 	/**
 	 * Definice existence volby typu signed
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptSigned(String sectionName, String option);
 	
 	/**
 	 * Definice existence volby typu signed a nastaveni jeji defaultni hodnoty
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param defaultValue defaultni hodnota
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptSigned(String sectionName, String option, BigInteger defaultValue);
 	
 	/**
 	 * Definice existence volby typu unsigned
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptUnsigned(String sectionName, String option);
 	
 	/**
 	 * Definice existence volby typu unsigned a nastaveni jeji defaultni hodnoty
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param defaultValue defaultni hodnota
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptUnsigned(String sectionName, String option, BigInteger defaultValue);
 	
 	/**
 	 * Definice existence volby typu enum
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param enumName Nazev vyctoveho typu, do nehoz patri hodnoty volby
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptEnum(String sectionName, String option, String enumName);
 	
 	/**
 	 * Definice existence volby typu enum a nastaveni jeji defaultni hodnoty
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param enumName Nazev vyctoveho typu, do nehoz patri hodnoty volby
 	 * @param defaultValue defaultni hodnota
 	 * @return vytvorena volba
 	 * @throws BadValueException V pripade ze default hodnota nepatri do prislusneho vyctoveho typu
 	 */
 	public IniOption defineOptEnum(String sectionName, String option, String enumName, String defaultValue) throws BadValueException;
 
 	
 	/**
 	 * Definice existence list-volby typu string
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param delimiter oddelovac jednotlivych prvku seznamu - pripustne hodnoty ',' a ':'
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptListString(String sectionName, String option, char delimiter);
 	
 	/**
 	 * Definice existence list-volby typu string a nastaveni jejich defaultnich hodnot
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param delimiter oddelovac jednotlivych prvku seznamu - pripustne hodnoty ',' a ':'
 	 * @param defaultValue defaultni hodnota
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptListString(String sectionName, String option, char delimiter, List<String> defaultValue);
 	
 	/**
 	 * Definice existence list-volby typu boolean
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param delimiter oddelovac jednotlivych prvku seznamu - pripustne hodnoty ',' a ':'
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptListBoolean(String sectionName, String option, char delimiter);
 	
 	/**
 	 * Definice existence list-volby typu boolean a nastaveni jejich defaultnich hodnot
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param delimiter oddelovac jednotlivych prvku seznamu - pripustne hodnoty ',' a ':'
 	 * @param defaultValue defaultni hodnota
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptListBoolean(String sectionName, String option, char delimiter, List<Boolean> defaultValue);
 	
 	/**
 	 * Definice existence list-volby typu float
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param delimiter oddelovac jednotlivych prvku seznamu - pripustne hodnoty ',' a ':'
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptListFloat(String sectionName, String option, char delimiter);
 	
 	/**
 	 * Definice existence list-volby typu float a nastaveni jejich defaultnich hodnot
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param delimiter oddelovac jednotlivych prvku seznamu - pripustne hodnoty ',' a ':'
 	 * @param defaultValue defaultni hodnota
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptListFloat(String sectionName, String option, char delimiter, List<Float> defaultValue);
 	
 	/**
 	 * Definice existence list-volby typu signed
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param delimiter oddelovac jednotlivych prvku seznamu - pripustne hodnoty ',' a ':'
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptListSigned(String sectionName, String option, char delimiter);
 	
 	/**
 	 * Definice existence list-volby typu signed a nastaveni jejich defaultnich hodnot
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param delimiter oddelovac jednotlivych prvku seznamu - pripustne hodnoty ',' a ':'
 	 * @param defaultValue defaultni hodnota
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptListSigned(String sectionName, String option, char delimiter, List<BigInteger> defaultValue);
 	
 	/**
 	 * Definice existence list-volby typu unsigned
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param delimiter oddelovac jednotlivych prvku seznamu - pripustne hodnoty ',' a ':'
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptListUnsigned(String sectionName, String option, char delimiter);
 	
 	/**
 	 * Definice existence list-volby typu unsigned a nastaveni jejich defaultnich hodnot
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param delimiter oddelovac jednotlivych prvku seznamu - pripustne hodnoty ',' a ':'
 	 * @param defaultValue defaultni hodnota
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptListUnsigned(String sectionName, String option, char delimiter, List<BigInteger> defaultValue);
 	
 	/**
 	 * Definice existence list-volby typu enum
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param enumName Nazev vyctoveho typu, do nehoz patri hodnoty volby
 	 * @param delimiter oddelovac jednotlivych prvku seznamu - pripustne hodnoty ',' a ':'
 	 * @return vytvorena volba
 	 */
 	public IniOption defineOptListEnum(String sectionName, String option, String enumName, char delimiter);
 	
 	/**
 	 * Definice existence list-volby typu enum a nastaveni jejich defaultnich hodnot
 	 * @param sectionName Nazev sekce do niz volba patri
 	 * @param option Nazev volby
 	 * @param enumName Nazev vyctoveho typu, do nehoz patri hodnoty volby
 	 * @param delimiter oddelovac jednotlivych prvku seznamu - pripustne hodnoty ',' a ':'
 	 * @param defaultValue defaultni hodnota
 	 * @return vytvorena volba
 	 * @throws BadValueException V pripade ze default hodnoty nepatri do prislusneho vyctoveho typu
 	 */
 	public IniOption defineOptListEnum(String sectionName, String option, String enumName, char delimiter, List<String> defaultValue) throws BadValueException;
 
 	/**
 	 * Vytvori skeci prislusneho nazvu
 	 * @param sectionName nazev sekce
 	 * @return vytvorena sekce
 	 * @throws BadValueException V pripade ze vytvarena sekce uz existuje
 	 */
 	public IniSection addSection(String sectionName) throws BadValueException;
 	
 	/**
 	 * Najde sekci podle zadaneho nazvu sekce
 	 * @param sectionName nazev sekce
 	 * @return nalezena sekce pokud existuje, jinak null
 	 */
 	public IniSection getSection(String sectionName);
 
 	/**
 	 * Vytvori vyctovy typ zadaneho nazvu a hodnot
 	 * @param enumName nazev vyctoveho typu
 	 * @param values pripustne hodnoty vyctoveho typu
 	 */
 	public void createEnumType(String enumName, String[] values);
 	
 	/**
 	 * Zjisti, zda jen hodnota platna pro dany vyctovy typ
 	 * @param enumName vyctovy typ, pro nejz se testuje hodnota
 	 * @param value testovana hodnota
 	 * @return true, pokud hodnota patri do daneho vyctoveho typu, jinak false
 	 */
 	public boolean isValidForEnum(String enumName, String value);
 
 	/**
 	 * Vstupni metoda pro visitora (Visitor design pattern)
 	 * @param visitor instance visitora
 	 */
 	public void accept (IniVisitor visitor);
 	
 	/**
 	 * Zjisteni komentaru na konci ini souboru, ktere neprislusi zadne sekci
 	 * Kazda radka je ulozena v samostatnem stringu.
 	 * @return Seznam komentaru na konci souboru nebo null, pokud takove neexistuji
 	 */
 	public List<String> getClosingComments();
 	
 	/**
 	 * Nastaveni komentaru na konci ini souboru, ktere neprislusi zadne sekci
 	 * Kazda radka je ulozena v samostatnem stringu.
 	 * @param closingComments Seznam komentaru
 	 */
 	public void setClosingComments(List<String> closingComments);
 }
