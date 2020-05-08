 package cz.zcu.kiv.bp.uniplayer.bindings.adapted;
 
 import java.io.File;
 import java.lang.reflect.Array;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.annotation.adapters.XmlAdapter;
 import javax.xml.namespace.QName;
 
 import cz.zcu.kiv.bp.uniplayer.bindings.TBigDecimalList;
 import cz.zcu.kiv.bp.uniplayer.bindings.TBigIntegerList;
 import cz.zcu.kiv.bp.uniplayer.bindings.TBooleanList;
 import cz.zcu.kiv.bp.uniplayer.bindings.TByteArrayList;
 import cz.zcu.kiv.bp.uniplayer.bindings.TByteList;
 import cz.zcu.kiv.bp.uniplayer.bindings.TCollection;
 import cz.zcu.kiv.bp.uniplayer.bindings.TDoubleList;
 import cz.zcu.kiv.bp.uniplayer.bindings.TFileList;
 import cz.zcu.kiv.bp.uniplayer.bindings.TFloatList;
 import cz.zcu.kiv.bp.uniplayer.bindings.TIntList;
 import cz.zcu.kiv.bp.uniplayer.bindings.TLongList;
 import cz.zcu.kiv.bp.uniplayer.bindings.TShortList;
 import cz.zcu.kiv.bp.uniplayer.bindings.TStringList;
 import cz.zcu.kiv.bp.uniplayer.bindings.TValue;
 
 
 public class ValueAdapter extends XmlAdapter<TValue, Value>
 {
 
     @Override
     public Value unmarshal(TValue arg) throws Exception
     {
         Value ret = new Value();
                 
         if (arg.getString() != null)
         {
         	ret.setType(String.class);
             ret.setVal(arg.getString().getValue());
         }
         else if (arg.getShort() != null)
         {
         	ret.setType(short.class);
             ret.setVal(arg.getShort().getValue());
         }
         else if (arg.getInt() != null)
         {
         	ret.setType(int.class);
             ret.setVal(arg.getInt().getValue());
         }
         else if (arg.getLong() != null)
         {
         	ret.setType(long.class);
             ret.setVal(arg.getLong().getValue());
         }
         else if (arg.getFloat() != null)
         {
         	ret.setType(float.class);
             ret.setVal(arg.getFloat().getValue());
         }
         else if (arg.getDouble() != null)
         {
         	ret.setType(double.class);
             ret.setVal(arg.getDouble().getValue());
         }
         else if (arg.getByte() != null)
         {
         	ret.setType(byte.class);
             ret.setVal(arg.getByte().getValue());
         }
         else if (arg.getBoolean() != null)
         {
         	ret.setType(boolean.class);
             ret.setVal(arg.getBoolean().getValue());
         }
         else if (arg.getBigInteger() != null)
         {
         	ret.setType(BigInteger.class);
             ret.setVal(arg.getBigInteger().getValue());
         }
         else if (arg.getBigDecimal() != null)
         {
         	ret.setType(BigDecimal.class);
             ret.setVal(arg.getBigDecimal().getValue());
         }
         else if (arg.getBase64() != null)
         {
         	ret.setType(byte[].class);
             ret.setVal(arg.getBase64().getValue());
         }
         else if (arg.getFile() != null)
         {
         	ret.setType(File.class);
         	ret.setVal(arg.getFile());
         }
         else if (arg.getStringList() != null)
         {
         	ret.setType(TStringList.class);
         	TStringList listElem =  arg.getStringList();
         	List<String> list = listElem.getString();
         	Object val = this.createListValue(listElem, list, "");
         	ret.setVal(val);
         }
         else if (arg.getLongList() != null)
         {
         	ret.setType(TLongList.class);
         	TLongList listElem =  arg.getLongList();
         	List<Long> list = listElem.getLong();
         	Object val = this.createListValue(listElem, list, 0L);
         	ret.setVal(val);
         }
         else if (arg.getIntList() != null)
         {
         	ret.setType(TIntList.class);
         	TIntList listElem =  arg.getIntList();
         	List<Integer> list = listElem.getInt();
         	Object val = this.createListValue(listElem, list, 0);
         	ret.setVal(val);
         }
         else if (arg.getShortList() != null)
         {
         	ret.setType(TShortList.class);
         	TShortList listElem =  arg.getShortList();
         	List<Short> list = listElem.getShort();
         	Object val = this.createListValue(listElem, list, (short)0);
         	ret.setVal(val);
         }
         else if (arg.getByteList() != null)
         {
         	ret.setType(TByteList.class);
         	TByteList listElem =  arg.getByteList();
         	List<Byte> list = listElem.getByte();
         	Object val = this.createListValue(listElem, list, (byte)0x00);
         	ret.setVal(val);
         }
         else if (arg.getDoubleList() != null)
         {
         	ret.setType(TDoubleList.class);
         	TDoubleList listElem =  arg.getDoubleList();
         	List<Double> list = listElem.getDouble();
         	Object val = this.createListValue(listElem, list, 0.0);
         	ret.setVal(val);
         }
         else if (arg.getFloatList() != null)
         {
         	ret.setType(TFloatList.class);
         	TFloatList listElem =  arg.getFloatList();
         	List<Float> list = listElem.getFloat();
         	Object val = this.createListValue(listElem, list, 0.0f);
         	ret.setVal(val);
         }
         else if (arg.getBigDecimalList() != null)
         {
         	ret.setType(TBigDecimalList.class);
         	TBigDecimalList listElem =  arg.getBigDecimalList();
         	List<BigDecimal> list = listElem.getBigDecimal();
         	Object val = this.createListValue(listElem, list, BigDecimal.ZERO);
         	ret.setVal(val);
         }
         else if (arg.getBigIntegerList() != null)
         {
         	ret.setType(TBigIntegerList.class);
         	TBigIntegerList listElem =  arg.getBigIntegerList();
         	List<BigInteger> list = listElem.getBigInteger();
         	Object val = this.createListValue(listElem, list, BigInteger.ZERO);
         	ret.setVal(val);
         }
         else if (arg.getBooleanList() != null)
         {
         	ret.setType(TBooleanList.class);
         	TBooleanList listElem =  arg.getBooleanList();
         	List<Boolean> list = listElem.getBoolean();
         	Object val = this.createListValue(listElem, list, false);
         	ret.setVal(val);
         }
         else if (arg.getFileList() != null)
         {
         	ret.setType(TFileList.class);
         	TFileList listElem =  arg.getFileList();
         	List<String> list = listElem.getFile();
         	Object val = this.createListValue(listElem, list, ".");
         	ret.setVal(val);
         }
         else if (arg.getByteArrayList() != null)
         {
         	ret.setType(TByteArrayList.class);
         	TByteArrayList listElem =  arg.getByteArrayList();
         	List<byte[]> list = listElem.getBase64();
         	Object val = this.createListValue(listElem, list, new byte[0]);
         	ret.setVal(val);
         }
         
         return ret;
     }
 
 	@Override
     public TValue marshal(Value arg) throws Exception
     {
         TValue ret = new TValue();
         Class<?> clazz = arg.getType();
 
         if (clazz == String.class)
         {
         	ret.setString(this.createElement((String) arg.getVal(), String.class, "string"));
         }
        else if (clazz == Short.TYPE || clazz == Short.class)
         {
         	ret.setShort(this.createElement((Short) arg.getVal(), short.class, "short"));
         }
        else if (clazz == Integer.TYPE || clazz == Integer.class)
         {
         	ret.setInt(this.createElement((Integer) arg.getVal(), int.class, "int"));
         }
        else if (clazz == Long.TYPE || clazz == Long.class)
         {
         	ret.setLong(this.createElement((Long) arg.getVal(), long.class, "long"));
         }
        else if (clazz == Float.TYPE  || clazz == Float.class/*clazz.equals(Float.class) || clazz.equals(float.class)*/)
         {
         	ret.setFloat(this.createElement((Float) arg.getVal(), float.class, "float"));
         }
        else if (clazz == Double.TYPE || clazz == Double.class/*clazz.equals(Double.class) || clazz.equals(double.class)*/)
         {
         	ret.setDouble(this.createElement((Double) arg.getVal(), double.class, "double"));
         }
        else if (clazz == Byte.TYPE || clazz == Byte.class /*clazz.equals(Byte.class) || clazz.equals(byte.class)*/)
         {
         	ret.setByte(this.createElement((Byte) arg.getVal(), byte.class, "byte"));
         }
        else if (clazz == Boolean.TYPE || clazz == Boolean.class /*clazz.equals(Boolean.class) || clazz.equals(boolean.class)*/)
         {
         	ret.setBoolean(this.createElement((Boolean) arg.getVal(), boolean.class, "boolean"));
         }
         else if (clazz == BigInteger.class)
         {
         	ret.setBigInteger(this.createElement((BigInteger) arg.getVal(), BigInteger.class, "BigInteger"));
         }
         else if (clazz == BigDecimal.class)
         {
         	ret.setBigDecimal(this.createElement((BigDecimal) arg.getVal(), BigDecimal.class, "BigDecimal"));
         }
         else if (clazz == Byte[].class || clazz == byte[].class)
         {
         	ret.setBase64(this.createElement((byte[]) arg.getVal(), byte[].class, "base64"));
         }
         else if (clazz == File.class)
         {
         	ret.setFile((File) arg.getVal());
         }
         else if (clazz == TLongList.class)
         {
         	TLongList lngL = new TLongList();
         	createElement(arg.getVal(), lngL, lngL.getLong(), 1L);
         	ret.setLongList(lngL);
         }
         else if (clazz == TIntList.class)
         {
         	TIntList intL = new TIntList();
         	createElement(arg.getVal(), intL, intL.getInt(), 0);
         	ret.setIntList(intL);
         }
         else if (clazz == TShortList.class)
         {
         	TShortList shrtL = new TShortList();
         	createElement(arg.getVal(), shrtL, shrtL.getShort(), (short)0);
         	ret.setShortList(shrtL);
         }
         else if (clazz == TByteList.class)
         {
         	TByteList bytL = new TByteList();
         	createElement(arg.getVal(), bytL, bytL.getByte(), (byte) 0x00);
         	ret.setByteList(bytL);
         }
         else if (clazz == TDoubleList.class)
         {
         	TDoubleList dblL = new TDoubleList();
         	createElement(arg.getVal(), dblL, dblL.getDouble(), 0.0);
         	ret.setDoubleList(dblL);
         }
         else if (clazz == TFloatList.class)
         {
         	TFloatList fltL = new TFloatList();
         	createElement(arg.getVal(), fltL, fltL.getFloat(), 0.0f);
         	ret.setFloatList(fltL);
         }
         else if (clazz == TBigDecimalList.class)
         {
         	TBigDecimalList bigDecL = new TBigDecimalList();
         	createElement(arg.getVal(), bigDecL, bigDecL.getBigDecimal(), BigDecimal.ZERO);
         	ret.setBigDecimalList(bigDecL);
         }
         else if (clazz == TBigIntegerList.class)
         {
         	TBigIntegerList bigintL = new TBigIntegerList();
         	createElement(arg.getVal(), bigintL, bigintL.getBigInteger(), BigInteger.ZERO);
         	ret.setBigIntegerList(bigintL);
         }
         else if (clazz == TBooleanList.class)
         {
         	TBooleanList booL = new TBooleanList();
         	createElement(arg.getVal(), booL, booL.getBoolean(), false);
         	ret.setBooleanList(booL);
         }
         else if (clazz == TFileList.class)
         {
         	TFileList filL = new TFileList();
         	createElement(arg.getVal(), filL, filL.getFile(), ".");
         	ret.setFileList(filL);
         }
         else if (clazz == TStringList.class)
         {
         	TStringList strL = new TStringList();
         	createElement(arg.getVal(), strL, strL.getString(), "");
         	ret.setStringList(strL);
         }
         else if (clazz == TByteArrayList.class)
         {
         	TByteArrayList bayList = new TByteArrayList();
         	createElement(arg.getVal(), bayList, bayList.getBase64(), new byte[0]);
         	ret.setByteArrayList(bayList);
         }
 
         return ret;
     }
 
 	/// TODO add @SuppresWarning after testing
 	private <T extends TCollection, V extends List<U>, U> Object createListValue(T listElement, V list, U itemPrototype)
     {    	
 		Object val = null;
 		if (listElement.getType().equals("array"))
 		{
 			val =  list.toArray( (U[]) Array.newInstance(itemPrototype.getClass(), list.size()) );
 		}
 		else if (listElement.getType().equals("LinkedList"))
 		{
 			val = new LinkedList<U>(list);
 		}
 		else if (listElement.getType().equals("ArrayList"))
 		{
 			val = list;
 		}
 		return val;
     }
 
 	/// TODO add @SuppresWarning after testing
 	private <T extends TCollection, V extends List<U>, U> void createElement(Object value, T targetElement, V targetList, U itemPrototype)
 	{		
 //		U[] prototype = (U[]) Array.newInstance(itemPrototype.getClass(), 0);
 		
 		if (value.getClass().equals(ArrayList.class))
 		{
 			targetElement.setType("ArrayList");
 			targetList.addAll((V) value);
 		}
 		else if (value.getClass().equals(LinkedList.class))
 		{
 			targetElement.setType("LinkedList");
 			targetList.addAll((V) value);
 		}
 		else if (value.getClass().isArray() && value.getClass().getComponentType() == itemPrototype.getClass())
 		{
 			targetElement.setType("array");
 			targetList.addAll(Arrays.asList((U[]) value));
 		}
 		/*else if (value.getClass().equals(prototype.getClass()))
 		{
 			targetElement.setType("array");
 			targetList.addAll(Arrays.asList((U[]) value));
 		}*/
 	}
 	
 	private <T> JAXBElement<T> createElement(T arg, Class<T> elemType, String elemName)
 	{
 		JAXBElement<T> ret = null;
 		ret = new JAXBElement<T>(
 			new QName("", elemName),
 			elemType,
 			TValue.class,
 			arg
 		);
 		if (arg == null)
 		{
 			ret.setNil(true);
 		}
 		return ret;
 	}
 }
