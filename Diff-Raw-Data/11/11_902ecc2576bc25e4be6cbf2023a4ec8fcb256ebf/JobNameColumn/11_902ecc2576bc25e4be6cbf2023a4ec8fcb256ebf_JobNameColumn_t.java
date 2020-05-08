 package com.robestone.hudson.compactcolumns;
 
 import hudson.Extension;
 import hudson.views.JobColumn;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import com.robestone.hudson.compactcolumns.AbstractStatusesColumn.AbstractCompactColumnDescriptor;
 
 /**
  * @author jacob robertson
  */
 public class JobNameColumn extends JobColumn {
 
 	@DataBoundConstructor
     public JobNameColumn() {
     }
 
     @Extension
     public static class DescriptorImpl extends AbstractCompactColumnDescriptor {
		public String getColumnDisplayName() {
			return hudson.views.Messages.JobColumn_DisplayName();
		}
         @Override
         public String getDisplayName() {
             return Messages.Compact_Column_Job_Name();
         }
     }
 }
