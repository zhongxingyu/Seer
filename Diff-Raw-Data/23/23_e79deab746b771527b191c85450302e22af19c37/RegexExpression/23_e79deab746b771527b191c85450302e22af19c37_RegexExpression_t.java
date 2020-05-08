 package br.com.igorribeirolima.fx.regex;
 
 import java.util.regex.Pattern;
 
 public class RegexExpression {
   
   public static final String OPERATORS = RegexOperator.regexOperators();
  public static final String CARACTER = "[a-zA-Z0-9_ãáàâêéíõóúüç :]";
   public static final String WORD = "([\"]("+CARACTER+"+)[\"])";
   public static final String DIGIT = "("+RegexNumber.DIGIT+"|"+WORD+")";
   public static final String SIMPLE_EXPRESSION = "("+DIGIT+ ")(("+ OPERATORS +")("+DIGIT+"))"; //suporta apenas a operacao entre dois numeros
   public static final String GENERIC_EXPRESSION = "("+DIGIT+ ")(("+ OPERATORS +")("+DIGIT+"))*";
   
   public static boolean isWord(String word) {
     return Pattern.matches(WORD, word);
   }
   
   public static boolean isNumber(String number) {
     return Pattern.matches(RegexNumber.DIGIT, number);
   }
   
   public static boolean isBrazilianNumber(String number) {
     return Pattern.matches(RegexNumber.BRAZILIAN_NUMBER, number);
   }
   
   public static Object convertDigitToObject(String digito) {
     if (isNumber(digito)) return new Double(digito);
     else if (isWord(digito)) return new String(digito);
     else return null;
   }
   
 }
