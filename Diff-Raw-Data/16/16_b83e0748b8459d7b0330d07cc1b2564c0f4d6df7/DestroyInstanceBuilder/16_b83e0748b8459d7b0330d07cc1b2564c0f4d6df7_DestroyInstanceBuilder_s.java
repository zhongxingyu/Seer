 /*
  * Copyright 2013 Qubell, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.qubell.jenkinsci.plugins.qubell.builders;
 
 import com.qubell.services.InstanceStatusCode;
 import hudson.Extension;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 /**
 * Destroys a qubell instance, by using pre-saved instance id (see {@link StartInstanceBuilder})
  * After destroy is initiated, waits for instance to turn into Destroyed state.
  *
  * @author Alex Krupnov
  */
 public class DestroyInstanceBuilder extends RunCommandBuilder {
 
     /**
      * @param timeout         timeout string value coming from form
      * @param instanceOptions pre-defined instance options see {@link #getInstanceId()}
      */
     @DataBoundConstructor
     public DestroyInstanceBuilder(String timeout, InstanceOptions instanceOptions) {
         super("destroy", "{}", timeout, instanceOptions, null, InstanceStatusCode.DESTROYED);
     }
 
     /**
      * Descriptor for {@link DestroyInstanceBuilder}. Used as a singleton.
      * The class is marked as public so that it can be accessed from views.
      * See <tt>src/main/resources/com/qubell/jenkinsci/plugins/qubell/builders/DestroyInstanceBuilder/*.jelly</tt>
      * for the actual HTML fragment for the configuration screen.
      */
     @Extension // This indicates to Jenkins that this is an implementation of an extension point.
     public static final class DestroyInstanceDescriptor extends BaseDescriptor {
         /**
          * This human readable builder name is used in the configuration screen.
          */
         public String getDisplayName() {
             return "Qubell: Destroy Instance";
         }
     }
 
 }
