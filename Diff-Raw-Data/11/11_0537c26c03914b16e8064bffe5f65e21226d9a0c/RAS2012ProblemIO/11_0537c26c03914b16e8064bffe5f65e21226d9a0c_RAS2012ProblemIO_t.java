 package org.drools.planner.examples.ras2012;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.drools.planner.benchmark.api.ProblemIO;
 import org.drools.planner.core.solution.Solution;
 import org.drools.planner.examples.ras2012.model.Arc;
 import org.drools.planner.examples.ras2012.model.Arc.TrackType;
 import org.drools.planner.examples.ras2012.model.MaintenanceWindow;
 import org.drools.planner.examples.ras2012.model.Network;
 import org.drools.planner.examples.ras2012.model.Node;
 import org.drools.planner.examples.ras2012.model.ScheduleAdherenceRequirement;
 import org.drools.planner.examples.ras2012.model.Train;
 import org.drools.planner.examples.ras2012.parser.DataSetParser;
 import org.drools.planner.examples.ras2012.parser.DataSetParser.ParsedTrain;
 import org.drools.planner.examples.ras2012.parser.ParseException;
 import org.drools.planner.examples.ras2012.parser.Token;
 
 public class RAS2012ProblemIO implements ProblemIO {
 
     private static TrackType getArcType(final Token t) {
         final String value = RAS2012ProblemIO.tokenToString(t);
         switch (value) {
             case "0":
                 return TrackType.MAIN_0;
             case "1":
                 return TrackType.MAIN_1;
             case "2":
                 return TrackType.MAIN_2;
             case "SW":
                 return TrackType.SWITCH;
             case "S":
                 return TrackType.SIDING;
             case "C":
                 return TrackType.CROSSOVER;
             default:
                 throw new IllegalArgumentException("Invalid value for track type: " + value);
         }
     }
 
     private static BigDecimal tokenToBigDecimal(final Token t) {
         return new BigDecimal(RAS2012ProblemIO.tokenToString(t));
     }
 
     private static boolean tokenToBoolean(final Token t) {
         return Boolean.valueOf(RAS2012ProblemIO.tokenToString(t));
     }
 
     private static Integer tokenToInteger(final Token t) {
         return Integer.valueOf(RAS2012ProblemIO.tokenToString(t));
     }
 
     private static String tokenToString(final Token t) {
         return t.toString();
     }
 
     private Map<Integer, Node> nodes;
 
     private RAS2012Solution createSolution(final DataSetParser p) {
         // retrieve speeds for different track types
         final int eastboundSpeed = RAS2012ProblemIO.tokenToInteger(p.getSpeedEastbound());
         final int westboundSpeed = RAS2012ProblemIO.tokenToInteger(p.getSpeedWestbound());
         final int sidingsSpeed = RAS2012ProblemIO.tokenToInteger(p.getSpeedSidings());
         final int crossoverSpeed = RAS2012ProblemIO.tokenToInteger(p.getSpeedCrossovers());
         // set speeds for different track types
         for (final TrackType t : TrackType.values()) {
             if (t.isMainTrack()) {
                 TrackType.setSpeed(t, eastboundSpeed, westboundSpeed);
             } else if (t == TrackType.SIDING) {
                 TrackType.setSpeed(t, sidingsSpeed);
             } else {
                 TrackType.setSpeed(t, crossoverSpeed);
             }
         }
         final String name = RAS2012ProblemIO.tokenToString(p.getName());
         final Collection<Arc> arcs = this.initArcs(p);
         final Collection<MaintenanceWindow> mows = this.initMOW(p);
         final Collection<Train> trains = this.initTrains(p);
         return new RAS2012Solution(name, new Network(this.nodes.values(), arcs), mows, trains);
     }
 
     @Override
     public String getFileExtension() {
        return "xml";
     }
 
     private Collection<Arc> initArcs(final DataSetParser p) {
         final List<Token> trackTypes = p.getTracks();
         final List<List<Token>> arcs = p.getArcs();
         final List<Token> trackLengths = p.getLengths();
         // first validate that there is proper amount of data in all structures
         final int numberOfItems = arcs.size();
         if (trackLengths.size() != numberOfItems) {
             throw new IllegalStateException("Arc lengths do not correspond to their number.");
         }
         if (trackTypes.size() != numberOfItems) {
             throw new IllegalStateException("Arc types do not correspond to their number.");
         }
         // now start processing
         final List<Arc> newArcs = new ArrayList<Arc>();
         final Map<Integer, Node> newNodes = new TreeMap<Integer, Node>();
         for (int i = 0; i < numberOfItems; i++) {
             final TrackType t = RAS2012ProblemIO.getArcType(trackTypes.get(i));
             final BigDecimal length = RAS2012ProblemIO.tokenToBigDecimal(trackLengths.get(i));
             // now convert node numbers to Node instances
             final int startNodeId = RAS2012ProblemIO.tokenToInteger(arcs.get(i).get(0));
             final int endNodeId = RAS2012ProblemIO.tokenToInteger(arcs.get(i).get(1));
             if (!newNodes.containsKey(startNodeId)) {
                 newNodes.put(startNodeId, new Node(startNodeId));
             }
             if (!newNodes.containsKey(endNodeId)) {
                 newNodes.put(endNodeId, new Node(endNodeId));
             }
             // and finally create the arc
             final Arc arc = new Arc(t, length, newNodes.get(startNodeId), newNodes.get(endNodeId));
             newArcs.add(arc);
         }
         // store the nodes for future reference
         this.nodes = Collections.unmodifiableMap(newNodes);
         return newArcs;
     }
 
     private Collection<MaintenanceWindow> initMOW(final DataSetParser p) {
         final List<MaintenanceWindow> mows = new ArrayList<MaintenanceWindow>();
         for (final List<Token> mow : p.getMows()) {
             final MaintenanceWindow newMow = new MaintenanceWindow(this.nodes.get(RAS2012ProblemIO
                     .tokenToInteger(mow.get(0))), this.nodes.get(RAS2012ProblemIO
                     .tokenToInteger(mow.get(1))), RAS2012ProblemIO.tokenToInteger(mow.get(2)),
                     RAS2012ProblemIO.tokenToInteger(mow.get(3)));
             mows.add(newMow);
         }
         return mows;
     }
 
     private Train initTrain(final ParsedTrain t) {
         final boolean hazmat = RAS2012ProblemIO.tokenToBoolean(t.getHazmat());
         final boolean isWestbound = RAS2012ProblemIO.tokenToString(t.getDirection()).equals(
                 "WESTBOUND");
         final int originalScheduleAdherence = RAS2012ProblemIO.tokenToInteger(t.getSaStatus());
         final int entryTime = RAS2012ProblemIO.tokenToInteger(t.getTimeEntry());
         final int wantTime = RAS2012ProblemIO.tokenToInteger(t.getWantTime().get(1));
         final int tob = RAS2012ProblemIO.tokenToInteger(t.getTOB());
         final String name = RAS2012ProblemIO.tokenToString(t.getHeader());
         final BigDecimal length = RAS2012ProblemIO.tokenToBigDecimal(t.getLength());
         final BigDecimal speedMultiplier = RAS2012ProblemIO.tokenToBigDecimal(t
                 .getSpeedMultiplier());
         final Node origin = this.nodes.get(RAS2012ProblemIO.tokenToInteger(t.getNodeOrigin()));
         final Node destination = this.nodes.get(RAS2012ProblemIO.tokenToInteger(t
                 .getNodeDestination()));
         // just checking; make sure that the direction and target depot match
         final String wantDepot = RAS2012ProblemIO.tokenToString(t.getWantTime().get(0));
         if (wantDepot.equals("WEST") && !isWestbound || wantDepot.equals("EAST") && isWestbound) {
             throw new IllegalStateException("Train is headed away from the target destination!");
         }
         // and now assemble schedules
         final List<ScheduleAdherenceRequirement> sars = new ArrayList<ScheduleAdherenceRequirement>();
         for (int i = 0; i < t.getSchedule().size(); i++) {
             final List<Token> data = t.getSchedule().get(i);
             final Node n = this.nodes.get(RAS2012ProblemIO.tokenToInteger(data.get(0)));
             final int time = RAS2012ProblemIO.tokenToInteger(data.get(1));
             final ScheduleAdherenceRequirement sar = new ScheduleAdherenceRequirement(n, time);
             sars.add(sar);
         }
         return new Train(name, length, speedMultiplier, tob, origin, destination, entryTime,
                 wantTime, originalScheduleAdherence, sars, hazmat, isWestbound);
     }
 
     private Collection<Train> initTrains(final DataSetParser p) {
         // first make sure there's as much trains as stated
         final List<ParsedTrain> origTrains = p.getTrains();
         if (!RAS2012ProblemIO.tokenToInteger(p.getNumTrains()).equals(origTrains.size())) {
             throw new IllegalStateException(
                     "Number of trains specified doesn't match the actual number of trains!");
         }
         // now parse each train individually
         final List<Train> trains = new ArrayList<Train>();
         for (final ParsedTrain t : origTrains) {
             trains.add(this.initTrain(t));
         }
         return trains;
     }
 
     @Override
     public RAS2012Solution read(final File inputSolutionFile) {
         try (InputStream is = new FileInputStream(inputSolutionFile)) {
             final DataSetParser p = new DataSetParser(is);
             p.parse();
            return this.createSolution(p);
         } catch (final FileNotFoundException e) {
             throw new IllegalArgumentException("Solution file doesn't exist: " + inputSolutionFile,
                     e);
         } catch (final IOException e) {
             throw new IllegalArgumentException("Problem reading solution file: "
                     + inputSolutionFile, e);
         } catch (final ParseException e) {
             throw new IllegalArgumentException("Problem parsing solution file: "
                     + inputSolutionFile, e);
         }
     }
 
     @Override
     public void write(@SuppressWarnings("rawtypes") final Solution solution,
             final File outputSolutionFile) {
         // FIXME implement this
     }
 
 }
