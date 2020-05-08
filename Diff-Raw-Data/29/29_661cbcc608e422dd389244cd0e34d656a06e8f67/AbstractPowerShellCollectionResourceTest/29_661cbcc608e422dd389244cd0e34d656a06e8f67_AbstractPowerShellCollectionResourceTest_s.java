 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.powershell.resource;
 
 import java.net.URI;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Executor;
 
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.core.UriBuilder;
 
 import com.redhat.rhevm.api.model.BaseResource;
 import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
 import com.redhat.rhevm.api.common.resource.AbstractUpdatableResource;
 import com.redhat.rhevm.api.common.util.QueryHelper;
 import com.redhat.rhevm.api.common.util.ReflectionHelper;
 
 import com.redhat.rhevm.api.powershell.util.ControllableExecutor;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPool;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.runner.RunWith;
 
 import static org.easymock.classextension.EasyMock.expect;
 
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.mockStatic;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 import static org.powermock.api.easymock.PowerMock.verifyAll;
 
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( { PowerShellCmd.class })
 public abstract class AbstractPowerShellCollectionResourceTest<R extends BaseResource,
                                                                U extends AbstractUpdatableResource<R>,
                                                                T extends AbstractPowerShellCollectionResource<R, U>>
     extends BasePowerShellResourceTest {
 
     protected static final String[] NAMES = {"sedna", "eris", "orcus"};
     protected static final String[] NAMES_SUBSET = {"eris", "orcus"};
     protected static final String NEW_NAME = "ceres";
     protected static final String[] DESCRIPTIONS = {"first", "second", "third"};
     protected static final String[] DESCRIPTIONS_SUBSET = {"first", "second"};
     protected static final String NEW_DESCRIPTION = "Newbie right here!";
 
     private static final String SELECT_COMMAND = "select-{0}";
 
 
     private static final String QUERY_RETURN =
         "{0}id: " + "eris".hashCode() + " \n name: eris {1}\n\n" +
         "{0}id: " + "orcus".hashCode() + " \n name: orcus {1}";
 
     private static final String ADD_COMMAND =
         "add-{0}{1} -name \"" + NEW_NAME +
                "\" -description \"" + NEW_DESCRIPTION + "\" ";
 
     private static final String REMOVE_COMMAND = "remove-{0} -{0}id \"" + "eris".hashCode() + "\"";
 
     private static final String SEARCH_OPTION = " -searchtext ";
 
     protected static final String[] NOTHING = null;
 
     protected T resource;
     protected U updatable;
     protected String collectionName;
     protected String individualName;
     protected String[] extraArgs;
     protected Executor executor;
     protected PowerShellParser parser;
 
     protected AbstractPowerShellCollectionResourceTest(U updatable,
                                                        String collectionName,
                                                        String individualName,
                                                        String[] extraArgs) {
         this.updatable = updatable;
         this.collectionName = collectionName;
         this.individualName = individualName;
         this.extraArgs = extraArgs;
     }
 
     @Before
     public void setUp() throws Exception {
         executor = new ControllableExecutor();
         parser = PowerShellParser.newInstance();
         resource = getResource();
         resource.setExecutor(executor);
         resource.setParser(parser);
     }
 
     @After
     public void tearDown() {
         verifyAll();
     }
 
     protected UriInfo setUpResourceExpectations(String command, String ret, QueryParam query, String ... names) throws Exception {
         return setUpResourceExpectations(asArray(command), asArray(ret), false, query, names);
     }
 
     protected UriInfo setUpResourceExpectations(String command, String ret, String ... names) throws Exception {
         return setUpResourceExpectations(asArray(command), asArray(ret), false, null, names);
     }
 
     protected UriInfo setUpAddResourceExpectations(String command, String ret, String name) throws Exception {
         return setUpAddResourceExpectations(asArray(command), asArray(ret), name);
     }
 
     protected UriInfo setUpAddResourceExpectations(String[] commands, String[] rets, String name) throws Exception {
         return setUpResourceExpectations(commands, rets, true, null, name);
     }
 
     protected UriInfo setUpResourceExpectations(String[] commands, String[] rets, QueryParam query, String ... names) throws Exception {
         return setUpResourceExpectations(commands, rets, false, query, names);
     }
 
     @SuppressWarnings("unchecked")
     protected UriInfo setUpResourceExpectations(String[] commands, String[] rets, boolean add, QueryParam query, String ... names) throws Exception {
         if (commands != null) {
             mockStatic(PowerShellCmd.class);
             for (int i = 0 ; i < Math.min(commands.length, rets.length) ; i++) {
                 if (commands[i] != null) {
                     expect(PowerShellCmd.runCommand(setUpPoolExpectations(), commands[i])).andReturn(rets[i]);
                 }
             }
         }
         UriInfo uriInfo = createMock(UriInfo.class);
         if (query != null) {
             MultivaluedMap<String, String> queries = createMock(MultivaluedMap.class);
             List<String> queryParam = new ArrayList<String>();
             queryParam.add(query.value());
             expect(queries.get("search")).andReturn(queryParam).anyTimes();
             expect(uriInfo.getQueryParameters()).andReturn(queries).anyTimes();
         } else {
             expect(uriInfo.getQueryParameters()).andReturn(null).anyTimes();
         }
         if (add) {
             String href = URI_ROOT + SLASH + collectionName + SLASH + names[0].hashCode();
             UriBuilder uriBuilder = createMock(UriBuilder.class);
             expect(uriInfo.getAbsolutePathBuilder()).andReturn(uriBuilder);
             expect(uriBuilder.path(Integer.toString(names[0].hashCode()))).andReturn(uriBuilder);
             expect(uriBuilder.build()).andReturn(new URI(href)).anyTimes();
         }
         replayAll();
         return uriInfo;
     }
 
     @SuppressWarnings("unchecked")
     protected UriInfo setUpResourceExpectations(int shells, String[] commands, String[] rets, boolean add, QueryParam query, String ... names) throws Exception {
         if (commands != null) {
             mockStatic(PowerShellCmd.class);
             PowerShellPool pool = setUpPoolExpectations(shells);
             for (int i = 0 ; i < Math.min(commands.length, rets.length) ; i++) {
                 if (commands[i] != null) {
                     expect(PowerShellCmd.runCommand(pool, commands[i])).andReturn(rets[i]);
                 }
             }
         }
         UriInfo uriInfo = createMock(UriInfo.class);
         if (query != null) {
             MultivaluedMap<String, String> queries = createMock(MultivaluedMap.class);
             List<String> queryParam = new ArrayList<String>();
             queryParam.add(query.value());
             expect(queries.get("search")).andReturn(queryParam).anyTimes();
             expect(uriInfo.getQueryParameters()).andReturn(queries).anyTimes();
         } else {
             expect(uriInfo.getQueryParameters()).andReturn(null).anyTimes();
         }
         if (add) {
             String href = URI_ROOT + SLASH + collectionName + SLASH + names[0].hashCode();
             UriBuilder uriBuilder = createMock(UriBuilder.class);
             expect(uriInfo.getAbsolutePathBuilder()).andReturn(uriBuilder);
             expect(uriBuilder.path(Integer.toString(names[0].hashCode()))).andReturn(uriBuilder);
             expect(uriBuilder.build()).andReturn(new URI(href)).anyTimes();
         }
         replayAll();
         return uriInfo;
     }
 
     protected PowerShellPool setUpPoolExpectations() {
         return setUpPoolExpectations(1);
     }
 
     protected PowerShellPool setUpPoolExpectations(int times) {
         PowerShellPoolMap poolMap = createMock(PowerShellPoolMap.class);
         resource.setPowerShellPoolMap(poolMap);
         PowerShellPool pool = createMock(PowerShellPool.class);
         expect(poolMap.get()).andReturn(pool).times(times);
         return pool;
     }
 
     protected String getSelectCommand() {
         return MessageFormat.format(SELECT_COMMAND, individualName);
     }
 
     protected String getSelectReturn() {
         return formatXmlReturn(individualName, NAMES, DESCRIPTIONS, extraArgs);
     }
 
     protected String getQueryReturn() {
         return formatXmlReturn(individualName, NAMES_SUBSET, DESCRIPTIONS_SUBSET, extraArgs);
     }
 
     protected String getAddReturn() {
         return formatXmlReturn(individualName,
                                new String[] { NEW_NAME },
                                new String[] { NEW_DESCRIPTION },
                                extraArgs);
     }
 
     protected String getQueryCommand(Class<?> clz) {
         return getSelectCommand() + SEARCH_OPTION + "\"" + QueryHelper.RETURN_TYPES.get(clz) + QUERY + "\"";
     }
 
     protected String getAddCommand(boolean async) {
         return MessageFormat.format(ADD_COMMAND, individualName, async ? " -async" : "");
     }
 
     protected String getAddCommand() {
         return getAddCommand(false);
     }
 
     protected String getRemoveCommand() {
         return MessageFormat.format(REMOVE_COMMAND, individualName);
     }
 
     protected R getModel(String name, String description) {
         R model = ReflectionHelper.newModel(updatable);
         model.setId(Integer.toString(name.hashCode()));
         model.setName(name);
         model.setDescription(description);
         populateModel(model);
         return model;
     }
 
     protected void verifyResponse(Response r, String name, String description) {
         verifyResponse(r, name, description, collectionName);
     }
 
     protected void verifyCollection(List<R> collection, String[] names, String[] descriptions) {
         assertNotNull(collection);
         assertEquals("unexpected collection size", collection.size(), names.length);
         for (int i = 0; i < names.length; i++) {
             R model = collection.remove(0);
             assertEquals(Integer.toString(names[i].hashCode()), model.getId());
             assertEquals(names[i], model.getName());
             assertEquals(descriptions[i], model.getDescription());
         }
     }
 
     protected void verifyResource(AbstractActionableResource<R> resource, String name) {
         assertNotNull(resource);
         assertEquals(resource.getId(), Integer.toString(name.hashCode()));
         assertSame(resource.getExecutor(), executor);
     }
 
     protected abstract T getResource();
 
     protected abstract void populateModel(R model);
 }
 
