 package org.genericsystem.cdi;
 
 import java.io.Externalizable;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 import java.util.Map;
 import javax.enterprise.inject.spi.BeanManager;
 import org.genericsystem.core.AbstractContext;
 import org.genericsystem.core.Archiver.SnapshotLoader;
 import org.genericsystem.core.Archiver.SnapshotWriter;
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.CacheImpl;
 import org.genericsystem.core.Engine;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.core.HomeTreeNode;
 import org.genericsystem.core.Transaction;
 import org.jboss.arquillian.testenricher.cdi.container.CDIExtension;
 import org.jboss.solder.beanManager.BeanManagerLocator;
 import org.jboss.solder.beanManager.BeanManagerUtils;
 import org.jboss.solder.core.Veto;
 
 @Veto
 public class SerializableCache extends CacheImpl implements Externalizable {
 
	public SerializableCache() {
		super((Cache) null);
	}

 	public SerializableCache(Cache cache) {
 		super(cache);
 	}
 
 	public SerializableCache(Engine engine) {
 		super(engine);
 	}
 
 	@Override
 	public void writeExternal(ObjectOutput out) throws IOException {
 		if (subContext instanceof CacheImpl) {
 			out.writeBoolean(true);
			out.writeObject(subContext);
 		} else {
 			out.writeBoolean(false);
 			out.writeLong(((Transaction) subContext).getTs());
 		}
 
 		Map<Long, HomeTreeNode> homeTreeMap = new HashMap<>();
 		out.writeInt(adds.size());
 		for (Generic add : adds)
 			SnapshotWriter.writeGeneric((GenericImpl) add, (ObjectOutputStream) out, (ObjectOutputStream) out, homeTreeMap);
 		out.writeInt(automatics.size());
 		for (Generic automatic : automatics)
 			SnapshotWriter.writeGeneric((GenericImpl) automatic, (ObjectOutputStream) out, (ObjectOutputStream) out, homeTreeMap);
 		out.writeInt(removes.size());
 		for (Generic remove : removes)
 			out.writeLong(((GenericImpl) remove).getDesignTs());
 	}
 
 	@Override
 	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
 		BeanManager beanManager;
 		try {
 			beanManager = new BeanManagerLocator().getBeanManager();
 		} catch (Exception e) {
 			beanManager = CDIExtension.getBeanManager();
 		}
 		assert beanManager != null;
 		Engine engine = BeanManagerUtils.getContextualInstance(beanManager, Engine.class);
		if (in.readBoolean())
 			subContext = (AbstractContext) in.readObject();
		else
 			subContext = new Transaction(in.readLong(), engine);
 		this.start();
 		Map<Long, HomeTreeNode> homeTreeMap = new HashMap<>();
 		Map<Long, Generic> genericMap = new HashMap<>();
 		int addSize = in.readInt();
 		SerializableSnapshotLoader loader = new SerializableSnapshotLoader();
 		for (int i = 0; i < addSize; i++)
 			adds.add(loader.loadGeneric(engine, (ObjectInputStream) in, (ObjectInputStream) in, homeTreeMap, genericMap));
 		int automaticSize = in.readInt();
 		for (int i = 0; i < automaticSize; i++)
 			automatics.add(loader.loadGeneric(engine, (ObjectInputStream) in, (ObjectInputStream) in, homeTreeMap, genericMap));
 		int removeSize = in.readInt();
 		for (int i = 0; i < removeSize; i++)
 			removes.add(findByDesignTs(engine, (ObjectInputStream) in, genericMap));
 	}
 
 	private static class SerializableSnapshotLoader extends SnapshotLoader {
 
 		@Override
 		protected Generic[] loadAncestors(Engine engine, ObjectInputStream in, Map<Long, Generic> genericMap) throws IOException {
 			int length = in.readInt();
 			Generic[] ancestors = new Generic[length];
 			for (int index = 0; index < length; index++)
 				ancestors[index] = ((CacheImpl) engine.getCurrentCache()).findByDesignTs(engine, in, genericMap);
 			return ancestors;
 		}
 
 		@Override
 		protected void plug(GenericImpl generic) {
			generic.getCurrentCache().plug(generic);
 		}
 	}
 
 }
