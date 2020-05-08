 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.zuehlke.zfb.model.chart;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import javafx.beans.property.ObjectProperty;
 import javafx.beans.property.SimpleObjectProperty;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.scene.chart.PieChart;
 import javafx.scene.chart.PieChart.Data;
 
 /**
  *
  * @author rlo
  */
 public class ChartModel implements ChangeListener<File> {
 
     private final ObjectProperty<File> currentDirectory;
     private final ObjectProperty<ObservableList<Data>> chartData;
 
     public ChartModel(final ObjectProperty<File> currentDirectory) {
         this.currentDirectory = currentDirectory;
         this.chartData = new SimpleObjectProperty<>();
         this.currentDirectory.addListener(this);
         this.changed(null, null, currentDirectory.get());
     }
 
     public ObservableList<Data> getChartData() {
         return chartData.get();
     }
 
     public void setChartData(ObservableList<Data> chartData) {
         this.chartData.set(chartData);
     }
 
     public ObjectProperty<ObservableList<Data>> chartDataProperty() {
         return chartData;
     }
 
     @Override
     public void changed(ObservableValue<? extends File> ov, File oldValue, File newValue) {
         if (newValue != null) {
             File[] listFiles = newValue.listFiles();
             ObservableList<PieChart.Data> chartDatas = FXCollections.observableArrayList();
             if (listFiles != null) {
                 for (final File file : listFiles) {
                     chartDatas.add(new Data(file.getName(), file.getName().length()));
                 }
             }
             chartData.set(chartDatas);
         }
     }
}
