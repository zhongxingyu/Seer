 package org.drools.planner.examples.ras2012.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.concurrent.TimeUnit;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.TemplateException;
 import org.drools.planner.examples.ras2012.ProblemSolution;
 import org.drools.planner.examples.ras2012.ScoreCalculator;
 import org.drools.planner.examples.ras2012.model.Arc;
 import org.drools.planner.examples.ras2012.model.Itinerary;
 import org.drools.planner.examples.ras2012.model.ItineraryAssignment;
 import org.drools.planner.examples.ras2012.model.MaintenanceWindow;
 import org.drools.planner.examples.ras2012.model.Node;
 import org.drools.planner.examples.ras2012.model.Route;
 import org.drools.planner.examples.ras2012.model.ScheduleAdherenceRequirement;
 import org.drools.planner.examples.ras2012.model.Track;
 import org.drools.planner.examples.ras2012.model.Train;
 import org.drools.planner.examples.ras2012.model.WaitTime;
 import org.drools.planner.examples.ras2012.parser.DataSetParser;
 import org.drools.planner.examples.ras2012.parser.DataSetParser.ParsedTrain;
 import org.drools.planner.examples.ras2012.parser.ParseException;
 import org.drools.planner.examples.ras2012.parser.Token;
 import org.drools.planner.examples.ras2012.util.model.Territory;
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.filter.ElementFilter;
 import org.jdom2.input.DOMBuilder;
 import org.jdom2.util.IteratorIterable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 
 public class SolutionIO {
 
     private final Configuration freemarker;
 
     private Map<Integer, Node>  nodes;
 
     private static final Logger logger = LoggerFactory.getLogger(SolutionIO.class);
 
     private static BigDecimal convertMillisToSeconds(final long time) {
         return BigDecimal
                 .valueOf(time)
                 .divide(BigDecimal.valueOf(1000), Converter.BIGDECIMAL_SCALE,
                         Converter.BIGDECIMAL_ROUNDING).setScale(3);
     }
 
     private static Track getArcType(final Token t) {
         final String value = SolutionIO.tokenToString(t);
         if (value.equals("0")) {
             return Track.MAIN_0;
         } else if (value.equals("1")) {
             return Track.MAIN_1;
         } else if (value.equals("2")) {
             return Track.MAIN_2;
         } else if (value.equals("SW")) {
             return Track.SWITCH;
         } else if (value.equals("S")) {
             return Track.SIDING;
         } else if (value.equals("C")) {
             return Track.CROSSOVER;
         } else {
             throw new IllegalArgumentException("Invalid value for track type: " + value);
         }
     }
 
     private static Arc locateArc(final ProblemSolution solution, final String arc) {
         final String arcId = arc.substring(1, arc.length() - 1); // convert "(A,B)" into "A,B"
         final String[] nodeIds = arcId.split("\\Q,\\E");
         if (nodeIds.length != 2) {
             throw new IllegalArgumentException("Invalid Arc id: " + arc);
         }
         // we need to browse all routes to look for the particular arc
         for (final Route r : solution.getTerritory().getAllRoutes()) {
             for (final Arc a : r.getProgression().getArcs()) {
                 final int leftNode = Integer.valueOf(nodeIds[0]);
                 final int rightNode = Integer.valueOf(nodeIds[1]);
                 final boolean matches = a.getOrigin(r).getId() == leftNode
                         && a.getDestination(r).getId() == rightNode;
                 final boolean reverseMatches = a.getOrigin(r).getId() == rightNode
                         && a.getDestination(r).getId() == leftNode;
                 if (matches || reverseMatches) {
                     return a;
                 }
             }
         }
         throw new IllegalArgumentException("Arc not found: " + arc);
     }
 
     private static Train locateTrain(final ProblemSolution solution, final String trainId) {
         for (final Train t : solution.getTrains()) {
             if (t.getName().equals(trainId)) {
                 return t;
             }
         }
         return null;
     }
 
     private static Document parseXml(final File result) throws SAXException, IOException,
             ParserConfigurationException {
         final String content = SolutionIO.readFile(result);
         return SolutionIO.parseXml(content);
     }
 
     private static Document parseXml(final InputStream result) throws IOException,
             ParserConfigurationException, SAXException {
         final String content = SolutionIO.readStream(result);
         return SolutionIO.parseXml(content);
     }
 
     private static Document parseXml(String content) throws IOException,
             ParserConfigurationException, SAXException {
         if (content.length() == 0) {
             throw new IOException("There was a problem reading the XML file.");
         }
         final int documentStart = content.indexOf('<');
         final int documentEnd = content.lastIndexOf('>');
         content = content.substring(documentStart, documentEnd + 1);
         // prepare DOM
         final DocumentBuilderFactory domfactory = DocumentBuilderFactory.newInstance();
         domfactory.setNamespaceAware(false);
         final DocumentBuilder dombuilder = domfactory.newDocumentBuilder();
         final org.w3c.dom.Document doc = dombuilder.parse(new ByteArrayInputStream(content
                 .getBytes("UTF-8")));
         return new DOMBuilder().build(doc);
     }
 
     private static void processXmlMovements(final ProblemSolution solution, final Train train,
             final IteratorIterable<Element> movements) {
         // first locate the route we are on
         final Collection<Route> availableRoutes = solution.getTerritory().getRoutes(train);
         final Collection<Route> routes = new HashSet<Route>(availableRoutes);
         final Iterator<Element> it = movements.iterator();
         Arc lastArc = null;
         long lastTravellingTime = Long.MAX_VALUE;
         while (it.hasNext()) {
             final Element e = it.next();
             if (e.getAttribute("arc") == null) {
                 lastArc = null;
                 lastTravellingTime = Long.MAX_VALUE;
                 continue;
             }
             lastArc = SolutionIO.locateArc(solution, e.getAttribute("arc").getValue());
             lastTravellingTime = new BigDecimal(e.getAttributeValue("exit")).multiply(
                     BigDecimal.valueOf(1000)).longValue()
                     - new BigDecimal(e.getAttributeValue("entry")).multiply(
                             BigDecimal.valueOf(1000)).longValue();
             for (final Route r : availableRoutes) {
                 if (!r.getProgression().contains(lastArc)) {
                     routes.remove(r);
                 }
             }
         }
         if (routes.size() == 0) {
             throw new IllegalStateException("No route found for train: " + train.getName());
         }
         Route properRoute = null;
         if (lastArc == null
                 || Converter.getTimeFromSpeedAndDistance(train.getMaximumSpeed(lastArc.getTrack()),
                         lastArc.getLength()) >= lastTravellingTime) {
             properRoute = routes.iterator().next();
         } else {
             /*
              * the proper route not ending in a terminal but still must have the last node as a wait point. but this rule
              * applies only in cases where the delay that took the exit out of the horizon did actually happen at the end of the
              * arc. that's what the time comparions above are for.
              */
             for (final Route r : routes) {
                 if (r.getProgression().getWaitPoints().contains(lastArc.getDestination(train))) {
                     properRoute = r;
                     break;
                 }
             }
         }
         if (properRoute == null) {
             throw new IllegalStateException("No proper route found for train: " + train.getName());
         }
         // now create the proper itinerary
         final ItineraryAssignment ia = solution.getAssignment(train);
         ia.setRoute(properRoute);
         final Itinerary i = ia.getItinerary();
         // and then set the wait times
         int movementCount = 0;
         boolean reachedDestination = false;
         for (final Element e : movements) {
             final boolean isMovement = e.getName().equals("movement");
             final boolean isDestination = e.getName().equals("destination");
             reachedDestination = reachedDestination || isDestination;
            if (isMovement || isDestination) {
                 movementCount++;
                 final long time = new BigDecimal(e.getAttributeValue("entry")).multiply(
                         BigDecimal.valueOf(1000)).longValue();
                final Node n = isDestination ? train.getDestination() : SolutionIO.locateArc(
                        solution, e.getAttribute("arc").getValue()).getOrigin(train);
                 final long actualTime = i.getArrivalTime(n);
                 final long delay = time - actualTime;
                 if (delay != 0) {
                     i.setWaitTime(n, WaitTime.getWaitTime(delay, TimeUnit.MILLISECONDS));
                 }
             } else {
                 throw new IllegalStateException("Train " + train.getName()
                         + " has illegal element: " + e);
             }
         }
         if (movementCount == 0) {
             /* we need so big delay that the train completely falls out of the planning horizon. */
             i.setWaitTime(train.getOrigin(), WaitTime.getWaitTime(
                     solution.getPlanningHorizon(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS));
         } else if (!reachedDestination) {
             /*
              * train needs to get so big a delay at the first node out the horizon so that every other node falls right out too.
              * but only when the exit time actually falls inside the planning horizon.
              */
             Element e = null;
             final Iterator<Element> it2 = movements.iterator();
             while (it2.hasNext()) {
                 e = it2.next();
             }
             final Node n = SolutionIO.locateArc(solution, e.getAttribute("arc").getValue())
                     .getDestination(train);
             final long time = new BigDecimal(e.getAttributeValue("exit")).multiply(
                     BigDecimal.valueOf(1000)).longValue();
             final long delay = time - i.getArrivalTime(n);
             if (delay != 0) {
                 i.setWaitTime(n, WaitTime.getWaitTime(delay, TimeUnit.MILLISECONDS));
             }
         }
         i.resetLatestWaitTimeChange();
     }
 
     private static void processXmlTrains(final Document doc, final ProblemSolution solution) {
         final IteratorIterable<Element> trains = doc.getDescendants(new ElementFilter("train"));
         for (final Element e : trains) {
             final String trainId = e.getAttribute("id").getValue();
             final Train t = SolutionIO.locateTrain(solution, trainId);
             if (t == null) {
                 throw new IllegalStateException("Train not in solution: " + t);
             }
             SolutionIO.processXmlMovements(solution, t,
                     e.getChild("movements").getDescendants(new ElementFilter()));
         }
     }
 
     private static String readFile(final File file) {
         try {
             return new Scanner(new FileInputStream(file)).useDelimiter("\\A").next();
         } catch (final FileNotFoundException e) {
             return "";
         }
     }
 
     private static String readStream(final InputStream is) {
         return new Scanner(is).useDelimiter("\\A").next();
     }
 
     private static BigDecimal tokenToBigDecimal(final Token t) {
         return new BigDecimal(SolutionIO.tokenToString(t));
     }
 
     private static boolean tokenToBoolean(final Token t) {
         return Boolean.valueOf(SolutionIO.tokenToString(t));
     }
 
     private static Integer tokenToInteger(final Token t) {
         return Integer.valueOf(SolutionIO.tokenToString(t));
     }
 
     private static String tokenToString(final Token t) {
         return t.toString();
     }
 
     public SolutionIO() {
         this.freemarker = new Configuration();
         this.freemarker.setClassForTemplateLoading(SolutionIO.class, "");
         this.freemarker.setObjectWrapper(new DefaultObjectWrapper());
         this.freemarker.setLocale(Locale.US);
         this.freemarker.setNumberFormat("computer");
     }
 
     private ProblemSolution createSolution(final DataSetParser p) {
         // retrieve speeds for different track types
         final int eastboundSpeed = SolutionIO.tokenToInteger(p.getSpeedEastbound());
         final int westboundSpeed = SolutionIO.tokenToInteger(p.getSpeedWestbound());
         final int sidingsSpeed = SolutionIO.tokenToInteger(p.getSpeedSidings());
         final int crossoverSpeed = SolutionIO.tokenToInteger(p.getSpeedCrossovers());
         // set speeds for different track types
         for (final Track t : Track.values()) {
             if (t.isMainTrack()) {
                 Track.setSpeed(t, eastboundSpeed, westboundSpeed);
             } else if (t == Track.SIDING) {
                 Track.setSpeed(t, sidingsSpeed);
             } else {
                 Track.setSpeed(t, crossoverSpeed);
             }
         }
         final String name = SolutionIO.tokenToString(p.getName());
         final Collection<Arc> arcs = this.initArcs(p);
         final Collection<MaintenanceWindow> mows = this.initMOW(p);
         final Collection<Train> trains = this.initTrains(name, p);
         return new ProblemSolution(name, new Territory(this.nodes.values(), arcs), mows, trains);
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
             final Track t = SolutionIO.getArcType(trackTypes.get(i));
             final BigDecimal length = SolutionIO.tokenToBigDecimal(trackLengths.get(i));
             // now convert node numbers to Node instances
             final int startNodeId = SolutionIO.tokenToInteger(arcs.get(i).get(0));
             final int endNodeId = SolutionIO.tokenToInteger(arcs.get(i).get(1));
             if (!newNodes.containsKey(startNodeId)) {
                 newNodes.put(startNodeId, Node.getNode(startNodeId));
             }
             if (!newNodes.containsKey(endNodeId)) {
                 newNodes.put(endNodeId, Node.getNode(endNodeId));
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
             final MaintenanceWindow newMow = new MaintenanceWindow(this.nodes.get(SolutionIO
                     .tokenToInteger(mow.get(0))), this.nodes.get(SolutionIO.tokenToInteger(mow
                     .get(1))), SolutionIO.tokenToInteger(mow.get(2)), SolutionIO.tokenToInteger(mow
                     .get(3)));
             mows.add(newMow);
         }
         return mows;
     }
 
     private Train initTrain(final String solutionName, final ParsedTrain t) {
         final boolean hazmat = SolutionIO.tokenToBoolean(t.getHazmat());
         final boolean isWestbound = SolutionIO.tokenToString(t.getDirection()).equals("WESTBOUND");
         final int originalScheduleAdherence = SolutionIO.tokenToInteger(t.getSaStatus());
         final int entryTime = SolutionIO.tokenToInteger(t.getTimeEntry());
         final int wantTime = SolutionIO.tokenToInteger(t.getWantTime().get(1));
         final int tob = SolutionIO.tokenToInteger(t.getTOB());
         final String name = SolutionIO.tokenToString(t.getHeader());
         final BigDecimal length = SolutionIO.tokenToBigDecimal(t.getLength());
         final BigDecimal speedMultiplier = SolutionIO.tokenToBigDecimal(t.getSpeedMultiplier());
         final Node origin = this.nodes.get(SolutionIO.tokenToInteger(t.getNodeOrigin()));
         final Node destination = this.nodes.get(SolutionIO.tokenToInteger(t.getNodeDestination()));
         // just checking; make sure that the direction and target depot match
         final String wantDepot = SolutionIO.tokenToString(t.getWantTime().get(0));
         if (wantDepot.equals("WEST") && !isWestbound || wantDepot.equals("EAST") && isWestbound) {
             SolutionIO.logger.info("Train " + name
                     + " is headed away from the target destination. This bug in " + solutionName
                     + " will be corrected by directing the train to the proper destination.");
         }
         // and now assemble schedules
         final List<ScheduleAdherenceRequirement> sars = new ArrayList<ScheduleAdherenceRequirement>();
         for (int i = 0; i < t.getSchedule().size(); i++) {
             final List<Token> data = t.getSchedule().get(i);
             final Node n = this.nodes.get(SolutionIO.tokenToInteger(data.get(0)));
             final int time = SolutionIO.tokenToInteger(data.get(1));
             final ScheduleAdherenceRequirement sar = new ScheduleAdherenceRequirement(n, time);
             sars.add(sar);
         }
         return new Train(name, length, speedMultiplier, tob, origin, destination, entryTime,
                 wantTime, originalScheduleAdherence, sars, hazmat, isWestbound);
     }
 
     private Collection<Train> initTrains(final String solutionName, final DataSetParser p) {
         // first make sure there's as much trains as stated
         final List<ParsedTrain> origTrains = p.getTrains();
         if (!SolutionIO.tokenToInteger(p.getNumTrains()).equals(origTrains.size())) {
             throw new IllegalStateException(
                     "Number of trains specified doesn't match the actual number of trains!");
         }
         // now parse each train individually
         final List<Train> trains = new ArrayList<Train>();
         for (final ParsedTrain t : origTrains) {
             trains.add(this.initTrain(solutionName, t));
         }
         return trains;
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private Map prepareTexData(final ProblemSolution solution) {
         final ScoreCalculator calc = new ScoreCalculator();
         calc.resetWorkingSolution(solution);
         final Map map = new HashMap();
         map.put("name", solution.getName());
         final Set trainsMap = new LinkedHashSet();
         for (final Train t : solution.getTrains()) {
             trainsMap.add(this.prepareTexTrain(solution.getAssignment(t).getItinerary(), solution,
                     calc));
         }
         map.put("trains", trainsMap);
         map.put("cost", -calc.calculateScore().getSoftScore());
         return map;
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private Map prepareTexTrain(final Itinerary itinerary, final ProblemSolution solution,
             final ScoreCalculator calculator) {
         final Train train = itinerary.getTrain();
         final Map map = new HashMap();
         map.put("name", train.getName());
         map.put("delay", SolutionIO.convertMillisToSeconds(itinerary.getDelay(solution
                 .getPlanningHorizon(TimeUnit.MILLISECONDS))));
         map.put("unpreferredPenalty", calculator.getUnpreferredTracksPenalty(itinerary));
         map.put("stops", this.prepareTexTrainStops(itinerary, solution, calculator));
         map.put("numStops", ((Collection) map.get("stops")).size());
         final long horizon = solution.getPlanningHorizon(TimeUnit.MILLISECONDS);
         final long arrival = itinerary.getArrivalTime();
         final boolean isInHorizon = arrival <= horizon;
         final long wantTime = train.getWantTime(TimeUnit.MILLISECONDS);
         map.put("twt", SolutionIO.convertMillisToSeconds(wantTime));
         map.put("twtDiff", isInHorizon ? SolutionIO.convertMillisToSeconds(wantTime - arrival)
                 : null);
         map.put("twtPenalty", isInHorizon ? calculator.getWantTimePenalty(itinerary) : "");
         return map;
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private Map prepareTexTrainStop(final Itinerary itinerary, final Node n,
             final ProblemSolution solution, final ScoreCalculator calculator) {
         final Train t = itinerary.getTrain();
         final long horizon = solution.getPlanningHorizon(TimeUnit.MILLISECONDS);
         final long arrival = itinerary.getArrivalTime(n);
         final boolean isInHorizon = arrival <= horizon;
         final Map stop = new HashMap();
         stop.put("node", n.getId());
         stop.put("arrive", SolutionIO.convertMillisToSeconds(arrival).stripTrailingZeros());
         if (t.getScheduleAdherenceRequirements().containsKey(n)) {
             final long wantTime = t.getScheduleAdherenceRequirements().get(n)
                     .getTimeSinceStartOfWorld(TimeUnit.MILLISECONDS);
             stop.put("sa", SolutionIO.convertMillisToSeconds(wantTime));
             stop.put("saDiff", isInHorizon ? SolutionIO.convertMillisToSeconds(wantTime - arrival)
                     : null);
             stop.put("saPenalty",
                     isInHorizon ? calculator.getScheduleAdherencePenalty(itinerary, n) : "");
         }
         return stop;
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private Collection prepareTexTrainStops(final Itinerary itinerary,
             final ProblemSolution solution, final ScoreCalculator calculator) {
         final Train train = itinerary.getTrain();
         // prepare the set of stops, make sure they are in a proper order
         final List<Node> nodes = new ArrayList<Node>();
         final Map<Node, ScheduleAdherenceRequirement> sa = train.getScheduleAdherenceRequirements();
         for (final Node n : itinerary.getRoute().getProgression().getNodes()) {
             if (sa.containsKey(n)) {
                 nodes.add(n);
             }
         }
         // and now populate the stop data
         final List stops = new ArrayList();
         for (final Node node : nodes) {
             final Map stop = this.prepareTexTrainStop(itinerary, node, solution, calculator);
             stops.add(stop);
         }
         return stops;
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private Set prepareXmlData(final ProblemSolution solution) throws IOException {
         final Set set = new LinkedHashSet();
         for (final Train t : solution.getTrains()) {
             final Map map = new HashMap();
             map.put("name", t.getName());
             map.put("movements", this.prepareXmlMovements(t, solution));
             final Itinerary i = solution.getAssignment(t).getItinerary();
             if (i.getArrivalTime() <= solution.getPlanningHorizon(TimeUnit.MILLISECONDS)) {
                 final BigDecimal timeInSeconds = SolutionIO.convertMillisToSeconds(i
                         .getArrivalTime(t.getDestination()));
                 map.put("destinationEntry", timeInSeconds.stripTrailingZeros());
             }
             set.add(map);
         }
         return set;
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private Set prepareXmlMovements(final Train t, final ProblemSolution solution) {
         final Set set = new LinkedHashSet();
         final Itinerary i = solution.getAssignment(t).getItinerary();
         final long horizon = solution.getPlanningHorizon(TimeUnit.MILLISECONDS);
         for (final SortedMap.Entry<Long, Arc> entry : i.getScheduleWithArcs().entrySet()) {
             final Map map = new HashMap();
             final Arc arc = entry.getValue();
             if (arc.getDestination(t) == t.getDestination()) {
                 // this is the move into the destination; ignore here, handle elsewhere
                 break;
             }
             if (entry.getKey() >= horizon) {
                 continue;
             }
             final BigDecimal timeInSeconds = SolutionIO.convertMillisToSeconds(
                     i.getArrivalTime(arc)).stripTrailingZeros();
             final BigDecimal leaveTime = SolutionIO.convertMillisToSeconds(i.getLeaveTime(arc))
                     .stripTrailingZeros();
             if (leaveTime.intValue() > horizon) {
                 continue;
             }
             map.put("origin", arc.getOrigin(t).getId());
             map.put("destination", arc.getDestination(t).getId());
             map.put("entry", timeInSeconds);
             map.put("exit", leaveTime);
             set.add(map);
         }
         return set;
     }
 
     public ProblemSolution read(final File inputSolutionFile) {
         InputStream is = null;
         try {
             is = new FileInputStream(inputSolutionFile);
             return this.read(is);
         } catch (final FileNotFoundException e) {
             throw new IllegalArgumentException("Solution file doesn't exist: " + inputSolutionFile,
                     e);
         } catch (final ParseException e) {
             throw new IllegalArgumentException("Problem parsing solution file: "
                     + inputSolutionFile, e);
         } finally {
             if (is != null) {
                 try {
                     is.close();
                 } catch (final IOException e) {
                     // nothing to do here
                 }
             }
         }
     }
 
     public ProblemSolution read(final File inputSolutionFile, final File result) {
         final ProblemSolution solution = this.read(inputSolutionFile);
         try {
             SolutionIO.processXmlTrains(SolutionIO.parseXml(result), solution);
         } catch (final Exception e) {
             throw new IllegalStateException("Problem reading XML file.", e);
         }
         return solution;
     }
 
     public ProblemSolution read(final InputStream inputSolution) throws ParseException {
         final DataSetParser p = new DataSetParser(inputSolution);
         p.parse();
         return this.createSolution(p);
     }
 
     public ProblemSolution read(final InputStream inputSolution, final InputStream result) {
         try {
             final ProblemSolution solution = this.read(inputSolution);
             SolutionIO.processXmlTrains(SolutionIO.parseXml(result), solution);
             return solution;
         } catch (final Exception e) {
             throw new IllegalStateException("Problem reading XML file.", e);
         }
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public void writeTex(final ProblemSolution solution, final File outputSolutionFile) {
         try {
             final Map map = this.prepareTexData(solution);
             map.put("id", outputSolutionFile.getName());
             this.freemarker.getTemplate("schedule.tex.ftl").process(map,
                     new FileWriter(outputSolutionFile));
         } catch (final TemplateException e) {
             SolutionIO.logger.error("Failed processing LaTeX schedule template.", e);
         } catch (final IOException e) {
             SolutionIO.logger.error("Failed writing " + solution.getName() + " into "
                     + outputSolutionFile, e);
         }
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public void writeXML(final ProblemSolution solution, final File outputSolutionFile) {
         try {
             final Map map = new HashMap();
             map.put("trains", this.prepareXmlData(solution));
             map.put("name", solution.getName());
             this.freemarker.getTemplate("schedule.xml.ftl").process(map,
                     new FileWriter(outputSolutionFile));
         } catch (final TemplateException e) {
             SolutionIO.logger.error("Failed processing XML schedule template.", e);
         } catch (final IOException e) {
             SolutionIO.logger.error("Failed writing " + solution.getName() + " into "
                     + outputSolutionFile, e);
         }
     }
 }
