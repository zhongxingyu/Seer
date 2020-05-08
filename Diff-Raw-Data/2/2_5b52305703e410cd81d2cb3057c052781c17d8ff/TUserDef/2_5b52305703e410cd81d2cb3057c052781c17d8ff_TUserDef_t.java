 package us.jubat.common.type;
 
 import org.msgpack.type.ArrayValue;
 import org.msgpack.type.Value;
 
 import us.jubat.common.TypeMismatch;
 import us.jubat.common.UserDefinedMessage;
 
 public abstract class TUserDef<T extends UserDefinedMessage> implements
 		TType<T> {
 	private final int numMember;
 
 	public TUserDef(int numMember) {
 		this.numMember = numMember;
 	}
 
 	public void check(T value) {
 		if (value == null) {
 			throw new NullPointerException();
 		} else {
 			value.check();
 		}
 	}
 
 	public T revert(Value value) {
 		if (value.isArrayValue()) {
 			ArrayValue array = value.asArrayValue();
			if (array.size() == this.numMember) {
 				return this.create(array);
 			} else {
 				throw new TypeMismatch();
 			}
 		} else {
 			throw new TypeMismatch();
 		}
 	}
 
 	public abstract T create(ArrayValue value);
 }
