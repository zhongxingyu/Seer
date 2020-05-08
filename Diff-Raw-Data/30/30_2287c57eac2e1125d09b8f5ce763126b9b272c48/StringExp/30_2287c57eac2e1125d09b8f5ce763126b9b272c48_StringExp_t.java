 package descent.internal.compiler.parser;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 
 import descent.internal.compiler.parser.ast.IASTVisitor;
 
 import static descent.internal.compiler.parser.MATCH.MATCHexact;
 import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
 
import static descent.internal.compiler.parser.TY.Tarray;
 import static descent.internal.compiler.parser.TY.Tchar;
 import static descent.internal.compiler.parser.TY.Tdchar;
import static descent.internal.compiler.parser.TY.Tdelegate;
 import static descent.internal.compiler.parser.TY.Tpointer;
 import static descent.internal.compiler.parser.TY.Tsarray;
 import static descent.internal.compiler.parser.TY.Tvoid;
 import static descent.internal.compiler.parser.TY.Twchar;
 
 // DMD 1.020
 public class StringExp extends Expression {
 
 	public char[] sourceString;
 
 	public char[] string;
 	public int len;
 	public char postfix;
 	public int sz; // 1: char, 2: wchar, 4: dchar
 	public boolean committed; // !=0 if type is committed
 
 	public List<StringExp> allStringExps;
 
 	public StringExp(Loc loc, char[] string) {
 		this(loc, string, string.length);
 	}
 
 	public StringExp(Loc loc, char[] string, int len) {
 		this(loc, string, len, (char) 0);
 	}
 
 	public StringExp(Loc loc, char[] string, int len, char postfix) {
 		super(loc, TOK.TOKstring);
 		this.string = string;
 		this.len = len;
 		this.sz = 1;
 		this.committed = false;
 		this.postfix = postfix;
 	}
 
 	@Override
 	public void accept0(IASTVisitor visitor) {
 		visitor.visit(this);
 		visitor.endVisit(this);
 	}
 
 	@Override
 	public String toChars(SemanticContext context) {
 		OutBuffer out = new OutBuffer();
 		HdrGenState hdr = new HdrGenState();
 		toCBuffer(out, hdr, context);
 		return out.toChars();
 	}
 
 	@Override
 	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
 			SemanticContext context) {
 		// TODO semantic: this is not the real implementation
 		buf.data.append("\"");
 		buf.data.append(this.string);
 		buf.data.append("\"");
 	}
 
 	public StringExp toUTF8(Scope sc, SemanticContext context) {
 		if (sz != 1) {
 			// Convert to UTF-8 string
 			committed = false;
 			Expression e = castTo(sc, Type.tchar.arrayOf(context), context);
 			e = e.optimize(WANTvalue, context);
 			Assert.isTrue(e.op == TOK.TOKstring);
 			StringExp se = (StringExp) e;
 			Assert.isTrue(se.sz == 1);
 			return se;
 		}
 		return this;
 	}
 
 	@Override
 	public Expression semantic(Scope sc, SemanticContext context) {
 		if (type == null) {
 			OutBuffer buffer = new OutBuffer();
 			int newlen = 0;
 			String p;
 			int[] u = { 0 };
 			int[] c = { 0 };
 
 			switch (postfix) {
 			case 'd':
 				for (u[0] = 0; u[0] < len;) {
 					p = Utf.decodeChar(string, 0, len, u, c);
 					// utf_decodeChar((unsigned char )string, len, &u, &c);
 					if (p != null) {
 						error("%s", p);
 						break;
 					} else {
 						buffer.write4(c[0]);
 						newlen++;
 					}
 				}
 				buffer.write4(0);
 				string = buffer.extractData().toCharArray();
 				len = newlen;
 				sz = 4;
 				type = new TypeSArray(Type.tdchar, new IntegerExp(loc, len,
 						Type.tindex));
 				committed = true;
 				break;
 
 			case 'w':
 				for (u[0] = 0; u[0] < len;) {
 					p = Utf.decodeChar(string, 0, len, u, c);
 					if (p != null) {
 						error("%s", p);
 						break;
 					} else {
 						buffer.writeUTF16(c[0]);
 						newlen++;
 						if (c[0] >= 0x10000) {
 							newlen++;
 						}
 					}
 				}
 				buffer.writeUTF16(0);
 				string = buffer.extractData().toCharArray();
 				len = newlen;
 				sz = 2;
 				type = new TypeSArray(Type.twchar, new IntegerExp(loc, len,
 						Type.tindex));
 				committed = true;
 				break;
 
 			case 'c':
 				committed = true;
 			default:
 				type = new TypeSArray(Type.tchar, new IntegerExp(loc, len,
 						Type.tindex));
 				break;
 			}
 			type = type.semantic(loc, sc, context);
 		}
 		return this;
 	}
 
 	@Override
 	public int getNodeType() {
 		return STRING_EXP;
 	}
 
 	@Override
 	public MATCH implicitConvTo(Type t, SemanticContext context) {
 		if (!committed) {
 			if (!committed && t.ty == Tpointer && t.next.ty == Tvoid) {
 				return MATCHnomatch;
 			}
 			if (type.ty == Tsarray || type.ty == Tarray || type.ty == Tpointer) {
 				if (type.next.ty == Tchar) {
 					switch (t.ty) {
 					case Tsarray:
 						if (type.ty == Tsarray
 								&& ((TypeSArray) type).dim.toInteger(context) != ((TypeSArray) t).dim
 										.toInteger(context)) {
 							return MATCHnomatch;
 						}
 					case Tarray:
 					case Tpointer:
 						if (t.next.ty == Tchar) {
 							return MATCHexact;
 						} else if (t.next.ty == Twchar) {
 							return MATCHexact;
 						} else if (t.next.ty == Tdchar) {
 							return MATCHexact;
 						}
 						break;
 					}
 				}
 			}
 		}
 		return super.implicitConvTo(t, context);
 	}
 
 	@Override
 	public boolean isBool(boolean result) {
 		return result ? true : false;
 	}
 	
 	@Override
 	public Expression interpret(InterState istate, SemanticContext context) {
 		return this;
 	}
 
 	@Override
 	public Expression castTo(Scope sc, Type t, SemanticContext context) {
 		StringExp se;
 		Type tb;
 		int unique;
 
 		if (!committed && t.ty == Tpointer && t.next.ty == Tvoid) {
 			error("cannot convert string literal to void*");
 		}
 
 		tb = t.toBasetype(context);
 		if (tb.ty == Tdelegate && type.toBasetype(context).ty != Tdelegate) {
 			return super.castTo(sc, t, context);
 		}
 
 		se = this;
 		unique = 0;
 		if (!committed) {
 			// Copy when committing the type
 			char[] s;
 			// TODO see if the following translation is ok, and if a copy is needed
 			// s = mem.malloc((len + 1) * sz);
 			// memcpy(s, string, (len + 1) * sz);
 			s = Arrays.copyOf(string, string.length);
 			se = new StringExp(loc, s, len);
 			se.type = type;
 			se.sz = sz;
 			se.committed = false;
 			unique = 1; // this is the only instance
 		}
 		se.type = type.toBasetype(context);
 		if (tb == se.type) {
 			se.type = t;
 			se.committed = true;
 			return se;
 		}
 
 		if (tb.ty != Tsarray && tb.ty != Tarray && tb.ty != Tpointer) {
 			se.committed = true;
 			// goto Lcast;
 			return castTo_Lcast(se, t);
 		}
 		if (se.type.ty != Tsarray && se.type.ty != Tarray
 				&& se.type.ty != Tpointer) {
 			se.committed = true;
 			// goto Lcast;
 			return castTo_Lcast(se, t);
 		}
 
 		if (se.committed) {
 			if (se.type.next.size(context) == tb.next.size(context)) {
 				se.type = t;
 				return se;
 			}
 			// goto Lcast;
 			return castTo_Lcast(se, t);
 		}
 
 		se.committed = true;
 
 		TY tfty;
 		TY ttty;
 		String p;
 		int[] u = { 0 };
 		int[] c = { 0 };
 		int newlen;
 
 		{
 			OutBuffer buffer = new OutBuffer();
 			newlen = 0;
 			tfty = se.type.next.toBasetype(context).ty;
 			ttty = tb.next.toBasetype(context).ty;
 			
 			int x = X(tfty, ttty);
 			if (x == X(Tchar, Tchar) || 
 				x == X(Twchar, Twchar) ||
 				x == X(Tdchar, Tdchar)) {
 				// break;
 			} else if (x == X(Tchar, Twchar)) {
 				for (u[0] = 0; u[0] < len;) {
 					p = Utf.decodeChar(se.string, 0, len, u, c);
 					if (p != null) {
 						error("%s", p);
 					} else {
 						buffer.writeUTF16(c[0]);
 					}
 				}
 				newlen = buffer.offset() / 2;
 				buffer.writeUTF16(0);
 				// goto L1;
 				if (0 == unique) {
 					se = new StringExp(loc, null, 0);
 				}
 				buffer.data.getChars(0, buffer.offset(),
 						se.string = new char[buffer.offset()], 0);
 				se.len = newlen;
 				se.sz = tb.next.size(context);
 			} else if (x == X(Tchar, Tdchar)) {
 				for (u[0] = 0; u[0] < len;) {
 					p = Utf.decodeChar(se.string, 0, len, u, c);
 					if (p != null) {
 						error("%s", p);
 					}
 					buffer.write4(c[0]);
 					newlen++;
 				}
 				buffer.write4(0);
 				// goto L1;
 				if (0 == unique) {
 					se = new StringExp(loc, null, 0);
 				}
 				buffer.data.getChars(0, buffer.offset(),
 						se.string = new char[buffer.offset()], 0);
 				se.len = newlen;
 				se.sz = tb.next.size(context);
 			} else if (x == X(Twchar, Tchar)) {
 				for (u[0] = 0; u[0] < len;) {
 					p = Utf.decodeWchar(se.string, 0, len, u, c);
 					if (p != null) {
 						error("%s", p);
 					} else {
 						buffer.writeUTF8(c[0]);
 					}
 				}
 				newlen = buffer.offset();
 				buffer.writeUTF8(0);
 				// goto L1;
 				if (0 == unique) {
 					se = new StringExp(loc, null, 0);
 				}
 				buffer.data.getChars(0, buffer.offset(),
 						se.string = new char[buffer.offset()], 0);
 				se.len = newlen;
 				se.sz = tb.next.size(context);
 			} else if (x == X(Twchar, Tdchar)) {
 				for (u[0] = 0; u[0] < len;) {
 					p = Utf.decodeWchar(se.string, 0, len, u, c);
 					if (p != null) {
 						error("%s", p);
 					}
 					buffer.write4(c[0]);
 					newlen++;
 				}
 				buffer.write4(0);
 				// goto L1;
 				if (0 == unique) {
 					se = new StringExp(loc, null, 0);
 				}
 				buffer.data.getChars(0, buffer.offset(),
 						se.string = new char[buffer.offset()], 0);
 				se.len = newlen;
 				se.sz = tb.next.size(context);
 			} else if (x == X(Tdchar, Tchar)) {
 				for (u[0] = 0; u[0] < len; u[0]++) {
 					c[0] = se.string[u[0]];
 					if (!Utf.isValidDchar(c[0])) {
 						error("invalid UCS-32 char \\U%08x", c[0]);
 					} else {
 						buffer.writeUTF8(c[0]);
 					}
 					newlen++;
 				}
 				newlen = buffer.offset();
 				buffer.writeUTF8(0);
 				// goto L1;
 				if (0 == unique) {
 					se = new StringExp(loc, null, 0);
 				}
 				buffer.data.getChars(0, buffer.offset(),
 						se.string = new char[buffer.offset()], 0);
 				se.len = newlen;
 				se.sz = tb.next.size(context);
 			} else if (x == X(Tdchar, Twchar)) {
 				for (u[0] = 0; u[0] < len; u[0]++) {
 					c[0] = se.string[u[0]];
 					if (!Utf.isValidDchar(c[0])) {
 						error("invalid UCS-32 char \\U%08x", c[0]);
 					} else {
 						buffer.writeUTF16(c[0]);
 					}
 					newlen++;
 				}
 				newlen = buffer.offset() / 2;
 				buffer.writeUTF16(0);
 				// goto L1;
 			// L1: 
 				if (0 == unique) {
 					se = new StringExp(loc, null, 0);
 				}
 				buffer.data.getChars(0, buffer.offset(),
 						se.string = new char[buffer.offset()], 0);
 				se.len = newlen;
 				se.sz = tb.next.size(context);
 			} else {
 				if (se.type.next.size(context) == tb.next.size(context)) {
 					se.type = t;
 					return se;
 				}
 				// goto Lcast;
 				return castTo_Lcast(se, t);
 			}
 		}
 
 		// See if need to truncate or extend the literal
 		if (tb.ty == Tsarray) {
 			int dim2 = ((TypeSArray) tb).dim.toInteger(context).intValue();
 
 			// Changing dimensions
 			if (dim2 != se.len) {
 				int newsz = se.sz;
 
 				if (unique == 1 && dim2 < se.len) {
 					se.len = dim2;
 					// Add terminating 0
 					// --> no need in Java
 					// memset((unsigned char *)se.string + dim2 * newsz, 0, newsz);
 				} else {
 					// Copy when changing the string literal
 					char[] s = null;
 					int d;
 
 					d = (dim2 < se.len) ? dim2 : se.len;
 					// TODO semantic
 					// s = (unsigned char *)mem.malloc((dim2 + 1) * newsz);
 					// memcpy(s, se.string, d * newsz);
 					// Extend with 0, add terminating 0
 					// TODO semantic
 					// memset((char *)s + d * newsz, 0, (dim2 + 1 - d) * newsz);
 					se = new StringExp(loc, s, dim2);
 					se.committed = true; // it now has a firm type
 					se.sz = newsz;
 				}
 			}
 		}
 		se.type = t;
 		return se;
 	}
 
 	private Expression castTo_Lcast(StringExp se, Type t) {
 		Expression e = new CastExp(loc, se, t);
 		e.type = t;
 		return e;
 	}
 
 	private int X(TY tf, TY tt) {
 		return ((tf.ordinal()) * 256 + (tt.ordinal()));
 	}
 
 }
