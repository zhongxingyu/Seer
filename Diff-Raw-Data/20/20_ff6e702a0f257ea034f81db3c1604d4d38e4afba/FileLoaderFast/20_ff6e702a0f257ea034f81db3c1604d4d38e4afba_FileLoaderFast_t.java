 package visualization;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.Arrays;
 
 import dataStructure.Connection;
 import dataStructure.Point;
 import dataStructure.RoadType;
 
 public class FileLoaderFast implements FileLoader {
   private BigDecimal xMin = new BigDecimal(700000);
   private BigDecimal xMax = new BigDecimal(0);
   private BigDecimal yMin = new BigDecimal(700000);
   private BigDecimal yMax = new BigDecimal(0);
  private int scale = 500; // alskdjalskdj
   private int index;
   private File a, b;
   private BufferedReader inputA, inputB;
 
   Connection[] connections = new Connection[2]; //sorted by ID
   Point[] points = new Point[2]; //sorted by ID
   
   //TODO Specify exception
   //TODO Update controller to not sort
   //TODO Multithread this
   public FileLoaderFast() throws IOException{
     loadPoints();
     loadConnections();
   }
   
   private void loadPoints() throws IOException{
     a = new File("C:/Users/Phillip/MapOfDenmark/src/Union/kdv_node_unload.txt");
     inputA =  new BufferedReader(new FileReader(a));
     String line = null;
     int index = -1;
     
     while ((line = inputA.readLine()) != null){
       if (index == -1)index=1;
       if (index == points.length) resizeP(index*2);
       else{
       String[] split = line.split(",");
      Point p = new Point(index, new BigDecimal(Integer.parseInt(split[3])/scale), new BigDecimal(Integer.parseInt(split[4])/scale));
       points[index] = p;
       //set max and min
       if(points[index].getX().compareTo(xMin)==1) xMin = points[index].getX();
       if(points[index].getX().compareTo(xMax)==1) xMax = points[index].getX();
       if(points[index].getY().compareTo(yMin)==1) yMin = points[index].getY();
       if(points[index].getY().compareTo(yMax)==1) yMax = points[index].getY();
 
       index++;
       }
     }
     
    Arrays.sort(points, 0, index);
   }
   
   private void loadConnections() throws IOException{
     b = new File("C:/Users/Phillip/MapOfDenmark/src/Union/kdv_unload.txt");
     inputB =  new BufferedReader(new FileReader(b));
     String line = null;
     int index = -1;
     
     while ((line = inputB.readLine()) != null){
       if(index == connections.length) resizeC(index*2);
       if (index == -1)index = 0;
      else{
       String[] split =line.split(",");
       Point p = points[Arrays.binarySearch(points, Integer.parseInt(split[0]))];
       Point q = points[Arrays.binarySearch(points, Integer.parseInt(split[1]))];
       RoadType r = null;
 //     switch(Integer.parseInt(split[5])){
 //     case 1: r = RoadType.MOTORVEJ;
 //     case 2: r = RoadType.MOTORTRAFIKVEJ;
 //     case 3: r = RoadType.PRIMRRUTE;
 //     case 4: r = RoadType.SEKUNDRRUTE;
 //     case 5: r = RoadType.VEJ3;
 //     case 6: r = RoadType.ANDENVEJ;
 //     case 8: r = RoadType.STI;
 //     case 10: r = RoadType.MARKVEJ;
 //     case 11: r = RoadType.GGADER;
 //     case 21: r = RoadType.PROJMOTVEJ;
 //     case 22: r = RoadType.PROJMOTORTRAFIKVEJ;
 //     case 23: r = RoadType.PROJPRIMRVEJ;
 //     case 24: r = RoadType.PROJSEKUNDRRUTE;
 //     case 25: r = RoadType.PROJVEJ6M;
 //     case 26: r = RoadType.PROJVEJ3M;
 //     case 28: r = RoadType.PROJSTI;
 //     case 31: r = RoadType.MOTORVEJSAFKRSEL;
 //     case 32: r = RoadType.MOTORTRAFIKVEJSAFKRSEL;
 //     case 33: r = RoadType.PRIMRVEJSAFKRSEL;
 //     case 34: r = RoadType.SEKUNDERVEJSAFKRSEL;
 //     case 35: r = RoadType.ANDENVEJAFKRESL;
 //     case 41: r = RoadType.MOTORVEJSTUNNEL;
 //     case 42: r = RoadType.MOTORTRAFIKVEJSTUNNEL;
 //     case 43: r = RoadType.PRIMRVEJSTUNNEL;
 //     case 44: r = RoadType.SEKUNDRVEJSTUNNEL;
 //     case 45: r = RoadType.ANDENVEJTUNNEL;
 //     case 46: r = RoadType.MINDREVEJTUNNEL;
 //     case 48: r = RoadType.STITUNNEL;
 //     case 80: r = RoadType.FRGE;
 //     case 99: r = RoadType.UKENDT;
       
      connections[index] = new Connection(p ,q , r);
      p.addConnection(connections[index].getID());
      q.addConnection(connections[index].getID());
      index++;
      }
     }
    Arrays.sort(connections, 0, index); //sorted by ID
   }
 
   @Override
   public BigDecimal getxMax() {
     return xMax;
   }
 
   @Override
   public BigDecimal getyMax() {
     return yMax;
   }
 
   @Override
   public Connection[] getConnections() {
     return connections;
   }
 
   @Override
   public Point[] getCords() {
     return points;
   }
   
   private void resizeC(int newsize){
     Connection[] tmp = new Connection[newsize];
     for(int i = 0; i < index; i++){
       tmp[i] = connections[i];
     }
     connections = tmp;
   }
   
   private void resizeP(int newsize){
     Point[] tmp = new Point[newsize];
     for(int i = 0; i < index; i++){
       tmp[i] = points[i];
     }
     points = tmp;
   }
 
 }
