 package views;
 
 import java.awt.CardLayout;
 import java.awt.ComponentOrientation;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.ListSelectionModel;
 import javax.swing.WindowConstants;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import models.TriangleCatalog;
 import controllers.MainFrameController;
 
 /**
  * @author iNCREDiBLE
  */
 public class MainFrame extends JFrame implements View {
 
   /**
    * <code>serialVersionUID</code>
    */
   private static final long   serialVersionUID = 1L;
   private TriangleCatalog     _catalog;
   private MainFrameController _controller;
 
   public enum StatusIcons {
    OK, @SuppressWarnings( "hiding" )
    ERROR, VIEW, INFO
   }
 
   public MainFrame() {
     initComponents();
     setStatus( "Ready", StatusIcons.OK );
   }
 
   public MainFrame( TriangleCatalog tc, MainFrameController mfc ) {
     this();
     _catalog = tc;
     _controller = mfc;
     _triangleList.setModel( tc.getListModel() );
     update();
   }
 
   public int getCurrentSelection() {
     return _triangleList.getSelectedIndex();
   }
 
   public void setCurrentSelection( int index ) {
     _triangleList.setSelectedIndex( index );
   }
 
   public void setStatus( String status, StatusIcons icon ) {
     switch ( icon ) {
       case OK :
         _statusIcon.setIcon( new ImageIcon( getClass().getResource( "/1258131295_button_ok.png" ) ) );
         break;
       case ERROR :
         _statusIcon.setIcon( new ImageIcon( getClass().getResource( "/1258189728_agt_action_fail.png" ) ) );
         break;
       case VIEW :
         _statusIcon.setIcon( new ImageIcon( getClass().getResource( "/1258132688_Search.png" ) ) );
         break;
       case INFO :
         _statusIcon.setIcon( new ImageIcon( getClass().getResource( "/1258191714_info_16.png" ) ) );
         break;
     }
     _statusBar.setText( status );
 
     final Timer timer = new Timer();
     timer.schedule( new TimerTask() {
 
       public void run() {
         _statusIcon.setIcon( new ImageIcon( getClass().getResource( "/1258132164_Hourglass.png" ) ) );
         _statusBar.setText( "Waiting for user interaction..." );
         timer.cancel();
       }
     }, 5000 );
 
   }
 
   private void exitButtonActionPerformed( ActionEvent e ) {
     _controller.exit();
   }
 
   private void removeButtonActionPerformed( ActionEvent e ) {
     _controller.remove();
   }
 
   private void addButtonActionPerformed( ActionEvent e ) {
     _controller.add();
   }
 
   private void triangleListValueChanged( ListSelectionEvent e ) {
     update();
   }
 
   private void aboutButtonActionPerformed( ActionEvent e ) {
     _controller.about();
   }
 
   private void viewComboboxItemStateChanged( ItemEvent e ) {
     int index = _viewCombobox.getSelectedIndex();
     CardLayout cl = (CardLayout)( _containerPanel.getLayout() );
     switch ( index ) {
       case 0 :
         cl.show( _containerPanel, "AreaPanel" );
         setStatus( "Switched to view basic information about the selected triangle", StatusIcons.VIEW );
         break;
       case 1 :
         cl.show( _containerPanel, "SidesAndAnglesPanel" );
         setStatus( "Switched to view the sides and angles of the currently selected triangle", StatusIcons.VIEW );
         break;
       case 2 :
         cl.show( _containerPanel, "HeightsPanel" );
         setStatus( "Switched to view the heights of the selected triangle", StatusIcons.VIEW );
         break;
 
     }
   }
 
   private void saveButtonActionPerformed( ActionEvent e ) {
     _controller.save();
   }
 
   private void openButtonActionPerformed( ActionEvent e ) {
     _controller.open();
   }
 
   private void initComponents() {
     // JFormDesigner - Component initialization - DO NOT MODIFY //GEN-BEGIN:initComponents
     _mainPanel = new JFrame();
     _toolbar = new JToolBar();
     _openButton = new JButton();
     _saveButton = new JButton();
     _addButton = new JButton();
     _removeButton = new JButton();
     _aboutButton = new JButton();
     _exitButton = new JButton();
     _previewPanel = new JSplitPane();
     _trianglePreview = new JPanel();
     _scrollPane = new JScrollPane();
     _triangleList = new JList();
     _informationPanel = new JPanel();
     _selectPanel = new JPanel();
     _viewLabel = new JLabel();
     _viewCombobox = new JComboBox();
     _containerPanel = new JPanel();
     _areaPanel = new JPanel();
     _triangleTypeLabel = new JLabel();
     _triangleTypeField = new JTextField();
     _perimeterLabel = new JLabel();
     _perimeterField = new JTextField();
     _areaLabel = new JLabel();
     _areaField = new JTextField();
     _sidesAndAnglesPanel = new JPanel();
     _sideALabel = new JLabel();
     _sideA = new JTextField();
     _angleALabel = new JLabel();
     _angleA = new JTextField();
     _sideBLabel = new JLabel();
     _sideB = new JTextField();
     _angleBLabel = new JLabel();
     _angleB = new JTextField();
     _sideCLabel = new JLabel();
     _sideC = new JTextField();
     _angleCLabel = new JLabel();
     _angleC = new JTextField();
     _heightPanel = new JPanel();
     _heightALabel = new JLabel();
     _heightA = new JTextField();
     _heightBLabel = new JLabel();
     _heightB = new JTextField();
     _heightCLabel = new JLabel();
     _heightC = new JTextField();
     _statusBarPanel = new JPanel();
     _statusIcon = new JButton();
     _statusBar = new JLabel();
 
     // ======== _mainPanel ========
     {
       _mainPanel.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
       _mainPanel.setIconImage( new ImageIcon( getClass().getResource( "/shapes.png" ) ).getImage() );
       _mainPanel.setTitle( "Triangle Calculator" );
       _mainPanel.setVisible( true );
       _mainPanel.setName( "MainFrame" );
       _mainPanel.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
       _mainPanel.setMinimumSize( new Dimension( 200, 200 ) );
       _mainPanel.setResizable( false );
       Container _mainPanelContentPane = _mainPanel.getContentPane();
       _mainPanelContentPane.setLayout( new GridBagLayout() );
       ( (GridBagLayout)_mainPanelContentPane.getLayout() ).columnWidths = new int[] { 10, 0 };
       ( (GridBagLayout)_mainPanelContentPane.getLayout() ).rowHeights = new int[] { 0, 0, 0, 0, 0 };
       ( (GridBagLayout)_mainPanelContentPane.getLayout() ).columnWeights = new double[] { 1.0, 1.0E-4 };
       ( (GridBagLayout)_mainPanelContentPane.getLayout() ).rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 1.0E-4 };
 
       // ======== _toolbar ========
       {
         _toolbar.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
         _toolbar.setFloatable( false );
 
         // ---- _openButton ----
         _openButton.setText( "Open" );
         _openButton.setIcon( new ImageIcon( getClass().getResource( "/1256324094_folder_orange_open.png" ) ) );
         _openButton.setToolTipText( "Open an existing file from the disk" );
         _openButton.addActionListener( new ActionListener() {
 
           public void actionPerformed( ActionEvent e ) {
             openButtonActionPerformed( e );
           }
         } );
         _toolbar.add( _openButton );
 
         // ---- _saveButton ----
         _saveButton.setText( "Save" );
         _saveButton.setIcon( new ImageIcon( getClass().getResource( "/save-32x32.png" ) ) );
         _saveButton.setToolTipText( "Save your work" );
         _saveButton.addActionListener( new ActionListener() {
 
           public void actionPerformed( ActionEvent e ) {
             saveButtonActionPerformed( e );
           }
         } );
         _toolbar.add( _saveButton );
         _toolbar.addSeparator( new Dimension( 30, 0 ) );
 
         // ---- _addButton ----
         _addButton.setText( "Add" );
         _addButton.setIcon( new ImageIcon( getClass().getResource( "/add-file-32x32.png" ) ) );
         _addButton.setToolTipText( "Add a new triangle" );
         _addButton.addActionListener( new ActionListener() {
 
           public void actionPerformed( ActionEvent e ) {
             addButtonActionPerformed( e );
           }
         } );
         _toolbar.add( _addButton );
 
         // ---- _removeButton ----
         _removeButton.setText( "Remove" );
         _removeButton.setIcon( new ImageIcon( getClass().getResource( "/delete-32x32.png" ) ) );
         _removeButton.setToolTipText( "Remove selected triangle from the list" );
         _removeButton.addActionListener( new ActionListener() {
 
           public void actionPerformed( ActionEvent e ) {
             removeButtonActionPerformed( e );
           }
         } );
         _toolbar.add( _removeButton );
         _toolbar.addSeparator( new Dimension( 30, 0 ) );
 
         // ---- _aboutButton ----
         _aboutButton.setText( "About" );
         _aboutButton.setIcon( new ImageIcon( getClass().getResource( "/about-32x32.png" ) ) );
         _aboutButton.setToolTipText( "Display information about this program" );
         _aboutButton.addActionListener( new ActionListener() {
 
           public void actionPerformed( ActionEvent e ) {
             aboutButtonActionPerformed( e );
           }
         } );
         _toolbar.add( _aboutButton );
 
         // ---- _exitButton ----
         _exitButton.setText( "Exit" );
         _exitButton.setIcon( new ImageIcon( getClass().getResource( "/1258135403_exit.png" ) ) );
         _exitButton.setToolTipText( "Quits the application" );
         _exitButton.addActionListener( new ActionListener() {
 
           public void actionPerformed( ActionEvent e ) {
             exitButtonActionPerformed( e );
           }
         } );
         _toolbar.add( _exitButton );
       }
       _mainPanelContentPane.add( _toolbar, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 0 ), 0, 0 ) );
 
       // ======== _previewPanel ========
       {
         _previewPanel.setBorder( new TitledBorder( "Preview" ) );
         _previewPanel.setDividerLocation( 475 );
         _previewPanel.setOneTouchExpandable( true );
         _previewPanel.setFocusable( false );
         _previewPanel.setRequestFocusEnabled( false );
 
         // ======== _trianglePreview ========
         {
           _trianglePreview.setPreferredSize( new Dimension( 200, 200 ) );
           _trianglePreview.setRequestFocusEnabled( false );
           _trianglePreview.setLayout( new FlowLayout() );
         }
         _previewPanel.setLeftComponent( _trianglePreview );
 
         // ======== _scrollPane ========
         {
           _scrollPane.setPreferredSize( new Dimension( 10, 10 ) );
           _scrollPane.setAutoscrolls( true );
           _scrollPane.setMaximumSize( new Dimension( 10, 10 ) );
 
           // ---- _triangleList ----
           _triangleList.setSelectedIndex( 0 );
           _triangleList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
           _triangleList.setVisibleRowCount( 20 );
           _triangleList.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
           _triangleList.setMinimumSize( new Dimension( 40, 50 ) );
           _triangleList.setMaximumSize( new Dimension( 40, 50 ) );
           _triangleList.setPreferredSize( new Dimension( 40, 50 ) );
           _triangleList.addListSelectionListener( new ListSelectionListener() {
 
             public void valueChanged( ListSelectionEvent e ) {
               triangleListValueChanged( e );
             }
           } );
           _scrollPane.setViewportView( _triangleList );
         }
         _previewPanel.setRightComponent( _scrollPane );
       }
       _mainPanelContentPane.add( _previewPanel, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 0 ), 0, 0 ) );
 
       // ======== _informationPanel ========
       {
         _informationPanel.setBorder( new TitledBorder( "Information" ) );
         _informationPanel.setLayout( new GridBagLayout() );
         ( (GridBagLayout)_informationPanel.getLayout() ).columnWidths = new int[] { 0, 0 };
         ( (GridBagLayout)_informationPanel.getLayout() ).rowHeights = new int[] { 0, 0, 0 };
         ( (GridBagLayout)_informationPanel.getLayout() ).columnWeights = new double[] { 1.0, 1.0E-4 };
         ( (GridBagLayout)_informationPanel.getLayout() ).rowWeights = new double[] { 0.0, 0.0, 1.0E-4 };
 
         // ======== _selectPanel ========
         {
           _selectPanel.setLayout( new GridBagLayout() );
           ( (GridBagLayout)_selectPanel.getLayout() ).columnWidths = new int[] { 0, 100, 0 };
           ( (GridBagLayout)_selectPanel.getLayout() ).rowHeights = new int[] { 0, 0 };
           ( (GridBagLayout)_selectPanel.getLayout() ).columnWeights = new double[] { 0.0, 0.0, 1.0E-4 };
           ( (GridBagLayout)_selectPanel.getLayout() ).rowWeights = new double[] { 0.0, 1.0E-4 };
 
           // ---- _viewLabel ----
           _viewLabel.setText( "View" );
           _selectPanel.add( _viewLabel, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
 
           // ---- _viewCombobox ----
           _viewCombobox.setModel( new DefaultComboBoxModel( new String[] { "Triangle Type, Area and Perimeter", "Sides and Angles", "Heights" } ) );
           _viewCombobox.addItemListener( new ItemListener() {
 
             public void itemStateChanged( ItemEvent e ) {
               viewComboboxItemStateChanged( e );
             }
           } );
           _selectPanel.add( _viewCombobox, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
         }
         _informationPanel.add( _selectPanel, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 0 ), 0, 0 ) );
 
         // ======== _containerPanel ========
         {
           _containerPanel.setLayout( new CardLayout() );
 
           // ======== _areaPanel ========
           {
             _areaPanel.setLayout( new GridBagLayout() );
             ( (GridBagLayout)_areaPanel.getLayout() ).columnWidths = new int[] { 80, 200, 0 };
             ( (GridBagLayout)_areaPanel.getLayout() ).rowHeights = new int[] { 0, 0, 0, 0 };
             ( (GridBagLayout)_areaPanel.getLayout() ).columnWeights = new double[] { 0.0, 0.0, 1.0E-4 };
             ( (GridBagLayout)_areaPanel.getLayout() ).rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };
 
             // ---- _triangleTypeLabel ----
             _triangleTypeLabel.setText( "Triangle Type" );
             _triangleTypeLabel.setLabelFor( _triangleTypeField );
             _areaPanel.add( _triangleTypeLabel, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 5 ), 0, 0 ) );
 
             // ---- _triangleTypeField ----
             _triangleTypeField.setEditable( false );
             _areaPanel.add( _triangleTypeField, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 0 ), 0, 0 ) );
 
             // ---- _perimeterLabel ----
             _perimeterLabel.setText( "Perimeter" );
             _perimeterLabel.setLabelFor( _perimeterField );
             _areaPanel.add( _perimeterLabel, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 5 ), 0, 0 ) );
 
             // ---- _perimeterField ----
             _perimeterField.setEditable( false );
             _areaPanel.add( _perimeterField, new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 0 ), 0, 0 ) );
 
             // ---- _areaLabel ----
             _areaLabel.setText( "Area" );
             _areaLabel.setLabelFor( _areaField );
             _areaPanel.add( _areaLabel, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
 
             // ---- _areaField ----
             _areaField.setEditable( false );
             _areaPanel.add( _areaField, new GridBagConstraints( 1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
           }
           _containerPanel.add( _areaPanel, "AreaPanel" );
 
           // ======== _sidesAndAnglesPanel ========
           {
             _sidesAndAnglesPanel.setLayout( new GridBagLayout() );
             ( (GridBagLayout)_sidesAndAnglesPanel.getLayout() ).columnWidths = new int[] { 55, 205, 80, 55, 200, 0 };
             ( (GridBagLayout)_sidesAndAnglesPanel.getLayout() ).rowHeights = new int[] { 0, 0, 0, 0 };
             ( (GridBagLayout)_sidesAndAnglesPanel.getLayout() ).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
             ( (GridBagLayout)_sidesAndAnglesPanel.getLayout() ).rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };
 
             // ---- _sideALabel ----
             _sideALabel.setText( "Side a" );
             _sidesAndAnglesPanel.add( _sideALabel, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 5 ), 0, 0 ) );
 
             // ---- _sideA ----
             _sideA.setEditable( false );
             _sidesAndAnglesPanel.add( _sideA, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 5 ), 0, 0 ) );
 
             // ---- _angleALabel ----
             _angleALabel.setText( "Angle A" );
             _sidesAndAnglesPanel.add( _angleALabel, new GridBagConstraints( 3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 5 ), 0, 0 ) );
 
             // ---- _angleA ----
             _angleA.setEditable( false );
             _sidesAndAnglesPanel.add( _angleA, new GridBagConstraints( 4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 0 ), 0, 0 ) );
 
             // ---- _sideBLabel ----
             _sideBLabel.setText( "Side b" );
             _sidesAndAnglesPanel.add( _sideBLabel, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 5 ), 0, 0 ) );
 
             // ---- _sideB ----
             _sideB.setEditable( false );
             _sidesAndAnglesPanel.add( _sideB, new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 5 ), 0, 0 ) );
 
             // ---- _angleBLabel ----
             _angleBLabel.setText( "Angle B" );
             _sidesAndAnglesPanel.add( _angleBLabel, new GridBagConstraints( 3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 5 ), 0, 0 ) );
 
             // ---- _angleB ----
             _angleB.setEditable( false );
             _sidesAndAnglesPanel.add( _angleB, new GridBagConstraints( 4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 0 ), 0, 0 ) );
 
             // ---- _sideCLabel ----
             _sideCLabel.setText( "Side c" );
             _sidesAndAnglesPanel.add( _sideCLabel, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
 
             // ---- _sideC ----
             _sideC.setEditable( false );
             _sidesAndAnglesPanel.add( _sideC, new GridBagConstraints( 1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
 
             // ---- _angleCLabel ----
             _angleCLabel.setText( "Angle C" );
             _sidesAndAnglesPanel.add( _angleCLabel, new GridBagConstraints( 3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
 
             // ---- _angleC ----
             _angleC.setEditable( false );
             _sidesAndAnglesPanel.add( _angleC, new GridBagConstraints( 4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
           }
           _containerPanel.add( _sidesAndAnglesPanel, "SidesAndAnglesPanel" );
 
           // ======== _heightPanel ========
           {
             _heightPanel.setLayout( new GridBagLayout() );
             ( (GridBagLayout)_heightPanel.getLayout() ).columnWidths = new int[] { 80, 200, 0 };
             ( (GridBagLayout)_heightPanel.getLayout() ).rowHeights = new int[] { 0, 0, 0, 0 };
             ( (GridBagLayout)_heightPanel.getLayout() ).columnWeights = new double[] { 0.0, 0.0, 1.0E-4 };
             ( (GridBagLayout)_heightPanel.getLayout() ).rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };
 
             // ---- _heightALabel ----
             _heightALabel.setText( "Height for a" );
             _heightPanel.add( _heightALabel, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 5 ), 0, 0 ) );
 
             // ---- _heightA ----
             _heightA.setEditable( false );
             _heightPanel.add( _heightA, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 0 ), 0, 0 ) );
 
             // ---- _heightBLabel ----
             _heightBLabel.setText( "Height for b" );
             _heightPanel.add( _heightBLabel, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 5 ), 0, 0 ) );
 
             // ---- _heightB ----
             _heightB.setEditable( false );
             _heightPanel.add( _heightB, new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 0 ), 0, 0 ) );
 
             // ---- _heightCLabel ----
             _heightCLabel.setText( "Height for c" );
             _heightPanel.add( _heightCLabel, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
 
             // ---- _heightC ----
             _heightC.setEditable( false );
             _heightPanel.add( _heightC, new GridBagConstraints( 1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
           }
           _containerPanel.add( _heightPanel, "HeightsPanel" );
         }
         _informationPanel.add( _containerPanel, new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
       }
       _mainPanelContentPane.add( _informationPanel, new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 5, 0 ), 0, 0 ) );
 
       // ======== _statusBarPanel ========
       {
         _statusBarPanel.setBorder( new BevelBorder( BevelBorder.LOWERED ) );
         _statusBarPanel.setLayout( new GridBagLayout() );
         ( (GridBagLayout)_statusBarPanel.getLayout() ).columnWidths = new int[] { 25, 0, 0 };
         ( (GridBagLayout)_statusBarPanel.getLayout() ).rowHeights = new int[] { 20, 0 };
         ( (GridBagLayout)_statusBarPanel.getLayout() ).columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
         ( (GridBagLayout)_statusBarPanel.getLayout() ).rowWeights = new double[] { 0.0, 1.0E-4 };
 
         // ---- _statusIcon ----
         _statusIcon.setPreferredSize( new Dimension( 10, 10 ) );
         _statusIcon.setFocusable( false );
         _statusBarPanel.add( _statusIcon, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 5 ), 0, 0 ) );
 
         // ---- _statusBar ----
         _statusBar.setText( "Ready" );
         _statusBarPanel.add( _statusBar, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
       }
       _mainPanelContentPane.add( _statusBarPanel, new GridBagConstraints( 0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
       _mainPanel.setSize( 615, 625 );
       _mainPanel.setLocationRelativeTo( null );
     }
     // JFormDesigner - End of component initialization //GEN-END:initComponents
   }
 
   public void update() {
     int index = getCurrentSelection();
 
     if ( ( _catalog.size() == 0 ) || ( index == -1 ) ) {
       // Reset the _areaPanel fields
       _perimeterField.setText( "" );
       _areaField.setText( "" );
       _triangleTypeField.setText( "" );
 
       // Reset the _sidesAndAnglesPanel fields
       _sideA.setText( "" );
       _sideB.setText( "" );
       _sideC.setText( "" );
       _angleA.setText( "" );
       _angleB.setText( "" );
       _angleC.setText( "" );
 
       // Reset the _heightPanel fields
       _heightA.setText( "" );
       _heightB.setText( "" );
       _heightC.setText( "" );
 
       _viewCombobox.setEnabled( false );
       return;
     }
 
     // Set the _areaPanel fields
     double perimeter = _catalog.getTriangleAt( index ).getPerimeter();
     double area = _catalog.getTriangleAt( index ).getArea();
     String type = _catalog.getTriangleAt( index ).getType();
 
     _perimeterField.setText( String.format( "%.4f", perimeter ) );
     _areaField.setText( String.format( "%.4f", area ) );
     _triangleTypeField.setText( type );
 
     // Set the _sidesAndAnglesPanel fields
     double side1 = _catalog.getTriangleAt( index ).getSide( 0 );
     double side2 = _catalog.getTriangleAt( index ).getSide( 1 );
     double side3 = _catalog.getTriangleAt( index ).getSide( 2 );
 
     double angle1 = _catalog.getTriangleAt( index ).getAngle( 0 );
     double angle2 = _catalog.getTriangleAt( index ).getAngle( 1 );
     double angle3 = _catalog.getTriangleAt( index ).getAngle( 2 );
 
     _sideA.setText( String.format( "%.2f", side1 ) );
     _sideB.setText( String.format( "%.2f", side2 ) );
     _sideC.setText( String.format( "%.2f", side3 ) );
 
     _angleA.setText( String.format( "%.2f", angle1 ) );
     _angleB.setText( String.format( "%.2f", angle2 ) );
     _angleC.setText( String.format( "%.2f", angle3 ) );
 
     // Set the _heightPanel fields
     double hA = _catalog.getTriangleAt( index ).getHeight( 0 );
     double hB = _catalog.getTriangleAt( index ).getHeight( 1 );
     double hC = _catalog.getTriangleAt( index ).getHeight( 2 );
 
     _heightA.setText( String.format( "%.4f", hA ) );
     _heightB.setText( String.format( "%.4f", hB ) );
     _heightC.setText( String.format( "%.4f", hC ) );
 
     _viewCombobox.setEnabled( true );
   }
 
   // JFormDesigner - Variables declaration - DO NOT MODIFY //GEN-BEGIN:variables
   private JFrame      _mainPanel;
   private JToolBar    _toolbar;
   private JButton     _openButton;
   private JButton     _saveButton;
   private JButton     _addButton;
   private JButton     _removeButton;
   private JButton     _aboutButton;
   private JButton     _exitButton;
   private JSplitPane  _previewPanel;
   private JPanel      _trianglePreview;
   private JScrollPane _scrollPane;
   private JList       _triangleList;
   private JPanel      _informationPanel;
   private JPanel      _selectPanel;
   private JLabel      _viewLabel;
   private JComboBox   _viewCombobox;
   private JPanel      _containerPanel;
   private JPanel      _areaPanel;
   private JLabel      _triangleTypeLabel;
   private JTextField  _triangleTypeField;
   private JLabel      _perimeterLabel;
   private JTextField  _perimeterField;
   private JLabel      _areaLabel;
   private JTextField  _areaField;
   private JPanel      _sidesAndAnglesPanel;
   private JLabel      _sideALabel;
   private JTextField  _sideA;
   private JLabel      _angleALabel;
   private JTextField  _angleA;
   private JLabel      _sideBLabel;
   private JTextField  _sideB;
   private JLabel      _angleBLabel;
   private JTextField  _angleB;
   private JLabel      _sideCLabel;
   private JTextField  _sideC;
   private JLabel      _angleCLabel;
   private JTextField  _angleC;
   private JPanel      _heightPanel;
   private JLabel      _heightALabel;
   private JTextField  _heightA;
   private JLabel      _heightBLabel;
   private JTextField  _heightB;
   private JLabel      _heightCLabel;
   private JTextField  _heightC;
   private JPanel      _statusBarPanel;
   private JButton     _statusIcon;
   private JLabel      _statusBar;
   // JFormDesigner - End of variables declaration //GEN-END:variables
 
 }
