 /*
  * The MIT License
  * 
  * Copyright (c) 2012, Jesse Farinacci
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, entribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.jenkins.ci.plugins.keyboard_shortcuts;
 
 import hudson.Extension;
 import hudson.model.UserProperty;
 import hudson.model.UserPropertyDescriptor;
 import hudson.model.User;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
  */
 public final class KeyboardShortcutsUserProperty extends UserProperty {
    @Extension
     public static final class DescriptorImpl extends UserPropertyDescriptor {
         public DescriptorImpl() {
             super(KeyboardShortcutsUserProperty.class);
         }
 
         @Override
         public String getDisplayName() {
             return Messages.Keyboard_Shortcuts_Plugin_DisplayName();
         }
 
         @Override
         public KeyboardShortcutsUserProperty newInstance(
                 final StaplerRequest request, final JSONObject formData)
                 throws FormException {
             try {
                 LOG.info("newInstance: " + formData);
                 return new KeyboardShortcutsUserProperty(
                         formData.getBoolean("disabled"));
             }
 
             catch (final JSONException e) {
                 throw new FormException(e, "disabled");
             }
         }
 
         @Override
         public KeyboardShortcutsUserProperty newInstance(final User user) {
             return new KeyboardShortcutsUserProperty();
         }
     }
 
     public static final boolean        DEFAULT_DISABLED = false;
 
     @Extension
     public static final DescriptorImpl DESCRIPTOR       = new DescriptorImpl();
 
     private static final Logger        LOG              = Logger.getLogger(KeyboardShortcutsUserProperty.class
                                                                 .getName());
 
     private boolean                    disabled;
 
     public KeyboardShortcutsUserProperty() {
         this(DEFAULT_DISABLED);
         LOG.info("KeyboardShortcutsUserProperty()");
     }
 
     @DataBoundConstructor
     public KeyboardShortcutsUserProperty(final boolean disabled) {
         super();
         LOG.info("KeyboardShortcutsUserProperty(" + disabled + ")");
        this.disabled = disabled;
     }
 
     @Override
     public UserPropertyDescriptor getDescriptor() {
         return DESCRIPTOR;
     }
 
     public boolean isDisabled() {
         return disabled;
     }
 
     private void save() {
         LOG.info("SAVE: " + user);
         if (user != null) {
             try {
                 user.save();
             }
 
             catch (final IOException e) {
                 LOG.warning(e.getMessage());
                 if (LOG.isLoggable(Level.INFO)) {
                     LOG.log(Level.INFO, e.getMessage(), e);
                 }
             }
         }
     }
 
     public void setDisabled(final boolean disabled) {
         this.disabled = disabled;
     }
 
     @Override
     public String toString() {
         return KeyboardShortcutsUserProperty.class.getName() + " - disabled? "
                 + disabled;
     }
 }
