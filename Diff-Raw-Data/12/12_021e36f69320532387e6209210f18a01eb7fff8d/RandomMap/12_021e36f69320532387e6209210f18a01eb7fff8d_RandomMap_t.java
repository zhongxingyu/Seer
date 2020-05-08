 package random;
 
 // Libraries
 import scenario.*;
 import arena.Terrain;
 import arena.Appearence;
 import stackable.item.*;
 
 /**
  * <b>Random Map Generator</b><br>
  * Generate an hexagonal matrix with 
  * an specified theme, with dimensions
  * side x side and a 'Weather' style.
  * @see arena.World
  * @see gui
  *
  * @author Vinicius Silva
  */
 public class RandomMap
 {
     Weather     style;
     int         nPlayer;
     int         side;
     char[][]    matrix;
         
     /**
      * Default constructor.<br>
      * Construct a RandomMap object, accordingly with the 
      * theme chosen and the size of the map.
      * 
      * @param style   Weather of the map
      * @param nPlayer Number of players
      * @param side    Size of the side of the 
      *                matrix
     * @see Weather
      */
     public RandomMap(Weather style, int nPlayer, int side)
     {
         this.style = style;
         this.nPlayer = nPlayer;
         this.side = side;
         Theme t = null;
         if      (style == Weather.ARTICAL)  t = new Winter();
         else if (style == Weather.TROPICAL) t = new Jungle();
         else if (style == Weather.DESERTIC) t = new Desert();
         else                                t = new CalmField();
         this.matrix = t.generateMatrix(this.side);        
     }
     
     /**
      * Creates a random terrain matrix accordingly 
      * to the theme, items and scenarios.
      * @return Random terrain matrix
      */
     public Terrain[][] generateMap()
     {
         Terrain[][] map = new Terrain[this.side][this.side];
         
         Appearence a = null;
         
         if      (style == Weather.ARTICAL)  a = Appearence.TUNDRA;
         else if (style == Weather.TROPICAL) a = Appearence.GRASS;
         else if (style == Weather.DESERTIC) a = Appearence.DIRT; 
         else                                a = Appearence.GRASS;
         
         for(int i = 0; i < this.side; i++)
             for(int j = 0; j < this.side; j++)
             {
                 switch (this.matrix[i][j])
                 {
                     case ' '     : map[i][j] = new Terrain(this.nPlayer, Appearence.GRASS);                             break;
                     case '.'     : map[i][j] = new Terrain(this.nPlayer, Appearence.TUNDRA);                            break;
                     case ':'     : map[i][j] = new Terrain(this.nPlayer, Appearence.DIRT);                              break;
                     case '#'     : map[i][j] = new Terrain(this.nPlayer, Appearence.ICE);                               break;
                     case '≈'     : map[i][j] = new Terrain(this.nPlayer, Appearence.WATER);                             break;
                     case 'S'     : map[i][j] = new Terrain(this.nPlayer, Appearence.SAND) ;                             break;         
                     case '~'     : map[i][j] = new Terrain(this.nPlayer, Appearence.WATER , new Water());               break;
                     case '\u2662': map[i][j] = new Terrain(this.nPlayer, a                , new Crystal());             break;
                     case '$'     : map[i][j] = new Terrain(this.nPlayer, Appearence.SAND  , new Crystal());             break;
                     case '\u2663': map[i][j] = new Terrain(this.nPlayer, a                , new Tree());                break;
                     case 'O'     : map[i][j] = new Terrain(this.nPlayer, a                , new Rock());                break;
                     case '*'     : map[i][j] = new Terrain(this.nPlayer, a                , new Stone());               break;
                     case '§'     : map[i][j] = new Terrain(this.nPlayer, Appearence.SAND  , new Stone());               break;
                     case 'B'     : map[i][j] = new Terrain(this.nPlayer, a                , new Base());                break;    
                     case 'Y'     : map[i][j] = new Terrain(this.nPlayer, a                , new Tree(), new Crystal()); break;
                     case '@'     : map[i][j] = new Terrain(this.nPlayer, a                , new Rock(), new Crystal()); break;
                     case '©'     : map[i][j] = new Terrain(this.nPlayer, Appearence.SAND  , new Rock(), new Crystal()); break;
                     case 'õ'     : map[i][j] = new Terrain(this.nPlayer, Appearence.WATER , new Crystal());             break;
                     default      : map[i][j] = new Terrain(this.nPlayer, Appearence.GRASS);                             break;
                 }
             }
         return map;
     }
     
     /**
      * This method returns a matrix of characters,
      * with debug purposes.
      * @return Matrix of characters representing
      *         the entire map.
      */
     public char[][] getMatrix()
     {   
         return this.matrix;
     }
 }
