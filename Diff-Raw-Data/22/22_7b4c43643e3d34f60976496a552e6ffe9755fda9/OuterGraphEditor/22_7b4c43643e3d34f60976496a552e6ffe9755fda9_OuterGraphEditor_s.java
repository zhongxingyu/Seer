 /*******************************************************************************
  * Copyright (c) 2012 Emanuele Tamponi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele Tamponi - initial API and implementation
  ******************************************************************************/
 package game.plugins.editors.graph;
 
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Node;
 import javafx.scene.control.Button;
 import game.configuration.Configurable.Change;
 import game.core.Graph;
 import game.editorsystem.ControlledEditor;
 import game.editorsystem.Editor;
 import game.editorsystem.EditorWindow;
 
 public class OuterGraphEditor extends Editor {
 	
 	private static class GraphEditor extends ControlledEditor {
 		
 		@Override
 		public Class getBaseEditableClass() {
 			return Graph.class;
 		}
 
 		@Override
 		public boolean isInline() {
 			return false;
 		}
 	}
 	
 	public Button editorButton = new Button("Edit graph");
 	public GraphEditor editor = new GraphEditor();
 	
 	public OuterGraphEditor() {
 		editorButton.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent event) {
 				new EditorWindow(editor).showAndWait();
 			}
 		});
 	}
 
 	@Override
 	public Class getBaseEditableClass() {
 		return Graph.class;
 	}
 
 	@Override
 	public boolean isInline() {
 		return true;
 	}
 
 	@Override
 	public Node getView() {
 		return editorButton;
 	}
 
 	@Override
 	public void connectView() {
 		editor.setModel(getModel());
 		editor.connectView();
 	}
 
 	@Override
 	public void updateView(Change change) {
 		editor.updateView(change);
 	}
 
 }
