 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.desktop.impl.dialogues.settings;
 
import java.util.Arrays;
 import java.util.HashMap;
 
 import org.osgi.service.metatype.AttributeDefinition;
 
 abstract class AbstractMultiAttrConfig extends AbstractAttrConfig<Object> {
 	
 	protected final String[] labels;
 	protected final String[] values;
 	protected final HashMap<String,Integer> valMap = new HashMap<String,Integer>();
 	
 	protected AbstractMultiAttrConfig(final AttributeDefinition ad) {
 		super(ad);
 		labels = ad.getOptionLabels();
 		values = ad.getOptionValues();
 		for (int i=0; i<values.length; i++)
 			valMap.put(values[i].intern(), Integer.valueOf(i));
 	}
 	
 	@Override
 	protected Object fromObject(Object value) {
 		return valMap.get(value);
 	}
 	
 	@Override
 	protected int[] normalize(Object currentValue) {
 		final Object value = currentValue; // super.normalize(currentValue);
 		
 		final int[] selectedIndices;
 		if (value == null) {
			if (logger.isDebugEnabled()) {
				final String stacktrace = Arrays.toString(new Throwable().getStackTrace()).replace(',', '\n');
				logger.warn("normalizing null-value for '" + ad.getID() + "' from\n" + stacktrace.substring(1, stacktrace.length() - 1));
			}
			final String[] defs = ad.getDefaultValue();
			if (defs != null) {
				logger.info("falling back to default settings for " + ad.getID());
				return normalize(defs);
			}
			logger.warn("no default value set for " + ad.getID());
 			selectedIndices = new int[1];
 		} else if (value.getClass().isArray()) {
 			final Object[] curVals = (Object[])value;
 			// get selected indices
 			selectedIndices = new int[curVals.length];
 			int i = 0;
 			for (final Object val : curVals) {
 				final Integer idx = valMap.get(((val instanceof String) ? (String)val : val.toString()).intern());
 				if (idx != null)
 					selectedIndices[i++] = idx.intValue();
 			}
 		} else {
 			selectedIndices = new int[1];
 			final Integer idx = valMap.get(((value instanceof String) ? (String)value : value.toString()).intern());
 			if (idx != null)
 				selectedIndices[0] = idx.intValue();
 		}
 		return selectedIndices;
 	}
 }
