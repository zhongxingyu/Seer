 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.detector.hardwaretriggerable;
 
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.scannable.PositionCallableProvider;
 import gda.device.scannable.PositionInputStream;
 import gda.device.scannable.PositionStreamIndexer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.NoSuchElementException;
import java.util.Vector;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.LinkedBlockingQueue;
 
 public class DummyHardwareTriggerableSimpleDetector extends DummyHardwareTriggerableDetectorBase implements
 		PositionCallableProvider<Double>, PositionInputStream<Double> {
 
 	class ImmediatelyAvailable implements Callable<Double> {
 
 		private final Double value;
 
 		public ImmediatelyAvailable(Double value) {
 			this.value = value;
 		}
 
 		@Override
 		public Double call() throws Exception {
 			return value;
 		}
 	}
 
 	private double lastCollectedXValue = 0;
 
 	private double lastCollectedValue = 1;
 
 	private volatile BlockingQueue<Double> readDataBuffer = new LinkedBlockingQueue<Double>();
 
 	private PositionStreamIndexer<Double> streamIndexer;
 
 	private volatile int totalRead;
 
 	private Thread singleReadoutThread;
 
 	public DummyHardwareTriggerableSimpleDetector(String name) {
 		setName(name);
 		setInputNames(new String[] { name });
 		setExtraNames(new String[] {});
 		setOutputFormat(new String[] { "%s" });
 	}
 
 	// Detector
 	@Override
 	public int[] getDataDimensions() throws DeviceException {
 		return new int[] { 1 };
 	}
 
 	@Override
 	public void collectData() throws DeviceException {
 		if (isHardwareTriggering()) {
 			super.collectData();
 		} else {
 			collectSingleReadoutValue();
 		}
 	}
 
 	@Override
 	public Object readout() throws DeviceException {
 		try {
 			return getPositionCallable().call();
 		} catch (Exception e) {
 			throw new DeviceException("Exception in " + getName() + " readout: ", e);
 		}
 	}
 
 	@Override
 	public Callable<Double> getPositionCallable() throws DeviceException {
 		if (isHardwareTriggering()) {
 			return streamIndexer.getPositionCallable();
 		}
 		return new ImmediatelyAvailable(lastCollectedValue);
 	}
 
 	@Override
 	public void atScanLineStart() throws DeviceException {
 		streamIndexer = new PositionStreamIndexer<Double>(this);
 	}
 
 	@Override
 	public boolean createsOwnFiles() throws DeviceException {
 		return false;
 	}
 
 	@Override
 	public String getDescription() throws DeviceException {
 		return "";
 	}
 
 	@Override
 	public String getDetectorID() throws DeviceException {
 		return "";
 	}
 
 	@Override
 	public String getDetectorType() throws DeviceException {
 		return "";
 	}
 
 	//
 	private void collectSingleReadoutValue() throws DeviceException {
 		setStatus(Detector.BUSY);
 		singleReadoutThread = new Thread(new SingleReadoutTask(getCollectionTime(), getName()));
 		singleReadoutThread.start();
 	}
 
 	class SingleReadoutTask implements Runnable {
 		private final double deltaT;
 		private final String name;
 
 		public SingleReadoutTask(final double deltaT, final String name) {
 			this.deltaT = deltaT;
 			this.name = name;
 		}
 
 		@Override
 		public void run() {
 			try {
 				Thread.sleep((long) (deltaT * 1000));
 			} catch (InterruptedException e) {
 				terminal.print(name + " interupted while collecting single point");
 			} finally {
 				lastCollectedXValue += deltaT;
 				lastCollectedValue = calcNewCount(lastCollectedXValue);
 				readDataBuffer.add(lastCollectedValue);
 				setStatus(Detector.IDLE);
 			}
 		}
 	}
 
 	@Override
 	public void waitWhileBusy() throws DeviceException, InterruptedException {
 		if (singleReadoutThread != null) {
 			singleReadoutThread.join();
 		}
 	}
 
 	private double calcNewCount(double x) {
 		return Math.cos(x);
 	}
 
 	@Override
 	public List<Double> read(int maxToRead) throws NoSuchElementException, InterruptedException {
 		List<Double> read = new ArrayList<Double>();
 		if (totalRead >= getHardwareTriggerProvider().getNumberTriggers()) {
 			throw new NoSuchElementException("All " + totalRead + " elements have already been read out of "
 					+ getHardwareTriggerProvider().getNumberTriggers() + " available");
 		}
 		read.add(readDataBuffer.take()); // wait until one is available
 		readDataBuffer.drainTo(read, maxToRead-1); // drain remaining
 		totalRead += read.size();
 		return read;
 	}
 
 	@Override
 	void simulatedTriggerRecieved() throws DeviceException {
 		lastCollectedXValue += getCollectionTime();
 		lastCollectedValue = calcNewCount(lastCollectedXValue);
 		terminal.print(getName() + " received trigger @ " + lastCollectedXValue + "s --> " + lastCollectedValue);
 		readDataBuffer.add(lastCollectedValue);
 	}
 
 }
