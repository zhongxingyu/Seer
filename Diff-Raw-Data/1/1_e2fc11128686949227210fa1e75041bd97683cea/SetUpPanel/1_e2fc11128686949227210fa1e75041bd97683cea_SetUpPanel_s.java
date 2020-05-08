 /**
  * 
  */
 package pl.edu.agh.cs.kraksim.main;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.Properties;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import pl.edu.agh.cs.kraksim.sna.centrality.CentrallityCalculator;
 import pl.edu.agh.cs.kraksim.sna.centrality.MeasureType;
 
 public class SetUpPanel extends JPanel
 {
   private static final long      serialVersionUID = -4635082252841397559L;
 
   private InputPanel             cityMapLocation;
   private InputPanel             travellingSchemeLocation;
   private InputPanel             statsOutputLocation;
   private InputPanel             algorithm;
   private InputPanel             yellowTransition;
   
   private JFrame                 myFrame          = null;               
   
   JButton                        init             = new JButton( "Załaduj" );
 
   JPanel                         filesPane        = null;
   
   MainVisualisationPanel         parent           = null;
 
   private Properties             params;
   private Properties             lastSessionParams;
   private String carMoveModel;
 
   public SetUpPanel(MainVisualisationPanel parent, Properties params) {
     super();
     this.parent = parent;
     initParams( params );
     initLayout();
   }
 
   public SetUpPanel(MainVisualisationPanel parent) {
     super();
     this.parent = parent;
     initParams ( new Properties() );
     initLayout();
   }
   
   private void initParams(Properties params) {
     this.params = params;
     this.lastSessionParams = new Properties();
     
     if (this.params.getProperty("lastSessionFile") != null) {
         try {
             lastSessionParams.load(new FileInputStream(params.getProperty("lastSessionFile")));
         } catch (Exception e) {
             // nothing
         }
     }
   }
   
   private String getParam(String name) {
       
       String param;
       if (lastSessionParams != null && lastSessionParams.getProperty(name)!= null) {
           param = lastSessionParams.getProperty( name );
       }
       else {
           param = params.getProperty( name );
       }
     return param;
   }
   
   private void storeParam(String key, String value) {
       
       this.lastSessionParams.put(key, value);
       
       String lastSessionFile = params.getProperty("lastSessionFile");
       if (lastSessionFile != null) {
           try {
               lastSessionParams.store(new FileOutputStream(lastSessionFile), null);
           } catch (Exception e) {
           }
       }
       
   }
   
   public void initLayout() {
     if (myFrame == null){
       myFrame = new JFrame ("Ustawienia");
       myFrame.setSize( 370, 280 );
       myFrame.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
       createLayout();
       myFrame.add( this );
     }
     init.setVisible( true );
     init.setEnabled( true );
     myFrame.setVisible( true );
   }
 
   private void createLayout() {
     setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ) );
     setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
 
     final JFileChooser fc = new JFileChooser();
     String workDir = getParam( "workDir" );
     fc.setCurrentDirectory( new File( workDir ) );
 
     filesPane = new JPanel();
     filesPane.setLayout( new GridLayout( 0, 1 ) );
     filesPane.setBorder( BorderFactory.createTitledBorder( "Parameters" ) );
 
     String fileLocation = getParam( "cityMapFile" );
     cityMapLocation = new InputPanel( "Mapa miasta", fileLocation,
     //                                      "C:/workspace/AGHworkspace/KraksimMSc/src/resources/tests/9xgridmodel.xml",
     //                                      "C:/workspace/AGHworkspace/KraksimMSc/src/resources/tests/Net/b.xml",
         20, fc );
     fileLocation = getParam( "travelSchemeFile" );
     travellingSchemeLocation = new InputPanel( "Schemat ruchu",
     //                                               "C:/workspace/AGHworkspace/KraksimMSc/src/resources/tests/9xgridtraffic.xml",
     //                                               "C:/workspace/AGHworkspace/KraksimMSc/src/resources/tests/Net/tb.xml",
         fileLocation, 20, fc );
     fileLocation = getParam( "statOutFile" );
     statsOutputLocation = new InputPanel( "Statystyki",
     //                                               "C:/workspace/AGHworkspace/KraksimMSc/src/resources/tests/9xgridtraffic.xml",
     fileLocation, 20, fc );
     algorithm = new InputPanel( "Algorithm", getParam( "algorithm" ), 20, null );
     yellowTransition = new InputPanel( "Yellow Duration", "3", 20, null );
 
     filesPane.add( cityMapLocation );
     filesPane.add( travellingSchemeLocation );
     filesPane.add( statsOutputLocation );
     filesPane.add( algorithm );
     filesPane.add( yellowTransition );
 
     add( filesPane );
     
 	  //Miary
 		JPanel measurePanel = new JPanel();
 		measurePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		measurePanel.setBorder(BorderFactory.createTitledBorder("Measures"));
 		measurePanel.setPreferredSize(new Dimension(600, 55));
 		measurePanel.setMinimumSize(new Dimension(600, 55));
 		measurePanel.setMaximumSize(new Dimension(1600, 55));
 		
 		JComboBox types = new JComboBox();
 		types.addItem(MeasureType.PageRank);
 		types.addItem(MeasureType.BetweenesCentrallity);
 		
 		types.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				JComboBox cb = (JComboBox)e.getSource();
 				CentrallityCalculator.measureType = (MeasureType)cb.getSelectedItem();
 			}
 		});
 
 	measurePanel.add(types);
 	add(measurePanel);
 
 	JPanel moveModelPane = new JPanel();
 	moveModelPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
 	moveModelPane.setBorder(BorderFactory
 			.createTitledBorder("Move model settings"));
 	JComboBox<String> moveModels = new JComboBox<String>();
 	moveModels.addItem("co");
 	moveModels.addItem("nagle");
 	moveModels.addItem("to po diable");
 	moveModels.addActionListener(new ActionListener() {
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			JComboBox<String> source = (JComboBox<String>)e.getSource();
 			carMoveModel = (String) source.getSelectedItem();
 		}
 	});
 	
 	moveModelPane.add(moveModels);
 		
 	String lastMoveModel = getParam("carMoveModel");
 	moveModels.setSelectedItem(lastMoveModel);
 	
 	add(moveModelPane);
 
     JPanel commandsPane = new JPanel();
     commandsPane.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
     commandsPane.setBorder( BorderFactory.createTitledBorder( "Commands" ) );
     commandsPane.setPreferredSize( new Dimension( 600, 55 ) );
     commandsPane.setMinimumSize( new Dimension( 600, 55 ) );
     commandsPane.setMaximumSize( new Dimension( 1600, 55 ) );
 
     //synchronize buttons first
     init.setEnabled( true );
 
     init.addActionListener( new ActionListener() {
 
       public void actionPerformed(ActionEvent e) {
         
         Properties props = new Properties();
         props.setProperty( "cityMapFile", cityMapLocation.getText() );
         storeParam       ( "cityMapFile", cityMapLocation.getText() );
         props.setProperty( "travelSchemeFile", travellingSchemeLocation.getText() );
         storeParam       ( "travelSchemeFile", travellingSchemeLocation.getText() );
         props.setProperty( "statOutFile", statsOutputLocation.getText() );
         storeParam       ( "statOutFile", statsOutputLocation.getText() );
         props.setProperty( "algorithm", algorithm.getText() );
         storeParam       ( "algorithm", algorithm.getText() );
         props.setProperty( "yellowTransition", yellowTransition.getText() );
         storeParam       ( "yellowTransition", yellowTransition.getText() );
         storeParam       ( "workDir", fc.getCurrentDirectory().toString() );
         props.setProperty("carMoveModel", carMoveModel);
         
         props.setProperty("visualization", "true");
         
         SetUpPanel.this.parent.initializeSimulation( props );
         init.setEnabled( false );
         myFrame.setVisible( false );
       }
 
     } );
     
     JButton cancel = new JButton ("Anuluj");
     cancel.addActionListener( new ActionListener() {
 
       public void actionPerformed(ActionEvent arg0) {
         SetUpPanel.this.myFrame.setVisible( false );
       }
     });
     
     commandsPane.add( init );
     commandsPane.add( cancel );
 
     add( commandsPane, BorderLayout.NORTH );
   }
 
   
   public void end() {
     init.setEnabled( true );
   }
 }
 
