 /*
 * sones GraphDB Java Client Library 
 * Copyright (C) 2007-2011 sones GmbH - http://www.sones.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
 package de.sones.GraphDBClient.Demo;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 
 import de.sones.GraphDBClient.GraphDBClient;
 import de.sones.GraphDBClient.Objects.BinaryProperty;
 import de.sones.GraphDBClient.Objects.IHyperEdge;
 import de.sones.GraphDBClient.Objects.ISingleEdge;
 import de.sones.GraphDBClient.Objects.Property;
 import de.sones.GraphDBClient.Objects.Vertex;
 import de.sones.GraphDBClient.QueryResult.QueryResult;
 
 public class GraphDSClientDemo {
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException, Exception {
 		
 		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		GraphDBClient client  = new GraphDBClient("127.0.0.1","test","test",9975);
 		while(true){
 			System.out.print("Type in the GQL: ");
 			String gql = console.readLine();
 			if(gql != ""){
 			QueryResult result = client.Query(gql);	
 			printQueryResult(result);
 			}
 					
 		}
 		
 		
 		
 		
 	}
 	/**
 	 * Prints a QueryResult
 	 * @param myResult QueryResult to print
 	 */
 	public static void printQueryResult(QueryResult myResult){
 		
 		System.out.println("The current QueryResult contains the following data:");
 		System.out.println(myResult.getQueryLanguage() + ": " + myResult.getQueryString());
 		System.out.println("Duartion: " + myResult.getDuration());
 		System.out.println("ResultType: " + myResult.getResultType().toString());
 		System.out.println("Error: " + myResult.getErrorMessage());
 		System.out.println("The fetched Vertices are:\n");
 		
 		List<Vertex> vertexviewlist = myResult.getVertexViewList();
 		
 		for (Vertex vertex : vertexviewlist) {
 			printvertex(vertex, vertexviewlist.indexOf(vertex),0);
 		}
 		
 	}
 	/**
 	 * Prints a vertex with all the attributes and edges
 	 * @param myVertex The Vertex to print
 	 * @param vertexnumber The number of the vertex in the current context
 	 * @param depth The depth of the tabs
 	 */
 	private static void printvertex(Vertex myVertex, long vertexnumber, int depth){
 		String tabs = "";
 		for (int i = 0; i < depth; i++) {
 			tabs += "\t";
 		}
 		
 		System.out.println(tabs + "--"+ " Vertex " + Long.toString(vertexnumber));
 		tabs = "";
 		for (int i = 0; i < depth + 1; i++) {
 			tabs += "\t";
 		}
 		System.out.println(tabs + "\\");
 		System.out.println(tabs + " |- Properties are:");
 		List<Property> properties = myVertex.getProperties();
 		for (Property property : properties) {
 			System.out.println(tabs + " |   *" + property.getId() + ": " + property.getValue());
 		}
 		System.out.println(tabs + " |");
 		List<BinaryProperty> binproper = myVertex.getBinaryProperties();
 		System.out.println(tabs + " |- BinaryProperties are:");
 		for (BinaryProperty binaryProperty : binproper) {
 			System.out.println(tabs + " |   *" + binaryProperty.getId() + ": " + binaryProperty.getContent());
 		}
 		System.out.println(tabs + " |");
 		List<IHyperEdge> edges = myVertex.getHyperEdges();
 		System.out.println(tabs + " |- Edges are:");
 		
 		for (IHyperEdge edge : edges) {
 			System.out.println(tabs + "     ->" + edge.getName()+":");
 
 
 			properties = edge.getProperties();
 			for (Property property : properties) {
 				System.out.println(tabs + "       \\   " + property.getId() + ": " + property.getValue());
 			}
 			List<ISingleEdge> singleedges = edge.getEdges();
 			for (ISingleEdge singleedge : singleedges) {
 				printvertex((Vertex) singleedge.getTargetVertex(),(long)edges.indexOf(edge),depth + 2);
 			}
 		}
 		List<ISingleEdge> sedges = myVertex.getSingleEdges(); 
 		for (ISingleEdge sedge : sedges) {
 			System.out.println(tabs + "  ->" + sedge.getName()+":");
 			printvertex((Vertex) sedge.getTargetVertex(),sedges.indexOf(sedge),depth + 2);
 		}
 		
 		System.out.println("\n");
 		
 		
 		
 				
 		
 	}
 
 }
