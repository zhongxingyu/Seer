 package com.hifiremote.jp1;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 
 import javax.swing.JOptionPane;
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class DeviceUpgradePanel.
  */
 public class DeviceUpgradePanel extends RMTablePanel< DeviceUpgrade >
 {
 
   /**
    * Instantiates a new device upgrade panel.
    */
   public DeviceUpgradePanel()
   {
     super( new DeviceUpgradeTableModel() );
     footerPanel.add( upgradeBugPane, BorderLayout.PAGE_START );
     Font font = upgradeBugPane.getFont();
     Font font2 = font.deriveFont( Font.BOLD, 12 );
     upgradeBugPane.setFont( font2 );
     upgradeBugPane.setBackground( Color.RED );
     upgradeBugPane.setForeground( Color.YELLOW );
     String bugText = "NOTE:  This remote contains a bug that prevents device upgrades from working "
         + "if they use upgraded protocols.\nWorkaround:  Set up devices that use upgraded protocols "
         + "as \"Device Button Restricted\"";
     upgradeBugPane.setText( bugText );
     upgradeBugPane.setEditable( false );
     upgradeBugPane.setVisible( false );
   }
 
   /**
    * Sets the.
    * 
    * @param remoteConfig
    *          the remote config
    */
   @Override
   public void set( RemoteConfiguration remoteConfig )
   {
     ( ( DeviceUpgradeTableModel )model ).set( remoteConfig );
     this.remoteConfig = remoteConfig;
     table.initColumns( model );
     upgradeBugPane.setVisible( remoteConfig != null && remoteConfig.getRemote().hasUpgradeBug() );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
    */
   @Override
   public DeviceUpgrade createRowObject( DeviceUpgrade baseUpgrade )
   {
     // Never used, the methods that call it are overridden separately.
     return null;
   }
 
   private void createRowObjectA( DeviceUpgrade baseUpgrade )
   {
     System.err.println( "DeviceUpgradePanel.createRowObject()" );
     DeviceUpgrade upgrade = null;
     if ( baseUpgrade == null )
     {
       upgrade = new DeviceUpgrade();
       Remote remote = remoteConfig.getRemote();
       upgrade.setRemote( remote );
       if ( remote.getDeviceUpgradeAddress() != null )
       {
         upgrade.setButtonIndependent( false );
         upgrade.setButtonRestriction( DeviceButton.noButton );
         String msg = "<html>This remote has device upgrades that are available on<br>"
             + "all device buttons and ones that are only available on a<br>"
             + "specified button.  The same upgrade can even be in both<br>"
             + "categories.  This new upgrade will be created as being in<br>"
             + "neither category.  After pressing OK, edit the new table<br>"
             + "entry to set the availability as required.</html>";
         JOptionPane.showMessageDialog( RemoteMaster.getFrame(), msg, "Creating a new device upgrade",
             JOptionPane.PLAIN_MESSAGE );
       }
     }
     else
     {
       upgrade = new DeviceUpgrade( baseUpgrade );
     }
     oldUpgrade = baseUpgrade;
 
     RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, table );
     List< Remote > remotes = new ArrayList< Remote >( 1 );
     remotes.add( remoteConfig.getRemote() );
 
     editor = new DeviceUpgradeEditor( rm, upgrade, remotes, rowOut, this );
   }
 
   private DeviceUpgrade createRowObjectB( DeviceUpgradeEditor editor )
   {
     this.editor = null;
     DeviceUpgrade newUpgrade = editor.getDeviceUpgrade();
     if ( newUpgrade == null || oldUpgrade == null )
     {
       return newUpgrade;
     }
 
     int boundDeviceButtonIndex = remoteConfig.findBoundDeviceButtonIndex( oldUpgrade );
     rowBound = boundDeviceButtonIndex;
     if ( rowBound == -1 )
     {
       rowBound = null;
       return newUpgrade;
     }
 
     java.util.List< KeyMove > upgradeKeyMoves = newUpgrade.getKeyMoves();
     java.util.List< KeyMove > keyMoves = remoteConfig.getKeyMoves();
     for ( KeyMove upgradeKeyMove : upgradeKeyMoves )
     {
       for ( ListIterator< KeyMove > li = keyMoves.listIterator(); li.hasNext(); )
       {
         KeyMove keyMove = li.next();
         if ( keyMove.getDeviceButtonIndex() != boundDeviceButtonIndex )
         {
           continue;
         }
         if ( keyMove.getKeyCode() == upgradeKeyMove.getKeyCode() )
         {
           li.remove();
           Remote remote = remoteConfig.getRemote();
           System.err.println( "Removed keyMove assigned to " + remote.getDeviceButtons()[ boundDeviceButtonIndex ]
               + ':' + remote.getButtonName( keyMove.getKeyCode() )
               + " since there is one assigned in the device upgrade" );
           break;
         }
       }
     }
     return newUpgrade;
   }
 
   @Override
   protected void editRowObject( int row )
   {
     rowOut = row;
     createRowObjectA( getRowObject( row ) );
   }
 
   @Override
   protected void newRowObject( DeviceUpgrade baseUpgrade, int row, int modelRow, boolean select )
   {
     rowOut = null;
     rowNew = row == -1 ? null : row;
     rowModel = modelRow == -1 ? null : modelRow;
     this.select = select;
     createRowObjectA( baseUpgrade );
   }
 
   public void endEdit( DeviceUpgradeEditor editor, Integer row )
   {
     DeviceUpgrade newUpgrade = createRowObjectB( editor );
     if ( newUpgrade == null )
     {
       return;
     }
     if ( row != null )
     {
       // Edit
       model.setRow( sorter.modelIndex( row ), newUpgrade );
       Protocol pOld = oldUpgrade.getProtocol();
       Protocol pNew = newUpgrade.getProtocol();
      if ( oldUpgrade.needsProtocolCode() && ( pOld != pNew ) )
       {
         boolean pUsed = false;
         for ( DeviceUpgrade du : remoteConfig.getDeviceUpgrades() )
         {
           if ( du.getProtocol() == pOld )
           {
             pUsed = true;
             break;
           }
         }
         if ( !pUsed )
         {
           // Old protocol now unused so add to protocol updates
           remoteConfig.getProtocolUpgrades().add( pOld.getProtocolUpgrade( remoteConfig.getRemote() ) );
         }
       }
 
       RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, this );
       DeviceButtonTableModel deviceModel = rm.getGeneralPanel().getDeviceButtonTableModel();
 
       if ( rowBound != null )
       {
         deviceModel.setValueAt( newUpgrade.getDeviceType(), rowBound, 2 );
         deviceModel.setValueAt( new SetupCode( newUpgrade.getSetupCode() ), rowBound, 3 );
         deviceModel.fireTableRowsUpdated( rowBound, rowBound );
       }
     }
     else
     {
       // New, Clone
       if ( remoteConfig.findBoundDeviceButtonIndex( newUpgrade ) == -1 )
       {
         // upgrade isn't bound to a device button.
         Remote remote = remoteConfig.getRemote();
         DeviceButton[] devButtons = remote.getDeviceButtons();
         DeviceButton devButton = ( DeviceButton )JOptionPane.showInputDialog( RemoteMaster.getFrame(),
             "The device upgrade \"" + newUpgrade.toString()
                 + "\" is not assigned to a device button.\nDo you want to assign it now?\n"
                 + "To do so, select the desired device button and press OK.\n" + "Otherwise please press Cancel.\n",
             "Unassigned Device Upgrade", JOptionPane.QUESTION_MESSAGE, null, devButtons, null );
         if ( devButton != null )
         {
           short[] data = remoteConfig.getData();
           DeviceType devType = remote.getDeviceTypeByAliasName( newUpgrade.getDeviceTypeAliasName() );
           devButton.setSetupCode( ( short )newUpgrade.getSetupCode(), data );
           devButton.setDeviceTypeIndex( ( short )devType.getNumber(), data );
           devButton.setDeviceGroup( ( short )devType.getGroup(), data );
           if ( remote.getDeviceUpgradeAddress() != null )
           {
             String message = "Remember to set the button-dependent and/or button-independent\n"
                 + " settings in a manner consistent with your choice of button\n" + " assignment.";
             String title = "Creating a new device upgrade";
             JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, title, JOptionPane.INFORMATION_MESSAGE );
           }
         }
       }
       
       if ( rowNew == null )
       {
         model.addRow( newUpgrade );
         row = model.getRowCount();
       }
       else
       {
         model.insertRow( rowModel, newUpgrade );
       }
 
       if ( select )
       {
         table.setRowSelectionInterval( rowNew, rowNew );
       }
     }
     // If new upgrade uses an upgrade protocol not previously used, delete that protocol
     // from the list of unused protocols.
 
     ProtocolUpgrade puUsed = null;
     for ( ProtocolUpgrade pu : remoteConfig.getProtocolUpgrades() )
     {
       if ( pu.getProtocol() == newUpgrade.getProtocol() )
       {
         puUsed = pu;
         break;
       }
     }
     if ( puUsed != null )
     {
       remoteConfig.getProtocolUpgrades().remove( puUsed );
     }
 
   }
 
   public DeviceUpgradeEditor getDeviceUpgradeEditor()
   {
     return editor;
   }
 
   private Integer rowOut = null;
   private Integer rowBound = null;
   private Integer rowNew = null;
   private Integer rowModel = null;
   private Boolean select = null;
   private DeviceUpgrade oldUpgrade = null;
   private DeviceUpgradeEditor editor = null;
 
   /** The remote config. */
   private RemoteConfiguration remoteConfig;
 
   private JTextPane upgradeBugPane = new JTextPane();
 
 }
