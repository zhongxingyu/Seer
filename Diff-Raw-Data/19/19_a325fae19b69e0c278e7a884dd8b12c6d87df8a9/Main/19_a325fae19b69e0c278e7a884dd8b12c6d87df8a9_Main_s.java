 package ab11.aufgabe3;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Baka
  * Date: 23.01.13
  * Time: 20:28
  * To change this template use File | Settings | File Templates.
  */
 public class Main {
 
     public static void main(String[] args){
         IGraph graph = new Graph();
 
         graph.node(0,"hallo");
         graph.node(1,"hallo");
         graph.node(2,"hallo");
         graph.node(3,"hallo");
 
         graph.edge(0,1,100);
         graph.edge(0,2,50);
         graph.edge(1,3,10);
         graph.edge(1,2,15);
 
         graph.print();
     }
 
 }
