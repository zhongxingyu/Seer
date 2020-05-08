 package br.org.indt.ndg.lwuit.ui.renderers;
 
 import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
 import com.sun.lwuit.Graphics;
 
 public class SimpleListWithAnimatedTextCellRenderer extends SimpleListCellRenderer {
 
     private final int PADDING = 5;
     private int position = 0;
 
     protected String getTextToAnimate() {
         return m_label.getText();
     }
 
     public void resetPosition() {
         position = 0;
     }
 
     public void incrementPosition() {
         if ( position + PADDING >= Integer.MAX_VALUE ) {
             resetPosition();
         }
         position += PADDING;
     }
 
     public void paint(Graphics g) {
         String text = getTextToAnimate();
         int width = getStyle().getFont().stringWidth(text);
        if (hasFocus() && (getWidth() <= width + PADDING)) {
             width+=PADDING*3;
             int fontHeight = NDGStyleToolbox.getInstance().listStyle.selectedFont.getHeight();
             int yPos = getY()+ (getHeight()-fontHeight)/2;
             if (getWidth() >= width + PADDING) { // otherwise a letter is cut
                 super.paint(g);
                 return;
             }
             int actualPosition = position % width;
             g.setColor(NDGStyleToolbox.getInstance().listStyle.selectedFontColor);
             g.setFont(NDGStyleToolbox.getInstance().listStyle.selectedFont);
             g.translate(-actualPosition, 0);
             g.drawString(text, getX(), yPos);
             g.translate(width, 0);
             g.drawString(text, getX(), yPos);
             g.translate(actualPosition - width, 0);
         } else {
             super.paint(g);
         }
     }
 }
