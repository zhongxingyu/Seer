 /*
  * #%L
  * Bitrepository Integrity Service
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 
 package org.bitrepository.integrityservice.workflow;
 
 import org.bitrepository.service.workflow.JobID;
 import org.bitrepository.service.workflow.Workflow;
 import org.bitrepository.service.workflow.WorkflowContext;
 
 public class TestWorkflow extends Workflow {
     private JobID jobID;
 
     public TestWorkflow() {}
 
     public TestWorkflow(String collectionID) {
         initialise(null, collectionID);
     }
 
     @Override
     public String getDescription() {
         return "Stubbed workflow used for testing";
     }
 
     @Override
     public JobID getJobID() {
         return jobID;
     }
 
     @Override
     public void initialise(WorkflowContext context, String collectionID) {
        jobID = new JobID(getClass().getSimpleName(), collectionID);
     }
 
     @Override
     public String toString() {
         return "TestWorkflow{" +
                 "jobID=" + jobID +
                 '}';
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof TestWorkflow)) return false;
 
         TestWorkflow that = (TestWorkflow) o;
 
         if (!jobID.equals(that.jobID)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return jobID.hashCode();
     }
 }
