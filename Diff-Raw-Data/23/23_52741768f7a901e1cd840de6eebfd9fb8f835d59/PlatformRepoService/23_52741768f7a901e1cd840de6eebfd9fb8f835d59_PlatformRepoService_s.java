 /**
  * Copyright Universite Joseph Fourier (www.ujf-grenoble.fr)
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.liglab.adele.cilia.workbench.restmonitoring.service.platform;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONObject;
 
 import fr.liglab.adele.cilia.workbench.common.cilia.CiliaException;
 import fr.liglab.adele.cilia.workbench.common.identifiable.PlatformID;
 import fr.liglab.adele.cilia.workbench.common.marker.CiliaFlag;
 import fr.liglab.adele.cilia.workbench.common.marker.ErrorsAndWarningsFinder;
 import fr.liglab.adele.cilia.workbench.common.parser.PlainFile;
 import fr.liglab.adele.cilia.workbench.common.service.AbstractRepoService;
 import fr.liglab.adele.cilia.workbench.common.service.Changeset;
 import fr.liglab.adele.cilia.workbench.common.service.IRepoServiceListener;
 import fr.liglab.adele.cilia.workbench.common.service.MergeUtil;
 import fr.liglab.adele.cilia.workbench.common.service.chain.ChainRepoService;
 import fr.liglab.adele.cilia.workbench.designer.service.chain.abstractcompositionsservice.AbstractCompositionsRepoService;
 import fr.liglab.adele.cilia.workbench.restmonitoring.misc.preferencepage.RestMonitoringPreferencePage;
 import fr.liglab.adele.cilia.workbench.restmonitoring.parser.platform.PlatformChain;
 import fr.liglab.adele.cilia.workbench.restmonitoring.parser.platform.PlatformFile;
 import fr.liglab.adele.cilia.workbench.restmonitoring.parser.platform.PlatformModel;
 import fr.liglab.adele.cilia.workbench.restmonitoring.utils.http.CiliaRestHelper;
 
 /**
  * 
  * @author Etienne Gandrille
  */
 public class PlatformRepoService extends ChainRepoService<PlatformFile, PlatformModel, PlatformChain> implements ErrorsAndWarningsFinder, IRepoServiceListener {
 
 	/** Singleton instance */
 	private static PlatformRepoService INSTANCE;
 
 	/** The key used to search the repository path into the preferences store. */
 	private static String PREFERENCE_PATH_KEY = RestMonitoringPreferencePage.REST_PLATFORM_REPOSITORY_PATH;
 
 	/** Spec files extension. */
 	public final static String ext = ".pf";
 
 	/** Repository Name */
 	private final static String repositoryName = "Cilia Platforms";
 
 	private static final Object keyUpdateInProgress = new Object();
 	private static List<String> chainIdInUpdate = new ArrayList<String>();
 
 	public static PlatformRepoService getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new PlatformRepoService();
 			INSTANCE.updateModel();
 		}
 		return INSTANCE;
 	}
 
 	private PlatformRepoService() {
 		super(PREFERENCE_PATH_KEY, ext, repositoryName, PlatformModel.ROOT_NODE_NAME);
 		AbstractCompositionsRepoService.getInstance().registerListener(this);
 	}
 
 	public void updateModel() {
 		File[] list = getFiles();
 		List<PlatformFile> elements = new ArrayList<PlatformFile>();
 		for (File file : list) {
 			elements.add(new PlatformFile(new PlainFile(file)));
 		}
 
 		// Updates existing model with computed model
 		List<Changeset> changes = null;
 		try {
 			changes = merge(elements);
 		} catch (CiliaException e) {
 			e.printStackTrace();
 		}
 
 		// Update content provider
 		contentProvider = new PlatformContentProvider(repoContent);
 
 		// Update markers relative to this repository
 		changes.addAll(updateMarkers());
 
 		// Sends notifications
 		notifyListeners(changes);
 	}
 
 	public PlatformModel getPlatformModel(PlatformID id) {
 		for (PlatformFile file : repoContent) {
 			PlatformModel pfModel = file.getModel();
 			if (pfModel != null && pfModel.getPlatformID() != null)
 				if (pfModel.getPlatformID().equals(id))
 					return pfModel;
 		}
 		return null;
 	}
 
 	public PlatformChain getPlatformChain(PlatformID platformId, String chainId) {
 		PlatformModel pfModel = getPlatformModel(platformId);
 		if (pfModel == null)
 			return null;
 		return pfModel.getChain(chainId);
 	}
 
 	/**
 	 * Merge a list of repo element into the current model. Only differences
 	 * between the argument and the model are merge back into the model.
 	 * 
 	 * @param repoElements
 	 *            a new model
 	 * @return a list of changesets, which can be empty.
 	 */
 	protected List<Changeset> merge(List<PlatformFile> repoElements) throws CiliaException {
 
 		ArrayList<Changeset> retval = new ArrayList<Changeset>();
 
 		retval.addAll(MergeUtil.mergeLists(repoElements, this.repoContent));
 
 		// path update
 		for (Changeset c : retval)
 			c.pushPathElement(this);
 
 		return retval;
 	}
 
 	@Override
 	protected String getContentForNewFile(String... parameters) {
 		return "<" + PlatformModel.ROOT_NODE_NAME + " host=\"" + parameters[0] + "\" port=\"" + parameters[1] + "\">\n</" + PlatformModel.ROOT_NODE_NAME + ">";
 	}
 
 	@Override
 	public CiliaFlag[] getErrorsAndWarnings() {
 		List<CiliaFlag> errorList = new ArrayList<CiliaFlag>();
 
 		// Nothing to check
 
 		return CiliaFlag.generateTab(errorList);
 	}
 
 	public void updateChains(String platformFileId, String[] chains) {
 
 		// merge and compute changes
 		PlatformFile pfFile = getFileFromId(platformFileId);
 		List<Changeset> changes = pfFile.getModel().mergeChains(chains);
 
 		// path update
 		for (Changeset c : changes) {
 			c.pushPathElement(pfFile);
 			c.pushPathElement(this);
 		}
 
 		// Update content provider
 		contentProvider = new PlatformContentProvider(repoContent);
 
 		// Update markers relative to this repository
 		changes.addAll(updateMarkers());
 
 		// Sends notifications
 		notifyListeners(changes);
 	}
 
 	public void updateChain(final PlatformModel platform, final String chainName) throws CiliaException {
 		if (platform == null)
 			throw new CiliaException("Can't update model: Platform is null");
 
 		final PlatformID platformID = platform.getPlatformID();
 		if (platformID.isValid() != null)
 			throw new CiliaException("Can't update model: Platform is not valid (" + platform.getPlatformID().isValid() + ")");
 
 		final String id = platform.getPlatformID().toString() + " - " + chainName;
 
 		synchronized (keyUpdateInProgress) {
 			if (chainIdInUpdate.contains(id)) {
 				// update already in progress
 				return;
 			} else {
 				// synchronisation needed
 				chainIdInUpdate.add(id);
 			}
 		}
 
 		try {
 			JSONObject json = null;
 			try {
 				json = CiliaRestHelper.getChainContent(platformID, chainName);
 			} catch (CiliaException e) {
 				throw new CiliaException("Error while asking chain content for " + chainName + " to " + platformID.toString(), e);
 			}
 
 			PlatformChain newChain = new PlatformChain(json, platform);
 			List<Changeset> changes = platform.getChain(chainName).merge(newChain);
 
 			for (Changeset c : changes) {
 				c.pushPathElement(platform.getPlatformFile());
 				c.pushPathElement(this);
 			}
 
 			// Update content provider
 			contentProvider = new PlatformContentProvider(repoContent);
 
 			// Update markers relative to this repository
 			changes.addAll(updateMarkers());
 
 			// Sends notifications
 			notifyListeners(changes);
 		} catch (CiliaException e) {
 			synchronized (keyUpdateInProgress) {
 				chainIdInUpdate.remove(id);
 			}
 			throw new CiliaException(e);
 		}
 
 		synchronized (keyUpdateInProgress) {
 			chainIdInUpdate.remove(id);
 		}
 	}
 
 	@Override
 	public void repositoryContentUpdated(AbstractRepoService<?, ?> abstractRepoService, List<Changeset> changes) {
 		updateModel();
 	}
 }
