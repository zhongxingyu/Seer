 /***
   Copyright (c) 2013 CommonsWare, LLC
   
   Licensed under the Apache License, Version 2.0 (the "License"); you may
   not use this file except in compliance with the License. You may obtain
   a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
  */
 
 package com.commonsware.cwac.camera;
 
 import android.os.Build;
 
 public class DeviceProfile {
   private static volatile DeviceProfile SINGLETON=null;
 
   synchronized public static DeviceProfile getInstance() {
     if (SINGLETON == null) {
      if ("samsung".equalsIgnoreCase(Build.MANUFACTURER)) {
         SINGLETON=new SamsungDeviceProfile();
       }
       else if ("motorola".equalsIgnoreCase(Build.MANUFACTURER)) {
         SINGLETON=new MotorolaDeviceProfile();
       }
       else {
         SINGLETON=new DeviceProfile();
       }
     }
 
     return(SINGLETON);
   }
 
   private DeviceProfile() {
   }
 
   public boolean useTextureView() {
     return(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
   }
 
   public boolean encodesRotationToExif() {
     return(false);
   }
 
  public boolean rotateBasedOnExif() {
    return(false);
  }

  private static class FullExifFixupDeviceProfile extends DeviceProfile {
     @Override
     public boolean encodesRotationToExif() {
       return(true);
     }
 
     @Override
    public boolean rotateBasedOnExif() {
       return(true);
     }
   }

  private static class SamsungDeviceProfile extends
      FullExifFixupDeviceProfile {
  }

  private static class MotorolaDeviceProfile extends
      FullExifFixupDeviceProfile {
  }
 }
