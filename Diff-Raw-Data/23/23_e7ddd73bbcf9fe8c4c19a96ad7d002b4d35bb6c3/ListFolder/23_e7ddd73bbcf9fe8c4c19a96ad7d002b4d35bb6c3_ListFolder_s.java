 /*******************************************************************************
  * Copyright (c) Sep 19, 2011 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package util;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.ecore.resource.Resource;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 import com.google.common.collect.UnmodifiableIterator;
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.NetxstudioPackage;
 import com.netxforge.netxstudio.ServerSettings;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.data.IDataProvider;
 import com.netxforge.netxstudio.server.Server;
 import com.netxforge.netxstudio.server.jsp.JSPActivator;
 
 
 /**
  * 
  * @author Christophe Bouhier
  *
  */
 public final class ListFolder {
 
 	@Inject
 	@Server
	IDataProvider dataProvider;

 	@Inject
 	private ModelUtils modelUtils;
 
 	private ServerSettings settings;
 
 	private static final ListFolder INSTANCE = new ListFolder();
 
 	public static final ListFolder getInstance() {
 		return INSTANCE;
 	}
 
 	public ListFolder() {
 		JSPActivator.getInstance().getInjector().injectMembers(this);
 		dataProvider.openSession();
 		Resource res = dataProvider
 				.getResource(NetxstudioPackage.Literals.SERVER_SETTINGS);
 		if (res.getContents().size() == 1) {
 			settings = (ServerSettings) res.getContents().get(0);
 		}
 
 	}
 
 	public String getPath() {
 		return settings.getExportPath();
 	}
 
 	public List<File> getFiles(String contextPath) {
 
 		// Compare to base context path.
 		int index = contextPath.indexOf(JSPActivator.CONTEXT_PATH, 0);
 		String exportPath = settings.getExportPath();
 		if (index == 0) {
 			exportPath += contextPath.substring(JSPActivator.CONTEXT_PATH
 					.length());
 		}
 		File path = new File(exportPath);
 		if (path.exists() && path.isDirectory()) {
 			File[] files = path.listFiles();
 			List<File> filesList = Lists.newArrayList(files);
 			Collections
 					.sort(filesList, modelUtils.fileLastModifiedComparator());
 			UnmodifiableIterator<File> filter = Iterators.filter(
 					filesList.iterator(), modelUtils.nonHiddenFile());
 			return Lists.newArrayList(filter);
 		}
 		return Lists.newArrayList();
 	}
 
 	public static final int NOT_PROCESSED = 0; // NOT DONE FILES.
 	public static final int DONE = 1; // NOT DONE FILES.
 	public static final int DONE_WITH_FAILURE = 2; // NOT DONE FILES.
 
 	public List<File> getSourceFiles(String contextPath, int type) {
 
 		List<File> result = Lists.newArrayList();
 
 		// Compare to base context path.
 		int index = contextPath.indexOf(JSPActivator.CONTEXT_PATH, 0);
 		String importPath = settings.getImportPath();
 		if (index == 0) {
 			importPath += contextPath.substring(JSPActivator.CONTEXT_PATH
 					.length());
 		}
 		File path = new File(importPath);
 		if (path.exists() && path.isDirectory()) {
 			File[] files = path.listFiles();
 			List<File> filesList = Lists.newArrayList(files);
 			Collections
 					.sort(filesList, modelUtils.fileLastModifiedComparator());
 
 			Predicate<File> fileExtensionPredicate = null;
 			switch (type) {
 			
 			case NOT_PROCESSED: {
 				// All non DONE, and non DONE_WITH_FAILURES. 
 				fileExtensionPredicate = modelUtils.extensionFile(true,
 						ModelUtils.EXTENSION_DONE,
 						ModelUtils.EXTENSION_DONE_WITH_FAILURES);
 			}
 				break;
 			case DONE: {
 				fileExtensionPredicate = modelUtils
 						.extensionFile(ModelUtils.EXTENSION_DONE);
 			}
 				break;
 
 			case DONE_WITH_FAILURE: {
 				fileExtensionPredicate = modelUtils
 						.extensionFile(ModelUtils.EXTENSION_DONE_WITH_FAILURES);
 			}
 				break;
 			}
 
 			UnmodifiableIterator<File> filter = Iterators.filter(
 					filesList.iterator(), modelUtils.nonHiddenFile());
 			// Check to see, if we have a predicate to filter. 
 			if (fileExtensionPredicate != null) {
 				filter = Iterators.filter(filter, fileExtensionPredicate);
 			}
 
 			result = Lists.newArrayList(filter);
 		}
 
 		return result;
 	}
 
 }
