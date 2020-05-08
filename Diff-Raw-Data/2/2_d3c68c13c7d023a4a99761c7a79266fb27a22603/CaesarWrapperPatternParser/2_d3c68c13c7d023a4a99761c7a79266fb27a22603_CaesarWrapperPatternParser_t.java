 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright � 2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package org.caesarj.compiler.aspectj;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.aspectj.weaver.ISourceContext;
 import org.aspectj.weaver.Member;
 import org.aspectj.weaver.Shadow;
 import org.aspectj.weaver.patterns.AndPointcut;
 import org.aspectj.weaver.patterns.ArgsPointcut;
 import org.aspectj.weaver.patterns.CflowPointcut;
 import org.aspectj.weaver.patterns.HandlerPointcut;
 import org.aspectj.weaver.patterns.IToken;
 import org.aspectj.weaver.patterns.ITokenSource;
 import org.aspectj.weaver.patterns.ModifiersPattern;
 import org.aspectj.weaver.patterns.NamePattern;
 import org.aspectj.weaver.patterns.NotPointcut;
 import org.aspectj.weaver.patterns.OrPointcut;
 import org.aspectj.weaver.patterns.ParserException;
 import org.aspectj.weaver.patterns.PatternParser;
 import org.aspectj.weaver.patterns.Pointcut;
 import org.aspectj.weaver.patterns.ReferencePointcut;
 import org.aspectj.weaver.patterns.SignaturePattern;
 import org.aspectj.weaver.patterns.ThisOrTargetPointcut;
 import org.aspectj.weaver.patterns.ThrowsPattern;
 import org.aspectj.weaver.patterns.TypePattern;
 import org.aspectj.weaver.patterns.TypePatternList;
 import org.aspectj.weaver.patterns.WildTypePattern;
 import org.aspectj.weaver.patterns.WithinPointcut;
 import org.aspectj.weaver.patterns.WithincodePointcut;
 
 /**
  * Extends the PatternParser to generate Wrappers for Pointcut objects
  * instead of the objects themselves. It also performs the registration
  * of parsed pointcuts to the CaesarPointcutScope class, so that more 
  * information about the pointcut can be used when resolving them.
  *
  * @author Thiago Tonelli Bartolomei <thiagobart@gmail.com>
  *
  */
 public class CaesarWrapperPatternParser extends PatternParser {
 
 	/**
 	 * Store the tokenSource too
 	 */
 	private ITokenSource tokenSource = null;
 	
 	private ISourceContext sourceContext;
 	
 	/**
 	 * Constructor for PatterParserWrapper.
 	 * Just calls super.
 	 * 
 	 * @param tokenSource the tokensource object
 	 */
 	public CaesarWrapperPatternParser(ITokenSource tokenSource) {
 		super(tokenSource);
 		this.tokenSource = tokenSource;
 		this.sourceContext = tokenSource.getSourceContext();
 	}
 	
     // ----------------------------------------------------------------------
     // CODE COPIED FROM ASPECTJ'S PatternParser AND MODIFIED TO GENERATE WRAPPERS
     // ----------------------------------------------------------------------
 	
 	public CaesarPointcutWrapper parsePointcutWrapper() {		
 		int start = tokenSource.getIndex();
 		IToken t = tokenSource.peek();
 		Pointcut p = t.maybeGetParsedPointcut();
 		if (p != null) {
 			tokenSource.next();
 			return new CaesarPointcutWrapper(p);
 		}
 		
 		String kind = parseIdentifier();
 		tokenSource.setIndex(start);
 		if (kind.equals("execution") || kind.equals("call") || 
 						kind.equals("get") || kind.equals("set")) {
 			return parseKindedPointcut();
 		} else if (kind.equals("args")) {
 			return parseArgsPointcut();
 		} else if (kind.equals("this") || kind.equals("target")) {
 			return parseThisOrTargetPointcut();
 		} else if (kind.equals("within")) {
 			return parseWithinPointcut();
 		} else if (kind.equals("withincode")) {
 			return parseWithinCodePointcut();
 		} else if (kind.equals("cflow")) {
 			return parseCflowPointcut(false);
 		} else if (kind.equals("cflowbelow")) {
 			return parseCflowPointcut(true);
 		} else  if (kind.equals("adviceexecution")) {
 			parseIdentifier(); eat("(");
 			eat(")");
 			
 			// Creates the wrapper 
 			CaesarKindedPointcut pointcut = new CaesarKindedPointcut(Shadow.AdviceExecution,
 					new SignaturePattern(Member.ADVICE, ModifiersPattern.ANY, 
 							TypePattern.ANY, TypePattern.ANY, NamePattern.ANY, 
 							TypePatternList.ANY, 
 							ThrowsPattern.ANY));
 			CaesarPointcutWrapper wrapper = new CaesarPointcutWrapper(pointcut);
 			return wrapper;
 			
 		} else  if (kind.equals("handler")) {
 			parseIdentifier(); eat("(");
 			TypePattern typePat = parseTypePattern();
 			eat(")");
 			
 			// Creates the wrapper 
 			HandlerPointcut pointcut =  new HandlerPointcut(typePat);
 			CaesarPointcutWrapper wrapper = new CaesarPointcutWrapper(pointcut);
 			wrapper.setExceptionType(typePat);
 			return wrapper;
 			
 		} else  if (kind.equals("initialization")) {
 			parseIdentifier(); eat("(");
 			SignaturePattern sig = parseConstructorSignaturePattern();
 			eat(")");
 			
 			// Creates the wrapper
 			CaesarKindedPointcut regular =  new CaesarKindedPointcut(
 					Shadow.Initialization, 
 					sig);
 			
 			return new CaesarPointcutWrapper(regular);
 			
 			/*
 			// Transform the object initialization to the constructor with Object. This will
 			// cause some different semantics than AspectJ	
 
 			CaesarCloner c = CaesarCloner.instance();
 			
 			SignaturePattern cclassSig = 
 				new SignaturePattern(Member.CONSTRUCTOR, sig.getModifiers(),
                     c.clone(sig.getReturnType()), c.clone(sig.getDeclaringType()),
                     c.clone(sig.getName()), createObjectTypeList(),
                     c.clone(sig.getThrowsPattern()));
 			
 			CaesarKindedPointcut regular =  new CaesarKindedPointcut(
 					Shadow.Initialization, 
 					sig);
 			
 			CaesarKindedPointcut cclass =  new CaesarKindedPointcut(
 					Shadow.Initialization, 
 					cclassSig);
 			
 			registerPointcut(new CaesarPointcutWrapper(regular));
 			//registerPointcut(new CaesarPointcutWrapper(cclass));
 			
 			// Creates an orPointcut for both the regular java and cclass constructors
 			Pointcut orPointcut = new OrPointcut(
 					regular,
 					cclass);
 			
 			//return new CaesarPointcutWrapper(orPointcut); 
 			return new CaesarPointcutWrapper(regular);
 			 */
 			
 		} else  if (kind.equals("staticinitialization")) {
 			parseIdentifier(); eat("(");
 			TypePattern typePat = parseTypePattern();
 			eat(")");
 			SignaturePattern sig = new SignaturePattern(Member.STATIC_INITIALIZATION, ModifiersPattern.ANY, 
 					TypePattern.ANY, typePat, NamePattern.ANY, TypePatternList.EMPTY, 
 					ThrowsPattern.ANY);
 
 			// Creates the wrapper and register it
 			CaesarKindedPointcut pt = new CaesarKindedPointcut(Shadow.StaticInitialization, sig);
 			CaesarPointcutWrapper wrapper = new CaesarPointcutWrapper(pt);
 			registerPointcut(wrapper);
 			
 			// Append something like || get(public * Classname_Impl_Mixin_*.field)
 			TypePattern mixinType = createMixinType(sig.getDeclaringType());
 			SignaturePattern mixinSignature = createMixinSignature(sig, mixinType);
 			
 			CaesarKindedPointcut mixin = 
 				new CaesarKindedPointcut(Shadow.StaticInitialization, mixinSignature);
 
 			// Register the mixin
 			wrapper = new CaesarPointcutWrapper(mixin, sig.getDeclaringType());
 			wrapper.setDeclaringType(mixinType);
 			registerPointcut(wrapper);
 			
 			// Creates an orPointcut for both the type and the mixin
 			Pointcut orPointcut = new OrPointcut(
 					pt,
 					mixin);
 			
 			return new CaesarPointcutWrapper(orPointcut);
 
 		} else  if (kind.equals("preinitialization")) {
 			parseIdentifier(); eat("(");
 			SignaturePattern sig = parseConstructorSignaturePattern();
 			eat(")");
 			
 			CaesarCloner c = CaesarCloner.instance();
 			
 			SignaturePattern cclassSig = 
 				new SignaturePattern(Member.CONSTRUCTOR, sig.getModifiers(),
                     c.clone(sig.getReturnType()), c.clone(sig.getDeclaringType()),
                     c.clone(sig.getName()), createObjectTypeList(),
                     c.clone(sig.getThrowsPattern()));
 			
 			CaesarKindedPointcut regular =  new CaesarKindedPointcut(
 					Shadow.PreInitialization, 
 					sig);
 			
 			CaesarKindedPointcut cclass =  new CaesarKindedPointcut(
 					Shadow.PreInitialization, 
 					cclassSig);
 			
 			registerPointcut(new CaesarPointcutWrapper(regular));
 			registerPointcut(new CaesarPointcutWrapper(cclass));
 			
 			// Creates an orPointcut for both the regular java and cclass constructors
 			Pointcut orPointcut = new OrPointcut(
 					regular,
 					cclass);
 			
 			return new CaesarPointcutWrapper(orPointcut); 
 			
 			/*
 			// Transform the object preinitialization to the constructor with Object. This will
 			// cause some different semantics than AspectJ
 			sig = new SignaturePattern(Member.CONSTRUCTOR, sig.getModifiers(),
                     sig.getReturnType(), sig.getDeclaringType(),
                     sig.getName(), createObjectTypeList(),
                     sig.getThrowsPattern());
 			
 			CaesarKindedPointcut pointcut = new CaesarKindedPointcut(Shadow.PreInitialization, sig);
 			return new CaesarPointcutWrapper(pointcut);
 			*/
 		} else {
 			return parseReferencePointcut();
 		}
 	}
 
 	
 	private CaesarPointcutWrapper parseKindedPointcut() {
 		String kind = parseIdentifier();  
 		eat("(");
 		SignaturePattern sig;
 
 		Shadow.Kind shadowKind = null;
 		if (kind.equals("execution")) {
 			sig = parseMethodOrConstructorSignaturePattern();
 			if (sig.getKind() == Member.METHOD) {
 				shadowKind = Shadow.MethodExecution;
 			} else if (sig.getKind() == Member.CONSTRUCTOR) {
 				shadowKind = Shadow.ConstructorExecution;
 			}          
 		} else if (kind.equals("call")) {
 			sig = parseMethodOrConstructorSignaturePattern();
 			if (sig.getKind() == Member.METHOD) {
 				shadowKind = Shadow.MethodCall;
 			} else if (sig.getKind() == Member.CONSTRUCTOR) {
 				shadowKind = Shadow.ConstructorCall;
 			}	          
 		} else if (kind.equals("get")) {
 			sig = parseFieldSignaturePattern();
 			shadowKind = Shadow.FieldGet;
 		} else if (kind.equals("set")) {
 			sig = parseFieldSignaturePattern();
 			shadowKind = Shadow.FieldSet;
 		} else {
 			throw new ParserException("bad kind: " + kind, tokenSource.peek());
 		}
 		eat(")");
 		
 		// Creates the wrapper
 		
 		if(Shadow.MethodCall.equals(shadowKind) || Shadow.MethodExecution.equals(shadowKind)) {
 			
 			// Method call and execution are wrapped in an "and pointcut" to avoid getting constructors
 			CaesarKindedPointcut p =  new CaesarKindedPointcut(shadowKind, sig);
 			registerPointcut(new CaesarPointcutWrapper(p));
 			
 			Pointcut andPointcut = new AndPointcut(
 					p,
 					new NotPointcut(
 							new CaesarKindedPointcut(
 									shadowKind,
 									this.createConstructorSignature()),
 							tokenSource.peek().getStart()));
 			
 			return new CaesarPointcutWrapper(andPointcut);
 		} else if (
 				Shadow.ConstructorCall.equals(shadowKind) || 
 				Shadow.ConstructorExecution.equals(shadowKind)) {
 			
 			Shadow.Kind cclassShadowKind = null;
 			
 			// Transform the constructor call/execution to a method call/execution, 
 			// using $constructor and the same parameters
 			if (Shadow.ConstructorCall.equals(shadowKind)) {
 				cclassShadowKind = Shadow.MethodCall;
 			} else {
 				cclassShadowKind = Shadow.MethodExecution;
 			}
 			
 			CaesarCloner c = CaesarCloner.instance();
 			
 			SignaturePattern cclassSig = 
 				new SignaturePattern(Member.METHOD, sig.getModifiers(),
 					createCaesarObjectPattern(), c.clone(sig.getDeclaringType()),
 					new NamePattern("$constructor"), c.clone(sig.getParameterTypes()),
 					c.clone(sig.getThrowsPattern()));
 			
 			CaesarKindedPointcut regular =  new CaesarKindedPointcut(
 					shadowKind, 
 					sig);
 			
 			CaesarKindedPointcut cclass =  new CaesarKindedPointcut(
 					cclassShadowKind, 
 					cclassSig);
 			
 			registerPointcut(new CaesarPointcutWrapper(regular));
 			registerPointcut(new CaesarPointcutWrapper(cclass));
 			
 			// Creates an orPointcut for both the regular java and cclass constructors
 			Pointcut orPointcut = new OrPointcut(
 					regular,
 					cclass);
 			
 			return new CaesarPointcutWrapper(orPointcut);
 		}
 		
 		if (Shadow.FieldGet.equals(shadowKind) || Shadow.FieldSet.equals(shadowKind)) {
 
 			// Creates the wrapper and register it
 			CaesarKindedPointcut p = new CaesarKindedPointcut(shadowKind, sig);
 			CaesarPointcutWrapper wrapper = new CaesarPointcutWrapper(p);
 			registerPointcut(wrapper);
 			
 			// Append something like || get(public * Classname_Impl_Mixin_*.field)
 			TypePattern mixinType = createMixinType(sig.getDeclaringType());
 			SignaturePattern mixinSignature = createMixinSignature(sig, mixinType);
 			
 			CaesarKindedPointcut mixin = new CaesarKindedPointcut(shadowKind, mixinSignature);
 
 			// Register the mixin
 			wrapper = new CaesarPointcutWrapper(mixin, sig.getDeclaringType());
 			wrapper.setDeclaringType(mixinType);
 			registerPointcut(wrapper);
 			
 			// Creates an orPointcut for both the type and the mixin
 			Pointcut orPointcut = new OrPointcut(
 					p,
 					mixin);
 			
 			return new CaesarPointcutWrapper(orPointcut);
 			
 		}
 		CaesarKindedPointcut p =  new CaesarKindedPointcut(shadowKind, sig);
 		return new CaesarPointcutWrapper(p);
 	}
 	
 	/**
 	 * Method parseArgsPointcut.
 	 * @return Pointcut
 	 */
 	private CaesarPointcutWrapper parseArgsPointcut() {
 		parseIdentifier();
 		TypePatternList arguments = parseArgumentsPattern();
 
 		// Creates the wrapper 
 		ArgsPointcut p = new ArgsPointcut(arguments);
 		CaesarPointcutWrapper wrapper = new CaesarPointcutWrapper(p);
 		return wrapper;
 		
 	}
 	
 	/**
 	 * Method parseThisOrTargetPointcut.
 	 * @return Pointcut
 	 */
 	private CaesarPointcutWrapper parseThisOrTargetPointcut() {
 		String kind = parseIdentifier();
 		eat("(");
 		TypePattern type = parseTypePattern();
 		eat(")");
 		
 		// Creates the wrapper 
 		ThisOrTargetPointcut p = new ThisOrTargetPointcut(kind.equals("this"), type);
 		CaesarPointcutWrapper wrapper = new CaesarPointcutWrapper(p);
 		return wrapper;
 	}
 	
 	/**
 	 * Method parseWithinPointcut.
 	 * @return Pointcut
 	 */
 	private CaesarPointcutWrapper parseWithinPointcut() {
 		parseIdentifier();
 		eat("(");
 		TypePattern type = parseTypePattern();
 		eat(")");
 		
 		// Creates the wrapper and register it
 		WithinPointcut p = new WithinPointcut(type);
 		CaesarPointcutWrapper wrapper = new CaesarPointcutWrapper(p);
 		wrapper.setTypePattern(type);
 		registerPointcut(wrapper);
 		
 		// Creates something like || within(Classname_Impl_Mixin_*)
 		TypePattern mixinType = createMixinType(type);
 
 		WithinPointcut mixin = new WithinPointcut(mixinType);
 
 		// Register the mixin
 		wrapper = new CaesarPointcutWrapper(mixin, type);
		wrapper.setTypePattern(mixinType);
 		registerPointcut(wrapper);
 		
 		// Creates an orPointcut for both the type and the mixin
 		Pointcut orPointcut = new OrPointcut(
 				p,
 				mixin);
 		
 		return new CaesarPointcutWrapper(orPointcut);
 	}
 
 	/**
 	 * Parses a Withincode Pointcut
 	 * 
 	 * @return
 	 */
 	private CaesarPointcutWrapper parseWithinCodePointcut() {
 		
 		// Parses the signature pattern
 		parseIdentifier();
 		eat("(");
 		SignaturePattern sig = parseMethodOrConstructorSignaturePattern();
 		eat(")");
 		
 		// Gets the declaring type
 		TypePattern type = sig.getDeclaringType();
 		
 		// Creates the wrapper and register it
 		Pointcut p = new WithincodePointcut(sig);
 		CaesarPointcutWrapper wrapper = new CaesarPointcutWrapper(p);
 		wrapper.setDeclaringType(type);
 		registerPointcut(wrapper);
 		
 		if (Member.CONSTRUCTOR.equals(sig.getKind())) {
 			
 			// Transform the constructor withincode to a method withincode, 
 			// using $constructor and the same parameters (for cclasses)
 			CaesarCloner c = CaesarCloner.instance();
 			
 			sig = 
 				new SignaturePattern(Member.METHOD, sig.getModifiers(),
 					createCaesarObjectPattern(), c.clone(type),
 					new NamePattern("$constructor"), c.clone(sig.getParameterTypes()),
                     c.clone(sig.getThrowsPattern()));
 			
 			WithincodePointcut cclass =  new WithincodePointcut(sig);
 			wrapper = new CaesarPointcutWrapper(cclass);
 			wrapper.setDeclaringType(sig.getDeclaringType());
 			registerPointcut(wrapper);
 			
 			// Creates an orPointcut for both the regular java and cclass constructors
 			p = new OrPointcut(
 					p,
 					cclass);
 		}
 
 		// Creates something like || withincode(* Classname_Impl_Mixin_*.m())
 		TypePattern mixinType = createMixinType(type);
 		SignaturePattern mixinSignature = createMixinSignature(sig, mixinType);
 		
 		WithincodePointcut mixin = new WithincodePointcut(mixinSignature);
 
 		// Register the mixin
 		wrapper = new CaesarPointcutWrapper(mixin, type);
 		wrapper.setDeclaringType(mixinType);
 		registerPointcut(wrapper);
 		
 		// Creates an orPointcut for both the type and the mixin
 		Pointcut orPointcut = new OrPointcut(
 				p,
 				mixin);
 		
 		return new CaesarPointcutWrapper(orPointcut);
 	}
 
 	private CaesarPointcutWrapper parseCflowPointcut(boolean isBelow) {
 		parseIdentifier();
 		eat("(");
 		Pointcut entry = parsePointcut();
 		eat(")");
 		
 		// Creates the wrapper 
 		CflowPointcut p = new CflowPointcut(entry, isBelow, null);
 		CaesarPointcutWrapper wrapper = new CaesarPointcutWrapper(p);
 		return wrapper;
 		
 	}
 	
 	private SignaturePattern parseConstructorSignaturePattern() {
 		SignaturePattern ret = parseMethodOrConstructorSignaturePattern();
 		if (ret.getKind() == Member.CONSTRUCTOR) return ret;
 		
 		throw new ParserException("constructor pattern required, found method pattern",
 				ret);
 	}
 	
 	private CaesarPointcutWrapper parseReferencePointcut() {
 		TypePattern onType = parseTypePattern();
 		NamePattern name = tryToExtractName(onType);
 		if (name == null) {
     		throw new ParserException("name pattern", tokenSource.peek());
     	}
     	if (onType.toString().equals("")) {
     		onType = null;
     	}
 		
 		TypePatternList arguments = parseArgumentsPattern();
 		
 		// Creates the wrapper 
 		ReferencePointcut p = new ReferencePointcut(onType, name.maybeGetSimpleName(), arguments);
 		CaesarPointcutWrapper wrapper = new CaesarPointcutWrapper(p);
 		wrapper.setOnTypeSymbolic(p.onTypeSymbolic);
 		return wrapper;
 	}
 	
 	private NamePattern tryToExtractName(TypePattern nextType) {
 		if (nextType == TypePattern.ANY) {
 			return NamePattern.ANY;
 		} else if (nextType instanceof CaesarWildTypePattern) {
 			CaesarWildTypePattern p = (CaesarWildTypePattern)nextType;
 			return p.extractName();
 		} else if (nextType instanceof WildTypePattern) {
 			WildTypePattern p = (WildTypePattern)nextType;
 			return p.extractName();
 		} else {
 		    return null;
 		}
 	}
 	
 	public ModifiersPattern parseModifiersPattern() {
 		int requiredFlags = 0;
 		int forbiddenFlags = 0;
 		int start;
 		while (true) {
 		    start = tokenSource.getIndex();
 		    boolean isForbidden = false;
 		    isForbidden = maybeEat("!");
 		    IToken t = tokenSource.next();
 		    int flag = ModifiersPattern.getModifierFlag(t.getString());
 		    if (flag == -1) break;
 		    if (isForbidden) forbiddenFlags |= flag;
 		    else requiredFlags |= flag;
 		}
 		
 		tokenSource.setIndex(start);
 		if (requiredFlags == 0 && forbiddenFlags == 0) {
 			return ModifiersPattern.ANY;
 		} else {
 			return new CaesarModifiersPattern(requiredFlags, forbiddenFlags);
 		}
 	}
 	
 	//----------------------------------------------------------------------
     // COPIED JUST TO ADAPT FOR CaesarWildTypePatterns
     // ----------------------------------------------------------------------
 	
 	
 	public TypePattern parseSingleTypePattern() {
 		List<NamePattern> names = parseDottedNamePattern(); 
 //		new ArrayList();
 //		NamePattern p1 = parseNamePattern();
 //		names.add(p1);
 //		while (maybeEat(".")) {
 //			if (maybeEat(".")) {
 //				names.add(NamePattern.ELLIPSIS);
 //			}
 //			NamePattern p2 = parseNamePattern();
 //			names.add(p2);
 //		}
 		int dim = 0;
 		while (maybeEat("[")) {
 			eat("]");
 			dim++;
 		}
 			
 		
 		boolean includeSubtypes = maybeEat("+");
 		int endPos = tokenSource.peek(-1).getEnd();
 		
 		//??? what about the source location of any's????
 		if (names.size() == 1 && ((NamePattern)names.get(0)).isAny() && dim == 0) return TypePattern.ANY;
 		
 		return new CaesarWildTypePattern(names, includeSubtypes, dim, endPos);
 	}
 	
 	public List<NamePattern> parseDottedNamePattern() {
 		List<NamePattern> names = new ArrayList<NamePattern>();
 		StringBuffer buf = new StringBuffer();
 		IToken previous = null;
 		boolean justProcessedEllipsis = false; // Remember if we just dealt with an ellipsis (PR61536)
 		boolean justProcessedDot = false; 
 		boolean onADot = false;
 		while (true) {
 			IToken tok = null;
 			int startPos = tokenSource.peek().getStart();
 			String afterDot = null;
 			while (true) {
 				if (previous !=null && previous.getString().equals(".")) justProcessedDot = true;
 				tok = tokenSource.peek();
 				onADot = (tok.getString().equals("."));
 				if (previous != null) {
 					if (!isAdjacent(previous, tok)) break;
 				}
 				if (tok.getString() == "*" || tok.isIdentifier()) {
 					buf.append(tok.getString());
 				} else if (tok.getLiteralKind() != null) {
 					//System.err.println("literal kind: " + tok.getString());
 					String s = tok.getString();
 					int dot = s.indexOf('.');
 					if (dot != -1) {
 						buf.append(s.substring(0, dot));
 						afterDot = s.substring(dot+1);
 						previous = tokenSource.next();
 						break;
 					}
 					buf.append(s);  // ??? so-so
 				} else {
 					break;
 				}
 				previous = tokenSource.next();
 				//XXX need to handle floats and other fun stuff
 			}
 			int endPos = tokenSource.peek(-1).getEnd();
 			if (buf.length() == 0 && names.isEmpty()) {
 				throw new ParserException("expected name pattern", tok);
 			} 
 			
 			if (buf.length() == 0 && justProcessedEllipsis) {
 				throw new ParserException("name pattern cannot finish with ..", tok);
 			}
 			if (buf.length() == 0 && justProcessedDot && !onADot) {
 					throw new ParserException("name pattern cannot finish with .", tok);
 			}
 			
 			if (buf.length() == 0) {
 				names.add(NamePattern.ELLIPSIS);
 				justProcessedEllipsis = true;
 			} else {
 				checkLegalName(buf.toString(), previous);
 				NamePattern ret = new NamePattern(buf.toString());
 				ret.setLocation(sourceContext, startPos, endPos);
 				names.add(ret);
 				justProcessedEllipsis = false;
 			}
 			
 			if (afterDot == null) {
 				buf.setLength(0);
 				if (!maybeEat(".")) break;
 				else previous = tokenSource.peek(-1);
 			} else {
 				buf.setLength(0);
 				buf.append(afterDot);
 				afterDot = null;
 			}
 		}
 		//System.err.println("parsed: " + names);
 		return names;
 	}
 	
 	private boolean isAdjacent(IToken first, IToken second) {
 		return first.getEnd() == second.getStart()-1;
 	}
 	
 	private void checkLegalName(String s, IToken tok) {
 		char ch = s.charAt(0);
 		if (!(ch == '*' || Character.isJavaIdentifierStart(ch))) {
 			throw new ParserException("illegal identifier start (" + ch + ")", tok);
 		}
 		
 		for (int i=1, len=s.length(); i < len; i++) {
 			ch = s.charAt(i);
 			if (!(ch == '*' || Character.isJavaIdentifierPart(ch))) {
 				throw new ParserException("illegal identifier character (" + ch + ")", tok);
 			}
 		}
 		
 	}
 	
 	private boolean maybeEatNew(TypePattern returnType) {
 		
 		if (returnType instanceof CaesarWildTypePattern) {
 			CaesarWildTypePattern p = (CaesarWildTypePattern)returnType;
 			if (p.maybeExtractName("new")) return true;
 		}
 		if (returnType instanceof WildTypePattern) {
 			WildTypePattern p = (WildTypePattern)returnType;
 			if (p.maybeExtractName("new")) return true;
 		}
 		int start = tokenSource.getIndex();
 		if (maybeEat(".")) {
 			String id = maybeEatIdentifier();
 			if (id != null && id.equals("new")) return true;
 			tokenSource.setIndex(start);
 		}
 		
 		return false;
 	}
 
     // ----------------------------------------------------------------------
     // CODE FOR REGISTRING THE GENERATED POINTCUTS
     // ----------------------------------------------------------------------
 	
 	/**
 	 * Extends PatterParser's parseSinglePointcut to register the
 	 * relation between the TypePattern with the Pointcut.
 	 */
 	public Pointcut parseSinglePointcut() {		
 		
 		// Parse the pointcut with PatterParser
 		CaesarPointcutWrapper p = this.parsePointcutWrapper();
 		
 		// Store in the map
 		registerPointcut(p);
 		
 		// Return the PatterParser result
 		return p.getWrappee();
 	}
 	
 	
 	/**
 	 * Register the relation between the TypePattern in 
 	 * the Pointcut with the Pointcut itself. The map will 
 	 * be stored statically in the CaesarPointcutScope, 
 	 * where it will be used to lookup the correct type in pointcuts.
 	 * 
 	 * @param pointcut the pointcut to be registered
 	 */
 	protected void registerPointcut(CaesarPointcutWrapper pointcut) {
 
 		if (pointcut.isKinded()) {
 			CaesarKindedPointcut p = (CaesarKindedPointcut) pointcut.getWrappee();
 			
 			CaesarPointcutScope.register(
 					p.getSignature().getDeclaringType(),
 					pointcut);
 			return;
 		}
 
 		if (pointcut.isWithin()) {
 			CaesarPointcutScope.register(
 					pointcut.getTypePattern(),
 					pointcut);
 			return;
 		}
 		
 		if (pointcut.isWithincode()) {
 			CaesarPointcutScope.register(
 					pointcut.getDeclaringType(),
 					pointcut);
 			return;
 		}
 
 		if (pointcut.isHandler()) {
 			CaesarPointcutScope.register(
 					pointcut.getExceptionType(),
 					pointcut);
 			return;
 		}
 		
 		if (pointcut.isReference()) {
 			CaesarPointcutScope.register(
 					pointcut.getOnTypeSymbolic(),
 					pointcut);
 			return;
 		}
 		
 		// No need to store information for these types
 		if (pointcut.isArgs()) {
 			return;
 		}
 		if (pointcut.isThisOrTarget()) {
 			return;
 		}
 		if (pointcut.isCflow()) {
 			return;
 		}
 	}
 	
     // ----------------------------------------------------------------------
     // HELPERS
     // ----------------------------------------------------------------------
 	
 	/**
 	 * Creates a TypePatternList which contains only the CaesarWildTypePattern needed
 	 * to match the java.lang.Object name. This is used to create the parameters
 	 * list when matching the default contructor
 	 * 
 	 * @return a list with the pattern for Object
 	 */
 	protected TypePatternList createObjectTypeList() {
 
 		ArrayList<NamePattern> names = new ArrayList<NamePattern>();
 		names.add(new NamePattern("java.lang.Object"));
 
 		return
 			new TypePatternList(
 				new TypePattern[] { new CaesarWildTypePattern(names, false, 0)} );
 	}
 
 	/**
 	 * Creates a type pattern (a CaesarWildTypePattern) which represents the CaesarObject
 	 * class. 
 	 * 
 	 * @return a type pattern for the CaesarObject class
 	 */
 	protected TypePattern createCaesarObjectPattern() {
 		
 		ArrayList<NamePattern> names = new ArrayList<NamePattern>();
 		names.add(new NamePattern("org.caesarj.runtime.CaesarObject"));
 		
 		return new CaesarWildTypePattern(
 				names,
 				false,
 				0);
 	}
 	
 	/**
 	 * Creates a signator which select constructors:
 	 * 
 	 *   * $constructor(..)
 	 *   
 	 * @return
 	 */
 	protected SignaturePattern createConstructorSignature() {
 		
 		return new SignaturePattern(
 				Member.METHOD, ModifiersPattern.ANY,
 				TypePattern.ANY, TypePattern.ANY,
                 new NamePattern("$constructor"), 
                 new TypePatternList(new TypePattern[] { TypePattern.ELLIPSIS }),
                 ThrowsPattern.ANY);
 	}
 	
 	/**
 	 * Returns a clone of the name patterns this type pattern has
 	 *  
 	 * @param pattern
 	 * @return a list of name patterns, which is a clone of this pattern
 	 */
 	protected List<NamePattern> createMixinNamePatterns(TypePattern pattern) {
 		
 		ArrayList<NamePattern> namePatterns = new ArrayList<NamePattern>();
 		
 		String[] names = pattern.toString().split("\\.");
 		
 		for (int i = 0; i < names.length; i++) {
 			namePatterns.add(new NamePattern(names[i]));
 		}
 		
 		return namePatterns;
 	}
 	
 	/**
 	 * Creates a mixin type pattern (a CaesarWildTypePatter) for this type, using the 
 	 * createMixinNamePatterns method.
 	 * 
 	 * @param type the type
 	 * @return a mixin pattern to the type
 	 */
 	protected TypePattern createMixinType(TypePattern type) {
 
 		return
 			new CaesarWildTypePattern(
 				createMixinNamePatterns(type),
 				type.isIncludeSubtypes(),
 				0);
 	}
 
 	/**
 	 * Creates a signature pattern which is a clone of this signature but has the mixinType 
 	 * as declaringType
 	 * 
 	 * @param sig
 	 * @param mixinType
 	 * @return
 	 */
 	protected SignaturePattern createMixinSignature(SignaturePattern sig, TypePattern mixinType) {
 		
 		CaesarCloner c = CaesarCloner.instance();
 		
 		return
 			new SignaturePattern(
 					sig.getKind(), sig.getModifiers(),
 					c.clone(sig.getReturnType()), mixinType,
 					c.clone(sig.getName()), c.clone(sig.getParameterTypes()),
 					c.clone(sig.getThrowsPattern()));
 	}
 }
