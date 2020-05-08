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
     // Log.d("DeviceProfile", Build.PRODUCT);
 
     if (SINGLETON == null) {
       if ("occam".equals(Build.PRODUCT)) {
         SINGLETON=new Nexus4DeviceProfile();
       }
      else if ("m7".equals(Build.PRODUCT) && "HTC".equalsIgnoreCase(Build.MANUFACTURER)) {
        SINGLETON=new HtcOneDeviceProfile();
      }
       else if ("gd1wifiue".equals(Build.PRODUCT)) {
         SINGLETON=new SamsungGalaxyCameraDeviceProfile();
       }
       else if ("espressowifiue".equals(Build.PRODUCT)) {
         SINGLETON=new SamsungGalaxyTab2Profile();
       }
       else if ("samsung".equalsIgnoreCase(Build.MANUFACTURER)) {
         SINGLETON=new SamsungDeviceProfile();
       }
       else if ("motorola".equalsIgnoreCase(Build.MANUFACTURER)) {
         SINGLETON=new MotorolaDeviceProfile();
       }
       else if ("htc_vivow".equalsIgnoreCase(Build.PRODUCT)) {
         SINGLETON=new DroidIncredible2Profile();
       }
       else if ("C1505_1271-7585".equalsIgnoreCase(Build.PRODUCT)) {
         SINGLETON=new SonyXperiaEProfile();
       }
       else {
         SINGLETON=new DeviceProfile();
       }
     }
 
     return(SINGLETON);
   }
 
   public DeviceProfile() {
   }
 
   public boolean useTextureView() {
     return(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && !isCyanogenMod());
   }
 
   public boolean encodesRotationToExif() {
     return(false);
   }
 
   public boolean rotateBasedOnExif() {
     return(false);
   }
 
   public boolean portraitFFCFlipped() {
     return(false);
   }
 
   public int getMaxPictureHeight() {
     return(Integer.MAX_VALUE);
   }
 
   // based on http://stackoverflow.com/a/9801191/115145
   // and
   // https://github.com/commonsguy/cwac-camera/issues/43#issuecomment-23791446
 
   private boolean isCyanogenMod() {
     return(System.getProperty("os.version").contains("cyanogenmod") || Build.HOST.contains("cyanogenmod"));
   }
 
  private static class HtcOneDeviceProfile extends DeviceProfile {
    public int getMaxPictureHeight() {
      return(1400);
    }
  }

   private static class Nexus4DeviceProfile extends DeviceProfile {
     public int getMaxPictureHeight() {
       return(720);
     }
   }
 
   private static class SamsungGalaxyTab2Profile extends DeviceProfile {
     public int getMaxPictureHeight() {
       return(1104);
     }
   }
 
   public static class FullExifFixupDeviceProfile extends DeviceProfile {
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
 
   private static class SamsungGalaxyCameraDeviceProfile extends
       SamsungDeviceProfile {
     public int getMaxPictureHeight() {
       return(3072);
     }
   }
 
   private static class MotorolaDeviceProfile extends
       FullExifFixupDeviceProfile {
   }
 
   private static class DroidIncredible2Profile extends DeviceProfile {
     public boolean portraitFFCFlipped() {
       return(true);
     }
   }
 
   private static class SonyXperiaEProfile extends
       FullExifFixupDeviceProfile {
   }
 }
