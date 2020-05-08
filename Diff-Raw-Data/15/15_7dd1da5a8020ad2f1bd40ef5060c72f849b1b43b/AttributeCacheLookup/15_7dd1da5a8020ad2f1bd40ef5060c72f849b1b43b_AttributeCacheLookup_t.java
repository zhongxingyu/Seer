 /**
  *
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, NCSA.  All rights reserved.
  *
  * Developed by:
  * The Automated Learning Group
  * University of Illinois at Urbana-Champaign
  * http://www.seasr.org
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal with the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject
  * to the following conditions:
  *
  * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimers.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimers in
  * the documentation and/or other materials provided with the distribution.
  *
  * Neither the names of The Automated Learning Group, University of
  * Illinois at Urbana-Champaign, nor the names of its contributors may
  * be used to endorse or promote products derived from this Software
  * without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
  * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
  *
  */
 
 package org.seasr.meandre.components.tools.tuples;
 
 import java.io.BufferedReader;
import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.seasr.datatypes.core.BasicDataTypes.Strings;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 import org.seasr.meandre.support.generic.io.IOUtils;
 import org.seasr.meandre.support.generic.io.PathUtils;
 
 /**
  * @author Boris Capitanu
  */
 
 @Component(
         name = "Attribute Cache Lookup",
         creator = "Boris Capitanu",
         baseURL = "meandre://seasr.org/components/foundry/",
         firingPolicy = FiringPolicy.all,
         mode = Mode.compute,
         rights = Licenses.UofINCSA,
         tags = "sentiment, cache, concept",
         description = "This component looks up the key for a tuple in the ignore file, " +
         		"if specified, and ignores the tuple (passes it on as is) if it's found there, otherwise " +
         		"it looks it up in the cache file and if an attribute is found in cache it is associated with " +
         		"the tuple, otherwise, if a cache miss occurred the tuple is pushed out on the 'tuple_not_cached' " +
         		"port for processing by other means." ,
         dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
 )
 public class AttributeCacheLookup extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TUPLE,
             description = "The tuple" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_TUPLE = Names.PORT_TUPLE;
 
     @ComponentInput(
             name = Names.PORT_META_TUPLE,
             description = "The meta data for the tuple(s)" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = "not_cached_tuple",
             description = "The tuple that was not found in cache, and not ignored" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_TUPLE_NOT_CACHED = "not_cached_tuple";
 
     @ComponentOutput(
             name = "not_cached_meta",
             description = "The meta for the tuple that was not found in cache, and not ignored" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_TUPLE_NOT_CACHED_META = "not_cached_meta";
 
     @ComponentOutput(
             name = Names.PORT_TUPLE,
             description = "The tuple augmented with the attribute found in the cache" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_TUPLE = Names.PORT_TUPLE;
 
     @ComponentOutput(
             name = Names.PORT_META_TUPLE,
             description = "The meta data for the augmented tuple (same as input, plus the new attribute)" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 
     //----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             description = "The cache file where to look up attribute values",
             name = "attribute_cache_file",
             defaultValue = ""
     )
     protected static final String PROP_CACHE = "attribute_cache_file";
 
     @ComponentProperty(
             description = "Optional: The file that stores the keys that should be ignored (for which the tuple will be passed through as-is)",
             name = "ignore_keys_file",
             defaultValue = ""
     )
     protected static final String PROP_IGNORE = "ignore_keys_file";
 
     @ComponentProperty(
             description = "The attribute from the incoming tuple that will be used as key " +
             		"for lookup in the cache and ignore file",
             name = "key",
             defaultValue = ""
     )
     protected static final String PROP_KEY = "key";
 
     @ComponentProperty(
             description = "The attribute to be added to the input tuple that will hold the value from the cache",
             name = "attribute_name",
             defaultValue = ""
     )
     protected static final String PROP_ATTRIBUTE = "attribute_name";
 
     //--------------------------------------------------------------------------------------------
 
 
     protected Map<String, String> _attributeCacheMap = null;
     protected Set<String> _ignoreCacheSet = Collections.synchronizedSet(new HashSet<String>());
     protected String _key;
     protected String _attributeName;
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
         _key = getPropertyOrDieTrying(PROP_KEY, ccp);
         _attributeName = getPropertyOrDieTrying(PROP_ATTRIBUTE, ccp);
 
         String defaultDir = ccp.getPublicResourcesDirectory();
 
         String cacheFile = getPropertyOrDieTrying(PROP_CACHE, ccp);
         _attributeCacheMap = loadCacheData(PathUtils.relativize(new URI(cacheFile), defaultDir));
 
         String ignoreFile = getPropertyOrDieTrying(PROP_IGNORE, true, false, ccp);
         if (ignoreFile.length() > 0)
             _ignoreCacheSet = loadIgnoreData(PathUtils.relativize(new URI(ignoreFile), defaultDir));
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         Strings inTuple = (Strings) cc.getDataComponentFromInput(IN_TUPLE);
         Strings inMetaTuple = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
 
         SimpleTuplePeer inPeer  = new SimpleTuplePeer(inMetaTuple);
         SimpleTuplePeer outPeer = new SimpleTuplePeer(inPeer, new String[] { _attributeName });
 
         int KEY_IDX = inPeer.getIndexForFieldName(_key);
         int ATTRIB_IDX = outPeer.getIndexForFieldName(_attributeName);
 
         if (KEY_IDX == -1)
             throw new ComponentExecutionException(String.format("Incoming tuple has no attributed named '%s'", _key));
 
         SimpleTuple tuple    = inPeer.createTuple();
         SimpleTuple outTuple = outPeer.createTuple();
 
         tuple.setValues(inTuple);
         outTuple.setValue(tuple);
 
         String key = tuple.getValue(KEY_IDX);
         // Look it up in the ignore set
        if (_ignoreCacheSet != null && _ignoreCacheSet.contains(key)) {
             console.fine(String.format("key '%s' found in ignore list", key));
             outTuple.setValue(ATTRIB_IDX, "");
             cc.pushDataComponentToOutput(OUT_TUPLE, outTuple.convert());
             cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
             return;
         }
 
         // Look it up in the cache
        String value = _attributeCacheMap != null ? _attributeCacheMap.get(key) : null;
         if (value == null) {
             // Not found in cache
             console.fine(String.format("key '%s' not cached", key));
             cc.pushDataComponentToOutput(OUT_TUPLE_NOT_CACHED, inTuple);
             cc.pushDataComponentToOutput(OUT_TUPLE_NOT_CACHED_META, inMetaTuple);
             return;
         }
 
         // Found in cache
         console.fine(String.format("key '%s' found in cache with value '%s'", key, value));
         outTuple.setValue(ATTRIB_IDX, value);
 
         cc.pushDataComponentToOutput(OUT_TUPLE, outTuple.convert());
         cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
         _attributeCacheMap = null;
         _ignoreCacheSet = null;
     }
 
     //--------------------------------------------------------------------------------------------
 
     private Set<String> loadIgnoreData(URI ignoreUri) throws IOException {
         Set<String> ignoreSet = new HashSet<String>();
 
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(IOUtils.getReaderForResource(ignoreUri));
             String line;
             while ((line = reader.readLine()) != null)
                 ignoreSet.add(line);
         }
        catch (FileNotFoundException e) {
        	return null;
        }
         finally {
             if (reader != null)
                 reader.close();
         }
 
         return Collections.synchronizedSet(ignoreSet);
     }
 
     private Map<String, String> loadCacheData(URI cacheUri) throws IOException {
         Map<String, String> cacheMap = new HashMap<String, String>();
 
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(IOUtils.getReaderForResource(cacheUri));
             String line;
             while ((line = reader.readLine()) != null) {
                 String[] parts = line.split("\t");
                 if (parts.length < 2)
                     throw new IOException("Invalid cache file format");
 
                 String key = parts[0].trim();
                 String value = parts[1].trim();
                 cacheMap.put(key, value);
             }
         }
        catch (FileNotFoundException e) {
        	return null;
        }
         finally {
             if (reader != null)
                 reader.close();
         }
 
         return Collections.synchronizedMap(cacheMap);
     }
 }
