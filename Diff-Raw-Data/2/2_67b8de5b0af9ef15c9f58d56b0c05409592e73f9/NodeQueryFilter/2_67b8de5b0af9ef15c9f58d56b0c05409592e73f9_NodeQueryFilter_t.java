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
 
 package com.agileapes.couteau.graph.query;
 
 import com.agileapes.couteau.basics.api.Filter;
 import com.agileapes.couteau.basics.api.impl.FilterChain;
 import com.agileapes.couteau.graph.node.Node;
 import com.agileapes.couteau.graph.node.NodeFilter;
 import com.agileapes.couteau.graph.query.filters.OriginNodeAware;
 
 /**
  * NodeQueryFilter is a filter chain that will hold all other filters applicable throughout the search
  * process.
  *
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (2013/7/26, 11:03)
  */
 public class NodeQueryFilter extends FilterChain<Node> implements NodeFilter {
 
     /**
      * will change the origin of the search
      * @param origin    the new origin
      * @return will return the current filter (for chaining purposes)
      */
     public NodeQueryFilter forOrigin(Node origin) {
        for (Filter<? super Node> filter : filters) {
             if (filter instanceof OriginNodeAware) {
                 ((OriginNodeAware) filter).setOrigin(origin);
             }
         }
         return this;
     }
 
 }
