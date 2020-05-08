 package memoryManager;
 
 import utils.ByteConverter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nikita_kartashov
  * Date: 23/10/2013
  * Time: 17:35
  * To change this template use File | Settings | File Templates.
  */
 public class DiskPage {
     public static final int MAX_PAGE_SIZE = 4096; // 4K
 	public static final int DATA_OFFSET = 96;
 	public static final long NULL_PTR = -1;
 
     public DiskPage(byte[] rawPage, boolean blankPage)
     {
 		_rawPage = rawPage;
 	    if (blankPage)
 	    {
 		    initHeader(_rawPage);
		    _nextPageIndex = NULL_PTR;
		    _prevPageIndex = NULL_PTR;
 	    }
 	    else
 	    {
 		    _nextPageIndex = ByteConverter.longFromByte(rawPage, NEXT_PAGE_INDEX_OFFSET);
 		    _prevPageIndex = ByteConverter.longFromByte(rawPage, PREV_PAGE_INDEX_OFFSET);
 	    }
     }
 
 	public void setNextPageIndex(long index)
 	{
 		setNextPageIndex(_rawPage, index);
 	}
 
 	public void setPrevPageIndex(long index)
 	{
 		setPrevPageIndex(_rawPage, index);
 	}
 
 	public void write(int dstOffset, byte[] source, int srcOffset, int length)
 	{
 		System.arraycopy(source, srcOffset, _rawPage, DATA_OFFSET + dstOffset, length);
 	}
 
 	public byte[] read(int srcOffset, int length)
 	{
 		byte[] result = new byte[length];
 		System.arraycopy(_rawPage, DATA_OFFSET + srcOffset, result, 0, length);
 		return result;
 	}
 
 	public long nextPageIndex() {return _nextPageIndex;}
 	
 	public long prevPageIndex() {return _prevPageIndex;}
 
 	public byte[] rawPage() {return _rawPage;}
 
 	public static void initHeader(byte[] rawPage)
 	{
 		setNextPageIndex(rawPage, NULL_PTR);
 		setPrevPageIndex(rawPage, NULL_PTR);
 	}
 
 	private static void setNextPageIndex(byte[] rawPage, long index)
 	{
 		byte[] newValues = ByteConverter.longToByte(index);
 		System.arraycopy(newValues, 0, rawPage, NEXT_PAGE_INDEX_OFFSET, ByteConverter.LONG_LENGTH_IN_BYTES);
 	}
 
 	private static void setPrevPageIndex(byte[] rawPage, long index)
 	{
 		byte[] newValues = ByteConverter.longToByte(index);
 		System.arraycopy(newValues, 0, rawPage, PREV_PAGE_INDEX_OFFSET, ByteConverter.LONG_LENGTH_IN_BYTES);
 	}
 
 
 
 	protected byte[] _rawPage;
 	protected long _nextPageIndex;
 	protected long _prevPageIndex;
 
 
 	protected static int NEXT_PAGE_INDEX_OFFSET = 0;
 	protected static int PREV_PAGE_INDEX_OFFSET = 8;
 }
