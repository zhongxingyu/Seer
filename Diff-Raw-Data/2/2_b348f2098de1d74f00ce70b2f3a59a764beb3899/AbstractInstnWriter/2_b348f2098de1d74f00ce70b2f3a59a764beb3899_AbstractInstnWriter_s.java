 /*
  *  Copyright (C) 2010 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fr.jamgotchian.abcd.core.output;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.List;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public abstract class AbstractInstnWriter implements InstnWriter {
 
     protected final Writer writer;
 
     public AbstractInstnWriter(Writer writer) {
         this.writer = writer;
     }
 
     abstract void writeIndex(int index) throws IOException;
 
     abstract void writeIndent(int count) throws IOException;
 
     void writeIndent() throws IOException {
         writeIndent(1);
     }
 
     abstract void writeOpcode(int opcode) throws IOException;
 
     abstract void writeLabel(int label) throws IOException;
 
     abstract void writeLt() throws IOException;
 
     abstract void writeGt() throws IOException;
 
     abstract void writeLineOpcode() throws IOException;
 
     void writeSpace() throws IOException {
         writer.write(" ");
     }
 
     public void writeFieldOrMethodInstn(int index, int opcode, String scope, String fieldOrMethodName) throws IOException {
         writeIndex(index);
         writeIndent();
         writeOpcode(opcode);
         writeSpace();
         writeLt();
         writer.append(scope);
         writer.append(".");
         writer.append(fieldOrMethodName);
         writeGt();
     }
 
     public void writeIIncInstn(int index, int opcode, int var, int incr) throws IOException {
         writeIndex(index);
         writeIndent();
         writeOpcode(opcode);
         writeSpace();
         writer.append(Integer.toString(var));
         writeSpace();
         writer.append(Integer.toString(incr));
     }
 
     public void writeInstn(int index, int opcode) throws IOException {
         writeIndex(index);
         writeIndent();
         writeOpcode(opcode);
     }
 
     public void writeIntInstn(int index, int opcode, int operand) throws IOException {
         writeIndex(index);
         writeIndent();
         writeOpcode(opcode);
         writeSpace();
         writer.append(Integer.toString(operand));
     }
 
     public void writerJumpInstn(int index, int opcode, int label) throws IOException {
         writeIndex(index);
         writeIndent();
         writeOpcode(opcode);
         writeSpace();
         writeLabel(label);
     }
 
     public void writeLabelInstn(int index, int label) throws IOException {
         writeIndex(index);
         writeLabel(label);
     }
 
     public void writeLdcInstn(int index, int opcode, Object cst) throws IOException {
         writeIndex(index);
         writeIndent();
         writeOpcode(opcode);
         writeSpace();
         writer.append(cst.toString());
     }
 
     public void writeLookupSwitchInstn(int index, int opcode, List<Integer> keys,
                                        int defaultLabel, List<Integer> labels) throws IOException {
         writeIndex(index);
         writeIndent();
         writeOpcode(opcode);
         writeSpace();
         writer.append(Integer.toString(keys.size()));
         writeEol();
         for (int i = 0; i < keys.size(); i++) {
             writeIndex(-1); // just for alignment
             writeIndent(5);
             writer.append(Integer.toString(keys.get(i)));
             writer.append(':');
             writeSpace();
             writeLabel(labels.get(i));
             writeEol();
         }
         writeIndex(-1); // just for alignment
         writeIndent(5);
         writer.append("default:");
         writeSpace();
         writeLabel(defaultLabel);
     }
 
     public void writeMultiANewArrayInstn(int index, int opcode, String type, int dims) throws IOException {
         writeIndex(index);
         writeIndent();
         writeOpcode(opcode);
         writeSpace();
         writer.write(type);
         writeSpace();
         writer.write(Integer.toString(dims));
     }
 
     public void writeTableSwitchInstn(int index, int opcode, int min, int max,
                                       int defaultLabel, List<Integer> labels) throws IOException {
         writeIndex(index);
         writeIndent();
         writeOpcode(opcode);
         writeSpace();
         writer.append(Integer.toString(min));
         writer.append(" to ");
         writer.append(Integer.toString(max));
         writeEol();
         for (int i = min; i <= max; i++) {
             writeIndex(-1); // just for alignment
             writeIndent(5);
             writer.append(Integer.toString(i));
             writer.append(':');
             writeSpace();
            writeLabel(labels.get(i));
             writeEol();
         }
         writeIndex(-1); // just for alignment
         writeIndent(5);
         writer.append("default:");
         writeSpace();
         writeLabel(defaultLabel);
     }
 
     public void writeTypeInstn(int index, int opcode, String type) throws IOException {
         writeIndex(index);
         writeIndent();
         writeOpcode(opcode);
         writeSpace();
         writeLt();
         writer.append(type);
         writeGt();
     }
 
     public void writeVarInstn(int index, int opcode, int var) throws IOException {
         writeIndex(index);
         writeIndent();
         writeOpcode(opcode);
         writeSpace();
         writer.append(Integer.toString(var));
     }
 
     public void writeLineInstn(int index, int line, int startLabel) throws IOException {
         writeIndex(index);
         writeIndent();
         writeLineOpcode();
         writeSpace();
         writer.write(Integer.toString(line));
         writeSpace();
         writeLabel(startLabel);
     }
 
     public void writeFrameInstn(int index) throws IOException {
         writeIndex(index);
     }
 }
