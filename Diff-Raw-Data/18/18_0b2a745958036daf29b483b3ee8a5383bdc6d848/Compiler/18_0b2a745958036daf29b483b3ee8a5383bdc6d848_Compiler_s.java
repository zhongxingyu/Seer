 package compiler;
 
 import java.io.Reader;
 import java.util.ArrayList;
 
 import compiler.abstracter.Abstracter;
 import compiler.parser.Parser;
 import compiler.parser.Instruction;
 import compiler.reducer.SKMachine;
 import compiler.combinators.CombinatorManager;
 import compiler.graph.GraphFactory;
 import compiler.graph.GraphSerializer;
 import compiler.graph.Node;
 
 /**
  * Classe haut niveau effectuant la compilation du code et renvoyant le résultat
  * @author remot
  *
  */
 public class Compiler {
 	private boolean finished = false;
 	private boolean lineFinished = false;
 	ArrayList<Instruction> symbols;
 	private SKMachine sk;
 	private int currentInstructionIndex = 0;
 	private Instruction currentInstruction;
 	private CompilerCallback callback;
 	private Thread compilationThread;
 	
 	public Compiler(Reader input, CompilerCallback callback) {
 		CombinatorManager.reset();
 		
 		this.callback = callback;
 		
 		try {
 			symbols = Parser.parse(input);
 			registerNextInstruction();
 		}
 		catch(CompilerException e) {
 			sendFailure(e, false);
 		}
 		
 		
 	}
 	
 	/**
 	 * @return le graphe réduit sous forme de chaîne de caractères
 	 */
 	public String getResult() {
 		if(sk == null) {
 			return "";
 		}
 
 		String result = GraphSerializer.serialize(sk.getReducedGraph());
 
        if(isFinished() && result.startsWith("I ")) {
             result = result.substring(2);
         }
 
         return result;
 	}
 	
 	/*
 	 * Envoie le résultat au callback
 	 */
 	private void sendResult() {
 		if(currentInstruction == null) {
 			currentInstruction = new Instruction();
 		}
 		
 		callback.onResult(getResult(), currentInstruction.getLastLine(),
 				currentInstruction.getPosition(), isFinished());
 	}
 
     private void  sendFailure(CompilerException e, boolean populateException) {
         if(populateException && currentInstruction != null) {
             e.setLine(currentInstruction.getLastLine());
             e.setPosition(currentInstruction.getPosition());
         }
 
         callback.onFailure(e);
     }
 	
 	public boolean isFinished() {
 		return finished;
 	}
 
     public boolean isInterrupted() {
         return(sk == null || (compilationThread != null && compilationThread.isInterrupted()));
     }
 	
 	// Compile l'instruction en graphe
 	private boolean registerNextInstruction() {
 		if(currentInstructionIndex >= symbols.size()) {
             currentInstruction = new Instruction();
 			finished = true;
 			return false;
 		}
 		
 		Node graph;
 		
 		currentInstruction = symbols.get(currentInstructionIndex);
 
 		try {
             graph = GraphFactory.create(currentInstruction.getInstruction());
 
             Abstracter ab = new Abstracter(graph.getRoot());
             graph = ab.getAbstractedGraph();
 		}
 		catch(CompilerException e) {
 			sendFailure(e, true);
 			return false;
 		}
 		
 		if(sk == null) {
 			sk = new SKMachine(graph);
 		}
 		else {
 			sk.setGraph(graph);
 		}
 		
 		lineFinished = false;
 		
 		currentInstructionIndex++;
 		return true;
 	}
 
 	
 	// réduit une étape
 	private void step() throws CompilerException {
 		if(!finished && (!lineFinished || registerNextInstruction()) && sk != null) {
             lineFinished = !sk.step();
 		}
 	}
 	
 	// Réduit l'instruction suivante
 	private void instruction() throws CompilerException {
 		do {
 			step();
 		} while(!isFinished() && !lineFinished && !isInterrupted() && sk != null);
 	}
 	
 	// réduit TOUT
 	private void all() throws CompilerException {
 		while(!isFinished() && !isInterrupted() && sk != null) {
 			instruction();
 			sendResult();
 		}
 	}
 	
 	/**
 	 * effectue une étape de la réduction
 	 * @return false si aucune étape n'a pu être effectué et que la réduction est donc finie, true sinon
 	 */
 	public boolean reduceStep() {
 		if(!finished) {
 			try {
 				step();
 
                 if(lineFinished) {
                     registerNextInstruction();
                 }

				sendResult();
 			}
 			catch(CompilerException e) {
 				sendFailure(e, true);
 			}
 		}
 		
 		return !finished;
 	}
 	
 	/**
 	 * effectue la réduction d'une ligne
 	 * 
 	 * @return le thread qui exécute la réduction
 	 */
 	public Thread reduceInstruction() {
 		final Compiler t = this;
 		
 		Runnable r = new Runnable() {
 			public void run() {
 				synchronized(t) {
 					try {
 						t.instruction();
                         t.sendResult();
 					}
 					catch(CompilerException e) {
 						sendFailure(e, true);
 					}
 				}
 			}
 		};
 		
 		compilationThread = new Thread(r);
 		compilationThread.start();
 		
 		return compilationThread;
 	}
 	
 	/**
 	 * effectue la réduction totale
 	 * 
 	 * @return le thread qui exécute la réduction
 	 */
 	public Thread reduceAll() {
 		finished = false;
 		final Compiler t = this;
 		
 		Runnable r = new Runnable() {
 			public void run() {
 				synchronized(t) {
 					try {
                         t.all();
 					}
 					catch(CompilerException e) {
 						sendFailure(e, true);
 					}
 				}
 			}
 		};
 		
 		compilationThread = new Thread(r);
 		compilationThread.start();
 		
 		return compilationThread;
 	}
 	
 	/**
 	 * stoppe la réduction
 	 */
 	public void stopReduction() {if(compilationThread != null) {
 			compilationThread.interrupt();
 		}
 	}
 
 }
