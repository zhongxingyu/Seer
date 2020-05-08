 package tetramethylbutaan;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 public class Main
 {
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		new Main();
 	}
 	
 	public Main()
 	{
 		for (int K = 1; K <= 5; K +=2)
 		{
 			Graph.K = K;
 			for (String dataType : new String[]{"xor", "diabetes", "mnist"})
 			{
 				setDimensions(dataType);
 				for (int order = Graph.EDIT_1ST_ORDER; order <= Graph.EDIT_2ND_ORDER; order++)
 				{
 					System.out.println("\n" + dataType + ", order " + order + ", K = " + K + ":");
 					trainingSetTest(dataType + "_train.txt", new RelatedNeighbourHoodGraph(), order, 0.8);
 				}
 			}
 		}
 		//customTest();
 		//autoTest();
 	}
 	
 	private void setDimensions(String dataType)
 	{
		if (dataType.equals("xor"))
		{
 			Point.nrDimensions = 2;
			Point.nrClasses = 2;
		}
 		else if (dataType.equals("diabetes"))
		{
 			Point.nrDimensions = 8;
			Point.nrClasses = 2;
		}
 		else if (dataType.equals("mnist"))
		{
 			Point.nrDimensions = 196;
			Point.nrClasses = 10;
		}
 	}
 	
 	/**
 	 * tests on a given training set and prints the results
 	 * @param filename the training set
 	 * @param graph the graph to test on, empty graph expected
 	 * @param fraction how much data should be used to construct the training set, between 0 and 1.
 	 * the rest of the data will be used to test.
 	 */
 	private void trainingSetTest(String filename, Graph graph, int order, double fraction)
 	{
 		TrainingSetReader reader = new TrainingSetReader(filename, graph, fraction, false);
 		List<GraphPoint> testSet = reader.getTestSet();
 		
 		double sizeBefore = (double) graph.getPoints().size();
 		constructGraph(graph, order, false);
 		double reduction = (double) graph.getPoints().size() / sizeBefore;
 		
 		int nrTests = testSet.size();
 		int nrCorrect = 0;
 		for (GraphPoint point : testSet)
 		{
 			int classification = point.getClassification();
 			if (classification == graph.test(point))
 				nrCorrect++;
 		}
 		System.out.println((100.0 * (double)nrCorrect/(double)nrTests) + "% correct");
 		System.out.println(100.0 * reduction + "% reduction");
 	}
 	
 	private void constructGraph(Graph graph, int order)
 	{
 		constructGraph(graph, order, true);
 	}
 	
 	private void constructGraph(Graph graph, int order, boolean printStatus)
 	{
 		if (printStatus)
 			System.out.println("Creating edges...");
 		graph.createEdges();
 		if (printStatus)
 			System.out.println("Editing graph...");
 		graph.edit(order);
 		if (printStatus)
 			System.out.println("Condensing graph...");
 		graph.condense();
 	}
 	
 	private void customTest()
 	{
 		String fileType = "mnist";
 		
 		Graph graph = new TrainingSetReader(fileType + "_train.txt", new RelatedNeighbourHoodGraph()).getData();
 		constructGraph(graph, Graph.EDIT_1ST_ORDER);
 		
 		String testFile = fileType + "_test.txt";
 		System.out.println("Collecting results...");
         new TestSetReader(testFile, graph, "testResults/classes_k1_2ndorderedit_1stordertest.txt");
         graph.K = 3;
         new TestSetReader(testFile, graph, "testResults/classes_k3_2ndorderedit_1stordertest.txt");
         graph.K = 5;
         new TestSetReader(testFile, graph, "testResults/classes_k5_2ndorderedit_1stordertest.txt");
         try
         {
             // Create file
             FileWriter fstream = new FileWriter("testResults/prototypes_2ndorderedit.txt");
             BufferedWriter out = new BufferedWriter(fstream);
 
             for(GraphPoint p : graph.getPoints())
             {
                 double[] features = p.getFeatures();
                 for(int i = 0; i < features.length; i++)
                 {
                     out.write(features[i] + " ");
                 }
 
                 out.write(p.getClassification() + "\n");
             }
 
             out.close();
         }
         catch (Exception ex)
         {
             System.err.println("Error: " + ex.getMessage());
         }
 		
 		
         /*
 		Iterator<Integer> result = new TestSetReader("mnist_train.txt", gabrielGraph).getResults().iterator();
 		while(result.hasNext())
 			System.out.println(result.next());*/
 		//new TestSetReader("trainSets/train_set_001_n1000_err0.txt", gabrielGraph);*/
 		//new GabrielVisualiser(gabrielGraph);
 	}
 	
 	private void autoTest()
 	{
 		new AutoTester();
 	}
 }
