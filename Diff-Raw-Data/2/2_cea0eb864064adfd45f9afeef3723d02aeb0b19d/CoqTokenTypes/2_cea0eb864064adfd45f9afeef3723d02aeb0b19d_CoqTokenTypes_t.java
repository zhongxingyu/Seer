 package com.qq.coqide.lexer;
 
 import com.intellij.psi.tree.IElementType;
 
 public interface CoqTokenTypes {
 
     /****************************** Keywords ****************************************/
 
     // Vernacular
 
     IElementType ADMITTED    = new VernacularKeyword("ADMITTED");
     IElementType AXIOM       = new VernacularKeyword("AXIOM");
     IElementType COFIXPOINT  = new VernacularKeyword("COFIXPOINT");
     IElementType COINDUCTIVE = new VernacularKeyword("COINDUCTIVE");
     IElementType CONJECTURE  = new VernacularKeyword("CONJECTURE");
     IElementType COROLLARY   = new VernacularKeyword("COROLLARY");
     IElementType DEFINED     = new VernacularKeyword("DEFINED");
    IElementType DEFINITION  = new VernacularKeyword("DEFINITION");
     IElementType EXAMPLE     = new VernacularKeyword("EXAMPLE");
     IElementType FACT        = new VernacularKeyword("FACT");
     IElementType FIXPOINT    = new VernacularKeyword("FIXPOINT");
     IElementType HYPOTHESES  = new VernacularKeyword("HYPOTHESES");
     IElementType HYPOTHESIS  = new VernacularKeyword("HYPOTHESIS");
     IElementType INDUCTIVE   = new VernacularKeyword("INDUCTIVE");
     IElementType LEMMA       = new VernacularKeyword("LEMMA");
     IElementType LET_UPPER   = new VernacularKeyword("LET_UPPER");
     IElementType PARAMETER   = new VernacularKeyword("PARAMETER");
     IElementType PARAMETERS  = new VernacularKeyword("PARAMETERS");
     IElementType PROOF       = new VernacularKeyword("PROOF");
     IElementType PROPOSITION = new VernacularKeyword("PROPOSITION");
     IElementType QED         = new VernacularKeyword("QED");
     IElementType REMARK      = new VernacularKeyword("REMARK");
     IElementType THEOREM     = new VernacularKeyword("THEOREM");
     IElementType VARIABLE    = new VernacularKeyword("VARIABLE");
     IElementType VARIABLES   = new VernacularKeyword("VARIABLES");
 
     
     // Gallina
 
     IElementType UNDERSCORE = new GallinaKeyword("UNDERSCORE");
     IElementType AS         = new GallinaKeyword("AS");
     IElementType AT         = new GallinaKeyword("AT");
     IElementType COFIX      = new GallinaKeyword("COFIX");
     IElementType ELSE       = new GallinaKeyword("ELSE");
     IElementType END        = new GallinaKeyword("END");
     IElementType EXISTS     = new GallinaKeyword("EXISTS");
     IElementType EXISTS2    = new GallinaKeyword("EXISTS2");
     IElementType FIX        = new GallinaKeyword("FIX");
     IElementType FOR        = new GallinaKeyword("FOR");
     IElementType FORALL     = new GallinaKeyword("FORALL");
     IElementType FUN        = new GallinaKeyword("FUN");
     IElementType IF         = new GallinaKeyword("IF");
     IElementType IF_UPPER   = new GallinaKeyword("IF_UPPER");
     IElementType IN         = new GallinaKeyword("IN");
     IElementType LET        = new GallinaKeyword("LET");
     IElementType MATCH      = new GallinaKeyword("MATCH");
     IElementType MOD        = new GallinaKeyword("MOD");
     IElementType PROP       = new GallinaKeyword("PROP");
     IElementType RETURN     = new GallinaKeyword("RETURN");
     IElementType SET        = new GallinaKeyword("SET");
     IElementType THEN       = new GallinaKeyword("THEN");
     IElementType TYPE       = new GallinaKeyword("TYPE");
     IElementType USING      = new GallinaKeyword("USING");
     IElementType WHERE      = new GallinaKeyword("WHERE");
     IElementType WITH       = new GallinaKeyword("WITH");
 
 
     /****************************** Special tokens **********************************/
 
     IElementType EXCLAMATION        = new CoqSpecialToken("EXCLAMATION");
     IElementType PERCENT            = new CoqSpecialToken("PERCENT");
     IElementType AMPERSAND          = new CoqSpecialToken("AMPERSAND");
     IElementType DOUBLE_AMPERSAND   = new CoqSpecialToken("DOUBLE_AMPERSAND");
     IElementType LEFT_PAREN         = new CoqSpecialToken("LEFT_PAREN");
     IElementType EMPTY_PAREN        = new CoqSpecialToken("EMPTY_PAREN");
     IElementType RIGHT_PAREN        = new CoqSpecialToken("RIGHT_PAREN");
     IElementType ASTERISK           = new CoqSpecialToken("ASTERISK");
     IElementType PLUS               = new CoqSpecialToken("PLUS");
     IElementType DOUBLE_PLUS        = new CoqSpecialToken("DOUBLE_PLUS");
     IElementType COMMA              = new CoqSpecialToken("COMMA");
     IElementType HYPHEN             = new CoqSpecialToken("HYPHEN");
     IElementType RIGHT_SIMPLE_ARROW = new CoqSpecialToken("RIGHT_SIMPLE_ARROW");
     IElementType DOT                = new CoqSpecialToken("DOT");
     IElementType DOT_PAREN          = new CoqSpecialToken("DOT_PAREN");
     IElementType DOUBLE_DOT         = new CoqSpecialToken("DOUBLE_DOT");
     IElementType SLASH              = new CoqSpecialToken("SLASH");
     IElementType AND                = new CoqSpecialToken("AND");
     IElementType COLON              = new CoqSpecialToken("COLON");
     IElementType DOUBLE_COLON       = new CoqSpecialToken("DOUBLE_COLON");
     IElementType INCLUDED           = new CoqSpecialToken("INCLUDED");
     IElementType COLON_EQUAL        = new CoqSpecialToken("COLON_EQUAL");
     IElementType INCLUDES           = new CoqSpecialToken("INCLUDES");
     IElementType SEMICOLON          = new CoqSpecialToken("SEMICOLON");
     IElementType LESS               = new CoqSpecialToken("LESS");
     IElementType LEFT_SIMPLE_ARROW  = new CoqSpecialToken("LEFT_SIMPLE_ARROW");
     IElementType IF_AND_ONLY_IF     = new CoqSpecialToken("IF_AND_ONLY_IF");
     IElementType LESS_COLON         = new CoqSpecialToken("LESS_COLON");
     IElementType LESS_EQUAL         = new CoqSpecialToken("LESS_EQUAL");
     IElementType DISTINCT           = new CoqSpecialToken("DISTINCT");
     IElementType EQUAL              = new CoqSpecialToken("EQUAL");
     IElementType RIGHT_DOUBLE_ARROW = new CoqSpecialToken("RIGHT_DOUBLE_ARROW");
     IElementType HAPPINESS          = new CoqSpecialToken("HAPPINESS");
     IElementType GREATER            = new CoqSpecialToken("GREATER");
     IElementType PATH               = new CoqSpecialToken("PATH");
     IElementType GREATER_EQUAL      = new CoqSpecialToken("GREATER_EQUAL");
     IElementType QUESTION           = new CoqSpecialToken("QUESTION");
     IElementType QUESTION_EQUAL     = new CoqSpecialToken("QUESTION_EQUAL");
     IElementType AT_SIGN            = new CoqSpecialToken("AT_SIGN");
     IElementType LEFT_BRACKET       = new CoqSpecialToken("LEFT_BRACKET");
     IElementType OR                 = new CoqSpecialToken("OR");
     IElementType RIGHT_BRACKET      = new CoqSpecialToken("RIGHT_BRACKET");
     IElementType CARET              = new CoqSpecialToken("CARET");
     IElementType LEFT_BRACE         = new CoqSpecialToken("LEFT_BRACE");
     IElementType PIPE               = new CoqSpecialToken("PIPE");
     IElementType PIPE_HYPHEN        = new CoqSpecialToken("PIPE_HYPHEN");
     IElementType DOUBLE_PIPE        = new CoqSpecialToken("DOUBLE_PIPE");
     IElementType RIGHT_BRACE        = new CoqSpecialToken("RIGHT_BRACE");
     IElementType EQUIVALENCY        = new CoqSpecialToken("EQUIVALENCY");
         
 
     /****************************** Literals ****************************************/
 
     IElementType INTEGER        = new CoqInteger("INTEGER");
     IElementType STRING_LITERAL = new CoqString("STRING_LITERAL");
 
 
     /****************************** Composed tokens *********************************/
 
     IElementType COMMENT = new CoqComment("COMMENT");
     IElementType IDENT   = new CoqIdent("IDENT");
 }
