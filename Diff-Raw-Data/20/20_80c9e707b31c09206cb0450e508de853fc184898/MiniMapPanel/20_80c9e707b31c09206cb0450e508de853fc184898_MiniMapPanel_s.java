 package kkckkc.syntaxpane;
 
 import kkckkc.syntaxpane.model.FoldManager;
 import kkckkc.syntaxpane.model.LineManager;
 import kkckkc.syntaxpane.model.MutableFoldManager;
 import kkckkc.syntaxpane.model.SourceDocument;
 import kkckkc.utils.swing.ColorUtils;
 import kkckkc.utils.swing.Wiring;
 
 import javax.swing.*;
 import javax.swing.plaf.PanelUI;
 import java.awt.*;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
 class MiniMapPanel extends JPanel implements LineManager.LineListener, PropertyChangeListener {
     private static final int PIXELS_PER_LINE = 2;
     private static final int LEFT_COLUMN = 3;
     private static final int REPAINT_DELAY = 100;
     private static final String WHITESPACE = " \t";
 
     // Collaborators
     private JScrollPane scrollPane;
     private ScrollableSourcePane.SourceJEditorPane editorPane;
     private JViewport viewPort;
     private SourceDocument document;
 
     // Properties
     private Color highlightColor;
 
     // State
     private boolean isDragAndDrop = false;
     private double dragHandleOffset = 0;
     private Rectangle highlightRectangle = null;
 
     // Cache
     private int lineHeight;
 
     public MiniMapPanel(JViewport vp, JScrollPane sp, ScrollableSourcePane.SourceJEditorPane ep) {
         this.scrollPane = sp;
         this.editorPane = ep;
         this.viewPort = vp;
 
         Wiring.wire(this.editorPane, this, "font");
 
         installListeners();
     }
 
     private void installListeners() {
         MouseAdapter ma = new MouseListener();
         addMouseListener(ma);
         addMouseMotionListener(ma);
 
         this.scrollPane.getVerticalScrollBar().addAdjustmentListener(new ScrollBarListener());
         this.editorPane.addPropertyChangeListener("document", this);
     }
 
     @Override
     public Dimension getPreferredSize() {
         Dimension d = super.getPreferredSize();
         d.setSize(editorPane.getWrapColumn(), PIXELS_PER_LINE * getFoldManager().getVisibleLineCount());
         return d;
     }
 
     @Override
     public void setFont(Font font) {
         FontMetrics fontMetrics = getFontMetrics(font);
         lineHeight = fontMetrics.getHeight();
     }
 
     private void startDragAndDrop(Point point) {
         this.isDragAndDrop = true;
         this.dragHandleOffset = point.y - highlightRectangle.getY();
     }
 
     private void endDragAndDrop() {
         this.isDragAndDrop = false;
         repaint(highlightRectangle);
     }
 
     private void drag(Point point) {
         if (isDragAndDrop) {
             int lineIdx = Math.min(point.y - (int) dragHandleOffset, getHeight() - highlightRectangle.height) / PIXELS_PER_LINE;
             lineIdx = getFoldManager().fromVisibleIndex(lineIdx);
             int y = lineHeight * lineIdx;
             y = Math.max(0, y);
             scrollPane.getViewport().setViewPosition(new Point(0, y));
         }
     }
 
     @Override
     public void paintComponent(Graphics g) {
         Graphics2D g2 = (Graphics2D) g;
 
         g2.setColor(getBackground());
         g2.fill(g2.getClipBounds());
 
         paintHighlight(g2);
         paintLines(g2);
     }
 
     private void paintLines(Graphics2D g2) {
         LineManager lineManager = getLineManager();
         FoldManager foldManager = getFoldManager();
 
         int clipStartY = g2.getClipBounds().y;
         int clipEndY = clipStartY + g2.getClipBounds().height;
 
         int startLine = clipStartY / PIXELS_PER_LINE;
         int endLine = clipEndY / PIXELS_PER_LINE;
         int y = (g2.getClipBounds().y / PIXELS_PER_LINE) * PIXELS_PER_LINE;
 
         g2.setColor(getForeground());
         Iterator<LineManager.Line> it = lineManager.iterator(
                 getFoldManager().fromVisibleIndex(startLine),
                 getFoldManager().fromVisibleIndex(endLine));
         while (it.hasNext()) {
             LineManager.Line l = it.next();
 
             if (foldManager.getFoldState(l) == FoldManager.State.FOLDED_SECOND_LINE_AND_REST) {
                 continue;
             }
 
             int start = LEFT_COLUMN;
             StringTokenizer tok = new StringTokenizer(l.getCharSequence(false).toString(), WHITESPACE, true);
             while (tok.hasMoreTokens()) {
                 String item = tok.nextToken();
                 if (item.equals("\t")) {
                     start += getTabWidth();
                 } else if (item.equals(" ")) {
                     start++;
                 } else {
                     g2.drawLine(start, y, start + item.length() - 1, (int) y);
                     start += item.length();
                 }
             }
 
             y += PIXELS_PER_LINE;
         }
     }
 
     private void paintHighlight(Graphics2D g2) {
         Rectangle rect = highlightRectangle;
         if (rect.intersects(g2.getClipBounds())) {
             g2.setColor(highlightColor);
             g2.fill(rect);
 
             g2.setColor(ColorUtils.mix(highlightColor, getForeground(), isDragAndDrop ? 0.9 : 0.5));
             g2.drawLine(rect.x, rect.y + 1, rect.x + rect.width, rect.y + 1);
             g2.drawLine(rect.x, rect.y + rect.height - 1, rect.x + rect.width, rect.y + rect.height - 1);
         }
     }
 
     private int getTabWidth() {
         return 4;
     }
 
     private LineManager getLineManager() {
         return ((SourceDocument) editorPane.getDocument()).getLineManager();
     }
 
     private MutableFoldManager getFoldManager() {
         return ((SourceDocument) editorPane.getDocument()).getFoldManager();
     }
 
     private void moveHighlightPosition(double highlightPosition) {
         Rectangle newHighlightRectangle =
                 new Rectangle(
                         0, (int) (getFoldManager().getVisibleLineCount() * PIXELS_PER_LINE * highlightPosition),
                         getWidth(), PIXELS_PER_LINE * (scrollPane.getHeight() / lineHeight));
         if (newHighlightRectangle.equals(highlightRectangle)) return;
 
         if (highlightRectangle != null) {
             repaint(highlightRectangle.union(newHighlightRectangle));
         } else {
             repaint(newHighlightRectangle);
         }
 
         this.highlightRectangle = newHighlightRectangle;
     }
 
     @Override
     public void propertyChange(PropertyChangeEvent event) {
         if (document != null) {
            document.getLineManager().removeLineListener(this);
         }
         document = (SourceDocument) event.getNewValue();
         document.getLineManager().addLineListener(this);
         document.addFoldListener(new MutableFoldManager.FoldListener() {
 			public void foldUpdated() {
 				repaint();
 			}
 		});
     }
 
     @Override
     public void linesAdded(java.util.List<LineManager.Line> lines) {
        repaint(REPAINT_DELAY, 0, lines.get(0).getIdx() * PIXELS_PER_LINE - 1,
                getWidth(), getHeight());
     }
 
     @Override
     public void linesUpdated(java.util.List<LineManager.Line> lines) {
        repaint(REPAINT_DELAY, 0, lines.get(0).getIdx() * PIXELS_PER_LINE - 1,
                getTabWidth(), lines.get(lines.size() - 1).getIdx() * PIXELS_PER_LINE + 1);
     }
 
     @Override
     public void linesRemoved(java.util.List<LineManager.Line> lines) {
        repaint(REPAINT_DELAY, 0, lines.get(0).getIdx() * PIXELS_PER_LINE - 1,
                getWidth(), getHeight());
     }
 
     public void setHighlightColor(Color highlightColor) {
         this.highlightColor = highlightColor;
     }
 
     @Override
     public void updateUI() {
     }
 
     @Override
     public void setUI(PanelUI ui) {
     }
 
     private class MouseListener extends MouseAdapter {
         @Override
         public void mouseClicked(MouseEvent e) {
             if (e.getButton() == 1 && ! highlightRectangle.contains(e.getPoint())) {
                 int lineIdx = e.getY() / PIXELS_PER_LINE;
                 scrollPane.getViewport().setViewPosition(new Point(0, lineHeight * lineIdx));
             }
         }
 
         @Override
         public void mousePressed(MouseEvent e) {
             if (e.getButton() == 1 && highlightRectangle.contains(e.getPoint())) {
                 startDragAndDrop(e.getPoint());
             }
         }
 
         @Override
         public void mouseReleased(MouseEvent e) {
             endDragAndDrop();
         }
 
         @Override
         public void mouseExited(MouseEvent e) {
             endDragAndDrop();
         }
 
         @Override
         public void mouseDragged(MouseEvent e) {
             drag(e.getPoint());
         }
     }
 
     private class ScrollBarListener implements AdjustmentListener {
         int position = -1;
         int currentValue = -1;
 
         @Override
         public void adjustmentValueChanged(AdjustmentEvent e) {
             if (e.getValue() != currentValue) {
                 currentValue = e.getValue();
 
                 moveHighlightPosition(
                         scrollPane.getViewport().getViewRect().getY() / editorPane.getBounds().getHeight());
             }
 
             if (getBounds().getHeight() > viewPort.getExtentSize().getHeight()) {
                 int newPosition = (int) ((getBounds().getHeight() - viewPort.getExtentSize().getHeight()) *
                             (e.getValue() / (editorPane.getBounds().getHeight() - scrollPane.getBounds().getHeight())));
                 if (newPosition != position) {
                     position = newPosition;
                     viewPort.setViewPosition(new Point(0, position));
                 }
             }
         }
     }
 }
 
