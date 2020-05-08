 /*
  * Swordess-ldap, an Object-Directory Mapping tool. 
  * 
  * Copyright (c) 2013, 2013 Liu Xingyu.
  * 
  * Swordess-ldap is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * Swordess-ldap is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with Swordess-ldap. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.swordess.ldap;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * An JNDI based interface to interact with a LDAP server.
  * <p/>
  * 
  * This doc will show you following things:
  * <ol>
  * <li>Reference Integrity</li>
  * <li>Continuous Usage of an Entity</li>
  * <li>Session Cache and Resource's Release</li>
  * </ol>
  * 
  * <b>Reference Integrity</b>
  * <p/>
  * Each implementation should guarantee the reference integrity, which means its
  * okay to use <code>==</code> to compare two lookups via the given dn with all
  * the defined attributes during a same session:
  * 
  * <pre>
  * SomeType entity = session.read(SomeType.class, dn);
  * SomeType anotherTry = session.read(SomeType.class, dn);
  * if (entity == anotherTry) { // this always works
  *     // ...
  * }
  * 
  * Map&lt;String, Object&gt; bar = session.read(Foo.class, dn, returningAttrs);
  * Map&lt;String, Object&gt; anotherBar = session.read(Foo.class, dn, returningAttrs);
  * if (bar == anotherBar) { // this might not working
  * }
  * </pre>
  * 
  * But it may be not reliable to use <code>==</code> to compare two searches
  * directly via the given filter approach even during the same session:
  * 
  * <pre>
  * List&lt;SomeType&gt; entities = session.search(SomeType.class, filter);
  * List&lt;SomeType&gt; anotherTry = session.search(SomeType.class, filter);
  * if (entities == anotherTry) { // this might not working
  * }
  * </pre>
  * 
  * Still, the reference integrity should at least guarantee the possibility to
  * use <code>==</code> to distinguish one search result with another, which
  * means the following code snippet works (continue with the example shown
  * above):
  * 
  * <pre>
  * SomeType foo = entities.get(0);
  * if (anotherTye.get(0) == foo) { // in general this works, but in the real
  *                                 // circumstance, it might be broke by changes
  *                                 // which occurred in LDAP server side.
  * }
  * </pre>
  * 
  * So, the safest approach is still using <code>equals()</code> method to do
  * comparisons.
  * <p/>
  * 
  * <b>Continuous Usage of an Entity</b>
  * <p/>
  * Another issue with session is about the missed real-time feature which is
  * caused by the possible logic in LDAP server: when one transient object is
  * persisted to the server, the changes which might happen in LDAP side might
  * not be reflected in session cache. So code which relies on real-time changes
  * in LDAP side should be avoided. And if you really need the changes, just do
  * an extra lookup! Note that this issue will has no compact with the basic
  * continuous usage of a persist entity (more details, please refer to
  * {@link #update(Object)}).
  * <p/>
  * 
  * <b>Session Cache and Resource's Release</b>
  * <p/>
  * Each session will maintain an internal cache for accelerating the lookup
  * process.
  * <p/>
  * Currently, there are two ways to get a session:
  * <ul>
  * <li><code>
  * SessionFactory sessionFactory = SessionFactory.getDefaultFactory();
  * Session session = sessionFactory.getCurrentSession();
  * </code></li>
  * <li><code>
  * SessionFactory sessionFactory = SessionFactory.getDefaultFactory();
  * Session session = sessionFactory.openSession();
  * </code></li>
  * </ul>
  * 
  * Using either approach, you both need to close the session to release all the
  * resource including cache, JNDI connection, etc. But different approaches lead
  * to different side effects when you forget to close the session.
  * <ul>
  * <li>
  * If you use the second approach, the only side effect is that all the resource
  * the session occupies still there until the system garbage collection.</li>
  * <li>
  * If you use the first approach, the side effect include not only the session
  * itself but also the next session you get via
  * <code>sessionFactory.getCurrentSession()</code>. As you forget to close when
  * you last use the <b>current session</b>, the cache is not cleared, the
  * connection is not released and the session itself is not destroyed!!! In
  * general, this will lead to incorrect lookup process and break you code.</li>
  * </ul>
  * So, please remember to close the session no matter what approach you get it
  * (more details, please refer to {@link #close()})!!!
  * <p/>
  * 
  * @author Liu Xingyu <xingyulliiuu@gmail.com>
  * 
  */
 public interface Session {
 
     /**
      * Persist an object.
      * <p/>
      * It is decided by the implementor to refresh all the data of the given
      * object or not. That means, its not reliable to use the specified
      * <tt>obj</tt> after calling method:
      * 
      * <pre>
      * session.create(obj);
      * List&lt;User&gt; users = obj.getUsers(); // this might not working!!!
      * </pre>
      * 
      * And the safest way to get the most refreshed data of this <tt>obj</tt> is
      * to call {@link #read(Class, String)} and leave the original <tt>obj</tt>
      * behind:
      * 
      * <pre>
      * session.create(obj);
      * String dn = buildDn(obj); // its easy for client code to do this
      * SomeType entity = session.read(SomeType.class, dn);
      * List&lt;User&gt; users = entity.getUsers();
      * </pre>
      * 
      * @param obj
      *        a transient object
      */
     public void create(Object obj);
     
     /**
      * Update an entity.
      * <p/>
      * Implementor of this method is required to ensure the entity's basic
      * continuous usage. That means after invoking update(entity), its possible
      * to call update(entity) again and again.
      * 
      * <pre>
      * entity.getUsers().add(&quot;foo&quot;);
      * session.update(entity);
      * 
      * List&lt;String&gt; someMore = Arrays.asList(&quot;bar&quot;, &quot;zoo&quot;);
      * entity.getUsers().addAll(someMore);
      * session.update(entity);
      * 
      * entity.setAddress(&quot;floatcloud&quot;);
      * session.update(entity);
      * </pre>
      * 
      * But whether refresh all the data of this entity or not is optional. This
      * means the following code snippet might not working:
      * 
      * <pre>
      * entity.getUsers().add(&quot;foo&quot;); // assume that &quot;foo&quot; is a female :)
      * session.update();
      * 
      * boolean isFooThere = entity.getFemaleUsers().contains(&quot;foo&quot;); // this might not
      *                                                               // working
      * </pre>
      * 
      * @param entity
      *        a persistent entity
      */
     public void update(Object entity);
     
     /**
      * Delete an entry via the given dn.
      * 
      * @param dn
      */
     public void delete(String dn);
     
     /**
      * Delete an entry via the given entity.
      * 
      * @param entity a persistent entity
      */
     public void delete(Object entity);
     
     /**
      * Lookup for an entity with all the defined attributes via the specified
      * class and dn.
      * 
      * @param <T>
      * @param clazz
      *        an {@link Entry} annotated class
      * @param dn
      *        dn string
      * @return entity which carries at least all the defined attributes
      */
     public <T> T read(Class<T> clazz, String dn);
 
     /**
      * Lookup information via the specified clazz, dn and returning attribute
      * names.
      * <p/>
      * 
     * The value in the map will be a single String object if there is
     * only one attribute value, otherwise it will be a List of String.
     * <p/>
     * 
      * Note:
      * <ul>
      * <li>Reference integrity is optional, so don't rely on <code>==</code> to
      * distinguish two maps.</li>.
      * <li>Its not limited to fetch attributes among all the defined attributes,
      * any attribute as long as it exist in LDAP side is possible to be fetched
      * here.</li>
      * </ul>
      * 
      * @param clazz
      *        an {@link Entry} annotated class
      * @param dn
      *        dn string
      * @param returningAttrs
      *        attributes to fetch
      * @return a map which carries the specified attributes
      */
     public Map<String, Object> read(Class<?> clazz, String dn, String[] returningAttrs);
     
     /**
      * Search for a bunch of entities via the given filter.
      * 
      * @param <T>
      * @param clazz
      *        an {@link Entry} annotated class
      * @param filter
      *        LDAP filter string
      * @return
      */
     public <T> List<T> search(Class<T> clazz, String filter);
     
     /**
      * Search for a bunch of entities via the given filter and returning
      * attribute names.
      * <p/>
      * 
     * The value in the map will be a single String object if there is
     * only one attribute value, otherwise it will be a List of String.
     * <p/>
     * 
      * Note:
      * <ul>
      * <li>Reference integrity is optional, so don't rely on <code>==</code> to
      * distinguish two map elements.</li>.
      * <li>Its not limited to fetch attributes among all the defined attributes,
      * any attribute as long as it exist in LDAP side is possible to be fetched
      * here.</li>
      * </ul>
      * 
      * @param clazz
      *        an {@link Entry} annotated class
      * @param filter
      *        LDAP filter string
      * @param returningAttrs
      *        attributes to fetch
      * @return
      */
     public List<Map<String, Object>> search(Class<?> clazz, String filter, String[] returningAttrs);
     
     /**
      * Search for a unique result via the given filter. If there are actually
      * multiple results, the first one will be returned.
      * 
      * @param <T>
      * @param clazz
      *        an {@link Entry} annotated class
      * @param filter
      *        LDAP filter string
      * @return
      */
     public <T> T uniqueSearch(Class<T> clazz, String filter);
     
     /**
      * Search for a unique result via the given filter and returning attribute
      * names. If there are actually multiple results, the first one will be
      * returned.
      * <p/>
      * 
     * The value in the map will be a single String object if there is
     * only one attribute value, otherwise it will be a List of String.
     * <p/>
     * 
      * Note:
      * <ul>
      * <li>Reference integrity is optional, so don't rely on <code>==</code> to
      * distinguish two maps.</li>.
      * <li>Its not limited to fetch attributes among all the defined attributes,
      * any attribute as long as it exist in LDAP side is possible to be fetched
      * here.</li>
      * </ul>
      * 
      * @param clazz
      *        an {@link Entry} annotated class
      * @param filter
      *        LDAP filter string
      * @param returningAttrs
      *        attributes to fetch
      * @return
      */
     public Map<String, Object> uniqueSearch(Class<?> clazz, String filter, String[] returningAttrs);
     
     /**
      * Clear session cache if has and release resources this session occupies.
      * <p/>
      * NOTE: In order to maintain the session cache and pool the internal JNDI
      * connections correctly, <b>it is a must for a conceptual integrated code
      * logic to call this method at its end.</b> And we recommend a best
      * practice using following code block to do this:
      * 
      * <pre>
      * public void removeMales() {
      *     try {
      *         // ...
      *         methodA();
      *         methodB();
      *     } finally {
      *         session.close();
      *     }
      * }
      * 
      * private void methodA() {
      * }
      * 
      * private void methodB() {
      * }
      * </pre>
      * 
      * In the example above, <b>removeMales()</b> is a conceptual integrated
      * code logic, so it needs to close the session at its end. While
      * <b>methodA()</b> and <b>methodB()</b> is just a method component during
      * <b>removeMales()</b>, so they have no need to close the session.
      */
     public void close();
     
 }
