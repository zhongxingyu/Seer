 package testcases;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import org.junit.Test;
 
 import elfville.protocol.*;
 import elfville.protocol.Response.Status;
 import elfville.protocol.models.SerializablePost;
 
 public class CentralBoardTest extends TestBase {
 
 	@Test
 	// clientNum clients create clientNum posts. Check if all posts succeeded.
 	public void test1post() throws IOException {
 		for (int i = 0; i < clientNum; i++) {
 			String title = "title-" + i;
 			String content = "content-" + i;
 			PostCentralBoardRequest req = new PostCentralBoardRequest(title, content);
 			Response resp = socketControllers.get(i).send(req);
 			// System.out.println("test1post() " + i + " " + resp.status.toString());
 			assertEquals(resp.status, Status.SUCCESS);
 		}
 	}
 
 	/**
 	 * Test that all submitted posts can be retrieved.
 	 * @throws IOException
 	 */
 	@Test
 	// One single client gets all posts from the central board. The returned posts should be the
 	// same as those that were inserted earlier, ordered by created dates.
 	public void test2get() throws IOException {
 		CentralBoardRequest req = new CentralBoardRequest();
 		CentralBoardResponse resp = socketControllers.get(0).send(req);
 		assertEquals(resp.status, Status.SUCCESS);
 
 		for (int i = 0; i < clientNum; i++) {
 			SerializablePost post = resp.posts.get(clientNum - i - 1);  // the latest comes first
 			System.out.println("get " + i);
 			System.out.println(post.title);
 			System.out.println(post.content);
 			assertEquals("title-" + i, post.title);
 			assertEquals("content-" + i, post.content);
 		}
 
 	}
 	
 	@Test
 	public void testVoting() throws IOException {
 		// make post
 		PostCentralBoardRequest req = new PostCentralBoardRequest("a message",
 				"a title");
 		socketControllers.get(0).send(req);
 		
 		// get posts
 		CentralBoardRequest req2 = new CentralBoardRequest();
 		CentralBoardResponse resp2 = socketControllers.get(0).send(req2);
 		
 		// ensure you can vote
 		String postA = resp2.posts.get(0).modelID;
 		VoteRequest voteReq = new VoteRequest(postA, true);
 		Response voteResp = socketControllers.get(0).send(voteReq);
 		assertTrue(voteResp.isOK());
 		
 		// ensure you cannot vote on same post twice
 		Response voteResp2 = socketControllers.get(0).send(voteReq);
 		assertFalse(voteResp2.isOK());
 	}
 
 	@Test
 	// A single client votes either Upsock or Downsock on posts. 
 	// Check if SUCCESS is returned. Check if the returned posts get
 	// the right number of upsock/downsock.
 	public void test3SingleVote() throws IOException {
 		CentralBoardRequest req = new CentralBoardRequest();
 		CentralBoardResponse resp = socketControllers.get(0).send(req);
 		assertEquals(resp.status, Status.SUCCESS);
 
 		for (int i = 0; i < clientNum; i++) {
 			SerializablePost post = resp.posts.get(i);
 			VoteRequest voteReq = new VoteRequest(post.modelID, i / 2 == 0);
 			Response voteRes = socketControllers.get(0).send(req);
 			assertEquals(voteRes.status, Status.SUCCESS);
 		}
 
 		resp = socketControllers.get(0).send(req);
 		assertEquals(resp.status, Status.SUCCESS);
 
 		for (int i = 0; i < clientNum; i++) {
 			SerializablePost post = resp.posts.get(i);
 			if (i / 2 == 0) {
 				assertEquals(1, post.upvotes);
 			} else {
 				assertEquals(1, post.downvotes);
 			}
 		}
 	}	
 
 	@Test
 	// Test if the same client can vote twice
 	public void test4VoteTwice() throws IOException {
 		CentralBoardRequest req = new CentralBoardRequest();
 		CentralBoardResponse resp = socketControllers.get(0).send(req);
 		assertEquals(resp.status, Status.SUCCESS);
 
 		for (int i = 0; i < clientNum; i++) {
 			SerializablePost post = resp.posts.get(i);
 			VoteRequest voteReq = new VoteRequest(post.modelID, i / 2 == 0);
 			Response voteRes = socketControllers.get(0).send(req);
 			assertEquals(voteRes.status, Status.FAILURE);
 		}
 
 
 		for (int i = 0; i < clientNum; i++) {
 			SerializablePost post = resp.posts.get(i);
 			VoteRequest voteReq = new VoteRequest(post.modelID, i / 2 != 0);
 			Response voteRes = socketControllers.get(0).send(req);
 			assertEquals(voteRes.status, Status.FAILURE);
 		}
 
 	}
 
 	@Test
 	// Clients delete their votes. Check SUCCEED
 	public void test5DeleteVote() throws IOException {
 		CentralBoardRequest req = new CentralBoardRequest();
 		CentralBoardResponse resp = socketControllers.get(0).send(req);
 		assertEquals(resp.status, Status.SUCCESS);
 
 		for (int i = 0; i < clientNum; i++) {
 			SerializablePost post = resp.posts.get(i);
			DeleteCentralBoardRequest deleteReq = new DeleteCentralBoardRequest();
			deleteReq.post = post;
 			Response deleteRes = socketControllers.get(i).send(deleteReq);
 			assertEquals(deleteRes.status, Status.SUCCESS);
 		}
 	}
 	
 	@Test
 	// Multiple clients vote on different posts. Check if the returned posts are ordered correctly.
 	public void test6VoteOrder() throws IOException {
 		test1post();
 
 		CentralBoardRequest req = new CentralBoardRequest();
 		CentralBoardResponse resp = socketControllers.get(0).send(req);
 		assertEquals(resp.status, Status.SUCCESS);
 		
 		// Vote in the descending order, i.e. vote clientNum gets 
 		for (int i = 0; i < clientNum; i++) {
 			for (int k = i; k < clientNum; k++) {
 				VoteRequest voteReq = new VoteRequest(resp.posts.get(k).modelID, true);
 				Response voteRes = socketControllers.get(i).send(req);
 				assertEquals(voteRes.status, Status.SUCCESS);
 			}
 		}
 		
 		req = new CentralBoardRequest();
 		resp = socketControllers.get(0).send(req);
 		assertEquals(resp.status, Status.SUCCESS);
 
 		for (int i = 0; i < clientNum; i++) {
 			SerializablePost post = resp.posts.get(i);  // reordered now. the first comes first
 			System.out.println(post.title);
 			System.out.println(post.content);
 			assertEquals("title-" + i, post.title);
 			assertEquals("content-" + i, post.content);
 		}
 		
 		
 	}
 }
