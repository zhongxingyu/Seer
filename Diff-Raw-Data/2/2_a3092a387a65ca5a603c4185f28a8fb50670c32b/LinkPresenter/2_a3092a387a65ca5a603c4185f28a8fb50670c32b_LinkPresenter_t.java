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
 
 import java.util.LinkedHashSet;
 
 import net.customware.gwt.presenter.client.EventBus;
 
 import org.eastway.echarts.client.EchartsUser;
 import org.eastway.echarts.client.rpc.CachingDispatchAsync;
 import org.eastway.echarts.client.rpc.EchartsCallback;
 import org.eastway.echarts.shared.GetLinks;
 import org.eastway.echarts.shared.GetLinksResult;
 
 import com.google.gwt.user.client.ui.HasWidgets;
 
 public class LinkPresenter implements Presenter {
 
 	public interface Display extends EchartsDisplay {
 		void setData(LinkedHashSet<String[]> data);
 	}
 
 	private LinkedHashSet<String[]> data;
 	private Display display;
 	private GetLinks action;
 	private CachingDispatchAsync dispatch;
 	private EventBus eventBus;
 
 	public LinkPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatch, GetLinks action) {
 		this.action = action;
 		this.eventBus = eventBus;
 		this.dispatch = dispatch;
 		this.display = display;
 	}
 
 	public void fetchData() {
		dispatch.execute(action, new EchartsCallback<GetLinksResult>(eventBus) {
 			@Override
 			protected void handleFailure(Throwable caught) {
 			}
 
 			@Override
 			protected void handleSuccess(GetLinksResult result) {
 				setData(result.getLinks());
 				display.setData(result.getLinks());
 			}
 		});
 	}
 
 	public LinkedHashSet<String[]> getData() {
 		return data;
 	}
 
 	public void setData(LinkedHashSet<String[]> d) {
 		this.data = d;
 		for (String[] s : data) {
 			s[1] += "?staffid=" + EchartsUser.staffId + "&PATID=" + action.getCaseNumber();
 		}
 	}
 
 	@Override
 	public void go(HasWidgets container) {
 		container.clear();
 		container.add(display.asWidget());
 		fetchData();
 	}
 }
