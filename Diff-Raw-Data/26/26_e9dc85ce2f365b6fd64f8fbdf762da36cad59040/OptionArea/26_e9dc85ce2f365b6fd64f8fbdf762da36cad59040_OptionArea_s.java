 package org.lehirti.engine.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.awt.geom.Rectangle2D;
 import java.io.Externalizable;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.JComponent;
 
 import org.lehirti.engine.res.ResourceCache;
 import org.lehirti.engine.res.images.CommonImage;
 import org.lehirti.engine.res.images.ImageWrapper;
 import org.lehirti.engine.res.text.CommonText;
 import org.lehirti.engine.res.text.TextKey;
 import org.lehirti.engine.res.text.TextWrapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class OptionArea extends JComponent implements Externalizable {
   private static final long serialVersionUID = 1L;
   
   private static final Logger LOGGER = LoggerFactory.getLogger(OptionArea.class);
   
   private final double screenX;
   private final double screenY;
   private final double sizeX;
   private final double sizeY;
   
   private final int cols;
   private final int rows;
   
   private final Map<Key, TextWrapper> options = new EnumMap<Key, TextWrapper>(Key.class);
   private boolean isTextInput = false;
   private TextKey textInputLabel = null;
   private String currentTextInput = null;
   
   private volatile boolean repaintNeeded = false;
   
   final ImageWrapper optionAreaBackground;
   
   public OptionArea(final double screenX, final double screenY, final double sizeX, final double sizeY, final int cols,
       final int rows) {
     this.screenX = screenX;
     this.screenY = screenY;
     this.sizeX = sizeX;
     this.sizeY = sizeY;
     this.cols = cols;
     this.rows = rows;
     this.optionAreaBackground = ResourceCache.get(CommonImage.OPTION_AREA_BACKGROUND);
     this.optionAreaBackground.pinRandomImage();
   }
   
   @Override
  public void paintComponent(final Graphics g) {
     g.setColor(Color.WHITE);
     g.fillRect(0, 0, getWidth(), getHeight());
     g.drawImage(this.optionAreaBackground.getImage(), 0, 0, getWidth(), getHeight(), null);
     g.setColor(Color.BLACK);
     final Dimension size = getSize();
     final Dimension sizeOfOneOptionField = new Dimension(size.width / this.cols, size.height / this.rows);
     final Font font = Main.getCurrentTextArea().getScaledFont();
     g.setFont(font);
     final FontMetrics fontMetrics = g.getFontMetrics(font);
     final int fontHeight = fontMetrics.getHeight();
     if (this.isTextInput) {
       final TextWrapper wrapper = ResourceCache.get(this.textInputLabel);
       final String labelString = wrapper.getValue();
       final Rectangle2D labelBounds = fontMetrics.getStringBounds(labelString, g);
       g.drawString(labelString, (int) ((size.width - labelBounds.getWidth()) / 2), (int) labelBounds.getHeight());
       
       final Rectangle2D inputBounds = fontMetrics.getStringBounds(this.currentTextInput, g);
       g.drawString(this.currentTextInput, (int) ((size.width - inputBounds.getWidth()) / 2),
           (int) (labelBounds.getHeight() + inputBounds.getHeight() + (size.height / 10)));
     } else {
       final int yOffset = fontHeight + (sizeOfOneOptionField.height - fontHeight) / 4;
       for (final Map.Entry<Key, TextWrapper> option : this.options.entrySet()) {
         final Key key = option.getKey();
         final TextWrapper wrapper = ResourceCache.get(CommonText.KEY_OPTION);
         wrapper.addParameter(key.getKeyText());
         final TextWrapper text = option.getValue();
         final int x = key.col * sizeOfOneOptionField.width;
         final int y = key.row * sizeOfOneOptionField.height + yOffset;
         
         // g.drawRect(key.col * sizeOfOneOptionField.width, key.row * sizeOfOneOptionField.height,
         // sizeOfOneOptionField.width, sizeOfOneOptionField.height);
         
         g.drawImage(key.getButtonImage(), key.col * sizeOfOneOptionField.width, key.row * sizeOfOneOptionField.height,
             sizeOfOneOptionField.height, sizeOfOneOptionField.height, null);
         final Rectangle2D keyBounds = fontMetrics.getStringBounds(key.getKeyText(), g);
         final int xOffsetKey = (int) ((sizeOfOneOptionField.height - keyBounds.getWidth()) / 2);
         g.drawString(key.getKeyText(), key.col * sizeOfOneOptionField.width + xOffsetKey, key.row
             * sizeOfOneOptionField.height + yOffset);
         
         final Dimension sizeOfOneOptionFieldWOKey = new Dimension(sizeOfOneOptionField.width
             - sizeOfOneOptionField.height, sizeOfOneOptionField.height);
         
         if (fontMetrics.stringWidth(text.getValue()) < sizeOfOneOptionField.width - sizeOfOneOptionField.height) {
           g.drawString(text.getValue(), x + sizeOfOneOptionField.height, y);
         } else {
           fitStringIntoOptionField(g, text.getValue(), x + sizeOfOneOptionField.height, key.row
               * sizeOfOneOptionField.height, sizeOfOneOptionFieldWOKey, font.getSize());
         }
       }
     }
   }
   
   private void fitStringIntoOptionField(final Graphics g, final String optionString, final int xOffset,
       final int yOffset, final Dimension optionField, final int fontSize) {
     final String[] words = optionString.split(" ");
     for (int i = fontSize; i > 0; i--) {
       final Font font = g.getFont();
       final Font newFont = new Font(font.getName(), font.getStyle(), i);
       final FontMetrics metrics = g.getFontMetrics(newFont);
       final int nrLines = optionField.height / metrics.getHeight();
       final List<String> lines = new ArrayList<String>(nrLines);
       int k = 0;
       for (int j = 0; j < nrLines; j++) {
         if (k >= words.length) {
           break;
         }
         final StringBuilder sb = new StringBuilder(words[k++]);
         while (k < words.length && metrics.stringWidth(sb.toString() + " " + words[k]) < optionField.width) {
           sb.append(" ");
           sb.append(words[k++]);
         }
         lines.add(sb.toString());
       }
       if (k >= words.length) {
         g.setFont(newFont);
         for (int j = 0; j < lines.size(); j++) {
           final int stringWidth = metrics.stringWidth(lines.get(j));
           final int xPad = (optionField.width - stringWidth) / 2;
           final int yPad = (optionField.height - lines.size() * metrics.getHeight()) / 4;
           g.drawString(lines.get(j), xOffset + xPad, yOffset + yPad + ((j + 1) * metrics.getHeight()));
         }
         return;
       }
     }
   }
   
   @Override
   public Dimension getMinimumSize() {
     return getPreferredSize();
   }
   
   @Override
   public Dimension getMaximumSize() {
     return getPreferredSize();
   }
   
   @Override
   public Dimension getPreferredSize() {
     final Dimension size = getParent().getSize();
     double width = size.width;
     double height = size.height;
     if (width < 640) {
       width = 640;
     }
     if (height < 640) {
       height = 640;
     }
     
     final double baseSizeX = width / this.screenX;
     final double baseSizeY = height / this.screenY;
     final double baseSize = baseSizeX < baseSizeY ? baseSizeX : baseSizeY;
     
     width = baseSize * this.sizeX;
     height = baseSize * this.sizeY;
     
     if (LOGGER.isTraceEnabled()) {
       LOGGER.trace("preferredSize: " + width + " " + height);
     }
     return new Dimension((int) width, (int) height);
   }
   
   public List<TextWrapper> getAllOptions() {
     return new ArrayList<TextWrapper>(this.options.values());
   }
   
   @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
     this.options.clear();
     final int nrOfTexts = in.readInt();
     for (int i = 0; i < nrOfTexts; i++) {
       this.options.put((Key) in.readObject(), (TextWrapper) in.readObject());
     }
     repaint();
   }
   
   @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
     out.writeInt(this.options.size());
     for (final Entry<Key, TextWrapper> entry : this.options.entrySet()) {
       out.writeObject(entry.getKey());
       out.writeObject(entry.getValue());
     }
   }
   
  public void clearOptions() {
     this.isTextInput = false;
     if (!this.options.isEmpty()) {
       this.options.clear();
       this.repaintNeeded = true;
     }
   }
   
   // TODO: error on non-option keys
  public void setOption(final TextKey text, final Key key) {
     this.options.put(key, ResourceCache.get(text));
     this.repaintNeeded = true;
   }
   
   // TODO: error on non-option keys
  public void setOption(final TextWrapper text, final Key key) {
     this.options.put(key, text);
     this.repaintNeeded = true;
   }
   
  public void setTextInputOption(final TextKey text, final String initialTextInput) {
     this.isTextInput = true;
     this.textInputLabel = text;
     this.currentTextInput = initialTextInput;
     this.repaintNeeded = true;
   }
   
   /**
    * callable from non-gui thread
    */
   public void repaintIfNeeded() {
     if (this.repaintNeeded) {
       try {
         javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
           public void run() {
             repaint();
           }
         });
       } catch (final InterruptedException e) {
         LOGGER.error("Thread " + Thread.currentThread().toString() + " has been interrupted; terminating thread", e);
         throw new ThreadDeath();
       } catch (final InvocationTargetException e) {
         LOGGER.error("InvocationTargetException trying to add text to text area", e);
       }
       this.repaintNeeded = false;
     }
   }
   
  public String getCurrentTextInput() {
     return this.currentTextInput;
   }
   
  public boolean handleKeyEvent(final KeyEvent e) {
     final char keyChar = e.getKeyChar();
     if (keyChar == KeyEvent.CHAR_UNDEFINED) {
       return false;
     }
     
     if (keyChar == '\b') {
       if (this.currentTextInput.length() > 0) {
         this.currentTextInput = this.currentTextInput.substring(0, this.currentTextInput.length() - 1);
       }
     } else {
       this.currentTextInput += keyChar;
     }
     repaint();
     return true;
   }
 }
