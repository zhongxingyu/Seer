 package ru.spbstu.telematics.lab_3.radio;
 
 import java.util.Random;
 
 /**
  * Данный класс реализует логику работы радио.
  * Имена кнопок и состояния радио в объединении образуют заданный
  * в задаче алфавит.
  */
 public class Radio implements IRadio, Runnable {
 	
 	/**
 	 * Имена кнопок на радио.
 	 */
 	public static String[] BUTTON_TYPES = { 
 		"ON",
 		"OFF",
 		"SCAN",
 		"RESET",
 	};
 	
 	/**
 	 * Состояния радио.
 	 */
 	enum State {
 		ON,
 		OFF,
 		SCAN,
 		LOCK,
 		END
 	};
 
 	public static int HIGH_FREQUENCY = 108000;
 	public static int LOW_FREQUENCY = 88000;
 	public static int FREQUENCY_STEP = 500;
 	
 	private Thread _thread;
 	private RadioInterface _interface;
 	private int _frequency;
 	private Object _mutex;
	private State _state;
 
 	public Radio()
 	{
 		_interface = new RadioInterface(this);
 		_thread = new Thread(this);
 		_frequency = 0;
 		_state = State.OFF;
 	}
 
 	/**
 	 * Данный метод запускает радио.
 	 */
 	public void start() {
 		_interface.init();
 		_thread.start();
 		_interface.switchOn();
 		_interface.run();
 	}
 	
 	/**
 	 * Данный метод реализует метод интерфейса {@link Runnable#run()},
 	 * который должен содержать код выполняемы потоком. Здесь производится
 	 * имитация работы радио в соответствии с условием задачи.
 	 * При вызове методов, соответствующих символам алфавита, происходит
 	 * переход радио из состояния в состояние {@link State}.
 	 */
 	@Override
 	public void run() {
 		_mutex = new Object();
 		double frequency = 0;
 		int currentFrequency = 0;
 		boolean isFound, isFirstCycle;
 		Random rand = new Random();
 		while(!_thread.isInterrupted()) {
 			isFirstCycle = true;
 			while(_state != State.OFF) {
 				if(isFirstCycle)
 				{
 					_interface.message("Radio turned on!");
 					isFirstCycle = false;
 				}
 				while(_state == State.SCAN)
 				{
 					isFound = false;
 					currentFrequency = _frequency;
 					while(_frequency > LOW_FREQUENCY && !isFound) {
 						isFound = (rand.nextBoolean()) ? rand.nextBoolean() : false;
 						frequency = (Math.floor(_frequency / 100) / 10);
 						_interface.message("Look at FM: " + frequency);
 						_frequency -= FREQUENCY_STEP;
 						try {
 							_thread.sleep(100);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 					if(!isFound) {
 						_interface.message("Cannot find station");
 						end();
 					}
 					else {
 						_interface.message("You are listening radio at " + frequency + " FM");
 						currentFrequency = _frequency;
 						lock();
 					}
 				}
 			}
 			_interface.message("Radio turned off!");
 			synchronized (_mutex) {
 				try {
 					_mutex.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Метод, осуществляющий переход радио в состояние ON.
 	 */
 	@Override
 	public void on() {
 		if(_state == State.OFF)
 		{
			_state = State.ON;
			_frequency = HIGH_FREQUENCY;
 			synchronized (_mutex) {
 				_mutex.notify();
 			}
 		}
 		else
 			_interface.message("Incorrect command");
 	}
 
 	/**
 	 * Метод, осуществляющий переход радио в состояние OFF.
 	 */
 	@Override
 	public void off() {
 		if(_state == State.LOCK ||
 		   _state == State.END) {
 			synchronized (_mutex) {
 				_mutex.notify();
 			}
 		}
 		if(_state != State.OFF)
 			_state = State.OFF;
 		else
 			_interface.message("Incorrect command");
 	}
 
 	/**
 	 * Метод, осуществляющий переход радио в состояние SCAN.
 	 */
 	@Override
 	public void scan() {
 		if(_state == State.ON ||
 		   _state == State.LOCK ||
 		   _state == State.END) {
 			_state = State.SCAN;
 			synchronized (_mutex) {
 				_mutex.notify();
 			}
 		}
 		else
 			_interface.message("Incorrect command");
 	}
 
 	/**
 	 * Метод, осуществляющий переход радио в состояние ON, при
 	 * этом значение текущей частоты, с которой будет происходить
 	 * поиск "рабочей" частоты устанавливается равным значению 
 	 * константы HIGH_FREQUENCY.
 	 */
 	@Override
 	public void reset() {
 		if(_state == State.SCAN || 
 		   _state == State.LOCK ||
 		   _state == State.END) {
 			_state = State.ON;
			_frequency = HIGH_FREQUENCY;
			_interface.message("Reset frequency pointer");
 			synchronized (_mutex) {
 				_mutex.notify();
 			}
 		}
 		else
 			_interface.message("Incorrect command");
 	}
 
 	/**
 	 * Метод, осуществляющий переход радио в состояние LOCK.
 	 */
 	private void lock() {
 		if(_state == State.SCAN)
 		{
			_state = State.LOCK;
 			synchronized (_mutex) {
 				try {
 					_mutex.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		else
 			_interface.message("Incorrect command");
 	}
 
 	/**
 	 * Метод, осуществляющий переход радио в состояние END.
 	 */
 	private void end() {
 		if(_state == State.SCAN) {
			_state = State.END;
 			synchronized (_mutex) {
 				try {
 					_mutex.wait();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		else
 			_interface.message("Incorrect command");
 	}
 
 }
