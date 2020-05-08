 /*
  * Copyright 2005-2007 jWic group (http://www.jwic.de)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * de.jwic.controls.combo.ComboElementContentProvider
  * Created on Jan 9, 2010
  * $Id: SelectElementContentProvider.java,v 1.1 2010/04/22 16:00:10 lordsam Exp $
  */
 package de.jwic.data;
 
 import java.util.List;
 
 import de.jwic.util.Util;
 
 
 /**
  * Content provider for IComboElement objects. This provider is based upon the ListContentProvider and
  * uses the IComboElement.key property as key if specified. If not, it uses the list index as a fallback.
  * @author lippisch
  */
 public class SelectElementContentProvider extends ListContentProvider<ISelectElement> {
 	private static final long serialVersionUID = 1L;
 	/**
 	 * @param list
 	 */
 	public SelectElementContentProvider(List<ISelectElement> list) {
 		super(list);
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.controls.combo.ListContentProvider#getUniqueKey(java.lang.Object)
 	 */
 	@Override
 	public String getUniqueKey(ISelectElement object) {
 		if (object.getKey() != null) {
 			return object.getKey();
 		} else {
 			int idx = data.indexOf(object);
 			return Integer.toString(idx);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.jwic.controls.combo.ListContentProvider#getObjectFromKey(java.lang.String)
 	 */
 	@Override
 	public ISelectElement getObjectFromKey(String uniqueKey) {
 		for (ISelectElement elm : data) {
 			if (uniqueKey.equals(elm.getKey())) {
 				return elm; 
 			}
 		}
		// still not found - is it an Index?
		if (Util.tryParseInt(uniqueKey)) {
			return super.getObjectFromKey(uniqueKey);
		}
 		return null;
 	}
 
 }
