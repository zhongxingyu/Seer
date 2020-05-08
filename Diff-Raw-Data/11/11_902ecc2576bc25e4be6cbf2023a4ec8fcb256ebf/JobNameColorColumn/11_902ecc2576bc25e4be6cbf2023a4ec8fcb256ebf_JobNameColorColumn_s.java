 package com.robestone.hudson.compactcolumns;
 
 import hudson.Extension;
 import hudson.model.Job;
 import hudson.model.Result;
 import hudson.model.Run;
 
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import com.robestone.hudson.compactcolumns.AbstractStatusesColumn.AbstractCompactColumnDescriptor;
 
 /**
  * @author jacob robertson
  */
 public class JobNameColorColumn extends AbstractCompactColumn {
 
 	private boolean showColor;
 	private boolean showDescription;
 	private boolean showLastBuild;
 	
 	@DataBoundConstructor
 	public JobNameColorColumn(boolean showColor, boolean showDescription,
 			boolean showLastBuild, String colorblindHint) {
 		super(colorblindHint);
 		this.showColor = showColor;
 		this.showDescription = showDescription;
 		this.showLastBuild = showLastBuild;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public String getStyle(Job job) {
 		Result result = null;
 		if (job != null) {
 			Run run = job.getLastBuild();
 			if (run != null) {
 				result = run.getResult();
 			}
 		}
 		String color;
 		String underline;
 		if (result == null) {
 			color = BuildInfo.OTHER_COLOR;
 			underline = AbstractStatusesColumn.OTHER_UNDERLINE_STYLE;
 		} else if (Result.ABORTED.equals(result)) {
 			color = BuildInfo.OTHER_COLOR;
 			underline = AbstractStatusesColumn.OTHER_UNDERLINE_STYLE;
 		} else if (Result.FAILURE.equals(result)) {
 			color = BuildInfo.FAILED_COLOR;
 			underline = AbstractStatusesColumn.FAILED_UNDERLINE_STYLE;
 		} else if (Result.NOT_BUILT.equals(result)) {
 			color = BuildInfo.OTHER_COLOR;
 			underline = AbstractStatusesColumn.OTHER_UNDERLINE_STYLE;
 		} else if (Result.SUCCESS.equals(result)) {
 			color = BuildInfo.getStableColorString();
 			underline = AbstractStatusesColumn.STABLE_UNDERLINE_STYLE;
 		} else if (Result.UNSTABLE.equals(result)) {
 			color = BuildInfo.UNSTABLE_COLOR;
 			underline = AbstractStatusesColumn.UNSTABLE_UNDERLINE_STYLE;
 		} else {
 			color = BuildInfo.OTHER_COLOR;
 			underline = AbstractStatusesColumn.OTHER_UNDERLINE_STYLE;
 		}
 		String style = "";
 		if (showColor) {
 			style += ("color: " + color + ";");
 		}
 		if (isShowColorblindUnderlineHint()) {
 			style += ("text-decoration: none; border-bottom: " + underline + ";");
 		}
 		return style;
 	}
 	@SuppressWarnings("rawtypes")
 	public String getToolTip(Job job, Locale locale) {
 		StringBuilder tip = new StringBuilder();
 		if (showDescription) {
 			String desc = job.getDescription();
 			if (!StringUtils.isEmpty(desc)) {
 				tip.append(desc);
 			}
 		}
 		if (showLastBuild) {
 			List<BuildInfo> builds = AbstractStatusesColumn.getBuilds(job, false, false, true, isShowColorblindUnderlineHint(), 0);
 			if (!builds.isEmpty()) {
 				BuildInfo build = builds.get(0);
 				String desc = AbstractStatusesColumn.getBuildDescriptionToolTip(build, locale);
 				if (!StringUtils.isEmpty(desc)) {
 					if (tip.length() > 0) {
 						tip.append("<hr/>");
 					}
 					tip.append(desc);
 				}
 			}
 		}
 		return tip.toString();
 	}
     public boolean isShowColor() {
 		return showColor;
 	}
 	public boolean isShowDescription() {
 		return showDescription;
 	}
 	public boolean isShowLastBuild() {
 		return showLastBuild;
 	}
 
 	@Extension
     public static class DescriptorImpl extends AbstractCompactColumnDescriptor {
         @Override
         public String getDisplayName() {
             return Messages.Compact_Column_Job_Name_w_Options();
         }
         @Override
         public String getHelpFile() {
             return "/plugin/compact-columns/job-name-color-column.html";
         }
     }
 }
