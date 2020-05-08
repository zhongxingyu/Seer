 /* Draw_Pile.java
  * Purpose: Draw_Pile is the class that will be an implementation of the stack of cards that the user will thumb through
  *          to choose cards from and place onto the playing field.
  */
 
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.Color;
 
 public class Draw_Pile extends Piles{
     
     
     Draw_Pile(int size, int x_of_up, int y_of_up, int x_of_down, int y_of_down) {
         super(size, size, x_of_up, y_of_up, x_of_down, y_of_down);
     }
     
     /* void draw_three()
      * Purpse: draw_three() is the function which the draw pile uses to draw the top three cards from the face-down draw pile
      *         and transfers it over to the face up pile.
      * 
      * Procedure: This function will just pop three cards off the top of the face-down and push them onto the face-up pile.
      */
     
     public void draw_three(  ) {
         if ( isEmpty('d') && isEmpty('u') ) {
             //Play sound to notify user that there are no more cards for either piles.
         }
         
         //This case is for if all the cards are in the face-up pile in which they need to be transferred to the face-down pile.
         else if ( isEmpty('d') ) {
             int check = capac_of_up;
             
             for( int i = 0; i < check; i++ ) {
                 push( pop('u'), 'd' );
             }
         }
         
         else {
             int check;
             if( capac_of_down >= 3 )
                 check = 3;
             else
                 check = capac_of_down;
 
             for (int i = 0; i < check; i++){
                 push( pop('d'), 'u' );              
             }    
         }
     }
     
     @Override
     public void draw() {
         Color.white.bind();
         
         if (isEmpty('u') && isEmpty('d')) {
             blank.bind();
 
             GL11.glBegin(GL11.GL_QUADS);
                 GL11.glTexCoord2f(0, 0);
                 GL11.glVertex2f((float) coords_of_up[0], (float) coords_of_up[1]);
                 GL11.glTexCoord2f(0.75f, 0);
                 GL11.glVertex2f((float) coords_of_up[0] + blank.getTextureWidth(), (float) coords_of_up[1]);
                 GL11.glTexCoord2f(0.75f, 0.95f);
                 GL11.glVertex2f((float) coords_of_up[0] + blank.getTextureWidth(), (float) coords_of_up[1] + blank.getTextureHeight());
                 GL11.glTexCoord2f(0, 0.95f);
                 GL11.glVertex2f((float) coords_of_up[0], (float) coords_of_up[1] + blank.getTextureHeight());
             GL11.glEnd();
 
             GL11.glBegin(GL11.GL_QUADS);
                 GL11.glTexCoord2f(0, 0);
                 GL11.glVertex2f((float) coords_of_down[0], (float) coords_of_down[1]);
                 GL11.glTexCoord2f(0.75f, 0);
                 GL11.glVertex2f((float) coords_of_down[0] + blank.getTextureWidth(), (float) coords_of_down[1]);
                 GL11.glTexCoord2f(0.75f, 0.95f);
                 GL11.glVertex2f((float) coords_of_down[0] + blank.getTextureWidth(), (float) coords_of_down[1] + blank.getTextureHeight());
                 GL11.glTexCoord2f(0, 0.95f);
                 GL11.glVertex2f((float) coords_of_down[0], (float) coords_of_down[1] + blank.getTextureHeight());
             GL11.glEnd();
         }
         
         else if (isEmpty('u')) {
             back.bind();
 
             GL11.glBegin(GL11.GL_QUADS);
                 GL11.glTexCoord2f(0, 0);
                 GL11.glVertex2f((float) coords_of_down[0], (float) coords_of_down[1]);
                 GL11.glTexCoord2f(0.75f, 0);
                 GL11.glVertex2f((float) coords_of_down[0] + back.getTextureWidth(), (float) coords_of_down[1]);
                 GL11.glTexCoord2f(0.75f, 0.95f);
                 GL11.glVertex2f((float) coords_of_down[0] + back.getTextureWidth(), (float) coords_of_down[1] + back.getTextureHeight());
                 GL11.glTexCoord2f(0, 0.95f);
                 GL11.glVertex2f((float) coords_of_down[0], (float) coords_of_down[1] + back.getTextureHeight());
             GL11.glEnd();
             
             
             blank.bind();
             
             GL11.glBegin(GL11.GL_QUADS);
                 GL11.glTexCoord2f(0, 0);
                 GL11.glVertex2f((float) coords_of_up[0], (float) coords_of_up[1]);
                 GL11.glTexCoord2f(0.75f, 0);
                 GL11.glVertex2f((float) coords_of_up[0] + blank.getTextureWidth(), (float) coords_of_up[1]);
                 GL11.glTexCoord2f(0.75f, 0.95f);
                 GL11.glVertex2f((float) coords_of_up[0] + blank.getTextureWidth(), (float) coords_of_up[1] + blank.getTextureHeight());
                 GL11.glTexCoord2f(0, 0.95f);
                 GL11.glVertex2f((float) coords_of_up[0], (float) coords_of_up[1] + blank.getTextureHeight());
             GL11.glEnd();
         }
         
         else {
            /* Loop used later for if the up pile has more than one card on it.
            for(int i = 0; i < capac_of_up; i++) {
                
            } 
            */
            this.up[top_of_up].setX(coords_of_up[0]);
            this.up[top_of_up].setY(coords_of_up[1]);
             
            this.up[top_of_up].draw();
         }
         
     }
 }
