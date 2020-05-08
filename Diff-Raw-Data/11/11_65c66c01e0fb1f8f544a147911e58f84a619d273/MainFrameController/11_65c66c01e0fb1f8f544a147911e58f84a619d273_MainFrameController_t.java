 package controllers;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import models.Triangle;
 import models.TriangleCatalog;
 import utils.SSSCase;
 import utils.TriangleCase;
 import utils.TriangleException;
 import views.AboutFrame;
 import views.MainFrame;
 import views.MainFrame.StatusIcons;
 
 public class MainFrameController {
 
   private TriangleCatalog    _catalog;
   private MainFrame          _frame;
   private JDialog            _aboutFrame;
   private AddFrameController _afController;
   private static int         count;
 
   public MainFrameController( TriangleCatalog tc ) {
     count = 0;
     _catalog = tc;
     _frame = new MainFrame( tc, this );
 
     _catalog.addObserver( _frame );
   }
 
   public void exit() {
     System.exit( 0 );
   }
 
   public void add() {
     Triangle triangle = null;
 
     if ( _afController == null ) {
       _afController = new AddFrameController( _frame );
     }
 
     triangle = _afController.add();
     if ( triangle != null ) {
       triangle.setName( "Triangle    " + ( ++count ) );
       _catalog.addTriangle( triangle );
       _frame.setCurrentSelection( _catalog.size() - 1 );
     }
 
   }
 
   public void remove() {
     String message1 = "You don't have any triangles left in the list!";
     String message2 = "Please select a triangle from the list!";
     if ( _catalog.size() == 0 ) {
       _frame.setStatus( message1, StatusIcons.ERROR );
       JOptionPane.showMessageDialog( _frame, message1, "Error", JOptionPane.ERROR_MESSAGE, new ImageIcon( getClass().getResource( "/1258191240_agt_action_fail.png" ) ) );
       return;
     }
 
     int index = _frame.getCurrentSelection();
 
     if ( index == -1 ) {
       _frame.setStatus( message2, StatusIcons.ERROR );
       JOptionPane.showMessageDialog( _frame, message2, "Error", JOptionPane.ERROR_MESSAGE, new ImageIcon( getClass().getResource( "/1258191240_agt_action_fail.png" ) ) );
       return;
     }
 
     _catalog.removeTriangleAt( index );
 
     _frame.setStatus( "Successfully removed the triangle from the list", StatusIcons.OK );
   }
 
   public void save() {
     String filename = null;
     File selectedFile;
     PrintWriter pw = null;
 
     JFileChooser fc = new JFileChooser();
     fc.addChoosableFileFilter( new FileNameExtensionFilter( "Triangle file (.tri)", "tri" ) );
 
     if ( fc.showSaveDialog( _frame ) == JFileChooser.APPROVE_OPTION ) {
       selectedFile = fc.getSelectedFile();
       filename = selectedFile.getPath();
       if ( !filename.toLowerCase().endsWith( ".tri" ) ) {
         filename = filename.concat( ".tri" );
         selectedFile = new File( filename );
       }
       try {
         pw = new PrintWriter( new FileWriter( selectedFile ) );
       } catch ( Exception e ) {
         _frame.setStatus( e.getMessage(), StatusIcons.ERROR );
         JOptionPane.showMessageDialog( _frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, new ImageIcon( getClass().getResource( "/1258191240_agt_action_fail.png" ) ) );
       }
 
       for ( Triangle triangle : _catalog ) {
         pw.printf( "%30f\t%30f\t%30f\n", triangle.getSide( 0 ), triangle.getSide( 1 ), triangle.getSide( 2 ) );
       }
 
       pw.close();
 
       _frame.setStatus( "Successfully saved the list of triangles in " + selectedFile.getName(), StatusIcons.OK );
     }
 
   }
 
   public void open() {
     File selectedFile;
     Scanner sc = null;
 
     JFileChooser fc = new JFileChooser();
     fc.addChoosableFileFilter( new FileNameExtensionFilter( "Triangle file (.tri)", "tri" ) );
 
     if ( fc.showOpenDialog( _frame ) == JFileChooser.APPROVE_OPTION ) {
       selectedFile = fc.getSelectedFile();
       try {
         sc = new Scanner( selectedFile );
       } catch ( FileNotFoundException e ) {
         _frame.setStatus( e.getMessage(), StatusIcons.ERROR );
         JOptionPane.showMessageDialog( _frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, new ImageIcon( getClass().getResource( "/1258191240_agt_action_fail.png" ) ) );
       }
 
       double side1;
       double side2;
       double side3;
       TriangleCase tc;
       Triangle triangle = null;
 
       _catalog.clear();
       count = 0;
 
       while ( sc.hasNext() ) {
         side1 = sc.nextDouble();
         side2 = sc.nextDouble();
         side3 = sc.nextDouble();
 
         tc = new SSSCase( side1, side2, side3 );
         try {
           triangle = tc.calculateTriangle();
         } catch ( TriangleException e ) {
           _frame.setStatus( e.getMessage(), StatusIcons.ERROR );
           JOptionPane.showMessageDialog( _frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, new ImageIcon( getClass().getResource( "/1258191240_agt_action_fail.png" ) ) );
         }
 
         triangle.setName( "Triangle   " + ( ++count ) );
         _catalog.addTriangle( triangle );
       }
 
       _frame.setStatus( "Successfully opened the list of triangles from " + selectedFile.getName(), StatusIcons.OK );
     }
   }
 
   public void about() {
     if ( _aboutFrame == null ) {
       _aboutFrame = new AboutFrame( _frame );
     }
 
     _aboutFrame.setVisible( true );
   }
 }
