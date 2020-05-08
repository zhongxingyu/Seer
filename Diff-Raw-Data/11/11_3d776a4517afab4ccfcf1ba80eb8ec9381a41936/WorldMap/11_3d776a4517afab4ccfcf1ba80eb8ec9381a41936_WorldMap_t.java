 package com.pathfinder.internal;
 
 import com.google.common.base.Functions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Maps;
 import org.jgrapht.Graph;
 import org.jgrapht.graph.DirectedMultigraph;
 
 import java.util.List;
 import java.util.Map;
 
 import static org.jgrapht.Graphs.addAllVertices;
 import static org.jgrapht.alg.DijkstraShortestPath.findPathBetween;
 
 final class WorldMap {
     private static final City HONG_KONG = new City("CNHKG", new Coordinate(114.304428, 22.337864));
     private static final City MELBOURNE = new City("AUMEL", new Coordinate(144.962982, -37.813187));
     private static final City STOCKHOLM = new City("SESTO", new Coordinate(18.064487, 59.332787));
     private static final City HELSINKI = new City("FIHEL", new Coordinate(24.938240, 60.169811));
     private static final City CHICAGO = new City("USCHI", new Coordinate(-87.629799, 41.878113));
     private static final City TOKYO = new City("JNTKO", new Coordinate(139.691711, 35.689487));
     private static final City HAMBURG = new City("DEHAM", new Coordinate(9.991575, 53.553814));
     private static final City SHANGAI = new City("CNSHA", new Coordinate(121.473701, 31.230392));
     private static final City ROTTERDAM = new City("NLRTM", new Coordinate(4.481776, 51.924217));
     private static final City GOTHENBURG = new City("SEGOT", new Coordinate(11.986500, 57.696995));
     private static final City HANG_ZOU = new City("CNHGH", new Coordinate(120.155357, 30.273977));
     private static final City DALLAS = new City("USDAL", new Coordinate(-96.769920, 32.802956));
     private static final City NEW_YORK = new City("USNYC", new Coordinate(-74.005974, 40.714352));
 
     private static final List<City> CITIES = ImmutableList.of(
         HONG_KONG, MELBOURNE, STOCKHOLM, HELSINKI, CHICAGO, TOKYO, HAMBURG, SHANGAI, ROTTERDAM, GOTHENBURG, HANG_ZOU, DALLAS, NEW_YORK);
 
     private static final Graph<City, Leg> GRAPH = new DirectedMultigraph<City, Leg>(new LegFactory());
     private static final Map<String, City> UNLOCODE_MAP = Maps.uniqueIndex(CITIES, Functions.toStringFunction());
 
     private static void newLeg(City a, City b){
         GRAPH.addEdge(a, b);
         GRAPH.addEdge(b, a);
        System.out.println(a + ", " + b + ": " + a.distanceTo(b));
     }
 
     static {
         addAllVertices(GRAPH, CITIES);
         
         newLeg(HONG_KONG, MELBOURNE);
         newLeg(HONG_KONG, SHANGAI);
         newLeg(HONG_KONG, ROTTERDAM);
         newLeg(MELBOURNE, TOKYO);
         newLeg(STOCKHOLM, HELSINKI);
         newLeg(STOCKHOLM, GOTHENBURG);
         newLeg(HELSINKI, HAMBURG);
         newLeg(CHICAGO, DALLAS);
         newLeg(CHICAGO, NEW_YORK);
         newLeg(TOKYO, HANG_ZOU);
         newLeg(TOKYO, DALLAS);
         newLeg(HAMBURG, ROTTERDAM);
         newLeg(HAMBURG, GOTHENBURG);
         newLeg(SHANGAI, HANG_ZOU);
         newLeg(ROTTERDAM, GOTHENBURG);
         newLeg(ROTTERDAM, NEW_YORK);
         newLeg(DALLAS, NEW_YORK);
     }
 
     static List<Leg> shortestPathBetween(String origin, String destination){
         return findPathBetween(GRAPH, coordinate(origin), coordinate(destination));
     }
 
     private static City coordinate(String unlocode){
         return UNLOCODE_MAP.get(unlocode);
     }
 }
