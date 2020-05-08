 package jenkins.plugins.maveninfo.columns;
 
 import hudson.Extension;
 import hudson.maven.MavenModule;
 import hudson.maven.MavenModuleSet;
 import hudson.maven.MavenModuleSetBuild;
 import hudson.views.ListViewColumnDescriptor;
 import hudson.views.ListViewColumn;
 
 import java.util.Collections;
 import java.util.List;
 
 import jenkins.model.Jenkins;
 import jenkins.plugins.maveninfo.config.MavenInfoJobConfig;
 import jenkins.plugins.maveninfo.l10n.Messages;
 import jenkins.plugins.maveninfo.util.BuildUtils;
 import jenkins.plugins.maveninfo.util.MavenModuleComparator;
 import jenkins.plugins.maveninfo.util.ModuleNamePattern;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.bind.JavaScriptMethod;
 
 /**
  * Prints version of the last build of a Maven Job.
  * 
  * @author emenaceb
  * 
  */
 public class LastVersionColumn extends AbstractMavenInfoColumn {
 
 	@Extension
 	public static class DescriptorImpl extends ListViewColumnDescriptor {
 		@Override
 		public String getDisplayName() {
 			return Messages.MavenVersionColumn_DisplayName();
 		}
 
 
 		@Override
 		public boolean shownByDefault() {
 			return false;
 		}
 	}
 
 
 	@DataBoundConstructor
 	public LastVersionColumn(String columnName) {
 		super(columnName);
 	}
 
 	@JavaScriptMethod
 	public JSONObject getAjaxModuleList(String jobId) {
 		List<MavenModuleSet> list = Jenkins.getInstance().getAllItems(
 				MavenModuleSet.class);
 
 		for (MavenModuleSet mms : list) {
 			if (mms.getName().equals(jobId)) {
 				return getModuleList(mms);
 			}
 		}
 		return new JSONObject();
 	}
 
 	protected MavenModuleSetBuild getBuild(MavenModuleSet job) {
 		return BuildUtils.getLastBuild(job);
 	}
 
 	private JSONObject getModuleAsJson(MavenModule module) {
 		JSONObject json = new JSONObject();
 		json.accumulate("groupId", module.getModuleName().groupId);
 		json.accumulate("artifactId", module.getModuleName().artifactId);
 		json.accumulate("version", module.getVersion());
 		return json;
 	}
 
 	private JSONObject getModuleList(MavenModuleSet job) {
 
 		MavenModuleSetBuild build = getBuild(job);
 		ModuleNamePattern mainPattern = getModulePattern(job);
 
 		MavenModule mainModule = BuildUtils.getMainModule(build, mainPattern);
 		List<MavenModule> modules = BuildUtils.getModules(build);
 
 		Collections.sort(modules, new MavenModuleComparator());
 		JSONObject json = new JSONObject();
 		json.accumulate("mainModule", getModuleAsJson(mainModule));
 		JSONArray jsonModules = new JSONArray();
 		for (MavenModule module : modules) {
 			jsonModules.add(getModuleAsJson(module));
 		}
 		json.accumulate("modules", jsonModules);
 		return json;
 	}
 
 	protected ModuleNamePattern getModulePattern(MavenModuleSet job) {
 		MavenInfoJobConfig cfg = BuildUtils.getJobConfig(job);
 		return cfg.getCompiledMainModulePattern();
 	}
 
 	public String getVersion(MavenModuleSet job) {
 		MavenModuleSetBuild build = getBuild(job);
 		ModuleNamePattern mainPattern = getModulePattern(job);
 
 		MavenModule m = BuildUtils.getMainModule(build, mainPattern);
		return m != null? m.getVersion() : null;
 	}
 
 	public boolean isMultipleVersions(MavenModuleSet job) {
 		MavenModuleSetBuild build = BuildUtils.getLastBuild(job);
 		List<MavenModule> modules = BuildUtils.getModules(build);
 		if (modules.size() <= 1) {
 			return false;
 		}
 		String version = null;
 		for (MavenModule module : modules) {
 			if (!module.getVersion().equals(version)) {
 				if (version == null) {
 					version = module.getVersion();
 				} else {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	/**
 	 * Deserialization method to ensure compatibility.
 	 * @return
 	 */
 	public Object readResolve() {
 		
 		// 0.0.5 -> 0.1.0 added column name to object fields
 		// Keep caption
 		if(getColumnName() == null) {
 			setColumnName(Messages.MavenVersionColumn_Caption());
 		}
 		
 		return this ;
 	}
 }
