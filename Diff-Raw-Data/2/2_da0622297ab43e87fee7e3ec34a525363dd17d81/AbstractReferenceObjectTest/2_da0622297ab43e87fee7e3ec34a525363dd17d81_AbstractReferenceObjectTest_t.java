 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.domain;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 /**
  * {@link AbstractReferenceObject} のユニットテスト
  * @author nilcy
  */
@SuppressWarnings("all")
 public final class AbstractReferenceObjectTest {
     /** テスト用の参照オブジェクト */
     private TestReferenceObject testee;
     /** 初期処理 */
     @Before
     public void before() {
         this.testee = new TestReferenceObject();
     }
     /** {@link AbstractReferenceObject#AbstractReferenceObject()} のユニットテスト */
     @Test
     public void testAbstractReferenceObject() {
         assertThat(this.testee, is(not(nullValue())));
     }
     /**
      * {@link AbstractReferenceObject#getId()} と
      * {@link AbstractReferenceObject#setId(Long)} のユニットテスト
      */
     @Test
     public void testGetSetId() {
         assertThat(this.testee.getId(), is(nullValue()));
         this.testee.setId(Long.valueOf(0L));
         assertThat(this.testee.getId(), is(Long.valueOf(0L)));
     }
     /**
      * {@link AbstractReferenceObject#sameIdentityAs(AbstractReferenceObject)}
      * のユニットテスト
      */
     @Test
     public void testSameIdentityAs() {
         final TestReferenceObject nullObject = null;
         assertThat(this.testee.sameIdentityAs(nullObject), is(false));
         final TestReferenceObject other = new TestReferenceObject();
         assertThat(this.testee.sameIdentityAs(other), is(true));
         this.testee.setId(0L);
         other.setId(0L);
         assertThat(this.testee.sameIdentityAs(other), is(true));
         this.testee.setId(0L);
         other.setId(1L);
         assertThat(this.testee.sameIdentityAs(other), is(false));
     }
     @Test
     public void testIdentity() {
         assertThat(this.testee.identity(), is(nullValue()));
         this.testee.setId(0L);
         assertThat(this.testee.identity(), is(0L));
     }
 }
