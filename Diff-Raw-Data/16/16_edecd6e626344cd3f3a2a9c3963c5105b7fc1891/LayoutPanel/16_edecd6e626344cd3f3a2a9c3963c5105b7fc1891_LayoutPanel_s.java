 package com.hifiremote.jp1;
 
 import java.awt.*;
 import javax.swing.border.*;
 import javax.swing.*;
 import java.text.*;
 import javax.swing.text.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import javax.swing.event.*;
 import java.util.*;
 import java.awt.datatransfer.*;
 import java.awt.dnd.*;
 import info.clearthought.layout.*;
 
 public class LayoutPanel
   extends KMPanel
   implements ActionListener
 {
   public LayoutPanel( DeviceUpgrade devUpgrade )
   {
     super( devUpgrade );
     setLayout( new BorderLayout());
     imagePanel = new ImagePanel();
     // Don't know why, but tooltips don't work without this
     imagePanel.setToolTipText( "" );
 
     scrollPane = new JScrollPane( imagePanel,
                                   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                   JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
     add( scrollPane, BorderLayout.WEST );
 
     JPanel rightPanel = new JPanel( new BorderLayout());
     add( rightPanel, BorderLayout.CENTER );
 
     JPanel modePanel = new JPanel( new FlowLayout( FlowLayout.LEFT ));
     modePanel.setBorder( BorderFactory.createTitledBorder( "Mode" ));
 
     ButtonGroup group = new ButtonGroup();
 
     normalMode = new JRadioButton( "Normal", true );
     normalMode.addActionListener( this );
     group.add( normalMode );
     modePanel.add( normalMode );
     
     shiftMode = new JRadioButton( "Shift" );
     shiftMode.addActionListener( this );
     group.add( shiftMode );
     modePanel.add( shiftMode );
     
     xShiftMode = new JRadioButton( "XShift" );
     xShiftMode.addActionListener( this );
     group.add( xShiftMode );
     modePanel.add( xShiftMode );
 
     JPanel infoPanel = new JPanel( new GridLayout( 2, 2 ));
 
     infoPanel.add( new JLabel( "Button:" ));
     buttonName = new JTextField();
     buttonName.setEditable( false );
     infoPanel.add( buttonName );
 
     infoPanel.add( new JLabel( "Function:" ));
     function = new JTextField();
     function.setEditable( false );
     infoPanel.add( function );
 
     Box box = Box.createVerticalBox();
     box.add( modePanel );
     box.add( infoPanel );
 
     rightPanel.add( box, BorderLayout.NORTH );
 
     JPanel panel = new JPanel( new BorderLayout());
     JLabel label = new JLabel( "Available Functions:" );
     label.setBorder( BorderFactory.createEmptyBorder( 2, 2, 3, 2 ));
     panel.add( label, BorderLayout.NORTH );
     rightPanel.add( panel, BorderLayout.CENTER );
 
     JPanel outerPanel = new JPanel( new BorderLayout());
     functionPanel = new JPanel( new GridFlowLayout());
 
     outerPanel.add( functionPanel, BorderLayout.CENTER );
     panel.add( outerPanel, BorderLayout.CENTER );
 
     panel = new JPanel();
     autoAssign = new JButton( "Auto assign" );
     autoAssign.setToolTipText( "Automatically assign functions to buttons, by matching names." );
     autoAssign.addActionListener( this );
     panel.add( autoAssign );
 
     box = Box.createVerticalBox();
     box.add( new JSeparator());
     box.add( panel );
 
     rightPanel.add( box, BorderLayout.SOUTH );
 
     DropTarget dropTarget = new LayoutDropTarget();
     dropTarget.setComponent( imagePanel );
 
     MouseListener ml = new MouseAdapter()
     {
       public void mousePressed( MouseEvent e )
       {
         Point p = e.getPoint();
         ButtonShape savedShape = currentShape;
         currentShape = getShapeAtPoint( p );
         Button b = getButtonForShape( currentShape );
         if ( b == null )
           currentShape = null;
 
         if ( currentShape == null )
           return;
 
         if ( currentShape != savedShape )
         {
           setButtonText( currentShape, getButtonForShape( currentShape ));
           doRepaint();
         }
         showPopup( e );
       }
 
       public void mouseReleased( MouseEvent e )
       {
         showPopup( e );
       }
 
       private void showPopup( MouseEvent e )
       {
         if ( e.isPopupTrigger() )
         {
           ButtonShape buttonShape = getShapeAtPoint( e.getPoint());
           if ( buttonShape != null )
           {
             currentShape = buttonShape;
             doRepaint();
             popup.show( imagePanel, e.getX(), e.getY());
           }
         }
       }
     };
     imagePanel.addMouseListener( ml );
   }
 
   private void addFunction( Function f )
   {
     if (( f == null ) ||
         (( f.getHex() != null ) && ( f.getName() != null ) && (f.getName().length() > 0 )))
     {
       FunctionLabel l;
       if ( f == null )
         l = new FunctionLabel( null );
       else
         l = f.getLabel();
       l.addMouseListener( doubleClickListener );
       functionPanel.add( l );
 
       FunctionItem item;
       if ( f == null )
         item = new FunctionItem( null );
       else
         item = f.getItem();
       item.addActionListener( this );
       popup.add( item );
     }
   }
 
   private void setFunctions()
   {
     popup = new JPopupMenu();
     popup.setLayout( new GridLayout( 0, 3 ));
     FunctionItem item = null;
 
     functionPanel.removeAll();
     FunctionLabel label = null;
     Function function = null;
 
     Vector funcs = deviceUpgrade.getFunctions();
     for ( int i = 0; i < funcs.size(); i++ )
     {
       function = ( Function )funcs.elementAt( i );
       addFunction( function );
     }
     funcs = deviceUpgrade.getExternalFunctions();
     for ( int i = 0; i < funcs.size(); i++ )
     {
       function = ( Function )funcs.elementAt( i );
       addFunction( function );
     }
     addFunction( null );
   }
 
   public void update()
   {
     Remote r = deviceUpgrade.getRemote();
     ImageIcon icon = r.getImageIcon();
     if ( icon != null )
     {
       Dimension size = new Dimension( icon.getIconWidth(),
                                       icon.getIconHeight());
       imagePanel.setPreferredSize( size );
       imagePanel.revalidate();
     }
     ButtonShape[] buttonShapes = deviceUpgrade.getRemote().getButtonShapes();
     boolean found = false;
     for ( int i = 0; i < buttonShapes.length; i++ )
     {
       if ( currentShape == buttonShapes[ i ])
       {
         found = true;
         break;
       }
     }
     if ( !found )
       currentShape = null;
 
     setButtonText( currentShape, getButtonForShape( currentShape ));
 
     setFunctions();
 
     if ( r.getXShiftEnabled())
       xShiftMode.setEnabled( true );
     else
     {
       xShiftMode.setEnabled( false );
       if ( xShiftMode.isSelected())
         normalMode.setSelected( true );
     }
 
     doRepaint();
   }
 
   private void doRepaint()
   {
     imagePanel.repaint( 0L, 0, 0, imagePanel.getWidth(), imagePanel.getHeight());
   }
 
   private void setButtonText( ButtonShape buttonShape, Button b )
   {
     if (( buttonShape != null ) && ( b != null ))
     {
       Remote r = deviceUpgrade.getRemote();
       String name = buttonShape.getName();
       if ( name == null )
       {
         if ( normalMode.isSelected())
           name = b.getName();
         else if ( shiftMode.isSelected())
         {
           name = b.getShiftedName();
           if ( name == null )
             name = r.getShiftLabel() + '-' + b.getName(); 
         }
         else if ( xShiftMode.isSelected())
         {
           name = b.getXShiftedName();
           if ( name == null )
             name = r.getXShiftLabel() + '-' + b.getName();
         }
       }
       buttonName.setText( name );
       Function f = null;
       if ( normalMode.isSelected())
         f = b.getFunction();
       else if ( shiftMode.isSelected())
         f = b.getShiftedFunction();
       else if ( xShiftMode.isSelected())
         f = b.getXShiftedFunction();
       if ( f != null )
         function.setText( f.getName());
       else
         function.setText( "" );
     }
     else
     {
       buttonName.setText( "" );
       function.setText( "" );
     }
   }
 
   public ButtonShape getShapeAtPoint( Point p )
   {
     ButtonShape[] buttonShapes = deviceUpgrade.getRemote().getButtonShapes();
     ButtonMap map = deviceUpgrade.getDeviceType().getButtonMap();
     for ( int i = 0; i < buttonShapes.length; i++ )
     {
       ButtonShape buttonShape = buttonShapes[ i ];
       Shape s = buttonShape.getShape();
       if (( s != null ) && s.contains( p ))
         return buttonShape;
     }
     return null;
   }
 
   public Button getButtonForShape( ButtonShape buttonShape )
   {
     if ( buttonShape == null )
       return null;
 
     Button b = buttonShape.getButton();
     ButtonMap map = deviceUpgrade.getDeviceType().getButtonMap();
     if ( normalMode.isSelected())
     {
       if ( !b.getIsNormal())
         return null;
       else if ( b.allowsKeyMove() || map.isPresent( b )) 
         return b;
     }
     else if ( shiftMode.isSelected())
     {
       if ( b.getIsNormal())
       {
         if ( !b.allowsShiftedKeyMove())
           return null;
         if (( b.getShiftedButton() != null ))
           b = b.getShiftedButton();
         else
           return b;
       }
       if ( b.getIsShifted())
       {
         if ( b.allowsKeyMove() || map.isPresent( b ))
           return b.getBaseButton();
         else
           return null;
       }
       else
         return null;
     }
     else if ( xShiftMode.isSelected())
     {
       if ( b.getIsNormal())
       {
         if ( !b.allowsXShiftedKeyMove())
           return null;
         if ( b.getXShiftedButton() != null )
           b = b.getXShiftedButton();
         else 
           return b;
       }
       if ( b.getIsXShifted())
       {
         if ( b.allowsKeyMove() || map.isPresent( b ))
           return b.getBaseButton();
       }
       else if ( b.allowsXShiftedKeyMove())
         return b;
     }
     return null;
   }
 
   // From interface ActionListener
   public void actionPerformed( ActionEvent e )
   {
     Object source = e.getSource();
     if ( source == autoAssign )
     {
       deviceUpgrade.autoAssignFunctions();
       doRepaint();
     }
     else if (( source == normalMode ) || 
              ( source == shiftMode ) ||
              ( source == xShiftMode ))
     {
       Button b = getButtonForShape( currentShape );
       if ( b == null )
         currentShape = null;
       setButtonText( currentShape, b );
       doRepaint();
     }
   }
 
   class DoubleClickListener
     extends MouseAdapter
   {
     public void mouseClicked( MouseEvent e )
     {
       if (( currentShape == null ) || ( e.getClickCount() < 2 ))
         e.consume();
       else
       {
         Button button = getButtonForShape( currentShape );
         if ( button == null )
           return;
 
         ButtonMap map = deviceUpgrade.getDeviceType().getButtonMap();
         Function f = ( Function )(( FunctionLabel )e.getSource()).getFunction();
         if ( normalMode.isSelected())
             button.setFunction( f );
         else if ( shiftMode.isSelected())
           button.setShiftedFunction( f );
         else if ( xShiftMode.isSelected())
           button.setXShiftedFunction( f );
           
         setButtonText( currentShape, button );
         doRepaint();
       }
     }
   }
 
   class LayoutDropTarget
     extends DropTarget
   {
     public LayoutDropTarget()
     {
       setDefaultActions( DnDConstants.ACTION_COPY );
     }
 
     public void dragOver( DropTargetDragEvent dtde )
     {
       Point p = dtde.getLocation();
       currentShape = getShapeAtPoint( p );
       Button button = getButtonForShape( currentShape );
       if ( button != null )
         dtde.acceptDrag( dtde.getDropAction());
       else
       {
         currentShape = null;
         dtde.rejectDrag();
       }
       doRepaint();
     }
 
     public void drop( DropTargetDropEvent dtde )
     {
       Point p = dtde.getLocation();
       currentShape = getShapeAtPoint( p );
       Button button = getButtonForShape( currentShape );
       if ( button != null )
       {
         int action = dtde.getDropAction();
         dtde.acceptDrop( action );
         Transferable tf = dtde.getTransferable();
         DataFlavor[] flavors = tf.getTransferDataFlavors();
         try
         {
           Function f = ( Function )tf.getTransferData( LocalObjectTransferable.getFlavor());
           if ( normalMode.isSelected())
             button.setFunction( f );
           else if ( shiftMode.isSelected())
             button.setShiftedFunction( f );
           else if ( xShiftMode.isSelected())
             button.setXShiftedFunction( f );
           setButtonText( currentShape, button );
         }
         catch ( Exception e )
         {
           e.printStackTrace( System.err );
         }
       }
       else
         dtde.rejectDrop();
       doRepaint();
     }
   }
 
   private class ImagePanel
     extends JPanel
     implements Scrollable
   {
     public void paint( Graphics g )
     {
       Graphics2D g2 = ( Graphics2D ) g;
       g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON );
       Remote r = deviceUpgrade.getRemote();
       ImageIcon icon = r.getImageIcon();
       if ( icon != null )
         g2.drawImage( icon.getImage(), null, null );
 
       if ( currentShape != null )
       {
         g2.setPaint( Color.white );
         g2.setStroke( new BasicStroke( 4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
         g2.draw( currentShape.getShape());
       }
 
       g2.setStroke( new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
 
       DeviceType devType = deviceUpgrade.getDeviceType();
       ButtonMap map = devType.getButtonMap();
 
       ButtonShape[] buttonShapes = r.getButtonShapes();
       for ( int i = 0; i < buttonShapes.length; i++ )
       {
         ButtonShape buttonShape = buttonShapes[ i ];
         Button b = getButtonForShape( buttonShape );
         if ( b == null )
           continue;
 
         Shape s = buttonShape.getShape();
 
         Function f = null;
         if ( normalMode.isSelected())
           f = b.getFunction();
         else if ( shiftMode.isSelected())
           f = b.getShiftedFunction();
         else if ( xShiftMode.isSelected())
           f = b.getXShiftedFunction();
         
         if ( f != null )
         {
           g2.setPaint( Color.yellow );
           g2.fill( s );
         }
         
         if ( shiftMode.isSelected())
           b = b.getShiftedButton();
         else if ( xShiftMode.isSelected())
           b = b.getXShiftedButton();
         
         if ( map.isPresent( b ))
         {
           g2.setPaint( Color.orange );
           g2.draw( s );
         }
       }
     }
 
     public String getToolTipText( MouseEvent e )
     {
       ButtonShape buttonShape = getShapeAtPoint( e.getPoint());
       if ( buttonShape == null )
         return null;
 
       Button b = getButtonForShape( buttonShape );
       if ( b == null )
         return null;
      
       String name = buttonShape.getName();
       if ( name != null )
         return name;
 
       Remote r = deviceUpgrade.getRemote();
       if ( normalMode.isSelected())
         name = b.getName();
       else if ( shiftMode.isSelected())
       {
         name = b.getShiftedName();
         if ( name == null )
           name = r.getShiftLabel() + '-' + b.getName();
       }
       else if ( xShiftMode.isSelected())
       {
         name = b.getXShiftedName();
         if ( name == null )
           name = r.getXShiftLabel() + '-' + b.getName();
       }
 
       return name;
     }
 
     public Dimension getPreferredScrollableViewportSize()
     {
       ImageIcon icon = deviceUpgrade.getRemote().getImageIcon();
       Dimension rc = null;
       if ( icon != null )
       {
         int w = icon.getIconWidth();
         int h = icon.getIconHeight();
         if ( scrollPane.getViewport().getExtentSize().height < h )
           w += scrollPane.getVerticalScrollBar().getWidth();
 
         rc = new Dimension( w, h );
       }
       else
         rc = new Dimension( 0, 0 );
 
       return rc;
     }
 
     public int getScrollableUnitIncrement( Rectangle visibleRect,
                                            int orientation,
                                            int direction )
     {
       return 1;
     }
 
     public int getScrollableBlockIncrement( Rectangle visibleRect,
                                             int orientation,
                                             int direction )
     {
       return visibleRect.height;
     }
 
     public boolean getScrollableTracksViewportWidth()
     {
       return true;
     }
 
     public boolean getScrollableTracksViewportHeight()
     {
       return false;
     }
   }
 
   private ButtonShape currentShape = null;
   private JPanel imagePanel = null;
   private JRadioButton normalMode = null;
   private JRadioButton shiftMode = null;
   private JRadioButton xShiftMode = null;
   private JTextField buttonName = null;
   private JTextField function = null;
   private JButton autoAssign = null;
   private JPopupMenu popup = null;
   private JPanel functionPanel = null;
   private JScrollPane scrollPane = null;
   private DoubleClickListener doubleClickListener = new DoubleClickListener();
 }
