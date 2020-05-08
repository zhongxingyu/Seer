 /*
  * Copyright 2010 Softgress - http://www.softgress.com/
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sim.data;
 
 
 /**
  * Holds information about system.
  * 
  * @author vroman
  * 
  */
 public interface SystemMetrics extends Metrics {
 
 	public SystemId getSystemId();
 
 	/**
 	 * @return returns system load percentage
 	 */
 	public double getSystemLoadAverage();
 
 	/**
 	 * @return returns total system free memory kB
 	 */
 	public long getTotalSystemFreeMemory();
 
 	/**
 	 * @return returns total system used memory kB
 	 */
 	public long getTotalSystemUsedMemory();
 
 	/**
 	 * @return returns total system used swap kB
 	 */
 	public long getTotalSystemUsedSwap();
 
 	/**
 	 * @return returns system open file descriptors count
 	 */
 	public long getSystemOpenFileDescriptors();
 
 	/**
 	 * @return returns size of data read from swap in kB
 	 */
 	public long getSwapIn();
 
 	/**
 	 * @return returns size of data write to swap in kB
 	 */
 	public long getSwapOut();
 
 	/**
 	 * @return returns system I/O reads kB
 	 */
 	public long getIORead();
 
 	/**
 	 * @return returns system I/O writes kB
 	 */
 	public long getIOWrite();
 
 	/**
 	 * @return returns user cpu percentage
 	 */
 	public double getUserPerc();
 
 	/**
 	 * @return returns system cpu percentage
 	 */
 	public double getSysPerc();
 
 	/**
 	 * @return returns idle cpu percentage
 	 */
 	public double getIdlePerc();
 
 	/**
 	 * @return returns wait cpu percentage
 	 */
 	public double getWaitPerc();
 
 	/**
 	 * @return returns irq cpu percentage
 	 */
 	public double getIrqPerc();
 
 	/**
 	 * @return returns user cpu time
 	 */
 	public double getUser();
 
 	/**
 	 * @return returns system cpu time
 	 */
 	public double getSys();
 
 	/**
 	 * @return returns idle cpu time
 	 */
 	public double getIdle();
 
 	/**
 	 * @return returns wait cpu time
 	 */
 	public double getWait();
 
 	/**
 	 * @return returns irq cpu time
 	 */
 	public double getIrq();
 
 	/**
 	 * @return total number of processes
 	 */
 	public long getProcessesCount();
 
 	/**
 	 * @return total number of processes in run state
 	 */
 	public long getRunningProcessesCount();
 
 	/**
 	 * @return total number of threads
 	 */
 	public long getThreadsCount();
 
 	/**
	 * @return the number of current outbound tcp connections
 	 */
 	public long getTcpOutbound();
 
 	/**
	 * @return the number of current inbound tcp connections
 	 */
 	public long getTcpInbound();
 
 	/**
 	 * @return the total number of bytes sent over all installed network
	 *         interfaces, except loopback, since last measurement
 	 */
 	public long getNetworkSent();
 
 	/**
 	 * @return the total number of bytes received over all installed network
	 *         interfaces, except loopback, since last measurement
 	 */
 	public long getNetworkReceived();
 
 	/**
 	 * @return the total number of bytes sent over loopback interface since last
 	 *         measurement
 	 */
 	public long getLoopbackNetworkSent();
 
 	/**
 	 * @return the total number of bytes received over loopback interface since
 	 *         last measurement
 	 */
 	public long getLoopbackNetworkReceived();
 }
