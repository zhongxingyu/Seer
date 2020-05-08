 /**
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  * 
  * This file is part of Universal Task Executer.
  * 
  * Universal Task Executer is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Universal Task Executer is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executer. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute.console;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
import net.lmxm.ute.beans.configuration.ApplicationPreferences;
 import net.lmxm.ute.beans.configuration.Configuration;
 import net.lmxm.ute.beans.jobs.Job;
 import net.lmxm.ute.beans.jobs.SingleTaskJob;
 import net.lmxm.ute.beans.tasks.Task;
 import net.lmxm.ute.configuration.ConfigurationReader;
 import net.lmxm.ute.configuration.ConfigurationUtils;
 import net.lmxm.ute.executers.jobs.JobExecuter;
 import net.lmxm.ute.executers.jobs.JobExecuterFactory;
 import net.lmxm.ute.resources.ApplicationResourceType;
 import net.lmxm.ute.resources.ResourcesUtils;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.beust.jcommander.JCommander;
 
 /**
  * The Class ConsoleApplication.
  */
 public final class ConsoleApplication {
 
 	/** The Constant LOGGER. */
 	private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleApplication.class);
 
 	/** The args. */
 	private final String[] args;
 
 	/**
 	 * Instantiates a new console application.
 	 * 
 	 * @param args the args
 	 */
 	public ConsoleApplication(final String[] args) {
 		super();
 
 		this.args = args;
 	}
 
 	/**
 	 * Execute.
 	 */
 	public void execute() {
 		final String prefix = "execute() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final ConsoleArguments consoleArguments = getConsoleArguments();
 		final File inputFile = consoleArguments.getInputFile();
 		final Configuration configuration = new ConfigurationReader(inputFile).read();
 
		loadAndValidatePreferencesAreSet(configuration);

 		final List<Job> jobs = loadJobs(configuration, consoleArguments);
 		executeJobs(jobs, configuration);
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Execute jobs.
 	 * 
 	 * @param jobs the jobs
 	 * @param configuration the configuration
 	 */
 	private void executeJobs(final List<Job> jobs, final Configuration configuration) {
 		final String prefix = "executeJobs() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		// Execute jobs
 		for (final Job job : jobs) {
 			LOGGER.debug("{} executing job {}", prefix, job.getId());
 
 			final Job jobInterpolated = ConfigurationUtils.interpolateJobValues(job, configuration);
 			job.removeEmptyObjects();
 
 			final JobExecuter jobExecuter = JobExecuterFactory.create(jobInterpolated, configuration);
 			jobExecuter.addJobStatusListener(new ConsoleJobStatusListener());
 			jobExecuter.addStatusChangeListener(new ConsoleStatusChangeListener());
 			jobExecuter.execute();
 		}
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Gets the console arguments.
 	 * 
 	 * @return the console arguments
 	 */
 	private ConsoleArguments getConsoleArguments() {
 		final String prefix = "getConsoleArguments() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final ConsoleArguments consoleArguments = new ConsoleArguments();
 		final JCommander jCommander = new JCommander(consoleArguments);
 
 		jCommander.setProgramName(ResourcesUtils.getResourceText(ApplicationResourceType.NAME));
 
 		try {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("{} parsing args={}", prefix, Arrays.toString(args));
 			}
 
 			jCommander.parse(args);
 		}
 		catch (final RuntimeException e) {
 			LOGGER.error(prefix + " error occurred parsing arguments");
 
 			System.out.println(e.getMessage() + "\n");
 			jCommander.usage();
 			System.exit(-1);
 		}
 
 		LOGGER.debug("{} returning", prefix);
 
 		return consoleArguments;
 	}
 
 	/**
	 * Load and validate preferences are set.
	 * 
	 * @param configuration the configuration
	 */
	private void loadAndValidatePreferencesAreSet(final Configuration configuration) {
		final String prefix = "loadAndValidatePreferencesAreSet() :";

		LOGGER.debug("{} entered", prefix);

		final ApplicationPreferences applicationPreferences = new ApplicationPreferences(new File(
				configuration.getAbsolutePath()));
		applicationPreferences.loadPreferenceValues(configuration.getPreferences());

		if (applicationPreferences.hasAllPreferences(configuration.getPreferences())) {
			LOGGER.debug("{} all preferences have values", prefix);
		}
		else {
			LOGGER.error("{} at least one preference does not have a value", prefix);
			throw new RuntimeException("Preferences must be assigned values before continuing");
		}

		LOGGER.debug("{} leaving", prefix);
	}

	/**
 	 * Load jobs.
 	 * 
 	 * @param configuration the configuration
 	 * @param consoleArguments the console arguments
 	 * @return the list
 	 */
 	private List<Job> loadJobs(final Configuration configuration, final ConsoleArguments consoleArguments) {
 		final String prefix = "loadJobs() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<Job> jobs = new ArrayList<Job>();
 
 		final String jobId = consoleArguments.getJobId();
 		final String taskId = consoleArguments.getTaskId();
 
 		final Job job = ConfigurationUtils.getJob(configuration, jobId);
 		if (job == null) {
 			LOGGER.error("{} job with id \"{}\" does not exist", prefix, jobId);
 			throw new RuntimeException("Job with id \"" + jobId + "\" does not exist");
 		}
 
 		if (consoleArguments.getTaskId() == null) {
 			jobs.add(job);
 		}
 		else {
 			SingleTaskJob singleTaskJob = null;
 
 			for (final Task task : job.getTasks()) {
 				if (task.getId().equals(taskId)) {
 					singleTaskJob = new SingleTaskJob(task);
 					break;
 				}
 			}
 
 			if (singleTaskJob == null) {
 				LOGGER.error("{} task with id \"{}\" does not exist", prefix, taskId);
 				throw new RuntimeException("Task with id \"" + taskId + "\" does not exist");
 			}
 
 			jobs.add(singleTaskJob);
 		}
 
 		LOGGER.debug("{} returning", prefix);
 
 		return jobs;
 	}
 }
