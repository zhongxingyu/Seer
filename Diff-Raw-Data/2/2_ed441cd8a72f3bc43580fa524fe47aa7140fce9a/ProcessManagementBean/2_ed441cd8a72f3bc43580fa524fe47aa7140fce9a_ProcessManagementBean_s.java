 package uk.co.bssd.jmx;
 
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 
 import javax.management.MBeanServerConnection;
 
 import com.sun.management.OperatingSystemMXBean;
 
 @SuppressWarnings("restriction")
 public class ProcessManagementBean {
 
 	private final OperatingSystemMXBean operatingSystemBean;
 	private final int availableProcessors;
 
 	private long lastSystemTime;
 	private long lastProcessCpuTime;
 
 	public ProcessManagementBean(MBeanServerConnection connection) {
 		this.operatingSystemBean = operatingSystemBean(connection);
 		this.availableProcessors = this.operatingSystemBean
 				.getAvailableProcessors();
 
 		this.lastSystemTime = System.nanoTime();
 		this.lastProcessCpuTime = this.operatingSystemBean.getProcessCpuTime();
 	}
 
 	public double cpuUsage() {
 		long systemTime = System.nanoTime();
 		long processCpuTime = this.operatingSystemBean.getProcessCpuTime();
 
		double cpuUsage = (processCpuTime - this.lastProcessCpuTime)
 				/ (systemTime - this.lastSystemTime);
 
 		lastSystemTime = systemTime;
 		lastProcessCpuTime = processCpuTime;
 
 		return cpuUsage / this.availableProcessors;
 	}
 
 	private OperatingSystemMXBean operatingSystemBean(
 			MBeanServerConnection connection) {
 		return platformBean(connection,
 				ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME,
 				OperatingSystemMXBean.class);
 	}
 
 	private <T> T platformBean(MBeanServerConnection connection,
 			String beanName, Class<T> clazz) {
 		try {
 			return ManagementFactory.newPlatformMXBeanProxy(connection,
 					beanName, clazz);
 		} catch (IOException e) {
 			throw new RuntimeException("Unable to get OperatingSystemMXBean", e);
 		}
 	}
 }
