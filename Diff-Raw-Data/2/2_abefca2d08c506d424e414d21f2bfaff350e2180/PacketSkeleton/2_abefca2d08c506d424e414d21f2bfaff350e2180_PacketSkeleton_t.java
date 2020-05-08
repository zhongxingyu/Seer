 
 package com.zanoccio.packetkit.headers;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import com.zanoccio.packetkit.IP4Address;
 import com.zanoccio.packetkit.MACAddress;
 import com.zanoccio.packetkit.PacketFragment;
 import com.zanoccio.packetkit.PacketUtilities;
 import com.zanoccio.packetkit.exceptions.CannotPopulateFromNetworkInterfaceException;
 import com.zanoccio.packetkit.exceptions.InvalidFieldException;
 import com.zanoccio.packetkit.exceptions.NotDeclareDynamicException;
 import com.zanoccio.packetkit.exceptions.PacketKitException;
 import com.zanoccio.packetkit.exceptions.SlotTakenException;
 import com.zanoccio.packetkit.headers.annotations.Checksum;
 import com.zanoccio.packetkit.headers.annotations.Data;
 import com.zanoccio.packetkit.headers.annotations.DynamicSize;
 import com.zanoccio.packetkit.headers.annotations.FixedSize;
 import com.zanoccio.packetkit.headers.annotations.FromNetworkInterface;
 import com.zanoccio.packetkit.headers.annotations.StaticFragment;
 
 /**
  * Represents a cached form of a
  * 
  * @author wiktor
  * 
  */
 public class PacketSkeleton {
 
 	public static final int DEFAULT_LOGICAL_SLOT = 0;
 	public static final int DATA_LOGICAL_SLOT = 100;
 	public static final int CHECKSUM_LOGICAL_SLOT = 200;
 
 	public static HashMap<Class<? extends Object>, FragmentSlotType> VALIDPRIMITIVES;
 	static {
 		VALIDPRIMITIVES = new HashMap<Class<? extends Object>, FragmentSlotType>();
 		VALIDPRIMITIVES.put(Integer.TYPE, FragmentSlotType.INT);
 		VALIDPRIMITIVES.put(Short.TYPE, FragmentSlotType.SHORT);
 	}
 
 	private String name;
 	private Class<? extends PacketHeader> klass;
 	private boolean isfixedsize;
 	private Integer size;
 
 	// private ArrayList<Field> fieldsfromnetworkinterface;
 	private ArrayList<FragmentSlot> logicalslotlist;
 	private ArrayList<FragmentSlot> physicalslotlist;
 	private ArrayList<FragmentSlot> dynamicslots;
 
 
 	public PacketSkeleton(Class<? extends PacketHeader> klass) throws PacketKitException {
 		construct(klass);
 	}
 
 
 	@SuppressWarnings("boxing")
 	public void construct(Class<? extends PacketHeader> klass) throws PacketKitException {
 		int slotindex = DEFAULT_LOGICAL_SLOT;
 
 		isfixedsize = true;
 		// fieldsfromnetworkinterface = new ArrayList<Field>();
 
 		name = klass.getName();
 		this.klass = klass;
 
 		Field[] fields = klass.getDeclaredFields();
 
 		//
 		// 1) Sort by Physical Slot
 		//
 
 		HashSet<Integer> physicalslots = new HashSet<Integer>();
 		physicalslotlist = new ArrayList<FragmentSlot>();
 		logicalslotlist = new ArrayList<FragmentSlot>();
 
 		for (Field field : fields) {
 			// pull the StaticFragment annotation for this field
 			StaticFragment annotation = field.getAnnotation(StaticFragment.class);
 
 			// no StaticFragment annotation, so keep going
 			if (annotation == null)
 				continue;
 
 			int fragmentslot;
 
 			// check whether a physical slot was specified
 			if (annotation.slot() != StaticFragment.DEFAULT_SIZE)
 				// use it
 				fragmentslot = annotation.slot();
 			else
 				fragmentslot = slotindex;
 
 			// increment the physical slot index
 			slotindex++;
 
 			// check that the physical slot hasn't been taken
 			if (physicalslots.contains(slotindex))
 				throw new SlotTakenException(field);
 
 			// reserve the physical slot
 			physicalslots.add(slotindex);
 
 			// create the fragment
 			FragmentSlot fragment = new FragmentSlot();
 			fragment.physicalslot = fragmentslot;
 			fragment.field = field;
 
 			// add the fragment
 			physicalslotlist.add(fragment);
 		}
 
 		Collections.sort(physicalslotlist, new PhysicalSlotComparator());
 
 		//
 		// 2) Compute size and offset
 		//
 		int offset = 0;
 		for (FragmentSlot fragment : physicalslotlist) {
 			Field field = fragment.field;
 			StaticFragment annotation = field.getAnnotation(StaticFragment.class);
 
 			Class<? extends Object> fieldtype = field.getType();
 
 			// check whether this field is autowired
 			if (field.isAnnotationPresent(FromNetworkInterface.class)) {
 				if (fieldtype == IP4Address.class)
 					fragment.type = FragmentSlotType.IP4ADDRESS;
 				else if (fieldtype == MACAddress.class)
 					fragment.type = FragmentSlotType.MACADDRESS;
 				else
 					throw new CannotPopulateFromNetworkInterfaceException(field, fieldtype);
 			}
 
 			// check for a Checksum or Data annotation
 			if (field.isAnnotationPresent(Checksum.class)) {
 				// Checksum checksum = field.getAnnotation(Checksum.class);
 				// if (fieldtype != Integer.TYPE)
 				// throw new InvalidFieldException(field,
 				// " must be an int to store a checksum");
 
 				fragment.type = FragmentSlotType.CHECKSUM;
 			}
 
 			// check for a Data annotation
 			if (field.isAnnotationPresent(Data.class)) {
 				fragment.type = FragmentSlotType.DATA;
 			}
 
 			// if we don't already know the type of the field
 			if (fragment.type == null)
 				// verify that the field's type is compatible with the framework
 				if (!VALIDPRIMITIVES.containsKey(fieldtype)) {
 					// and the type is being valid
 					if (isValidType(fieldtype))
 						fragment.type = FragmentSlotType.PACKETFRAGMENT;
 				} else {
 					if (fieldtype == Integer.TYPE)
 						fragment.type = FragmentSlotType.INT;
 					else if (fieldtype == Short.TYPE)
 						fragment.type = FragmentSlotType.SHORT;
 					else
 						throw new InvalidFieldException(field, "Unsupported field primitive");
 				}
 
 			if (fragment.type == null)
 				throw new InvalidFieldException(field, "Cannot determine type of packet fragment");
 
 			fragment.type = fragment.type;
 
 			// ensure that the field is efficiently accessible
 			if (!isFieldAccessible(field))
 				throw new InvalidFieldException(field, "must be public or packaged scoped");
 
 			// compute the size of this fragment
 			if (annotation.size() == StaticFragment.DEFAULT_SIZE) {
 				// check whether the field has a fixed size defined
 				if (fieldtype.isAnnotationPresent(FixedSize.class)) {
 					FixedSize sizeanno = fieldtype.getAnnotation(FixedSize.class);
 					fragment.size = sizeanno.size();
 				} else
 					// the fragment is dynamic. and thus so is the packet
 					fragment.size = FixedSize.DYNAMIC;
 			} else {
 				fragment.size = annotation.size();
 			}
 
 			// set the offset for this fragment
 			fragment.offset = offset;
 
 			// if this fragment has a size, increase the offset
 			if (fragment.size != FixedSize.DYNAMIC)
 				offset += fragment.size;
 
 			fragment.fixed = (fragment.size != FixedSize.DYNAMIC);
 
 			//
 			// find the method for reconstructing the fragment from bytes
 			//
 
 			fragment.constructor = getConstructorMethod(field, fragment);
 
 			if (fragment.fixed == false)
 				fragment.sizemethod = getSizeMethod(field, fragment);
 
 			//
 			// Compute Logical Slot
 			//
 			fragment.logicalslot = fragment.physicalslot;
 			switch (fragment.type) {
 			case CHECKSUM:
 				fragment.logicalslot += CHECKSUM_LOGICAL_SLOT;
 				break;
 
 			case DATA:
 				fragment.logicalslot += DATA_LOGICAL_SLOT;
 				break;
 
 			default:
 				// the logical slot is just the original slot
 				break;
 			}
 			logicalslotlist.add(fragment);
 		}
 
 		//
 		// 3) Finally, sort by logical slot
 		//
 
 		Collections.sort(logicalslotlist, new LogicalSlotComparator());
 
 		// find dynamic slots
 		dynamicslots = new ArrayList<FragmentSlot>();
 
 		for (FragmentSlot fragment : logicalslotlist)
 			if (fragment.fixed == false)
 				dynamicslots.add(fragment);
 
 		// ensure the packet is fixed size or has been declared as dynamic
 		if (isFixedSize() == true)
 			size = offset;
 		else {
 			size = null;
 			if (!klass.isAnnotationPresent(DynamicSize.class))
 				throw new NotDeclareDynamicException(klass);
 		}
 	}
 
 
 	@SuppressWarnings("unchecked")
 	private Method getSizeMethod(Field field, FragmentSlot fragment) throws PacketKitException {
 		Class<? extends Object> fieldtype = field.getType();
 		Method sizemethod;
 		Class<? extends Object>[] signature = new Class[] { PacketUtilities.BYTE_ARRAY };
 
 		try {
 			switch (fragment.type) {
 			case DATA:
 				sizemethod = PacketUtilities.class.getDeclaredMethod("byteArrayLength", signature);
 				break;
 
 			default:
 				throw new InvalidFieldException(field, "Cannot dynamically size field: " + field);
 			}
 		} catch (NoSuchMethodException e) {
 			throw new InvalidFieldException(field, e);
 		}
 
 		return sizemethod;
 	}
 
 
 	@SuppressWarnings("unchecked")
 	private Method getConstructorMethod(Field field, FragmentSlot fragment) throws PacketKitException {
 		Method constructor;
 		Class<? extends Object> fieldtype = field.getType();
 
 		// create method signatures
 		Class<? extends Object>[] signature = null;
 		signature = new Class[] { PacketUtilities.BYTE_ARRAY, Integer.TYPE, Integer.TYPE };
 
 		try {
 			switch (fragment.type) {
 			case INT:
 				constructor = PacketUtilities.class.getDeclaredMethod("intFromByteArray", signature);
 				break;
 
 			case SHORT:
 				constructor = PacketUtilities.class.getDeclaredMethod("shortFromByteArray", signature);
 				break;
 
 			case CHECKSUM:
 			case DATA:
 				// we have to treat these specially
 				constructor = null;
 				break;
 
 			case IP4ADDRESS:
 			case MACADDRESS:
 			case PACKETFRAGMENT:
 			default:
 				constructor = fieldtype.getDeclaredMethod("fromBytes", signature);
 				break;
 			}
 
 			// verify the constructor is static
 			if (constructor != null && !Modifier.isStatic(constructor.getModifiers()))
 				throw new InvalidFieldException(field, "the byte reconstructor method is not static: " + constructor);
 
 			// verify the constructor is accessible
 			if (constructor != null && !Modifier.isPublic(constructor.getModifiers()))
 				throw new InvalidFieldException(field, "the byte reconstructor method is not public: " + constructor);
 		} catch (SecurityException e) {
 			throw new InvalidFieldException(field, e);
 		} catch (NoSuchMethodException e) {
 			throw new InvalidFieldException(field, e);
 		}
 
 		return constructor;
 	}
 
 
 	/**
 	 * Gives true if the given field is accessible (ie, either public or
 	 * package-accessible)
 	 */
 	private boolean isFieldAccessible(Field field) {
 		int modifiers = field.getModifiers();
 		return !(Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers));
 	}
 
 
 	/**
 	 * Gives true if the given class implements {@link PacketFragment}.
 	 */
 	private boolean isValidType(Class<? extends Object> fieldtype) {
 		Class<? extends Object>[] interfaces = fieldtype.getInterfaces();
 		boolean validtype = false;
 		for (Class<? extends Object> iface : interfaces)
 			if (iface == PacketFragment.class) {
 				validtype = true;
 				break;
 			}
 
 		return validtype;
 	}
 
 
 	public boolean isFixedSize() {
 		return isfixedsize;
 	}
 
 
 	@SuppressWarnings("boxing")
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("PacketSkeleton(").append(name).append('\n');
 		sb.append("\t").append(klass.getCanonicalName()).append('\n');
 		sb.append("\tfixed: " + isFixedSize()).append('\n');
 		sb.append("\tsize: " + getSize()).append('\n');
 
 		for (FragmentSlot slot : logicalslotlist) {
 			sb.append(String.format("\t%d:%s  %d  %15s  %15s  %s\n", slot.physicalslot, slot.fixed ? "fixed "
 			        : "dynamic", slot.size, slot.type, slot.field.getName(), slot.field.getType().getCanonicalName()));
 		}
 
 		sb.append(")");
 		return sb.toString();
 	}
 
 
 	public void deleteme(Integer size) {
 		this.size = size;
 	}
 
 
 	public Integer getSize() {
 		return size;
 	}
 
 
 	/**
 	 * @see #getLogicalSlotOrder()
 	 * @return
 	 */
 	public List<FragmentSlot> getSlots() {
 		return logicalslotlist;
 	}
 
 
 	/**
 	 * @return the list of slots for the packet in their logical (fill) order
 	 */
 	public List<FragmentSlot> getLogicalSlotOrder() {
 		return getSlots();
 	}
 
 
 	/**
 	 * @return the list of slots for the packet in their physical order
 	 */
	public List<FragmentSlot> getPhysicalSlotOrder() {
 		return physicalslotlist;
 	}
 
 
 	public List<FragmentSlot> getDynamicSlots() {
 		return dynamicslots;
 	}
 }
 
 enum FragmentSlotType {
 	// general case
 	PACKETFRAGMENT,
 	CHECKSUM,
 	DATA,
 
 	// primitives
 	INT,
 	SHORT,
 
 	// values from a network interface
 	IP4ADDRESS,
 	MACADDRESS;
 }
 
 /**
  * Lightweight container for an individual fragment of a packet.
  * 
  * @author wiktor
  */
 class FragmentSlot implements Comparable<FragmentSlot> {
 	public FragmentSlotType type;
 
 	/**
 	 * the fragment's relative position from other fragments within this packet.
 	 */
 	public int physicalslot;
 
 	/**
 	 * when this fragment should be filled relative to other fragments within
 	 * the packet.
 	 */
 	public int logicalslot;
 
 	/**
 	 * the field this fragment is stored in within a {@link PacketFragment}
 	 * container
 	 */
 	public Field field;
 
 	/**
 	 * the offset, in bytes, of this fragment from the start of a packet
 	 */
 	public int offset;
 
 	/**
 	 * the size, in bytes, of this fragment
 	 */
 	public int size;
 
 	/**
 	 * true if this fragment is fixed in size
 	 */
 	public boolean fixed;
 
 	/**
 	 * a reference to a Method with a signature of [byte[], int, int] ->
 	 * [PacketFragment]
 	 */
 	public Method constructor;
 
 	/**
 	 * a reference to a Method which dynamically computes the size of this
 	 * fragment.
 	 */
 	public Method sizemethod;
 
 
 	@Override
 	public int compareTo(FragmentSlot other) {
 		return this.physicalslot - other.physicalslot;
 	}
 
 
 	@Override
 	public String toString() {
 		return "FragmentSlot(\n\tphysicalslot: " + physicalslot + "\n\tlogicalslot: " + logicalslot + "\n\tfield: "
 		        + field + "\n\toffset:" + offset + "\n\tsize:" + size + "\n\tfixed:" + fixed + "\n\tconstructor: "
 		        + constructor + "\n\tsizemethod: " + sizemethod + "\n)";
 	}
 }
 
 /**
  * {@link Comparator} for {@link FragmentSlot}s by their physical slot.
  * 
  * @author wiktor
  * 
  */
 class PhysicalSlotComparator implements Comparator<FragmentSlot> {
 
 	@Override
 	public int compare(FragmentSlot left, FragmentSlot right) {
 		return left.physicalslot - right.physicalslot;
 	}
 
 }
 
 /**
  * {@link Comparator} for {@link FragmentSlot} by their offset
  * 
  * @author wiktor
  * 
  */
 class LogicalSlotComparator implements Comparator<FragmentSlot> {
 
 	@Override
 	public int compare(FragmentSlot left, FragmentSlot right) {
 		return left.logicalslot - right.logicalslot;
 	}
 
 }
