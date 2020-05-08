 /* * Copyright 2012 Oregon State University.
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
  * 
  */
 package org.iplant.pipeline.client.builder;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.iplant.pipeline.client.Resources;
 import org.iplant.pipeline.client.dnd.DragCreator;
 import org.iplant.pipeline.client.json.App;
 import org.iplant.pipeline.client.json.Input;
 import org.iplant.pipeline.client.json.Output;
 import org.iplant.pipeline.client.json.PipeApp;
 import org.iplant.pipeline.client.json.PipeComponent;
 import org.iplant.pipeline.client.json.Pipeline;
 import org.iplant.pipeline.client.json.autobeans.PipelineApp;
 import org.iplant.pipeline.client.json.autobeans.PipelineAppMapping;
 import org.iplant.pipeline.client.json.autobeans.PipelineAutoBeanFactory;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONNumber;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 
 public class PipelineCreator extends Composite {
 
 	PipelineWorkspace workspace;
 	HorizontalPanel main = new HorizontalPanel();
     private final PipelineAutoBeanFactory factory = GWT.create(PipelineAutoBeanFactory.class);;
 
 	public PipelineCreator() {
 	    Resources.INSTANCE.css().ensureInjected();
 
 	    workspace = new PipelineWorkspace(new Pipeline("", "", false, 0));
 		workspace.setHeight("100%");
 		workspace.setWidth("100%");
 		main.setStyleName("pipe-table");
 		main.add(workspace);
 		main.setCellHeight(workspace, "100%");
 		main.setHeight("100%");
 		initWidget(main);
 	}
 
 	/**
 	 * This will load an existing pipeline into the creator.
 	 * @param json The json representation of a Pipeline
 	 */
     @Deprecated
 	public void loadPipeline(JSONObject json) {
 		main.remove(workspace);
 		workspace = new PipelineWorkspace(getPipelineFromJson(json));
 		workspace.setHeight("100%");
 		workspace.setWidth("100%");
 		main.insert(workspace, 0);
 	}
 	
     @Deprecated
 	public void appendApp(JSONObject app){
 		workspace.appendApp(DragCreator.createApp(app));
 	}
 	
     @Deprecated
 	private Pipeline getPipelineFromJson(JSONObject json){
 		Pipeline ret = new Pipeline();
 		JSONArray apps = (JSONArray) json.get("apps");
 		ret.setDescription(json.get("description").isString().stringValue());
 		ret.setName(json.get("name").isString().stringValue());
 		for(int i=0;i<apps.size();i++){
 			JSONObject appObj = (JSONObject) apps.get(i);
 			JSONArray mappingsA = (JSONArray) appObj.get("mappings");
 			App app = DragCreator.createApp(appObj);
 			for(int m=0;m<mappingsA.size();m++){
 				JSONObject map = (JSONObject) mappingsA.get(m);
 				int step = (int) ((JSONNumber)map.get("step")).doubleValue();
 				PipeComponent stepC = ret.getSteps().get(step);
 				App appM = ((PipeApp)stepC).getApp();
 				JSONObject maps = (JSONObject) map.get("map");
 				Iterator<String> it =maps.keySet().iterator();
 				while(it.hasNext()){
 					String inputId =it.next();
 					String outputId = ((JSONString)maps.get(inputId)).stringValue();
 					for(Output output : appM.getOutputs()){
 						if(output.getID().equals(outputId)){
 							output.setParent(stepC);
 							for(Input input:app.getInputs()){
 								if(input.getID().equals(inputId)){
 									input.setMapped(output);
 									break;
 								}
 							}
 							break;
 						}
 					}
 				}
 				
 			}
 			PipeApp pipeApp = new PipeApp(1, 1, i);
 			pipeApp.setPosition(i);
 			pipeApp.setApp(app);
 			ret.addStep(pipeApp);
 		}
 		return ret;
 	}
 
 	/**
 	 * This will return the json that represents the pipeline that is being
 	 * built.
 	 * 
 	 * @return the json in sting format of the new pipeline
 	 */
     @Deprecated
 	public JSONObject getPipelineJson() {
 		JSONObject ret = new JSONObject();
 		ret.put("name", new JSONString(workspace.getPipeline().getName()));
 		ret.put("description",new JSONString( workspace.getPipeline().getDescription()));
 		JSONArray appsArray = new JSONArray();
 		Vector<PipeComponent> steps = workspace.getPipeline().getSteps();
 		int i=0;
 		for (PipeComponent step : steps) {
 			App app = ((PipeApp) step).getApp();
 			JSONObject jsonApp = new JSONObject();
 			jsonApp.put("id", new JSONString(app.getID()));
 			jsonApp.put("name", new JSONString(app.getName()));
 			jsonApp.put("description", new JSONString(app.getDescription()));
 			jsonApp.put("step", new JSONNumber(step.getPosition()));
 			HashMap<PipeComponent, ArrayList<Input>> mappings = new HashMap<PipeComponent, ArrayList<Input>>();
 			for (Input input : step.getInputs()) {
 				if (input.getMapped() != null) {
 					PipeComponent parent = input.getMapped().getParent();
 					ArrayList<Input> maps = mappings.get(parent);
 					if (maps == null)
 						maps = new ArrayList<Input>();
 					maps.add(input);
 					mappings.put(parent, maps);
 				}
 			}
 			JSONArray jsonMappings = new JSONArray();
 			jsonApp.put("mappings", jsonMappings);
 			jsonApp.put("inputs", app.getInputJson());
 			jsonApp.put("outputs", app.getOutputJson());
 			Iterator<PipeComponent> it = mappings.keySet().iterator();
 			int mappingsI =0;
 			while (it.hasNext()) {
 				PipeComponent mappedTo = it.next();
 				App mappedApp = ((PipeApp) mappedTo).getApp();
 				JSONObject jsonMap = new JSONObject();
 				jsonMap.put("step", new JSONNumber(mappedTo.getPosition()));
 				jsonMap.put("id", new JSONString(mappedApp.getID()));
 				ArrayList<Input> inputs = mappings.get(mappedTo);
 				JSONObject mapO = new JSONObject();
 				for (Input input : inputs) {
 					mapO.put(input.getID(), new JSONString(input.getMapped().getID() ));
 				}
 				jsonMap.put("map", mapO);
 				jsonMappings.set(mappingsI++, jsonMap);
 			}
 			appsArray.set(i++,jsonApp);
 		}
 		ret.put("apps", appsArray);
 		return ret;
 	}
 
     /**
      * This will load an existing pipeline into the creator.
      * 
      * @param json The json representation of a Pipeline
      */
     public void loadPipeline(org.iplant.pipeline.client.json.autobeans.Pipeline json) {
         main.remove(workspace);
         workspace = new PipelineWorkspace(buildPipeline(json));
         workspace.setHeight("100%");
         workspace.setWidth("100%");
         main.insert(workspace, 0);
     }
 
     private Pipeline buildPipeline(org.iplant.pipeline.client.json.autobeans.Pipeline json) {
         Pipeline ret = new Pipeline();
         List<PipelineApp> apps = json.getApps();
         ret.setDescription(json.getDescription());
         ret.setName(json.getName());
 
         if (apps != null) {
             int i = 0;
             for (PipelineApp appObj : apps) {
                 App app = DragCreator.createApp(appObj);
 
                 List<PipelineAppMapping> mappingsA = appObj.getMappings();
                 if (mappingsA != null) {
                     for (PipelineAppMapping map : mappingsA) {
                         int step = map.getStep();
                        PipeComponent stepC = ret.getSteps().get(step);
                         App appM = ((PipeApp)stepC).getApp();
                         Map<String, String> maps = map.getMap();
                         for (String inputId : maps.keySet()) {
                             String outputId = maps.get(inputId);
                             for (Output output : appM.getOutputs()) {
                                 if (output.getID().equals(outputId)) {
                                     output.setParent(stepC);
                                     for (Input input : app.getInputs()) {
                                         if (input.getID().equals(inputId)) {
                                             input.setMapped(output);
                                             break;
                                         }
                                     }
                                     break;
                                 }
                             }
                         }
                     }
                 }
                 PipeApp pipeApp = new PipeApp(1, 1, i++);
                 pipeApp.setPosition(i);
                 pipeApp.setApp(app);
                 ret.addStep(pipeApp);
             }
         }
 
         return ret;
     }
 
     public void appendApp(PipelineApp app) {
         workspace.appendApp(DragCreator.createApp(app));
     }
 
     /**
      * This will return the Pipeline AutoBean that represents the pipeline that is being built.
      * 
      * @return the AutoBean of the new pipeline
      */
     public org.iplant.pipeline.client.json.autobeans.Pipeline getPipeline() {
         org.iplant.pipeline.client.json.autobeans.Pipeline ret = factory.pipeline().as();
         ret.setName(workspace.getPipeline().getName());
         ret.setDescription(workspace.getPipeline().getDescription());
 
         List<PipelineApp> apps = new ArrayList<PipelineApp>();
         Vector<PipeComponent> steps = workspace.getPipeline().getSteps();
         for (PipeComponent step : steps) {
             App app = ((PipeApp)step).getApp();
 
             PipelineApp jsonApp = factory.app().as();
             jsonApp.setId(app.getID());
             jsonApp.setName(app.getName());
             jsonApp.setDescription(app.getDescription());
             jsonApp.setStep(step.getPosition());
 
             HashMap<PipeComponent, ArrayList<Input>> mappings = new HashMap<PipeComponent, ArrayList<Input>>();
             for (Input input : step.getInputs()) {
                 if (input.getMapped() != null) {
                     PipeComponent parent = input.getMapped().getParent();
                     ArrayList<Input> maps = mappings.get(parent);
                     if (maps == null)
                         maps = new ArrayList<Input>();
                     maps.add(input);
                     mappings.put(parent, maps);
                 }
             }
 
             List<PipelineAppMapping> jsonMappings = new ArrayList<PipelineAppMapping>();
             jsonApp.setMappings(jsonMappings);
 
             for (PipeComponent mappedTo : mappings.keySet()) {
                 App mappedApp = ((PipeApp)mappedTo).getApp();
                 PipelineAppMapping jsonMap = factory.appMapping().as();
                 jsonMap.setStep(mappedTo.getPosition());
                 jsonMap.setId(mappedApp.getID());
                 ArrayList<Input> inputs = mappings.get(mappedTo);
                 Map<String, String> map = new HashMap<String, String>();
                 for (Input input : inputs) {
                     map.put(input.getID(), input.getMapped().getID());
                 }
                 jsonMap.setMap(map);
                 jsonMappings.add(jsonMap);
             }
 
             jsonApp.setInputs(app.getAppDataInputs());
             jsonApp.setOutputs(app.getAppDataOutputs());
 
             apps.add(jsonApp);
         }
 
         ret.setApps(apps);
 
         return ret;
     }
 
 }
