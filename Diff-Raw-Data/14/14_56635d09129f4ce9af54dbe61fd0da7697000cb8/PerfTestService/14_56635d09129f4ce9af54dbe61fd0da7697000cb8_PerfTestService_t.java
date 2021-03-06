 /*
  * Copyright (C) 2012 - 2012 NHN Corporation
  * All rights reserved.
  *
  * This file is part of The nGrinder software distribution. Refer to
  * the file LICENSE which is part of The nGrinder distribution for
  * licensing details. The nGrinder distribution is available on the
  * Internet at http://nhnopensource.org/ngrinder
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.ngrinder.perftest.service;
 
 import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
 import static org.ngrinder.common.util.Preconditions.checkNotNull;
 import static org.ngrinder.common.util.Preconditions.checkNotZero;
 import static org.ngrinder.perftest.model.Status.getProcessingOrTestingTestStatus;
 import static org.ngrinder.perftest.repository.PerfTestSpecification.createdBy;
 import static org.ngrinder.perftest.repository.PerfTestSpecification.emptyPredicate;
 import static org.ngrinder.perftest.repository.PerfTestSpecification.idSetEqual;
 import static org.ngrinder.perftest.repository.PerfTestSpecification.likeTestNameOrDescription;
 import static org.ngrinder.perftest.repository.PerfTestSpecification.statusSetEqual;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.grinder.SingleConsole;
 import net.grinder.SingleConsole.StopReason;
 import net.grinder.common.GrinderProperties;
 import net.grinder.console.model.ConsoleProperties;
 import net.grinder.util.ConsolePropertiesFactory;
 import net.grinder.util.Directory;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.ngrinder.common.constant.NGrinderConstants;
 import org.ngrinder.common.exception.NGrinderRuntimeException;
 import org.ngrinder.infra.config.Config;
 import org.ngrinder.model.Role;
 import org.ngrinder.model.User;
 import org.ngrinder.perftest.model.PerfTest;
 import org.ngrinder.perftest.model.ProcessAndThread;
 import org.ngrinder.perftest.model.Status;
 import org.ngrinder.perftest.repository.PerfTestRepository;
 import org.ngrinder.script.model.FileEntry;
 import org.ngrinder.script.model.FileType;
 import org.ngrinder.script.service.FileEntryService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.jpa.domain.Specifications;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * {@link PerfTest} Service Class.
  * 
  * This class contains various method which mainly get the {@link PerfTest} matching specific
  * conditions.
  * 
  * @author Mavlarn
  * @author JunHo Yoon
  * @since 3.0
  */
 @Service
 public class PerfTestService implements NGrinderConstants {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(PerfTestService.class);
 
 	private static final String DATA_FILE_EXTENSION = ".data";
 
 	@Autowired
 	private PerfTestRepository perfTestRepository;
 
 	@Autowired
 	private ConsoleManager consoleManager;
 
 	@Autowired
 	private Config config;
 
 	@Autowired
 	private FileEntryService fileEntryService;
 
 	private NumberFormat formatter = new DecimalFormat("###.###");
 
 	private int MaximumConcurrentTestCount = 0;
 
 	/**
 	 * Get {@link PerfTest} list on the user.
 	 * 
 	 * @param user
 	 *            user
 	 * @param query
 	 * @param isFinished
 	 *            only find finished test
 	 * @param pageable
 	 *            paging info
 	 * @return found {@link PerfTest} list
 	 */
 	public Page<PerfTest> getPerfTestList(User user, String query, boolean isFinished, Pageable pageable) {
 
 		Specifications<PerfTest> spec = Specifications.where(emptyPredicate());
 
 		// User can see only his own test
 		if (user.getRole().equals(Role.USER)) {
 			spec = spec.and(createdBy(user));
 		}
 
 		if (isFinished) {
 			spec = spec.and(statusSetEqual(Status.FINISHED));
 		}
 		if (StringUtils.isNotBlank(query)) {
 			spec = spec.and(likeTestNameOrDescription(query));
 		}
 		return perfTestRepository.findAll(spec, pageable);
 	}
 
 	public List<PerfTest> getPerfTest(User user, Integer[] ids) {
 
 		Specifications<PerfTest> spec = Specifications.where(emptyPredicate());
 
 		// User can see only his own test
 		if (user.getRole().equals(Role.USER)) {
 			spec = spec.and(createdBy(user));
 		}
 
 		spec = spec.and(idSetEqual(ids));
 		return perfTestRepository.findAll(spec);
 	}
 
 	/**
 	 * Get PerfTest count which have given status.
 	 * 
 	 * @param user
 	 *            user who created test. null to retrieve all
 	 * @param statuses
 	 *            status set
 	 * @return the count
 	 */
 	public long getPerfTestCount(User user, Status... statuses) {
 		Specifications<PerfTest> spec = Specifications.where(emptyPredicate());
 
 		// User can see only his own test
 		if (user != null) {
 			spec = spec.and(createdBy(user));
 		}
 
 		if (statuses.length != 0) {
 			spec = spec.and(statusSetEqual(statuses));
 		}
 
 		return perfTestRepository.count(spec);
 	}
 
 	/**
 	 * Get {@link PerfTest} list which have give state.
 	 * 
 	 * @param user
 	 *            user who created {@link PerfTest}. if null, retrieve all test
 	 * @param statuses
 	 *            set of {@link Status}
 	 * @return found {@link PerfTest} list.
 	 */
 	public List<PerfTest> getPerfTest(User user, Status... statuses) {
 		Specifications<PerfTest> spec = Specifications.where(emptyPredicate());
 
 		// User can see only his own test
 		if (user != null) {
 			spec = spec.and(createdBy(user));
 		}
 		if (statuses.length != 0) {
 			spec = spec.and(statusSetEqual(statuses));
 		}
 
 		return perfTestRepository.findAll(spec);
 	}
 
 	/**
 	 * Save {@link PerfTest}.
 	 * 
 	 * @param perfTest
 	 *            {@link PerfTest} instance to be saved.
 	 * @return Saved {@link PerfTest}
 	 */
 	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
	@Transactional
 	public PerfTest savePerfTest(PerfTest perfTest) {
 		checkNotNull(perfTest);
 		// Merge if necessary
 		if (perfTest.exist()) {
 			PerfTest existingPerfTest = perfTestRepository.findOne(perfTest.getId());
 			perfTest = existingPerfTest.merge(perfTest);
 		}
 		return perfTestRepository.save(perfTest);
 	}
 
 	/**
 	 * Save performance test with given status.
 	 * 
 	 * This method is only used for changing {@link Status}
 	 * 
 	 * @param perfTest
 	 *            {@link PerfTest} instance which will be saved.
 	 * @param status
 	 *            Status to be assigned
 	 * @return saved {@link PerfTest}
 	 */
 	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
	@Transactional
 	public PerfTest setRecodingStarting(PerfTest perfTest, long systemTimeMills) {
 		checkNotNull(perfTest);
 		checkNotNull(perfTest.getId(), "perfTest with status should save Id");
 		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
 		if (findOne == null) {
 			return null;
 		}
 		findOne.setStartTime(new Date(systemTimeMills));
 		return perfTestRepository.save(findOne);
 	}
 
 	/**
 	 * Mark test error on {@link PerfTest} instance
 	 * 
 	 * @param perfTest
 	 *            {@link PerfTest}
 	 * @param singleConsole
 	 *            console in use
 	 * @param e
 	 *            exception occurs.
 	 */
 	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
 	public void markAbromalTermination(PerfTest perfTest, StopReason reason) {
 		// Leave last status as test error cause
 		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
 		if (findOne == null) {
 			return;
 		}
 
 		findOne.setTestErrorCause(perfTest.getStatus());
 		findOne.setLastProgressMessage(reason.name());
 		findOne.setStatus(Status.ABNORMAL_TESTING);
 	}
 
 	/**
 	 * Save performance test with given status.
 	 * 
 	 * This method is only used for changing {@link Status}
 	 * 
 	 * @param perfTest
 	 *            {@link PerfTest} instance which will be saved.
 	 * @param status
 	 *            Status to be assigned
 	 * @return saved {@link PerfTest}
 	 */
 	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
 	@Transactional
 	public PerfTest changePerfTestStatus(PerfTest perfTest, Status status) {
 		checkNotNull(perfTest);
 		checkNotNull(perfTest.getId(), "perfTest with status should save Id");
 		perfTest.setStatus(checkNotNull(status, "status should not be null"));
 		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
 		if (findOne == null) {
 			return null;
 		}
 		findOne.setStatus(status);
 		return perfTestRepository.save(findOne);
 	}
 
 	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
 	@Transactional
 	public void markProgress(PerfTest perfTest, String message) {
 		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
 		if (findOne == null) {
 			return;
 		}
 		findOne.setLastProgressMessage(message);
 		perfTestRepository.save(findOne);
 	}
 
 	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
 	@Transactional
 	public void markProgressAndStatus(PerfTest perfTest, Status status, String message) {
 		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
 		if (findOne == null) {
 			return;
 		}
 		findOne.setStatus(status);
 		findOne.setLastProgressMessage(message);
 		perfTestRepository.save(findOne);
 	}
 
 	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
 	@Transactional
 	public void markProgressAndStatusAndFinishTimeAndStatistics(PerfTest perfTest, Status status,
 					String message) {
 		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
 		if (findOne == null) {
 			return;
 		}
 		findOne.setStatus(status);
 		findOne.setLastProgressMessage(message);
 		findOne.setFinishTime(new Date());
 		updatePerfTestAfterTestFinish(findOne);
 		perfTestRepository.save(findOne);
 	}
 
 	/**
 	 * Mark test error on {@link PerfTest} instance
 	 * 
 	 * @param perfTest
 	 *            {@link PerfTest}
 	 * @param reason
 	 *            error reason
 	 */
 	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
 	@Transactional
 	public void markPerfTestError(PerfTest perfTest, String message) {
 		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
 		if (findOne == null) {
 			return;
 		}
 		findOne.setStatus(Status.ABNORMAL_TESTING);
 		findOne.setTestErrorCause(findOne.getStatus());
 		findOne.setLastProgressMessage(message);
 		perfTestRepository.save(findOne);
 	}
 
 	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
 	@Transactional
 	public void markPerfTestConsoleStart(PerfTest perfTest, int consolePort, Integer testTrialCount) {
 		PerfTest findOne = perfTestRepository.findOne(perfTest.getId());
 		if (findOne == null) {
 			return;
 		}
 		findOne.setPort(consolePort);
 		findOne.setTestTrialCount(++testTrialCount);
 		findOne.setStatus(Status.START_CONSOLE_FINISHED);
 		perfTestRepository.save(findOne);
 	}
 
 	/**
 	 * Get PerfTest by testId.
 	 * 
 	 * @param testId
 	 *            PerfTest id
 	 * @return found {@link PerfTest}, null otherwise
 	 */
 	@Cacheable(value = "perftest")
 	public PerfTest getPerfTest(long testId) {
 		return perfTestRepository.findOne(testId);
 	}
 
 	/**
 	 * Get next runnable PerfTest list.
 	 * 
 	 * @return found {@link PerfTest}, null otherwise
 	 */
 	@Cacheable(value = "perftest")
 	@Transactional
 	public PerfTest getPerfTestCandiate() {
 		List<PerfTest> readyPerfTests = perfTestRepository
 						.findAllByStatusOrderByScheduledTimeAsc(Status.READY);
 		List<PerfTest> usersFirstPerfTests = filterCurrentlyRunningTestUsersTest(readyPerfTests);
 		return usersFirstPerfTests.isEmpty() ? null : readyPerfTests.get(0);
 	}
 
 	/**
 	 * Get currently running {@link PerfTest} list.
 	 * 
 	 * @return
 	 */
 	public List<PerfTest> getCurrentlyRunningTest() {
 		return getPerfTest(null, Status.getProcessingOrTestingTestStatus());
 	}
 
 	/**
 	 * Filter out {@link PerfTest} whose owner is running another test now..
 	 * 
 	 * @param perfTestLists
 	 *            perf test
 	 * @return filtered perf test
 	 */
 	private List<PerfTest> filterCurrentlyRunningTestUsersTest(List<PerfTest> perfTestLists) {
 		List<PerfTest> currentlyRunningTests = getCurrentlyRunningTest();
 		final Set<User> currentlyRunningTestOwners = new HashSet<User>();
 		for (PerfTest each : currentlyRunningTests) {
 			currentlyRunningTestOwners.add((User) ObjectUtils.defaultIfNull(each.getLastModifiedUser(),
 							each.getCreatedUser()));
 		}
 		CollectionUtils.filter(perfTestLists, new Predicate() {
 			@Override
 			public boolean evaluate(Object object) {
 				PerfTest perfTest = (PerfTest) object;
 				return !currentlyRunningTestOwners.contains(ObjectUtils.defaultIfNull(
 								perfTest.getLastModifiedUser(), perfTest.getCreatedUser()));
 			}
 		});
 		return perfTestLists;
 	}
 
 	/**
 	 * Get currently testing PerfTest.
 	 * 
 	 * @return found {@link PerfTest} list
 	 */
 	@Cacheable(value = "perftestlist")
 	public List<PerfTest> getTestingPerfTest() {
 		return getPerfTest(null, Status.getTestingTestStates());
 	}
 
 	/**
 	 * Get abnormally testing PerfTest.
 	 * 
 	 * @return found {@link PerfTest} list
 	 */
 	public List<PerfTest> getAbnoramlTestingPerfTest() {
 		return getPerfTest(null, Status.ABNORMAL_TESTING);
 	}
 
 	/**
 	 * Delete PerfTest by id.
 	 * 
 	 * Never use this method in runtime. This method is used only for testing.
 	 * 
 	 * @param id
 	 *            {@link PerfTest} it
 	 */
 	@CacheEvict(value = { "perftest", "perftestlist" }, allEntries = true)
 	public void deletePerfTest(long id) {
 		perfTestRepository.delete(id);
 	}
 
 	/**
 	 * Get PerfTest Directory in which the distributed file is stored.
 	 * 
 	 * @param perfTest
 	 *            pefTest from which distribution dire.ctory calculated
 	 * @return path on in files are saved.
 	 */
 	public File getPerfTestFilePath(PerfTest perfTest) {
 		return config.getHome().getPerfTestDirectory(
 						checkNotZero(perfTest.getId(), "perftest id should not be 0 or zero").toString());
 	}
 
 	/**
 	 * Create {@link GrinderProperties} based on the passed {@link PerfTest}
 	 * 
 	 * @param perfTest
 	 *            base data
 	 * @return created {@link GrinderProperties} instance
 	 */
 	public GrinderProperties getGrinderProperties(PerfTest perfTest) {
 		try {
 			// Copy grinder properties
 			File userGrinderPropertiesPath = new File(getPerfTestDirectory(perfTest),
 							DEFAULT_GRINDER_PROPERTIES_PATH);
 			FileUtils.copyFile(config.getHome().getDefaultGrinderProperties(), userGrinderPropertiesPath);
 			GrinderProperties grinderProperties = new GrinderProperties(userGrinderPropertiesPath);
 			grinderProperties.setAssociatedFile(new File(userGrinderPropertiesPath.getName()));
 			grinderProperties.setProperty(GrinderProperties.SCRIPT,
 							FilenameUtils.getName(checkNotEmpty(perfTest.getScriptName())));
 			ProcessAndThread calcProcessAndThread = calcProcessAndThread(checkNotZero(
 							perfTest.getVuserPerAgent(), "vuser count should be provided"));
 			grinderProperties.setInt(GRINDER_PROP_THREAD, calcProcessAndThread.getThreadCount());
 			grinderProperties.setInt(GRINDER_PROP_PROCESSES, calcProcessAndThread.getProcessCount());
 			if ("D".equals(perfTest.getThreshold())) {
 				grinderProperties.setLong(GRINDER_PROP_DURATION, perfTest.getDuration());
 			} else {
 				grinderProperties.setInt(GRINDER_PROP_RUNS, perfTest.getRunCount());
 			}
 			grinderProperties.setProperty(NGRINDER_PROP_ETC_HOSTS,
 							StringUtils.defaultIfBlank(perfTest.getTargetHosts(), ""));
 			grinderProperties.setBoolean(GRINDER_PROP_USE_CONSOLE, true);
 			grinderProperties.setInt(GRINDER_PROP_INITIAL_SLEEP_TIME, perfTest.getInitSleepTime());
 			grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT, perfTest.getProcessIncrement());
 			grinderProperties.setInt(GRINDER_PROP_PROCESS_INCREMENT_INTERVAL,
 							perfTest.getProcessIncrementInterval());
 			return grinderProperties;
 		} catch (Exception e) {
 			throw new NGrinderRuntimeException("error while prepare grinder property for "
 							+ perfTest.getTestName(), e);
 		}
 	}
 
 	/**
 	 * Prepare files for distribution.
 	 * 
 	 * @param perfTest
 	 *            perfTest
 	 * @return File location in which the perftest should have.
 	 */
 	public File prepareDistribution(PerfTest perfTest) {
 		checkNotNull(perfTest.getId(), "perfTest should have id");
 		String scriptName = checkNotEmpty(perfTest.getScriptName(), "perfTest should have script name");
 		User user = perfTest.getCreatedUser();
 
 		// Get all files in the script path
 		FileEntry scriptEntry = fileEntryService.getFileEntry(user, perfTest.getScriptName());
 		List<FileEntry> fileEntries = fileEntryService.getLibAndResourcesEntries(user,
 						checkNotEmpty(scriptName));
 		File perfTestDirectory = getPerfTestDirectory(perfTest);
 		fileEntries.add(scriptEntry);
 
 		perfTestDirectory.mkdirs();
 
 		// Distribute each files in that folder.
 		for (FileEntry each : fileEntries) {
 			// Directory is not subject to be distributed.
 			if (each.getFileType() == FileType.DIR) {
 				continue;
 			}
 			LOGGER.info(each.getPath() + " is being written in " + perfTestDirectory);
 			fileEntryService.writeContentTo(user, each.getPath(), perfTestDirectory);
 		}
 		LOGGER.info("File write is completed in " + perfTestDirectory);
 		return perfTestDirectory;
 	}
 
 	/**
 	 * Get perf test base directory
 	 * 
 	 * @param perfTest
 	 *            perfTest
 	 * @return prefTest base path
 	 */
 	public File getPerfTestBaseDirectory(PerfTest perfTest) {
 		return config.getHome().getPerfTestDirectory(perfTest.getId().toString());
 	}
 
 	/**
 	 * Get user perf test directory fot
 	 * 
 	 * @param perfTest
 	 * @param subDir
 	 * @return
 	 */
 	public File getPerfTestDirectory(PerfTest perfTest) {
 		return new File(getPerfTestBaseDirectory(perfTest), NGrinderConstants.PATH_DIST);
 	}
 
 	/**
 	 * Get the optimal process and thread count.
 	 * 
 	 * FIXME : This method should be optimized more.
 	 * 
 	 * @param newVuser
 	 *            the count of virtual users per agent
 	 * @return optimal process thread count
 	 */
 	public ProcessAndThread calcProcessAndThread(int newVuser) {
 		int threadCount = 2;
 		int processCount = newVuser / threadCount + newVuser % threadCount;
 		return new ProcessAndThread(processCount, threadCount);
 	}
 
 	/**
 	 * get report data by test id, data type, and image width
 	 * 
 	 * @param testId
 	 *            test id
 	 * @param dataType
 	 *            data type
 	 * @param imgWidth
 	 *            image width
 	 * @return report data
 	 * @throws IOException
 	 */
 	public List<Object> getReportData(long testId, String dataType, int imgWidth) throws IOException {
 		// TODO: later, we can make the file content as the string of list, then
 		// we can
 		// just return the file content directly, it will be much faster.
 		List<Object> reportData = new ArrayList<Object>();
 		File reportFolder = config.getHome().getPerfTestDirectory(
 						testId + File.separator + NGrinderConstants.PATH_REPORT);
 		if (imgWidth < 100) {
 			imgWidth = 100;
 		}
 		int pointCount = imgWidth / 10;
 		int lineNumber;
 		File targetFile = null;
 		targetFile = new File(reportFolder, dataType.toLowerCase() + DATA_FILE_EXTENSION);
 		if (!targetFile.exists()) {
 			return reportData;
 		}
 		LineNumberReader lnr = null;
 		try {
 			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(targetFile)));
 			lnr.skip(targetFile.length());
 			lineNumber = lnr.getLineNumber() + 1;
 
 		} finally {
 			IOUtils.closeQuietly(lnr);
 		}
 		FileReader reader = null;
 		BufferedReader br = null;
 		try {
 			reader = new FileReader(targetFile);
 			br = new BufferedReader(reader);
 			String data = null;
 			int current = 0;
 			int interval = lineNumber / pointCount;
 			// TODO should get average data
 			// FIXME : NEVER NEVER DO IT. Be aware of memory size.!!
 			while (StringUtils.isNotBlank(data = br.readLine())) {
 				if (0 == current) {
 					double number = NumberUtils.createDouble(data);
 					reportData.add(number);
 				}
 				if (++current >= interval) {
 					current = 0;
 				}
 			}
 		} finally {
 			IOUtils.closeQuietly(reader);
 			IOUtils.closeQuietly(br);
 		}
 
 		return reportData;
 	}
 
 	/**
 	 * Get report file name for give test id.
 	 * 
 	 * @param testId
 	 * @return report file path
 	 */
 	public File getReportFile(long testId) {
 		return new File(getReportFileDirectory(testId), NGrinderConstants.REPORT_CSV);
 	}
 
 	/**
 	 * Get report file directory for give test id.
 	 * 
 	 * @param testId
 	 * @return report file path
 	 */
 
 	public File getReportFileDirectory(long testId) {
 		return new File(config.getHome().getPerfTestDirectory(String.valueOf(testId)),
 						NGrinderConstants.PATH_REPORT);
 	}
 
 	/**
 	 * To get statistics data when test is running If the console is not available.. it returns
 	 * empty map.
 	 */
 	public Map<String, Object> getStatistics(int port) {
 		SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(port);
 		if (consoleUsingPort == null) {
 			LOGGER.warn("console using {} port is not available", port);
 			// return null if there is no console. We will check null after calling this method.
 			return null;
 		}
 		return consoleUsingPort.getStatictisData();
 	}
 
 	/**
 	 * Get all perf test list.
 	 * 
 	 * Note : This is only for test
 	 * 
 	 * @return all {@link PerfTest} list
 	 * 
 	 */
 	public List<PerfTest> getAllPerfTest() {
 		return perfTestRepository.findAll();
 	}
 
 	/**
 	 * Create {@link ConsoleProperties} based on given {@link PerfTest} instance.
 	 * 
 	 * @param perfTest
 	 *            perfTest
 	 * @return {@link ConsoleProperties}
 	 */
 	public ConsoleProperties createConsoleProperties(PerfTest perfTest) {
 		ConsoleProperties consoleProperties = ConsolePropertiesFactory.createEmptyConsoleProperties();
 		try {
 			consoleProperties.setAndSaveDistributionDirectory(new Directory(getPerfTestDirectory(perfTest)));
 		} catch (Exception e) {
 			throw new NGrinderRuntimeException("Error while setting console properties", e);
 		}
 		return consoleProperties;
 	}
 
 	/**
 	 * Update the given {@link PerfTest} properties after test finished.
 	 * 
 	 * @param perfTest
 	 *            perfTest
 	 */
 	public void updatePerfTestAfterTestFinish(PerfTest perfTest) {
 		checkNotNull(perfTest);
 		int port = perfTest.getPort();
 		Map<String, Object> result = getStatistics(port);
 		if (result == null) {
 			return;
 		}
 		@SuppressWarnings("unchecked")
 		Map<String, Object> totalStatistics = (Map<String, Object>) result.get("totalStatistics");
 		perfTest.setErrors((int) ((Double) totalStatistics.get("Errors")).doubleValue());
 		perfTest.setTps(Double.parseDouble(formatter.format(totalStatistics.get("TPS"))));
 		perfTest.setMeanTestTime(Double.parseDouble(formatter.format(ObjectUtils.defaultIfNull(
 						totalStatistics.get("Mean_Test_Time_(ms)"), 0D))));
 		perfTest.setPeakTps(Double.parseDouble(formatter.format(ObjectUtils.defaultIfNull(
 						totalStatistics.get("Peak_TPS"), 0D))));
 		perfTest.setTests((int) ((Double) totalStatistics.get("Tests")).doubleValue());
 	}
 
 	/**
 	 * Get maximum concurrent test count.
 	 * 
 	 * @return maximum concurrent test
 	 */
 	public int getMaximumConcurrentTestCount() {
 		if (MaximumConcurrentTestCount == 0) {
 			MaximumConcurrentTestCount = config.getSystemProperties().getPropertyInt(
 							NGrinderConstants.NGRINDER_PROP_MAX_CONCURRENT_TEST,
 							NGrinderConstants.NGRINDER_PROP_MAX_CONCURRENT_TEST_VALUE);
 		}
 		return MaximumConcurrentTestCount;
 	}
 
 	/**
 	 * Check the test can be executed more.
 	 * 
 	 * @return true if possible
 	 */
 	public boolean canExecuteTestMore() {
 		return getPerfTestCount(null, Status.getProcessingOrTestingTestStatus()) < getMaximumConcurrentTestCount();
 	}
 
 	@Transactional
 	public void stopPerfTest(User user, Long id) {
 		PerfTest perfTest = getPerfTest(id);
 		if (user.getRole() != Role.ADMIN && !perfTest.getLastModifiedUser().equals(user)) {
 			return;
 		}
 		perfTest.setStopRequest(true);
 		SingleConsole consoleUsingPort = consoleManager.getConsoleUsingPort(perfTest.getPort());
 		consoleUsingPort.cancel();
 		perfTestRepository.save(perfTest);
 	}
 
 	/**
 	 * Return stop requested test
 	 * 
 	 * @return stop requested perf test
 	 */
 	public List<PerfTest> getStopRequestedPerfTest() {
 		final List<PerfTest> perfTests = getPerfTest(null, getProcessingOrTestingTestStatus());
 		CollectionUtils.filter(perfTests, new Predicate() {
 			@Override
 			public boolean evaluate(Object object) {
 				return (((PerfTest) object).getStopRequest() == Boolean.TRUE);
 			}
 		});
 		return perfTests;
 	}
 
 }
