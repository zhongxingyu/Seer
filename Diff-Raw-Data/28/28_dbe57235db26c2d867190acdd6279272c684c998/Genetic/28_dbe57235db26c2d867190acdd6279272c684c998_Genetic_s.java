 package edu.pw.gis.algorithm;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Random;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import edu.pw.gis.graph.Edge;
 import edu.pw.gis.graph.Graph;
 import edu.pw.gis.parser.SNDParser;
 
 public class Genetic {
 
 	public final int MAX_WEIGHT; // maksymalna waga kraw_dzi
 	public final int INITIAL_POPULATION_NO; // liczba populacji (ilosc grafow)
 	public final double ALFA_RATE; // wsp_czynnik wyboru klasy A
 	public final double BETA_RATE; // wsp_czynnik wyboru klasy B
 	public final double MUTATION_RATE; // wspolczynnik mutacji
 	public final double CROSS_RATE; // wspolczynnik krzyzowania
 	public final double MAX_USAGE; // Pozadane maksymalne procentowe obciazenie
 									// kraw_dzi
 	public final double MAX_ITERATION_NO; // Maksymalna ilosc iteracji (ilosc
 											// pokolen)
 	public ArrayList<Graph> graph_list; // badany graf
 	public ArrayList<Graph> new_graph_list; // badany graf
 
 	private final int cardA; // liczba osobnikow w klasie A
 	private final int cardB; // liczba osobnikow w klasie B
 	private long startTime;
 
 	public boolean debug = false;
 
 	public Genetic(int max_weight, int initial_population, double alfa_rate,
 			double beta_rate, double mutation_rate, double cross_rate,
 			double max_usage, double max_iteration_no) {
 		this.MAX_WEIGHT = max_weight;
 		this.INITIAL_POPULATION_NO = initial_population;
 		this.ALFA_RATE = alfa_rate;
 		this.BETA_RATE = beta_rate;
 		this.MUTATION_RATE = mutation_rate;
 		this.CROSS_RATE = cross_rate;
 		this.MAX_USAGE = max_usage;
 		this.MAX_ITERATION_NO = max_iteration_no;
 		this.graph_list = new ArrayList<Graph>();
 		this.new_graph_list = new ArrayList<Graph>();
 
 		this.cardA = (int) Math.floor(ALFA_RATE * INITIAL_POPULATION_NO);
 		this.cardB = (int) Math.ceil(BETA_RATE * INITIAL_POPULATION_NO);
 
 	}
 
 	/**
	 * 
 	 */
 	public void createInitialPopulation(Graph g) {
 
 		for (int i = 0; i < this.INITIAL_POPULATION_NO; i++) {
 			generateCloneWithRandomWeights(g);
 		}
 
 		graph_list = new_graph_list;
 		new_graph_list = new ArrayList<Graph>();
 	}
 
 	public void generateCloneWithRandomWeights(Graph g) {
 		Graph tmp;
 
 		tmp = g.clone();
 
 		tmp.randomWeights(this.MAX_WEIGHT, g.max_capacity);
 		this.new_graph_list.add(tmp);
 	}
 
 	public void evaluatePopulation() {
 		ExecutorService pool = Executors.newFixedThreadPool(5);
 
 		for (Graph g : graph_list) {
 			pool.submit(new FlowCalculator(g));
 			// FlowCalculator f = new FlowCalculator(g);
 			// f.run();
 		}
 
 		 pool.shutdown();
 		
 		 while (!pool.isTerminated()) {
 		 // czekamy na wykonanie wszystkich watkow
 		 }
 
 		// sortowanie celem pozniejszego podzialu na klasy
 		Collections.sort(graph_list, new Comparator<Graph>() {
 			public int compare(Graph g1, Graph g2) {
 				if (g1.highestUsage < g2.highestUsage) {
 					return -1;
 				} else if (g1.highestUsage > g2.highestUsage) {
 					return 1;
 				} else
 					return 0;
 			}
 		});
 
 	}
 
 	public void evolutionStep() {
 		for (int i = 0; i < graph_list.size(); i++) {
 
 			if (i < cardA) { // klasa A, przezywaja wszyscy
 				new_graph_list.add(graph_list.get(i));
 			}
 
 			else if (i >= cardA && i < cardB) { // klasa B
				getChild(graph_list.get(0));
 			}
 
 			else if (i >= cardB) { // klasa C, umieraja, a na ich miejsce
 									// losowane sa
 									// nowe
 				generateCloneWithRandomWeights(graph_list.get(0)); // wszystko
 																	// jedno, z
 																	// jakiego
 																	// sklonujemy
 			}
 		}
 
 		graph_list = new_graph_list;
 		new_graph_list = new ArrayList<Graph>();
 
 	}
 
 	public void printUsages() {
 		for (Graph g : graph_list)
 			System.out.println("Uzycie: " + g.highestUsage);
 	}
 	
 	public void printHighestUsage() {
 		System.out.println("Najwieksze uzycie: " + graph_list.get(0).highestUsage);		
 	}
 
 	public void getChild(Graph g) {
 		Graph child;
 		Random rand = new Random();
 		int rr = rand.nextInt(cardA + 1);
 		Graph dad = graph_list.get(rr);
 		rr = rand.nextInt(cardB - cardA + 1) + cardA;
 		Graph mom = graph_list.get(rr);
 
 		child = (Graph) g.clone();
 		for (int j = 0; j < child.edgeList.size(); j++) {
 			Edge e = child.edgeList.get(j);
 			if (rand.nextFloat() < MUTATION_RATE) // mutacja
				//e.weight = (int) (rand.nextInt(this.MAX_WEIGHT) + 1);
				e.weight = 5;
 			else if (rand.nextFloat() < CROSS_RATE)
 				e.weight = dad.edgeList.get(j).weight;
 			else
 				e.weight = mom.edgeList.get(j).weight;
 		}
 
 		new_graph_list.add(child);
 	}
 
 	public void go(long start) {
 		startTime = start;
 		for (int i = 0; i < MAX_ITERATION_NO; i++) {
 
 			evaluatePopulation();
 
 			if (i % 50 == 0 && this.debug) {
 				System.out
 						.println("new population "
 								+ i
 								+ "===================================================");
 				//printUsages();
 				printHighestUsage();
 				System.out
 						.println("end population "
 								+ i
 								+ "===================================================");
 			}
 
 			if (graph_list.get(0).highestUsage <= MAX_USAGE) {
 				System.out.println("Desired usage reached:"
 						+ graph_list.get(0).highestUsage);
 				break;
 			}
 
 			// ograniczenie na czas wykonania (10 sekund)
 			if (System.currentTimeMillis() - startTime > 10000) {
 				System.out.println("Ups. 10 sec passed. Time exceeded.");
 				break;
 			}
 
 			evolutionStep();
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		SNDParser snd_parser = new SNDParser("xml/simple_test_graph.xml");
 
 		Genetic genetic = new Genetic(10, 3, 0.2, 0.7, 0.01, 0.5, 0.5, 100);
 
 		genetic.createInitialPopulation(snd_parser.graph);
 
 		// genetic.graph_list.get(0).addNode(new Node());
 		// genetic.graph_list.get(1).nodeList.get(0).name="DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDddd";
 		//
 		// for(int i=0; i<genetic.INITIAL_POPULATION_NO; i++)
 		// {
 		// System.out.println("Graf populacji " + i);
 		// System.out.println();
 		// System.out.println(genetic.graph_list.get(i).nodeList.get(0).name);
 		// genetic.graph_list.get(i).printAdjList();
 		// }
 	}
 }
