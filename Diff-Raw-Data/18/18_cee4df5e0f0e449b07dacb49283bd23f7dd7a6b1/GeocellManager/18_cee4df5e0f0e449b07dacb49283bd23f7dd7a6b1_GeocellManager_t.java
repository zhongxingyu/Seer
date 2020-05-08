 package com.beoui.geocell;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 
 import com.beoui.geocell.comparator.LocationComparableTuple;
 import com.beoui.geocell.model.BoundingBox;
 import com.beoui.geocell.model.CostFunction;
 import com.beoui.geocell.model.DefaultCostFunction;
 import com.beoui.geocell.model.GeocellQuery;
 import com.beoui.geocell.model.LocationCapable;
 import com.beoui.geocell.model.Point;
 import com.beoui.geocell.model.Tuple;
 
 /**
 #
 # Copyright 2010 Alexandre Gellibert
 #
 # Licensed under the Apache License, Version 2.0 (the "License");
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #     http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
  */
 
 
 /**
  * Ported java version of python geocell: http://code.google.com/p/geomodel/source/browse/trunk/geo/geocell.py
  *
  * Defines the notion of 'geocells' and exposes methods to operate on them.
 
     A geocell is a hexadecimal string that defines a two dimensional rectangular
     region inside the [-90,90] x [-180,180] latitude/longitude space. A geocell's
     'resolution' is its length. For most practical purposes, at high resolutions,
     geocells can be treated as single points.
 
     Much like geohashes (see http://en.wikipedia.org/wiki/Geohash), geocells are
     hierarchical, in that any prefix of a geocell is considered its ancestor, with
     geocell[:-1] being geocell's immediate parent cell.
 
     To calculate the rectangle of a given geocell string, first divide the
     [-90,90] x [-180,180] latitude/longitude space evenly into a 4x4 grid like so:
 
                  +---+---+---+---+ (90, 180)
                  | a | b | e | f |
                  +---+---+---+---+
                  | 8 | 9 | c | d |
                  +---+---+---+---+
                  | 2 | 3 | 6 | 7 |
                  +---+---+---+---+
                  | 0 | 1 | 4 | 5 |
       (-90,-180) +---+---+---+---+
 
     NOTE: The point (0, 0) is at the intersection of grid cells 3, 6, 9 and c. And,
           for example, cell 7 should be the sub-rectangle from
           (-45, 90) to (0, 180).
 
     Calculate the sub-rectangle for the first character of the geocell string and
     re-divide this sub-rectangle into another 4x4 grid. For example, if the geocell
     string is '78a', we will re-divide the sub-rectangle like so:
 
                    .                   .
                    .                   .
                . . +----+----+----+----+ (0, 180)
                    | 7a | 7b | 7e | 7f |
                    +----+----+----+----+
                    | 78 | 79 | 7c | 7d |
                    +----+----+----+----+
                    | 72 | 73 | 76 | 77 |
                    +----+----+----+----+
                    | 70 | 71 | 74 | 75 |
       . . (-45,90) +----+----+----+----+
                    .                   .
                    .                   .
 
     Continue to re-divide into sub-rectangles and 4x4 grids until the entire
     geocell string has been exhausted. The final sub-rectangle is the rectangular
     region for the geocell.
  *
  * @author api.roman.public@gmail.com (Roman Nurik)
  * @author (java portage) Alexandre Gellibert
  *
  *
  */
 
 public class GeocellManager {
 
     // The maximum *practical* geocell resolution.
     public static final int MAX_GEOCELL_RESOLUTION = 13;
 
     // The maximum number of geocells to consider for a bounding box search.
     private static final int MAX_FEASIBLE_BBOX_SEARCH_CELLS = 300;
 
     // Function used if no custom function is used in bestBboxSearchCells method
     private static final CostFunction DEFAULT_COST_FUNCTION = new DefaultCostFunction();
 
     private static final Logger logger = GeocellLogger.get();
 
     /**
      * Returns the list of geocells (all resolutions) that are containing the point
      *
      * @param point
      * @return Returns the list of geocells (all resolutions) that are containing the point
      */
     public static List<String> generateGeoCell(Point point) {
         List<String> geocells = new ArrayList<String>();
         String geocellMax = GeocellUtils.compute(point, GeocellManager.MAX_GEOCELL_RESOLUTION);
         for(int i = 1; i < GeocellManager.MAX_GEOCELL_RESOLUTION; i++) {
             geocells.add(GeocellUtils.compute(point, i));
         }
         geocells.add(geocellMax);
         return geocells;
     }
 
     /**
      * Returns an efficient set of geocells to search in a bounding box query.
 
       This method is guaranteed to return a set of geocells having the same
       resolution.
 
      * @param bbox: A geotypes.Box indicating the bounding box being searched.
      * @param costFunction: A function that accepts two arguments:
      * numCells: the number of cells to search
      * resolution: the resolution of each cell to search
             and returns the 'cost' of querying against this number of cells
             at the given resolution.)
      * @return A list of geocell strings that contain the given box.
      */
     public static List<String> bestBboxSearchCells(BoundingBox bbox, CostFunction costFunction) {
 
         String cellNE = GeocellUtils.compute(bbox.getNorthEast(), GeocellManager.MAX_GEOCELL_RESOLUTION);
         String cellSW = GeocellUtils.compute(bbox.getSouthWest(), GeocellManager.MAX_GEOCELL_RESOLUTION);
 
         // The current lowest BBOX-search cost found; start with practical infinity.
         double minCost = Double.MAX_VALUE;
 
         // The set of cells having the lowest calculated BBOX-search cost.
         List<String> minCostCellSet = new ArrayList<String>();
 
         // First find the common prefix, if there is one.. this will be the base
         // resolution.. i.e. we don't have to look at any higher resolution cells.
         int minResolution = 0;
         int maxResoltuion = Math.min(cellNE.length(), cellSW.length());
         while(minResolution < maxResoltuion  && cellNE.substring(0, minResolution+1).startsWith(cellSW.substring(0, minResolution+1))) {
             minResolution++;
         }
 
         // Iteravely calculate all possible sets of cells that wholely contain
         // the requested bounding box.
         for(int curResolution = minResolution; curResolution < GeocellManager.MAX_GEOCELL_RESOLUTION + 1; curResolution++) {
             String curNE = cellNE.substring(0, curResolution);
             String curSW = cellSW.substring(0, curResolution);
 
             int numCells = GeocellUtils.interpolationCount(curNE, curSW);
             if(numCells > MAX_FEASIBLE_BBOX_SEARCH_CELLS) {
                 continue;
             }
 
             List<String> cellSet = GeocellUtils.interpolate(curNE, curSW);
             Collections.sort(cellSet);
 
             double cost;
             if(costFunction == null) {
                 cost = DEFAULT_COST_FUNCTION.defaultCostFunction(cellSet.size(), curResolution);
             } else {
                 cost = costFunction.defaultCostFunction(cellSet.size(), curResolution);
             }
 
             if(cost <= minCost) {
                 minCost = cost;
                 minCostCellSet = cellSet;
             } else {
                 if(minCostCellSet.size() == 0) {
                     minCostCellSet = cellSet;
                 }
                 // Once the cost starts rising, we won't be able to do better, so abort.
                 break;
             }
         }
         logger.log(Level.INFO, "Calculate cells "+StringUtils.join(minCostCellSet, ", ")+" in box ("+bbox.getSouth()+","+bbox.getWest()+") ("+bbox.getNorth()+","+bbox.getEast()+")");
         return minCostCellSet;
     }
 
     /**
      *
      * Performs a proximity/radius fetch on the given query.
 
         Fetches at most <max_results> entities matching the given query,
         ordered by ascending distance from the given center point, and optionally
         limited by the given maximum distance.
 
         This method uses a greedy algorithm that starts by searching high-resolution
         geocells near the center point and gradually looking in lower and lower
         resolution cells until max_results entities have been found matching the
         given query and no closer possible entities can be found.
      *
      * @param center A Point indicating the center point around which to search for matching entities.
      * @param maxResults (required) must be > 0. The larger this number, the longer the fetch will take.
      * @param maxDistance (optional) A number indicating the maximum distance to search, in meters. Set to 0 if no max distance is expected
      * @param entityClass class of the entity to search. MUST implement LocationCapable class because we use entity location and key, and also "GEOCELLS" columnn in query.
      * @param baseQuery query that will be enhanced by algorithm. see GeocellQuery class for more information.
     * @param pm PersistenceManager to be used to create new queries
      * @param maxGeocellResolution the resolution (size of cell) when we start the algorithm. If you expect your search to run until big boxes (not many entities near the center), think about using a lower resolution for better performance. If you don't want to bother, use other method below without this parameter.
      * @return the list of entities found near the center and ordered by distance.
      *
      * @throws all exceptions that can be thrown when running queries.
      */
     @SuppressWarnings("unchecked")
     public static final <T extends LocationCapable> List<T> proximityFetch(Point center, int maxResults, double maxDistance, Class<T> entityClass, GeocellQuery baseQuery, PersistenceManager pm, int maxGeocellResolution) {
         List<LocationComparableTuple<T>> results = new ArrayList<LocationComparableTuple<T>>();
 
        Validate.isTrue(maxGeocellResolution < MAX_GEOCELL_RESOLUTION + 1,
                 "Invalid max resolution parameter. Must be inferior to ", MAX_GEOCELL_RESOLUTION);
 
         // The current search geocell containing the lat,lon.
         String curContainingGeocell = GeocellUtils.compute(center, maxGeocellResolution);
 
         // Set of already searched cells
         Set<String> searchedCells = new HashSet<String>();
 
         /*
          * The currently-being-searched geocells.
          * NOTES:
          * Start with max possible.
          * Must always be of the same resolution.
          * Must always form a rectangular region.
          * One of these must be equal to the cur_containing_geocell.
          */
         List<String> curGeocells = new ArrayList<String>();
         curGeocells.add(curContainingGeocell);
         double closestPossibleNextResultDist = 0;
 
         /*
          * Assumes both a and b are lists of (entity, dist) tuples, *sorted by dist*.
          * NOTE: This is an in-place merge, and there are guaranteed no duplicates in the resulting list.
          */
 
         int noDirection [] = {0,0};
         List<Tuple<int[], Double>> sortedEdgesDistances = Arrays.asList(new Tuple<int[], Double>(noDirection, 0d));
 
         while(!curGeocells.isEmpty()) {
             closestPossibleNextResultDist = sortedEdgesDistances.get(0).getSecond();
             if(maxDistance > 0 && closestPossibleNextResultDist > maxDistance) {
                 break;
             }
 
             Set<String> curTempUnique = new HashSet<String>(curGeocells);
             curTempUnique.removeAll(searchedCells);
             List<String> curGeocellsUnique = new ArrayList<String>(curTempUnique);
 
             // Run query on the next set of geocells.
            String queryStart = baseQuery.getBaseQuery() == null || baseQuery.getBaseQuery().trim().length() == 0 ? " " : baseQuery.getBaseQuery() + " && ";
            Query query = pm.newQuery(entityClass, queryStart + "geocells.contains(geocellsP)");
 
             if(baseQuery.getDeclaredParameters() == null || baseQuery.getDeclaredParameters().trim().length() == 0) {
                 query.declareParameters("String geocellsP");
             } else {
                 query.declareParameters(baseQuery.getDeclaredParameters() + ", String geocellsP");
             }
 
             List<T> newResultEntities;
             if(baseQuery.getParameters() == null || baseQuery.getParameters().isEmpty()) {
                 newResultEntities =(List<T>) query.execute(curGeocellsUnique);
             } else {
                 List<Object> parameters = new ArrayList<Object>(baseQuery.getParameters());
                 parameters.add(curGeocellsUnique);
                 newResultEntities = (List<T>) query.executeWithArray(parameters.toArray());
             }
 
             logger.log(Level.FINE, "fetch complete for: " + StringUtils.join(curGeocellsUnique, ", "));
 
             searchedCells.addAll(curGeocells);
 
             // Begin storing distance from the search result entity to the
             // search center along with the search result itself, in a tuple.
             List<LocationComparableTuple<T>> newResults = new ArrayList<LocationComparableTuple<T>>();
             for(T entity : newResultEntities) {
                 newResults.add(new LocationComparableTuple<T>(entity, GeocellUtils.distance(center, entity.getLocation())));
             }
             // TODO (Alex) we can optimize here. Sort is needed only if new_results.size() > max_results.
             Collections.sort(newResults);
             newResults = newResults.subList(0, Math.min(maxResults, newResults.size()));
 
             // Merge new_results into results
             for(LocationComparableTuple<T> tuple : newResults) {
                 // contains method will check if entity in tuple have same key
                 if(!results.contains(tuple)) {
                     results.add(tuple);
                 }
             }
 
             Collections.sort(results);
             results = results.subList(0, Math.min(maxResults, results.size()));
 
             sortedEdgesDistances = GeocellUtils.distanceSortedEdges(curGeocells, center);
 
             if(results.size() == 0 || curGeocells.size() == 4) {
                 /* Either no results (in which case we optimize by not looking at
                         adjacents, go straight to the parent) or we've searched 4 adjacent
                         geocells, in which case we should now search the parents of those
                         geocells.*/
                 curContainingGeocell = curContainingGeocell.substring(0, Math.max(curContainingGeocell.length() - 1,0));
                 if(curContainingGeocell.length() == 0) {
                     break;  // Done with search, we've searched everywhere.
                 }
                 List<String> oldCurGeocells = new ArrayList<String>(curGeocells);
                 curGeocells.clear();
                 for(String cell : oldCurGeocells) {
                     if(cell.length() > 0) {
                         String newCell = cell.substring(0, cell.length() - 1);
                         if(!curGeocells.contains(newCell)) {
                             curGeocells.add(newCell);
                         }
                     }
                 }
                 if(curGeocells.size() == 0) {
                     break;  // Done with search, we've searched everywhere.
                 }
             } else if(curGeocells.size() == 1) {
                 // Get adjacent in one direction.
                 // TODO(romannurik): Watch for +/- 90 degree latitude edge case geocells.
                 int nearestEdge[] = sortedEdgesDistances.get(0).getFirst();
                 curGeocells.add(GeocellUtils.adjacent(curGeocells.get(0), nearestEdge));
             } else if(curGeocells.size() == 2) {
                 // Get adjacents in perpendicular direction.
                 int nearestEdge[] = GeocellUtils.distanceSortedEdges(Arrays.asList(curContainingGeocell), center).get(0).getFirst();
                 int[] perpendicularNearestEdge = {0,0};
                 if(nearestEdge[0] == 0) {
                     // Was vertical, perpendicular is horizontal.
                     for(Tuple<int[], Double> edgeDistance : sortedEdgesDistances) {
                         if(edgeDistance.getFirst()[0] != 0) {
                             perpendicularNearestEdge = edgeDistance.getFirst();
                             break;
                         }
                     }
                 } else {
                     // Was horizontal, perpendicular is vertical.
                     for(Tuple<int[], Double> edgeDistance : sortedEdgesDistances) {
                         if(edgeDistance.getFirst()[0] == 0) {
                             perpendicularNearestEdge = edgeDistance.getFirst();
                             break;
                         }
                     }
                 }
                 List<String> tempCells = new ArrayList<String>();
                 for(String cell : curGeocells) {
                     tempCells.add(GeocellUtils.adjacent(cell, perpendicularNearestEdge));
                 }
                 curGeocells.addAll(tempCells);
             }
 
             // We don't have enough items yet, keep searching.
             if(results.size() < maxResults) {
                 logger.log(Level.FINE,  results.size()+" results found but want "+maxResults+" results, continuing search.");
                 continue;
             }
 
             logger.log(Level.FINE, results.size()+" results found.");
 
             // If the currently max_results'th closest item is closer than any
             // of the next test geocells, we're done searching.
             double currentFarthestReturnableResultDist = GeocellUtils.distance(center, results.get(maxResults - 1).getFirst().getLocation());
             if (closestPossibleNextResultDist >=
                 currentFarthestReturnableResultDist) {
                 logger.log(Level.FINE, "DONE next result at least "+closestPossibleNextResultDist+" away, current farthest is "+currentFarthestReturnableResultDist+" dist");
                 break;
             }
             logger.log(Level.FINE, "next result at least "+closestPossibleNextResultDist+" away, current farthest is "+currentFarthestReturnableResultDist+" dist");
         }
         List<T> result = new ArrayList<T>();
         for(Tuple<T, Double> entry : results.subList(0, Math.min(maxResults, results.size()))) {
             if(maxDistance == 0 || entry.getSecond() < maxDistance) {
                 result.add(entry.getFirst());
             }
         }
         logger.log(Level.INFO, "Proximity query looked in "+ searchedCells.size() +" geocells and found "+result.size()+" results.");
         return result;
     }
 
     /**
      *
      * See javadoc of method with parameter maxResolution.
      * Use MAX_GEOCELL_RESOLUTION as a starting resolution.
      *
      */
     public static final <T extends LocationCapable> List<T> proximityFetch(Point center, int maxResults, double maxDistance, Class<T> entityClass, GeocellQuery baseQuery, PersistenceManager pm) {
         return proximityFetch(center, maxResults, maxDistance, entityClass, baseQuery, pm, MAX_GEOCELL_RESOLUTION);
     }
 
 }
