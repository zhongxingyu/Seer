 package com.jcertif.presentation.ui.calendar;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.jcertif.presentation.data.bo.cedule.Evenement;
 import com.jcertif.presentation.data.bo.participant.Participant;
 import com.jcertif.presentation.data.bo.presentation.PropositionPresentation;
 import com.vaadin.Application;
 import com.vaadin.addon.calendar.event.CalendarEvent;
 import com.vaadin.addon.calendar.ui.CalendarComponentEvents.EventClick;
 import com.vaadin.addon.calendar.ui.CalendarComponentEvents.EventClickHandler;
 import com.vaadin.terminal.ExternalResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.CustomLayout;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.themes.Runo;
 
 /**
  * @author rossi
  * 
  */
 public class CalendarApplication extends Application implements EventClickHandler {
 
 	private static final long serialVersionUID = 1L;
 	private static final Logger LOGGER = LoggerFactory.getLogger(CalendarApplication.class);
 	private JCertifCalendar calendar;
 	private Panel detailPanel;
 	private Window mainWindow;
 
 	@Override
 	public void init() {
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Building Calendar Application");
 		}
 		setTheme("jcertifruno");
 		mainWindow = new Window();
 
 		// CustomLayout custom = new CustomLayout("details_event_layout");
 		// custom.addStyleName("customlayoutexample");
 
 		// Use it as the layout of the Panel.
 		// getDetailPanel().setContent(custom);
 		mainWindow.addStyleName("jcertif_calendar");
 		mainWindow.getContent().addComponent(getCalendarComponent());
 		DateFormat dateF = new SimpleDateFormat("dd/MM/yyyy HH");
 		Date startDate = null;
 		Date endDate = null;
 		try {
 			endDate = dateF.parse("03/09/2011 09");
 			startDate = dateF.parse("04/09/2011 21");
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		List<CalendarEvent> events = getCalendarComponent().getEventProvider().getEvents(startDate,
 				endDate);
 		CalendarEvent firstEvent = null;
 		for (CalendarEvent calendarEvent : events) {
 			if (firstEvent == null) {
 				firstEvent = calendarEvent;
 			} else {
 				if (firstEvent.getStart().after(calendarEvent.getStart())) {
 					firstEvent = calendarEvent;
 				}
 			}
 
 		}
 
 		if (firstEvent != null) {
 			updateDetailPanel((JCertifCalendarEvent) firstEvent);
 		}
 
 		setMainWindow(mainWindow);
 
 	}
 
 	private JCertifCalendar getCalendarComponent() {
 		if (calendar == null) {
 			calendar = new JCertifCalendar();
 			calendar.setEventProvider(new JCertifEventProvider());
			calendar.setStyleName(Runo.LAYOUT_DARKER);
 			calendar.setHandler(this);
 		}
 		return calendar;
 	}
 
 	private Panel getDetailPanel() {
 		if (detailPanel == null) {
 			detailPanel = new Panel();
 			detailPanel.setStyleName("event_details_panel");
 		}
 		return detailPanel;
 	}
 
 	private void updateDetailPanel(final JCertifCalendarEvent event) {
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Updating Detail Panel");
 		}
 		event.setStyleName("selected");
 		getDetailPanel().removeAllComponents();
 		// VerticalLayout layout = new VerticalLayout();
 		// layout.setStyleName("details_event");
 
 		CustomLayout custom = new CustomLayout("details_event_layout");
 		custom.addStyleName("details_event_layout");
 
 		// Use it as the layout of the Panel.
 		getDetailPanel().setContent(custom);
 
 		String libelleSalle = event.getJcertifEvent().getCeduleSalles().iterator().next()
 				.getSalle().getLibelle();
 
 		custom.addComponent(new Label("Salle " + libelleSalle), "ou");
 
 		Date debut = event.getJcertifEvent().getDateDebutPrevue().getTime();
 		custom.addComponent(
 				new Label(new SimpleDateFormat("EEEEEEEE").format(debut)
 						+ " de "
 						+ new SimpleDateFormat("HH:mm").format(debut)
 						+ "  "
 						+ new SimpleDateFormat("HH:mm").format(event.getJcertifEvent()
 								.getDateFinPrevue().getTime())), "quand");
 		custom.addComponent(new Label(findSujet(event.getJcertifEvent())), "categorie");
 		Set<Participant> participantSet = findParticipant(event.getJcertifEvent());
 
 		if (participantSet == null || participantSet.isEmpty()) {
 			LOGGER.warn("Pas de prsentation pour cet vnement");
 		} else {
 			// TODO Grer le cas de plusieurs participant
 			Participant participant = participantSet.iterator().next();
 
 			custom.addComponent(new Label(event.getJcertifEvent().getPropositionPresentation()
 					.getTitre()), "titre");
 
 			custom.addComponent(new Button("je veux participer"), "participer");
 
 			custom.addComponent(new Label(participant.getNom() + " " + participant.getPrenom()),
 					"presentateur");
 
 			if (participant.getProfilUtilisateur() != null
 					&& participant.getProfilUtilisateur().getPhoto() != null) {
 				ExternalResource res = new ExternalResource("./images/speakers/"
 						+ participant.getProfilUtilisateur().getPhoto());
 
 				// Display the image in an Embedded component.
 				Embedded embedded = new Embedded("", res);
 				embedded.addStyleName("photo_speaker");
 
 				custom.addComponent(embedded, "photo");
 			}
 
 			custom.addComponent(new Label(participant.getDetails()), "details");
 
 			custom.addComponent(new Label(event.getJcertifEvent().getPropositionPresentation()
 					.getMotCle().getMotCle()), "motcle");
 		}
 
 		final HorizontalLayout layoutH = new HorizontalLayout();
 		layoutH.setSizeFull();
 
 		layoutH.addComponent(getCalendarComponent());
 		layoutH.setExpandRatio(getCalendarComponent(), 3);
 		layoutH.addComponent(getDetailPanel());
 		layoutH.setExpandRatio(getDetailPanel(), 1);
 		mainWindow.getContent().removeAllComponents();
 		mainWindow.getContent().addComponent(layoutH);
 
 	}
 
 	private String findSujet(final Evenement event) {
 		String sujet = "<Aucun>";
 		PropositionPresentation pres = event.getPropositionPresentation();
 
 		if (pres.getSujets() == null || pres.getSujets().isEmpty()) {
 			LOGGER.warn("Evenement sans Sujet, Evenement.id={0}", event.getId());
 		} else {
 			sujet = pres.getSujets().iterator().next().getLibelle();
 		}
 
 		return sujet;
 	}
 
 	private Set<Participant> findParticipant(final Evenement event) {
 		Set<Participant> participantSet = event.getPropositionPresentation().getParticipants();
 		if (participantSet == null) {
 			LOGGER.warn("Evenement sans participant Evenement.id={0}", event.getId());
 		}
 		return participantSet;
 	}
 
 	@Override
 	public void eventClick(EventClick event) {
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Event Click on Calendar Application");
 		}
 		updateDetailPanel((JCertifCalendarEvent) event.getCalendarEvent());
 	}
 
 }
