 
 package com.zanoccio.packetkit.headers;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.PriorityQueue;
 
 import com.zanoccio.packetkit.IP4Address;
 import com.zanoccio.packetkit.MACAddress;
 import com.zanoccio.packetkit.PacketFragment;
 import com.zanoccio.packetkit.exceptions.CannotPopulateFromNetworkInterfaceException;
 import com.zanoccio.packetkit.exceptions.InvalidFieldException;
 import com.zanoccio.packetkit.exceptions.InvalidStaticFragmentTypeException;
 import com.zanoccio.packetkit.exceptions.NotDeclareDynamicException;
 import com.zanoccio.packetkit.exceptions.PacketKitException;
 import com.zanoccio.packetkit.exceptions.SlotTakenException;
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
 	public static HashMap<Class<? extends Object>, FragmentSlotType> VALIDPRIMITIVES;
 	static {
 		VALIDPRIMITIVES = new HashMap<Class<? extends Object>, FragmentSlotType>();
 		VALIDPRIMITIVES.put(Integer.TYPE, FragmentSlotType.INT);
 		VALIDPRIMITIVES.put(Short.TYPE, FragmentSlotType.SHORT);
 	}
 
 	public static HashMap<Class<? extends Object>, PacketSkeleton> SKELETON_CACHE;
 	static {
 		SKELETON_CACHE = new HashMap<Class<? extends Object>, PacketSkeleton>();
 	}
 
 	private String name;
 	private Class<? extends PacketHeader> klass;
 	private boolean isfixedsize;
 	private Integer size;
 
 	private ArrayList<Field> fieldsfromnetworkinterface;
 	private PriorityQueue<FragmentSlot> queue;
 
 
 	public PacketSkeleton(Class<? extends PacketHeader> klass) throws PacketKitException {
 		construct(klass);
 	}
 
 
 	public void construct(Class<? extends PacketHeader> klass) throws PacketKitException {
 		isfixedsize = true;
 
 		this.name = klass.getName();
 		this.klass = klass;
 
 		Field[] fields = klass.getDeclaredFields();
 
 		HashSet<Integer> slots = new HashSet<Integer>();
 		queue = new PriorityQueue<FragmentSlot>();
 		fieldsfromnetworkinterface = new ArrayList<Field>();
 
 		int packetsize = 0;
 		// examine each field to construct this packet's skeleton
 		for (Field field : fields) {
 			FragmentSlotType slottype = null;
 
 			boolean isfragmentfixed = true;
 			int fragmentsize;
 
 			StaticFragment annotation = field.getAnnotation(StaticFragment.class);
 
 			// check whether this field is autowired by the network
 			// interface
 			Class<? extends Object> fieldtype = field.getType();
 			if (field.isAnnotationPresent(FromNetworkInterface.class)) {
 				fieldsfromnetworkinterface.add(field);
 
 				if (fieldtype == IP4Address.class)
 					slottype = FragmentSlotType.IP4ADDRESS;
 				else if (fieldtype == MACAddress.class)
 					slottype = FragmentSlotType.MACADDRESS;
 				else
 					throw new CannotPopulateFromNetworkInterfaceException(field, fieldtype);
 			}
 
 			// no StaticFragment annotation, move along...
 			if (annotation == null)
 				continue;
 
 			// check that the slot is unused
 			int slot = annotation.slot();
 			if (slots.contains(slot))
 				throw new SlotTakenException(field);
 
 			// verify the field's type
 			if (!VALIDPRIMITIVES.containsKey(fieldtype)) {
 				Class<? extends Object>[] interfaces = fieldtype.getInterfaces();
 				boolean validtype = false;
 				for (Class<? extends Object> iface : interfaces)
 					if (iface == PacketFragment.class) {
 						validtype = true;
 						break;
 					}
 
 				if (!validtype)
 					throw new InvalidStaticFragmentTypeException(field);
 
 				// if the slot type is already set it's being autowired
 				if (slottype == null)
 					slottype = FragmentSlotType.PACKETFRAGMENT;
 
 			} else {
 				slottype = VALIDPRIMITIVES.get(fieldtype);
 			}
 
 			// verify that the field is accessible
 			int modifiers = field.getModifiers();
 			if (Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers))
 				throw new InvalidFieldException(field, " must be public or package scoped");
 
 			if (annotation.size() < 0) {
 				// if there is no explicit size
 				if (fieldtype.isAnnotationPresent(FixedSize.class)) {
 					// but the type has a @FixedSize annotation
 					FixedSize sizeannotation = fieldtype.getAnnotation(FixedSize.class);
 					fragmentsize = sizeannotation.size();
 				} else {
 					// too bad... dynamic
 					isfixedsize = false;
 					isfragmentfixed = false;
 					fragmentsize = -1;
 				}
 			} else {
 				fragmentsize = annotation.size();
 			}
 
 			slots.add(Integer.valueOf(slot));
 			queue.add(new FragmentSlot(slottype, slot, field, fragmentsize, isfragmentfixed));
 
 			if (fragmentsize > 0)
 				packetsize += fragmentsize;
 		}
 
 		if (isFixedSize() == true)
 			size = Integer.valueOf(packetsize);
 		else {
 			size = null;
 			if (!klass.isAnnotationPresent(DynamicSize.class))
 				throw new NotDeclareDynamicException(klass);
 		}
 
 		synchronized (SKELETON_CACHE) {
 			SKELETON_CACHE.put(klass, this);
 		}
 	}
 
 
 	public boolean isFixedSize() {
 		return isfixedsize;
 	}
 
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("PacketSkeleton(").append(name).append('\n');
 		sb.append("\t").append(klass.getCanonicalName()).append('\n');
 		sb.append("\tfixed: " + isFixedSize()).append('\n');
 		sb.append("\tsize: " + getSize()).append('\n');
 
 		for (FragmentSlot slot : queue) {
 			sb.append(String.format("\t%d:%s  %d  %15s  %15s  %s\n", slot.slot, slot.fixed ? "fixed " : "dynamic",
 			                        slot.size, slot.type, slot.field.getName(), slot.field.getType().getCanonicalName()));
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
 
 
 	public PriorityQueue<FragmentSlot> getSlots() {
 		return queue;
 	}
 }
 
 enum FragmentSlotType {
 	// general case
 	PACKETFRAGMENT,
 
 	// primitives
 	INT,
 	SHORT,
 
 	// values from a network interface
 	IP4ADDRESS,
 	MACADDRESS;
 }
 
 class FragmentSlot implements Comparable<FragmentSlot> {
 	public FragmentSlotType type;
 
 	public int slot;
 	public Field field;
 	public int size;
 	public boolean fixed;
 
 
 	public FragmentSlot(FragmentSlotType type, int slot, Field field, int size, boolean fixed) {
 		this.slot = slot;
 		this.field = field;
 		this.size = size;
 		this.fixed = fixed;
 		this.type = type;
 	}
 
 
 	@Override
 	public int compareTo(FragmentSlot other) {
 		return this.slot - other.slot;
 	}
 }
