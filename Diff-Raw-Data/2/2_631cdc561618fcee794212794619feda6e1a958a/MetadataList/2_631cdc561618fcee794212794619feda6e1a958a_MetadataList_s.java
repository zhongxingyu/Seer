 package monbulk.MetadataEditor;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import monbulk.client.Monbulk;
 import monbulk.client.Settings;
 import monbulk.shared.Services.MetadataService;
 import monbulk.shared.Services.MetadataService.DestroyMetadataHandler;
 import monbulk.shared.Services.MetadataService.GetMetadataTypesHandler;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 
 public class MetadataList extends Composite implements KeyUpHandler, KeyDownHandler
 {
 	public interface Handler
 	{
 		public enum NewType
 		{
 			New,
 			Clone,
 			FromTemplate,
 		};
 
 		/**
 		 * Callback for a selection on the list.  This is the new selection.
 		 * Note that calling getSelectedMetadataName will return the previous
 		 * selection.  Returning false will cancel the selection.
 		 * @param metadataName
 		 * @return
 		 */
 		public boolean onMetadataSelected(String metadataName);
 		public void onRefreshList();
 		public void onRemoveMetadata(String metadataName);
 		public void onNewMetadata(NewType type);
 	};
 
 	private static MetadataListUiBinder uiBinder = GWT.create(MetadataListUiBinder.class);
 	interface MetadataListUiBinder extends UiBinder<Widget, MetadataList> { }
 
 	protected List<String> m_metadataTypes = null;
 	private Handler m_handler = null;
 	private String m_selected = "";
 	private boolean m_showTemplates = true;
 
 	@UiField HTMLPanel m_buttonsPanel;
 	@UiField Button m_refreshList;
 	@UiField Button m_removeMetadata;
 	@UiField Button m_newMetadata;
 	@UiField Button m_fromTemplate;
 	@UiField Button m_cloneMetadata;
 	@UiField ListBox m_metadataListBox;
 	@UiField TextBox m_filterTextBox;
 
 	//Extended aglenn 6/5/12
 	@UiField LayoutPanel m_LayoutPanel;
 	
 	// Item to select after metadata list is populated.
 	private String m_itemToSelect = "";
 
 	public MetadataList()
 	{
 		initWidget(uiBinder.createAndBindUi(this));
 		
 		m_filterTextBox.addKeyUpHandler(this);
 		m_filterTextBox.addKeyDownHandler(this);
 		m_filterTextBox.setText("");
 		
 		populateListBox();
 	}
 	
 	/**
 	 * If 'showRefresh' is true the refresh button will be visible.
 	 * @param showRefresh
 	 */
 	public void setShowRefresh(boolean showRefresh)
 	{
 		m_refreshList.setVisible(showRefresh);
 	}
 	
 	/**
 	 * if 'showRemove' is true the remove button will be visible.
 	 * @param showRemove
 	 */
 	public void setShowRemove(boolean showRemove)
 	{
 		m_removeMetadata.setVisible(showRemove);
 	}
 	
 	/**
 	 * If 'showNew' is true the new button will be visible.
 	 * @param showNew
 	 */
 	public void setShowNew(boolean showNew)
 	{
 		m_newMetadata.setVisible(showNew);
 	}
 	
 	/**
 	 * If 'showFromTemplate' is true the from template button
 	 * will be visible.
 	 * @param showFromTemplate
 	 */
 	public void setShowFromTemplate(boolean showFromTemplate)
 	{
 		m_fromTemplate.setVisible(showFromTemplate);
 	}
 	
 	/**
 	 * If 'showClone' is true the clone button will be visible.
 	 * @param showClone
 	 */
 	public void setShowClone(boolean showClone)
 	{
 		m_cloneMetadata.setVisible(showClone);
 	}
 	
 	/**
 	 * If 'showTemplates' is true templates will be shown,
 	 * otherwise they will be hidden.
 	 * @param showTemplates
 	 */
 	public void setShowTemplates(boolean showTemplates)
 	{
 		m_showTemplates = showTemplates;
 	}
 
 	/**
 	 * Sets the metadata list handler.
 	 * @param handler
 	 */
 	public void setHandler(Handler handler)
 	{
 		m_handler = handler;
 	}
 	
 	/**
 	 * Returns the panel that contains the buttons (can be used to add
 	 * custom buttons to the control).
 	 * @return
 	 */
 	public HTMLPanel getButtonsPanel()
 	{
 		return m_buttonsPanel;
 	}
 
 	/**
 	 * Returns the selected metadata name, or empty string if none
 	 * is selected.
 	 * @return
 	 */
 	public String getSelectedMetadataName()
 	{
 		return m_selected;
 	}
 	
 	/**
 	 * Sets the listbox focus state to 'focus'.
 	 * @param focus
 	 */
 	public void setFocus(boolean focus)
 	{
 		m_metadataListBox.setFocus(focus);
 	}
 
 	/**
 	 * Removes the specified metadata from the list.
 	 * @param metadata
 	 */
 	public void remove(String metadata)
 	{
 		int newIndex = 0;
 		int count = m_metadataListBox.getItemCount();
 		for (int i = 0; i < count; i++)
 		{
 			String foo = m_metadataListBox.getItemText(i);
 			if (foo.equals(metadata))
 			{
 				newIndex = i < (count - 1) ? i : (count - 2);
 				m_metadataListBox.removeItem(i);
 				break;
 			}
 		}
 		
 		// Remove from list.
 		m_metadataTypes.remove(metadata);
 		
 		// Select next item in list.
 		selectMetadata(newIndex);
 	}
 
 	/**
 	 * Refreshes the metadata list.  If 'selection' is not an empty
 	 * string, the metadata named 'selection' will be selected when
 	 * the list has finished populating.
 	 * @param selection
 	 */
 	public void refresh(String selection)
 	{
 		m_itemToSelect = selection;
 		populateListBox();
 		m_filterTextBox.setText("");
 	}
 	
 	/**
 	 * Clears the selection on the list box.
 	 */
 	public void clearSelection()
 	{
 		m_metadataListBox.setSelectedIndex(-1);
 		m_selected = "";
 		m_fromTemplate.setEnabled(false);
 	}
 	
 	/**
 	 * Adds a dummy item called <new metadata>.
 	 */
 	public void addDummyItem()
 	{
 		m_metadataListBox.addItem("<new metadata>");
 		setSelectedMetadata("<new metadata>");
 
 		// Can't do anything until no longer creating new metadata.
 		m_removeMetadata.setEnabled(false);
 		m_newMetadata.setEnabled(false);
 		m_fromTemplate.setEnabled(false);
 		m_cloneMetadata.setEnabled(false);
 	}
 	
 	/**
 	 * Removes the dummy item.
 	 */
 	public void removeDummyItem()
 	{
 		int index = m_metadataListBox.getItemCount() - 1;
 		if (index >= 0)
 		{
 			String item = m_metadataListBox.getItemText(index);
 			if (item.equals("<new metadata>"))
 			{
 				m_metadataListBox.removeItem(index);
 			}
 		}
 
 		// Update button state.
 		setButtonState();
 	}
 
 	public void onKeyDown(KeyDownEvent event)
 	{
 		if (event.getSource() == m_filterTextBox)
 		{
 			// If user presses down arrow in filter text box, they
 			// automatically start scrolling through the metadata list.
 			if (event.isDownArrow())
 			{
 				m_metadataListBox.setFocus(true);
 			}
 		}
 	}
 
 	public void onKeyUp(KeyUpEvent event)
 	{
 		if (event.getSource() == m_filterTextBox)
 		{
 			int keyCode = event.getNativeKeyCode();
 			if ((keyCode >= 'a' && keyCode <= 'z') ||
 				(keyCode >= 'A' && keyCode <= 'Z' ) ||
 				(keyCode == '.') ||
 				(keyCode == KeyCodes.KEY_BACKSPACE) ||
 				(keyCode == KeyCodes.KEY_DELETE))
 			{
 				// Filter list using filter text.
 				String filterText = m_filterTextBox.getText();
 				m_metadataListBox.clear();
 				if (m_metadataTypes != null)
 				{
 					for (int i = 0; i < m_metadataTypes.size(); i++)
 					{
 						String m = m_metadataTypes.get(i);
 						if (m.indexOf(filterText) >= 0 || filterText.length() == 0)
 						{
 							m_metadataListBox.addItem(m);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Populates the list of available metadata from the server.
 	 */
 	public void populateListBox()
 	{
 		m_metadataListBox.clear();
 		setButtonState();
 
 		MetadataService service = MetadataService.get();
 		if (service != null)
 		{
 			service.getMetadataTypes(new GetMetadataTypesHandler()
 			{
 				// Callback for reading a list of metadata.
 				public void onGetMetadataTypes(List<String> types)
 				{
 					m_metadataTypes = new ArrayList<String>();
 					
 					if (types != null)
 					{
 						// Select the first item by default.
 						int selectionIndex = -1;
 
 						Settings settings = Monbulk.getSettings();
 						String namespace = settings.getDefaultNamespace() + ".";
 						String template = namespace + "template.";
 						
 						// Add all items.
 						for (int i = 0; i < types.size(); i++)
 						{
 							String name = types.get(i);
 							
 							// Filter out templates if we don't want to see them.
 							if (name.startsWith(namespace) && (m_showTemplates || !name.startsWith(template)))
 							{
 								m_metadataTypes.add(name);
 								m_metadataListBox.addItem(name);
 								if (name.equals(m_itemToSelect))
 								{
 									// We've found the item we need to select.
 									selectionIndex = m_metadataTypes.size() - 1;
 								}
 							}
 						}
 
 						// If nothing was selected but there are metadata items
 						// then select the first one by default.
 						if (selectionIndex == -1 && m_metadataTypes.size() > 0)
 						{
 							selectionIndex = 0;
 						}
 						
 						if (selectionIndex != -1)
 						{
 							selectMetadata(selectionIndex);
 							setFocus(true);
 						}
 					}
 					
 					setButtonState();
 				}
 			});
 		}
 	}
 
 	private void selectMetadata(int index)
 	{
 		if (index >= 0 && index < m_metadataListBox.getItemCount())
 		{
 			m_metadataListBox.setSelectedIndex(index);
 			onMetadataSelected(null);
 		}
 
 		m_itemToSelect = "";
 	}
 
 	private void setButtonState()
 	{
 		Settings settings = Monbulk.getSettings();
 		String ns = settings.getDefaultNamespace() + ".template.";
 		m_fromTemplate.setEnabled(m_selected.startsWith(ns));
 		m_removeMetadata.setEnabled(m_selected.length() > 0);
 		m_cloneMetadata.setEnabled(m_selected.length() > 0);
 		m_newMetadata.setEnabled(true);
 	}
 
 	/**
 	 * Sets the selected metadata in the list box without firing any events.
 	 * @param metadata
 	 */
 	public void setSelectedMetadata(String metadata)
 	{
 		int index = -1;
 		for (int i = 0; i < m_metadataListBox.getItemCount(); i++)
 		{
 			if (m_metadataListBox.getItemText(i).equals(metadata))
 			{
 				index = i;
 				break;
 			}
 		}
 
 		m_metadataListBox.setSelectedIndex(index);
 		m_selected = metadata;
 		setButtonState();
 	}
 
 	@UiHandler("m_metadataListBox")
 	protected void onMetadataSelected(ChangeEvent event)
 	{
 		int index = m_metadataListBox.getSelectedIndex();
 		String selected = m_metadataListBox.getValue(index);
 		boolean allowSelect = true;
 
 		if (m_handler != null)
 		{
 			allowSelect = m_handler.onMetadataSelected(selected);
 		}
 		
 		if (allowSelect)
 		{
 			// Handler allows the selection.
 			m_selected = selected;
 		}
 		else
 		{
 			// Handler cancelled the selection so select the previous metadata.
 			setSelectedMetadata(m_selected);
 		}
 
 		setButtonState();
 	}
 	
 	@UiHandler("m_refreshList")
 	protected void onRefreshList(ClickEvent event)
 	{
 		refresh("");
 
 		if (m_handler != null)
 		{
 			m_handler.onRefreshList();
 		}
 	}
 	
 	@UiHandler("m_removeMetadata")
 	protected void onRemoveMetadata(ClickEvent event)
 	{
 		String selected = getSelectedMetadataName();
 
 		if (selected.length() > 0)
 		{
 			String msg = "Are you sure you wish to remove the metadata '" + selected + "'?";
 			if (Window.confirm(msg))
 			{
 				msg = "The metadata '" + selected + "' will be removed.  Please type the word 'delete' below to confirm removal.";
 				String result = Window.prompt(msg, "");
 				if (result.equals("delete"))
 				{
 					if (m_handler != null)
 					{
 						m_handler.onRemoveMetadata(selected);
 					}
 	
 					// Call service to destroy metadata.
 					MetadataService service = MetadataService.get();
 					service.destroyMetadata(selected, new DestroyMetadataHandler()
 					{
 						public void onDestroyMetadata(String name, boolean success)
 						{
 							if (success)
 							{
 								// Metadata was successfully destroyed, so refresh our list.
 								remove(name);
 								Window.alert("The metadata '" + name + "' was removed.");
 							}
 						}
 					});
 				}
 				else
 				{
 					Window.alert("The metadata '" + selected + "' will not be removed.");
 				}
 			}
 		}
 	}
 
 	@UiHandler({ "m_newMetadata", "m_cloneMetadata", "m_fromTemplate" })
 	protected void onNewMetadata(ClickEvent event)
 	{
 		if (m_handler != null)
 		{
 			Handler.NewType type = Handler.NewType.New;
 			if (event.getSource() == m_cloneMetadata)
 			{
 				type = Handler.NewType.Clone;
 			}
 			else if (event.getSource() == m_fromTemplate)
 			{
 				type = Handler.NewType.FromTemplate;
 			}
 
 			m_handler.onNewMetadata(type);
 		}
 	}
 	
 	/**
 	 * Added by aglenn
 	 * @param shouldHide
 	 * @since 196
 	 */
 	protected void hideListBox(Boolean shouldHide)
 	{
 		this.m_metadataListBox.setVisible(shouldHide);
 	}
 	/**
 	 * Added by aglenn
 	 * @return LayoutPanel the baseLayout for this widget
 	 * @since 196
 	 */
 	protected LayoutPanel getLayout()
 	{
 		return m_LayoutPanel;
 	}
 	/**
 	 * Added by aglenn
 	 * @return String Value of the filter TextBox
 	 * 196
 	 */
 	protected String getTextBoxValue()
 	{
 		return this.m_filterTextBox.getValue();
 	}
 	/**
 	 * HACK - Need to shuffle widgets down after add as LayoutPanel.insert doesn't seem to be working with UIBinder 
 	 * @param w	A widget to add into a layer
 	 */
 	protected void addWidget(Widget w)
 	{
 		this.m_LayoutPanel.add(w);
 		//this.m_LayoutPanel.setWidgetVerticalPosition(w, Layout.Alignment.BEGIN);
 		this.m_LayoutPanel.setWidgetTopHeight(w, 30, Style.Unit.PX, 510, Style.Unit.PX);
 		//this.m_LayoutPanel.setWidgetTopHeight(this.m_filterTextBox, 0, Style.Unit.PX, 30, Style.Unit.PX);
 		this.m_LayoutPanel.setWidgetTopHeight(this.m_buttonsPanel, 545, Style.Unit.PX, 24, Style.Unit.PX);
 		
 	}
 }
