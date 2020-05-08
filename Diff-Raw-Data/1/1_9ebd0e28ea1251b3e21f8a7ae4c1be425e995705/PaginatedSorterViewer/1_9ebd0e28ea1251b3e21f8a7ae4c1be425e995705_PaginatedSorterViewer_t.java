 /*
  * Copyright 2011 Instituto Superior Tecnico
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the vaadin-framework.
  *
  *   The vaadin-framework Infrastructure is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version
  *   3 of the License, or (at your option) any later version.*
  *
  *   vaadin-framework is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with vaadin-framework. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.vaadinframework.ui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.vaadinframework.VaadinResourceConstants;
 import pt.ist.vaadinframework.VaadinResources;
 
 import com.vaadin.data.Container;
 import com.vaadin.data.Container.Filterable;
 import com.vaadin.data.Container.Indexed;
 import com.vaadin.data.Container.ItemSetChangeEvent;
 import com.vaadin.data.Container.ItemSetChangeListener;
 import com.vaadin.data.Container.ItemSetChangeNotifier;
 import com.vaadin.data.Container.Sortable;
 import com.vaadin.data.Container.Viewer;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.data.util.IndexedContainer;
 import com.vaadin.data.util.filter.SimpleStringFilter;
 import com.vaadin.data.validator.IntegerValidator;
 import com.vaadin.event.FieldEvents.TextChangeEvent;
 import com.vaadin.event.FieldEvents.TextChangeListener;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.ComponentContainer;
 import com.vaadin.ui.GridLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Select;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.themes.BaseTheme;
 import com.vaadin.ui.themes.Reindeer;
 
 public class PaginatedSorterViewer extends GridLayout implements Viewer, VaadinResourceConstants {
     interface PageChangeListener {
 	public void pageChanged(PageChangeEvent event);
     }
 
     public class PageChangeEvent {
 
 	final PaginatedSorterViewer viewer;
 
 	public PageChangeEvent(PaginatedSorterViewer table) {
 	    this.viewer = table;
 	}
 
 	public PaginatedSorterViewer getTable() {
 	    return viewer;
 	}
 
 	public int getCurrentPage() {
 	    return viewer.getCurrentPage();
 	}
 
 	public int getTotalAmountOfPages() {
 	    return viewer.getTotalAmountOfPages();
 	}
     }
 
     public class PageLengthControl extends HorizontalLayout {
 	public PageLengthControl(int pageLength) {
 	    setSpacing(true);
 	    addComponent(new Label(VaadinResources.getString(COMMONS_ITEMS_PER_PAGE_LABEL) + ":"));
 	    Select lengthSelect = new Select();
 	    addComponent(lengthSelect);
 	    lengthSelect.addItem(5);
 	    lengthSelect.addItem(10);
 	    lengthSelect.addItem(25);
 	    lengthSelect.addItem(50);
 	    lengthSelect.addItem(100);
 	    lengthSelect.setImmediate(true);
 	    lengthSelect.setNullSelectionAllowed(false);
 	    lengthSelect.setWidth("50px");
 	    lengthSelect.select(pageLength);
 	    lengthSelect.addListener(new ValueChangeListener() {
 		@Override
 		public void valueChange(ValueChangeEvent event) {
 		    setPageLength((Integer) event.getProperty().getValue());
 		}
 	    });
 	    setPageLength(pageLength);
 	    PaginatedSorterViewer.this.addListener(new PageChangeListener() {
 		@Override
 		public void pageChanged(PageChangeEvent event) {
 		    setVisible(event.getTotalAmountOfPages() != 1);
 		}
 	    });
 	}
     }
 
     public class PageChangerControl extends HorizontalLayout {
 
 	private final TextField pageSelection;
 	private final Label totalPagesLabel;
 
 	public PageChangerControl() {
 	    setSpacing(true);
 	    final Button first = new Button("<<", new ClickListener() {
 		@Override
 		public void buttonClick(ClickEvent event) {
 		    setCurrentPage(0);
 		}
 	    });
 	    first.addStyleName(BaseTheme.BUTTON_LINK);
 	    addComponent(first);
 
 	    final Button previous = new Button("<", new ClickListener() {
 		@Override
 		public void buttonClick(ClickEvent event) {
 		    previousPage();
 		}
 	    });
 	    previous.addStyleName(BaseTheme.BUTTON_LINK);
 	    addComponent(previous);
 
 	    addComponent(new Label(VaadinResources.getString(COMMONS_PAGE_LABEL) + ":"));
 	    pageSelection = new TextField();
 	    pageSelection.setImmediate(true);
 	    pageSelection.setWidth(20, UNITS_PIXELS);
 	    pageSelection.setValue(String.valueOf(getCurrentPage()));
 	    pageSelection.addValidator(new IntegerValidator(null));
 	    pageSelection.addStyleName(Reindeer.TEXTFIELD_SMALL);
 	    pageSelection.addListener(new ValueChangeListener() {
 		@Override
 		public void valueChange(ValueChangeEvent event) {
 		    if (pageSelection.isValid() && pageSelection.getValue() != null) {
 			setCurrentPage(Integer.valueOf(pageSelection.getValue().toString()));
 		    }
 		}
 	    });
 	    addComponent(pageSelection);
 
 	    addComponent(new Label("/"));
 
 	    totalPagesLabel = new Label();
 	    addComponent(totalPagesLabel);
 
 	    final Button next = new Button(">", new ClickListener() {
 		@Override
 		public void buttonClick(ClickEvent event) {
 		    nextPage();
 		}
 	    });
 	    next.addStyleName(BaseTheme.BUTTON_LINK);
 	    addComponent(next);
 
 	    final Button last = new Button(">>", new ClickListener() {
 		@Override
 		public void buttonClick(ClickEvent event) {
 		    setCurrentPage(getTotalAmountOfPages());
 		}
 	    });
 	    last.addStyleName(BaseTheme.BUTTON_LINK);
 	    addComponent(last);
 	    PaginatedSorterViewer.this.addListener(new PageChangeListener() {
 		@Override
 		public void pageChanged(PageChangeEvent event) {
 		    pageSelection.setValue(event.getCurrentPage());
 		    pageSelection.setWidth(String.valueOf(event.getTotalAmountOfPages()).length() * 8 + 4, UNITS_PIXELS);
 		    totalPagesLabel.setValue(event.getTotalAmountOfPages());
 		    setVisible(event.getTotalAmountOfPages() != 1);
 		}
 	    });
 	}
     }
 
     public class GroupControl extends Button {
 	private boolean ascending = true;
 
 	private final Object propertyId;
 
 	public GroupControl(final Object propertyId, String label) {
 	    this.propertyId = propertyId;
 	    setCaption(label);
 	    addStyleName(BaseTheme.BUTTON_LINK);
 	    addListener(new ClickListener() {
 		@Override
 		public void buttonClick(ClickEvent event) {
 		    if (GroupControl.this.equals(currentGrouper)) {
 			ascending = !ascending;
 		    } else {
 			currentGrouper.unselect();
 		    }
 		    currentGrouper = GroupControl.this;
 		    forceGroup();
 		}
 	    });
 	    forceGroup();
 	}
 
 	private void unselect() {
 	    setIcon(null);
 	}
 
 	private void forceGroup() {
 	    if (ascending) {
 		setIcon(new ThemeResource("../runo/icons/16/arrow-up.png"));
 	    } else {
 		setIcon(new ThemeResource("../runo/icons/16/arrow-down.png"));
 	    }
 	    content.setGroupPropertyId(propertyId);
 	    if (currentSorter == null) {
 		sort(new Object[] { propertyId }, new boolean[] { ascending });
 	    } else {
 		sort(new Object[] { propertyId, currentSorter.propertyId }, new boolean[] { ascending, currentSorter.ascending });
 	    }
 	}
     }
 
     public class SorterControl extends Button {
 	private boolean ascending = true;
 
 	private final Object propertyId;
 
 	public SorterControl(final Object propertyId, String label) {
 	    this.propertyId = propertyId;
 	    setCaption(label);
 	    addStyleName(BaseTheme.BUTTON_LINK);
 	    addListener(new ClickListener() {
 		@Override
 		public void buttonClick(ClickEvent event) {
 		    if (SorterControl.this.equals(currentSorter)) {
 			ascending = !ascending;
 		    } else {
 			currentSorter.unselect();
 		    }
 		    currentSorter = SorterControl.this;
 		    forceSort();
 		}
 	    });
 	    forceSort();
 	}
 
 	private void unselect() {
 	    setIcon(null);
 	}
 
 	private void forceSort() {
 	    if (ascending) {
 		setIcon(new ThemeResource("../runo/icons/16/arrow-up.png"));
 	    } else {
 		setIcon(new ThemeResource("../runo/icons/16/arrow-down.png"));
 	    }
 	    if (currentGrouper == null) {
 		sort(new Object[] { propertyId }, new boolean[] { ascending });
 	    } else {
 		sort(new Object[] { currentGrouper.propertyId, propertyId },
 			new boolean[] { currentGrouper.ascending, ascending });
 	    }
 	}
     }
 
     public class FilterControl extends TextField {
 	public FilterControl(final Object propertyId, String label) {
 	    setTextChangeEventMode(TextChangeEventMode.LAZY);
 	    setTextChangeTimeout(200);
 	    setInputPrompt(label);
 	    addStyleName(Reindeer.TEXTFIELD_SMALL);
 	    addListener(new TextChangeListener() {
 		@Override
 		public void textChange(TextChangeEvent event) {
 		    filter(propertyId, event.getText());
 		}
 	    });
 	}
     }
 
     public static interface ContentViewerFactory {
 	public Viewer makeViewer();
     }
 
     public static class GroupWrapper extends VerticalLayout implements Viewer, ItemSetChangeListener {
 	private Object groupPropertyId;
 
 	private final ContentViewerFactory factory;
 
 	private Container container;
 
 	public GroupWrapper(ContentViewerFactory factory) {
 	    this.factory = factory;
	    setSpacing(true);
 	}
 
 	public void setGroupPropertyId(Object groupPropertyId) {
 	    this.groupPropertyId = groupPropertyId;
 	}
 
 	public Object getGroupPropertyId() {
 	    return groupPropertyId;
 	}
 
 	@Override
 	public void setContainerDataSource(Container newDataSource) {
 	    if (container != newDataSource) {
 		if (container != null) {
 		    if (container instanceof Container.ItemSetChangeNotifier) {
 			((Container.ItemSetChangeNotifier) container).removeListener(this);
 		    }
 		}
 
 		// Assigns new data source
 		container = newDataSource;
 
 		// Adds listeners
 		if (container != null) {
 		    if (container instanceof Container.ItemSetChangeNotifier) {
 			((Container.ItemSetChangeNotifier) container).addListener(this);
 		    }
 		}
 		refreshComponents(container);
 	    }
 	}
 
 	@Override
 	public Container getContainerDataSource() {
 	    return container;
 	}
 
 	@Override
 	public void containerItemSetChange(ItemSetChangeEvent event) {
 	    this.container = event.getContainer();
 	    refreshComponents(event.getContainer());
 	}
 
 	@Service
 	private void refreshComponents(Container container) {
 	    removeAllComponents();
 	    if (groupPropertyId != null) {
 		Map<Object, Container> groups = new HashMap<Object, Container>();
 		ArrayList<Object> ids = new ArrayList<Object>();
 		for (Object itemId : container.getItemIds()) {
 		    Object value = container.getContainerProperty(itemId, groupPropertyId).getValue();
 		    if (!groups.containsKey(value)) {
 			ids.add(value);
 			groups.put(value, cloneContainerStructure(container));
 		    }
 		    Item item = groups.get(value).addItem(itemId);
 		    for (Object property : container.getContainerPropertyIds()) {
 			item.getItemProperty(property).setValue(container.getContainerProperty(itemId, property).getValue());
 		    }
 		}
 		// if (groups.size() > 1) {
 		for (Object groupId : ids) {
 		    Panel section = new Panel(groupId != null ? groupId.toString() : null);
 		    addComponent(section);
 		    Viewer viewer = factory.makeViewer();
 		    viewer.setContainerDataSource(groups.get(groupId));
 		    section.addComponent((Component) viewer);
 		}
 		// } else if (groups.size() == 1) {
 		// Viewer viewer = factory.makeViewer();
 		// viewer.setContainerDataSource(groups.values().iterator().next());
 		// addComponent((Component) viewer);
 		// }
 	    } else {
 		Viewer viewer = factory.makeViewer();
 		viewer.setContainerDataSource(container);
 		addComponent((Component) viewer);
 	    }
 	}
     }
 
     static class ControlVisibilityListener implements ComponentAttachListener, ComponentDetachListener {
 	@Override
 	public void componentDetachedFromContainer(ComponentDetachEvent event) {
 	    determineVisibility(event.getContainer());
 	}
 
 	@Override
 	public void componentAttachedToContainer(ComponentAttachEvent event) {
 	    determineVisibility(event.getContainer());
 	}
 
 	public static void determineVisibility(ComponentContainer controls) {
 	    controls.setVisible(controls.getComponentIterator().hasNext());
 	}
     }
 
     private final GroupWrapper content;
 
     private GroupControl currentGrouper;
     private SorterControl currentSorter;
     private PageLengthControl pageLengthControl;
     private PageChangerControl pageChangerControl;
 
     private Indexed realContainer;
     private IndexedContainer shownContainer;
     private int index = 0;
     private int pageLength = 0;
     private List<PageChangeListener> listeners = null;
 
     public PaginatedSorterViewer(ContentViewerFactory factory) {
 	super(3, 3);
 	setWidth(100, UNITS_PERCENTAGE);
 	setSpacing(true);
 	this.content = new GroupWrapper(factory);
 	Component topleft = makeControlsLayout();
 	addComponent(topleft);
 	setComponentAlignment(topleft, Alignment.MIDDLE_LEFT);
 	Component topcenter = makeControlsLayout();
 	addComponent(topcenter);
 	setComponentAlignment(topcenter, Alignment.MIDDLE_CENTER);
 	Component topright = makeControlsLayout();
 	addComponent(topright);
 	setComponentAlignment(topright, Alignment.MIDDLE_RIGHT);
 	addComponent(content, 0, 1, 2, 1);
 	Component bottomleft = makeControlsLayout();
 	addComponent(bottomleft);
 	setComponentAlignment(bottomleft, Alignment.MIDDLE_LEFT);
 	Component bottomcenter = makeControlsLayout();
 	addComponent(bottomcenter);
 	setComponentAlignment(bottomcenter, Alignment.MIDDLE_CENTER);
 	Component bottomright = makeControlsLayout();
 	addComponent(bottomright);
 	setComponentAlignment(bottomright, Alignment.MIDDLE_RIGHT);
     }
 
     private static Component makeControlsLayout() {
 	HorizontalLayout layout = new HorizontalLayout();
 	layout.setSpacing(true);
 	layout.setVisible(false);
 	ControlVisibilityListener controlVisibilityListener = new ControlVisibilityListener();
 	layout.addListener((ComponentAttachListener) controlVisibilityListener);
 	layout.addListener((ComponentDetachListener) controlVisibilityListener);
 	return layout;
     }
 
     public void setPagination() {
 	setPagination(25);
     }
 
     public void setPagination(int pageLength) {
 	setPagination(pageLength, Alignment.BOTTOM_LEFT, Alignment.BOTTOM_RIGHT);
     }
 
     public void setPagination(int pageLength, Alignment pageLengthPosition, Alignment pageChangerPosition) {
 	pageLengthControl = new PageLengthControl(pageLength);
 	addControls(pageLengthControl, pageLengthPosition);
 	pageChangerControl = new PageChangerControl();
 	addControls(pageChangerControl, pageChangerPosition);
     }
 
     public void setGrouper(Object[] grouperIds, String[] grouperLabels) {
 	setGrouper(Alignment.TOP_LEFT, grouperIds, grouperLabels);
     }
 
     public void setGrouper(Alignment position, Object[] grouperIds, String[] grouperLabels) {
 	HorizontalLayout controls = new HorizontalLayout();
 	controls.setSpacing(true);
 	if (grouperIds.length > 0) {
 	    controls.setVisible(true);
 	    controls.addComponent(new Label(VaadinResources.getString(COMMONS_GROUPBY_LABEL) + ":"));
 	    currentGrouper = new GroupControl(grouperIds[0], grouperLabels[0]);
 	    controls.addComponent(currentGrouper);
 	    for (int i = 1; i < grouperIds.length; i++) {
 		controls.addComponent(new Label("|"));
 		controls.addComponent(new GroupControl(grouperIds[i], grouperLabels[i]));
 	    }
 	} else {
 	    controls.removeAllComponents();
 	    controls.setVisible(false);
 	}
 	addControls(controls, position);
     }
 
     public void setSorter(Object[] sorterIds, String[] sorterLabels) {
 	setSorter(Alignment.TOP_LEFT, sorterIds, sorterLabels);
     }
 
     public void setSorter(Alignment position, Object[] sorterIds, String[] sorterLabels) {
 	HorizontalLayout controls = new HorizontalLayout();
 	controls.setSpacing(true);
 	if (sorterIds.length > 0) {
 	    controls.setVisible(true);
 	    controls.addComponent(new Label(VaadinResources.getString(COMMONS_SORTBY_LABEL) + ":"));
 	    currentSorter = new SorterControl(sorterIds[0], sorterLabels[0]);
 	    controls.addComponent(currentSorter);
 	    for (int i = 1; i < sorterIds.length; i++) {
 		controls.addComponent(new Label("|"));
 		controls.addComponent(new SorterControl(sorterIds[i], sorterLabels[i]));
 	    }
 	} else {
 	    controls.removeAllComponents();
 	    controls.setVisible(false);
 	}
 	addControls(controls, position);
     }
 
     public void setFilter(Object[] filterIds, String[] filterLabels) {
 	setFilter(Alignment.TOP_RIGHT, filterIds, filterLabels);
     }
 
     public void setFilter(Alignment position, Object[] filterIds, String[] filterLabels) {
 	HorizontalLayout controls = new HorizontalLayout();
 	controls.setSpacing(true);
 	if (filterIds.length > 0) {
 	    controls.setVisible(true);
 	    controls.addComponent(new Label(VaadinResources.getString(COMMONS_FILTERBY_LABEL) + ":"));
 	    for (int i = 0; i < filterIds.length; i++) {
 		controls.addComponent(new FilterControl(filterIds[i], filterLabels[i]));
 	    }
 	} else {
 	    controls.removeAllComponents();
 	    controls.setVisible(false);
 	}
 	addControls(controls, position);
     }
 
     private void addControls(Component controls, Alignment position) {
 	HorizontalLayout controlBar;
 	int x = position.isLeft() ? 0 : (position.isCenter() ? 1 : 2);
 	int y = position.isTop() ? 0 : 2;
 	controlBar = (HorizontalLayout) getComponent(x, y);
 	controlBar.addComponent(controls);
     }
 
     @Override
     public void setContainerDataSource(Container newDataSource) {
 	if (newDataSource == null) {
 	    setVisible(false);
 	    return;
 	}
 	setVisible(true);
 	if (!(newDataSource instanceof Container.Indexed)) {
 	    throw new IllegalArgumentException("Can only use containers that implement Container.Indexed");
 	}
 	if (!(newDataSource instanceof Container.ItemSetChangeNotifier)) {
 	    throw new IllegalArgumentException("Can only use containers that implement Container.ItemSetChangeNotifier");
 	}
 	this.realContainer = (Indexed) newDataSource;
 	if (currentGrouper != null) {
 	    currentGrouper.forceGroup();
 	}
 	if (currentSorter != null) {
 	    currentSorter.forceSort();
 	}
 	// if (pageChangerControl != null) {
 	// pageChangerControl.update();
 	// }
 	cutShownContainer();
 	((ItemSetChangeNotifier) realContainer).addListener(new ItemSetChangeListener() {
 	    @Override
 	    public void containerItemSetChange(ItemSetChangeEvent event) {
 		cutShownContainer();
 	    }
 	});
     }
 
     @Override
     public Container getContainerDataSource() {
 	return realContainer;
     }
 
     public int getPageLength() {
 	return pageLength;
     }
 
     private void cutShownContainer() {
 	if (realContainer != null) {
 	    if (index < 0) {
 		index = 0;
 	    }
 	    if (index > realContainer.size() - 1) {
 		int pages = 0;
 		if (getPageLength() != 0) {
 		    pages = (int) Math.floor(0.0 + (realContainer.size() - 1) / getPageLength());
 		}
 		index = pages * getPageLength();
 	    }
 	    if (shownContainer == null) {
 		shownContainer = cloneContainerStructure(realContainer);
 	    }
 	    shownContainer.removeListener(content);
 	    shownContainer.removeAllItems();
 	    if (realContainer.size() != 0) {
 		Object itemId = realContainer.getIdByIndex(index);
 		addShownItem(itemId);
 		int lastIndex = getPageLength() == 0 ? realContainer.size() : getPageLength();
 		for (int i = 1; i < lastIndex; i++) {
 		    itemId = realContainer.nextItemId(itemId);
 		    if (itemId == null) {
 			break;
 		    }
 		    addShownItem(itemId);
 		}
 	    }
 	    if (listeners != null) {
 		PageChangeEvent event = new PageChangeEvent(this);
 		for (PageChangeListener listener : listeners) {
 		    listener.pageChanged(event);
 		}
 	    }
 	    shownContainer.addListener(content);
 	    content.containerItemSetChange(new ItemSetChangeEvent() {
 		@Override
 		public Container getContainer() {
 		    return shownContainer;
 		}
 	    });
 	}
     }
 
     private void addShownItem(Object itemId) {
 	Item realItem = realContainer.getItem(itemId);
 	Item shownItem = shownContainer.addItem(itemId);
 	for (Object property : realContainer.getContainerPropertyIds()) {
 	    shownItem.getItemProperty(property).setValue(realItem.getItemProperty(property).getValue());
 	}
     }
 
     public void setPageLength(int pageLength) {
 	if (pageLength >= 0 && getPageLength() != pageLength) {
 	    this.pageLength = pageLength;
 	    cutShownContainer();
 	}
     }
 
     public void nextPage() {
 	index += getPageLength();
 	cutShownContainer();
     }
 
     public void previousPage() {
 	index -= getPageLength();
 	cutShownContainer();
     }
 
     public int getCurrentPage() {
 	double pageLength = getPageLength();
 	int page = (int) Math.floor(index / pageLength) + 1;
 	if (page < 1) {
 	    page = 1;
 	}
 	return page;
     }
 
     public void setCurrentPage(int page) {
 	int newIndex = (page - 1) * getPageLength();
 	if (newIndex < 0) {
 	    newIndex = 0;
 	}
 	if (newIndex != index) {
 	    index = newIndex;
 	    cutShownContainer();
 	}
     }
 
     public int getTotalAmountOfPages() {
 	int size = realContainer.size();
 	double pageLength = getPageLength();
 	int pageCount = (int) Math.ceil(size / pageLength);
 	if (pageCount < 1) {
 	    pageCount = 1;
 	}
 	return pageCount;
     }
 
     public void addListener(PageChangeListener listener) {
 	if (listeners == null) {
 	    listeners = new ArrayList<PageChangeListener>();
 	}
 	listeners.add(listener);
     }
 
     public void removeListener(PageChangeListener listener) {
 	if (listeners == null) {
 	    listeners = new ArrayList<PageChangeListener>();
 	}
 	listeners.remove(listener);
     }
 
     public void sort(Object[] propertyId, boolean[] ascending) throws UnsupportedOperationException {
 	if (realContainer != null) {
 	    if (realContainer instanceof Sortable) {
 		((Sortable) realContainer).sort(propertyId, ascending);
 	    } else if (realContainer != null) {
 		throw new UnsupportedOperationException("Underlying Data does not allow sorting");
 	    }
 	}
     }
 
     public void filter(Object propertyId, String filterString) {
 	if (realContainer != null) {
 	    if (realContainer instanceof Filterable) {
 		Filterable filterable = (Filterable) realContainer;
 		filterable.removeAllContainerFilters();
 		filterable.addContainerFilter(new SimpleStringFilter(propertyId, filterString, true, false));
 	    } else if (realContainer != null) {
 		throw new UnsupportedOperationException("Underlying Data does not allow sorting");
 	    }
 	}
     }
 
     protected static IndexedContainer cloneContainerStructure(Container container) {
 	IndexedContainer cloned = new IndexedContainer();
 	for (Object propertyId : container.getContainerPropertyIds()) {
 	    cloned.addContainerProperty(propertyId, container.getType(propertyId), null);
 	}
 	return cloned;
     }
 }
