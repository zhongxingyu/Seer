 /**
  * 
  */
 package org.cotrix.web.common.client.widgets;
 
 
 import java.util.EnumSet;
 
 import org.cotrix.web.common.client.widgets.EnumListBox.LabelProvider;
 import org.cotrix.web.common.shared.CsvConfiguration;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.SimpleCheckBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class CsvConfigurationPanel extends Composite implements HasValueChangeHandlers<CsvConfiguration> {
 	
 	private interface Value {
 		public String getLabel();
 		public char getValue();
 	}
 	
 	private enum Separator implements Value {
 		COMMA("comma",','),
 		SEMICOLON("semicolon",';'),
 		SPACE("space",' '),
 		TAB("tab",'\t'),
 		CUSTOM("other",(char)0)
 		;
 		public static LabelProvider<Separator> LABEL_PROVIDER = new LabelProvider<CsvConfigurationPanel.Separator>() {
 
 			@Override
 			public String getLabel(Separator item) {
 				return item.getLabel();
 			}
 		};
 		protected String label;
 		protected char value;
 		/**
 		 * @param label
 		 * @param value
 		 */
 		private Separator(String label, char value) {
 			this.label = label;
 			this.value = value;
 		}
 		/**
 		 * @return the label
 		 */
 		public String getLabel() {
 			return label;
 		}
 		/**
 		 * @return the value
 		 */
 		public char getValue() {
 			return value;
 		}
 	}
 	
 	protected enum Quote implements Value {
 		DOUBLE("double quote",'"'),
 		SINGLE("single quote",'\''),
 		CUSTOM("other",'o')
 		;
 		public static LabelProvider<Quote> LABEL_PROVIDER = new LabelProvider<CsvConfigurationPanel.Quote>() {
 
 			@Override
 			public String getLabel(Quote item) {
 				return item.getLabel();
 			}
 		};
 		
 		protected String label;
 		protected char value;
 		/**
 		 * @param label
 		 * @param value
 		 */
 		private Quote(String label, char value) {
 			this.label = label;
 			this.value = value;
 		}
 		/**
 		 * @return the label
 		 */
 		public String getLabel() {
 			return label;
 		}
 		/**
 		 * @return the value
 		 */
 		public char getValue() {
 			return value;
 		}
 	}
 	
 	public interface RefreshHandler extends EventHandler {
 		public void onRefresh(CsvConfiguration configuration);
 	}
 	
 	public @UiField ListBox charsetField;
 	public @UiField SimpleCheckBox hasHeaderField;
 	public @UiField(provided=true) EnumListBox<Separator> separatorField;
 	public @UiField TextBox customSeparatorField;
 	public @UiField TextBox commentField;
 	public @UiField(provided=true) EnumListBox<Quote> quoteField;
 	public @UiField TextBox customQuoteField;
 	
 	protected RefreshHandler refreshHandler;
 
 	public CsvConfigurationPanel(UiBinder<Widget, CsvConfigurationPanel> binder) {
 
 		separatorField = new EnumListBox<CsvConfigurationPanel.Separator>(Separator.class, Separator.LABEL_PROVIDER);
 		quoteField = new EnumListBox<CsvConfigurationPanel.Quote>(Quote.class, Quote.LABEL_PROVIDER);
 		
 		initWidget(binder.createAndBindUi(this));
 		
 		bind(separatorField, customSeparatorField, Separator.CUSTOM);
 		bind(quoteField, customQuoteField, Quote.CUSTOM);
 
 		bindChangeHandlers();
 	}
 	
 	private void bindChangeHandlers() {
 
 		
 		hasHeaderField.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				fireValueChange();
 			}
 		});
 		
 		ChangeHandler changeHandler = new ChangeHandler() {
 			
 			@Override
 			public void onChange(ChangeEvent event) {
 				fireValueChange();
 			}
 		};
 		separatorField.addChangeHandler(changeHandler);
 		quoteField.addChangeHandler(changeHandler);
 		customSeparatorField.addChangeHandler(changeHandler);
 		customQuoteField.addChangeHandler(changeHandler);
 		charsetField.addChangeHandler(changeHandler);
 		commentField.addChangeHandler(changeHandler);
 		
 	}
 	
 	@UiHandler("refreshButton")
 	public void refreshButtonClicked(ClickEvent clickEvent)
 	{
 		if (refreshHandler!=null) refreshHandler.onRefresh(getConfiguration());
 	}
 	
 	private <E extends Enum<E>> void bind(final EnumListBox<E> listBox, final TextBox textBox, final E custom)
 	{
 		listBox.addChangeHandler(new ChangeHandler() {
 			
 			@Override
 			public void onChange(ChangeEvent event) {
 				E value = listBox.getSelectedValue();
 				textBox.setEnabled(value == custom);
 			}
 		});
 	}
 
 	/**
 	 * @param refreshHandler the saveHandler to set
 	 */
 	public void setRefreshHandler(RefreshHandler refreshHandler) {
 		this.refreshHandler = refreshHandler;
 	}
 
 	public void setConfiguration(CsvConfiguration configuration)
 	{
 	
 		hasHeaderField.setValue(configuration.isHasHeader());
 		Log.trace("separator: "+configuration.getFieldSeparator());
 		
 		updateListBox(Separator.class, separatorField, configuration.getFieldSeparator(), customSeparatorField, Separator.CUSTOM);
 		updateListBox(Quote.class, quoteField, configuration.getQuote(), customQuoteField, Quote.CUSTOM);
 		
 		commentField.setValue(String.valueOf(configuration.getComment()));
 
 		charsetField.clear();
 		for (String charset:configuration.getAvailablesCharset()) charsetField.addItem(charset);
 		selectValue(charsetField, configuration.getCharset());
 		
 	}
 	
 	private <E extends Enum<E> & Value> E getValue(Class<E> eclass, char value, E custom)
 	{
 		for (E element : EnumSet.allOf(eclass)) {
 			if (element.getValue() == value) return element;
 		}
 		return custom;
 	}
 	
 	private <E extends Enum<E> & Value> void updateListBox(Class<E> eclass, EnumListBox<E> listBox, char fieldValue, TextBox textBox, E custom)
 	{
 		E value = getValue(eclass, fieldValue, custom);
 		listBox.setSelectedValue(value);
 		textBox.setEnabled(value == custom);
 		if (value == custom) textBox.setValue(String.valueOf(fieldValue));
 	}
 	
 	private boolean selectValue(ListBox listBox, String value)
 	{
 		for (int i = 0; i<listBox.getItemCount(); i++) {
 			String candidate = listBox.getValue(i);
			if (value.equals(candidate)) {
 				listBox.setSelectedIndex(i);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public CsvConfiguration getConfiguration()
 	{
 		CsvConfiguration configuration = new CsvConfiguration();
 		
 		//TODO validation
 		configuration.setCharset(charsetField.getValue(charsetField.getSelectedIndex()));
 		configuration.setComment(commentField.getValue().charAt(0));
 		configuration.setFieldSeparator(getValue(separatorField, customSeparatorField, Separator.CUSTOM));
 		configuration.setHasHeader(hasHeaderField.getValue());
 		configuration.setQuote(getValue(quoteField, customQuoteField, Quote.CUSTOM));
 		return configuration;
 	}
 	
 	private <E extends Enum<E> & Value> char getValue(EnumListBox<E> listbox, TextBox textBox, E custom)
 	{
 		E value = listbox.getSelectedValue();
 		if (value == null) throw new IllegalStateException("Invalid selected index");
 		if (value == custom) {
 			String text = textBox.getValue();
 			if (text.isEmpty()) throw new IllegalStateException("Invalid box value, it is empty");
 			return text.charAt(0);
 		}
 		return value.getValue();
 	}
 	
 	private void fireValueChange() {
 		ValueChangeEvent.fire(this, getConfiguration());
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<CsvConfiguration> handler) {
 		return addHandler(handler, ValueChangeEvent.getType());
 	}
 }
