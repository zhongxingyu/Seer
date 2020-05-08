 package uk.ac.kmi.microwsmo.client.controller;
 
 import java.util.List;
 
 import uk.ac.kmi.microwsmo.client.MicroWSMOeditor;
 import uk.ac.kmi.microwsmo.client.util.CSSIconImage;
 import uk.ac.kmi.microwsmo.client.util.Message;
 import uk.ac.kmi.microwsmo.client.view.AnnotationsTree;
 import uk.ac.kmi.microwsmo.client.view.BaseTreeItem;
 import uk.ac.kmi.microwsmo.client.view.DomainOntologiesTree;
 import uk.ac.kmi.microwsmo.client.view.ServicePropertiesTree;
 
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.google.gwt.user.client.Window;
 
 final class SemanticController {
 
 	/**
 	 * Query the semantic crawler which retrieve the semantic documents.
 	 */
 	public static void queryWatson() {
 		String keyword = ControllerToolkit.getTextSelected();
 		// if the keyword is valid and if is not present yet in the service properties
 		if( isValid(keyword) ) {
 			// if the model doesn't contain the keyword yet
 			ServicePropertiesTree spTree = MicroWSMOeditor.getServicePropertiesTree();
 			DomainOntologiesTree doTree = MicroWSMOeditor.getDomainOntologiesTree();
 			if( !spTree.contains(keyword) ) {
 				BaseTreeItem spLoadingItem = new BaseTreeItem("loading " + keyword, CSSIconImage.LOADING);
 				BaseTreeItem odLoadingItem = new BaseTreeItem("loading " + keyword, CSSIconImage.LOADING);
 				spTree.addRootItem(spLoadingItem);
 				doTree.addRootItem(odLoadingItem);
 				generateServiceProperties(keyword, false);
 				generateDomainOntologies(keyword, false);
 				
 				//Logger
 				logEvents("ItemSearchWatson", "queryWatson", keyword);
 				
 			} else {
 				Message.show(Message.TERM_ALREADY_RETRIEVED, keyword);
 			}
 		// otherwise warn the user
 		} else {
 			Message.show(Message.WATSON_SELECTION);
 		}
 	}
 	
 	public static native void logEvents(String item, String method, String element) /*-{
 	var httpRequest = @uk.ac.kmi.microwsmo.client.controller.ControllerToolkit::getXMLHttpRequest()();
 	if( httpRequest != null ) {
 		var parameters = "method=" +method + "&element=" + element + "&item=" + item;
 		httpRequest.open("POST", "../logger", true);
 		// set the header
 		httpRequest.setRequestHeader("content-type", "application/x-www-form-urlencoded");
 		httpRequest.setRequestHeader("content-length", parameters.length);
 		httpRequest.setRequestHeader("connection", "close");
 		httpRequest.send(parameters);
 	}
 	}-*/;
 	
 	public static void viewMore(String keyword) {
 		generateServiceProperties(keyword, true);
 		generateDomainOntologies(keyword, true);
 	}
 
 	/**
 	 * Returns {@code true} if the keyword is not empty, and is not a white space string.
 	 * 
 	 * @param keyword the string to check.
 	 * @return true if is valid.
 	 */
 	private static boolean isValid(String keyword) {
 		return keyword != null && !keyword.equals("") && !keyword.equals("\\s");
 	}
 	
 	private static native void generateServiceProperties(String keyword, boolean viewMore) /*-{
 		// handle the http comunication object
 		var httpRequest = @uk.ac.kmi.microwsmo.client.controller.ControllerToolkit::getXMLHttpRequest()();
 		if( httpRequest != null ) {
 			var parameters = "method=sp&keyword=" + keyword;
 			httpRequest.open("POST", "../watson", true);
 			// set the header
 			httpRequest.setRequestHeader("content-type", "application/x-www-form-urlencoded");
 			httpRequest.setRequestHeader("content-length", parameters.length);
 			httpRequest.setRequestHeader("connection", "close");
 			httpRequest.send(parameters);
 			// callback function
 			httpRequest.onreadystatechange = function() {
 				if( httpRequest.readyState == 4 && httpRequest.status == 200) {
 					var result = httpRequest.responseText;
 					if( viewMore ) {
 						@uk.ac.kmi.microwsmo.client.controller.SemanticController::nextServicePropertiesAdding(Ljava/lang/String;Ljava/lang/String;)(result,keyword);
 					} else {
 						@uk.ac.kmi.microwsmo.client.controller.SemanticController::firstServicePropertiesAdding(Ljava/lang/String;Ljava/lang/String;)(result,keyword);
 					}
 				}
 			}
 		} else {
 			@uk.ac.kmi.microwsmo.client.util.Message::show(Ljava/lang/String;)
 	    	(@uk.ac.kmi.microwsmo.client.util.Message::AJAX);
 		}
 	}-*/;
 	
 	private static native void generateDomainOntologies(String keyword, boolean viewMore) /*-{
 		// handle the http comunication object
 		var httpRequest = @uk.ac.kmi.microwsmo.client.controller.ControllerToolkit::getXMLHttpRequest()();
 		if( httpRequest != null ) {
 			var parameters = "method=do&keyword=" + keyword;
 			httpRequest.open("POST", "../watson", true);
 			// set the header
 			httpRequest.setRequestHeader("content-type", "application/x-www-form-urlencoded");
 			httpRequest.setRequestHeader("content-length", parameters.length);
 			httpRequest.setRequestHeader("connection", "close");
 			httpRequest.send(parameters);
 			// callback function
 			httpRequest.onreadystatechange = function() {
 				if( httpRequest.readyState == 4 && httpRequest.status == 200) {
 					var result = httpRequest.responseText;
 					if( viewMore ) {
 						@uk.ac.kmi.microwsmo.client.controller.SemanticController::nextOntologyDomainAdding(Ljava/lang/String;Ljava/lang/String;)(result,keyword);
 					} else {
 						@uk.ac.kmi.microwsmo.client.controller.SemanticController::firstOntologyDomainAdding(Ljava/lang/String;Ljava/lang/String;)(result,keyword);
 					}
 				}
 			}
 		} else {
 			@uk.ac.kmi.microwsmo.client.util.Message::show(Ljava/lang/String;)
 	    	(@uk.ac.kmi.microwsmo.client.util.Message::AJAX);
 		}
 	}-*/;
 	
 	private static void firstServicePropertiesAdding(String result, String keyword) {
 		// retrieve the service properties tree
 		ServicePropertiesTree tree = MicroWSMOeditor.getServicePropertiesTree();
 		// delete the loading icon
 		tree.removeItemById("loading " + keyword);
 		// if the result is empty
 		if( result.startsWith("0") ) {
 			Message.show(Message.NO_WATSON_RESULT);
 			return;
 		}
 		BaseTreeItem keywordItem = new BaseTreeItem(keyword, CSSIconImage.TERM);
 		tree.addRootItem(keywordItem);
 		String[] stringSplitted = result.split("<!>");
 		int nextResults = new Integer(stringSplitted[0]);
 		stringSplitted = stringSplitted[1].split("<&>");
 		for( int i = 0; i < stringSplitted.length; i++ ) {
 			String s = stringSplitted[i];
 			String[] chunk = s.split("<:>");
 			String name = chunk[1];
 			String icon = CSSIconImage.getEntityIcon(chunk[0]);
 			BaseTreeItem entityItem = new BaseTreeItem(name, icon);
 			tree.addItem(keywordItem, entityItem);
 			
 			// retrieve the ontology for this entity
 			for( int j = 2; j < chunk.length; j++ ) {
 				name = chunk[j];
 				BaseTreeItem ontologyItem = new BaseTreeItem(name, CSSIconImage.RDF);
 				tree.addItem(entityItem, ontologyItem);
 			}
 		}
 		tree.sortBy("sorterField");
 		if( nextResults > 0 ) {
 			BaseTreeItem linkItem = new BaseTreeItem("view more");
 			linkItem.setID("viewMore" + keyword);
 			tree.addItem(keywordItem, linkItem);
 		}
 	}
 	
 	private static void firstOntologyDomainAdding(String result, String keyword) {
 		// retrieve the tree
 		DomainOntologiesTree tree = MicroWSMOeditor.getDomainOntologiesTree();
 		// remove the loading icon
 		tree.removeItemById("loading " + keyword);
 		// if the result is empty
 		if( result.startsWith("0") ) {
 			return;
 		}
 		// manage the string result
 		String[] split = result.split("<!>")[1].split("<&>");
 		for( String s : split ) {
 			String[] chunk = s.split("<:>");
 			String name = chunk[0];
 			BaseTreeItem ontologyItem = null;
 			boolean canContinue = true;
 	
 			// if the ontology have not been retrieved yet
 			if( !tree.contains(name) ) {
 				// create a new root item
 				ontologyItem = new BaseTreeItem(name, CSSIconImage.RDF);
 				tree.addRootItem(ontologyItem);
 				
 				BaseTreeItem concepts = new BaseTreeItem("Concepts", CSSIconImage.FOLDER_CLOSED);
 				tree.addItem(ontologyItem, concepts);
 				ontologyItem.setKind(1);
 			// otherwise
 			} else {
 				// retrieve the root item
 				ontologyItem = tree.getItemById(name);
 				
 				List<ModelData> children = tree.getChildren(ontologyItem);
 				for( ModelData child : children ) {
 					BaseTreeItem item = (BaseTreeItem) child;
 					if( item.getID().equals(keyword) ) {
 						canContinue = false;
 						break;
 					}
 				}
 			}
 			
 			if( canContinue ) {
 				BaseTreeItem termItem = new BaseTreeItem(keyword, CSSIconImage.TERM);
 				tree.addItem(ontologyItem, termItem);
 				String icon = "";
 				for( int i = 1; i < chunk.length; i++ ) {
 					if( (i % 2) != 0 ) {
 						icon = CSSIconImage.getEntityIcon(chunk[i]);
 					} else {
 						name = chunk[i];
 						BaseTreeItem entityItem = new BaseTreeItem(name, icon);
 						tree.addItem(termItem, entityItem);
 					}
 				}
 			}
 			
 		}
 	}
 	
 	private static void nextServicePropertiesAdding(String result, String keyword) {
 		ServicePropertiesTree tree = MicroWSMOeditor.getServicePropertiesTree();
 		BaseTreeItem rootItem = tree.getItemById(keyword);
 		tree.removeSelectedItem();
 		
 		String[] stringSplitted = result.split("<!>");
 		int nextResults = new Integer(stringSplitted[0]);
 		stringSplitted = stringSplitted[1].split("<&>");
 		for( int i = 0; i < stringSplitted.length; i++ ) {
 			String s = stringSplitted[i];
 			String[] chunk = s.split("<:>");
 			String name = chunk[1];
 			String icon = CSSIconImage.getEntityIcon(chunk[0]);
 			BaseTreeItem entityItem = new BaseTreeItem(name, icon);
 			tree.addItem(rootItem, entityItem);
 			
 			// retrieve the ontology for this entity
 			for( int j = 2; j < chunk.length; j++ ) {
 				name = chunk[j];
 				BaseTreeItem ontologyItem = new BaseTreeItem(name, CSSIconImage.RDF);
 				tree.addItem(entityItem, ontologyItem);
 			}
 		}
 		tree.sortBy("sorterField");
 		if( nextResults > 0 ) {
 			BaseTreeItem linkItem = new BaseTreeItem("view more");
 			linkItem.setID("viewMore" + keyword);
 			tree.addItem(rootItem, linkItem);
 		}
 	}
 	
 	private static void nextOntologyDomainAdding(String result, String keyword) {
 		DomainOntologiesTree tree = MicroWSMOeditor.getDomainOntologiesTree();
 		
 		// if the result is empty
 		if( result.startsWith("0") ) {
 			return;
 		}
 		
 		// manage the string result
 		String[] split = result.split("<!>")[1].split("<&>");
 		for( String s : split ) {
 			String[] chunk = s.split("<:>");
 			String name = chunk[0];
 			BaseTreeItem ontologyItem = null;
 			boolean canContinue = true;
 	
 			// if the ontology have not been retrieved yet
 			if( !tree.contains(name) ) {
 				// create a new root item
 				ontologyItem = new BaseTreeItem(name, CSSIconImage.RDF);
 				tree.addRootItem(ontologyItem);
 				
 				BaseTreeItem concepts = new BaseTreeItem("Concepts", CSSIconImage.FOLDER_CLOSED);
 				tree.addItem(ontologyItem, concepts);
 			// otherwise
 			} else {
 				// retrieve the root item
 				ontologyItem = tree.getItemById(name);
 				
 				List<ModelData> children = tree.getChildren(ontologyItem);
 				for( ModelData child : children ) {
 					BaseTreeItem item = (BaseTreeItem) child;
 					if( item.getID().equals(keyword) ) {
 						canContinue = false;
 						break;
 					}
 				}
 			}
 			
 			if( canContinue ) {
 				BaseTreeItem termItem = new BaseTreeItem(keyword, CSSIconImage.TERM);
 				tree.addItem(ontologyItem, termItem);
 				String icon = null;
 				for( int i = 1; i < chunk.length; i++ ) {
 					if( (i % 2) != 0 ) {
 						icon = CSSIconImage.getEntityIcon(chunk[i]);
 					} else {
 						name = chunk[i];
 						BaseTreeItem entityItem = new BaseTreeItem(name, icon);
 						tree.addItem(termItem, entityItem);
 					}
 				}
 			}
 			
 		}
 	}
 	
 	public static native void retreiveConceptsOf(String url) /*-{
 	// handle the http comunication object
 	var httpRequest = @uk.ac.kmi.microwsmo.client.controller.ControllerToolkit::getXMLHttpRequest()();
 	if( httpRequest != null ) {
 		var parameters = "method=getConcepts&ontoURI=" + encodeURIComponent(url);
 		httpRequest.open("POST", "../watson", true);
 		// set the header
 		httpRequest.setRequestHeader("content-type", "application/x-www-form-urlencoded");
 		httpRequest.setRequestHeader("content-length", parameters.length);
 		httpRequest.setRequestHeader("connection", "close");
 		httpRequest.send(parameters);
 		// callback function
 		httpRequest.onreadystatechange = function() {
 			if( httpRequest.readyState == 4 && httpRequest.status == 200) {
 				var result = httpRequest.responseText;
 				@uk.ac.kmi.microwsmo.client.controller.SemanticController::populateConcepts(Ljava/lang/String;Ljava/lang/String;)(result, url);
 			}
 		}
 	} else {
 		@uk.ac.kmi.microwsmo.client.util.Message::show(Ljava/lang/String;)
     	(@uk.ac.kmi.microwsmo.client.util.Message::AJAX);
 	}
 }-*/;
 	
 	private static void populateConcepts(String result, String url) {
 		DomainOntologiesTree tree = MicroWSMOeditor.getDomainOntologiesTree();
 		BaseTreeItem item = tree.getItemById(url);
 		BaseTreeItem conceptsItem = tree.getChildOfConcepts(item);
 		conceptsItem.setID("");
 		if( !result.equals("") ) {
 			String[] concepts = result.split("<:>");
 			for( String concept : concepts ) {
 				tree.addItem(conceptsItem, new BaseTreeItem(concept, CSSIconImage.ONTO_CLASS));
 			}
 		} else {
 			tree.addItem(conceptsItem, new BaseTreeItem("No concepts"));
 		}
 	}
 	
	private static void populateAnnotations(String url, String id, String icon, String sel) {
 		AnnotationsTree tree = MicroWSMOeditor.getAnnotationsTree();
 		String keyword = ControllerToolkit.getTextSelected();
		if(keyword.trim() == "" || keyword == null){
			keyword = sel;
		}
 		BaseTreeItem root = new BaseTreeItem(keyword, CSSIconImage.TERM);
 		root.setID(id);
 		tree.addRootItem(root);
 		BaseTreeItem child = new BaseTreeItem(url, icon);
 		child.setKind(2);
 		tree.addItem(root, child);
 	}
 	
	public static native void annotate(String url, String icon, String sel) /*-{
 		try {
 			// retrieves the selection inside the iframe
 			var selection = @uk.ac.kmi.microwsmo.client.controller.ControllerToolkit::getSelection()(); 
 			try {
 				var range = selection.getRangeAt(0);
 				try {
 					if (typeof range.surroundContents != "undefined") {
 						// create the element
 						element = $doc.createElement("span");
 						// generate a unique ID and set it to the element
 						id = @uk.ac.kmi.microwsmo.client.util.ComponentID.IDGenerator::getID(Ljava/lang/String;)("entity");
 						attribute = $doc.createAttribute("id");
 						attribute.value = id;
 						element.setAttributeNode(attribute);
 						// create the attribute "class"
 						attribute = $doc.createAttribute("class");
 						attribute.value = "entity";
 						element.setAttributeNode(attribute);
 						// create the attribute "rel"
 						attribute = $doc.createAttribute("rel");
 						attribute.value = "model";
 						element.setAttributeNode(attribute);
 						// create the attribute "href"
 						attribute = document.createAttribute("href");
 						attribute.value = url;
 						element.setAttributeNode(attribute);
 						// add the element to the DOM
 					
 						//range.surroundContents(element);
 						element.appendChild(range.extractContents());
						//console.log(sel);
 						range.insertNode(element);
 						
 						// add the new element to the Annotations's tree
						@uk.ac.kmi.microwsmo.client.controller.SemanticController::populateAnnotations(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(url,id, icon, sel);
 					}
 				} catch (Error) {
 					@uk.ac.kmi.microwsmo.client.util.Message::show(Ljava/lang/String;)
 		    		(@uk.ac.kmi.microwsmo.client.util.Message::ANNOTATION_SELECTION);
 				}
 			} catch (Error) {
 				@uk.ac.kmi.microwsmo.client.util.Message::show(Ljava/lang/String;)
 		    	(@uk.ac.kmi.microwsmo.client.util.Message::ANNOTATION_SELECTION);
 			}
 		} catch (Error) {
 			@uk.ac.kmi.microwsmo.client.util.Message::show(Ljava/lang/String;)
 		    (@uk.ac.kmi.microwsmo.client.util.Message::ANNOTATION_SELECTION);
 		}
 	}-*/;
 }
