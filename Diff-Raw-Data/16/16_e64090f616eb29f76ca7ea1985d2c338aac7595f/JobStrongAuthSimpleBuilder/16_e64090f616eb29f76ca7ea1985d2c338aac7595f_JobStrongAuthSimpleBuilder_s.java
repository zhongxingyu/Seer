 /**
  * The MIT License
  * 
  * Copyright (C) 2012 KK.Kon
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
 package org.jenkinsci.plugins.job_strongauth_simple;
 
 import hudson.Launcher;
 import hudson.Extension;
 import hudson.util.FormValidation;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.AbstractProject;
 import hudson.model.Cause;
 import hudson.model.Result;
 import hudson.model.Run;
 import hudson.model.User;
 import hudson.tasks.Builder;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.triggers.TimerTrigger;
 import hudson.util.RunList;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.QueryParameter;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * Sample {@link Builder}.
  *
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
  * and a new {@link HelloWorldBuilder} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link #name})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
  * method will be invoked. 
  *
  * @author KK.Kon
  */
 public class JobStrongAuthSimpleBuilder extends Builder {
 
     // default expireTimeInHours
     private static final int DEFAULT_EXPIRE_TIME_IN_HOURS = 16;
 
     private Integer getEffectiveExpireTime( final Integer jobValue, final Integer globalValue )
     {
         if ( null == jobValue && null == globalValue )
         {
             return DEFAULT_EXPIRE_TIME_IN_HOURS;
         }
 
         Integer value = null;
         {
             if ( null != globalValue )
             {
                 value = globalValue;
             }
 
             if ( null != jobValue )
             {
                 value = jobValue;
             }
         }
         return value;
     }
 
     private Set<String> makeAuthUsers( final String jobUsers, final boolean useGlobalUsers, final String globalUsers )
     {
         if ( null == jobUsers && null == globalUsers )
         {
             return Collections.emptySet();
         }
 
         Set<String> set = new HashSet<String>();
         if ( null != jobUsers )
         {
             final String[] users = jobUsers.split(",");
             if ( null != users )
             {
                 for ( final String userRaw : users )
                 {
                     if ( null != userRaw )
                     {
                         final String userTrimed = userRaw.trim();
                         if ( null != userTrimed )
                         {
                             set.add( userTrimed );
                         }
                     }
                 }
             }
         }
         if ( useGlobalUsers )
         {
             final String[] users = globalUsers.split(",");
             if ( null != users )
             {
                 for ( final String userRaw : users )
                 {
                     if ( null != userRaw )
                     {
                         final String userTrimed = userRaw.trim();
                         if ( null != userTrimed )
                         {
                             set.add( userTrimed );
                         }
                     }
                 }
             }
         }
 
         return set;
     }
 
 
     private String getUserNameFromCause( final Cause cause )
     {
         // UserCause deprecated 1.428
 
         Class<?> clazz = null;
         Object retval = null;
         {
             Method method = null;
             try
             {
                 clazz = Class.forName("hudson.model.Cause$UserIdCause");
             }
             catch ( ClassNotFoundException e )
             {
 
             }
 
             if ( null != clazz )
             {
                 try
                 {
                     method = cause.getClass().getMethod( "getUserName", new Class<?>[]{} );
                 }
                 catch ( SecurityException e)
                 {
                     
                 }
                 catch ( NoSuchMethodException e )
                 {
                     
                 }
 
                 if ( null != method )
                 {
                     try
                     {
                         retval = method.invoke( cause, new Object[]{} );
                     }
                     catch ( IllegalAccessException e )
                     {
                         
                     }
                     catch ( IllegalArgumentException e )
                     {
                         
                     }
                     catch ( InvocationTargetException e )
                     {
                         
                     }
                 }
             }
         }
         if ( null != retval )
         {
             if ( retval instanceof String )
             {
                 return (String)retval;
             }
         }
 
         {
             Method method = null;
             try
             {
                 clazz = Class.forName("hudson.model.Cause$UserCause");
             }
             catch ( ClassNotFoundException e )
             {
 
             }
 
             if ( null != clazz )
             {
                 try
                 {
                     method = cause.getClass().getMethod( "getUserName", new Class<?>[]{} );
                 }
                 catch ( SecurityException e)
                 {
                     
                 }
                 catch ( NoSuchMethodException e )
                 {
                     
                 }
 
                 if ( null != method )
                 {
                     try
                     {
                         retval = method.invoke( cause, new Object[]{} );
                     }
                     catch ( IllegalAccessException e )
                     {
                         
                     }
                     catch ( IllegalArgumentException e )
                     {
                         
                     }
                     catch ( InvocationTargetException e )
                     {
                         
                     }
                 }
             }
         }
         if ( null != retval )
         {
             if ( retval instanceof String )
             {
                 return (String)retval;
             }
         }
 
         LOGGER.severe( "unknown cause" );
         return null;
     }
 
     private Cause getCauseFromRun( final Run run )
     {
         if ( null == run )
         {
             return null;
         }
 
         Class<?> clazz = null;
         {
             try
             {
                 clazz = Class.forName("hudson.model.Cause$UserIdCause");
                 if ( null != clazz )
                 {
                     // getCause since 1.362
                     final Cause cause = run.getCause( clazz );
                     if ( null != cause )
                     {
                         return cause;
                     }
                 }
             }
             catch ( ClassNotFoundException e )
             {
 
             }
 
         }
         {
             try
             {
                 clazz = Class.forName("hudson.model.Cause$UserCause");
                 if ( null != clazz )
                 {
                     final Cause cause = run.getCause( clazz );
                     if ( null != cause )
                     {
                         return cause;
                     }
                 }
             }
             catch ( ClassNotFoundException e )
             {
 
             }
 
         }
 
         return null;
     }
 
     private final String jobUsers;
     private final boolean useGlobalUsers;
     private final Integer jobMinAuthUserNum;
    private final boolean useExpireTime;
     private final Integer jobExpireTimeInHours;
     //private final boolean buildKickByTimerTrigger;
 
     // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
     @DataBoundConstructor
     public JobStrongAuthSimpleBuilder(
             final String jobUsers
             , final boolean useGlobalUsers
             , final Integer jobMinAuthUserNum
            , final boolean useExpireTime
             , final Integer jobExpireTimeInHours
 //            , final boolean buildKickByTimerTrigger
             )
     {
         this.jobUsers = jobUsers;
         this.useGlobalUsers = useGlobalUsers;
         this.jobMinAuthUserNum = jobMinAuthUserNum;
        this.useExpireTime = useExpireTime;
         this.jobExpireTimeInHours = jobExpireTimeInHours;
         //this.buildKickByTimerTrigger = buildKickByTimerTrigger;
     }
 
     /**
      * We'll use this from the <tt>config.jelly</tt>.
      */
     public String getJobUsers() {
         return jobUsers;
     }
     public boolean getUseGlobalUsers() {
         return useGlobalUsers;
     }
     public Integer getJobMinAuthUserNum() {
         return jobMinAuthUserNum;
     }
    public boolean getUseExpireTime() {
        return useExpireTime;
     }
     public Integer getJobExpireTimeInHours() {
         return jobExpireTimeInHours;
     }
 //    public Integer getBuildKickByTimerTrigger() {
 //        return buildKickByTimerTrigger;
 //    }
 
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
         // This is where you 'build' the project.
 
         final PrintStream log = listener.getLogger();
         {
             final Cause cause = getCauseFromRun( build );
             if ( null == cause )
             {
                 log.println( "internal error. getCauseFromRun failed. Caused by `" + getDescriptor().getDisplayName() + "`" );
                 build.setResult(Result.FAILURE);
                 return false;
             }
             else
             {
                 final String userName = getUserNameFromCause(cause);
                 log.println( "userName=" + userName );
 
                 if ( null == userName )
                 {
                     log.println( "internal error. getUserNameFromCause failed. Caused by `" + getDescriptor().getDisplayName() + "`" );
                     build.setResult(Result.FAILURE);
                     return false;
                 }
                 else
                 {
                     if ( 0 < userName.length() )
                     {
                         if ( 0 == "anonymous".compareToIgnoreCase(userName) )
                         {
                             build.setResult( Result.ABORTED );
                             log.println( "reject `anonymous` user's build. Caused by `" + getDescriptor().getDisplayName() + "`" );
                             return false;
                         }
                     }
                 }
             }
         }
 
         boolean currentBuildCauseByTimerTrigger = false;
         {
             final Cause cause = build.getCause(TimerTrigger.TimerTriggerCause.class);
             if ( null != cause )
             {
                 if ( cause instanceof TimerTrigger.TimerTriggerCause )
                 {
                     final TimerTrigger.TimerTriggerCause causeTimer = (TimerTrigger.TimerTriggerCause)cause;
 
                     if ( null != causeTimer )
                     {
                         currentBuildCauseByTimerTrigger = true;
                     }
                 }
             }
         }
 
         final Calendar calStart = Calendar.getInstance();
 
         // This also shows how you can consult the global configuration of the builder
         final Integer expireTimeInHours = getEffectiveExpireTime( jobExpireTimeInHours, getDescriptor().getExpireTimeInHours() );
         LOGGER.finest("expireTimeInHours="+expireTimeInHours+".");
         final Set<String> authUsers = makeAuthUsers( this.jobUsers, this.useGlobalUsers, getDescriptor().getUsers() );
         LOGGER.finest( "authUsers=" + authUsers );
 
 
         build.setResult(Result.SUCCESS);
 
         RunList runList = build.getProject().getBuilds();
         LOGGER.finest( "runList=" + runList );
         final int currentNumber = build.getNumber();
 
         final Set<String> listUsers = new HashSet<String>();
         Calendar calLastBuild = calStart;
         for ( final Object r : runList )
         {
             if ( null == r )
             {
                 continue;
             }
 
             if ( r instanceof Run )
             {
                 final Run run = (Run)r;
                 LOGGER.finest("run: " + run );
 
                 /*
                 // skip current build
                 if ( currentNumber <= run.getNumber() )
                 {
                     log.println( "skip current." );
                     continue;
                 }
                 */
 
                 final Result result = run.getResult();
                 LOGGER.finest( " result: " + result );
 
                 if ( Result.SUCCESS.ordinal == result.ordinal )
                 {
                     if ( run.getNumber() < currentNumber )
                     {
                         break;
                     }
                 }
 
                 final Calendar calRun = run.getTimestamp();
                if ( this.getUseExpireTime() )
                 {
                     final long lDistanceInMillis = calLastBuild.getTimeInMillis() - calRun.getTimeInMillis();
                     final long lDistanceInSeconds = lDistanceInMillis / (1000);
                     final long lDistanceInMinutes = lDistanceInSeconds / (60);
                     final long lDistanceInHours = lDistanceInMinutes / (60);
 
                     LOGGER.finest( " lDistanceInHours=" + lDistanceInHours );
                     if ( expireTimeInHours < lDistanceInHours )
                     {
                         LOGGER.finest( " expireTimeInHours=" + expireTimeInHours + ",lDistanceInHours=" + lDistanceInHours );
                         break;
                     }
                 }
 
                 final Cause cause = getCauseFromRun( run );
                 if ( null == cause )
                 {
                     log.println( "internal error. getCauseFromRun failed. Caused by `" + getDescriptor().getDisplayName() + "`" );
                     build.setResult(Result.FAILURE);
                     return false;
                 }
                 else
                 {
                     final String userName = getUserNameFromCause(cause);
                     LOGGER.finest( " usercause:" + userName );
 
                     if ( null == userName )
                     {
                         log.println( "internal error. getUserNameFromCause failed. Caused by `" + getDescriptor().getDisplayName() + "`" );
                         build.setResult(Result.FAILURE);
                         return false;
                     }
                     else
                     {
                         if ( 0 < userName.length() )
                         {
                             if ( 0 != "anonymous".compareToIgnoreCase(userName) )
                             {
                                 listUsers.add( userName );
 
                                 if ( authUsers.contains( userName ) )
                                 {
                                     calLastBuild = run.getTimestamp();
                                 }
                             }
                         }
                     }
                 }
             }
         } // for RunList
 
         LOGGER.finest( "listUsers=" + listUsers );
         
         boolean strongAuth = false;
         {
             int count = 0;
 
             for ( Iterator<String> it = listUsers.iterator(); it.hasNext(); )
             {
                 final String user = it.next();
                 if ( null != user )
                 {
                     if ( authUsers.contains( user ) )
                     {
                         count += 1;
                     }
                 }
             }
             LOGGER.finest( "count=" + count );
 
             if ( null == jobMinAuthUserNum )
             {
                 final int authUserCount = authUsers.size();
                 LOGGER.finest( "authUserCount=" + authUserCount );
                 if ( authUserCount <= count )
                 {
                     strongAuth = true;
                 }
             }
             else
             {
                 LOGGER.finest( "jobMinAuthUserNum=" + jobMinAuthUserNum );
                 if ( jobMinAuthUserNum.intValue() <= count )
                 {
                     strongAuth = true;
                 }
             }
         }
 
         if ( strongAuth )
         {
             boolean doBuild = false;
             {
 //                if ( buildKickByTimerTrigger )
 //                {
 //                    if ( currentBuildCauseByTimerTrigger )
 //                    {
 //                        // no build
 //                    }
 //                    else
 //                    {
 //                        doBuild = true;
 //                    }
 //                }
 //                else
                 {
                     doBuild = true;
                 }
             }
 
             if ( doBuild )
             {
                 build.setResult(Result.SUCCESS);
                 return true;
             }
         }
 
         log.println( "stop build. number of authed people does not satisfy. Caused by `" + getDescriptor().getDisplayName() + "`" );
         build.setResult(Result.NOT_BUILT);
         return false;
     }
 
     // Overridden for better type safety.
     // If your plugin doesn't really define any property on Descriptor,
     // you don't have to do this.
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl)super.getDescriptor();
     }
 
     /**
      * Descriptor for {@link JobStrongAuthSimpleBuilder}. Used as a singleton.
      * The class is marked as public so that it can be accessed from views.
      *
      * <p>
      * See <tt>src/main/resources/hudson/plugins/job_strongauth_simple/JobStrongAuthSimpleBuilder/*.jelly</tt>
      * for the actual HTML fragment for the configuration screen.
      */
     @Extension // This indicates to Jenkins that this is an implementation of an extension point.
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
         /**
          * To persist global configuration information,
          * simply store it in a field and call save().
          *
          * <p>
          * If you don't want fields to be persisted, use <tt>transient</tt>.
          */
         private Integer expireTimeInHours;
         
         private String users;
 
         public DescriptorImpl() {
             load();
             {
                 final Collection<User> userAll = User.getAll();
                 for ( final User user : userAll )
                 {
                     LOGGER.finest( "Id:          " + user.getId() );
                     LOGGER.finest( "DisplayName: " + user.getDisplayName() );
                     LOGGER.finest( "FullName:    " + user.getFullName() );
                 }
             }
         }
 
         /**
          * Performs on-the-fly validation of the form field 'users'.
          *
          * @param value
          *      This parameter receives the value that the user has typed.
          * @return
          *      Indicates the outcome of the validation. This is sent to the browser.
          */
         public FormValidation doCheckUsers(@QueryParameter String value)
                 throws IOException, ServletException {
             if ( 0 == value.length() )
             {
                 return FormValidation.ok();
             }
             
             final String invalidUser = checkUsers( value );
             if ( null != invalidUser )
             {
                 return FormValidation.error("Invalid user: " + invalidUser );
             }
 
             return FormValidation.ok();
         }
 
         /**
          * Performs on-the-fly validation of the form field 'expireTimeInHours'.
          *
          * @param value
          *      This parameter receives the value that the user has typed.
          * @return
          *      Indicates the outcome of the validation. This is sent to the browser.
          */
         public FormValidation doCheckExpireTimeInHours(@QueryParameter String value)
                 throws IOException, ServletException {
 
 //            if (value.length() == 0)
 //                return FormValidation.warning("Please set expire time");
             if ( 0 == value.length() )
             {
                 return FormValidation.ok();
             }
 
             try
             {
                 int intValue = Integer.parseInt(value);
                 if ( intValue < 0 )
                 {
                     return FormValidation.error("Please set positive value");
                 }
             }
             catch ( NumberFormatException e )
             {
                 return FormValidation.error("Please set numeric value");
             }
             return FormValidation.ok();
         }
 
         /**
          * Performs on-the-fly validation of the form field 'jobExpireTimeInHours'.
          *
          * @param value
          *      This parameter receives the value that the user has typed.
          * @return
          *      Indicates the outcome of the validation. This is sent to the browser.
          */
         public FormValidation doCheckJobExpireTimeInHours(@QueryParameter String value)
                 throws IOException, ServletException {
 
 //            if (value.length() == 0)
 //                return FormValidation.warning("Please set expire time");
             if ( 0 == value.length() )
             {
                 return FormValidation.ok();
             }
 
             try
             {
                 int intValue = Integer.parseInt(value);
                 if ( intValue < 0 )
                 {
                     return FormValidation.error("Please set positive value");
                 }
             }
             catch ( NumberFormatException e )
             {
                 return FormValidation.error("Please set numeric value");
             }
             return FormValidation.ok();
         }
 
         /**
          * Performs on-the-fly validation of the form field 'jobUsers'.
          *
          * @param value
          *      This parameter receives the value that the user has typed.
          * @return
          *      Indicates the outcome of the validation. This is sent to the browser.
          */
         public FormValidation doCheckJobUsers(@QueryParameter String value)
                 throws IOException, ServletException {
 
             if ( 0 == value.length() )
             {
                 return FormValidation.ok();
             }
             
             final String invalidUser = checkUsers( value );
             if ( null != invalidUser )
             {
                 return FormValidation.error("Invalid user@job: " + invalidUser );
             }
 
             return FormValidation.ok();
         }
         /**
          * Performs on-the-fly validation of the form field 'jobMinAuthUserNum'.
          *
          * @param value
          *      This parameter receives the value that the user has typed.
          * @return
          *      Indicates the outcome of the validation. This is sent to the browser.
          */
         public FormValidation doCheckJobMinAuthUserNum(@QueryParameter String value)
                 throws IOException, ServletException {
 
             if ( 0 == value.length() )
             {
                 return FormValidation.ok();
             }
 
             try
             {
                 int intValue = Integer.parseInt(value);
                 if ( intValue < 0 )
                 {
                     return FormValidation.error("Please set positive value");
                 }
             }
             catch ( NumberFormatException e )
             {
                 return FormValidation.error("Please set numeric value");
             }
 
             return FormValidation.ok();
         }
 
 
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             // Indicates that this builder can be used with all kinds of project types 
             return true;
         }
 
         /**
          * This human readable name is used in the configuration screen on job.
          */
         public String getDisplayName() {
             return "StrongAuthSimple for Job";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             // To persist global configuration information,
             // set that to properties and call save().
 
             //expireTimeInHours = formData.getInt("expireTimeInHours"); // invoke 500 error
             if ( formData.containsKey("expireTimeInHours") )
             {
                 final String value = formData.getString("expireTimeInHours");
                 if ( null != value )
                 {
                     if ( 0 < value.length() )
                     {
                         try
                         {
                             expireTimeInHours = Integer.parseInt(value);
                         }
                         catch ( NumberFormatException e )
                         {
                             LOGGER.warning( e.toString() );
                             return false;
                         }
                     }
                 }
             }
             if ( formData.containsKey("users") )
             {
                 users = formData.getString("users");
             }
             // ^Can also use req.bindJSON(this, formData);
             //  (easier when there are many fields; need set* methods for this, like setUseFrench)
             save();
             return super.configure(req,formData);
         }
 
         /**
          * This method returns true if the global configuration says we should speak French.
          *
          * The method name is bit awkward because global.jelly calls this method to determine
          * the initial state of the checkbox by the naming convention.
          */
         public Integer getExpireTimeInHours() {
             return expireTimeInHours;
         }
         
         public String getUsers() {
             return users;
         }
         
         public String checkUsers( final String value )
         {
             if ( null == value )
             {
                 return null;
             }
 
             final Collection<User> userAll = User.getAll();
 
             String invalidUser = null;
             if ( null != userAll )
             {
                 final String[] inputUsers = value.split(",");
                 if ( null == inputUsers )
                 {
                     final String userTrimed = value.trim();
 
                     boolean validUser = false;
                     for ( final User user : userAll )
                     {
                         if ( null != user )
                         {
                             final String dispName = user.getDisplayName();
                             if ( null != dispName )
                             {
                                 if ( 0 == userTrimed.compareTo(dispName) )
                                 {
                                     validUser = true;
                                     break;
                                 }
                             }
                         }
                     } // for userAll
 
                     if ( validUser )
                     {
                         // nothing
                     }
                     else
                     {
                         invalidUser = userTrimed;
                     }
                 }
                 else
                 {
                     for ( final String userRaw : inputUsers )
                     {
                         if ( null != userRaw )
                         {
                             final String userTrimed = userRaw.trim();
 
                             boolean validUser = false;
                             for ( final User user : userAll )
                             {
                                 if ( null != user )
                                 {
                                     final String dispName = user.getDisplayName();
                                     if ( null != dispName )
                                     {
                                         if ( 0 == userTrimed.compareTo(dispName) )
                                         {
                                             validUser = true;
                                             break;
                                         }
                                     }
                                 }
                             } // for userAll
 
                             if ( validUser )
                             {
                                 // nothing
                             }
                             else
                             {
                                 invalidUser = userTrimed;
                             }
 
                             if ( null != invalidUser )
                             {
                                 break;
                             }
                         }
                     }
                 }
             }
 
             return invalidUser;
         }
     }
 
     private static final Logger LOGGER = Logger.getLogger(JobStrongAuthSimpleBuilder.class.getName());
 }
 
