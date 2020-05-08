 /*
  * Copyright (c) 2013. AgileApes (http://www.agileapes.scom/), and
  * associated organization.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this
  * software and associated documentation files (the "Software"), to deal in the Software
  * without restriction, including without limitation the rights to use, copy, modify,
  * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies
  * or substantial portions of the Software.
  */
 
 package com.agileapes.couteau.xml.query.filters;
 
 import com.agileapes.couteau.graph.node.ConfigurableNodeFilter;
 import com.agileapes.couteau.graph.node.Node;
 import com.agileapes.couteau.xml.node.NodeType;
 import com.agileapes.couteau.xml.node.XmlNode;
 
import static com.agileapes.couteau.basics.collections.CollectionWrapper.with;

 /**
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (7/31/13, 11:43 AM)
  */
 public class NodeTypeFilter<N extends Node> implements ConfigurableNodeFilter<N> {
 
    private NodeType nodeType = null;
 
     @Override
     public void setAttribute(String name, String value) {
        if (with("type", "0").has(name)) {
            nodeType = Enum.valueOf(NodeType.class, value.toUpperCase());
        }
     }
 
     @Override
     public boolean accepts(N item) {
        return nodeType == null || item instanceof XmlNode && ((XmlNode) item).getNodeType().equals(nodeType);
     }
 
 }
