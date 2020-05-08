 package solitaire.DnD;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Point;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragGestureListener;
 import java.awt.dnd.DragSource;
 import java.awt.dnd.DragSourceDragEvent;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.dnd.DragSourceMotionListener;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import listener.ADragSourceListener;
 import listener.ADropTargetListener;
 import solitaire.controleur.CCarte;
 import solitaire.controleur.CTasDeCarte;
 import solitaire.presentation.PCarte;
 import solitaire.presentation.PTasDeCarte;
 
 /*
  * Classe abstraite de prsentation capable de faire du DnD
  */
 @SuppressWarnings( "serial" )
 public abstract class ADnD extends JPanel
 {
     protected DropTarget dropTarget;
 
     protected DropTargetDropEvent theFinalEvent;
 
     protected DragSource dragSource = null;
 
     protected MyDragSourceListener myDragSourceListener = null;
 
     // origine du drag
     protected Point dragOrigin = null;
 
     protected DragGestureEvent theInitialEvent = null;
 
     protected PCarte selected;
 
     protected CCarte selectedControl;
 
     protected PTasDeCarte Tastemporaire;
 
     // frame qui va contenir le tas de carte en cours de DnD
     protected JFrame dragFrame = null;
 
     // le controleur associ au drag
     protected IControleurDnD controlleur;
 
     // le composant graphique sur lequel on va dmarr le DnD
     protected JPanel composantContainDragger;
 
     // class dtectant le dbut d'un DND
     protected class MyDragGestureListener implements DragGestureListener
     {
         public MyDragGestureListener()
         {
         }
 
         /*
          * dbut DND
          */
         @Override
         public void dragGestureRecognized( DragGestureEvent dge )
         {
             selected = null;
             selectedControl = null;
             theInitialEvent = dge;
             dragOrigin = dge.getDragOrigin(); // Point de dpart du drag
             try
             { // on rcupre la PCarte associ au drag
                 if ( composantContainDragger != null )
                 {
                     selected = (PCarte) composantContainDragger.getComponentAt( dragOrigin );
                     selectedControl = selected.getControleur();
                 }
             }
             catch ( Exception e )
             {
                 //pas trs beau mais pas envie de polluer la console
             }
             // on prvient le controleur de la classe driv d'un dbut de Drag
             // sur la carte selectedControl
             if ( controlleur != null )
                 controlleur.p2c_debutDnDDrag( selectedControl );
         }
     }
 
     protected class MyDragSourceListener extends ADragSourceListener
     {
         public MyDragSourceListener()
         {
         }
 
         // on signale au controleur du drop
         @Override
         public void dragDropEnd( DragSourceDropEvent event )
         {
             controlleur.p2c_finDnD4DragSource( Tastemporaire.getControleur(), event.getDropSuccess() );
             dragFrame.setVisible( false );
             repaint();
         }
 
         @Override
         public void dragEnter( DragSourceDragEvent evt )
         {
             evt.getDragSourceContext().setCursor( new Cursor( Cursor.MOVE_CURSOR ) );
         }
     }
 
     // Quand on est en cours de DnD on dplace la frame avec la souris
     protected class MyDragSourceMotionListener implements DragSourceMotionListener
     {
 
         public MyDragSourceMotionListener()
         {
         }
 
         @Override
        public void dragMouseMoved( DragSourceDragEvent evt )
        {  
            int parentX = getRootPane().getX();
            int parentY = getRootPane().getY();
            int eventX = evt.getLocation().x + 15;
            int eventY = evt.getLocation().y - 1;
            dragFrame.setLocation( eventX - parentX, eventY - parentY );
             repaint();
         }
     }
 
     // class qui va grer le drop
     protected class MyDropTargetListener extends ADropTargetListener
     {
         protected PTasDeCarte pTas = null;
 
         public MyDropTargetListener()
         {
         }
 
         @Override
         public void dragEnter( DropTargetDragEvent event )
         {
 
             try
             {
                 Transferable transferable = event.getTransferable();
                 if ( transferable.isDataFlavorSupported( new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType ) ) )
                 {
                     event.acceptDrag( DnDConstants.ACTION_MOVE );
                     pTas = (PTasDeCarte) transferable.getTransferData( new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType ) );
                     controlleur.p2c_DragEnter( pTas.getControleur() );
                 }
 
             }
             catch ( Exception e )
             {
             }
         }
 
         @Override
         public void dragExit( DropTargetEvent event )
         {
             controlleur.p2c_DragExit( pTas.getControleur() );
         }
 
         @Override
         // on drop le tas de carte
         public void drop( DropTargetDropEvent dtde )
         {
             theFinalEvent = dtde;
             controlleur.p2c_finDnD4DropTarget( (CTasDeCarte) pTas.getControleur() );
         }
     }
 
     // Drag valide donc on met le tas de carte tmp dans une frame qu'on baladera
     // en glisser dposer
     public void c2p_debutDnDValide( PTasDeCarte tmp )
     {
         Tastemporaire = tmp;
         dragSource.startDrag( theInitialEvent, DragSource.DefaultMoveNoDrop, tmp, myDragSourceListener );
 
         dragFrame = new JFrame();
 
         dragFrame.add( tmp );
         dragFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); // Already
                                                                     // there
         dragFrame.setExtendedState( JFrame.MAXIMIZED_BOTH );
         dragFrame.setUndecorated( true );
         dragFrame.setVisible( true );
         dragFrame.pack();
         repaint();
     }
 
     public void c2p_debutDnDInvalide()
     {
 
     }
 
     public void finDnDValide()
     {
         theFinalEvent.acceptDrop( DnDConstants.ACTION_MOVE );
         theFinalEvent.getDropTargetContext().dropComplete( true );
         repaint();
         color_resetColor();
     }
 
     public void finDnDInvalid()
     {
         theFinalEvent.rejectDrop();
         color_resetColor();
     }
 
     public void color_isEmpilable()
     {
         setCursor( new Cursor( Cursor.HAND_CURSOR ) );
        if(composantContainDragger!=null)
         composantContainDragger.setBackground( Color.green );
     }
 
     public void color_resetColor()
     {
         setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
        if(composantContainDragger!=null)
         composantContainDragger.setBackground( new Color( 13, 131, 53 ) ); // vert
     }
 
     public void color_isNotEmpilable()
     {
         setCursor( new Cursor( Cursor.CROSSHAIR_CURSOR ) );
        if(composantContainDragger!=null)
         composantContainDragger.setBackground( Color.red );
     }
 
 }
