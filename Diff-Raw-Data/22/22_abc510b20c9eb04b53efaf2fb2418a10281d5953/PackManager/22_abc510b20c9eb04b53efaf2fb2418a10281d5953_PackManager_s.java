 /* Soot - a J*va Optimization Framework
  * Copyright (C) 2003 Ondrej Lhotak
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 package soot;
 import java.util.*;
 import soot.jimple.toolkits.invoke.*;
 import soot.jimple.toolkits.base.*;
 import soot.grimp.toolkits.base.*;
 import soot.baf.toolkits.base.*;
 import soot.jimple.toolkits.typing.*;
 import soot.jimple.toolkits.scalar.*;
 import soot.jimple.toolkits.scalar.pre.*;
 import soot.jimple.toolkits.annotation.arraycheck.*;
 import soot.jimple.toolkits.annotation.profiling.*;
 import soot.jimple.toolkits.annotation.nullcheck.*;
 import soot.jimple.toolkits.annotation.tags.*;
 import soot.jimple.toolkits.pointer.*;
 import soot.tagkit.*;
 import soot.options.Options;
 import soot.toolkits.scalar.*;
 import soot.jimple.spark.SparkTransformer;
 import soot.jimple.spark.callgraph.CHATransformer;
 import soot.jimple.spark.fieldrw.*;
 
 /** Manages the Packs containing the various phases and their options. */
 public class PackManager {
     public PackManager( Singletons.Global g ) 
     {
         Pack p;
 
         // Jimple body creation
         addPack(p = new JimpleBodyPack());
         {
             p.add(new Transform("jb.ls", LocalSplitter.v()));
             p.add(new Transform("jb.a", Aggregator.v()));
             p.add(new Transform("jb.asv", Aggregator.v()));
             p.add(new Transform("jb.ule", UnusedLocalEliminator.v()));
             p.add(new Transform("jb.tr", TypeAssigner.v()));
             p.add(new Transform("jb.lns", LocalNameStandardizer.v()));
             p.add(new Transform("jb.ulp", LocalPacker.v()));
             p.add(new Transform("jb.cp", CopyPropagator.v()));
             p.add(new Transform("jb.dae", DeadAssignmentEliminator.v()));
             p.add(new Transform("jb.cp-ule", UnusedLocalEliminator.v()));
             p.add(new Transform("jb.lp", LocalPacker.v()));
             p.add(new Transform("jb.ne", NopEliminator.v()));
             p.add(new Transform("jb.uce", UnreachableCodeEliminator.v()));
         }
 
         // Call graph pack
         addPack(p = new RadioScenePack("cg"));
         {
             p.add(new Transform("cg.oldcha", OldCHATransformer.v()));
             p.add(new Transform("cg.vta", VTATransformer.v()));
             p.add(new Transform("cg.cha", CHATransformer.v()));
             p.add(new Transform("cg.spark", SparkTransformer.v()));
         }
 
         // Whole-Shimple transformation pack
         addPack(p = new ScenePack("wstp"));
 
         // Whole-Shimple Optimization pack
         addPack(p = new ScenePack("wsop"));
 
         // Whole-Jimple transformation pack
         addPack(p = new ScenePack("wjtp"));
         {
         }
 
         // Whole-Jimple Optimization pack
         addPack(p = new ScenePack("wjop"));
         {
             p.add(new Transform("wjop.smb", StaticMethodBinder.v()));
             p.add(new Transform("wjop.si", StaticInliner.v()));
         }
 
         // Give another chance to do Whole-Jimple transformation
         // The RectangularArrayFinder will be put into this package.
         addPack(p = new ScenePack("wjap"));
         {
             p.add(new Transform("wjap.ra", RectangularArrayFinder.v()));
         }
 
         // Shimple transformation pack
         addPack(p = new BodyPack("stp"));
 
         // Shimple optimization pack
         addPack(p = new BodyPack("sop"));
 
         // Jimple transformation pack
         addPack(p = new BodyPack("jtp"));
 
         // Jimple optimization pack
         addPack(p = new BodyPack("jop"));
         {
             p.add(new Transform("jop.cse", CommonSubexpressionEliminator.v()));
             p.add(new Transform("jop.bcm", BusyCodeMotion.v()));
             p.add(new Transform("jop.lcm", LazyCodeMotion.v()));
             p.add(new Transform("jop.cp", CopyPropagator.v()));
             p.add(new Transform("jop.cpf", ConstantPropagatorAndFolder.v()));
             p.add(new Transform("jop.cbf", ConditionalBranchFolder.v()));
             p.add(new Transform("jop.dae", DeadAssignmentEliminator.v()));
             p.add(new Transform("jop.uce1", UnreachableCodeEliminator.v()));
             p.add(new Transform("jop.ubf1", UnconditionalBranchFolder.v()));
             p.add(new Transform("jop.uce2", UnreachableCodeEliminator.v()));
             p.add(new Transform("jop.ubf2", UnconditionalBranchFolder.v()));
             p.add(new Transform("jop.ule", UnusedLocalEliminator.v()));
         }
 
         // Jimple annotation pack
         addPack(p = new BodyPack("jap"));
         {
             p.add(new Transform("jap.npc", NullPointerChecker.v()));
             p.add(new Transform("jap.abc", ArrayBoundsChecker.v()));
             p.add(new Transform("jap.profiling", ProfilingGenerator.v()));
             p.add(new Transform("jap.sea", SideEffectTagger.v()));
             p.add(new Transform("jap.fieldrw", FieldTagger.v()));
         }
 
         // Grimp body creation
         addPack(p = new BodyPack("gb"));
         {
             p.add(new Transform("gb.a1", Aggregator.v()));
             p.add(new Transform("gb.cf", ConstructorFolder.v()));
             p.add(new Transform("gb.a2", Aggregator.v()));
             p.add(new Transform("gb.ule", UnusedLocalEliminator.v()));
         }
 
         // Grimp optimization pack
         addPack(p = new BodyPack("gop"));
 
         // Baf body creation
         addPack(p = new BodyPack("bb"));
         {
             p.add(new Transform("bb.lso", LoadStoreOptimizer.v()));
             p.add(new Transform("bb.pho", PeepholeOptimizer.v()));
             p.add(new Transform("bb.ule", UnusedLocalEliminator.v()));
             p.add(new Transform("bb.lp", LocalPacker.v()));
         }
 
         // Baf optimization pack
         addPack(p = new BodyPack("bop"));
 
         // Code attribute tag aggregation pack
         addPack(p = new BodyPack("tag"));
         {
             p.add(new Transform("tag.ln", LineNumberTagAggregator.v()));
             p.add(new Transform("tag.an", ArrayNullTagAggregator.v()));
             p.add(new Transform("tag.dep", DependenceTagAggregator.v()));
             p.add(new Transform("tag.fieldrw", FieldTagAggregator.v()));
         }
     }
     public static PackManager v() { return G.v().PackManager(); }
 
     private Map phaseToOptionMap = new HashMap();
     private Map packNameToPack = new HashMap();
     private List packList = new LinkedList();
 
     private void addPack( Pack p ) {
         if( packNameToPack.containsKey( p.getPhaseName() ) )
             throw new RuntimeException( "Duplicate pack "+p.getPhaseName() );
         packNameToPack.put( p.getPhaseName(), p );
         packList.add( p );
     }
 
     public boolean hasPack(String phaseName) {
         return getPhase( phaseName ) != null;
     }
 
     public Pack getPack(String phaseName) {
         Pack p = (Pack) packNameToPack.get(phaseName);
         return p;
     }
 
     public boolean hasPhase(String phaseName) {
         return getPhase(phaseName) != null;
     }
 
     public HasPhaseOptions getPhase(String phaseName) {
         int index = phaseName.indexOf( "." );
         if( index < 0 ) return getPack( phaseName );
         String packName = phaseName.substring(0,index);
         if( !hasPack( packName ) ) return null;
         return getPack( packName ).get( phaseName );
     }
 
     public Transform getTransform(String phaseName) {
         return (Transform) getPhase( phaseName );
     }
 
     public Map getPhaseOptions(String phaseName) {
         return getPhaseOptions(getPhase(phaseName));
     }
 
     public Map getPhaseOptions(HasPhaseOptions phase) {
         Map ret = (Map) phaseToOptionMap.get(phase);
         if( ret == null ) ret = new HashMap();
         else ret = new HashMap( ret );
         StringTokenizer st = new StringTokenizer( phase.getDefaultOptions() );
         while( st.hasMoreTokens() ) {
             String opt = st.nextToken();
             String key = getKey( opt );
             String value = getValue( opt );
             if( !ret.containsKey( key ) ) ret.put( key, value );
         }
         return Collections.unmodifiableMap(ret);
     }
 
     public boolean processPhaseOptions(String phaseName, String option) {
         StringTokenizer st = new StringTokenizer(option, ",");
         while (st.hasMoreTokens()) {
             if( !setPhaseOption( phaseName, st.nextToken() ) ) {
                 return false;
             }
         }
         return true;
     }
 
     /** This method returns true iff key "name" is in options 
         and maps to "true". */
     public static boolean getBoolean(Map options, String name)
     {
         return options.containsKey(name) &&
             options.get(name).equals("true");
     }
 
 
 
     /** This method returns the value of "name" in options 
         or "" if "name" is not found. */
     public static String getString(Map options, String name)
     {
         return options.containsKey(name) ?
             (String)options.get(name) : "";
     }
 
 
 
     /** This method returns the float value of "name" in options 
         or 1.0 if "name" is not found. */
     public static float getFloat(Map options, String name)
     {
         return options.containsKey(name) ?
             new Float((String)options.get(name)).floatValue() : 1.0f;
     }
 
 
 
     /** This method returns the integer value of "name" in options 
         or 0 if "name" is not found. */
     public static int getInt(Map options, String name)
     {
         return options.containsKey(name) ?
             new Integer((String)options.get(name)).intValue() : 0;
     }
 
 
     private Map mapForPhase( String phaseName ) {
         HasPhaseOptions phase = getPhase( phaseName );
         if( phase == null ) return null;
         Map optionMap = (Map) phaseToOptionMap.get( phase );
         if( optionMap == null ) {
             phaseToOptionMap.put( phase, optionMap = new HashMap() );
         }
         return optionMap;
     }
 
     private String getKey( String option ) {
         int delimLoc = option.indexOf(":");
         if (delimLoc < 0) {
             return option;
         } else {
             return option.substring(0, delimLoc);
         }
     }
     private String getValue( String option ) {
         int delimLoc = option.indexOf(":");
         if (delimLoc < 0) {
             return "true";
         } else {
             return option.substring(delimLoc+1);
         }
     }
     public boolean setPhaseOption( String phaseName, String option ) {
         Map optionMap = mapForPhase( phaseName );
         if( optionMap == null ) {
             G.v().out.println( "Option "+option+" given for nonexistent"
                     +" phase "+phaseName );
             return false;
         }
        optionMap.put( getKey( option ), getValue( option ) );
        return true;
     }
 
     public void setPhaseOptionIfUnset( String phaseName, String option ) {
         Map optionMap = mapForPhase( phaseName );
         if( optionMap == null )
             throw new RuntimeException( "No such phase "+phaseName );
         if( optionMap.containsKey( getKey( option ) ) ) return;
         optionMap.put( getKey( option ), getValue( option ) );
     }
 
     public Collection allPacks() {
         return Collections.unmodifiableList( packList );
     }
 }
