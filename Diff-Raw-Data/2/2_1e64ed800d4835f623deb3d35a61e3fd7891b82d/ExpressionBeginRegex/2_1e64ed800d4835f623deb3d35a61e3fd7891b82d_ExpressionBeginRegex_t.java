 package model.regex;
 import java.util.ArrayList;
 
 //import javax.swing.text.html.parser.Parser;
 
 import model.*;
 
 
 public class ExpressionBeginRegex extends Regex {
 		public ExpressionBeginRegex(ParserInput input) {
			super("\\(([^ ^)]+)",1,input);
 		}
 
 		Expression parse(String commandName) {			
 			ArrayList<Expression> list = new ArrayList<Expression>();
 			while (inputstream.notAtEndOfString()) {
 				list.add(new Parser(inputstream).parseExpression());
 				inputstream.skipWhiteSpace();
 				if (inputstream.currentCharacter() == ')') {
 					inputstream.move(1);
 					return new Expression(commandName, list);
 				}
 			}
 
 			throw new ParserException("Expected close paren, instead found "
 					+ inputstream.getMyInput());
 
 		}
 	}
