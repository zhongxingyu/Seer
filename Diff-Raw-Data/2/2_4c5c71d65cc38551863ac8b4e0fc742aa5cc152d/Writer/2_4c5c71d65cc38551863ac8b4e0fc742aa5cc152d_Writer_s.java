 package file;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import model.Edge;
 import model.Graph;
 import model.Vertex;
 
 public class Writer {
 	
	// fr die Funktion Writer mssen der Graph g und den gewnschte Namen fr das Text File bergeben werden
 	public Writer(Graph g, File f) throws IOException{
 		// Das File mit dem gewnschten Namen wird erzeugt
 		FileWriter file = new FileWriter(f);
 		// Solange Vertices im Graph g vorhanden sind
 		for(Vertex v : g.getVertices()){
 			file.write("v\t");	// gibt die Identifikation fr Vertex
 			file.write(v.getName()+"\t");	// der Name fr den Vertex
 			file.write(v.getX()+"\t");	// die X-Koordinate fr den Vertex
 			file.write(v.getY()+"\n");	// die Y-Koordinate fr den Vertex
 		}
 		// Solange Edges in im Graph vorhandne sind
 		for(Edge e : g.getEdges()){
 			file.write("e\t");	// Identifikation fr Edge
 			file.write(e.getV1()+"\t");	// Start Vertex der Edge
 			file.write(e.getV2()+"\t");	// End Vertex der Edge
 			file.write(e.getWeight()+"\n");	// Gewichtung der Edge
 		}
 		
 		// file wird geschlossen
 		file.close();
 	}
 
 }
