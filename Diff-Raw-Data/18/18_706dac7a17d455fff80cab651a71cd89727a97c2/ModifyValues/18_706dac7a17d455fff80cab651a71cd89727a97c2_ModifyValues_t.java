 package es.udc.cartolab.gvsig.epanet.cad;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.hardcode.gdbms.engine.values.Value;
 import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
 import com.iver.cit.gvsig.exceptions.validate.ValidateRowException;
 import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
 import com.iver.cit.gvsig.fmap.core.DefaultFeature;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.core.IRow;
 import com.iver.cit.gvsig.fmap.edition.EditionEvent;
 import com.iver.cit.gvsig.fmap.edition.IEditableSource;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 
 import es.udc.cartolab.gvsig.epanet.exceptions.ExternalError;
 import es.udc.cartolab.gvsig.navtable.ToggleEditing;
 
 public class ModifyValues extends ToggleEditing {
 
     private FLyrVect layer;
 
     public ModifyValues(FLyrVect layer) {
 	this.layer = layer;
     }
 
     public void startEditing() {
 	startEditing(layer);
     }
 
     public void stopEditing() {
 	try {
 	    stopEditing(layer, false);
 	} catch (StopWriterVisitorException e) {
 	    cancelEditing();
	    throw new ExternalError(e);
 	}
     }
 
     public void cancelEditing() {
 	boolean dontSave = true;
 	try {
 	    stopEditing(layer, dontSave);
 	} catch (StopWriterVisitorException e1) {
 	}
     }
 
     public void modifyValues(int rowPos, int[] indexes, Value[] values) {
 
 	try {
 	    doModifyValues(rowPos, indexes, values);
 	} catch (ExpansionFileReadException e) {
 	    cancelEditing();
 	    throw new ExternalError(e);
 	} catch (ReadDriverException e) {
 	    cancelEditing();
 	    throw new ExternalError(e);
 	} catch (ValidateRowException e) {
 	    cancelEditing();
 	    throw new ExternalError(e);
 	}
     }
 
     private void doModifyValues(int rowPos, int[] indexes, Value[] values)
 	    throws ExpansionFileReadException, ReadDriverException,
 	    ValidateRowException {
 	IEditableSource source = (IEditableSource) layer.getSource();
 	IRowEdited row = source.getRow(rowPos);
 	Value[] attributes = row.getAttributes();
 
 	for (int i = 0; i < indexes.length; i++) {
 	    if (indexes[i] != -1) {
 		attributes[indexes[i]] = values[i];
 	    }
 	}
 
 	IGeometry geom = ((DefaultFeature) row.getLinkedRow()).getGeometry();
 	IRow newRow = new DefaultFeature(geom, attributes, row.getID());
 	source.modifyRow(rowPos, newRow, "epanet", EditionEvent.ALPHANUMERIC);
     }
 
 }
