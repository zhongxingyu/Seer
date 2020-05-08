 package com.hifiremote.jp1;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JOptionPane;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class DeviceButtonTableModel.
  */
 public class DeviceButtonTableModel extends JP1TableModel< DeviceButton >
 {
 
   /**
    * Instantiates a new device button table model.
    */
 
   public DeviceButtonTableModel()
   {
     deviceTypeEditor = new DeviceTypeEditor( deviceTypeBox, softHT );
     deviceTypeEditor.setClickCountToStart( RMConstants.ClickCountToStart );
     sequenceEditor = new DefaultCellEditor( sequenceBox );
     sequenceEditor.setClickCountToStart( RMConstants.ClickCountToStart );
   }
 
   /**
    * Sets the.
    * 
    * @param remoteConfig
    *          the remote config
    */
   public void set( RemoteConfiguration remoteConfig )
   {
     this.remoteConfig = remoteConfig;
     if ( remoteConfig != null )
     {
       Remote remote = remoteConfig.getRemote();
       setData( remote.getDeviceButtons() );
       SoftDevices softDevices = remote.getSoftDevices();
       DefaultComboBoxModel comboModel = new DefaultComboBoxModel( remote.getAllDeviceTypes() );
       if ( remote.getSoftHomeTheaterType() >= 0 )
       {
         // Set the values passed to DeviceTypeEditor
         softHT.setUse( true );
         softHT.setDeviceType( remote.getSoftHomeTheaterType() );
         softHT.setDeviceCode( remote.getSoftHomeTheaterCode() );
       }
 
       if ( softDevices != null && softDevices.inUse() )
       {
         comboModel.addElement( new DeviceType( "", 0, 0xFFFF ) );
       }
       deviceTypeBox.setModel( comboModel );
 
       if ( softDevices != null && softDevices.usesSequence() )
       {
         adjustSequenceRange();
       }
       setupCodeRenderer = new SetupCodeRenderer( remoteConfig );
       setupCodeEditor = new SetupCodeEditor( setupCodeRenderer );
     }
   }
 
   private int getDeviceCount()
   {
     int len = 0;
     for ( int i = 0; i < getRowCount(); i++ )
     {
       if ( getExtendedTypeIndex( i ) != 0xFF )
       {
         len++ ;
       }
     }
     return len;
   }
 
   private void adjustSequenceRange()
   {
     int len = getDeviceCount();
     Integer[] values = new Integer[ len ];
     for ( int i = 0; i < len; i++ )
     {
       values[ i ] = i + 1;
     }
     sequenceBox.setModel( new DefaultComboBoxModel( values ) );
   }
 
   /**
    * Sets the editable.
    * 
    * @param flag
    *          the new editable
    */
   public void setEditable( boolean flag )
   {
     editable = flag;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.TableModel#getColumnCount()
    */
   public int getColumnCount()
   {
     int count = 5;
 
     if ( remoteConfig != null )
     {
       Remote remote = remoteConfig.getRemote();
       if ( remote.getDeviceLabels() != null )
       {
         ++count;
       }
       SoftDevices softDevices = remote.getSoftDevices();
       if ( softDevices != null && softDevices.usesSequence() )
       {
         ++count;
       }
     }
     return count;
   }
 
   /*
    * A remote can have a Sequence column (index 6) but no Label column (index 5), so map actual column number to an
    * effective column number
    */
   private int getEffectiveColumn( int col )
   {
     if ( remoteConfig != null && remoteConfig.getRemote().getDeviceLabels() == null && col == 5 )
     {
       return 6;
     }
     return col;
   }
 
   private int getExtendedTypeIndex( int row )
   {
     // This extends the range of values of the device type index beyond 0x0F to use a distinctive
     // value, 0xFF, to signify an empty device slot in a remote that uses soft devices.
     short[] data = remoteConfig.getData();
     Remote remote = remoteConfig.getRemote();
     DeviceButton db = getRow( row );
     if ( remote.getSoftDevices() == null || db.getDeviceSlot( data ) != 0xFFFF )
     {
       return db.getDeviceTypeIndex( data );
     }
     else
     {
       // if remote uses soft devices, a full setup code of 0xFFFF marks an empty
       // device slot, for which we use a special type index of 0xFF
       return 0xFF;
     }
   }
 
   /** The Constant colNames. */
   private static final String[] colNames =
   {
       "#", "Device Button", "Type", "<html>Setup<br>Code</html>", "Note", "Label", "Seq"
   };
 
   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.AbstractTableModel#getColumnName(int)
    */
   @Override
   public String getColumnName( int col )
   {
     return colNames[ getEffectiveColumn( col ) ];
   }
 
   /** The col prototype names. */
   private static String[] colPrototypeNames =
   {
       " 00 ", "Device Button", "__VCR/DVD__", "Setup", "A Meaningful, Reasonable Note", "Label", "Seq"
   };
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
    */
   @Override
   public String getColumnPrototypeName( int col )
   {
     return colPrototypeNames[ getEffectiveColumn( col ) ];
   }
 
   /** The Constant colClasses. */
   private static final Class< ? >[] colClasses =
   {
       Integer.class, String.class, DeviceType.class, SetupCode.class, String.class, String.class, Integer.class
   };
 
   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
    */
   @Override
   public Class< ? > getColumnClass( int col )
   {
     return colClasses[ getEffectiveColumn( col ) ];
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
    */
   @Override
   public boolean isCellEditable( int row, int col )
   {
     // If remote uses soft devices, device type must be set before other columns can be edited.
     // If remote uses soft home theater, the setup code is left blank and is not editable.
     return editable && col > 1 && ( col == 2 || getExtendedTypeIndex( row ) != 0xFF )
         && ( col != 3 || getValueAt( row, col ) != null );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.TableModel#getValueAt(int, int)
    */
   public Object getValueAt( int row, int column )
   {
     short[] data = remoteConfig.getData();
     Remote remote = remoteConfig.getRemote();
     DeviceButton db = getRow( row );
     int typeIndex = getExtendedTypeIndex( row );
     int group = db.getDeviceGroup( data );
     if ( typeIndex == 0xFF && column > 1 )
     {
       return null;
     }
     switch ( getEffectiveColumn( column ) )
     {
       case 0:
         return new Integer( row + 1 );
       case 1:
         return db.getName();
       case 2:
       {
 //        return remote.getDeviceTypeByIndex( typeIndex );
         return remote.getDeviceTypeByIndexAndGroup( typeIndex, group );
       }
       case 3:
       {
         // For remotes that use soft home theater, the HT setup code is specified in the RDF,
         // is not editable and so should be hidden.
         if ( softHT.inUse() && typeIndex == softHT.getDeviceType() )
         {
           return null;
         }
         return new SetupCode( db.getSetupCode( data ) );
       }
       case 4:
       {
         String[] notes = remoteConfig.getDeviceButtonNotes();
         String note = null;
         if ( notes != null )
         {
           note = notes[ row ];
         }
         if ( note == null )
         {
           DeviceUpgrade deviceUpgrade = remoteConfig.getAssignedDeviceUpgrade( db );
           if ( deviceUpgrade != null )
           {
             note = deviceUpgrade.getDescription();
           }
         }
         if ( note == null )
         {
           return "";
         }
         else
         {
           return note;
         }
       }
       case 5:
       {
         DeviceLabels labels = remote.getDeviceLabels();
         return labels.getText( data, row );
       }
       case 6:
       {
         SoftDevices softDevices = remote.getSoftDevices();
         int seq = softDevices.getSequencePosition( row, getRowCount(), data );
         if ( seq == -1 )
         {
           return null;
         }
         else
         {
           return seq + 1;
         }
       }
       default:
         return null;
     }
   }
   
   public boolean hasInvalidCodes()
   {
     Remote remote = remoteConfig.getRemote();
     if ( remote.getSetupValidation() == Remote.SetupValidation.OFF )
     {
       return false;
     }
     
     boolean result = false;    
     for ( int i = 0; i < remote.getDeviceButtons().length; i++ )
     {
       DeviceButton deviceButton = remote.getDeviceButtons()[ i ];
       DeviceType deviceType = ( DeviceType )getValueAt( i, 2 );
       SetupCode setupCode = ( SetupCode )getValueAt( i, 3 );
       if ( deviceType != null && setupCode != null )
       {
         setupCodeRenderer.setDeviceButton( deviceButton );
         setupCodeRenderer.setDeviceType( deviceType );
         result = result || !setupCodeRenderer.isValid( setupCode.getValue() );
       }
     }
     return result;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
    */
   @Override
   public void setValueAt( Object value, int row, int col )
   {
     short[] data = remoteConfig.getData();
     Remote remote = remoteConfig.getRemote();
     DeviceButton db = getRow( row );
     SoftDevices softDevices = remote.getSoftDevices();
     DeviceType oldDevType = null;
     SetupCode oldSetupCode = null;
     if ( col == 2 || col == 3 )
     {
       oldDevType = ( DeviceType )getValueAt( row, 2 );
       oldSetupCode = ( SetupCode )getValueAt( row, 3  );
     }
     
     if ( col == 2 )
     {
       DeviceType newDevType = ( DeviceType )value;
       if ( ! isValidDevice( row, newDevType, oldSetupCode ) )
       {
         return;
       }
 
       int newIndex = newDevType.getNumber();
       int newGroup = newDevType.getGroup();
       int oldIndex = getExtendedTypeIndex( row );
       int oldGroup = db.getDeviceGroup( data );
       
       DeviceLabels labels = remote.getDeviceLabels();
 
      if ( ( oldIndex == newIndex ) && ( oldGroup == -1 || oldGroup == newGroup ) )
       {
         return;
       }
       
       if ( isUpgradeWithKeymoves( oldDevType, oldSetupCode, true ) )
       {
         preserveKeyMoves( row, oldDevType, oldSetupCode );
       }
       
       if ( softHT.inUse() && newIndex == softHT.getDeviceType() )
       {
         db.zeroDeviceSlot( data );
         db.setDeviceTypeIndex( ( short )newIndex, data );
         db.setSetupCode( ( short )softHT.getDeviceCode(), data );
       }
       else
       {
         if ( oldIndex == 0xFF )
         {
           db.zeroDeviceSlot( data );
         }
         db.setDeviceTypeIndex( ( short )newIndex, data );
         db.setDeviceGroup( ( short )newGroup, data );
       }
 
       if ( labels != null )
       {
         String name = newIndex == 0xFF ? "" : remote.getDeviceTypeByIndex( newIndex ).getName();
         labels.setText( name, row, data );
         if ( labels.usesDefaultLabels() )
         {
           labels.setDefaultText( name, row, data );
         }
       }
 
       if ( softDevices != null && softDevices.usesFilledSlotCount() )
       {
         softDevices.setFilledSlotCount( getDeviceCount(), data );
       }
 
       if ( softDevices != null && softDevices.usesSequence() )
       {
         adjustSequenceRange();
         if ( oldIndex == 0xFF )
         {
           softDevices.setSequenceIndex( row, sequenceBox.getItemCount() - 1, data );
         }
         else if ( newIndex == 0xFF )
         {
           softDevices.deleteSequenceIndex( row, getRowCount(), data );
         }
       }
     }
     else if ( col == 3 )
     {
       SetupCode newSetupCode = null;
       if ( value.getClass() == String.class )
       {
         newSetupCode = new SetupCode( ( String )value );
       }
       else
       {
         newSetupCode = ( SetupCode )value;
       }
       
       if ( newSetupCode.getValue() == oldSetupCode.getValue() 
           || ! isValidDevice( row, oldDevType, newSetupCode ) )
       {
         return;
       }
       
       if ( isUpgradeWithKeymoves( oldDevType, oldSetupCode, true ) )
       {
         preserveKeyMoves( row, oldDevType, oldSetupCode );
       }
       
       db.setSetupCode( ( short )newSetupCode.getValue(), data );
     }
     else if ( col == 4 )
     {
       String strValue = ( ( String )value ).trim();
       if ( "".equals( strValue ) )
       {
         strValue = null;
       }
 
       remoteConfig.getDeviceButtonNotes()[ row ] = strValue;
     }
     else if ( getEffectiveColumn( col ) == 5 )
     {
       remote.getDeviceLabels().setText( ( String )value, row, data );
     }
     else if ( getEffectiveColumn( col ) == 6 )
     {
       int rows = getRowCount();
       int newSeq = ( ( Integer )value ).intValue() - 1;
       int oldSeq = softDevices.getSequencePosition( row, rows, data );
 
       if ( newSeq == oldSeq )
       {
         return;
       }
       softDevices.deleteSequenceIndex( row, rows, data );
       softDevices.insertSequenceIndex( row, newSeq, rows, data );
       fireTableDataChanged();
     }
     propertyChangeSupport.firePropertyChange( "value", null, null );
   }
   
   private boolean isValidDevice( int row, DeviceType devType, SetupCode setupCode )
   {
     Remote remote = remoteConfig.getRemote();
     DeviceButton db = getRow( row );
     Button button = remote.getButton( db.getName() );
 
     if ( isUpgradeWithKeymoves( devType, setupCode, false )
         && ( ( button != null && ! button.allowsKeyMove() )// case of real device button
             || ( row > 7 && remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.NORMAL ) ) ) // case of phantom device button
     {
       String message = "Device " + devType.getName() + " " + setupCode.getValue() + 
       " cannot be assigned to\nbutton " + db.getName() + " as it is an upgrade " +
       "that contains\nkeymoves";
       String title = "Device Button Assignment";
       JOptionPane.showMessageDialog( null, message, title, JOptionPane.ERROR_MESSAGE );
       return false;
     }
     return true;
   }
    
   private boolean isUpgradeWithKeymoves( DeviceType devType, SetupCode setupCode, boolean ask )
   {
     if ( devType != null && setupCode != null )
     {
       DeviceUpgrade du = remoteConfig.findDeviceUpgrade( devType.getNumber(), setupCode.getValue() );
       if ( du != null && du.getKeyMoves().size() > 0 )
       {
         if ( ask )
         {
           // If user does not wish to preserve keymoves, treat as not having any
           String message = "The current device " + devType.getName() + " " + setupCode.getValue() + 
           " contains keymoves.  Do you want to preserve them?";
           String title = "Device Change";
           return JOptionPane.showConfirmDialog( null, message, title, JOptionPane.YES_NO_OPTION, 
               JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION;
         }
         else
         {
           return true;
         }
       }
     }
     return false;
   }
   
   private void preserveKeyMoves( int devButtonIndex, DeviceType devType, SetupCode setupCode )
   {
     if ( devType == null || setupCode == null )
     {
       return;
     }
     DeviceUpgrade du = remoteConfig.findDeviceUpgrade( devType.getNumber(), setupCode.getValue() );
     if ( du == null )
     {
       return;
     }    
     for ( KeyMove keyMove : du.getKeyMoves() )
     {
       keyMove.setDeviceButtonIndex( devButtonIndex );
       remoteConfig.getKeyMoves().add( keyMove );
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
    */
   @Override
   public TableCellRenderer getColumnRenderer( int col )
   {
     if ( col == 0 )
     {
       return new RowNumberRenderer();
     }
     else if ( col == 3 )
     {
       return setupCodeRenderer;
     }
     return null;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.JP1TableModel#getColumnEditor(int)
    */
   @Override
   public TableCellEditor getColumnEditor( int col )
   {
     if ( !editable )
     {
       return null;
     }
 
     if ( col == 2 )
     {
       return deviceTypeEditor;
     }
     else if ( col == 3 )
     {
       return setupCodeEditor;
     }
     else if ( col == 4 || getEffectiveColumn( col ) == 5 )
     {
       return selectAllEditor;
     }
     else if ( getEffectiveColumn( col ) == 6 )
     {
       return sequenceEditor;
     }
     return null;
   }
 
   /** The remote config. */
   private RemoteConfiguration remoteConfig = null;
 
   /** The device type box. */
   private DefaultCellEditor deviceTypeEditor = null;
   private JComboBox deviceTypeBox = new JComboBox();
 
   /** The setup code editor */
   private SelectAllCellEditor selectAllEditor = new SelectAllCellEditor();
   
   private SetupCodeRenderer setupCodeRenderer = null;
   private SetupCodeEditor setupCodeEditor = null;
 
   private DefaultCellEditor sequenceEditor = null;
   private JComboBox sequenceBox = new JComboBox();
 
   /** The editable. */
   private boolean editable = true;
 
   private SoftHomeTheater softHT = new SoftHomeTheater();
 }
