 /**
  * <copyright>
  * </copyright>
  *
 * $Id: PaletteInfoImpl.java,v 1.5 2007/04/16 19:55:08 itrimble Exp $
  */
 package org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.internal.impl;
 
 import java.util.Iterator;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.jst.jsf.common.metadata.Entity;
 import org.eclipse.jst.jsf.common.metadata.Model;
 import org.eclipse.jst.jsf.common.metadata.Trait;
 import org.eclipse.jst.jsf.common.metadata.internal.TraitValueHelper;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.PaletteInfo;
 import org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.PaletteInfosPackage;
 import org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.TagCreationInfo;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Palette Info</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.internal.impl.PaletteInfoImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.internal.impl.PaletteInfoImpl#getTag <em>Tag</em>}</li>
  *   <li>{@link org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.internal.impl.PaletteInfoImpl#getDisplayLabel <em>Display Label</em>}</li>
  *   <li>{@link org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.internal.impl.PaletteInfoImpl#getDescription <em>Description</em>}</li>
  *   <li>{@link org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.internal.impl.PaletteInfoImpl#getExpert <em>Expert</em>}</li>
  *   <li>{@link org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.internal.impl.PaletteInfoImpl#getHidden <em>Hidden</em>}</li>
  *   <li>{@link org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.internal.impl.PaletteInfoImpl#getSmallIcon <em>Small Icon</em>}</li>
  *   <li>{@link org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.internal.impl.PaletteInfoImpl#getLargeIcon <em>Large Icon</em>}</li>
  *   <li>{@link org.eclipse.jst.pagedesigner.editors.palette.paletteinfos.internal.impl.PaletteInfoImpl#getTagCreation <em>Tag Creation</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class PaletteInfoImpl extends EObjectImpl implements PaletteInfo {	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2007 Oracle Corporation";
 
 	/**
 	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getId()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ID_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getId()
 	 * @generated
 	 * @ordered
 	 */
 	protected String id = ID_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getTag() <em>Tag</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTag()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String TAG_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getTag() <em>Tag</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTag()
 	 * @generated
 	 * @ordered
 	 */
 	protected String tag = TAG_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDisplayLabel() <em>Display Label</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDisplayLabel()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DISPLAY_LABEL_EDEFAULT = null;
 
 
 
 	/**
 	 * The cached value of the '{@link #getDisplayLabel() <em>Display Label</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDisplayLabel()
 	 * @generated
 	 * @ordered
 	 */
 	protected String displayLabel = DISPLAY_LABEL_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DESCRIPTION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected String description = DESCRIPTION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getExpert() <em>Expert</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getExpert()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Boolean EXPERT_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getExpert() <em>Expert</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getExpert()
 	 * @generated
 	 * @ordered
 	 */
 	protected Boolean expert = EXPERT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getHidden() <em>Hidden</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getHidden()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Boolean HIDDEN_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getHidden() <em>Hidden</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getHidden()
 	 * @generated
 	 * @ordered
 	 */
 	protected Boolean hidden = HIDDEN_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getSmallIcon() <em>Small Icon</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSmallIcon()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String SMALL_ICON_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getSmallIcon() <em>Small Icon</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSmallIcon()
 	 * @generated
 	 * @ordered
 	 */
 	protected String smallIcon = SMALL_ICON_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getLargeIcon() <em>Large Icon</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLargeIcon()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String LARGE_ICON_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getLargeIcon() <em>Large Icon</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLargeIcon()
 	 * @generated
 	 * @ordered
 	 */
 	protected String largeIcon = LARGE_ICON_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getTagCreation() <em>Tag Creation</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTagCreation()
 	 * @generated
 	 * @ordered
 	 */
 	protected TagCreationInfo tagCreation = null;
 
 	private Entity _tag;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected PaletteInfoImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return PaletteInfosPackage.Literals.PALETTE_INFO;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setId(String newId) {
 		String oldId = id;
 		id = newId;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PaletteInfosPackage.PALETTE_INFO__ID, oldId, id));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getTag() {
 		return tag != null ? tag : getId();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTag(String newTag) {
 		String oldTag = tag;
 		tag = newTag;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PaletteInfosPackage.PALETTE_INFO__TAG, oldTag, tag));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getDisplayLabel() {
 		if (displayLabel == null){
 			return getTagTraitValueAsString(getTag(), TRAIT_DISPLAY_LABEL);
 		}
 		return getNLSedValue(displayLabel);
 	}
 
 	private String getNLSedValue(final String rawValue) {		
 		String value = TraitValueHelper.getNLSValue((Trait)this.eContainer.eContainer(), rawValue);								
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	private String getTagTraitValueAsString(final String _id, final String traitKey) {
 		EObject obj = getTagTraitValue(getTag(), traitKey);
 		if (obj != null){
 			return TraitValueHelper.getValueAsString((Trait)obj.eContainer());
 		}
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	private EObject getTagTraitValue(final String tagName, final String traitKey) {
		Entity tag = getTag(tagName);
		if (tag != null){
			for (Iterator it=tag.getTraits().iterator();it.hasNext();){
 				Trait trait = (Trait)it.next();
 				if (traitKey.equals(trait.getId()))
 					return trait.getValue(); 	
 			}
 		}
 		return null;
 	}
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	private Entity getTag(String tagName) {
 		if (_tag == null){
 			//rely on PaletteInfos objects being inside a Model trait only			
 			try {
 				Model model = (Model)this.eContainer().eContainer().eContainer();
 				//we could probably do better than this... oh well
                 //FIXME: C.B: this is really bad! We could probably do better than
                 // write code that expects to throw ClassCastExceptions and then hides them?  I hope so!
 				_tag = findTag(model, tagName);
 			} catch (ClassCastException e) {		
 			    PDPlugin.getLogger(getClass()).error(e);
 				return null;
 			}
 		}
 		return _tag;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	private Entity findTag(Model model, String tagid) {
 		for (Iterator it=model.getChildEntities().iterator();it.hasNext();){
			Entity tag = (Entity)it.next();
			if (tagid.equals(tag.getId()))
					return tag;
 		}
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDisplayLabel(String newDisplayLabel) {
 		String oldDisplayLabel = displayLabel;
 		displayLabel = newDisplayLabel;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PaletteInfosPackage.PALETTE_INFO__DISPLAY_LABEL, oldDisplayLabel, displayLabel));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getDescription() {
 		if (description == null){
 			return getTagTraitValueAsString(getId(), TRAIT_DESCRIPTION);
 		}
 		return getNLSedValue(description);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDescription(String newDescription) {
 		String oldDescription = description;
 		description = newDescription;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PaletteInfosPackage.PALETTE_INFO__DESCRIPTION, oldDescription, description));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Boolean getExpert() {
 		return expert;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setExpert(Boolean newExpert) {
 		Boolean oldExpert = expert;
 		expert = newExpert;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PaletteInfosPackage.PALETTE_INFO__EXPERT, oldExpert, expert));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Boolean getHidden() {
 		return hidden;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setHidden(Boolean newHidden) {
 		Boolean oldHidden = hidden;
 		hidden = newHidden;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PaletteInfosPackage.PALETTE_INFO__HIDDEN, oldHidden, hidden));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getSmallIcon() {
 		if (smallIcon == null){
 			return getTagTraitValueAsString(getId(), TRAIT_SMALL_ICON);
 		}
 		return smallIcon;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSmallIcon(String newSmallIcon) {
 		String oldSmallIcon = smallIcon;
 		smallIcon = newSmallIcon;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PaletteInfosPackage.PALETTE_INFO__SMALL_ICON, oldSmallIcon, smallIcon));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getLargeIcon() {
 		if (largeIcon == null){
 			return getTagTraitValueAsString(getId(), TRAIT_LARGE_ICON);
 		}
 		return largeIcon;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLargeIcon(String newLargeIcon) {
 		String oldLargeIcon = largeIcon;
 		largeIcon = newLargeIcon;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PaletteInfosPackage.PALETTE_INFO__LARGE_ICON, oldLargeIcon, largeIcon));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public TagCreationInfo getTagCreation() {
 		if (tagCreation != null && tagCreation.eIsProxy()) {
 			InternalEObject oldTagCreation = (InternalEObject)tagCreation;
 			tagCreation = (TagCreationInfo)eResolveProxy(oldTagCreation);
 			if (tagCreation != oldTagCreation) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, PaletteInfosPackage.PALETTE_INFO__TAG_CREATION, oldTagCreation, tagCreation));
 			}
 		}
 		else if (tagCreation == null){
 			//delegate to entity if it exists
 			return (TagCreationInfo)getTagTraitValue(getTag(), "tag-create");
 		}
 		return tagCreation;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TagCreationInfo basicGetTagCreation() {
 		return tagCreation;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTagCreation(TagCreationInfo newTagCreation) {
 		TagCreationInfo oldTagCreation = tagCreation;
 		tagCreation = newTagCreation;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PaletteInfosPackage.PALETTE_INFO__TAG_CREATION, oldTagCreation, tagCreation));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isExpert() {
 		if (expert == null){
 			String val = getTagTraitValueAsString(getId(), "expert");
 			return Boolean.valueOf(val).booleanValue();			
 		}
 		return expert.booleanValue();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean isHidden() {
 		if (hidden == null){
 			String val = getTagTraitValueAsString(getId(), "hidden");
 			return Boolean.valueOf(val).booleanValue();	
 		}
 		return hidden.booleanValue();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case PaletteInfosPackage.PALETTE_INFO__ID:
 				return getId();
 			case PaletteInfosPackage.PALETTE_INFO__TAG:
 				return getTag();
 			case PaletteInfosPackage.PALETTE_INFO__DISPLAY_LABEL:
 				return getDisplayLabel();
 			case PaletteInfosPackage.PALETTE_INFO__DESCRIPTION:
 				return getDescription();
 			case PaletteInfosPackage.PALETTE_INFO__EXPERT:
 				return getExpert();
 			case PaletteInfosPackage.PALETTE_INFO__HIDDEN:
 				return getHidden();
 			case PaletteInfosPackage.PALETTE_INFO__SMALL_ICON:
 				return getSmallIcon();
 			case PaletteInfosPackage.PALETTE_INFO__LARGE_ICON:
 				return getLargeIcon();
 			case PaletteInfosPackage.PALETTE_INFO__TAG_CREATION:
 				if (resolve) return getTagCreation();
 				return basicGetTagCreation();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case PaletteInfosPackage.PALETTE_INFO__ID:
 				setId((String)newValue);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__TAG:
 				setTag((String)newValue);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__DISPLAY_LABEL:
 				setDisplayLabel((String)newValue);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__DESCRIPTION:
 				setDescription((String)newValue);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__EXPERT:
 				setExpert((Boolean)newValue);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__HIDDEN:
 				setHidden((Boolean)newValue);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__SMALL_ICON:
 				setSmallIcon((String)newValue);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__LARGE_ICON:
 				setLargeIcon((String)newValue);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__TAG_CREATION:
 				setTagCreation((TagCreationInfo)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case PaletteInfosPackage.PALETTE_INFO__ID:
 				setId(ID_EDEFAULT);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__TAG:
 				setTag(TAG_EDEFAULT);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__DISPLAY_LABEL:
 				setDisplayLabel(DISPLAY_LABEL_EDEFAULT);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__DESCRIPTION:
 				setDescription(DESCRIPTION_EDEFAULT);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__EXPERT:
 				setExpert(EXPERT_EDEFAULT);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__HIDDEN:
 				setHidden(HIDDEN_EDEFAULT);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__SMALL_ICON:
 				setSmallIcon(SMALL_ICON_EDEFAULT);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__LARGE_ICON:
 				setLargeIcon(LARGE_ICON_EDEFAULT);
 				return;
 			case PaletteInfosPackage.PALETTE_INFO__TAG_CREATION:
 				setTagCreation((TagCreationInfo)null);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case PaletteInfosPackage.PALETTE_INFO__ID:
 				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
 			case PaletteInfosPackage.PALETTE_INFO__TAG:
 				return TAG_EDEFAULT == null ? tag != null : !TAG_EDEFAULT.equals(tag);
 			case PaletteInfosPackage.PALETTE_INFO__DISPLAY_LABEL:
 				return DISPLAY_LABEL_EDEFAULT == null ? displayLabel != null : !DISPLAY_LABEL_EDEFAULT.equals(displayLabel);
 			case PaletteInfosPackage.PALETTE_INFO__DESCRIPTION:
 				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
 			case PaletteInfosPackage.PALETTE_INFO__EXPERT:
 				return EXPERT_EDEFAULT == null ? expert != null : !EXPERT_EDEFAULT.equals(expert);
 			case PaletteInfosPackage.PALETTE_INFO__HIDDEN:
 				return HIDDEN_EDEFAULT == null ? hidden != null : !HIDDEN_EDEFAULT.equals(hidden);
 			case PaletteInfosPackage.PALETTE_INFO__SMALL_ICON:
 				return SMALL_ICON_EDEFAULT == null ? smallIcon != null : !SMALL_ICON_EDEFAULT.equals(smallIcon);
 			case PaletteInfosPackage.PALETTE_INFO__LARGE_ICON:
 				return LARGE_ICON_EDEFAULT == null ? largeIcon != null : !LARGE_ICON_EDEFAULT.equals(largeIcon);
 			case PaletteInfosPackage.PALETTE_INFO__TAG_CREATION:
 				return tagCreation != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (id: ");
 		result.append(id);
 		result.append(", tag: ");
 		result.append(tag);
 		result.append(", displayLabel: ");
 		result.append(displayLabel);
 		result.append(", description: ");
 		result.append(description);
 		result.append(", expert: ");
 		result.append(expert);
 		result.append(", hidden: ");
 		result.append(hidden);
 		result.append(", smallIcon: ");
 		result.append(smallIcon);
 		result.append(", largeIcon: ");
 		result.append(largeIcon);
 		result.append(')');
 		return result.toString();
 	}
 
 } //PaletteInfoImpl
