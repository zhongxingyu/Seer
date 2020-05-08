 /*******************************************************************************
  * Copyright (c) 3 jul. 2013 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.common.model;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.notify.AdapterFactory;
 
 import com.google.common.collect.Lists;
 import com.netxforge.netxstudio.common.context.IComputationContext;
 import com.netxforge.netxstudio.generics.DateTimeRange;
 
 /**
  * A Composite summary
  * 
  * @author Christophe Bouhier
  */
 public class CompositeSummary implements IMonitoringSummary {
 
 	ComputationContextProvider contextProvider = new ComputationContextProvider();
 
 	private List<IMonitoringSummary> summaries = Lists.newArrayList();
 
	public void addSummary(IMonitoringSummary summary) {
		this.summaries.add(summary);
	}

	public List<IMonitoringSummary> getSummaries() {
		return summaries;
	}

 	public int totalRag(RAG status) {
 		int totalRag = 0;
 		for (IMonitoringSummary s : summaries) {
 			totalRag += s.totalRag(status);
 		}
 		return totalRag;
 	}
 
 	public int[] rag() {
 		int[] rag = new int[] { 0, 0, 0 };
 		for (IMonitoringSummary s : summaries) {
 			rag[RAG.RED.ordinal()] += s.rag()[RAG.RED.ordinal()];
 			rag[RAG.AMBER.ordinal()] += s.rag()[RAG.AMBER.ordinal()];
 			rag[RAG.GREEN.ordinal()] += s.rag()[RAG.GREEN.ordinal()];
 		}
 		return rag;
 	}
 
 	public void addContextObject(IComputationContext object) {
 		contextProvider.addContextObject(object);
 
 	}
 
 	public void addContextObjects(IComputationContext... objects) {
 		contextProvider.addContextObjects(objects);
 
 	}
 
 	public void clearContextObject() {
 		contextProvider.clearContextObject();
 
 	}
 
 	public IComputationContext[] getContextObjects() {
 		return contextProvider.getContextObjects();
 	}
 
 	public List<IComputationContext> getContextObjectsAsList() {
 		return null;
 	}
 
 	public void compute(IProgressMonitor monitor) {
 		throw new UnsupportedOperationException(
 				"Composite computations are not permitted");
 	}
 
 	public boolean isComputed() {
 		return false;
 	}
 
 	public void setSelfAdaptFactory(AdapterFactory adapterFactory) {
 		throw new UnsupportedOperationException(
 				"Composite self-adaptation is not permitted");
 	}
 
 	public String getPeriodFormattedString() {
 		throw new UnsupportedOperationException("TODO");
 	}
 
 	public DateTimeRange getPeriod() {
 		throw new UnsupportedOperationException("TODO");
 	}
 
 }
