 package ssl.editors.frm;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseWheelListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Slider;
 
 import fdk.frm.FRMFrame;
 
 public class FRMList extends Composite {
 
     private ArrayList<Image>              m_cachedImages       = new ArrayList<Image>();
     // private IFrmDescriptor[] m_descs;
     private int                           m_cellW;
     private int                           m_cellH;
     private int                           m_topLeftCell        = 0;
     private int                           m_visibleCols;
     private int                           m_visibleRows;
     private int                           m_cellSpacingW       = 10;
     private int                           m_cellSpacingH       = 20;
     private PaletteData                   m_pal;
     private Slider                        m_slider;
     private Canvas                        m_canvas;
 
     private IFrmListLabelProvider         m_labelProvider;
     private IFrmListContentProvider       m_contentProvider;
     private Object                        m_input;
     private Object[]                      m_elements;
     private Object                        m_selected;
 
     private ArrayList<ISelectionListener> m_selectionListeners = new ArrayList<ISelectionListener>();
    private Timer                         m_preloader          = new Timer();
 
     /**
      * Create the composite.
      * 
      * @param parent
      * @param style
      */
     public FRMList(Composite parent, int style) {
         super(parent, style);
         GridLayout gridLayout = new GridLayout(2, false);
         gridLayout.marginHeight = 0;
         gridLayout.marginWidth = 0;
         gridLayout.verticalSpacing = 0;
         gridLayout.horizontalSpacing = 0;
         setLayout(gridLayout);
 
         m_canvas = new Canvas(this, SWT.NO_BACKGROUND);
         m_canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
         m_canvas.addControlListener(new ControlAdapter() {
             @Override
             public void controlResized(ControlEvent e) {
                 recalcSize();
             }
         });
         m_canvas.setBackground(new Color(getDisplay(), new RGB(90, 90, 90)));
         m_canvas.addPaintListener(new PaintListener() {
             @Override
             public void paintControl(PaintEvent e) {
                 final Color bg = new Color(getDisplay(), 90, 90, 90);
                 final Image image = new Image(getDisplay(), m_canvas.getBounds());
                 final GC gcImage = new GC(image);
 
                 gcImage.setBackground(bg);
                 gcImage.fillRectangle(0, 0, m_canvas.getSize().x, m_canvas.getSize().y);
 
                 for (int i = 0; i < m_cachedImages.size(); i++) {
                     boolean selected = m_elements[m_topLeftCell + i] == m_selected ? true : false;
                     redrawCell(i, gcImage, selected);
                 }
                 e.gc.drawImage(image, 0, 0);
             }
         });
 
         addMouseWheelListener(new MouseWheelListener() {
             @Override
             public void mouseScrolled(MouseEvent e) {
                 m_slider.setSelection(m_slider.getSelection() - e.count);
                 scroll();
             }
         });
 
         addSelectionListener(new ISelectionListener() {
             @Override
             public void select(Object selected) {
                 m_selected = selected;
                 m_canvas.redraw();
             }
         });
 
         m_canvas.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseUp(MouseEvent e) {
                 int x = e.x / (m_cellW + m_cellSpacingW);
                 int y = e.y / (m_cellH + m_cellSpacingH);
 
                 if (x >= m_visibleCols || y >= m_visibleRows) {
                     return;
                 }
 
                 int id = m_topLeftCell + x + y * m_visibleCols;
                 if (id >= m_elements.length) {
                     return;
                 }
 
                 Object o = m_elements[id];
                 for (ISelectionListener sl : m_selectionListeners) {
                     sl.select(o);
                 }
             }
         });
 
         addKeyListener(new KeyListener() {
             @Override
             public void keyReleased(KeyEvent e) {}
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.keyCode == SWT.ARROW_DOWN) {
                     m_slider.setSelection(m_slider.getSelection() + 1);
                 }
                 if (e.keyCode == SWT.ARROW_UP) {
                     m_slider.setSelection(m_slider.getSelection() - 1);
                 }
                 if (e.keyCode == SWT.ARROW_RIGHT) {
                     m_slider.setSelection(m_slider.getSelection() + m_visibleRows);
                 }
                 if (e.keyCode == SWT.ARROW_LEFT) {
                     m_slider.setSelection(m_slider.getSelection() - m_visibleRows);
                 }
                 scroll();
             }
         });
 
         m_slider = new Slider(this, SWT.VERTICAL);
         m_slider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
         m_slider.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 scroll();
             }
         });
     }
 
     protected void scroll() {
         if ((m_topLeftCell - m_slider.getSelection() * m_visibleCols) != 0) {
             m_topLeftCell = m_slider.getSelection() * m_visibleCols;
             int ns = m_elements.length - m_topLeftCell;
             int vc = m_visibleCols * m_visibleRows;
             ns = ns < 0 ? 0 : ns;
             ns = ns > vc ? vc : ns;
             cacheImages(ns);
             m_canvas.redraw();
         }
     }
 
     @Override
     public void dispose() {
         for (Image img : m_cachedImages) {
             img.dispose();
         }
        try {
            m_preloader.cancel();
        } catch (Exception e) {}
         super.dispose();
     }
 
     public void setLabelProvider(IFrmListLabelProvider lp) {
         m_labelProvider = lp;
     }
 
     public void setContentProvider(IFrmListContentProvider cp) {
         m_contentProvider = cp;
     }
 
     private void preloadImages(final Object[] lst) {
        m_preloader.cancel();
        m_preloader = new Timer();
        m_preloader.schedule(new TimerTask() {
             @Override
             public void run() {
                 for (Object o : lst) {
                     m_labelProvider.getImage(o);
                 }
             }
         }, 0);
     }
 
     public void setInput(Object input) {
         m_input = input;
         m_contentProvider.inputChanged(this, input, m_input);
         m_elements = m_contentProvider.getElements();
         preloadImages(m_elements);
         recalcSize();
     }
 
     @Override
     protected void checkSubclass() {}
 
     public void setPal(PaletteData pal) {
         m_pal = pal;
     }
 
     public void setCellSize(int w, int h) {
         m_cellW = w;
         m_cellH = h;
     }
 
     @Override
     public void layout() {
         super.layout();
         recalcSize();
     }
 
     protected void recalcSize() {
         int w = getBounds().width;
         int h = getBounds().height;
         m_visibleCols = w / (m_cellW + m_cellSpacingW);
         m_visibleRows = h / (m_cellH + m_cellSpacingH);
         int visibleCells = m_visibleCols * m_visibleRows;
 
         int rest = m_elements.length - m_topLeftCell;
         visibleCells = rest < visibleCells ? rest : visibleCells;
 
         cacheImages(visibleCells);
 
         if (visibleCells != 0) {
             int rows = (int) Math.ceil((float) m_elements.length / (float) m_visibleCols);
             m_slider.setMaximum(rows + m_visibleRows + 2);
         }
     }
 
     protected void redrawCell(int idx, GC gc, boolean selected) {
         int x = m_cellSpacingW + (idx % m_visibleCols) * (m_cellW + m_cellSpacingW);
         int y = m_cellSpacingH + (idx / m_visibleCols) * (m_cellH + m_cellSpacingH);
         int w = m_cachedImages.get(idx).getBounds().width;
         int h = m_cachedImages.get(idx).getBounds().height;
         float imgAspect = (float) w / (float) h;
 
         int drawW = w;
         int drawH = h;
         if (drawW > m_cellW) {
             drawW = m_cellW;
             drawH = (int) (drawW / imgAspect);
         }
         if (drawH > m_cellW) {
             drawH = m_cellH;
             drawW = (int) (drawH * imgAspect);
         }
 
         int drawX = x + (m_cellW - drawW) / 2;
         int drawY = y + (m_cellH - drawH) / 2;
 
         final Color bg = new Color(getDisplay(), 65, 110, 155);
         final Color bg1 = new Color(getDisplay(), 90, 90, 90);
         final Color font = new Color(getDisplay(), 251, 255, 125);
         final Color black = new Color(getDisplay(), 0, 0, 0);
 
         gc.setBackground(bg);
         gc.setForeground(black);
         gc.fillRectangle(x, y, m_cellW, m_cellH);
         gc.drawRectangle(x, y, m_cellW, m_cellH);
         if (selected) {
             gc.setForeground(font);
             gc.drawRectangle(x - 1, y - 1, m_cellW + 2, m_cellH + 2);
         }
 
         if (drawW != w || drawH != h) {
             gc.setInterpolation(SWT.LOW);
         }
         gc.drawImage(m_cachedImages.get(idx), 0, 0, w, h, drawX, drawY, drawW, drawH);
         gc.setInterpolation(SWT.DEFAULT);
 
         gc.setBackground(bg1);
         gc.setForeground(black);
         String text = m_labelProvider.getText(m_elements[m_topLeftCell + idx]);
         gc.setClipping(x, y + m_cellH, m_cellW, 18);
         gc.drawText(text, x + 1 + 5, y + m_cellH + 2);
         gc.setForeground(font);
         gc.drawText(text, x + 5, y + m_cellH + 1, true);
         gc.setClipping((Rectangle) null);
         gc.setBackground(bg);
     }
 
     protected void cacheImages(int size) {
 
         if (m_cachedImages != null) {
             for (Image img : m_cachedImages) {
                 img.dispose();
             }
         }
         m_cachedImages.clear();
         for (int i = 0; i < size; i++) {
             FRMFrame frm = m_labelProvider.getImage(m_elements[m_topLeftCell + i]);
             m_cachedImages.add(FID.getImage(frm.getWidth(), frm.getHeight(), frm.getData(), m_pal));
         }
     }
 
     public void addSelectionListener(ISelectionListener sl) {
         m_selectionListeners.add(sl);
     }
 
     public Object getSelection() {
         return m_selected;
     }
 
 }
