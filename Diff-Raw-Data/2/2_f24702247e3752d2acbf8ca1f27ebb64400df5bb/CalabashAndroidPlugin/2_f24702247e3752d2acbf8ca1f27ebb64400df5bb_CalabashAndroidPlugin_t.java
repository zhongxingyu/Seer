 // Copyright (c) 2012, Daniel Andersen (dani_ande@yahoo.dk)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // 1. Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 // 3. The name of the author may not be used to endorse or promote products derived
 //    from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 // ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package com.trollsahead.qcumberless.plugins.calabash;
 
 import com.trollsahead.qcumberless.device.Device;
 import com.trollsahead.qcumberless.device.calabash.CalabashAndroidDevice;
 import com.trollsahead.qcumberless.device.calabash.CalabashAndroidDeviceImportStepDefinitions;
 import com.trollsahead.qcumberless.engine.FlashingMessageManager;
 import com.trollsahead.qcumberless.gui.ProgressBar;
 import com.trollsahead.qcumberless.model.StepDefinition;
 import com.trollsahead.qcumberless.plugins.ButtonBarMethodCallback;
 import com.trollsahead.qcumberless.plugins.ElementMethodCallback;
 import com.trollsahead.qcumberless.plugins.Plugin;
 import com.trollsahead.qcumberless.util.SimpleRubyStepDefinitionParser;
 
 import java.net.URL;
 import java.util.*;
 import java.util.List;
 
 public class CalabashAndroidPlugin implements Plugin {
     private CalabashAndroidDevice calabashAndroidDevice;
 
     public void initialize() {
         calabashAndroidDevice = new CalabashAndroidDevice();
     }
 
     public Set<Device> getDevices() {
         Set<Device> devices = new HashSet<Device>();
         devices.add(calabashAndroidDevice);
         return devices;
     }
 
    public Map<String, List<StepDefinition>> getStepDefinitions() {
         ProgressBar progressBar = new ProgressBar("Importing step definitions");
         FlashingMessageManager.addMessage(progressBar);
         try {
             final URL[] urls = new URL[] {
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/additions_manual_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/app_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/assert_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/check_box_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/context_menu_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/date_picker_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/enter_text_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/location_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/navigation_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/press_button_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/progress_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/rotation_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/screenshot_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/spinner_steps.rb"),
                     new URL("https://raw.github.com/calabash/calabash-android/master/ruby-gem/lib/calabash-android/steps/time_picker_steps.rb")
             };
             return SimpleRubyStepDefinitionParser.parseFiles(urls, progressBar);
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         } finally {
             FlashingMessageManager.removeMessage(progressBar);
         }
     }
 
     public List<ElementMethodCallback> getDefinedElementMethodsApplicableFor(int type) {
         return null;
     }
 
     public List<ButtonBarMethodCallback> getButtonBarMethods() {
         List<ButtonBarMethodCallback> list = new LinkedList<ButtonBarMethodCallback>();
         list.add(new CalabashAndroidDeviceImportStepDefinitions(this));
         return list;
     }
 }
