 /*******************************************************************************
  * Copyright (c) May 20, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Martin Taal - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.data.importer;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.eclipse.emf.cdo.common.id.CDOID;
 import org.eclipse.emf.cdo.transaction.CDOTransaction;
 import org.eclipse.emf.cdo.util.CommitException;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.resource.Resource;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.NetxstudioPackage;
 import com.netxforge.netxstudio.ServerSettings;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.data.IDataProvider;
 import com.netxforge.netxstudio.data.importer.IComponentLocator.IdentifierDescriptor;
 import com.netxforge.netxstudio.data.importer.IComponentLocator.MetricDescriptor;
 import com.netxforge.netxstudio.data.internal.DataActivator;
 import com.netxforge.netxstudio.data.job.IRunMonitor;
 import com.netxforge.netxstudio.generics.DateTimeRange;
 import com.netxforge.netxstudio.generics.GenericsFactory;
 import com.netxforge.netxstudio.generics.Value;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.metrics.IdentifierDataKind;
 import com.netxforge.netxstudio.metrics.KindHintType;
 import com.netxforge.netxstudio.metrics.Mapping;
 import com.netxforge.netxstudio.metrics.MappingColumn;
 import com.netxforge.netxstudio.metrics.MappingRecord;
 import com.netxforge.netxstudio.metrics.MappingStatistic;
 import com.netxforge.netxstudio.metrics.Metric;
 import com.netxforge.netxstudio.metrics.MetricSource;
 import com.netxforge.netxstudio.metrics.MetricsFactory;
 import com.netxforge.netxstudio.metrics.MetricsPackage;
 import com.netxforge.netxstudio.metrics.ObjectKindType;
 import com.netxforge.netxstudio.metrics.ValueDataKind;
 import com.netxforge.netxstudio.metrics.ValueKindType;
 import com.netxforge.netxstudio.scheduling.JobRunState;
 
 /**
  * The main entry class for the Metrics importing. Uses a delegation pattern so
  * this can be used on client and server.
  * 
  * @author Martin Taal
  * @author Christophe Bouhier
  */
 public abstract class AbstractMetricValuesImporter implements IImporterHelper,
 		IMetricValueImporter {
 
 	private MetricSource metricSource;
 
 	private IRunMonitor jobMonitor;
 
 	// Note: Not injected, as we inject with a local provider.
 	protected IDataProvider dataProvider;
 
 	protected IComponentLocator componentLocator;
 
 	protected ModelUtils modelUtils;
 
 	/** A Global collection of mapping records, added to a statistic **/
 	private List<MappingRecord> failedRecords = new ArrayList<MappingRecord>();
 
 	private String importMessage;
 
 	private List<ImportWarning> warnings = new ArrayList<ImportWarning>();
 
 	private Throwable throwable;
 
 	private int intervalHint = 60;
 
 	/** An estimate of the interval is based on the processed time stamps. **/
 	private int intervalEstimate = -1;
 
 	/**
 	 * The helper provides implementation of specialized methods depending on
 	 * the environmnet (Client or Server).
 	 * 
 	 */
 	private IImporterHelper helper;
 
 	private CDOID cdoID;
 
 	/*
 	 * Maximum number of stats per Metric Source.
 	 */
 	private int maxStats = -1;
 
 	private TSProcessor tsProcessor;
 
 	/** A cached header time stamp */
 	private Date headerTimeStamp = null;
 
 	/**
 	 * A collection of descriptors which act as a template and will be populated
 	 * with the correct values when processing each row
 	 **/
 	private List<IComponentLocator.IdentifierDescriptor> dataIdentifiers;
 
 	/**
 	 * A collection of descriptors for the header which is always populated.
 	 */
 	private List<IComponentLocator.IdentifierDescriptor> headerIdentifiers = new ArrayList<IComponentLocator.IdentifierDescriptor>();
 
 	/**
 	 * The run period, this is the over period of imported values for this run.
 	 */
 	private PeriodEstimate runPeriodEstimate = new PeriodEstimate();
 
 	/**
 	 * A sub period, for example if multiple files are processed in one run.
 	 */
 	private PeriodEstimate subPeriodEstimate;
 
 	@Inject
 	public AbstractMetricValuesImporter(IComponentLocator locator,
 			ModelUtils modelUtils) {
 		this.componentLocator = locator;
 		this.modelUtils = modelUtils;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#process()
 	 */
 	public void process() {
 
 		if (DataActivator.DEBUG) {
 			DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 					"Start processing import");
 		}
 
 		initializeProviders(componentLocator);
 
 		// Indexer feedback.
 		int totalSeconds = 0;
 
 		// Wait synchronized.
 		synchronized (this) {
 			while (!componentLocator.isInitialized()) {
 				try {
 					if (DataActivator.DEBUG) {
 						DataActivator.TRACE.trace(
 								DataActivator.TRACE_IMPORT_OPTION,
 								"Yawn.... waiting for locator (" + totalSeconds
 										+ " sec)");
 					}
 					this.wait(1000);
 					totalSeconds++;
 				} catch (InterruptedException e) {
 					if (DataActivator.DEBUG) {
 						DataActivator.TRACE.trace(
 								DataActivator.TRACE_IMPORT_OPTION,
 								"Error while waiting for locator.", e);
 					}
 				}
 			}
 		}
 
 		final long startTime = System.currentTimeMillis();
 		long endTime = startTime;
 		boolean errorOccurred = false;
 		int totalRows = 0;
 		String message = "Stat initialized";
 
 		// The parent mapping statistic, later rewrite the end time, total rows
 		// etc...
 		final MappingStatistic mappingStatistic = createMappingStatistics(
 				startTime, endTime, 0, message, getMappingIntervalEstimate(),
 				null);
 
 		// Opens a session, first transaction on the IDataProvider
 		// The MetricSource is cached and used to
 		// Add the main statistic
 		// Get the name for logging.
 		// Get the file pattern.
 		// Get the mapping information.
 		// When update the statistics.
 		addAndTruncate(mappingStatistic, getMetricSource().getStatistics());
 
 		commitTransactionWithoutClosing();
 
 		try {
 			jobMonitor.setTask("Processing metricsource "
 					+ getMetricSource().getName());
 
 			if (DataActivator.DEBUG) {
 				DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 						"-- Processing metricsource "
 								+ getMetricSource().getName());
 			}
 
 			final String msLocation = getMetricSource().getMetricLocation();
 
 			String rootUrl = System.getProperty(ROOT_SYSTEM_PROPERTY);
 			// CB 27-09-2011 ROOT_SYSTEM_PROPERTY as fallback, on the server
 			// setting.
 			Resource settingsResource = this.getDataProvider().getResource(
 					NetxstudioPackage.Literals.SERVER_SETTINGS);
 			if (settingsResource != null
 					&& settingsResource.getContents().size() == 1) {
 				ServerSettings settings = (ServerSettings) settingsResource
 						.getContents().get(0);
 				rootUrl = settings.getImportPath();
 			}
 
 			// CB 27-09-2011, add a separator
 			if (!rootUrl.endsWith(File.separator)
 					&& !msLocation.startsWith(File.separator)) {
 				rootUrl += File.separator;
 			}
 
 			final String fileOrDirectory = rootUrl + msLocation;
 
 			if (DataActivator.DEBUG) {
 				DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 						"-- Calculated import directory for "
 								+ getMetricSource().getName() + " ="
 								+ fileOrDirectory);
 			}
 
 			boolean noFiles = true;
 			final StringBuilder fileList = new StringBuilder();
 			final File rootFile = new File(fileOrDirectory);
 
 			String filterPattern = getMetricSource().getFilterPattern();
 			if (filterPattern == null && getFileExtension() != null) {
 				filterPattern = "[^\\s]+(\\.(?i)" + getFileExtension() + ")$";
 			} else {
 				// We concat the file extension, to the pattern automaticly.
 				filterPattern = filterPattern.concat("(\\.(?i)"
 						+ getFileExtension() + ")$");
 
 				Pattern.compile(filterPattern);
 			}
 
 			if (DataActivator.DEBUG) {
 				DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 						"-- Looking for files with pattern" + filterPattern);
 			}
 
 			// Start processing in the specified root directory, make sure it
 			// exists.
 			if (!rootFile.exists()) {
 				jobMonitor.appendToLog("Root directory/file ("
 						+ rootFile.getAbsolutePath() + ") does not exist");
 				noFiles = true;
 				errorOccurred = true;
 			} else if (rootFile.isFile()) {
 				try {
 					noFiles = false;
 					fileList.append(rootFile.getAbsolutePath());
 					totalRows += doProcessFile(rootFile, mappingStatistic);
 				} catch (final Throwable t) {
 					errorOccurred = true;
 					jobMonitor.appendToLog("Error (" + t.getMessage()
 							+ ") while processing file "
 							+ rootFile.getAbsolutePath());
 				}
 			} else {
 				// After each iteration, check if the scheduler is active, and
 				// cancel otherwise.
 				for (final File file : rootFile.listFiles()) {
 					if (cancelled()) {
 						// we are cancelled, abort the next file.
 						if (DataActivator.DEBUG) {
 							DataActivator.TRACE
 									.trace(DataActivator.TRACE_IMPORT_OPTION,
 											"Importer instructed to abort the import process");
 						}
 						break;
 					}
 
 					// Track the file name and orignal path.
 					final String fileName = file.getName();
 					final String originalPath = file.getAbsolutePath();
 
 					if (DataActivator.DEBUG) {
 						DataActivator.TRACE.trace(
 								DataActivator.TRACE_IMPORT_OPTION,
 								"-- Checking file=" + fileName);
 					}
 					if (filterPattern == null
 							|| fileName.matches(filterPattern)) {
 						try {
 							fileList.append((fileList.length() > 0 ? "\n" : "")
 									+ fileName);
 							noFiles = false;
 							totalRows += doProcessFile(file, mappingStatistic);
 
 						} catch (final Throwable t) {
 							errorOccurred = true;
 							// Processing failed, the file name will still be
 							// named .process
 							// Consider renaming it to .error
 							errorProcessFile(file, originalPath);
 							jobMonitor.appendToLog("Error (" + t.getMessage()
 									+ ") while processing file "
 									+ file.getAbsolutePath());
 							if (DataActivator.DEBUG) {
 								DataActivator.TRACE.trace(
 										DataActivator.TRACE_IMPORT_OPTION,
 										" -- Error processing file", t);
 							}
 
 						}
 					}
 
 				}
 			}
 
 			if (noFiles) {
 				jobMonitor.appendToLog("No files found for processing");
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_IMPORT_OPTION,
 							" -- No files found for processing");
 				}
 			}
 
 			jobMonitor.setTask("Processing metricsource "
 					+ getMetricSource().getName());
 			jobMonitor.setMsg("Creating mappingstatistics");
 			jobMonitor.incrementProgress(1, true);
 
 			// Moves the records from the subprocesses....
 			// mappingStatistic.geecords().addAll(getFailedRecords());
 
 			message = noFiles ? "No files processed" : fileList.toString();
 			if (message.length() > 2000) {
 				message = message.substring(0, 2000);
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE
 							.trace(DataActivator.TRACE_IMPORT_OPTION,
 									"Message too long for processing, trunked on 2000 characters!");
 				}
 			}
 			
 			
 			if (errorOccurred || getFailedRecords().size() > 0) {
 				jobMonitor.setFinished(JobRunState.FINISHED_WITH_ERROR, null);
 			} else {
 				jobMonitor.setFinished(JobRunState.FINISHED_SUCCESSFULLY, null);
 			}
 
 			if (DataActivator.DEBUG) {
 				DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 						"Succesfull processing import for metric source"
 								+ getMetricSource().getName());
 			}
 
 		} catch (final Throwable t) {
 			message = t.getMessage();
 			if (t instanceof PatternSyntaxException) {
 				message = "File filter pattern is not valid, abort";
 			}
 
 			jobMonitor.setFinished(JobRunState.FINISHED_WITH_ERROR, t);
 			
 			if (DataActivator.DEBUG) {
 				DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 						"Error Processing metricsource "
 								+ getMetricSource().getName(), t);
 			}
 		} finally {
 			// finalize with a mapping statistic.
 			// Set the end time on the mapping stats.
 			endTime = System.currentTimeMillis();
 			mappingStatistic.getMappingDuration().setEnd(
 					modelUtils.toXMLDate(new Date(endTime)));
 			mappingStatistic.setMessage(message);
 			mappingStatistic.setTotalRecords(totalRows);
 			getRunPeriodEstimate().write(mappingStatistic);
 		}
 
 		if (DataActivator.DEBUG) {
 			DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 					"Mapping statistics");
 			DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 					" -- from ="
 							+ mappingStatistic.getMappingDuration().getBegin()
 							+ " to "
 							+ mappingStatistic.getMappingDuration().getEnd());
 			DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 					" -- total records =" + totalRows);
 			DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 					" -- Period Estimate from ="
 							+ getRunPeriodEstimate().metricPeriodEstimateBegin
 							+ " to "
 							+ getRunPeriodEstimate().metricPeriodEstimateEnd);
 			DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 					" -- Interval (estimate) =" + intervalEstimate);
 		}
 
 		commitTransactionAndClose();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#setImporter
 	 * (com.netxforge.netxstudio.data.importer.AbstractMetricValuesImporter)
 	 */
 	public void setImporter(AbstractMetricValuesImporter importer) {
 		// Ignore, should not be called here.
 	}
 
 	/**
 	 * @param mappingStatistic
 	 * @param statistics
 	 */
 	public void addAndTruncate(final MappingStatistic mappingStatistic,
 			EList<MappingStatistic> statistics) {
 
 		// Lazy init maxStats var.
 		if (maxStats == -1) {
 			boolean storeMaxStats = false;
 			String property = properties().getProperty(
 					NETXSTUDIO_MAX_MAPPING_STATS_QUANTITY);
 			if (property == null) {
 				maxStats = new Integer(
 						NETXSTUDIO_MAX_MAPPING_STATS_QUANTITY_DEFAULT);
 				storeMaxStats = true;
 
 			} else {
 				try {
 					maxStats = new Integer(property);
 				} catch (NumberFormatException nfe) {
 
 					if (DataActivator.DEBUG) {
 						DataActivator.TRACE.trace(
 								DataActivator.TRACE_IMPORT_OPTION,
 								"Error reading property", nfe);
 					}
 
 					maxStats = new Integer(
 							NETXSTUDIO_MAX_MAPPING_STATS_QUANTITY_DEFAULT);
 					storeMaxStats = true;
 				}
 			}
 
 			if (storeMaxStats) {
 				// Should be saved when the Activator stops!
 				properties().setProperty(NETXSTUDIO_MAX_MAPPING_STATS_QUANTITY,
 						new Integer(maxStats).toString());
 			}
 		}
 
 		statistics.add(0, mappingStatistic);
 
 		// truncate the list, if exceeding max. size.
 		if (statistics.size() > maxStats) {
 
 			List<MappingStatistic> subList = Lists.newArrayList(statistics
 					.subList(0, maxStats));
 
 			boolean retainAll = statistics.retainAll(subList);
 
 			if (retainAll) {
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_IMPORT_OPTION,
 							"truncing mapping statistics to max " + maxStats);
 				}
 			}
 		}
 	}
 
 	/*
 	 * Commits, but doesn't close the transaction nor the session.
 	 */
 	private void commitTransactionWithoutClosing() {
 		// Commit in a throwable, otherwise the session woudn't be closed.
 		try {
 			CDOTransaction transaction = getDataProvider().getTransaction();
 			transaction.setCommitComment(IDataProvider.SERVER_COMMIT_COMMENT);
 			transaction.commit();
 		} catch (final Throwable t) {
 			if (DataActivator.DEBUG) {
 				DataActivator.TRACE.trace(
 						DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 						"Commit failed", t);
 			}
 		}
 	}
 
 	private void commitTransactionAndClose() {
 		try {
 			getDataProvider().commitTransactionThenClose();
 		} catch (final Throwable t) {
 			if (DataActivator.DEBUG) {
 				DataActivator.TRACE.trace(
 						DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 						"Commit failed", t);
 			}
 		} finally {
 			getDataProvider().closeSession();
 		}
 	}
 
 	private int doProcessFile(File file, MappingStatistic parentStatistic)
 			throws Throwable {
 
 		int rows = 0;
 
 		final String fileName = file.getName();
 		final String originalPath = file.getAbsolutePath();
 
 		// Work with the renamed file, abort if rename fails.
 
 		if (RENAME_FILE_AT_PROCESS) {
 			file = preProcessFile(file);
 			if (file == null) {
 				return rows;
 			}
 		}
 		
 		// Clear the run parameters.
 		setImportMessage("");
 		getFailedRecords().clear();
 		final int beforeFailedSize = getFailedRecords().size();
 		setSubPeriodEstimate(new PeriodEstimate());
 		final long startTime = System.currentTimeMillis();
 		long endTime = startTime;
 		
 		// CB 13-01, changed to task, as msg, will be overwritten quickly.
 		jobMonitor.setTask("Processing file " + fileName);
 		jobMonitor.appendToLog("Processing file " + fileName);
 		rows = processFile(file);
 
 		int afterFailedSize = getFailedRecords().size();
 		List<MappingRecord> fileFailedRecords = null;
 		if (afterFailedSize > beforeFailedSize) {
 			// http://work.netxforge.com/issues/324
 			fileFailedRecords = this.getFailedRecords().subList(
 					beforeFailedSize, afterFailedSize);
 			if (DataActivator.DEBUG) {
 				DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 						"-- # of failed records for file=" + file.getName()
 								+ " is =" + fileFailedRecords.size());
 			}
 
 		}
 
 		endTime = System.currentTimeMillis();
 
 		// http://work.netxforge.com/issues/357
 		// Add information about created resources to the message.
 		String createdResourcesCount = this.getImportMessage();
 
 		final MappingStatistic fileMappingStatistics = this
 				.createMappingStatistics(startTime, endTime, rows, fileName
 						+ " created resource: " + createdResourcesCount,
 						intervalEstimate, fileFailedRecords);
 
 		// Write the period estimate.
 		getSubPeriodEstimate().write(fileMappingStatistics);
 		parentStatistic.getSubStatistics().add(fileMappingStatistics);

 		// commit everything sofar in this transaction....
 		commitTransactionWithoutClosing();
 
 		postProcessFile(file, originalPath, afterFailedSize > beforeFailedSize);
 
 		return rows;
 
 	}
 
 	protected PeriodEstimate getRunPeriodEstimate() {
 		return runPeriodEstimate;
 	}
 
 	protected int getMappingIntervalEstimate() {
 		return this.intervalEstimate;
 	}
 
 	protected abstract String getFileExtension();
 
 	/**
 	 * A {@link File} being processed is renamed.
 	 * 
 	 * @param file
 	 * @return The renamed File.
 	 */
 	private File preProcessFile(File file) {
 		renameFile(file, new File(file.getAbsolutePath()
 				+ ModelUtils.EXTENSION_PROCESS));
 		File renamedFile = new File(file + ModelUtils.EXTENSION_PROCESS);
 		if (renamedFile.exists()) {
 			return renamedFile;
 		} else {
 			if (DataActivator.DEBUG) {
 				DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 						"File: " + file.getAbsolutePath()
 								+ " rename for processing failed, abort");
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * A {@link File} being processed is post-renamed.
 	 * 
 	 * @param file
 	 * @param error
 	 */
 	private void postProcessFile(File file, String originalPath, boolean error) {
 		// Strip the process extension of the file.
 
 		if (error) {
 			renameFile(file, new File(originalPath
 					+ ModelUtils.EXTENSION_DONE_WITH_FAILURES));
 		} else {
 			renameFile(file, new File(originalPath + ModelUtils.EXTENSION_DONE));
 		}
 	}
 
 	private void errorProcessFile(File file, String originalPath) {
 		// Strip the process extension of the file.
 
 		renameFile(file, new File(originalPath + ModelUtils.EXTENSION_ERROR));
 	}
 
 	/**
 	 * Rename a file being processed.
 	 * 
 	 * @param file
 	 * @param newFile
 	 */
 	private void renameFile(File file, File newFile) {
 		boolean renameTo = file.renameTo(newFile);
 		if (DataActivator.DEBUG) {
 			if (renameTo) {
 				DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 						"-- rename successed for file=" + file.getName());
 			} else {
 				DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 						"-- rename failed for file= " + file.getName()
 								+ ", the file is likely still open");
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#setImportHelper
 	 * (com.netxforge.netxstudio.data.importer.IImporterHelper)
 	 */
 	public void setImportHelper(IImporterHelper helper) {
 		this.helper = helper;
 	}
 
 	protected abstract int processFile(File file) throws Exception;
 
 	// create the mapping statistics on the basis of the errors and
 	// warnings
 	protected MappingStatistic createMappingStatistics(long startTime,
 			long endTime, int totalRows, String message, int intervalEstimate,
 			List<MappingRecord> failedRecords) {
 
 		final MappingStatistic statistic = MetricsFactory.eINSTANCE
 				.createMappingStatistic();
 
 		final DateTimeRange range = GenericsFactory.eINSTANCE
 				.createDateTimeRange();
 		range.setBegin(modelUtils.toXMLDate(new Date(startTime)));
 		range.setEnd(modelUtils.toXMLDate(new Date(endTime)));
 
 		statistic.setMappingDuration(range);
 		statistic.setTotalRecords(totalRows);
 		statistic.setMessage(message);
 		statistic.setIntervalEstimate(intervalEstimate);
 
 		// now add the failed records.
 		if (failedRecords != null) {
 			statistic.getFailedRecords().addAll(failedRecords);
 		}
 
 		return statistic;
 	}
 
 	protected abstract int getTotalRows();
 
 	protected int processRows() {
 
 		// Start TMP Transaction (for each batch of rows to process)
 		// This will be used for:
 		// 1. Getting the component object.
 		// 2. Getting the NetXResource, MetricValueRange
 		// 3. Add the values
 
 		CDOTransaction tmpTransaction = this.getDataProvider().getSession()
 				.openTransaction();
 
 		// Make sure the mapping is available.
 		// Preset the Interval hint as it might not be available from the
 		// mapping.
 		if (getMapping().getIntervalHint() > 0) {
 			intervalHint = getMapping().getIntervalHint();
 			intervalEstimate = intervalHint;
 		}
 
 		jobMonitor.setMsg("Processing header row");
 
 		// Our TimeStamp processor.
 		tsProcessor = new TSProcessor(this);
 
 		processHeaderRow();
 
 		tsProcessor.prepTimeStamp(getMapping().getDataMappingColumns());
 
 		jobMonitor.setMsg("Processing rows");
 
 		if (DataActivator.DEBUG) {
 			DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 					" processing rows, total rows=" + getTotalRows());
 		}
 
 		int totalRows = 0;
 		int rowsToProcess = getTotalRows();
 
 		jobMonitor.setTotalWork(rowsToProcess - getMapping().getFirstDataRow()
 				+ 10);
 		jobMonitor.setWorkDone(0); // reset the work done.
 
 		// The last known timestamp.
 		Date rowTimeStamp = null;
 
 		dataIdentifiers = prepareIdentifiers(getMapping()
 				.getDataMappingColumns());
 
 		List<MetricDescriptor> metricDescriptors = prepareMetricDescriptors();
 
 		// Tracks the number of newly created NetXResource objects.
 		int createdNetXResourcesCount = 0;
 
 		for (int rowNum = getMapping().getFirstDataRow(); rowNum < rowsToProcess; rowNum++) {
 
 			jobMonitor.setMsg("Processing row " + rowNum);
 			jobMonitor.incrementProgress(1, (rowNum % 10) == 0);
 
 			if (DataActivator.DEBUG) {
 				BigDecimal percentil = new BigDecimal(rowNum).divide(
 						new BigDecimal(rowsToProcess), 5, RoundingMode.HALF_UP);
 				DataActivator.TRACE.trace(DataActivator.TRACE_IMPORT_OPTION,
 						"-- new row=" + rowNum + " (" + rowsToProcess + ")"
 								+ percentil.movePointRight(2) + "%");
 			}
 
 			try {
 
 				// CB 180122012, why is this here, for a read and open a
 				// transaction?
 				// this.getMappingColumn();
 				totalRows++;
 
 				// Debug.
 				// ID prop: Position= 0 col: 2, ID prop: NodeID= YPSGSN1 col: 2,
 				// ID prop: Position= 3 col: 2, ID prop: Name= 0 col: 2
 				// Set the descriptor values for the give row.
 				populateIdentifierDescriptors(dataIdentifiers, rowNum);
 
 				// used a composed collection for processing.
 				List<IdentifierDescriptor> composedIdentifiers = Lists
 						.newArrayList(dataIdentifiers);
 
 				// Merge the descriptors from header and data when needed.
 				if (!headerIdentifiers.isEmpty()) {
 					composedIdentifiers.addAll(0, headerIdentifiers);
 				}
 
 				// Validate the identifiers.
 				validateIdentifiers(composedIdentifiers);
 
 				// Get the row timestamp
 				rowTimeStamp = tsProcessor.getTimeStampValue(rowNum, false);
 				// As last update the last row time stamp.
 				if (rowTimeStamp != null) {
 					getSubPeriodEstimate().updatePeriodEstimate(rowTimeStamp);
 				}
 
 				final int intervalHintFromRow = getIntervalHint(rowNum);
 
 				// give preference to the intervalhint from a row mapping.
 				this.intervalEstimate = intervalHintFromRow;
 
 				// Locate our components which match the given descriptors.
 				// There are multiple, later on get the correct one for the
 				// metric reference.
 
 				final List<Component> componentsForID = getComponentLocator()
 						.locateComponents(tmpTransaction, composedIdentifiers);
 
 				// If no result, skip this row we won't be able to do anything
 				// with the value here.
 				if (componentsForID.isEmpty()) {
 					// We should get the total count recorded though, so
 					// multiply by the number of metrics.
 					createNotMatchedMappingRecord(composedIdentifiers,
 							getFailedRecords());
 					continue;
 				}
 
 				// Cache a located component, to add multiple values on the same
 				// component.
 				Component locatedComponent = null;
 
 				for (MetricDescriptor md : metricDescriptors) {
 
 					// Match on the cached (Last resolved) component, if not
 					// find the correct one.
 					if (locatedComponent == null
 							|| !locatedComponent.getMetricRefs().contains(
 									md.getMetric())) {
 						Iterable<Component> componentsForMetric = modelUtils
 								.componentsForMetric(componentsForID,
 										md.getMetric());
 						int resultSize = Iterables.size(componentsForMetric);
 						if (resultSize == 1) {
 							locatedComponent = componentsForMetric.iterator()
 									.next();
 						} else if (resultSize > 1) {
 							// report a double entry.
 							createMatchDuplicatesMappingRecord(
 									composedIdentifiers, failedRecords);
 							continue;
 						}
 					}
 
 					if (locatedComponent == null) {
 						// As we are now doing the metric matching, we set
 						// the failed identifiers
 						// to be the identifiers last used for component
 						// lookup.
 						createMetricNotMatchedMappingRecord(
 								composedIdentifiers, getFailedRecords());
 
 						continue;
 					}
 
 					if (DataActivator.DEBUG) {
 						DataActivator.TRACE
 								.trace(DataActivator.TRACE_IMPORT_OPTION,
 										"-- column="
 												+ md.getColumn()
 												+ " comp: "
 												+ modelUtils
 														.printModelObject(locatedComponent)
 												+ " metric:"
 												+ modelUtils
 														.printModelObject(md
 																.getMetric()));
 					}
 
 					final Double value = getNumericCellValue(rowNum,
 							md.getColumn());
 
 					if (DataActivator.DEBUG) {
 						DataActivator.TRACE.trace(
 								DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 								"Storing value: " + value);
 					}
 
 					createdNetXResourcesCount += addMetricValue(
 							md.getValueDataKind(), rowTimeStamp,
 							locatedComponent, value, intervalHintFromRow);
 
 					// Close the temp transaction.
 
 				}
 
 			} catch (final Exception e) {
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 							"Error processing row=" + rowNum, e);
 				}
 				this.createExceptionMappingRecord(e, this.getFailedRecords());
 
 			}
 		}
 		try {
 			tmpTransaction
 					.setCommitComment(IDataProvider.SERVER_COMMIT_COMMENT);
 			tmpTransaction.commit();
 		} catch (CommitException e) {
 		} finally {
 			tmpTransaction.close();
 		}
 		this.setImportMessage(new Integer(createdNetXResourcesCount).toString());
 		return totalRows;
 	}
 	
 	private void setSubPeriodEstimate(PeriodEstimate pe){
 		subPeriodEstimate = pe;
 	}
 
 	private PeriodEstimate getSubPeriodEstimate() {
 		return subPeriodEstimate;
 	}
 
 	private List<IComponentLocator.MetricDescriptor> prepareMetricDescriptors() {
 
 		List<IComponentLocator.MetricDescriptor> metricsDescriptors = Lists
 				.newArrayList();
 		for (final MappingColumn mappingColumn : getMapping()
 				.getDataMappingColumns()) {
 
 			// columnBeingProcessed = column.getColumn();
 			if (mappingColumn.getDataType() instanceof ValueDataKind) {
 				final ValueDataKind valueDataKind = getValueDataKind(mappingColumn);
 				// Process a metric column.
 				if (valueDataKind.getValueKind() == ValueKindType.METRIC) {
 					// Check that the metric ref is set, other wise bail.
 					if (!valueDataKind
 							.eIsSet(MetricsPackage.Literals.VALUE_DATA_KIND__METRIC_REF)) {
 
 						// An invalid met Mapping entry.
 						continue;
 						// throw new java.lang.IllegalStateException(
 						// "Metric reference not set");
 					}
 
 					Metric metricRef = getValueDataKind(mappingColumn)
 							.getMetricRef();
 					MetricDescriptor md = IComponentLocator.MetricDescriptor
 							.valueFor(valueDataKind, metricRef,
 									mappingColumn.getColumn());
 					metricsDescriptors.add(md);
 
 				}
 			}
 		}
 		return metricsDescriptors;
 
 	}
 
 	/**
 	 * Produce the {@link IComponentLocator.IdentifierDescriptor Identifier
 	 * Descriptors} .</br></br> The procedure iterates through the
 	 * {@link MappingColumn mapping columns}, for each column of type
 	 * {@link IdentifierDataKind} a descriptor is produced for the specified
 	 * column index. </br></br> If the column specifies a
 	 * {@link MetricsPackage.Literals.IDENTIFIER_DATA_KIND__PATTERN }, then this
 	 * pattern. (A regular expression) is also stored in the
 	 * descriptor.</br></br> The reset parameter controls if the header
 	 * Identifier Descriptors should be merged in.
 	 * 
 	 * @param mappingColumns
 	 *            The mapping columns for this {@link MetricSource}
 	 * @param row
 	 *            The Row Number for which descriptors should be produced.
 	 * @param reset
 	 *            Set to <code>true</code> when it's not needed to merge in the
 	 *            Header Identifier Descriptors.
 	 * @return
 	 */
 	private List<IComponentLocator.IdentifierDescriptor> prepareIdentifiers(
 			EList<MappingColumn> mappingColumns) {
 
 		final List<IComponentLocator.IdentifierDescriptor> result = Lists
 				.newArrayList();
 
 		for (final MappingColumn column : mappingColumns) {
 			if (column.getDataType() instanceof IdentifierDataKind) {
 
 				final IdentifierDataKind identifierDataKind = (IdentifierDataKind) column
 						.getDataType();
 
 				// Apply the pattern to the value.
 				Pattern pattern = null;
 
 				if (identifierDataKind
 						.eIsSet(MetricsPackage.Literals.IDENTIFIER_DATA_KIND__PATTERN)) {
 					String extractionPattern = identifierDataKind.getPattern();
 					// PATTERN: if extractionPattern != null then use it to
 					// extract the
 					// to-compare value
 					// from the componentValue or the dataValue, the dataValue
 					// is read from
 					// the excel
 					if (extractionPattern != null
 							&& extractionPattern.length() > 0) {
 						pattern = Pattern.compile(extractionPattern);
 					}
 				}
 
 				final IComponentLocator.IdentifierDescriptor identifierValue = IComponentLocator.IdentifierDescriptor
 						.valueFor(identifierDataKind, pattern,
 								column.getColumn());
 				result.add(identifierValue);
 			}
 		}
 		return ImmutableList.copyOf(result);
 	}
 
 	/**
 	 * Set the values of the descriptors for mapping to components for a
 	 * specified row index.
 	 * 
 	 * @param rowNum
 	 */
 	public void populateIdentifierDescriptors(
 			List<IComponentLocator.IdentifierDescriptor> descriptors, int rowNum) {
 		for (IComponentLocator.IdentifierDescriptor descriptor : descriptors) {
 			String dataValue = getStringCellValue(rowNum,
 					descriptor.getColumn()).trim();
 			descriptor.setIdentifier(dataValue);
 		}
 	}
 
 	/**
 	 * @param elementIdentifiers
 	 */
 	private void validateIdentifiers(
 			final List<IComponentLocator.IdentifierDescriptor> elementIdentifiers) {
 		// We need at least a node and a component.
 		IdentifierValidator validator = new IdentifierValidator();
 
 		validator.validateIdentifiers(elementIdentifiers);
 
 		if (!validator.hasNodeAndChild()) {
 			if (validator.hasNode()) {
 				// Check for auto-create.
 				// throw new java.lang.IllegalStateException(
 				// "At least one child of Node Identifier is required");
 			} else {
 				// here we need to bail, not even a Node?
 				throw new java.lang.IllegalStateException(
 						"Network ELement Identifier is required");
 			}
 		}
 	}
 
 	/**
 	 * Validator for the mapping identifiers.
 	 */
 	static class IdentifierValidator {
 
 		int countNode = 0;
 		int countComponent = 0;
 		int countRelationship = 0;
 
 		public void validateIdentifiers(
 				List<IComponentLocator.IdentifierDescriptor> elementIdentifiers) {
 
 			for (IComponentLocator.IdentifierDescriptor iv : elementIdentifiers) {
 				switch (iv.getKind().getObjectKind().getValue()) {
 				case ObjectKindType.EQUIPMENT_VALUE:
 				case ObjectKindType.FUNCTION_VALUE: {
 					countComponent++;
 					break;
 				}
 				case ObjectKindType.NODE_VALUE: {
 					countNode++;
 					break;
 				}
 				case ObjectKindType.RELATIONSHIP_VALUE: {
 					countRelationship++;
 					break;
 				}
 				}
 			}
 
 		}
 
 		public boolean hasNodeAndChild() {
 			if (countNode > 0 && (countComponent > 0 || countRelationship > 0)) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 
 		public boolean hasNode() {
 			return countNode > 0;
 		}
 
 	}
 
 	/**
 	 * Learn a period from processed time stamps. Used by
 	 * {@link ResultProcessor} to check already written values.
 	 * 
 	 * @author Christophe Bouhier
 	 */
 	public class PeriodEstimate {
 
 		private Date metricPeriodEstimateBegin;
 		private Date metricPeriodEstimateEnd;
 
 		public void updatePeriodEstimate(Date rowTimeStamp) {
 
 			updateBegin(rowTimeStamp);
 			updateEnd(rowTimeStamp);
 		}
 
 		/**
 		 * @param rowTimeStamp
 		 */
 		private void updateEnd(Date rowTimeStamp) {
 			if (metricPeriodEstimateEnd == null) {
 				metricPeriodEstimateEnd = rowTimeStamp;
 			} else {
 				if (rowTimeStamp.after(metricPeriodEstimateEnd)) {
 					metricPeriodEstimateEnd = rowTimeStamp;
 				}
 			}
 		}
 
 		/**
 		 * @param rowTimeStamp
 		 */
 		private void updateBegin(Date rowTimeStamp) {
 			if (metricPeriodEstimateBegin == null) {
 				metricPeriodEstimateBegin = rowTimeStamp;
 			} else {
 				// If we are earlier, we become the begin.
 				if (rowTimeStamp.before(metricPeriodEstimateBegin)) {
 					metricPeriodEstimateBegin = rowTimeStamp;
 				}
 			}
 		}
 
 		/**
 		 * Update with another estimate.
 		 * 
 		 * @param pe
 		 */
 		public void updatePeriodEstimate(PeriodEstimate pe) {
 			updateBegin(pe.metricPeriodEstimateBegin);
 			updateEnd(pe.metricPeriodEstimateEnd);
 		}
 
 		/**
 		 * 
 		 * @return
 		 */
 		public boolean isSingle() {
 
 			// Make sure our estimate is set.
 			if (metricPeriodEstimateBegin == null
 					|| metricPeriodEstimateEnd == null) {
 				throw new IllegalStateException(
 						"Period estimate should have been derived already from this import");
 			}
 			return metricPeriodEstimateBegin.equals(metricPeriodEstimateEnd);
 		}
 
 		/**
 		 * Write this estimate in the statistic.
 		 * 
 		 * @throws IllegalStateException
 		 *             when the estimate is not determined.
 		 * 
 		 * @param mappingStat
 		 */
 		public void write(MappingStatistic mappingStat) {
 
			// Make sure our estimate is set.
 			if (metricPeriodEstimateBegin == null
 					|| metricPeriodEstimateEnd == null) {
				throw new IllegalStateException(
						"Period estimate should have been derived already from this import");
 			}
 
 			final DateTimeRange metricPeriodEstimate = GenericsFactory.eINSTANCE
 					.createDateTimeRange();
 			metricPeriodEstimate.setBegin(modelUtils
 					.toXMLDate(metricPeriodEstimateBegin));
 			metricPeriodEstimate.setEnd(modelUtils
 					.toXMLDate(metricPeriodEstimateEnd));
 			mappingStat.setPeriodEstimate(metricPeriodEstimate);
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private String createNetworkElementLocatorLog(Metric metric,
 			List<IComponentLocator.IdentifierDescriptor> identifierValues) {
 		final StringBuilder sb = new StringBuilder();
 		sb.append("Could not locate networkElement for metric "
 				+ metric.getName());
 		sb.append("Using identifiers: ");
 		for (final IComponentLocator.IdentifierDescriptor idValue : identifierValues) {
 			sb.append(" - " + idValue.getKind().getObjectKind().getName()
 					+ ": " + idValue.getIdentifier());
 		}
 		return sb.toString();
 	}
 
 	protected void processHeaderRow() {
 		final int headerRow = getMapping().getHeaderRow();
 		if (headerRow <= 0) {
 			return;
 		}
 
 		headerIdentifiers = prepareIdentifiers(getMapping()
 				.getHeaderMappingColumns());
 
 		populateIdentifierDescriptors(headerIdentifiers, headerRow);
 
 		tsProcessor.prepTimeStamp(getMapping().getHeaderMappingColumns());
 		headerTimeStamp = tsProcessor.getTimeStampValue(headerRow, true);
 
 		getRunPeriodEstimate().updatePeriodEstimate(headerTimeStamp);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.data.importer.IMetricValueImporter#
 	 * toValidExpressionName(java.lang.String)
 	 */
 	public String toValidExpressionName(String value) {
 		final StringBuilder sb = new StringBuilder();
 		for (final char c : value.toCharArray()) {
 			boolean validChar = '0' <= c && c <= '9';
 			validChar |= 'A' <= c && c <= 'Z';
 			validChar |= 'a' <= c && c <= 'z';
 			if (validChar) {
 				sb.append(c);
 			} else {
 				sb.append("_");
 			}
 		}
 		return sb.toString();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#getValueDataKind
 	 * (com.netxforge.netxstudio.metrics.MappingColumn)
 	 */
 	public ValueDataKind getValueDataKind(MappingColumn column) {
 		return (ValueDataKind) column.getDataType();
 	}
 
 	/**
 	 * 
 	 * @param allIdentifiers
 	 * @param records
 	 */
 	protected void createNotMatchedMappingRecord(
 			List<IComponentLocator.IdentifierDescriptor> allIdentifiers,
 			List<MappingRecord> records) {
 		String msg = constructErrorMessage(IMPORT_ERROR.ComponentNotMatched,
 				allIdentifiers);
 		getOrCreateMappingRecord(records, msg);
 	}
 
 	/**
 	 * Duplicate Error Record
 	 * 
 	 * @param allIdentifiers
 	 * @param records
 	 */
 	protected void createMatchDuplicatesMappingRecord(
 			List<IComponentLocator.IdentifierDescriptor> allIdentifiers,
 			List<MappingRecord> records) {
 		String msg = constructErrorMessage(
 				IMPORT_ERROR.ComponentMatchDuplicates, allIdentifiers);
 		getOrCreateMappingRecord(records, msg);
 	}
 
 	public void createMetricNotMatchedMappingRecord(
 			List<IdentifierDescriptor> composedIdentifiers,
 			List<MappingRecord> records) {
 		String msg = constructErrorMessage(IMPORT_ERROR.MetricNotFound,
 				composedIdentifiers);
 		getOrCreateMappingRecord(records, msg);
 	}
 
 	/**
 	 * Exception Error Record
 	 */
 	protected void createExceptionMappingRecord(Exception exception,
 			List<MappingRecord> records) {
 		String msg = constructErrorMessage(IMPORT_ERROR.GeneralError, exception);
 		getOrCreateMappingRecord(records, msg);
 	}
 
 	/**
 	 * @param records
 	 * @param msg
 	 */
 	private void getOrCreateMappingRecord(List<MappingRecord> records,
 			String msg) {
 		MappingRecord record;
 		if ((record = this.hasMappingRecord(msg, records)) != null) {
 			long count = record.getCount() + 1;
 			record.setCount(count);
 		} else {
 			record = createMappingRecord(msg.toString());
 			records.add(record);
 		}
 	}
 
 	private String constructErrorMessage(IMPORT_ERROR error, Exception exception) {
 		return constructErrorMessage(error, null, exception);
 	}
 
 	private String constructErrorMessage(IMPORT_ERROR error,
 			List<IComponentLocator.IdentifierDescriptor> allIdentifiers) {
 		return constructErrorMessage(error, allIdentifiers, null);
 	}
 
 	/**
 	 * The data is structured, so it can be processed back by clients.
 	 * 
 	 * "error:[optional error part]" The pattern is optional.
 	 * <ul>
 	 * <li>For {@link IMPORT_ERROR#GeneralError} => the Exception stack trace</li>
 	 * <li>For {@link IMPORT_ERROR#ComponentNotMatched} =>
 	 * column:kind:property:identifier:[pattern]</li>
 	 * <li>For {@link IMPORT_ERROR#ComponentMatchDuplicates} =>
 	 * column:kind:property:identifier:[pattern]</li>
 	 * <li>For {@link IMPORT_ERROR#MetricNotFound} =>
 	 * column:kind:property:identifier:[pattern]</li>
 	 * </ul>
 	 **/
 	private String constructErrorMessage(IMPORT_ERROR error,
 			List<IComponentLocator.IdentifierDescriptor> allIdentifiers,
 			Exception exception) {
 		final StringBuilder sb = new StringBuilder();
 		sb.append(error.ordinal() + ":");
 
 		switch (error) {
 		case GeneralError:
 			if (exception != null) {
 				sb.append(exception.getMessage());
 			}
 			break;
 		case ComponentNotMatched: {
 			constructIdentifierMsg(allIdentifiers, sb);
 		}
 			break;
 		case MetricNotFound: {
 			constructIdentifierMsg(allIdentifiers, sb);
 		}
 			break;
 		case ComponentMatchDuplicates: {
 			constructIdentifierMsg(allIdentifiers, sb);
 		}
 			break;
 		}
 
 		return sb.toString();
 	}
 
 	/**
 	 * @param allIdentifiers
 	 * @param sb
 	 */
 	private void constructIdentifierMsg(
 			List<IComponentLocator.IdentifierDescriptor> allIdentifiers,
 			final StringBuilder sb) {
 		for (final IComponentLocator.IdentifierDescriptor idValue : allIdentifiers) {
 			sb.append(idValue.getColumn() + ":"
 					+ idValue.getKind().getObjectKind().getName() + ":"
 					+ idValue.getObjectProperty() + ":"
 					+ idValue.getIdentifier() + ":" + idValue.getPattern()
 					+ "\n");
 		}
 	}
 
 	protected MappingRecord createMappingRecord(String message) {
 		final MappingRecord record = MetricsFactory.eINSTANCE
 				.createMappingRecord();
 		// record.setColumn(column + "");
 		if (message.length() > 254) {
 			message = message.substring(0, 254);
 		}
 		record.setMessage(message);
 		return record;
 	}
 
 	protected MappingRecord hasMappingRecord(String s,
 			List<MappingRecord> records) {
 		for (MappingRecord mr : records) {
 			if (mr.getMessage().equals(s)) {
 				// This is the same Failure record.
 				return mr;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * A Processor for TimeStamps. It maintains state of a {@link DateFormat}
 	 * and the column(s) where time stamp values can be found.
 	 * 
 	 * @author Christophe Bouhier
 	 * 
 	 */
 	public class TSProcessor {
 
 		private SimpleDateFormat tsParser = null;
 
 		/**
 		 * A Map of TS kind and corresponding column in the input for easy
 		 * retrieval of the value in the processing phase
 		 */
 		private Map<ValueKindType, Integer> tsColumns = Maps.newHashMap();
 
 		/** The current Importer */
 		private IMetricValueImporter currentImporter;
 
 		public TSProcessor(IMetricValueImporter currentImporter) {
 			this.currentImporter = currentImporter;
 		}
 
 		/**
 		 * Get a timestamp using the defined mapping columns.
 		 */
 		public Date getTimeStampValue(int rowNum, boolean reset) {
 
 			if (!reset && headerTimeStamp != null) {
 				return headerTimeStamp;
 			}
 
 			Date returnDate = new Date();
 
 			try {
 				// if we are Excel, get the timestamp directly without a date
 				// formatter. , else process the
 				// provided patterns. Note that the first occurence will return
 				// the date. We don't not allow multiple DATE/TIME combinations
 				// here.
 				if (currentImporter instanceof XLSMetricValuesImporter) {
 
 					for (ValueKindType vkt : tsColumns.keySet()) {
 
 						switch (vkt.getValue()) {
 						case ValueKindType.DATE_VALUE: {
 							return getDateCellValue(rowNum,
 									tsColumns.get(ValueKindType.DATE));
 						}
 						case ValueKindType.TIME_VALUE: {
 							return getDateCellValue(rowNum,
 									tsColumns.get(ValueKindType.TIME));
 						}
 						case ValueKindType.DATETIME_VALUE: {
 							return getDateCellValue(rowNum,
 									tsColumns.get(ValueKindType.DATETIME));
 						}
 						}
 					}
 
 				} else {
 
 					String dateValue = null;
 					String timeValue = null;
 					String dateTimeValue = null;
 
 					for (ValueKindType vkt : tsColumns.keySet()) {
 
 						switch (vkt.getValue()) {
 						case ValueKindType.DATE_VALUE: {
 							dateValue = getStringCellValue(rowNum,
 									tsColumns.get(ValueKindType.DATE));
 						}
 							break;
 						case ValueKindType.TIME_VALUE: {
 							timeValue = getStringCellValue(rowNum,
 									tsColumns.get(ValueKindType.TIME));
 						}
 							break;
 						case ValueKindType.DATETIME_VALUE: {
 							dateTimeValue = getStringCellValue(rowNum,
 									tsColumns.get(ValueKindType.DATETIME));
 						}
 							break;
 						}
 					}
 
 					String value = null;
 
 					if (dateTimeValue != null) {
 						value = dateTimeValue;
 					} else if (dateValue != null && timeValue != null) {
 						value = dateValue + " " + timeValue;
 					} else if (dateValue != null) {
 						value = dateValue;
 					} else if (timeValue != null) {
 						value = timeValue;
 					}
 
 					if (DataActivator.DEBUG) {
 						DataActivator.TRACE.trace(
 								DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 								"Processed timestamp mappings, resolving ");
 						DataActivator.TRACE.trace(
 								DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 								"DATETIME="
 										+ (dateTimeValue == null ? "Not Set"
 												: dateTimeValue));
 						DataActivator.TRACE.trace(
 								DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 								"TIME="
 										+ (timeValue == null ? "Not Set"
 												: timeValue));
 						DataActivator.TRACE.trace(
 								DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 								"DATE="
 										+ (dateValue == null ? "Not Set"
 												: dateValue));
 					}
 
 					returnDate = processTSMapping(tsParser, value);
 				}
 			} catch (ParseException pe) {
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_IMPORT_OPTION,
 							"Error parsing timestamp", pe);
 				}
 				throw new IllegalStateException(pe);
 			} finally {
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_IMPORT_DETAILS_OPTION, "TS is "
 									+ returnDate);
 				}
 			}
 			return returnDate;
 		}
 
 		public void prepTimeStamp(List<MappingColumn> mappingColumns) {
 
 			// Reset before re-processing columns.
 			tsColumns.clear();
 			tsParser = null;
 
 			try {
 
 				// Process either a date Time Pattern , date or time
 				// pattern.
 				// Note: Time and Date is also a valid combination.
 
 				String datePattern = null;
 				String timePattern = null;
 				String dateTimePattern = null;
 
 				for (final MappingColumn column : mappingColumns) {
 					if (column.getDataType() instanceof ValueDataKind) {
 						final ValueDataKind vdk = (ValueDataKind) column
 								.getDataType();
 
 						// Should only accept one single ValueDataKind if
 						// DateTime,
 						// break out if we find one.
 
 						switch (vdk.getValueKind().getValue()) {
 						case ValueKindType.DATE_VALUE: {
 							tsColumns.put(ValueKindType.DATE,
 									column.getColumn());
 							datePattern = vdk.getFormat();
 						}
 							break;
 						case ValueKindType.TIME_VALUE: {
 							tsColumns.put(ValueKindType.TIME,
 									column.getColumn());
 							timePattern = vdk.getFormat();
 						}
 							break;
 						case ValueKindType.DATETIME_VALUE: {
 							tsColumns.put(ValueKindType.DATETIME,
 									column.getColumn());
 							dateTimePattern = vdk.getFormat();
 						}
 							break;
 
 						}
 					}
 				}
 
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 							"Processed timestamp mappings, resolving ");
 
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 							"DATETIME PATTERN="
 									+ (dateTimePattern == null ? "Not Set"
 											: dateTimePattern));
 
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 							"TIME PATTERN="
 									+ (timePattern == null ? "Not Set"
 											: timePattern));
 
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 							"DATE PATTERN="
 									+ (datePattern == null ? "Not Set"
 											: datePattern));
 				}
 
 				tsParser = getTSParser(datePattern, timePattern,
 						dateTimePattern);
 			} finally {
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_IMPORT_DETAILS_OPTION,
 							"parser is " + tsParser + " columns: " + tsColumns);
 				}
 			}
 		}
 
 		/**
 		 * 
 		 * @param dateProcessor
 		 * @param value
 		 * @return
 		 * @throws ParseException
 		 */
 		private Date processTSMapping(SimpleDateFormat dateProcessor,
 				String value) throws ParseException {
 			return dateProcessor.parse(value);
 		}
 
 		/**
 		 * Get a timestamp format/parser for the provided patterns.
 		 * 
 		 * @param datePattern
 		 * @param timePattern
 		 * @param dateTimePattern
 		 * @param dateValue
 		 * @param timeValue
 		 * @param dateTimeValue
 		 * @return
 		 */
 		private SimpleDateFormat getTSParser(String datePattern,
 				String timePattern, String dateTimePattern) {
 
 			String pattern = null;
 
 			if (dateTimePattern != null) {
 				pattern = dateTimePattern;
 			} else if (datePattern != null && timePattern != null) {
 				pattern = datePattern + " " + timePattern;
 			} else if (datePattern != null) {
 				pattern = datePattern;
 			} else if (timePattern != null) {
 				pattern = timePattern;
 			}
 			if (pattern == null) {
 				pattern = ModelUtils.DEFAULT_DATE_TIME_PATTERN;
 			}
 
 			return new SimpleDateFormat(pattern);
 		}
 	}
 
 	protected abstract Date getDateCellValue(int rowNum, int column);
 
 	private int getIntervalHint(int rowNum) {
 		for (final MappingColumn column : getMapping().getDataMappingColumns()) {
 			if (column.getDataType() instanceof ValueDataKind
 					&& ((ValueDataKind) column.getDataType()).getValueKind() == ValueKindType.INTERVAL) {
 				try {
 					return new Double(getNumericCellValue(rowNum,
 							column.getColumn())).intValue();
 				} catch (final Exception e) {
 					throw new IllegalStateException(e);
 				}
 			}
 		}
 		return intervalHint;
 	}
 
 	protected abstract String getStringCellValue(int row, int column);
 
 	protected abstract double getNumericCellValue(int row, int column);
 
 	protected Mapping getMapping() {
 		return metricSource.getMetricMapping();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#getMetricSource
 	 * ()
 	 */
 	public MetricSource getMetricSource() {
 		if (metricSource == null) {
 			metricSource = (MetricSource) getDataProvider().getTransaction()
 					.getObject(cdoID);
 		}
 		return metricSource;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#setMetricSource
 	 * (com.netxforge.netxstudio.metrics.MetricSource)
 	 */
 	public void setMetricSource(MetricSource metricSource) {
 		this.metricSource = metricSource;
 	}
 
 	public static class ImportError extends ImportWarning {
 		private Throwable throwable;
 
 		public Throwable getThrowable() {
 			return throwable;
 		}
 
 		public void setThrowable(Throwable throwable) {
 			this.throwable = throwable;
 		}
 	}
 
 	public static class ImportWarning {
 		private String message;
 		private int row = -1;
 		private int column = -1;
 
 		public int getRow() {
 			return row;
 		}
 
 		public void setRow(int row) {
 			this.row = row;
 		}
 
 		public int getColumn() {
 			return column;
 		}
 
 		public void setColumn(int column) {
 			this.column = column;
 		}
 
 		public String getMessage() {
 			return message;
 		}
 
 		public void setMessage(String message) {
 			this.message = message;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#getWarnings()
 	 */
 	public List<ImportWarning> getWarnings() {
 		return warnings;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#setWarnings
 	 * (java.util.List)
 	 */
 	public void setWarnings(List<ImportWarning> warnings) {
 		this.warnings = warnings;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.data.importer.IMetricValueImporter#
 	 * getComponentLocator()
 	 */
 	public IComponentLocator getComponentLocator() {
 		return componentLocator;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.data.importer.IMetricValueImporter#
 	 * setComponentLocator
 	 * (com.netxforge.netxstudio.data.importer.IComponentLocator)
 	 */
 	public void setComponentLocator(IComponentLocator networkElementLocator) {
 		this.componentLocator = networkElementLocator;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#getFailedRecords
 	 * ()
 	 */
 	public List<MappingRecord> getFailedRecords() {
 		return failedRecords;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#setFailedRecords
 	 * (java.util.List)
 	 */
 	public void setFailedRecords(List<MappingRecord> failedRecords) {
 		this.failedRecords = failedRecords;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#getJobMonitor
 	 * ()
 	 */
 	public IRunMonitor getJobMonitor() {
 		return jobMonitor;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#setJobMonitor
 	 * (com.netxforge.netxstudio.data.job.IRunMonitor)
 	 */
 	public void setJobMonitor(IRunMonitor jobMonitor) {
 		this.jobMonitor = jobMonitor;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.netxforge.netxstudio.data.importer.IMetricValueImporter#
 	 * setMetricSourceWithId(org.eclipse.emf.cdo.common.id.CDOID)
 	 */
 	public void setMetricSourceWithId(CDOID cdoID) {
 		this.cdoID = cdoID;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#getThrowable
 	 * ()
 	 */
 	public Throwable getThrowable() {
 		return throwable;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#setThrowable
 	 * (java.lang.Throwable)
 	 */
 	public void setThrowable(Throwable throwable) {
 		this.throwable = throwable;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#setDataProvider
 	 * (com.netxforge.netxstudio.data.IDataProvider)
 	 */
 	public void setDataProvider(IDataProvider dataProvider) {
 		this.dataProvider = dataProvider;
 	}
 
 	/**
 	 * Delegate to the currently set helper.
 	 */
 	public void addToValueRange(NetXResource foundNetXResource, int intervalHint,
 			KindHintType kindHintType, List<Value> newValues, Date start,
 			Date end) {
 		if (helper != null) {
 			this.helper.addToValueRange(foundNetXResource, intervalHint,
 					kindHintType, newValues, start, end);
 		} else {
 			throw new java.lang.IllegalStateException(
 					"AbstractMetricValueImporter: Import helper should be set");
 		}
 	}
 
 	public int addMetricValue(ValueDataKind vdk, Date timeStamp,
 			Component networkElement, Double dblValue, int intervalHint) {
 		if (helper != null) {
 			return this.helper.addMetricValue(vdk, timeStamp, networkElement,
 					dblValue, intervalHint);
 		} else {
 			throw new java.lang.IllegalStateException(
 					"AbstractMetricValueImporter: Import helper should be set");
 		}
 	}
 
 	/**
 	 * Delegate to the currently set helper.
 	 */
 	public void initializeProviders(IComponentLocator networkElementLocator) {
 		if (helper == null) {
 			throw new java.lang.IllegalStateException(
 					"AbstractMetricValueImporter: Import helper should be set");
 		} else {
 			helper.initializeProviders(networkElementLocator);
 		}
 		// else {
 		// throw new java.lang.IllegalStateException(
 		// "AbstractMetricValueImporter: Import helper should be set");
 		// }
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#getDataProvider
 	 * ()
 	 */
 	public IDataProvider getDataProvider() {
 
 		if (dataProvider == null) {
 			if (helper != null) {
 				dataProvider = helper.getDataProvider();
 			} else {
 				throw new java.lang.IllegalStateException(
 						"AbstractMetricValueImporter: Import helper should be set");
 			}
 		}
 		return dataProvider;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.netxforge.netxstudio.data.importer.IMetricValueImporter#cancelled()
 	 */
 	public boolean cancelled() {
 		if (helper != null) {
 			return helper.cancelled();
 		} else {
 			throw new java.lang.IllegalStateException(
 					"AbstractMetricValueImporter: Import helper should be set");
 		}
 	}
 
 	public Properties properties() {
 		if (helper != null) {
 			return helper.properties();
 		} else {
 			throw new java.lang.IllegalStateException(
 					"AbstractMetricValueImporter: Import helper should be set");
 		}
 	}
 
 	/**
 	 * @return the importMessage
 	 */
 	public String getImportMessage() {
 		return importMessage;
 	}
 
 	/**
 	 * @param importMessage
 	 *            the importMessage to set
 	 */
 	public void setImportMessage(String importMessage) {
 		this.importMessage = importMessage;
 	}
 
 }
