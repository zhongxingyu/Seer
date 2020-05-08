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
 package org.eastway.echarts.client.presenter;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import com.google.gwt.event.shared.EventBus;
 
 import org.eastway.echarts.client.EchartsUser;
 import org.eastway.echarts.client.events.ChangeCurrentEhrEvent;
 import org.eastway.echarts.client.events.ChangeCurrentEhrEventHandler;
 import org.eastway.echarts.client.events.LogoutEvent;
 import org.eastway.echarts.client.events.OpenEhrEvent;
 import org.eastway.echarts.client.events.ViewGroupProgressNoteEvent;
 import org.eastway.echarts.client.events.ViewLastSeenReportEvent;
 import org.eastway.echarts.client.events.ViewMedsomSignaturesEvent;
 import org.eastway.echarts.client.events.ViewOverlapsReportEvent;
 import org.eastway.echarts.client.events.ViewProfileEvent;
 import org.eastway.echarts.client.events.ViewProviderSignaturesEvent;
 import org.eastway.echarts.client.events.ViewStaffHistoryEvent;
 import org.eastway.echarts.client.events.ViewSupervisorSignaturesEvent;
 import org.eastway.echarts.client.events.ViewTicklerEvent;
 import org.eastway.echarts.client.rpc.AssignmentProxy;
 import org.eastway.echarts.client.rpc.AssignmentRequest;
 import org.eastway.echarts.client.rpc.DemographicsProxy;
 import org.eastway.echarts.client.rpc.EHRProxy;
 import org.eastway.echarts.client.rpc.EchartsRequestFactory;
 import org.eastway.echarts.client.rpc.EhrRequest;
 import org.eastway.echarts.client.rpc.PatientProxy;
 import org.eastway.echarts.client.rpc.ProductivityProxy;
 import org.eastway.echarts.client.view.DashboardView;
 
 import com.google.gwt.requestfactory.shared.Receiver;
