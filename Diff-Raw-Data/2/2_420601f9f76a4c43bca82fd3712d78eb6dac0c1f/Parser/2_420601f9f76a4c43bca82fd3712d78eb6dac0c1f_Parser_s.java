 package com.prealpha.dcputil.compiler.parser;
 
 import com.prealpha.dcputil.compiler.info.Operator;
 import com.prealpha.dcputil.compiler.info.Operator.OperatorPack;
 import com.prealpha.dcputil.compiler.info.Value;
 import com.prealpha.dcputil.compiler.info.Value.ValuePack;
 import com.prealpha.dcputil.compiler.lexer.Expression;
 import com.prealpha.dcputil.compiler.lexer.Token;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: Ty
  * Date: 7/17/12
  * Time: 8:56 AM
  */
 public class Parser {
     private int counter = 0;
     private Map<String,Integer> labelToLine = new HashMap<String,Integer>();
     private Map<ValuePack,String> packToLabel = new HashMap<ValuePack, String>();
 
     public List<PackGroup> parse(List<Expression> expressions) throws ParserException {
         this.counter=0;
         this.labelToLine.clear();
        this.packToLable.clear();
         List<PackGroup> toReturn = new ArrayList<PackGroup>(expressions.size());
         for(Expression expression:expressions){
             PackGroup pg = parseExpression(expression);
             if(pg!=null){
                 toReturn.add(pg);
             }
         }
         this.fillLabels();
         return toReturn;
     }
 
     private void fillLabels() throws ParserException {
         for(ValuePack vp: packToLabel.keySet()){
             if (labelToLine.containsKey(packToLabel.get(vp))) {
                 vp.setData((char)(int)labelToLine.get(packToLabel.get(vp)));
             } else {
                 throw new ParserException("Can not find label for: \""+ packToLabel.get(vp)+"\".  on line "+vp.lineNum+
                         ".\nLabel does not exist or you are trying to access items like [PC] or IA",vp.lineNum);
             }
         }
     }
 
 
 
     private PackGroup parseExpression(Expression expression) throws ParserException {
         int start = 0;
         Token[] tokens = expression.tokens;
         Token first = expression.tokens[0];
         int line = first.lineNum;
 
         OperatorPack operator = null;
         while(operator==null && start<tokens.length){
             operator = getOperator(tokens[start]);
             start++;
         }
         if(operator == null){
             return null;
         }
         operator.setLineNum(line);
         if(!operator.is("DAT")){
             counter++;
         }
 
         List<ValuePack> valuePacks = new ArrayList<ValuePack>();
         for(int i=start, k=1; i<tokens.length;i++,k++){
             if(operator.is("DAT")){
                 counter++;
                 ValuePack vp = new ValuePack((char)0xffff,0,"data-literal").withData(parseSingular(tokens[i].orig,line));
                 vp.setLineNum(line);
                 valuePacks.add(vp);
             }
             else{
                 ValuePack vp = getValue(tokens[i],k);
                 vp.setLineNum(line);
                 valuePacks.add(vp);
             }
         }
 
         return new PackGroup(operator,valuePacks);
     }
 
     private static final Pattern labelDefinition = Pattern.compile("^:(([a-z]|[A-Z]|_)\\w*)$");
 
     /**
      * Returns an OperatorPack containing the data about the Operator.
      * If it could not be found (or is actually a label definition),
      * returns null.
      * @param token
      * @return The Operator Pack or null.
      * @throws ParserException If it is unable to be parsed
      */
     private OperatorPack getOperator(Token token) throws ParserException {
         String orig = token.orig.trim();
 
         Matcher labelDefinitionM = labelDefinition.matcher(orig);
         if(labelDefinitionM.matches()){
             register(labelDefinitionM.group(1),counter);
             return null;
         }
 
         OperatorPack op = Operator.operators.get(orig);
         if(op != null){
             return op.clone();
         }
 
         throw new ParserException("Unable to parse operator \""+orig+"\".",token.lineNum);
 
     }
 
 
 
 
     private static final Pattern register                = Pattern.compile("^(A|B|C|X|Y|Z|I|J|SP|PC|EX)$");
     private static final Pattern pointerRegister         = Pattern.compile("^\\[(A|B|C|X|Y|Z|I|J|SP)\\]$");
     private static final Pattern pointerRegisterPlusNext = Pattern.compile("^\\[(A|B|C|X|Y|Z|I|J|SP)\\+((0x\\w+)|(\\d+)|(0b(1|0)+))\\]$");
     private static final Pattern stackOperations         = Pattern.compile("^((PUSH)|(POP)|(PEEK))$");
     private static final Pattern pointerNextPlusRegister = Pattern.compile("^\\[((0x\\w+)|(\\d+)|(0b(1|0)+))\\+(A|B|C|X|Y|Z|I|J|SP)\\]$");
     private static final Pattern literal                 = Pattern.compile("^((0x\\w+)|(\\d+)|(0b(1|0)+))$");
     private static final Pattern pointerNext             = Pattern.compile("^\\[((0x\\w+)|(\\d+)|(0b(1|0)+))\\]$");
     private static final Pattern labelRef                = Pattern.compile("^(([a-z]|[A-Z]|_)\\w*)$");
     private static final Pattern pointerLabelRef         = Pattern.compile("^\\[(([a-z]|[A-Z]|_)\\w*)\\]$");
 
     private ValuePack getValue(Token token, int position) throws ParserException {
         String original = token.orig.trim().replace(" ","").replace("\t","");
         int line  = token.lineNum;
 
         // Register
         // A
         Matcher registerM = register.matcher(original);
         if(registerM.matches()){
             return Value.values.get(registerM.group(1)).clone();
         }
 
         // Register Pointer
         // [A]
         Matcher pointerRegisterM = pointerRegister.matcher(original);
         if(pointerRegisterM.matches()){
             return Value.values.get("["+pointerRegisterM.group(1)+"]").clone();
         }
 
         // Stack Operations
         Matcher stackOperationsM = stackOperations.matcher(original);
         if(stackOperationsM.matches()){
             return Value.values.get(stackOperationsM.group(1)).clone();
         }
 
         // Register Pointer Plus Next
         // [A+5]
         Matcher pointerRegisterPlusM = pointerRegisterPlusNext.matcher(original);
         if(pointerRegisterPlusM.matches()){
             String reg = pointerRegisterPlusM.group(1);
             char next  = parseSingular(pointerRegisterPlusM.group(2),line);
             counter++;
             return Value.values.get("["+reg+"+next]").withData(next);
         }
         // Register Next Plus Pointer
         // [5+A]
         Matcher pointerNextPlusRegisterM = pointerNextPlusRegister.matcher(original);
         if(pointerNextPlusRegisterM.matches()){
             char next  = parseSingular(pointerNextPlusRegisterM.group(1),line);
             String reg = pointerNextPlusRegisterM.group(6);
             counter++;
             return Value.values.get("["+reg+"+next]").withData(next);
         }
 
         // Next
         // 250
         Matcher nextM = literal.matcher(original);
         if(nextM.matches()){
             char next = parseSingular(nextM.group(1),line);
             if ((next == 0xffff || next <= 30) && position == 2){
                 return Value.values.get(""+(int)next).clone();
             }
             else{
                 counter++;
                 return Value.values.get("next").withData(next);
             }
         }
 
         // Pointer Next
         // [250]
         Matcher pointerNextM = pointerNext.matcher(original);
         if(pointerNextM.matches()){
             char next = parseSingular(pointerNextM.group(1),line);
             counter++;
             return Value.values.get("[next]").withData(next);
         }
 
         // Label Reference
         // some_label
         Matcher labelRefM = labelRef.matcher(original);
         if(labelRefM.matches()){
             counter++;
             ValuePack vp = Value.values.get("next").clone();
             register(vp,labelRefM.group(1));
             return vp;
         }
 
         // Label Pointer Reference
         // [some_label]
         Matcher pointerLabelRefM = pointerLabelRef.matcher(original);
         if(pointerLabelRefM.matches()){
             counter++;
             ValuePack vp = Value.values.get("[next]").clone();
             register(vp,pointerLabelRefM.group(1));
             return vp;
         }
 
         throw new ParserException("Unable to parse token: \""+token.orig+"\" on line: "+line,line);
     }
 
     private void register(ValuePack vp, String label){
         packToLabel.put(vp, label);
     }
     private void register(String label, int line){
         labelToLine.put(label,line);
     }
 
     private Character getInner(String input,int line) throws ParserException {
         input = input.trim();
         if(isNumber(input)){
             return parseSingular(input,line);
         }
         else{
             // We assume that this is a label
             return null;
         }
     }
     static final Pattern numberPattern = Pattern.compile("^((0x\\w+)|(\\d+)|(0b(1|0)+))$");
     private boolean isNumber(String input){
         return numberPattern.matcher(input).matches();
     }
     private char parseSingular(String input,int lineNum) throws ParserException {
         input = input.trim();
         int value = 0;
         int radix ;
 
         if(input.toUpperCase().startsWith("0X")){
             radix = 16;
             input = input.substring(2);
         }
         else if(input.toUpperCase().startsWith("B")){
             radix = 2;
             input = input.substring(1);
         }
         else{
             radix = 10;
         }
         try{
             value = Integer.parseInt(input,radix);
         }catch (NumberFormatException nfe){
             throw new ParserException("Number can not be parsed: \""+input+"\" on line: "+lineNum,lineNum);
         }
 
         if(value<=Character.MAX_VALUE && value>=Character.MIN_VALUE){
             return (char) value;
         }
         else{
             throw new ParserException("Number is either above or below the limit.",lineNum);
         }
     }
 }
