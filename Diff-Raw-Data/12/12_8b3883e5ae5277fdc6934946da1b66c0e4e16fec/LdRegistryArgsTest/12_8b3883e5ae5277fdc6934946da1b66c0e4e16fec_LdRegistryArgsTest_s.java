 package ru.alepar.zx80.op.factory;
 
 import org.junit.Test;
 import ru.alepar.zx80.Speccy;
 import ru.alepar.zx80.SpeccyFactory;
 import ru.alepar.zx80.base.Cell;
 import ru.alepar.zx80.base.Word;
 import ru.alepar.zx80.cpu.Register;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 import static ru.alepar.zx80.base.Address.*;
 
 /**
  * User: alepar
  * Date: Sep 17, 2010
  */
 public class LdRegistryArgsTest {
 
     private final Speccy speccy = SpeccyFactory.buildSpeccy();
     private final OpFactory opFactory = new LdRegistryArgs(speccy);
 
     @Test
     public void ldBToAExecutedProperly() {
         Cell[] opcode = new Cell[]{new Cell()};
         opcode[0].setValue((byte) 0x78); //B -> A
 
         assertThat(opFactory.accept(opcode), equalTo(1));
         speccy.getRegistryBlock().getCell(Register.B).setValue((byte) 0xca);
         opFactory.build(opcode).execute(speccy);
         assertThat(speccy.getRegistryBlock().getCell(Register.A).getValue(), equalTo((byte) 0xca));
     }
 
     @Test
     public void ldFToHExecutedProperly() {
         Cell[] opcode = new Cell[]{new Cell()};
         opcode[0].setValue((byte) 0x64); //F -> H
 
         assertThat(opFactory.accept(opcode), equalTo(1));
         speccy.getRegistryBlock().getCell(Register.F).setValue((byte) 0xca);
         opFactory.build(opcode).execute(speccy);
         assertThat(speccy.getRegistryBlock().getCell(Register.H).getValue(), equalTo((byte) 0xca));
     }
 
     @Test
     public void ldAToLExecutedProperly() {
         Cell[] opcode = new Cell[]{new Cell()};
         opcode[0].setValue((byte) 0x6f); //A -> L
 
         assertThat(opFactory.accept(opcode), equalTo(1));
         speccy.getRegistryBlock().getCell(Register.A).setValue((byte) 0xca);
         opFactory.build(opcode).execute(speccy);
         assertThat(speccy.getRegistryBlock().getCell(Register.L).getValue(), equalTo((byte) 0xca));
     }
 
     @Test
    public void ldHLToAExecutedProperly() {
         Cell[] opcode = new Cell[]{new Cell()};
         opcode[0].setValue((byte) 0x7e); // (HL) -> A
 
         assertThat(opFactory.accept(opcode), equalTo(1));
 
         Word word = speccy.getRegistryBlock().getWord(Register.H, Register.L);
         word.setValue(0x01);
         speccy.getMemory().getCell(address(word)).setValue((byte) 0xca);
 
         opFactory.build(opcode).execute(speccy);
 
         assertThat(speccy.getRegistryBlock().getCell(Register.A).getValue(), equalTo((byte) 0xca));
     }
 
 
 }
