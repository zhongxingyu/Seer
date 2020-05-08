 package at.fhv.audioracer.serial;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import jssc.SerialPort;
 import jssc.SerialPortEvent;
 import jssc.SerialPortEventListener;
 import jssc.SerialPortException;
 import jssc.SerialPortList;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import at.fhv.audioracer.serial.CarClient.Velocity;
 import at.fhv.audioracer.server.CarClientManager;
 
 public class SerialInterface implements SerialPortEventListener, ICarClientListener {
 	private static class Command {
 		byte command;
 		CarClient car;
 	}
 	
 	private static final byte CAR_CONNECTED = 0x1;
 	private static final byte CAR_DISCONNECTED = 0x2;
 	private static final byte CAR_UPDATE_VELOCITY = (byte) 0xFF;
 	private static final byte CAR_TRIM = (byte) 0xFE;
 	private static final byte START = (byte) 0xFD;
 	
 	private static Logger _logger = LoggerFactory.getLogger(SerialInterface.class);
 	
 	private final Lock _lock;
 	
 	private final SerialPort _serialPort;
 	
 	private boolean _running;
 	
 	// used to determine if a log message is sent
 	private boolean _logging = false;
 	private boolean _carriageReturnReceived = false;
 	
 	private Map<Byte, CarClient> _carClients;
 	private BlockingQueue<Command> _writingQueue;
 	
 	public SerialInterface(String port) throws SerialPortException {
 		_lock = new ReentrantLock();
 		
 		_serialPort = new SerialPort(port);
 		_serialPort.openPort();
 		
 		_serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);
 		_serialPort.setRTS(true);
 		_serialPort.addEventListener(this, (SerialPort.MASK_CTS | SerialPort.MASK_RXCHAR));
 		_serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8,
 				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, true, false);
 		
 		_carClients = new HashMap<Byte, CarClient>();
 		
 		_writingQueue = new LinkedBlockingDeque<Command>();
 		Command startCommand = new Command();
 		startCommand.command = START;
 		_writingQueue.add(startCommand);
 		startWriting();
 	}
 	
 	public static String[] getPortNames() {
 		return SerialPortList.getPortNames();
 	}
 	
 	@Override
 	public void serialEvent(SerialPortEvent serialPortEvent) {
 		try {
 			if (serialPortEvent.isRXCHAR()) {
 				byte[] buff = _serialPort.readBytes(1);
 				if (_logging) {
 					if (buff[0] == '\n') {
 						_carriageReturnReceived = false;
 						_logging = false;
 						System.out.println();
 					} else {
 						if (_carriageReturnReceived) {
 							_carriageReturnReceived = false;
 							System.out.print('\r');
 						}
 						if (buff[0] == '\r') {
 							_carriageReturnReceived = true;
 						} else {
 							System.out.print((char) buff[0]);
 						}
 					}
 				} else {
 					switch (buff[0]) {
 						case 'D':
 						case 'W':
 						case 'E':
 							// Logging
 							System.out.print((char) buff[0]);
 							_logging = true;
 							break;
 						case CAR_CONNECTED:
 							carConnected();
 							break;
 						case CAR_DISCONNECTED:
 							carDisconnected();
 						case START:
 							interfaceStarted();
 							break;
 					}
 				}
 			} else if (serialPortEvent.isCTS()) {
 				startWriting();
 			}
 		} catch (SerialPortException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void carConnected() throws SerialPortException {
 		byte id = _serialPort.readBytes(1)[0];
 		carConnected(id);
 	}
 	
 	private void carConnected(byte id) {
 		CarClient carClient = _carClients.get(id);
 		if (carClient == null) {
 			carClient = new CarClient(id);
 			_carClients.put(id, carClient);
 			carClient.getListenerList().add(this);
 		}
 		
 		CarClientManager.getInstance().connect(carClient);
 	}
 	
 	private void carDisconnected() throws SerialPortException {
 		byte id = _serialPort.readBytes(1)[0];
 		carDisconnected(id);
 	}
 	
 	private void carDisconnected(byte id) {
 		CarClient carClient = _carClients.get(id);
 		if (carClient != null) {
 			CarClientManager.getInstance().disconnect(carClient);
 		}
 	}
 	
 	private void interfaceStarted() throws SerialPortException {
 		_logger.debug("started.");
 		byte connectedCars = _serialPort.readBytes(1)[0];
 		
 		for (byte i = 0; i < 8; i++) {
 			if ((connectedCars & (1 << i)) != 0) {
 				carConnected(i);
 			} else {
 				carDisconnected(i);
 			}
 		}
 	}
 	
 	@Override
 	public void onVelocityChanged(CarClient carClient) {
 		Command command = new Command();
 		command.command = CAR_UPDATE_VELOCITY;
 		command.car = carClient;
 		
 		_writingQueue.add(command);
 		startWriting();
 	}
 	
 	@Override
 	public void onTrim(CarClient carClient) {
 		Command command = new Command();
 		command.command = CAR_TRIM;
 		command.car = carClient;
 		
 		_writingQueue.add(command);
 		startWriting();
 	}
 	
 	/**
 	 * Checks if the writing thread is already running. If the thread is not running, it will be started.
 	 */
 	private void startWriting() {
 		_lock.lock();
 		try {
 			if (!_running && _serialPort.isCTS()) {
 				_running = true;
 				Thread thread = new Thread(new Runnable() {
 					@Override
 					public void run() {
 						SerialInterface.this.run();
 					}
 				}, "SerialInterface");
 				thread.setDaemon(true);
 				thread.start();
 			}
 		} catch (SerialPortException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			_lock.unlock();
 		}
 	}
 	
 	private void run() {
 		// Ensure, that _running has always the correct value!
 		// This means, before the thread could end, the lock must be acquired.
 		// Also _running must be set before the thread starts.
 		_logger.debug("Start writing");
 		_lock.lock();
 		boolean locked = true;
 		try {
 			while (_serialPort.isCTS()) {
 				// only send when hardware is ready
 				
 				_lock.unlock();
 				locked = false;
 				
 				byte[] buff = null;
 				Command command = _writingQueue.take();
 				if (command.command == CAR_UPDATE_VELOCITY) {
 					Velocity velocity = command.car.getVelocity();
 					if (velocity != null) {
 						buff = new byte[] { command.command, command.car.getCarClientId(),
								velocity.speed, velocity.direction };
 					}
 				} else {
 					buff = new byte[] { command.command,
 							(command.car != null ? command.car.getCarClientId() : 0), 0, 0 }; // always padding to 4 byte
 				}
 				
 				if (buff != null) {
 					_serialPort.writeBytes(buff);
 				}
 				
 				_lock.lock();
 				locked = true;
 			}
 			_logger.debug("Stop writing");
 		} catch (SerialPortException | InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			if (!locked) {
 				_lock.lock();
 			}
 			
 			_running = false;
 			_lock.unlock();
 		}
 	}
 }
