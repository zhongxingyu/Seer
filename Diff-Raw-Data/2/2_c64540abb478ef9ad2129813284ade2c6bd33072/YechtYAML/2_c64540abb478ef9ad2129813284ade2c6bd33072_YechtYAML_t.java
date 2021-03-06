 package org.yecht.ruby;
 
 import java.util.List;
 import java.util.LinkedList;
 
 import org.yecht.BadAnchorHandler;
 import org.yecht.BytecodeNodeHandler;
 import org.yecht.Bytestring;
 import org.yecht.Data;
 import org.yecht.Emitter;
 import org.yecht.EmitterHandler;
 import org.yecht.ErrorHandler;
 import org.yecht.IoStrRead;
 import org.yecht.JechtIO;
 import org.yecht.MapPart;
 import org.yecht.Node;
 import org.yecht.NodeHandler;
 import org.yecht.Parser;
 import org.yecht.ParserInput;
 import org.yecht.OutputHandler;
 import org.yecht.Pointer;
 import org.yecht.ImplicitScanner;
 import org.yecht.MapStyle;
 import org.yecht.SeqStyle;
 import org.yecht.ScalarStyle;
 
 import org.jruby.Ruby;
 import org.jruby.RubyArray;
 import org.jruby.RubyClass;
 import org.jruby.RubyEnumerable;
 import org.jruby.RubyHash;
 import org.jruby.RubyKernel;
 import org.jruby.RubyModule;
 import org.jruby.RubyNumeric;
 import org.jruby.RubyObject;
 import org.jruby.RubyString;
 import org.jruby.anno.JRubyMethod;
 import org.jruby.runtime.Block;
 import org.jruby.runtime.BlockCallback;
 import org.jruby.runtime.ThreadContext;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.jruby.runtime.ObjectAllocator;
 import org.jruby.util.ByteList;
 import org.jruby.util.TypeConverter;
 
 public class YechtYAML {
     private static interface PossibleLinkNode {
         void addLink(StorageLink link);
         void replaceLinks(IRubyObject newObject);
     }
 
     private static abstract class StorageLink {
         public abstract void replaceLinkWith(IRubyObject object);
     }
 
     private static class ArrayStorageLink extends StorageLink {
         private final RubyArray array;
         private final int index;
         private final IRubyObject originalObject;
 
         public ArrayStorageLink(IRubyObject arr, int index, IRubyObject originalObject) {
             this.array = (RubyArray)arr;
             this.index = index;
             this.originalObject = originalObject;
         }
 
         public void replaceLinkWith(IRubyObject newObject) {
             array.store(index, newObject);
         }
     }
 
     private static class HashStorageLink extends StorageLink {
         private final RubyHash hash;
         private final IRubyObject key;
         private final IRubyObject originalObject;
 
         public HashStorageLink(IRubyObject h, IRubyObject key, IRubyObject originalObject) {
             this.hash = (RubyHash)h;
             this.key = key;
             this.originalObject = originalObject;
         }
 
         public void replaceLinkWith(IRubyObject newObject) {
             hash.fastASet(key, newObject);
         }
     }
 
     public static class BadAlias extends RubyObject implements PossibleLinkNode {
         public static final ObjectAllocator Allocator = new ObjectAllocator() {
                 public IRubyObject allocate(Ruby runtime, RubyClass klass) {
                     return new BadAlias(runtime, klass);
                 }
             };
         
         public BadAlias(Ruby runtime, RubyClass metaClass) {
             super(runtime, metaClass);
         }
 
         private List<StorageLink> links = new LinkedList<StorageLink>();
         public void addLink(StorageLink link) {
             links.add(link);
         }
 
         public void replaceLinks(IRubyObject newObject) {
             for(StorageLink sl : links) {
                 sl.replaceLinkWith(newObject);
             }
         }
 
         // syck_badalias_initialize
         @JRubyMethod
         public static IRubyObject initialize(IRubyObject self, IRubyObject val) {
             self.getInstanceVariables().setInstanceVariable("@name", val);
             return self;
         }
 
         // syck_badalias_cmp
         @JRubyMethod(name = "<=>")
         public static IRubyObject cmp(IRubyObject alias1, IRubyObject alias2) {
             IRubyObject str1 = alias1.getInstanceVariables().getInstanceVariable("@name");
             IRubyObject str2 = alias2.getInstanceVariables().getInstanceVariable("@name");
             return str1.callMethod(alias1.getRuntime().getCurrentContext(), "<=>", str2);
         }
     }
 
     public static class RubyIoStrRead implements IoStrRead {
         private IRubyObject port;
         public RubyIoStrRead(IRubyObject port) {
             this.port = port;
         }
 
         // rb_syck_io_str_read
         public int read(Pointer buf, JechtIO.Str str, int max_size, int skip) {
             int len = 0;
             max_size -= skip;
             if(max_size <= 0) {
                 max_size = 0;
             } else {
                 IRubyObject src = port;
                 IRubyObject n = RubyNumeric.int2fix(port.getRuntime(), max_size);
                 IRubyObject str2 = src.callMethod(port.getRuntime().getCurrentContext(), "read", n);
                 if(!str2.isNil()) {
                     ByteList res = str2.convertToString().getByteList();
                     len = res.realSize;
                     System.arraycopy(res.bytes, res.begin, buf.buffer, buf.start+skip, len);
                 }
             }
             len += skip;
             buf.buffer[buf.start+len] = 0;
             return len;
         }
     }
 
     private static int extractInt(byte[] buff, int p, int pend) {
         int len = 0;
         while((p+len) < pend && Character.isDigit((char)buff[p+len])) {
             len++;
         }
         try {
             return Integer.parseInt(new String(buff, p, len, "ISO-8859-1"));
         } catch(java.io.UnsupportedEncodingException e) {return -1;}
     }
     
     // rb_syck_mktime
     public static IRubyObject makeTime(Ruby runtime, Pointer str, int len) {
 //         System.err.println("makeTime(" + new String(str.buffer, str.start, len) + ")");
         int ptr = str.start;
         int pend = ptr + len;
         IRubyObject year = runtime.newFixnum(0);
         IRubyObject mon = runtime.newFixnum(0);
         IRubyObject day = runtime.newFixnum(0);
         IRubyObject hour = runtime.newFixnum(0);
         IRubyObject min = runtime.newFixnum(0);
         IRubyObject sec = runtime.newFixnum(0);
         long usec = 0;
 
         if(str.buffer[ptr] != 0 && ptr < pend) {
             year = runtime.newFixnum(extractInt(str.buffer, ptr, pend));
         }
 
         ptr += 4;
         if(str.buffer[ptr] != 0 && ptr < pend) {
             while(!Character.isDigit((char)str.buffer[ptr]) && ptr < pend) ptr++;
             mon = runtime.newFixnum(extractInt(str.buffer, ptr, pend));
         }
 
         ptr += 2;
         if(str.buffer[ptr] != 0 && ptr < pend) {
             while(!Character.isDigit((char)str.buffer[ptr]) && ptr < pend) ptr++;
             day = runtime.newFixnum(extractInt(str.buffer, ptr, pend));
         }
 
         ptr += 2;
         if(str.buffer[ptr] != 0 && ptr < pend) {
             while(!Character.isDigit((char)str.buffer[ptr]) && ptr < pend) ptr++;
             hour = runtime.newFixnum(extractInt(str.buffer, ptr, pend));
         }
 
         ptr += 2;
         if(str.buffer[ptr] != 0 && ptr < pend) {
             while(!Character.isDigit((char)str.buffer[ptr]) && ptr < pend) ptr++;
             min = runtime.newFixnum(extractInt(str.buffer, ptr, pend));
         }
 
         ptr += 2;
         if(str.buffer[ptr] != 0 && ptr < pend) {
             while(!Character.isDigit((char)str.buffer[ptr]) && ptr < pend) ptr++;
             sec = runtime.newFixnum(extractInt(str.buffer, ptr, pend));
         }
 
         ptr += 2;
         if(ptr < pend && str.buffer[ptr] == '.') {
             int end = ptr + 1;
             while(Character.isDigit((char)str.buffer[end]) && end < pend) end++;
             byte[] padded = new byte[]{'0', '0', '0', '0', '0', '0'};
             System.arraycopy(str.buffer, ptr+1, padded, 0, end - (ptr+1));
             try {
                 usec = Long.parseLong(new String(padded, 0, 6, "ISO-8859-1"));
             } catch(java.io.UnsupportedEncodingException e) {}
         } else {
             usec = 0;
         }
 
         while(ptr < pend && str.buffer[ptr] != 'Z' && str.buffer[ptr] != '+' && str.buffer[ptr] != '-' && str.buffer[ptr] != 0) {
             ptr++;
         }
 
         if(ptr < pend && (str.buffer[ptr] == '-' || str.buffer[ptr] == '+')) {
             int lenx = 1;
             while(ptr+lenx < pend && Character.isDigit((char)str.buffer[ptr+lenx])) {
                 lenx++;
             }
             if(str.buffer[ptr] == '+') {
                 ptr++;
                 lenx--;
             }
             try {
                 long tz_offset = Long.parseLong(new String(str.buffer, ptr, lenx, "ISO-8859-1")) * 3600;
                 ptr+=lenx;
                 while(ptr < pend && str.buffer[ptr] != ':' && str.buffer[ptr] != 0 ) {
                     ptr++;
                 }
                 if(ptr < pend && str.buffer[ptr] == ':') {
                     ptr++;
                     if(tz_offset < 0) {
                         tz_offset -= extractInt(str.buffer, ptr, pend) * 60;
                     } else {
                         tz_offset += extractInt(str.buffer, ptr, pend) * 60;
                     }
                 }
                 
                 IRubyObject time = runtime.getClass("Time").callMethod(runtime.getCurrentContext(), "utc", new IRubyObject[]{year,mon,day,hour,min,sec});
                 long tmp = RubyNumeric.num2long(time.callMethod(runtime.getCurrentContext(), "to_i")) - tz_offset;
                 return runtime.getClass("Time").callMethod(runtime.getCurrentContext(), "at", new IRubyObject[]{runtime.newFixnum(tmp), runtime.newFixnum(usec)});
             } catch(java.io.UnsupportedEncodingException e) {}
         } else {
             // Make UTC time
             return runtime.getClass("Time").callMethod(runtime.getCurrentContext(), "utc", new IRubyObject[]{year,mon,day,hour,min,sec,runtime.newFixnum(usec)});
         }
         System.err.println("oopsie, returning null");
         return null;
     }
 
     // yaml_org_handler
     public static boolean orgHandler(IRubyObject self, org.yecht.Node n, IRubyObject[] ref) {
 //         System.err.println("orgHandler(" + self + ", " + n + ")");
         final Ruby runtime = self.getRuntime();
         ThreadContext ctx = runtime.getCurrentContext();
         String type_id = n.type_id;
         boolean transferred = false;
         IRubyObject obj = runtime.getNil();
 
         if(type_id != null && type_id.startsWith("tag:yaml.org,2002:")) {
             type_id = type_id.substring(18);
         }
 
         try {
             switch(n.kind) {
             case Str:
                 transferred = true;
                 Data.Str ds = (Data.Str)n.data;
 //                 System.err.println(" we have type id: " + type_id + " for: " + n);
                 if(type_id == null) {
                     obj = RubyString.newString(runtime, ds.ptr.buffer, ds.ptr.start, ds.len);
                 } else if(type_id.equals("null")) {
                     obj = runtime.getNil();
                 } else if(type_id.equals("binary")) {
                     obj = RubyString.newString(runtime, ds.ptr.buffer, ds.ptr.start, ds.len);
                     obj.callMethod(ctx, "tr!", new IRubyObject[]{runtime.newString("\n\t "), runtime.newString("")});
                     IRubyObject arr = obj.callMethod(ctx, "unpack", runtime.newString("m"));
                    obj = ((RubyArray)arr).shift(ctx);
                 } else if(type_id.equals("bool#yes")) {
                     obj = runtime.getTrue();
                 } else if(type_id.equals("bool#no")) {
                     obj = runtime.getFalse();
                 } else if(type_id.equals("int#hex")) {
                     n.strBlowAwayCommas();
                     obj = RubyNumeric.str2inum(runtime, RubyString.newString(runtime, ds.ptr.buffer, ds.ptr.start, ds.len), 16, true);
                 } else if(type_id.equals("int#oct")) {
                     n.strBlowAwayCommas();
                     obj = RubyNumeric.str2inum(runtime, RubyString.newString(runtime, ds.ptr.buffer, ds.ptr.start, ds.len),  8, true);
                 } else if(type_id.equals("int#base60")) {
                     long sixty = 1;
                     long total = 0;
                     n.strBlowAwayCommas();
                     int ptr = ds.ptr.start;
                     int end = ptr + ds.len;
                     while(end > ptr) {
                         long bnum = 0;
                         int colon = end - 1;
                         while(colon >= ptr && ds.ptr.buffer[colon] != ':' ) {
                             colon--;
                         }
                         bnum = Integer.parseInt(new String(ds.ptr.buffer, colon+1, end-(colon+1), "ISO-8859-1"));
                         total += bnum * sixty;
                         sixty *= 60;
                         end = colon;
                     }
                     obj = runtime.newFixnum(total);
                 } else if(type_id.startsWith("int")) {
                     n.strBlowAwayCommas();
                     obj = RubyNumeric.str2inum(runtime, RubyString.newString(runtime, ds.ptr.buffer, ds.ptr.start, ds.len),  10, true);
                 } else if(type_id.equals("float#base60")) {
                     long sixty = 1;
                     double total = 0.0;
                     n.strBlowAwayCommas();
                     int ptr = ds.ptr.start;
                     int end = ptr + ds.len;
                     while(end > ptr) {
                         double bnum = 0;
                         int colon = end - 1;
                         while(colon >= ptr && ds.ptr.buffer[colon] != ':' ) {
                             colon--;
                         }
                         bnum = Double.parseDouble(new String(ds.ptr.buffer, colon+1, end-(colon+1), "ISO-8859-1"));
                         total += bnum * sixty;
                         sixty *= 60;
                         end = colon;
                     }
                     obj = runtime.newFloat(total);
                 } else if(type_id.equals("float#nan")) {
                     obj = runtime.newFloat(Double.NaN);
                 } else if(type_id.equals("float#inf")) {
                     obj = runtime.newFloat(Double.POSITIVE_INFINITY);
                 } else if(type_id.equals("float#neginf")) {
                     obj = runtime.newFloat(Double.NEGATIVE_INFINITY);
                 } else if(type_id.startsWith("float")) {
                     n.strBlowAwayCommas();
                     obj = RubyString.newString(runtime, ds.ptr.buffer, ds.ptr.start, ds.len);
                     obj = obj.callMethod(ctx, "to_f");
                 } else if(type_id.equals("timestamp#iso8601")) {
                     obj = makeTime(runtime, ds.ptr, ds.len);
                 } else if(type_id.equals("timestamp#spaced")) {
                     obj = makeTime(runtime, ds.ptr, ds.len);
                 } else if(type_id.equals("timestamp#ymd")) {
                     IRubyObject year = runtime.newFixnum(Integer.parseInt(new String(ds.ptr.buffer, 0, 4, "ISO-8859-1")));
                     IRubyObject mon = runtime.newFixnum(Integer.parseInt(new String(ds.ptr.buffer, 5, 2, "ISO-8859-1")));
                     IRubyObject day = runtime.newFixnum(Integer.parseInt(new String(ds.ptr.buffer, 8, 2, "ISO-8859-1")));
                 
                     RubyKernel.require(runtime.getTopSelf(), runtime.newString("date"), Block.NULL_BLOCK);
 
                     obj = runtime.getClass("Date").callMethod(ctx, "new", new IRubyObject[] {year, mon, day});
                 } else if(type_id.startsWith("timestamp")) {
                     obj = makeTime(runtime, ds.ptr, ds.len);
                 } else if(type_id.startsWith("merge")) {
                     obj = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("MergeKey").callMethod(ctx, "new");
                 } else if(type_id.startsWith("default")) {
                     obj = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("DefaultKey").callMethod(ctx, "new");
                 } else if(ds.style == ScalarStyle.Plain && ds.len > 1 && ds.ptr.buffer[ds.ptr.start] == ':') {
 //                     System.err.println("houston, we have a symbol: " + n);
                     obj = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("DefaultResolver").callMethod(ctx, "transfer", 
                                                                                                                                                new IRubyObject[]{runtime.newString("tag:ruby.yaml.org,2002:sym"),
                                                                                                                                                                  RubyString.newString(runtime, ds.ptr.buffer, ds.ptr.start+1, ds.len-1)
                                                                                                                                                });
 //                     System.err.println(" resulting in: " + obj);
                 } else if(type_id.equals("str")) {
                     obj = RubyString.newString(runtime, ds.ptr.buffer, ds.ptr.start, ds.len);
                 } else {
                     transferred = false;
                     obj = RubyString.newString(runtime, ds.ptr.buffer, ds.ptr.start, ds.len);
                 }
                 break;
             case Seq:
                 if(type_id == null || "seq".equals(type_id)) {
                     transferred = true;
                 }
                 Data.Seq dl = (Data.Seq)n.data;
                 obj = RubyArray.newArray(runtime, dl.idx);
                 for(int i = 0; i < dl.idx; i++) {
                     IRubyObject _obj = (IRubyObject)n.seqRead(i);
                     if(_obj instanceof PossibleLinkNode) {
                         ((PossibleLinkNode)_obj).addLink(new ArrayStorageLink(obj, i, _obj));
                     }
                     ((RubyArray)obj).store(i, _obj);
                 }
                 break;
             case Map:
                 if(type_id == null || "map".equals(type_id)) {
                     transferred = true;
                 }
                 Data.Map dm = (Data.Map)n.data;
                 obj = RubyHash.newHash(runtime);
                 RubyClass cMergeKey = (RubyClass)(((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("MergeKey"));
                 RubyClass cDefaultKey = (RubyClass)(((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("DefaultKey"));
                 for(int i = 0; i < dm.idx; i++) {
                     IRubyObject k = (IRubyObject)n.mapRead(MapPart.Key, i);
                     IRubyObject v = (IRubyObject)n.mapRead(MapPart.Value, i);
                     if(null == k) {
                         System.err.println("working on: " + n);
                         System.err.println("k is nil!");
                     }
                     if(null == v) {
                         v = runtime.getNil();
                     }
                     boolean skip_aset = false;
 
                     if(cMergeKey.isInstance(k)) {
                         IRubyObject tmp = null;
                         if(!(tmp = TypeConverter.convertToTypeWithCheck(v, runtime.getHash(), "to_hash")).isNil()) {
                             IRubyObject dup = v.callMethod(ctx, "dup");
                             dup.callMethod(ctx, "update", obj);
                             obj = dup;
                             skip_aset = true;
                         } else if(!(tmp = v.checkArrayType()).isNil()) {
                             IRubyObject end = ((RubyArray)tmp).pop(ctx);
                             IRubyObject tmph = TypeConverter.convertToTypeWithCheck(end, runtime.getHash(), "to_hash");
                             if(!tmph.isNil()) {
                                 final IRubyObject dup = tmph.callMethod(ctx, "dup");
                                 tmp = ((RubyArray)tmp).reverse();
                                 ((RubyArray)tmp).append(obj);
                                 RubyEnumerable.callEach(runtime, ctx, tmp, new BlockCallback() {
                                         // syck_merge_i
                                         public IRubyObject call(ThreadContext _ctx, IRubyObject[] largs, Block blk) {
                                             IRubyObject entry = largs[0];
                                             IRubyObject tmp = null;
                                             if(!(tmp = TypeConverter.convertToTypeWithCheck(entry, runtime.getHash(), "to_hash")).isNil()) {
                                                 dup.callMethod(_ctx, "update", tmp);
                                             }
                                             return runtime.getNil();
                                         }
                                     });
                                 obj = dup;
                                 skip_aset = true;
                             }
                         }
                     } else if(cDefaultKey.isInstance(k)) {
                         obj.callMethod(ctx, "default=", v);
                         skip_aset = true;
                     }
                 
                     if(!skip_aset) {
                         if(v instanceof PossibleLinkNode) {
                             ((PossibleLinkNode)v).addLink(new HashStorageLink(obj, k, v));
                         }
                         ((RubyHash)obj).fastASet(k, v);
                     }
                 }
 
                 break;
             }
         } catch(java.io.UnsupportedEncodingException e) {}
         ref[0] = obj;
 //         System.err.println(" - transferred: " + transferred);
         return transferred;
     }
 
     public static class RubyLoadHandler implements NodeHandler {
         private Ruby runtime;
 
         public RubyLoadHandler(Ruby runtime) {
             this.runtime = runtime;
         }
 
         // rb_syck_load_handler
         public Object handle(Parser p, org.yecht.Node n) {
 //             System.err.println("load_handler for node: " + n.type_id + " with anchor: " + n.anchor);
 //             System.err.println(" id: " + n.id);
 //             if(n.id != null) {
 //                 System.err.println(" val: " + ((IRubyObject)n.id).inspect().toString());
 //             }
 
 //             System.err.println("rb_syck_load_handler(" + n + ")");
             YParser.Extra bonus = (YParser.Extra)p.bonus;
             IRubyObject resolver = bonus.resolver;
             if(resolver.isNil()) {
                 resolver = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("DefaultResolver");
             }
             
             IRubyObject _n = runtime.newData((RubyClass)((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("Node"), n);
             
             IRubyObject obj = resolver.callMethod(runtime.getCurrentContext(), "node_import", _n);
 //             System.err.println(" node_import -> " + obj);
             if(n.id != null && !obj.isNil()) {
                 if(n.id instanceof PossibleLinkNode) {
                     ((PossibleLinkNode)n.id).replaceLinks(obj);
                 }
                 n.id = obj;
 //                 System.err.println(" -- LoadHandler, setting id, yay!");
             }
 
             if(bonus.taint) {
                 obj.setTaint(true);
             }
 
             if(bonus.proc != null) {
                 bonus.proc.callMethod(runtime.getCurrentContext(), "call", obj);
             }
             
             ((RubyHash)bonus.data).fastASet(((RubyHash)bonus.data).rb_size(), obj);
 
 //             System.err.println(" -> rb_syck_load_handler=" + n.id);
             return obj;
         }
     }
 
     public static class RubyErrHandler implements ErrorHandler {
         private Ruby runtime;
 
         public RubyErrHandler(Ruby runtime) {
             this.runtime = runtime;
         }
 
         // rb_syck_err_handler
         public void handle(Parser p, String msg) {
             int endl = p.cursor;
             while(p.buffer.buffer[endl] != 0 && p.buffer.buffer[endl] != '\n') {
                 endl++;
             }
             try {
                 int lp = p.lineptr;
                 if(lp < 0) {
                     lp = 0;
                 }
                 String line = new String(p.buffer.buffer, lp, endl-lp, "ISO-8859-1");
                 String m1 = msg + " on line " + p.linect + ", col " + (p.cursor-lp) + ": `" + line + "'";
                 throw runtime.newArgumentError(m1);
             } catch(java.io.UnsupportedEncodingException e) {
             }
             
         }
     }
 
     public static class RubyBadAnchorHandler implements BadAnchorHandler {
         private Ruby runtime;
 
         public RubyBadAnchorHandler(Ruby runtime) {
             this.runtime = runtime;
         }
 
         // rb_syck_bad_anchor_handler
         public org.yecht.Node handle(Parser p, String a) {
             IRubyObject anchor_name = runtime.newString(a);
             IRubyObject nm = runtime.newString("name");
             org.yecht.Node badanc = org.yecht.Node.newMap(nm, anchor_name);
             badanc.type_id = "tag:ruby.yaml.org,2002:object:YAML::Yecht::BadAlias";
             return badanc;
         }
     }
 
     // syck_set_model
     public static void setModel(IRubyObject p, IRubyObject input, IRubyObject model) {
         Ruby runtime = p.getRuntime();
         Parser parser = (Parser)p.dataGetStructChecked();
         parser.handler(new RubyLoadHandler(runtime));
         if(model == runtime.newSymbol("Generic")) {
             p.callMethod(runtime.getCurrentContext(), "set_resolver", ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("GenericResolver"));
         }
         parser.implicitTyping(true);
         parser.taguriExpansion(true);
 
         if(input.isNil()) {
             input = p.getInstanceVariables().getInstanceVariable("@input");
         }
 
         if(input == runtime.newSymbol("bytecode")) {
             parser.setInputType(ParserInput.Bytecode_UTF8);
         } else {
             parser.setInputType(ParserInput.YAML_UTF8);
         }
 
         parser.errorHandler(new RubyErrHandler(runtime));
         parser.badAnchorHandler(new RubyBadAnchorHandler(runtime));
     }
 
     // syck_parser_assign_io
     public static boolean assignIO(Ruby runtime, Parser parser, IRubyObject[] pport) {
         boolean taint = true;
         IRubyObject tmp, port = pport[0];
         if(!(tmp = port.checkStringType()).isNil()) {
             taint = port.isTaint();
             port = tmp;
             ByteList bl = ((RubyString)port).getByteList();
             parser.str(Pointer.create(bl.bytes, bl.begin), bl.realSize, null);
         } else if(port.respondsTo("read")) {
             if(port.respondsTo("binmode")) {
                 port.callMethod(runtime.getCurrentContext(), "binmode");
             }
             parser.str(Pointer.empty(), 0, new RubyIoStrRead(port));
         } else {
             throw runtime.newTypeError("instance of IO needed");
         }
         pport[0] = port;
         return taint;
     }
 
     public static class Module {
         // rb_syck_compile
         @JRubyMethod(name = "compile", required = 1, module = true)
         public static IRubyObject compile(IRubyObject self, IRubyObject port) {
             Parser parser = Parser.newParser();
             boolean taint = assignIO(self.getRuntime(), parser, new IRubyObject[] {port});
             parser.handler(new BytecodeNodeHandler());
             parser.errorHandler(null);
             parser.implicitTyping(false);
             parser.taguriExpansion(false);
             Bytestring sav = (Bytestring)parser.parse();
             int len = Bytestring.strlen(sav.buffer);
             ByteList bl = new ByteList(new byte[len+2], false);
             bl.append(sav.buffer, 0, len);
             bl.append('D');
             bl.append('\n');
             IRubyObject iro = RubyString.newStringLight(self.getRuntime(), bl);
             if(taint) iro.setTaint(true);
             return iro;
         }
     }
 
     public static class Resolver {
         // syck_const_find
         public static IRubyObject const_find(IRubyObject self, IRubyObject const_name) {
             RubyModule tclass = self.getRuntime().getObject();
             RubyArray tparts = ((RubyString)const_name).split(self.getRuntime().getCurrentContext(), self.getRuntime().newString("::"));
             for(int i=0; i < tparts.getLength(); i++) {
                 String tpart = tparts.entry(i).toString();
                 if(!tclass.hasConstant(tpart)) {
                     return self.getRuntime().getNil();
                 }
                 tclass = (RubyModule)tclass.getConstant(tpart);
             }
             return tclass;
         }
 
         // syck_resolver_initialize
         @JRubyMethod
         public static IRubyObject initialize(IRubyObject self) {
             self.getInstanceVariables().setInstanceVariable("@tags", RubyHash.newHash(self.getRuntime()));
             return self;
         }
 
         // syck_resolver_add_type
         @JRubyMethod
         public static IRubyObject add_type(IRubyObject self, IRubyObject taguri, IRubyObject cls) {
             IRubyObject tags = self.callMethod(self.getRuntime().getCurrentContext(), "tags");
             ((RubyHash)tags).fastASet(taguri, cls);
             return self.getRuntime().getNil();
         }        
 
         // syck_resolver_use_types_at
         @JRubyMethod
         public static IRubyObject use_types_at(IRubyObject self, IRubyObject hsh) {
             self.getInstanceVariables().setInstanceVariable("@tags", hsh);
             return self.getRuntime().getNil();
         }        
 
         // syck_resolver_detect_implicit
         @JRubyMethod
         public static IRubyObject detect_implicit(IRubyObject self, IRubyObject val) {
             return RubyString.newEmptyString(self.getRuntime());
         }        
 
         // syck_resolver_transfer
         @JRubyMethod
         public static IRubyObject transfer(IRubyObject self, IRubyObject type, IRubyObject val) {
             final Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
             if(type.isNil() || type.convertToString().getByteList().realSize == 0) {
                 type = self.callMethod(ctx, "detect_implicit", val);
             }
             
             if(!(type.isNil() || type.convertToString().getByteList().realSize == 0)) {
                 IRubyObject colon = runtime.newString(":");
                 IRubyObject tags = self.callMethod(ctx, "tags");
                 IRubyObject target_class = ((RubyHash)tags).op_aref(ctx, type);
                 IRubyObject subclass = target_class;
                 IRubyObject obj = runtime.getNil();
                 
                 if(target_class.isNil()) {
                     RubyArray subclass_parts = runtime.newArray();
                     RubyArray parts = ((RubyString)type).split(ctx, colon);
                     while(parts.getLength() > 1) {
                         subclass_parts.unshift(parts.pop(ctx));
                         IRubyObject partial = parts.join(ctx, colon);
                         target_class = ((RubyHash)tags).op_aref(ctx, partial);
                         if(target_class.isNil()) {
                             ((RubyString)partial).append(colon);
                             target_class = ((RubyHash)tags).op_aref(ctx, partial);
                         }
                         if(!target_class.isNil()) {
                             subclass = target_class;
                             if(subclass_parts.getLength() > 0 && target_class.respondsTo("yaml_tag_subclasses?") && target_class.callMethod(ctx, "yaml_tag_subclasses?").isTrue()) {
                                 subclass = subclass_parts.join(ctx, colon);
                                 subclass = target_class.callMethod(ctx, "yaml_tag_read_class", subclass);
                                 IRubyObject subclass_v = const_find(self, subclass);
                                 if(subclass_v != runtime.getNil()) {
                                     subclass = subclass_v;
                                 } else if(target_class == runtime.getObject() && subclass_v == runtime.getNil()) {
                                     target_class = ((RubyModule)runtime.getModule("YAML")).getConstant("Object");
                                     type = subclass;
                                     subclass = target_class;
                                 } else {
                                     throw runtime.newTypeError("invalid subclass");
                                 }
                             }
                             break;
                         }
                     }
                 }
 
                 if(target_class.respondsTo("call")) {
                     obj = target_class.callMethod(ctx, "call", new IRubyObject[]{type, val});
                 } else {
                     if(target_class.respondsTo("yaml_new")) {
                         obj = target_class.callMethod(ctx, "yaml_new", new IRubyObject[]{subclass, type, val});
                     } else if(!target_class.isNil()) {
                         if(subclass == runtime.getBignum()) {
                             obj = RubyNumeric.str2inum(runtime, val.convertToString(), 10);
                         } else {
                             obj = ((RubyClass)subclass).allocate();
                         }
                         
                         if(obj.respondsTo("yaml_initialize")) {
                             obj.callMethod(ctx, "yaml_initialize", new IRubyObject[]{type, val});
                         } else if(!obj.isNil() && val instanceof RubyHash) {
                             final IRubyObject _obj = obj;
                             RubyEnumerable.callEach(runtime, ctx, val, new BlockCallback() {
                                     public IRubyObject call(ThreadContext _ctx, IRubyObject[] largs, Block blk) {
                                         IRubyObject ivname = ((RubyArray)largs[0]).entry(0);
                                         String ivn = "@" + ivname.convertToString().toString();
                                         _obj.getInstanceVariables().setInstanceVariable(ivn, ((RubyArray)largs[0]).entry(1));
                                         return runtime.getNil();
                                     }
                                 });
                         }
                     } else {
                         RubyArray parts = ((RubyString)type).split(ctx, colon);
                         IRubyObject scheme = parts.shift(ctx);
                         if(scheme.convertToString().toString().equals("x-private")) {
                             IRubyObject name = parts.join(ctx, colon);
                             obj = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("PrivateType").callMethod(ctx, "new", new IRubyObject[]{name, val});
                         } else {
                             IRubyObject domain = parts.shift(ctx);
                             IRubyObject name = parts.join(ctx, colon);
                             obj = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("DomainType").callMethod(ctx, "new", new IRubyObject[]{domain, name, val});
                         }
                     }
                 }
                 
                 val = obj;
             }
 
             return val;
 
         }        
 
         // syck_resolver_node_import
         @JRubyMethod
         public static IRubyObject node_import(IRubyObject self, IRubyObject node) {
 //             System.err.println("syck_resolver_node_import()");
             final Ruby runtime = self.getRuntime();
             final ThreadContext ctx = runtime.getCurrentContext();
             org.yecht.Node n = (org.yecht.Node)node.dataGetStructChecked();
             IRubyObject obj = null;
             switch(n.kind) {
             case Str:
                 Data.Str dd = (Data.Str)n.data;
                 obj = RubyString.newStringShared(runtime, dd.ptr.buffer, dd.ptr.start, dd.len);
                 break;
             case Seq:
                 Data.Seq ds = (Data.Seq)n.data;
                 obj = RubyArray.newArray(runtime, ds.idx);
                 for(int i = 0; i < ds.idx; i++) {
                     IRubyObject obj2 = (IRubyObject)n.seqRead(i);
                     ((RubyArray)obj).store(i, obj2);
                 }
                 break;
             case Map:
                 Data.Map dm = (Data.Map)n.data;
                 obj = RubyHash.newHash(runtime);
                 RubyClass cMergeKey = (RubyClass)(((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("MergeKey"));
                 RubyClass cDefaultKey = (RubyClass)(((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("DefaultKey"));
                 RubyClass cHash = runtime.getHash();
                 RubyClass cArray = runtime.getArray();
                 
                 for(int i = 0; i < dm.idx; i++) {
                     IRubyObject k = (IRubyObject)n.mapRead(MapPart.Key, i);
                     IRubyObject v = (IRubyObject)n.mapRead(MapPart.Value, i);
                     if(null == v) {
                         v = runtime.getNil();
                     }
                     boolean skip_aset = false;
                     
                     if(cMergeKey.isInstance(k)) {
                         if(cHash.isInstance(v)) {
                             IRubyObject dup = v.callMethod(ctx, "dup");
                             dup.callMethod(ctx, "update", obj);
                             obj = dup;
                             skip_aset = true;
                         } else if(cArray.isInstance(v)) {
                             IRubyObject end = ((RubyArray)v).pop(ctx);
                             if(cHash.isInstance(end)) {
                                 final IRubyObject dup = end.callMethod(ctx, "dup");
                                 v = ((RubyArray)v).reverse();
                                 ((RubyArray)v).append(obj);
 
                                 RubyEnumerable.callEach(runtime, ctx, v, new BlockCallback() {
                                         // syck_merge_i
                                         public IRubyObject call(ThreadContext _ctx, IRubyObject[] largs, Block blk) {
                                             IRubyObject entry = largs[0];
                                             IRubyObject tmp = null;
                                             if(!(tmp = TypeConverter.convertToTypeWithCheck(entry, runtime.getHash(), "to_hash")).isNil()) {
                                                 dup.callMethod(_ctx, "update", tmp);
                                             }
                                             return runtime.getNil();
                                         }
                                     });
 
                                 obj = dup;
                                 skip_aset = true;
                             }
                         }
                     } else if(cDefaultKey.isInstance(k)) {
                         obj.callMethod(ctx, "default=", v);
                         skip_aset = true;
                     }
                     
                     if(!skip_aset) {
                         ((RubyHash)obj).fastASet(k, v);
                     }
                 }
                 break;
             }
             
             if(n.type_id != null) {
                 obj = self.callMethod(ctx, "transfer", new IRubyObject[]{runtime.newString(n.type_id), obj});
             }
 
             return obj;
         }
 
         // syck_resolver_tagurize
         @JRubyMethod
         public static IRubyObject tagurize(IRubyObject self, IRubyObject val) {
             IRubyObject tmp = val.checkStringType();
             if(!tmp.isNil()) {
                 String taguri = ImplicitScanner.typeIdToUri(tmp.toString());
                 val = self.getRuntime().newString(taguri);
             }
             return val;
         }        
     }
 
     public static class DefaultResolver {
         // syck_defaultresolver_node_import
         @JRubyMethod
         public static IRubyObject node_import(IRubyObject self, IRubyObject node) {
 //             System.err.println("syck_defaultresolver_node_import()");
             org.yecht.Node n = (org.yecht.Node)node.dataGetStructChecked();
             IRubyObject[] _obj = new IRubyObject[]{null};
             if(!orgHandler(self, n, _obj)) {
                 _obj[0] = self.callMethod(self.getRuntime().getCurrentContext(), "transfer", new IRubyObject[]{self.getRuntime().newString(n.type_id), _obj[0]});
             }
             return _obj[0];
         }        
 
         // syck_defaultresolver_detect_implicit
         @JRubyMethod
         public static IRubyObject detect_implicit(IRubyObject self, IRubyObject val) {
             IRubyObject tmp = TypeConverter.convertToTypeWithCheck(val, self.getRuntime().getString(), "to_str");
             if(!tmp.isNil()) {
                 ByteList bl = ((RubyString)tmp).getByteList();
                 String type_id = ImplicitScanner.matchImplicit(Pointer.create(bl.bytes, bl.begin), bl.realSize);
                 return self.getRuntime().newString(type_id);
             }
             return RubyString.newEmptyString(self.getRuntime());
         }        
     }
 
     public static class GenericResolver {
         // syck_genericresolver_node_import
         @JRubyMethod
         public static IRubyObject node_import(IRubyObject self, IRubyObject node) {
 //             System.err.println("syck_genericresolver_node_import()");
             Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
             org.yecht.Node n = (org.yecht.Node)node.dataGetStructChecked();
             IRubyObject t = runtime.getNil();
             IRubyObject obj = t;
             IRubyObject v = t;
             IRubyObject style = t;
 
             if(n.type_id != null) {
                 t = runtime.newString(n.type_id);
             }
 
             switch(n.kind) {
             case Str:
                 Data.Str dd = (Data.Str)n.data;
                 v = RubyString.newStringShared(runtime, dd.ptr.buffer, dd.ptr.start, dd.len);
                 switch(dd.style) {
                 case OneQuote:
                     style = runtime.newSymbol("quote1");
                     break;
                 case TwoQuote:
                     style = runtime.newSymbol("quote2");
                     break;
                 case Fold:
                     style = runtime.newSymbol("fold");
                     break;
                 case Literal:
                     style = runtime.newSymbol("literal");
                     break;
                 case Plain:
                     style = runtime.newSymbol("plain");
                     break;
                 }
                 obj = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("Scalar").callMethod(ctx, "new", new IRubyObject[]{t, v, style});
                 break;
             case Seq:
                 v = RubyArray.newArray(runtime, n.seqCount());
                 for(int i = 0; i < n.seqCount(); i++) {
                     IRubyObject obj3 = (IRubyObject)n.seqRead(i);
                     ((RubyArray)v).store(i, obj3);
                 }
                 if(((Data.Seq)n.data).style == SeqStyle.Inline) {
                     style = runtime.newSymbol("inline");
                 }
                 obj = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("Seq").callMethod(ctx, "new", new IRubyObject[]{t, v, style});
                 obj.getInstanceVariables().setInstanceVariable("@kind", runtime.newSymbol("seq"));
                 break;
             case Map:
                 v = RubyHash.newHash(runtime);
                 for(int i = 0; i < n.mapCount(); i++) {
                     IRubyObject k3 = (IRubyObject)n.mapRead(MapPart.Key, i);
                     IRubyObject v3 = (IRubyObject)n.mapRead(MapPart.Value, i);
                     if(null == v3) {
                         v3 = runtime.getNil();
                     }
 
                     ((RubyHash)v).fastASet(k3, v3);
                 }
                 if(((Data.Map)n.data).style == MapStyle.Inline) {
                     style = runtime.newSymbol("inline");
                 }
                 obj = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("Map").callMethod(ctx, "new", new IRubyObject[]{t, v, style});
                 obj.getInstanceVariables().setInstanceVariable("@kind", runtime.newSymbol("map"));
                 break;
             }
 
             return obj;
         }        
     }
 
     public static class YParser {
         public static class Extra {
             public IRubyObject data;
             public IRubyObject proc;
             public IRubyObject resolver;
             public boolean taint;
         }
 
         public static final ObjectAllocator Allocator = new ObjectAllocator() {
                 // syck_parser_s_alloc
                 public IRubyObject allocate(Ruby runtime, RubyClass klass) {
 //                     System.err.println("ALLOCATING PARSER");
                     Parser parser = Parser.newParser();
                     parser.bonus = new Extra();
                     IRubyObject pobj = runtime.newData(klass, parser);
                     parser.setRootOnError(runtime.getNil());
                     return pobj;
                 }
             };
 
         @JRubyMethod(optional = 1)
         public static IRubyObject initialize(IRubyObject self, IRubyObject[] args) {
             IRubyObject options = null;
             if(args.length == 0) {
                 options = RubyHash.newHash(self.getRuntime());
             } else {
                 options = args[0].convertToHash();
             }
             self.getInstanceVariables().setInstanceVariable("@options", options);
             self.getInstanceVariables().setInstanceVariable("@input", self.getRuntime().getNil());
             self.getInstanceVariables().setInstanceVariable("@resolver", self.getRuntime().getNil());
 
             return self;
         }
         
         // syck_parser_bufsize_set
         @JRubyMethod(name="bufsize=")
         public static IRubyObject bufsize_set(IRubyObject self, IRubyObject size) {
             if(size.respondsTo("to_i")) {
                 int n = RubyNumeric.fix2int(size.callMethod(self.getRuntime().getCurrentContext(), "to_i"));
                 Parser p = (Parser)self.dataGetStructChecked();
                 p.bufsize = n;
             }
             return self;
         }        
 
         // syck_parser_bufsize_get
         @JRubyMethod
         public static IRubyObject bufsize(IRubyObject self) {
             Parser p = (Parser)self.dataGetStructChecked();
             return self.getRuntime().newFixnum(p.bufsize);
         }        
         
         // syck_parser_load
         @JRubyMethod(required = 1, optional = 1)
         public static IRubyObject load(IRubyObject self, IRubyObject[] args) {
             Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
             IRubyObject port = args[0];
             IRubyObject proc = null;
             if(args.length > 1) {
                 proc = args[1];
             } else {
                 proc = runtime.getNil();
             }
 
             IRubyObject input = ((RubyHash)self.callMethod(ctx, "options")).op_aref(ctx, runtime.newSymbol("input"));
             IRubyObject model = ((RubyHash)self.callMethod(ctx, "options")).op_aref(ctx, runtime.newSymbol("Model"));
 
             Parser parser = (Parser)self.dataGetStructChecked();
             setModel(self, input, model);
             
             Extra bonus = (Extra)parser.bonus;
             bonus.taint = assignIO(runtime, parser, new IRubyObject[]{port});
             parser.setRootOnError(runtime.getNil());
             bonus.data = RubyHash.newHash(runtime);
             bonus.resolver = self.callMethod(ctx, "resolver");
 //             System.err.println("Parser resolver is : " + bonus.resolver);
             if(proc.isNil()) {
                 bonus.proc = null;
             } else {
                 bonus.proc = proc;
             }
 
             IRubyObject id = (IRubyObject)parser.parse();
             IRubyObject result = id;
             return result;
         }
 
         // syck_parser_load_documents
         @JRubyMethod(frame=true)
         public static IRubyObject load_documents(IRubyObject self, IRubyObject port, Block proc) {
             Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
 
             IRubyObject input = ((RubyHash)self.callMethod(ctx, "options")).op_aref(ctx, runtime.newSymbol("input"));
             IRubyObject model = ((RubyHash)self.callMethod(ctx, "options")).op_aref(ctx, runtime.newSymbol("Model"));
 
             Parser parser = (Parser)self.dataGetStructChecked();
             setModel(self, input, model);
 
             Extra bonus = (Extra)parser.bonus;
             bonus.taint = assignIO(runtime, parser, new IRubyObject[]{port});
             parser.setRootOnError(runtime.getNil());
             bonus.resolver = self.callMethod(ctx, "resolver");
             bonus.proc = null;
 
             while(true) {
                 bonus.data = RubyHash.newHash(runtime);
                 IRubyObject v = (IRubyObject)parser.parse();
                 if(parser.eof) {
                     return runtime.getNil();
                 }
 
                 proc.yield(ctx, v);
             }
         }
 
         // syck_parser_set_resolver
         @JRubyMethod
         public static IRubyObject set_resolver(IRubyObject self, IRubyObject resolver) {
             self.getInstanceVariables().setInstanceVariable("@resolver", resolver);
             return self;
         }        
     }
 
     public static class Node {
         // syck_node_init_copy
         @JRubyMethod
         public static IRubyObject initialize_copy(IRubyObject copy, IRubyObject orig) {
             if(copy == orig) {
                 return copy;
             }
 
             if(orig.getClass() != RubyObject.class) {
                 throw copy.getRuntime().newTypeError("wrong argument type");
             }
 
             org.yecht.Node orig_n = (org.yecht.Node)orig.dataGetStructChecked();
             org.yecht.Node copy_n = (org.yecht.Node)copy.dataGetStructChecked();
 
             copy_n.id = orig_n.id;
             copy_n.kind = orig_n.kind;
             copy_n.type_id = orig_n.type_id;
             copy_n.anchor = orig_n.anchor;
             copy_n.data = orig_n.data.copy();
 
             return copy;
         }        
 
         // syck_node_type_id_set
         @JRubyMethod(name = "type_id=")
         public static IRubyObject set_type_id(IRubyObject self, IRubyObject type_id) {
             org.yecht.Node node = (org.yecht.Node)self.dataGetStructChecked();
             if(!type_id.isNil()) {
                 node.type_id = type_id.convertToString().toString();
             }
             self.getInstanceVariables().setInstanceVariable("@type_id", type_id);
             return type_id;
         }        
 
         // syck_node_transform
         @JRubyMethod
         public static IRubyObject transform(IRubyObject self) {
 //             System.err.println("syck_node_transform()");
             Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
             org.yecht.Node orig_n = (org.yecht.Node)self.dataGetStructChecked();
             IRubyObject t = runtime.newData(self.getType(), null);
             org.yecht.Node n = null;
 
             switch(orig_n.kind) {
             case Map:
                 n = org.yecht.Node.allocMap();
                 t.dataWrapStruct(n);
                 Data.Map dm = (Data.Map)orig_n.data;
                 for(int i=0; i < dm.idx; i++) {
                     IRubyObject k = ((IRubyObject)orig_n.mapRead(MapPart.Key, i)).callMethod(ctx, "transform");
                     IRubyObject v = ((IRubyObject)orig_n.mapRead(MapPart.Value, i)).callMethod(ctx, "transform");
                     n.mapAdd(k, v);
                 }
                 break;
             case Seq:
                 n = org.yecht.Node.allocSeq();
                 t.dataWrapStruct(n);
                 Data.Seq ds = (Data.Seq)orig_n.data;
                 for(int i=0; i < ds.idx; i++) {
                     IRubyObject itm = ((IRubyObject)orig_n.seqRead(i)).callMethod(ctx, "transform");
                     n.seqAdd(itm);
                 }
                 break;
             case Str:
                 Data.Str dss = (Data.Str)orig_n.data;
                 n = org.yecht.Node.newStr(dss.ptr, dss.len, dss.style);
                 t.dataWrapStruct(n);
                 break;
             }
 
             if(orig_n.type_id != null) {
                 n.type_id = orig_n.type_id;
             }
 
             if(orig_n.anchor != null) {
                 n.anchor = orig_n.anchor;
             }
 
             n.id = t;
 //             System.err.println("syck_node_transform(), setting id of object on: " + n);
             IRubyObject result = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("DefaultResolver").callMethod(ctx, "node_import", t);
             return result;
         }
     }
 
     public static class Scalar {
         public static final ObjectAllocator Allocator = new ObjectAllocator() {
                 // syck_scalar_alloc
                 public IRubyObject allocate(Ruby runtime, RubyClass klass) {
 //                     System.err.println("ALLOCATING SCALAR");
                     org.yecht.Node node = org.yecht.Node.allocStr();
                     IRubyObject obj = runtime.newData(klass, node);
                     node.id = obj;
 //                     System.err.println("syck_scalar_alloc() -> setting id: " + node.id);
                     return obj;
                 }
             };
 
         // syck_scalar_initialize
         @JRubyMethod
         public static IRubyObject initialize(IRubyObject self, IRubyObject type_id, IRubyObject val, IRubyObject style) {
             Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
             self.getInstanceVariables().setInstanceVariable("@kind", runtime.newSymbol("scalar"));
             self.callMethod(ctx, "type_id=", type_id);
             self.callMethod(ctx, "value=", val);
             self.callMethod(ctx, "style=", style);
             return self;
         }
 
         // syck_scalar_style_set
         @JRubyMethod(name = "style=")
         public static IRubyObject style_set(IRubyObject self, IRubyObject style) {
             org.yecht.Node node = (org.yecht.Node)self.dataGetStructChecked();
             Ruby runtime = self.getRuntime();
             Data.Str ds = (Data.Str)node.data;
             if(style.isNil()) {
                 ds.style = ScalarStyle.None;
             } else if(style == runtime.newSymbol("quote1")) {
                 ds.style = ScalarStyle.OneQuote;
             } else if(style == runtime.newSymbol("quote2")) {
                 ds.style = ScalarStyle.TwoQuote;
             } else if(style == runtime.newSymbol("fold")) {
                 ds.style = ScalarStyle.Fold;
             } else if(style == runtime.newSymbol("literal")) {
                 ds.style = ScalarStyle.Literal;
             } else if(style == runtime.newSymbol("plain")) {
                 ds.style = ScalarStyle.Plain;
             }
             self.getInstanceVariables().setInstanceVariable("@style", style);
             return self;
         }
 
         // syck_scalar_value_set
         @JRubyMethod(name = "value=")
         public static IRubyObject value_set(IRubyObject self, IRubyObject val) {
             org.yecht.Node node = (org.yecht.Node)self.dataGetStructChecked();
             Ruby runtime = self.getRuntime();
             Data.Str ds = (Data.Str)node.data;
             
             val = val.convertToString();
             ByteList bl = ((RubyString)val).getByteList();
             byte[] bss = new byte[bl.realSize];
             System.arraycopy(bl.bytes, bl.begin, bss, 0, bss.length);
             ds.ptr = Pointer.create(bss, 0);
             ds.len = bss.length;
             ds.style = ScalarStyle.None;
             self.getInstanceVariables().setInstanceVariable("@value", val);
             return val;
         }
     }
 
     public static class Seq {
         public static final ObjectAllocator Allocator = new ObjectAllocator() {
                 // syck_seq_alloc
                 public IRubyObject allocate(Ruby runtime, RubyClass klass) {
 //                     System.err.println("ALLOCATING SEQ");
                     org.yecht.Node node = org.yecht.Node.allocSeq();
                     IRubyObject obj = runtime.newData(klass, node);
                     node.id = obj;
 //                     System.err.println("syck_seq_alloc() -> setting id");
                     return obj;
                 }
             };
 
         // syck_seq_initialize
         @JRubyMethod
         public static IRubyObject initialize(IRubyObject self, IRubyObject type_id, IRubyObject val, IRubyObject style) {
             Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
             self.getInstanceVariables().setInstanceVariable("@kind", runtime.newSymbol("seq"));
             self.callMethod(ctx, "type_id=", type_id);
             self.callMethod(ctx, "value=", val);
             self.callMethod(ctx, "style=", style);
             return self;
         }
 
         // syck_seq_value_set
         @JRubyMethod(name = "value=")
         public static IRubyObject value_set(IRubyObject self, IRubyObject val) {
             org.yecht.Node node = (org.yecht.Node)self.dataGetStructChecked();
             Ruby runtime = self.getRuntime();
 
             val = val.checkArrayType();
             if(!val.isNil()) {
                 node.seqEmpty();
                 Data.Seq ds = (Data.Seq)node.data;
                 for(int i=0; i<((RubyArray)val).getLength(); i++) {
                     node.seqAdd(((RubyArray)val).entry(i));
                 }
             }
 
             self.getInstanceVariables().setInstanceVariable("@value", val);
             return val;
         }
 
         // syck_seq_style_set
         @JRubyMethod(name = "style=")
         public static IRubyObject style_set(IRubyObject self, IRubyObject style) {
             org.yecht.Node node = (org.yecht.Node)self.dataGetStructChecked();
             Ruby runtime = self.getRuntime();
             Data.Seq ds = (Data.Seq)node.data;
             if(style == runtime.newSymbol("inline")) {
                 ds.style = SeqStyle.Inline;
             } else {
                 ds.style = SeqStyle.None;
             }
 
             self.getInstanceVariables().setInstanceVariable("@style", style);
             return self;
         }
 
         // syck_seq_add_m
         @JRubyMethod
         public static IRubyObject add(IRubyObject self, IRubyObject val) {
             IRubyObject emitter = self.getInstanceVariables().getInstanceVariable("@emitter");
             org.yecht.Node node = (org.yecht.Node)self.dataGetStructChecked();
             if(emitter.respondsTo("node_export")) {
                 val = emitter.callMethod(self.getRuntime().getCurrentContext(), "node_export", val);
             }
             node.seqAdd(val);
             ((RubyArray)self.getInstanceVariables().getInstanceVariable("@value")).append(val);
             return self;
         }
     }
 
     public static class Map {
         public static final ObjectAllocator Allocator = new ObjectAllocator() {
                 // syck_map_alloc
                 public IRubyObject allocate(Ruby runtime, RubyClass klass) {
 //                     System.err.println("ALLOCATING MAP");
                     org.yecht.Node node = org.yecht.Node.allocMap();
                     IRubyObject obj = runtime.newData(klass, node);
                     node.id = obj;
 //                     System.err.println("syck_map_alloc() -> setting id");
                     return obj;
                 }
             };
 
         // syck_map_initialize
         @JRubyMethod
         public static IRubyObject initialize(IRubyObject self, IRubyObject type_id, IRubyObject val, IRubyObject style) {
             org.yecht.Node node = (org.yecht.Node)self.dataGetStructChecked();
             Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
             Data.Map ds = (Data.Map)node.data;
 
             if(!val.isNil()) {
                 IRubyObject hsh = TypeConverter.convertToTypeWithCheck(val, runtime.getHash(), "to_hash");
 
                 if(hsh.isNil()) {
                     throw runtime.newTypeError("wrong argument type");
                 }
                 IRubyObject keys = hsh.callMethod(ctx, "keys");
                 for(int i = 0; i < ((RubyArray)keys).getLength(); i++) {
                     IRubyObject key = ((RubyArray)keys).entry(i);
                     node.mapAdd(key, ((RubyHash)hsh).op_aref(ctx, key));
                 }
             }
 
             self.getInstanceVariables().setInstanceVariable("@kind", runtime.newSymbol("seq")); // NOT A TYPO - Syck does the same
             self.callMethod(ctx, "type_id=", type_id);
             self.callMethod(ctx, "value=", val);
             self.callMethod(ctx, "style=", style);
             return self;
         }
 
         // syck_map_value_set
         @JRubyMethod(name = "value=")
         public static IRubyObject value_set(IRubyObject self, IRubyObject val) {
             org.yecht.Node node = (org.yecht.Node)self.dataGetStructChecked();
             Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
 
             if(!val.isNil()) {
                 IRubyObject hsh = TypeConverter.convertToTypeWithCheck(val, runtime.getHash(), "to_hash");
 
                 if(hsh.isNil()) {
                     throw runtime.newTypeError("wrong argument type");
                 }
                 node.mapEmpty();
                 IRubyObject keys = hsh.callMethod(ctx, "keys");
                 for(int i = 0; i < ((RubyArray)keys).getLength(); i++) {
                     IRubyObject key = ((RubyArray)keys).entry(i);
                     node.mapAdd(key, ((RubyHash)hsh).op_aref(ctx, key));
                 }
             }
 
             self.getInstanceVariables().setInstanceVariable("@value", val);
             return val;
         }
 
         // syck_map_add_m
         @JRubyMethod
         public static IRubyObject add(IRubyObject self, IRubyObject key, IRubyObject val) {
             IRubyObject emitter = self.getInstanceVariables().getInstanceVariable("@emitter");
             org.yecht.Node node = (org.yecht.Node)self.dataGetStructChecked();
             if(emitter.respondsTo("node_export")) {
                 key = emitter.callMethod(self.getRuntime().getCurrentContext(), "node_export", key);
                 val = emitter.callMethod(self.getRuntime().getCurrentContext(), "node_export", val);
             }
             node.mapAdd(key, val);
             ((RubyHash)self.getInstanceVariables().getInstanceVariable("@value")).fastASet(key, val);
             return self;
         }
 
         // syck_map_style_set
         @JRubyMethod(name = "style=")
         public static IRubyObject style_set(IRubyObject self, IRubyObject style) {
             org.yecht.Node node = (org.yecht.Node)self.dataGetStructChecked();
             Ruby runtime = self.getRuntime();
             Data.Map ds = (Data.Map)node.data;
             if(style == runtime.newSymbol("inline")) {
                 ds.style = MapStyle.Inline;
             } else {
                 ds.style = MapStyle.None;
             }
 
             self.getInstanceVariables().setInstanceVariable("@style", style);
             return self;
         }
     }
 
     public static class PrivateType {
         // syck_privatetype_initialize
         @JRubyMethod
         public static IRubyObject initialize(IRubyObject self, IRubyObject type_id, IRubyObject val) {
             self.getInstanceVariables().setInstanceVariable("@type_id", type_id);
             self.getInstanceVariables().setInstanceVariable("@value", val);
             return self;
         }
     }
 
     public static class DomainType {
         // syck_domaintype_initialize
         @JRubyMethod
         public static IRubyObject initialize(IRubyObject self, IRubyObject domain, IRubyObject type_id, IRubyObject val) {
             self.getInstanceVariables().setInstanceVariable("@domain", domain);
             self.getInstanceVariables().setInstanceVariable("@type_id", type_id);
             self.getInstanceVariables().setInstanceVariable("@value", val);
             return self;
         }
     }
 
     public static class YObject {
         // syck_yobject_initialize
         @JRubyMethod
         public static IRubyObject initialize(IRubyObject self, IRubyObject klass, IRubyObject ivars) {
             self.getInstanceVariables().setInstanceVariable("@class", klass);
             self.getInstanceVariables().setInstanceVariable("@ivars", ivars);
             return self;
         }
 
         // syck_yobject_initialize
         @JRubyMethod
         public static IRubyObject yaml_initialize(IRubyObject self, IRubyObject klass, IRubyObject ivars) {
             self.getInstanceVariables().setInstanceVariable("@class", klass);
             self.getInstanceVariables().setInstanceVariable("@ivars", ivars);
             return self;
         }
     }
 
     // syck_out_mark
     public static void outMark(IRubyObject emitter, IRubyObject node) {
         Emitter emitterPtr = (Emitter)emitter.dataGetStructChecked();
         YEmitter.Extra bonus = (YEmitter.Extra)emitterPtr.bonus;
         node.getInstanceVariables().setInstanceVariable("@emitter", emitter);
         if(!bonus.oid.isNil()) {
             ((RubyHash)bonus.data).fastASet(bonus.oid, node);
         }
     }
 
     public static class Out {
         // syck_out_initialize
         @JRubyMethod
         public static IRubyObject initialize(IRubyObject self, IRubyObject emitter) {
             self.getInstanceVariables().setInstanceVariable("@emitter", emitter);
             return self;
         }
 
         // syck_out_map
         @JRubyMethod(required = 1, optional = 1, frame = true)
         public static IRubyObject map(IRubyObject self, IRubyObject[] args, Block block) {
             Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
             IRubyObject type_id = args[0];
             IRubyObject style = args.length == 1 ? runtime.getNil() : args[1];
             IRubyObject map = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("Map").callMethod(ctx, "new", new IRubyObject[]{type_id, RubyHash.newHash(runtime), style});
             outMark(self.getInstanceVariables().getInstanceVariable("@emitter"), map);
             block.yield(ctx, map);
             return map;
         }
 
         // syck_out_seq
         @JRubyMethod(required = 1, optional = 1, frame = true)
         public static IRubyObject seq(IRubyObject self, IRubyObject[] args, Block block) {
             Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
             IRubyObject type_id = args[0];
             IRubyObject style = args.length == 1 ? runtime.getNil() : args[1];
             IRubyObject seq = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("Seq").callMethod(ctx, "new", new IRubyObject[]{type_id, RubyArray.newArray(runtime), style});
             outMark(self.getInstanceVariables().getInstanceVariable("@emitter"), seq);
             block.yield(ctx, seq);
             return seq;
         }
 
         // syck_out_scalar
         @JRubyMethod(required = 2, optional = 1, frame = true)
         public static IRubyObject scalar(IRubyObject self, IRubyObject[] args, Block block) {
             Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
             IRubyObject type_id = args[0];
             IRubyObject str = args[1];
             IRubyObject style = args.length == 2 ? runtime.getNil() : args[2];
             IRubyObject scalar = ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("Scalar").callMethod(ctx, "new", new IRubyObject[]{type_id, str, style});
             outMark(self.getInstanceVariables().getInstanceVariable("@emitter"), scalar);
             return scalar;
         }
     }
 
     public static class RubyEmitterHandler implements EmitterHandler { 
         private Ruby runtime;
 
         public RubyEmitterHandler(Ruby runtime) {
             this.runtime = runtime;
         }
 
         // rb_syck_emitter_handler
         public void handle(Emitter e, Object data) {
             org.yecht.Node n = (org.yecht.Node)((IRubyObject)data).dataGetStructChecked();
             switch(n.kind) {
             case Map:
                 Data.Map dm = (Data.Map)n.data;
                 e.emitMap(n.type_id, dm.style);
                 for(int i = 0; i < dm.idx; i++) {
                     e.emitItem(n.mapRead(MapPart.Key, i));
                     e.emitItem(n.mapRead(MapPart.Value, i));
                 }
                 e.emitEnd();
                 break;
             case Seq:
                 Data.Seq ds = (Data.Seq)n.data;
                 e.emitSeq(n.type_id, ds.style);
                 for(int i = 0; i < ds.idx; i++) {
                     e.emitItem(n.seqRead(i));
                 }
                 e.emitEnd();
                 break;
             case Str:
                 Data.Str dss = (Data.Str)n.data;
                 e.emitScalar(n.type_id, dss.style, 0, 0, 0, dss.ptr, dss.len);
                 break;
             }
         }
     }
 
     public static class RubyOutputHandler implements OutputHandler {
         private Ruby runtime;
 
         public RubyOutputHandler(Ruby runtime) {
             this.runtime = runtime;
         }
 
         // rb_syck_output_handler
         public void handle(Emitter emitter, byte[] str, int len) {
             YEmitter.Extra bonus = (YEmitter.Extra)emitter.bonus;
             IRubyObject dest = bonus.port;
             if(dest instanceof RubyString) {
                 ((RubyString)dest).cat(new ByteList(str, 0, len, false));
             } else {
                 dest.callMethod(runtime.getCurrentContext(), "write", RubyString.newStringShared(runtime, str, 0, len));
             }
         }
     }
 
     public static class YEmitter {
         public static class Extra {
             public IRubyObject oid;
             public IRubyObject data;
             public IRubyObject port;
         }
 
         public static final ObjectAllocator Allocator = new ObjectAllocator() {
                 // syck_emitter_s_alloc
                 public IRubyObject allocate(Ruby runtime, RubyClass klass) {
 //                     System.err.println("ALLOCATING EMITTER");
                     Emitter emitter = new Emitter();
                     emitter.bonus = new Extra();
                     IRubyObject pobj = runtime.newData(klass, emitter);
                     emitter.handler(new RubyEmitterHandler(runtime));
                     emitter.outputHandler(new RubyOutputHandler(runtime));
                     
                     pobj.getInstanceVariables().setInstanceVariable("@out", ((RubyModule)((RubyModule)runtime.getModule("YAML")).getConstant("Yecht")).getConstant("Out").callMethod(runtime.getCurrentContext(), "new", pobj));
                     return pobj;
                 }
             };
 
 
         // syck_emitter_set_resolver
         @JRubyMethod
         public static IRubyObject set_resolver(IRubyObject self, IRubyObject resolver) {
             self.getInstanceVariables().setInstanceVariable("@resolver", resolver);
             return self;
         }
 
         // syck_emitter_node_export
         @JRubyMethod
         public static IRubyObject node_export(IRubyObject self, IRubyObject node) {
             return node.callMethod(self.getRuntime().getCurrentContext(), "to_yaml", self);
         }
 
         // syck_emitter_reset
         @JRubyMethod(name = {"initialize", "reset"}, optional = 1)
         public static IRubyObject reset(IRubyObject self, IRubyObject[] args) {
             Ruby runtime = self.getRuntime();
             ThreadContext ctx = runtime.getCurrentContext();
             Emitter emitter = (Emitter)self.dataGetStructChecked();
             Extra bonus = (Extra)emitter.bonus;
             bonus.oid = runtime.getNil();
             bonus.port = runtime.newString("");
             bonus.data = RubyHash.newHash(runtime);
             
             IRubyObject options = null;
             IRubyObject tmp;
             if(args.length == 1) {
                 options = args[0];
                 if(!(tmp = options.checkStringType()).isNil()) {
                     bonus.port = tmp;
                 } else if(options.respondsTo("write")) {
                     bonus.port = options;
                 } else {
                     options = TypeConverter.convertToTypeWithCheck(options, runtime.getHash(), "to_hash");
                     self.getInstanceVariables().setInstanceVariable("@options", options);
                 }
             } else {
                 options = RubyHash.newHash(runtime);
                 self.getInstanceVariables().setInstanceVariable("@options", options);
             }
 
 
             emitter.headless = false;
             self.getInstanceVariables().setInstanceVariable("@level", runtime.newFixnum(0));
             self.getInstanceVariables().setInstanceVariable("@resolver", runtime.getNil());
 
             return self;
         }
 
         // syck_emitter_emit
         @JRubyMethod(optional = 1, frame = true)
         public static IRubyObject emit(IRubyObject self, IRubyObject[] _oid, Block proc) {
             Ruby runtime = self.getRuntime();
             int level = RubyNumeric.fix2int(self.getInstanceVariables().getInstanceVariable("@level")) + 1;
             self.getInstanceVariables().setInstanceVariable("@level", runtime.newFixnum(level));
             ThreadContext ctx = runtime.getCurrentContext();
             Emitter emitter = (Emitter)self.dataGetStructChecked();
             Extra bonus = (Extra)emitter.bonus;
 
             IRubyObject oid = _oid.length == 0 ? runtime.getNil() : _oid[0];
             
             bonus.oid = oid;
             IRubyObject symple;
             if(!oid.isNil() && bonus.data.callMethod(ctx, "has_key?", oid).isTrue()) {
                 symple = ((RubyHash)bonus.data).op_aref(ctx, oid);
             } else {
                 symple = proc.yield(ctx, self.getInstanceVariables().getInstanceVariable("@out"));
             }
             emitter.markNode(symple);
 
             level--;
             self.getInstanceVariables().setInstanceVariable("@level", runtime.newFixnum(level));
             if(level == 0) {
                 emitter.emit(symple);
                 emitter.flush(0);
                 return bonus.port;
             }
             return symple;
         }
     }
 }
 
