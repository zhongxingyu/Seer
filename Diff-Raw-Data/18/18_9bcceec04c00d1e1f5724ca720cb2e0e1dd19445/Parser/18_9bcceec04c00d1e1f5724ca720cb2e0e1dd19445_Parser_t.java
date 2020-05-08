 public class Parser {
 
     // need a symbol table
     private Symtab symtab = new Symtab();
 
     // the first sets.
     // note: we cheat sometimes:
     // when there is only a single token in the set,
     // we generally just compare tkrep with the first token.
     TK f_declaration[] = {TK.VAR, TK.CONST, TK.none};
     TK f_var_decl[] = {TK.VAR, TK.none};
     TK f_const_decl[] = {TK.CONST, TK.none};
     TK f_statement[] = {TK.ID, TK.PRINT, TK.IF, TK.WHILE, TK.FOR, TK.REPEAT, TK.none};
     TK f_print[] = {TK.PRINT, TK.none};
     TK f_assignment[] = {TK.ID, TK.none};
     TK f_if[] = {TK.IF, TK.none};
     TK f_while[] = {TK.WHILE, TK.none};
     TK f_for[] = {TK.FOR, TK.none};
     TK f_expression[] = {TK.ID, TK.NUM, TK.LPAREN, TK.none};
     TK f_str[] = {TK.STR, TK.none};
     TK f_repeat[] = {TK.REPEAT, TK.none};
 
     // tok is global to all these parsing methods;
     // scan just calls the scanner's scan method and saves the result in tok.
     private Token tok; // the current token
     private void scan() {
         tok = scanner.scan();
     }
 
     private Scan scanner;
     private boolean bc;
     Parser(Scan scanner) {
         this.scanner = scanner;
         bc = false;
         scan();
         program();
         if( tok.kind != TK.EOF )
             parse_error("junk after logical end of program");
     }
 
     // for code generation
     private static final int initialValueEVariable = 8888;
     private static final int initialValueEVariableArr = 4444;
     
     // print something in the generated code
     private void gcprint(String str) {
         System.out.println(str);
     }
     // print identifier in the generated code
     // it prefixes x_ in case id conflicts with C keyword.
     private void gcprintid(String str) {
         System.out.println("x_"+str);
     }
 
     private void program() {
         gcprint("#include <stdio.h>");
         gcprint("#include <stdlib.h>");
         gcprint("main() ");
         block();
         if(bc) {
         	bcGen();
         }
     }
 
     private void block() {
         gcprint("{");
         symtab.begin_st_block();
         while( first(f_declaration) ) {
             declaration();
         }
         while( first(f_statement) ) {
             statement();
         }
         symtab.end_st_block();
         gcprint("}");
      }
     
     private void bcGen() {
    	gcprint("int bc(int x, int size, int line, char* str)");
     	gcprint("{");
     	gcprint("if(x<size && x>=0) {");
     	gcprint("return x;");
     	gcprint("} else {");
    	gcprint("fprintf(stderr, \"Out of bounds access of array '%s' at line %d\\n\", str, line);");
     	gcprint("exit(1);");
     	gcprint("}");
     	gcprint("}");
     }
 
     private void declaration() {
         if (first(f_var_decl)) {
             var_decl();
         }
         else if (first(f_const_decl)) {
             const_decl();
         }
         else
             parse_error("oops -- declaration bad first");
     }
 
     private void var_decl() {
         mustbe(TK.VAR);
         var_decl_id();
         while( is(TK.COMMA) ) {
             scan();
             var_decl_id();
         }
     }
 
     private void var_decl_id() {
 		if (is(TK.ID)) {
 			if (symtab.add_entry(tok.string, tok.lineNumber, TK.VAR)) {
 				gcprint("int ");
 				if (tok.arr) {
 					arr_dec(false);
 				}
                 else {
                     gcprintid(tok.string);
                     gcprint("="+initialValueEVariable+";");
                 }
             }
 			else if (tok.arr) {
 				arr_dec(true);
 			}
             scan();
         }
         else {
             parse_error("expected id in var declaration, got " + tok);
         }
 	}
     
     private void arr_dec(boolean skip) {
 		if (skip) {
 			scan();
 			bound();
 			mustbe(TK.COL);
 			bound();
 			if (!(is(TK.ENDARR)))
 				parse_error("expected closing bracket!");
 		}
 		else {
 			String varID = tok.string;
 			int lbound, ubound, arrSize;
 			scan();
 			lbound = bound();
 			mustbe(TK.COL);
 			ubound = bound();
 			if (is(TK.ENDARR)) {
 				arrSize = ubound - lbound + 1;
 				if (arrSize < 1) {
 					parse_error("size of the array must be positive!");
 				}
 				symtab.edit_array(varID, lbound, ubound, tok.lineNumber);
 
 				gcprintid(varID + "[" + arrSize + "]");
 				gcprint("= {" + initialValueEVariableArr + ",");
 				for (int i = 1; i < arrSize; i++)
 					gcprint(initialValueEVariableArr + ",");
 				gcprint("};");
 			} else {
 				parse_error("expected closing bracket!");
 			}
 		}
     }
 
 	private int bound() {
 		int my_bound = 0;
 		if (is(TK.MINUS)) {
 			scan();
 			my_bound = 0 - Integer.parseInt(tok.string);
 		} else if (is(TK.NUM)) {
 			my_bound = Integer.parseInt(tok.string);
 		}
 		mustbe(TK.NUM);
 		return my_bound;
 	}
 
     private void const_decl() {
         mustbe(TK.CONST);
         boolean newConst = const_decl_id();
         mustbe(TK.EQ);
         if (newConst) {
             gcprint("=");
             gcprint(tok.string);
             gcprint(";");
         }
         mustbe(TK.NUM);
     }
 
     private boolean const_decl_id() {
         if( is(TK.ID) ) {
             boolean ret;
             if (ret = symtab.add_entry(tok.string, tok.lineNumber, TK.CONST)) {
                 gcprint("int ");
                 gcprintid(tok.string);
             }
             scan();
             return ret;
         }
         else {
             parse_error("expected id in const declaration, got " + tok);
             return false; // meaningless since parse_error doesn't return
         }
     }
 
     private void statement(){
         if( first(f_assignment) )
             assignment();
         else if( first(f_print) )
             print();
         else if( first(f_if) )
             ifproc();
         else if( first(f_while) )
             whileproc();
         else if( first(f_for) )
             forproc();
         else if ( first(f_repeat))
             repeatproc();
         else
             parse_error("oops -- statement bad first");
     }
 
     private void assignment(){
         if( is(TK.ID) ) {
             Entry e = lvalue_id(tok.string, tok.lineNumber);
             array(e);
             scan();
         }
         else {
             parse_error("missing id on left-hand-side of assignment");
         }
         mustbe(TK.ASSIGN);
         gcprint("=");
         expression();
         gcprint(";");
     }
 
     private void print(){
         mustbe(TK.PRINT);
         if (first(f_str)) {
             gcprint("printf(\"" + tok.string + "\\n\");");
             scan();
         }
         else if (first(f_expression)) {
             gcprint("printf(\"%d\\n\", ");
             expression();
             gcprint(");");
         }
         else {
             parse_error("Print statement incorrect.");
         }
     }
     
     private void repeatproc() {
         mustbe(TK.REPEAT);
         gcprint("do");
         block();
         mustbe(TK.UNTIL);
         gcprint("while(!(");
         expression();
         gcprint("));");
     }
 
     private void ifproc(){
         mustbe(TK.IF);
         gcprint("if(");
         expression();
         gcprint(")");
         mustbe(TK.THEN);
         block();
         while( is(TK.ELSIF) ) {
             scan();
             gcprint("else if(");
             expression();
             gcprint(")");
             mustbe(TK.THEN);
             block();
         }
         if( is(TK.ELSE) ) {
             scan();
             gcprint("else");
             block();
         }
         mustbe(TK.END);
     }
 
     private void whileproc(){
         mustbe(TK.WHILE);
         gcprint("while(");
         expression();
         gcprint(")");
         mustbe(TK.DO);
         block();
         mustbe(TK.END);
     }
 
     private void forproc(){
         mustbe(TK.FOR);
         gcprint("for(");
         String id = tok.string;
         Entry iv = null; // index variable in symtab
         if( is(TK.ID) ) {
             iv = lvalue_id(tok.string, tok.lineNumber);
             if(iv.arrSize>0) {
             	parse_error("array can't be index variable");
             }
             iv.setIsIV(true); // mark Entry as IV
             scan();
         }
         else {
             parse_error("missing id on left-hand-side of assignment in for");
         }
         mustbe(TK.ASSIGN);
         gcprint("=");
         expression();
         gcprint(";");
         boolean up = true;
         if( is(TK.TO) ) {
             up = true;
         }
         else if( is(TK.DOWNTO) ) {
             up = false;
         }
         else
             parse_error("for statement is missing to/downto");
         scan();
         gcprintid(id);
         gcprint(up?"<=":">=");
         expression();
         mustbe(TK.DO);
         gcprint(";");
         gcprintid(id);
         gcprint(up?"++)":"--)");
         block();
         mustbe(TK.END);
         iv.setIsIV(false); // mark Entry as no longer IV
     }
 
     private void expression(){
         simple();
         while( is(TK.EQ) || is(TK.LT) || is(TK.GT) ||
                is(TK.NE) || is(TK.LE) || is(TK.GE)) {
             if( is(TK.EQ) ) gcprint("==");
             else if( is(TK.NE) ) gcprint("!=");
             else gcprint(tok.string);
             scan();
             simple();
         }
     }
 
     private void simple(){
         term();
         while( is(TK.PLUS) || is(TK.MINUS) ) {
             gcprint(tok.string);
             scan();
             term();
         }
     }
 
     private void term(){
         factor();
         while(  is(TK.TIMES) || is(TK.DIVIDE) ) {
             gcprint(tok.string);
             scan();
             factor();
         }
     }
 
     private void factor(){
         if( is(TK.LPAREN) ) {
             gcprint("(");
             scan();
             expression();
             mustbe(TK.RPAREN);
             gcprint(")");
         }
         else if( is(TK.ID) ) {
             Entry e = rvalue_id(tok.string, tok.lineNumber);
             array(e);
             scan();
         }
         else if( is(TK.NUM) ) {
             gcprint(tok.string);
             scan();
         }
         else
             parse_error("factor");
     }
     
     private void array(Entry e) {
     	if (e.arrSize > 0 && tok.arr) {
     		bc = true;
 			gcprint("[");
 			gcprint("bc(");
 			gcprint("-("+ e.lbound +")+");
 			scan();
 			expression();
 			if (is(TK.ENDARR)) {
 				gcprint(",");
 				gcprint(e.arrSize+"");
				gcprint(",");
				gcprint(tok.lineNumber+"");
				gcprint(",");
				gcprint("\"" + e.varName + "\"");
 				gcprint(")");
 				gcprint("]");
 			} else {
 				parse_error("forgot closed bracket in array assignment");
 			}
 		} else if (tok.arr) {
 			parse_error("using id as an array, but never was declared as an array");
 		} else if (e.arrSize > 0) {
 			parse_error("declared id as an array, but not referencing as an array");
 		}
     }
 
     private Entry lvalue_id(String id, int lno) {
         Entry e = symtab.search(id);
         if( e == null) {
             System.err.println("undeclared variable "+ id + " on line "
                                + lno);
             System.exit(1);
         }
         if( !e.isVar()) {
             System.err.println("constant on left-hand-side of assignment "+ id + " on line "
                                + lno);
             System.exit(1);
         }
         if( e.getIsIV()) {
             System.err.println("index variable on left-hand-side of assignment "+ id + " on line "
                                + lno);
             System.exit(1);
         }
         gcprintid(id);
         return e;
     }
 
     private Entry rvalue_id(String id, int lno) {
         Entry e = symtab.search(id);
         if( e == null) {
             System.err.println("undeclared variable "+ id + " on line "
                                + lno);
             System.exit(1);
         }
         gcprintid(id);
         return e;
     }
 
 
     // is current token what we want?
     private boolean is(TK tk) {
         return tk == tok.kind;
     }
 
     // ensure current token is tk and skip over it.
     private void mustbe(TK tk) {
         if( ! is(tk) ) {
             System.err.println( "mustbe: want " + tk + ", got " +
                                     tok);
             parse_error( "missing token (mustbe)" );
         }
         scan();
     }
     boolean first(TK [] set) {
         int k = 0;
         while(set[k] != TK.none && set[k] != tok.kind) {
             k++;
         }
         return set[k] != TK.none;
     }
 
     private void parse_error(String msg) {
         System.err.println( "can't parse: line "
                             + tok.lineNumber + " " + msg );
         System.exit(1);
     }
 }
