 /*
  * Copyright 2010 Ian Hilt
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.eastway.echarts.client;
 
 import net.customware.gwt.presenter.client.EventBus;
 
 import org.eastway.echarts.client.common.AddressColumnDefinitionsImpl;
 import org.eastway.echarts.client.common.DemographicsColumnDefinitionsImpl;
 import org.eastway.echarts.client.common.DiagnosisColumnDefinitionsImpl;
 import org.eastway.echarts.client.events.LogoutEvent;
 import org.eastway.echarts.client.events.LogoutEventHandler;
 import org.eastway.echarts.client.events.OpenCpstNoteEvent;
 import org.eastway.echarts.client.events.OpenCpstNoteEventHandler;
 import org.eastway.echarts.client.events.OpenDoctorProgressNoteEvent;
 import org.eastway.echarts.client.events.OpenDoctorProgressNoteEventHandler;
 import org.eastway.echarts.client.events.OpenEhrEvent;
 import org.eastway.echarts.client.events.OpenEhrEventHandler;
 import org.eastway.echarts.client.events.OpenIndividualProgressNoteEvent;
 import org.eastway.echarts.client.events.OpenIndividualProgressNoteEventHandler;
 import org.eastway.echarts.client.events.OpenIspEvent;
 import org.eastway.echarts.client.events.OpenIspEventHandler;
 import org.eastway.echarts.client.events.OpenNurseProgressNoteEvent;
 import org.eastway.echarts.client.events.OpenNurseProgressNoteEventHandler;
 import org.eastway.echarts.client.events.ViewAddressesEvent;
 import org.eastway.echarts.client.events.ViewAddressesEventHandler;
 import org.eastway.echarts.client.events.ViewAppointmentsEvent;
 import org.eastway.echarts.client.events.ViewAppointmentsEventHandler;
 import org.eastway.echarts.client.events.ViewContactsEvent;
 import org.eastway.echarts.client.events.ViewContactsEventHandler;
 import org.eastway.echarts.client.events.ViewDemographicsEvent;
 import org.eastway.echarts.client.events.ViewDemographicsEventHandler;
 import org.eastway.echarts.client.events.ViewDiagnosesEvent;
 import org.eastway.echarts.client.events.ViewDiagnosesEventHandler;
 import org.eastway.echarts.client.events.ViewLinksEvent;
 import org.eastway.echarts.client.events.ViewLinksEventHandler;
 import org.eastway.echarts.client.events.ViewMedicationsEvent;
 import org.eastway.echarts.client.events.ViewMedicationsEventHandler;
 import org.eastway.echarts.client.events.ViewMessagesEvent;
 import org.eastway.echarts.client.events.ViewMessagesEventHandler;
 import org.eastway.echarts.client.events.ViewPatientSummaryEvent;
 import org.eastway.echarts.client.events.ViewPatientSummaryEventHandler;
 import org.eastway.echarts.client.events.ViewProfileEvent;
 import org.eastway.echarts.client.events.ViewProfileEventHandler;
 import org.eastway.echarts.client.events.ViewReferralEvent;
 import org.eastway.echarts.client.events.ViewReferralEventHandler;
 import org.eastway.echarts.client.events.ViewServiceHistoryEvent;
 import org.eastway.echarts.client.events.ViewServiceHistoryEventHandler;
 import org.eastway.echarts.client.events.ViewSignatureEvent;
 import org.eastway.echarts.client.events.ViewSignatureEventHandler;
 import org.eastway.echarts.client.events.ViewTicklerEvent;
 import org.eastway.echarts.client.events.ViewTicklerEventHandler;
 import org.eastway.echarts.client.events.ViewTreatmentPlanEvent;
 import org.eastway.echarts.client.events.ViewTreatmentPlanEventHandler;
 import org.eastway.echarts.client.presenter.AddressPresenter;
 import org.eastway.echarts.client.presenter.AppointmentPresenter;
 import org.eastway.echarts.client.presenter.ContactPresenter;
 import org.eastway.echarts.client.presenter.DashboardPresenter;
 import org.eastway.echarts.client.presenter.DemographicsPresenter;
 import org.eastway.echarts.client.presenter.DiagnosisPresenter;
 import org.eastway.echarts.client.presenter.EHRPresenter;
 import org.eastway.echarts.client.presenter.LinkPresenter;
 import org.eastway.echarts.client.presenter.MedicationPresenter;
 import org.eastway.echarts.client.presenter.MessagesPresenter;
 import org.eastway.echarts.client.presenter.PatientSummaryPresenter;
 import org.eastway.echarts.client.presenter.Presenter;
 import org.eastway.echarts.client.presenter.ProfilePresenter;
 import org.eastway.echarts.client.presenter.ReferralPresenter;
 import org.eastway.echarts.client.presenter.TicklerPresenter;
 import org.eastway.echarts.client.rpc.CachingDispatchAsync;
 import org.eastway.echarts.client.view.AddressViewImpl;
 import org.eastway.echarts.client.view.AppointmentView;
 import org.eastway.echarts.client.view.ContactView;
 import org.eastway.echarts.client.view.DemographicsViewImpl;
 import org.eastway.echarts.client.view.DiagnosisViewImpl;
 import org.eastway.echarts.client.view.EHRView;
 import org.eastway.echarts.client.view.EHRViewImpl;
 import org.eastway.echarts.client.view.LinkView;
 import org.eastway.echarts.client.view.MedicationView;
 import org.eastway.echarts.client.view.MessagesView;
 import org.eastway.echarts.client.view.PatientSummaryView;
 import org.eastway.echarts.client.view.ReferralView;
 import org.eastway.echarts.shared.Address;
 import org.eastway.echarts.shared.Demographics;
 import org.eastway.echarts.shared.Diagnosis;
 import org.eastway.echarts.shared.EHR;
 import org.eastway.echarts.shared.GetAddresses;
 import org.eastway.echarts.shared.GetAppointments;
 import org.eastway.echarts.shared.GetContacts;
 import org.eastway.echarts.shared.GetDemographics;
 import org.eastway.echarts.shared.GetDiagnoses;
 import org.eastway.echarts.shared.GetLinks;
 import org.eastway.echarts.shared.GetMedications;
 import org.eastway.echarts.shared.GetMessages;
 import org.eastway.echarts.shared.GetPatientSummary;
 import org.eastway.echarts.shared.GetReferral;
 
 import com.google.gwt.requestfactory.shared.RequestEvent;
 import com.google.gwt.requestfactory.shared.RequestEvent.State;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.inject.Inject;
 
 public class AppController implements Presenter {
 	private final EventBus eventBus;
 	private EHRView<EHR> ehrView;
 	private CachingDispatchAsync dispatch;
 	private DashboardPresenter dashboardPresenter;
 	@Inject private TicklerPresenter ticklerPresenter;
 	@Inject private ProfilePresenter profilePresenter;
 	@Inject private DiagnosisColumnDefinitionsImpl diagnosisColumnDefinitions;
 	@Inject private DemographicsColumnDefinitionsImpl demographicsColumnDefinitions;
 	@Inject private AddressColumnDefinitionsImpl addressColumnDefinitions;
 
 	@Inject
 	public AppController(DashboardPresenter dashboardPresenter, EventBus eventBus, CachingDispatchAsync dispatch) {
 		this.dashboardPresenter = dashboardPresenter;
 		this.eventBus = eventBus;
 		this.dispatch = dispatch;
 		bind();
 	}
 
 	private void bind() {
 		eventBus.addHandler(OpenEhrEvent.TYPE, new OpenEhrEventHandler() {
 			@Override
 			public void onOpenEhr(OpenEhrEvent event) {
 				doViewEhr(event.getCaseNumber());
 			}
 		});
 		eventBus.addHandler(ViewPatientSummaryEvent.TYPE, new ViewPatientSummaryEventHandler() {
 			@Override
 			public void onViewPatientSummary(ViewPatientSummaryEvent event) {
 				doViewPatientSummary(event.getView(), event.getAction());
 			}
 		});
 		eventBus.addHandler(ViewMessagesEvent.TYPE, new ViewMessagesEventHandler() {
 			@Override
 			public void onViewMessages(ViewMessagesEvent event) {
 				doViewMessages(event.getView(), event.getAction());
 			}
 		});
 		eventBus.addHandler(ViewReferralEvent.TYPE, new ViewReferralEventHandler() {
 			@Override
 			public void onViewReferral(ViewReferralEvent event) {
 				doViewReferral(event.getView(), event.getAction());
 			}
 		});
 		eventBus.addHandler(ViewAppointmentsEvent.TYPE, new ViewAppointmentsEventHandler() {
 			@Override
 			public void onViewAppointments(ViewAppointmentsEvent event) {
 				doViewAppointments(event.getView(), event.getAction());
 			}
 		});
 		eventBus.addHandler(ViewDiagnosesEvent.TYPE, new ViewDiagnosesEventHandler() {
 			@Override
 			public void onViewDiagnoses(ViewDiagnosesEvent event) {
 				doViewDiagnoses(event.getView(), event.getCaseNumber(), event.getAction());
 			}
 		});
 		eventBus.addHandler(ViewLinksEvent.TYPE, new ViewLinksEventHandler() {
 			@Override
 			public void onViewLinks(ViewLinksEvent event) {
 				doViewLinksEvent(event.getView(), event.getAction());
 			}
 		});
 		eventBus.addHandler(ViewAddressesEvent.TYPE, new ViewAddressesEventHandler() {
 			@Override
 			public void onViewAddresses(ViewAddressesEvent event) {
 				doViewAddresses(event.getView(), event.getAction());
 			}
 		});
 		eventBus.addHandler(ViewContactsEvent.TYPE, new ViewContactsEventHandler() {
 			@Override
 			public void onViewContacts(ViewContactsEvent event) {
 				doViewContacts(event.getView(), event.getAction());
 			}
 		});
 		eventBus.addHandler(ViewMedicationsEvent.TYPE, new ViewMedicationsEventHandler() {
 			@Override
 			public void onViewMedications(ViewMedicationsEvent event) {
 				doViewMedications(event.getView(), event.getAction());
 			}
 		});
 		eventBus.addHandler(ViewTicklerEvent.TYPE, new ViewTicklerEventHandler() {
 			@Override
 			public void onOpenTickler(ViewTicklerEvent event) {
 				doViewTickler();
 			}
 		});
 		eventBus.addHandler(ViewProfileEvent.TYPE, new ViewProfileEventHandler() {
 			@Override
 			public void onViewProfile(ViewProfileEvent event) {
 				doViewProfile();
 			}
 		});
 		eventBus.addHandler(RequestEvent.TYPE, new RequestEvent.Handler() {
 			// Only show loading status if a request isn't serviced in 250ms.
 			private static final int LOADING_TIMEOUT = 250;
 			private int REQUEST_EVENT_COUNT = 0;
 			@Override
 			public void onRequestEvent(RequestEvent requestEvent) {
 				if (requestEvent.getState() == State.SENT) {
 					dashboardPresenter.getDisplay().getMole().showDelayed(LOADING_TIMEOUT);
 					REQUEST_EVENT_COUNT++;
 				} else if (REQUEST_EVENT_COUNT > 1) {
 					REQUEST_EVENT_COUNT--;
 				} else {
 					REQUEST_EVENT_COUNT--;
 					dashboardPresenter.getDisplay().getMole().hide();
 				}
 			}
 		});
 		eventBus.addHandler(ViewDemographicsEvent.TYPE, new ViewDemographicsEventHandler() {
 			@Override
 			public void onViewDemographics(ViewDemographicsEvent event) {
 				doViewDemographics(event.getView(), event.getAction());
 			}
 		});
 		eventBus.addHandler(OpenIspEvent.TYPE, new OpenIspEventHandler() {
 			@Override
 			public void onOpenIsp(OpenIspEvent event) {
 				doOpenIsp(event.getCaseNumber());
 			}
 		});
 		eventBus.addHandler(ViewTreatmentPlanEvent.TYPE, new ViewTreatmentPlanEventHandler() {
 			@Override
 			public void onViewTreatmentPlan(ViewTreatmentPlanEvent event) {
 				doViewTreatmentPlan(event.getCaseNumber(), event.getView());
 			}
 		});
 		eventBus.addHandler(ViewServiceHistoryEvent.TYPE, new ViewServiceHistoryEventHandler() {
 			@Override
 			public void onViewServiceHistory(ViewServiceHistoryEvent event) {
 				doViewServiceHistory(event.getCaseNumber(), event.getView());
 			}
 		});
 		eventBus.addHandler(LogoutEvent.TYPE, new LogoutEventHandler() {
 			@Override
 			public void onLogout(LogoutEvent event) {
 				doLogout();
 			}
 		});
 		eventBus.addHandler(OpenCpstNoteEvent.TYPE, new OpenCpstNoteEventHandler() {
 			@Override
 			public void onOpenCpstNote(OpenCpstNoteEvent event) {
 				doOpenCpstNote(event.getCaseNumber());
 			}
 		});
 		eventBus.addHandler(OpenIndividualProgressNoteEvent.TYPE, new OpenIndividualProgressNoteEventHandler() {
 			@Override
 			public void onOpenIndividualProgressNote(
 					OpenIndividualProgressNoteEvent event) {
 				doOpenIndividualProgressNote(event.getCaseNumber());
 			}
 		});
 		eventBus.addHandler(OpenDoctorProgressNoteEvent.TYPE, new OpenDoctorProgressNoteEventHandler() {
 			@Override
 			public void onOpenDoctorProgressNote(
 					OpenDoctorProgressNoteEvent event) {
 				doOpenDoctorProgressNote(event.getCaseNumber());
 			}
 		});
 		eventBus.addHandler(OpenNurseProgressNoteEvent.TYPE, new OpenNurseProgressNoteEventHandler() {
 			@Override
 			public void onOpenNurseProgressNote(OpenNurseProgressNoteEvent event) {
 				doOpenNurseProgressNote(event.getCaseNumber());
 			}
 		});
 		eventBus.addHandler(ViewSignatureEvent.TYPE, new ViewSignatureEventHandler() {
 			@Override
 			public void onViewSignature(ViewSignatureEvent event) {
 				doViewSignature();
 			}
 		});
 	}
 
 	private void doViewSignature() {
		Window.open("http://ewsql.eastway.local/echarts-asp/signatures/sign.asp", "_blank", "");
 	}
 
 	private void doOpenNurseProgressNote(String caseNumber) {
 		Window.open("http://ewsql.eastway.local/echarts-asp/Forms/103PM-NPNEdit.asp?staffid=" + EchartsUser.staffId + "&PATID=" + caseNumber, "_blank", "");
 	}
 
 	private void doOpenDoctorProgressNote(String caseNumber) {
 		Window.open("http://ewsql.eastway.local/echarts-asp/Forms/104PharmEdit.asp?staffid=" + EchartsUser.staffId + "&PATID=" + caseNumber, "_blank", "");
 	}
 
 	private void doOpenIndividualProgressNote(String caseNumber) {
 		Window.open("http://ewsql.eastway.local/echarts-asp/Forms/102IPNEdit.asp?staffid=" + EchartsUser.staffId + "&PATID=" + caseNumber, "_blank", "");
 	}
 
 	private void doOpenCpstNote(String caseNumber) {
 		Window.open("http://ewsql.eastway.local/echarts-asp/Forms/101CPSTEdit.asp?staffid=" + EchartsUser.staffId + "&PATID=" + caseNumber, "_blank", "");
 	}
 
 	private void doLogout() {
 		Window.Location.assign("http://ewsql.eastway.local/echarts/logout.aspx?continue=" + Window.Location.getHref());
 	}
 
 	private void doViewServiceHistory(String caseNumber, EHRView<EHR> view) {
 		Frame frame = new Frame("http://ewsql.eastway.local/echarts-asp/client/clienthistory.asp?staffid=" + EchartsUser.staffId + "&PATID=" + caseNumber);
 		view.getDisplayArea().clear();
 		view.getDisplayArea().add(frame);
 		frame.setSize("100%", "100%");
 	}
 
 	private void doViewTreatmentPlan(String caseNumber, EHRView<EHR> view) {
 		Frame frame = new Frame("http://ewsql.eastway.local/echarts-asp/client/treatmentplan.asp?staffid=" + EchartsUser.staffId + "&PATID=" + caseNumber);
 		view.getDisplayArea().clear();
 		view.getDisplayArea().add(frame);
 		frame.setSize("100%", "100%");
 	}
 
 	private void doOpenIsp(String caseNumber) {
 		Window.open("http://ewsql.eastway.local/echarts-asp/Forms/GandO.asp?staffid=" + EchartsUser.staffId + "&PATID=" + caseNumber, "_blank", "");
 	}
 
 	private void doViewTickler() {
 		dashboardPresenter.getDisplay().addTab(
 				ticklerPresenter
 					.getDisplay()
 					.asWidget(), "Tickler");
 		ticklerPresenter.go(null);
 	}
 
 	private void doViewProfile() {
 		dashboardPresenter.getDisplay().addTab(profilePresenter.getDisplay().asWidget(), "Profile");
 		profilePresenter.go(null);
 	}
 
 	private void doViewMedications(EHRView<EHR> view, GetMedications action) {
 		Presenter presenter = new MedicationPresenter(new MedicationView(), eventBus, dispatch, action);
 		presenter.go(view.getDisplayArea());
 	}
 
 	private void doViewContacts(EHRView<EHR> view, GetContacts action) {
 		Presenter presenter = new ContactPresenter(new ContactView(), eventBus, dispatch, action);
 		presenter.go(view.getDisplayArea());
 	}
 
 	private void doViewAddresses(EHRView<EHR> view, GetAddresses action) {
 		Presenter presenter = new AddressPresenter(new AddressViewImpl<Address>(), addressColumnDefinitions, eventBus, dispatch, action);
 		presenter.go(view.getDisplayArea());
 	}
 
 	private void doViewLinksEvent(EHRView<EHR> view, GetLinks action) {
 		Presenter presenter = new LinkPresenter(new LinkView(), eventBus, dispatch, action);
 		presenter.go(view.getDisplayArea());
 	}
 
 	private void doViewDiagnoses(EHRView<EHR> view, String caseNumber, GetDiagnoses action) {
 		DiagnosisPresenter diagnosisPresenter = new DiagnosisPresenter(new DiagnosisViewImpl<Diagnosis>(), diagnosisColumnDefinitions, eventBus, dispatch, action);
 		diagnosisPresenter.go(view.getDisplayArea());
 	}
 
 	private void doViewAppointments(EHRView<EHR> view, GetAppointments action) {
 		Presenter presenter = new AppointmentPresenter(new AppointmentView(), eventBus, dispatch, action);
 		presenter.go(view.getDisplayArea());
 	}
 
 	private void doViewReferral(EHRView<EHR> view, GetReferral action) {
 		Presenter presenter = new ReferralPresenter(new ReferralView(), eventBus, dispatch, action);
 		presenter.go(view.getDisplayArea());
 	}
 
 	private void doViewMessages(EHRView<EHR> view, GetMessages action) {
 		Presenter presenter = new MessagesPresenter(new MessagesView(), eventBus, dispatch, action);
 		presenter.go(view.getDisplayArea());
 	}
 
 	private void doViewEhr(String caseNumber) {
 		ehrView = new EHRViewImpl<EHR>();
 		Presenter presenter = new EHRPresenter(ehrView, eventBus, dispatch, caseNumber);
 		presenter.go(null);
 		dashboardPresenter.getDisplay().addTab(ehrView.asWidget(), caseNumber);
 	}
 
 	private void doViewPatientSummary(EHRView<EHR> view, GetPatientSummary action) {
 		Presenter presenter = new PatientSummaryPresenter(new PatientSummaryView(), eventBus, dispatch, action);
 		presenter.go(view.getDisplayArea());
 	}
 
 	private void doViewDemographics(final EHRView<EHR> view, GetDemographics action) {
 		Presenter presenter = new DemographicsPresenter(new DemographicsViewImpl<Demographics>(), demographicsColumnDefinitions, eventBus, dispatch, action);
 		presenter.go(view.getDisplayArea());
 	}
 
 	@Override
 	public void go(final HasWidgets container) {
 		container.clear();
 		dashboardPresenter.go(container);
 	}
 }
