 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package web.shedule.action;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.interceptor.SessionAware;
 import org.hibernate.Criteria;
 import org.hibernate.criterion.DetachedCriteria;
 
 import web.shedule.dao.SlotDao;
 import web.shedule.model.Slot;
 import web.shedule.util.Debug;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 @Result(name = "success", type = "json")
 public class TestDataProvider extends ActionSupport implements SessionAware {
 	private List<CustomModel> results;
 	private static final long serialVersionUID = 5078264277068533593L;
 	private static final Log log = LogFactory.getLog(TestDataProvider.class);
 	private Map<String, Object> session;
 
 	public class CustomModel {
 
 		private String data1;
 		private List<String> dataArray;
 
 		public String getData1() {
 			return data1;
 		}
 
 		public void setData1(String data1) {
 			this.data1 = data1;
 		}
 
 		public List<String> getDataArray() {
 			return dataArray;
 		}
 
 		public void setDataArray(List<String> dataArray) {
 			this.dataArray = dataArray;
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public String execute() {
 		CustomModel cm1 = new CustomModel();
 		cm1.setData1("data1");
 		List<String> l1 = new ArrayList<String>();
 		l1.add("test1");
 		l1.add("test2");
 		cm1.setDataArray(l1);
 
 		CustomModel cm2 = new CustomModel();
 		cm2.setData1("data2");
 		List<String> l2 = new ArrayList<String>();
 		l2.add("test3");
 		l2.add("test4");
 		cm2.setDataArray(l2);
 
 		results = new ArrayList<CustomModel>();
 		results.add(cm1);
 		results.add(cm2);
 		return SUCCESS;
 	}
 
 	public String getJSON() {
 		return execute();
 	}
 
 	@Override
 	public void setSession(Map<String, Object> session) {
 		this.session = session;
 	}
 
 	public void setResults(List<CustomModel> results) {
 		this.results = results;
 	}
 
 	public List<CustomModel> getResults() {
 		return results;
 	}
 }
