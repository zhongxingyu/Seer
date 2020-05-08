 /*
  * Copyright (C) 2012 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jvss.dump;
 
 import org.jvss.logical.VssFile;
 import org.jvss.logical.VssProject;
 import org.jvss.logical.VssRevision;
 
 import java.io.PrintStream;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Dumps the VSS project/file hierarchy to a text writer.
  */
 public class TreeDumper
 {
 
    private final PrintStream writer;
 
    private final Set<String> physicalNames;
 
    private boolean includeRevisions;
 
    public TreeDumper(PrintStream writer)
    {
       this.writer = writer;
       this.physicalNames = new HashSet<String>();
    }
 
    /**
     * @return the includeRevisions
     */
    public boolean isIncludeRevisions()
    {
       return includeRevisions;
    }
 
    /**
     * @param includeRevisions
     *           the includeRevisions to set
     */
    public void setIncludeRevisions(boolean includeRevisions)
    {
       this.includeRevisions = includeRevisions;
    }
 
    /**
     * @return the writer
     */
    public PrintStream getWriter()
    {
       return writer;
    }
 
    /**
     * @return the physicalNames
     */
    public Set<String> getPhysicalNames()
    {
       return physicalNames;
    }
 
    public void dumpProject(VssProject project)
    {
       dumpProject(project, 0);
    }
 
    public void dumpProject(VssProject project, int indent)
    {
       //var indentStr = new string(' ', indent);
       StringBuilder indentBuilder = new StringBuilder();
       for (int i = 0; i < indent; i++)
       {
          indentBuilder.append(' ');
       }
       String indentStr = indentBuilder.toString();
       physicalNames.add(project.getPhysicalName());
       writer.printf("%s%s/ (%s)", indentStr, project.getName(), project.getPhysicalName());
       writer.println();
 
       for (VssProject subproject : project.getProjects())
       {
          dumpProject(subproject, indent + 2);
       }
 
       for (VssFile file : project.getFiles())
       {
          physicalNames.add(file.getPhysicalName());
         writer
            .format("{0}  {1} ({2}) - {3}", indentStr, file.getName(), file.getPhysicalName(), file.GetPath(project));
          writer.println();
          if (includeRevisions)
          {
             for (VssRevision version : file.getRevisions())
             {
               writer.format("{0}    #{1} {2} {3}", indentStr, version.getVersion(), version.getUser(),
                  version.getDate());
                writer.println();
             }
          }
       }
    }
 }
