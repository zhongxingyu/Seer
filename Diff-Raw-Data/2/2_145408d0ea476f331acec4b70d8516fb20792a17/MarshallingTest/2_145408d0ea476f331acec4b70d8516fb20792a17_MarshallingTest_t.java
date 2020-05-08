 package com.twolattes.json;
 
 import static com.twolattes.json.Json.FALSE;
 import static com.twolattes.json.Json.NULL;
 import static com.twolattes.json.Json.TRUE;
 import static com.twolattes.json.Json.array;
 import static com.twolattes.json.Json.number;
 import static com.twolattes.json.Json.object;
 import static com.twolattes.json.Json.string;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.junit.Test;
 
 import com.twolattes.json.OuterClass.InnerClass;
 
 
 public class MarshallingTest {
   @Test
   public void testBaseTypeEntity() throws JSONException {
     Marshaller<BaseTypeEntity> marshaller = TwoLattes.createMarshaller(BaseTypeEntity.class);
 
     BaseTypeEntity base = new BaseTypeEntity.Factory().create(5, 'h', 89L, 3.2f, (short) 16, "ya", true, 6218.687231);
 
     Json.Object o = object();
     o.put(string("_0"), number(5));
     o.put(string("_1"), string("h"));
     o.put(string("_2"), number(89));
     o.put(string("_3"), number(3.2));
     o.put(string("_4"), number(16));
     o.put(string("_5"), string("ya"));
     o.put(string("_6"), TRUE);
     o.put(string("_7"), number(6218.687231));
 
     assertEquals(o, marshaller.marshall(base));
   }
 
   @Test
   public void testNullInEntity() throws Exception {
     Email e = new Email();
     e.email = null;
 
     Marshaller<Email> m = TwoLattes.createMarshaller(Email.class);
 
     assertEquals(NULL, m.marshall(e).get(string("email")));
   }
 
   @Test
   public void testCollectionEntity() throws JSONException {
     Marshaller<CollectionEntity> marshaller = TwoLattes.createMarshaller(CollectionEntity.class);
 
     List<String> friends = new ArrayList<String>();
     friends.add("Jack");
     friends.add("Monica");
     CollectionEntity base = new CollectionEntity.Factory().create(friends);
 
     assertEquals(
         array(string("Jack"), string("Monica")),
         marshaller.marshall(base).get(string("friends")));
   }
 
   @Test
   public void testInlinedEntityUsingValueOption() throws JSONException {
     Marshaller<User> marshaller = TwoLattes.createMarshaller(User.class);
 
     Email e = new Email(); e.email = "plperez@stanford.edu";
     User u = new User(); u.email = e;
 
     assertEquals(
         string("plperez@stanford.edu"),
         marshaller.marshall(u).get(string("email")));
   }
 
   @Test
   public void testInlinedEntityUsingInlineAnnotation1() throws JSONException {
     Marshaller<UserInlinedEmail> marshaller = TwoLattes.createMarshaller(UserInlinedEmail.class);
 
     EmailInline e = new EmailInline(); e.email = "plperez@stanford.edu";
     UserInlinedEmail u = new UserInlinedEmail(); u.email = e;
 
     assertEquals(
         string("plperez@stanford.edu"),
         marshaller.marshall(u, "1").get(string("email")));
   }
 
   @Test
   public void testInlinedEntityUsingInlineAnnotation2() throws JSONException {
     Marshaller<UserInlinedEmail> marshaller = TwoLattes.createMarshaller(UserInlinedEmail.class);
 
     EmailInline e1 = new EmailInline(); e1.email = "plperez@stanford.edu";
     EmailInline e2 = new EmailInline(); e2.email = "pascal@cs.stanford.edu";
     UserInlinedEmail u = new UserInlinedEmail();
     u.emails.put("foo", e1);
     u.emails.put("bar", e2);
 
     Json.Object o1 = marshaller.marshall(u, "2");
     Object emails = o1.get(string("emails"));
     assertTrue(emails instanceof Json.Object);
     Json.Object o2 = (Json.Object) emails;
     assertEquals(string("plperez@stanford.edu"), o2.get(string("foo")));
     assertEquals(string("pascal@cs.stanford.edu"), o2.get(string("bar")));
   }
 
   @Test
   public void testInlinedEntityUsingInlineAnnotation3() throws JSONException {
     Marshaller<UserInlinedEmail> marshaller = TwoLattes.createMarshaller(UserInlinedEmail.class);
 
     EmailInline e = new EmailInline(); e.email = "plperez@stanford.edu";
     UserInlinedEmail u = new UserInlinedEmail(); u.emailNoInline = e;
 
     Json.Object o = marshaller.marshall(u, "3");
     assertTrue(o.get(string("emailNoInline")) instanceof Json.Object);
     Json.Object oe = (Json.Object) o.get(string("emailNoInline"));
     assertEquals(string("plperez@stanford.edu"), oe.get(string("email")));
   }
 
   @Test
   public void testInlinedEntityUsingInlineAnnotation4() throws JSONException {
     Marshaller<UserInlinedEmail> marshaller = TwoLattes.createMarshaller(UserInlinedEmail.class);
 
     EmailInline e = new EmailInline(); e.email = "plperez@stanford.edu";
     UserInlinedEmail u = new UserInlinedEmail(); u.emailsArray = new EmailInline[] { e };
 
     Json.Object o = marshaller.marshall(u, "4");
     assertTrue(o.get(string("emailsArray")) instanceof Json.Array);
     Json.Array oe = (Json.Array) o.get(string("emailsArray"));
     assertEquals(string("plperez@stanford.edu"), oe.get(0));
   }
 
   @Test
   public void testInlinedEntityUsingInlineAnnotation5() throws JSONException {
     Marshaller<UserInlinedEmail> marshaller = TwoLattes.createMarshaller(UserInlinedEmail.class);
 
     EmailInline e = new EmailInline(); e.email = "plperez@stanford.edu";
     UserInlinedEmail u = new UserInlinedEmail(); u.emailsList.add(e);
 
     Json.Object o = marshaller.marshall(u, "5");
     assertTrue(o.get(string("emailsList")) instanceof Json.Array);
     Json.Array oe = (Json.Array) o.get(string("emailsList"));
     assertEquals(string("plperez@stanford.edu"), oe.get(0));
   }
 
   @Test
   public void testInlinedEntityUsingInlineAnnotation6() throws JSONException {
     Marshaller<EmailInline> marshaller = TwoLattes.createMarshaller(EmailInline.class);
 
     EmailInline e = new EmailInline(); e.email = "plperez@stanford.edu";
     ArrayList<EmailInline> list = new ArrayList<EmailInline>();
     list.add(e);
 
     Json.Array a = marshaller.marshallList(list);
     assertEquals(1, a.size());
     assertEquals(string("plperez@stanford.edu"), a.get(0));
   }
 
   @Test
   public void testInlininPolymorphicEntityThatHasOnlyDiscriminator() {
     Marshaller<InlinePolymorphic> marshaller = TwoLattes.createMarshaller(InlinePolymorphic.class);
 
     Json.Object notInlined = marshaller.marshall(new InlinePolymorphic() {{
       doNotInlineMe = new Polymorphic();
     }});
 
     assertEquals(
         object(string("doNotInlineMe"), object(string("foo"), string("bar"))),
         notInlined);
 
     Json.Object inlined = marshaller.marshall(new InlinePolymorphic() {{
       inlineMe = new Polymorphic();
     }});
 
     assertEquals(
         object(string("inlineMe"), string("bar")),
         inlined);
   }
 
   @Test
   public void testListOfEntities() throws JSONException {
     Marshaller<User> marshaller = TwoLattes.createMarshaller(User.class);
 
     Email e1 = new Email(); e1.email = "plperez@stanford.edu";
     User u1 = new User(); u1.email = e1;
 
     Email e2 = new Email(); e2.email = "dalia_ma@hotmail.com";
     User u2 = new User(); u2.email = e2;
 
     Json.Array array = marshaller.marshallList(Arrays.asList(u1, u2));
     assertEquals(
         string("plperez@stanford.edu"),
         ((Json.Object) array.get(0)).get(string("email")));
     assertEquals(
         string("dalia_ma@hotmail.com"),
         ((Json.Object) array.get(1)).get(string("email")));
   }
 
   @Test
   public void testMapOfEntities() throws Exception {
     Marshaller<User> marshaller = TwoLattes.createMarshaller(User.class);
 
    Email e1 = new Email(); e1.email = "jmjacobs@stanford.edu";
     User u1 = new User(); u1.email = e1;
     Map<String, User> map = Collections.singletonMap("1", u1);
 
     assertEquals(
         object(
             string("1"),
             object(
                 string("email"),
                 string("jmjacobs@stanford.edu"))),
         marshaller.marshallMap(map));
   }
 
   @Test
   public void testEmptyMapOfEntities() throws Exception {
     Marshaller<User> marshaller = TwoLattes.createMarshaller(User.class);
 
     assertEquals(
         object(),
         marshaller.marshallMap(Collections.<String, User>emptyMap()));
   }
 
   @Test
   public void testMapOfEntitiesWithNullValue() throws Exception {
     Marshaller<User> marshaller = TwoLattes.createMarshaller(User.class);
     Map<String, User> map = Collections.singletonMap("1", null);
 
     assertEquals(
         object(string("1"), Json.NULL),
         marshaller.marshallMap(map));
   }
 
   @Test
   public void testEmbeddedMapOfEntities() throws Exception {
     Marshaller<EntityMap> marshaller = TwoLattes.createMarshaller(EntityMap.class);
 
     EntityMap em = new EntityMap();
     Email e1 = new Email(); e1.email = "plperez@stanford.edu";
     em.addEmail("Jack", e1);
 
     Json.Object jsonEmails = object();
     Json.Object jsonPlperez = object();
     Json.Object jsonEm = object();
     jsonPlperez.put(string("email"), string("plperez@stanford.edu"));
     jsonEmails.put(string("Jack"), jsonPlperez);
     jsonEm.put(string("emails"), jsonEmails);
 
     assertEquals(jsonEm, marshaller.marshall(em));
   }
 
   @Test
   public void testNullArray() throws Exception {
     Marshaller<ArrayEntity> marshaller = TwoLattes.createMarshaller(ArrayEntity.class);
 
     assertEquals(
         NULL,
         marshaller.marshall(new ArrayEntity()).get(string("values")));
   }
 
   @Test
   public void testArray() throws Exception {
     Marshaller<ArrayEntity> marshaller = TwoLattes.createMarshaller(ArrayEntity.class);
 
     ArrayEntity arrayEntity = new ArrayEntity();
     arrayEntity.values = new String[] {"ya", "yo", "yi"};
     Json.Object o = marshaller.marshall(arrayEntity);
 
     assertTrue(o.get(string("values")) instanceof Json.Array);
     Json.Array array = (Json.Array) o.get(string("values"));
     assertEquals(3, (array).size());
     assertEquals(string("ya"), array.get(0));
     assertEquals(string("yo"), array.get(1));
     assertEquals(string("yi"), array.get(2));
   }
 
   @Test(expected = StackOverflowError.class)
   public void testCyclicStructureWarn() throws Exception {
     Node n = new Node();
     n.addNeighbor(n);
 
     Marshaller<Node> marshaller = TwoLattes.createMarshaller(Node.class);
 
     marshaller.marshall(n);
   }
 
   @Test
   public void testUserType() throws Exception {
     Marshaller<EntityWithURL> m = TwoLattes.createMarshaller(EntityWithURL.class);
 
     EntityWithURL entity = new EntityWithURL();
     entity.setUrl(new URL("http://www.twolattes.com"));
 
     assertEquals(
         string("http://www.twolattes.com"),
         m.marshall(entity).get(string("url")));
   }
 
   @Test
   public void testInnerClass() throws Exception {
     Marshaller<InnerClass> m =  TwoLattes.createMarshaller(OuterClass.InnerClass.class);
 
     InnerClass e = new InnerClass();
     e.field = "hello";
 
     Json.Object o = m.marshall(e);
     assertEquals(1, o.size());
     assertEquals(string("hello"), o.get(string("field")));
   }
 
   @Test
   public void testGetterSetter1() throws Exception {
     Marshaller<GetterSetterEntity> m = TwoLattes.createMarshaller(GetterSetterEntity.class);
 
     GetterSetterEntity e = new GetterSetterEntity();
     e.setName("Jack");
 
     Json.Object o = m.marshall(e);
     assertEquals(1, o.size());
     assertEquals(string("Jack"), o.get(string("name")));
   }
 
   @Test
   public void testGetterSetter2() throws Exception {
     Marshaller<EntityInterface> m = TwoLattes.createMarshaller(EntityInterface.class);
 
     EntityInterface e = new EntityInterfaceImpl();
     e.setWhatever(false);
 
     Json.Object o = m.marshall(e);
     assertEquals(1, o.size());
     assertEquals(FALSE, o.get(string("whatever")));
   }
 
   @Test
   public void testDoublyInlined1() throws Exception {
   	Marshaller<DoublyInlined> m = TwoLattes.createMarshaller(DoublyInlined.class);
 
   	// entity
   	DoublyInlined entity = new DoublyInlined();
   	DoublyInlined.Bar bar = new DoublyInlined.Bar();
   	bar.hello = "hello";
   	DoublyInlined.Foo foo = new DoublyInlined.Foo();
   	foo.bar = bar;
   	entity.foo = foo;
 
   	Json.Object o = m.marshall(entity);
   	assertEquals(1, o.size());
   	assertEquals(object(string("foo"), string("hello")), o);
   }
 
   @Test
   public void testDoublyInlined2() throws Exception {
   	Marshaller<DoublyInlined> m = TwoLattes.createMarshaller(DoublyInlined.class);
 
   	// entity
   	DoublyInlined entity = new DoublyInlined();
   	DoublyInlined.Foo foo = new DoublyInlined.Foo();
   	foo.bar = null;
   	entity.foo = foo;
 
   	Json.Object o = m.marshall(entity);
   	assertEquals(1, o.size());
   	assertEquals(NULL, o.get(string("foo")));
   }
 
   @Test
   public void testDoublyInlined3() throws Exception {
   	Marshaller<DoublyInlined> m = TwoLattes.createMarshaller(DoublyInlined.class);
 
   	// entity
   	DoublyInlined entity = new DoublyInlined();
   	entity.foo = null;
 
   	Json.Object o = m.marshall(entity);
   	assertEquals(1, o.size());
   	assertEquals(NULL, o.get(string("foo")));
   }
 
   @Test
   public void testDoublyInlinedWithGetters() throws Exception {
     Marshaller<DoublyInlinedWithGetters> m =
         TwoLattes.createMarshaller(DoublyInlinedWithGetters.class);
 
     // entity
     DoublyInlinedWithGetters entity = new DoublyInlinedWithGetters();
     DoublyInlinedWithGetters.Bar bar = new DoublyInlinedWithGetters.Bar();
     bar.hello = "hello";
     DoublyInlinedWithGetters.Foo foo = new DoublyInlinedWithGetters.Foo();
     foo.bar = bar;
     entity.foo = foo;
 
     Json.Object o = m.marshall(entity);
     assertEquals(1, o.size());
     assertEquals(object(string("foo"), string("hello")), o);
   }
 
   @Test
   public void testTriplyInlined1() throws Exception {
     Marshaller<TriplyInlined> m = TwoLattes.createMarshaller(TriplyInlined.class);
 
     // entity
     TriplyInlined entity = new TriplyInlined();
     TriplyInlined.Bar bar = new TriplyInlined.Bar();
     TriplyInlined.Baz baz = new TriplyInlined.Baz();
     baz.hello = "hello";
     bar.baz = baz;
     TriplyInlined.Foo foo = new TriplyInlined.Foo();
     foo.bar = bar;
     entity.foo = foo;
 
     Json.Object o = m.marshall(entity);
     assertEquals(1, o.size());
     assertEquals(string("hello"), o.get(string("foo")));
   }
 
   @Test
   public void testPrivateNoArgConstructor() throws Exception {
     Marshaller<PrivateNoArgConstructor> m =
         TwoLattes.createMarshaller(PrivateNoArgConstructor.class);
     PrivateNoArgConstructor e = new PrivateNoArgConstructor("hi");
     Json.Object o = m.marshall(e);
 
     assertEquals(1, o.size());
     assertEquals(string("hi"), o.get(string("foo")));
   }
 
   @Test
   public void testSameEntityShouldNotBeConsideredACyclycity() throws Exception {
   	UserWithTwoInlinedEmail user = new UserWithTwoInlinedEmail();
   	Email email = new Email();
   	email.email = "somewhere@bug.com";
 
   	// two values point to the same entity
   	user.email1 = email;
   	user.email2 = email;
 
   	Json.Object o = TwoLattes.createMarshaller(UserWithTwoInlinedEmail.class).marshall(user);
 
   	assertTrue(o.get(string("email1")) instanceof Json.Object);
   	assertTrue(o.get(string("email2")) instanceof Json.Object);
   }
 
   @Test
   public void testTypeOnGetter() throws Exception {
   	TypeOnGetter e = new TypeOnGetter();
 
   	Json.Object o = TwoLattes.createMarshaller(TypeOnGetter.class).marshall(e);
 
   	assertEquals(1, o.size());
   	assertEquals(string("http://www.kaching.com"), o.get(string("url")));
   }
 
   @Test
   public void nativeArray() throws Exception {
     Marshaller<EntityWithNativeArray> m = TwoLattes.createMarshaller(EntityWithNativeArray.class);
 
     EntityWithNativeArray e = new EntityWithNativeArray();
     e.ids = new int[] { 5, 1, 2 };
 
     Json.Object o = m.marshall(e);
 
     assertEquals(1, o.size());
     Json.Array ids = (Json.Array) o.get(string("ids"));
     assertEquals(3, ids.size());
     assertEquals(number(5), ids.get(0));
     assertEquals(number(1), ids.get(1));
     assertEquals(number(2), ids.get(2));
   }
 
   @Test
   public void arrayOfArray() throws Exception {
     ArrayOfArray arrayOfArray = new ArrayOfArray();
     arrayOfArray.matrix = new Integer[][] {{56, 57}, {58, 59}};
 
     Json.Object o = TwoLattes.createMarshaller(ArrayOfArray.class).marshall(arrayOfArray);
 
     assertEquals(1, o.size());
     Json.Array matrix = (Json.Array) o.get(string("matrix"));
     assertEquals(2, matrix.size());
     Json.Array a0 = (Json.Array) matrix.get(0);
     assertEquals(2, a0.size());
     assertEquals(number(56), a0.get(0));
     assertEquals(number(57), a0.get(1));
     Json.Array a1 = (Json.Array) matrix.get(1);
     assertEquals(2, a1.size());
     assertEquals(number(58), a1.get(0));
     assertEquals(number(59), a1.get(1));
   }
 
   @Test
   public void nullOptionalObject() throws Exception {
     NullOptionalValue obj = new NullOptionalValue();
 
     Json.Object o = TwoLattes.createMarshaller(NullOptionalValue.class).marshall(obj);
     assertEquals(0, o.size());
   }
 
   @Test
   public void nonNullOptionalObject() throws Exception {
     NullOptionalValue obj = new NullOptionalValue();
     obj.setOptional("optional");
 
     Json.Object o = TwoLattes.createMarshaller(NullOptionalValue.class).marshall(obj);
     assertEquals(1, o.size());
   }
 
   @Test
   public void testDifferentFieldGetterSetterName() {
     Foo foo = new Foo();
     foo.foo = 42;
 
     assertEquals(
         object(string("foo"), number(42), string("bar"), number(42)),
         TwoLattes.createMarshaller(Foo.class).marshall(foo));
   }
 
 }
