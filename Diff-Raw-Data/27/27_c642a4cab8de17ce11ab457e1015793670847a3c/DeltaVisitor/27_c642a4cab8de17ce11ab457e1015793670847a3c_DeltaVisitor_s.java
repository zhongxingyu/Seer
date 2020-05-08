 /*
 DMEclipseSaveHookPlugin - Eclipse plugin for executing java code on java source code changes - http://www.davide.bz/dmesh
 
 Copyright (C) 2013 Davide Montesin <d@vide.bz> - Bolzano/Bozen - Italy
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>
 */
 
 package bz.davide.dmeclipsesavehookplugin.builder;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.runtime.CoreException;
 
 /**
  * @author Davide Montesin <d@vide.bz>
  */
 public class DeltaVisitor implements IResourceDeltaVisitor
 {
   public static final String MARKER_TYPE = "JBindPluginEclipse.JBindPluginEclipseError";

   int                        countSrc    = 0;
 
    @Override
    public boolean visit(IResourceDelta delta) throws CoreException
    {
       IResource resource = delta.getResource();
       if (resource instanceof IFile)
       {
          String resourcePath = resource.getFullPath().toPortableString();
          System.out.println("CHANGER: " + resourcePath);
          // Warning: not to include file extensions for files that are copied to output path too!
          // i.e. .get.xml
          String[] srcPostfixes = new String[] { "/.project", "/.classpath", "/dmxmljson.conf", ".java" };
          for (String srcPostfix : srcPostfixes)
          {
             if (resourcePath.endsWith(srcPostfix))
             {
                this.countSrc++;
                break;
             }
          }
       }
       //return true to continue visiting children.
       return true;
    }
 }
