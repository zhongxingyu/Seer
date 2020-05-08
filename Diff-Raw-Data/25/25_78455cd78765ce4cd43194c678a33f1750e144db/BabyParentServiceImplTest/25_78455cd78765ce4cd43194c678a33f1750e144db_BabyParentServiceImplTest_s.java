/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package ro.danix.first.model.service.user.impl;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import static org.hamcrest.Matchers.*;
 import java.math.BigInteger;
 import lombok.extern.slf4j.Slf4j;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.test.util.ReflectionTestUtils;
 import ro.danix.first.exception.ExceptionUtils;
 import ro.danix.first.model.domain.user.BabyParent;
 import ro.danix.first.model.domain.user.User;
 import ro.danix.first.model.domain.user.factory.BabyParentFactory;
 import ro.danix.first.model.repository.user.BabyParentRepository;
 import ro.danix.test.FastRunningTests;
 
 /**
  *
  * @author moro
  */
 @Category(FastRunningTests.class)
 @RunWith(MockitoJUnitRunner.class)
 @Slf4j
 public class BabyParentServiceImplTest {
 
     @Mock
     private ExceptionUtils exceptionUtils;
     
     @Mock
     private BabyParentRepository babyParentRepository;
     
     @InjectMocks
     BabyParentServiceImpl babyParentService;
     
     BabyParentFactory babyParentFactory = new BabyParentFactory();
         
     @Before
     public void setUp() {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
    public void testFollow() {
         // given
         log.info("start followTest");
 
         BabyParent babyParent = babyParentFactory.build();
         ReflectionTestUtils.setField(babyParent, "id", new BigInteger("1"));
         BabyParent follower = babyParentFactory.build();
         ReflectionTestUtils.setField(follower, "id", new BigInteger("2"));
         // when
         babyParentService.follow(babyParent, follower);
         //then
         assertThat(babyParent.getFollowersCount(), is(new Long(1)));
         assertThat(babyParent.getFollowerIds(), hasItem(follower.getId()));
 
         assertThat(follower.getFollowingCount(), is(new Long(1)));
         assertThat(follower.getFollowingIds(), hasItem(babyParent.getId()));       
 
     }
 }
