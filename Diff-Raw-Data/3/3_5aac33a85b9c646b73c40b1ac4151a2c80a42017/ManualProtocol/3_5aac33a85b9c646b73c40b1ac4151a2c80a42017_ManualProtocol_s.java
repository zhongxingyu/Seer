 package com.hifiremote.jp1;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import com.hifiremote.jp1.translate.Translate;
 import com.hifiremote.jp1.translate.Translator;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class ManualProtocol.
  */
 public class ManualProtocol extends Protocol
 {
 
   /** The Constant ONE_BYTE. */
   public final static int ONE_BYTE = 0;
 
   /** The Constant BEFORE_CMD. */
   public final static int BEFORE_CMD = 1;
 
   /** The Constant AFTER_CMD. */
   public final static int AFTER_CMD = 2;
 
   /**
    * Instantiates a new manual protocol.
    * 
    * @param id
    *          the id
    * @param props
    *          the props
    */
   public ManualProtocol( Hex id, Properties props )
   {
     super( null, id, props );
     if ( props != null )
     {
       notes = props.getProperty( "Protocol.notes" );
     }
   }
   
   public ManualProtocol( Properties props )
   {
     this( null, props );
     name = props.getProperty( "Name" );
     id = new Hex( props.getProperty( "PID" ) );
   }
 
   /**
    * Instantiates a new manual protocol.
    * 
    * @param p
    *          the p
    */
   public ManualProtocol( ManualProtocol p )
   {
     super( p.getName(), p.id, null );
     if ( p.defaultFixedData != null )
     {
       defaultFixedData = p.defaultFixedData;
     }
   }
 
   /**
    * Instantiates a new manual protocol.
    * 
    * @param name
    *          the name
    * @param id
    *          the id
    * @param cmdType
    *          the cmd type
    * @param signalStyle
    *          the signal style
    * @param devBits
    *          the dev bits
    * @param parms
    *          the parms
    * @param rawHex
    *          the raw hex
    * @param cmdBits
    *          the cmd bits
    */
   public ManualProtocol( String name, Hex id, int cmdType, String signalStyle, int devBits, List< Value > parms,
       short[] rawHex, int cmdBits )
   {
     super( name, id, new Properties() );
     System.err.println( "ManualProtocol constructor:" );
     System.err.println( "  name=" + name );
     System.err.println( "  id=" + id );
     System.err.println( "  cmdType=" + cmdType );
     System.err.println( "  signalStyle=" + signalStyle );
     System.err.println( "  devBits=" + devBits );
     System.err.println( "  parms.size()=" + parms.size() );
     System.err.println( "  rawHex=" + Hex.toString( rawHex ) );
     System.err.println( "  cmdBits=" + cmdBits );
 
     boolean lsb = false;
     boolean comp = false;
     if ( signalStyle.startsWith( "LSB" ) )
     {
       lsb = true;
     }
     if ( signalStyle.endsWith( "COMP" ) )
     {
       comp = true;
     }
 
     createDefaultParmsAndTranslators( cmdType, lsb, comp, devBits, parms, rawHex, cmdBits );
   }
 
   /**
    * Creates the default parms and translators.
    * 
    * @param cmdType
    *          the cmd type
    * @param lsb
    *          the lsb
    * @param comp
    *          the comp
    * @param devBits
    *          the dev bits
    * @param parms
    *          the parms
    * @param rawHex
    *          the raw hex
    * @param cmdBits
    *          the cmd bits
    */
   public void createDefaultParmsAndTranslators( int cmdType, boolean lsb, boolean comp, int devBits,
       List< Value > parms, short[] rawHex, int cmdBits )
   {
     DirectDefaultValue defaultValue = new DirectDefaultValue( new Integer( 0 ) );
 
     devParms = new DeviceParameter[ parms.size() ];
     deviceTranslators = new Translator[ parms.size() ];
 
     int offset = parms.size();
     short[] fixedBytes = new short[ offset + rawHex.length ];
 
     for ( int i = 0; i < parms.size(); i++ )
     {
       devParms[ i ] = new NumberDeviceParm( "Device " + ( i + 1 ), defaultValue, 10, devBits );
       System.err.println( "Setting devParms[ " + i + " ]=" + parms.get( i ) );
       devParms[ i ].setValue( parms.get( i ) );
       fixedBytes[ i ] = ( ( Integer )parms.get( i ).getUserValue() ).shortValue();
       deviceTranslators[ i ] = new Translator( lsb, comp, i, devBits, i * 8 );
     }
 
     for ( int i = 0; i < rawHex.length; i++ )
     {
       fixedBytes[ i + offset ] = rawHex[ i ];
     }
 
     defaultFixedData = new Hex( fixedBytes );
 
     short[] mask = new short[ defaultFixedData.length() ];
     for ( int i = 0; i < mask.length; ++i )
     {
       mask[ i ] = 0xFF;
     }
     fixedDataMask = new Hex( mask );
 
     int cmdLength = cmdType >> 4;
     switch ( cmdType )
     {
       case ONE_BYTE:
         cmdIndex = 0;
         cmdLength = 1;
         break;
       case BEFORE_CMD:
         cmdIndex = 1;
         cmdLength = 2;
         break;
       case AFTER_CMD:
         cmdIndex = 0;
         cmdLength = 2;
         break;
       default:
         cmdIndex = 0;
     }
 
     defaultCmd = new Hex( new short[ cmdLength ] );
     cmdParms = new CmdParameter[ cmdLength ];
     cmdTranslators = new Translator[ cmdLength ];
     importCmdTranslators = new Translator[ cmdLength - 1 ];
 
     for ( int i = 0; i < cmdLength; ++i )
     {
       if ( i == cmdIndex )
       {
         System.err.println( "Creating OBC parm & translator for index " + i + " at bit " + i * 8 );
         cmdParms[ i ] = new NumberCmdParm( "OBC", null, cmdBits );
         cmdTranslators[ i ] = new Translator( lsb, comp, cmdIndex, cmdBits, cmdIndex * 8 );
       }
       else
       {
         System.err.println( "Creating Byte " + ( i + 1 ) + " parm & translators for index " + i + " at bit " + i * 8 );
         cmdParms[ i ] = new NumberCmdParm( "Byte " + ( i + 1 ), defaultValue, cmdBits );
         cmdTranslators[ i ] = new Translator( false, false, i, 8, i * 8 );
        importCmdTranslators[ i == 0 ? 0 : i - 1 ] = new Translator( false, false, i - 1, 8, i * 8 );
       }
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.Protocol#getName()
    */
   @Override
   public String getName()
   {
     if ( name != null )
     {
       return name;
     }
     else if ( id != null )
     {
       return "PID " + id.toString();
     }
     else
     {
       return "Manual Settings";
     }
   }
 
   /**
    * Sets the device parms.
    * 
    * @param v
    *          the new device parms
    */
   public void setDeviceParms( List< DeviceParameter > v )
   {
     devParms = new DeviceParameter[ v.size() ];
     v.toArray( devParms );
   }
 
   /**
    * Sets the device translators.
    * 
    * @param v
    *          the new device translators
    */
   public void setDeviceTranslators( List< Translate > v )
   {
     deviceTranslators = new Translator[ v.size() ];
     v.toArray( deviceTranslators );
   }
 
   /**
    * Sets the command parms.
    * 
    * @param v
    *          the new command parms
    */
   public void setCommandParms( List< CmdParameter > v )
   {
     cmdParms = new CmdParameter[ v.size() ];
     v.toArray( cmdParms );
   }
 
   /**
    * Sets the command translators.
    * 
    * @param v
    *          the new command translators
    */
   public void setCommandTranslators( List< Translate > v )
   {
     cmdTranslators = new Translator[ v.size() ];
     v.toArray( cmdTranslators );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.Protocol#importCommand(com.hifiremote.jp1.Hex, java.lang.String, boolean, int, boolean)
    */
   @Override
   public void importCommand( Hex hex, String text, boolean useOBC, int obcIndex, boolean useEFC )
   {
     if ( text.indexOf( ' ' ) != -1 || text.indexOf( 'h' ) != -1 )
     {
       Hex newHex = new Hex( text );
       if ( newHex.length() > hex.length() )
       {
         setDefaultCmd( newHex );
       }
       hex = newHex;
     }
     else
     {
       super.importCommand( hex, text, useOBC, obcIndex, useEFC );
     }
   }
 
   // for importing byte2 values from a KM upgrade.
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.Protocol#importCommandParms(com.hifiremote.jp1.Hex, java.lang.String)
    */
   @Override
   public void importCommandParms( Hex hex, String text )
   {
     if ( cmdParms.length == 1 )
     {
       return;
     }
     StringTokenizer st = new StringTokenizer( text );
     Value[] values = new Value[ st.countTokens() ];
     int index = 0;
     while ( st.hasMoreTokens() )
     {
       values[ index++ ] = new Value( Integer.valueOf( st.nextToken(), 16 ) );
     }
 
     for ( index = 0; index < values.length; index++ )
     {
       for ( int i = 0; i < importCmdTranslators.length; i++ )
       {
         importCmdTranslators[ i ].in( values, hex, devParms, index );
       }
     }
   }
 
   /**
    * Store.
    * 
    * @param out
    *          the out
    */
   public void store( PropertyWriter out )
   {
     if ( devParms.length > 0 )
     {
       StringBuilder buff = new StringBuilder();
       for ( int i = 0; i < devParms.length; i++ )
       {
         if ( i > 0 )
         {
           buff.append( ',' );
         }
         DeviceParameter devParm = devParms[ i ];
         buff.append( devParm.toString() );
       }
       out.print( "DevParms", buff.toString() );
     }
     if ( deviceTranslators != null && deviceTranslators.length > 0 )
     {
       StringBuilder buff = new StringBuilder();
       for ( int i = 0; i < deviceTranslators.length; i++ )
       {
         if ( i > 0 )
         {
           buff.append( ' ' );
         }
         buff.append( deviceTranslators[ i ].toString() );
       }
       out.print( "DeviceTranslator", buff.toString() );
     }
     if ( cmdParms.length > 0 )
     {
       StringBuilder buff = new StringBuilder();
       for ( int i = 0; i < cmdParms.length; i++ )
       {
         if ( i > 0 )
         {
           buff.append( ',' );
         }
         buff.append( cmdParms[ i ] );
       }
       out.print( "CmdParms", buff.toString() );
     }
     if ( cmdTranslators.length > 0 )
     {
       StringBuilder buff = new StringBuilder();
       for ( int i = 0; i < cmdTranslators.length; i++ )
       {
         if ( i > 0 )
         {
           buff.append( ' ' );
         }
         buff.append( cmdTranslators[ i ] );
       }
       out.print( "CmdTranslator", buff.toString() );
     }
     out.print( "DefaultCmd", defaultCmd.toString() );
     out.print( "CmdIndex", Integer.toString( cmdIndex ) );
     out.print( "FixedData", defaultFixedData.toString() );
     for ( Iterator< String > i = code.keySet().iterator(); i.hasNext(); )
     {
       Object key = i.next();
       out.print( "Code." + key, code.get( key ).toRawString() );
     }
     if ( notes != null )
     {
       out.print( "Protocol.notes", notes );
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.Protocol#store(com.hifiremote.jp1.PropertyWriter, com.hifiremote.jp1.Value[])
    */
   @Override
   public void store( PropertyWriter out, Value[] vals, Remote remote ) throws IOException
   {
     System.err.println( "ManualProtocol.store" );
     super.store( out, vals, remote );
     store( out );
   }
 
   /**
    * Sets the default cmd.
    * 
    * @param cmd
    *          the new default cmd
    */
   public void setDefaultCmd( Hex cmd )
   {
     defaultCmd = cmd;
   }
 
   /**
    * Sets the raw hex.
    * 
    * @param rawHex
    *          the new raw hex
    */
   public void setRawHex( Hex rawHex )
   {
     defaultFixedData = rawHex;
   }
 
   /**
    * Sets the code.
    * 
    * @param pCode
    *          the code
    * @param p
    *          the p
    */
   public void setCode( Hex pCode, Processor p )
   {
     code.put( p.getEquivalentName(), pCode );
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.Protocol#needsCode(com.hifiremote.jp1.Remote)
    */
   @Override
   public boolean needsCode( Remote r )
   {
     return true;
   }
 
   /**
    * Sets the name.
    * 
    * @param name
    *          the new name
    */
   public void setName( String name )
   {
     this.name = name;
   }
 
   /**
    * Sets the iD.
    * 
    * @param newID
    *          the new iD
    */
   public void setID( Hex newID )
   {
     id = newID;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see com.hifiremote.jp1.Protocol#importUpgradeCode(java.lang.String)
    */
   @Override
   public Hex importUpgradeCode( String notes )
   {
     Hex importedCode = super.importUpgradeCode( notes );
     if ( importedCode == null )
     {
       return null;
     }
 
     int importedCmdLength = getCmdLengthFromCode();
     System.err.println( "importedCmdLength=" + importedCmdLength + ", but defaultCmd.length()=" + defaultCmd.length() );
     // There's more bytes than we thought, so need to add another cmd parameter, translator, and importer
     if ( importedCmdLength != defaultCmd.length() )
     {
       /*
        * short[] newCmd = new short[ importedCmdLength ]; defaultCmd = new Hex( newCmd ); int newParmIndex =
        * importedCmdLength - 1;
        * 
        * CmdParameter[] newParms = new CmdParameter[ cmdParms.length + 1 ]; Translate[] newTranslators = new Translate[
        * cmdTranslators.length + 1 ]; Translate[] newImporters = new Translate[ importCmdTranslators.length + 1 ];
        * 
        * System.arraycopy( cmdParms, 0, newParms, 0, cmdParms.length ); System.arraycopy( cmdTranslators, 0,
        * newTranslators, 0, cmdTranslators.length ); System.arraycopy( importCmdTranslators, 0, newImporters, 0,
        * importCmdTranslators.length );
        * 
        * cmdParms = newParms; cmdTranslators = newTranslators; importCmdTranslators = newImporters;
        * 
        * int newIndex = 2; if ( cmdIndex == 1 ) { (( Translator )cmdTranslators[ 0 ] ).setBitOffset( 16 ); newIndex = 1;
        * }
        * 
        * cmdParms[ newParmIndex ] = new NumberCmdParm( "Byte 3", new DirectDefaultValue( new Integer( 0 )), 8, 16 );
        * cmdTranslators[ newParmIndex ] = new Translator( false, false, 2, 8, newIndex 8 ); importCmdTranslators[
        * newParmIndex - 1 ] = new Translator( false, false, 1, 8, newIndex 8 );
        */
       boolean lsb = ( ( Translator )cmdTranslators[ cmdIndex ] ).getLSB();
       boolean comp = ( ( Translator )cmdTranslators[ cmdIndex ] ).getComp();
 
       defaultCmd = new Hex( new short[ importedCmdLength ] );
       if ( cmdIndex != 0 )
       {
         cmdIndex = importedCmdLength - 1;
       }
 
       cmdParms = new CmdParameter[ importedCmdLength ];
       cmdTranslators = new Translate[ importedCmdLength ];
       importCmdTranslators = new Translate[ importedCmdLength - 1 ];
 
       DirectDefaultValue zero = new DirectDefaultValue( Integer.valueOf( 0 ) );
       for ( int i = 0; i < importedCmdLength; ++i )
       {
         if ( i == cmdIndex )
         {
           cmdParms[ i ] = new NumberCmdParm( "OBC", zero, 8, 10 );
           cmdTranslators[ i ] = new Translator( lsb, comp, i, 8, i * 8 );
         }
         else
         {
           cmdParms[ i ] = new NumberCmdParm( "Byte " + ( i + 1 ), zero, 8, 16 );
           cmdTranslators[ i ] = new Translator( false, false, i, 8, i * 8 );
         }
         if ( cmdIndex == 0 )
         {
           if ( i > 0 )
           {
             System.err.printf( "Creating importCmdTranslator for index %d to bit %d\n", Integer.valueOf( i - 1 ),
                 Integer.valueOf( i * 8 ) );
             importCmdTranslators[ i - 1 ] = new Translator( false, false, i - 1, 8, i * 8 );
           }
         }
         else
         {
           if ( i < importedCmdLength - 1 )
           {
             System.err.printf( "Creating importCmdTranslator for index %d to bit %d\n", Integer.valueOf( i ), Integer
                 .valueOf( i * 8 ) );
             importCmdTranslators[ i ] = new Translator( false, false, i, 8, i * 8 );
           }
         }
       }
     }
 
     return importedCode;
   }
   
   public String getIniString( boolean addName, boolean newName )
   {
     StringWriter sw = new StringWriter();
     PrintWriter pw = new PrintWriter( sw );
     String name = newName ? getDefaultName( id ) : getName();
     try
     { 
       pw.println( "[" + name + "]" );
       if ( addName )
       {
         pw.println( "Name=" + name );
       }
       pw.println( "PID=" + getID() );
       store( new PropertyWriter( pw ) );
     }
     catch ( Exception ex )
     {
       ex.printStackTrace( System.err );
     }
     return sw.toString();
   }
   
   public IniSection getIniSection()
   {
     StringReader sr = new StringReader( getIniString( true, false ) );
     BufferedReader in = new BufferedReader( sr );
     PropertyReader pr = new PropertyReader( in );
     return pr.nextSection();
   }
   
   public int getNameIndex()
   {
     if ( ( name == null ) || ! name.startsWith( "Manual Settings" ) )
     {
       return 0;
     }
     int begin = name.indexOf( "(" ) + 1;
     int end = name.indexOf( ")" );
     if ( begin == 0 || end == begin )
     {
       return 1;
     }
     else
     {        
       try
       {
         int nameIndex = Integer.parseInt( name.substring( begin, end ) );
         return nameIndex;         
       }
       catch ( NumberFormatException e )
       {
         return 0;
       }
     }
   }
   
   public static String getDefaultName( Hex pid )
   {
     String name = "Manual Settings: " + pid;
     int index = ProtocolManager.getManualSettingsIndex( pid );
     if ( index > 0 )
     {
       // There is already at least one manual protocol with this PID
       name += " (" + ( index + 1 ) + ")";
     }
     return name;
   }
 }
