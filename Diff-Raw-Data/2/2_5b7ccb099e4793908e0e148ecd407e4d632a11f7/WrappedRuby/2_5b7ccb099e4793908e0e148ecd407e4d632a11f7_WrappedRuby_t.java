 /**
 *    Copyright (C) 2008 10gen Inc.
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package ed.lang.ruby;
 
 import org.jruby.RubyObject;
 import org.jruby.RubyModule;
 import org.jruby.internal.runtime.methods.DynamicMethod;
 import org.jruby.runtime.Block;
 import org.jruby.runtime.ThreadContext;
 import org.jruby.runtime.builtin.IRubyObject;
 
 /**
  * WrappedRuby is a helper for JSObjects that wrap Ruby objects, like {@link
 * JSObjectWrapper} and {@link JSArrayWrapper}. It performs tasks common to
  * those wrappers, avoiding code duplication among them.
  */
 class WrappedRuby {
 
     private RubyObject robj;
     private RubyModule xgenModule; // cached copy
 
     public WrappedRuby(RubyObject robj) {
         this.robj = robj;
         xgenModule = robj.getRuntime().getOrCreateModule(RuntimeEnvironment.XGEN_MODULE_NAME);
     }
 
     IRubyObject ivarName(Object key) {
         String str = key.toString();
         if (!str.startsWith("@"))
             str = "@" + str;
         return robj.getRuntime().newString(str);
     }
 
     /**
      * Returns <code>true</code> if the metaclass of <code>robj</code> has a
      * method named <var>name</var> and it is not implemented only in the XGen
      * module. In other words, we want to return <code>true</code> if the
      * method exists but it is not a top-level JavaScript method that was
      * imported early on (either it is not a JS method or it was overridden).
      */
     boolean respondsToAndIsNotXGen(String name) {
         if (!robj.respondsTo(name))
             return false;
         DynamicMethod dm = robj.getMetaClass().searchMethod(name);
         return !dm.getImplementationClass().equals(xgenModule);
     }
 
     void removeIvarIfExists(String skey) {
         IRubyObject name = ivarName(skey);
         if (robj.instance_variable_defined_p(robj.getRuntime().getCurrentContext(), name).isTrue())
             robj.remove_instance_variable(robj.getRuntime().getCurrentContext(), name, Block.NULL_BLOCK);
         removeMethodIfExists(skey);
         removeMethodIfExists(skey + "=");
     }
 
     void removeMethodIfExists(String skey) {
         if (respondsToAndIsNotXGen(skey)) {
             ThreadContext context = robj.getRuntime().getCurrentContext();
             IRubyObject[] names = new IRubyObject[] {robj.getRuntime().newString(skey)};
             try {
                 robj.getSingletonClass().remove_method(context, names);
             }
             catch (Exception e) {
                 try {
                     robj.type().remove_method(context, names);
                 }
                 catch (Exception e2) {}
             }
         }
     }
 
 }
