 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.jamgotchian.abcd.core.ir;
 
 import com.google.common.base.Objects;
 import fr.jamgotchian.abcd.core.common.ABCDException;
 import fr.jamgotchian.abcd.core.common.ABCDWriter;
 import static fr.jamgotchian.abcd.core.util.ConsoleUtil.*;
 import java.util.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class RegionAnalysis {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(RegionAnalysis.class.getName());
 
     private final ControlFlowGraph cfg0;
 
     private final ABCDWriter writer;
 
     private int level;
 
     public RegionAnalysis(ControlFlowGraph cfg0, ABCDWriter writer) {
         this.cfg0 = cfg0;
         this.writer = writer;
     }
 
     private static ControlFlowGraph createSubCFG(ControlFlowGraph cfg, Region region) {
         if (region.getEntry() == null
                 || region.getExit() == null
                 || region.getEntry().equals(region.getExit())) {
             throw new ABCDException("Cannot create subgraph from region " + region);
         }
         ControlFlowGraph subCfg
                 = new ControlFlowGraph(cfg.getName(), region.getEntry(), region.getExit());
         for (BasicBlock bb : region.getBasicBlocks()) {
             if (!bb.equals(region.getEntry()) && !bb.equals(region.getExit())) {
                 subCfg.addBasicBlock(bb);
             }
         }
         for (Edge e : cfg.getEdges()) {
             BasicBlock source = cfg.getEdgeSource(e);
             BasicBlock target = cfg.getEdgeTarget(e);
             if (subCfg.containsBasicBlock(source)
                     && subCfg.containsBasicBlock(target)) {
                 subCfg.addEdge(source, target, e);
             }
         }
         return subCfg;
     }
 
     private boolean checkTrivialRegion(ControlFlowGraph cfg, Region region) {
         LOGGER.trace("Check trivial region {}", region);
         if (region.getChildCount() == 1) {
             Region child = region.getFirstChild();
             if (child.getEntry().getType() != BasicBlockType.JUMP_IF) {
                 LOGGER.debug("Found trivial region {}", region);
                 region.setParentType(ParentType.TRIVIAL);
 
                 // propagate to children
                 if (!checkRegion(cfg, child)) {
                     throw new ABCDException("Cannot find type of region "
                             + child);
                 }
                 return true;
             }
         }
         return false;
     }
 
     private boolean checkSequenceRegion(ControlFlowGraph cfg, Region region) {
         LOGGER.trace("Check sequence region {}", region);
         if (region.getChildCount() != 2) {
             return false;
         }
         Iterator<Region> it = region.getChildren().iterator();
         Region child1 = it.next();
         Region child2 = it.next();
         Region firstRegion = null;
         Region secondRegion = null;
         if (child2.getEntry().equals(child1.getExit())
                 && Objects.equal(region.getExit(), child2.getExit())) {
             firstRegion = child1;
             secondRegion = child2;
         } else if (child1.getEntry().equals(child2.getExit())
                 && Objects.equal(region.getExit(), child1.getExit())) {
             firstRegion = child2;
             secondRegion = child1;
         }
         if (firstRegion != null && secondRegion != null) {
             LOGGER.debug("Found sequence region {}", region);
             region.setParentType(ParentType.SEQUENCE);
             firstRegion.setChildType(ChildType.FIRST);
             secondRegion.setChildType(ChildType.SECOND);
 
             // propagate to children
             if (!checkRegion(cfg, firstRegion)) {
                 throw new ABCDException("Cannot find type of first region "
                             + firstRegion);
             }
             if (!checkRegion(cfg, secondRegion)) {
                 throw new ABCDException("Cannot find type of second region "
                             + secondRegion);
             }
             return true;
         }
 
         return false;
     }
 
     private boolean checkWhileLoopRegion(ControlFlowGraph cfg, Region region) {
         LOGGER.trace("Check while loop region {}", region);
         // a loop region must have 2 incomings edges, the region entry edge
         // and the loop back edge
         if (cfg.getPredecessorCountOf(region.getEntry()) != 2) {
             return false;
         }
         // check that one of the 2 incoming edges is a back edge
         Edge backEdge = null;
         for (Edge e : cfg.getIncomingEdgesOf(region.getEntry())) {
             if (e.hasAttribute(EdgeAttribute.LOOP_BACK_EDGE)) {
                 backEdge = e;
             } else if (e.hasAttribute(EdgeAttribute.LOOP_BACK_EDGE)) {
                 backEdge = e;
             }
         }
         if (backEdge == null) {
             return false;
         }
         // check that the loop region also contains the tail block
         BasicBlock tailBlock = cfg.getEdgeSource(backEdge);
         if (!region.getBasicBlocks().contains(tailBlock)) {
             return false;
         }
 
         LOGGER.debug("Found while loop region {}", region);
         region.setParentType(ParentType.WHILE_LOOP);
 
         // build the body subgraph
         //   - remove exit edges and exit block
         //   - replace the exit block by the tail block
         //   - remove the back edge
         ControlFlowGraph subCfg = createSubCFG(cfg, region);
         subCfg.setName("Body subgraph of loop " + region);
         subCfg.removeBasicBlock(subCfg.getExitBlock());
         subCfg.setExitBlock(tailBlock);
         subCfg.removeEdge(backEdge);
 
         // build rpst from control flow subgraph
         RPST subRpst = checkGraph(subCfg);
 
         Region rootRegion = subRpst.getRootRegion();
        Region bodyRegion = rootRegion.getFirstChild();
        bodyRegion.setChildType(ChildType.LOOP_BODY);
         region.removeChildren();
        region.addChild(bodyRegion);
 
         return true;
     }
 
     private boolean checkDoWhileLoopRegion(ControlFlowGraph cfg, Region region) {
         LOGGER.trace("Check do while loop region {}", region);
         if (region.getChildCount() == 1) {
             if (region.getEntry().getType() == BasicBlockType.JUMP_IF) {
                 if (cfg.getSuccessorCountOf(region.getEntry()) == 2) {
                     Region bodyRegion = region.getEntryChild();
 
                     LOGGER.debug("Found do while loop region {}", region);
 
                     region.setParentType(ParentType.DO_WHILE_LOOP);
                     bodyRegion.setChildType(ChildType.LOOP_BODY);
 
                     // propagate to children
                     if (!checkRegion(cfg, bodyRegion)) {
                         throw new ABCDException("Cannot find type of body region "
                                 + bodyRegion);
                     }
                     return true;
                 }
             }
         }
         return false;
     }
 
     private boolean checkIfThenBreakRegion(ControlFlowGraph cfg, Region region) {
         LOGGER.trace("Check if then break region {}", region);
         if (region.getChildCount() == 1) {
             if (region.getEntry().getType() == BasicBlockType.JUMP_IF) {
                 if (cfg.getSuccessorCountOf(region.getEntry()) == 1) {
                     Region ifRegion = region.getEntryChild();
 
                     LOGGER.debug("Found if then break region {}", region);
                     region.setParentType(ParentType.IF_THEN_BREAK);
                    ifRegion.setChildType(ChildType.IF);
                    
                     // propagate to children
                     if (!checkRegion(cfg, ifRegion)) {
                         throw new ABCDException("Cannot find type of if region "
                                 + ifRegion);
                     }
                     return true;
                 }
             }
         }
         return false;
     }
 
     private boolean checkIfThenRegion(ControlFlowGraph cfg, Region region) {
         LOGGER.trace("Check if then region {}", region);
         if (region.getChildCount() == 2) {
             Region ifRegion = null;
             Region thenRegion = null;
             for (Region child : region.getChildren()) {
                 if (child.getEntry().equals(region.getEntry())) {
                     ifRegion = child;
                 } else {
                     thenRegion = child;
                 }
             }
             if (ifRegion != null && thenRegion != null) {
                 if (Objects.equal(region.getExit(), thenRegion.getExit())) {
                     Edge thenEdge = cfg.getEdge(ifRegion.getEntry(), thenRegion.getEntry());
                     Edge elseEdge = cfg.getEdge(ifRegion.getEntry(), region.getExit());
                     if (thenEdge != null && elseEdge != null) {
                         ParentType parentType = null;
                         if (Boolean.TRUE.equals(thenEdge.getValue())
                                 && Boolean.FALSE.equals(elseEdge.getValue())) {
                             parentType = ParentType.IF_THEN;
                         } else if (Boolean.FALSE.equals(thenEdge.getValue())
                                 && Boolean.TRUE.equals(elseEdge.getValue())) {
                             parentType = ParentType.IF_NOT_THEN;
                         }
                         if (parentType != null) {
                             LOGGER.debug("Found if then region {}", region);
                             region.setParentType(parentType);
                             ifRegion.setChildType(ChildType.IF);
                             thenRegion.setChildType(ChildType.THEN);
 
                             // propagate to children
                             if (!checkRegion(cfg, ifRegion)) {
                                 throw new ABCDException("Cannot find type of if region "
                                         + ifRegion);
                             }
                             if (!checkRegion(cfg, thenRegion)) {
                                 throw new ABCDException("Cannot find type of then region "
                                         + thenRegion);
                             }
                             return true;
                         }
                     }
                 }
             }
         }
         return false;
     }
 
     private boolean checkIfThenElseRegion(ControlFlowGraph cfg, Region region) {
         LOGGER.trace("Check if then else region {}", region);
         if (region.getChildCount() == 3) {
             Region ifRegion = null;
             Region thenOrElseRegion1 = null;
             Region thenOrElseRegion2 = null;
             for (Region child : region.getChildren()) {
                 if (child.getEntry().equals(region.getEntry())) {
                     ifRegion = child;
                 } else {
                     if (thenOrElseRegion1 == null) {
                         thenOrElseRegion1 = child;
                     } else {
                         thenOrElseRegion2 = child;
                     }
                 }
             }
             Region thenRegion = null;
             Region elseRegion = null;
             if (ifRegion != null && thenOrElseRegion1 != null
                     && thenOrElseRegion2 != null) {
                 BasicBlock ifBB = ifRegion.getEntry();
                 BasicBlock thenOrElseBB1 = thenOrElseRegion1.getEntry();
                 BasicBlock thenOrElseBB2 = thenOrElseRegion2.getEntry();
                 Edge thenOrElseEdge1 = cfg.getEdge(ifBB, thenOrElseBB1);
                 Edge thenOrElseEdge2 = cfg.getEdge(ifBB, thenOrElseBB2);
                 if (thenOrElseEdge1 != null && thenOrElseEdge2 != null) {
                     if (Boolean.TRUE.equals(thenOrElseEdge1.getValue())) {
                         thenRegion = thenOrElseRegion1;
                     } else if (Boolean.TRUE.equals(thenOrElseEdge2.getValue())) {
                         thenRegion = thenOrElseRegion2;
                     }
                     if (Boolean.FALSE.equals(thenOrElseEdge1.getValue())) {
                         elseRegion = thenOrElseRegion1;
                     } else if (Boolean.FALSE.equals(thenOrElseEdge2.getValue())) {
                         elseRegion = thenOrElseRegion2;
                     }
                 }
             }
             if (thenRegion != null && elseRegion != null
                     && thenRegion.getExit().equals(elseRegion.getExit())) {
                 LOGGER.debug("Found if then else region {}", region);
                 region.setParentType(ParentType.IF_THEN_ELSE);
                 ifRegion.setChildType(ChildType.IF);
                 thenRegion.setChildType(ChildType.THEN);
                 elseRegion.setChildType(ChildType.ELSE);
 
                 // propagate to children
                 if (!checkRegion(cfg, ifRegion)) {
                     throw new ABCDException("Cannot find type of if region "
                             + ifRegion);
                 }
                 if (!checkRegion(cfg, thenRegion)) {
                     throw new ABCDException("Cannot find type of then region "
                             + thenRegion);
                 }
                 if (!checkRegion(cfg, elseRegion)) {
                     throw new ABCDException("Cannot find type of else region "
                             + elseRegion);
                 }
                 return true;
             }
         }
         return false;
     }
 
     private boolean checkSwitchCaseRegion(ControlFlowGraph cfg, Region region) {
         LOGGER.trace("Check switch case region {}", region);
         if (region.getChildCount() < 2) {
             return false;
         }
         Region switchRegion = null;
         List<Region> caseRegions = new ArrayList<Region>();
         for (Region child : region.getChildren()) {
             if (child.getEntry().equals(region.getEntry())
                     && child.getExit().getType() == BasicBlockType.SWITCH) {
                 switchRegion = child;
             } else {
                 caseRegions.add(child);
             }
         }
         if (switchRegion == null) {
             return false;
         }
         for (Region caseRegion : caseRegions) {
             Edge incomingEdge
                     = cfg.getEdge(switchRegion.getExit(), caseRegion.getEntry());
             if (incomingEdge == null
                     || !(incomingEdge.getValue() instanceof CaseValues)) {
                 return false;
             }
             if (!caseRegion.getExit().equals(region.getExit())) {
                 return false;
             }
             caseRegion.setData(incomingEdge.getValue());
         }
         Edge emptyCaseEdge = cfg.getEdge(region.getEntry(), region.getExit());
         if (emptyCaseEdge != null
                 && emptyCaseEdge.getValue() instanceof CaseValues) {
             // store empty cases info in switch region
             switchRegion.setData(emptyCaseEdge.getValue());
         }
 
         LOGGER.debug("Found switch case region {}", region);
         region.setParentType(ParentType.SWITCH_CASE);
         switchRegion.setChildType(ChildType.SWITCH);
         for (Region caseRegion : caseRegions) {
             caseRegion.setChildType(ChildType.CASE);
         }
 
         // propagate to children
         if (!checkRegion(cfg, switchRegion)) {
             throw new ABCDException("Cannot find type of switch region "
                             + switchRegion);
         }
         for (Region caseRegion : caseRegions) {
             if (!checkRegion(cfg, caseRegion)) {
                 throw new ABCDException("Cannot find type of case region "
                             + caseRegion);
             }
         }
         return true;
     }
 
     private boolean checkTryCatchFinallyRegion(ControlFlowGraph cfg, Region region) {
         LOGGER.trace("Check try catch finally region {}", region);
         Set<Region> handlerRegions = new HashSet<Region>();
         Region finallyRegion = null;
         Set<BasicBlock> handlerEntries = new HashSet<BasicBlock>();
         for (Region child : region.getChildren()) {
             // the child region is an exception handler whether its exit is connected
             // to parent region exit and its entry basic block has attribute
             // EXCEPTION_HANDLER_ENTRY
             if (child.getExit().equals(region.getExit())
                     && child.getEntry().hasProperty(BasicBlockPropertyName.EXCEPTION_HANDLER_ENTRY)) {
                 handlerRegions.add(child);
                 handlerEntries.add(child.getEntry());
                 if (child.getEntry().hasProperty(BasicBlockPropertyName.FINALLY_ENTRY)) {
                     finallyRegion = child;
                 }
             }
         }
         if (handlerRegions.isEmpty()) {
             return false;
         }
         // search inlined finally region
         Set<Region> inlinedFinallyRegions = new HashSet<Region>();
         if (finallyRegion != null) {
             for (Region child : region.getChildren()) {
                 if (child.equals(finallyRegion)) {
                     continue;
                 }
                 if (child.getExit().equals(region.getExit())) {
                     if (child.deepEquals(finallyRegion)) {
                         inlinedFinallyRegions.add(child);
                     }
                 }
             }
         }
         // every other child region should be connected to handlers
         Set<Region> otherChildren = new HashSet<Region>(region.getChildren());
         otherChildren.removeAll(handlerRegions);
         otherChildren.removeAll(inlinedFinallyRegions);
         for (Region otherChild : otherChildren) {
             for (BasicBlock bb : otherChild.getBasicBlocks()) {
                 if (!cfg.getExceptionalSuccessorsOf(bb).containsAll(handlerEntries)) {
                     return false;
                 }
             }
         }
 
         // build control flow subgraph
         ControlFlowGraph subCfg = createSubCFG(cfg, region);
         subCfg.setName("Try subgraph of region " + region);
 
         // remove handlers basic blocks
         for (Region handlerRegion : handlerRegions) {
             for (BasicBlock bb : handlerRegion.getBasicBlocks()) {
                 subCfg.removeBasicBlock(bb);
             }
         }
 
         // build rpst from control flow subgraph
         RPST subRpst = checkGraph(subCfg);
 
         LOGGER.debug("Found try catch finally region {}", region);
 
         region.setParentType(ParentType.TRY_CATCH_FINALLY);
         region.removeChildren();
         Region rootRegion = subRpst.getRootRegion();
         Region tryRegion = rootRegion.getEntryChild();
         tryRegion.setChildType(ChildType.TRY);
         region.addChild(tryRegion);
         for (Region handlerRegion : handlerRegions) {
             if (finallyRegion != null && handlerRegion.equals(finallyRegion)) {
                 handlerRegion.setChildType(ChildType.FINALLY);
             } else {
                 handlerRegion.setChildType(ChildType.CATCH);
             }
             region.addChild(handlerRegion);
         }
         // insert inlined finally regions
         for (Region child : tryRegion.getSubRegions()) {
             for (Region inlinedFinallyRegion : inlinedFinallyRegions) {
                 if (child.getEntry().equals(inlinedFinallyRegion.getEntry())
                         && child.getExit().equals(inlinedFinallyRegion.getExit())) {
                     child.insertParent(new Region(child.getEntry(),
                                                   child.getExit(),
                                                   ParentType.INLINED_FINALLY));
                     break;
                 }
             }
         }
 
         // propagate to children
         if (!checkRegion(cfg, tryRegion)) {
             throw new ABCDException("Cannot find type of try region "
                             + tryRegion);
         }
         for (Region handlerRegion : handlerRegions) {
             if (!checkRegion(cfg, handlerRegion)) {
                 throw new ABCDException("Cannot find type of handler region "
                             + handlerRegion);
             }
         }
         return true;
     }
 
     private boolean checkRegion(ControlFlowGraph cfg, Region region) {
         if (region.getParentType() != ParentType.UNDEFINED) {
             return true;
         }
         if (!(checkIfThenElseRegion(cfg, region)
                 || checkIfThenRegion(cfg, region)
                 || checkSwitchCaseRegion(cfg, region)
                 || checkSequenceRegion(cfg, region)
                 || checkWhileLoopRegion(cfg, region)
                 || checkDoWhileLoopRegion(cfg, region)
                 || checkTrivialRegion(cfg, region)
                 || checkIfThenBreakRegion(cfg, region)
                 || checkTryCatchFinallyRegion(cfg, region))) {
             return false;
         } else {
             return true;
         }
     }
 
     private RPST checkRegions(ControlFlowGraph cfg) {
         // build refined program structure tree
         RPST rpst = new RPSTBuilder(cfg).build();
 
         Region root = rpst.getRootRegion();
         for (Region child : root.getChildren()) {
             if (!checkRegion(cfg, child)) {
                 throw new ABCDException("Cannot find type of top level region "
                         + child);
             }
         }
 
         return rpst;
     }
 
     private RPST checkGraph(ControlFlowGraph cfg) {
         LOGGER.debug("Check {}", cfg.getName());
 
         int currentLevel = level++;
 
         cfg.updateDominatorInfo();
 
         ControlFlowGraph cleanCfg = new ControlFlowGraph(cfg);
         List<BasicBlock> otherExits = cleanCfg.getOtherExits();
         cleanCfg.removeDanglingBlocks();
         cleanCfg.updateDominatorInfo();
         cleanCfg.updatePostDominatorInfo();
         cleanCfg.updateLoopInfo();
 
         if (otherExits.size() > 0) {
             LOGGER.debug("!!! Dangling branches detected, check region on modified subgraph");
         }
 
         RPST rpst = checkRegions(cleanCfg);
 
         if (otherExits.size() > 0) {
             for (BasicBlock otherExit : otherExits) {
                 LOGGER.trace("Search for {} successor", otherExit);
                 BasicBlock bb;
                 for (bb = otherExit;
                         bb != null && bb.getRegion() == null;
                         bb = cfg.getDominatorInfo().getImmediateDominatorOf(bb)) {
                     // empty
                 }
                 if (bb != null) {
                     Region region = bb.getRegion().getParent();
                     BasicBlock successor = region.getExit();
                     LOGGER.trace("Found successor for {} : {}", otherExit, successor);
                     cfg.addEdge(otherExit, successor).addAttribute(EdgeAttribute.FAKE_EDGE);
                 }
             }
 
             cfg.updateDominatorInfo();
             cfg.updatePostDominatorInfo();
             cfg.updateLoopInfo();
 
             LOGGER.debug("Re-check regions...");
 
             rpst = checkRegions(cfg);
         }
 
         writer.writeRPST(rpst, currentLevel);
 
         return rpst;
     }
 
     public RPST analyse() {
         level = 0;
 
         cfg0.removeCriticalEdges();
 
         ControlFlowGraph cfg = new ControlFlowGraph(cfg0);
         cfg.setName("Main graph");
 
         RPST rpst = checkGraph(cfg);
 
         return rpst;
     }
 }
