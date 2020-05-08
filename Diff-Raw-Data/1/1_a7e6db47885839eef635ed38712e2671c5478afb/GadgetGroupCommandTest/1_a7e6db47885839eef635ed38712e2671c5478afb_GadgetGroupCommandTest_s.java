 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.gadgetcontainer.application;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 import static org.easymock.classextension.EasyMock.*;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.StringWriter;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.globant.katari.hibernate.coreuser.domain.CoreUser;
 import com.globant.katari.gadgetcontainer.application.TokenService;
 
 import com.globant.katari.shindig.domain.Application;
 import com.globant.katari.gadgetcontainer.domain.SampleUser;
 
 import com.globant.katari.gadgetcontainer.domain.GadgetGroup;
 import com.globant.katari.gadgetcontainer.domain.ContextUserService;
 import com.globant.katari.gadgetcontainer.domain.GadgetInstance;
 import com.globant.katari.gadgetcontainer.domain.GadgetGroupRepository;
 
 public class GadgetGroupCommandTest {
 
   private TokenService tokenService;
 
   private String gadgetXmlUrl = "file:///" + new File(
       "target/test-classes/SampleGadget.xml").getAbsolutePath();
 
   private CoreUser user = new SampleUser("me");
 
   @Before
   public void setUp() throws Exception {
     tokenService = createMock(TokenService.class);
     expect(tokenService.createSecurityToken(eq(0L), eq(0L),
         isA(GadgetInstance.class))).andReturn("mockToken").anyTimes();
     replay(tokenService);
   }
 
   @Test
   public void testExecute_nullGroup() {
     GadgetGroupCommand command = new GadgetGroupCommand(
         createMock(GadgetGroupRepository.class),
         createMock(ContextUserService.class),
         createMock(TokenService.class));
     try {
       command.execute();
       fail("should fail because we never set the groupName command property");
     } catch (Exception e) {
     }
   }
 
   @Test
   public void testExecute() throws Exception {
 
     String groupName = "theGroup";
 
     GadgetGroup gadgetGroup = new GadgetGroup(user, groupName, 3);
     Application application = new Application(gadgetXmlUrl);
     GadgetInstance gadgetInstance = new GadgetInstance(application, 1, 2);
     gadgetGroup.add(gadgetInstance);
 
     GadgetGroupRepository repository = createMock(GadgetGroupRepository.class);
     expect(repository.findGadgetGroup(0, groupName)).andReturn(gadgetGroup);
     replay(repository);
 
     ContextUserService userService = createMock(ContextUserService.class);
     expect(userService.getCurrentUser()).andReturn(user);
     replay(userService);
 
     GadgetGroupCommand command;
     command = new GadgetGroupCommand(repository, userService, tokenService);
     command.setGroupName(groupName);
 
     assertThat(command.execute().write(new StringWriter()).toString(),
         is(baselineJson(true)));
 
     verify(userService);
     verify(repository);
   }
 
   @Test
   public void testExecute_staticGroup() throws Exception {
     String groupName = "theGroup";
 
     GadgetGroup gadgetGroup = new GadgetGroup(null, groupName, 3);
     Application application = new Application(gadgetXmlUrl);
     GadgetInstance gadgetInstance = new GadgetInstance(application, 1, 2);
     gadgetGroup.add(gadgetInstance);
 
     GadgetGroupRepository repository = createMock(GadgetGroupRepository.class);
     expect(repository.findGadgetGroup(0, groupName)).andReturn(gadgetGroup);
     replay(repository);
 
     ContextUserService userService = createMock(ContextUserService.class);
     expect(userService.getCurrentUser()).andReturn(user);
     replay(userService);
 
     GadgetGroupCommand command;
     command = new GadgetGroupCommand(repository, userService, tokenService);
     command.setGroupName(groupName);
 
     assertThat(command.execute().write(new StringWriter()).toString(),
         is(baselineJson(false)));
 
     verify(userService);
     verify(repository);
   }
 
   @Test
   public void testExecute_createFromTemplate() throws Exception {
     String groupName = "theGroup";
 
     // A group template
     GadgetGroup gadgetGroup = new GadgetGroup(groupName, 3);
     Application application = new Application(gadgetXmlUrl);
     GadgetInstance gadgetInstance = new GadgetInstance(application, 1, 2);
     gadgetGroup.add(gadgetInstance);
 
     GadgetGroupRepository repository = createMock(GadgetGroupRepository.class);
     expect(repository.findGadgetGroup(0, groupName)).andReturn(null);
     expect(repository.findGadgetGroupTemplate(groupName))
       .andReturn(gadgetGroup);
     repository.save(isA(GadgetGroup.class));
     replay(repository);
 
     ContextUserService userService = createMock(ContextUserService.class);
     expect(userService.getCurrentUser()).andReturn(user);
     replay(userService);
 
     GadgetGroupCommand command;
     command = new GadgetGroupCommand(repository, userService, tokenService);
     command.setGroupName(groupName);
 
     assertThat(command.execute().write(new StringWriter()).toString(),
         is(baselineJson(true)));
 
     verify(userService);
     verify(repository);
   }
 
   /** Creates the baseline json string, a string with a sample json object.
    *
    * @return the json string.
    *
    * @throws JSONException
    */
   private String baselineJson(final boolean isCustomizable)
     throws JSONException {
     try {
       JSONObject groupJson = new JSONObject();
       groupJson.put("id", 0);
       groupJson.put("name", "theGroup");
       groupJson.put("ownerId", 0);
       groupJson.put("viewerId", 0);
       groupJson.put("numberOfColumns", 3);
       groupJson.put("customizable", isCustomizable);
 
       JSONObject gadgetJson = new JSONObject();
       gadgetJson.put("id", 0);
      gadgetJson.put("icon", "");
       gadgetJson.put("title", "Test title");
       gadgetJson.put("appId", 0);
       gadgetJson.put("column", 1);
       gadgetJson.put("order", 2);
       gadgetJson.put("url", gadgetXmlUrl);
       gadgetJson.put("icon", "");
       gadgetJson.put("securityToken", "mockToken");
       groupJson.append("gadgets", gadgetJson);
       return groupJson.toString();
     } catch(JSONException e) {
       throw new RuntimeException("Error generating json", e);
     }
   }
 }
 
