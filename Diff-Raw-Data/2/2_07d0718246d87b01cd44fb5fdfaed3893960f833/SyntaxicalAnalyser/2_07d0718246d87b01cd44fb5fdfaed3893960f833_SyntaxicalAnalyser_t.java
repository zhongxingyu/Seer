 package compiler.parser;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import compiler.CompilerException;
 
 /**
  * Réalise l'analyse syntaxique du code en sortie de SemanticalAnalyser
  * 
  * Transforme les suites de symboles du langage en suite de noms de combinateurs
  * 
  * @author qchen
  *
  */
 public class SyntaxicalAnalyser {
 	ArrayList<Instruction> instructions;
 	ArrayList<Instruction> result;
 	
 	int currentInstructionIndex;
 	Instruction currentInstruction;
 	
 	int currentSymbolIndex;
 	String currentSymbol;
 	
 	private static ArrayList<String> operators;
 	
 	static {
 		operators = new ArrayList<String>();
 
 		String[] ops = {"+", "-", "*", "/", "&&", "||",
 				"<", ">", "<=", ">=", "=", "!="};
 
         Collections.addAll(operators, ops);
 	}
 	
 	public SyntaxicalAnalyser(ArrayList<Instruction> instructions) throws CompilerException {
 		this.instructions = instructions;
 		currentInstructionIndex = -1;
 		result = new ArrayList<Instruction>();
 		
 		while(nextInstruction()) {
 			parseExpression();
 		}
 	}
 	
 	public ArrayList<Instruction> getInstructions() {
 		return result;
 	}
 	
 	private boolean isAtEndOfInstructions() {
 		return currentInstructionIndex >= instructions.size();
 	}
 	
 	private boolean nextInstruction() {
 		if(currentInstruction != null && !currentInstruction.getInstruction().isEmpty()) {
 			result.add(currentInstruction);
 		}
 		
 		currentInstructionIndex++;
 		
 		if(isAtEndOfInstructions()) {
 			return false;
 		}
 		
 		currentInstruction = instructions.get(currentInstructionIndex);
 		
 		currentSymbolIndex = -1;
 		
 		return nextSymbol() || nextInstruction();
 	}
 	
 	private boolean isAtEndOfSymbols() {
 		return currentSymbolIndex >= currentInstruction.getInstruction().size();
 	}
 	
 	private void registerSymbol() {
 		currentSymbol = currentInstruction.getInstruction().get(currentSymbolIndex);
 	}
 	
 	private void setCurrentSymbolIndex(int index) {
 		currentSymbolIndex = index;
 		registerSymbol();
 	}
 	
 	private boolean nextSymbol() {
 		currentSymbolIndex++;
 		
 		if(isAtEndOfSymbols()) {
 			return false;
 		}
 		
 		registerSymbol();
 		
 		return true;
 	}
 	
 	private boolean previousSymbol() {
 		if(currentSymbolIndex <= 0) {
 			currentSymbolIndex = 0;
 			return false;
 		}
 		
 		currentSymbolIndex--;
 		
 		registerSymbol();
 		
 		return true;
 	}
 	
 	private void parseExpression() throws CompilerException {
 		boolean isDefinition = false;
 		
 		do {
 			if(currentSymbol.equals(":=")) {
 				isDefinition = true;
 			}
 		} while(!isDefinition && nextSymbol());
 		
 		ArrayList<String> expr;
 		
 		if(isDefinition) {
 			expr = parseDefinition();
 		}
 		else {
 			setCurrentSymbolIndex(0);
 			expr = parseEvaluable();
 		}
 		
 		if(currentSymbol.equals(";;")) {
 			expr.add(0, "debug");
 			nextSymbol();
 		}
 		
 		if(!isAtEndOfSymbols() && !currentSymbol.equals("#")) {
 			error("Symbole inattendu à la fin de l'expression: " + currentSymbol);
 		}
 		
 		currentInstruction.setInstruction(expr);
 	}
 	
 	private ArrayList<String> parseDefinition() throws CompilerException {
 		Stack<String> definitionHead = new Stack<String>();
 		
 		ArrayList<String> result = new ArrayList<String>();
 		result.add(":=");
 		
 		int defSymbolIndex = currentSymbolIndex;
 		
 		while(previousSymbol()) {
 			if(!isName()) {
 				error("Les symboles à gauche de := doivent être des noms de combinateur, et pas " + currentSymbol);
 			}
 			
 			definitionHead.push(currentSymbol);
 		}
 		
 		if(definitionHead.isEmpty()) {
 			error("Il faut au moins le nom du combinateur à définir à gauche de :=");
 		}
 		
 		String funcName = definitionHead.pop();
 		result.add("$" + funcName);
 
 		
 		setCurrentSymbolIndex(defSymbolIndex);
 		
 		if(!nextSymbol()) {
 			error("Une expression doit se trouver à droite de :=");
 		}
 		
 		if(!definitionHead.isEmpty()) {
 			ArrayList<String> varNames = new ArrayList<String>();
 			
 			while(!definitionHead.isEmpty()) {
 				String var = definitionHead.pop();
                 result.add("I");
                 result.add("(");
 				result.add("lambda");
 				result.add("$" + var);
 				varNames.add(var);
 			}
 			
 			ArrayList<String> expr = new ArrayList<String>();
 
 			for(String symbol: parseEvaluable()) {
 				if(symbol.equals(funcName)) {
 					expr.add("@" + symbol);
 				}
 				else {
 					boolean wasVarName = false;
 
 					for(String varName: varNames) {
 						if(symbol.equals(varName)) {
 							expr.add("$" + symbol);
 							wasVarName = true;
 							break;
 						}
 					}
 
 					if(!wasVarName) {
 						expr.add(symbol);
 					}
 				}
 			}
 			
 			result.addAll(expr);
 
             for (int i = 0; i < varNames.size(); i++) {
                 result.add(")");
             }
 		}
 		else {
 			ArrayList<String> expr = new ArrayList<String>();
 			
 			for(String symbol: parseEvaluable()) {
 				if(symbol.equals(funcName)) {
 					expr.add("@" + symbol);
 				}
 				else {
 					expr.add(symbol);
 				}
 			}
 			
 			result.addAll(expr);
 		}
 		
 		return result;
 	}
 	
 	private boolean isName() {
 		Pattern p = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");
 		Matcher m = p.matcher(currentSymbol);
 		
 		if(m.find()) {
 			return true;
 		}
 		
 		try {
 			Integer.parseInt(currentSymbol);
 			return true;
 		}
 		catch(NumberFormatException e) {
 			return false;
 		}
 	}
 
     private ArrayList<String> parseEvaluable() throws CompilerException {
         return parseEvaluable(false);
     }
 
 	private ArrayList<String> parseEvaluable(boolean isOperand) throws CompilerException {
 		ArrayList<String> result = new ArrayList<String>();
 		
 		Stack<Integer> operandsPosition = new Stack<Integer>();
 
 		do {
 			operandsPosition.push(result.size());
 			
 			if(currentSymbol.equals("(")) {
 				result.addAll(parseParenthesis());
 			}
 			else if(currentSymbol.equals("[")) {
 				result.addAll(parseVector());
 			}
 			else if(isEvaluableSeparator()) {
 				break;
 			}
 			else if(currentSymbol.equals("if")) {
 				result.addAll(parseCondition());
                 break;
 			}
 			else if(currentSymbol.equals("lambda")) {
 					result.addAll(parseLambda());
                     break;
 				}
 			else if(isName() || currentSymbol.equals("!")) {
 				result.add(currentSymbol);
 			}
 			else if(isOperator()) {
                 if(isOperand) {
                     break;
                 }
 
 				operandsPosition.pop();
 				
 				if(operandsPosition.isEmpty()) {
					error("Opérateur " + currentSymbol + "n'a pas d'opérande gauche");
 				}
 				
 				parseOperator(result, operandsPosition.peek());
                 previousSymbol();
 			}
 			
 		} while(nextSymbol());
 		
 		return result;
 	}
 	
 	private boolean isEvaluableSeparator() {
 		return currentSymbol.equals("#") ||
 				currentSymbol.equals(")") ||
 				currentSymbol.equals("]") ||
 				currentSymbol.equals(",") ||
 				currentSymbol.equals("then") ||
 				currentSymbol.equals("else") ||
 				currentSymbol.charAt(0) == ';';
 	}
 	
 	private ArrayList<String> parseParenthesis() throws CompilerException {
 		if(!nextSymbol()) {
 			error("'(' à la fin de l'instruction");
 		}
 		
 		ArrayList<String> result = new ArrayList<String>();
 		result.addAll(wrapInParenthesis(parseEvaluable()));
 		
 		if(isAtEndOfSymbols() || !currentSymbol.equals(")")) {
 			error("Parenthèses non fermées");
 		}
 		
 		return result;
 	}
 
     private void parseOperator(List<String> expression, int firstOperandPos) throws CompilerException {
         expression.add(0, "(");
         expression.add(")");
         expression.add(firstOperandPos, currentSymbol);
         expression.add(firstOperandPos, "(");
 
         if(!nextSymbol()) {
             throw new CompilerException("Opérateur " + currentSymbol + " doit avoir une deuxième opérande");
         }
 
         expression.addAll(wrapInParenthesis(parseEvaluable(true)));
         expression.add(")");
     }
 
 	private ArrayList<String> parseVector() throws CompilerException {
 		ArrayList<String> result = new ArrayList<String>();
 		
 		Stack<ArrayList<String>> elements = new Stack<ArrayList<String>>();
 		
 		while(!currentSymbol.equals("]") && nextSymbol()) {
 			elements.add(wrapInParenthesis(parseEvaluable()));
 			
 			if(!currentSymbol.equals(",") && !currentSymbol.equals("]")) {
 				error("Les éléments d'un vecteur doivent être séparés par une virgule");
 			}
 
 		}
 		
 		if(!currentSymbol.equals("]")) {
 			error("Vecteur non terminé, il faut ajouter ']'");
 		}
 		
 		if(elements.size() == 1) {
 			error("Vecteur à un élément interdit.");
 		}
 		
 		while(!elements.isEmpty()) {
 			ArrayList<String> element = elements.pop();
 			
 			if(result.isEmpty()) {
 				result.addAll(element);
 			}
 			else {
 				result.addAll(0, element);
 				result.add(0, "vec");
 				result.add(0, "(");
 				result.add(")");
 			}
 		}
 		
 		return result;
 	}
 	
 	private ArrayList<String> parseLambda() throws CompilerException {
 		ArrayList<String> result = new ArrayList<String>();
 		
 		String lambdaName = "lambda";
 		
 		if(!nextSymbol()) {
 			error("'lambda' ne peut être à la fin de l'instruction");
 		}
 		
 		if(currentSymbol.charAt(0) == '+') {
 			lambdaName += currentSymbol;
 			
 			if(!nextSymbol()) {
 				error("'" + lambdaName + "' ne peut être à la fin de l'instruction");
 			}
 		}
 		
 		ArrayList<String> varNames = new ArrayList<String>();
 		
 		do {
 			if(currentSymbol.equals(",")) {
 				continue;
 			}
 			
 			if(!isName()) {
 				error("'" + currentSymbol + "' n'est pas un nom de variable valide pour une abstraction");
 			}
 			
 			varNames.add(currentSymbol);
 		} while(nextSymbol() && !currentSymbol.equals("."));
 		
 		if(!currentSymbol.equals(".")) {
 			error("Expression d'abstraction mal formée, il manque '. <expression à abstraire>");
 		}
 		
 		if(!nextSymbol()) {
 			error("Expression d'abstraction mal formée, il manque l'expression à abstraire après le point");
 		}
 
 		ArrayList<String> expr = new ArrayList<String>();
 
 		for(String symbol: parseEvaluable()) {
 			boolean wasVar = false;
 
 			for(String varName: varNames) {
 				if(symbol.equals(varName)) {
 					expr.add("$" + symbol);
 					wasVar = true;
 					break;
 				}
 			}
 
 			if(!wasVar) {
 				expr.add(symbol);
 			}
 		}
 
         // Des parenthèses au début du code sont équivalentes à pas de pérenthèses, dans le graphe.
         // c'est rpoblématique pour lambda  car l'abstraction s'effectue sur le graphe suivant lambda.
         // En rajoutant I, on garde les parenthèses.
         result.add("I");
 		result.add("(");
 
 		for(String varName: varNames) {
 			result.add(lambdaName);
 			result.add("$" + varName);
 		}
 
 		result.addAll(expr);
 
 		result.add(")");
 
 		return result;
 	}
 	
 	private ArrayList<String> parseCondition() throws CompilerException {
 		ArrayList<String> result = new ArrayList<String>();
 		
 		if(!nextSymbol()) {
 			error("Un 'if' doit être suivi d'une condition, d'une clause then, puis d'une clause else");
 		}
 		
 		ArrayList<String> condition = parseEvaluable();
 		
 		result.addAll(wrapInParenthesis(condition));
 		
 		if(!currentSymbol.equals("then")) {
 			error("La condition d'un 'if' doit être suivie d'une clause then");
 		}
 		
 		nextSymbol();
 		
 		ArrayList<String> thenClause = parseEvaluable();
 
 		result.addAll(wrapInParenthesis(thenClause));
 		
 		if(!currentSymbol.equals("else")) {
 			error("La clause then d'un if doit être suivie d'une clause else");
 		}
 		
 		nextSymbol();
 
 		ArrayList<String> elseClause = parseEvaluable();
 		
 		result.addAll(wrapInParenthesis(elseClause));
 		
 		return result;
 	}
 	
 	private void error(String msg) throws CompilerException {
 		throw new CompilerException(
 				msg,
 				currentInstruction.getLine(),
 				currentInstruction.getPosition());
 	}
 	
 	private boolean isOperator() {
 		for(String op: operators) {
 			if(op.equals(currentSymbol)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
     private ArrayList<String> wrapInParenthesis(ArrayList<String> expr) {
         if(expr.size() <= 1) {
             return expr;
         }
 
         if(expr.get(0).equals("(")) {
             int count = 1, index = 1;
 
             while(count > 0 && index < expr.size()) {
                 if(expr.get(index).equals("(")) {
                     count++;
                 }
                 else if(expr.get(index).equals(")")) {
                     count--;
                 }
 
                 index++;
             }
 
             if(index == expr.size()) {
                 return expr;
             }
         }
 
         ArrayList<String> expr2 = new ArrayList<String>();
         expr2.add("(");
         expr2.addAll(expr);
         expr2.add(")");
         return expr2;
     }
 
 }
