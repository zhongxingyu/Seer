 /*
  * Copyright (c) 2010, Regents of the University of Colorado
  * 
  * All rights reserved. Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided that the following 
  * conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution. 
  * Neither the name of the University of Colorado nor the names of its
  * contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package lasp.tss.variable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import lasp.tss.TSSException;
 import lasp.tss.TSSPublicException;
 import lasp.tss.filter.Filter;
 
 import org.apache.log4j.Logger;
 
 import ucar.ma2.Array;
 import ucar.ma2.DataType;
 import ucar.ma2.Range;
 import ucar.nc2.Attribute;
 import ucar.nc2.Variable;
 
 /**
  * Abstract class for variables in the TSS implementation of the Common Data Model.
  * 
  * @author Doug Lindholm
  */
 public abstract class TSSVariable {
 
     // Initialize a logger.
     private static final Logger _logger = Logger.getLogger(TSSVariable.class);
     
     /**
      * The Variable that contains this one.
      * It will be null for the TimeSeries sequence only.
      * This will be defined when the VariableFactory constructs the dataset.
      */
     private CompositeVariable _parent;
     
     /**
      * Internal representation of the variable.
      * Can model the variable, even subset, without reading data.
      * This will come from the reading of the NcML during VariableFactory construction.
      */
     private ucar.nc2.Variable _ncVariable; 
     
     /**
      * Let each variable maintain its "projection" state. 
      * I.e., has it been selected for output.
      * Default to true (no projection clause in the request).
      * Otherwise, this will be set by a ProjectionConstraint.
      */
     private boolean _projected = true; 
 
     /**
      * List of Filters to apply to each time sample during read/write.
      * Added here by a SelectionConstraint or FilterConstraint.
      */
     private List<Filter> _filterList; 
     
     //--------------------------------------------------------------------------
     
     /**
      * Return the name of the OPeNDAP variable type represented by this subclass.
      */
     public abstract String getDodsType();
     
     /**
      * Construct a variable based on a NetCDF Variable 
      * (as opposed to a Group for CompositeVariable-s).
      */
     protected TSSVariable(CompositeVariable parent, Variable ncVariable) {
         _parent = parent;
         _ncVariable = ncVariable;
     }
     
     /**
      * Return the internal NetCDF Variable.
      */
     public Variable getNetcdfVariable() {
         return _ncVariable;
     }
 
     /**
      * Let Variable instances replace the internal netCDF Variable.
      */
     protected void setNetcdfVariable(Variable ncVariable) {
         _ncVariable = ncVariable;
     }
     
     /**
      * Return the parent CompositeVariable that contains this one.
      * Will be null if this is the top level TimeSeries sequence.
      */
     public CompositeVariable getParent() {
         return _parent;
     }
     
     /**
      * Return the short (local) name of this variable.
      */
     public String getName() {
         return _ncVariable.getShortName();
     }
     
     /**
      * Return the complete name of the variable 
      * with all ancestors' names starting with TimeSeries 
      * delimited by "."
      */
     public String getLongName() {
         String name = getName();
         
         CompositeVariable parent = getParent();
         while (parent != null) {
             name = parent.getName() + "." + name;
             parent = parent.getParent();
         }
         
         return name;
     }
 
     /**
      * Return the NetCDF Attributes from the internal NetCDF Variable,
      * ultimately from the NcML. These will be preserved in the OPeNDAP DAS output.
      */
     public List<Attribute> getAttributes() {
         return _ncVariable.getAttributes();
     }
     
     /**
      * Return the value of the given attribute as a String. 
      * If the type is not String, it will assume it is a Number and try to make a String out of it.
      */
     public String getAttributeValue(String string) {
         String value = null;
         
         Attribute att = _ncVariable.findAttribute(string);
         if (att != null) {
             if (att.isString()) value = att.getStringValue();
             else value = att.getNumericValue().toString();
         }
         
         return value;
     }
     
     /**
      * Add an attribute to appear in the OPeNDAP DAS.
      */
     public void addAttribute(String name, String value) {
         Attribute att = new Attribute(name, value);
         _ncVariable.addAttribute(att);
     }
         
     /**
      * Return the units for this variable.
      * May be null.
      */
     public String getUnits() {
         String units = getAttributeValue("units");
         return units;
     }
     
   //---------------------------------------------------------------------------------
 
     /**
      * Return the number of dependent variables encapsulated by this variable.
      * Default to one, which is the case for scalar variables.
      */
     public int getNumDependentVariables() {
         return 1;
     }
     
     /**
      * Number of bytes needed to hold one time sample.
      * For scalars, this is the size of the primitive type. (e.g. double: 8)
      */
     public int getTimeSampleSizeInBytes() {
         //DataType type = getNetcdfVariable().getDataType();
         //int size = type.getSize();
         
         //assume all data are doubles
         return 8;
     }
 
     /**
      * Apply the time range to redefine the NetCDF Variable before it is read.
      * Use the name of the Range to find and apply to the appropriate variable dimension.
      * Called during TimeSeriesDataset init while applying constraints.
      */
     public void subset(Range range) {
         //Handle empty range. Should result in no data.
         if (range == null || range.length() == 0) {
             //variable is empty
             setLength(0);
             return;
         }
         
         String rname = range.getName(); //name of dimension
         
         Variable ncvar = getNetcdfVariable();
         int idim = ncvar.findDimensionIndex(rname);
         
         List<Range> ranges = new ArrayList<Range>(ncvar.getRanges()); //need copy, original is immutable
         ranges.set(idim, range);
         
         try {
             _ncVariable = ncvar.section(ranges);
         } catch (Exception e) {
             String msg = "Failed to subset variable for index range: [" + range + "]";
             _logger.error(msg, e);
             throw new TSSPublicException(msg, e);
         } 
         
     }
     
     /**
      * Allow a Constraint to add a Filter to be applied to this variable during read/write.
      */
     public void addFilter(Filter filter) {
         if (_filterList == null) _filterList = new ArrayList<Filter>();
         if (filter != null) _filterList.add(filter);
         else {
             String msg = "Tried to add a null Filter.";
             _logger.warn(msg);
         }
     }
     
     /**
      * Is this Variable a String?
      */
     public boolean isString() {
        DataType type = getNetcdfVariable().getDataType();
        return type.isString();
        //return (this instanceof StringVariable);
     }
     
     /**
      * Is this Variable independent?
      */
     public boolean isIndependent() {
         return (this instanceof IndependentVariable);
     }
     
     /**
      * Is this Variable a Structure?
      */
     public boolean isStructure() {
         return (this instanceof StructureVariable);
     }
     
     /**
      * Is this Variable a Sequnece?
      */
     public boolean isSequence() {
         return (this instanceof SequenceVariable);
     }
     
     /**
      * Is this variable to be output in the response.
      */
     public boolean isProjected() {
         return _projected;
     }
     
     /**
      * Tell this variable if it has been selected for output.
      */
     public void setProjected(boolean b) {
         _projected = b;
     }
  
   //---------------------------------------------------------------------------
 
     /**
      * Read all values of the variable within the requested range of its 
      * independent variables as defined by the constraint expression.
      */
     public Array read() {
         Array ncArray = null;
         
         //Hack to support empty Variable
         int length = getLength();
         if (length == 0) return null;
         
         Variable ncvar = getNetcdfVariable();
         
         try {
             ncArray = ncvar.read();
         } catch (Exception e) {
             String msg = "Failed to read variable: " + ncvar.getName();
             _logger.error(msg, e);
             throw new TSSException(msg, e);
         }
 
         return ncArray;
     }
 
 
     /**
      * Return all the data values for this variable adhering to currently applied constraints.
      * May be null.
      */
     public double[] getValues() {
         double[] values = null;
         Array array = read();
         
         //makes a copy of the data, but getStorage return entire backing array, unsectioned
         if (array != null) values = (double[]) array.get1DJavaArray(double.class); 
         
         return values;
     }
 
     /**
      * Return a 1D array of data values representing this variable for one time sample. 
      * The time dimension varies slowest. May be null.
      * Scalar will have a single value. 
      * Structure will have one for each member.
      * Sequence will have one for every inner function sample times the number of members.
      */
     public double[] getValues(int timeIndex) {
         double[] d = null;
 
         Array array = getTimeSample(timeIndex);
         if (array == null) return null;
         
         d = (double[]) array.get1DJavaArray(double.class);
 
         return d;
     }
     
     /**
      * Get a time sample and return as an array of Strings.
      * Will return null if no samples satisfy this request.
      */
     public String[] getStringValues(int timeIndex) {
         String[] ss = null;
         
         double[] values = getValues(timeIndex);
         if (values != null) {
             int n = values.length;
             ss = new String[n];
             int i=0;
             for (double d : values) ss[i++] = ""+d;
         }
         
         return ss;
     }
 
     /**
      * Allow Variable implementations to specify how a String representation should be parsed into a double.
      * Used by the SelectionConstraint.
      */
     public double parseValue(String s) {
         double d = Double.parseDouble(s);
         return d;
     }
     
     /**
      * Return a NetCDF Array with the requested time sample.
      * Involves reading a section of the variable, as opposed to sectioning a previously read Array. 
      */
     public Array getTimeSample(int timeIndex) {
         Variable ncvar = getNetcdfVariable();
         Array array = null;  
         
         try {    
             List<Range> ranges = new ArrayList<Range>(ncvar.getRanges()); //original Ranges are immutable
             Range trange = new Range(timeIndex, timeIndex);
             ranges.set(0, trange); //Time is first dimension
             array = ncvar.read(ranges);
             
             array = applyFilters(array);
             
         } catch (Exception e) {
             String msg = "Failed to read data for variable: " + ncvar.getName();
             _logger.error(msg, e);
             throw new TSSPublicException(msg, e);
         }
 
         return array;
     }
 
     /**
      * Apply the Variable's filters to the given Array.
      * The expectation is that if any variable fails the filter, 
      * the whole time sample is dropped. That's left up to the Writer.
      */
     protected Array applyFilters(Array array) {
         
         if (_filterList != null) {
             for (Filter filter : _filterList) {
                 array = filter.filter(array);
                 if (array == null) break; //nothing left to filter out
             }
         }
         
         return array;
     }
     
 
   //---------------------------------------------------
     
     /**
      * Hack to allow us to have empty Variables.
      * -1: variable is undefined
      *  0: variable is empty
      */
     private int _length = -1; //not defined
     
     /**
      * Return the number of samples for this variable.
      * Assumes the variable is one-dimensional.
      */
     public int getLength() {
         if (_length != -1) return _length; 
         
         int length = getNetcdfVariable().getShape(0);
         return length;
     }
     
     /**
      * Set the length of the variable.
      * Only a hack to support empty Variables (length=0).
      */
     public void setLength(int length) {
         _length = length;
     }
     
   //---------------------------------------------------
     
     /**
      * Use the internal NetCDF Variable's String representation for this.
      */
     public String toString() {
         String s = getNetcdfVariable().toString();
         return s;
     }
         
 }
