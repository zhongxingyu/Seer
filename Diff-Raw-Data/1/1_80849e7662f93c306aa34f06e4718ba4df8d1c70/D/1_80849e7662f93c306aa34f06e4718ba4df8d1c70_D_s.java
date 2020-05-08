 package com.dooapp.easyInheritance.wrapper;
 
 import com.dooapp.easyInheritance.entity.DBean;
 
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.beans.property.SimpleObjectProperty;
 import javafx.beans.property.ObjectProperty;
 import javafx.beans.property.SimpleLongProperty;
 import javafx.beans.property.LongProperty;
 import javafx.beans.property.SimpleStringProperty;
 import javafx.beans.property.StringProperty;
 import javafx.beans.property.SimpleIntegerProperty;
 import javafx.beans.property.IntegerProperty;
 
 
 
 
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 import org.jdom2.Attribute;
 import org.jdom2.Element;
 
 /**
  * <!-- begin-user-doc -->
  * <!--  end-user-doc  -->
  * @generated
  */
 
 public class D
 // Start of user code bloc for inheritance
 
 // End of user code
 // Start of user code bloc for interface
  implements com.dooapp.lib.common.entity.Wrapper
 // End of user code
 {
 	/**
 	* * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
	*
 	private StringProperty attributeProperty;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	
 	private ObjectProperty<B> bProperty;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	
 	private ObjectProperty<java.util.Date> creationDateProperty;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	
 	private LongProperty idProperty;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	
 	private ObjectProperty<java.util.Date> updateDateProperty;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	
 	private IntegerProperty versionProperty;
 
 	/**
 	 * Handled Entity
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
  	 */
 	private final DBean d;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public D(){
 		this(new DBean());	
 	}
 	
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public D(DBean entity) {
 		if (entity == null) {
 			throw new NullPointerException("Unable to create a wrapper with a null bean");
 		}
 		if (entity.isWrapped()) {
 			if (entity.getWrapper() != this) {
 				throw new RuntimeException("A bean can only have one wrapper, use #getWrapper instead");
 			}
 		}
 		entity.setWrapper(this);
 		this.d = entity;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public DBean getDBean(){
 		return this.d;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public DBean getBean() {
 		return  this.d;
 	}
 	
 
 	/*
 	 * Generated getters and setters
 	 */
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public StringProperty attributeProperty(){
 		if (this.attributeProperty == null) {
 			this.attributeProperty = new SimpleStringProperty(d.getAttribute());
 			this.attributeProperty.addListener(new ChangeListener<String>() {
 				@Override
 				public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
 					d.setAttribute((String) arg2);
 				}
 			});
 		}
 		return this.attributeProperty;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public String getAttribute(){
 		return attributeProperty().get();
 	} 
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void setAttribute(String myAttribute){
 		this.attributeProperty().set(myAttribute);
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void updateAttribute(final String myAttribute, final Object mutex) {
 		if (javafx.application.Platform.isFxApplicationThread()) {
 			setAttribute(myAttribute);
 			if (mutex != null) {
 				mutex.notify();
 			}
 		} else {
 			javafx.application.Platform.runLater(new Runnable() {
 				@Override
 				public void run() {
 					setAttribute(myAttribute);
 					if (mutex != null) {
 						mutex.notify();
 					}
 				}
 			});
 		}
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void updateAttribute(final String myAttribute) {
 		updateAttribute(myAttribute, null);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public ObjectProperty<B> bProperty() {
 		if (bProperty == null) {
 			bProperty = new SimpleObjectProperty<B>(d.getB() == null ? null : d
 					.getB().getWrapper());
 			bProperty.addListener(new ChangeListener<B>() {
 				@Override
 				public void changed(ObservableValue<? extends B> obj, B oldValue, B newValue) {
 					d.setB(newValue == null ? null : newValue.getBean());
 				}
 			});
 		}
 		return bProperty;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public B getB(){
 		return bProperty().get();
 	} 
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void setB(B myB){
 		if (bProperty == null) {
 				d.setB(myB == null ? null : myB.getBean());
 			} else {
 				this.bProperty().set(myB);
 			}
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void updateB(final B myB, final Object mutex) {
 		if (javafx.application.Platform.isFxApplicationThread()) {
 			setB(myB);
 			if (mutex != null) {
 				mutex.notify();
 			}
 		} else {
 			javafx.application.Platform.runLater(new Runnable() {
 				@Override
 				public void run() {
 					setB(myB);
 					if (mutex != null) {
 						mutex.notify();
 					}
 				}
 			});
 		}
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void updateB(final B myB) {
 		updateB(myB, null);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public ObjectProperty<java.util.Date> creationDateProperty() {
 		if (creationDateProperty == null) {
 			creationDateProperty = new SimpleObjectProperty<java.util.Date>(d.getCreationDate());
 			creationDateProperty.addListener(new ChangeListener<java.util.Date>() {
 				@Override
 				public void changed(ObservableValue<? extends java.util.Date> arg0, java.util.Date arg1, java.util.Date arg2) {
 					d.setCreationDate(arg2);
 				}
 			});
 		}
 		return creationDateProperty;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public java.util.Date getCreationDate(){
 		return creationDateProperty().get();
 	} 
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void setCreationDate(java.util.Date myCreationDate){
 		if (creationDateProperty == null) {
 				d.setCreationDate(myCreationDate);
 			} else {
 				this.creationDateProperty().set(myCreationDate);
 			}
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void updateCreationDate(final java.util.Date myCreationDate, final Object mutex) {
 		if (javafx.application.Platform.isFxApplicationThread()) {
 			setCreationDate(myCreationDate);
 			if (mutex != null) {
 				mutex.notify();
 			}
 		} else {
 			javafx.application.Platform.runLater(new Runnable() {
 				@Override
 				public void run() {
 					setCreationDate(myCreationDate);
 					if (mutex != null) {
 						mutex.notify();
 					}
 				}
 			});
 		}
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void updateCreationDate(final java.util.Date myCreationDate) {
 		updateCreationDate(myCreationDate, null);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public LongProperty idProperty(){
 		if (this.idProperty == null) {
 			this.idProperty = new SimpleLongProperty(d.getId());
 			this.idProperty.addListener(new ChangeListener<Number>() {
 				@Override
 				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
 					d.setId((Long) arg2);
 				}
 			});
 		}
 		return this.idProperty;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public long getId(){
 		return idProperty().get();
 	} 
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void setId(long myId){
 		this.idProperty().set(myId);
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void updateId(final long myId, final Object mutex) {
 		if (javafx.application.Platform.isFxApplicationThread()) {
 			setId(myId);
 			if (mutex != null) {
 				mutex.notify();
 			}
 		} else {
 			javafx.application.Platform.runLater(new Runnable() {
 				@Override
 				public void run() {
 					setId(myId);
 					if (mutex != null) {
 						mutex.notify();
 					}
 				}
 			});
 		}
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void updateId(final long myId) {
 		updateId(myId, null);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public ObjectProperty<java.util.Date> updateDateProperty() {
 		if (updateDateProperty == null) {
 			updateDateProperty = new SimpleObjectProperty<java.util.Date>(d.getUpdateDate());
 			updateDateProperty.addListener(new ChangeListener<java.util.Date>() {
 				@Override
 				public void changed(ObservableValue<? extends java.util.Date> arg0, java.util.Date arg1, java.util.Date arg2) {
 					d.setUpdateDate(arg2);
 				}
 			});
 		}
 		return updateDateProperty;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public java.util.Date getUpdateDate(){
 		return updateDateProperty().get();
 	} 
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void setUpdateDate(java.util.Date myUpdateDate){
 		if (updateDateProperty == null) {
 				d.setUpdateDate(myUpdateDate);
 			} else {
 				this.updateDateProperty().set(myUpdateDate);
 			}
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void updateUpdateDate(final java.util.Date myUpdateDate, final Object mutex) {
 		if (javafx.application.Platform.isFxApplicationThread()) {
 			setUpdateDate(myUpdateDate);
 			if (mutex != null) {
 				mutex.notify();
 			}
 		} else {
 			javafx.application.Platform.runLater(new Runnable() {
 				@Override
 				public void run() {
 					setUpdateDate(myUpdateDate);
 					if (mutex != null) {
 						mutex.notify();
 					}
 				}
 			});
 		}
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void updateUpdateDate(final java.util.Date myUpdateDate) {
 		updateUpdateDate(myUpdateDate, null);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public IntegerProperty versionProperty(){
 		if (this.versionProperty == null) {
 			this.versionProperty = new SimpleIntegerProperty(d.getVersion());
 			this.versionProperty.addListener(new ChangeListener<Number>() {
 				@Override
 				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
 					d.setVersion((Integer) arg2);
 				}
 			});
 		}
 		return this.versionProperty;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public int getVersion(){
 		return versionProperty().get();
 	} 
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void setVersion(int myVersion){
 		this.versionProperty().set(myVersion);
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void updateVersion(final int myVersion, final Object mutex) {
 		if (javafx.application.Platform.isFxApplicationThread()) {
 			setVersion(myVersion);
 			if (mutex != null) {
 				mutex.notify();
 			}
 		} else {
 			javafx.application.Platform.runLater(new Runnable() {
 				@Override
 				public void run() {
 					setVersion(myVersion);
 					if (mutex != null) {
 						mutex.notify();
 					}
 				}
 			});
 		}
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	public void updateVersion(final int myVersion) {
 		updateVersion(myVersion, null);
 	}
 
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
  	 */
 	public String toXML(){
 		XMLOutputter serializer = new XMLOutputter();
 		serializer.setFormat(Format.getPrettyFormat());
 		return getDBean() != null ? serializer.outputString(this.toDomXML()) : "";
 		
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	private Element toDomXML(){
 		Element xmlElement = new Element("D");
 		if (getAttribute() != null)
 			xmlElement.setAttribute(new Attribute("attribute", getAttribute()));
 		xmlElement.setAttribute(new Attribute("id", String.valueOf(getId())));
 		if (getCreationDate() != null)
 			xmlElement.setAttribute(new Attribute("creationDate", getCreationDate().toString()));
 		if (getUpdateDate() != null)
 			xmlElement.setAttribute(new Attribute("updateDate", getUpdateDate().toString()));
 		xmlElement.setAttribute(new Attribute("version", String.valueOf(getVersion())));
 		
 		if (getB() != null)
 			xmlElement.setAttribute("b", String.valueOf(getB().getId()));
 		
 		
 		// For custom purposes
 		xmlElement = this.customDomXML(xmlElement);
 		
 		return xmlElement;
 	}
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!--  end-user-doc  -->
 	 * @generated
 	 */
 	private Element customDomXML(Element e) {
 		return e;
 	}
 
 }
