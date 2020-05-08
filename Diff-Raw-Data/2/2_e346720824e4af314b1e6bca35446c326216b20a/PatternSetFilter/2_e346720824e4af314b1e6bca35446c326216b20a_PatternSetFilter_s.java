 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003 - 2005  Lars Khne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //////////////////////////////////////////////////////////////////////////////
 
 package net.sf.clirr.ant;
 
 import net.sf.clirr.core.ClassFilter;
 
 import java.util.List;
 import java.io.File;
 
 import org.apache.bcel.classfile.JavaClass;
 import org.apache.tools.ant.types.selectors.SelectorUtils;
 import org.apache.tools.ant.types.PatternSet;
 import org.apache.tools.ant.Project;
 
 /**
  * A ClassFilter that uses Ant PatternSets as the decision criteria.
  *
  * @author lkuehne
  */
 class PatternSetFilter implements ClassFilter
 {
     private final Project project;
     private final List patternSets;
 
 
     /**
      * Creates a new PatternSetFilter.
      * @param project the current Ant project
      * @param patternSets a List of Ant PatternSet objects
      */
     public PatternSetFilter(Project project, List patternSets)
     {
         this.project = project;
         this.patternSets = patternSets;
     }
 
 
     public boolean isSelected(JavaClass clazz)
     {
         // The patternset evaluation code below was copied from Apache Ant's Expand task.
         // I feel this code should be available as a library function inside Ant somewhere...
         String className = clazz.getClassName();
        String name = className.replace('.', '/');
 
 
         if (patternSets == null || patternSets.isEmpty())
         {
             return true;
         }
 
         boolean included = false;
         for (int i = 0; i < patternSets.size(); i++)
         {
             PatternSet p = (PatternSet) patternSets.get(i);
             p.getIncludePatterns(project);
 
             String[] incls = p.getIncludePatterns(project);
             if (incls == null || incls.length == 0)
             {
                 // no include pattern implicitly means includes="**"
                 incls = new String[] {"**"};
             }
 
             for (int w = 0; w < incls.length; w++)
             {
                 String pattern = incls[w].replace('/', File.separatorChar)
                         .replace('\\', File.separatorChar);
                 if (pattern.endsWith(File.separator))
                 {
                     pattern += "**";
                 }
 
                 included = SelectorUtils.matchPath(pattern, name);
                 if (included)
                 {
                     break;
                 }
             }
 
             if (!included)
             {
                 break;
             }
 
 
             String[] excls = p.getExcludePatterns(project);
             if (excls != null)
             {
                 for (int w = 0; w < excls.length; w++)
                 {
                     String pattern = excls[w]
                             .replace('/', File.separatorChar)
                             .replace('\\', File.separatorChar);
                     if (pattern.endsWith(File.separator))
                     {
                         pattern += "**";
                     }
                     included = !(SelectorUtils.matchPath(pattern, name));
                     if (!included)
                     {
                         break;
                     }
                 }
             }
         }
         project.log("included " + className + " = " + included, Project.MSG_VERBOSE);
         return included;
     }
 }
