 package org.openspaces.analytics;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.script.Bindings;
 import javax.script.ScriptException;
 
 import org.openspaces.core.GigaSpace;
 import org.openspaces.dynamic.DynamicBase;
 import org.openspaces.events.DynamicEventTemplate;
 import org.openspaces.events.adapter.SpaceDataEvent;
 import org.openspaces.events.polling.ReceiveHandler;
 import org.openspaces.events.polling.receive.MultiTakeReceiveOperationHandler;
 import org.openspaces.events.polling.receive.ReceiveOperationHandler;
 
 import com.gigaspaces.query.IdQuery;
 
 /**
  * This is the service that executes analytics defined by dynamic code.  Also
  * listens for code introduction events (AccumulatorDef) 
  * 
  * @author DeWayne
  *
  */
 public class DynaAccumulatorContainer extends DynamicBase{
 	private static final Logger log=Logger.getLogger(DynaAccumulatorContainer.class.getName());
 	private Object template;
 
 	public DynaAccumulatorContainer(){
 		super(null,null,null,null);
 	}
 
 	public DynaAccumulatorContainer(GigaSpace space,String name,Object template,String defaultLang, String defaultCode){
 		super(space,name,defaultLang,defaultCode);
 		this.template=template;
 		start();
 	}
 
 	@ReceiveHandler
 	ReceiveOperationHandler opHandler(){
 		MultiTakeReceiveOperationHandler h=new MultiTakeReceiveOperationHandler();
 		h.setMaxEntries(10);
 		return h;
 	}
 
 	@DynamicEventTemplate
 	Object unprocessedData() {
 		return template;
 	}
 
 	@SpaceDataEvent
 	public Event[] eventListener(Event[] events) {
 
 		List<List<String>> elist=new ArrayList<List<String>>(); 
 		for(Event event:events){
 			elist.add(event.getFields());
 		}
 		try {
 			if(script!=null){
 				Bindings bindings=engine.createBindings();
 				bindings.put("events",elist);
 				AccumulatorChangeSet changes=new AccumulatorChangeSet();
 				bindings.put("changes",changes);
 				script.eval(bindings);
 				IdQuery<Accumulator> idQuery = new IdQuery<Accumulator>(Accumulator.class, name );
 				if(changes.getChangeCount()>0){
 					changes.summarize();
 					space.change(idQuery,changes);
 				}
 				for(Event event:events){
 					event.setProcessed(true);
 				}
 			}
 			else{
 				log.severe("null script execute attempted");
 			}
 		} catch (ScriptException e) {
 			e.printStackTrace();
 		}
 		return events;
 	}
 
 	/**
 	 * Wrap in event enumeration loop
 	 */
 	@Override
 	protected String preCompile(String code) {
 		return "events.each{fields->"+code+"}";
 	}
 
 	@Override
 	public void destroy(){
 		super.destroy();
 	}
 	
 	@Override
 	protected void onPreCompile() {
 		
 	}
 
 	@Override
 	protected void onPostCompile() {
 		// new code compiled, create accumulator
 		Accumulator acc=new Accumulator(name);
 		space.write(acc);
 		log.info("wrote new accumulator");
 	}
 
 }
