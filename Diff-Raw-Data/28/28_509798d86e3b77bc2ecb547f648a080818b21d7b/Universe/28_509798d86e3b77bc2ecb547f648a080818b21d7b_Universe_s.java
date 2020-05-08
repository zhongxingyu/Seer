 /**
  * @author MetaGalactic Merchants
  * @version 1.0
  * 
  */
 
 package edu.gatech.cs2340.group29.spacemerchant.model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Random;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Point;
 import edu.gatech.cs2340.group29.spacemerchant.R;
 
 /**
  * The Class Universe.
  */
 public class Universe
 {
     private ArrayList<Point>  planet_xy;
     private ArrayList<String> planet_name;
     private ArrayList<Planet> universe;
     
     private int               width;
     private int               height;
     
     private int               difficulty;
     private Context           context;
     
     /**
      * Instantiates a new universe.
      *
      * @param difficulty the int
      * @param context the Context
      */
     public Universe( int difficulty, Context context )
     {
         this.difficulty = difficulty;
         this.context = context;
     }
     
     /**
      * Generate planets.
      *
      * @return the array list
      */
     public ArrayList<Planet> generatePlanets()
     {
         // 12^2 < 120 (# of planets) with some room to breathe,
         // a.k.a. the smallest universe size
         width = 12 * difficulty;
         height = 12 * difficulty;
         Resources res = context.getResources();
         planet_name = new ArrayList<String>( Arrays.asList( res.getStringArray( R.array.planets ) ) );
         
         Random r = new Random();
         while ( planet_xy.size() < 120 )
         {
             int x = r.nextInt( width + 1 );
             int y = r.nextInt( height + 1 );
             
             Point p = new Point( x, y );
             planet_xy.add( p );
             // remove duplicates
             HashSet<Point> hs = new HashSet<Point>();
             hs.addAll( planet_xy );
             planet_xy.clear();
             planet_xy.addAll( hs );
         }
         
         for ( int i = 0; i < planet_name.size(); i++ )
         {
             universe.add( new Planet( planet_name.get( i ), ( ( Point ) planet_xy.get( i ) ).x,
                     ( ( Point ) planet_xy.get( i ) ).y ) );
         }
         
         return universe;
     }
     
     /**
      * Override:
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString()
     {
         return universe.toString();
     }
     
     /**
      * Gets the planet_xy.
      *
      * @return the planet_xy
      */
     public ArrayList<Point> getPlanet_xy()
     {
         return planet_xy;
     }
     
     /**
      * Sets the planet_xy.
      *
      * @param planet_xy the new planet_xy
      */
     public void setPlanet_xy( ArrayList<Point> planet_xy )
     {
         this.planet_xy = planet_xy;
     }
     
     /**
      * Gets the planet_name.
      *
      * @return the planet_name
      */
     public ArrayList<String> getPlanet_name()
     {
         return planet_name;
     }
     
     /**
      * Sets the planet_name.
      *
      * @param planet_name the new planet_name
      */
     public void setPlanet_name( ArrayList<String> planet_name )
     {
         this.planet_name = planet_name;
     }
     
     /**
      * Gets the universe.
      *
      * @return the universe
      */
     public ArrayList<Planet> getUniverse()
     {
         return universe;
     }
     
     /**
      * Sets the universe.
      *
      * @param universe the new universe
      */
     public void setUniverse( ArrayList<Planet> universe )
     {
         this.universe = universe;
     }
     
     /**
      * Gets the width.
      *
      * @return the width
      */
     public int getWidth()
     {
         return width;
     }
     
     /**
      * Sets the width.
      *
      * @param width the new width
      */
     public void setWidth( int width )
     {
         this.width = width;
     }
     
     /**
      * Gets the height.
      *
      * @return the height
      */
     public int getHeight()
     {
         return height;
     }
     
     /**
      * Sets the height.
      *
      * @param height the new height
      */
     public void setHeight( int height )
     {
         this.height = height;
     }
 }
