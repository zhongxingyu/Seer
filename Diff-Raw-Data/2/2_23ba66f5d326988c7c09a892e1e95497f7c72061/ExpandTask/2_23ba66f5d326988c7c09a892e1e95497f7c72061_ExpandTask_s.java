 /*
  * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.build.ant.artifact;
 
 import java.util.Collection;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.nuxeo.build.maven.MavenClient;
 import org.nuxeo.build.maven.MavenClientFactory;
 import org.nuxeo.build.maven.filter.AndFilter;
 import org.nuxeo.build.maven.filter.CompositeFilter;
import org.nuxeo.build.maven.filter.Filter;
 import org.nuxeo.build.maven.graph.Node;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class ExpandTask extends Task {
 
     public String key;
 
     public int depth = 1;
 
     public AndFilter filter = new AndFilter();
     
     public void setKey(String key) {
         this.key = key;
     }
 
     public void setDepth(String depth) {
         if ("all".equals(depth)) {
             this.depth = Integer.MAX_VALUE;
         } else {
             this.depth = Integer.parseInt(depth);
         }
     }
 
     public void addExcludes(Excludes excludes) {
         filter.addFilter(excludes.getFilter());
     }
 
     public void addIncludes(Includes includes) {
         filter.addFilter(includes.getFilter());
     }
  
     protected boolean acceptNode(Node node) {
         return true;
     }
 
     @Override
     public void execute() throws BuildException {
         Collection<Node> nodes = null;
         MavenClient maven = MavenClientFactory.getInstance();
         Graph graph = maven.getGraph();
         if (key == null) {
             nodes = graph.getRoots();
         } else {
             nodes = graph.find(key);
         }
         for (Node node : nodes) {
             if (!acceptNode(node)) {
                 continue;
             }
             graph.resolveDependencyTree(node, CompositeFilter.compact(filter), depth);
         }
     }
 
 }
