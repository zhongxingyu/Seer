 package com.hifiremote.jp1;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.LineNumberReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.swing.JOptionPane;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class ProtocolManager.
  */
 public class ProtocolManager
 {
 
   /**
    * Instantiates a new protocol manager.
    */
   protected ProtocolManager()
   {}
 
   /**
    * Gets the protocol manager.
    * 
    * @return the protocol manager
    */
   public static ProtocolManager getProtocolManager()
   {
     return protocolManager;
   }
 
   /**
    * Load.
    * 
    * @param f
    *          the f
    * @throws Exception
    *           the exception
    */
   public void load( File f ) throws Exception
   {
     if ( loaded )
     {
       return;
     }
 
     while ( !f.canRead() )
     {
       JOptionPane.showMessageDialog( null, "Couldn't read " + f.getName() + "!", "Error", JOptionPane.ERROR_MESSAGE );
       RMFileChooser chooser = new RMFileChooser( f.getParentFile() );
       chooser.setFileSelectionMode( RMFileChooser.FILES_ONLY );
       chooser.setDialogTitle( "Pick the file containing the protocol definitions" );
       int returnVal = chooser.showOpenDialog( null );
       if ( returnVal != RMFileChooser.APPROVE_OPTION )
       {
         System.exit( -1 );
       }
       else
       {
         f = chooser.getSelectedFile();
       }
     }
     LineNumberReader rdr = new LineNumberReader( new FileReader( f ) );
     rdr.setLineNumber( 1 );
     Properties props = null;
     String name = null;
     Hex id = null;
     String type = null;
     extra = false;
 
     while ( true )
     {
       String line = rdr.readLine();
       if ( line == null )
       {
         break;
       }
 
       line = line.trim();
 
       if ( line.length() == 0 || line.charAt( 0 ) == '#' )
       {
         continue;
       }
 
       line = line.replaceAll( "\\\\n", "\n" );
       line = line.replaceAll( "\\\\t", "\t" );
       while ( line.endsWith( "\\" ) )
       {
         String temp = rdr.readLine().trim();
         temp = temp.replaceAll( "\\\\n", "\n" );
         temp = temp.replaceAll( "\\\\t", "\t" );
         line = line.substring( 0, line.length() - 1 ) + temp;
       }
 
       if ( line.charAt( 0 ) == '[' ) // begin new protocol
       {
         if ( name != null )
         {
           Protocol protocol = ProtocolFactory.createProtocol( name, id, type, props );
           if ( protocol != null )
           {
             add( protocol );
           }
         }
         name = line.substring( 1, line.length() - 1 ).trim();
         props = new Properties();
         id = null;
         type = "Protocol";
       }
       else
       {
         StringTokenizer st = new StringTokenizer( line, "=", true );
         String parmName = st.nextToken().trim();
         String parmValue = null;
         st.nextToken(); // skip the =
         if ( !st.hasMoreTokens() )
         {
           continue;
         }
         else
         {
           parmValue = st.nextToken( "" ); // .trim();
         }
 
         if ( parmName.equals( "PID" ) )
         {
           id = new Hex( parmValue );
         }
         else if ( parmName.equals( "Type" ) )
         {
           type = parmValue;
         }
         else
         {
           props.setProperty( parmName, parmValue );
         }
       }
     }
     rdr.close();
     add( ProtocolFactory.createProtocol( name, id, type, props ) );
     ManualProtocol manualProtocol = new ManualProtocol( new Hex( "FF FF" ), null );
     manualProtocol.setName( manualProtocol.getName() );
     add( manualProtocol );
     extra = true;
 
     if ( byName.size() < 2 )
     {
       JOptionPane.showMessageDialog( null, "No protocols were loaded!", "Error", JOptionPane.ERROR_MESSAGE );
       System.exit( -1 );
     }
 
     // Sort the names array
     String[] temp = new String[ 0 ];
     temp = names.toArray( temp );
     Arrays.sort( temp );
     names = new ArrayList< String >( temp.length );
     for ( int i = 0; i < temp.length; i++ )
     {
       names.add( temp[ i ] );
     }
 
     loaded = true;
   }
 
   /**
    * Adds the.
    * 
    * @param p
    *          the p
    */
   public void add( Protocol p )
   {
     /*
      * if ( p.getClass() == ManualProtocol.class ) { manualProtocol = ( ManualProtocol )p; return; }
      */
 
     // Add the protocol to the byName hashtable
     String name = p.getName();
     List< Protocol > v = byName.get( name );
     if ( v == null )
     {
       v = new ArrayList< Protocol >();
       byName.put( name, v );
       names.add( name );
     }
     v.add( p );
 
     // add the protocol to the byPID hashtable
     Hex id = p.getID();
     v = byPID.get( id );
     if ( v == null )
     {
       v = new ArrayList< Protocol >();
       byPID.put( id, v );
     }
     else
     {
       String pvName = p.getVariantName();
       for ( Protocol tryit : v )
       {
         String tryName = tryit.getVariantName();
         if ( pvName == null && tryName == null || pvName.equals( tryName ) )
         {
           System.err.println( "**** Warning: multiple protocols with PID " + id + " and variantName " + pvName );
           break;
         }
       }
     }
     v.add( p );
        
     if ( p instanceof ManualProtocol )
     {
       int nameIndex = ( ( ManualProtocol )p ).getNameIndex();
       Integer index = manualSettingsIndex.get( id );
       if ( nameIndex > 0 && ( index == null || nameIndex > index) )
       {
         manualSettingsIndex.put( id, nameIndex );
       }
     }   
 
     id = p.getAlternatePID();
     if ( id != null )
     {
       v = byAlternatePID.get( id );
       if ( v == null )
       {
         v = new ArrayList< Protocol >();
         byAlternatePID.put( id, v );
       }
       v.add( p );
     }
     
     if ( extra )
     {
       extras.add( p );
     }
   }
   
   public void remove( Protocol p )
   {
     String name = p.getName();
     List< Protocol > vn = byName.get( name );
     Hex id = p.getID();
     List< Protocol > vp = byPID.get( id );
     if ( vn == null || vp == null )
     {
       return;
     }
     
     if ( vn.size() == 1 )
     {
       names.remove( name );
       byName.remove( name );
     }
     else
     {
       vn.remove( p );
     }
   
     if ( vp.size() == 1 )
     {
       byPID.remove( id );
     }
     else
     {
       vp.remove( p );
     }
     
     if ( p instanceof ManualProtocol )
     {
       int nameIndex = ( ( ManualProtocol )p ).getNameIndex();
       Integer index = manualSettingsIndex.get( id );
       if ( index != null && nameIndex == index )
       {
         // Reset manualSettingsIndex to largest index remaining after p deleted
         vp = byPID.get( id ); // Value after removal of p
         if ( vp == null )
         {
           manualSettingsIndex.remove( id );
         }
         else
         {
           index = 0;
           for ( Protocol pp : vp )
           {
             if ( pp instanceof ManualProtocol )
             {
               nameIndex = ( ( ManualProtocol )pp ).getNameIndex();
               if ( nameIndex > index ) index = nameIndex;
             }
           }
           if ( index == 0 )
           {
             manualSettingsIndex.remove( id );
           }
           else
           {
             manualSettingsIndex.put( id, index );
           }
         }
       }
     }   
 
     id = p.getAlternatePID();
     if ( id != null )
     {
       vp = byAlternatePID.get( id );
       if ( vp != null && vp.size() == 1 )
       {
         byAlternatePID.remove( id );
       }
       else if ( vp != null )
       {
         vp.remove( p );
       }
     }
     
     Hashtable< String, List< Hex >> rp = p.getRemoteAltPIDs();
     for ( String sig : rp.keySet() )
     {
       for ( Hex hex : rp.get( sig ) )
       {
         if ( byAltPIDRemote.get( sig ) != null && byAltPIDRemote.get( sig ).get( hex ) != null )
         {
           byAltPIDRemote.get( sig ).get( hex ).remove( p );
           if ( byAltPIDRemote.get( sig ).get( hex ).size() == 0 )
           {
             byAltPIDRemote.get( sig ).remove( hex );
           }
           if ( byAltPIDRemote.get( sig ).size() == 0 )
           {
             byAltPIDRemote.remove( sig );
           }
         }
       }
     }
     
     if ( extras.contains( p ) )
     {
       extras.remove( p );
     }
   }
 
   /**
    * Gets the names.
    * 
    * @return the names
    */
   public List< String > getNames()
   {
     return names;
   }
 
   /**
    * Gets the protocols for remote.
    * 
    * @param remote
    *          the remote
    * @return the protocols for remote
    */
   public List< Protocol > getProtocolsForRemote( Remote remote )
   {
     return getProtocolsForRemote( remote, true );
   }
 
   /**
    * Gets the protocols for remote.
    * 
    * @param remote
    *          the remote
    * @param allowUpgrades
    *          the allow upgrades
    * @return the protocols for remote
    */
   public List< Protocol > getProtocolsForRemote( Remote remote, boolean allowUpgrades )
   {
     List< Protocol > rc = new ArrayList< Protocol >();
     for ( String name : names )
     {
       Protocol p = findProtocolForRemote( remote, name, allowUpgrades );
       if ( p != null )
       {
         rc.add( p );
       }
     }
     /*
      * if ( allowUpgrades && manualProtocol.hasCode( remote )) rc.add( manualProtocol );
      */
     return rc;
   }
 
   /**
    * Find by name.
    * 
    * @param name
    *          the name
    * @return the list< protocol>
    */
   public List< Protocol > findByName( String name )
   {
     List< Protocol > v = byName.get( name );
     /*
      * if (( v == null ) && name.equals( manualProtocol.getName())) { v = new ArrayList< Protocol >(); v.add(
      * manualProtocol ); }
      */
     return v;
   }
 
   /**
    * Find by pid.
    * 
    * @param id
    *          the id
    * @return the list< protocol>
    */
   public List< Protocol > findByPID( Hex id )
   {
     List< Protocol > rc = null;
     List< Protocol > list = byPID.get( id );
     if ( list == null )
     {
       rc = new ArrayList< Protocol >( 0 );
     }
     else
     {
       rc = new ArrayList< Protocol >( list.size() );
       rc.addAll( list );
     }
     return rc;
   }
 
   public List< Protocol > getBuiltinProtocolsForRemote( Remote remote, Hex pid )
   {
     List< Protocol > results = new ArrayList< Protocol >();
     for ( Protocol protocol : findByPID( pid ) )
     {
       if ( remote.supportsVariant( pid, protocol.getVariantName() ) )
       {
         results.add( protocol );
       }
     }
     return results;
   }
 
   /**
    * Find by alternate pid.
    * 
    * @param id
    *          the id
    * @return the list< protocol>
    */
   public List< Protocol > findByAlternatePID( Remote remote, Hex id )
   {
     return findByAlternatePID( remote, id, false );
   }
   
   
   public List< Protocol > findByAlternatePID( Remote remote, Hex id, boolean checkUserAltPIDs )
   {
     List< Protocol > list = new ArrayList< Protocol >();
     List< Protocol > l = byAlternatePID.get( id );
     if ( l != null ) list.addAll( l );
     if ( checkUserAltPIDs && byAltPIDRemote.get( remote.getSignature() ) != null )
     {
       l = byAltPIDRemote.get( remote.getSignature() ).get( id );
       if ( l != null ) list.addAll( l );
     }
     return list.size() == 0 ? null : list;
   }
   
   public void putAltPIDRemote( Hex id, Remote remote, Protocol p )
   {
     Hashtable< Hex, List<Protocol> > table = byAltPIDRemote.get( remote.getSignature() );
     if ( table == null )
     {
       table = new Hashtable< Hex, List<Protocol> >();
       byAltPIDRemote.put( remote.getSignature(), table );
     }
     List< Protocol > list = table.get( id );
     if ( list == null )
     {
       list = new ArrayList< Protocol >();
       table.put( id, list );
     }
     if ( !list.contains( p ) )
     {
       list.add( p );
     }
   }
   
   public int countAltPIDRemoteEntries()
   {
     int n = 0;
     for ( String sig : byAltPIDRemote.keySet() )
     {
       Hashtable< Hex, List<Protocol> > table = byAltPIDRemote.get( sig );
       for ( Hex h : table.keySet() )
       {
         n += table.get( h ).size();
       }
     }
     return n;
   }
   
   public void clearAltPIDRemoteEntries()
   {
     for ( Hex h : byPID.keySet() )
     {
       for ( Protocol p : byPID.get( h ) )
       {
         p.getRemoteAltPIDs().clear();
       }
     }
     byAltPIDRemote.clear();
   }
   
   public void setAltPIDRemoteProperties( PropertyFile properties )
   {
     for ( String key : properties.stringPropertyNames() )
     {
       if ( key.startsWith( "RemoteAltPID" ) )
       {
         properties.remove( key );
       }
     }
     int n = 1;
     for ( String sig : byAltPIDRemote.keySet() )
     {
       Set< Protocol > prots = new HashSet< Protocol >();
       for ( Hex id : byAltPIDRemote.get( sig ).keySet() )
       {
         for ( Protocol p : byAltPIDRemote.get( sig ).get( id ) )
         {
           prots.add( p );
         }
       }
 
       for ( Protocol p : prots )
       {
         String key = "RemoteAltPID." + n++;
         String val = sig + " [" + p.getName() + "] ";
         for ( Hex id : p.getRemoteAltPIDs().get( sig ) )
         {
           val += id.toString() + " ";
         }
         val = val.substring( 0, val.length() - 1 );
         properties.setProperty( key, val );
       }
     }
   }
   
   public void loadAltPIDRemoteProperties( PropertyFile properties )
   {
     int n = 1;
     String value = null;
     while ( ( value = properties.getProperty( "RemoteAltPID." + n++ )) != null )
     {
       int pos = value.indexOf( " [" );
       if ( pos < 0 ) continue;    // should not occur
       String sig = value.substring( 0, pos ).trim();
       value = value.substring( pos + 2 );
       pos = value.indexOf( "] " );
       if ( pos < 0 ) continue;    // should not occur
       String pName = value.substring( 0, pos ).trim();
       value = value.substring( pos + 2 );
       List< Protocol > pList = byName.get( pName );
       if ( pList == null ) continue;
       Hex hex = new Hex( value );
       List< Remote > remotes = RemoteManager.getRemoteManager().findRemoteBySignature( sig );
       if ( remotes.size() == 0 ) continue;
       // Assume all remotes with same signature have same processor
       Remote remote = remotes.get( 0 );
       Iterator< Protocol > it = pList.iterator();
       while ( it.hasNext() )
       {
         if ( !it.next().hasCode( remote ) ) it.remove();
       }
       for ( Protocol p : pList )
       {
         for ( int i = 0; i < hex.length()/2; i++ )
         {
           p.putAlternatePID( remote, hex.subHex( 2 * i, 2 ) );
         }
       }
     }
   }
 
   /**
    * Find protocol for remote.
    * 
    * @param remote
    *          the remote
    * @param name
    *          the name
    * @return the protocol
    */
   public Protocol findProtocolForRemote( Remote remote, String name )
   {
     return findProtocolForRemote( remote, name, true );
   }
 
   /**
    * Find protocol for remote.
    * 
    * @param remote
    *          the remote
    * @param name
    *          the name
    * @param allowUpgrades
    *          the allow upgrades
    * @return the protocol
    */
   public Protocol findProtocolForRemote( Remote remote, String name, boolean allowUpgrades )
   {
     Protocol protocol = null;
     Protocol tentative = null;
 
     List< Protocol > protocols = findByName( name );
     if ( protocols == null )
     {
       return null;
     }
     for ( Protocol p : protocols )
     {
       if ( remote.supportsVariant( p.getID(), p.getVariantName() ) )
       {
         protocol = p;
         break;
       }
 
       if ( tentative == null )
       {
         if ( allowUpgrades && p.hasCode( remote ) )
         {
           tentative = p;
         }
       }
     }
     if ( protocol == null )
     {
       protocol = tentative;
     }
 
     return protocol;
   }
 
   /**
    * Find protocol for remote.
    * 
    * @param remote
    *          the remote
    * @param id
    *          the id
    * @param fixedData
    *          the fixed data
    * @return the protocol
    */
   public Protocol findProtocolForRemote( Remote remote, Hex id, Hex fixedData )
   {
     List< Protocol > protocols = protocolManager.findByPID( id );
     for ( Protocol p : protocols )
     {
       if ( !remote.supportsVariant( id, p.getVariantName() ) )
       {
         continue;
       }
       Value[] vals = p.importFixedData( fixedData );
       Hex calculatedFixedData = p.getFixedData( vals );
       if ( calculatedFixedData.equals( fixedData ) )
       {
         return p;
       }
     }
     return null;
   }
 
   /**
    * Find protocol for remote.
    * 
    * @param remote
    *          the remote
    * @param id
    *          the id
    * @return the protocol
    */
   public Protocol findProtocolForRemote( Remote remote, Hex id )
   {
     return findProtocolForRemote( remote, id, true );
   }
 
   /**
    * Find protocol for remote.
    * 
    * @param remote
    *          the remote
    * @param id
    *          the id
    * @param allowUpgrades
    *          the allow upgrades
    * @return the protocol
    */
   public Protocol findProtocolForRemote( Remote remote, Hex id, boolean allowUpgrades )
   {
     Protocol protocol = null;
     Protocol tentative = null;
     List< Protocol > protocols = findByPID( id );
     if ( protocols == null )
     {
       protocols = findByAlternatePID( remote, id );
     }
 
     if ( protocols == null )
     {
       return null;
     }
 
     for ( Protocol p : protocols )
     {
       if ( remote.supportsVariant( id, p.getVariantName() ) )
       {
         protocol = p;
         break;
       }
       if ( tentative == null )
       {
         if ( allowUpgrades && p.hasCode( remote ) )
         {
           tentative = p;
         }
       }
     }
     if ( protocol == null )
     {
       protocol = tentative;
     }
     return protocol;
   }
 
   /**
    * Find protocol by old name.
    * 
    * @param remote
    *          the remote
    * @param name
    *          the name
    * @param pid
    *          the pid
    * @return the protocol
    */
   public Protocol findProtocolByOldName( Remote remote, String name, Hex pid )
   {
     Protocol matchByName = null;
     List< Protocol > protocols = getProtocolsForRemote( remote );
     if ( protocols == null )
     {
       return null;
     }
     for ( Protocol p : protocols )
     {
       for ( String oldName : p.getOldNames() )
       {
         if ( name.equals( oldName ) )
         {
           if ( matchByName == null )
           {
             matchByName = p;
           }
           if ( p.getID().equals( pid ) )
           {
             return p;
           }
         }
       }
     }
 
     return matchByName;
   }
 
   /**
    * Find protocol.
    * 
    * @param name
    *          the name
    * @param id
    *          the id
    * @param variantName
    *          the variant name
    * @return the protocol
    */
   public Protocol findProtocol( String name, Hex id, String variantName )
   {
     List< Protocol > protocols = findByPID( id );
     if ( protocols == null )
     {
       return null;
     }
     for ( Protocol p : protocols )
     {
       if ( p.getName().equals( name ) && p.getVariantName().equals( variantName ) )
       {
         return p;
       }
     }
     return null;
   }
 
   /**
    * Find nearest protocol.
    * 
    * @param name
    *          the name
    * @param id
    *          the id
    * @param variantName
    *          the variant name
    * @return the protocol
    */
   public Protocol findNearestProtocol( Remote remote, String name, Hex id, String variantName )
   {
     System.err
         .println( "ProtocolManager.findNearestProtocol( " + remote + ", " + name + ", " + id + ", " + variantName );
     Protocol near = null;
     List< Protocol > protocols = findByPID( id );
     if ( protocols == null )
     {
       protocols = findByAlternatePID( remote, id );
     }
     if ( protocols == null )
     {
       System.err.println( "No protocol found" );
       return null;
     }
     for ( Protocol p : protocols )
     {    
       if ( ( variantName == null || variantName.equals( p.getVariantName() ) )
           && remote.supportsVariant( id, p.getVariantName() ) )
       {
         if ( p.getName().equals( name ) )
         {
           System.err.println( "Found built-in protocol " + p );
           return p;
         }
         else if ( name.equals( "pid: " + id.toString() ) )
         {
           System.err.println( "Recreating derived protocol from " + p );
           Properties props = new Properties();
           for ( Processor pr : ProcessorManager.getProcessors() )
           {
             Hex hCode = p.getCode( pr );
             if ( hCode != null )
             {
               props.put( "Code." + pr.getEquivalentName(), hCode.toString() );
             }
           }
           String variant = p.getVariantName();
           if ( variant != null && variant.length() > 0 )
           {
             props.put( "VariantName", variant );
           }
           p = ProtocolFactory.createProtocol( "pid: " + id.toString(), id, "Protocol", props );
           ProtocolManager.getProtocolManager().add( p );
           System.err.println( "Using recreated protocol " + name );
           return p;
         }
       }
      if ( p.getName().equals( name ) && p.hasCode( remote ) && near == null )
       {
         near = p;
      }

      
      
     }
     if ( near != null )
     {
       System.err.println( "Found protocol " + near );
       return near;
     }
     protocols = findByName( name );
     if ( protocols != null )
     {
       near = protocols.get( 0 );
     }
     if ( near != null )
     {
       System.err.println( "Found protocol " + near );
       return near;
     }
     near = findProtocolByOldName( remote, name, id );
     if ( near != null )
     {
       System.err.println( "Found protocol " + near );
       return near;
     }    
     System.err.println( "No protocol found" );
     return null;
   }
   
   public static int getManualSettingsIndex( Hex pid )
   {
     Integer index = manualSettingsIndex.get( pid );
     return ( index == null ) ? 0 : index;
   }
   
   /**
    * A selective reset that only removes protocols whose pid is in the given list.
    */
   public void reset( List< Integer > pids )
   {
     // Remove extra protocols.  Clone first as extras is modified by remove().
     List< Protocol > extrasClone = new ArrayList< Protocol >( extras );
     for ( Protocol p : extrasClone )
     {
       if ( pids.contains( p.getID().get( 0 ) ) )
       {
         remove( p );
       }
     }
     // Remove custom code
     for ( List< Protocol > l : byName.values() )
     {
       for ( Protocol p : l )
       {
         if ( pids.contains( p.getID().get( 0 ) ) )
         {
           p.customCode.clear();
         }
       }
     }
   }
   
   public void reset()
   {
     // Remove extra protocols.  Clone first as extras is modified by remove().
     List< Protocol > extrasClone = new ArrayList< Protocol >( extras );
     for ( Protocol p : extrasClone )
     {
       remove( p );
     }
     // Remove all custom code
     for ( List< Protocol > l : byName.values() )
     {
       for ( Protocol p : l )
       {
         p.customCode.clear();
       }
     }
     // Reset all manual settings indexes
     manualSettingsIndex.clear();
   }
   
   /*
    * public ManualProtocol getManualProtocol() { System.err.println( "ProtocolManager.getManualProtocol(): " +
    * manualProtocol ); return manualProtocol; }
    */
   /** The protocol manager. */
   private static ProtocolManager protocolManager = new ProtocolManager();
   // private static ManualProtocol manualProtocol = null;
   /** The loaded. */
   private boolean loaded = false;
 
   /** The names. */
   private List< String > names = new ArrayList< String >();
 
   /** The by name. */
   private Hashtable< String, List< Protocol >> byName = new Hashtable< String, List< Protocol >>();
 
   /** The by pid. */
   private Hashtable< Hex, List< Protocol >> byPID = new Hashtable< Hex, List< Protocol >>();
 
   /** The by alternate pid. */
   private Hashtable< Hex, List< Protocol >> byAlternatePID = new Hashtable< Hex, List< Protocol >>();
   
   /** By remote-specific alt PID, remote keyed by signature */
   private Hashtable< String, Hashtable< Hex, List< Protocol > > > byAltPIDRemote = new Hashtable< String, Hashtable< Hex, List<Protocol > > >();
 
   private boolean extra = true;
   
   private List< Protocol > extras = new ArrayList< Protocol >();
   
   /** An index for each manual protocol PID that is maintained by add(Protocol) and
    *  that can be used to create a unique default name even with multiple such protocols
    *  with the same PID.
    */
   private static Hashtable< Hex, Integer > manualSettingsIndex = new Hashtable< Hex, Integer >();
 }
