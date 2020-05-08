 package rocks6205.editor.mvc;
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import rocks6205.editor.events.SVGEditPanMouseAdaptor;
 import rocks6205.editor.viewcomponents.SVGEditorBottomToolbar;
 import rocks6205.editor.viewcomponents.SVGEditorDeleteAccessoryPanel;
import rocks6205.editor.viewcomponents.SVGEditorCanvasViewport;
 import rocks6205.editor.viewcomponents.SVGEditorMenubar;
 import rocks6205.editor.viewcomponents.SVGEditorTopToolbar;
 import rocks6205.system.properties.SVGEditorTheme;
 
 //~--- JDK imports ------------------------------------------------------------
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JViewport;
 
 /**
  * A class defining how the main user interface should look like.
  * <p>
  *
  * @author Toh Huey Jing
  * @author Cheow Yeong Chi
  *
  * @since 1.2
  */
 public class SVGEditorView extends JFrame {
     private static final long serialVersionUID = 6764861773639452353L;
 
     /*
      * PROPERTIES
      */
     private float zoomScale;
 
     /**
      * Model object
      */
     private SVGEditorModel model;
 
     /**
      * Controller object
      */
     private SVGEditorViewController controller;
 
     /*
      * GUI COMPONENTS
      */
     SVGEditorMenubar              menuBar;
     SVGEditorTopToolbar           topTool;
     SVGEditorBottomToolbar        bottomTool;
     SVGEditorDeleteAccessoryPanel delete;
     JPanel                        panelTop, panelBottom;
     SVGEditorEditingPanel         editPanel;
     JViewport                     viewport;
     Container                     container;
     JScrollPane                   scrollPane;
     Dimension                     renderAreaSize;
     LSTabbedPane                  tabbedPane;
 
     /*
      * ACTION COMPONENTS
      */
 
     /**
      * Handle pan events.
      */
     private SVGEditPanMouseAdaptor panListener;
 
     /*
      * CONSTRUCTOR
      */
 
     /**
      * Construct an <code>SVGView</code> instance with components initialised
      * and properly customised.
      */
     public SVGEditorView() {
         super();
         initialise();
         customise();
     }
 
     /**
      * Initialisation of GUI components.
      */
     private void initialise() {
         menuBar        = new SVGEditorMenubar(this);
         topTool        = new SVGEditorTopToolbar(this);
         bottomTool     = new SVGEditorBottomToolbar(this);
         delete         = new SVGEditorDeleteAccessoryPanel(this);
         panelTop       = new JPanel();
         panelBottom    = new JPanel();
         editPanel      = new SVGEditorEditingPanel(this);
         viewport       = new JViewport();
         container      = getContentPane();
         renderAreaSize = new Dimension(0, 0);
         scrollPane     = new JScrollPane(editPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                          JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
         tabbedPane  = new LSTabbedPane();
         panListener = new SVGEditPanMouseAdaptor();
     }
 
     /**
      * Customisation of GUI components.
      *
      */
     private void customise() {
         setSize(1000, 1000);
         setTitle("SVG Editor");
         setupTabbedPane();
         scrollPane.setBounds(new Rectangle(renderAreaSize));
         editPanel.setBackground(Color.WHITE);
         container.setLayout(new BorderLayout());
         panelTop.setLayout(new BorderLayout());
         panelBottom.setLayout(new BorderLayout());
         panelTop.add(topTool, BorderLayout.WEST);
         panelBottom.add(bottomTool, BorderLayout.WEST);
         panelBottom.add(delete, BorderLayout.EAST);
         setVisible(true);
         setMinimumSize(new Dimension(700, 700));
         setClosingEvent();
         setJMenuBar(menuBar);
         container.add(scrollPane, BorderLayout.CENTER);
         container.add(panelTop, BorderLayout.NORTH);
         container.add(panelBottom, BorderLayout.SOUTH);
         viewport = scrollPane.getViewport();
         viewport.addMouseListener(panListener);
         viewport.addMouseMotionListener(panListener);
         panelTop.setBackground(SVGEditorTheme.MASTER_DEFAULT_COLOR);
         panelBottom.setBackground(SVGEditorTheme.MASTER_DEFAULT_COLOR);
     }
 
     /**
      * Prompt exit confirmation while user clicks on 'x' button on the window
      */
     private void setClosingEvent() {
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent winEvt) {
                 int closeCf = JOptionPane.showConfirmDialog(null, "Exit SVG Editor?", "Confirm exit",
                                   JOptionPane.WARNING_MESSAGE);
 
                 if (closeCf == JOptionPane.YES_OPTION) {
                     System.exit(0);
                 }
             }
         });
     }
 
     /*
      * ACCESSORS
      */
 
     /**
      * @return Model object
      */
     public SVGEditorModel getModel() {
         return model;
     }
 
     /**
      * @return Controller object
      */
     public SVGEditorViewController getController() {
         return controller;
     }
 
     /*
      * MUTATORS
      */
 
     /**
      * @param controller Controller object
      */
     public void setController(SVGEditorViewController controller) {
         this.controller = controller;
     }
 
     /**
      * @param model Model object
      */
     public void setModel(SVGEditorModel model) {
         this.model = model;
     }
 
     /**
      * <p>Updates the image canvas whenever a change is made to the observed object
      * which is the <code>SVGModel.</code></p>
      * {@inheritDoc}
      */
 
 //  public void update(Observable o, Object arg) {
 //      if (o instanceof SVGEditorModel) {
 //          float w = ((SVGEditorModel) o).getSVGElement().getWidth().getValue();
 //          float h = ((SVGEditorModel) o).getSVGElement().getHeight().getValue();
 //
 //          renderAreaSize.height = (int) h;
 //          renderAreaSize.width  = (int) w;
 //          scrollPane.setPreferredSize(renderAreaSize);
 //          renderPanel.setPreferredSize(renderAreaSize);
 //          renderPanel.setCanvas((SVGImageCanvas) arg);
 //          renderPanel.revalidate();
 //          renderPanel.repaint();
 //          scrollPane.setViewportView(renderPanel);
 //          scrollPane.revalidate();
 //          scrollPane.repaint();
 //      }
 //  }
     public void update() {
 
         // TODO Auto-generated method stub
     }
 
     public float getZoomScale() {
         return zoomScale;
     }
 
    private void setupTabbedPane() {
        
    }
 }
