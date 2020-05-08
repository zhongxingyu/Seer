 /*  RapidMiner Integration for KNIME
  *  Copyright (C) 2012 Mind Eratosthenes Kft.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.mind_era.knime_rapidminer.knime.nodes;
 
 import java.util.Collections;
 import java.util.List;
import java.util.logging.Logger;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.knime.core.node.defaultnodesettings.HasTableSpecAndRowId;
 
 import com.mind_era.knime_rapidminer.knime.nodes.internal.RapidMinerNodePlugin;
 import com.mind_era.knime_rapidminer.knime.nodes.preferences.PreferenceConstants;
 import com.mind_era.knime_rapidminer.knime.nodes.preferences.PreferenceInitializer;
 import com.mind_era.knime_rapidminer.knime.nodes.util.KnimeRepository;
 import com.rapid_i.Launcher;
 import com.rapid_i.deployment.update.client.ManagedExtension;
 import com.rapidminer.RapidMiner;
 import com.rapidminer.gui.AbstractUIState;
 import com.rapidminer.gui.MainFrame;
 import com.rapidminer.parameter.ParameterType;
 import com.rapidminer.parameter.ParameterTypeBoolean;
 import com.rapidminer.parameter.ParameterTypeInt;
 import com.rapidminer.parameter.ParameterTypeStringCategory;
 import com.rapidminer.repository.Repository;
 import com.rapidminer.repository.RepositoryAccessor;
 import com.rapidminer.repository.RepositoryFactory;
 import com.rapidminer.repository.RepositoryManager;
 import com.rapidminer.tools.ParameterService;
 
 /**
  * Initialize RapidMiner based on eclipse settings.
  * 
  * @author Gabor
  */
 public class RapidMinerInit {
	private static volatile boolean isInitialized = false, isInitializing = false;
 
 	public static synchronized void init(final boolean force) {
 		if (!isInitialized || force) {
			if (isInitializing && ! force) {
				Logger.getAnonymousLogger().warning("Initializing RapidMiner from another initializer, or the first initialization was not finished properly.");
				return;
			}
			isInitializing = true;
 			final String rapidMinerHome = RapidMinerNodePlugin.getDefault()
 					.getPreferenceStore()
 					.getString(PreferenceConstants.RAPIDMINER_PATH);
 			System.setProperty(Launcher.PROPERTY_RAPIDMINER_HOME,
					rapidMinerHome);
 			RapidMiner
 					.setExecutionMode(RapidMiner.ExecutionMode.EMBEDDED_WITH_UI);
 			RepositoryManager.registerFactory(new RepositoryFactory() {
 
 				@Override
 				public List<? extends Repository> createRepositoriesFor(
 						final RepositoryAccessor accessor) {
 					if (accessor instanceof HasTableSpecAndRowId) {
 						final HasTableSpecAndRowId model = (HasTableSpecAndRowId) accessor;
 						return Collections.singletonList(new KnimeRepository(
 								model));
 
 					}
 					return Collections.emptyList();
 				}
 			});
 			ManagedExtension.init();
 			
 			RapidMiner.init();
 			//Initialize the static initializers for MainFrame and AbstractUIPlugin
 			@SuppressWarnings("unused")
 			String _ = MainFrame.PROPERTY_RAPIDMINER_GUI_LOG_LEVEL.toString() + AbstractUIState.DOCK_GROUP_ROOT.getName();
 			//End of static init.
 			
 			isInitialized = true;
			isInitializing = false;
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public static synchronized void setPreferences() {
 		final IPreferenceStore store = RapidMinerNodePlugin.getDefault()
 				.getPreferenceStore();
 		for (final String parameterKey : ParameterService.getParameterKeys()) {
 			final ParameterType type = ParameterService
 					.getParameterType(parameterKey);
 			String storeKey = PreferenceInitializer
 					.getRapidminerPreferenceKey(parameterKey);
 			if (type instanceof ParameterTypeBoolean) {
 				ParameterService.setParameterValue(parameterKey, Boolean
 						.toString(store.getBoolean(storeKey)));
 			} else if (type instanceof ParameterTypeInt) {
 				ParameterService.setParameterValue(parameterKey, Integer
 						.toString(store.getInt(storeKey)));
 			} else if (type instanceof ParameterTypeStringCategory) {
 				ParameterService.setParameterValue(parameterKey, Integer
 						.toString(store.getInt(storeKey)));
 			} else {
 				if (type != null && type.getDefaultValueAsString() != null && !type.getDefaultValueAsString().equals(store.getDefaultString(storeKey))) {
 					store.setDefault(storeKey, type.getDefaultValueAsString());
 				}
 				ParameterService.setParameterValue(parameterKey, store
 						.getString(storeKey));
 			}
 		}
 	}
 }
