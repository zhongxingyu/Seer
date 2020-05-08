 package ru.alepar.zx80.op.factory;
 
 import org.junit.Test;
 import ru.alepar.zx80.Speccy;
 import ru.alepar.zx80.SpeccyFactory;
 import ru.alepar.zx80.base.Cell;
 import ru.alepar.zx80.base.Word;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 import static ru.alepar.zx80.base.Address.*;
 import static ru.alepar.zx80.cpu.Register.*;
 
 /**
  * User: alepar
  * Date: Sep 23, 2010
  */
 public class LdIndirectRegTest {
 
     private final Speccy speccy = SpeccyFactory.buildSpeccy();
     private final OpFactory opFactory = new LdIndirectReg(speccy);
 
     @Test
    public void ldBCToAExecutedProperly() {
         Cell[] opcode = new Cell[]{new Cell()};
         opcode[0].setValue((byte) 0x0a); // (BC) -> A
 
         assertThat(opFactory.accept(opcode), equalTo(1));
 
         Word word = speccy.getRegistryBlock().getWord(B, C);
         word.setValue(0x01);
         speccy.getMemory().getCell(address(word)).setValue((byte) 0xca);
 
         opFactory.build(opcode).execute(speccy);
 
         assertThat(speccy.getRegistryBlock().getCell(A).getValue(), equalTo((byte) 0xca));
     }
 
     @Test
    public void ldAtoDEExecutedProperly() {
         Cell[] opcode = new Cell[]{new Cell()};
         opcode[0].setValue((byte) 0x12); // A -> (DE)
 
         assertThat(opFactory.accept(opcode), equalTo(1));
 
         Word word = speccy.getRegistryBlock().getWord(D, E);
         word.setValue(0x01);
         speccy.getRegistryBlock().getCell(A).setValue((byte) 0xca);
         opFactory.build(opcode).execute(speccy);
 
         assertThat(speccy.getMemory().getCell(address(word)).getValue(), equalTo((byte) 0xca));
     }
 
 
 }
