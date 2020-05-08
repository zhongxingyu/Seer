 package com.frugs.filesync.local;
 
 import com.frugs.filesync.domain.Diff;
 import com.frugs.filesync.local.system.SystemCommandFacade;
 
 import java.io.IOException;
 
 public class FileUpdateFacade {
     private final SystemCommandFacade systemCommandFacade;
 
     public FileUpdateFacade(SystemCommandFacade systemCommandFacade) {
         this.systemCommandFacade = systemCommandFacade;
     }
 
     public Diff getCurrentState() throws IOException {
         return systemCommandFacade.gitDiffHead();
     }
 
     public Diff interDiff(Diff first, Diff second) throws IOException {
         Diff result;
 
         if (first.isEmpty()) {
             result = second;
         } else {
             result = systemCommandFacade.interDiff(first, second);
         }
 
         return result;
     }
 
     public void applyDiff(Diff diff) throws IOException {
         if (diff.hasChanges()) {
             systemCommandFacade.gitApply(diff);
         }
     }
 }
