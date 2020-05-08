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
  * de.jwic.ecolib.controls.coledit.IColumnSelectorListener
  * Created on 06.06.2011
  * $Id: IColumnSelectorListener.java,v 1.1 2011/06/06 12:13:19 lordsam Exp $
  */
 package de.jwic.ecolib.controls.coledit;
 
import java.io.Serializable;

 /**
  * Listen to ColumnSelector events.
  * @author lippisch
  */
public interface IColumnSelectorListener extends Serializable {
 
 	/**
 	 * Fired when the column order or visibility was updated.
 	 * @param event
 	 */
 	public void columnsUpdated(ColumnSelectorEvent event);
 	
 }
