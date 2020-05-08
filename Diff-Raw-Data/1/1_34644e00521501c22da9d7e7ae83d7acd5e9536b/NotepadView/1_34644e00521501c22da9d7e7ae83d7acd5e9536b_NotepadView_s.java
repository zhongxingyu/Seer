 package notepad.view;
 
 import notepad.NotepadException;
 import notepad.controller.NotepadController;
 import notepad.utils.Segment;
 import notepad.utils.SegmentL;
 import org.apache.log4j.Logger;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.font.FontRenderContext;
 import java.awt.font.TextLayout;
 import java.text.AttributedString;
 import java.util.ArrayList;
 
 /**
  * Evgeny Vanslov
  * vans239@gmail.com
  */
 public class NotepadView extends JPanel {
     private static final Logger log = Logger.getLogger(NotepadView.class);
     private static final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
     private static final Color CARET_COLOR = Color.red;
     private static final Color TEXT_COLOR = Color.black;
 
     private int maxLength;
     private long viewPosition = 0;
     private int caretPosition = 0;
     private Segment selectionSegment;
 
     private boolean isShowSelection = false;
 
     private String text;
     private NotepadController controller;
 
     private ArrayList<TextLayoutInfo> layouts = new ArrayList<TextLayoutInfo>();
     private FontRenderContext frc = getFontMetrics(font).getFontRenderContext();
 
     public NotepadView(final NotepadController controller) throws NotepadException {
         this.controller = controller;
         addComponentListener(new ComponentAdapter() {
             public void componentResized(ComponentEvent e) {
                 updateMaxLength();
                 update();
             }
         });
     }
 
     public boolean isShowSelection() {
         return isShowSelection;
     }
 
     public void updateSelectionSegment(Segment draggedSegment) {
         this.selectionSegment = draggedSegment;
     }
 
     public int getCaretPosition() {
         return caretPosition;
     }
 
     public void showSelectionSegment(boolean isShow) {
         isShowSelection = isShow;
     }
 
     public Segment getSelectionSegment() {
         return selectionSegment;
     }
 
     public void updateMaxLength() {
         //todo find real coeff
         maxLength = 3 * (getSize().width / getFontMetrics(font).charWidth('a')) * (getSize().height / getFontMetrics(font).getHeight()) / 2;
     }
 
     public Dimension getPreferredSize() {
         return new Dimension(600, 480);
     }
 
     public long getViewPosition() {
         return viewPosition;
     }
 
     public long getEditPosition() {
         return viewPosition + caretPosition;
     }
 
     public void updateScrollShift(final int shift) throws NotepadException {
         viewPosition = new SegmentL(0, controller.length()).nearest(viewPosition + shift);
     }
 
     public boolean isAvailableShiftScroll(final int shift) throws NotepadException {
         return new SegmentL(0, controller.length()).in(viewPosition + shift);
     }
 
     public SegmentL getAvailableScrollShift() throws NotepadException {
         return new SegmentL(-viewPosition, controller.length() - viewPosition);
     }
 
     public void updateCaretShift(final int shift) throws NotepadException {
         final Segment segment = getAvailableCaretShift();
         if (segment.in(shift)) {
             caretPosition += shift;
         } else {
             final SegmentL segmentL = getAvailableScrollShift();
             viewPosition += segmentL.nearest(shift);
             caretPosition = (int) (Math.min(viewPosition + caretPosition, controller.length()) - viewPosition);
         }
     }
 
     public void updateCaretGoTo(final int value) {
         caretPosition = value;
     }
 
     public ArrayList<TextLayoutInfo> getLayouts() {
         return layouts;
     }
 
     public boolean isAvailableShiftCaret(final int shift) {
         return new Segment(0, text.length()).in(caretPosition + shift);
     }
 
     public Segment getAvailableCaretShift() {
         return new Segment(-caretPosition, text.length() - caretPosition);
     }
 
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         setBackground(Color.white);
         if (text.isEmpty()) {
             return;           //todo empty text edition
         }
         final Graphics2D g2d = (Graphics2D) g;
         final int drawPosX = 0;
         final int drawPosY = 0;
 
         g2d.translate(drawPosX, drawPosY);
         drawLayouts(g2d);
         drawCaret(g2d);
         if (isShowSelection) {
             drawDragged(g2d);
         }
     }
 
 
     public void update() {
         try {
             updateText();
         } catch (NotepadException e) {
             log.error("Can't update text for view", e);
         }
         repaint();
     }
 
     private void updateText() throws NotepadException {
         text = controller.get(viewPosition, Math.min((int) (controller.length() - viewPosition), maxLength));
         if (caretPosition > text.length()) {
             caretPosition = text.length();
         }
         initLayouts();
     }
 
     private void initLayouts() {
         layouts.clear();
         int breakWidth = getSize().width;
         int height = getSize().height;
         int x = 0;
         int y = 0;
         int position = 0;
         final String lineSeparator = "\n";
         String lines[] = text.split(lineSeparator);
         for (String line : lines) {
             if (y > height) {
                 break;
             }
             line += " " ; //lineSeparator.length
             if (line.isEmpty()) {
                 final TextLayout layout = new TextLayout(new AttributedString(" ").getIterator(), frc);
                 y += layout.getAscent() + layout.getDescent() + layout.getLeading();
                 layouts.add(new TextLayoutInfo(layout, new Point(x, y), position));
                 continue;
             }
             final MonospacedLineBreakMeasurer lineMeasurer =
                     new MonospacedLineBreakMeasurer(line, getFontMetrics(font), frc);
             lineMeasurer.setPosition(0);
             while (lineMeasurer.getPosition() < line.length() && y <= height) {
                 final TextLayout layout = lineMeasurer.nextLayout(breakWidth);
                 y += layout.getAscent() + layout.getDescent() + layout.getLeading();
                 layouts.add(new TextLayoutInfo(layout, new Point(x, y), position));
                 position += layout.getCharacterCount();
             }
             int i = 0;
         }
         if (y > height) {
             final TextLayoutInfo textLayoutInfo = layouts.get(layouts.size() - 1);
             layouts.remove(layouts.size() - 1);
             text = text.substring(0, textLayoutInfo.getPosition());
             caretPosition = Math.min(text.length(), caretPosition);
         }
     }
 
     private void drawLayouts(Graphics2D g2d) {
         for (final TextLayoutInfo layoutInfo : layouts) {
             layoutInfo.getLayout().draw(g2d, layoutInfo.getOrigin().x, layoutInfo.getOrigin().y);
         }
     }
 
     private void drawCaret(Graphics2D g2d) {
         for (int i = 0; i < layouts.size(); ++i) {
             final TextLayoutInfo layoutInfo = layouts.get(i);
             if (caretInThisTextLayout(layoutInfo, i == layouts.size() - 1)) {
                 g2d.translate(layoutInfo.getOrigin().x, layoutInfo.getOrigin().y);
                 final Shape[] carets = layoutInfo.getLayout().getCaretShapes(
                         caretPosition - layoutInfo.getPosition());
                 g2d.setColor(CARET_COLOR);
                 g2d.draw(carets[0]);
                 g2d.setColor(TEXT_COLOR);
                 g2d.translate(-layoutInfo.getOrigin().x, -layoutInfo.getOrigin().y);
             }
         }
     }
 
     private void drawDragged(Graphics2D g2d) {
         int draggedFrom = selectionSegment.getStart();
         int draggedTo = selectionSegment.getEnd();
 
         for (int i = 0; i < layouts.size(); ++i) {
             final TextLayoutInfo layoutInfo = layouts.get(i);
             final Segment segment =
                     new Segment(layoutInfo.getPosition(), layoutInfo.getPosition() + layoutInfo.getLayout().getCharacterCount());
             if (draggedFrom < segment.getEnd()) {
                 g2d.translate(layoutInfo.getOrigin().x, layoutInfo.getOrigin().y);
                 final Segment blackboxSegment =
                         new Segment(segment.nearest(draggedFrom) - layoutInfo.getPosition(), segment.nearest(draggedTo) - layoutInfo.getPosition());
                 if (blackboxSegment.getEnd() != blackboxSegment.getStart()) {
                     final Shape blackbox = layoutInfo.getLayout().getLogicalHighlightShape(blackboxSegment.getStart(), blackboxSegment.getEnd());
                     g2d.setColor(CARET_COLOR);
                     g2d.draw(blackbox);
                     g2d.setColor(TEXT_COLOR);
                 }
                 g2d.translate(-layoutInfo.getOrigin().x, -layoutInfo.getOrigin().y);
             }
         }
     }
 
     //todo private?
     public boolean caretInThisTextLayout(TextLayoutInfo layoutInfo, boolean isLastLayout) {
         int from = layoutInfo.getPosition();
         int to = from + layoutInfo.getLayout().getCharacterCount();
         return (caretPosition >= from && caretPosition < to)
                 || (caretPosition > from && isLastLayout);
     }
 }
