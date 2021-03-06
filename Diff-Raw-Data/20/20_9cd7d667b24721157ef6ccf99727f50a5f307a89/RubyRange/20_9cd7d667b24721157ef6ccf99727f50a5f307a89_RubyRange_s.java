 /***** BEGIN LICENSE BLOCK *****
  * Version: CPL 1.0/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Common Public
  * License Version 1.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.eclipse.org/legal/cpl-v10.html
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * Copyright (C) 2001 Chad Fowler <chadfowler@chadfowler.com>
  * Copyright (C) 2001 Alan Moore <alan_moore@gmx.net>
  * Copyright (C) 2001 Ed Sinjiashvili <slorcim@users.sourceforge.net>
  * Copyright (C) 2001-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
  * Copyright (C) 2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
  * Copyright (C) 2002-2006 Thomas E Enebo <enebo@acm.org>
  * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
  * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
  * Copyright (C) 2005 Charles O Nutter <headius@headius.com>
  * Copyright (C) 2006 Miguel Covarrubias <mlcovarrubias@gmail.com>
  * 
  * Alternatively, the contents of this file may be used under the terms of
  * either of the GNU General Public License Version 2 or later (the "GPL"),
  * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the CPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the CPL, the GPL or the LGPL.
  ***** END LICENSE BLOCK *****/
 package org.jruby;
 
 import java.io.IOException;
 import java.util.List;
 import org.jruby.anno.JRubyMethod;
 import org.jruby.exceptions.RaiseException;
 import org.jruby.runtime.Arity;
 import org.jruby.runtime.Block;
 import org.jruby.runtime.CallbackFactory;
 import org.jruby.runtime.MethodIndex;
 import org.jruby.runtime.ObjectAllocator;
 import org.jruby.runtime.ObjectMarshal;
 import org.jruby.runtime.ThreadContext;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.jruby.runtime.builtin.Variable;
 import org.jruby.runtime.component.VariableEntry;
 import org.jruby.runtime.marshal.MarshalStream;
 import org.jruby.runtime.marshal.UnmarshalStream;
 
 /**
  * @author jpetersen
  */
 public class RubyRange extends RubyObject {
 
     private IRubyObject begin;
     private IRubyObject end;
     private boolean isExclusive;
 
     public RubyRange(Ruby runtime, RubyClass impl) {
         super(runtime, impl);
         begin = end = runtime.getNil();
     }
 
     public void init(IRubyObject aBegin, IRubyObject aEnd, RubyBoolean aIsExclusive) {
         if (!(aBegin instanceof RubyFixnum && aEnd instanceof RubyFixnum)) {
             try {
                 IRubyObject result = aBegin.callMethod(getRuntime().getCurrentContext(), MethodIndex.OP_SPACESHIP, "<=>", aEnd);
                 if (result.isNil()) throw getRuntime().newArgumentError("bad value for range");
             } catch (RaiseException rExcptn) {
                 throw getRuntime().newArgumentError("bad value for range");
             }
         }
 
         this.begin = aBegin;
         this.end = aEnd;
         this.isExclusive = aIsExclusive.isTrue();
     }
     
     private static ObjectAllocator RANGE_ALLOCATOR = new ObjectAllocator() {
         public IRubyObject allocate(Ruby runtime, RubyClass klass) {
             return new RubyRange(runtime, klass);
         }
     };
     
     protected void copySpecialInstanceVariables(IRubyObject clone) {
         RubyRange range = (RubyRange)clone;
         range.begin = begin;
         range.end = end;
         range.isExclusive = isExclusive;
     }
 
     private static final ObjectMarshal RANGE_MARSHAL = new ObjectMarshal() {
         public void marshalTo(Ruby runtime, Object obj, RubyClass type,
                               MarshalStream marshalStream) throws IOException {
             RubyRange range = (RubyRange)obj;
 
             List<Variable<IRubyObject>> attrs = range.getVariableList();
 
             attrs.add(new VariableEntry<IRubyObject>("begin", range.begin));
             attrs.add(new VariableEntry<IRubyObject>("end", range.end));
             attrs.add(new VariableEntry<IRubyObject>(
                     "excl", range.isExclusive ? runtime.getTrue() : runtime.getFalse()));
 
             marshalStream.dumpVariables(attrs);
         }
 
         public Object unmarshalFrom(Ruby runtime, RubyClass type,
                                     UnmarshalStream unmarshalStream) throws IOException {
             RubyRange range = (RubyRange)type.allocate();
             
             unmarshalStream.registerLinkTarget(range);
 
             unmarshalStream.defaultVariablesUnmarshal(range);
             
             range.begin = range.removeInternalVariable("begin");
             range.end = range.removeInternalVariable("end");
             range.isExclusive = range.removeInternalVariable("excl").isTrue();
 
             return range;
         }
     };
     
     public static RubyClass createRangeClass(Ruby runtime) {
         RubyClass result = runtime.defineClass("Range", runtime.getObject(), RANGE_ALLOCATOR);
         runtime.setRange(result);
         result.kindOf = new RubyModule.KindOf() {
                 public boolean isKindOf(IRubyObject obj, RubyModule type) {
                     return obj instanceof RubyRange;
                 }
             };
         
         result.setMarshal(RANGE_MARSHAL);
         
         CallbackFactory callbackFactory = runtime.callbackFactory(RubyRange.class);
         
         result.includeModule(runtime.getEnumerable());
 
         // We override Enumerable#member? since ranges in 1.8.1 are continuous.
         //        result.defineMethod("member?", callbackFactory.getMethod("include_p", RubyKernel.IRUBY_OBJECT));
         //        result.defineMethod("===", callbackFactory.getMethod("include_p", RubyKernel.IRUBY_OBJECT));
 
         result.defineAnnotatedMethods(RubyRange.class);
         
         CallbackFactory classCB = runtime.callbackFactory(RubyClass.class);
         result.getMetaClass().defineMethod("new", classCB.getOptMethod("newInstance"));
         
         result.dispatcher = callbackFactory.createDispatcher(result);
         
         return result;
     }
 
     /**
      * Converts this Range to a pair of integers representing a start position 
      * and length.  If either of the range's endpoints is negative, it is added to 
      * the <code>limit</code> parameter in an attempt to arrive at a position 
      * <i>p</i> such that <i>0&nbsp;&lt;=&nbsp;p&nbsp;&lt;=&nbsp;limit</i>. If 
      * <code>truncate</code> is true, the result will be adjusted, if possible, so 
      * that <i>begin&nbsp;+&nbsp;length&nbsp;&lt;=&nbsp;limit</i>.  If <code>strict</code> 
      * is true, an exception will be raised if the range can't be converted as 
      * described above; otherwise it just returns <b>null</b>. 
      * 
      * @param limit    the size of the object (e.g., a String or Array) that 
      *                 this range is being evaluated against.
      * @param truncate if true, result must fit within the range <i>(0..limit)</i>.
      * @param isStrict   if true, raises an exception if the range can't be converted.
      * @return         a two-element array representing a start value and a length, 
      *                 or <b>null</b> if the conversion failed.
      */
     public long[] getBeginLength(long limit, boolean truncate, boolean isStrict) {
         long beginLong = RubyNumeric.num2long(begin);
         long endLong = RubyNumeric.num2long(end);
 
         // Apparent legend for MRI 'err' param to JRuby 'truncate' and 'isStrict':
         // 0 =>  truncate && !strict
         // 1 => !truncate &&  strict
         // 2 =>  truncate &&  strict
         if (!isExclusive) {
             endLong++;
         }
 
         if (beginLong < 0) {
             beginLong += limit;
             if (beginLong < 0) {
                 if (isStrict) {
                     throw getRuntime().newRangeError(inspect().toString() + " out of range.");
                 }
                 return null;
             }
         }
 
         if (truncate && beginLong > limit) {
             if (isStrict) {
                 throw getRuntime().newRangeError(inspect().toString() + " out of range.");
             }
             return null;
         }
 
         if (truncate && endLong > limit) {
             endLong = limit;
         }
 
         if (endLong < 0 || (!isExclusive && endLong == 0)) {
             endLong += limit;
             // don't check against negative endLong,
             // that's how MRI behaves.
         }
 
         return new long[]{beginLong, Math.max(endLong - beginLong, 0L)};
     }
 
     public long[] begLen(long len, int err){
         long beg = RubyNumeric.num2long(this.begin);
         long end = RubyNumeric.num2long(this.end);
 
         if(beg < 0){
             beg += len;
             if(beg < 0){
                 if(err != 0){
                     throw getRuntime().newRangeError(beg + ".." + (isExclusive ? "." : "") + end + " out of range");
                 }
                 return null;
             }
         }
 
         if(err == 0 || err == 2){
             if(beg > len){
                 if(err != 0){
                     throw getRuntime().newRangeError(beg + ".." + (isExclusive ? "." : "") + end + " out of range");
                 }
                 return null;
             }
             if(end > len){
                 end = len;
             }
         }
         if(end < 0){
             end += len;
         }
         if(!isExclusive){
             end++;
         }
         len = end - beg;
         if(len < 0){
             len = 0;
         }
 
         return new long[]{beg, len};
     }    
 
     public static RubyRange newRange(Ruby runtime, IRubyObject begin, IRubyObject end, boolean isExclusive) {
         RubyRange range = new RubyRange(runtime, runtime.getRange());
         range.init(begin, end, isExclusive ? runtime.getTrue() : runtime.getFalse());
         return range;
     }
 
     @JRubyMethod(name = "initialize", required = 2, optional = 1, frame = true)
     public IRubyObject initialize(IRubyObject[] args, Block unusedBlock) {
         if (args.length == 3) {
             init(args[0], args[1], args[2].isTrue() ? getRuntime().getTrue() : getRuntime().getFalse());
         } else if (args.length == 2) {
             init(args[0], args[1], getRuntime().getFalse());
         } else {
             throw getRuntime().newArgumentError("Wrong arguments. (anObject, anObject, aBoolean = false) expected");
         }
         return getRuntime().getNil();
     }
 
     @JRubyMethod(name = {"first", "begin"})
     public IRubyObject first() {
         return begin;
     }
 
     @JRubyMethod(name = {"last", "end"})
     public IRubyObject last() {
         return end;
     }
     
     @JRubyMethod(name = "hash")
     public RubyFixnum hash() {
         ThreadContext context = getRuntime().getCurrentContext();
         long baseHash = (isExclusive ? 1 : 0);
         long beginHash = ((RubyFixnum) begin.callMethod(context, MethodIndex.HASH, "hash")).getLongValue();
         long endHash = ((RubyFixnum) end.callMethod(context, MethodIndex.HASH, "hash")).getLongValue();
         
         long hash = baseHash;
         hash = hash ^ (beginHash << 1);
         hash = hash ^ (endHash << 9);
         hash = hash ^ (baseHash << 24);
         
         return getRuntime().newFixnum(hash);
     }
     
     private static byte[] DOTDOTDOT = "...".getBytes();
     private static byte[] DOTDOT = "..".getBytes();
 
     @JRubyMethod(name = "inspect", frame = true)
     public IRubyObject inspect(Block block) {
         ThreadContext context = getRuntime().getCurrentContext();        
         RubyString str = RubyString.objAsString(begin.callMethod(context, "inspect")).strDup();
         RubyString str2 = RubyString.objAsString(end.callMethod(context, "inspect"));
 
         str.cat(isExclusive ? DOTDOTDOT : DOTDOT);
         str.concat(str2);
         str.infectBy(str2);
         return str;
     }
     
     @JRubyMethod(name = "to_s", frame = true)
     public IRubyObject to_s(Block block) {
         ThreadContext context = getRuntime().getCurrentContext();        
         RubyString str = RubyString.objAsString(begin).strDup();
         RubyString str2 = RubyString.objAsString(end);
 
         str.cat(isExclusive ? DOTDOTDOT : DOTDOT);
         str.concat(str2);
         str.infectBy(str2);
         return str;
 
     }
 
     @JRubyMethod(name = "exclude_end?")
     public RubyBoolean exclude_end_p() {
         return getRuntime().newBoolean(isExclusive);
     }
 
     @JRubyMethod(name = "length", frame = true)
     public RubyFixnum length(Block block) {
         long size = 0;
         ThreadContext context = getRuntime().getCurrentContext();
 
         if (begin.callMethod(context, MethodIndex.OP_GT, ">", end).isTrue()) {
             return getRuntime().newFixnum(0);
         }
 
         if (begin instanceof RubyFixnum && end instanceof RubyFixnum) {
             size = ((RubyNumeric) end).getLongValue() - ((RubyNumeric) begin).getLongValue();
             if (!isExclusive) {
                 size++;
             }
         } else { // Support length for arbitrary classes
             IRubyObject currentObject = begin;
 	    int compareMethod = isExclusive ? MethodIndex.OP_LT : MethodIndex.OP_LE;
 
 	    while (currentObject.callMethod(context, compareMethod, (String)MethodIndex.NAMES.get(compareMethod), end).isTrue()) {
 		size++;
 		if (currentObject.equals(end)) {
 		    break;
 		}
 		currentObject = currentObject.callMethod(context, "succ");
 	    }
 	}
         return getRuntime().newFixnum(size);
     }
 
     @JRubyMethod(name = "==", required = 1, frame = true)
     public IRubyObject op_equal(IRubyObject other, Block block) {
         if (this == other) return getRuntime().getTrue();
         if (!(other instanceof RubyRange)) return getRuntime().getFalse();
         RubyRange otherRange = (RubyRange) other;
         boolean result =
             begin.op_equal(otherRange.begin).isTrue() &&
             end.op_equal(otherRange.end).isTrue() &&
             isExclusive == otherRange.isExclusive;
         return getRuntime().newBoolean(result);
     }
     
     @JRubyMethod(name = "eql?", required = 1)
     public IRubyObject eql_p(IRubyObject other) {
         if (this == other) return getRuntime().getTrue();
         if (!(other instanceof RubyRange)) return getRuntime().getFalse();
         RubyRange otherRange = (RubyRange)other;
         if (!begin.equals(otherRange.begin) || !end.equals(otherRange.end) || isExclusive != otherRange.isExclusive) return getRuntime().getFalse();
         return getRuntime().getTrue();
     }
 
     @JRubyMethod(name = "each", frame = true)
     public IRubyObject each(Block block) {
         ThreadContext context = getRuntime().getCurrentContext();
         
         if (!begin.respondsTo("succ")) throw getRuntime().newTypeError("can't iterate from " + begin.getMetaClass().getName());
 
         if (begin instanceof RubyFixnum && end instanceof RubyFixnum) {
             long endLong = ((RubyNumeric) end).getLongValue();
             long i = ((RubyNumeric) begin).getLongValue();
 
             if (!isExclusive) {
                 endLong += 1;
             }
 
             for (; i < endLong; i++) {
                 block.yield(context, getRuntime().newFixnum(i));
             }
         } else if (begin instanceof RubyString) {
             ((RubyString) begin).upto(end, isExclusive, block);
         } else if (getRuntime().getNumeric().isInstance(begin)) {
             if (!isExclusive) {
                 end = end.callMethod(context, MethodIndex.OP_PLUS, "+", RubyFixnum.one(getRuntime()));
             }
            while (begin.callMethod(context, MethodIndex.OP_LT, "<", end).isTrue()) {
                 block.yield(context, begin);
                 begin = begin.callMethod(context, MethodIndex.OP_PLUS, "+", RubyFixnum.one(getRuntime()));
             }
         } else {
             IRubyObject v = begin;
 
             if (isExclusive) {
                while (v.callMethod(context, MethodIndex.OP_LT, "<", end).isTrue()) {
                     if (v.equals(end)) {
                         break;
                     }
                     block.yield(context, v);
                     v = v.callMethod(context, "succ");
                 }
             } else {
                while (v.callMethod(context, MethodIndex.OP_LE, "<=", end).isTrue()) {
                     block.yield(context, v);
                     if (v.equals(end)) {
                         break;
                     }
                     v = v.callMethod(context, "succ");
                 }
             }
         }
 
         return this;
     }
     
     @JRubyMethod(name = "step", optional = 1, frame = true)
     public IRubyObject step(IRubyObject[] args, Block block) {
        Arity.checkArgumentCount(getRuntime(), args, 0, 1);
        
         IRubyObject currentObject = begin;
         int compareMethod = isExclusive ? MethodIndex.OP_LT : MethodIndex.OP_LE;
         // int stepSize = (int) (args.length == 0 ? 1 : args[0].convertToInteger().getLongValue());
         double stepSize = 1.0;
         if (args.length != 0) {
             stepSize = Double.parseDouble(args[0].toString());
         }
         if (stepSize == 0) {
             throw getRuntime().newArgumentError("step can't be 0");
         }
         else if (stepSize < 0) {
             throw getRuntime().newArgumentError("step can't be negative");
         }
 
         ThreadContext context = getRuntime().getCurrentContext();
         
         if (begin instanceof RubyFloat && end instanceof RubyFloat) {
             RubyFloat stepNum = getRuntime().newFloat(stepSize);
             while (currentObject.callMethod(context, compareMethod, MethodIndex.NAMES.get(compareMethod), end).isTrue()) {
                 block.yield(context, currentObject);
                 currentObject = currentObject.callMethod(context, MethodIndex.OP_PLUS, "+", stepNum);
             }
         } else if (begin instanceof RubyNumeric && end instanceof RubyNumeric) {
             stepSize = Math.floor(stepSize);
             if (stepSize == 0) {
                 throw getRuntime().newArgumentError("step can't be 0");
             }
             RubyFixnum stepNum = getRuntime().newFixnum(Double.valueOf(stepSize).longValue());
             while (currentObject.callMethod(context, compareMethod, MethodIndex.NAMES.get(compareMethod), end).isTrue()) {
                 block.yield(context, currentObject);
                 currentObject = currentObject.callMethod(context, MethodIndex.OP_PLUS, "+", stepNum);
             }
         } else if(begin instanceof RubyString && end instanceof RubyString) {
           RubyString afterEnd = isExclusive ? (RubyString) end : (RubyString) end.callMethod(context, "succ");
           boolean pastEnd = isExclusive && currentObject.callMethod(context, MethodIndex.EQUALEQUAL, "==", end).isTrue();  
           while(pastEnd == false) {
               block.yield(context, currentObject);
               for (int i = 0; i < stepSize; i++) {
                   currentObject = currentObject.callMethod(context, "succ");
                   if(currentObject.callMethod(context, MethodIndex.EQUALEQUAL, "==", afterEnd).isTrue()) {
                     pastEnd = true;
                     break;
                   } 
               }
           }
         } else {
             while (currentObject.callMethod(context, compareMethod, MethodIndex.NAMES.get(compareMethod), end).isTrue()) {
                 block.yield(context, currentObject);
                 
                 for (int i = 0; i < stepSize; i++) {
                     currentObject = currentObject.callMethod(context, "succ");
                 }
             }
         }
         
         return this;
     }
 
     private boolean r_lt(IRubyObject a, IRubyObject b) {
         IRubyObject r = a.callMethod(getRuntime().getCurrentContext(),MethodIndex.OP_SPACESHIP, "<=>",b);
         if(r.isNil()) {
             return false;
         }
         if(RubyComparable.cmpint(r,a,b) < 0) {
             return true;
         }
         return false;
     }
 
     private boolean r_le(IRubyObject a, IRubyObject b) {
         IRubyObject r = a.callMethod(getRuntime().getCurrentContext(),MethodIndex.OP_SPACESHIP, "<=>",b);
         if(r.isNil()) {
             return false;
         }
         if(RubyComparable.cmpint(r,a,b) <= 0) {
             return true;
         }
         return false;
     }
 
     @JRubyMethod(name = {"include?", "member?", "==="}, required = 1, frame = true)
     public RubyBoolean include_p(IRubyObject obj, Block block) {
         RubyBoolean val = getRuntime().getFalse();
         if(r_le(begin,obj)) {
             if(isExclusive) {
                 if(r_lt(obj,end)) {
                     val = getRuntime().getTrue();
                 }
             } else {
                 if(r_le(obj,end)) {
                     val = getRuntime().getTrue();
                 }
             }
         }
         return val;
     }
 }
