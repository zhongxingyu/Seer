 /*
  * Catcher
  *
  * License: GPL v3
  * Authors: richard_jonsson@hotmail.com, tommyc@lavabit.com
  */
 package Maps;
 
 import System.IImageLoader;
 import System.Position;
 import System.IMapProvider;
 import System.MathUtil;
 import System.StringUtils;
 
 /**
  * Spherical mercator (Google style) tiled map with 256x256px tiles.
  *
  * References:
  * http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
  *
  * Known bugs:
  * Doesn't handle wrapping around longitude -180/179
  */
 public class MercatorMap implements IMapProvider {
     private IImageLoader imageLoader = null;
 
     private static final int NOF_CACHED_TILES = 16;
 
     // We could set this to 0 and see the entire world, but what use is it?
     private static final int ZOOM_MIN = 6;
 
     // Depending on map source, this value varies. 14 would be safe for most
     // maps.
     private static final int ZOOM_MAX = 20;
 
     // fixme: OSM's mapnik maps are hardcoded, and these don't belong here!
     // fixme: review http://wiki.openstreetmap.org/wiki/Tile_usage_policy
     private static final String mapSource=
             "http://tile.openstreetmap.org/[Z]/[X]/[Y].png";
     private static final String mapID="osm_mapnik";
 
     // fixme: add format detection
     private static final String mapTileFormat=".png";
 
     /*
      * source is an URL typically in the form
      * http://maps.url/?x=[X]&y=[Y]&z=[Z] or
      * http://maps.url/[Z]/[X]/[Y].png
      *
      * [X], [Y] and [Z] are case sensitive, and must all be in the string
      *
      * An other client uses [INVZ] which in fact is NOT inverted. URLs formatted
      * for that client needs to drop "INV" for those URLs to work with Catcher.
      *
      * Returns true if map source is valid.
      * Note that this function does not check if the maps are available or even
      * if the domain exists.
      */
     public boolean isValidMapSource(String source, String id) {
         // We have this input validation here in case the settings file is
         // altered with an external tool. It is assumed the strings are != null.
         if ((source.indexOf("[X]")>0)
                 && (source.indexOf("[Y]")>0)
                 && (source.indexOf("[Z]")>0)
                 && (source.indexOf("http")==0) // Note that https is allowed too
                 && (id.length() > 0)) {
             return true;
         }
         return false;
     }
 
     public MercatorMap(IImageLoader imageLoader) {
         this.imageLoader = imageLoader;
     }
 
     public int zoomIn(int zoom) {
         return (zoom<=ZOOM_MAX? ++zoom:zoom);
     }
 
     public int zoomOut(int zoom) {
         return (zoom>=ZOOM_MIN? --zoom:zoom);
     }
 
     public Position XYtoPosition(int x, int y, Position center, int mapWidth,
             int mapHeight, int zoom) {
         int[] mapTileX = tileX(center.getLon(), zoom);
         int[] mapTileY = tileY(center.getLat(), zoom);
 
         int xPos = (mapTileX[0]<<8)+mapTileX[1]-(mapWidth>>1)+x;
         int yPos = (mapTileY[0]<<8)+mapTileY[1]-(mapHeight>>1)+y;
 
         return new Position(yToLat(yPos, zoom), xToLon(xPos, zoom));
     }
 
     public int[] positionToXY(Position position, Position center, int mapWidth,
             int mapHeight, int zoom) {
         int[] mapTileX = tileX(center.getLon(), zoom);
         int[] mapTileY = tileY(center.getLat(), zoom);
 
         int[] tileX = tileX(position.getLon(), zoom);
         int[] tileY = tileY(position.getLat(), zoom);
 
         int pX = (tileX[0]<<8)+tileX[1];
         int pY = (tileY[0]<<8)+tileY[1];
 
         int mapX = (mapTileX[0]<<8)+mapTileX[1];
         int mapY = (mapTileY[0]<<8)+mapTileY[1];
 
         int retX = pX-mapX+(mapWidth>>1);
         int retY = pY-mapY+(mapHeight>>1);
 
         int[] ret = {retX, retY};
         return ret;
     }
 
     /*
      * fixme: Prioritize tile loading order, load from center
      * 
      * This could be done by calculating each tile's center position and
      * ordering by distance to map center. Optimize by offsetting map center
      * position by a half tile instead.
      */
     public Object getMap(Position center, int width, int height, int zoom) {
 
         // Get center tile position and calculate the rest from there.
         int[] mapTileX = tileX(center.getLon(), zoom);
         int[] mapTileY = tileY(center.getLat(), zoom);
 
         // offset in map to center tile
         int ctx = (width >> 1)-mapTileX[1];
         int cty = (height >> 1)-mapTileY[1];
 
         /*
          * Find tile arrangement
          *
          * case1 tx1=57 width=240
          * 57+255 >> 3 = 312 >> 8 = 1
          * 240-57 >> 3 = 183 >> 8 = 0
          * case2 tx1=-3
          * -3+255 >> 8 = 252 >> 8 = 0
          * 240-3 >> 8 = 237 >> 8 = 0
          * case3 tx1=0 width=256
          * 0+255 >> 8 = 0
          * 255-0 >> 8 = 0
          * case4 tx1=1 width=256
          * 1+255 >> 8 = 1
          * 255-1 >> 8 = 0
          * case5 tx1=-1 width=256
          * -1+255 >> 8 = 0
          * 255--1 >> 8 = 1
         */
         int tilesLeft = (ctx+255) >> 8;
         int tilesAbove = (cty+255) >> 8;
 
         int tilesRight = (width-1-ctx) >> 8;
         int tilesBelow = (height-1-cty) >> 8;
 
         int nofTilesX = tilesLeft+tilesRight+1;
         int nofTilesY = tilesAbove+tilesBelow+1;
 
         // offset of topleftmost tile.
         int firstX = ctx-(tilesLeft<<8);
         int firstY = cty-(tilesAbove<<8);
         int firstTileX = mapTileX[0]-tilesLeft;
         int firstTileY = mapTileY[0]-tilesAbove;
 
         Object map = imageLoader.createImage(width, height);
         Object imTile = null;
         for (int y=0; y<nofTilesY; y++) {
             for (int x=0; x<nofTilesX; x++) {
                 imTile = getTile(firstTileX+x, firstTileY+y, zoom);
                map = imageLoader.drawImage(map, imTile, firstX+(x<<8),
                        firstY+(y<<8));
             }
         }
         return map;
     }
 
     /*
      * Returns a string url
      */
     private String getTileURL(int tileX, int tileY, int tileZ) {
         String s = new String(mapSource);
         s = StringUtils.replace(s, "[X]", String.valueOf(tileX));
         s = StringUtils.replace(s, "[Y]", String.valueOf(tileY));
         s = StringUtils.replace(s, "[Z]", String.valueOf(tileZ));
         return s;
     }
 
     /*
      * Returns the path to the local tile.
      * It's the callers responsibility to check if the tile exists.
      */
     private String getTilePath(int tileX, int tileY, int tileZ) {
         return mapID+"/"+String.valueOf(tileZ)
                 +"/"+String.valueOf(tileX)
                 +"/"+String.valueOf(tileY)+mapTileFormat;
     }
 
     /*
      * Get tile from image loader.
      */
     private Object getTile(int tileX, int tileY, int tileZ) {
         // Local storage path
         String path = getTilePath(tileX, tileY, tileZ);
         // http url
         String url = getTileURL(tileX, tileY, tileZ);
         Object imTile = imageLoader.httpLoad(url, path, NOF_CACHED_TILES);
 
         return imTile;
     }
 
     /*
      * Calculate tile x from longitude.
      * Returns { tile, offset in tile }
      * (verified)
      */
     private int[] tileX(double lon, int zoom) {
         int tileX = (int)((lon + 180) / 360 * (1 << (zoom+8)));
         int[] ret = {tileX >> 8, tileX & 0xff};
         return ret;
     }
 
     /*
      * Calculate tile y from latitude.
      * Returns { tile, offset in tile }
      * (verified)
      */
     private int[] tileY(double lat, int zoom) {
         lat = lat*Math.PI/180;
         int tileY = (int)((1 - MathUtil.log(Math.tan(lat)+1/Math.cos(lat)) /
                 Math.PI) / 2 * (1 << (zoom+8)));
 
         int[] ret = {tileY >> 8, tileY & 0xff};
         return ret;
     }
 
     /*
      * Returns longitude for given tile.tileoffs X value
      * x is a fixed point int n.8 (8 bits fraction)
      *
      * fixme: optimize
      */
     private double xToLon(int x, int zoom) {
         double dx = (double)x/256;
         return dx / (1 << zoom) * 360 - 180;
     }
 
     /*
      * Returns latitude for given tile+tileoffs Y value
      * y is a fixed point int n.8 (8 bits fraction)
      * 
      * fixme: optimize
      */
     private double yToLat(int y, int zoom) {
         double dy = (double)y/256;
         double n = Math.PI - 2 * Math.PI * dy / (1 << zoom);
         return 180 / Math.PI * MathUtil.atan(0.5 * (MathUtil.exp(n) -
                 MathUtil.exp(-n)));
     }
 }
