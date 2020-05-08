 /*
  * The MIT License
  *
  * Copyright 2013 Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
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
 package com.synopsys.arc.jenkinsci.plugins.cygwinprocesskiller;
 
 import hudson.Extension;
 import hudson.model.Node;
 import hudson.model.TaskListener;
 import hudson.slaves.NodeSpecific;
 import hudson.tools.ToolDescriptor;
 import hudson.tools.ToolInstallation;
 import hudson.tools.ToolProperty;
 import java.io.IOException;
 import java.util.List;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 /**
  *
  * @author Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
  */
 public class CygwinKillerInstallation extends ToolInstallation implements NodeSpecific<CygwinKillerInstallation> {
     @DataBoundConstructor
     public CygwinKillerInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
         super(name, home, properties);
     }
 
     @Override
     public CygwinKillerInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new CygwinKillerInstallation(getName(), translateFor(node, log), getProperties());
     }
     
     @Extension
     public static class DescriptorImpl extends ToolDescriptor<CygwinKillerInstallation> {
         public DescriptorImpl() {
             load();
         }
        
         @Override
         public String getDisplayName() {
             return Messages.CygwinKillerInstallation_DisplayName();
         }
         
         @Override
         public void setInstallations(CygwinKillerInstallation... installations) {
             super.setInstallations(installations);
             save();
         }
     }
 }
