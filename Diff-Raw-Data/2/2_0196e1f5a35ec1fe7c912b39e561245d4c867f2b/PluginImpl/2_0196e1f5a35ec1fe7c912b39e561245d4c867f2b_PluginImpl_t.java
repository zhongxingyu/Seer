 /*
  *  The MIT License
  *
  *  Copyright 2011 Sony Ericsson Mobile Communications. All rights reserved.
  *
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  *  in the Software without restriction, including without limitation the rights
  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *  copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  */
 package com.sonyericsson.hudson.plugins.metadata.model;
 
 import com.sonyericsson.hudson.plugins.metadata.Messages;
 import com.sonyericsson.hudson.plugins.metadata.model.definitions.AbstractMetadataDefinition;
 import com.sonyericsson.hudson.plugins.metadata.model.values.AbstractMetadataValue;
 import com.sonyericsson.hudson.plugins.metadata.model.values.DateMetadataValue;
 import com.sonyericsson.hudson.plugins.metadata.model.values.NumberMetadataValue;
 import com.sonyericsson.hudson.plugins.metadata.model.values.StringMetadataValue;
 import com.sonyericsson.hudson.plugins.metadata.model.values.TreeNodeMetadataValue;
 import hudson.Extension;
 import hudson.Plugin;
 import hudson.model.Hudson;
 import hudson.model.Items;
 import hudson.model.Run;
 import hudson.security.Permission;
 import hudson.security.PermissionGroup;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Main plugin singleton.
  *
  * @author Tomas Westling &lt;thomas.westling@sonyericsson.com&gt;
  */
 @Extension
 public class PluginImpl extends Plugin {
 
     /**
      * Permission Groups for metadata related tasks.
      */
     public static final PermissionGroup PERMISSION_GROUP =
             new PermissionGroup(PluginImpl.class, Messages._Permission_Metadata());
 
     /**
      * Permission to read metadata on objects. Primarily for use in CLI commands.
      */
     public static final Permission READ_METADATA =
             new Permission(PERMISSION_GROUP, "Read", Messages._Permission_Read(), Hudson.READ);
 
     /**
      * Permission to update metadata on objects. Primarily for use in CLI commands.
      */
     public static final Permission UPDATE_METADATA =
             new Permission(PERMISSION_GROUP, "Update", Messages._Permission_Update(), Hudson.ADMINISTER);
 
     /**
      * Permission to update and replace metadata on objects. Primarily for use in CLI commands.
      */
     public static final Permission REPLACE_METADATA =
             new Permission(PERMISSION_GROUP, "Replace", Messages._Permission_Replace(), Hudson.ADMINISTER);
 
     /**
      * Permission to configure metadata definitions, i.e. use the Metadata configuration page.
      */
     public static final Permission CONFIGURE_DEFINITIONS =
             new Permission(PERMISSION_GROUP, "Configure", Messages._Permission_Configure_definitions(),
                     Hudson.ADMINISTER);
 
     private List<AbstractMetadataDefinition> definitions = new LinkedList<AbstractMetadataDefinition>();
 
     @Override
     public void start() throws Exception {
        super.start();
        load();
         performXStreamRegistrations();
     }
 
     /**
      * Process the XStream annotations.
      */
     private void performXStreamRegistrations() {
         Class[] types = {
                 AbstractMetadataValue.class,
                 DateMetadataValue.class,
                 NumberMetadataValue.class,
                 StringMetadataValue.class,
                 TreeNodeMetadataValue.class,
                 MetadataJobProperty.class,
                 MetadataNodeProperty.class, };
         //Register it in all known XStreams just to be sure.
         Hudson.XSTREAM.processAnnotations(types);
         Items.XSTREAM.processAnnotations(types);
         Run.XSTREAM.processAnnotations(types);
     }
 
     /**
      * Setter for the list of AbstractMetaDataDefinitions.
      *
      * @param definitions the list of AbstractMetaDataDefinitions
      */
     public void setDefinitions(List<AbstractMetadataDefinition> definitions) {
         this.definitions = definitions;
     }
 
     /**
      * Getter for the list of AbstractMetaDataDefinitions.
      *
      * @return the list of AbstractMetaDataDefinitions
      */
     public List<AbstractMetadataDefinition> getDefinitions() {
         return definitions;
     }
 
     /**
      * Gets the singleton instance of this Plugin.
      *
      * @return the instance.
      */
     public static PluginImpl getInstance() {
         return Hudson.getInstance().getPlugin(PluginImpl.class);
     }
 
 
 }
