 package org.iucn.sis.client.panels.assessments;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.iucn.sis.client.api.caches.ChangesFieldWidgetCache;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.utils.BasicWindow;
 import org.iucn.sis.client.api.utils.FormattedDate;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.shared.api.displays.Display;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentChange;
 import org.iucn.sis.shared.api.models.Edit;
 import org.iucn.sis.shared.api.models.Field;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.ButtonBar;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 
 public class TrackChangesPanel extends BasicWindow implements DrawsLazily {
 	
 	private final Assessment assessment;
 	private Edit edit;
 	
 	private final LayoutContainer oldField, newField;
 	private final ChangesFieldWidgetCache cache;
 	
 	public TrackChangesPanel(Assessment assessment) {
 		this(assessment, null);
 	}
 
 	public TrackChangesPanel(Assessment assessment, Edit edit) {
 		super("View Assessment Changes", "icon-changes");
 		setSize(800, 650);
 		setLayout(new FillLayout());
 		
 		this.assessment = assessment;
 		this.edit = edit;
 		
 		oldField = createChangeContainer();
 		newField = createChangeContainer();
 		
 		cache = ChangesFieldWidgetCache.get();
 	}
 	
 	private LayoutContainer createChangeContainer() {
 		LayoutContainer container = new LayoutContainer(new FlowLayout(0));
 		container.addStyleName("x-panel");
 		//container.setScrollMode(Scroll.AUTO);
 		
 		return container;
 	}
 	
 	@Override
 	public void show() {
 		draw(new DrawsLazily.DoneDrawingCallback() {
 			public void isDrawn() {
 				open();
 				if (edit != null)
 					view(edit);
 			}
 		});
 	}
 	
 	public void open() {
 		super.show();
 	}
 	
 	@Override
 	public void draw(final DoneDrawingCallback callback) {
 		drawLeft(new DoneDrawingCallbackWithParam<LayoutContainer>() {
 			public void isDrawn(LayoutContainer parameter) {
 				final ContentPanel oldFieldPanel = new ContentPanel();
 				oldFieldPanel.setHeading("Old Version");
 				oldFieldPanel.setScrollMode(Scroll.AUTO);
 				oldFieldPanel.add(oldField);
 				
 				final ContentPanel newFieldPanel = new ContentPanel();
 				newFieldPanel.setHeading("New Version");
 				newFieldPanel.setScrollMode(Scroll.AUTO);
 				newFieldPanel.add(newField);
 				
 				final BorderLayoutData oldFieldLD = new BorderLayoutData(LayoutRegion.NORTH, 300);
 				oldFieldLD.setCollapsible(true);
 				oldFieldLD.setFloatable(true);
 				
 				final BorderLayoutData newFieldLD = new BorderLayoutData(LayoutRegion.CENTER, 300);
 				newFieldLD.setCollapsible(true);
 				newFieldLD.setFloatable(true);
 				
 				final LayoutContainer center = new LayoutContainer(new BorderLayout());
 				center.add(oldFieldPanel, oldFieldLD);
 				center.add(newFieldPanel, newFieldLD);
 				
 				final LayoutContainer container = new LayoutContainer(new BorderLayout());
 				container.add(parameter, new BorderLayoutData(LayoutRegion.WEST, 200, 200, 200));
 				container.add(center, new BorderLayoutData(LayoutRegion.CENTER));
 				
 				add(container);
 				
 				callback.isDrawn();
 			}
 		});
 	}
 
 	/*
 	 * TODO: maybe pull this live from the server
 	 * via /changes/assessments/{id}
 	 */
 	private void drawLeft(final DrawsLazily.DoneDrawingCallbackWithParam<LayoutContainer> callback) {
 		final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
 		document.get(UriBase.getInstance().getSISBase() + "/changes/assessments/" + assessment.getId(), new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				final FlowLayout l = new FlowLayout();
 				l.setMargins(new Margins(5, 0, 5, 0));
 				
 				final LayoutContainer container = new LayoutContainer(l);
 				container.setScrollMode(Scroll.AUTOY);
 				
 				final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName(Edit.ROOT_TAG);
 				for (int i = 0; i < nodes.getLength(); i++) {
 					final Edit edit = Edit.fromXML(nodes.elementAt(i)); 
 				
 					final ButtonBar bar = new ButtonBar();
 					bar.setAlignment(HorizontalAlignment.CENTER);
 					bar.add(new Button("View", new SelectionListener<ButtonEvent>() {
 						public void componentSelected(ButtonEvent ce) {
 							view(edit);
 						}
 					}));
 					
 					final LayoutContainer info = new LayoutContainer();
 					info.addStyleName("page_assessment_trackChanges_edits");
 					info.add(new Html("<span class=\"page_assessment_trackChanges_edits_user\">" + edit.getUser().getDisplayableName() + "</span>"));
 					info.add(new Html("<span>" + FormattedDate.FULL.getDate(edit.getCreatedDate()) + "</span>"));
					if (edit.getReason() != null)
						info.add(new Html("<span>" + edit.getReason() + "</span>"));
 					info.add(bar);
 					
 					container.add(info);
 				}
 				
 				callback.isDrawn(container);
 			}
 			public void onFailure(Throwable caught) {
 				WindowUtils.errorAlert("Failed to load change history, please try again later.");
 			}
 		});
 	}
 	
 	private void view(Edit edit) {
 		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 		document.get(UriBase.getInstance().getSISBase() + "/changes/assessments/" + assessment.getId() + "/edit/" + edit.getId(), new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				final Map<String, AssessmentChange> changes = new LinkedHashMap<String, AssessmentChange>();
 				final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("change");
 				for (int i = 0; i < nodes.getLength(); i++) {
 					AssessmentChange change = AssessmentChange.fromXML(nodes.elementAt(i));
 					changes.put(change.getFieldName(), change);
 				}
 			
				if (!changes.isEmpty())
					showChanges(changes);
				else
					WindowUtils.infoAlert("No field changes available for this edit.");
 			}
 			public void onFailure(Throwable caught) {
 				WindowUtils.errorAlert("Could not load this change, please try again later.");
 			}
 		});		
 	}
 	
 	private void showChanges(final Map<String, AssessmentChange> changes) {
 		cache.prefetchList(changes.keySet(), new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				oldField.removeAll();
 				newField.removeAll();
 				oldField.mask("Loading...");
 				newField.mask("Loading...");
 				DeferredCommand.addCommand(new Command() {
 					public void execute() {
 						for (AssessmentChange change : changes.values()) {
 							showChange(change.getOldField(), change.getFieldName(), oldField, true);
 							showChange(change.getNewField(), change.getFieldName(), newField, false);
 						}
 						oldField.unmask();
 						newField.unmask();
 						oldField.layout(true);
 						newField.layout(true);
 					}
 				});
 			}
 			public void onFailure(Throwable caught) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 	}
 	
 	private void showChange(Field field, String fieldName, LayoutContainer target, boolean isOld) {
 		final Display display = isOld ? cache.getOldWidget(fieldName, field) : cache.getNewWidget(fieldName, field);
 		if (display == null)
 			return;
 		
 		display.setData(field);
 		
 		final HorizontalPanel myContent = new HorizontalPanel();
 		myContent.addStyleName("SISPage_Field");
 		myContent.add(display.showViewOnly());
 		myContent.setWidth("100%");
 		
 		target.add(myContent);
 	}
 	
 }
