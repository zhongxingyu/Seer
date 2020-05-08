 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.exercise;
 
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.math.AnchoredVector;
 import edu.gatech.statics.objects.Connector;
 import edu.gatech.statics.objects.ConstantObject;
 import edu.gatech.statics.objects.connectors.Connector2ForceMember2d;
 import edu.gatech.statics.util.Pair;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class SymbolManager {
 
     private List<Pair<Connector, AnchoredVector>> symbolicLoads = new ArrayList<Pair<Connector, AnchoredVector>>();
     private List<String> symbols = new ArrayList<String>();
     private List<ConstantObject> symbolicConstants = new ArrayList<ConstantObject>();
 
     public List<Pair<Connector, AnchoredVector>> getSymbolicLoads() {
         return Collections.unmodifiableList(symbolicLoads);
     }
 
     public List<ConstantObject> getSymbolicConstants() {
         return Collections.unmodifiableList(symbolicConstants);
     }
 
     @Override
     public String toString() {
         return "SymbolManager: {symbols=" + symbols + ", symbolicLoads=" + symbolicLoads + ", symbolicConstants=" + symbolicConstants + "}";
     }
 
     public List<String> getSymbols() {
         return Collections.unmodifiableList(symbols);
     }
 
     public void addSymbol(ConstantObject obj) {
         if (!symbols.contains(obj.getName())) {
             symbols.add(obj.getName());
             symbolicConstants.add(obj);
         }
     }
 
     public ConstantObject getConstant(String name) {
         for (ConstantObject constantObject : symbolicConstants) {
             if (constantObject.getName().equals(name)) {
                 return constantObject;
             }
         }
         return null;
     }
 
     public void addSymbol(AnchoredVector load, Connector connector) {
         if (!load.isSymbol()) {
             throw new UnsupportedOperationException(
                     "Cannot add a symbol to the symbol manager for the non-symbolic quantified object \"" + load + "\"");
         }
         String name = load.getSymbolName();
 
         // remove the old load
         if (symbols.contains(name)) {
             symbols.remove(name);
             Pair<Connector, AnchoredVector> old = null;
             for (Pair<Connector, AnchoredVector> testPair : symbolicLoads) {
                 AnchoredVector test = testPair.getRight();
                 if (test.getSymbolName().equals(name)) {
                     old = testPair;
                 }
             }
             symbolicLoads.remove(old);
         }
 
         StaticsApplication.logger.info("Adding to SymbolManager: " + load + " at connector: " + connector);
 
         // add the new load
         symbols.add(load.getSymbolName());
         //if (load instanceof AnchoredVector) {
         //symbolicLoads.add(((AnchoredVector) quantified).clone());
         // make a defensive copy
         symbolicLoads.add(new Pair<Connector, AnchoredVector>(connector, load));
     }
 
     public AnchoredVector getLoad(AnchoredVector load, Connector connector) {
 
         Connector other2FMConnector = null;
         if (connector != null && connector instanceof Connector2ForceMember2d) {
             other2FMConnector = ((Connector2ForceMember2d) connector).getOpposite();
         }
 
         for (Pair<Connector, AnchoredVector> pair : symbolicLoads) {
             if (pair.getLeft() == connector || (other2FMConnector != null && pair.getLeft() == other2FMConnector)) {
                 AnchoredVector test = pair.getRight();
 
                 if (test.getVectorValue().equals(load.getVectorValue())) {
                     return test.getUnmodifiableAnchoredVector();
                 } else if (test.getVectorValue().negate().equals(load.getVectorValue())) {
                     return test.getUnmodifiableAnchoredVector();
                 }
             }
         }
         return null;
     }
 
     public Pair<Connector, AnchoredVector> getLoadPair(String symbolName) {
         for (Pair<Connector, AnchoredVector> pair : symbolicLoads) {
             if (symbolName.equals(pair.getRight().getSymbolName())) {
                 return pair;
             }
         }
         return null;
     }
 //    /**
 //     * Add a symbol to the symbol manager for the quantified object.
 //     * Loads are recognized and kept track of for use later.
 //     * Loads are also *cloned* so that solving a AnchoredVector in an equation does not have
 //     * unexpected consequences.
 //     * @param quantified
 //     */
 //    public void addSymbol(AnchoredVector load) {
 //        if (load.isSymbol()) {
 //            String name = load.getSymbolName();
 //
 //            // remove the old load
 //            if (symbols.contains(name)) {
 //                symbols.remove(name);
 //                AnchoredVector old = null;
 //                for (AnchoredVector test : symbolicLoads) {
 //                    if (test.getSymbolName().equals(name)) {
 //                        old = test;
 //                    }
 //                }
 //                symbolicLoads.remove(old);
 //            }
 //
 //            StaticsApplication.logger.info("Adding to SymbolManager: " + load);
 //
 //            // add the new load
 //            symbols.add(load.getSymbolName());
 //            //if (load instanceof AnchoredVector) {
 //            //symbolicLoads.add(((AnchoredVector) quantified).clone());
 //            // make a defensive copy
 //            symbolicLoads.add(new AnchoredVector((AnchoredVector) load));
 //            //}
 //
 //        } else {
 //            throw new UnsupportedOperationException(
 //                    "Cannot add a symbol to the symbol manager for the non-symbolic quantified object \"" + load + "\"");
 //        }
 //    }
 //
 //    /**
 //     * This serves a similar function to getLoad, but it does not check for the case of
 //     * two force members.
 //     * @param load
 //     * @return
 //     */
 //    public List<AnchoredVector> getLoadDirect(AnchoredVector load) {
 //        // this method checks given loads that are pointing in both directions,
 //        // either opposite or same direction. However, this PREFERS loads pointing
 //        // in the same direction, so we run two checks. First in same direction, then opposite.
 //
 //        List<AnchoredVector> results = new ArrayList<AnchoredVector>();
 //
 //        if (symbolicLoads.contains(load)) {
 //            results.add(load);
 //            return results; // ??
 //            //return Collections.singletonList(load);
 //            //return load.getUnmodifiableAnchoredVector();
 //        }
 //
 ////        AnchoredVector maybe = null;
 //        for (AnchoredVector toCheck : symbolicLoads) {
 //            if (load.getAnchor() == toCheck.getAnchor() &&
 //                    load.getVectorValue().equals(toCheck.getVectorValue())) {
 //
 //                // if we have a match that has the same name, return it immediately
 //                if (load.isSymbol() && load.getSymbolName().equals(toCheck.getSymbolName())) {
 //                    //return toCheck.getUnmodifiableAnchoredVector();
 //                    results.add(0, toCheck.getUnmodifiableAnchoredVector());
 //                }
 //
 //                // otherwise, defer a return of an ordinary match.
 ////                maybe = toCheck.getUnmodifiableAnchoredVector();
 //                else
 //                results.add(toCheck.getUnmodifiableAnchoredVector());
 //            }
 //        }
 ////        if (maybe != null) {
 ////            return maybe;
 ////        }
 //
 //        for (AnchoredVector toCheck : symbolicLoads) {
 //            if (load.getAnchor() == toCheck.getAnchor() &&
 //                    load.getVectorValue().equals(toCheck.getVectorValue().negate())) {
 //                // return defensive copy
 ////                return toCheck.getUnmodifiableAnchoredVector();
 //                results.add(toCheck.getUnmodifiableAnchoredVector());
 //            }
 //        }
 //
 //        return results;
 //    }
 //
 //    /**
 //     * This attempts to find a stored symbolic AnchoredVector for the provided AnchoredVector.
 //     * The direction of the symbolic AnchoredVector should not be taken as meaningful, as its opposite may
 //     * be the one of interest depending on the circumstance.
 //     * This should return a single AnchoredVector if presented with either end of a two force member.
 //     * @param AnchoredVector
 //     * @return
 //     */
 //    public List<AnchoredVector> getLoad(AnchoredVector load) {
 //        //AnchoredVector directLoad = getLoadDirect(load);
 //        List<AnchoredVector> results = getLoadDirect(load);
 ////        if (directLoad != null) {
 ////            return directLoad;
 ////        }
 //
 //        // okay, here we try to check for the 2fm case
 //        // make a list of connectors present at that point
 //        // the reaction was not found at this AnchoredVector, so we want to try to find the opposite one.
 //        //List<Connector> connectors = Exercise.getExercise().getSelectDiagram().getConnectorsAtPoint(AnchoredVector.getAnchor());
 //        //List<Connector> connectors = Exercise.getExercise().getSchematic().getConnectorsAtPoint(AnchoredVector.getAnchor());
 //        //for (Connector connector : connectors) {
 //        for (SimulationObject obj : Exercise.getExercise().getSchematic().allObjects()) {
 //            if (obj instanceof Connector2ForceMember2d) {
 //                Connector2ForceMember2d connector2fm = (Connector2ForceMember2d) obj;
 //                if (connector2fm.getAnchor() != load.getAnchor()) {
 //                    continue;
 //                }
 //                if (connector2fm.getDirection().equals(load.getVectorValue()) || connector2fm.getDirection().equals(load.getVectorValue().negate())) {
 //
 //                    // this one lines up with the connector2fm
 //                    // perform the main check, but use the other anchor.
 //                    Connector2ForceMember2d opposite = connector2fm.getOpposite();
 //
 //                    if (opposite == null) {
 //                        continue;
 //                    }
 //
 //                    // perform the main check again
 //                    for (AnchoredVector toCheck : symbolicLoads) {
 //                        if (opposite.getAnchor() == toCheck.getAnchor() &&
 //                                (connector2fm.getDirection().equals(toCheck.getVectorValue()) ||
 //                                connector2fm.getDirection().equals(toCheck.getVectorValue().negate()))) {
 //                            //&&
 //                            //(load.getVectorValue().equals(toCheck.getVectorValue()) ||
 //                            //(load.getVectorValue().equals(toCheck.getVectorValue().negate())))) {
 //                            // return defensive copy
 //                            //return new AnchoredVector(toCheck);
 //
 //                            results.add(toCheck.getUnmodifiableAnchoredVector());
 //                            //return toCheck.getUnmodifiableAnchoredVector();
 //                        }
 //                    }
 //                }
 //            }
 //        }
 //
 //        return results;
 //        //return null;
 //    }
 //
 //    /**
 //     * Returns the symbolic quantity for the specified AnchoredVector. This method is used for verifying that
 //     * the AnchoredVector given is the same as one which has been stored in the symbol manager.
 //     * If it does match, then the symbol name is returned, otherwise the method returns null.
 //     * @param AnchoredVector
 //     * @return
 //     */
 //    public Quantity getSymbol(AnchoredVector AnchoredVector) {
 //        for (AnchoredVector toCheck : symbolicLoads) {
 //            if (AnchoredVector.getAnchor() == toCheck.getAnchor() &&
 //                    (AnchoredVector.getVectorValue().equals(toCheck.getVectorValue()) ||
 //                    (AnchoredVector.getVectorValue().equals(toCheck.getVectorValue().negate())))) {
 //
 //                return toCheck.getVector().getQuantity();
 //            }
 //        }
 //        return null;
 //    }
 }
