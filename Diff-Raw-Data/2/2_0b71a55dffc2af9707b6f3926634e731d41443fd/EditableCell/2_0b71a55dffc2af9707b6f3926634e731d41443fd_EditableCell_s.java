 package ch.cern.atlas.apvs.client.widget;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.cell.client.ButtonCell;
 import com.google.gwt.cell.client.CheckboxCell;
 import com.google.gwt.cell.client.DateCell;
 import com.google.gwt.cell.client.EditTextCell;
 import com.google.gwt.cell.client.SelectionCell;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.cell.client.TextInputCell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.text.shared.SafeHtmlRenderer;
 import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
 
 public class EditableCell extends AbstractCell<Object> {
 	private TextInputSizeCell textInputCell;
 	private MyEditTextCell editCell;
 	private MySelectionCell selectionCell;
 	private MyCheckboxCell checkboxCell;
 	private MyButtonCell buttonCell;
 	private MyTextCell textCell;
 	private MyDateCell dateCell;
 	private MyDurationCell durationCell;
 
 	private List<Class<?>> cellClasses;
 	private boolean enabled = true;
 
 	public EditableCell() {
 		this(null);
 	}
 
 	public EditableCell(List<Class<?>> cellClasses) {
 		this(cellClasses, 20);
 	}
 
 	public EditableCell(List<Class<?>> cellClasses, int size) {
 
 		this.cellClasses = cellClasses;
 		if (cellClasses == null) {
 			cellClasses = Collections.emptyList();
 		}
 
 		enabled = true;
 
 		textInputCell = new TextInputSizeCell(size);
 		editCell = new MyEditTextCell();
 		selectionCell = new MySelectionCell();
 		checkboxCell = new MyCheckboxCell();
 		buttonCell = new MyButtonCell();
 		textCell = new MyTextCell();
 		dateCell = new MyDateCell();
 		durationCell = new MyDurationCell();
 
 	}
 
 	public void setOptions(List<String> options) {
 		selectionCell.setOptions(options);
 	}
 
 	public void setDateFormat(DateTimeFormat format) {
 		dateCell.setFormat(format);
 	}
 
 	// FIXME works only for checkbox
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 
 		checkboxCell.setEnabled(enabled);
 	}
 
 	@Override
 	public boolean isEditing(Context context, Element parent, Object value) {
 		Class<?> cellClass = getCellClass(context, value);
 		if (cellClass.equals(TextInputCell.class)) {
 			return textInputCell.isEditing(context, parent, getString(value));
 		} else if (cellClass.equals(EditTextCell.class)) {
 			return editCell.isEditing(context, parent, getString(value));
 		} else if (cellClass.equals(SelectionCell.class)) {
 			return selectionCell.isEditing(context, parent, getString(value));
 		} else if (cellClass.equals(CheckboxCell.class)) {
 			if (value instanceof Boolean) {
 				return checkboxCell.isEditing(context, parent, (Boolean) value);
 			} else {
 				return checkboxCell.isEditing(context, parent,
 						Boolean.valueOf(getString(value)));
 			}
 		} else if (cellClass.equals(ButtonCell.class)) {
 			return buttonCell.isEditing(context, parent, getString(value));
 		} else if (cellClass.equals(DateCell.class)) {
 			return dateCell.isEditing(context, parent, (Date) value);
 		} else if (cellClass.equals(DurationCell.class)) {
 			return durationCell.isEditing(context, parent, (Long) value);
 		} else {
 			return textCell.isEditing(context, parent, getString(value));
 		}
 	}
 
 	private String getString(Object value) {
		return value instanceof String ? (String) value : value.toString();
 	}
 
 	@Override
 	public void onBrowserEvent(Context context, Element parent, Object value,
 			NativeEvent event, final ValueUpdater<Object> valueUpdater) {
 		Class<?> cellClass = getCellClass(context, value);
 
 		if (cellClass.equals(TextInputCell.class)) {
 			textInputCell.onBrowserEvent(context, parent, getString(value),
 					event, new ValueUpdater<String>() {
 						@Override
 						public void update(String value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		} else if (cellClass.equals(EditTextCell.class)) {
 			editCell.onBrowserEvent(context, parent, getString(value), event,
 					new ValueUpdater<String>() {
 						@Override
 						public void update(String value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		} else if (cellClass.equals(SelectionCell.class)) {
 			selectionCell.onBrowserEvent(context, parent, getString(value),
 					event, new ValueUpdater<String>() {
 						@Override
 						public void update(String value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		} else if (cellClass.equals(CheckboxCell.class)) {
 			if (value instanceof Boolean) {
 				checkboxCell.onBrowserEvent(context, parent, (Boolean) value,
 						event, new ValueUpdater<Boolean>() {
 							@Override
 							public void update(Boolean value) {
 								if (valueUpdater != null) {
 									valueUpdater.update(value);
 								}
 							}
 						});
 			} else {
 				checkboxCell.onBrowserEvent(context, parent,
 						Boolean.valueOf(getString(value)), event,
 						new ValueUpdater<Boolean>() {
 							@Override
 							public void update(Boolean value) {
 								if (valueUpdater != null) {
 									valueUpdater.update(value);
 								}
 							}
 						});
 			}
 		} else if (cellClass.equals(ButtonCell.class)) {
 			buttonCell.onBrowserEvent(context, parent, getString(value), event,
 					new ValueUpdater<String>() {
 						@Override
 						public void update(String value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		} else if (cellClass.equals(DateCell.class)) {
 			dateCell.onBrowserEvent(context, parent, (Date) value, event,
 					new ValueUpdater<Date>() {
 						@Override
 						public void update(Date value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		} else if (cellClass.equals(DurationCell.class)) {
 			durationCell.onBrowserEvent(context, parent, (Long) value, event,
 					new ValueUpdater<Long>() {
 						@Override
 						public void update(Long value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		} else {
 			textCell.onBrowserEvent(context, parent, getString(value), event,
 					new ValueUpdater<String>() {
 						@Override
 						public void update(String value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		}
 	}
 
 	@Override
 	public void render(Context context, Object value, SafeHtmlBuilder sb) {
 		Class<?> cellClass = getCellClass(context, value);
 
 		if (cellClass.equals(TextInputCell.class)) {
 			textInputCell.render(context, getString(value), sb);
 		} else if (cellClass.equals(EditTextCell.class)) {
 			editCell.render(context, getString(value), sb);
 		} else if (cellClass.equals(SelectionCell.class)) {
 			selectionCell.render(context, getString(value), sb);
 		} else if (cellClass.equals(CheckboxCell.class)) {
 			if (value instanceof Boolean) {
 				checkboxCell.render(context, (Boolean) value, sb);
 			} else {
 				checkboxCell.render(context, Boolean.valueOf(getString(value)),
 						sb);
 			}
 		} else if (cellClass.equals(ButtonCell.class)) {
 			if (value instanceof SafeHtml) {
 				buttonCell.render(context, (SafeHtml) value, sb);
 			} else {
 				buttonCell.render(context, getString(value), sb);
 			}
 		} else if (cellClass.equals(DateCell.class)) {
 			dateCell.render(context, (Date) value, sb);
 		} else if (cellClass.equals(DurationCell.class)) {
 			durationCell.render(context, (Long) value, sb);
 		} else {
 			if (value instanceof SafeHtml) {
 				textCell.render(context, (SafeHtml) value, sb);
 			} else {
 				textCell.render(context, getString(value), sb);
 			}
 		}
 	}
 
 	@Override
 	public boolean dependsOnSelection() {
 		return false;
 	}
 
 	@Override
 	public Set<String> getConsumedEvents() {
 		Set<String> events = new HashSet<String>();
 
 		Set<String> textInputCellEvents = textInputCell.getConsumedEvents();
 		if (textInputCellEvents != null) {
 			events.addAll(textInputCellEvents);
 		}
 		Set<String> editCellEvents = editCell.getConsumedEvents();
 		if (editCellEvents != null) {
 			events.addAll(editCellEvents);
 		}
 		Set<String> selectionCellEvents = selectionCell.getConsumedEvents();
 		if (selectionCellEvents != null) {
 			events.addAll(selectionCellEvents);
 		}
 		Set<String> checkboxCellEvents = checkboxCell.getConsumedEvents();
 		if (checkboxCellEvents != null) {
 			events.addAll(checkboxCellEvents);
 		}
 		Set<String> buttonCellEvents = buttonCell.getConsumedEvents();
 		if (buttonCellEvents != null) {
 			events.addAll(buttonCellEvents);
 		}
 		Set<String> dateCellEvents = dateCell.getConsumedEvents();
 		if (dateCellEvents != null) {
 			events.addAll(dateCellEvents);
 		}
 		Set<String> durationCellEvents = durationCell.getConsumedEvents();
 		if (durationCellEvents != null) {
 			events.addAll(durationCellEvents);
 		}
 		Set<String> textCellEvents = textCell.getConsumedEvents();
 		if (textCellEvents != null) {
 			events.addAll(textCellEvents);
 		}
 		return events;
 	}
 
 	@Override
 	public boolean handlesSelection() {
 		return false;
 	}
 
 	@Override
 	protected void onEnterKeyDown(Context context, Element parent,
 			Object value, NativeEvent event,
 			final ValueUpdater<Object> valueUpdater) {
 		Class<?> cellClass = getCellClass(context, value);
 		if (cellClass.equals(TextInputCell.class)) {
 			textInputCell.onEnterKeyDown(context, parent, getString(value),
 					event, new ValueUpdater<String>() {
 						@Override
 						public void update(String value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		} else if (cellClass.equals(EditTextCell.class)) {
 			editCell.onEnterKeyDown(context, parent, getString(value), event,
 					new ValueUpdater<String>() {
 						@Override
 						public void update(String value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		} else if (cellClass.equals(SelectionCell.class)) {
 			selectionCell.onEnterKeyDown(context, parent, getString(value),
 					event, new ValueUpdater<String>() {
 						@Override
 						public void update(String value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		} else if (cellClass.equals(CheckboxCell.class)) {
 			if (value instanceof Boolean) {
 				checkboxCell.onEnterKeyDown(context, parent, (Boolean) value,
 						event, new ValueUpdater<Boolean>() {
 							@Override
 							public void update(Boolean value) {
 								if (valueUpdater != null) {
 									valueUpdater.update(value);
 								}
 							}
 						});
 			} else {
 				checkboxCell.onEnterKeyDown(context, parent,
 						Boolean.valueOf(getString(value)), event,
 						new ValueUpdater<Boolean>() {
 							@Override
 							public void update(Boolean value) {
 								if (valueUpdater != null) {
 									valueUpdater.update(value);
 								}
 							}
 						});
 			}
 		} else if (cellClass.equals(ButtonCell.class)) {
 			buttonCell.onEnterKeyDown(context, parent, getString(value), event,
 					new ValueUpdater<String>() {
 						@Override
 						public void update(String value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		} else if (cellClass.equals(DateCell.class)) {
 			dateCell.onEnterKeyDown(context, parent, (Date) value, event,
 					new ValueUpdater<Date>() {
 						@Override
 						public void update(Date value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		} else if (cellClass.equals(DurationCell.class)) {
 			durationCell.onEnterKeyDown(context, parent, (Long) value, event,
 					new ValueUpdater<Long>() {
 						@Override
 						public void update(Long value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		} else {
 			textCell.onEnterKeyDown(context, parent, getString(value), event,
 					new ValueUpdater<String>() {
 						@Override
 						public void update(String value) {
 							if (valueUpdater != null) {
 								valueUpdater.update(value);
 							}
 						}
 					});
 		}
 	}
 
 	@Override
 	public boolean resetFocus(Context context, Element parent, Object value) {
 		Class<?> cellClass = getCellClass(context, value);
 		if (cellClass.equals(TextInputCell.class)) {
 			return textInputCell.resetFocus(context, parent, getString(value));
 		} else if (cellClass.equals(EditTextCell.class)) {
 			return editCell.resetFocus(context, parent, getString(value));
 		} else if (cellClass.equals(SelectionCell.class)) {
 			return selectionCell.resetFocus(context, parent, getString(value));
 		} else if (cellClass.equals(CheckboxCell.class)) {
 			if (value instanceof Boolean) {
 				return checkboxCell
 						.resetFocus(context, parent, (Boolean) value);
 			} else {
 				return checkboxCell.resetFocus(context, parent,
 						Boolean.valueOf(getString(value)));
 			}
 		} else if (cellClass.equals(ButtonCell.class)) {
 			return buttonCell.resetFocus(context, parent, getString(value));
 		} else if (cellClass.equals(DateCell.class)) {
 			return dateCell.resetFocus(context, parent, (Date) value);
 		} else if (cellClass.equals(DurationCell.class)) {
 			return durationCell.resetFocus(context, parent, (Long) value);
 		} else {
 			return textCell.resetFocus(context, parent, getString(value));
 		}
 	}
 
 	@Override
 	public void setValue(Context context, Element parent, Object value) {
 		Class<?> cellClass = getCellClass(context, value);
 		if (cellClass.equals(TextInputCell.class)) {
 			textInputCell.setValue(context, parent, getString(value));
 		} else if (cellClass.equals(EditTextCell.class)) {
 			editCell.setValue(context, parent, getString(value));
 		} else if (cellClass.equals(SelectionCell.class)) {
 			selectionCell.setValue(context, parent, getString(value));
 		} else if (cellClass.equals(CheckboxCell.class)) {
 			if (value instanceof Boolean) {
 				checkboxCell.setValue(context, parent, (Boolean) value);
 			} else {
 				checkboxCell.setValue(context, parent,
 						Boolean.valueOf(getString(value)));
 			}
 		} else if (cellClass.equals(ButtonCell.class)) {
 			buttonCell.setValue(context, parent, getString(value));
 		} else if (cellClass.equals(DateCell.class)) {
 			dateCell.setValue(context, parent, (Date) value);
 		} else if (cellClass.equals(DurationCell.class)) {
 			durationCell.setValue(context, parent, (Long) value);
 		} else {
 			textCell.setValue(context, parent, getString(value));
 		}
 	}
 
 	protected Class<?> getCellClass(Context context, Object value) {
 		int row = context.getIndex();
 		if ((0 <= row) && (row < cellClasses.size())) {
 			return cellClasses.get(row);
 		}
 		return TextCell.class;
 	}
 
 	private class MyEditTextCell extends EditTextCell {
 		@Override
 		protected void onEnterKeyDown(Context context, Element parent,
 				String value, NativeEvent event,
 				ValueUpdater<String> valueUpdater) {
 			super.onEnterKeyDown(context, parent, value, event, valueUpdater);
 		}
 
 		@Override
 		public void render(Context context, String value, SafeHtmlBuilder sb) {
 			if ((value != null) && (value.length() > 20)) {
 				value = value.substring(0, 10) + "..."
 						+ value.substring(value.length() - 10);
 			}
 			super.render(context, value, sb);
 		}
 	}
 
 	private class MySelectionCell extends DynamicSelectionCell {
 		@Override
 		protected void onEnterKeyDown(Context context, Element parent,
 				String value, NativeEvent event,
 				ValueUpdater<String> valueUpdater) {
 			super.onEnterKeyDown(context, parent, value, event, valueUpdater);
 		}
 	}
 
 	private class MyCheckboxCell extends ActiveCheckboxCell {
 		@Override
 		protected void onEnterKeyDown(Context context, Element parent,
 				Boolean value, NativeEvent event,
 				ValueUpdater<Boolean> valueUpdater) {
 			super.onEnterKeyDown(context, parent, value, event, valueUpdater);
 		}
 	}
 
 	private class MyButtonCell extends ButtonCell {
 		@Override
 		protected void onEnterKeyDown(Context context, Element parent,
 				String value, NativeEvent event,
 				ValueUpdater<String> valueUpdater) {
 			super.onEnterKeyDown(context, parent, value, event, valueUpdater);
 		}
 	}
 
 	private class MyTextCell extends TextCell {
 		@Override
 		protected void onEnterKeyDown(Context context, Element parent,
 				String value, NativeEvent event,
 				ValueUpdater<String> valueUpdater) {
 			super.onEnterKeyDown(context, parent, value, event, valueUpdater);
 		}
 	}
 
 	private class MyDateCell extends AbstractCell<Date> {
 		private DateTimeFormat format;
 		private final SafeHtmlRenderer<String> renderer;
 
 		public MyDateCell() {
 			format = DateTimeFormat.getFormat(PredefinedFormat.DATE_FULL);
 			renderer = SimpleSafeHtmlRenderer.getInstance();
 		}
 
 		@Override
 		protected void onEnterKeyDown(Context context, Element parent,
 				Date value, NativeEvent event, ValueUpdater<Date> valueUpdater) {
 			super.onEnterKeyDown(context, parent, value, event, valueUpdater);
 		}
 
 		@Override
 		public void render(Context context, Date value, SafeHtmlBuilder sb) {
 			if (value != null) {
 				sb.append(renderer.render(format.format(value)));
 			}
 		}
 
 		public void setFormat(DateTimeFormat format) {
 			this.format = format;
 		}
 	}
 
 	private class MyDurationCell extends DurationCell {
 		@Override
 		protected void onEnterKeyDown(Context context, Element parent,
 				Long value, NativeEvent event, ValueUpdater<Long> valueUpdater) {
 			super.onEnterKeyDown(context, parent, value, event, valueUpdater);
 		}
 	}
 }
