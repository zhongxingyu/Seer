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
 
 package gda.device.scannable;
 
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.Scannable;
 
 import java.util.NoSuchElementException;
 import java.util.Vector;
 
 /**
 * Provides a very simple input stream for reading positions/data from {@link Scannable}s / {@link Detector}s.
  *
  * @param <T>
  */
 public interface PositionInputStream<T> {
 
 	/**
 	 * Return at least one element, but no more than maxToRead, waiting until able to do so. The parameter maxToRead is
	 * there to protect the caller from potentially receiving too much data in call, not to meet the needs of the
 	 * detector.
 	 * 
 	 * @param maxToRead
 	 *            the maximum number of elements to read. Must be one or more.
 	 * @return at least one element from the head of the stream, oldest on the left
 	 * @throws NoSuchElementException
 	 *             if the stream will have nothing to return
 	 * @throws InterruptedException
 	 */
 	public Vector<T> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException;
 
 }
