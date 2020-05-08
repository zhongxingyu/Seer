 package com.niffy.logforwarder.client;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.niffy.logforwarder.lib.ClientSelector;
 import com.niffy.logforwarder.lib.logmanagement.ILogOwner;
 import com.niffy.logforwarder.lib.logmanagement.LogRequest;
 import com.niffy.logforwarder.lib.messages.IMessage;
 import com.niffy.logforwarder.lib.messages.MessageDeleteRequest;
 import com.niffy.logforwarder.lib.messages.MessageDeleteResponse;
 import com.niffy.logforwarder.lib.messages.MessageError;
 import com.niffy.logforwarder.lib.messages.MessageFlag;
 import com.niffy.logforwarder.lib.messages.MessageSendRequest;
 import com.niffy.logforwarder.lib.messages.MessageSendDataFile;
 
 public class Requester implements ILogOwner {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 	private final Logger log = LoggerFactory.getLogger(Requester.class);
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	protected ClientSelector mSelector;
 	protected HashMap<String, Device> mDevices = new HashMap<String, Device>();
 	protected Setting mSetting;
 	protected AtomicInteger mSeq = new AtomicInteger();
 	protected int mVersion = -1;
 	/**
 	 * Key = Seq num Value = Logrequest
 	 */
 	protected HashMap<Integer, LogRequest<IMessage>> mRequests = new HashMap<Integer, LogRequest<IMessage>>();
 	/**
 	 * Key = Seq num Value = Device its for
 	 */
 	protected HashMap<Integer, Device> mRequestDeviceCrossRef = new HashMap<Integer, Device>();
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	public Requester(final ClientSelector pSelector, final HashMap<String, Device> pDevices, final Setting pSetting,
 			final int pVersion) {
 		this.mSelector = pSelector;
 		this.mDevices = pDevices;
 		this.mSetting = pSetting;
 		this.mVersion = pVersion;
 	}
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 	@Override
 	public <T extends IMessage> void handleResponse(int pRequest, T Message) {
 		this.handleMessage(pRequest, Message);
 	}
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	public void getAll() {
 		ArrayList<String> Devices = this.mSetting.getDevices();
 		for (String key : Devices) {
 			Device device = this.mDevices.get(key);
 			if (device != null) {
 				this.queueGetRequest(device);
 			} else {
 				log.error("Could not find device obj for : {}", key);
 			}
 		}
 	}
 
 	public void getSingle(final Device pDevice) {
 		this.queueGetRequest(pDevice);
 	}
 
 	public void deleteAll() {
 		ArrayList<String> Devices = this.mSetting.getDevices();
 		for (String key : Devices) {
 			Device device = this.mDevices.get(key);
 			if (device != null) {
 				this.queueDeleteRequest(device);
 			} else {
 				log.error("Could not find device obj for : {}", key);
 			}
 		}
 	}
 
 	public void deleteSingle(final Device pDevice) {
 		this.queueDeleteRequest(pDevice);
 	}
 
 	protected void queueGetRequest(final Device pDevice) {
 		LogRequest<IMessage> request = this.produceGetRequest(pDevice);
 		if (request != null) {
 			this.mSelector.addRequest(request);
 		} else {
 			log.error("Could not produce a get request for device: {}", pDevice.getName());
 		}
 	}
 
 	protected LogRequest<IMessage> produceGetRequest(final Device pDevice) {
 		final int pSequence = this.mSeq.getAndIncrement();
 		InetSocketAddress pAddress = this.getAddress(pDevice.getAddress(), this.getDevicePort(pDevice));
 		if (pAddress == null) {
 			log.error("Could not get address and port for device: {}", pDevice.getName());
 			return null;
 		}
 		MessageSendRequest pMessage = new MessageSendRequest(this.mVersion, MessageFlag.SEND_REQUEST.getNumber());
 		pMessage.setSDCard(this.mSetting.getSDCard());
 		pMessage.setLogFileNameAndPath(this.mSetting.getFileNamePath());
 		pMessage.setSequence(pSequence);
 		LogRequest<IMessage> request = new LogRequest<IMessage>(pSequence, pAddress, pMessage, this);
 		this.mRequests.put(request.getClientRequest(), request);
 		this.mRequestDeviceCrossRef.put(pSequence, pDevice);
 		return request;
 	}
 
 	protected void queueDeleteRequest(final Device pDevice) {
 		LogRequest<IMessage> request = this.produceDeleteRequest(pDevice);
 		if (request != null) {
 			this.mSelector.addRequest(request);
 		} else {
 			log.error("Could not produce a delete request for device: {}", pDevice.getName());
 		}
 	}
 
 	protected LogRequest<IMessage> produceDeleteRequest(final Device pDevice) {
 		final int pSequence = this.mSeq.getAndIncrement();
 		InetSocketAddress pAddress = this.getAddress(pDevice.getAddress(), this.getDevicePort(pDevice));
 		if (pAddress == null) {
 			log.error("Could not get address and port for device: {}", pDevice.getName());
 			return null;
 		}
 		MessageDeleteRequest pMessage = new MessageDeleteRequest(this.mVersion, MessageFlag.DELETE_REQUEST.getNumber());
 		pMessage.setSDCard(this.mSetting.getSDCard());
 		pMessage.setLogFileNameAndPath(this.mSetting.getFileNamePath());
 		pMessage.setSequence(pSequence);
 		LogRequest<IMessage> request = new LogRequest<IMessage>(pSequence, pAddress, pMessage, this);
 		this.mRequests.put(request.getClientRequest(), request);
 		this.mRequestDeviceCrossRef.put(pSequence, pDevice);
 		return request;
 	}
 
 	protected <T extends IMessage> void handleMessage(int pRequest, T pMessage) {
 		int flag = pMessage.getMessageFlag();
 		if (flag == MessageFlag.DELETE_RESPONSE.getNumber()) {
 			this.handleDeleteResponse(pMessage);
 		} else if (flag == MessageFlag.SEND_DATA_FILE.getNumber()) {
 			this.handleGetResponse(pMessage);
 		} else if (flag == MessageFlag.ERROR.getNumber()) {
 			this.handleErrorResponse(pMessage);
 		}
 	}
 
 	protected <T extends IMessage> void handleGetResponse(T pMessage) {
 		MessageSendDataFile response = (MessageSendDataFile) pMessage;
 		this.writeFile(response.getData(), this.mRequestDeviceCrossRef.get(response.getSequence()));
 		this.mRequests.remove(pMessage.getSequence());
 		this.mRequestDeviceCrossRef.remove(pMessage.getSequence());
 	}
 
 	protected <T extends IMessage> void handleDeleteResponse(T pMessage) {
 		MessageDeleteResponse response = (MessageDeleteResponse) pMessage;
 		boolean deleted = response.getDeleted();
 		if (deleted) {
 			log.info("Seq: {} was deleted", response.getSequence());
 		} else {
 			log.warn("Seq: {} was NOT deleted", response.getSequence());
 		}
 		this.mRequests.remove(pMessage.getSequence());
 		this.mRequestDeviceCrossRef.remove(pMessage.getSequence());
 	}
 
 	protected <T extends IMessage> void handleErrorResponse(T pMessage) {
 		MessageError error = (MessageError) pMessage;
 		log.error("Server could not handle request Seq: {} Msg: {}", error.getSequence(), error.getError());
 	}
 
 	protected void writeFile(final byte[] pData, final Device pDevice) {
 		if (pDevice != null) {
 			FileOutputStream fop = null;
			String pPath = this.mSetting.getStoragePath() + pDevice.getFileName();
 			try {
 				File file = new File(pPath);
 				if (!file.exists()) {
 					file.createNewFile();
 				}
 				fop = new FileOutputStream(file);
 				fop.write(pData);
 				fop.flush();
 				fop.close();
 			} catch (IOException e) {
 				log.error("File operations error for device: {}", pDevice.getName());
 			} finally {
 				try {
 					if (fop != null) {
 						fop.close();
 						log.info("Wrote file for device: {} to {}", pDevice.getName(), pPath);
 					}
 				} catch (IOException e) {
 					log.error("Could not close file writter", e);
 				}
 			}
 		} else {
 			log.error("Could not write to file as device is null");
 		}
 	}
 
 	protected int getDevicePort(final Device pDevice) {
 		int pDefaultPort = this.mSetting.getServerPort();
 		int pDevicePort = pDevice.getPort();
 		if (pDevicePort != -1) {
 			return pDevicePort;
 		} else {
 			return pDefaultPort;
 		}
 	}
 
 	protected InetSocketAddress getAddress(final String pIP, int pPort) {
 		try {
 			InetAddress pInetAddy = InetAddress.getByName(pIP);
 			return new InetSocketAddress(pInetAddy, pPort);
 		} catch (UnknownHostException e) {
 			log.error("Could not get InetSocketAddress for: {}", pIP);
 		}
 		return null;
 	}
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 
 }
