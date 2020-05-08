 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb;
 
 public class RowBatch {
 	public boolean selectedInUse;
 	public int[] selected;
 	public int size;
 	public Row[] rows;

	/**
	 * @since 2.4.48
	 */
	public RowBatch rebuild() {
		RowBatch c = new RowBatch();
		c.size = size;
		c.rows = new Row[size];

		if (selectedInUse) {
			for (int i = 0; i < size; i++)
				c.rows[i] = rows[selected[i]].clone();
		} else {
			for (int i = 0; i < size; i++)
				c.rows[i] = rows[i].clone();
		}

		return c;
	}
 }
