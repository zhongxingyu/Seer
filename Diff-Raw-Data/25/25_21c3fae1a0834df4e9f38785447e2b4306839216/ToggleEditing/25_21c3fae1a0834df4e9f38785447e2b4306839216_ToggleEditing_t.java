 /*
  * This file is part of NavTable
  * Copyright (C) 2009 - 2010  Cartolab (Universidade da Corua)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * 
  * Authors:
  *   Juan Ignacio Varela Garc�a <nachouve (at) gmail (dot) com>
  *   Pablo Sanxiao Roca <psanxiao (at) gmail (dot) com>
  *   Javier Est�vez Vali�as <valdaris (at) gmail (dot) com>
  */
 package es.icarto.gvsig.catastro.utils;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.sql.Types;
 import java.text.ParseException;
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.hardcode.gdbms.engine.data.driver.DriverException;
 import com.hardcode.gdbms.engine.values.Value;
 import com.hardcode.gdbms.engine.values.ValueFactory;
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.cit.gvsig.CADExtension;
 import com.iver.cit.gvsig.EditionManager;
 import com.iver.cit.gvsig.EditionUtilities;
 import com.iver.cit.gvsig.ProjectExtension;
 import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
 import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileWriteException;
 import com.iver.cit.gvsig.exceptions.layers.CancelEditingLayerException;
 import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
 import com.iver.cit.gvsig.exceptions.table.CancelEditingTableException;
 import com.iver.cit.gvsig.exceptions.validate.ValidateRowException;
 import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
 import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
 import com.iver.cit.gvsig.fmap.MapControl;
 import com.iver.cit.gvsig.fmap.ViewPort;
 import com.iver.cit.gvsig.fmap.core.DefaultFeature;
 import com.iver.cit.gvsig.fmap.core.DefaultRow;
 import com.iver.cit.gvsig.fmap.core.FShape;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.IRow;
 import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
 import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
 import com.iver.cit.gvsig.fmap.drivers.ITableDefinition;
 import com.iver.cit.gvsig.fmap.edition.DefaultRowEdited;
 import com.iver.cit.gvsig.fmap.edition.EditionEvent;
 import com.iver.cit.gvsig.fmap.edition.EditionExceptionOld;
 import com.iver.cit.gvsig.fmap.edition.IEditableSource;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.fmap.edition.ISpatialWriter;
 import com.iver.cit.gvsig.fmap.edition.IWriteable;
 import com.iver.cit.gvsig.fmap.edition.IWriter;
 import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
 import com.iver.cit.gvsig.fmap.edition.rules.IRule;
 import com.iver.cit.gvsig.fmap.edition.rules.RulePolygon;
 import com.iver.cit.gvsig.fmap.layers.FBitSet;
 import com.iver.cit.gvsig.fmap.layers.FLayer;
 import com.iver.cit.gvsig.fmap.layers.FLayers;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.fmap.layers.SpatialCache;
 import com.iver.cit.gvsig.fmap.rendering.ILegend;
 import com.iver.cit.gvsig.gui.cad.DefaultCADTool;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 import com.iver.cit.gvsig.project.documents.table.ProjectTable;
 import com.iver.cit.gvsig.project.documents.table.gui.Table;
 import com.iver.cit.gvsig.project.documents.view.IProjectView;
 import com.iver.cit.gvsig.project.documents.view.gui.BaseView;
 
 /**
  * Class for start, stop or toggle the editing on a vector layer. Based on the
  * StartingEditing Extension of gvSIG
  * 
  * @author Nacho Varela
  * @author Javier Estevez
  * @author Pablo Sanxiao
  * @author Francisco Puga
  * @author Andres Maneiro
  */
 
 public class ToggleEditing {
 
     protected static Logger logger = Logger.getLogger("ToggleEditing");
     private FLyrVect layerVectorial = null;
     private VectorialEditableAdapter vea = null;
     private VectorialLayerEdited vle = null;
 
     public boolean toggle(FLayer layer) {
 	return true;
     }
 
     /**
      * @param layer
      *            The vectorial layer to be edited.
      */
     public void startEditing(FLayer layer) {
 
 	CADExtension.initFocus();
 	com.iver.andami.ui.mdiManager.IWindow f = PluginServices
 		.getMDIManager().getActiveWindow();
 
 	if (f instanceof BaseView) {
 	    BaseView vista = (BaseView) f;
 	    MapControl mapControl = vista.getMapControl();
 
 	    IProjectView model = vista.getModel();
 	    FLayers layers = model.getMapContext().getLayers();
 	    layers.setAllActives(false);
 
 	    if (layer instanceof FLyrVect) {
 		layer.setActive(true);
 		EditionManager editionManager = CADExtension
 			.getEditionManager();
 		editionManager.setMapControl(mapControl);
 		layerVectorial = (FLyrVect) layer;
 
 		layerVectorial.addLayerListener(editionManager);
 		ILegend legendOriginal = layerVectorial.getLegend();
 
 		try {
 		    layerVectorial.setEditing(true);
 		} catch (StartEditionLayerException e) {
 		    logger.error(e.getMessage(), e);
 		}
 		vea = (VectorialEditableAdapter) layerVectorial.getSource();
 		vea.getRules().clear();
 		try {
 		    if (vea.getShapeType() == FShape.POLYGON) {
 			IRule rulePol = new RulePolygon();
 			vea.getRules().add(rulePol);
 		    }
 		} catch (ReadDriverException e) {
 		    logger.error(e.getMessage(), e);
 		}
 
 		// if (!(layerVectorial.getSource().getDriver() instanceof
 		// IndexedShpDriver)) {
 		// vle = (VectorialLayerEdited)
 		// editionManager.getLayerEdited(layerVectorial);
 		// vle.setLegend(legendOriginal);
 		// }
 		vle = (VectorialLayerEdited) editionManager
 			.getLayerEdited(layerVectorial);
 		vea.getCommandRecord().addCommandListener(mapControl);
 		// If there's a table linked to this layer, its model is changed
 		// to VectorialEditableAdapter.
 		ProjectExtension pe = (ProjectExtension) PluginServices
 			.getExtension(ProjectExtension.class);
 		ProjectTable pt = pe.getProject().getTable(layerVectorial);
 		if (pt != null) {
 		    pt.setModel(vea);
 		    changeModelTable(pt, vea);
 		}
 		vista.repaintMap();
 	    }
 	}
     }
 
     public void startEditing(IEditableSource source) {
 	try {
 	    source.startEdition(EditionEvent.ALPHANUMERIC);
 	} catch (StartWriterVisitorException e) {
 	    logger.error(e.getMessage(), e);
 	}
     }
 
     /**
      * Checks the layers from the TOC and get the layer to be edited with its
      * name.
      * 
      * @param layerName
      *            The name of the layer to be edited.
      */
     @Deprecated
     public void startEditing(String layerName) {
 	FLayer layer = null;
 	com.iver.andami.ui.mdiManager.IWindow f = PluginServices
 		.getMDIManager().getActiveWindow();
 	if (f instanceof BaseView) {
 	    BaseView vista = (BaseView) f;
 	    IProjectView model = vista.getModel();
 	    FLayers layers = model.getMapContext().getLayers();
 	    layers.setAllActives(false);
 	    layer = layers.getLayer(layerName);
 	}
 	startEditing(layer);
     }
 
     /**
      * @param layer
      *            The layer wich edition will be stoped.
      * @param cancel
      *            false if we want to save the layer, true if we don't.
      */
     public void stopEditing(FLayer layer, boolean cancel) {
 
 	EditionManager edMan = CADExtension.getEditionManager();
 	vle = (VectorialLayerEdited) edMan.getActiveLayerEdited();
 	com.iver.andami.ui.mdiManager.IWindow f = PluginServices
 		.getMDIManager().getActiveWindow();
 
 	try {
 	    if (f instanceof BaseView) {
 		BaseView vista = (BaseView) f;
 		MapControl mapControl = vista.getMapControl();
 
 		IProjectView model = vista.getModel();
 		FLayers layers = model.getMapContext().getLayers();
 		layers.setAllActives(false);
 		if (cancel) {
 		    cancelEdition((FLyrVect) layer);
 		} else {
 		    saveLayer((FLyrVect) layer);
 		}
 
 		VectorialLayerEdited lyrEd = (VectorialLayerEdited) edMan
 			.getActiveLayerEdited();
 		try {
 		    ((FLyrVect) layer).getRecordset().removeSelectionListener(
 			    lyrEd);
 		} catch (ReadDriverException e) {
 		    NotificationManager
 			    .addError("Remove Selection Listener", e);
 		}
 
 		VectorialEditableAdapter vea = (VectorialEditableAdapter) ((FLyrVect) layer)
 			.getSource();
 		vea.getCommandRecord().removeCommandListener(mapControl);
 		layer.setEditing(false);
 		layer.setActive(true);
 	    }
 	} catch (DriverException e) {
 	    logger.error(e.getMessage(), e);
 	} catch (IOException e) {
 	    logger.error(e.getMessage(), e);
 	} catch (StartEditionLayerException e) {
 	    logger.error(e.getMessage(), e);
 	} catch (EditionExceptionOld e) {
 	    logger.error(e.getMessage(), e);
 	}
     }
 
     /**
      * @param construccionesLayer
      *            The name of the layer wich edition will be stoped.
      * @param cancel
      *            false if we want to save the layer, true if we don't.
      */
     @Deprecated
     public void stopEditing(String layerName, boolean cancel) {
 
 	FLayer layer = null;
 	com.iver.andami.ui.mdiManager.IWindow f = PluginServices
 		.getMDIManager().getActiveWindow();
 
 	if (f instanceof BaseView) {
 	    BaseView vista = (BaseView) f;
 	    IProjectView model = vista.getModel();
 	    FLayers layers = model.getMapContext().getLayers();
 	    layers.setAllActives(false);
 	    layer = layers.getLayer(layerName);
 	}
 
 	stopEditing(layer, cancel);
     }
 
     public void stopEditing(IEditableSource source) {
 	try {
 	    IWriteable w = (IWriteable) source;
 	    IWriter writer = w.getWriter();
 	    if (writer == null) {
 		NotificationManager.addError(
 			"No existe driver de escritura para la tabla"
 				+ source.getRecordset().getName(),
 			new EditionExceptionOld());
 	    } else {
 		ITableDefinition tableDef = source.getTableDefinition();
 		writer.initialize(tableDef);
 		source.stopEdition(writer, EditionEvent.ALPHANUMERIC);
 	    }
 	} catch (ReadDriverException e) {
 	    logger.error(e.getMessage(), e);
 	} catch (InitializeWriterException e) {
 	    logger.error(e.getMessage(), e);
 	} catch (StopWriterVisitorException e) {
 	    logger.error(e.getMessage(), e);
 	}
     }
 
     private void changeModelTable(ProjectTable pt, VectorialEditableAdapter vea) {
 	com.iver.andami.ui.mdiManager.IWindow[] views = PluginServices
 		.getMDIManager().getAllWindows();
 	for (int i = 0; i < views.length; i++) {
 	    if (views[i] instanceof Table) {
 		Table table = (Table) views[i];
 		ProjectTable model = table.getModel();
 		if (model.equals(pt)) {
 		    table.setModel(pt);
 		    vea.getCommandRecord().addCommandListener(table);
 		}
 	    }
 	}
     }
 
     private void cancelEdition(FLyrVect layer) throws IOException {
 	layer.setProperty("stoppingEditing", new Boolean(true));
 	com.iver.andami.ui.mdiManager.IWindow[] views = PluginServices
 		.getMDIManager().getAllWindows();
 	VectorialEditableAdapter vea = (VectorialEditableAdapter) layer
 		.getSource();
 	try {
 	    vea.cancelEdition(EditionEvent.GRAPHIC);
 	} catch (CancelEditingLayerException e) {
 	    logger.error(e.getMessage(), e);
 	}
 	for (int j = 0; j < views.length; j++) {
 	    if (views[j] instanceof Table) {
 		Table table = (Table) views[j];
 		if (table.getModel().getAssociatedTable() != null
 			&& table.getModel().getAssociatedTable().equals(layer)) {
 		    try {
 			table.cancelEditing();
 		    } catch (CancelEditingTableException e) {
 			logger.error(e.getMessage(), e);
 		    }
 		}
 	    }
 	}
 	layer.setProperty("stoppingEditing", new Boolean(false));
     }
 
     private void saveLayer(FLyrVect layer) throws DriverException,
 	    EditionExceptionOld {
 	layer.setProperty("stoppingEditing", new Boolean(true));
 	VectorialEditableAdapter vea = (VectorialEditableAdapter) layer
 		.getSource();
 
 	ISpatialWriter writer = (ISpatialWriter) vea.getWriter();
 	com.iver.andami.ui.mdiManager.IWindow[] views = PluginServices
 		.getMDIManager().getAllWindows();
 	for (int j = 0; j < views.length; j++) {
 	    if (views[j] instanceof Table) {
 		Table table = (Table) views[j];
 		if (table.getModel().getAssociatedTable() != null
 			&& table.getModel().getAssociatedTable().equals(layer)) {
 		    table.stopEditingCell();
 		}
 	    }
 	}
 	vea.cleanSelectableDatasource();
 	try {
 	    layer.setRecordset(vea.getRecordset());
 	} catch (ReadDriverException e) {
 	    logger.error(e.getMessage(), e);
 	}
 	// The layer recordset must have the changes we made
 	ILayerDefinition lyrDef;
 	try {
 	    lyrDef = EditionUtilities.createLayerDefinition(layer);
 	    String aux = "FIELDS:";
 	    FieldDescription[] flds = lyrDef.getFieldsDesc();
 	    for (int i = 0; i < flds.length; i++) {
 		aux = aux + ", " + flds[i].getFieldAlias();
 	    }
 	    // System.err.println("Escribiendo la capa " + lyrDef.getName() +
 	    // " con los campos " + aux);
 	    lyrDef.setShapeType(layer.getShapeType());
 	    writer.initialize(lyrDef);
 	    vea.stopEdition(writer, EditionEvent.GRAPHIC);
 	    layer.setProperty("stoppingEditing", new Boolean(false));
 	} catch (ReadDriverException e) {
 	    logger.error(e.getMessage(), e);
 	} catch (InitializeWriterException e) {
 	    logger.error(e.getMessage(), e);
 	} catch (StopWriterVisitorException e) {
 	    logger.error(e.getMessage(), e);
 	}
     }
 
     /**
      * Modidify a single value of a register. It creates the new value from its
      * String representation. IMPORTANT: StartEditing and StopEditing is
      * required before and after call this method.
      * 
      * @param layer
      *            the layer that contains the feature to be changed.
      * @param rowPos
      *            the data row to be changed.
      * @param colPos
      *            the data column position to be changed.
      * @param newValue
      *            the String representation of the value to be saved.
      * 
      */
     public void modifyValue(FLyrVect layer, int rowPos, int colPos,
 	    String newValue) throws Exception {
 	VectorialEditableAdapter edAdapter = (VectorialEditableAdapter) layer
 		.getSource();
 	if (newValue == null) {
 	    newValue = "";
 	}
 	ITableDefinition tableDef;
 	tableDef = edAdapter.getTableDefinition();
 	FieldDescription[] fieldDesc = tableDef.getFieldsDesc();
 	int type = fieldDesc[colPos].getFieldType();
 	if (type == 16) {
 	    // in this case type is boolean
 	    type = Types.BIT;
 	}
 	// System.out.println("El valor " + newValue + " es de tipo " +
 	// FieldDescription.typeToString(type));
 	Value val;
 	if (newValue.length() == 0) {
 	    val = ValueFactory.createNullValue();
 	} else {
 	    val = ValueFactory.createValueByType(newValue, type);
 	}
 	modifyValue(layer, rowPos, colPos, val);
     }
 
     /**
      * Modidify a single value of a register. IMPORTANT: StartEditing and
      * StopEditing is required before and after call this method.
      * 
      * @param layer
      *            the layer that contains the feature to be changed.
      * @param rowPos
      *            the data row to be changed.
      * @param colPos
      *            the data column position to be changed.
      * @param newValue
      *            the value to be saved.
      * 
      * @throws DriverException
      * @throws IOException
      * 
      */
     public void modifyValue(FLyrVect layer, int rowPos, int colPos,
 	    Value newValue) throws Exception {
 
 	if (newValue == null) {
 	    newValue = ValueFactory.createNullValue();
 	}
 	VectorialEditableAdapter edAdapter = (VectorialEditableAdapter) layer
 		.getSource();
 	IRowEdited row;
 	row = edAdapter.getRow(rowPos);
 	Value[] attributes = row.getAttributes();
 	ITableDefinition tableDef;
 	tableDef = edAdapter.getTableDefinition();
 	FieldDescription[] fieldDesc = tableDef.getFieldsDesc();
 	int type = fieldDesc[colPos].getFieldType();
 	if (type == 16) {
 	    // in this case type is boolean
 	    type = Types.BIT;
 	}
 	int valueType = newValue.getSQLType();
 	if (valueType == 0 || valueType == -1 || valueType == type) {
 	    if (row.getLinkedRow() instanceof IFeature) {
 		attributes[colPos] = newValue;
 		IGeometry geometry = ((DefaultFeature) row.getLinkedRow())
 			.getGeometry();
		IRow newRow = new DefaultFeature(geometry, attributes,
			row.getID());
 		edAdapter.modifyRow(rowPos, newRow, "NAVTABLE MODIFY",
 			EditionEvent.ALPHANUMERIC);
 	    } else {
 		logger.error("This is not a geometry");
 	    }
 	} else {
 	    logger.error("Tipo incorrecto: values es " + newValue.getSQLType()
 		    + " y el campo es " + type);
 	}
     }
 
     public void modifyValue(IEditableSource source, int rowPos, int colPos,
 	    String newValue) throws Exception {
 
 	if (newValue == null) {
 	    newValue = "";
 	}
 	ITableDefinition tableDef;
 	tableDef = source.getTableDefinition();
 	FieldDescription[] fieldDesc = tableDef.getFieldsDesc();
 	int type = fieldDesc[colPos].getFieldType();
 	if (type == 16) {
 	    // in this case type is boolean
 	    type = Types.BIT;
 	}
 	// System.out.println("El valor " + newValue + " es de tipo " +
 	// FieldDescription.typeToString(type));
 	Value val;
 	if (newValue.length() == 0) {
 	    val = ValueFactory.createNullValue();
 	} else {
 	    val = ValueFactory.createValueByType(newValue, type);
 	}
 	IRowEdited row = source.getRow(rowPos);
 	Value[] attributes = row.getAttributes();
 	attributes[colPos] = val;
 	IRow newRow = new DefaultRow(attributes);
 	source.modifyRow(rowPos, newRow, "NAVTABLE MODIFY",
 		EditionEvent.ALPHANUMERIC);
     }
 
     /**
      * Modify an the desired of values of a register IMPORTANT: StartEditing and
      * StopEditing is required before and after call this method.
      * 
      * @param layer
      *            the layer that contains the feature to be changed.
      * @param rowPos
      *            the data row to be changed.
      * @param attPos
      *            An array that contains the index of the columns that will be
      *            modified.
      * @param attStringValues
      *            the new values to save (in correlative order with attPos)
      * 
      * @throws DriverException
      * @throws IOException
      * 
      */
     public void modifyValues(FLyrVect layer, int rowPos, int[] attPos,
 	    String[] attStringValues) {
 
 	// definition of needed variables
 	int type;
 	Value[] attValues;
 	FieldDescription[] fieldDesc;
 	IGeometry geometry;
 	IRowEdited row;
 	VectorialEditableAdapter edAdapter;
 	try {
 	    // instantiate the needed classes and initialize vars
 	    edAdapter = (VectorialEditableAdapter) layer.getSource();
 	    row = edAdapter.getRow(rowPos);
 	    geometry = ((DefaultFeature) row.getLinkedRow()).getGeometry();
 	    attValues = row.getAttributes();
 	    ITableDefinition tableDef = edAdapter.getTableDefinition();
 	    fieldDesc = tableDef.getFieldsDesc();
 	} catch (Exception e) {
 	    logger.error(e.getMessage(), e);
 	    return;
 	}
 	// iterate throw the values that changed creating an array with all the
 	// new values of the row
 	for (int i = 0; i < attPos.length; i++) {
 	    // get the type of the layer's field
 	    type = fieldDesc[attPos[i]].getFieldType();
 	    if (type == 16) { // in this case type is boolean
 		type = Types.BIT;
 	    }
 	    // modify the value that changed
 	    if (attStringValues[i] == null || attStringValues[i].length() == 0) {
 		attValues[attPos[i]] = ValueFactory.createNullValue();
 	    } else {
 		try {
 		    attValues[attPos[i]] = ValueFactory.createValueByType(
 			    attStringValues[i], type);
 		    // System.out.println("El valor " + attStringValues[i] +
 		    // " es de tipo " + FieldDescription.typeToString(type));
 		} catch (NumberFormatException e) {
 		    logger.error("Tipo incorrecto: El valor del campo "
 			    + fieldDesc[attPos[i]].getFieldName() + " es \""
 			    + attStringValues[i] + "\" y debera ser "
 			    + FieldDescription.typeToString(type), e);
 		} catch (java.text.ParseException e) {
 		    logger.error(e.getMessage(), e);
 		}
 	    }
 	}
 	// change the file on memory
 	row.setAttributes(attValues);
 	IRow newRow = new DefaultFeature(geometry, attValues, row.getID());
 	try {
 	    edAdapter.modifyRow(rowPos, newRow, "NAVTABLE MODIFY",
 		    EditionEvent.ALPHANUMERIC);
 	} catch (Exception e) {
 	    logger.error(e.getMessage(), e);
 	}
     }
 
     public void modifyValues(IEditableSource source, int rowPosition,
 	    int[] attIndexes, String[] attValues) {
 	try {
 	    IRowEdited row;
 	    row = source.getRow(rowPosition);
 	    Value[] attributes = row.getAttributes();
 	    Value val;
 	    int type;
 	    ITableDefinition tableDef;
 	    tableDef = source.getTableDefinition();
 	    FieldDescription[] fieldDesc = tableDef.getFieldsDesc();
 	    for (int i = 0; i < attIndexes.length; i++) {
 		if (attValues[i].length() == 0 || attValues[i] == null) {
 		    val = ValueFactory.createNullValue();
 		} else {
 		    type = fieldDesc[attIndexes[i]].getFieldType();
 		    if (type == 16) {// in this case type is boolean
 			type = Types.BIT;
 		    }
 		    val = ValueFactory.createValueByType(attValues[i], type);
 		}
 		attributes[attIndexes[i]] = val;
 	    }
 	    IRow newRow = new DefaultRow(attributes);
 	    source.modifyRow(rowPosition, newRow, "NAVTABLE MODIFY",
 		    EditionEvent.ALPHANUMERIC);
 	} catch (ExpansionFileReadException e) {
 	    logger.error(e.getMessage(), e);
 	} catch (ReadDriverException e) {
 	    logger.error(e.getMessage(), e);
 	} catch (ParseException e) {
 	    logger.error(e.getMessage(), e);
 	} catch (ValidateRowException e) {
 	    logger.error(e.getMessage(), e);
 	}
     }
 
     public void modifyFeature(int index, IFeature row, String nameOfCADToolUsed) {
 	try {
	    /*
	     * TODO: use this method with CARE!! Test how row is calculated in
	     * other methods and modify this method to match that. In certain
	     * unidentified ocasions, row needs to be the row in recordset,
	     * other times, needs to be "row+1"
	     */
 	    vea.modifyRow(index, row, nameOfCADToolUsed, EditionEvent.GRAPHIC);
 	} catch (ValidateRowException e) {
 	    NotificationManager.addError(e.getMessage(), e);
 	} catch (ExpansionFileWriteException e) {
 	    NotificationManager.addError(e.getMessage(), e);
 	} catch (ReadDriverException e) {
 	    NotificationManager.addError(e.getMessage(), e);
 	}
 	// draw(row.getGeometry().cloneGeometry());
     }
 
     public void addGeometryWithParametrizedValues(IGeometry geometry,
 	    Value[] values, String nameOfCADToolUsed) {
 	try {
 	    String newFID = vea.getNewFID();
 	    DefaultFeature df = new DefaultFeature(geometry, values, newFID);
 	    int index = vea.addRow(df, nameOfCADToolUsed, EditionEvent.GRAPHIC);
 	    clearSelection();
 	    // ArrayList selectedRow = vle.getSelectedRow();
 
 	    ViewPort vp = vle.getLayer().getMapContext().getViewPort();
 	    BufferedImage selectionImage = new BufferedImage(
 		    vp.getImageWidth(), vp.getImageHeight(),
 		    BufferedImage.TYPE_INT_ARGB);
 	    Graphics2D gs = selectionImage.createGraphics();
 	    int inversedIndex = vea.getInversedIndex(index);
 	    vle.addSelectionCache(new DefaultRowEdited(df,
 		    IRowEdited.STATUS_ADDED, inversedIndex));
 	    vea.getSelection().set(inversedIndex);
 	    IGeometry geom = df.getGeometry();
 	    geom.cloneGeometry().draw(gs, vp, DefaultCADTool.selectionSymbol);
 	    vle.drawHandlers(geom.cloneGeometry(), gs, vp);
 	    vea.setSelectionImage(selectionImage);
 	    insertSpatialCache(geom);
 	} catch (ReadDriverException e) {
 	    NotificationManager.addError(e.getMessage(), e);
 	    return;
 	} catch (ValidateRowException e) {
 	    NotificationManager.addError(e.getMessage(), e);
 	    return;
 	}
 	// draw(geometry.cloneGeometry());
     }
 
     protected void insertSpatialCache(IGeometry geom) {
 	SpatialCache spatialCache = ((FLyrVect) vle.getLayer())
 		.getSpatialCache();
 	Rectangle2D r = geom.getBounds2D();
 	if (geom.getGeometryType() == FShape.POINT) {
 	    r = new Rectangle2D.Double(r.getX(), r.getY(), 1, 1);
 	}
 	spatialCache.insert(r, geom);
 
     }
 
     public void clearSelection() throws ReadDriverException {
 	if (vle != null) {
 	    ArrayList selectedRow = vle.getSelectedRow();
 	    ArrayList selectedHandlers = vle.getSelectedHandler();
 	    selectedRow.clear();
 	    selectedHandlers.clear();
 	}
 	if (vea != null) {
 	    FBitSet selection = vea.getSelection();
 	    selection.clear();
 	    vea.setSelectionImage(null);
 	    vea.setHandlersImage(null);
 	}
     }
 
 }
