 package org.programmingteam;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.programmingteam.qsync.QSync;
 import org.programmingteam.qsync.QSyncImport;
 import org.programmingteam.qsync.QSyncVcxproj;
 import org.programmingteam.vs2010.VcxprojSync;
 
 
 public class Main
 {
 	public static Options OPTS;
 	
 	public enum InteractiveOption
 	{
 		SAVE, CANCEL, INVALID
 	};
 	
 	public static void main(String[] args)
 	{	
 		OPTS = new Options(args);
 		
 		File qsyncFile = new File(OPTS.getFile());
 		if(!qsyncFile.exists())
 		{
 			Log.e("Configuration file not found (" + OPTS.getFile() + ")");
 			System.exit(-1);
 		}
 		
 		//Read file
 		QSync qsync = new QSync(qsyncFile);
 		qsync.debugPrint();
 		Log.v("Qsyn created ok.");
 		
 		List<QSyncVcxproj> qsyncProjs = qsync.getProjects();
 		for(QSyncVcxproj qsyncProj : qsyncProjs)
 		{
 			Log.d(">>> Sync: " + qsyncProj.getVcxproj() + "");
 			VcxprojSync vcxprojSync = new VcxprojSync(qsyncProj.getVcxproj(), qsyncProj.getVcxprojFilters());
 			for(QSyncImport imp: qsyncProj.getImportList())
 			{
 				Log.d("Parsing <import tofilter=\"" + imp.getToFilter() + "\">");
 				vcxprojSync.invalidateFilters(imp.getToFilter());
 				
 				ArrayList<File> dirList = new ArrayList<File>();
 				if(imp.getInclude()!=null) dirList.add(new File(imp.getInclude()));
 				if(imp.getSrc()!=null) dirList.add(new File(imp.getSrc()));
 				
 				Log.v("Include directory: " + imp.getInclude());
 				Log.v("Src directory: " + imp.getSrc());
 				
 				while(dirList.size()>0)
 				{
 					File dir = dirList.get(0);
 					dirList.remove(0);
 					File listFiles[] = dir.listFiles();
 					if(listFiles==null)
 					{
 						Log.e("Directory does not exist! " + dir);
 						System.exit(-1);
 					}
 					for(int i=0; i<listFiles.length; ++i)
 					{
 						if(listFiles[i].isDirectory())
 						{
 							dirList.add(listFiles[i]);
 							
 							if(imp.isIncludeEmptyDirs())
 							{
 								String toFilter = imp.getFileFilterPath(listFiles[i].getAbsolutePath()); 
 								toFilter = Helpers.stripSlashes(toFilter);
 								vcxprojSync.syncFilter(toFilter);
 							}
 						}
 						else
 						{
							//Watch out! imp.getInclude() and imp.getSrc() can return nulls!
							boolean flgIncludeDir = imp.getInclude()!=null && listFiles[i].getAbsolutePath().startsWith(imp.getInclude());
 							boolean flgMatchGlobalIncludeExt = Helpers.isInclude(listFiles[i], qsync.getIncludeExt());
 							boolean flgMatchGlobalCompileExt = Helpers.isCompile(listFiles[i], qsync.getCompileExt());
 							boolean flgMatchGlobalPattern = flgMatchGlobalCompileExt || flgMatchGlobalIncludeExt;
 							boolean flgAddFile = false;
 							
							if(flgIncludeDir && imp.matchesInclue(listFiles[i].getName()) && flgMatchGlobalPattern)
 							{
 								Log.v("+" + listFiles[i] +" (added)");
 								flgAddFile = true;
 							}
							else if(!flgIncludeDir && imp.matchesSrc(listFiles[i].getName()) && flgMatchGlobalPattern)
 							{
 								Log.v("+" + listFiles[i] +" (added)");
 								flgAddFile = true;
 							}
 							
 							if(!flgAddFile)
 							{
 								Log.v("-" + listFiles[i] + " (skipped)");
 								continue;
 							}
 							
							boolean isExcludedFromBuild = flgIncludeDir?
 									imp.isExcludedInc(""+listFiles[i].getName()):
 									imp.isExcludedSrc(""+listFiles[i].getName());
 									
 							VcxprojSync.SyncType syncType = flgMatchGlobalIncludeExt?
 									VcxprojSync.SyncType.INCLUDE:
 									VcxprojSync.SyncType.COMPILE;
 							
 							try
 							{
 								String toFilter = imp.getFileFilterPath(listFiles[i].getAbsolutePath());
 								toFilter = Helpers.getPath(toFilter);
 								toFilter = Helpers.stripSlashes(toFilter);
 								vcxprojSync.syncFile(
 										qsyncProj.getRelativeFile(listFiles[i].getCanonicalPath()), 
 										toFilter, 
 										syncType, 
 										isExcludedFromBuild);
 							}
 							catch(IOException e)
 							{
 								Log.e("Error obtaining folder cannonical path!" + listFiles[i]); 
 							}
 						}
 					}
 				}
 				vcxprojSync.printLog(imp.getToFilter());
 			}
 				
 			//TODO save files!
 			if(vcxprojSync.getNumChanges()==0)
 			{
 				Log.d("0 modifications detected, skipping file save...");
 			}
 			else if(OPTS.isPretend())
 			{
 				Log.d("Pretend option: skipping file save...");
 			}
 			else
 			{
 				Log.d_noln(vcxprojSync.getNumChanges() + " modifications detected");
 				
 				InteractiveOption opt = InteractiveOption.SAVE;
 				if(OPTS.isInteractive())
 				{
 					opt = InteractiveOption.INVALID;
 					while(opt==InteractiveOption.INVALID)
 					{
 						Log.d_noln(" [s]ave [c]ancel ");
 						String string ="";
 						InputStreamReader input = new InputStreamReader(System.in);
 						BufferedReader reader = new BufferedReader(input);
 						try { string = reader.readLine(); }
 						catch(Exception e) {}
 						
 						if(string.equals("s"))
 							opt = InteractiveOption.SAVE;
 						else if(string.equals("c"))
 							opt = InteractiveOption.CANCEL;
 						else
 							Log.d_noln(" invalid option");
 					}
 				}
 				
 				if(opt==InteractiveOption.SAVE)
 				{
 					Log.d("saving...");
 					vcxprojSync.saveVcxproj(OPTS.getOutput());
 					vcxprojSync.saveVcxprojFilters(OPTS.getOutput());
 				}
 				else
 				{
 					Log.d("skipping file save");
 				}
 			}
 			Log.d("Done.");
 		}
 
 	}
 }
