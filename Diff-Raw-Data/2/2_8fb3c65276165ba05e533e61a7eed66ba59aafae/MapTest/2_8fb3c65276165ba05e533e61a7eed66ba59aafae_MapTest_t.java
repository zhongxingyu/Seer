 package dk.itu.grp11.test;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import dk.itu.grp11.data.Map;
 import dk.itu.grp11.data.Parser;
 import dk.itu.grp11.enums.MapBound;
 
 public class MapTest {
   /*
   //Testing getPart
   @Test
   public void test0() {
     Parser p = new Parser(new File("src\\dk\\itu\\grp11\\test\\test_points.txt"), new File("src\\dk\\itu\\grp11\\test\\test_roads.txt"));
     
     HashMap<Integer, Point> points = p.points();
     DimensionalTree<Double, RoadType, Road> roads = p.roads();
     Map map = new Map(points, roads);
     
     FileServer fs = new FileServer(map);
     Session ses = new Session("TestSes");
     System.out.println("Getting part: " + map.getPart(320.0, 330.0, 150.0, 100.0, 1, ses));
     assertEquals("var svg = $('#map-container').svg('get');\n"+
                  "svg.line(300.0, "+(377.0-356.0)+", 390.0, 0.0, {stroke: 'rgb(255,0,0)', strokeWidth: '0.3%'});\n", map.getPart(320, 330, 150, 100, 1, ses));
   }*/
   
   //Testing getZoomLevelX
   @Test
   public void test1() {
     assertEquals(1, Map.getZoomLevelX(Parser.getParser().mapBound(MapBound.MAXX)-Parser.getParser().mapBound(MapBound.MINX)));
   }
   
   //Testing getZoomLevelY
   @Test
   public void test2() {
     assertEquals(1, Map.getZoomLevelY(Parser.getParser().mapBound(MapBound.MAXY)-Parser.getParser().mapBound(MapBound.MINY)));
   }
   
   //Testing zoomLevelX
   @Test
   public void test3() {
     
   }
   
   //Testing zoomLevelY
   @Test
   public void test4() {
     
   }
   
   
   
   /*
   
   // Testing single road in viewbox
   @Test
   public void test0() {
     Point[] points = new Point[10];
     points[0] = new Point(1, 300, 356);
     points[1] = new Point(2, 390, 377);
     
     Road[] roads = new Road[10];
     roads[0] = new Road(1, 2, "Niceness street", 1);
     
     Map map = new Map(points, roads, new double[] {}); //Empty double array
     
     assertEquals(map.getPart(320, 330, 150, 100), "<line id=\"line\" x1=\""+300.0+"\" y1=\""+356.0+"\" x2=\""+390.0+"\" y2=\""+377.0+"\" style=\"stroke:rgb(0,0,0); stroke-width:2;\"></line>\n");
   }
   
   //Testing two roads in viewbox and one not in viewbox
   @Test
   public void test1() {
     Point[] points = new Point[10];
     points[0] = new Point(1, 300, 356);
     points[1] = new Point(2, 390, 377);
     points[2] = new Point(3, 800, 700);
     points[3] = new Point(4, 430, 431);
     
     Road[] roads = new Road[10];
     roads[0] = new Road(1, 2, "Niceness street", 1);
     roads[1] = new Road(2, 3, "Long street", 1);
     roads[2] = new Road(3, 4, "Fail street", 1);
     
     Map map = new Map(points, roads, new double[] {});
     
     assertEquals(map.getPart(320, 330, 150, 100), "<line id=\"line\" x1=\""+300.0+"\" y1=\""+356.0+"\" x2=\""+390.0+"\" y2=\""+377.0+"\" style=\"stroke:rgb(0,0,0); stroke-width:2;\"></line>\n"+
                                                   "<line id=\"line\" x1=\""+390.0+"\" y1=\""+377.0+"\" x2=\""+800.0+"\" y2=\""+700.0+"\" style=\"stroke:rgb(0,0,0); stroke-width:2;\"></line>\n");
   }
   
   //Testing that a road outside the viewbox should not be returned
   @Test
   public void test2() {
     Point[] points = new Point[10];
     points[0] = new Point(1, 300, 356);
     points[1] = new Point(2, 390, 377);
     points[2] = new Point(3, 800, 700);
     points[3] = new Point(4, 430, 430);
     
     Road[] roads = new Road[10];
     roads[0] = new Road(1, 2, "Niceness street", 1);
     roads[1] = new Road(2, 3, "Long street", 1);
     roads[2] = new Road(3, 4, "Fail street", 1);
     
     Map map = new Map(points, roads, new double[] {});
     
     assertFalse(map.getPart(320, 331, 150, 100).equals("<line id=\"line\" x1=\""+300.0+"\" y1=\""+356.0+"\" x2=\""+390.0+"\" y2=\""+377.0+"\" style=\"stroke:rgb(0,0,0); stroke-width:2;\"></line>\n"+
                                                   "<line id=\"line\" x1=\""+390.0+"\" y1=\""+377.0+"\" x2=\""+800.0+"\" y2=\""+700.0+"\" style=\"stroke:rgb(0,0,0); stroke-width:2;\"></line>\n"+
                                                   "<line id=\"line\" x1=\""+800.0+"\" y1=\""+700.0+"\" x2=\""+430.0+"\" y2=\""+431.0+"\" style=\"stroke:rgb(0,0,0); stroke-width:2;\"></line>\n"));
     }
   
   //Testing two roads in viewbox and one partly in viewbox
   @Test
   public void test3() {
     Point[] points = new Point[10];
     points[0] = new Point(1, 300, 356);
     points[1] = new Point(2, 390, 377);
     points[2] = new Point(3, 800, 700);
     points[3] = new Point(4, 430, 431);
     
     Road[] roads = new Road[10];
     roads[0] = new Road(1, 2, "Niceness street", 1);
     roads[1] = new Road(2, 3, "Long street", 1);
     roads[2] = new Road(3, 4, "Fail street", 1);
     
     Map map = new Map(points, roads, new double[] {});
     
     assertEquals(map.getPart(320, 331, 150, 100), "<line id=\"line\" x1=\""+300.0+"\" y1=\""+356.0+"\" x2=\""+390.0+"\" y2=\""+377.0+"\" style=\"stroke:rgb(0,0,0); stroke-width:2;\"></line>\n"+
                                                   "<line id=\"line\" x1=\""+390.0+"\" y1=\""+377.0+"\" x2=\""+800.0+"\" y2=\""+700.0+"\" style=\"stroke:rgb(0,0,0); stroke-width:2;\"></line>\n"+
                                                   "<line id=\"line\" x1=\""+800.0+"\" y1=\""+700.0+"\" x2=\""+430.0+"\" y2=\""+431.0+"\" style=\"stroke:rgb(0,0,0); stroke-width:2;\"></line>\n");
   }*/
 }
