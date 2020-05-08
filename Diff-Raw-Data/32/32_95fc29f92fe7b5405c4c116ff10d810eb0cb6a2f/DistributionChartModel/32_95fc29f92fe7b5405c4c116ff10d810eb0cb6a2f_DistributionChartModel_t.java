 package de.hszg.datenpannen.dataloss.model;
 
 import javafx.beans.binding.Bindings;
 import javafx.beans.property.SimpleStringProperty;
 import javafx.beans.property.StringProperty;
 import javafx.collections.FXCollections;
 import javafx.collections.ObservableList;
 import javafx.scene.chart.PieChart;
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 /**
  * DataModel für das Verteilungs-Pie-Chart.
  */
 public class DistributionChartModel {
 
     private StringProperty title = new SimpleStringProperty();
     private ObservableList<PieChart.Data> distribtutionData = FXCollections.observableArrayList();
     @Inject
     private DatalossResult result;
 
     DistributionChartModel(DatalossResult result) {
         this();
         this.result = result;
         initialize();
     }
 
     public DistributionChartModel() {
     }
 
     @PostConstruct
     final void initialize() {
         title.set("Aufteilung der Kosten");
 
         for (CostDistribution costDistribution : CostDistribution.values()) {
             PieChart.Data element = new PieChart.Data(costDistribution.toString(), 0);
            element.pieValueProperty().bind(result.getAvgDistributionCost(costDistribution));
             element.nameProperty().bind(Bindings.format("%s (%,.0f \u20AC)",
                    costDistribution, result.getSelectedDistributionCost(costDistribution)));
             distribtutionData.add(element);
         }
     }
 
     public StringProperty title() {
         return title;
     }
 
     public ObservableList<PieChart.Data> distribtutionData() {
         return distribtutionData;
     }
 }
