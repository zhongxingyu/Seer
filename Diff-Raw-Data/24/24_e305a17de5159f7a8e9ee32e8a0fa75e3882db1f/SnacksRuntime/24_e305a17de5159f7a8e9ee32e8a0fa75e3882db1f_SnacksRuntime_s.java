 package snacks.lang;
 
 import static me.qmx.jitescript.util.CodegenUtils.p;
 import static me.qmx.jitescript.util.CodegenUtils.sig;
 
 import java.lang.invoke.CallSite;
 import java.lang.invoke.MethodHandle;
 import java.lang.invoke.MethodHandles.Lookup;
 import java.lang.invoke.MethodType;
 import java.lang.invoke.MutableCallSite;
 import com.headius.invokebinder.Binder;
 import org.objectweb.asm.Handle;
 import org.objectweb.asm.Opcodes;
 
 public class SnacksRuntime {
 
     public static final Handle BOOTSTRAP = new Handle(
         Opcodes.H_INVOKESTATIC,
         p(SnacksRuntime.class),
         "bootstrap",
         sig(CallSite.class, Lookup.class, String.class, MethodType.class)
     );
 
     public static CallSite bootstrap(Lookup lookup, String name, MethodType type) throws ReflectiveOperationException {
         MutableCallSite callSite = new MutableCallSite(type);
         MethodHandle send = Binder.from(type)
             .insert(0, lookup)
             .invokeStatic(lookup, SnacksRuntime.class, name);
         callSite.setTarget(send);
         return callSite;
     }
 
     public static Object apply(Lookup lookup, Object function, Object argument) throws Throwable {
         return methodHandleFor(lookup, function, argument).invoke(function, argument);
     }
 
     private static MethodHandle methodHandleFor(Lookup lookup, Object function, Object argument) throws Throwable {
         Binder binder = Binder.from(Object.class, Object.class, Object.class);
         try {
             return binder.cast(Object.class, function.getClass(), argument.getClass()).invokeVirtual(lookup, "apply");
        } catch (ReflectiveOperationException exception) {
            return binder.cast(Object.class, function.getClass(), Object.class).invokeVirtual(lookup, "apply");
         }
     }
 }
