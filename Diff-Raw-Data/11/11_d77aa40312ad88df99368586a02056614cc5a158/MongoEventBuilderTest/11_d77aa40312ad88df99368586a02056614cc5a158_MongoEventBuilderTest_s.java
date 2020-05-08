 package nz.ac.vuw.ecs.rprofs.server.db;
 
 import com.google.common.collect.Lists;
 import com.mongodb.BasicDBObject;
 import com.mongodb.BasicDBObjectBuilder;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
 import nz.ac.vuw.ecs.rprofs.server.domain.Event;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Date;
 
 import static org.junit.Assert.*;
 
 /**
  * Author: Stephen Nelson <stephen@sfnelson.org>
  * Date: 9/09/11
  */
 public class MongoEventBuilderTest {
 
 	private static final short DS = 8;
 
 	private Dataset ds;
 	private MongoEventBuilder b;
 
 	private BasicDBObject result;
 
 	@Before
 	public void createBuilder() {
 		ds = new Dataset(new DatasetId(DS), "foo", new Date());
 		b = new MongoEventBuilder() {
 			@Override
 			DBCollection _getCollection() {
 				return null;
 			}
 
 			@Override
 			EventId _createId() {
 				return new EventId((Long) b.get("_id"));
 			}
 
 			@Override
 			void _store(DBObject data) {
 				result = new BasicDBObject();
 				result.putAll(data);
 			}
 		};
 
 		result = null;
 	}
 
 	@Test
 	public void testSetId() throws Exception {
 		EventId id = EventId.create(ds, 1);
 
 		assertNull(b.b.get("_id"));
 		assertSame(b, b.setId(id));
 		assertEquals(id.getValue(), b.b.get("_id"));
 	}
 
 	@Test
 	public void testSetThread() throws Exception {
 		InstanceId thread = InstanceId.create(ds, 2);
 
 		assertNull(b.b.get("thread"));
 		assertEquals(b, b.setThread(thread));
 		assertEquals(thread.getValue(), b.b.get("thread"));
 	}
 
 	@Test
 	public void testSetEvent() throws Exception {
 		assertNull(b.b.get("event"));
 		assertEquals(b, b.setEvent(3));
 		assertEquals(3, b.b.get("event"));
 	}
 
 	@Test
 	public void testSetClazz() throws Exception {
 		ClazzId clazz = ClazzId.create(ds, 4);
 
 		assertNull(b.b.get("class"));
 		assertEquals(b, b.setClazz(clazz));
 		assertEquals(clazz.getValue(), b.b.get("class"));
 	}
 
 	@Test
 	public void testSetMethod() throws Exception {
 		ClazzId clazz = ClazzId.create(ds, 4);
 		MethodId method = MethodId.create(ds, clazz, (short) 5);
 
 		assertNull(b.b.get("method"));
 		assertEquals(b, b.setMethod(method));
 		assertEquals(method.getValue(), b.b.get("method"));
 	}
 
 	@Test
 	public void testSetField() throws Exception {
 		ClazzId clazz = ClazzId.create(ds, 4);
 		FieldId field = FieldId.create(ds, clazz, (short) 6);
 
 		assertNull(b.b.get("field"));
 		assertEquals(b, b.setField(field));
 		assertEquals(field.getValue(), b.b.get("field"));
 	}
 
 	@Test
 	public void testArgs() throws Exception {
 		InstanceId x = InstanceId.create(ds, 7);
 		InstanceId y = null;
 		InstanceId z = InstanceId.create(ds, 8);
 
 		assertFalse(b.b.containsField("args"));
 
 		b.addArg(x);
 		b.addArg(y);
 		b.addArg(z);
 
 		assertEquals(Lists.newArrayList(x.getValue(), null, z.getValue()), b.b.get("args"));
 	}
 
 	@Test
 	public void testStore() throws Exception {
 		EventId id = EventId.create(ds, 1);
 		InstanceId thread = InstanceId.create(ds, 2);
 		ClazzId clazz = ClazzId.create(ds, 4);
 		MethodId method = MethodId.create(ds, clazz, (short) 5);
 		FieldId field = FieldId.create(ds, clazz, (short) 6);
 		InstanceId x = InstanceId.create(ds, 7);
 		InstanceId y = null;
 		InstanceId z = InstanceId.create(ds, 8);
 
 		b.setId(id);
 		b.setThread(thread);
 		b.setEvent(3);
 		b.setClazz(clazz);
 		b.setMethod(method);
 		b.setField(field);
 		b.addArg(x);
 		b.addArg(y);
 		b.addArg(z);
 
 		b.store();
 
 		assertTrue(b.b.isEmpty());
 
 		assertEquals(id.getValue(), result.get("_id"));
 		assertEquals(thread.getValue(), result.get("thread"));
 		assertEquals(3, result.get("event"));
 		assertEquals(clazz.getValue(), result.get("class"));
 		assertEquals(method.getValue(), result.get("method"));
 		assertEquals(field.getValue(), result.get("field"));
 		assertEquals(Lists.newArrayList(x.getValue(), null, z.getValue()), result.get("args"));
 	}
 
 	@Test
 	public void testGet() throws Exception {
 		EventId id = EventId.create(ds, 1);
 		InstanceId thread = InstanceId.create(ds, 2);
 		ClazzId clazz = ClazzId.create(ds, 4);
 		MethodId method = MethodId.create(ds, clazz, (short) 5);
 		FieldId field = FieldId.create(ds, clazz, (short) 6);
 		InstanceId x = InstanceId.create(ds, 7);
		InstanceId y = null;
 		InstanceId z = InstanceId.create(ds, 8);
 
 		b.init(new BasicDBObjectBuilder()
 				.add("_id", id.getValue())
 				.add("thread", thread.getValue())
 				.add("class", clazz.getValue())
 				.add("event", Event.METHOD_EXCEPTION)
 				.add("method", method.getValue())
 				.add("field", field.getValue())
 				.add("args", Lists.newArrayList(x.getValue(), 0l, z.getValue()))
 				.get());
 		Event result = b.get();
 
 		assertEquals(id, result.getId());
 		assertEquals(thread, result.getThread());
 		assertEquals(Event.METHOD_EXCEPTION, result.getEvent());
 		assertEquals(clazz, result.getClazz());
 		assertEquals(method, result.getMethod());
 		assertEquals(field, result.getField());
 		assertEquals(Lists.newArrayList(x, y, z), result.getArgs());
 	}
 }
