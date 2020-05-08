 package nz.ac.vuw.ecs.rprofs.server.weaving;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import nz.ac.vuw.ecs.rprofs.client.shared.Collections;
 import nz.ac.vuw.ecs.rprofs.server.context.Context;
 import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
 import nz.ac.vuw.ecs.rprofs.server.domain.Class;
 import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
 import nz.ac.vuw.ecs.rprofs.server.domain.Event;
 import nz.ac.vuw.ecs.rprofs.server.domain.Instance;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.ClassId;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.EventId;
 
 public class ActiveContext {
 
 	private Logger log = Logger.getLogger("weaver");
 
 	private final ContextManager manager;
 	private final Context context;
 	private final Dataset dataset;
 
 	private long eventId = 0;
 	private int classId = 0;
 
 	private final Map<String, List<ClassId>> awaitingSuper;
 
 	public ActiveContext(ContextManager manager, Context context, Dataset dataset) {
 		this.manager = manager;
 		this.context = context;
 		this.dataset = dataset;
 
 		awaitingSuper = Collections.newMap();
 	}
 
 	public Dataset getDataset() {
 		return dataset;
 	}
 
 	public Context getContext() {
 		return context;
 	}
 
 	public void setMainMethod(String program) {
 		Context d = manager.getDefault();
 		try {
 			d.open();
 			Dataset ds = d.find(Dataset.class, dataset.getId());
 			ds.setProgram(program);
 		}
 		finally {
 			d.close();
 		}
 	}
 
 	public EventId nextEvent() {
 		return new EventId(dataset.getId(), ++eventId);
 	}
 
 	public ClassId nextClass() {
 		return new ClassId(dataset.getId(), ++classId);
 	}
 
	public void storeClass(ClassRecord cr) {
 		Class cls = cr.toClass();
 		context.em().persist(cls);
 
 		log.info(String.format("storing new class %s (%s)", cls.getName(), cls.getId().toString()));
 
 		for (FieldRecord fr: cr.getFields().values()) {
 			context.em().persist(fr.toAttribute(cls));
 		}
 
 		for (MethodRecord mr: cr.getMethods().values()) {
 			context.em().persist(mr.toAttribute(cls));
 		}
 
 		if (cr.getSuperName() != null) {
 			Class parent = manager.getClasses().findClass(cr.getSuperName());
 
 			if (parent != null) {
 				cls.setParent(parent);
 			}
 			else {
 				List<ClassId> list = awaitingSuper.get(cr.getSuperName());
 				if (list == null) {
 					list = Collections.newList();
 					awaitingSuper.put(cr.getSuperName(), list);
 				}
 				list.add(cls.getId());
 			}
 		}
 
 		if (awaitingSuper.containsKey(cls.getName())) {
 			for (ClassId cid: awaitingSuper.remove(cls.getName())) {
 				log.info(String.format("looking for class %s", cid.toString()));
 				Class c = context.em().find(Class.class, cid);
 				if (c != null) {
 					log.info(String.format("found class %s (%s) with parent %s (%s)", c.getName(), cid.toString(),
 							cls.getName(), cls.getId().toString()));
 					c.setParent(cls);
 				}
 				else {
 					throw new NullPointerException(
 							String.format("could not find class id %s with parent %s (%s) [%d %d]",
 									cid.toString(), cls.getName(), cls.getId().toString(), cid.getId(), cls.getId().getId()));
 				}
 			}
 		}
 
 		context.em().persist(new Event(nextEvent(), null, Event.CLASS_WEAVE, cls, null, new ArrayList<Instance>()));
 
 		log.info(String.format("finished storing new class %s (%s)", cls.getName(), cls.getId().toString()));
 	}
 }
