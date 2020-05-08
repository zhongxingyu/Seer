 /*******************************************************************************
  * Copyright (c) 2008 Nigel Westbury and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Nigel Westbury - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.babel.runtime.external;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.ui.forms.widgets.Hyperlink;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 
 public class TranslatableSet implements ITranslatableSet {
 
 	/**
 	 * All messages added to this object must come from bundles that have the same
 	 * locale.  This is the locale of messages in this collection.
 	 */
 	private Locale locale;
 	
 	protected Map<Object, TranslatableTextInput> localizableTextCollection = new HashMap<Object, TranslatableTextInput>();	
 	protected ArrayList<Object> controlOrder = new ArrayList<Object>();	
 
 	public TranslatableSet() {
 		this.locale = Locale.getDefault();
 	}
 	
 	public TranslatableTextInput[] getLocalizedTexts() {
 		/*
 		 * We need to get the values from the map, but return them in the order
 		 * in which the controls were originally added. This ensures a more
 		 * sensible order.
 		 */
 		TranslatableTextInput[] result = new TranslatableTextInput[controlOrder.size()];
 		int i = 0;
 		for (Object controlKey: controlOrder) {
 			result[i++] = localizableTextCollection.get(controlKey); 
 		}
 		return result;
 	}
 
 	public void layout() {
 		// This default implementation does nothing.
 		// Override this method to re-calculate layouts that may
 		// be affected by changes to the text values in this set.
 	}
 	
 	public void associate(Object controlKey, TranslatableTextInput textInput) {
 		textInput.getLocalizedTextObject().validateLocale(locale);
 		
 		if (!controlOrder.contains(controlKey)) {
 			controlOrder.add(controlKey);
 		}
 		localizableTextCollection.put(controlKey, textInput);
 	}
 
 	public void associate(final Label label, ITranslatableText localizableText) {
 		associate(label, new TranslatableTextInput(localizableText) {
 			@Override
 			public void updateControl(String text) {
 				label.setText(text);
 			}
 		}); 
 	}
 
 	public void associate(final Button button, ITranslatableText localizableText) {
		if (localizableText.getLocalizedText().equals("adde....")) {
			System.out.println("here");
		}
 		associate(button, new TranslatableTextInput(localizableText) {
 			@Override
 			public void updateControl(String text) {
 				button.setText(text);
 			}
 		}); 
 	}
 
 	public void associateToolTip(final Button button, ITranslatableText localizableText) {
 		associate(new ToolTipKey(button), new TranslatableTextInput(localizableText) {
 			@Override
 			public void updateControl(String text) {
 				button.setToolTipText(text);
 			}
 		}); 
 	}
 
 	public void associate(final Item item, ITranslatableText localizableText) {
 		associate(item, new TranslatableTextInput(localizableText) {
 			@Override
 			public void updateControl(String text) {
 				item.setText(text);
 			}
 		}); 
 	}
 
 	public void associate(final Shell shell, ITranslatableText localizableText) {
 		associate(shell, new TranslatableTextInput(localizableText) {
 			@Override
 			public void updateControl(String text) {
 				shell.setText(text);
 			}
 		}); 
 	}
 
 	public void associate(final TabItem tabItem, ITranslatableText localizableText) {
 		associate(tabItem, new TranslatableTextInput(localizableText) {
 			@Override
 			public void updateControl(String text) {
 				tabItem.setText(text);
 			}
 		}); 
 	}
 
 	public void associate(final ScrolledForm form, ITranslatableText localizableText) {
 		associate(form, new TranslatableTextInput(localizableText) {
 			@Override
 			public void updateControl(String text) {
 				form.setText(text);
 			}
 		}); 
 	}
 
 	public void associate(final Section section, ITranslatableText localizableText) {
 		associate(section, new TranslatableTextInput(localizableText) {
 			@Override
 			public void updateControl(String text) {
 				section.setText(text);
 			}
 		}); 
 	}
 
 	public void associateDescription(final Section section, ITranslatableText localizableText) {
 		associate(new DescriptionKey(section), new TranslatableTextInput(localizableText) {
 			@Override
 			public void updateControl(String text) {
 				section.setDescription(text);
 			}
 		}); 
 	}
 
 	public void associate(final Hyperlink link, ITranslatableText localizableText) {
 		associate(link, new TranslatableTextInput(localizableText) {
 			@Override
 			public void updateControl(String text) {
 				link.setText(text);
 			}
 		}); 
 	}
 
 	//	public void associate(
 //			FormPage formPage, ITranslatableText localizableText) {
 //		associate(formPage, new TranslatableTextInput(localizableText) {
 //			@Override
 //			public void updateControl(String text) {
 //				formPage.setPartName(text);
 //			}
 //		}); 
 //	}
 
 	public Locale getLocale() {
 		return locale;
 	}
 
 	/**
 	 * This method should be used to register translatable text that does
 	 * not appear directly in the part/dialog.  For example, text that appears
 	 * in a message dialog.  Such text does not have to be updated in the controls
 	 * so long as it is re-read every time it is about to be displayed.
 	 *  
 	 * @param localizableText
 	 */
 	public void associate(ITranslatableText localizableText) {
 		associate(new Object(), new TranslatableTextInput(localizableText) {
 			@Override
 			public void updateControl(String text) {
 				// do nothing
 			}
 		}); 
 	}
 
 	/**
 	 * This method should be used to register translatable text that does
 	 * not appear directly in the part/dialog.  For example, text that appears
 	 * in a tooltip.  This method should be used only if the tooltip is built each
 	 * time, such as a tooltip in a table.
 	 * 
 	 * Although every cell in a column has a different tooltip, they all have tooltips
 	 * built in the same way from the same templates.  Therefore the tooltips for a column
 	 * should appear just once.  However, we update the sample data so that the user
 	 * always sees the last toolip shown.
 	 *  
 	 * @param localizableText
 	 */
 	public void associate2(Object control, ITranslatableText localizableText) {
 		associate(control, new TranslatableTextInput(localizableText) {
 			@Override
 			public void updateControl(String text) {
 				// do nothing
 			}
 		}); 
 	}
 	
 	/**
 	 * This class is used to form a key that identifies the tooltip
 	 * for a control.  Normally the control itself is used as the key.
 	 * However that would cause a conflict with the primary text for
 	 * the control, hence the need to use this wrapper as a key for
 	 * the tooltip.
 	 */
 	class ToolTipKey {
 		private Object control;
 		
 		ToolTipKey(Object control) {
 			this.control = control;
 		}
 		
 		@Override
 		public boolean equals(Object other) {
 			return other instanceof ToolTipKey
 				&& ((ToolTipKey)other).control.equals(control);
 		}
 		
 		@Override
 		public int hashCode() {
 			return control.hashCode();
 		}
 	}
 	
 	/**
 	 * This class is used to form a key that identifies the description for a
 	 * section. The section itself is used as the key for the title. Therefore
 	 * we cannot use the section as a key for the description because that would
 	 * cause a conflict. Hence the need to use this wrapper as a key for the
 	 * description.
 	 */
 	class DescriptionKey {
 		private Object control;
 		
 		DescriptionKey(Object control) {
 			this.control = control;
 		}
 		
 		@Override
 		public boolean equals(Object other) {
 			return other instanceof ToolTipKey
 				&& ((ToolTipKey)other).control.equals(control);
 		}
 		
 		@Override
 		public int hashCode() {
 			return control.hashCode();
 		}
 	}
 }
