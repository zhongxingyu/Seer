 package de.thatsich.bachelor.featureextraction.restricted.models;
 
 import javafx.beans.property.ListProperty;
 import javafx.beans.property.ObjectProperty;
 import javafx.beans.property.SimpleListProperty;
 import javafx.beans.property.SimpleObjectProperty;
 import javafx.collections.FXCollections;
 import de.thatsich.bachelor.featureextraction.api.entities.FeatureVector;
 import de.thatsich.bachelor.featureextraction.api.entities.FeatureVectorSet;
 
public class FeatureVectorSets {
 	
 	// Properties
 	private final ListProperty<FeatureVectorSet> featureVectorSetList = new SimpleListProperty<FeatureVectorSet>(FXCollections.<FeatureVectorSet>observableArrayList());
 	private final ObjectProperty<FeatureVectorSet> selectedFeatureVectorSet = new SimpleObjectProperty<FeatureVectorSet>();
 	private final ObjectProperty<FeatureVector> selectedFeatureVector = new SimpleObjectProperty<FeatureVector>();
 	
 	// Property Getter
 	public ListProperty<FeatureVectorSet> getFeatureVectorSetListProperty() { return this.featureVectorSetList; }
 	public ObjectProperty<FeatureVectorSet> getSelectedFeatureVectorSetProperty() { return this.selectedFeatureVectorSet; }
 	public ObjectProperty<FeatureVector> getSelectedFeatureVectorProperty() { return selectedFeatureVector; }
 	
 	// Getter
 	public FeatureVectorSet getSelectedFeatureVectorSet() { return this.selectedFeatureVectorSet.get(); }
 	public FeatureVector getSelectedFeatureVector() { return this.selectedFeatureVector.get(); }
 	
 	// Setter
 	public void setSelectedFeatureVectorSet(FeatureVectorSet featureVectorSet) { this.selectedFeatureVectorSet.set(featureVectorSet); }
 	public void setSelectedFeatureVector(FeatureVector featureVector) { this.selectedFeatureVector.set(featureVector); }
 }
