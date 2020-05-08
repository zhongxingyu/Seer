 package org.imirsel.nema.flowservice;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.jcip.annotations.ThreadSafe;
 
 import org.imirsel.nema.contentrepository.client.ArtifactService;
 import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
 import org.imirsel.nema.flowservice.monitor.JobStatusMonitor;
 import org.imirsel.nema.repository.RepositoryClientConnectionPool;
 
 /**
  * Handles the instantiation of new {@link MeandreServerProxy} instances.
  * 
  * @author shirk
  * @since 0.6.0
  * 
  */
 @ThreadSafe
 public class MeandreServerProxyFactory {
 
 	private JobStatusMonitor jobStatusMonitor;
 
 	private ArtifactService artifactService;
 	private RepositoryClientConnectionPool repositoryClientConnectionPool;
 	
 
 	private Map<String, MeandreServerProxy> proxyInstances = new HashMap<String, MeandreServerProxy>(
 			8);
 
 	/**
 	 * Given a {@link MeandreServerProxyConfig}, create a new
 	 * {@link MeandreServerProxy} instance.
 	 * 
 	 * @param config
 	 *            The configuration upon which the new
 	 *            {@link MeandreServerProxy} instance should be based.
 	 * @return A {@link MeandreServerProxy} instance.
 	 */
 	public synchronized MeandreServerProxy getServerProxyInstance(
 			MeandreServerProxyConfig config) {
 		String key = keyFor(config);
 		MeandreServerProxy instance = null;
 		if (proxyInstances.containsKey(key)) {
 			instance = proxyInstances.get(key);
 		} else {
 			instance = new RemoteMeandreServerProxy();
 			instance.setConfig(config);
 			instance.setJobStatusMonitor(jobStatusMonitor);
 			instance
 					.setRepositoryClientConnectionPool(repositoryClientConnectionPool);
 			instance.init();
 			instance.setArtifactService(artifactService);
 			proxyInstances.put(key, instance);
 		}
 		return instance;
 	}
 
 	/**
 	 * Create a key to be used in the instance map for the given
 	 * {@link MeandreServerProxyConfig}.
 	 * 
 	 * @param config
 	 *            The {@link MeandreServerProxyConfig} for which the key will be
 	 *            generated.
 	 * @return The key string.
 	 */
 	private String keyFor(MeandreServerProxyConfig config) {
 		return config.getHost() + ":" + config.getPort();
 	}
 
 	/**
 	 * Set the {@link JobStatusMonitor} that new {@link MeandreServerProxy}
 	 * instances will be given.
 	 * 
 	 * @param jobStatusMonitor
 	 *            Reference to the {@link JobStatusMonitor} the
 	 *            {@link MeandreServerProxy} instances will use to monitor the
 	 *            jobs they are running.
 	 */
 	public void setJobStatusMonitor(JobStatusMonitor jobStatusMonitor) {
 		this.jobStatusMonitor = jobStatusMonitor;
 	}
 
 	/**
 	 * Return the {@link JobStatusMonitor} that new {@link MeandreServerProxy}
 	 * instances are being created with references to.
 	 * 
 	 * @return Current {@link JobStatusMonitor}
 	 */
 	public JobStatusMonitor getJobStatusMonitor() {
 		return jobStatusMonitor;
 	}
 
 	/**
 	 * Get the {@link RepositoryClientConnectionPool} that new
 	 * {@link MeandreServerProxy} instances are being given.
 	 * 
 	 * @return {@link RepositoryClientConnectionPool} that are being set upon
 	 *         new {@link MeandreServerProxy} instances.
 	 */
 	public RepositoryClientConnectionPool getRepositoryClientConnectionPool() {
 		return repositoryClientConnectionPool;
 	}
 
 	/**
 	 * Set the {@link RepositoryClientConnectionPool} that new
 	 * {@link MeandreServerProxy} instances should be given.
 	 * 
	 * @return {@link RepositoryClientConnectionPool} that should be set upon
 	 *         new {@link MeandreServerProxy} instances.
 	 */
 	public void setRepositoryClientConnectionPool(
 			RepositoryClientConnectionPool repositoryClientConnectionPool) {
 		this.repositoryClientConnectionPool = repositoryClientConnectionPool;
 	}
 
 }
