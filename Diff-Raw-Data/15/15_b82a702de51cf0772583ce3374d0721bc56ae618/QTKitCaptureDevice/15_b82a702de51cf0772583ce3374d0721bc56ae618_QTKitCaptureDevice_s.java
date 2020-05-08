 //
 //  QTKitCaptureDevice.java
 //  QTCubed
 //
 //  Created by Chappell Charles on 10/02/19.
 //  Copyright (c) 2010 MC Cubed, Inc. All rights reserved.
 //
 //  This program is free software; you can redistribute it and/or modify
 //  it under the terms of the GNU General Public License as published by
 //  the Free Software Foundation; either version 2 of the License, or
 //  (at your option) any later version.
 //  
 //  This program is distributed in the hope that it will be useful,
 //  but WITHOUT ANY WARRANTY; without even the implied warranty of
 //  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //  GNU General Public License for more details.
 //  
 //  You should have received a copy of the GNU General Public License along
 //  with this program; if not, write to the Free Software Foundation, Inc.,
 //  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 //  
 //  
 //  This is also dual licensed as proprietary software, and may be used in
 //  commercial/proprietary software products by obtaining a license from:
 //  MC Cubed, Inc
 //  1-3-4 Kamikizaki, Urawa-ku
 //  Saitama, Saitama, 330-0071
 //  Japan
 package net.mc_cubed.qtcubed;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 public class QTKitCaptureDevice {
 
     public static QTKitCaptureDevice defaultInputDevice(QTKitMediaType qtKitMediaType) {
         long deviceRef = _defaultInputDevice(qtKitMediaType.ordinal());
         return new QTKitCaptureDevice(deviceRef);
     }
 
     native private static long _defaultInputDevice(int ordinal);
     protected final long deviceRef;
 
     protected QTKitCaptureDevice(long newDeviceRef) {
         this.deviceRef = newDeviceRef;
     }

     @Override
     protected void finalize() {
         _release(deviceRef);
     }
 
     protected native void _release(long deviceRef);
 
     static public Collection<QTKitCaptureDevice> inputDevices() {
         Collection<QTKitCaptureDevice> retval = new LinkedList<QTKitCaptureDevice>();
 
         for (long captureDeviceRef : _inputDevices()) {
             retval.add(new QTKitCaptureDevice(captureDeviceRef));
         }
 
         return retval;
     }
 
     native static protected long[] _inputDevices();
 
     public void open() {
         _open(deviceRef);
     }
 
     protected native void _open(long deviceRef);
 
     public boolean isOpen() {
         return _isOpen(deviceRef);
     }
 
     protected native boolean _isOpen(long deviceRef);
 
     public void close() {
         _close(deviceRef);
     }
 
     protected native void _close(long deviceRef);
 
     public String localizedDisplayName() {
         return _localizedDisplayName(deviceRef);
     }
 
     protected native String _localizedDisplayName(long deviceRef);
 
     @Override
     public String toString() {
         return localizedDisplayName();
     }
 
     public Collection<QTKitFormatDescription> getFormatDescriptions() {
         List<QTKitFormatDescription> retval = new LinkedList<QTKitFormatDescription>();
         for (QTKitFormatDescription description : _getFormatDescriptions(deviceRef)) {
             retval.add(description);
         }
 
         return retval;
     }
 
     private native QTKitFormatDescription[] _getFormatDescriptions(long captureDeviceRef);
 
     public String uniqueId() {
         return _uniqueId(deviceRef);
     }
 
     private native String _uniqueId(long captureDeviceRef);
 
     public String modelUniqueId() {
         return _modelUniqueId(deviceRef);
     }
 
     private native String _modelUniqueId(long captureDeviceRef);
 
     public boolean hasMediaType(QTKitMediaType type) {
         return _hasMediaType(deviceRef,type.ordinal());
     }
 
     private native boolean _hasMediaType(long captureDeviceRef,int typeOrdinal);
 
     public Properties deviceAttributes() {
         return _deviceAttributes(deviceRef);
     }
 
     private native Properties _deviceAttributes(long captureDeviceRef);
 
     public static Collection<QTKitCaptureDevice> inputDevicesWithMediaType(QTKitMediaType type) {
         throw new java.lang.UnsupportedOperationException("Not implemented yet");
     }
 
     public static QTKitCaptureDevice deviceWithUniqueId(String id) {
         long deviceId = _deviceWithUniqueId(id);
         if (deviceId == 0) {
             return null;
         }
 
         return new QTKitCaptureDevice(deviceId);
     }
 
     static native private long _deviceWithUniqueId(String id);
 }
