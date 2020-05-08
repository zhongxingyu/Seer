 package org.genericsystem.myadmin.beans;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.enterprise.context.SessionScoped;
 import javax.faces.model.SelectItem;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.genericsystem.constraints.RequiredConstraintImpl;
 import org.genericsystem.constraints.SingularConstraintImpl;
 import org.genericsystem.constraints.SizeConstraintImpl;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.myadmin.util.GsMessages;
 import org.genericsystem.systemproperties.CascadeRemoveSystemProperty;
import org.genericsystem.systemproperties.NoInheritanceSystemType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Named
 @SessionScoped
 public class PopupPanelBean implements Serializable {
 	protected static Logger log = LoggerFactory.getLogger(PopupPanelBean.class);
 
 	private static final long serialVersionUID = -3475636000594021248L;
 
 	@Inject
 	private GsMessages messages;
 
 	private String popupClass;
 
 	private int popupPos;
 
 	private Boolean popupValue;
 
 	private String popupkey;
 
 	private String popupValueProp;
 
 	@Named
 	private Attribute attribute;
 
 	@Inject
 	private GuiGenericsTreeBean genericTreeBean;
 
 	public void addProperties() {
 		genericTreeBean.getSelectedTreeNode().getGeneric().getPropertiesMap().put(getPopupkey(), getPopupValue());
 		messages.info("successadd", getClazz().getSimpleName());
 	}
 
 	public void addConstraints() {
 		((GenericImpl) genericTreeBean.getSelectedTreeNode().getGeneric()).setConstraintValue(getClazz(), popupPos, popupValue);
 		messages.info("successadd", getClazz().getSimpleName());
 	}
 
 	public void addSystemProperty() {
 		((GenericImpl) genericTreeBean.getSelectedTreeNode().getGeneric()).setSystemPropertyValue(getClazz(), popupPos, popupValue);
 		messages.info("successadd", getClazz().getSimpleName());
 	}
 
 	@SuppressWarnings("unchecked")
 	public Class<GenericImpl> getClazz() {
 		try {
 			return (Class<GenericImpl>) Class.forName(getPopupClass().substring(6, getPopupClass().length()));
 		} catch (ClassNotFoundException e) {
 			messages.info("ClassNotFoundException " + e.getMessage());
 		}
 		return null;
 	}
 
 	public List<SelectItem> getConstraintClass() {
 		List<SelectItem> list = new ArrayList<>();
 		list.add(new SelectItem(RequiredConstraintImpl.class, "RequiredConstraintImpl"));
 		list.add(new SelectItem(SingularConstraintImpl.class, "SingularConstraintImpl"));
 		list.add(new SelectItem(SizeConstraintImpl.class, "SizeConstraintImpl"));
 		return list;
 	}
 
 	public List<SelectItem> getSystemPropertiesClass() {
 		List<SelectItem> list = new ArrayList<>();
 		list.add(new SelectItem(CascadeRemoveSystemProperty.class, "CascadeRemoveSystemProperty"));
		list.add(new SelectItem(NoInheritanceSystemType.class, "NoInheritanceSystemType"));
 		return list;
 	}
 
 	public Attribute getAttribute() {
 		return attribute;
 	}
 
 	public void setAttribute(Attribute attribute) {
 		this.attribute = attribute;
 	}
 
 	public String getPopupClass() {
 		return popupClass;
 	}
 
 	public void setPopupClass(String popupClass) {
 		this.popupClass = popupClass;
 	}
 
 	public int getPopupPos() {
 		return popupPos;
 	}
 
 	public void setPopupPos(int popupPos) {
 		this.popupPos = popupPos;
 	}
 
 	public Boolean getPopupValue() {
 		return popupValue;
 	}
 
 	public void setPopupValue(Boolean popupValue) {
 		this.popupValue = popupValue;
 	}
 
 	public String getPopupkey() {
 		return popupkey;
 	}
 
 	public void setPopupkey(String popupkey) {
 		this.popupkey = popupkey;
 	}
 
 	public String getPopupValueProp() {
 		return popupValueProp;
 	}
 
 	public void setPopupValueProp(String popupValueProp) {
 		this.popupValueProp = popupValueProp;
 	}
 
 }
