 package pl.edu.pw.elka.pszt.inteligraph.model;
 
 import java.awt.Point;
 import java.io.File;
 import java.util.Collection;
 import java.util.Random;
 
 import javax.swing.SwingUtilities;
 
 import pl.edu.pw.elka.pszt.inteligraph.events.EventsBlockingQueue;
 import sun.awt.windows.ThemeReader;
 import sun.security.provider.certpath.Vertex;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.SparseGraph;
 
 public class Model
 {
 	private EventsBlockingQueue blockingQueue;
 	
 	/**
 	 * Ilość iteracji wykonana przez algorytm ewolucyjny;
 	 */
 	private int evolutionSteps = 0;
 	
 	/**
 	 * Logiczny model grafu
 	 */
 	private Graph<VertexName, String> graph;
 	
 	/**
 	 * Aktualna populacja
 	 */
 	private Population currentPopulation;
 	
 	/**
 	 * Najlepsze uzyskane rozwiązanie
 	 */
 	private SubjectCollection bestSubjectCollection;
 	
 	/**
 	 * Wątek przeprowadzający obliczenia
 	 */
 	private Thread calculationThread;
 	
 	public Model(EventsBlockingQueue blockingQueue)
 	{
 		this.blockingQueue = blockingQueue;
 	}	
 	
 	/**
 	 * @return Graf
 	 */
 	public Graph<VertexName, String> getGraph()
 	{
 		return graph;
 	}
 
 	/**
 	 * @return liczba przebytych kroków algorytmu
 	 */
 	public int getEvolutionSteps() {
 	    return evolutionSteps;
 	}
 
 
 	/**
 	 * Buduje graf na podstawie informacji z pliku XML
 	 * @param xmlFile plik xml z definicją grafu
 	 */
 	public void buildGraph(File xmlFile)
 	{
 		GraphParser read = new GraphParser();
 		InputGraph readConfig = read.readXmlGraph(xmlFile);
 		graph = readConfig.getGraph();
 	}
 
 
 	/**
 	 * @return Najlepsze uzyskane rozmieszczenie 
 	 * @throws Exception 
 	 */
 	public Arrangement getBestArrangement() throws Exception
 	{
 		if(bestSubjectCollection == null)
 			throw new Exception("No arrangement has been found");
 		
 		Arrangement arrangement = new Arrangement();
 		
 		for(Subject subject : this.bestSubjectCollection)
 		{
 			arrangement.put(subject.getVertexName(), subject.getPoint());
 		}
 		
 		return arrangement;
 	}
 
 
 	/**
 	 * Funkcja realizująca strategię ewolucyjną mi+labda.
 	 * 
 	 * @param mi
 	 * @param lambda 
 	 * @param iterations Liczba iteracji, po której algorytm ma się zakończyć
 	 */
 	public void calculateVerticesPositions(Integer mi, Integer lambda, final Integer evolutionStepsToDo)
 	{
 		SubjectCollection subjectCollection;
 		Subject subject;
 		Point point;
 		Deviation deviation;
 		Population temporaryPopulation;
 		
 		Collection<VertexName> verticies = this.graph.getVertices();
 		
 		this.currentPopulation = this.generateFirstPopulation(verticies, mi);
 		
 		this.pickBestSubjectCollection();
 		
 		this.calculationThread = new Thread(new Runnable()
 		{
 			
 			@Override
 			public void run()
 			{
 				do
 				{
 					//algorytm ewolucyjny
 				}while(evolutionStepsToDo == null || Model.this.evolutionSteps < evolutionStepsToDo);
 				
 			}
 		});
 	}
 
 
 	/**
 	 * Wybiera najlepsze rozwiązanie z aktualnej populacji
 	 */
 	private void pickBestSubjectCollection()
 	{
 		for(SubjectCollection solution : this.currentPopulation)
 		{
			if(this.bestSubjectCollection == null || this.calculateQuality(solution) > this.bestSubjectCollection.getQuality())
 			{
 				this.bestSubjectCollection = solution;
 			}
 		}
 	}
 
 
 	/**
 	 * Oblicza jakość rozwiązania pod wzglądem spełnienia kryteriów
 	 * @param solution
 	 * @return jakośc rozwiązania
 	 */
 	private Integer calculateQuality(SubjectCollection solution)
 	{
 		// TODO Auto-generated method stub
 		solution.setQuality(1);
 		return 1;
 	}
 
 
 	/**
 	 * @param mi
 	 * @return Pierwsza populacja
 	 */
 	private Population generateFirstPopulation(Collection<VertexName> verticies, Integer mi)
 	{
 		Random random = new Random();
 		Population firstPopulation = new Population();
 		SubjectCollection subjectCollection;
 		Point point;
 		Deviation deviation;
 		Subject subject;
 		
 		//Generowanie "mi" losowych rozwiązań(SubjectCollections)
 		for(int i=0; i < mi; i++)
 		{
 			subjectCollection = new SubjectCollection();
 			
 			//Dla każdego wierzchołka
 			do
 			{
 				for(VertexName vertex : verticies)
 				{
 					//Generowanie punktu
 					point = new Point(random.nextInt(800), random.nextInt(600));
 					
 					//Generowanie odchylenia
 					deviation = new Deviation(0.1);
 					
 					//Tworzenie osobnika
 					subject = new Subject(vertex, point, deviation);
 					
 					//Dodawanie osobnika do rozwiązania
 					subjectCollection.add(subject);
 				}
 			} while(this.calculateQuality(subjectCollection) < 0); //Losuje tak długo, aż rozwiązanie będzie dopuszczalne
 			
 			//Dodawanie rozwiązania do populacji
 			firstPopulation.add(subjectCollection);
 		}
 				
 		return firstPopulation;
 	}
 	
 	public void calculateVerticesPositions(Integer mi, Integer lambda)
 	{
 		this.calculateVerticesPositions(mi, lambda, null);
 	}
 	
 	/**
 	 * Przerywa obliczenia
 	 */
 	public void stopCalculations()
 	{
 		
 	}
 	
 	
 }
