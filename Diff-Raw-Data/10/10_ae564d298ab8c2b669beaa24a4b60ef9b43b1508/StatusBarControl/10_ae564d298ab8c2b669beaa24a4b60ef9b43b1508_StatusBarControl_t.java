 /*
  * Copyright 2007 jWic group (http://www.jwic.de)
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
  * de.jwic.ecolib.tableviewer.StatusBarControl.java
  * Created on Apr 5, 2007
  * $Id: StatusBarControl.java,v 1.4 2010/05/11 13:21:17 lordsam Exp $
  * @author jbornema
  */
 
 package de.jwic.controls.tableviewer;
 
 import java.text.MessageFormat;
 import java.util.Arrays;
 
 import de.jwic.base.ControlContainer;
 import de.jwic.base.IControlContainer;
 import de.jwic.controls.ListBox;
 import de.jwic.events.ElementSelectedEvent;
 import de.jwic.events.ElementSelectedListener;
 
 /**
  * Created on Apr 5, 2007
  * @author jbornema
  */
 public class StatusBarControl extends ControlContainer {
 	private static final long serialVersionUID = 1L;
 	private ListBox lbcMaxLines;
 	private PagingControl ctrlPaging;
 
 	private boolean enabled = true;
 	
 	public StatusBarControl(IControlContainer container, TableModel model) {
 		this(container, null, model);
 	}
 
 	public StatusBarControl(final IControlContainer container, String name, final TableModel model) {
 		super(container, name);
 
 		// add pagingControl
 		ctrlPaging = new PagingControl(this, "paging", model);
 		
 		// add MaxLines control
 		lbcMaxLines = new ListBox(this, "lbcMaxLines");
		lbcMaxLines.addElement("- Auto -", "0");
 		// add elements
 		int[] choices = {5, 10, 15, 25, 50, 100};
 		String msg = "{0} rows per page";
 		for (int i = 0; i < choices.length; i++) {
 			lbcMaxLines.addElement(
 					MessageFormat.format(
 							msg, 
 							new Object[] { new Integer(choices[i]) }), 
 					Integer.toString(choices[i]));
 		}
 		lbcMaxLines.addElement("- All -", "-1");
 		lbcMaxLines.addElementSelectedListener(new ElementSelectedListener() {
 			private static final long serialVersionUID = 1L;
 			public void elementSelected(ElementSelectedEvent event) {
 				String maxLineSelection = (String)event.getElement();
 				if(maxLineSelection != null && maxLineSelection.length() > 0)
 					model.setMaxLines(Integer.parseInt(maxLineSelection));
 			}
 		});
 		lbcMaxLines.setChangeNotification(true);
 		
 		// add tableModelListener to flag the control "requireRedraw" when
 		// the range has been modified.
 		model.addTableModelListener(new TableModelAdapter() {
 			private static final long serialVersionUID = 1L;
 			public void rangeUpdated(TableModelEvent event) {
 				container.setRequireRedraw(true);
 				String key = Integer.toString(model.getRange().getMax());
 				if (!key.equals(lbcMaxLines.getSelectedKey()) && lbcMaxLines.getSelectedKeys() != null && Arrays.asList(lbcMaxLines.getSelectedKeys()).contains(key)) {
 					lbcMaxLines.setSelectedKey(key);
 				}
 			}
 		});
 	}
 
 	/**
 	 * @return the PagingControl
 	 */
 	public PagingControl getPagingControl() {
 		return ctrlPaging;
 	}
 	
 	/**
 	 * @return the MaxLinesControl
 	 */
 	public ListBox getMaxLinesControl() {
 		return lbcMaxLines;
 	}
 	
 	/**
 	 * @return the enabled
 	 */
 	public boolean isEnabled() {
 		return enabled;
 	}
 	
 	/**
 	 * @param enabled the enabled to set
 	 */
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 		lbcMaxLines.setEnabled(enabled);
 		ctrlPaging.setEnabled(enabled);
 	}
 	
 }
