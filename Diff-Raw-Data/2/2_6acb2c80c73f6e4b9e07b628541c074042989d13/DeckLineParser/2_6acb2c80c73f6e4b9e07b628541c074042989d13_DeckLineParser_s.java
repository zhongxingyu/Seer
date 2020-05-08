 package dk.itu.jesl.deck_code.processor;
 
 import java.util.regex.*;
 
 class DeckLineParser {
     interface LineProc {
         void stop();
         void label(String label);
         void makeDeck(String deckName);
         void moveTop(String left, String right);
         void moveAll(String left, String right);
         void jump(String label);
         void jumpEmpty(String deckName, String label);
         void jumpNotEmpty(String deckName, String label);
         void jumpLess(String left, String right, String label);
         void jumpGreater(String left, String right, String label);
         void jumpEqual(String left, String right, String label);
         void output(String deckName);
         void read(String deckName);
         void parseException(String message);
     }
 
     private static String labelS = "(\\w+):";
     private static String instrS = "(\\w+)\\s+(.*)";
     private static String ifEmptyS = "if\\s+empty\\s+(\\w+)\\s*,\\s*(\\w+)";
     private static String ifNotEmptyS = "if\\s+not\\s+empty\\s+(\\w+)\\s*,\\s*(\\w+)";
     private static String ifCompS = "if\\s+(\\w+)\\s+(\\w+)\\s*,\\s*(\\w+)\\s*,\\s*(\\w+)";
 
     private static Pattern topP = Pattern.compile("(stop)|" + labelS + "|" + instrS);
     private static Pattern instrP = Pattern.compile("(deck)|(movetop)|(moveall)|(jump)|(output)");
     private static Pattern paramP = Pattern.compile("(?:(\\w+)(?:\\s*,\\s*(\\w+))?)?");
     private static Pattern deckParamP = Pattern.compile("(\\w+)(\\s+input)?");
    private static Pattern jumpParamP = Pattern.compile(ifEmptyS + "|" + ifNotEmptyS + ifCompS + "|" + "|(\\w+)");
 
     static void parseLine(String line, LineProc todo) {
         if (line.length() == 0) {
             ;                   // empty line, ignore
         } else {
             Matcher topM = topP.matcher(line);
             if (!topM.matches()) {
                 todo.parseException("Unrecognized syntax: " + line);
             } else if (topM.group(1) != null) {
                 todo.stop();
             } else if (topM.group(2) != null) {
                 todo.label(topM.group(2));
             } else {
                 String instr = topM.group(3);
                 String param = topM.group(4);
                 Matcher instrM = instrP.matcher(instr);
                 if (!instrM.matches()) {
                     todo.parseException("Illegal instruction: " + instr);
                 } else if (instrM.group(1) != null) { // deck
                     Matcher deckParamM = deckParamP.matcher(param);
                     if (!deckParamM.matches()) {
                         todo.parseException("Illegal deck definition parameters: " + param);
                     } else if (deckParamM.group(2) != null) {
                         todo.read(deckParamM.group(1));
                     } else {
                         todo.makeDeck(deckParamM.group(1));
                     }
                 } else if (instrM.group(2) != null) { // movetop
                     Matcher paramM = checkParam(param, 2, todo);
                     if (paramM != null) todo.moveTop(paramM.group(1), paramM.group(2));
                 } else if (instrM.group(3) != null) { // moveall
                     Matcher paramM = checkParam(param, 2, todo);
                     if (paramM != null) todo.moveAll(paramM.group(1), paramM.group(2));
                 } else if (instrM.group(4) != null) { // jump
                     Matcher jumpParamM = jumpParamP.matcher(param);
                     if (!jumpParamM.matches()) {
                         todo.parseException("Illegal jump parameters: " + param);
                     } else if (jumpParamM.group(1) != null) { // if empty
                         todo.jumpEmpty(jumpParamM.group(1), jumpParamM.group(2));
                     } else if (jumpParamM.group(3) != null) { // if not empty
                         todo.jumpNotEmpty(jumpParamM.group(3), jumpParamM.group(4));
                     } else if (jumpParamM.group(5) != null) { // if <comparison>
                         String cond = jumpParamM.group(5);
                         String left = jumpParamM.group(6);
                         String right = jumpParamM.group(7);
                         String label = jumpParamM.group(8);
                         if ("less".equals(cond)) {
                             todo.jumpLess(left, right, label);
                         } else if ("greater".equals(cond)) {
                             todo.jumpGreater(left, right, label);
                         } else if ("equal".equals(cond)) {
                             todo.jumpEqual(left, right, label);
                         } else {
                             todo.parseException("Expected less, greater, or equal: " + cond);
                         }
                     } else {    // unconditional jump
                         todo.jump(jumpParamM.group(9));
                     }
                 } else if (instrM.group(5) != null) { // output
                     Matcher paramM = checkParam(param, 1, todo);
                     if (paramM != null) todo.output(paramM.group(1));
                 }
             }
         }
     }
 
     private static Matcher checkParam(String param, int expected, LineProc todo) {
         Matcher paramM = paramP.matcher(param);
         if (!paramM.matches()) {
             todo.parseException("Illegal parameter");
             return paramM;
         } else if (paramM.group(2) != null) {
             if (expected == 2) {
                 return paramM;
             } else {
                 todo.parseException("Unexpected second parameter: " + paramM.group(2));
                 return null;
             }
         } else if (paramM.group(1) != null) {
             if (expected == 1) {
                 return paramM;
             } else if (expected == 0) {
                 todo.parseException("Unexpected parameter: " + param);
                 return null;
             } else {
                 todo.parseException("Missing second parameter");
                 return null;
             }
         } else {
             if (expected == 0) {
                 return paramM;
             } else {
                 todo.parseException("Unexpected parameter: " + param);
                 return null;
             }
         }
     }
 }
