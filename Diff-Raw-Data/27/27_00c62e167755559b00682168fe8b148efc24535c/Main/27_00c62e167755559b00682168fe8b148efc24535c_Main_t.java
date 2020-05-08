 package il.ac.idc.jdt.gui2;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Iterables;
 import com.google.common.eventbus.EventBus;
 import il.ac.idc.jdt.IOParsers;
 import il.ac.idc.jdt.Point;
 import il.ac.idc.jdt.extra.IOUtils;
 import il.ac.idc.jdt.extra.constraint.ConstrainedDelaunayTriangulation;
 import il.ac.idc.jdt.extra.constraint.datamodel.Line;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import javax.annotation.Nullable;
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.EventObject;
 import java.util.List;
 import java.util.Set;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.collect.Iterables.concat;
 import static com.google.common.collect.Iterables.transform;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.collect.Sets.newHashSet;
 import static il.ac.idc.jdt.extra.IOUtils.storeSegments;
 import static il.ac.idc.jdt.gui2.CanvasPanel.ClearEvent;
 import static il.ac.idc.jdt.gui2.CanvasPanel.LoadPointsEvent;
 import static il.ac.idc.jdt.gui2.SegmentsPanel.*;
 import static java.lang.String.format;
 import static javax.swing.JOptionPane.showMessageDialog;
 import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
 
 /**
  * Created by IntelliJ IDEA.
  * User: daniels
  * Date: 5/27/12
  */
 public class Main extends JFrame
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
     private static final Function<Line, Iterable<Point>> toPoints = new Function<Line, Iterable<Point>>()
     {
         @Override
         public Iterable<Point> apply(Line segment)
         {
             return newArrayList(segment.points());
         }
     };
 
    private static final Function<Point, Point> toZetLessPoints = new Function<Point, Point>()
    {
        @Override
        public Point apply(Point input)
        {
            return new Point(input.getX(), input.getY());
        }
    };

     CanvasPanel canvasPanel;
 
    StatusPanel statusPanel;
     SegmentsPanel segmentsPanel;
     final EventBus eventBus = new EventBus();
     final MouseAdapter mouseAdapter = new MouseAdapter() {};
 
     Main()
     {
         initFrame();
         eventBus.register(canvasPanel);
         eventBus.register(statusPanel);
         eventBus.register(segmentsPanel);
         doShow();
     }
 
     private void doShow()
     {
         centerOnScreen();
         setVisible(true);
     }
 
     private void centerOnScreen()
     {
         int widthWindow = getWidth();
         int heightWindow = getHeight();
 
         Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
         int X = (screen.width / 2) - (widthWindow / 2); // Center horizontally.
         int Y = (screen.height / 2) - (heightWindow / 2); // Center vertically.
 
         setBounds(X, Y, widthWindow, heightWindow);
     }
 
     private void initFrame()
     {
         setTitle("Delaunay Triangulation GUI");
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setResizable(false);
         setLayout(new BorderLayout());
 
         // menu
         setJMenuBar(menuBar(
                 menu("File",
                         menuItem(loadPointsFileAction()),
                         menuItem(loadSegmentsFileAction()),
                         menuItem(saveSegmentsFileAction()),
                         new JSeparator(),
                         menuItem(clearAction())),
                 menu("Triangulation",
                         menuItem(runTriangulationAction()),
                         menuItem(resetTriangulationAction()))));
 
         // canvas panel
         canvasPanel = new CanvasPanel(eventBus);
         add(canvasPanel, BorderLayout.LINE_START);
 
         // segments panel
         segmentsPanel = new SegmentsPanel(eventBus);
         add(segmentsPanel, BorderLayout.LINE_END);
 
         // status bar panel
         statusPanel = new StatusPanel();
         statusPanel.setPreferredSize(new Dimension(getWidth(), StatusPanel.HEIGHT));
         add(statusPanel, BorderLayout.PAGE_END);
 
         pack();
     }
     
     private JFileChooser createFileChooser(String filterDescription, String extensionFilter)
     {
         JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
         FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(filterDescription, extensionFilter);
         fileChooser.setFileFilter(fileFilter);
         return fileChooser;        
     }
 
     private Action saveSegmentsFileAction()
     {
         return new AbstractAction("Save segments...")
         {
             final JFileChooser fileChooser = createFileChooser("TSEG files (*.tseg)", "tseg");
             
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (hasSegments())
                 {
                     if (showAndApproved())
                         tryStoreSegments(ensureFileExtension(fileChooser.getSelectedFile()));
                 }
                 else
                 {
                     informNothingToSave();
                 }
             }
 
             private File ensureFileExtension(File selectedFile)
             {
                 String fileName = selectedFile.getAbsolutePath();
 
                 String extension = ".tseg";
 
                 if (!endsWithIgnoreCase(fileName, extension))
                     selectedFile = new File(
                             StringUtils.indexOf(fileName, ".") > -1 ?
                             StringUtils.left(fileName, StringUtils.lastIndexOf(fileName, ".")) + extension :
                                     fileName + extension);
 
                 return selectedFile;
             }
 
             private boolean showAndApproved()
             {
                 return fileChooser.showSaveDialog(Main.this) == JFileChooser.APPROVE_OPTION;
             }
 
             private void informNothingToSave()
             {
                 showMessageDialog(Main.this,
                         "No segments found.",
                         "Save segments",
                         JOptionPane.INFORMATION_MESSAGE);
             }
 
             private boolean hasSegments()
             {
                 return !Iterables.isEmpty(getTriangulationDataSource().getSegments());
             }
         };
     }
 
     private void tryStoreSegments(File selectedFile)
     {
         try
         {
             storeSegments(selectedFile, getTriangulationDataSource().getSegments());
             showMessageDialog(Main.this, "Segments file saved successfully.", "Segments save", JOptionPane.INFORMATION_MESSAGE);
         }
         catch (IOException e)
         {
             final String message = "Failed to save segments to " + selectedFile.getAbsolutePath();
             showMessageDialog(Main.this, message, "Segments save", JOptionPane.ERROR_MESSAGE);
             LOGGER.warn(message, e);
         }
     }
 
     private Action loadSegmentsFileAction()
     {
         return new AbstractAction("Load segments...")
         {
             final JFileChooser fileChooser = createFileChooser("TSEG files (*.tseg)", "tseg");
             
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 if (hasPoints())
                 {
                     if (showAndFileChosen())
                         tryLoadingSegmentsFile(fileChooser.getSelectedFile());
                 }
                 else
                 {
                     showMessageDialog(
                             Main.this, 
                             "Cannot load segments. Load points first.", 
                             "Error loading segments", 
                             JOptionPane.WARNING_MESSAGE);
                 }
             }
 
             private boolean hasPoints()
             {
                 return !Iterables.isEmpty(getTriangulationDataSource().getPoints());
             }
 
             private boolean showAndFileChosen()
             {
                 int result = fileChooser.showOpenDialog(Main.this);
                 return result == JFileChooser.APPROVE_OPTION;
             }
         };
     }
 
     private void tryLoadingSegmentsFile(File selectedFile)
     {
         try
         {
             Collection<Line> segments = IOUtils.loadSegments(selectedFile);
             validateSegmentsBasedOnPoints(segments);
 
             for (Line line : segments)
                 eventBus.post(new SegmentAddedEvent(this, line));

            canvasPanel.repaint();
         }
         catch (Exception e)
         {
             String message = "Failed to load segments from file";
             showMessageDialog(
                     this,
                     message, 
                     "Error loading segments", 
                     JOptionPane.ERROR_MESSAGE);
             LOGGER.warn(message, e);
         }
     }
 
     private void validateSegmentsBasedOnPoints(Collection<Line> segments)
     {
        Set<Point> points = newHashSet(transform(getTriangulationDataSource().getPoints(), toZetLessPoints));
         checkArgument(points.containsAll(newHashSet(concat(transform(segments, toPoints)))),
                 "Segments have to be based on provided points");
     }
 
     private Action resetTriangulationAction()
     {
         return new AbstractAction("Reset")
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 eventBus.post(new TriangulationResetEvent(Main.this));
             }
         };
     }
 
     private Action runTriangulationAction()
     {
         return new AbstractAction("Run")
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 TriangulationDataSource dataSource = getTriangulationDataSource();
                 if (!isEmpty())
                 {
                     eventBus.post(new TriangulationStartedEvent(Main.this));
                     Main.this.addMouseListener(mouseAdapter);
                     Main.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     new TriangulationWorker(dataSource).execute();
                 }
             }
 
             private boolean isEmpty()
             {
                 TriangulationDataSource dataSource = getTriangulationDataSource();
                 if (dataSource == null)
                     return true;
 
                 return Iterables.isEmpty(dataSource.getPoints());
             }
         };
     }
 
     private TriangulationDataSource getTriangulationDataSource()
     {
         return canvasPanel;
     }
 
     private Action clearAction()
     {
         return new AbstractAction("Clear")
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 eventBus.post(new ClearEvent(Main.this));
             }
         };
     }
 
     private JMenuBar menuBar(JMenu ... menus)
     {
         JMenuBar menuBar = new JMenuBar();
         for (JMenu menu : menus)
             menuBar.add(menu);
         return menuBar;
     }
     
     private JMenu menu(String text, JComponent ... items)
     {
         JMenu menu = new JMenu(text);
         for (JComponent item : items)
             menu.add(item);
         return menu;
     }
     
     private JMenuItem menuItem(Action action)
     {
         return new JMenuItem(action);
     }
 
     private Action loadPointsFileAction()
     {
         return new AbstractAction("Load points...")
         {
             final JFileChooser fileChooser = createFileChooser("TSIN files (*.tsin)", "tsin");
 
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 int result = fileChooser.showOpenDialog(Main.this);
                 if (result == JFileChooser.APPROVE_OPTION)
                 {
                     eventBus.post(new ClearEvent(Main.this));
                     tryLoadingPointsFile(fileChooser.getSelectedFile());
                 }
             }
         }; 
     }
 
     private void tryLoadingPointsFile(File file)
     {
         try
         {
             List<Point> points = IOParsers.readPoints(file);
             eventBus.post(new LoadPointsEvent(this, points));
         }
         catch (Exception e)
         {
             e.printStackTrace();
             String message = format("Failed to load points from file %s: %s", file.getAbsolutePath(), e.getMessage());
             showMessageDialog(this,
                     message,
                     "Error loading TSIN file",
                     JOptionPane.ERROR_MESSAGE);
             LOGGER.warn(message, e);
         }
     }
 
     public static void main(String[] args)
     {
         Main main = new Main();
         main.doShow();
 
         if (args.length > 0)
             main.tryLoadingPointsFile(new File(args[0]));
     }
 
     class TriangulationWorker extends SwingWorker<ConstrainedDelaunayTriangulation, Object>
     {
         final TriangulationDataSource dataSource;
         long start;
 
         TriangulationWorker(TriangulationDataSource dataSource)
         {
             this.dataSource = dataSource;
         }
 
         @Override
         protected ConstrainedDelaunayTriangulation doInBackground() throws Exception
         {
             start = System.currentTimeMillis();
             ConstrainedDelaunayTriangulation triangulation = new ConstrainedDelaunayTriangulation(
                     newHashSet(dataSource.getPoints()));
 
             for (Line constraint : dataSource.getSegments())
                 triangulation.addConstraint(constraint);
             
             return triangulation;
         }
 
         @Override
         protected void done()
         {
             try
             {
                 eventBus.post(new TriangulationCalculatedEvent(Main.this, get(), System.currentTimeMillis() - start));
             }
             catch (Exception e)
             {
                 e.printStackTrace();
                 String message = "Failed running constrained triangulation";
                 showMessageDialog(Main.this,
                         message,
                         "Error running triangulation",
                         JOptionPane.ERROR_MESSAGE);
                 LOGGER.warn(message, e);
             }
             finally
             {
                 eventBus.post(new TriangulationEndedEvent(Main.this));
                 Main.this.removeMouseListener(mouseAdapter);
                 Main.this.setCursor(Cursor.getDefaultCursor());
             }
         }
     }
     
     public interface TriangulationDataSource
     {
         Iterable<Point> getPoints();
         Iterable<Line> getSegments();
     }
     
     public static class TriangulationStartedEvent extends EventObject
     {
         public TriangulationStartedEvent(Object source)
         {
             super(source);
         }
     }
     
     public static class TriangulationEndedEvent extends EventObject
     {
         public TriangulationEndedEvent(Object source)
         {
             super(source);
         }
     }
 }
