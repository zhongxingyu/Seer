 package applets.Termumformungen$in$der$Technik_02_Kondensatoren;
 
 import java.util.Random;
 import javax.swing.JScrollPane;
 
 public class Content {
 
 	Applet applet;
 	PGraph graph;
 	ElectronicCircuit circuit;
 	VTEquationInput equationInput;
 	VTLabel questionLabel;
 	
 	public Content(Applet applet) {
 		this.applet = applet;		
 	}
 	
 	public void init() {
 		applet.setSize(600, 500);
 		applet.scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 	}
 
 	void postinit() {}
 	void next(int i) {}	
 	boolean isCorrect(int i, String sel) { return false; }
 	
 	void initNewCircuit() {
 		graph.setSize(520, 300);
 		graph.clear();
 		
		circuit.constructOnPGraph_Start(graph, 100, 100);
		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.ECapacitor.class, 200, 100);
		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.ECapacitor.class, 300, 100);
		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.ECapacitor.class, 400, 100);
		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.Conn.class, 400, 300);
		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.VoltageSource.class, 100, 300);
		circuit.constructOnPGraph_Next(graph, ElectronicCircuit.Conn.class, 100, 100);
 		circuit.constructOnPGraph_Final(graph);
 		
		ElectronicCircuit.EquationQuestion question = circuit.randomEquationQuestion();
		equationInput.setEquationSystem(circuit.getEquationSystem(), question.wantedExpr, question.allowedVars);
 		equationInput.clear();
		questionLabel.setText(equationInput.wantedResult.toString());
 	}
 	
 	public void run() {
 		//Utils.debugUtilsParsingOpTree();
 		//EquationSystem.debug();
 		
 		graph = new PGraph(applet, 400, 400);
 		circuit = new ElectronicCircuit();
 		equationInput = new VTEquationInput("equ", 10, 10, applet.getWidth() - 60);
 		questionLabel = new VTLabel("question", "", 10, 10, applet.getWidth() - 60);
 		initNewCircuit();
 				
 		applet.vtmeta.setExtern(new VisualThing[] {
 				new VTButton("new", "neue Aufgabe", 10, 10, new Runnable() {
 					public void run() {
 						initNewCircuit();
 						applet.revalidateVisualThings();
 						applet.repaint();						
 					}
 				}),
 				new VTImage("graph", 10, 20, applet.getWidth() - 60, 400, graph),
 				questionLabel,
 				equationInput,
 		});
 	}
 	
 }
