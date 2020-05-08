 /*
  * Copyright (c) 2008-2010, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  * Licensed under the terms of the New BSD License.
  */
 package chord.project;
 
 import hj.runtime.wsh.WshRuntime_c;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.FileWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import CnCHJ.api.ItemCollection;
 import CnCHJ.runtime.CnCRuntime;
 
 import chord.program.Program;
 import chord.project.analyses.DlogAnalysis;
 import chord.project.analyses.ProgramDom;
 import chord.project.analyses.ProgramRel;
 import chord.project.ITask;
 import chord.util.ArraySet;
 import chord.util.ArrayUtils;
 import chord.util.ClassUtils;
 import chord.util.StringUtils;
 import chord.util.tuple.object.Pair;
 import chord.util.ChordRuntimeException;
 import chord.bddbddb.RelSign;
 import chord.bddbddb.Dom;
 
 /**
  * A Chord project comprising a set of tasks and a set of targets
  * produced/consumed by those tasks.
  * 
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 public class ModernProject extends Project {
 	private static ModernProject project = null;
 	public static ModernProject g() {
 		if (project == null)
 			project = new ModernProject();
 		return project;
 	}
 	private boolean isBuilt = false;
 	private CnCRuntime runtime;
 
 	@Override
 	public void build() {
 		if (isBuilt)
 			return;
 		TaskParser taskParser = new TaskParser();
 		if (!taskParser.run())
 			abort();
 		Map<String, Set<TrgtInfo>> nameToTrgtInfosMap =
 			taskParser.getNameToTrgtInfosMap();
 		TrgtParser trgtParser = new TrgtParser(nameToTrgtInfosMap);
 		if (!trgtParser.run())
 			abort();
 /*
 		for (Pair<ProgramRel, RelSign> tuple : todo) {
 			ProgramRel rel = tuple.val0;
 			RelSign sign = tuple.val1;
 			String[] domNames = sign.getDomNames();
 			ProgramDom[] doms = new ProgramDom[domNames.length];
 			for (int i = 0; i < domNames.length; i++) {
 				String domName = StringUtils.trimNumSuffix(domNames[i]);
 				Object trgt = nameToTrgtMap.get(domName);
 				assert (trgt != null);
 				assert (trgt instanceof ProgramDom);
 				doms[i] = (ProgramDom) trgt;
 			}
 			rel.setSign(sign);
 			rel.setDoms(doms);
 		}
 		if (Config.reuseRels)
 			throw new RuntimeException();
 */
 
 		/*
 			// TODO
 			// no need to create each step coll as tasks have already been created
 			DomMstep domMstep = new DomMstep();
 			DomHstep domHstep = new DomHstep();
 			RelMHstep relMHstep = new RelMHstep();
 			// create each ctrl coll and link it to step colls it prescribes
 			TagCollection Mtags   = new TagCollectionImpl(g, "M" , new IStep[] { domMstep });
 			TagCollection Htags   = new TagCollectionImpl(g, "H" , new IStep[] { domHstep });
 			TagCollection MHtags = new TagCollectionImpl(g, "MH", new IStep[] { relMHstep });
 		 */
 		ArrayList cList = new ArrayList();
 		/*	
 			// add all ctrl colls to cList
 			cList.add(Mtags);
 			cList.add(Htags);
 			cList.add(MHtags);
 			// create each item coll
 			ItemCollection domMitems = ItemCollectionFactory.Create("M");
 			ItemCollection domHitems = ItemCollectionFactory.Create("H");
 			ItemCollection relMHitems = ItemCollectionFactory.Create("MH");
 			// create map from each item coll's name to item coll
 		 */
 		runtime = new CnCRuntime(cList, null);
 		isBuilt = true;
 	}
 
 	@Override
 	public void run(String[] taskNames) {
 		build();
         WshRuntime_c.getCurrentWshActivity().startFinish();
         try {
         	for (String name : taskNames) {
             	ICtrlCollection c = getCtrlCollectionOfTask(name);
                c.Put("dummy");
         	}
         } catch (Throwable ex) {
         	Messages.fatal(ex);
         }
         WshRuntime_c.getCurrentWshActivity().stopFinish();
 	}
 	
 	@Override
 	public void printRels(String[] relNames) {
		throw new RuntimeException();
 	}
 
 	@Override
 	public void print() {
 		build();
 		throw new RuntimeException();
 	}
 
 	public ItemCollection getDataCollectionByName(String name) {
 		// TODO
 		// return item collection named name
 		return null;
 	}
 
 	public ICtrlCollection getCtrlCollectionByName(String name) {
 		// TODO
 		// return ctrl collection named name
 		return null;
 	}
 
 	public ICtrlCollection getCtrlCollectionOfTask(String name) {
 		// TODO
 		// return ctrl collection prescribing task
 		return null;
 	}
 
 	public CnCRuntime getRuntime() {
 		return runtime;
 	}
 
 	private static String getNameAndURL(ITask task) {
 		Class clazz = task.getClass();
 		String loc;
 		if (clazz == DlogAnalysis.class) {
 			loc = ((DlogAnalysis) task).getFileName();
 			loc = (new File(loc)).getName();
 		} else
 			loc = clazz.getName().replace(".", "/") + ".html";
 		loc = Config.javadocURL + loc;
 		return "producer_name=\"" + task.getName() +
 			"\" producer_url=\"" + loc + "\"";
 	}
 }
