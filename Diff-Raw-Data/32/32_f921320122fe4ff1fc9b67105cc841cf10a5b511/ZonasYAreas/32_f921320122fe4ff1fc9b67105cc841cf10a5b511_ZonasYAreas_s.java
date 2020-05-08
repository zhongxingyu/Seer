 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.uca.info2.application;
 
import src.edu.uca.info2.components.Area;
import src.edu.uca.info2.map.ZAMap;
import src.edu.uca.info2.viewer.ZAMapViewFrame;
 
 /**
  * Aplicacion para visualizar las zonas y las areas descritas en los json
  * 
  * @author Toshiba
  */
 public class ZonasYAreas {
 	private static ZAMapViewFrame frame;
 
 	public static void main(String[] args) throws FileNotFoundException,
			IOException {
 		List<Area> lalista = new ArrayList<Area>();
 
 		frame = new ZAMapViewFrame();
 		ZAMap map = (ZAMap) frame.getView().getMap();
 		// map.loadElementsFromJson();
 	}
 
 }
