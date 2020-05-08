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
 import com.sonyericsson.hudson.plugins.metadata.model.values.MetadataValue;
 import com.sonyericsson.hudson.plugins.metadata.model.values.ParentUtil;
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import hudson.Extension;
 import hudson.ExtensionList;
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.model.Hudson;
 import hudson.model.JobProperty;
 import hudson.model.JobPropertyDescriptor;
 import hudson.security.ACL;
 import net.sf.json.JSON;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.export.Exported;
 import org.kohsuke.stapler.export.ExportedBean;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import static com.sonyericsson.hudson.plugins.metadata.Constants.REQUEST_ATTR_METADATA_CONTAINER;
 
 /**
  * Gives support for meta data on Projects and their builds.
  *
  * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
  */
 @edu.umd.cs.findbugs.annotations.SuppressWarnings(
         value = "UG_SYNC_SET_UNSYNC_GET",
         justification = "It is synchronized")
 @XStreamAlias("job-metadata")
 @ExportedBean
 public class MetadataJobProperty extends JobProperty<AbstractProject<?, ?>> implements MetadataContainer<MetadataValue> {
 
     private List<MetadataValue> values;
     private transient MetadataJobAction metadataJobAction;
 
     /**
      * Standard DataBound Constructor.
      *
      * @param values the meta data.
      */
     @DataBoundConstructor
     public MetadataJobProperty(List<MetadataValue> values) {
        for (MetadataValue value : values) {
            value.setParent(this);
        }
         this.values = values;
     }
 
     /**
      * Default constructor. <strong>Do not use unless you are a serializer.</strong>
      */
     public MetadataJobProperty() {
     }
 
     /**
      * The meta data.
      *
      * @return the values.
      */
     public synchronized List<MetadataValue> getValues() {
         if (values == null) {
             values = new LinkedList<MetadataValue>();
         }
         return values;
     }
 
     /**
      * All the non generated values. I.e. the values that the user has put in.
      *
      * @return all user values.
      */
     public synchronized List<MetadataValue> getUserValues() {
         List<MetadataValue> allValues = getValues();
         List<MetadataValue> userValues = new LinkedList<MetadataValue>();
         for (MetadataValue value : allValues) {
             if (!value.isGenerated()) {
                 userValues.add(value);
             }
         }
         return userValues;
     }
 
     /**
      * The current Project.
      *
      * @return the owner.
      */
     public AbstractProject<?, ?> getOwner() {
         return owner;
     }
 
     @Override
     public synchronized Collection<? extends Action> getJobActions(AbstractProject<?, ?> job) {
         if (metadataJobAction == null) {
             metadataJobAction = new MetadataJobAction(job.getProperty(this.getClass()));
         }
         return Collections.singletonList(metadataJobAction);
     }
 
     @Override
     public synchronized MetadataValue getChild(String name) {
         return ParentUtil.getChildValue(getValues(), name);
     }
 
     @Override
     public synchronized int indexOf(String name) {
         return ParentUtil.getChildIndex(values, name);
     }
 
     @Override
     public synchronized MetadataValue setChild(int index, MetadataValue value) {
         return values.set(index, value);
     }
 
     @Override
     public synchronized Collection<MetadataValue> addChild(MetadataValue value) {
         return ParentUtil.addChildValue(this, getValues(), value);
     }
 
     @Override
     public synchronized Collection<MetadataValue> addChildren(Collection<MetadataValue> childValues) {
         return ParentUtil.addChildValues(this, getValues(), childValues);
     }
 
     @Override
     @Exported
     public Collection<MetadataValue> getChildren() {
         return values;
     }
 
     /**
      * All registered meta data descriptors. To be used by a hetero-list.
      *
      * @param request the current http request.
      * @return a list.
      */
     public synchronized List<AbstractMetadataDefinition> getDefinitions(StaplerRequest request) {
         return PluginImpl.getInstance().getDefinitions();
     }
 
     @Override
     public String getFullName() {
         return "";
     }
 
     @Override
     public String getFullName(String separator) {
         return "";
     }
 
     @Override
     public String getFullNameFrom(MetadataParent base) {
         return "";
     }
 
     @Override
     public synchronized JSON toJson() {
         return ParentUtil.toJson(this);
     }
 
     @Override
     public boolean requiresReplacement() {
         return false;
     }
 
     @Override
     public synchronized void save() throws IOException {
         if (owner != null) {
             owner.save();
         } else {
             throw new IOException("This container is not attached to any job.");
         }
     }
 
     @Override
     public ACL getACL() {
         if (owner != null) {
             return owner.getACL();
         } else {
             return Hudson.getInstance().getACL();
         }
     }
 
     /**
      * Descriptor for the {@link MetadataJobProperty}.
      */
     @Extension
     public static class MetaDataJobPropertyDescriptor extends JobPropertyDescriptor {
 
         @Override
         public String getDisplayName() {
             return Messages.MetadataJobProperty_DisplayName();
         }
 
         /**
          * All registered meta data descriptors that applies to jobs. To be used by a hetero-list.
          *
          * @param request the current http request.
          * @return a list.
          */
         public List<AbstractMetadataValue.AbstractMetaDataValueDescriptor> getValueDescriptors(StaplerRequest request) {
             request.setAttribute(REQUEST_ATTR_METADATA_CONTAINER, this);
             List<AbstractMetadataValue.AbstractMetaDataValueDescriptor> list =
                     new LinkedList<AbstractMetadataValue.AbstractMetaDataValueDescriptor>();
             ExtensionList<AbstractMetadataValue.AbstractMetaDataValueDescriptor> extensionList =
                     Hudson.getInstance().getExtensionList(AbstractMetadataValue.AbstractMetaDataValueDescriptor.class);
             for (AbstractMetadataValue.AbstractMetaDataValueDescriptor d : extensionList) {
                 if (d.appliesTo(this)) {
                     list.add(d);
                 }
             }
             return list;
         }
 
         /**
          * Gives the {@link MetadataJobProperty} for the given Job. If no metadata is available on the job the property
          * will be created and added to it.
          *
          * @param project the job.
          * @return the property created or found.
          *
          * @throws IOException if an error occurs when adding the property to the node.
          */
         public static MetadataJobProperty instanceFor(AbstractProject project) throws IOException {
             MetadataJobProperty property = (MetadataJobProperty)project.getProperty(MetadataJobProperty.class);
 
             if (property == null) {
                 property = new MetadataJobProperty();
                 project.addProperty(property);
             }
 
             if (property.getOwner() == null || property.getOwner() != project) {
                 property.setOwner(project); //Quick fix for some unit tests and just to be sure it is set.
             }
             return property;
         }
     }
 }
