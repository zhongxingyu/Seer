 package org.jabox.scm.github;
 
import junit.framework.Assert;
 import junit.framework.TestCase;
 
 public class GitHubFacadeTest extends TestCase {
 
	public void testLogin() {
		Assert.assertFalse(GitHubFacade.validateLogin("user", "wrong"));
		Assert.assertTrue(GitHubFacade.validateLogin("user", "correct"));
 
	}
 }
