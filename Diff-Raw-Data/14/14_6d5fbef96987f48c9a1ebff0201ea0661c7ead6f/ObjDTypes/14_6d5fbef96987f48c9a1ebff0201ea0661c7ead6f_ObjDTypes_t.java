 // This is a generated file. Not intended for manual editing.
 package com.antonzherdev.objd.psi;
 
 import com.intellij.psi.tree.IElementType;
 import com.intellij.psi.PsiElement;
 import com.intellij.lang.ASTNode;
 import com.antonzherdev.objd.psi.impl.*;
 
 public interface ObjDTypes {
 
   IElementType CALL_NAME = new ObjDElementType("CALL_NAME");
   IElementType CASE_COND = new ObjDElementType("CASE_COND");
   IElementType CASE_COND_CONST = new ObjDElementType("CASE_COND_CONST");
   IElementType CASE_COND_TP = new ObjDElementType("CASE_COND_TP");
   IElementType CASE_COND_UNAPPLY = new ObjDElementType("CASE_COND_UNAPPLY");
   IElementType CASE_COND_VAL = new ObjDElementType("CASE_COND_VAL");
   IElementType CASE_ITEM = new ObjDElementType("CASE_ITEM");
   IElementType CLASS_BODY = new ObjDElementType("CLASS_BODY");
   IElementType CLASS_CONSTRUCTOR_FIELD = new ObjDElementType("CLASS_CONSTRUCTOR_FIELD");
   IElementType CLASS_EXTENDS = new ObjDElementType("CLASS_EXTENDS");
   IElementType CLASS_GENERIC = new ObjDElementType("CLASS_GENERIC");
   IElementType CLASS_GENERICS = new ObjDElementType("CLASS_GENERICS");
   IElementType CLASS_NAME = new ObjDElementType("CLASS_NAME");
   IElementType CLASS_STATEMENT = new ObjDElementType("CLASS_STATEMENT");
   IElementType CLASS_TYPE = new ObjDElementType("CLASS_TYPE");
   IElementType DATA_TYPE = new ObjDElementType("DATA_TYPE");
   IElementType DATA_TYPE_COLLECTION = new ObjDElementType("DATA_TYPE_COLLECTION");
   IElementType DATA_TYPE_GENERICS = new ObjDElementType("DATA_TYPE_GENERICS");
   IElementType DATA_TYPE_LAMBDA = new ObjDElementType("DATA_TYPE_LAMBDA");
   IElementType DATA_TYPE_MAP = new ObjDElementType("DATA_TYPE_MAP");
   IElementType DATA_TYPE_OPTION = new ObjDElementType("DATA_TYPE_OPTION");
   IElementType DATA_TYPE_REF = new ObjDElementType("DATA_TYPE_REF");
   IElementType DATA_TYPE_SELF = new ObjDElementType("DATA_TYPE_SELF");
   IElementType DATA_TYPE_SIMPLE = new ObjDElementType("DATA_TYPE_SIMPLE");
   IElementType DATA_TYPE_TUPLE = new ObjDElementType("DATA_TYPE_TUPLE");
   IElementType DEF_NAME = new ObjDElementType("DEF_NAME");
   IElementType DEF_PARAMETER = new ObjDElementType("DEF_PARAMETER");
   IElementType DEF_STATEMENT = new ObjDElementType("DEF_STATEMENT");
   IElementType ENUM_ITEM = new ObjDElementType("ENUM_ITEM");
   IElementType EXPORT_STATEMENT = new ObjDElementType("EXPORT_STATEMENT");
   IElementType EXPR = new ObjDElementType("EXPR");
   IElementType EXPR_ARR = new ObjDElementType("EXPR_ARR");
   IElementType EXPR_BOOL = new ObjDElementType("EXPR_BOOL");
   IElementType EXPR_BRACES = new ObjDElementType("EXPR_BRACES");
   IElementType EXPR_BRACKETS = new ObjDElementType("EXPR_BRACKETS");
   IElementType EXPR_CALL = new ObjDElementType("EXPR_CALL");
   IElementType EXPR_CALL_PARAM = new ObjDElementType("EXPR_CALL_PARAM");
   IElementType EXPR_CALL_PARAMS = new ObjDElementType("EXPR_CALL_PARAMS");
   IElementType EXPR_CALL_POST_LAMBDA = new ObjDElementType("EXPR_CALL_POST_LAMBDA");
   IElementType EXPR_CASE = new ObjDElementType("EXPR_CASE");
   IElementType EXPR_COMP = new ObjDElementType("EXPR_COMP");
   IElementType EXPR_DO = new ObjDElementType("EXPR_DO");
   IElementType EXPR_DOT = new ObjDElementType("EXPR_DOT");
   IElementType EXPR_IF = new ObjDElementType("EXPR_IF");
   IElementType EXPR_INDEX = new ObjDElementType("EXPR_INDEX");
   IElementType EXPR_LAMBDA = new ObjDElementType("EXPR_LAMBDA");
   IElementType EXPR_MD = new ObjDElementType("EXPR_MD");
   IElementType EXPR_MINUS = new ObjDElementType("EXPR_MINUS");
   IElementType EXPR_NOT = new ObjDElementType("EXPR_NOT");
   IElementType EXPR_PM = new ObjDElementType("EXPR_PM");
   IElementType EXPR_RETURN = new ObjDElementType("EXPR_RETURN");
   IElementType EXPR_SELF = new ObjDElementType("EXPR_SELF");
   IElementType EXPR_SET = new ObjDElementType("EXPR_SET");
   IElementType EXPR_STRING_CONST = new ObjDElementType("EXPR_STRING_CONST");
   IElementType EXPR_THROW = new ObjDElementType("EXPR_THROW");
   IElementType EXPR_VAL = new ObjDElementType("EXPR_VAL");
   IElementType EXPR_WHILE = new ObjDElementType("EXPR_WHILE");
   IElementType FIELD_STATEMENT = new ObjDElementType("FIELD_STATEMENT");
   IElementType IMPORT_OD_FILE = new ObjDElementType("IMPORT_OD_FILE");
   IElementType IMPORT_STATEMENT = new ObjDElementType("IMPORT_STATEMENT");
   IElementType INDEX_OP = new ObjDElementType("INDEX_OP");
   IElementType LAMBDA_PAR = new ObjDElementType("LAMBDA_PAR");
   IElementType MODS = new ObjDElementType("MODS");
   IElementType PACK_STATEMENT = new ObjDElementType("PACK_STATEMENT");
   IElementType TERM = new ObjDElementType("TERM");
   IElementType TYPE_STATEMENT = new ObjDElementType("TYPE_STATEMENT");
 
   IElementType AND = new ObjDTokenType("AND");
   IElementType ARROW = new ObjDTokenType("ARROW");
   IElementType CLOSE_BRACE = new ObjDTokenType("CLOSE_BRACE");
   IElementType CLOSE_BRACKET = new ObjDTokenType("CLOSE_BRACKET");
   IElementType CLOSE_SQUARE_BRACKET = new ObjDTokenType("CLOSE_SQUARE_BRACKET");
   IElementType COLON = new ObjDTokenType("COLON");
   IElementType COMMA = new ObjDTokenType("COMMA");
   IElementType COMMENT = new ObjDTokenType("COMMENT");
   IElementType DIV = new ObjDTokenType("DIV");
   IElementType DIV_SET = new ObjDTokenType("DIV_SET");
   IElementType DOT = new ObjDTokenType("DOT");
   IElementType EQ = new ObjDTokenType("EQ");
   IElementType EXCLAMATION = new ObjDTokenType("EXCLAMATION");
   IElementType FLOAT = new ObjDTokenType("FLOAT");
   IElementType IDENT = new ObjDTokenType("IDENT");
   IElementType INT = new ObjDTokenType("INT");
   IElementType LESS = new ObjDTokenType("LESS");
   IElementType LESSEQ = new ObjDTokenType("LESSEQ");
   IElementType MINUS = new ObjDTokenType("MINUS");
   IElementType MINUS_MINUS = new ObjDTokenType("MINUS_MINUS");
   IElementType MINUS_SET = new ObjDTokenType("MINUS_SET");
   IElementType MORE = new ObjDTokenType("MORE");
   IElementType MOREEQ = new ObjDTokenType("MOREEQ");
   IElementType MUL = new ObjDTokenType("MUL");
   IElementType MUL_SET = new ObjDTokenType("MUL_SET");
   IElementType NOT_EQ = new ObjDTokenType("NOT_EQ");
   IElementType OPEN_BRACE = new ObjDTokenType("OPEN_BRACE");
   IElementType OPEN_BRACKET = new ObjDTokenType("OPEN_BRACKET");
   IElementType OPEN_SQUARE_BRACKET = new ObjDTokenType("OPEN_SQUARE_BRACKET");
   IElementType OR = new ObjDTokenType("OR");
   IElementType PLUS = new ObjDTokenType("PLUS");
   IElementType PLUS_PLUS = new ObjDTokenType("PLUS_PLUS");
   IElementType PLUS_SET = new ObjDTokenType("PLUS_SET");
   IElementType SET = new ObjDTokenType("SET");
   IElementType STRING = new ObjDTokenType("STRING");
   IElementType TP_BOOL = new ObjDTokenType("TP_BOOL");
   IElementType TP_BYTE = new ObjDTokenType("TP_BYTE");
   IElementType TP_FLOAT = new ObjDTokenType("TP_FLOAT");
   IElementType TP_FLOAT4 = new ObjDTokenType("TP_FLOAT4");
   IElementType TP_FLOAT8 = new ObjDTokenType("TP_FLOAT8");
   IElementType TP_INT = new ObjDTokenType("TP_INT");
   IElementType TP_INT4 = new ObjDTokenType("TP_INT4");
   IElementType TP_INT8 = new ObjDTokenType("TP_INT8");
   IElementType TP_STRING = new ObjDTokenType("TP_STRING");
   IElementType TP_UBYTE = new ObjDTokenType("TP_UBYTE");
   IElementType TP_UINT = new ObjDTokenType("TP_UINT");
   IElementType TP_UINT4 = new ObjDTokenType("TP_UINT4");
   IElementType TP_UINT8 = new ObjDTokenType("TP_UINT8");
   IElementType TP_VOID = new ObjDTokenType("TP_VOID");
   IElementType WHAT = new ObjDTokenType("WHAT");
   IElementType W_BREAK = new ObjDTokenType("W_BREAK");
   IElementType W_CASE = new ObjDTokenType("W_CASE");
   IElementType W_CLASS = new ObjDTokenType("W_CLASS");
   IElementType W_DEF = new ObjDTokenType("W_DEF");
   IElementType W_DO = new ObjDTokenType("W_DO");
   IElementType W_ELSE = new ObjDTokenType("W_ELSE");
   IElementType W_ENUM = new ObjDTokenType("W_ENUM");
   IElementType W_EXPORT = new ObjDTokenType("W_EXPORT");
   IElementType W_EXTENDS = new ObjDTokenType("W_EXTENDS");
   IElementType W_FALSE = new ObjDTokenType("W_FALSE");
   IElementType W_IF = new ObjDTokenType("W_IF");
   IElementType W_IMPORT = new ObjDTokenType("W_IMPORT");
   IElementType W_LAZY = new ObjDTokenType("W_LAZY");
   IElementType W_NIL = new ObjDTokenType("W_NIL");
   IElementType W_PACKAGE = new ObjDTokenType("W_PACKAGE");
   IElementType W_PRIVATE = new ObjDTokenType("W_PRIVATE");
   IElementType W_PROTECTED = new ObjDTokenType("W_PROTECTED");
   IElementType W_RETURN = new ObjDTokenType("W_RETURN");
   IElementType W_SELF = new ObjDTokenType("W_SELF");
   IElementType W_STATIC = new ObjDTokenType("W_STATIC");
   IElementType W_STRUCT = new ObjDTokenType("W_STRUCT");
   IElementType W_STUB = new ObjDTokenType("W_STUB");
   IElementType W_THROW = new ObjDTokenType("W_THROW");
   IElementType W_TRAIT = new ObjDTokenType("W_TRAIT");
   IElementType W_TRUE = new ObjDTokenType("W_TRUE");
   IElementType W_TYPE = new ObjDTokenType("W_TYPE");
   IElementType W_VAL = new ObjDTokenType("W_VAL");
   IElementType W_VAR = new ObjDTokenType("W_VAR");
   IElementType W_WEAK = new ObjDTokenType("W_WEAK");
   IElementType W_WHILE = new ObjDTokenType("W_WHILE");
   IElementType W_WITH = new ObjDTokenType("W_WITH");
 
   class Factory {
     public static PsiElement createElement(ASTNode node) {
       IElementType type = node.getElementType();
        if (type == CALL_NAME) {
         return new ObjDCallNameImpl(node);
       }
       else if (type == CASE_COND) {
         return new ObjDCaseCondImpl(node);
       }
       else if (type == CASE_COND_CONST) {
         return new ObjDCaseCondConstImpl(node);
       }
       else if (type == CASE_COND_TP) {
         return new ObjDCaseCondTpImpl(node);
       }
       else if (type == CASE_COND_UNAPPLY) {
         return new ObjDCaseCondUnapplyImpl(node);
       }
       else if (type == CASE_COND_VAL) {
         return new ObjDCaseCondValImpl(node);
       }
       else if (type == CASE_ITEM) {
         return new ObjDCaseItemImpl(node);
       }
       else if (type == CLASS_BODY) {
         return new ObjDClassBodyImpl(node);
       }
       else if (type == CLASS_CONSTRUCTOR_FIELD) {
         return new ObjDClassConstructorFieldImpl(node);
       }
       else if (type == CLASS_EXTENDS) {
         return new ObjDClassExtendsImpl(node);
       }
       else if (type == CLASS_GENERIC) {
         return new ObjDClassGenericImpl(node);
       }
       else if (type == CLASS_GENERICS) {
         return new ObjDClassGenericsImpl(node);
       }
       else if (type == CLASS_NAME) {
         return new ObjDClassNameImpl(node);
       }
       else if (type == CLASS_STATEMENT) {
         return new ObjDClassStatementImpl(node);
       }
       else if (type == CLASS_TYPE) {
         return new ObjDClassTypeImpl(node);
       }
       else if (type == DATA_TYPE) {
         return new ObjDDataTypeImpl(node);
       }
       else if (type == DATA_TYPE_COLLECTION) {
         return new ObjDDataTypeCollectionImpl(node);
       }
       else if (type == DATA_TYPE_GENERICS) {
         return new ObjDDataTypeGenericsImpl(node);
       }
       else if (type == DATA_TYPE_LAMBDA) {
         return new ObjDDataTypeLambdaImpl(node);
       }
       else if (type == DATA_TYPE_MAP) {
         return new ObjDDataTypeMapImpl(node);
       }
       else if (type == DATA_TYPE_OPTION) {
         return new ObjDDataTypeOptionImpl(node);
       }
       else if (type == DATA_TYPE_REF) {
         return new ObjDDataTypeRefImpl(node);
       }
       else if (type == DATA_TYPE_SELF) {
         return new ObjDDataTypeSelfImpl(node);
       }
       else if (type == DATA_TYPE_SIMPLE) {
         return new ObjDDataTypeSimpleImpl(node);
       }
       else if (type == DATA_TYPE_TUPLE) {
         return new ObjDDataTypeTupleImpl(node);
       }
       else if (type == DEF_NAME) {
         return new ObjDDefNameImpl(node);
       }
       else if (type == DEF_PARAMETER) {
         return new ObjDDefParameterImpl(node);
       }
       else if (type == DEF_STATEMENT) {
         return new ObjDDefStatementImpl(node);
       }
       else if (type == ENUM_ITEM) {
         return new ObjDEnumItemImpl(node);
       }
       else if (type == EXPORT_STATEMENT) {
         return new ObjDExportStatementImpl(node);
       }
       else if (type == EXPR) {
         return new ObjDExprImpl(node);
       }
       else if (type == EXPR_ARR) {
         return new ObjDExprArrImpl(node);
       }
       else if (type == EXPR_BOOL) {
         return new ObjDExprBoolImpl(node);
       }
       else if (type == EXPR_BRACES) {
         return new ObjDExprBracesImpl(node);
       }
       else if (type == EXPR_BRACKETS) {
         return new ObjDExprBracketsImpl(node);
       }
       else if (type == EXPR_CALL) {
         return new ObjDExprCallImpl(node);
       }
       else if (type == EXPR_CALL_PARAM) {
         return new ObjDExprCallParamImpl(node);
       }
       else if (type == EXPR_CALL_PARAMS) {
         return new ObjDExprCallParamsImpl(node);
       }
       else if (type == EXPR_CALL_POST_LAMBDA) {
         return new ObjDExprCallPostLambdaImpl(node);
       }
       else if (type == EXPR_CASE) {
         return new ObjDExprCaseImpl(node);
       }
       else if (type == EXPR_COMP) {
         return new ObjDExprCompImpl(node);
       }
       else if (type == EXPR_DO) {
         return new ObjDExprDoImpl(node);
       }
       else if (type == EXPR_DOT) {
         return new ObjDExprDotImpl(node);
       }
       else if (type == EXPR_IF) {
         return new ObjDExprIfImpl(node);
       }
       else if (type == EXPR_INDEX) {
         return new ObjDExprIndexImpl(node);
       }
       else if (type == EXPR_LAMBDA) {
         return new ObjDExprLambdaImpl(node);
       }
       else if (type == EXPR_MD) {
         return new ObjDExprMdImpl(node);
       }
       else if (type == EXPR_MINUS) {
         return new ObjDExprMinusImpl(node);
       }
       else if (type == EXPR_NOT) {
         return new ObjDExprNotImpl(node);
       }
       else if (type == EXPR_PM) {
         return new ObjDExprPmImpl(node);
       }
       else if (type == EXPR_RETURN) {
         return new ObjDExprReturnImpl(node);
       }
       else if (type == EXPR_SELF) {
         return new ObjDExprSelfImpl(node);
       }
       else if (type == EXPR_SET) {
         return new ObjDExprSetImpl(node);
       }
       else if (type == EXPR_STRING_CONST) {
         return new ObjDExprStringConstImpl(node);
       }
       else if (type == EXPR_THROW) {
         return new ObjDExprThrowImpl(node);
       }
       else if (type == EXPR_VAL) {
         return new ObjDExprValImpl(node);
       }
       else if (type == EXPR_WHILE) {
         return new ObjDExprWhileImpl(node);
       }
       else if (type == FIELD_STATEMENT) {
         return new ObjDFieldStatementImpl(node);
       }
       else if (type == IMPORT_OD_FILE) {
         return new ObjDImportOdFileImpl(node);
       }
       else if (type == IMPORT_STATEMENT) {
         return new ObjDImportStatementImpl(node);
       }
       else if (type == INDEX_OP) {
         return new ObjDIndexOpImpl(node);
       }
       else if (type == LAMBDA_PAR) {
         return new ObjDLambdaParImpl(node);
       }
       else if (type == MODS) {
         return new ObjDModsImpl(node);
       }
       else if (type == PACK_STATEMENT) {
         return new ObjDPackStatementImpl(node);
       }
       else if (type == TERM) {
         return new ObjDTermImpl(node);
       }
       else if (type == TYPE_STATEMENT) {
         return new ObjDTypeStatementImpl(node);
       }
       throw new AssertionError("Unknown element type: " + type);
     }
   }
 }
