 package csci4370project3;
 
 
 /*******************************************************************************
  * @file  Table.java
  *
  * @author   John Miller
  * @desc CSCI 4370 Project 1. Group 3. Stephen Lago Nick Burlingame Woong Kim Jeffrey Swindle
  */
 
 import java.io.Serializable;
 import static java.lang.Boolean.*;
 import static java.lang.System.out;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.*;
 
 /*******************************************************************************
  * This class implements relational database tables (including attribute names,
  * domains and a list of tuples.  Five basic relational algebra operators are
  * provided: project, select, union, minus and join.  The insert data manipulation
  * operator is also provided.  Missing are update and delete data manipulation
  * operators.
  */
 public class Table
        implements Serializable, Cloneable
 {
     /** Debug flag, turn off once implemented
      */
     private static final boolean DEBUG = true;
 
     /** Counter for naming temporary tables.
      */
     private static int count = 0;
 
     /** Table name.
      */
     private final String name;
 
     /** Array of attribute names.
      */
     private final String [] attribute;
 
     /** Array of attribute domains: a domain may be
      *  integer types: Long, Integer, Short, Byte
      *  real types: Double, Float
      *  string types: Character, String
      */
     private final Class [] domain;
 
     /** Collection of tuples (data storage).
      */
     private final List <Comparable []> tuples;
 
     /** Primary key. 
      */
     private final String [] key;
 
     /** Index into tuples (maps key to tuple).
      */
     private final Map <KeyType, Comparable []> index;
 
     /***************************************************************************
      * Construct an empty table from the meta-data specifications.
      * @param _name       the name of the relation
      * @param _attribute  the string containing attributes names
      * @param _domain     the string containing attribute domains (data types)
      * @param _key        the primary key
      */  
     public Table (String _name, String [] _attribute, Class [] _domain, String [] _key)
     {
         name      = _name;
         attribute = _attribute;
         domain    = _domain;
         key       = _key;
         //tuples    = new ArrayList <> ();                // also try FileList, see below
         tuples    = new FileList (this, tupleSize ());
         
         //Index with simple TreeMap
         //index     = new TreeMap <> ();                  // also try BPTreeMap, LinHash or ExtHash
         
         
         //Index with BpTree
         KeyType keyType = new KeyType(key);
         Class <?> myClass2 = tuples.getClass();
         index = new BpTree(keyType.getClass(), myClass2);
     } // Table
 
 
     /***************************************************************************
      * Construct an empty table from the raw string specifications.
      * @param name        the name of the relation
      * @param attributes  the string containing attributes names
      * @param domains     the string containing attribute domains (data types)
      */
     public Table (String name, String attributes, String domains, String _key)
     {
         this (name, attributes.split (" "), findClass (domains.split (" ")), _key.split(" "));
         out.println ("DDL> create table " + name + " (" + attributes + ")");
     } // Table
 
     /***************************************************************************
      * Construct an empty table using the meta-data of an existing table.
      * @param tab     the table supplying the meta-data
      * @param suffix  the suffix appended to create new table name
      */
     public Table (Table tab, String suffix)
     {
         this (tab.name + suffix, tab.attribute, tab.domain, tab.key);
     } // Table
 
     /***************************************************************************
      * Project the tuples onto a lower dimension by keeping only the given attributes.
      * Check whether the original key is included in the projection.
      * #usage movie.project ("title year studioNo")
      * @param attributeList  the attributes to project onto
      * @return  the table consisting of projected tuples
      * @author Jeffrey Swindle
      */
     public Table project (String attributeList)
     {
         out.println ("RA> " + name + ".project (" + attributeList + ")");
 
         String [] pAttribute = attributeList.split (" ");
         int []    colPos     = match (pAttribute);
         Class []  colDomain  = extractDom (domain, colPos);
         String [] newKey     = null;
         Table result         = null;
         int [] monitor       = new int[key.length];
         
         
         //Check for duplicate attributes
         for( int i = 0 ; i < pAttribute.length; i++){
             
             for( int j = 0 ; j < key.length ; j++ ){
                 
                 if( pAttribute[i].equalsIgnoreCase(key[j])){
                     monitor[j]++;
                 }//If
                 
             }//for
                 
         }//for
         
         //If any original key is not found as a passed attribute return the new table
         //with all passed attributes as the keys
         for( int i : monitor){
             if( i == 0 ){
                 result = new Table (name + count++, pAttribute, colDomain, pAttribute);
                 for (Comparable [] tup : tuples) {
                 	Comparable [] keyVal = new Comparable [result.key.length];
                     int []        cols   = match (result.key);
                     for (int j = 0; j < keyVal.length; j++) keyVal [j] = tup[cols [j]];
                 	if(!(result.index.containsKey(new KeyType (keyVal))))
                 	{
                 		//if the key exists in table1 and not in table2, add the tuple (otherwise skip it)
                 		result.insert(extractTup (tup, colPos));
                 	}
                     
                 } // for
                 return result;
             }//if
         }//for
         
         //If all original key values are found in the passed attributes
         //use them as the keys for the table to return
         result = new Table (name + count++, pAttribute, colDomain, this.key);
         
         for (Comparable [] tup : tuples) {
         	Comparable [] keyVal = new Comparable [result.key.length];
             int []        cols   = match (result.key);
             for (int j = 0; j < keyVal.length; j++) keyVal [j] = tup[cols [j]];
         	if(!(result.index.containsKey(new KeyType (keyVal))))
         	{
         		//if the key exists in table1 and not in table2, add the tuple (otherwise skip it)
         		result.insert(extractTup (tup, colPos));
         	}
         } // for
         
         return result;
     } // project
 
 
     /***************************************************************************
      * Select the tuples satisfying the given condition.
      * A condition is written as infix expression consists of 
      *   6 comparison operators: "==", "!=", "<", "<=", ">", ">="
      *   2 Boolean operators:    "&", "|"  (from high to low precedence)
      * #usage movie.select ("1979 < year & year < 1990")
      * @param condition  the check condition for tuples
      * @return  the table consisting of tuples satisfying the condition
      * @author Woong Kim
      */
     public Table select (String condition)
     {
         out.println ("RA> " + name + ".select (" + condition + ")");
 
         String [] postfix = infix2postfix (condition);
         Table     result  = new Table (name + count++, attribute, domain, key);
         boolean search = false;
         
         for(int i=0;i<postfix.length;i++){
             Comparable[] temp = new Comparable[1];
             temp[0] = postfix[i];
             if(index.containsValue(temp))
             {
                 search = true;
             }
         }
         
         //Get tuples that match the conditions
         for (Comparable [] tup : tuples) {
             if (evalTup (postfix, tup))
             {
                 result.insert(tup);
             }//if
         }
 
         return result;
     } // select
 
         /***************************************************************************
      * Union this table and table2.  Check that the two tables are compatible.
      * #usage movie.union (show)
      * @param table2  the rhs table in the union operation
      * @return  the table representing the union (this U table2)
      * @author Woong Kim
      */
     public Table union (Table table2)
     {
         
         out.println ("RA> " + name + ".union (" + table2.name + ")");
 	//creates the result table
         Table result = new Table (name + count++, attribute, domain, key);
         //if the two tables are incompatible, prints out the error statement
 	if(!this.compatible(table2)){
 		out.println("Incompatible tables");
 	}
 	else{
             //adds first table to result
             for (Comparable [] tup : tuples) {
                 result.insert(tup);
             }
 
             for(int i = 0; i < table2.tuples.size(); i++){
                 Comparable[] current = (Comparable[]) table2.tuples.get(i);
                 Comparable [] keyVal = new Comparable [table2.key.length];
                 int []        cols   = match(result.key);
 
                 for (int j = 0; j < keyVal.length; j++) keyVal [j] = current[cols [j]];
                 if(!(result.index.containsKey(new KeyType (keyVal))))
                 {
                         //if the key exists in table1 and not in table2, add the tuple (otherwise skip it)
                         result.insert(current);
                 }
 
             }
         }
 	//returns the result
         return result;
 	
     } // union
 
 
     /***************************************************************************
      * Take the difference of this table and table2.  Check that the two tables
      * are compatible.
      * #usage movie.minus (show)
      * @param table2  the rhs table in the minus operation
      * @return  the table representing the difference (this - table2)
      * @author Stephen Lago
      */
     public Table minus (Table table2)
     {
         out.println ("RA> " + name + ".minus (" + table2.name + ")");
         //Ensure the tables are compatible, otherwise return a copy of table1
         if(!(this.compatible(table2)))
         {
         	Table result = new Table (this, "minus"); count++;
         	for(int i=0;i<this.tuples.size();i++)
         	{
         		Comparable[] current = (Comparable[]) this.tuples.get(i);
         		result.insert(current);
         	}
         	
         	return(result);
         }
         //create an empty table based on "this" with the prefix "minus"
         Table result = new Table (this, "minus"); count++;
         //iterate over all the tuples in this
         for(int i=0;i<this.tuples.size();i++)
         {
         	//check each tuple, get the key, and check to see if that key also exists in table2
         	Comparable[] current = (Comparable[]) this.tuples.get(i);
         	Comparable [] keyVal = new Comparable [this.key.length];
             int []        cols   = match (this.key);
             for (int j = 0; j < keyVal.length; j++) keyVal [j] = current[cols [j]];
         	if(!(table2.index.containsKey(new KeyType (keyVal))))
         	{
         		//if the key exists in table1 and not in table2, add the tuple (otherwise skip it)
         		result.insert(current);
         	}
         }
         //return the table
         return result;
     } // minus
 
  /***************************************************************************
      * Join this table and table2.  If an attribute name appears in both tables,
      * assume it is from the first table unless it is qualified with the first
      * letter of the second table's name (e.g., "s.").
      * In the result, disambiguate the attribute names in a similar way
      * (e.g., prefix the second occurrence with "s_").
      * Caveat: the key parameter assumes joining the table with the foreign key
      * (this) to the table containing the primary key (table2).
      * #usage movie.join ("studioNo == name", studio);
      * #usage movieStar.join ("name == s.name", starsIn);
      * @param condition  the join condition for tuples
      * @param table2     the rhs table in the join operation
      * @return  the table representing the join (this |><| table2)
      * @author Stephen Lago
      */
     public Table join (String condition, Table table2)
     {
         out.println ("RA> " + name + ".join (" + condition + ", " + table2.name + ")");
 
         Table emptyTable = new Table(name + count++, new String[0], new Class[0], key);
         //first check the condition input to make sure it is valid
         String[] splitCondition = condition.split(" ");
         if(splitCondition.length!=3)
         {
             out.println("Sorry, join condition invalid: format must be \"attribute1name == attribute2Name\"");
             return(emptyTable);
         }
         if(!(splitCondition[1].equalsIgnoreCase("==")))
         {
             out.println("Sorry, join condition invalid: comparator must be \"==\"");
             return(emptyTable);
         }
         //make sure the first attribute in the condition exists in the first table
         int firstValuePos = this.columnPos(splitCondition[0]);
         if(firstValuePos==-1)
         {
             out.println("Sorry, join condition invalid: first attribute does not exist in calling table");
             return(emptyTable);
         }
         //make sure the second attribute in the condition exists in the second table
         int secondValuePos = table2.columnPos(splitCondition[2]);
         if(secondValuePos==-1)
         {
             if(splitCondition[2].startsWith("s."))
             {
                 splitCondition[2] = splitCondition[2].substring(2);
                 secondValuePos = table2.columnPos(splitCondition[2]);
             }
             if(secondValuePos==-1)
             {
                 out.println("Sorry, join condition invalid: second attribute does not exist in parameter table");
                 return(emptyTable);
             }
         }
         //Okay, if we get to this point, the condition must be valid, carry on
         
        boolean isNull = false;
         //First figure out how big the table will be (which should = table1 + table2 - 1)
         int thisTableSize = this.attribute.length;
         int secondTableSize = table2.attribute.length;
         int resultTableSize = thisTableSize + (secondTableSize -1);
         //create appropriate variables to hold attributes and domains for the new table
         String[] resultAttributes = new String[resultTableSize];
         Class[] resultDomains = new Class[resultTableSize];
         //initialize these arrays by adding every attribute of table1
         //and every attribute of table2 EXCEPT for the one named in the condition
         int colCounter = 0;
         //handle the first table
         while(colCounter<thisTableSize)
         {
             resultAttributes[colCounter] = this.attribute[colCounter];
             resultDomains[colCounter] =  this.domain[colCounter];
             colCounter++;
         }
         //handle the second table
         int table2Counter = (colCounter - thisTableSize);
         while(colCounter<resultTableSize)
         {
             //if it isn't the exception, carry on 
             if(table2Counter != secondValuePos)
             {
                 //check against the first table's attributes to look for prefixing requirements
                 String dupPrefix = "s_";
                 String current2Attr = table2.attribute[table2Counter];
                 for(int i=0;i<thisTableSize;i++)
                 {
                     String current1Attr = this.attribute[i];
                     //if the attribute name already exists in table 1, add a prefix to the table 2 attribute name
                     if(current1Attr.equalsIgnoreCase(current2Attr))
                     {
                         current2Attr = dupPrefix + current2Attr;
                         break;
                     }
                 }
                 //carry on
                 resultAttributes[colCounter] = current2Attr;
                 resultDomains[colCounter] = table2.domain[table2Counter];
                 //if it is the exception, leave the table2 counter, but back up on the colCounter, then carry on without adding anything
             }else
             {
                 colCounter--;
             }
             colCounter++;
             table2Counter++;
         }
         
         //create the new table
         Table result = new Table (name + count++, resultAttributes, resultDomains, key);
 
         //now we can insert the tuples into the table
         //go through every tuple of the first table
         int tupCounter = 0;
         if(this.tuples.size()==0)
         {
             out.println("There are no tuples in the first table, therefore join results in empty table");
         }
         while(tupCounter < this.tuples.size())
         {
             //make a new tuple
             Comparable[] newTup = new Comparable[resultTableSize];
             //go through the first table's attributes and assign as usual
             colCounter = 0;
             while(colCounter<thisTableSize)
             {
                 int[] pos = new int[1];
                 pos[0] = colCounter;
                 Comparable[] thisTupVal = extractTup(this.tuples.get(tupCounter),pos);
                 newTup[colCounter] = thisTupVal[0];
                 colCounter++;
             }
             //find the foreign key value in this table
             Comparable[] fKey = new Comparable[1];
             fKey[0] = newTup[firstValuePos];
             //now find the matching tuple in the second table
             int tup2Counter = -1;
             int[] pos = new int[1];
             pos[0] = secondValuePos;
             Comparable[] reference;
             //Comparable[] reference = new Comparable[1];
             try{
             	reference = table2.index.get(new KeyType(fKey));
             }catch(java.lang.ClassCastException e)
             {
             	out.println("Sorry, join conditions invalid: attribute 2 is not primary key ");
             	return(emptyTable);
             }
 
             //if we get here, the correct tuple number is in tup2Counter
             int col2Counter = 0;
             //add the values of the referenced tuple to the result tuple
             
             while(colCounter<resultTableSize)
             {
                 if(col2Counter!=secondValuePos)
                 {
                     int[] pos2 = new int[1];
                     pos2[0] = col2Counter;
                     //Comparable[] referencedTup = extractTup(table2.tuples.get(tup2Counter),pos2);
                     try{
                     	newTup[colCounter] = reference[col2Counter];
                     }catch(Exception e)
                     {
                    	isNull = true;
                    	break;
                    	//out.println("Sorry, join condition invalid: attribute 1 is not a foreign key to attribute 2");
                    	//return(emptyTable);
                     }
                     //we still have to skip the exception (join condition attribute)
                 }else
                 {
                     colCounter--;
                 }
                 colCounter++;
                 col2Counter++;
             }
 
             //insert the resulting tuple
            if(!isNull){result.insert(newTup);}
            isNull = false;
 
             //increment
             tupCounter++;
         }
         
         //all done
         return result;
     } // join
 
 
     /***************************************************************************
      * Insert a tuple to the table.
      * #usage movie.insert ("'Star_Wars'", 1977, 124, "T", "Fox", 12345)
      * @param tup  the array of attribute values forming the tuple
      * @return  whether insertion was successful
      */
     public boolean insert (Comparable [] tup)
     {
         out.println ("DML> insert into " + name + " values ( " + Arrays.toString (tup) + " )");
 
         if (typeCheck (tup, domain)) {
             tuples.add (tup);
             Comparable [] keyVal = new Comparable [key.length];
             int []        cols   = match (key);
             for (int j = 0; j < keyVal.length; j++) keyVal [j] = tup [cols [j]];
             index.put (new KeyType (keyVal), tup);
             return true;
         } else {
             return false;
         } // if
     } // insert
 
     /***************************************************************************
      * Get the name of the table.
      * @return  the table's name
      */
     public String getName ()
     {
         return name;
     } // getName
 
     /***************************************************************************
      * Print the table.
      */
     public void print ()
     {
         out.println ("\n Table " + name);
 
         out.print ("|-");
         for (int i = 0; i < attribute.length; i++) {
             out.print ("---------------");
         }
         out.println ("-|");
         out.print ("| ");
         for (String a : attribute) {
             out.printf ("%15s", a);
         }
         out.println (" |");
 
         if (DEBUG) {
             out.print ("|-");
             for (int i = 0; i < domain.length; i++) out.print ("---------------");
             out.println ("-|");
             out.print ("| ");
             for (Class d : domain) out.printf ("%15s", d.getSimpleName ());
             out.println (" |");
         } // if
 
         out.print ("|-");
         for (int i = 0; i < attribute.length; i++) out.print ("---------------");
         out.println ("-|");
         for (Comparable [] tup : tuples) {
             out.print ("| ");
             for (Comparable attr : tup) {
                 out.printf ("%15s", attr);
             }
             out.println (" |");
         } // for
         out.print ("|-");
         for (int i = 0; i < attribute.length; i++) out.print ("---------------");
         out.println ("-|");
     } // print
 
     /***************************************************************************
      * Determine whether the two tables (this and table2) are compatible, i.e.,
      * have the same number of attributes each with the same corresponding domain.
      * @param table2  the rhs table
      * @return  whether the two tables are compatible
      * @author Stephen Lago
      */
     private boolean compatible (Table table2)
     {
         //currently checks that both tables have the same number of domains
     	//then checks to see if each domain is the same type as the relative one in the other table and has the same name
     	
     	//assume they are compatible, and look for reasons that they aren't
     	boolean areCompatible = true;
         Class[] thisAttributes = this.domain;
         Class[] thoseAttributes = table2.domain;
         //checks to see if both tables have the same number of domains
         if(thisAttributes.length!=thoseAttributes.length)
         {
         	return(false);
         }
         //now compares relative domains to ensure same type and same name
         //(e.g. Table1.domain1 is the same type as Table2.domain1)
         for(int i=0;i<thisAttributes.length;i++)
         {
         	int[] pos = new int[1];
         	pos[0] = i;
         	Class[] thisExtract = Table.extractDom(thisAttributes,pos);
         	Class[] thoseExtract = Table.extractDom(thoseAttributes,pos);
         	//checking for type equality
         	if(thisExtract[0]!=thoseExtract[0])
         	{
         		return(false);
         	}
         	String thisName = this.attribute[i];
         	String thoseName = table2.attribute[i];
         	//checking for name equality
         	if(!(thisName.equals(thoseName)))
         	{
         		return(false);
         	}
         }
         return(areCompatible);
     } // compatible
 
     /***************************************************************************
      * Return the column position for the given column/attribute name.
      * @param column  the given column/attribute name
      * @return  the column index position
      */
     private int columnPos (String column)
     {
         for (int j = 0; j < attribute.length; j++) {
            if (column.equals (attribute [j])) return j;
         } // for
 
         return -1;  // column name not found in this table
     } // columnPos
 
     /***************************************************************************
      * Return all the column positions for the given column/attribute names.
      * @param columns  the array of column/attribute names
      * @return  the array of column index positions
      */
     private int [] match (String [] columns)
     {
         int [] colPos = new int [columns.length];
 
         for (int i = 0; i < columns.length; i++) {
             colPos [i] = columnPos (columns [i]);
         } // for
 
         return colPos;
     } // match
 
     /***************************************************************************
      * Check whether the tuple satisfies the condition.  Use a stack-based postfix
      * expression evaluation algorithm.
      * @param postfix  the postfix expression for the condition
      * @param tup      the tuple to check
      * @return  whether to keep the tuple
      * @author Nick Burlingame
      */
     @SuppressWarnings("unchecked")
     private boolean evalTup (String [] postfix, Comparable [] tup)
     {
                 if (postfix == null) return true;
         Stack <Comparable <?>> spre = new Stack <> ();
         Stack <Comparable <?>> s = new Stack <> ();
         
         for(int i = 0; i < postfix.length; i++){
             if(postfix[i].startsWith("'") && postfix[i].endsWith("'")){
                 postfix[i] = postfix[i].substring(1,postfix[i].length()-1);
             }
         }
         
         //put it in backwards
         for (String token : postfix) { spre.push(token); } // for
         
         //and let's just fix that to put it in right
         for(String token : postfix){ s.push(spre.pop()); }
         
         boolean result = false;
         Stack<Comparable <?>> bs = new Stack<>();
         
         while(!(s.empty()))
         {
         	//get the next value in the post fix expresssion
         	String value1 = (String) s.pop();
         	//if it is an & comparator
         	if(value1.equals("&"))
         	{
         		boolean bvalue1 = (Boolean) bs.pop();
         		boolean bvalue2 = (Boolean) bs.pop();
         		boolean tempValue = (bvalue1 && bvalue2);
         		bs.push(tempValue);
         	//if it is an | comparator
         	}else if(value1.equals("|"))
         	{
         		boolean bvalue1 = (Boolean) bs.pop();
         		boolean bvalue2 = (Boolean) bs.pop();
         		boolean tempValue = (bvalue1 || bvalue2);
         		bs.push(tempValue);
         	//if it is an equality/inequality operator
         	}else
         	{
         		String value2 = (String) s.pop();
         		String value3 = (String) s.pop();
         		int[] pos = new int[1];
         		pos[0] = columnPos(value1);
         		Comparable tableValue;
         		String sQueryValue;
         		Comparable queryValue;
         		Class tupClass;
         		boolean reversed = false;
         		//if the second value is the attribute name
         		if(pos[0]==-1)
         		{
         			pos[0] = columnPos(value2);
             		tableValue = tup[pos[0]];
             		sQueryValue = value1;
             		tupClass = this.domain[pos[0]];
             		reversed = true;
                         //if the first value is the attribute name
         		}else
         		{
             		tableValue = tup[pos[0]];
             		sQueryValue = value2;
             		tupClass = this.domain[pos[0]];
         		}
 
 				queryValue = String2Type.cons(tupClass,sQueryValue);
 
         		//start comparing
         		if(value3.equals("=="))
         		{
         			boolean tempValue = (tableValue.compareTo(queryValue)==0);
         			bs.push(tempValue);
         		}else if(value3.equals("!="))
         		{
         			boolean tempValue = (tableValue.compareTo(queryValue)!=0);
         			bs.push(tempValue);
         		}else if(value3.equals("<"))
         		{
         			boolean tempValue;
         			if(!reversed)
         			{
         				tempValue = (tableValue.compareTo(queryValue)<0);
         			}else
         			{
         				tempValue = (queryValue.compareTo(tableValue)<0);
         			}
         			bs.push(tempValue);
         		}else if(value3.equals("<="))
         		{
         			boolean tempValue;
         			if(!reversed)
         			{
         				tempValue = (tableValue.compareTo(queryValue)<=0);
         			}else
         			{
         				tempValue = (queryValue.compareTo(tableValue)<=0);
         			}
         			bs.push(tempValue);
         		}else if(value3.equals(">"))
         		{
         			boolean tempValue;
         			if(!reversed)
         			{
         				tempValue = (tableValue.compareTo(queryValue)>0);
         			}else
         			{
         				tempValue = (queryValue.compareTo(tableValue)>0);
         			}
         			bs.push(tempValue);
         		}else if(value3.equals(">="))
         		{
         			boolean tempValue;
         			if(!reversed)
         			{
         				tempValue = (tableValue.compareTo(queryValue)>=0);
         			}else
         			{
         				tempValue = (queryValue.compareTo(tableValue)>=0);
         			}
         			bs.push(tempValue);
         		}
         	}
         	//"==", "!=", "<", "<=", ">", ">=" "&", "|"  (from high to low precedence)
         }
         
         result = (Boolean) bs.pop();
 
         return(result);
     } // evalTup
 
     /***************************************************************************
      * Pack tuple tup into a record/byte-buffer (array of bytes).
      * @param tup  the array of attribute values forming the tuple
      * @return  a tuple packed into a record/byte-buffer
      * @author Jeffrey Swindle
     */ 
     public byte [] pack (Comparable [] tup)
     {
         byte [] record = new byte [tupleSize ()];
         byte [] b      = null;
         int     s      = 0;
         int     i      = 0;
 
         //Convert each attribute to its corresponding byte representation
         //Using big endian for the byte storage
         for (int j = 0; j < domain.length; j++) {
             switch (domain [j].getName ()) {
             case "java.lang.Byte": 
                 b = new byte[] {(byte) tup[j]};
                 s = 1;  
                 break;
             case "java.lang.Short": 
                 b = Conversions.short2ByteArray((Short) tup[j]);
                 s = 2;  
                 break;
             case "java.lang.Integer":
                 b = Conversions.int2ByteArray ((Integer) tup [j]);
                 s = 4;
                 break;
             case "java.lang.Long": 
                 b = Conversions.long2ByteArray((Long) tup[j]);
                 s =8 ;  
                 break;  
             case "java.lang.Float": 
                 b = Conversions.float2ByteArray((Float) tup[j]);
                 s = 4;  
                 break;
             case "java.lang.Double": 
                 b = Conversions.double2ByteArray((Double) tup[j]);
                 s = 8;  
                 break;
             case "java.lang.Character": 
                 String tempString = Character.toString((char)tup[j]);
                 byte[] tempByteArray = tempString.getBytes();
                 b = new byte[2];
                 b[0] = tempByteArray[0];
                 b[1] = 0;
                 s = 2;  
                 break;
             case "java.lang.String":
                 //Using 64 bytes to represent each string
                 //Copy everything up to 64 bytes in the string and then fill
                 //with nulls
                 byte[] temp = new byte[64];
                 byte[] temp2 = ((String) tup [j]).getBytes();
                 for( int k = 0 ; k < temp2.length ; k++ ){
                     temp[k] = temp2[k];
                 }
                 for( int k = temp2.length ; k < 64 ; k++ ){
                     temp[k] = 0;
                 }
                 b = temp;
                 s = 64;
                 break;
 
             } // switch
             if (b == null) {
                 out.println ("Table.pack: byte array b is null");
                 return null;
             } // if
             for (int k = 0; k < s; k++) {
                 record [i++] = b [k];
             }
         } // for
         return record;
     } // pack
     
     /***************************************************************************
      * Unpack the record/byte-buffer (array of bytes) to reconstruct a tuple.
      * @param record  the byte-buffer in which the tuple is packed
      * @return  an unpacked tuple
      * @author Jeffrey Swindle
      */
     public Comparable [] unpack (byte [] record)
     {
         Comparable [] tup = new Comparable[domain.length];
         
         //Setup a byte buffer to parse record and instantiate each attribute with
         //its appropriate type
         ByteBuffer buffer = ByteBuffer.wrap(record);
         //Byte order is big endian
         buffer.order(ByteOrder.BIG_ENDIAN);
         
         //For each domain in the table get the correct type's bytes
         for (int i = 0; i < domain.length; i++) {
             switch (domain [i].getName ()) {
             case "java.lang.Byte": 
                 tup[i] = buffer.get();
                 break;
             case "java.lang.Short": 
                 tup[i] = buffer.getShort();
                 break;
             case "java.lang.Integer":
                 tup[i] = buffer.getInt();
                 break;
             case "java.lang.Long": 
                 tup[i] = buffer.getLong();
                 break;  
             case "java.lang.Float": 
                 tup[i] = buffer.getFloat();  
                 break;
             case "java.lang.Double": 
                 tup[i] = buffer.getDouble();
                 break;
             case "java.lang.Character": 
                 tup[i] = (char)buffer.get();  
                 buffer.get();
                 break;
             case "java.lang.String":
                 //Strings are setup as fixed, 64 bytes, when written so read
                 //64 bytes and trim off unneeded bytes
                 byte b[] = new byte[64];
                 for( int j = 0 ; j < 64 ; j++ ){
                     b[j] = buffer.get();
                 }
                 String temp = new String(b).trim();
                 tup[i] = temp;
                 break;
             } // switch
         }//for
         return tup;
     } // unpack
     
 
     /***************************************************************************
      * Determine the size of tuples in this table in terms of the number of bytes
      * required to store it in a record/byte-buffer.
      * Attribute domains: a domain may be
      *  integer types: Long, Integer, Short, Byte
      *  real types: Double, Float
      *  string types: Character, String
      * @return  the size of packed-tuples in bytes
      * @author Jeffrey Swindle
      */
     private int tupleSize ()
     {
         int s = 0;
 
         for (int j = 0; j < domain.length; j++) {
             switch (domain [j].getName ()) {
                 case "java.lang.Byte": s += 1;  break;
                 case "java.lang.Short": s += 2;  break;
                 case "java.lang.Integer": s += 4;  break;
                 case "java.lang.Long": s +=8 ;  break;  
                 case "java.lang.Float": s += 4;  break;
                 case "java.lang.Double": s += 8;  break;
                 case "java.lang.Character": s += 2;  break;
                 case "java.lang.String":  s += 64; break;
             } // switch
         } // for
 
         return s;
     } // tupleSize
 
     //------------------------ Static Utility Methods --------------------------
 
     /***************************************************************************
      * Check the size of the tuple (number of elements in list) as well as the
      * type of each value to ensure it is from the right domain. 
      * @param tup  the tuple as a list of attribute values
      * @param dom  the domains (attribute types)
      * @return  whether the tuple has the right size and values that comply
      *          with the given domains
      * @author Jeffrey Swindle
      */
     private static boolean typeCheck (Comparable [] tup, Class [] dom)
     { 
 
         //Check the length
         if( tup.length != dom.length )
             return false;
         
         //Check the class types
         for( int i = 0 ; i < tup.length ; i++ ){
             if( tup[i].getClass() != dom[i] )
                    return false;
         }//for
 
         return true;
     } // typeCheck
 
     /***************************************************************************
      * Determine if the token/op is a comparison operator.
      * @param op  the token/op to check
      * @return  whether it a comparison operator
      */
     private static boolean isComparison (String op)
     {
         return op.equals ("==") || op.equals ("!=") ||
                op.equals ("<")  || op.equals ("<=") ||
                op.equals (">")  || op.equals (">=");
     } // isComparison
 
     /***************************************************************************
      * Compare values x and y according to the comparison operator.
      * @param   x   the first operand
      * @param   op  the comparison operator
      * @param   y   the second operand
      * @return  whether the comparison evaluates to true or false
      */
     @SuppressWarnings("unchecked")
     private static boolean compare (Comparable x, String op , Comparable y)
     {
         switch (op) {
         case "==": return x.compareTo (y) == 0;
         case "!=": return x.compareTo (y) != 0;
         case "<":  return x.compareTo (y) <  0;
         case "<=": return x.compareTo (y) <= 0;
         case ">":  return x.compareTo (y) >  0;
         case ">=": return x.compareTo (y) >= 0;
         default: { out.println ("compare: error - unexpected op"); return false; }
         } // switch
     } // compare
 
     /***************************************************************************
      * Convert an untokenized infix expression to a tokenized postfix expression.
      * This implementation does not handle parentheses ( ).
      * Ex: "1979 < year & year < 1990" --> { "1979", "year", "<", "year", "1990", "<", "&" } 
      * @param condition  the untokenized infix condition
      * @return  resultant tokenized postfix expression
      * @author Nick Burlingame
      */
     private static String [] infix2postfix (String condition)
     {
  	//Given stuff
 	if(condition == null || condition.trim() == "") return null;
 	String[] infix = condition.split(" ");
 	String[] postfix = new String[infix.length];
 
 	String[] errorReturn = new String[0];
 	//Error checking. The condition should never be of even length.
 	if(infix.length%2 == 0){
 		System.out.println("ERROR: CONDITION FORMATTED INCORRECTLY");
 		return errorReturn;
 	}
 		
 	//Error checking. Returns null if an operator or a value are ever next to each other.
 	String previous = "";
 	for(int i = 0; i < infix.length; i++){
 		String current = infix[i];
 		if((isComparison(previous) || previous.equals("&") || previous.equals("|")) && (isComparison(current) || current.equals("&") || current.equals("|"))){
 			System.out.println("ERROR: TWO OPERATORS ARE PLACE NEXT TO EACHOTHER");
 			return errorReturn;
 		}
 		if((!isComparison(previous) && !previous.equals("&") && !previous.equals("|") && !previous.equals("")) && (!isComparison(current) && !current.equals("&") && !current.equals("|"))){
 			System.out.println("ERROR: TWO VALUES ARE PLACE NEXT TO EACHOTHER");
 			return errorReturn;
 		}
 		previous = current;
 	}
 	
 	//If the length is three, and it's passed error checking then we can simply put it into postfix form.
 	if(condition.split(" ").length == 3){
 		postfix[0] = condition.split(" ")[0];
 		postfix[1] = condition.split(" ")[2];
 		postfix[2] = condition.split(" ")[1];
 		return postfix;
 	}
 
 	//currentString keeps track of every string that's been tried so far.
 	String leftString = "";
 	String rightString = "";
 		
 	//This for loop checks for the | symbol which has the highest priority and sticks it on the end.
 	boolean orFound = false;
 	int orPosition = 0;
 	for(int i = 0; i < infix.length; i++){
 		if (infix[i].equals("|")){
 			orFound = true;
 			orPosition = i;
 		}
 	}
 	//Once we've found the location of the last or, split the string in half and recursively send both halves back through the method. 
 	//Then put them back together.
 	if(orFound == true){
 		String[] leftSideArray = new String[orPosition];
 		System.arraycopy(infix, 0, leftSideArray, 0, leftSideArray.length);
            	for(int i = 0; i < leftSideArray.length; i++){
 			leftString += leftSideArray[i] + " ";
 		}
 		leftSideArray = infix2postfix(leftString.trim());
 		System.arraycopy(leftSideArray, 0, postfix, 0, leftSideArray.length);
 		String[] rightSideArray = new String[infix.length - orPosition - 1];
 		System.arraycopy(infix, orPosition + 1, rightSideArray, 0, rightSideArray.length);
 		for(int i = 0; i < rightSideArray.length; i++){
 			rightString += rightSideArray[i] + " ";
 		}
 		rightSideArray = infix2postfix(rightString.trim());
 		System.arraycopy(rightSideArray, 0, postfix, orPosition, rightSideArray.length);
 		postfix[postfix.length-1] = "|";
 		return postfix;
 	}
 	
 	//Same as above except with and.
 	boolean andFound = false;
 	int andPosition = 0;
 	for(int i = 0; i < infix.length; i++){
 		if (infix[i].equals("&")){
 			andFound = true;
 			andPosition = i;
 		}
 	}
 		
 	if(andFound == true){
 		String[] leftSideArray = new String[andPosition];
 		System.arraycopy(infix, 0, leftSideArray, 0, leftSideArray.length);
 		for(int i = 0; i < leftSideArray.length; i++){
 			leftString += leftSideArray[i] + " ";
 		}
 		leftSideArray = infix2postfix(leftString.trim());
 		System.arraycopy(leftSideArray, 0, postfix, 0, leftSideArray.length);
 		String[] rightSideArray = new String[infix.length - andPosition - 1];
 		System.arraycopy(infix, andPosition + 1, rightSideArray, 0, rightSideArray.length);
 		for(int i = 0; i < rightSideArray.length; i++){
 			rightString += rightSideArray[i] + " ";
 		}
 		rightSideArray = infix2postfix(rightString.trim());
 		System.arraycopy(rightSideArray, 0, postfix, andPosition, rightSideArray.length);
 		postfix[postfix.length-1] = "&";
 		return postfix;
 	}
 		
     	return postfix;
     }//infix2postfix
 
     
     /***************************************************************************
      * Find the classes in the "java.lang" package with given names.
      * @param className  the array of class name (e.g., {"Integer", "String"})
      * @return  the array of Java classes for the corresponding names
      */
     private static Class [] findClass (String [] className)
     {
         Class [] classArray = new Class [className.length];
 
         for (int i = 0; i < className.length; i++) {
             try {
                 classArray [i] = Class.forName ("java.lang." + className [i]);
             } catch (ClassNotFoundException ex) {
                 out.println ("findClass: " + ex);
             } // try
         } // for
 
         return classArray;
     } // findClass
 
     /***************************************************************************
      * Extract the corresponding domains from the group.
      * @param group   where to extract from
      * @param colPos  the column positions to extract
      * @return  the extracted domains
      */
     private static Class [] extractDom (Class [] group, int [] colPos)
     {
         Class [] dom = new Class [colPos.length];
 
         for (int j = 0; j < colPos.length; j++) {
             dom [j] = group [colPos [j]];
         } // for
 
         return dom;
     } // extractDom
 
     /***************************************************************************
      * Extract the corresponding attribute values from the group.
      * @param group   where to extract from
      * @param colPos  the column positions to extract
      * @return  the extracted attribute values
      * @author Jeffrey Swindle
      */
     private static Comparable [] extractTup (Comparable [] group, int [] colPos)
     {
         Comparable [] tup = new Comparable [colPos.length];
 
         for (int i = 0; i < colPos.length; i++) {
             tup [i] = group [colPos [i]];
 	} // for
 
         return tup;
     } // extractTup
 
 } // Table class
 
