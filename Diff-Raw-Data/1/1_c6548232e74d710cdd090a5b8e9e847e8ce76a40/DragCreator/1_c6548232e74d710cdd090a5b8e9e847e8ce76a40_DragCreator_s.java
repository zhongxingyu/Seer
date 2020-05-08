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
 package org.iplant.pipeline.client.dnd;
 
 import org.iplant.pipeline.client.json.App;
 import org.iplant.pipeline.client.json.IPCType;
 import org.iplant.pipeline.client.json.Input;
 import org.iplant.pipeline.client.json.Output;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONBoolean;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.ui.Image;
 
 public class DragCreator {
 
 	private static IPCType draggedRecord;
 	public static final int MOVE = 1;
 	public static final int COPY = 2;
 	public static final int DELETE = 3;
 	public static Element dragImageElement = getNOImageElement();
 	public static JavaScriptObject dragEvent;
 
 	public static native void addDrag(Element element, IPCType rec, DragListener listener) /*-{
 		function handleDragStart(e) {
 
 			var dragIcon = listener.@org.iplant.pipeline.client.dnd.DragListener::getDragImage(Lorg/iplant/pipeline/client/json/IPCType;)(rec);
 			e.dataTransfer.setDragImage(dragIcon, -10, -10);
 			//e.dataTransfer.effectAllowed = 'copy';
 			@org.iplant.pipeline.client.dnd.DragCreator::draggedRecord = rec;
 			listener.@org.iplant.pipeline.client.dnd.DragListener::dragStart(Lorg/iplant/pipeline/client/json/IPCType;)(rec);
 			e.dataTransfer.effectAllowed = 'all';
 
 			if (element.getAttribute("data-downloadurl") != null) {
 				e.dataTransfer.setData("DownloadURL", element.getAttribute("data-downloadurl"));
 			} else {
 				e.dataTransfer.setData('Text',rec.@org.iplant.pipeline.client.json.IPCType::getId()()); // required otherwise doesn't work
 				@org.iplant.pipeline.client.dnd.DragCreator::dragEvent = dragIcon;
 			}
 		}
 
 		function handleDragOver(e) {
 			if (e.stopPropagation) {
 				e.stopPropagation(); // stops the browser from redirecting.
 			}
 			if(e.preventDefault)
 			e.preventDefault();
 			var canDrop = listener.@org.iplant.pipeline.client.dnd.DragListener::dragOver(Lorg/iplant/pipeline/client/json/IPCType;)(rec);
 			if (canDrop)
 				e.dataTransfer.dropEffect = 'copy';
 			else {
 				e.dataTransfer.dropEffect = 'move';
 			}
 			return false;
 		}
 
 		function handleDragEnter(e) {
 			event.preventDefault();
 			// this / e.target is the current hover target.
 			listener.@org.iplant.pipeline.client.dnd.DragListener::dragEnter(Lorg/iplant/pipeline/client/json/IPCType;)(rec);
 			return true;
 		}
 
 		function handleDragLeave(e) {
 			listener.@org.iplant.pipeline.client.dnd.DragListener::dragLeave(Lorg/iplant/pipeline/client/json/IPCType;)(rec);
 		}
 		function handleDrop(e) {
 			// this / e.target is current target element.
 			if (e.stopPropagation) {
 				e.stopPropagation(); // stops the browser from redirecting.
 			}
 			if (e.preventDefault)
 				e.preventDefault();
 			var data = e.dataTransfer.getData('Text');
 			if (data && isNaN(data)) {
 				//				//item is an json app from iplant
 				var obj = eval("(" + data + ")");
 				var app = @org.iplant.pipeline.client.dnd.DragCreator::createApp(Lcom/google/gwt/core/client/JavaScriptObject;)(obj);
 				@org.iplant.pipeline.client.dnd.DragCreator::draggedRecord = app;
 			}
 			listener.@org.iplant.pipeline.client.dnd.DragListener::drop(Lorg/iplant/pipeline/client/json/IPCType;)(rec);
 		}
 
 		function handleDragEnd(e) {
 			listener.@org.iplant.pipeline.client.dnd.DragListener::dragEnd(Lorg/iplant/pipeline/client/json/IPCType;)(rec);
 		}
 
 		element.addEventListener('dragstart', handleDragStart, false);
 		element.addEventListener('dragenter', handleDragEnter, false);
 		element.addEventListener('dragover', handleDragOver, false);
 		element.addEventListener('dragleave', handleDragLeave, false);
 		element.addEventListener('drop', handleDrop, false);
 		element.addEventListener('dragend', handleDragEnd, false);
 	}-*/;
 
 	public static native void addDrop(Element element, IPCType rec, DropListener listener) /*-{
 		function handleDragOver(e) {
 			if (e.stopPropagation) {
 				e.stopPropagation(); // stops the browser from redirecting.
 			}
 			if(e.preventDefault)
 			e.preventDefault();
 			var canDrop = listener.@org.iplant.pipeline.client.dnd.DropListener::dragOver(Lorg/iplant/pipeline/client/json/IPCType;)(rec);
 			if (canDrop)
 				e.dataTransfer.dropEffect = 'copy';
 			else {
 				e.dataTransfer.dropEffect = 'move';
 			}
 			return false;
 		}
 
 		function handleDragEnter(e) {
 			if (e.stopPropagation) {
 				e.stopPropagation(); // stops the browser from redirecting.
 			}
 			if(e.preventDefault)
 			e.preventDefault();
 			var canDrop = listener.@org.iplant.pipeline.client.dnd.DropListener::dragEnter(Lorg/iplant/pipeline/client/json/IPCType;)(rec);
 			if (canDrop)
 				e.dataTransfer.dropEffect = 'copy';
 			else {
 				e.dataTransfer.dropEffect = 'move';
 			}
 			return false;
 		}
 
 		function handleDragLeave(e) {
 			listener.@org.iplant.pipeline.client.dnd.DropListener::dragLeave(Lorg/iplant/pipeline/client/json/IPCType;)(rec);
 		}
 		function handleDrop(e) {
 			// this / e.target is current target element.
 			if (e.stopPropagation) {
 				e.stopPropagation(); // stops the browser from redirecting.
 			}
 			if (e.preventDefault)
 				e.preventDefault();
 			var data = e.dataTransfer.getData('Text');
 			if (isNaN(data)) {
 				//				//item is an json app from iplant
 				var obj = eval("(" + data + ")");
 				var app = @org.iplant.pipeline.client.dnd.DragCreator::createApp(Lcom/google/gwt/core/client/JavaScriptObject;)(obj);
 				@org.iplant.pipeline.client.dnd.DragCreator::draggedRecord = app;
 			}
 			listener.@org.iplant.pipeline.client.dnd.DropListener::drop(Lorg/iplant/pipeline/client/json/IPCType;)(rec);
 		}
 
 		function addEvent(el, type, fn) {
 			if (el && el.nodeName || el === window) {
 				el.addEventListener(type, fn, false);
 			} else if (el && el.length) {
 				for ( var i = 0; i < el.length; i++) {
 					addEvent(el[i], type, fn);
 				}
 			}
 		}
 		//		addEvent(element, 'dragenter', handleDragEnter);
 		addEvent(element, 'dragover', handleDragOver);
 		addEvent(element, 'dragleave', handleDragLeave);
 		addEvent(element, 'drop', handleDrop);
 	}-*/;
 
 	public static IPCType getDragSource() {
 		return draggedRecord;
 	}
 
 	private static App createApp(String name, String description, String id) {
 		App app = new App();
 		app.setName(name);
 		app.setDescription(description);
 		app.setId(1);
 		app.setID(id);
 		return app;
 	}
 
 	private static App createApp(com.google.gwt.core.client.JavaScriptObject json) {
 		return createApp(new JSONObject(json));
 	}
 
 	public static App createApp(JSONObject json) {
 		App app = new App();
 		app.setName(((JSONString) json.get("name")).stringValue());
 		app.setDescription(((JSONString) json.get("description")).stringValue());
 		app.setId(1);
 		app.setID(((JSONString) json.get("id")).stringValue());
 		JSONArray inputs = (JSONArray) json.get("inputs");
 		app.setInputJson(inputs);
 		for (int i = 0; i < inputs.size(); i++) {
 			Input input = new Input();
 			JSONObject obj = (JSONObject) inputs.get(i);
 			JSONObject dataObj = (JSONObject) obj.get("data_object");
 			if (dataObj != null) {
 				input.setName(((JSONString) dataObj.get("name")).stringValue());
 				input.setDescription(((JSONString) dataObj.get("description")).stringValue());
 				input.setId(1);
 				input.setRequired(((JSONBoolean) dataObj.get("required")).booleanValue());
 				input.setType("File:" + ((JSONString) dataObj.get("format")).stringValue());
 				input.setID(((JSONString) dataObj.get("id")).stringValue());
 				app.addInput(input);
 			}
 		}
 		JSONArray outputs = (JSONArray) json.get("outputs");
 		app.setOutputJson(outputs);
 		for (int i = 0; i < outputs.size(); i++) {
 			Output output = new Output();
 			JSONObject obj = (JSONObject) outputs.get(i);
 			JSONObject dataObj = (JSONObject) obj.get("data_object");
 			if (dataObj != null) {
 				output.setName(((JSONString) dataObj.get("name")).stringValue());
 				output.setDescription(((JSONString) dataObj.get("description")).stringValue());
 				output.setId(1);
 				output.setType(((JSONString) dataObj.get("format")).stringValue());
 				output.setID(((JSONString) dataObj.get("id")).stringValue());
 				app.addOutput(output);
 			}
 		}
 
 		return app;
 	}
 
 	public static Element getImageElement(String src) {
 		Image img = new Image(src);
 		img.setWidth("20px");
 		img.setHeight("20px");
 		return img.getElement();
 	}
 
 	public static Element getOKImageElement() {
 		Image img = new Image("/images/add.png");
 		img.setWidth("20px");
 		img.setHeight("20px");
 		return img.getElement();
 	}
 
 	public static Element getNOImageElement() {
 		Image img = new Image("/images/down.png");
 		img.setWidth("20px");
 		img.setHeight("20px");
 		return img.getElement();
 	}
 }
