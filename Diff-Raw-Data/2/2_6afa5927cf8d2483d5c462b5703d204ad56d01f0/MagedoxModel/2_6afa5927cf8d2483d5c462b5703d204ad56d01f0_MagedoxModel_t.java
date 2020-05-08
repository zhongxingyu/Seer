 package com.kodokux.magedox;
 
 import com.intellij.openapi.Disposable;
 import com.intellij.openapi.project.Project;
 
 /**
  * Created with IntelliJ IDEA.
  * User: johna
  * Date: 13/04/27
  * Time: 2:15
  * To change this template use File | Settings | File Templates.
  */
 public class MagedoxModel implements Disposable {
     private Project project;
 
     public MagedoxModel(Project project) {
         this.project = project;
     }
 
     @Override
     public void dispose() {
        System.out.println("MagedoxModel::dispose  3");
     }
 
     public Project getProject () {
         return this.project;
     }
 }
