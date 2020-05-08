 package ontopt.pen;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * <p>
  * Class from PEN Parser.
  * </p>
  * <p>
  * This is the main class. It implements the Earley's chart parsing algorithm.
  * </p>
  * <p>
  * Copyright: Copyright (c) 2002-2009
  * </p>
  * <p>
  * Company: CISUC
  * </p>
  * 
  * @author Nuno Seco, Hugo Gon�alo Oliveira
  * @version 1.0
  */
 
 public class EarleyParser
 {
 	private boolean stop;
 	
 	/**
 	 * A dummy rule. This is the first rule to be put in the chart. it initializes the parsing process
 	 */
 	private NonterminalRule dummieRule;
 
 	/**
 	 * The grammar that has the syntactic rules.
 	 */
 	private Grammar grammar;
 
 	/**
 	 * An array of charts columns, being the Earley parser chart, composed of n+1
 	 * columns, where n = number of words in sentence.
 	 */
 	private ChartColumn[] chart;
 
 	/**
 	 * Time spent in parsing
 	 */
 	private long parseTime;
 	
 	private TransitiveMatrix rMatrix;
 	
 	/**
 	 * The constructor
 	 * 
 	 * @throws GrammarException
 	 */
 	public EarleyParser(String grammarFile) throws GrammarException
 	{
 		grammar = new Grammar(grammarFile);
 		new GrammarValidator(grammar).validate();
 
 //		System.out.println("Here it would have been printed the matrix..");
 		this.rMatrix = TransitiveMatrix.getMatrix(grammar);
 //		System.out.println(rMatrix.getTransitiveLCRelation("N", "NP"));
 		dummieRule = new NonterminalRule(0., "", null, Grammar.PARSE_ROOT, grammar);
 		
 		stop = false;
 	}
 
 	/**
 	 * Receives a sentence to be parsed
 	 * 
 	 * @param sentence
 	 *            The sentence to parse
 	 * @return A list of possible parse trees.
 	 */
 	public synchronized ArrayList<SemanticNode> parseSentence(Sentence sentence)
 	{
 		stop = false;
 		
 		//long begin = System.currentTimeMillis();
 		chart = new ChartColumn[sentence.getSentenceSize() + 1];
 		ArrayList<State> stateList = new ArrayList<State>();
 	
 		for (int i = 0; i < chart.length; i++)
 		{			
 			chart[i] = new ChartColumn(stateList);
 		}
 
 		State row = new State(dummieRule);
 		row.setForwardProbability(1.);
 		row.setInnerProbability(1.);
 		chart[0].addState(row);
 		
 		//System.err.println("sentence size = "+sentence.getSentenceSize());
 		for (int i = 0; i < sentence.getSentenceSize() + 1; i++)
 		{
 			if(stop)
 				return null;
 			
 			System.out.println("\n\n%%State "+i);
 			
 			//System.err.println("chart array ["+i+"] size= "+chart[i].size());
 			for (int j = 0; j < chart[i].size(); j++)
 			{
 				if(stop)
 					return null;
 				
 				row = chart[i].getState(j);
 				if (!row.isComplete() && row.getNextConstituent().compareTo(Grammar.PHRASE_LOWER_LIMIT) >= 0)
 				{
 //					System.err.println(1);
 					predictor(row);
 				}
 				else if (!row.isComplete() && row.getNextConstituent().compareTo(Grammar.PHRASE_LOWER_LIMIT) < 0)
 				{
 //					System.err.println(2);
 					scanner(row, sentence);
 				}
 				else
 				{
 //					System.err.println(3);
 					completer(row);
 				}
 				
 			}
 			if(i<sentence.getSentenceSize()){
 				
 			System.out.println("Prefix Probabilitie:");
 			System.out.println(Double.toString(sentence.getPrefix(i)));
 		
 			}
 		}
 		printChart();
 		ArrayList<SemanticNode> trees = getTrees();
 		//parseTime = System.currentTimeMillis() - begin;
 		chart = null;
 		return trees;
 	}
 
 	public void stopParsing()
 	{
 		stop = true;
 	}
 	
 	public boolean isStopped()
 	{
 		return stop;
 	}
 	
 	public Grammar getGrammar()
 	{
 		return grammar;
 	}
 
 	/**
 	 * Get time spent in last parse
 	 * 
 	 * @return Time spent parsing
 	 */
 	public long getLastParseTime()
 	{
 		return parseTime;
 	}
 
 	/**
 	 * Gets the parse trees associated to the current charts
 	 * 
 	 * @return list of parse trees
 	 */
 	public ArrayList<SemanticNode> getTrees()
 	{
 		ArrayList<State> ruleRoots = chart[chart.length - 1].getRoots();
 		ArrayList<SemanticNode> sentenceRoots = new ArrayList<SemanticNode>();
 
 		for (int i = 0; i < ruleRoots.size(); i++)
 		{
 			sentenceRoots.add(getTree((State) ruleRoots.get(i)));
 		}
 
 		Collections.sort(sentenceRoots);
 		return sentenceRoots;
 	}
 
 	/**
 	 * Recursive method to get parse trees. Creates TreeNodes from the chartrow and then recurses on the
 	 * chartrow parents.
 	 * 
 	 * @param node
 	 *            The chartrow
 	 * @return The parse tree
 	 */
 	private SemanticNode getTree(State node)
 	{
 		ArrayList<Integer> parents = node.getParents();
 		SemanticNode root;
 
 		Rule rule = node.getRule();
 
 		if (rule instanceof NonterminalRule)
 		{
 			root = new SemanticNode(grammar.getDataType(rule.getHead()), rule.getWeight(), rule.getAnnotation());
 		}
 		else
 		{
 			root = new SemanticNode(((TerminalRule) rule).getWord(), rule.getWeight(), rule.getAnnotation());
 		}
 
 		for (int i = parents.size() - 1; i >= 0; i--)
 		{
 			root.addChild(getTree(node.getStateFromState((Integer) parents.get(i))));
 		}
 
 		return root;
 	}
 
 	/**
 	 * The predictor Process. Creates new states representing top-down expectations generated during the
 	 * parsing process. The Predictor is applied to any state that has a nonterminal to the right of the dot
 	 * that is not a part of speech category. This application results in the creation of one new state for
 	 * each alternative expansion of that nonterminal provided by the grammar. These new states are placed
 	 * into the same chart entry as the generating state. They begin and end at the point in the input where
 	 * the generating state ends.
 	 * 
 	 * @param row
 	 *            The row to be predicted
 	 */
 	private void predictor(State stateIn)
 	{
 		Integer next = stateIn.getNextConstituent();
 		ArrayList<Rule> list = grammar.getAllRulesWithHead(next);
 
 		// System.out.println("LIST: "+list);
 		// System.out.println("ROW: "+row);
 
 		State newState;
 		int[] positions = new int[2];
 		positions[0] = stateIn.getPositions()[1];
 		positions[1] = positions[0];
 
 		for (int i = 0; i < list.size(); i++)
 		{
 			Rule curRule = list.get(i);
 			
 			newState = new State(curRule, positions);
 			newState.setProcess("Predictor");
 
 			String curNonterminal = this.grammar.getDataType(next);
 			Double rValue = this.rMatrix.getTransitiveLCRelation(curNonterminal,newState.getRule().getLHS());
 			rValue = (rValue != 0) ? rValue : 1;
 			newState.setForwardProbability(stateIn.getForwardProbability()*rValue*curRule.getProbability());
 			newState.setInnerProbability(curRule.getProbability());
 			System.out.println("	%prediction: " + newState);
 			
 			enqueue(newState, positions[0], true, false);
 		}
 	}
 
 	/**
 	 * When a state has a part of speech category to the right of the dot, the scanner is called to examine
 	 * the input and incorporate a state corresponding to the predicted part of speech into the chart. This is
 	 * accomplished by creating a new state from the input state with the dot advanced over the predicted
 	 * input category.
 	 * 
 	 * @param row
 	 *            the row to be scanned
 	 * @param sentence
 	 *            The sentence being parsed
 	 */
 	private void scanner(State stateIn, Sentence sentence)
 	{
 		State newState;
 		int positions[] = new int[2];
 
 		// Special case in which the dot is after the end of the sentence, and an empty terminal is read
 		if (stateIn.getPositions()[1] >= sentence.getSentenceSize())
 		{                    
 			if (stateIn.getNextConstituent() != null && stateIn.getNextConstituent().equals(Grammar.EMPTY_TERMINAL))
 			{
 				positions[0] = stateIn.getPositions()[1];
 				positions[1] = stateIn.getPositions()[1];
 				newState = new State(new TerminalRule(stateIn.getNextConstituent(), "", grammar), positions);
 				newState.setProcess("Scanner");
 
 //				newState.setForwardProbability(stateIn.getInnerProbability());				
 //				newState.setInnerProbability(stateIn.getInnerProbability());
 				
 				System.out.println("	%scan: empty terminal after string scanned");
 				
 				// TODO: this might not be needed, check...
 				enqueue(newState, positions[1],false,false);                                
 			}
 			return;
 		}
 
 		Integer next = stateIn.getNextConstituent();
 		String word = sentence.getWord(stateIn.getPositions()[1]);
 
 		if (grammar.getTerminal(word).equals(next) || next.equals(Grammar.UNKNOWN_TERMINAL))
 		{
 			positions[0] = stateIn.getPositions()[1];
 			positions[1] = stateIn.getPositions()[1] + 1;
 			newState = new State(new TerminalRule(next, word, grammar), positions);
 			newState.setProcess("Scanner");
 			newState.setForwardProbability(stateIn.getForwardProbability());				
 			newState.setInnerProbability(stateIn.getInnerProbability());
 			System.out.println(Integer.toString(stateIn.getPositions()[0]));
 			
 			
			// FIXME:this might not work if the Grammar does contains terminal production which are not unit productions
 			sentence.updatePrefix(stateIn.getForwardProbability(), stateIn.getPositions()[1]);
 			System.out.println("	%scan: "+newState);
 			// FIXME: this might not be needed
 			enqueue(newState, positions[1]);
 		}
 
 		if (next.equals(Grammar.EMPTY_TERMINAL))
 		{
 			positions[0] = stateIn.getPositions()[1];
 			positions[1] = stateIn.getPositions()[1];
 			newState = new State(new TerminalRule(next, "", grammar), positions);
 			newState.setProcess("Scanner");
 			// FIXME: do we have to update probabilities here?
 			// FIXME: the enqueue operation might not be needed (no need of checking for duplicates)
 			System.out.println("	%scan: empty terminal scanned"+newState);
 			enqueue(newState, positions[1]);
 		}
 	}
 
 	/**
 	 * The completer is applied to a state when its dot has reached the right end of the rule. Intuitively,
 	 * the presence of such a state represents the fact that the parser has successfully discovered a
 	 * particular grammatical category over some span of the input. The purpose of the completer is to find
 	 * and advance all previously created states that were looking for this grammatical category at this
 	 * position in he input. New states are then created by copying the older state, advancing the dot over
 	 * the expected category and installing the new state in the current chart entry.
 	 * 
 	 * @param row
 	 *            The row of the chart
 	 */
 	private void completer(State row)
 	{
 		int chartIndex = row.getPositions()[0];
 		State newRow;
 		int positions[];
 		for (int i = 0; i < chart[chartIndex].size(); i++)
 		{
 
 			if (chart[chartIndex].getState(i).getPositions()[1] == chartIndex && !chart[chartIndex].getState(i).isComplete()
 					&& chart[chartIndex].getState(i).getNextConstituent().equals(row.getRule().getHead()))
 			{
 			
 				positions = new int[2];
 
 				positions[0] = chart[chartIndex].getState(i).getPositions()[0];
 				positions[1] = row.getPositions()[1];
 				newRow = new State(chart[chartIndex].getState(i).getRule(), positions);
 				newRow.addParentState(row.getState());
 				newRow.addParentStates(chart[chartIndex].getState(i).getParents());
 				newRow.setProcess("Completer");
 				newRow.setDot(chart[chartIndex].getState(i).getDot() + 1);
 
 				State iState=row;
 				State jState=chart[chartIndex].getState(i);
 				Double rValue=1.;
 				Rule curRule = chart[chartIndex].getState(i).getRule();
 				if (curRule.size()>1){
 					rValue = this.rMatrix.getTransitiveUnitRelation(curRule.getLHS(), curRule.getLeftmost());
 					rValue = (rValue != 0) ? rValue : 1;
 				}
 				newRow.setForwardProbability(jState.getForwardProbability()*iState.getInnerProbability()*rValue);
 				newRow.setInnerProbability(jState.getInnerProbability()*iState.getInnerProbability()*rValue);
 				System.out.println("	%completion: " + newRow);
 				
 				enqueue(newRow, row.getPositions()[1], false, false);
 			}
 		}
 	}
 
 	/**
 	 * Adds the chartrow to the chart if it does not already exist.
 	 * 
 	 * @param row
 	 *            the row to add
 	 * @param index
 	 *            the index of the chart
 	 */
 	private void enqueue(State stateIn, int index, Boolean sumForwardProbabilities, Boolean sumInnerProbabilities)
 	{
 		if (stateIn.getRule().getHead() == null)
 		{
 			return;
 		}
 
 		if (!chart[index].exists(stateIn))
 		{
 			System.out.println("		%enqueue: entry not found: "+stateIn);
 			chart[index].addState(stateIn);
 		} else {
 			State stateExisting = chart[index].getState(stateIn);
 			System.out.println("		%enqueue: entry found: "+stateExisting);
 			if (sumForwardProbabilities == true) {
 				stateExisting.setForwardProbability(stateExisting.getForwardProbability() + stateIn.getForwardProbability());
 				System.out.println("		%enqueue: adding forward: "+stateExisting.getForwardProbability());
 			}
 			
 			if (sumInnerProbabilities){
 					stateExisting.setInnerProbability(stateExisting.getInnerProbability() + stateIn.getInnerProbability());
 					System.out.println("		%enqueue: adding inner: "+stateExisting.getForwardProbability());
 			}
 		}
 	}
 
 	private void enqueue(State stateIn, int index)
 	{
 		enqueue(stateIn, index, false,false);
 	}
 	
 	/**
 	 * Prints the chart
 	 */
 	@SuppressWarnings("unused")
 	private void printChart()
 	{
 		for (int i = 0; i < chart.length; i++)
 		{
 			System.out.println("ChartColumn " + i);
 			System.out.println(chart[i].toString());
 		}
 	}
 
 	public static void main(String[] args)
 	{
 		if (args.length != 2)
 		{
 			System.out.println("Usage:\n");
 			System.out.println("java -jar pen.jar <grammar> <sentences_file>");
 			return;
 		}
 
 		try
 		{
 			
 			EarleyParser parser = new EarleyParser(args[0]);
 			SemanticNode node;
 			Outputter outputter = new Outputter(System.out);
 			List<SemanticNode> parses;
 			String buffer;
 
 			BufferedReader reader = new BufferedReader(new FileReader(args[1]));
 
 			// For each input sentence
 			while ((buffer = reader.readLine()) != null)
 			{
 //				if(!buffer.startsWith("#"))
 //				{
 					System.out.println("\n***** Derivations for: \n" + buffer);
 					System.out.println("");
 					parses = parser.parseSentence(new PenSentence(buffer));
 					
 					// For each possible parse
 					for (int i = 0; i < parses.size(); i++)
 					{
 						node = parses.get(i);
 						//outputter.print(node, false, true, 0);
 						outputter.printPenn(node);
 					}
 //				}
 
 			}
 //			
 //			System.out.println(parser.rMatrix.getTransitiveLCRelation("S", "S"));
 //			System.out.println(parser.rMatrix.getTransitiveLCRelation("TOP", "S"));
 //			System.out.println(parser.rMatrix.getTransitiveLCRelation("S", "TOP"));
 //			System.out.println(parser.rMatrix.getTransitiveLCRelation("S", "a"));
 			parser.rMatrix.printRMatrix();
 			
 //			System.out.println("Dumping rules with head NP...");
 //			Grammar g = parser.getGrammar();
 //			for (Rule r : g.getAllRulesWithHead("NP")) {
 //				System.out.println(r.getLeftmost());
 //			}
 			
 //			System.out.println("Dumping nonterminals...");
 //			Grammar g = parser.getGrammar();
 //			for (String s : g.getNonterminals()) {
 //				System.out.println(s);
 //			}
 		}
 		catch (FileNotFoundException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (GrammarException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 }
