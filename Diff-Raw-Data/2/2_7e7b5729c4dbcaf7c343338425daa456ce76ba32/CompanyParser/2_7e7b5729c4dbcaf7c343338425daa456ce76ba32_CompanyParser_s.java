 import java.util.regex.*;
 import java.math.BigInteger;
 
 public class CompanyParser implements LineParser {
 
     private static final String sqlStart_stats = "INSERT company_stats (company_id,money,loan,value,trains,roadvs,planes,ships) values ";
     private static final String sqlStart_main = "INSERT IGNORE company (company_id,colour,name,founded) values ";
 
    private static final String matchingRegEx = "^#:(\\d+)\\(([^\\)]*)\\) Company Name: '([^']*)'  Year Founded: (\\d+)  Money: (\\d+)  Loan: (\\d+)  Value: (\\d+)  \\(T:(\\d+), R:(\\d+), P:(\\d+), S:(\\d+)\\).*$";
     private static final int matchingGroups = 11;
     private static final Pattern matchingPattern;
 
     public Integer company_id;
     public String colour;
     public String name;
     public Integer founded;
     public BigInteger money;
     public BigInteger loan;
     public BigInteger value;
     public Integer trains;
     public Integer roadvs;
     public Integer planes;
     public Integer ships;
 
     static {
 	matchingPattern = Pattern.compile(matchingRegEx);
     }
 
     /**
      * Tries to parse a line. Returns false if it's not possible to parse.
      * In the case of true this method clears its previous state ad it is
      * guaranteed to have "fresh" values. In the case of false the object's
      * content is undefined.
      */
     public boolean match(String line) {
 
 	Matcher matcher = matchingPattern.matcher(line);
 	if (!matcher.matches() || matchingGroups != matcher.groupCount()) {
 	    return false; // this is not right pattern
 	}
 	
 	this.company_id = new Integer(matcher.group(1));
 	this.colour = matcher.group(2);
 	this.name = matcher.group(3);
 	this.founded = new Integer(matcher.group(4));
 	this.money = new BigInteger(matcher.group(5));
 	this.loan = new BigInteger(matcher.group(6));
 	this.value = new BigInteger(matcher.group(7));
 	this.trains = new Integer(matcher.group(8));
 	this.roadvs = new Integer(matcher.group(9));
 	this.planes = new Integer(matcher.group(10));
 	this.ships = new Integer(matcher.group(11));
 
 	return true; // Success.
     }
 
     public void appendSQL(SQLBuilder sql) {
 
 	// Company info, database drops this if it's unchanged
 	sql.appendRaw(sqlStart_main);
 	sql.appendRaw('(');
 	sql.appendNumber(company_id);
 	sql.appendRaw(',');
 	sql.appendString(colour);
 	sql.appendRaw(',');
 	sql.appendString(name);
 	sql.appendRaw(',');
 	sql.appendNumber(founded);
 	sql.appendRaw(");");
 	
 	// Company statistics
 	sql.appendRaw(sqlStart_stats);
 	sql.appendRaw('(');
 	sql.appendNumber(company_id);
 	sql.appendRaw(',');
 	sql.appendNumber(money);
 	sql.appendRaw(',');
 	sql.appendNumber(loan);
 	sql.appendRaw(',');
 	sql.appendNumber(value);
 	sql.appendRaw(',');
 	sql.appendNumber(trains);
 	sql.appendRaw(',');
 	sql.appendNumber(roadvs);
 	sql.appendRaw(',');
 	sql.appendNumber(planes);
 	sql.appendRaw(',');
 	sql.appendNumber(ships);
 	sql.appendRaw(");");
     }
 
     public void clear() {
 
 	this.company_id = null;
 	this.money = null;
 	this.loan = null;
 	this.value = null;
 	this.trains = null;
 	this.roadvs = null;
 	this.planes = null;
 	this.ships = null;
     }
 
     public String parserName() {
 	return "Company";
     }
 
     public String engineerDebug() {
 	return "company: "+ this.company_id + " money: " + this.money;
     }
 }
