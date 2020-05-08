 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share � to copy, distribute and transmit the work
     to Remix � to adapt the work
 
  Under the following conditions:
     Attribution � You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial � You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver � Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain � Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights � In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice � For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.Flags.metrics;
 
 import io.github.alshain01.Flags.Flags;
 import io.github.alshain01.Flags.SystemType;
 import io.github.alshain01.Flags.metrics.Metrics.Graph;
import io.github.alshain01.Flags.metrics.Metrics;
 
 import java.io.IOException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.Plugin;
 
 public class MetricsManager {
 	public static void StartMetrics(Plugin plugin) {
 		try {
 			final Metrics metrics = new Metrics(plugin);
 
 			// Land System Graph
 			final Graph systemGraph = metrics.createGraph("Land System");
 			for (final SystemType system : SystemType.values()) {
 				if (SystemType.getActive() == system) {
 					systemGraph.addPlotter(new Metrics.Plotter(system.getDisplayName()) {
 						@Override
 						public int getValue() {
 							return 1;
 						}
 					});
 				}
 			}
 
 			// Land System by PlayersGraph
 			final Graph systemPlayersGraph = metrics
 					.createGraph("Land System by Players");
 			for (final SystemType system : SystemType.values()) {
 				if (SystemType.getActive() == system) {
 					systemPlayersGraph.addPlotter(new Metrics.Plotter(system.getDisplayName()) {
 						@Override
 						public int getValue() {
 							return Bukkit.getOnlinePlayers().length;
 						}
 					});
 				}
 			}
 
 			// Flag groups installed
 			final Graph groupGraph = metrics.createGraph("Flag Groups");
 			for (final String group : Flags.getRegistrar().getFlagGroups()) {
 				groupGraph.addPlotter(new Metrics.Plotter(group) {
 					@Override
 					public int getValue() {
 						return 1;
 					}
 				});
 			}
 
 			// Border Patrol Status
 			final Graph bpGraph = metrics.createGraph("BorderPatrol Enabled");
 			if (Flags.getBorderPatrolEnabled()) {
 				bpGraph.addPlotter(new Metrics.Plotter("Enabled") {
 					@Override
 					public int getValue() {
 						return 1;
 					}
 				});
 			} else {
 				bpGraph.addPlotter(new Metrics.Plotter("Disabled") {
 					@Override
 					public int getValue() {
 						return 1;
 					}
 				});
 			}
 
 			// Auto Update settings
 			final Graph updateGraph = metrics.createGraph("Update Configuration");
 			if (!plugin.getConfig()
 					.getBoolean("Flags.Update.Check")) {
 				updateGraph.addPlotter(new Metrics.Plotter("No Updates") {
 					@Override
 					public int getValue() {
 						return 1;
 					}
 				});
 			} else if (!plugin.getConfig()
 					.getBoolean("Flags.Update.Download")) {
 				updateGraph
 						.addPlotter(new Metrics.Plotter("Check for Updates") {
 							@Override
 							public int getValue() {
 								return 1;
 							}
 						});
 			} else {
 				updateGraph.addPlotter(new Metrics.Plotter("Download Updates") {
 					@Override
 					public int getValue() {
 						return 1;
 					}
 				});
 			}
 
 			// Economy Graph
 			final Graph econGraph = metrics.createGraph("Economy Enabled");
 			if (Flags.getEconomy() == null) {
 				econGraph.addPlotter(new Metrics.Plotter("No") {
 					@Override
 					public int getValue() {
 						return 1;
 					}
 				});
 			} else {
 				econGraph.addPlotter(new Metrics.Plotter("Yes") {
 					@Override
 					public int getValue() {
 						return 1;
 					}
 				});
 			}
 
 			metrics.start();
 		} catch (final IOException e) {
 			plugin.getLogger().info(e.getMessage());
 		}
 	}
 
 	private MetricsManager() {
 	}
 }
