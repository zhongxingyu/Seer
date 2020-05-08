 /**
  * 
  */
 package ecologylab.xml.library.kml.feature.container;
 
 import java.util.ArrayList;
 
 import ecologylab.xml.xml_inherit;
 import ecologylab.xml.library.kml.feature.KmlFeature;
 import ecologylab.xml.library.kml.feature.Placemark;
 import ecologylab.xml.library.kml.style.StyleSelector;
 
 /**
  * @author Zach
  * 
  */
 @xml_inherit public abstract class Container extends KmlFeature
 {
 	@xml_collection("Placemark") ArrayList<Placemark>	placemarks;
 
 	@xml_collection("Document") ArrayList<Document>		documents;
 
 	@xml_collection("Folder") ArrayList<Folder>			folder;
 
 	// TODO Folder, NetworkLink, PhotoOverlay, ScreenOverlay, GroundOverlay
 	/**
 	 * 
 	 */
 	public Container()
 	{
 	}
 
 	public Container(String name, String description, StyleSelector selector)
 	{
 		super(name, description, selector);
 	}
 	
 	public void addPlacemark(Placemark newPlacemark)
 	{
		this.placemarks.add(newPlacemark);
 	}
 	
 	/**
 	 * Don't modify the placemarks you get from this function. Or do.
 	 * Whatever.
 	 */
 	public ArrayList<Placemark> getPlacemarks() 
 	{
		return placemarks;
 	}
 	public ArrayList<Document> getDocuments() 
 	{
 		return documents;
 	}
 	public ArrayList<Folder> getFolders() 
 	{
 		return folder;
 	}
 }
