 package com.controlgroup.ci.jenkinsgithubcommitstatus;
 
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.util.ArgumentListBuilder;
 
 import java.io.IOException;
 
 
 /**
  * User: vikash
  * Date: 4/28/13
  * Time: 6:11 PM
  */
 public class CurlImpl implements IGithubStatusAPI {
                                           // /repos/:owner/:repo/statuses/:sha
     private static final String API_URL = "https://api.github.com/repos/%s/%s/statuses/%s";
 
     public void doPost(BuildListener listener, Launcher launcher, Notification post) {
        //build argument list for launcher
         ArgumentListBuilder args = new ArgumentListBuilder();
         //determine os
         if (launcher.isUnix()) {
             //use curl
             args.add("curl");
             //set auth token in header
             args.add("-H");
            args.add(String.format("\"Authorization: token %s\"",post.getAuthToken()));
             //set status data
             args.add("--data"); //--data '{"state":"success"}'
            args.add(String.format("'{\"state\":\"%s\"}'",post.getBuildResult()));
         } else {
             //TODO WINDOWS IMPL OF CURL
         }
 
         //setup API URL
         //POST /repos/:owner/:repo/statuses/:sha
         args.add(String.format(API_URL,post.getRepoOwner(),post.getRepoName(),post.getCommitSha()));
 
         //launch command
         try {
             int r;
             r = launcher.launch().cmds(args).stdout(listener).join();
             if (r != 0) {
                 listener.finished(Result.FAILURE);
             }else {
                 listener.finished(Result.SUCCESS);
             }
 
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             listener.finished(Result.FAILURE);
         } catch (InterruptedException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             listener.finished(Result.FAILURE);
         }
 
     }
 }
