 /*-
  * Copyright (c) 2007, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.ui;
 
 import net.rim.device.api.system.Application;
 import net.rim.device.api.system.DeviceInfo;
 import net.rim.device.api.system.RadioInfo;
 import net.rim.device.api.system.RadioStatusListener;
 import net.rim.device.api.system.SystemListener;
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.DrawStyle;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Graphics;
 
 /**
  * General purpose header field for application screens.
  * Based on the sample code provided here:
  * http://www.northcubed.com/site/?p=15
  */
 public class HeaderField extends Field {
     private Font headerFont;
     private String title;
     private boolean showSignal;
     private boolean showBattery;
     private boolean showTitle;
     private int fieldWidth;
     private int fieldHeight;
     private int fontColor;
     private int backgroundColor;
     private int batteryBackground;
     private int signalBarColor;
     private SystemListener systemListener;
     private RadioStatusListener radioStatusListener;
     private boolean listenersActive;
     private int signalLevel;
     private int batteryLevel;
     
     public HeaderField(String title) {
         super(Field.NON_FOCUSABLE);
         this.title = title;
         this.showSignal = true;
         this.showBattery = true;
         this.showTitle = true;
         this.fontColor = -1;
         this.headerFont = Font.getDefault().derive(Font.BOLD);
         this.backgroundColor = 0;
         this.batteryBackground = 0x999999;
         this.signalBarColor = Color.BLUE;
         this.fieldHeight = headerFont.getHeight();
         this.fieldWidth = Graphics.getScreenWidth();
         signalLevel = RadioInfo.getSignalLevel();
         batteryLevel = DeviceInfo.getBatteryLevel();
         
         this.listenersActive = false;
         
         this.systemListener = new SystemListener() {
             public void powerOff() {
             }
             public void powerUp() {
             }
             public void batteryLow() {
                 onBatteryStatusChanged();
             }
             public void batteryGood() {
                 onBatteryStatusChanged();
             }
             public void batteryStatusChange(int status) {
                 onBatteryStatusChanged();
             }
         };
         this.radioStatusListener = new RadioStatusListener() {
             public void signalLevel(int level) {
                 onRadioStatusChanged();
             }
             public void networkStarted(int networkId, int service) {
                 onRadioStatusChanged();
             }
             public void baseStationChange() {
                 onRadioStatusChanged();
             }
             public void radioTurnedOff() {
                 onRadioStatusChanged();
             }
             public void pdpStateChange(int apn, int state, int cause) {
                 onRadioStatusChanged();
             }
             public void networkStateChange(int state) {
                 onRadioStatusChanged();
             }
             public void networkScanComplete(boolean success) {
                 onRadioStatusChanged();
             }
             public void mobilityManagementEvent(int eventCode, int cause) {
                 onRadioStatusChanged();
             }
             public void networkServiceChange(int networkId, int service) {
                 onRadioStatusChanged();
             }
         };
     }
     
     protected void onBatteryStatusChanged() {
         batteryLevel = DeviceInfo.getBatteryLevel();
         invalidate();
     }
     
     protected void onRadioStatusChanged() {
         signalLevel = RadioInfo.getSignalLevel();
         invalidate();
     }
     
     protected void onDisplay() {
         if(!listenersActive) {
             Application.getApplication().addSystemListener(systemListener);
             Application.getApplication().addRadioListener(radioStatusListener);
         }
         super.onExposed();
     }
     
     protected void onExposed() {
         if(!listenersActive) {
             Application.getApplication().addSystemListener(systemListener);
             Application.getApplication().addRadioListener(radioStatusListener);
         }
         super.onExposed();
     }
     
     protected void onObscured() {
         if(listenersActive) {
             Application.getApplication().removeSystemListener(systemListener);
             Application.getApplication().removeRadioListener(radioStatusListener);
         }
         super.onObscured();
     }
     
     protected void onUndisplay() {
         if(listenersActive) {
             Application.getApplication().removeSystemListener(systemListener);
             Application.getApplication().removeRadioListener(radioStatusListener);
         }
         super.onUndisplay();
     }

    /**
     * Remove any global event listeners.  Intended to be called on shutdown,
     * where the active screen may not get popped off the stack prior to
     * System.exit() being called.
     */
    public void removeListeners() {
        if(listenersActive) {
            Application.getApplication().removeSystemListener(systemListener);
            Application.getApplication().removeRadioListener(radioStatusListener);
        }
    }
     
     public void setTitle(String title) {
         this.title = title;
         invalidate();
     }
     
     public void setFontColor(int fontColor) {
         this.fontColor = fontColor;
         invalidate();
     }
     
     public void setBatteryBackground(int batteryBackground) {
         this.batteryBackground = batteryBackground;
         invalidate();
     }
     
     public void setBackgroundColor(int backgroundColor) {
         this.backgroundColor = backgroundColor;
         invalidate();
     }
     
     public void showSignal(boolean bool) {
         showSignal = bool;
         invalidate();
     }
     
     public void showBattery(boolean bool) {
         showBattery = bool;
         invalidate();
     }
     
     public void showTitle(boolean bool) {
         showTitle = bool;
         invalidate();
     }
     
     protected void layout(int width, int height) {
         setExtent(getPreferredWidth(), getPreferredHeight());
     }
     
     public int getPreferredWidth() {
         return fieldWidth;
     }
     
     public int getPreferredHeight() {
         return fieldHeight;
     }
     
     protected void paint(Graphics graphics) {
         if(fontColor == -1) {
             fontColor = graphics.getColor();
         }
 
         graphics.setFont(headerFont);
         int graphicsDiff = 0;
         int preferredWidth = this.getPreferredWidth();
         int preferredHeight = this.getPreferredHeight();
         int midPoint = preferredHeight / 2;
         
         if(backgroundColor != 0) {
             graphics.setColor(backgroundColor);
             graphics.fillRect(0, 0, preferredWidth, preferredHeight);
         }
         
         if(showSignal) {
             graphicsDiff = graphicsDiff + 30;
             graphics.setColor(0x999999);
             graphics.fillRect(preferredWidth - 28, midPoint + 2, 6, 4);
             graphics.fillRect(preferredWidth - 23, midPoint, 6, 6);
             graphics.fillRect(preferredWidth - 18, midPoint - 2, 6, 8);
             graphics.fillRect(preferredWidth - 13, midPoint - 4, 6, 10);
             graphics.fillRect(preferredWidth - 8, midPoint - 6, 6, 12);
 
             graphics.setColor(signalBarColor);
             if(signalLevel >= -120) {
                 //1 band
                 graphics.fillRect(preferredWidth - 27, midPoint + 3, 4, 2);
             }
             if(signalLevel >= -101) {
                 //2 bands
                 graphics.fillRect(preferredWidth - 22, midPoint + 1, 4, 4);
             }
             if(signalLevel >= -92) {
                 //3 bands
                 graphics.fillRect(preferredWidth - 17, midPoint - 1, 4, 6);
             }
             if(signalLevel >= -86) {
                 //4 bands
                 graphics.fillRect(preferredWidth - 12, midPoint - 3, 4, 8);
             }
             if(signalLevel >= -77) {
                 //5 bands
                 graphics.fillRect(preferredWidth - 7, midPoint - 5, 4, 10);
             }
         }
         
         if(showBattery) {
             graphics.setColor(batteryBackground);
             graphics.fillRect(preferredWidth - 23 - graphicsDiff, midPoint - 4, 20, 8);
             graphics.fillRect(preferredWidth - 3 - graphicsDiff, midPoint - 2, 1, 4);
             if(batteryLevel > 75) {
                 graphics.setColor(0x28f300);
             }
             else if(batteryLevel > 50) {
                 graphics.setColor(0x91dc00);
             }
             else if(batteryLevel > 25) {
                 graphics.setColor(0xefec00);
             }
             else {
                 graphics.setColor(0xff2200);
             }
             double powerLong = ((18.00/100) * batteryLevel);
             int power = (int)powerLong;
             graphics.fillRect(preferredWidth - 22 - graphicsDiff, midPoint - 3, power, 6);
             graphicsDiff = graphicsDiff + 24;
         }
         
         graphics.setColor(fontColor);
         
         if(showTitle) {
             int limit = 2;
             if(showSignal) {
                 limit += 28;
             }
             if(showBattery) {
                 limit += 25;
             }
             graphics.drawText(title, 1, 0, DrawStyle.ELLIPSIS, preferredWidth - limit);
         }
     }
 }
