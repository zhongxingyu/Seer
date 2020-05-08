 /*
  * Copyright (c) 2006 Andy Wood
  */
 package uk.co.mindtrains;
 
 import java.awt.Component;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyDescriptor;
 import java.io.IOException;
 
 import javax.swing.JDesktopPane;
 import javax.swing.JInternalFrame;
 import javax.swing.JLayeredPane;
 import javax.swing.border.LineBorder;
 
 import uk.co.mindtrains.Piece.Label;
 
 import com.l2fprod.common.propertysheet.DefaultProperty;
 import com.l2fprod.common.propertysheet.Property;
 import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
 import com.l2fprod.common.propertysheet.PropertySheetTableModel;
 
 /**
  * Layout class manages the list of track pieces as the user is
  * putting it together.
  */
 public class Layout extends JDesktopPane
 {
 	private static final long serialVersionUID = 1L;
 	private Piece.Label dragging;
 	private Piece.Label main;
 	private Point dragOffset;
 	private Rectangle draggingRect = new Rectangle();
 	private PropertySheetTableModel model;
 	
 	public Layout( Point start, PropertySheetTableModel m, final PropertyEditorRegistry registry )
 	{
 		model = m;
 		
 		setLayout( null );
 		
 		addMouseListener( new MouseAdapter()
 		{
 			public void mousePressed( MouseEvent e )
 			{
 				Component piece = findComponentAt( e.getPoint() );
 				if ( piece != null && piece instanceof Piece.Label )
 				{
 					setDragging( (Piece.Label)piece );
 					dragOffset = new Point( e.getX() - piece.getX(), e.getY() - piece.getY() );
 					remove( piece );
 					dragging.setBorder( LineBorder.createGrayLineBorder() );
 					add( piece, 0 );
 					model.setProperties( createProperties( dragging.getPiece().getProperties(), registry ) );
 					repaint();
 				}
 				else
					dragging = null;
 			}
 		} );
 
 		addMouseMotionListener( new MouseMotionAdapter()
 		{
 			public void mouseDragged( MouseEvent e )
 			{
 				if ( dragging != null )
 				{
 					dragging.setLocation( e.getX() - (int)dragOffset.getX(), e.getY() - (int)dragOffset.getY() );
 					snap( dragging );
 				}
 			}
 		} );
 		
 		new DropTarget( this,
 		                DnDConstants.ACTION_COPY,
 		                new DropTargetListener()
 		                {
 						public void dragEnter( DropTargetDragEvent dtde )
 						{
 							dtde.acceptDrag(DnDConstants.ACTION_COPY);
 							add( dragging, JLayeredPane.DRAG_LAYER );
 							Point location = new Point( dtde.getLocation() );
 				        	location.translate( -dragging.getIcon().getIconWidth(), -dragging.getIcon().getIconHeight() );
 							dragging.setLocation( location );
 							snap( dragging );
 							draggingRect.setRect( dragging.getLocation().x, dragging.getLocation().y,
 							                      dragging.getIcon().getIconWidth(), dragging.getIcon().getIconHeight() );
 							paintImmediately( draggingRect );
 						}
 
 						public void dragOver( DropTargetDragEvent dtde )
 						{
 							dtde.acceptDrag(DnDConstants.ACTION_COPY);
 							Point location = new Point( dtde.getLocation() );
 				        	location.translate( -dragging.getIcon().getIconWidth(), -dragging.getIcon().getIconHeight() );
 							dragging.setLocation( location );
 							snap( dragging );
 							Rectangle newRect = new Rectangle( dragging.getLocation().x, dragging.getLocation().y,
 							                                   dragging.getIcon().getIconWidth(), dragging.getIcon().getIconHeight() );
 							draggingRect.add( newRect );
 							paintImmediately( draggingRect );
 							draggingRect = newRect;
 						}
 
 						public void dropActionChanged( DropTargetDragEvent dtde )
 						{
 							 dtde.acceptDrag(DnDConstants.ACTION_COPY);
 						}
 
 						public void dragExit( DropTargetEvent dte )
 						{
 							remove( dragging );
 							paintImmediately( draggingRect );
 						}
 
 						public void drop( DropTargetDropEvent dtde )
 						{
 							dtde.acceptDrop( DnDConstants.ACTION_COPY );
 							remove( dragging );
 							paintImmediately( draggingRect );
 							try
 							{
 								Piece piece = (Piece)dtde.getTransferable().getTransferData( IconTransferHandler.NATIVE_FLAVOR );
 					        	Point location = new Point( dtde.getLocation() );
 					        	location.translate( -piece.getIcon().getIconWidth(), -piece.getIcon().getIconHeight() );
 								add( piece, location, true );
 								repaint();
 								dtde.dropComplete( true );
 							}
 							catch ( UnsupportedFlavorException e )
 							{
 								e.printStackTrace();
 							}
 							catch ( IOException e )
 							{
 								e.printStackTrace();
 							}
 						}
 			
 		               },
 		               true );
 
 		IconTransferHandler.setLayout( this );
 	}
 	
 	public void setDragging( Piece.Label d )
 	{
 		if ( dragging != null )
 		{
 			dragging.setBorder( null );
 			model.setProperties( new Property[ 0 ] );
 		}
 		dragging = d;
 	}
 
 	public Piece.Label add( Piece piece, Point location, boolean snap )
 	{
 		Piece.Label label = piece.new Label();
     	label.setLocation( location );
     	if ( snap )
     		snap( label );
 		add( label, 0 );
 		return label;
 	}
 
 	public void run( JInternalFrame palette, PropertySheetTableModel model  )
 	{
 		new Run( this, palette, model );
 	}
 
 	public void setMain( Label main )
 	{
 		this.main = main;
 	}
 
 	public Piece.Label getMain()
 	{
 		return main;
 	}
 
 	protected void snap( Piece.Label piece )
 	{
 		Point snap = null;
 		for ( int i = 0; i < getComponentCount(); i++ )
 		{
 			Component component = getComponent( i );
 			if ( component != piece && component instanceof Piece.Label )
 				snap = Piece.closest( ( (Piece.Label)component ).getPiece().snap( piece.getPiece() ), snap, piece.getLocation() );
 		}
 		if ( snap != null )
 			piece.setLocation( snap );
 	}
 
 	public void save()
 	{
 		try
 		{
 			Saver saver = new Saver( this );
 			saver.save( "docs/saved.xml" );
 		}
 		catch ( IOException e )
 		{
 			e.printStackTrace( System.err );
 		}
 	}
 
     public static Property[] createProperties( final Object object, PropertyEditorRegistry registry )
 	{
     	if ( object != null )
     	{
 			try
 			{
 	    		PropertyDescriptor[] descriptors = Introspector.getBeanInfo( object.getClass(), Object.class ).getPropertyDescriptors();
 	    		Property[] properties = new Property[ descriptors.length ];
 	    	             
 		    	for ( int i = 0; i < descriptors.length; i++ )
 		    	{
 		    		final DefaultProperty property = new DefaultProperty();
 		    		property.setName( descriptors[ i ].getName() );
 		    		property.setDisplayName( descriptors[ i ].getDisplayName() );
 		    		property.setType( descriptors[ i ].getPropertyType() );
 		    		property.readFromObject( object );
 		    		property.addPropertyChangeListener( new PropertyChangeListener() {
 						public void propertyChange( PropertyChangeEvent arg0 )
 						{
 							property.writeToObject( object );
 						}
 		    		} );
 		    		properties[ i ] = property;
 		    	}
 		    	
 		    	if ( object instanceof Increment.By )
 		    		registry.registerEditor( properties[ 0 ], Increment.By.Editor.class );
 		    	else if ( object instanceof If.Limit )
 		    		registry.registerEditor( properties[ 0 ], If.Limit.Editor.class );
 		    	
 				return properties;
 			}
 			catch ( IntrospectionException e )
 			{
 				e.printStackTrace( System.err );
 			}
     	}
     	return new Property[ 0 ];
 	}
 }
