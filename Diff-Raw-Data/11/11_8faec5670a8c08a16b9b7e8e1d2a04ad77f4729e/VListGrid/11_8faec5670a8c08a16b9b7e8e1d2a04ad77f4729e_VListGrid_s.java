 package org.vaadin.smartgwt.client.ui.grid;
 
 import org.vaadin.smartgwt.client.core.JSON;
 import org.vaadin.smartgwt.client.core.PaintableListListener;
 import org.vaadin.smartgwt.client.core.PaintablePropertyUpdater;
 import org.vaadin.smartgwt.client.core.VJSObject;
 import org.vaadin.smartgwt.client.ui.utils.PainterHelper;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.smartgwt.client.core.DataClass;
 import com.smartgwt.client.data.DataSource;
 import com.smartgwt.client.util.JSOHelper;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
 import com.smartgwt.client.widgets.grid.events.SelectionEvent;
 import com.smartgwt.client.widgets.grid.events.SelectionUpdatedEvent;
 import com.smartgwt.client.widgets.grid.events.SelectionUpdatedHandler;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.Paintable;
 import com.vaadin.terminal.gwt.client.UIDL;
 
 public class VListGrid extends ListGrid implements Paintable {
 	private final PaintablePropertyUpdater propertyUpdater = new PaintablePropertyUpdater();
 	private final Element element = DOM.createDiv();
 	private String pid;
 	private ApplicationConnection client;
 
 	public VListGrid() {
 		propertyUpdater.addPaintableListListener("fields", new PaintableListListener() {
 			@Override
 			public void onAdd(Paintable[] source, Integer index, Paintable element) {
 				setFields(toListGridFieldArray(source));
 			}
 
 			@Override
 			public void onRemove(Paintable[] source, Integer index, Paintable element) {
 				setFields(toListGridFieldArray(source));
 			}
 
 			private ListGridField[] toListGridFieldArray(Paintable[] source) {
 				final ListGridField[] fields = new ListGridField[source.length];
 
 				for (int i = 0; i < source.length; i++) {
 					fields[i] = ((VListGridField) source[i]).getJSObject();
 				}
 
 				return fields;
 			}
 		});
 	}
 
 	@Override
 	public Element getElement() {
 		return element;
 	}
 
 	@Override
 	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
 		if (uidl.hasAttribute("cached")) {
 			return;
 		}
 
 		if (this.pid == null) {
 			this.pid = uidl.getId();
 			this.client = client;
 
 			addSelectionUpdatedHandler(new SelectionUpdatedHandler() {
 				@Override
 				public void onSelectionUpdated(SelectionUpdatedEvent event) {
 					final JavaScriptObject selectedRecordsJSA = toJSOArray(getSelectedRecords());
 					VListGrid.this.client.updateVariable(pid, "selectedRecords", JSON.stringify(selectedRecordsJSA), false);
 				}
 			});
 
			new ServerSideEventRegistration("*hasSelectionChangedHandlers") {
 				@Override
 				protected HandlerRegistration registerHandler() {
 					return addSelectionChangedHandler(new SelectionChangedHandler() {
 						@Override
 						public void onSelectionChanged(SelectionEvent event) {
 							final JavaScriptObject eventJSO = JavaScriptObject.createObject();
 							JSOHelper.setAttribute(eventJSO, "record", toJSO(event.getRecord()));
 							JSOHelper.setAttribute(eventJSO, "state", event.getState());
 							JSOHelper.setAttribute(eventJSO, "selection", toJSOArray(event.getSelection()));
 							JSOHelper.setAttribute(eventJSO, "selectedRecord", toJSO(event.getSelectedRecord()));
 							VListGrid.this.client.updateVariable(pid, "onSelectionChanged.event", JSON.stringify(eventJSO), true);
 						}
 					});
 				}
 			};
 
			new ServerSideEventRegistration("*hasSelectionUpdatedHandlers") {
 				@Override
 				protected HandlerRegistration registerHandler() {
 					return addSelectionUpdatedHandler(new SelectionUpdatedHandler() {
 						@Override
 						public void onSelectionUpdated(SelectionUpdatedEvent event) {
 							VListGrid.this.client.updateVariable(pid, "onSelectionUpdated.event", true, true);
 						}
 					});
 				}
 			};
 		}
 
 		propertyUpdater.updateFromUIDL(uidl, client);
 
 		if (uidl.hasAttribute("dataSource")) {
 			final Paintable paintable = uidl.getPaintableAttribute("dataSource", client);
 			setDataSource(((VJSObject<DataSource>) paintable).getJSObject());
 		}
 
 		PainterHelper.updateSmartGWTComponent(client, this, uidl);
 	}
 
 	private static JavaScriptObject toJSOArray(DataClass[] array) {
 		final JavaScriptObject arrayJSO = JavaScriptObject.createArray();
 		for (int i = 0; i < array.length; i++) {
 			JSOHelper.setArrayValue(arrayJSO, i, array[i].getJsObj());
 		}
 		return arrayJSO;
 	}
 
 	private static JavaScriptObject toJSO(DataClass dataClass) {
 		return dataClass == null ? null : dataClass.getJsObj();
 	}
 
 	private static abstract class ServerSideEventRegistration {
 		private final String uidlAttribute;
 		private HandlerRegistration registration;
 
 		public ServerSideEventRegistration(String uidlAttribute) {
 			this.uidlAttribute = uidlAttribute;
 		}
 
 		public void updateFromUIDL(UIDL uidl) {
 			if (uidl.hasAttribute(uidlAttribute) && registration == null) {
 				registration = registerHandler();
 			} else if (!uidl.hasAttribute(uidlAttribute) && registration != null) {
 				registration.removeHandler();
 				registration = null;
 			}
 		}
 
 		protected abstract HandlerRegistration registerHandler();
 	}
 }
