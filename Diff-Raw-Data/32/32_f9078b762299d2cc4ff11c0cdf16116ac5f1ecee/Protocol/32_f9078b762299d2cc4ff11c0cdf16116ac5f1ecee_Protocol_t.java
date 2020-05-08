 package com.hifiremote.jp1;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumnModel;
 
 import com.hifiremote.jp1.importer.Importer;
 import com.hifiremote.jp1.importer.ImporterFactory;
 import com.hifiremote.jp1.importer.ReorderImporter;
 import com.hifiremote.jp1.initialize.CmdIndexInitializer;
 import com.hifiremote.jp1.initialize.Initializer;
 import com.hifiremote.jp1.initialize.InitializerFactory;
 import com.hifiremote.jp1.translate.Translate;
 import com.hifiremote.jp1.translate.Translator;
 import com.hifiremote.jp1.translate.TranslatorFactory;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class Protocol.
  */
 public class Protocol
 {
 
   /**
    * Instantiates a new protocol.
    * 
    * @param name
    *          the name
    * @param id
    *          the id
    * @param props
    *          the props
    */
   public Protocol( String name, Hex id, Properties props )
   {
     this.name = name;
     this.id = id;
 
     if ( props == null )
     {
       props = new Properties();
     }
     this.variantName = props.getProperty( "VariantName", "" );
     String temp = props.getProperty( "DefaultCmd", "00" );
     if ( temp != null )
     {
       this.defaultCmd = new Hex( temp );
     }
     this.cmdIndex = Integer.parseInt( props.getProperty( "CmdIndex", "0" ) );
 
     temp = props.getProperty( "AlternatePID" );
     if ( temp != null )
     {
       StringTokenizer st = new StringTokenizer( temp.trim(), "," );
       alternatePID = new Hex( st.nextToken() );
       while ( st.hasMoreTokens() )
       {
         altPIDOverrideList.add( st.nextToken() );
       }
     }
 
     temp = props.getProperty( "DevParms", "" );
     if ( temp != null )
     {
       devParms = DeviceParmFactory.createParameters( temp );
     }
 
     temp = props.getProperty( "DeviceTranslator" );
     if ( temp != null )
     {
       deviceTranslators = TranslatorFactory.createTranslators( temp );
     }
 
     temp = props.getProperty( "DeviceImporter" );
     if ( temp != null )
     {
       devImporters = ImporterFactory.createImporters( temp );
     }
     else
     {
       System.err.println( "Generating deviceImporter for protocol " + name );
       int mappedIndex = 0;
       boolean needRemap = false;
       String[] map = new String[ 6 ];
       int maxParm = Math.min( map.length, devParms.length );
       for ( int i = 0; i < maxParm; i++ )
       {
         System.err.println( "DevParm is " + devParms[ i ].getName() );
         if ( devParms[ i ].getClass() != FlagDeviceParm.class )
         {
           map[ i ] = Integer.toString( mappedIndex );
         }
         else
         {
           needRemap = true;
         }
         mappedIndex++ ;
       }
       if ( needRemap )
       {
         devImporters = new Importer[ 1 ];
         devImporters[ 0 ] = new ReorderImporter( map );
       }
     }
 
     defaultFixedData = new Hex( props.getProperty( "FixedData", "" ) );
     temp = props.getProperty( "FixedDataMask" );
     if ( temp != null )
     {
       fixedDataMask = new Hex( temp );
     }
     else
     {
       short[] mask = new short[ defaultFixedData.length() ];
       for ( int i = 0; i < mask.length; ++i )
       {
         mask[ i ] = 0xFF;
       }
 
       fixedDataMask = new Hex( mask );
     }
 
     temp = props.getProperty( "CmdTranslator" );
     if ( temp != null )
     {
       cmdTranslators = TranslatorFactory.createTranslators( temp );
     }
     else
     {
       cmdTranslators = new Translate[ 0 ];
     }
 
     temp = props.getProperty( "ImportCmdTranslator" );
     if ( temp != null )
     {
       importCmdTranslators = TranslatorFactory.createTranslators( temp );
     }
 
     notes = props.getProperty( "Notes" );
 
     int fixedBytes = -1;
     int variableBytes = -1;
     for ( String pName : ProcessorManager.getProcessorNames() )
     {
       temp = props.getProperty( "Code." + pName );
       if ( temp != null )
       {
         Hex hex = new Hex( temp );
         int value = hex.getData()[ 2 ];
         if ( pName.equals( "HCS08" ) )
         {
           value = hex.getData()[ 4 ];
         }
         int fixedDataLength = value >> 4;
         int cmdLength = value & 0x0F;
         if ( fixedBytes == -1 )
         {
           fixedBytes = fixedDataLength;
           variableBytes = cmdLength;
         }
         else if ( fixedBytes != fixedDataLength || variableBytes != cmdLength )
         {
           System.err.println( "Protocol code for " + pName + " uses " + fixedDataLength + " fixed bytes and "
               + cmdLength + " variable bytes instead of " + fixedBytes + " and " + variableBytes );
         }
         code.put( pName, hex );
       }
       temp = props.getProperty( "CodeTranslator." + pName );
       if ( temp != null )
       {
         Translate[] xlators = TranslatorFactory.createTranslators( temp );
         codeTranslator.put( pName, xlators );
       }
     }
 
     temp = props.getProperty( "CmdParms", "" );
     StringTokenizer st = new StringTokenizer( temp, "," );
     int count = st.countTokens();
     cmdParms = new CmdParameter[ count ];
     for ( int i = 0; i < count; i++ )
     {
       String str = st.nextToken();
       cmdParms[ i ] = CmdParmFactory.createParameter( str, devParms, cmdParms );
       if ( cmdParms[ i ] == null )
       {
         System.err.println( "Protocol.Protocol(" + name + ") failed createParameter(" + str + ")" );
       }
     }
     temp = props.getProperty( "CmdParmInit" );
     if ( temp != null )
     {
       cmdParmInit = InitializerFactory.create( temp );
     }
 
     temp = props.getProperty( "OldNames" );
     if ( temp != null )
     {
       StringTokenizer st2 = new StringTokenizer( temp, "," );
       while ( st2.hasMoreTokens() )
       {
         oldNames.add( st2.nextToken().trim() );
       }
     }
 
     temp = props.getProperty( "KeyMovesOnly" );
     keyMovesOnly = temp != null;
 
     // Figure out protocols that only have protocol code
     if ( cmdParms.length == 0 && code.size() > 0 )
     {
       // First figure out how many fixed bytes and cmd bytes there are
       Set< String > keys = code.keySet();
       Iterator< String > it = keys.iterator();
       String key = it.next();
       Hex pCode = code.get( key );
       int value = pCode.getData()[ 2 ];
       if ( key.equals( "HCS08" ) )
       {
         value = pCode.getData()[ 4 ];
       }
       int fixedDataLength = value >> 4;
       int cmdLength = value & 0x0F;
 
       // Generate the Device Parameters and Translators
       short[] hex = new short[ fixedDataLength ];
       defaultFixedData = new Hex( hex );
       int numDevParms = fixedDataLength; // Signal style and bits/cmd
       int styleIndex = numDevParms++ ;
       int devBitsIndex = -1;
       if ( fixedDataLength > 0 )
       {
         devBitsIndex = numDevParms++ ; // bits/dev
       }
       int cmdBitsIndex = numDevParms++ ;
       int cmdByteIndex = -1;
       if ( cmdLength > 1 )
       {
         cmdByteIndex = numDevParms++ ;
       }
       devParms = new DeviceParameter[ numDevParms ];
       deviceTranslators = new Translator[ fixedDataLength ];
       DirectDefaultValue defaultZero = new DirectDefaultValue( new Integer( 0 ) );
       String[] choices =
       {
           "MSB", "MSB-COMP", "LSB", "LSB-COMP"
       };
       devParms[ styleIndex ] = new ChoiceDeviceParm( "Signal Style", defaultZero, choices );
       DirectDefaultValue defaultEight = new DirectDefaultValue( new Integer( 8 ) );
       if ( devBitsIndex != -1 )
       {
         devParms[ devBitsIndex ] = new NumberDeviceParm( "Bits / Device", defaultEight, 10, 4 );
       }
       devParms[ cmdBitsIndex ] = new NumberDeviceParm( "Bits / Command", defaultEight, 10, 4 );
       if ( cmdByteIndex != -1 )
       {
         String[] indexChoices =
         {
             "0", "1"
         };
         devParms[ cmdByteIndex ] = new ChoiceDeviceParm( "Cmd byte index", defaultZero, indexChoices );
         cmdParmInit = new Initializer[ 1 ];
         cmdParmInit[ 0 ] = new CmdIndexInitializer( cmdByteIndex, this );
       }
 
       for ( int i = 0; i < fixedDataLength; i++ )
       {
         devParms[ i ] = new NumberDeviceParm( "Device " + i, defaultZero, 10 );
         Translator translator = new Translator( false, false, i, 8, i * 8 );
         deviceTranslators[ i ] = translator;
         translator.setStyleIndex( styleIndex );
         if ( devBitsIndex != -1 )
         {
           translator.setBitsIndex( devBitsIndex );
         }
       }
 
       hex = new short[ cmdLength ];
       defaultCmd = new Hex( hex );
       cmdTranslators = new Translate[ cmdLength ];
       cmdParms = new CmdParameter[ cmdLength ];
       for ( int i = 0; i < cmdLength; i++ )
       {
         String parmName = "OBC";
         if ( i != cmdIndex )
         {
           parmName = "Byte " + ( i + 1 );
         }
         cmdParms[ i ] = new NumberCmdParm( parmName, null );
         Translator translator = new Translator( false, false, i, 8, i * 8 );
         cmdTranslators[ i ] = translator;
         translator.setStyleIndex( styleIndex );
         translator.setBitsIndex( cmdBitsIndex );
       }
 
       short[] mask = new short[ defaultFixedData.length() ];
       for ( int i = 0; i < mask.length; ++i )
       {
         mask[ i ] = 0xFF;
       }
 
       fixedDataMask = new Hex( mask );
     }
   }
 
   /**
    * Gets the fixed data mask.
    * 
    * @return the fixed data mask
    */
   public Hex getFixedDataMask()
   {
     return fixedDataMask;
   }
 
   /**
    * Gets the first processor.
    * 
    * @return the first processor
    */
   public String getFirstProcessor()
   {
     Set< String > keys = code.keySet();
     Iterator< String > it = keys.iterator();
     String key = it.next();
     System.err.println( "Protocol.getFirstProcessor() key=" + key );
     return key;
   }
 
   /**
    * Gets the cmd length from code.
    * 
    * @return the cmd length from code
    */
   public int getCmdLengthFromCode()
   {
     String proc = getFirstProcessor();
     System.err.println( "Protocol.getCmdLengthFromCode() proc=" + proc );
     return getCmdLengthFromCode( proc, code.get( proc ) );
   }
 
   /**
    * Gets the cmd length from code.
    * 
    * @param proc
    *          the proc
    * @param pCode
    *          the code
    * @return the cmd length from code
    */
   public static int getCmdLengthFromCode( String proc, Hex pCode )
   {
     int value = pCode.getData()[ 2 ];
     if ( proc.equals( "HCS08" ) )
     {
       value = pCode.getData()[ 4 ];
     }
     return value & 0x0F;
   }
 
   /**
    * Gets the fixed data length from code.
    * 
    * @return the fixed data length from code
    */
   public int getFixedDataLengthFromCode()
   {
     String proc = getFirstProcessor();
     return getFixedDataLengthFromCode( proc, code.get( proc ) );
   }
 
   /**
    * Gets the fixed data length from code.
    * 
    * @param proc
    *          the proc
    * @param pCode
    *          the code
    * @return the fixed data length from code
    */
   public static int getFixedDataLengthFromCode( String proc, Hex pCode )
   {
     int value = pCode.getData()[ 2 ];
     if ( proc.equals( "HCS08" ) )
     {
       value = pCode.getData()[ 4 ];
     }
     return value >> 4;
   }
 
   /**
    * Reset.
    */
   public void reset()
   {
     int len = devParms.length;
     Value[] vals = new Value[ len ];
     for ( int i = 0; i < len; i++ )
     {
       vals[ i ] = new Value( null, devParms[ i ].getDefaultValue() );
     }
     setDeviceParms( vals );
   }
 
   /**
    * Sets the properties.
    * 
    * @param props
    *          the new properties
    * @param remote
    */
   public void setProperties( Properties props, Remote remote )
   {
     for ( String key : props.stringPropertyNames() )
     {
       if ( key.startsWith( "CustomCode." ) )
       {
         String procName = key.substring( 11 );
         Processor proc = ProcessorManager.getProcessor( procName );
         addCustomCode( proc, new Hex( props.getProperty( key ) ) );
       }
     }
   }
 
   /**
    * Import upgrade code.
    * 
    * @param notes
    *          the notes
    * @return the hex
    */
   public Hex importUpgradeCode( String notes )
   {
     Hex importedCode = null;
     StringTokenizer st = new StringTokenizer( notes, "\n" );
     String text = null;
     String processor = null;
     while ( st.hasMoreTokens() )
     {
       while ( st.hasMoreTokens() )
       {
         text = st.nextToken().toUpperCase();
         if ( text.startsWith( "UPGRADE PROTOCOL 0 =" ) )
         {
           int pos = text.indexOf( '(' );
           int pos2 = text.indexOf( ')', pos );
           processor = text.substring( pos + 1, pos2 );
           if ( processor.startsWith( "S3C8" ) )
           {
             processor = "S3C80";
           }
           System.err.println( "Imported processor name=" + processor );
           break;
         }
       }
       if ( st.hasMoreTokens() )
       {
         text = st.nextToken(); // 1st line of code
         while ( st.hasMoreTokens() )
         {
           String temp = st.nextToken();
           if ( temp.equalsIgnoreCase( "End" ) )
           {
             break;
           }
           text = text + ' ' + temp;
         }
         Processor p = ProcessorManager.getProcessor( processor );
         System.err.println( "Processor=" + p );
         importedCode = new Hex( text );
         System.err.println( "Protocol.importUpgradeCode(), putting code for name=" + p.getEquivalentName() + ",code="
             + importedCode );
         code.put( p.getEquivalentName(), importedCode );
       }
     }
     return importedCode;
   }
 
   public void addCustomCode( Processor processor, Hex code )
   {
     customCode.put( processor.getEquivalentName(), code );
   }
 
   public Hex getCustomCode( Processor processor )
   {
     return customCode.get( processor.getEquivalentName() );
   }
 
   public boolean hasCustomCode()
   {
     return !customCode.isEmpty();
   }
 
   /**
    * Gets the panel.
    * 
    * @param deviceUpgrade
    *          the device upgrade
    * @return the panel
    */
   public KMPanel getPanel( DeviceUpgrade deviceUpgrade )
   {
     return null;
   }
 
   /**
    * Initialize parms.
    */
   public void initializeParms()
   {
     if ( cmdParmInit != null )
     {
       for ( int i = 0; i < cmdParmInit.length; i++ )
       {
         cmdParmInit[ i ].initialize( devParms, cmdParms );
       }
     }
   }
 
   /**
    * Needs code.
    * 
    * @param remote
    *          the remote
    * @return true, if successful
    */
   public boolean needsCode( Remote remote )
   {
     return getCustomCode( remote.getProcessor() ) != null || !remote.supportsVariant( id, variantName );
   }
 
   /**
    * Checks for any code.
    * 
    * @return true, if successful
    */
   public boolean hasAnyCode()
   {
     return !code.isEmpty();
   }
 
   /**
    * Checks for code.
    * 
    * @param remote
    *          the remote
    * @return true, if successful
    */
   public boolean hasCode( Remote remote )
   {
     return getCode( remote ) != null;
   }
 
   /**
    * Gets the code.
    * 
    * @param remote
    *          the remote
    * @return the code
    */
   public Hex getCode( Remote remote )
   {
     return getCode( remote.getProcessor() );
   }
 
   /**
    * Gets the code.
    * 
    * @param p
    *          the p
    * @return the code
    */
   public Hex getCode( Processor p )
   {
     Hex hex = customCode.get( p.getEquivalentName() );
     if ( hex == null )
     {
       hex = code.get( p.getEquivalentName() );
     }
     return hex;
   }
 
   /**
    * Gets the code translators.
    * 
    * @param remote
    *          the remote
    * @return the code translators
    */
   public Translate[] getCodeTranslators( Remote remote )
   {
     return codeTranslator.get( remote.getProcessor().getEquivalentName() );
   }
 
   /**
    * Import device parms.
    * 
    * @param parms
    *          the parms
    */
   public void importDeviceParms( Value[] parms )
   {
     if ( devImporters != null )
     {
       for ( int i = 0; i < devImporters.length; i++ )
       {
         parms = devImporters[ i ].convertParms( parms );
       }
     }
     setDeviceParms( parms );
   }
 
   /**
    * Import fixed data.
    * 
    * @param hex
    *          the hex
    * @return the value[]
    */
   public Value[] importFixedData( Hex hex )
   {
     Value[] vals = getDeviceParmValues();
     for ( int i = 0; i < deviceTranslators.length; i++ )
     {
       deviceTranslators[ i ].out( hex, vals, devParms );
     }
     return vals;
   }
 
   /**
    * Sets the device parms.
    * 
    * @param parms
    *          the new device parms
    */
   public void setDeviceParms( Value[] parms )
   {
     if ( parms.length != devParms.length )
     {
       System.err.println( "Protocol.setDeviceParms(), protocol=" + getDiagnosticName() + ", parms.length="
           + parms.length + " and devParms.length=" + devParms.length );
     }
 
     for ( int i = 0; i < parms.length; i++ )
     {
       if ( i < devParms.length && parms[ i ] != null ) // && ( parms[ i ].getUserValue() != null ))
       {
         System.err.println( "Setting devParms[ " + i + " ](" + devParms[ i ].getName() + ") to "
             + parms[ i ].getUserValue() );
         devParms[ i ].setValue( parms[ i ].getUserValue() );
       }
     }
   }
 
   /**
    * Gets the device parm values.
    * 
    * @return the device parm values
    */
   public Value[] getDeviceParmValues()
   {
     Value[] rc = new Value[ devParms.length ];
     for ( int i = 0; i < rc.length; i++ )
     {
       DeviceParameter parm = devParms[ i ];
       rc[ i ] = new Value( parm.getValue(), parm.getDefaultValue() );
     }
     return rc;
   }
 
   public int getOEMParmVariance( Value[] vals )
   {
     int index = 0;
     for ( int i = 0; i < devParms.length; i++ )
     {
       String parmName = devParms[ i ].getName().toUpperCase();
       if ( parmName.startsWith( "OEM" ) || parmName.startsWith( "PARM" ) )
       {
 
         if ( vals[ i ].getValue() instanceof Integer && devParms[ i ].getDefaultValue().value() instanceof Integer )
         {
           Integer userValue = ( Integer )vals[ i ].getValue();
           Integer defaultValue = ( Integer )devParms[ i ].getDefaultValue().value();
           index += Math.abs( userValue.intValue() - defaultValue.intValue() );
         }
       }
     }
     return index;
   }
 
   /**
    * Gets the default cmd.
    * 
    * @return the default cmd
    */
   public Hex getDefaultCmd()
   {
     Hex rc = null;
     try
     {
       rc = ( Hex )defaultCmd.clone();
     }
     catch ( CloneNotSupportedException e )
     {}
     Value[] vals = new Value[ cmdParms.length ];
 
     for ( int i = 0; i < cmdParms.length; i++ )
     {
       DefaultValue def = cmdParms[ i ].getDefaultValue();
       Object val = null;
       if ( def != null )
       {
         val = def.value();
       }
       vals[ i ] = new Value( val );
     }
 
     for ( int i = 0; i < cmdTranslators.length; i++ )
     {
       cmdTranslators[ i ].in( vals, rc, devParms, -1 );
     }
 
     return rc;
   }
 
   /**
    * Gets the cmd index.
    * 
    * @return the cmd index
    */
   public int getCmdIndex()
   {
     return cmdIndex;
   }
 
   /**
    * Sets the cmd index.
    * 
    * @param index
    *          the new cmd index
    */
   public boolean setCmdIndex( int newIndex )
   {
     boolean nameChanged = false;
 
     if ( cmdIndex != newIndex )
     {
       if ( cmdParms[ newIndex ].getName().equals( "Byte " + ( newIndex + 1 ) ) )
       {
         cmdParms[ newIndex ].setName( "OBC" );
         nameChanged = true;
       }
       if ( cmdParms[ cmdIndex ].getName().equals( "OBC" ) )
       {
         cmdParms[ cmdIndex ].setName( "Byte " + ( cmdIndex + 1 ) );
         nameChanged = true;
       }
 
       cmdIndex = newIndex;
     }
     return nameChanged;
   }
 
   /**
    * Gets the device parameters.
    * 
    * @return the device parameters
    */
   public DeviceParameter[] getDeviceParameters()
   {
     return devParms;
   }
 
   /**
    * Gets the command parameters.
    * 
    * @return the command parameters
    */
   public CmdParameter[] getCommandParameters()
   {
     return cmdParms;
   }
 
   /**
    * Gets the notes.
    * 
    * @return the notes
    */
   public String getNotes()
   {
     return notes;
   }
 
   /**
    * Gets the old names.
    * 
    * @return the old names
    */
   public java.util.List< String > getOldNames()
   {
     return oldNames;
   }
 
   // These methods allow adding columns to the Functions Panel
   /**
    * Gets the column count.
    * 
    * @return the column count
    */
   public int getColumnCount()
   {
     return cmdParms.length;
   }
 
   /**
    * Gets the column class.
    * 
    * @param col
    *          the col
    * @return the column class
    */
   public Class< ? > getColumnClass( int col )
   {
     return cmdParms[ col ].getValueClass();
   }
 
   /**
    * Gets the column editor.
    * 
    * @param col
    *          the col
    * @return the column editor
    */
   public TableCellEditor getColumnEditor( int col )
   {
     return cmdParms[ col ].getEditor();
   }
 
   /**
    * Gets the column renderer.
    * 
    * @param col
    *          the col
    * @return the column renderer
    */
   public TableCellRenderer getColumnRenderer( int col )
   {
     return cmdParms[ col ].getRenderer();
   }
 
   /**
    * Gets the column name.
    * 
    * @param col
    *          the col
    * @return the column name
    */
   public String getColumnName( int col )
   {
     return cmdParms[ col ].getDisplayName();
   }
 
   /**
    * Gets the values.
    * 
    * @param hex
    *          the hex
    * @return the values
    */
   public Value[] getValues( Hex hex )
   {
     Value[] vals = new Value[ cmdParms.length ];
     for ( int i = 0; i < cmdTranslators.length; i++ )
     {
       cmdTranslators[ i ].out( hex, vals, devParms );
     }
     for ( int i = 0; i < cmdParms.length; i++ )
     {
       System.err.println( "Setting default for index " + i );
       System.err.println( "vals[ " + i + " ] is " + vals[ i ] );
       vals[ i ].setDefaultValue( cmdParms[ i ].getDefaultValue() );
     }
     return vals;
   }
 
   /**
    * Gets the value at.
    * 
    * @param col
    *          the col
    * @param hex
    *          the hex
    * @return the value at
    */
   public Object getValueAt( int col, Hex hex )
   {
     Value[] vals = getValues( hex );
     Value v = vals[ col ];
     if ( v == null )
     {
       System.err.println( "Protocol.getValueAt(" + col + ") failed" );
       return new Integer( 0 );
     }
     return cmdParms[ col ].getValue( v.getValue() );
   }
 
   /**
    * Sets the value at.
    * 
    * @param col
    *          the col
    * @param hex
    *          the hex
    * @param value
    *          the value
    */
   public void setValueAt( int col, Hex hex, Object value )
   {
     Value[] vals = getValues( hex );
     vals[ col ] = new Value( cmdParms[ col ].convertValue( value ), null );
     for ( int i = 0; i < cmdTranslators.length; i++ )
     {
       cmdTranslators[ i ].in( vals, hex, devParms, col );
     }
   }
 
   /**
    * Import command.
    * 
    * @param hex
    *          the hex
    * @param text
    *          the text
    * @param useOBC
    *          the use obc
    * @param obcIndex
    *          the obc index
    * @param useEFC
    *          the use efc
    */
   public void importCommand( Hex hex, String text, boolean useOBC, int obcIndex, boolean useEFC )
   {
     if ( useEFC )
     {
       EFC.toHex( Short.parseShort( text ), hex, cmdIndex );
     }
     else
     {
       // if ( useOBC )
       setValueAt( obcIndex, hex, new Short( text ) );
     }
   }
 
   /**
    * Import command parms.
    * 
    * @param hex
    *          the hex
    * @param text
    *          the text
    */
   public void importCommandParms( Hex hex, String text )
   {
     System.err.println( "Protocol.importCommandParms( " + text + " ), cmdParms.length=" + cmdParms.length );
 
     if ( cmdParms.length == 1 )
     {
       return;
     }
     Translate[] translators = importCmdTranslators;
     if ( translators == null )
     {
       translators = cmdTranslators;
     }
     StringTokenizer st = new StringTokenizer( text );
     Value[] values = new Value[ st.countTokens() ];
     int index = 0;
     while ( st.hasMoreTokens() )
     {
       String val = st.nextToken();
       Object obj = null;
       try
       {
         obj = new Integer( val );
       }
       catch ( NumberFormatException x )
       {
         obj = val;
       }
       values[ index++ ] = new Value( obj );
     }
 
     for ( index = 0; index < values.length; index++ )
     {
       for ( int i = 0; i < translators.length; i++ )
       {
         translators[ i ].in( values, hex, devParms, index );
       }
     }
   }
 
   /**
    * Checks if is editable.
    * 
    * @param col
    *          the col
    * @return true, if is editable
    */
   public boolean isEditable( int col )
   {
     return true;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
     return getName();
   }
 
   /**
    * Gets the name.
    * 
    * @return the name
    */
   public String getName()
   {
     return name;
   }
 
   /**
    * Gets the iD.
    * 
    * @return the iD
    */
   public Hex getID()
   {
     return id;
   }
 
   /**
    * Gets the alternate pid.
    * 
    * @return the alternate pid
    */
   public Hex getAlternatePID()
   {
     return alternatePID;
   }
 
   /**
    * Gets the iD.
    * 
    * @param remote
    *          the remote
    * @return the iD
    */
   public Hex getID( Remote remote )
   {
     if ( alternatePID == null )
     {
       return id;
     }
 
     if ( !needsCode( remote ) )
     {
       return id;
     }
 
     // At this point we know that this protocol variant is not built-in, and that there is
     // an alternate PID. But we should only use the alternate PID if the remote has a
     // different variant of the main PID built in. If this is not so, use the main PID.
 
     Protocol p = ProtocolManager.getProtocolManager().findProtocolForRemote( remote, id, false );
 
     if ( p == null )
     {
       return id;
     }
 
     // There is a protocol with this ID in the remote, so use the alternate PID unless
     // the variant in the remote is included in the override list of the alternate.
 
     if ( altPIDOverrideList.isEmpty() )
     {
       // There is no override list, so use the alternate PID.
       return alternatePID;
     }
 
     // There is an override list, so check if the built-in variant is included. If it is,
     // then we override it in the remote by using the main PID, not the alternate. An entry
     // of "none" in the override list corresponds to overriding the null variant, whose value
     // is an empty string. For safety, check for either a null string or an empty one.
 
     String builtin = p.getVariantName();
     if ( builtin == null || builtin.isEmpty() )
     {
       builtin = "none";
     }
 
     for ( String temp : altPIDOverrideList )
     {
       if ( temp.equalsIgnoreCase( builtin ) )
       {
         return id;
       }
     }
     return alternatePID;
   }
   
   public String getStarredID( Remote remote )
   {
     String starredID = id.toString();
     if ( needsCode( remote ) )
     {
       starredID += "*";
     }
     return starredID;
   }
 
   /**
    * Gets the variant name.
    * 
    * @return the variant name
    */
   public String getVariantName()
   {
     return variantName;
   }
 
   /**
    * Gets the diagnostic name.
    * 
    * @return the diagnostic name
    */
   public String getDiagnosticName()
   {
     String result = "\"" + name + "\" (" + id;
     if ( variantName.length() > 0 )
     {
       result += " : " + variantName;
     }
     return result + ")";
   }
 
   /**
    * Gets the fixed data.
    * 
    * @param parms
    *          the parms
    * @return the fixed data
    */
   public Hex getFixedData( Value[] parms )
   {
     Hex temp = null;
     try
     {
       temp = ( Hex )defaultFixedData.clone();
     }
     catch ( CloneNotSupportedException e )
     {}
     // Value[] parms = getDeviceParmValues();
     if ( deviceTranslators != null )
     {
       for ( int i = 0; i < deviceTranslators.length; i++ )
       {
         deviceTranslators[ i ].in( parms, temp, devParms, -1 );
       }
     }
     return temp;
   }
 
   /**
    * Gets the fixed data length.
    * 
    * @return the fixed data length
    */
   public int getFixedDataLength()
   {
     return defaultFixedData.length();
   }
 
   // 
   /**
    * Convert the functions defined in this protocol to the new Protocol
    * 
    * @param funcs
    *          the funcs
    * @param newProtocol
    *          the new protocol
    * @return true, if successful
    */
   public boolean convertFunctions( java.util.List< Function > funcs, Protocol newProtocol )
   {
     CmdParameter[] newParms = newProtocol.cmdParms;
 
     int max = cmdParms.length;
     if ( newProtocol.cmdParms.length < max )
     {
       max = newProtocol.cmdParms.length;
     }
     // count the number of matching parameters
     int matchingParms = 0;
     // create a map of command parameter indexes from this protocol to the new one
     int[] oldIndex = new int[ max ];
     int[] newIndex = new int[ max ];
     for ( int i = 0; i < cmdParms.length; i++ )
     {
       String name = cmdParms[ i ].getName();
       for ( int j = 0; j < newParms.length; j++ )
       {
         if ( name.equals( newParms[ j ].getName() ) )
         {
           oldIndex[ matchingParms ] = i;
           newIndex[ matchingParms ] = j;
           matchingParms++ ;
           break;
         }
       }
     }
 
     // create Value arrays for holding the command parameter values
     Value[] currValues = new Value[ cmdParms.length ];
     Value[] newValues = new Value[ newProtocol.cmdParms.length ];
     // setup the correct default values.
     for ( int i = 0; i < newValues.length; i++ )
     {
       newValues[ i ] = new Value( null, newProtocol.cmdParms[ i ].getDefaultValue() );
     }
 
     // now convert each defined function
     java.util.List< java.util.List< String >> failedToConvert = new ArrayList< java.util.List< String >>();
     Hex[] convertedHex = new Hex[ funcs.size() ];
     int index = 0;
     for ( Function f : funcs )
     {
       Hex hex = f.getHex();
       Hex newHex = newProtocol.getDefaultCmd();
       if ( hex != null )
       {
         // extract the command parms from the hex
         for ( int i = 0; i < cmdTranslators.length; i++ )
         {
           cmdTranslators[ i ].out( hex, currValues, devParms );
         }
 
         // copy the matching parameters to the new Values;
         for ( int i = 0; i < matchingParms; i++ )
         {
           newValues[ newIndex[ i ] ] = currValues[ oldIndex[ i ] ];
         }
 
         // generate the appropriate hex for the new protocol
         try
         {
           for ( int i = 0; i < newProtocol.cmdTranslators.length; i++ )
           {
             newProtocol.cmdTranslators[ i ].in( newValues, newHex, newProtocol.devParms, -1 );
           }
         }
         catch ( IllegalArgumentException ex )
         {
           java.util.List< String > temp = new ArrayList< String >( 2 );
           temp.add( f.getName() );
           temp.add( ex.getMessage() );
           failedToConvert.add( temp );
         }
         convertedHex[ index++ ] = newHex;
       }
       else
       {
         convertedHex[ index++ ] = null;
       }
     }
     if ( !failedToConvert.isEmpty() )
     {
       String message = "<html>The following functions could not be converted for use with the " + newProtocol.getName()
           + " protocol.<p>If you need help figuring out what to do about this, please post<br>"
           + "a question in the JP1 Forums at http://www.hifi-remote.com/forums</html>";
 
       JPanel panel = new JPanel( new BorderLayout() );
 
       JLabel text = new JLabel( message );
       text.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
       panel.add( text, BorderLayout.NORTH );
 
       java.util.List< String > titles = new ArrayList< String >();
       titles.add( "Function" );
       titles.add( "Reason" );
       String[][] failedToConvertArray = new String[ failedToConvert.size() ][];
       int i = 0;
       for ( java.util.List< String > l : failedToConvert )
       {
         failedToConvertArray[ i++ ] = l.toArray( new String[ 2 ] );
       }
       JTableX table = new JTableX( failedToConvertArray, titles.toArray() );
       Dimension d = table.getPreferredScrollableViewportSize();
       int showRows = Math.min( 14, failedToConvert.size() );
       // d.height = ( table.getRowHeight() + table.getIntercellSpacing().height ) * showRows;
       d.height = table.getRowHeight() * showRows;
       int nameWidth = 0;
       int warningWidth = 0;
       TableColumnModel cm = table.getColumnModel();
       JTableHeader th = table.getTableHeader();
       DefaultTableCellRenderer cr = ( DefaultTableCellRenderer )th.getDefaultRenderer();
       for ( int j = 0; j < failedToConvertArray.length; ++j )
       {
         String[] vals = failedToConvertArray[ j ];
         cr.setText( vals[ 0 ] );
         nameWidth = Math.max( nameWidth, cr.getPreferredSize().width );
         cr.setText( vals[ 1 ] );
         warningWidth = Math.max( warningWidth, cr.getPreferredSize().width );
       }
       cm.getColumn( 0 ).setPreferredWidth( nameWidth );
       cm.getColumn( 1 ).setPreferredWidth( warningWidth );
       d.width = nameWidth + table.getIntercellSpacing().width + warningWidth + 10;
       table.setPreferredScrollableViewportSize( d );
       panel.add( new JScrollPane( table ), BorderLayout.CENTER );
 
       String[] buttonText =
       {
           "Use " + newProtocol + " anyway", "Revert to " + this
       };
       int rc = JOptionPane.showOptionDialog( null, panel, "Change Protocol Error", JOptionPane.YES_NO_OPTION,
           JOptionPane.WARNING_MESSAGE, null, buttonText, buttonText[ 0 ] );
       if ( rc == JOptionPane.NO_OPTION )
       {
         return false;
       }
     }
     // copy the converted hex values into the functions
     index = 0;
     for ( Function f : funcs )
     {
       f.setHex( convertedHex[ index++ ] );
     }
     return true;
   }
   
   public ProtocolUpgrade getProtocolUpgrade( Remote remote )
   {
     int pid = id.get( 0 );
    Hex code = null;
    // Make sure that device combiner override of getCode(remote) is not called when
    // custom code is present.
    if ( hasCustomCode() )
    {
      code = getCode( remote.getProcessor() );
    }
    else
    {
      code = getCode( remote );
    }
     if ( code == null )
     {
       return null;
     }
     // The new code for S3C80Processor.translate now ensures that manual protocols
     // are not translated when they are already correct for the remote concerned.
     code = remote.getProcessor().translate( code, remote );
     ProtocolUpgrade pu = new ProtocolUpgrade( pid, code, null );
     pu.setProtocol( this );
     return pu;
   }
 
   /**
    * Update functions.
    * 
    * @param funcs
    *          the funcs
    */
   public void updateFunctions( java.util.List< Function > funcs )
   {
     Value[] values = new Value[ cmdParms.length ];
     for ( Function f : funcs )
     {
       Hex hex = f.getHex();
       if ( hex != null )
       {
         // extract the command parms from the hex
         for ( int i = 0; i < cmdTranslators.length; i++ )
         {
           cmdTranslators[ i ].out( hex, values, devParms );
         }
 
         // recompute the hex
         for ( int i = 0; i < cmdTranslators.length; i++ )
         {
           cmdTranslators[ i ].in( values, hex, devParms, -1 );
         }
 
         // store the hex back into the function
         f.setHex( hex );
       }
     }
   }
 
   /**
    * Different.
    * 
    * @param props
    *          the props
    * @return the int
    */
   public int different( Properties props )
   //
   // This is intended to become a fuzzy comparison to help select the best protocol
   // when protocols.ini has been changed and there is no perfect fit.
   //
   // It returns the value tooDifferent in cases where this protocol wouldn't be
   // good enough even if it were the best found. (It never returns any value greater
   // than tooDifferent).
   // It returns 0 for a perfect match or a larger value for a worse match.
   //
   // For now it absolutely requires a match of id. I expect that won't always be
   // an absolute.
   {
     Hex pid = new Hex( props.getProperty( "Protocol" ) );
     if ( !pid.equals( id ) )
     {
       return tooDifferent;
     }
     int result = 0;
     String str = props.getProperty( "Protocol.name" );
     if ( str != null && !str.equals( name ) )
     {
       // I think we should use a fuzzy string compare here, but for now...
       result += 1000;
     }
     str = props.getProperty( "FixedData" );
     if ( str != null )
     {
       Hex hex = new Hex( str );
       if ( hex.length() != defaultFixedData.length() )
       {
         result += 2000;
       }
       else
       {
         // Ought to compare lengths and valid ranges of parms
         // Ought to test translate Parms to see how closely they match
       }
     }
     return result;
   }
 
   /**
    * Store.
    * 
    * @param out
    *          the out
    * @param parms
    *          the parms
    * @throws IOException
    *           Signals that an I/O exception has occurred.
    */
   public void store( PropertyWriter out, Value[] parms ) throws IOException
   {
     out.print( "Protocol", id.toString() );
     out.print( "Protocol.name", getName() );
     if ( variantName.length() > 0 )
     {
       out.print( "Protocol.variantName", variantName );
     }
     // Value[] parms = getDeviceParmValues();
     if ( parms != null && parms.length != 0 )
     {
       out.print( "ProtocolParms", DeviceUpgrade.valueArrayToString( parms ) );
     }
     Hex fixedData = getFixedData( parms );
     if ( fixedData != null )
     {
       out.print( "FixedData", fixedData.toString() );
     }
     for ( Map.Entry< String, Hex > entry : customCode.entrySet() )
     {
       out.print( "CustomCode." + entry.getKey(), entry.getValue().toString() );
     }
   }
 
   /**
    * Gets the device translators.
    * 
    * @return the device translators
    */
   public Translate[] getDeviceTranslators()
   {
     return deviceTranslators;
   }
 
   /**
    * Gets the cmd translators.
    * 
    * @return the cmd translators
    */
   public Translate[] getCmdTranslators()
   {
     return cmdTranslators;
   }
 
   /**
    * Checks if is column width fixed.
    * 
    * @param col
    *          the col
    * @return true, if is column width fixed
    */
   public boolean isColumnWidthFixed( int col )
   {
     return true;
   }
 
   /**
    * Gets the key moves only.
    * 
    * @return the key moves only
    */
   public boolean getKeyMovesOnly()
   {
     return keyMovesOnly;
   }
 
   /** The Constant tooDifferent. */
   public final static int tooDifferent = 0x7FFFFFFF;
 
   /** The name. */
   protected String name = null;;
 
   /** The id. */
   protected Hex id = null;
 
   /** The alternate pid. */
   private Hex alternatePID = null;
 
   /** The variant name. */
   protected String variantName = null;
 
   /** The fixed data. */
   protected Hex defaultFixedData = null;
 
   /** The fixed data mask. */
   protected Hex fixedDataMask = null;
 
   /** The default cmd. */
   protected Hex defaultCmd = null;
 
   /** The cmd index. */
   protected int cmdIndex;
 
   /** The dev parms. */
   protected DeviceParameter[] devParms = null;
 
   /** The device translators. */
   protected Translate[] deviceTranslators = new Translate[ 0 ];
 
   /** The dev import translators. */
   protected Translate[] devImportTranslators = null;
 
   /** The cmd parms. */
   protected CmdParameter[] cmdParms = null;
 
   /** The cmd translators. */
   protected Translate[] cmdTranslators = null;
 
   /** The import cmd translators. */
   protected Translate[] importCmdTranslators = null;
 
   /** The dev importers. */
   protected Importer[] devImporters = null;
 
   /** The code. */
   protected HashMap< String, Hex > code = new HashMap< String, Hex >( 6 );
 
   protected HashMap< String, Hex > customCode = new HashMap< String, Hex >( 6 );
 
   /** The code translator. */
   protected HashMap< String, Translate[] > codeTranslator = new HashMap< String, Translate[] >( 6 );
 
   /** The cmd parm init. */
   protected Initializer[] cmdParmInit = null;
 
   /** The notes. */
   protected String notes = null;
 
   /** The old names. */
   private java.util.List< String > oldNames = new ArrayList< String >();
 
   /** The alt pid override list. */
   private java.util.List< String > altPIDOverrideList = new ArrayList< String >();
 
   /** The key moves only. */
   private boolean keyMovesOnly = false;
 }
