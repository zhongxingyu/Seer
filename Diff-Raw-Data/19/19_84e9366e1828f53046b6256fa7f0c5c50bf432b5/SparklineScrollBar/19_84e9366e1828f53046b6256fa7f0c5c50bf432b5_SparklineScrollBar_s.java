 /**
  *   Copyright 2011 Karl Martens
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *       
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  *
  *   net.karlmartens.ui, is a library of UI widgets
  */
 package net.karlmartens.ui.widget;
 
 import static net.karlmartens.ui.Images.ARROW_LEFT;
 import static net.karlmartens.ui.Images.ARROW_RIGHT;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.BitSet;
 import java.util.List;
 
 import net.karlmartens.platform.util.ArraySupport;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.Layer;
 import org.eclipse.draw2d.LayeredPane;
 import org.eclipse.draw2d.LineBorder;
 import org.eclipse.draw2d.MouseMotionListener;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.StackLayout;
 import org.eclipse.draw2d.XYLayout;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseTrackListener;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.TypedListener;
 
 public final class SparklineScrollBar extends Composite {
 
   private static final int THUMB_ALPHA_DEFAULT = 60;
   private static final int THUMB_ALPHA_OVER = 80;
   private static final Color LABEL_DEFAULT_COLOR = ColorConstants.black;
 
   private Color _dataPointColor = ColorConstants.gray;
   private Color _highlightColor = ColorConstants.blue;
 
   private final Image[] _images;
   private final org.eclipse.swt.widgets.Label _left;
   private final FigureCanvas _track;
   private final org.eclipse.swt.widgets.Label _right;
   private Layer _sparkLineLayer;
   private Layer _thumbLayer;
   private Layer _highlightLayer;
   private RectangleFigure _thumbFigure;
   private Layer _labelLayer;
   private Label _labelFigure;
 
   private int _minimum = 0;
   private int _maximum = 100;
   private int _thumb = 10;
   private BitSet _highlight = new BitSet();
   private int _increment = 1;
   private int _selection = 0;
   private double[] _data = new double[101];
   private boolean _inUpdate = false;
 
   public SparklineScrollBar(Composite parent, int style) {
     super(parent, style | SWT.DOUBLE_BUFFERED);
 
     final GridLayout layout = new GridLayout(3, false);
     layout.horizontalSpacing = 1;
     layout.verticalSpacing = 0;
     layout.marginBottom = 0;
     layout.marginTop = 0;
     layout.marginLeft = 0;
     layout.marginRight = 0;
     layout.marginHeight = 0;
     layout.marginWidth = 0;
     setLayout(layout);
 
     final Listener listener = new Listener();
     final Display display = getShell().getDisplay();
 
     _images = new Image[] { ARROW_LEFT.createImage(), //
         ARROW_RIGHT.createImage(), //
     };
 
     _left = new org.eclipse.swt.widgets.Label(this, SWT.CENTER);
     _left.setImage(_images[0]);
     _left.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
     _left.addMouseListener(listener);
     _left.addMouseTrackListener(listener);
 
     _track = new FigureCanvas(this);
     _track.setScrollBarVisibility(FigureCanvas.NEVER);
     _track.setContents(createContents(listener));
     _track.getViewport().setContentsTracksHeight(true);
     _track.getViewport().setContentsTracksWidth(true);
     _track.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
     _track.addMouseListener(listener);
 
     _right = new org.eclipse.swt.widgets.Label(this, SWT.CENTER);
     _right.setImage(_images[1]);
     _right.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
     _right.addMouseListener(listener);
     _right.addMouseTrackListener(listener);
 
     setBackground(display.getSystemColor(SWT.COLOR_WHITE));
     setForeground(display.getSystemColor(SWT.COLOR_WHITE));
 
     refresh();
     addControlListener(listener);
     addDisposeListener(listener);
   }
 
   private IFigure createContents(Listener listener) {
     final LayeredPane container = new LayeredPane();
     container.setBorder(new LineBorder());
     container.setLayoutManager(new StackLayout());
 
     _sparkLineLayer = new Layer();
     _sparkLineLayer.setLayoutManager(new XYLayout());
     container.add(_sparkLineLayer, 0);
 
     _highlightLayer = new Layer();
     _highlightLayer.setLayoutManager(new XYLayout());
     container.add(_highlightLayer, 1);
 
     _thumbLayer = new Layer();
     _thumbLayer.setLayoutManager(new XYLayout());
     container.add(_thumbLayer, 2);
 
     _labelLayer = new Layer();
     _labelLayer.setLayoutManager(new XYLayout());
     container.add(_labelLayer, 3);
 
     _thumbFigure = new RectangleFigure();
     _thumbFigure.setBackgroundColor(ColorConstants.yellow);
     _thumbFigure.setForegroundColor(ColorConstants.yellow);
     _thumbFigure.setAlpha(THUMB_ALPHA_DEFAULT);
     _thumbFigure.addMouseListener(listener);
     _thumbFigure.addMouseMotionListener(listener);
     _thumbLayer.add(_thumbFigure);
 
     _labelFigure = new Label();
     _labelFigure.setLabelAlignment(Label.NORTH);
     _labelFigure.setTextAlignment(Label.LEFT);
     _labelFigure.setForegroundColor(LABEL_DEFAULT_COLOR);
     _labelFigure.setVisible(false);
     _labelLayer.add(_labelFigure);
 
     return container;
   }
 
   public void setMinimum(int value) {
     checkWidget();
    if (value < 0 || value >= _maximum)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     _minimum = value;
     resizeData();
   }
 
   public int getMinimum() {
     checkWidget();
     return _minimum;
   }
 
   public void setMaximum(int value) {
     checkWidget();
    if (value <= _minimum)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     _maximum = value;
     resizeData();
   }
 
   public int getMaximum() {
     checkWidget();
     return _maximum;
   }
 
   public void setThumb(int value) {
     checkWidget();
     if (value <= 0 || value > (_maximum - _minimum + 1))
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     _thumb = value;
     refreshThumb();
   }
 
   public int getThumb() {
     checkWidget();
     return _thumb;
   }
 
   public void setHighlights(int[] indices) {
     checkWidget();
     if (indices == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
 
     try {
       _inUpdate = true;
       clearHighlights();
       highlight(indices);
     } finally {
       _inUpdate = false;
     }
     refreshHighlights();
   }
 
   public void setHighlights(int fromIndex, int toIndex) {
     checkWidget();
     if (fromIndex > toIndex)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     try {
       _inUpdate = true;
       clearHighlights();
       highlight(fromIndex, toIndex);
     } finally {
       _inUpdate = false;
     }
     refreshHighlights();
   }
 
   public void setHighlight(int index) {
     checkWidget();
     if (index < _minimum || index > _maximum)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     try {
       _inUpdate = true;
       clearHighlights();
       highlight(index);
     } finally {
       _inUpdate = false;
     }
     refreshHighlights();
   }
 
   public void clearHighlights() {
     checkWidget();
     _highlight.clear();
     refreshHighlights();
   }
 
   public void highlight(int[] indices) {
     checkWidget();
     if (indices == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
 
     try {
       _inUpdate = true;
       for (int index : indices) {
         highlight(index);
       }
     } finally {
       _inUpdate = false;
     }
     refreshHighlights();
   }
 
   public void highlight(int fromIndex, int toIndex) {
     checkWidget();
     if (fromIndex >= toIndex) {
       SWT.error(SWT.ERROR_INVALID_ARGUMENT);
     }
 
     try {
       _inUpdate = true;
       for (int index = fromIndex; index < toIndex; index++) {
         highlight(index);
       }
     } finally {
       _inUpdate = false;
     }
     refreshHighlights();
   }
 
   public void highlight(int index) {
     checkWidget();
     if (index < _minimum || index > _maximum)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     _highlight.set(index - _minimum);
     refreshHighlights();
   }
 
   public int[] getHighlights() {
     checkWidget();
     final int[] highlight = new int[_highlight.size()];
     int idx = 0;
     for (int h = _highlight.nextSetBit(0); h >= 0; h = _highlight.nextSetBit(h + 1)) {
       highlight[idx++] = h + _minimum;
     }
     return highlight;
   }
 
   public void setIncrement(int value) {
     checkWidget();
     if (value < 1)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     _increment = value;
   }
 
   public int getIncrement() {
     checkWidget();
     return _increment;
   }
 
   public void setSelection(int value) {
     checkWidget();
     final int v = Math.min(Math.max(_minimum, value), _maximum - _thumb + 1);
     if (v == _selection)
       return;
 
     _selection = v;
     refreshThumb();
     notifyListeners(SWT.Selection, new Event());
   }
 
   public int getSelection() {
     checkWidget();
     return _selection;
   }
 
   public double[] getDataPoints() {
     final int points = computeDataPoints();
     final double[] data = new double[points];
     System.arraycopy(_data, 0, data, 0, points);
     return data;
   }
 
   public void setDataPoints(double[] data) {
     setDataPoints(data, _minimum);
   }
 
   public void setDataPoints(double data, int index) {
     setDataPoints(new double[] { data }, index);
   }
 
   public void setDataPoints(double[] data, int index) {
     checkWidget();
     if (index < _minimum || index > _maximum)
       SWT.error(SWT.ERROR_INVALID_RANGE);
 
     final int i = index - _minimum;
     final int l = Math.min(_maximum - _minimum + 1 - i, data.length);
     System.arraycopy(data, 0, _data, i, l);
     refreshSparkline();
   }
 
   public void setLabel(String text) {
     _labelFigure.setText(text);
     refreshLabel();
   }
 
   public void setLabelFont(Font font) {
     _labelFigure.setFont(font);
     refreshLabel();
   }
 
   public void setLabelColor(Color color) {
     _labelFigure.setForegroundColor(color);
   }
 
   @Override
   public void setBackground(Color color) {
     super.setBackground(color);
     _left.setBackground(color);
     _track.setBackground(color);
     _right.setBackground(color);
   }
 
   @Override
   public void setForeground(Color color) {
     super.setForeground(color);
     _track.setForeground(color);
   }
 
   public void setThumbColor(Color color) {
     _thumbFigure.setBackgroundColor(color);
     _thumbFigure.setForegroundColor(color);
   }
 
   public void setSparklineColor(Color color) {
     _dataPointColor = color;
     updateLayerColor(_sparkLineLayer, color);
   }
 
   public void setHighlightColor(Color color) {
     _highlightColor = color;
     updateLayerColor(_highlightLayer, color);
   }
 
   public void addSelectionListener(SelectionListener listener) {
     final TypedListener typedListener = new TypedListener(listener);
     addListener(SWT.Selection, typedListener);
     addListener(SWT.DefaultSelection, typedListener);
   }
 
   public void removeSelectionListener(SelectionListener listener) {
     final TypedListener typedListener = new TypedListener(listener);
     removeListener(SWT.Selection, typedListener);
     removeListener(SWT.DefaultSelection, typedListener);
   }
 
   public void refresh() {
     this.layout();
     final Rectangle available = _track.getClientArea();
     _track.getContents().setSize(available.width, available.height);
 
     refreshSparkline();
     refreshHighlights();
     refreshThumb();
     refreshLabel();
   }
 
   private void refreshHighlights() {
     if (_inUpdate)
       return;
 
     final Rectangle available = _track.getClientArea();
     _highlightLayer.setSize(available.width, available.height);
 
     final List<Point> figures = new ArrayList<Point>();
     int previous = 0;
     Point p = null;
     for (int i = _highlight.nextSetBit(0); i >= 0; i = _highlight.nextSetBit(i + 1)) {
       if (p == null || i != previous + 1) {
         p = new Point(i, 1);
         figures.add(p);
       } else {
         p.y++;
       }
       previous = i;
     }
 
     resizeLayer(_highlightLayer, figures.size(), _highlightColor);
 
     final List<?> c = _highlightLayer.getChildren();
     final RectangleFigure[] points = c.toArray(new RectangleFigure[] {});
     for (int i = 0; i < points.length; i++) {
       final Point figure = figures.get(i);
       final int x = computeX(figure.x);
       final int width = computeX(figure.x + figure.y) - x;
 
       final org.eclipse.draw2d.geometry.Rectangle newBounds = new org.eclipse.draw2d.geometry.Rectangle(x, 0, width, available.height);
       if (newBounds.equals(points[i].getBounds()))
         continue;
 
       points[i].setBounds(newBounds);
       points[i].setAlpha(80);
       points[i].invalidate();
     }
   }
 
   private void refreshThumb() {
     final Rectangle available = _track.getClientArea();
     _thumbLayer.setSize(available.width, available.height);
     _labelLayer.setSize(available.width, available.height);
 
     final int x = computeX(_selection - _minimum);
     _thumbFigure.setLocation(new Point(x, 0));
     _thumbFigure.setSize(Math.max(5, available.width * _thumb / computeDataPoints()), available.height);
     _thumbFigure.invalidate();
   }
 
   private void refreshLabel() {
     final GC gc = new GC(this);
     gc.setFont(_labelFigure.getFont());
     final org.eclipse.swt.graphics.Point extent = gc.stringExtent(_labelFigure.getText());
     gc.dispose();
 
     _labelFigure.setSize(extent.x, extent.y);
     final int x = computeX(_selection - _minimum);
     _labelFigure.setLocation(new Point(x + 3, 3));
     _labelFigure.invalidate();
   }
 
   private void refreshSparkline() {
     resizeLayer(_sparkLineLayer, computeDataPoints(), _dataPointColor);
 
     final Rectangle available = _track.getClientArea();
     _sparkLineLayer.setSize(available.width, available.height);
 
     final double max = Math.max(ArraySupport.max(_data), 0.0);
     final double min = Math.min(ArraySupport.min(_data), 0.0);
     final double range = max - min;
 
     final double scale = (double) available.height * 0.9 / range;
     final int origin = (int) (available.height * (max / range + 0.05));
 
     final List<?> c = _sparkLineLayer.getChildren();
     final IFigure[] points = c.toArray(new IFigure[] {});
     int x = 0;
     for (int i = 0; i < points.length; i++) {
       final int x1 = computeX(i + 1);
       final int width = x1 - x;
       final int height = (int) (scale * _data[i]);
       points[i].setVisible(height != 0 && width >= 0);
 
       if (width <= 0)
         continue;
 
       final int y = origin - Math.max(height, 0);
       final org.eclipse.draw2d.geometry.Rectangle bounds = points[i].getBounds();
       final org.eclipse.draw2d.geometry.Rectangle newBounds = new org.eclipse.draw2d.geometry.Rectangle(x, y, width, Math.abs(height));
       if (!bounds.equals(newBounds)) {
         points[i].setBounds(newBounds);
         points[i].invalidate();
       }
 
       x = x1;
     }
   }
 
   private double _lastError = 0.0;
   private int _lastX = 0;
   private int _lastIdx = 0;
 
   private int computeX(int index) {
     if (index == _lastIdx)
       return _lastX;
 
     if (index < _lastIdx) {
       _lastX = 0;
       _lastError = 0.0;
       _lastIdx = 0;
     }
 
     final Rectangle available = _track.getClientArea();
     final double idealPointWidth = (double) available.width / (double) computeDataPoints();
     final int pointWidth = Math.max(1, (int) idealPointWidth);
     final double delta = idealPointWidth - pointWidth;
 
     for (int i = _lastIdx; i < index; i++) {
       final int width = pointWidth + (int) _lastError;
       _lastError = _lastError + delta - (int) _lastError;
 
       if (width <= 0)
         continue;
 
       _lastX += width;
     }
 
     _lastIdx = index;
     return _lastX;
   }
 
   private int computeDataPoints() {
     return _maximum - _minimum + 1;
   }
 
   private void resizeData() {
     final int points = computeDataPoints();
     if (_data.length == points)
       return;
 
     if (_data.length > points) {
       Arrays.fill(_data, points, _data.length, 0.0);
     }
 
     if (_data.length < points) {
       final double[] newData = new double[points];
       System.arraycopy(_data, 0, newData, 0, _data.length);
       _data = newData;
     }
 
     refresh();
   }
 
   private void incrementSelection(int direction) {
     setSelection(_selection + (_increment * direction));
   }
 
   private static IFigure createDataPointFigure(Color color) {
     final RectangleFigure figure = new RectangleFigure();
     figure.setBackgroundColor(color);
     figure.setForegroundColor(color);
     return figure;
   }
 
   private static void resizeLayer(Layer layer, int numObjects, Color color) {
     final List<?> dataPoints = layer.getChildren();
     for (int i = dataPoints.size() - 1; i >= numObjects; i--) {
       layer.remove((IFigure) dataPoints.get(i));
       layer.invalidate();
     }
 
     for (int i = dataPoints.size(); i < numObjects; i++) {
       layer.add(createDataPointFigure(color));
       layer.invalidate();
     }
   }
 
   private static void updateLayerColor(Layer layer, Color color) {
     for (Object o : layer.getChildren()) {
       final IFigure point = (IFigure) o;
       point.setForegroundColor(color);
       point.setBackgroundColor(color);
       point.invalidate();
     }
   }
 
   private final class Listener implements DisposeListener, MouseListener, MouseTrackListener, org.eclipse.draw2d.MouseListener, MouseMotionListener,
       ControlListener {
 
     private Repeater _repeater;
     private Point _startLocation;
     private int _offset = 0;
 
     @Override
     public void widgetDisposed(DisposeEvent e) {
       for (int i = 0; i < _images.length; i++) {
         _images[i].dispose();
       }
     }
 
     @Override
     public void mouseDown(MouseEvent e) {
       if (isDisposed())
         return;
 
       final Object source = e.getSource();
       if (source == _left) {
         incrementSelection(-1);
         createRepeater(-1);
         _labelFigure.setVisible(true);
         return;
       }
 
       if (source == _right) {
         incrementSelection(1);
         createRepeater(1);
         _labelFigure.setVisible(true);
         return;
       }
 
       if (source == _track) {
         final double pct = (double) e.x / (double) _track.getClientArea().width;
         final int points = computeDataPoints();
         final int selection = (int) (points * pct) + _minimum;
         if (selection < _selection || selection >= _selection + _thumb)
           setSelection(selection);
         _labelFigure.setVisible(true);
         return;
       }
     }
 
     @Override
     public void mouseUp(MouseEvent e) {
       if (isDisposed())
         return;
 
       cancelRepeater();
       final Object source = e.getSource();
       if (source == _left || source == _right)
         _labelFigure.setVisible(false);
     }
 
     @Override
     public void mouseDoubleClick(MouseEvent e) {
       // do nothing
     }
 
     @Override
     public void mouseEnter(MouseEvent e) {
       if (isDisposed())
         return;
 
       final Display display = getShell().getDisplay();
       final Object source = e.getSource();
       if (source == _left) {
         _left.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
       }
       if (source == _right) {
         _right.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
       }
     }
 
     @Override
     public void mouseExit(MouseEvent e) {
       if (isDisposed())
         return;
 
       final Object source = e.getSource();
       if (source == _left) {
         _left.setBackground(getBackground());
       }
       if (source == _right) {
         _right.setBackground(getBackground());
       }
     }
 
     @Override
     public void mouseHover(MouseEvent e) {
       // do nothing
     }
 
     @Override
     public void controlMoved(ControlEvent e) {
       e.getSource();
       // do nothing
     }
 
     @Override
     public void controlResized(ControlEvent e) {
       if (isDisposed())
         return;
 
       refresh();
     }
 
     private void cancelRepeater() {
       if (_repeater == null)
         return;
 
       _repeater._cancelled = true;
       _repeater = null;
     }
 
     private void createRepeater(int direction) {
       cancelRepeater();
       _repeater = new Repeater(direction);
     }
 
     @Override
     public void mouseDragged(org.eclipse.draw2d.MouseEvent e) {
       if (isDisposed())
         return;
 
       if (_startLocation != null && e.getSource() == _thumbFigure) {
         e.consume();
         final int diff = e.getLocation().x - _startLocation.x;
         final double pct = (double) diff / (double) _track.getClientArea().width;
 
         final int points = computeDataPoints();
         final int correction = (int) (points * pct);
         setSelection(_selection + correction);
         _startLocation.x = _track.getClientArea().width * _selection / points + _offset;
         return;
       }
     }
 
     @Override
     public void mouseEntered(org.eclipse.draw2d.MouseEvent e) {
       if (isDisposed())
         return;
 
       if (e.getSource() == _thumbFigure) {
         e.consume();
         _thumbFigure.setAlpha(THUMB_ALPHA_OVER);
         _labelFigure.setVisible(true);
       }
     }
 
     @Override
     public void mouseExited(org.eclipse.draw2d.MouseEvent e) {
       if (isDisposed())
         return;
 
       if (e.getSource() == _thumbFigure) {
         e.consume();
         _thumbFigure.setAlpha(THUMB_ALPHA_DEFAULT);
         _labelFigure.setVisible(false);
       }
     }
 
     @Override
     public void mouseHover(org.eclipse.draw2d.MouseEvent e) {
       // do nothing
     }
 
     @Override
     public void mouseMoved(org.eclipse.draw2d.MouseEvent e) {
       // do nothing
     }
 
     @Override
     public void mouseDoubleClicked(org.eclipse.draw2d.MouseEvent e) {
       // do nothing
     }
 
     @Override
     public void mousePressed(org.eclipse.draw2d.MouseEvent e) {
       if (isDisposed())
         return;
 
       if (e.getSource() == _thumbFigure) {
         e.consume();
         _startLocation = e.getLocation();
         _offset = _startLocation.x - _thumbFigure.getLocation().x;
       }
     }
 
     @Override
     public void mouseReleased(org.eclipse.draw2d.MouseEvent e) {
       if (isDisposed())
         return;
 
       if (e.getSource() == _thumbFigure) {
         e.consume();
         _startLocation = null;
       }
     }
 
   }
 
   private class Repeater implements Runnable {
 
     private final int _direction;
     private int _iterations = 0;
     private boolean _cancelled = false;
 
     private Repeater(int direction) {
       _direction = direction;
       schedule();
     }
 
     @Override
     public void run() {
       if (isDisposed() || _cancelled)
         return;
 
       final int speed = Math.max(Math.min(_iterations++ / 8, 64), 1);
       incrementSelection(_direction * speed);
       schedule();
     }
 
     private void schedule() {
       getShell().getDisplay().timerExec(Math.max(20, 400 / Math.max(1, _iterations)), this);
     }
   }
 }
