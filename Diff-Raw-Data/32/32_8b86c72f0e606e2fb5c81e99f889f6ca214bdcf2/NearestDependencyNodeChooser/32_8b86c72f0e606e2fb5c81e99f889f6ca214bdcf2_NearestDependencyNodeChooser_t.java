 /**
  * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.maven.dependency.model.aether;
 
 import java.util.List;
 
 import org.sonatype.aether.graph.DependencyNode;
 
 public class NearestDependencyNodeChooser implements DependencyNodeChooser
 {
    @Override
    public DependencyNode choose(List<DependencyNode> nodes)
    {
      final boolean hasVisibles = hasVisibles(nodes);

       DependencyNode chosen = null;
       int chosenDepth = Integer.MAX_VALUE;

       for (DependencyNode node : nodes)
       {
          final DependencyNode2 adapter = DependencyNode2Adapter.get(node);
         if ((hasVisibles && !adapter.isVisible()) || getRoot(node, adapter) == null)
          {
             continue;
          }
 
          final int nodeDepth = adapter.getMinimalDepth();
          if (chosenDepth > nodeDepth)
          {
             chosen = node;
             chosenDepth = nodeDepth;
          }
       }
       return chosen;
    }
 
   private boolean hasVisibles(List<DependencyNode> nodes)
   {
      for (DependencyNode node : nodes)
      {
         final DependencyNode2 adapter = DependencyNode2Adapter.get(node);
         if (adapter.isVisible())
         {
            return true;
         }
      }
      return false;
   }

    private DependencyNode getRoot(DependencyNode node, DependencyNode2 adapter)
    {
       if (adapter.getParents().isEmpty())
       {
          return node;
       }
 
       for (DependencyNode parent : adapter.getParents())
       {
          final DependencyNode2 parentAdapter = DependencyNode2Adapter.get(parent);
          if (parentAdapter.getReplacement() != null)
          {
             continue;
          }
 
          final DependencyNode root = getRoot(parent, parentAdapter);
          if (root != null)
          {
             return root;
          }
       }
 
       return null;
    }
 }
