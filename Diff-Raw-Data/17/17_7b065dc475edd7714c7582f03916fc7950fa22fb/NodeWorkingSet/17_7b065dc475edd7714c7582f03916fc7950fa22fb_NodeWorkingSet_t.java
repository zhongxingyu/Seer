 /**
  * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.b2eclipse.input.node;
 
 import java.util.ArrayList;
 
 /**
  * 
  * @author WD
  * 
  */
 public class NodeWorkingSet extends Node
 {
    private String longName;
    private String shortName;
 
    public NodeWorkingSet(Node parent, String name, String lastModuleName)
    {
       super();
       children = new ArrayList<Node>();
 
       this.name = name;
 
       longName = name;
       if (name.contains("/"))
       {
          shortName = this.name.substring(this.name.lastIndexOf("/") + 1, this.name.length());
          if (!shortName.equals(lastModuleName))
          {
             shortName = lastModuleName + "/" + shortName;
          }
       }
       else
       {
          shortName = this.name;
       }
       file = null;
       this.parent = parent;
       parent.addChild(this);
 
    }
 
    // protected void finalize() throws Throwable
    // {
    // WSNameValidator.removeFromlist(this.name);
    // super.finalize();
    // }
 
   // Is used in renaming a working set, after that long/short -names aren't available anymore.
   @Override
   public void setName(String name)
   {
      this.name = name;
      longName = name;
      shortName = name;
   }

    public String getLongName()
    {
       return longName;
    }
 
    public void setShortName()
    {
       name = shortName;
    }
 
    public void setLongName()
    {
       name = longName;
    }
 }
