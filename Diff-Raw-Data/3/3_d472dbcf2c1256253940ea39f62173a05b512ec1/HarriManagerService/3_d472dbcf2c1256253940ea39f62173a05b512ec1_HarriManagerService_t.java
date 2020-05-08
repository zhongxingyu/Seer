 package gov.usgs.cida.harri;
 
 import org.teleal.cling.UpnpService;
 import org.teleal.cling.UpnpServiceImpl;
 import org.teleal.cling.controlpoint.*;
 import org.teleal.cling.model.action.*;
 import org.teleal.cling.model.message.*;
 import org.teleal.cling.model.message.header.*;
 import org.teleal.cling.model.meta.*;
 import org.teleal.cling.model.types.*;
 import org.teleal.cling.registry.*;
 
 public class HarriManagerService implements Runnable {
 	public static final String DEVICE_PREFIX = "HARRI_Device";
 	public static final String DEVICE_MANUFACTURER = "CIDA";
 
 	public static void main(String[] args) throws Exception {
 		// Start a user thread that runs the UPnP stack
 		Thread clientThread = new Thread(new HarriManagerService());
 		clientThread.setDaemon(false);
 		clientThread.start();
 
 	}
 
 	public void run() {
 		try {
 
 			UpnpService upnpService = new UpnpServiceImpl();
 
 			// Add a listener for device registration events
 			upnpService.getRegistry().addListener(
 					createRegistryListener(upnpService)
 					);
 
 			// Broadcast a search message for all devices
 			upnpService.getControlPoint().search(
 					new STAllHeader()
 					);
 
 		} catch (Exception ex) {
 			System.err.println("Exception occured: " + ex);
 			System.exit(1);
 		}
 	}
 
 	private RegistryListener createRegistryListener(final UpnpService upnpService) {
 		return new DefaultRegistryListener() {
 			private boolean isHarriDevice(final RemoteDevice device) {
				return device.getDetails().getManufacturerDetails()!=null &&
						device.getDetails().getManufacturerDetails().getManufacturer().equals(DEVICE_MANUFACTURER) && 
 						device.getDetails().getModelDetails().getModelName().contains(DEVICE_PREFIX);
 			}
 			
 			@Override
 			public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
 				if(!isHarriDevice(device)){
 					return;
 				}
 				//if not a HARRI device, do nothing
 				System.out.println("HARRI Device has been added: " + device.getDetails().getModelDetails().getModelName());
 				doExampleServiceCall(upnpService, device); //TODO delete when not needed
 			}
 
 			@Override
 			public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
 				if(!isHarriDevice(device)){
 					return;
 				}
 				System.out.println("HARRI Device " + device.getDetails().getModelDetails().getModelName() + " has been removed!");
 			}
 
 		};
 	}
 	
 	//TODO remove example
 	private void doExampleServiceCall(final UpnpService upnpService, final RemoteDevice device){
 		ServiceId serviceId = new UDAServiceId("ExampleHarriService"); //NOTE: a service on the device is annotated with this value
 		Service exampleHarriAction;
 		if ((exampleHarriAction = device.findService(serviceId)) != null) {
 
 			System.out.println("HARRI Service discovered on device " + device.getDetails().getModelDetails().getModelName() + ": " + exampleHarriAction);
 			executeAction(upnpService, exampleHarriAction);
 		}
 	}
 	
 	//TODO remove example
 	private void executeAction(UpnpService upnpService, Service exampleHarriActionService) {
 
 		ActionInvocation setTargetInvocation =
 				new ExampleHarriActionInvocation(exampleHarriActionService);
 
 		// Executes asynchronous in the background
 		upnpService.getControlPoint().execute(
 				new ActionCallback(setTargetInvocation) {
 
 					@Override
 					public void success(ActionInvocation invocation) {
 						assert invocation.getOutput().length == 0;
 						System.out.println("Successfully called remote action on HARRI device!");
 					}
 
 					@Override
 					public void failure(ActionInvocation invocation,
 							UpnpResponse operation,
 							String defaultMsg) {
 						System.err.println(defaultMsg);
 					}
 				}
 				);
 
 	}
 
 	//TODO remove example
 	private class ExampleHarriActionInvocation extends ActionInvocation {
 		ExampleHarriActionInvocation(Service service) {
 			super(service.getAction("DoExampleAction")); //NOTE: this string is a method in the service
 			try {
 				// Throws InvalidValueException if the value is of wrong type
 				setInput("HarriManagerId", "EXAMPLE_HARRI_MANAGER_ID"); //TODO get this example harri manager id from somewhere useful
 			} catch (InvalidValueException ex) {
 				System.err.println(ex.getMessage());
 				System.exit(1);
 			}
 		}
 	}
 }
