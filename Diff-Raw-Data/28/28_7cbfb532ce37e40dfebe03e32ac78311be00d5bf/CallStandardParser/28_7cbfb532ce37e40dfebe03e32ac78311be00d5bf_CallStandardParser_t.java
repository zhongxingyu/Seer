 package de.weltraumschaf.caythe.frontend.pascal.parsers;
 
 import de.weltraumschaf.caythe.intermediate.TypeSpecification;
 import de.weltraumschaf.caythe.frontend.Token;
 import de.weltraumschaf.caythe.frontend.pascal.PascalErrorCode;
 import de.weltraumschaf.caythe.frontend.pascal.PascalTopDownParser;
 import de.weltraumschaf.caythe.intermediate.CodeFactory;
 import de.weltraumschaf.caythe.intermediate.CodeNode;
 
 import de.weltraumschaf.caythe.intermediate.RoutineCode;
 import de.weltraumschaf.caythe.intermediate.SymbolTableEntry;
 
 import de.weltraumschaf.caythe.intermediate.symboltableimpl.Predefined;
 import de.weltraumschaf.caythe.intermediate.symboltableimpl.RoutineCodeImpl;
 import de.weltraumschaf.caythe.intermediate.typeimpl.TypeFormImpl;
 import static de.weltraumschaf.caythe.intermediate.codeimpl.CodeNodeTypeImpl.CALL;
 import static de.weltraumschaf.caythe.intermediate.symboltableimpl.SymbolTableKeyImpl.ROUTINE_CODE;
 import static de.weltraumschaf.caythe.intermediate.codeimpl.CodeKeyImpl.ID;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public class CallStandardParser extends CallParser {
 
     public CallStandardParser(PascalTopDownParser parent) {
         super(parent);
     }
 
     @Override
     public CodeNode parse(Token token) throws Exception {
         CodeNode callNode = CodeFactory.createCodeNode(CALL);
         SymbolTableEntry pfId = symbolTableStack.lookup(token.getText().toLowerCase());
         RoutineCode routineCode = (RoutineCode) pfId.getAttribute(ROUTINE_CODE);
         callNode.setAttribute(ID, pfId);
 
         token = nextToken(); // Consume procedure or function idnetnifier.
 
         switch ((RoutineCodeImpl) routineCode) {
             case READ:
             case READLN:  return parseReadReadln(token, callNode, pfId);
 
             case WRITE:
             case WRITELN: return parseWriteWriteln(token, callNode, pfId);
 
             case EOF:
             case EOLN:    return parseEofEoln(token, callNode, pfId);
 
             case ABS:
             case SQR:     return parseAbsSqr(token, callNode, pfId);
 
             case ARCTAN:
             case COS:
             case EXP:
             case LN:
             case SIN:
             case SQRT:    return parseArctanConExpLnSinSqrt(token, callNode, pfId);
 
             case PRED:
             case SUCC:    return parsePredSucc(token, callNode, pfId);
 
             case CHR:     return parseChr(token, callNode, pfId);
             case ODD:     return parseOdd(token, callNode, pfId);
             case ORD:     return parseOrd(token, callNode, pfId);
 
             case ROUND:
             case TRUNC:   return parseRoundTrunc(token, callNode, pfId);
 
             default:      return null;
         }
     }
 
     private CodeNode parseReadReadln(Token token, CodeNode callNode, SymbolTableEntry pfId) throws Exception {
         CodeNode parmsNode = parseActualParameters(token, pfId, false, true, false);
         callNode.addChild(parmsNode);
 
         // Read must have parameters
         if ((pfId == Predefined.readId) && callNode.getChildren().isEmpty()) {
            errorHandler.flag(token, PascalErrorCode.WRONG_NUMBER_OF_PARAMS, this);
         }
 
         return callNode;
     }
 
     private CodeNode parseWriteWriteln(Token token, CodeNode callNode, SymbolTableEntry pfId) throws Exception {
         CodeNode parmsNode = parseActualParameters(token, pfId, false, false, true);
         callNode.addChild(parmsNode);
 
         // Write must have parameters.
         if ((pfId == Predefined.writeId) && callNode.getChildren().isEmpty()) {
            errorHandler.flag(token, PascalErrorCode.WRONG_NUMBER_OF_PARAMS, this);
         }
 
         return callNode;
     }
 
     private CodeNode parseEofEoln(Token token, CodeNode callNode, SymbolTableEntry pfId) throws Exception {
         CodeNode parmsNode = parseActualParameters(token, pfId, false, false, false);
         callNode.addChild(parmsNode);
 
         // There should be no actual paremeters.
         // The function return type is boolean.
         if (checkParamCount(token, parmsNode, 0)) {
             callNode.setTypeSpecification(Predefined.booleanType);
         }
 
         return callNode;
     }
 
     private CodeNode parseAbsSqr(Token token, CodeNode callNode, SymbolTableEntry pfId) throws Exception {
         CodeNode parmsNode = parseActualParameters(token, pfId, false, false, false);
         callNode.addChild(parmsNode);
 
         // There should be one integer or real parameter.
         // The function return type is the parameter type.
         if (checkParamCount(token, parmsNode, 1)) {
             TypeSpecification argType = parmsNode.getChildren().get(0).getTypeSpecification().baseType();
 
             if ((argType == Predefined.integerType) || (argType == Predefined.realType)) {
                 callNode.setTypeSpecification(argType);
             } else {
                 errorHandler.flag(token, PascalErrorCode.INVALID_TYPE, this);
             }
         }
 
         return callNode;
     }
 
     private CodeNode parseArctanConExpLnSinSqrt(Token token, CodeNode callNode, SymbolTableEntry pfId) throws Exception {
         CodeNode parmsNode = parseActualParameters(token, pfId, false, false, false);
         callNode.addChild(parmsNode);
 
         // There should be one integer or real parameter.
         // The retrn type is real.
         if (checkParamCount(token, parmsNode, 1)) {
             TypeSpecification argType = parmsNode.getChildren().get(0).getTypeSpecification().baseType();
 
             if ((argType == Predefined.integerType) || (argType == Predefined.realType)) {
                 callNode.setTypeSpecification(Predefined.realType);
             } else {
                 errorHandler.flag(token, PascalErrorCode.INVALID_TYPE, this);
             }
         }
 
         return callNode;
     }
 
     private CodeNode parsePredSucc(Token token, CodeNode callNode, SymbolTableEntry pfId) throws Exception {
         CodeNode parmsNode = parseActualParameters(token, pfId, false, false, false);
         callNode.addChild(parmsNode);
 
         // There should be one integer or enumeration parameter.
         // The function return type is the parameter type.
         if (checkParamCount(token, parmsNode, 1)) {
             TypeSpecification argType = parmsNode.getChildren().get(0).getTypeSpecification().baseType();
 
             if ((argType == Predefined.integerType) || (argType.getForm() == TypeFormImpl.ENUMERATION)) {
                 callNode.setTypeSpecification(argType);
             } else {
                 errorHandler.flag(token, PascalErrorCode.INVALID_TYPE, this);
             }
         }
 
         return callNode;
     }
 
     private CodeNode parseChr(Token token, CodeNode callNode, SymbolTableEntry pfId) throws Exception {
         CodeNode parmsNode = parseActualParameters(token, pfId, false, false, false);
         callNode.addChild(parmsNode);
 
         // There should be one integer parameter.
         // The function return type is character.
         if (checkParamCount(token, parmsNode, 1)) {
             TypeSpecification argType = parmsNode.getChildren().get(0).getTypeSpecification().baseType();
 
             if (argType == Predefined.integerType) {
                 callNode.setTypeSpecification(Predefined.charType);
             } else {
                 errorHandler.flag(token, PascalErrorCode.INVALID_TYPE, this);
             }
         }
 
         return callNode;
     }
 
     private CodeNode parseOdd(Token token, CodeNode callNode, SymbolTableEntry pfId) throws Exception {
         CodeNode parmsNode = parseActualParameters(token, pfId, false, false, false);
         callNode.addChild(parmsNode);
 
         // There should be one integer parameter.
         // The function return type is boolean.
         if (checkParamCount(token, parmsNode, 1)) {
             TypeSpecification argType = parmsNode.getChildren().get(0).getTypeSpecification().baseType();
 
             if (argType == Predefined.integerType) {
                 callNode.setTypeSpecification(Predefined.booleanType);
             } else {
                 errorHandler.flag(token, PascalErrorCode.INVALID_TYPE, this);
             }
         }
 
         return callNode;
     }
 
     private CodeNode parseOrd(Token token, CodeNode callNode, SymbolTableEntry pfId) throws Exception {
         CodeNode parmsNode = parseActualParameters(token, pfId, false, false, false);
         callNode.addChild(parmsNode);
 
         // There should be one character or enumeration parameter.
         // The function return type is integer.
         if (checkParamCount(token, parmsNode, 1)) {
             TypeSpecification argType = parmsNode.getChildren().get(0).getTypeSpecification().baseType();
 
             if ((argType == Predefined.charType) || (argType.getForm() == TypeFormImpl.ENUMERATION)) {
                 callNode.setTypeSpecification(Predefined.integerType);
             } else {
                 errorHandler.flag(token, PascalErrorCode.INVALID_TYPE, this);
             }
         }
 
         return callNode;
     }
 
     private CodeNode parseRoundTrunc(Token token, CodeNode callNode, SymbolTableEntry pfId) throws Exception {
         CodeNode parmsNode = parseActualParameters(token, pfId, false, false, false);
         callNode.addChild(parmsNode);
 
         // There should be one real parameter.
         // The function return type is integer.
         if (checkParamCount(token, parmsNode, 1)) {
             TypeSpecification argType = parmsNode.getChildren().get(0).getTypeSpecification().baseType();
 
             if (argType == Predefined.realType) {
                 callNode.setTypeSpecification(Predefined.integerType);
             } else {
                 errorHandler.flag(token, PascalErrorCode.INVALID_TYPE, this);
             }
         }
 
         return callNode;
     }
 
     private boolean checkParamCount(Token token, CodeNode parmsNode, int count) {
         if ((parmsNode == null) && (count == 0)) {
             return true;
         }
 
         if (parmsNode.getChildren().size() == count) {
             return true;
         }
 
        errorHandler.flag(token, PascalErrorCode.WRONG_NUMBER_OF_PARAMS, this);
         return false;
     }
 
 }
