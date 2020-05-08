 package pt.inevo.encontra.geometry;
 
 import pt.inevo.encontra.graph.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 @SuppressWarnings("serial")
 public class PolygonSet extends ArrayList<Polygon>{
 
     public static int MAX_GRAPH_VERTEX_COUNT=50000;
 
     ArrayList<Point> _all_points_array=new ArrayList<Point>();
     static Logger log = Logger.getLogger(PolygonSet.class.getName());
 
     /***
      * @desc simplifies the polygons in this set
      * @note removes inclusions and disposes small polygons
      */
     void Simplify(double smaller_polygon_length)
     {
         //STARTING_PROCESS_MESSAGE("Polygon set simplification");
 
         Polygon p1, p2;
         int i,j;
         boolean changes = false;
 
         // remove small polygons
         j=0;
         for (i=size(); i>0;) {
             p1 = get(--i);
 
             if (p1.GetLength()< smaller_polygon_length )  {
                 remove(i);
                 j++;
             }
         }
 
         log.info(String.format("Removed %d small polygons", j));
 
         // check adjacencies
         for (i=0; i<size();i++) {
             p1 = get(i);
             for (j=i+1; j<size();j++) {
                 p2 = get(j);
 
                 // see if p1 and p2 are strictly adjacent
                 if (p1.IsAdjacent(p2)) {
                     // check if p1 contains p2
                     if (p1.Contains(p2)) {
                         changes |= p1.Minus(p2);
                     } else {
                         // just when p1 does not contains p2
                         // we need to check if p2 contains p1
                         if (p2.Contains(p1)) {
                             changes |= p2.Minus(p1);
                         }
                     }
                 }
             }
         }
 
         //ENDING_PROCESS_MESSAGE();
     }
 
 
     // Constructs a polygon set from given line set
     public boolean Construct(LineSet line_set)
     {
         //STARTING_PROCESS_MESSAGE("Polygon set construction");
 
         if (line_set==null) return false;
 
 
         // first we clear the polygons array
         //WX_CLEAR_ARRAY(_polygons_array);
         clear();
 
         // then we create the graph
         log.info("Creating graph...");
         Graph G = LinesToGraph(line_set);
 
 
 
         log.info(String.format("Created graph with %d vertices and %d edges.",
                 G.getVertexCount(),
                 G.getEdgeCount()));
 
         if (G.getVertexCount()>MAX_GRAPH_VERTEX_COUNT) {
             log.info("Number of vertices exceeds the maximum allowed. Polygon detection interrupted.");
         } else {
             // run the Floyd-Warshall Algorithm
             //G.FloydWarshall();
 
 
             //YIELD_CONTROL();
 
             /*
                if (!PolygonDetector::WasInterrupted()){ */
             // run the Horton's Algorithm
             log.info("Detecting cycles.");
             CycleSet cycle_set = new CycleSet(G); //.Horton();
             //YIELD_CONTROL();
 
             if (cycle_set==null) {
                 log.log(Level.SEVERE,"Could not find the cycle set produced by Horton algorithm.");
             } else {
                 //if (!PolygonDetector::Silent())
                 log.info(String.format("Detected %d cycles.", cycle_set.size()));
 
                 //if (!PolygonDetector::WasInterrupted()){
                 // Convert cycles to polygons
                 CyclesToPolygons(cycle_set);
                 //}
 
                 //DELETE_OBJECT(cycle_set);
             }
             //}
         }
 
 
         //ENDING_PROCESS_MESSAGE();
 
         return true;
     }
 
 
     /***
      * @desc fill the points array with all points existing in entities
      */
     void CreatePointsArray(LineSet line_set)
     {
 
         if (line_set!=null) {
             _all_points_array.clear();
             int i;
             for (i=0; i< line_set.size(); i++) {
 
                 Line line = line_set.get(i);
                 line.CalculateFirstAndLastPoint();
 
                 Point p = line.GetStartPoint();
                 if (p!=null) {
                     p.SetOwnerEntity(line);
                     p.setIndex(i);
                     _all_points_array.add(p);
                 }
 
                 p = line.GetEndPoint();
                 if (p!=null) {
                     p.SetOwnerEntity(line);
                     p.setIndex(i);
                     _all_points_array.add(p);
                 }
             }
 
             // the points are sorted in order to allow fast
             // identification of coincident points
             //_all_points_array.Sort(GraphicalPrimitives2D::Point2D::CompareOrder);
             Collections.sort(_all_points_array);
 
             // at the end we update the index on all points
             for(i=0; i<_all_points_array.size();i++)
                 _all_points_array.get(i).setIndex(i);
         }
     }
 
     /***
      * @desc creates a graph representing the lines of this table
      * @return a pointer to a new graph object
      * @note IMPORTANT: don't forget to delete the Graph elsewhere
      */
     Graph LinesToGraph(LineSet line_set)
     {
         //STARTING_PROCESS_MESSAGE("Graph construction");
 
         if (line_set==null) {
             log.log(Level.SEVERE,"Unable to compute graph from line set.");
             return null;
         }
         // first, create the points array from the current line set
         CreatePointsArray(line_set);
 
         // then create the graph
         Graph G = new Graph();
 
         // because of using GetPointCount we are sure that all points
         // already have an ID, so we can add lines to the graph
         GetPointCount();
 
 
         Line  line;
 
         for (int i=0; i<line_set.size(); i++ ) {
             line = line_set.get(i);
             if (line!=null) {
                 //G.SetAdjacency(line.GetStartPoint().GetID(), line.GetEndPoint().GetID());
 
                long n1Id = line.GetStartPoint().GetID();
                GraphNode n1 = G.findNode(n1Id);
                if (n1 == null) {
                    n1 = new GraphNode(n1Id);
                    G.addVertex(n1);
                }
 //                n1.setUserDatum("point", line.GetStartPoint().toString(),UserData.SHARED);
                 n1.setUserDatum("point", line.GetStartPoint().toString());
 
                long n2Id = line.GetEndPoint().GetID();
                GraphNode n2 = G.findNode(n2Id);
                if (n2 == null) {
                    n2 = new GraphNode(n2Id);
                    G.addVertex(n2);
                }
 //                n2.setUserDatum("point", line.GetEndPoint().toString(), UserData.SHARED);
                 n2.setUserDatum("point", line.GetEndPoint().toString());
 
 
                 G.addEdge(new GraphAdjacencyEdge(n1,n2), n1, n2);
                 //G.addEdge(new GraphAdjacencyEdge(n2,n1));
             }
 
         }
 
         //ENDING_PROCESS_MESSAGE();
         return G;
     }
 
     /***
      * @desc determine the number of existing points.
      *       this method also sets a non-unique id for each point.
      *       this id is shared by all coincident points
      */
     int GetPointCount()
     {
         int id = 0;
         Point current, previous = null;
 
         for (int i=0; i<_all_points_array.size(); i++) {
             current = _all_points_array.get(i);
 
             if (( (previous!=null) && !current.equals(previous)) || (previous==null)) {
                 id++;
                 previous = current;
             }
             current.SetID(id-1);
         }
 
         return id;
     }
 
     /***
      * @desc creates polygons from cycles
      * @para cycle set
      */
     void CyclesToPolygons(CycleSet cycle_set)
     {
         //STARTING_PROCESS_MESSAGE("Cycle to polygon conversion");
 
         if (cycle_set!=null) {
             // then create polygons
             for (int i=0; i<cycle_set.size();i++){
                 Cycle cycle = cycle_set.get(i);
                 //YIELD_CONTROL();
 
                 if (cycle.GetVertexCount()>2) {
                     Polygon plg = new Polygon();
 
                     for (int j=0; j<cycle.GetVertexCount();j++){
                         Point p = PointByID(cycle.GetVertex(j).intValue());
                         if (p!=null)
                             plg.AddVertex(p);
                     }
 
                     // adds new polygon to polygons array
                     add(plg);
 
                     // update polygon first and last point, plus closed status
                     plg.CalculateFirstAndLastPoint();
 
 
                     // simplify the polygon, removing unnecessary vertices
                     // IMPORTANT NOTICE: This should not be called here,
                     // because we need all vertices for remove contained polygons
                     // with single adjacencies. This simplification is done
                     // later, within the polygon set simplification
                     // plg.Simplify();
 
                 }
             }
         }
         //ENDING_PROCESS_MESSAGE();
     }
 
     /***
      * @returns the point with id indicated
      */
     Point PointByID(int id)
     {
         Point p;
 
         // note: the position is at least equal to id
         for (int i=0; i<_all_points_array.size();i++) {
             p = _all_points_array.get(i);
             if (p.GetID()==id)
                 return p;
         }
 
         return null;
     }
 
 
     /***
      * @return a string with the SVG containing the polygon set
      */
     public String AsSVG(boolean colorized)
     {
         String result = "";
 
         // Add SVG Header
         //result+="<?xml version=\"1.0\" standalone=\"no\"?>\n";
         //result+="<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\n";
         //result+="\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n";
 
        result+="<svg width=\"100%\" height=\"100%\" "; // viewBox=\"0 0 1.0 1.0\"\n";
         result+="xmlns=\"http://www.w3.org/2000/svg\">\n";
 
         float factor = 1.0f;
         float delta = 0;
 
         if (size()>0)
             delta=1.0f/(size()+1);
 
         float r, g, b;
 
         /*
           // draw polygons
           for (int i=0;i<size();i++) {
               Polygon p = get(i);
               //Color::HSVtoRGB(&r,&g,&b, factor*POLYGON_COLOR_H, POLYGON_COLOR_S, POLYGON_COLOR_V);
               // printf("%f | %f | %s\n", factor, factor*POLYGON_COLOR_H, Color::RGBtoHEX(r,g,b).c_str());
               if (p!=null)
                   result += p.AsString(true,"#000000","none");//,wxString::FromAscii(Color::RGBtoHEX(r,g,b).c_str())) + wxT("\n");
               factor-=delta;
           }*/
 
         // draw polylines
         for (int i=0;i<size();i++) {
             Polygon p = get(i);
            String color = (colorized) ? Integer.toHexString( new Random().nextInt(16777215)) : "000000";
             if (p!=null)
                 result += p.AsString(true,"#"+color,"none");
             factor-=delta;
         }
 
         result += "</svg>";
         return result;
     }
 }
