 package edu.mit.cci.turksnet;
 
 import com.sun.org.apache.xpath.internal.NodeSet;
 import edu.mit.cci.turksnet.util.HeadlessRunner;
 import edu.mit.cci.turkit.util.TurkitOutputSink;
 import edu.mit.cci.turkit.util.U;
 import edu.mit.cci.turkit.util.WireTap;
 import edu.mit.cci.turksnet.web.NodeForm;
 import org.apache.log4j.Logger;
 import org.apache.sling.commons.json.JSONException;
 import org.apache.sling.commons.json.JSONObject;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.tostring.RooToString;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.swing.*;
 import javax.xml.bind.annotation.XmlTransient;
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.*;
 
 @RooJavaBean
 @RooToString
 @RooEntity
 public class Session_ {
 
     @Transient
     private Logger log = Logger.getLogger(Session_.class);
 
     private String network;
 
     @Temporal(TemporalType.TIMESTAMP)
     @DateTimeFormat(style = "S-")
     private Date created;
 
     private Boolean active;
 
     @ManyToOne
     private Experiment experiment;
 
     private int iteration;
 
 
 
      @Transient
     @XmlTransient
     public List<String> test = Arrays.asList("one", "two", "three");
 
 
 
 
     @ManyToMany(cascade = CascadeType.ALL)
     private Set<Node> availableNodes = new HashSet<Node>();
 
     @Column(columnDefinition = "LONGTEXT")
     private String outputLog;
 
     @Column(columnDefinition = "LONGTEXT")
     private String properties;
 
     @Transient
     Map<String, String> propertiesAsMap = null;
 
     @Transient
     DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
 
     @Transient
     HeadlessRunner runner;
 
     private String qualificationRequirements;
 
     public Session_() {
     }
 
     public Session_(long experimentId) {
         setExperiment(Experiment.findExperiment(experimentId));
         setIteration(0);
         setActive(true);
 
     }
 
     public void addNode(Node n) {
         getAvailableNodes().add(n);
     }
 
     public Map<String, String> getPropertiesAsMap() {
         if (propertiesAsMap == null) {
             propertiesAsMap = new HashMap<String, String>();
             propertiesAsMap.putAll(U.splitProperties(getProperties()));
         }
         return Collections.unmodifiableMap(propertiesAsMap);
     }
 
     public void storePropertyMap() {
         StringBuffer buffer = new StringBuffer();
         for (Map.Entry<String, String> ent : getPropertiesAsMap().entrySet()) {
             buffer.append(ent.getKey()).append("=").append(ent.getValue()).append("\n");
         }
         setProperties(buffer.toString());
     }
 
     public void updateProperty(String property, Object value) {
         if (propertiesAsMap == null) {
             getPropertiesAsMap();
         }
         propertiesAsMap.put(property, value.toString());
         storePropertyMap();
         merge();
     }
 
     public List<Node>  getNodesAsList() {
         return new ArrayList<Node>(getAvailableNodes());
     }
 
 
 
 
     public void processNodeResults(String turkerId, Map<String,String> results) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
         Node n = findNodeForTurker(turkerId);
         log.debug("Am I active? "+getActive());
         if (n == null) {
             log.info("Could not identify node for turker " + turkerId);
             throw new IllegalArgumentException("Could not identify turker " + turkerId);
         } else {
             //logNodeEvent(n, "results");
             n.setAcceptingInput(false);
             n.persist();
             log.debug("Set node "+n.getId()+" to not accept");
             synchronized (getClass()) {
                 experiment.getActualPlugin().processResults(n, results);
                 logNodeEvent(n, "results");
                 boolean doneiteration = true;
                 for (Node node : getAvailableNodes()) {
                     log.debug("Making sure "+n.getId()+" is up to date");
                     node.merge();
                     log.debug("Checking node for doneness "+node.getId());
                     if (node.isAcceptingInput()) {
                         log.debug("Node is accepting input!");
                         doneiteration = false;
                     } else {
 
                     }   log.debug("Node is NOT accepting input!");
                 }
                 if (doneiteration) {
                     setIteration(getIteration() + 1);
 
                 }
                 log.debug("Checking for session doneness");
                 if (experiment.getActualPlugin().checkDone(Session_.this)) {
                     log.debug("I am DONE");
                     setActive(false);
 
 
                 }
 
             }
             persist();
 
         }
 
 
     }
 
     public void processNodeResults(String turkerId, String results) throws JSONException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        results = results.trim();
         if (results.startsWith("(")) {
             results = results.substring(1,results.length()-1);
         }
         System.err.println("Processing results: "+results);
 
         JSONObject obj = new JSONObject(results);
         Map<String,String> map = new HashMap<String, String>();
         for (Iterator<String> i = obj.keys();i.hasNext();) {
             String key = i.next();
           map.put(i.next(),obj.get(key).toString());
 
         }
        processNodeResults(turkerId,map);
 
     }
 
 
 
     public Node findNodeForTurker(String turkerId) {
         log.debug("Looking for node for "+turkerId);
         for (Node n : getAvailableNodes()) {
             log.debug("Looking at node "+n.getId()+" : "+n.getTurkerId()+" ?= "+turkerId);
             if (turkerId.equals(n.getTurkerId())) {
                 log.debug("Yup!");
                 return n;
             }
             log.debug("Nope :(");
         }
         return null;
     }
 
     public Node assignNodeToTurker(String turkerId) {
         for (Node n : getAvailableNodes()) {
             if (n.getTurkerId() == null) {
                 n.setTurkerId(turkerId);
                 n.persist();
                 logNodeEvent(n, "assigned");
                 return n;
             }
         }
         return null;
     }
 
     public Node getNodeForTurker(String turkerId) {
         Node n = findNodeForTurker(turkerId);
 
         if (n == null) {
             log.debug("Cannot find node for turker "+turkerId+" so will assign");
             n = assignNodeToTurker(turkerId);
             log.debug("Turker "+turkerId+" assigned to node "+n.getId()+":"+n.getTurkerId());
         }
         return n;
     }
 
      public Map<String,String> getBonus(String turkerid) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
         Node n =getNodeForTurker(turkerid);
         return getExperiment().getActualPlugin().getBonus(n);
     }
 
     private void logNodeEvent(Node n, String type) {
         SessionLog slog = new SessionLog();
         slog.setDate_(new Date());
         slog.setNode(n);
         slog.setSession_(slog.getSession_());
         slog.setType(type);
         slog.setNodePublicData(n.getPublicData_());
         slog.setNodePrivateData(n.getPrivateData_());
         slog.persist();
     }
 
     public void run() throws Exception {
         this.active = true;
         this.merge();
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 {
                     try {
                         runner = new HeadlessRunner(new BeanFieldSink());
                         runner.loadScript("Experiment:" + experiment.getId() + "_Session:" + getId(), experiment.getJavaScript(), experiment.getPropsAsMap(), Session_.this);
                         runner.run(true);
                     } catch (Exception e) {
                         updateLog(e.getMessage());
                     }
                 }
             }
         });
     }
 
     public String getHitCreationString(String baseurl) throws Exception {
         String result = null;
         try {
             result = this.experiment.getActualPlugin().getHitCreation(this, baseurl);
         } catch (Exception e) {
             throw new Exception(e);
         }
         return result;
     }
 
     private void updateLog(String update) {
         Session_ s = entityManager().find(Session_.class, Session_.this.getId());
         String e = s.getOutputLog() == null ? "" : getOutputLog();
         s.setOutputLog(e + stamp() + update);
         s.merge();
     }
 
     private String stamp() {
         return "\n -- " + format.format(new Date()) + " -- \n";
     }
 
     public class BeanFieldSink implements TurkitOutputSink {
 
         WireTap wireTap;
 
         @Override
         public void startCapture() {
             wireTap = new WireTap();
         }
 
         @Override
         public void stopCapture() {
             updateLog(wireTap.close());
             wireTap = null;
         }
 
         @Override
         public void setText(String text) {
             updateLog(text);
         }
     }
 }
