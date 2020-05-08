 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package edu.teco.dnd.graphiti.model.impl;
 
 import java.io.Serializable;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.Map;
 import java.util.UUID;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 import edu.teco.dnd.blocks.AssignmentException;
 import edu.teco.dnd.blocks.FunctionBlock;
 import edu.teco.dnd.blocks.Input;
 import edu.teco.dnd.blocks.InvalidFunctionBlockException;
 import edu.teco.dnd.blocks.Output;
 import edu.teco.dnd.blocks.RetrievementException;
 import edu.teco.dnd.graphiti.model.FunctionBlockModel;
 import edu.teco.dnd.graphiti.model.InputModel;
 import edu.teco.dnd.graphiti.model.ModelPackage;
 import edu.teco.dnd.graphiti.model.OptionModel;
 import edu.teco.dnd.graphiti.model.OutputModel;
 import edu.teco.dnd.module.RemoteConnectionTarget;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>Function Block Model</b></em>'. <!--
  * end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link edu.teco.dnd.graphiti.model.impl.FunctionBlockModelImpl#getType <em>Type</em>}</li>
  *   <li>{@link edu.teco.dnd.graphiti.model.impl.FunctionBlockModelImpl#getInputs <em>Inputs</em>}</li>
  *   <li>{@link edu.teco.dnd.graphiti.model.impl.FunctionBlockModelImpl#getOutputs <em>Outputs</em>}</li>
  *   <li>{@link edu.teco.dnd.graphiti.model.impl.FunctionBlockModelImpl#getOptions <em>Options</em>}</li>
  *   <li>{@link edu.teco.dnd.graphiti.model.impl.FunctionBlockModelImpl#getID <em>ID</em>}</li>
  *   <li>{@link edu.teco.dnd.graphiti.model.impl.FunctionBlockModelImpl#getPosition <em>Position</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class FunctionBlockModelImpl extends EObjectImpl implements FunctionBlockModel {
 	/**
 	 * The default value of the '{@link #getType() <em>Type</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @see #getType()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String TYPE_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @see #getType()
 	 * @generated
 	 * @ordered
 	 */
 	protected String type = TYPE_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getInputs() <em>Inputs</em>}' containment reference list.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @see #getInputs()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList inputs;
 
 	/**
 	 * The cached value of the '{@link #getOutputs() <em>Outputs</em>}' containment reference list.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @see #getOutputs()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList outputs;
 
 	/**
 	 * The cached value of the '{@link #getOptions() <em>Options</em>}' containment reference list.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @see #getOptions()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList options;
 
 	/**
 	 * The default value of the '{@link #getID() <em>ID</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getID()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final UUID ID_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getID() <em>ID</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getID()
 	 * @generated
 	 * @ordered
 	 */
 	protected UUID iD = ID_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getPosition() <em>Position</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getPosition()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String POSITION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getPosition() <em>Position</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getPosition()
 	 * @generated
 	 * @ordered
 	 */
 	protected String position = POSITION_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected FunctionBlockModelImpl() {
 		super();
 	}
 
 	protected FunctionBlockModelImpl(Class<? extends FunctionBlock> cls) {
 		super();
 		setID(UUID.randomUUID());
 		if (cls != null) {
 			setType(cls.getName());
 			for (Field field : FunctionBlock.getInputs(cls)) {
 				InputModel input = ModelFactoryImpl.eINSTANCE.createInputModel();
 				input.setFunctionBlock(this);
 				input.setName(field.getName());
 				input.setQueued(field.getAnnotation(Input.class).value());
 				input.setType(field.getType().getName());
 			}
 			for (Field field : FunctionBlock.getOutputs(cls)) {
 				OutputModel output = ModelFactoryImpl.eINSTANCE.createOutputModel();
 				output.setFunctionBlock(this);
 				output.setName(field.getName());
 				final Type genericType = field.getGenericType();
 				if (genericType instanceof ParameterizedType) {
 					final Type[] actualTypes = ((ParameterizedType) genericType).getActualTypeArguments();
 					if (actualTypes != null && actualTypes.length == 1 && actualTypes[0] instanceof Class<?>
 							&& Serializable.class.isAssignableFrom((Class<?>) actualTypes[0])) {
 						output.setType(((Class<?>) actualTypes[0]).getName());
 					}
 				}
 			}
 			for (Field field : FunctionBlock.getOptions(cls)) {
 				OptionModel option = ModelFactoryImpl.eINSTANCE.createOptionModel();
 				option.setName(field.getName());
 				option.setType(field.getType().getName());
 				option.setFunctionBlock(this);
 			}
 			FunctionBlock block = null;
 			try {
 				Constructor<? extends FunctionBlock> constructor = cls.getConstructor(String.class);
 				block = constructor.newInstance("id");
 			} catch (InstantiationException e) {
 			} catch (IllegalAccessException e) {
 			} catch (IllegalArgumentException e) {
 			} catch (InvocationTargetException e) {
 			} catch (NoSuchMethodException e) {
 			} catch (SecurityException e) {
 			}
 			if (block != null) {
 				for (Object optionObject : getOptions()) {
 					OptionModel option = (OptionModel) optionObject;
 					Serializable value = null;
 					try {
 						value = block.getOption(option.getName());
 					} catch (RetrievementException e) {
 						continue;
 					}
 					option.setValue(value);
 				}
 			}
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return ModelPackage.Literals.FUNCTION_BLOCK_MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getType() {
 		return type;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setType(String newType) {
 		String oldType = type;
 		type = newType;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.FUNCTION_BLOCK_MODEL__TYPE, oldType, type));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getInputs() {
 		if (inputs == null) {
 			inputs = new EObjectContainmentWithInverseEList(InputModel.class, this, ModelPackage.FUNCTION_BLOCK_MODEL__INPUTS, ModelPackage.INPUT_MODEL__FUNCTION_BLOCK);
 		}
 		return inputs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getOutputs() {
 		if (outputs == null) {
 			outputs = new EObjectContainmentWithInverseEList(OutputModel.class, this, ModelPackage.FUNCTION_BLOCK_MODEL__OUTPUTS, ModelPackage.OUTPUT_MODEL__FUNCTION_BLOCK);
 		}
 		return outputs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getOptions() {
 		if (options == null) {
 			options = new EObjectContainmentWithInverseEList(OptionModel.class, this, ModelPackage.FUNCTION_BLOCK_MODEL__OPTIONS, ModelPackage.OPTION_MODEL__FUNCTION_BLOCK);
 		}
 		return options;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public UUID getID() {
 		return iD;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setID(UUID newID) {
 		UUID oldID = iD;
 		iD = newID;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.FUNCTION_BLOCK_MODEL__ID, oldID, iD));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getPosition() {
 		return position;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPosition(String newPosition) {
 		String oldPosition = position;
 		position = newPosition;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.FUNCTION_BLOCK_MODEL__POSITION, oldPosition, position));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case ModelPackage.FUNCTION_BLOCK_MODEL__INPUTS:
 				return ((InternalEList)getInputs()).basicAdd(otherEnd, msgs);
 			case ModelPackage.FUNCTION_BLOCK_MODEL__OUTPUTS:
 				return ((InternalEList)getOutputs()).basicAdd(otherEnd, msgs);
 			case ModelPackage.FUNCTION_BLOCK_MODEL__OPTIONS:
 				return ((InternalEList)getOptions()).basicAdd(otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case ModelPackage.FUNCTION_BLOCK_MODEL__INPUTS:
 				return ((InternalEList)getInputs()).basicRemove(otherEnd, msgs);
 			case ModelPackage.FUNCTION_BLOCK_MODEL__OUTPUTS:
 				return ((InternalEList)getOutputs()).basicRemove(otherEnd, msgs);
 			case ModelPackage.FUNCTION_BLOCK_MODEL__OPTIONS:
 				return ((InternalEList)getOptions()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case ModelPackage.FUNCTION_BLOCK_MODEL__TYPE:
 				return getType();
 			case ModelPackage.FUNCTION_BLOCK_MODEL__INPUTS:
 				return getInputs();
 			case ModelPackage.FUNCTION_BLOCK_MODEL__OUTPUTS:
 				return getOutputs();
 			case ModelPackage.FUNCTION_BLOCK_MODEL__OPTIONS:
 				return getOptions();
 			case ModelPackage.FUNCTION_BLOCK_MODEL__ID:
 				return getID();
 			case ModelPackage.FUNCTION_BLOCK_MODEL__POSITION:
 				return getPosition();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case ModelPackage.FUNCTION_BLOCK_MODEL__TYPE:
 				setType((String)newValue);
 				return;
 			case ModelPackage.FUNCTION_BLOCK_MODEL__INPUTS:
 				getInputs().clear();
 				getInputs().addAll((Collection)newValue);
 				return;
 			case ModelPackage.FUNCTION_BLOCK_MODEL__OUTPUTS:
 				getOutputs().clear();
 				getOutputs().addAll((Collection)newValue);
 				return;
 			case ModelPackage.FUNCTION_BLOCK_MODEL__OPTIONS:
 				getOptions().clear();
 				getOptions().addAll((Collection)newValue);
 				return;
 			case ModelPackage.FUNCTION_BLOCK_MODEL__ID:
 				setID((UUID)newValue);
 				return;
 			case ModelPackage.FUNCTION_BLOCK_MODEL__POSITION:
 				setPosition((String)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case ModelPackage.FUNCTION_BLOCK_MODEL__TYPE:
 				setType(TYPE_EDEFAULT);
 				return;
 			case ModelPackage.FUNCTION_BLOCK_MODEL__INPUTS:
 				getInputs().clear();
 				return;
 			case ModelPackage.FUNCTION_BLOCK_MODEL__OUTPUTS:
 				getOutputs().clear();
 				return;
 			case ModelPackage.FUNCTION_BLOCK_MODEL__OPTIONS:
 				getOptions().clear();
 				return;
 			case ModelPackage.FUNCTION_BLOCK_MODEL__ID:
 				setID(ID_EDEFAULT);
 				return;
 			case ModelPackage.FUNCTION_BLOCK_MODEL__POSITION:
 				setPosition(POSITION_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case ModelPackage.FUNCTION_BLOCK_MODEL__TYPE:
 				return TYPE_EDEFAULT == null ? type != null : !TYPE_EDEFAULT.equals(type);
 			case ModelPackage.FUNCTION_BLOCK_MODEL__INPUTS:
 				return inputs != null && !inputs.isEmpty();
 			case ModelPackage.FUNCTION_BLOCK_MODEL__OUTPUTS:
 				return outputs != null && !outputs.isEmpty();
 			case ModelPackage.FUNCTION_BLOCK_MODEL__OPTIONS:
 				return options != null && !options.isEmpty();
 			case ModelPackage.FUNCTION_BLOCK_MODEL__ID:
 				return ID_EDEFAULT == null ? iD != null : !ID_EDEFAULT.equals(iD);
 			case ModelPackage.FUNCTION_BLOCK_MODEL__POSITION:
 				return POSITION_EDEFAULT == null ? position != null : !POSITION_EDEFAULT.equals(position);
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (type: ");
 		result.append(type);
 		result.append(", iD: ");
 		result.append(iD);
 		result.append(", position: ");
 		result.append(position);
 		result.append(')');
 		return result.toString();
 	}
 
 	@Override
 	public String getTypeName() {
 		if (type != null && !type.isEmpty()) {
 			return type.substring(type.lastIndexOf('.') + 1);
 		}
 		return type;
 	}
 
 	@Override
 	public boolean isSensor() {
 		return getInputs().isEmpty() && !getOutputs().isEmpty();
 	}
 
 	@Override
 	public boolean isActor() {
 		return !getInputs().isEmpty() && getOutputs().isEmpty();
 	}
 
 	@Override
 	public FunctionBlock createBlock() throws InvalidFunctionBlockException {
 		return createBlock(getClass().getClassLoader());
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public FunctionBlock createBlock(final ClassLoader cl) throws InvalidFunctionBlockException {
 		if (cl == null) {
 			throw new IllegalArgumentException("cl must not be null");
 		}
 		Class<? extends FunctionBlock> cls = null;
 		try {
 			cls = (Class<? extends FunctionBlock>) cl.loadClass(type);
 		} catch (ClassNotFoundException e) {
 			throw new InvalidFunctionBlockException("Could not get block class", e);
 		} catch (ClassCastException e) {
 			throw new InvalidFunctionBlockException("Could not get block class", e);
 		}
 		Constructor<? extends FunctionBlock> constructor = null;
 		try {
			constructor = cls.getConstructor(UUID.class);
 		} catch (NoSuchMethodException e) {
 			throw new InvalidFunctionBlockException("Could not find constructor", e);
 		} catch (SecurityException e) {
 			throw new InvalidFunctionBlockException("Could not find constructor", e);
 		}
 		FunctionBlock block = null;
 		try {
 			block = constructor.newInstance(getID());
 		} catch (InstantiationException e) {
 			throw new InvalidFunctionBlockException("Could not instantiate FunctionBlock", e);
 		} catch (IllegalAccessException e) {
 			throw new InvalidFunctionBlockException("Could not instantiate FunctionBlock", e);
 		} catch (IllegalArgumentException e) {
 			throw new InvalidFunctionBlockException("Could not instantiate FunctionBlock", e);
 		} catch (InvocationTargetException e) {
 			throw new InvalidFunctionBlockException("Could not instantiate FunctionBlock", e);
 		}
 		for (Object optionObject : getOptions()) {
 			final OptionModel option = (OptionModel) optionObject;
 			try {
 				block.setOption(option.getName(), option.getValue());
 			} catch (AssignmentException e) {
 				throw new InvalidFunctionBlockException("Could not set option", e);
 			}
 		}
 		Map<String, Output<?>> outputs = block.getOutputs();
 		for (Object outputModelObject : getOutputs()) {
 			final OutputModel outputModel = (OutputModel) outputModelObject;
 			Output<?> output = outputs.get(outputModel.getName());
 			for (Object inputObject : outputModel.getInputs()) {
 				final InputModel input = (InputModel) inputObject;
 				UUID uuid = input.getFunctionBlock().getID();
 				output.addConnection(new RemoteConnectionTarget(outputModel.getName(),
 						uuid, input.getName()));
 			}
 		}
 		block.setPosition(getPosition());
 		return block;
 	}
 } // FunctionBlockModelImpl
