 //-----------------------------------------------------------------------
 // FILE    : SyntaxViewer.java
 // SUBJECT : Class that allows an abstract syntax tree to be viewed.
 // AUTHOR  : (C) Copyright 2012 by Peter C. Chapin <PChapin@vtc.vsc.edu>
 //
 //-----------------------------------------------------------------------
 package edu.uvm.nesc;
 
 import java.io.PrintStream;
 import java.util.Stack;
 import org.antlr.runtime.tree.*;
 
 /**
  * Provides a nice way to view SpartanRPC abstract syntax trees.
  * @author Peter C. Chapin
  */
 public class SyntaxViewer {
 
     private PrintStream sink;
     private Tree        syntaxTree;
     private int         indentationLevel = 0;
     private boolean     suppressRewriting = false;
 
     // Used to handle parentheses in nested declarators in a nice way. Consider, for example,
     //     int (*p)(int x);
     // In this case the declarator '*p' should be parenthesized, but neither the declarator 'x' nor the overall
     // declarator needs to be. (Note that they could be, but that is ugly).
     //
     private Stack<Integer> declaratorNestingLevels = new Stack<>();
 
     // Used to prevent the top level expression from being parenthesized. For example, normally we have something like
     // this:
     //     ( x = ( a * ( b + c ) ) );
     // If this flag is set to false before rewriting this expression we have instead:
     //       x = ( a * ( b + c ) );
     // This looks a little nicer (especially in, for example, the conditional expressions of if, while, and for loops).
     // Fully "correct" handling of this issue would require the rewriter to consider operator precedence when deciding
     // when to parenthesize.
     //
     private boolean enableExpressionParentheses = true;
 
 
     /**
      * Constructs a SyntaxViewer.
      *
      * @param outputDestination The object into which the output is sent.
      * @param syntax The tree that this viewer will use.
      */
     public SyntaxViewer(PrintStream outputDestination, Tree syntax)
     {
         sink = outputDestination;
         syntaxTree = syntax;
         declaratorNestingLevels.push(0);
     }
 
 
     /**
      * Outputs the entire syntax tree in ANTLR's tree notation. The output is sent to the PrintStream object previously
      * given to the constructor. This method adds a '\n' to the end of the output.
      *
      */
     public void writeAST()
     {
         sink.print(syntaxTree.toStringTree());
         sink.print("\n");
     }
 
 
     /**
      * Indents by an amount related to the current indentation level. This method is used during rewriting to make the
      * output look approximately nice.
      */
     private void indent()
     {
         for (int i = 0; i < indentationLevel; ++i) {
             sink.print("    ");
         }
     }
 
 
     /**
      * Outputs the entire syntax tree in source code form. The output is sent to the PrintStream object previously given
      * to the constructor. This method adds a '\n' to the end of the output.
      *
      */
     public void rewrite()
     {
         rewrite(syntaxTree);
         sink.print("\n");
     }
 
 
     /**
      * Outputs the syntax tree rooted at t in source code form. The output is sent to the PrintStream object previously
      * given to the constructor.
      *
      * @param t The tree to output.
      */
     private void rewrite(Tree t)
     {
         int value;
         int currentChild;  // Used when processing structure or enumeration declarations.
 
         switch (t.getType()) {
             // Putting a space after all occurrences of RAW_IDENTIFIER is overkill. However it is important to include a
             // space after RAW_IDENTIFIERS that are type names (at least when they appear as a declaration specifier in
             // a declaration or function definition). Ideally the rewriter for those constructs would include the extra
             // space when necessary. However, it is easier to just include it here. Aside from making the output look a
             // little funny, the extra space is harmless in other cases.
             //
             case nesCLexer.RAW_IDENTIFIER:
                 sink.print(t.getText());
                 sink.print(" ");
                 break;
 
             // Declarations
             // ------------
 
             // Raw tokens. These tokens just stand for themselves in the AST. These cases could be handled by the
             // default case instead. However, having them here makes it explicit which tokens should be processed in
             // this way. (Eventually the default case should probably be changed to throw an exception of some kind to
             // indicate that an unexpected token was encountered).
             //
             case nesCLexer.ASYNC:
             case nesCLexer.AUTO:
             case nesCLexer.CALL:
             case nesCLexer.CHAR:
             case nesCLexer.COMMAND:
             case nesCLexer.CONST:
             case nesCLexer.EXTERN:
             case nesCLexer.EVENT:
             case nesCLexer.INLINE:
             case nesCLexer.INT:
             case nesCLexer.NORACE:
             case nesCLexer.POST:
             case nesCLexer.REGISTER:
             case nesCLexer.RESTRICT:
             case nesCLexer.SHORT:
             case nesCLexer.SIGNAL:
             case nesCLexer.SIGNED:
             case nesCLexer.STATIC:
             case nesCLexer.TASK:
             case nesCLexer.TYPEDEF:
             case nesCLexer.UNSIGNED:
             case nesCLexer.VOID:
             case nesCLexer.VOLATILE:
                 sink.print(t.getText());
                 sink.print(" ");
                 break;
                 
             case nesCLexer.DECLARATION:
                 indent();
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     rewrite(t.getChild(i));
                     if (t.getChild(i).getType() == nesCLexer.RAW_IDENTIFIER) sink.print(" ");
                 }
                 if (t.getChild(0).getType() != nesCLexer.FUNCTION_DEFINITION) {
                     sink.print(";");
                 }
                 sink.print("\n");
                 break;
 
             case nesCLexer.STRUCT:
             case nesCLexer.UNION:
             case nesCLexer.NX_STRUCT:
             case nesCLexer.NX_UNION:
                 sink.print(t.getText());
                 sink.print(" ");
                 currentChild = 0;
                 if (t.getChild(currentChild).getType() != nesCLexer.DECLARATION) {
                     rewrite(t.getChild(currentChild));
                     sink.print(" ");
                     ++currentChild;
                 }
                 if (currentChild < t.getChildCount()) {
                     sink.print("{\n");
                     ++indentationLevel;
                     while (currentChild < t.getChildCount()) {
                         rewrite(t.getChild(currentChild));
                         ++currentChild;
                     }
                     --indentationLevel;
                     indent();
                     sink.print("} ");
                 }
                 break;
 
             case nesCLexer.ENUM:
                 sink.print("enum ");
                 currentChild = 0;
                 if (t.getChild(currentChild).getType() != nesCLexer.ENUMERATOR) {
                     rewrite(t.getChild(currentChild));
                     sink.print(" ");
                     ++currentChild;
                 }
                 if (currentChild < t.getChildCount()) {
                     sink.print("{ ");
                     boolean firstTime = true;
                     while (currentChild < t.getChildCount()) {
                         if (!firstTime) sink.print(", ");
                         rewrite(t.getChild(currentChild));
                         firstTime = false;
                         ++currentChild;
                     }
                     sink.print("} ");
                 }
                 break;
 
             case nesCLexer.ENUMERATOR:
                 rewrite(t.getChild(0));
                 if (t.getChildCount() > 1) {
                     sink.print(" = ");
                     rewrite(t.getChild(1));
                 }
                 break;
 
             case nesCLexer.DECLARATOR_LIST:
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     if (i != 0) sink.print(", ");
                     rewrite(t.getChild(i));
                 }
                 break;
 
             case nesCLexer.INIT_DECLARATOR:
                 rewrite(t.getChild(0));
                 if (t.getChildCount() > 1) {
                     sink.print(" = ");
                     for (int i = 1; i < t.getChildCount(); ++i) {
                         rewrite(t.getChild(i));
                     }
                 }
                 break;
 
             case nesCLexer.DECLARATOR:
                 if (declaratorNestingLevels.peek() != 0) sink.print("(");
                 value = declaratorNestingLevels.pop();
                 declaratorNestingLevels.push(value + 1);
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     rewrite(t.getChild(i));
                     sink.print(" ");
                 }
                 value = declaratorNestingLevels.pop();
                 declaratorNestingLevels.push(value - 1);
                 if (declaratorNestingLevels.peek() != 0) sink.print(")");
                 break;
 
             case nesCLexer.INITIALIZER_LIST:
                 sink.print("{ ");
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     if (i != 0) sink.print(", ");
                     rewrite(t.getChild(i));
                 }
                 sink.print(" }");
                 break;
 
             case nesCLexer.POINTER_QUALIFIER:
                 sink.print("*");
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     rewrite(t.getChild(i));
                 }
                 break;
 
             case nesCLexer.DECLARATOR_ARRAY_MODIFIER:
                 sink.print("[");
                 if (t.getChildCount() != 0) rewrite(t.getChild(0));
                 sink.print("]");
                 break;
 
             case nesCLexer.DECLARATOR_PARAMETER_LIST_MODIFIER:
                 sink.print("( ");
                 rewrite(t.getChild(0));
                 sink.print(" )");
                 break;
 
             case nesCLexer.PARAMETER_LIST:
                 declaratorNestingLevels.push(0);
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     if (i != 0) sink.print(", ");
                     rewrite(t.getChild(i));
                 }
                 declaratorNestingLevels.pop();
                 break;
 
             case nesCLexer.PARAMETER:
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     rewrite(t.getChild(i));
                     if (t.getChild(i).getType() == nesCLexer.RAW_IDENTIFIER) sink.print(" ");
                 }
                 break;
 
             case nesCLexer.FUNCTION_DEFINITION:
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     if (t.getChild(i).getType() == nesCLexer.COMPOUND_STATEMENT) {
                         sink.print("\n");
                         ++indentationLevel;  // This is a hack.
                     }
                     rewrite(t.getChild(i));
                     if (t.getChild(i).getType() == nesCLexer.COMPOUND_STATEMENT) {
                         --indentationLevel;
                     }
                 }
                 break;
 
             // Statements
             // ----------
                 
             // Expression statements are handled here.
             case nesCLexer.STATEMENT:
                 indent();
                 enableExpressionParentheses = false;
                 if (t.getChildCount() == 1) rewrite(t.getChild(0));
                 enableExpressionParentheses = true;
                 sink.print(";\n");
                 break;
 
             case nesCLexer.COMPOUND_STATEMENT:
                 // Outdent the braces.
                 --indentationLevel; indent(); sink.print("{\n"); ++indentationLevel;
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     rewrite(t.getChild(i));
                 }
                 --indentationLevel; indent(); sink.print("}\n"); ++indentationLevel;
                 break;
 
             case nesCLexer.LABELED_STATEMENT:
                 // Outdent the label.
                 --indentationLevel; indent(); sink.print(t.getChild(0)); ++indentationLevel;
                 sink.print(":\n");
                 rewrite(t.getChild(1));
                 break;
 
             case nesCLexer.CASE:
                 // Outdent the case label.
                 --indentationLevel;
                 indent();
                 sink.print("case ");
                 rewrite(t.getChild(0));
                 sink.print(":\n");
                 ++indentationLevel;
                 rewrite(t.getChild(1));
                 break;
 
             // This handles 'default' in switch statements, but not in declarations.
             case nesCLexer.DEFAULT:
                 // Outdent the default label.
                 --indentationLevel; indent(); sink.print("default:\n"); ++indentationLevel;
                 rewrite(t.getChild(0));
 
             case nesCLexer.ATOMIC:
                 indent();
                 sink.print("atomic\n");
                 ++indentationLevel;
                 rewrite(t.getChild(0));
                 --indentationLevel;
                 break;
 
             case nesCLexer.IF:
                 indent();
                 sink.print("if( ");
                 enableExpressionParentheses = false;
                 rewrite(t.getChild(0));
                 enableExpressionParentheses = true;
                 sink.print(" )\n");
                 ++indentationLevel;
                 rewrite(t.getChild(1));
                 --indentationLevel;
                 if (t.getChildCount() == 3) {
                     indent();
                     sink.print("else\n");
                     ++indentationLevel;
                     rewrite(t.getChild(2));
                     --indentationLevel;
                 }
                 break;
 
             case nesCLexer.SWITCH:
                 indent();
                 sink.print("switch( ");
                 enableExpressionParentheses = false;
                 rewrite(t.getChild(0));
                 enableExpressionParentheses = true;
                 sink.print(" )\n");
                 ++indentationLevel;
                 rewrite(t.getChild(1));
                 --indentationLevel;
                 break;
                 
             case nesCLexer.WHILE:
                 indent();
                 sink.print("while( ");
                 enableExpressionParentheses = false;
                 rewrite(t.getChild(0));
                 enableExpressionParentheses = true;
                 sink.print(" )\n");
                 ++indentationLevel;
                 rewrite(t.getChild(1));
                 --indentationLevel;
                 break;
                 
             case nesCLexer.DO:
                 indent();
                 sink.print("do\n");
                 ++indentationLevel;
                 rewrite(t.getChild(0));
                 --indentationLevel;
                 indent();
                 sink.print("while( ");
                 enableExpressionParentheses = false;
                 rewrite(t.getChild(1));
                 enableExpressionParentheses = true;
                 sink.print(");\n");
                 break;
                 
             case nesCLexer.FOR:
                 indent();
                 sink.print("for( ");
                 // The loop header.
                 for (int i = 0; i < 3; ++i) {
                     if (i != 0) sink.print("; ");
                     rewrite(t.getChild(i));
                 }
                 sink.print(" )\n");
                 ++indentationLevel;
                 rewrite(t.getChild(3));  // The controlled statement.
                 --indentationLevel;
                 break;
                 
             case nesCLexer.FOR_INITIALIZE:
                 enableExpressionParentheses = false;
                 if (t.getChildCount() == 1) rewrite(t.getChild(0));
                 enableExpressionParentheses = true;
                 break;
                 
             case nesCLexer.FOR_CONDITION:
                 enableExpressionParentheses = false;
                 if (t.getChildCount() == 1) rewrite(t.getChild(0));
                 enableExpressionParentheses = true;
                 break;
                 
             case nesCLexer.FOR_ITERATION:
                 enableExpressionParentheses = false;
                 if (t.getChildCount() == 1) rewrite(t.getChild(0));
                 enableExpressionParentheses = true;
                 break;
                 
             case nesCLexer.GOTO:
                 indent();
                 sink.print("goto ");
                 rewrite(t.getChild(1));
                 sink.print(";\n");
                 break;
                 
             case nesCLexer.CONTINUE:
                 indent();
                 sink.print("continue;\n");
                 break;
                 
             case nesCLexer.BREAK:
                 indent();
                 sink.print("break;\n");
                 break;
                 
             case nesCLexer.RETURN:
                 indent();
                 sink.print("return ");
                 if (t.getChildCount() == 1) rewrite(t.getChild(0));
                 sink.print(";\n");
                 break;
                 
             // Expressions
             // -----------
 
             // All binary infix operators are handled the same way.
             case nesCLexer.AMP:
             case nesCLexer.AND:
             case nesCLexer.ASSIGN:
             case nesCLexer.BITANDASSIGN:
             case nesCLexer.BITOR:
             case nesCLexer.BITORASSIGN:
             case nesCLexer.BITXOR:
             case nesCLexer.BITXORASSIGN:
             case nesCLexer.COMMA:
             case nesCLexer.DIVASSIGN:
             case nesCLexer.DIVIDE:
             case nesCLexer.EQUAL:
             case nesCLexer.GREATER:
             case nesCLexer.GREATEREQUAL:
             case nesCLexer.LESS:
             case nesCLexer.LESSEQUAL:
             case nesCLexer.LSHIFT:
             case nesCLexer.LSHIFTASSIGN:
             case nesCLexer.MINUS:
             case nesCLexer.MINUSASSIGN:
             case nesCLexer.MODULUS:
             case nesCLexer.MODASSIGN:
             case nesCLexer.NOTEQUAL:
             case nesCLexer.OR:
             case nesCLexer.PLUS:
             case nesCLexer.PLUSASSIGN:
             case nesCLexer.RSHIFT:
             case nesCLexer.RSHIFTASSIGN:
             case nesCLexer.STAR:
                 boolean oldExpressionParentheses = enableExpressionParentheses;
                 if (enableExpressionParentheses) sink.print("( ");
                 enableExpressionParentheses = true;
                 rewrite(t.getChild(0));    // The left operand.
                 sink.print(" ");
                 sink.print(t.getText());   // The operator itself.
                 sink.print(" ");
                 rewrite(t.getChild(1));    // The right operand.
                 enableExpressionParentheses = oldExpressionParentheses;
                 if (enableExpressionParentheses) sink.print(" )");
                 break;
 
             case nesCLexer.POSTFIX_EXPRESSION:
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     rewrite(t.getChild(i));
                 }
                 break;
 
             case nesCLexer.BUILTIN_VA_ARG:
                 sink.print("__builtin_va_arg(");
                 rewrite(t.getChild(0));
                 sink.print(", ");
                 rewrite(t.getChild(1));
                 sink.print(")");
                 break;
                 
             case nesCLexer.ARGUMENT_LIST:
                 sink.print("( ");
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     if (i != 0) sink.print(", ");
                     rewrite(t.getChild(i));
                 }
                 sink.print(" )");
                 break;
 
             case nesCLexer.ARRAY_ELEMENT_SELECTION:
                 sink.print("[");
                 rewrite(t.getChild(0));
                 sink.print("]");
                 break;
 
             case nesCLexer.DOT:
                 sink.print(".");
                 rewrite(t.getChild(0));
                 break;
 
             case nesCLexer.ARROW:
                 sink.print("->");
                 rewrite(t.getChild(0));
                 break;
 
             case nesCLexer.PLUSPLUS:
                 sink.print("++");
                 break;
 
             case nesCLexer.MINUSMINUS:
                 sink.print("--");
                 break;
 
             case nesCLexer.PRE_INCREMENT:
                 sink.print("++");
                 rewrite(t.getChild(0));
                 break;
 
             case nesCLexer.PRE_DECREMENT:
                 sink.print("--");
                 rewrite(t.getChild(0));
                 break;
 
             case nesCLexer.ADDRESS_OF:
                 sink.print("&");
                 rewrite(t.getChild(0));
                 break;
 
             case nesCLexer.UNARY_PLUS:
                 sink.print("+");
                 rewrite(t.getChild(0));
                 break;
 
             case nesCLexer.UNARY_MINUS:
                 sink.print("-");
                 rewrite(t.getChild(0));
                 break;
 
             // Perhaps the AST does not need to distinguish between these cases.
             case nesCLexer.SIZEOF_TYPE:
             case nesCLexer.SIZEOF_EXPRESSION:
                 sink.print("sizeof( ");
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     rewrite(t.getChild(i));
                 }
                 sink.print(" )");
                 break;
 
             case nesCLexer.CAST:
                 sink.print("(");
                 for (int i = 1; i < t.getChildCount(); ++i) {
                     rewrite(t.getChild(i));
                 }
                 sink.print(")( ");
                 rewrite(t.getChild(0));
                 sink.print(" )");
                 break;
 
             case nesCLexer.BITCOMPLEMENT:
                 sink.print("~");
                 rewrite(t.getChild(0));
                 break;
 
             case nesCLexer.NOT:
                 sink.print("!");
                 rewrite(t.getChild(0));
                 break;
 
             // The AST distinguishes this use of '*' from multiplication. How nice.
             case nesCLexer.DEREFERENCE:
                 sink.print("( *");
                 rewrite(t.getChild(0));
                 sink.print(" )");
                 break;
 
             // Large Scale Structure
             // ---------------------
 
             case nesCLexer.FILE:
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     if (!suppressRewriting || t.getChild(i).getType() == nesCLexer.LINE_DIRECTIVE)
                         rewrite(t.getChild(i));
                 }
                 break;
 
             // This is a simple hack. Emit a #include for line directives that reference header files. If #includes were
             // nested this will cause spurious #include directives to be emitted here. However, that should not cause
             // any problems for the back end nesC compiler. Note that the AST for material in a header file should not
             // be rewritten. However, when a line directive for the main .nc file is encountered, rewriting should be
             // turned on again.
             //
             case nesCLexer.LINE_DIRECTIVE:
                 String fileNameWithQuotes = t.getChild(0).getText();
                 if (fileNameWithQuotes.endsWith(".h\"") || fileNameWithQuotes.endsWith(".h>")) {
 
                     // We always output Unix style names. Even on Windows, the program will be compiled under Cygwin.
                     fileNameWithQuotes = fileNameWithQuotes.replace("\\\\", "/");
 
                     // // TODO: Generalize to produce Cygwin paths on Windows and normal paths on Unix.
                     // if (fileNameWithQuotes.startsWith("\"/cygwin")) {
                     //     fileNameWithQuotes = "\"" + fileNameWithQuotes.substring(8);
                     // }
                     // // I assume absolute paths need to be replaced for Cygwin. This will be false on Unix systems.
                     // else if (fileNameWithQuotes.startsWith("\"/")) {
                     //     fileNameWithQuotes = "\"/cygdrive/c" + fileNameWithQuotes.substring(1);
                     // }
 
                     // Output the reconstructed #include directive.
                     sink.print("#include " + fileNameWithQuotes + "\n");
                     suppressRewriting = true;
                 }
                 else {
                     suppressRewriting = false;
                 }
                 break;
 
             case nesCLexer.INTERFACE:
                 // This is correct for 'interface' as it appears in specifications.
                 if (t.getChild(0).getType() == nesCLexer.INTERFACE_TYPE) {
                     indent();
                     sink.print("interface ");
                     rewrite(t.getChild(0));
                     if (t.getChildCount() > 1) {
                         sink.print(" as ");
                         rewrite(t.getChild(1));
                     }
                     sink.print(";\n");
                 }
                 // Otherwise we are defining an interface type.
                 else {
                     sink.print("interface ");
                     sink.print(t.getChild(0));
                     sink.print(" {\n");
                     ++indentationLevel;
                     for (int i = 1; i < t.getChildCount(); ++i) {
                         rewrite(t.getChild(i));
                     }
                     --indentationLevel;
                     sink.print("}\n");
                 }
                 break;
 
             case nesCLexer.COMPONENT_DEFINITION:
                 rewrite(t.getChild(0));  // The kind of component.
                 rewrite(t.getChild(1));  // Name of the configuration.
 
                 // Component parameters, if present.
                 Tree componentParameters = null;
                 if (t.getChildCount() == 5) {
                     componentParameters = t.getChild(4);
                 }
                 else if (t.getChildCount() == 4 && t.getChild(3).getType() == nesCLexer.COMPONENT_PARAMETER_LIST) {
                     componentParameters = t.getChild(3);
                 }
                 if (componentParameters != null) rewrite(componentParameters);
 
                 rewrite(t.getChild(2));  // Specification.
 
                 // Implementation, if present.
                 if (t.getChildCount() >= 4 && t.getChild(3).getType() == nesCLexer.IMPLEMENTATION) {
                     rewrite(t.getChild(3));
                 }
                 break;
 
             case nesCLexer.COMPONENT_KIND:
                 switch (t.getChild(0).getType()) {
                     case nesCLexer.CONFIGURATION:
                         sink.print("configuration ");
                         break;
                     case nesCLexer.MODULE:
                         sink.print("module ");
                         break;
                     case nesCLexer.GENERIC:
                         switch (t.getChild(1).getType()) {
                             case nesCLexer.CONFIGURATION:
                                 sink.print("generic configuration ");
                                 break;
                             case nesCLexer.MODULE:
                                 sink.print("generic module ");
                                 break;
                         }
                         break;
                 }
                 break;
 
             case nesCLexer.COMPONENT_PARAMETER_LIST:
                 sink.print("(");
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     if (i > 0) sink.print(", ");
                     rewrite(t.getChild(i));
                 }
                 sink.print(") ");
                 break;
 
             case nesCLexer.SPECIFICATION:
                 sink.print(" {\n");
                 ++indentationLevel;
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     rewrite(t.getChild(i));
                 }
                 --indentationLevel;
                 sink.print("}\n");
                 break;
 
             case nesCLexer.USES:
                 indent();
                 sink.print("uses {\n");
                 ++indentationLevel;
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     rewrite(t.getChild(i));
                 }
                 --indentationLevel;
                 indent();
                 sink.print("}\n");
                 break;
 
             case nesCLexer.PROVIDES:
                 indent();
                 sink.print("provides {\n");
                 ++indentationLevel;
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     rewrite(t.getChild(i));
                 }
                 --indentationLevel;
                 indent();
                 sink.print("}\n");
                 break;
 
             case nesCLexer.INTERFACE_TYPE:
                 // Assume there is no 'remotable' or 'with' parts left in the AST.
                 sink.print(t.getChild(0));
                 if (t.getChildCount() > 1) {
                     sink.print("<");
                     for (int i = 1; i < t.getChildCount(); ++i) {
                         if (i != 1) sink.print(", ");
                         rewrite(t.getChild(i));
                     }
                     sink.print(">");
                 }
                 break;
 
             case nesCLexer.IMPLEMENTATION:
                 sink.print("implementation {\n");
                 ++indentationLevel;
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     if (!suppressRewriting || t.getChild(i).getType() == nesCLexer.LINE_DIRECTIVE)
                         rewrite(t.getChild(i));
                 }
                 --indentationLevel;
                 sink.print("}\n");
                 break;
 
             // Configuration Implementation
             // ----------------------------
 
             case nesCLexer.COMPONENTS:
                 indent();
                 sink.print("components ");
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     if (i != 0) sink.print(", ");
                     rewrite(t.getChild(i));
                 }
                 sink.print(";\n");
                 break;
 
             case nesCLexer.COMPONENT_DECLARATION:
                 rewrite(t.getChild(0));      // The component_ref.
                 if (t.getChildCount() == 2) {
                     sink.print(" as ");
                     rewrite(t.getChild(1));  // The component's alias (if there is one).
                 }
                 break;
 
             case nesCLexer.COMPONENT_INSTANTIATION:
                 sink.print("new ");
                 rewrite(t.getChild(0));
                 sink.print("( ");
                 if (t.getChildCount() > 1) {
                     rewrite(t.getChild(1));
                 }
                 sink.print(" )");
                 break;
 
             case nesCLexer.COMPONENT_ARGUMENTS:
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     if (i != 0) sink.print(", ");
                     rewrite(t.getChild(i));
                 }
                 break;
 
             case nesCLexer.CONNECTION:
                 indent();
                 rewrite(t.getChild(1));
                 sink.print(" ");
                 sink.print(t.getChild(0).getText());
                 sink.print(" ");
                rewrite(t.getChild(0).getChild(0));
                 sink.print(";\n");
                 break;
 
             case nesCLexer.IDENTIFIER_PATH:
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     if (i != 0) sink.print(".");
                     rewrite(t.getChild(i));
                 }
                 break;
 
             // The NULL token should probably not appear in the trees given to this method. It is intended to be used as
             // a placeholder by other programs that manipulate trees (such as Sprocket). The idea is that all NULL
             // tokens would be removed from the tree before rewriting. However, in case a NULL token does remain, the
             // code below does the most natural thing with it.
             //
             case nesCLexer.NULL:
                 for (int i = 0; i < t.getChildCount(); ++i) {
                     rewrite(t.getChild(i));
                 }
                 break;
 
             // Is it right to have this default case? It is useful during development because it shows the "next" token
             // that needs to be implemented in order to get a proper rewriting.
             //
             default:
                 sink.print(t.getText());
                 sink.print(" ");
                 break;
         }
     }
 }
