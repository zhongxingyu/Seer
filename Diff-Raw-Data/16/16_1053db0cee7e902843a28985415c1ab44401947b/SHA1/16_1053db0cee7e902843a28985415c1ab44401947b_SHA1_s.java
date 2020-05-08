 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 
 public class SHA1
 {
    private byte[] original;
    private static final int BLOCK_BYTES = 64;
    private static final int BLOCK_ITERATIONS = 80;
    private static final int LENGTH_BYTES = 8;
    private static final int FUNC_BOUND1 = 20;
    private static final int FUNC_BOUND2 = 40;
    private static final int FUNC_BOUND3 = 60;
    private static final int FUNC_BOUND4 = 80;
 
    public SHA1(byte[] data)
    {
       this.original = pad(data);
    }
 
    public static byte[] pad(byte[] data)
    {
       ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
       int minLength = data.length + LENGTH_BYTES + 1;
       int zerosToAdd = BLOCK_BYTES - (minLength % BLOCK_BYTES);
       
       try
       {
          byteStream.write(data);
          byteStream.write(1 << 7);
          byte[] zeros = new byte[zerosToAdd];
          Arrays.fill(zeros, (byte) 0);
          byteStream.write(zeros);
          byteStream.write(Common.makeLongBytes(data.length * Byte.SIZE));
       }
       catch(IOException ioe)
       {
          System.err.println(ioe.getMessage());
       }
       return byteStream.toByteArray();
    }
 
    public byte[] compute()
    {
       int blocks = original.length / BLOCK_BYTES;
       int[] hashValues = new int[]{0x67452301, 0xefcdab89, 0x98badcfe, 
                                    0x10325476, 0xc3d2e1f0};
       for(int i = 0; i < blocks; i++)
       {
          int a = hashValues[0]; 
          int b = hashValues[1]; 
          int c = hashValues[2]; 
          int d = hashValues[3]; 
          int e = hashValues[4]; 
          int temp;
          int[] w = messageScheduler(original, i);
          for(int j = 0; j < BLOCK_ITERATIONS; j++)
          {
             temp = Common.rotateLeftCircular(a, 5) + sha1Function(j, b, c, d) +
                    constant(j) + w[j];
             e = d;
             d = c;
             c = Common.rotateLeftCircular(b, 30);
             b = a;
             a = temp;
          }
          hashValues[0] = a + hashValues[0];
          hashValues[1] = b + hashValues[1];
          hashValues[2] = c + hashValues[2];
          hashValues[3] = d + hashValues[3];
          hashValues[4] = e + hashValues[4];
       }
       byte[] result = new byte[20];
       int counter = 0;
       for(int i = 0; i < hashValues.length; i++)
       {
          byte[] temp = Common.makeIntBytes(hashValues[i]);
          for(int j = 0; j < temp.length; j++)
          {
             result[counter] = temp[j];
             counter++;
          }
       }
       return result;
    }
 
    /**
     * This method does addition modulo 2^32 as required by the SHA-1 standard.
     * @param first - the first number to add
     * @param second - the second number to add
     * @return the two numbers added together mod 2^32
     * */
   private int addMod2(int first, int second)
    {
       //Integer.MAX holds 2^32 - 1 since it's giving the signed max.  
       //The unsigned value will be double of that + 1
      long MAX_UNSIGNED_INT = (Integer.MAX_VALUE + 1) * 2;
       //Holds the maximum value of an int.
       long FULL_INT = 0xFFFFFFFFL;
       long total = (first & FULL_INT) + (second & FULL_INT);
       return (int) (total % MAX_UNSIGNED_INT);
    }
 
    private int sha1Function(int iteration, int first, int second, int third)
    {
       if(iteration < 0 || iteration >= FUNC_BOUND4)
       {
          throw new InvalidSelectionException("Invalid iteration in SHA1");
       }
       else if (iteration < FUNC_BOUND1)
       {
          return (first & second) ^ (~first & third);
       }
       else if (iteration < FUNC_BOUND2)
       {
          return first ^ second ^ third;
       }
       else if (iteration < FUNC_BOUND3)
       {
          return (first & second) ^ (first & third) ^ (second & third);
       }
       else if (iteration < FUNC_BOUND4)
       {
          return (first ^ second ^ third);
       }
       else
       {
          return 0;
       }
    }
 
    public int[] messageScheduler(byte[] data, int start)
    {
       int BOUNDARY = 16;
       int PREV_INDEX1 = 3;
       int PREV_INDEX2 = 8;
       int PREV_INDEX3 = 14;
       int PREV_INDEX4 = 16;
 
       int[] result = new int[BLOCK_ITERATIONS];
       int bytesInInt = Integer.SIZE / Byte.SIZE;
       int i;
       for(i = 0; i < BOUNDARY; i++)
       {
          result[i] = Common.makeBytesInt(data, i, i + bytesInInt);
       }
       for(; i < result.length; i++)
       {
          int temp = result[i - PREV_INDEX1] ^ result[i - PREV_INDEX2] ^ 
              result[i - PREV_INDEX3] ^ result[i - PREV_INDEX4];
          result[i] = Common.rotateLeftCircular(temp, 1);
       }
       return result;
    }
 
    public int constant(int iteration)
    {
       int FIRST_BOUNDARY = 20;
       int SECOND_BOUNDARY = 40;
       int THIRD_BOUNDARY = 60;
       if(iteration < FIRST_BOUNDARY)
       {
          return 0x5a827999;
       }
       else if(iteration < SECOND_BOUNDARY)
       {
          return 0x6ed9eba1;
       }
       else if(iteration < THIRD_BOUNDARY)
       {
          return 0x8f1bbcdc;
       }
       else
       {
          return 0xca62c1d6;
       }
    }
 }
