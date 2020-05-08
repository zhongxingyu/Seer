 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Compiler {
 	private final static Pattern relation_pattern = Pattern.compile("(.+)(>|<|>=|<=|==|!=)+(.+)");
 	private final static String operators = "+-*/()";
 	private final static Pattern expression_pattern = Pattern.compile("([a-zA-Z0-9" + operators + "]+)");
 
 	Scanner scanner;
 	int[] memory;
 	ArrayList<Integer> constants;
 	ArrayList<Integer> line_number_list;
 	HashMap<Integer, Integer> line_numbers;
 	HashMap<String, Integer> variables;
 	int last_line_number, pointer, data_pointer;
 
 	public Compiler(File file) throws FileNotFoundException {
 		scanner = new Scanner(file);
 		memory = new int[100];
 		constants = new ArrayList<Integer>();
 		line_number_list = new ArrayList<Integer>();
 		line_numbers = new HashMap<Integer, Integer>();
 		variables = new HashMap<String, Integer>();
 		last_line_number = 0;
 		pointer = 0;
 		data_pointer = 99;
 	}
 
 	public int getLineNumber() {
 		return last_line_number;
 	}
 
 	public int[] compile() throws OutOfMemoryException, IllegalArgumentException, InvalidVariableException, SyntaxException, GotoException, LineNumberException, UndefinedVariableException {
 		while(scanner.hasNextLine()) {
 			if(pointer >= data_pointer)
 				throw new OutOfMemoryException();
 
 			String[] command = scanner.nextLine().split(" "); //Everything is separated by spaces
 
 			//Double check line numbers
 			int line_number = Integer.parseInt(command[0]);
 			if(line_number <= last_line_number)
 				throw new LineNumberException();
 
 			//Add it to the hashmap and line number array
 			if(!line_number_list.contains(line_number))
 				line_number_list.add(line_number);
 			line_numbers.put(line_number, pointer);
 			last_line_number = line_number;
 
 			//Ignore comment lines
 			if(command[1].equalsIgnoreCase("rem"))
 				continue;
 			//Make a new variable and remember it
 			else if(command[1].equalsIgnoreCase("input")) {
 				if(!Character.isLetter(command[2].charAt(0)))
 					throw new InvalidVariableException();
 
 				variables.put(command[2], data_pointer);
 				memory[pointer] = 1000 + data_pointer;
 				data_pointer--;
 			}
 			//Simply print variable
 			else if(command[1].equalsIgnoreCase("print")) {
 				if(!variables.containsKey(command[2]))
 					throw new UndefinedVariableException();
 
 				memory[pointer] = 1100 + variables.get(command[2]);
 			}
 			//If a variable doesn't exist, create it then parse the expression
 			else if(command[1].equalsIgnoreCase("let")) {
 				String[] params = command[2].split("=", 1);
 
 				if(params.length < 2)
 					throw new SyntaxException();
 
 				if(!Character.isLetter(params[0].charAt(0)))
 					throw new InvalidVariableException();
 
 				if(!variables.containsKey(params[0])) {
 					variables.put(params[0], data_pointer);
 					data_pointer--;
 				}
 
 				parseExpression(params[1], variables.get(params[0]));
 			}
 			//Put a new goto
 			else if(command[1].equalsIgnoreCase("goto")) {
 				int goto_line = Integer.parseInt(command[2]);
 				if(!line_number_list.contains(goto_line))
 					line_number_list.add(goto_line);
 
				memory[pointer] = 14000 + line_number_list.indexOf(goto_line);
 			}
 			//Yay for if's
 			else if(command[1].equalsIgnoreCase("if")) {
 				if(!command[3].equalsIgnoreCase("goto"))
 					throw new IllegalArgumentException();
 
 				parseRelation(command[2], Integer.parseInt(command[4])); //Call parse relation
 			}
 			//Put a halt
 			else if(command[1].equalsIgnoreCase("end"))
 				memory[pointer] = 4300;
 
 			pointer++;
 		}
 
 		//Make sure we still have room for constants
 		if(pointer + constants.size() > data_pointer) {
 			//You dun goofed
 		}
 
 		//Load the constants on the end of the program
 		for(int i = 0; i < constants.size(); i++)
 			memory[pointer + i] = constants.get(i);
 
 
 		for(int i = 0; i < 100; i++) {
			int opcode = memory[i] % 100;
			switch(opcode) {
 				//Constants
 				case 130:
 				case 131:
 				case 132:
 				case 133:
 					//Take 1 off of the opcode then add the appropriate pointer to the constant
 					memory[i] = opcode - 100 + pointer + memory[i] / 100;
 					break;
 				//Line numbers
 				case 140:
 				case 141:
 				case 142:
					Integer line_number_pointer = line_numbers.get(line_number_list.get(memory[i] / 100));
 					if(line_number_pointer == null)
 						throw new GotoException();
 
 					memory[i] = opcode  - 100 + line_number_pointer;
 					break;
 			}
 		}
 		
 		return memory;
 	}
 
 	private void parseRelation(String relation, int goto_symbol) throws SyntaxException {
 		//Check relations based on regexes
 		Matcher matcher = relation_pattern.matcher(relation);
 		if(!matcher.matches())
 			throw new SyntaxException();
 
 		parseExpression(matcher.group(1), data_pointer);
 		data_pointer--;
 		parseExpression(matcher.group(3), data_pointer);
 
 		memory[pointer] = 2000 + data_pointer;
 		pointer++;
 
 		if(matcher.group(2).charAt(0) == '>') {
 			memory[pointer] = 3100 + data_pointer + 1; //First number
 			pointer++;
 			memory[pointer] = 14200 + goto_symbol; //Branch negative to "goto"
 			if(matcher.group(2).charAt(1) == '=') {
 				pointer++;
 				memory[pointer] = 4100 + goto_symbol; //Also branch zero if equal to
 			}
 		}
 		else if(matcher.group(2).charAt(0) == '<') {
 			memory[pointer - 1] = 2000 + data_pointer + 1;
 			memory[pointer] = 3100 + data_pointer;
 			pointer++;
 			memory[pointer] = 14200 + goto_symbol;
 			if(matcher.group(2).charAt(1) == '=') {
 				pointer++;
 				memory[pointer] = 14100 + goto_symbol;
 			}
 		}
 		else if(matcher.group(2).equals("==")) {
 			memory[pointer] = 3100 + data_pointer + 1;
 			pointer++;
 			memory[pointer] = 14100 + goto_symbol;
 		}
 		else if(matcher.group(2).equals("!=")) {
 			if(!constants.contains(-1))
 				constants.add(-1);
 
 			memory[pointer] = 3100 + data_pointer + 1;
 			pointer++;
 			memory[pointer] = 14200 + goto_symbol;
 			pointer++;
 			memory[pointer] = 13300 + constants.indexOf(-1);
 			pointer++;
 			memory[pointer] = 14200 + goto_symbol;
 		}
 
 		pointer++;
 		data_pointer--;
 	}
 
 	private void parseExpression(String expression, int value_pointer) throws SyntaxException, NumberFormatException {
 		//Check expressions based on regexes
 		Matcher matcher = expression_pattern.matcher(expression);
 		if(!matcher.matches())
 			throw new SyntaxException();
 
 		ArrayList<String> postfix = convertToPostfix(expression);
 
 		int temp_data_pointer = data_pointer;
 		for(int i = 0; i < postfix.size(); i++) {
 			int operator = operators.indexOf(postfix.get(i).charAt(0));
 			if(operator != -1) {
 				if(temp_data_pointer < pointer) {
 					//You dun goofed!
 				}
 
 				int operand;
 				if(Character.isLetter(postfix.get(i - 1).charAt(0))) {
 					if(!variables.containsKey(postfix.get(i - 1))) {
 						variables.put(postfix.get(i - 1), data_pointer);
 						data_pointer--;
 					}
 
 					operand = variables.get(postfix.get(i - 1));
 				}
 				else {
 					int number = Integer.parseInt(postfix.get(i - 1));
 
 					if(!constants.contains(number))
 						constants.add(number);
 
 					operand = constants.indexOf(number);
 				}
 
 				int load;
 				if(operators.indexOf(postfix.get(i - 1).charAt(0)) != -1) {
 					load = temp_data_pointer;
 					temp_data_pointer++;
 				}
 				else if(Character.isLetter(postfix.get(i - 2).charAt(0))) {
 					if(!variables.containsKey(postfix.get(i - 2))) {
 						variables.put(postfix.get(i - 2), data_pointer);
 						data_pointer--;
 					}
 
 					load = variables.get(postfix.get(i - 2));
 				}
 				else {
 					int number = Integer.parseInt(postfix.get(i - 2));
 
 					if(!constants.contains(number))
 						constants.add(number);
 
 					load = constants.indexOf(number);
 				}
 
 				memory[pointer] = 2000 + load;
 				pointer++;
 				switch(operator) {
 					case 0:
 						memory[pointer] = 3000 + operand;
 						break;
 					case 1:
 						memory[pointer] = 3100 + operand;
 						break;
 					case 2:
 						memory[pointer] = 3200 + operand;
 						break;
 					case 3:
 						memory[pointer] = 3300 + operand;
 						break;
 					default:
 						//You dun goofed!
 				}
 				pointer++;
 				memory[pointer] = 2100 + temp_data_pointer;
 				pointer++;
 				temp_data_pointer--;
 			}
 		}
 	}
 
 	private ArrayList<String> convertToPostfix(String infix) {
 		ArrayList<String> postfix = new ArrayList<String>();
 		Stack<Character> processStack = new Stack<Character>();
 		char[] chars = infix.toCharArray();
 		for(int i = 0; i < chars.length; i++) {
 			String term = new String();
 			while(operators.indexOf(chars[i]) == -1 || i == 0 || (i > 0 && operators.indexOf(chars[i - 1]) != -1 && operators.indexOf(chars[i]) == 1)) {
 				term += chars[i];
 				i++;
 			}
 			postfix.add(term);
 			if(processStack.empty()) {
 				processStack.push(chars[i]);
 			}
 			else {
 				while(operators.indexOf(chars[i]) <= operators.indexOf(processStack.peek())) {
 					postfix.add(Character.toString(processStack.pop()));
 				}
 				processStack.push(chars[i]);
 			}
 		}
 		return postfix;
 	}
 }
