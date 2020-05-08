 import java.util.regex.*;
 import java.math.BigInteger;
 
 public class QuarterStatsParser implements LineParser {
 
    private static final String sqlStart_stats = "INSERT company_stats (company_id,date,money,loan,value,trains,roadvs,planes,ships,income,expenses,cargo,tiles) values ";
     private static final String sqlStart_main = "INSERT IGNORE company (company_id,colour,name,founded) values ";
 
    private static final String matchingRegEx = "^(\\d+)\\(([^\\)]*)\\) Company: .* Year Founded: (\\d+) Money: (-?\\d+) Loan: (\\d+) Value: (-?\\d+) \\(T:(\\d+), R:(\\d+), P:(\\d+), S:(\\d+)\\)  Income: (-?\\d+) Expenses: (-?\\d+) Delivered cargo: (-?\\d+) Tiles owned: (-?\\d+)$";
     private static final int matchingGroups = 15;
     private static final Pattern matchingPattern;
 
     public QuarterHeadingParser heading;
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
     public BigInteger income;
     public BigInteger expenses;
     public Integer cargo;
     public Integer tiles;
 
     static {
 	matchingPattern = Pattern.compile(matchingRegEx);
     }
 
     /**
      * A reference to quarter heading is needed to get a right date
      * because it comes before this line.
      */
     QuarterStatsParser(QuarterHeadingParser heading) {
 	this.heading = heading;
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
 	this.income = new BigInteger(matcher.group(12));
 	this.expenses = new BigInteger(matcher.group(13));
 	this.cargo = new Integer(matcher.group(14));
 	this.tiles = new Integer(matcher.group(15));
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
 	sql.appendDate(heading.day);
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
 	sql.appendRaw(',');
 	sql.appendNumber(income);
 	sql.appendRaw(',');
 	sql.appendNumber(expenses);
 	sql.appendRaw(',');
 	sql.appendNumber(cargo);
 	sql.appendRaw(',');
 	sql.appendNumber(tiles);
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
 	this.income = null;
 	this.expenses = null;
 	this.cargo = null;
 	this.tiles = null;
     }
 
     public String parserName() {
 	return "Quarter year stats";
     }
 
     public String engineerDebug() {
 	return "quarter. company: "+ this.company_id + " money: " + this.money;
     }
 }
