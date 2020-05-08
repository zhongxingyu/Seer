 package bigimp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.TouchEvent;
 import org.eclipse.swt.events.TouchListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.swt.widgets.Shell;
 
 //import com.ibm.icu.util.CharsTrie.Iterator;
 
 public class SwtExample {
 
   private Display display;
   private Shell shell;
   private Group group;
   private Image image;
   private Canvas canvas;
   private Rectangle rect = new Rectangle(0, 0, 200, 200);
   private List<Rectangle> rects = new ArrayList<Rectangle>();
   private boolean mouseDown = false;
   private MouseHandler mouseHandler;
   private Button button;
   private Composite buttonBar;
 
   private class PaintHandler implements PaintListener {
     public void paintControl (PaintEvent e) {
       Rectangle b = image.getBounds();
       Rectangle cb = canvas.getBounds();
       double s = 1.0;
       if ( b.width > b.height ) {
         s = ((double) cb.width) / b.width; 
       }
       else {
         s = ((double) cb.height) / b.height;
       }
       e.gc.drawImage(image, 0, 0, b.width, b.height, 0, 0, (int) (b.width * s), (int) (b.height * s));
       e.gc.setForeground(display.getSystemColor(SWT.COLOR_RED)); 
       System.out.format("Now we have %d rects\n", rects.size());
       Iterator<Rectangle> iter = rects.iterator();
       while( iter.hasNext() ) {
     	  Rectangle r = iter.next();
     	  System.out.println("   r = " + r);
     	  e.gc.drawRectangle(r);
       }      
     }
   }
 	
 	private class MouseHandler implements MouseListener, MouseMoveListener {
 
     @Override
     public void mouseDoubleClick(MouseEvent arg0) {
     }
 
     @Override
     public void mouseDown(MouseEvent arg) {
       rect.x = arg.x;
       rect.y = arg.y;
       rect.width = 0;
       rect.height = 0;
       mouseDown = true;
       canvas.redraw();
     }
 
     @Override
     public void mouseUp(MouseEvent arg) {
       rect.width = arg.x - rect.x;
       rect.height = arg.y - rect.y;
       mouseDown = false;
       System.out.println("adding a rect");
      // No copy constructor for Rectangle??
      rects.add( new Rectangle(rect.x, rect.y, rect.width, rect.height ) );
       canvas.redraw();
     }
 
     @Override
     public void mouseMove(MouseEvent arg) {
       if ( mouseDown ) {
         rect.width = arg.x - rect.x;
         rect.height = arg.y - rect.y;
         canvas.redraw();
       }
     }
 	  
 	}
 	
 	private class OnImageSaveAction implements SelectionListener, TouchListener {
 
     @Override
     public void widgetDefaultSelected(SelectionEvent arg0) {
       System.out.println("Button selected!");
     }
 
     @Override
     public void widgetSelected(SelectionEvent arg0) {
       System.out.println("Button selected 2!");
     }
 
     @Override
     public void touch(TouchEvent arg0) {
       System.out.println("Button selected 3!");
     }
 	  
 	}
 
 
   public static void main(String[] args) throws FileNotFoundException {
 	  
 	  SwtExample example = new SwtExample();
 	  example.init();
 	  example.run();
 	  example.dispose();
   }
 	
 	public void run() {
     while (!shell.isDisposed()) {
       if (!display.readAndDispatch()) display.sleep();
     }
 	}
 
   private void dispose() {
     image.dispose();
     display.dispose();
   }
 
   private void init() {
     GridLayout shellLayout = new GridLayout();
     shellLayout.numColumns = 1;
     
     RowLayout buttonBarLayout = new RowLayout();
     buttonBarLayout.type = SWT.HORIZONTAL;
     buttonBarLayout.fill = true;
     buttonBarLayout.pack = true;
     
     display = new Display();
 
     shell = new Shell(display);
     shell.setLayout(shellLayout);
     
     buttonBar = new Composite(shell, 0);
     buttonBar.setLayout(buttonBarLayout);
     GridData buttonBarGridData = new GridData();
     buttonBarGridData.horizontalAlignment = GridData.FILL_HORIZONTAL;
     buttonBarGridData.grabExcessHorizontalSpace = true;
     buttonBarGridData.grabExcessVerticalSpace = false;
     buttonBar.setLayoutData(buttonBarGridData);
     
     button = new Button(buttonBar, SWT.PUSH);
     button.setText("Save Image");
     button.addSelectionListener(new OnImageSaveAction());
     button.addTouchListener(new OnImageSaveAction());
     
     loadImage();
     group = new Group (shell, SWT.NONE);
     group.setLayout(new FillLayout());
     group.setText ("a square");
     GridData groupGridData = new GridData();
     groupGridData.horizontalAlignment = GridData.FILL;
     groupGridData.verticalAlignment = GridData.FILL;
     groupGridData.grabExcessHorizontalSpace = true;
     groupGridData.grabExcessVerticalSpace = true;
     group.setLayoutData(groupGridData);
     
     canvas = new Canvas(group, SWT.NONE);
     canvas.addPaintListener(new PaintHandler());
     mouseHandler = new MouseHandler();
     canvas.addMouseListener(mouseHandler);
     canvas.addMouseMoveListener(mouseHandler);
     
     shell.pack();
     shell.open();
   }
 
   private void loadImage() {
     try {
       InputStream imageStream = new FileInputStream(new File("data/images/1277383675Image000009.jpg"));
       image = new Image(display, imageStream);
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 
 }
