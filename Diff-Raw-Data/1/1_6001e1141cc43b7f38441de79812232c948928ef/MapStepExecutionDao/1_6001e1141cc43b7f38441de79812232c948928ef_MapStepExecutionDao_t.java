 /*
  * Copyright 2006-2007 the original author or authors.
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
 package org.springframework.batch.core.repository.dao;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.batch.core.Entity;
 import org.springframework.batch.core.JobExecution;
 import org.springframework.batch.core.StepExecution;
 import org.springframework.dao.OptimisticLockingFailureException;
 import org.springframework.util.Assert;
 
 /**
  * In-memory implementation of {@link StepExecutionDao}.
  */
 public class MapStepExecutionDao implements StepExecutionDao {
 
 	private static Map<Long, Map<String, StepExecution>> executionsByJobExecutionId = new HashMap<Long, Map<String, StepExecution>>();
 	private static long currentId = 0;
 
 	public static void clear() {
 		executionsByJobExecutionId.clear();
 	}
 
 	public void saveStepExecution(StepExecution stepExecution) {
 		Assert.isTrue(stepExecution.getId() == null);
 		Assert.isTrue(stepExecution.getVersion() == null);
 		Assert.notNull(stepExecution.getJobExecutionId(), "JobExecution must be saved already.");
 
 		Map<String, StepExecution> executions = executionsByJobExecutionId.get(stepExecution.getJobExecutionId());
 		if (executions == null) {
 			executions = new HashMap<String, StepExecution>();
 			executionsByJobExecutionId.put(stepExecution.getJobExecutionId(), executions);
 		}
 		stepExecution.setId(new Long(currentId++));
 		stepExecution.incrementVersion();
 		executions.put(stepExecution.getStepName(), stepExecution);
 	}
 
 	public void updateStepExecution(StepExecution stepExecution) {
 
 		Assert.notNull(stepExecution.getJobExecutionId());
 
 		Map<String, StepExecution> executions = executionsByJobExecutionId.get(stepExecution.getJobExecutionId());
 		Assert.notNull(executions, "step executions for given job execution are expected to be already saved");
 
 		StepExecution persistedExecution = (StepExecution) executions.get(stepExecution.getStepName());
 		Assert.notNull(persistedExecution, "step execution is expected to be already saved");
 
 		synchronized (stepExecution) {
 			if (!persistedExecution.getVersion().equals(stepExecution.getVersion())) {
 				throw new OptimisticLockingFailureException("Attempt to update step execution id=" + stepExecution.getId() + " with wrong version (" + stepExecution.getVersion() + "), where current version is " + persistedExecution.getVersion());
 			}
 
 			stepExecution.incrementVersion();
 			executions.put(stepExecution.getStepName(), stepExecution);
 		}
 	}
 
 	public StepExecution getStepExecution(JobExecution jobExecution, String stepName) {
 		Map<String, StepExecution> executions = executionsByJobExecutionId.get(jobExecution.getId());
 		if (executions == null) {
 			return null;
 		}
 
 		return (StepExecution) executions.get(stepName);
 	}
 
 	public List<StepExecution> getStepExecutions(JobExecution jobExecution) {
 		Map<String, StepExecution> executions = executionsByJobExecutionId.get(jobExecution.getId());
 		List<StepExecution> result = new ArrayList<StepExecution>(executions.values());
 		Collections.sort(result, new Comparator<Entity>() {
 
 			public int compare(Entity o1, Entity o2) {
 				return Long.signum(o2.getId() - o1.getId());
 			}
 		});
 		return result;
 	}
 }
