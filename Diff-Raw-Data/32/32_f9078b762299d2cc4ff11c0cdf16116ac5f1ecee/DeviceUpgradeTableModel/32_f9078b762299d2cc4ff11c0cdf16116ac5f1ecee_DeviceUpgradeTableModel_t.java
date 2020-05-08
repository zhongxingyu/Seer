 package com.hifiremote.jp1;
 
 import java.awt.Component;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class DeviceUpgradeTableModel.
  */
 public class DeviceUpgradeTableModel extends JP1TableModel< DeviceUpgrade > implements PropertyChangeListener
 {
 
   /**
    * Instantiates a new device upgrade table model.
    */
   public DeviceUpgradeTableModel()
   {}
 
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
       for ( DeviceUpgrade upgrade : remoteConfig.getDeviceUpgrades() )
       {
         upgrade.removePropertyChangeListener( this );
       }
 
       for ( DeviceUpgrade upgrade : remoteConfig.getDeviceUpgrades() )
       {
         upgrade.addPropertyChangeListener( this );
       }
       setData( remoteConfig.getDeviceUpgrades() );
 
       Remote remote = remoteConfig.getRemote();
       if ( remote.getDeviceUpgradeAddress() != null )
       {
         DefaultComboBoxModel comboModel = new DefaultComboBoxModel( remote.getDeviceButtons() );
         comboModel.insertElementAt( DeviceButton.noButton, 0 );
         deviceButtonBox.setModel( comboModel );
       }
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.TableModel#getColumnCount()
    */
   public int getColumnCount()
   {
     int count = 7;
 
     if ( remoteConfig != null )
     {
       Remote remote = remoteConfig.getRemote();
       if ( remote.getDeviceUpgradeAddress() != null )
       {
         count += 2;
       }
     }
     return count;
   }
 
   public int getEffectiveColumn( int col )
   {
     if ( ( remoteConfig == null || remoteConfig != null && remoteConfig.getRemote().getDeviceUpgradeAddress() == null )
         && col >= 3 )
     {
       return col + 2;
     }
     return col;
   }
 
   /** The Constant colNames. */
   private static final String[] colNames =
   {
       "#", "<html>Device<br>Type</html>", "<html>Setup<br>Code</html>", "<html>Specific to<br>Device Button</html>",
       "<html>Available on<br>Other Buttons?</html>", "PID", "Variant", "Protocol", "Description"
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
 
   /** The Constant colPrototypeNames. */
   private static final String[] colPrototypeNames =
   {
       " 00 ", "CBL/SAT__", "Setup ", "Device Button", "Other Buttons?", "0000_", "Variant_____",
       "Panasonic Mixed Combo___", "A relatively long description and then some more___"
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
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
    */
   @Override
   public boolean isColumnWidthFixed( int col )
   {
     if ( getEffectiveColumn( col ) < 7 )
     {
       return true;
     }
     else
     {
       return false;
     }
   }
 
   /** The Constant colClasses. */
   private static final Class< ? >[] colClasses =
   {
       Integer.class, String.class, SetupCode.class, String.class, Boolean.class, Protocol.class, Protocol.class,
       Protocol.class, String.class
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
     col = getEffectiveColumn( col );
     if ( col == 3 || col == 4 || col == 8 )
     {
       return true;
     }
     else if ( col == 5 || col == 6 || col == 7 )
     {
       Protocol p = getRow( row ).getProtocol();
       return p instanceof ManualProtocol;
     }
     return false;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.TableModel#getValueAt(int, int)
    */
   public Object getValueAt( int row, int column )
   {
     DeviceUpgrade device = getRow( row );
     switch ( getEffectiveColumn( column ) )
     {
       case 0:
         return new Integer( row + 1 );
       case 1:
         return device.getDeviceTypeAliasName();
       case 2:
         return new SetupCode( device.getSetupCode() );
       case 3:
         return device.getButtonRestriction().getName();
       case 4:
         return device.getButtonIndependent();
       case 5:
       case 6:
       case 7:
         // The true values for columns 5 and 6 are created by the renderer
         return device.getProtocol();
       case 8:
         return device.getDescription();
     }
     return null;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
    */
   @Override
   public void setValueAt( Object value, int row, int col )
   {
     DeviceUpgrade device = getRow( row );
     switch ( getEffectiveColumn( col ) )
     {
       case 3:
         device.setButtonRestriction( ( ( DeviceButton )value ) );
         propertyChangeSupport.firePropertyChange( "device", null, null );
         break;
       case 4:
         device.setButtonIndependent( ( Boolean )value );
         propertyChangeSupport.firePropertyChange( "device", null, null );
         break;
       case 5:
       case 6:
       case 7:
         device.setProtocol( ( Protocol )value );
         propertyChangeSupport.firePropertyChange( "device", null, null );
         break;
       case 8:
         device.setDescription( ( String )value );
         break;
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
     col = getEffectiveColumn( col );
     if ( col == 0 )
     {
       return new RowNumberRenderer();
     }
     else if ( col == 5 )
     {
       return new DefaultTableCellRenderer()
       {
         @Override
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected,
             boolean hasFocus, int row, int col )
         {
           String starredID = ( ( Protocol )value ).getStarredID( remoteConfig.getRemote() );
           return super.getTableCellRendererComponent( table, starredID, isSelected, false, row, col );
         }
       };
     }
     else if ( col == 6 )
     {
       return new DefaultTableCellRenderer()
       {
         @Override
         public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected,
             boolean hasFocus, int row, int col )
         {
           Protocol protocol = ( Protocol )value;
           String variant = protocol.getVariantName();
           if ( protocol.getCustomCode( remoteConfig.getRemote().getProcessor() ) != null )
           {
             if ( variant.equals( "" ) )
             {
               variant = "Custom";
             }
             else
             {
               variant += "-Custom";
             }
           }
           return super.getTableCellRendererComponent( table, variant, isSelected, false, row, col );
         }
       };
     }
     return null;
   }
 
   @Override
   public TableCellEditor getColumnEditor( int col )
   {
     col = getEffectiveColumn( col );
     switch ( col )
     {
       case 3:
         DefaultCellEditor editor = new DefaultCellEditor( deviceButtonBox );
         editor.setClickCountToStart( RMConstants.ClickCountToStart );
         return editor;
       case 5:
       case 6:
       case 7:
         if ( remoteConfig != null )
         {
           return new ManualSettingsEditor( remoteConfig.getRemote(), col );
         }
         else
         {
           return null;
         }
       case 8:
         return descriptionEditor;
     }
     return null;
   }
 
   // private JCheckBox otherAvailabilityBox = new JCheckBox();
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.JP1TableModel#removeRow(int)
    */
   @Override
   public void removeRow( int row )
   {
     DeviceUpgrade du = getRow( row );
     Protocol p = du.getProtocol();
     boolean pUsed = false;
     for ( DeviceUpgrade temp : remoteConfig.getDeviceUpgrades() )
     {
       if ( temp != du && temp.getProtocol() == p  )
       {
         pUsed = true;
         break;
       }
     }
    if ( du.needsProtocolCode() && !pUsed )
     {
       String title = "Device Upgrade Deletion";
       String message = "The protocol used by the device upgrade being deleted is a protocol\n"
                      + "upgrade that is not used by any other device upgrade and so will \n"
                      + "normally also be deleted.  Do you wish to keep the protocol upgrade?";
       int ask = JOptionPane.showConfirmDialog( null, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
       if ( ask == JOptionPane.CANCEL_OPTION )
       {
         return;
       }
       else if ( ask == JOptionPane.YES_OPTION )
       {
         // Add to protocol upgrade list
         remoteConfig.getProtocolUpgrades().add( p.getProtocolUpgrade( remoteConfig.getRemote() ) );
       }
     }
     du.removePropertyChangeListener( this );
     super.removeRow( row );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.JP1TableModel#insertRow(int, java.lang.Object)
    */
   @Override
   public void insertRow( int row, DeviceUpgrade upgrade )
   {
     upgrade.addPropertyChangeListener( this );
     super.insertRow( row, upgrade );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.JP1TableModel#addRow(java.lang.Object)
    */
   @Override
   public void addRow( DeviceUpgrade upgrade )
   {
     upgrade.addPropertyChangeListener( this );
     super.addRow( upgrade );
   }
 
   // PropertyChangeListener
   /*
    * (non-Javadoc)
    * 
    * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
    */
   public void propertyChange( PropertyChangeEvent e )
   {
     Object source = e.getSource();
     List< DeviceUpgrade > upgrades = remoteConfig.getDeviceUpgrades();
     for ( int i = 0; i < upgrades.size(); ++i )
     {
       DeviceUpgrade upgrade = upgrades.get( i );
       if ( upgrade == source )
       {
         fireTableRowsUpdated( i, i );
         return;
       }
     }
   }
 
   /** The remote config. */
   private RemoteConfiguration remoteConfig = null;
 
   private SelectAllCellEditor descriptionEditor = new SelectAllCellEditor();
 
   private JComboBox deviceButtonBox = new JComboBox();
 }
