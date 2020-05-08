package com.buglabs.bt.ledbar;
 
 public interface BluetoothHud {
 
     /**
      * Initiate a bluetooth discovery, searching for a device by it's
      * FullName.
      *
      * @param fullName
      *  A search string - will attempt to connect to any device that
      *  has a name containing fullName.
      * @return
      *  Returns successful discovery of device
      *  If false, device was not found, and will not be connected
      *  Does not imply we are connected yet, although thread will
      *  attempt to connect immediately after setName() returns.
      */
     public boolean setName(String fullName);
 
     /**
      * Initiate a bluetooth discovery, searching for a device by it's
      * MAC Address
      *
      * @param macAddr
      *  A search string - will attempt to connect to any device that
      *  has a MAC address containing macAddr.
      *  This should not contain any colons.
      * @return
      *  Returns successful discovery of device
      *  If false, device was not found, and will not be connected
      *  Does not imply we are connected yet, although thread will
      *  attempt to connect immediately after setMac() returns.
      */
     public boolean setMac(String macAddr);
 
     /**
      * Queries the remote device, updating it's online status
      *
      * @return
      *  Returns true if the device responds to an RSSI request.
      */
     public boolean online();
 
     /**
      * Immediately set an LED channel to a given intensity
      *
      * @param chan
      *  The LED channel to set (currently 0-4 are supported)
      * @param value
      *  The intensity to set, given as a double between 0.0 and 1.0
      * @return
      *  Returns true if device was connected
      *  No guarantee of reception.
      */
     public boolean set(int chan, double value);
 
     /**
      * Immediately set all LED channels to a given intensity
      *
      * @param value
      *  The intensity to set, given as a double between 0.0 and 1.0
      * @return
      *  Returns true if device was connected
      *  No guarantee of reception.
      */
     public boolean setAll(double value);
 
     /**
      * Will read the current battery level of the device
      *
      * @return
      *  Returns the raw voltage ADC value, ranging from 0-1023
      *  More testing is needed, but rawBatteryLevel()/158.7 is
      *  approximately the current battery voltage.
      */
     public int rawBatteryLevel();
 
     /**
      * Fade an LED channel to a given intensity
      * This will fade from the current channel intensity to
      * the specified destination intensity
      *
      * @param chan
      *  The LED channel to set (currently 0-4 are supported)
      * @param duration
      *  The fade duration, in ms
      * @param value
      *  The intensity to set, given as a double between 0.0 and 1.0
      * @return
      *  Returns true if device was connected
      *  No guarantee of reception.
      */
     public boolean fade(int chan, long duration, double value);
 
     /**
      * Disconnect the bluetooth device.
      * In order to re-establish a connection, setName or setMac
      * must be called.
      */
     public void disconnect();
 
     /**
      * Attempt to disconnect and quickly reconnect
      * NOTE - this is hacky and experimental!
      * @return
      *  Returns true if we successfully reconnected
      *  NOTE - this should always disconnect
      */
     public boolean quickReconnect();
 }
