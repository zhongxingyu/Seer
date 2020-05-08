 package com.buschmais.tinkerforge4jenkins.core.notifier.common;
 
 import java.lang.reflect.ParameterizedType;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import com.buschmais.tinkerforge4jenkins.core.BuildState;
 import com.buschmais.tinkerforge4jenkins.core.JobState;
 import com.buschmais.tinkerforge4jenkins.core.NotifierDevice;
 import com.buschmais.tinkerforge4jenkins.core.schema.configuration.v1.BrickletConfigurationType;
 import com.buschmais.tinkerforge4jenkins.core.schema.configuration.v1.JobsType;
 import com.tinkerforge.Device;
 
 /**
  * Abstract base implementation for {@link NotifierDevice}s.
  * 
  * @author dirk.mahler
  * 
  * @param <T>
  *            The type of the underlying TinkerForge device.
  * @param <C>
  *            The configuration type.
  */
 public abstract class AbstractNotifierDevice<T extends Device, C extends BrickletConfigurationType>
 		implements NotifierDevice<T, C> {
 
 	/**
 	 * The TinkerForge device.
 	 */
 	private T device;
 
 	/**
 	 * The configuration.
 	 */
 	private C configuration;
 
 	/**
 	 * A map holding the {@link JobState}s identified by their names.
 	 */
 	private SortedMap<String, JobState> jobStates = new TreeMap<String, JobState>();
 
 	/**
 	 * A map holding sets {@link JobState}s according to their
 	 * {@link BuildState}s.
 	 */
 	private Map<BuildState, Set<JobState>> jobStatesByBuildState = new HashMap<BuildState, Set<JobState>>();
 
 	/**
 	 * Constructs the {@link AbstractNotifierDevice}.
 	 * 
 	 * @param device
 	 *            The TinkerForge device.
 	 */
 	protected AbstractNotifierDevice(T device) {
 		this.device = device;
 		for (BuildState buildState : BuildState.values()) {
 			jobStatesByBuildState.put(buildState, new HashSet<JobState>());
 		}
 	}
 
 	@Override
 	public T getDevice() {
 		return device;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void setConfiguration(BrickletConfigurationType configuration) {
 		ParameterizedType type = (ParameterizedType) this.getClass()
 				.getGenericSuperclass();
 		Class<C> configurationType = (Class<C>) type.getActualTypeArguments()[1];
 		this.configuration = configurationType.cast(configuration);
 	}
 
 	@Override
 	public void update(JobState state) {
 		JobState previousState = jobStates.put(state.getName(), state);
 		if (previousState != null) {
 			jobStatesByBuildState.get(previousState.getBuildState()).remove(
 					previousState);
 		}
 		jobStatesByBuildState.get(state.getBuildState()).add(state);
 	}
 
 	/**
 	 * Return the configuration.
 	 * 
 	 * @return The configuration.
 	 */
 	protected C getConfiguration() {
 		return configuration;
 	}
 
 	/**
 	 * Return the {@link JobState}s.
 	 * 
 	 * @return the {@link JobState}s.
 	 */
 	public SortedMap<String, JobState> getJobStates() {
 		return jobStates;
 	}
 
 	/**
 	 * Return the {@link JobState}s matching the given {@link BuildState}.
 	 * 
 	 * @param buildState
 	 *            The {@link BuildState}.
 	 * 
 	 * @return the {@link JobState}s.
 	 */
 	public Set<JobState> getJobsByBuildState(BuildState buildState) {
 		return jobStatesByBuildState.get(buildState);
 	}
 
 	/**
 	 * Filters a collection of jobs using a filter.
 	 * 
 	 * @param jobs
 	 *            The collection of {@link JobState}s.
 	 * @param filter
 	 *            The filter, represented by a {@link JobsType}. This parameter
 	 *            is optional (may be <code>null</code>).
 	 * @return The set of filtered {@link JobState}s.
 	 */
 	public Set<JobState> filter(Collection<JobState> jobs, JobsType filter) {
		Set<String> filterSet = new HashSet<String>();
 		if (filter != null) {
 			filterSet.addAll(filter.getJob());
		}
		Set<JobState> filteredJobs = new HashSet<JobState>();
		for (JobState job : jobs) {
			if (filterSet.contains(job.getName())) {
				filteredJobs.add(job);
 			}
 		}
		return filteredJobs;
 	}
 }
