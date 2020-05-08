 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.command.base;
 
 import org.apache.felix.gogo.commands.Option;
 
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.BaseResource;
 
 /**
  * Performs an addition of a resource
  */
 public abstract class AbstractAddCommand<T extends BaseResource> extends AbstractCommand {
 
     @Option(name = "-d", aliases = { "--description" }, description = "Description of the new entity", required = false, multiValued = false)
     private String description;
 
     @Option(name = "-l", aliases = {"--verbose"}, description="Display new entity in verbose format", required = false, multiValued = false)
     protected boolean verbose;
 
     protected VerboseDisplay<T> verboseDisplay;
 
     protected T doAdd(T addition, Class<T> clz, String rel, String localName) throws Exception {
         addition.setDescription(description);
         return client.doAdd(addition, clz, client.getTopLink(rel), localName);
     }
 
     protected void display(T model) {
         if (model != null) {
             StringBuffer print = new StringBuffer("Created [");
             print.append(model.getName()).append("] [");
             print.append(model.getId()).append("] ");
             print.append(model.isSetDescription() ? model.getDescription() : "").append("\n");
             System.out.print(print.toString());
             verbose(model);
         }
     }
 
     protected void verbose(T model) {
        if (verbose && verboseDisplay != null) {
             verboseDisplay.expand(model);
         }
     }
 
     @SuppressWarnings("unchecked")
     public void setVerboseDisplay(VerboseDisplay verboseDisplay) {
         this.verboseDisplay = (VerboseDisplay<T>)verboseDisplay;
     }
 }
