 package com.hifiremote.jp1;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class LearnedSignalDialog.
  */
 public class LearnedSignalDialog extends JDialog implements ActionListener, DocumentListener
 {
 
   /**
    * Show dialog.
    * 
    * @param locationComp
    *          the location comp
    * @param learnedSignal
    *          the learned signal
    * @return the learned signal
    */
   public static LearnedSignal showDialog( Component locationComp, LearnedSignal learnedSignal,
       RemoteConfiguration config )
   {
     if ( dialog == null )
     {
       dialog = new LearnedSignalDialog( locationComp );
     }
 
     dialog.setRemoteConfiguration( config );
     dialog.setLearnedSignal( learnedSignal );
     
     // Set preferred size of advanced button to that for the wider 
     // of the two possible button captions
     dialog.setAdvancedButtonText( false );
     dialog.pack();
     dialog.advancedButton.setPreferredSize( dialog.advancedButton.getSize() );
     dialog.setAdvancedButtonText( dialog.advancedArea.isVisible() );
     dialog.applyButton.setEnabled( false );
     dialog.pack();
     
     dialog.setLocationRelativeTo( locationComp );
     dialog.setVisible( true );
     return dialog.learnedSignal;
   }
   
   public static void reset()
   {
     if ( dialog != null )
     {
       dialog.dispose();
       dialog = null;
     }
   }
 
   /**
    * Instantiates a new learned signal dialog.
    * 
    * @param c
    *          the c
    */
   private LearnedSignalDialog( Component c )
   {
     super( ( JFrame )SwingUtilities.getRoot( c ) );
     setTitle( "Learned Signal Details" );
     setModal( true );
 
     JComponent contentPane = ( JComponent )getContentPane();
     contentPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
 
     JPanel topPanel = new JPanel( new BorderLayout() );
     contentPane.add( topPanel, BorderLayout.PAGE_START );
     
     // Add the bound device and key controls
     JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ) );
     panel.setAlignmentX( Component.LEFT_ALIGNMENT );
     panel.setBorder( BorderFactory.createTitledBorder( "Bound Key" ) );
     panel.add( new JLabel( "Device:" ) );
     panel.add( boundDevice );
     panel.add( Box.createHorizontalStrut( 5 ) );
     panel.add( new JLabel( "Key:" ) );
     panel.add( boundKey );
     panel.add( shift );
     panel.add( xShift );    
     topPanel.add( panel, BorderLayout.LINE_START );
     boundKey.addActionListener( this );
     shift.addActionListener( this );
     xShift.addActionListener( this );
     
     topPanel.add( advancedButton, BorderLayout.LINE_END );
     advancedButton.setToolTipText( "Shows or hides the signal timing details" );
     advancedButton.addActionListener( this );
         
     signalTextArea.setEditable( true );
     signalTextArea.setLineWrap( true );
     signalTextArea.setWrapStyleWord( true );
     signalTextArea.getDocument().addDocumentListener(new DocumentListener() {
       public void changedUpdate(DocumentEvent e) {
           signalTextChanged();
       }
       public void removeUpdate(DocumentEvent e) {
           signalTextChanged();
       }
       public void insertUpdate(DocumentEvent e) {
           signalTextChanged();
       }
     });
     signalTextArea.setToolTipText( "Edits to Signal Data do not take effect until you press Apply or OK" );
     JScrollPane scrollPane = new JScrollPane( signalTextArea );
     scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Signal Data" ), scrollPane.getBorder() ) );
     topPanel.add( scrollPane, BorderLayout.PAGE_END );
     
     table = new JP1Table( model );
     table.setCellSelectionEnabled( false );
     table.setRowSelectionAllowed( true );
     Dimension d = table.getPreferredScrollableViewportSize();
     d.width = table.getPreferredSize().width;
     d.height = 3 * table.getRowHeight();
     table.setPreferredScrollableViewportSize( d );
     table.initColumns( model );
     scrollPane = new JScrollPane( table );
     scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Decodes" ), scrollPane.getBorder() ) );
     contentPane.add( scrollPane, BorderLayout.CENTER );
     
     advancedArea = Box.createVerticalBox();
     advancedArea.setBorder( BorderFactory.createTitledBorder( "Advanced Details" ) );
 
     // add panel with rounding/analysis controls
     panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 1, 1 ) );
     panel.add( new JLabel( " Round To: " ) );
     panel.add( burstRoundBox );
    panel.add( new JLabel( "  Anaylzer: ") );
     panel.add( analyzerBox );
     panel.add( new JLabel( "  Analysis: ") );
     panel.add( analysisBox );
     panel.add( new JLabel( "  ") );
     panel.add( analysisMessageLabel );
     advancedArea.add( panel );
     
     // setup analyzer/analysis boxes and message label
     analysisMessageLabel.setText( null );
     ItemListener i = new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
           if ( e.getStateChange() == ItemEvent.SELECTED )
             onAnalysisChange();
         }
     };
     analyzerBox.addItemListener( i );
     analysisBox.addItemListener( i );
     
     // setup round to box
    burstRoundBox.setColumns( 4 );
     burstRoundBox.getDocument().addDocumentListener(new DocumentListener() {
       public void changedUpdate(DocumentEvent e) {
         setAdvancedAreaTextFields();
       }
       public void removeUpdate(DocumentEvent e) {
         setAdvancedAreaTextFields();
       }
       public void insertUpdate(DocumentEvent e) {
         setAdvancedAreaTextFields();
       }
     });
     
     burstTextArea.setEditable( false );
     burstTextArea.setLineWrap( true );
     burstTextArea.setWrapStyleWord( true );
     scrollPane = new JScrollPane( burstTextArea );
     scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Bursts" ), scrollPane.getBorder() ) );
     advancedArea.add( scrollPane );
    // temporarily hiding bursts...may remove entirely
    burstTextArea.getParent().getParent().setVisible( false );
 
     onceDurationTextArea.setEditable( false );
     onceDurationTextArea.setLineWrap( true );
     onceDurationTextArea.setWrapStyleWord( true );
     scrollPane = new JScrollPane( onceDurationTextArea );
     scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Sent Once" ), scrollPane.getBorder() ) );
     advancedArea.add( scrollPane );
 
     repeatDurationTextArea.setEditable( false );
     repeatDurationTextArea.setLineWrap( true );
     repeatDurationTextArea.setWrapStyleWord( true );
     scrollPane = new JScrollPane( repeatDurationTextArea );
     scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Sent Repeatedly" ), scrollPane.getBorder() ) );
     advancedArea.add( scrollPane );
 
     extraDurationTextArea.setEditable( false );
     extraDurationTextArea.setLineWrap( true );
     extraDurationTextArea.setWrapStyleWord( true );
     scrollPane = new JScrollPane( extraDurationTextArea );
     scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Sent on Release" ), scrollPane.getBorder() ) );
     advancedArea.add( scrollPane );
 
     Box bottomBox = Box.createVerticalBox();
     contentPane.add( bottomBox, BorderLayout.PAGE_END );
     bottomBox.add( advancedArea );
     
     // Add the action buttons
     JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
     bottomBox.add( buttonPanel );
     
     applyButton.addActionListener( this );
     applyButton.setEnabled( false );
     applyButton.setToolTipText( "Apply edits made in the Signal Data panel without closing dialog" );
     buttonPanel.add( applyButton );
 
     okButton.addActionListener( this );
     okButton.setToolTipText( "Apply all edits and exit dialog" );
     buttonPanel.add( okButton );
 
     cancelButton.addActionListener( this );
     cancelButton.setToolTipText( "Abandon all edits and exit dialog" );
     buttonPanel.add( cancelButton );
     
     advancedArea.setVisible( false );
     setAdvancedButtonText( advancedArea.isVisible() );
   }
 
   /**
    * Sets the learned signal.
    * 
    * @param learnedSignal
    *          the new learned signal
    */
   private void setLearnedSignal( LearnedSignal learnedSignal )
   {
     table.initColumns( model );
     if ( learnedSignal == null )
     {
       this.learnedSignal = new LearnedSignal( 0, 0, new Hex(), null );
       boundKey.setSelectedIndex( 0 );
       shift.setSelected( false );
       xShift.setSelected( false );
       model.set( this.learnedSignal );
       signalTextArea.setText( null );
       burstRoundBox.setText( null );
       burstTextArea.setText( null );
       onceDurationTextArea.setText( null );
       repeatDurationTextArea.setText( null );
       extraDurationTextArea.setText( null );
       analyzerBox.setModel( new DefaultComboBoxModel( new String[] { "..." } ) );
       analysisBox.setModel( new DefaultComboBoxModel( new String[] { "..." } ) );
       return;
     }
     this.learnedSignal = learnedSignal;
     Remote remote = config.getRemote();
     boundDevice.setSelectedItem( remote.getDeviceButton( learnedSignal.getDeviceButtonIndex() ) );
     setButton( learnedSignal.getKeyCode(), boundKey, shift, xShift );
     model.set( learnedSignal );
     signalTextLock = true;
     signalTextArea.setText( learnedSignal.getSignalHex( config.getRemote() ).toString() );
     signalTextLock = false;
     
     LearnedSignalTimingAnalyzer timingAnalyzer = this.learnedSignal.getTimingAnalyzer();
     if ( !timingAnalyzer.getIsValid() )
     {
       burstRoundBox.setText( null );
       analysisMessageLabel.setText( null );
       burstTextArea.setText( "Unable to unpack learned signal data...analysis not possible." );
       onceDurationTextArea.setText( null );
       repeatDurationTextArea.setText( null );
       extraDurationTextArea.setText( null );
       burstTextArea.setRows( 1 );
       onceDurationTextArea.setRows( 1 );
       repeatDurationTextArea.setRows( 1 );
       extraDurationTextArea.setRows( 1 );
       onceDurationTextArea.getParent().getParent().setVisible( false );
       repeatDurationTextArea.getParent().getParent().setVisible( false );
       extraDurationTextArea.getParent().getParent().setVisible( false );      
       analyzerBox.setModel( new DefaultComboBoxModel( new String[] { "..." } ) );
       analysisBox.setModel( new DefaultComboBoxModel( new String[] { "..." } ) );
       pack();       
     }
     else
     {
       analysisUpdating = true;
       advancedAreaUpdating = true;
 
       analyzerBox.setModel( new DefaultComboBoxModel( timingAnalyzer.getAnalyzerNames() ) );
       analyzerBox.setSelectedItem( timingAnalyzer.getSelectedAnalyzer().getName() );
       analysisBox.setModel( new DefaultComboBoxModel( timingAnalyzer.getSelectedAnalyzer().getAnalysisNames() ) );
       analysisBox.setSelectedItem( timingAnalyzer.getSelectedAnalysisName() );
       analysisMessageLabel.setText( timingAnalyzer.getSelectedAnalysis().getMessage() );
       burstRoundBox.setText( Integer.toString( timingAnalyzer.getSelectedAnalyzer().getRoundTo() ) );
 
       advancedAreaUpdating = false;
       analysisUpdating = false;
 
       setAdvancedAreaTextFields();
     }
   }
 
   private void onAnalysisChange()
   {
     if ( analysisUpdating )
       return;
     analysisUpdating = true;
     
     LearnedSignalTimingAnalyzer timingAnalyzer = this.learnedSignal.getTimingAnalyzer();
     if ( !timingAnalyzer.getSelectedAnalyzer().getName().equals( analyzerBox.getSelectedItem().toString() ) )
     {
       timingAnalyzer.setSelectedAnalyzer( analyzerBox.getSelectedItem().toString() ); // will auto select preferred analysis
       analysisBox.setModel( new DefaultComboBoxModel( timingAnalyzer.getSelectedAnalyzer().getAnalysisNames() ) );
       analysisBox.setSelectedItem( timingAnalyzer.getSelectedAnalysisName() );
       burstRoundBox.setText( Integer.toString( timingAnalyzer.getSelectedAnalyzer().getRoundTo() ) );
       // setting burst text will trigger this so not called here
       //setAdvancedAreaTextFields();
     }
     else
     {
       timingAnalyzer.setSelectedAnalysisName( analysisBox.getSelectedItem().toString() );
       setAdvancedAreaTextFields();
     }
     analysisUpdating = false;
   }
 
   private void setAdvancedAreaTextFields()
   {
     if ( advancedAreaUpdating )
       return;
     advancedAreaUpdating = true;
 
     int r = 1;
     String roundText = burstRoundBox.getText();
     if ( roundText != null && !roundText.isEmpty() )
       try { r = Integer.parseInt( roundText ); }
       catch (NumberFormatException e) { r = 1; }
     
     this.learnedSignal.getTimingAnalyzer().getSelectedAnalyzer().setRoundTo( r );
 
     LearnedSignalTimingAnalysis analysis = this.learnedSignal.getTimingAnalyzer().getSelectedAnalysis();
     analysisMessageLabel.setText( analysis.getMessage() );
 
     String temp = analysis.getBurstString();
     burstTextArea.setText( temp );
     burstTextArea.setRows( (int)Math.ceil( (double)temp.length() / 75.0 ) );
 
     temp = analysis.getOneTimeDurationString();
     onceDurationTextArea.setText( temp );
     onceDurationTextArea.setRows( (int)Math.ceil( (double)temp.length() / 75.0 ) );
     onceDurationTextArea.getParent().getParent().setVisible( !temp.equals( "** No signal **" ) );
     
     temp = analysis.getRepeatDurationString();
     repeatDurationTextArea.setText( temp );
     repeatDurationTextArea.setRows( (int)Math.ceil( (double)temp.length() / 75.0 ) );
     repeatDurationTextArea.getParent().getParent().setVisible( !temp.equals( "** No signal **" ) );
 
     temp = analysis.getExtraDurationString();
     extraDurationTextArea.setText( temp );
     extraDurationTextArea.setRows( (int)Math.ceil( (double)temp.length() / 75.0 ) );
     extraDurationTextArea.getParent().getParent().setVisible( !temp.equals( "** No signal **" ) );
     
     pack();       
     advancedAreaUpdating = false;
   }
   
   private void setRemoteConfiguration( RemoteConfiguration config )
   {
     this.config = config;
     Remote remote = config.getRemote();
     shift.setText( remote.getShiftLabel() );
     xShift.setText( remote.getXShiftLabel() );
     xShift.setVisible( remote.getXShiftEnabled() );
     boundDevice.setModel( new DefaultComboBoxModel( remote.getDeviceButtons() ) );
     boundKey.setModel( new DefaultComboBoxModel( remote.getLearnButtons() ) );
   }
 
   private void setButton( int code, JComboBox comboBox, JCheckBox shiftBox, JCheckBox xShiftBox )
   {
     Remote remote = config.getRemote();
     Button b = remote.getButton( code );
     if ( b == null )
     {
       int base = code & 0x3F;
       if ( base != 0 )
       {
         b = remote.getButton( base );
         if ( ( base | remote.getShiftMask() ) == code )
         {
           shiftBox.setEnabled( b.allowsShiftedMacro() );
           shiftBox.setSelected( true );
           comboBox.setSelectedItem( b );
           return;
         }
         if ( remote.getXShiftEnabled() && ( base | remote.getXShiftMask() ) == code )
         {
           xShiftBox.setEnabled( remote.getXShiftEnabled() & b.allowsXShiftedMacro() );
           xShiftBox.setSelected( true );
           comboBox.setSelectedItem( b );
           return;
         }
       }
       b = remote.getButton( code & ~remote.getShiftMask() );
       if ( b != null )
       {
         shiftBox.setSelected( true );
       }
       else if ( remote.getXShiftEnabled() )
       {
         b = remote.getButton( code ^ ~remote.getXShiftMask() );
         if ( b != null )
         {
           xShiftBox.setSelected( true );
         }
       }
     }
 
     shiftBox.setEnabled( b.allowsShiftedKeyMove() );
     xShiftBox.setEnabled( b.allowsXShiftedKeyMove() );
 
     if ( b.getIsXShifted() )
     {
       xShiftBox.setSelected( true );
     }
     else if ( b.getIsShifted() )
     {
       shiftBox.setSelected( true );
     }
 
     comboBox.removeActionListener( this );
     comboBox.setSelectedItem( b );
     comboBox.addActionListener( this );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed( ActionEvent event )
   {
     Object source = event.getSource();
     Remote remote = config.getRemote();
     Button b = ( Button )boundKey.getSelectedItem();
     boolean ok = true;
     
     if ( source == applyButton || source == okButton )
     {
       int deviceIndex = ( ( DeviceButton )boundDevice.getSelectedItem() ).getButtonIndex();
       int keyCode = getKeyCode( boundKey, shift, xShift );
       learnedSignal.setDeviceButtonIndex( deviceIndex );
       learnedSignal.setKeyCode( keyCode );
 
       if ( signalTextHasChanged )
       {
         Hex data = ( new Hex( Hex.parseHex( signalTextArea.getText() ) ) ).subHex( 3 );
         learnedSignal.setData( data );
         learnedSignal.clearTimingAnalyzer();
 
         UnpackLearned ul = learnedSignal.getUnpackLearned();
         if ( config.hasSegments() )
         {
           // set default value
           learnedSignal.setSegmentFlags( 0xFF );
         }
         if ( ! ul.ok )
         {
           ok = false;
           String message = "Malformed learned signal: " + ul.error;
           String title = "Learned Signal Error";
           JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
         }
       }
     }
     
     if ( source == applyButton && ok )
     {
       setAdvancedAreaTextFields();
       model.set( learnedSignal );
       applyButton.setEnabled( false );
     }
     else if ( source == okButton && ok )
     {
       setVisible( false );
     }
     else if ( source == cancelButton )
     {
       learnedSignal = null;
       setVisible( false );
     }
     else if ( source == advancedButton )
     {
       advancedArea.setVisible( ! advancedArea.isVisible() );
       setAdvancedButtonText( advancedArea.isVisible() );
       pack(); 
     }
     else if ( source == shift )
     {
       if ( shift.isSelected() )
       {
         xShift.setSelected( false );
       }
       else if ( b != null && remote.getXShiftEnabled() )
       {       
         xShift.setSelected( b.needsShift( Button.LEARN_BIND ) );
       }
     }
     else if ( source == xShift )
     {
       if ( xShift.isSelected() )
       {
         shift.setSelected( false );
       }
       else if ( b != null )
       {
         shift.setSelected( b.needsShift( Button.LEARN_BIND ) );
       }
     }    
     else if ( source == boundKey )
     {
       if ( b != null )
       {
         b.setShiftBoxes( Button.LEARN_BIND, shift, xShift );
       }
     }
   }
   
   private void setAdvancedButtonText( boolean hide )
   {
     String text = "<html><center>";
     text += hide ? "Hide " : "Show ";
     text += "Advanced<br>Details</center></html>";
     advancedButton.setText( text );
   }
   
   private int getKeyCode( JComboBox comboBox, JCheckBox shiftBox, JCheckBox xShiftBox )
   {
     int keyCode = ( ( Button )comboBox.getSelectedItem() ).getKeyCode();
     if ( shiftBox.isSelected() )
     {
       keyCode |= config.getRemote().getShiftMask();
     }
     else if ( xShiftBox.isSelected() )
     {
       keyCode |= config.getRemote().getXShiftMask();
     }
     return keyCode;
   }
   
   private boolean signalTextHasChanged = false;
   private boolean signalTextLock = false;
   private void signalTextChanged()
   {
     if ( signalTextLock )
       return;
     signalTextHasChanged = true;
     applyButton.setEnabled( true );
   }
 
   private void documentChanged( DocumentEvent e )
   {
     applyButton.setEnabled( true );
   }
 
   private RemoteConfiguration config = null;
 
   /** The bound device. */
   private JComboBox boundDevice = new JComboBox();
 
   /** The bound key. */
   private JComboBox boundKey = new JComboBox();
 
   /** The shift. */
   private JCheckBox shift = new JCheckBox();
 
   /** The x shift. */
   private JCheckBox xShift = new JCheckBox();
 
   /** The ok button. */
   private JButton okButton = new JButton( "OK" );
 
   private JButton cancelButton = new JButton( "Cancel" );
   
   private JButton applyButton = new JButton( "Apply" );
   
   private JButton advancedButton = new JButton();
   
   private Box advancedArea = null;
   
   private boolean advancedAreaUpdating = false;
   private boolean analysisUpdating = false;
   // text box to enter rounding of times
   private JTextField burstRoundBox = new JTextField();
   // drop down to pick timing analyzer
   private JComboBox analyzerBox = new JComboBox();
   // drop down to pick timing analysis
   private JComboBox analysisBox = new JComboBox();
   // label to hold analysis result message
   private JLabel analysisMessageLabel = new JLabel();
 
   /** The burst text area. */
   private JTextArea burstTextArea = new JTextArea( 4, 70 );
 
   /** The duration text area. */
   private JTextArea onceDurationTextArea = new JTextArea( 8, 70 );
 
   /** The duration text area. */
   private JTextArea repeatDurationTextArea = new JTextArea( 8, 70 );
 
   /** The duration text area. */
   private JTextArea extraDurationTextArea = new JTextArea( 8, 70 );
 
   /** The duration text area. */
   //private JTextArea durationTextArea = new JTextArea( 8, 70 );
   
   private JTextArea signalTextArea = new JTextArea( 3, 70 );
 
   /** The learned signal. */
   private LearnedSignal learnedSignal = null;
 
   /** The model. */
   private JP1Table table = null;
   private DecodeTableModel model = new DecodeTableModel();
 
   /** The dialog. */
   private static LearnedSignalDialog dialog = null;
 
   @Override
   public void changedUpdate( DocumentEvent e )
   {
     documentChanged( e );
   }
 
   @Override
   public void insertUpdate( DocumentEvent e )
   {
     documentChanged( e );
   }
 
   @Override
   public void removeUpdate( DocumentEvent e )
   {
     documentChanged( e );
   }
 
 }
