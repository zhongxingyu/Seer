 package org.sopeco.frontend.client.layout.center.specification;
 
 import org.sopeco.frontend.client.event.EventControl;
 import org.sopeco.frontend.client.event.InitialAssignmentChangedEvent;
 import org.sopeco.frontend.client.event.InitialAssignmentChangedEvent.ChangeType;
 import org.sopeco.frontend.client.widget.tree.TreeItem;
 
 import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.CheckBox;
 
 /**
  * 
  * @author Marius Oehler
  * 
  */
 public class SelectionTreeItemLeaf extends TreeItem implements ValueChangeHandler<Boolean> {
 
 	private static final String CSS_LEAF_CLASS = "selectionTreeItemLeaf";
 	private Element textElement;
 	private CheckBox selectedCheckBox;
 
 	public SelectionTreeItemLeaf(String pText) {
 		super(pText);
 	}
 
 	public SelectionTreeItemLeaf(String pText, boolean selected) {
 		super(pText);
 
 		if (selected) {
 			selectedCheckBox.setValue(selected);
 		}
 	}
 
 	@Override
 	protected void initialize() {
 		addStyleName(CSS_LEAF_CLASS);
		setHeight("26px");
 
 		textElement = DOM.createSpan();
 		textElement.setInnerHTML(getText());
 
		textElement.getStyle().setLeft(10, Unit.PX);
		textElement.getStyle().setRight(40, Unit.PX);
		textElement.getStyle().setOverflow(Overflow.HIDDEN);
		textElement.getStyle().setPosition(Position.ABSOLUTE);
		textElement.setTitle(getText());

 		selectedCheckBox = new CheckBox();
 		selectedCheckBox.addValueChangeHandler(this);
		selectedCheckBox.getElement().getStyle().setPosition(Position.ABSOLUTE);
		selectedCheckBox.getElement().getStyle().setRight(0, Unit.PX);
 
 		getElement().appendChild(textElement);
 		add(selectedCheckBox);
 	}
 
 	@Override
 	public void onValueChange(ValueChangeEvent<Boolean> event) {
 		ChangeType type;
 		if (event.getValue()) {
 			type = ChangeType.Added;
 		} else {
 			type = ChangeType.Removed;
 		}
 		InitialAssignmentChangedEvent changeEvent = new InitialAssignmentChangedEvent(getPath(), type);
 		EventControl.get().fireEvent(changeEvent);
 	}
 }
