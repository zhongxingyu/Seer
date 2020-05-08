 package net.mirky.redis;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import net.mirky.redis.Disassembler.Lang.Tabular.BytecodeCollector;
 
 final class LangParser {
     byte[] bytecode;
     private final BytecodeCollector coll;
     final int[] dispatchTable;
     final Disassembler.Lang.Tabular.Linkage linkage;
     final Map<String, Integer> minitablesByName;
     private int minitableCounter;
     final Map<String, Integer> referredLanguagesByName;
     int referredLanguageCounter;
     int dispatchSuboffset;
     int defaultCountdown;
     boolean trivial;
     private final List<MinitableReferencePatch> minitableReferencePatches;
 
     LangParser() {
         dispatchTable = new int[256];
         for (int i = 0; i < 256; i++) {
             dispatchTable[i] = -1;
         }
         coll = new BytecodeCollector();
         linkage = new Disassembler.Lang.Tabular.Linkage();
         minitablesByName = new HashMap<String, Integer>();
         minitableCounter = 0;
         referredLanguagesByName = new HashMap<String, Integer>();
         referredLanguageCounter = 0;
         dispatchSuboffset = 0;
         defaultCountdown = 0; // by default, no default countdown
         trivial = false; // by default, not trivial
         minitableReferencePatches = new ArrayList<MinitableReferencePatch>();
     }
 
     final void parse(String name, BufferedReader reader) throws IOException, DisassemblyTableParseError {
         Set<String> knownHeaderItems = new TreeSet<String>();
         // note that the membership is checked with a downcased specimen
         knownHeaderItems.add("dispatch-suboffset");
         knownHeaderItems.add("default-countdown");
         knownHeaderItems.add("trivial");
         Set<String> seenHeaderLines = new TreeSet<String>();
         
         ParseUtil.IndentableLexer lexer = new ParseUtil.IndentableLexer(new ParseUtil.LineSource.File(reader), new ParseUtil.ErrorLocator(name, 0), '#');
         try {
             while (true) {
                 if (!lexer.atAlphanumeric()) {
                     break;
                 }
                 String itemName = lexer.peekDashedWord(null);
                 if (!knownHeaderItems.contains(itemName.toLowerCase())) {
                     break;
                 }
                 if (seenHeaderLines.contains(itemName.toLowerCase())) {
                     throw new DisassemblyTableParseError("duplicate lang header item " + itemName);
                 }
                 seenHeaderLines.add(itemName.toLowerCase());
                 lexer.readDashedWord(null);
                 lexer.skipSpaces();
                 lexer.pass(':');
                 // Note that comments are not ignored after header items.
                 lexer.skipSpaces();
                 int posBeforeContent = lexer.getPos();
                 try {
                     processHeader(itemName, lexer.readRestOfLine());
                 } catch (NumberFormatException e) {
                     lexer.errorAtPos(posBeforeContent, "not a proper numeric value");
                 }
                 lexer.passLogicalNewline();
             }
             lexer.passDashedWord("dispatch");
             parseDispatchTable(lexer);
             while (!lexer.atEndOfFile()) {
                 lexer.noIndent();
                 if (lexer.passOptDashedWord("minitable")) {
                     parseMinitableDeclaration(lexer);
                 } else {
                     break;
                 }
             }
             lexer.requireEndOfFile();
         } finally {
             reader.close();
         }
         
         bytecode = coll.finish();
         for (MinitableReferencePatch patch : minitableReferencePatches) {
             patch.apply();
         }
     }
 
     // Called by {@code parse(...)} for each header name-value pair. Guaranteed
     // to be called at most once per name.
     private final void processHeader(String name, String value) throws NumberFormatException, DisassemblyTableParseError {
         if (name.equalsIgnoreCase("Dispatch-suboffset")) {
             dispatchSuboffset = Integer.parseInt(value);
         } else if (name.equalsIgnoreCase("Default-countdown")) {
             defaultCountdown = Integer.parseInt(value);
         } else if (name.equalsIgnoreCase("Trivial")) {
             trivial = parseBoolean(value);
         } else {
             throw new DisassemblyTableParseError("unknown lang file header item " + name);
         }
     }
 
     private static final boolean parseBoolean(String value) throws DisassemblyTableParseError {
         if (value.equalsIgnoreCase("true")) {
             return true;
         }
         if (value.equalsIgnoreCase("false")) {
             return false;
         }
         throw new DisassemblyTableParseError("not a Boolean value: " + value);
     }
 
     private final void parseMinitableDeclaration(ParseUtil.IndentableLexer lexer) throws IOException, DisassemblyTableParseError {
         lexer.skipSpaces();
         int before = lexer.getPos();
         String tableName = lexer.readDashedWord("minitable name");
         if (minitablesByName.containsKey(tableName)) {
             lexer.errorAtPos(before, "duplicate minitable name");
         }
         lexer.skipSpaces();
         lexer.pass(':');
         lexer.skipSpaces();
         List<String> minitable = new ArrayList<String>();
         while (lexer.atAlphanumeric()) {
             minitable.add(lexer.readDashedWord("item"));
             lexer.skipSpaces();
         }
         // minitable size must be a power of two
         // (so that we can mask off excess high bits meaningfully)
         if (minitable.size() == 0 || (minitable.size() & (minitable.size() - 1)) != 0) {
             throw new DisassemblyTableParseError("invalid minitable size for " + tableName);
         }
         if (minitableCounter >= Disassembler.Bytecode.MAX_MINITABLE_COUNT) {
             throw new RuntimeException("too many minitables");
         }
         minitablesByName.put(tableName, new Integer(minitableCounter));
         linkage.minitables[minitableCounter++] = minitable.toArray(new String[0]);
         lexer.passLogicalNewline();
     }
 
     private final void parseDispatchTable(ParseUtil.IndentableLexer lexer) throws IOException,
             RuntimeException, DisassemblyTableParseError {
         lexer.passLogicalNewline();
         lexer.passIndent();
         while (!lexer.atDedent()) {
             lexer.noIndent();
             lexer.pass('[');
             lexer.skipSpaces();
             CodeSet set = CodeSet.parse(lexer);
             lexer.skipSpaces();
             lexer.pass(']');
             lexer.skipSpaces();
             for (int i = 0; i < 256; i++) {
                 if (set.matches(i)) {
                     if (dispatchTable[i] != -1) {
                         throw new DisassemblyTableParseError("duplicate decipherer for 0x" + Hex.b(i));
                     }
                     dispatchTable[i] = coll.currentPosition();
                 }
             }
             while (!lexer.atEndOfLine()) {
                 char c = lexer.readChar();
                 if (c == '<') {
                     parseBroketed(lexer);
                 } else {
                     if (c < 0x20 || c > 0x7E) {
                         throw new RuntimeException("invalid literal character code 0x" + Hex.w(c));
                     }
                     coll.add((byte) c);
                 }
             }
             coll.add(Disassembler.Bytecode.COMPLETE);
             lexer.passLogicalNewline();
         }
         lexer.discardDedent();
     }
 
     // called with lexer's cursor immediately after the opening broket; returns
     // with the cursor immediately after the closing broket
     private final void parseBroketed(ParseUtil.IndentableLexer lexer) {
         int size = 0;
         do {
             lexer.skipSpaces();
             size = parseBroketedStep(lexer, size);
         } while (lexer.passOpt(','));
         if (size != 0) {
             lexer.error("final step missing");
         }
         lexer.pass('>');
     }
 
     // also eats up the whitespace following the step
     private final int parseBroketedStep(ParseUtil.IndentableLexer lexer, int size) {
         int posBeforeStep = lexer.getPos();
         String verb = lexer.readDashedWord("processing step");
         lexer.skipSpaces();
         int posBeforeArg = lexer.getPos();
         String arg;
         if (lexer.atAlphanumeric()) {
             arg = lexer.readDashedWord("processing step argument");
             lexer.skipSpaces();
         } else {
             arg = null;
         }
         if (verb.equals("tempswitch")) {
             if (size != 0) {
                 lexer.errorAtPos(posBeforeStep, "misplaced tempswitch");
             }
             if (arg == null) {
                 lexer.errorAtPos(posBeforeArg, "expected lang reference");
             }
             coll.add((byte) (Disassembler.Bytecode.TEMPSWITCH_0 + resolveReferredLanguage(arg)));
             return 0;
         } else if (verb.equals("dispatch")) {
             if (size != 1) {
                 lexer.errorAtPos(posBeforeStep, "misplaced dispatch");
             }
             if (arg == null) {
                 lexer.errorAtPos(posBeforeArg, "expected lang reference");
             }
             coll.add((byte) (Disassembler.Bytecode.DISPATCH_0 + resolveReferredLanguage(arg)));
             return 0;
         } else {
             String step = arg == null ? verb : verb + ' ' + arg;
             Disassembler.Bytecode.StepDeclaration resolvedStep = Disassembler.Bytecode.resolveSimpleStep(step);
             if (resolvedStep == null) {
                 switch (size) {
                     case 1:
                         resolvedStep = Disassembler.Bytecode.resolveSimpleStep("<byte> " + step);
                         break;
                     case 2:
                         resolvedStep = Disassembler.Bytecode.resolveSimpleStep("<wyde> " + step);
                         break;
                 }
             }
             if (resolvedStep != null) {
                 if (!resolvedStep.typeMatches(size)) {
                     lexer.errorAtPos(posBeforeStep, "type mismatch");
                 }
                 coll.add(resolvedStep.code);
                 return resolvedStep.sizeAfter != -1 ? resolvedStep.sizeAfter : size;
             } else {
                 // unknown -- assume it's a minitable reference
                 if (size == 0) {
                     lexer.errorAtPos(posBeforeStep, "attempt to look up void value");
                 }
                 int position = coll.currentPosition();
                 coll.add((Disassembler.Bytecode.INVALID));
                 minitableReferencePatches.add(new MinitableReferencePatch(position, step));
                 return 0;
             }
         }
     }
 
     /**
      * Determines the index of the given referred language. Adds
      * this language to the referred language list if it is not
      * already found. Throws an exception if the referred
      * language list is full.
      */
     private final int resolveReferredLanguage(String newLangName) {
         Integer index = referredLanguagesByName.get(newLangName);
         if (index == null) {
             if (referredLanguageCounter >= Disassembler.Bytecode.MAX_REFERRED_LANGUAGE_COUNT) {
                 // FIXME: this ought to be properly pinpointed
                 throw new RuntimeException("too many referred languages");
             }
             linkage.referredLanguages[referredLanguageCounter] = newLangName;
            referredLanguagesByName.put(newLangName, index);
             return referredLanguageCounter++;
         } else {
             return index.intValue();
         }
     }
 
     class MinitableReferencePatch {
         public final int position;
         public final String minitableName;
         
         public MinitableReferencePatch(int position, String minitableName) {
             this.position = position;
             this.minitableName = minitableName;
         }
     
         public final void apply() throws DisassemblyTableParseError {
             if (bytecode[position] != Disassembler.Bytecode.INVALID) {
                 throw new RuntimeException("bug detected");
             }
             if (!minitablesByName.containsKey(minitableName)) {
                 throw new DisassemblyTableParseError("unknown minitable: " + minitableName);
             }
             int minitableNumber = minitablesByName.get(minitableName).intValue();
             assert minitableNumber < Disassembler.Bytecode.MAX_MINITABLE_COUNT;
             bytecode[position] = (byte) (Disassembler.Bytecode.MINITABLE_LOOKUP_0 + minitableNumber);
         }
     }
 
     /*
      * An instance of this with a brief message is thrown internally
      * when lang file parsing fails. Considering that all our lang files
      * are considered internal to the project, this is not supposed to
      * happen, so we catch all our {@link DisassemblyTableParseError}:s
      * and throw a RuntimeException to the caller instead (but we'll
      * retain the {@link DisassemblyTableParseError} as a cause).
      */
     static final class DisassemblyTableParseError extends Exception {
         DisassemblyTableParseError(String msg) {
             super(msg);
         }
 
         public DisassemblyTableParseError(String msg, Exception cause) {
             super(msg, cause);
         }
     }
 }
