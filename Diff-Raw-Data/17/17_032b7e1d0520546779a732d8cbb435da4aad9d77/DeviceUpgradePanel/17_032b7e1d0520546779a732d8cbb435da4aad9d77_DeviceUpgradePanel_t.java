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
 
     RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, table );
     List< Remote > remotes = new ArrayList< Remote >( 1 );
     remotes.add( remoteConfig.getRemote() );
     DeviceUpgradeEditor editor = new DeviceUpgradeEditor( rm, upgrade, remotes );
     upgrade = editor.getDeviceUpgrade();
     if ( upgrade == null )
     {
       return null;
     }
 
     int boundDeviceButtonIndex = remoteConfig.findBoundDeviceButtonIndex( upgrade );
     if ( boundDeviceButtonIndex == -1 )
     {
       return upgrade;
     }
 
     java.util.List< KeyMove > upgradeKeyMoves = upgrade.getKeyMoves();
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
     return upgrade;
   }
 
   /** The remote config. */
   private RemoteConfiguration remoteConfig;
 
   private JTextPane upgradeBugPane = new JTextPane();
 }
