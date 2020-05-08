 /**
  *
  */
 package com.jug.paramaxflow.gui;
 
 import ij.IJ;
 import ij.ImageJ;
 import ij.ImagePlus;
 import ij.WindowManager;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.KeyStroke;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import loci.formats.gui.ExtensionFileFilter;
 import net.imglib2.RandomAccessibleInterval;
 import net.imglib2.converter.Converters;
 import net.imglib2.img.ImagePlusAdapter;
 import net.imglib2.img.Img;
 import net.imglib2.img.display.imagej.ImageJFunctions;
 import net.imglib2.type.numeric.integer.LongType;
 import net.imglib2.type.numeric.real.DoubleType;
 import net.imglib2.util.Util;
 import net.imglib2.view.Views;
 
 import org.jhotdraw.draw.AttributeKey;
 import org.jhotdraw.draw.BezierFigure;
 import org.jhotdraw.draw.EllipseFigure;
 import org.jhotdraw.draw.LineFigure;
 import org.jhotdraw.draw.tool.BezierTool;
 import org.jhotdraw.draw.tool.CreationTool;
 import org.jhotdraw.util.ResourceBundleUtil;
 
 import view.component.IddeaComponent;
 
 import com.jug.fkt.Function1D;
 import com.jug.fkt.FunctionComposerDialog;
 import com.jug.fkt.SampledFunction1D;
 import com.jug.segmentation.SegmentationMagic;
 import com.jug.segmentation.SilentWekaSegmenter;
 import com.jug.util.IddeaUtil;
 import com.jug.util.converter.RealDoubleNormalizeConverter;
 
 /**
  * @author jug
  */
 public class ParaMaxFlowPanel extends JPanel implements ActionListener, ChangeListener {
 
 //	private static final String DEFAULT_PATH = "/Users/moon/Projects/git-projects/fjug/ImageJ_PlugIns/ParaMaxFlow/src/main/resources/";
 	private static final String DEFAULT_PATH = "/Users/jug/Dropbox/WorkingData/Repositories/GIT/ImageJ_PlugIns/ParaMaxFlow/src/main/resources";
 	final int PLOT_STEPS = 200;
 
 	private static ParaMaxFlowPanel main;
 
 	private static JFrame guiFrame;
 	private final Frame frame;
 	private final ImagePlus imgPlus;
 	private JTabbedPane tabsViews;
 
 	// IddeaCompontents for input and results + nested control elements
 	private final IddeaComponent icOrig;
 	private JButton bHistogramToUnaries;
 
 	private final IddeaComponent icSumImg;
 	private JButton bExportSumImg;
 
 	private final IddeaComponent icSeg;
 	private JSlider sliderSegmentation;
 	private JButton bExportCurrentSegmentation;
 	private long currSeg = -1;
 	private long numSols = -1;
 
 	// SETTING THE POTENTIALS
 	private JTabbedPane tabsPotentials;
 
 	// Potentials by functions (plus modulation)
 	private JTabbedPane tabsFunctionBasedPotentials;
 	private CostFunctionPanel costPlots;
 	private JButton bLoadCostFunctions;
 	private JButton bSaveCostFunctions;
 	private JButton bSetUnaries;
 	private JButton bSetPairwiseIsing;
 	private JButton bSetPairwiseEdge;
 	private final IddeaComponent icCostFunctionModulation;
 	private JButton bLoadCostModulationClassifier;
 	private JToggleButton bUseCostModulationClassifier;
 	private SilentWekaSegmenter< DoubleType > classifierCostsModulation;
 
 	// Potentials by classification
 	private JTabbedPane tabsClassificationBasedPotentials;
 	private final IddeaComponent icUnaryPotentials;
 	private JButton bLoadUnaryPotentialsClassifier;
 	private SilentWekaSegmenter< DoubleType > classifierUnaryCosts;
 	private JTextField txtClassifiedUnaryCostFactor;
 	private final IddeaComponent icPairwisePotentials;
 	private JButton bLoadPairwisePotentialsClassifier;
 	private SilentWekaSegmenter< DoubleType > classifierPairwiseCosts;
 	private JTextField txtCostClassifierIsing;
 
 	// Top level control buttons
 	private JButton bCompute;
 
 	// All the image container we need
 	private final RandomAccessibleInterval< DoubleType > imgOrig;     // original pixel values -- used for classification!
 	private final RandomAccessibleInterval< DoubleType > imgOrigNorm; // normalized pixel values -- used for everything else!
 	private RandomAccessibleInterval< DoubleType > imgCostModulationImage;
 	private RandomAccessibleInterval< DoubleType > imgUnaryCostImage;
 	private RandomAccessibleInterval< DoubleType > imgPairwiseCostImage;
 	private RandomAccessibleInterval< LongType > imgSumLong;
 	private RandomAccessibleInterval< LongType > imgSegmentation;
 
 	private FunctionComposerDialog funcComposerUnaries;
 	private FunctionComposerDialog funcComposerPairwiseEdge;
 
 	// The colors used to annotate foreground and background pixels
 	private final Color colorForeground = new Color( 0.0f, 1.0f, 0.0f, 0.25f );
 	private final Color colorBackground = new Color( 1.0f, 0.0f, 0.0f, 0.25f );
 
 	/**
 	 * @param imgPlus
 	 */
 	@SuppressWarnings( { "unchecked", "rawtypes" } )
 	public ParaMaxFlowPanel( final Frame frame, final ImagePlus imgPlus ) {
 		super( new BorderLayout( 5, 5 ) );
 		setBorder( BorderFactory.createEmptyBorder( 10, 15, 5, 15 ) );
 		this.frame = frame;
 		this.imgPlus = imgPlus;
 
 		final Img< ? > temp = ImagePlusAdapter.wrapNumeric( imgPlus );
 		this.imgOrig = Converters.convert( Views.interval( temp, temp ), new RealDoubleNormalizeConverter( 1.0 ), new DoubleType() );
 		this.imgOrigNorm = Converters.convert( Views.interval( temp, temp ), new RealDoubleNormalizeConverter( imgPlus.getStatistics().max ), new DoubleType() );
 
 		this.imgCostModulationImage = null;
 		this.imgUnaryCostImage = null;
 		this.imgPairwiseCostImage = null;
 		this.imgSumLong = null;
 		this.imgSegmentation = null;
 
 		this.icOrig = new IddeaComponent( Views.interval( imgOrigNorm, imgOrigNorm ) );
 		this.icSumImg = new IddeaComponent();
 		this.icSeg = new IddeaComponent();
 
 		this.icCostFunctionModulation = new IddeaComponent();
 		this.icUnaryPotentials = new IddeaComponent();
 		this.icPairwisePotentials = new IddeaComponent();
 
 		buildGui();
 
 		this.frame.setSize( 1200, 1024 );
 	}
 
 	private void buildGui() {
 		this.tabsViews = new JTabbedPane();
 
 		// ****************************************************************************************
 		// *** IMAGE VIEWER and NESTED CONTROLS
 		// ****************************************************************************************
 		installSegmentationToolbar( this.icOrig );
 		icOrig.showMenu( true );
 		this.icOrig.setToolBarLocation( BorderLayout.WEST );
 		this.icOrig.setToolBarVisible( true );
 		this.icOrig.setPreferredSize( new Dimension( imgPlus.getWidth(), imgPlus.getHeight() ) );
 		bHistogramToUnaries = new JButton( "set unary potentials from annotated pixels" );
 		bHistogramToUnaries.addActionListener( this );
 
 		this.icSumImg.installDefaultToolBar();
 		this.icSumImg.setToolBarLocation( BorderLayout.WEST );
 		this.icSumImg.setToolBarVisible( true );
 		this.icSumImg.setPreferredSize( new Dimension( imgPlus.getWidth(), imgPlus.getHeight() ) );
 		bExportSumImg = new JButton( "export to fiji" );
 		bExportSumImg.setEnabled( false );
 		bExportSumImg.addActionListener( this );
 
 		this.icSeg.installDefaultToolBar();
 		this.icSeg.setToolBarLocation( BorderLayout.WEST );
 		this.icSeg.setToolBarVisible( true );
 		this.icSeg.setPreferredSize( new Dimension( imgPlus.getWidth(), imgPlus.getHeight() ) );
 		sliderSegmentation = new JSlider( 0, 0 );
 		sliderSegmentation.addChangeListener( this );
 		bExportCurrentSegmentation = new JButton( "export to fiji" );
 		bExportCurrentSegmentation.setEnabled( false );
 		bExportCurrentSegmentation.addActionListener( this );
 
 		// ****************************************************************************************
 		// *** POTENTIAL RELATED STUFF
 		// ****************************************************************************************
 		this.tabsPotentials = new JTabbedPane();
 		this.tabsFunctionBasedPotentials = new JTabbedPane();
 		this.tabsClassificationBasedPotentials = new JTabbedPane();
 
 		// COST PANEL
 		// ----------
 		costPlots = new CostFunctionPanel();
 		costPlots.setFixedBoundsOnX( 0.0, 1.0 );
 		updateCostPlots();
 		costPlots.setPreferredSize( new Dimension( 500, 500 ) );
 
 		bLoadCostFunctions = new JButton( "load costs" );
 		bLoadCostFunctions.addActionListener( this );
 		bSaveCostFunctions = new JButton( "save costs" );
 		bSaveCostFunctions.addActionListener( this );
 
 		bSetUnaries = new JButton( "unary costs" );
 		bSetUnaries.addActionListener( this );
 		bSetPairwiseIsing = new JButton( "ising prior" );
 		bSetPairwiseIsing.addActionListener( this );
 		bSetPairwiseEdge = new JButton( "edge prior" );
 		bSetPairwiseEdge.addActionListener( this );
 
 		// COST MODULTATION CLASS.
 		// -----------------------
 		this.icCostFunctionModulation.installDefaultToolBar();
 		this.icCostFunctionModulation.setToolBarLocation( BorderLayout.WEST );
 		this.icCostFunctionModulation.setToolBarVisible( true );
 		this.icCostFunctionModulation.setPreferredSize( new Dimension( imgPlus.getWidth(), imgPlus.getHeight() ) );
 
 		bLoadCostModulationClassifier = new JButton( "load cost modulation classifier" );
 		bLoadCostModulationClassifier.addActionListener( this );
 		bUseCostModulationClassifier = new JToggleButton( "use it" );
 		bUseCostModulationClassifier.addActionListener( this );
 		bUseCostModulationClassifier.setEnabled( false );
 
 		// COST CLASSIFIER
 		// ---------------
 		this.icUnaryPotentials.installDefaultToolBar();
 		this.icUnaryPotentials.setToolBarLocation( BorderLayout.WEST );
 		this.icUnaryPotentials.setToolBarVisible( true );
 		this.icUnaryPotentials.setPreferredSize( new Dimension( imgPlus.getWidth(), imgPlus.getHeight() ) );
 		bLoadUnaryPotentialsClassifier = new JButton( "load unary potential classifier" );
 		bLoadUnaryPotentialsClassifier.addActionListener( this );
 		txtClassifiedUnaryCostFactor = new JTextField( "1.0", 5 );
 
 		this.icPairwisePotentials.installDefaultToolBar();
 		this.icPairwisePotentials.setToolBarLocation( BorderLayout.WEST );
 		this.icPairwisePotentials.setToolBarVisible( true );
 		this.icPairwisePotentials.setPreferredSize( new Dimension( imgPlus.getWidth(), imgPlus.getHeight() ) );
 		bLoadPairwisePotentialsClassifier = new JButton( "load pairwise potential classifier" );
 		bLoadPairwisePotentialsClassifier.addActionListener( this );
 		txtCostClassifierIsing = new JTextField( "0.0", 5 );
 
 		// ****************************************************************************************
 		// *** TOP LEVEL TEXTs AND CONTROLS
 		// ****************************************************************************************
 		final JPanel pControls = new JPanel();
 
 		final JTextArea textIntro = new JTextArea( "" + "Thanks to Vladimir Kolmogorov for native parametric max-flow code.\n" + "Classification models can be generated using the 'Trainable WEKA Segmentation'-plugin.\n" + "Bugs, comments, feedback? jug@mpi-cbg.de" );
 		textIntro.setBackground( new JButton().getBackground() );
 		textIntro.setEditable( false );
 		textIntro.setBorder( BorderFactory.createEmptyBorder( 0, 2, 5, 2 ) );
 
 		bCompute = new JButton( "start segmentation" );
 		bCompute.addActionListener( this );
 
 		// ****************************************************************************************
 		// *** PLUG ALL TOGETHER
 		// ****************************************************************************************
 		JPanel help = new JPanel( new BorderLayout() );
 
 		// top level tabs
 		// --------------
 		final JPanel pRawImageControls = new JPanel();
 		pRawImageControls.setLayout( new BoxLayout( pRawImageControls, BoxLayout.LINE_AXIS ) );
 		pRawImageControls.add( Box.createHorizontalGlue() );
 		pRawImageControls.add( bHistogramToUnaries );
 		pRawImageControls.add( Box.createHorizontalGlue() );
 		help.add( icOrig, BorderLayout.CENTER );
 		help.add( pRawImageControls, BorderLayout.SOUTH );
 		tabsViews.addTab( "raw data", help );
 
 		tabsViews.addTab( "potentials", tabsPotentials );
 
 		help = new JPanel( new BorderLayout() );
 		final JPanel pSumImageControls = new JPanel();
 		pSumImageControls.setLayout( new BoxLayout( pSumImageControls, BoxLayout.LINE_AXIS ) );
 		pSumImageControls.add( Box.createHorizontalGlue() );
 		pSumImageControls.add( bExportSumImg );
 		pSumImageControls.add( Box.createHorizontalGlue() );
 		help.add( icSumImg, BorderLayout.CENTER );
 		help.add( pSumImageControls, BorderLayout.SOUTH );
 		tabsViews.addTab( "sum img", help );
 
 		help = new JPanel( new BorderLayout() );
 		final JPanel pSegmentationControls = new JPanel();
 		pSegmentationControls.setLayout( new BoxLayout( pSegmentationControls, BoxLayout.LINE_AXIS ) );
 		pSegmentationControls.add( Box.createHorizontalGlue() );
 		pSegmentationControls.add( sliderSegmentation );
 		pSegmentationControls.add( bExportCurrentSegmentation );
 		pSegmentationControls.add( Box.createHorizontalGlue() );
 		help.add( icSeg, BorderLayout.CENTER );
 		help.add( pSegmentationControls, BorderLayout.SOUTH );
 		tabsViews.addTab( "segm. hyp.", help );
 
 		// potentials by function tabs
 		// ---------------------------
 		help = new JPanel( new BorderLayout() );
 		final JPanel pCostFunctionControls = new JPanel();
 		pCostFunctionControls.setLayout( new BoxLayout( pCostFunctionControls, BoxLayout.LINE_AXIS ) );
 		pCostFunctionControls.add( Box.createHorizontalGlue() );
 		pCostFunctionControls.add( bLoadCostFunctions );
 		pCostFunctionControls.add( bSaveCostFunctions );
 		pCostFunctionControls.add( Box.createHorizontalGlue() );
 		pCostFunctionControls.add( bSetUnaries );
 		pCostFunctionControls.add( bSetPairwiseIsing );
 		pCostFunctionControls.add( bSetPairwiseEdge );
 		pCostFunctionControls.add( Box.createHorizontalGlue() );
 		help.add( costPlots, BorderLayout.CENTER );
 		help.add( pCostFunctionControls, BorderLayout.SOUTH );
 		tabsFunctionBasedPotentials.addTab( "cost functions", help );
 
 		help = new JPanel( new BorderLayout() );
 		final JPanel pCostModulationControls = new JPanel();
 		pCostModulationControls.add( Box.createHorizontalGlue() );
 		pCostModulationControls.add( bLoadCostModulationClassifier );
 		pCostModulationControls.add( bUseCostModulationClassifier );
 		help.add( icCostFunctionModulation, BorderLayout.CENTER );
 		help.add( pCostModulationControls, BorderLayout.SOUTH );
 		tabsFunctionBasedPotentials.addTab( "cost modulation", help );
 		tabsPotentials.addTab( "function based", tabsFunctionBasedPotentials );
 
 		// potentials by classification tabs
 		// ---------------------------------
 		help = new JPanel( new BorderLayout() );
 		final JPanel pUnariesClassifierControls = new JPanel();
 		pUnariesClassifierControls.add( Box.createHorizontalGlue() );
 		pUnariesClassifierControls.add( bLoadUnaryPotentialsClassifier );
 		pUnariesClassifierControls.add( Box.createHorizontalGlue() );
 		pUnariesClassifierControls.add( new JLabel( "Normalize maxval to:" ) );
 		pUnariesClassifierControls.add( this.txtClassifiedUnaryCostFactor );
 		help.add( icUnaryPotentials, BorderLayout.CENTER );
 		help.add( pUnariesClassifierControls, BorderLayout.SOUTH );
 		tabsClassificationBasedPotentials.addTab( "unaries", help );
 		help = new JPanel( new BorderLayout() );
 		final JPanel pPairwiseClassifierControls = new JPanel();
 		pPairwiseClassifierControls.add( Box.createHorizontalGlue() );
 		pPairwiseClassifierControls.add( bLoadPairwisePotentialsClassifier );
 		pPairwiseClassifierControls.add( Box.createHorizontalGlue() );
 		pPairwiseClassifierControls.add( new JLabel( "Ising cost:" ) );
 		pPairwiseClassifierControls.add( this.txtCostClassifierIsing );
 		help.add( icPairwisePotentials, BorderLayout.CENTER );
 		help.add( pPairwiseClassifierControls, BorderLayout.SOUTH );
 		tabsClassificationBasedPotentials.addTab( "pairwise", help );
 		tabsPotentials.addTab( "classif. based", tabsClassificationBasedPotentials );
 
 		add( textIntro, BorderLayout.NORTH );
 		add( tabsViews, BorderLayout.CENTER );
 
 		pControls.add( bCompute );
 		add( pControls, BorderLayout.SOUTH );
 
 		// - - - - - - - - - - - - - - - - - - - - - - - -
 		// KEYSTROKE SETUP (usingInput- and ActionMaps)
 		// - - - - - - - - - - - - - - - - - - - - - - - -
 		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( '.' ), "MMGUI_bindings" );
 		this.getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( ',' ), "MMGUI_bindings" );
 
 		this.getActionMap().put( "MMGUI_bindings", new AbstractAction() {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed( final ActionEvent e ) {
 				if ( e.getActionCommand().equals( "," ) ) {
 					decrementSolutionToShow();
 				}
 				if ( e.getActionCommand().equals( "." ) ) {
 					incrementSolutionToShow();
 				}
 			}
 		} );
 	}
 
 	/**
 	 * @param iddeaComponent
 	 *            the IddeaCompnent this toolbar should be added to
 	 */
 	private void installSegmentationToolbar( final IddeaComponent iddeaComponent ) {
 		final ResourceBundleUtil labels = ResourceBundleUtil.getBundle( "org.jhotdraw.draw.Labels" );
 		final ResourceBundleUtil mylabels = ResourceBundleUtil.getBundle( "model.Labels" );
 
 		iddeaComponent.installDefaultToolBar();
 
 		iddeaComponent.addToolBarSeparator();
 
 		final HashMap< AttributeKey, Object > foreground = new HashMap< AttributeKey, Object >();
 		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( foreground, colorForeground );
 		org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH.put( foreground, 15d );
 		iddeaComponent.addTool( new BezierTool( new BezierFigure(), foreground ), "edit.scribbleForeground", mylabels );
 
 		final HashMap< AttributeKey, Object > background = new HashMap< AttributeKey, Object >();
 		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( background, colorBackground );
 		org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH.put( background, 15d );
 		iddeaComponent.addTool( new BezierTool( new BezierFigure(), background ), "edit.scribbleBackground", mylabels );
 
 		iddeaComponent.addToolStrokeWidthButton( new double[] { 1d, 5d, 10d, 15d, 30d } );
 
 		iddeaComponent.addToolBarSeparator();
 
 		final HashMap< AttributeKey, Object > line = new HashMap< AttributeKey, Object >();
 		org.jhotdraw.draw.AttributeKeys.FILL_COLOR.put( line, new Color( 0.0f, 0.0f, 1.0f, 0.1f ) );
 		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( line, new Color( 0.0f, 0.0f, 1.0f, 0.33f ) );
 		iddeaComponent.addTool( new CreationTool( new LineFigure(), line ), "edit.createLine", labels );
 
 		final HashMap< AttributeKey, Object > ellipse = new HashMap< AttributeKey, Object >();
 		org.jhotdraw.draw.AttributeKeys.FILL_COLOR.put( ellipse, new Color( 0.0f, 0.0f, 1.0f, 0.1f ) );
 		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( ellipse, new Color( 0.0f, 0.0f, 1.0f, 0.33f ) );
 		iddeaComponent.addTool( new CreationTool( new EllipseFigure(), ellipse ), "edit.createEllipse", labels );
 
 		final HashMap< AttributeKey, Object > scribble = new HashMap< AttributeKey, Object >();
 		org.jhotdraw.draw.AttributeKeys.FILL_COLOR.put( scribble, new Color( 0.0f, 0.0f, 1.0f, 0.1f ) );
 		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( scribble, new Color( 0.0f, 0.0f, 1.0f, 0.33f ) );
 		org.jhotdraw.draw.AttributeKeys.STROKE_WIDTH.put( scribble, 15d );
 		iddeaComponent.addTool( new BezierTool( new BezierFigure( false ), scribble ), "edit.createScribble", labels );
 
 		final HashMap< AttributeKey, Object > polygon = new HashMap< AttributeKey, Object >();
 		org.jhotdraw.draw.AttributeKeys.FILL_COLOR.put( polygon, new Color( 0.0f, 0.0f, 1.0f, 0.1f ) );
 		org.jhotdraw.draw.AttributeKeys.STROKE_COLOR.put( polygon, new Color( 0.0f, 0.0f, 1.0f, 0.33f ) );
 		iddeaComponent.addTool( new BezierTool( new BezierFigure( true ), polygon ), "edit.createPolygon", labels );
 
 	}
 
 	/**
 	 * Refreshes and displays all potential cost functions.
 	 */
 	private void updateCostPlots() {
 		costPlots.removeAllPlots();
 
 		final double[] xArray = new double[ PLOT_STEPS ];
 		final double[] costUnary = new double[ PLOT_STEPS ];
 		final double[] costIsing = new double[ PLOT_STEPS ];
 		final double[] costPairwiseX = new double[ PLOT_STEPS ];
 		final double[] costPairwiseY = new double[ PLOT_STEPS ];
 		final double[] costPairwiseZ = new double[ PLOT_STEPS ];
 		for ( int i = 0; i < PLOT_STEPS; i++ ) {
 			final double value = ( ( double ) i + 1 ) / PLOT_STEPS;
 			xArray[ i ] = value;
 			costUnary[ i ] = SegmentationMagic.getFktUnary().evaluate( value );
 			costIsing[ i ] = SegmentationMagic.getCostIsing();
 			costPairwiseX[ i ] = SegmentationMagic.getFktPairwiseX().evaluate( value );
 			costPairwiseY[ i ] = SegmentationMagic.getFktPairwiseY().evaluate( value );
 			costPairwiseZ[ i ] = SegmentationMagic.getFktPairwiseZ().evaluate( value );
 		}
 
 		costPlots.addLinePlot( "Unary costs", new Color( 80, 255, 80 ), xArray, costUnary );
 		costPlots.addLinePlot( "Ising costs", new Color( 127, 127, 255 ), xArray, costIsing );
 		costPlots.addLinePlot( "Pairwise costs (X)", new Color( 200, 64, 64 ), xArray, costPairwiseX );
 		costPlots.addLinePlot( "Pairwise costs (Y)", new Color( 200, 64, 64 ), xArray, costPairwiseY );
 		costPlots.addLinePlot( "Pairwise costs (Z)", new Color( 200, 64, 64 ), xArray, costPairwiseZ );
 
 	}
 
 	/**
 	 * Switches to the previous segmentation hypothesis.
 	 */
 	protected void decrementSolutionToShow() {
 		if ( currSeg > 0 ) {
 			this.currSeg--;
 		}
 		this.sliderSegmentation.setValue( ( int ) currSeg );
 	}
 
 	/**
 	 * Switches to the next segmentation hypothesis.
 	 */
 	protected void incrementSolutionToShow() {
 		if ( currSeg < this.numSols ) {
 			this.currSeg++;
 		}
 		this.sliderSegmentation.setValue( ( int ) currSeg );
 	}
 
 	/**
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed( final ActionEvent e ) {
 
 		if ( e.getSource().equals( bLoadCostModulationClassifier ) ) {
 			actionLoadCostModulationClassifier();
 			computeCostModulationImage();
 
 		} else if ( e.getSource().equals( bLoadUnaryPotentialsClassifier ) ) {
 			actionLoadUnaryCostClassifier();
 			computeUnaryCostImage();
 
 		} else if ( e.getSource().equals( bLoadPairwisePotentialsClassifier ) ) {
 			actionLoadPairwiseCostClassifier();
 			computePairwiseCostImage();
 
 		} else if ( e.getSource().equals( bLoadCostFunctions ) ) {
 			actionLoadCostFunction();
 
 		} else if ( e.getSource().equals( bSaveCostFunctions ) ) {
 			actionSaveCostFunctions();
 
 		} else if ( e.getSource().equals( bSetUnaries ) ) {
 			actionSetUnaryPotentialFunction();
 
 		} else if ( e.getSource().equals( bSetPairwiseIsing ) ) {
 			actionSetBinaryIsingPotentialFunction();
 
 		} else if ( e.getSource().equals( bSetPairwiseEdge ) ) {
 			actionSetBinaryEdgePotentialFunction();
 
 		} else if ( e.getSource().equals( bCompute ) ) {
 			actionStartParaMaxFlow();
 
 		} else if ( e.getSource().equals( bExportSumImg ) ) {
 			actionExportSumImgToFiji();
 
 		} else if ( e.getSource().equals( bExportCurrentSegmentation ) ) {
 			actionExportCurrentSegmentationToFiji();
 
 		} else if ( e.getSource().equals( bHistogramToUnaries ) ) {
 			actionCreateUnaryPotentialFunctionFromRawImageAnotations();
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 	public void actionCreateUnaryPotentialFunctionFromRawImageAnotations() {
 		if ( LongType.class.isInstance( Util.getTypeFromInterval( icOrig.getSourceImage() ) ) || DoubleType.class.isInstance( Util.getTypeFromInterval( icOrig.getSourceImage() ) ) ) {
 
 			final SampledFunction1D fHist = IddeaUtil.getHistogramFromInteractiveViewer( icOrig, colorForeground, 0, 1, PLOT_STEPS );
 			final SampledFunction1D fHistBG = IddeaUtil.getHistogramFromInteractiveViewer( icOrig, colorBackground, 0, 1, PLOT_STEPS );
 
 			fHist.normalizeToDiscreteDistribution();
 			fHistBG.normalizeToDiscreteDistribution();
 
 			fHist.mult( -1.0 );
 			fHist.add( fHistBG );
 			final double min = fHist.getMin();
 			final double max = fHist.getMax();
 			final double absmax = Math.max( Math.abs( min ), Math.abs( max ) );
 			if ( absmax != 0.0 ) fHist.mult( 1.0 / absmax );
 
 			SegmentationMagic.setFktUnary( fHist );
 			updateCostPlots();
			tabsViews.setSelectedIndex( 2 );
 		} else {
 			JOptionPane.showMessageDialog( this.getRootPane(), "Histograms (and therefore unaries) can only be created for images of pixel-types 'LongType' or 'DoubleType'." );
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void actionExportCurrentSegmentationToFiji() {
 		if ( this.imgSegmentation != null ) {
 			ImageJFunctions.show( this.imgSegmentation );
 		} else {
 			JOptionPane.showMessageDialog( this.getRootPane(), "The source image was not yet segmented sucessfully, hence there is no current segmentation!" );
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void actionExportSumImgToFiji() {
 		if ( this.imgSumLong != null ) {
 			ImageJFunctions.show( this.imgSumLong );
 		} else {
 			JOptionPane.showMessageDialog( this.getRootPane(), "The source image was not yet segmented sucessfully, hence there is no sum-img yet!" );
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void actionStartParaMaxFlow() {
 		boolean success = false;
 
 		if ( tabsFunctionBasedPotentials.isShowing() ) {
 			success = actionStartParaMaxFlowUsingFunctionBasedPotentials();
 		} else if ( tabsClassificationBasedPotentials.isShowing() ) {
 			success = actionStartParaMaxFlowUsingClassificationBasedPotentials();
 		} else {
 			JOptionPane.showMessageDialog( this.getRootPane(), "Tab 'potentials' must be selected to start the segmentation.\n(In order to know which type of costs should be used...)" );
 			return;
 		}
 
 		if ( success ) {
 			this.icSumImg.setLongTypeSourceImage( this.imgSumLong );
 			bExportSumImg.setEnabled( true );
 			bExportCurrentSegmentation.setEnabled( true );
 
 			this.numSols = SegmentationMagic.getNumSolutions();
 			this.sliderSegmentation.setMaximum( ( int ) this.numSols );
 			this.sliderSegmentation.setValue( ( int ) this.numSols / 2 );
 
 			this.tabsViews.setSelectedIndex( 2 );
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private boolean actionStartParaMaxFlowUsingClassificationBasedPotentials() {
 		try {
 			if ( this.imgUnaryCostImage != null && this.imgPairwiseCostImage != null ) {
 				final double unaryCostFactor = Double.parseDouble( this.txtClassifiedUnaryCostFactor.getText() );
 				final double isingCosts = Double.parseDouble( this.txtCostClassifierIsing.getText() );
 				this.imgSumLong = SegmentationMagic.returnPotentialImageBasedParamaxflowRegionSums( imgOrigNorm, unaryCostFactor, this.imgUnaryCostImage, isingCosts, this.imgPairwiseCostImage );
 				return true;
 			} else {
 				JOptionPane.showMessageDialog( this.getRootPane(), "Unary and pairwise cost images must be loaded! Segmentation aborted." );
 			}
 		} catch ( final NumberFormatException e ) {
 			JOptionPane.showMessageDialog( this.getRootPane(), "Ising costs or unary cost factor cannot be parsed as double-value." );
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 */
 	private boolean actionStartParaMaxFlowUsingFunctionBasedPotentials() {
 		if ( this.bUseCostModulationClassifier.isSelected() ) {
 			this.imgSumLong = SegmentationMagic.returnClassificationModulatedParamaxflowRegionSums( imgOrigNorm, imgCostModulationImage );
 		} else {
 			this.imgSumLong = SegmentationMagic.returnFunctionPotentialBasedParamaxflowRegionSums( imgOrigNorm );
 		}
 		return true;
 	}
 
 	/**
 	 * 
 	 */
 	public void actionSetBinaryEdgePotentialFunction() {
 		if ( funcComposerPairwiseEdge == null ) {
 			funcComposerPairwiseEdge = new FunctionComposerDialog( SegmentationMagic.getFktPairwiseX() );
 		}
 		final Function1D< Double > newFkt = funcComposerPairwiseEdge.open();
 		if ( newFkt != null ) {
 			SegmentationMagic.setFktPairwiseX( newFkt );
 			SegmentationMagic.setFktPairwiseY( newFkt );
 			SegmentationMagic.setFktPairwiseZ( newFkt );
 		}
 		updateCostPlots();
 	}
 
 	/**
 	 * 
 	 */
 	public void actionSetBinaryIsingPotentialFunction() {
 		try {
 			SegmentationMagic.setCostIsing( Double.parseDouble( JOptionPane.showInputDialog( "Please add an Ising cost:" ) ) );
 		} catch ( final NumberFormatException ex ) {
 			JOptionPane.showMessageDialog( this, "Parse error: please input a number that can be parsed as a double." );
 		} catch ( final NullPointerException ex2 ) {
 			// cancel was hit in JOptionPane
 		}
 		updateCostPlots();
 	}
 
 	/**
 	 * 
 	 */
 	public void actionSetUnaryPotentialFunction() {
 		if ( funcComposerUnaries == null ) {
 			funcComposerUnaries = new FunctionComposerDialog( SegmentationMagic.getFktUnary() );
 		}
 		final Function1D< Double > newFkt = funcComposerUnaries.open();
 		if ( newFkt != null ) {
 			SegmentationMagic.setFktUnary( newFkt );
 		}
 		updateCostPlots();
 	}
 
 	/**
 	 * 
 	 */
 	public void actionSaveCostFunctions() {
 		final JFileChooser fc = new JFileChooser( DEFAULT_PATH );
 		fc.addChoosableFileFilter( new ExtensionFileFilter( new String[] { "jug", "JUG" }, "JUG-cost-function-file" ) );
 
 		if ( fc.showSaveDialog( this.getTopLevelAncestor() ) == JFileChooser.APPROVE_OPTION ) {
 			File file = fc.getSelectedFile();
 			if ( !file.getAbsolutePath().endsWith( ".jug" ) && !file.getAbsolutePath().endsWith( ".JUG" ) ) {
 				file = new File( file.getAbsolutePath() + ".jug" );
 			}
 			try {
 				final FileOutputStream fileOut = new FileOutputStream( file );
 				final ObjectOutputStream out = new ObjectOutputStream( fileOut );
 				out.writeObject( SegmentationMagic.getFktUnary() );
 				out.writeObject( new Double( SegmentationMagic.getCostIsing() ) );
 				out.writeObject( SegmentationMagic.getFktPairwiseX() );
 				out.writeObject( SegmentationMagic.getFktPairwiseY() );
 				out.writeObject( SegmentationMagic.getFktPairwiseZ() );
 			} catch ( final IOException ex ) {
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void actionLoadCostFunction() {
 		final JFileChooser fc = new JFileChooser( DEFAULT_PATH );
 		fc.addChoosableFileFilter( new ExtensionFileFilter( new String[] { "jug", "JUG" }, "JUG-cost-function-file" ) );
 
 		if ( fc.showOpenDialog( this.getTopLevelAncestor() ) == JFileChooser.APPROVE_OPTION ) {
 			final File file = fc.getSelectedFile();
 			try {
 				final FileInputStream fileIn = new FileInputStream( file );
 				final ObjectInputStream in = new ObjectInputStream( fileIn );
 				SegmentationMagic.setFktUnary( ( Function1D< Double > ) in.readObject() );
 				SegmentationMagic.setCostIsing( ( ( Double ) in.readObject() ).doubleValue() );
 				SegmentationMagic.setFktPairwiseX( ( Function1D< Double > ) in.readObject() );
 				SegmentationMagic.setFktPairwiseY( ( Function1D< Double > ) in.readObject() );
 				SegmentationMagic.setFktPairwiseZ( ( Function1D< Double > ) in.readObject() );
 			} catch ( final IOException ex ) {
 				ex.printStackTrace();
 			} catch ( final ClassNotFoundException ex2 ) {
 				ex2.printStackTrace();
 			}
 			updateCostPlots();
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void computeCostModulationImage() {
 		if ( this.classifierCostsModulation != null ) {
 			SegmentationMagic.setClassifier( this.classifierCostsModulation );
 			this.imgCostModulationImage = SegmentationMagic.returnClassification( this.imgOrig );
 			this.icCostFunctionModulation.setDoubleTypeSourceImage( this.imgCostModulationImage );
 		} else {
 			JOptionPane.showMessageDialog( this.getRootPane(), "No classifier was loaded yet!" );
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void computeUnaryCostImage() {
 		if ( this.classifierUnaryCosts != null ) {
 			SegmentationMagic.setClassifier( this.classifierUnaryCosts );
 			this.imgUnaryCostImage = SegmentationMagic.returnClassification( this.imgOrig );
 			this.icUnaryPotentials.setDoubleTypeSourceImage( this.imgUnaryCostImage );
 		} else {
 			JOptionPane.showMessageDialog( this.getRootPane(), "No classifier was loaded yet!" );
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void computePairwiseCostImage() {
 		if ( this.classifierPairwiseCosts != null ) {
 			SegmentationMagic.setClassifier( this.classifierPairwiseCosts );
 			this.imgPairwiseCostImage = SegmentationMagic.returnClassification( this.imgOrig );
 			this.icPairwisePotentials.setDoubleTypeSourceImage( this.imgPairwiseCostImage );
 		} else {
 			JOptionPane.showMessageDialog( this.getRootPane(), "No classifier was loaded yet!" );
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void actionLoadCostModulationClassifier() {
 		final JFileChooser fc = new JFileChooser( DEFAULT_PATH );
 		fc.addChoosableFileFilter( new ExtensionFileFilter( new String[] { "model", "MODEL" }, "weka-model-file" ) );
 
 		if ( fc.showOpenDialog( guiFrame ) == JFileChooser.APPROVE_OPTION ) {
 			final File file = fc.getSelectedFile();
 			SegmentationMagic.setClassifier( file.getParent() + "/", file.getName() );
 			classifierCostsModulation = SegmentationMagic.getClassifier();
 			this.bUseCostModulationClassifier.setEnabled( true );
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void actionLoadUnaryCostClassifier() {
 		final JFileChooser fc = new JFileChooser( DEFAULT_PATH );
 		fc.addChoosableFileFilter( new ExtensionFileFilter( new String[] { "model", "MODEL" }, "weka-model-file" ) );
 
 		if ( fc.showOpenDialog( guiFrame ) == JFileChooser.APPROVE_OPTION ) {
 			final File file = fc.getSelectedFile();
 			SegmentationMagic.setClassifier( file.getParent() + "/", file.getName() );
 			classifierUnaryCosts = SegmentationMagic.getClassifier();
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void actionLoadPairwiseCostClassifier() {
 		final JFileChooser fc = new JFileChooser( DEFAULT_PATH );
 		fc.addChoosableFileFilter( new ExtensionFileFilter( new String[] { "model", "MODEL" }, "weka-model-file" ) );
 
 		if ( fc.showOpenDialog( guiFrame ) == JFileChooser.APPROVE_OPTION ) {
 			final File file = fc.getSelectedFile();
 			SegmentationMagic.setClassifier( file.getParent() + "/", file.getName() );
 			classifierPairwiseCosts = SegmentationMagic.getClassifier();
 		}
 	}
 
 	/**
 	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
 	 */
 	@Override
 	public void stateChanged( final ChangeEvent e ) {
 		if ( e.getSource().equals( sliderSegmentation ) ) {
 			currSeg = sliderSegmentation.getValue();
 			this.imgSegmentation = SegmentationMagic.returnSegmentation( imgSumLong, currSeg );
 			this.icSeg.setLongTypeSourceImage( imgSegmentation );
 		}
 	}
 
 	public static void main( final String[] args ) {
 		ImageJ temp = IJ.getInstance();
 
 		if ( temp == null ) {
 			temp = new ImageJ();
 			// IJ.open( "/Users/moon/Documents/clown.tif" );
 			// IJ.open( "/Users/moon/Pictures/spim/spim-0.tif" );
 			IJ.open( "/Users/jug/Desktop/clown.tif" );
 //			IJ.open( "/Users/jug/Desktop/demo.tif" );
 		}
 
 		final ImagePlus imgPlus = WindowManager.getCurrentImage();
 		if ( imgPlus == null ) {
 			IJ.error( "There must be an active, open window!" );
 			// System.exit( 1 );
 			return;
 		}
 
 		guiFrame = new JFrame( "ParaMaxFlow Segmentation" );
 		main = new ParaMaxFlowPanel( guiFrame, imgPlus );
 
 		// main.imgCanvas = imgPlus.getCanvas();
 
 		guiFrame.add( main );
 		guiFrame.setVisible( true );
 	}
 }
