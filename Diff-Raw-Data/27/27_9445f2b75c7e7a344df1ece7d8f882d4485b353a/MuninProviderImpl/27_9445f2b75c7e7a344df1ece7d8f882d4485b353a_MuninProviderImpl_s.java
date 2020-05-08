 // ______________________________________________________________________________
 //
 //           Project: jolokia-munin-plugin
 //            Module: jolokia-munin-plugin
 //             Class: MuninProviderImpl
 //              File: MuninProviderImpl.java
 //        changed by: christian
 //       change date: 24.02.13 18:28
 //       description: Provides the munin config and values
 // ______________________________________________________________________________
 //
 //         Copyright: (c) Christian Fritz, all rights reserved
 // ______________________________________________________________________________
 package de.chrfritz.jolokiamunin.munin.impl;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Strings;
 import de.chrfritz.jolokiamunin.config.Category;
 import de.chrfritz.jolokiamunin.config.Field;
 import de.chrfritz.jolokiamunin.config.Graph;
 import de.chrfritz.jolokiamunin.jolokia.Fetcher;
 import de.chrfritz.jolokiamunin.jolokia.FetcherException;
 import de.chrfritz.jolokiamunin.jolokia.FetcherFactory;
 import de.chrfritz.jolokiamunin.jolokia.Request;
 import de.chrfritz.jolokiamunin.munin.MuninProvider;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A handler that thats get a configuration and produces either the configuration for munin or fetches the values and
  * generate the munin value string.
  */
 public class MuninProviderImpl implements MuninProvider {
 
     private FetcherFactory fetcherFactory;
 
     /**
      * Create a new instance with the given fetcher factory.
      *
      * @param fetcherFactory The fetcher factory.
      */
     public MuninProviderImpl(FetcherFactory fetcherFactory) {
 
         this.fetcherFactory = fetcherFactory;
     }
 
     /**
      * /**
      * Get the munin compatible configuration for a list of categories.
      *
      * @param categories The list of configurations
      * @return The produced munin configuration.
      */
     @Override
     public String getConfig(List<Category> categories) {
 
         StringBuilder buffer = new StringBuilder();
         for (Category category : categories) {
             buffer.append(getConfig(category));
         }
         return buffer.toString();
     }
 
     /**
      * Get the munin compatible configuration for a single category.
      *
      * @param category The category.
      * @return The produced munin configuration.
      */
     @Override
     public String getConfig(Category category) {
 
         StringBuilder buffer = new StringBuilder();
         for (Graph graph : category.getGraphs()) {
 
            buffer.append("multigraph ").append(category.getName()).append(graph.getName()).append("\n");
             addAttribute(buffer, "graph_title", graph.getTitle());
             addAttribute(buffer, "graph_args", graph.getArgs());
             addAttribute(buffer, "graph_category", category.getName());
             addAttribute(buffer, "graph_info", graph.getInfo());
             addAttribute(buffer, "graph_vlabel", graph.getVlabel());
 
             StringBuilder fields = new StringBuilder();
             StringBuilder fieldOrder = new StringBuilder();
 
             getFieldDefinitions(graph, fields, fieldOrder);
 
             addAttribute(buffer, "graph_order", fieldOrder.toString());
             buffer.append(fields);
         }
 
         return buffer.toString();
     }
 
     /**
      * Added the the field configuration to a buffer.
      *
      * @param graph      The graph for the field definitions
      * @param fields     The fields buffer.
      *                   This buffer is filled with the field configuration.
      * @param fieldOrder The field order buffer.
      *                   This buffer is filled with the field names to add this to a order field.
      */
     private void getFieldDefinitions(Graph graph, StringBuilder fields, StringBuilder fieldOrder) {
         for (Field field : graph.getFields()) {
             String fieldName = graph.getName() + "_" + field.getName();
             fieldOrder.append(fieldName).append(" ");
             addFieldAttribute(fields, fieldName, "label", field.getLabel());
             addFieldAttribute(fields, fieldName, "draw", field.getDraw());
             addFieldAttribute(fields, fieldName, "info", field.getInfo());
             addFieldAttribute(fields, fieldName, "type", field.getType());
             addFieldAttribute(fields, fieldName, "warning", field.getWarning());
             addFieldAttribute(fields, fieldName, "critical", field.getCritical());
             addFieldAttribute(fields, fieldName, "colour", field.getColor());
         }
     }
 
     /**
      * Fetches all the values of a list of categories and generates the munin values string.
      *
      * @param categories The list of categories.
      * @return The fetched values as munin compatible string.
      * @throws FetcherException In case of all errors when try to fetching the values.
      */
     @Override
     public String getValues(List<Category> categories) throws FetcherException {
 
         StringBuilder buffer = new StringBuilder();
         for (Category category : categories) {
             buffer.append(getValues(category));
         }
         return buffer.toString();
     }
 
     /**
      * Fetch all values within a single category and generates the munin value string.
      *
      * @param category The category
      * @return The fetched values as munin compatible string.
      * @throws FetcherException In case of all errors when try to fetching the values.
      */
     @Override
     public String getValues(Category category) throws FetcherException {
 
         try {
             StringBuilder buffer = new StringBuilder();
             Map<String, Request> requests = buildRequests(category);
 
             Fetcher fetcher = fetcherFactory.getInstance(category.getSourceUrl());
 
             Map<Request, Number> values = fetcher.fetchValues(new ArrayList<>(requests.values()));
             for (Graph graph : category.getGraphs()) {
                buffer.append("multigraph ").append(category.getName()).append(graph.getName()).append("\n");
                 for (Field field : graph.getFields()) {
                     String fieldName = graph.getName() + "_" + field.getName();
 
                     addFieldAttribute(buffer, fieldName, "value", values.get(requests.get(fieldName)));
                 }
             }
 
             return buffer.toString();
        }
        catch (MalformedURLException e) {
             throw new FetcherException("Malformed source url for category.", e);
         }
     }
 
     /**
      * Build the unique requests for a single category which should be executed when the values should be realy fetched.
      * <p/>
      * It returns a map which maps the munin field name to the created request must not have all values filled.
      *
      * @param category The category they values should be fetched.
      * @return A map which maps the munin field name to the created request.
      */
     private Map<String, Request> buildRequests(Category category) {
         Map<String, Request> requests = new HashMap<>();
 
         for (Graph graph : category.getGraphs()) {
             String graphMbean = graph.getMbean();
             String graphAttribute = graph.getAttribute();
             String graphName = graph.getName();
 
             for (Field field : graph.getFields()) {
                 String mbean = Objects.firstNonNull(field.getMbean(), graphMbean);
                 String attribute = Objects.firstNonNull(field.getAttribute(), graphAttribute);
                 requests.put(graphName + "_" + field.getName(), new Request(mbean, attribute, field.getPath()));
             }
         }
         return requests;
     }
 
     /**
      * Add a new attribute to the given buffer.
      *
      * @param buffer The buffer.
      * @param name   The attributes name.
      * @param value  The value of the attribute.
      */
     private static void addAttribute(StringBuilder buffer, String name, String value) {
         if (!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(value)) {
            buffer.append(name).append(" ").append(value.trim()).append("\n");
         }
     }
 
     /**
      * Add a new field attribute to the given buffer.
      *
      * @param buffer    The buffer.
      * @param fieldName The field name for the attribute.
      * @param attribute The attributes name.
      * @param value     The value of the attribute.
      */
     private static void addFieldAttribute(StringBuilder buffer, String fieldName, String attribute, String value) {
         addAttribute(buffer, fieldName + "." + attribute, value);
     }
 
     /**
      * Add a new field attribute to the given buffer.
      *
      * @param buffer    The buffer.
      * @param fieldName The field name for the attribute.
      * @param attribute The attributes name.
      * @param value     The value of the attribute.
      */
     private static void addFieldAttribute(StringBuilder buffer, String fieldName, String attribute, Number value) {
        buffer.append(fieldName).append(".").append(attribute).append(" ").append(value).append("\n");
     }
 }