import com.google.gwt.requestfactory.shared.ServerFailure;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.inject.Inject;
 
 public class DashboardPresenter implements Presenter, DashboardView.Presenter<LinkedHashMap<String, Long>> {
 
 	class EHRFetcher {
 		EHRProxy fetchedEHR;
 		List<AssignmentProxy> fetchedAssignments;
 
 		void Run(final EhrRequest ehrRequest, final AssignmentRequest assignmentRequest, final String caseNumber, final Receiver<EHRFetcher> callback) {
 			ehrRequest.findEHRByCaseNumber(caseNumber)
 				.with("patient")
 				.with("patient.caseStatus")
 				.with("demographics")
 				.fire(
 					new Receiver<EHRProxy>() {
 						@Override
 						public void onSuccess(EHRProxy response) {
 							if (response != null)
 								fetchedEHR = response;
 								assignmentRequest.findAssignmentsByCaseNumber(caseNumber).fire(
 										new Receiver<List<AssignmentProxy>>() {
 											@Override
 											public void onSuccess(List<AssignmentProxy> response) {
 												fetchedAssignments = response;
 												if (fetchedAssignments != null)
 													callback.onSuccess(EHRFetcher.this);
 											}
 										});
 						}

						@Override
						public void onFailure(ServerFailure error) {
							com.google.gwt.user.client.Window.alert(error.getMessage());
						}
 					});
			
 		}
 	}
 
 	private DashboardView<LinkedHashMap<String, Long>> view;
 	private EventBus eventBus;
 	private EchartsRequestFactory requestFactory;
 
 	@Inject
 	public DashboardPresenter(DashboardView<LinkedHashMap<String, Long>> view, EventBus eventBus, final EchartsRequestFactory requestFactory) {
 		this.view = view;
 		this.eventBus = eventBus;
 		this.view.setPresenter(this);
 		this.requestFactory = requestFactory;
 	}
 
 	private void bind() {
 		eventBus.addHandler(ChangeCurrentEhrEvent.TYPE, new ChangeCurrentEhrEventHandler() {
 			@Override
 			public <T> void onChangeCurrentEhr(ChangeCurrentEhrEvent<T> event) {
 				setCurrentEhrData((EHRProxy)event.getEhr());
 			}
 		});
 	}
 
 	public DashboardView<LinkedHashMap<String, Long>> getDisplay() {
 		return view;
 	}
 
 	@Override
 	public void go(HasWidgets container) {
 		container.clear();
 		container.add(view.asWidget());
 		bind();
 		fetchData();
 		if (Cookies.getCookie("first_login") != null)
 			view.isFirstLogin();
 	}
 
 	private void setCurrentEhrData(EHRProxy ehr) {
 		if (ehr == null) {
 			view.showEhrStub(false);
 			return;
 		}
 		PatientProxy patient = ehr.getPatient();
 		DemographicsProxy demographics = ehr.getDemographics();
 		view.setName(patient.getName());
		if (patient.getCaseStatus() != null)
			view.setCaseStatus(patient.getCaseStatus().getCodeDescriptor() == null ? "NO DATA" : patient.getCaseStatus().getCodeDescriptor());
		else
			view.setCaseStatus("NO DATA");
 		view.setDob(demographics.getDob());
 		view.setProvider(getProvider(ehr.getAssignments()));
 		view.setSsn(patient.getSsn());
 		view.showEhrStub(true);
 	}
 
 	private String getProvider(List<AssignmentProxy> list) {
 		if (list == null) {
 			return "";
 		} else {
 			List<String> providers = new ArrayList<String>();
 			String provider = null;
 			for (AssignmentProxy a : list) {
 				providers.add(a.getStaffName());
 				if (a.getStaff() != null && a.getStaff().matches(EchartsUser.staffId))
 					provider = a.getStaffName();
 			}
 			if (provider == null && list.size() > 0)
 				provider = list.get(0).getStaffName();
			if (provider == null)
				return "NO DATA";
 			return provider;
 		}
 	}
 
 	private void fetchData() {
 		getProductivityData();
 	}
 
 	@Override
 	public void changeCurrentEhr(Object ehr) {
 			eventBus.fireEvent(new ChangeCurrentEhrEvent<EHRProxy>((EHRProxy)ehr));
 	}
 
 	@Override
 	public void openEhr(String text) {
 		final EhrRequest ehrRequest = requestFactory.ehrRequest();
 		AssignmentRequest assignmentRequest = requestFactory.assignmentRequest();
 		new EHRFetcher().Run(ehrRequest, assignmentRequest, text.replaceAll("(.*) - .*", "$1"), new Receiver<DashboardPresenter.EHRFetcher>() {
 			@Override
 			public void onSuccess(EHRFetcher response) {
 				EHRProxy ehr = requestFactory.ehrRequest().edit(response.fetchedEHR);
 				ehr.setAssignments(response.fetchedAssignments);
 				eventBus.fireEvent(new OpenEhrEvent(ehr));
 			}
 		});
 	}
 
 	private void getProductivityData() {
 		requestFactory.productivityRequest().findProductivityByStaffId(EchartsUser.staffId).fire(new Receiver<ProductivityProxy>() {
 			@Override
 			public void onSuccess(ProductivityProxy response) {
				if (response == null)
					return;
 				String color = null;
 				if (response.getTotal() < response.getYellowNumber())
 					color = "red";
 				else if (response.getTotal() < response.getGreenNumber())
 					color = "yellow";
 				else
 					color = "green";
 				view.setProductivity(response.getTotal().toString(), color);
 				view.setBonusProjection(response.getGreenNumber().toString());
 			}
 		});
 	}
 
 	@Override
 	public void openTickler() {
 		eventBus.fireEvent(new ViewTicklerEvent());
 	}
 
 	@Override
 	public void logout() {
 		eventBus.fireEvent(new LogoutEvent());
 	}
 
 	@Override
 	public void openProfile() {
 		eventBus.fireEvent(new ViewProfileEvent());
 	}
 
 	@Override
 	public void openProviderSignatures() {
 		eventBus.fireEvent(new ViewProviderSignaturesEvent());
 	}
 
 	@Override
 	public void openSupervisorSignatures() {
 		eventBus.fireEvent(new ViewSupervisorSignaturesEvent());
 	}
 
 	@Override
 	public void openMedsomSignatures() {
 		eventBus.fireEvent(new ViewMedsomSignaturesEvent());
 	}
 
 	@Override
 	public void openStaffHistory() {
 		eventBus.fireEvent(new ViewStaffHistoryEvent());
 	}
 
 	@Override
 	public void openGroupProgressNote() {
 		eventBus.fireEvent(new ViewGroupProgressNoteEvent());
 	}
 
 	@Override
 	public void openLastSeenReport() {
 		eventBus.fireEvent(new ViewLastSeenReportEvent());
 	}
 
 	@Override
 	public void openOverlapsReport() {
 		eventBus.fireEvent(new ViewOverlapsReportEvent());
 	}
 }
