 import java.io.*;
 import java.util.Scanner;
 
 public class Main {
 
     public static void main(String[] args) throws IOException {
 
         String inputFileName = args[0];
         FileReader reader = new FileReader(inputFileName);
         Scanner in = new Scanner(reader);
         String line = in.nextLine();
         Scanner lineScanner = new Scanner(line);
         int numVertices = lineScanner.nextInt();
         lineScanner.close();
 
         // insert code here to build the graph from the input file
         Graph g = new Graph(numVertices);
         int vertex = 0;
         while(in.hasNextLine()) {
             int j = 0;
             line = in.nextLine();
             Scanner scanner = new Scanner(line);
             while(scanner.hasNextInt()) {
                 int i = scanner.nextInt();
                 if(i==1) {
                     g.getVertex(vertex).addToAdjList(j);
                 }
                 j++;
             }
             vertex++;
             scanner.close();
         }
        in.close();
         reader.close();
 
         // conduct the breadth-first search
         g.bfs();
         
         String outputFileName = args[1];
         FileWriter writer = new FileWriter(outputFileName);
         
         // insert code here to output the predecessor information
         BufferedWriter out = new BufferedWriter(writer);
         for(int i = 0; i < numVertices; i++) {
             out.write(g.getVertex(i).getPredecessor() + " ");
         }
         out.close();
         writer.close();
 
     }
 
 }
