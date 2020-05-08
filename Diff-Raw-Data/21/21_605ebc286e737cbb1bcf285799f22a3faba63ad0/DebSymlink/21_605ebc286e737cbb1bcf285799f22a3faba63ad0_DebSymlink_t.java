 /*
  * Copyright (c) KloudTek Ltd 2012.
  */
 
 package com.kloudtek.buildmagic.tools.createinstaller.deb;
 
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.types.Path;
 
 public class DebSymlink extends Path {
    private String target;
 
     public DebSymlink(Project p, String path) {
         super(p, path);
     }
 
     public DebSymlink(Project project) {
         super(project);
     }
 
    public String getTarget() {
        return target;
     }
 
    public void setTarget(String target) {
        this.target = target;
     }
 }
