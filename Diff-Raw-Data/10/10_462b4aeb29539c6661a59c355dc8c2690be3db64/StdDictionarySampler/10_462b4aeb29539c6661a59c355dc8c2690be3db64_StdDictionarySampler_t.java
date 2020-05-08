 /****************************************************************************************
  * Copyright (C) 2010,2011 The Hebrew University of Jerusalem, 
  * Department of Computer Science and Engineering
  * Project: GRR - Generating Random RDF
  * Author:	Daniel Blum, The Hebrew University of Jerusalem, 
  * Department of Computer Science and Engineering, http://www.cs.huji.ac.il/~danieb12/
  *
  * This file is part of GRR.
  * GRR is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * Please note that no part of this code is officially supported by HUJI
  * (The Hebrew University of Jerusalem) or any of its developers.
  *
  *  GRR is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with GRR (COPYRIGHT-gpl.txt).  If not, see <http://www.gnu.org/licenses/>.
  *
  ****************************************************************************************/
 
 package JavaApi.Samplers.DictionarySamplers;
 
 
 import JavaApi.Samplers.NumberSamplers.NaturalNumberSamplers.*;
 import JavaApi.Samplers.SamplingMode;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 /**
  * A standard implementation of the IDictionarySampler interface which uses the Java
  * standard random numbers generation for selecting a specific value
  * from the dictionary that was loaded by the invocation of init().
  * This class can be used to return values with or without repetitions from the set of
  * values loaded in the the init() method
  */
 public class StdDictionarySampler implements IDictionarySampler {
 
     /**
      * Consts
      */
 
     private static final String TXT_FILE = "txt";
 
     /**
      * Class Members
      */
     private boolean _isInitialized;
     private IStdNaturalNumberSampler _nSampler;
     private HashMap<Integer, String> _labelValues;
     private SamplingMode _samplingMode;
     private NoRepetitionMode _nSamplerMode;
 
     /**
      * Default constructor that sets the sampling mode to be NoRepetitionMode.HASE_SET_WITH_REMAINING_NUMBERS
      */
     public StdDictionarySampler() {
         _isInitialized = false;
         _labelValues = new HashMap<Integer, String>();
         _samplingMode = SamplingMode.RANDOM_GLOBAL_DISTINCT;
         _nSamplerMode = NoRepetitionMode.HASE_SET_WITH_REMAINING_NUMBERS;
     }
 
     /**
      * Constructor that sets the NoRepetitionMode to be the given mode
      * @param nSamplerMode - The NoRepetitionMode that will be set for this instance 
      */
     public StdDictionarySampler(NoRepetitionMode nSamplerMode)
     {
         this();
         _nSamplerMode = nSamplerMode;
     }
 
     /**
      * IDictionarySampler - Interface Implementation
      */
 
     /**
      * Initializes this instance with the data found in the given file. Invoking this methods will un-initialize any
      * previous invocation of any of the init methods.
      * @param fileName - A file containing the data that this instance will use for returning random values. Currently
      * only .txt file in a specific format is supported (see additional documentation).
      */
     @Override
     public void init(String fileName) throws IOException {
 
         HashMap<Integer, String> hashMap = new HashMap<Integer, String>();
         HashSet<String> set = new HashSet<String>();
 
         // Decide what is the file type
        String[] fileParts = fileName.split("\\.");
         String fileTypeName = fileParts[fileParts.length-1].toLowerCase();
 
         // Parse the file according to it's type
         if (fileTypeName.equals(TXT_FILE)) {
             BufferedReader br = new BufferedReader(new FileReader(fileName));
             String value;
             int index = 0;
             while ((value = br.readLine()) != null) {
                 if (set.contains(value))
                     throw new IllegalArgumentException("File contains duplicated values! Example: " + value);
 
                 set.add(value);
                 hashMap.put(index, value);
                 index++;
             }
             br.close();
         } else
             throw new IllegalArgumentException("File type isn't supported");
 
         init(hashMap);
     }
 
     /**
      * Returns true iff the Dictionary Sampler was initialized by invoking one of the init() methods.
      * @return - True iff the Dictionary Sampler was initialized by invoking one of the init() methods.
      */
     @Override
     public boolean isInitialized() {
         return _isInitialized;
     }
 
     /**
      * Returns a random label value from the dictionary that was supplied in the init() method.
      * The returned values might be with or without repetitions according to the sampling mode that was set.
      * Default is without repetitions, which means that if all values were returned the instance becomes uninitialized.
      * @return - A random label value from the dictionary that was supplied in the init() method.
      * @throws IllegalStateException - If the instance wasn't initialized by an invocation of init().
      */
     @Override
     public String getRandomLabel() throws IllegalStateException {
 
         if (!_isInitialized)
             throw new IllegalStateException("Instance wasn't initialized by calling init()");
 
         if (!hasNext())
         {
             _isInitialized = false;
             throw new IllegalStateException("d-sampler doesn't have any values left");
         }
 
         int index;
         String label;
 
         switch (_samplingMode) {
             case RANDOM_GLOBAL_DISTINCT:
                 index = _nSampler.getNextNatural();
                 label = _labelValues.get(index);
                 if (!hasNext()) // No more values left in the list, so the instance becomes uninitialized
                     _isInitialized = false;
                 break;
             case RANDOM_REPEATABLE:
                 index = _nSampler.getNextNatural();
                 label = _labelValues.get(index);
                 break;
             default:
                 throw new IllegalArgumentException("The current sampling mode isn't supported: " + _samplingMode);
         }
 
         return label;
     }
 
     /**
      * Additional Public Methods
      */
 
     /**
      * Initializes this instance with the given hash-map. Invoking this methods will re-initialize any previous
      * invocation of any of the init methods.
      * @param hashMap - The data that this instance will use for returning random values.
      */
     public void init(HashMap<Integer, String> hashMap) {
 
         _labelValues.clear();
         _labelValues.putAll(hashMap);
         initNSampler();
         _isInitialized = true;
     }
 
     /**
      * Initializes this instance with the given array list. Invoking this methods will re-initialize any previous
      * invocation of any of the init methods.
      * @param arrayList - The data that this instance will use for returning random values.
      */
     public void init(ArrayList<String> arrayList) {
 
         HashMap<Integer, String> hashMap = new HashMap<Integer, String>();
         for (int i = 0; i < arrayList.size(); i++)
             hashMap.put(i, arrayList.get(i));
 
         init(hashMap);
     }
 
     /**
      * Sets the sampling mode that is being used in this instance.
      * (With/Without repetition)
      * @param mode - The new sampling mode which will be set for this instance 
      */
     public void setSamplingMode(SamplingMode mode) {
         _samplingMode = mode;
     }
 
     /**
      * Returns true iff this instance has more values to return (helpful in case that we are using a non-repeatable mode)
      * @return - true iff this instance has more values to return (helpful in case that we are using a non-repeatable
      * mode)
      */
     public boolean hasNext() {
          switch (_samplingMode) {
             case RANDOM_GLOBAL_DISTINCT:
                 return ((NumberSamplerNoRep)_nSampler).hasNextNumber();    
             case RANDOM_REPEATABLE:
                 return true;
             default:
                 throw new IllegalArgumentException("The current sampling mode isn't supported: " + _samplingMode);
         }
     }
 
     /**
      * Internal Private Methods
      */
 
     /**
      * Helper method for initializing the instance according to the sampling mode chosen 
      */
     private void initNSampler() {
         switch (_samplingMode) {
             case RANDOM_GLOBAL_DISTINCT:
                 NumberSamplerNoRep sampler = NumberSamplerNoRepFactory.getNoRepNumberSampler(_nSamplerMode);
                 sampler.setMaxValue(_labelValues.size() - 1);
                 sampler.init();
                 _nSampler = sampler;
                 break;
             case RANDOM_REPEATABLE:
                 IStdNaturalNumberSampler samplerWithRep = new StdNaturalNumberSampler();
                 samplerWithRep.setMaxValue(_labelValues.size());
                 _nSampler = samplerWithRep;
                 break;
             default:
                 throw new IllegalStateException("Dictionary sampler currently doesn't support the given sampling mode: " + _samplingMode);
         }
     }
 }
