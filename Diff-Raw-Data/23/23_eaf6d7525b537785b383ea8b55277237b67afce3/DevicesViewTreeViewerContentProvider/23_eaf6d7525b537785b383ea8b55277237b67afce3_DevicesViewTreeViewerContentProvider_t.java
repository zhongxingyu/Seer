 package de.ptb.epics.eve.viewer.views.devicesview;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 
 import de.ptb.epics.eve.data.measuringstation.Detector;
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.Device;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.Motor;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 
 /**
  * <code>DevicesViewTreeViewerContentProvider</code> is the content provider of 
  * the tree viewer defined in 
  * {@link de.ptb.epics.eve.viewer.views.devicesview.DevicesView}.
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public class DevicesViewTreeViewerContentProvider 
 						implements ITreeContentProvider, IModelUpdateListener {
 
 	// the input of the tree viewer
 	private IMeasuringStation measuringStation;
 
 	// a reference to the viewer this provider is assigned to
 	private Viewer viewer;
 	
 	// lists of devices that don't have class names
 	private List<Motor> motorsWithoutClassName;
 	private List<Detector> detectorsWithoutClassName;
 	private List<Device> devicesWithoutClassName;
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean hasChildren(final Object element) {
 		
 		if(element instanceof IMeasuringStation) {
 			// the measuring station has children if it contains 
 			// motors, detectors or devices
 			final IMeasuringStation measuringStation = (IMeasuringStation)element;
 			return  measuringStation.getMotors().size() > 0 || 
 					measuringStation.getDetectors().size() > 0 || 
 					measuringStation.getDevices().size() > 0 || 
 					measuringStation.getEvents().size() > 0;
 		} else if(element instanceof List<?>) {
 			// a list of motors, detectors or devices has children if it is not empty
 			return (((List<?>)element).size() > 0);
 		} else if(element instanceof Motor) {
 			// a motor has children if it has axes
 			return ((Motor)element).getAxes().size() > 0;
 		} else if(element instanceof Detector) {
 			// a detector has children if it has channels
 			return ((Detector)element).getChannels().size() > 0;
 		} else if(element instanceof MotorAxis) {
 			// a motor axis has no children
 			return false;
 		} else if(element instanceof DetectorChannel) {
 			// a detector channel has no children
 			return false;
 		} else if(element instanceof Device) {
 			// a device has no children
 			return false;
 		} else if(element instanceof String) {
 			// a class has children if it has any devices
 			return (measuringStation.getDeviceList((String)element).size() > 0);
 		}
 		// all elements other than a measuring station do not have children
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Object[] getChildren(final Object parentElement) {
 		
 		if(parentElement instanceof String) {
 			// return all devices (children) of a class
 			return measuringStation.getDeviceList((String) parentElement).toArray();
 		} else if(parentElement instanceof List<?>) {
 			// return all motors/detectors/devices without a class name
 			Object object = ((List<?>)parentElement).get(0);
 			if(object instanceof Motor) {
 				return motorsWithoutClassName.toArray();
 			}
 			else if(object instanceof Detector) {
 				return detectorsWithoutClassName.toArray();
 			}
 			else if(object instanceof Device) {
 				return devicesWithoutClassName.toArray();
 			}
 		} else if(parentElement instanceof Motor) {
 			// return all axes of a motor
 			List<MotorAxis> returnList = new ArrayList<MotorAxis>();
 			final Motor motor = (Motor)parentElement;
 			final List<MotorAxis> axis = motor.getAxes();
 			for(final MotorAxis ma : axis) {
 				if(ma.getClassName().isEmpty()) {
 					returnList.add(ma);
 				}
 			}
 			return returnList.toArray();
 		} else if(parentElement instanceof Detector) {
 			// return all channels of a detector
 			List<DetectorChannel> returnList = new ArrayList<DetectorChannel>();
 			final Detector detector = (Detector)parentElement;
 			final List<DetectorChannel> channels = detector.getChannels();
 			for(final DetectorChannel ch : channels) {
 				if(ch.getClassName().isEmpty()) {
 					returnList.add(ch);
 				}
 			}
 			return returnList.toArray();
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Object[] getElements(final Object inputElement) {
 		/*
 		 * if the input is a measuring station the tree viewer gets the 
 		 * following root elements:
 		 *  - all classes
 		 *  - all motors without a class name
 		 *  - all detectors without a class name
 		 *  - all devices without a class name
 		 */
 		if(inputElement instanceof IMeasuringStation) {
 			final IMeasuringStation measuringStation = 
 					(IMeasuringStation)inputElement;
 			List<Object> returnList = new ArrayList<Object>();
 			
 			// add all classes as elements
 			if (measuringStation.getClassNameList().size() > 0){
 				returnList.addAll(measuringStation.getClassNameList());
 			}
 			
 			// add motors without class names as motors entry
			if (motorsWithoutClassName != null && 
				!motorsWithoutClassName.isEmpty()) {
 				returnList.add(motorsWithoutClassName);
 			}
 			// add detectors without class names as detectors entry
			if (detectorsWithoutClassName != null && 
				!detectorsWithoutClassName.isEmpty()) {
 				returnList.add(detectorsWithoutClassName);
 			}
 			// add devices without class names as devices entry
			if (devicesWithoutClassName != null && 
				!devicesWithoutClassName.isEmpty()) {
 				returnList.add(devicesWithoutClassName);
 			}
 			return returnList.toArray();
 		} 
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Object getParent(final Object element) {
 	
 		if(element instanceof List<?>) {
 			return this.measuringStation;
 		} else if(element instanceof Motor) {
 			if(((Motor)element).getClassName().isEmpty()) {
 				return motorsWithoutClassName;
 			} else {
 				return ((Motor)element).getClass();
 			}
 		} else if(element instanceof Detector) {
 			if(((Detector)element).getClassName().isEmpty()) {
 				return detectorsWithoutClassName;
 			} else {
 				return ((Detector)element).getClass();
 			}
 		} else if(element instanceof Device) {
 			if(((Device)element).getClassName().isEmpty()) {
 				return devicesWithoutClassName;
 			} else {
 				return ((Device)element).getClass();
 			}
 		} else if(element instanceof MotorAxis) {
 			return ((MotorAxis)element).getParent();
 		} else if(element instanceof DetectorChannel) {
 			return ((DetectorChannel)element).getParent();
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dispose() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void inputChanged(final Viewer viewer, final Object oldInput, 
 			final Object newInput) {
 		if(measuringStation != null) {
 			measuringStation.removeModelUpdateListener(this);
 		}
 		measuringStation = (IMeasuringStation)newInput;
 		if(measuringStation != null) {
 			measuringStation.addModelUpdateListener(this);
 		}
 		
 		this.viewer = viewer;
 		
 		// a new measuring station was set -> collect devices without class names
 		updateDeviceLists();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateEvent(ModelUpdateEvent modelUpdateEvent) {
 		updateDeviceLists();
 		viewer.refresh();
 	}
 	
 	/*
 	 * helper function which builds lists of motors, detectors and devices 
 	 * that don't have class names
 	 */
 	private void updateDeviceLists() {
 		if(measuringStation.getMotors().size() > 0) {
 			motorsWithoutClassName = new ArrayList<Motor>();
 			for(Motor m : measuringStation.getMotors()) {
 				if(m.getClassName().isEmpty()) {
 					motorsWithoutClassName.add(m);
 				}
 			} 
 		} else {
 			motorsWithoutClassName = null;
 		}
 		
 		if(measuringStation.getDetectors().size() > 0) {
 			detectorsWithoutClassName = new ArrayList<Detector>();
 			for(Detector det : measuringStation.getDetectors()) {
 				if(det.getClassName().isEmpty()) {
 					detectorsWithoutClassName.add(det);
 				}
 			} 
 		} else {
 			detectorsWithoutClassName = null;
 		}
 		
 		if(measuringStation.getDevices().size() > 0) {
 			devicesWithoutClassName = new ArrayList<Device>();
 			for(Device dev : measuringStation.getDevices()) {
 				if(dev.getClassName().isEmpty()) {
 					devicesWithoutClassName.add(dev);
 				}
 			} 
 		} else {
 			devicesWithoutClassName = null;
 		}
 	}
 }
