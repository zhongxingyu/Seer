 package net.ripe.db.whois.api.whois;
 
 import net.ripe.db.whois.api.AbstractRestClientTest;
 import net.ripe.db.whois.api.httpserver.Audience;
 import net.ripe.db.whois.common.IntegrationTest;
 import net.ripe.db.whois.common.domain.IpRanges;
 import net.ripe.db.whois.common.rpsl.RpslObject;
 import net.ripe.db.whois.update.mail.MailSenderStub;
 import org.glassfish.jersey.media.multipart.FormDataMultiPart;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 import javax.ws.rs.BadRequestException;
 import javax.ws.rs.ForbiddenException;
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.MediaType;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 
 @Category(IntegrationTest.class)
 public class SyncUpdatesServiceTestIntegration extends AbstractRestClientTest {
 
     private static final Audience AUDIENCE = Audience.PUBLIC;
 
     private static final String MNTNER_TEST_MNTNER =
             "mntner:        mntner\n" +
             "descr:         description\n" +
             "admin-c:       TP1-TEST\n" +
             "upd-to:        noreply@ripe.net\n" +
             "notify:        noreply@ripe.net\n" +
             "auth:          MD5-PW $1$TTjmcwVq$zvT9UcvASZDQJeK8u9sNU.    # emptypassword\n" +
             "mnt-by:        mntner\n" +
             "referral-by:   mntner\n" +
             "changed:       noreply@ripe.net 20120801\n" +
             "source:        TEST";
 
     private static final String PERSON_ANY1_TEST =
             "person:        Test Person\n" +
             "nic-hdl:       TP1-TEST\n" +
             "source:        TEST";
 
     @Autowired private MailSenderStub mailSender;
     @Autowired private IpRanges ipRanges;
 
     @Test
     public void empty_request() throws Exception {
         try {
             createResource(AUDIENCE, "whois/syncupdates/test")
                     .request()
                     .get(String.class);
             fail();
         } catch (BadRequestException e) {
             // expected
         }
     }
 
     @Test
     public void get_help_parameter_only() throws Exception {
         String response = createResource(AUDIENCE, "whois/syncupdates/test?HELP=yes")
                     .request()
                     .get(String.class);
 
         assertThat(response, containsString("You have requested Help information from the RIPE NCC Database"));
         assertThat(response, containsString("From-Host: 127.0.0.1"));
         assertThat(response, containsString("Date/Time: "));
         assertThat(response, not(containsString("$")));
     }
 
     @Test
     public void post_multipart_form_help_parameter_only() {
         final FormDataMultiPart multipart = new FormDataMultiPart().field("HELP", "help");
         String response = createResource(AUDIENCE, "whois/syncupdates/test")
                 .request()
                 .post(Entity.entity(multipart, multipart.getMediaType()), String.class);
 
         assertThat(response, containsString("You have requested Help information from the RIPE NCC Database"));
     }
 
     @Test
     public void post_url_encoded_form_help_parameter_only() {
         String response = createResource(AUDIENCE, "whois/syncupdates/test")
                 .request()
                 .post(Entity.entity("HELP=yes", MediaType.APPLICATION_FORM_URLENCODED), String.class);
 
         assertThat(response, containsString("You have requested Help information from the RIPE NCC Database"));
     }
 
     @Test
     public void help_and_invalid_parameter() throws Exception {
         String response = createResource(AUDIENCE, "whois/syncupdates/test?HELP=yes&INVALID=true")
                     .request()
                     .get(String.class);
 
         assertThat(response, containsString("You have requested Help information from the RIPE NCC Database"));
     }
 
     @Test
     public void diff_parameter_only() throws Exception {
         try {
             createResource(AUDIENCE, "whois/syncupdates/test?DIFF=yes")
                     .request()
                     .get(String.class);
         } catch (BadRequestException e) {
             assertThat(e.getResponse().readEntity(String.class), containsString("Invalid request"));
         }
     }
 
     @Test
     public void redirect_not_allowed() throws Exception {
         ipRanges.setTrusted();
         rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
 
         try {
             createResource(AUDIENCE, "whois/syncupdates/test?" + "REDIRECT=yes&DATA=" + encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword"))
                     .request()
                     .get(String.class);
             fail();
         } catch (ForbiddenException e) {
             final String response = e.getResponse().readEntity(String.class);
             assertThat(response, not(containsString("Create SUCCEEDED: [mntner] mntner")));
             assertThat(response, containsString("Not allowed to disable notifications: 127.0.0.1"));
         }
     }
 
     @Test
     public void redirect_allowed() throws Exception {
         ipRanges.setTrusted("0/0", "::0/0");
         rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
 
         String response = createResource(AUDIENCE, "whois/syncupdates/test?" + "REDIRECT=yes&DATA=" + encode(MNTNER_TEST_MNTNER + "\nremarks: updated" + "\npassword: emptypassword"))
                     .request()
                     .get(String.class);
 
         assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
         assertThat(response, not(containsString("Not allowed to disable notifications: 127.0.0.1\n")));
         assertFalse(anyMoreMessages());
     }
 
     @Test
     public void notify_without_redirect() throws Exception {
         rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
         rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));
 
         String response = createResource(AUDIENCE, "whois/syncupdates/test?" + "DATA=" + encode(MNTNER_TEST_MNTNER + "\nremarks: updated" + "\npassword: emptypassword"))
                     .request()
                     .get(String.class);
 
         assertThat(response, containsString("Modify SUCCEEDED: [mntner] mntner"));
 
         assertNotNull(getMessage("noreply@ripe.net"));
         assertThat(anyMoreMessages(), is(false));
     }
 
     @Test
     public void only_data_parameter_create_object() throws Exception {
         rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
 
         String response = createResource(AUDIENCE, "whois/syncupdates/test?" + "DATA=" + encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword"))
                     .request()
                     .get(String.class);
 
         assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
     }
 
     @Test
    public void create_object_invalid_source_in_url() throws Exception {
        try {
            createResource(AUDIENCE, "whois/syncupdates/invalid?DATA=" + encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword"))
                    .request()
                    .get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), containsString("Invalid source specified: invalid"));
        }
    }

    @Test
    public void create_object_invalid_source_in_data() throws Exception {
         rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
        final String mntnerInvalidSource = MNTNER_TEST_MNTNER.replaceAll("source:\\s+TEST", "source: invalid");

        System.out.println("mntner = " + mntnerInvalidSource);
 
         String response = createResource(AUDIENCE, "whois/syncupdates/test?" + "DATA=" + encode(mntnerInvalidSource + "\npassword: emptypassword"))
                     .request()
                     .get(String.class);
 
         assertThat(response, containsString("Error:   Unrecognized source: invalid"));
     }
 
     @Test
     public void only_data_parameter_update_object() throws Exception {
         rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
         rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));
 
         String response = createResource(AUDIENCE, "whois/syncupdates/test?" + "DATA=" + encode(MNTNER_TEST_MNTNER + "\nremarks: new" + "\npassword: emptypassword"))
                     .request()
                     .get(String.class);
 
         assertThat(response, containsString("Modify SUCCEEDED: [mntner] mntner"));
     }
 
     @Test
     public void only_new_parameter() throws Exception {
         try {
             createResource(AUDIENCE, "whois/syncupdates/test?NEW=yes")
                     .request()
                     .get(String.class);
         } catch (BadRequestException e) {
             assertThat(e.getResponse().readEntity(String.class), containsString("DATA parameter is missing"));
         }
     }
 
     @Test
     public void new_and_data_parameters_get_request() throws Exception {
         rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
 
         String response = createResource(AUDIENCE, "whois/syncupdates/test?" + "DATA=" + encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword") + "&NEW=yes")
                 .request()
                 .get(String.class);
 
         assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
     }
 
     @Test
     public void new_and_data_parameters_existing_object_get_request() throws Exception {
         rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
         rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));
 
         String response = createResource(AUDIENCE, "whois/syncupdates/test?" + "DATA=" + encode(MNTNER_TEST_MNTNER + "\nremarks: new" + "\npassword: emptypassword") + "&NEW=yes")
                 .request()
                 .get(String.class);
 
         assertThat(response, containsString(
                 "***Error:   Enforced new keyword specified, but the object already exists in the\n" +
                 "            database"));
     }
 
     @Test
     public void new_and_data_parameters_urlencoded_post_request() throws Exception {
         rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
 
         String response = createResource(AUDIENCE, "whois/syncupdates/test")
                 .request()
                 .post(Entity.entity("DATA=" + encode(MNTNER_TEST_MNTNER + "\npassword: emptypassword") + "&NEW=yes", MediaType.APPLICATION_FORM_URLENCODED), String.class);
 
         assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
     }
 
     @Test
     public void new_and_data_parameters_multipart_post_request() throws Exception {
         rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
 
         final FormDataMultiPart multipart = new FormDataMultiPart().field("DATA", MNTNER_TEST_MNTNER + "\npassword: emptypassword").field("NEW", "yes");
         String response = createResource(AUDIENCE, "whois/syncupdates/test")
                 .request()
                 .post(Entity.entity(multipart, multipart.getMediaType()), String.class);
 
         assertThat(response, containsString("Create SUCCEEDED: [mntner] mntner"));
     }
 
     @Test
     public void post_url_encoded_data_with_charset() throws Exception {
         rpslObjectUpdateDao.createObject(RpslObject.parse(PERSON_ANY1_TEST));
         rpslObjectUpdateDao.createObject(RpslObject.parse(MNTNER_TEST_MNTNER));
 
         String response = createResource(AUDIENCE, "whois/syncupdates/test")
                 .request()
                 .post(Entity.entity("DATA=" + encode(
                         "person:     Test Person\n" +
                         "address:    Flughafenstraße 109/a\n" +
                         "phone:      +49 282 411141\n" +
                         "fax-no:     +49 282 411140\n" +
                         "nic-hdl:    TP1-TEST\n" +
                         "changed:    dbtest@ripe.net 20120101\n" +
                         "mnt-by:     mntner\n" +
                         "source:     INVALID\n" +
                         "password: emptypassword", "ISO-8859-1"), MediaType.valueOf("application/x-www-form-urlencoded; charset=ISO-8859-1")), String.class);
 
         assertThat(response, containsString("***Error:   Unrecognized source: INVALID"));
         assertThat(response, containsString("Flughafenstraße 109/a"));
     }
 
     // helper methods
 
     private MimeMessage getMessage(final String to) throws MessagingException {
         return mailSender.getMessage(to);
     }
 
     private boolean anyMoreMessages() {
         return mailSender.anyMoreMessages();
     }
 
     @Override
     protected WebTarget createResource(final Audience audience, final String path) {
         return client.target(String.format("http://localhost:%s/%s", getPort(audience), path));
     }
 }
