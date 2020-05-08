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
 package game.plugins.editors;
 
 import game.configuration.Configurable;
 import game.configuration.Configurable.Change;
 import game.editorsystem.Editor;
 import game.editorsystem.EditorWindow;
 import game.editorsystem.Option;
 import game.plugins.Implementation;
 import game.utils.Utils;
 
 import java.util.Observable;
 import java.util.Set;
 
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Node;
 import javafx.scene.control.Button;
 import javafx.scene.control.ComboBox;
 import javafx.scene.layout.HBox;
 
 public class ImplementationChooserEditor extends Editor {
 	
 	private ChangeListener<Implementation> listener = new ChangeListener<Implementation>() {
 		@Override
 		public void changed(
 				ObservableValue<? extends Implementation> observable,
 				Implementation oldValue, Implementation newValue) {
 			if (getModel() == null)
 				return;
 			Configurable selected = box.getValue().getContent();
 			if (getModel().getContent() != selected)
 				getModel().setContent(selected);
 		}
 	};
 	
 	private HBox container = new HBox();
 	
 	private ComboBox<Implementation<Configurable>> box = new ComboBox<>();
 	
 	public ImplementationChooserEditor() {
 		Button editButton = new Button("Edit");
 		editButton.setPrefWidth(50);
 		editButton.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent event) {
 				if (getModel().getContent() == null)
 					return;
 				
 				Option option = new Option((Configurable)getModel().getContent());
 				Editor editor = option.getBestEditor();
 				editor.setModel(option);
 				EditorWindow window = new EditorWindow(editor);
 				window.show();
 			}
 		});
 		container.getChildren().addAll(box, editButton);
		box.prefWidthProperty().bind(container.widthProperty().subtract(editButton.prefWidthProperty()));
 	}
 
 	@Override
 	public Node getView() {
 		return container;
 	}
 
 	@Override
 	public void connectView() {		
 		box.getSelectionModel().selectedItemProperty().removeListener(listener);
 		box.getItems().clear();
 		
 		if (getModel() != null) {
 			Object current = getModel().getContent();
 			box.getItems().add(new Implementation(null));
 			if (current == null)
 				box.getSelectionModel().select(0);
 			
 			Set<Implementation<Configurable>> implementations = getModel().getCompatibleImplementations();
 			for (Implementation<Configurable> impl: implementations) {
 				if (current != null && current.getClass().equals(impl.getContent().getClass())) {
 					box.getItems().add(new Implementation(current));
 					box.getSelectionModel().select(box.getItems().size()-1);
 				} else
 					box.getItems().add(impl);
 			}
 
 			box.getSelectionModel().selectedItemProperty().addListener(listener);
 		}
 	}
 
 	@Override
 	public void update(Observable observed, Object message) {
 		if (message instanceof Change) {
 			updateView((Change)message);
 		}
 	}
 
 	@Override
 	public void updateView(Change change) {
 		connectView();
 	}
 
 	@Override
 	public boolean canEdit(Class type) {
 		return !Utils.isConcrete(type) && getBaseEditableClass().isAssignableFrom(type);
 	}
 
 	@Override
 	public Class getBaseEditableClass() {
 		return Configurable.class;
 	}
 
 	@Override
 	public boolean isInline() {
 		return true;
 	}
 
 }
