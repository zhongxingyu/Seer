 package eu.stratuslab.storage.disk.resources;
 
 import static org.restlet.data.MediaType.APPLICATION_JSON;
 import static org.restlet.data.MediaType.TEXT_HTML;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.restlet.data.Form;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.storage.disk.utils.DiskProperties;
 import eu.stratuslab.storage.disk.utils.DiskUtils;
 import eu.stratuslab.storage.disk.utils.MiscUtils;
 
 public class MountsResource extends BaseResource {
 
     private String diskId = null;
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
 
     }
 
     @Get("html")
     public Representation getAsHtml() {
 
         getLogger().info("DiskResource getAsHtml: " + diskId);
 
         Map<String, Object> info = getMountProperties(diskId);
 
         return createTemplateRepresentation("html/mounts.ftl", info, TEXT_HTML);
     }
 
     @Get("json")
     public Representation getAsJson() {
 
         getLogger().info("DiskResource getAsJson: " + diskId);
 
         Map<String, Object> info = getMountProperties(diskId);
 
         return createTemplateRepresentation("json/mounts.ftl", info,
                 APPLICATION_JSON);
     }
 
     @Post("form:json")
     public Representation mountDiskAsJson(Representation entity) {
 
         extractNodeAndVmId(entity);
 
         if (node != null || vmId != null) {
 
             String target = zk.nextHotpluggedDiskTarget(node, vmId, diskId);
 
             getLogger().info(
                     "DiskResource mountDiskAsJson (dynamic): " + diskId + ", "
                             + node + ", " + vmId + ", " + target);
 
             return attachDisk(target);
 
         } else {
 
             getLogger().info(
                     "DiskResource mountDiskAsJson (static): " + diskId + ", "
                             + DiskProperties.STATIC_DISK_TARGET);
 
             return attachDisk(DiskProperties.STATIC_DISK_TARGET);
         }
 
     }
 
     private Map<String, Object> getMountProperties(String uuid) {
         Map<String, Object> info = this.createInfoStructure("mounts");
         info.put("mounts", zk.getDiskMounts(uuid));
         return info;
     }
 
     private Representation actionResponse(List<String> diskUuids,
             String diskTarget) {
         Map<String, Object> info = new HashMap<String, Object>();
 
         info.put("uuids", diskUuids);
         info.put("node", node);
         info.put("vm_id", vmId);
 
         if (diskTarget != null) {
             info.put("target", diskTarget);
         }
 
         return createTemplateRepresentation("json/action.ftl", info,
                 APPLICATION_JSON);
     }
 
     private Representation attachDisk(String target) {
 
         if (diskId == null) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "Disk UUID not provided");
         } else if (!zk.diskExists(diskId)) {
             throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                     "unknown disk UUID");
         } else if (target.equals(DiskProperties.DISK_TARGET_LIMIT)) {
             throw new ResourceException(Status.CLIENT_ERROR_CONFLICT,
                     "Target limit reached. Restart instance to attach new disk");
         }
 
         Properties diskProperties = zk.getDiskProperties(diskId);
         if (!hasSufficientRightsToView(diskProperties)) {
             throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN,
                     "Not enough rights to attach disk");
         }
 
         getLogger().info(
                 "attachDisk: " + node + " " + vmId + " " + diskId + " "
                         + target);
 
         zk.addDiskMount(node, vmId, diskId, target, getLogger());
 
         List<String> diskIds = new LinkedList<String>();
         diskIds.add(diskId);
 
         if (!target.equals(DiskProperties.STATIC_DISK_TARGET)) {
             getLogger().info(
                     "hotPlugDisk: " + node + " " + vmId + " " + diskId + " "
                             + target);
             DiskUtils.attachHotplugDisk(serviceName(), servicePort(), node,
                     vmId, diskId, target);
         }
 
         return actionResponse(diskIds, target);
     }
 
     private void extractNodeAndVmId(Representation entity) {
 
         MiscUtils.checkForNullEntity(entity);
 
         Form form = new Form(entity);
 
         if (form.size() != 2) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "form has incorrect number of values; expected 2, received "
                             + form.size());
         }
 
         node = form.getFirstValue("node");
         if (node == null) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "missing node attribute");
         }
 
         vmId = form.getFirstValue("vm_id");
         if (vmId == null) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "missing vm_id attribute");
         }
 
     }
 }
