 package org.oobium.build.console.commands.export;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.oobium.build.console.BuilderCommand;
 import org.oobium.build.console.Eclipse;
 import org.oobium.build.gen.android.GeneratorEvent;
 import org.oobium.build.gen.android.GeneratorListener;
 import org.oobium.build.gen.android.ScaffoldingGenerator;
 import org.oobium.build.workspace.AndroidApp;
 import org.oobium.build.workspace.ClientExporter;
 import org.oobium.build.workspace.Module;
 import org.oobium.build.workspace.Project;
 import org.oobium.build.workspace.Project.Type;
 import org.oobium.build.workspace.Workspace;
 
 public class ClientCommand extends BuilderCommand {
 
 	@Override
 	protected void configure() {
 		moduleRequired = true;
 	}
 	
 	@Override
 	protected void run() {
 		Workspace workspace = getWorkspace();
 		Module module = getModule();
 		
 		Project target = null;
 		if(hasParam("target")) {
 			target = workspace.getProject(param("target"));
 			if(target == null) {
 				console.err.println(param("target") + " does not exist in the workspace");
 				return;
 			}
 		}
 		if(target == null) {
 			Project[] projects = workspace.getProjects(Type.Android);
 			if(projects.length == 1) {
 				String s = ask("use \"" + projects[0] + "\" as the target? [Y/N] ");
 				if("Y".equalsIgnoreCase(s)) {
 					target = (AndroidApp) projects[0];
 				}
 			} else if(projects.length > 1) {
 				Arrays.sort(projects);
 				StringBuilder sb = new StringBuilder();
 				sb.append(projects.length).append(" Android projects found:");
 				for(int i = 0; i < projects.length; i++) {
 					sb.append("\n  " + i + " - " + projects[i].name);
 				}
 				sb.append("\nselect which to use as the target (0-" + (projects.length - 1) + "), or <Enter> for none ");
 				String s = ask(sb.toString());
 				try {
 					int i = Integer.parseInt(s);
 					if(i >= 0 && i < projects.length) {
 						target = (AndroidApp) projects[i];
 					}
 				} catch(Exception e) {
 					// discard
 				}
 			}
 		}
 		try {
 			boolean full = !module.hasModels() || ("Y".equalsIgnoreCase(ask("export the full [P]roject, or just its [M]odels? [P/M] ")));
 
 			ClientExporter exporter = new ClientExporter(workspace, module);
 			exporter.setFull(full);
 			exporter.includeSource(true);
 			exporter.setTarget(target);
 			File[] jars = exporter.export();
 			for(File jar : jars) {
 				if(target == null) {
 					console.out.println("exported <a href=\"open file " + jar.getAbsolutePath() + "\">" + jar.getName() + "</a>" +
 							" to <a href=\"open file " + workspace.getExportDir().getAbsolutePath() + "\">Export Directory</a>");
 				} else {
 					int len = target.file.getAbsolutePath().length();
 					String name = target.name + ": " + jar.getParent().substring(len);
 					console.out.println("exported <a href=\"open file " + jar.getAbsolutePath() + "\">" + jar.getName() + "</a>" +
 							" to <a href=\"open file " + jar.getParent() + "\">" + name + "</a>");
 				}
 			}
 
 			if(target instanceof AndroidApp) {
 				String s = ask("create scaffolding? [Y/N] ");
 				if("Y".equalsIgnoreCase(s)) {
 					AndroidApp android = (AndroidApp) target;
 					ScaffoldingGenerator gen = new ScaffoldingGenerator(module, android);
 					gen.setListener(new GeneratorListener() {
 						@Override
 						public void handleEvent(GeneratorEvent event) {
 							console.out.println(event.data);
 						}
 					});
 					List<File> files = gen.generateScaffolding();
 					for(File file : files) {
 						int len = target.file.getAbsolutePath().length();
 						String path = target.file.getAbsolutePath() + "#" + file.getAbsolutePath().substring(len+1);
 						console.out.println("  exported <a href=\"open file " + path + "\">" + file.getName() + "</a>");
 					}
 
 					File[] ma = android.getMainActivities();
 					if(ma.length == 1) {
 						String name = ma[0].getName();
 						name = name.substring(0, name.length()-5);
 						s = ask("add launch buttons to the '" + name + "' activity? [Y/N] ");
 						if("Y".equalsIgnoreCase(s)) {
 							File layout = gen.generateLaunchButton(ma[0]);
 							if(layout != null) {
 								int len = target.file.getAbsolutePath().length();
 								String path = target.file.getAbsolutePath() + "#" + ma[0].getAbsolutePath().substring(len+1);
 								console.out.println("  modified activity <a href=\"open file " + path + "\">" + ma[0].getName() + "</a>");
 								
 								path = target.file.getAbsolutePath() + "#" + layout.getAbsolutePath().substring(len+1);
 								console.out.println("  modified layout <a href=\"open file " + path + "\">" + layout.getName() + "</a>");
 							}
 						}
 					}
 				}
 			}
 			
 			if(target != null) {
				console.out.println("export complete\n *** models to be accessed by the client must be published first ***");
 				Eclipse.refreshProject(target.name);
 			}
 		} catch(IOException e) {
 			console.err.print(e);
 		}
 	}
 	
 }
