 /* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
  *
  * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *  Generalitat Valenciana
  *   Conselleria d'Infraestructures i Transport
  *   Av. Blasco Ib��ez, 50
  *   46010 VALENCIA
  *   SPAIN
  *
  *      +34 963862235
  *   gvsig@gva.es
  *      www.gvsig.gva.es
  *
  *    or
  *
  *   IVER T.I. S.A
  *   Salamanca 50
  *   46005 Valencia
  *   Spain
  *
  *   +34 963163400
  *   dac@iver.es
  */
 package com.iver.cit.gvsig.project.documents.view;
 
 import geomatico.events.EventBus;
 
 import javax.inject.Inject;
 
 import org.exolab.castor.xml.XMLException;
 import org.gvsig.map.MapContext;
 import org.gvsig.persistence.generated.DocumentType;
 import org.gvsig.persistence.generated.ViewDocumentType;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.ui.mdiManager.IWindow;
 import com.iver.cit.gvsig.project.documents.exceptions.OpenException;
 import com.iver.cit.gvsig.project.documents.exceptions.SaveException;
 import com.iver.cit.gvsig.project.documents.view.gui.ViewProperties;
 
 /**
  * Clase que representa una vista del proyecto
  * 
  * @author Fernando Gonz�lez Cort�s
  */
 public class ProjectView extends ProjectViewBase {
 
 	// public static int numViews = 0;
 
 	// public static int METROS = 0;
 	// public static int KILOMETROS = 1;
 	// public static int[] unidades = new int[] { METROS, KILOMETROS };
 	// /private Color backgroundColor = new Color(255, 255, 255);
 
 	@Inject
 	public ProjectView(EventBus eventBus) {
 		super(eventBus);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 * @throws XMLException
 	 * @throws SaveException
 	 */
 	public ViewDocumentType getXMLEntity() {
 
 		ViewDocumentType xml = new ViewDocumentType();
 		super.fill(xml);
 		// xml.putProperty("nameClass", this.getClass().getName());
 		// remove old hyperlink persistence
 		// xml.putProperty("m_selectedField", m_selectedField);
 		// xml.putProperty("m_typeLink", m_typeLink);
 		// xml.putProperty("m_extLink", m_extLink);
 		xml.setMainMap(mapContext.getXML());
 
 		if (mapOverViewContext != null) {
 			xml.setOverviewMap(mapOverViewContext.getXML());
 		}
 		return xml;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param xml
 	 *            DOCUMENT ME!
 	 * @param p
 	 *            DOCUMENT ME!
 	 * @throws OpenException
 	 * 
 	 * @see com.iver.cit.gvsig.project.documents.ProjectDocument#setXMLEntity(com.iver.utiles.XMLEntity)
 	 */
 	public void setXMLEntity(DocumentType xml) throws OpenException {
 		super.read(xml);
 		ViewDocumentType viewDocumentXML = (ViewDocumentType) xml;
 		try {
 			MapContext mapContext = newMapContext();
 			mapContext.setXML(viewDocumentXML.getMainMap());
 			setMapContext(mapContext);
 			if (viewDocumentXML.getOverviewMap() != null) {
 				mapContext = newMapContext();
 				mapContext.setXML(viewDocumentXML.getMainMap());
 				setMapOverViewContext(mapContext);
 			}
 
 			showErrors();
 		} catch (Exception e) {
 			throw new OpenException(e, this.getClass().getName());
 		}
 	}
 
 	public String getFrameName() {
 		return PluginServices.getText(this, "Vista");
 	}
 
 	public IWindow createWindow() {
 		com.iver.cit.gvsig.project.documents.view.gui.View view = new com.iver.cit.gvsig.project.documents.view.gui.View();
 		if (windowData != null)
 			view.setWindowData(windowData);
 		view.initialize();
 		view.setModel(this);
 		callCreateWindow(view);
 		return view;
 	}
 
 	public IWindow getProperties() {
 		return new ViewProperties(this);
 	}
 
 	// public int computeSignature() {
 	// int result = 17;
 	//
 	// Class clazz = getClass();
 	// Field[] fields = clazz.getDeclaredFields();
 	// for (int i = 0; i < fields.length; i++) {
 	// try {
 	// String type = fields[i].getType().getName();
 	// if (type.equals("boolean")) {
 	// result += 37 + ((fields[i].getBoolean(this)) ? 1 : 0);
 	// } else if (type.equals("java.lang.String")) {
 	// Object v = fields[i].get(this);
 	// if (v == null) {
 	// result += 37;
 	// continue;
 	// }
 	// char[] chars = ((String) v).toCharArray();
 	// for (int j = 0; j < chars.length; j++) {
 	// result += 37 + (int) chars[i];
 	// }
 	// } else if (type.equals("byte")) {
 	// result += 37 + (int) fields[i].getByte(this);
 	// } else if (type.equals("char")) {
 	// result += 37 + (int) fields[i].getChar(this);
 	// } else if (type.equals("short")) {
 	// result += 37 + (int) fields[i].getShort(this);
 	// } else if (type.equals("int")) {
 	// result += 37 + fields[i].getInt(this);
 	// } else if (type.equals("long")) {
 	// long f = fields[i].getLong(this) ;
 	// result += 37 + (f ^ (f >>> 32));
 	// } else if (type.equals("float")) {
 	// result += 37 + Float.floatToIntBits(fields[i].getFloat(this));
 	// } else if (type.equals("double")) {
 	// long f = Double.doubleToLongBits(fields[i].getDouble(this));
 	// result += 37 + (f ^ (f >>> 32));
 	// } else {
 	// Object obj = fields[i].get(this);
 	// result += 37 + ((obj != null)? obj.hashCode() : 0);
 	// }
 	// } catch (Exception e) { e.printStackTrace(); }
 	//
 	// }
 	// return result;
 	// }
 }
