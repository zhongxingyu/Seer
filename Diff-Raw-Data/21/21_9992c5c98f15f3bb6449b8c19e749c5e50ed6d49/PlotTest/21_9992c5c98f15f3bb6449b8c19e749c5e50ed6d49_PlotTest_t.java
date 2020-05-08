 import java.awt.*;
 import java.awt.event.*;
 import java.io.File;
 import java.util.ArrayList;
 import javax.swing.*;
 
 import edu.mines.jtk.awt.*;
 import edu.mines.jtk.dsp.*;
 import edu.mines.jtk.util.Cdouble;
 import edu.mines.jtk.mosaic.*;
 import static edu.mines.jtk.util.ArrayMath.*;
 
 public class PlotTest{
 
   public static void main(String[] args){
     SwingUtilities.invokeLater(new Runnable() {
       public void run() {
         new PlotTest();
       }
     });
   }
 
   // Location and size of overlay plot.
   private static final int O_X = 100;
   private static final int O_Y = 0;
   private static final int O_WIDTH = 520;
   private static final int O_HEIGHT = 550;
 
   // Location and size of response plot.
   private static final int RP_X = O_X+O_WIDTH;
   private static final int RP_Y = 0;
   private static final int RP_WIDTH = 520;
   private static final int RP_HEIGHT = 550;
   
   // Plot of source/receivers
   // private ArrayList<Phone> _shots;
   private ArrayList<Phone> _recs;
   private BasePlot _bp;
   private ResponsePlot _rp;
 
   private PlotTest(){
     // _shots = new ArrayList<Phone>(0);
     _recs = new ArrayList<Phone>(0);
     _bp = new BasePlot();
     _rp = new ResponsePlot();
   }
 
   private void addPhone(Phone phone) {
     _recs.add(phone);
     _bp.updateBPView();
   }
 
 
 ///////////////////////////////////////////////////////////////////////////
 
   private class BasePlot {
 
     private PlotFrame _plotFrame;
     private PlotPanel _plotPanel;
     private PointsView _baseView;
     
     private BasePlot() {
 
       // The plot panel.
       _plotPanel = new PlotPanel();
       _plotPanel.setTitle("Base Plot Test");
       _plotPanel.setHLabel("Easting (UTM)");
       _plotPanel.setVLabel("Northing (UTM)");
       _plotPanel.setHLimits(-100.0,100.0);
       _plotPanel.setVLimits(-100.0,100.0);
 
       // A grid view for horizontal and vertical lines (axes).
       _plotPanel.addGrid("H0-V0-");
 
       // A plot frame has a mode for zooming in tiles or tile axes.
       _plotFrame = new PlotFrame(_plotPanel);
       TileZoomMode tzm = _plotFrame.getTileZoomMode();
 
       // We add two more modes for editing poles and zeros.
       ModeManager mm = _plotFrame.getModeManager();
       AddMode am = new AddMode(mm); // for test points
       // PoleZeroMode zm = new PoleZeroMode(mm,false);  // for zeros
 
       // The menu bar includes a mode menu for selecting a mode.
       JMenu fileMenu = new JMenu("File");
       fileMenu.setMnemonic('F');
       fileMenu.add(new SaveAsPngAction(_plotFrame)).setMnemonic('a');
       fileMenu.add(new ExitAction()).setMnemonic('x');
       JMenu modeMenu = new JMenu("Mode");
       modeMenu.setMnemonic('M');
       modeMenu.add(new ModeMenuItem(tzm));
       modeMenu.add(new ModeMenuItem(am));
       JMenuBar menuBar = new JMenuBar();
       menuBar.add(fileMenu);
       menuBar.add(modeMenu);
       _plotFrame.setJMenuBar(menuBar);
 
       // The tool bar includes toggle buttons for selecting a mode.
       JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
       toolBar.setRollover(true);
       toolBar.add(new ModeToggleButton(tzm));
       toolBar.add(new ModeToggleButton(am));
       _plotFrame.add(toolBar,BorderLayout.WEST);
 
       // Initially, enable editing of poles.
       // pm.setActive(true);
 
       // Make the plot frame visible.
       _plotFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       _plotFrame.setLocation(O_X,O_Y);
       _plotFrame.setSize(O_WIDTH,O_HEIGHT);
       _plotFrame.setFontSizeForPrint(8,240);
       _plotFrame.setVisible(true);
 
     }
 
     // Makes poles view consistent with the list of poles.
     private void updateBPView() {
       int np = _recs.size();
       float[] xp = new float[np];
       float[] yp = new float[np];
       boolean[] sel = new boolean[np];
       for (int ip=0; ip<np; ++ip) {
         Phone p = _recs.get(ip);
         xp[ip] = (float)p.x;
         yp[ip] = (float)p.y;
         sel[ip] = p.selected;
       }
       if (_baseView==null) {
         _baseView = _plotPanel.addPoints(xp,yp);
         _baseView.setMarkStyle(PointsView.Mark.CROSS);
         _baseView.setLineStyle(PointsView.Line.NONE);
       } else {
         _baseView.set(xp,yp);
       }
     }
 
  }
 
   ///////////////////////////////////////////////////////////////////////////
 
   private class ResponsePlot {
 
     private PlotPanel _plotPanelH;
     private PlotFrame _plotFrame;
     private SequenceView _hView;
     private PointsView _pView;
 
     // The amplitude response can be in decibels (db).
     private ResponsePlot() {
 
       // One plot panel for the impulse response.
       _plotPanelH = new PlotPanel();
       _plotPanelH.setHLabel("Easting (UTM)");
       _plotPanelH.setVLabel("Time (ms)");
       _plotPanelH.setTitle("Title");
 
       // This first update constructs a sequence view for the impulse 
       // response, and a points view for amplitude and phase responses.
       // updateViews();
 
       _plotFrame = new PlotFrame(_plotPanelH);
 
       // The menu bar.
       JMenu fileMenu = new JMenu("File");
       fileMenu.setMnemonic('F');
       fileMenu.add(new SaveAsPngAction(_plotFrame)).setMnemonic('a');
       fileMenu.add(new ExitAction()).setMnemonic('x');
       JMenuBar menuBar = new JMenuBar();
       menuBar.add(fileMenu);
       _plotFrame.setJMenuBar(menuBar);
 
       // Make the plot frame visible.
       _plotFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       _plotFrame.setLocation(RP_X,RP_Y);
       _plotFrame.setSize(RP_WIDTH,RP_HEIGHT);
       _plotFrame.setFontSizeForPrint(8,240);
       _plotFrame.setVisible(true);
     }
 
   }
 
   ///////////////////////////////////////////////////////////////////////////
 
   private class AddMode extends Mode {
     public AddMode(ModeManager modeManager) {
       super(modeManager);
         setName("Add Receivers");
         // setIcon(loadIcon(PolesAndZerosDemo.class,"Poles16.png"));
         setMnemonicKey(KeyEvent.VK_X);
         setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_X,0));
         setShortDescription("Add (Shift), remove (Ctrl), or drag receivers");
       }
     
     // When this mode is activated (or deactivated) for a tile, it simply 
     // adds (or removes) its mouse listener to (or from) that tile.
     protected void setActive(Component component, boolean active) {
       if (component instanceof Tile) {
         if (active) {
           component.addMouseListener(_ml);
         } else {
           component.removeMouseListener(_ml);
         }
       }
     }
   }
 
     private boolean _editing; // true, if currently editing
     private Tile _tile; // tile in which editing began
 
     // Handles mouse pressed and released events.
     private MouseListener _ml = new MouseAdapter() {
       public void mousePressed(MouseEvent e) {
         if (e.isShiftDown()) {
           add(e);
         } else if (e.isControlDown()) {
           remove(e);
         } else {
           if (beginEdit(e)) {
             _editing = true;
             _tile.addMouseMotionListener(_mml);
           }
         }
       }
       public void mouseReleased(MouseEvent e) {
         if (_editing) {
           _tile.removeMouseMotionListener(_mml);
           endEdit(e);
           _editing = false;
         }
       }
     };
 
   // Handles mouse dragged events.
     private MouseMotionListener _mml = new MouseMotionAdapter() {
       public void mouseDragged(MouseEvent e) {
         if (_editing)
           duringEdit(e);
       }
     };
 
   // Converts an point (x,y) in pixels to a complex number z.
     private Cdouble pointToComplex(int x, int y) {
       Transcaler ts = _tile.getTranscaler();
       Projector hp = _tile.getHorizontalProjector();
       Projector vp = _tile.getVerticalProjector();
       double xu = ts.x(x);
       double yu = ts.y(y);
       double xv = hp.v(xu);
       double yv = vp.v(yu);
       return roundToReal(new Cdouble(xv,yv));
     }
 
     // Converts  complex number z to an point (x,y) in pixels.
    private Phone pixelToPoint(double x, double y) {
      Transcaler ts = _tile.getTranscaler();
      Projector hp = _tile.getHorizontalProjector();
      Projector vp = _tile.getVerticalProjector();
      double xu = hp.u(x);
      double yu = vp.u(y);
      double xp = ts.x(xu);
      double yp = ts.y(yu);
      return new Phone(xp,yp);
    }

    // Converts  complex number z to an point (x,y) in pixels.
     private Point complexToPoint(Cdouble z) {
       Transcaler ts = _tile.getTranscaler();
       Projector hp = _tile.getHorizontalProjector();
       Projector vp = _tile.getVerticalProjector();
       double xu = hp.u(z.r);
       double yu = vp.u(z.i);
       int xp = ts.x(xu);
       int yp = ts.y(yu);
       return new Point(xp,yp);
     }
 
     // If the specified complex number c is nearly on the real axis 
     // (within a small fixed number of pixels), then rounds this 
     // complex number to the nearest real number by setting the 
     // imaginary part to zero.
     private Cdouble roundToReal(Cdouble c) {
       Cdouble cr = new Cdouble(c.r,0.0);
       Point pr = complexToPoint(cr);
       Point p = complexToPoint(c);
       return (abs(p.y-pr.y)<6)?cr:c;
     }
 
     // Determines whether a specified point (x,y) is within a small
     // fixed number of pixels to the specified complex number c.
     private boolean closeEnough(int x, int y, Cdouble c) {
       Point p = complexToPoint(c); 
       return abs(p.x-x)<6 && abs(p.y-y)<6;
     }
 
     // Adds a pole or zero at mouse coordinates (x,y).
     // TODO: Convert pixels to actual
     private void add(MouseEvent e) {
       _tile = (Tile)e.getSource();
       double x = e.getX();
       double y = e.getY();
      Phone p = pixelToPoint(x,y);
      System.out.println("x: " + x + " y: " + y + " p.x: " + p.x + " p.y: " + p.y);
      addPhone(p);      
     }
     
     private void remove(MouseEvent e) {
       _tile = (Tile)e.getSource();
       int x = e.getX();
       int y = e.getY();
     }
     
     // Begins editing of an existing pole or zero, if close enough.
     // Returns true, if close enough so that we have begun editing; 
     // false, otherwise.
     private boolean beginEdit(MouseEvent e) {
       _tile = (Tile)e.getSource();
       int x = e.getX();
       int y = e.getY();
       return false;
     }
 
   private void duringEdit(MouseEvent e) {
       int x = e.getX();
       int y = e.getY();
       // Cdouble z = pointToComplex(x,y);
       // if (_poles) {
       //  movePole(_zedit,z);
       // } else {
       //  moveZero(_zedit,z);
       // }
       // _zedit = z;
     }
 
     // Called when done editing a pole or zero.
     private void endEdit(MouseEvent e) {
       duringEdit(e);
       _editing = false;
     } 
  
   ///////////////////////////////////////////////////////////////////////////
   
   // Actions common to both plot frames.
   private class ExitAction extends AbstractAction {
     private ExitAction() {
       super("Exit");
     }
     public void actionPerformed(ActionEvent event) {
       System.exit(0);
     }
   }
   private class SaveAsPngAction extends AbstractAction {
     private PlotFrame _plotFrame;
     private SaveAsPngAction(PlotFrame plotFrame) {
       super("Save as PNG");
       _plotFrame = plotFrame;
     }
     public void actionPerformed(ActionEvent event) {
       JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
       fc.showSaveDialog(_plotFrame);
       File file = fc.getSelectedFile();
       if (file!=null) {
         String filename = file.getAbsolutePath();
         _plotFrame.paintToPng(300,6,filename);
       }
     }
   }
 
   public class Phone {
     Phone(double x, double y){
      this.x = x; 
      this.y = y;
     }
 
     public double x, y, elev;
     public Sampling s;
     public boolean selected;
     // public Shot s;
   }
 
 }
 
