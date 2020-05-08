 /*
  * Software License Agreement (BSD License)
  * 
  * Copyright (c) 2010, IIIA-CSIC, Artificial Intelligence Research Institute
  * All rights reserved.
  * 
  * Redistribution and use of this software in source and binary forms, with or
  * without modification, are permitted provided that the following conditions
  * are met:
  * 
  *   Redistributions of source code must retain the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer.
  * 
  *   Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer in the documentation and/or other
  *   materials provided with the distribution.
  * 
  *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute 
  *   nor the names of its contributors may be used to
  *   endorse or promote products derived from this
  *   software without specific prior written permission of
  *   IIIA-CSIC, Artificial Intelligence Research Institute
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package es.csic.iiia.iea.ddm;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Random;
 import java.util.Set;
 
 /**
  * Base implementation of a cost function.
  *
  * This class provides some basic methods implementing the operations that can
  * be made over cost functions, while delegating the actual cost/utility values
  * representation/storage to the concrete class that extends it.
  *
  * @author Marc Pujol <mpujol at iiia.csic.es>
  */
 public abstract class AbstractCostFunction implements CostFunction {
 
     /**
      * Ordered list of variables involved in this function.
      */
     protected Variable[] variables;
 
     /**
      * Unordered set of variables involved in this function.
      */
     protected LinkedHashSet<Variable> variableSet;
 
     /**
      * Total size (in elements) of the hypercube formed by this function's
      * variables.
      */
     protected int size;
 
     /**
      * List of aggregated dimensionality up to "index".
      */
     protected int[] sizes;
 
     /**
      * Creates a new CostFunction, initialized to zeros.
      *
      * @param variables involved in this factor.
      */
     public AbstractCostFunction(Variable[] variables) {
         this.variables = variables;
         this.variableSet = new LinkedHashSet<Variable>(Arrays.asList(variables));
         computeFunctionSize();
     }
 
     /**
      * Constructs a new factor by copying the given one.
      *
      * @param factor factor to copy.
      */
     public AbstractCostFunction(CostFunction factor) {
         variableSet = new LinkedHashSet<Variable>(factor.getVariableSet());
         variables = variableSet.toArray(new Variable[0]);
         if (factor instanceof AbstractCostFunction) {
             size = factor.getSize();
             sizes = ((AbstractCostFunction)factor).getSizes().clone();
         } else {
             computeFunctionSize();
         }
     }
 
     /**
      * Computes the function's size and dimensionalities.
      * @see #size
      * @see #sizes
      */
     private void computeFunctionSize() {
         final int len = variables.length;
         size = len>0 ? 1 : 0;
         sizes = new int[len];
         boolean overflow = false;
         for (int i=0; i<len; i++) {
             sizes[i] = size;
             size *= variables[len-i-1].getDomain();
             if (size < 0) {
                 overflow = true;
                 break;
             }
         }
 
         if (overflow) {
             size = -1;
         }
     }
 
     /**
      * Gets the value of this factor for the given linearized index.
      *
      * @param index linearized index.
      * @return value corresponding factor value.
      */
     protected abstract double getValue(int index);
 
     /**
      * Gets the value of this factor for the given linearized index.
      *
      * @param index linearized index.
      * @param value of the given index.
      */
     public abstract void setValue(int index, double value);
 
     /**
      * Obtains an iterator over the linearized indices of non-infinity elements of this
      * cost function.
      * 
      * @return Iterator over the indices of this cost function.
      */
     public abstract Iterator<Integer> iterator();
 
     /**
      * Get the optimal variable configuration according to the given
      * operation. If there are multiple optimal configurations, a randomly
      * chosen one is returned.
      *
      * @param mapping variable mapping table to be filled with the optimal
      *                configuration.
      * @param operation operation used to calculate the optimum.
      */
     public void getBestConfiguration(Hashtable<Variable, Integer> mapping, Summarize operation) {
         // Find the maximal value
         ArrayList<Integer> idx = new ArrayList<Integer>();
         double optimal = operation.getNoGood();
         Iterator<Integer> it = iterator();
         while(it.hasNext()) {
             final int i = it.next();
             final double value = getValue(i);
             if (operation.isBetter(value, optimal)) {
                 optimal = value;
                 idx.clear();
                 idx.add(i);
             } else if (value == optimal) {
                 idx.add(i);
             }
         }
         int i = idx.get(new Random().nextInt(idx.size()));
         // Retrieve it's mapping
         mapping.putAll(getMapping(i, null));
     }
 
     /**
      * Get the linearized index corresponding to the given variable mapping.
      *
      * @param mapping of the desired configuration.
      * @return corresponding linearized index.
      */
     public int getIndex(Hashtable<Variable, Integer> mapping) {
         final int len = variables.length;
         int idx = -1;
         for (int i = 0; i < len; i++) {
             if (idx < 0) idx = 0;
             Integer v = mapping.get(variables[i]);
             if (v != null) {
                 idx += sizes[len - i - 1] * v;
             }
         }
         return idx;
     }
 
     /**
      * Get the variable mapping corresponding to the given linearized index.
      *
      * @param index linearized index of the desired configuration.
      * @param mapping variable mapping to be filled. If null, a new mapping
      *                is automatically instantiated.
      * @return variable mapping filled with the desired configuration.
      */
     public Hashtable<Variable, Integer> getMapping(int index, Hashtable<Variable, Integer> mapping) {
         if (mapping == null) {
             mapping = new Hashtable<Variable, Integer>(variables.length);
         } else {
             mapping.clear();
         }
         int[] sub = indexToSubindex(index);
         for (int i = 0, len = variables.length; i < len; i++) {
             mapping.put(variables[i], sub[i]);
         }
         return mapping;
     }
 
     /**
      * Get the function's size (in number of possible configurations).
      * @return number of function's possible configurations.
      */
     public int getSize() {
         return size;
     }
 
     /**
      * Get the functions's aggregated dimesionalities vector.
      * @return function's aggregated dimensionalities vector.
      */
     protected int[] getSizes() {
         return sizes;
     }
 
     /** {@inheritDoc} */
     public String getName() {
         StringBuffer buf = new StringBuffer();
         buf.append("F(");
         if (variables.length > 0) {
             buf.append(variables[0].getName());
             for (int i = 1; i < variables.length; i++) {
                 buf.append(",");
                 buf.append(variables[i].getName());
             }
         }
         buf.append(")");
         return buf.toString();
     }
 
     /** {@inheritDoc} */
     public double getValue(int[] index) {
         return getValue(subindexToIndex(index));
     }
 
     /** {@inheritDoc} */
     public double getValue(Hashtable<Variable, Integer> mapping) {
         return getValue(this.getIndex(mapping));
     }
 
     /** {@inheritDoc} */
     public Set<Variable> getVariableSet() {
         return variableSet;
     }
 
     /**
      * Returns the subindices list (ordered list of values for each variable of
      * this factor) corresponding to the given values array index.
      * index.
      *
      * @param index values array index.
      * @return subindices list.
      */
     protected int[] indexToSubindex(int index) {
         int[] idx = new int[variables.length];
         final int len = variables.length;
         for (int i = 0; i < len; i++) {
             final int ii = len - 1 - i;
             idx[i] = index / sizes[ii];
             index = index % sizes[ii];
         }
         return idx;
     }
 
     /** {@inheritDoc} */
     public void negate(Combine operation) {
         Iterator<Integer> it = iterator();
         while(it.hasNext()) {
             final int i = it.next();
             switch (operation) {
                 case COMBINE_PRODUCT:
                     setValue(i, 1 / getValue(i));
                     break;
                 case COMBINE_SUM:
                     setValue(i, -getValue(i));
                     break;
             }
         }
     }
 
     /** {@inheritDoc} */
     public CostFunction combine(CostFunction factor, Combine operation) {
 
         // Combination with null factors gives a null / the other factor
         if (factor == null || factor.getSize()==0) {
             return buildCostFunction(this);
         }
         if (this.getSize() == 0) {
             return buildCostFunction(factor);
         }
 
         // Compute the variable set intersection (sets doesn't allow duplicates)
         LinkedHashSet<Variable> vars = new LinkedHashSet<Variable>(variableSet);
         vars.addAll(factor.getVariableSet());
 
         // Perform the combination using the given mode
         CostFunction result = buildCostFunction(vars.toArray(new Variable[0]),
                 operation.getNeutralValue());
 
         Hashtable<Variable, Integer> map = null;
         for (int i=0; i<result.getSize(); i++) {
             map = result.getMapping(i, map);
 
             switch(operation) {
                 case COMBINE_PRODUCT:
                     result.setValue(i, getValue(map) * factor.getValue(map));
                     break;
 
                 case COMBINE_SUM:
                     result.setValue(i, getValue(map) + factor.getValue(map));
                     break;
 
             }
 
         }
 
         return result;
     }
 
     /** {@inheritDoc} */
     public void normalize(Normalize mode) {
         if (mode == Normalize.NORMALIZE_NONE) {
             return;
         }
 
         // Calculate aggregation
         Iterator<Integer> it = iterator();
         double sum = 0;
         while(it.hasNext()) {
             sum += getValue(it.next());
         }
         
         final double dlen = (double)size;
         final double avg = sum / dlen;
         it = iterator();
         switch (mode) {
             case NORMALIZE_SUM0:
                 while(it.hasNext()) {
                     final int i = it.next();
                     setValue(i, getValue(i) - avg);
                 }
                 break;
             case NORMALIZE_SUM1:
                 // Avoid div by 0
                 while(it.hasNext()) {
                     final int i = it.next();
                     final double value = getValue(i);
                     final double v = sum != 0 ? value/sum : 1/dlen;
                     setValue(i, v);
                 }
                 break;
         }
     }
 
     /** {@inheritDoc} */
     public CostFunction reduce(Hashtable<Variable, Integer> mapping) {
         // Calculate the new factor's variables
         LinkedHashSet<Variable> newVariables = new LinkedHashSet<Variable>(variableSet);
         newVariables.removeAll(mapping.keySet());
         if (newVariables.size() == 0) {
             return null;
         }
 
         // Instantiate it
         CostFunction result = buildCostFunction(newVariables.toArray(new Variable[0]));
         Hashtable<Variable, Integer> map = null;
         Iterator<Integer> it = result.iterator();
         for (int i = 0, len = result.getSize(); i < len; i++) {
             map = result.getMapping(i, map);
             map.putAll(mapping);
             final int idx = getIndex(map);
             result.setValue(i, getValue(idx));
         }
         return result;
     }
 
     /** {@inheritDoc} */
     public void setValue(int[] index, double value) {
         setValue(subindexToIndex(index), value);
     }
 
     /**
      * Converts a vector of indices (one for each variable) to the corresponding
      * linearized index of the whole function.
      *
      * @param subindex vector of variable configurations (indices).
      * @return corresponding linearized index.
      */
     protected int subindexToIndex(int[] subindex) {
         // Check index lengths
         if (subindex.length != sizes.length) {
             throw new IllegalArgumentException("Invalid index specification");
         }
         // Compute subindex -> index offset
         int idx = 0;
         for (int i = 0; i < subindex.length; i++) {
             // Check domain limits
             if (variables[i].getDomain() <= subindex[i]) {
                 throw new IllegalArgumentException("Invalid index for dimension " + i);
             }
             idx += sizes[subindex.length - i - 1] * subindex[i];
         }
         return idx;
     }
 
     /** {@inheritDoc} */
     public CostFunction summarize(Variable[] vars, Summarize operation) {
         /*HashSet<Variable> varSet = new HashSet<Variable>(Arrays.asList(vars));
         varSet.retainAll(variableSet);*/
         HypercubeCostFunction result = 
                 new HypercubeCostFunction(vars, operation.getNoGood());
         Hashtable<Variable, Integer> map = null;
         Iterator<Integer> it = iterator();
         while (it.hasNext()) {
             final int i = it.next();
             map = getMapping(i, map);
             final int idx = result.getIndex(map);
             // This value is lost during the summarization
             if (idx < 0) continue;
             result.setValue(idx, operation.eval(getValue(i), result.getValue(idx)));
         }
         return result;
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof CostFunction)) {
             return false;
         }
         final CostFunction other = (CostFunction) obj;
 
        return equals(other, 0.0001);
     }
 
     /**
      * Indicates whether some other factor is "equal to" this one, concerning a
      * delta.
      *
      * @param other the reference object with which to compare.
      * @param delta the maximum delta between factor values for which both
      * numbers are still considered equal.
      * @return  <code>true</code> if this object is the same as the obj
      *          argument; <code>false</code> otherwise.
      */
     public boolean equals(CostFunction other, double delta) {
 
         if (other == null) {
             return false;
         }
 
         if (this.variableSet != other.getVariableSet() &&
                 (this.variableSet == null ||
                 !this.variableSet.equals(other.getVariableSet())))
         {
             return false;
         }
 
         Hashtable<Variable, Integer> map = null;
         for (int i=0; i<size; i++) {
             map = this.getMapping(i, map);
             final double v1 = getValue(i);
             final double v2 = other.getValue(map);
             if (Double.isNaN(v1) ^ Double.isNaN(v2)) {
                 return false;
             }
             final double e = getValue(i) - other.getValue(map);
             if (Math.abs(e) > delta) {
                 return false;
             }
         }
 
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 37 * hash + (this.variableSet != null ? this.variableSet.hashCode() : 0);
         return hash;
     }
 }
