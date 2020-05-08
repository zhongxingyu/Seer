 package src;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.IntBuffer;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.usb.UsbException;
 import javax.usb.UsbHostManager;
 import javax.usb.UsbServices;
 
 import de.ailis.usb4java.libusb.Device;
 import de.ailis.usb4java.libusb.DeviceDescriptor;
 import de.ailis.usb4java.libusb.DeviceHandle;
 import de.ailis.usb4java.libusb.DeviceList;
 import de.ailis.usb4java.libusb.LibUsb;
 
 public class LibUSBTest implements Runnable {
 
 	public static void main(String[] args) {
 		LibUSBTest test = new LibUSBTest();
 
 		//start USB
 		new Thread(test).start();
 
 		//wait 20 sec
 		try {
 			Thread.sleep(20000); 
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		//close it
 		test.fermati.set(true);
 		System.out.println("Chiudo programma");
 	}
 
 	public final AtomicBoolean fermati = new AtomicBoolean(false);
 
 	private Device STM;
 
 	AtomicInteger[] valore = new AtomicInteger[3];
 
 	private USBLIstener listener;
 
 	public LibUSBTest(){
 		for(int i = 0; i<valore.length; i++){
 			valore[i] = new AtomicInteger();
 		}
 	}
 
 	private void retrieveSTM() {
 
 		STM = null;
 
 		try {
 			// do not remove, hidden inizialization here!
 			UsbServices services = UsbHostManager.getUsbServices();
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UsbException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		DeviceList list = new DeviceList();
 		LibUsb.getDeviceList(null, list);
 		for (Device d : list) {
 			DeviceDescriptor descriptor = new DeviceDescriptor();
 			LibUsb.getDeviceDescriptor(d, descriptor);
 			// System.out.println(descriptor.dump());
 			if (descriptor.idProduct() == 0x5710
 					&& descriptor.idVendor() == 0x0483) {
 				System.out.println("Found ");
 				System.out.println(descriptor.dump());
 				STM = d;
 			}
 		}
 
 	}
 
 	@Override
 	public void run() {
 		while (!fermati.get()) {
 			retrieveSTM();
 
 			if (STM != null) {
 				DeviceHandle STMhandle = new DeviceHandle();
 				//LibUsb.open(STM, STMhandle);
 				System.out.println(LibUsb.errorName(LibUsb.open(STM, STMhandle)));
 				System.out.println(STMhandle);
 				ByteBuffer data = ByteBuffer.allocateDirect(512);
 				data.order(ByteOrder.LITTLE_ENDIAN);
 				IntBuffer transferred = IntBuffer.allocate(1);
 				long time, time2;
 				time = System.nanoTime();
 				int result = 0;
 				int lastSeq = -1;
 				int[] packetNumber = new int[6];
 				long lastTime = System.nanoTime();
 				int diff=0;
 				while (result == 0 && !fermati.get() ) {
 					//System.out.println(i);
 					if(System.nanoTime()-lastTime>=1000000000 && listener != null){
 						System.out.print("Numero pacchetti:");
 						for(int j = 0; j<packetNumber.length; j++){
 							System.out.print(" "+packetNumber[j]);
 							packetNumber[j] = 0;
 						}
 						System.out.println();
 
 						System.out.println("differenti "+diff);
 						diff = 0;
 						lastTime = System.nanoTime();
 
 					}
 					result = LibUsb.bulkTransfer(STMhandle, 0x81, data, transferred, 0);
 					int transferredBytes = transferred.get();
 					//System.out.println("Trasnferred bytes: "+ transferredBytes);
 					//System.out.println(LibUsb.errorName(result));
 					while(data.position()<transferredBytes){
 						int currentSeq = data.getShort() & 0xFFFF;
 						int packetType = data.getShort() & 0xFFFF;
 						
 						if (packetType == 3){//if STRING
 							System.out.print("Letto da USB: ");
 							
 							byte c;
 							while ( (c = data.get())!='\0' && data.hasRemaining())
 								System.out.print((char)c);
 							
 							System.out.println();
 							
 						}
 						
 						if (packetType == 4){//if DCM
 							float q[] = new float[4];
 							q[0] = data.getFloat();
 							q[1] = data.getFloat();
 							q[2] = data.getFloat();
 							q[3] = data.getFloat();
 							if (listener!=null)
 								listener.setDCM(q);
 							
 						}
 						
 						if (packetType == 5){//if ANGLE
 							float ypr[] = new float[3];
 							ypr[0] = data.getFloat();
 							ypr[1] = data.getFloat();
 							ypr[2] = data.getFloat();
 							if (listener!=null)
 								listener.setEulerianBypass(ypr);
 							
 						}
 						
 						if (listener!=null  && packetType >= 0 && packetType <= 2){
 							short value1 = data.getShort();
 							short value2 = data.getShort();
 							short value3 = data.getShort();
 
 						
 							switch (packetType) {
 							case 0:		
 								listener.setRawGyroscope(value1, value2, value3);
 								break;
 							case 1:		
 								listener.setRawAccelerometer(value1, value2, value3);
 								break;
 							case 2:		
 								listener.setRawMagnetometer(value1, value2, value3);
 								break;
 							default:
 								break;
 							}
 						}else{
							/*
 							for (int i=0;i< 6;i++){
 								System.out.write(data.get());
 							}
 							System.out.println();
							*/
 						}
 
 						if(lastSeq!=-1){
 							if(currentSeq-lastSeq!=1 && !(lastSeq == 65535 && currentSeq == 0)){
 								throw new RuntimeException("Incorrect sequence number. Last: "+lastSeq+" Current: "+currentSeq);
 							}
 						}
 						packetNumber[packetType]++;
 						lastSeq = currentSeq;
 					}
 
 					/*
 					 * printEpReg(data); printEpReg(data); printEpReg(data);
 					 */
 
 					//data.toString();
 					transferred.clear();
 					data.clear();
 				}
 
 				LibUsb.close(STMhandle);
 
 				time2 = System.nanoTime() - time;
 				double timeS = time2 / 1000000000d;
 				System.out.println(time2);
 				System.out.println(8000 / timeS);
 				System.out.println(data);
 			} else {
 				System.out.println("STM non trovata");
 			}
 
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public int get(int i){
 		return valore[i].get();
 	}
 
 	public void setListener(USBLIstener usbReader) {
 		this.listener = usbReader;
 	}
 
 }
