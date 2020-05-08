 package edu.uft.tocantins.Models;
 
 import javax.swing.JOptionPane;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Definição da classe MyGraph
  * Realiza a representação do grafo (estado)
  * @author @author <a href="mailto:wolfdragon2@gmail.com">Sávio S. Dias</a>
  * @version 1.0
  */
 public class MyGraph extends Object
 {
     // <------------------- 0.Variables ------------------->
     private static MyGraph instance;
     private ArrayList<Vertex> vertexes = new ArrayList<Vertex>();
     private ArrayList<Edge> edges = new ArrayList<Edge>();
     private int[][] adjacencyMatrix;
     
     // <------------------- 1.Constructors ------------------->
     /**
      * Construtor vazio, inicializa o grafo para novas operações
      */
     private MyGraph()
     {
         vertexes.clear();
         edges.clear();
         
         readDataBase();
     }
     
     // <------------------- 2.Getters methods ------------------->
     /**
      * Retorna a classe, evitando que a classe seja varias vezes instanciada
      * @return classe instanciada
      */
     public static MyGraph getInstance()
     {
         if( instance == null ) instance = new MyGraph();
         return instance;
     }
     
     /**
      * Retorna os vertices do grafo
      * @return vertices do grafo
      */
     public ArrayList<Vertex> getVertexes()
     {
         return this.vertexes;
     }
     
     /**
      * Retorna vertice do vetor de vertices no indice index
      * @param index indice do vertice no vetor de vertice
      * @return vertice no indice index
      */
     public Vertex getVertex( int index )
     {
         return this.vertexes.get( index );
     }
     
     /**
      * Retorna as arestas do grafo
      * @return arestas do grafo
      */
     public ArrayList<Edge> getEdges()
     {
         return this.edges;
     }
     
     /**
      * Retorna aresta do vetor de arestas no indice index
      * @param index indice da aresta no vetor de arestas
      * @return aresta no indice index
      */
     public Edge getEdge( int index )
     {
         return this.edges.get( index );
     }
     
     /**
      * Retorna aresta de origem source e destino destination, caso exista
      * @param source id do vertice de origem
      * @param destination id do vertice de destino
      * @return aresta entre os dois vertices
      */
     public Edge getEdge( int source, int destination )
     {
         if( hasEdge( source, destination ) )
         {
             for( int i = 0; i < edges.size(); i++ )
             {
                 if( edges.get( i ).compare( source, destination ) )
                     return edges.get( i );
             }
         }
         return null;
     }
     
     /**
      * Retorna a matriz de adjacencias
      * @return matriz de adjacencias
      */
     public int[][] getAdjacencyMatrix()
     {
         if( adjacencyMatrix == null ) makeMatrix();
         return this.adjacencyMatrix;
     }
     
     // <------------------- 3.Read and Transfer Data ------------------->
     /**
      * Realiza leitura das tabelas do banco de dados e os armazena na instância
      */
     public void readDataBase()
     {
         try
         {
             DBManager dataBase = DBManager.getInstance();
             
             // Adding vertexes (cities)
             ResultSet search = dataBase.getResultSet( "SELECT * FROM cidades" );
             while( search.next() )
                 addVertex( search.getString( "Nome" ) , search.getInt( "Populacao" ) );
             
             search.close();
             search = null;
             
             // Adding edges (roads)
             search = dataBase.getResultSet( "SELECT * FROM conexoes" );
             while( search.next() )
                 addEdge( search.getInt( "idConexoes" ) - 1 , search.getInt( "Cidade" ) - 1, search.getInt( "Distancia" ), !search.getBoolean( "Sem_Asfalto" ) );
             
             search.close();
         }
         catch( SQLException exception )
         {
             JOptionPane.showMessageDialog( null, exception.getMessage() );
         }
     }
     
     // <------------------- 4.Functional methods ------------------->
     /**
      * Adiciona um vertice ao grafo (cidade)
      * @param cityName nome da cidade a ser definida pelo vertice
      * @param population população da cidade representada pelo vertice
      * @return true se adicionou vertice com sucesso, false caso contrario
      */
     public boolean addVertex( String cityName, int population )
     {
         return ( vertexes.add( new Vertex( cityName, population ) ) ) ? true : false;
     }
     
     /**
      * Adiciona uma aresta ao grafo (estrada)
      * @param source id do vertice de origem
      * @param destination id do vertice de destino
      * @param distance distancia do vertice origem ao vertice destino
      * @return true se adicionou aresta com sucesso, false caso contrario
      */
     public boolean addEdge( int source, int destination, int distance, boolean asphalt )
     {
         if( hasVertex( source ) && hasVertex( destination ) )
             return ( edges.add( new Edge( source, destination, distance, asphalt ) ) ) ? true : false;
         else return false;
     }
     
     /**
      * Cria matriz de adjacencia do grafo
      */
     public void makeMatrix()
     {
         allocateMatrix();
         
         for( int i = 0; i < vertexes.size(); i++ )
         {
             for( int j = 0; j < vertexes.size(); j++ )
             {
                 if( i == j )
                     adjacencyMatrix[i][j] = vertexes.get( i ).getPopulation();
                 else if( hasEdge( i, j ) )
                     adjacencyMatrix[i][j] = adjacencyMatrix[j][i] = getEdge( i, j ).getDistance();
                 else adjacencyMatrix[i][j] = adjacencyMatrix[j][i] = 0;
             }
         }
     }
     
     /**
      * Aloca matriz de adjacencia do grafo
      */
     private void allocateMatrix()
     {
         adjacencyMatrix = new int[ vertexes.size() ][ vertexes.size() ];
     }
     
     /**
      * Verifica se existe aresta entre os vertices de id source e destination
      * @param source id do vertice de origem
      * @param destination id do vertice de destino
      * @return true se existe a aresta entre os vertices, false caso contrario
      */
     public boolean hasEdge( int source, int destination )
     {
         for( int i = 0; i < edges.size(); i++ )
             if( edges.get( i ).compare( source, destination ) )
                 return true;
         
         return false;
     }
     
     /**
      * Verifica se existe vertice com ID, id
      * @param id id a ser verificado
      * @return true se existe, false caso contrario
      */
     private boolean hasVertex( int id )
     {
         for( int i = 0; i < vertexes.size(); i++ )
             if( vertexes.get( i ).getID() == id)
                 return true;
         return false;
     }
     
     // <------------------- 5.Algorithm ------------------->
     /**
      * Usa o algoritmo de dijkstra para calcular o menor caminho de source à destination
      * @param source id do vertice de origem
      * @param destination id do vertice de destino
      * @return o tamanho do menor caminho entre source e destination
      */
     public int dijkstra( int source, int destination )
     {
        if( adjacencyMatrix == null ) makeMatrix();
        
         int distances[] = new int[ vertexes.size() ];
         int visited[] = new int[ vertexes.size() ];
         
         Arrays.fill( distances, 0x7f );
         distances[ source ] = 0;
         
         while( true )
         {
             int n = -1;
             for( int i = 0; i < vertexes.size(); i++ )
                if( visited[i] != 1 && ( n < 0 || distances[i] < distances[n] ) )
                     n = i;
             
             if( n < 0 ) break;
             visited[n] = 1;
             
             for( int i = 0; i < vertexes.size(); i++ )
                if( adjacencyMatrix[n][i] != 1 && distances[i] > distances[n] + adjacencyMatrix[n][i] )
                     distances[i] = distances[n] + adjacencyMatrix[n][i];
         }
         
         return distances[ destination ];
     }
 }
