 /**
  * Copyright (C) 2012 Philip W. Sorst <philip@sorst.net>
  * and individual contributors as indicated
  * by the @authors tag.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.dontdrinkandroot.utils.jobmanager;
 
 import net.dontdrinkandroot.utils.Percent;
import net.dontdrinkandroot.utils.progressmonitor.ProgressMonitor;
 import net.dontdrinkandroot.utils.progressmonitor.ProgressStatus;
import net.dontdrinkandroot.utils.progressmonitor.impl.SimpleProgressMonitor;
 
 
 public class TestJob extends AbstractJob<Integer> {
 
 	private final Integer i;
 
	private final ProgressMonitor monitor = new SimpleProgressMonitor();
 
 
 	public TestJob(final Integer i) {
 
 		super("testjob." + i);
 
 		this.i = i;
 	}
 
 
 	@Override
 	protected Integer doRun() {
 
 		try {
 			this.monitor.setMessage("Preparing");
 			Thread.sleep(100L);
 			this.monitor.setMessage("Done");
 			this.monitor.setProgress(Percent.HUNDRED);
 		} catch (final InterruptedException e) {
 			// throw new RuntimeException(e);
 		}
 
 		return this.i * 10;
 	}
 
 
 	@Override
 	public int hashCode() {
 
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (this.i == null ? 0 : this.i.hashCode());
 
 		return result;
 	}
 
 
 	@Override
 	public boolean equals(final Object obj) {
 
 		if (this == obj) {
 			return true;
 		}
 
 		if (obj == null) {
 			return false;
 		}
 
 		if (this.getClass() != obj.getClass()) {
 			return false;
 		}
 
 		final TestJob other = (TestJob) obj;
 		if (this.i == null) {
 			if (other.i != null) {
 				return false;
 			}
 		} else if (!this.i.equals(other.i)) {
 			return false;
 		}
 
 		return true;
 	}
 
 
	@Override
 	public ProgressStatus getProgressStatus() {
 
 		return this.monitor.getProgressStatus();
 	}
 
 }
