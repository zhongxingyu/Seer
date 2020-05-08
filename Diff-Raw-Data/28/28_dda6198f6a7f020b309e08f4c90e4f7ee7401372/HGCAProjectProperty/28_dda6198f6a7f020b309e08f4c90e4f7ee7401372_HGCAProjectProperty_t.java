 /*
  * The MIT License
  * 
  * Copyright (c) 2004-2009, Andrew Bayer
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
 package hudson.plugins.hgca;
 
 import hudson.model.AbstractProject;
 import hudson.model.Job;
 import hudson.model.JobProperty;
 import hudson.model.JobPropertyDescriptor;
 import hudson.model.Descriptor;
 import hudson.model.Action;
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.DataBoundConstructor;
 import net.sf.json.JSONObject;
 
 import java.util.HashMap;
 import java.util.Arrays;
 import java.util.List;
 /**
  * Property for {@link AbstractProject} that stores the HGCA pattern/URL pairs. Also handles setting
  * them globally.
  *
  * @author Andrew Bayer
  */
 public final class HGCAProjectProperty extends JobProperty<AbstractProject<?,?>> {
     
     /**
      * HGCA Pattern/URL pairs for this project - overrides global settings.
      */
     public final HashMap<String,String> annoPats; 
 
     @DataBoundConstructor
    public HGCAProjectProperty(List<Entry> annoPats) {
         this.annoPats = toMap(annoPats);
     }
 
     public HGCAProjectProperty(Entry... annoPats) {
         this(Arrays.asList(annoPats));
     }
     
     public boolean useAnnotations() {
         // Cheating slightly - getAnnotations().size() will be > 0 if there are *any* annotation pairs,
         // local or global. This'll make sure that the Enable checkbox is checked if there are global
         // pairs.
         if (getAnnotations().size()==0) 
             return false;
 
         return true;
     }
 
     public HashMap<String,String> getAnnotations() {
         // Get global annotations first, then project annotations.
         HashMap<String,String> allAnnos = new HashMap<String,String>();
         if (DESCRIPTOR.getGlobalAnnotations()!=null && DESCRIPTOR.getGlobalAnnotations().size() > 0)
             allAnnos.putAll(DESCRIPTOR.getGlobalAnnotations());
         allAnnos.putAll(annoPats);
         return allAnnos;
     }
 
     public DescriptorImpl getDescriptor() {
         return DESCRIPTOR;
     }
     
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     public static final class DescriptorImpl extends JobPropertyDescriptor {
         // We basically rip off the same logic for per-project annotation pairs for use in setting
         // global annotation pairs.
         private HashMap<String,String> globalAnnotations;
 
         public DescriptorImpl() {
             super(HGCAProjectProperty.class);
             load();
         }
 
         public boolean isApplicable(Class<? extends Job> jobType) {
             return AbstractProject.class.isAssignableFrom(jobType);
         }
 
         public HashMap<String,String> getGlobalAnnotations() {
             return globalAnnotations;
         }
 
         public String getDisplayName() {
             return "HGCA pattern/URL annotation pairs";
         }
 
         @Override
        public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
             // to persist global configuration information,
             // set that to properties and call save().
             HGCAProjectProperty hpp = req.bindJSON(HGCAProjectProperty.class,o);
             // Check the temporary project property to see if it has any pairs - make
             // sure to use annoPats directly and not getAnnotations(), 'cos that includes
             // existing global annotation pairs, and therefore breaks deletion.
             if (hpp.annoPats.size() > 0) 
                 this.globalAnnotations = hpp.annoPats;
             
             save();
             return super.configure(req);
         }
         
         @Override
        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             JSONObject toBind;
             System.err.println(formData.toString());
             // annoBlock contains the pairs in per-project configuration, but they're not in a block like 
             // that for global configuration.
             if (formData.has("annoBlock"))
                 toBind = formData.getJSONObject("annoBlock");
             else 
                 toBind = formData;
             HGCAProjectProperty hpp = req.bindJSON(HGCAProjectProperty.class,toBind);
             // If there are no per-project or global annotations, keep hpp null.
             if (hpp.getAnnotations().size() == 0)
                 hpp = null;
             return hpp;
         }
     }
 
     public static class Entry {
         public String key, value;
         
         @DataBoundConstructor
        public Entry(String key, String value) {
             this.key = key;
             this.value = value;
         }
     }
    
     private static HashMap<String,String> toMap(List<Entry> entries) {
         HashMap<String,String> map = new HashMap<String,String>();
         if (entries!=null && entries.size() > 0) {
             for (Entry entry: entries) {
                 map.put(entry.key,entry.value);
             }
         }
         return map;
     }
 
 }
