 /*
  * The MIT License
  *
  * Copyright (c) 2010, Manufacture Française des Pneumatiques Michelin, Romain Seguy
  * Copyright (c) 2007-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Erik Ramfelt,
  *                          Henrik Lynggaard, Peter Liljenberg, Andrew Bayer
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
 
 package com.michelin.cio.hudson.plugins.clearcaseucmbaseline;
 
 import hudson.FilePath;
 import hudson.Util;
 import hudson.plugins.clearcase.ClearToolExec;
 import hudson.plugins.clearcase.ClearToolLauncher;
 import hudson.plugins.clearcase.util.PathUtil;
 import hudson.util.ArgumentListBuilder;
 import hudson.util.VariableResolver;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * This class defines the cleartool command for use by
  * {@link ClearCaseUcmBaselineParameterValue#createBuildWrapper(hudson.model.AbstractBuild)}.
  * 
  * <p>While this class extends {@link ClearToolExec}, most of {@link ClearToolExec}'s
  * methods will throw an {@link UnsupportedOperationException} since only the methods
  * useful for {@link ClearCaseUcmBaselineParameterValue#createBuildWrapper(hudson.model.AbstractBuild)}
  * have been implemented.</p>
  *
  * @author Romain Seguy (http://davadoc.deviantart.com)
  */
 public class ClearToolUcmBaseline extends ClearToolExec {
 
     /**
      * Used to cache components given the baseline they belong to (the key is
      * a baseline selector).
      */
     private transient Map<String, String> componentsCache = new HashMap<String, String>();
     /**
      * Used to cache root dirs given the component they belong to (the key is
      * a component selector).
      */
     private transient Map<String, String> componentRootDirsCache = new HashMap<String, String>();
     /**
      * Used to cache the dependent baselines given the baseline they depend on.
      */
     private transient Map<String, String[]> dependentBaselinesCache = new HashMap<String, String[]>();
 
     public ClearToolUcmBaseline(VariableResolver variableResolver, ClearToolLauncher launcher) {
         super(variableResolver, launcher, null);
     }
 
     /**
      * Returns, for a given ClearCase UCM baseline, the ClearCase UCM component
      * this baseline refers to.
      *
      * <p>The ClearCase UCM components are cached for each instance of the class.</p>
      *
      * @see ftp://ftp.software.ibm.com/software/rational/docs/v2002/cc/cc_ref_1.pdf (%[component]p, page 392)
      */
     public String getComponentFromBaseline(String pvob, String baseline) throws IOException, InterruptedException {
         String baselineSelector = baseline + '@' + pvob;
 
         if(componentsCache.containsKey(baselineSelector)) {
             return componentsCache.get(baselineSelector);
         }
 
         // cleartool lsbl -fmt "%[component]p" <baseline>@<pvob>
         ArgumentListBuilder cmd = new ArgumentListBuilder();
         cmd.add("lsbl");
         cmd.add("-fmt");
         cmd.add("%[component]p");
         cmd.add(baselineSelector);
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         launcher.run(cmd.toCommandArray(), null, baos, null);
         String cleartoolOutput = ClearCaseUcmBaselineUtils.processCleartoolOuput(baos);
         baos.close();
 
         // ensure no error has occured
         if(cleartoolOutput.contains("cleartool: Error")) {
             launcher.getListener().error("Failed to get component from the baseline " + baselineSelector + ".");
             throw new IOException("Failed to get the component from the baseline " + baselineSelector + ": " + cleartoolOutput);
         }
 
         // no error ==> it means the cleartool ouput just contains the component
 
         // caching
         componentsCache.put(baselineSelector, cleartoolOutput);
 
         return cleartoolOutput;
     }
 
     /**
      * Returns, for a given ClearCase UCM component, its root dir.
      *
      * <p>Note that in case the component is rootless, an empty string is
      * returned (cf. HUDSON-6398).</p>
      * <p>The root dirs are cached for each instance of the class.</p>
      *
      * @see ftp://ftp.software.ibm.com/software/rational/docs/v2002/cc/cc_ref_1.pdf (%[root_dir]p, page 392)
      */
     public String getComponentRootDir(String pvob, String component) throws IOException, InterruptedException {
         String componentSelector = component + '@' + pvob;
 
         if(componentRootDirsCache.containsKey(componentSelector)) {
             return componentRootDirsCache.get(componentSelector);
         }
 
         // cleartool lscomp -fmt "%[root_dir]p" <component>@<pvob>
         ArgumentListBuilder cmd = new ArgumentListBuilder();
         cmd.add("lscomp");
         cmd.add("-fmt");
         cmd.add("%[root_dir]p");
         cmd.add(componentSelector);
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         launcher.run(cmd.toCommandArray(), null, baos, null);
         String cleartoolOutput = ClearCaseUcmBaselineUtils.processCleartoolOuput(baos);
         baos.close();
 
         // ensure no error has occured
         if(cleartoolOutput != null && cleartoolOutput.contains("cleartool: Error")) {
             launcher.getListener().error("Failed to get root dir of the component " + componentSelector + ".");
             throw new IOException("Failed to get the root dir of the component " + componentSelector + ": " + cleartoolOutput);
         }
 
         // no error ==> it means the cleartool ouput just contains the root dir
         // or an empty string if the component is rootless -- cf. HUDSON-6398
 
         // caching
         componentRootDirsCache.put(componentSelector, cleartoolOutput);
 
         return cleartoolOutput != null ? cleartoolOutput.trim() : StringUtils.EMPTY;
     }
 
     /**
      * Returns, for a given ClearCase UCM composite baseline, all the baselines
      * in the dependencies graph.
      *
      * @return An array of ClearCase UCM baseline selectors (it may be empty)
      *
      * @see ftp://ftp.software.ibm.com/software/rational/docs/v2002/cc/cc_ref_1.pdf (%[depends_on_closure]p, page 392)
      */
     public String[] getDependentBaselines(String pvob, String baseline) throws IOException, InterruptedException {
         String baselineSelector = baseline + '@' + pvob;
 
         if(dependentBaselinesCache.containsKey(baselineSelector)) {
             return dependentBaselinesCache.get(baselineSelector);
         }
 
         String[] dependentBaselines = null;
 
         ArgumentListBuilder cmd = new ArgumentListBuilder();
         cmd.clear();
         cmd.add("lsbl");
         cmd.add("-fmt");
         cmd.add("%[depends_on_closure]p");
         cmd.add(baselineSelector);
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         launcher.run(cmd.toCommandArray(), null, baos, null);
         String cleartoolOutput = ClearCaseUcmBaselineUtils.processCleartoolOuput(baos);
         baos.close();
 
         // ensure no error has occured
         if(cleartoolOutput.contains("cleartool: Error")) {
             launcher.getListener().error("Failed to get the dependent baselines from the baseline " + baselineSelector + ".");
             throw new IOException("Failed to get the dependent baselines from the baseline " + baselineSelector + ": " + cleartoolOutput);
         }
 
         if(cleartoolOutput.length() > 0) {
             dependentBaselines = cleartoolOutput.split(" ");
         }
         else {
             dependentBaselines = new String[0];
         }
 
         // caching
         dependentBaselinesCache.put(baselineSelector, dependentBaselines);
 
         return dependentBaselines;
     }
 
     @Override
     protected FilePath getRootViewPath(ClearToolLauncher launcher) {
         return launcher.getWorkspace();
     }
 
     public void update(String viewName, String loadRules) throws IOException, InterruptedException {
         unsupportedMethod(Thread.currentThread().getStackTrace()[0]);
     }
 
     @Override
     public void rmview(String viewName) throws IOException, InterruptedException {
         // cleartool rmview -force <view name>
         ArgumentListBuilder cmd = new ArgumentListBuilder();
         cmd.add("rmview");
         cmd.add("-force");
         cmd.add(viewName);
 
         // run the cleartool command
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         launcher.run(cmd.toCommandArray(), null, baos, null);
         String cleartoolOutput = ClearCaseUcmBaselineUtils.processCleartoolOuput(baos);
         baos.close();
         
         // ensure no error has occured
         if(cleartoolOutput.contains("cleartool: Error")) {
             launcher.getListener().error("Failed to remove view " + viewName + ".");
             throw new IOException("Failed to remove view " + viewName + ": " + cleartoolOutput);
         }
 
         // manually delete the view folder if cleartool rmview didn't actually remove it
         FilePath viewFilePath = launcher.getWorkspace().child(viewName);
         if(viewFilePath.exists()) {
             launcher.getListener().getLogger().println("View folder was not actually removed by \"cleartool rmview\"; Removing it now...");
             viewFilePath.deleteRecursive();
         }
     }
 
     @Override
     public void rmviewtag(String viewName) throws IOException, InterruptedException {
         unsupportedMethod(Thread.currentThread().getStackTrace()[0]);
     }
 
     public void mkview(String viewName, String streamSelector) throws IOException, InterruptedException {
         mkview(viewName, null, true, null);
     }
 
     public void mkview(String viewName, String mkviewOptionalParam, boolean snapshotView, String streamSelector) throws IOException, InterruptedException {
         // cleartool mkview -tag <tag> <view path> <optional parameters>
         ArgumentListBuilder cmd = new ArgumentListBuilder();
         cmd.add("mkview");
         if(snapshotView) {
             cmd.add("-snapshot");
         }
         cmd.add("-tag");
         cmd.add(viewName);  // let's save user config stuff: we reuse the view name as the tag name
 
         // HUDSON-6409
         if(StringUtils.isNotBlank(mkviewOptionalParam)) {
             cmd.addTokenized(Util.replaceMacro(mkviewOptionalParam, variableResolver));
         }
 
        cmd.add(viewName);
 
         // run the cleartool command
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         launcher.run(cmd.toCommandArray(), null, baos, null);
         String cleartoolOutput = ClearCaseUcmBaselineUtils.processCleartoolOuput(baos);
         baos.close();
 
         // ensure no error has occured
         if(cleartoolOutput.contains("cleartool: Error")) {
             launcher.getListener().error("Failed to create view " + viewName + ".");
             throw new IOException("Failed to create view " + viewName + ": " + cleartoolOutput);
         }
     }
 
     // ClearCase plugin 1.1 upward compatibility
     @Override
     public void mkview(String viewName, String streamSelector, String defaultStorageDir) throws IOException, InterruptedException {
         unsupportedMethod(Thread.currentThread().getStackTrace()[0]);
     }
 
     public void setcs(String viewName, String configSpec) throws IOException, InterruptedException {
         FilePath workspace = launcher.getWorkspace();
         FilePath configSpecFile = workspace.createTextTempFile("configspec", ".txt", configSpec);
         String configSpecLocation = ".." + File.separatorChar + configSpecFile.getName();
         configSpecLocation = PathUtil.convertPathForOS(configSpecLocation, launcher.getLauncher());
 
         // cleartool setcs <configspec>
         ArgumentListBuilder cmd = new ArgumentListBuilder();
         cmd.add("setcs");
         cmd.add("-tag");
         cmd.add(viewName);
     	cmd.add(configSpecLocation);
 
         // run the cleartool command
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         launcher.run(cmd.toCommandArray(), null, baos, workspace.child(viewName));
         String cleartoolOutput = ClearCaseUcmBaselineUtils.processCleartoolOuput(baos);
         baos.close();
 
         configSpecFile.delete();
 
         // ensure no error has occured
         if(cleartoolOutput.contains("cleartool: Error")) {
             launcher.getListener().error("Failed to set the config spec of the view " + viewName + ".");
             throw new IOException("Failed to set the config spec of the view " + viewName + ": " + cleartoolOutput);
         }
     }
 
     @Override
     public void startView(String viewTags) throws IOException, InterruptedException {
         unsupportedMethod(Thread.currentThread().getStackTrace()[0]);
     }
 
     public void syncronizeViewWithStream(String viewName, String stream) throws IOException, InterruptedException {
         unsupportedMethod(Thread.currentThread().getStackTrace()[0]);
     }
 
     // ClearCase plugin 1.2 upward compatibility
     @Override
     public void update(String viewName, String[] loadRules) throws IOException, InterruptedException {
         unsupportedMethod(Thread.currentThread().getStackTrace()[0]);
     }
 
     /**
      * Always throws an {@link UnsupportedOperationException}.
      * 
      * @param ste a {@link StackTraceElement} from which the name of the method
      *            calling this one will be gathered (must NOT be {@code null}
      */
     private void unsupportedMethod(StackTraceElement ste) {
         throw new UnsupportedOperationException(
                 ClearCaseUcmBaselineSCM.class.getName()
                 + " does not support the "
                 + ste.getMethodName()
                 + " method.");
     }
 
 }
