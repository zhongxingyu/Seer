 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.meta.parameters;
 
 import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
 
 import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataXMLWriter;
 import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataParserFactory;
 
 
 import java.io.*;
 import java.util.Hashtable;
 import java.util.Vector;
 
 /**
  * Represents a set of String keys, and associated parameter objects that specify 
  * various parameters for the analysis and their values.
  * 
  * @author Colin J. Fuller
  *
  */
 
 public class ParameterDictionary implements Serializable {
 
 	public static final long serialVersionUID=1495412129L;
 		
 	private Hashtable<String, java.util.List<Parameter> > parameters;
 
 	private ParameterDictionary(){}
 
 
     /**
      * Copy constructor.
      *
      * This will create a new ParameterDictionary using the key, value pairs in another ParameterDictionary.
      *
      * This will make a deep copy.
      *
      * @param toCopy    The ParameterDictionary whose keys/values are to be copied.
      */
 	public ParameterDictionary(ParameterDictionary toCopy) {
 		
 		this.parameters = new Hashtable<String, java.util.List<Parameter> >();
 		
 		for (String key : toCopy.parameters.keySet()) {
 			
 			java.util.List<Parameter> pl = toCopy.parameters.get(key);
 			
 			java.util.List<Parameter> newPl = new java.util.ArrayList<Parameter>();
 			
 			for (Parameter p : pl) {
 				newPl.add(new Parameter(p));
 				
 			}
 			
 			this.parameters.put(key, newPl);
 			
 		}
 	
 	}
 
 
     /**
      * Factory method to create a new empty dictionary.
      *
      * @return  A new ParameterDictionary whose internal storage has been initialized but contains no entries.
      */
     public static ParameterDictionary emptyDictionary() {
 
         ParameterDictionary parameters = new ParameterDictionary();
 
 		parameters.parameters = new Hashtable<String, java.util.List<Parameter> >();
 
         return parameters;
     }
 
 
 
     private static void parseSingleParameter(ParameterDictionary parameters, String currArg) {
 
         if (currArg.isEmpty() || currArg.startsWith("#")) {return;}
 
         if (! (currArg.matches("[A-Za-z0-9_\\-\\.\\/\\\\: ]+=[A-Za-z0-9_\\-\\.\\/\\\\:\\, ]*"))){
 
 			LoggingUtilities.getLogger().warning("Malformed argument: " + currArg + ".  Ignoring.");
 
 		} else {
 
 			String[] splitArg = currArg.split("=", -1);
 
 			if (splitArg[1].toLowerCase().equals("true")) {
 
 				splitArg[1] = "true";
 
 			} else if (splitArg[1].toLowerCase().equals("false")) {
 
 				splitArg[1] = "false";
 
 			}
 
                if (parameters.hasKey(splitArg[0])) {
                    String temp = parameters.getValueForKey(splitArg[0]);
                    temp += " " + splitArg[1];
                    splitArg[1] = temp;
                }
 
                parameters.setValueForKey(splitArg[0], splitArg[1]);
 
 		}
     }
 	
 	//public instance methods
 
     /**
      * Gets the value stored for a given key.  Returns null, as per the specification in java.util.Hashtable if there is
      * no value for key stored.
      * 
      * @param key       The key whose associated value is to be returned.
      * @return          The corresponding value, in String format.
      */
 	public String getValueForKey(String key) {
 	
 		return this.getValueForKey(key, 0);
 		
 	}
 	
 	/**
 	* Gets the ith value stored for a given key.  Does not check bounds, so check the number of
	* values stored using {@link getValueCountForKey(String)} beforehand.
 	* @param key 	The key whose associated value is to be returned.
 	* @param i 		The index of the value associated with this key to return.
 	* @return 		The value of the parameter as a String, or null if there is no value stored.
 	*/
 	public String getValueForKey(String key, int i) {
 		java.util.List<Parameter> pl = this.parameters.get(key);
 		
 		if (pl == null) {
 			return null;
 		}
 		
 		return pl.get(i).stringValue();
 	}
 	
 	/**
 	* Gets the ith value stored for a given key.  Does not check bounds, so check the number of
	* values stored using {@link getValueCountForKey(String)} beforehand.
 	* @param key 	The key whose associated value is to be returned.
 	* @param i 		The index of the value associated with this key to return.
 	* @return 		The Parameter object associated with the key, or null if there is no value stored.
 	*/
 	public Parameter getParameterForKey(String key, int i) {
 		java.util.List<Parameter> pl = this.parameters.get(key);
 		
 		if (pl == null) {
 			return null;
 		}
 		
 		return pl.get(i);
 	}
 
 	/**
 	* Gets the number of parameters associated with a given key.  
 	* @param key 	The key whose associated value count is to be returned.
 	* @return 		The Parameter object associated with the key, or null if there is no value stored.
 	*/
 	public int getValueCountForKey(String key) {
 		java.util.List<Parameter> pl = this.parameters.get(key);
 		
 		if (pl == null) {
 			return 0;
 		}
 		
 		return pl.size();
 	}
 	
 
 
     /**
      * Gets the value stored for a given key as an int.
      *
      * Does not perform type-checking on the conversion from String to int.
      *
      * @param key       The key whose associated value is to be returned.
      * @return          The corresponding value, as an int.
      */
     public int getIntValueForKey(String key) {
         return Integer.parseInt(getValueForKey(key));
     }
 
     /**
      * Gets the value stored for a given key as a boolean.
      *
      * Does not perform type-checking on the conversion from String to boolean.
      *
      * @param key       The key whose associated value is to be returned.
      * @return          The corresponding value, as a boolean.
      */
     public boolean getBooleanValueForKey(String key) {
         return Boolean.parseBoolean(getValueForKey(key));
     }
 
     /**
      * Gets the value stored for a given key as a double.
      *
      * Does not perform type-checking on the conversion from String to double.
      *
      * @param key       The key whose associated value is to be returned.
      * @return          The corresponding value, as a double.
      */
     public double getDoubleValueForKey(String key) {
         return Double.parseDouble(getValueForKey(key));
     }
 
     /**
      * Convenience method for checking if the ParameterDictionary has a key and that the corresponding value is true.
      * 
      * @param key       The key to be checked.
      * @return          The logical and of hasKey(key) and getBooleanValueForKey(key).
      */
     public boolean hasKeyAndTrue(String key) {
         return this.hasKey(key) && this.getBooleanValueForKey(key);
     }
 
 
     /**
      * Adds a key, value pair to the ParameterDictionary.  Does nothing if the specified key already has an associated
      * value.
      *
      * This method is useful for setting up default values after parsing user-specified options, as the defaults will
      * not overwrite the existing values.
      *
      * @param key       The key to add to the ParameterDictionary if it is not present.
      * @param value     The key's associated String value.  A new Parameter object will be constructed.
      */
 	public void addIfNotSet(String key, String value) {
 	
 		this.addIfNotSet(key, this.parameterForStringValue(key, value));
 	
 		return;
 	
 	}
 	
 	/**
      * Adds a key, value pair to the ParameterDictionary.  Does nothing if the specified key already has an associated
      * value.
      *
      * This method is useful for setting up default values after parsing user-specified options, as the defaults will
      * not overwrite the existing values.
      *
      * @param key       The key to add to the ParameterDictionary if it is not present.
      * @param value     The key's associated Parameter.
      */
 	public void addIfNotSet(String key, Parameter value) {
 	
 		if (! this.parameters.containsKey(key) ) {
 		
 			this.addValueForKey(key, value);
 		
 		}
 	
 		return;
 	
 	}
 
 
     /**
      * Removes all key,value pairs from the ParameterDictionary that are not listed in a specified array of valid keys.
      *
      * @param legalArguments    A String array containing the valid keys; any key, value pair not matching one of these keys will be discarded.
      */
 	public void discardIllegalArguments(String[] legalArguments) {
 		
 		Vector<String> toRemove = new Vector<String>();
 		
 		for (String key : this.parameters.keySet()) {
 		
 			boolean legal = false;
 		
 			for (int i = 0; i < legalArguments.length; i++) {
 			
 				if (legalArguments[i].equals( key)) {
 				
 					legal = true;
 					break;
 				
 				}
 			
 			}
 			
 			if (!legal) {
 			
 				toRemove.add(key);
 			
 			}
 		
 		}
 		
 		for (String key : toRemove) {
 		
 			this.parameters.remove(key);
 		
 		}
 		
 		return;
 		
 	}
 
     /**
      * Checks if the ParameterDictionary contains a specified key.
      *
      * @param key   A String that is the key to check for in this ParameterDictionary.
      * @return      A boolean corresponding to whether the specified key has an associated value.
      */
 	public boolean hasKey(String key) {
 	
 		return this.parameters.containsKey(key);
 	
 	}
 
 
     /**
      * Adds a key, value pair to the ParameterDictionary.
      *
      * This is distinct from {@link #addIfNotSet(String, String)} in that this method will overwrite any existing value.
      *
      * @param key   The key to add to the ParameterDictionary.
      * @param value The value associated with the key, which will overwrite any existing value.
      */
 	public void setValueForKey(String key, String value) {
 	
 		this.setValueForKey(key, this.parameterForStringValue(key, value));
 	
 	}
 	
 	/**
      * Adds a key, value pair to the ParameterDictionary.
      *
      * This is distinct from {@link #addIfNotSet(String, String)} in that this method will add an additional value for a key if there is one already present.
      *
      * @param key   The key to add to the ParameterDictionary.
      * @param value The Parameter associated with the key
      */
 	public void addValueForKey(String key, Parameter value) {
 	
 		if (this.parameters.get(key) == null) {
 			this.parameters.put(key, new java.util.ArrayList<Parameter>());
 		}
 	
 		this.parameters.get(key).add(value);
 	
 	}
 
 	/**
      * Sets a key, value pair in the ParameterDictionary.
      *
      * This is distinct from {@link #addIfNotSet(String, String)} in that this method will overwrite existing values.
      *
      * @param key   The key to add to the ParameterDictionary.
      * @param value The Parameter associated with the key
      */
 	public void setValueForKey(String key, Parameter value) {
 		
 		java.util.List<Parameter> pl = new java.util.ArrayList<Parameter>();
 		
 		pl.add(value);
 		
 		this.parameters.put(key, pl);
 		
 	}
 
     /**
      * Writes the ParameterDictionary's contents to a specified file in plaintext format.
      * 
      * @param filename  The full filename (including path); any existing file with this name will be overwritten without confirmation.
      * @throws java.io.IOException  If an IOException occurs while opening or writing to the file.
      * @deprecated use an AnalysisMetadata object and write that instead
      */
 	@Deprecated
 	public void writeParametersToFile(String filename) throws java.io.IOException {
 
         (new AnalysisMetadataXMLWriter()).writeParameterDictionaryToXMLFile(this, filename);
 		
 		System.out.println(filename);
 		
 		return;
 
 	}
 
     /**
      * Writes the ParameterDictionary's contents to a String as XML suitable for inclusion as a section in a file.
      * 
      * @return a String containing the XML representation of the ParameterDictionary.
      * 
      */
 	public String writeParametersToXMLString() {
 		return (new AnalysisMetadataXMLWriter()).writeParameterDictionaryToXMLString(this);
 	}
 
 	/**
      * Reads a set of parameters from an XML file.
      * @param filename  The XML file containing the parameter information.
      * @return          A ParameterDictionary containing the parameters parsed from the specified file.
      * @deprecated use an AnalysisMetadata object instead
      */
 	@Deprecated
     public static ParameterDictionary readParametersFromFile(String filename) {
         return AnalysisMetadataParserFactory.createParserForFile(filename).parseFileToParameterDictionary(filename);
     }
 
 
     /**
      * Adds a Parameter object to the parameter dictionary.
      * @param p     The Parameter to add.
      */
     public void addParameter(Parameter p) {
     	
         this.addValueForKey(p.getName(), p);
     }
 
     /**
      * Gets a set containing the names of all the parameters in the ParameterDictionary.
      * @return  The Set containing the names.
      */
     public java.util.Set<String> getKeys() {
         return this.parameters.keySet();
     }
 
     /**
      * Gets the Parameter type for a given parameter name as one of the values of the static fields in the Parameter class.
      * @param key   The name of the parameter.
      * @return      A ParameterType corresponding to the type of the parameter.
      */
     public ParameterType getTypeForKey(String key) {
         return this.getTypeForKey(key, 0);
     }
 
 	/**
 	 * Gets the Parameter type for a given parameter name as one of the values of the static fields in the Parameter class.
 	 * @param key   The name of the parameter.
 	 * @param i 	The index of the value associated with this key whose type will be retrieved.
 	 * @return      A ParameterType corresponding to the type of the parameter. 
 	 */
     public ParameterType getTypeForKey(String key, int i) {
         return this.parameters.get(key).get(i).getType();
     }
 
 	private Parameter parameterForStringValue(String key, String value) {
 		return new Parameter(key, key, ParameterType.STRING_T, value, null);
 	}
 	
 
 }
 	
 
