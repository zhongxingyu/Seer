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
 
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
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
 
 public class DiskResource extends DiskBaseResource {
 
 	private static final String UUID_KEY_NAME = Disk.UUID_KEY;
 
 	@Override
 	protected void doInit() throws ResourceException {
 
 		Disk disk = loadExistingDisk();
 		
 		if (!hasSufficientRightsToView(disk)) {
 			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN,
 					"insuffient access rights to view disk (" + getDiskId()
 							+ ")");
 		}
 
 	}
 
 	@Put
 	public void update(Representation entity) {
 
 		checkIsSuper();
 
 		MiscUtils.checkForNullEntity(entity);
 
		Disk disk = loadExistingDisk();
		disk = processWebForm(disk, new Form(entity));

 		disk.setUuid(getDiskId());
 
 		updateDisk(disk);
 	}
 
 	@Post
 	public void createCopyOnWriteOrRebase(Representation entity) {
 
 		Disk disk = Disk.load(getDiskId());
 		
 		if(disk == null) {
 			throw(new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Disk " + getDiskId() + " doesn't exists"));
 		}
 
 		String newUuid = null;
 		if (disk.getIscow()) {
 			newUuid = rebase(disk);
 		} else {
 			newUuid = createCoW(disk);
 		}
 
 		redirectSeeOther(getBaseUrl() + "/disks/" + newUuid + "/");
 
 	}
 
 	private String createCoW(Disk disk) {
 
 		DiskUtils.createCoWDisk(disk);
 
 		incrementOriginDiskUserCount();
 
 		return disk.getUuid();
 	}
 
 	private void incrementOriginDiskUserCount() {
 		incrementUserCount(getDiskId());
 	}
 
 	private String rebase(Disk disk) {
 
 		String newUuid = DiskUtils.rebaseDisk(disk);
 
 		Disk newDisk = initializeDisk();
 		newDisk.setUuid(newUuid);
 		newDisk.setIsreadonly(true);
 		newDisk.setSize(disk.getSize());
 
 		// TODO: implement here and remove client-side implementation
 		// newProperties = calculateHashes(newProperties);
 		registerDisk(newDisk);
 
 		return newUuid;
 	}
 
 	protected Properties calculateHashes(Properties properties) {
 		String identifier;
 		try {
 			identifier = DiskUtils.calculateHash(properties
 					.getProperty(Disk.UUID_KEY));
 		} catch (FileNotFoundException e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
 					e.getMessage());
 		}
 		properties.put(Disk.DISK_IDENTIFER_KEY, identifier);
 
 		return properties;
 	}
 
 	@Get("html")
 	public Representation getAsHtml() {
 
 		getLogger().info("DiskResource getAsHtml: " + getDiskId());
 
 		Map<String, Object> info = loadDiskProperties();
 
 		addDiskUserHeader();
 
 		return createTemplateRepresentation("html/disk.ftl", info, TEXT_HTML);
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
 
 		Representation image = new FileRepresentation(DiskUtils.getCompressedDiskLocation(uuid),
 				MediaType.APPLICATION_GNU_ZIP);
 		image.getDisposition().setType(Disposition.TYPE_ATTACHMENT);
 
 		return image;
 	}
 
 	/**
 	 * The compression logic removes the original file after compression
 	 * therefore, if the raw file exists means that the compression
 	 * is ongoing
 	 */
 	protected Boolean isImageBeingCompressed(String uuid) {
 		return DiskUtils.isCompressedDiskBuilding(uuid);
 	}
 
 	protected void waitWhileImageCompressed(String uuid) {
 		while (isImageBeingCompressed(uuid)) {
 			getLogger().info("Waiting for file to be compressed...");
 			MiscUtils.sleep(5000);
 		}
 	}
 
 	protected boolean needToCompressImage(String uuid) {
 		return !FileUtils.isCompressedDiskExists(uuid) || DiskUtils.hasCompressedDiskExpire(uuid);
 	}
 
 	protected void compressImage() {
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
 		Map<String, Object> infos = createInfoStructure("Disk Information");
 
 		loadExistingDisk();
 
 		Disk disk = Disk.load(getDiskId());
 
 		infos.put("disk", disk);
 		infos.put("currenturl", getCurrentUrl());
 		infos.put("can_delete", hasSufficientRightsToDelete(disk));
 
 		return infos;
 
 	}
 
 	private void addDiskUserHeader() {
 		Form diskUserHeaders = (Form) getResponse().getAttributes().get(
 				"org.restlet.http.headers");
 
 		if (diskUserHeaders == null) {
 			diskUserHeaders = new Form();
 			getResponse().getAttributes().put("org.restlet.http.headers",
 					diskUserHeaders);
 		}
 
 //		diskUserHeaders.add("X-DiskUser-Limit",
 //				String.valueOf(RootApplication.CONFIGURATION.USERS_PER_DISK));
 //		diskUserHeaders.add("X-DiskUser-Remaining",
 //				String.valueOf(zk.remainingFreeUser(getDiskId())));
 	}
 
 	private void processDeleteDiskRequest() {
 
 		Disk disk = Disk.load(getDiskId());
 		if (disk == null) {
 			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "disk ("
 					+ getDiskId() + ") does not exist");
 		}
 
 		if (!hasSufficientRightsToDelete(disk)) {
 			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN,
 					"insufficient rights to delete disk (" + disk.getUuid() + ")");
 		}
 
 		if (disk.getMountsCount() > 0) {
 			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "disk ("
 					+ disk.getUuid() + ") is in use and can't be deleted");
 		}
 
 		deleteDisk();
 	}
 
 	private void deleteDisk() {
 		deleteDisk(getDiskId());
 	}
 
 	private void deleteDisk(String uuid) {
 		Disk disk = Disk.load(uuid);
 		String parentUuid = disk.getBaseDiskUuid();
 		if(parentUuid != null) {
 			Disk parent = Disk.load(parentUuid);
 			parent.decrementUserCount();
 		}
 		disk.remove();
 	}
 
 }
