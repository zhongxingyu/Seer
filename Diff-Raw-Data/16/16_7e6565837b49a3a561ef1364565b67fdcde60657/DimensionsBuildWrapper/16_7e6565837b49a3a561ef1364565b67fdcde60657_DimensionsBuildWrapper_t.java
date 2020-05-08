 
 /* ===========================================================================
  *  Copyright (c) 2007 Serena Software. All rights reserved.
  *
  *  Use of the Sample Code provided by Serena is governed by the following
  *  terms and conditions. By using the Sample Code, you agree to be bound by
  *  the terms contained herein. If you do not agree to the terms herein, do
  *  not install, copy, or use the Sample Code.
  *
  *  1.  GRANT OF LICENSE.  Subject to the terms and conditions herein, you
  *  shall have the nonexclusive, nontransferable right to use the Sample Code
  *  for the sole purpose of developing applications for use solely with the
  *  Serena software product(s) that you have licensed separately from Serena.
  *  Such applications shall be for your internal use only.  You further agree
  *  that you will not: (a) sell, market, or distribute any copies of the
  *  Sample Code or any derivatives or components thereof; (b) use the Sample
  *  Code or any derivatives thereof for any commercial purpose; or (c) assign
  *  or transfer rights to the Sample Code or any derivatives thereof.
  *
  *  2.  DISCLAIMER OF WARRANTIES.  TO THE MAXIMUM EXTENT PERMITTED BY
  *  APPLICABLE LAW, SERENA PROVIDES THE SAMPLE CODE AS IS AND WITH ALL
  *  FAULTS, AND HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EITHER
  *  EXPRESSED, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY
  *  IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, OF FITNESS FOR A
  *  PARTICULAR PURPOSE, OF LACK OF VIRUSES, OF RESULTS, AND OF LACK OF
  *  NEGLIGENCE OR LACK OF WORKMANLIKE EFFORT, CONDITION OF TITLE, QUIET
  *  ENJOYMENT, OR NON-INFRINGEMENT.  THE ENTIRE RISK AS TO THE QUALITY OF
  *  OR ARISING OUT OF USE OR PERFORMANCE OF THE SAMPLE CODE, IF ANY,
  *  REMAINS WITH YOU.
  *
  *  3.  EXCLUSION OF DAMAGES.  TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE
  *  LAW, YOU AGREE THAT IN CONSIDERATION FOR RECEIVING THE SAMPLE CODE AT NO
  *  CHARGE TO YOU, SERENA SHALL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER,
  *  INCLUDING BUT NOT LIMITED TO DIRECT, SPECIAL, INCIDENTAL, INDIRECT, OR
  *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR LOSS OF
  *  PROFITS OR CONFIDENTIAL OR OTHER INFORMATION, FOR BUSINESS INTERRUPTION,
  *  FOR PERSONAL INJURY, FOR LOSS OF PRIVACY, FOR NEGLIGENCE, AND FOR ANY
  *  OTHER LOSS WHATSOEVER) ARISING OUT OF OR IN ANY WAY RELATED TO THE USE
  *  OF OR INABILITY TO USE THE SAMPLE CODE, EVEN IN THE EVENT OF THE FAULT,
  *  TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, OR BREACH OF CONTRACT,
  *  EVEN IF SERENA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  THE
  *  FOREGOING LIMITATIONS, EXCLUSIONS AND DISCLAIMERS SHALL APPLY TO THE
  *  MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW.  NOTWITHSTANDING THE ABOVE,
  *  IN NO EVENT SHALL SERENA'S LIABILITY UNDER THIS AGREEMENT OR WITH RESPECT
  *  TO YOUR USE OF THE SAMPLE CODE AND DERIVATIVES THEREOF EXCEED US$10.00.
  *
  *  4.  INDEMNIFICATION. You hereby agree to defend, indemnify and hold
  *  harmless Serena from and against any and all liability, loss or claim
  *  arising from this agreement or from (i) your license of, use of or
  *  reliance upon the Sample Code or any related documentation or materials,
  *  or (ii) your development, use or reliance upon any application or
  *  derivative work created from the Sample Code.
  *
  *  5.  TERMINATION OF THE LICENSE.  This agreement and the underlying
  *  license granted hereby shall terminate if and when your license to the
  *  applicable Serena software product terminates or if you breach any terms
  *  and conditions of this agreement.
  *
  *  6.  CONFIDENTIALITY.  The Sample Code and all information relating to the
  *  Sample Code (collectively "Confidential Information") are the
  *  confidential information of Serena.  You agree to maintain the
  *  Confidential Information in strict confidence for Serena.  You agree not
  *  to disclose or duplicate, nor allow to be disclosed or duplicated, any
  *  Confidential Information, in whole or in part, except as permitted in
  *  this Agreement.  You shall take all reasonable steps necessary to ensure
  *  that the Confidential Information is not made available or disclosed by
  *  you or by your employees to any other person, firm, or corporation.  You
  *  agree that all authorized persons having access to the Confidential
  *  Information shall observe and perform under this nondisclosure covenant.
  *  You agree to immediately notify Serena of any unauthorized access to or
  *  possession of the Confidential Information.
  *
  *  7.  AFFILIATES.  Serena as used herein shall refer to Serena Software,
  *  Inc. and its affiliates.  An entity shall be considered to be an
  *  affiliate of Serena if it is an entity that controls, is controlled by,
  *  or is under common control with Serena.
  *
  *  8.  GENERAL.  Title and full ownership rights to the Sample Code,
  *  including any derivative works shall remain with Serena.  If a court of
  *  competent jurisdiction holds any provision of this agreement illegal or
  *  otherwise unenforceable, that provision shall be severed and the
  *  remainder of the agreement shall remain in full force and effect.
  * ===========================================================================
  */
 
 /**
  ** @brief This experimental plugin extends Hudson support for Dimensions SCM repositories
  **
  ** @author Tim Payne
  **
  **/
 
 package hudson.plugins.dimensionsscm;
 
 // Dimensions imports
 import hudson.plugins.dimensionsscm.DimensionsAPI;
 import hudson.plugins.dimensionsscm.DimensionsSCM;
 import hudson.plugins.dimensionsscm.Logger;
 import com.serena.dmclient.api.DimensionsResult;
 
 // Hudson imports
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor.FormException;
 import hudson.model.Descriptor;
 import hudson.model.Result;
 import hudson.tasks.BuildWrapper.Environment;
 import hudson.tasks.BuildWrapper;
 import hudson.tasks.BuildWrapperDescriptor;
 import hudson.util.FormValidation;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.util.Map;
 
 public class DimensionsBuildWrapper extends BuildWrapper {
 
    private DimensionsSCM scm = null;
 
     /**
      * Descriptor should be singleton.
      */
     public Descriptor<BuildWrapper> getDescriptor() {
         return DMWBLD_DESCRIPTOR;
     }
 
     @Extension
     public static final DescriptorImpl DMWBLD_DESCRIPTOR = new DescriptorImpl();
 
 
     /**
      * Default constructor.
      */
     @DataBoundConstructor
     public DimensionsBuildWrapper() {
     }
 
     /**
      * Default environment setup.
      */
     @Override
     public Environment setUp(final AbstractBuild build, Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
         long key=-1;
         if (build.getProject().getScm() instanceof DimensionsSCM) {
             Logger.Debug("Invoking build setup callout " + this.getClass().getName());
             if (scm == null)
                 scm = (DimensionsSCM)build.getProject().getScm();
             Logger.Debug("Dimensions user is "+scm.getJobUserName()+" , Dimensions installation is "+scm.getJobServer());
             try {
                 key = scm.getAPI().login(scm.getJobUserName(),
                                        scm.getJobPasswd(),
                                        scm.getJobDatabase(),
                                        scm.getJobServer());
                 if (key>0)
                 {
                     DimensionsResult res = scm.getAPI().lockProject(key,scm.getProject());
                     if (res==null) {
                         listener.getLogger().println("[DIMENSIONS] Locking the project in Dimensions failed");
                         build.setResult(Result.FAILURE);
                         listener.getLogger().flush();
                     }
                     else {
                         listener.getLogger().println("[DIMENSIONS] Dimensions project was successfully locked");
                         listener.getLogger().flush();
                     }
                 }
             } catch(Exception e) {
                 listener.fatalError("Unable to lock Dimensions project - " + e.getMessage());
             }
             finally
             {
                 scm.getAPI().logout(key);
             }
         } else {
             listener.fatalError("[DIMENSIONS] This plugin only works with a Dimensions SCM engine");
             build.setResult(Result.FAILURE);
             throw new IOException("[DIMENSIONS] This plugin only works with a Dimensions SCM engine");
         }
 
         return new EnvironmentImpl(build);
     }
 
 
     /*
      * Implementation class for Dimensions plugin
      */
     public static final class DescriptorImpl extends BuildWrapperDescriptor {
 
         /*
          * Loads the descriptor
          */
         public DescriptorImpl() {
             super(DimensionsBuildWrapper.class);
             load();
             Logger.Debug("Loading " + this.getClass().getName());
         }
 
         public String getDisplayName() {
             return "Lock Dimensions project while the build is in progress";
         }
 
 
         /*
          *  This builder can be used with all project types
          */
         @Override
         public boolean isApplicable(AbstractProject<?,?> item) {
             return true;
         }
 
         /*
          * Save the descriptor configuration
          */
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             req.bindParameters(this,"DimensionsBuildWrapper");
             save();
             return true;
         }
 
         /*
          * Get help file
          */
         @Override
         public String getHelpFile() {
             return "/plugin/dimensionsscm/helpbwrapper.html";
         }
     }
 
     /*
      * Implementation class for Dimensions environment plugin
      */
     class EnvironmentImpl extends Environment {
 
         AbstractBuild<?,?> elbuild;
 
         /**
          * Default constructor.
          */
         EnvironmentImpl(AbstractBuild<?,?> build) {
             this.elbuild = build;
         }
 
         /**
          * Build environment
          */
         @Override
         public void buildEnvVars(Map<String, String> env) {
         }
 
         /**
          * Post build step - always called
          */
         @Override
         public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException {
             long key=-1;
             if (scm != null) {
                 Logger.Debug("Invoking build tearDown callout " + this.getClass().getName());
                 Logger.Debug("Dimensions user is "+scm.getJobUserName()+" , Dimensions installation is "+scm.getJobServer());
                 try {
                     key = scm.getAPI().login(scm.getJobUserName(),
                                            scm.getJobPasswd(),
                                            scm.getJobDatabase(),
                                            scm.getJobServer());
                     if (key>0)
                     {
                         Logger.Debug("Unlocking the project");
                         DimensionsResult res = scm.getAPI().unlockProject(key,scm.getProject());
                         if (res==null) {
                             listener.getLogger().println("[DIMENSIONS] Unlocking the project in Dimensions failed");
                             build.setResult(Result.FAILURE);
                             listener.getLogger().flush();
                         }
                         else {
                             listener.getLogger().println("[DIMENSIONS] Dimensions project was successfully unlocked");
                             listener.getLogger().flush();
                         }
                     }
                     else {
                         listener.fatalError("[DIMENSIONS] Login to Dimensions failed.");
                         build.setResult(Result.FAILURE);
                         return false;
                     }
                 } catch(Exception e) {
                     listener.fatalError("Unable to unlock Dimensions project - " + e.getMessage());
                     build.setResult(Result.FAILURE);
                     return false;
                 }
                 finally
                 {
                     scm.getAPI().logout(key);
                 }
             }
             return true;
         }
     }
 }
