 package com.rightscale.ssh.launchers.unix;
 
 import com.rightscale.ssh.*;
 import com.rightscale.ssh.launchers.*;
 import com.rightscale.util.*;
 import java.io.*;
 
 public class GnomeTerminal extends SimpleUnixLauncher {
    public GnomeTerminal(Launchpad launchpad) {
        super(launchpad, "gnome-terminal --help", "gnome-terminal -x");
     }
 }
