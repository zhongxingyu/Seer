 package com.aneesh.gae.domain.service;
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.classextension.EasyMock.createMock;
 import static org.easymock.classextension.EasyMock.replay;
 import static org.easymock.classextension.EasyMock.verify;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Test;
 
 import com.aneesh.gae.domain.QuickThought;
 import com.aneesh.gae.domain.Tag;
 import com.aneeshpu.gae.domain.repository.ThoughtRepository;
 import com.aneeshpu.gae.domain.service.Mind;
 
 public class MindTest {
 
 	@Test
	public void should_be_capable_of_thinking_new_thoughts_tagged_with_existing_tags() throws Exception {
 		
 		ThoughtRepository thoughtRepositoryMock = createMock(ThoughtRepository.class);
 		String aCapableMind = "a capable mind";
 		
 		Tag mindTag = new Tag("mind");
 		expect(thoughtRepositoryMock.find(mindTag)).andReturn(mindTag);
 		
 		replay(thoughtRepositoryMock);
 		
 		Mind mind = new Mind(thoughtRepositoryMock);
 		QuickThought thought = mind.think(aCapableMind, "mind");
 		
 		assertTrue(thought.isTaggedWith(mindTag));
 		verify(thoughtRepositoryMock);
 	}
 	
 	@Test
 	public void should_remember_all_the_thoughts_I_have_had() throws Exception {
 
 		ThoughtRepository thoughtRepositoryMock = createMock(ThoughtRepository.class);
 		ArrayList<QuickThought> allMyThoughts = new ArrayList<QuickThought>();
 		QuickThought firstThought = new QuickThought("first thought", "first");
 		allMyThoughts.add(firstThought);
 		QuickThought secondThought = new QuickThought("second thought", "second");
 		allMyThoughts.add(secondThought);
 		
 		expect(thoughtRepositoryMock.allMyThoughts()).andReturn(allMyThoughts);
 		replay(thoughtRepositoryMock);
 		
 		Mind mind = new Mind(thoughtRepositoryMock);
 		Collection<QuickThought> allThoughtsKnownToMyMind = mind.allMyThoughts();
 		
 		assertTrue(allThoughtsKnownToMyMind.contains(firstThought));
 		assertTrue(allThoughtsKnownToMyMind.contains(secondThought));
 		
 		verify(thoughtRepositoryMock);
 	}
 	
 	@Test
 	public void searching_by_tag_should_return_all_thoughts() throws Exception {
 		
 		QuickThought gaeThought = new QuickThought("Google app engine is the coolest thing I have done all my life", "gae,java");
 		QuickThought pythonOnGaeThought = new QuickThought("Google app engine supports both python and java", "gae,python, java");
 		
 		ThoughtRepository thoughtRepository = createMock(ThoughtRepository.class);
 		String tag = "gae";
 		
 		ArrayList<QuickThought> thoughts = new ArrayList<QuickThought>();
 		
 		thoughts.add(gaeThought);
 		thoughts.add(pythonOnGaeThought);
 		
 		expect(thoughtRepository.allThoughtsTaggedWith(new Tag(tag))).andReturn(thoughts);
 		replay(thoughtRepository);
 		
 		Mind myMind = new Mind(thoughtRepository);
 		
 		Collection<QuickThought> allMyThoughts = myMind.allThoughtTaggedWith(tag);
 		
 		assertTrue(allMyThoughts.contains(gaeThought));
 		assertTrue(allMyThoughts.contains(pythonOnGaeThought));
 	}
 }
