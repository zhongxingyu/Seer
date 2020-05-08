 package org.gnome.dconf;
 
 import static org.bridj.Pointer.pointerToCString;
 import static org.bridj.Pointer.pointerToPointer;
 import static org.gnome.dconf.DConf.DConfClient;
 import org.bridj.Pointer;
 import org.gtk.glib.GLib;
 import org.gtk.glib.GLib.GError;
 import org.gtk.glib.GLib.GVariant;
 import org.gtk.glib.GLib.GVariantClass;
 import org.gtk.glib.GLib.GVariantType;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * dconf client
  * <p>Instances of this class can be used to interact with the dconf subsystem.</p>
  * @author Benjamin P. Jung
  */
 public class Client {
 
     private final DConfClient _client;
 
     private Client(final DConfClient _client) {
         super();
         this._client = _client;
     }
 
     public static Client create() {
         final Pointer<DConfClient> _client = DConf.clientNew();
         return new Client(_client.get());
     }
 
     public Boolean readBoolean(final String key) {
         return this.<Boolean>read(key, GVariantClass.BOOLEAN);
     }
 
     public Byte readByte(final String key) {
         return this.<Byte>read(key, GVariantClass.BYTE);
     }
 
     public String readString(final String key) {
         return this.<String>read(key, GVariantClass.STRING);
     }
 
     public void writeBoolean(final String key, final Boolean value) {
         this.<Boolean>write(key, value, GVariantClass.BOOLEAN);
     }
 
     public void writeByte(final String key, final Byte value) {
         this.<Byte>write(key, value, GVariantClass.BYTE);
     }
 
     public void writeString(final String key, final String value) {
         this.<String>write(key, value, GVariantClass.STRING);
     }
 
     public List<String> list(final String dir) {
         final Pointer<Integer> _lengthPtr = Pointer.allocateInt();
         final Pointer<Pointer<Byte>> _list = DConf.clientList(_client.getPeer(), pointerToCString(dir), _lengthPtr);
 
         final int _length = _lengthPtr.get().intValue();
         final List<String> list = new ArrayList<>(_length);
 
         for (int i = 0; i < _length; i++) {
             list.add(_list.get(i).getCString());
         }
 
         return list;
 
     }
 
     /**
      * Helper method that wraps up all the different conversion facilities offered by GLib.
      * @param key
      * @param variantClass
      * @param <T>
      * @return
      */
     protected <T> T read(final String key, final GVariantClass variantClass) {
 
         // Fetch config entry from dconf.
         final Pointer<GVariant> _variantPtr = DConf.clientRead(this._client.getPeer(), pointerToCString(key));
         if (_variantPtr == Pointer.NULL) {
             return null;
         }
 
         switch (variantClass) {
             case BOOLEAN:
                 return (T) Boolean.valueOf(GLib.variantGetBoolean(_variantPtr));
             case BYTE:
                 return (T) Byte.valueOf(GLib.variantGetByte(_variantPtr));
             case INT16:
                 return (T) Short.valueOf(GLib.variantGetInt16(_variantPtr));
             case INT32:
                 return (T) Integer.valueOf(GLib.variantGetInt32(_variantPtr));
             case INT64:
                return (T) Long.valueOf(GLib.variantGetInt16(_variantPtr));
             case DOUBLE:
                 return (T) Double.valueOf(GLib.variantGetDouble(_variantPtr));
             case STRING:
                 return (T) GLib.variantGetString(_variantPtr).getCString();
             default:
                 throw new IllegalStateException("Unsupported variant class encountered.");
         }
     }
 
     protected <T> void write(final String key, final T value, final GVariantClass variantClass) {
         final Pointer<Pointer<GError>> _error = pointerToPointer(Pointer.NULL);
         final Pointer<GVariant> _value;
         switch (variantClass) {
             case BOOLEAN:
                 _value = GLib.variantNewBoolean(((Boolean) value).booleanValue());
                 break;
             case BYTE:
                 _value = GLib.variantNewByte(((Byte) value).byteValue());
                 break;
             case INT16:
                 _value = GLib.variantNewInt16(((Short) value).shortValue());
                 break;
             case INT32:
                 _value = GLib.variantNewInt32(((Integer) value).intValue());
                 break;
             case INT64:
                 _value = GLib.variantNewInt64(((Long) value).longValue());
                 break;
             case DOUBLE:
                 _value = GLib.variantNewDouble(((Double) value).doubleValue());
                 break;
             case STRING:
                 _value = GLib.variantNewString(pointerToCString((String) value));
                 break;
             default:
                 throw new IllegalStateException("Unsupported variant class encountered.");
         }
         DConf.clientWriteFast(this._client.getPeer(), pointerToCString(key), _value, _error);
     }
 
     @Override
     public void finalize() throws Throwable {
         if (this._client != null) {
             DConf.clientSync(this._client.getPeer());
         }
     }
 
 }
