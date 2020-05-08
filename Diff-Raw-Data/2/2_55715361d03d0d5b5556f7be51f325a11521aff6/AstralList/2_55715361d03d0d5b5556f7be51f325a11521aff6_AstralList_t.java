 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * A list is a, well, list of objects. You can select from the list, scroll
  * the list, and all kinds of neat stuff.
  */
 package gdi.component;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 /**
  *
  * @author Nathan Wiehoff
  */
 public class AstralList extends AstralComponent {
 
     ArrayList<Object> listContents = new ArrayList<>();
     //appearance
    protected Font font = new Font("Monospaced", Font.PLAIN, 10);
     protected Color fontColor = amber;
     protected Color backColor = windowGrey;
     protected Color selectColor = Color.darkGray;
     BufferedImage buffer;
     //index and scrolling
     private int index = 0;
     protected int scrollPosition = 0;
     private int scrollDirection = 0;
     //for getting info about my location
     private AstralWindow parent;
     //for the scroll bar
     private int oldMx;
     private int oldMy;
     boolean dragging = false;
 
     public AstralList(AstralWindow parent) {
         super();
         this.parent = parent;
     }
 
     @Override
     public void render(Graphics f) {
         if (visible) {
             try {
                 if (buffer == null) {
                     buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                 }
                 Graphics2D s = (Graphics2D) buffer.getGraphics();
                 //draw the background
                 s.setColor(backColor);
                 s.fillRect(0, 0, width, height);
                 //draw over the selected
                 s.setColor(selectColor);
                 s.fillRect(0, (index - scrollPosition) * getFont().getSize(), width - 10, getFont().getSize());
                 //draw indicator that there is more or less to the list
                 //draw the text
                 s.setFont(getFont());
                 s.setColor(fontColor);
                 for (int a = scrollPosition; a < listContents.size(); a++) {
                     s.drawString(listContents.get(a).toString(), 1, ((a + 1) - scrollPosition) * getFont().getSize());
                 }
                 int displayableLines = height / getFont().getSize();
                 //draw scroll bar
                 s.setColor(fontColor);
                 if (displayableLines >= listContents.size()) {
                     s.fillRect(width - 10, 0, 9, height);
                 } else {
                     double scale = (double) displayableLines / (double) listContents.size();
                     double listPercent = (double) scrollPosition / (double) listContents.size();
                     s.fillRect(width - 10, (int) (listPercent * height), 9, (int) (scale * height) + 2);
                 }
                 //draw the edges
                 s.drawRect(0, 0, width - 1, height - 1);
                 //push
                 f.drawImage(buffer, x, y, null);
             } catch (Exception e) {
                 scrollPosition = 0;
                 index = 0;
             }
         }
     }
 
     @Override
     public void periodicUpdate() {
         if (scrollDirection == 0) {
         } else if (scrollDirection == -1) {
             scrollUp();
         } else if (scrollDirection == 1) {
             scrollDown();
         }
         if (listContents.size() < scrollPosition) {
             scrollPosition = listContents.size();
         }
     }
 
     public void addToList(Object item) {
         listContents.add(item);
     }
 
     public void removeFromList(Object item) {
         listContents.remove(item);
     }
 
     public void clearList() {
         listContents.clear();
     }
 
     public int getIndex() {
         return index;
     }
 
     public void setIndex(int index) {
         if (index < listContents.size()) {
             this.index = index;
         }
     }
 
     public Object getItemAtIndex(int index) {
         if (index < listContents.size() && index > -1) {
             return listContents.get(index);
         } else {
             index = 0;
             return null;
         }
     }
 
     public Color getSelectColor() {
         return selectColor;
     }
 
     public void setSelectColor(Color selectColor) {
         this.selectColor = selectColor;
     }
 
     public void scrollDown() {
         scrollPosition++;
         if (scrollPosition > listContents.size() - (int) (height / getFont().getSize())) {
             scrollPosition = listContents.size() - (int) (height / getFont().getSize());
         }
     }
 
     public void scrollUp() {
         scrollPosition--;
         if (scrollPosition < 0) {
             scrollPosition = 0;
         }
     }
 
     @Override
     public void handleKeyPressedEvent(KeyEvent ke) {
         if (ke.getKeyCode() == KeyEvent.VK_UP) {
             scrollDirection = -1;
         } else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
             scrollDirection = 1;
         }
     }
 
     @Override
     public void handleKeyReleasedEvent(KeyEvent ke) {
         if (ke.getKeyCode() == KeyEvent.VK_UP) {
             scrollDirection = 0;
         } else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
             scrollDirection = 0;
         }
     }
 
     @Override
     public void handleMouseClickedEvent(MouseEvent me) {
         //determine the relative mouse location
         int rx = me.getX() - parent.getX() - x;
         int ry = me.getY() - parent.getY() - y;
         //create the local mouse rect
         Rectangle mouseRect = new Rectangle(rx, ry, 1, 1);
         //check for list intersections to update selection
         checkForListIntersection(mouseRect);
     }
 
     @Override
     public void handleMousePressedEvent(MouseEvent me) {
         //determine the relative mouse location
         int rx = me.getX() - parent.getX() - x;
         int ry = me.getY() - parent.getY() - y;
         oldMx = rx;
         oldMy = ry;
         //create the local mouse rect
         Rectangle mouseRect = new Rectangle(rx, ry, 1, 1);
         //create the scrollbar rect
         Rectangle scrollRect = new Rectangle(width - 9, 0, 9, height);
         int displayableLines = height / (getFont().getSize());
         if (scrollRect.intersects(mouseRect) && (displayableLines < listContents.size())) {
             dragging = true;
         }
     }
 
     @Override
     public void handleMouseReleasedEvent(MouseEvent me) {
         if (dragging) {
             //scroll based on how far the mouse moved
             int dy = (me.getY() - parent.getY() - y) - oldMy;
             double change = dy / (double) height;
             scrollPosition += (int) (listContents.size() * change);
             //check bounds
             if (scrollPosition < 0) {
                 scrollPosition = 0;
             }
             if (scrollPosition > listContents.size()) {
                 scrollPosition = listContents.size() - 1;
             }
         }
         dragging = false;
     }
 
     private void checkForListIntersection(Rectangle mouseRect) {
         int displayableLines = height / getFont().getSize();
         for (int a = 0; a < displayableLines; a++) {
             Rectangle lRect = new Rectangle(0, a * (getFont().getSize()), width - 9, getFont().getSize());
             if (lRect.intersects(mouseRect)) {
                 int newSelection = a + scrollPosition;
                 setIndex(newSelection);
             }
         }
     }
 
     public Font getFont() {
         return font;
     }
 
     public void setFont(Font font) {
         this.font = font;
     }
 }
