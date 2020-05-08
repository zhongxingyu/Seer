 package de.ptb.epics.eve.viewer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.csstudio.platform.data.ISeverity;
 import org.csstudio.utility.pv.PV;
 import org.csstudio.utility.pv.PVFactory;
 import org.csstudio.utility.pv.PVListener;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 import de.ptb.epics.eve.data.TransportTypes;
 import de.ptb.epics.eve.data.measuringstation.AbstractDevice;
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.Device;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 import de.ptb.epics.eve.ecp1.client.interfaces.IMeasurementDataListener;
 import de.ptb.epics.eve.ecp1.client.model.MeasurementData;
 
 public class CommonTableElement {
 	
 	private AbstractDevice device;
 	private TableViewer viewer;
 	private String name;
 	private CommonTableElementPV valuePv = null;
 	private CommonTableElementPV gotoPv = null;
 	private CommonTableElementPV unitPv = null;
 	private CommonTableElementPV setPv = null;
 	private CommonTableElementPV statusPv = null;
 	private CommonTableElementPV stopPv = null;
 	private CommonTableElementPV triggerPv = null;
 	private CommonTableElementPV tweakvaluePv = null;
 	private CommonTableElementPV tweakforwardPv = null;
 	private CommonTableElementPV tweakreversePv = null;
 	private String unit;
 	private CommonTableElementEngineData engine;
 	private boolean initialized = false;
 	private HashMap<String, CellEditor> cellEditorHash;
 	
 	public CommonTableElement(AbstractDevice abstractdevice, TableViewer viewer){
 		this.device = abstractdevice;
 		this.viewer = viewer;
 		name = abstractdevice.getName();
 		unit = "";
 		cellEditorHash = new HashMap<String, CellEditor>();
 
 
 		if( device instanceof MotorAxis ){
 			MotorAxis motorAxis = (MotorAxis)device;
 			engine = new CommonTableElementEngineData(abstractdevice.getID(), this);
 			if ( (motorAxis.getPosition() != null) && 
 					( motorAxis.getPosition().getAccess().getTransport() == TransportTypes.CA )) {
 				valuePv = new CommonTableElementPV(motorAxis.getPosition().getAccess().getVariableID(), this);
 				valuePv.setReadOnly(motorAxis.getPosition().getAccess().isReadOnly());
 			}
 			if ((motorAxis.getGoto().getAccess() != null) &&
 					(motorAxis.getGoto().getAccess().getTransport() == TransportTypes.CA)){
 				gotoPv = new CommonTableElementPV(motorAxis.getGoto().getAccess().getVariableID(), this);
 				gotoPv.setReadOnly(motorAxis.getGoto().getAccess().isReadOnly());
 				if (motorAxis.getGoto().isDiscrete()) gotoPv.setDiscreteValues((String[]) motorAxis.getGoto().getDiscreteValues().toArray(new String[0]));
 
 				// TODO getSet is not yet defined in xml
 				String setPvName = motorAxis.getGoto().getAccess().getVariableID();
 				if (setPvName.endsWith(".VAL")) setPvName.replaceFirst(".VAL$", "");
 				setPvName += ".SET";
 				setPv = new CommonTableElementPV(setPvName, this);
 				//if (motorAxis.getSet().isDiscrete()) setPv.setDiscreteValues((String[]) motorAxis.getSet().getDiscreteValues().toArray(new String[0]));
 			}
 			if (motorAxis.getUnit() != null){
 				if (motorAxis.getUnit().getAccess() != null) {
 					if (motorAxis.getUnit().getAccess().getTransport() == TransportTypes.CA){
 						unitPv = new CommonTableElementPV( motorAxis.getUnit().getAccess().getVariableID(), this);
 					}
 				}
 				else
 					unit = motorAxis.getUnit().getValue();
 			}
			if ((motorAxis.getStatus().getAccess() != null) &&
 					(motorAxis.getStatus().getAccess().getTransport() == TransportTypes.CA)){
 				statusPv = new CommonTableElementPV(motorAxis.getStatus().getAccess().getVariableID(), this);
 			}
			if ((motorAxis.getStop().getAccess() != null) &&
 					(motorAxis.getStop().getAccess().getTransport() == TransportTypes.CA)){
 				stopPv = new CommonTableElementPV(motorAxis.getStop().getAccess().getVariableID(), this);
 			}
			if ((motorAxis.getTweakForward().getAccess() != null) &&
 					(motorAxis.getTweakForward().getAccess().getTransport() == TransportTypes.CA)){
 				tweakforwardPv = new CommonTableElementPV(motorAxis.getTweakForward().getAccess().getVariableID(), this);
 			}
			if ((motorAxis.getTweakReverse().getAccess() != null) &&
 					(motorAxis.getTweakReverse().getAccess().getTransport() == TransportTypes.CA)){
 				tweakreversePv = new CommonTableElementPV(motorAxis.getTweakReverse().getAccess().getVariableID(), this);
 			}
			if ((motorAxis.getTweakValue().getAccess() != null) &&
 					(motorAxis.getTweakValue().getAccess().getTransport() == TransportTypes.CA)){
 				tweakvaluePv = new CommonTableElementPV(motorAxis.getTweakValue().getAccess().getVariableID(), this);
 				tweakvaluePv.setReadOnly(motorAxis.getTweakValue().getAccess().isReadOnly());
 				if (motorAxis.getTweakValue().isDiscrete()) tweakvaluePv.setDiscreteValues((String[]) motorAxis.getTweakValue().getDiscreteValues().toArray(new String[0]));
 			}
 		}
 		if( device instanceof DetectorChannel ){
 			DetectorChannel channel = (DetectorChannel)device;
 			engine = new CommonTableElementEngineData(abstractdevice.getID(), this);
 			if ((channel.getRead() != null) && 
 					( channel.getRead().getAccess().getTransport() == TransportTypes.CA )) {
 				valuePv = new CommonTableElementPV(  channel.getRead().getAccess().getVariableID(), this);
 				valuePv.setReadOnly(channel.getRead().getAccess().isReadOnly());
 			}
 			if (channel.getUnit() != null){
 				if (channel.getUnit().getAccess() != null) {
 					if (channel.getUnit().getAccess().getTransport() == TransportTypes.CA)
 						unitPv = new CommonTableElementPV( channel.getUnit().getAccess().getVariableID(), this);
 				}
 				else
 					unit = channel.getUnit().getValue();
 			}
 			//TODO missing TRIGGER ...
 		}
 		if( device instanceof Device ){
 			Device realDevice = (Device)device;
 			if ((realDevice.getValue() != null) && 
 					( realDevice.getValue().getAccess().getTransport() == TransportTypes.CA )) {
 				valuePv = new CommonTableElementPV( realDevice.getValue().getAccess().getVariableID(), this);
 				valuePv.setReadOnly(realDevice.getValue().getAccess().isReadOnly());
 			}
 			if (realDevice.getUnit() != null){
 				if (realDevice.getUnit().getAccess() != null) {
 					if (realDevice.getUnit().getAccess().getTransport() == TransportTypes.CA)
 						unitPv = new CommonTableElementPV( realDevice.getUnit().getAccess().getVariableID(), this);
 				}
 				else
 					unit = realDevice.getUnit().getValue();
 			}
 		}
 	}
 	
 	public void init() {
 		initialized  = true;
 	}
 
 	public void dispose(){
 		
 		if (valuePv != null) valuePv.dispose();
 		if (gotoPv != null) gotoPv.dispose();
 		if (unitPv != null) unitPv.dispose();
 		if (setPv != null) setPv.dispose();
 		if (triggerPv != null) triggerPv.dispose();
 		if (tweakvaluePv != null) tweakvaluePv.dispose();
 		if (tweakforwardPv != null) tweakforwardPv.dispose();
 		if (tweakreversePv != null) tweakreversePv.dispose();
 		cellEditorHash.clear();
 	}
 	
 	
 	public boolean isReadonly(String property){
 		if (property.equals("value") && (valuePv != null))
 			return valuePv.isReadOnly();
 		else if (property.equals("goto") && (gotoPv != null))
 			return gotoPv.isReadOnly();
 		else if (property.equals("tweakvalue") && (tweakvaluePv != null))
 			return tweakvaluePv.isReadOnly();
 		else if (property.equals("unit") && (unitPv != null))
 				return unitPv.isReadOnly();
 		else if (property.equals("set") && (setPv != null)) 
 			return setPv.isReadOnly();
 		
 		return true;
 	}
 	
 	public boolean isDiscrete(String property){
 
 		boolean pvDiscrete = false;
 		
 		if (property.equals("value")){
 			if (valuePv != null) pvDiscrete = valuePv.isDiscrete();
 		}
 		else if (property.equals("set")){
 			if (setPv != null) pvDiscrete = setPv.isDiscrete();
 		}
 		else if (property.equals("goto")){
 			if (gotoPv != null) pvDiscrete = gotoPv.isDiscrete();
 		}
 		else if (property.equals("tweakvalue")){
 			if (tweakvaluePv != null) pvDiscrete = tweakvaluePv.isDiscrete();
 		}
 		return pvDiscrete;
 	}
 
 	public boolean isConnected(String property) {
 		if ((property.equals("value")) && (valuePv != null))
 			return valuePv.isConnected();
 		if ((property.equals("name")) && (valuePv != null))
 			return valuePv.isConnected();
 		if ((property.equals("goto")) && (gotoPv != null))
 			return gotoPv.isConnected();
 		if ((property.equals("tweakvalue")) && (tweakvaluePv != null))
 			return tweakvaluePv.isConnected();
 		if ((property.equals("set")) && (setPv != null))
 			return setPv.isConnected();
 			
 		return false;
 	}
 	
 	// TODO colors should be COLOR_PV_CONNECTED/DISCONNECTED
 	public Color getConnectColor(String property) {
 		if (isConnected(property))
 			return Activator.getDefault().getColor("COLOR_PV_CONNECTED");
 		else
 			return Activator.getDefault().getColor("COLOR_PV_DISCONNECTED");
 	}
 
 	// TODO we need the severity Color here
 	public Color getSeverityColor(String property) {
 		String status = ""; 
 		if (property.equals("value")){
 			if (valuePv != null) status = valuePv.getStatus();
 		}
 		else if (property.equals("unit")){
 			if (unitPv != null) status = unitPv.getStatus();
 		}
 		else if (property.equals("goto")){
 			if (gotoPv != null) status = gotoPv.getStatus();
 		}
 		else if (property.equals("set")){
 			if (gotoPv != null) status = gotoPv.getStatus();
 		}
 		else if (property.equals("goto")){
 			if (gotoPv != null) status = gotoPv.getStatus();
 		}
 		else if (property.equals("tweakvalue")){
 			if (tweakvaluePv != null) status = tweakvaluePv.getStatus();
 		}
 		else if ((property.equals("status")) && (statusPv != null)) {
 			String statusVal = getValue("status");
 			if (statusVal.equals("Limit"))
 				return Activator.getDefault().getColor("COLOR_PV_MAJOR");
 			else
 				return Activator.getDefault().getColor("COLOR_PV_OK");
 		}
 
 		if(status.equals("OK"))
 			return Activator.getDefault().getColor("COLOR_PV_OK");
 		else if(status.equals("MINOR"))
 			return Activator.getDefault().getColor("COLOR_PV_MINOR");
 		else if(status.equals("MAJOR"))
 			return Activator.getDefault().getColor("COLOR_PV_MAJOR");
 		else if(status.equals("INVALID"))
 			return Activator.getDefault().getColor("COLOR_PV_INVALID");
 
 		return Activator.getDefault().getColor("COLOR_PV_INITIAL");
 	}
 
 	public String[] getSelectStrings(String property) {
 		if (isDiscrete(property)){
 			if (property.equals("value")){
 				if ((valuePv != null) && valuePv.isDiscrete()) 
 					return valuePv.getDiscreteValues();
 			}
 			else if (property.equals("goto")){
 				if ((gotoPv != null) && gotoPv.isDiscrete()) 
 					return gotoPv.getDiscreteValues();
 			}
 			else if (property.equals("set")){
 				if ((setPv != null) && setPv.isDiscrete())
 					return setPv.getDiscreteValues();
 			}
 		}
 		return new ArrayList<String>().toArray(new String[0]);
 	}
 
 	public void update() {
 
 		final CommonTableElement thisCommonTableElement = this;
 		if (!initialized) return;
 		if (!viewer.getControl().isDisposed()){
 			viewer.getControl().getDisplay().asyncExec(new Runnable() {
 				
 				@Override
 				public void run() {
 					if (!viewer.getControl().isDisposed()) {
 						viewer.update(thisCommonTableElement, null);
 					}
 				}
 			});
 		}
 	}
 
 	public void setCellEditor(CellEditor cellEditor, String column) {
 		cellEditorHash.put(column, cellEditor);
 	}
 
 	public CellEditor getCellEditor(String column) {
 		if (cellEditorHash.containsKey(column)){
 			return cellEditorHash.get(column);
 		}
 		else
 			return null;
 	}
 
 	public String getValue(String property) {
 		if (property.equals("set") && (setPv != null))
 			return setPv.getValue();
 		else if (property.equals("goto") && (gotoPv != null))
 			return gotoPv.getValue();
 		else if (property.equals("engine") && (engine != null))
 			return engine.getValue();
 		else if (property.equals( "name"))
 			return name;
 		else if (property.equals("value") && (valuePv != null))
 			return valuePv.getValue();
 		else if (property.equals("status") && (statusPv != null)){
 			String valueString = statusPv.getValue();
 			try {
 				int status = Integer.parseInt(valueString);
 				if (((status & 4) > 0) || ((status & 8192) > 0)) return "Limit";
 				else if ((status & 1024) > 0) return "Moving";
 				else return "Idle";
 			} catch (Exception e) {
 				return valueString;
 			}
 		}
 		else if (property.equals("unit")){
 			if (unitPv != null)
 				return unitPv.getValue();
 			else
 				return unit;
 		}
 		else if (property.equals( "tweakvalue") && (tweakvaluePv != null))
 			return tweakvaluePv.getValue();
 
 		return "";
 	}
 
 	public void setValue(Object value, String column) {
 		String newValue = "";
 		if (getCellEditor(column) instanceof ComboBoxCellEditor){
 			int index = 0;
 			String[] items = ((ComboBoxCellEditor)getCellEditor(column)).getItems();
 			if (value instanceof Integer) index = ((Integer)value).intValue();
 			if (items.length > index) newValue = items[index];
 		}
 		else if (value instanceof String)
 			newValue = (String) value;
 
 		if (column.equals("set") && (setPv != null))
 			setPv.setValue(newValue);
 		else if (column.equals("goto") && (gotoPv != null))
 			gotoPv.setValue(newValue);
 		else if (column.equals("value") && (valuePv != null))
 			valuePv.setValue(newValue);
 		else if (column.equals("unit") && (unitPv != null))
 			unitPv.setValue(newValue);
 		else if (column.equals("tweakvalue") && (tweakvaluePv != null))
 			tweakvaluePv.setValue(newValue);
 	}
 
 	public void trigger() {
 		if (triggerPv != null && triggerPv.isConnected()){
 			DetectorChannel channel = (DetectorChannel)device;
 			if (channel.getTrigger().getValue() != null)
 				triggerPv.setValue(channel.getTrigger().getValue().getDefaultValue());
 		}
 	}
 
 	public void stop() {
 		if (stopPv != null && stopPv.isConnected()){
 			MotorAxis axis = (MotorAxis)device;
 			if (axis.getStop().getValue() != null)
 				stopPv.setValue(axis.getStop().getValue().getDefaultValue());
 		}
 	}
 
 	public AbstractDevice getAbstractDevice() {
 		return device;
 	}
 
 	public void tweak(boolean forward) {
 		if (forward && (tweakforwardPv != null) && tweakforwardPv.isConnected()){
 			MotorAxis motorAxis = (MotorAxis)device;
 			if (motorAxis.getTweakForward().getValue() != null)
 				tweakforwardPv.setValue(motorAxis.getTweakForward().getValue().getDefaultValue());
 		}
 		else if (!forward && (tweakreversePv != null) && tweakreversePv.isConnected()){
 			MotorAxis motorAxis = (MotorAxis)device;
 			if (motorAxis.getTweakReverse().getValue() != null)
 			tweakreversePv.setValue(motorAxis.getTweakReverse().getValue().getDefaultValue());
 		}
 	}
 }
