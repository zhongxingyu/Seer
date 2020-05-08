 /*
  * Copyright 2008 Josh Kropf
  * 
  * This file is part of bb-ant-tools.
  * 
  * bb-ant-tools is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * bb-ant-tools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with bb-ant-tools; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package ca.slashdev.bb.tasks;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.types.Resource;
 import org.apache.tools.ant.types.ResourceCollection;
 import org.apache.tools.ant.types.resources.FileResource;
 import org.apache.tools.ant.util.FileUtils;
 import org.apache.tools.ant.util.ResourceUtils;
 
 import ca.slashdev.bb.types.OverrideType;
 import ca.slashdev.bb.util.Utils;
 
 /**
  * @author josh
  */
 public class JadtoolTask extends BaseTask {
    private File input;
    private File destDir;
    private Vector<ResourceCollection> resources = new Vector<ResourceCollection>();
    private Vector<OverrideType> overrides = new Vector<OverrideType>();
    private Map<String, OverrideType> overrideMap = new HashMap<String, OverrideType>();
    
    public void setInput(File input) {
       this.input = input;
    }
    
    public void setDestDir(File destDir) {
       this.destDir = destDir;
    }
    
    public void add(ResourceCollection res) {
       resources.add(res);
    }
    
    public void add(OverrideType override) {
       overrides.add(override);
    }
    
    @Override
    public void execute() throws BuildException {
       super.execute();
       
       if (input == null)
          throw new BuildException("input is a required attribute");
       
       if (!input.exists())
          throw new BuildException("input file is missing");
       
       if (resources.size() == 0)
          throw new BuildException("specify at least one cod file");
       
       if (destDir == null)
          throw new BuildException("destdir is a required attribute");
       
       if (!destDir.exists())
          if (!destDir.mkdirs())
             throw new BuildException("unable to create destination directory");
       
       for (OverrideType o : overrides) {
          o.validate();
          overrideMap.put(o.getKey().toLowerCase(), o);
       }
       
       executeRewrite();
    }
    
    @SuppressWarnings("unchecked")
    private void executeRewrite() {
       BufferedReader reader = null;
       PrintStream output = null;
       
       try {
          try {
             reader = new BufferedReader(new FileReader(input));
             output = new PrintStream(new File(destDir, input.getName()));
             
             int i, num = 0;
             String line, key, value;
             OverrideType override;
             
             while ((line = reader.readLine()) != null) {
                num ++;
                
                i = line.indexOf(':');
                if (i == -1)
                   throw new BuildException("unexpected line in jad file: "+num);
                
                key = line.substring(0, i);
               value = line.substring(i+1).trim();
                
                if (key.startsWith("RIM-COD-URL")
                      || key.startsWith("RIM-COD-SHA1")
                      || key.startsWith("RIM-COD-Size")) {
                   continue; // ignore line
                }
                
                // check for .jad element override, remove from map if found
                override = overrideMap.get(key.toLowerCase());
                if (override != null) {
                   value = override.getValue();
                   overrideMap.remove(key.toLowerCase());
                }
                
                output.printf("%s: %s\n", key, value);
             }
          } catch (IOException e) {
             throw new BuildException("error creating jad file", e);
          }
          
          try {
             int num = 0;
             File destFile;
             Resource r;
             
             for (ResourceCollection rc : resources) {
                Iterator<Resource> i = rc.iterator();
                while (i.hasNext()) {
                   r = i.next();
                   destFile = new File(destDir, Utils.getFilePart(r));
                   
                   if (Utils.isZip(r)) {
                      String[] zipEntries = Utils.extract(r, destDir);
                      
                      for (String entry : zipEntries) {
                         destFile = new File(destDir, entry);
                         
                         if (num == 0) {
                            output.printf("RIM-COD-URL: %s\n", destFile.getName());
                            output.printf("RIM-COD-SHA1: %s\n", Utils.getSHA1(destFile));
                            output.printf("RIM-COD-Size: %d\n", destFile.length());
                         } else {
                            output.printf("RIM-COD-URL-%d: %s\n", num, destFile.getName());
                            output.printf("RIM-COD-SHA1-%d: %s\n", num, Utils.getSHA1(destFile));
                            output.printf("RIM-COD-Size-%d: %d\n", num, destFile.length());
                         }
                         
                         num++;
                      }
                   } else {
                      ResourceUtils.copyResource(r, new FileResource(destFile));
                      
                      if (num == 0) {
                         output.printf("RIM-COD-URL: %s\n", destFile.getName());
                         output.printf("RIM-COD-SHA1: %s\n", Utils.getSHA1(destFile));
                         output.printf("RIM-COD-Size: %d\n", destFile.length());
                      } else {
                         output.printf("RIM-COD-URL-%d: %s\n", num, destFile.getName());
                         output.printf("RIM-COD-SHA1-%d: %s\n", num, Utils.getSHA1(destFile));
                         output.printf("RIM-COD-Size-%d: %d\n", num, destFile.length());
                      }
                      num ++;
                   }
                   
                }
             }
          } catch (IOException e) {
             throw new BuildException("error copying cod file", e);
          }
          
          // flush remaining overrides into target .jad
          for (OverrideType override : overrideMap.values()) {
              output.printf("%s: %s\n", override.getKey(), override.getValue());
          }
       } finally {
          FileUtils.close(reader);
          FileUtils.close(output);
       }
    }
 }
