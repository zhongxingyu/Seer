 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.metatile;
 
 import com.vividsolutions.jts.geom.Envelope;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.geotools.util.SoftValueHashMap;
 import org.geotools.util.WeakHashSet;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Set;
 import javax.servlet.http.HttpServletRequest;
 
 
 public class QuickTileCache {
     /**
      * Set of parameters that we can ignore, since they do not define a map, are
      * either unrelated, or define the tiling instead
      */
     private static final Set ignoredParameters;
 
     static {
         ignoredParameters = new HashSet();
         ignoredParameters.add("REQUEST");
         ignoredParameters.add("TILED");
         ignoredParameters.add("BBOX");
         ignoredParameters.add("WIDTH");
         ignoredParameters.add("HEIGHT");
         ignoredParameters.add("SERVICE");
         ignoredParameters.add("VERSION");
         ignoredParameters.add("EXCEPTIONS");
     }
 
     /**
      * The time to live for the tiles in cache, in milliseconds. When this time expires,
      * the tiles in the cache will be considered stale
      */
     public static final long TIME_TO_LIVE = 5000;
 
     /**
      * Canonicalizer used to return the same object when two threads ask for the
      * same meta-tile
      */
     private WeakHashSet metaTileKeys = new WeakHashSet();
     private SoftValueHashMap tileCache = new SoftValueHashMap(0);
 
     /**
      * Given a tiled request, builds a key that can be used to access the cache
      * looking for a specific meta-tile, and also as a synchronization tool to
      * avoid multiple requests to trigger parallel computation of the same
      * meta-tile
      *
      * @param request
      * @return
      */
     public MetaTileKey getMetaTileKey(GetMapRequest request) {
         String mapDefinition = buildMapDefinition(request.getHttpServletRequest());
         Envelope bbox = request.getBbox();
         Point2D origin = request.getTilesOrigin();
         MapKey mapKey = new MapKey(mapDefinition, normalize(bbox.getWidth() / request.getWidth()),
                 origin);
         Point tileCoords = getTileCoordinates(bbox, origin);
         Point metaTileCoords = getMetaTileCoordinates(tileCoords);
         MetaTileKey key = new MetaTileKey(mapKey, metaTileCoords);
 
         // since this will be used for thread synchronization, we have to make
         // sure two thread asking for the same meta tile will get the same key
         // object
         return (MetaTileKey) metaTileKeys.canonicalize(key);
     }
 
     /**
      * Given a tile, returns the coordinates of the meta-tile that contains it
      * (where the meta-tile coordinate is the coordinate of its lower left
      * subtile)
      *
      * @param tileCoords
      * @return
      */
     Point getMetaTileCoordinates(Point tileCoords) {
         int x = tileCoords.x;
         int y = tileCoords.y;
         int rx = x % 3;
         int ry = y % 3;
         int mtx = (rx == 0) ? x : ((x >= 0) ? (x - rx) : (x - 3 - rx));
         int mty = (ry == 0) ? y : ((y >= 0) ? (y - ry) : (y - 3 - ry));
 
         return new Point(mtx, mty);
     }
 
     /**
      * Given an envelope and origin, find the tile coordinate (row,col)
      *
      * @param env
      * @param origin
      * @return
      */
     Point getTileCoordinates(Envelope env, Point2D origin) {
        // this was using the low left corner and Math.round, but turned
        // out to be fragile when fairly zoomed in. Using the tile center
        // and then flooring the division seems to work much more reliably.
        double centerx = env.getMinX() + env.getWidth() / 2;
        double centery = env.getMinY() + env.getHeight() / 2;
        int x = (int) Math.floor((centerx - origin.getX()) / env.getWidth());
        int y = (int) Math.floor((centery - origin.getY()) / env.getWidth());
 
         return new Point(x, y);
     }
 
     /**
      * This is tricky. We need to have doubles that can be compared by equality
      * because resolution and origin are doubles, and are part of a hashmap key,
      * so we have to normalize them somehow, in order to make the little
      * differences disappear. Here we take the mantissa, which is made of 52
      * bits, and throw away the 20 more significant ones, which means we're
      * dealing with 12 significant decimal digits (2^40 -> more or less one
      * billion million). See also <a
      * href="http://en.wikipedia.org/wiki/IEEE_754">IEEE 754</a> on Wikipedia.
      *
      * @param d
      * @return
      */
     static double normalize(double d) {
         if (Double.isInfinite(d) || Double.isNaN(d)) {
             return d;
         }
 
         return Math.round(d * 10e6) / 10e6;
     }
 
     /**
      * Turns the request back into a sort of GET request (not url-encoded) for
      * fast comparison
      *
      * @param request
      * @return
      */
     private String buildMapDefinition(HttpServletRequest request) {
         StringBuffer sb = new StringBuffer();
         Enumeration en = request.getParameterNames();
 
         while (en.hasMoreElements()) {
             String paramName = (String) en.nextElement();
 
             if (ignoredParameters.contains(paramName.toUpperCase())) {
                 continue;
             }
 
             // we don't have multi-valued parameters afaik, otherwise we would
             // have to use getParameterValues and deal with the returned array
             sb.append(paramName).append('=').append(request.getParameter(paramName));
 
             if (en.hasMoreElements()) {
                 sb.append('&');
             }
         }
 
         return sb.toString();
     }
 
     /**
      * Key defining a tiling layer in a map
      */
     static class MapKey {
         String mapDefinition;
         double resolution;
         Point2D origin;
 
         public MapKey(String mapDefinition, double resolution, Point2D origin) {
             super();
             this.mapDefinition = mapDefinition;
             this.resolution = resolution;
             this.origin = origin;
         }
 
         public int hashCode() {
             return new HashCodeBuilder().append(mapDefinition).append(resolution).append(resolution)
                                         .append(origin).toHashCode();
         }
 
         public boolean equals(Object obj) {
             if (!(obj instanceof MapKey)) {
                 return false;
             }
 
             MapKey other = (MapKey) obj;
 
             return new EqualsBuilder().append(mapDefinition, other.mapDefinition)
                                       .append(resolution, other.resolution)
                                       .append(origin, other.origin).isEquals();
         }
 
         public String toString() {
             return mapDefinition + "\nw:" + "\nresolution:" + resolution + "\norig:"
             + origin.getX() + "," + origin.getY();
         }
     }
 
     /**
      * Key that identifies a certain meta-tile in a tiled map layer
      */
     static class MetaTileKey {
         MapKey mapKey;
         Point metaTileCoords;
 
         public MetaTileKey(MapKey mapKey, Point metaTileCoords) {
             super();
             this.mapKey = mapKey;
             this.metaTileCoords = metaTileCoords;
         }
 
         public Envelope getMetaTileEnvelope() {
             double minx = mapKey.origin.getX() + (mapKey.resolution * 256 * metaTileCoords.x);
             double miny = mapKey.origin.getY() + (mapKey.resolution * 256 * metaTileCoords.y);
 
             return new Envelope(minx, minx + (mapKey.resolution * 256 * 3), miny,
                 miny + (mapKey.resolution * 256 * 3));
         }
 
         public int hashCode() {
             return new HashCodeBuilder().append(mapKey).append(metaTileCoords).toHashCode();
         }
 
         public boolean equals(Object obj) {
             if (!(obj instanceof MetaTileKey)) {
                 return false;
             }
 
             MetaTileKey other = (MetaTileKey) obj;
 
             return new EqualsBuilder().append(mapKey, other.mapKey)
                                       .append(metaTileCoords, other.metaTileCoords).isEquals();
         }
 
         public int getMetaFactor() {
             return 3;
         }
 
         public int getTileSize() {
             return 256;
         }
 
         public String toString() {
             return mapKey + "\nmtc:" + metaTileCoords.x + "," + metaTileCoords.y;
         }
     }
 
     /**
      * Gathers a tile from the cache, if available
      * @param key
      * @param request
      * @return
      */
     public synchronized BufferedImage getTile(MetaTileKey key, GetMapRequest request) {
         CacheElement ce = (CacheElement) tileCache.get(key);
 
         if (ce == null) {
             return null;
         }
 
         if (ce.isExpired()) {
             tileCache.remove(key);
 
             return null;
         }
 
         return getTile(key, request, ce.tiles);
     }
 
     /**
      *
      * @param key
      * @param request
      * @param tiles
      * @return
      */
     public BufferedImage getTile(MetaTileKey key, GetMapRequest request, BufferedImage[] tiles) {
         Point tileCoord = getTileCoordinates(request.getBbox(), key.mapKey.origin);
         Point metaCoord = key.metaTileCoords;
 
         return tiles[tileCoord.x - metaCoord.x
         + ((tileCoord.y - metaCoord.y) * key.getMetaFactor())];
     }
 
     /**
      * Puts the specified tile array in the cache, and returns the tile the request was looking for
      * @param key
      * @param request
      * @param tiles
      * @return
      */
     public synchronized void storeTiles(MetaTileKey key, BufferedImage[] tiles) {
         tileCache.put(key, new CacheElement(tiles));
     }
 
     class CacheElement {
         BufferedImage[] tiles;
         long timeCached;
 
         public CacheElement(BufferedImage[] tiles) {
             this.tiles = tiles;
             this.timeCached = System.currentTimeMillis();
         }
 
         public boolean isExpired() {
             return (System.currentTimeMillis() - timeCached) > TIME_TO_LIVE;
         }
     }
 }
