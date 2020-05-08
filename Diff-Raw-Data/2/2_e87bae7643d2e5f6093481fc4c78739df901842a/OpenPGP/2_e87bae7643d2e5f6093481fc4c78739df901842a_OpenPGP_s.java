 import java.math.BigInteger;
 /**
  * This class contains constants and methods specific to the OpenPGP standard.
  * @author - Jonathan Nanney
  * */
 public class OpenPGP
 {
    /** Constant indicating that the packet uses the SHA-1 hash algorithm */
    public static final byte SHA1_CONSTANT = 2;
    /**The left two bits will always be 1 in a new format tag. So it should be 3
    (which is 2 1-bits) shifted five places. */
    public static final byte NEW_TAG_MASK = 3 << 5;
    /** Constant that indicates a packet uses RSA*/
    public static final byte RSA_CONSTANT = 1;
    /** Constant that indicates packet uses 3DES */
    public static final byte TRIPLEDES_CONSTANT = 2;
    /** Tag for signature packets */
    public static final byte SIGNATURE_PACKET_TAG = 2 | NEW_TAG_MASK;
    /** The tag for packets that hold a public key */
    public static final byte PUBLIC_KEY_PACKET_TAG = 6 | NEW_TAG_MASK;
    /** The tag for packets that hold a private key */
    public static final byte PRIVATE_KEY_PACKET_TAG = 5 | NEW_TAG_MASK;
    /** The tag for packets that hold unencrypted data */
    public static final byte LITERAL_DATA_PACKET_TAG = 11 | NEW_TAG_MASK;
    /** The tag for packets that hold a session key encrypted with a public key */
    public static final byte PK_SESSION_KEY_TAG = 1 | NEW_TAG_MASK;
    /** The tag for packets that hold data encrypted with a symmetric algorithm */
    public static final byte SYMMETRIC_DATA_TAG = 9 | NEW_TAG_MASK;
    /** The tag for packets that hold compressed data */
    public static final byte COMPRESSED_DATA_TAG = 8 | NEW_TAG_MASK;
    /** The maximum length that can be expressed in a 1 byte MPI */
    public static final long MAX_ONE_OCTET = 191;
    /** The maximum length that can be expressed in a 2 byte MPI */
    public static final long MAX_TWO_OCTETS = 8383;
    /** The maximum length that can be exprssed with a 5 byte MPI */
   public static final long MAX_FIVE_OCTETS = 0xFFFFFFFF;
    /** The number of bytes in an MPI used for the length of the MPI*/
    public static final byte MPI_LENGTH_BYTES = 2; 
    /** The version number for public key packets */
    public static final byte PUBLIC_KEY_VERSION = 4;
    /** The number of bytes used to express the time */
    public static final byte TIME_BYTES = 4;
    /** The number of bytes that 3DES can encrypt at once */
    public static final byte TRIPLEDES_BLOCK_BYTES = 8;
    
    /**
     * Takes an array of bytes representing an MPI (multiprecision integer) 
     * and returns just the number without the length bytes.  MPIs are unsigned
     * integers with 2 length bytes at the beginning.
     * @param data - the array to get the number from
     * @param start - the index of the array where the MPI starts
     * @return a byte array representing the number.  
     * */
    public static byte[] getMultiprecisionInteger(byte[] data, int start)
    {
       int i = start;
       int mpiLength = (data[i++] << Byte.SIZE) | ((int) data[i++] & Common.BYTE_MASK);
       mpiLength = 0XFFFF & mpiLength;
       byte[] mpi = new byte[mpiLength / Byte.SIZE];
       for(int j = 0; j < mpi.length && i < data.length; i++, j++)
       {
          mpi[j] = data[i];
       }
       return mpi;
    }
 
    /**
     * Takes a number and returns it as an MPI (multiprecision integer).  An 
     * MPI is a unsigned integer with 2 length bytes at the beginning.
     * @param num - the number to turn into an MPI
     * @return a byte array containg the MPI
     * */
    public static byte[] makeMultiprecisionInteger(BigInteger num)
    {
       byte[] temp = num.toByteArray();
       return makeMultiprecisionInteger(temp);
    }
    
    /**
     * Turns a number into a multiprecision integer (MPI).  MPI's are simply 
     * unsigned numbers where the first 2 bytes indicate the length of the MPI
     * in bits.
     * @param num - the number to turn into an MPI
     * @return the created MPI
     * */
    public static byte[] makeMultiprecisionInteger(byte[] num)
    {
       byte[] result = new byte[num.length + OpenPGP.MPI_LENGTH_BYTES];
       int numBits = num.length * Byte.SIZE;
       result[0] = (byte) (numBits >> Byte.SIZE);
       result[1] = (byte) (numBits & Common.BYTE_MASK);
       for(int i = OpenPGP.MPI_LENGTH_BYTES; i < result.length; i++)
       {
          result[i] = num[i - OpenPGP.MPI_LENGTH_BYTES];
       }
       return result;
    }
 
    /**
     * Given the length of a packet in bytes it will convert that length to the
     * format needed for packets.  Old format packets will never and should 
     * never be generated.
     * @param length - the length in bytes of the packet
     * @return the packet length expressed in 1,2 or 5 bytes.
     * */
    public static byte[] makeNewFormatLength(long length)
    {
       byte[] result = new byte[0];
       if(length <= OpenPGP.MAX_ONE_OCTET)
       {
          result = new byte[1];
          result[0] = (byte) length;
       }
       else if(length <= OpenPGP.MAX_TWO_OCTETS)
       {
          result = new byte[2];
          long first = (length >>> Byte.SIZE) + 191;
          long second = (length & Common.BYTE_MASK) - 192;
          result[0] = (byte) (first & Common.BYTE_MASK);
          result[1] = (byte) (second & Common.BYTE_MASK);
       }
       else if(length <= OpenPGP.MAX_FIVE_OCTETS)
       {
          result = new byte[5];
          result[0] = (byte) Common.BYTE_MASK;
          int mask = Common.BYTE_MASK;
          for(int i = 1; i <result.length; i++)
          {
             result[i] = (byte) (length & (mask << ((4-i) * 8)));
          }
       }
       return result;
    }
    
    /**
     * Gets the length of an packet.  New format because PGP used to generate 
     * packets differently.  Packets in the new format will always have a tag
     * where the left two most bits are 1.  This version currently does not 
     * accept old format packets.  
     * @param bytes - the data to get the length from
     * @return the length in bytes of a packet
     * */
    public static int getNewFormatLength(int[] bytes)
    {
       int result = 0;
       if(bytes.length == 1)
       {
          result = bytes[0];
          result = result & Common.BYTE_MASK;
       }
       else if(bytes.length == 2)
       {
          result = ((bytes[0] - 192) << Byte.SIZE) + bytes[1] + 192;
          result = result & Common.TWO_BYTE_MASK;
       }
       else if(bytes.length == 5)
       {
          result = (bytes[1] << 24) | (bytes[2] << 16) | (bytes[3] << Byte.SIZE) | bytes[4];
       }
       return result;
    }
 }
