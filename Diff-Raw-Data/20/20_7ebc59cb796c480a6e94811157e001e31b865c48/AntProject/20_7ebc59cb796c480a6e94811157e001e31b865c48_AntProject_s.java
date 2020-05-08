 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: AntProject.java,v 1.3 2003-07-12 03:31:46 shahid.shah Exp $
  */
 
 package com.netspective.sparx.ant;
 
 import java.io.File;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 import org.apache.tools.ant.Main;
 
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.ValueContext;
 import com.netspective.commons.value.PresentationValue;
 import com.netspective.commons.value.Value;
 import com.netspective.commons.value.AbstractValue;
 import com.netspective.commons.value.source.AbstractValueSource;
 
 public class AntProject
 {
     private String id;
     private ValueSource file;
     private boolean defaultProject;
     private AntBuildDialog antDialog;
     private String privateTargetPrefixChars = "._";
     private boolean showPrivateTargets = false;
 
     public AntProject()
     {
     }
 
     public String getId()
     {
         return id;
     }
 
     public void setId(String id)
     {
         this.id = id;
     }
 
     public ValueSource getFile()
     {
         return file;
     }
 
     public void setFile(ValueSource file)
     {
         this.file = file;
     }
 
     public boolean isDefault()
     {
         return defaultProject;
     }
 
     public void setDefault(boolean defaultProject)
     {
         this.defaultProject = defaultProject;
     }
 
     public String getPrivateTargetPrefixChars()
     {
         return privateTargetPrefixChars;
     }
 
     public void setPrivateTargetPrefixChars(String privateTargetPrefixChars)
     {
         this.privateTargetPrefixChars = privateTargetPrefixChars;
     }
 
     public boolean isShowPrivateTargets()
     {
         return showPrivateTargets;
     }
 
     public void setShowPrivateTargets(boolean showPrivateTargets)
     {
         this.showPrivateTargets = showPrivateTargets;
     }
 
     public boolean isPrivateTargetName(String targetName)
     {
         return privateTargetPrefixChars.indexOf(targetName.charAt(0)) != -1;
     }
 
     public Project getProject(ValueContext vc)
     {
         if(this.file == null)
             return null;
 
         return getConfiguredProject(new File(this.file.getTextValue(vc)));
     }
 
     public AntBuildDialog getDialog()
     {
         return antDialog;
     }
 
     public AntBuildDialog createDialog()
     {
         return new AntBuildDialog();
     }
 
     public void addDialog(AntBuildDialog dialog)
     {
         dialog.setAntProject(this);
         antDialog = dialog;
     }
 
     public AntProjectTargetsValueSource getTargets()
     {
         return new AntProjectTargetsValueSource();
     }
 
     protected class AntProjectTargetsValueSource extends AbstractValueSource
     {
         public PresentationValue getPresentationValue(ValueContext vc)
         {
             PresentationValue result = new PresentationValue();
             PresentationValue.Items items = result.createItems();
 
             File projectFile = new File(file.getTextValue(vc));
             if(!projectFile.exists())
             {
                 items.addItem(projectFile + " not found.");
                 return result;
             }
 
             Project project = AntProject.getConfiguredProject(projectFile);
 
             String defaultTargetName = project.getDefaultTarget();
             Set sortedTargetNames = new TreeSet(project.getTargets().keySet());
             for(Iterator i = sortedTargetNames.iterator(); i.hasNext(); )
             {
                 String targetName = (String) i.next();
                 if(! isShowPrivateTargets() && isPrivateTargetName(targetName))
                     continue;
                 if(targetName.equals(defaultTargetName))
                     items.addItem(targetName + " (default)", targetName);
                 else
                     items.addItem(targetName);
             }
 
             return result;
         }
 
         public Value getValue(ValueContext vc)
         {
             final File projectFile = new File(file.getTextValue(vc));
             if(! projectFile.exists())
             {
                 return new AbstractValue()
                 {
                     public Object getValue()
                     {
                         return getTextValue();
                     }
 
                     public String getTextValue()
                     {
                         return projectFile + " not found.";
                     }
 
                     public String[] getTextValues()
                     {
                         return new String[] { getTextValue() };
                     }
 
                     public List getListValue()
                     {
                         List list = new ArrayList();
                         list.add(getTextValue());
                         return list;
                     }
                 };
             }
 
             Project project = AntProject.getConfiguredProject(projectFile);
 
             final Set sortedTargetNames = new TreeSet(project.getTargets().keySet());
             return new AbstractValue()
             {
                 public Object getValue()
                 {
                     return getTextValue();
                 }
 
                 public String getTextValue()
                 {
                     // just return the first one if a single value is requested
                     return (String) sortedTargetNames.iterator().next();
                 }
 
                 public String[] getTextValues()
                 {
                     return (String[]) sortedTargetNames.toArray(new String[sortedTargetNames.size()]);
                 }
 
                 public List getListValue()
                 {
                     List list = new ArrayList();
                     list.addAll(sortedTargetNames);
                     return list;
                 }
             };
         }
 
         public boolean hasValue(ValueContext vc)
         {
             Value value = getValue(vc);
             return value.getTextValue() != null;
         }
     }
 
     public static Project getConfiguredProject(File buildFile)
     {
         Project project = new Project();
         project.init();
         ProjectHelper.configureProject(project, buildFile);
         project.setUserProperty("ant.file", buildFile.getAbsolutePath());
         project.setUserProperty("ant.version", Main.getAntVersion());
         return project;
     }
 }
