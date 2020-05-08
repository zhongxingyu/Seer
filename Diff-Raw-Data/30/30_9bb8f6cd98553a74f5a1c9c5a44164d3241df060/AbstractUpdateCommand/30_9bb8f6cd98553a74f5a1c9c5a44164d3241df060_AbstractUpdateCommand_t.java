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
 
 import java.lang.reflect.Method;
 import java.util.List;
 
 import org.apache.felix.gogo.commands.Option;
 
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.BaseResource;
 import com.redhat.rhevm.api.model.Host;
 
 import static com.redhat.rhevm.api.common.util.ReflectionHelper.set;
 import static com.redhat.rhevm.api.command.base.BaseClient.pad;
 import static com.redhat.rhevm.api.command.base.BaseClient.value;
 
 /**
  * Performs an update on a resource
  */
 public abstract class AbstractUpdateCommand<T extends BaseResource> extends AbstractCommand {
 
     @Option(name = "-f", aliases = { "--field" }, description = "Resource field to be updated", required = true, multiValued = false)
     private String field;
 
     @Option(name = "-v", aliases = {"--value"}, description="New value for field", required = true, multiValued = false)
     protected String value;
 
     @Option(name = "-l", aliases = {"--verbose"}, description="Display modified entity in verbose format", required = false, multiValued = false)
     protected boolean verbose;
 
     protected VerboseDisplay<T> verboseDisplay;
 
     protected void doUpdate(List<T> collection, Class<T> clz, String localName, String name) throws Exception {
         T resource = getResource(collection, name);
         if (resource != null) {
             if (set(resource, field, value)) {
                 if (resource.getHref() != null) {
                     display(client.doUpdate(resource, clz, field, resource.getHref(), localName));
                 }
             } else {
                 System.err.println(field + " is unknown or not a top-level primitive element");
             }
         }
     }
 
     protected void display(T model) {
         if (model != null) {
             System.out.println(pad("Name", longer(model.getName(), "Name"), false)
                                + pad("ID", longer(model.getId(), "ID"), false)
                                + (model.isSetDescription() ? " Description" : ""));
             System.out.println(pad(model.getName(), longer(model.getName(), "Name"))
                                + pad(model.getId(), longer(model.getId(), "ID"))
                                + " " + value(model.getDescription()));
             verbose(model);
         }
     }
 
     protected int longer(String a, String b) {
         return a.length() > b.length() ? a.length() : b.length();
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
