 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INSFO-RI-261552.
 
  Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 package eu.stratuslab.storage.disk.resources;
 
 import static org.restlet.data.MediaType.APPLICATION_JSON;
 import static org.restlet.data.MediaType.TEXT_HTML;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.restlet.data.Disposition;
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.representation.FileRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Delete;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.Put;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.storage.disk.utils.DiskUtils;
 import eu.stratuslab.storage.disk.utils.FileUtils;
 import eu.stratuslab.storage.disk.utils.MiscUtils;
 import eu.stratuslab.storage.persistence.Disk;
 import eu.stratuslab.storage.persistence.Disk.DiskType;
 
 public class DiskResource extends DiskBaseResource {
 
 	public static final String EDIT_QUERY_VALUE = "edit";
 	private static final String UUID_KEY_NAME = Disk.UUID_KEY;
 	private boolean isEdit = false;
 
 	@Override
 	protected void doInit() throws ResourceException {
 
 		Disk disk = loadExistingDisk();
 
 		if (!hasSufficientRightsToView(disk)) {
 			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN,
					"insuffient access rights to view disk (" + getDiskId()
 							+ ")");
 		}
 
 		if (getRequest().getAttributes().containsKey(EDIT_QUERY_VALUE)) {
 			isEdit = Boolean.parseBoolean(getRequest().getAttributes()
 					.get(EDIT_QUERY_VALUE).toString());
 		}
 	}
 
 	@Put("form")
 	public void updateForm(Representation entity) {
 
 		update(entity);
 
 		redirectSeeOther(getRequest().getResourceRef());
 	}
 
 	@Put("json")
 	public void updateJson(Representation entity) {
 
 		update(entity);
 	}
 
 	public void update(Representation entity) {
 
 		MiscUtils.checkForNullEntity(entity);
 
 		Disk disk = loadExistingDisk();
 
 		hasSufficientRightsToEdit(disk);
 
 		disk = processWebForm(disk, new Form(entity));
 
 		disk.setUuid(getDiskId());
 
 		updateDisk(disk);
 	}
 
 	@Post("form:html")
 	public void createCopyOnWriteOrRebase(Representation entity) {
 
 		Disk newDisk = createCopyOnWriteOrRebase();
 
 		redirectSeeOther(getBaseUrl() + "/disks/" + newDisk.getUuid());
 
 	}
 
 	@Post("form:json")
 	public Representation createCopyOnWriteOrRebaseAsJson(Representation entity) {
 
 		Disk newDisk = createCopyOnWriteOrRebase();
 
 		Map<String, Object> info = new HashMap<String, Object>();
 		addDiskToInfo(newDisk, info);
 
 		return createTemplateRepresentation("json/disk.ftl", info,
 				APPLICATION_JSON);
 	}
 
 	private void addDiskToInfo(Disk newDisk, Map<String, Object> info) {
 		info.put("disk", newDisk);
 	}
 
 	protected Disk createCopyOnWriteOrRebase() {
 		Disk disk = Disk.load(getDiskId());
 
 		if (disk == null) {
 			throw (new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Disk "
 					+ getDiskId() + " doesn't exists"));
 		}
 
 		Disk newDisk = null;
 		if (disk.getType() == DiskType.MACHINE_IMAGE_LIVE) {
 			newDisk = rebase(disk);
 		} else if (disk.getType() == DiskType.MACHINE_IMAGE_ORIGIN) {
 			newDisk = createMachineImageCoW(disk);
 		} else {
 			throw (new ResourceException(Status.CLIENT_ERROR_CONFLICT,
 					"Invalid disk state: " + disk.getType()
 							+ ". Cannot create copy or save as new image."));
 		}
 		return newDisk;
 	}
 
 	private Disk createMachineImageCoW(Disk disk) {
 		return DiskUtils.createMachineImageCoWDisk(disk);
 	}
 
 	private Disk rebase(Disk disk) {
 
 		String rebasedUuid = DiskUtils.rebaseDisk(disk);
 
 		Disk newDisk = null;
 
 		// some rebase implementation create new LUNs, others don't
 		if(rebasedUuid == null || "".equals(rebasedUuid)) {
 			newDisk = disk;
 		} else {
 			newDisk = initializeDisk();
 			newDisk.setUuid(rebasedUuid);
 		}
 		
 		newDisk.setType(DiskType.MACHINE_IMAGE_ORIGIN);
 		newDisk.setSize(disk.getSize());
 		newDisk.setSeed(true);
 		newDisk.setUsersCount(0);
 		newDisk.setQuarantine("");
 
 		newDisk.store();
 
 		return newDisk;
 	}
 
 	@Get("html")
 	public Representation getAsHtml() {
 
 		getLogger().info("DiskResource getAsHtml: " + getDiskId());
 
 		Map<String, Object> info = loadDiskProperties();
 
 		addDiskUserHeader();
 
 		if (isEdit) {
 			return createTemplateRepresentation("html/disk-edit.ftl", info,
 					TEXT_HTML);
 		} else {
 			return createTemplateRepresentation("html/disk.ftl", info,
 					TEXT_HTML);
 		}
 	}
 
 	@Get("json")
 	public Representation getAsJson() {
 
 		getLogger().info("DiskResource getAsJson: " + getDiskId());
 
 		Map<String, Object> info = loadDiskProperties();
 
 		addDiskUserHeader();
 
 		return createTemplateRepresentation("json/disk.ftl", info,
 				APPLICATION_JSON);
 	}
 
 	@Get("gzip")
 	public Representation toZip() {
 
 		String uuid = getDiskId();
 
 		cleanCache(uuid);
 
 		if (isImageBeingCompressed(uuid)) {
 			waitWhileImageCompressed(uuid);
 		} else if (needToCompressImage(uuid)) {
 			compressImage();
 		}
 
 		Representation image = new FileRepresentation(
 				DiskUtils.getCompressedDiskLocation(uuid),
 				MediaType.APPLICATION_GNU_ZIP);
 		image.getDisposition().setType(Disposition.TYPE_ATTACHMENT);
 
 		return image;
 	}
 
 	/**
 	 * The compression logic removes the original file after compression
 	 * therefore, if the raw file exists means that the compression is ongoing
 	 */
 	private Boolean isImageBeingCompressed(String uuid) {
 		return DiskUtils.isCompressedDiskBuilding(uuid);
 	}
 
 	private void waitWhileImageCompressed(String uuid) {
 		while (isImageBeingCompressed(uuid)) {
 			getLogger().info("Waiting for file to be compressed...");
 			MiscUtils.sleep(5000);
 		}
 	}
 
 	private boolean needToCompressImage(String uuid) {
 		return !FileUtils.isCompressedDiskExists(uuid)
 				|| DiskUtils.hasCompressedDiskExpire(uuid);
 	}
 
 	private void compressImage() {
 		getLogger().info("Creating compressed disk");
 		DiskUtils.createCompressedDisk(getDiskId());
 	}
 
 	@Delete("html")
 	public Representation deleteDiskAsHtml() {
 
 		getLogger().info("DiskResource deleteDiskAsHtml: " + getDiskId());
 
 		processDeleteDiskRequest();
 
 		redirectSeeOther(getBaseUrl() + "/disks/");
 
 		Map<String, Object> info = createInfoStructure("redirect");
 		return createTemplateRepresentation("html/redirect.ftl", info,
 				TEXT_HTML);
 	}
 
 	@Delete("json")
 	public Representation deleteDiskAsJson() {
 
 		getLogger().info("DiskResource deleteDiskAsJson: " + getDiskId());
 
 		processDeleteDiskRequest();
 
 		Map<String, Object> info = new HashMap<String, Object>();
 		info.put("key", UUID_KEY_NAME);
 		info.put("value", getDiskId());
 
 		return createTemplateRepresentation("json/keyvalue.ftl", info,
 				APPLICATION_JSON);
 	}
 
 	private Map<String, Object> loadDiskProperties() {
 		Map<String, Object> info = createInfoStructure("Disk Information");
 
 		addCreateFormDefaults(info);
 
 		Disk disk = loadExistingDisk();
 
 		addDiskToInfo(disk, info);
 		info.put("currenturl", getCurrentUrl());
 		info.put("can_edit", hasSufficientRightsToEdit(disk));
 
 		return info;
 
 	}
 
 	private void addDiskUserHeader() {
 		Form diskUserHeaders = (Form) getResponse().getAttributes().get(
 				"org.restlet.http.headers");
 
 		if (diskUserHeaders == null) {
 			diskUserHeaders = new Form();
 			getResponse().getAttributes().put("org.restlet.http.headers",
 					diskUserHeaders);
 		}
 
 	}
 
 	private void processDeleteDiskRequest() {
 
 		Disk disk = loadExistingDisk();
 
 		if (!hasSufficientRightsToEdit(disk)) {
 			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN,
 					"insufficient rights to delete disk (" + disk.getUuid()
 							+ ")");
 		}
 
 		if (disk.getMountsCount() > 0) {
 			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "disk ("
 					+ disk.getUuid() + ") is in use and can't be deleted");
 		}
 
 		deleteDisk(disk);
 	}
 
 	private void deleteDisk(Disk disk) {
 		String parentUuid = disk.getBaseDiskUuid();
 		disk.remove();
 
 		try {
 			DiskUtils.removeDisk(disk.getUuid());
 		} catch (ResourceException ex) {
 			disk.store(); // store it back since remove failed
 			throw (ex);
 		}
 
 		if (parentUuid != null) {
 			Disk parent = Disk.load(parentUuid);
 			parent.decrementUserCount();
 		}
 	}
 }
