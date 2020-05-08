 package org.bh.gui.swing.comp;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 
 import org.bh.data.types.IValue;
 import org.bh.gui.CompValueChangeManager;
 import org.bh.gui.IBHModelComponent;
 import org.bh.platform.IPlatformListener;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.Services;
 import org.bh.platform.PlatformEvent.Type;
 import org.bh.platform.i18n.ITranslator;
 import org.bh.validation.ValidationRule;
 
 public class BHComboBox extends JComboBox implements IBHModelComponent,
 		ActionListener, IPlatformListener {
 	private static final long serialVersionUID = 3609724364063209645L;
 	static final ITranslator translator = Services.getTranslator();
 	private String key;
 	private String hint;
 	private int lastSelectedIndex = 0;
 	private boolean sorted = false;
 	private Item[] items = new Item[0];
 	final CompValueChangeManager valueChangeManager = new CompValueChangeManager();
 	boolean changeListenerEnabled = true;
 
 	public BHComboBox(Object key) {
 		this.key = key.toString();
 		reloadText();
		Services.addPlatformListener(this);
 		addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (changeListenerEnabled)
 					valueChangeManager
 							.fireCompValueChangeEvent(BHComboBox.this);
 			}
 		});
 	}
 
 	public void setValueList(Item[] items) {
 		this.items = items;
 		if (sorted) {
 			sortItems();
 		}
 		setModel(new DefaultComboBoxModel(this.items));
 		updateUI();
 	}
 
 	@Override
 	public IValue getValue() {
 		return ((Item) this.getSelectedItem()).getValue();
 	}
 
 	@Override
 	public void setValue(IValue value) {
 		if (value == null) {
 			// at this point, it is necessary to trigger a change event because
 			// the first item seems to be selected, but in fact is not saved in
 			// the DTO
 			if (this.getItemCount() > 0)
 				this.setSelectedIndex(0);
 			return;
 		}
 
 		for (int i = 0; i < this.getItemCount(); i++) {
 			Item item = (Item) this.getItemAt(i);
 			if (value.equals(item.getValue())) {
 				changeListenerEnabled = false;
 				this.setSelectedIndex(i);
 				changeListenerEnabled = true;
 				return;
 			}
 		}
 	}
 
 	@Override
 	public String getHint() {
 		return hint;
 	}
 
 	@Override
 	public String getKey() {
 		return key;
 	}
 
 	protected void reloadText() {
 		hint = Services.getTranslator().translate(key, ITranslator.LONG);
 		setToolTipText(hint);
 		this.updateUI();
 	}
 
 	@Override
 	public void platformEvent(PlatformEvent e) {
 		if (e.getEventType() == Type.LOCALE_CHANGED) {
 			reloadText();
 		}
 	}
 
 	@Override
 	public ValidationRule[] getValidationRules() {
 		return new ValidationRule[0];
 	}
 
 	@Override
 	public void setValidationRules(ValidationRule[] validationRules) {
 		// nothing to do
 	}
 
 	public void setSorted(boolean sorted) {
 		this.sorted = sorted;
 		if (sorted) {
 			sortItems();
 		}
 	}
 
 	public boolean isSorted() {
 		return sorted;
 	}
 
 	public void sortItems() {
 		Arrays.sort(items, new Comparator<Item>() {
 			@Override
 			public int compare(Item o1, Item o2) {
 				return o1.toString().compareTo(o2.toString());
 			}
 		});
 	}
 
 	public static class Item {
 		private String key;
 		private IValue value;
 
 		public Item(String key, IValue value) {
 			this.key = key;
 			this.value = value;
 		}
 
 		@Override
 		public String toString() {
 			return translator.translate(key);
 		}
 
 		public String getKey() {
 			return key;
 		}
 
 		public IValue getValue() {
 			return value;
 		}
 	}
 
 	@Override
 	public CompValueChangeManager getValueChangeManager() {
 		return valueChangeManager;
 	}
 	
 	public int getLastSelectedIndex() {
 		return lastSelectedIndex;
 	}
 
 	@Override
 	public void setSelectedIndex(int arg0) {
 		lastSelectedIndex = getSelectedIndex();
 		super.setSelectedIndex(arg0);
 	}
 
 	@Override
 	public void setSelectedItem(Object arg0) {
 		lastSelectedIndex = getSelectedIndex();
 		super.setSelectedItem(arg0);
 	}
 
 	
 }
