 package net.mirky.redis.analysers;
 
 import java.io.PrintStream;
 
 import net.mirky.redis.Disassembler;
 import net.mirky.redis.Format;
 import net.mirky.redis.Format.UnknownOption;
 import net.mirky.redis.Analyser;
 import net.mirky.redis.ReconstructionDataCollector;
 
 @Format.Optionses({
        @Format.Options("binary/decoding:decoding=ascii/origin:unsigned-hex=0/cpu:lang=none/api:api=none/entry:entry"),
 
         /*
          * Headerless ZXS file. We'll imply the CPU and decoding from context
          * but consider them explicit.
          */
         @Format.Options(".zx4/decoding!:decoding=zx-spectrum/origin:unsigned-hex=0/cpu!:lang=z80/api:api=none/entry:entry"),
 
         /* D64 header block. */
         @Format.Options("d64-header/decoding:decoding=petscii/api:api=none/cpu:lang=none/entry:entry"),
 
         /*
          * Headerless executable. Originally developed for CP/M; also used in
          * PC-DOS. Can hold binary code for i8080, i8085, z80, certain z80
          * successors, or i8086. We'll assume CP/M as API and z80 as processor
          * -- this covers most cases that are interesting in retrocomputing
          * context.
          */
         @Format.Options(".com/decoding:decoding=ascii/origin:unsigned-hex=0x0100/cpu:lang=z80/api:api=cpm/entry:entry=0x0100"),
         
         // ZX Spectrum BASIC number array file format.
         @Format.Options(value = "zxs-number-array/decoding:decoding=zx-spectrum", aliases = ".zx1"),
         
         // ZX Spectrum BASIC character array file format.
         @Format.Options(value = "zxs-char-array/decoding:decoding=zx-spectrum", aliases = ".zx2")
 })
 public class RawBinaryAnalyser extends Analyser.Leaf {
     @Override
     protected final ReconstructionDataCollector dis(Format format, byte[] data, PrintStream port) throws UnknownOption, RuntimeException {
         Disassembler disassembler = new Disassembler(data, format);
         Disassembler.Lang cpu = (Disassembler.Lang) ((Format.Option.SimpleOption) format.getOption("cpu")).value;
         Format.Option.EntryPoints entryPoints = (Format.Option.EntryPoints) format.getOption("entry");
         for (Format.EntryPoint entryPoint : entryPoints) {
             disassembler.noteAbsoluteEntryPoint(entryPoint.address, entryPoint.lang != null ? entryPoint.lang : cpu);
         }
         disassembler.run();
         disassembler.printResults(port);
         return null;
     }
 }
