 package ch.bergturbenthal.marathontabelle.web;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.servlet.annotation.WebServlet;
 
 import ch.bergturbenthal.marathontabelle.generator.GeneratePdf;
 import ch.bergturbenthal.marathontabelle.model.MarathonData;
 import ch.bergturbenthal.marathontabelle.model.PhaseData;
 import ch.bergturbenthal.marathontabelle.model.TimeEntry;
 import ch.bergturbenthal.marathontabelle.web.binding.DurationFieldFactory;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.datatype.joda.JodaModule;
 import com.vaadin.annotations.Theme;
 import com.vaadin.annotations.VaadinServletConfiguration;
 import com.vaadin.data.Container;
 import com.vaadin.data.fieldgroup.BeanFieldGroup;
 import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.event.DataBoundTransferable;
 import com.vaadin.event.dd.DragAndDropEvent;
 import com.vaadin.event.dd.DropHandler;
 import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
 import com.vaadin.event.dd.acceptcriteria.SourceIs;
 import com.vaadin.server.StreamResource;
 import com.vaadin.server.StreamResource.StreamSource;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.server.VaadinServlet;
 import com.vaadin.shared.ui.dd.VerticalDropLocation;
 import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
 import com.vaadin.ui.AbstractTextField;
 import com.vaadin.ui.BrowserFrame;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.Table.ColumnGenerator;
 import com.vaadin.ui.Table.TableDragMode;
 import com.vaadin.ui.TableFieldFactory;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 
 @Theme("mytheme")
 @SuppressWarnings("serial")
 public class MyVaadinUI extends UI {
 	@WebServlet(value = "/*", asyncSupported = true)
 	@VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "ch.bergturbenthal.marathontabelle.web.AppWidgetSet")
 	public static class Servlet extends VaadinServlet {
 	}
 
 	private final ObjectMapper mapper = new ObjectMapper();
 	private final File file = new File(System.getProperty("user.home"), "marathon.json");
 
 	public MyVaadinUI() {
 		mapper.registerModule(new JodaModule());
 	}
 
 	private void container2Model(final BeanItemContainer<TimeEntry> itemContainer, final PhaseData phaseData) {
 		final List<TimeEntry> entries = phaseData.getEntries();
 		entries.clear();
 		entries.addAll(itemContainer.getItemIds());
 	}
 
 	private Object emptyIfNull(final Object value) {
 		if (value == null)
 			return null;
 		return value;
 	}
 
 	@Override
 	protected void init(final VaadinRequest request) {
 		final TabSheet tabLayout = new TabSheet();
 		final VerticalLayout layout = new VerticalLayout();
 		layout.addComponent(tabLayout);
 		setContent(layout);
 
 		final MarathonData marathonData = loadMarathonData();
 
 		final Collection<Runnable> saveRunnables = new ArrayList<Runnable>();
 
 		final FormLayout phaseATabContent = new FormLayout();
 		saveRunnables.add(showPhaseData(phaseATabContent, marathonData.getPhaseA()));
 		tabLayout.addTab(phaseATabContent, "Phase A");
 
 		final FormLayout phaseDTabContent = new FormLayout();
 		saveRunnables.add(showPhaseData(phaseDTabContent, marathonData.getPhaseD()));
 		tabLayout.addTab(phaseDTabContent, "Phase D");
 
 		final FormLayout phaseETabContent = new FormLayout();
 		saveRunnables.add(showPhaseData(phaseETabContent, marathonData.getPhaseE()));
 		tabLayout.addTab(phaseETabContent, "Phase E");
 
 		final VerticalLayout outputLayout = new VerticalLayout();
 		final FormLayout outputParameters = new FormLayout();
 
 		final CheckBox phaseAss = new CheckBox("Phase A");
 		phaseAss.setValue(Boolean.TRUE);
 		outputParameters.addComponent(phaseAss);
 
 		final CheckBox phaseDss = new CheckBox("Phase D");
 		phaseDss.setValue(Boolean.FALSE);
 		outputParameters.addComponent(phaseDss);
 
 		final CheckBox phaseEss = new CheckBox("Phase E");
 		phaseEss.setValue(Boolean.TRUE);
 		outputParameters.addComponent(phaseEss);
 
 		outputLayout.setSizeFull();
 		outputLayout.addComponent(outputParameters);
 		outputLayout.setExpandRatio(outputParameters, 0);
 		final StreamResource source = new StreamResource(new StreamSource() {
 
 			@Override
 			public InputStream getStream() {
 				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				new GeneratePdf().makePdf(os, marathonData, phaseAss.getValue().booleanValue(), phaseDss.getValue().booleanValue(), phaseEss.getValue().booleanValue());
 				return new ByteArrayInputStream(os.toByteArray());
 			}
 		}, makeOutputFilename());
 		source.setMIMEType("application/pdf");
 		final BrowserFrame pdf = new BrowserFrame("Output", source);
 		pdf.setSizeFull();
 		outputLayout.addComponent(pdf);
 		outputLayout.setExpandRatio(pdf, 1);
 		tabLayout.addTab(outputLayout, "Resultat");
 		tabLayout.setSizeFull();
 		layout.setExpandRatio(tabLayout, 1);
 
 		layout.setSizeFull();
 
 		saveRunnables.add(new Runnable() {
 			@Override
 			public void run() {
 				saveMarathonData(marathonData);
 			}
 		});
 		saveRunnables.add(new Runnable() {
 
 			@Override
 			public void run() {
 				source.setFilename(makeOutputFilename());
 				pdf.markAsDirty();
 			}
 		});
 		final Button saveButton = new Button("Übernehmen", new ClickListener() {
 			@Override
 			public void buttonClick(final ClickEvent event) {
 				for (final Runnable runnable : saveRunnables) {
 					runnable.run();
 				}
 			}
 		});
 		layout.addComponent(saveButton);
 	}
 
 	private MarathonData loadMarathonData() {
 		try {
 			if (file.exists())
 				return mapper.readValue(file, MarathonData.class);
 
 			return new MarathonData(new PhaseData(), new PhaseData(), new PhaseData());
 		} catch (final IOException e) {
 			throw new RuntimeException("Cannot load " + file, e);
 		}
 	}
 
 	private String makeOutputFilename() {
 		return "marathon-" + System.currentTimeMillis() + ".pdf";
 	}
 
 	private void model2Container(final PhaseData phaseData, final BeanItemContainer<TimeEntry> itemContainer) {
 		itemContainer.removeAllItems();
 		itemContainer.addAll(phaseData.getEntries());
 	}
 
 	private void saveMarathonData(final MarathonData data) {
 		try {
 			mapper.writeValue(file, data);
 		} catch (final IOException e) {
 			throw new RuntimeException("Cannot write " + file, e);
 		}
 	}
 
 	private Runnable showPhaseData(final FormLayout layout, final PhaseData phaseData) {
 		layout.setMargin(true);
 		final BeanFieldGroup<PhaseData> binder = new BeanFieldGroup<PhaseData>(PhaseData.class);
 		final DurationFieldFactory fieldFactory = new DurationFieldFactory();
 		binder.setFieldFactory(fieldFactory);
 		binder.setItemDataSource(phaseData);
 
 		layout.addComponent(binder.buildAndBind("geplante Startzeit", "startTime"));
 		layout.addComponent(binder.buildAndBind("Maximale Zeit", "maxTime"));
 		layout.addComponent(binder.buildAndBind("Minimale Zeit", "minTime"));
 		layout.addComponent(binder.buildAndBind("Länge in m", "length"));
 		layout.addComponent(binder.buildAndBind("Geschwindigkeit im m/s", "velocity"));
 		// layout.addComponent(binder.buildAndBind("Tabelle", "entries"));
 
 		final BeanItemContainer<TimeEntry> itemContainer = new BeanItemContainer<TimeEntry>(TimeEntry.class);
 		itemContainer.addAll(phaseData.getEntries());
 
 		final Table table = new Table("Strecke");
 		table.setContainerDataSource(itemContainer);
 		table.setEditable(true);
 		table.setSortEnabled(false);
 		table.setNullSelectionAllowed(true);
 		layout.addComponent(table);
 		table.setDragMode(TableDragMode.ROW);
 		table.setDropHandler(new DropHandler() {
 
 			@Override
 			public void drop(final DragAndDropEvent dropEvent) {
 				final DataBoundTransferable t = (DataBoundTransferable) dropEvent.getTransferable();
 				final TimeEntry sourceItemId = (TimeEntry) t.getItemId(); // returns our Bean
 
 				final AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
 				final TimeEntry targetItemId = (TimeEntry) dropData.getItemIdOver(); // returns our Bean
 
 				// No move if source and target are the same, or there is no target
 				if (sourceItemId == targetItemId || targetItemId == null)
 					return;
 
 				// Let's remove the source of the drag so we can add it back where requested...
 				itemContainer.removeItem(sourceItemId);
 
 				if (dropData.getDropLocation() == VerticalDropLocation.BOTTOM) {
 					itemContainer.addItemAfter(targetItemId, sourceItemId);
 				} else {
 					final Object prevItemId = itemContainer.prevItemId(targetItemId);
 					itemContainer.addItemAfter(prevItemId, sourceItemId);
 				}
 			}
 
 			@Override
 			public AcceptCriterion getAcceptCriterion() {
 				return new SourceIs(table);
 			}
 		});
 		table.addGeneratedColumn("", new ColumnGenerator() {
 
 			@Override
 			public Object generateCell(final Table source, final Object itemId, final Object columnId) {
 				final Button button = new Button("Löschen");
 				button.addClickListener(new ClickListener() {
 					@Override
 					public void buttonClick(final ClickEvent event) {
 						source.getContainerDataSource().removeItem(itemId);
 					}
 				});
 				return button;
 			}
 		});
 		final TableFieldFactory tableFieldFactory = new TableFieldFactory() {
 
 			@Override
 			public Field<?> createField(final Container container, final Object itemId, final Object propertyId, final Component uiContext) {
 				final Class<?> type = container.getType(propertyId);
 				final Field field = fieldFactory.createField(type, Field.class);
 				if (field instanceof AbstractTextField) {
 					final AbstractTextField abstractTextField = (AbstractTextField) field;
 					abstractTextField.setNullRepresentation("");
 					abstractTextField.setValidationVisible(true);
 				}
 				return field;
 			}
 		};
 		table.setTableFieldFactory(tableFieldFactory);
 
 		layout.addComponent(new Button("Neuer Steckenpunkt", new ClickListener() {
 
 			@Override
 			public void buttonClick(final ClickEvent event) {
 				itemContainer.addBean(new TimeEntry());
 			}
 		}));
 
 		layout.addComponent(new Button("Reset Strecke", new ClickListener() {
 
 			@Override
 			public void buttonClick(final ClickEvent event) {
 				try {
 					binder.commit();
 					container2Model(itemContainer, phaseData);
 
 					phaseData.setDefaultPoints();
 
 					model2Container(phaseData, itemContainer);
 					System.out.println(phaseData);
 				} catch (final CommitException e) {
 				}
 			}
 		}));
 		return new Runnable() {
 			@Override
 			public void run() {
 				try {
 					binder.commit();
 					container2Model(itemContainer, phaseData);
 				} catch (final CommitException e) {
 					throw new RuntimeException("Cannot commit", e);
 				}
 			}
 		};
 	}
 }
