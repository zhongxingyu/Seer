 /*TODO: 
   Find a way to return the face and what not in a way that makes sense so 
   * that it can be removed from the deck test and still retain functionality.
   */
 package practiceUno; 
   
 import static java.lang.System.out; 
 
 /**
  *
  * @author mike
  */
 public class Card
 {
     /**
      *
      */
     public  enum cardColor {
         /**
          *
          */
         BLANK,
         /**
          *
          */
         BLUE,
         /**
          *
          */
         RED,
         /**
          *
          */
         GREEN,
         /**
          *
          */
         YELLOW}; 
     /**
      *
      */
     public  enum cardFace {
         /**
          *
          */
         BLANK,
         /**
          *
          */
         ZERO,
         /**
          *
          */
         ONE,
         /**
          *
          */
         TWO,
         /**
          *
          */
         THREE,
         /**
          *
          */
         FOUR,
         /**
          *
          */
         FIVE,
         /**
          *
          */
         SIX,
         /**
          *
          */
         SEVEN,
         /**
          *
          */
         EIGHT,
         /**
          *
          */
         NINE}
     /**
      *
      */
     public  enum cardSpecial {
         /**
          *
          */
         BLANK,
         /**
          *
          */
         REVERSE,
         /**
          *
          */
         SKIP,
         /**
          *
          */
         DRTWO}
     /**
      *
      */
     public  enum cardWild {
         /**
          *
          */
         BLANK,
         /**
          *
          */
         WILD,
         /**
          *
          */
         WILDDRFOUR};
  
     private String sideEffect; 
     private cardFace face;
     private cardColor color; 
     private cardSpecial spec;
     private cardWild wild; 
 
     /**
     *  TODO: Fix the print method
      */
     public void Print()
     {
 	if(!face.equals(cardFace.BLANK))
		out.print(face.ordinal() + " ");
 
 	if(!color.equals(cardColor.BLANK))
		out.print(color.toString().charAt(0) + " ");
             
 
 	if(!spec.equals(cardSpecial.BLANK))
 	        out.print(color + " ");
 		 
 	if(!wild.equals(cardWild.BLANK))
 	    out.print(wild + " " );
 
 	out.print(""); 
     }
    
 
     /**
      *
      * @param newColor
      * @param newFace
      * @param newSide
      */
     public Card(cardColor newColor, cardFace newFace, String newSide)
     {
 	face = newFace; 
 	sideEffect = newSide;
 	spec = cardSpecial.BLANK; 
 	wild = cardWild.BLANK; 
 	color = newColor;
     }
 
     /**
      *
      * @param c
      * @param s
      * @param newSide
      */
     public Card(cardColor c, cardSpecial s, String newSide)
     {
 	face = cardFace.BLANK; 
 	color = c;
 	spec = s; 
 	wild = cardWild.BLANK; 
 	sideEffect = newSide; 
     }
 
     /**
      *
      * @param w
      * @param newSide
      */
     public Card(cardWild w, String newSide)
     {
 	face = cardFace.BLANK; 
 	color = cardColor.BLANK; 
 	spec = cardSpecial.BLANK;  
 	wild = w; 
 	sideEffect = newSide; 
     }
 
     
 }
