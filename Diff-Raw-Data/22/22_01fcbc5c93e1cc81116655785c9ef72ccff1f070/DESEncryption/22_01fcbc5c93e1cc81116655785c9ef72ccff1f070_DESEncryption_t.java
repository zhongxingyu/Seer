 public class DESEncryption
 {
    private long plaintext;
    private long cyphertext;
    private DESKey key;
 
    private final int[] flipPositions = {33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 
       43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 
       61, 62, 63, 64, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 
       17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
 
    private final int[] initialPermutation = 
    {
       58, 50, 42, 34, 26, 18, 10, 2,
       60, 52, 44, 36, 28, 20, 12, 4,
       62, 54, 46, 38, 30, 22, 14, 6,
       64, 56, 48, 40, 32, 24, 16, 8,
       57, 49, 41, 33, 25, 17, 9, 1,
       59, 51, 43, 35, 27, 19, 11, 3,
       61, 53, 45, 37, 29, 21, 13, 5,
       63, 55, 47, 39, 31, 23, 15, 7
    };
 
    private final int[] inversePermutation = 
    {
       40, 8, 48, 16, 56, 24, 64, 32, 
       39, 7, 47, 15, 55, 23, 63, 31, 
       38, 6, 46, 14, 54, 22, 62, 30, 
       37, 5, 45, 13, 53, 21, 61, 29, 
       36, 4, 44, 12, 52, 20, 60, 28, 
       35, 3, 43, 11, 51, 19, 59, 27, 
       34, 2, 42, 10, 50, 18, 58, 26, 
       33, 1, 41, 9, 49, 17, 57, 25
    };
 
    private final int[] expandPositions =
    {
       32, 1, 2, 3, 4, 5,
       4, 5, 6, 7, 8, 9,
       8, 9, 10, 11, 12, 13,
       12, 13, 14, 15, 16, 17,
       16, 17, 18, 19, 20, 21,
       20, 21, 22, 23, 24, 25,
       24, 25, 26, 27, 28, 29,
       28, 29, 30, 31, 32, 1
    };
 
    public DESEncryption(long plaintext)
    {
       this.plaintext = plaintext;
       //key = new DESKey();
       key = new DESKey(0x3b3898371520f75eL);
    }
 
    public long encrypt() throws InvalidNumberException
    {
       long result = Common.switchBits(plaintext, initialPermutation);
       long left = Common.getBits(result, 1, 33);
       long right = Common.getBits(result, 33, 65);
       for (int i = 1; i <= 16; i++)
       {
          result = encryptionIteration(left, right, i);
          //System.out.println("i is " + i);
          //System.out.println("Left " + Common.showBinary(left) + " Right: " + Common.showBinary(right));
          left = Common.getBits(result, 1, 33);
          //System.out.println("Left   " + Common.showBinary(left));
          right = Common.getBits(result, 33, 65);
          //System.out.println("Right  " + Common.showBinary(right));
       }
       result = Common.switchBits(result, flipPositions);
       result = Common.switchBits(result, inversePermutation);
       cyphertext = result;
       return result;
    }
 
    public long encryptionIteration(long left, long right, int iteration)
       throws InvalidNumberException
    {
      System.out.println("Iteration " + iteration);
       long result = ((long) right) << Integer.SIZE - 1;
       long shortkey = key.keyScheduler(iteration);  
      long cypherResult = cypherFunction(right, shortkey);
      System.out.println("CypherFunction : " + Common.showBinary(cypherResult));
      System.out.println("Left           : " + Common.showBinary(left));
      result = result | (left ^ cypherResult);
      System.out.println("Result         : " + Common.showBinary(result));
       return result;
    }
 
   private long cypherFunction(long block, long shortKey) 
       throws InvalidNumberException
    {
      System.out.println("Block is : " + Common.showBinary((block)));
       long result = 0;
       block = block << 32;
       long expandedBlock = Common.switchBits(block, expandPositions);
       expandedBlock = expandedBlock >>> 16;
       long temp = shortKey ^ expandedBlock;
       temp = temp << 16;
       for(int i = 0; i < 8; i++)
       {
          byte sixBits =  (byte) Common.getBits(temp, i * 6 + 1, (i + 1) * 6 + 1);
          byte[][] sTable = DESArrays.getSelectionTables(i + 1);
          byte fourBits = sFunction(sixBits, sTable);
          result = result << 4;
          result = result | fourBits;
       }
       result = result << 32;
       result = Common.switchBits(result, DESArrays.getPermutationFunction());
       result = result >>> 32;
      return result;
    }
 
    public long decrypt() throws InvalidNumberException
    {
       long result = Common.switchBits(cyphertext, initialPermutation);
       for (int i = 16; i >= 1; i--)
       {
          int left = (int) Common.getBits(result, 1, 33);
          int right = (int) Common.getBits(result, 33, 65);
          result = encryptionIteration(right, left, i);
       }
       result = Common.switchBits(result, flipPositions);
       result = Common.switchBits(result, inversePermutation);
       return result;
    }
    
 
 
    public byte sFunction(byte sixBits, byte[][] sTable) 
       throws InvalidNumberException
    {
       byte firstBitPosition = 3;
       byte lastBitPosition = 8;
       //get first and last bit as the row, get the middle 4 bits as the col
       int row = (Common.getBit(sixBits, firstBitPosition) << 1) | 
          (Common.getBit(sixBits, lastBitPosition));
       int col = 0;
       for(int i = firstBitPosition + 1; i < lastBitPosition; i++)
       {
          col = col | (Common.getBit(sixBits, i) << (7 - i));
       }
       return sTable[row][col];
    }
 
 }
