 import java.util.HashMap;
 import java.util.LinkedList;
 
 import org.junit.Test;
 
 import play.libs.Crypto;
 import play.mvc.Controller;
 import play.mvc.Http.StatusCode;
 import play.test.UnitTest;
 import api.requests.*;
 import api.responses.*;
 import api.entities.*;
 import controllers.APIClient;
 
 public class APIClientTest extends UnitTest {
 	
 	public static void failIfNotSuccessful(Response response) {
     	if(!StatusCode.success(response.statusCode)) {
     		if(response.error_message == null) {
     			response.error_message = "No error message from service.";
     		}
     		fail("did not get the HTTP-OK status-code from the service, got "+response.statusCode+": "+response.error_message);
 		}
 	}
 	
 	@Test
     public void testLogin() throws Exception {
 		APIClient apiClient = new APIClient((long) 1, "openarms");
 		
     	PollJSON pj1 = new PollJSON();
     	pj1.question = "This is the first question.";
     	CreatePollResponse response1 = (CreatePollResponse) apiClient.sendRequest(new CreatePollRequest(pj1));
     	failIfNotSuccessful(response1);
     	assertEquals(pj1.question, response1.poll.question);
     	assertNotNull(response1.poll.id);
     	
     	PollJSON pj2 = new PollJSON();
     	pj2.id = (long) response1.poll.id;
     	pj2.question = "This is a new question: "+Math.random();
     	UpdatePollResponse response2 = (UpdatePollResponse) apiClient.sendRequest(new UpdatePollRequest(pj2));
     	failIfNotSuccessful(response2);
     	assertEquals(pj2.question, response2.poll.question);
     }
/*
     @Test
     public void testCreateChoice() throws Exception {
     	ChoiceJSON c = new ChoiceJSON();
     	c.text = "choice text";
     	CreateChoiceResponse response = (CreateChoiceResponse) APIClient.send(new CreateChoiceRequest(c));
     	failIfNotSuccessful(response);
     	assertNotNull(response.choice.id);
     	assertEquals(response.choice.text, c.text);
     }
     
     @Test
     public void testCreatePollInstance() throws Exception {
     	PollInstanceJSON p = new PollInstanceJSON();
     	p.poll_id = (long) 1;
     	CreatePollInstanceResponse response = (CreatePollInstanceResponse) APIClient.send(new CreatePollInstanceRequest(p));
     	failIfNotSuccessful(response);
     	assertNotNull(response.pollinstance.id);
     	assertEquals(response.pollinstance.poll_id, p.poll_id);
     }
  
 	@Test
     public void testCreateUser() throws Exception {
 		UserJSON u = new UserJSON();
 		u.name = "test";
 		u.email = "avas@dfsdf.com";
 		u.secret = null;
 		u.attributes = new HashMap<String, String>();
 		u.attributes.put("password", "1234");
 
 		CreateUserResponse response = (CreateUserResponse) APIClient.send(new CreateUserRequest(u));
 
     	failIfNotSuccessful(response);
     	assertNotNull(response.user.id);
     	assertEquals(response.user.name, u.name);
    }  */
 	/*
     @Test
     public void testCreatePoll() throws Exception {
     	PollJSON p = new PollJSON();
     	ChoiceJSON c = new ChoiceJSON();
     	c.text = "TestChoice";
     	p.question = "poll question";
     	p.reference = "Test";
     	p.choices = new LinkedList();
     	p.choices.add(c);
     	CreatePollResponse response = (CreatePollResponse) APIClient.send(new CreatePollRequest(p));
     	failIfNotSuccessful(response);
     	assertNotNull(response.poll.id);
     	assertEquals(response.poll.question, p.question);
     }
     
     @Test
     public void testRetrievePoll() throws Exception {
     	// IS NEEDED TO CREATE POLL IN ADMIN TO WORKING TEST PROPERLY !!! :)
     	PollJSON p = new PollJSON();
     	Long a = 1L;
     	p.id = a;
     	ReadPollResponse response =  (ReadPollResponse) APIClient.send(new ReadPollRequest(p.id));
     	failIfNotSuccessful(response);
     	assertNotNull(response.poll.id);
     	assertEquals(response.poll.id,a);
     }
     */
     /*
     @Test
     public void testCreateVote() throws Exception {
     	VoteJSON v = new VoteJSON();
     	v.choiceid = (long) 1;
     	v.pollInstanceid = (long) 1;
     	CreateVoteResponse response = (CreateVoteResponse) APIClient.send(new CreateVoteRequest(v));
     	failIfNotSuccessful(response);
     	assertNotNull(response.vote.id);
     	assertEquals(response.vote.choiceid, v.choiceid);
     }*/
 }
