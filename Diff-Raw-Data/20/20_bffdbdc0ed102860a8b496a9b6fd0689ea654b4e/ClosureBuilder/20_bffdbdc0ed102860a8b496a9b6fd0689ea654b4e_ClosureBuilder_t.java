 package org.stefanl.closure_utilities.closure;
 
 import org.stefanl.closure_utilities.html.HtmlBuildOptions;
 import org.stefanl.closure_utilities.html.HtmlBuilder;
 import org.stefanl.closure_utilities.internal.AbstractBuilder;
 import org.stefanl.closure_utilities.internal.BuildException;
 import org.stefanl.closure_utilities.internal.IBuilder;
 import org.stefanl.closure_utilities.javascript.JsBuildOptions;
 import org.stefanl.closure_utilities.javascript.JsBuilder;
 import org.stefanl.closure_utilities.render.DefaultHtmlRenderer;
 import org.stefanl.closure_utilities.stylesheets.GssBuildOptions;
 import org.stefanl.closure_utilities.stylesheets.GssBuilder;
 import org.stefanl.closure_utilities.templates.SoyBuildOptions;
 import org.stefanl.closure_utilities.templates.SoyBuilder;
 
 import javax.annotation.Nonnull;
 import java.io.File;
 
 public class ClosureBuilder
         extends AbstractBuilder<ClosureBuildOptions>
         implements IBuilder {
 
     public ClosureBuilder() {}
 
     public ClosureBuilder(@Nonnull final ClosureBuildOptions buildOptions) {
         super(buildOptions);
     }
 
     private final GssBuilder gssBuilder = new GssBuilder();
 
     private final SoyBuilder soyBuilder = new SoyBuilder();
 
     private final JsBuilder jsBuilder = new JsBuilder();
 
     private final HtmlBuilder htmlBuilder = new HtmlBuilder();
 
     @Override
     public void reset() {
         super.reset();
         gssBuilder.reset();
         soyBuilder.reset();
         jsBuilder.reset();
         htmlBuilder.reset();
     }
 
     public File getGssOutputFile() {
         return new File(buildOptions.getOutputDirectory(), "style.css");
     }
 
     @Nonnull
     public GssBuildOptions getGssBuildOptions() {
 
         final GssBuildOptions gssBuildOptions = new GssBuildOptions();
 
         gssBuildOptions.setShouldCalculateDependencies(true);
         gssBuildOptions.setAssetsDirectory(buildOptions.getAssetsDirectory());
         gssBuildOptions.setEntryPoints(buildOptions.getGssEntryPoints());
         gssBuildOptions.setSourceDirectories(
                 buildOptions.getGssSourceDirectories());
         gssBuildOptions.setRenameMap(
                 buildOptions.getCssClassRenameMap());
         gssBuildOptions.setShouldGenerateForDebug(
                 buildOptions.getShouldDebug());
         gssBuildOptions.setShouldGenerateForProduction(
                 buildOptions.getShouldCompile());
         gssBuildOptions.setOutputFile(getGssOutputFile());
         return gssBuildOptions;
     }
 
     public void buildGss()
             throws BuildException {
         if (!buildOptions.getIgnoreGssBuild()) {
             gssBuilder.setBuildOptions(getGssBuildOptions());
             gssBuilder.build();
         }
     }
 

    @Nonnull
    public File getSoyOutputDirectory() {
        final File soyOutputDirectory = buildOptions.getSoyOutputDirectory();
        if (soyOutputDirectory != null) {
            return soyOutputDirectory;
        }

        File outputDirectory = buildOptions.getOutputDirectory();
        return new File(outputDirectory, "compiled-templates");
    }

     @Nonnull
     public SoyBuildOptions getSoyBuildOptions() {
         final SoyBuildOptions soyBuildOptions = new SoyBuildOptions();
         soyBuildOptions.setSourceDirectories(
                 buildOptions.getSoySourceDirectories());
        soyBuildOptions.setOutputDirectory(getSoyOutputDirectory());
         return soyBuildOptions;
     }
 
     public void buildSoy()
             throws BuildException {
         if (!buildOptions.getIgnoreSoyBuild()) {
             soyBuilder.setBuildOptions(getSoyBuildOptions());
             soyBuilder.build();
         }
     }
 
     @Nonnull
     public JsBuildOptions getJsBuildOptions() {
 
         JsBuildOptions jsBuildOptions = new JsBuildOptions();
         jsBuildOptions.setEntryPoints(
                 buildOptions.getJavascriptEntryPoints());
         jsBuildOptions.setSourceDirectories(
                 buildOptions.getJavascriptSourceDirectories());
         return jsBuildOptions;
 
     }
 
     public void buildJs() throws BuildException {
         if (!buildOptions.getIgnoreJsBuild()) {
             jsBuilder.setBuildOptions(getJsBuildOptions());
             jsBuilder.build();
         }
     }
 
     @Nonnull
     public HtmlBuildOptions getHtmlBuildOptions() {
 
         HtmlBuildOptions htmlBuildOptions = new HtmlBuildOptions();
         htmlBuildOptions.setHtmlRenderer(new DefaultHtmlRenderer());
         htmlBuildOptions.setShouldBuildInline(false);
         htmlBuildOptions.setLocationMap(null);
         htmlBuildOptions.setStylesheetFiles(gssBuilder.getSourceFiles());
         htmlBuildOptions.setJavascriptFiles(jsBuilder.getSourceFiles());
         htmlBuildOptions.setContent(null);
         htmlBuildOptions.setOutputFile(null);
         return htmlBuildOptions;
     }
 
 
     public void buildHtml() throws BuildException {
         if (!buildOptions.getIgnoreHtmlBuild()) {
             htmlBuilder.setBuildOptions(getHtmlBuildOptions());
             htmlBuilder.build();
         }
     }
 
     @Override
     public void buildInternal() throws BuildException {
         buildGss();
         buildSoy();
         buildJs();
         buildHtml();
     }
 
     @Override
     public void checkOptions() throws BuildException {
         super.checkOptions();
         if (!buildOptions.getIgnoreBuilds()) {
             checkNotNull(buildOptions.getOutputDirectory(),
                     "You need to specify a output directory");
         }
     }
 }
