 /*-------------------------------------------------------------------------
 |   A wrapper to convert RXTX into Linux Java Comm
 |   Copyright 1998 Kevin Hester, kevinh@acm.org
 |   Copyright 2000 Trent Jarvi, trentjarvi@yahoo.com
 |
 |   This library is free software; you can redistribute it and/or
 |   modify it under the terms of the GNU Library General Public
 |   License as published by the Free Software Foundation; either
 |   version 2 of the License, or (at your option) any later version.
 |
 |   This library is distributed in the hope that it will be useful,
 |   but WITHOUT ANY WARRANTY; without even the implied warranty of
 |   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 |   Library General Public License for more details.
 |
 |   You should have received a copy of the GNU Library General Public
 |   License along with this library; if not, write to the Free
 |   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 --------------------------------------------------------------------------*/
 
 /* Martin Pool <mbp@linuxcare.com> added support for explicitly-specified
  * lists of ports, October 2000. */
 
 package gnu.io;
 
 import java.io.*;
 import java.util.*;
 import javax.comm.*;
 import java.util.StringTokenizer;
 
 /**
    This is the JavaComm for Linux driver.
 */
 public class RXTXCommDriver implements CommDriver
 {
 	private static boolean debug = false;
 	static
 	{
 		System.loadLibrary( "Serial" );
 	}
 
 	/** Get the Serial port prefixes for the running OS */
 	private native boolean isDeviceGood(String dev);
 
 
 	private final String[] getPortPrefixes(String AllKnownPorts[]) {
 		/*
 		256 is the number of prefixes ( COM, cua, ttyS, ...) not
 		the number of devices (ttyS0, ttyS1, ttyS2, ...)
 
 		On a Linux system there are about 400 prefixes in /dev.  
 		registerScannedPorts() assigns AllKnownPorts to something less
 		than 50 prefixes.
 
 		rxtx-1.5 uses Vectors to avoid this mess.
 
 		Trent
 		*/
 
 		String PortString[]=new String [256];
 		if(AllKnownPorts==null)
 		{
 			if (debug)
 				System.out.println("\nRXTXCommPort:getPortPrefixes() No ports are known for this System.\nPlease check the ports listed for " +osName+" in RXTXCommDriver:registerScannedPorts()\n");
 		}
 		int i=0;
 		for(int j=0;j<AllKnownPorts.length;j++){
 			if(isDeviceGood(AllKnownPorts[j])) {
 				PortString[i++]=new String(AllKnownPorts[j]);
 			}
 		}
 		String[] returnArray=new String[i];
 		System.arraycopy(PortString, 0, returnArray, 0, i);
 		if(PortString[0]==null)
 		{
 			if (debug)
 				System.out.println("\nRXTXCommPort:getPortPrefixes() No ports matched the list assumed for this\nSystem in the directory /dev.  Please check the ports listed for \"" +osName+"\" in\nRXTXCommDriver:registerScannedPorts()\n");
 		}
 		else
 		{
 			if (debug)
 				System.out.println("\nRXTXCommPort:getPortPrefixes()\nThe following port prefixes have been identified as valid on "+osName+":\n");
 			for(int j=0;j<returnArray.length;j++)
 			{
 				if (debug)
 					System.out.println(j +" "+ PortString[j]);
 			}
 		}
 		return returnArray;
 	}
 
 
    /*
     * A primitive test of whether a specified device is available or
     * not.
     *
     * FIXME: This is actually a pretty poor test.  These Java calls
     * map in the 1.2.2 Unix JDK into calls to access(2), which checks
     * the permission bits on the file.  There are at least two
     * problems with this, however.
     *
     * Firstly, in Linux 2.2 it will fail with EROFS for a device node
     * on a read-only device, even though you're allowed to write to
     * them.  This is fixed in Linux 2.4, but may exist on other
     * systems as well.
     *
     * Secondly and more importantly, simply being permittedq to access
     * the device doesn't mean that it physically exists, and therefore
     * that an attempt to use it will succeed.
     *
     * One alternative approach would be to try to actually open the
     * device for read/write, but that's not a good idea because
     * opening a device can affect the control pins, etc.
     *
     * Even better might be to return all possible devices for this
     * operating system or hardware without trying to check that
     * they're usable, and then letting the application cope with ones
     * that actually turn out not to work.
     *
     * @author Martin Pool
     */
 	private boolean accessReadWrite(String portName)
 	{
 		final File port = new File(portName);
 		return port.canRead() && port.canWrite();
 	}
 
 
 	private void RegisterValidPorts(
 		String devs[],
 		String Prefix[],
 		int PortType
 	) {
 		int p =0 ;
 		int i =0;
 		if (debug)
 			System.out.println("Entering RegisterValidPorts()");
 		if ( devs[0]==null || Prefix[0]==null) return;
 		for( i = 0;i<devs.length; i++ ) {
 			for( p = 0;p<Prefix.length; p++ ) {
 				String portName = new String("/dev/" + devs[ i ]);
 				if( devs[ i ].startsWith( Prefix[ p ] ) ) {
 					if (accessReadWrite(portName))
 						CommPortIdentifier.addPortName(
 							portName,
 							PortType,
 							this
 						);
 				}
 			}
 		}
 		if (debug)
 			 System.out.println("Leaving RegisterValidPorts()");
 	}
 
 	String osName=System.getProperty("os.name");
 
    /*
     * initialize() will be called by the CommPortIdentifier's static
     * initializer. The responsibility of this method is:
     * 1) Ensure that that the hardware is present.
     * 2) Load any required native libraries.
     * 3) Register the port names with the CommPortIdentifier.
 	*
 	* <p>From the NullDriver.java CommAPI sample.
 	*
 	* added printerport stuff
 	* Holger Lehmann
 	* July 12, 1999
 	* IBM
 
 	* Added ttyM for Moxa boards
 	* Removed obsolete device cuaa
 	* Peter Bennett
 	* January 02, 2000
 	* Bencom
 
     */
 	public void initialize()
 	{
 	/*
 	 First try to register ports specified in the properties
 	 file.  If that doesn't exist, then scan for ports.
 	*/
 		if (!registerSpecifiedPorts())
 			registerScannedPorts();
    	}
 
 
 	private void addSpecifiedPorts(String names, int type)
 	{
 		final String pathSep = System.getProperty("path.separator", ":");
 		final StringTokenizer tok = new StringTokenizer(names, pathSep);
 
 		while (tok.hasMoreElements())
 		{
 			String portName = tok.nextToken();
 
 			if (accessReadWrite(portName))
 				CommPortIdentifier.addPortName(portName,
 					type, this);
 		}
 	}
 
 
    /*
     * Register ports specified by the gnu.io.rxtx.SerialPorts and
     * gnu.io.rxtx.ParallelPorts system properties.
     */
 	private boolean registerSpecifiedPorts()
 	{
 		boolean found = false;
 		String val;
 
 		val = System.getProperty("gnu.io.rxtx.SerialPorts");
 		if (val != null)
 		{
 			addSpecifiedPorts(val, CommPortIdentifier.PORT_SERIAL);
 			found = true;
 		}
 
 		val = System.getProperty("gnu.io.rxtx.ParallelPorts");
 		if (val != null)
 		{
 			addSpecifiedPorts(val, CommPortIdentifier.PORT_PARALLEL);
 			found = true;
 		}
 
 		return found;
 	}
 
 
    /*
     * Look for all entries in /dev, and if they look like they should
     * be serial ports on this OS and they can be opened then register
     * them.
     *
     */
 	private void registerScannedPorts()
 	{
 		File dev = new File( "/dev" );
 		String[] devs = dev.list();
 		String[] AllKnownSerialPorts;
 		if(osName.equals("Linux"))
 		{
 			String[] Temp = {
 			"comx",      // linux COMMX synchronous serial card
 			"holter",    // custom card for heart monitoring
 			"modem",     // linux symbolic link to modem.
 			"ttyircomm", // linux IrCommdevices (IrDA serial emu)
 			"ttycosa0c", // linux COSA/SRP synchronous serial card
 			"ttycosa1c", // linux COSA/SRP synchronous serial card
 			"ttyC", // linux cyclades cards
 			"ttyCH",// linux Chase Research AT/PCI-Fast serial card
 			"ttyD", // linux Digiboard serial card
 			"ttyE", // linux Stallion serial card
 			"ttyF", // linux Computone IntelliPort serial card
 			"ttyH", // linux Chase serial card
 			"ttyI", // linux virtual modems
 			"ttyL", // linux SDL RISCom serial card
 			"ttyM", // linux PAM Software's multimodem boards
 				// linux ISI serial card
 			"ttyMX",// linux Moxa Smart IO cards
 			"ttyP", // linux Hayes ESP serial card
 			"ttyR", // linux comtrol cards
 				// linux Specialix RIO serial card
 			"ttyS", // linux Serial Ports
 			"ttySI",// linux SmartIO serial card
 			"ttySR",// linux Specialix RIO serial card 257+
 			"ttyT", // linux Technology Concepts serial card
 			"ttyUSB",//linux USB serial converters
 			"ttyV", // linux Comtrol VS-1000 serial controller
 			"ttyW", // linux specialix cards
 			"ttyX", // linux SpecialX serial card
 			};
 			AllKnownSerialPorts=Temp;
 		}
 
		else if(osName.equals("irix")) // FIXME this is probably wrong
 		{
 			String[] Temp = {
 			"ttyc", // irix raw character devices
 			"ttyd", // irix basic serial ports
 			"ttyf", // irix serial ports with hardware flow
 			"ttym", // irix modems
 			"ttyq", // irix pseudo ttys
 			"tty4d",// irix RS422
 			"tty4f",// irix RS422 with HSKo/HSki
 			"midi", // irix serial midi
 			"us"    // irix mapped interface
 			};
 			AllKnownSerialPorts=Temp;
 		}
 
 		else if(osName.equals("FreeBSD")) //FIXME this is probably wrong
 		{
 			String[] Temp = {
 			"cuaa"  // FreeBSD Serial Ports
 			};
 			AllKnownSerialPorts=Temp;
 		}
 
 		else if(osName.equals("NetBSD")) // FIXME this is probably wrong
 		{
 			String[] Temp = {
 			"tty0"  // netbsd serial ports
 			};
 			AllKnownSerialPorts=Temp;
 		}
 
 		else if(osName.equals("HP-UX"))
 		{
 			String[] Temp = {
 			"tty0p",// HP-UX serial ports
 			"tty1p" // HP-UX serial ports
 			};
 			AllKnownSerialPorts=Temp;
 		}
 
 		else if(osName.equals("BeOS"))
 		{
 			String[] Temp = {
 			"serial" // BeOS serial ports
 			};
 			AllKnownSerialPorts=Temp;
 		}
 
 		else if(osName.equals("WIN32")) // FIXME this is probably wrong
 		{
 			String[] Temp = {
 			"COM"    // win32 serial ports
 			};
 			AllKnownSerialPorts=Temp;
 		}
 
 		else
 		{
 			if (debug)
 				System.out.println(osName + " ports have not been entered in RXTXCommDriver.java.  This may just be a typo in the method initialize().");
 			AllKnownSerialPorts=null;
 		}
 
 	/** Get the Parallel port prefixes for the running os
 	* Holger Lehmann
 	* July 12, 1999
 	* IBM
 	*/
 		String[] AllKnownParallelPorts;
 		if(osName.equals("Linux"))
 		{
 			String[] temp={
 				"lp"    // linux printer port
 			};
 			AllKnownParallelPorts=temp;
 		}
 		else  /* printer support is green */
 		{
 			AllKnownParallelPorts=null;
 		}
 
 		if (devs==null)
 		{
 			if (debug)
 				System.out.println("RXTXCommDriver:registerScannedPorts() no Device files to check ");
 			return;
 		}
 		RegisterValidPorts(
 			devs,
 			getPortPrefixes(AllKnownSerialPorts),
 			CommPortIdentifier.PORT_SERIAL
 		);
 		/*
 		if(AllKnownParallelPorts!=null)
 			RegisterValidPorts(
 				devs,
 				getPortPrefixes(AllKnownParallelPorts),
 				CommPortIdentifier.PORT_PARALLEL
 			);
 		else if (debug) 
 			System.out.println("Skipping Printer Ports");
 	*/
 	}
 
 
 	/*
 	 * getCommPort() will be called by CommPortIdentifier from its
 	 * openPort() method. portName is a string that was registered earlier
 	 * using the CommPortIdentifier.addPortName() method. getCommPort()
 	 * returns an object that extends either SerialPort or ParallelPort.
 	 *
 	 * <p>From the NullDriver.java CommAPI sample.
 	 */
 	public CommPort getCommPort( String portName, int portType )
 	{
 		try {
 			if (portType==CommPortIdentifier.PORT_SERIAL)
 			{
 				return new RXTXPort( portName );
 			}
 			else if (portType==CommPortIdentifier.PORT_PARALLEL)
 			{
 				return new LPRPort( portName );
 			}
 		} catch( PortInUseException e ) {
 			if (debug)
 				System.out.println(
 					"Port in use by another application");
 		}
 		return null;
 	}
 }
