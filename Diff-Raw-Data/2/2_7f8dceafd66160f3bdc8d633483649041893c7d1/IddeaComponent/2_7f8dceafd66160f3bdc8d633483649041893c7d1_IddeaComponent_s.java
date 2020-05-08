 package view.component;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 
 import javax.swing.Action;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JToolBar;
 import javax.swing.SwingUtilities;
 
 import model.figure.DrawFigureFactory;
 import net.imglib2.IterableInterval;
 import net.imglib2.RandomAccessibleInterval;
 import net.imglib2.RealRandomAccessible;
 import net.imglib2.converter.RealARGBConverter;
 import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
 import net.imglib2.realtransform.AffineTransform2D;
 import net.imglib2.type.NativeType;
 import net.imglib2.type.Type;
 import net.imglib2.type.numeric.RealType;
 import net.imglib2.type.numeric.integer.LongType;
 import net.imglib2.type.numeric.real.DoubleType;
 import net.imglib2.view.IntervalView;
 import net.imglib2.view.Views;
 
 import org.jhotdraw.draw.AttributeKey;
 import org.jhotdraw.draw.BezierFigure;
 import org.jhotdraw.draw.DefaultDrawingEditor;
 import org.jhotdraw.draw.Drawing;
 import org.jhotdraw.draw.DrawingEditor;
 import org.jhotdraw.draw.QuadTreeDrawing;
 import org.jhotdraw.draw.action.ButtonFactory;
 import org.jhotdraw.draw.io.DOMStorableInputOutputFormat;
 import org.jhotdraw.draw.io.InputFormat;
 import org.jhotdraw.draw.io.OutputFormat;
 import org.jhotdraw.draw.tool.BezierTool;
 import org.jhotdraw.util.ResourceBundleUtil;
 
 import view.display.InteractiveDrawingView;
 import view.viewer.InteractiveRealViewer2D;
 
 import com.jug.util.ImglibUtil;
 
 import controller.tool.SpimTool;
 
 /**
  * Created with IntelliJ IDEA.
  * 
  * @author HongKee Moon, Florian Jug
  * @version 0.1beta
  * @since 9/4/13
  */
 public class IddeaComponent extends JPanel {
 
 	private static final long serialVersionUID = -3808140519052170304L;
 
 	/**
 	 * It holds the current interactive viewer 2d
 	 */
 	private InteractiveRealViewer2D< ? > currentInteractiveViewer2D;
 
 	private DrawingEditor editor;
 	private String toolbarLocation;
 	private boolean toolbarVisible = false;
 	private IntervalView< DoubleType > intervalViewDoubleType = null;
 
 	private JToolBar tb;
 	private JScrollPane scrollPane;
 	private InteractiveDrawingView view;
 
 	public IddeaComponent() {
 
 		editor = new DefaultDrawingEditor();
 		createToolbar();
 
 		try {
 			initComponents();
 		} catch ( final Exception ex ) {
 			ex.printStackTrace();
 		}
 
 		setEditor( editor );
 		view.setDrawing( createDrawing() );
 	}
 
 	public IddeaComponent( final IntervalView< DoubleType > viewImg ) {
 
 		this.intervalViewDoubleType = viewImg;
 
 		editor = new DefaultDrawingEditor();
 		createToolbar();
 
 		try {
 			initComponents();
 		} catch ( final Exception ex ) {
 			ex.printStackTrace();
 		}
 
 		setEditor( editor );
 		view.setDrawing( createDrawing() );
 	}
 
 	public InteractiveRealViewer2D getCurrentInteractiveViewer2D() {
 		return currentInteractiveViewer2D;
 	}
 
 	public InteractiveDrawingView getInteractiveDrawingView( final IntervalView< DoubleType > viewImg ) {
 		if ( viewImg != null ) {
 			final AffineTransform2D transform = new AffineTransform2D();
 
 			final DoubleType min = new DoubleType();
 			final DoubleType max = new DoubleType();
 			ImglibUtil.computeMinMax( viewImg, min, max );
 
 			final RealRandomAccessible< DoubleType > interpolated = Views.interpolate( Views.extendZero( viewImg ), new NearestNeighborInterpolatorFactory< DoubleType >() );
 			final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( min.get(), max.get() );
 
 			currentInteractiveViewer2D = new InteractiveRealViewer2D< DoubleType >( ( int ) viewImg.max( 0 ), ( int ) viewImg.max( 1 ), interpolated, transform, converter );
 		} else {
 			final AffineTransform2D transform = new AffineTransform2D();
 			final RealRandomAccessible< DoubleType > dummy = new DummyRealRandomAccessible();
 			final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( 0, 0 );
 
			currentInteractiveViewer2D = new InteractiveRealViewer2D< DoubleType >( 0, 0, dummy, transform, converter );
 		}
 
 		return currentInteractiveViewer2D.getJHotDrawDisplay();
 	}
 
 	/**
 	 * Creates a new Drawing for this view.
 	 */
 	protected Drawing createDrawing() {
 		final Drawing drawing = new QuadTreeDrawing();
 		final DOMStorableInputOutputFormat ioFormat = new DOMStorableInputOutputFormat( new DrawFigureFactory() );
 
 		drawing.addInputFormat( ioFormat );
 
 		drawing.addOutputFormat( ioFormat );
 		return drawing;
 	}
 
 	/**
 	 * Sets a drawing editor for the view.
 	 */
 	public void setEditor( final DrawingEditor newValue ) {
 		if ( editor != null ) {
 			editor.remove( view );
 		}
 		editor = newValue;
 		if ( editor != null ) {
 			editor.add( view );
 		}
 	}
 
 	private void createToolbar() {
 		final ResourceBundleUtil labels = ResourceBundleUtil.getBundle( "org.jhotdraw.draw.Labels" );
 
 		tb = new JToolBar();
 		tb.setOrientation( JToolBar.HORIZONTAL );
 
 		addCreationButtonsTo( tb, editor );
 		tb.setName( labels.getString( "window.drawToolBar.title" ) );
 	}
 
 	private void addCreationButtonsTo( final JToolBar tb, final DrawingEditor editor ) {
 		addDefaultCreationButtonsTo( tb, editor, ButtonFactory.createDrawingActions( editor ), ButtonFactory.createSelectionActions( editor ) );
 	}
 
 	public void addDefaultCreationButtonsTo( final JToolBar tb, final DrawingEditor editor, final Collection< Action > drawingActions, final Collection< Action > selectionActions ) {
 
 		ButtonFactory.addSelectionToolTo( tb, editor, drawingActions, selectionActions );
 
 		final ResourceBundleUtil labels = ResourceBundleUtil.getBundle( "model.Labels" );
 		ButtonFactory.addToolTo( tb, editor, new SpimTool(), "edit.createSpim", labels );
 
 		tb.addSeparator();
 
 		final HashMap< AttributeKey, Object > a = new HashMap< AttributeKey, Object >();
 		org.jhotdraw.draw.AttributeKeys.FILL_COLOR.put( a, new Color( 0.0f, 1.0f, 0.0f, 0.1f ) );
 		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( a, new Color( 1.0f, 0.0f, 0.0f, 0.33f ) );
 		ButtonFactory.addToolTo( tb, editor, new BezierTool( new BezierFigure( true ), a ), "edit.createPolygon", ResourceBundleUtil.getBundle( "org.jhotdraw.draw.Labels" ) );
 
 		final HashMap< AttributeKey, Object > foreground = new HashMap< AttributeKey, Object >();
 		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( foreground, new Color( 1.0f, 0.0f, 0.0f, 0.33f ) );
 		org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH.put( foreground, 15d );
 		ButtonFactory.addToolTo( tb, editor, new BezierTool( new BezierFigure(), foreground ), "edit.scribbleForeground", labels );
 
 		final HashMap< AttributeKey, Object > background = new HashMap< AttributeKey, Object >();
 		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( background, new Color( 0.0f, 0.0f, 1.0f, 0.33f ) );
 		org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH.put( background, 15d );
 		ButtonFactory.addToolTo( tb, editor, new BezierTool( new BezierFigure(), background ), "edit.scribbleBackground", labels );
 
 		tb.add( ButtonFactory.createStrokeWidthButton( editor, new double[] { 5d, 10d, 15d }, ResourceBundleUtil.getBundle( "org.jhotdraw.draw.Labels" ) ) );
 	}
 
 	private void initComponents() {
 		scrollPane = new javax.swing.JScrollPane();
 
 		view = getInteractiveDrawingView( intervalViewDoubleType );
 
 		setLayout( new java.awt.BorderLayout() );
 
 		scrollPane.setHorizontalScrollBarPolicy( javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
 		scrollPane.setVerticalScrollBarPolicy( javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER );
 		scrollPane.setViewportView( view );
 
 		add( scrollPane, java.awt.BorderLayout.CENTER );
 
 		if ( toolbarVisible ) {
 			add( tb, toolbarLocation );
 		}
 	}
 
 	public void setToolBarLocation( final String location ) {
 		if ( location.equals( BorderLayout.WEST ) || location.equals( BorderLayout.EAST ) ) {
 			tb.setOrientation( JToolBar.VERTICAL );
 		} else {
 			tb.setOrientation( JToolBar.HORIZONTAL );
 		}
 
 		toolbarLocation = location;
 	}
 
 	public void setToolBarVisible( final boolean visible ) {
 		toolbarVisible = visible;
 		if ( toolbarVisible ) {
 			add( tb, toolbarLocation );
 		} else {
 			remove( tb );
 		}
 	}
 
 	public void loadAnnotations( final String filename ) {
 		try {
 			final Drawing drawing = createDrawing();
 
 			boolean success = false;
 			for ( final InputFormat sfi : drawing.getInputFormats() ) {
 				try {
 					sfi.read( new FileInputStream( filename ), drawing, true );
 					success = true;
 					break;
 				} catch ( final Exception e ) {
 					// try with the next input format
 				}
 			}
 			if ( !success ) {
 				final ResourceBundleUtil labels = ResourceBundleUtil.getBundle( "org.jhotdraw.app.Labels" );
 				throw new IOException( labels.getFormatted( "file.open.unsupportedFileFormat.message", filename ) );
 			}
 
 			SwingUtilities.invokeAndWait( new Runnable() {
 
 				@Override
 				public void run() {
 					view.setDrawing( drawing );
 				}
 			} );
 		} catch ( final InterruptedException e ) {
 			final InternalError error = new InternalError();
 			error.initCause( e );
 			throw error;
 		} catch ( final java.lang.reflect.InvocationTargetException e ) {
 			final InternalError error = new InternalError();
 			error.initCause( e );
 			throw error;
 		} catch ( final IOException e ) {
 			final InternalError error = new InternalError();
 			error.initCause( e );
 			throw error;
 		}
 	}
 
 	public void saveAnnotations( final String filename ) {
 		final Drawing drawing = view.getDrawing();
 		final OutputFormat outputFormat = drawing.getOutputFormats().get( 0 );
 		try {
 			outputFormat.write( new FileOutputStream( filename ), drawing );
 		} catch ( final IOException e ) {
 			final InternalError error = new InternalError();
 			error.initCause( e );
 			throw error;
 
 		}
 	}
 
 	public < T extends RealType< T > & NativeType< T >> void updateScreenImage( final IntervalView< T > viewImg ) {
 		final T min = Views.iterable( viewImg ).firstElement().copy();
 		final T max = min.copy();
 		ImglibUtil.computeMinMax( viewImg, min, max );
 
 		RealRandomAccessible< T > interpolated = null;
 		if ( viewImg.numDimensions() > 2 ) {
 			interpolated = Views.interpolate( Views.extendZero( Views.hyperSlice( viewImg, 2, 0 ) ), new NearestNeighborInterpolatorFactory< T >() );
 		} else {
 			interpolated = Views.interpolate( Views.extendZero( viewImg ), new NearestNeighborInterpolatorFactory< T >() );
 		}
 
 		final RealARGBConverter< T > converter = new RealARGBConverter< T >( min.getMinValue(), max.getMaxValue() );
 
 		updateDoubleTypeSourceAndConverter( interpolated, converter );
 	}
 
 	/**
 	 * Sets the image data to be displayed.
 	 * 
 	 * @param viewImg
 	 *            an IntervalView<DoubleType> containing the desired view
 	 *            onto the raw image data
 	 */
 	public void setDoubleTypeScreenImage( final IntervalView< DoubleType > viewImg ) {
 		final DoubleType min = new DoubleType();
 		final DoubleType max = new DoubleType();
 		ImglibUtil.computeMinMax( viewImg, min, max );
 
 		RealRandomAccessible< DoubleType > interpolated = null;
 		if ( viewImg.numDimensions() > 2 ) {
 			interpolated = Views.interpolate( Views.extendZero( Views.hyperSlice( viewImg, 2, 0 ) ), new NearestNeighborInterpolatorFactory< DoubleType >() );
 		} else {
 			interpolated = Views.interpolate( Views.extendZero( viewImg ), new NearestNeighborInterpolatorFactory< DoubleType >() );
 		}
 
 		final RealARGBConverter< DoubleType > converter = new RealARGBConverter< DoubleType >( min.get(), max.get() );
 
 		updateDoubleTypeSourceAndConverter( interpolated, converter );
 	}
 
 	/**
 	 * Sets the image data to be displayed.
 	 * 
 	 * @param rai
 	 *            an RandomAccessibleInterval<DoubleType> containing the desired
 	 *            view
 	 *            onto the raw image data
 	 */
 	public void setDoubleTypeScreenImage( final RandomAccessibleInterval< DoubleType > rai ) {
 		this.setDoubleTypeScreenImage( Views.interval( rai, rai ) );
 	}
 
 	/**
 	 * Sets the image data to be displayed.
 	 * 
 	 * @param glf
 	 *            the GrowthLineFrameto be displayed
 	 * @param viewImg
 	 *            an IntervalView<LongType> containing the desired view
 	 *            onto the raw image data
 	 */
 	public void setLongTypeScreenImage( final IntervalView< LongType > viewImg ) {
 
 		final LongType min = new LongType();
 		final LongType max = new LongType();
 		ImglibUtil.computeMinMax( viewImg, min, max );
 
 		final RealRandomAccessible< LongType > interpolated = Views.interpolate( Views.extendZero( viewImg ), new NearestNeighborInterpolatorFactory< LongType >() );
 
 		final RealARGBConverter< LongType > converter = new RealARGBConverter< LongType >( min.get(), max.get() );
 
 		updateDoubleTypeSourceAndConverter( interpolated, converter );
 	}
 
 	/**
 	 * Sets the image data to be displayed.
 	 * 
 	 * @param rai
 	 *            an RandomAccessibleInterval<LongType> containing the desired
 	 *            view
 	 *            onto the raw image data
 	 */
 	public void setLongTypeScreenImage( final RandomAccessibleInterval< LongType > rai ) {
 		this.setLongTypeScreenImage( Views.interval( rai, rai ) );
 	}
 
 	/**
 	 * Update the realRandomSource with new source.
 	 * 
 	 * @param source
 	 */
 	public void updateDoubleTypeSourceAndConverter( final RealRandomAccessible source, final RealARGBConverter converter ) {
 		currentInteractiveViewer2D.updateConverter( converter );
 		currentInteractiveViewer2D.updateSource( source );
 	}
 
 	private static < T extends Type< T > & Comparable< T > > void getMinMax( final IterableInterval< T > source, final T minValue, final T maxValue ) {
 		for ( final T t : source )
 			if ( minValue.compareTo( t ) > 0 )
 				minValue.set( t );
 			else if ( maxValue.compareTo( t ) < 0 ) maxValue.set( t );
 	}
 
 //    public < T extends RealType< T > & NativeType< T >> InteractiveViewer2D show( final Img<T> interval ) {
 //        final AffineTransform2D transform = new AffineTransform2D();
 //        InteractiveViewer2D iview = null;
 //
 //        System.out.println(interval.firstElement().getClass());
 //
 //
 //        {
 //            final T min = Views.iterable( interval ).firstElement().copy();
 //            final T max = min.copy();
 //            getMinMax( Views.iterable( interval ), min, max );
 //
 ////            RealRandomAccessible< T > interpolated = Views.interpolate( interval, new NLinearInterpolatorFactory<T>() );
 //            RealRandomAccessible< T > interpolated = Views.interpolate( Views.extendZero(interval), new NearestNeighborInterpolatorFactory<T>() );
 //            //final RealARGBConverter< T > converter = new RealARGBConverter< T >( min.getMinValue(), max.getMaxValue());
 //
 //            final LUTConverter< T > converter = new LUTConverter< T >( min.getMinValue(), max.getMaxValue(), ColorTables.FIRE);
 //            iview = new InteractiveViewer2D<T>((int)interval.max(0), (int)interval.max(1), Views.extendZero(interval), transform, converter);
 //        }
 //
 //        return iview;
 //    }
 
 	@Override
 	public void setPreferredSize( final Dimension dim ) {
 		currentInteractiveViewer2D.getJHotDrawDisplay().setPreferredSize( dim );
 	}
 	// End of variables declaration//GEN-END:variables
 
 }
