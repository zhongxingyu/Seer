 package descent.internal.compiler.parser;
 
 import static descent.internal.compiler.parser.DYNCAST.DYNCAST_DSYMBOL;
 import static descent.internal.compiler.parser.DYNCAST.DYNCAST_EXPRESSION;
 import static descent.internal.compiler.parser.DYNCAST.DYNCAST_TUPLE;
 import static descent.internal.compiler.parser.DYNCAST.DYNCAST_TYPE;
 import static descent.internal.compiler.parser.LINK.LINKd;
 import static descent.internal.compiler.parser.MATCH.MATCHconst;
 import static descent.internal.compiler.parser.MATCH.MATCHexact;
 import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
 import static descent.internal.compiler.parser.PROT.PROTpackage;
 import static descent.internal.compiler.parser.PROT.PROTprivate;
 import static descent.internal.compiler.parser.PROT.PROTprotected;
 import static descent.internal.compiler.parser.STC.STClazy;
 import static descent.internal.compiler.parser.STC.STCmanifest;
 import static descent.internal.compiler.parser.STC.STCout;
 import static descent.internal.compiler.parser.STC.STCref;
 import static descent.internal.compiler.parser.TOK.TOKarray;
 import static descent.internal.compiler.parser.TOK.TOKassocarrayliteral;
 import static descent.internal.compiler.parser.TOK.TOKblit;
 import static descent.internal.compiler.parser.TOK.TOKconstruct;
 import static descent.internal.compiler.parser.TOK.TOKdefault;
 import static descent.internal.compiler.parser.TOK.TOKdelegate;
 import static descent.internal.compiler.parser.TOK.TOKdotexp;
 import static descent.internal.compiler.parser.TOK.TOKfloat64;
 import static descent.internal.compiler.parser.TOK.TOKforeach_reverse;
 import static descent.internal.compiler.parser.TOK.TOKfunction;
 import static descent.internal.compiler.parser.TOK.TOKnull;
 import static descent.internal.compiler.parser.TOK.TOKslice;
 import static descent.internal.compiler.parser.TOK.TOKstring;
 import static descent.internal.compiler.parser.TOK.TOKsuper;
 import static descent.internal.compiler.parser.TOK.TOKsymoff;
 import static descent.internal.compiler.parser.TOK.TOKtuple;
 import static descent.internal.compiler.parser.TOK.TOKtype;
 import static descent.internal.compiler.parser.TOK.TOKvar;
 import static descent.internal.compiler.parser.TY.Tarray;
 import static descent.internal.compiler.parser.TY.Tbit;
 import static descent.internal.compiler.parser.TY.Tclass;
 import static descent.internal.compiler.parser.TY.Tdelegate;
 import static descent.internal.compiler.parser.TY.Terror;
 import static descent.internal.compiler.parser.TY.Tfunction;
 import static descent.internal.compiler.parser.TY.Tident;
 import static descent.internal.compiler.parser.TY.Tpointer;
 import static descent.internal.compiler.parser.TY.Tsarray;
 import static descent.internal.compiler.parser.TY.Tstruct;
 import static descent.internal.compiler.parser.TY.Ttuple;
 import static descent.internal.compiler.parser.TY.Tvoid;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 
 import descent.core.compiler.CharOperation;
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.parser.ast.ASTNode;
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 // class Object in DMD compiler
 
 public abstract class ASTDmdNode extends ASTNode {
 
 	public final static int COST_MAX = 250;
 
 	public final static int WANTflags = 1;
 	public final static int WANTvalue = 2;
 	public final static int WANTinterpret = 4;
 
 	public final static int ARGUMENT = 1;
 	public final static int CATCH = 2;
 	public final static int ALIGN_DECLARATION = 3;
 	public final static int ANON_DECLARATION = 4;
 	public final static int COMPILE_DECLARATION = 5;
 	public final static int CONDITIONAL_DECLARATION = 6;
 	public final static int LINK_DECLARATION = 7;
 	public final static int PRAGMA_DECLARATION = 8;
 	public final static int DEBUG_SYMBOL = 9;
 	public final static int ALIAS_DECLARATION = 10;
 	public final static int FUNC_DECLARATION = 11;
 	public final static int CTOR_DECLARATION = 12;
 	public final static int DELETE_DECLARATION = 13;
 	public final static int DTOR_DECLARATION = 14;
 	public final static int FUNC_LITERAL_DECLARATION = 15;
 	public final static int INVARIANT_DECLARATION = 16;
 	public final static int NEW_DECLARATION = 17;
 	public final static int STATIC_CTOR_DECLARATION = 18;
 	public final static int STATIC_DTOR_DECLARATION = 19;
 	public final static int UNIT_TEST_DECLARATION = 20;
 	public final static int PROT_DECLARATION = 21;
 	public final static int STORAGE_CLASS_DECLARATION = 22;
 	public final static int BASE_CLASS = 23;
 	public final static int TYPEDEF_DECLARATION = 24;
 	public final static int VAR_DECLARATION = 25;
 	public final static int ENUM_MEMBER = 26;
 	public final static int IMPORT = 27;
 	public final static int MODIFIER_DECLARATION = 28;
 	public final static int MULTI_IMPORT = 29;
 	public final static int CLASS_DECLARATION = 30;
 	public final static int INTERFACE_DECLARATION = 31;
 	public final static int STRUCT_DECLARATION = 32;
 	public final static int UNION_DECLARATION = 33;
 	public final static int ENUM_DECLARATION = 34;
 	public final static int MODULE = 35;
 	public final static int TEMPLATE_DECLARATION = 36;
 	public final static int TEMPLATE_INSTANCE = 37;
 	public final static int TEMPLATE_MIXIN = 38;
 	public final static int STATIC_ASSERT = 39;
 	public final static int VERSION = 40;
 	public final static int VERSION_SYMBOL = 41;
 	public final static int ARRAY_LITERAL_EXP = 42;
 	public final static int ADD_ASSIGN_EXP = 43;
 	public final static int INCREMENT_EXP = 44;
 	public final static int ADD_EXP = 45;
 	public final static int AND_AND_EXP = 46;
 	public final static int AND_ASSIGN_EXP = 47;
 	public final static int AND_EXP = 48;
 	public final static int ASSIGN_EXP = 49;
 	public final static int CAT_ASSIGN_EXP = 50;
 	public final static int CAT_EXP = 51;
 	public final static int CMP_EXP = 52;
 	public final static int COMMA_EXP = 53;
 	public final static int COND_EXP = 54;
 	public final static int DIV_ASSIGN_EXP = 55;
 	public final static int DIV_EXP = 56;
 	public final static int EQUAL_EXP = 57;
 	public final static int IDENTITY_EXP = 58;
 	public final static int IN_EXP = 59;
 	public final static int MIN_ASSIGN_EXP = 60;
 	public final static int DECREMENT_EXP = 61;
 	public final static int MIN_EXP = 62;
 	public final static int MOD_ASSIGN_EXP = 63;
 	public final static int MOD_EXP = 64;
 	public final static int MUL_ASSIGN_EXP = 65;
 	public final static int MUL_EXP = 66;
 	public final static int OR_ASSIGN_EXP = 67;
 	public final static int OR_EXP = 68;
 	public final static int OR_OR_EXP = 69;
 	public final static int POST_EXP = 70;
 	public final static int SHL_ASSIGN_EXP = 71;
 	public final static int SHL_EXP = 72;
 	public final static int SHR_ASSIGN_EXP = 73;
 	public final static int SHR_EXP = 74;
 	public final static int USHR_ASSIGN_EXP = 75;
 	public final static int USHR_EXP = 76;
 	public final static int XOR_ASSIGN_EXP = 77;
 	public final static int XOR_EXP = 78;
 	public final static int DECLARATION_EXP = 79;
 	public final static int DOLLAR_EXP = 80;
 	public final static int FUNC_EXP = 81;
 	public final static int IDENTIFIER_EXP = 82;
 	public final static int TEMPLATE_INSTANCE_WRAPPER = 83;
 	public final static int IFTYPE_EXP = 84;
 	public final static int INTEGER_EXP = 85;
 	public final static int SWITCH_ERROR_STATEMENT = 86;
 	public final static int NEW_ANON_CLASS_EXP = 87;
 	public final static int NEW_EXP = 88;
 	public final static int NULL_EXP = 89;
 	public final static int REAL_EXP = 90;
 	public final static int SCOPE_EXP = 91;
 	public final static int STRING_EXP = 92;
 	public final static int THIS_EXP = 93;
 	public final static int SUPER_EXP = 94;
 	public final static int TYPE_DOT_ID_EXP = 95;
 	public final static int TYPE_EXP = 96;
 	public final static int TYPEID_EXP = 97;
 	public final static int ADDR_EXP = 98;
 	public final static int ARRAY_EXP = 99;
 	public final static int ASSERT_EXP = 100;
 	public final static int CALL_EXP = 101;
 	public final static int CAST_EXP = 102;
 	public final static int COM_EXP = 103;
 	public final static int COMPILE_EXP = 104;
 	public final static int DELETE_EXP = 105;
 	public final static int DOT_ID_EXP = 106;
 	public final static int DOT_TEMPLATE_INSTANCE_EXP = 107;
 	public final static int FILE_EXP = 108;
 	public final static int NEG_EXP = 109;
 	public final static int NOT_EXP = 110;
 	public final static int BOOL_EXP = 111;
 	public final static int PTR_EXP = 112;
 	public final static int SLICE_EXP = 113;
 	public final static int UADD_EXP = 114;
 	public final static int ARRAY_INITIALIZER = 115;
 	public final static int EXP_INITIALIZER = 116;
 	public final static int STRUCT_INITIALIZER = 117;
 	public final static int VOID_INITIALIZER = 118;
 	public final static int MODIFIER = 119;
 	public final static int MODULE_DECLARATION = 120;
 	public final static int ASM_STATEMENT = 121;
 	public final static int BREAK_STATEMENT = 122;
 	public final static int CASE_STATEMENT = 123;
 	public final static int COMPILE_STATEMENT = 124;
 	public final static int COMPOUND_STATEMENT = 125;
 	public final static int ASM_BLOCK = 126;
 	public final static int CONDITIONAL_STATEMENT = 127;
 	public final static int CONTINUE_STATEMENT = 128;
 	public final static int DEFAULT_STATEMENT = 129;
 	public final static int DO_STATEMENT = 130;
 	public final static int EXP_STATEMENT = 131;
 	public final static int DECLARATION_STATEMENT = 132;
 	public final static int FOREACH_STATEMENT = 133;
 	public final static int FOR_STATEMENT = 134;
 	public final static int GOTO_CASE_STATEMENT = 135;
 	public final static int GOTO_DEFAULT_STATEMENT = 136;
 	public final static int GOTO_STATEMENT = 137;
 	public final static int IF_STATEMENT = 138;
 	public final static int LABEL_STATEMENT = 139;
 	public final static int ON_SCOPE_STATEMENT = 140;
 	public final static int PRAGMA_STATEMENT = 141;
 	public final static int RETURN_STATEMENT = 142;
 	public final static int STATIC_ASSERT_STATEMENT = 143;
 	public final static int SWITCH_STATEMENT = 144;
 	public final static int SYNCHRONIZED_STATEMENT = 145;
 	public final static int THROW_STATEMENT = 146;
 	public final static int VOLATILE_STATEMENT = 148;
 	public final static int WHILE_STATEMENT = 149;
 	public final static int WITH_STATEMENT = 150;
 	public final static int TEMPLATE_ALIAS_PARAMETER = 151;
 	public final static int TEMPLATE_TUPLE_PARAMETER = 152;
 	public final static int TEMPLATE_TYPE_PARAMETER = 153;
 	public final static int TEMPLATE_VALUE_PARAMETER = 154;
 	public final static int TYPE_A_ARRAY = 155;
 	public final static int TYPE_BASIC = 156;
 	public final static int TYPE_D_ARRAY = 157;
 	public final static int TYPE_DELEGATE = 158;
 	public final static int TYPE_FUNCTION = 159;
 	public final static int TYPE_POINTER = 160;
 	public final static int TYPE_IDENTIFIER = 161;
 	public final static int TYPE_INSTANCE = 162;
 	public final static int TYPE_TYPEOF = 163;
 	public final static int TYPE_S_ARRAY = 164;
 	public final static int TYPE_SLICE = 165;
 	public final static int TYPE_TYPEDEF = 166;
 	public final static int TYPE_ENUM = 167;
 	public final static int TUPLE_DECLARATION = 168;
 	public final static int TYPE_TUPLE = 169;
 	public final static int VAR_EXP = 170;
 	public final static int DOT_VAR_EXP = 171;
 	public final static int TYPE_STRUCT = 172;
 	public final static int DSYMBOL_EXP = 173;
 	public final static int TYPE_CLASS = 174;
 	public final static int THIS_DECLARATION = 175;
 	public final static int ARRAY_SCOPE_SYMBOL = 176;
 	public final static int SCOPE_DSYMBOL = 177;
 	public final static int TEMPLATE_EXP = 178;
 	public final static int TRY_FINALLY_STATEMENT = 179;
 	public final static int TRY_CATCH_STATEMENT = 180;
 	public final static int LABEL_DSYMBOL = 181;
 	public final static int HALT_EXP = 182;
 	public final static int SYM_OFF_EXP = 183;
 	public final static int SCOPE_STATEMENT = 184;
 	public final static int DELEGATE_EXP = 185;
 	public final static int TUPLE_EXP = 186;
 	public final static int UNROLLED_LOOP_STATEMENT = 187;
 	public final static int COMPLEX_EXP = 188;
 	public final static int ASSOC_ARRAY_LITERAL_EXP = 189;
 	public final static int FOREACH_RANGE_STATEMENT = 190;
 	public final static int TRAITS_EXP = 191;
 	public final static int COMMENT = 192;
 	public final static int PRAGMA = 193;
 	public final static int ARRAY_LENGTH_EXP = 194;
 	public final static int DOT_TEMPLATE_EXP = 195;
 	public final static int TYPE_REFERENCE = 196;
 	public final static int TYPE_RETURN = 197;
 	public final static int FILE_INIT_EXP = 198;
 	public final static int LINE_INIT_EXP = 199;
 	public final static int DEFAULT_INIT_EXP = 200;
 	public final static int POSTBLIT_DECLARATION = 201;
 	public final static int TEMPLATE_THIS_PARAMETER = 202;
 	public final static int OVER_EXP = 203;
 	public final static int STRUCT_LITERAL_EXP = 204;
 	public final static int INDEX_EXP = 205;
 	public final static int ALIAS_THIS = 206;
 
 	// Defined here because MATCH and Match overlap on Windows
 	public static class Match {
 		public int count; // number of matches found
 		public MATCH last; // match level of lastf
 		public FuncDeclaration lastf; // last matching function we found
 		public FuncDeclaration nextf; // current matching function
 		public FuncDeclaration anyf; // pick a func, any func, to use for error recovery
 	};
 
 	private final static class EXP_SOMETHING_INTERPRET extends Expression {
 		public EXP_SOMETHING_INTERPRET() {
 			super(null, 0, null);
 		}
 
 		@Override
 		public int getNodeType() {
 			return 0;
 		}
 
 		@Override
 		public String toChars(SemanticContext context) {
 			return null;
 		}
 
 		@Override
 		protected void accept0(IASTVisitor visitor) {
 		}
 	}
 
 	public final static Expression EXP_CANT_INTERPRET = new EXP_SOMETHING_INTERPRET();
 	public final static Expression EXP_CONTINUE_INTERPRET = new EXP_SOMETHING_INTERPRET();
 	public final static Expression EXP_BREAK_INTERPRET = new EXP_SOMETHING_INTERPRET();
 	public final static Expression EXP_GOTO_INTERPRET = new EXP_SOMETHING_INTERPRET();
 	public final static Expression EXP_VOID_INTERPRET = new EXP_SOMETHING_INTERPRET();
 
 	/***************************************************************************
 	 * Helper function for ClassDeclaration::accessCheck() Returns: 0 no access
 	 * 1 access
 	 */
 	public static boolean accessCheckX(Dsymbol smember, Dsymbol sfunc,
 			AggregateDeclaration dthis, AggregateDeclaration cdscope) {
 		Assert.isNotNull(dthis);
 
 		if (dthis.hasPrivateAccess(sfunc) || dthis.isFriendOf(cdscope)) {
 			if (smember.toParent() == dthis) {
 				return true;
 			} else {
 				ClassDeclaration cdthis = dthis.isClassDeclaration();
 				if (cdthis != null) {
 					for (int i = 0; i < cdthis.baseclasses.size(); i++) {
 						BaseClass b = cdthis.baseclasses.get(i);
 						PROT access;
 
 						access = b.base.getAccess(smember);
 						if (access.level >= PROTprotected.level
 								|| accessCheckX(smember, sfunc, b.base, cdscope)) {
 							return true;
 						}
 
 					}
 				}
 			}
 		} else {
 			if (smember.toParent() != dthis) {
 				ClassDeclaration cdthis = dthis.isClassDeclaration();
 				if (cdthis != null) {
 					for (int i = 0; i < cdthis.baseclasses.size(); i++) {
 						BaseClass b = cdthis.baseclasses.get(i);
 
 						if (accessCheckX(smember, sfunc, b.base, cdscope)) {
 							return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public static void inferApplyArgTypes(TOK op, Arguments arguments,
 			Expression aggr, SemanticContext context) {
 		if (arguments == null || arguments.isEmpty()) {
 			return;
 		}
 
 		/*
 		 * Return if no arguments need types.
 		 */
 		for (int u = 0; true; u++) {
 			if (u == arguments.size()) {
 				return;
 			}
 			Argument arg = arguments.get(u);
 			if (arg.type == null) {
 				break;
 			}
 		}
 
 		AggregateDeclaration ad;
 		FuncDeclaration fd;
 
 		Argument arg = arguments.get(0);
 		Type taggr = aggr.type;
 		if (taggr == null) {
 			return;
 		}
 		Type tab = taggr.toBasetype(context);
 		switch (tab.ty) {
 		case Tarray:
 		case Tsarray:
 		case Ttuple:
 			if (arguments.size() == 2) {
 				if (arg.type == null) {
 					arg.type = Type.tsize_t; // key type
 				}
 				arg = arguments.get(1);
 			}
 			if (arg.type == null && tab.ty != Ttuple) {
 				arg.type = tab.nextOf(); // value type
 			}
 			break;
 
 		case Taarray: {
 			TypeAArray taa = (TypeAArray) tab;
 
 			if (arguments.size() == 2) {
 				if (arg.type == null) {
 					arg.type = taa.index; // key type
 				}
 				arg = arguments.get(1);
 			}
 			if (arg.type == null) {
 				arg.type = taa.next; // value type
 			}
 			break;
 		}
 
 		case Tclass: {
 			ad = ((TypeClass) tab).sym;
 			// goto Laggr;
 			/*
 			 * Look for an int opApply(int delegate(ref Type [, ...]) dg);
 			 * overload
 			 */
 			Dsymbol s = search_function(ad,
 					(op == TOKforeach_reverse) ? Id.applyReverse : Id.apply,
 					context);
 			if (s != null) {
 				fd = s.isFuncDeclaration();
 				if (fd != null) {
 					inferApplyArgTypesX(fd, arguments, context);
 				}
 			}
 			break;
 		}
 
 		case Tstruct: {
 			ad = ((TypeStruct) tab).sym;
 			// goto Laggr;
 			/*
 			 * Look for an int opApply(int delegate(inout Type [, ...]) dg);
 			 * overload
 			 */
 			Dsymbol s = search_function(ad,
 					(op == TOKforeach_reverse) ? Id.applyReverse : Id.apply,
 					context);
 			if (s != null) {
 				fd = s.isFuncDeclaration();
 				if (fd != null) {
 					inferApplyArgTypesX(fd, arguments, context);
 				}
 			}
 			break;
 		}
 
 		case Tdelegate: {
 			if (false && aggr.op == TOKdelegate) {
 				DelegateExp de = (DelegateExp) aggr;
 
 				fd = de.func.isFuncDeclaration();
 				if (fd != null) {
 					inferApplyArgTypesX(fd, arguments, context);
 				}
 			} else {
 				inferApplyArgTypesY((TypeFunction) tab.nextOf(), arguments,
 						context);
 			}
 			break;
 		}
 
 		default:
 			break; // ignore error, caught later
 		}
 	}
 
 	public static void inferApplyArgTypesX(FuncDeclaration fstart,
 			Arguments arguments, SemanticContext context) {
 		Declaration d;
 		Declaration next;
 
 		for (d = fstart; d != null; d = next) {
 			FuncDeclaration f;
 			FuncAliasDeclaration fa;
 			AliasDeclaration a;
 
 			fa = d.isFuncAliasDeclaration();
 			if (fa != null) {
 				inferApplyArgTypesX(fa.funcalias, arguments, context);
 				next = fa.overnext;
 			} else if ((f = d.isFuncDeclaration()) != null) {
 				next = f.overnext;
 
 				TypeFunction tf = (TypeFunction) f.type;
 				if (inferApplyArgTypesY(tf, arguments, context)) {
 					continue;
 				}
 				if (arguments.size() == 0) {
 					return;
 				}
 			} else if ((a = d.isAliasDeclaration()) != null) {
 				Dsymbol s = a.toAlias(context);
 				next = s.isDeclaration();
 				if (next == a) {
 					break;
 				}
 				if (next == fstart) {
 					break;
 				}
 			} else {
 				if (context.acceptsErrors()) {
 					context.acceptProblem(Problem.newSemanticTypeError(
 							IProblem.DivisionByZero, d));
 				}
 				break;
 			}
 		}
 	}
 
 	public static boolean inferApplyArgTypesY(TypeFunction tf,
 			Arguments arguments, SemanticContext context) {
 		int nparams;
 		Argument p;
 
 		if (Argument.dim(tf.parameters, context) != 1) {
 			return true;
 		}
 		p = Argument.getNth(tf.parameters, 0, context);
 		if (p.type.ty != Tdelegate) {
 			return true;
 		}
 		tf = (TypeFunction) p.type.nextOf();
 		Assert.isTrue(tf.ty == Tfunction);
 
 		/*
 		 * We now have tf, the type of the delegate. Match it against the
 		 * arguments, filling in missing argument types.
 		 */
 		nparams = Argument.dim(tf.parameters, context);
 		if (nparams == 0 || tf.varargs != 0) {
 			return true; // not enough parameters
 		}
 		if (arguments.size() != nparams) {
 			return true; // not enough parameters
 		}
 
 		for (int u = 0; u < nparams; u++) {
 			Argument arg = arguments.get(u);
 			Argument param = Argument.getNth(tf.parameters, u, context);
 			if (arg.type != null) {
 				if (!arg.type.equals(param.type)) {
 					/*
 					 * Cannot resolve argument types. Indicate an error by
 					 * setting the number of arguments to 0.
 					 */
 					arguments.clear();
 					return false;
 				}
 				continue;
 			}
 			arg.type = param.type;
 		}
 		return false;
 	}
 
 	public static Expression resolveProperties(Scope sc, Expression e,
 			SemanticContext context) {
 		if (e.type != null) {
 			Type t = e.type.toBasetype(context);
 			
 			boolean condition;
 			if (context.isD2()) {
 				condition = t.ty == Tfunction || e.op == TOK.TOKoverloadset;
 			} else {
 				condition = t.ty == Tfunction;
 			}
 
 			if (condition) {
 				Expression e2 = e;
 				
 				e = new CallExp(e.filename, e.lineNumber,  e);
 				e.copySourceRange(e2);
 				e = e.semantic(sc, context);
 			}
 
 			/*
 			 * Look for e being a lazy parameter; rewrite as delegate call
 			 */
 			else if (e.op == TOKvar) {
 				VarExp ve = (VarExp) e;
 
 				if ((ve.var.storage_class & STClazy) != 0) {
 					e = new CallExp(e.filename, e.lineNumber,  e);
 					e = e.semantic(sc, context);
 				}
 			}
 
 			else if (e.op == TOKdotexp) {
 				if (context.acceptsErrors()) {
 					context.acceptProblem(Problem.newSemanticTypeError(
 							IProblem.SymbolHasNoValue, e,
 							e.toChars(context)));
 				}
 			}
 		}
 		return e;
 	}
 	
 	/******************************
 	 * Perform canThrow() on an array of Expressions.
 	 * @param context TODO
 	 */
 	public static boolean arrayExpressionCanThrow(Expressions exps, SemanticContext context) {
 		if (exps != null) {
 			for (int i = 0; i < exps.size(); i++) {
 				Expression e = (Expression) exps.get(i);
 				if (e != null && e.canThrow(context))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	public static Dsymbol search_function(ScopeDsymbol ad,
 			char[] funcid, SemanticContext context) {
 		Dsymbol s;
 		FuncDeclaration fd;
 		TemplateDeclaration td;
 
 		s = ad.search(null, 0, funcid, 0, context);
 		if (s != null) {
 			Dsymbol s2;
 
 			s2 = s.toAlias(context);
 			fd = s2.isFuncDeclaration();
 			if (fd != null && fd.type.ty == Tfunction) {
 				return fd;
 			}
 
 			td = s2.isTemplateDeclaration();
 			if (td != null) {
 				return td;
 			}
 		}
 		return null;
 	}
 	
 	public void accessCheck(Scope sc, Expression e, Declaration d,
 			SemanticContext context) {
 		accessCheck(sc, e, d, context, null);
 	}
 
 	public void accessCheck(Scope sc, Expression e, Declaration d,
 			SemanticContext context, ASTDmdNode reference) {
 		if (e == null) {
 			if ((d.prot() == PROTprivate && d.getModule() != sc.module
 					|| d.prot() == PROTpackage && !hasPackageAccess(sc, d))) {
 				if (context.acceptsErrors()) {
 					context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolIsNotAccessible, this, d.kind(), d
 							.getModule().toChars(context), d.toChars(context),
 							sc.module.toChars(context)));
 				}
 			}
 		} else if (e.type.ty == Tclass) { // Do access check
 			ClassDeclaration cd;
 
 			cd = (((TypeClass) e.type).sym);
 			if (e.op == TOKsuper) {
 				ClassDeclaration cd2;
 
 				cd2 = sc.func.toParent().isClassDeclaration();
 				if (cd2 != null) {
 					cd = cd2;
 				}
 			}
 			cd.accessCheck(sc, d, context, reference != null ? reference : e);
 		} else if (e.type.ty == Tstruct) { // Do access check
 			StructDeclaration cd;
 
 			cd = (((TypeStruct) e.type).sym);
 			cd.accessCheck(sc, d, context, reference != null ? reference : e);
 		}
 	}
 
 	public static void argExpTypesToCBuffer(OutBuffer buf,
 			Expressions arguments, HdrGenState hgs, SemanticContext context) {
 		if (arguments != null) {
 			OutBuffer argbuf = new OutBuffer();
 
 			for (int i = 0; i < arguments.size(); i++) {
 				Expression arg = arguments.get(i);
 
 				if (i != 0) {
 					buf.writeByte(',');
 				}
 				argbuf.reset();
 				arg.type.toCBuffer2(argbuf, hgs, 0, context);
 				buf.write(argbuf);
 			}
 		}
 	}
 
 	public static void argsToCBuffer(OutBuffer buf, Expressions arguments,
 			HdrGenState hgs, SemanticContext context) {
 		if (arguments != null) {
 			for (int i = 0; i < arguments.size(); i++) {
 				Expression arg = arguments.get(i);
 				
 				if (arg != null) {
 					if (i != 0) {
 						buf.writeByte(',');
 					}
 					expToCBuffer(buf, hgs, arg, PREC.PREC_assign, context);
 				}
 			}
 		}
 	}
 
 	public static void arrayExpressionSemantic(Expressions exps, Scope sc,
 			SemanticContext context) {
 		if (exps != null) {
 			for (int i = 0; i < exps.size(); i++) {
 				Expression e = exps.get(i);
 
 				e = e.semantic(sc, context);
 				exps.set(i, e);
 			}
 		}
 	}
 
 	private Expression createTypeInfoArray(Scope sc, List<Expression> exps,
 			int dim) {
 		// TODO semantic
 		return null;
 	}
 
 	public DYNCAST dyncast() {
 		return DYNCAST.DYNCAST_OBJECT;
 	}
 	
 	public static final void expToCBuffer(OutBuffer buf, HdrGenState hgs,
 			Expression e, PREC pr, SemanticContext context) {
 		expToCBuffer(buf, hgs, e, pr.ordinal(), context);
 	}
 
 	public static void expToCBuffer(OutBuffer buf, HdrGenState hgs,
 			Expression e, int pr, SemanticContext context) {
 		// SEMANTIC
 		if (e == null || e.op == null) {
 			return;
 		}
 		
 		if (e.op.precedence.ordinal() < pr ||
 				(pr == PREC.PREC_rel.ordinal() && e.op.precedence.ordinal() == pr)
 				) {
 			buf.writeByte('(');
 			e.toCBuffer(buf, hgs, context);
 			buf.writeByte(')');
 		} else {
 			e.toCBuffer(buf, hgs, context);
 		}
 	}
 
 	protected void fatal(SemanticContext context) {
 		context.fatalWasSignaled = true;
 	}
 
 	public static boolean findCondition(HashtableOfCharArrayAndObject ids, char[] ident) {
 		return ids != null && ids.containsKey(ident);
 	}
 	
 	public static boolean findCondition(HashtableOfCharArrayAndObject ids, IdentifierExp ident) {
 		if (ident == null || ident.ident == null) {
 			return false;
 		}
 		return ids != null && ids.containsKey(ident.ident);
 	}
 
 	public void functionArguments(char[] filename, int lineNumber, Scope sc, TypeFunction tf,
 			Expressions arguments, SemanticContext context) {
 		int n;
 		int done;
 		Type tb;
 
 //		Assert.isNotNull(arguments);
 		int nargs = arguments != null ? arguments.size() : 0;
 		int nparams = Argument.dim(tf.parameters, context);
 
 		if (nargs > nparams && tf.varargs == 0) {
 			if (context.acceptsErrors()) {
 				context.acceptProblem(Problem.newSemanticTypeError(
 						IProblem.ExpectedNumberArguments, this, String.valueOf(nparams), String.valueOf(nargs)));
 			}
 		}
 
 		n = (nargs > nparams) ? nargs : nparams; // n = max(nargs, nparams)
 
 		done = 0;
 		for (int i = 0; i < n; i++) {
 			Expression arg;
 
 			if (i < nargs) {
 				arg = arguments.get(i);
 			} else {
 				arg = null;
 			}
 
 			if (i < nparams) {
 				Argument p = Argument.getNth(tf.parameters, i, context);
 
 				boolean gotoL2 = false;
 				if (arg == null) {
 					if (p.defaultArg == null) {
 						if (tf.varargs == 2 && i + 1 == nparams) {
 							// goto L2;
 							gotoL2 = true;
 						}
 						if (!gotoL2) {
 							if (context.acceptsErrors()) {
 								context.acceptProblem(Problem.newSemanticTypeError(
 										IProblem.ExpectedNumberArguments, this, String.valueOf(nparams), String.valueOf(nargs)));
 							}
 						}
 						break;
 					}
 					if (!gotoL2) {
 						arg = p.defaultArg;
 						if (context.isD2()) {
 							if (arg.op == TOKdefault) {
 								DefaultInitExp de = (DefaultInitExp) arg;
 								arg = de.resolve(filename, lineNumber, sc, context);
 							} else {
 								arg = arg.copy();
 							}
 						} else {
 							arg = arg.copy();
 						}
 						arguments.add(arg);
 						nargs++;
 					}
 				}
 
 				if ((tf.varargs == 2 && i + 1 == nparams) || gotoL2) {
 					boolean gotoL1 = false;
 					
 					if (!gotoL2) {
 						if (arg.implicitConvTo(p.type, context) != MATCH.MATCHnomatch) {
 							if (nargs != nparams) {
 								context.acceptProblem(Problem.newSemanticTypeError(
 										IProblem.ExpectedNumberArguments, this, String.valueOf(nparams), String.valueOf(nargs)));
 							}
 							gotoL1 = true;
 							// goto L1;
 						}
 					}
 					
 					if (!gotoL1 || gotoL2) {
 						// L2:
 						tb = p.type.toBasetype(context);
 						Type tret = p.isLazyArray(context);
 						switch (tb.ty) {
 						case Tsarray:
 						case Tarray: { 
 							// Create a static array variable v of type
 							// arg.type
 	
 							IdentifierExp id = context.uniqueId("__arrayArg");
 							Type t = new TypeSArray(((TypeArray) tb).next, new IntegerExp(filename, lineNumber,
 									nargs - i), context.encoder);
 							t = t.semantic(filename, lineNumber, sc, context);
 							VarDeclaration v = new VarDeclaration(filename, lineNumber, t, id,
 									new VoidInitializer(filename, lineNumber));
 							v.semantic(sc, context);
 							v.parent = sc.parent;
 	
 							Expression c = new DeclarationExp(filename, lineNumber, v);
 							c.type = v.type;
 	
 							for (int u = i; u < nargs; u++) {
 								Expression a = arguments.get(u);
 								if (tret != null && !((TypeArray)tb).next.equals(a.type)) {
 									a = a.toDelegate(sc, tret, context);
 								}
 	
 								Expression e = new VarExp(filename, lineNumber, v);
 								e = new IndexExp(filename, lineNumber, e, new IntegerExp(filename, lineNumber, u + 1
 										- nparams));
 								AssignExp ae = new AssignExp(filename, lineNumber, e, a);
 								if (context.isD2()) {
 									ae.op = TOK.TOKconstruct;
 								}
 								if (c != null) {
 									c = new CommaExp(filename, lineNumber, c, ae);
 								} else {
 									c = ae;
 								}
 							}
 							arg = new VarExp(filename, lineNumber, v);
 							if (c != null) {
 								arg = new CommaExp(filename, lineNumber, c, arg);
 							}
 							break;
 						}
 						case Tclass: { /*
 						 * Set arg to be: new Tclass(arg0, arg1,
 						 * ..., argn)
 						 */
 							Expressions args = new Expressions(nargs - 1);
 							args.setDim(nargs - 1);
 							for (int u = i; u < nargs; u++) {
 								args.set(u - i, arguments.get(u));
 							}
 							arg = new NewExp(filename, lineNumber, null, null, p.type, args);
 							break;
 						}
 						default:
 							if (arg == null) {
 								if (context.acceptsErrors()) {
 									context.acceptProblem(Problem.newSemanticTypeError(IProblem.NotEnoughArguments, this));
 								}
 								return;
 							}
 							break;
 						}
 						arg = arg.semantic(sc, context);
 						arguments.setDim(i + 1);
 						done = 1;
 					}
 				}
 
 				// L1: 
 				if (context.isD2()) {
 					if (!((p.storageClass & STClazy) != 0 && p.type.ty == Tvoid)) {
 						if (p.type != arg.type) {
 							arg = arg.implicitCastTo(sc, p.type, context);
 							arg = arg.optimize(WANTvalue, context);
 						}
 					}
 					if ((p.storageClass & STCref) != 0) {
 						arg = arg.toLvalue(sc, arg, context);
 					} else if ((p.storageClass & STCout) != 0) {
 						arg = arg.modifiableLvalue(sc, arg, context);
 					}
 
 					// Convert static arrays to pointers
 					tb = arg.type.toBasetype(context);
 					if (tb.ty == Tsarray) {
 						arg = arg.checkToPointer(context);
 					}
 
 					if (tb.ty == Tstruct
 							&& 0 == (p.storageClass & (STCref | STCout))) {
 						arg = callCpCtor(filename, lineNumber, sc, arg, context);
 					}
 
 					// Convert lazy argument to a delegate
 					if ((p.storageClass & STClazy) != 0) {
 						arg = arg.toDelegate(sc, p.type, context);
 					}
 					
 				    /*
 					 * Look for arguments that cannot 'escape' from the called
 					 * function.
 					 */
 					if (!tf.parameterEscapes(p, context)) {
 						/*
 						 * Function literals can only appear once, so if this
 						 * appearance was scoped, there cannot be any others.
 						 */
 						if (arg.op == TOKfunction) {
 							FuncExp fe = (FuncExp) arg;
 							fe.fd.tookAddressOf = 0;
 						}
 
 						/*
 						 * For passing a delegate to a scoped parameter, this
 						 * doesn't count as taking the address of it. We only
 						 * worry about 'escaping' references to the function.
 						 */
 						else if (arg.op == TOKdelegate) {
 							DelegateExp de = (DelegateExp) arg;
 							if (de.e1.op == TOKvar) {
 								VarExp ve = (VarExp) de.e1;
 								FuncDeclaration f = ve.var.isFuncDeclaration();
 								if (f != null) {
 									f.tookAddressOf--;
 									// printf("tookAddressOf = %d\n",
 									// f.tookAddressOf);
 								}
 							}
 						}
 					}
 				} else {
 					if (!((p.storageClass & STClazy) != 0 && p.type.ty == Tvoid)) {
 						arg = arg.implicitCastTo(sc, p.type, context);
 					}
 					if ((p.storageClass & (STCout | STCref)) != 0) {
 						// BUG: should check that argument to inout is type
 						// 'invariant'
 						// BUG: assignments to inout should also be type 'invariant'
 						arg = arg.modifiableLvalue(sc, arg, context);
 	
 						// if (arg.op == TOKslice)
 						// arg.error("cannot modify slice %s", arg.toChars());
 	
 						// Don't have a way yet to do a pointer to a bit in array
 						if (arg.op == TOKarray && 
 								arg.type.toBasetype(context).ty == Tbit) {
 							if (context.acceptsErrors()) {
 								context.acceptProblem(Problem.newSemanticTypeError(
 										IProblem.CannotHaveOutOrInoutArgumentOfBitInArray, this));
 							}
 						}
 					}
 					
 					// Convert static arrays to pointers
 					tb = arg.type.toBasetype(context);
 					if (tb.ty == Tsarray) {
 						arg = arg.checkToPointer(context);
 					}
 				}				
 
 				// Convert lazy argument to a delegate
 				if ((p.storageClass & STClazy) != 0) {
 					arg = arg.toDelegate(sc, p.type, context);
 				}
 			} else {
 
 				// If not D linkage, do promotions
 				if (tf.linkage != LINKd) {
 					// Promote bytes, words, etc., to ints
 					arg = arg.integralPromotions(sc, context);
 
 					// Promote floats to doubles
 					switch (arg.type.ty) {
 					case Tfloat32:
 						arg = arg.castTo(sc, Type.tfloat64, context);
 						break;
 
 					case Timaginary32:
 						arg = arg.castTo(sc, Type.timaginary64, context);
 						break;
 					}
 				}
 
 				// Convert static arrays to dynamic arrays
 				tb = arg.type.toBasetype(context);
 				if (tb.ty == Tsarray) {
 					TypeSArray ts = (TypeSArray) tb;
 					Type ta = ts.next.arrayOf(context);
 					if (ts.size(arg.filename, arg.lineNumber, context) == 0) {
 						arg = new NullExp(arg.filename, arg.lineNumber);
 						arg.type = ta;
 					} else {
 						arg = arg.castTo(sc, ta, context);
 					}
 				}
 				
 			    if (tb.ty == Tstruct) {
 					arg = callCpCtor(filename, lineNumber, sc, arg, context);
 				}
 
 				// Give error for overloaded function addresses
 				if (arg.op == TOKsymoff) {
 					SymOffExp se = (SymOffExp) arg;
 					if (se.hasOverloads
 							&& null == se.var.isFuncDeclaration().isUnique(
 									context)) {
 						if (context.acceptsErrors()) {
 							context.acceptProblem(Problem.newSemanticTypeError(IProblem.FunctionIsOverloaded, arg, arg.toChars(context)));
 						}
 					}
 				}
 
 				arg.rvalue(context);
 			}
 			arg = arg.optimize(WANTvalue, context);
 			arguments.set(i, arg);
 			if (done != 0) {
 				break;
 			}
 		}
 
 		// If D linkage and variadic, add _arguments[] as first argument
 		if (tf.linkage == LINKd && tf.varargs == 1) {
 			Expression e;
 			e = createTypeInfoArray(sc, 
 					arguments.subList(nparams, arguments.size()), 
 					arguments.size() - nparams);
 			arguments.add(0, e);
 		}
 	}
 
 	private Expression callCpCtor(char[] filename, int lineNumber, Scope sc, Expression e, SemanticContext context) {
 		Type tb = e.type.toBasetype(context);
 		assert (tb.ty == Tstruct);
 		StructDeclaration sd = ((TypeStruct) tb).sym;
 		if (sd.cpctor != null) {
 			/*
 			 * Create a variable tmp, and replace the argument e with: (tmp =
 			 * e),tmp and let AssignExp() handle the construction. This is not
 			 * the most efficent, ideally tmp would be constructed directly onto
 			 * the stack.
 			 */
 			IdentifierExp idtmp = context.uniqueId("__tmp");
 			VarDeclaration tmp = new VarDeclaration(filename, lineNumber, tb, idtmp,
 					new ExpInitializer(null, 0, e));
 			Expression ae = new DeclarationExp(filename, lineNumber, tmp);
 			e = new CommaExp(filename, lineNumber, ae, new VarExp(filename, lineNumber, tmp));
 			e = e.semantic(sc, context);
 		}
 		return e;
 	}
 
 	public abstract int getNodeType();
 
 	/***************************************************************************
 	 * Determine if scope sc has package level access to s.
 	 */
 	public static boolean hasPackageAccess(Scope sc, Dsymbol s) {
 
 		for (; s != null; s = s.parent) {
 			if (s.isPackage() != null && s.isModule() == null) {
 				break;
 			}
 		}
 
 		if (s != null && s == sc.module.parent) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Determine if 'this' is available. If it is, return the FuncDeclaration
 	 * that has it.
 	 */
 	public FuncDeclaration hasThis(Scope sc) {
 		FuncDeclaration fd;
 		FuncDeclaration fdthis;
 
 		fdthis = sc.parent.isFuncDeclaration();
 
 		// Go upwards until we find the enclosing member function
 		fd = fdthis;
 		while (true) {
 			if (fd == null) {
 				// goto Lno;
 				return null; // don't have 'this' available
 			}
 			if (!fd.isNested()) {
 				break;
 			}
 
 			Dsymbol parent = fd.parent;
 			while (parent != null) {
 				TemplateInstance ti = parent.isTemplateInstance();
 				if (ti != null) {
 					parent = ti.parent;
 				} else {
 					break;
 				}
 			}
 
 			fd = fd.parent.isFuncDeclaration();
 		}
 
 		if (fd.isThis() == null) {
 			// goto Lno;
 			return null; // don't have 'this' available
 		}
 
 		Assert.isNotNull(fd.vthis());
 		return fd;
 	}
 
 	public void preFunctionArguments(char[] filename, int lineNumber, Scope sc, Expressions exps,
 			SemanticContext context) {
 		if (exps != null) {
 			expandTuples(exps, context);
 
 			for (int i = 0; i < exps.size(); i++) {
 				Expression arg = exps.get(i);
 
 				if (arg.type == null) {
 					if (context.acceptsWarnings()) {
 						context.acceptProblem(Problem.newSemanticTypeWarning(
 								IProblem.SymbolNotAnExpression, 0, arg.start,
 								arg.length, arg.toChars(context)));
 					}
 					arg = new IntegerExp(arg.filename, arg.lineNumber, 0, Type.tint32);
 				}
 
 				arg = resolveProperties(sc, arg, context);
 				exps.set(i, arg);
 			}
 		}
 	}
 
 	public boolean RealEquals(real_t r1, real_t r2) {
 		return r1.equals(r2);
 	}
 
 	public String toChars(SemanticContext context) {
 		throw new IllegalStateException(
 				"This is an abstract method in DMD an should be implemented");
 	}
 
 	protected String toPrettyChars(SemanticContext context) {
 		throw new IllegalStateException("Problem reporting not implemented");
 	}
 
 	public final int getElementType() {
 		return getNodeType();
 	}
 	
 	/*************************************
 	 * If variable has a const initializer,
 	 * return that initializer.
 	 */
 
 	public static Expression expandVar(int result, VarDeclaration v,
 			SemanticContext context) {
 		Expression e = null;
 		if (v != null
 				&& (v.isConst() || v.isInvariant(context) || (v.storage_class & STCmanifest) != 0)) {
 			Type tb = v.type.toBasetype(context);
 			if ((result & WANTinterpret) != 0
 					|| (v.storage_class & STCmanifest) != 0
 					|| (tb.ty != Tsarray && tb.ty != Tstruct)) {
 				if (v.init != null) {
 					if (v.inuse != 0) {
 						// goto L1;
 						return e;
 					}
 					Expression ei = v.init.toExpression(context);
 					if (null == ei) {
 						// goto L1;
 						return e;
 					}
 					if (ei.op == TOKconstruct || ei.op == TOKblit) {
 						AssignExp ae = (AssignExp) ei;
 						ei = ae.e2;
 						if (ei.isConst() != true && ei.op != TOKstring) {
 							// goto L1;
 							return e;
 						}
 						if (ei.type != v.type) {
 							// goto L1;
 							return e;
 						}
 					}
 					if (v.scope != null) {
 						v.inuse++;
 						e = ei.syntaxCopy(context);
 						e = e.semantic(v.scope, context);
 						e = e.implicitCastTo(v.scope, v.type, context);
 						v.scope = null;
 						v.inuse--;
 					} else if (null == ei.type) {
 						// goto L1;
 						return e;
 					} else {
 						// Should remove the copy() operation by
 						// making all mods to expressions copy-on-write
 						e = ei.copy();
 					}
 				} else {
 					// goto L1;
 					return e;
 				}
 				if (e.type != v.type) {
 					e = e.castTo(null, v.type, context);
 				}
 				e = e.optimize(result, context);
 			}
 		}
 		//L1: 
 		return e;
 	}
 
 
 	/*************************************
 	 * If expression is a variable with a const initializer,
 	 * return that initializer.
 	 */
 	
 	public static Expression fromConstInitializer(Expression e1,
 			SemanticContext context) {
 		return fromConstInitializer(0, e1, context);
 	}
 
 	public static Expression fromConstInitializer(int result, Expression e1,
 			SemanticContext context) {
 		if (context.isD2()) {
 		    Expression e = e1;
 			if (e1.op == TOKvar) {
 				VarExp ve = (VarExp) e1;
 				VarDeclaration v = ve.var.isVarDeclaration();
 				e = expandVar(result, v, context);
 				if (e != null) {
 					if (e.type != e1.type) { // Type 'paint' operation
 						e = e.copy();
 						e.type = e1.type;
 					}
 				} else
 					e = e1;
 			}
 			return e;
 		} else {
 			if (e1.op == TOKvar) {
 				VarExp ve = (VarExp) e1;
 				VarDeclaration v = ve.var.isVarDeclaration();
 				if (v != null && v.isConst() && v.init() != null) {
 					Expression ei = v.init().toExpression(context);
 					if (ei != null && ei.type != null) {
 						e1 = ei;
 					}
 				}
 			}
 		}
 		return e1;
 	}
 
 	public static void arrayExpressionScanForNestedRef(Scope sc, Expressions a,
 			SemanticContext context) {
 		if (null != a) {
 			for (int i = 0; i < a.size(); i++) {
 				Expression e = a.get(i);
 
 				if (null != e) {
 					e.scanForNestedRef(sc, context);
 				}
 			}
 		}
 	}
 
 	public static String mangle(Declaration sthis) {
 		OutBuffer buf = new OutBuffer();
 		String id;
 		Dsymbol s;
 
 		s = sthis;
 		do {
 			if (s.ident != null) {
 				FuncDeclaration fd = s.isFuncDeclaration();
 				if (s != sthis && fd != null) {
 					id = mangle(fd);
 					buf.prependstring(id);
 					// goto L1;
 					break;
 				} else {
 					id = s.ident.toChars();
 					int len = id.length();
 					buf.prependstring(id);
 					buf.prependstring(len);
 				}
 			} else {
 				buf.prependstring("0");
 			}
 			s = s.parent;
 		} while (s != null);
 
 		// L1:
 		FuncDeclaration fd = sthis.isFuncDeclaration();
 		if (fd != null && (fd.needThis() || fd.isNested())) {
 			buf.writeByte(Type.needThisPrefix());
 		}
 		if (sthis.type.deco != null) {
 			buf.writestring(sthis.type.deco);
 		} else {
 			if (!fd.inferRetType()) {
 				throw new IllegalStateException("assert (fd.inferRetType);");
 			}
 		}
 
 		id = buf.toChars();
 		buf.data = null;
 		return id;
 	}
 
 	public static Dsymbol getDsymbol(ASTDmdNode oarg, SemanticContext context) {
 		Dsymbol sa;
 		Expression ea = isExpression(oarg);
 		if (ea != null) { // Try to convert Expression to symbol
 			if (ea.op == TOKvar) {
 				sa = ((VarExp) ea).var;
 			} else if (ea.op == TOKfunction) {
 				sa = ((FuncExp) ea).fd;
 			} else {
 				sa = null;
 			}
 		} else { // Try to convert Type to symbol
 			Type ta = isType(oarg);
 			if (ta != null) {
 				sa = ta.toDsymbol(null, context);
 			} else {
 				sa = isDsymbol(oarg); // if already a symbol
 			}
 		}
 		return sa;
 	}
 
 	public static Type getType(ASTDmdNode o) {
 		Type t = isType(o);
 		if (null == t) {
 			Expression e = isExpression(o);
 			if (e != null) {
 				t = e.type;
 			}
 		}
 		return t;
 	}
 
 	public static Dsymbol isDsymbol(ASTDmdNode o) {
 		//return dynamic_cast<Dsymbol >(o);
 		if (null == o || o.dyncast() != DYNCAST_DSYMBOL) {
 			return null;
 		}
 		return (Dsymbol) o;
 	}
 
 	public static Expression isExpression(ASTDmdNode o) {
 		//return dynamic_cast<Expression >(o);
 		if (null == o || o.dyncast() != DYNCAST_EXPRESSION) {
 			return null;
 		}
 		return (Expression) o;
 	}
 
 	public static Tuple isTuple(ASTDmdNode o) {
 		//return dynamic_cast<Tuple >(o);
 		if (null == o || o.dyncast() != DYNCAST_TUPLE) {
 			return null;
 		}
 		return (Tuple) o;
 	}
 
 	public static Type isType(ASTDmdNode o) {
 		//return dynamic_cast<Type >(o);
 		if (null == o || o.dyncast() != DYNCAST_TYPE) {
 			return null;
 		}
 		return (Type) o;
 	}
 
 	public static Expression semanticLength(Scope sc, Type t, Expression exp,
 			SemanticContext context) {
 		if (t.ty == Ttuple) {
 			ScopeDsymbol sym = new ArrayScopeSymbol(sc, (TypeTuple) t);
 			sym.parent = sc.scopesym;
 			sc = sc.push(sym);
 
 			exp = exp.semantic(sc, context);
 
 			sc.pop();
 		} else {
 			exp = exp.semantic(sc, context);
 		}
 		return exp;
 	}
 
 	public static Expression semanticLength(Scope sc, TupleDeclaration s,
 			Expression exp, SemanticContext context) {
 		ScopeDsymbol sym = new ArrayScopeSymbol(sc, s);
 		sym.parent = sc.scopesym;
 		sc = sc.push(sym);
 
 		exp = exp.semantic(sc, context);
 
 		sc.pop();
 		return exp;
 	}
 
 	public static void overloadResolveX(Match m, FuncDeclaration fstart,
 			Expression ethis, Expressions arguments, SemanticContext context) {
 		Param2 p = new Param2();
 		p.m = m;
 		p.ethis = ethis;
 		p.arguments = arguments;
 		overloadApply(fstart, fp2, p, context);
 	}
 
 	public static interface OverloadApply_fp {
 		int call(Object param, FuncDeclaration f, SemanticContext context);
 	}
 	
 	public final static OverloadApply_fp fp1 = new OverloadApply_fp() {
 
 		public int call(Object param, FuncDeclaration f, SemanticContext context) {
 			Param1 p = (Param1) param;
 			Type t = p.t;
 
 			if (t.equals(f.type)) {
 				p.f = f;
 				return 1;
 			}
 
 			if (context.isD2()) {
 				/*
 				 * Allow covariant matches, if it's just a const conversion of
 				 * the return type
 				 */
 				if (t.ty == Tfunction) {
 					TypeFunction tf = (TypeFunction) f.type;
 					if (tf.covariant(t, context) == 1
 							&& tf.nextOf().implicitConvTo(t.nextOf(), context)
 									.ordinal() >= MATCHconst.ordinal()) {
 						p.f = f;
 						return 1;
 					}
 				}
 			}
 		    return 0;
 		}
 
 	};
 
 	public final static OverloadApply_fp fp2 = new OverloadApply_fp() {
 
 		public int call(Object param, FuncDeclaration f, SemanticContext context) {
 			Param2 p = (Param2) param;
 			Match m = p.m;
 			Expressions arguments = p.arguments;
 			MATCH match;
 
 			if (f != m.lastf) // skip duplicates
 			{
 				TypeFunction tf;
 
 				m.anyf = f;
 				tf = (TypeFunction) f.type;
 				match = tf.callMatch(f.needThis() ? p.ethis : null, arguments, context);
 				if (match != MATCHnomatch) {
 					if (match.ordinal() > m.last.ordinal()) {
 						// goto LfIsBetter;
 						m.last = match;
 						m.lastf = f;
 						m.count = 1;
 						return 0;
 					}
 
 					if (match.ordinal() < m.last.ordinal()) {
 						// goto LlastIsBetter;
 						return 0;
 					}
 
 					/* See if one of the matches overrides the other.
 					 */
 					if (m.lastf.overrides(f, context)) {
 						// goto LlastIsBetter;
 						return 0;
 					} else if (f.overrides(m.lastf, context)) {
 						// goto LfIsBetter;
 						m.last = match;
 						m.lastf = f;
 						m.count = 1;
 						return 0;
 					}
 					
 					/* Try to disambiguate using template-style partial ordering rules.
 				     * In essence, if f() and g() are ambiguous, if f() can call g(),
 				     * but g() cannot call f(), then pick f().
 				     * This is because f() is "more specialized."
 				     */
 				    {
 						MATCH c1 = f.leastAsSpecialized(m.lastf, context);
 						MATCH c2 = m.lastf.leastAsSpecialized(f, context);
 						if (c1.ordinal() > c2.ordinal()) {
 							// goto LfIsBetter;
 							m.last = match;
 							m.lastf = f;
 							m.count = 1;
 							return 0;
 						}
 						if (c1.ordinal() < c2.ordinal()) {
 							// goto LlastIsBetter;
 							return 0;
 						}
 					}
 
 					// Lambiguous:
 					m.nextf = f;
 					m.count++;
 					return 0;
 				}
 			}
 			return 0;
 		}
 
 	};
 
 	/***************************************************
 	 * Visit each overloaded function in turn, and call
 	 * (*fp)(param, f) on it.
 	 * Exit when no more, or (*fp)(param, f) returns 1.
 	 * Returns:
 	 *	0	continue
 	 *	1	done
 	 */
 
 	public static boolean overloadApply(FuncDeclaration fstart,
 			OverloadApply_fp fp, Object param, SemanticContext context) {
 		FuncDeclaration f;
 		Declaration d;
 		Declaration next;
 
 		for (d = fstart; d != null; d = next) {
 			FuncAliasDeclaration fa = d.isFuncAliasDeclaration();
 
 			if (fa != null) {
 				if (overloadApply(fa.funcalias, fp, param, context)) {
 					return false;
 				}
 				next = fa.overnext;
 			} else {
 				AliasDeclaration a = d.isAliasDeclaration();
 
 				if (a != null) {
 					Dsymbol s = a.toAlias(context);
 					next = s.isDeclaration();
 					if (next == a) {
 						break;
 					}
 					if (next == fstart) {
 						break;
 					}
 				} else {
 					f = d.isFuncDeclaration();
 					if (null == f) {
 						if (context.acceptsErrors()) {
 							context.acceptProblem(Problem.newSemanticTypeError(
 									IProblem.SymbolIsAliasedToAFunction, a, a.toChars(context)));
 						}
 						break; // BUG: should print error message?
 					}
 					if (fp.call(param, f, context) != 0) {
 						return true;
 					}
 
 					next = f.overnext;
 				}
 			}
 		}
 		return false;
 	}
 
 	public static Expression interpret_aaLen(InterState istate,
 			Expressions arguments, SemanticContext context) {
 		if (null == arguments || arguments.size() != 1) {
 			return null;
 		}
 		Expression earg = arguments.get(0);
 		earg = earg.interpret(istate, context);
 		if (earg == EXP_CANT_INTERPRET) {
 			return null;
 		}
 		if (earg.op != TOKassocarrayliteral) {
 			return null;
 		}
 		AssocArrayLiteralExp aae = (AssocArrayLiteralExp) earg;
 		Expression e = new IntegerExp(aae.filename, aae.lineNumber, aae.keys.size(), Type.tsize_t);
 		return e;
 	}
 
 	public static Expression interpret_aaKeys(InterState istate,
 			Expressions arguments, SemanticContext context) {
 		if (null == arguments || arguments.size() != 2) {
 			return null;
 		}
 		Expression earg = arguments.get(0);
 		earg = earg.interpret(istate, context);
 		if (earg == EXP_CANT_INTERPRET) {
 			return null;
 		}
 		if (earg.op != TOKassocarrayliteral) {
 			return null;
 		}
 		AssocArrayLiteralExp aae = (AssocArrayLiteralExp) earg;
 		Expression e = new ArrayLiteralExp(aae.filename, aae.lineNumber, aae.keys);
 		return e;
 	}
 
 	public static Expression interpret_aaValues(InterState istate,
 			Expressions arguments, SemanticContext context) {
 		if (null == arguments || arguments.size() != 3) {
 			return null;
 		}
 		Expression earg = arguments.get(0);
 		earg = earg.interpret(istate, context);
 		if (earg == EXP_CANT_INTERPRET) {
 			return null;
 		}
 		if (earg.op != TOKassocarrayliteral) {
 			return null;
 		}
 		AssocArrayLiteralExp aae = (AssocArrayLiteralExp) earg;
 		Expression e = new ArrayLiteralExp(aae.filename, aae.lineNumber, aae.values);
 		return e;
 	}
 
 	public void expandTuples(Expressions exps, SemanticContext context) {
 		if (exps != null) {
 			for (int i = 0; i < exps.size(); i++) {
 				Expression arg = exps.get(i);
 				if (null == arg) {
 					continue;
 				}
 
 				// Look for tuple with 0 members
 				if (arg.op == TOKtype) {
 					TypeExp e = (TypeExp) arg;
 					if (e.type.toBasetype(context).ty == Ttuple) {
 						TypeTuple tt = (TypeTuple) e.type.toBasetype(context);
 
 						if (null == tt.arguments || tt.arguments.size() == 0) {
 							exps.remove(i);
 							if (i == exps.size()) {
 								return;
 							}
 							i--;
 							continue;
 						}
 					}
 				}
 
 				// Inline expand all the tuples
 				while (arg.op == TOKtuple) {
 					TupleExp te = (TupleExp) arg;
 
 					exps.remove(i); // remove arg
 					exps.addAll(i, te.exps); // replace with tuple contents
 					if (i == exps.size()) {
 						return; // empty tuple, no more arguments
 					}
 					arg = exps.get(i);
 				}
 			}
 		}
 	}
 
 	public static Expression expType(Type type, Expression e, SemanticContext context) {
 		if (!same(type, e.type, context)) {
 			e = e.copy();
 			e.type = type;
 		}
 		return e;
 	}
 
 	public static final Expression getVarExp(char[] filename, int lineNumber, InterState istate,
                                              Declaration d,
                                              SemanticContext context)
     {
         Expression e = EXP_CANT_INTERPRET;
         VarDeclaration v = d.isVarDeclaration();
         SymbolDeclaration s = d.isSymbolDeclaration();
         if (null != v)
         {
         	boolean condition;
         	if (context.isD2()) {
         		condition = (v.isConst() || v.isInvariant(context)) && v.init != null && null == v.value;
         	} else {
         		condition = v.isConst() && null != v.init();
         	}
         	
             if (condition)
             {
                 e = v.init().toExpression(context);
                 if (e != null && null == e.type) {
 					e.type = v.type;
 				}
             }
             else
             {
                 e = v.value();
                 if (null == e) {
                 	if (context.acceptsErrors()) {
 	                	context.acceptProblem(Problem.newSemanticTypeError(
 	        					IProblem.VariableIsUsedBeforeInitialization, v, v.toChars(context)));
                 	}
                 }
                 else if (e != EXP_CANT_INTERPRET) {
 					e = e.interpret(istate, context);
 				}
             }
             if (null == e) {
 				e = EXP_CANT_INTERPRET;
 			}
         }
         else if (null != s)
         {
             if (s.dsym().toInitializer() == s.sym())
             {
                 Expressions exps = new Expressions(0);
                 e = new StructLiteralExp(null, 0, s.dsym(), exps);
                 e = e.semantic(null, context);
             }
         }
         return e;
     }
 
 	public static void ObjectToCBuffer(OutBuffer buf, HdrGenState hgs,
 			ASTDmdNode oarg, SemanticContext context) {
 		Type t = isType(oarg);
 		Expression e = isExpression(oarg);
 		Dsymbol s = isDsymbol(oarg);
 		Tuple v = isTuple(oarg);
 		if (null != t) {
 			t.toCBuffer(buf, null, hgs, context);
 		} else if (null != e) {
 			e.toCBuffer(buf, hgs, context);
 		} else if (null != s) {
 			String p = null != s.ident ? s.ident.toChars() : s.toChars(context);
 			buf.writestring(p);
 		} else if (null != v) {
 			Objects args = v.objects;
 			for (int i = 0; i < args.size(); i++) {
 				if (i > 0) {
 					buf.writeByte(',');
 				}
 				ASTDmdNode o = args.get(i);
 				ObjectToCBuffer(buf, hgs, o, context);
 			}
 		} else if (null == oarg) {
 			buf.writestring("null");
 		} else {
 			assert (false);
 		}
 	}
 
 	public static void templateResolve(Match m, TemplateDeclaration td,
 			Scope sc, char[] filename, int lineNumber, Objects targsi, Expression ethis, Expressions arguments,
 			SemanticContext context) {
 		FuncDeclaration fd;
 
 		assert (td != null);
 		fd = td.deduceFunctionTemplate(sc, filename, lineNumber, targsi, ethis, arguments, context);
 		if (null == fd) {
 			return;
 		}
 		m.anyf = fd;
 		if (m.last.ordinal() >= MATCHexact.ordinal()) {
 			m.nextf = fd;
 			m.count++;
 		} else {
 			m.last = MATCHexact;
 			m.lastf = fd;
 			m.count = 1;
 		}
 	}
 	
 	public static boolean match(ASTDmdNode o1, ASTDmdNode o2,
 			TemplateDeclaration tempdecl, Scope sc, SemanticContext context) {
 		Type t1 = isType(o1);
 		Type t2 = isType(o2);
 		Expression e1 = isExpression(o1);
 		Expression e2 = isExpression(o2);
 		Dsymbol s1 = isDsymbol(o1);
 		Dsymbol s2 = isDsymbol(o2);
 		Tuple v1 = isTuple(o1);
 		Tuple v2 = isTuple(o2);
 
 		/* A proper implementation of the various equals() overrides
 		 * should make it possible to just do o1.equals(o2), but
 		 * we'll do that another day.
 		 */
 
 		if (t1 != null) {
 			/* if t1 is an instance of ti, then give error
 			 * about recursive expansions.
 			 */
 			Dsymbol s = t1.toDsymbol(sc, context);
 			if (s != null && s.parent != null) {
 				TemplateInstance ti1 = s.parent.isTemplateInstance();
 				if (ti1 != null && ti1.tempdecl == tempdecl) {
 					for (Scope sc1 = sc; sc1 != null; sc1 = sc1.enclosing) {
 						if (sc1.scopesym == ti1) {
 							if (context.acceptsErrors()) {
 								context.acceptProblem(Problem.newSemanticTypeError(
 										IProblem.RecursiveTemplateExpansionForTemplateArgument, t1, t1.toChars(context)));
 							}
 							return true; // fake a match
 						}
 					}
 				}
 			}
 
 			if (null == t2 || !t1.equals(t2)) {
 				// goto Lnomatch;
 				return false;
 			}
 		} else if (e1 != null) {
 			if (null == e2) {
 				// goto Lnomatch;
 				return false;
 			}
 			if (!e1.equals(e2, context)) {
 				// goto Lnomatch;
 				return false;
 			}
 		} else if (s1 != null) {
 			if (null == s2 || !s1.equals(s2) || s1.parent != s2.parent) {
 				// goto Lnomatch;
 				return false;
 			}
 			if (context.isD2()) {
 				VarDeclaration _v1 = s1.isVarDeclaration();
 				VarDeclaration _v2 = s2.isVarDeclaration();
 				if (_v1 != null && _v2 != null && (_v1.storage_class & _v2.storage_class & STCmanifest) != 0)
 				{   ExpInitializer ei1 = _v1.init.isExpInitializer();
 				    ExpInitializer ei2 = _v2.init.isExpInitializer();
 				    if (ei1 != null && ei2 != null && !ei1.exp.equals(ei2.exp)) {
 				    	// goto Lnomatch;
 				    	return false;
 				    }
 				}
 			}
 		} else if (v1 != null) {
 			if (null == v2) {
 				// goto Lnomatch;
 				return false;
 			}
 			if (size(v1.objects) != size(v2.objects)) {
 				// goto Lnomatch;
 				return false;
 			}
 			for (int i = 0; i < size(v1.objects); i++) {
 				if (!match(v1.objects.get(i),
 						v2.objects.get(i), tempdecl, sc, context)) {
 					// goto Lnomatch;
 					return false;
 				}
 			}
 		}
 		return true; // match
 		//	Lnomatch:
 		//	    return 0;	// nomatch;
 	}
 
 	/**
 	 * Returns the size of a list which may ne <code>null</code>.
 	 * In such case, 0 is returned.
 	 */
 	public static int size(List list) {
 		return list == null ? 0 : list.size();
 	}
 	
 	/**
 	 * If an error has to be marked in this node, returns
 	 * the start position to mark this error. For example,
 	 * if this is a ClassDeclaration, it returns the position
 	 * where it's name starts. If this is an anonymous EnumDeclaration,
 	 * the start of the "enum" keyword is returned.
 	 * 
 	 * By default, the start of this node is returned. Subclasses
 	 * may override.
 	 * 
 	 * The length can be obtained with {@link #getErrorLength()}.
 	 */
 	public int getErrorStart() {
 		return start;
 	}
 	
 	/**
 	 * By default, the length of this node is returned. Subclasses
 	 * may override.
 	 * 
 	 * @see #getErrorStart()
 	 */
 	public int getErrorLength() {
 		return length;
 	}
 	
 	public final void copySourceRange(ASTDmdNode other) {
 		this.start = other.start;
 		this.length = other.length;
 	}
 	
 	public final void copySourceRange(ASTDmdNode first, ASTDmdNode last) {
 		int newLength = last.start + last.length - first.start;
 		if (newLength <= 0) {
 			return;
 		}
 		
 		this.start = first.start;
 		this.length = last.start + last.length - first.start;
 	}
 	
 	public void copySourceRange(List<? extends ASTDmdNode> a) {
 		if (a.isEmpty()) {
 			return;
 		} else {
 			copySourceRange(a.get(0), a.get(a.size() - 1));
 		}
 	}
 	
 	protected char[] getFQN(Identifiers packages, IdentifierExp id) {
 		// TODO Descent char[] optimize, don't use StringBuilder
 		StringBuilder sb = new StringBuilder();
 		if (packages != null) {
 			for(int i = 0; i < packages.size(); i++) {
 				sb.append(packages.get(i).toCharArray());
 				sb.append('.');
 			}
 		}
 		
 		if (id != null) {
 			sb.append(id.toCharArray());
 		}
 		
 		char[] ret = new char[sb.length()];
 		sb.getChars(0, sb.length(), ret, 0);
 		return ret;	
 	}
 	
 	public static void realToMangleBuffer(OutBuffer buf, real_t value) {
 		/* Rely on %A to get portable mangling.
 	     * Must munge result to get only identifier characters.
 	     *
 	     * Possible values from %A	=> mangled result
 	     * NAN			=> NAN
 	     * -INF			=> NINF
 	     * INF			=> INF
 	     * -0X1.1BC18BA997B95P+79	=> N11BC18BA997B95P79
 	     * 0X1.9P+2			=> 19P2
 	     */
 
 	    if (value.isNaN()) {
 			buf.writestring("NAN");	// no -NAN bugs
 		} else
 	    {
 			char[] buffer = value.toString().toCharArray();
 			for (int i = 0; i < buffer.length; i++)
 			{   char c = buffer[i];
 	
 			    switch (c)
 			    {
 				case '-':
 				    buf.writeByte('N');
 				    break;
 	
 				case '+':
 				case 'X':
 				case '.':
 				    break;
 	
 				case '0':
 				    if (i < 2) {
 						break;		// skip leading 0X
 					}
 				default:
 				    buf.writeByte(c);
 				    break;
 			    }
 			}
 	    }
 	}
 	
 	public int getLineNumber() {
 		return -1;
 	}
 	
 	public void setLineNumber(int lineNumber) {
 		// empty
 	}
 	
 	public static final boolean equals(IdentifierExp e1, IdentifierExp e2) {
 		if (e1 == null && e2 == null) {
 			return true;
 		} 
 		if ((e1 == null) != (e2 == null)) {
 			return false;
 		}
 		return equals(e1.ident, e2.ident);
 	}
 	
 	public static final boolean equals(char[] e1, char[] e2) {
 		return CharOperation.equals(e1, e2);
 	}
 	
 	public static final boolean equals(char[] e1, IdentifierExp e2) {
 		if (e2 == null) {
 			return false;
 		}
 		return CharOperation.equals(e1, e2.ident);
 	}
 	
 	public static final boolean equals(IdentifierExp e1, char[] e2) {
 		if (e1 == null) {
 			return false;
 		}
 		return CharOperation.equals(e1.ident, e2);
 	}
 	
 	public static final boolean same(Type t1, Type t2, SemanticContext context) {
 		if (t1 == null && t2 == null) {
 			return true;
 		} 
 		if ((t1 == null) != (t2 == null)) {
 			return false;
 		}
 		
 		return t1.same(t2);	
 	}
 	
 	public static int templateIdentifierLookup(IdentifierExp id, TemplateParameters parameters) {
 		for (int i = 0; i < size(parameters); i++) {
 			TemplateParameter tp = parameters.get(i);
 
 			if (equals(tp.ident, id)) {
 				return i;
 			}
 		}
 
 		return -1;
 	}
 	
 	public static int templateParameterLookup(Type tparam,
 			TemplateParameters parameters) {
 		if (tparam.ty != Tident) {
 			throw new IllegalStateException("assert(tparam.ty == Tident);");
 		}
 		TypeIdentifier tident = (TypeIdentifier) tparam;
 		if (size(tident.idents) == 0) {
 			return templateIdentifierLookup(tident.ident, parameters);
 		}
 		return -1;
 	}
 	
 	public static Expression eval_builtin(BUILTIN builtin,
 			Expressions arguments, SemanticContext context) {
 		if (size(arguments) == 0) {
 			return null;
 		}
 		
 		Expression arg0 = arguments.get(0);
 		Expression e = null;
 		switch (builtin) {
 		case BUILTINsin:
 			if (arg0.op == TOKfloat64) {
 				e = new RealExp(null, 0, arg0.toReal(context).sin(),
 						context.isD2() ? arg0.type : Type.tfloat80);
 			}
 			break;
 
 		case BUILTINcos:
 			if (arg0.op == TOKfloat64) {
 				e = new RealExp(null, 0, arg0.toReal(context).cos(),
 						context.isD2() ? arg0.type : Type.tfloat80);
 			}
 			break;
 
 		case BUILTINtan:
 			if (arg0.op == TOKfloat64) {
 				e = new RealExp(null, 0, arg0.toReal(context).tan(),
 						context.isD2() ? arg0.type : Type.tfloat80);
 			}
 			break;
 
 		case BUILTINsqrt:
 			if (arg0.op == TOKfloat64) {
 				e = new RealExp(null, 0, arg0.toReal(context).sqrt(),
 						context.isD2() ? arg0.type : Type.tfloat80);
 			}
 			break;
 
 		case BUILTINfabs:
 			if (arg0.op == TOKfloat64) {
 				e = new RealExp(null, 0, arg0.toReal(context).abs(),
 						context.isD2() ? arg0.type : Type.tfloat80);
 			}
 			break;
 		}
 		return e;
 	}
 	
 	/*************************************************************
 	 * Now that we have the right function f, we need to get the
 	 * right 'this' pointer if f is in an outer class, but our
 	 * existing 'this' pointer is in an inner class.
 	 * This code is analogous to that used for variables
 	 * in DotVarExp::semantic().
 	 */
 	public static Expression getRightThis(char[] filename, int lineNumber, Scope sc,
 			AggregateDeclaration ad, Expression e1, Declaration var,
 			SemanticContext context) {
 		boolean gotoL1 = true;
 	L1:
 		while(gotoL1) {
 			gotoL1 = false;
 			
 			Type t = e1.type.toBasetype(context);
 	
 			if (ad != null
 					&& !(t.ty == Tpointer && t.nextOf().ty == Tstruct && ((TypeStruct) t.nextOf()).sym == ad)
 					&& !(t.ty == Tstruct && ((TypeStruct) t).sym == ad)) {
 				ClassDeclaration cd = ad.isClassDeclaration();
 				ClassDeclaration tcd = t.isClassHandle();
 	
 				if (null == cd || null == tcd
 						|| !(tcd == cd || cd.isBaseOf(tcd, null, context))) {
 					if (tcd != null && tcd.isNested()) { // Try again with outer scope
 	
 						e1 = new DotVarExp(filename, lineNumber, e1, tcd.vthis);
 						e1.type = tcd.vthis.type;
 //						e1 = e1.semantic(sc, context);
 	
 						// Skip over nested functions, and get the enclosing
 						// class type.
 						int n = 0;
 						Dsymbol s;
 						for(s = tcd.toParent(); s != null && s.isFuncDeclaration() != null; s = s.toParent()) {
 							FuncDeclaration f = s.isFuncDeclaration();
 							if (f.vthis != null) {
 								n++;
 								e1 = new VarExp(filename, lineNumber, f.vthis);
 							}
 							s = s.toParent();
 						}
 						if (s != null && s.isClassDeclaration() != null) {
 							e1.type = s.isClassDeclaration().type;
 							if (n > 1) {
 								e1 = e1.semantic(sc, context);
 							}
 						} else {
 							e1 = e1.semantic(sc, context);
 						}
 						// goto L1;
 						gotoL1 = true;
 						continue L1;
 					}
 					if (context.acceptsErrors()) {
 						context.acceptProblem(Problem.newSemanticTypeError(IProblem.ThisForSymbolNeedsToBeType, var, var
 								.toChars(context), ad.toChars(context), t.toChars(context)));
 					}
 				}
 			}
 		}
 		return e1;
 	}
 	
 	public void errorOnModifier(int problemId, TOK tok, SemanticContext context) {
 		boolean reported = false;
 		
 		List<Modifier> modifiers;
 		if ((modifiers = context.Module_rootModule.getModifiers(this)) != null) {
 			for (Modifier modifier : modifiers) {
 				if (modifier.tok == tok) {
 					if (context.acceptsErrors()) {
 						context.acceptProblem(Problem.newSemanticTypeError(
 								problemId, modifier));
 					}
 					reported = true;
 				}
 			}
 		}
 		
 		List<Modifier> extraModifiers;
 		if ((extraModifiers = context.getExtraModifiers(this)) != null) {
 			for (Modifier modifier : extraModifiers) {
 				if (modifier.tok == tok) {
 					if (context.acceptsErrors()) {
 						context.acceptProblem(Problem.newSemanticTypeError(
 								problemId, modifier));
 					}
 					reported = true;
 				}
 			}
 		}
 		
 		if (!reported) {
 			if (context.acceptsErrors()) {
 				context.acceptProblem(Problem.newSemanticTypeErrorLoc(
 						problemId, this));
 			}
 		}
 	}
 	
 	/**************************************
 	 * Combine types.
 	 * Output:
 	 *	*pt	merged type, if *pt is not NULL
 	 *	*pe1	rewritten e1
 	 *	*pe2	rewritten e2
 	 * Returns:
 	 *	!=0	success
 	 *	0	failed
 	 */
 	public static boolean typeMerge(Scope sc, Expression e, Type[] pt,
 			Expression[] pe1, Expression[] pe2, SemanticContext context) {
 		Expression e1 = pe1[0].integralPromotions(sc, context);
 		Expression e2 = pe2[0].integralPromotions(sc, context);
 
 		Type t1 = e1.type;
 		Type t2 = e2.type;
 		assert (t1 != null);
 		Type t = t1;
 
 		assert (t2 != null);
 
 		Type t1b = t1.toBasetype(context);
 		Type t2b = t2.toBasetype(context);
 
 		TY ty = (TY) Type.impcnvResult[t1b.ty.ordinal()][t2b.ty.ordinal()];
 		if (ty != Terror) {
 			TY ty1;
 			TY ty2;
 
 			ty1 = (TY) Type.impcnvType1[t1b.ty.ordinal()][t2b.ty.ordinal()];
 			ty2 = (TY) Type.impcnvType2[t1b.ty.ordinal()][t2b.ty.ordinal()];
 
 			if (t1b.ty == ty1) // if no promotions
 			{
 				if (t1 == t2) {
 					t = t1;
 					// goto Lret;
 					return typeMerge_Lret(pt, pe1, pe2, t2b, e1, e2);
 				}
 
 				if (t1b == t2b) {
 					t = t1b;
 					// goto Lret;
 					return typeMerge_Lret(pt, pe1, pe2, t2b, e1, e2);
 				}
 			}
 
 			t = Type.basic[ty.ordinal()];
 
 			t1 = Type.basic[ty1.ordinal()];
 			t2 = Type.basic[ty2.ordinal()];
 			e1 = e1.castTo(sc, t1, context);
 			e2 = e2.castTo(sc, t2, context);
 			// goto Lret;
 			return typeMerge_Lret(pt, pe1, pe2, t2b, e1, e2);
 		}
 
 		t1 = t1b;
 		t2 = t2b;
 
 		boolean repeat = true;
 		Lagain: while (repeat) {
 			repeat = false;
 
 			if (same(t1, t2, context)) {
 			} else if (t1.ty == Tpointer && t2.ty == Tpointer) {
 				// Bring pointers to compatible type
 				Type t1n = t1.nextOf();
 				Type t2n = t2.nextOf();
 
 				if (same(t1n, t2n, context))
 					;
 				else if (t1n.ty == Tvoid) // pointers to void are always
 											// compatible
 					t = t2;
 				else if (t2n.ty == Tvoid)
 					;
 				else if (t1n.mod != t2n.mod) {
 					t1 = t1n.mutableOf(context).constOf(context).pointerTo(
 							context);
 					t2 = t2n.mutableOf(context).constOf(context).pointerTo(
 							context);
 					t = t1;
 					// goto Lagain;
 					repeat = true;
 					continue Lagain;
 				} else if (t1n.ty == Tclass && t2n.ty == Tclass) {
 					ClassDeclaration cd1 = t1n.isClassHandle();
 					ClassDeclaration cd2 = t2n.isClassHandle();
 					int[] offset = { 0 };
 
 					if (cd1.isBaseOf(cd2, offset, context)) {
 						if (offset[0] != 0)
 							e2 = e2.castTo(sc, t, context);
 					} else if (cd2.isBaseOf(cd1, offset, context)) {
 						t = t2;
 						if (offset[0] != 0)
 							e1 = e1.castTo(sc, t, context);
 					} else {
 						// goto Lincompatible;
 						return false;
 					}
 				} else {
 					// goto Lincompatible;
 					return false;
 				}
 			} else if ((t1.ty == Tsarray || t1.ty == Tarray)
 					&& e2.op == TOKnull && t2.ty == Tpointer
 					&& t2.nextOf().ty == Tvoid) {
 				/*
 				 * (T[n] op void) (T[] op void)
 				 */
 				// goto Lx1;
 				return typeMerge_Lx1(pt, pe1, pe2, t2b, e1, e2, t1b, sc,
 						context);
 			} else if ((t2.ty == Tsarray || t2.ty == Tarray)
 					&& e1.op == TOKnull && t1.ty == Tpointer
 					&& t1.nextOf().ty == Tvoid) {
 				/*
 				 * (void op T[n]) (void op T[])
 				 */
 				// goto Lx2;
 				return typeMerge_Lx2(pt, pe1, pe2, t1b, e1, e2, t2b, sc,
 						context);
 			} else if ((t1.ty == Tsarray || t1.ty == Tarray)
 					&& t1.implicitConvTo(t2, context) != MATCHnomatch) {
 				// goto Lt2;
 				return typeMerge_Lt2(pt, pe1, pe2, t1b, e1, e2, sc, t2b,
 						context);
 			} else if ((t2.ty == Tsarray || t2.ty == Tarray)
 					&& t2.implicitConvTo(t1, context) != MATCHnomatch) {
 				// goto Lt1;
 				return typeMerge_Lt1(pt, pe1, pe2, t2b, e1, e2, sc, t1b,
 						context);
 			}
 			/*
 			 * If one is mutable and the other invariant, then retry with both
 			 * of them as const
 			 */
 			else if ((t1.ty == Tsarray || t1.ty == Tarray || t1.ty == Tpointer)
 					&& (t2.ty == Tsarray || t2.ty == Tarray || t2.ty == Tpointer)
 					&& t1.nextOf().mod != t2.nextOf().mod) {
 
 				if (t1.ty == Tpointer)
 					t1 = t1.nextOf().mutableOf(context).constOf(context)
 							.pointerTo(context);
 				else
 					t1 = t1.nextOf().mutableOf(context).constOf(context)
 							.arrayOf(context);
 
 				if (t2.ty == Tpointer)
 					t2 = t2.nextOf().mutableOf(context).constOf(context)
 							.pointerTo(context);
 				else
 					t2 = t2.nextOf().mutableOf(context).constOf(context)
 							.arrayOf(context);
 				t = t1;
 				// goto Lagain;
 				repeat = true;
 				continue Lagain;
 			} else if (t1.ty == Tclass || t2.ty == Tclass) {
 				while (true) {
 					int i1 = e2.implicitConvTo(t1, context).ordinal();
 					int i2 = e1.implicitConvTo(t2, context).ordinal();
 
 					if (i1 != 0 && i2 != 0) {
 						// We have the case of class vs. void*, so pick class
 						if (t1.ty == Tpointer)
 							i1 = 0;
 						else if (t2.ty == Tpointer)
 							i2 = 0;
 					}
 
 					if (i2 != 0) {
 						// goto Lt2;
 						return typeMerge_Lt2(pt, pe1, pe2, t1b, e1, e2, sc,
 								t2b, context);
 					} else if (i1 != 0) {
 						// goto Lt1;
 						return typeMerge_Lt1(pt, pe1, pe2, t2b, e1, e2, sc,
 								t1b, context);
 					} else if (t1.ty == Tclass && t2.ty == Tclass) {
 						TypeClass tc1 = (TypeClass) t1;
 						TypeClass tc2 = (TypeClass) t2;
 
 						/*
 						 * Pick 'tightest' type
 						 */
 						ClassDeclaration cd1 = tc1.sym.baseClass;
 						ClassDeclaration cd2 = tc2.sym.baseClass;
 
 						if (cd1 != null && cd2 != null) {
 							t1 = cd1.type;
 							t2 = cd2.type;
 						} else if (cd1 != null)
 							t1 = cd1.type;
 						else if (cd2 != null)
 							t2 = cd2.type;
 						else {
 							// goto Lincompatible;
 							return false;
 						}
 					} else {
 						// goto Lincompatible;
 						return false;
 					}
 				}
 			} else if (t1.ty == Tstruct && t2.ty == Tstruct) {
 				if (((TypeStruct) t1).sym != ((TypeStruct) t2).sym) {
 					// goto Lincompatible;
 					return false;
 				}
 			} else if ((e1.op == TOKstring || e1.op == TOKnull)
 					&& e1.implicitConvTo(t2, context) != MATCHnomatch) {
 				// goto Lt2;
 				return typeMerge_Lt2(pt, pe1, pe2, t1b, e1, e2, sc, t2b,
 						context);
 			}
 
 			else if ((e2.op == TOKstring || e2.op == TOKnull)
 					&& e2.implicitConvTo(t1, context) != MATCHnomatch) {
 				// goto Lt1;
 				return typeMerge_Lt1(pt, pe1, pe2, t2b, e1, e2, sc, t1b,
 						context);
 			} else if (t1.ty == Tsarray
 					&& t2.ty == Tsarray
 					&& e2.implicitConvTo(t1.nextOf().arrayOf(context), context) != MATCHnomatch) {
 				// Lx1:
 				return typeMerge_Lx1(pt, pe1, pe2, t2b, e1, e2, t1b, sc,
 						context);
 			} else if (t1.ty == Tsarray
 					&& t2.ty == Tsarray
 					&& e1.implicitConvTo(t2.nextOf().arrayOf(context), context) != MATCHnomatch) {
 				// Lx2:
 				return typeMerge_Lx2(pt, pe1, pe2, t1b, e1, e2, t2b, sc,
 						context);
 			} else if (t1.isintegral() && t2.isintegral()) {
 				throw new IllegalStateException();
 			} else if (e1.op == TOKslice && t1.ty == Tarray
 					&& e2.implicitConvTo(t1.nextOf(), context) != MATCHnomatch) {
 				// T[] op T
 				e2 = e2.castTo(sc, t1.nextOf(), context);
 				t = t1.nextOf().arrayOf(context);
 			} else if (e2.op == TOKslice && t2.ty == Tarray
 					&& e1.implicitConvTo(t2.nextOf(), context) != MATCHnomatch) {
 				// T op T[]
 				e1 = e1.castTo(sc, t2.nextOf(), context);
 				t = t2.nextOf().arrayOf(context);
 
 				e1 = e1.optimize(WANTvalue, context);
 				if (e != null && e.isCommutative() && e1.isConst()) {
 					/*
 					 * Swap operands to minimize number of functions generated
 					 */
 					Expression tmp = e1;
 					e1 = e2;
 					e2 = tmp;
 				}
 			} else {
 				// Lincompatible:
 				return false;
 			}
 		}
 
 		return typeMerge_Lret(pt, pe1, pe2, t2b, e1, e2);
 	}
 	
 	private static boolean typeMerge_Lt1(Type[] pt, Expression[] pe1, Expression[] pe2, Type t, Expression e1, Expression e2, Scope sc, Type t1, SemanticContext context) {
 		e2 = e2.castTo(sc, t1, context);
 		t = t1;
 		return typeMerge_Lret(pt, pe1, pe2, t, e1, e2);
 	}
 	
 	private static boolean typeMerge_Lt2(Type[] pt, Expression[] pe1, Expression[] pe2, Type t, Expression e1, Expression e2, Scope sc, Type t2, SemanticContext context) {
 		e1 = e1.castTo(sc, t2, context);
 		t = t2;
 		return typeMerge_Lret(pt, pe1, pe2, t, e1, e2);
 	}
 	
 	private static boolean typeMerge_Lx1(Type[] pt, Expression[] pe1, Expression[] pe2, Type t, Expression e1, Expression e2, Type t1, Scope sc, SemanticContext context) {
 		t = t1.nextOf().arrayOf(context);
 		e1 = e1.castTo(sc, t, context);
 		e2 = e2.castTo(sc, t, context);
 		
 		return typeMerge_Lret(pt, pe1, pe2, t, e1, e2);
 	}
 	
 	private static boolean typeMerge_Lx2(Type[] pt, Expression[] pe1, Expression[] pe2, Type t, Expression e1, Expression e2, Type t2, Scope sc, SemanticContext context) {
 		t = t2.nextOf().arrayOf(context);
 		e1 = e1.castTo(sc, t, context);
 		e2 = e2.castTo(sc, t, context);
 		
 		return typeMerge_Lret(pt, pe1, pe2, t, e1, e2);
 	}
 	
 	private static boolean typeMerge_Lret(Type[] pt, Expression[] pe1, Expression[] pe2, Type t, Expression e1, Expression e2) {
		if (pt[0] == null) {
 			pt[0] = t;
 		}
 		pe1[0] = e1;
 		pe2[0] = e2;
 		return true;
 	}
 	
 	/***********************************
 	 * See if both types are arrays that can be compared
 	 * for equality. Return !=0 if so.
 	 * If they are arrays, but incompatible, issue error.
 	 * This is to enable comparing things like an immutable
 	 * array with a mutable one.
 	 */
 	public boolean arrayTypeCompatible(char[] filename, int lineNumber, Type t1, Type t2,
 			SemanticContext context) {
 		t1 = t1.toBasetype(context);
 		t2 = t2.toBasetype(context);
 
 		if ((t1.ty == Tarray || t1.ty == Tsarray || t1.ty == Tpointer)
 				&& (t2.ty == Tarray || t2.ty == Tsarray || t2.ty == Tpointer)) {
 			if (t1.nextOf().implicitConvTo(t2.nextOf(), context).ordinal() < MATCHconst
 					.ordinal()
 					&& t2.nextOf().implicitConvTo(t1.nextOf(), context)
 							.ordinal() < MATCHconst.ordinal()
 					&& (t1.nextOf().ty != Tvoid && t2.nextOf().ty != Tvoid)) {
 				if (context.acceptsErrors()) {
 					// SEMANTIC missing ok location
 					context.acceptProblem(Problem.newSemanticTypeError(IProblem.ArrayEqualityComparisonTypeMismatch, t1, t1.toChars(context), t2.toChars(context)));
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	/*******************************************
 	 * Given a symbol that could be either a FuncDeclaration or
 	 * a function template, resolve it to a function symbol.
 	 *	sc		instantiation scope
 	 *	loc		instantiation location
 	 *	targsi		initial list of template arguments
 	 *	ethis		if !NULL, the 'this' pointer argument
 	 *	fargs		arguments to function
 	 *	flags		1: do not issue error message on no match, just return NULL
 	 */
 	public static FuncDeclaration resolveFuncCall(Scope sc, char[] filename,
 			int lineNumber, Dsymbol s, Objects tiargs, Expression ethis,
 			Expressions arguments, int flags, SemanticContext context) {
 		if (null == s)
 			return null; // no match
 		FuncDeclaration f = s.isFuncDeclaration();
 		if (f != null)
 			f = f.overloadResolve(filename, lineNumber, ethis, arguments,
 					context, ethis); // SEMANTIC ethis is caller?
 		else {
 			TemplateDeclaration td = s.isTemplateDeclaration();
 			f = td.deduceFunctionTemplate(sc, filename, lineNumber, tiargs,
 					null, arguments, flags, context);
 		}
 		return f;
 	}
 	
 	public int getStart() {
 		return start;
 	}
 	
 	public void setStart(int start) {
 		this.start = start;
 	}
 	
 }
