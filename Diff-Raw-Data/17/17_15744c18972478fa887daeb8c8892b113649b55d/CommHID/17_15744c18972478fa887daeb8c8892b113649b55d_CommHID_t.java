 package com.hifiremote.jp1.io;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import com.codeminders.hidapi.HIDManager;
 import com.codeminders.hidapi.ClassPathLibraryLoader;
 //import com.codeminders.hidapi.ClassPathLibraryLoader;
 import com.codeminders.hidapi.HIDDevice;
 import com.codeminders.hidapi.HIDDeviceInfo;
 //import com.codeminders.hidapi.HIDDeviceNotFoundException;
 import com.hifiremote.jp1.Hex;
 import com.hifiremote.jp1.Remote;
 import com.hifiremote.jp1.RemoteManager;
 import com.hifiremote.jp1.RemoteMaster;
  
 public class CommHID extends IO 
 {
 	HIDManager hid_mgr;
 	HIDDevice devHID;
 	Remote remote = null;
 	int thisPID;
 	String signature;
 	int E2address;
 	int E2size;
 	HIDDeviceInfo[] HIDinfo = new HIDDeviceInfo[10];
 	byte outReport[] = new byte[65];  // Note the asymmetry:  writes need outReport[0] to be an index 
 	byte inReport[] = new byte[64];   // reads don't return the index byte
 	byte dataRead[] = new byte[0x420];
 	byte ssdIn[] = new byte[62];
 	byte ssdOut[] = new byte[62];
 	int interfaceType = -1;
 	int firmwareFileCount = 0;
 	LinkedHashMap< String, Hex > firmwareFileVersions = new LinkedHashMap< String, Hex >();
 	
 	public static void LoadHIDLibrary()  {
 		ClassPathLibraryLoader.loadNativeHIDLibrary();
 	}
 	
 	int getPIDofAttachedRemote() {
 		try  {
 			hid_mgr = HIDManager.getInstance();
 			HIDinfo = hid_mgr.listDevices();
 			for (int i = 0; i<HIDinfo.length; i++)  
 				if (HIDinfo[i].getVendor_id() == 0x06E7) {
 					thisPID = HIDinfo[i].getProduct_id();
 					return thisPID;
 				}
 		}  catch (Exception e) {
     		return 0;
     	}
 		return 0;
 	}
 	
 	public String getInterfaceName() {
 		return "CommHID";
 	}
 	
 	 public String getInterfaceVersion() {
 		 return "0.1";
 	 }
 	 
 	 public String[] getPortNames() {
 		 String[] portNames  = {"HID"};
 		 return portNames;
 	 }
 	 
 	 int getRemotePID() {
 			return thisPID;
 		}
 	
 	byte jp12ComputeCheckSum( byte[] data, int start, int length ) {
 		int sum = 0;
 		int end = start + length;
 		for (int i = start; i < end; i++)  {
 			sum ^= (int)data[i] & 0xFF;
 		}
 		return (byte) sum;
 	}
 
 	void assembleMAXQreadAddress( int address, int blockLength, byte[] cmdBuff) {   
 		cmdBuff[0] = 0x00;  //packet length
 		cmdBuff[1] = 0x08;  //packet length
 		cmdBuff[2] = 0x01;  //Read command
 		cmdBuff[3] = (byte) ((address >> 24) & 0xff);
 		cmdBuff[4] = (byte) ((address >> 16) & 0xff);
 		cmdBuff[5] = (byte) ((address >>  8) & 0xff);
 		cmdBuff[6] = (byte) (address & 0xff);
 		cmdBuff[7] = (byte) ((blockLength >>  8) & 0xff);
 		cmdBuff[8] = (byte) (blockLength & 0xff);
 		cmdBuff[9] = jp12ComputeCheckSum(cmdBuff, 0, 9);
 	}
 	
 	boolean eraseMAXQ_Lite( int startAddress, int endAddress ){
 		byte[] cmdBuff = new byte[12];
 		cmdBuff[0] = (byte) 0x00;  //packet length
 		cmdBuff[1] = (byte) 0x0A;  //packet length
 		cmdBuff[2] = (byte) 0x03;  //erase command
 		cmdBuff[3] = (byte)( (startAddress >> 24) & 0xff);
 		cmdBuff[4] = (byte)((startAddress >> 16) & 0xff);
 		cmdBuff[5] = (byte)((startAddress >>  8) & 0xff);
 		cmdBuff[6] = (byte)(startAddress & 0xff);
 		cmdBuff[7] = (byte)((endAddress >> 24) & 0xff);
 		cmdBuff[8] = (byte)((endAddress >> 16) & 0xff);
 		cmdBuff[9] = (byte)((endAddress >>  8) & 0xff);
 		cmdBuff[10] = (byte)(endAddress & 0xff);
 		cmdBuff[11] = jp12ComputeCheckSum(cmdBuff, 0, 11);
 		System.arraycopy(cmdBuff, 0, outReport, 1, cmdBuff.length);
 		try {
 			devHID.write(outReport);
 		} catch (Exception e) {
 			return false;
 		}
 		if ( !readMAXQreport() || (dataRead[2] != 0) ) //Wait for remote to respond and check for error
 			return false;
 		return true;
 	}
 
 	boolean writeMAXQ_Lite_Block( int address, byte[] buffer, int blockLength ) {
 			byte[] cmdBuff = new byte[7]; 
 			int pkgLen;
 			if (blockLength > 0x38) 
 				return false;
 			pkgLen = blockLength + 6;
 			cmdBuff[0] = (byte) (pkgLen >> 8);  //packet length
 			cmdBuff[1] = (byte) (pkgLen & 0xFF);  //packet length
 			cmdBuff[2] = (byte) 0x02;  //write command
 			cmdBuff[3] = (byte) ((address >> 24) & 0xff);
 			cmdBuff[4] = (byte) ((address >> 16) & 0xff);
 			cmdBuff[5] = (byte) ((address >>  8) & 0xff);
 			cmdBuff[6] = (byte) (address & 0xff);
 			System.arraycopy(cmdBuff, 0, outReport, 1, cmdBuff.length);  //outReport must contain an index byte
 			System.arraycopy(buffer, 0, outReport, cmdBuff.length + 1, blockLength);
 			outReport[blockLength + cmdBuff.length + 1] = jp12ComputeCheckSum(outReport, 1, blockLength + cmdBuff.length);
 			try {
 				devHID.write(outReport);
 			} catch (Exception e) {
 				return false;
 			}
 			return true;
 		}
 
 	boolean writeMAXQcmdReport(byte [] cmdBuff)  {
 		  System.arraycopy(cmdBuff, 0, outReport, 1, cmdBuff.length);
 		  try {
 		    devHID.write(outReport);
 		  } catch (Exception e) {
 		    return false;
 		  }
 		  return true;
 	}
 	
 	boolean readMAXQreport()  {
 		try {
 			devHID.readTimeout(inReport, 3000);
 			System.arraycopy(inReport, 0, dataRead, 0, 64);
 		} catch (Exception e) {
 			return false;
 		}
 		return true;
 	}
 	
 	boolean MAXQ_USB_getInfoAndSig()  {
 		byte[] cmdBuff = {(byte)0x00, (byte)0x02, (byte)0x50, (byte)0x52};
 		int sigAdr, E2StartPtr, E2EndPtr, temp;
 		if (!writeMAXQcmdReport(cmdBuff))
 			return false;
 		if (!readMAXQreport() || (dataRead[0] != 0) || (dataRead[1] != 8) || (dataRead[2] != 0) )  
 			return false;
 		sigAdr = ((dataRead[6] & 0xFF) << 16) + ((dataRead[7] & 0xFF) << 8) + (dataRead[8] & 0xFF);
 		if (readMAXQ_Lite(sigAdr, dataRead, 0x54) != 0x54)
 			return false;
 		try {
 			signature = new String(dataRead, 6,6, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 		}
 		E2StartPtr = ((dataRead[52] & 0xFF) << 16) + ((dataRead[53] & 0xFF) << 8) + (dataRead[54] & 0xFF);
 		E2EndPtr   = ((dataRead[56] & 0xFF) << 16) + ((dataRead[57] & 0xFF) << 8) + (dataRead[58] & 0xFF);
 		if (readMAXQ_Lite(E2StartPtr, dataRead, 0x04 )  != 0x04)
 			return false;
 		E2address = ((dataRead[0] & 0xFF) << 24) + ((dataRead[1] & 0xFF) << 16) + ((dataRead[2] & 0xFF) << 8) + (dataRead[3] & 0xFF);
 		if(readMAXQ_Lite(E2EndPtr,  dataRead, 0x04 ) != 0x04)
 			return false;
 		temp = ((dataRead[0] & 0xFF) << 24) + ((dataRead[1] & 0xFF) << 16) + ((dataRead[2] & 0xFF) << 8) + (dataRead[3] & 0xFF);
 		E2size = temp - E2address;
 		return true;
 	}
 	
 	@Override
 	public String openRemote(String notUsed) {
 	  try  
 	  {
 	    getPIDofAttachedRemote();
 	    devHID = hid_mgr.openById(0x06E7, thisPID, null);
 	    devHID.enableBlocking();
 	    List< Remote > remotes = RemoteManager.getRemoteManager().findRemoteBySignature( getRemoteSignature() );
 	    if ( remotes.size() > 0 )
 	    {
 	      remote = remotes.get( 0 );
 	      remote.load();
 	      interfaceType = remote.isSSD() ? 0x201 : 0x106;
 	    }
 	    if ( interfaceType == 0x106 )
 	    {
 	      MAXQ_USB_getInfoAndSig();
 	    }
 	    else
 	    {
 	      E2address = remote.getBaseAddress();
 	      E2size = remote.getEepromSize();
 	    }
 	  }  
 	  catch (Exception e) 
 	  {
 	    return "";
 	  }
 	  return "HID";
 	}
 
 	@Override
 	public void closeRemote() {
 	  try  {
 	    devHID.close();
 	  } catch (Exception e) {
 
 	  }	
 	}
 
 	@Override
 	public String getRemoteSignature() {
 	  return "USB" + Integer.toHexString( thisPID ).toUpperCase();
 	}
 
 	@Override
 	public int getRemoteEepromAddress() {
 	  return E2address;
 	}
 
 	@Override
 	public int getRemoteEepromSize() {
 	  return E2size;
 	}
 	
 	@Override
 	public boolean remoteUsesSSD()
 	{
 	  return ( remote != null ) && remote.isSSD();
 	}
 	
 	@Override
 	public int getInterfaceType() {
 	  return interfaceType;
 	}
 	
 	int readMAXQ_Lite( int address, byte[] buffer, int length ) {  //MAXQ
 		byte[] cmdBuff = new byte[10];
 		assembleMAXQreadAddress(address, length, cmdBuff);
 		int numToRead = length + 4;  // total packet  length plus error byte and checksum
 		if (!writeMAXQcmdReport(cmdBuff))
 			return -1;
 		int numReports = 1 + numToRead/64;
 		int dataIdx = 0;
 		int reportOffset = 3;  //First report has length and error bytes
 		for (int i=0; i < numReports; i++) {
 			try {
 				devHID.readTimeout(inReport, 3000);
 				System.arraycopy(inReport,reportOffset, buffer, dataIdx, 
 				                      Math.min(length - dataIdx, 64 - reportOffset));
 			} catch (Exception e) {
 				return -1;
 			}
 			dataIdx += 64 - reportOffset;
 			reportOffset = 0;
 		}
 		return length;
 	}
 	
 	int writeMAXQ_Lite( int address,  byte[] buffer, int length )  {
 		int writeBlockSize = 0x38;
 		int erasePageSize = 0x200;
 		int offset, endAdr;
 		int blockLength = writeBlockSize;
 		byte tempBuf[] = new byte[65];
 		if ((address < E2address) || (address + length > E2address + E2size) )
 			return -1;
 		if ((length % erasePageSize) != 0)
 			return -1;
 		endAdr = address + length - 1;
 		eraseMAXQ_Lite( address, endAdr );
 		offset = 0;
 		do {
 			if (( offset + blockLength ) > length )
 				blockLength = length - offset;
 			System.arraycopy(buffer, offset, tempBuf, 0, blockLength);
 			if ( !writeMAXQ_Lite_Block( address + offset, tempBuf, blockLength ))
 				return -1;
 			if ( !readMAXQreport() || (dataRead[2] != 0) ) //Wait for remote to respond and check for error
 				return -1;
 			offset += blockLength;
 		}  while ( offset < length ); 
 		return offset;
 	}
 	
 	public int readRemote( int address, byte[] buffer, int length ) 
 	{
 		int bytesRead = -1;
 		if ( interfaceType == 0x106 )
 		{
 			bytesRead = readMAXQ_Lite(address,buffer, length);
 		}
 		else if ( interfaceType == 0x201 )
 		{
 		  bytesRead = readTouch( buffer );
 		}
     return bytesRead;
 	}
 	
 	private int getEndBXML( int fileStart, byte[] buffer )
   {
 	  int pos = fileStart;
 	  int itemsLength = ( buffer[ pos + 14 ] & 0xFF ) |  ( ( buffer[ pos + 15 ] & 0xFF ) << 8 );
 	  pos += 17 + itemsLength;
 	  List< Integer > tags = new ArrayList< Integer >();
 	  while ( true )
 	  {
 	    int tag = buffer[ pos++ ] & 0xFF;
 	    if ( ( tag & 0x80 ) == 0 )
 	    {
 	      tags.add( 0, tag );
 	      pos += ( buffer[ pos ] & 0xFF ) + 1;
 	    }
 	    else
 	    {
 	      int last = tags.remove( 0 );
 	      if ( tag != ( last | 0x80  ) )
 	      {
 	        System.err.println( "XCF file nesting error at " + Integer.toHexString( pos - 1 ) );
 	        break;
 	      }
 	      if ( tags.isEmpty() )
 	      {
 	        break;
 	      }
 	    }  
 	  }
 	  return pos;
   }
 	
 	private boolean ssdInOK()
 	{
 	  boolean res = ssdIn[ 0 ] == 1;
 	  for ( int i = 1; i < 62; i++ )
 	  {
 	    res &= ssdIn[ i ] == 0;
 	  }
 	  if ( !res )
 	  {
 	    System.err.println( "Input packet failure: " + ssdIn[ 0 ] + "" + ssdIn[ 1 ] + "" + ssdIn[ 2 ] + "" + ssdIn[ 3 ] + "" + ssdIn[ 4 ] + "" + ssdIn[ 5 ]);
 	  }
 	  return res;
 	}
 	
 	int writeTouch( byte[] buffer )
 	{
	  int status = ( buffer[ 0 ] & 0xFF ) | ( ( buffer[ 1 ] & 0x0F ) << 8 );
	  int dataEnd = ( buffer[ 2 ] & 0xFF ) | ( ( buffer[ 3 ] & 0xFF ) << 8 ) | ( ( buffer[ 1 ] & 0xF0 ) << 12 );
     int pos = 4;
     int index = -1;
 	  while ( pos < dataEnd )
     {
       while ( index < 16 && ( status & ( 1 << ++index ) ) == 0 ) {};
       if ( index == 16 )
       {
         break;
       }
       String name = Remote.userFilenames[ index ];
       System.err.println( "Sending file " + name );
       int count = 0;
       int end = name.endsWith( ".xcf" ) ? getEndBXML( pos, buffer ) : dataEnd;
       System.err.println( "File start: " + Integer.toHexString( pos ) + ", end: " + Integer.toHexString( end ) );
       int len = end - pos;
       Arrays.fill( ssdOut, ( byte )0 );
       ssdOut[ 0 ] = 0x13;
       ssdOut[ 2 ] = ( byte )( len & 0xFF );
       ssdOut[ 3 ] = ( byte )( ( len >> 8 ) & 0xFF );
       ssdOut[ 6 ] = ( byte )name.length();
       for ( int i = 0; i < name.length(); i++ )
       {
         ssdOut[ 7 + i ] = ( byte )name.charAt( i );
       }
       writeTouchUSBReport( ssdOut, 62 );
       System.err.println( "Header packet sent" );
       readTouchUSBReport(ssdIn);
 
       while ( pos < end )
       {
         if ( !ssdInOK() )
         {
           System.err.println( "Error: terminating at position " + Integer.toHexString( pos ) );
           return pos;
         }
         int size = Math.min( end - pos, 56 );
         Arrays.fill( ssdOut, ( byte )0 );
         ssdOut[ 0 ] = 0x14;
         ssdOut[ 2 ] = ( byte )( count & 0xFF );
         ssdOut[ 3 ] = ( byte )( ( count >> 8 ) & 0xFF );
         ssdOut[ 4 ] = ( byte )size;
         System.arraycopy( buffer, pos, ssdOut, 6, size );
         pos += size;
         writeTouchUSBReport( ssdOut, 62 );
         count++;
         System.err.println( "Packet " + count + " sent" );
         readTouchUSBReport(ssdIn);
       }
     }
 	  return buffer.length;
 	}
 	
 	public void writeSystemFile( File file )
 	{
 	  String name = file.getName();
 	  byte[] data = RemoteMaster.readBinary( file );
 	  if ( data == null )
 	  {
 	    System.err.println( "Write System File aborting.  Unable to read data file" );
 	    return;
 	  }
 
 	  int len = data.length;
 	  int pos = 0;
 	  int count = 0;
 	  Arrays.fill( ssdOut, ( byte )0 );
 	  ssdOut[ 0 ] = 0x13;
 	  ssdOut[ 2 ] = ( byte )( len & 0xFF );
 	  ssdOut[ 3 ] = ( byte )( ( len >> 8 ) & 0xFF );
 	  ssdOut[ 4 ] = ( byte )( ( len >> 16 ) & 0xFF );
 	  ssdOut[ 6 ] = ( byte )name.length();
 	  for ( int i = 0; i < name.length(); i++ )
 	  {
 	    ssdOut[ 7 + i ] = ( byte )name.charAt( i );
 	  }
 	  writeTouchUSBReport( ssdOut, 62 );
 	  System.err.println( "Header packet sent" );
 	  readTouchUSBReport(ssdIn);
 
 	  while ( pos < len )
 	  {
 	    if ( !ssdInOK() )
 	    {
 	      System.err.println( "Error: terminating at position " + Integer.toHexString( pos ) );
 	      return;
 	    }
 	    int size = Math.min( len - pos, 56 );
 	    Arrays.fill( ssdOut, ( byte )0 );
 	    ssdOut[ 0 ] = 0x14;
 	    ssdOut[ 2 ] = ( byte )( count & 0xFF );
 	    ssdOut[ 3 ] = ( byte )( ( count >> 8 ) & 0xFF );
 	    ssdOut[ 4 ] = ( byte )size;
 	    System.arraycopy( data, pos, ssdOut, 6, size );
 	    pos += size;
 	    writeTouchUSBReport( ssdOut, 62 );
 	    count++;
 	    System.err.println( "Packet " + count + " sent" );
 	    readTouchUSBReport(ssdIn);
 	  }
 	  System.err.println( "Bytes written to " + name + ": " + pos );
 	}
 	
 	int readTouch( byte[] buffer )
 	{
 	  int status = 0;
 	  byte[] o = new byte[2];
     o[0]=1;
     writeTouchUSBReport( new byte[]{4}, 1 );
     if ( readTouchUSBReport( ssdIn ) < 0 )
     {
       return 0;
     }
     firmwareFileCount = ssdIn[ 3 ];
     System.err.println( "Firmware file version data:" );
     for ( int i = 0; i < firmwareFileCount; i++ )
     {
       if ( readTouchUSBReport( ssdIn ) < 0 )
       {
         return 0;
       }
       saveVersionData();
       o[1] = ssdIn[ 1 ];
       writeTouchUSBReport( o, 2 );
     }
     System.err.println( "User file length data:" );
     for ( String name : Remote.userFilenames )
     {
       Arrays.fill( ssdOut, ( byte )0 );
       ssdOut[ 0 ] = 0x19;
       ssdOut[ 2 ] = ( byte )name.length();
       for ( int i = 0; i < name.length(); i++ )
       {
         ssdOut[ 4 + i ] = ( byte )name.charAt( i );
       }
       writeTouchUSBReport( ssdOut, 62 );
       if ( readTouchUSBReport( ssdIn ) < 0 )
       {
         return 0;
       }
       Hex hex = new Hex( 8 );
       for ( int i = 0; i < 8; i++ )
       {
         hex.set( ( short )ssdIn[ i ], i );
       }
       System.err.println( "  " + name + " : " + hex );
     }
     int ndx = 4;
     for ( int n = 0; n < Remote.userFilenames.length; n++ )
     {
       String name = Remote.userFilenames[ n ];
       Arrays.fill( ssdOut, ( byte )0 );
       ssdOut[ 0 ] = 0x12;
       ssdOut[ 2 ] = ( byte )name.length();
       for ( int i = 0; i < name.length(); i++ )
       {
         ssdOut[ 3 + i ] = ( byte )name.charAt( i );
       }
       writeTouchUSBReport( ssdOut, 62 );
       if ( readTouchUSBReport( ssdIn ) < 0 )
       {
         return 0;
       }
       if ( ( ssdIn[ 2 ] & 0x10 ) == 0x10 )
       {
         System.err.println( "File " + name + " is absent" );
         continue;
       }
       int count = ( ssdIn[ 3 ] & 0xFF ) + 0x100 * ( ssdIn[ 4 ] & 0xFF )+ 0x10000 * ( ssdIn[ 5 ] & 0xFF );
       int total = 0;
       ssdOut[ 0 ] = 1;
       ssdOut[ 2 ] = 0;
       status |= 1 << n;
       while ( total < count )
       {
         if ( readTouchUSBReport( ssdIn ) < 0 )
         {
           return ndx;
         }
         int len = ssdIn[ 4 ];
         total += len;
         System.arraycopy( ssdIn, 6, buffer, ndx, len );
         ndx += len;
         ssdOut[ 1 ] = ssdIn[ 1 ];
         writeTouchUSBReport( ssdOut, 62 );
       }
       System.err.println( "File " + name + " has reported length " + count + ", actual length " + total );
       System.err.println( "  Start = " + Integer.toHexString( ndx - total ) + ", end = " + Integer.toHexString( ndx - 1 ) );
     }
     Arrays.fill( buffer, ndx, buffer.length, ( byte )0xFF );
     buffer[ 0 ] = ( byte )( status & 0xFF );
     buffer[ 1 ] = ( byte )( ( ( status >> 8 ) & 0x0F ) | ( ( ndx >> 12 ) & 0xF0 ) );
     buffer[ 2 ] = ( byte )( ndx & 0xFF );
     buffer[ 3 ] = ( byte )( ( ndx >> 8 ) & 0xFF );
     
     if ( RemoteMaster.getSystemFiles() )
     {
       readSystemFiles();
     }
 
     // Need to return the buffer length rather than bytesRead for
     // consistency with normal remotes, which do read the entire buffer
     return buffer.length;
 	}
 	
 	private void readSystemFiles()
 	{
 	  for ( String name : firmwareFileVersions.keySet() )
     {
       if ( name.indexOf( "." ) > 0 )
       try
       {
         OutputStream output = null;
         File outputDir = new File( RemoteMaster.getWorkDir(), "XSight" );
         if ( !outputDir.exists() )
         {
           outputDir.mkdirs();
         }
         try 
         {
           output = new BufferedOutputStream(new FileOutputStream( new File( outputDir, name  ), false ) );
           Arrays.fill( ssdOut, ( byte )0 );
           ssdOut[ 0 ] = 0x12;
           ssdOut[ 2 ] = ( byte )name.length();
           for ( int i = 0; i < name.length(); i++ )
           {
             ssdOut[ 3 + i ] = ( byte )name.charAt( i );
           }
           writeTouchUSBReport( ssdOut, 62 );
           if ( readTouchUSBReport( ssdIn ) < 0 )
           {
             System.err.println( "Unable to read system file " + name );
             return;
           }
           int count = ( ssdIn[ 3 ] & 0xFF ) + 0x100 * ( ssdIn[ 4 ] & 0xFF ) + 0x10000 * ( ssdIn[ 5 ] & 0xFF );
           int total = 0;
           ssdOut[ 0 ] = 1;
           ssdOut[ 2 ] = 0;
           while ( total < count )
           {
             if ( readTouchUSBReport( ssdIn ) < 0 )
             {
               break;
             }
             int len = ssdIn[ 4 ];
             total += len;
             output.write( ssdIn, 6, len );
             ssdOut[ 1 ] = ssdIn[ 1 ];
             writeTouchUSBReport( ssdOut, 62 );
           }
           System.err.println( "File " + name + " has reported length " + count + ", actual length " + total );
         }
         catch(FileNotFoundException ex){
           System.err.println( "Unable to open file " + name );
         }
         finally
         {
           if ( output != null )
           {
             output.close();
           }
         }
       }
       catch(IOException ex){
         ex.printStackTrace( System.err );
       }
     }
   }
 
   void saveVersionData()
 	{
 	  Hex hex = new Hex( 12 );
 	  for ( int i = 0; i < 12; i++ )
 	  {
 	    hex.set( ( short )ssdIn[ i ], i );
 	  }
 	  StringBuilder sb = new StringBuilder();
 	  for ( int i = 12; i < ssdIn.length && ssdIn[ i ] != 0 ; i++ )
 	  {
 	      sb.append( (char)ssdIn[ i ] );
 	  }
 	  String name = sb.toString();
 	  firmwareFileVersions.put( name, hex );
 	  System.err.println( "  " + name + " : " + hex.toString() );
 	}
 	
 	public int writeRemote( int address, byte[] buffer, int length ) {  //if Touch, must be 62 bytes or less
 		int bytesWritten = -1;
 		if ( interfaceType == 0x106 )
 			bytesWritten = writeMAXQ_Lite(address, buffer, length);
 		else if ( interfaceType == 0x201 )
 		  bytesWritten = writeTouch( buffer );
 		return bytesWritten;
 	}
 	
 	int readTouchUSBReport(byte[] buffer) { 
 	  int bytesRead = -1;
 		try {
 		  Arrays.fill( inReport, ( byte )0xFF );
 		  bytesRead = devHID.readTimeout(inReport, 3000);
 		  if ( inReport[ 0 ] == ( byte )0xFF )
 		  {
 		    return -2;  // signifies timed out as 0xFF is not a known packet type
 		  }
 			System.arraycopy(inReport,0, buffer, 0, 62);
 		} catch (Exception e) {
 			return -1;    // signifies error
 		}
 		return bytesRead;
 	}
 	
 	int writeTouchUSBReport( byte[] buffer, int length ) {  //buffer must be <=62 bytes
 		System.arraycopy(buffer,0, outReport, 1, length);  //outReport[0] is index byte
 		if (length <= 62) 
 			Arrays.fill(outReport, length + 1, 63, (byte)0);  
 		else
 			return -1;
 		int crc =  CalcCRCofReport(outReport);
 		int bytesWritten = -1;
 		outReport[0] = (byte)0;
 		outReport[63] = (byte) (crc & 0xFF);
 		outReport[64] = (byte) (crc >> 8);
 		try {
 		  bytesWritten = devHID.write(outReport);
 		} catch (Exception e) {
 			return -1;
 		}
 		return bytesWritten;
 	}
 	
 	int CalcCRC(byte[] inBuf, int start, int end) {
   	  int poly = 0x8408; //0x1021 reversed
       int crc, i, j, byteVal;
           crc = 0xFFFF;
           for (i = start; i <= end; i++) {  // skip index byte
             byteVal = inBuf[i] & 0xFF; //bytes are always signed in Java;
               crc = crc ^ byteVal;
               for (j = 0; j < 8; j++) {
                   if ((crc & 1) == 1) 
                       crc = (crc >> 1) ^ poly;
                   else
                       crc = crc >> 1;
               }
           }
           return crc;
 	}
  
   int CalcCRCofReport(byte[] inBuf) {
     return CalcCRC(inBuf, 1, 62);
   }
 	
     /**
      * Instantiates a new CommHID.
      * 
      * @throws UnsatisfiedLinkError
      *           the unsatisfied link error
      */
     public CommHID() throws UnsatisfiedLinkError  {
       super( libraryName );  
     }
 
     /**
      * Instantiates a new CommHID.
      * 
      * @param folder
      *          the folder
      * @throws UnsatisfiedLinkError
      *           the unsatisfied link error
      */
     public CommHID( File folder ) throws UnsatisfiedLinkError  {
       super( folder, libraryName ); 
     }
     
     private final static String libraryName = "hidapi";
    
 }
 
 	
