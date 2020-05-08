 /*
  * Copyright (C) 2012 Timo Vesalainen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.vesalainen.parser;
 
 import java.lang.reflect.InvocationTargetException;
 import org.vesalainen.bcc.type.ClassWrapper;
 import org.vesalainen.bcc.FieldInitializer;
 import org.vesalainen.bcc.IllegalConversionException;
 import org.vesalainen.bcc.LookupList;
 import org.vesalainen.bcc.MethodCompiler;
 import org.vesalainen.bcc.type.MethodWrapper;
 import org.vesalainen.bcc.SubClass;
 import org.vesalainen.grammar.AnnotatedGrammar;
 import org.vesalainen.grammar.GTerminal;
 import org.vesalainen.grammar.state.AmbiguousExpressionException;
 import org.vesalainen.grammar.state.DFA;
 import org.vesalainen.grammar.state.DFAState;
 import org.vesalainen.grammar.state.IllegalExpressionException;
 import org.vesalainen.grammar.state.NFA;
 import org.vesalainen.grammar.state.NFAState;
 import org.vesalainen.grammar.state.Scope;
 import org.vesalainen.parser.util.InputReader;
 import org.vesalainen.parser.annotation.GenRegex;
 import org.vesalainen.parser.annotation.ParseMethod;
 import org.vesalainen.parser.annotation.RecoverMethod;
 import org.vesalainen.parser.annotation.Rule;
 import org.vesalainen.parser.annotation.Rules;
 import org.vesalainen.parser.annotation.Terminal;
 import org.vesalainen.parser.annotation.TraceMethod;
 import org.vesalainen.regex.MatchCompiler;
 import org.vesalainen.regex.Regex;
 import org.vesalainen.regex.Regex.Option;
 import java.io.File;
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Member;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Type;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.vesalainen.bcc.ClassCompiler;
 import org.vesalainen.bcc.MethodImplementor;
 import org.vesalainen.bcc.type.Descriptor;
 import org.vesalainen.bcc.type.Generics;
 import org.vesalainen.grammar.Grammar;
 import org.vesalainen.lpg.Item;
 import org.vesalainen.lpg.Lr0State;
 import org.vesalainen.lpg.State;
 import org.vesalainen.parser.annotation.GenClassname;
 import org.vesalainen.parser.annotation.GrammarDef;
 import org.vesalainen.parser.annotation.ParserContext;
 import org.vesalainen.parser.util.HashMapSet;
 import org.vesalainen.parser.util.MapSet;
 import org.vesalainen.parser.util.PeekableIterator;
 
 /**
  *
  * @author tkv
  */
 public class ParserCompiler implements ClassCompiler, ParserConstants
 {
     private Grammar grammar;
     private SubClass subClass;
     private Map<Set<GTerminal>,Integer> inputMap = new HashMap<>();
     private MapSet<Set<GTerminal>,State> inputSetUsageMap = new HashMapSet<>();
     private Map<Integer,String> expectedMap = new HashMap<>();
     private Type thisClass;
     private Class<?> superClass;
     private Method recoverMethod;
     private Method traceMethod;
     private List<RegexWrapper> regexList;
     private boolean implementsParserInfo;
     private Set<Member> implementedAbstractMethods = new HashSet<>();
     private int nextInput;
     private Deque<Member> reducers = new ArrayDeque<>();
     private Set<String> parseMethodNames = new HashSet<>();
 
     private int lrkLevel;
     private File classDir;
     private File srcDir;
 
     /**
      * Creates a parser using annotations in parserClass.
      * @param superClass
      * @param fullyQualifiedname
      * @throws NoSuchMethodException
      * @throws IOException
      * @throws NoSuchFieldException
      * @throws ClassNotFoundException
      */
     public ParserCompiler(Class<?> superClass) throws IOException, ReflectiveOperationException
     {
         this(superClass, createAnnotatedGrammar(superClass));
     }
     /**
      * Creates a parser using grammar.
      * @param superClass Super class for parser. Possible parser annotations
      * are not processed.
      * @param grammar The grammar
      * @throws NoSuchMethodException
      * @throws IOException
      * @throws NoSuchFieldException
      */
     public ParserCompiler(Class<?> superClass, Grammar grammar) throws IOException, ReflectiveOperationException
     {
         this(ClassWrapper.wrap(getThisClassname(superClass), superClass), grammar);
     }
     /**
      * Creates a parser using grammar.
      * @param superClass Super class for parser. Possible parser annotations
      * are not processed.
      * @param fullyQualifiedname  Parser class name .
      * @param grammar
      * @throws NoSuchMethodException
      * @throws IOException
      * @throws NoSuchFieldException
      * @throws ClassNotFoundException 
      */
     public ParserCompiler(ClassWrapper thisClass, Grammar grammar) throws IOException, ReflectiveOperationException
     {
         this.superClass = (Class<?>) thisClass.getSuperclass();
         this.grammar = grammar;
         GrammarDef grammarDef = superClass.getAnnotation(GrammarDef.class);
         if (grammarDef != null)
         {
             lrkLevel = grammarDef.lrkLevel();
         }
         this.thisClass = thisClass;
         subClass = new SubClass(thisClass);
     }
     private static AnnotatedGrammar createAnnotatedGrammar(Class<?> parserClass) throws IOException
     {
         if (!parserClass.isAnnotationPresent(GrammarDef.class))
         {
             throw new ParserException("@GrammarDef missing from "+parserClass);
         }
         return new AnnotatedGrammar(parserClass);
     }
     private static String getThisClassname(Class<?> parserClass)
     {
         GenClassname genClassname = parserClass.getAnnotation(GenClassname.class);
         if (genClassname == null)
         {
             throw new IllegalArgumentException("@GenClassname missing from "+parserClass);
         }
         return genClassname.value();
     }
     @Override
     public void compile() throws IOException, ReflectiveOperationException
     {
         subClass.codeStaticInitializer(resolvStaticInitializers(superClass));
         subClass.codeDefaultConstructor(resolvInitializers(superClass));
 
         if (ParserInfo.class.isAssignableFrom(superClass))
         {
             implementsParserInfo = true;
         }
 
         compileParseMethods(subClass);
         resolveRecoverAndTrace(superClass);
         overrideAbstractMethods();
         implementNeededReducers();
         compileInputs();
         if (implementsParserInfo)
         {
             compileParserInfo();
         }
     }
 
     public int getLrkLevel()
     {
         return lrkLevel;
     }
     int getInputNumber(Set<GTerminal> inputSet, State state)
     {
         inputSetUsageMap.add(inputSet, state);
         Integer n = inputMap.get(inputSet);
         if (n == null)
         {
             nextInput++;
             StringBuilder sb = new StringBuilder();
             for (GTerminal t : inputSet)
             {
                 String expression = t.getUnescapedExpression();
                 if (expression == null)
                 {
                     expression = t.getName();
                 }
                 if (sb.length() == 0)
                 {
                     sb.append("\n  "+t.getName()+"="+expression+"\n");
                 }
                 else
                 {
                     sb.append("| "+t.getName()+"="+expression+"\n");
                 }
             }
             expectedMap.put(nextInput, sb.toString());
             inputMap.put(inputSet, nextInput);
             return nextInput;
         }
         else
         {
             return n;
         }
     }
     private void compileParseMethods(SubClass subClass) throws IOException, NoSuchMethodException
     {
         Class<?> clazz = superClass;
         while (clazz != null)
         {
             compileParseMethods(subClass, clazz);
             clazz = clazz.getSuperclass();
         }
     }
     private void compileParseMethods(SubClass subClass, Class<?> clazz) throws IOException, NoSuchMethodException
     {
         for (Method method : clazz.getDeclaredMethods())
         {
             if (method.isAnnotationPresent(ParseMethod.class))
             {   // TODO IOException handling and AutoCloseable
                 List<String> contextList = new ArrayList<>();
                 ParseMethod pm = method.getAnnotation(ParseMethod.class);
                 Class<?> parseReturnType = method.getReturnType();
                 Class<?>[] parameters = method.getParameterTypes();
                 if (parameters.length == 0)
                 {
                     throw new IllegalArgumentException("@ParseMethod method "+method+" must have at least one parameter");
                 }
                 if (pm.lower() && pm.upper())
                 {
                     throw new IllegalArgumentException("@ParseMethod method "+method+" has both lower- and upper-case set");
                 }
                 Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                 Class<?>[] parseParameters = Arrays.copyOf(parameters, parameters.length);
                 parseParameters[0] = InputReader.class;
                 for (int ii=1;ii<parameters.length;ii++)
                 {
                     boolean pcf = false;
                     for (Annotation a : parameterAnnotations[ii])
                     {
                         if (a.annotationType().equals(ParserContext.class))
                         {
                             ParserContext parserContext = (ParserContext) a;
                             contextList.add(parserContext.value());
                             pcf = true;
                             break;
                         }
                     }
                     if (!pcf)
                     {
                         throw new IllegalArgumentException("extra not @ParserContext parameter in "+method);
                     }
                 }
                 if (parameters.length != parseParameters.length)
                 {
                     throw new IllegalArgumentException("all @ParserContext not have same number of parameters");
                 }
                 for (int ii=1;ii<parameters.length;ii++)
                 {
                     boolean pcf = false;
                     for (Annotation a : parameterAnnotations[ii])
                     {
                         if (a.annotationType().equals(ParserContext.class))
                         {
                             ParserContext parserContext = (ParserContext) a;
                             if (ii > contextList.size())
                             {
                                 throw new IllegalArgumentException("@ParserContext("+parserContext.value()+") not found in "+method+" ("+contextList+")");
                             }
                             String name = contextList.get(ii-1);
                             if (!name.equals(parserContext.value()))
                             {
                                 throw new IllegalArgumentException("@ParserContext("+parserContext.value()+") name in "+method+" differs from previous "+name);
                             }
                             if (!parseParameters[ii].equals(parameters[ii]))
                             {
                                 throw new IllegalArgumentException("@ParserContext("+parserContext.value()+") type "+parameters[ii]+" in "+method+" differs from previous "+parseParameters[ii]);
                             }
                             pcf = true;
                             break;
                         }
                     }
                     if (!pcf)
                     {
                         throw new IllegalArgumentException("extra not @ParserContext parameter in "+method);
                     }
                 }
                 MethodCompiler mc = subClass.override(Modifier.PUBLIC, method);
                 mc.nameArgument(IN, 1);
                 for (int ii=0;ii<contextList.size();ii++)
                 {
                     mc.nameArgument(contextList.get(ii), ii+2);
                 }
                 mc.tload(THIS);
                 if (InputReader.class.isAssignableFrom(parameters[0]))
                 {
                     mc.tload(IN);
                 }
                 else
                 {
                     List<Class<?>> pList = new ArrayList<>();
                     pList.add(parameters[0]);
                     if (pm.size() != -1)
                     {
                         pList.add(int.class);
                     }
                     if (!pm.charSet().isEmpty())
                     {
                         pList.add(String.class);
                     }
                     if (pm.upper() || pm.lower())
                     {
                         pList.add(boolean.class);
                     }
                     Constructor irc = null;
                     for (Constructor c : InputReader.class.getConstructors())
                     {
                         Class<?>[] params = c.getParameterTypes();
                         if (params.length == pList.size())
                         {
                             int index = 0;
                             boolean ok = true;
                             for (Class<?> cls : pList)
                             {
                                 if (!params[index].isAssignableFrom(cls))
                                 {
                                     ok = false;
                                     break;
                                 }
                                 index++;
                             }
                             if (ok)
                             {
                                 irc = c;
                                 break;
                             }
                         }
                     }
                     if (irc == null)
                     {
                         throw new ParserException(method+" signature not compatible with any InputReader constructor");
                     }
                     mc.anew(InputReader.class);
                     mc.dup();
                     mc.tload(IN);
                     if (pm.size() != -1)
                     {
                         mc.iconst(pm.size());
                     }
                     if (!pm.charSet().isEmpty())
                     {
                         mc.tconst(pm.charSet());
                     }
                     if (pm.upper() || pm.lower())
                     {
                         mc.tconst(pm.upper());
                     }
                     mc.invoke(irc);
                 }
                 if (pm.useOffsetLocatorException())
                 {
                     mc.dup();
                     mc.tconst(true);
                     mc.invoke(InputReader.class.getMethod("useOffsetLocatorException", boolean.class));
                 }
                 for (int ii=0;ii<contextList.size();ii++)
                 {
                     mc.tload(contextList.get(ii));
                 }
                 String parserMethodname = makeUniqueMethodname(PARSEMETHODPREFIX+pm.start());
                 MethodWrapper mw = new MethodWrapper(Modifier.PRIVATE, thisClass, parserMethodname, parseReturnType, parseParameters);
                 mc.invokevirtual(mw);
                 mc.treturn();
                 mc.end();
                 ParserMethodCompiler pmc = new ParserMethodCompiler(pm.start(), pm.eof(), pm.syntaxOnly(), pm.whiteSpace());
                 pmc.setContextList(contextList);
                 mw.setImplementor(pmc);
                 mw.setWideIndex(pm.wideIndex());
                 addReducer(mw);
             }
         }
     }
 
     private String makeUniqueMethodname(String name)
     {
         name = MethodWrapper.makeJavaIdentifier(name);
         if (parseMethodNames.contains(name))
         {
             throw new ParserException("duplicate method name "+name);
         }
         return name;
     }
     private void compileParserInfo() throws IOException, NoSuchMethodException
     {
         compileGetToken();
         compileGetRule();
         compileGetExpected();
     }
     private void compileGetToken() throws IOException, NoSuchMethodException
     {
         MethodCompiler mc = subClass.defineMethod(Modifier.PUBLIC, GETTOKEN, String.class, int.class);
         mc.nameArgument(TOKEN, 1);
         LookupList list = new LookupList();
         for (String symbol : grammar.getSymbols())
         {
             int number = grammar.getNumber(symbol);
             list.addLookup(number, symbol.toString());
         }
         mc.tload(TOKEN);
         mc.optimizedSwitch("error", list);
         for (String symbol : grammar.getSymbols())
         {
             mc.fixAddress(symbol.toString());
             mc.ldc(symbol.toString());
             mc.treturn();
         }
         mc.fixAddress("error");
         mc.ldc("unknown token");
         mc.treturn();
         mc.end();
     }
     private void compileGetRule() throws IOException, NoSuchMethodException
     {
         MethodCompiler mc = subClass.defineMethod(Modifier.PUBLIC, GETRULE, String.class, int.class);
         mc.nameArgument(RULE, 1);
         LookupList list = new LookupList();
         Map<Integer,String> ruleDesc = grammar.getRuleDescriptions();
         for (int number : ruleDesc.keySet())
         {
             list.addLookup(number, "rule-"+number);
         }
         mc.tload(RULE);
         mc.optimizedSwitch("error", list);
         for (int number : ruleDesc.keySet())
         {
             mc.fixAddress("rule-"+number);
             mc.ldc(ruleDesc.get(number));
             mc.treturn();
         }
         mc.fixAddress("error");
         mc.ldc("unknown rule");
         mc.treturn();
         mc.end();
     }
     private void compileGetExpected() throws IOException, NoSuchMethodException
     {
         MethodCompiler mc = subClass.defineMethod(Modifier.PUBLIC, GETEXPECTED, String.class, int.class);
         mc.nameArgument(INPUT, 1);
         LookupList list = new LookupList();
         for (int number : expectedMap.keySet())
         {
             list.addLookup(number, INPUT+number);
         }
         mc.tload(INPUT);
         mc.optimizedSwitch("error", list);
         for (int number : expectedMap.keySet())
         {
             mc.fixAddress(INPUT+number);
             mc.ldc(expectedMap.get(number));
             mc.treturn();
         }
         mc.fixAddress("error");
         mc.ldc("unknown input");
         mc.treturn();
         mc.end();
     }
 
     private void compileInputs() throws IOException, NoSuchMethodException
     {
         for (Set<GTerminal> set : inputMap.keySet())
         {
             if (!set.isEmpty())
             {
                 int inputNumber = inputMap.get(set);
                 NFA<Integer> nfa = null;
                 Scope<NFAState<Integer>> nfaScope = new Scope<>(INPUT+inputNumber);
                 Scope<DFAState<Integer>> dfaScope = new Scope<>(INPUT+inputNumber);
                 for (GTerminal terminal : set)
                 {
                     if (terminal.getExpression() != null)
                     {
                         if (nfa == null)
                         {
                             nfa = terminal.createNFA(nfaScope);
                         }
                         else
                         {
                             NFA<Integer> nfa2 = terminal.createNFA(nfaScope);
                             nfa = new NFA(nfaScope, nfa, nfa2);
                         }
                     }
                 }
                 try
                 {
                     if (nfa != null)
                     {
                         DFA dfa = nfa.constructDFA(dfaScope);
                         //dfa.checkAnotherAcceptingState();
                         MethodWrapper mw = new MethodWrapper(Modifier.PRIVATE, thisClass, INPUT+inputNumber, int.class, InputReader.class);
                         MatchCompiler<Integer> ic = new MatchCompiler<>(dfa, ERROR, EOF);
                         mw.setImplementor(ic);
                         subClass.implement(mw);
                     }
                     else
                     {
                         MethodCompiler mc = subClass.defineMethod(Modifier.PRIVATE, INPUT+inputNumber, int.class, InputReader.class);
                         EofCompiler ec = new EofCompiler(mc);
                         ec.compile();
                     }
                 }
                 catch (AmbiguousExpressionException ex)
                 {
                     String s1 = grammar.getSymbol((Integer)ex.getToken1());
                     String s2 = grammar.getSymbol((Integer)ex.getToken2());
                     throw new AmbiguousGrammarException("expression "+getExpected(inputNumber)+"used in "+getInputUsageFor(set)+" is ambiguous. conflicting symbols "+s1+" and "+s2, ex);
                 }
                 catch (IllegalExpressionException ex)
                 {
                     throw new AmbiguousGrammarException("grammar is ambiguous "+set+" accepts same string "+ex.getMessage()+"used in "+getInputUsageFor(set), ex);
                 }
             }
         }
     }
     String getInputUsageFor(Set<GTerminal> set) throws IOException
     {
         Set<State> stateSet = inputSetUsageMap.get(set);
         if (stateSet != null)
         {
             StringBuilder sb = new StringBuilder();
             for (State state : stateSet)
             {
                 if (state instanceof Lr0State)
                 {
                     Lr0State s = (Lr0State) state;
                     PeekableIterator<Item> ni  = s.getKernelItemsPtr();
                     while (ni.hasNext())
                     {
                         sb.append("\n");
                         Item i = ni.next();
                         i.print(sb);
                     }
                     ni  = s.getCompleteItemsPtr();
                     while (ni.hasNext())
                     {
                         sb.append("\n");
                         Item i = ni.next();
                         i.print(sb);
                     }
                 }
             }
             return sb.toString();
         }
         else
         {
             throw new IllegalArgumentException("state for input set "+set+" not found");
         }
     }
     String getExpected(int inputNumber)
     {
         return expectedMap.get(inputNumber);
     }
     private FieldInitializer[] resolvInitializers(Class<?> parserClass)
     {
         List<FieldInitializer> list = new ArrayList<>();
         Class<?> clazz = parserClass;
         while (clazz != null)
         {
             for (Field f : clazz.getDeclaredFields())
             {
                 if (!Modifier.isStatic(f.getModifiers()))
                 {
                     if (f.isAnnotationPresent(GenRegex.class))
                     {
                         list.add(createRegex(f));
                     }
                 }
             }
             clazz = clazz.getSuperclass();
         }
         return list.toArray(new FieldInitializer[list.size()]);
     }
     private FieldInitializer[] resolvStaticInitializers(Class<?> parserClass)
     {
         List<FieldInitializer> list = new ArrayList<>();
         Class<?> clazz = parserClass;
         while (clazz != null)
         {
             for (Field f : clazz.getDeclaredFields())
             {
                 if (Modifier.isStatic(f.getModifiers()))
                 {
                     if (f.isAnnotationPresent(GenRegex.class))
                     {
                         list.add(createRegex(f));
                     }
                 }
             }
             clazz = clazz.getSuperclass();
         }
         return list.toArray(new FieldInitializer[list.size()]);
     }
     private FieldInitializer createRegex(Field f)
     {
         if (!Regex.class.isAssignableFrom(f.getType()))
         {
             throw new IllegalArgumentException(f+" cannot be initialized with Regex subclass");
         }
         if (regexList == null)
         {
             regexList = new ArrayList<>();
         }
         GenRegex ra = f.getAnnotation(GenRegex.class);
         String cn = Generics.getFullyQualifiedForm(thisClass)+"Regex"+regexList.size();
         ClassWrapper regexImpl = ClassWrapper.wrap(cn, Regex.class);
         regexList.add(new RegexWrapper(ra.value(), cn, ra.options()));
         return FieldInitializer.getObjectInstance(f, regexImpl);
     }
 
     private void resolveRecoverAndTrace(Class<?> parserClass)
     {
         for (Method m : parserClass.getDeclaredMethods())
         {
             if (m.isAnnotationPresent(RecoverMethod.class))
             {
                 if (recoverMethod != null)
                 {
                     throw new IllegalArgumentException("there can be only one @RecoverMethod");
                 }
                 recoverMethod = m;
             }
             if (m.isAnnotationPresent(TraceMethod.class))
             {
                 if (traceMethod != null)
                 {
                     throw new IllegalArgumentException("there can be only one @TraceMethod");
                 }
                 traceMethod = m;
             }
         }
     }
     /**
      * Implement abstract method which have either one parameter and returning something or
      * void type not returning anything.
      * @throws IOException
      * @throws NoSuchMethodException
      * @throws NoSuchFieldException
      * @throws ClassNotFoundException 
      */
     private void overrideAbstractMethods() throws IOException, NoSuchMethodException, NoSuchFieldException, ClassNotFoundException
     {
         Set<Member> set = new HashSet<>();
         Class<?> cls = superClass;
         while (!Object.class.equals(cls))
         {
             for (Method method : cls.getDeclaredMethods())
             {
                 if (Modifier.isAbstract(method.getModifiers()))
                 {
                     if (method.isAnnotationPresent(Terminal.class) || method.isAnnotationPresent(Rule.class) || method.isAnnotationPresent(Rules.class) )
                     {
                         if (!contains(set, method))
                         {
                             set.add(method);
                             implementedAbstractMethods.add(method);
                             Class<?> returnType = method.getReturnType();
                             Class<?>[] params = method.getParameterTypes();
                             if (!Void.TYPE.equals(returnType) && params.length == 1)
                             {
                                 MethodCompiler mc = subClass.override(Modifier.PROTECTED, method);
                                 mc.nameArgument(ARG, 1);
                                 try
                                 {
                                     mc.convert(ARG, method.getReturnType());
                                 }
                                 catch (IllegalConversionException ex)
                                 {
                                     throw new IOException("bad conversion with "+method, ex);
                                 }
                                 mc.treturn();
                                 mc.end();
                             }
                             else
                             {
                                 if (Void.TYPE.equals(returnType) && params.length == 0)
                                 {
                                     MethodCompiler mc = subClass.override(Modifier.PROTECTED, method);
                                     mc.treturn();
                                     mc.end();
                                 }
                                 else
                                 {
                                     throw new IllegalArgumentException("cannot implement abstract method "+method);
                                 }
                             }
                         }
                     }
                 }
                 else
                 {
                     set.add(method);
                 }
             }
             cls = cls.getSuperclass();
         }
     }
     
     private void implementNeededReducers() throws IOException
     {
         while (!reducers.isEmpty())
         {
             Member reducer = reducers.pollFirst();
             if (!implementedAbstract(reducer))
             {
                 MethodImplementor implementor = Generics.getImplementor(reducer);
                 try
                 {
                     Method setParserCompiler = implementor.getClass().getMethod("setParserCompiler", ParserCompiler.class);
                     try
                     {
                         setParserCompiler.invoke(implementor, this);
                     }
                     catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                     {
                         throw new IOException(ex);
                     }
                 }
                 catch (NoSuchMethodException ex)
                 {
                 }
                 catch (SecurityException ex)
                 {
                     throw new IOException(ex);
                 }
                 subClass.implement(reducer);
             }
         }
     }
 
 
     /**
      * Saves Parser class in java classfile format in dir. File path is defined by
      * dir and classname. Example dir = c:\temp class is foo.bar.Main file path is
      * c:\temp\foo\bar\Main.class
      * if srcDir is not null, creates a byte code source file to dir. File content is similar to the
      * output of javap utility. Your IDE might be able to use this file for debugging
      * the actual byte code. (NetBeans can if this file located like java source
      * files)
      *
      * Example dir = c:\src class is foo.bar.Main Source file path is
      * c:\src\foo\bar\Main.jasm
      * @throws IOException
      */
     @Override
     public void saveClass() throws IOException
     {
         if (srcDir != null)
         {
             subClass.createSourceFile(srcDir);
         }
         subClass.save(classDir);
         if (regexList != null)
         {
             for (RegexWrapper rw : regexList)
             {
                 try
                 {
                     Regex.saveAs(rw.getExpression(), classDir, srcDir, rw.getClassname(), rw.getOptions());
                 }
                 catch (Exception ex)
                 {
                     throw new IOException(ex);
                 }
             }
         }
     }
     /**
      * Creates a byte code source file to dir. File content is similar to the
      * output of javap utility. Your IDE might be able to use this file for debugging
      * the actual byte code. (NetBeans can if this file located like java source
      * files)
      *
      * Example dir = c:\src class is foo.bar.Main Source file path is
      * c:\src\foo\bar\Main.jasm
      * @param dir
      * @throws IOException
      */
     public void createSource(File dir) throws IOException
     {
         subClass.createSourceFile(dir);
     }
     /**
      * Compile the parser class dynamically. Nice method for experimenting and testing.
      * For real parser use ant task ParserBuilder
      * @return
      * @throws IOException
      * @throws InstantiationException
      * @throws IllegalAccessException
      */
     public Class<?> loadDynamic()
     {
         try
         {
             return subClass.load();
         }
         catch (IOException ex)
         {
             throw new ParserException(ex);
         }
     }
 
     public Object parserInstance()
     {
         try
         {
             Class<?> c = subClass.load();
             return c.newInstance();
         }
         catch (IOException | InstantiationException | IllegalAccessException ex)
         {
             throw new ParserException(ex);
         }
     }
     /**
      * @deprecated 
      * @param debug 
      */
     public void setDebug(boolean debug)
     {
     }
 
     SubClass getSubClass()
     {
         return subClass;
     }
 
     Grammar getGrammar()
     {
         return grammar;
     }
 
     Method getRecoverMethod()
     {
         return recoverMethod;
     }
 
     Type getThisClass()
     {
         return thisClass;
     }
 
     boolean implementsParserInfo()
     {
         return implementsParserInfo;
     }
 
     boolean implementedAbstract(Member reducer)
     {
         return implementedAbstractMethods.contains(reducer);
     }
 
     Method getTraceMethod()
     {
         return traceMethod;
     }
 
     void addReducer(Member reducer)
     {
         if (Generics.needsImplementation(reducer))
         {
             reducers.add(reducer);
         }
     }
 
     private boolean contains(Set<Member> set, Member method)
     {
         String md = Descriptor.getMethodDesriptor(method);
         String name = Generics.getName(method);
         for (Member sm : set)
         {
             String sn = Generics.getName(sm);
             if (name.equals(sn))
             {
                 String sd = Descriptor.getMethodDesriptor(sm);
                 if (md.equals(sd))
                 {
                     return true;
                 }
             }
         }
         return false;
     }
 
     @Override
     public void setClassDir(File classDir)
     {
         this.classDir = classDir;
     }
 
     @Override
     public void setSrcDir(File srcDir)
     {
         this.srcDir = srcDir;
     }
 
     public File getClassDir()
     {
         return classDir;
     }
 
     public File getSrcDir()
     {
         return srcDir;
     }
 
     private static class RegexWrapper
     {
         private String expression;
         private String classname;
         private Option[] options;
 
         public RegexWrapper(String expression, String classname, Option... options)
         {
             this.expression = expression;
             this.classname = classname;
             this.options = options;
         }
 
         public String getClassname()
         {
             return classname;
         }
 
         public String getExpression()
         {
             return expression;
         }
 
         public Option[] getOptions()
         {
             return options;
         }
 
     }
 }
