 package edu.uca.info2.components;
 
import aimax.osm.data.BoundingBox;
import aimax.osm.data.Position;
import aimax.osm.data.entities.MapNode;
import aimax.osm.data.entities.MapWay;
import edu.uca.info2.map.ZAMap;
import edu.uca.info2.util.MapUtils;
import jpl.Compound;
import jpl.Term;

import java.util.ArrayList;
import java.util.List;
 
 public class Area {
 
 	public static final String pl_compound_name = "zona";
 
 	private float costo; // costo de recorrer el area en km
 	private Zone zone;
 	private ZAMap map;
 	private long centerNodeId;
 	private int radius;
 	private List<MapNode> nodos;
 
 	public Area(long centerNodeId) {
 		setCenterNodeId(centerNodeId);
 		setRadius(1);
 		this.nodos = new ArrayList<MapNode>();
 	}
 
 	public void setCosto(float costo) {
 		this.costo = costo;
 	}
 
 	public float getCosto() {
 		return costo;
 	}
 
 	public long getCenterNodeId() {
 		return centerNodeId;
 	}
 
 	public MapNode getCenterNode() {
 		return map.getNode(centerNodeId);
 	}
 
 	public void setCenterNodeId(long centerNodeId) {
 		this.centerNodeId = centerNodeId;
 	}
 
 	public int getRadius() {
 		return radius;
 	}
 
 	public void setRadius(int radius) {
 		this.radius = radius;
 	}
 
 	public float getLat() {
 		return new Position(map.getNode(centerNodeId)).getLat();
 	}
 
 	public float getLon() {
 		return new Position(map.getNode(centerNodeId)).getLon();
 	}
 
 	public String toString() {
 
 		return " Area: center: " + getCenterNodeId() + " radius: "
 				+ getRadius();
 	}
 
 	public List<MapNode> getNodos() {
 		return nodos;
 	}
 
 	public ZAMap getMap() {
 		return map;
 	}
 
 	public void setMap(ZAMap map) {
 		this.map = map;
 	}
 
 	public Zone getZone() {
 		return zone;
 	}
 
 	/* busca los nodos dentro de su rango y los agrega a su lista de nodos */
 	public void findNodes() {
 		this.nodos = new ArrayList<MapNode>();
 		MapNode centernode = map.getNode(centerNodeId);
 		for (MapWay mapway : map.getWays(new BoundingBox())) {
 			for (MapNode nodo : mapway.getNodes()) {
 				if (new Position(nodo).getDistKM(centernode) < radius) {
 					this.nodos.add(nodo);
 				}
 			}
 		}
 	}
 
 	public boolean isNodeInArea(MapNode node) {
 		return this.nodos.contains(node);
 	}
 
 	/*
 	 * establece la zona en la que se encuentra, basandose en las zonas de su
 	 * mapa.
 	 */
 	public void findZone() {
 		for (Zone zona : map.getZones()) {
 			float latmin = zona.getLat1();
 			float latmax = zona.getLat2();
 			float lonmin = zona.getLon1();
 			float lonmax = zona.getLon2();
 			// intercambiamos las coordenadas min y max para el caso de que
 			// esten al revez
 			if (latmin > latmax) {
 				float aux = latmin;
 				latmin = latmax;
 				latmax = aux;
 			}
 			if (lonmin > lonmax) {
 				float aux = lonmin;
 				lonmin = lonmax;
 				lonmax = aux;
 			}
 			float lat = getLat();
 			float lon = getLon();
 			// establecemos la zona si el area pertenece a esta
 			if (latmin <= lat && lat <= latmax && lonmin <= lon
 					&& lon <= lonmax) {
 				this.zone = zona;
 				break;
 			}
 		}
 	}
 
 	public Compound toPlCompoundTerm() {
 		return new Compound("zona", new Term[] { new jpl.Integer(centerNodeId),
 				new jpl.Float(costo),
 				new jpl.Integer(zone.getRestriction().getStartTime()), // new
 																		// Atom(Integer.toString(zone.getRestriction().getStartTime())),
 				new jpl.Integer(zone.getRestriction().getEndTime()) }); // new
 																		// Atom(Integer.toString(zone.getRestriction().getEndTime()))});
 	}
 
 	public List<Segment> segmentsInArea() {
 		List<Segment> segments = new ArrayList<Segment>();
 
 		for (MapNode from : this.getNodos()) {
 			for (MapNode to : MapUtils.neighborsForNode(from, this)) {
 				Segment s = new Segment(from, to);
 
 				if (!segments.contains(s)) {
 					segments.add(s);
 				}
 			}
 		}
 
 		return segments;
 	}
 
 }
