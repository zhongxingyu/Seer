 package nz.ac.vuw.ecs.rprofs.server.db;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import javax.validation.constraints.NotNull;
 import nz.ac.vuw.ecs.rprofs.server.data.util.Builder;
 import nz.ac.vuw.ecs.rprofs.server.data.util.Creator;
 import nz.ac.vuw.ecs.rprofs.server.data.util.Query;
 import nz.ac.vuw.ecs.rprofs.server.data.util.Updater;
 import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
 import nz.ac.vuw.ecs.rprofs.server.model.Id;
 import org.bson.BSONObject;
 
 /**
  * Author: Stephen Nelson <stephen@sfnelson.org>
  * Date: 14/09/11
  */
 public abstract class EntityBuilder<B extends EntityBuilder<B, I, T>, I extends Id<I, T>, T extends DataObject<I, T>>
 		implements Creator<B, I, T>, Updater<I, T>, Query<I, T>, Builder<B, I, T> {
 
 	protected BasicDBObject b;
 
 	public EntityBuilder() {
 		b = new BasicDBObject();
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public B init() {
 		reset();
 		return (B) this;
 	}
 
 	@Override
 	public I store() {
 		I id = _createId();
 		b.put("_id", id.getValue());
 		_store(b);
 		reset();
 		return id;
 	}
 
 	@Override
 	public long count() {
 		long result = _count(b);
 		reset();
 		return result;
 	}
 
 	@Override
 	public Cursor<? extends T> find() {
 		return find(0, Integer.MAX_VALUE);
 	}
 
 	@Override
 	public Cursor<? extends T> find(final long start, final long max) {
 		final DBCursor c = _query(b).skip((int) start);
 		return new Cursor<T>() {
 			int count = 0;
 
 			@Override
 			public boolean hasNext() {
 				return c.hasNext() && count < max;
 			}
 
 			@Override
 			public T next() {
 				assert (hasNext());
 				count++;
 				reset();
 				init(c.next());
 				return get();
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException("not implemented");
 			}
 
 			@Override
 			public int count() {
 				return c.count();
 			}
 
 			@Override
 			public void close() {
 				c.close();
 			}
 		};
 	}
 
 	@Override
 	public void update(I toUpdate) {
 		Long id = toUpdate.getValue();
 		_update(new BasicDBObject("_id", id), b);
 		reset();
 	}
 
 	protected void reset() {
 		b = new BasicDBObject();
 	}
 
 	@SuppressWarnings("unchecked")
 	B init(@NotNull BSONObject init) {
 		if (init != null) b.putAll(init);
 		return (B) this;
 	}
 
 	abstract I _createId();
 
 	abstract void _store(DBObject toStore);
 
 	abstract void _update(DBObject ref, DBObject update);
 
 	abstract DBCursor _query(DBObject ref);
 
 	abstract long _count(DBObject ref);
 
 	public abstract T get();
 }
