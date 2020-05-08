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
 package net.lmxm.ute.configuration;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.lmxm.ute.beans.*;
 import net.lmxm.ute.beans.configuration.Configuration;
 import net.lmxm.ute.beans.jobs.Job;
 import net.lmxm.ute.beans.jobs.SequentialJob;
 import net.lmxm.ute.beans.locations.FileSystemLocation;
 import net.lmxm.ute.beans.locations.HttpLocation;
 import net.lmxm.ute.beans.locations.MavenRepositoryLocation;
 import net.lmxm.ute.beans.locations.SubversionRepositoryLocation;
 import net.lmxm.ute.beans.sources.HttpSource;
 import net.lmxm.ute.beans.sources.MavenRepositorySource;
 import net.lmxm.ute.beans.sources.SubversionRepositorySource;
 import net.lmxm.ute.beans.targets.FileSystemTarget;
 import net.lmxm.ute.beans.tasks.*;
 import net.lmxm.ute.enums.SubversionRevision;
 import net.lmxm.ute.exceptions.ConfigurationException;
 import net.lmxm.ute.resources.types.ExceptionResourceType;
 import net.lmxm.ute.subversion.utils.SubversionUtils;
 import noNamespace.*;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.xmlbeans.XmlException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class ConfigurationReader.
  */
 public final class ConfigurationReader {
 
 	/** The Constant LOGGER. */
 	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationReader.class);
 
 	/** The configuration file. */
 	private final File configurationFile;
 
 	/**
 	 * Instantiates a new configuration reader.
 	 * 
 	 * @param configurationFile the configuration file
 	 */
 	public ConfigurationReader(final File configurationFile) {
 		super();
 
 		checkNotNull(configurationFile, "Configuration file is null");
 		checkArgument(configurationFile.exists(), "Configuration file does not exist");
 
 		this.configurationFile = configurationFile;
 	}
 
 	/**
 	 * Parses the file.
 	 * 
 	 * @param fileType the file type
 	 * 
 	 * @return the file
 	 */
 	private FileReference parseFile(final FileType fileType) {
 		final String prefix = "parseFile() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		FileReference file = new FileReference();
 
 		file.setName(StringUtils.trim(fileType.getName()));
 		file.setTargetName(StringUtils.trim(fileType.getTargetName()));
 
 		if (file.isEmpty()) {
 			file = null;
 		}
 
 		LOGGER.debug("{} returning {}", prefix, file);
 
 		return file;
 	}
 
 	/**
 	 * Parses the files.
 	 * 
 	 * @param task the task
 	 * @param filesType the files type
 	 */
 	private void parseFiles(final FilesTask task, final FilesType filesType) {
 		final String prefix = "parseFiles() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		if (filesType == null) {
 			LOGGER.debug("{} filesType is null, exiting", prefix);
 
 			return;
 		}
 
 		final List<FileReference> files = task.getFiles();
 		final FileType[] fileTypeArray = filesType.getFileArray();
 
 		if (ArrayUtils.isEmpty(fileTypeArray)) {
 			LOGGER.debug("{} fileArray is empty is null, exiting", prefix);
 
 			return;
 		}
 
 		LOGGER.debug("{} parsing {} file types", prefix, fileTypeArray.length);
 
 		for (final FileType fileType : fileTypeArray) {
 			final FileReference file = parseFile(fileType);
 
 			if (file != null) {
 				files.add(file);
 			}
 		}
 
 		LOGGER.debug("{} exiting", prefix);
 	}
 
 	/**
 	 * Parses the file system delete task.
 	 * 
 	 * @param taskType the task type
 	 * @param job the job
 	 * @param configuration the configuration
 	 * @return the task
 	 */
 	private AbstractTask parseFileSystemDeleteTask(final FileSystemDeleteTaskType taskType, final Job job,
 			final Configuration configuration) {
 		final String prefix = "parseFileSystemDeleteTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final FileSystemDeleteTask task = new FileSystemDeleteTask(job);
 
 		task.setStopOnError(taskType.getStopOnError());
 		task.setTarget(parseFileSystemTarget(taskType.getFileSystemTarget(), configuration));
 
 		parseFiles(task, taskType.getFiles());
 
 		LOGGER.debug("{} returning {}", prefix, task);
 
 		return task;
 	}
 
 	/**
 	 * Parses the file system location.
 	 * 
 	 * @param locationType the location type
 	 * @param configuration the configuration
 	 * @return the file system location
 	 */
 	private FileSystemLocation parseFileSystemLocation(final FileSystemLocationType locationType,
 			final Configuration configuration) {
 		final String prefix = "parseFileSystemLocation() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final FileSystemLocation location = new FileSystemLocation();
 
 		location.setId(StringUtils.trim(locationType.getId()));
 		location.setPath(StringUtils.trim(locationType.getPath()));
 
 		LOGGER.debug("{} returning {}", prefix, location);
 
 		return location;
 	}
 
 	/**
 	 * Parses the file system locations.
 	 * 
 	 * @param locationsType the locations type
 	 * @param configuration the configuration
 	 */
 	private void parseFileSystemLocations(final LocationsType locationsType, final Configuration configuration) {
 		final String prefix = "parseFileSystemLocations() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<FileSystemLocation> locations = configuration.getFileSystemLocations();
 
 		LOGGER.debug("{} parsing {} file system locations", prefix, locations.size());
 
 		for (final FileSystemLocationType locationType : locationsType.getFileSystemLocationArray()) {
 			locations.add(parseFileSystemLocation(locationType, configuration));
 		}
 
 		Collections.sort(locations);
 
 		LOGGER.debug("{} exiting", prefix);
 	}
 
 	/**
 	 * Parses the file system target.
 	 * 
 	 * @param targetType the target type
 	 * @param configuration the configuration
 	 * 
 	 * @return the file system target
 	 */
 	private FileSystemTarget parseFileSystemTarget(final FileSystemTargetType targetType,
 			final Configuration configuration) {
 		final String prefix = "parseFileSystemTarget() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final FileSystemTarget target = new FileSystemTarget();
 		final FileSystemLocation targetLocation = ConfigurationUtils.findFileSystemLocationById(configuration,
 				targetType.getLocationId());
 
 		target.setLocation(targetLocation);
 		target.setRelativePath(targetType.getRelativePath());
 
 		LOGGER.debug("{} returning {}", prefix, target);
 
 		return target;
 	}
 
 	/**
 	 * Parses the find replace task.
 	 * 
 	 * @param taskType the task type
 	 * @param job the job
 	 * @param configuration the configuration
 	 * @return the abstract task
 	 */
 	private AbstractTask parseFindReplaceTask(final FindReplaceTaskType taskType, final Job job,
 			final Configuration configuration) {
 		final String prefix = "parseFindReplaceTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final FindReplaceTask task = new FindReplaceTask(job);
 
 		task.setScope(ConfigurationConversionUtils.convertScopeTypeToScope(taskType.getScope()));
 		task.setTarget(parseFileSystemTarget(taskType.getFileSystemTarget(), configuration));
 
 		parseFiles(task, taskType.getFiles());
 		parsePatterns(task, taskType.getPatterns());
 
 		LOGGER.debug("{} returning {}", prefix, task);
 
 		return task;
 	}
 
 	/**
 	 * Parses the groovy task.
 	 * 
 	 * @param taskType the task type
 	 * @param job the job
 	 * @param configuration the configuration
 	 * @return the abstract task
 	 */
 	private AbstractTask parseGroovyTask(final GroovyTaskType taskType, final Job job, final Configuration configuration) {
 		final String prefix = "parseGroovyTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final GroovyTask task = new GroovyTask(job);
 
 		task.setScript(taskType.getScript());
 		task.setTarget(parseFileSystemTarget(taskType.getFileSystemTarget(), configuration));
 
 		parseFiles(task, taskType.getFiles());
 
 		LOGGER.debug("{} returning {}", prefix, task);
 
 		return task;
 	}
 
 	/**
 	 * Parses the http download task.
 	 * 
 	 * @param taskType the task type
 	 * @param job the job
 	 * @param configuration the configuration
 	 * @return the abstract task
 	 */
 	private AbstractTask parseHttpDownloadTask(final HttpDownloadTaskType taskType, final Job job,
 			final Configuration configuration) {
 		final String prefix = "parseHttpDownloadTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final HttpDownloadTask task = new HttpDownloadTask(job);
 
 		task.setSource(parseHttpSource(taskType.getHttpSource(), configuration));
 		task.setTarget(parseFileSystemTarget(taskType.getFileSystemTarget(), configuration));
 
 		parseFiles(task, taskType.getFiles());
 
 		LOGGER.debug("{} returning {}", prefix, task);
 
 		return task;
 	}
 
 	/**
 	 * Parses the http location.
 	 * 
 	 * @param locationType the location type
 	 * @param configuration the configuration
 	 * @return the http location
 	 */
 	private HttpLocation parseHttpLocation(final HttpLocationType locationType, final Configuration configuration) {
 		final String prefix = "parseHttpLocation() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final HttpLocation location = new HttpLocation();
 
 		location.setId(StringUtils.trim(locationType.getId()));
 		location.setUrl(StringUtils.trim(StringUtils.trim(locationType.getUrl())));
 
 		LOGGER.debug("{} returning {}", prefix, location);
 
 		return location;
 	}
 
 	/**
 	 * Parses the http locations.
 	 * 
 	 * @param locationsType the locations type
 	 * @param configuration the configuration
 	 */
 	private void parseHttpLocations(final LocationsType locationsType, final Configuration configuration) {
 		final String prefix = "parseHttpLocations() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<HttpLocation> locations = configuration.getHttpLocations();
 		final HttpLocationType[] locationTypeArray = locationsType.getHttpLocationArray();
 
 		LOGGER.debug("{} parsing {} HTTP locations", prefix, locationTypeArray.length);
 
 		for (final HttpLocationType httpLocationType : locationTypeArray) {
 			locations.add(parseHttpLocation(httpLocationType, configuration));
 		}
 
 		Collections.sort(locations);
 
 		LOGGER.debug("{} exiting", prefix);
 	}
 
 	/**
 	 * Parses the http query params.
 	 * 
 	 * @param sourceType the source type
 	 * @param source the source
 	 */
 	private void parseHttpQueryParams(final HttpSourceType sourceType, final HttpSource source) {
 		final String prefix = "parseHttpQueryParams() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final Map<String, String> queryParams = new HashMap<String, String>();
 
 		if (sourceType.isSetQueryParams()) {
 			for (final QueryParam queryParam : sourceType.getQueryParams().getQueryParamArray()) {
 				queryParams.put(StringUtils.trim(queryParam.getName()), StringUtils.trim(queryParam.getValue()));
 			}
 		}
 
 		source.setQueryParams(queryParams);
 
 		LOGGER.debug("{} leaving", prefix);
 	}
 
 	/**
 	 * Parses the http source.
 	 * 
 	 * @param sourceType the source type
 	 * @param configuration the configuration
 	 * @return the http source
 	 */
 	private HttpSource parseHttpSource(final HttpSourceType sourceType, final Configuration configuration) {
 		final String prefix = "parseHttpSource() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final HttpSource source = new HttpSource();
 		final HttpLocation location = ConfigurationUtils
 				.findHttpLocationById(configuration, sourceType.getLocationId());
 
 		source.setLocation(location);
 		source.setRelativePath(sourceType.getRelativePath());
 
 		parseHttpQueryParams(sourceType, source);
 
 		LOGGER.debug("{} returning {}", prefix, source);
 
 		return source;
 	}
 
 	/**
 	 * Parses the job.
 	 * 
 	 * @param jobType the job type
 	 * @param configuration the configuration
 	 * 
 	 * @return the job
 	 */
 	private SequentialJob parseJob(final JobType jobType, final Configuration configuration) {
 		final String prefix = "parseJob() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final SequentialJob job = new SequentialJob();
 
 		job.setDescription(StringUtils.trim(jobType.getDescription()));
 		job.setId(StringUtils.trim(jobType.getId()));
 
 		final List<Task> tasks = job.getTasks();
 
 		LOGGER.debug("{} parsing {} tasks", prefix, tasks.size());
 
         if (jobType.getTasks() != null) {
             for (final TaskType taskType : jobType.getTasks().getTaskArray()) {
                 tasks.add(parseTask(taskType, job, configuration));
             }
         }
 
 		LOGGER.debug("{} returning {}", prefix, job);
 
 		return job;
 	}
 
 	/**
 	 * Parses the jobs.
 	 * 
 	 * @param configurationType the configuration type
 	 * @param configuration the configuration
 	 */
 	private void parseJobs(final UteConfigurationType configurationType, final Configuration configuration) {
 		final String prefix = "parseJobs() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<Job> jobs = configuration.getJobs();
 
 		LOGGER.debug("{} parsing {} jobs", prefix, jobs.size());
 
 		for (final JobType jobType : configurationType.getJobs().getJobArray()) {
 			jobs.add(parseJob(jobType, configuration));
 		}
 
 		Collections.sort(jobs);
 
 		LOGGER.debug("{} exiting", prefix);
 	}
 
 	/**
 	 * Parses the locations.
 	 * 
 	 * @param configurationType the configuration type
 	 * @param configuration the configuration
 	 */
 	private void parseLocations(final UteConfigurationType configurationType, final Configuration configuration) {
 		final String prefix = "parseLocations() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final LocationsType locationsType = configurationType.getLocations();
 
 		parseFileSystemLocations(locationsType, configuration);
 		parseHttpLocations(locationsType, configuration);
 		parseMavenRepositoryLocations(locationsType, configuration);
 		parseSubversionRepositoryLocations(locationsType, configuration);
 
 		LOGGER.debug("{} exiting", prefix);
 	}
 
     /**
      * Parses the Maven artifact.
      *
      * @param mavenArtifactType the Maven artifact type
      *
      * @return the file
      */
     private MavenArtifact parseMavenArtifact(final MavenArtifactType mavenArtifactType) {
         final String prefix = "parseMavenArtifact() :";
 
         LOGGER.debug("{} entered", prefix);
 
         MavenArtifact mavenArtifact = new MavenArtifact();
 
         mavenArtifact.setCoordinates(StringUtils.trim(mavenArtifactType.getCoordinates()));
         mavenArtifact.setTargetName(StringUtils.trim(mavenArtifactType.getTargetName()));
 
         if (mavenArtifact.isEmpty()) {
             mavenArtifact = null;
         }
 
         LOGGER.debug("{} returning {}", prefix, mavenArtifact);
 
         return mavenArtifact;
     }
 
     /**
      * Parses the Maven artifacts.
      *
      * @param task the task
      * @param taskType the task type
      */
     private void parseMavenArtifacts(final MavenRepositoryDownloadTask task, final MavenRepositoryDownloadTaskType taskType) {
         final String prefix = "parseMavenArtifacts() :";
 
         LOGGER.debug("{} entered", prefix);
 
         if (taskType == null) {
             LOGGER.debug("{} filesType is null, exiting", prefix);
 
             return;
         }
 
         final List<MavenArtifact> artifacts = task.getArtifacts();
         final MavenArtifactType[] mavenArtifactTypes = taskType.getArtifacts().getArtifactArray();
 
         if (ArrayUtils.isEmpty(mavenArtifactTypes)) {
             LOGGER.debug("{} fileArray is empty is null, exiting", prefix);
 
             return;
         }
 
         LOGGER.debug("{} parsing {} Maven artifact types", prefix, mavenArtifactTypes.length);
 
         for (final MavenArtifactType artifactType : mavenArtifactTypes) {
             final MavenArtifact artifact = parseMavenArtifact(artifactType);
 
             if (artifact != null) {
                 artifacts.add(artifact);
             }
         }
 
         LOGGER.debug("{} exiting", prefix);
     }
 
     /**
      * Parses the Maven repository download task.
      *
      * @param taskType the task type
      * @param job the job
      * @param configuration the configuration
      * @return the abstract task
      */
     private AbstractTask parseMavenRepositoryDownloadTask(final MavenRepositoryDownloadTaskType taskType, final Job job,
                                                           final Configuration configuration) {
         final String prefix = "parseMavenRepositoryDownloadTask() :";
 
         LOGGER.debug("{} entered", prefix);
 
         final MavenRepositoryDownloadTask task = new MavenRepositoryDownloadTask(job);
 
         task.setSource(parseMavenRepositorySource(taskType.getMavenRepositorySource(), configuration));
         task.setTarget(parseFileSystemTarget(taskType.getFileSystemTarget(), configuration));
 
         parseMavenArtifacts(task, taskType);
 
         LOGGER.debug("{} returning {}", prefix, task);
 
         return task;
     }
 
     /**
      * Parses the Maven repository location.
      *
      * @param locationType the location type
      * @param configuration the configuration
      * @return the Maven repository location
      */
     private MavenRepositoryLocation parseMavenRepositoryLocation(
             final MavenRepositoryLocationType locationType, final Configuration configuration) {
         final String prefix = "parseMavenRepositoryLocation() :";
 
         LOGGER.debug("{} entered", prefix);
 
         final MavenRepositoryLocation location = new MavenRepositoryLocation();
 
         location.setId(StringUtils.trim(locationType.getId()));
         location.setUrl(StringUtils.trim(locationType.getUrl()));
 
         LOGGER.debug("{} returning {}", prefix, location);
 
         return location;
     }
 
     /**
      * Parses the Maven repository locations.
      *
      * @param locationsType the locations type
      * @param configuration the configuration
      */
     private void parseMavenRepositoryLocations(final LocationsType locationsType, final Configuration configuration) {
         final String prefix = "parseMavenRepositoryLocations() :";
 
         LOGGER.debug("{} entered", prefix);
 
         final List<MavenRepositoryLocation> locations = configuration.getMavenRepositoryLocations();
 
         final MavenRepositoryLocationType[] locationTypeArray = locationsType
                 .getMavenRepositoryLocationArray();
 
         LOGGER.debug("{} parsing {} Subversion repository locations", prefix, locationTypeArray.length);
 
         for (final MavenRepositoryLocationType mavenRepositoryLocationType : locationTypeArray) {
             locations.add(parseMavenRepositoryLocation(mavenRepositoryLocationType, configuration));
         }
 
         Collections.sort(locations);
 
         LOGGER.debug("{} exiting", prefix);
     }
 
     /**
      * Parses the Maven repository source.
      *
      * @param sourceType the source type
      * @param configuration the configuration
      * @return the Maven repository source
      */
     private MavenRepositorySource parseMavenRepositorySource(final MavenRepositorySourceType sourceType, final Configuration configuration) {
         final String prefix = "parseMavenRepositorySource() :";
 
         LOGGER.debug("{} entered", prefix);
 
         final MavenRepositorySource source = new MavenRepositorySource();
         final MavenRepositoryLocation location = ConfigurationUtils
                 .findMavenRepositoryLocationById(configuration, sourceType.getLocationId());
 
         source.setLocation(location);
         source.setRelativePath(sourceType.getRelativePath());
 
         LOGGER.debug("{} returning {}", prefix, source);
 
         return source;
     }
 
 	/**
 	 * Parses the pattern.
 	 * 
 	 * @param patternType the pattern type
 	 * @return the find replace pattern
 	 */
 	private FindReplacePattern parsePattern(final PatternType patternType) {
 		final String prefix = "parsePattern() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		FindReplacePattern pattern = new FindReplacePattern();
 
 		// Intentially not trimming find and replace as they may contain leading or trailing spaces
 		pattern.setFind(patternType.getFind());
 		pattern.setReplace(patternType.getReplace());
 
 		if (pattern.isEmpty()) {
 			pattern = null;
 		}
 		else if (!pattern.isValid()) {
 			LOGGER.error("{} The find pattern \"{}\" is not valid", prefix, patternType.getFind());
 			throw new ConfigurationException(ExceptionResourceType.INVALID_PATTERN, patternType.getFind());
 		}
 
 		LOGGER.debug("{} returning {}", prefix, pattern);
 
 		return pattern;
 	}
 
 	/**
 	 * Parses the patterns.
 	 * 
 	 * @param task the task
 	 * @param patternsType the patterns type
 	 */
 	private void parsePatterns(final FindReplaceTask task, final PatternsType patternsType) {
 		final String prefix = "parsePatterns() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		if (patternsType == null) {
 			LOGGER.debug("{} patternsType is null, exiting", prefix);
 
 			return;
 		}
 
 		final List<FindReplacePattern> patterns = task.getPatterns();
 		final PatternType[] patternTypeArray = patternsType.getPatternArray();
 
 		if (ArrayUtils.isEmpty(patternTypeArray)) {
 			LOGGER.debug("{} fileArray is empty is null, exiting", prefix);
 
 			return;
 		}
 
 		LOGGER.debug("{} parsing {} pattern types", prefix, patternTypeArray.length);
 
 		for (final PatternType patternType : patternTypeArray) {
 			final FindReplacePattern pattern = parsePattern(patternType);
 
 			if (pattern != null) {
 				patterns.add(pattern);
 			}
 		}
 
 		LOGGER.debug("{} exiting", prefix);
 	}
 
 	/**
 	 * Parses the preference.
 	 * 
 	 * @param preferenceType the preference type
 	 * @return the preference
 	 */
 	private Preference parsePreference(final PreferenceType preferenceType) {
 		final String prefix = "parsePreference() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final Preference preference = new Preference();
 
 		preference.setId(StringUtils.trim(preferenceType.getId()));
 
 		LOGGER.debug("{} returning {}", prefix, preference);
 
 		return preference;
 	}
 
 	/**
 	 * Parses the preferences.
 	 * 
 	 * @param configurationType the configuration type
 	 * @param configuration the configuration
 	 */
 	private void parsePreferences(final UteConfigurationType configurationType, final Configuration configuration) {
 		final String prefix = "parsePreferences() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<Preference> preferences = configuration.getPreferences();
 
 		LOGGER.debug("{} parsing {} preferences", prefix, preferences.size());
 
 		for (final PreferenceType preferenceType : configurationType.getPreferences().getPreferenceArray()) {
 			preferences.add(parsePreference(preferenceType));
 		}
 
 		Collections.sort(preferences);
 
 		LOGGER.debug("{} exiting", prefix);
 	}
 
 	/**
 	 * Parses the properties.
 	 * 
 	 * @param configurationType the configuration type
 	 * @param configuration the configuration
 	 */
 	private void parseProperties(final UteConfigurationType configurationType, final Configuration configuration) {
 		final String prefix = "parseProperties() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<Property> properties = configuration.getProperties();
 
 		LOGGER.debug("{} parsing {} properties", prefix, properties.size());
 
 		for (final PropertyType propertyType : configurationType.getProperties().getPropertyArray()) {
 			properties.add(parseProperty(propertyType));
 		}
 
 		Collections.sort(properties);
 
 		LOGGER.debug("{} exiting", prefix);
 	}
 
 	/**
 	 * Parses the property.
 	 * 
 	 * @param propertyType the property type
 	 * 
 	 * @return the property
 	 */
 	private Property parseProperty(final PropertyType propertyType) {
 		final String prefix = "parseProperty() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final Property property = new Property();
 
 		property.setId(StringUtils.trim(propertyType.getId()));
 		property.setValue(StringUtils.trim(propertyType.getValue()));
 
 		LOGGER.debug("{} returning {}", prefix, property);
 
 		return property;
 	}
 
 	/**
 	 * Parses the subversion export task.
 	 * 
 	 * @param taskType the task type
 	 * @param job the job
 	 * @param configuration the configuration
 	 * @return the task
 	 */
 	private AbstractTask parseSubversionExportTask(final SubversionExportTaskType taskType, final Job job,
 			final Configuration configuration) {
 		final String prefix = "parseSubversionExportTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final SubversionExportTask task = new SubversionExportTask(job);
 
 		task.setSource(parseSubversionRepositorySource(taskType.getSubversionRepositorySource(), configuration));
 		task.setTarget(parseFileSystemTarget(taskType.getFileSystemTarget(), configuration));
 
 		task.setDepth(ConfigurationConversionUtils.convertSubversionDepthTypeToSubversionDepth(taskType.getDepth()));
 		parseSubversionRevision(task, taskType.getRevision());
 
 		parseFiles(task, taskType.getFiles());
 
 		LOGGER.debug("{} returning {}", prefix, task);
 
 		return task;
 	}
 
 	/**
 	 * Parses the subversion repository location.
 	 * 
 	 * @param locationType the location type
 	 * @param configuration the configuration
 	 * @return the subversion repository location
 	 */
 	private SubversionRepositoryLocation parseSubversionRepositoryLocation(
			final SubversionRespositoryLocationType locationType, final Configuration configuration) {
 		final String prefix = "parseSubversionRepositoryLocation() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final SubversionRepositoryLocation location = new SubversionRepositoryLocation();
 
 		location.setId(StringUtils.trim(locationType.getId()));
 		location.setUrl(StringUtils.trim(locationType.getUrl()));
 		location.setUsername(StringUtils.trim(locationType.getUsername()));
 		location.setPassword(StringUtils.trim(locationType.getPassword()));
 
 		LOGGER.debug("{} returning {}", prefix, location);
 
 		return location;
 	}
 
 	/**
 	 * Parses the subversion repository locations.
 	 * 
 	 * @param locationsType the locations type
 	 * @param configuration the configuration
 	 */
 	private void parseSubversionRepositoryLocations(final LocationsType locationsType, final Configuration configuration) {
 		final String prefix = "parseSubversionRepositoryLocations() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final List<SubversionRepositoryLocation> locations = configuration.getSubversionRepositoryLocations();
 
		final SubversionRespositoryLocationType[] locationTypeArray = locationsType
				.getSubversionRespositoryLocationArray();
 
 		LOGGER.debug("{} parsing {} Subversion repository locations", prefix, locationTypeArray.length);
 
		for (final SubversionRespositoryLocationType subversionRepositoryLocationType : locationTypeArray) {
 			locations.add(parseSubversionRepositoryLocation(subversionRepositoryLocationType, configuration));
 		}
 
 		Collections.sort(locations);
 
 		LOGGER.debug("{} exiting", prefix);
 	}
 
 	/**
 	 * Parses the subversion repository source.
 	 * 
 	 * @param sourceType the source type
 	 * @param configuration the configuration
 	 * 
 	 * @return the subversion repository source
 	 */
 	private SubversionRepositorySource parseSubversionRepositorySource(final SubversionRepositorySourceType sourceType,
 			final Configuration configuration) {
 		final String prefix = "parseSubversionRepositorySource() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final SubversionRepositorySource source = new SubversionRepositorySource();
 		final SubversionRepositoryLocation location = ConfigurationUtils.findSubversionRepositoryLocationById(
 				configuration, sourceType.getLocationId());
 
 		source.setLocation(location);
 		source.setRelativePath(StringUtils.trim(sourceType.getRelativePath()));
 
 		LOGGER.debug("{} returning {}", prefix, source);
 
 		return source;
 	}
 
 	/**
 	 * Parses the subversion revision.
 	 * 
 	 * @param task the task
 	 * @param revision the revision
 	 */
 	private void parseSubversionRevision(final SubversionExportTask task, final String revision) {
 		final String prefix = "parseSubversionRevision() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final String trimmedRevision = StringUtils.trimToNull(revision);
 
 		if (trimmedRevision == null || SubversionUtils.isHeadRevision(trimmedRevision)) {
 			task.setRevision(SubversionRevision.HEAD);
 		}
 		else if (SubversionUtils.isRevisionNumber(trimmedRevision)) {
 			task.setRevision(SubversionRevision.NUMBERED);
 			task.setRevisionNumber(Long.valueOf(trimmedRevision));
 		}
 		else if (SubversionUtils.isRevisionDate(trimmedRevision)) {
 			task.setRevision(SubversionRevision.DATE);
 			task.setRevisionDate(SubversionUtils.parseRevisionDate(trimmedRevision));
 		}
 		else {
 			LOGGER.error("{} Invalid revision value", prefix);
 			throw new RuntimeException("Invalid revision value"); // TODO
 		}
 
 		LOGGER.debug("{} returning", prefix);
 	}
 
 	/**
 	 * Parses the subversion export task.
 	 * 
 	 * @param taskType the task type
 	 * @param job the job
 	 * @param configuration the configuration
 	 * @return the task
 	 */
 	private AbstractTask parseSubversionUpdateTask(final SubversionUpdateTaskType taskType, final Job job,
 			final Configuration configuration) {
 		final String prefix = "parseSubversionUpdateTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		final SubversionUpdateTask task = new SubversionUpdateTask(job);
 
 		task.setTarget(parseFileSystemTarget(taskType.getFileSystemTarget(), configuration));
 
 		parseFiles(task, taskType.getFiles());
 
 		LOGGER.debug("{} returning {}", prefix, task);
 
 		return task;
 	}
 
 	/**
 	 * Parses the task.
 	 * 
 	 * @param taskType the task type
 	 * @param job the job
 	 * @param configuration the configuration
 	 * @return the task
 	 */
 	private AbstractTask parseTask(final TaskType taskType, final Job job, final Configuration configuration) {
 		final String prefix = "parseTask() :";
 
 		LOGGER.debug("{} entered", prefix);
 
 		AbstractTask task = null;
 
 		if (taskType.isSetFileSystemDeleteTask()) {
 			task = parseFileSystemDeleteTask(taskType.getFileSystemDeleteTask(), job, configuration);
 		}
 		else if (taskType.isSetFindReplaceTask()) {
 			task = parseFindReplaceTask(taskType.getFindReplaceTask(), job, configuration);
 		}
 		else if (taskType.isSetGroovyTask()) {
 			task = parseGroovyTask(taskType.getGroovyTask(), job, configuration);
 		}
 		else if (taskType.isSetHttpDownloadTask()) {
 			task = parseHttpDownloadTask(taskType.getHttpDownloadTask(), job, configuration);
 		}
 		else if (taskType.isSetMavenRepositoryDownloadTask()) {
 			task = parseMavenRepositoryDownloadTask(taskType.getMavenRepositoryDownloadTask(), job, configuration);
 		}
 		else if (taskType.isSetSubversionExportTask()) {
 			task = parseSubversionExportTask(taskType.getSubversionExportTask(), job, configuration);
 		}
 		else if (taskType.isSetSubversionUpdateTask()) {
 			task = parseSubversionUpdateTask(taskType.getSubversionUpdateTask(), job, configuration);
 		}
 		else {
 			LOGGER.error("{} Unsupported task type {}", prefix, taskType.getClass());
 			throw new ConfigurationException(ExceptionResourceType.UNSUPPORTED_TASK_TYPE, taskType.getClass());
 		}
 
 		task.setDescription(taskType.getDescription());
 		task.setEnabled(taskType.getEnabled());
 		task.setId(taskType.getId());
 
 		LOGGER.debug("{} returning {}", prefix, task);
 
 		return task;
 	}
 
 	/**
 	 * Read.
 	 * 
 	 * @return the configuration
 	 */
 	public Configuration read() {
 		final String prefix = "read() :";
 
 		LOGGER.debug("{} entered, file={}", prefix, configurationFile);
 
 		final Configuration configuration = new Configuration();
 
 		configuration.setConfigurationFile(configurationFile);
 
 		try {
 			final UteConfigurationDocument document = UteConfigurationDocument.Factory.parse(configurationFile);
 			final UteConfigurationType configurationType = document.getUteConfiguration();
 
 			parsePreferences(configurationType, configuration);
 			parseProperties(configurationType, configuration);
 			parseLocations(configurationType, configuration);
 			parseJobs(configurationType, configuration);
 
 			ConfigurationUtils.validateConfiguration(configuration);
 		}
 		catch (final XmlException e) {
 			LOGGER.error("XmlException caught while parsing configuration file", e);
 			throw new ConfigurationException(ExceptionResourceType.INVALID_CONFIGURATION_FILE, e);
 		}
 		catch (final IOException e) {
 			LOGGER.error("IOException caught while parsing configuration file", e);
 			throw new ConfigurationException(ExceptionResourceType.ERROR_LOADING_CONFIGURATION_FILE, e);
 		}
 
 		LOGGER.debug("{} returning {}", prefix, configuration);
 
 		return configuration;
 	}
 }
