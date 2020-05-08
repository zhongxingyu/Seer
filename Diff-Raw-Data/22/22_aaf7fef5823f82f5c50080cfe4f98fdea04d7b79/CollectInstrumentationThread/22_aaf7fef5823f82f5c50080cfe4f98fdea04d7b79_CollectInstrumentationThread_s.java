 /**
 * $Id: CollectInstrumentationThread.java,v 1.2 2007/05/10 22:42:42 vpapad Exp $
  *
  */
 
 package niagara.data_manager;
 
 import org.w3c.dom.*;
 
 import niagara.physical.Tunable;
 import niagara.query_engine.*;
 import niagara.utils.*;
 import niagara.connection_server.Catalog;
 import niagara.connection_server.NiagraServer;
 import niagara.logical.CollectInstrumentation;
 
 import java.util.Timer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.TimerTask;
 
 import niagara.ndom.DOMFactory;
 import niagara.optimizer.colombia.*;
 
 public class CollectInstrumentationThread extends SourceThread {
     // Optimization-time attributes
     private String plan;
     private ArrayList<String> operators;
     /** Approximate time between clock ticks, in milliseconds */
     protected int period;
 
     // Runtime attributes
     protected SinkTupleStream outputStream;
     protected Timer timer;
     protected CollectInstrumentationTask task;
     private long startTime;
 
     public CollectInstrumentationThread() {
         isSendImmediate = true;
     }
 
     public CollectInstrumentationThread(String plan,
             ArrayList<String> operators, int period) {
         this.plan = plan;
         this.operators = operators;
         this.period = period;
         isSendImmediate = true;
     }
 
     public void plugIn(SinkTupleStream outputStream, DataManager dm) {
         this.outputStream = outputStream;
         outputStream.setSendImmediate();
     }
 
     /**
      * Thread run method
      *
      */
     public void run() {
         startTime = System.nanoTime();
         task = new CollectInstrumentationTask(this);
         timer = new Timer("CollectInstrumentation", false);
         timer.scheduleAtFixedRate(task, 0, period);
     }
 
     public void setPeriod(int period) {
         this.period = period;
         if (task != null) {
 	    timer.cancel();
 	    task.cancel();
             task = new CollectInstrumentationTask(this);
             timer.scheduleAtFixedRate(task, 0, period);
         }
     }
 
     @Tunable(name = "period",
             type = Tunable.TunableType.INTEGER,
             setter = "setPeriod",
             description = "Collection period")
     public int getPeriod() {
         return period;
     }
 
     public void opInitFrom(LogicalOp op) {
         CollectInstrumentation lop = (CollectInstrumentation) op;
         this.plan = lop.getPlan();
         this.operators = lop.getOperators();
         this.period = lop.getPeriod();
     }
 
     public Op opCopy() {
         return new CollectInstrumentationThread(plan, operators, period);
     }
 
     public boolean equals(Object o) {
         if (o == null || !(o instanceof CollectInstrumentationThread))
             return false;
         if (o.getClass() != getClass())
             return o.equals(this);
         CollectInstrumentationThread other = (CollectInstrumentationThread) o;
 
         return plan.equals(other.plan) && operators.equals(other.operators)
                 && period == other.period;
     }
 
     public int hashCode() {
         return plan.hashCode() ^ operators.hashCode() ^ period;
     }
 
     public TupleSchema getTupleSchema() {
         TupleSchema ts = new TupleSchema();
         ts.addMappings(getLogProp().getAttrs());
         return ts;
     }
 
     public void dumpAttributesInXML(StringBuffer sb) {
         sb.append(" plan='").append(plan).append("' operators='");
         // Strip brackets from ArrayList.toString()
         String ops = operators.toString();
         sb.append(ops.substring(1, ops.length() - 1));
         sb.append("' period='").append(period).append("'");
         sb.append(" schema='").append(getLogProp().getAttrs().toString()).append("'/>");
     }
 
     public Cost findLocalCost(ICatalog catalog, LogicalProperty[] InputLogProp) {
         // XXX vpapad TODO: Totally bogus!
         // What's the cost of something that runs forever?
         // We should do something with rates here.
         return new Cost(10 * catalog.getDouble("tuple_construction_cost"));
     }
 
     /** 
      * @see niagara.query_engine.SchemaProducer#constructTupleSchema(niagara.query_engine.TupleSchema[])
      */
     public void constructTupleSchema(TupleSchema[] inputSchemas) {
         // Do nothing
     }
 
     class CollectInstrumentationTask extends TimerTask {
         CollectInstrumentationThread tt;
         // Runtime attributes
         private ArrayList<Instrumentable> ops;
         private HashMap<Instrumentable, String> ops2names;
         private ArrayList<String> instrumentationNames;
         private ArrayList<Object> instrumentationValues;
         private Document doc;
         private Catalog catalog;
         private SinkTupleStream outputStream;
         private long startTime;
 
         public CollectInstrumentationTask(CollectInstrumentationThread tt) {
             catalog = NiagraServer.getCatalog();
             ops = new ArrayList<Instrumentable>();
             ops2names = new HashMap<Instrumentable, String>();
             instrumentationNames = new ArrayList<String>();
             instrumentationValues = new ArrayList<Object>();
             for (String opName : operators) {
                 Instrumentable op = catalog.getOperator(plan, opName);
                 if (op != null) {
                     ops.add(op);
                     ops2names.put(op, opName);
                 }
             }
             doc = DOMFactory.newDocument();
             outputStream = tt.outputStream;
             startTime = tt.startTime;
         }
 
         public void run() {
             // If the plan is no longer active, we're done
             if (!catalog.isActive(plan)) {
                 cleanUp();
 		timer.cancel();
 		return;
 	    }
 
             for (Instrumentable op : ops) {
                 long currentTime = (System.nanoTime()) - startTime;
                 
                 StringAttr time = new StringAttr(String.valueOf(currentTime));
                 op.getInstrumentationValues(instrumentationNames,
                         instrumentationValues);
                 StringAttr opName = new StringAttr(ops2names.get(op));
                 for (int i = 0; i < instrumentationNames.size(); i++) {
                     Object value = instrumentationValues.get(i);
                     if (value == null)
                         continue;
 
                    Tuple tuple = new Tuple(true, 4);
                     tuple.appendAttribute(time);
                     tuple.appendAttribute(opName);
                     tuple.appendAttribute(new StringAttr(instrumentationNames.get(i)));
                     if (value instanceof Node) {
                         Node n = doc.importNode((Node) value, true);
                         tuple.appendAttribute(new XMLAttr(n));
                     } else if (value instanceof Tuple) {
                         Tuple t = (Tuple) value;
                         Node tupleNode = doc.createElement("tuple");
                         TupleSchema ts = op.getTupleSchema();
                         int tupleLength = ts.getLength();
                         for (int j = 0; j < tupleLength; j++) {
                             Element attrNode = doc.createElement("attribute");
                             attrNode.setAttribute("name", 
                                     ts.getVariableName(j));
 			    // XXX vpapad: PhysicalConstruct generates tuple 
 			    // attributes that are plain XML nodes, not 
 			    // XMLAttr...
 			    Object tupleAttr = t.getAttribute(j);
 			    if (tupleAttr instanceof XMLAttr) {
 				Node n = doc.importNode(((XMLAttr) tupleAttr).getNodeValue(), true);
 				attrNode.appendChild(n);
 			    } if (tupleAttr instanceof BaseAttr) {
 				String attrValue = ((BaseAttr) t.getAttribute(j)).toASCII();
 				attrNode.appendChild(doc.createTextNode(attrValue));
 			    } else {
 				if (!(tupleAttr instanceof Node)) {
 				    throw new PEException("Unexpected tuple attribute type");
 				}
 				Node n = doc.importNode((Node) tupleAttr, true);
 				attrNode.appendChild(n);
 			    }
                             tupleNode.appendChild(attrNode);
                         }
                         tuple.appendAttribute(new XMLAttr(tupleNode));
                     } else
                         tuple.appendAttribute(new StringAttr(value.toString()));
                     do {
                         try {
                             if (outputStream.getStatus() == SinkTupleStream.Closed) {
                                 cancel();
                                 return;
                             }
                             outputStream.putTuple(tuple);
                             break;
                         } catch (InterruptedException ie) {
                             // XXX vpapad: what am I supposed to do here?
                         } catch (ShutdownException se) {
                             cancel();
                             cleanUp();
                             return;
                         }
                     } while (true);
                 }
                 instrumentationNames.clear();
                 instrumentationValues.clear();
             }
         }
     }
 
     public void cleanUp() {
         try {
 	    timer.cancel();
             outputStream.endOfStream();
         } catch (java.lang.InterruptedException ie) {
             /* do nothing */
         } catch (ShutdownException se) {
             /* do nothing */
         }
     }
     
     public void dumpChildrenInXML(StringBuffer sb) {
         // Do nothing
     }
 }
 
