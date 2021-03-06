 package hudson.security;
 
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 import hudson.model.Descriptor;
 import hudson.model.Hudson;
 import hudson.util.FormFieldValidator;
 import hudson.Functions;
 import net.sf.json.JSONObject;
 import org.acegisecurity.userdetails.UsernameNotFoundException;
 import org.acegisecurity.acls.sid.Sid;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.QueryParameter;
 import org.springframework.dao.DataAccessException;
 
 import javax.servlet.ServletException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.io.IOException;
 
 /**
  * Role-based authorization via a matrix.
  *
  * @author Kohsuke Kawaguchi
  */
 // TODO: think about the concurrency commitment of this class
 public class GlobalMatrixAuthorizationStrategy extends AuthorizationStrategy {
     private transient SidACL acl = new AclImpl();
 
     /**
      * List up all permissions that are granted.
      *
      * Strings are either the granted authority or the principal,
      * which is not distinguished.
      */
     private final Map<Permission,Set<String>> grantedPermissions = new HashMap<Permission, Set<String>>();
 
     private final Set<String> sids = new HashSet<String>();
 
     /**
      * Adds to {@link #grantedPermissions}.
      * Use of this method should be limited during construction,
      * as this object itself is considered immutable once populated.
      */
     public void add(Permission p, String sid) {
         Set<String> set = grantedPermissions.get(p);
         if(set==null)
             grantedPermissions.put(p,set = new HashSet<String>());
         set.add(sid);
         sids.add(sid);
     }
 
     /**
      * Works like {@link #add(Permission, String)} but takes both parameters
      * from a single string of the form <tt>PERMISSIONID:sid</tt>
      */
     private void add(String shortForm) {
         int idx = shortForm.indexOf(':');
         add(Permission.fromId(shortForm.substring(0,idx)),shortForm.substring(idx+1));
     }
 
     @Override
     public SidACL getRootACL() {
         return acl;
     }
 
     public Set<String> getGroups() {
         return sids;
     }
 
     private Object readResolve() {
         acl = new AclImpl();
         return this;
     }
 
     /**
      * Checks if the given SID has the given permission.
      */
     public boolean hasPermission(String sid, Permission p) {
         for(; p!=null; p=p.impliedBy) {
             Set<String> set = grantedPermissions.get(p);
             if(set!=null && set.contains(sid))
                 return true;
         }
         return false;
     }
 
     /**
      * Checks if the permission is explicitly given, instead of implied through {@link Permission#impliedBy}.
      */
     public boolean hasExplicitPermission(String sid, Permission p) {
         Set<String> set = grantedPermissions.get(p);
         return set != null && set.contains(sid);
     }
 
     /**
      * Returns all SIDs configured in this matrix, minus "anonymous"
      *
      * @return
      *      Always non-null.
      */
     public List<String> getAllSIDs() {
         Set<String> r = new HashSet<String>();
         for (Set<String> set : grantedPermissions.values())
             r.addAll(set);
         r.remove("anonymous");
 
         String[] data = r.toArray(new String[r.size()]);
         Arrays.sort(data);
         return Arrays.asList(data);
     }
 
     private final class AclImpl extends SidACL {
         protected Boolean hasPermission(Sid p, Permission permission) {
             if(GlobalMatrixAuthorizationStrategy.this.hasPermission(toString(p),permission))
                 return true;
             return null;
         }
     }
 
     public Descriptor<AuthorizationStrategy> getDescriptor() {
         return DESCRIPTOR;
     }
 
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     /**
      * Persist {@link GlobalMatrixAuthorizationStrategy} as a list of IDs that
      * represent {@link GlobalMatrixAuthorizationStrategy#grantedPermissions}.
      */
     public static class ConverterImpl implements Converter {
         public boolean canConvert(Class type) {
             return type==GlobalMatrixAuthorizationStrategy.class;
         }
 
         public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
             GlobalMatrixAuthorizationStrategy strategy = (GlobalMatrixAuthorizationStrategy)source;
 
             for (Entry<Permission, Set<String>> e : strategy.grantedPermissions.entrySet()) {
                 String p = e.getKey().getId();
                 for (String sid : e.getValue()) {
                     writer.startNode("permission");
                     context.convertAnother(p+':'+sid);
                     writer.endNode();
                 }
             }
 
         }
 
         public Object unmarshal(HierarchicalStreamReader reader, final UnmarshallingContext context) {
             GlobalMatrixAuthorizationStrategy as = create();
 
             while (reader.hasMoreChildren()) {
                 reader.moveDown();
                 String id = (String)context.convertAnother(as,String.class);
                 as.add(id);
                 reader.moveUp();
             }
 
             return as;
         }
 
         protected GlobalMatrixAuthorizationStrategy create() {
             return new GlobalMatrixAuthorizationStrategy();
         }
     }
     
     static {
         LIST.add(DESCRIPTOR);
     }
 
     public static class DescriptorImpl extends Descriptor<AuthorizationStrategy> {
         protected DescriptorImpl(Class<? extends GlobalMatrixAuthorizationStrategy> clazz) {
             super(clazz);
         }
 
         public DescriptorImpl() {
         }
 
         public String getDisplayName() {
             return Messages.GlobalMatrixAuthorizationStrategy_DisplayName();
         }
 
         public AuthorizationStrategy newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             GlobalMatrixAuthorizationStrategy gmas = create();
             for(Map.Entry<String,JSONObject> r : (Set<Map.Entry<String,JSONObject>>)formData.getJSONObject("data").entrySet()) {
                 String sid = r.getKey();
                 for(Map.Entry<String,Boolean> e : (Set<Map.Entry<String,Boolean>>)r.getValue().entrySet()) {
                     if(e.getValue()) {
                         Permission p = Permission.fromId(e.getKey());
                         gmas.add(p,sid);
                     }
                 }
             }
             return gmas;
         }
 
         protected GlobalMatrixAuthorizationStrategy create() {
             return new GlobalMatrixAuthorizationStrategy();
         }
 
         public String getHelpFile() {
             return "/help/security/global-matrix.html";
         }
 
         public List<PermissionGroup> getAllGroups() {
             List<PermissionGroup> groups = new ArrayList<PermissionGroup>(PermissionGroup.getAll());
             groups.remove(PermissionGroup.get(Permission.class));
             return groups;
         }
 
         public boolean showPermission(Permission p) {
             return true;
         }
 
         public void doCheckName(@QueryParameter String value ) throws IOException, ServletException {
             final String v = value.substring(1,value.length()-1);
             new FormFieldValidator(Hudson.ADMINISTER) {
                 protected void check() throws IOException, ServletException {
                     SecurityRealm sr = Hudson.getInstance().getSecurityRealm();
                     String ev = Functions.escape(v);
 
                     if(v.equals("authenticated")) {
                         // systerm reserved group
                         respond("<span>"+ makeImg("user.gif") +ev+"</span>");
                         return;
                     }
 
                     try {
                         sr.loadUserByUsername(v);
                         respond("<span>"+ makeImg("person.gif") +ev+"</span>");
                         return;
                     } catch (UserMayOrMayNotExistException e) {
                         // undecidable, meaning the user may exist
                         respond("<span>"+ev+"</span>");
                         return;
                     } catch (UsernameNotFoundException e) {
                         // fall through next
                     } catch (DataAccessException e) {
                         // fall through next
                     }
 
                     try {
                         sr.loadGroupByGroupname(v);
                         respond("<span>"+ makeImg("user.gif") +ev+"</span>");
                         return;
                     } catch (UserMayOrMayNotExistException e) {
                         // undecidable, meaning the group may exist
                         respond("<span>"+ev+"</span>");
                         return;
                     } catch (UsernameNotFoundException e) {
                         // fall through next
                     } catch (DataAccessException e) {
                         // fall through next
                     }
 
                     // couldn't find it. it doesn't exit
                     respond("<span>"+ makeImg("error.gif") +ev+"</span>");
                 }
             }.process();
         }
 
         private String makeImg(String gif) {
            return String.format("<img src='%s%s/images/16x16/%s' style='margin-right:0.2em'>", Hudson.getInstance().getRootUrlFromRequest(), Hudson.RESOURCE_PATH, gif);
         }
     }
 }
 
