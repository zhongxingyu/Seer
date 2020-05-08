 /**
  *
  */
 package nz.ac.vuw.ecs.rprofs.server.domain;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.Nullable;
 import javax.validation.constraints.NotNull;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
 import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
 import nz.ac.vuw.ecs.rprofs.server.model.EventVisitor;
 
 /**
  * @author Stephen Nelson (stephen@sfnelson.org)
  */
 public class Event implements DataObject<EventId, Event> {
 
 	public static final int OBJECT_ALLOCATED = 0x1;
 	public static final int ARRAY_ALLOCATED = 0x2;
 	public static final int METHOD_ENTER = 0x4;
 	public static final int METHOD_RETURN = 0x8;
 	public static final int FIELD_READ = 0x10;
 	public static final int FIELD_WRITE = 0x20;
 	public static final int CLASS_WEAVE = 0x40;
 	public static final int CLASS_INITIALIZED = 0x80;
 	public static final int OBJECT_TAGGED = 0x100;
 	public static final int OBJECT_FREED = 0x200;
 	public static final int METHOD_EXCEPTION = 0x400;
 
 	public static final int ALL = 0xFFF;
 	public static final int ALLOCATION = OBJECT_ALLOCATED | OBJECT_TAGGED;
 	public static final int METHODS = METHOD_ENTER | METHOD_RETURN | METHOD_EXCEPTION;
 	public static final int FIELDS = FIELD_READ | FIELD_WRITE;
 	public static final int CLASS_EVENTS = CLASS_WEAVE | CLASS_INITIALIZED;
	public static final int HAS_CLASS = METHODS | FIELDS | CLASS_EVENTS | OBJECT_TAGGED | OBJECT_ALLOCATED;
 
 	@NotNull
 	private EventId id;
 
 	@Nullable
 	private InstanceId thread;
 
 	private int event;
 
 	@Nullable
 	private ClazzId type;
 
 	@Nullable
 	private MethodId method;
 
 	@Nullable
 	private FieldId field;
 
 	@Nullable
 	private List<InstanceId> args;
 
 	public Event() {
 	}
 
 	public Event(@NotNull EventId id, int event) {
 		this.id = id;
 		this.event = event;
 	}
 
 	@NotNull
 	@Override
 	public EventId getId() {
 		return id;
 	}
 
 	@Nullable
 	public InstanceId getThread() {
 		return thread;
 	}
 
 	public void setThread(@Nullable InstanceId thread) {
 		this.thread = thread;
 	}
 
 	@Nullable
 	public ClazzId getClazz() {
 		return type;
 	}
 
 	public void setClazz(@Nullable ClazzId type) {
 		this.type = type;
 	}
 
 	@NotNull
 	public int getEvent() {
 		return event;
 	}
 
 	@Nullable
 	public FieldId getField() {
 		return field;
 	}
 
 	@Nullable
 	public MethodId getMethod() {
 		return method;
 	}
 
 	public void setAttribute(@NotNull AttributeId<?, ?> attribute) {
 		if (attribute instanceof MethodId) {
 			method = (MethodId) attribute;
 		} else {
 			field = (FieldId) attribute;
 		}
 	}
 
 	@Nullable
 	public List<InstanceId> getArgs() {
 		if (this.args == null || this.args.isEmpty()) {
 			return null;
 		} else {
 			return Collections.unmodifiableList(args);
 		}
 	}
 
 	@Nullable
 	public InstanceId getFirstArg() {
 		List<InstanceId> args = this.args;
 		if (args != null && args.size() >= 1) {
 			return args.get(0);
 		}
 
 		return null;
 	}
 
 	public void setArgs(@Nullable List<InstanceId> args) {
 		this.args = args;
 	}
 
 	public void visit(EventVisitor visitor) {
 		switch (getEvent()) {
 			case OBJECT_ALLOCATED:
 				visitor.visitObjectAllocated(this);
 				break;
 			case ARRAY_ALLOCATED:
 				visitor.visitArrayAllocated(this);
 				break;
 			case METHOD_ENTER:
 				visitor.visitMethodEnter(this);
 				break;
 			case METHOD_RETURN:
 				visitor.visitMethodReturn(this);
 				break;
 			case FIELD_READ:
 				visitor.visitFieldRead(this);
 				break;
 			case FIELD_WRITE:
 				visitor.visitFieldWrite(this);
 				break;
 			case CLASS_WEAVE:
 				visitor.visitClassWeave(this);
 				break;
 			case CLASS_INITIALIZED:
 				visitor.visitClassInitialized(this);
 				break;
 			case OBJECT_TAGGED:
 				visitor.visitObjectTagged(this);
 				break;
 			case OBJECT_FREED:
 				visitor.visitObjectFreed(this);
 				break;
 			case METHOD_EXCEPTION:
 				visitor.visitMethodException(this);
 				break;
 		}
 	}
 }
