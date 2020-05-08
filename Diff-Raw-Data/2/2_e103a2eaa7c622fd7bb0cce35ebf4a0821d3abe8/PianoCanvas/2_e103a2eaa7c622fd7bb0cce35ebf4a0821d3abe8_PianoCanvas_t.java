 import javax.microedition.lcdui.Canvas;
 import javax.microedition.lcdui.Graphics;
 
 /**
  * Canvas of the Piano MIDlet
  *
  * @author Wincent Balin
  */
 
 public class PianoCanvas extends Canvas
 {
     public static final int KEYS = 12;
 
     public static final int BLACK_COLOR = 0x00000000;
     public static final int WHITE_COLOR = 0x00FFFFFF;
     public static final int CONTOUR_COLOR = 0x00000000;
 
     public static final int MARGIN = 10;
     public static final int KEYS_NOTES_DISTANCE = 10;
 
     public static final boolean[] WHITE_KEY =
     {
         true,
         false,
         true,
         false,
         true,
         true,
         false,
         true,
         false,
         true,
         false,
         true
     };
 
     private boolean[] keyPressed = new boolean[KEYS];
 
     /**
      * Constructor.
      */
     PianoCanvas()
     {
         // Initialize canvas
         super();
 
         // Initialize key pressed field
         for(int i = 0; i < KEYS; i++)
             keyPressed[i] = false;
     }
 
     /**
      * Handler of the painting event.
      *
      * @param g Graphics to pain on
      */
     public void paint(Graphics g)
     {
         // Get dimentions of display to draw upon
         final int width = getWidth();
         final int height = getHeight();
 
         final int fontHeight = g.getFont().getHeight();
 
         // Calculate dimension of a key
         final int whiteKeyWidth = (width - MARGIN * 2) / 7;
         final int whiteKeyHeight = height -
                 (MARGIN + KEYS_NOTES_DISTANCE + fontHeight * 2);
         final int blackKeyWidth = whiteKeyWidth / 2; // One half
         final int blackKeyHeight = whiteKeyHeight / 2; // One half
         final int blackKeyNarrowWidth = blackKeyWidth / 2;
         final int whiteKeyNarrowWidth = whiteKeyWidth - blackKeyNarrowWidth;
         final int whiteKeyLesserHeight = whiteKeyHeight - blackKeyHeight;
 
         /* Draw background */
         g.setColor(WHITE_COLOR);
         g.fillRect(0, 0, width, height);
 
         /* Draw keys */
 
         // Draw C
         final int cx1 = 0 + MARGIN;
         final int cy1 = 0 + MARGIN;
         final int cx2 = cx1 + whiteKeyNarrowWidth;
         final int cy2 = cy1;
         final int cx3 = cx2;
         final int cy3 = cy2 + blackKeyHeight;
         final int cx4 = cx1 + whiteKeyWidth;
         final int cy4 = cy3;
         final int cx5 = cx4;
         final int cy5 = cy1 + whiteKeyHeight;
         final int cx6 = cx1;
         final int cy6 = cy5;
 
         setKeyColor(g, 0);
         g.fillRect(cx1, cy1, whiteKeyNarrowWidth, whiteKeyHeight);
         g.fillRect(cx3, cy3, blackKeyNarrowWidth, whiteKeyLesserHeight);
 
         g.setColor(CONTOUR_COLOR);
         g.drawLine(cx1, cy1, cx2, cy2);
         g.drawLine(cx2, cy2, cx3, cy3);
         g.drawLine(cx3, cy3, cx4, cy4);
         g.drawLine(cx4, cy4, cx5, cy5);
         g.drawLine(cx5, cy5, cx6, cy6);
         g.drawLine(cx6, cy6, cx1, cy1);
 
         // Draw C#
         final int ccx1 = cx2;
         final int ccy1 = cy2;
         final int ccx2 = ccx1 + blackKeyWidth;
         final int ccy2 = ccy1;
         //final int ccx3 = ccx2;
         //final int ccy3 = ccy2 + blackKeyHeight;
         //final int ccx4 = ccx1;
         //final int ccy4 = ccy3;
 
         setKeyColor(g, 1);
         g.fillRect(ccx1, ccy1, blackKeyWidth, blackKeyHeight);
 
         g.setColor(CONTOUR_COLOR);
         g.drawRect(ccx1, ccy1, blackKeyWidth, blackKeyHeight);
 
         // Draw D
         final int dx1 = ccx2;
         final int dy1 = ccy2;
         final int dx2 = dx1 + whiteKeyNarrowWidth - blackKeyNarrowWidth;
         final int dy2 = dy1;
         final int dx3 = dx2;
         final int dy3 = dy2 + blackKeyHeight;
         final int dx4 = cx4 + whiteKeyWidth;
         final int dy4 = dy3;
         final int dx5 = dx4;
         final int dy5 = dy2 + whiteKeyHeight;
         final int dx6 = cx5;
         final int dy6 = dy5;
         final int dx7 = dx6;
         final int dy7 = dy3;
         final int dx8 = dx1;
         final int dy8 = dy3;
 
         setKeyColor(g, 2);
         g.fillRect(dx1, dy1, dx2 - dx1, blackKeyHeight);
         g.fillRect(dx7, dy7, whiteKeyWidth, whiteKeyLesserHeight);
 
         g.setColor(CONTOUR_COLOR);
         g.drawLine(dx1, dy1, dx2, dy2);
         g.drawLine(dx2, dy2, dx3, dy3);
         g.drawLine(dx3, dy3, dx4, dy4);
         g.drawLine(dx4, dy4, dx5, dy5);
         g.drawLine(dx5, dy5, dx6, dy6);
         g.drawLine(dx6, dy6, dx7, dy7);
         g.drawLine(dx7, dy7, dx8, dy8);
         g.drawLine(dx8, dy8, dx1, dy1);
 
         // Draw D#
         final int ddx1 = dx2;
         final int ddy1 = dy2;
         final int ddx2 = ddx1 + blackKeyWidth;
         final int ddy2 = ddy1;
         //final int ddx3 = ddx2;
         //final int ddy3 = ddy2 + blackKeyHeight;
         //final int ddx4 = ddx1;
         //final int ddy4 = ddy3;
 
         setKeyColor(g, 3);
         g.fillRect(ddx1, ddy1, blackKeyWidth, blackKeyHeight);
 
         g.setColor(CONTOUR_COLOR);
         g.drawRect(ddx1, ddy1, blackKeyWidth, blackKeyHeight);
 
         // Draw E
         final int ex1 = ddx2;
         final int ey1 = ddy2;
         final int ex2 = dx4 + whiteKeyWidth;
         final int ey2 = ey1;
         final int ex3 = ex2;
         final int ey3 = ey2 + whiteKeyHeight;
         final int ex4 = dx5;
         final int ey4 = ey3;
         final int ex5 = ex4;
         final int ey5 = dy4;
         final int ex6 = ex5 + blackKeyNarrowWidth;
         final int ey6 = ey5;
 
         setKeyColor(g, 4);
         g.fillRect(ex1, ey1, whiteKeyNarrowWidth, blackKeyHeight);
         g.fillRect(ex5, ey5, whiteKeyWidth, whiteKeyLesserHeight);
 
         g.setColor(CONTOUR_COLOR);
         g.drawLine(ex1, ey1, ex2, ey2);
         g.drawLine(ex2, ey2, ex3, ey3);
         g.drawLine(ex3, ey3, ex4, ey4);
         g.drawLine(ex4, ey4, ex5, ey5);
         g.drawLine(ex5, ey5, ex6, ey6);
         g.drawLine(ex6, ey6, ex1, ey1);
 
         // Draw F
         final int fx1 = ex2;
         final int fy1 = ey2;
         final int fx2 = fx1 + whiteKeyNarrowWidth;
         final int fy2 = fy1;
         final int fx3 = fx2;
         final int fy3 = fy2 + blackKeyHeight;
         final int fx4 = fx1 + whiteKeyWidth;
         final int fy4 = fy3;
         final int fx5 = fx4;
         final int fy5 = fy1 + whiteKeyHeight;
         final int fx6 = fx1;
         final int fy6 = fy5;
 
         setKeyColor(g, 5);
         g.fillRect(fx1, fy1, whiteKeyNarrowWidth, whiteKeyHeight);
         g.fillRect(fx3, fy3, blackKeyNarrowWidth, whiteKeyLesserHeight);
 
         g.setColor(CONTOUR_COLOR);
         g.drawLine(fx1, fy1, fx2, fy2);
         g.drawLine(fx2, fy2, fx3, fy3);
         g.drawLine(fx3, fy3, fx4, fy4);
         g.drawLine(fx4, fy4, fx5, fy5);
         g.drawLine(fx5, fy5, fx6, fy6);
         g.drawLine(fx6, fy6, fx1, fy1);
 
         // Draw F#
         final int ffx1 = fx2;
         final int ffy1 = fy2;
         final int ffx2 = ffx1 + blackKeyWidth;
         final int ffy2 = ffy1;
         //final int ffx3 = ffx2;
         //final int ffy3 = ffy2 + blackKeyHeight;
         //final int ffx4 = ffx1;
         //final int ffy4 = ffy3;
 
         setKeyColor(g, 6);
         g.fillRect(ffx1, ffy1, blackKeyWidth, blackKeyHeight);
 
         g.setColor(CONTOUR_COLOR);
         g.drawRect(ffx1, ffy1, blackKeyWidth, blackKeyHeight);
 
         // Draw G
         final int gx1 = ffx2;
         final int gy1 = ffy2;
         final int gx2 = gx1 + whiteKeyNarrowWidth - blackKeyNarrowWidth;
         final int gy2 = gy1;
         final int gx3 = gx2;
         final int gy3 = gy2 + blackKeyHeight;
         final int gx4 = fx4 + whiteKeyWidth;
         final int gy4 = gy3;
         final int gx5 = gx4;
         final int gy5 = gy2 + whiteKeyHeight;
         final int gx6 = fx5;
         final int gy6 = gy5;
         final int gx7 = gx6;
         final int gy7 = gy3;
         final int gx8 = gx1;
         final int gy8 = gy3;
 
         setKeyColor(g, 7);
         g.fillRect(gx1, gy1, gx2 - gx1, blackKeyHeight);
         g.fillRect(gx7, gy7, whiteKeyWidth, whiteKeyLesserHeight);
 
         g.setColor(CONTOUR_COLOR);
         g.drawLine(gx1, gy1, gx2, gy2);
         g.drawLine(gx2, gy2, gx3, gy3);
         g.drawLine(gx3, gy3, gx4, gy4);
         g.drawLine(gx4, gy4, gx5, gy5);
         g.drawLine(gx5, gy5, gx6, gy6);
         g.drawLine(gx6, gy6, gx7, gy7);
         g.drawLine(gx7, gy7, gx8, gy8);
         g.drawLine(gx8, gy8, gx1, gy1);
 
         // Draw G#
         final int ggx1 = gx2;
         final int ggy1 = gy2;
         final int ggx2 = ggx1 + blackKeyWidth;
         final int ggy2 = ggy1;
         //final int ggx3 = ggx2;
         //final int ggy3 = ggy2 + blackKeyHeight;
         //final int ggx4 = ggx1;
         //final int ggy4 = ggy3;
 
         setKeyColor(g, 8);
         g.fillRect(ggx1, ggy1, blackKeyWidth, blackKeyHeight);
 
         g.setColor(CONTOUR_COLOR);
         g.drawRect(ggx1, ggy1, blackKeyWidth, blackKeyHeight);
 
         // Draw A
         final int ax1 = ggx2;
         final int ay1 = ggy2;
         final int ax2 = ax1 + whiteKeyNarrowWidth - blackKeyNarrowWidth;
         final int ay2 = ay1;
         final int ax3 = ax2;
         final int ay3 = ay2 + blackKeyHeight;
         final int ax4 = gx4 + whiteKeyWidth;
         final int ay4 = ay3;
         final int ax5 = ax4;
         final int ay5 = ay2 + whiteKeyHeight;
         final int ax6 = gx5;
         final int ay6 = ay5;
         final int ax7 = ax6;
         final int ay7 = ay3;
         final int ax8 = ax1;
         final int ay8 = ay3;
 
         setKeyColor(g, 9);
         g.fillRect(ax1, ay1, ax2 - ax1, blackKeyHeight);
         g.fillRect(ax7, ay7, whiteKeyWidth, whiteKeyLesserHeight);
 
         g.setColor(CONTOUR_COLOR);
         g.drawLine(ax1, ay1, ax2, ay2);
         g.drawLine(ax2, ay2, ax3, ay3);
         g.drawLine(ax3, ay3, ax4, ay4);
         g.drawLine(ax4, ay4, ax5, ay5);
         g.drawLine(ax5, ay5, ax6, ay6);
         g.drawLine(ax6, ay6, ax7, ay7);
         g.drawLine(ax7, ay7, ax8, ay8);
         g.drawLine(ax8, ay8, ax1, ay1);
 
         // Draw A#
         final int aax1 = ax2;
         final int aay1 = ay2;
         final int aax2 = aax1 + blackKeyWidth;
         final int aay2 = aay1;
         //final int aax3 = aax2;
         //final int aay3 = aay2 + blackKeyHeight;
         //final int aax4 = aax1;
         //final int aay4 = aay3;
 
         setKeyColor(g, 10);
         g.fillRect(aax1, aay1, blackKeyWidth, blackKeyHeight);
 
         g.setColor(CONTOUR_COLOR);
         g.drawRect(aax1, aay1, blackKeyWidth, blackKeyHeight);
 
         // Draw H
         final int hx1 = aax2;
         final int hy1 = aay2;
         final int hx2 = ax4 + whiteKeyWidth;
         final int hy2 = hy1;
         final int hx3 = hx2;
         final int hy3 = hy2 + whiteKeyHeight;
         final int hx4 = ax5;
         final int hy4 = hy3;
         final int hx5 = hx4;
         final int hy5 = ay4;
         final int hx6 = hx5 + blackKeyNarrowWidth;
         final int hy6 = hy5;
 
        setKeyColor(g, 11);
         g.fillRect(hx1, hy1, whiteKeyNarrowWidth, blackKeyHeight);
         g.fillRect(hx5, hy5, whiteKeyWidth, whiteKeyLesserHeight);
 
         g.setColor(CONTOUR_COLOR);
         g.drawLine(hx1, hy1, hx2, hy2);
         g.drawLine(hx2, hy2, hx3, hy3);
         g.drawLine(hx3, hy3, hx4, hy4);
         g.drawLine(hx4, hy4, hx5, hy5);
         g.drawLine(hx5, hy5, hx6, hy6);
         g.drawLine(hx6, hy6, hx1, hy1);
 
 
     }
 
     /**
      * Set color of the key.
      *
      * @param g Graphics to paint upon
      * @param index Index of the in an octave
      */
     private void setKeyColor(Graphics g, int index)
     {
         if(WHITE_KEY[index] && !keyPressed[index] ||
            !WHITE_KEY[index] && keyPressed[index])
         {
             g.setColor(WHITE_COLOR);
         }
         else
         {
             g.setColor(BLACK_COLOR);
         }
     }
 
     /**
      * Handler of key pressed event.
      *
      * @param keyCode Code of the key pressed
      */
     public void keyPressed(int keyCode)
     {
         switch(keyCode)
         {
             case KEY_NUM1:
                 keyPressed[0] = true;
                 break;
 
             case KEY_NUM2:
                 keyPressed[1] = true;
                 break;
 
             case KEY_NUM3:
                 keyPressed[2] = true;
                 break;
 
             case KEY_NUM4:
                 keyPressed[3] = true;
                 break;
 
             case KEY_NUM5:
                 keyPressed[4] = true;
                 break;
 
             case KEY_NUM6:
                 keyPressed[5] = true;
                 break;
 
             case KEY_NUM7:
                 keyPressed[6] = true;
                 break;
 
             case KEY_NUM8:
                 keyPressed[7] = true;
                 break;
 
             case KEY_NUM9:
                 keyPressed[8] = true;
                 break;
 
             case KEY_STAR:
                 keyPressed[9] = true;
                 break;
 
             case KEY_NUM0:
                 keyPressed[10] = true;
                 break;
 
             case KEY_POUND:
                 keyPressed[11] = true;
                 break;
         }
     }
 
     /**
      * Handler of key released event.
      *
      * @param keyCode Code of the key released
      */
     public void keyReleased(int keyCode)
     {
         switch(keyCode)
         {
             case KEY_NUM1:
                 keyPressed[0] = false;
                 break;
 
             case KEY_NUM2:
                 keyPressed[1] = false;
                 break;
 
             case KEY_NUM3:
                 keyPressed[2] = false;
                 break;
 
             case KEY_NUM4:
                 keyPressed[3] = false;
                 break;
 
             case KEY_NUM5:
                 keyPressed[4] = false;
                 break;
 
             case KEY_NUM6:
                 keyPressed[5] = false;
                 break;
 
             case KEY_NUM7:
                 keyPressed[6] = false;
                 break;
 
             case KEY_NUM8:
                 keyPressed[7] = false;
                 break;
 
             case KEY_NUM9:
                 keyPressed[8] = false;
                 break;
 
             case KEY_STAR:
                 keyPressed[9] = false;
                 break;
 
             case KEY_NUM0:
                 keyPressed[10] = false;
                 break;
 
             case KEY_POUND:
                 keyPressed[11] = false;
                 break;
         }
     }
 }
