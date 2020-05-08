 package de.thatsich.bachelor.featureextraction.api.core;
 
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import de.thatsich.bachelor.featureextraction.api.entities.FeatureVector;
import de.thatsich.bachelor.featureextraction.api.entities.FeatureVectorSet;
 
public interface IFeatureVectorSets {
	// Property Getter
	public ListProperty<FeatureVectorSet> getFeatureVectorSetListProperty();
	public ObjectProperty<FeatureVectorSet> getSelectedFeatureVectorSetProperty();
	public ObjectProperty<FeatureVector> getSelectedFeatureVectorProperty();
	
	// Getter
	public FeatureVectorSet getSelectedFeatureVectorSet();
	public FeatureVector getSelectedFeatureVector();
	
	// Setter
	public void setSelectedFeatureVectorSet(FeatureVectorSet featureVectorSet);
	public void setSelectedFeatureVector(FeatureVector featureVector);
 }
