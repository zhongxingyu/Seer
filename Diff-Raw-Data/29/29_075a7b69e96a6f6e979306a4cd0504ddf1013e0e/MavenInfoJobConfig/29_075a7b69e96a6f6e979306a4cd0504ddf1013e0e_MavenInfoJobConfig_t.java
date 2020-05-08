 package jenkins.plugins.maveninfo.config;
 
 import hudson.Extension;
 import hudson.maven.MavenModule;
 import hudson.maven.MavenModuleSet;
 import hudson.model.JobProperty;
 import hudson.model.JobPropertyDescriptor;
 import hudson.model.Job;
 import hudson.util.FormValidation;
 import jenkins.plugins.maveninfo.l10n.Messages;
 import jenkins.plugins.maveninfo.util.InvalidPatternException;
 import jenkins.plugins.maveninfo.util.ModuleNamePattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 
 /**
  * Maven info extractor configuration.
  * 
  * @author emenaceb
  * 
  */
 public class MavenInfoJobConfig extends JobProperty<Job<?, ?>> {
 
 	@Extension(ordinal = 100)
 	public static class JobConfigDescriptor extends JobPropertyDescriptor {
 
 		public FormValidation checkPattern(String value, boolean optional) {
 
 			if (optional && StringUtils.isBlank(value)) {
 				return FormValidation.ok();
 			}
 			try {
 				new ModuleNamePattern(value);
 				return FormValidation.ok();
 			} catch (InvalidPatternException ex) {
 				return FormValidation.error(ex.getMessage());
 			}
 
 		}
 
 		public FormValidation doCheckMainModulePattern(
 				@QueryParameter String value) {
 			return checkPattern(value, true);
 		}
 
 		@Override
 		public String getDisplayName() {
 			return Messages.MavenInfoJobConfig_DisplayName();
 
 		}
 
 		@Override
 		public boolean isApplicable(Class<? extends Job> prj) {
 			return MavenModule.class.isAssignableFrom(prj)
 					|| MavenModuleSet.class.isAssignableFrom(prj);
 		}
 
 	}
 
 	private String mainModulePattern;
 
 	private String dependenciesPattern;
 
 	private boolean assignName;
 
 	private String nameTemplate;
 
 	private boolean assignDescription;
 
 	private String descriptionTemplate;
 
 	private transient ModuleNamePattern compiledMainModulePattern;
 
 	private transient ModuleNamePattern compiledDependenciesPattern;
 
 	public MavenInfoJobConfig() {
 		this("", "", true, "", true, "");
 	}
 
 	@DataBoundConstructor
 	public MavenInfoJobConfig(String mainModulePattern,
 			String dependenciesPattern, boolean assignName,
 			String nameTemplate, boolean assignDescription,
 			String descriptionTemplate) {
 		super();
 		setMainModulePattern(mainModulePattern);
 		setDependenciesPattern(dependenciesPattern);
 		setAssignName(assignName);
 		setNameTemplate(nameTemplate);
 		setAssignDescription(assignDescription);
 		setDescriptionTemplate(descriptionTemplate);
 	}
 
 	public ModuleNamePattern getCompiledDependenciesPattern() {
		if (this.compiledDependenciesPattern == null
				&& StringUtils.isNotBlank(dependenciesPattern)) {

			this.compiledDependenciesPattern = new ModuleNamePattern(
					this.dependenciesPattern);

		}
 		return compiledDependenciesPattern;
 	}
 
 	public ModuleNamePattern getCompiledMainModulePattern() {
		if (this.compiledMainModulePattern == null
				&& StringUtils.isNotBlank(mainModulePattern)) {

			this.compiledMainModulePattern = new ModuleNamePattern(
					this.mainModulePattern);

		}
 		return compiledMainModulePattern;
 	}
 
 	public String getDependenciesPattern() {
 		return dependenciesPattern;
 	}
 
 	public String getDescriptionTemplate() {
 		return descriptionTemplate;
 	}
 
 	public String getMainModulePattern() {
 		return mainModulePattern;
 	}
 
 	public String getNameTemplate() {
 		return nameTemplate;
 	}
 
 	public boolean isAssignDescription() {
 		return assignDescription;
 	}
 
 	public boolean isAssignName() {
 		return assignName;
 	}
 
 	public void setAssignDescription(boolean assignDescription) {
 		this.assignDescription = assignDescription;
 	}
 
 	public void setAssignName(boolean assignName) {
 		this.assignName = assignName;
 	}
 
 	public void setDependenciesPattern(String dependenciesPattern) {
 		if (StringUtils.isNotBlank(dependenciesPattern)) {
 			this.dependenciesPattern = dependenciesPattern.trim();
 		} else {
 			this.dependenciesPattern = "";
 		}
		this.compiledDependenciesPattern = null;
 	}
 
 	public void setDescriptionTemplate(String descriptionTemplate) {
 		this.descriptionTemplate = descriptionTemplate;
 	}
 
 	public void setMainModulePattern(String mainModulePattern) {
 		if (StringUtils.isNotBlank(mainModulePattern)) {
 			this.mainModulePattern = mainModulePattern.trim();
 		} else {
 			this.mainModulePattern = "";
 		}
		this.compiledMainModulePattern = null;
 	}
 
 	public void setNameTemplate(String nameTemplate) {
 		this.nameTemplate = nameTemplate;
 	}
 
 }
