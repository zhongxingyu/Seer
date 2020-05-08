 package br.com.leandromoreira.jdcpu16br;
 
 import static br.com.leandromoreira.jdcpu16br.CPU.*;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestAddressModeCPU {
 
     private final CPU cpu = new CPU();
     private final Memory memory = cpu.memory();
 
     @Before
     public void setup() {
         cpu.reset();
     }
 
     @Test
     public void it_performs_set_a_to_direct_registers() {
         cpu.setRegister(A, 0xF);
         cpu.setRegister(B, 0xA);
         memory.writeAt(0x0000, 0b000001_000000_0001);
         cpu.step();
         assertThat(cpu.register(A), is(cpu.register(B)));
     }
 
     @Test
     public void it_performs_set_a_to_indirect_registers() {
         cpu.setRegister(A, 0x0005);
         memory.writeAt(0x0005, 0x4);
         memory.writeAt(0x0000, 0b001000_000000_0001);
         cpu.step();
         assertThat(cpu.register(A), is(0x4));
     }

     @Test
     public void it_performs_set_a_to_next_word() {
         memory.writeAt(0x0000, 0x7C01);
         memory.writeAt(0x0001, 0x0030);
         cpu.step();
         assertThat(cpu.register(A), is(0x0030));
     }
 }
