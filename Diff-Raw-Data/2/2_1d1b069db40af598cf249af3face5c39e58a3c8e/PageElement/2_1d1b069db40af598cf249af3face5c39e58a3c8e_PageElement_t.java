 /*
  * Created on Apr 20, 2005
  * 
  * This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE
  * Version 2, which can be found at http://www.gnu.org/copyleft/gpl.html
 */
 package org.cubictest.model;
 
 import static org.cubictest.model.IdentifierType.LABEL;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Base class for elements on a page.
  * 
  * @author skyttere
  * @author chr_schwarz
  *
  */
 public abstract class PageElement extends PropertyAwareObject 
 			implements Cloneable, IActionElement, IDescription {
 	
 	public static final String DIRECT_EDIT_IDENTIFIER = "directEditIdentifier";
 	private String description = "";
 	private List<Identifier> identifiers;
 	private Identifier directEditIdentifier;
 	private boolean not;
 	
 	public String toString() {
 		return getType() + ": " + getDescription() + " = " + getText();
 	}
 	
 	/**
 	 * Get the user interaction types that is possible on this page elemenent.
 	 */
 	public List<ActionType> getActionTypes() {
 		List<ActionType> actions = new ArrayList<ActionType>();
 		actions.add(ActionType.CLICK);
 		actions.add(ActionType.MOUSE_OVER);
 		actions.add(ActionType.MOUSE_OUT);
 		actions.add(ActionType.DBLCLICK);
 		return actions;
 	}
 	
 	@Override
 	public void resetStatus() {
 		setStatus(TestPartStatus.UNKNOWN);		
 	}
 	
 	@Override
 	public PageElement clone() throws CloneNotSupportedException {
 		PageElement clone = (PageElement) super.clone();
 		clone.setDescription(description);
 		List<Identifier> clonedIdentifiers = new ArrayList<Identifier>();
 		for(Identifier id : identifiers){
 			Identifier idClone = id.clone();
 			clonedIdentifiers.add(idClone);
 			if (idClone.equals(directEditIdentifier))
 				clone.directEditIdentifier = idClone;
 		}
 		clone.setIdentifiers(clonedIdentifiers);
 		return clone;
 	}
 	
 	/**
 	 * Get the text that is shown in the CubicTest GUI for the page element.
 	 * @return the text that is shown in the CubicTest GUI for the page element.
 	 */
 	public String getDescription() {
 		if (description == null)
 			return "";
 		return description;
 	}
 
 	/**
 	 * Set the text that is shown in the CubicTest GUI for the page element.
 	 * @param text The text that is shown in the CubicTest GUI for the page element.
 	 */
 	public void setDescription(String desc) {
 		String oldDesc = this.description;
 		this.description = desc;
 		firePropertyChange(PropertyAwareObject.NAME, oldDesc, desc);
 	}
 	
 	public String getText() {
 		return getDirectEditIdentifier().getValue();
 	}
 	
 	/**
 	 * Set the text used to identify the element in the HTML page.
 	 * Can be e.g. text in label, ID or name, according to type in setIdentifierType().
 	 */
 	public void setText(String text) {
 		String oldText = getText();
 		getDirectEditIdentifier().setValue(text);
 		firePropertyChange(PropertyAwareObject.NAME, oldText, text);
 	}
 	
 	/**
 	 * Get the user friendly type-name of this page element.
 	 */
 	public abstract String getType();
 	
 	/**
 	 * Get the default action for user interaction with this page element.
 	 */
 	public ActionType getDefaultAction(){
 		return getActionTypes().get(0);
 	}
 	
 	public void setIdentifiers(List<Identifier> identifiers) {
 		this.identifiers = identifiers;
 	}
 	
 	public List<Identifier> getIdentifiers() {
 		if(identifiers == null){
 			identifiers = new ArrayList<Identifier>();
 			for(IdentifierType type : getIdentifierTypes()){
 				Identifier identifier = new Identifier();
 				identifier.setType(type);
 				identifiers.add(identifier);
 			}
 			if(identifiers.size() > 0)
 				identifiers.get(0).setProbability(Identifier.MAX_PROBABILITY);
 		}
 		return identifiers;
 	}
 	
 	/**
 	 * Set the identifier types that this page elements supports.
 	 */
 	public void addIdentifier(Identifier identifier) {
 		identifiers.add(identifier);
 		firePropertyChange(PropertyAwareObject.NAME, null, identifier);
 	}
 	
 	/**
 	 * Get the identifier types that this page elements supports.
 	 * @return the identifier types that this page elements supports.
 	 */
 	public List<IdentifierType> getIdentifierTypes() {	
 		List<IdentifierType> list = new ArrayList<IdentifierType>();
 		list.add(LABEL);
 		return list;
 	}
 	
 	public void setDirectEditIdentifier(Identifier directEditIdentifier) {
 		Identifier oldDirectEditIdentifier = this.directEditIdentifier;
 		this.directEditIdentifier = directEditIdentifier;
 		firePropertyChange(DIRECT_EDIT_IDENTIFIER, oldDirectEditIdentifier, directEditIdentifier);
 	}
 	
 	public Identifier getDirectEditIdentifier() {
 		if(directEditIdentifier == null){
 			directEditIdentifier = getIdentifiers().get(0);
 		}
 		return directEditIdentifier;
 	}
 	
 	public boolean isNot(){
 		return not;
 	}
 	
 	public void setNot(boolean not){
 		boolean oldNot = this.not;
 		this.not = not;
 		firePropertyChange(PropertyAwareObject.NOT, oldNot, not);
 	}
 
 	
 	/**
 	 * Gets the Identifier with the highest probability (must be greater than "indifferent").
 	 * If more than one has the same probability, returns the first.
 	 */
 	public Identifier getMostSignificantIdentifier() {
 		int highestProbability = 0;
 		Identifier result = null;
 		
 		for (Identifier identifier : getIdentifiers()) {
 			if (identifier.getProbability() >  highestProbability) {
 				result = identifier;
 				highestProbability = identifier.getProbability();
 			}
 			else if (identifier.getProbability() ==  highestProbability) {
 				//direct edit has precedence:
 				if (getDirectEditIdentifier().equals(identifier)) {
 					result = identifier;
 					highestProbability = identifier.getProbability();
 				}
				else if (StringUtils.isNotBlank(identifier.getValue()) && (result == null || StringUtils.isBlank(result.getValue()))) {
 					//the ID with a non-null value has precedence:
 					result = identifier;
 					highestProbability = identifier.getProbability();
 				}
 			}
 		}
 		return result;
 	}
 	
 	public Identifier getIdentifier(IdentifierType idType) {
 		for (Identifier identifier : getIdentifiers()) {
 			if (identifier.getType().equals(idType)) {
 				return identifier;
 			}
 		}
 		return null;
 	}
 }
