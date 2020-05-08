 /*
  * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
  */
 package com.irksomeideas.domal.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 
 import javafx.beans.property.SimpleStringProperty;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.MapChangeListener;
 import javafx.collections.ObservableList;
 import javafx.collections.ObservableMap;
 
 import org.springframework.integration.annotation.ServiceActivator;
 
 public class MetricStreamServiceImpl implements MetricStreamService {
 
   // You add a device by adding an entry with an empty observable array list
   // of issue IDs in the projects Map.
   final ObservableList<String> deviceNames;
   final ObservableMap<String, ObservableList<String>> devicesMap;
   final ObservableMap<String, MetricStreamStub> metricStreamsMap;
   
   public MetricStreamServiceImpl() {
     devicesMap = FXCollections.observableMap(new TreeMap<String, ObservableList<String>>());
     deviceNames = FXCollections.<String> observableArrayList();
     deviceNames.addAll(devicesMap.keySet());
     devicesMap.addListener(devicesMapChangeListener);
 
     metricStreamsMap = FXCollections.observableMap(new TreeMap<String, MetricStreamStub>());
     metricStreamsMap.addListener(metricStreamsMapChangeListener);
   }
   
   List<MetricHandler> handlers = new ArrayList<MetricHandler>();
   
   public void addMetricHandler(MetricHandler handler) {
     handlers.add(handler);
   }
   
  /**
   * This is the way that the model is told about new metrics.
   * @param m
   */
   @ServiceActivator
   public void persistMetric(Metric m) {
     if (!deviceNames.contains(m.deviceName)) {
       deviceNames.add(m.deviceName);
       devicesMap.put(m.deviceName, FXCollections.observableList(new ArrayList<String>()));
     }
     
     if (!devicesMap.get(m.deviceName).contains(m.metricName)) {
       devicesMap.get(m.deviceName).add(m.metricName);
       metricStreamsMap.put(m.metricName, createMetricStreamStrub(m));
     }
   }
   
   private MetricStreamStub createMetricStreamStrub(Metric m) {
     return new MetricStreamStub(m.deviceName, m.metricName);
   }
 
   // The projectNames list is kept in sync with the project's map by observing
   // the projectsMap and modifying the projectNames list in consequence.
   final MapChangeListener<String, ObservableList<String>> devicesMapChangeListener = new MapChangeListener<String, ObservableList<String>>() {
     @Override
     public void onChanged(Change<? extends String, ? extends ObservableList<String>> change) {
       if (change.wasAdded())
         deviceNames.add(change.getKey());
       if (change.wasRemoved())
         deviceNames.remove(change.getKey());
     }
   };
 
   // A MetricStream stub.
   public final class MetricStreamStub implements ObservableMetricStream {
     private final SimpleStringProperty deviceName;
     private final SimpleStringProperty metricName;
     private final SimpleStringProperty units;
 
     MetricStreamStub(String deviceName, String metricName) {
       assert deviceNames.contains(deviceName);
       //assert !devicesMap.get(deviceName).contains(id);
       //assert !metricStreamsMap.containsKey(id);
       this.deviceName = new SimpleStringProperty(deviceName);
       this.metricName = new SimpleStringProperty(metricName);
       this.units = new SimpleStringProperty("");
     }
 
     @Override
     public String getDeviceName() {
       return deviceName.get();
     }
 
     @Override
     public String getMetricName() {
       return metricName.get();
     }
 
     @Override
     public String getUnits() {
       return units.get();
     }
 
     @Override
     public ObservableValue<String> deviceNameProperty() {
       return deviceName;
     }
 
     @Override
     public ObservableValue<String> metricNameProperty() {
       return metricName;
     }
 
     @Override
     public ObservableValue<String> unitsProperty() {
       return units;
     }
   }
 
   // You create new issue by adding a MetricStreamStub instance to the metricStreamsMap.
   // the new id will be automatically added to the corresponding list in
   // the devicesMap.
   //
   final MapChangeListener<String, MetricStreamStub> metricStreamsMapChangeListener = new MapChangeListener<String, MetricStreamStub>() {
     @Override
     public void onChanged(Change<? extends String, ? extends MetricStreamStub> change) {
       if (change.wasAdded()) {
         final MetricStreamStub val = change.getValueAdded();
         devicesMap.get(val.getDeviceName()).add(val.getMetricName());
       }
       if (change.wasRemoved()) {
         final MetricStreamStub val = change.getValueRemoved();
         devicesMap.get(val.getDeviceName()).remove(val.getMetricName());
       }
     }
   };
 
   @Override
   public ObservableList<String> getDeviceNames() {
     return deviceNames;
   }
 
   @Override
   public ObservableList<String> getMetricStreamIds(String projectName) {
     return devicesMap.get(projectName);
   }
 
   @Override
   public MetricStreamStub getMetricStream(String issueId) {
     return metricStreamsMap.get(issueId);
   }
 }
