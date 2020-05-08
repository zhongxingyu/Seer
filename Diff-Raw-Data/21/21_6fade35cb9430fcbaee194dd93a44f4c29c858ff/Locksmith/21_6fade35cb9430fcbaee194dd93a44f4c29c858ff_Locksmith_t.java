 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.maven.plugins.prerelease;
 
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.io.OS;
 import net.oneandone.sushi.util.Separator;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * Find an delete stale locks. JVM crashes or kill -9 does not properly cleanup locks. This goals repairs them by looking for locks
  * from processes that no longer exists.
  *
  * Note that this locking cleanup should run in a single, separate process to avoid running into its own locking problems. It
  * must not be included in the lock file creating itself because there are multiple processes involved.
  */
 @Mojo(name = "locksmith", requiresProject = false)
 public class Locksmith extends Base {
     /**
      * Fail when stale locks are detected?
      */
     @Parameter(property = "prerelease.locksmith.fail")
     private boolean fail = false;
 
     /**
      * Delete stale locks?
      */
     @Parameter(property = "prerelease.locksmith.delete")
     private boolean delete = false;
 
     private static final long COUNT = 3;
     private static final long HOUR = 1000L * 60 * 60;
 
     @Override
     public void doExecute() throws Exception {
         Map<String, Long> started;
         FileNode root;
         String pid;
         Long time;
         int errors;
         List<Node> locks;
 
         started = startedMap();
         errors = 0;
         root = world.file(archiveRoot);
         if (!root.exists()) {
             return;
         }
         locks = root.find("*/*.LOCK");
         for (Node file : locks) {
             pid = file.readString();
             if (pid.trim().isEmpty()) {
                 getLog().info(file + ": old lock file format");
                 if (System.currentTimeMillis() - file.getLastModified() < HOUR * COUNT) {
                     getLog().info("> younger than " + COUNT + " hours - considered ok");
                     continue;
                 }
             } else {
                 time = started.get(pid);
                 if (time == null) {
                     getLog().info(file + ": stale lock - no process with id " + pid);
                 } else if (file.getLastModified() < time) {
                     getLog().info(file + ": stale lock - process with id " + pid + " younger than lock file");
                 } else {
                     getLog().debug(file + " ok");
                     continue;
                 }
             }
             errors++;
             if (delete) {
                 getLog().info("deleting " + file);
                 file.deleteFile();
             }
         }
         getLog().info("locks checked: " + locks.size() + ", locks stale: " + errors);
         if (errors > 0 && fail) {
             throw new MojoExecutionException("stale locks: " + errors);
         }
     }
 
     private static final SimpleDateFormat TODAY = new SimpleDateFormat("HH:mm:ss");
     private static final SimpleDateFormat OTHER = new SimpleDateFormat("MMM dd", Locale.US);
    private static final SimpleDateFormat MAC = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US);
 
     private Map<String, Long> startedMap() throws IOException {
        String[] cmd;
         Map<String, Long> result;
         boolean first;
         int idx;
         String pid;
         String startedStr;
         Date startedDate;
 
         result = new HashMap<>();
         first = true;
        if (OS.CURRENT == OS.MAC) {
            cmd = new String[] { "ps", "ax", "-o", "pid", "-o", "lstart"};
        } else {
            cmd = new String[] { "ps", "ax", "--format", "pid,start" };
        }

        for (String line : Separator.RAW_LINE.split(((FileNode) world.getWorking()).exec(cmd))) {
             if (first) {
                 if (!line.contains("PID")) {
                     throw new IllegalStateException(line);
                 }
                 first = false;
             } else {
                 line = line.trim();
                 idx = line.indexOf(' ');
                 if (idx == -1) {
                     throw new IllegalStateException(line);
                 }
                 pid = line.substring(0, idx);
                 startedStr = line.substring(idx + 1).trim();
                 try {
                    if (OS.CURRENT == OS.MAC) {
                        startedDate = MAC.parse(startedStr);
                    } else {
                        startedDate = (startedStr.indexOf(':') == -1 ? OTHER: TODAY).parse(startedStr);
                    }
                 } catch (ParseException e) {
                     throw new IllegalStateException("invalid date in line " + line, e);
                 }
                 result.put(pid, startedDate.getTime());
             }
         }
         return result;
     }
 }
