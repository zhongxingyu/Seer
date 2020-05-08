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
 package fr.liglab.adele.cilia.workbench.designer.service.abstractcompositionsservice;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.common.base.Preconditions;
 
 import fr.liglab.adele.cilia.workbench.common.cilia.CiliaException;
 import fr.liglab.adele.cilia.workbench.common.identifiable.NameNamespaceID;
 import fr.liglab.adele.cilia.workbench.common.marker.CiliaFlag;
 import fr.liglab.adele.cilia.workbench.common.marker.ErrorsAndWarningsFinder;
 import fr.liglab.adele.cilia.workbench.common.marker.IdentifiableUtils;
 import fr.liglab.adele.cilia.workbench.designer.parser.abstractcompositions.AbstractCompositionFile;
 import fr.liglab.adele.cilia.workbench.designer.parser.abstractcompositions.AbstractCompositionModel;
 import fr.liglab.adele.cilia.workbench.designer.parser.abstractcompositions.Binding;
 import fr.liglab.adele.cilia.workbench.designer.parser.abstractcompositions.Chain;
 import fr.liglab.adele.cilia.workbench.designer.parser.abstractcompositions.ComponentRef;
 import fr.liglab.adele.cilia.workbench.designer.parser.common.element.Cardinality;
 import fr.liglab.adele.cilia.workbench.designer.parser.common.element.IGenericAdapter;
 import fr.liglab.adele.cilia.workbench.designer.parser.common.element.IGenericMediator;
 import fr.liglab.adele.cilia.workbench.designer.preferencePage.CiliaDesignerPreferencePage;
 import fr.liglab.adele.cilia.workbench.designer.service.abstractreposervice.AbstractRepoService;
 import fr.liglab.adele.cilia.workbench.designer.service.abstractreposervice.Changeset;
 import fr.liglab.adele.cilia.workbench.designer.service.abstractreposervice.MergeUtil;
 
 /**
  * A central place for managing the DScilia repository. The repository can be
  * asked to refresh the model. The repository can be asked to send model update
  * notifications.
  * 
  * @author Etienne Gandrille
  */
 public class AbstractCompositionsRepoService extends
 		AbstractRepoService<AbstractCompositionFile, AbstractCompositionModel> implements ErrorsAndWarningsFinder {
 
 	/** Singleton instance */
 	private static AbstractCompositionsRepoService INSTANCE;
 
 	/** The key used to search the repository path into the preferences store. */
 	private static String PREFERENCE_PATH_KEY = CiliaDesignerPreferencePage.ABSTRACT_COMPO_REPOSITORY_PATH;
 
	/** DScilia files extension. */
	private final static String ext = ".compo";
 
 	/** Repository Name */
	private final static String repositoryName = "DSCilia repo service";
 
 	/**
 	 * Gets the singleton instance.
 	 * 
 	 * @return the instance.
 	 */
 	public static AbstractCompositionsRepoService getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new AbstractCompositionsRepoService();
 			INSTANCE.updateModel();
 		}
 		return INSTANCE;
 	}
 
 	/**
 	 * Constructor. Registers for repository path update and constructs the
 	 * model.
 	 */
 	private AbstractCompositionsRepoService() {
 		super(PREFERENCE_PATH_KEY, ext, repositoryName);
 	}
 
 	/**
 	 * Updates the model and sends notifications.
 	 */
 	public void updateModel() {
 
 		File[] list = getFiles();
 		List<AbstractCompositionFile> elements = new ArrayList<AbstractCompositionFile>();
 		for (File jar : list) {
 			String path = jar.getPath();
 			elements.add(new AbstractCompositionFile(path));
 		}
 
 		// Updates existing model with computed model
 		List<Changeset> changes = null;
 		try {
 			changes = merge(elements);
 		} catch (CiliaException e) {
 			e.printStackTrace();
 		}
 
 		// Update content provider
 		contentProvider = new AbstractCompositionsContentProvider(model);
 
 		// Update markers relative to this repository
 		updateMarkers();
 
 		// Sends notifications
 		notifyListeners(changes);
 	}
 
 	/**
 	 * Merge a list of repo element into the current model. Only differences
 	 * between the argument and the model are merge back into the model.
 	 * 
 	 * @param repoElements
 	 *            a new model
 	 * @return a list of changesets, which can be empty.
 	 * @throws CiliaException
 	 */
 	private List<Changeset> merge(List<AbstractCompositionFile> repoElements) throws CiliaException {
 
 		ArrayList<Changeset> retval = new ArrayList<Changeset>();
 
 		retval.addAll(MergeUtil.mergeLists(repoElements, this.model));
 
 		// path update
 		for (Changeset c : retval)
 			c.pushPathElement(this);
 
 		return retval;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.liglab.adele.cilia.workbench.designer.service.abstractreposervice.
 	 * AbstractRepoService#getContentForNewFile()
 	 */
 	@Override
 	protected String getContentForNewFile() {
 		return "<" + AbstractCompositionModel.ROOT_NODE_NAME + ">\n</" + AbstractCompositionModel.ROOT_NODE_NAME + ">";
 	}
 
 	public String isNewChainNameAllowed(NameNamespaceID id) {
 
 		if (isNameUsesAllowedChar(id.getName()) != null)
 			return isNameUsesAllowedChar(id.getName());
 
 		if (isNameUsesAllowedChar(id.getNamespace()) != null)
 			return isNameUsesAllowedChar(id.getNamespace());
 
 		if (id.getName().length() == 0) {
 			return "Empty name is not allowed";
 		}
 
 		Chain chain = findChain(id);
 		if (chain != null) {
 			return "A chain with this name/namespace already exists in the repository.";
 		}
 
 		return null;
 	}
 
 	public Chain findChain(NameNamespaceID chainName) {
 		for (Chain chain : getChains())
 			if (chain.getId().equals(chainName))
 				return chain;
 		return null;
 	}
 
 	private List<Chain> getChains() {
 		List<Chain> retval = new ArrayList<Chain>();
 		for (AbstractCompositionModel model : findAbstractElements())
 			retval.addAll(model.getChains());
 		return retval;
 	}
 
 	/**
 	 * Creates the chain in a repository element.
 	 * 
 	 * @param repo
 	 *            the repo
 	 * @param chainName
 	 *            the chain name
 	 */
 	public void createChain(AbstractCompositionFile repo, NameNamespaceID id) {
 		if (repo.getModel() == null)
 			return;
 		if (isNewChainNameAllowed(id) != null)
 			return;
 
 		try {
 			repo.getModel().createChain(id);
 		} catch (CiliaException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void deleteChain(Chain chain) {
 		AbstractCompositionFile repo = (AbstractCompositionFile) contentProvider.getParent(chain);
 		if (repo == null)
 			return;
 		try {
 			repo.getModel().deleteChain(chain.getId());
 		} catch (CiliaException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public AbstractCompositionFile getRepoElement(Object object) {
 		Preconditions.checkNotNull(object);
 
 		if (object instanceof AbstractCompositionFile)
 			return (AbstractCompositionFile) object;
 		Object parent = getContentProvider().getParent(object);
 		if (parent != null)
 			return getRepoElement(parent);
 		else
 			return null;
 	}
 
 	public void createMediator(Chain chain, String id, IGenericMediator type) throws CiliaException {
 		AbstractCompositionFile file = (AbstractCompositionFile) getContentProvider().getParent(chain);
 		if (file == null)
 			return;
 		file.getModel().createMediator(chain, id, type);
 	}
 
 	public void createAdapter(Chain chain, String id, IGenericAdapter type) throws CiliaException {
 		AbstractCompositionFile repo = (AbstractCompositionFile) getContentProvider().getParent(chain);
 		if (repo == null)
 			return;
 		repo.getModel().createAdapter(chain, id, type);
 	}
 
 	public void deleteComponent(Chain chain, ComponentRef component) throws CiliaException {
 		AbstractCompositionFile repo = (AbstractCompositionFile) getContentProvider().getParent(chain);
 		if (repo == null)
 			return;
 		repo.getModel().deleteComponent(chain, component);
 	}
 
 	public void createBinding(Chain chain, String srcElem, String srcPort, String dstElem, String dstPort,
 			Cardinality srcCard, Cardinality dstCard) throws CiliaException {
 		AbstractCompositionFile repo = (AbstractCompositionFile) getContentProvider().getParent(chain);
 		if (repo == null)
 			return;
 		repo.getModel().createBinding(chain, srcElem, srcPort, dstElem, dstPort, srcCard, dstCard);
 	}
 
 	@Override
 	public CiliaFlag[] getErrorsAndWarnings() {
 		List<CiliaFlag> errorList = IdentifiableUtils.getErrorsNonUniqueId(this, getChains());
 
 		return CiliaFlag.generateTab(errorList);
 	}
 
 	public void deleteBinding(Chain chain, Binding binding) throws CiliaException {
 		AbstractCompositionFile repo = (AbstractCompositionFile) getContentProvider().getParent(chain);
 		if (repo == null)
 			return;
 		repo.getModel().deleteBinding(chain, binding);
 	}
 }
