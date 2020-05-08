 /*
  *  This file is part of the jasm project (http://code.google.com/p/jasm).
  *
  *  This file is licensed to you under the BSD License; You may not use
  *  this file except in compliance with the License. See the LICENSE.txt
  *  file distributed with this work for a copy of the License and information
  *  regarding copyright ownership.
  */
 package jasm.x86.dis;
 
 import jasm.Argument;
 import jasm.Assembler;
 import jasm.AssemblyException;
 import jasm.SymbolSet;
 import jasm.WordWidth;
 import jasm.amd64.AMD64GeneralRegister8;
 import jasm.dis.DecoderException;
 import jasm.dis.DisassembledInstruction;
 import jasm.dis.Disassembler;
 import jasm.tools.Assembly;
 import jasm.tools.Immediate16Argument;
 import jasm.tools.Immediate32Argument;
 import jasm.tools.Immediate64Argument;
 import jasm.tools.Immediate8Argument;
 import jasm.tools.cisc.x86.ModCase;
 import jasm.tools.cisc.x86.RMCase;
 import jasm.tools.cisc.x86.SibBaseCase;
 import jasm.tools.cisc.x86.X86Assembly;
 import jasm.tools.cisc.x86.X86EnumerableParameter;
 import jasm.tools.cisc.x86.X86Field;
 import jasm.tools.cisc.x86.X86NumericalParameter;
 import jasm.tools.cisc.x86.X86Opcode;
 import jasm.tools.cisc.x86.X86Parameter;
 import jasm.tools.cisc.x86.X86Template;
 import jasm.util.EndianUtil;
 import jasm.util.HexByte;
 import jasm.util.StaticLoophole;
 import jasm.x86.X86InstructionPrefix;
 import java.io.BufferedInputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A disassembler for x86 instructions.
  *
  * @see Disassembler
  * @see X86DisassembledInstruction
  */
 public abstract class X86Disassembler<Template_Type extends X86Template<Template_Type>, DisassembledInstruction_Type extends DisassembledInstruction<Template_Type>>
     extends Disassembler<Template_Type, DisassembledInstruction_Type> {
 
   private static final int MORE_THAN_ANY_INSTRUCTION_LENGTH = 100;
   private int _currentOffset;
 
   protected X86Disassembler(Assembly<Template_Type> assembly, WordWidth addressWidth) {
     super(assembly, addressWidth);
   }
 
   private X86InstructionHeader decodeHeader(final InputStream stream)
       throws IOException, DecoderException {
 
     boolean hasAddressSizePrefix = false;
     X86InstructionPrefix operandSizePrefix = null;
     HexByte rexPrefix = null;
     X86InstructionPrefix group1Prefix = null;
     X86InstructionPrefix group2Prefix = null;
     HexByte opcode1;
     HexByte opcode2 = null;
 
     final X86InstructionPrefix[] prefixes = X86InstructionPrefix.values();
 
     int byteValue;
     boolean matched = false;
     boolean checkedPrefixes = false;
     while ((byteValue = stream.read()) != -1 && !matched) {
       checkedPrefixes = true;
       final HexByte hexByte = HexByte.values()[byteValue];
       for (X86InstructionPrefix prefix : prefixes) {
         if (hexByte == prefix.getValue()) {
           final int group = prefix.getGroup();
           if (1 == group) {
             if (null != group1Prefix) {
               throw new DecoderException("Multiple group 1 prefixes specified");
             }
             group1Prefix = prefix;
           } else if (2 == group) {
             if (null != group2Prefix) {
               throw new DecoderException("Multiple group 2 prefixes specified");
             }
             group2Prefix = prefix;
           } else if (3 == group) {
             if (null != operandSizePrefix) {
               throw new DecoderException("Multiple group 3 prefixes specified");
             }
             operandSizePrefix = prefix;
           } else { //group == 4
             if (hasAddressSizePrefix) {
               throw new DecoderException("Multiple group 4 prefixes specified");
             }
            hasAddressSizePrefix = false;
           }
           matched = true;
           break;
         }
       }
       if (!matched) break;
     }
     if (byteValue < 0) {
       if (!checkedPrefixes) return null;
       else throw new EOFException();
     }
 
     HexByte hexByte = HexByte.values()[byteValue];
 
     //Decode rex prefix for amd64/ia32e
     if (WordWidth.BITS_64 == addressWidth() && X86Opcode.isRexPrefix(hexByte)) {
       rexPrefix = hexByte;
       byteValue = stream.read();
       if (-1 == byteValue) throw new EOFException();
       hexByte = HexByte.values()[byteValue];
     }
 
     opcode1 = hexByte;
     if (X86Opcode.isStandardOpcode2Prefix(hexByte)) {
       byteValue = stream.read();
       if (-1 == byteValue) throw new EOFException();
       opcode2 = HexByte.values()[byteValue];
     }
 
     return new X86InstructionHeader(opcode1,
                                     opcode2,
                                     hasAddressSizePrefix,
                                     operandSizePrefix,
                                     rexPrefix,
                                     group1Prefix,
                                     group2Prefix);
   }
 
   private List<Argument> scanArguments(BufferedInputStream stream, Template_Type template, X86InstructionHeader header, byte modRMByte, byte sibByte) throws IOException {
     final ArrayList<Argument> arguments = new ArrayList<Argument>();
     final byte rexByte = (header.rexPrefix() != null) ? header.rexPrefix().byteValue() : 0;
     for (X86Parameter parameter : template.parameters()) {
       int value = 0;
       switch (parameter.place()) {
         case MOD_REG_REXR:
           value = X86Field.extractRexValue(X86Field.REX_R_BIT_INDEX, rexByte);
           // fall through...
         case MOD_REG:
           value += X86Field.REG.extract(modRMByte);
           break;
         case MOD_RM_REXB:
           value = X86Field.extractRexValue(X86Field.REX_B_BIT_INDEX, rexByte);
           // fall through...
         case MOD_RM:
           value += X86Field.RM.extract(modRMByte);
           break;
         case SIB_BASE_REXB:
           value = X86Field.extractRexValue(X86Field.REX_B_BIT_INDEX, rexByte);
           // fall through...
         case SIB_BASE:
           value += X86Field.BASE.extract(sibByte);
           break;
         case SIB_INDEX_REXX:
           value = X86Field.extractRexValue(X86Field.REX_X_BIT_INDEX, rexByte);
           // fall through...
         case SIB_INDEX:
           value += X86Field.INDEX.extract(sibByte);
           break;
         case SIB_SCALE:
           value = X86Field.SCALE.extract(sibByte);
           break;
         case IMMEDIATE:
         case DISPLACEMENT:
           final X86NumericalParameter numericalParameter = (X86NumericalParameter) parameter;
           switch (numericalParameter.width()) {
             case BITS_8:
               arguments.add(new Immediate8Argument(EndianUtil.readByte(stream)));
               break;
             case BITS_16:
               arguments.add(new Immediate16Argument(EndianUtil.readLEShort(stream)));
               break;
             case BITS_32:
               arguments.add(new Immediate32Argument(EndianUtil.readLEInt(stream)));
               break;
             case BITS_64:
               arguments.add(new Immediate64Argument(EndianUtil.readLELong(stream)));
               break;
           }
           continue;
 
         case APPEND:
           final X86EnumerableParameter enumerableParameter = (X86EnumerableParameter) parameter;
           final SymbolSet symbolSet = enumerableParameter.getSymbolSet();
           arguments.add((Argument) symbolSet.fromValue(EndianUtil.readByte(stream)));
           continue;
 
         case OPCODE1_REXB:
           value = X86Field.extractRexValue(X86Field.REX_B_BIT_INDEX, rexByte);
           // fall through...
         case OPCODE1:
           value += header.opcode1().ordinal() & 7;
           break;
         case OPCODE2_REXB:
           value = X86Field.extractRexValue(X86Field.REX_B_BIT_INDEX, rexByte);
           // fall through...
         case OPCODE2:
           value += header.opcode2().ordinal() & 7;
           break;
       }
       final X86EnumerableParameter enumerableParameter = (X86EnumerableParameter) parameter;
       final SymbolSet symbolSet = enumerableParameter.getSymbolSet();
       if (symbolSet == AMD64GeneralRegister8.SYMBOLS) {
         arguments.add(AMD64GeneralRegister8.fromValue(value, header.rexPrefix() != null));
       } else {
         arguments.add((Argument) symbolSet.fromValue(value));
       }
     }
     return arguments;
   }
 
   private int getModVariantParameterIndex(Template_Type template, byte modRMByte, byte sibByte) {
     if (template.modCase() == ModCase.MOD_0 && X86Field.MOD.extract(modRMByte) != ModCase.MOD_0.value()) {
       switch (template.rmCase()) {
         case NORMAL:
           if (template.addressSizeAttribute() == WordWidth.BITS_16) {
             if (X86Field.RM.extract(modRMByte) != RMCase.SWORD.value()) {
               return -1;
             }
           } else if (X86Field.RM.extract(modRMByte) != RMCase.SDWORD.value()) {
             return -1;
           }
           for (int i = 0; i < template.parameters().size(); i++) {
             switch (template.parameters().get(i).place()) {
               case MOD_RM_REXB:
               case MOD_RM:
                 return i;
               default:
                 break;
             }
           }
           break;
         case SIB:
           if (template.sibBaseCase() == SibBaseCase.GENERAL_REGISTER && X86Field.BASE.extract(sibByte) == 5) {
             for (int i = 0; i < template.parameters().size(); i++) {
               switch (template.parameters().get(i).place()) {
                 case SIB_BASE_REXB:
                 case SIB_BASE:
                   return i;
                 default:
                   break;
               }
             }
           }
           break;
         default:
           break;
       }
     }
     return -1;
   }
 
   private byte getSibByte(BufferedInputStream stream, Template_Type template, byte modRMByte) throws IOException {
     if (template.addressSizeAttribute() == WordWidth.BITS_16) {
       return 0;
     }
     if (template.hasSibByte()) {
       return EndianUtil.readByte(stream);
     }
     if (template.hasModRMByte() && X86Field.RM.extract(modRMByte) == RMCase.SIB.value() &&
         X86Field.MOD.extract(modRMByte) != ModCase.MOD_3.value()) {
       return EndianUtil.readByte(stream);
     }
     return 0;
   }
 
   protected abstract Map<X86InstructionHeader, LinkedList<Template_Type>> headerToTemplates();
 
   private DisassembledInstruction_Type scanInstruction(BufferedInputStream stream, X86InstructionHeader header)
       throws IOException, DecoderException {
     boolean isFloatingPointEscape = false;
     if (X86Opcode.isFloatingPointEscape(header.opcode1())) {
       final int byte2 = stream.read();
       final HexByte hexByte = HexByte.values()[byte2];
       if (X86Opcode.isFloatingPointOpcode2(hexByte)) {
         isFloatingPointEscape = true;
         header.fixOpcode2(hexByte);
       }
     }
     if (header.opcode1() != null) {
       final LinkedList<Template_Type> templates = headerToTemplates().get(header);
       if (templates != null) {
         for (Template_Type template : templates) {
           stream.reset();
           decodeHeader(stream);
           if (isFloatingPointEscape) {
             stream.read();
           }
           try {
             byte modRMByte = 0;
             byte sibByte = 0;
             int modVariantParameterIndex = -1;
             List<Argument> arguments = null;
             if (template.hasModRMByte()) {
               modRMByte = EndianUtil.readByte(stream);
               sibByte = getSibByte(stream, template, modRMByte);
               modVariantParameterIndex = getModVariantParameterIndex(template, modRMByte, sibByte);
               if (modVariantParameterIndex >= 0) {
                 final Template_Type modVariantTemplate =
                     X86Assembly.getModVariantTemplate(templates,
                                                       template,
                                                       template.parameters().get(modVariantParameterIndex).type());
                 arguments = scanArguments(stream, modVariantTemplate, header, modRMByte, sibByte);
               }
             }
             if (arguments == null) {
               arguments = scanArguments(stream, template, header, modRMByte, sibByte);
             }
             if (modVariantParameterIndex >= 0) {
               final Immediate8Argument immediateArgument = (Immediate8Argument) arguments.get(modVariantParameterIndex);
               if (immediateArgument.value() != 0) {
                 continue;
               }
 
               // Remove the mod variant argument
               final Argument modVariantArgument = arguments.get(modVariantParameterIndex);
               final ArrayList<Argument> result = new ArrayList<Argument>();
               for (Argument element : arguments) {
                 if (modVariantArgument != element) {
                   result.add(element);
                 }
               }
               arguments = result;
             }
             if (-1 == indexOfNull(arguments)) {
               final Assembler assembler = createAssembler(_currentOffset);
               final byte[] bytes;
               try {
                 assembly().assemble(assembler, template, arguments);
                 bytes = assembler.toByteArray();
               } catch (AssemblyException e) {
                 throw new DecoderException("Error assembling decded instruction", e);
               }
               stream.reset();
               if (startsWith(stream, bytes)) {
                 final DisassembledInstruction_Type disassembledInstruction =
                     createDisassembledInstruction(_currentOffset, bytes, template, arguments);
                 _currentOffset += bytes.length;
                 return disassembledInstruction;
               }
             }
           } catch (IOException ioException) {
             // this one did not work, so loop back up and try another template
           }
         }
       }
     }
     throw new DecoderException("unknown instruction: " + header);
   }
 
   private static int indexOfNull(List<?> list) {
     int i = 0;
     for (Object element : list) {
       if (element == null) {
         return i;
       }
       ++i;
     }
     return -1;
   }
 
   @Override
   public final List<DisassembledInstruction_Type> scanOneInstruction(BufferedInputStream stream)
       throws IOException, DecoderException {
     stream.mark(MORE_THAN_ANY_INSTRUCTION_LENGTH);
     final X86InstructionHeader header = decodeHeader(stream);
     if (header == null) {
       throw new DecoderException("unknown instruction");
     }
     return StaticLoophole.asList(scanInstruction(stream, header));
   }
 
   @Override
   protected final List<DisassembledInstruction_Type> scan(BufferedInputStream stream)
       throws IOException, DecoderException {
     final ArrayList<DisassembledInstruction_Type> result = new ArrayList<DisassembledInstruction_Type>();
     while (true) {
       stream.mark(MORE_THAN_ANY_INSTRUCTION_LENGTH);
       final X86InstructionHeader header = decodeHeader(stream);
       if (header == null) {
         return result;
       }
       final DisassembledInstruction_Type disassembledInstruction = scanInstruction(stream, header);
       result.add(disassembledInstruction);
     }
   }
 
   private static boolean startsWith(BufferedInputStream bufferedInputStream, byte[] bytes) throws IOException {
     final byte[] data = new byte[bytes.length];
     bufferedInputStream.mark(bytes.length);
     try {
       int n = 0;
       while (n < data.length) {
         final int count = bufferedInputStream.read(data, n, data.length - n);
         if (count < 0) {
           throw new EOFException((data.length - n) + " of " + data.length + " bytes unread");
         }
         n += count;
       }
       if (java.util.Arrays.equals(data, bytes)) {
         return true;
       }
     } catch (IOException ioException) {
       // This is OK
     }
     bufferedInputStream.reset();
     return false;
   }
 
   protected static <Template_Type extends X86Template<Template_Type>> Map<X86InstructionHeader, LinkedList<Template_Type>> createMapping(
       Assembly<Template_Type> assembly, WordWidth addressWidth) {
     final Map<X86InstructionHeader, LinkedList<Template_Type>> result =
         new HashMap<X86InstructionHeader, LinkedList<Template_Type>>();
     for (Template_Type template : assembly.templates()) {
       X86InstructionHeader header = newInstructionHeader(template, addressWidth, 0, 0);
       LinkedList<Template_Type> matchingTemplates = result.get(header);
       if (matchingTemplates == null) {
         matchingTemplates = new LinkedList<Template_Type>();
         result.put(header, matchingTemplates);
       }
       matchingTemplates.addLast(template);
       for (X86Parameter parameter : template.parameters()) {
         switch (parameter.place()) {
           case OPCODE1_REXB:
           case OPCODE1:
             for (int i = 0; i < 8; i++) {
               header = newInstructionHeader(template, addressWidth, i, 0);
               result.put(header, matchingTemplates);
             }
             break;
           case OPCODE2_REXB:
           case OPCODE2:
             for (int i = 0; i < 8; i++) {
               header = newInstructionHeader(template, addressWidth, 0, i);
               result.put(header, matchingTemplates);
             }
             break;
           default:
             break;
         }
       }
     }
     return result;
   }
 
   private static <Template_Type extends X86Template<Template_Type>> X86InstructionHeader newInstructionHeader(final Template_Type template,
                                                                                                               final WordWidth addressWidth,
                                                                                                               final int opcode1Modifier,
                                                                                                               final int opcode2Modifier) {
     final HexByte opcode1 = HexByte.values()[template.description().opcode1().value() + opcode1Modifier];
     final HexByte opcode2 =
         (template.description().opcode2() == null) ?
         null :
         HexByte.values()[template.description().opcode2().value() + opcode2Modifier];
 
     final X86InstructionPrefix isp = template.description().operandPrefix();
     X86InstructionPrefix operandSizePrefix = (isp != null) ? isp : null;
     if (template.operandSizeAttribute() == WordWidth.BITS_16) {
       assert operandSizePrefix == null;
       operandSizePrefix = X86InstructionPrefix.OPERAND_SIZE;
     }
 
     return new X86InstructionHeader(opcode1,
                                     opcode2,
                                     template.addressSizeAttribute() != addressWidth,
                                     operandSizePrefix,
                                     null,
                                     template.description().mandatoryGroup1Prefix(),
                                     null);
   }
 }
