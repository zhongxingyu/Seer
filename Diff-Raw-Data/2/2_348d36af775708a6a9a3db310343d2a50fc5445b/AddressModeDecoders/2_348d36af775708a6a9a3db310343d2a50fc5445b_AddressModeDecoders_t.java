 package br.com.leandromoreira.jdcpu16br.cpu;
 
 import br.com.leandromoreira.jdcpu16br.misc.HexadecimalUtil;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 public class AddressModeDecoders {
 
     private static final int NUMBER_OF_DECODERS = 0x40;
     private static final int POP = 0x18;
     private static final int PEEK = 0x19;
     private static final int PUSH = 0x1A;
     private static final int SP_DECODER = 0x1B;
     private static final int PC_DECODER = 0x1C;
     private static final int O_DECODER = 0x1D;
     private static final int NEXT_WORD_INDIRECT = 0x1E;
     private static final int NEXT_WORD = 0x1F;
     private final CPU cpu;
     private final HexadecimalUtil formatter = new HexadecimalUtil();
     private final String[] literalRegisterFor = new String[]{"A", "B", "C", "X", "Y", "Z", "I", "J"};
 
     public AddressModeDecoders(final CPU cpu) {
         checkNotNull(cpu);
         this.cpu = cpu;
     }
 
     public AddressModeDecoder[] all() {
         final AddressModeDecoder[] decoder = new AddressModeDecoder[NUMBER_OF_DECODERS];
         fillDecoderDirectRegister(decoder);
         fillDecoderIndirectRegister(decoder);
         fillDecoderIndirectNextWordPlusRegister(decoder);
         fillDecoderLiteral(decoder);
         decoder[POP] = new AddressModeDecoder(POP) {
 
             @Override
             public void write(final int value) {
                 cpu.memory().writeAt(cpu.popStackPointer(), value);
             }
 
             @Override
             public int read() {
                 return cpu.memory().readFrom(cpu.popStackPointer());
             }
         };
         decoder[PEEK] = new AddressModeDecoder(PEEK) {
 
             @Override
             public void write(final int value) {
                 cpu.memory().writeAt(cpu.getStackPointer(), value);
             }
 
             @Override
             public int read() {
                 return cpu.memory().readFrom(cpu.getStackPointer());
             }
         };
         decoder[PUSH] = new AddressModeDecoder(PUSH) {
 
             @Override
             public void write(final int value) {
                 cpu.memory().writeAt(cpu.pushStackPointer(), value);
             }
 
             @Override
             public int read() {
                 return cpu.memory().readFrom(cpu.pushStackPointer());
             }
         };
         decoder[SP_DECODER] = new AddressModeDecoder(SP_DECODER) {
 
             @Override
             public void write(final int value) {
                 cpu.setStackPointer(value);
             }
 
             @Override
             public int read() {
                 return cpu.getStackPointer();
             }
         };
        
        //TODO: this decoder can't sum when setted! I guess
         decoder[PC_DECODER] = new AddressModeDecoder(PC_DECODER) {
 
             @Override
             public void write(final int value) {
                 cpu.setProgramCounter(value);
             }
 
             @Override
             public int read() {
                 return cpu.getProgramCounter();
             }
         };
         decoder[O_DECODER] = new AddressModeDecoder(O_DECODER) {
 
             @Override
             public void write(final int value) {
                 cpu.setOverflow(value);
             }
 
             @Override
             public int read() {
                 return cpu.getOverflow();
             }
         };
         decoder[NEXT_WORD_INDIRECT] = new AddressModeDecoder(NEXT_WORD_INDIRECT) {
 
             @Override
             public void write(final int value) {
                 representation = indirectFormat(formatter.toHexadecimal(cpu.memory().readFrom(getMyProgramCounter() + 1)));
                 cpu.memory().writeAt(cpu.memory().readFrom(getMyProgramCounter() + 1), value);
             }
 
             @Override
             public int read() {
                 representation = indirectFormat(formatter.toHexadecimal(cpu.memory().readFrom(getMyProgramCounter() + 1)));
                 return cpu.memory().readFrom(cpu.memory().readFrom(getMyProgramCounter() + 1));
             }
 
             @Override
             public int size() {
                 return 1;
             }
 
             @Override
             public int extraCycles() {
                 return 1;
             }
         };
         decoder[NEXT_WORD] = new AddressModeDecoder(NEXT_WORD) {
 
             @Override
             public void write(final int value) {
             }
 
             @Override
             public int read() {
                 representation = formatter.toHexadecimal(cpu.memory().readFrom(getMyProgramCounter() + 1));
                 return cpu.memory().readFrom(getMyProgramCounter() + 1);
             }
 
             @Override
             public int size() {
                 return 1;
             }
         };
         return decoder;
     }
 
     private void fillDecoderDirectRegister(final AddressModeDecoder[] decoder) {
         for (int registerIndex = 0x00; registerIndex <= 0x07; registerIndex++) {
             decoder[registerIndex] = new AddressModeDecoder(registerIndex) {
 
                 @Override
                 public void write(final int value) {
                     representation = literalRegisterFor[index];
                     cpu.setRegister(index, value);
                 }
 
                 @Override
                 public int read() {
                     representation = literalRegisterFor[index];
                     return cpu.register(index);
                 }
             };
         }
     }
 
     private void fillDecoderIndirectRegister(final AddressModeDecoder[] decoder) {
         for (int registerIndex = 0x08; registerIndex <= 0x0F; registerIndex++) {
             decoder[registerIndex] = new AddressModeDecoder(registerIndex) {
 
                 @Override
                 public void write(int value) {
                     representation = indirectFormat(literalRegisterFor[index - 0x8]);
                     cpu.memory().writeAt(cpu.register(index - 0x8), value);
                 }
 
                 @Override
                 public int read() {
                     representation = indirectFormat(literalRegisterFor[index - 0x8]);
                     return cpu.memory().readFrom(cpu.register(index - 0x8));
                 }
             };
         }
     }
 
     private void fillDecoderLiteral(final AddressModeDecoder[] decoder) {
         for (int registerIndex = 0x20; registerIndex <= 0x3F; registerIndex++) {
             decoder[registerIndex] = new AddressModeDecoder(registerIndex) {
 
                 @Override
                 public void write(int value) {
                 }
 
                 @Override
                 public int read() {
                     representation = formatter.toHexadecimal(index - 0x20);
                     return index - 0x20;
                 }
             };
         }
     }
 
     private void fillDecoderIndirectNextWordPlusRegister(final AddressModeDecoder[] decoder) {
         for (int registerIndex = 0x10; registerIndex <= 0x17; registerIndex++) {
             decoder[registerIndex] = new AddressModeDecoder(registerIndex) {
 
                 @Override
                 public void write(int value) {
                     cpu.memory().writeAt(nextWordPlusRegister(), value);
                 }
 
                 @Override
                 public int read() {
                     return cpu.memory().readFrom(nextWordPlusRegister());
                 }
 
                 private int nextWordPlusRegister() {
                     final int nextWord = getMyProgramCounter() + 1;
                     final int correctIndex = index - 0x10;
                     representation = indirectFormat(formatter.toHexadecimal(cpu.memory().readFrom(nextWord)) + " + " + literalRegisterFor[correctIndex]);
                     return cpu.memory().readFrom(nextWord) + cpu.register(correctIndex);
                 }
 
                 @Override
                 public int size() {
                     return 1;
                 }
 
                 @Override
                 public int extraCycles() {
                     return 1;
                 }
             };
         }
     }
     
     private String indirectFormat(final Object value){
         return "[" + value + "]";
     }
 }
