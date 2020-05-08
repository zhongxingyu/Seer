 package edu.osu.cse.mmxi;
 
 import edu.osu.cse.mmxi.machine.Machine;
 import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;
 
 public class Console {
 
     public Console(final Machine m, final String file) {
         if (file == null) {
             m.ui.warn("No files given!");
             System.exit(1);
         }
 
         int memTrack = -1;
         while (!m.hasHalted()) {
             short mem;
             if (memTrack == -1)
                 mem = (short) ((m.getPCRegister().getValue() & 0xfff8) - 8);
             else
                 mem = (short) memTrack;
             m.ui.print("\n                           ");
             for (int j = 0; j < 8; j++)
                 m.ui.print(" --" + (mem + j & 7) + "-");
             m.ui.print("\n");
             for (int i = 0; i < 4; i++) {
                 m.ui.print("R" + 2 * i + ": ");
                 m.ui.print(MemoryUtilities.uShortToHex(m.getRegister(2 * i).getValue())
                     + "  ");
                 m.ui.print("R" + (2 * i + 1) + ": ");
                 m.ui.print(MemoryUtilities.uShortToHex(m.getRegister(2 * i + 1)
                     .getValue()) + "   ");
                 m.ui.print(MemoryUtilities.uShortToHex((short) (mem + 8 * i)) + " | ");
                 for (int j = 0; j < 8; j++)
                     m.ui.print(MemoryUtilities.uShortToHex(m.getMemory((short) (mem + 8
                         * i + j)))
                         + " ");
                 m.ui.print("\n");
             }
             m.ui.print("\n          PC: "
                 + MemoryUtilities.uShortToHex(m.getPCRegister().getValue()) + "  ");
             m.ui.print((m.getFlags().getN() ? "n" : "-")
                 + (m.getFlags().getZ() ? "z" : "-") + (m.getFlags().getP() ? "p" : "-")
                 + "  ");
             m.ui.print(MemoryUtilities.uShortToHex(m.getMemory(m.getPCRegister()
                 .getValue())) + ": ");
             m.ui.print(m.alu.readInstructionAt(m.getPCRegister().getValue()) + "\n\n");
             while (true) {
                 String s = m.ui.prompt("Press ENTER to step, "
                     + "or a hex address or 'pc' to track memory:\n> ");
                 if (s.length() != 0) {
                     memTrack = -2;
                     while (true) {
                         try {
                             if (s.equalsIgnoreCase("pc"))
                                 memTrack = -1;
                             else {
                                 memTrack = Integer.parseInt(s, 16);
                                 if ((memTrack & 0xffff0000) != 0)
                                     memTrack = -2;
                             }
                         } catch (final NumberFormatException e) {
                         }
                         if (memTrack == -2)
                             s = m.ui.prompt("Invalid hex or "
                                 + "number out of range.\n> ");
                         else
                             break;
                     }
                     if (memTrack == -1)
                         mem = (short) ((m.getPCRegister().getValue() & 0xfff8) - 8);
                     else
                         mem = (short) memTrack;
                     m.ui.print("\n      ");
                     for (int j = 0; j < 16; j++)
                         m.ui.print(" --"
                             + Integer.toHexString(mem + j & 15).toUpperCase() + "-");
                     m.ui.print("\n");
                     for (int i = 0; i < 8; i++) {
                         m.ui.print(MemoryUtilities.uShortToHex((short) (mem + 16 * i))
                             + " | ");
                         for (int j = 0; j < 16; j++)
                             m.ui.print(MemoryUtilities.uShortToHex(m
                                 .getMemory((short) (mem + 16 * i + j))) + " ");
                         m.ui.print("\n");
                     }
                     m.ui.print("\n");
                 } else
                     break;
             }
 
             m.stepClock();
         }
         m.ui.print("Machine halted after " + (m.clockCount() - 1) + " steps.");
     }
 
 }
