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
  * de.jwic.ecolib.controls.datapicker.DateChangedListener
  * Created on 12.11.2012
  * $Id:$
  */
 package de.jwic.ecolib.controls.datepicker;
 
 import java.util.Date;
 
 /**
  *
  * @author bogdan
  */
public interface DateChangedListener {
 	
 	
 	/**
 	 * 
 	 * @param oldDate - the previous date
 	 * @param newDate - the current date
 	 * <br/>Both can be null
 	 */
 	public void onDateChanged(Date oldDate,Date newDate);
 }
