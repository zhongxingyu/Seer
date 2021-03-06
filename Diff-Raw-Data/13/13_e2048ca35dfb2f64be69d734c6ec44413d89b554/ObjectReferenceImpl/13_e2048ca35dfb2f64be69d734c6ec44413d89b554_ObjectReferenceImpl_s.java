 package org.eclipse.jdi.internal;
 
 /*
  * (c) Copyright IBM Corp. 2000, 2001.
  * All Rights Reserved.
  */
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.eclipse.jdi.internal.jdwp.JdwpCommandPacket;
 import org.eclipse.jdi.internal.jdwp.JdwpID;
 import org.eclipse.jdi.internal.jdwp.JdwpObjectID;
 import org.eclipse.jdi.internal.jdwp.JdwpReplyPacket;
 
 import com.sun.jdi.ClassNotLoadedException;
 import com.sun.jdi.Field;
 import com.sun.jdi.IncompatibleThreadStateException;
 import com.sun.jdi.InternalException;
 import com.sun.jdi.InvalidTypeException;
 import com.sun.jdi.InvocationException;
 import com.sun.jdi.Method;
 import com.sun.jdi.ObjectCollectedException;
 import com.sun.jdi.ObjectReference;
 import com.sun.jdi.PrimitiveType;
 import com.sun.jdi.PrimitiveValue;
 import com.sun.jdi.ReferenceType;
 import com.sun.jdi.ThreadReference;
 import com.sun.jdi.Type;
 import com.sun.jdi.Value;
 
 /**
  * this class implements the corresponding interfaces
  * declared by the JDI specification. See the com.sun.jdi package
  * for more information.
  *
  */
 public class ObjectReferenceImpl extends ValueImpl implements ObjectReference {
 	/** JDWP Tag. */
 	public static final byte tag = JdwpID.OBJECT_TAG;
 	
 	/** ObjectID of object that corresponds to this reference. */
 	private JdwpObjectID fObjectID;
 	
 	/**
 	 * Creates new ObjectReferenceImpl.
 	 */
 	public ObjectReferenceImpl(VirtualMachineImpl vmImpl, JdwpObjectID objectID) {
 		this("ObjectReference", vmImpl, objectID); //$NON-NLS-1$
 	}
 
 	/**
 	 * Creates new ObjectReferenceImpl.
 	 */
 	public ObjectReferenceImpl(String description, VirtualMachineImpl vmImpl, JdwpObjectID objectID) {
 		super(description, vmImpl);
 		fObjectID = objectID;
 	}
 	
 	/**
 	 * @returns tag.
 	 */
 	public byte getTag() {
 		return tag;
 	}
 	
 	/**
 	 * @return Returns Jdwp Object ID.
 	 */
 	public JdwpObjectID getObjectID() {
 		return fObjectID;
 	}
 
 	/** 
 	 * Prevents garbage collection for this object. 
 	 */
 	public void disableCollection() {
 		initJdwpRequest();
 		try {
 			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.OR_DISABLE_COLLECTION, this);
 			defaultReplyErrorHandler(replyPacket.errorCode());
 		} finally {
 			handledJdwpRequest();
 		}
 	}
 	
 	/** 
 	 * Permits garbage collection for this object. 
 	 */
 	public void enableCollection() {
 		initJdwpRequest();
 		try {
 			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.OR_ENABLE_COLLECTION, this);
 			defaultReplyErrorHandler(replyPacket.errorCode());
 		} finally {
 			handledJdwpRequest();
 		}
 	}
 	
 	/** 
 	 * Inner class used to return monitor info.
 	 */
 	private class MonitorInfo {
 		ThreadReferenceImpl owner;
 		int entryCount;
 		ArrayList waiters;
 	}
 		
 	/** 
 	 * @return Returns monitor info.
 	 */
 	private MonitorInfo monitorInfo() throws IncompatibleThreadStateException {
 		// Note that this information should not be cached.
 		initJdwpRequest();
 		try {
 			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.OR_MONITOR_INFO, this);
 			switch (replyPacket.errorCode()) {
 				case JdwpReplyPacket.INVALID_THREAD:
 					throw new IncompatibleThreadStateException();
 				case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
 					throw new IncompatibleThreadStateException();
 			}
 	
 			defaultReplyErrorHandler(replyPacket.errorCode());
 			
 			DataInputStream replyData = replyPacket.dataInStream();
 			MonitorInfo result = new MonitorInfo();
 			result.owner = ThreadReferenceImpl.read(this, replyData);
 			result.entryCount = readInt("entry count", replyData); //$NON-NLS-1$
 			int nrOfWaiters = readInt("nr of waiters", replyData); //$NON-NLS-1$
 			result.waiters = new ArrayList(nrOfWaiters);
 			for (int i = 0; i < nrOfWaiters; i++)
 				result.waiters.add(ThreadReferenceImpl.read(this, replyData));
 			return result;
 		} catch (IOException e) {
 			defaultIOExceptionHandler(e);
 			return null;
 		} finally {
 			handledJdwpRequest();
 		}
 	}
 	
 	/** 
 	 * @return Returns an ThreadReference for the thread, if any, which currently owns this object's monitor.
 	 */
 	public ThreadReference owningThread() throws IncompatibleThreadStateException {
 		return monitorInfo().owner;
 	}
 
 	/** 
 	 * @return Returns the number times this object's monitor has been entered by the current owning thread. 
 	 */
 	public int entryCount() throws IncompatibleThreadStateException {
 		return monitorInfo().entryCount;
 	}
 
 	/** 
 	 * @return Returns a List containing a ThreadReference for each thread currently waiting for this object's monitor.
 	 */
 	public List waitingThreads() throws IncompatibleThreadStateException {
 		return monitorInfo().waiters;
 	}
 				
 	/** 
 	 * @return Returns the value of a given instance or static field in this object. 
 	 */
 	public Value getValue(Field field) {
 		ArrayList list = new ArrayList(1);
 		list.add(field);
 		return (ValueImpl)getValues(list).get(field);
 	}
 		
 	/** 
 	 * @return Returns the value of multiple instance and/or static fields in this object. 
 	 */
 	public Map getValues(List allFields) {
 		// Note that this information should not be cached.
 		initJdwpRequest();
 		try {
 			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
 			DataOutputStream outData = new DataOutputStream(outBytes);
 			
 			/*
 			 * Distinguish static fields from non-static fields:
 			 * For static fields ReferencTypeImpl.getValues() must be used.
 			 */
 			Vector staticFields = new Vector();
 			Vector nonStaticFields = new Vector();
 	
 			// Separate static and non-static fields.
 			int allFieldsSize = allFields.size();
 			for (int i = 0; i < allFieldsSize; i++) {
 				FieldImpl field = (FieldImpl)allFields.get(i);
 				checkVM(field);
 		   		if (field.isStatic())
 		   			staticFields.add(field);
 		   		else
 		   			nonStaticFields.add(field);
 			}
 			
 			// First get values for the static fields.
 			Map resultMap  = referenceType().getValues(staticFields);
 			
 			// Then get the values for the non-static fields.
 			int nonStaticFieldsSize = nonStaticFields.size();
 			write(this, outData);
 			writeInt(nonStaticFieldsSize, "size", outData); //$NON-NLS-1$
 			for (int i = 0; i < nonStaticFieldsSize; i++) {
 				FieldImpl field = (FieldImpl)nonStaticFields.get(i);
 				field.write(this, outData);
 			}
 	
 			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.OR_GET_VALUES, outBytes);
 			defaultReplyErrorHandler(replyPacket.errorCode());
 			
 			DataInputStream replyData = replyPacket.dataInStream();
 			int nrOfElements = readInt("elements", replyData); //$NON-NLS-1$
 			if (nrOfElements != nonStaticFieldsSize) 
 				throw new InternalError(JDIMessages.getString("ObjectReferenceImpl.Retrieved_a_different_number_of_values_from_the_VM_than_requested_1")); //$NON-NLS-1$
 				
 			for (int i = 0; i < nrOfElements; i++) {
 				resultMap.put(nonStaticFields.get(i), ValueImpl.readWithTag(this, replyData));
 			}
 			return resultMap;
 		} catch (IOException e) {
 			defaultIOExceptionHandler(e);
 			return null;
 		} finally {
 			handledJdwpRequest();
 		}
 	}
 	
 	/** 
 	 * @return Returns the hash code value.
 	 */
 	public int hashCode() {
 		return fObjectID.hashCode();
 	}
 	
 	/**
 	 * @return Returns true if two mirrors refer to the same entity in the target VM.
 	 * @see java.lang.Object#equals(Object)
 	 */
 	public boolean equals(Object object) {
 
 		return object != null
 			&& object.getClass().equals(this.getClass())
 			&& fObjectID.equals(((ObjectReferenceImpl)object).fObjectID)
 			&& virtualMachine().equals(((MirrorImpl)object).virtualMachine());
 	}
 	
 	/**
 	 * @return Returns Jdwp version of given options.
 	 */
 	private int optionsToJdwpOptions(int options) {
 		int jdwpOptions = 0;
    		if ((options & INVOKE_SINGLE_THREADED) != 0) {
    			jdwpOptions |= MethodImpl.INVOKE_SINGLE_THREADED_JDWP;
    		}
    		if ((options & INVOKE_NONVIRTUAL) != 0) {
    			jdwpOptions |= MethodImpl.INVOKE_NONVIRTUAL_JDWP;
    		}
    		return jdwpOptions;
 	}
 	
 	/**
 	 * Invokes the specified static Method in the target VM.
 	 * @return Returns a Value mirror of the invoked method's return value.
 	 */
 	public Value invokeMethod(ThreadReference thread, Method method, List arguments, int options) throws InvalidTypeException, ClassNotLoadedException, IncompatibleThreadStateException, InvocationException {
 	   	checkVM(thread);
 		checkVM(method);
 		ThreadReferenceImpl threadImpl = (ThreadReferenceImpl)thread;
 		MethodImpl methodImpl = (MethodImpl)method;
 		
 		// Perform some checks for IllegalArgumentException.
 		if (!referenceType().allMethods().contains(method))
 			throw new IllegalArgumentException(JDIMessages.getString("ObjectReferenceImpl.Class_does_not_contain_given_method_2")); //$NON-NLS-1$
 		if (method.argumentTypeNames().size() != arguments.size())
 			throw new IllegalArgumentException(JDIMessages.getString("ObjectReferenceImpl.Number_of_arguments_doesn__t_match_3")); //$NON-NLS-1$
 		if (method.isConstructor() || method.isStaticInitializer())
 			throw new IllegalArgumentException(JDIMessages.getString("ObjectReferenceImpl.Method_is_constructor_or_intitializer_4")); //$NON-NLS-1$
 		if ((options & INVOKE_NONVIRTUAL) != 0 && method.isAbstract())
 			throw new IllegalArgumentException(JDIMessages.getString("ObjectReferenceImpl.Method_is_abstract_and_can_therefore_not_be_invoked_nonvirtual_5")); //$NON-NLS-1$
 
 		List argumentTypes= method.argumentTypes();
 		Type argumentType;
 		String typeSignature;
 		Value argument;
 		PrimitiveValue primitiveValue;
 		for (int i= 0, numArgs= arguments.size(); i < numArgs; i++) {
 			argumentType= (Type)argumentTypes.get(i);
 			argument= (Value)arguments.get(i);
 			typeSignature= argumentType.signature();
 			if (argumentType instanceof PrimitiveType && !typeSignature.equals(argument.type().signature())) {
 				// Convert primitive value parameters to the type that matches the method signature
 				primitiveValue= (PrimitiveValue)argument;
 				switch (typeSignature.charAt(0)) {
 				case 'B':
 					arguments.set(i, new ByteValueImpl(virtualMachineImpl(), new Byte(primitiveValue.byteValue())));
 					break;
 				case 'C':
 					arguments.set(i, new CharValueImpl(virtualMachineImpl(), new Character(primitiveValue.charValue())));
 					break;
 				case 'S':
 					arguments.set(i, new ShortValueImpl(virtualMachineImpl(), new Short(primitiveValue.shortValue())));
 					break;
 				case 'I':
 					arguments.set(i, new IntegerValueImpl(virtualMachineImpl(), new Integer(primitiveValue.intValue())));
 					break;
 				case 'J':
 					arguments.set(i, new LongValueImpl(virtualMachineImpl(), new Long(primitiveValue.longValue())));
 					break;
 				case 'F':
 					arguments.set(i, new FloatValueImpl(virtualMachineImpl(), new Float(primitiveValue.floatValue())));
 					break;
 				case 'D':
 					arguments.set(i, new DoubleValueImpl(virtualMachineImpl(), new Double(primitiveValue.doubleValue())));
 					break;
 				}
 			}
 		}
 
 		initJdwpRequest();
 		try {
 			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
 			DataOutputStream outData = new DataOutputStream(outBytes);
 			write(this, outData);
 			threadImpl.write(this, outData);
 			((ReferenceTypeImpl)referenceType()).write(this, outData);
 			methodImpl.write(this, outData);
 			
 			writeInt(arguments.size(), "size", outData); //$NON-NLS-1$
 			Iterator iter = arguments.iterator();
 			while(iter.hasNext()) {
 				ValueImpl elt = (ValueImpl)iter.next();
 				if (elt != null) {
 					checkVM(elt);
 					elt.writeWithTag(this, outData);
 				} else {
 					ValueImpl.writeNullWithTag(this, outData);
 				}
 			}
 			
 			writeInt(optionsToJdwpOptions(options),"options", MethodImpl.invokeOptionsVector(), outData); //$NON-NLS-1$
 			
 			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.OR_INVOKE_METHOD, outBytes);
 			switch (replyPacket.errorCode()) {
 				case JdwpReplyPacket.TYPE_MISMATCH:
 					throw new InvalidTypeException();
 				case JdwpReplyPacket.INVALID_CLASS:
 					throw new ClassNotLoadedException(JDIMessages.getString("ObjectReferenceImpl.One_of_the_arguments_of_ObjectReference.invokeMethod()_6")); //$NON-NLS-1$
 				case JdwpReplyPacket.INVALID_THREAD:
 					throw new IncompatibleThreadStateException();
 				case JdwpReplyPacket.THREAD_NOT_SUSPENDED:
 					throw new IncompatibleThreadStateException();
 				case JdwpReplyPacket.INVALID_TYPESTATE:
 					throw new IncompatibleThreadStateException();
 			}
 			defaultReplyErrorHandler(replyPacket.errorCode());
 			DataInputStream replyData = replyPacket.dataInStream();
 			ValueImpl value = ValueImpl.readWithTag(this, replyData);
 			ObjectReferenceImpl exception = ObjectReferenceImpl.readObjectRefWithTag(this, replyData);
 			if (exception != null)
 				throw new InvocationException(exception);
 			return value;
 		} catch (IOException e) {
 			defaultIOExceptionHandler(e);
 			return null;
 		} finally {
 			handledJdwpRequest();
 		}
 	}
  
 	
 	/**
 	 * @return Returns if this object has been garbage collected in the target VM.
 	 */
 	public boolean isCollected() {
 		// Note that this information should not be cached.
 		initJdwpRequest();
 		try {
 			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.OR_IS_COLLECTED, this);
 			switch (replyPacket.errorCode()) {
 				case JdwpReplyPacket.INVALID_OBJECT:
 					return true;
 				case JdwpReplyPacket.NOT_IMPLEMENTED:
 					// Workaround for problem in J2ME WTK (wireless toolkit)
 					// @see Bug 12966
 					return false;
 				default:
 					defaultReplyErrorHandler(replyPacket.errorCode());
 					break;
 			};
 			DataInputStream replyData = replyPacket.dataInStream();
 			boolean result = readBoolean("is collected", replyData); //$NON-NLS-1$
 			return result;
 		} catch (IOException e) {
 			defaultIOExceptionHandler(e);
 			return false;
 		} finally {
 			handledJdwpRequest();
 		}
 	}
 
 	
 	/**
 	 * @return Returns the ReferenceType that mirrors the type of this object.
 	 */
 	public ReferenceType referenceType() {
 		initJdwpRequest();
 		try {
 			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.OR_REFERENCE_TYPE, this);
 			defaultReplyErrorHandler(replyPacket.errorCode());
 			DataInputStream replyData = replyPacket.dataInStream();
			return ReferenceTypeImpl.readWithTypeTag(this, replyData);
 		} catch (IOException e) {
 			defaultIOExceptionHandler(e);
 			return null;
 		} finally {
 			handledJdwpRequest();
 		}
 	}
 	
 	/**
 	 * @return Returns the Type that mirrors the type of this object.
 	 */
 	public Type type() {
 		return referenceType();
 	}
 	
 	/**
 	 * Sets the value of a given instance or static field in this object. 
 	 */
 	public void setValue(Field field, Value value) throws InvalidTypeException, ClassNotLoadedException {
 		// Note that this information should not be cached.
 		initJdwpRequest();
 		try {
 			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
 			DataOutputStream outData = new DataOutputStream(outBytes);
 			write(this, outData);
 			writeInt(1, "size", outData);	// We only set one field //$NON-NLS-1$
 			checkVM(field);
 			((FieldImpl)field).write(this, outData);
 
 			if (value != null) {
 				checkVM(value);
 				((ValueImpl)value).write(this, outData);
 			} else {
 				ValueImpl.writeNull(this, outData);
 			}
 	
 			JdwpReplyPacket replyPacket = requestVM(JdwpCommandPacket.OR_SET_VALUES, outBytes);
 			switch (replyPacket.errorCode()) {
 				case JdwpReplyPacket.TYPE_MISMATCH:
 					throw new InvalidTypeException();
 				case JdwpReplyPacket.INVALID_CLASS:
 					throw new ClassNotLoadedException(referenceType().name());
 			}
 			defaultReplyErrorHandler(replyPacket.errorCode());
 		} catch (IOException e) {
 			defaultIOExceptionHandler(e);
 		} finally {
 			handledJdwpRequest();
 		}
 	}
 	
 	/**
 	 * @return Returns a unique identifier for this ObjectReference. 
 	 */
 	public long uniqueID() {
 		if (isCollected())
 			throw new ObjectCollectedException();
 		return fObjectID.value();
 	}
 		
 	/**
 	 * @return Returns string with value of ID.
 	 */
 	public String idString() {
 		return "(id=" + fObjectID + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * @return Returns description of Mirror object.
 	 */
 	public String toString() {
 		try {
 			return type().toString() + " " + idString(); //$NON-NLS-1$
 		} catch (ObjectCollectedException e) {
 			return JDIMessages.getString("ObjectReferenceImpl.(Garbage_Collected)_ObjectReference__8") + idString(); //$NON-NLS-1$
 		} catch (Exception e) {
 			return fDescription;
 		}
 	}
 
 	/**
 	 * @return Reads JDWP representation and returns new instance.
 	 */
 	public static ObjectReferenceImpl readObjectRefWithoutTag(MirrorImpl target, DataInputStream in) throws IOException {
 		VirtualMachineImpl vmImpl = target.virtualMachineImpl();
 		JdwpObjectID ID = new JdwpObjectID(vmImpl);
 		ID.read(in);
 		if (target.fVerboseWriter != null)
 			target.fVerboseWriter.println("objectReference", ID.value()); //$NON-NLS-1$
 
 		if (ID.isNull())
 			return null;
 			
 		ObjectReferenceImpl mirror = new ObjectReferenceImpl(vmImpl, ID);
 		return mirror;
 	}
 	
 	/**
 	 * @return Reads JDWP representation and returns new instance.
 	 */
 	public static ObjectReferenceImpl readObjectRefWithTag(MirrorImpl target, DataInputStream in) throws IOException {
 		byte tag = target.readByte("object tag", JdwpID.tagMap(), in); //$NON-NLS-1$
 		switch (tag) {
 	   		case 0:
 				return null;
 			case ObjectReferenceImpl.tag:
 				return ObjectReferenceImpl.readObjectRefWithoutTag(target, in);
 			case ArrayReferenceImpl.tag:
 				return ArrayReferenceImpl.read(target, in);
 			case ClassLoaderReferenceImpl.tag:
 				return ClassLoaderReferenceImpl.read(target, in);
 			case ClassObjectReferenceImpl.tag:
 				return ClassObjectReferenceImpl.read(target, in);
 			case StringReferenceImpl.tag:
 				return StringReferenceImpl.read(target, in);
 			case ThreadGroupReferenceImpl.tag:
 				return ThreadGroupReferenceImpl.read(target, in);
 			case ThreadReferenceImpl.tag:
 				return ThreadReferenceImpl.read(target, in);
    	   	}
 		throw new InternalException(JDIMessages.getString("ObjectReferenceImpl.Invalid_ObjectID_tag_encountered___9") + tag); //$NON-NLS-1$
 	}
 
 	/**
 	 * Writes JDWP representation without tag.
 	 */
 	public void write(MirrorImpl target, DataOutputStream out) throws IOException {
 		fObjectID.write(out);
 		if (target.fVerboseWriter != null)
 			target.fVerboseWriter.println("objectReference", fObjectID.value()); //$NON-NLS-1$
 	}
 }
