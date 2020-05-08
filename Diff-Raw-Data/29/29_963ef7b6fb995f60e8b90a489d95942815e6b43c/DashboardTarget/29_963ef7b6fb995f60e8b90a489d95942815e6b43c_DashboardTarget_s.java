 package org.chai.kevin.dashboard;
 
 /* 
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 import javax.persistence.Basic;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.chai.kevin.Exportable;
 import org.chai.kevin.Period;
 import org.chai.kevin.data.Calculation;
 import org.chai.kevin.location.CalculationLocation;
 import org.chai.kevin.reports.AbstractReportTarget;
 import org.chai.kevin.reports.ReportProgram;
 import org.chai.kevin.reports.ReportTarget;
 import org.chai.kevin.util.Utils;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 @Entity(name="DashboardTarget")
 @Table(name="dhsst_dashboard_target")
 @Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
 public class DashboardTarget extends AbstractReportTarget implements DashboardEntity, ReportTarget, Exportable {
 
 	private ReportProgram program;
 	protected Integer weight;
 	
 	@Transient
 	public Calculation<?> getCalculation() {
		return (Calculation<?>)getData();
 	}
 
 	@ManyToOne(targetEntity=ReportProgram.class)
 	public ReportProgram getProgram() {
 		return program;
 	}
 	
 	public void setProgram(ReportProgram program) {
 		this.program = program;
 	}
 	
 	@Override
 	public <T> T visit(DashboardVisitor<T> visitor, CalculationLocation location, Period period) {
 		return visitor.visitTarget(this, location, period);
 	}
 	
 	@Override
 	@Transient
 	public boolean hasChildren() {	
 		return false;
 	}
 	
 	@Override
 	@Transient
 	public boolean isTarget() {
 		return true;
 	}
 	
 	@Override
 	@Transient
 	public ReportProgram getReportProgram() {
 		ReportProgram reportProgram = getProgram();
 		return reportProgram;
 	}
 	
 	@Override
 	public String toString() {
 		return "DashboardTarget[getId()=" + getId() + ", getCode()="
 				+ getCode() + "]";
 	}
 	
 	@Override
 	public String toExportString() {
 		return "[" + Utils.formatExportCode(getCode()) + "]";
 	}
 	@Basic
 	public Integer getWeight() {
 		return weight;
 	}
 	public void setWeight(Integer weight) {
 		this.weight = weight;
 	}
 
 }
