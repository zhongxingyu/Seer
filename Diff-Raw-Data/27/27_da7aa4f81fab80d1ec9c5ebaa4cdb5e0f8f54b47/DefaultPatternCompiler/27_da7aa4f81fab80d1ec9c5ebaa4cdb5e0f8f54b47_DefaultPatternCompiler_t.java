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
 
 package com.agileapes.couteau.graph.query.impl;
 
 import com.agileapes.couteau.graph.node.Node;
 import com.agileapes.couteau.graph.node.NodeFilter;
 import com.agileapes.couteau.graph.query.NodeQueryFilter;
 import com.agileapes.couteau.graph.query.PatternCompiler;
 import com.agileapes.couteau.graph.query.QuerySnippetParser;
 import com.agileapes.couteau.strings.document.DocumentReader;
 import com.agileapes.couteau.strings.document.impl.DefaultDocumentReader;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Pattern;
 
 /**
  * This is an implementation of {@link PatternCompiler} that will take in a chain of parsers and compile the
  * input pattern into accepting filters.
  *
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (2013/7/30, 6:00)
  */
 public class DefaultPatternCompiler implements PatternCompiler {
 
     public static final Pattern WHITESPACE = Pattern.compile("\\s+");
 
     private final List<QuerySnippetParser> parsers = new ArrayList<QuerySnippetParser>();
 
     public DefaultPatternCompiler(List<QuerySnippetParser> parsers) {
         this.parsers.addAll(parsers);
     }
 
     public DefaultPatternCompiler(QuerySnippetParser... parsers) {
         this(Arrays.asList(parsers));
     }
 
     public void addParser(QuerySnippetParser parser) {
         parsers.add(parser);
     }
 
     @Override
     public List<NodeQueryFilter<?>> compile(String pattern) {
         final DocumentReader reader = new DefaultDocumentReader(pattern);
         reader.skip(WHITESPACE);
         final ArrayList<NodeQueryFilter<?>> filters = new ArrayList<NodeQueryFilter<?>>();
         while (reader.hasMore()) {
             final NodeQueryFilter<?> filter = new NodeQueryFilter<Node>();
             for (QuerySnippetParser parser : parsers) {
                 final List<NodeFilter> nodeFilters;
                nodeFilters = reader.parse(parser);
                 if (nodeFilters != null) {
                     for (NodeFilter nodeFilter : nodeFilters) {
                         //noinspection unchecked
                         filter.addFilter(nodeFilter);
                     }
                     if (parser.endsParsing()) {
                         break;
                     }
                 }
             }
             filters.add(filter);
             reader.skip(WHITESPACE);
             if (reader.hasMore()) {
                 reader.expect("/", false);
             }
         }
         return filters;
     }
 
 }
