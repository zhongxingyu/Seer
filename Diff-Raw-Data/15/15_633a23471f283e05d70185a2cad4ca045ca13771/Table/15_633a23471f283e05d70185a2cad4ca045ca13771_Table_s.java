 import java.util.*;
 
 public class Table {
 	String name;
 	RowList table;
 	
 	public Table(String command){
 		// pull table name, trim whitespace before opening parenthesis
 		name = command.substring(0, command.indexOf('('));
 		command = command.substring(name.length(), command.length()).trim();
 		ArrayList<String> createList = parseCreate(command);
 		table = new RowList();
 		createFields(createList);
 	}
 	
 	public ArrayList<String> parseCreate(String command){
 		ArrayList<String> parsedStatement = new ArrayList<String>();
 		String cmd = command;
 		
 		// remove semicolon & trim white space
 		cmd = cmd.replace(";", "").trim();
 		
		// check for opening & closing parentheses
 		for(int i = 0; i < cmd.length(); i++){
			int paren = 0;
		
 		}
		if(!(cmd.charAt(0) == '(' && cmd.charAt(cmd.length()-1) == ')')){
			System.out.println("Syntax error in Create Table command");
 			return null;
 		}
 		
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
 		
 		for(int i = 0; i < list.size(); i++){
 			String error = "Error in Create Table Syntax:  Field " + (i+1);
 			int places = 0;
 			String string = list.get(i);
 			StringTokenizer tokens = new StringTokenizer(list.get(i));
 			name = tokens.nextToken();
 			type = string.substring(name.length() + 1).trim().toUpperCase();
 			
 			if(type.contains("CHAR")){
 				if(!(type.contains("(") && type.contains(")"))){
 					System.out.println(error);
 					return;
 				}
 				else{
 					if(type.substring(0, type.indexOf('(')).trim().contentEquals("CHAR") ||
 					type.substring(0, type.indexOf('(')).trim().contentEquals("CHARACTER")){
 					
 						places = Integer.parseInt(type.substring(
 							             type.indexOf('(')+1,type.indexOf(')')).trim());
 						Header header = new Header("CHARACTER", name, places);
 						dlist.add(header);
 					}
 					else{
 						System.out.println(error);
 						return;
 					}
 				}
 			}
 			else if(type.contains("INT")){
 				if(type.contains("(") && type.contains(")")){
 					if(type.substring(0, type.indexOf('(')).trim().contentEquals("INT") ||
 						type.substring(0, type.indexOf('(')).trim().contentEquals("INTEGER")){
 						places = Integer.parseInt(type.substring(
 								type.indexOf('(')+1, type.indexOf(')')).trim());
 						Header header = new Header("INTEGER", name, places);
 						dlist.add(header);
 					}
 					else{
 						System.out.println(error);
 						return;
 					}
 				}
 				else if(type.contentEquals("INT") || 
 					type.contentEquals("INTEGER")){
 					Header header = new Header("INTEGER", name, places);
 					dlist.add(header);
 				}
 				else{
 					System.out.println(error);
 					return;
 				}
 			}
 			else if(type.contains("NUM")){
 				Header header;
 				if(type.contains("(") && type.contains(")")){
 					if(type.substring(0, type.indexOf('(')).trim().contentEquals("NUM") ||
 							type.substring(0, type.indexOf('(')).trim().contentEquals("NUMBER")){
 						if(type.substring(type.indexOf('(')+1, type.indexOf(')')).contains(",")){
 							places = Integer.parseInt(type.substring(
 									type.indexOf('(')+1, type.indexOf(',')).trim());
 							int dec = Integer.parseInt(type.substring(
 									type.indexOf(',')+1, type.indexOf(')')).trim());
 							header = new Header("NUMBER", name, places, dec);
 						}
 						else{
 							places = Integer.parseInt(type.substring(type.indexOf('('),
 																	 type.indexOf(')')).trim());
 							header = new Header("NUMBER", name, places);
 						}
 						dlist.add(header);
 					}
 					else{
 						System.out.println(error);
 						return;
 					}
 				}
 				else if(type.contentEquals("NUM") || type.contentEquals("NUMBER")){
 					header = new Header("NUMBER", name, places);
 				}
 				else{
 					System.out.println(error);
 					return;
 				}
 			}
 			else if(type.contentEquals("DATE")){
 				Header header = new Header("DATE", name, 0);
 				dlist.add(header);
 			}
 			else{
 				System.out.println(error);
 				return;
 			}
 			
 		} //end for loop (all fields added to header row)
 		
 		table.add(dlist);
 	}
 	
 	public void insert(String command){
 		
 	}
 	
 	public void print(){
 		System.out.println(name + ": Table");
 		for(int i = 0; i < table.getRow(0).getSize(); i++)
 			System.out.print(table.getRow(0).getData(i) + "\t");
 		System.out.println();
 		System.out.println("----------------------------------------------");
 		if(table.getSize() > 0){
 			for(int i = 1; i < table.getSize(); i++){
 				for(int j = 0; j < table.getRow(i).getSize(); j++){
 					System.out.print(table.getRow(i).getData(j) + "\t");
 				}
 				System.out.println();
 			
 			}
 		}
 	}
 	
 	
 	
 	
 	public void deleteallrowsForthetable(){
 		
 		if(table.getSize() == 1){
 			//do nothing
 			System.out.println("no rows to delete");
 		} else {
 			for(int i = table.getSize(); i > 1; i--){
 				
 				table.deleteRow(i);
 				//System.out.print(table.getRow(0).getData(i) + "\t");
 				
 				
 			}
 			System.out.println("deleted all rows from table");
 		}
 		
 		
 	}
 	
 public void deleterowswhere(String conditionField, String fieldValue){
 		String temprowfield;
 		
 		//for(int j = 0; j < )
 	//	table.getRow(0).getData(index)
 		
 		
 		if(table.getSize() == 1){
 			//do nothing
 		} else {
 			for(int i = table.getSize(); i > 1; i--){
 				
 				table.deleteRow(i);
 				//System.out.print(table.getRow(0).getData(i) + "\t");
 			}
 		}
 		
 		
 	}
 }
