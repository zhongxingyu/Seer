 package org.caesarj.compiler.aspectj;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.aspectj.bridge.IMessage;
 import org.aspectj.bridge.Message;
 import org.aspectj.bridge.MessageUtil;
 import org.aspectj.org.eclipse.jdt.core.IType;
 import org.aspectj.util.FileUtil;
 import org.aspectj.util.FuzzyBoolean;
 import org.aspectj.weaver.ResolvedTypeX;
 import org.aspectj.weaver.TypeX;
 import org.aspectj.weaver.WeaverMessages;
 import org.aspectj.weaver.patterns.BindingTypePattern;
 import org.aspectj.weaver.patterns.Bindings;
 import org.aspectj.weaver.patterns.ExactTypePattern;
 import org.aspectj.weaver.patterns.FormalBinding;
 import org.aspectj.weaver.patterns.IScope;
 import org.aspectj.weaver.patterns.NamePattern;
 import org.aspectj.weaver.patterns.SimpleScope;
 import org.aspectj.weaver.patterns.TypePattern;
 import org.aspectj.weaver.patterns.WildTypePattern;
 import org.caesarj.compiler.export.CClass;
 
 public class CaesarWildTypePattern extends WildTypePattern {
 
 	NamePattern[] namePatterns;
 	int ellipsisCount;
 	String[] importedPrefixes;
 	String[] knownMatches;
 	int dim;
 
 	CaesarWildTypePattern(NamePattern[] namePatterns, List names, boolean includeSubtypes, int dim) {
 		super(names, includeSubtypes, dim);
 		this.namePatterns = namePatterns;
 		this.dim = dim;
 		ellipsisCount = 0;
 		for (int i=0; i<namePatterns.length; i++) {
 			if (namePatterns[i] == NamePattern.ELLIPSIS) ellipsisCount++;
 		}
 		setLocation(namePatterns[0].getSourceContext(), namePatterns[0].getStart(), namePatterns[namePatterns.length-1].getEnd());
 	}
 
 	public CaesarWildTypePattern(List<NamePattern> names, boolean includeSubtypes, int dim) {
 		this((NamePattern[]) names.toArray(new NamePattern[names.size()]), names, includeSubtypes, dim);
 	}
 	
 	public CaesarWildTypePattern(List<NamePattern> names, boolean includeSubtypes, int dim, int endPos) {
 		this(names, includeSubtypes, dim);
 		this.end = endPos;
 	}
 	
 	/**
 	 * Resolve this type pattern
 	 */
 	public TypePattern resolveBindings(
 			IScope scope,
 			Bindings bindings,
 			boolean allowBinding,
 			boolean requireExactType) {
 		
 		// If there is no pointcut scope, resolve with the super
 		if (! (scope instanceof CaesarPointcutScope)) {
 			return resolveBindingsOriginal(scope, bindings, allowBinding, requireExactType);
 		}
 		CaesarPointcutScope caesarScope = (CaesarPointcutScope) scope;
 		
 		// if it is the star, return the "Any Type" pattern
 		if (isStar() && dim == 0) {
 			return TypePattern.ANY;
 		}
 
 		// if it is only a simple name (without ., .. or *) try to resolve it
 		String name = maybeGetSimpleName();
 		if (name != null) {
 			FormalBinding formalBinding = scope.lookupFormal(name);
 			if (formalBinding != null) {
 				if (bindings == null) {
 					scope.message(IMessage.ERROR, this, "negation doesn't allow binding");
 					return this;
 				}
 				if (!allowBinding) {
 					scope.message(IMessage.ERROR, this, 
 						"name binding only allowed in target, this, and args pcds");
 					return this;
 				}
 				
 				BindingTypePattern binding = new BindingTypePattern(formalBinding);
 				binding.copyLocationFrom(this);
 				bindings.register(binding, scope);
 				
 				return binding;
 			}
 		}
 		
 		// Gets the pointcut wrapper for this type pattern
 		CaesarPointcutWrapper wrapper = CaesarPointcutScope.getRegistred(this);
 		
 		// if it is a clean name (without .. or *) try to resolve it
 		name = maybeGetCleanName();
 		if (name != null) {
 			
 			String originalName = name;
 
 			// Check if it is a mixin
 			if (wrapper != null && wrapper.isMixin() && scope instanceof CaesarPointcutScope) {
 				
 				CClass cclass = caesarScope.lookupClass(name);
 				if (cclass != null)
 					name = getMixinPattern(cclass);
 				
 				String[] names = name.split("[/\\$\\.]");
 				
 				namePatterns = new NamePattern[names.length];
 				
 				for (int i = 0; i < names.length; i++) {
 					namePatterns[i] = new NamePattern(names[i]);
 				}
 				
 			} else {
 				// No mixins, try to resolve the type
 				ResolvedTypeX resolvedType = scope.getWorld().resolve(TypeX.forName(name), true);
 				TypeX type;
 				while((type = scope.lookupType(name, this)) == ResolvedTypeX.MISSING) {
 					int lastDot = name.lastIndexOf('.');
 					if (lastDot == -1) break;
 					name = name.substring(0, lastDot) + '$' + name.substring(lastDot+1);
 					if (resolvedType == ResolvedTypeX.MISSING)
 						resolvedType = scope.getWorld().resolve(TypeX.forName(name),true);
 				}
 				// Check the result
 				if (type == ResolvedTypeX.MISSING) {
 					if (requireExactType) {
 						if (!allowBinding) {
 							scope.getWorld().getMessageHandler().handleMessage(
 								MessageUtil.error(WeaverMessages.format(WeaverMessages.CANT_BIND_TYPE,originalName),
 												getSourceLocation()));
 						} else if (scope.getWorld().getLint().invalidAbsoluteTypeName.isEnabled()) {
 							scope.getWorld().getLint().invalidAbsoluteTypeName.signal(originalName, getSourceLocation());
 						}
 						return NO;
 					} else if (scope.getWorld().getLint().invalidAbsoluteTypeName.isEnabled()) {
 						// Only put the lint warning out if we can't find it in the world
 						if (resolvedType == ResolvedTypeX.MISSING)
 						  scope.getWorld().getLint().invalidAbsoluteTypeName.signal(originalName, getSourceLocation());
 					}
 				} else {
 					if (dim != 0) type = TypeX.makeArray(type, dim);
 					TypePattern ret = new ExactTypePattern(type, includeSubtypes);
 					ret.copyLocationFrom(this);
 					return ret;
 				}
 			}
 		} else {
 			
 			// In case we have wildcards
 			if (requireExactType) {
 				scope.getWorld().getMessageHandler().handleMessage(
 					MessageUtil.error(WeaverMessages.format(WeaverMessages.WILDCARD_NOT_ALLOWED),
 										getSourceLocation()));
 				return NO;
 			}
 		}
 		
 		importedPrefixes = scope.getImportedPrefixes();
 		knownMatches = preMatch(scope.getImportedNames());
 		
 		// Here we have only the case where we have at least one * or .. between the names
 		// Now, for each name that has no * or .., resolve it in the world
 
 		// Check if it is a mixin
 		boolean isMixin = false;
 		if (wrapper != null && wrapper.isMixin()) {
 			isMixin = true;	
 		}
 		
 		for(int i = 0; i < namePatterns.length; i++) {
 			name = namePatterns[i].maybeGetSimpleName();
 			if (name != null) {
 				if (! isMixin) {
 					namePatterns[i] = resolve(scope, namePatterns[i], name);
 				} else {
 					CClass cclass = caesarScope.lookupClass(name);
 					if (cclass != null) {
 						namePatterns[i] = new NamePattern(name + "_Impl_Mixin_*");
 					}
 				}
 			}
 		}
 		
 		return this;
 	}
 	
 	protected NamePattern resolve(IScope scope, NamePattern n, String cleanName) {
 		
 		if (n == null || cleanName == null) return n;
 			
 		ResolvedTypeX resolvedTypeInTheWorld = 
 			scope.getWorld().resolve(TypeX.forName(cleanName),true);
 		
 		TypeX type;
 		
 		while ((type = scope.lookupType(cleanName, this)) == ResolvedTypeX.MISSING) {
 			int lastDot = cleanName.lastIndexOf('.');
 			if (lastDot == -1) break;
 			cleanName = cleanName.substring(0, lastDot) + '$' + cleanName.substring(lastDot+1);
 			if (resolvedTypeInTheWorld == ResolvedTypeX.MISSING)
 				resolvedTypeInTheWorld = scope.getWorld().resolve(TypeX.forName(cleanName),true);					
 		}
 		
 		if (type == ResolvedTypeX.MISSING) {
 			return n;
 		}
 		
 		return new NamePattern(type.getClassName());
 	}
 	
 	protected String getMixinPattern(CClass cclass) {
 
         StringBuffer strBuf = new StringBuffer();
         String qualifiedName = cclass.getQualifiedName();
         String packagePrefix = "";
         String type = qualifiedName;
 	        
         int i = qualifiedName.lastIndexOf('/');
 	        
         if(i >= 0) {
             packagePrefix = qualifiedName.substring(0, i+1);
             type = qualifiedName.substring(i+1, qualifiedName.length());
         }
 	        
         strBuf.append(packagePrefix);
 	        
         StringBuffer mixinName = new StringBuffer();
         StringTokenizer tok = new StringTokenizer(type, "$");
         while(tok.hasMoreTokens()) {
        	strBuf.append(tok.nextToken() + "_Impl*");

            if(tok.hasMoreTokens())
                strBuf.append('$');
        }

        strBuf.append("_Mixin*");
        
        return strBuf.toString();
	}
	
	protected String getMixinPatternOld(CClass cclass) {

        StringBuffer strBuf = new StringBuffer();
        String qualifiedName = cclass.getQualifiedName();
        String packagePrefix = "";
        String type = qualifiedName;
	        
        int i = qualifiedName.lastIndexOf('/');
	        
        if(i >= 0) {
            packagePrefix = qualifiedName.substring(0, i+1);
            type = qualifiedName.substring(i+1, qualifiedName.length());
        }
	        
        strBuf.append(packagePrefix);
	        
        StringBuffer mixinName = new StringBuffer();
        StringTokenizer tok = new StringTokenizer(type, "$");
        while(tok.hasMoreTokens()) {
         	//mixinName.append(tok.nextToken() + "_");
             //strBuf.append(mixinName + "Impl_Mixin*");
         	strBuf.append(tok.nextToken() + "_Impl_Mixin*");
         	
             if(tok.hasMoreTokens())
                 strBuf.append('$');
         }
 	        
         return strBuf.toString();	    
 	}
 	
     // ----------------------------------------------------------------------
     // CODE COPIED FROM ASPECTJ'S WildTypePattern
     // ----------------------------------------------------------------------
 	
 	//XXX inefficient implementation
 	public static char[][] splitNames(String s) {
 		List<char[]> ret = new ArrayList<char[]>();
 		int startIndex = 0;
 		while (true) {
 		    int breakIndex = s.indexOf('.', startIndex);  // what about /
 		    if (breakIndex == -1) breakIndex = s.indexOf('$', startIndex);  // we treat $ like . here
 		    if (breakIndex == -1) break;
 		    char[] name = s.substring(startIndex, breakIndex).toCharArray();
 		    ret.add(name);
 		    startIndex = breakIndex+1;
 		}
 		ret.add(s.substring(startIndex).toCharArray());
 		return (char[][])ret.toArray(new char[ret.size()][]);
 	}	
 	
 
 	/**
 	 * @see org.aspectj.weaver.TypePattern#matchesExactly(IType)
 	 */
 	protected boolean matchesExactly(ResolvedTypeX type) {
 		String targetTypeName = type.getName();
 		
 		//System.err.println("match: " + targetTypeName + ", " + knownMatches); //Arrays.asList(importedPrefixes));
 		
 		return matchesExactlyByName(targetTypeName);
 	}
 
     /**
 	 * @param targetTypeName
 	 * @return
 	 */
 	private boolean matchesExactlyByName(String targetTypeName) {
 		//XXX hack
 		if (knownMatches == null && importedPrefixes == null) {
 			return innerMatchesExactly(targetTypeName);
 		}
 		
 		if (isStar()) {
 			// we match if the dimensions match
 			int numDimensionsInTargetType = 0;
 			if (dim > 0) {
 				int index;
 				while((index = targetTypeName.indexOf('[')) != -1) {
 					numDimensionsInTargetType++;
 					targetTypeName = targetTypeName.substring(index+1);
 				}
 				if (numDimensionsInTargetType == dim) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 		}
 		
 		// if our pattern is length 1, then known matches are exact matches
 		// if it's longer than that, then known matches are prefixes of a sort
 		if (namePatterns.length == 1) {
 			for (int i=0, len=knownMatches.length; i < len; i++) {
 				if (knownMatches[i].equals(targetTypeName)) return true;
 			}
 		} else {
 			for (int i=0, len=knownMatches.length; i < len; i++) {
 				String knownPrefix = knownMatches[i] + "$";
 				if (targetTypeName.startsWith(knownPrefix)) {
 					int pos = lastIndexOfDotOrDollar(knownMatches[i]);
 					if (innerMatchesExactly(targetTypeName.substring(pos+1))) {
 						return true;
 					}
 				}
 			}
 		}
 
 
 		// if any prefixes match, strip the prefix and check that the rest matches
 		// assumes that prefixes have a dot at the end
 		for (int i=0, len=importedPrefixes.length; i < len; i++) {
 			String prefix = importedPrefixes[i];
 			//System.err.println("prefix match? " + prefix + " to " + targetTypeName);
 			if (targetTypeName.startsWith(prefix)) {
 				
 				if (innerMatchesExactly(targetTypeName.substring(prefix.length()))) {
 					return true;
 				}
 			}
 		}
 		
 		return innerMatchesExactly(targetTypeName);
 	}
 
 	private int lastIndexOfDotOrDollar(String string) {
     	int dot = string.lastIndexOf('.');
     	int dollar = string.lastIndexOf('$');
     	return Math.max(dot, dollar);
     }
 
 	
 	private boolean innerMatchesExactly(String targetTypeName) {
 		//??? doing this everytime is not very efficient
 		char[][] names = splitNames(targetTypeName);
 
         return innerMatchesExactly(names);
 	}
 
     private boolean innerMatchesExactly(char[][] names) {
         		
         		int namesLength = names.length;
         		int patternsLength = namePatterns.length;
         		
         		int namesIndex = 0;
         		int patternsIndex = 0;
         		
         		if (ellipsisCount == 0) {
         			if (namesLength != patternsLength) return false;
         			while (patternsIndex < patternsLength) {
         				if (!namePatterns[patternsIndex++].matches(names[namesIndex++])) {
         					return false;
         				}
         			}
         			return true;
         		} else if (ellipsisCount == 1) {
         			if (namesLength < patternsLength-1) return false;
         			while (patternsIndex < patternsLength) {
         				NamePattern p = namePatterns[patternsIndex++];
         				if (p == NamePattern.ELLIPSIS) {
         					namesIndex = namesLength - (patternsLength-patternsIndex);
         				} else {
         				    if (!p.matches(names[namesIndex++])) {
         					    return false;
         				    }
         				}
         			}
         			return true;
         		} else {
         //            System.err.print("match(\"" + Arrays.asList(namePatterns) + "\", \"" + Arrays.asList(names) + "\") -> ");
                     boolean b = outOfStar(namePatterns, names, 0, 0, patternsLength - ellipsisCount, namesLength, ellipsisCount);
         //            System.err.println(b);
                     return b;
         		}
     }
     private static boolean outOfStar(final NamePattern[] pattern, final char[][] target, 
                                               int           pi,            int       ti, 
                                               int           pLeft,         int       tLeft,
                                        final int            starsLeft) {
         if (pLeft > tLeft) return false;
         while (true) {
             // invariant: if (tLeft > 0) then (ti < target.length && pi < pattern.length) 
             if (tLeft == 0) return true;
             if (pLeft == 0) {
                 return (starsLeft > 0);  
             }
             if (pattern[pi] == NamePattern.ELLIPSIS) {
                 return inStar(pattern, target, pi+1, ti, pLeft, tLeft, starsLeft-1);
             }
             if (! pattern[pi].matches(target[ti])) {
                 return false;
             }
             pi++; ti++; pLeft--; tLeft--;
         }
     }    
     private static boolean inStar(final NamePattern[] pattern, final char[][] target, 
                                             int          pi,            int      ti, 
                                      final int          pLeft,          int      tLeft,
                                             int         starsLeft) {
         // invariant: pLeft > 0, so we know we'll run out of stars and find a real char in pattern
         // of course, we probably can't parse multiple ..'s in a row, but this keeps the algorithm
         // exactly parallel with that in NamePattern
         NamePattern patternChar = pattern[pi];
         while (patternChar == NamePattern.ELLIPSIS) {
             starsLeft--;
             patternChar = pattern[++pi];
         }
         while (true) {
             // invariant: if (tLeft > 0) then (ti < target.length)
             if (pLeft > tLeft) return false;
             if (patternChar.matches(target[ti])) {
                 if (outOfStar(pattern, target, pi+1, ti+1, pLeft-1, tLeft-1, starsLeft)) return true;
             }
             ti++; tLeft--;
         }
     }
 	
 	/**
 	 * @see org.aspectj.weaver.TypePattern#matchesInstanceof(IType)
 	 */
 	public FuzzyBoolean matchesInstanceof(ResolvedTypeX type) {
 		//XXX hack to let unmatched types just silently remain so
 		if (maybeGetSimpleName() != null) return FuzzyBoolean.NO;
 		
 		type.getWorld().getMessageHandler().handleMessage(
 			new Message("can't do instanceof matching on patterns with wildcards",
 				IMessage.ERROR, null, getSourceLocation()));
 		return FuzzyBoolean.NO;
 	}
 
 	public NamePattern extractName() {
 		//System.err.println("extract from : " + Arrays.asList(namePatterns));
 		int len = namePatterns.length;
 		NamePattern ret = namePatterns[len-1];
 		NamePattern[] newNames = new NamePattern[len-1];
 		System.arraycopy(namePatterns, 0, newNames, 0, len-1);
 		namePatterns = newNames;
 		//System.err.println("    left : " + Arrays.asList(namePatterns));
 		return ret;
 	}
 	
 	/**
 	 * Method maybeExtractName.
 	 * @param string
 	 * @return boolean
 	 */
 	public boolean maybeExtractName(String string) {
 		int len = namePatterns.length;
 		NamePattern ret = namePatterns[len-1];
 		String simple = ret.maybeGetSimpleName();
 		if (simple != null && simple.equals(string)) {
 			extractName();
 			return true;
 		}
 		return false;
 	}
     	
 	/**
 	 * If this type pattern has no '.' or '*' in it, then
 	 * return a simple string
 	 * 
 	 * otherwise, this will return null;
 	 */
 	public String maybeGetSimpleName() {
 		if (namePatterns.length == 1) {
 			return namePatterns[0].maybeGetSimpleName();
 		}
 		return null;
 	}
 	
 	/**
 	 * If this type pattern has no '*' or '..' in it
 	 */
 	public String maybeGetCleanName() {
 		if (namePatterns.length == 0) {
 			throw new RuntimeException("bad name: " + namePatterns);
 		}
 		//System.out.println("get clean: " + this);
 		StringBuffer buf = new StringBuffer();
 		for (int i=0, len=namePatterns.length; i < len; i++) {
 			NamePattern p = namePatterns[i];
 			String simpleName = p.maybeGetSimpleName();
 			if (simpleName == null) return null;
 			if (i > 0) buf.append(".");
 			buf.append(simpleName);
 		}
 		//System.out.println(buf);
 		return buf.toString();
 	}		
 
 
 	/**
 	 * Need to determine if I'm really a pattern or a reference to a formal
 	 * 
 	 * We may wish to further optimize the case of pattern vs. non-pattern
 	 * 
 	 * We will be replaced by what we return
 	 */
 	public TypePattern resolveBindingsOriginal(IScope scope, Bindings bindings, 
     								boolean allowBinding, boolean requireExactType)
     { 		
     	if (isStar()) {
     		if (dim == 0) { // pr72531
     			return TypePattern.ANY;  //??? loses source location
     		} 
 		}
 
 		String simpleName = maybeGetSimpleName();
 		if (simpleName != null) {
 			FormalBinding formalBinding = scope.lookupFormal(simpleName);
 			if (formalBinding != null) {
 				if (bindings == null) {
 					scope.message(IMessage.ERROR, this, "negation doesn't allow binding");
 					return this;
 				}
 				if (!allowBinding) {
 					scope.message(IMessage.ERROR, this, 
 						"name binding only allowed in target, this, and args pcds");
 					return this;
 				}
 				
 				BindingTypePattern binding = new BindingTypePattern(formalBinding);
 				binding.copyLocationFrom(this);
 				bindings.register(binding, scope);
 				
 				return binding;
 			}
 		}
 		
 		String cleanName = maybeGetCleanName();
 		String originalName = cleanName;
 		// if we discover it is 'MISSING' when searching via the scope, this next local var will
 		// tell us if it is really missing or if it does exist in the world and we just can't
 		// see it from the current scope.
 		ResolvedTypeX resolvedTypeInTheWorld = null;
 		if (cleanName != null) {
 			TypeX type;
 			
 			//System.out.println("resolve: " + cleanName);
 			//??? this loop has too many inefficiencies to count
 			resolvedTypeInTheWorld = scope.getWorld().resolve(TypeX.forName(cleanName),true);
 			while ((type = scope.lookupType(cleanName, this)) == ResolvedTypeX.MISSING) {
 				int lastDot = cleanName.lastIndexOf('.');
 				if (lastDot == -1) break;
 				cleanName = cleanName.substring(0, lastDot) + '$' + cleanName.substring(lastDot+1);
 				if (resolvedTypeInTheWorld == ResolvedTypeX.MISSING)
 					resolvedTypeInTheWorld = scope.getWorld().resolve(TypeX.forName(cleanName),true);					
 			}
 			if (type == ResolvedTypeX.MISSING) {
 				if (requireExactType) {
 					if (!allowBinding) {
 						scope.getWorld().getMessageHandler().handleMessage(
 							MessageUtil.error(WeaverMessages.format(WeaverMessages.CANT_BIND_TYPE,originalName),
 											getSourceLocation()));
 					} else if (scope.getWorld().getLint().invalidAbsoluteTypeName.isEnabled()) {
 						scope.getWorld().getLint().invalidAbsoluteTypeName.signal(originalName, getSourceLocation());
 					}
 					return NO;
 				} else if (scope.getWorld().getLint().invalidAbsoluteTypeName.isEnabled()) {
 					// Only put the lint warning out if we can't find it in the world
 					if (resolvedTypeInTheWorld == ResolvedTypeX.MISSING)
 					  scope.getWorld().getLint().invalidAbsoluteTypeName.signal(originalName, getSourceLocation());
 				}
 			} else {
 				if (dim != 0) type = TypeX.makeArray(type, dim);
 				TypePattern ret = new ExactTypePattern(type, includeSubtypes);
 				ret.copyLocationFrom(this);
 				return ret;
 			}
 		} else {
 			if (requireExactType) {
 				scope.getWorld().getMessageHandler().handleMessage(
 					MessageUtil.error(WeaverMessages.format(WeaverMessages.WILDCARD_NOT_ALLOWED),
 										getSourceLocation()));
 				return NO;
 			}
 			//XXX need to implement behavior for Lint.invalidWildcardTypeName
 		}
 		
 		importedPrefixes = scope.getImportedPrefixes();
 		knownMatches = preMatch(scope.getImportedNames());
 		
 		return this;
 	}
 	
 	public TypePattern resolveBindingsFromRTTI(boolean allowBinding, boolean requireExactType) {
 	   	if (isStar()) {
 			return TypePattern.ANY;  //??? loses source location
 		}
 
 		String cleanName = maybeGetCleanName();
 		if (cleanName != null) {
 			Class clazz = null;
 			clazz = maybeGetPrimitiveClass(cleanName);
 
 			while (clazz == null) {
 				try {
 					clazz = Class.forName(cleanName);
 				} catch (ClassNotFoundException cnf) {
 					int lastDotIndex = cleanName.lastIndexOf('.');
 					if (lastDotIndex == -1) break;
 					cleanName = cleanName.substring(0, lastDotIndex) + '$' + cleanName.substring(lastDotIndex+1);
 				}
 			}
 			
 			if (clazz == null) {
 				try {
 					clazz = Class.forName("java.lang." + cleanName);
 				} catch (ClassNotFoundException cnf) {
 				}
 			}
 
 			if (clazz == null) {
 				if (requireExactType) {
 					return NO;
 				}
 			} else {
 				TypeX type = TypeX.forName(clazz.getName());
 				if (dim != 0) type = TypeX.makeArray(type,dim);
 				TypePattern ret = new ExactTypePattern(type, includeSubtypes);
 				ret.copyLocationFrom(this);
 				return ret;
 			}
 		} else if (requireExactType) {
 		 	return NO;
 		}
 					
 		importedPrefixes = SimpleScope.javaLangPrefixArray;
 		knownMatches = new String[0];
 		
 		return this;	
 	}
 
 	private Class maybeGetPrimitiveClass(String typeName) {
 		return (Class) ExactTypePattern.primitiveTypesMap.get(typeName);
 	}
 	
 	public boolean isStar() {
 		return namePatterns.length == 1 && namePatterns[0].isAny();
 	}
 
 	/**
 	 * returns those possible matches which I match exactly the last element of
 	 */
 	private String[] preMatch(String[] possibleMatches) {
 		//if (namePatterns.length != 1) return CollectionUtil.NO_STRINGS;
 		
 		List<String> ret = new ArrayList<String>();
 		for (int i=0, len=possibleMatches.length; i < len; i++) {
 			char[][] names = splitNames(possibleMatches[i]); //??? not most efficient
 			if (namePatterns[0].matches(names[names.length-1])) {
 				ret.add(possibleMatches[i]);
 			}
 		}
 		return (String[])ret.toArray(new String[ret.size()]);
 	}
 	
     
 //	public void postRead(ResolvedTypeX enclosingType) {
 //		this.importedPrefixes = enclosingType.getImportedPrefixes();
 //		this.knownNames = prematch(enclosingType.getImportedNames());
 //	}
 
 
     public String toString() {
     	StringBuffer buf = new StringBuffer();
     	for (int i=0, len=namePatterns.length; i < len; i++) {
     		NamePattern name = namePatterns[i];
     		if (name == null) {
     			buf.append(".");
     		} else {
     			if (i > 0) buf.append(".");
     			buf.append(name.toString());
     		}
     	}
     	return buf.toString();
     }
     
     public boolean equals(Object other) {
     	
     	if (other instanceof CaesarWildTypePattern) {
     		CaesarWildTypePattern c = (CaesarWildTypePattern) other;
     		int len = c.namePatterns.length;
     		if (len != this.namePatterns.length) return false;
     		for (int i = 0; i < len; i++) {
     			if (! c.namePatterns[i].equals(this.namePatterns[i])) return false;
     		}
     		return true;
     	}
     	
     	if (!(other instanceof WildTypePattern)) return false;
 
     	WildTypePattern o = (WildTypePattern)other;
     	return o.equals(this);
 	}
 
     public int hashCode() {
         int result = 17;
         for (int i = 0, len = namePatterns.length; i < len; i++) {
             result = 37*result + namePatterns[i].hashCode();
         }
         return result;
     }
 
     
     public FuzzyBoolean matchesInstanceof(Class type) {
     	return FuzzyBoolean.NO;
     }
     
     public boolean matchesExactly(Class type) {
     	return matchesExactlyByName(type.getName());
     }
     
 	/**
 	 * @see org.aspectj.weaver.patterns.PatternNode#write(DataOutputStream)
 	 */
 	public void write(DataOutputStream s) throws IOException {
 		s.writeByte(TypePattern.WILD);
 		s.writeShort(namePatterns.length);
 		for (int i = 0; i < namePatterns.length; i++) {
 			namePatterns[i].write(s);
 		}
 		s.writeBoolean(includeSubtypes);
 		s.writeInt(dim);
 		//??? storing this information with every type pattern is wasteful of .class
 		//    file size. Storing it on enclosing types would be more efficient
 		FileUtil.writeStringArray(knownMatches, s);
 		FileUtil.writeStringArray(importedPrefixes, s);
 		writeLocation(s);
 	}
 }
