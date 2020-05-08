 // This is a generated file. Not intended for manual editing.
 package com.antonzherdev.objd.parser;
 
 import org.jetbrains.annotations.*;
 import com.intellij.lang.LighterASTNode;
 import com.intellij.lang.PsiBuilder;
 import com.intellij.lang.PsiBuilder.Marker;
 import com.intellij.openapi.diagnostic.Logger;
 import static com.antonzherdev.objd.psi.ObjDTypes.*;
 import static com.antonzherdev.objd.parser.GeneratedParserUtilBase.*;
 import com.intellij.psi.tree.IElementType;
 import com.intellij.lang.ASTNode;
 import com.intellij.psi.tree.TokenSet;
 import com.intellij.lang.PsiParser;
 
 @SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
 public class ObjDParser implements PsiParser {
 
   public static Logger LOG_ = Logger.getInstance("com.antonzherdev.objd.parser.ObjDParser");
 
   @NotNull
   public ASTNode parse(IElementType root_, PsiBuilder builder_) {
     int level_ = 0;
     boolean result_;
     builder_ = adapt_builder_(root_, builder_, this);
     if (root_ == CALL_NAME) {
       result_ = call_name(builder_, level_ + 1);
     }
     else if (root_ == CASE_COND) {
       result_ = case_cond(builder_, level_ + 1);
     }
     else if (root_ == CASE_ITEM) {
       result_ = case_item(builder_, level_ + 1);
     }
     else if (root_ == CLASS_BODY) {
       result_ = class_body(builder_, level_ + 1);
     }
     else if (root_ == CLASS_CONSTRUCTOR_FIELD) {
       result_ = class_constructor_field(builder_, level_ + 1);
     }
     else if (root_ == CLASS_EXTENDS) {
       result_ = class_extends(builder_, level_ + 1);
     }
     else if (root_ == CLASS_GENERIC) {
       result_ = class_generic(builder_, level_ + 1);
     }
     else if (root_ == CLASS_GENERICS) {
       result_ = class_generics(builder_, level_ + 1);
     }
     else if (root_ == CLASS_NAME) {
       result_ = class_name(builder_, level_ + 1);
     }
     else if (root_ == CLASS_STATEMENT) {
       result_ = class_statement(builder_, level_ + 1);
     }
     else if (root_ == CLASS_TYPE) {
       result_ = class_type(builder_, level_ + 1);
     }
     else if (root_ == DATA_TYPE) {
       result_ = data_type(builder_, level_ + 1);
     }
     else if (root_ == DATA_TYPE_COLLECTION) {
       result_ = data_type_collection(builder_, level_ + 1);
     }
     else if (root_ == DATA_TYPE_GENERICS) {
       result_ = data_type_generics(builder_, level_ + 1);
     }
     else if (root_ == DATA_TYPE_LAMBDA) {
       result_ = data_type_lambda(builder_, level_ + 1);
     }
     else if (root_ == DATA_TYPE_MAP) {
       result_ = data_type_map(builder_, level_ + 1);
     }
     else if (root_ == DATA_TYPE_OPTION) {
       result_ = data_type_option(builder_, level_ + 1);
     }
     else if (root_ == DATA_TYPE_REF) {
       result_ = data_type_ref(builder_, level_ + 1);
     }
     else if (root_ == DATA_TYPE_SELF) {
       result_ = data_type_self(builder_, level_ + 1);
     }
     else if (root_ == DATA_TYPE_SIMPLE) {
       result_ = data_type_simple(builder_, level_ + 1);
     }
     else if (root_ == DATA_TYPE_TUPLE) {
       result_ = data_type_tuple(builder_, level_ + 1);
     }
     else if (root_ == DEF_NAME) {
       result_ = def_name(builder_, level_ + 1);
     }
     else if (root_ == DEF_PARAMETER) {
       result_ = def_parameter(builder_, level_ + 1);
     }
     else if (root_ == DEF_STATEMENT) {
       result_ = def_statement(builder_, level_ + 1);
     }
     else if (root_ == ENUM_ITEM) {
       result_ = enum_item(builder_, level_ + 1);
     }
     else if (root_ == EXPR) {
       result_ = expr_(builder_, level_ + 1);
     }
     else if (root_ == EXPR_ARR) {
       result_ = expr_arr(builder_, level_ + 1);
     }
     else if (root_ == EXPR_BOOL) {
       result_ = expr_bool(builder_, level_ + 1);
     }
     else if (root_ == EXPR_BRACES) {
       result_ = expr_braces(builder_, level_ + 1);
     }
     else if (root_ == EXPR_BRACKETS) {
       result_ = expr_brackets(builder_, level_ + 1);
     }
     else if (root_ == EXPR_CALL) {
       result_ = expr_call(builder_, level_ + 1);
     }
     else if (root_ == EXPR_CALL_PARAM) {
       result_ = expr_call_param(builder_, level_ + 1);
     }
     else if (root_ == EXPR_CALL_PARAMS) {
       result_ = expr_call_params(builder_, level_ + 1);
     }
     else if (root_ == EXPR_CASE) {
       result_ = expr_case(builder_, level_ + 1);
     }
     else if (root_ == EXPR_COMP) {
       result_ = expr_comp(builder_, level_ + 1);
     }
     else if (root_ == EXPR_DO) {
       result_ = expr_do(builder_, level_ + 1);
     }
     else if (root_ == EXPR_DOT) {
       result_ = expr_dot(builder_, level_ + 1);
     }
     else if (root_ == EXPR_IF) {
       result_ = expr_if(builder_, level_ + 1);
     }
     else if (root_ == EXPR_INDEX) {
       result_ = expr_index(builder_, level_ + 1);
     }
     else if (root_ == EXPR_LAMBDA) {
       result_ = expr_lambda(builder_, level_ + 1);
     }
     else if (root_ == EXPR_MD) {
       result_ = expr_md(builder_, level_ + 1);
     }
     else if (root_ == EXPR_MINUS) {
       result_ = expr_minus(builder_, level_ + 1);
     }
     else if (root_ == EXPR_NOT) {
       result_ = expr_not(builder_, level_ + 1);
     }
     else if (root_ == EXPR_PM) {
       result_ = expr_pm(builder_, level_ + 1);
     }
     else if (root_ == EXPR_RETURN) {
       result_ = expr_return(builder_, level_ + 1);
     }
     else if (root_ == EXPR_SELF) {
       result_ = expr_self(builder_, level_ + 1);
     }
     else if (root_ == EXPR_SET) {
       result_ = expr_set(builder_, level_ + 1);
     }
     else if (root_ == EXPR_STRING_CONST) {
       result_ = expr_string_const(builder_, level_ + 1);
     }
     else if (root_ == EXPR_THROW) {
       result_ = expr_throw(builder_, level_ + 1);
     }
     else if (root_ == EXPR_VAL) {
       result_ = expr_val(builder_, level_ + 1);
     }
     else if (root_ == EXPR_WHILE) {
       result_ = expr_while(builder_, level_ + 1);
     }
     else if (root_ == FIELD_STATEMENT) {
       result_ = field_statement(builder_, level_ + 1);
     }
     else if (root_ == IMPORT_OD_FILE) {
       result_ = import_od_file(builder_, level_ + 1);
     }
     else if (root_ == IMPORT_STATEMENT) {
       result_ = import_statement(builder_, level_ + 1);
     }
     else if (root_ == INDEX_OP) {
       result_ = index_op(builder_, level_ + 1);
     }
     else if (root_ == LAMBDA_PAR) {
       result_ = lambda_par(builder_, level_ + 1);
     }
     else if (root_ == MODS) {
       result_ = mods(builder_, level_ + 1);
     }
     else if (root_ == PACK_STATEMENT) {
       result_ = pack_statement(builder_, level_ + 1);
     }
     else if (root_ == TERM) {
       result_ = term_(builder_, level_ + 1);
     }
     else if (root_ == TYPE_STATEMENT) {
       result_ = type_statement(builder_, level_ + 1);
     }
     else {
       Marker marker_ = builder_.mark();
       enterErrorRecordingSection(builder_, level_, _SECTION_RECOVER_, null);
       result_ = parse_root_(root_, builder_, level_);
       exitErrorRecordingSection(builder_, level_, result_, true, _SECTION_RECOVER_, TOKEN_ADVANCER);
       marker_.done(root_);
     }
     return builder_.getTreeBuilt();
   }
 
   protected boolean parse_root_(final IElementType root_, final PsiBuilder builder_, final int level_) {
     return file(builder_, level_ + 1);
   }
 
   private static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
     TokenSet.create(DATA_TYPE, DATA_TYPE_COLLECTION, DATA_TYPE_LAMBDA, DATA_TYPE_MAP,
       DATA_TYPE_OPTION, DATA_TYPE_SIMPLE, DATA_TYPE_TUPLE),
     TokenSet.create(EXPR, EXPR_ARR, EXPR_BOOL, EXPR_BRACES,
       EXPR_BRACKETS, EXPR_CALL, EXPR_COMP, EXPR_DO,
       EXPR_DOT, EXPR_IF, EXPR_INDEX, EXPR_LAMBDA,
       EXPR_MD, EXPR_MINUS, EXPR_NOT, EXPR_PM,
       EXPR_SELF, EXPR_SET, EXPR_STRING_CONST, EXPR_THROW,
       EXPR_VAL, EXPR_WHILE, TERM),
   };
 
   public static boolean type_extends_(IElementType child_, IElementType parent_) {
     for (TokenSet set : EXTENDS_SETS_) {
       if (set.contains(child_) && set.contains(parent_)) return true;
     }
     return false;
   }
 
   /* ********************************************************** */
   // IDENT
   public static boolean call_name(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "call_name")) return false;
     if (!nextTokenIs(builder_, IDENT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, IDENT);
     if (result_) {
       marker_.done(CALL_NAME);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // case_cond_unapply | case_cond_val | expr_string_const | INT | FLOAT | W_NIL | W_TRUE | W_FALSE
   public static boolean case_cond(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_cond")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<case cond>");
     result_ = case_cond_unapply(builder_, level_ + 1);
     if (!result_) result_ = case_cond_val(builder_, level_ + 1);
     if (!result_) result_ = expr_string_const(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, INT);
     if (!result_) result_ = consumeToken(builder_, FLOAT);
     if (!result_) result_ = consumeToken(builder_, W_NIL);
     if (!result_) result_ = consumeToken(builder_, W_TRUE);
     if (!result_) result_ = consumeToken(builder_, W_FALSE);
     if (result_) {
       marker_.done(CASE_COND);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // COMMA data_type
   static boolean case_cond_tp(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_cond_tp")) return false;
     if (!nextTokenIs(builder_, COMMA)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && data_type(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // IDENT OPEN_BRACKET case_cond (COMMA case_cond)* CLOSE_BRACKET case_cond_tp?
   static boolean case_cond_unapply(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_cond_unapply")) return false;
     if (!nextTokenIs(builder_, IDENT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, IDENT, OPEN_BRACKET);
     result_ = result_ && case_cond(builder_, level_ + 1);
     result_ = result_ && case_cond_unapply_3(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
     result_ = result_ && case_cond_unapply_5(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COMMA case_cond)*
   private static boolean case_cond_unapply_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_cond_unapply_3")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!case_cond_unapply_3_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "case_cond_unapply_3");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA case_cond
   private static boolean case_cond_unapply_3_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_cond_unapply_3_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && case_cond(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // case_cond_tp?
   private static boolean case_cond_unapply_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_cond_unapply_5")) return false;
     case_cond_tp(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // IDEND case_cond_tp?
   static boolean case_cond_val(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_cond_val")) return false;
     if (!nextTokenIs(builder_, IDEND)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, IDEND);
     result_ = result_ && case_cond_val_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // case_cond_tp?
   private static boolean case_cond_val_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_cond_val_1")) return false;
     case_cond_tp(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // case_cond ARROW expr_
   public static boolean case_item(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "case_item")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<case item>");
     result_ = case_cond(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, ARROW);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (result_) {
       marker_.done(CASE_ITEM);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // OPEN_BRACE enum_item* (def_statement | field_statement)* CLOSE_BRACE
   public static boolean class_body(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_body")) return false;
     if (!nextTokenIs(builder_, OPEN_BRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_BRACE);
     result_ = result_ && class_body_1(builder_, level_ + 1);
     result_ = result_ && class_body_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACE);
     if (result_) {
       marker_.done(CLASS_BODY);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // enum_item*
   private static boolean class_body_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_body_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!enum_item(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "class_body_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // (def_statement | field_statement)*
   private static boolean class_body_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_body_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!class_body_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "class_body_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // def_statement | field_statement
   private static boolean class_body_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_body_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = def_statement(builder_, level_ + 1);
     if (!result_) result_ = field_statement(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // W_WEAK? (W_VAR | W_VAL)? def_name COLON data_type (SET expr_)?
   public static boolean class_constructor_field(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_constructor_field")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<class constructor field>");
     result_ = class_constructor_field_0(builder_, level_ + 1);
     result_ = result_ && class_constructor_field_1(builder_, level_ + 1);
     result_ = result_ && def_name(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COLON);
     result_ = result_ && data_type(builder_, level_ + 1);
     result_ = result_ && class_constructor_field_5(builder_, level_ + 1);
     if (result_) {
       marker_.done(CLASS_CONSTRUCTOR_FIELD);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // W_WEAK?
   private static boolean class_constructor_field_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_constructor_field_0")) return false;
     consumeToken(builder_, W_WEAK);
     return true;
   }
 
   // (W_VAR | W_VAL)?
   private static boolean class_constructor_field_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_constructor_field_1")) return false;
     class_constructor_field_1_0(builder_, level_ + 1);
     return true;
   }
 
   // W_VAR | W_VAL
   private static boolean class_constructor_field_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_constructor_field_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_VAR);
     if (!result_) result_ = consumeToken(builder_, W_VAL);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (SET expr_)?
   private static boolean class_constructor_field_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_constructor_field_5")) return false;
     class_constructor_field_5_0(builder_, level_ + 1);
     return true;
   }
 
   // SET expr_
   private static boolean class_constructor_field_5_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_constructor_field_5_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SET);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // OPEN_BRACKET class_constructor_field (COMMA class_constructor_field)* CLOSE_BRACKET
   static boolean class_constructor_fields(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_constructor_fields")) return false;
     if (!nextTokenIs(builder_, OPEN_BRACKET)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_BRACKET);
     result_ = result_ && class_constructor_field(builder_, level_ + 1);
     result_ = result_ && class_constructor_fields_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COMMA class_constructor_field)*
   private static boolean class_constructor_fields_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_constructor_fields_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!class_constructor_fields_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "class_constructor_fields_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA class_constructor_field
   private static boolean class_constructor_fields_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_constructor_fields_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && class_constructor_field(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // data_type_ref data_type_generics?
   public static boolean class_extends(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_extends")) return false;
     if (!nextTokenIs(builder_, IDENT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = data_type_ref(builder_, level_ + 1);
     result_ = result_ && class_extends_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(CLASS_EXTENDS);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // data_type_generics?
   private static boolean class_extends_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_extends_1")) return false;
     data_type_generics(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // class_name (W_EXTENDS data_type)?
   public static boolean class_generic(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_generic")) return false;
     if (!nextTokenIs(builder_, IDENT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = class_name(builder_, level_ + 1);
     result_ = result_ && class_generic_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(CLASS_GENERIC);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (W_EXTENDS data_type)?
   private static boolean class_generic_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_generic_1")) return false;
     class_generic_1_0(builder_, level_ + 1);
     return true;
   }
 
   // W_EXTENDS data_type
   private static boolean class_generic_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_generic_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_EXTENDS);
     result_ = result_ && data_type(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // LESS class_generic (COMMA class_generic)* MORE
   public static boolean class_generics(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_generics")) return false;
     if (!nextTokenIs(builder_, LESS)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LESS);
     result_ = result_ && class_generic(builder_, level_ + 1);
     result_ = result_ && class_generics_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, MORE);
     if (result_) {
       marker_.done(CLASS_GENERICS);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (COMMA class_generic)*
   private static boolean class_generics_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_generics_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!class_generics_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "class_generics_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA class_generic
   private static boolean class_generics_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_generics_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && class_generic(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // IDENT
   public static boolean class_name(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_name")) return false;
     if (!nextTokenIs(builder_, IDENT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, IDENT);
     if (result_) {
       marker_.done(CLASS_NAME);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // W_PRIVATE? class_type class_name class_generics? class_constructor_fields? (W_EXTENDS class_extends expr_call_params? (W_WITH class_extends)* )? class_body?
   public static boolean class_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_statement")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<class statement>");
     result_ = class_statement_0(builder_, level_ + 1);
     result_ = result_ && class_type(builder_, level_ + 1);
     result_ = result_ && class_name(builder_, level_ + 1);
     result_ = result_ && class_statement_3(builder_, level_ + 1);
     result_ = result_ && class_statement_4(builder_, level_ + 1);
     result_ = result_ && class_statement_5(builder_, level_ + 1);
     result_ = result_ && class_statement_6(builder_, level_ + 1);
     if (result_) {
       marker_.done(CLASS_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // W_PRIVATE?
   private static boolean class_statement_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_statement_0")) return false;
     consumeToken(builder_, W_PRIVATE);
     return true;
   }
 
   // class_generics?
   private static boolean class_statement_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_statement_3")) return false;
     class_generics(builder_, level_ + 1);
     return true;
   }
 
   // class_constructor_fields?
   private static boolean class_statement_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_statement_4")) return false;
     class_constructor_fields(builder_, level_ + 1);
     return true;
   }
 
   // (W_EXTENDS class_extends expr_call_params? (W_WITH class_extends)* )?
   private static boolean class_statement_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_statement_5")) return false;
     class_statement_5_0(builder_, level_ + 1);
     return true;
   }
 
   // W_EXTENDS class_extends expr_call_params? (W_WITH class_extends)*
   private static boolean class_statement_5_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_statement_5_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_EXTENDS);
     result_ = result_ && class_extends(builder_, level_ + 1);
     result_ = result_ && class_statement_5_0_2(builder_, level_ + 1);
     result_ = result_ && class_statement_5_0_3(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // expr_call_params?
   private static boolean class_statement_5_0_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_statement_5_0_2")) return false;
     expr_call_params(builder_, level_ + 1);
     return true;
   }
 
   // (W_WITH class_extends)*
   private static boolean class_statement_5_0_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_statement_5_0_3")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!class_statement_5_0_3_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "class_statement_5_0_3");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // W_WITH class_extends
   private static boolean class_statement_5_0_3_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_statement_5_0_3_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_WITH);
     result_ = result_ && class_extends(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // class_body?
   private static boolean class_statement_6(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_statement_6")) return false;
     class_body(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // W_CLASS | W_TRAIT | W_STRUCT | W_ENUM
   public static boolean class_type(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "class_type")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<class type>");
     result_ = consumeToken(builder_, W_CLASS);
     if (!result_) result_ = consumeToken(builder_, W_TRAIT);
     if (!result_) result_ = consumeToken(builder_, W_STRUCT);
     if (!result_) result_ = consumeToken(builder_, W_ENUM);
     if (result_) {
       marker_.done(CLASS_TYPE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // data_type_lambda | data_type_option | data_type_collection | data_type_map | data_type_tuple | data_type_simple | data_type_self
   public static boolean data_type(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<data type>");
     result_ = data_type_lambda(builder_, level_ + 1);
     if (!result_) result_ = data_type_option(builder_, level_ + 1);
     if (!result_) result_ = data_type_collection(builder_, level_ + 1);
     if (!result_) result_ = data_type_map(builder_, level_ + 1);
     if (!result_) result_ = data_type_tuple(builder_, level_ + 1);
     if (!result_) result_ = data_type_simple(builder_, level_ + 1);
     if (!result_) result_ = data_type_self(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), DATA_TYPE)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(DATA_TYPE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // OPEN_SQUARE_BRACKET data_type CLOSE_SQUARE_BRACKET
   public static boolean data_type_collection(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_collection")) return false;
     if (!nextTokenIs(builder_, OPEN_SQUARE_BRACKET)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_SQUARE_BRACKET);
     result_ = result_ && data_type(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_SQUARE_BRACKET);
     if (result_) {
       marker_.done(DATA_TYPE_COLLECTION);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // LESS data_type (COMMA data_type)* MORE
   public static boolean data_type_generics(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_generics")) return false;
     if (!nextTokenIs(builder_, LESS)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LESS);
     result_ = result_ && data_type(builder_, level_ + 1);
     result_ = result_ && data_type_generics_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, MORE);
     if (result_) {
       marker_.done(DATA_TYPE_GENERICS);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (COMMA data_type)*
   private static boolean data_type_generics_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_generics_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!data_type_generics_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "data_type_generics_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA data_type
   private static boolean data_type_generics_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_generics_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && data_type(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // (data_type_option | data_type_collection | data_type_tuple | data_type_simple | data_type_self) ARROW data_type
   public static boolean data_type_lambda(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_lambda")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<data type lambda>");
     result_ = data_type_lambda_0(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, ARROW);
     result_ = result_ && data_type(builder_, level_ + 1);
     if (result_) {
       marker_.done(DATA_TYPE_LAMBDA);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // data_type_option | data_type_collection | data_type_tuple | data_type_simple | data_type_self
   private static boolean data_type_lambda_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_lambda_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = data_type_option(builder_, level_ + 1);
     if (!result_) result_ = data_type_collection(builder_, level_ + 1);
     if (!result_) result_ = data_type_tuple(builder_, level_ + 1);
     if (!result_) result_ = data_type_simple(builder_, level_ + 1);
     if (!result_) result_ = data_type_self(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // OPEN_SQUARE_BRACKET data_type COLON data_type CLOSE_SQUARE_BRACKET
   public static boolean data_type_map(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_map")) return false;
     if (!nextTokenIs(builder_, OPEN_SQUARE_BRACKET)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_SQUARE_BRACKET);
     result_ = result_ && data_type(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COLON);
     result_ = result_ && data_type(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_SQUARE_BRACKET);
     if (result_) {
       marker_.done(DATA_TYPE_MAP);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // (data_type_simple| data_type_collection | data_type_tuple | data_type_self) WHAT
   public static boolean data_type_option(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_option")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<data type option>");
     result_ = data_type_option_0(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, WHAT);
     if (result_) {
       marker_.done(DATA_TYPE_OPTION);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // data_type_simple| data_type_collection | data_type_tuple | data_type_self
   private static boolean data_type_option_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_option_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = data_type_simple(builder_, level_ + 1);
     if (!result_) result_ = data_type_collection(builder_, level_ + 1);
     if (!result_) result_ = data_type_tuple(builder_, level_ + 1);
     if (!result_) result_ = data_type_self(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // OPEN_SQUARE_BRACKET INT_CONST? CLOSE_SQUARE_BRACKET
   static boolean data_type_post_arr(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_post_arr")) return false;
     if (!nextTokenIs(builder_, OPEN_SQUARE_BRACKET)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_SQUARE_BRACKET);
     result_ = result_ && data_type_post_arr_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_SQUARE_BRACKET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // INT_CONST?
   private static boolean data_type_post_arr_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_post_arr_1")) return false;
     consumeToken(builder_, INT_CONST);
     return true;
   }
 
   /* ********************************************************** */
   // IDENT
   public static boolean data_type_ref(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_ref")) return false;
     if (!nextTokenIs(builder_, IDENT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, IDENT);
     if (result_) {
       marker_.done(DATA_TYPE_REF);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // W_SELF
   public static boolean data_type_self(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_self")) return false;
     if (!nextTokenIs(builder_, W_SELF)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_SELF);
     if (result_) {
       marker_.done(DATA_TYPE_SELF);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // (data_type_ref | TP_BOOL | TP_STRING |
   //     TP_INT4 | TP_UINT4 | TP_INT8 | TP_UINT8 | TP_INT | TP_UINT | TP_FLOAT | TP_FLOAT8 | TP_FLOAT4 | TP_BYTE | TP_UBYTE | TP_VOID) data_type_generics? data_type_post_arr?
   public static boolean data_type_simple(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_simple")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<data type simple>");
     result_ = data_type_simple_0(builder_, level_ + 1);
     result_ = result_ && data_type_simple_1(builder_, level_ + 1);
     result_ = result_ && data_type_simple_2(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), DATA_TYPE_SIMPLE)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(DATA_TYPE_SIMPLE);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // data_type_ref | TP_BOOL | TP_STRING |
   //     TP_INT4 | TP_UINT4 | TP_INT8 | TP_UINT8 | TP_INT | TP_UINT | TP_FLOAT | TP_FLOAT8 | TP_FLOAT4 | TP_BYTE | TP_UBYTE | TP_VOID
   private static boolean data_type_simple_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_simple_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = data_type_ref(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, TP_BOOL);
     if (!result_) result_ = consumeToken(builder_, TP_STRING);
     if (!result_) result_ = consumeToken(builder_, TP_INT4);
     if (!result_) result_ = consumeToken(builder_, TP_UINT4);
     if (!result_) result_ = consumeToken(builder_, TP_INT8);
     if (!result_) result_ = consumeToken(builder_, TP_UINT8);
     if (!result_) result_ = consumeToken(builder_, TP_INT);
     if (!result_) result_ = consumeToken(builder_, TP_UINT);
     if (!result_) result_ = consumeToken(builder_, TP_FLOAT);
     if (!result_) result_ = consumeToken(builder_, TP_FLOAT8);
     if (!result_) result_ = consumeToken(builder_, TP_FLOAT4);
     if (!result_) result_ = consumeToken(builder_, TP_BYTE);
     if (!result_) result_ = consumeToken(builder_, TP_UBYTE);
     if (!result_) result_ = consumeToken(builder_, TP_VOID);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // data_type_generics?
   private static boolean data_type_simple_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_simple_1")) return false;
     data_type_generics(builder_, level_ + 1);
     return true;
   }
 
   // data_type_post_arr?
   private static boolean data_type_simple_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_simple_2")) return false;
     data_type_post_arr(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // OPEN_BRACKET data_type? (COMMA data_type)* CLOSE_BRACKET
   public static boolean data_type_tuple(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_tuple")) return false;
     if (!nextTokenIs(builder_, OPEN_BRACKET)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_BRACKET);
     result_ = result_ && data_type_tuple_1(builder_, level_ + 1);
     result_ = result_ && data_type_tuple_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
     if (result_) {
       marker_.done(DATA_TYPE_TUPLE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // data_type?
   private static boolean data_type_tuple_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_tuple_1")) return false;
     data_type(builder_, level_ + 1);
     return true;
   }
 
   // (COMMA data_type)*
   private static boolean data_type_tuple_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_tuple_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!data_type_tuple_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "data_type_tuple_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA data_type
   private static boolean data_type_tuple_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "data_type_tuple_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && data_type(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // IDENT
   public static boolean def_name(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_name")) return false;
     if (!nextTokenIs(builder_, IDENT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, IDENT);
     if (result_) {
       marker_.done(DEF_NAME);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // (def_name | W_SELF)? COLON data_type (SET expr_)?
   public static boolean def_parameter(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_parameter")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<def parameter>");
     result_ = def_parameter_0(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, COLON);
     result_ = result_ && data_type(builder_, level_ + 1);
     result_ = result_ && def_parameter_3(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEF_PARAMETER);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (def_name | W_SELF)?
   private static boolean def_parameter_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_parameter_0")) return false;
     def_parameter_0_0(builder_, level_ + 1);
     return true;
   }
 
   // def_name | W_SELF
   private static boolean def_parameter_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_parameter_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = def_name(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, W_SELF);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (SET expr_)?
   private static boolean def_parameter_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_parameter_3")) return false;
     def_parameter_3_0(builder_, level_ + 1);
     return true;
   }
 
   // SET expr_
   private static boolean def_parameter_3_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_parameter_3_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SET);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // OPEN_BRACKET def_parameter? (COMMA def_parameter)* CLOSE_BRACKET
   static boolean def_parameters(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_parameters")) return false;
     if (!nextTokenIs(builder_, OPEN_BRACKET)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_BRACKET);
     result_ = result_ && def_parameters_1(builder_, level_ + 1);
     result_ = result_ && def_parameters_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // def_parameter?
   private static boolean def_parameters_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_parameters_1")) return false;
     def_parameter(builder_, level_ + 1);
     return true;
   }
 
   // (COMMA def_parameter)*
   private static boolean def_parameters_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_parameters_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!def_parameters_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "def_parameters_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA def_parameter
   private static boolean def_parameters_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_parameters_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && def_parameter(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // mods W_DEF def_name class_generics? def_parameters? (COLON data_type)? SET? expr_?
   public static boolean def_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_statement")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<def statement>");
     result_ = mods(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, W_DEF);
     result_ = result_ && def_name(builder_, level_ + 1);
     result_ = result_ && def_statement_3(builder_, level_ + 1);
     result_ = result_ && def_statement_4(builder_, level_ + 1);
     result_ = result_ && def_statement_5(builder_, level_ + 1);
     result_ = result_ && def_statement_6(builder_, level_ + 1);
     result_ = result_ && def_statement_7(builder_, level_ + 1);
     if (result_) {
       marker_.done(DEF_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // class_generics?
   private static boolean def_statement_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_statement_3")) return false;
     class_generics(builder_, level_ + 1);
     return true;
   }
 
   // def_parameters?
   private static boolean def_statement_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_statement_4")) return false;
     def_parameters(builder_, level_ + 1);
     return true;
   }
 
   // (COLON data_type)?
   private static boolean def_statement_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_statement_5")) return false;
     def_statement_5_0(builder_, level_ + 1);
     return true;
   }
 
   // COLON data_type
   private static boolean def_statement_5_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_statement_5_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COLON);
     result_ = result_ && data_type(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // SET?
   private static boolean def_statement_6(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_statement_6")) return false;
     consumeToken(builder_, SET);
     return true;
   }
 
   // expr_?
   private static boolean def_statement_7(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "def_statement_7")) return false;
     expr_(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // def_name expr_call_params?
   public static boolean enum_item(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "enum_item")) return false;
     if (!nextTokenIs(builder_, IDENT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = def_name(builder_, level_ + 1);
     result_ = result_ && enum_item_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(ENUM_ITEM);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // expr_call_params?
   private static boolean enum_item_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "enum_item_1")) return false;
     expr_call_params(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // expr_val | expr_set
   public static boolean expr_(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expr>");
     result_ = expr_val(builder_, level_ + 1);
     if (!result_) result_ = expr_set(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), EXPR)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(EXPR);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // OPEN_SQUARE_BRACKET expr_? (COMMA expr_)* CLOSE_SQUARE_BRACKET
   public static boolean expr_arr(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_arr")) return false;
     if (!nextTokenIs(builder_, OPEN_SQUARE_BRACKET)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_SQUARE_BRACKET);
     result_ = result_ && expr_arr_1(builder_, level_ + 1);
     result_ = result_ && expr_arr_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_SQUARE_BRACKET);
     if (result_) {
       marker_.done(EXPR_ARR);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // expr_?
   private static boolean expr_arr_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_arr_1")) return false;
     expr_(builder_, level_ + 1);
     return true;
   }
 
   // (COMMA expr_)*
   private static boolean expr_arr_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_arr_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!expr_arr_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "expr_arr_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA expr_
   private static boolean expr_arr_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_arr_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // expr_comp ((AND | OR) expr_bool)?
   public static boolean expr_bool(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_bool")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expr bool>");
     result_ = expr_comp(builder_, level_ + 1);
     result_ = result_ && expr_bool_1(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), EXPR_BOOL)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(EXPR_BOOL);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // ((AND | OR) expr_bool)?
   private static boolean expr_bool_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_bool_1")) return false;
     expr_bool_1_0(builder_, level_ + 1);
     return true;
   }
 
   // (AND | OR) expr_bool
   private static boolean expr_bool_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_bool_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = expr_bool_1_0_0(builder_, level_ + 1);
     result_ = result_ && expr_bool(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // AND | OR
   private static boolean expr_bool_1_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_bool_1_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, AND);
     if (!result_) result_ = consumeToken(builder_, OR);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // OPEN_BRACE expr_* CLOSE_BRACE
   public static boolean expr_braces(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_braces")) return false;
     if (!nextTokenIs(builder_, OPEN_BRACE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_BRACE);
     result_ = result_ && expr_braces_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACE);
     if (result_) {
       marker_.done(EXPR_BRACES);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // expr_*
   private static boolean expr_braces_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_braces_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!expr_(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "expr_braces_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   /* ********************************************************** */
   // OPEN_BRACKET expr_ (COMMA expr_)* CLOSE_BRACKET
   public static boolean expr_brackets(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_brackets")) return false;
     if (!nextTokenIs(builder_, OPEN_BRACKET)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_BRACKET);
     result_ = result_ && expr_(builder_, level_ + 1);
     result_ = result_ && expr_brackets_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
     if (result_) {
       marker_.done(EXPR_BRACKETS);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (COMMA expr_)*
   private static boolean expr_brackets_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_brackets_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!expr_brackets_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "expr_brackets_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA expr_
   private static boolean expr_brackets_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_brackets_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // call_name expr_call_generics? expr_call_params?
   public static boolean expr_call(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call")) return false;
     if (!nextTokenIs(builder_, IDENT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = call_name(builder_, level_ + 1);
     result_ = result_ && expr_call_1(builder_, level_ + 1);
     result_ = result_ && expr_call_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPR_CALL);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // expr_call_generics?
   private static boolean expr_call_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call_1")) return false;
     expr_call_generics(builder_, level_ + 1);
     return true;
   }
 
   // expr_call_params?
   private static boolean expr_call_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call_2")) return false;
     expr_call_params(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // LESS data_type (COMMA data_type)* MORE
   static boolean expr_call_generics(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call_generics")) return false;
     if (!nextTokenIs(builder_, LESS)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, LESS);
     result_ = result_ && data_type(builder_, level_ + 1);
     result_ = result_ && expr_call_generics_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, MORE);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COMMA data_type)*
   private static boolean expr_call_generics_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call_generics_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!expr_call_generics_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "expr_call_generics_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA data_type
   private static boolean expr_call_generics_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call_generics_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && data_type(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // (def_name SET)? expr_
   public static boolean expr_call_param(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call_param")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expr call param>");
     result_ = expr_call_param_0(builder_, level_ + 1);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPR_CALL_PARAM);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (def_name SET)?
   private static boolean expr_call_param_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call_param_0")) return false;
     expr_call_param_0_0(builder_, level_ + 1);
     return true;
   }
 
   // def_name SET
   private static boolean expr_call_param_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call_param_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = def_name(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, SET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // OPEN_BRACKET expr_call_param? (COMMA expr_call_param)* CLOSE_BRACKET
   public static boolean expr_call_params(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call_params")) return false;
     if (!nextTokenIs(builder_, OPEN_BRACKET)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_BRACKET);
     result_ = result_ && expr_call_params_1(builder_, level_ + 1);
     result_ = result_ && expr_call_params_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
     if (result_) {
       marker_.done(EXPR_CALL_PARAMS);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // expr_call_param?
   private static boolean expr_call_params_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call_params_1")) return false;
     expr_call_param(builder_, level_ + 1);
     return true;
   }
 
   // (COMMA expr_call_param)*
   private static boolean expr_call_params_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call_params_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!expr_call_params_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "expr_call_params_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA expr_call_param
   private static boolean expr_call_params_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_call_params_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && expr_call_param(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
  // W_CASE OPEN_BRACKET term_ CLOSE_BRACKET (OPEN_BRACKET case_item* CLOSE_BRACKET | case_item*)
   public static boolean expr_case(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_case")) return false;
     if (!nextTokenIs(builder_, W_CASE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, W_CASE, OPEN_BRACKET);
    result_ = result_ && term_(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
     result_ = result_ && expr_case_4(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPR_CASE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // OPEN_BRACKET case_item* CLOSE_BRACKET | case_item*
   private static boolean expr_case_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_case_4")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = expr_case_4_0(builder_, level_ + 1);
     if (!result_) result_ = expr_case_4_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // OPEN_BRACKET case_item* CLOSE_BRACKET
   private static boolean expr_case_4_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_case_4_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_BRACKET);
     result_ = result_ && expr_case_4_0_1(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // case_item*
   private static boolean expr_case_4_0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_case_4_0_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!case_item(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "expr_case_4_0_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // case_item*
   private static boolean expr_case_4_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_case_4_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!case_item(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "expr_case_4_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   /* ********************************************************** */
   // expr_pm ((EQ| MORE | LESS| MOREEQ| LESSEQ | NOT_EQ) expr_pm)?
   public static boolean expr_comp(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_comp")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expr comp>");
     result_ = expr_pm(builder_, level_ + 1);
     result_ = result_ && expr_comp_1(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), EXPR_COMP)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(EXPR_COMP);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // ((EQ| MORE | LESS| MOREEQ| LESSEQ | NOT_EQ) expr_pm)?
   private static boolean expr_comp_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_comp_1")) return false;
     expr_comp_1_0(builder_, level_ + 1);
     return true;
   }
 
   // (EQ| MORE | LESS| MOREEQ| LESSEQ | NOT_EQ) expr_pm
   private static boolean expr_comp_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_comp_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = expr_comp_1_0_0(builder_, level_ + 1);
     result_ = result_ && expr_pm(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // EQ| MORE | LESS| MOREEQ| LESSEQ | NOT_EQ
   private static boolean expr_comp_1_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_comp_1_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, EQ);
     if (!result_) result_ = consumeToken(builder_, MORE);
     if (!result_) result_ = consumeToken(builder_, LESS);
     if (!result_) result_ = consumeToken(builder_, MOREEQ);
     if (!result_) result_ = consumeToken(builder_, LESSEQ);
     if (!result_) result_ = consumeToken(builder_, NOT_EQ);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // W_DO expr_ W_WHILE OPEN_BRACKET expr_ CLOSE_BRACKET
   public static boolean expr_do(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_do")) return false;
     if (!nextTokenIs(builder_, W_DO)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_DO);
     result_ = result_ && expr_(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, W_WHILE, OPEN_BRACKET);
     result_ = result_ && expr_(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
     if (result_) {
       marker_.done(EXPR_DO);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // expr_index (DOT expr_index)*
   public static boolean expr_dot(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_dot")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expr dot>");
     result_ = expr_index(builder_, level_ + 1);
     result_ = result_ && expr_dot_1(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), EXPR_DOT)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(EXPR_DOT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (DOT expr_index)*
   private static boolean expr_dot_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_dot_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!expr_dot_1_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "expr_dot_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // DOT expr_index
   private static boolean expr_dot_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_dot_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, DOT);
     result_ = result_ && expr_index(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // W_IF OPEN_BRACKET expr_ CLOSE_BRACKET expr_ (W_ELSE expr_)?
   public static boolean expr_if(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_if")) return false;
     if (!nextTokenIs(builder_, W_IF)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, W_IF, OPEN_BRACKET);
     result_ = result_ && expr_(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
     result_ = result_ && expr_(builder_, level_ + 1);
     result_ = result_ && expr_if_5(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPR_IF);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (W_ELSE expr_)?
   private static boolean expr_if_5(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_if_5")) return false;
     expr_if_5_0(builder_, level_ + 1);
     return true;
   }
 
   // W_ELSE expr_
   private static boolean expr_if_5_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_if_5_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_ELSE);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // term_ index_op?
   public static boolean expr_index(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_index")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expr index>");
     result_ = term_(builder_, level_ + 1);
     result_ = result_ && expr_index_1(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), EXPR_INDEX)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(EXPR_INDEX);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // index_op?
   private static boolean expr_index_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_index_1")) return false;
     index_op(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // (lambda_par | lambda_pars) ARROW expr_
   public static boolean expr_lambda(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_lambda")) return false;
     if (!nextTokenIs(builder_, IDENT) && !nextTokenIs(builder_, OPEN_BRACKET)
         && replaceVariants(builder_, 2, "<expr lambda>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expr lambda>");
     result_ = expr_lambda_0(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, ARROW);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPR_LAMBDA);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // lambda_par | lambda_pars
   private static boolean expr_lambda_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_lambda_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = lambda_par(builder_, level_ + 1);
     if (!result_) result_ = lambda_pars(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // expr_dot ((MUL | DIV) expr_md)?
   public static boolean expr_md(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_md")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expr md>");
     result_ = expr_dot(builder_, level_ + 1);
     result_ = result_ && expr_md_1(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), EXPR_MD)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(EXPR_MD);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // ((MUL | DIV) expr_md)?
   private static boolean expr_md_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_md_1")) return false;
     expr_md_1_0(builder_, level_ + 1);
     return true;
   }
 
   // (MUL | DIV) expr_md
   private static boolean expr_md_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_md_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = expr_md_1_0_0(builder_, level_ + 1);
     result_ = result_ && expr_md(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // MUL | DIV
   private static boolean expr_md_1_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_md_1_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, MUL);
     if (!result_) result_ = consumeToken(builder_, DIV);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // MINUS expr_
   public static boolean expr_minus(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_minus")) return false;
     if (!nextTokenIs(builder_, MINUS)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, MINUS);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPR_MINUS);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // EXCLAMATION expr_
   public static boolean expr_not(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_not")) return false;
     if (!nextTokenIs(builder_, EXCLAMATION)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, EXCLAMATION);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPR_NOT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // expr_md ((PLUS | MINUS) expr_pm)?
   public static boolean expr_pm(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_pm")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expr pm>");
     result_ = expr_md(builder_, level_ + 1);
     result_ = result_ && expr_pm_1(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), EXPR_PM)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(EXPR_PM);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // ((PLUS | MINUS) expr_pm)?
   private static boolean expr_pm_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_pm_1")) return false;
     expr_pm_1_0(builder_, level_ + 1);
     return true;
   }
 
   // (PLUS | MINUS) expr_pm
   private static boolean expr_pm_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_pm_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = expr_pm_1_0_0(builder_, level_ + 1);
     result_ = result_ && expr_pm(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // PLUS | MINUS
   private static boolean expr_pm_1_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_pm_1_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, PLUS);
     if (!result_) result_ = consumeToken(builder_, MINUS);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // W_RETURN expr_
   public static boolean expr_return(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_return")) return false;
     if (!nextTokenIs(builder_, W_RETURN)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_RETURN);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPR_RETURN);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // W_SELF
   public static boolean expr_self(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_self")) return false;
     if (!nextTokenIs(builder_, W_SELF)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_SELF);
     if (result_) {
       marker_.done(EXPR_SELF);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // expr_bool ((SET | PLUS_SET| MINUS_SET | MUL_SET | DIV_SET) expr_bool)? post_op?
   public static boolean expr_set(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_set")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expr set>");
     result_ = expr_bool(builder_, level_ + 1);
     result_ = result_ && expr_set_1(builder_, level_ + 1);
     result_ = result_ && expr_set_2(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), EXPR_SET)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(EXPR_SET);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // ((SET | PLUS_SET| MINUS_SET | MUL_SET | DIV_SET) expr_bool)?
   private static boolean expr_set_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_set_1")) return false;
     expr_set_1_0(builder_, level_ + 1);
     return true;
   }
 
   // (SET | PLUS_SET| MINUS_SET | MUL_SET | DIV_SET) expr_bool
   private static boolean expr_set_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_set_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = expr_set_1_0_0(builder_, level_ + 1);
     result_ = result_ && expr_bool(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // SET | PLUS_SET| MINUS_SET | MUL_SET | DIV_SET
   private static boolean expr_set_1_0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_set_1_0_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SET);
     if (!result_) result_ = consumeToken(builder_, PLUS_SET);
     if (!result_) result_ = consumeToken(builder_, MINUS_SET);
     if (!result_) result_ = consumeToken(builder_, MUL_SET);
     if (!result_) result_ = consumeToken(builder_, DIV_SET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // post_op?
   private static boolean expr_set_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_set_2")) return false;
     post_op(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // STRING+
   public static boolean expr_string_const(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_string_const")) return false;
     if (!nextTokenIs(builder_, STRING)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, STRING);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!consumeToken(builder_, STRING)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "expr_string_const");
         break;
       }
       offset_ = next_offset_;
     }
     if (result_) {
       marker_.done(EXPR_STRING_CONST);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // W_THROW expr_
   public static boolean expr_throw(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_throw")) return false;
     if (!nextTokenIs(builder_, W_THROW)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_THROW);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPR_THROW);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // (W_VAL | W_VAR) def_name (COLON data_type)? (SET expr_)?
   public static boolean expr_val(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_val")) return false;
     if (!nextTokenIs(builder_, W_VAL) && !nextTokenIs(builder_, W_VAR)
         && replaceVariants(builder_, 2, "<expr val>")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<expr val>");
     result_ = expr_val_0(builder_, level_ + 1);
     result_ = result_ && def_name(builder_, level_ + 1);
     result_ = result_ && expr_val_2(builder_, level_ + 1);
     result_ = result_ && expr_val_3(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPR_VAL);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // W_VAL | W_VAR
   private static boolean expr_val_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_val_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_VAL);
     if (!result_) result_ = consumeToken(builder_, W_VAR);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COLON data_type)?
   private static boolean expr_val_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_val_2")) return false;
     expr_val_2_0(builder_, level_ + 1);
     return true;
   }
 
   // COLON data_type
   private static boolean expr_val_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_val_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COLON);
     result_ = result_ && data_type(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (SET expr_)?
   private static boolean expr_val_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_val_3")) return false;
     expr_val_3_0(builder_, level_ + 1);
     return true;
   }
 
   // SET expr_
   private static boolean expr_val_3_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_val_3_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SET);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // W_WHILE OPEN_BRACKET expr_ CLOSE_BRACKET expr_
   public static boolean expr_while(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "expr_while")) return false;
     if (!nextTokenIs(builder_, W_WHILE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, W_WHILE, OPEN_BRACKET);
     result_ = result_ && expr_(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (result_) {
       marker_.done(EXPR_WHILE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // mods (W_VAR | W_VAL)? def_name (COLON data_type)? (SET expr_)?
   public static boolean field_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "field_statement")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<field statement>");
     result_ = mods(builder_, level_ + 1);
     result_ = result_ && field_statement_1(builder_, level_ + 1);
     result_ = result_ && def_name(builder_, level_ + 1);
     result_ = result_ && field_statement_3(builder_, level_ + 1);
     result_ = result_ && field_statement_4(builder_, level_ + 1);
     if (result_) {
       marker_.done(FIELD_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   // (W_VAR | W_VAL)?
   private static boolean field_statement_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "field_statement_1")) return false;
     field_statement_1_0(builder_, level_ + 1);
     return true;
   }
 
   // W_VAR | W_VAL
   private static boolean field_statement_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "field_statement_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_VAR);
     if (!result_) result_ = consumeToken(builder_, W_VAL);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COLON data_type)?
   private static boolean field_statement_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "field_statement_3")) return false;
     field_statement_3_0(builder_, level_ + 1);
     return true;
   }
 
   // COLON data_type
   private static boolean field_statement_3_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "field_statement_3_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COLON);
     result_ = result_ && data_type(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (SET expr_)?
   private static boolean field_statement_4(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "field_statement_4")) return false;
     field_statement_4_0(builder_, level_ + 1);
     return true;
   }
 
   // SET expr_
   private static boolean field_statement_4_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "field_statement_4_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, SET);
     result_ = result_ && expr_(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // pack_statement? statement_*
   static boolean file(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "file")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = file_0(builder_, level_ + 1);
     result_ = result_ && file_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // pack_statement?
   private static boolean file_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "file_0")) return false;
     pack_statement(builder_, level_ + 1);
     return true;
   }
 
   // statement_*
   private static boolean file_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "file_1")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!statement_(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "file_1");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   /* ********************************************************** */
   // IDENT
   public static boolean import_od_file(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "import_od_file")) return false;
     if (!nextTokenIs(builder_, IDENT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, IDENT);
     if (result_) {
       marker_.done(IMPORT_OD_FILE);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // W_IMPORT (import_od_file | STRING+ | (LESS IDENT (DIV IDENT)* DOT IDENT MORE))
   public static boolean import_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "import_statement")) return false;
     if (!nextTokenIs(builder_, W_IMPORT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_IMPORT);
     result_ = result_ && import_statement_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(IMPORT_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // import_od_file | STRING+ | (LESS IDENT (DIV IDENT)* DOT IDENT MORE)
   private static boolean import_statement_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "import_statement_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = import_od_file(builder_, level_ + 1);
     if (!result_) result_ = import_statement_1_1(builder_, level_ + 1);
     if (!result_) result_ = import_statement_1_2(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // STRING+
   private static boolean import_statement_1_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "import_statement_1_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, STRING);
     int offset_ = builder_.getCurrentOffset();
     while (result_) {
       if (!consumeToken(builder_, STRING)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "import_statement_1_1");
         break;
       }
       offset_ = next_offset_;
     }
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // LESS IDENT (DIV IDENT)* DOT IDENT MORE
   private static boolean import_statement_1_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "import_statement_1_2")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, LESS, IDENT);
     result_ = result_ && import_statement_1_2_2(builder_, level_ + 1);
     result_ = result_ && consumeTokens(builder_, 0, DOT, IDENT, MORE);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (DIV IDENT)*
   private static boolean import_statement_1_2_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "import_statement_1_2_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!import_statement_1_2_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "import_statement_1_2_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // DIV IDENT
   private static boolean import_statement_1_2_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "import_statement_1_2_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, DIV, IDENT);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // OPEN_SQUARE_BRACKET expr_ CLOSE_SQUARE_BRACKET index_op?
   public static boolean index_op(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "index_op")) return false;
     if (!nextTokenIs(builder_, OPEN_SQUARE_BRACKET)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_SQUARE_BRACKET);
     result_ = result_ && expr_(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_SQUARE_BRACKET);
     result_ = result_ && index_op_3(builder_, level_ + 1);
     if (result_) {
       marker_.done(INDEX_OP);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // index_op?
   private static boolean index_op_3(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "index_op_3")) return false;
     index_op(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // def_name (COLON data_type)?
   public static boolean lambda_par(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "lambda_par")) return false;
     if (!nextTokenIs(builder_, IDENT)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = def_name(builder_, level_ + 1);
     result_ = result_ && lambda_par_1(builder_, level_ + 1);
     if (result_) {
       marker_.done(LAMBDA_PAR);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (COLON data_type)?
   private static boolean lambda_par_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "lambda_par_1")) return false;
     lambda_par_1_0(builder_, level_ + 1);
     return true;
   }
 
   // COLON data_type
   private static boolean lambda_par_1_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "lambda_par_1_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COLON);
     result_ = result_ && data_type(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // OPEN_BRACKET lambda_par (COMMA lambda_par)* CLOSE_BRACKET
   static boolean lambda_pars(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "lambda_pars")) return false;
     if (!nextTokenIs(builder_, OPEN_BRACKET)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, OPEN_BRACKET);
     result_ = result_ && lambda_par(builder_, level_ + 1);
     result_ = result_ && lambda_pars_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, CLOSE_BRACKET);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // (COMMA lambda_par)*
   private static boolean lambda_pars_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "lambda_pars_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!lambda_pars_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "lambda_pars_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // COMMA lambda_par
   private static boolean lambda_pars_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "lambda_pars_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, COMMA);
     result_ = result_ && lambda_par(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // static_mod | weak_mod | private_mod
   static boolean mod(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "mod")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = static_mod(builder_, level_ + 1);
     if (!result_) result_ = weak_mod(builder_, level_ + 1);
     if (!result_) result_ = private_mod(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // mod*
   public static boolean mods(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "mods")) return false;
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<mods>");
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!mod(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "mods");
         break;
       }
       offset_ = next_offset_;
     }
     marker_.done(MODS);
     exitErrorRecordingSection(builder_, level_, true, false, _SECTION_GENERAL_, null);
     return true;
   }
 
   /* ********************************************************** */
   // W_PACKAGE IDENT (DOT IDENT)*
   public static boolean pack_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pack_statement")) return false;
     if (!nextTokenIs(builder_, W_PACKAGE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, W_PACKAGE, IDENT);
     result_ = result_ && pack_statement_2(builder_, level_ + 1);
     if (result_) {
       marker_.done(PACK_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // (DOT IDENT)*
   private static boolean pack_statement_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pack_statement_2")) return false;
     int offset_ = builder_.getCurrentOffset();
     while (true) {
       if (!pack_statement_2_0(builder_, level_ + 1)) break;
       int next_offset_ = builder_.getCurrentOffset();
       if (offset_ == next_offset_) {
         empty_element_parsed_guard_(builder_, offset_, "pack_statement_2");
         break;
       }
       offset_ = next_offset_;
     }
     return true;
   }
 
   // DOT IDENT
   private static boolean pack_statement_2_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "pack_statement_2_0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeTokens(builder_, 0, DOT, IDENT);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // PLUS_PLUS | MINUS_MINUS
   static boolean post_op(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "post_op")) return false;
     if (!nextTokenIs(builder_, MINUS_MINUS) && !nextTokenIs(builder_, PLUS_PLUS)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, PLUS_PLUS);
     if (!result_) result_ = consumeToken(builder_, MINUS_MINUS);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // W_PRIVATE
   static boolean private_mod(PsiBuilder builder_, int level_) {
     return consumeToken(builder_, W_PRIVATE);
   }
 
   /* ********************************************************** */
   // (W_STUB? (class_statement | def_statement | field_statement | type_statement))|import_statement |COMMENT
   static boolean statement_(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "statement_")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = statement__0(builder_, level_ + 1);
     if (!result_) result_ = import_statement(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, COMMENT);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // W_STUB? (class_statement | def_statement | field_statement | type_statement)
   private static boolean statement__0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "statement__0")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = statement__0_0(builder_, level_ + 1);
     result_ = result_ && statement__0_1(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   // W_STUB?
   private static boolean statement__0_0(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "statement__0_0")) return false;
     consumeToken(builder_, W_STUB);
     return true;
   }
 
   // class_statement | def_statement | field_statement | type_statement
   private static boolean statement__0_1(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "statement__0_1")) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = class_statement(builder_, level_ + 1);
     if (!result_) result_ = def_statement(builder_, level_ + 1);
     if (!result_) result_ = field_statement(builder_, level_ + 1);
     if (!result_) result_ = type_statement(builder_, level_ + 1);
     if (!result_) {
       marker_.rollbackTo();
     }
     else {
       marker_.drop();
     }
     return result_;
   }
 
   /* ********************************************************** */
   // W_STATIC
   static boolean static_mod(PsiBuilder builder_, int level_) {
     return consumeToken(builder_, W_STATIC);
   }
 
   /* ********************************************************** */
   // expr_case | expr_throw | expr_not | expr_if | expr_lambda | expr_braces | expr_call | expr_arr | expr_brackets |
   //     expr_minus | W_NIL | W_TRUE | W_FALSE | expr_string_const | INT | FLOAT | expr_self | expr_while | expr_do | W_BREAK | expr_return
   public static boolean term_(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "term_")) return false;
     boolean result_ = false;
     int start_ = builder_.getCurrentOffset();
     Marker marker_ = builder_.mark();
     enterErrorRecordingSection(builder_, level_, _SECTION_GENERAL_, "<term>");
     result_ = expr_case(builder_, level_ + 1);
     if (!result_) result_ = expr_throw(builder_, level_ + 1);
     if (!result_) result_ = expr_not(builder_, level_ + 1);
     if (!result_) result_ = expr_if(builder_, level_ + 1);
     if (!result_) result_ = expr_lambda(builder_, level_ + 1);
     if (!result_) result_ = expr_braces(builder_, level_ + 1);
     if (!result_) result_ = expr_call(builder_, level_ + 1);
     if (!result_) result_ = expr_arr(builder_, level_ + 1);
     if (!result_) result_ = expr_brackets(builder_, level_ + 1);
     if (!result_) result_ = expr_minus(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, W_NIL);
     if (!result_) result_ = consumeToken(builder_, W_TRUE);
     if (!result_) result_ = consumeToken(builder_, W_FALSE);
     if (!result_) result_ = expr_string_const(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, INT);
     if (!result_) result_ = consumeToken(builder_, FLOAT);
     if (!result_) result_ = expr_self(builder_, level_ + 1);
     if (!result_) result_ = expr_while(builder_, level_ + 1);
     if (!result_) result_ = expr_do(builder_, level_ + 1);
     if (!result_) result_ = consumeToken(builder_, W_BREAK);
     if (!result_) result_ = expr_return(builder_, level_ + 1);
     LighterASTNode last_ = result_? builder_.getLatestDoneMarker() : null;
     if (last_ != null && last_.getStartOffset() == start_ && type_extends_(last_.getTokenType(), TERM)) {
       marker_.drop();
     }
     else if (result_) {
       marker_.done(TERM);
     }
     else {
       marker_.rollbackTo();
     }
     result_ = exitErrorRecordingSection(builder_, level_, result_, false, _SECTION_GENERAL_, null);
     return result_;
   }
 
   /* ********************************************************** */
   // W_TYPE class_name class_generics? SET class_extends
   public static boolean type_statement(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "type_statement")) return false;
     if (!nextTokenIs(builder_, W_TYPE)) return false;
     boolean result_ = false;
     Marker marker_ = builder_.mark();
     result_ = consumeToken(builder_, W_TYPE);
     result_ = result_ && class_name(builder_, level_ + 1);
     result_ = result_ && type_statement_2(builder_, level_ + 1);
     result_ = result_ && consumeToken(builder_, SET);
     result_ = result_ && class_extends(builder_, level_ + 1);
     if (result_) {
       marker_.done(TYPE_STATEMENT);
     }
     else {
       marker_.rollbackTo();
     }
     return result_;
   }
 
   // class_generics?
   private static boolean type_statement_2(PsiBuilder builder_, int level_) {
     if (!recursion_guard_(builder_, level_, "type_statement_2")) return false;
     class_generics(builder_, level_ + 1);
     return true;
   }
 
   /* ********************************************************** */
   // W_WEAK
   static boolean weak_mod(PsiBuilder builder_, int level_) {
     return consumeToken(builder_, W_WEAK);
   }
 
 }
