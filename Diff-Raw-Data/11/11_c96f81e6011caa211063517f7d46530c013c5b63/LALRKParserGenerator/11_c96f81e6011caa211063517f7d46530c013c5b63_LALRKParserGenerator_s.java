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
 package org.vesalainen.lpg;
 
 import org.vesalainen.grammar.Accept;
 import org.vesalainen.grammar.Eof;
 import org.vesalainen.grammar.GRule;
 import org.vesalainen.grammar.Nonterminal;
 import org.vesalainen.grammar.GrammarException;
 import org.vesalainen.grammar.Symbol;
 import org.vesalainen.grammar.Err;
 import org.vesalainen.grammar.Empty;
 import org.vesalainen.grammar.Omega;
 import org.vesalainen.grammar.GTerminal;
 import org.vesalainen.grammar.Nil;
 import java.io.IOException;
 import org.vesalainen.bcc.ObjectType;
 import org.vesalainen.parser.util.PeekableIterator;
 import org.vesalainen.parser.util.RHSComparator;
 import org.vesalainen.parser.util.HashMapList;
 import org.vesalainen.parser.annotation.ParserContext;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Member;
 import java.lang.reflect.Type;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import org.vesalainen.bcc.type.Generics;
 import org.vesalainen.parser.util.AppendablePrinter;
 import org.vesalainen.parser.util.HashMapTreeSet;
 import org.vesalainen.parser.util.HtmlPrinter;
 import org.vesalainen.parser.util.MapList;
 import org.vesalainen.parser.util.MapSet;
 import org.vesalainen.parser.util.NumMap;
 import org.vesalainen.parser.util.NumMapList;
 import org.vesalainen.parser.util.NumMapSet2;
 import org.vesalainen.parser.util.NumSet;
 import org.vesalainen.parser.util.Numerable;
 
 /**
  * LALRKParserGenerator creates the parser states.
  * <p>
  * Implementation of this parser generator is based on Jikes Parser Generator which
  * is written in C.
  * @see <a href="http://sourceforge.net/projects/jikes/files/">IBM Jikes Compiler for the Java Language</a>
  * @see <a href="http://jikes.sourceforge.net/documents/thesis.pdf"> A Practical method for Constructing Efficient LALR(k) Parsers with Automatic Error Recovery</a>
  */
 public class LALRKParserGenerator
 {
     private static final int INFINITY = 999999;
     private int lalrLevel;
     private List<GRule> rules;
     private List<Symbol> symbols;
     private List<Nonterminal> nonterminals;
     private List<GTerminal> terminals;
     private boolean readReduce = true;
     private boolean singleProductions = false;    // ???
     private int defaultOpt = 0;
     private Map<Numerable, Integer> indexMap;  // used in digraph
     private Map<LaPtr, Integer> laIndexMap;  // used in digraph
     private Deque<Object> stack;    // used in digraph
     private List<Item> itemList = new ArrayList<>();
     private Map<Set<Item>, Lr0State> stateTable = new LinkedHashMap<>();
     private Deque<Lr0State> newStates = new ArrayDeque<>();
     private Set<Set<Shift>> shiftMap = new LinkedHashSet<>();
     private Map<Lr0State, LaPtr> laBase = new NumMap<>();
     private int highestLevel = 0;
     private int nextStateNumber = 1;
     private Sources sources;
     private boolean conflicts = true;
     private List<Conflict> conflictList;
     private int conflictCount;
     private MapSet<Lr0State, Symbol> visited = new NumMapSet2<>();
     private List<LaState> laStateList = new ArrayList<>();
     private List<State> stateList = new ArrayList<>();
     private List<Lr0State> lr0StateList = new ArrayList<>();
     private Accept start;
     private Nil nil;
     private Omega omega;
     private Empty empty;
     private Eof eof;
     private Err error;
     private boolean debug;
     // following structures are not in original jikes code
     private Set<Set<GTerminal>> laShiftTable = new HashSet<>();
     private Set<Set<GTerminal>> lr0InputTable = new HashSet<>();
     private Set<Set<GTerminal>> laInputTable = new HashSet<>();
     private EnumSet<ObjectType> usedTypes = EnumSet.noneOf(ObjectType.class);
 
     public LALRKParserGenerator(int lalrLevel, List<GRule> rules, List<Symbol> symbols, List<Nonterminal> nonterminals, List<GTerminal> terminals)
     {
         this.lalrLevel = lalrLevel;
         this.rules = rules;
         this.symbols = symbols;
         this.nonterminals = nonterminals;
         this.terminals = terminals;
         Collections.sort(rules);
         Collections.sort(terminals);
         for (Nonterminal nt : nonterminals)
         {
             if (nt instanceof Accept)
             {
                 start = (Accept) nt;
             }
             if (nt instanceof Nil)
             {
                 nil = (Nil) nt;
             }
             if (nt instanceof Omega)
             {
                 omega = (Omega) nt;
             }
         }
         if (start == null)
         {
             throw new IllegalArgumentException("No Start nonterminal");
         }
         if (nil == null)
         {
             nil = new Nil();
         }
         if (omega == null)
         {
             omega = new Omega();
         }
         for (GTerminal t : terminals)
         {
             if (t instanceof Empty)
             {
                 empty = (Empty) t;
             }
             if (t instanceof Eof)
             {
                 eof = (Eof) t;
             }
             if (t instanceof Err)
             {
                 error = (Err) t;
             }
         }
         if (empty == null)
         {
             empty = new Empty();
         }
         if (eof == null)
         {
             eof = new Eof();
         }
         if (error == null)
         {
             error = new Err();
         }
         mkFirst();
         mkLr0();
         mkreducts();
         computeInputTable();
 //        print(System.err);
     }
 
     public EnumSet<ObjectType> getUsedTypes()
     {
         return usedTypes;
     }
 
     public int getLrkLevel()
     {
         return highestLevel;
     }
 
     public boolean isLrk()
     {
         return !laStateList.isEmpty();
     }
 
     public List<LaState> getLaStateList()
     {
         return laStateList;
     }
 
     public List<Lr0State> getLr0StateList()
     {
         return lr0StateList;
     }
 
     public Set<Set<GTerminal>> getLaInputTable()
     {
         return laInputTable;
     }
 
     public Set<Set<GTerminal>> getLr0InputTable()
     {
         return lr0InputTable;
     }
 
     private void computeInputTable()
     {
         for (State state : stateList)
         {
             Set<GTerminal> set = new TreeSet<>();
             if (state instanceof Lr0State)
             {
                 Lr0State lr0State = (Lr0State) state;
                 for (Shift shift : lr0State.getShiftList())
                 {
                     boolean add = set.add(shift.getSymbol());
                     assert add;
                 }
                 for (Reduce reduce : lr0State.getReduceList())
                 {
                     boolean add = set.add(reduce.getSymbol());
                     assert add;
                 }
                 /*
                 if (lr0InputTable.contains(set))
                 {
                     for (Set<GTerminal> l : lr0InputTable)
                     {
                         if (set.equals(l))
                         {
                             set = l;
                         }
                     }
                 }
                 else
                 {
                     lr0InputTable.add(set);
                 }
                  *
                  */
                 lr0InputTable.add(set);
             }
             else
             {
                 LaState laState = (LaState) state;
                 for (LaShift shift : laState.getShiftList())
                 {
                     boolean add = set.add(shift.getSymbol());
                     assert add;
                 }
                 for (LaReduce reduce : laState.getReduceList())
                 {
                     boolean add = set.add(reduce.getSymbol());
                     assert add;
                 }
                 /*
                 if (laInputTable.contains(set))
                 {
                     for (Set<GTerminal> l : laInputTable)
                     {
                         if (set.equals(l))
                         {
                             set = l;
                         }
                     }
                 }
                 else
                 {
                     laInputTable.add(set);
                 }
                  *
                  */
                 laInputTable.add(set);
             }
             state.setInputSet(set);
         }
     }
 
     public Set<Set<Shift>> getShiftMap()
     {
         return shiftMap;
     }
 
     public List<Nonterminal> nonterminals()
     {
         return nonterminals;
     }
 
     public List<State> getStateList()
     {
         return stateList;
     }
 
     public List<Item> getItemList()
     {
         return itemList;
     }
 
     public List<Nonterminal> getNonterminals()
     {
         return nonterminals;
     }
 
     public List<GRule> getRules()
     {
         return rules;
     }
 
     public List<Symbol> getSymbols()
     {
         return symbols;
     }
 
     public List<GTerminal> getTerminals()
     {
         return terminals;
     }
 
     private void computeMaxStack(Nonterminal nt)
     {
         stack.push(nt);
 
         int index = stack.size();
         setIndexOf(nt, index);
 
         int stackSize = 0;
 
         for (GRule rule : nt.getLhsRule())
         {
             int idx = 1;
             Member reducer = rule.getReducer();
             if (reducer != null)
             {
                 Type[] params = Generics.getParameterTypes(reducer);
                 Annotation[][] parameterAnnotations = Generics.getParameterAnnotations(reducer);
                 for (int ii=0;ii<params.length;ii++)
                 {
                     boolean pc = false;
                     for (Annotation a : parameterAnnotations[ii])
                     {
                         if (a.annotationType().equals(ParserContext.class))
                         {
                             pc = true;
                             break;
                         }
                     }
                     if (!pc)
                     {
                         ObjectType ot = ObjectType.valueOf(params[ii]);
                         usedTypes.add(ot);
                     }
                 }
             }
             for (Symbol symbol : rule.getRight())
             {
                 if (symbol instanceof Nonterminal)
                 {
                     Nonterminal ntSymbol = (Nonterminal) symbol;
                     if (indexOf(ntSymbol) == 0)
                     {
                         computeMaxStack(ntSymbol);
                     }
                     setIndexOf(nt, Math.min(indexOf(nt), indexOf(ntSymbol)));
                     if (idx == 1 && nt.equals(ntSymbol))
                     {
                         stackSize = Math.max(stackSize, idx);
                     }
                     else
                     {
                         if (indexOf(ntSymbol) != INFINITY)   // loop
                         {
                             stackSize = INFINITY;
                             break;
                         }
                         else
                         {
                             stackSize = Math.max(stackSize, idx-1+ntSymbol.getStackSize());
                         }
                     }
                 }
                 else
                 {
                     GTerminal t = (GTerminal) symbol;
                     stackSize = Math.max(stackSize, idx);
                     ObjectType ot = ObjectType.valueOf(t.getReducerType());
                 }
                 idx++;
             }
         }
         if (indexOf(nt) == index)
         {
             Nonterminal symbol = (Nonterminal) stack.peek();
             while (!symbol.equals(nt))
             {
                 stack.pop();
                 symbol.setStackSize(stackSize);
                 setIndexOf(symbol, INFINITY);
                 symbol = (Nonterminal) stack.peek();
             }
             setIndexOf(nt, INFINITY);
             stack.pop();
         }
         nt.setStackSize(stackSize);
     }
 
     private void updateMax(int[] a1, int[] au)
     {
         assert a1.length == au.length;
         for (int ii=0;ii<a1.length;ii++)
         {
             a1[ii] = Math.max(a1[ii], au[ii]);
         }
     }
     public int getStackSize()
     {
         return start.getStackSize();
     }
 
     private void mkFirst()
     {
         indexMap = new NumMap<>();
         stack = new ArrayDeque<>();
         for (Nonterminal nt : nonterminals)
         {
             if (indexOf(nt) == 0)
             {
                 computeMaxStack(nt);
             }
         }
         indexMap = new NumMap<>();
         stack = new ArrayDeque<>();
         for (Nonterminal nt : nonterminals)
         {
             if (indexOf(nt) == 0)
             {
                 computeClosure(nt);
             }
         }
 
         computeNullable();
 
         indexMap = new NumMap<>();
         stack = new ArrayDeque<>();
         for (Nonterminal nt : nonterminals)
         {
             if (indexOf(nt) == 0)
             {
                 computeFirst(nt);
             }
         }
 
         if (start.isNullable())
         {
             start.setNullable(false);
             Set<GTerminal> temp = start.getFirstSet();
             temp.remove(empty);
             temp.add(eof);
         }
         checkNonTerminals();
 
         List<List<Symbol>> firstElement = new ArrayList<>();
         HashMapList<Integer,Item> iMap = new HashMapList<>();
 
         List<Symbol> emptyList = new ArrayList<>();
         firstElement.add(emptyList);
 
         GRule rule0 = rules.get(0);
         Item item0 = new Item(0, rule0, empty, 0);
         itemList.add(item0);
 
         for (GRule rule : rules)
         {
             int j = 0;
             int i = 0;
             int k = rule.getRight().size() - 1;
             for (Symbol symbol : rule.getRight())
             {
                 Item item = new Item(itemList.size(), rule, symbol, j);
                 itemList.add(item);
                 rule.addItem(item);
                 if (lalrLevel > 1
                         || symbol instanceof Nonterminal
                         || empty.equals(symbol))
                 {
                     if (i == k)
                     {
                         iMap.add(0, item);
                     }
                     else
                     {
                         List<Symbol> ls = rule.getRight().subList(i + 1, k + 1);
                         int idx = firstElement.indexOf(ls);
                         if (idx == -1)
                         {
                             iMap.add(firstElement.size(), item);
                             firstElement.add(ls);
                         }
                         else
                         {
                             iMap.add(idx, item);
                         }
                     }
                 }
                 i++;
                 j++;
             }
             Item item = new Item(itemList.size(), rule, empty, j);
             itemList.add(item);
             rule.addItem(item);
         }
 
         int index = 0;
         for (List<Symbol> list : firstElement)
         {
             Set<GTerminal> set = sFirst(list);
             for (Item item : iMap.get(index))
             {
                 item.setSuffix(list);
                 item.setFirstSet(set);
             }
             index++;
         }
         for (GRule rule : start.getLhsRule())
         {
             Item item = rule.getItem(0);
             item.setSuffix(new ArrayList<Symbol>());
             Set<GTerminal> set = new NumSet<>();
             set.add(eof);
             item.setFirstSet(set);
         }
 
         if (readReduce)
         {
             for (GRule rule : rules)
             {
                 int j = rule.getRight().size();
                 if (!start.equals(rule.getLeft()) && j > 0)
                 {
                     rule.setAdequateItem();
                 }
             }
         }
 
         for (Nonterminal nt : nonterminals)
         {
             for (GRule rule : nt.getLhsRule())
             {
                 nt.addFirstItem(rule.getItem(0));
             }
         }
     /***************************************************************/
     /* If LALR_LEVEL > 1, we need to calculate RMPSELF, a set that */
     /* identifies the nonterminals that can right-most produce     */
     /* themselves. In order to compute RMPSELF, the map PRODUCES   */
     /* must be constructed which identifies for each nonterminal   */
     /* the set of nonterminals that it can right-most produce.     */
     /***************************************************************/
         if (lalrLevel > 1)
         {
             for (Nonterminal nt : nonterminals)
             {
                 for (Item item : nt.getFirstItems())
                 {
                     Symbol symbol = item.getSymbol();
                     if (symbol instanceof Nonterminal)
                     {
                         if (item.getFirstSet().contains(empty)
                                 && (!nt.getProduces().contains(symbol)))
                         {
                             nt.addProduces((Nonterminal) symbol);
                             nt.addDirectProduces((Nonterminal) symbol);
                         }
                     }
                 }
             }
             indexMap = new NumMap<>();
             stack = new ArrayDeque<>();
             for (Nonterminal nt : nonterminals)
             {
                 if (indexOf(nt) == 0)
                 {
                     computeProduces(nt);
                 }
             }
             initRmpSelf();
         }
     }
 
     private void mkLr0()
     {
         /*****************************************************************/
         /* Kernel of the first state consists of the first items in each */
         /* rule produced by Accept non-terminal.                         */
         /*****************************************************************/
         Set<Item> kernel = new TreeSet<>();
         for (Item item : start.getFirstItems())
         {
             kernel.add(item);
         }
         /*****************************************************************/
         /* Insert first state in STATE_TABLE and keep constructing states*/
         /* until we no longer can.                                       */
         /*****************************************************************/
         Lr0State state = lr0StateMap(kernel);
         while (!newStates.isEmpty())
         {
             state = newStates.removeFirst();
             /******************************************************************/
             /* Now we construct a list of all non-terminals that can be       */
             /* introduced in this state through closure.  The CLOSURE of each */
             /* non-terminal has been previously computed in MKFIRST.          */
             /******************************************************************/
             List<Nonterminal> ntList = new ArrayList<Nonterminal>();
             Iterator<Item> ii = state.getKernelItemsPtr();
             while (ii.hasNext())
             {
                 Item item = ii.next();
                 Symbol symbol = item.getSymbol();
                 if (debug) System.err.println("State "+state.getNumber()+" item "+item.getNumber()+" symbol "+symbol.getNumber());
                 if (symbol instanceof Nonterminal)
                 {
                     Nonterminal nt = (Nonterminal) symbol;
                     if (!ntList.contains(nt))
                     {
                         ntList.add(0, nt);
                         for (Nonterminal n : nt.getClosure())
                         {
                             if (!ntList.contains(n))
                             {
                                 ntList.add(0, n);
                             }
                         }
                     }
                 }
             }
             /*******************************************************************/
             /*   We now construct lists of all start items that the closure    */
             /* non-terminals produce.  A map from each non-terminal to its set */
             /* start items has previously been computed in MKFIRST. (CLITEMS)  */
             /* Empty items are placed directly in the state, whereas non_empty */
             /* items are placed in a temporary list rooted at CLOSURE_ROOT.    */
             /*******************************************************************/
             List<Item> closureList = new ArrayList<Item>();
             for (Nonterminal symbol : ntList)
             {
                 for (Item item : symbol.getFirstItems())
                 {
                     if (item.isFinal())
                     {
                         state.addCompleteItem(item);
                     }
                     else
                     {
                         closureList.add(item);
                     }
                 }
             }
             ii = state.getKernelItemsPtr();
             while (ii.hasNext())
             {
                 closureList.add(ii.next());
             }
 
             /*******************************************************************/
             /*   In this loop, the PARTITION map is constructed. At this point,*/
             /* ITEM_PTR points to all the non_complete items in the closure of */
             /* the state, plus all the kernel items.  We note that the kernel  */
             /* items may still contain complete-items, and if any is found, the*/
             /* COMPLETE_ITEMS list is updated.                                 */
             /*******************************************************************/
             HashMapTreeSet<Symbol, Item> partition = new HashMapTreeSet<>();    // it is important to keep order like this!!!
             List<Symbol> list = new ArrayList<>();
             for (Item item : closureList)
             {
                 Symbol symbol = item.getSymbol();
                 if (!empty.equals(symbol))
                 {
                     Item nextItem = item.next();
                     if (!partition.containsKey(symbol))
                     {
                         list.add(0, symbol);
                     }
                     partition.add(symbol, nextItem);
                 }
                 else
                 {
                     state.addCompleteItem(item);
                 }
             }
             /*******************************************************************/
             /* We now iterate over the set of partitions and update the state  */
             /* automaton and the transition maps: SHIFT and GOTO. Each         */
             /* partition represents the kernel of a state.                     */
             /*******************************************************************/
             for (Symbol symbol : list)
             {
                 Action action = null;
                 Lr0State newState = null;
                 /*****************************************************************/
                 /* If the partition contains only one item, and it is adequate   */
                 /* (i.e. the dot immediately follows the last symbol), and       */
                 /* READ-REDUCE is requested, a new state is not created, and the */
                 /* action is marked as a Shift-reduce or a Goto-reduce. Otherwise*/
                 /* if a state with that kernel set does not yet exist, we create */
                 /* it.                                                           */
                 /*****************************************************************/
                 Set<Item> q = partition.get(symbol);
                 if (readReduce && q.size() == 1)
                 {
                     Item item = q.iterator().next();
                     if (item.isFinal())
                     {
                         GRule rule = item.getRule();
                         if (!start.equals(rule.getLeft()))
                         {
                             action = rule;
                         }
                     }
                 }
 
                 if (action == null)
                 {
                     newState = lr0StateMap(q);
                     action = newState;
                     if (debug) System.err.println("new state "+newState.getNumber());
                 }
             /****************************************************************/
             /* At this stage, the partition list has been freed (for an old */
             /* state or an ADEQUATE item), or used (for a new state).  The  */
             /* PARTITION field involved should be reset.                    */
             /****************************************************************/
                 partition.remove(symbol);           /* To be reused */
 
             /*****************************************************************/
             /* At this point, ACTION contains the value of the state to Shift*/
             /* to, or rule to Read-Reduce on. If the symbol involved is a    */
             /* terminal, we update the Shift map; else, it is a non-terminal */
             /* and we update the Goto map.                                   */
             /* Shift maps are constructed temporarily in SHIFT_ACTION.       */
             /* Later, they are inserted into a map of unique Shift maps, and */
             /* shared by states that contain identical shifts.               */
             /* Since the lookahead set computation is based on the GOTO maps,*/
             /* all these maps and their element maps should be kept as       */
             /* separate entities.                                            */
             /*****************************************************************/
                 if (symbol instanceof GTerminal)
                 {
                     GTerminal t = (GTerminal) symbol;
                     state.addShift(t, action);
                     if (debug) System.err.println("Shift("+state.getNumber()+") "+symbol.getNumber()+" -> "+action.getNumber());
                 }
                 else
                 {
                     Nonterminal nt = (Nonterminal) symbol;
                     state.addGoto(nt, action);
                     if (debug) System.err.println("Goto("+state.getNumber()+") "+symbol.getNumber()+" -> "+action.getNumber());
                 }
             }
             state.setShiftMap(shiftMap(state.getShiftList()));
         }
         stateTable = null;
     }
 
     private Set<Shift> shiftMap(Set<Shift> shiftList)
     {
         if (shiftMap.contains(shiftList))
         {
             for (Set<Shift> sma : shiftMap)
             {
                 if (shiftList.equals(sma))
                 {
                     return sma;
                 }
             }
         }
         else
         {
             shiftMap.add(shiftList);
         }
         return shiftList;
     }
 
     private Lr0State lr0StateMap(Set<Item> kernel)
     {
         Lr0State state = stateTable.get(kernel);
         if (state == null)
         {
             state = new Lr0State(nextStateNumber++, kernel);
             stateTable.put(kernel, state);
             newStates.addLast(state);
             stateList.add(state);
             lr0StateList.add(state);
         }
         return state;
     }
 
     private void mkreducts()
     {
         initLalrkProcess();
         /**********************************************************************/
         /* First, construct the IN_STAT map. Next, iterate over the states to */
         /* construct two boolean vectors.  One indicates whether there is a   */
         /* shift action on the ERROR symbol when the DEFAULT_OPT is 5.  The   */
         /* other indicates whether it is all right to take default action in  */
         /* states containing exactly one final item.                          */
         /*                                                                    */
         /* We also check whether the grammar is LR(0). I.e., whether it needs */
         /* any look-ahead at all.                                             */
         /**********************************************************************/
         buildInStat();
 
         for (Lr0State state : lr0StateList)
         {
             state.setNoShiftOnErrorSym(true);
             if (defaultOpt == 5)
             {
                 for (Shift shift : state.getShiftList())
                 {
                     if (error.equals(shift.getSymbol()))
                     {
                         state.setNoShiftOnErrorSym(false);
                     }
                 }
             }
             /**********************************************************************/
             /*   Compute whether this state is a final state.  I.e., a state that */
             /* contains only a single complete item. If so, mark it as a default  */
             /* state. Note that if the READ-REDUCE option is used, the automaton  */
             /* will not contain such states. Also, states are marked only when    */
             /* default actions are requested.                                     */
             /**********************************************************************/
             PeekableIterator<Item> itemPtr = state.getKernelItemsPtr();
             state.setSingleCompleteItem(
                     !readReduce
                     && !singleProductions
                     && defaultOpt > 0
                     && !itemPtr.hasNext()
                     && itemPtr.peek().isFinal());
             /**********************************************************************/
             /* If a state has a complete item, and more than one kernel item      */
             /* which is different from the complete item, then this state         */
             /* requires look-ahead for the complete item.                         */
             /**********************************************************************/
             if (highestLevel == 0)
             {
                 PeekableIterator<Item> r = state.getCompleteItemsPtr();
                 if (r != null)
                 {
                     if (itemPtr.hasNext()
                             || !itemPtr.peek().equals(r.peek()))
                     {
                         highestLevel = 1;
                     }
                 }
             }
         }
         computeRead();
 
         int[] ruleCount = new int[rules.size()];
         /****************************************************************/
         /* We are now ready to construct the reduce map. First, we      */
         /* initialize MAX_LA_STATE to NUM_STATES. If no lookahead       */
         /* state is added (the grammar is LALR(1)) this value will not  */
         /* change. Otherwise, MAX_LA_STATE is incremented by 1 for each */
         /* lookahead state added.                                       */
         /****************************************************************/
         int numStates = lr0StateList.size();
 
         Set<GTerminal> lookAhead = new NumSet<>();
         MapList<GTerminal, Item> action = new NumMapList<>(new RHSComparator());
         Set<GTerminal> symbolList = new NumSet<>();
 
         for (Lr0State stateNo : lr0StateList)
         {
             GRule defaultRule = null;
             symbolList.clear();
 
             PeekableIterator<Item> itemPtr = stateNo.getCompleteItemsPtr();
             if (itemPtr.hasNext())
             {
     /**********************************************************************/
     /* Check if it is possible to take default reduction. The DEFAULT_OPT */
     /* parameter indicates what kind of default options are requested.    */
     /* The various values it can have are:                                */
     /*                                                                    */
     /*    a)   0 => no default reduction.                                 */
     /*    b)   1 => default reduction only on adequate states. I.e.,      */
     /*              states with only one complete item in their kernel.   */
     /*    c)   2 => Default on all states that contain exactly one        */
     /*              complete item not derived from an empty rule.         */
     /*    d)   3 => Default on all states that contain exactly one        */
     /*              complete item including items from empty rules.       */
     /*    e)   4 => Default reduction on all states that contain exactly  */
     /*              one item. If a state contains more than one item we   */
     /*              take Default on the item that generated the most      */
     /*              reductions. If there is a tie, one is selected at     */
     /*              random.                                               */
     /*    f)   5 => Same as 4 except that no default actions are computed */
     /*              for states that contain a shift action on the ERROR   */
     /*              symbol.                                               */
     /*                                                                    */
     /*  In the code below, we first check for category 3.  If it is not   */
     /* satisfied, then we check for the others. Note that in any case,    */
     /* default reductions are never taken on the ACCEPT rule.             */
     /*                                                                    */
     /**********************************************************************/
                 Item itemNo = itemPtr.peek();
                 GRule ruleNo = itemNo.getRule();
                 Nonterminal symbol = ruleNo.getLeft();
                 if (stateNo.isSingleCompleteItem() && !start.equals(symbol))
                 {
                     defaultRule = ruleNo;
                     itemPtr = null; /* No need to check for conflicts */
                 }
                 /******************************************************************/
                 /* Iterate over all complete items in the state, build action     */
                 /* map, and check for conflicts.                                  */
                 /******************************************************************/
                 for (;itemPtr != null && itemPtr.hasNext();itemPtr.next())
                 {
                     itemNo = itemPtr.peek();
                     ruleNo = itemNo.getRule();
                     computeLa(stateNo, itemNo, lookAhead);
                     for (GTerminal tsymbol : lookAhead)
                     {
                         if (!action.containsKey(tsymbol))
                         {
                             symbolList.add(tsymbol);
                         }
                         action.add(tsymbol, itemNo);
                     }
                 }
                 /******************************************************************/
                 /* At this stage, we have constructed the ACTION map for STATE_NO.*/
                 /* ACTION is a map from each symbol into a set of final items.    */
                 /* The rules associated with these items are the rules by which   */
                 /* to reduce when the lookahead is the symbol in question.        */
                 /* SYMBOL_LIST/SYMBOL_ROOT is a list of the non-empty elements of */
                 /* ACTION. If the number of elements in a set ACTION(t), for some */
                 /* terminal t, is greater than one or it is not empty and STATE_NO*/
                 /* contains a shift action on t then STATE_NO has one or more     */
                 /* conflict(s). The procedure RESOLVE_CONFLICTS takes care of     */
                 /* resolving the conflicts appropriately and returns an ACTION    */
                 /* map where each element has either 0 (if the conflicts were     */
                 /* shift-reduce conflicts, the shift is given precedence) or 1    */
                 /* element (if the conflicts were reduce-reduce conflicts, only   */
                 /* the first element in the ACTION(t) list is returned).          */
                 /******************************************************************/
                 if (!symbolList.isEmpty())
                 {
                     resolveConflicts(stateNo, symbolList, action);
                     for (GTerminal sym : symbolList)
                     {
                         if (action.containsKey(sym))
                         {
                             itemNo = action.get(sym).get(0);
                             ruleCount[itemNo.getRule().getNumber()]++;
                         }
                     }
                 }
             }
             /*********************************************************************/
             /* We are now ready to compute the size of the reduce map for        */
             /* STATE_NO (reduce_size) and the default rule.                      */
             /* If the state being processed contains only a single complete item */
             /* then the DEFAULT_RULE was previously computed and the list of     */
             /* symbols is empty.                                                 */
             /* NOTE: a REDUCE_ELEMENT will be allocated for all states, even     */
             /* those that have no reductions at all. This will facilitate the    */
             /* Table Compression routines, for they can assume that such an      */
             /* object exists, and can be used for Default values.                */
             /*********************************************************************/
             if (!symbolList.isEmpty())
             {
                 /******************************************************************/
                 /* Compute REDUCE_SIZE, the number of reductions in the state and */
                 /* DEFAULT_RULE: the rule with the highest number of reductions   */
                 /* to it.                                                         */
                 /******************************************************************/
                 int n = 0;
                 for (PeekableIterator<Item> p = stateNo.getCompleteItemsPtr();p.hasNext();p.next())
                 {
                     Item itemNo = p.peek();
                     GRule ruleNo = itemNo.getRule();
                     Nonterminal symbol = ruleNo.getLeft();
                     if (ruleCount[ruleNo.getNumber()] > n
                             && stateNo.isNoShiftOnErrorSym()
                             && !start.equals(symbol))
                     {
                         n = ruleCount[ruleNo.getNumber()];
                         defaultRule = ruleNo;
                     }
                 }
                 /*********************************************************/
                 /*   If the removal of single productions is requested   */
                 /* and/or parsing tables will not be output, figure out  */
                 /* if the level of the default option requested permits  */
                 /* default actions, and compute how many reduce actions  */
                 /* can be eliminated as a result.                        */
                 /*********************************************************/
                 if (defaultOpt == 0)
                 {
                     defaultRule = null;
                 }
                 else
                 {
                     if (!singleProductions)
                     {
                         PeekableIterator<Item> q = stateNo.getCompleteItemsPtr();
                         if (!q.hasNextNext())
                         {
                             Item itemNo = q.peek();
                             GRule ruleNo = itemNo.getRule();
                             if (!(defaultOpt > 2
                                     || (defaultOpt == 2 && !ruleNo.getRight().isEmpty())))
                             {
                                 defaultRule = null;
                             }
                         }
                     }
                 }
             }
             /**************************************************************/
             /*   NOTE that the default fields are set for all states,     */
             /* whether or not DEFAULT actions are requested. This is      */
             /* all right since one can always check whether (DEFAULT > 0) */
             /* before using these fields.                                 */
             /**************************************************************/
             stateNo.setDefaultReduce(defaultRule);
             for (GTerminal symbol : symbolList)
             {
                 if (action.containsKey(symbol))
                 {
                     GRule ruleNo = action.get(symbol).get(0).getRule();
                     if (!ruleNo.equals(defaultRule) || singleProductions)
                     {
                         stateNo.addReduce(symbol, ruleNo);
                     }
                     action.remove(symbol);
                 }
             }
         /************************************************************/
         /* Reset RULE_COUNT elements used in this state.            */
         /************************************************************/
             for (PeekableIterator<Item> q = stateNo.getCompleteItemsPtr();q.hasNext();q.next())
             {
 
                 GRule ruleNo = q.peek().getRule();
                 ruleCount[ruleNo.getNumber()] = 0;
             }
         } /* end for ALL_STATES*/
         /****************************************************************/
         /* If the automaton required multiple lookahead, construct the  */
         /* permanent lookahead states.                                  */
         /****************************************************************/
         if (!laStateList.isEmpty())
         {
             createLaStats();
         }
         /****************************************************************/
         /* If the removal of single productions is requested, do that.  */
         /****************************************************************/
         if (singleProductions)
         {
             removeSingleProductions();
         }
         /****************************************************************/
         /* If either more than one lookahead was needed or the removal  */
         /* of single productions was requested, the automaton was       */
         /* transformed with the addition of new states and new          */
         /* transitions. In such a case, we reconstruct the IN_STAT map. */
         /****************************************************************/
         if (lalrLevel > 1 || singleProductions)
         {
             buildInStat();
         }
         /******************************************************************/
         /* Print informational messages and free all temporary space that */
         /* was used to compute lookahead information.                     */
         /******************************************************************/
         if (conflictCount != 0)
         {
             if (highestLevel != INFINITY)
             {
                 throw new GrammarException("This grammar is not LALR(" + highestLevel + ")");
             }
             else
             {
                 throw new GrammarException("This grammar is not LALR(K).\n");
             }
         }
         else
         {
             if (highestLevel == 0)
             {
                 System.err.println("This grammar is LR(0).\n");
             }
             else
             {
                 System.err.println("This grammar is LALR(" + highestLevel + ")");
             }
         }
     }
 
     private void removeSingleProductions()
     {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     private void createLaStats()
     {
         for (LaState laState : laStateList)
         {
             State s = laState.getInState();
             if (s instanceof Lr0State)
             {
                 Lr0State state = (Lr0State) s;
                 Set<Shift> set = new HashSet<>();
                 set.addAll(state.getShiftList());
                 Iterator<Shift> si = set.iterator();
                 while (si.hasNext())
                 {
                     Shift shift = si.next();
                     if (shift.getSymbol().equals(laState.getSymbol()))
                     {
                         Action action = shift.getAction();
                         if (action instanceof Lr0State)
                         {
                             Lr0State st = (Lr0State) action;
                             st.getInStates().remove(state);
                         }
                         si.remove();
                     }
                 }
                 set.add(new Shift(laState.getSymbol(), laState));
                 state.setShiftMap(shiftMap(set));
             }
         }
     }
 
     /***********************************************************************/
     /* If conflicts were detected and LALR(k) processing was requested,    */
     /* where k > 1, then we attempt to resolve the conflicts by computing  */
     /* more lookaheads.  Shift-Reduce conflicts are processed first,       */
     /* followed by Reduce-Reduce conflicts.                                */
     /***********************************************************************/
     private void resolveConflicts(Lr0State stateNo, Set<GTerminal> symbolList, MapList<GTerminal, Item> action)
     {
         conflictList = new ArrayList<>();
         for (Shift sh : stateNo.getShiftList())
         {
             GTerminal symbol = sh.getSymbol();
             if (singleProductions && action.containsKey(symbol))
             {
                 addConflictSymbol(stateNo, symbol);
             }
             if (lalrLevel > 1 && action.containsKey(symbol))
             {
                 sources = new Sources();
 
                 StackElement q = new StackElement(stateNo);
 
                 Action ac = sh.getAction();
                 if (ac instanceof Lr0State)
                 {
                     sources.addConfigs((Act) ac,q); // S + num_rules
                 }
                 else
                 {
                     sources.addConfigs(new ShiftReduceAct((GRule) ac), q);   // -rule
                 }
                 for (Item itemNo : action.get(symbol))
                 {
                     ReduceAct ract = new ReduceAct(itemNo.getRule());
                     Nonterminal lhsSymbol = itemNo.getRule().getLeft();
 
                     visited.clear();
 
                     for (Lr0State v : lpgAccess(stateNo, itemNo))
                     {
                         q = new StackElement(v);
 
                         StackElement newConfigs = followSources(q, lhsSymbol, symbol);
                         if (newConfigs != null)
                         {
                             sources.addConfigs(ract, newConfigs);   // +rule
                         }
                     }
                 }
                 /***************************************************************/
                 /* The function STATE_TO_RESOLVE_CONFLICTS returns a pointer   */
                 /* value to a STATE_ELEMENT which has been constructed to      */
                 /* resolve the conflicts in question. If the value returned by */
                 /* that function is NULL, then it was not possible to resolve  */
                 /* the conflicts.  In any case, STATE_TO_RESOLVE_CONFLICTS     */
                 /* frees the space that is used by the action map headed by    */
                 /* ACTION_ROOT.                                                */
                 /***************************************************************/
                 LaState state = stateToResolveConflicts(sources, symbol, 2);
                 if (state != null)
                 {
                     state.setInState(stateNo);
                     action.remove(symbol);
                 }
             }
             /***************************************************************/
             /* If unresolved shift-reduce conflicts are detected on symbol,*/
             /*  add them to the list of conflicts so they can be reported  */
             /* (if the CONFLICT option is on) and count them.              */
             /***************************************************************/
             if (action.containsKey(symbol))
             {
                 Action act = sh.getAction();
                 for (Item p : action.get(symbol))
                 {
                     if (conflicts)
                     {
                         SRConflict q = new SRConflict(act, p, symbol);
                         conflictList.add(q);
                         conflictCount++;
                     }
                 }
                 /***********************************************************/
                 /* Remove reduce actions defined on symbol so as to give   */
                 /* precedence to the shift.                                */
                 /***********************************************************/
                 action.remove(symbol);
             }
         }
         /*******************************************************************/
         /* We construct a map from each action to a list of states as we   */
         /* did for the Shift-reduce conflicts. A boolean vector ITEM_SEEN  */
         /* is used to prevent duplication of actions. This problem does    */
         /* not occur with Shift-Reduce conflicts.                          */
         /*******************************************************************/
         for (GTerminal symbol : symbolList)
         {
             if (action.containsKey(symbol))
             {
                 if (singleProductions && action.get(symbol).size() > 1)
                 {
                     addConflictSymbol(stateNo, symbol);
                 }
                 if (lalrLevel > 1 && action.get(symbol).size() > 1)
                 {
                     sources = new Sources();
                     for (Item p : action.get(symbol))
                     {
                         Item itemNo = p;
                         ReduceAct ract = new ReduceAct(itemNo.getRule());
                         Nonterminal lhsSymbol = itemNo.getRule().getLeft();
 
                         visited.clear();
 
                         for (Lr0State s : lpgAccess(stateNo, itemNo))
                         {
                             StackElement q = new StackElement(s);
 
                             StackElement newConfigs = followSources(q, lhsSymbol, symbol);
                             if (newConfigs != null)
                             {
                                 sources.addConfigs(ract, newConfigs);
                             }
                         }
                     }
                     /***************************************************************/
                     /*     STATE_TO_RESOLVE_CONFLICTS will return a pointer to a   */
                     /* STATE_ELEMENT if the conflicts were resolvable with more    */
                     /* lookaheads, otherwise, it returns NULL.                     */
                     /***************************************************************/
                     LaState state = stateToResolveConflicts(sources, symbol, 2);
                     if (state != null)
                     {
                         state.setInState(stateNo);
                         action.remove(symbol);
                     }
                 }
                 /***********************************************************/
                 /* If unresolved reduce-reduce conflicts are detected on   */
                 /* symbol, add them to the list of conflicts so they can be*/
                 /* reported (if the CONFLICT option is on) and count them. */
                 /***********************************************************/
                 if (action.containsKey(symbol))
                 {
                     Iterator<Item> ii = action.get(symbol).iterator();
                     Item act = ii.next();
                     while (ii.hasNext())
                     {
                         Item p = ii.next();
                         if (conflicts)
                         {
                             RRConflict q = new RRConflict(act, p, symbol);
                             conflictList.add(q);
                             conflictCount++;
                         }
                     }
                     /***********************************************************/
                     /* Remove all reduce actions that are defined on symbol    */
                     /* except the first one. That rule is the one with the     */
                     /* longest right-hand side that was associated with symbol.*/
                     /* See code in MKRED.C.                                    */
                     /***********************************************************/
                     while (action.get(symbol).size() > 1)
                     {
                         action.get(symbol).remove(1);
                     }
                 }
             }
         }
         /*******************************************************************/
         /* If any unresolved conflicts were detected, process them.        */
         /*******************************************************************/
         if (!conflictList.isEmpty())
         {
             processConflicts(stateNo);
         }
     }
 
     private void processConflicts(Lr0State stateNo)
     {
         System.err.println("Conflict on state "+stateNo);
         for (Conflict conflict : conflictList)
         {
             System.err.println(conflict);
         }
     }
 
     private LaState stateToResolveConflicts(Sources sources, GTerminal laSymbol, int level)
     {
         Set<GTerminal> lookAhead = new NumSet<>();
         MapList<GTerminal, Act> action = new NumMapList<>();
         Set<GTerminal> symbolSet = new NumSet<>();
         Set<GTerminal> shiftSet = new NumSet<>();
         Set<GTerminal> reduceSet = new NumSet<>();
         Map<GTerminal,LaState> laShiftState = new NumMap<>();
         int[] ruleCount = new int[rules.size()];
         LaState state = null;
 
         Sources newSources = new Sources();
 
         if (level > highestLevel)
         {
             highestLevel = level;
         }
     /*******************************************************************/
     /* One of the parameters received is a SOURCES map whose domain is */
     /* a set of actions and each of these actions is mapped into a set */
     /* of configurations that can be reached after that action is      */
     /* executed (in the state where the conflicts were detected).      */
     /* In this loop, we compute an ACTION map which maps each each     */
     /* terminal symbol into 0 or more actions in the domain of SOURCES.*/
     /*                                                                 */
     /* NOTE in a sources map, if a configuration is associated with    */
     /* more than one action then the grammar is not LALR(k) for any k. */
     /* We check for that condition below. However, this check is there */
     /* for purely cosmetic reason. It is not necessary for the         */
     /* algorithm to work correctly and its removal will speed up this  */
     /* loop somewhat (for conflict-less input).                        */
     /* The first loop below initializes the hash table used for        */
     /* lookups ...                                                     */
     /*******************************************************************/
         sources.clearStackSeen();
 
         for (Act act : sources.getActList())
         {
         /***************************************************************/
         /* For each action we iterate over its associated set of       */
         /* configurations and invoke NEXT_LA to compute the lookahead  */
         /* set for that configuration. These lookahead sets are in     */
         /* turn unioned together to form a lookahead set for the       */
         /* action in question.                                         */
         /***************************************************************/
             lookAhead.clear();
             for (StackElement stc=sources.getConfigs(act);stc != null;stc=stc.getNext())
             {
                 if (sources.stackWasSeen(stc))
                 {
                     highestLevel = INFINITY;
                     return state;
                 }
                 nextLa(stc, laSymbol, lookAhead);
             }
             lookAhead.remove(empty);
         /***************************************************************/
         /* For each lookahead symbol computed for this action, add an  */
         /* action to the ACTION map and keep track of the symbols on   */
         /* which any action is defined.                                */
         /* If new conflicts are detected and we are already at the     */
         /* lookahead level requested, we terminate the computation...  */
         /***************************************************************/
             int count = 0;
             for (GTerminal symbol : lookAhead)
             {
                 count++;
                 if (!action.containsKey(symbol))
                 {
                     symbolSet.add(symbol);
                 }
                 else
                 {
                     if (level == lalrLevel)
                     {
                         return state;
                     }
                 }
                 action.add(symbol, act);
             }
         /***************************************************************/
         /* If the action in question is a reduction then we keep track */
         /* of how many times it was used.                              */
         /***************************************************************/
             if (act instanceof ReduceAct)   // >0 && < num_rules
             {
                 ReduceAct ract = (ReduceAct) act;
                 ruleCount[ract.getNumber()] = count;
             }
         }
     /*******************************************************************/
     /* We now iterate over the symbols on which actions are defined.   */
     /* If we detect conflicts on any symbol, we compute new sources    */
     /* and try to recover by computing more lookahead. Otherwise, we   */
     /* update the counts and create two lists: a list of symbols on    */
     /* which shift actions are defined and a list of symbols on which  */
     /* reduce actions are defined.                                     */
     /*******************************************************************/
         for (GTerminal symbol : symbolSet)
         {
         /***************************************************************/
         /* We have four cases to consider:                             */
         /*    1. There are conflicts on SYMBOL                         */
         /*    2. The action on SYMBOL is a shift-reduce                */
         /*    3. The action on SYMBOL is a shift                       */
         /*    4. The action on SYMBOL is a reduce                      */
         /***************************************************************/
             if (action.get(symbol).size() > 1)
             {
                 newSources = new Sources();
                 for (Act act : action.get(symbol))
                 {
                     if (act instanceof ReduceAct)
                     {
                         GRule rule = (GRule) act;
                         ruleCount[rule.getNumber()]--;
                     }
                     visited.clear();
                     for (StackElement stc=sources.getConfigs(act);stc != null;stc=stc.getNext())
                     {
                         StackElement newConfigs = followSources(stc, laSymbol, symbol);
                         newSources.addConfigs(act, newConfigs);
                     }
                 }
                 action.remove(symbol);
 
                 state = stateToResolveConflicts(newSources, symbol, level + 1);
                 if (state == null)
                 {
                     return state;
                 }
                 laShiftState.put(symbol, state);
                 action.add(symbol, state);
                 shiftSet.add(symbol);
             }
             else
             {
                 if (action.get(symbol).get(0) instanceof ShiftReduceAct)    // -rule
                 {
                     shiftSet.add(symbol);
                 }
                 else
                 {
                     if (action.get(symbol).get(0) instanceof ShiftAct)  // > num_rules
                     {
                         shiftSet.add(symbol);
                     }
                     else
                     {
                         reduceSet.add(symbol);
                     }
                 }
             }
         }
     /*******************************************************************/
     /* We now iterate over the reduce actions in the domain of sources */
     /* and compute a default action.                                   */
     /*******************************************************************/
         GRule defaultRule = null;
         int count = 0;
         for (Act act : sources.getActList())
         {
             if (act instanceof ReduceAct)
             {
                 GRule rule = (GRule) act;
                 if (ruleCount[rule.getNumber()] > count)
                 {
                     count = ruleCount[rule.getNumber()];
                     defaultRule = rule;
                 }
             }
         }
     /*******************************************************************/
     /* By now, we are ready to create a new look-ahead state. The      */
     /* actions for the state are in the ACTION vector, and the         */
     /* constants: NUM_SHIFT_ACTIONS and NUM_REDUCE_ACTIONS indicate    */
     /* the number of shift and reduce actions in the ACTION vector.    */
     /* Note that the IN_STATE field of each look-ahead state created   */
     /* is initially set to the number associated with that state. If   */
     /* all the conflicts detected in the state, S, that requested the  */
     /* creation of a look-ahead state are resolved, then this field    */
     /* is updated with S.                                              */
     /* Otherwise, this field indicates that this look-ahead state is   */
     /* dangling - no other state point to it.                          */
     /*******************************************************************/
         state = new LaState();
         state.setSymbol(laSymbol);
         state.setNumber(nextStateNumber++);
         state.setInState(state);
     /*******************************************************************/
     /* If there are any shift-actions in this state, we create a shift */
     /* map for them if one does not yet exist, otherwise, we reuse the */
     /* old existing one.                                               */
     /*******************************************************************/
         if (!shiftSet.isEmpty())
         {
         /***************************************************************/
         /* In this loop, we compute the hash address as the number of  */
         /* shift actions, plus the sum of all the symbols on which a   */
         /* shift action is defined.  As a side effect, we also take    */
         /* care of some other issues. Shift actions which were encoded */
         /* to distinguish them from reduces action are decoded.        */
         /* The counters for shift and shift-reduce actions are updated.*/
         /* For all Shift actions to look-ahead states, the IN_STATE    */
         /* field of these look-ahead target states are updated.        */
         /***************************************************************/
             for (GTerminal symbol : shiftSet)
             {
                 if (action.get(symbol).get(0) instanceof LaState)
                 {
                     laShiftState.get(symbol).setInState(state);
                 }
             }
         /***************************************************************/
         /* Search list associated with HASH_ADDRESS, and if the shift  */
         /* map in question is found, update the SHIFT, and SHIFT_NUMBER*/
         /* fields of the new Look-Ahead State.                         */
         /***************************************************************/
             if (laShiftTable.contains(shiftSet))
             {
                 for (Set<GTerminal> sl : laShiftTable)
                 {
                     if (sl.equals(shiftSet))
                     {
                         shiftSet = sl;
                         break;
                     }
                 }
             }
             else
             {
                 laShiftTable.add(shiftSet);
             }
             for (GTerminal symbol : shiftSet)
             {
                 state.addShift(symbol, action.get(symbol).get(0));
             }
         }
     /*******************************************************************/
     /* Construct Reduce map.                                           */
     /* When SPACE or TIME tables are requested, no default actions are */
     /* taken.                                                          */
     /*******************************************************************/
         for (GTerminal symbol : reduceSet)
         {
             if (defaultOpt == 0 || !action.get(symbol).get(0).equals(defaultRule))
             {
                 state.addReduce(symbol, action.get(symbol).get(0));
             }
         }
         if (defaultOpt > 0)
         {
             state.setDefaultRule(defaultRule);
         }
         if (state.getReduceList().isEmpty() && state.getShiftList().isEmpty())
         {
             return null;    // no shift or reduce actions
         }
         laStateList.add(state);
         stateList.add(state);
         return state;
     }
 /***********************************************************************/
 /* This function has a similar structure as FOLLOW_SOURCES.  But,      */
 /* instead of computing configurations that can be reached, it         */
 /* computes lookahead symbols that can be reached.  It takes as        */
 /* argument a configuration STACK, a SYMBOL on which a transition can  */
 /* be made in the configuration and a set variable, LOOK_AHEAD, where  */
 /* the result is to be stored.  When NEXT_LA is invoked from the       */
 /* outside, LOOK_AHEAD is assumed to be initialized to the empty set.  */
 /* NEXT_LA first executes the transition on SYMBOL and thereafter, all */
 /* terminal symbols that can be read are added to LOOKAHEAD.           */
 /***********************************************************************/
     private void nextLa(StackElement stack, Symbol laSymbol, Set<GTerminal> lookAhead)
     {
     /*******************************************************************/
     /* The only symbol that can follow the end-of-file symbol is the   */
     /* end-of-file symbol.                                             */
     /*******************************************************************/
         if (eof.equals(laSymbol))
         {
             lookAhead.add(eof);
             return;
         }
         Lr0State state = stack.getState();
     /*******************************************************************/
     /* Find the transition defined on the symbol...                    */
     /*******************************************************************/
         Action action = null;
         if (laSymbol instanceof Nonterminal)
         {
             for (Goto go : state.getGotoList())
             {
                 if (laSymbol.equals(go.getSymbol()))
                 {
                     action = go.getAction();
                     break;
                 }
             }
         }
         else
         {
             for (Shift shift : state.getShiftList())
             {
                 if (laSymbol.equals(shift.getSymbol()))
                 {
                     action = shift.getAction();
                     break;
                 }
             }
         }
     /*******************************************************************/
     /* If the ACTion on the symbol is a shift or a goto, then all      */
     /* terminal symbols that can be read in ACT are added to           */
     /* LOOK_AHEAD.                                                     */
     /*******************************************************************/
         if (action instanceof State)
         {
             Lr0State st = (Lr0State) action;
             lookAhead.addAll(st.getReadSet());
         }
     /*******************************************************************/
     /* We now iterate over the kernel set of items associated with the */
     /* ACTion defined on SYMBOL...                                     */
     /* Recall that the READ_SET of ACT is but the union of the FIRST   */
     /* map defined on the suffixes of the items in the kernel of ACT.  */
     /*******************************************************************/
         Set<Item> itemPtr;
         if (action instanceof State)
         {
             Lr0State as = (Lr0State) action;
             itemPtr = as.getKernelItems();
         }
         else
         {
             GRule rule = (GRule) action;
             itemPtr = rule.getAdequateItem();
         }
         for (Item itemNo : itemPtr)
         {
         /***************************************************************/
         /* For each item that is a final item whose left-hand side     */
         /* is neither the starting symbol nor a symbol that can        */
         /* right-most produce itself...                                */
         /***************************************************************/
             if (itemNo.predessor().getFirstSet().contains(empty))
             {
                 GRule ruleNo = itemNo.getRule();
                 Nonterminal lhsSymbol = ruleNo.getLeft();
                 if (!start.equals(lhsSymbol) && !lhsSymbol.isRmpSelf())
                 {
                 /*******************************************************/
                 /* If the length of the prefix of the item preceeding  */
                 /* the dot is shorter that the length of the stack, we */
                 /* retrace the item's path within the stack and        */
                 /* invoke NEXT_LA with the prefix of the stack         */
                 /* where the item was introduced through closure, the  */
                 /* left-hand side of the item and LOOK_AHEAD.          */
                 /*******************************************************/
                     if (itemNo.getDot() < stack.getSize())
                     {
                         StackElement q = stack;
                         for (int i=1;i < itemNo.getDot();i++)
                         {
                             q = q.getPrevious();
                         }
                         nextLa(q, lhsSymbol, lookAhead);
                     }
                     else
                     {
                     /***************************************************/
                     /* Compute the item in the root state of the stack,*/
                     /* and find the root state...                      */
                     /***************************************************/
                         itemNo = itemNo.predessor(stack.getSize());
                         StackElement q = stack;
                         for (;q.getSize() != 1;q=q.getPrevious());
                     /***************************************************/
                     /* We are now back in the main automaton, find all */
                     /* sources where the item was introduced through   */
                     /* closure and add all terminal symbols in the     */
                     /* follow set of the left-hand side symbol in each */
                     /* source to LOOK_AHEAD.                           */
                     /***************************************************/
                         for (Lr0State p : lpgAccess(q.getState(), itemNo))
                         {
                             Goto go = null;
                             for (Goto g : p.getGotoList())
                             {
                                 if (lhsSymbol.equals(g.getSymbol()))
                                 {
                                     go = g;
                                     break;
                                 }
                             }
                         /***********************************************/
                         /* If look-ahead after left hand side is not   */
                         /* yet computed,call LA_TRAVERSE to compute it.*/
                         /***********************************************/
                             if (laIndexOf(go.getLa()) == 0)
                             {
                                 Deque<LaPtr> stackTop = new ArrayDeque<LaPtr>();
                                 laTraverse(p, go, stackTop);
                             }
                             lookAhead.addAll(go.getLa().getLaSet());
                         }
                     }
                 }
             }
         }
     }
 
     /***********************************************************************/
     /* This function takes as argument a configuration STACK, a SYMBOL on  */
     /* which a transition can be made in the configuration and a terminal  */
     /* lookahead symbol, LA_SYMBOL. It executes the transition on SYMBOL   */
     /* and simulates all paths taken in the automaton after that transition*/
     /* until new state(s) are reached where a transition is possible on    */
     /* the lookahead symbol. It then returns the new set of configurations */
     /* found on which a transition on LA_SYMBOL is possible.               */
     /***********************************************************************/
     private StackElement followSources(StackElement stack, Symbol symbol, GTerminal laSymbol)
     {
         /*******************************************************************/
         /* If the starting configuration consists of a single state and    */
         /* the initial [state, symbol] pair has already been visited,      */
         /* return the null set. Otherwise, mark the pair visited and ...   */
         /*******************************************************************/
         StackElement configs = null;
 
         Lr0State stateNo = stack.getState();
         if (stack.getSize() == 1)
         {
             if (visited.contains(stateNo, symbol)
                     || (stateNo.getNumber() == 1 && start.equals(symbol)))
             {
                 return configs;
             }
             visited.add(stateNo, symbol);
         }
         /*******************************************************************/
         /* Find the transition defined on the symbol...                    */
         /* If the SYMBOL is a nonterminal and we can determine that the    */
         /* lookahead symbol (LA_SYMBOL) cannot possibly follow the         */
         /* nonterminal in question in this context, we simply abandon the  */
         /* search and return the NULL set.                                 */
         /*******************************************************************/
         Action act = null;
         if (symbol instanceof Nonterminal)
         {
             Set<Goto> go_to = stateNo.getGotoList();
 
             Goto go = null;
             for (Goto i : go_to)
             {
                 if (symbol.equals(i.getSymbol()))
                 {
                     go = i;
                     break;
                 }
             }
             if (laIndexOf(go.getLa()) == 0)
             {
                 Deque<LaPtr> stackTop = new ArrayDeque<LaPtr>();
                 laTraverse(stateNo, go, stackTop);
             }
             if (!go.getLa().getLaSet().contains(laSymbol))
             {
                 return configs;
             }
             act = go.getAction();
         }
         else
         {
             Shift sh = null;
             for (Shift i : stateNo.getShiftList())
             {
                 if (symbol.equals(i.getSymbol()))
                 {
                     sh = i;
                     break;
                 }
             }
             act = sh.getAction();
         }
         /*******************************************************************/
         /* If the ACTion on the symbol is a shift or a goto, ...           */
         /*******************************************************************/
         if (act instanceof Lr0State)
         {
             /***************************************************************/
             /* We check to see if the new state contains an action on the  */
             /* lookahead symbol. If that's the case then we create a new   */
             /* configuration by appending ACT to the starting configuration*/
             /* and add this newly formed configuration to the set(list) of */
             /* configurations...                                           */
             /***************************************************************/
             Lr0State actst = (Lr0State) act;
             boolean trans = false;
             for (Shift sh : actst.getShiftList())
             {
                 if (laSymbol.equals(sh.getSymbol()))
                 {
                     trans = true;
                     break;
                 }
             }
             if (trans)
             {
                 configs = new StackElement(actst, stack);
             }
             /***************************************************************/
             /* If the new state cannot get into a cycle of null            */
             /* transitions, we check to see if it contains any transition  */
             /* on a nullable nonterminal. For each such transition, we     */
             /* append the new state to the stack and recursively invoke    */
             /* FOLLOW_SOURCES to check if a transition on LA_SYMBOL cannot */
             /* follow such a null transition.                              */
             /***************************************************************/
             if (!actst.isCyclic())
             {
                 for (Goto i : actst.getGotoList())
                 {
                     Nonterminal nt = i.getSymbol();
                     if (nt.isNullable())
                     {
                         StackElement q = new StackElement(actst);
                         StackElement newConfigs = followSources(q, nt, laSymbol);
                         if (newConfigs != null)
                         {
                             configs = StackElement.unionConfigSets(configs, newConfigs);
                         }
                     }
                 }
             }
         }
         /*******************************************************************/
         /* We now iterate over the kernel set of items associated with the */
         /* ACTion defined on SYMBOL...                                     */
         /*******************************************************************/
         PeekableIterator<Item> itemPtr = null;
         if (act instanceof Lr0State)
         {
             Lr0State sact = (Lr0State) act;
             itemPtr = sact.getKernelItemsPtr();
         }
         else
         {
             GRule ract = (GRule) act;
             itemPtr = ract.getAdequateItemPtr();
         }
         while (itemPtr.hasNext())
         {
             Item itemNo = itemPtr.next();
             /***************************************************************/
             /* For each item that is a final item whose left-hand side     */
             /* is neither the starting symbol nor a symbol that can        */
             /* right-most produce itself...                                */
             /***************************************************************/
             if (empty.equals(itemNo.getSymbol()))
             {
                 GRule ruleNo = itemNo.getRule();
                 Nonterminal lhsSymbol = ruleNo.getLeft();
                 if (!start.equals(lhsSymbol) && !lhsSymbol.isRmpSelf())
                 {
                     /*******************************************************/
                     /* If the length of the prefix of the item preceeding  */
                     /* the dot is shorter that the length of the stack, we */
                     /* retrace the item's path within the stack and        */
                     /* invoke FOLLOW_SOURCES with the prefix of the stack  */
                     /* where the item was introduced through closure, the  */
                     /* left-hand side of the item and the lookahead symbol.*/
                     /*******************************************************/
                     if (itemNo.getDot() < stack.getSize())
                     {
                         StackElement q = stack;
                         for (int i = 1; i < itemNo.getDot(); i++)
                         {
                             q = q.getPrevious();
                         }
                         q = followSources(q, lhsSymbol, laSymbol);
                         configs = StackElement.unionConfigSets(configs, q);
 
                     }
                     else
                     {
                         /***************************************************/
                         /* Compute the item in the root state of the stack,*/
                         /* and find the root state...                      */
                         /***************************************************/
                         itemNo = itemNo.predessor(stack.getSize());
                         StackElement q = stack;
                         for (; q.getSize() != 1;q = q.getPrevious());
                     /***************************************************/
                     /* We are now back in the main automaton, find all */
                     /* sources where the item was introduced through   */
                     /* closure start a new configuration and invoke    */
                     /* FOLLOW_SOURCES with the appropriate arguments to*/
                     /* calculate the set of configurations associated  */
                     /* with these sources.                             */
                     /***************************************************/
                         for (Lr0State p : lpgAccess(q.getState(), itemNo))
                         {
                             q = new StackElement(p);
                             StackElement newConfigs = followSources(q, lhsSymbol, laSymbol);
                             if (newConfigs != null)
                             {
                                 configs = StackElement.unionConfigSets(configs, newConfigs);
                             }
                         }
                     }
                 }
             }
         }
         return configs;
     }
 
     private void addConflictSymbol(Lr0State stateNo, GTerminal symbol)
     {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     private void computeLa(Lr0State stateNo, Item itemNo, Set<GTerminal> lookAhead)
     {
         Nonterminal lhsSymbol = itemNo.getRule().getLeft();
         if (start.equals(lhsSymbol))
         {
             lookAhead.clear();
             lookAhead.addAll(itemNo.predessor().getFirstSet());
             return;
         }
         lookAhead.clear();
 
         for (Lr0State s : lpgAccess(stateNo, itemNo))
         {
             Goto go = null;
             /*****************************************************************/
             /* Search for GOTO action in Access-State after reducing rule to */
             /* its left hand side(LHS_SYMBOL). Q points to the state.        */
             /*****************************************************************/
             Set<Goto> go_to = s.getGotoList();
             for (Goto i : go_to)
             {
                 if (lhsSymbol.equals(i.getSymbol()))
                 {
                     go = i;
                     break;
                 }
             }
             /***********************************************************/
             /* If look-ahead after left hand side is not yet computed, */
             /* LA_TRAVERSE the graph to compute it.                    */
             /***********************************************************/
             if (laIndexOf(go.getLa()) == 0)
             {
                 Deque<LaPtr> stackTop = new ArrayDeque<LaPtr>();
                 laTraverse(s, go, stackTop);
             }
             lookAhead.addAll(go.getLa().getLaSet());
         }
         lookAhead.remove(empty);
     }
 
     private void laTraverse(Lr0State stateNo, Goto go, Deque<LaPtr> stack)
     {
         LaPtr laPtr = go.getLa();
         stack.push(laPtr);
 
 
         int index = stack.size();
         setLaIndexOf(laPtr, index);
 
         /**********************************************************************/
         /* Compute STATE, action to perform on Goto symbol in question. If    */
         /* STATE is positive, it denotes a state to which to shift. If it is  */
         /* negative, it is a rule on which to perform a Goto-Reduce.          */
         /**********************************************************************/
         PeekableIterator<Item> r = null;
         Action action = go.getAction();
 
         if (action instanceof Lr0State)
         {
             Lr0State as = (Lr0State) action;
             r = as.getKernelItemsPtr();
         }
         else
         {
             GRule ra = (GRule) action;
             r = ra.getAdequateItemPtr();
         }
         while (r.hasNext())
         {
             Item ri = r.next();
             Item item = ri.predessor();
 
             if (item.getFirstSet().contains(empty))
             {
                 Nonterminal lhsSymbol = item.getRule().getLeft();
 
                 for (Lr0State t : lpgAccess(stateNo, item))
                 {
                     /**********************************************************/
                     /* Search for GOTO action in access-state after reducing  */
                     /* RULE to its left hand side (SYMBOL). Q points to the   */
                     /* GOTO_ELEMENT in question.                              */
                     /**********************************************************/
                     Goto goTo = null;
                     Set<Goto> go_to = t.getGotoList();
 
 
                     for (Goto i : go_to)
                     {
                         if (lhsSymbol.equals(i.getSymbol()))
                         {
                             goTo = i;
                             break;
                         }
                     }
                     if (laIndexOf(goTo.getLa()) == 0)
                     {
                         laTraverse(t, goTo, stack);
                     }
                     laPtr.add(goTo.getLa().getLaSet());
                     setLaIndexOf(laPtr, Math.min(laIndexOf(laPtr), laIndexOf(goTo.getLa())));
                 }
             }
         }
         if (laIndexOf(laPtr) == index)
         {
             LaPtr s = (LaPtr) stack.peek();
             while (!s.equals(laPtr))
             {
                 stack.pop();
                 s.set(laPtr.getLaSet());
                 setLaIndexOf(s, INFINITY);
                 s = (LaPtr) stack.peek();
             }
             setLaIndexOf(laPtr, INFINITY);
             stack.pop();
         }
     }
 
     private void computeRead()
     {
         /************************************************************************/
         /*  We traverse all the states and for all complete items that requires */
         /* a look-ahead set, we retrace the state digraph (with the help of the */
         /* routine TRACE_LALR_PATH) and assign a unique number to all look-ahead*/
         /* follow sets that it needs. A look-ahead follow set is a set of       */
         /* terminal symbols associated with a pair [S, A], where S is a state,  */
         /* and A is a non-terminal:                                             */
         /*                                                                      */
         /* [S, A] --> Follow-set                                                */
         /* Follow-set = {t | t is a terminal that can be shifted on after       */
         /*                      execution of a goto action on A in state S}.    */
         /*                                                                      */
         /* Each follow set is initialized with the set of terminals that can be */
         /* shifted on in state S2, where GOTO(S, A) = S2. After initialization  */
         /* a follow set F that does not contain the special terminal symbol     */
         /* EMPTY is marked with the help of the array LA_BASE, and if the       */
         /* highest level of look-ahead allowed is 1, then only one such set is  */
         /* allocated, and shared for all pairs (S, B) whose follow set is F.    */
         /************************************************************************/
         for (Lr0State state : lr0StateList)
         {
             if (!(lalrLevel <= 1 && state.isSingleCompleteItem()))
             {
                 for (PeekableIterator<Item> p = state.getCompleteItemsPtr();p.hasNext(); p.next())
                 {
                     Item item = p.peek();
                     GRule rule = item.getRule();
                     Nonterminal lhsSymbol = rule.getLeft();
 
                     if (!start.equals(lhsSymbol))
                     {
                         for (Lr0State s : lpgAccess(state, item))
                         {
                             Goto go = null;
                             Set<Goto> go_to = s.getGotoList();
                             for (Goto i : go_to)
                             {
                                 if (lhsSymbol.equals(i.getSymbol()))
                                 {
                                     go = i;
                                     break;
                                 }
                             }
                             if (go == null)
                             {
                                 throw new IllegalArgumentException("problem with grammar: "+state+" "+s+" lhsSymbol "+lhsSymbol+" not in gotolist "+go_to);
                             }
                             if (go.getLa() == null)
                             {
                                 traceLalrPath(s, go);
                             }
                         }
                     }
                 }
             }
             /***********************************************************************/
             /*  If the look-ahead level is greater than 1 or single productions    */
             /* actions are to be removed when possible, then we have to compute    */
             /* a Follow-set for all pairs [S, A] in the state automaton. Therefore,*/
             /* we also have to consider Shift-reduce actions as reductions, and    */
             /* trace back to their roots as well.                                  */
             /* Note that this is not necessary for Goto-reduce actions. Since      */
             /* they terminate with a non-terminal, and that non-terminal is        */
             /* followed by the empty string, and we know that it must produce a    */
             /* rule that either ends up in a reduction, a shift-reduce, or another */
             /* goto-reduce. It will therefore be taken care of automatically by    */
             /* transitive closure.                                                 */
             /***********************************************************************/
             if (lalrLevel > 1 || singleProductions)
             {
                 for (Shift sh : state.getShiftList())
                 {
                     Action action = sh.getAction();
                     
 
                     if (action instanceof GRule)
                     {
                         GRule rule = (GRule) action;
                         Nonterminal lhsSymbol = rule.getLeft();
                         Item adequate = rule.getAdequateItemPtr().peek();
                         Item item = adequate.predessor();
 
                         for (Lr0State s : lpgAccess(state, item))
                         {
                             Goto go = null;
                             Set<Goto> go_to = s.getGotoList();
 
                             for (Goto i : go_to)
                             {
                                 if (lhsSymbol.equals(i.getSymbol()))
                                 {
                                     go = i;
                                     break;
                                 }
                             }
                             if (go.getLa() == null)
                             {
                                 traceLalrPath(s, go);
                             }
                         }
                     }
                 }
             }
             /*******************************************************************/
             /* We also need to compute the set of terminal symbols that can be */
             /* read in a state entered via a terminal transition.              */
             /*******************************************************************/
             if (lalrLevel > 1 && !state.isFirst())
             {
                 Iterator<Item> q = state.getKernelItemsPtr();
                 Item item = q.next().predessor();
 
                 if (item.getSymbol() instanceof GTerminal)
                 {
                     state.setReadSet(item.getFirstSet());
 
                     while (q.hasNext())
                     {
                         item = q.next().predessor();
                         assert item.getSymbol() instanceof GTerminal;   // ???
                         state.addReadSet(item.getFirstSet());
                     }
                 }
             }
         }
         /*********************************************************************/
         /*   We now allocate space for LA_INDEX and LA_SET, and initialize   */
         /* all its elements as indicated in reduce.h. The array LA_BASE is   */
         /* used to keep track of Follow sets that have been initialized. If  */
         /* another set needs to be initialized with a value that has been    */
         /* already computed, LA_BASE is used to retrieve the value.          */
         /*********************************************************************/
         laBase.clear();
         laIndexMap = new HashMap<>();
         PeekableIterator<Item> q = null;
         for (Lr0State state_no : lr0StateList)
         {
             for (Goto go : state_no.getGotoList())
             {
                 LaPtr laPtr = go.getLa();
                 if (laPtr != null) /* Follow Look-ahead needed */
                 {
                     Action action = go.getAction();
 
                     if (action instanceof Lr0State)
                     {
                         Lr0State state = (Lr0State) action;
                         if (laBase.containsKey(state))  /* already computed */
                         {
                             setLaIndexOf(laPtr, laIndexOf(laBase.get(state)));
                             laPtr.set(laBase.get(state).getLaSet());
                             q = null;
                         }
                         else
                         {
                             laBase.put(state, laPtr);
                             q = state.getKernelItemsPtr();
                         }
                     }
                     else
                     {
                         GRule rule = (GRule) action;
                         q = rule.getAdequateItemPtr();
                     }
                     if (q != null)
                     {
                         Item itemNo = q.next().predessor();
                         laPtr.set(itemNo.getFirstSet());
 
                         while (q.hasNext())
                         {
                             Item item=q.next();
                             Item it = item.predessor();
                             laPtr.add(it.getFirstSet());
                         }
                         if (laPtr.getLaSet().contains(empty))
                         {
                             setLaIndexOf(laPtr, 0);
                         }
                         else
                         {
                             setLaIndexOf(laPtr, INFINITY);
                         }
                         if (lalrLevel > 1 || singleProductions)
                         {
                             if (action instanceof Lr0State)
                             {
                                 Lr0State state = (Lr0State) action;
                                 state.setReadSet(laPtr.getLaSet());
                             }
                         }
                     }
                 }
             }
         }
     }
 
     private void traceLalrPath(Lr0State state_no, Goto go)
     {
         PeekableIterator<Item> r = null;
         Action action = go.getAction();
 
         if (action instanceof Lr0State)
         {
             Lr0State s = (Lr0State) action;
             if (laBase.containsKey(s)
                     && lalrLevel == 1
                     && !singleProductions)
             {
                 go.setLa(laBase.get(s));
                 return;
             }
             r = s.getKernelItemsPtr();
         }
         else
         {
             GRule rule = (GRule) action;
             r = rule.getAdequateItemPtr();
         }
 
         LaPtr la = new LaPtr();
         go.setLa(la);
 
         boolean containsEmpty = false;
         for (;r.hasNext();r.next())
         {
             Item item = r.peek().predessor();
 
             if (item.getFirstSet().contains(empty))
             {
                 containsEmpty = true;
                 Nonterminal symbol = item.getRule().getLeft();
 
                 for (Lr0State t : lpgAccess(state_no, item))
                 {
                     Goto gg = null;
                     Set<Goto> go_to = t.getGotoList();
 
                     for (Goto i : go_to)
                     {
                         if (symbol.equals(i.getSymbol()))
                         {
                             go = i;
                             break;
                         }
                     }
                     if (go.getLa() == null)
                     {
                         traceLalrPath(t, go);
                     }
                 }
             }
         }
 /********************************************************************/
 /* If the look-ahead follow set involved does not contain EMPTY, we */
 /* mark the state involved (STATE) so that other look-ahead follow  */
 /* sets which may need this set may reuse the same one.             */
 /* NOTE that if CONTAINS_EMPTY is false, then STATE has to denote a */
 /* state number (positive value) and not a rule number (negative).  */
 /********************************************************************/
         if (!containsEmpty)
         {
             laBase.put((Lr0State) action, go.getLa());
         }
     }
 
     /*****************************************************************************/
     /* Given a STATE_NO and an ITEM_NO, ACCESS computes the set of states where  */
     /* the rule from which ITEM_NO is derived was introduced through closure.    */
     /*****************************************************************************/
     private List<Lr0State> lpgAccess(Lr0State state, Item item)
     {
         Set<Lr0State> result = new NumSet<>();
         result.add(state);
 
         for (int i = item.getDot(); i > 0; i--)
         {
             Set<Lr0State> nset = new NumSet<>();
             for (Lr0State st : result)
             {
                 Set<Lr0State> in = st.getInStates();
                 if (in != null)
                 {
                     nset.addAll(in);
                 }
             }
             result = nset;
         }
         // this is just for getting similar states as jikes do
         List<Lr0State> list = new ArrayList<>();
         list.addAll(result);
         Collections.sort(list);
         Collections.reverse(list);
         return list;
     }
 
     private void buildInStat()
     {
         for (Lr0State state : lr0StateList)
         {
             for (Shift shift : state.getShiftList())
             {
                 Action action = shift.getAction();
                 
                 if (action instanceof Lr0State)
                 {
                     Lr0State st = (Lr0State) action;
                     st.addInState(state);
                 }
             }
             for (Goto g : state.getGotoList())
             {
                 Action action = g.getAction();
                 if (action instanceof Lr0State)
                 {
                     Lr0State st = (Lr0State) action;
                     st.addInState(state);
                 }
             }
         }
     }
 
     private void initLalrkProcess()
     {
         if (lalrLevel > 1)
         {
             for (Nonterminal nt : nonterminals)
             {
                 if (nt.isRmpSelf())
                 {
                     throw new GrammarException(nt+" produces itself");
                 }
             }
 
             indexMap = new NumMap<>();
             stack = new ArrayDeque<>();
             for (Lr0State state : lr0StateList)
             {
                 if (indexOf(state) == 0)
                 {
                     computeCyclic(state);
                 }
                 if (state.isCyclic())
                 {
                     throw new GrammarException(state+" is cyclic");
                 }
             }
         }
     }
 
     private void computeCyclic(Lr0State state)
     {
         stack.push(state);
 
         int index = stack.size();
         setIndexOf(state, index);
 
         for (Goto g : state.getGotoList())
         {
             Nonterminal symbol = g.getSymbol();
             Action action = g.getAction();
 
             if (action instanceof Lr0State && symbol.isNullable())
             {
                 Lr0State st = (Lr0State) action;
                 if (indexOf(st) == 0)
                 {
                     computeCyclic(st);
                 }
                 else
                 {
                     if (indexOf(st) != INFINITY)
                     {
                         state.setCyclic(true);
                     }
                 }
                 state.setCyclic(state.isCyclic() || st.isCyclic());
                 setIndexOf(state, Math.min(indexOf(state), indexOf(st)));
             }
         }
         if (indexOf(state) == index)
         {
             Lr0State s = (Lr0State) stack.peek();
             while (!s.equals(state))
             {
                 stack.pop();
                 setIndexOf(s, INFINITY);
                 s = (Lr0State) stack.peek();
             }
             setIndexOf(state, INFINITY);
             stack.pop();
         }
     }
 
     private void computeProduces(Nonterminal symbol)
     {
         stack.push(symbol);
         int index = stack.size();
         setIndexOf(symbol, index);
         for (Nonterminal newSymbol : symbol.getDirectProduces())
         {
             if (indexOf(newSymbol) == 0)
             {
                 computeProduces(newSymbol);
             }
             setIndexOf(symbol, Math.min(indexOf(symbol), indexOf(newSymbol)));
             symbol.addProduces(newSymbol.getProduces());
         }
         if (indexOf(symbol) == index)
         {
             Nonterminal newSymbol = (Nonterminal) stack.peek();
             while (!newSymbol.equals(symbol))
             {
                 stack.pop();
                 newSymbol.setProduces(symbol.getProduces());
                 setIndexOf(symbol, INFINITY);
                 newSymbol = (Nonterminal) stack.peek();
             }
             setIndexOf(symbol, INFINITY);
             stack.pop();
         }
     }
 
     private void initRmpSelf()
     {
         for (Nonterminal nt : nonterminals)
         {
             if (nt.getProduces().contains(nt))
             {
                 nt.setRmpSelf(true);
             }
         }
     }
 
     private Set<GTerminal> sFirst(List<Symbol> list)
     {
         Set<GTerminal> set = new NumSet<>();
         Symbol symbol = null;
         Iterator<Symbol> si = list.iterator();
 
         if (si.hasNext())
         {
             symbol = si.next();
         }
         else
         {
             symbol = empty;
         }
         if (symbol instanceof GTerminal)
         {
             set.add((GTerminal) symbol);
         }
         else
         {
             Nonterminal nt = (Nonterminal) symbol;
             set.addAll(nt.getFirstSet());
         }
         while (si.hasNext() && set.contains(empty))
         {
             symbol = si.next();
             set.remove(empty);
             if (symbol instanceof GTerminal)
             {
                 set.add((GTerminal) symbol);
             }
             else
             {
                 Nonterminal nt = (Nonterminal) symbol;
                 set.addAll(nt.getFirstSet());
             }
         }
         return set;
     }
 
     private void computeNullable()
     {
         boolean changed = true;
         Map<GRule, PeekableIterator<Symbol>> rhsStart = new NumMap<>();
 
         for (GRule rule : rules)
         {
             rhsStart.put(rule, new PeekableIterator(rule.getRight().iterator()));
         }
 
         while (changed)
         {
             changed = false;
             for (Nonterminal nt : nonterminals)
             {
                 if (!nt.isNullable())
                 {
                     for (GRule rule : nt.getLhsRule())
                     {
                         if (isNullableRhs(rhsStart.get(rule)))
                         {
                             changed = true;
                             nt.setNullable(true);
                         }
                     }
                 }
             }
         }
     }
 
     private boolean isNullableRhs(PeekableIterator<Symbol> it)
     {
         for (; it.hasNext(); it.next())
         {
             Symbol symbol = it.peek();
             if (symbol instanceof GTerminal)
             {
                 return false;
             }
             else
             {
                 Nonterminal nt = (Nonterminal) symbol;
                 if (!nt.isNullable())
                 {
                     return false;
                 }
             }
         }
         return true;
     }
 
     private void computeClosure(Nonterminal lhsSymbol)
     {
         Map<Nonterminal,Nonterminal> nontList = new NumMap<>();
         stack.push(lhsSymbol);
 
         int index = stack.size();
         setIndexOf(lhsSymbol, index);
 
         for (Nonterminal i : nonterminals)
         {
             nontList.put(i, omega);
         }
 
         nontList.put(lhsSymbol, nil);
         Nonterminal ntRoot = lhsSymbol;
 
         for (GRule rule : lhsSymbol.getLhsRule())
         {
             Symbol symbol = rule.getFirstRight();
             if (symbol == null)
             {
                 symbol = empty;
             }
             if (symbol instanceof Nonterminal)
             {
                 Nonterminal ntSymbol = (Nonterminal) symbol;
                 if (omega.equals(nontList.get(ntSymbol)))
                 {
                     if (indexOf(ntSymbol) == 0)
                     {
                         computeClosure(ntSymbol);
                     }
                     setIndexOf(lhsSymbol, Math.min(indexOf(lhsSymbol), indexOf(ntSymbol)));
 
                     nontList.put(ntSymbol, ntRoot);
                     ntRoot = ntSymbol;
                     for (Nonterminal q : ntSymbol.getClosure())
                     {
                         if (omega.equals(nontList.get(q)))
                         {
                             nontList.put(q, ntRoot);
                             ntRoot = q;
                         }
                     }
                 }
             }
         }
         for (;!ntRoot.equals(lhsSymbol);ntRoot = nontList.get(ntRoot))
         {
             lhsSymbol.addClosure(ntRoot);
         }
         if (indexOf(lhsSymbol) == index)
         {
             Nonterminal symbol = (Nonterminal) stack.peek();
             while (!symbol.equals(lhsSymbol))
             {
                 stack.pop();
                 Set<Nonterminal> temp = symbol.getClosure();
                 temp.clear();
                 temp.addAll(lhsSymbol.getClosure());
                 setIndexOf(symbol, INFINITY);
                 symbol = (Nonterminal) stack.peek();
             }
             setIndexOf(lhsSymbol, INFINITY);
             stack.pop();
         }
     }
 
     private void computeFirst(Nonterminal nonterminal)
     {
         stack.push(nonterminal);
         int index = stack.size();
         setIndexOf(nonterminal, index);
 
         for (GRule rule : nonterminal.getLhsRule())
         {
             boolean blocked = false;
 
             for (Symbol symbol : rule.getRight())
             {
                 if (symbol instanceof Nonterminal)
                 {
                     Nonterminal nt = (Nonterminal) symbol;
                     if (indexOf(nt) == 0)
                     {
                         computeFirst(nt);
                     }
                     setIndexOf(nonterminal, Math.min(indexOf(nonterminal), indexOf(nt)));
 
                     Set<GTerminal> temp = new NumSet<>();
                     temp.addAll(nt.getFirstSet());
                     temp.remove(empty);
                     nonterminal.addFirst(temp);
                     blocked = !nt.isNullable();
                 }
                 else
                 {
                     nonterminal.addFirst((GTerminal)symbol);
                     blocked = true;
                 }
                 if (blocked)
                 {
                     break;
                 }
             }
             if (!blocked)
             {
                 nonterminal.addFirst(empty);
             }
         }
         if (indexOf(nonterminal) == index)
         {
             Nonterminal symbol = (Nonterminal) stack.peek();
             while (!symbol.equals(nonterminal))
             {
                 stack.pop();
                 Set<GTerminal> temp = symbol.getFirstSet();
                 temp.clear();
                 temp.addAll(nonterminal.getFirstSet());
                 setIndexOf(symbol, INFINITY);
                 symbol = (Nonterminal) stack.peek();
             }
             setIndexOf(nonterminal, INFINITY);
             stack.pop();
         }
     }
 
     private int indexOf(Object obj)
     {
         if (indexMap.containsKey(obj))
         {
             return indexMap.get(obj);
         }
         else
         {
             return 0;
         }
     }
 
     private void setIndexOf(Numerable obj, int index)
     {
         indexMap.put(obj, index);
     }
 
     private int laIndexOf(LaPtr laPtr)
     {
         if (laIndexMap.containsKey(laPtr))
         {
             return laIndexMap.get(laPtr);
         }
         else
         {
             return 0;
         }
     }
 
     private void setLaIndexOf(LaPtr laPtr, int index)
     {
         laIndexMap.put(laPtr, index);
     }
 
     private void checkNonTerminals()
     {
         Map<Nonterminal, Boolean> producesTerminals = new NumMap<>();
         for (Nonterminal nt : nonterminals)
         {
             producesTerminals.put(nt, false);
         }
         producesTerminals.put(start, true);
         boolean changed = true;
         Map<GRule, PeekableIterator<Symbol>> rhsStart = new NumMap<>();
 
 
         for (GRule rule : rules)
         {
             rhsStart.put(rule, new PeekableIterator(rule.getRight().iterator()));
         }
         while (changed)
         {
             changed = false;
             for (Nonterminal nt : nonterminals)
             {
                 if (!producesTerminals.get(nt))
                 {
                     for (GRule rule : nt.getLhsRule())
                     {
                         if (isTerminalRhs(rhsStart.get(rule), producesTerminals))
                         {
                             changed = true;
                             producesTerminals.put(nt, true);
                         }
                     }
                 }
             }
         }
         List<Nonterminal> ntList = new ArrayList<>();
 
         for (Nonterminal nt : nonterminals)
         {
             if (!producesTerminals.get(nt))
             {
                 ntList.add(nt);
             }
         }
         if (!ntList.isEmpty())
         {
             throw new GrammarException(ntList + " does not generate any terminal strings");
         }
     }
 
     private boolean isTerminalRhs(PeekableIterator<Symbol> it, Map<Nonterminal, Boolean> producesTerminals)
     {
         for (; it.hasNext(); it.next())
         {
             Symbol symbol = it.peek();
             if (symbol instanceof Nonterminal)
             {
                 Nonterminal nt = (Nonterminal) symbol;
                 if (!producesTerminals.get(nt))
                 {
                     return false;
                 }
             }
         }
         return true;
     }
 
     public void printAll(HtmlPrinter p) throws IOException
     {
         p.h1("BNF");
         p.linkDestination("BNF");
         Nonterminal nt = null;
         for (GRule rule : rules)
         {
             Nonterminal left = rule.getLeft();
             if (!left.equals(nt))
             {
                 nt = left;
                 p.linkDestination(nt.toString());
             }
             rule.print(p);
         }
         p.h1("Terminals");
         p.linkDestination("Terminals");
         for (GTerminal terminal : terminals)
         {
             if (!terminal.isAnonymous())
             {
                 p.linkDestination(terminal.getName());
                 p.p();
                 p.print(terminal.getName());
                 p.print(" = '");
                 p.print(terminal.getUnescapedExpression());
                 p.print("'");
             }
         }
         printStates(p);
         printLaStates(p);
     }
     private void printStates(HtmlPrinter p) throws IOException
     {
         p.h1("States");
         p.linkDestination("States");
         for (Lr0State s : lr0StateList)
         {
             p.linkDestination("state"+s.getNumber());
             p.h2("State " + s.getNumber());
             p.print("(");
             for (Lr0State in : s.getInStates())
             {
                 p.linkSource("#state"+in.getNumber(), " "+in.getNumber());
             }
             p.println(" )");
             PeekableIterator<Item> ni  = s.getKernelItemsPtr();
             while (ni.hasNext())
             {
                 p.p();
                 Item i = ni.next();
                 if (i.isFinal())
                 {
                     i.print(p);
                     p.linkSource("#rule"+i.getRule().getNumber(), " (" + i.getRule().getNumber() + ")");
                 }
                 else
                 {
                     i.print(p);
                 }
             }
             ni  = s.getCompleteItemsPtr();
             while (ni.hasNext())
             {
                 p.p();
                 Item i = ni.next();
                 if (i.isFinal())
                 {
                     i.print(p);
                     p.linkSource("#rule"+i.getRule().getNumber(), " (" + i.getRule().getNumber() + ")");
                 }
                 else
                 {
                     i.print(p);
                 }
             }
             for (Shift sh : s.getShiftList())
             {
                 p.p();
                 Action a = sh.getAction();
 
                 if (a instanceof Lr0State)
                 {
                     Lr0State st = (Lr0State) a;
                     printAction(p, st, sh.getSymbol(), "Shift");
                     //p.linkSource("#state"+st.getNumber(), sh.getSymbol() + "   Shift  " + st.getNumber());
                 }
                 else
                 {
                     if (a instanceof LaState)
                     {
                         LaState st = (LaState) a;
                         printAction(p, st, sh.getSymbol(), "Shift");
                         //p.linkSource("#state"+st.getNumber(), sh.getSymbol() + "   Shift  " + st.getNumber());
                     }
                     else
                     {
                         GRule r = (GRule) a;
                         printAction(p, r, sh.getSymbol(), "Sh/Rd");
                         //p.linkSource("#rule"+r.getNumber(), sh.getSymbol() + "   Sh/Rd  " + r.getNumber());
                     }
                 }
             }
             for (Goto g : s.getGotoList())
             {
                 p.p();
                 Action a = g.getAction();
                 if (a instanceof Lr0State)
                 {
                     Lr0State st = (Lr0State) a;
                     printAction(p, st, g.getSymbol(), "Goto");
                     //p.linkSource("#state"+st.getNumber(), g.getSymbol() + "   Goto  " + st.getNumber());
                 }
                 else
                 {
                     GRule r = (GRule) a;
                     printAction(p, r, g.getSymbol(), "Gt/Rd");
                     //p.linkSource("#rule"+r.getNumber(), g.getSymbol() + "   Gt/Rd  " + r.getNumber());
                 }
             }
             for (Reduce r : s.getReduceList())
             {
                 p.p();
                 int n = r.getRule().getNumber();
                 printAction(p, r.getRule(), r.getSymbol(), "Reduce");
                 //p.linkSource("#rule"+n, r.getSymbol() + "   Reduce  " + n);
             }
             if (s.getDefaultReduce() != null)
             {
                 p.p();
                 p.linkSource("#rule"+s.getDefaultReduce().getNumber(), "Default Reduce");
             }
         }
     }
     private void printLaStates(HtmlPrinter p) throws IOException
     {
         if (laStateList.isEmpty())
         {
             return;
         }
         p.h1("Lookahead States");
         p.linkDestination("LaStates");
         for (LaState s : laStateList)
         {
             p.linkDestination("state"+s.getNumber());
             p.h2("State " + s.getNumber());
             p.print("(");
             p.linkSource("#state"+s.getInState().getNumber(), " "+s.getInState().getNumber());
             p.println(" )");
             p.p();
             for (LaShift shift : s.getShiftList())
             {
                 p.p();
                 shift.getSymbol().print(p);
                 Act act = shift.getAct();
                 if (act instanceof LaState)
                 {
                     p.println("  La/Sh ");
                    p.linkSource("state"+act.getNumber(), " "+act.getNumber());
                 }
                 else
                 {
                     if (act instanceof ShiftReduceAct)
                     {
                         p.println("  Sh/Rd ");
                        p.linkSource("rule"+act.getNumber(), " "+act.getNumber());
                     }
                     else
                     {
                         if (act instanceof Lr0State)
                         {
                             p.println("  Shift ");
                            p.linkSource("state"+act.getNumber(), " "+act.getNumber());
                         }
                         else
                         {
                             throw new UnsupportedOperationException("not supported yet");
                         }
                     }
                 }
             }
             p.p();
             for (LaReduce reduce : s.getReduceList())
             {
                 p.p();
                 p.print(reduce.getSymbol());
                 Act act = reduce.getAct();
                 if (act instanceof ReduceAct)
                 {
                     p.println("  Reduce ");
                    p.linkSource("rule"+act.getNumber(), " "+act.getNumber());
                 }
                 else
                 {
                     if (act instanceof ShiftReduceAct)
                     {
                         throw new UnsupportedOperationException("not supported yet");
                     }
                     else
                     {
                         if (act instanceof Lr0State)
                         {
                             throw new UnsupportedOperationException("not supported yet");
                         }
                         else
                         {
                             throw new UnsupportedOperationException("not supported yet");
                         }
                     }
                 }
             }
         }
     }
 
     private void printAction(HtmlPrinter p, GRule r, Symbol s, String action) throws IOException
     {
         s.print(p);
         p.print(" ");
         p.print(action);
         p.print(" ");
         p.linkSource("#rule"+r.getNumber(), String.valueOf(r.getNumber()));
     }
     private void printAction(HtmlPrinter p, State st, Symbol s, String action) throws IOException
     {
         s.print(p);
         p.print(" ");
         p.print(action);
         p.print(" ");
         p.linkSource("#state"+st.getNumber(), String.valueOf(st.getNumber()));
     }
     /*
     public final void print(Appendable a) throws IOException
     {
         AppendablePrinter out = new AppendablePrinter(a);
         out.println("Rules:");
         printRules(out);
         out.println("First map for non-terminals:");
         printFirstMapForNonterminals(out);
         out.println();
         out.println("Closure for non-terminals:");
         printClosureForNonterminals(out);
         out.println();
         out.println("Nullable non-terminals:");
         printNullableNonterminals(out);
         out.println();
         printStates(out);
         out.println("LA STATES");
         out.println();
         printLaStates(out);
     }
     * 
     */
     private GRule getRule(int number)
     {
         for (GRule rule : rules)
         {
             if (number == rule.getNumber())
             {
                 return rule;
             }
         }
         return null;
     }
 
     public void printRules(Appendable out) throws IOException
     {
         printRules(new AppendablePrinter(out));
     }
     private void printRules(AppendablePrinter out) throws IOException
     {
         for (GRule rule : rules)
         {
             out.println(rule.getDescription());
         }
     }
 
     public void printFirstMapForNonterminals(Appendable out) throws IOException
     {
         printFirstMapForNonterminals(new AppendablePrinter(out));
     }
     private void printFirstMapForNonterminals(AppendablePrinter out) throws IOException
     {
         for (Nonterminal nt : nonterminals)
         {
             out.println(nt+" ==>> "+nt.getFirstSet());
         }
     }
 
     public void printClosureForNonterminals(Appendable out) throws IOException
     {
         printClosureForNonterminals(new AppendablePrinter(out));
     }
     private void printClosureForNonterminals(AppendablePrinter out) throws IOException
     {
         for (Nonterminal nt : nonterminals)
         {
             out.println(nt+" ==>> "+nt.getClosure());
         }
     }
 
     public void printNullableNonterminals(Appendable out) throws IOException
     {
         printNullableNonterminals(new AppendablePrinter(out));
     }
     private void printNullableNonterminals(AppendablePrinter out) throws IOException
     {
         for (Nonterminal nt : nonterminals)
         {
             out.println(nt+" ==>> "+nt.isNullable());
         }
     }
 
     public void printLaStates(Appendable out) throws IOException
     {
         printLaStates(new AppendablePrinter(out));
     }
 }
