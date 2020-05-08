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
 package org.nuxeo.build.maven.filter;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.model.Dependency;
 import org.nuxeo.build.maven.graph.Edge;
 import org.nuxeo.build.maven.graph.Node;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class ArtifactIdFilter implements Filter {
 
     protected SegmentMatch matcher;
 
     public ArtifactIdFilter(String pattern) {
         this(SegmentMatch.parse(pattern));
     }
 
     public ArtifactIdFilter(SegmentMatch matcher) {
         this.matcher = matcher;
     }
 
     public boolean match(String segment) {
         return matcher.match(segment);
     }
 
     public boolean accept(Edge edge, Dependency dep) {
        final String artifactId = dep.getArtifactId();
        final boolean match = matcher.match(artifactId);
         return match;
     }
 
     public boolean accept(Edge edge) {
         return matcher.match(edge.dst.getArtifact().getArtifactId());
     }
 
     public boolean accept(Artifact artifact) {
         return matcher.match(artifact.getArtifactId());
     }
 
     public boolean accept(Node node) {
         return accept(node.getArtifact());
     }
 
     @Override
     public String toString() {
         return "" + getClass() + " [" + matcher + "]";
     }
 
 }
