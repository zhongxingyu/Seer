 package eu.stratuslab.storage.disk.resources;
 
 import static org.restlet.data.MediaType.APPLICATION_JSON;
 import static org.restlet.data.MediaType.TEXT_HTML;
 
 import java.util.Map;
 import java.util.Properties;
 
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Delete;
 import org.restlet.resource.Get;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.storage.disk.utils.DiskProperties;
 import eu.stratuslab.storage.disk.utils.DiskUtils;
 
 public class MountResource extends BaseResource {
 
     private String diskId = null;
     private String mountId = null;
     private String node = null;
     private String vmId = null;
 
     @Override
     public void doInit() {
 
         Map<String, Object> attributes = getRequest().getAttributes();
 
         Object diskIdValue = attributes.get("uuid");
         if (diskIdValue == null) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "disk UUID cannot be null");
         }
         diskId = diskIdValue.toString();
 
         Object mountIdValue = attributes.get("mountid");
         if (mountIdValue == null) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "mount ID cannot be null");
 
         }
         mountId = mountIdValue.toString();
 
         String[] fields = mountId.split("-", 2);
         if (fields.length != 2) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "malformed mount ID");
         }
 
         vmId = fields[0];
         if ("".equals(vmId)) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "illegal VM identifier");
         }
 
         node = fields[1];
         if ("".equals(node)) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "illegal node name");
         }
 
     }
 
     @Get("html")
     public Representation getAsHtml() {
 
         getLogger().info(
                 "DiskResource getAsHtml: " + diskId + ", " + mountId + ", "
                         + node + ", " + vmId);
 
         Map<String, Object> info = getMountProperties();
         return createTemplateRepresentation("html/mount.ftl", info, TEXT_HTML);
     }
 
     @Get("json")
     public Representation getAsJson() {
 
         getLogger().info(
                 "DiskResource getAsJson: " + diskId + ", " + mountId + ", "
                         + node + ", " + vmId);
 
         Map<String, Object> info = getMountProperties();
         return createTemplateRepresentation("json/mount.ftl", info,
                 APPLICATION_JSON);
     }
 
     @Delete("html")
     public Representation detachDiskAsHtml(Representation entity) {
 
         getLogger().info(
                 "DiskResource detachDiskAsHtml: " + diskId + ", " + mountId
                         + ", " + node + ", " + vmId);
 
         detachHotPluggedDisk();
 
         MESSAGES.push("Your disk has been unmounted.");
         redirectSeeOther(getBaseUrl() + "/disks/" + diskId + "/mounts/");
 
         Map<String, Object> info = createInfoStructure("redirect");
         return createTemplateRepresentation("html/redirect.ftl", info,
                 TEXT_HTML);
     }
 
     @Delete("json")
     public Representation detachDiskAsJson(Representation entity) {
 
         getLogger().info(
                 "DiskResource detachDiskAsJson: " + diskId + ", " + mountId
                         + ", " + node + ", " + vmId);
 
         detachHotPluggedDisk();
 
         Map<String, Object> info = getMountProperties();
         return createTemplateRepresentation("json/mount.ftl", info,
                 APPLICATION_JSON);
     }
 
     private Map<String, Object> getMountProperties() {
 
         Map<String, Object> info = this.createInfoStructure("mount");
         info.put("diskId", diskId);
         info.put("mountId", mountId);
         info.put("vmId", vmId);
         info.put("node", node);
         return info;
     }
 
     private void detachHotPluggedDisk() {
 
         Properties diskProperties = getDiskProperties(diskId);
 
         if (!hasSufficientRightsToView(diskProperties)) {
             throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN,
                     "insufficient rights to detach disk");
         }
 
         if (diskId == null || !diskExists(diskId)) {
             throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                     "unknown disk: " + diskId);
         }
 
         String diskTarget = zk.diskTarget(node, vmId, diskId);
         zk.removeDiskUser(node, vmId, diskId);
 
         // Only force the dismount if this was mounted through the
         // storage service.
         if (!diskTarget.equals(DiskProperties.STATIC_DISK_TARGET)) {
             DiskUtils.detachHotplugDisk(serviceName(), servicePort(), node,
                     vmId, diskId, diskTarget);
         }
 
     }
 
 }
