 package edu.mit.cci.turksnet;
 
 import edu.mit.cci.turkit.gui.HeadlessRunner;
 import edu.mit.cci.turkit.util.NamedSource;
 import edu.mit.cci.turkit.util.TurkitOutputSink;
 import edu.mit.cci.turkit.util.U;
 import edu.mit.cci.turkit.util.WireTap;
 import edu.mit.cci.turksnet.web.NodeForm;
 import flexjson.JSON;
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
 import javax.swing.text.html.HTMLDocument;
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
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
 
     @ManyToMany(cascade = CascadeType.ALL)
     private Set<Node> pendingNodes = new HashSet<Node>();
 
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
 
 
 
     public void processNodeResults(String turkerId, Map<String,String> results) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
         Node n = findNodeForTurker(turkerId);
         if (n == null) {
             log.info("Could not identify node for turker " + turkerId);
             throw new IllegalArgumentException("Could not identify turker " + turkerId);
         } else {
             //logNodeEvent(n, "results");
             n.setAcceptingInput(false);
             n.merge();
             synchronized (getClass()) {
                 experiment.getActualPlugin().processResults(n, results);
                 logNodeEvent(n, "results");
                 boolean doneiteration = true;
                 for (Node node : getAvailableNodes()) {
                     if (node.isAcceptingInput()) {
                         doneiteration = false;
                     }
                 }
                 if (doneiteration) {
                     setIteration(getIteration() + 1);
 
                 }
                 if (experiment.getActualPlugin().checkDone(Session_.this)) {
                     setActive(false);
                 }
 
             }
             persist();
 
         }
     }
 
     public void processNodeResults(String turkerId, String results) throws JSONException {
         results = results.trim();
        if (results.startsWith("(")) {
             results = results.substring(1,results.length()-1);
         }
         System.err.println("Processing results: "+results);
 
         JSONObject obj = new JSONObject(results);
         Map<String,String> result = new HashMap<String, String>();
         for (Iterator<String> i = obj.keys();i.hasNext();) {
             String key = i.next();
            result.put(i.next(),obj.get(key).toString());
 
         }
         processNodeResults(turkerId,results);
 
     }
 
     public void postBack(NodeForm form) {
 
         try {
             U.webPost(new URL(form.getSubmitTo()),"assignmentId",form.getAssignmentId(),"public",form.getPublicData(),"private",form.getPrivateData());
         } catch (Exception e) {
 
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             log.error("There was a problem posting form data back to amazon",e);
         }
 
 
     }
 
     public Node findNodeForTurker(String turkerId) {
         for (Node n : availableNodes) {
             if (turkerId.equals(n.getTurkerId())) {
                 return n;
             }
         }
         return null;
     }
 
     public Node assignNodeToTurker(String turkerId) {
         for (Node n : availableNodes) {
             if (n.getTurkerId() == null) {
                 n.setTurkerId(turkerId);
                 n.merge();
                 logNodeEvent(n, "assigned");
                 return n;
             }
         }
         return null;
     }
 
     public Node getNodeForTurker(String turkerId) {
         Node n = findNodeForTurker(turkerId);
         if (n == null) {
             n = assignNodeToTurker(turkerId);
         }
         return n;
     }
 
     private void logNodeEvent(Node n, String type) {
         SessionLog slog = new SessionLog();
         slog.setDate_(new Date());
         slog.setNode(n);
         slog.setSession_(slog.getSession_());
         slog.setType(type);
         slog.setNodePublicData(n.getPublicData_());
         slog.setNodePrivateData(n.getPrivateData_());
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
