 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Formatter;
 import java.util.ArrayList;
 import java.util.List;
 import java.io.InputStream;
 import java.io.IOException;
 
 /**
  * Class that contains static functions that can be used multiple places.  
  * @author Jonathan Nanney
  * */
 public class Common
 {
    private Common() {}
 
    /**
     * This function performs a left circular shift.  That is, it's as if you 
     * did a regular left shift and moved the bits that were pushed off onto 
     * the right end.
     * @param num - the number to shift.
     * @param shiftLength - the amount of bits to shift by
     * @return the number shifted left circularly.
     * */
    public static int rotateLeftCircular(int num, int shiftLength)
    {
       //This code copied almost exactly out of FIPS-180-2 page 8.
       return (num << shiftLength) | (num >>> (Integer.SIZE - shiftLength));
    }
 
    public static int makeBytesInt(byte[] data, int start, int end)
    {
       int result = 0;
       for(int i = start; i < end; i++)
       {
          result = result << Byte.SIZE;
         int byteValue = (int) (0xFF & data[i]);
          result = result | byteValue;
       }
       return result;
    }
 
    /**
     * This method turns a specific subset of a byte array into a long
     * @param data - the array to get the bytes from
     * @param start - the first byte. Inclusive
     * @param end - the last byte. Exclusive
     * @return the long value of the selected bytes
     * */
    public static long makeBytesLong(byte[] data, int start, int end)
    {
       long result = 0;
       for(int i = start; i < end; i++)
       {
          result = result << Byte.SIZE;
         long byteValue = (long) (0xFF & data[i]);
          result = result | byteValue;
       }
       return result;
    }
    
    /**
     * This method turns bytes into a long
     * @param input - the bytes to turn into a long
     * @return the long created from these bytes
     * */
    public static long makeBytesLong(byte[] input)
    {
       return makeBytesLong(input, 0, input.length);
    }
 
    /**
     * Puts all the data from a given input stream into a byte[] array
     * @param in - the input stream to read from
     * @return the byte array containing all of the data from the input stream
     * */
    public static byte[] readAllData(InputStream in) throws IOException
    {
       ArrayList<Byte> list = new ArrayList<Byte>();
       while(in.available() > 0)
       {
          int toAdd = in.read();
          if(toAdd != -1)
          {
             list.add(new Byte((byte) toAdd));
          }
       }
       byte[] result = new byte[list.size()];
       for(int i = 0; i < result.length; i++)
       {
          result[i] = list.get(i).byteValue();
       }
       return result;
    }
    
    /**
     * Turns a list of Byte objects into an array of byte primitives
     * @param bytes - the list of Bytes to turn into primitives
     * @return the array of bytes
     * */
    public static byte[] makeByteListPrimitive(List<Byte> bytes)
    {
       byte[] result = new byte[bytes.size()];
       for(int i = 0; i < result.length; i++)
       {
          result[i] = bytes.get(i);
       }
       return result;
    }
    
    /**
     * Turns a long into an array of bytes
     * @param number - the number to put into a byte array
     * @return the array of bytes
     * */
    public static byte[] makeLongBytes(long number)
    {
       int bytesInLong = Long.SIZE / Byte.SIZE;
       byte[] result = new byte[bytesInLong];
       long shiftSpaces = Long.SIZE - Byte.SIZE;
       for(int i = 0; i < bytesInLong; i++)
       {
          result[i] = (byte) (number >>> shiftSpaces);
          shiftSpaces -= Byte.SIZE;
       }
       return result;
    }
 
    public static byte[] makeIntBytes(int number)
    {
       int bytesInInt = Integer.SIZE / Byte.SIZE;
       byte[] result = new byte[bytesInInt];
       long shiftSpaces = Integer.SIZE - Byte.SIZE;
       for(int i = 0; i < result.length; i++)
       {
          result[i] = (byte) (number >>> shiftSpaces);
          shiftSpaces -= Byte.SIZE;
       }
       return result;
    }
    /**
     * Gets the current time expressed as seconds since Jan 1, 1970 and puts it
     * into an array of 4 bytes
     * @return the epoch time in a byte array
     * */
    public static byte[] getByteTime()
    {
       Calendar cal = new GregorianCalendar();
       int time = (int) (cal.getTimeInMillis() / 1000); 
       byte[] byteTime = new byte[4];
       int mask = 0xFF000000;
       int shiftAmount = 24;
       for(int i = 0; i < byteTime.length; i++)
       {
          byteTime[i] = (byte) ((time & mask) >>> shiftAmount);
          mask = mask >> Byte.SIZE;
          shiftAmount -= Byte.SIZE;
       }
       return byteTime;
    }
    
    /**
     * Gets the bits from the starting to the ending position inclusive.  
     * Numbering starts at 1 and ends at 64.  Since Java is Big-Endian bit 1 is
     * the left most (and most significant) bit.
     * @param number - the number to get the bits from
     * @param start - the starting bit position 
     * @param end - the ending bit position 
     * @return a number containing the selected bits. 
     * */
    public static long getBits(long number, int start, int end) throws InvalidSelectionException
    {
       if (start > end || start < 1 || end > Long.SIZE)
       {
          throw new InvalidSelectionException("Invalid bit positions");
       }
       long result = 0;
       for(int i = start; i <= end; i++)
       {
          result = result << 1;
          result = result | getBit(number, i);
       }
       return result;
    }
 
    
    /**
     * Gets the bit in the specificified position.  Indexing starts at 1.
     * @param number - the number to get the bit from
     * @param position - the position of the bit to get.  Indexing starts at 1
     * @return a 1 or 0 depending on what the specified bit was set to
     * */
    public static byte getBit(byte number, int position)
       throws InvalidSelectionException
    {
       if (position > Byte.SIZE || position < 1)
       {
          throw new InvalidSelectionException(position + " is not a valid bit " + 
             "position");
       }
       byte mask = 1;
       int value = (mask << (Byte.SIZE - position)) & number;
       if (value > 0)
       {
          return 1;
       }
       else
       {
          return 0;
       }
    }
 
    /**
     * Gets either a 0 or a 1 from the given position.  Note that the leftmost
     * bit is bit 1 (not bit 0) because that's what the DES standard wants.
     * @param number - the number to get the bit from
     * @param position - the spot in the number where the bit is
     * @return a byte that stores 0 or 1 depending on the bit
     **/
    public static byte getBit(long number, int position) 
       throws InvalidSelectionException
    {
       if (position > Long.SIZE || position < 1)
       {
          throw new InvalidSelectionException(position + " is not a valid bit " + 
             "position");
       }
       long mask = 1;
       long value = (mask << (Long.SIZE - position)) & number;
       if (value != 0)
       {
          return 1;
       }
       else
       {
          return 0;
       }
    }
    
    /**
     * Given two 32 bit numbers this will make one long
     * @param left - the number to be placed in the most significant places
     * @param right - the number to be placed in the least significant places
     * @return the 64 bit number
     * */
    public static long makeLong(int left, int right)
    {
       long result = ((long) left) << 32;
       result = result | right;
       return result;
    }
    
    /**
     * Switches the position of bits in a number based on the array.
     * @param original - the number to switch the positions in 
     * @param newPositions - the array with the bits in their new positions.  
     *        Format of the array is that each element of the array has a
     *        number which represents which bit from the original to take.  e.g
     *        if there is an array {64, 63} this will move the last two bits
     *        into the first 2 places
     * @return a long with the bits in their new positions.
     * */
    public static long switchBits(long original, int newPositions[]) 
       throws InvalidSelectionException
    {
       long result = 0;
       for(int i = 0; i < newPositions.length; i++)
       {
          long bit = getBit(original, newPositions[i]);
          result = result | (bit << Long.SIZE - i - 1);
       }
       return result;
    }
    
    /**
     * Returns a string of the number in binary.  Used for debugging
     * @param num - the number to make a string out of.
     * @return num as a String of bits
     * */
    public static String showBinary(long num)
    {
       String result = "";
       while(num != 0)
       {
          result = (num & 1) + result;
          num = num >>> 1;
       }
       return result;
     
    }
 }
