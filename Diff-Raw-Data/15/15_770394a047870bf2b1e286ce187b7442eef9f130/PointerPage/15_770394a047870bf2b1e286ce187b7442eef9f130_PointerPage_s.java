 package memoryManager;
 
 import utils.ByteConverter;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nikita_kartashov
  * Date: 06/11/2013
  * Time: 00:38
  * To change this template use File | Settings | File Templates.
  */
 public class PointerPage extends DiskPage
 {
 	public PointerPage(byte[] rawPage, boolean blankPage)
 	{
 		super(rawPage, blankPage);
 		if (blankPage)
 			initHeader(_rawPage);
 		else
 			_pointersCount = ByteConverter.intFromByte(_rawPage, COUNT_OFFSET);
 	}
 
 	public long getPointer(int index)
 	{
 		return ByteConverter.longFromByte(_rawPage, DATA_OFFSET + index * ByteConverter.LONG_LENGTH_IN_BYTES);
 	}
 
 	public long getLastPointer()
 	{
 		if (_pointersCount == 0)
 			return -1;
 		return getPointer(_pointersCount - 1);
 	}
 
 	public void setPointer(int index, long pointer) throws IndexOutOfBoundsException
 	{
 		if (index > MAX_POINTERS_COUNT)
 			throw new IndexOutOfBoundsException("Cannot set pointer with " + index + ": index is out of range");
 		byte[] newValues = ByteConverter.longToByte(pointer);
 		System.arraycopy(newValues, 0, _rawPage,
 			DATA_OFFSET + index * ByteConverter.LONG_LENGTH_IN_BYTES, ByteConverter.LONG_LENGTH_IN_BYTES);
 	}
 
 	public void addPointer(long pointer)
 	{
 		setPointer(_pointersCount++, pointer);
 		setPointersCount();
 	}
 
 	public void removePointer(int index)
 	{
 		if (index != _pointersCount - 1)
 		{
 			long lastPointer = getPointer(_pointersCount - 1);
 			setPointer(index, lastPointer);
 			--_pointersCount;
 			setPointersCount();
 		}
 		else
 			removeLastPointer();
 	}
 
 	public void removeLastPointer()
 	{
 		--_pointersCount;
 		setPointersCount();
 	}
 
 	public boolean isFull() {return _pointersCount == MAX_POINTERS_COUNT;}
 
 	public boolean isEmpty() {return _pointersCount == 0;}
 
 	public long pointersCount() {return _pointersCount;}
 
 	public static void initHeader(byte[] rawData)
 	{
 		setPointersCount(rawData, 0);
 	}
 
 	private void setPointersCount()
 	{
 		setPointersCount(_rawPage, _pointersCount);
 	}
 
 	private static void setPointersCount(byte[] rawData, int count)
 	{
 		byte[] pointersCount = ByteConverter.intToByte(count);
 		System.arraycopy(pointersCount, 0, rawData, COUNT_OFFSET, ByteConverter.INT_LENGTH_IN_BYTES);
 	}
 
 	private int _pointersCount = 0;
 
 	private static int MAX_POINTERS_COUNT = 500;
 	private static int COUNT_OFFSET = 16;
 }
