 package ir.xweb.module;
 
import javax.script.ScriptException;
 import java.util.List;
 import java.util.Map;
 
 public class RoleManager {
 
     private final List<ModuleInfoRole> roles;
 
     protected RoleManager(final List<ModuleInfoRole> roles) {
         this.roles = roles;
     }
 
     public boolean hasPermission(final Map<String, String> params, final String role) {
         final String _role = role == null ? "" : role;
 
         for(ModuleInfoRole r:roles) {
             if(!hasPermission(params, _role, r)) {
                 return false;
             }
         }
 
         return true; // no role
     }
 
     private boolean hasPermission(final Map<String, String> params, final String _role, final ModuleInfoRole r) {
         for(Map.Entry<String, String> e:params.entrySet()) {
             if(e.getKey().matches(r.param())) {
                 if(r.match() == null || r.match().matches(e.getValue())) {
                     boolean result = true;
 
                     if(r.accept() != null && !_role.matches(r.accept())) {
                         result = false;
                     }
                     if(r.reject() != null && _role.matches(r.reject())) {
                         result = false;
                     }
 
                     if(r.and() != null) {
                         boolean and = result;
                         for(ModuleInfoRole a:r.and()) {
                             and = and | hasPermission(params, _role, a);
                             if(!and) {
                                 return false;
                             }
                         }
                     }
                     else if(r.or() != null) {
                         boolean or = result;
                         for(ModuleInfoRole o:r.or()) {
                             or = or | hasPermission(params, _role, o);
                         }
                         if(!or) {
                             return false;
                         }
                     }
 
                     else if(!result) {
                         return false;
                     }
                 }
             }
         }
 
         return true;
     }
 }
