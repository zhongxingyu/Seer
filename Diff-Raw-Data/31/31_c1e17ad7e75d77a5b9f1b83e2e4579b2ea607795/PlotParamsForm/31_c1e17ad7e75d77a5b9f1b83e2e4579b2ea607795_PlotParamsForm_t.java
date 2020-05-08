 /*
 
 $Source: /share/content/gforge/webcgh/webgenome/src/org/rti/webcgh/webui/plot/PlotParamsForm.java,v $
$Revision: 1.4 $
$Date: 2006-05-26 17:39:37 $
 
 The Web CGH Software License, Version 1.0
 
 Copyright 2003 RTI. This software was developed in conjunction with the National 
 Cancer Institute, and so to the extent government employees are co-authors, any 
 rights in such works shall be subject to Title 17 of the United States Code, 
 section 105.
 
 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this 
 list of conditions and the disclaimer of Article 3, below. Redistributions in 
 binary form must reproduce the above copyright notice, this list of conditions 
 and the following disclaimer in the documentation and/or other materials 
 provided with the distribution.
 
 2. The end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment:
 
 "This product includes software developed by the RTI and the National Cancer 
 Institute."
 
 If no such end-user documentation is to be included, this acknowledgment shall 
 appear in the software itself, wherever such third-party acknowledgments 
 normally appear.
 
 3. The names "The National Cancer Institute", "NCI", 
 Research Triangle Institute, and "RTI" must not be used to endorse or promote 
 products derived from this software.
 
 4. This license does not authorize the incorporation of this software into any 
 proprietary programs. This license does not authorize the recipient to use any 
 trademarks owned by either NCI or RTI.
 
 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL 
 CANCER INSTITUTE, RTI, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 */
 
 
 /**
  * Struts form class for capturing user plotting preferences.
  */
 
 package org.rti.webcgh.webui.plot;
 
 
 import java.util.Collection;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.collections.primitives.ArrayIntList;
 import org.apache.commons.collections.primitives.IntIterator;
 import org.apache.commons.collections.primitives.IntList;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionError;
 import org.rti.webcgh.array.GenomeIntervalDto;
 import org.rti.webcgh.array.GenomeIntervalFormatException;
 import org.rti.webcgh.graph.ChromosomeIdeogramSize;
 import org.rti.webcgh.graph.PlotParameters;
 import org.rti.webcgh.graph.PlotType;
 import org.rti.webcgh.graph.Units;
 
 
 /**
  * Struts form class for capturing user plotting preferences.
  */
 public class PlotParamsForm extends ActionForm {
     
     private String width = "800";  // Width of plot
     private String height = "300"; // Height of plot
     private String genomeIntervals = "1";
     private String units = Units.KB.getName();
 	private String minY = null;
 	private String maxY = null;
 	private String showLabels = "on";
 	private String plotType = "genome";
 	private String selectedFeatureTypes = "";
 	private String pipelineName = "none";
 	private String plotsPerRow = "5";
 	private String chromIdeogramSize = ChromosomeIdeogramSize.MEDIUM.getName();
 	private String lowerFoldChangeThresh = "-0.1";
 	private String upperFoldChangeThresh = "0.1";
 	private String showIdeogram = "";
 	private String selectedProbes = "";
 	private String assemblyId = "none";
 	private String quantitationTypeId = "";
 	private String paramsConfigured = "";
     
     
     /**
      * @return Returns the quantitationTypeId.
      */
     public String getQuantitationTypeId() {
         return quantitationTypeId;
     }
     
     
     /**
      * @param quantitationTypeId The quantitationTypeId to set.
      */
     public void setQuantitationTypeId(String quantitationTypeId) {
         this.quantitationTypeId = quantitationTypeId;
     }
     
     
 	/**
 	 * @return Returns the genomeIntervals.
 	 */
 	public String getGenomeIntervals() {
 		return genomeIntervals;
 	}
 	
 	
 	/**
 	 * @param genomeIntervals The genomeIntervals to set.
 	 */
 	public void setGenomeIntervals(String genomeIntervals) {
 		this.genomeIntervals = genomeIntervals;
 	}
 	
 	
 	/**
 	 * @return Returns the units.
 	 */
 	public String getUnits() {
 		return units;
 	}
 	
 	
 	/**
 	 * @param units The units to set.
 	 */
 	public void setUnits(String units) {
 		this.units = units;
 	}
 	
 	
     /**
      * Get width of plot
      * @return Width of plot
      */
     public String getWidth() {return width;}
     
     /**
      * Get height of plot
      * @return Height of plot
      */
     public String getHeight() {return height;}
     
     
     /**
      * Set width of plot
      * @param width
      */
     public void setWidth(String width) {this.width = width;}
     
     /**
      * Set height of plot
      * @param height
      */
     public void setHeight(String height) {this.height = height;}
     
     
     /**
      * Setter for property chromsPerRow
      * @param chromsPerRow Chromosomes per row in ideogram plot
      */
     public void setPlotsPerRow(String chromsPerRow) {
     	this.plotsPerRow = chromsPerRow;
     }
     
     
     /**
      * Getter for property chromsPerRow
      * @return Chromosomes per row in ideogram plot
      */
     public String getPlotsPerRow() {
     	return plotsPerRow;
     }
     
     
     /**
      * Return chromosome per row in ideogram plot as an integer value
      * @return Chromosome per row in ideogram plot as an integer value
      */
     public int getChromsPerRowAsInt() {
     	int num = -1;
     	try {
     		num = Integer.parseInt(plotsPerRow);
     	} catch (NumberFormatException e) {}
     	return num;
     }
     
     
     /**
      * Setter for property chromIdeogramSize
      * @param size Relative size of chromosome ideogram (e.g. S/M/L)
      */
     public void setChromIdeogramSize(String size) {
     	this.chromIdeogramSize = size;
     }
     
     
     /**
      * Getter for property chromIdeogramSize
      * @return Relative size of chromosome ideogram (e.g. S/M/L)
      */
     public String getChromIdeogramSize() {
     	return chromIdeogramSize;
     }
     
     
     /**
      * Getter for property paramsConfigured
      * @return Returns "true" if user has configured the plot parameters in popup window
      */
     public String getParamsConfigured() {
 		return paramsConfigured;
 	}
 
 
     /**
      * Setter for property paramsConfigured
      * @param paramsConfigured If equal to "true", indicates if user has configured plot params in the popup window yet
      */
 	public void setParamsConfigured(String paramsConfigured) {
 		this.paramsConfigured = paramsConfigured;
 	}
 
 
 	/**
      * Reset form fields
      * @param mapping Routing information
      * @param request Servlet request object
      */
     public void reset(ActionMapping mapping, HttpServletRequest request) {
     	
     	// *********************************************************
     	// Workaround for framework not setting checkboxes properly
     	// *********************************************************
     	if (request.getParameter("update") != null) {
     		showLabels = (request.getParameter("showLabels") == null)? 
 				"" : "on";
     		showIdeogram = (request.getParameter("showIdeogram") == null)? 
 				"" : "on";
     	}
     	// **************************************************************
     }
         
     /**
      * Validate form fields
      * @param mapping Routing information
      * @param request Servlet request object
      * @return Errors
      */
     public ActionErrors validate
     (
         ActionMapping mapping, HttpServletRequest request
     ) {
     	String plotTypeParam = request.getParameter("plotType");
         ActionErrors errors= new ActionErrors();
         
         // Genome segment
         boolean canBeEmpty = ! "annotation".equals(plotTypeParam);
         boolean invalidSeg = false;
 		boolean lowerThresh = validDoubleField(lowerFoldChangeThresh, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
 		boolean upperThresh = validDoubleField(upperFoldChangeThresh, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
 		
 		if (! lowerThresh) {
 			invalidSeg = true;
 			errors.add("lowerFoldChangeThresh", new ActionError("invalid.field"));
 		}
 		if (! upperThresh) {
 			invalidSeg = true;
 			if (lowerThresh)
 				errors.add("lowerFoldChangeThresh", new ActionError("invalid.field"));
 		}
 		if (lowerThresh && upperThresh) {
 			if (getLowerThreshAsDouble() > getUpperThreshAsDouble()) 
 				errors.add("lowerFoldChangeThresh", new ActionError("invalid.field"));
 		}
 		if (this.genomeIntervals != null && this.genomeIntervals.length() > 1) {
 			try {
 				GenomeIntervalDto[] dtos = 
 					GenomeIntervalDto.parse(this.genomeIntervals, 
 							Units.getUnits(this.units));
 				for (int i = 0; i < dtos.length; i++) {
 					GenomeIntervalDto dto = dtos[i];
 					if (dto.getStart() > dto.getEnd()) {
 						errors.add("genomeIntervals", new ActionError("invalid.field"));
 						break;
 					}
 				}
 			} catch (Exception e) {
 				errors.add("genomeIntervals", new ActionError("invalid.field"));
 			}
 		}
 				
         // Plot dimensions
         if (! validIntegerField(width, 50, 4000, false))
 			errors.add("width", new ActionError("invalid.field"));
 		canBeEmpty = "annotation".equals(plotTypeParam);
 		if (! validIntegerField(height, 50, 10000, canBeEmpty))
 			errors.add("height", new ActionError("invalid.field"));
 			
 		// Plotting parameters
 		boolean validY = true;
 		if (! validDoubleField(minY, -1000.0, 1000.0, true)) {
 			errors.add("minY", new ActionError("invalid.field"));
 			validY = false;
 		} if (! validDoubleField(maxY, -1000.0, 1000.0, true)) {
 			errors.add("maxY", new ActionError("invalid.field"));
 			validY = false;
 		}
 		if (validY) {
 			double doubleMinY = this.getMinYAsDouble();
 			double doubleMaxY = this.getMaxYAsDouble();
 			if (! Double.isNaN(doubleMinY) && ! Double.isNaN(doubleMaxY))
 			    if (doubleMinY > doubleMaxY) {
 			        errors.add("minY", new ActionError("invalid.field"));
 			        errors.add("maxY", new ActionError("invalid.field"));
 			    }
 		}
 			
 		// Ideogram plot parameters
 		if (! validIntegerField(plotsPerRow, 1, 100, true))
 			errors.add("chromsPerRow", new ActionError("invalid.field"));
 		
 		// Selected probes
 		if ("bar".equals(plotType)) {
 			if (selectedProbes == null || selectedProbes.length() < 1)
 				errors.add("selectedProbes", new ActionError("invalid.field"));
 		}
 		if (selectedProbes != null && selectedProbes.length() > 0) {
 			if (! validCommaSeparatedList(selectedProbes))
 				errors.add("selectedProbes", new ActionError("invalid.field"));
 		}
 		
 		// Reference genome assembly
 //		if ("ideogram".equals(plotType) || "annotation".equals(plotType)) {
 //			if (this.assemblyId == null || this.assemblyId.length() < 1 ||
 //				"none".equals(this.assemblyId))
 //				errors.add("assemblyId", new ActionError("invalid.field"));
 //		} else if ("scatter".equals(plotType) && "on".equalsIgnoreCase(this.showIdeogram)) {
 //		    if (this.assemblyId == null || this.assemblyId.length() < 1 ||
 //				"none".equals(this.assemblyId))
 //				errors.add("assemblyId", new ActionError("invalid.field"));
 //		}
 			
         if (errors.size() > 0) {
         	errors.add("global", new ActionError("invalid.fields"));
         	
         }
        
         return errors;
     }
     
     
     private boolean validCommaSeparatedList(String text) {
     	boolean valid = true;
     	IntList wordBreaks = findWordBreaks(text);
     	for (IntIterator it = wordBreaks.iterator(); it.hasNext() & valid;) {
     		int p = it.next();
     		if (! breakContainsChar(text, p, ','))
     			valid = false;
     	}
     	return valid;
     }
     
     
     private IntList findWordBreaks(String text) {
     	IntList list = new ArrayIntList();
     	boolean inBreak = false;
     	for (int i = 0; i < text.length(); i++) {
     		char c = text.charAt(i);
     		if (c == ',' || Character.isWhitespace(c)) {
     			if (! inBreak) {
     				list.add(i);
     				inBreak = true;
     			}
     		} else
     			inBreak = false;
     	}
     	return list;
     }
     
     
     private boolean breakContainsChar(String text, int breakPoint, char c) {
     	boolean contains = false;
     	for (int i = breakPoint; i < text.length() && ! contains; i++) {
     		char d = text.charAt(i);
     		if (c == d)
     			contains = true;
     		if (! Character.isWhitespace(d))
     			break;
     	}
     	return contains;
     }
     
     
     /**
      * Is field empty?
      * @param value Field value
      * @return T/F depending on if field is empty
      */
     private boolean emptyField(String value) {
     	return value == null || value.length() < 1;
     }
     
     
     /**
      * Does field contain a valid integer?
      * @param value Field value
      * @param minValue Minimum valid value for field
      * @param maxValue Maximul valid valud for field
      * @param canBeEmpty Can field be empty?
      * @return T/F
      */
     private boolean validIntegerField
     (
     	String value, int minValue, int maxValue, boolean canBeEmpty
     ) {
     	boolean valid = true;
     	if (value == null || value.length() < 1) {
     		if (canBeEmpty)
     			return true;
     		else
     			valid = false;
     	}
     	if (valid) {
     		int temp = -1;
     		try {
     			temp = Integer.parseInt(value);
     		} catch (NumberFormatException e) {
     			return false;
     		}
     		if (temp < minValue || temp > maxValue)
     			valid = false;
     	}
     	return valid;
     }
     
     
     
     /**
      * Does field contain a valid double value?
      * @param value Field value
      * @param minValue Minimum valid value
      * @param maxValue Maximum valid value
      * @param canBeEmpty Can field be empty?
      * @return T/F
      */
 	private boolean validDoubleField(
 		String value, double minValue, double maxValue, boolean canBeEmpty
 	) {
 		boolean valid = true;
 		if (value == null || value.length() < 1) {
 			if (canBeEmpty)
 				return true;
 			else
 				valid = false;
 		}
 		if (valid) {
 			double temp = Double.NaN;
 			try {
 				temp = Double.parseDouble(value);
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			if (temp < minValue || temp > maxValue)
 				valid = false;
 		}
 		return valid;
 	}
     
 	/** 
 	 * Get minimum color saturation value for copy number intensity
 	 * @return Minimum color saturation value
 	 */
 	public String getMinSaturation() {return minY;}
     
     
 	/** 
 	 * Get maximum color saturation value for copy number intensity
 	 * @return Maximum color saturation value
 	 */
 	public String getMaxSaturation() {return maxY;}
 	
 	/** 
 	 * Set maximum color saturation value for copy number intensity
 	 * @param max Maximum saturation
 	 */
 	public void setMaxSaturation(String max) {
 		maxY = max;
 	}
     
 	/** 
 	 * Set minimum color saturation value for copy number intensity
 	 * @param min Minimum color saturation value
 	 */
 	public void setMinSaturation(String min) {
 		minY = min;
 	}
 	
 	
 	/**
 	 * Setter for property minY
 	 * @param minY Minimum Y-axis value
 	 */
 	public void setMinY(String minY) {
 		this.minY = minY;
 	}
 	
 	
 	/**
 	 * Getter for property minY
 	 * @return Minimum Y-axis value
 	 */
 	public String getMinY() {
 		return minY;
 	}
 	
 	
 	/**
 	 * Setter for property maxY
 	 * @param maxY Maximum Y-axis value
 	 */
 	public void setMaxY(String maxY) {
 		this.maxY = maxY;
 	}
 	
 	
 	/**
 	 * Getter for property maxY
 	 * @return Maximum Y-axis value
 	 */
 	public String getMaxY() {
 		return maxY;
 	}
 	
 	
 	/**
 	 * Setter method for attribute showLabels
 	 * @param showLabels Show feature labels in plot?
 	 */
 	public void setShowLabels(String showLabels) {
 		this.showLabels = showLabels;
 	}
 	
 	/**
 	 * Setter method for attribute show Labels
 	 * @return Show feature labels in plot?
 	 */
 	public String getShowLabels() {
 		return showLabels;
 	}
 	
 	
 	/**
 	 * Setter for property plotType
 	 * @param plotType Type of plot
 	 */
 	public void setPlotType(String plotType) {
 		this.plotType = plotType;
 	}
 	
 	
 	/**
 	 * Getter for property plotType
 	 * @return Type of plot
 	 */
 	public String getPlotType() {
 		return plotType;
 	}
 	
 	
 	/**
 	 * Setter for property selectedFeatureTypes
 	 * @param types Comma separated String list of selected feature types
 	 */
 	public void setSelectedFeatureTypes(String types) {
 		this.selectedFeatureTypes = types;
 	}
 	
 	
 	/**
 	 * Getter for property selectedFeatureTypes
 	 * @return Comma separated String list of selected feature types
 	 */
 	public String getSelectedFeatureTypes() {
 		return selectedFeatureTypes;
 	}
 	
 	
 	/**
 	 * Get list of selected feature types as an array
 	 * @return Array of selected feature types
 	 */
 	public String[] getSelectedFeatureTypesAsArray() {
 		Collection col = this.getSelectedFeatureTypesAsCollection();
 		String[] types = new String[0];
 		types = (String[])col.toArray(types);
 		return types;
 	}
 	
 	
 	/**
 	 * Return selecte feature types as a Collection of Strings
 	 * @return Collection of Strings
 	 */
 	public Collection getSelectedFeatureTypesAsCollection() {
 		Collection col = new ArrayList();
 		StringTokenizer tok = new StringTokenizer(this.selectedFeatureTypes, ",");
 		while (tok.hasMoreTokens())
 			col.add(tok.nextToken());
 		return col;
 	}
 	
 	
 	/**
 	 * Setter for property pipeline name
 	 * @param pipelineName Name of analytic pipeline
 	 */
 	public void setPipelineName(String pipelineName) {
 		this.pipelineName = pipelineName;
 	}
 	
 	
 	/**
 	 * Getter for property pipeline name
 	 * @return Name of analytic pipeline
 	 */
 	public String getPipelineName() {
 		return pipelineName;
 	}
 	
 	
 	/**
 	 * Get "multiplier" associated with a set of units.
 	 * (i.e. The multiplier for "KB" is 1000)
 	 * @param units Set of units
 	 * @return Multiplier for given set of units
 	 */
 	public static synchronized int multiplier(String units) {
 		int mplr = 0;
 		if (units.equals("bp"))
 			mplr = 1;
 		else if (units.equals("kb"))
 			mplr = 1000;
 		else if (units.equals("mb"))
 			mplr = 1000000;
 		return mplr;
 	}
 	
 	
 	/**
 	 * Get width property as an integer
 	 * @return Width property as an integer
 	 */
 	public int getWidthAsInt() {
 		int widthInt = -1;
 		try {
 			widthInt = Integer.parseInt(width);
 		} catch (Exception e) {}
 		return widthInt;
 	}
 	
 	
 	/**
 	 * Get height property as an integer
 	 * @return Height property as an integer
 	 */
 	public int getHeightAsInt() {
 		int heightInt = -1;
 		try {
 			heightInt = Integer.parseInt(height);
 		} catch (Exception e) {}
 		return heightInt;
 	}
 	
 	
 	/**
 	 * Get minSaturation property as a double
 	 * @return minSaturation property as a double
 	 */
 	public double getMinSaturationAsDouble() {
 		double minSatDouble = Double.NaN;
 		try {
 			minSatDouble = Double.parseDouble(minY);
 		} catch (Exception e) {}
 		return minSatDouble;
 	}
 	
 	
 	/**
 	 * Get maxSaturation property as a double
 	 * @return maxSaturation property as a double
 	 */
 	public double getMaxSaturationAsDouble() {
 		double maxSatDouble = Double.NaN;
 		try {
 			maxSatDouble = Double.parseDouble(maxY);
 		} catch (Exception e) {}
 		return maxSatDouble;
 	}
 	
 	
 	/**
 	 * Get minY property as a double
 	 * @return minY property as a double
 	 */
 	public double getMinYAsDouble() {
 		double minYDouble = Double.NaN;
 		try {
 			minYDouble = Double.parseDouble(minY);
 		} catch (Exception e) {}
 		return minYDouble;
 	}
 	
 	
 	/**
 	 * Get maxY property as a double
 	 * @return maxY property as a double
 	 */
 	public double getMaxYAsDouble() {
 		double maxYDouble = Double.NaN;
 		try {
 			maxYDouble = Double.parseDouble(maxY);
 		} catch (Exception e) {}
 		return maxYDouble;
 	}
 	
 	
 	/**
 	 * Get showLabels property as a boolean
 	 * @return showLabels property as a boolean
 	 */
 	public boolean isShowLabels() {
 		return "on".equals(showLabels);
 	}
 	
 	
 	/**
 	 * Setter for property showIdeogram
 	 * @param showIdeogram Show ideogram in plot?
 	 */
 	public void setShowIdeogram(String showIdeogram) {
 		this.showIdeogram = showIdeogram;
 	}
 	
 	
 	/**
 	 * Getter for property showIdeogram
 	 * @return Show ideogram in plot?
 	 */
 	public String getShowIdeogram() {
 		return showIdeogram;
 	}
 	
 	
 	/**
 	 * Show ideogram in plot?
 	 * @return T/F
 	 */
 	public boolean isShowIdeogram() {
 		return "on".equals(showIdeogram);
 	}
 	
 	
 	/**
 	 * Determins if one set of units is "less" than another
 	 * (i.e. BP is less than KB)
 	 * @param u1 First set of units
 	 * @param u2 Second set of units
 	 * @return T/F if u1 less than u2
 	 */
 	private boolean unitsLessThan(String u1, String u2) {
 		boolean lessThan = false;
 		if (u1.equals("bp")) {
 			if (u2.equals("kb") || u2.equals("mb"))
 				lessThan = true;
 		} else if (u1.equals("kb")) {
 			if (u2.equals("mb"))
 				lessThan = true;
 		}
 		return lessThan;
 	}
 	/**
 	 * @return Lower fold change threshold
 	 */
 	public String getLowerFoldChangeThresh() {
 		return lowerFoldChangeThresh;
 	}
 
 	/**
 	 * @return Upper fold change threshold
 	 */
 	public String getUpperFoldChangeThresh() {
 		return upperFoldChangeThresh;
 	}
 
 	/**
 	 * @param string
 	 */
 	public void setLowerFoldChangeThresh(String string) {
 		lowerFoldChangeThresh = string;
 	}
 
 	/**
 	 * @param string
 	 */
 	public void setUpperFoldChangeThresh(String string) {
 		upperFoldChangeThresh = string;
 	}
 	
 	/**
 	 * Get upperFoldChangeThresh property as a double
 	 * @return upperFoldChangeThresh property as a double
 	 */
 	public double getUpperThreshAsDouble() {
 		double upperDouble = Double.NaN;
 		try {
 			upperDouble = Double.parseDouble(upperFoldChangeThresh);
 		} catch (Exception e) {}
 		return upperDouble;
 	}
 	
 	/**
 	 * Get lowerFoldChangeThresh property as a double
 	 * @return lowerFoldChangeThresh property as a double
 	 */
 	public double getLowerThreshAsDouble() {
 		double lowerDouble = Double.NaN;
 		try {
 			lowerDouble = Double.parseDouble(lowerFoldChangeThresh);
 		} catch (Exception e) {}
 		return lowerDouble;
 	}	
 
 
 	/**
 	 * @return Returns the probeList.
 	 */
 	public String getSelectedProbes() {
 		return selectedProbes;
 	}
 	
 	
 	/**
 	 * @param selectedProbes The probeList to set.
 	 */
 	public void setSelectedProbes(String selectedProbes) {
 		this.selectedProbes = selectedProbes;
 	}
 	
 	
 	/**
 	 * Get selected probes
 	 * @return Selected probes
 	 */
 	public Collection getSelectedProbesAsList() {
 		Collection probes = new ArrayList();
 		if (selectedProbes != null && selectedProbes.length() > 0) {
 			StringTokenizer tok = new StringTokenizer(selectedProbes, ",");
 			while (tok.hasMoreTokens()) {
 				String probe = tok.nextToken();
 				probes.add(probe);
 			}
 		}
 		return probes;
 	}
 	
 	
 	/**
 	 * @return Returns the assemblyId.
 	 */
 	public String getAssemblyId() {
 		return assemblyId;
 	}
 	
 	
 	/**
 	 * @param assemblyId The assemblyId to set.
 	 */
 	public void setAssemblyId(String assemblyId) {
 		this.assemblyId = assemblyId;
 	}
 	
 		
 	/**
 	 * Get plot parameters
 	 * @return Plot parameters
 	 * @throws GenomeIntervalFormatException
 	 */
     public PlotParameters getPlotParameters() throws GenomeIntervalFormatException {
         Units units = Units.getUnits(this.units);
         PlotParameters pp = new PlotParameters();
         if (! this.emptyField(this.height))
         	pp.setHeight(Integer.parseInt(this.height));
         if (! this.emptyField(this.maxY))
         	pp.setMaxY(Double.parseDouble(this.maxY));
         if (! this.emptyField(this.minY))
         	pp.setMinY(Double.parseDouble(this.minY));
         if (! this.emptyField(this.width))
         	pp.setWidth(Integer.parseInt(this.width));
         if (! this.emptyField(this.plotsPerRow))
             pp.setPlotsPerRow(Integer.parseInt(this.plotsPerRow));
         pp.setXUnits(units);
         pp.setChromosomeIdeogramSize(ChromosomeIdeogramSize.getChromosomeIdeogramSize(this.chromIdeogramSize));
         pp.setGenomeIntervalDtos(GenomeIntervalDto.parse(this.genomeIntervals, units));
         pp.setPlotType(PlotType.getPlotType(this.plotType));
         pp.setShowIdeogram(this.showIdeogram != null && "on".equals(this.showIdeogram));
         if (this.emptyField(this.lowerFoldChangeThresh))
             pp.setLowerMaskValue(Double.NaN);
         else
             pp.setLowerMaskValue(Double.parseDouble(this.lowerFoldChangeThresh));
         if (this.emptyField(this.upperFoldChangeThresh))
             pp.setUpperMaskValue(Double.NaN);
         else
             pp.setUpperMaskValue(Double.parseDouble(this.upperFoldChangeThresh));
         return pp;
     }
 }
