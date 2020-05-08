 import java.util.*;
 
 public class Table {
 	String name;
 	RowList table;
 	
 	public Table(String command){
 		// pull table name, trim whitespace before opening parenthesis
 		name = command.substring(0, command.indexOf('(')).trim().toUpperCase();
 		command = command.substring(name.length(), command.length()).trim();
 		ArrayList<String> createList;
 		if((createList = parseCreate(command)) == null){
 			this.name = null;
 			return;
 		}
 		table = new RowList();
 		createFields(createList);
 	}
 	
 	public ArrayList<String> parseCreate(String command){
 		ArrayList<String> parsedStatement = new ArrayList<String>();
 		String cmd = command;
 		
 		// remove semicolon & trim white space
 		cmd = cmd.replace(";", "").trim();
 		
 		if(cmd.charAt(cmd.length()-1) != ')'){
 			System.out.println("Error in CREATE TABLE syntax");
 			return null;
 		}
 		
 		// check for matching sets of parentheses
 		if(!checkParentheses(cmd))
 			return null;
 		
 		// remove leading parenthesis
 		cmd = cmd.substring(1, cmd.length());
 		
 		String temp;
 		int pos = 0;
 		
 		// parse data types & labels
 		while(pos < cmd.length()){
 			
 			// if comma exists, pull string before comma index
 			if(cmd.substring(pos, cmd.lastIndexOf(')')).contains(","))
 				temp = cmd.substring(pos, cmd.indexOf(','));
 			// else pull rest of string (last term)
 			else
 				temp = cmd.substring(pos, cmd.lastIndexOf(')'));
 			
 			// increase pos to index after comma
 			pos = pos + temp.length()+1;
 			
 			// if string contains unmatched parenthesis
 			if(temp.contains("(") && !temp.contains(")")){
 				temp = temp + ","; //insert skipped comma
 				
 				// if another comma exists, append substring
 				if(cmd.substring(pos, cmd.length()).contains(",")){
 					int i = pos;
 					while(cmd.charAt(i) != ',')
 						i++;
 					temp = temp + cmd.substring(pos, i);
 					pos = i + 1;
 				}
 				// else (no comma) pull rest of string (final term)
 				else{
 					temp = temp + cmd.substring(pos, cmd.lastIndexOf(')'));
 					pos = cmd.lastIndexOf(')');
 				}
 			}
 			
 			// trim temp string, set new trimmed cmd string, reset pos to 0
 			temp = temp.trim();
 			if(!(temp.contentEquals("")))
 				parsedStatement.add(temp);
 			cmd = cmd.substring(pos, cmd.length()).trim();
 			pos = 0;		
 		}	
 		
 		return parsedStatement;
 	}
 	
 	public void createFields(ArrayList<String> list){
 		String name;
 		String type;
 		DataList dlist = new DataList();
 		
 		if(list == null)
 			return;
 		
 		for(int i = 0; i < list.size(); i++){
 			String error = "Error in Create Table Syntax:  Field " + (i+1);
 			int places = 0;
 			boolean notNull = false;
 			String string = list.get(i);
 			StringTokenizer tokens = new StringTokenizer(list.get(i));
 			name = tokens.nextToken();
 			type = string.substring(name.length() + 1).trim().toUpperCase();
 			
 			//check for CHAR
 			if(type.contains("CHAR")){
 				if(type.contains("NOT NULL"))
 					notNull = true;
 				//error if no opening & closing parentheses
 				if(!(type.contains("(") && type.contains(")"))){
 					System.out.println(error);
					this.name = null;
 					return;
 				}
 				else{
 					if(type.substring(0, type.indexOf('(')).trim().contentEquals("CHAR") ||
 					type.substring(0, type.indexOf('(')).trim().contentEquals("CHARACTER")){
 					
 						//pull number of characters & create Header object
 						places = Integer.parseInt(type.substring(
 							             type.indexOf('(')+1,type.indexOf(')')).trim());
 						Header header = new Header("CHARACTER", name, places, notNull);
 						dlist.add(header);
 					}
 					//mismatched data type
 					else{
 						System.out.println(error);
 						return;
 					}
 				}
 			}
 			//check for INT
 			else if(type.contains("INT")){
 				//check for opening & closing parentheses
 				if(type.contains("(") && type.contains(")")){
 					if(type.substring(0, type.indexOf('(')).trim().contentEquals("INT") ||
 						type.substring(0, type.indexOf('(')).trim().contentEquals("INTEGER")){
 						places = Integer.parseInt(type.substring(
 								type.indexOf('(')+1, type.indexOf(')')).trim());
 						Header header = new Header("INTEGER", name, places, notNull);
 						dlist.add(header);
 					}
 					//mismatched data type
 					else{
 						System.out.println(error);
 						return;
 					}
 				}
 				//no parentheses
 				else if(type.contentEquals("INT") || 
 					type.contentEquals("INTEGER")){
 					Header header = new Header("INTEGER", name, places, notNull);
 					dlist.add(header);
 				}
 				//mismatched data type
 				else{
 					System.out.println(error);
 					return;
 				}
 			}
 			//check for NUM
 			else if(type.contains("NUM")){
 				Header header;
 				//check for opening & closing parentheses
 				if(type.contains("(") && type.contains(")")){
 					if(type.substring(0, type.indexOf('(')).trim().contentEquals("NUM") ||
 							type.substring(0, type.indexOf('(')).trim().contentEquals("NUMBER")){
 						//check for decimal digit formatting - parse integer & decimal digits
 						if(type.substring(type.indexOf('(')+1, type.indexOf(')')).contains(",")){
 							places = Integer.parseInt(type.substring(
 									type.indexOf('(')+1, type.indexOf(',')).trim());
 							int dec = Integer.parseInt(type.substring(
 									type.indexOf(',')+1, type.indexOf(')')).trim());
 							header = new Header("NUMBER", name, places, dec, notNull);
 						}
 						//no comma - parse integer digits
 						else{
 							places = Integer.parseInt(type.substring(type.indexOf('('),
 																	 type.indexOf(')')).trim());
 							header = new Header("NUMBER", name, places, notNull);
 						}
 						//add Header to DataList
 						dlist.add(header);
 					}
 					//mismatched data type
 					else{
 						System.out.println(error);
 						return;
 					}
 				}
 				//no argument for # digits
 				else if(type.contentEquals("NUM") || type.contentEquals("NUMBER")){
 					header = new Header("NUMBER", name, places, notNull);
 					dlist.add(header);
 				}
 				//mismatched data type
 				else{
 					System.out.println(error);
 					return;
 				}
 			}
 			//check for DATE
 			else if(type.contentEquals("DATE")){
 				Header header = new Header("DATE", name, 0, notNull);
 				dlist.add(header);
 			}
 			//unknown data type
 			else{
 				System.out.println(error);
 				return;
 			}
 			
 		} //end for loop (all fields added to header row)
 		
 		table.add(dlist);
 	}
 	
 	public void insert(String cmd){
 		String literalError;
 		ArrayList<String> fields = null;
 		ArrayList<String> literals = new ArrayList<String>();
 		ArrayList<Header> headers = new ArrayList<Header>();
 		Header h;
 		DataList dlist = new DataList();
 		StringTokenizer st;
 		int attributes = table.getRow(0).getSize();
 		int index;
 		
 		if(!checkParentheses(cmd))
 			return;
 		
 		//check for (field[, field]...)
 		if(cmd.substring(0, cmd.toUpperCase().indexOf("VALUES")).contains("(")
 				&& cmd.substring(0, cmd.toUpperCase().indexOf("VALUES")).contains(")")){
 			String f = cmd.substring(cmd.indexOf("(") + 1, cmd.indexOf(")"));
 			st = new StringTokenizer(f, ",");
 			fields = new ArrayList<String>();
 			while(st.hasMoreTokens())
 				fields.add(st.nextToken().trim());
 			cmd = cmd.substring(cmd.indexOf(")") + 1);
 		}
 		
 		//put literals into ArrayList
 		String l = cmd.substring(cmd.indexOf("(") + 1, cmd.lastIndexOf(")"));
 		st = new StringTokenizer(l, ",");
 		while(st.hasMoreTokens())
 			literals.add(st.nextToken().trim());
 		
 		//check number of literals
 		/*if(literals.size() != table.getRow(0).getSize()){
 			System.out.println("Error inserting tuple:  # literals != # attributes");
 			return;
 		}*/
 		
 		//if (field[, field]...) exists
 		if(fields != null){
 			//check number of fields
 			/*if(fields.size() != table.getRow(0).getSize()){
 				System.out.println("Error inserting tuple:  # fields != # attributes");
 				return;
 			}*/
 			//check number of fields against number of literals
 			if(literals.size() != fields.size()){
 				System.out.println("Syntax error:  # fields != # literals");
 				return;
 			}
 		}
 	
 		//fill dlist ArrayList with NULL
 		//fill headers ArrayList with headers
 		for(int i = 0; i < attributes; i++){
 			dlist.add(new NullType());
 			headers.add((Header)table.getRow(0).getData(i));
 		}
 			
 		
 		for(int i = 0; i < literals.size(); i++){
 			String fieldError = "Syntax error:  Field " + (i+1);
 			literalError = "Syntax error:  Literal " + (i+1);
 			
 			//if (field[, field]...) exists
 			if(fields != null){
 				//search for field name in the attributes
 				if((index = table.getRow(0).getFieldIndex(fields.get(i))) == -1){
 					System.out.println(fieldError);
 					return;
 				}
 			}
 			//(field[, field]...) not present; index literals in order
 			else
 				index = i;
 			
 			h = headers.get(index);
 			l = literals.get(i);
 			
 			//check for character type in field
 			if(h.getType().equalsIgnoreCase("character")){
 				//if starting quote but no ending quote
 				if(l.startsWith("\"") && !(l.endsWith("\""))){
 					//get next literal, add to current string
 					literals.remove(i);
 					l = l + ", " + literals.get(i);
 					literals.remove(i);
 					literals.add(i, l);
 				}
 				//if starting & ending quotes not present
 				if(!(l.startsWith("\"") && l.endsWith("\""))){
 					//return error
 					System.out.println(fieldError);
 					return;
 				}
 				
 				// remove quotations from literal
 				l = l.substring(1, l.lastIndexOf("\""));
 				
 				//check length of string against assigned places
 				if(l.length() > h.getPlaces()){
 					System.out.println("Too many characters in literal " + (i+1));
 					return;
 				}
 				dlist.remove(index);
 				dlist.add(index, new CharType(h.getPlaces(), l));
 			}
 			//check for integer type in field
 			else if(h.getType().equalsIgnoreCase("integer")){
 				//verify that literal is integer
 				if(!(isInteger(l))){
 					System.out.println(literalError);
 					return;
 				}
 				//if places != 0, make sure integer isn't too long
 				if(h.getPlaces() > 0){
 					if(!(l.length() > h.getPlaces())){
 						dlist.remove(index);
 						dlist.add(index, new IntType(h.getPlaces(),l));
 					}
 					else{
 						System.out.println("Too many digits in literal " + (i+1));
 						return;
 					}
 				}
 				else{  //else places == 0, add literal
 					dlist.remove(index);
 					dlist.add(index, new IntType(l));
 				}
 			}
 			//check for number type in field
 			else if(h.getType().equalsIgnoreCase("number")){
 				//verify that literal is double
 				if(!(isDouble(l))){
 					System.out.println(literalError);
 					return;
 				}
 				//if places != 0
 				if(h.getPlaces() > 0){
 					//check for decimal
 					if(l.contains(".")){
 						//verify length of integer part
 						if(!(l.substring(0,l.indexOf(".")).length()> (h.getPlaces() - h.getDec()))
 								&& h.getDec() > 0){
 							//verify length of decimal part
 							if(!(l.substring(l.indexOf(".")+1).length() > h.getDec())){
 								dlist.remove(index);
 								dlist.add(index, new Number(h.getPlaces(), h.getDec(), l));
 								continue;
 							}
 						}					
 					}
 					//no decimal part, check length of integer
 					else if(!(l.length() > h.getPlaces())){
 						dlist.remove(index);
 						dlist.add(index, new Number(h.getPlaces(), l));
 						continue;
 					}
 				}
 				//no length specifiers; add to row
 				else{
 					dlist.remove(index);
 					dlist.add(index, new Number(l));
 					continue;
 				}
 				
 				//error has occurred in formatting
 				System.out.println(literalError);
 				return;
 			}
 			//check for date type
 			else if(h.getType().equalsIgnoreCase("date")){
 				//check for both slashes in date
 				if(l.contains("/")){
 					if(l.substring(l.indexOf("/")).contains("/")){
 						dlist.remove(index);
 						dlist.add(index, new DateType(l));
 					}
 				}
 				//date format error
 				else{
 					System.out.println(literalError);
 					return;
 				}
 			}
 			else{
 				System.out.println("An unknown error has occurred in INSERT.  Please try again.\n");
 				return;
 			}	
 		}
 		
 		NullType n = new NullType();
 		for(int i = 0; i < dlist.getSize(); i++){
 			if(headers.get(i).notNull && (dlist.getData(i).getClass() == n.getClass())){
 				System.out.println("Insert error:  Null value inserted into NOT NULL field");
 				return;
 			}
 		}
 		
 		table.add(dlist);
 		System.out.println("Successfully inserted row into " + name);
 	}
 	
 	/* UpdateFields
 	 * Updates the fields of the entered table.
 	 */
 	public String updateFields(ArrayList<String> attrNames, DataList values, String condAttr, Object condValue, boolean conditional){
 		ArrayList<Integer> rowNum = new ArrayList<Integer>();
 		ArrayList<Integer> indices = new ArrayList<Integer>();
 		ArrayList<Object> changedValues = new ArrayList<Object>();
 		
 		for(int count = 0; count < attrNames.size(); count++){
 			int index = getHeaderIndex(attrNames.get(count));
 			if(index == -1){
 				return "Entered field '" + attrNames.get(count) + "' is not in table '" + name + "'.";
 			}else{
 				if(!conditional){
 					int num;
 					for(num = 1; num < table.getSize(); num++){
 						rowNum.add(num);
 						indices.add(index);
 						changedValues.add(values.getData(count));
 					}
 					if(num == 1){
 						return "There is no data in the table.";
 					}
 				}else{
 					int condIndex = getHeaderIndex(condAttr.trim());
 					if(condIndex == -1){
 						return "Entered field '" + condAttr + "' is not in table '" + name + "'.";
 					}else{
 						int num;
 						for(num = 1; num < table.getSize(); num++){
 							if(table.getRow(num).getData(condIndex).equals(condValue)){
 								rowNum.add(num);
 								indices.add(index);
 								changedValues.add(values.getData(count));
 							}
 						}
 						if(num == 1){
 							return "There is no data in the table.";
 						}
 					}
 				}
 			}
 		}
 		
 		//Make changes to the table values
 		for(int i = 0; i < indices.size(); i++){
 			table.getRow(rowNum.get(i)).setData(indices.get(i), changedValues.get(i));
 		}
 		
 		return "";
 	}
 	
 	public int getHeaderIndex(String fieldName){
 		int index = -1;
 		for(int i = 0; i < table.getRow(0).getSize(); i++){
 			Header attr = (Header) table.getRow(0).getData(i);
 			if(attr.getName().equalsIgnoreCase(fieldName)){
 				index = i;
 				break;
 			}
 		}
 		return index;
 	}
 	
 	public void print(){
 		int attributes = table.getRow(0).getSize();
 		int rows = table.getSize();
 		ArrayList<Header> headers = new ArrayList<Header>();
 		Header h;
 		
 		//print table name
 		System.out.println(name + ": Table");
 		
 		//print attributes
 		for(int i = 0; i < attributes; i++){
 			h = (Header)table.getRow(0).getData(i);
 			headers.add(h);
 			System.out.print(h + "   ");
 		}
 		System.out.print("\n----------------------------------------");
 		System.out.println("----------------------------------------");
 		if(rows > 0){
 			for(int i = 1; i < rows; i++){
 				for(int j = 0; j < attributes; j++){
 					String space = "   ";
 					if(headers.get(j).getPlaces() + headers.get(j).getDec() > 0){
 						while((headers.get(j).getName().length() + 3) >
 							(headers.get(j).getPlaces() + headers.get(j).getDec()
 							+ space.length()))
 							space = space + " ";
 						if(headers.get(j).getType().equals("NUMBER"))
 							space = space + " ";
 					}
 					else{
 						while(table.getRow(i).getData(j).toString().length()
 								+ space.length() < headers.get(j).getName().length() + 6)
 							space = space + " ";
 					}
 					System.out.print(table.getRow(i).getData(j) + space);
 					
 				}
 				System.out.println();
 			
 			}
 		}
 	}
 	
 	public void deleteallrowsForthetable(){
 		if(table.getSize() == 1){
 			//do nothing
 			System.out.println("no rows to delete");
 		}
 		else {
 			System.out.println("deleted all rows from table before " + (table.getSize() - 1) );
 			for(int i = (table.getSize() - 1); i > 0; i--){
 				//System.out.println(table.getRow(i).toString() + "\t");
 				table.deleteRow(i);
 			}
 			System.out.println("deleted all rows from table");
 		}	 
 	}
 	
 	public void deleterowswhere(String conditionField, String fieldValue){
 		//String temprowfield;
 		int indexofcondfield = -1;
 		//System.out.println("table row size" + table.getRow(0).getSize() + "conditionField " + conditionField + "field value" + fieldValue);
 		 
 		for(int j = 0; j < table.getRow(0).getSize(); j++ ) {
 			// System.out.println("conditionField " + conditionField + ".  " + table.getRow(0).getData(j).toString().trim().toUpperCase() + "."); 
 			if(conditionField.contains(table.getRow(0).getData(j).toString().trim().toUpperCase())){
 				System.out.println("conditionField " + conditionField + "  " + table.getRow(0).getData(j)); 
 				indexofcondfield = j;
 			}
 		}
 		System.out.println(indexofcondfield);
 		 
 		if(indexofcondfield == -1){
 			System.out.println("condition field is not valid or does not exist");
 		}
 		else {
 			for(int i = (table.getSize() - 1); i > 0; i--){
 				System.out.println("fieldvalue" + fieldValue + ". " + table.getRow(i).getData(indexofcondfield).toString() + "." + i );
 		 
 				if((fieldValue.toString().trim().toUpperCase()).equals(table.getRow(i).getData(indexofcondfield).toString().trim().toUpperCase())){
 					table.deleteRow(i);
 					System.out.println("Row " + (i+1) + "got deleted");
 				}
 			}
 		}
 	}
 
 	public boolean checkParentheses(String cmd){
 		int paren = 0;
 		for(int i = 0; i < cmd.length(); i++){
 			if(Character.valueOf(cmd.charAt(i)) == Character.valueOf('('))
 				paren += 1;
 			if(Character.valueOf(cmd.charAt(i)) == Character.valueOf(')'))
 				paren -= 1;
 		}
 		if(paren != 0){
 			System.out.println("Syntax error in command:  mismatched parentheses");
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean isInteger(String input){
 		try{
 			Integer.parseInt(input);
 			return true;
 		} catch(Exception e){
 			return false;
 		}
 	}
 	
 	public boolean isDouble(String input){
 		try{
 			Double.parseDouble(input);
 			return true;
 		} catch(Exception e){
 			return false;
 		}
 	}
 }
