 package com.dataart.devicehive.device.sample;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.dataart.android.devicehive.Command;
 import com.dataart.android.devicehive.DeviceClass;
 import com.dataart.android.devicehive.DeviceData;
 import com.dataart.android.devicehive.Network;
 import com.dataart.android.devicehive.Notification;
 import com.dataart.android.devicehive.device.CommandResult;
 import com.dataart.android.devicehive.device.Device;
 
 public class TestDevice extends Device {
 
 	private static final String TAG = "TestDevice";
 
 	private List<RegistrationListener> registrationListeners = new LinkedList<RegistrationListener>();
 	private List<CommandListener> commandListeners = new LinkedList<CommandListener>();
 	private List<NotificationListener> notificationListeners = new LinkedList<NotificationListener>();
 
 	public interface RegistrationListener {
 		void onDeviceRegistered();
 
 		void onDeviceFailedToRegister();
 	}
 
 	public interface CommandListener {
 		void onDeviceReceivedCommand(Command command);
 	}
 
 	public interface NotificationListener {
 		void onDeviceSentNotification(Notification notification);
 
 		void onDeviceFailedToSendNotification(Notification notification);
 	}
 
 	public TestDevice(Context context) {
 		super(context, getTestDeviceData());
 		attachEquipment(new TestEquipment());
 	}
 
 	private static DeviceData getTestDeviceData() {
 
 		final Network network = new Network(
				"Test Android Network(Device Framwork)",
				"Test Android Device Network(Device Framwork)");
 
 		final DeviceClass deviceClass = new DeviceClass(
				"Test Android Device Class(Device Framwork)", "1.0");
 
 		final DeviceData deviceData = new DeviceData(
 				"89A9435A-8DA6-4856-9061-31521A01FBD9",
 				"4D6B0A4A-CA77-4944-AAB0-52A7FE3DBD75",
				"Test Android Device(Device Framwork)",
 				DeviceData.DEVICE_STATUS_ONLINE, network, deviceClass);
 
 		return deviceData;
 	}
 
 	@Override
 	public CommandResult runCommand(final Command command) {
 		Log.d(TAG, "Executing command on test device: " + command.getCommand());
 
 		// execute command
 
 		if (shouldRunCommandAsynchronously(command)) {
 			runOnMainThread(new Runnable() {
 				@Override
 				public void run() {
 					notifyListenersCommandReceived(command);
 				}
 			});
 		} else {
 			notifyListenersCommandReceived(command);
 		}
 		return new CommandResult(CommandResult.STATUS_COMLETED,
 				"Executed on Android test device!");
 	}
 
 	@Override
 	public boolean shouldRunCommandAsynchronously(final Command command) {
 		return true;
 	}
 
 	public void addDeviceListener(RegistrationListener listener) {
 		registrationListeners.add(listener);
 	}
 
 	public void removeDeviceListener(RegistrationListener listener) {
 		registrationListeners.remove(listener);
 	}
 
 	public void addCommandListener(CommandListener listener) {
 		commandListeners.add(listener);
 	}
 
 	public void removeCommandListener(CommandListener listener) {
 		commandListeners.remove(listener);
 	}
 
 	public void addNotificationListener(NotificationListener listener) {
 		notificationListeners.add(listener);
 	}
 
 	public void removeNotificationListener(NotificationListener listener) {
 		notificationListeners.remove(listener);
 	}
 
 	public void removeListener(Object listener) {
 		registrationListeners.remove(listener);
 		commandListeners.remove(listener);
 		notificationListeners.remove(listener);
 	}
 
 	@Override
 	protected void onStartRegistration() {
 		Log.d(TAG, "onStartRegistration");
 	}
 
 	@Override
 	protected void onFinishRegistration() {
 		Log.d(TAG, "onFinishRegistration");
 		notifyListenersDeviceRegistered();
 	}
 
 	@Override
 	protected void onFailRegistration() {
 		Log.d(TAG, "onFailRegistration");
 		notifyListenersDeviceFailedToRegister();
 	}
 
 	@Override
 	protected void onStartProcessingCommands() {
 		Log.d(TAG, "onStartProcessingCommands");
 	}
 
 	@Override
 	protected void onStopProcessingCommands() {
 		Log.d(TAG, "onStopProcessingCommands");
 	}
 
 	@Override
 	protected void onStartSendingNotification(Notification notification) {
 		Log.d(TAG, "onStartSendingNotification : " + notification.getName());
 	}
 
 	@Override
 	protected void onFinishSendingNotification(Notification notification) {
 		Log.d(TAG, "onFinishSendingNotification : " + notification.getName());
 		notifyListenersDeviceSentNotification(notification);
 	}
 
 	@Override
 	protected void onFailSendingNotification(Notification notification) {
 		Log.d(TAG, "onFailSendingNotification : " + notification.getName());
 		notifyListenersDeviceFailedToSendNotification(notification);
 	}
 
 	private void notifyListenersCommandReceived(Command command) {
 		for (CommandListener listener : commandListeners) {
 			listener.onDeviceReceivedCommand(command);
 		}
 	}
 
 	private void notifyListenersDeviceRegistered() {
 		for (RegistrationListener listener : registrationListeners) {
 			listener.onDeviceRegistered();
 		}
 	}
 
 	private void notifyListenersDeviceFailedToRegister() {
 		for (RegistrationListener listener : registrationListeners) {
 			listener.onDeviceFailedToRegister();
 		}
 	}
 
 	private void notifyListenersDeviceSentNotification(Notification notification) {
 		for (NotificationListener listener : notificationListeners) {
 			listener.onDeviceSentNotification(notification);
 		}
 	}
 
 	private void notifyListenersDeviceFailedToSendNotification(
 			Notification notification) {
 		for (NotificationListener listener : notificationListeners) {
 			listener.onDeviceFailedToSendNotification(notification);
 		}
 	}
 
 }
