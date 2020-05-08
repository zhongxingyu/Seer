 /*
  * Web service utility functions for managing hibernate, json, etc.
  * 
  * Copyright (C) 2010 Regents of the University of Colorado.  
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package edu.ucdenver.bios.webservice.common.domain;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
import edu.ucdenver.bios.webservice.common.enumclasses.HorizontalAxisLabelEnum;
 import edu.ucdenver.bios.webservice.common.enums.StratificationVariableEnum;
 
 /**
  * Object describes Power Curve Structure.
  * 
  * @author Uttara Sakhadeo
  */
 @Entity
 @Table(name="POWER_CURVE_DESCRIPTION")
 public class PowerCurveDescription
 {	
 	/*--------------------
 	 * Member Variables
 	 *--------------------*/		
 	@Id
 	private int id;
 	@Column(name="sampleSize")
 	private int sampleSize;
 	@Column(name="powerCurveDescription")
 	private String powerCurveDescription;
 	@Column(name="statisticalTest")
 	private String statisticalTest;
 	@Column(name="regressionCoeeficientScaleFactor")
 	private float regressionCoeeficientScaleFactor;
 	@Column(name="variabilityScaleFactor")
 	private float variabilityScaleFactor;
 	@Column(name="typeIError")
 	private float typeIError;
 	@Column(name="uuid")
 	private StudyDesign studyDesign;
 	@Column(name="horizontalAxisLabel")
 	private HorizontalAxisLabelEnum horizontalAxisLabelEnum;
 	@Column(name="stratificationVariable")
 	private StratificationVariableEnum stratificationVarEnum;	
 	/*--------------------
 	 * Constructors
 	 *--------------------*/
 	/*
 	 * Create an empty Power Curve Description object
 	 */
 	public PowerCurveDescription()
 	{}
 	/*--------------------
 	 * Getter/Setter Methods
 	 *--------------------*/
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public int getSampleSize() {
 		return sampleSize;
 	}
 	public void setSampleSize(int sampleSize) {
 		this.sampleSize = sampleSize;
 	}
 	public String getPowerCurveDescription() {
 		return powerCurveDescription;
 	}
 	public void setPowerCurveDescription(String powerCurveDescription) {
 		this.powerCurveDescription = powerCurveDescription;
 	}
 	public String getStatisticalTest() {
 		return statisticalTest;
 	}
 	public void setStatisticalTest(String statisticalTest) {
 		this.statisticalTest = statisticalTest;
 	}
 	public float getRegressionCoeeficientScaleFactor() {
 		return regressionCoeeficientScaleFactor;
 	}
 	public void setRegressionCoeeficientScaleFactor(
 			float regressionCoeeficientScaleFactor) {
 		this.regressionCoeeficientScaleFactor = regressionCoeeficientScaleFactor;
 	}
 	public float getVariabilityScaleFactor() {
 		return variabilityScaleFactor;
 	}
 	public void setVariabilityScaleFactor(float variabilityScaleFactor) {
 		this.variabilityScaleFactor = variabilityScaleFactor;
 	}
 	public float getTypeIError() {
 		return typeIError;
 	}
 	public void setTypeIError(float typeIError) {
 		this.typeIError = typeIError;
 	}
 	public StudyDesign getStudyDesign() {
 		return studyDesign;
 	}
 	public void setStudyDesign(StudyDesign studyDesign) {
 		this.studyDesign = studyDesign;
 	}	
 	public StratificationVariableEnum getStratificationVarEnum() {
 		return stratificationVarEnum;
 	}
 	public void setStratificationVarEnum(
 			StratificationVariableEnum stratificationVarEnum) {
 		this.stratificationVarEnum = stratificationVarEnum;
 	}
 	public HorizontalAxisLabelEnum getHorizontalAxisLabelEnum() {
 		return horizontalAxisLabelEnum;
 	}
 	public void setHorizontalAxisLabelEnum(
 			HorizontalAxisLabelEnum horizontalAxisLabelEnum) {
 		this.horizontalAxisLabelEnum = horizontalAxisLabelEnum;
 	}
 	
 }
