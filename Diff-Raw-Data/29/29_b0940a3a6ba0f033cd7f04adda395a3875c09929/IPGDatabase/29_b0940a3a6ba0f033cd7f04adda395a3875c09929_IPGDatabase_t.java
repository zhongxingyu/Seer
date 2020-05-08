 package adam.betts.calculations;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Stack;
 
 import adam.betts.edges.Edge;
 import adam.betts.edges.IPGEdge;
 import adam.betts.graphs.IpointGraph;
 import adam.betts.graphs.utils.LeastCommonAncestor;
 import adam.betts.outputs.UDrawGraph;
 import adam.betts.programs.Program;
 import adam.betts.programs.Subprogram;
 import adam.betts.tools.MainWCETAnalyser;
 import adam.betts.utilities.Debug;
 import adam.betts.utilities.Enums.IPGEdgeType;
 import adam.betts.utilities.Globals;
 import adam.betts.vertices.Ipoint;
 import adam.betts.vertices.Vertex;
 
 public class IPGDatabase extends Database
 {
 
    final int rootID = 1;
     final protected IpointGraph ipg;
     protected HashMap <Integer, Long> observedWCETs = new HashMap <Integer, Long>();
     protected HashMap <Integer, HashMap <Integer, Integer>> unitBounds = new HashMap <Integer, HashMap <Integer, Integer>>();
 
     public IPGDatabase (final Program program)
     {
         super(program);
 
         ipg = program.getInlinedIPG(adam.betts.utilities.Globals
                 .getInstrumentationProfile());
 
         TraceParser parser = new TraceParser(rootID, ipg);
         initialise(rootID, parser);
 
         Debug.verboseMessage("Parsing trace file " + Globals.getTraceFileName());
         parser.doParsing(false);
         UDrawGraph.makeUDrawFile(Globals.getInstrumentationProfile(), ipg,
                 "inlined");
 
         if (MainWCETAnalyser.Globals.doObservedWCET())
         {
             Debug.verboseMessage("Reparsing for Observed WCETs");
             parser.doParsing(true);
         }
     }
 
    protected long doWCET ()
    {
        IPETModelIPG ipet = new IPETModelIPG(this, ipg, rootID);
        return ipet.wcet;
    }

     public final int getBound (int subprogramID, int unitID)
     {
         return unitBounds.get(subprogramID).get(unitID);
     }
 
     public final long getObservedWCET (int subprogramID)
     {
         return observedWCETs.get(subprogramID);
     }
 
     private void initialise (int rootID, TraceParser parser)
     {
         final int subprogramID = rootID;
 
         final IpointGraph ipg = program.getInlinedIPG(Globals
                 .getInstrumentationProfile());
 
         unitWCETs.put(subprogramID, new HashMap <Integer, Long>());
         unitBounds.put(subprogramID, new HashMap <Integer, Integer>());
         parser.tempUnitBounds.put(subprogramID,
                 new HashMap <Integer, Integer>());
         parser.properDescendants.put(subprogramID,
                 new HashMap <Integer, HashSet <Integer>>());
         mets.put(subprogramID, (long) 0);
         observedWCETs.put(subprogramID, (long) 0);
         tests.put(subprogramID, (long) 0);
 
         for (Vertex v : ipg)
         {
             Iterator <Edge> succIt = v.successorIterator();
             while (succIt.hasNext())
             {
                 IPGEdge e = (IPGEdge) succIt.next();
                 int edgeID = e.getEdgeID();
                 unitWCETs.get(subprogramID).put(edgeID, (long) 0);
                 unitBounds.get(subprogramID).put(edgeID, 0);
                 parser.tempUnitBounds.get(subprogramID).put(edgeID, 0);
             }
         }
     }
 
     private class TraceParser
     {
 
         /*
          * Cached LCA data structures
          */
         private HashMap <Integer, LeastCommonAncestor> lcas = new HashMap <Integer, LeastCommonAncestor>();
 
         /*
          * Stacks needed during trace parsing
          */
         private Stack <Integer> callStack = new Stack <Integer>();
         private Stack <Ipoint> ipointStack = new Stack <Ipoint>();
         private Stack <Long> metStack = new Stack <Long>();
         private Stack <Long> observedWCETStack = new Stack <Long>();
 
         /*
          * To keep track of the subprogram and where we are in the trace
          */
         private final int rootID;
         private int subprogramID;
         private Subprogram subprogram;
         private IpointGraph ipg;
         private Ipoint source;
         private Ipoint destination;
         private IPGEdge transition;
         private int edgeID;
         boolean newRun = true;
 
         private int testCounter = 0;
 
         /*
          * To keep track of times
          */
         private long observedWCET = 0;
         private long T1 = 0;
         private long T2 = 0;
 
         protected HashMap <Integer, HashMap <Integer, Integer>> tempUnitBounds = new HashMap <Integer, HashMap <Integer, Integer>>();
         protected HashMap <Integer, HashMap <Integer, HashSet <Integer>>> properDescendants = new HashMap <Integer, HashMap <Integer, HashSet <Integer>>>();
 
         private TraceParser (int rootID, IpointGraph ipg)
         {
             this.rootID = rootID;
             this.ipg = ipg;
             subprogramID = rootID;
         }
 
         private void doParsing (boolean doObservedWCET)
         {
             try
             {
                 /*
                  * The automata walking begins from the IPG of the root
                  * subprogram
                  */
                 source = ipg.getVertex(ipg.getEntryID());
 
                 BufferedReader in = new BufferedReader(new FileReader(
                         Globals.getTraceFileName()));
                 String str;
                 while ((str = in.readLine()) != null)
                 {
                     if (!str.startsWith("//") && !str.startsWith("/*"))
                     {
                         final String[] lexemes = str.split("\\s+");
                         long ipointID;
 
                         if (lexemes[0].startsWith("0x"))
                         {
                             ipointID = Long.parseLong(lexemes[0].substring(2));
                         }
                         else
                         {
                             ipointID = Long.parseLong(lexemes[0]);
                         }
 
                         long timeStamp = Long.parseLong(lexemes[1]);
 
                         Debug.debugMessage(getClass(),
                                 "Ipoint = 0x" + Long.toHexString(ipointID)
                                         + ", t = " + timeStamp, 3);
 
                         if (source.hasTraceSuccessor(ipointID))
                         {
                             if (newRun)
                             {
                                 startNewRun(doObservedWCET, ipointID, timeStamp);
                             }
                             else
                             {
                                 continueRun(doObservedWCET, ipointID, timeStamp);
                             }
                         }
                         else
                         {
                             Debug.debugMessage(
                                     getClass(),
                                     "Unable to find successor of ipoint "
                                             + source.getVertexID()
                                             + " with ipoint ID 0x"
                                             + Long.toHexString(ipointID), 1);
 
                             if (ipg.hasIpointID(ipointID) == false)
                             {
                                 Ipoint newv = new Ipoint(ipg.getNextVertexID(),
                                         ipointID);
                                 ipg.addIpoint(newv);
                             }
 
                             Ipoint destination = ipg.getVertex(ipointID);
                             int newEdgeID = ipg.getNextEdgeID();
 
                             source.addSuccessor(destination.getVertexID(),
                                     destination.getIpointID(), newEdgeID,
                                     IPGEdgeType.TRACE_EDGE);
                             destination.addPredecessor(source.getVertexID(),
                                     newEdgeID, IPGEdgeType.TRACE_EDGE);
 
                             Debug.debugMessage(getClass(), "Adding edge "
                                     + source.getVertexID() + " => "
                                     + destination.getVertexID(), 1);
 
                             unitWCETs.get(subprogramID)
                                     .put(newEdgeID, (long) 0);
                             unitBounds.get(subprogramID).put(newEdgeID, 0);
                             tempUnitBounds.get(subprogramID).put(newEdgeID, 0);
 
                             continueRun(doObservedWCET, ipointID, timeStamp);
                         }
                     }
                     else
                     {
                         Debug.debugMessage(getClass(), str, 4);
                     }
                 }
             }
             catch (Exception e)
             {
                 Debug.errorMessage(getClass(), e.getMessage());
             }

            if (!doObservedWCET)
            {
                long wcet = doWCET();
                System.out.println("WCET at trace #" + testCounter + " = "
                        + wcet);
            }
         }
 
         private void startNewRun (boolean doObservedWCET, long ipointID,
                 long timeStamp)
         {
             newRun = false;
             transition = source.getTraceSuccessor(ipointID);
             source = ipg.getVertex(transition.getVertexID());
             testCounter++;
 
             if (!doObservedWCET)
             {
                 if (MainWCETAnalyser.Globals.doIncrementalWCET())
                 {
                     Debug.debugMessage(getClass(), "Doing incremental WCET", 1);
 
                     /*
                      * Only do an incremental WCET computation if we have
                      * processed at least one run
                      */
                    if (testCounter > 1)
                     {
                        long wcet = doWCET();
                        System.out.println("WCET at trace #" + testCounter
                                + " = " + wcet);
                     }
                 }
 
                 T1 = timeStamp;
                 metStack.push(T1);
                 tests.put(subprogramID, tests.get(subprogramID) + 1);
             }
             else
             {
                 observedWCET = 0;
             }
         }
 
         private void continueRun (boolean doObservedWCET, long ipointID,
                 long timeStamp)
         {
             transition = source.getTraceSuccessor(ipointID);
             destination = ipg.getVertex(transition.getVertexID());
             edgeID = transition.getEdgeID();
 
             Debug.debugMessage(getClass(), "Analysing " + source.getVertexID()
                     + " => " + destination.getVertexID(), 3);
 
             int boundSoFar = tempUnitBounds.get(subprogramID).get(edgeID);
             tempUnitBounds.get(subprogramID).put(edgeID, boundSoFar + 1);
 
             if (!doObservedWCET)
             {
                 T2 = timeStamp;
                 long transitionWCET = T2 - T1;
 
                 if (transitionWCET > unitWCETs.get(subprogramID).get(edgeID))
                 {
                     unitWCETs.get(subprogramID).put(edgeID, transitionWCET);
                 }
 
                 T1 = T2;
             }
             else
             {
                 observedWCET += unitWCETs.get(subprogramID).get(edgeID);
             }
 
             advanceTransition(doObservedWCET);
         }
 
         private void advanceTransition (boolean doObservedWCET)
         {
             if (destination.isInlinedEntry())
             {
                 final String destinationSubprogramName = destination
                         .getSubprogramName();
                 final int destinationSubprogramID = program
                         .getSubprogramID(destinationSubprogramName);
 
                 Debug.debugMessage(getClass(), "Calling "
                         + destinationSubprogramName, 3);
 
                 if (!doObservedWCET)
                 {
                     metStack.push(T2);
                     tests.put(destinationSubprogramID,
                             tests.get(destinationSubprogramID) + 1);
                 }
                 else
                 {
                     observedWCETStack.push(observedWCET);
                     observedWCET = 0;
                 }
 
                 callStack.push(subprogramID);
                 ipointStack.push(destination);
                 subprogramID = destinationSubprogramID;
                 subprogram = program.getSubprogram(subprogramID);
                 ipg = subprogram.getIPG(Globals.getInstrumentationProfile());
                 source = ipg.getVertex(ipg.getEntryID());
                 transition = source
                         .getTraceSuccessor(destination.getIpointID());
                 source = ipg.getVertex(transition.getVertexID());
             }
             else if (ipg.isMasterExit(destination.getVertexID()))
             {
                 if (!doObservedWCET)
                 {
                     long startTime = metStack.pop();
                     long met = T2 - startTime;
 
                     if (met > mets.get(subprogramID))
                     {
                         mets.put(subprogramID, met);
                     }
                 }
                 else
                 {
                     if (observedWCET > observedWCETs.get(subprogramID))
                     {
                         observedWCETs.put(subprogramID, observedWCET);
                     }
                 }
 
                 if (subprogramID == rootID)
                 {
                     Debug.debugMessage(getClass(), "New run detected", 4);
                     newRun = true;
                     source = ipg.getVertex(ipg.getEntryID());
                     commitTempBounds();
                 }
                 else
                 {
                     Debug.debugMessage(getClass(), "Returning from "
                             + program.getSubprogram(subprogramID)
                                     .getSubprogramName(), 3);
 
                     if (doObservedWCET)
                     {
                         long startTime = observedWCETStack.pop();
                         observedWCET += startTime;
                     }
 
                     subprogramID = callStack.pop();
                     subprogram = program.getSubprogram(subprogramID);
                     ipg = subprogram
                             .getIPG(Globals.getInstrumentationProfile());
                     source = ipointStack.pop();
                     transition = source.getTraceSuccessor(destination
                             .getIpointID());
                     source = ipg.getVertex(transition.getVertexID());
                 }
             }
             else
             {
                 source = destination;
             }
         }
 
         private void commitTempBounds ()
         {
             for (int subprogramID : tempUnitBounds.keySet())
             {
                 for (int edgeID : tempUnitBounds.get(subprogramID).keySet())
                 {
                     int boundOnThisRun = tempUnitBounds.get(subprogramID).get(
                             edgeID);
                     if (boundOnThisRun > unitBounds.get(subprogramID).get(
                             edgeID))
                     {
                         unitBounds.get(subprogramID)
                                 .put(edgeID, boundOnThisRun);
                         Debug.debugMessage(getClass(), "Edge " + edgeID
                                 + ", bound = " + boundOnThisRun, 4);
                     }
 
                     tempUnitBounds.get(subprogramID).put(edgeID, 0);
                 }
             }
         }
     }
 
 }
